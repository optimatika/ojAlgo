/*
 * Copyright 1997-2018 Optimatika
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.ojalgo.optimisation;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import org.ojalgo.ProgrammingError;
import org.ojalgo.access.Access1D;
import org.ojalgo.array.Array1D;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.integer.IntegerSolver;
import org.ojalgo.type.CalendarDateUnit;
import org.ojalgo.type.TypeUtils;
import org.ojalgo.type.context.NumberContext;

public interface Optimisation {

    /**
     * Constraint
     *
     * @author apete
     */
    public static interface Constraint extends Optimisation {

        /**
         * The lower limit/bound - may return null.
         */
        BigDecimal getLowerLimit();

        /**
         * The upper limit/bound - may return null.
         */
        BigDecimal getUpperLimit();

        /**
         * The Constraint has a lower or an upper limit actually set (possibly both) - it actually is
         * constained.
         */
        boolean isConstraint();

        /**
         * The Constraint has both a lower limit and an upper limit, and they are equal.
         */
        boolean isEqualityConstraint();

        /**
         * The Constraint has a lower limit, and the upper limit (if it exists) is different.
         */
        boolean isLowerConstraint();

        /**
         * The Constraint has an upper limit, and the lower limit (if it exists) is different.
         */
        boolean isUpperConstraint();

    }

    public static interface Integration<M extends Optimisation.Model, S extends Optimisation.Solver> extends Optimisation {

        /**
         * An integration must be able to instantiate a solver that can handle (any) model instance.
         */
        S build(M model);

        /**
         * Extract state from the model and convert it to solver state.
         */
        Optimisation.Result extractSolverState(M model);

        /**
         * @return true if this solver (integration) can handle the input model
         */
        boolean isCapable(M model);

        /**
         * Convert solver state to model state. Transforming the solution (set of variable values) is the main
         * concern. Adjusting the objective function value (if needed) is best handled elsewhere, and is not
         * required here.
         */
        Optimisation.Result toModelState(Optimisation.Result solverState, M model);

        /**
         * Convert model state to solver state. Transforming the solution (set of variable values) is the main
         * concern. Adjusting the objective function value (if needed) is best handled elsewhere, and is not
         * required here.
         */
        Optimisation.Result toSolverState(Optimisation.Result modelState, M model);

    }

    public static interface Model extends Optimisation {

        /**
         * Cleanup when a model instance is no longer needed. The default implementation does nothing,
         */
        default void dispose() {
            ;
        }

        Optimisation.Result maximise();

        Optimisation.Result minimise();

        /**
         * @return true If eveything is ok. false The model is structurally ok, but the "value" breaks
         *         constraints - the solution is infeasible.
         */
        boolean validate();

    }

    /**
     * Objective
     *
     * @author apete
     */
    public static interface Objective extends Optimisation {

        /**
         * The weight/factor by which this model entity's value contributes to the objective function - may
         * return null.
         */
        BigDecimal getContributionWeight();

        /**
         * @return true if this Objective has a non zero contribution weight - it actually is contributing to
         *         the objective function.
         */
        boolean isObjective();

    }

    public static final class Options implements Optimisation, Cloneable {

        /**
         * Used to determine/validate feasibility. Are the constraints violated or not? Are the variable
         * values integer or not?
         */
        public NumberContext feasibility = new NumberContext(12, 8, RoundingMode.HALF_EVEN);

        /**
         * The maximmum number of iterations allowed for the solve() command.
         */
        public int iterations_abort = Integer.MAX_VALUE;

        /**
         * Calculations will be terminated after this number of iterations if a feasible solution has been
         * found. If no feasible solution has been found calculations will continue until one is found or
         * {@linkplain #iterations_abort} is reached. This option is, probably, only of interest with the
         * {@linkplain IntegerSolver}.
         */
        public int iterations_suffice = Integer.MAX_VALUE;

        /**
         * If this is null nothing is printed, if it is not null then progress/debug messages are printed to
         * that {@linkplain org.ojalgo.netio.BasicLogger.Printer}.
         */
        public BasicLogger.Printer logger_appender = null;

        /**
         * Detailed (debug) logging or not.
         */
        public boolean logger_detailed = true;

        /**
         * Which {@linkplain Solver} to debug. Null means NO solvers. This setting is only relevant if
         * {@link #logger_appender} has been set.
         */
        public Class<? extends Optimisation.Solver> logger_solver = null;

        /**
         * The (relative) MIP gap is the difference between the best integer solution found so far and a
         * node's non-integer solution, relative to the optimal value (approximated by the currently best
         * integer solution). If the gap is smaller than this value, then the corresponding branch i
         * terminated as it is deemed unlikely or too "expensive" to find better integer solutions there.
         */
        public double mip_gap = 1.0E-4;

        /**
         * For display only!
         */
        public NumberContext print = NumberContext.getGeneral(8, 10);

        /**
         * Used when copying the solver's solution back to the model (converting from double to BigDecimal).
         * Variable values, dual variable values, lagrange multipliers...
         */
        public NumberContext solution = new NumberContext(12, 14, RoundingMode.HALF_DOWN);

        /**
         * The maximmum number of millis allowed for the solve() command. Executions will be aborted
         * regardless of if a solution has been found or not.
         */
        public long time_abort = CalendarDateUnit.MILLENIUM.size();

        /**
         * Calculations will be terminated after this amount of time if a feasible solution has been found. If
         * no feasible solution has been found calculations will continue until one is found or
         * {@linkplain #time_abort} is reached. This option is , probably, only of interest with the
         * {@linkplain IntegerSolver}.
         */
        public long time_suffice = CalendarDateUnit.DAY.size();

        /**
         * If true models and solvers will validate data at various points. Validation is turned off by
         * default. Turning it on will significantly slow down execution - even very expensive validation may
         * be performed.
         */
        public boolean validate = false;

        private Object myConfigurator = null;

        public Options() {
            super();
        }

        /**
         * @deprecated Since v45 Wont be Cloneable either
         */
        @Deprecated
        public Options copy() {
            try {
                return (Options) this.clone();
            } catch (final CloneNotSupportedException exception) {
                return null;
            }
        }

        /**
         * Will configure detailed dubug logging and validation
         *
         * @param solver
         */
        public void debug(final Class<? extends Optimisation.Solver> solver) {
            logger_solver = solver;
            logger_appender = solver != null ? BasicLogger.DEBUG : null;
            logger_detailed = solver != null ? true : false;
            validate = solver != null ? true : false;
        }

        @SuppressWarnings("unchecked")
        public <T> Optional<T> getConfigurator(final Class<T> type) {
            ProgrammingError.throwIfNull(type);
            if ((myConfigurator != null) && type.isInstance(myConfigurator)) {
                return Optional.of((T) myConfigurator);
            } else {
                return Optional.empty();
            }
        }

        /**
         * Will configure high level (low volume) progress logging
         *
         * @param solver
         */
        public void progress(final Class<? extends Optimisation.Solver> solver) {
            logger_solver = solver;
            logger_appender = solver != null ? BasicLogger.DEBUG : null;
            logger_detailed = false;
            validate = false;
        }

        public void setConfigurator(final Object configurator) {
            ProgrammingError.throwIfNull(configurator);
            myConfigurator = configurator;
        }

        /**
         * @deprecated Since v45 Don't copy or clone these, create them the waynyou need them.
         */
        @Override
        @Deprecated
        protected Object clone() throws CloneNotSupportedException {
            return super.clone();
        }
    }

    public static final class Result implements Optimisation, Access1D<BigDecimal>, Comparable<Optimisation.Result>, Serializable {

        private transient Access1D<?> myMultipliers = null;
        private final Access1D<?> mySolution;
        private final Optimisation.State myState;
        private final double myValue; // Objective Function Value

        public Result(final Optimisation.State state, final Access1D<?> solution) {
            this(state, Double.NaN, solution);
        }

        public Result(final Optimisation.State state, final double value, final Access1D<?> solution) {

            super();

            ProgrammingError.throwIfNull(state, solution);

            myState = state;
            myValue = value;
            mySolution = solution;
        }

        public Result(final Optimisation.State state, final Optimisation.Result result) {
            this(state, result.getValue(), result);
        }

        public int compareTo(final Result reference) {
            return NumberContext.compare(myValue, reference.getValue());
        }

        public long count() {
            return mySolution.count();
        }

        public double doubleValue(final long index) {
            return mySolution.doubleValue(index);
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (this.getClass() != obj.getClass()) {
                return false;
            }
            final Result other = (Result) obj;
            if (myState != other.myState) {
                return false;
            }
            if (Double.doubleToLongBits(myValue) != Double.doubleToLongBits(other.myValue)) {
                return false;
            }
            return true;
        }

        public BigDecimal get(final long index) {
            return TypeUtils.toBigDecimal(mySolution.get(index));
        }

        /**
         * The dual variables or Lagrange multipliers associated with the problem.
         */
        public Optional<Access1D<?>> getMultipliers() {
            return Optional.ofNullable(myMultipliers);
        }

        public Optimisation.State getState() {
            return myState;
        }

        /**
         * Objective Function Value
         */
        public double getValue() {
            return myValue;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = (prime * result) + ((myState == null) ? 0 : myState.hashCode());
            long temp;
            temp = Double.doubleToLongBits(myValue);
            result = (prime * result) + (int) (temp ^ (temp >>> 32));
            return result;
        }

        public Result multipliers(final Access1D<?> multipliers) {
            myMultipliers = multipliers;
            return this;
        }

        public int size() {
            return (int) this.count();
        }

        @Override
        public String toString() {
            return myState + " " + myValue + " @ " + Array1D.PRIMITIVE64.copy(mySolution);
        }

    }

    /**
     * <p>
     * An {@linkplain Optimisation.Solver} instance implements a specific optimisation algorithm. Typically
     * each algorithm solves problems of (at least) one problem category. {@linkplain Optimisation.Model}
     * represents a problem category.
     * </p>
     * <p>
     * A solver internally works with primitive double.
     * </p>
     *
     * @author apete
     */
    public static interface Solver extends Optimisation {

        /**
         * Cleanup when a solver instance is no longer needed. The default implementation does nothing,
         */
        default void dispose() {
            ;
        }

        default Optimisation.Result solve() {
            return this.solve(null);
        }

        Optimisation.Result solve(Optimisation.Result kickStarter);

    }

    public static enum State implements Optimisation {

        /**
         * Approximate and/or Intermediate solution - Iteration point Probably infeasible, but still "good"
         */
        APPROXIMATE(8),

        /**
         * Unique (and optimal) solution - there is no other solution that is equal or better
         */
        DISTINCT(256),

        /**
         * Unexpected failure or exception
         */
        FAILED(-1),

        /**
         * Solved - a solution that complies with all constraints
         */
        FEASIBLE(16),

        /**
         * No solution that complies with all constraints exists
         */
        INFEASIBLE(-8),

        /**
         * The problem/model is infeasible, unbounded or otherwise invalid.
         */
        INVALID(-2),

        /**
         * Optimal solution - there is no better
         */
        OPTIMAL(64),

        /**
         * There's an infinite number of feasible solutions and no bound on the objective function value.
         * Please note that using this state indicator implies a feasible solution. If a feasible solution has
         * not been been found you should instead use {@link #INFEASIBLE} or {@link #INVALID}.
         */
        UNBOUNDED(-32),

        /**
         * New/changed problem
         */
        UNEXPLORED(0),

        /**
         * Model entities and solver components (matrices) are valid
         */
        VALID(4);

        private final int myValue;

        State(final int aValue) {
            myValue = aValue;
        }

        public boolean isApproximate() {
            return (this == APPROXIMATE) || this.isFeasible();
        }

        public boolean isDistinct() {
            return this.absValue() >= DISTINCT.absValue();
        }

        /**
         * FAILED, INVALID, INFEASIBLE or UNBOUNDED
         */
        public boolean isFailure() {
            return myValue < 0;
        }

        public boolean isFeasible() {
            return this.absValue() >= FEASIBLE.absValue();
        }

        public boolean isOptimal() {
            return this.absValue() >= OPTIMAL.absValue();
        }

        /**
         * VALID, APPROXIMATE, FEASIBLE, OPTIMAL or DISTINCT
         */
        public boolean isSuccess() {
            return myValue > 0;
        }

        /**
         * UNEXPLORED
         */
        public boolean isUnexplored() {
            return myValue == 0;
        }

        public boolean isValid() {
            return this.absValue() >= VALID.absValue();
        }

        private int absValue() {
            return Math.abs(myValue);
        }

    }

}

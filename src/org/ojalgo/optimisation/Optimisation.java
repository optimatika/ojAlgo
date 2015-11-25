/*
 * Copyright 1997-2015 Optimatika (www.optimatika.se)
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
         * May return null
         */
        BigDecimal getLowerLimit();

        /**
         * May return null
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
         * Convert solver state to model state.
         */
        Optimisation.Result toModelState(Optimisation.Result solverState, M model);

        /**
         * Convert model state to solver state.
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
         * @return true If eveything is ok
         * @return false The model is structurally ok, but the "value" breaks constraints - the solution is
         *         infeasible.
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
         * May return null
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
         * If this is null nothing is printed, if it is not null then debug statements are printed to that
         * {@linkplain BasicLogger.Printer}.
         */
        public BasicLogger.Printer debug_appender = null;

        /**
         * Which {@linkplain Solver} to debug. Null means NO solvers. This setting is only relevant if
         * {@link #debug_appender} has been set.
         */
        public Class<? extends Optimisation.Solver> debug_solver = null;

        /**
         * Used to determine if a variable value is integer or not.
         */
        public NumberContext integer = new NumberContext(12, 7, RoundingMode.HALF_EVEN);

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
         * The (relative) MIP gap is the difference between the best integer solution found so far and a
         * node's non-integer solution, relative to the optimal value. If the gap is smaller than this value,
         * then the corresponding branch i terminated as it is deemed unlikely or too "expensive" to find
         * better integer solutions there.
         */
        public double mip_gap = 1.0E-4;

        /**
         * Used to compare/check objective function values (incl. temporary, phase 1, objectives). The most
         * importatnt use of this parameter is, with the linear (simplex) solver, to determine if the phase 1
         * objective function value is zero or not. Thus it is used to determine if the problem is feasible or
         * not.
         * <ul>
         * <li>2015-01-30: Changed from 12,7 to 12,8 to be able to handle LinearProblems.testP20150127()</li>
         * </ul>
         */
        public NumberContext objective = new NumberContext(12, 8, RoundingMode.HALF_EVEN);

        /**
         * For display only!
         */
        public NumberContext print = NumberContext.getGeneral(8, 10);

        /**
         * Problem parameters; constraints and objectives The numbers used to state/describe the problem,
         * incl. when/if these are transformed during the solution algorithm.
         * <ul>
         * <li>2014-09-29: Changed from 11,9 to 12,8</li>
         * </ul>
         */
        public NumberContext problem = new NumberContext(12, 8, RoundingMode.HALF_EVEN);

        /**
         * Used to determine if a constraint is violated or not. Essentially this context determines if the
         * various validate(...) methods will return true or false. Calculate the slack - zero if the
         * constraint is "active" - and check the sign.
         * <ul>
         * <li>2015-09-05: Changed from 14,8 to 12,8 (the "8" can/should probably be increased)</li>
         * <li>2015-09-09: Changed from 12,8 to 10,8 (the "8" can only be increased if some test cases are
         * rewritten)</li>
         * </ul>
         */
        public NumberContext slack = new NumberContext(10, 8, RoundingMode.HALF_DOWN);

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

        public Options() {
            super();
        }

        public Options copy() {
            try {
                return (Options) this.clone();
            } catch (final CloneNotSupportedException anException) {
                return null;
            }
        }

        /**
         * Will set {@link #debug_appender} to BasicLogger#DEBUG, {@link #debug_solver} to solver and
         * {@link #validate} to true.
         *
         * @param solver
         */
        public void debug(final Class<? extends Optimisation.Solver> solver) {
            debug_appender = BasicLogger.DEBUG;
            debug_solver = solver;
            validate = true;
        }

        @Override
        protected Object clone() throws CloneNotSupportedException {
            return super.clone();
        }
    }

    public static final class Result implements Optimisation, Access1D<BigDecimal>, Comparable<Optimisation.Result>, Serializable {

        private final Access1D<?> mySolution;
        private final Optimisation.State myState;
        private final double myValue; // Objective Function Value

        public Result(final Optimisation.State state, final Access1D<?> solution) {
            this(state, Double.NaN, solution);
        }

        public Result(final Optimisation.State state, final double value, final Access1D<?> solution) {

            super();

            ProgrammingError.throwIfNull(state);
            ProgrammingError.throwIfNull(solution);

            myState = state;
            myValue = value;
            mySolution = solution;
        }

        public Result(final Optimisation.State state, final Optimisation.Result result) {
            this(state, result.getValue(), result);
        }

        public int compareTo(final Result reference) {

            final double tmpRefValue = reference.getValue();

            if (myValue > tmpRefValue) {
                return 1;
            } else if (myValue < tmpRefValue) {
                return -1;
            } else {
                return 0;
            }
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

        public int size() {
            return (int) this.count();
        }

        @Override
        public String toString() {
            return myState + " " + myValue + " @ " + Array1D.PRIMITIVE.copy(mySolution);
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
         * Optimal, but not distinct solution - there are other solutions that are equal, but not better.
         *
         * @deprecated v39 Use OPTIMAL instead
         */
        INDISTINCT(-128),

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
         * There's an infinite number of feasible solutions and no bound on the objective function value
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
         * FAILED, INVALID, INFEASIBLE, UNBOUNDED or INDISTINCT
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

/*
 * Copyright 1997-2022 Optimatika
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.Optional;

import org.ojalgo.ProgrammingError;
import org.ojalgo.array.ArrayR064;
import org.ojalgo.array.ArrayR128;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.integer.IntegerSolver;
import org.ojalgo.optimisation.integer.IntegerStrategy;
import org.ojalgo.structure.Access1D;
import org.ojalgo.type.CalendarDateDuration;
import org.ojalgo.type.CalendarDateUnit;
import org.ojalgo.type.TypeUtils;
import org.ojalgo.type.context.NumberContext;

public interface Optimisation {

    /**
     * Constraint
     *
     * @author apete
     */
    public interface Constraint extends Optimisation {

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

    public enum ConstraintType implements Optimisation {

        /**
         * Corresponds to setting {@link ModelEntity#level(Comparable)} and/or checking
         * {@link Constraint#isEqualityConstraint()}.
         */
        EQUALITY,
        /**
         * Corresponds to setting {@link ModelEntity#lower(Comparable)} and/or checking
         * {@link Constraint#isLowerConstraint()}.
         */
        LOWER,
        /**
         * Unconstrained
         */
        NONE,
        /**
         * Corresponds to setting {@link ModelEntity#upper(Comparable)} and/or checking
         * {@link Constraint#isUpperConstraint()}.
         */
        UPPER;

    }

    /**
     * An {@link Optimisation.Model} implementation should not depend on any specific
     * {@link Optimisation.Solver}, and {@link Optimisation.Solver} implementations should be usable
     * independently of any {@link Optimisation.Model}. For every specific combination of
     * {@link Optimisation.Model} and {@link Optimisation.Solver} (that should function together) there needs
     * to be an {@link Optimisation.Integration}.
     *
     * @author apete
     */
    public interface Integration<M extends Optimisation.Model, S extends Optimisation.Solver> extends Optimisation {

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

    public interface Model extends Optimisation {

        /**
         * Cleanup when a model instance is no longer needed.
         */
        default void dispose() {
            // The default implementation does nothing.
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
    public interface Objective extends Optimisation {

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

    public static final class Options implements Optimisation {

        /**
         * Used to determine/validate feasibility. Are the constraints violated or not? Are the variable
         * values integer or not?
         */
        public NumberContext feasibility = NumberContext.of(12, 8);

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
         * that {@linkplain org.ojalgo.netio.BasicLogger}.
         */
        public BasicLogger logger_appender = null;

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
         * @deprecated v51.1.0 No longer used for anything! Instead it is possible to specify an
         *             {@link IntegerStrategy} that offers much more control.
         */
        @Deprecated
        public double mip_defer = 0.99;

        /**
         * @deprecated v51.1.0 No longer used! Use {@link IntegerStrategy#getGapTolerance()} instead.
         */
        @Deprecated
        public double mip_gap = 1.0E-6;

        /**
         * For display only!
         */
        public NumberContext print = NumberContext.of(8, 10);

        /**
         * Describes the (required/sufficient) accuracy of the solution. It is used when copying the solver's
         * solution back to the model (converting from double to BigDecimal). Specific solvers may also use
         * this as a stopping criteria or similar. The default essentially copies the numbers as is –
         * corresponding to full double precision.
         */
        public NumberContext solution = NumberContext.ofScale(14).withMode(RoundingMode.HALF_DOWN);

        /**
         * Controls if sparse/iterative solvers should be favoured over dense/direct alternatives.
         * Sparse/iterative alternatives are usually preferable with larger models, but there are also
         * algorithmical differences that could make one alternative better than the other for a (your)
         * specific case. There are 3 different possibilities for this option:
         * <ol>
         * <li><b>TRUE</b> Will use the sparse linear solver and the iterative convex solver.</li>
         * <li><b>FALSE</b> Will use the dense linear solver and the direct convex solver.</li>
         * <li><b>NULL</b> ojAlgo will use some logic to choose for you. This is the default. Currently, the
         * dense LinearSolver and the iterative ConvexSolver will be used. In the vast majority of cases these
         * are the best alternatives.</li>
         * </ol>
         * In most cases you do not need to worry about this configuration option - leave this choice to
         * ojAlgo.
         */
        public Boolean sparse = null;

        /**
         * The maximmum number of millis allowed for the solve() command. Executions will be aborted
         * regardless of if a solution has been found or not.
         */
        public long time_abort = CalendarDateUnit.DAY.toDurationInMillis();

        /**
         * Calculations will be terminated after this amount of time if a feasible solution has been found. If
         * no feasible solution has been found calculations will continue until one is found or
         * {@linkplain #time_abort} is reached. This option is , probably, only of interest with the
         * {@linkplain IntegerSolver}.
         */
        public long time_suffice = CalendarDateUnit.HOUR.toDurationInMillis();

        /**
         * If true models and solvers will validate data at various points. Validation is turned off by
         * default. Turning it on will significantly slow down execution - even very expensive validation may
         * be performed.
         */
        public boolean validate = false;

        private Object myConfigurator = null;
        private IntegerStrategy myIntegerStrategy = IntegerStrategy.DEFAULT;

        public Options() {
            super();
        }

        public Options abort(final CalendarDateDuration duration) {
            ProgrammingError.throwIfNull(duration);
            time_abort = duration.toDurationInMillis();
            return this;
        }

        /**
         * Will configure detailed debug logging and validation
         */
        public void debug(final Class<? extends Optimisation.Solver> solver) {
            logger_solver = solver;
            logger_appender = solver != null ? BasicLogger.DEBUG : null;
            logger_detailed = (solver != null);
            validate = (solver != null);
        }

        public <T> Optional<T> getConfigurator(final Class<T> type) {
            ProgrammingError.throwIfNull(type);
            if (myConfigurator != null && type.isInstance(myConfigurator)) {
                return Optional.of((T) myConfigurator);
            }
            return Optional.empty();
        }

        public IntegerStrategy integer() {
            return myIntegerStrategy;
        }

        /**
         * Set the strategy/configuration for the {@link IntegerSolver}. You can either reconfigure the
         * {@link IntegerStrategy#DEFAULT} instance or create an entirely new implementation of the interface.
         */
        public Options integer(final IntegerStrategy strategy) {
            Objects.requireNonNull(strategy);
            myIntegerStrategy = strategy;
            return this;
        }

        /**
         * Will configure high level (low volume) progress logging
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

        public Options suffice(final CalendarDateDuration duration) {
            ProgrammingError.throwIfNull(duration);
            time_suffice = duration.toDurationInMillis();
            return this;
        }

    }

    public static final class Result implements Optimisation, Access1D<BigDecimal>, Comparable<Optimisation.Result> {

        public static Result of(final double value, final Optimisation.State state, final double... solution) {
            return new Result(state, value, ArrayR064.wrap(solution));
        }

        public static Result of(final Optimisation.State state, final double... solution) {
            return new Result(state, Double.NaN, Access1D.wrap(solution));
        }

        /**
         * Parse a {@link String}, as produced by the {@link #toString()} method, into a new instance.
         */
        public static Result parse(final String result) {

            int indexOfFirstSpace = result.indexOf(" ");
            int indexOfAtMark = result.indexOf(" @ ");

            String strState = result.substring(0, indexOfFirstSpace);
            String strValue = result.substring(indexOfFirstSpace + 1, indexOfAtMark);
            String[] strSolution = result.substring(indexOfAtMark + 5, result.length() - 2).split(", ");

            State state = Optimisation.State.valueOf(strState);
            double value = Double.parseDouble(strValue);
            ArrayR128 solution = ArrayR128.make(strSolution.length);
            for (int i = 0; i < strSolution.length; i++) {
                solution.set(i, new BigDecimal(strSolution[i]));
            }

            return new Result(state, value, solution);
        }

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
            if (obj == null || this.getClass() != obj.getClass()) {
                return false;
            }
            final Result other = (Result) obj;
            if (myState != other.myState || Double.doubleToLongBits(myValue) != Double.doubleToLongBits(other.myValue)) {
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

        /**
         * Will round the solution to the given precision
         */
        public Optimisation.Result getSolution(final NumberContext precision) {
            Optimisation.State state = this.getState();
            double value = this.getValue();
            ArrayR128 solution = ArrayR128.make(this.size());
            for (int i = 0, limit = solution.data.length; i < limit; i++) {
                solution.set(i, precision.enforce(this.get(i)));
            }
            return new Optimisation.Result(state, value, solution);
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
            result = prime * result + (myState == null ? 0 : myState.hashCode());
            long temp;
            temp = Double.doubleToLongBits(myValue);
            return prime * result + (int) (temp ^ temp >>> 32);
        }

        public Result multipliers(final Access1D<?> multipliers) {
            myMultipliers = multipliers;
            return this;
        }

        public int size() {
            return (int) this.count();
        }

        /**
         * May potentially be a very long {@link String} as it must contain all variable values. The
         * {@link String} produced here is (must be) usable by the {@link #parse(String)} method.
         */
        @Override
        public String toString() {
            return myState + " " + myValue + " @ " + Access1D.toString(mySolution);
        }

        public Result withState(final State state) {
            return new Result(state, myValue, mySolution);
        }

    }

    public enum Sense implements Optimisation {
        MAX, MIN;
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
    public interface Solver extends Optimisation {

        /**
         * Cleanup when a solver instance is no longer needed.
         */
        default void dispose() {
            // The default implementation does nothing.
        }

        default Optimisation.Result solve() {
            return this.solve(null);
        }

        Optimisation.Result solve(Optimisation.Result kickStarter);

    }

    public enum State implements Optimisation {

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
         * No solution that complies with all constraints exists (found).
         * <p>
         * In practise this often means "infeasible or unbounded". The key thing is that with this state the
         * returned solution is not (known to be) feasible.
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
         * <p>
         * Note that using this state indicator implies a feasible solution! This is not in line with how many
         * other optimisation tools interpret "unbounded".
         * <p>
         * If a feasible solution has not been found, the correct state indicator to use is
         * {@link #INFEASIBLE} or possibly {@link #INVALID}, {@link} #FAILED} or {@link #APPROXIMATE}.
         * <p>
         * If a problem is concluded to be unbounded but a feasible solution has been found, it may still be
         * preferable to us {@link #FEASIBLE} rather than {@link #UNBOUNDED}.
         * <p>
         * There is, unfortunately, no way to convey that a problem is proven to be unbounded without a
         * feasible solution.
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
            return this == APPROXIMATE || this.isFeasible();
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

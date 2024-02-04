/*
 * Copyright 1997-2024 Optimatika
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
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.ojalgo.ProgrammingError;
import org.ojalgo.array.ArrayR064;
import org.ojalgo.array.ArrayR256;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.convex.ConvexSolver;
import org.ojalgo.optimisation.integer.IntegerSolver;
import org.ojalgo.optimisation.integer.IntegerStrategy;
import org.ojalgo.optimisation.linear.LinearSolver;
import org.ojalgo.structure.Access1D;
import org.ojalgo.type.CalendarDateDuration;
import org.ojalgo.type.CalendarDateUnit;
import org.ojalgo.type.TypeUtils;
import org.ojalgo.type.context.NumberContext;
import org.ojalgo.type.keyvalue.EntryPair;
import org.ojalgo.type.keyvalue.EntryPair.KeyedPrimitive;

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
         * constrained.
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
         * <p>
         * The required behaviour here depends on how {@link #build(Optimisation.Model)} is implemented, and
         * is the reverse mapping of {@link #toSolverState(Optimisation.Result, Optimisation.Model)}.
         */
        Optimisation.Result toModelState(Optimisation.Result solverState, M model);

        /**
         * Convert model state to solver state. Transforming the solution (set of variable values) is the main
         * concern. Adjusting the objective function value (if needed) is best handled elsewhere, and is not
         * required here.
         * <p>
         * The required behaviour here depends on how {@link #build(Optimisation.Model)} is implemented, and
         * is the reverse mapping of {@link #toModelState(Result, Optimisation.Model)}.
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
         * This may turn on various experimental features. If you do not know exactly what you want to turn
         * on, for the specific version you're using, then always leave this 'false'.
         */
        public boolean experimental = false;

        /**
         * Used to determine/validate feasibility. Are the variables within their bounds or not, are the
         * constraints violated or not? are the variable values integer or not?
         * <p>
         * Primarily used in {@link ExpressionsBasedModel}. Not used (should not be) as part of solver logic,
         * but ouside the solvers to validate their results.
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
         * For display only! {@link #toString()} and log message formatting.
         */
        public NumberContext print = ModelEntity.PRINT;

        /**
         * Describes the (required/sufficient) accuracy of the solution. It is used when copying the solver's
         * solution back to the model (converting from double to BigDecimal). Specific solvers may also use
         * this as a stopping criteria or similar. The default essentially copies the numbers as is –
         * corresponding to full double precision – but with no more than 14 decimals.
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
        private ConvexSolver.Configuration myConvexConfiguration = new ConvexSolver.Configuration();
        private IntegerStrategy myIntegerStrategy = IntegerStrategy.DEFAULT;
        private LinearSolver.Configuration myLinearConfiguration = new LinearSolver.Configuration();

        public Options() {
            super();
        }

        public Options abort(final CalendarDateDuration duration) {
            ProgrammingError.throwIfNull(duration);
            time_abort = duration.toDurationInMillis();
            return this;
        }

        /**
         * Configurations specific to ojAlgo's built-in {@link ConvexSolver}.
         */
        public ConvexSolver.Configuration convex() {
            return myConvexConfiguration;
        }

        public Options convex(final ConvexSolver.Configuration configuration) {
            Objects.requireNonNull(configuration);
            myConvexConfiguration = configuration;
            return this;
        }

        /**
         * Will configure detailed debug logging and validation
         */
        public Options debug(final Class<? extends Optimisation.Solver> solver) {
            logger_solver = solver;
            logger_appender = solver != null ? BasicLogger.DEBUG : null;
            logger_detailed = solver != null;
            validate = solver != null;
            return this;
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

        public LinearSolver.Configuration linear() {
            return myLinearConfiguration;
        }

        /**
         * Configurations specific to ojAlgo's built-in {@link LinearSolver}.
         */
        public Options linear(final LinearSolver.Configuration configuration) {
            Objects.requireNonNull(configuration);
            myLinearConfiguration = configuration;
            return this;
        }

        /**
         * Will configure high level (low volume) progress logging
         */
        public Options progress(final Class<? extends Optimisation.Solver> solver) {
            logger_solver = solver;
            logger_appender = solver != null ? BasicLogger.DEBUG : null;
            logger_detailed = false;
            validate = false;
            return this;
        }

        /**
         * A configurator for 3:d party solvers. Each such solver may define its own configurator type.
         */
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

    /**
     * Basic description of the size/structure of an optimisation problem.
     */
    public interface ProblemStructure extends Optimisation {

        static final boolean DEBUG = false;

        /**
         * Not included in {@link #countConstraints()} (because they are not simple linear equality or
         * inequality constraints),
         */
        int countAdditionalConstraints();

        default int countConstraints() {
            return this.countEqualityConstraints() + this.countInequalityConstraints();
        }

        int countEqualityConstraints();

        int countInequalityConstraints();

        int countVariables();

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
            ArrayR256 solution = ArrayR256.make(strSolution.length);
            for (int i = 0; i < strSolution.length; i++) {
                solution.set(i, new BigDecimal(strSolution[i]));
            }

            return new Result(state, value, solution);
        }

        public static Result wrap(final Access1D<?> solution) {
            return new Result(State.APPROXIMATE, Double.NaN, solution);
        }

        private ConstraintsMap myConstraintsMap = null;
        private List<KeyedPrimitive<EntryPair<ModelEntity<?>, ConstraintType>>> myMatchedMultipliers = null;
        private Access1D<?> myMultipliers = null;
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

        @Override
        public int compareTo(final Result reference) {
            return NumberContext.compare(myValue, reference.getValue());
        }

        @Override
        public long count() {
            return mySolution.count();
        }

        @Override
        public double doubleValue(final int index) {
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

        @Override
        public BigDecimal get(final long index) {
            return TypeUtils.toBigDecimal(mySolution.get(index));
        }

        /**
         * The dual variables or Lagrange multipliers, matched to their respective constraints (model entity
         * and constraint type pairs).
         */
        public List<KeyedPrimitive<EntryPair<ModelEntity<?>, ConstraintType>>> getMatchedMultipliers() {
            if (myMatchedMultipliers == null) {
                if (myConstraintsMap != null && myMultipliers != null && myConstraintsMap.isEntityMap() && myConstraintsMap.size() == myMultipliers.size()) {
                    myMatchedMultipliers = myConstraintsMap.match(myMultipliers);
                } else {
                    myMatchedMultipliers = List.of();
                }
            }
            return myMatchedMultipliers;
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

            ArrayR256 solution = ArrayR256.make(this.size());
            for (int i = 0, limit = solution.data.length; i < limit; i++) {
                solution.set(i, precision.enforce(this.get(i)));
            }

            return this.withSolution(solution);
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

        public Result multipliers(final ConstraintsMap constraintsMap, final Access1D<?> multipliers) {
            myConstraintsMap = constraintsMap;
            myMultipliers = multipliers;
            return this;
        }

        public Result multipliers(final double... multipliers) {
            return this.multipliers(ArrayR064.wrap(multipliers));
        }

        @Override
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

        public Result withNegatedValue() {
            return this.withValue(-myValue);
        }

        public Result withSolution(final Access1D<?> solution) {

            Result retVal = new Result(myState, myValue, solution);

            this.multipliers(retVal);

            return retVal;
        }

        public Result withSolutionLength(final int length) {

            ArrayR064 solution = ArrayR064.make(length);

            for (int i = 0, limit = Math.min(solution.size(), mySolution.size()); i < limit; i++) {
                solution.set(i, mySolution.doubleValue(i));
            }

            return this.withSolution(solution);
        }

        public Result withState(final State state) {

            Result retVal = new Result(state, myValue, mySolution);

            this.multipliers(retVal);

            return retVal;
        }

        public Result withValue(final double value) {

            Result retVal = new Result(myState, value, mySolution);

            this.multipliers(retVal);

            return retVal;
        }

        private void multipliers(final Result target) {

            Optional<Access1D<?>> multipliers = this.getMultipliers();
            if (multipliers.isPresent()) {
                target.multipliers(multipliers.get());
            }

            List<KeyedPrimitive<EntryPair<ModelEntity<?>, ConstraintType>>> matchedMultipliers = this.getMatchedMultipliers();
            if (matchedMultipliers.size() > 0) {
                target.multipliers(matchedMultipliers);
            }
        }

        Result multipliers(final List<KeyedPrimitive<EntryPair<ModelEntity<?>, ConstraintType>>> matchedMultipliers) {
            myMatchedMultipliers = matchedMultipliers;
            return this;
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

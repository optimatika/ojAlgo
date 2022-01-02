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

import java.math.RoundingMode;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.ojalgo.ProgrammingError;
import org.ojalgo.array.SparseArray;
import org.ojalgo.function.multiary.MultiaryFunction.TwiceDifferentiable;
import org.ojalgo.matrix.Primitive64Matrix;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore.Factory;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.matrix.store.RowsSupplier;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.RowView;
import org.ojalgo.type.CalendarDateUnit;
import org.ojalgo.type.Stopwatch;
import org.ojalgo.type.context.NumberContext;

public abstract class GenericSolver implements Optimisation.Solver {

    public static abstract class Builder<B extends Builder<?, ?>, S extends GenericSolver> {

        protected static final Factory<Double, Primitive64Store> FACTORY = Primitive64Store.FACTORY;

        protected static final void append(final StringBuilder builder, final String label, final MatrixStore<Double> matrix) {
            if (builder != null && label != null && matrix != null) {
                builder.append("\n[");
                builder.append(label);
                builder.append("] = ");
                builder.append(Primitive64Matrix.FACTORY.copy(matrix));
            }
        }

        private final OptimisationData myData = new OptimisationData();

        protected Builder() {
            super();
        }

        public final S build() {
            myData.validate();
            return this.doBuild(new Optimisation.Options());
        }

        public final S build(final Optimisation.Options options) {
            ProgrammingError.throwIfNull(options);
            myData.validate();
            return this.doBuild(options);
        }

        public int countAdditionalConstraints() {
            return myData.countAdditionalConstraints();
        }

        public int countConstraints() {
            return this.countEqualityConstraints() + this.countInequalityConstraints() + this.countAdditionalConstraints();
        }

        public int countEqualityConstraints() {
            return myData.countEqualityConstraints();
        }

        public int countInequalityConstraints() {
            return myData.countInequalityConstraints();
        }

        public int countVariables() {
            return myData.countVariables();
        }

        public B equalities(final Access2D<Double> mtrxAE, final Access1D<Double> mtrxBE) {
            myData.setEqualities(mtrxAE, mtrxBE);
            return (B) this;
        }

        /**
         * [AE][X] == [BE]
         */
        public MatrixStore<Double> getAE() {
            return myData.getAE();
        }

        /**
         * [AE][X] == [BE]
         */
        public MatrixStore<Double> getBE() {
            return myData.getBE();
        }

        /**
         * Will replace each equality constraint with two inequality constraints
         */
        public void splitEqualities() {

            if (this.hasEqualityConstraints()) {

                myData.addInequalities(myData.getAE(), myData.getBE());
                myData.addInequalities(myData.getAE().negate(), myData.getBE().negate());

                myData.clearEqualities();
            }
        }

        public MatrixStore<Double> getC() {
            return myData.getObjective().getLinearFactors();
        }

        public boolean hasEqualityConstraints() {
            return myData.countEqualityConstraints() > 0;
        }

        public boolean hasInequalityConstraints() {
            return myData.countInequalityConstraints() > 0;
        }

        /**
         * @deprecated v50 You have to have an objective function.
         */
        @Deprecated
        public boolean hasObjective() {
            return myData.getObjective() != null;
        }

        public void reset() {
            myData.reset();
        }

        @Override
        public final String toString() {

            String simpleName = this.getClass().getSimpleName();

            StringBuilder retVal = new StringBuilder();

            retVal.append("<");
            retVal.append(simpleName);
            retVal.append(">");

            this.append(retVal);

            retVal.append("\n</");
            retVal.append(simpleName);
            retVal.append(">");

            return retVal.toString();
        }

        /**
         * @deprecated v50 No need for you to call this explicitly. Validation is done for you.
         */
        @Deprecated
        public final void validate() {
            myData.validate();
        }

        protected void append(final StringBuilder builder) {
            Builder.append(builder, "AE", this.getAE());
            Builder.append(builder, "BE", this.getBE());
            Builder.append(builder, "AI", this.getAI());
            Builder.append(builder, "BI", this.getBI());
            Builder.append(builder, "C", this.getC());
        }

        protected abstract S doBuild(Optimisation.Options options);

        /**
         * [AI][X] &lt;= [BI]
         */
        protected MatrixStore<Double> getAI() {
            return myData.getAI();
        }

        protected SparseArray<Double> getAI(final int row) {
            return myData.getAI(row);
        }

        protected RowsSupplier<Double> getAI(final int... rows) {
            return myData.getAI(rows);
        }

        /**
         * [AI][X] &lt;= [BI]
         */
        protected MatrixStore<Double> getBI() {
            return myData.getBI();
        }

        protected double getBI(final int row) {
            return myData.getBI(row);
        }

        protected <T extends TwiceDifferentiable<Double>> T getObjective() {
            return myData.getObjective();
        }

        protected RowView<Double> getRowsAI() {
            return myData.getRowsAI();
        }

        protected B inequalities(final Access2D<Double> mtrxAI, final Access1D<Double> mtrxBI) {
            myData.setInequalities(mtrxAI, mtrxBI);
            return (B) this;
        }

        protected void setObjective(final TwiceDifferentiable<Double> objective) {
            myData.setObjective(objective);
        }

    }

    protected static final NumberContext ACCURACY = NumberContext.of(12, 14).withMode(RoundingMode.HALF_DOWN);

    public final Optimisation.Options options;

    private final AtomicInteger myIterationsCount = new AtomicInteger(0);
    private State myState = State.UNEXPLORED;
    private final Stopwatch myStopwatch = new Stopwatch();

    @SuppressWarnings("unused")
    private GenericSolver() {
        this(new Optimisation.Options());
    }

    /**
     */
    protected GenericSolver(final Optimisation.Options solverOptions) {

        super();

        ProgrammingError.throwIfNull(solverOptions);

        options = solverOptions;
    }

    protected Optimisation.Result buildResult() {

        Access1D<?> solution = this.extractSolution();
        double value = this.evaluateFunction(solution);
        Optimisation.State state = this.getState();

        return new Optimisation.Result(state, value, solution);
    }

    protected final int countIterations() {
        return myIterationsCount.get();
    }

    protected final long countTime() {
        return myStopwatch.countMillis();
    }

    protected final void error(final String messagePattern, final Object... arguments) {
        BasicLogger.error(messagePattern, arguments);
    }

    protected abstract double evaluateFunction(final Access1D<?> solution);

    /**
     * Should be able to feed this to {@link #evaluateFunction(Access1D)}.
     */
    protected abstract Access1D<?> extractSolution();

    protected State getState() {
        return myState;
    }

    /**
     * Should be called after a completed iteration. The iterations count is not "1" untill the first
     * iteration is completed.
     */
    protected final int incrementIterationsCount() {
        int iterationsDone = myIterationsCount.incrementAndGet();
        if (this.isLogProgress() && iterationsDone % 100_000 == 0) {
            this.log("Done {} {} iterations after {}.", iterationsDone, this.getClass().getSimpleName(), myStopwatch.stop(CalendarDateUnit.SECOND));
        }
        return iterationsDone;
    }

    /**
     * Should be called at the start of an iteration (before it actually starts) to check if you should abort
     * instead. Will return false if either the iterations count or the execution time has reached their
     * respective limits.
     */
    protected final boolean isIterationAllowed() {

        if (myState.isFailure() || Thread.currentThread().isInterrupted()) {
            return false;
        }

        if (myState.isFeasible()) {
            return this.countTime() < options.time_suffice && this.countIterations() < options.iterations_suffice;
        }

        return this.countTime() < options.time_abort && this.countIterations() < options.iterations_abort;
    }

    /**
     * Detailed debug logging
     */
    protected final boolean isLogDebug() {
        return options.logger_detailed && this.isLogProgress();
    }

    /**
     * No logging
     */
    protected final boolean isLogOff() {
        return options.logger_appender == null || !options.logger_solver.isAssignableFrom(this.getClass());
    }

    /**
     * Cursory progress logging (at least)
     */
    protected final boolean isLogProgress() {
        return options.logger_appender != null && options.logger_solver.isAssignableFrom(this.getClass());
    }

    protected final void log() {
        if (options.logger_appender != null) {
            options.logger_appender.println();
        }
    }

    protected final void log(final String descripttion, final Access2D<?> matrix) {
        if (options.logger_appender != null) {
            options.logger_appender.printmtrx(descripttion, matrix, options.print);
        }
    }

    protected final void log(final String messagePattern, final Object... arguments) {
        if (options.logger_appender != null) {
            options.logger_appender.println(messagePattern, arguments);
        }
    }

    protected final void resetIterationsCount() {
        myIterationsCount.set(0);
        myStopwatch.reset();
    }

    /**
     * As the solver algorithm reaches various states it should be recorded here. It's particularly important
     * to record when a feasible solution has been reached.
     */
    protected final void setState(final State state) {
        Objects.requireNonNull(state);
        myState = state;
    }

}

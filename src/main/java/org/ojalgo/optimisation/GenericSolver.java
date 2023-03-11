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
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PhysicalStore.Factory;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.matrix.store.RowsSupplier;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Access2D.RowView;
import org.ojalgo.type.CalendarDateDuration;
import org.ojalgo.type.CalendarDateUnit;
import org.ojalgo.type.Stopwatch;
import org.ojalgo.type.context.NumberContext;

public abstract class GenericSolver implements Optimisation.Solver {

    public static abstract class Builder<B extends Builder<B, S>, S extends GenericSolver> {

        protected static final Factory<Double, Primitive64Store> FACTORY = Primitive64Store.FACTORY;

        protected static final void append(final StringBuilder builder, final String label, final MatrixStore<Double> matrix) {
            if (builder != null && label != null && matrix != null) {
                builder.append("\n[");
                builder.append(label);
                builder.append("] = ");
                builder.append(Access2D.toString(matrix));
            }
        }

        private final BuilderData myData = new BuilderData();
        private transient int myNumberOfVariables = -1;

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

        public final int countAdditionalConstraints() {
            return myData.countAdditionalConstraints();
        }

        public final int countConstraints() {
            return myData.countConstraints();
        }

        public final int countEqualityConstraints() {
            return myData.countEqualityConstraints();
        }

        public final int countInequalityConstraints() {
            return myData.countInequalityConstraints();
        }

        public final int countVariables() {
            if (myNumberOfVariables < 0) {
                myNumberOfVariables = myData.countVariables();
            }
            return myNumberOfVariables;
        }

        public B equalities(final Access2D<?> mtrxAE, final Access1D<?> mtrxBE) {
            myData.setEqualities(mtrxAE, mtrxBE);
            return (B) this;
        }

        public B equality(final double rhs, final double... factors) {

            Primitive64Store mBody = FACTORY.make(1, this.countVariables());
            for (int i = 0, limit = Math.min(factors.length, this.countVariables()); i < limit; i++) {
                mBody.set(i, factors[i]);
            }

            MatrixStore<Double> mRHS = FACTORY.makeSingle(rhs);

            myData.addEqualities(mBody, mRHS);

            return (B) this;
        }

        public void reset() {
            myData.reset();
            myNumberOfVariables = -1;
        }

        public final Optimisation.Result solve() {
            return this.build().solve();
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
         * [AE][X] == [BE]
         */
        protected MatrixStore<Double> getAE() {
            return myData.getAE();
        }

        protected SparseArray<Double> getAE(final int row) {
            return myData.getAE(row);
        }

        protected RowsSupplier<Double> getAE(final int... rows) {
            return myData.getAE(rows);
        }

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
         * [AE][X] == [BE]
         */
        protected MatrixStore<Double> getBE() {
            return myData.getBE();
        }

        protected double getBE(final int row) {
            return myData.getBE(row);
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

        protected MatrixStore<Double> getC() {
            return myData.getObjective().getLinearFactors(false);
        }

        protected PhysicalStore.Factory<Double, Primitive64Store> getFactory() {
            return FACTORY;
        }

        protected double[] getLowerBounds(final double defaultValue) {
            return myData.getLowerBounds(defaultValue).data;
        }

        protected TwiceDifferentiable<Double> getObjective() {
            return myData.getObjective(TwiceDifferentiable.class);
        }

        protected <T extends TwiceDifferentiable<Double>> T getObjective(final Class<T> type) {
            return myData.getObjective(type);
        }

        protected OptimisationData getOptimisationData() {
            return myData;
        }

        protected RowView<Double> getRowsAE() {
            return myData.getRowsAE();
        }

        protected RowView<Double> getRowsAI() {
            return myData.getRowsAI();
        }

        protected double[] getUpperBounds(final double defaultValue) {
            return myData.getUpperBounds(defaultValue).data;
        }

        protected B inequalities(final Access2D<?> mtrxAI, final Access1D<?> mtrxBI) {
            myData.setInequalities(mtrxAI, mtrxBI);
            return (B) this;
        }

        protected B inequality(final double rhs, final double... factors) {

            Primitive64Store mBody = FACTORY.make(1, this.countVariables());
            for (int i = 0, limit = Math.min(factors.length, this.countVariables()); i < limit; i++) {
                mBody.set(i, factors[i]);
            }

            MatrixStore<Double> mRHS = FACTORY.makeSingle(rhs);

            myData.addInequalities(mBody, mRHS);

            return (B) this;
        }

        protected void setNumberOfVariables(final int numberOfVariables) {
            if (numberOfVariables < 0) {
                throw new IllegalArgumentException();
            }
            if (myNumberOfVariables >= 0 && myNumberOfVariables != numberOfVariables) {
                throw new IllegalStateException();
            }
            myNumberOfVariables = numberOfVariables;
        }

        protected void setObjective(final TwiceDifferentiable<Double> objective) {
            myData.setObjective(objective);
        }

    }

    /**
     * @deprecated Don't use/depend on this. Define new instances independent of this.
     */
    @Deprecated
    protected static final NumberContext ACCURACY = NumberContext.of(12, 14).withMode(RoundingMode.HALF_DOWN);

    public final Optimisation.Options options;

    private transient String myClassSimpleName = null;
    private final AtomicInteger myIterationsCount = new AtomicInteger(0);
    private State myState = State.UNEXPLORED;
    private final Stopwatch myStopwatch = new Stopwatch();

    @SuppressWarnings("unused")
    private GenericSolver() {
        this(new Optimisation.Options());
    }

    protected GenericSolver(final Optimisation.Options solverOptions) {

        super();

        ProgrammingError.throwIfNull(solverOptions);

        options = solverOptions;
    }

    protected final int countIterations() {
        return myIterationsCount.get();
    }

    /**
     * The number of ms since solver instantiated or iterations count reset.
     */
    protected final long countTime() {
        return myStopwatch.countMillis();
    }

    protected final void error(final String messagePattern, final Object... arguments) {
        BasicLogger.error(messagePattern, arguments);
    }

    protected final String getClassSimpleName() {
        if (myClassSimpleName == null) {
            myClassSimpleName = this.getClass().getSimpleName();
        }
        return myClassSimpleName;
    }

    /**
     * The number of s since solver instantiated or iterations count reset.
     */
    protected final CalendarDateDuration getDuration() {
        return myStopwatch.stop(CalendarDateUnit.SECOND);
    }

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
            this.logProgress(iterationsDone, this.getClassSimpleName(), this.getDuration());
        }
        return iterationsDone;
    }

    /**
     * Should be called at the start of an iteration (before it actually starts) to check if you should abort
     * instead. Will return false if either the iterations count or the execution time has reached their
     * respective limits.
     */
    protected final boolean isIterationAllowed() {

        if (myState.isFailure() || Thread.currentThread().isInterrupted() || myState.isOptimal()) {
            return false;
        }

        if (myState.isFeasible() && (this.countTime() >= options.time_suffice || this.countIterations() >= options.iterations_suffice)) {
            return false;
        }

        if (this.countTime() >= options.time_abort || this.countIterations() >= options.iterations_abort) {
            return false;
        }

        return true;
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

    protected final void log(final int tabs, final String messagePattern, final Object... arguments) {
        if (options.logger_appender != null) {
            options.logger_appender.println(tabs, messagePattern, arguments);
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

    protected void logProgress(final int iterationsDone, final String classSimpleName, final CalendarDateDuration duration) {
        this.log("Done {} {} iterations in {}.", iterationsDone, classSimpleName, duration);
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

/*
 * Copyright 1997-2019 Optimatika
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
import org.ojalgo.function.multiary.MultiaryFunction;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.matrix.store.RowsSupplier;
import org.ojalgo.matrix.store.SparseStore;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.type.CalendarDateUnit;
import org.ojalgo.type.Stopwatch;
import org.ojalgo.type.context.NumberContext;

public abstract class GenericSolver implements Optimisation.Solver {

    public static abstract class Builder<B extends Builder<?, ?>, S extends GenericSolver> {

        private MatrixStore<Double> myAE = null;
        private RowsSupplier<Double> myAI = null;
        private MatrixStore<Double> myBE = null;
        private MatrixStore<Double> myBI = null;
        private MultiaryFunction.TwiceDifferentiable<Double> myObjective;

        protected Builder() {
            super();
        }

        public final S build() {
            return this.doBuild(new Optimisation.Options());
        }

        public final S build(final Optimisation.Options options) {
            ProgrammingError.throwIfNull(options);
            return this.doBuild(options);
        }

        public int countConstraints() {
            return this.countEqualityConstraints() + this.countInequalityConstraints();
        }

        public int countEqualityConstraints() {
            return (int) ((this.getAE() != null) ? this.getAE().countRows() : 0);
        }

        public int countInequalityConstraints() {
            return (int) ((this.getAI() != null) ? this.getAI().countRows() : 0);
        }

        public int countVariables() {

            int retVal = -1;

            if (this.getAE() != null) {
                retVal = (int) this.getAE().countColumns();
            } else if (this.getAI() != null) {
                retVal = (int) this.getAI().countColumns();
            } else if (myObjective != null) {
                retVal = myObjective.arity();
            } else {
                throw new ProgrammingError("Cannot deduce the number of variables!");
            }

            return retVal;
        }

        @SuppressWarnings("unchecked")
        public B equalities(final MatrixStore<Double> mtrxAE, final MatrixStore<Double> mtrxBE) {

            ProgrammingError.throwIfNull(mtrxAE, mtrxBE);
            ProgrammingError.throwIfNotEqualRowDimensions(mtrxAE, mtrxBE);

            myAE = mtrxAE;
            myBE = mtrxBE;

            return (B) this;
        }

        /**
         * [AE][X] == [BE]
         */
        public MatrixStore<Double> getAE() {
            return myAE;
        }

        /**
         * [AI][X] &lt;= [BI]
         */
        public RowsSupplier<Double> getAI() {
            return myAI;
        }

        protected SparseArray<Double> getAI(final int row) {
            return myAI.getRow(row);
        }

        /**
         * [AE][X] == [BE]
         */
        public MatrixStore<Double> getBE() {
            return myBE;
        }

        /**
         * [AI][X] &lt;= [BI]
         */
        public MatrixStore<Double> getBI() {
            return myBI;
        }

        public boolean hasEqualityConstraints() {
            return (myAE != null) && (myAE.countRows() > 0);
        }

        public boolean hasInequalityConstraints() {
            return (myAI != null) && (myAI.countRows() > 0);
        }

        public boolean hasObjective() {
            return myObjective != null;
        }

        @SuppressWarnings("unchecked")
        public B inequalities(final Access2D<Double> mtrxAI, final MatrixStore<Double> mtrxBI) {

            ProgrammingError.throwIfNull(mtrxAI, mtrxBI);
            ProgrammingError.throwIfNotEqualRowDimensions(mtrxAI, mtrxBI);

            if (mtrxAI instanceof RowsSupplier) {

                myAI = (RowsSupplier<Double>) mtrxAI;

            } else {

                myAI = PrimitiveDenseStore.FACTORY.makeRowsSupplier((int) mtrxAI.countColumns());
                myAI.addRows((int) mtrxAI.countRows());

                if (mtrxAI instanceof SparseStore) {

                    ((SparseStore<Double>) mtrxAI).nonzeros().forEach(nz -> myAI.getRow((int) nz.row()).set((int) nz.column(), nz.doubleValue()));

                } else {

                    double value;
                    for (int i = 0; i < mtrxAI.countRows(); i++) {
                        final SparseArray<Double> tmpRow = myAI.getRow(i);
                        for (int j = 0; j < mtrxAI.countColumns(); j++) {
                            value = mtrxAI.doubleValue(i, j);
                            if (!GenericSolver.ACCURACY.isZero(value)) {
                                tmpRow.set(j, value);
                            }
                        }
                    }
                }
            }

            myBI = mtrxBI;

            return (B) this;
        }

        public void reset() {
            myAE = null;
            myAI = null;
            myBE = null;
            myBI = null;
            myObjective = null;
        }

        public void validate() {

            if (this.hasEqualityConstraints()) {

                if (this.getAE() == null) {
                    throw new ProgrammingError("AE cannot be null!");
                } else if (this.getAE().countColumns() != this.countVariables()) {
                    throw new ProgrammingError("AE has the wrong number of columns!");
                } else if (this.getAE().countRows() != this.getBE().countRows()) {
                    throw new ProgrammingError("AE and BE do not have the same number of rows!");
                } else if (this.getBE().countColumns() != 1) {
                    throw new ProgrammingError("BE must have precisely one column!");
                }

            } else {

                myAE = null;
                myBE = null;
            }

            if (this.hasObjective()) {

                if (myObjective.arity() != this.countVariables()) {
                    throw new ProgrammingError("The objective function has the wrong arity!");
                }

            } else {

                myObjective = null;
            }

            if (this.hasInequalityConstraints()) {

                if (this.getAI() == null) {
                    throw new ProgrammingError("AI cannot be null!");
                } else if (this.getAI().countColumns() != this.countVariables()) {
                    throw new ProgrammingError("AI has the wrong number of columns!");
                } else if (this.getAI().countRows() != this.getBI().countRows()) {
                    throw new ProgrammingError("AI and BI do not have the same number of rows!");
                } else if (this.getBI().countColumns() != 1) {
                    throw new ProgrammingError("BI must have precisely one column!");
                }

            } else {

                myAI = null;
                myBI = null;
            }
        }

        protected abstract S doBuild(Optimisation.Options options);

        protected double evaluate(final Access1D<Double> arg) {
            return myObjective.invoke(arg);
        }

        protected void setObjective(final MultiaryFunction.TwiceDifferentiable<Double> objective) {
            myObjective = objective;
        }

    }

    protected static final NumberContext ACCURACY = new NumberContext(12, 14, RoundingMode.HALF_DOWN);

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

    protected final State getState() {
        return myState;
    }

    /**
     * Should be called after a completed iteration. The iterations count is not "1" untill the first
     * iteration is completed.
     */
    protected final int incrementIterationsCount() {
        int iterationsDone = myIterationsCount.incrementAndGet();
        if (this.isLogProgress() && ((iterationsDone % 1000) == 0)) {
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
        if (myState.isFailure()) {
            return false;
        } else if (myState.isFeasible()) {
            return (this.countTime() < options.time_suffice) && (this.countIterations() < options.iterations_suffice);
        } else {
            return (this.countTime() < options.time_abort) && (this.countIterations() < options.iterations_abort);
        }
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
        return (options.logger_appender == null) || (!options.logger_solver.isAssignableFrom(this.getClass()));
    }

    /**
     * Cursory progress logging (at least)
     */
    protected final boolean isLogProgress() {
        return (options.logger_appender != null) && (options.logger_solver.isAssignableFrom(this.getClass()));
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

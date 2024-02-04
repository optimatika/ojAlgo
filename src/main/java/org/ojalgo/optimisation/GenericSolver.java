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

import static org.ojalgo.function.constant.PrimitiveMath.ZERO;

import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.ojalgo.ProgrammingError;
import org.ojalgo.array.SparseArray;
import org.ojalgo.function.multiary.MultiaryFunction;
import org.ojalgo.function.multiary.MultiaryFunction.TwiceDifferentiable;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PhysicalStore.Factory;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.matrix.store.RowsSupplier;
import org.ojalgo.matrix.store.SparseStore;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Access2D.RowView;
import org.ojalgo.type.CalendarDateDuration;
import org.ojalgo.type.CalendarDateUnit;
import org.ojalgo.type.Stopwatch;
import org.ojalgo.type.context.NumberContext;

public abstract class GenericSolver implements Optimisation.Solver {

    public static abstract class Builder<B extends Builder<B, S>, S extends GenericSolver> implements Optimisation.ProblemStructure {

        private static final Factory<Double, Primitive64Store> FACTORY = Primitive64Store.FACTORY;

        private static MatrixStore<Double> add(final RowsSupplier<Double> baseA, final MatrixStore<Double> baseB, final Access2D<?> addA,
                final Access1D<?> addB) {

            ProgrammingError.throwIfNull(addA, addB);
            ProgrammingError.throwIfNotEqualRowDimensions(addA, addB);

            int baseRowDim = baseA.getRowDim();
            int addRowDim = addA.getRowDim();
            int addColDim = addA.getColDim();

            baseA.addRows(addRowDim);

            if (addA instanceof SparseStore) {

                ((SparseStore<?>) addA).nonzeros().forEach(nz -> baseA.getRow(baseRowDim + Math.toIntExact(nz.row())).set(nz.column(), nz.doubleValue()));

            } else {

                double value;
                for (int i = 0; i < addRowDim; i++) {
                    SparseArray<Double> tmpRow = baseA.getRow(baseRowDim + i);
                    for (int j = 0; j < addColDim; j++) {
                        value = addA.doubleValue(i, j);
                        if (value != ZERO) {
                            tmpRow.set(j, value);
                        }
                    }
                }
            }

            Primitive64Store retB = FACTORY.make(baseRowDim + addRowDim, 1);
            retB.fillMatching(baseB);
            retB.regionByOffsets(baseRowDim, 0).fillMatching(addB);

            return retB;
        }

        protected static final void append(final StringBuilder builder, final String label, final MatrixStore<Double> matrix) {
            if (builder != null && label != null && matrix != null) {
                builder.append("\n[");
                builder.append(label);
                builder.append("] = ");
                builder.append(Access2D.toString(matrix));
            }
        }

        /**
         * Assumed constrained to be <= 0.0
         */
        private Map<String, MultiaryFunction.TwiceDifferentiable<Double>> myAdditionalConstraints = null;
        private RowsSupplier<Double> myAE = null;
        private RowsSupplier<Double> myAI = null;
        private MatrixStore<Double> myBE = null;
        private MatrixStore<Double> myBI = null;
        private Primitive64Store myLowerBounds = null;
        private transient int myNumberOfVariables = -1;
        private MultiaryFunction.TwiceDifferentiable<Double> myObjective = null;
        private Primitive64Store myUpperBounds = null;

        protected Builder() {
            super();
        }

        public final S build() {
            this.validate();
            return this.doBuild(new Optimisation.Options());
        }

        public final S build(final Optimisation.Options options) {
            ProgrammingError.throwIfNull(options);
            this.validate();
            return this.doBuild(options);
        }

        @Override
        public int countAdditionalConstraints() {
            return myAdditionalConstraints != null ? myAdditionalConstraints.size() : 0;
        }

        @Override
        public int countEqualityConstraints() {
            return myAE != null ? myAE.getRowDim() : 0;
        }

        @Override
        public int countInequalityConstraints() {
            return myAI != null ? myAI.getRowDim() : 0;
        }

        @Override
        public int countVariables() {
            if (myNumberOfVariables < 0) {
                myNumberOfVariables = this.doCountVariables();
            }
            return myNumberOfVariables;
        }

        public void reset() {

            if (myAdditionalConstraints != null) {
                myAdditionalConstraints.clear();
            }

            myAdditionalConstraints = null;
            myAE = null;
            myAI = null;
            myBE = null;
            myBI = null;
            myLowerBounds = null;
            myObjective = null;
            myUpperBounds = null;

            myNumberOfVariables = -1;
        }

        public final Optimisation.Result solve() {
            return this.build().solve().withSolutionLength(this.countVariables());
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

        protected void append(final StringBuilder builder) {
            Builder.append(builder, "AE", this.getAE());
            Builder.append(builder, "BE", this.getBE());
            Builder.append(builder, "AI", this.getAI());
            Builder.append(builder, "BI", this.getBI());
            Builder.append(builder, "C", this.getC());
        }

        protected abstract S doBuild(Optimisation.Options options);

        protected int doCountVariables() {

            if (myAE != null) {
                return myAE.getColDim();
            }

            if (myAI != null) {
                return myAI.getColDim();
            }

            if (myObjective != null) {
                return myObjective.arity();
            }

            throw new ProgrammingError("Cannot deduce the number of variables!");
        }

        protected B equalities(final Access2D<?> mtrxAE, final Access1D<?> mtrxBE) {
            this.setEqualities(mtrxAE, mtrxBE);
            return (B) this;
        }

        protected B equality(final double rhs, final double... factors) {

            Primitive64Store mBody = FACTORY.make(1, this.countVariables());
            for (int i = 0, limit = Math.min(factors.length, this.countVariables()); i < limit; i++) {
                mBody.set(i, factors[i]);
            }

            MatrixStore<Double> mRHS = FACTORY.makeSingle(rhs);

            this.addEqualities(mBody, mRHS);

            return (B) this;
        }

        /**
         * Equality constraints body: [AE][X] == [BE]
         */
        protected MatrixStore<Double> getAE() {
            if (myAE != null) {
                return myAE.get();
            } else {
                return FACTORY.makeZero(0, this.countVariables());
            }
        }

        protected SparseArray<Double> getAE(final int row) {
            return myAE.getRow(row);
        }

        protected RowsSupplier<Double> getAE(final int... rows) {
            return myAE.selectRows(rows);
        }

        /**
         * Inequality constraints body: [AI][X] <= [BI]
         */

        protected MatrixStore<Double> getAI() {
            if (myAI != null) {
                return myAI.get();
            } else {
                return FACTORY.makeZero(0, this.countVariables());
            }
        }

        protected SparseArray<Double> getAI(final int row) {
            return myAI.getRow(row);
        }

        protected RowsSupplier<Double> getAI(final int... rows) {
            return myAI.selectRows(rows);
        }

        /**
         * Equality constraints RHS: [AE][X] == [BE]
         */

        protected MatrixStore<Double> getBE() {
            if (myBE != null) {
                return myBE;
            } else {
                return FACTORY.makeZero(0, 1);
            }
        }

        protected double getBE(final int row) {
            return myBE.doubleValue(row);
        }

        /**
         * Inequality constraints RHS: [AI][X] <= [BI]
         */

        protected MatrixStore<Double> getBI() {
            if (myBI != null) {
                return myBI;
            } else {
                return FACTORY.makeZero(0, 1);
            }
        }

        protected double getBI(final int row) {
            return myBI.doubleValue(row);
        }

        protected MatrixStore<Double> getC() {
            return this.getObjective().getLinearFactors(false);
        }

        protected PhysicalStore.Factory<Double, Primitive64Store> getFactory() {
            return FACTORY;
        }

        protected Primitive64Store getLowerBounds(final double defaultValue) {
            if (myLowerBounds == null) {
                myLowerBounds = FACTORY.make(this.countVariables(), 1);
                myLowerBounds.fillAll(defaultValue);
            }
            return myLowerBounds;
        }

        protected MultiaryFunction.TwiceDifferentiable<Double> getObjective() {
            return myObjective;
        }

        protected <T extends MultiaryFunction.TwiceDifferentiable<Double>> T getObjective(final Class<T> type) {
            return (T) myObjective;
        }

        protected RowView<Double> getRowsAE() {
            return myAE.rows();
        }

        protected RowView<Double> getRowsAI() {
            return myAI.rows();
        }

        protected Primitive64Store getUpperBounds(final double defaultValue) {
            if (myUpperBounds == null) {
                myUpperBounds = FACTORY.make(this.countVariables(), 1);
                myUpperBounds.fillAll(defaultValue);
            }
            return myUpperBounds;
        }

        protected B inequalities(final Access2D<?> mtrxAI, final Access1D<?> mtrxBI) {
            this.setInequalities(mtrxAI, mtrxBI);
            return (B) this;
        }

        protected B inequality(final double rhs, final double... factors) {

            Primitive64Store mBody = FACTORY.make(1, this.countVariables());
            for (int i = 0, limit = Math.min(factors.length, this.countVariables()); i < limit; i++) {
                mBody.set(i, factors[i]);
            }

            MatrixStore<Double> mRHS = FACTORY.makeSingle(rhs);

            this.addInequalities(mBody, mRHS);

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

        protected void setObjective(final MultiaryFunction.TwiceDifferentiable<Double> objective) {

            ProgrammingError.throwIfNull(objective);

            myObjective = objective;
        }

        void addConstraint(final String key, final TwiceDifferentiable<Double> value) {
            if (myAdditionalConstraints == null) {
                myAdditionalConstraints = new HashMap<>();
            }
            myAdditionalConstraints.put(key, value);
        }

        void addEqualities(final MatrixStore<?> mtrxAE, final MatrixStore<?> mtrxBE) {

            ProgrammingError.throwIfNull(mtrxAE, mtrxBE);
            ProgrammingError.throwIfNotEqualRowDimensions(mtrxAE, mtrxBE);

            if (myAE == null || myBE == null) {
                myAE = FACTORY.makeRowsSupplier(mtrxAE.getColDim());
                myBE = FACTORY.makeZero(0, 1);
            }

            myBE = Builder.add(myAE, myBE, mtrxAE, mtrxBE);
        }

        void addInequalities(final MatrixStore<?> mtrxAI, final MatrixStore<?> mtrxBI) {

            ProgrammingError.throwIfNull(mtrxAI, mtrxBI);
            ProgrammingError.throwIfNotEqualRowDimensions(mtrxAI, mtrxBI);

            if (myAI == null || myBI == null) {
                myAI = FACTORY.makeRowsSupplier(mtrxAI.getColDim());
                myBI = FACTORY.makeZero(0, 0);
            }

            myBI = Builder.add(myAI, myBI, mtrxAI, mtrxBI);
        }

        void newEqualities(final int nbEqualities, final int nbVariables) {

            MatrixStore<Double> mtrxAE = FACTORY.make(nbEqualities, nbVariables);
            MatrixStore<Double> mtrxBE = FACTORY.make(nbEqualities, 1);

            this.setEqualities(mtrxAE, mtrxBE);
        }

        void newInequalities(final int nbInequalities, final int nbVariables) {

            RowsSupplier<Double> mtrxAI = FACTORY.makeRowsSupplier(nbVariables);
            mtrxAI.addRows(nbInequalities);
            MatrixStore<Double> mtrxBI = FACTORY.make(nbInequalities, 1);

            this.setInequalities(mtrxAI, mtrxBI);
        }

        void setBounds(final Access1D<Double> lower, final Access1D<Double> upper) {

            ProgrammingError.throwIfNull(lower, upper);

            if (lower instanceof Primitive64Store) {
                myLowerBounds = (Primitive64Store) lower;
            } else {
                myLowerBounds = FACTORY.columns(lower);
            }

            if (upper instanceof Primitive64Store) {
                myUpperBounds = (Primitive64Store) upper;
            } else {
                myUpperBounds = FACTORY.columns(upper);
            }
        }

        void setEqualities(final Access2D<?> mtrxAE, final Access1D<?> mtrxBE) {

            ProgrammingError.throwIfNull(mtrxAE, mtrxBE);
            ProgrammingError.throwIfNotEqualRowDimensions(mtrxAE, mtrxBE);

            myAE = FACTORY.makeRowsSupplier(mtrxAE.getColDim());
            myBE = FACTORY.makeZero(0, 0);

            myBE = Builder.add(myAE, myBE, mtrxAE, mtrxBE);
        }

        void setInequalities(final Access2D<?> mtrxAI, final Access1D<?> mtrxBI) {

            ProgrammingError.throwIfNull(mtrxAI, mtrxBI);
            ProgrammingError.throwIfNotEqualRowDimensions(mtrxAI, mtrxBI);

            myAI = FACTORY.makeRowsSupplier(mtrxAI.getColDim());
            myBI = FACTORY.makeZero(0, 0);

            myBI = Builder.add(myAI, myBI, mtrxAI, mtrxBI);
        }

        void validate() {

            ProgrammingError.throwIfNull(myObjective);

            if (myAE != null || myBE != null) {
                ProgrammingError.throwIfNull(myAE, myBE);
                ProgrammingError.throwIfNotEqualRowDimensions(myAE, myBE);
            }

            if (myAI != null || myBI != null) {
                ProgrammingError.throwIfNull(myAI, myBI);
                ProgrammingError.throwIfNotEqualRowDimensions(myAI, myBI);
            }

            // Check number of variables/columns

            int nbVariables = this.countVariables();

            if (myAE != null && myAE.getColDim() != nbVariables) {
                throw new ProgrammingError("AE has the wrong number of columns!");
            }

            if (myBE != null && myBE.getColDim() != 1) {
                throw new ProgrammingError("BE must have precisely one column!");
            }

            if (myAI != null && myAI.getColDim() != nbVariables) {
                throw new ProgrammingError("AI has the wrong number of columns!");
            }

            if (myBI != null && myBI.getColDim() != 1) {
                throw new ProgrammingError("BI must have precisely one column!");
            }

            if (myObjective != null && myObjective.arity() != nbVariables) {
                throw new ProgrammingError("The objective function has the wrong arity!");
            }
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
    private ExpressionsBasedModel.Validator myValidator = null;

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

    /**
     * Optionally set a validator. If set, solvers may call {@link #validate(Access1D)} or
     * {@link #validate(ExpressionsBasedModel)} at suitable points in the code to validate its actions. This
     * is a solver debugging tool - not to be used in production code.
     */
    protected final void setValidator(final ExpressionsBasedModel.Validator validator) {
        myValidator = validator;
    }

    /**
     * @see #setValidator(org.ojalgo.optimisation.ExpressionsBasedModel.Validator)
     * @see org.ojalgo.optimisation.ExpressionsBasedModel.Validator#validate(Access1D, NumberContext,
     *      BasicLogger)
     */
    protected final boolean validate(final Access1D<?> solverSolution) {
        if (myValidator != null && solverSolution != null) {
            return myValidator.validate(solverSolution, options.feasibility, options.logger_appender);
        } else {
            return true;
        }
    }

    /**
     * @see #setValidator(org.ojalgo.optimisation.ExpressionsBasedModel.Validator)
     * @see org.ojalgo.optimisation.ExpressionsBasedModel.Validator#validate(ExpressionsBasedModel,
     *      NumberContext, BasicLogger)
     */
    protected final boolean validate(final ExpressionsBasedModel modifiedModel) {
        if (myValidator != null && modifiedModel != null) {
            return myValidator.validate(modifiedModel, options.feasibility, options.logger_appender);
        } else {
            return true;
        }
    }

}

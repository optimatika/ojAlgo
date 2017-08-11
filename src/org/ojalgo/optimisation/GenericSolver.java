/*
 * Copyright 1997-2017 Optimatika (www.optimatika.se)
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

import static org.ojalgo.constant.PrimitiveMath.*;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

import org.ojalgo.ProgrammingError;
import org.ojalgo.access.Access1D;
import org.ojalgo.access.Access2D;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.function.aggregator.PrimitiveAggregator;
import org.ojalgo.matrix.PrimitiveMatrix;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PhysicalStore.Factory;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.netio.BasicLogger;

public abstract class GenericSolver implements Optimisation.Solver, Serializable {

    public static abstract class AbstractBuilder<B extends AbstractBuilder<?, ?>, S extends GenericSolver> implements Cloneable {

        static final Factory<Double, PrimitiveDenseStore> FACTORY = PrimitiveDenseStore.FACTORY;

        private MatrixStore<Double> myAE = null;
        private MatrixStore.LogicalBuilder<Double> myAEbuilder = null;
        private MatrixStore<Double> myAI = null;
        private MatrixStore.LogicalBuilder<Double> myAIbuilder = null;
        private MatrixStore<Double> myBE = null;
        private MatrixStore.LogicalBuilder<Double> myBEbuilder = null;
        private MatrixStore<Double> myBI = null;
        private MatrixStore.LogicalBuilder<Double> myBIbuilder = null;
        private MatrixStore<Double> myC = null;
        private MatrixStore.LogicalBuilder<Double> myCbuilder = null;
        private MatrixStore<Double> myQ = null;
        private MatrixStore.LogicalBuilder<Double> myQbuilder = null;
        private PrimitiveDenseStore myX = null;

        protected AbstractBuilder() {
            super();
        }

        protected AbstractBuilder(final GenericSolver.AbstractBuilder<?, ?> matrices) {

            super();

            if (matrices.hasEqualityConstraints()) {
                this.equalities(matrices.getAE(), matrices.getBE());
            }

            if (matrices.hasObjective()) {
                if (matrices.getQ() != null) {
                    this.objective(matrices.getQ(), matrices.getC());
                } else {
                    this.objective(matrices.getC());
                }
            }

            if (matrices.hasInequalityConstraints()) {
                this.inequalities(matrices.getAI(), matrices.getBI());
            }
        }

        protected AbstractBuilder(final MatrixStore<Double> C) {

            super();

            this.objective(C);
        }

        protected AbstractBuilder(final MatrixStore<Double> Q, final MatrixStore<Double> C) {

            super();

            this.objective(Q, C);
        }

        protected AbstractBuilder(final MatrixStore<Double>[] matrices) {

            super();

            if ((matrices.length >= 2) && (matrices[0] != null) && (matrices[1] != null)) {
                this.equalities(matrices[0], matrices[1]);
            }

            if (matrices.length >= 4) {
                if (matrices[2] != null) {
                    this.objective(matrices[2], matrices[3]);
                } else if (matrices[3] != null) {
                    this.objective(matrices[3]);
                }
            }

            if ((matrices.length >= 6) && (matrices[4] != null) && (matrices[5] != null)) {
                this.inequalities(matrices[4], matrices[5]);
            }
        }

        /**
         * Will rescale problem parameters to minimise rounding and representation errors. Warning! This will
         * rescale the objective function and therefore also the optimal value (but not the solution).
         */
        @SuppressWarnings("unchecked")
        public B balance() {

            if (this.hasEqualityConstraints()) {
                this.balanceEqualityConstraints();
            }

            if (this.hasInequalityConstraints()) {
                this.balanceInequalityConstraints();
            }

            if (this.hasObjective()) {
                this.balanceObjective();
            }

            return (B) this;
        }

        public final S build() {
            return this.build(null);
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
            } else if (this.getQ() != null) {
                retVal = (int) this.getQ().countRows();
            } else if (this.getC() != null) {
                retVal = (int) this.getC().countRows();
            } else {
                throw new ProgrammingError("Cannot deduce the number of variables!");
            }

            return retVal;
        }

        /**
         * [AE][X] == [BE]
         */
        public MatrixStore<Double> getAE() {
            if (myAEbuilder != null) {
                if (myAE == null) {
                    myAE = myAEbuilder.get().copy();
                }
                return myAE;
            } else {
                return null;
            }
        }

        public MatrixStore<Double> getAEX() {

            final MatrixStore<Double> tmpAE = this.getAE();
            final PhysicalStore<Double> tmpX = this.getX();

            if ((tmpAE != null) && (tmpX != null)) {
                return tmpAE.multiply(tmpX);
            } else {
                return null;
            }
        }

        /**
         * [AI][X] &lt;= [BI]
         */
        public MatrixStore<Double> getAI() {
            if (myAIbuilder != null) {
                if (myAI == null) {
                    myAI = myAIbuilder.get().copy();
                }
                return myAI;
            } else {
                return null;
            }
        }

        public MatrixStore<Double> getAIX(final int[] selector) {

            final MatrixStore<Double> tmpAI = this.getAI();
            final PhysicalStore<Double> tmpX = this.getX();

            if ((tmpAI != null) && (tmpX != null)) {
                return tmpAI.logical().row(selector).get().multiply(tmpX);
            } else {
                return null;
            }
        }

        /**
         * [AE][X] == [BE]
         */
        public MatrixStore<Double> getBE() {
            if (myBEbuilder != null) {
                if (myBE == null) {
                    myBE = myBEbuilder.get().copy();
                }
                return myBE;
            } else {
                return null;
            }
        }

        /**
         * [AI][X] &lt;= [BI]
         */
        public MatrixStore<Double> getBI() {
            if (myBIbuilder != null) {
                if (myBI == null) {
                    myBI = myBIbuilder.get().copy();
                }
                return myBI;
            } else {
                return null;
            }
        }

        public MatrixStore<Double> getBI(final int[] selector) {
            return this.getBI().logical().row(selector).get();
        }

        /**
         * Linear objective: [C]
         */
        public MatrixStore<Double> getC() {
            if (myCbuilder != null) {
                if (myC == null) {
                    myC = myCbuilder.get().copy();
                }
                return myC;
            } else {
                return null;
            }
        }

        /**
         * Quadratic objective: [Q]
         */
        public MatrixStore<Double> getQ() {
            if (myQbuilder != null) {
                if (myQ == null) {
                    myQ = myQbuilder.get().copy();
                }
                return myQ;
            } else {
                return null;
            }
        }

        /**
         * Slack for Equalities: [SE] = [BE] - [AE][X]
         */
        public PhysicalStore<Double> getSE() {

            PhysicalStore<Double> retVal = null;

            if ((this.getAE() != null) && (this.getBE() != null) && (this.getX() != null)) {

                retVal = this.getBE().copy();

                retVal.modifyMatching(PrimitiveFunction.SUBTRACT, this.getAEX());
            }

            return retVal;
        }

        /**
         * Selected Slack for Inequalities
         */
        public MatrixStore<Double> getSI(final int... selector) {
            final PhysicalStore<Double> tmpSI = this.getSI();
            if (tmpSI != null) {
                return tmpSI.logical().row(selector).get();
            } else {
                return null;
            }
        }

        /**
         * Solution / Variables: [X]
         */
        public PhysicalStore<Double> getX() {
            if (myX == null) {
                myX = PrimitiveDenseStore.FACTORY.makeZero(this.countVariables(), 1);
            }
            return myX;
        }

        public boolean hasEqualityConstraints() {
            return (this.getAE() != null) && (this.getAE().countRows() > 0);
        }

        public boolean hasInequalityConstraints() {
            return (this.getAI() != null) && (this.getAI().countRows() > 0);
        }

        public boolean hasObjective() {
            return (this.getQ() != null) || (this.getC() != null);
        }

        public boolean isX() {
            return myX != null;
        }

        public void reset() {
            myAE = null;
            myAI = null;
            myBE = null;
            myBI = null;
            myC = null;
            myQ = null;
            myX = null;
        }

        public void resetX() {
            if (myX != null) {
                myX.fillAll(ZERO);
            }
        }

        public void setX(final int index, final double value) {
            this.getX().set(index, 0, value);
        }

        @Override
        public String toString() {

            final StringBuilder retVal = new StringBuilder("<" + this.getClass().getSimpleName() + ">");

            retVal.append("\n[AE] = " + (this.getAE() != null ? PrimitiveMatrix.FACTORY.copy(this.getAE()) : "?"));

            retVal.append("\n[BE] = " + (this.getBE() != null ? PrimitiveMatrix.FACTORY.copy(this.getBE()) : "?"));

            retVal.append("\n[Q] = " + (this.getQ() != null ? PrimitiveMatrix.FACTORY.copy(this.getQ()) : "?"));

            retVal.append("\n[C] = " + (this.getC() != null ? PrimitiveMatrix.FACTORY.copy(this.getC()) : "?"));

            retVal.append("\n[AI] = " + (this.getAI() != null ? PrimitiveMatrix.FACTORY.copy(this.getAI()) : "?"));

            retVal.append("\n[BI] = " + (this.getBI() != null ? PrimitiveMatrix.FACTORY.copy(this.getBI()) : "?"));

            retVal.append("\n[X] = " + (this.getX() != null ? PrimitiveMatrix.FACTORY.copy(this.getX()) : "?"));

            retVal.append("\n[SE] = " + (this.getSE() != null ? PrimitiveMatrix.FACTORY.copy(this.getSE()) : "?"));

            retVal.append("\n[SI] = " + (this.getSI() != null ? PrimitiveMatrix.FACTORY.copy(this.getSI()) : "?"));

            retVal.append("\n</" + this.getClass().getSimpleName() + ">");

            return retVal.toString();
        }

        private void balanceEqualityConstraints() {

            final PhysicalStore<Double> tmpBody = this.cast(this.getAE());
            final PhysicalStore<Double> tmpRHS = this.cast(this.getBE());

            this.balanceRows(tmpBody, tmpRHS, true);

            myAE = tmpBody;
            myBE = tmpRHS;

            this.validate();
        }

        private void balanceInequalityConstraints() {

            final PhysicalStore<Double> tmpBody = this.cast(this.getAI());
            final PhysicalStore<Double> tmpRHS = this.cast(this.getBI());

            this.balanceRows(tmpBody, tmpRHS, false);

            myAI = tmpBody;
            myBI = tmpRHS;

            this.validate();
        }

        private double balanceMatrices(final PhysicalStore<Double>[] someMatrices) {

            final AggregatorFunction<Double> tmpLargestAggr = PrimitiveAggregator.getSet().largest();
            final AggregatorFunction<Double> tmpSmallestAggr = PrimitiveAggregator.getSet().smallest();

            tmpLargestAggr.invoke(ONE);
            tmpSmallestAggr.invoke(ONE);

            for (final PhysicalStore<Double> tmpMatrix : someMatrices) {
                if (tmpMatrix != null) {
                    tmpMatrix.visitAll(tmpLargestAggr);
                    tmpMatrix.visitAll(tmpSmallestAggr);
                }
            }

            final double tmpExponent = this.getAdjustmentExponent(tmpLargestAggr.doubleValue(), tmpSmallestAggr.doubleValue());
            final double tmpFactor = PrimitiveFunction.POW.invoke(TEN, tmpExponent);

            final UnaryFunction<Double> tmpModifier = PrimitiveFunction.MULTIPLY.second(tmpFactor);

            for (final PhysicalStore<Double> tmpMatrix : someMatrices) {
                if (tmpMatrix != null) {
                    tmpMatrix.modifyAll(tmpModifier);
                }
            }

            return tmpFactor;
        }

        @SuppressWarnings("unchecked")
        private void balanceObjective() {

            final PhysicalStore<Double>[] tmpMatrices = (PhysicalStore<Double>[]) new PhysicalStore<?>[2];

            if (this.getQ() != null) {
                tmpMatrices[0] = this.cast(this.getQ());
            }
            if (this.getC() != null) {
                tmpMatrices[1] = this.cast(this.getC());
            }

            this.balanceMatrices(tmpMatrices);
            myQ = tmpMatrices[0];
            myC = tmpMatrices[1];

            this.validate();

        }

        private void balanceRows(final PhysicalStore<Double> tmpBody, final PhysicalStore<Double> tmpRHS, final boolean assertPositiveRHS) {

            final AggregatorFunction<Double> tmpLargestAggr = PrimitiveAggregator.getSet().largest();
            final AggregatorFunction<Double> tmpSmallestAggr = PrimitiveAggregator.getSet().smallest();

            double tmpExponent;
            double tmpFactor;

            UnaryFunction<Double> tmpModifier;

            for (int i = 0; i < tmpBody.countRows(); i++) {

                tmpLargestAggr.reset();
                tmpSmallestAggr.reset();

                tmpLargestAggr.invoke(ONE);
                tmpSmallestAggr.invoke(ONE);

                tmpBody.visitRow(i, 0, tmpLargestAggr);
                tmpBody.visitRow(i, 0, tmpSmallestAggr);

                tmpRHS.visitRow(i, 0, tmpLargestAggr);
                tmpRHS.visitRow(i, 0, tmpSmallestAggr);

                tmpExponent = this.getAdjustmentExponent(tmpLargestAggr.doubleValue(), tmpSmallestAggr.doubleValue());
                tmpFactor = PrimitiveFunction.POW.invoke(TEN, tmpExponent);
                if (assertPositiveRHS && (PrimitiveFunction.SIGNUM.invoke(tmpRHS.doubleValue(i, 0)) < ZERO)) {
                    tmpFactor = -tmpFactor;
                }

                tmpModifier = PrimitiveFunction.MULTIPLY.second(tmpFactor);

                tmpBody.modifyRow(i, 0, tmpModifier);
                tmpRHS.modifyRow(i, 0, tmpModifier);
            }
        }

        protected abstract S build(Optimisation.Options options);

        @Override
        @SuppressWarnings("unchecked")
        protected Object clone() throws CloneNotSupportedException {

            final GenericSolver.AbstractBuilder<B, S> retVal = (GenericSolver.AbstractBuilder<B, S>) super.clone();

            if (myX != null) {
                retVal.getX().fillMatching(myX);
            }

            return retVal;
        }

        @SuppressWarnings("unchecked")
        protected GenericSolver.AbstractBuilder<B, S> copy() {
            try {
                return (GenericSolver.AbstractBuilder<B, S>) this.clone();
            } catch (final CloneNotSupportedException anException) {
                return null;
            }
        }

        @SuppressWarnings("unchecked")
        protected B equalities(final MatrixStore<Double> AE, final MatrixStore<Double> BE) {

            if ((AE == null) || (BE == null) || (AE.countRows() != BE.countRows())) {
                throw new IllegalArgumentException();
            }

            if (myAEbuilder != null) {
                myAEbuilder.below(AE);
                this.reset();
            } else {
                myAE = AE;
                myAEbuilder = myAE.logical();
            }

            if (myBEbuilder != null) {
                myBEbuilder.below(BE);
                this.reset();
            } else {
                myBE = BE;
                myBEbuilder = BE.logical();
            }

            return (B) this;
        }

        protected int getAdjustmentExponent(final double largest, final double smallest) {
            return ModelEntity.getAdjustmentExponent(largest, smallest);
        }

        protected MatrixStore<Double> getAIX() {

            final MatrixStore<Double> tmpAI = this.getAI();
            final PhysicalStore<Double> tmpX = this.getX();

            if ((tmpAI != null) && (tmpX != null)) {
                return tmpAI.multiply(tmpX);
            } else {
                return null;
            }
        }

        /**
         * Slack for Inequalities: [SI] = [BI] - [AI][X]
         */
        protected PhysicalStore<Double> getSI() {

            PhysicalStore<Double> retVal = null;

            if ((this.getAI() != null) && (this.getBI() != null) && (this.getX() != null)) {

                retVal = this.getBI().copy();

                retVal.modifyMatching(PrimitiveFunction.SUBTRACT, this.getAIX());
            }

            return retVal;
        }

        @SuppressWarnings("unchecked")
        protected B inequalities(final MatrixStore<Double> AI, final MatrixStore<Double> BI) {

            if ((AI == null) || (BI == null) || (AI.countRows() != BI.countRows())) {
                throw new IllegalArgumentException();
            }

            if (myAIbuilder != null) {
                myAIbuilder.below(AI);
                this.reset();
            } else {
                myAI = AI;
                myAIbuilder = myAI.logical();
            }

            if (myBIbuilder != null) {
                myBIbuilder.below(BI);
                this.reset();
            } else {
                myBI = BI;
                myBIbuilder = BI.logical();
            }

            return (B) this;
        }

        @SuppressWarnings("unchecked")
        protected B objective(final MatrixStore<Double> C) {

            if (C == null) {
                throw new IllegalArgumentException();
            }

            if (myCbuilder != null) {
                myCbuilder.below(C);
                this.reset();
            } else {
                myC = C;
                myCbuilder = myC.logical();
            }

            return (B) this;
        }

        @SuppressWarnings("unchecked")
        protected B objective(final MatrixStore<Double> Q, final MatrixStore<Double> C) {

            if (Q == null) {
                throw new IllegalArgumentException();
            }

            if (myQbuilder != null) {
                myQbuilder.below(Q);
                this.reset();
            } else {
                myQ = Q;
                myQbuilder = myQ.logical();
            }

            final MatrixStore<Double> tmpC = C != null ? C : MatrixStore.PRIMITIVE.makeZero((int) Q.countRows(), 1).get();
            if (myCbuilder != null) {
                myCbuilder.below(tmpC);
                this.reset();
            } else {
                myC = tmpC;
                myCbuilder = myC.logical();
            }

            return (B) this;
        }

        protected void validate() {

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

                if ((this.getQ() != null) && ((this.getQ().countRows() != this.countVariables()) || (this.getQ().countColumns() != this.countVariables()))) {
                    throw new ProgrammingError("Q has the wrong number of rows and/or columns!");
                }

                if (((this.getC() != null) && (this.getC().countRows() != this.countVariables())) || (this.getC().countColumns() != 1)) {
                    throw new ProgrammingError("C has the wrong number of rows and/or columns!");
                }

            } else {

                myQ = null;
                myC = null;
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

        PhysicalStore<Double> cast(final Access2D<Double> matrix) {
            if (matrix instanceof PhysicalStore<?>) {
                return (PhysicalStore<Double>) matrix;
            } else {
                return FACTORY.copy(matrix);
            }

        }

    }

    public final Optimisation.Options options;

    private final AtomicInteger myIterationsCount = new AtomicInteger(0);
    private long myResetTime;
    private State myState = State.UNEXPLORED;

    @SuppressWarnings("unused")
    private GenericSolver() {
        this(null);
    }

    /**
     */
    protected GenericSolver(final Optimisation.Options solverOptions) {

        super();

        if (solverOptions != null) {
            options = solverOptions;
        } else {
            options = new Optimisation.Options();
        }

    }

    protected Optimisation.Result buildResult() {

        final MatrixStore<Double> tmpSolution = this.extractSolution();
        final double tmpValue = this.evaluateFunction(tmpSolution);
        final Optimisation.State tmpState = this.getState();

        return new Optimisation.Result(tmpState, tmpValue, tmpSolution);
    }

    protected final int countIterations() {
        return myIterationsCount.get();
    }

    protected final long countTime() {
        return System.currentTimeMillis() - myResetTime;
    }

    protected final void debug(final String descripttion, final Access2D<?> matrix) {
        if (options.debug_appender != null) {
            options.debug_appender.printmtrx(descripttion, matrix, options.print);
        }
    }

    protected final void debug(final String messagePattern, final Object... arguments) {
        if (options.debug_appender != null) {
            options.debug_appender.println(messagePattern, arguments);
        }
    }

    protected final void error(final String messagePattern, final Object... arguments) {
        BasicLogger.error(messagePattern, arguments);
    }

    protected abstract double evaluateFunction(final Access1D<?> solution);

    /**
     * Should be able to feed this to {@link #evaluateFunction(Access1D)}.
     */
    protected abstract MatrixStore<Double> extractSolution();

    protected final State getState() {
        return myState;
    }

    /**
     * Should be called after a completed iteration. The iterations count is not "1" untill the first
     * iteration is completed.
     */
    protected final int incrementIterationsCount() {
        return myIterationsCount.incrementAndGet();
    }

    protected abstract boolean initialise(Result kickStarter);

    protected final boolean isDebug() {
        return (options.debug_appender != null) && (options.debug_solver.isAssignableFrom(this.getClass()));
    }

    /**
     * Should be called at the start of an iteration (before it actually starts) to check if you should abort
     * instead. Will return false if either the iterations count or the execution time has reached their
     * respective limits.
     */
    protected final boolean isIterationAllowed() {

        final int tmpIterations = this.countIterations();
        final long tmpTime = this.countTime();

        final boolean tmpIterationOk = tmpIterations < options.iterations_abort;
        final boolean tmpTimeOk = tmpTime < options.time_abort;

        //        if (this.isDebug()) {
        //            this.logDebug("Iterations OK? {} {} < {}", tmpIterationOk, tmpIterations, options.iterations_abort);
        //            this.logDebug("Time OK? {} {} < {}", tmpTimeOk, tmpTime, options.time_abort);
        //        }

        return tmpTimeOk && tmpIterationOk;
    }

    protected abstract boolean needsAnotherIteration();

    protected final void resetIterationsCount() {
        myIterationsCount.set(0);
        myResetTime = System.currentTimeMillis();
    }

    protected final void setState(final State aState) {
        myState = aState;
    }

    /**
     * Should validate the solver data/input/structue. Even "expensive" validation can be performed as the
     * method should only be called if {@linkplain Optimisation.Options#validate} is set to true. In addition
     * to returning true or false the implementation should set the state to either
     * {@linkplain Optimisation.State#VALID} or {@linkplain Optimisation.State#INVALID} (or possibly
     * {@linkplain Optimisation.State#FAILED}). Typically the method should be called at the very beginning of
     * the solve-method.
     *
     * @return Is the solver instance valid?
     */
    protected abstract boolean validate();

}

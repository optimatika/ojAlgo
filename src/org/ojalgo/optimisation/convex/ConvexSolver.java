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
package org.ojalgo.optimisation.convex;

import static org.ojalgo.constant.PrimitiveMath.*;
import static org.ojalgo.function.PrimitiveFunction.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.ojalgo.ProgrammingError;
import org.ojalgo.access.Access1D;
import org.ojalgo.access.Access2D;
import org.ojalgo.access.IntIndex;
import org.ojalgo.access.IntRowColumn;
import org.ojalgo.array.Array1D;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.function.aggregator.PrimitiveAggregator;
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.matrix.PrimitiveMatrix;
import org.ojalgo.matrix.decomposition.Cholesky;
import org.ojalgo.matrix.decomposition.Eigenvalue;
import org.ojalgo.matrix.decomposition.LU;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PhysicalStore.Factory;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.matrix.store.SparseStore;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.GenericSolver;
import org.ojalgo.optimisation.ModelEntity;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.scalar.ComplexNumber;

/**
 * ConvexSolver solves optimisation problems of the form:
 * <p>
 * min 1/2 [X]<sup>T</sup>[Q][X] - [C]<sup>T</sup>[X]<br>
 * when [AE][X] == [BE]<br>
 * and [AI][X] &lt;= [BI]
 * </p>
 * <p>
 * The matrix [Q] is assumed to be symmetric (it must be made that way) and positive (semi)definite:
 * </p>
 * <ul>
 * <li>If [Q] is positive semidefinite, then the objective function is convex: In this case the quadratic
 * program has a global minimizer if there exists some feasible vector [X] (satisfying the constraints) and if
 * the objective function is bounded below on the feasible region.</li>
 * <li>If [Q] is positive definite and the problem has a feasible solution, then the global minimizer is
 * unique.</li>
 * </ul>
 * <p>
 * The general recommendation is to construct optimisation problems using {@linkplain ExpressionsBasedModel}
 * and not worry about solver details. If you do want to instantiate a convex solver directly use the
 * {@linkplain ConvexSolver.Builder} class. It will return an appropriate subclass for you.
 * </p>
 * <p>
 * When the KKT matrix is nonsingular, there is a unique optimal primal-dual pair (x,l). If the KKT matrix is
 * singular, but the KKT system is still solvable, any solution yields an optimal pair (x,l). If the KKT
 * system is not solvable, the quadratic optimization problem is unbounded below or infeasible.
 * </p>
 *
 * @author apete
 */
public abstract class ConvexSolver extends GenericSolver {

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

        protected AbstractBuilder(final ConvexSolver.AbstractBuilder<?, ?> matrices) {

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

        public abstract S build(Optimisation.Options options);

        @SuppressWarnings("unchecked")
        public ConvexSolver.AbstractBuilder<B, S> copy() {
            try {
                return (ConvexSolver.AbstractBuilder<B, S>) this.clone();
            } catch (final CloneNotSupportedException anException) {
                return null;
            }
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

        public MatrixStore<Double> getAIX() {

            final MatrixStore<Double> tmpAI = this.getAI();
            final PhysicalStore<Double> tmpX = this.getX();

            if ((tmpAI != null) && (tmpX != null)) {
                return tmpAI.multiply(tmpX);
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
         * Slack for Inequalities: [SI] = [BI] - [AI][X]
         */
        public PhysicalStore<Double> getSI() {

            PhysicalStore<Double> retVal = null;

            if ((this.getAI() != null) && (this.getBI() != null) && (this.getX() != null)) {

                retVal = this.getBI().copy();

                retVal.modifyMatching(PrimitiveFunction.SUBTRACT, this.getAIX());
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

            final double tmpExponent = ModelEntity.getAdjustmentExponent(tmpLargestAggr.doubleValue(), tmpSmallestAggr.doubleValue());
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

                tmpExponent = ModelEntity.getAdjustmentExponent(tmpLargestAggr.doubleValue(), tmpSmallestAggr.doubleValue());
                tmpFactor = PrimitiveFunction.POW.invoke(TEN, tmpExponent);
                if (assertPositiveRHS && (PrimitiveFunction.SIGNUM.invoke(tmpRHS.doubleValue(i, 0)) < ZERO)) {
                    tmpFactor = -tmpFactor;
                }

                tmpModifier = PrimitiveFunction.MULTIPLY.second(tmpFactor);

                tmpBody.modifyRow(i, 0, tmpModifier);
                tmpRHS.modifyRow(i, 0, tmpModifier);
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        protected Object clone() throws CloneNotSupportedException {

            final ConvexSolver.AbstractBuilder<B, S> retVal = (ConvexSolver.AbstractBuilder<B, S>) super.clone();

            if (myX != null) {
                retVal.getX().fillMatching(myX);
            }

            return retVal;
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

        void reset() {
            myAE = null;
            myAI = null;
            myBE = null;
            myBI = null;
            myC = null;
            myQ = null;
            myX = null;
        }

    }
    public static final class Builder extends AbstractBuilder<ConvexSolver.Builder, ConvexSolver> {

        public Builder(final MatrixStore<Double> Q, final MatrixStore<Double> C) {
            super(Q, C);
        }

        Builder() {
            super();
        }

        Builder(final ConvexSolver.Builder matrices) {
            super(matrices);
        }

        Builder(final MatrixStore<Double> C) {
            super(C);
        }

        Builder(final MatrixStore<Double>[] aMtrxArr) {
            super(aMtrxArr);
        }

        @Override
        public ConvexSolver build(final Optimisation.Options options) {

            this.validate();

            if (this.hasInequalityConstraints()) {
                if (this.hasEqualityConstraints()) {
                    return new IterativeMixedASS(this, options);
                } else {
                    return new IterativePureASS(this, options);
                }
            } else if (this.hasEqualityConstraints()) {
                return new QPESolver(this, options);
            } else {
                return new UnconstrainedSolver(this, options);
            }
        }

        @Override
        public ConvexSolver.Builder equalities(final MatrixStore<Double> AE, final MatrixStore<Double> BE) {
            return super.equalities(AE, BE);
        }

        @Override
        public ConvexSolver.Builder inequalities(final MatrixStore<Double> AI, final MatrixStore<Double> BI) {
            return super.inequalities(AI, BI);
        }

        @Override
        public Builder objective(final MatrixStore<Double> Q, final MatrixStore<Double> C) {
            return super.objective(Q, C);
        }

    }

    public static final class ModelIntegration extends ExpressionsBasedModel.Integration<ConvexSolver> {

        public ConvexSolver build(final ExpressionsBasedModel model) {

            final ConvexSolver.Builder tmpBuilder = ConvexSolver.getBuilder();

            ConvexSolver.copy(model, tmpBuilder);

            return tmpBuilder.build(model.options);
        }

        public boolean isCapable(final ExpressionsBasedModel model) {
            return !model.isAnyVariableInteger() && model.isAnyExpressionQuadratic();
        }

    }

    static final PhysicalStore.Factory<Double, PrimitiveDenseStore> FACTORY = PrimitiveDenseStore.FACTORY;

    public static void copy(final ExpressionsBasedModel sourceModel, final Builder destinationBuilder) {

        final List<Variable> tmpFreeVariables = sourceModel.getFreeVariables();
        final Set<IntIndex> tmpFixedVariables = sourceModel.getFixedVariables();
        final int tmpFreeVarDim = tmpFreeVariables.size();

        //        final Array1D<Double> tmpCurrentSolution = Array1D.PRIMITIVE.makeZero(tmpFreeVarDim);
        //        for (int i = 0; i < tmpFreeVariables.size(); i++) {
        //            final BigDecimal tmpValue = tmpFreeVariables.get(i).getValue();
        //            if (tmpValue != null) {
        //                tmpCurrentSolution.set(i, tmpValue.doubleValue());
        //            }
        //        }
        //        final Optimisation.Result tmpKickStarter = new Optimisation.Result(Optimisation.State.UNEXPLORED, Double.NaN, tmpCurrentSolution);

        // AE & BE

        final List<Expression> tmpEqExpr = sourceModel.constraints()
                .filter((final Expression c) -> c.isEqualityConstraint() && !c.isAnyQuadraticFactorNonZero()).collect(Collectors.toList());
        final int tmpEqExprDim = tmpEqExpr.size();

        if (tmpEqExprDim > 0) {

            final SparseStore<Double> tmpAE = SparseStore.PRIMITIVE.make(tmpEqExprDim, tmpFreeVarDim);
            final PhysicalStore<Double> tmpBE = FACTORY.makeZero(tmpEqExprDim, 1);

            for (int i = 0; i < tmpEqExprDim; i++) {

                final Expression tmpExpression = tmpEqExpr.get(i).compensate(tmpFixedVariables);

                for (final IntIndex tmpKey : tmpExpression.getLinearKeySet()) {
                    final int tmpIndex = sourceModel.indexOfFreeVariable(tmpKey.index);
                    if (tmpIndex >= 0) {
                        tmpAE.set(i, tmpIndex, tmpExpression.getAdjustedLinearFactor(tmpKey));
                    }
                }
                tmpBE.set(i, 0, tmpExpression.getAdjustedUpperLimit());
            }

            destinationBuilder.equalities(tmpAE, tmpBE);
        }

        // Q & C

        final Expression tmpObjExpr = sourceModel.objective().compensate(tmpFixedVariables);

        PhysicalStore<Double> tmpQ = null;
        if (tmpObjExpr.isAnyQuadraticFactorNonZero()) {
            tmpQ = FACTORY.makeZero(tmpFreeVarDim, tmpFreeVarDim);

            final BinaryFunction<Double> tmpBaseFunc = sourceModel.isMaximisation() ? SUBTRACT : ADD;
            UnaryFunction<Double> tmpModifier;
            for (final IntRowColumn tmpKey : tmpObjExpr.getQuadraticKeySet()) {
                final int tmpRow = sourceModel.indexOfFreeVariable(tmpKey.row);
                final int tmpColumn = sourceModel.indexOfFreeVariable(tmpKey.column);
                if ((tmpRow >= 0) && (tmpColumn >= 0)) {
                    tmpModifier = tmpBaseFunc.second(tmpObjExpr.getAdjustedQuadraticFactor(tmpKey));
                    tmpQ.modifyOne(tmpRow, tmpColumn, tmpModifier);
                    tmpQ.modifyOne(tmpColumn, tmpRow, tmpModifier);
                }
            }
        }

        PhysicalStore<Double> tmpC = null;
        if (tmpObjExpr.isAnyLinearFactorNonZero()) {
            tmpC = FACTORY.makeZero(tmpFreeVarDim, 1);
            if (sourceModel.isMinimisation()) {
                for (final IntIndex tmpKey : tmpObjExpr.getLinearKeySet()) {
                    final int tmpIndex = sourceModel.indexOfFreeVariable(tmpKey.index);
                    if (tmpIndex >= 0) {
                        tmpC.set(tmpIndex, 0, -tmpObjExpr.getAdjustedLinearFactor(tmpKey));
                    }
                }
            } else {
                for (final IntIndex tmpKey : tmpObjExpr.getLinearKeySet()) {
                    final int tmpIndex = sourceModel.indexOfFreeVariable(tmpKey.index);
                    if (tmpIndex >= 0) {
                        tmpC.set(tmpIndex, 0, tmpObjExpr.getAdjustedLinearFactor(tmpKey));
                    }
                }
            }
        }

        destinationBuilder.objective(tmpQ, tmpC);

        // AI & BI

        final List<Expression> tmpUpExpr = sourceModel.constraints()
                .filter((final Expression c2) -> c2.isUpperConstraint() && !c2.isAnyQuadraticFactorNonZero()).collect(Collectors.toList());
        final int tmpUpExprDim = tmpUpExpr.size();
        final List<Variable> tmpUpVar = sourceModel.bounds().filter((final Variable c4) -> c4.isUpperConstraint()).collect(Collectors.toList());
        final int tmpUpVarDim = tmpUpVar.size();

        final List<Expression> tmpLoExpr = sourceModel.constraints()
                .filter((final Expression c1) -> c1.isLowerConstraint() && !c1.isAnyQuadraticFactorNonZero()).collect(Collectors.toList());
        final int tmpLoExprDim = tmpLoExpr.size();
        final List<Variable> tmpLoVar = sourceModel.bounds().filter((final Variable c3) -> c3.isLowerConstraint()).collect(Collectors.toList());
        final int tmpLoVarDim = tmpLoVar.size();

        if ((tmpUpExprDim + tmpUpVarDim + tmpLoExprDim + tmpLoVarDim) > 0) {

            final SparseStore<Double> tmpAI = SparseStore.PRIMITIVE.make(tmpUpExprDim + tmpUpVarDim + tmpLoExprDim + tmpLoVarDim, tmpFreeVarDim);
            final PhysicalStore<Double> tmpBI = FACTORY.makeZero(tmpUpExprDim + tmpUpVarDim + tmpLoExprDim + tmpLoVarDim, 1);

            if (tmpUpExprDim > 0) {
                for (int i = 0; i < tmpUpExprDim; i++) {
                    final Expression tmpExpression = tmpUpExpr.get(i).compensate(tmpFixedVariables);
                    for (final IntIndex tmpKey : tmpExpression.getLinearKeySet()) {
                        final int tmpIndex = sourceModel.indexOfFreeVariable(tmpKey.index);
                        if (tmpIndex >= 0) {
                            tmpAI.set(i, tmpIndex, tmpExpression.getAdjustedLinearFactor(tmpKey));
                        }
                    }
                    tmpBI.set(i, 0, tmpExpression.getAdjustedUpperLimit());
                }
            }

            if (tmpUpVarDim > 0) {
                for (int i = 0; i < tmpUpVarDim; i++) {
                    final Variable tmpVariable = tmpUpVar.get(i);
                    tmpAI.set(tmpUpExprDim + i, sourceModel.indexOfFreeVariable(tmpVariable), tmpVariable.getAdjustmentFactor());
                    tmpBI.set(tmpUpExprDim + i, 0, tmpVariable.getAdjustedUpperLimit());
                }
            }

            if (tmpLoExprDim > 0) {
                for (int i = 0; i < tmpLoExprDim; i++) {
                    final Expression tmpExpression = tmpLoExpr.get(i).compensate(tmpFixedVariables);
                    for (final IntIndex tmpKey : tmpExpression.getLinearKeySet()) {
                        final int tmpIndex = sourceModel.indexOfFreeVariable(tmpKey.index);
                        if (tmpIndex >= 0) {
                            tmpAI.set(tmpUpExprDim + tmpUpVarDim + i, tmpIndex, -tmpExpression.getAdjustedLinearFactor(tmpKey));
                        }
                    }
                    tmpBI.set(tmpUpExprDim + tmpUpVarDim + i, 0, -tmpExpression.getAdjustedLowerLimit());
                }
            }

            if (tmpLoVarDim > 0) {
                for (int i = 0; i < tmpLoVarDim; i++) {
                    final Variable tmpVariable = tmpLoVar.get(i);
                    tmpAI.set(tmpUpExprDim + tmpUpVarDim + tmpLoExprDim + i, sourceModel.indexOfFreeVariable(tmpVariable), -tmpVariable.getAdjustmentFactor());
                    tmpBI.set(tmpUpExprDim + tmpUpVarDim + tmpLoExprDim + i, 0, -tmpVariable.getAdjustedLowerLimit());
                }
            }

            destinationBuilder.inequalities(tmpAI, tmpBI);
        }
    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public static Builder getBuilder(final MatrixStore<Double> Q, final MatrixStore<Double> C) {
        return ConvexSolver.getBuilder().objective(Q, C);
    }

    private final ConvexSolver.AbstractBuilder<?, ?> myMatrices;

    final Cholesky<Double> myCholesky;

    final LU<Double> myLU;

    @SuppressWarnings("unused")
    private ConvexSolver(final Options solverOptions) {
        this(null, solverOptions);
    }

    protected ConvexSolver(final ConvexSolver.AbstractBuilder<?, ?> matrices, final Optimisation.Options solverOptions) {

        super(solverOptions);

        myMatrices = matrices;

        final MatrixStore<Double> tmpQ = this.getQ();

        myCholesky = Cholesky.make(tmpQ);
        myLU = LU.make(tmpQ);
    }

    public void dispose() {

        super.dispose();

        myMatrices.reset();
    }

    public final Optimisation.Result solve(final Optimisation.Result kickStarter) {

        boolean tmpContinue = true;

        if (options.validate) {
            tmpContinue = this.validate();
        }

        if (tmpContinue) {
            tmpContinue = this.initialise(kickStarter);
        }

        if (tmpContinue) {

            this.resetIterationsCount();

            do {

                this.performIteration();

                this.incrementIterationsCount();

            } while (!this.getState().isFailure() && this.needsAnotherIteration() && this.isIterationAllowed());
        }

        return this.buildResult();
    }

    @Override
    public String toString() {
        return myMatrices.toString();
    }

    protected int countEqualityConstraints() {
        return myMatrices.countEqualityConstraints();
    }

    protected int countInequalityConstraints() {
        return myMatrices.countInequalityConstraints();
    }

    protected int countVariables() {
        return myMatrices.countVariables();
    }

    @Override
    protected double evaluateFunction(final Access1D<?> solution) {

        final MatrixStore<Double> tmpX = this.getX();

        return tmpX.transpose().multiply(this.getQ().multiply(tmpX)).multiply(0.5).subtract(tmpX.transpose().multiply(this.getC())).doubleValue(0L);
    }

    @Override
    protected MatrixStore<Double> extractSolution() {

        return this.getX().copy();

    }

    protected void fillX(final Access1D<?> solution) {
        final int tmpLimit = this.countVariables();
        for (int i = 0; i < tmpLimit; i++) {
            myMatrices.setX(i, solution.doubleValue(i));
        }
    }

    protected void fillX(final double value) {
        final int tmpLimit = this.countVariables();
        for (int i = 0; i < tmpLimit; i++) {
            myMatrices.setX(i, value);
        }
    }

    protected MatrixStore<Double> getAE() {
        return myMatrices.getAE();
    }

    protected MatrixStore<Double> getAEX() {
        return myMatrices.getAEX();
    }

    protected MatrixStore<Double> getAI() {
        return myMatrices.getAI();
    }

    protected MatrixStore<Double> getAIX(final int[] selector) {
        return myMatrices.getAIX(selector);
    }

    protected MatrixStore<Double> getBE() {
        return myMatrices.getBE();
    }

    protected MatrixStore<Double> getBI() {
        return myMatrices.getBI();
    }

    protected MatrixStore<Double> getBI(final int[] selector) {
        return myMatrices.getBI(selector);
    }

    protected MatrixStore<Double> getC() {
        return myMatrices.getC();
    }

    protected abstract MatrixStore<Double> getIterationKKT();

    protected abstract MatrixStore<Double> getIterationRHS();

    protected MatrixStore<Double> getQ() {
        return myMatrices.getQ();
    }

    protected PhysicalStore<Double> getSE() {
        return myMatrices.getSE();
    }

    protected MatrixStore<Double> getSI(final int... rowSelector) {
        return myMatrices.getSI(rowSelector);
    }

    protected PhysicalStore<Double> getX() {
        return myMatrices.getX();
    }

    protected boolean hasEqualityConstraints() {
        return myMatrices.hasEqualityConstraints();
    }

    protected boolean hasInequalityConstraints() {
        return myMatrices.hasInequalityConstraints();
    }

    protected boolean hasObjective() {
        return myMatrices.hasObjective();
    }

    protected boolean isX() {
        return myMatrices.isX();
    }

    abstract protected void performIteration();

    protected void resetX() {
        myMatrices.resetX();
    }

    protected void setX(final int index, final double value) {
        myMatrices.setX(index, value);
    }

    @Override
    protected boolean validate() {

        final MatrixStore<Double> tmpQ = this.getQ();
        final MatrixStore<Double> tmpC = this.getC();

        if ((tmpQ == null) || (tmpC == null)) {
            throw new IllegalArgumentException("Neither Q nor C may be null!");
        }

        if (!MatrixUtils.isHermitian(tmpQ)) {
            if (this.isDebug()) {
                this.debug("Q not symmetric!", tmpQ);
            }
            throw new IllegalArgumentException("Q must be symmetric!");
        }

        if (!myCholesky.isSPD()) {
            // Not symmetric positive definite. Check if at least positive semidefinite.

            final Eigenvalue<Double> tmpEvD = Eigenvalue.PRIMITIVE.make(true);

            tmpEvD.computeValuesOnly(tmpQ);

            final Array1D<ComplexNumber> tmpEigenvalues = tmpEvD.getEigenvalues();

            tmpEvD.reset();

            for (final ComplexNumber tmpValue : tmpEigenvalues) {
                if ((tmpValue.doubleValue() < ZERO) || !tmpValue.isReal()) {
                    if (this.isDebug()) {
                        this.debug("Q not positive semidefinite!");
                        this.debug("The eigenvalues are: {}", tmpEigenvalues);
                    }
                    throw new IllegalArgumentException("Q must be positive semidefinite!");
                }
            }
        }

        this.setState(State.VALID);
        return true;
    }

}

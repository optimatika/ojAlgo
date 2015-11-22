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
package org.ojalgo.optimisation.convex;

import static org.ojalgo.constant.PrimitiveMath.*;
import static org.ojalgo.function.PrimitiveFunction.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.ojalgo.access.Access1D;
import org.ojalgo.access.IntIndex;
import org.ojalgo.access.IntRowColumn;
import org.ojalgo.array.Array1D;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.matrix.decomposition.Cholesky;
import org.ojalgo.matrix.decomposition.Eigenvalue;
import org.ojalgo.matrix.decomposition.LU;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.matrix.store.SparseStore;
import org.ojalgo.optimisation.BaseSolver;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
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
 * {@linkplain Builder} class. It will return an appropriate subclass for you.
 * </p>
 * <p>
 * When the KKT matrix is nonsingular, there is a unique optimal primal-dual pair (x,l). If the KKT matrix is
 * singular, but the KKT system is still solvable, any solution yields an optimal pair (x,l). If the KKT
 * system is not solvable, the quadratic optimization problem is unbounded below or infeasible.
 * </p>
 *
 * @author apete
 */
public abstract class ConvexSolver extends BaseSolver {

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
                    return new MixedASS(this, options);
                } else {
                    return new PureASS(this, options);
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

    public static final class Input {

        private final MatrixStore<Double> myA;
        private final MatrixStore<Double> myB;
        private final MatrixStore<Double> myC;
        private final MatrixStore<Double> myQ;

        /**
         * | Q | = | C |
         *
         * @param Q
         * @param C
         */
        public Input(final MatrixStore<Double> Q, final MatrixStore<Double> C) {
            this(Q, C, null, null);
        }

        /**
         * | Q | A<sup>T</sup> | = | C | <br>
         * | A | 0 | = | B |
         *
         * @param Q
         * @param C
         * @param A
         * @param B
         */
        public Input(final MatrixStore<Double> Q, final MatrixStore<Double> C, final MatrixStore<Double> A, final MatrixStore<Double> B) {

            super();

            myQ = Q;
            myC = C;
            myA = A;
            myB = B;
        }

        final int countConstraints() {
            return (int) (myB != null ? myB.count() : 0);
        }

        final int countVariables() {
            return (int) myC.count();
        }

        final MatrixStore<Double> getA() {
            return myA;
        }

        final MatrixStore<Double> getB() {
            return myB;
        }

        final MatrixStore<Double> getC() {
            return myC;
        }

        final MatrixStore<Double> getKKT() {
            if (myA != null) {
                return myQ.builder().right(myA.transpose()).below(myA).build();
            } else {
                return myQ;
            }
        }

        final MatrixStore<Double> getQ() {
            return myQ;
        }

        final MatrixStore<Double> getRHS() {
            if (myB != null) {
                return myC.builder().below(myB).build();
            } else {
                return myC;
            }
        }

        final boolean isConstrained() {
            return (myA != null) && (myA.count() > 0L);
        }

    }

    public static final class Output {

        private final MatrixStore<Double> myL;
        private final boolean mySolvable;
        private final MatrixStore<Double> myX;

        Output() {

            super();

            myX = null;
            myL = null;
            mySolvable = false;
        }

        Output(final MatrixStore<Double> X, final MatrixStore<Double> L, final boolean solvable) {

            super();

            myX = X;
            myL = L;
            mySolvable = solvable;
        }

        public final MatrixStore<Double> getL() {
            return myL;
        }

        public final MatrixStore<Double> getX() {
            return myX;
        }

        public final boolean isSolvable() {
            return mySolvable;
        }

        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {

            final StringBuilder tmpSB = new StringBuilder();

            tmpSB.append(mySolvable);

            if (mySolvable) {

                tmpSB.append(" X=");

                tmpSB.append(Array1D.PRIMITIVE.copy(this.getX()));

                tmpSB.append(" L=");

                tmpSB.append(Array1D.PRIMITIVE.copy(this.getL()));
            }

            return tmpSB.toString();
        }

    }

    public static final Output FAILURE = new Output();

    static final PhysicalStore.Factory<Double, PrimitiveDenseStore> FACTORY = PrimitiveDenseStore.FACTORY;

    public static void copy(final ExpressionsBasedModel sourceModel, final ConvexSolver.Builder destinationBuilder) {

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

        final List<Expression> tmpEqExpr = sourceModel.constraints().filter((final Expression c) -> c.isEqualityConstraint() && !c.isAnyQuadraticFactorNonZero()).collect(Collectors.toList());
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

        final Expression tmpObjExpr = sourceModel.getObjectiveExpression().compensate(tmpFixedVariables);

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

        final List<Expression> tmpUpExpr = sourceModel.constraints().filter((final Expression c2) -> c2.isUpperConstraint() && !c2.isAnyQuadraticFactorNonZero()).collect(Collectors.toList());
        final int tmpUpExprDim = tmpUpExpr.size();
        final List<Variable> tmpUpVar = sourceModel.bounds().filter((final Variable c4) -> c4.isUpperConstraint()).collect(Collectors.toList());
        final int tmpUpVarDim = tmpUpVar.size();

        final List<Expression> tmpLoExpr = sourceModel.constraints().filter((final Expression c1) -> c1.isLowerConstraint() && !c1.isAnyQuadraticFactorNonZero()).collect(Collectors.toList());
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

    public static ConvexSolver.Builder getBuilder() {
        return new ConvexSolver.Builder();
    }

    public static ConvexSolver.Builder getBuilder(final MatrixStore<Double> Q, final MatrixStore<Double> C) {
        return ConvexSolver.getBuilder().objective(Q, C);
    }

    final Cholesky<Double> myCholesky;

    final LU<Double> myLU;

    protected ConvexSolver(final ConvexSolver.Builder matrices, final Optimisation.Options solverOptions) {

        super(matrices, solverOptions);

        final MatrixStore<Double> tmpQ = this.getIterationQ();

        myCholesky = Cholesky.make(tmpQ);
        myLU = LU.make(tmpQ);

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
    protected double evaluateFunction(final Access1D<?> solution) {

        final MatrixStore<Double> tmpX = this.getSolutionX();

        return tmpX.transpose().multiply(this.getQ().multiply(tmpX)).scale(0.5).subtract(tmpX.transpose().multiply(this.getC())).doubleValue(0L);
    }

    @Override
    protected MatrixStore<Double> extractSolution() {

        return this.getSolutionX().copy();

    }

    protected abstract MatrixStore<Double> getIterationKKT();

    protected abstract MatrixStore<Double> getIterationRHS();

    @Override
    protected boolean initialise(final Result kickStarter) {

        myCholesky.decompose(this.getIterationQ());

        return true;
    }

    abstract protected void performIteration();

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

            final Eigenvalue<Double> tmpEvD = Eigenvalue.makePrimitive(true);

            tmpEvD.computeValuesOnly(tmpQ);

            final Array1D<ComplexNumber> tmpEigenvalues = tmpEvD.getEigenvalues();

            tmpEvD.reset();

            for (final ComplexNumber tmpValue : tmpEigenvalues) {
                if ((tmpValue.doubleValue() < ZERO) || !tmpValue.isReal()) {
                    if (this.isDebug()) {
                        this.debug("Q not positive semidefinite!", tmpQ);
                    }
                    throw new IllegalArgumentException("Q must be positive semidefinite!");
                }
            }
        }

        this.setState(State.VALID);
        return true;
    }

    abstract MatrixStore<Double> getIterationC();

    final MatrixStore<Double> getIterationQ() {
        return this.getQ();
    }

    final MatrixStore<Double> getSolutionLE() {
        return this.getLE();
    }

    final MatrixStore<Double> getSolutionLI(final int... active) {
        return this.getLI(active);
    }

    final MatrixStore<Double> getSolutionX() {
        return this.getX();
    }

}

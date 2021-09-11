/*
 * Copyright 1997-2021 Optimatika
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

import static org.ojalgo.function.constant.PrimitiveMath.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.ojalgo.ProgrammingError;
import org.ojalgo.array.Array1D;
import org.ojalgo.array.SparseArray;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.matrix.decomposition.Cholesky;
import org.ojalgo.matrix.decomposition.Eigenvalue;
import org.ojalgo.matrix.decomposition.LU;
import org.ojalgo.matrix.decomposition.MatrixDecomposition;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.matrix.store.RowsSupplier;
import org.ojalgo.matrix.store.SparseStore;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.GenericSolver;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.UpdatableSolver;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.optimisation.linear.LinearSolver;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Access2D.Collectable;
import org.ojalgo.structure.Structure1D.IntIndex;
import org.ojalgo.structure.Structure2D.IntRowColumn;

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
public abstract class ConvexSolver extends GenericSolver implements UpdatableSolver {

    public static final class Builder extends GenericSolver.Builder<ConvexSolver.Builder, ConvexSolver> {

        private ConvexObjectiveFunction myObjective = null;

        /**
         * @deprecated v50 Use {@link ConvexSolver#newBuilder()} instead.
         */
        @Deprecated
        public Builder() {
            super();
        }

        /**
         * @deprecated v50 Use {@link ConvexSolver#newBuilder()} instead.
         */
        @Deprecated
        public Builder(final MatrixStore<Double> C) {

            super();

            this.objective(C);
        }

        /**
         * @deprecated v50 Use {@link ConvexSolver#newBuilder()} instead.
         */
        @Deprecated
        public Builder(final MatrixStore<Double> Q, final MatrixStore<Double> C) {

            super();

            this.objective(Q, C);
        }

        Builder(final MatrixStore<Double>[] matrices) {

            super();

            if (matrices.length >= 2 && matrices[0] != null && matrices[1] != null) {
                this.equalities(matrices[0], matrices[1]);
            }

            if (matrices.length >= 4) {
                if (matrices[2] != null) {
                    this.objective(matrices[2], matrices[3]);
                } else if (matrices[3] != null) {
                    this.objective(matrices[3]);
                }
            }

            if (matrices.length >= 6 && matrices[4] != null && matrices[5] != null) {
                this.inequalities(matrices[4], matrices[5]);
            }
        }

        @Override
        public MatrixStore<Double> getAI() {
            return super.getAI();
        }

        @Override
        public SparseArray<Double> getAI(final int row) {
            return super.getAI(row);
        }

        @Override
        public MatrixStore<Double> getAI(final int... rows) {
            return super.getAI(rows);
        }

        @Override
        public MatrixStore<Double> getBI() {
            return super.getBI();
        }

        /**
         * Linear objective: [C]
         */
        @Override
        public PhysicalStore<Double> getC() {
            return myObjective.linear();
        }

        /**
         * Quadratic objective: [Q]
         */
        public PhysicalStore<Double> getQ() {
            return myObjective.quadratic();
        }

        @Override
        public Builder inequalities(final Access2D<Double> mtrxAI, final Access1D<Double> mtrxBI) {
            return super.inequalities(mtrxAI, mtrxBI);
        }

        /**
         * @deprecated v50 Use {@link #objective(MatrixStore, MatrixStore)} instead, or build a
         *             {@link LinearSolver}.
         */
        @Deprecated
        public Builder objective(final MatrixStore<Double> mtrxC) {
            myObjective = ConvexSolver.toObjectiveFunction(null, mtrxC);
            this.setObjective(myObjective);
            return this;
        }

        public Builder objective(final MatrixStore<Double> mtrxQ, final MatrixStore<Double> mtrxC) {
            myObjective = ConvexSolver.toObjectiveFunction(mtrxQ, mtrxC);
            this.setObjective(myObjective);
            return this;
        }

        @Override
        public void reset() {
            super.reset();
            myObjective = null;
        }

        /**
         * Disregard the objective function (set it to zero) and form the dual LP.
         */
        public LinearSolver.GeneralBuilder toFeasibilityChecker() {

            MatrixStore<Double> mtrxAE = this.getAE();
            MatrixStore<Double> mtrxBE = this.getBE();

            MatrixStore<Double> mtrxAI = this.getAI();
            MatrixStore<Double> mtrxBI = this.getBI();

            LinearSolver.GeneralBuilder retVal = LinearSolver.newGeneralBuilder();

            int nbEqus = this.countEqualityConstraints();
            int nbIneq = this.countInequalityConstraints();
            int nbVars = this.countVariables();

            MatrixStore<Double> rhs = MatrixStore.PRIMITIVE64.makeZero(nbVars, 1).get();

            if (nbEqus > 0) {

                MatrixStore<Double> transpAE = mtrxAE.transpose();

                if (nbIneq > 0) {

                    retVal.objective(mtrxBE.logical().below(mtrxBE.negate(), mtrxBI).get());

                    retVal.equalities(transpAE.logical().right(transpAE.negate(), mtrxAI.transpose()).get(), rhs);

                } else {

                    retVal.objective(mtrxBE.logical().below(mtrxBE.negate()).get());

                    retVal.equalities(transpAE.logical().right(transpAE.negate()).get(), rhs);
                }

            } else if (nbIneq > 0) {

                retVal.objective(mtrxBI);

                retVal.equalities(mtrxAI.transpose(), rhs);

            } else {

                throw new IllegalStateException("The problem is unconstrained!");
            }

            return retVal;
        }

        /**
         * Linearise the objective function (at the specified point) and duplicate all variables to handle the
         * (potential) positive and negative parts separately.
         */
        public LinearSolver.GeneralBuilder toLinearApproximation(final Access1D<Double> point) {

            MatrixStore<Double> mtrxC = this.getObjective().toFirstOrderApproximation(point).getLinearFactors();

            MatrixStore<Double> mtrxAE = this.getAE();
            MatrixStore<Double> mtrxBE = this.getBE();

            MatrixStore<Double> mtrxAI = this.getAI();
            MatrixStore<Double> mtrxBI = this.getBI();

            LinearSolver.GeneralBuilder retVal = LinearSolver.newGeneralBuilder();

            retVal.objective(mtrxC.logical().below(mtrxC.negate()).get());

            if (mtrxAE != null && mtrxBE != null) {
                retVal.equalities(mtrxAE.logical().right(mtrxAE.negate()).get(), mtrxBE);
            }

            if (mtrxAI != null && mtrxBI != null) {
                retVal.inequalities(mtrxAI.logical().right(mtrxAI.negate()).get(), mtrxBI);
            }

            return retVal;
        }

        @Override
        protected void append(final StringBuilder builder) {

            super.append(builder);

            GenericSolver.Builder.append(builder, "Q", this.getQ());
        }

        @Override
        protected ConvexSolver doBuild(final Optimisation.Options options) {

            if (this.hasInequalityConstraints()) {
                if (options.sparse == null || options.sparse.booleanValue()) {
                    return new IterativeASS(this, options);
                }
                return new DirectASS(this, options);
            }
            if (this.hasEqualityConstraints()) {
                return new QPESolver(this, options);
            }
            return new UnconstrainedSolver(this, options);
        }

    }

    public static final class ModelIntegration extends ExpressionsBasedModel.Integration<ConvexSolver> {

        public ConvexSolver build(final ExpressionsBasedModel model) {

            final ConvexSolver.Builder tmpBuilder = ConvexSolver.newBuilder();

            ConvexSolver.copy(model, tmpBuilder);

            return tmpBuilder.build(model.options);
        }

        public boolean isCapable(final ExpressionsBasedModel model) {
            return !model.isAnyVariableInteger() && model.isAnyObjectiveQuadratic() && !model.isAnyConstraintQuadratic();
        }

        @Override
        protected boolean isSolutionMapped() {
            return true;
        }

    }

    private static final String Q_NOT_POSITIVE_SEMIDEFINITE = "Q not positive semidefinite!";
    private static final String Q_NOT_SYMMETRIC = "Q not symmetric!";
    private static final double SMALL_DIAGONAL_FACTOR = RELATIVELY_SMALL + MACHINE_EPSILON;

    public static void copy(final ExpressionsBasedModel sourceModel, final ConvexSolver.Builder destinationBuilder) {

        destinationBuilder.reset();

        final List<Variable> freeVariables = sourceModel.getFreeVariables();
        final Set<IntIndex> fixedVariables = sourceModel.getFixedVariables();

        final int numbVars = freeVariables.size();

        // AE & BE

        final List<Expression> tmpEqExpr = sourceModel.constraints()
                .filter((final Expression c) -> c.isEqualityConstraint() && !c.isAnyQuadraticFactorNonZero()).collect(Collectors.toList());
        final int numbEqExpr = tmpEqExpr.size();

        if (numbEqExpr > 0) {

            final SparseStore<Double> mtrxAE = SparseStore.PRIMITIVE64.make(numbEqExpr, numbVars);
            final PhysicalStore<Double> mtrxBE = Primitive64Store.FACTORY.make(numbEqExpr, 1);

            for (int i = 0; i < numbEqExpr; i++) {

                final Expression tmpExpression = tmpEqExpr.get(i).compensate(fixedVariables);

                for (final IntIndex tmpKey : tmpExpression.getLinearKeySet()) {
                    final int tmpIndex = sourceModel.indexOfFreeVariable(tmpKey.index);
                    if (tmpIndex >= 0) {
                        mtrxAE.set(i, tmpIndex, tmpExpression.getAdjustedLinearFactor(tmpKey));
                    }
                }
                mtrxBE.set(i, 0, tmpExpression.getAdjustedUpperLimit());
            }

            destinationBuilder.equalities(mtrxAE, mtrxBE);
        }

        // Q & C

        final Expression tmpObjExpr = sourceModel.objective().compensate(fixedVariables);

        PhysicalStore<Double> mtrxQ = null;
        if (tmpObjExpr.isAnyQuadraticFactorNonZero()) {
            mtrxQ = Primitive64Store.FACTORY.make(numbVars, numbVars);

            final BinaryFunction<Double> tmpBaseFunc = sourceModel.isMaximisation() ? SUBTRACT : ADD;
            UnaryFunction<Double> tmpModifier;
            for (final IntRowColumn tmpKey : tmpObjExpr.getQuadraticKeySet()) {
                final int tmpRow = sourceModel.indexOfFreeVariable(tmpKey.row);
                final int tmpColumn = sourceModel.indexOfFreeVariable(tmpKey.column);
                if (tmpRow >= 0 && tmpColumn >= 0) {
                    tmpModifier = tmpBaseFunc.second(tmpObjExpr.getAdjustedQuadraticFactor(tmpKey));
                    mtrxQ.modifyOne(tmpRow, tmpColumn, tmpModifier);
                    mtrxQ.modifyOne(tmpColumn, tmpRow, tmpModifier);
                }
            }
        }

        PhysicalStore<Double> mtrxC = null;
        if (tmpObjExpr.isAnyLinearFactorNonZero()) {
            mtrxC = Primitive64Store.FACTORY.make(numbVars, 1);
            if (sourceModel.isMinimisation()) {
                for (final IntIndex tmpKey : tmpObjExpr.getLinearKeySet()) {
                    final int tmpIndex = sourceModel.indexOfFreeVariable(tmpKey.index);
                    if (tmpIndex >= 0) {
                        mtrxC.set(tmpIndex, 0, -tmpObjExpr.getAdjustedLinearFactor(tmpKey));
                    }
                }
            } else {
                for (final IntIndex tmpKey : tmpObjExpr.getLinearKeySet()) {
                    final int tmpIndex = sourceModel.indexOfFreeVariable(tmpKey.index);
                    if (tmpIndex >= 0) {
                        mtrxC.set(tmpIndex, 0, tmpObjExpr.getAdjustedLinearFactor(tmpKey));
                    }
                }
            }
        }

        if (mtrxQ == null && mtrxC == null) {
            // In some very rare case the model was verified to be a quadratic
            // problem, but then the presolver eliminated/fixed all variables
            // part of the objective function - then we would end up here.
            // Rather than always having to do very expensive checks we simply
            // generate a well-behaved objective function here.
            mtrxQ = Primitive64Store.FACTORY.makeEye(numbVars, numbVars);
        }

        destinationBuilder.objective(mtrxQ, mtrxC);

        // AI & BI

        final List<Expression> tmpUpExpr = sourceModel.constraints().filter(e -> e.isUpperConstraint() && !e.isAnyQuadraticFactorNonZero())
                .collect(Collectors.toList());
        final int numbUpExpr = tmpUpExpr.size();

        final List<Variable> tmpUpVar = sourceModel.bounds().filter((final Variable c4) -> c4.isUpperConstraint()).collect(Collectors.toList());
        final int numbUpVar = tmpUpVar.size();

        final List<Expression> tmpLoExpr = sourceModel.constraints()
                .filter((final Expression c1) -> c1.isLowerConstraint() && !c1.isAnyQuadraticFactorNonZero()).collect(Collectors.toList());
        final int numbLoExpr = tmpLoExpr.size();

        final List<Variable> tmpLoVar = sourceModel.bounds().filter((final Variable c3) -> c3.isLowerConstraint()).collect(Collectors.toList());
        final int numbLoVar = tmpLoVar.size();

        if (numbUpExpr + numbUpVar + numbLoExpr + numbLoVar > 0) {

            final RowsSupplier<Double> mtrxAI = Primitive64Store.FACTORY.makeRowsSupplier(numbVars);
            final PhysicalStore<Double> mtrxBI = Primitive64Store.FACTORY.make(numbUpExpr + numbUpVar + numbLoExpr + numbLoVar, 1);

            if (numbUpExpr > 0) {
                for (int i = 0; i < numbUpExpr; i++) {
                    final SparseArray<Double> rowAI = mtrxAI.addRow();
                    final Expression tmpExpression = tmpUpExpr.get(i).compensate(fixedVariables);
                    for (final IntIndex tmpKey : tmpExpression.getLinearKeySet()) {
                        final int tmpIndex = sourceModel.indexOfFreeVariable(tmpKey.index);
                        if (tmpIndex >= 0) {
                            rowAI.set(tmpIndex, tmpExpression.getAdjustedLinearFactor(tmpKey));
                        }
                    }
                    mtrxBI.set(i, 0, tmpExpression.getAdjustedUpperLimit());
                }
            }

            if (numbUpVar > 0) {
                for (int i = 0; i < numbUpVar; i++) {
                    final SparseArray<Double> rowAI = mtrxAI.addRow();
                    final Variable tmpVariable = tmpUpVar.get(i);
                    rowAI.set(sourceModel.indexOfFreeVariable(tmpVariable), tmpVariable.getAdjustmentFactor());
                    mtrxBI.set(numbUpExpr + i, 0, tmpVariable.getAdjustedUpperLimit());
                }
            }

            if (numbLoExpr > 0) {
                for (int i = 0; i < numbLoExpr; i++) {
                    final SparseArray<Double> rowAI = mtrxAI.addRow();
                    final Expression tmpExpression = tmpLoExpr.get(i).compensate(fixedVariables);
                    for (final IntIndex tmpKey : tmpExpression.getLinearKeySet()) {
                        final int tmpIndex = sourceModel.indexOfFreeVariable(tmpKey.index);
                        if (tmpIndex >= 0) {
                            rowAI.set(tmpIndex, -tmpExpression.getAdjustedLinearFactor(tmpKey));
                        }
                    }
                    mtrxBI.set(numbUpExpr + numbUpVar + i, 0, -tmpExpression.getAdjustedLowerLimit());
                }
            }

            if (numbLoVar > 0) {
                for (int i = 0; i < numbLoVar; i++) {
                    final SparseArray<Double> rowAI = mtrxAI.addRow();
                    final Variable tmpVariable = tmpLoVar.get(i);
                    rowAI.set(sourceModel.indexOfFreeVariable(tmpVariable), -tmpVariable.getAdjustmentFactor());
                    mtrxBI.set(numbUpExpr + numbUpVar + numbLoExpr + i, 0, -tmpVariable.getAdjustedLowerLimit());
                }
            }

            destinationBuilder.inequalities(mtrxAI, mtrxBI);
        }
    }

    /**
     * @deprecated v50 Use {@link ConvexSolver#newBuilder()} instead.
     */
    @Deprecated
    public static Builder getBuilder() {
        return ConvexSolver.newBuilder();
    }

    /**
     * @deprecated v50 Use {@link ConvexSolver#newBuilder()} instead.
     */
    @Deprecated
    public static Builder getBuilder(final MatrixStore<Double> Q, final MatrixStore<Double> C) {
        return ConvexSolver.newBuilder().objective(Q, C);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    static ConvexObjectiveFunction toObjectiveFunction(final MatrixStore<Double> mtrxQ, final MatrixStore<Double> mtrxC) {

        if (mtrxQ == null && mtrxC == null) {
            ProgrammingError.throwWithMessage("Both parameters can't be null!");
        }

        PhysicalStore<Double> tmpQ = null;
        PhysicalStore<Double> tmpC = null;

        if (mtrxQ == null) {
            tmpQ = Primitive64Store.FACTORY.make(mtrxC.count(), mtrxC.count());
        } else if (mtrxQ instanceof PhysicalStore) {
            tmpQ = (PhysicalStore<Double>) mtrxQ;
        } else {
            tmpQ = mtrxQ.copy();
        }

        if (mtrxC == null) {
            tmpC = Primitive64Store.FACTORY.make(tmpQ.countRows(), 1L);
        } else if (mtrxC instanceof PhysicalStore) {
            tmpC = (PhysicalStore<Double>) mtrxC;
        } else {
            tmpC = mtrxC.copy();
        }

        return new ConvexObjectiveFunction(tmpQ, tmpC);
    }

    private final ConvexSolver.Builder myMatrices;
    private boolean myPatchedQ = false;
    private final Primitive64Store mySolutionX;
    private final MatrixDecomposition.Solver<Double> mySolverGeneral;
    private final Cholesky<Double> mySolverQ;
    private boolean myZeroQ = false;

    @SuppressWarnings("unused")
    private ConvexSolver(final Options solverOptions) {
        this(null, solverOptions);
    }

    protected ConvexSolver(final ConvexSolver.Builder matrices, final Optimisation.Options solverOptions) {

        super(solverOptions);

        myMatrices = matrices;

        mySolutionX = Primitive64Store.FACTORY.make(this.countVariables(), 1L);

        mySolverQ = Cholesky.PRIMITIVE.make(this.getMatrixQ());
        mySolverGeneral = LU.PRIMITIVE.make(this.getMatrixQ());
    }

    public void dispose() {

        super.dispose();

        myMatrices.reset();
    }

    public final Optimisation.Result solve(final Optimisation.Result kickStarter) {

        if (this.initialise(kickStarter)) {

            this.resetIterationsCount();

            do {

                this.performIteration();

            } while (this.isIterationAllowed() && this.needsAnotherIteration());
        }

        return this.buildResult();
    }

    @Override
    public String toString() {
        return myMatrices.toString();
    }

    protected boolean computeGeneral(final Collectable<Double, ? super PhysicalStore<Double>> matrix) {
        return mySolverGeneral.compute(matrix);
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

        final MatrixStore<Double> tmpX = this.getSolutionX();

        return tmpX.transpose().multiply(this.getMatrixQ().multiply(tmpX)).multiply(0.5).subtract(tmpX.transpose().multiply(this.getMatrixC())).doubleValue(0L);
    }

    @Override
    protected MatrixStore<Double> extractSolution() {

        return this.getSolutionX().copy();

    }

    protected abstract Collectable<Double, ? super PhysicalStore<Double>> getIterationKKT();

    protected abstract Collectable<Double, ? super PhysicalStore<Double>> getIterationRHS();

    protected MatrixStore<Double> getMatrixAE() {
        return myMatrices.getAE();
    }

    protected MatrixStore<Double> getMatrixAI() {
        return myMatrices.getAI();
    }

    protected SparseArray<Double> getMatrixAI(final int row) {
        return myMatrices.getAI(row);
    }

    protected MatrixStore<Double> getMatrixAI(final int[] rows) {
        return myMatrices.getAI(rows);
    }

    protected MatrixStore<Double> getMatrixBE() {
        return myMatrices.getBE();
    }

    protected MatrixStore<Double> getMatrixBI() {
        return myMatrices.getBI();
    }

    protected double getMatrixBI(final int row) {
        return myMatrices.getBI().doubleValue(row);
    }

    protected MatrixStore<Double> getMatrixBI(final int[] selector) {
        return myMatrices.getBI().logical().row(selector).get();
    }

    protected MatrixStore<Double> getMatrixC() {
        return myMatrices.getC();
    }

    protected PhysicalStore<Double> getMatrixQ() {
        return myMatrices.getQ();
    }

    protected int getRankGeneral() {
        if (mySolverGeneral instanceof MatrixDecomposition.RankRevealing) {
            return ((MatrixDecomposition.RankRevealing) mySolverGeneral).getRank();
        }
        if (mySolverGeneral.isSolvable()) {
            return (int) mySolverGeneral.reconstruct().countColumns();
        }
        return 0;
    }

    protected MatrixStore<Double> getSolutionGeneral(final Collectable<Double, ? super PhysicalStore<Double>> rhs) {
        return mySolverGeneral.getSolution(rhs);
    }

    protected MatrixStore<Double> getSolutionGeneral(final Collectable<Double, ? super PhysicalStore<Double>> rhs, final PhysicalStore<Double> preallocated) {
        return mySolverGeneral.getSolution(rhs, preallocated);
    }

    protected MatrixStore<Double> getSolutionQ(final Collectable<Double, ? super PhysicalStore<Double>> rhs) {
        return mySolverQ.getSolution(rhs);
    }

    protected MatrixStore<Double> getSolutionQ(final Collectable<Double, ? super PhysicalStore<Double>> rhs, final PhysicalStore<Double> preallocated) {
        return mySolverQ.getSolution(rhs, preallocated);
    }

    /**
     * Solution / Variables: [X]
     */
    protected PhysicalStore<Double> getSolutionX() {
        return mySolutionX;
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

    /**
     * @return true/false if the main algorithm may start or not
     */
    protected boolean initialise(final Result kickStarter) {

        PhysicalStore<Double> matrixQ = this.getMatrixQ();
        this.setState(State.VALID);

        boolean symmetric = true;
        if (options.validate) {

            if (!matrixQ.isHermitian()) {

                symmetric = false;
                this.setState(State.INVALID);

                if (!this.isLogDebug()) {
                    throw new IllegalArgumentException(Q_NOT_SYMMETRIC);
                }
                this.log(Q_NOT_SYMMETRIC, matrixQ);
            }
        }

        myPatchedQ = false;
        myZeroQ = false;
        if (!mySolverQ.compute(matrixQ)) {
            double largest = matrixQ.aggregateAll(Aggregator.LARGEST).doubleValue();
            if (largest > SMALL_DIAGONAL_FACTOR) {
                matrixQ.modifyDiagonal(ADD.by(SMALL_DIAGONAL_FACTOR * largest));
                mySolverQ.compute(matrixQ);
                myPatchedQ = true;
            } else {
                myZeroQ = true;
            }
        }

        boolean semidefinite = true;
        if (options.validate && !mySolverQ.isSPD()) {
            // Not symmetric positive definite. Check if at least positive semidefinite.

            Eigenvalue<Double> decompEvD = Eigenvalue.PRIMITIVE.make(matrixQ, true);
            decompEvD.computeValuesOnly(matrixQ);
            final Array1D<ComplexNumber> eigenvalues = decompEvD.getEigenvalues();
            decompEvD.reset();

            for (final ComplexNumber eigval : eigenvalues) {
                if (eigval.doubleValue() < ZERO && !eigval.isSmall(TEN) || !eigval.isReal()) {

                    semidefinite = false;
                    this.setState(State.INVALID);

                    if (!this.isLogDebug()) {
                        throw new IllegalArgumentException(Q_NOT_POSITIVE_SEMIDEFINITE);
                    }
                    this.log(Q_NOT_POSITIVE_SEMIDEFINITE);
                    this.log("The eigenvalues are: {}", eigenvalues);
                }
            }
        }

        return symmetric && semidefinite;
    }

    protected boolean isSolvableGeneral() {
        return mySolverGeneral.isSolvable();
    }

    protected boolean isSolvableQ() {
        //        double max = Math.max(RELATIVELY_SMALL, mySolverQ.getRankThreshold());
        //        int countVariables = this.countVariables();
        //        int countSignificant = mySolverQ.countSignificant(max);
        //        return countVariables == countSignificant;
        return mySolverQ.isSolvable();
    }

    protected abstract boolean needsAnotherIteration();

    abstract protected void performIteration();

    protected boolean solveFullKKT(final PhysicalStore<Double> preallocated) {
        if (this.computeGeneral(this.getIterationKKT())) {
            this.getSolutionGeneral(this.getIterationRHS(), preallocated);
            return true;
        }
        if (this.isLogDebug()) {
            options.logger_appender.println("KKT system unsolvable!");
            //                options.logger_appender.printmtrx("KKT", this.getIterationKKT().collect(FACTORY));
            //                options.logger_appender.printmtrx("RHS", this.getIterationRHS().collect(FACTORY));
        }
        return false;
    }

    protected Optimisation.Result solveLP() {
        return LinearSolver.solve(myMatrices, options, !myZeroQ);
    }

}

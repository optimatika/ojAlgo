/*
 * Copyright 1997-2018 Optimatika
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
import org.ojalgo.access.Access2D.Collectable;
import org.ojalgo.access.Structure1D.IntIndex;
import org.ojalgo.access.Structure2D.IntRowColumn;
import org.ojalgo.array.Array1D;
import org.ojalgo.array.SparseArray;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.matrix.PrimitiveMatrix;
import org.ojalgo.matrix.decomposition.Cholesky;
import org.ojalgo.matrix.decomposition.Eigenvalue;
import org.ojalgo.matrix.decomposition.LU;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PhysicalStore.Factory;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
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
import org.ojalgo.type.context.NumberContext;

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

        private static final NumberContext NC = NumberContext.getGeneral(12);

        private MatrixStore<Double> myAE = null;
        private RowsSupplier<Double> myAI = null;
        private MatrixStore<Double> myBE = null;
        private MatrixStore<Double> myBI = null;
        private MatrixStore<Double> myC = null;
        private PhysicalStore<Double> myQ = null;

        public Builder() {
            super();
        }

        public Builder(final MatrixStore<Double> C) {

            super();

            this.objective(C);
        }

        public Builder(final MatrixStore<Double> Q, final MatrixStore<Double> C) {

            super();

            this.objective(Q, C);
        }

        protected Builder(final ConvexSolver.Builder matrices) {

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
                this.inequalities(matrices.getAI().get(), matrices.getBI());
            }
        }

        Builder(final MatrixStore<Double>[] matrices) {

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

        @Override
        public int countConstraints() {
            return this.countEqualityConstraints() + this.countInequalityConstraints();
        }

        public int countEqualityConstraints() {
            return (int) ((this.getAE() != null) ? this.getAE().countRows() : 0);
        }

        public int countInequalityConstraints() {
            return (int) ((this.getAI() != null) ? this.getAI().countRows() : 0);
        }

        @Override
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

        public ConvexSolver.Builder equalities(final MatrixStore<Double> mtrxAE, final MatrixStore<Double> mtrxBE) {

            ProgrammingError.throwIfNull(mtrxAE, mtrxBE);
            ProgrammingError.throwIfNotEqualRowDimensions(mtrxAE, mtrxBE);

            myAE = mtrxAE;
            myBE = mtrxBE;

            return this;
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
        /**
         * @return
         */
        public RowsSupplier<Double> getAI() {
            return myAI;
        }

        public SparseArray<Double> getAI(final int row) {
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

        /**
         * Linear objective: [C]
         */
        public MatrixStore<Double> getC() {
            return myC;
        }

        /**
         * Quadratic objective: [Q]
         */
        public PhysicalStore<Double> getQ() {
            return myQ;
        }

        public boolean hasEqualityConstraints() {
            return (myAE != null) && (myAE.countRows() > 0);
        }

        public boolean hasInequalityConstraints() {
            return (myAI != null) && (myAI.countRows() > 0);
        }

        public boolean hasObjective() {
            return (myQ != null) || (myC != null);
        }

        public ConvexSolver.Builder inequalities(final Access2D<Double> mtrxAI, final MatrixStore<Double> mtrxBI) {

            ProgrammingError.throwIfNull(mtrxAI, mtrxBI);
            ProgrammingError.throwIfNotEqualRowDimensions(mtrxAI, mtrxBI);

            if (mtrxAI instanceof RowsSupplier) {

                myAI = (RowsSupplier<Double>) mtrxAI;

            } else {

                myAI = FACTORY.makeRowsSupplier((int) mtrxAI.countColumns());
                myAI.addRows((int) mtrxAI.countRows());

                if (mtrxAI instanceof SparseStore) {

                    ((SparseStore<Double>) mtrxAI).nonzeros().forEach(nz -> myAI.getRow((int) nz.row()).set((int) nz.column(), nz.doubleValue()));

                } else {

                    double value;
                    for (int i = 0; i < mtrxAI.countRows(); i++) {
                        final SparseArray<Double> tmpRow = myAI.getRow(i);
                        for (int j = 0; j < mtrxAI.countColumns(); j++) {
                            value = mtrxAI.doubleValue(i, j);
                            if (!NC.isZero(value)) {
                                tmpRow.set(j, value);
                            }
                        }
                    }
                }
            }

            myBI = mtrxBI;

            return this;
        }

        public Builder objective(final MatrixStore<Double> mtrxC) {

            ProgrammingError.throwIfNull(mtrxC);

            myC = mtrxC;

            return this;
        }

        public Builder objective(final MatrixStore<Double> mtrxQ, final MatrixStore<Double> mtrxC) {

            ProgrammingError.throwIfNull(mtrxQ);

            if (mtrxQ instanceof PhysicalStore) {
                myQ = (PhysicalStore<Double>) mtrxQ;
            } else {
                myQ = mtrxQ.copy();
            }

            myC = mtrxC != null ? mtrxC : MatrixStore.PRIMITIVE.makeZero((int) mtrxQ.countRows(), 1).get();

            return this;
        }

        public void reset() {
            myAE = null;
            myAI = null;
            myBE = null;
            myBI = null;
            myC = null;
            myQ = null;
        }

        @Override
        public String toString() {

            final String simpleName = this.getClass().getSimpleName();

            final StringBuilder retVal = new StringBuilder("<" + simpleName + ">");

            retVal.append("\n[AE] = " + (myAE != null ? PrimitiveMatrix.FACTORY.copy(this.getAE()) : "?"));

            retVal.append("\n[BE] = " + (myBE != null ? PrimitiveMatrix.FACTORY.copy(this.getBE()) : "?"));

            retVal.append("\n[Q] = " + (myQ != null ? PrimitiveMatrix.FACTORY.copy(this.getQ()) : "?"));

            retVal.append("\n[C] = " + (myC != null ? PrimitiveMatrix.FACTORY.copy(this.getC()) : "?"));

            retVal.append("\n[AI] = " + (myAI != null ? PrimitiveMatrix.FACTORY.copy(this.getAI()) : "?"));

            retVal.append("\n[BI] = " + (myBI != null ? PrimitiveMatrix.FACTORY.copy(this.getBI()) : "?"));

            retVal.append("\n</" + simpleName + ">");

            return retVal.toString();
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

        @Override
        protected ConvexSolver doBuild(final Optimisation.Options options) {

            this.validate();

            if (this.hasInequalityConstraints()) {
                return new IterativeASS(this, options);
            } else if (this.hasEqualityConstraints()) {
                return new QPESolver(this, options);
            } else {
                return new UnconstrainedSolver(this, options);
            }
        }

    }

    public static final class ModelIntegration extends ExpressionsBasedModel.Integration<ConvexSolver> {

        public ConvexSolver build(final ExpressionsBasedModel model) {

            final ConvexSolver.Builder tmpBuilder = ConvexSolver.getBuilder();

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

    static final Factory<Double, PrimitiveDenseStore> FACTORY = PrimitiveDenseStore.FACTORY;

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

            final SparseStore<Double> mtrxAE = SparseStore.PRIMITIVE.make(numbEqExpr, numbVars);
            final PhysicalStore<Double> mtrxBE = FACTORY.makeZero(numbEqExpr, 1);

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
            mtrxQ = FACTORY.makeZero(numbVars, numbVars);

            final BinaryFunction<Double> tmpBaseFunc = sourceModel.isMaximisation() ? SUBTRACT : ADD;
            UnaryFunction<Double> tmpModifier;
            for (final IntRowColumn tmpKey : tmpObjExpr.getQuadraticKeySet()) {
                final int tmpRow = sourceModel.indexOfFreeVariable(tmpKey.row);
                final int tmpColumn = sourceModel.indexOfFreeVariable(tmpKey.column);
                if ((tmpRow >= 0) && (tmpColumn >= 0)) {
                    tmpModifier = tmpBaseFunc.second(tmpObjExpr.getAdjustedQuadraticFactor(tmpKey));
                    mtrxQ.modifyOne(tmpRow, tmpColumn, tmpModifier);
                    mtrxQ.modifyOne(tmpColumn, tmpRow, tmpModifier);
                }
            }
        }

        PhysicalStore<Double> mtrxC = null;
        if (tmpObjExpr.isAnyLinearFactorNonZero()) {
            mtrxC = FACTORY.makeZero(numbVars, 1);
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

        destinationBuilder.objective(mtrxQ, mtrxC);

        // AI & BI

        final List<Expression> tmpUpExpr = sourceModel.constraints()
                .filter((e) -> e.isUpperConstraint() && !e.isAnyQuadraticFactorNonZero()).collect(Collectors.toList());
        final int numbUpExpr = tmpUpExpr.size();

        final List<Variable> tmpUpVar = sourceModel.bounds().filter((final Variable c4) -> c4.isUpperConstraint()).collect(Collectors.toList());
        final int numbUpVar = tmpUpVar.size();

        final List<Expression> tmpLoExpr = sourceModel.constraints()
                .filter((final Expression c1) -> c1.isLowerConstraint() && !c1.isAnyQuadraticFactorNonZero()).collect(Collectors.toList());
        final int numbLoExpr = tmpLoExpr.size();

        final List<Variable> tmpLoVar = sourceModel.bounds().filter((final Variable c3) -> c3.isLowerConstraint()).collect(Collectors.toList());
        final int numbLoVar = tmpLoVar.size();

        if ((numbUpExpr + numbUpVar + numbLoExpr + numbLoVar) > 0) {


            final RowsSupplier<Double> mtrxAI = FACTORY.makeRowsSupplier(numbVars);
            final PhysicalStore<Double> mtrxBI = FACTORY.makeZero(numbUpExpr + numbUpVar + numbLoExpr + numbLoVar, 1);

            if (numbUpExpr > 0) {
                for (int i = 0; i < numbUpExpr; i++) {
                    SparseArray<Double> rowAI = mtrxAI.addRow();
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
                    SparseArray<Double> rowAI = mtrxAI.addRow();
                    final Variable tmpVariable = tmpUpVar.get(i);
                    rowAI.set(sourceModel.indexOfFreeVariable(tmpVariable), tmpVariable.getAdjustmentFactor());
                    mtrxBI.set(numbUpExpr + i, 0, tmpVariable.getAdjustedUpperLimit());
                }
            }

            if (numbLoExpr > 0) {
                for (int i = 0; i < numbLoExpr; i++) {
                    SparseArray<Double> rowAI = mtrxAI.addRow();
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
                    SparseArray<Double> rowAI = mtrxAI.addRow();
                    final Variable tmpVariable = tmpLoVar.get(i);
                    rowAI.set(sourceModel.indexOfFreeVariable(tmpVariable), -tmpVariable.getAdjustmentFactor());
                    mtrxBI.set(numbUpExpr + numbUpVar + numbLoExpr + i, 0, -tmpVariable.getAdjustedLowerLimit());
                }
            }

            destinationBuilder.inequalities(mtrxAI, mtrxBI);
        }
    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public static Builder getBuilder(final MatrixStore<Double> Q, final MatrixStore<Double> C) {
        return ConvexSolver.getBuilder().objective(Q, C);
    }

    private final ConvexSolver.Builder myMatrices;
    private final PrimitiveDenseStore mySolutionX;
    private final LU<Double> mySolverGeneral;
    private final Cholesky<Double> mySolverQ;

    @SuppressWarnings("unused")
    private ConvexSolver(final Options solverOptions) {
        this(null, solverOptions);
    }

    protected ConvexSolver(final ConvexSolver.Builder matrices, final Optimisation.Options solverOptions) {

        super(solverOptions);

        myMatrices = matrices;

        mySolutionX = FACTORY.makeZero(this.countVariables(), 1L);

        mySolverQ = Cholesky.make(this.getMatrixQ());
        mySolverGeneral = LU.make(this.getMatrixQ());
    }

    public void dispose() {

        super.dispose();

        myMatrices.reset();
    }

    public final Optimisation.Result solve(final Optimisation.Result kickStarter) {

        boolean ok = true;

        if (options.validate) {
            ok = this.validate();
        }

        if (ok) {
            ok = this.initialise(kickStarter);
        }

        if (ok) {

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

    protected boolean computeGeneral(final Collectable<Double, ? super PhysicalStore<Double>> matrix) {
        return mySolverGeneral.compute(matrix);
    }

    protected boolean computeQ(final Collectable<Double, ? super PhysicalStore<Double>> matrix) {
        return mySolverQ.compute(matrix);
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

    protected RowsSupplier<Double> getMatrixAI() {
        return myMatrices.getAI();
    }

    /**
     * @param row
     * @return
     */
    protected SparseArray<Double> getMatrixAI(final int row) {
        return myMatrices.getAI().getRow(row);
    }

    protected RowsSupplier<Double> getMatrixAI(final int[] rows) {
        return myMatrices.getAI().selectRows(rows);
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
        return mySolverGeneral.getRank();
    }

    protected MatrixStore<Double> getSE() {
        return this.getSolutionX().premultiply(this.getMatrixAE()).operateOnMatching(this.getMatrixBE(), SUBTRACT).get();
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

    protected boolean initialise(final Result kickStarter) {

        this.computeQ(this.getMatrixQ());

        return true;
    }

    protected boolean isSolvableGeneral() {
        return mySolverGeneral.isSolvable();
    }

    protected boolean isSolvableQ() {
        return mySolverQ.isSolvable();
    }

    protected abstract boolean needsAnotherIteration();

    abstract protected void performIteration();

    protected boolean solveFullKKT(final PhysicalStore<Double> preallocated) {
        if (this.computeGeneral(this.getIterationKKT())) {
            this.getSolutionGeneral(this.getIterationRHS(), preallocated);
            return true;
        } else {
            if (this.isDebug()) {
                options.logger_appender.println("KKT system unsolvable!");
                options.logger_appender.printmtrx("KKT", this.getIterationKKT().collect(FACTORY));
                options.logger_appender.printmtrx("RHS", this.getIterationRHS().collect(FACTORY));
            }
            return false;
        }
    }

    protected Optimisation.Result solveLP() {
        return LinearSolver.solve(myMatrices, options);
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
    protected boolean validate() {

        final MatrixStore<Double> tmpQ = this.getMatrixQ();
        final MatrixStore<Double> tmpC = this.getMatrixC();

        if ((tmpQ == null) || (tmpC == null)) {
            throw new IllegalArgumentException("Neither Q nor C may be null!");
        }

        if (!MatrixUtils.isHermitian(tmpQ)) {
            if (this.isDebug()) {
                this.log("Q not symmetric!", tmpQ);
            }
            throw new IllegalArgumentException("Q must be symmetric!");
        }

        if (!mySolverQ.isSPD()) {
            // Not symmetric positive definite. Check if at least positive semidefinite.

            final Eigenvalue<Double> tmpEvD = Eigenvalue.PRIMITIVE.make(true);

            tmpEvD.computeValuesOnly(tmpQ);

            final Array1D<ComplexNumber> tmpEigenvalues = tmpEvD.getEigenvalues();

            tmpEvD.reset();

            for (final ComplexNumber tmpValue : tmpEigenvalues) {
                if ((tmpValue.doubleValue() < ZERO) || !tmpValue.isReal()) {
                    if (this.isDebug()) {
                        this.log("Q not positive semidefinite!");
                        this.log("The eigenvalues are: {}", tmpEigenvalues);
                    }
                    throw new IllegalArgumentException("Q must be positive semidefinite!");
                }
            }
        }

        this.setState(State.VALID);
        return true;
    }

    void supplySlackI(final PhysicalStore<Double> slack) {

        final RowsSupplier<Double> mtrxAI = myMatrices.getAI();
        final MatrixStore<Double> mtrxBI = this.getMatrixBI();
        final PhysicalStore<Double> mtrxX = this.getSolutionX();

        slack.fillMatching(mtrxBI);

        for (int i = 0; i < mtrxAI.countRows(); i++) {
            slack.add(i, -mtrxAI.getRow(i).dot(mtrxX));
        }
    }

    @Override
    public boolean update(Variable variable) {

        boolean retVal = true;

        if (variable.isLowerLimitSet()) {
            retVal = false;
        }

        if (variable.isUpperLimitSet()) {
            retVal = false;
        }

        return retVal;
    }


}

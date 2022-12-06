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
package org.ojalgo.optimisation.convex;

import static org.ojalgo.function.constant.PrimitiveMath.*;

import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.ojalgo.ProgrammingError;
import org.ojalgo.array.Array1D;
import org.ojalgo.array.ArrayR064;
import org.ojalgo.array.SparseArray;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.matrix.decomposition.Cholesky;
import org.ojalgo.matrix.decomposition.Eigenvalue;
import org.ojalgo.matrix.decomposition.LU;
import org.ojalgo.matrix.decomposition.MatrixDecomposition;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PhysicalStore.Factory;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.matrix.store.RowsSupplier;
import org.ojalgo.matrix.store.SparseStore;
import org.ojalgo.matrix.task.iterative.ConjugateGradientSolver;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.GenericSolver;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.OptimisationData;
import org.ojalgo.optimisation.UpdatableSolver;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.optimisation.linear.LinearSolver;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Access2D.Collectable;
import org.ojalgo.structure.Structure1D.IntIndex;
import org.ojalgo.structure.Structure2D;
import org.ojalgo.structure.Structure2D.IntRowColumn;
import org.ojalgo.type.context.NumberContext;

/**
 * ConvexSolver solves optimisation problems of the form:
 * <p>
 * min 1/2 [X]<sup>T</sup>[Q][X] - [C]<sup>T</sup>[X]<br>
 * when [AE][X] == [BE]<br>
 * and [AI][X] <= [BI]
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

        Builder(final int nbVariables) {
            super();
            this.setNumberOfVariables(nbVariables);
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
        public Builder inequalities(final Access2D<?> mtrxAI, final Access1D<?> mtrxBI) {
            return super.inequalities(mtrxAI, mtrxBI);
        }

        @Override
        public Builder inequality(final double rhs, final double... factors) {
            return super.inequality(rhs, factors);
        }

        /**
         * Set the linear part of the objective function
         */
        public Builder linear(final Access1D<?> factors) {
            this.getObjective().linear().fillMatching(factors);
            return this;
        }

        /**
         * Set the linear part of the objective function
         */
        public Builder linear(final double... factors) {
            this.getObjective().linear().fillMatching(FACTORY.column(factors));
            return this;
        }

        /**
         * Set one element of the linear part of the objective function
         */
        public Builder objective(final int index, final double value) {
            this.getObjective().linear().set(index, value);
            return this;
        }

        /**
         * Set one element of the quadratic part of the objective function
         */
        public Builder objective(final int row, final int col, final double value) {
            this.getObjective().quadratic().set(row, col, value);
            return this;
        }

        /**
         * @deprecated v50 Use {@link #objective(MatrixStore, MatrixStore)} instead, or build a
         *             {@link LinearSolver}.
         */
        @Deprecated
        public Builder objective(final MatrixStore<Double> mtrxC) {
            this.setObjective(ConvexSolver.toObjectiveFunction(null, mtrxC));
            return this;
        }

        public Builder objective(final MatrixStore<Double> mtrxQ, final MatrixStore<Double> mtrxC) {
            this.setObjective(ConvexSolver.toObjectiveFunction(mtrxQ, mtrxC));
            return this;
        }

        /**
         * Set the quadratic part of the objective function
         */
        public Builder quadratic(final Access2D<?> factors) {
            this.getObjective().quadratic().fillMatching(factors);
            return this;
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

            MatrixStore<Double> rhs = Primitive64Store.FACTORY.makeZero(nbVars, 1);

            if (nbEqus > 0) {

                MatrixStore<Double> transpAE = mtrxAE.transpose();

                if (nbIneq > 0) {

                    retVal.objective(mtrxBE.below(mtrxBE.negate()).below(mtrxBI));

                    retVal.equalities(transpAE.right(transpAE.negate()).right(mtrxAI.transpose()), rhs);

                } else {

                    retVal.objective(mtrxBE.below(mtrxBE.negate()));

                    retVal.equalities(transpAE.right(transpAE.negate()), rhs);
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
         * Approximate at origin (0.0 vector)
         *
         * @see #toLinearApproximation(Access1D)
         */
        public LinearSolver.GeneralBuilder toLinearApproximation() {
            return this.toLinearApproximation(ArrayR064.make(this.countVariables()));
        }

        /**
         * Linearise the objective function (at the specified point) and duplicate all variables to handle the
         * (potential) positive and negative parts separately.
         */
        public LinearSolver.GeneralBuilder toLinearApproximation(final Access1D<Double> point) {

            MatrixStore<Double> mtrxC = this.getObjective().toFirstOrderApproximation(point).getLinearFactors(false);

            MatrixStore<Double> mtrxAE = this.getAE();
            MatrixStore<Double> mtrxBE = this.getBE();

            MatrixStore<Double> mtrxAI = this.getAI();
            MatrixStore<Double> mtrxBI = this.getBI();

            LinearSolver.GeneralBuilder retVal = LinearSolver.newGeneralBuilder();

            retVal.objective(mtrxC.below(mtrxC.negate()));

            if (mtrxAE != null && mtrxBE != null) {
                retVal.equalities(mtrxAE.right(mtrxAE.negate()), mtrxBE);
            }

            if (mtrxAI != null && mtrxBI != null) {
                retVal.inequalities(mtrxAI.right(mtrxAI.negate()), mtrxBI);
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

            if (this.countInequalityConstraints() > 0) {
                if (options.sparse == null || options.sparse.booleanValue()) {
                    return new IterativeASS(this, options);
                } else {
                    return new DirectASS(this, options);
                }
            } else if (this.countEqualityConstraints() > 0) {
                return new QPESolver(this, options);
            } else {
                return new UnconstrainedSolver(this, options);
            }
        }

        /**
         * Linear objective: [C]
         */
        @Override
        protected PhysicalStore<Double> getC() {
            return this.getObjective().linear();
        }

        protected ConvexObjectiveFunction getObjective() {
            ConvexObjectiveFunction retVal = this.getObjective(ConvexObjectiveFunction.class);
            if (retVal == null) {
                int nbVariables = this.countVariables();
                Primitive64Store mtrxQ = FACTORY.make(nbVariables, nbVariables);
                Primitive64Store mtrxC = FACTORY.make(nbVariables, 1);
                retVal = new ConvexObjectiveFunction(mtrxQ, mtrxC);
                super.setObjective(retVal);
            }
            return retVal;
        }

        @Override
        protected OptimisationData getOptimisationData() {
            return super.getOptimisationData();
        }

        /**
         * Quadratic objective: [Q]
         */
        protected PhysicalStore<Double> getQ() {
            return this.getObjective().quadratic();
        }

    }

    public static final class Configuration {

        private NumberContext myIterative = NumberContext.of(10, 14).withMode(RoundingMode.HALF_DOWN);
        private double mySmallDiagonal = RELATIVELY_SMALL + MACHINE_EPSILON;
        private Function<Structure2D, MatrixDecomposition.Solver<Double>> mySolverGeneral = LU.R064::make;
        private Function<Structure2D, MatrixDecomposition.Solver<Double>> mySolverSPD = Cholesky.R064::make;

        public NumberContext iterative() {
            return myIterative;
        }

        /**
         * The accuracy of the iterative Schur complement solver used in {@link IterativeASS}. This is the
         * step that calculates the Lagrange multipliers (dual variables). The iterative solver used is a
         * {@link ConjugateGradientSolver}.
         */
        public Configuration iterative(final NumberContext accuracy) {
            Objects.requireNonNull(accuracy);
            myIterative = accuracy;
            return this;
        }

        public MatrixDecomposition.Solver<Double> newSolverGeneral(final Structure2D structure) {
            return mySolverGeneral.apply(structure);
        }

        public MatrixDecomposition.Solver<Double> newSolverSPD(final Structure2D structure) {
            return mySolverSPD.apply(structure);
        }

        public double smallDiagonal() {
            return mySmallDiagonal;
        }

        /**
         * The [Q] matrix (of quadratic terms) is "inverted" using a matrix decomposition returned by
         * {@link #newSolverSPD(Structure2D)}. If, after decomposition,
         * {@link MatrixDecomposition.Solver#isSolvable()} returns false a small constant is added to the
         * diagonal.
         * <p>
         * The small constant will be the largest absolute element times this small diagonal factor.
         * <p>
         * This is only meant to handle minor, unexpected, deficiencies.
         */
        public Configuration smallDiagonal(final double factor) {
            mySmallDiagonal = factor;
            return this;
        }

        /**
         * This matrix decomposition should be able to "invert" the full KKT systsem body matrix (which is
         * symmetric) and/or its Schur complement with regards to the [Q] matrix (of quadratic terms).
         */
        public Configuration solverGeneral(final Function<Structure2D, MatrixDecomposition.Solver<Double>> factory) {
            mySolverGeneral = factory;
            return this;
        }

        /**
         * The [Q] matrix (of quadratic terms) is supposed to be symmetric positive definite (or at least
         * semidefinite), but in reality there are usually many deficiencies. This matrix decomposition should
         * handle "inverting" the [Q] matrix.
         */
        public Configuration solverSPD(final Function<Structure2D, MatrixDecomposition.Solver<Double>> factory) {
            mySolverSPD = factory;
            return this;
        }

    }

    public static final class ModelIntegration extends ExpressionsBasedModel.Integration<ConvexSolver> {

        public ConvexSolver build(final ExpressionsBasedModel model) {

            ConvexSolver.Builder builder = ConvexSolver.newBuilder();

            ConvexSolver.copy(model, builder);

            return builder.build(model.options);
        }

        public boolean isCapable(final ExpressionsBasedModel model) {
            return !model.isAnyVariableInteger() && model.isAnyObjectiveQuadratic() && !model.isAnyConstraintQuadratic();
        }

        @Override
        protected boolean isSolutionMapped() {
            return true;
        }

    }

    public static final ModelIntegration INTEGRATION = new ModelIntegration();

    private static final String Q_NOT_POSITIVE_SEMIDEFINITE = "Q not positive semidefinite!";
    private static final String Q_NOT_SYMMETRIC = "Q not symmetric!";

    static final Factory<Double, Primitive64Store> MATRIX_FACTORY = Primitive64Store.FACTORY;

    public static void copy(final ExpressionsBasedModel sourceModel, final ConvexSolver.Builder destinationBuilder) {

        destinationBuilder.reset();

        List<Variable> freeVariables = sourceModel.getFreeVariables();
        Set<IntIndex> fixedVariables = sourceModel.getFixedVariables();

        int nbVariables = freeVariables.size();

        // AE & BE

        List<Expression> tmpEqExpr = sourceModel.constraints().filter((final Expression c) -> c.isEqualityConstraint() && !c.isAnyQuadraticFactorNonZero())
                .collect(Collectors.toList());
        int nbEqExpr = tmpEqExpr.size();

        if (nbEqExpr > 0) {

            SparseStore<Double> mtrxAE = SparseStore.PRIMITIVE64.make(nbEqExpr, nbVariables);
            PhysicalStore<Double> mtrxBE = Primitive64Store.FACTORY.make(nbEqExpr, 1);

            for (int i = 0; i < nbEqExpr; i++) {

                Expression expression = tmpEqExpr.get(i).compensate(fixedVariables);

                for (IntIndex key : expression.getLinearKeySet()) {
                    mtrxAE.set(i, sourceModel.indexOfFreeVariable(key.index), expression.doubleValue(key, true));
                }

                mtrxBE.set(i, 0, expression.getUpperLimit(true, Double.POSITIVE_INFINITY));
            }

            destinationBuilder.equalities(mtrxAE, mtrxBE);
        }

        // Q & C

        Expression tmpObjExpr = sourceModel.objective().compensate(fixedVariables);
        boolean max = sourceModel.getOptimisationSense() == Optimisation.Sense.MAX;

        PhysicalStore<Double> mtrxQ = null;
        if (tmpObjExpr.isAnyQuadraticFactorNonZero()) {
            mtrxQ = Primitive64Store.FACTORY.make(nbVariables, nbVariables);

            for (IntRowColumn key : tmpObjExpr.getQuadraticKeySet()) {
                int row = sourceModel.indexOfFreeVariable(key.row);
                int col = sourceModel.indexOfFreeVariable(key.column);

                double factor = max ? -tmpObjExpr.doubleValue(key, true) : tmpObjExpr.doubleValue(key, true);

                mtrxQ.add(row, col, factor);
                mtrxQ.add(col, row, factor);
            }
        }

        PhysicalStore<Double> mtrxC = null;
        if (tmpObjExpr.isAnyLinearFactorNonZero()) {
            mtrxC = Primitive64Store.FACTORY.make(nbVariables, 1);
            if (max) {
                for (IntIndex key : tmpObjExpr.getLinearKeySet()) {
                    mtrxC.set(sourceModel.indexOfFreeVariable(key.index), 0, tmpObjExpr.doubleValue(key, true));
                }
            } else {
                for (IntIndex key : tmpObjExpr.getLinearKeySet()) {
                    mtrxC.set(sourceModel.indexOfFreeVariable(key.index), 0, -tmpObjExpr.doubleValue(key, true));
                }
            }
        }

        if (mtrxQ == null && mtrxC == null) {
            // In some very rare case the model was verified to be a quadratic
            // problem, but then the presolver eliminated/fixed all variables
            // part of the objective function - then we would end up here.
            // Rather than always having to do very expensive checks we simply
            // generate a well-behaved objective function here.
            mtrxQ = Primitive64Store.FACTORY.makeEye(nbVariables, nbVariables);
        }

        destinationBuilder.objective(mtrxQ, mtrxC);

        // AI & BI

        List<Expression> tmpUpExpr = sourceModel.constraints().filter(e -> e.isUpperConstraint() && !e.isAnyQuadraticFactorNonZero())
                .collect(Collectors.toList());
        int nbUpExpr = tmpUpExpr.size();

        List<Variable> tmpUpVar = sourceModel.bounds().filter((final Variable c4) -> c4.isUpperConstraint()).collect(Collectors.toList());
        int nbUpVar = tmpUpVar.size();

        List<Expression> tmpLoExpr = sourceModel.constraints().filter((final Expression c1) -> c1.isLowerConstraint() && !c1.isAnyQuadraticFactorNonZero())
                .collect(Collectors.toList());
        int nbLoExpr = tmpLoExpr.size();

        List<Variable> tmpLoVar = sourceModel.bounds().filter((final Variable c3) -> c3.isLowerConstraint()).collect(Collectors.toList());
        int nbLoVar = tmpLoVar.size();

        if (nbUpExpr + nbUpVar + nbLoExpr + nbLoVar > 0) {

            RowsSupplier<Double> mtrxAI = Primitive64Store.FACTORY.makeRowsSupplier(nbVariables);
            PhysicalStore<Double> mtrxBI = Primitive64Store.FACTORY.make(nbUpExpr + nbUpVar + nbLoExpr + nbLoVar, 1);

            if (nbUpExpr > 0) {
                for (int i = 0; i < nbUpExpr; i++) {
                    SparseArray<Double> rowAI = mtrxAI.addRow();
                    Expression expression = tmpUpExpr.get(i).compensate(fixedVariables);
                    for (IntIndex key : expression.getLinearKeySet()) {
                        rowAI.set(sourceModel.indexOfFreeVariable(key.index), expression.doubleValue(key, true));
                    }
                    mtrxBI.set(i, 0, expression.getUpperLimit(true, Double.POSITIVE_INFINITY));
                }
            }

            if (nbUpVar > 0) {
                for (int i = 0; i < nbUpVar; i++) {
                    SparseArray<Double> rowAI = mtrxAI.addRow();
                    Variable variable = tmpUpVar.get(i);
                    rowAI.set(sourceModel.indexOfFreeVariable(variable), ONE);
                    mtrxBI.set(nbUpExpr + i, 0, variable.getUpperLimit(false, Double.POSITIVE_INFINITY));
                }
            }

            if (nbLoExpr > 0) {
                for (int i = 0; i < nbLoExpr; i++) {
                    SparseArray<Double> rowAI = mtrxAI.addRow();
                    Expression expression = tmpLoExpr.get(i).compensate(fixedVariables);
                    for (IntIndex key : expression.getLinearKeySet()) {
                        rowAI.set(sourceModel.indexOfFreeVariable(key.index), -expression.doubleValue(key, true));
                    }
                    mtrxBI.set(nbUpExpr + nbUpVar + i, 0, -expression.getLowerLimit(true, Double.NEGATIVE_INFINITY));
                }
            }

            if (nbLoVar > 0) {
                for (int i = 0; i < nbLoVar; i++) {
                    SparseArray<Double> rowAI = mtrxAI.addRow();
                    Variable variable = tmpLoVar.get(i);
                    rowAI.set(sourceModel.indexOfFreeVariable(variable), NEG);
                    mtrxBI.set(nbUpExpr + nbUpVar + nbLoExpr + i, 0, -variable.getLowerLimit(false, Double.NEGATIVE_INFINITY));
                }
            }

            destinationBuilder.inequalities(mtrxAI, mtrxBI);
        }
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilder(final Access2D<?> quadratic) {
        ConvexSolver.Builder retVal = new ConvexSolver.Builder(quadratic.getMinDim());
        retVal.quadratic(quadratic);
        return retVal;
    }

    public static Builder newBuilder(final int nbVariables) {
        return new ConvexSolver.Builder(nbVariables);
    }

    static ConvexSolver.Builder builder(final MatrixStore<Double>[] matrices) {
        return new ConvexSolver.Builder(matrices);
    }

    static ConvexSolver of(final MatrixStore<Double>[] matrices) {
        return ConvexSolver.builder(matrices).build();
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

    private final OptimisationData myMatrices;
    private boolean myPatchedQ = false;
    private final Primitive64Store mySolutionX;
    private final MatrixDecomposition.Solver<Double> mySolverGeneral;
    private final MatrixDecomposition.Solver<Double> mySolverQ;
    private boolean myZeroQ = false;

    ConvexSolver(final ConvexSolver.Builder convexSolverBuilder, final Optimisation.Options optimisationOptions) {

        super(optimisationOptions);

        myMatrices = convexSolverBuilder.getOptimisationData();

        mySolutionX = MATRIX_FACTORY.make(this.countVariables(), 1L);

        PhysicalStore<Double> mtrxQ = this.getMatrixQ();
        Configuration convexOptions = optimisationOptions.convex();
        mySolverQ = convexOptions.newSolverSPD(mtrxQ);
        mySolverGeneral = convexOptions.newSolverGeneral(mtrxQ);
    }

    public void dispose() {

        super.dispose();

        myMatrices.reset();
    }

    public UpdatableSolver.EntityMap getEntityMap() {
        return null;
    }

    public Optimisation.Result solve(final Optimisation.Result kickStarter) {

        if (this.initialise(kickStarter)) {

            this.resetIterationsCount();

            if (this.isIteratingPossible()) {

                do {

                    this.performIteration();

                } while (this.isIterationAllowed() && this.needsAnotherIteration());
            }
        }

        return this.buildResult();
    }

    @Override
    public String toString() {
        return myMatrices.toString();
    }

    protected Optimisation.Result buildResult() {

        Access1D<?> solution = this.extractSolution();
        double value = this.evaluateFunction(solution);
        Optimisation.State state = this.getState();

        return new Optimisation.Result(state, value, solution);
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

    protected double evaluateFunction(final Access1D<?> solution) {

        MatrixStore<Double> tmpX = this.getSolutionX();

        return tmpX.transpose().multiply(this.getMatrixQ().multiply(tmpX)).multiply(0.5).subtract(tmpX.transpose().multiply(this.getMatrixC())).doubleValue(0L);
    }

    protected MatrixStore<Double> extractSolution() {
        return this.getSolutionX().copy();
    }

    protected abstract Collectable<Double, ? super PhysicalStore<Double>> getIterationKKT();

    protected abstract Collectable<Double, ? super PhysicalStore<Double>> getIterationRHS();

    protected MatrixStore<Double> getMatrixAE() {
        return myMatrices.getAE();
    }

    protected SparseArray<Double> getMatrixAE(final int row) {
        return myMatrices.getAE(row);
    }

    protected RowsSupplier<Double> getMatrixAE(final int[] rows) {
        return myMatrices.getAE(rows);
    }

    protected MatrixStore<Double> getMatrixAI() {
        return myMatrices.getAI();
    }

    protected SparseArray<Double> getMatrixAI(final int row) {
        return myMatrices.getAI(row);
    }

    protected RowsSupplier<Double> getMatrixAI(final int[] rows) {
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
        return myMatrices.getBI().rows(selector);
    }

    protected MatrixStore<Double> getMatrixC() {
        return myMatrices.getObjective(ConvexObjectiveFunction.class).linear();
    }

    protected PhysicalStore<Double> getMatrixQ() {
        return myMatrices.getObjective(ConvexObjectiveFunction.class).quadratic();
    }

    protected int getRankGeneral() {
        if (mySolverGeneral instanceof MatrixDecomposition.RankRevealing) {
            return ((MatrixDecomposition.RankRevealing<?>) mySolverGeneral).getRank();
        } else if (mySolverGeneral.isSolvable()) {
            return mySolverGeneral.getColDim();
        } else {
            return 0;
        }
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
        return myMatrices.countEqualityConstraints() > 0;
    }

    protected boolean hasInequalityConstraints() {
        return myMatrices.countInequalityConstraints() > 0;
    }

    /**
     * @return true/false if the main algorithm may start or not
     */
    protected boolean initialise(final Result kickStarter) {

        PhysicalStore<Double> matrixQ = this.getMatrixQ();
        this.setState(State.VALID);

        boolean symmetric = true;
        if (options.validate && !matrixQ.isHermitian()) {

            symmetric = false;
            this.setState(State.INVALID);

            if (!this.isLogDebug()) {
                throw new IllegalArgumentException(Q_NOT_SYMMETRIC);
            }
            this.log(Q_NOT_SYMMETRIC, matrixQ);
        }

        myPatchedQ = false;
        myZeroQ = false;
        if (!mySolverQ.compute(matrixQ)) {
            double largest = matrixQ.aggregateAll(Aggregator.LARGEST).doubleValue();
            double small = options.convex().smallDiagonal();
            if (largest > small) {
                matrixQ.modifyDiagonal(ADD.by(small * largest));
                mySolverQ.compute(matrixQ);
                myPatchedQ = true;
            } else {
                myZeroQ = true;
            }
        }

        boolean semidefinite = true;
        if (options.validate && !mySolverQ.isSolvable()) {
            // Not symmetric positive definite. Check if at least positive semidefinite.

            Eigenvalue<Double> decompEvD = Eigenvalue.PRIMITIVE.make(matrixQ, true);
            decompEvD.computeValuesOnly(matrixQ);
            Array1D<ComplexNumber> eigenvalues = decompEvD.getEigenvalues();
            decompEvD.reset();

            for (ComplexNumber eigval : eigenvalues) {
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

    protected boolean isIteratingPossible() {
        return true;
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
            this.log("KKT system unsolvable!");
            this.log("KKT", this.getIterationKKT().collect(Primitive64Store.FACTORY));
            this.log("RHS", this.getIterationRHS().collect(Primitive64Store.FACTORY));
        }
        return false;
    }

    /**
     * The LP result with a {@link State} suitable for this solver – most likely {@link State#FEASIBLE}. IF
     * the LP was solved to optimality but the Q matrix (or the entire objective function) was disregarded
     * then the returned state will just be {@link State#FEASIBLE}.
     */
    protected Optimisation.Result solveLP() {

        Result resultLP = LinearSolver.solve(myMatrices, options, !myZeroQ);

        if (!myZeroQ && resultLP.getState().isFeasible()) {
            return resultLP.withState(State.FEASIBLE);
        }

        return resultLP;
    }

    boolean isPatchedQ() {
        return myPatchedQ;
    }

    boolean isZeroQ() {
        return myZeroQ;
    }

}

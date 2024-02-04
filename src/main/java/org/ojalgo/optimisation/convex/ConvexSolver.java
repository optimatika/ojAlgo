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
package org.ojalgo.optimisation.convex;

import static org.ojalgo.function.constant.PrimitiveMath.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.ojalgo.array.ArrayR064;
import org.ojalgo.array.SparseArray;
import org.ojalgo.array.SparseArray.NonzeroView;
import org.ojalgo.function.constant.BigMath;
import org.ojalgo.matrix.decomposition.Cholesky;
import org.ojalgo.matrix.decomposition.LU;
import org.ojalgo.matrix.decomposition.MatrixDecomposition;
import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.matrix.store.RowsSupplier;
import org.ojalgo.matrix.store.SparseStore;
import org.ojalgo.matrix.task.iterative.ConjugateGradientSolver;
import org.ojalgo.optimisation.ConstraintsMap;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.GenericSolver;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.optimisation.linear.LinearSolver;
import org.ojalgo.scalar.Quadruple;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
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
public abstract class ConvexSolver extends GenericSolver {

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
        public Builder equalities(final Access2D<?> mtrxAE, final Access1D<?> mtrxBE) {
            return super.equalities(mtrxAE, mtrxBE);
        }

        @Override
        public Builder equality(final double rhs, final double... factors) {
            return super.equality(rhs, factors);
        }

        @Override
        public ConvexObjectiveFunction<Double> getObjective() {
            ConvexObjectiveFunction<Double> retVal = this.getObjective(ConvexObjectiveFunction.class);
            if (retVal == null) {
                int nbVariables = this.countVariables();
                PhysicalStore<Double> mtrxQ = this.getFactory().make(nbVariables, nbVariables);
                PhysicalStore<Double> mtrxC = this.getFactory().make(nbVariables, 1);
                retVal = new ConvexObjectiveFunction<>(mtrxQ, mtrxC);
                super.setObjective(retVal);
            }
            return retVal;
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
            this.getObjective().linear().fillMatching(this.getFactory().column(factors));
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
        public Builder objective(final MatrixStore<?> mtrxC) {
            this.setObjective(BasePrimitiveSolver.toObjectiveFunction(null, mtrxC));
            return this;
        }

        public Builder objective(final MatrixStore<?> mtrxQ, final MatrixStore<?> mtrxC) {
            this.setObjective(BasePrimitiveSolver.toObjectiveFunction(mtrxQ, mtrxC));
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

            if (options.convex().isExtendedPrecision()) {

                ConvexData<Quadruple> data = this.getConvexData(GenericStore.R128);
                return new IterativeRefinementSolver(options, data);

            } else {

                ConvexData<Double> data = this.getConvexData(Primitive64Store.FACTORY);
                return BasePrimitiveSolver.newSolver(data, options);
            }
        }

        /**
         * Linear objective: [C]
         */
        @Override
        protected PhysicalStore<Double> getC() {
            return this.getObjective().linear();
        }

        protected <N extends Comparable<N>> ConvexData<N> getConvexData(final PhysicalStore.Factory<N, ?> factory) {

            int nbVars = this.countVariables();
            int nbEqus = this.countEqualityConstraints();
            int nbIneq = this.countInequalityConstraints();

            ConvexData<N> retVal = new ConvexData<>(false, factory, nbVars, nbEqus, nbIneq);

            retVal.getObjective().linear().fillMatching(this.getObjective().linear());
            retVal.getObjective().quadratic().fillMatching(this.getObjective().quadratic());

            for (int i = 0; i < nbEqus; i++) {
                for (NonzeroView<Double> nz : this.getAE(i).nonzeros()) {
                    retVal.setAE(i, (int) nz.index(), nz.doubleValue());
                }
                retVal.setBE(i, this.getBE(i));
            }

            for (int i = 0; i < nbIneq; i++) {
                for (NonzeroView<Double> nz : this.getAI(i).nonzeros()) {
                    retVal.setAI(i, (int) nz.index(), nz.doubleValue());
                }
                retVal.setBI(i, this.getBI(i));
            }

            return retVal;
        }

        /**
         * Quadratic objective: [Q]
         */
        protected PhysicalStore<Double> getQ() {
            return this.getObjective().quadratic();
        }

    }

    public static final class Configuration {

        private boolean myExtendedPrecision = false;
        private NumberContext myIterative = NumberContext.of(10, 14).withMode(RoundingMode.HALF_DOWN);
        private double mySmallDiagonal = RELATIVELY_SMALL + MACHINE_EPSILON;
        private Function<Structure2D, MatrixDecomposition.Solver<Double>> mySolverGeneral = LU.R064::make;
        private Function<Structure2D, MatrixDecomposition.Solver<Double>> mySolverSPD = Cholesky.R064::make;

        /**
         * With extended precision the usual solver is wrapped by a master algorithm, implemented in
         * {@link Quadruple} precision, that iteratively refines (zoom and shift) the problem to be solved by
         * the delegate solver. This enables to handle constraints with very high accuracy.
         * <p>
         * The iterative refinement solver cannot handle general inequality constraints, only simple variable
         * bounds (modelled as inequality constraints).
         * <p>
         * This is an experimental feature!
         */
        public Configuration extendedPrecision(final boolean extendedPrecision) {
            myExtendedPrecision = extendedPrecision;
            return this;
        }

        public boolean isExtendedPrecision() {
            return myExtendedPrecision;
        }

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

        @Override
        public ConvexSolver build(final ExpressionsBasedModel model) {

            Options options = model.options;

            if (options.convex().isExtendedPrecision()) {

                ConvexData<Quadruple> data = ConvexSolver.copy(model, GenericStore.R128);
                IterativeRefinementSolver solver = new IterativeRefinementSolver(options, data);

                if (model.options.validate) {
                    solver.setValidator(this.newValidator(model));
                }

                return solver;

            } else {

                ConvexData<Double> data = ConvexSolver.copy(model, Primitive64Store.FACTORY);
                BasePrimitiveSolver solver = BasePrimitiveSolver.newSolver(data, options);

                if (model.options.validate) {
                    solver.setValidator(this.newValidator(model));
                }

                return solver;
            }
        }

        @Override
        public boolean isCapable(final ExpressionsBasedModel model) {
            return !model.isAnyVariableInteger() && model.isAnyObjectiveQuadratic() && !model.isAnyConstraintQuadratic();
        }

        @Override
        public Result toModelState(final Result solverState, final ExpressionsBasedModel model) {

            List<Variable> freeVariables = model.getFreeVariables();
            Set<IntIndex> fixedVariables = model.getFixedVariables();
            int nbFreeVars = freeVariables.size();
            int nbModelVars = model.countVariables();

            ArrayR064 modelSolution = ArrayR064.make(nbModelVars);

            for (int i = 0; i < nbFreeVars; i++) {
                modelSolution.set(model.indexOf(freeVariables.get(i)), solverState.doubleValue(i));
            }

            for (IntIndex fixed : fixedVariables) {
                modelSolution.set(fixed.index, model.getVariable(fixed.index).getValue());
            }

            return solverState.withSolution(modelSolution);
        }

        @Override
        public Result toSolverState(final Result modelState, final ExpressionsBasedModel model) {

            List<Variable> freeVariables = model.getFreeVariables();
            int nbFreeVars = freeVariables.size();

            ArrayR064 solverSolution = ArrayR064.make(nbFreeVars);

            for (int i = 0; i < nbFreeVars; i++) {
                Variable variable = freeVariables.get(i);
                int modelIndex = model.indexOf(variable);
                solverSolution.set(i, modelState.doubleValue(modelIndex));
            }

            return modelState.withSolution(solverSolution);
        }

    }

    public static final ModelIntegration INTEGRATION = new ModelIntegration();

    public static void copy(final ExpressionsBasedModel sourceModel, final Builder destinationBuilder) {

        destinationBuilder.reset();

        List<Variable> freeVariables = sourceModel.getFreeVariables();
        Set<IntIndex> fixedVariables = sourceModel.getFixedVariables();

        int nbVariables = freeVariables.size();

        // AE & BE

        List<Expression> tmpEqExpr = sourceModel.constraints().filter((final Expression c) -> c.isEqualityConstraint() && !c.isAnyQuadraticFactorNonZero())
                .collect(Collectors.toList());
        int nbEqExpr = tmpEqExpr.size();

        if (nbEqExpr > 0) {

            SparseStore<Double> mtrxAE = SparseStore.R064.make(nbEqExpr, nbVariables);
            PhysicalStore<Double> mtrxBE = Primitive64Store.FACTORY.make(nbEqExpr, 1);

            for (int i = 0; i < nbEqExpr; i++) {

                Expression expression = tmpEqExpr.get(i).compensate(fixedVariables);

                for (IntIndex key : expression.getLinearKeySet()) {
                    mtrxAE.set(i, sourceModel.indexOfFreeVariable(key.index), expression.get(key, true));
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

                BigDecimal factor = max ? tmpObjExpr.get(key, true).negate() : tmpObjExpr.get(key, true);

                mtrxQ.add(row, col, factor);
                mtrxQ.add(col, row, factor);
            }
        }

        PhysicalStore<Double> mtrxC = null;
        if (tmpObjExpr.isAnyLinearFactorNonZero()) {
            mtrxC = Primitive64Store.FACTORY.make(nbVariables, 1);
            if (max) {
                for (IntIndex key : tmpObjExpr.getLinearKeySet()) {
                    mtrxC.set(sourceModel.indexOfFreeVariable(key.index), 0, tmpObjExpr.get(key, true));
                }
            } else {
                for (IntIndex key : tmpObjExpr.getLinearKeySet()) {
                    mtrxC.set(sourceModel.indexOfFreeVariable(key.index), 0, tmpObjExpr.get(key, true).negate());
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

    public static <N extends Comparable<N>> ConvexData<N> copy(final ExpressionsBasedModel sourceModel, final PhysicalStore.Factory<N, ?> factory) {

        List<Variable> freeVariables = sourceModel.getFreeVariables();
        Set<IntIndex> fixedVariables = sourceModel.getFixedVariables();

        int nbVariables = freeVariables.size();

        List<Expression> tmpEqExpr = sourceModel.constraints().filter((final Expression c) -> c.isEqualityConstraint() && !c.isAnyQuadraticFactorNonZero())
                .collect(Collectors.toList());
        int nbEqExpr = tmpEqExpr.size();

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

        // ConvexData<N> retVal = dataFactory.newInstance(nbVariables, nbEqExpr, nbUpExpr + nbUpVar + nbLoExpr + nbLoVar);
        ConvexData<N> retVal = new ConvexData<>(true, factory, nbVariables, nbEqExpr, nbUpExpr + nbUpVar + nbLoExpr + nbLoVar);

        ConstraintsMap constraintsMap = retVal.getConstraintsMap();

        // Q & C

        Expression tmpObjExpr = sourceModel.objective().compensate(fixedVariables);
        boolean max = sourceModel.getOptimisationSense() == Optimisation.Sense.MAX;
        boolean didSet = false;

        if (tmpObjExpr.isAnyQuadraticFactorNonZero()) {

            for (IntRowColumn key : tmpObjExpr.getQuadraticKeySet()) {
                int row = sourceModel.indexOfFreeVariable(key.row);
                int col = sourceModel.indexOfFreeVariable(key.column);

                BigDecimal factor = max ? tmpObjExpr.get(key, true).negate() : tmpObjExpr.get(key, true);

                retVal.addObjective(row, col, factor);
                retVal.addObjective(col, row, factor);
                didSet = true;
            }
        }

        if (tmpObjExpr.isAnyLinearFactorNonZero()) {

            if (max) {
                for (IntIndex key : tmpObjExpr.getLinearKeySet()) {
                    retVal.setObjective(sourceModel.indexOfFreeVariable(key.index), tmpObjExpr.get(key, true));
                    didSet = true;
                }
            } else {
                for (IntIndex key : tmpObjExpr.getLinearKeySet()) {
                    retVal.setObjective(sourceModel.indexOfFreeVariable(key.index), tmpObjExpr.get(key, true).negate());
                    didSet = true;
                }
            }
        }

        if (!didSet) {
            // In some very rare case the model was verified to be a quadratic
            // problem, but then the presolver eliminated/fixed all variables
            // part of the objective function - then we would end up here.
            // Rather than always having to do very expensive checks we simply
            // generate a well-behaved objective function here.
            for (int ij = 0; ij < nbVariables; ij++) {
                retVal.setObjective(ij, ij, BigMath.ONE);
            }
        }

        // AE & BE

        for (int i = 0; i < nbEqExpr; i++) {

            Expression expression = tmpEqExpr.get(i).compensate(fixedVariables);

            for (IntIndex key : expression.getLinearKeySet()) {
                retVal.setAE(i, sourceModel.indexOfFreeVariable(key.index), expression.get(key, true));
            }

            retVal.setBE(i, expression.getUpperLimit(true, BigMath.SMALLEST_POSITIVE_INFINITY));

            constraintsMap.setEntry(i, expression, ConstraintType.EQUALITY, false);
        }

        // AI & BI

        int base = 0;

        for (int i = 0; i < nbUpExpr; i++) {
            Expression expression = tmpUpExpr.get(i).compensate(fixedVariables);
            for (IntIndex key : expression.getLinearKeySet()) {
                retVal.setAI(base + i, sourceModel.indexOfFreeVariable(key.index), expression.get(key, true));
            }
            retVal.setBI(base + i, expression.getUpperLimit(true, BigMath.SMALLEST_POSITIVE_INFINITY));
            constraintsMap.setEntry(nbEqExpr + base + i, expression, ConstraintType.UPPER, false);
        }

        base += nbUpExpr;

        for (int i = 0; i < nbUpVar; i++) {
            Variable variable = tmpUpVar.get(i);
            retVal.setAI(base + i, sourceModel.indexOfFreeVariable(variable), ONE);
            retVal.setBI(base + i, variable.getUpperLimit(false, BigMath.SMALLEST_POSITIVE_INFINITY));
            constraintsMap.setEntry(nbEqExpr + base + i, variable, ConstraintType.UPPER, false);
        }

        base += nbUpVar;

        for (int i = 0; i < nbLoExpr; i++) {
            Expression expression = tmpLoExpr.get(i).compensate(fixedVariables);
            for (IntIndex key : expression.getLinearKeySet()) {
                retVal.setAI(base + i, sourceModel.indexOfFreeVariable(key.index), expression.get(key, true).negate());
            }
            retVal.setBI(base + i, expression.getLowerLimit(true, BigMath.SMALLEST_NEGATIVE_INFINITY).negate());
            constraintsMap.setEntry(nbEqExpr + base + i, expression, ConstraintType.UPPER, true);
        }

        base += nbLoExpr;

        for (int i = 0; i < nbLoVar; i++) {
            Variable variable = tmpLoVar.get(i);
            retVal.setAI(base + i, sourceModel.indexOfFreeVariable(variable), NEG);
            retVal.setBI(base + i, variable.getLowerLimit(false, BigMath.SMALLEST_NEGATIVE_INFINITY).negate());
            constraintsMap.setEntry(nbEqExpr + base + i, variable, ConstraintType.UPPER, true);
        }

        base += nbLoVar;

        return retVal;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilder(final Access2D<?> quadratic) {
        Builder retVal = new Builder(quadratic.getMinDim());
        retVal.quadratic(quadratic);
        return retVal;
    }

    public static Builder newBuilder(final int nbVariables) {
        return new Builder(nbVariables);
    }

    protected ConvexSolver(final Options solverOptions) {
        super(solverOptions);
    }

}

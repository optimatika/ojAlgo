/*
 * Copyright 1997-2025 Optimatika
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
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.ojalgo.array.ArrayR064;
import org.ojalgo.array.SparseArray.NonzeroView;
import org.ojalgo.function.constant.BigMath;
import org.ojalgo.matrix.decomposition.Cholesky;
import org.ojalgo.matrix.decomposition.LU;
import org.ojalgo.matrix.decomposition.MatrixDecomposition;
import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064CSC;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.matrix.task.iterative.ConjugateGradientSolver;
import org.ojalgo.matrix.task.iterative.IterativeSolverTask;
import org.ojalgo.matrix.task.iterative.Preconditioner;
import org.ojalgo.matrix.task.iterative.SSORPreconditioner;
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

    public enum Algorithm {

        /**
         * The original ojAlgo QP-solver. Mature and stable, but sometimes struggles with larger models.
         */
        ACTIVE_SET,
        /**
         * Typically faster, except for very small models, but not as stable/mature.
         */
        ADMM;

    }

    public static final class Builder extends GenericSolver.Builder<ConvexSolver.Builder, ConvexSolver> {

        Builder() {
            super();
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
                    this.objective(null, matrices[3]);
                }
            }

            if (matrices.length >= 6 && matrices[4] != null && matrices[5] != null) {
                this.inequalities(matrices[4], matrices[5]);
            }
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
        public LinearSolver.Builder toFeasibilityChecker() {

            MatrixStore<Double> mtrxAE = this.getAE();
            MatrixStore<Double> mtrxBE = this.getBE();

            MatrixStore<Double> mtrxAI = this.getAI();
            MatrixStore<Double> mtrxBI = this.getBI();

            LinearSolver.Builder retVal = LinearSolver.newBuilder();

            int nbEqus = this.countEqualityConstraints();
            int nbIneq = this.countInequalityConstraints();
            int nbVars = this.countVariables();

            MatrixStore<Double> rhs = R064Store.FACTORY.makeZero(nbVars, 1);

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
        public LinearSolver.Builder toLinearApproximation() {
            return this.toLinearApproximation(ArrayR064.make(this.countVariables()));
        }

        /**
         * Linearise the objective function (at the specified point) and duplicate all variables to handle the
         * (potential) positive and negative parts separately.
         */
        public LinearSolver.Builder toLinearApproximation(final Access1D<Double> point) {

            MatrixStore<Double> mtrxC = this.getObjective().toFirstOrderApproximation(point).getLinearFactors(false);

            MatrixStore<Double> mtrxAE = this.getAE();
            MatrixStore<Double> mtrxBE = this.getBE();

            MatrixStore<Double> mtrxAI = this.getAI();
            MatrixStore<Double> mtrxBI = this.getBI();

            LinearSolver.Builder retVal = LinearSolver.newBuilder();

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

                if (options.experimental) {
                    AlternatingDirectionSolver.Problem problem = this.getADProblem();
                    return new AlternatingDirectionSolver(problem, options);
                } else {
                    ConvexData<Double> data = this.getConvexData(R064Store.FACTORY);
                    return BasePrimitiveSolver.newSolver(data, options);
                }
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

        <N extends Comparable<N>> AlternatingDirectionSolver.Problem getADProblem() {

            int nbVars = this.countVariables();
            int nbEqus = this.countEqualityConstraints();
            int nbIneq = this.countInequalityConstraints();

            int m = nbEqus + nbIneq;
            int n = nbVars;

            R064CSC.Builder P = R064CSC.newBuilder(n, n);
            double[] q = new double[n];
            R064CSC.Builder A = R064CSC.newBuilder(m, n);
            double[] l = new double[m];
            double[] u = new double[m];

            PhysicalStore<Double> quadratic = this.getObjective().quadratic();
            PhysicalStore<Double> linear = this.getObjective().linear();

            for (int j = 0; j < n; j++) {
                q[j] = -linear.doubleValue(j);
                for (int i = 0; i <= j; i++) {
                    P.set(i, j, quadratic.doubleValue(i, j));
                }
            }

            for (int i = 0; i < nbEqus; i++) {
                for (NonzeroView<Double> nz : this.getAE(i).nonzeros()) {
                    A.set(i, (int) nz.index(), nz.doubleValue());
                }
                double be = this.getBE(i);
                l[i] = be;
                u[i] = be;
            }

            for (int i = 0; i < nbIneq; i++) {
                int row = nbEqus + i;
                for (NonzeroView<Double> nz : this.getAI(i).nonzeros()) {
                    A.set(row, (int) nz.index(), nz.doubleValue());
                }
                l[row] = Double.NEGATIVE_INFINITY;
                u[row] = this.getBI(i);
            }

            return new AlternatingDirectionSolver.Problem(P, q, A, l, u);
        }

    }

    public static final class Configuration {

        private Algorithm myAlgorithm = null;
        private boolean myCombinedScaleFactor = true;
        private boolean myExtendedPrecision = false;
        private NumberContext myIterativeAccuracy = NumberContext.of(10, 16).withMode(RoundingMode.HALF_DOWN);
        private Supplier<Preconditioner> myIterativePreconditioner = SSORPreconditioner::new;
        private Supplier<IterativeSolverTask> myIterativeSolver = ConjugateGradientSolver::new;
        private Boolean myProjection = null;
        private double mySmallDiagonal = RELATIVELY_SMALL + MACHINE_EPSILON;
        private Function<Structure2D, MatrixDecomposition.Solver<Double>> mySolverGeneral = LU.R064::make;
        private Function<Structure2D, MatrixDecomposition.Solver<Double>> mySolverSPD = Cholesky.R064::make;

        public Configuration algorithm(final Algorithm algorithm) {
            myAlgorithm = algorithm;
            return this;
        }

        /**
         * Only relevant with extended precision. With the extended precision solver the primal and dual
         * variables are scaled (shift and zoom) to iteratively generate subproblems. In theory there are
         * different scaling factors for the primal and dual variables, but forcing them to be the same
         * enables simplifications resulting in significant performance gains.
         * <p>
         * The default is to use the same scaling factor for both primal and dual variables. By setting this
         * to false, you switch to a slower more complex, but theoretically more accurate and flexible
         * algorithm.
         *
         * @see #extendedPrecision(boolean)
         */
        public Configuration combinedScaleFactor(final boolean combinedScaleFactor) {
            myCombinedScaleFactor = combinedScaleFactor;
            return this;
        }

        /**
         * With extended precision the usual solver is wrapped by a master algorithm, implemented in
         * {@link Quadruple} precision, that iteratively refines (zoom and shift) the problem to be solved by
         * the delegate solver. This enables to handle constraints with very high accuracy.
         * <p>
         * The iterative refinement solver cannot handle general inequality constraints, only simple variable
         * bounds (modelled as inequality constraints).
         * <p>
         * This is an experimental feature!
         * <p>
         * Setting this to true you should most likely also set the {@link Optimisation.Options#solution} to
         * something matching that allows for higher precision.
         */
        public Configuration extendedPrecision(final boolean extendedPrecision) {
            myExtendedPrecision = extendedPrecision;
            return this;
        }

        public boolean isCombinedScaleFactor() {
            return myCombinedScaleFactor;
        }

        public boolean isExtendedPrecision() {
            return myExtendedPrecision;
        }

        /**
         * @deprecated Since v56 It's applied for you when calling {@link #newIterativeSolver(int)}
         */
        @Deprecated
        public NumberContext iterative() {
            return myIterativeAccuracy;
        }

        /**
         * The accuracy of the iterative Schur complement solver used in {@link IterativeASS}. This is the
         * step that calculates the Lagrange multipliers (dual variables). The iterative solver used is a
         * {@link ConjugateGradientSolver}.
         */
        public Configuration iterative(final NumberContext accuracy) {
            Objects.requireNonNull(accuracy);
            myIterativeAccuracy = accuracy;
            return this;
        }

        /**
         * Select which iterative linear system solver to use for the Schur-complement step in IterativeASS.
         * Default is {@link ConjugateGradientSolver}. You may set e.g. new {@code QMRSolver()}.
         */
        public Configuration iterative(final Supplier<IterativeSolverTask> solver, final NumberContext accuracy) {
            Objects.requireNonNull(solver);
            Objects.requireNonNull(accuracy);
            myIterativeAccuracy = accuracy;
            myIterativeSolver = solver;
            return this;
        }

        public Configuration iterative(final Supplier<IterativeSolverTask> solver, final Supplier<Preconditioner> preconditioner) {
            Objects.requireNonNull(solver);
            Objects.requireNonNull(preconditioner);
            myIterativeSolver = solver;
            myIterativePreconditioner = preconditioner;
            return this;
        }

        public Configuration iterative(final Supplier<IterativeSolverTask> solver, final Supplier<Preconditioner> preconditioner,
                final NumberContext accuracy) {
            Objects.requireNonNull(solver);
            Objects.requireNonNull(preconditioner);
            Objects.requireNonNull(accuracy);
            myIterativeSolver = solver;
            myIterativePreconditioner = preconditioner;
            myIterativeAccuracy = accuracy;
            return this;
        }

        /**
         * Returns a new iterative solver instance configured with the current accuracy, maximum iterations,
         * and preconditioner settings.
         */
        public IterativeSolverTask newIterativeSolver(final int maxIterations) {
            IterativeSolverTask retVal = myIterativeSolver.get();
            retVal.configurator().accuracy(myIterativeAccuracy).iterations(maxIterations).preconditioner(myIterativePreconditioner.get());
            return retVal;
        }

        public MatrixDecomposition.Solver<Double> newSolverGeneral(final Structure2D structure) {
            return mySolverGeneral.apply(structure);
        }

        public MatrixDecomposition.Solver<Double> newSolverSPD(final Structure2D structure) {
            return mySolverSPD.apply(structure);
        }

        /**
         * Null-Space projection. (Eliminating equality constraints and reducing the number of variables.)
         * <p>
         * TRUE means yes, FALSE no, and NULL auto. Even if configured to TRUE there must also be both
         * equality and inequality constraints for this to actually be used.
         */
        public Configuration projection(final Boolean projection) {
            myProjection = projection;
            return this;
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

        Algorithm getAlgorithm() {
            return myAlgorithm;
        }

        Boolean getProjection() {
            return myProjection;
        }

    }

    public static final class ModelIntegration extends ExpressionsBasedModel.Integration<ConvexSolver> {

        @Override
        public ConvexSolver build(final ExpressionsBasedModel model) {

            Algorithm algorithm = model.options.convex().getAlgorithm();

            if (algorithm == null) {
                int n = model.countVariables();
                int m = model.countExpressions();
                if (m > 50 * n) {
                    algorithm = Algorithm.ACTIVE_SET;
                } else if (n > 100 * m) {
                    algorithm = Algorithm.ADMM;
                } else if (m + n < 750) {
                    algorithm = Algorithm.ACTIVE_SET;
                } else {
                    algorithm = Algorithm.ADMM;
                }
            }

            if (algorithm == Algorithm.ACTIVE_SET) {
                ExpressionsBasedModel.Integration.setSwitch(model, ExpressionsBasedModel.IntegrationProperty.ACTIVE_SET_OR_ADMM, false);
                return BasePrimitiveSolver.INTEGRATION.build(model);
            } else if (algorithm == Algorithm.ADMM) {
                ExpressionsBasedModel.Integration.setSwitch(model, ExpressionsBasedModel.IntegrationProperty.ACTIVE_SET_OR_ADMM, true);
                return AlternatingDirectionSolver.INTEGRATION.build(model);
            } else {
                throw new IllegalStateException("Unknown algorithm: " + algorithm);
            }
        }

        @Override
        public boolean isCapable(final ExpressionsBasedModel model) {
            return BasePrimitiveSolver.INTEGRATION.isCapable(model) || AlternatingDirectionSolver.INTEGRATION.isCapable(model);
        }

        @Override
        public Result toModelState(final Result solverState, final ExpressionsBasedModel model) {
            if (ExpressionsBasedModel.Integration.isSwitch(model, ExpressionsBasedModel.IntegrationProperty.ACTIVE_SET_OR_ADMM)) {
                return AlternatingDirectionSolver.INTEGRATION.toModelState(solverState, model);
            } else {
                return BasePrimitiveSolver.INTEGRATION.toModelState(solverState, model);
            }
        }

        @Override
        public Result toSolverState(final Result modelState, final ExpressionsBasedModel model) {
            if (ExpressionsBasedModel.Integration.isSwitch(model, ExpressionsBasedModel.IntegrationProperty.ACTIVE_SET_OR_ADMM)) {
                return AlternatingDirectionSolver.INTEGRATION.toSolverState(modelState, model);
            } else {
                return BasePrimitiveSolver.INTEGRATION.toSolverState(modelState, model);
            }
        }

        @Override
        protected int getIndexInSolver(final ExpressionsBasedModel model, final Variable variable) {
            return ExpressionsBasedModel.Integration.getIndexOfFreeInSolver(model, variable);
        }

        @Override
        protected Optimisation.Sense getSolverSense() {
            return Optimisation.Sense.MIN;
        }

    }

    public static final ExpressionsBasedModel.Integration<ConvexSolver> INTEGRATION = new ModelIntegration();

    /**
     * Should variable lower/upper bounds be read in their numerically-adjusted form? Bound-as-constraint
     * rows in {@code [AI]} pair a unit body coefficient ({@code ONE}/{@code NEG}) with the variable's
     * bound used as RHS — so the bound is naturally read in model units to stay consistent with the
     * unit-valued row.
     */
    private static final boolean ADJUSTED_BOUNDS = false;
    /**
     * Should constraint coefficients and right-hand-sides be read in their numerically-adjusted form? Row
     * scaling improves the conditioning of {@code [AE]} / {@code [AI]}.
     */
    private static final boolean ADJUSTED_CONSTRAINTS = true;
    /**
     * Should the objective's linear and quadratic coefficients be read in their numerically-adjusted form?
     * Consistent scaling of {@code Q} and {@code C} matches the constraint scaling and keeps KKT residuals
     * within tolerance.
     */
    private static final boolean ADJUSTED_OBJECTIVE = true;

    public static <N extends Comparable<N>> ConvexData<N> copy(final ExpressionsBasedModel model, final PhysicalStore.Factory<N, ?> factory) {

        List<Variable> freeVariables = model.getFreeVariables();
        Set<IntIndex> fixedVariables = model.getFixedVariables();

        int nbVariables = freeVariables.size();

        List<Expression> tmpEqExpr = model.constraints().filter((final Expression c) -> c.isEqualityConstraint() && !c.isAnyQuadraticFactorNonZero())
                .collect(Collectors.toList());
        int nbEqExpr = tmpEqExpr.size();

        List<Expression> tmpUpExpr = model.constraints().filter(e -> e.isUpperConstraint() && !e.isAnyQuadraticFactorNonZero()).collect(Collectors.toList());
        int nbUpExpr = tmpUpExpr.size();

        List<Variable> tmpUpVar = model.bounds().filter((final Variable c4) -> c4.isUpperConstraint()).collect(Collectors.toList());
        int nbUpVar = tmpUpVar.size();

        List<Expression> tmpLoExpr = model.constraints().filter((final Expression c1) -> c1.isLowerConstraint() && !c1.isAnyQuadraticFactorNonZero())
                .collect(Collectors.toList());
        int nbLoExpr = tmpLoExpr.size();

        List<Variable> tmpLoVar = model.bounds().filter((final Variable c3) -> c3.isLowerConstraint()).collect(Collectors.toList());
        int nbLoVar = tmpLoVar.size();

        ConvexData<N> retVal = new ConvexData<>(true, factory, nbVariables, nbEqExpr, nbUpExpr + nbUpVar + nbLoExpr + nbLoVar);

        // Variables

        for (int i = 0; i < nbVariables; i++) {
            retVal.setVariableIndices(i, model.indexOf(freeVariables.get(i)));
        }

        // Q & C

        Expression tmpObjExpr = model.objective().compensate(fixedVariables);
        boolean max = model.getOptimisationSense() == Optimisation.Sense.MAX;
        boolean didSet = false;

        if (tmpObjExpr.isAnyQuadraticFactorNonZero()) {

            for (IntRowColumn key : tmpObjExpr.getQuadraticKeySet()) {
                int row = model.indexOfFreeVariable(key.row);
                int col = model.indexOfFreeVariable(key.column);

                BigDecimal factor = max ? tmpObjExpr.get(key, ADJUSTED_OBJECTIVE).negate() : tmpObjExpr.get(key, ADJUSTED_OBJECTIVE);

                retVal.addObjective(row, col, factor);
                retVal.addObjective(col, row, factor);
                didSet = true;
            }
        }

        if (tmpObjExpr.isAnyLinearFactorNonZero()) {

            if (max) {
                for (IntIndex key : tmpObjExpr.getLinearKeySet()) {
                    retVal.setObjective(model.indexOfFreeVariable(key.index), tmpObjExpr.get(key, ADJUSTED_OBJECTIVE));
                    didSet = true;
                }
            } else {
                for (IntIndex key : tmpObjExpr.getLinearKeySet()) {
                    retVal.setObjective(model.indexOfFreeVariable(key.index), tmpObjExpr.get(key, ADJUSTED_OBJECTIVE).negate());
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
                retVal.setAE(i, model.indexOfFreeVariable(key.index), expression.get(key, ADJUSTED_CONSTRAINTS));
            }

            retVal.setBE(i, expression, ConstraintType.EQUALITY, expression.getUpperLimit(ADJUSTED_CONSTRAINTS, BigMath.SMALLEST_POSITIVE_INFINITY), false);

            // constraintsMap.setEntry(i, expression, ConstraintType.EQUALITY, false);
        }

        // AI & BI

        int base = 0;

        for (int i = 0; i < nbUpExpr; i++) {
            Expression expression = tmpUpExpr.get(i).compensate(fixedVariables);
            for (IntIndex key : expression.getLinearKeySet()) {
                retVal.setAI(base + i, model.indexOfFreeVariable(key.index), expression.get(key, ADJUSTED_CONSTRAINTS));
            }
            retVal.setBI(base + i, expression, ConstraintType.UPPER, expression.getUpperLimit(ADJUSTED_CONSTRAINTS, BigMath.SMALLEST_POSITIVE_INFINITY), false);
        }
        base += nbUpExpr;

        for (int i = 0; i < nbUpVar; i++) {
            Variable variable = tmpUpVar.get(i);
            retVal.setAI(base + i, model.indexOfFreeVariable(variable), ONE);
            retVal.setBI(base + i, variable, ConstraintType.UPPER, variable.getUpperLimit(ADJUSTED_BOUNDS, BigMath.SMALLEST_POSITIVE_INFINITY), false);
        }
        base += nbUpVar;

        for (int i = 0; i < nbLoExpr; i++) {
            Expression expression = tmpLoExpr.get(i).compensate(fixedVariables);
            for (IntIndex key : expression.getLinearKeySet()) {
                retVal.setAI(base + i, model.indexOfFreeVariable(key.index), expression.get(key, ADJUSTED_CONSTRAINTS).negate());
            }
            retVal.setBI(base + i, expression, ConstraintType.LOWER, expression.getLowerLimit(ADJUSTED_CONSTRAINTS, BigMath.SMALLEST_NEGATIVE_INFINITY).negate(), true);
        }
        base += nbLoExpr;

        for (int i = 0; i < nbLoVar; i++) {
            Variable variable = tmpLoVar.get(i);
            retVal.setAI(base + i, model.indexOfFreeVariable(variable), NEG);
            retVal.setBI(base + i, variable, ConstraintType.LOWER, variable.getLowerLimit(ADJUSTED_BOUNDS, BigMath.SMALLEST_NEGATIVE_INFINITY).negate(), true);
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

    /**
     * Use {@link ModelIntegration#build(ExpressionsBasedModel)} directly instead, and then remember to also
     * use {@link ModelIntegration#toModelState(Optimisation.Result, ExpressionsBasedModel)} and
     * {@link ModelIntegration#toSolverState(Optimisation.Result, ExpressionsBasedModel)}.
     *
     * @deprecated v57 Use {@link #INTEGRATION} instead.
     */
    @Deprecated
    public static ConvexSolver newSolver(final ExpressionsBasedModel model) {
        return INTEGRATION.build(model);
    }

    protected ConvexSolver(final Optimisation.Options optimisationOptions) {
        super(optimisationOptions);
    }

}
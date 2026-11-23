/*
 * Copyright 1997-2026 Optimatika
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
import org.ojalgo.optimisation.UpdatableSolver;
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
 * <p>
 * The matrix [Q] is assumed to be symmetric (it must be made that way) and positive (semi)definite:
 * <ul>
 * <li>If [Q] is positive semidefinite, then the objective function is convex: In this case the quadratic
 * program has a global minimizer if there exists some feasible vector [X] (satisfying the constraints) and if
 * the objective function is bounded below on the feasible region.
 * <li>If [Q] is positive definite and the problem has a feasible solution, then the global minimizer is
 * unique.
 * </ul>
 * <p>
 * The general recommendation is to construct optimisation problems using {@linkplain ExpressionsBasedModel}
 * and not worry about solver details. If you do want to instantiate a convex solver directly use the
 * {@linkplain ConvexSolver.Builder} class. It will return an appropriate subclass for you.
 * <p>
 * When the KKT matrix is nonsingular, there is a unique optimal primal-dual pair (x,l). If the KKT matrix is
 * singular, but the KKT system is still solvable, any solution yields an optimal pair (x,l). If the KKT
 * system is not solvable, the quadratic optimization problem is unbounded below or infeasible.
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
        private NumberContext myIterativeAccuracy = NumberContext.of(16, 12).withMode(RoundingMode.HALF_DOWN);
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
         * <p>
         * The precision and the scale are used for different things:
         * <ul>
         * <li>The precision determines when the iterative solver stops – when the residual is small relative
         * to the right-hand side (the relative error is 10<sup>1-precision</sup>).
         * <li>The scale determines if the iterative solution is used – the relative residual has to be less
         * than 0.5*10<sup>-scale</sup>. If not, the full KKT system is solved (using a matrix decomposition)
         * instead.
         * </ul>
         * For the iterative solutions to actually be used, the precision needs to be (at least) 2 larger than
         * the scale. The default is precision 16 and scale 12 – the iterative solver aims for full (double)
         * precision, and its solution is used unless it is much less accurate than that.
         */
        public Configuration iterative(final NumberContext accuracy) {
            Objects.requireNonNull(accuracy);
            myIterativeAccuracy = accuracy;
            return this;
        }

        /**
         * Select which iterative linear system solver to use for the Schur-complement step in IterativeASS.
         * Default is {@link ConjugateGradientSolver}. You may set e.g. new {@code QMRSolver()}.
         *
         * @see #iterative(NumberContext)
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

        /**
         * @see #iterative(NumberContext)
         */
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
         * The small constant will be the largest absolute element times this small diagonal factor. If that
         * is not enough, [Q] has negative curvature, the constant is increased (4 times at a time) up to the
         * largest absolute element times the square root of this factor. If not even that makes [Q] positive
         * definite the problem is not convex, and a solution is at best {@link Optimisation.State#FEASIBLE}.
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

        /**
         * Unless an algorithm is specified, the active set solver is used for small models, and for models
         * with many more constraints than variables. ADMM is used for the others. For models that are not too
         * large, the other algorithm is used as fallback, if the chosen one ends without a solution.
         */
        @Override
        public ConvexSolver build(final ExpressionsBasedModel model) {

            Algorithm algorithm = model.options.convex().getAlgorithm();
            boolean fallback = false;

            if (algorithm == null) {

                int n = model.countVariables();
                int m = model.countExpressions();

                fallback = m <= 2_002 && n <= 2_002;

                if (m > 10 * n || m + n < 826) {
                    algorithm = Algorithm.ACTIVE_SET;
                } else {
                    algorithm = Algorithm.ADMM;
                }
            }

            if (algorithm == Algorithm.ACTIVE_SET) {

                ExpressionsBasedModel.Integration.setSwitch(model, ExpressionsBasedModel.IntegrationProperty.ACTIVE_SET_OR_ADMM, false);

                ConvexSolver solver = BasePrimitiveSolver.INTEGRATION.build(model);

                if (fallback) {

                    return new ConvexSolver.SolverWithFallback(solver, () -> {
                        ExpressionsBasedModel.Integration.setSwitch(model, ExpressionsBasedModel.IntegrationProperty.ACTIVE_SET_OR_ADMM, true);
                        return AlternatingDirectionSolver.INTEGRATION.build(model);
                    });

                } else {

                    return solver;
                }

            } else if (algorithm == Algorithm.ADMM) {

                ExpressionsBasedModel.Integration.setSwitch(model, ExpressionsBasedModel.IntegrationProperty.ACTIVE_SET_OR_ADMM, true);

                ConvexSolver solver = AlternatingDirectionSolver.INTEGRATION.build(model);

                if (fallback) {

                    return new ConvexSolver.SolverWithFallback(solver, () -> {
                        ExpressionsBasedModel.Integration.setSwitch(model, ExpressionsBasedModel.IntegrationProperty.ACTIVE_SET_OR_ADMM, false);
                        return BasePrimitiveSolver.INTEGRATION.build(model);
                    });

                } else {

                    return solver;
                }

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

    /**
     * A primary solver, with a fallback. If the primary solver ends without a solution – not feasible, and
     * not proven infeasible or unbounded – the fallback solver is generated, from the model, and used
     * instead, for this and any later solves.
     * <p>
     * Not after the primary solver has been updated in place. Some of those updates may be to the solver
     * only, not to the model, and the fallback solver would not have them.
     */
    static final class SolverWithFallback extends ConvexSolver implements UpdatableSolver {

        /**
         * A feasible solution (not necessarily optimal), or proven to be infeasible or unbounded. Note that
         * {@link Optimisation.State#isFeasible()} is also true for {@link Optimisation.State#UNBOUNDED}.
         */
        private static boolean isSolved(final Optimisation.Result result) {

            Optimisation.State state = result.getState();

            return state.isSuccess() && state.isFeasible() || state == Optimisation.State.INFEASIBLE || state == Optimisation.State.UNBOUNDED;
        }

        private ConvexSolver myFallback = null;
        private final Supplier<ConvexSolver> myGenerator;
        private final ConvexSolver mySolver;
        private boolean myUpdatedInPlace = false;

        SolverWithFallback(final ConvexSolver primary, final Supplier<ConvexSolver> secondary) {
            super(primary.options);
            mySolver = primary;
            myGenerator = secondary;
            myFallback = null;
        }

        @Override
        public void dispose() {
            super.dispose();
            mySolver.dispose();
            if (myFallback != null) {
                myFallback.dispose();
            }
        }

        @Override
        public boolean fixVariable(final int index, final double value) {
            boolean updated = this.getSolver() instanceof UpdatableSolver updatable && updatable.fixVariable(index, value);
            myUpdatedInPlace |= updated;
            return updated;
        }

        @Override
        public double getDualMultiplier(final int index) {
            return this.getSolver() instanceof UpdatableSolver updatable ? updatable.getDualMultiplier(index) : NaN;
        }

        @Override
        public Optional<ExpressionsBasedModel.EntityMap> getEntityMap() {
            return this.getSolver() instanceof UpdatableSolver updatable ? updatable.getEntityMap() : Optional.empty();
        }

        @Override
        public double getReducedGradient(final int index) {
            return this.getSolver() instanceof UpdatableSolver updatable ? updatable.getReducedGradient(index) : NaN;
        }

        @Override
        public boolean updateRange(final int index, final double lower, final double upper) {
            boolean updated = this.getSolver() instanceof UpdatableSolver updatable && updatable.updateRange(index, lower, upper);
            myUpdatedInPlace |= updated;
            return updated;
        }

        private ConvexSolver getSolver() {
            return myFallback != null ? myFallback : mySolver;
        }

        @Override
        protected Optimisation.Result doSolve(final Optimisation.Result kickStarter) {

            Optimisation.Result result = this.getSolver().solve(kickStarter);
            this.incrementIterationsCount();

            if (myFallback == null && !myUpdatedInPlace && !SolverWithFallback.isSolved(result) && this.isIterationAllowed()) {

                if (this.isLogProgress()) {
                    this.log("Primary solver ended {} – switching to the fallback solver", result.getState());
                }

                myFallback = myGenerator.get();

                result = myFallback.solve(kickStarter);
                this.incrementIterationsCount();
            }

            return result;
        }

    }

    public static final ExpressionsBasedModel.Integration<ConvexSolver> INTEGRATION = new ModelIntegration();

    /**
     * Should variable lower/upper bounds be read in their numerically-adjusted form? Bounds are modelled as
     * rows in {@code [AI]} with a single coefficient, the variable's adjustment factor if adjusted or else 1,
     * and the bound as RHS. Unit rows keep the bounds in model units.
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

    /**
     * Equality constraints that are linear combinations of the others, and consistent with them, are left
     * out. [AE] then has full row rank, and those constraints have no dual values.
     */
    public static <N extends Comparable<N>> ConvexData<N> copy(final ExpressionsBasedModel model, final PhysicalStore.Factory<N, ?> factory) {

        List<Variable> freeVariables = model.getFreeVariables();
        Set<IntIndex> fixedVariables = model.getFixedVariables();

        int nbVariables = freeVariables.size();

        List<Expression> tmpEqExpr = ConvexSolver.selectIndependent(model, nbVariables,
                model.constraints().filter((final Expression c) -> c.isEqualityConstraint() && !c.isAnyQuadraticFactorNonZero())
                        .map(c -> c.compensate(fixedVariables)).collect(Collectors.toList()));
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

        retVal.setObjectiveAdjustmentFactor(ADJUSTED_OBJECTIVE ? tmpObjExpr.getAdjustmentFactor() : ONE);

        if (tmpObjExpr.isAnyQuadraticFactorNonZero()) {

            for (IntRowColumn key : tmpObjExpr.getQuadraticKeySet()) {
                int row = model.indexOfFreeVariable(key.row);
                int col = model.indexOfFreeVariable(key.column);

                BigDecimal factor = max ? tmpObjExpr.get(key, ADJUSTED_OBJECTIVE).negate() : tmpObjExpr.get(key, ADJUSTED_OBJECTIVE);

                retVal.addObjective(row, col, factor);
                retVal.addObjective(col, row, factor);
            }
        }

        if (tmpObjExpr.isAnyLinearFactorNonZero()) {

            if (max) {
                for (IntIndex key : tmpObjExpr.getLinearKeySet()) {
                    retVal.setObjective(model.indexOfFreeVariable(key.index), tmpObjExpr.get(key, ADJUSTED_OBJECTIVE));
                }
            } else {
                for (IntIndex key : tmpObjExpr.getLinearKeySet()) {
                    retVal.setObjective(model.indexOfFreeVariable(key.index), tmpObjExpr.get(key, ADJUSTED_OBJECTIVE).negate());
                }
            }
        }

        // AE & BE

        for (int i = 0; i < nbEqExpr; i++) {

            Expression expression = tmpEqExpr.get(i);

            for (IntIndex key : expression.getLinearKeySet()) {
                retVal.setAE(i, model.indexOfFreeVariable(key.index), expression.get(key, ADJUSTED_CONSTRAINTS));
            }

            retVal.setBE(i, expression, ConstraintType.EQUALITY, expression.getUpperLimit(ADJUSTED_CONSTRAINTS, BigMath.SMALLEST_POSITIVE_INFINITY), false,
                    ADJUSTED_CONSTRAINTS);

            // constraintsMap.setEntry(i, expression, ConstraintType.EQUALITY, false);
        }

        // AI & BI

        int base = 0;

        for (int i = 0; i < nbUpExpr; i++) {
            Expression expression = tmpUpExpr.get(i).compensate(fixedVariables);
            for (IntIndex key : expression.getLinearKeySet()) {
                retVal.setAI(base + i, model.indexOfFreeVariable(key.index), expression.get(key, ADJUSTED_CONSTRAINTS));
            }
            retVal.setBI(base + i, expression, ConstraintType.UPPER, expression.getUpperLimit(ADJUSTED_CONSTRAINTS, BigMath.SMALLEST_POSITIVE_INFINITY), false,
                    ADJUSTED_CONSTRAINTS);
        }
        base += nbUpExpr;

        for (int i = 0; i < nbUpVar; i++) {
            Variable variable = tmpUpVar.get(i);
            retVal.setAI(base + i, model.indexOfFreeVariable(variable), ADJUSTED_BOUNDS ? variable.getAdjustmentFactor() : ONE);
            retVal.setBI(base + i, variable, ConstraintType.UPPER, variable.getUpperLimit(ADJUSTED_BOUNDS, BigMath.SMALLEST_POSITIVE_INFINITY), false,
                    ADJUSTED_BOUNDS);
        }
        base += nbUpVar;

        for (int i = 0; i < nbLoExpr; i++) {
            Expression expression = tmpLoExpr.get(i).compensate(fixedVariables);
            for (IntIndex key : expression.getLinearKeySet()) {
                retVal.setAI(base + i, model.indexOfFreeVariable(key.index), expression.get(key, ADJUSTED_CONSTRAINTS).negate());
            }
            retVal.setBI(base + i, expression, ConstraintType.LOWER,
                    expression.getLowerLimit(ADJUSTED_CONSTRAINTS, BigMath.SMALLEST_NEGATIVE_INFINITY).negate(), true, ADJUSTED_CONSTRAINTS);
        }
        base += nbLoExpr;

        for (int i = 0; i < nbLoVar; i++) {
            Variable variable = tmpLoVar.get(i);
            retVal.setAI(base + i, model.indexOfFreeVariable(variable), ADJUSTED_BOUNDS ? -variable.getAdjustmentFactor() : NEG);
            retVal.setBI(base + i, variable, ConstraintType.LOWER, variable.getLowerLimit(ADJUSTED_BOUNDS, BigMath.SMALLEST_NEGATIVE_INFINITY).negate(), true,
                    ADJUSTED_BOUNDS);
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

    /**
     * The equality constraints, in order, without those that are linear combinations of the ones before –
     * found with a Cholesky decomposition of [A][A]<sup>T</sup>, extended one row/column at a time. A row
     * that can't be added is [a]<sub>k</sub> = [c]<sup>T</sup>[A] with [c] =
     * ([A][A]<sup>T</sup>)<sup>-1</sup> [A][a]<sub>k</sub><sup>T</sup>, and it is consistent if b<sub>k</sub>
     * = [c]<sup>T</sup>[b]. If any such row is not consistent, the problem is infeasible, and all rows are
     * returned.
     */
    private static List<Expression> selectIndependent(final ExpressionsBasedModel model, final int nbVariables, final List<Expression> equalities) {

        int nbEqus = equalities.size();

        if (nbEqus <= 1) {
            return equalities;
        }

        int[][] indices = new int[nbEqus][];
        double[][] values = new double[nbEqus][];
        double[] rhs = new double[nbEqus];

        for (int i = 0; i < nbEqus; i++) {

            Expression expression = equalities.get(i);
            Set<IntIndex> keys = expression.getLinearKeySet();

            indices[i] = new int[keys.size()];
            values[i] = new double[keys.size()];

            int k = 0;
            for (IntIndex key : keys) {
                indices[i][k] = model.indexOfFreeVariable(key.index);
                values[i][k] = expression.get(key, ADJUSTED_CONSTRAINTS).doubleValue();
                k++;
            }

            rhs[i] = expression.getUpperLimit(ADJUSTED_CONSTRAINTS, BigMath.SMALLEST_POSITIVE_INFINITY).doubleValue();
        }

        Cholesky<Double> gram = Cholesky.R064.make(0, 0);
        int[] kept = new int[nbEqus];
        int nbKept = 0;
        boolean consistent = true;

        double[] row = new double[nbVariables];

        for (int i = 0; i < nbEqus; i++) {

            for (int k = 0; k < indices[i].length; k++) {
                row[indices[i][k]] = values[i][k];
            }

            // The new column of [A][A]^T, with the diagonal element last
            double[] column = new double[nbKept + 1];
            for (int p = 0; p <= nbKept; p++) {
                int other = p < nbKept ? kept[p] : i;
                double dot = ZERO;
                for (int k = 0; k < indices[other].length; k++) {
                    dot += values[other][k] * row[indices[other][k]];
                }
                column[p] = dot;
            }

            if (gram.appendColumn(ArrayR064.wrap(column))) {

                kept[nbKept++] = i;

            } else {

                double combination = ZERO;
                if (nbKept > 0) {
                    double[] coefficients = Arrays.copyOf(column, nbKept);
                    gram.ftran(coefficients);
                    for (int p = 0; p < nbKept; p++) {
                        combination += coefficients[p] * rhs[kept[p]];
                    }
                }

                if (model.options.feasibility.isDifferent(rhs[i], combination)) {
                    consistent = false;
                }
            }

            for (int k = 0; k < indices[i].length; k++) {
                row[indices[i][k]] = ZERO;
            }
        }

        if (nbKept == nbEqus || !consistent) {
            return equalities;
        }

        List<Expression> retVal = new ArrayList<>(nbKept);
        for (int p = 0; p < nbKept; p++) {
            retVal.add(equalities.get(kept[p]));
        }
        return retVal;
    }

    /**
     * Whether a result with this state may have dual values and a reduced gradient: only with a (possibly
     * approximate) solution, not when the state is INFEASIBLE, UNBOUNDED or a failure.
     */
    static boolean isDualMeaningful(final Optimisation.State state) {
        return state.isSuccess() && state.isApproximate();
    }

    /**
     * Whether all the values are finite. A solver may return NaN values to signal that there is no solution,
     * with a state such as {@link Optimisation.State#APPROXIMATE} that otherwise suggests there is one. Note
     * that NaN values pass {@link ExpressionsBasedModel#validate(Access1D)}.
     */
    static boolean isFinite(final Access1D<?> values) {
        for (long i = 0L, limit = values.count(); i < limit; i++) {
            if (!Double.isFinite(values.doubleValue(i))) {
                return false;
            }
        }
        return true;
    }

    protected ConvexSolver(final Optimisation.Options optimisationOptions) {
        super(optimisationOptions);
    }

}
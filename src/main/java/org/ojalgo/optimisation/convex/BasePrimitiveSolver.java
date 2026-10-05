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

import java.util.Optional;
import java.util.function.Supplier;

import org.ojalgo.ProgrammingError;
import org.ojalgo.array.Array1D;
import org.ojalgo.array.ArrayR064;
import org.ojalgo.array.ArrayR256;
import org.ojalgo.array.SparseArray;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.matrix.decomposition.Eigenvalue;
import org.ojalgo.matrix.decomposition.MatrixDecomposition;
import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PhysicalStore.Factory;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.matrix.store.RowsSupplier;
import org.ojalgo.matrix.store.TransformableRegion;
import org.ojalgo.optimisation.ConstraintsMetaData;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.UpdatableSolver;
import org.ojalgo.optimisation.linear.LinearSolver;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Quadruple;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D.Collectable;
import org.ojalgo.type.context.NumberContext;

abstract class BasePrimitiveSolver extends ConvexSolver implements UpdatableSolver {

    public static final class Integration extends ExpressionsBasedModel.Integration<ConvexSolver> {

        @Override
        public ConvexSolver build(final ExpressionsBasedModel model) {

            Options options = model.options;

            if (options.convex().isExtendedPrecision()) {

                ConvexData<Quadruple> data = ConvexSolver.copy(model, GenericStore.R128);
                return new IterativeRefinementSolver(options, data);

            } else {

                ConvexData<Double> data = ConvexSolver.copy(model, R064Store.FACTORY);

                int nbVars = data.countVariables();
                int nbEqus = data.countEqualityConstraints();
                int nbInes = data.countInequalityConstraints();

                Boolean projection = options.convex().getProjection();

                if (nbEqus > 0 && nbInes > 0 && nbEqus <= nbVars
                        && (Boolean.TRUE.equals(projection) || (projection == null && BasePrimitiveSolver.isProjectionPreferred(nbVars, nbEqus)))) {

                    return new NullSpaceASS(options, data);

                } else {

                    return BasePrimitiveSolver.newSolver(data, options);
                }
            }
        }

        @Override
        public boolean isCapable(final ExpressionsBasedModel model) {
            return !model.isAnyVariableInteger() && model.isAnyObjectiveQuadratic() && !model.isAnyConstraintQuadratic();
        }

        @Override
        public Result toModelState(final Result solverState, final ExpressionsBasedModel model) {

            if (model.options.convex().isExtendedPrecision()) {
                return ExpressionsBasedModel.Integration.expandFreeToFull(solverState, model, ArrayR256.FACTORY, solverState.getReducedGradient(),
                        this.getSolverSense());
            } else {
                return ExpressionsBasedModel.Integration.expandFreeToFull(solverState, model, ArrayR064.FACTORY, solverState.getReducedGradient(),
                        this.getSolverSense());
            }
        }

        @Override
        public Result toSolverState(final Result modelState, final ExpressionsBasedModel model) {
            return ExpressionsBasedModel.Integration.reduceFullToFree(modelState, model, ArrayR064.FACTORY);
        }

        @Override
        protected Optimisation.Sense getSolverSense() {
            return Optimisation.Sense.MIN;
        }

    }

    /**
     * Correct the solution for the error caused by patching [Q] when that error is not small compared to the
     * size of the gradient terms.
     */
    private static final NumberContext BIAS = NumberContext.of(7);
    private static final String Q_NOT_POSITIVE_SEMIDEFINITE = "Q not positive semidefinite!";
    private static final String Q_NOT_SYMMETRIC = "Q not symmetric!";

    static final Integration INTEGRATION = new Integration();
    static final Factory<Double, R064Store> MATRIX_FACTORY = R064Store.FACTORY;

    static BasePrimitiveSolver.Builder builder(final MatrixStore<Double>[] matrices) {
        return new BasePrimitiveSolver.Builder(matrices);
    }

    /**
     * The (default) choice to eliminate the equality constraints using a null space projection, when there
     * are both equality and inequality constraints. Benchmarking shows it pays off unless there are very few
     * equality constraints relative to the number of variables, or the problem is very small. The null space
     * basis is dense, and that limits the size of the problems it's applied to.
     */
    static boolean isProjectionPreferred(final int nbVars, final int nbEqus) {
        return nbVars >= 80 && nbVars <= 64 * nbEqus && (long) nbVars * (nbVars - nbEqus) <= 16_000_000L;
    }

    static BasePrimitiveSolver newSolver(final ConvexData<Double> data, final Optimisation.Options options) {

        int nbEqus = data.countEqualityConstraints();
        int nbInes = data.countInequalityConstraints();

        if (nbInes > 0) {

            if (Boolean.TRUE.equals(options.sparse)) {

                return new IterativeASS(data, options);

            } else {

                return new DirectASS(data, options);
            }

        } else if (nbEqus > 0) {
            return new QPESolver(data, options);
        } else {
            return new UnconstrainedSolver(data, options);
        }
    }

    static ConvexSolver of(final MatrixStore<Double>[] matrices) {
        return BasePrimitiveSolver.builder(matrices).build();
    }

    static ConvexObjectiveFunction<Double> toObjectiveFunction(final MatrixStore<?> mtrxQ, final MatrixStore<?> mtrxC) {

        if (mtrxQ == null && mtrxC == null) {
            ProgrammingError.throwWithMessage("Both parameters can't be null!");
        }

        R064Store tmpQ = null;
        R064Store tmpC = null;

        if (mtrxQ == null) {
            tmpQ = R064Store.FACTORY.make(mtrxC.count(), mtrxC.count());
        } else if (mtrxQ instanceof R064Store) {
            tmpQ = (R064Store) mtrxQ;
        } else {
            tmpQ = R064Store.FACTORY.copy(mtrxQ);
        }

        if (mtrxC == null) {
            tmpC = R064Store.FACTORY.make(tmpQ.countRows(), 1L);
        } else if (mtrxC instanceof R064Store) {
            tmpC = (R064Store) mtrxC;
        } else {
            tmpC = R064Store.FACTORY.copy(mtrxC);
        }

        return new ConvexObjectiveFunction<>(tmpQ, tmpC);
    }

    private transient double[] myCachedReducedGradient = null;
    /**
     * The constant added to the diagonal of [Q] (to make it positive definite), or 0.0 if [Q] was not patched
     */
    private double myDiagonalPatch = ZERO;
    private final ConvexData<Double> myMatrices;
    private final R064Store mySolutionX;
    private final MatrixDecomposition.Solver<Double> mySolverGeneral;
    private final MatrixDecomposition.Solver<Double> mySolverQ;
    private boolean myZeroQ = false;

    BasePrimitiveSolver(final ConvexData<Double> convexData, final Optimisation.Options optimisationOptions) {

        super(optimisationOptions);

        myMatrices = convexData;

        mySolutionX = MATRIX_FACTORY.make(this.countVariables(), 1L);

        PhysicalStore<Double> mtrxQ = this.getMatrixQ();
        Configuration convexOptions = optimisationOptions.convex();
        mySolverQ = convexOptions.newSolverSPD(mtrxQ);
        mySolverGeneral = convexOptions.newSolverGeneral(mtrxQ);
    }

    @Override
    public void dispose() {

        super.dispose();

        myMatrices.reset();
    }

    @Override
    public Optional<ExpressionsBasedModel.EntityMap> getEntityMap() {
        return myMatrices.isEntityMap() ? Optional.of(myMatrices) : Optional.empty();
    }

    @Override
    public double getReducedGradient(final int index) {
        if (myCachedReducedGradient == null) {
            myCachedReducedGradient = this.extractReducedGradient();
        }
        return myCachedReducedGradient[index];
    }

    @Override
    public Optimisation.Result solve(final Optimisation.Result kickStarter) {

        myCachedReducedGradient = null;

        if (this.initialise(kickStarter)) {

            this.iterate();

            if (myDiagonalPatch > ZERO && state.isFeasible() && state != State.UNBOUNDED) {
                if (this.isUnbounded()) {
                    state = State.UNBOUNDED;
                } else if (this.isBiased()) {
                    this.correctBias();
                }
            }
        }

        return this.buildResult();
    }

    @Override
    public String toString() {
        return myMatrices.toString();
    }

    /**
     * The patch, p, makes the solution, x, violate the optimality conditions of the original problem by p[x].
     * Re-solving (once) with the linear term [C] + p[x], warm started from x, is a proximal point step - its
     * solution satisfies the original optimality conditions far better. If the re-solve fails the previous
     * solution is kept.
     */
    private void correctBias() {

        PhysicalStore<Double> mtrxX = this.getSolutionX();
        PhysicalStore<Double> mtrxC = myMatrices.getObjective().linear();

        State previousState = state;
        PhysicalStore<Double> previousX = mtrxX.copy();
        PhysicalStore<Double> previousC = mtrxC.copy();
        PhysicalStore<Double> previousL = this.copyDualSolution();

        mtrxC.modifyMatching(ADD, previousX.multiply(myDiagonalPatch));

        if (this.initialise(new Optimisation.Result(previousState, previousX))) {
            this.iterate();
        }

        mtrxC.fillMatching(previousC);

        if (!state.isFeasible()) {
            state = previousState;
            mtrxX.fillMatching(previousX);
            this.restoreDualSolution(previousL);
        }
    }

    /**
     * The reduced gradient in model space. {@link #computeReducedGradient()} works with the solver's data,
     * where the objective is scaled by {@link ConvexData#getObjectiveAdjustmentFactor()}.
     */
    private double[] extractReducedGradient() {
        double[] gradient = this.computeReducedGradient();
        double objectiveScale = myMatrices.getObjectiveAdjustmentFactor();
        if (objectiveScale != ONE) {
            for (int j = 0; j < gradient.length; j++) {
                gradient[j] /= objectiveScale;
            }
        }
        return gradient;
    }

    /**
     * Is the error caused by the patch, p, large enough to be worth correcting? The solution, x, violates the
     * original optimality conditions by p[x] - compared to the size of the gradient terms [C] and [Q][x].
     */
    private boolean isBiased() {

        PhysicalStore<Double> mtrxX = this.getSolutionX();

        double largestX = mtrxX.aggregateAll(Aggregator.LARGEST).doubleValue();
        double largestC = this.getMatrixC().aggregateAll(Aggregator.LARGEST).doubleValue();
        double largestQX = this.getMatrixQ().multiply(mtrxX).aggregateAll(Aggregator.LARGEST).doubleValue();

        return !BIAS.isSmall(Math.max(largestC, largestQX), myDiagonalPatch * largestX);
    }

    /**
     * When [Q] had to be patched, a small constant added to its diagonal, the problem actually solved is
     * strictly convex and always has a finite solution - also when the original problem is unbounded. Then
     * the solution is huge, dominated by a direction along which the original objective decreases without
     * bound. That's detected by checking that:
     * <ol>
     * <li>The solution is larger than the problem data suggests - closer, in log scale, to the size implied
     * by the patch than to the natural size, |[C]| / |[Q]|.
     * <li>The solution direction is an unbounded direction of the original problem: [Q][d] = 0, [AE][d] = 0,
     * [AI][d] <= 0 and [C]<sup>T</sup>[d] > 0.
     * </ol>
     */
    private boolean isUnbounded() {

        double tolerance = SQRT.invoke(options.convex().smallDiagonal());

        PhysicalStore<Double> x = this.getSolutionX();
        MatrixStore<Double> c = this.getMatrixC();

        double largestX = x.aggregateAll(Aggregator.LARGEST).doubleValue();
        double largestC = c.aggregateAll(Aggregator.LARGEST).doubleValue();

        if (myDiagonalPatch * largestX <= tolerance * largestC) {
            return false;
        }

        MatrixStore<Double> d = x.divide(largestX);

        if (c.dot(d) <= tolerance * largestC) {
            return false;
        }

        double largestQ = myDiagonalPatch / options.convex().smallDiagonal();
        MatrixStore<Double> originalQd = this.getMatrixQ().multiply(d).subtract(d.multiply(myDiagonalPatch));
        if (originalQd.aggregateAll(Aggregator.LARGEST).doubleValue() > tolerance * largestQ) {
            return false;
        }

        if (this.countEqualityConstraints() > 0) {
            MatrixStore<Double> mtrxAE = this.getMatrixAE();
            double largestAE = mtrxAE.aggregateAll(Aggregator.LARGEST).doubleValue();
            if (mtrxAE.multiply(d).aggregateAll(Aggregator.LARGEST).doubleValue() > tolerance * largestAE) {
                return false;
            }
        }

        if (this.countInequalityConstraints() > 0) {
            MatrixStore<Double> mtrxAI = this.getMatrixAI();
            double largestAI = mtrxAI.aggregateAll(Aggregator.LARGEST).doubleValue();
            if (mtrxAI.multiply(d).aggregateAll(Aggregator.MAXIMUM).doubleValue() > tolerance * largestAI) {
                return false;
            }
        }

        return true;
    }

    private void iterate() {

        this.resetIterationsCount();

        if (this.isIteratingPossible()) {

            do {

                this.performIteration();

            } while (this.isIterationAllowed() && this.needsAnotherIteration());
        }
    }

    protected Optimisation.Result buildResult() {

        Access1D<?> solution = this.extractSolution();
        double value = this.evaluateFunction(solution) / myMatrices.getObjectiveAdjustmentFactor();

        Supplier<Access1D<?>> reducedGradient = () -> ArrayR064.wrap(this.extractReducedGradient());

        return new Optimisation.Result(state, value, solution).withReducedGradient(reducedGradient);
    }

    protected boolean computeGeneral(final Collectable<Double, ? super TransformableRegion<Double>> matrix) {
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

    /**
     * With the original (unpatched) [Q]
     */
    protected double evaluateFunction(final Access1D<?> solution) {

        MatrixStore<Double> tmpX = this.getSolutionX();

        double quadratic = tmpX.dot(this.getMatrixQ().multiply(tmpX)) - myDiagonalPatch * tmpX.dot(tmpX);

        return quadratic / TWO - tmpX.dot(this.getMatrixC());
    }

    protected MatrixStore<Double> extractSolution() {
        return this.getSolutionX().copy();
    }

    protected abstract Collectable<Double, ? super TransformableRegion<Double>> getIterationKKT();

    protected abstract Collectable<Double, ? super TransformableRegion<Double>> getIterationRHS();

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
        ConvexObjectiveFunction<Double> objective = myMatrices.getObjective();
        return objective.linear();
    }

    protected PhysicalStore<Double> getMatrixQ() {
        ConvexObjectiveFunction<Double> objective = myMatrices.getObjective();
        return objective.quadratic();
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
        state = State.VALID;

        boolean symmetric = true;
        if (options.validate && !matrixQ.isHermitian()) {

            symmetric = false;
            state = State.INVALID;

            if (!this.isLogDebug()) {
                throw new IllegalArgumentException(Q_NOT_SYMMETRIC);
            }
            this.log(Q_NOT_SYMMETRIC, matrixQ);
        }

        if (!mySolverQ.isComputed()) {
            // Only when (re)computing - a re-initialisation, re-using the decomposition, keeps the patch
            myDiagonalPatch = ZERO;
            myZeroQ = false;
            if (!mySolverQ.compute(matrixQ)) {
                double largest = matrixQ.aggregateAll(Aggregator.LARGEST).doubleValue();
                double small = options.convex().smallDiagonal();
                if (largest > small) {
                    myDiagonalPatch = small * largest;
                    matrixQ.modifyDiagonal(ADD.by(myDiagonalPatch));
                    mySolverQ.compute(matrixQ);
                } else {
                    myZeroQ = true;
                }
            }
        }

        boolean semidefinite = true;
        if (options.validate && !mySolverQ.isSolvable()) {
            // Not symmetric positive definite. Check if at least positive semidefinite.

            Eigenvalue<Double> decompEvD = Eigenvalue.R064.make(matrixQ, true);
            decompEvD.computeValuesOnly(matrixQ);
            Array1D<ComplexNumber> eigenvalues = decompEvD.getEigenvalues();
            decompEvD.reset();

            for (ComplexNumber eigval : eigenvalues) {
                if (eigval.doubleValue() < ZERO && !eigval.isSmall(TEN) || !eigval.isReal()) {

                    semidefinite = false;
                    state = State.INVALID;

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
        // double max = Math.max(RELATIVELY_SMALL, mySolverQ.getRankThreshold());
        // int countVariables = this.countVariables();
        // int countSignificant = mySolverQ.countSignificant(max);
        // return countVariables == countSignificant;
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
            this.log("KKT", this.getIterationKKT().collect(R064Store.FACTORY));
            this.log("RHS", this.getIterationRHS().collect(R064Store.FACTORY));
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

        if (this.isLogDebug()) {
            this.log("LP solution: {}", resultLP);
            this.log("LP duals: {}", resultLP.getDualSolution().map(Supplier::get).get());
        }

        if (!myZeroQ && resultLP.getState().isFeasible()) {
            return resultLP.withState(State.FEASIBLE);
        }

        return resultLP;
    }

    /**
     * Compute the reduced gradient vector (gradient of the Lagrangian w.r.t. x). The base implementation
     * returns Qx - c, with the original (unpatched) [Q]; {@link ConstrainedSolver} overrides to add A'λ.
     */
    double[] computeReducedGradient() {
        int n = this.countVariables();
        double[] gradient = new double[n];
        PhysicalStore<Double> x = this.getSolutionX();
        MatrixStore<Double> Qx = this.getMatrixQ().multiply(x);
        MatrixStore<Double> c = this.getMatrixC();
        for (int j = 0; j < n; j++) {
            gradient[j] = Qx.doubleValue(j) - myDiagonalPatch * x.doubleValue(j) - c.doubleValue(j);
        }
        return gradient;
    }

    /**
     * A copy of the dual solution (Lagrange multipliers), or null if there is none.
     */
    PhysicalStore<Double> copyDualSolution() {
        return null;
    }

    ConstraintsMetaData getConstraintsMetaData() {
        return myMatrices.getConstraintsMetaData();
    }

    ConvexData<Double> getConvexData() {
        return myMatrices;
    }

    boolean isPatchedQ() {
        return myDiagonalPatch > ZERO;
    }

    boolean isZeroQ() {
        return myZeroQ;
    }

    /**
     * @see #copyDualSolution()
     */
    void restoreDualSolution(final PhysicalStore<Double> copy) {
        // No dual solution
    }

    /**
     * Specifically for use by {@link IterativeRefinementSolver2}
     */
    void update(final Access1D<?> mtrxC, final Access1D<?> mtrxBE, final Access1D<?> mtrxBI) {
        myMatrices.update(mtrxC, mtrxBE, mtrxBI);
    }

}

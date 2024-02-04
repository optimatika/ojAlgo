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

import org.ojalgo.ProgrammingError;
import org.ojalgo.array.Array1D;
import org.ojalgo.array.SparseArray;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.matrix.decomposition.Eigenvalue;
import org.ojalgo.matrix.decomposition.MatrixDecomposition;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PhysicalStore.Factory;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.matrix.store.RowsSupplier;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.UpdatableSolver;
import org.ojalgo.optimisation.linear.LinearSolver;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D.Collectable;

abstract class BasePrimitiveSolver extends ConvexSolver implements UpdatableSolver {

    private static final String Q_NOT_POSITIVE_SEMIDEFINITE = "Q not positive semidefinite!";
    private static final String Q_NOT_SYMMETRIC = "Q not symmetric!";

    static final Factory<Double, Primitive64Store> MATRIX_FACTORY = Primitive64Store.FACTORY;

    static BasePrimitiveSolver.Builder builder(final MatrixStore<Double>[] matrices) {
        return new BasePrimitiveSolver.Builder(matrices);
    }

    static BasePrimitiveSolver newSolver(final ConvexData<Double> data, final Optimisation.Options options) {

        if (data.countInequalityConstraints() > 0) {
            if (options.sparse == null || options.sparse.booleanValue()) {
                return new IterativeASS(data, options);
            } else {
                return new DirectASS(data, options);
            }
        } else if (data.countEqualityConstraints() > 0) {
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

        Primitive64Store tmpQ = null;
        Primitive64Store tmpC = null;

        if (mtrxQ == null) {
            tmpQ = Primitive64Store.FACTORY.make(mtrxC.count(), mtrxC.count());
        } else if (mtrxQ instanceof Primitive64Store) {
            tmpQ = (Primitive64Store) mtrxQ;
        } else {
            tmpQ = Primitive64Store.FACTORY.copy(mtrxQ);
        }

        if (mtrxC == null) {
            tmpC = Primitive64Store.FACTORY.make(tmpQ.countRows(), 1L);
        } else if (mtrxC instanceof Primitive64Store) {
            tmpC = (Primitive64Store) mtrxC;
        } else {
            tmpC = Primitive64Store.FACTORY.copy(mtrxC);
        }

        return new ConvexObjectiveFunction(tmpQ, tmpC);
    }

    private final ConvexData<Double> myMatrices;
    private boolean myPatchedQ = false;
    private final Primitive64Store mySolutionX;
    private final MatrixDecomposition.Solver<Double> mySolverGeneral;
    private final MatrixDecomposition.Solver<Double> mySolverQ;
    private boolean myZeroQ = false;

    BasePrimitiveSolver(final ConvexData<Double> convexSolverBuilder, final Optimisation.Options optimisationOptions) {

        super(optimisationOptions);

        myMatrices = convexSolverBuilder;

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
    public ConvexData<Double> getEntityMap() {
        return myMatrices;
    }

    @Override
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

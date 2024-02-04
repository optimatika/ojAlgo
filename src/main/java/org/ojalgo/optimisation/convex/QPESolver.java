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

import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.optimisation.Optimisation;

/**
 * Solves optimisation problems of the form:
 * <p>
 * min 1/2 [X]<sup>T</sup>[Q][X] - [C]<sup>T</sup>[X]<br>
 * when [AE][X] == [BE]
 * </p>
 *
 * @author apete
 */
final class QPESolver extends ConstrainedSolver {

    private boolean myFeasible = false;
    private final Primitive64Store myIterationX;

    QPESolver(final ConvexData<Double> convexSolverBuilder, final Optimisation.Options optimisationOptions) {

        super(convexSolverBuilder, optimisationOptions);

        myIterationX = MATRIX_FACTORY.make(this.countVariables(), 1L);
    }

    @Override
    protected boolean initialise(final Result kickStarter) {

        boolean ok = super.initialise(kickStarter);

        if (kickStarter != null && kickStarter.getState().isFeasible()) {

            this.getSolutionX().fillMatching(kickStarter);
            myFeasible = true;

            this.setState(State.FEASIBLE);

        } else {

            this.getSolutionX().fillAll(ZERO);
            myFeasible = false; // Could still be feasible, but doesn't matter...
        }

        return ok;
    }

    @Override
    protected boolean needsAnotherIteration() {
        return this.countIterations() < 1;
    }

    @Override
    protected void performIteration() {

        MatrixStore<Double> iterA = this.getIterationA();
        MatrixStore<Double> iterB = this.getIterationB();
        MatrixStore<Double> iterC = this.getIterationC();

        boolean solved = false;

        Primitive64Store iterX = myIterationX;
        Primitive64Store iterL = this.getSolutionL();

        if (iterA.countRows() < iterA.countColumns() && (solved = this.isSolvableQ())) {
            // Q is SPD
            // Actual/normal optimisation problem

            MatrixStore<Double> invQAt = this.getSolutionQ(iterA.transpose());

            // Negated Schur complement
            MatrixStore<Double> negS = iterA.multiply(invQAt);
            // TODO Symmetric, only need to calculate halv the Schur complement
            if (solved = this.computeGeneral(negS)) {

                // iterX temporarely used to store tmpInvQC
                MatrixStore<Double> invQC = this.getSolutionQ(iterC, iterX);

                this.getSolutionGeneral(iterA.multiply(invQC).subtract(iterB), iterL);
                this.getSolutionQ(iterC.subtract(iterA.transpose().multiply(iterL)), iterX);
            }
        }

        if (!solved) {
            // The above failed, try solving the full KKT system instaed

            Primitive64Store tmpXL = MATRIX_FACTORY.make(this.countVariables() + this.countIterationConstraints(), 1L);

            if (solved = this.solveFullKKT(tmpXL)) {
                iterX.fillMatching(tmpXL.limits(this.countVariables(), 1));
                iterL.fillMatching(tmpXL.offsets(this.countVariables(), 0));
            }
        }

        if (solved) {

            this.setState(State.OPTIMAL);

            if (myFeasible) {
                this.getSolutionX().modifyMatching(ADD, iterX);
            } else {
                this.getSolutionX().fillMatching(iterX);
            }

        } else if (myFeasible) {

            this.setState(State.FEASIBLE);

        } else {

            this.setState(State.INFEASIBLE);
            this.getSolutionX().fillAll(ZERO);
        }

        this.incrementIterationsCount();
    }

    @Override
    int countIterationConstraints() {
        return this.countEqualityConstraints();
    }

    @Override
    MatrixStore<Double> getIterationA() {
        return this.getMatrixAE();
    }

    @Override
    MatrixStore<Double> getIterationB() {
        if (myFeasible) {
            return MATRIX_FACTORY.makeZero(this.countEqualityConstraints(), 1);
        } else {
            return this.getMatrixBE();
        }
    }

    @Override
    MatrixStore<Double> getIterationC() {
        if (myFeasible) {
            MatrixStore<Double> mtrxQ = this.getMatrixQ();
            MatrixStore<Double> mtrxC = this.getMatrixC();
            PhysicalStore<Double> solX = this.getSolutionX();
            return mtrxC.subtract(mtrxQ.multiply(solX));
        } else {
            return this.getMatrixC();
        }
    }

}

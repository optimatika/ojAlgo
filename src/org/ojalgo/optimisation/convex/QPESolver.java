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

import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
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

    private final PrimitiveDenseStore myIterationX;

    QPESolver(final ConvexSolver.Builder matrices, final Optimisation.Options solverOptions) {

        super(matrices, solverOptions);

        myIterationX = FACTORY.makeZero(this.countVariables(), 1L);
    }

    private boolean isFeasible() {

        boolean retVal = true;

        final MatrixStore<Double> tmpSE = this.getSE();
        for (int i = 0; retVal && (i < tmpSE.countRows()); i++) {
            if (!options.feasibility.isZero(tmpSE.doubleValue(i))) {
                retVal = false;
            }
        }

        return retVal;
    }

    @Override
    protected boolean initialise(final Result kickStarter) {

        super.initialise(kickStarter);

        if ((kickStarter != null) && kickStarter.getState().isFeasible()) {
            this.getSolutionX().fillMatching(kickStarter);
            if (!(myFeasible = this.isFeasible())) {
                this.getSolutionX().fillAll(ZERO);
            }
        } else {
            this.getSolutionX().fillAll(ZERO);
            myFeasible = false; // Could still be feasible, but doesn't matter...
        }

        return true;
    }

    @Override
    protected boolean needsAnotherIteration() {
        return this.countIterations() < 1;
    }

    @Override
    protected void performIteration() {

        this.getIterationQ();
        final MatrixStore<Double> tmpIterC = this.getIterationC();
        final MatrixStore<Double> tmpIterA = this.getIterationA();
        final MatrixStore<Double> tmpIterB = this.getIterationB();

        boolean solved = false;

        final PrimitiveDenseStore tmpIterX = myIterationX;
        final PrimitiveDenseStore tmpIterL = FACTORY.makeZero(tmpIterA.countRows(), 1L);

        if ((tmpIterA.countRows() < tmpIterA.countColumns()) && (solved = this.isSolvableQ())) {
            // Q is SPD
            // Actual/normal optimisation problem

            final MatrixStore<Double> tmpInvQAT = this.getSolutionQ(tmpIterA.transpose());
            // TODO Only 1 column change inbetween active set iterations (add or remove 1 column)

            // Negated Schur complement
            final MatrixStore<Double> tmpS = tmpIterA.multiply(tmpInvQAT);
            // TODO Symmetric, only need to calculate halv the Schur complement
            if (solved = this.computeGeneral(tmpS)) {

                // tmpX temporarely used to store tmpInvQC
                final MatrixStore<Double> tmpInvQC = this.getSolutionQ(tmpIterC, tmpIterX); //TODO Constant if C doesn't change

                this.getSolutionGeneral(tmpIterA.multiply(tmpInvQC).subtract(tmpIterB), tmpIterL);
                this.getSolutionQ(tmpIterC.subtract(tmpIterA.transpose().multiply(tmpIterL)), tmpIterX);
            }

        }

        if (!solved) {
            // The above failed, try solving the full KKT system instaed

            final PrimitiveDenseStore tmpXL = FACTORY.makeZero(this.countVariables() + this.countIterationConstraints(), 1L);

            if (solved = this.solveFullKKT(tmpXL)) {
                tmpIterX.fillMatching(tmpXL.logical().limits(this.countVariables(), 1).get());
                tmpIterL.fillMatching(tmpXL.logical().offsets(this.countVariables(), 0).get());
            }
        }

        if (solved) {

            this.setState(State.OPTIMAL);

            if (myFeasible) {
                this.getSolutionX().modifyMatching(PrimitiveFunction.ADD, tmpIterX);
            } else {
                this.getSolutionX().fillMatching(tmpIterX);
            }

            // this.getLE().fillMatching(tmpIterL);

        } else {

            if (myFeasible) {

                this.setState(State.FEASIBLE);

            } else {

                this.setState(State.INFEASIBLE);
                this.getSolutionX().fillAll(ZERO);
            }
        }
    }

    @Override
    int countIterationConstraints() {
        return (int) this.getIterationA().countRows();
    }

    @Override
    final MatrixStore<Double> getIterationA() {
        return this.getMatrixAE();
    }

    @Override
    final MatrixStore<Double> getIterationB() {
        if (myFeasible) {
            return MatrixStore.PRIMITIVE.makeZero(this.countEqualityConstraints(), 1).get();
        } else {
            return this.getMatrixBE();
        }
    }

    @Override
    final MatrixStore<Double> getIterationC() {
        if (myFeasible) {
            final MatrixStore<Double> mtrxQ = this.getMatrixQ();
            final MatrixStore<Double> mtrxC = this.getMatrixC();
            final PhysicalStore<Double> solX = this.getSolutionX();
            return mtrxC.subtract(mtrxQ.multiply(solX));
        } else {
            return this.getMatrixC();
        }
    }

}

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

import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.optimisation.GenericSolver;
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

    QPESolver(final ConvexSolver.Builder matrices, final Optimisation.Options solverOptions) {

        super(matrices, solverOptions);

        myIterationX = Primitive64Store.FACTORY.make(this.countVariables(), 1L);
    }

    private boolean isFeasible() {

        boolean retVal = true;

        final MatrixStore<Double> tmpSE = this.getSlackE();
        for (int i = 0; retVal && i < tmpSE.countRows(); i++) {
            if (!GenericSolver.ACCURACY.isZero(tmpSE.doubleValue(i))) {
                retVal = false;
            }
        }

        return retVal;
    }

    @Override
    protected boolean initialise(final Result kickStarter) {

        boolean ok = super.initialise(kickStarter);

        if (kickStarter != null && kickStarter.getState().isFeasible()) {

            this.getSolutionX().fillMatching(kickStarter);

            if (!(myFeasible = this.isFeasible())) {
                this.getSolutionX().fillAll(ZERO);
            } else {
                this.setState(State.FEASIBLE);
            }

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

        final MatrixStore<Double> tmpIterC = this.getIterationC();
        final MatrixStore<Double> tmpIterA = this.getIterationA();
        final MatrixStore<Double> tmpIterB = this.getIterationB();

        boolean solved = false;

        final Primitive64Store tmpIterX = myIterationX;
        // final Primitive64Store tmpIterL = Primitive64Store.FACTORY.make(tmpIterA.countRows(), 1L);
        final Primitive64Store tmpIterL = this.getSolutionL();

        if (tmpIterA.countRows() < tmpIterA.countColumns() && (solved = this.isSolvableQ())) {
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

            final Primitive64Store tmpXL = Primitive64Store.FACTORY.make(this.countVariables() + this.countIterationConstraints(), 1L);

            if (solved = this.solveFullKKT(tmpXL)) {
                tmpIterX.fillMatching(tmpXL.limits(this.countVariables(), 1));
                tmpIterL.fillMatching(tmpXL.offsets(this.countVariables(), 0));
            }
        }

        if (solved) {

            this.setState(State.OPTIMAL);

            if (myFeasible) {
                this.getSolutionX().modifyMatching(ADD, tmpIterX);
            } else {
                this.getSolutionX().fillMatching(tmpIterX);
            }

            // this.getSolutionL().fillMatching(tmpIterL);

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
        return (int) this.getIterationA().countRows();
    }

    @Override
    MatrixStore<Double> getIterationA() {
        return this.getMatrixAE();
    }

    @Override
    MatrixStore<Double> getIterationB() {
        if (myFeasible) {
            return Primitive64Store.FACTORY.makeZero(this.countEqualityConstraints(), 1);
        }
        return this.getMatrixBE();
    }

    @Override
    MatrixStore<Double> getIterationC() {
        if (myFeasible) {
            final MatrixStore<Double> mtrxQ = this.getMatrixQ();
            final MatrixStore<Double> mtrxC = this.getMatrixC();
            final PhysicalStore<Double> solX = this.getSolutionX();
            return mtrxC.subtract(mtrxQ.multiply(solX));
        }
        return this.getMatrixC();
    }

}

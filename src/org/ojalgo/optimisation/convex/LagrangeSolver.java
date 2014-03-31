/* 
 * Copyright 1997-2014 Optimatika (www.optimatika.se)
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

import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.matrix.decomposition.Cholesky;
import org.ojalgo.matrix.decomposition.CholeskyDecomposition;
import org.ojalgo.matrix.decomposition.Eigenvalue;
import org.ojalgo.matrix.decomposition.EigenvalueDecomposition;
import org.ojalgo.matrix.store.IdentityStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;

/**
 * @author apete
 */
public final class LagrangeSolver extends ConvexSolver {

    LagrangeSolver(final ExpressionsBasedModel aModel, final Optimisation.Options solverOptions, final ConvexSolver.Builder matrices) {
        super(aModel, solverOptions, matrices);
    }

    private void extractSolution(final KKTSolver.Output output) {
        this.getX().fillMatching(output.getX());
        this.getLE().fillMatching(output.getL());
    }

    private KKTSolver.Input makeInput(final int patchLevel) {

        MatrixStore<Double> tmpQ = this.getQ();
        MatrixStore<Double> tmpC = this.getC();
        final MatrixStore<Double> tmpA = this.getAE();
        final MatrixStore<Double> tmpB = this.getBE();

        if ((patchLevel > 0) && (tmpA != null) && (tmpA.countRows() > 0L)) {
            tmpQ = tmpQ.add(tmpA.multiplyLeft(tmpA.transpose()));
            tmpC = tmpC.add(tmpB.multiplyLeft(tmpA.transpose()));
        }

        if (patchLevel > 1) {

            final double tmpLargest = tmpQ.aggregateAll(Aggregator.LARGEST);
            final double tmpRelativelySmall = MACHINE_DOUBLE_ERROR * tmpLargest;
            final double tmpPracticalLimit = MACHINE_DOUBLE_ERROR + IS_ZERO;
            final double tmpSmallToAdd = Math.max(tmpRelativelySmall, tmpPracticalLimit);

            tmpQ = tmpQ.add(IdentityStore.makePrimitive((int) tmpQ.countRows()).scale(tmpSmallToAdd));
        }

        return new KKTSolver.Input(tmpQ, tmpC, tmpA, tmpB);
    }

    @Override
    protected boolean initialise(final Result kickStart) {
        return true;
    }

    @Override
    protected boolean needsAnotherIteration() {
        return this.countIterations() < 1;
    }

    @Override
    protected void performIteration() {

        final KKTSolver.Input tmpInput = this.makeInput(0);
        final KKTSolver tmpSolver = new KKTSolver(tmpInput);
        final KKTSolver.Output tmpOutput = tmpSolver.solve(tmpInput);

        if (tmpOutput.isSolvable()) {

            this.setState(State.OPTIMAL);
            this.extractSolution(tmpOutput);

        } else {

            this.setState(State.INFEASIBLE);
            this.resetX();
        }
    }

    @Override
    protected boolean validate() {

        boolean retVal = true;
        this.setState(State.VALID);

        try {

            final MatrixStore<Double> tmpQ = this.getQ();

            final Cholesky<Double> tmpCholesky = CholeskyDecomposition.makePrimitive();
            tmpCholesky.compute(tmpQ, true);

            if (!tmpCholesky.isSPD()) {
                // Not positive definite. Check if at least positive semidefinite.

                final Eigenvalue<Double> tmpEigenvalue = EigenvalueDecomposition.makePrimitive(true);
                tmpEigenvalue.compute(tmpQ, true);

                final MatrixStore<Double> tmpD = tmpEigenvalue.getD();

                final int tmpLength = (int) tmpD.countRows();
                for (int ij = 0; retVal && (ij < tmpLength); ij++) {
                    if (tmpD.doubleValue(ij, ij) < ZERO) {
                        retVal = false;
                        this.setState(State.INVALID);
                    }
                }

            }

            if (retVal) {
                // Q ok, check AE

                //                final MatrixStore<Double> tmpAE = this.getAE();
                //
                //                final LU<Double> tmpLU = LUDecomposition.makePrimitive();
                //                tmpLU.compute(tmpAE);
                //
                //                if (tmpLU.getRank() != tmpAE.getRowDim()) {
                //                    retVal = false;
                //                    this.setState(State.INVALID);
                //                }
            }

        } catch (final Exception ex) {

            retVal = false;
            this.setState(State.FAILED);
        }

        return retVal;
    }

}

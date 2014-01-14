/* 
 * Copyright 1997-2013 Optimatika (www.optimatika.se)
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
package org.ojalgo.optimisation.quadratic;

import static org.ojalgo.constant.PrimitiveMath.*;

import org.ojalgo.matrix.decomposition.Cholesky;
import org.ojalgo.matrix.decomposition.CholeskyDecomposition;
import org.ojalgo.matrix.decomposition.DecompositionStore;
import org.ojalgo.matrix.decomposition.Eigenvalue;
import org.ojalgo.matrix.decomposition.EigenvalueDecomposition;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;

/**
 * When the KKT matrix is nonsingular, there is a unique optimal primal-dual pair (x,v). If the KKT matrix is singular,
 * but the KKT system is solvable, any solution yields an optimal pair (x,v). If the KKT system is not solvable, the
 * quadratic optimization problem is unbounded below or infeasible.
 * 
 * @author apete
 */
final class UnconstrainedSolver extends QuadraticSolver {

    private final Cholesky<Double> myCholesky = CholeskyDecomposition.makePrimitive();
    private final Eigenvalue<Double> myEigenvalue = EigenvalueDecomposition.makePrimitive(true);

    UnconstrainedSolver(final ExpressionsBasedModel aModel, final Optimisation.Options solverOptions, final QuadraticSolver.Builder matrices) {
        super(aModel, solverOptions, matrices);
    }

    @Override
    protected boolean initialise(final Result kickStart) {
        myCholesky.reset();
        myEigenvalue.reset();
        return true;
    }

    @Override
    protected boolean needsAnotherIteration() {
        return this.countIterations() < 1;
    }

    @Override
    protected void performIteration() {

        final MatrixStore<Double> tmpBody = this.getQ();
        final MatrixStore<Double> tmpRhs = this.getC();

        final DecompositionStore<Double> tmpX = this.getX();

        if (!myCholesky.isComputed()) {
            myCholesky.compute(tmpBody, false);
        }

        if (myCholesky.isSolvable()) {

            myCholesky.solve(tmpRhs, tmpX);
            this.setState(State.DISTINCT);

        } else {

            if (!myEigenvalue.isComputed()) {
                myEigenvalue.compute(tmpBody);
            }

            if (myEigenvalue.isSolvable()) {

                myEigenvalue.solve(tmpRhs, tmpX);
                this.setState(State.OPTIMAL);

            } else {

                this.resetX();
                this.setState(State.UNBOUNDED);
            }
        }
    }

    @Override
    protected boolean validate() {

        boolean retVal = true;
        this.setState(State.VALID);

        final MatrixStore<Double> tmpQ = this.getQ();

        myCholesky.compute(tmpQ, true);

        if (!myCholesky.isSPD()) {
            // Not positive definite. Check if at least positive semidefinite.

            myEigenvalue.compute(tmpQ, true);

            final MatrixStore<Double> tmpD = myEigenvalue.getD();

            final int tmpLength = (int) tmpD.countRows();
            for (int ij = 0; retVal && (ij < tmpLength); ij++) {
                if (tmpD.doubleValue(ij, ij) < ZERO) {
                    retVal = false;
                    this.setState(State.INVALID);
                }
            }
        }

        return retVal;
    }

}

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

import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.optimisation.ConstraintsMap;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.structure.Access2D.Collectable;

abstract class ConstrainedSolver extends BasePrimitiveSolver {

    private final Primitive64Store mySlackE;
    private final Primitive64Store mySolutionL;

    protected ConstrainedSolver(final ConvexData<Double> convexSolverBuilder, final Optimisation.Options optimisationOptions) {

        super(convexSolverBuilder, optimisationOptions);

        int numberOfEqualityConstraints = this.countEqualityConstraints();
        int numberOfInequalityConstraints = this.countInequalityConstraints();

        mySlackE = MATRIX_FACTORY.make(numberOfEqualityConstraints, 1L);
        mySolutionL = MATRIX_FACTORY.make(numberOfEqualityConstraints + numberOfInequalityConstraints, 1L);
    }

    @Override
    protected Result buildResult() {

        Result result = super.buildResult();

        ConstraintsMap constraints = this.getEntityMap().getConstraintsMap();
        if (constraints.isEntityMap()) {
            return result.multipliers(constraints, mySolutionL);
        } else {
            return result.multipliers(mySolutionL);
        }
    }

    @Override
    protected Collectable<Double, ? super PhysicalStore<Double>> getIterationKKT() {
        MatrixStore<Double> iterQ = this.getIterationQ();
        MatrixStore<Double> iterA = this.getIterationA();
        return iterQ.right(iterA.transpose()).below(iterA);
    }

    @Override
    protected Collectable<Double, ? super PhysicalStore<Double>> getIterationRHS() {
        MatrixStore<Double> iterC = this.getIterationC();
        MatrixStore<Double> iterB = this.getIterationB();
        return iterC.below(iterB);
    }

    @Override
    protected boolean initialise(final Result kickStarter) {

        boolean spdQ = super.initialise(kickStarter);

        if (options.validate) {

            MatrixStore<Double> iterationA = this.getIterationA();

            if (iterationA != null) {
                this.computeGeneral(iterationA.countRows() < iterationA.countColumns() ? iterationA.transpose() : iterationA);
                if (this.getRankGeneral() != iterationA.countRows()) {

                    this.setState(State.INVALID);

                    if (!this.isLogDebug()) {
                        throw new IllegalArgumentException("A not full (row) rank!");
                    }
                    this.log("A not full (row) rank!");
                }
            }
        }

        return spdQ;
    }

    /**
     * The number of rows in {@link #getIterationA()} and {@link #getIterationB()} without having to actually
     * create them.
     */
    abstract int countIterationConstraints();

    abstract MatrixStore<Double> getIterationA();

    abstract MatrixStore<Double> getIterationB();

    abstract MatrixStore<Double> getIterationC();

    MatrixStore<Double> getIterationL(final int[] included) {

        int tmpCountE = this.countEqualityConstraints();

        MatrixStore<Double> tmpLI = mySolutionL.offsets(tmpCountE, 0).rows(included);

        return mySolutionL.limits(tmpCountE, 1).below(tmpLI);
    }

    PhysicalStore<Double> getIterationQ() {
        return this.getMatrixQ();
    }

    PhysicalStore<Double> getSlackE() {

        MatrixStore<Double> mtrxBE = this.getMatrixBE();
        PhysicalStore<Double> mtrxX = this.getSolutionX();

        mySlackE.fillMatching(mtrxBE);

        for (int i = 0, limit = mtrxBE.getRowDim(); i < limit; i++) {
            mySlackE.add(i, -this.getMatrixAE(i).dot(mtrxX));
        }

        return mySlackE;
    }

    Primitive64Store getSolutionL() {
        return mySolutionL;
    }

}

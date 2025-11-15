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

import static org.ojalgo.function.constant.PrimitiveMath.ADD;
import static org.ojalgo.function.constant.PrimitiveMath.ZERO;

import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.matrix.store.TransformableRegion;
import org.ojalgo.optimisation.ConstraintsMetaData;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.structure.Access2D.Collectable;

abstract class ConstrainedSolver extends BasePrimitiveSolver {

    private final R064Store mySlackE;
    private final R064Store mySolutionL;

    protected ConstrainedSolver(final ConvexData<Double> convexData, final Optimisation.Options optimisationOptions) {

        super(convexData, optimisationOptions);

        int numberOfEqualityConstraints = this.countEqualityConstraints();
        int numberOfInequalityConstraints = this.countInequalityConstraints();

        mySlackE = MATRIX_FACTORY.make(numberOfEqualityConstraints, 1L);
        mySolutionL = MATRIX_FACTORY.make(numberOfEqualityConstraints + numberOfInequalityConstraints, 1L);
    }

    @Override
    protected Result buildResult() {

        Result result = super.buildResult();

        ConstraintsMetaData constraints = this.getEntityMap().getConstraintsMetaData();
        if (constraints.isEntityMap()) {
            return result.multipliers(constraints, mySolutionL);
        } else {
            return result.multipliers(mySolutionL);
        }
    }

    @Override
    protected Collectable<Double, ? super TransformableRegion<Double>> getIterationKKT() {
        MatrixStore<Double> iterQ = this.getIterationQ();
        MatrixStore<Double> iterA = this.getIterationA();
        boolean ext = options.convex().isExtendedPrecision();
        int m = (int) iterA.countRows();
        double diagMax = 1.0;
        double diagMin = Double.POSITIVE_INFINITY;
        for (int r = 0; r < m; r++) {
            double rowNorm = 0.0;
            for (int c = 0; c < iterA.countColumns(); c++) {
                rowNorm += Math.abs(iterA.doubleValue(r, c));
            }
            if (rowNorm > diagMax) diagMax = rowNorm;
            if (rowNorm > 0.0 && rowNorm < diagMin) diagMin = rowNorm;
        }
        double rho = DualRegularisation.strategy().compute(diagMax, diagMin, m, ext, this.isZeroQ());
        if (rho != 0.0) {
            PhysicalStore<Double> dualBlock = MATRIX_FACTORY.make(m, m);
            dualBlock.fillAll(ZERO);
            dualBlock.modifyDiagonal(ADD.by(-rho));
            return iterQ.right(iterA.transpose()).below(iterA.right(dualBlock));
        }
        return iterQ.right(iterA.transpose()).below(iterA);
    }

    @Override
    protected Collectable<Double, ? super TransformableRegion<Double>> getIterationRHS() {
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

    R064Store getSolutionL() {
        return mySolutionL;
    }

}
/*
 * Copyright 1997-2020 Optimatika
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
import org.ojalgo.structure.Access2D.Collectable;

abstract class ConstrainedSolver extends ConvexSolver {

    private final Primitive64Store mySlackE;

    protected ConstrainedSolver(final ConvexSolver.Builder matrices, final Options solverOptions) {

        super(matrices, solverOptions);

        int numberOfEqualityConstraints = this.countEqualityConstraints();

        mySlackE = Primitive64Store.FACTORY.make(numberOfEqualityConstraints, 1L);
    }

    @Override
    protected final Collectable<Double, ? super PhysicalStore<Double>> getIterationKKT() {
        final MatrixStore<Double> iterQ = this.getIterationQ();
        final MatrixStore<Double> iterA = this.getIterationA();
        return iterQ.logical().right(iterA.transpose()).below(iterA);
    }

    @Override
    protected final Collectable<Double, ? super PhysicalStore<Double>> getIterationRHS() {
        final MatrixStore<Double> iterC = this.getIterationC();
        final MatrixStore<Double> iterB = this.getIterationB();
        return iterC.logical().below(iterB);
    }

    @Override
    protected boolean initialise(final Result kickStarter) {

        boolean spdQ = super.initialise(kickStarter);

        boolean fullRankA = true;
        if (options.validate) {

            MatrixStore<Double> iterationA = this.getIterationA();

            if (iterationA != null) {
                this.computeGeneral(iterationA.countRows() < iterationA.countColumns() ? iterationA.transpose() : iterationA);
                if (this.getRankGeneral() != iterationA.countRows()) {

                    fullRankA = false;
                    this.setState(State.INVALID);

                    if (this.isLogDebug()) {
                        this.log("A not full (row) rank!");
                    } else {
                        throw new IllegalArgumentException("A not full (row) rank!");
                    }
                }
            }
        }

        return spdQ && fullRankA;
    }

    /**
     * The number of rows in {@link #getIterationA()} and {@link #getIterationB()} without having to actually
     * create them.
     */
    abstract int countIterationConstraints();

    abstract MatrixStore<Double> getIterationA();

    abstract MatrixStore<Double> getIterationB();

    abstract MatrixStore<Double> getIterationC();

    final PhysicalStore<Double> getIterationQ() {
        return this.getMatrixQ();
    }

    PhysicalStore<Double> getSlackE() {

        MatrixStore<Double> mtrxAE = this.getMatrixAE();
        MatrixStore<Double> mtrxBE = this.getMatrixBE();
        PhysicalStore<Double> mtrxX = this.getSolutionX();

        if ((mtrxAE != null) && (mtrxAE.count() != 0L)) {
            mtrxX.premultiply(mtrxAE).operateOnMatching(mtrxBE, SUBTRACT).supplyTo(mySlackE);
        }

        return mySlackE;
    }

}

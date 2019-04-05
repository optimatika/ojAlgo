/*
 * Copyright 1997-2019 Optimatika
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
import org.ojalgo.structure.Access2D.Collectable;

abstract class ConstrainedSolver extends ConvexSolver {

    protected ConstrainedSolver(final ConvexSolver.Builder matrices, final Options solverOptions) {
        super(matrices, solverOptions);
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
    protected boolean validate() {

        super.validate();

        final MatrixStore<Double> iterA = this.getIterationA();
        final MatrixStore<Double> iterB = this.getIterationB();

        if (((iterA != null) && (iterB == null)) || ((iterA == null) && (iterB != null))) {
            throw new IllegalArgumentException("Either A or B is null, and the other one is not!");
        }

        if (iterA != null) {
            this.computeGeneral(iterA.countRows() < iterA.countColumns() ? iterA.transpose() : iterA);
            if (this.getRankGeneral() != iterA.countRows()) {
                throw new IllegalArgumentException("A must have full (row) rank!");
            }
        }

        this.setState(State.VALID);
        return true;
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

}

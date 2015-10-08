/*
 * Copyright 1997-2015 Optimatika (www.optimatika.se)
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

abstract class ConstrainedSolver extends ConvexSolver {

    protected ConstrainedSolver(final Builder matrices, final Options solverOptions) {
        super(matrices, solverOptions);
    }

    abstract MatrixStore<Double> getIterationA();

    abstract MatrixStore<Double> getIterationB();

    @Override
    protected boolean validate() {

        super.validate();

        final MatrixStore<Double> tmpA = this.getIterationA();
        final MatrixStore<Double> tmpB = this.getIterationB();

        if (((tmpA != null) && (tmpB == null)) || ((tmpA == null) && (tmpB != null))) {
            throw new IllegalArgumentException("Either A or B is null, and the other one is not!");
        }

        if (tmpA != null) {
            myLU.decompose(tmpA.countRows() < tmpA.countColumns() ? tmpA.transpose() : tmpA);
            if (myLU.getRank() != tmpA.countRows()) {
                throw new IllegalArgumentException("A must have full (row) rank!");
            }
        }

        this.setState(State.VALID);
        return true;
    }

}

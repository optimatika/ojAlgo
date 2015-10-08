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

final class PureASS extends ActiveSetSolver {

    PureASS(final Builder matrices, final Options solverOptions) {
        super(matrices, solverOptions);
    }

    @Override
    MatrixStore<Double> getIterationA(final int[] included) {

        final MatrixStore<Double> tmpAI = this.getAI();

        MatrixStore<Double> retVal = null;
        if (included.length == 0) {
            retVal = MatrixStore.PRIMITIVE.makeZero(0, this.countVariables()).get();
        } else {
            retVal = tmpAI.builder().row(included).build();
        }

        return retVal;
    }

    @Override
    MatrixStore<Double> getIterationB(final int[] included) {

        // return MatrixStore.PRIMITIVE.makeZero(included.length, 1).get();

        final MatrixStore<Double> tmpBI = this.getBI();

        MatrixStore<Double> retVal = null;
        if (included.length == 0) {
            retVal = MatrixStore.PRIMITIVE.makeZero(0, 1).get();
        } else {
            retVal = tmpBI.builder().row(included).build();
        }

        return retVal;
    }

}

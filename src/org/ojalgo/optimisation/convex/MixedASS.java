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

import org.ojalgo.matrix.store.AboveBelowStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.RowsStore;
import org.ojalgo.matrix.store.ZeroStore;
import org.ojalgo.optimisation.system.KKTSystem;

class MixedASS extends ActiveSetSolver {

    MixedASS(final Builder matrices, final Options solverOptions) {
        super(matrices, solverOptions);
    }

    @Override
    KKTSystem.Input buildDelegateSolverInput(final int[] included) {

        final MatrixStore<Double> tmpQ = this.getQ();
        final MatrixStore<Double> tmpC = this.getC();
        final MatrixStore<Double> tmpAE = this.getAE();
        final MatrixStore<Double> tmpAI = this.getAI();
        final PhysicalStore<Double> tmpX = this.getX();

        final MatrixStore<Double> tmpSubQ = tmpQ;
        final MatrixStore<Double> tmpSubC = tmpC.subtract(tmpSubQ.multiply(tmpX));

        MatrixStore<Double> tmpSubAE = null;
        if (included.length == 0) {
            tmpSubAE = tmpAE;
        } else {
            tmpSubAE = new AboveBelowStore<Double>(tmpAE, new RowsStore<Double>(tmpAI, included));
        }
        final ZeroStore<Double> tmpSubBE = ZeroStore.makePrimitive((int) tmpSubAE.countRows(), 1);

        return new KKTSystem.Input(tmpSubQ, tmpSubC, tmpSubAE, tmpSubBE);
    }

}

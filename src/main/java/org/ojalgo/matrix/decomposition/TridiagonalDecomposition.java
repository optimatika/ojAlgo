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
package org.ojalgo.matrix.decomposition;

import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;

abstract class TridiagonalDecomposition<N extends Comparable<N>> extends InPlaceDecomposition<N> implements Tridiagonal<N> {

    private transient MatrixStore<N> myD = null;
    private transient DecompositionStore<N> myQ = null;

    protected TridiagonalDecomposition(final PhysicalStore.Factory<N, ? extends DecompositionStore<N>> factory) {
        super(factory);
    }

    public final MatrixStore<N> getD() {
        if (myD == null) {
            myD = this.makeD();
        }
        return myD;
    }

    public final MatrixStore<N> getQ() {
        return this.getDecompositionQ();
    }

    @Override

    public void reset() {

        super.reset();

        myD = null;
        myQ = null;
    }

    protected final DecompositionStore<N> getDecompositionQ() {
        if (myQ == null) {
            myQ = this.makeQ();
        }
        return myQ;
    }

    protected abstract void supplyDiagonalTo(double[] d, double[] e);

    abstract MatrixStore<N> makeD();

    abstract DecompositionStore<N> makeQ();

}

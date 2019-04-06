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
package org.ojalgo.matrix.decomposition;

import org.ojalgo.ProgrammingError;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.structure.Access2D;

abstract class InPlaceDecomposition<N extends Number> extends GenericDecomposition<N> {

    private int myColDim;
    private DecompositionStore<N> myInPlace;
    private int myRowDim;

    protected InPlaceDecomposition(final DecompositionStore.Factory<N, ? extends DecompositionStore<N>> factory) {
        super(factory);
    }

    public final MatrixStore<N> getInverse() {
        return this.getInverse(this.allocate(this.getRowDim(), this.getRowDim()));
    }

    public MatrixStore<N> getInverse(final PhysicalStore<N> preallocated) {
        ProgrammingError.throwForUnsupportedOptionalOperation();
        return null;
    }

    protected final int getColDim() {
        return myColDim;
    }

    @Override
    protected double getDimensionalEpsilon() {
        return this.getMaxDim() * PrimitiveMath.MACHINE_EPSILON;
    }

    protected final DecompositionStore<N> getInPlace() {
        return myInPlace;
    }

    protected final int getMaxDim() {
        return Math.max(myRowDim, myColDim);
    }

    protected final int getMinDim() {
        return Math.min(myRowDim, myColDim);
    }

    protected final int getRowDim() {
        return myRowDim;
    }

    final DecompositionStore<N> setInPlace(final Access2D.Collectable<N, ? super DecompositionStore<N>> matrix) {

        final int tmpRowDim = (int) matrix.countRows();
        final int tmpColDim = (int) matrix.countColumns();

        if ((myInPlace != null) && (myRowDim == tmpRowDim) && (myColDim == tmpColDim)) {

        } else {

            myInPlace = this.makeZero(tmpRowDim, tmpColDim);

            myRowDim = tmpRowDim;
            myColDim = tmpColDim;
        }

        matrix.supplyTo(myInPlace);

        this.aspectRatioNormal(tmpRowDim >= tmpColDim);
        this.computed(false);

        return myInPlace;
    }

}

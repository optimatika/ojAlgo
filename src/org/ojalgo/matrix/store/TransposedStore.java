/*
 * Copyright 1997-2014 Optimatika (www.optimatika.se)
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
package org.ojalgo.matrix.store;

import org.ojalgo.access.Access1D;
import org.ojalgo.scalar.Scalar;

public final class TransposedStore<N extends Number> extends TransjugatedStore<N> {

    public TransposedStore(final MatrixStore<N> aBase) {
        super(aBase);
    }

    public N get(final long aRow, final long aCol) {
        return this.getBase().get(aCol, aRow);
    }

    /**
     * @see org.ojalgo.matrix.store.MatrixStore#multiplyLeft(org.ojalgo.matrix.store.MatrixStore)
     */
    @Override
    public MatrixStore<N> multiplyLeft(final Access1D<N> leftMtrx) {

        MatrixStore<N> retVal;

        if (leftMtrx instanceof TransposedStore<?>) {

            retVal = this.getBase().multiply(((TransposedStore<N>) leftMtrx).getOriginal());

            retVal = new TransposedStore<N>(retVal);

        } else {

            retVal = super.multiplyLeft(leftMtrx);
        }

        return retVal;
    }

    /**
     * @see org.ojalgo.matrix.store.MatrixStore#multiply(org.ojalgo.matrix.store.MatrixStore)
     */
    @Override
    public MatrixStore<N> multiply(final Access1D<N> right) {

        MatrixStore<N> retVal;

        if (right instanceof TransposedStore<?>) {

            retVal = this.getBase().multiplyLeft(((TransposedStore<N>) right).getOriginal());

            retVal = new TransposedStore<N>(retVal);

        } else {

            retVal = super.multiply(right);
        }

        return retVal;
    }

    public Scalar<N> toScalar(final long row, final long column) {
        return this.getBase().toScalar(column, row);
    }

    @Override
    public MatrixStore<N> transpose() {
        return this.getBase();
    }

}

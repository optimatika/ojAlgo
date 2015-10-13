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
package org.ojalgo.matrix.store;

import org.ojalgo.access.Access1D;
import org.ojalgo.scalar.Scalar;

/**
 * ConjugatedStore
 *
 * @author apete
 */
final class ConjugatedStore<N extends Number> extends TransjugatedStore<N> {

    ConjugatedStore(final MatrixStore<N> aBase) {
        super(aBase);
    }

    @Override
    public MatrixStore<N> conjugate() {
        return this.getBase();
    }

    public N get(final long aRow, final long aCol) {
        return this.getBase().toScalar((int) aCol, (int) aRow).conjugate().getNumber();
    }

    @Override
    public MatrixStore<N> multiply(final Access1D<N> right) {

        MatrixStore<N> retVal;

        if (right instanceof ConjugatedStore<?>) {

            retVal = ((ConjugatedStore<N>) right).getOriginal().multiply(this.getBase());

            retVal = new ConjugatedStore<N>(retVal);

        } else {

            retVal = super.multiply(right);
        }

        return retVal;
    }

    public Scalar<N> toScalar(final long row, final long column) {
        return this.getBase().toScalar(column, row).conjugate();
    }

}

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
package org.ojalgo.matrix.store;

import org.ojalgo.scalar.Scalar;

final class UpperSymmetricStore<N extends Comparable<N>> extends ShadingStore<N> {

    private final boolean myHermitian;

    UpperSymmetricStore(final MatrixStore<N> base, final boolean hermitian) {
        super(base);
        myHermitian = hermitian;
    }

    @Override
    public double doubleValue(final int row, final int col) {
        if (row > col) {
            return this.base().doubleValue(col, row);
        }
        return this.base().doubleValue(row, col);
    }

    @Override
    public N get(final int row, final int col) {
        if (myHermitian) {
            return this.toScalar(row, col).get();
        }
        if (row > col) {
            return this.base().get(col, row);
        } else {
            return this.base().get(row, col);
        }
    }

    @Override
    public Scalar<N> toScalar(final long row, final long col) {
        if (row <= col) {
            return this.base().toScalar(row, col);
        }
        if (myHermitian) {
            return this.base().toScalar(col, row).conjugate();
        } else {
            return this.base().toScalar(col, row);
        }
    }
}

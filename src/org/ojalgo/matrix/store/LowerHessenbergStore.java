/*
 * Copyright 1997-2018 Optimatika
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

import org.ojalgo.ProgrammingError;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.scalar.Scalar;

/**
 * A Hessenberg matrix is one that is "almost" triangular. A lower Hessenberg matrix has zero entries above
 * the first superdiagonal.
 *
 * @author apete
 */
final class LowerHessenbergStore<N extends Number> extends ShadingStore<N> {

    @SuppressWarnings("unused")
    private LowerHessenbergStore(final int aRowDim, final int aColDim, final MatrixStore<N> base) {

        this(base);

        ProgrammingError.throwForIllegalInvocation();
    }

    LowerHessenbergStore(final MatrixStore<N> base) {
        super(base, (int) base.countRows(), (int) Math.min(base.countRows(), base.countColumns()));
    }

    public double doubleValue(final long row, final long col) {
        if ((row + 1) < col) {
            return PrimitiveMath.ZERO;
        } else {
            return this.getBase().doubleValue(row, col);
        }
    }

    public int firstInColumn(final int col) {
        if (col == 0) {
            return 0;
        } else {
            return col - 1;
        }
    }

    public N get(final long row, final long col) {
        if ((row + 1) < col) {
            return this.physical().scalar().zero().get();
        } else {
            return this.getBase().get(row, col);
        }
    }

    @Override
    public int limitOfRow(final int row) {
        return Math.min(row + 2, this.getColDim());
    }

    public Scalar<N> toScalar(final long row, final long col) {
        if ((row + 1) < col) {
            return this.physical().scalar().zero();
        } else {
            return this.getBase().toScalar(row, col);
        }
    }

}

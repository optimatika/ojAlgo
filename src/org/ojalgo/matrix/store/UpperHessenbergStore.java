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
 * A Hessenberg matrix is one that is "almost" triangular. An upper Hessenberg matrix has zero entries below
 * the first subdiagonal.
 *
 * @author apete
 */
final class UpperHessenbergStore<N extends Number> extends ShadingStore<N> {

    @SuppressWarnings("unused")
    private UpperHessenbergStore(final int aRowDim, final int aColDim, final MatrixStore<N> base) {

        this(base);

        ProgrammingError.throwForIllegalInvocation();
    }

    UpperHessenbergStore(final MatrixStore<N> base) {
        super(base, (int) Math.min(base.countRows(), base.countColumns()), (int) base.countColumns());
    }

    public double doubleValue(final long row, final long col) {
        if (row > (col + 1)) {
            return PrimitiveMath.ZERO;
        } else {
            return this.getBase().doubleValue(row, col);
        }
    }

    public int firstInRow(final int row) {
        if (row == 0) {
            return 0;
        } else {
            return row - 1;
        }
    }

    public N get(final long row, final long col) {
        if (row > (col + 1)) {
            return this.physical().scalar().zero().get();
        } else {
            return this.getBase().get(row, col);
        }
    }

    @Override
    public int limitOfColumn(final int col) {
        return Math.min(col + 2, this.getRowDim());
    }

    public Scalar<N> toScalar(final long row, final long col) {
        if (row > (col + 1)) {
            return this.physical().scalar().zero();
        } else {
            return this.getBase().toScalar(row, col);
        }
    }

}

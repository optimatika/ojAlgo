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

import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.scalar.Scalar;

/**
 * A Hessenberg matrix is one that is "almost" triangular. A lower Hessenberg matrix has zeros above the first
 * superdiagonal, and an upper Hessenberg matrix zeros below the first subdiagonal.
 *
 * @author apete
 */
final class LowerHessenbergStore<N extends Comparable<N>> extends ShadingStore<N> {

    LowerHessenbergStore(final MatrixStore<N> base) {
        super(base);
    }

    @Override
    public double doubleValue(final int row, final int col) {
        if (row + 1 < col) {
            return PrimitiveMath.ZERO;
        }
        return this.base().doubleValue(row, col);
    }

    @Override
    public int firstInColumn(final int col) {
        if (col == 0) {
            return 0;
        }
        return col - 1;
    }

    @Override
    public N get(final int row, final int col) {
        if (row + 1 < col) {
            return this.zero().get();
        }
        return this.base().get(row, col);
    }

    @Override
    public int limitOfRow(final int row) {
        return Math.min(row + 2, this.getColDim());
    }

    @Override
    public Scalar<N> toScalar(final long row, final long col) {
        if (row + 1 < col) {
            return this.zero();
        }
        return this.base().toScalar(row, col);
    }

}

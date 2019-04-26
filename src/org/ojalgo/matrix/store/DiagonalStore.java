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
package org.ojalgo.matrix.store;

import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.scalar.Scalar;

final class DiagonalStore<N extends Number> extends ShadingStore<N> {

    DiagonalStore(final MatrixStore<N> base) {
        super(base, Math.min(base.countRows(), base.countColumns()), Math.min(base.countRows(), base.countColumns()));
    }

    DiagonalStore(final MatrixStore<N> base, final boolean maintain) {
        super(base, maintain ? base.countRows() : Math.min(base.countRows(), base.countColumns()),
                maintain ? base.countColumns() : Math.min(base.countRows(), base.countColumns()));
    }

    public double doubleValue(final long row, final long col) {
        if (row == col) {
            return this.base().doubleValue(row, col);
        } else {
            return PrimitiveMath.ZERO;
        }
    }

    public int firstInColumn(final int col) {
        return col;
    }

    public int firstInRow(final int row) {
        return row;
    }

    public N get(final long row, final long col) {
        if (row == col) {
            return this.base().get(row, col);
        } else {
            return this.zero().get();
        }
    }

    @Override
    public int limitOfColumn(final int col) {
        return col + 1;
    }

    @Override
    public int limitOfRow(final int row) {
        return row + 1;
    }

    @Override
    public void supplyTo(final TransformableRegion<N> consumer) {
        consumer.reset();
        consumer.fillDiagonal(this.base().sliceDiagonal());
    }

    public Scalar<N> toScalar(final long row, final long col) {
        if (row == col) {
            return this.base().toScalar(row, col);
        } else {
            return this.zero();
        }
    }

}

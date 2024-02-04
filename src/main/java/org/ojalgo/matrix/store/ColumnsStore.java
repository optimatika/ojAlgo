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
 * A selection (re-ordering) of columns.
 *
 * @author apete
 */
final class ColumnsStore<N extends Comparable<N>> extends SelectingStore<N> {

    private final int[] myColumns;

    ColumnsStore(final MatrixStore<N> base, final int[] columns) {

        super(base, (int) base.countRows(), columns.length);

        myColumns = columns;
    }

    @Override
    public double doubleValue(final int row, final int col) {
        int colIndex = this.toBaseIndex(col);
        if (colIndex >= 0) {
            return this.base().doubleValue(row, colIndex);
        } else {
            return PrimitiveMath.ZERO;
        }
    }

    @Override
    public int firstInColumn(final int col) {
        int colIndex = this.toBaseIndex(col);
        if (colIndex >= 0) {
            return this.base().firstInColumn(colIndex);
        } else {
            return this.getRowDim();
        }
    }

    @Override
    public N get(final int row, final int col) {
        int colIndex = this.toBaseIndex(col);
        if (colIndex >= 0) {
            return this.base().get(row, colIndex);
        } else {
            return this.zero().get();
        }
    }

    @Override
    public int limitOfColumn(final int col) {
        int colIndex = this.toBaseIndex(col);
        if (colIndex >= 0) {
            return this.base().limitOfColumn(colIndex);
        } else {
            return 0;
        }
    }

    @Override
    public void supplyTo(final TransformableRegion<N> consumer) {
        final MatrixStore<N> base = this.base();
        for (int j = 0; j < myColumns.length; j++) {
            int colIndex = this.toBaseIndex(j);
            if (colIndex >= 0) {
                consumer.fillColumn(j, base.sliceColumn(colIndex));
            } else {
                consumer.fillColumn(j, this.zero().get());
            }
        }
    }

    @Override
    public Scalar<N> toScalar(final long row, final long col) {
        int colIndex = this.toBaseIndex(col);
        if (colIndex >= 0) {
            return this.base().toScalar(row, colIndex);
        } else {
            return this.zero();
        }
    }

    private int toBaseIndex(final int col) {
        return myColumns[col];
    }

    private int toBaseIndex(final long col) {
        return myColumns[Math.toIntExact(col)];
    }

}

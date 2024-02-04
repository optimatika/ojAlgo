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

final class LimitStore<N extends Comparable<N>> extends SelectingStore<N> {

    LimitStore(final int rowsCount, final int columnsCount, final MatrixStore<N> base) {
        super(base, rowsCount, columnsCount);
    }

    LimitStore(final long rowsCount, final long columnsCount, final MatrixStore<N> base) {
        super(base, rowsCount, columnsCount);
    }

    @Override
    public double doubleValue(final int row, final int col) {
        return this.base().doubleValue(row, col);
    }

    @Override
    public N get(final int row, final int col) {
        return this.base().get(row, col);
    }

    @Override
    public int limitOfColumn(final int col) {
        return Math.min(this.base().limitOfColumn(col), this.getRowDim());
    }

    @Override
    public int limitOfRow(final int row) {
        return Math.min(this.base().limitOfRow(row), this.getColDim());
    }

}

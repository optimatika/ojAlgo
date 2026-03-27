/*
 * Copyright 1997-2025 Optimatika
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

/**
 * A selection (re-ordering) of columns.
 *
 * @author apete
 */
class ColumnsStore<N extends Comparable<N>> extends SelectingStore<N> {

    final int[] columns;

    ColumnsStore(final MatrixStore<N> target, final int[] selection) {

        super(target, target.getRowDim(), selection.length);

        columns = selection;
    }

    @Override
    public double doubleValue(final int row, final int col) {
        return base.doubleValue(row, columns[col]);
    }

    @Override
    public int firstInColumn(final int col) {
        return base.firstInColumn(columns[col]);
    }

    @Override
    public N get(final int row, final int col) {
        return base.get(row, columns[col]);
    }

    @Override
    public int limitOfColumn(final int col) {
        return base.limitOfColumn(columns[col]);
    }

    @Override
    public void supplyTo(final TransformableRegion<N> consumer) {
        for (int j = 0, limit = columns.length; j < limit; j++) {
            consumer.fillColumn(j, base.sliceColumn(columns[j]));
        }
    }

    @Override
    public Scalar<N> toScalar(final int row, final int col) {
        return base.toScalar(row, columns[col]);
    }

}

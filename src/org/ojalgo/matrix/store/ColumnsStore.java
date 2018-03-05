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

import org.ojalgo.scalar.Scalar;

/**
 * A selection (re-ordering) of columns.
 *
 * @author apete
 */
final class ColumnsStore<N extends Number> extends SelectingStore<N> {

    private final int[] myColumns;

    ColumnsStore(final MatrixStore<N> base, final int... columns) {

        super(base, (int) base.countRows(), columns.length);

        myColumns = columns;
    }

    /**
     * @see org.ojalgo.matrix.store.MatrixStore#doubleValue(long, long)
     */
    public double doubleValue(final long row, final long col) {
        return this.getBase().doubleValue(row, myColumns[(int) col]);
    }

    public int firstInColumn(final int col) {
        return this.getBase().firstInColumn(myColumns[col]);
    }

    public N get(final long row, final long col) {
        return this.getBase().get(row, myColumns[(int) col]);
    }

    @Override
    public int limitOfColumn(final int col) {
        return this.getBase().limitOfColumn(myColumns[col]);
    }

    public void supplyTo(final ElementsConsumer<N> consumer) {
        final MatrixStore<N> tmpBase = this.getBase();
        for (int c = 0; c < myColumns.length; c++) {
            consumer.fillColumn(0, c, tmpBase.sliceColumn(0, myColumns[c]));
        }
    }

    public Scalar<N> toScalar(final long row, final long column) {
        return this.getBase().toScalar(row, myColumns[(int) column]);
    }

}

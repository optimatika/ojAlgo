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

import org.ojalgo.access.AccessUtils;
import org.ojalgo.scalar.Scalar;

/**
 * A selection (re-ordering) of rows.
 *
 * @author apete
 */
final class RowsStore<N extends Number> extends SelectingStore<N> {

    private final int[] myRows;

    RowsStore(final int first, final int limit, final MatrixStore<N> base) {
        this(base, AccessUtils.makeIncreasingRange(first, limit - first));
    }

    RowsStore(final MatrixStore<N> base, final int... rows) {

        super(rows.length, (int) base.countColumns(), base);

        myRows = rows;
    }

    /**
     * @see org.ojalgo.matrix.store.MatrixStore#doubleValue(long, long)
     */
    public double doubleValue(final long row, final long column) {
        return this.getBase().doubleValue(myRows[(int) row], column);
    }

    public int firstInRow(final int row) {
        return this.getBase().firstInRow(myRows[row]);
    }

    public N get(final long row, final long column) {
        return this.getBase().get(myRows[(int) row], column);
    }

    @Override
    public int limitOfRow(final int row) {
        return this.getBase().limitOfRow(myRows[row]);
    }

    public Scalar<N> toScalar(final long row, final long column) {
        return this.getBase().toScalar(myRows[(int) row], column);
    }

}

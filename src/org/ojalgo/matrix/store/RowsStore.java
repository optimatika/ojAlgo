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

import org.ojalgo.access.Access1D;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.scalar.Scalar;

/**
 * A selection (re-ordering) of rows.
 *
 * @author apete
 */
final class RowsStore<N extends Number> extends SelectingStore<N> {

    private final int[] myRows;

    RowsStore(final MatrixStore<N> base, final int... rows) {

        super(base, rows.length, (int) base.countColumns());

        myRows = rows;
    }

    /**
     * @see org.ojalgo.matrix.store.MatrixStore#doubleValue(long, long)
     */
    public double doubleValue(final long row, final long col) {
        return this.getBase().doubleValue(myRows[(int) row], col);
    }

    public int firstInRow(final int row) {
        return this.getBase().firstInRow(myRows[row]);
    }

    public N get(final long row, final long col) {
        return this.getBase().get(myRows[(int) row], col);
    }

    @Override
    public int limitOfRow(final int row) {
        return this.getBase().limitOfRow(myRows[row]);
    }

    public void multiply(final Access1D<N> right, final ElementsConsumer<N> target) {

        if (this.isPrimitive()) {

            target.countRows();
            final long tmpComplexity = this.countColumns();
            final long tmpTargetColumns = target.countColumns();

            final MatrixStore<N> tmpBase = this.getBase();

            for (int i = 0; i < myRows.length; i++) {
                final int tmpRow = myRows[i];

                final int tmpFirst = tmpBase.firstInRow(tmpRow);
                final int tmpLimit = tmpBase.limitOfRow(tmpRow);

                for (long j = 0L; j < tmpTargetColumns; j++) {

                    double tmpVal = PrimitiveMath.ZERO;

                    for (int c = tmpFirst; c < tmpLimit; c++) {
                        tmpVal += tmpBase.doubleValue(tmpRow, c) * right.doubleValue(c + (j * tmpComplexity));
                    }

                    target.set(i, j, tmpVal);
                }
            }

        } else {

            super.multiply(right, target);
        }

    }

    public void supplyTo(final ElementsConsumer<N> consumer) {
        final MatrixStore<N> tmpBase = this.getBase();
        for (int r = 0; r < myRows.length; r++) {
            consumer.fillRow(r, 0, tmpBase.sliceRow(myRows[r], 0));
        }
    }

    public Scalar<N> toScalar(final long row, final long column) {
        return this.getBase().toScalar(myRows[(int) row], column);
    }

}

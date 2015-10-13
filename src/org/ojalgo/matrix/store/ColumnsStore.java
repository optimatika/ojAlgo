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

import org.ojalgo.ProgrammingError;
import org.ojalgo.scalar.Scalar;

/**
 * A selection (re-ordering) of columns.
 *
 * @author apete
 */
final class ColumnsStore<N extends Number> extends SelectingStore<N> {

    private final int[] myColumns;
    private final int myFirst;

    @SuppressWarnings("unused")
    private ColumnsStore(final MatrixStore<N> aBase) {

        this(aBase, null);

        ProgrammingError.throwForIllegalInvocation();
    }

    ColumnsStore(final int aFirst, final int aLimit, final MatrixStore<N> aBase) {

        super((int) aBase.countRows(), aLimit - aFirst, aBase);

        myColumns = null;
        myFirst = aFirst;
    }

    ColumnsStore(final MatrixStore<N> aBase, final int... someColumns) {

        super((int) aBase.countRows(), someColumns.length, aBase);

        myColumns = someColumns;
        myFirst = 0;
    }

    /**
     * @see org.ojalgo.matrix.store.MatrixStore#doubleValue(long, long)
     */
    public double doubleValue(final long row, final long column) {
        if (myColumns != null) {
            return this.getBase().doubleValue(row, myColumns[(int) column]);
        } else {
            return this.getBase().doubleValue(row, myFirst + column);
        }
    }

    public N get(final long row, final long column) {
        if (myColumns != null) {
            return this.getBase().get(row, myColumns[(int) column]);
        } else {
            return this.getBase().get(row, myFirst + column);
        }
    }

    public Scalar<N> toScalar(final long row, final long column) {
        if (myColumns != null) {
            return this.getBase().toScalar(row, myColumns[(int) column]);
        } else {
            return this.getBase().toScalar(row, myFirst + column);
        }
    }

}

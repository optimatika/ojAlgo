/*
 * Copyright 1997-2016 Optimatika (www.optimatika.se)
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
package org.ojalgo.access;

import java.util.Iterator;

public final class RowView<N extends Number> implements Access1D<N>, Iterator<RowView<N>> {

    public static <S extends Number> Iterable<RowView<S>> makeIterable(final Access2D<S> access) {
        return new RowView<>(access).iterable;
    }

    private final Access2D<N> myAccess;
    private final long myLastRow;
    private long myRow = -1L;

    final Iterable<RowView<N>> iterable = () -> RowView.this;

    @SuppressWarnings("unused")
    private RowView() {
        this(null);
    }

    protected RowView(final Access2D<N> access, final long row) {

        super();

        myAccess = access;
        myLastRow = access.countRows() - 1L;

        myRow = row;
    }

    RowView(final Access2D<N> access) {
        this(access, -1L);
    }

    public long count() {
        return myAccess.countColumns();
    }

    public double doubleValue(final long index) {
        return myAccess.doubleValue(myRow, index);
    }

    public N get(final long index) {
        return myAccess.get(myRow, index);
    }

    public boolean hasNext() {
        return myRow < myLastRow;
    }

    public boolean hasPrevious() {
        return myRow > 0L;
    }

    public RowView<N> next() {
        myRow++;
        return this;
    }

    public RowView<N> previous() {
        myRow--;
        return this;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public long row() {
        return myRow;
    }

}

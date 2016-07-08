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

public final class ColumnView<N extends Number> implements Access1D<N>, Iterator<ColumnView<N>> {

    public static <S extends Number> Iterable<ColumnView<S>> makeIterable(final Access2D<S> access) {
        return new ColumnView<>(access).iterable;
    }

    private final Access2D<N> myAccess;
    private long myColumn = -1L;
    private final long myLastColumn;

    final Iterable<ColumnView<N>> iterable = () -> ColumnView.this;

    @SuppressWarnings("unused")
    private ColumnView() {
        this(null);
    }

    protected ColumnView(final Access2D<N> access, final long column) {

        super();

        myAccess = access;
        myLastColumn = access.countColumns() - 1L;

        myColumn = column;
    }

    ColumnView(final Access2D<N> access) {
        this(access, -1L);
    }

    public long column() {
        return myColumn;
    }

    public long count() {
        return myAccess.countRows();
    }

    public double doubleValue(final long index) {
        return myAccess.doubleValue(index, myColumn);
    }

    public N get(final long index) {
        return myAccess.get(index, myColumn);
    }

    public boolean hasNext() {
        return myColumn < myLastColumn;
    }

    public boolean hasPrevious() {
        return myColumn > 0L;
    }

    public ColumnView<N> next() {
        myColumn++;
        return this;
    }

    public ColumnView<N> previous() {
        myColumn--;
        return this;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

}

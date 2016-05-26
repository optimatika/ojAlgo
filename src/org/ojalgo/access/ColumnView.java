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
package org.ojalgo.access;

import java.util.Iterator;

public final class ColumnView<N extends Number> implements Access1D<N>, Iterator<ColumnView<N>> {

    public static <S extends Number> Iterable<ColumnView<S>> make(final Access2D<S> access) {
        return new ColumnView<S>(access).iterable;
    }

    public long column = -1L;

    private final Access2D<N> myAccess2D;

    final Iterable<ColumnView<N>> iterable = new Iterable<ColumnView<N>>() {

        public Iterator<ColumnView<N>> iterator() {
            return ColumnView.this;
        }

    };

    @SuppressWarnings("unused")
    private ColumnView() {
        this(null);
    }

    protected ColumnView(final Access2D<N> access, final long column) {

        super();

        myAccess2D = access;
        this.column = column;
    }

    ColumnView(final Access2D<N> access) {
        this(access, -1L);
    }

    public long count() {
        return myAccess2D.countRows();
    }

    public double doubleValue(final long index) {
        return myAccess2D.doubleValue(index, column);
    }

    public N get(final long index) {
        return myAccess2D.get(index, column);
    }

    public boolean hasNext() {
        return (column + 1L) < myAccess2D.countColumns();
    }

    public ColumnView<N> next() {
        column++;
        return this;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

}

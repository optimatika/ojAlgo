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
package org.ojalgo.access;

import java.util.Iterator;

import org.ojalgo.ProgrammingError;

public class ColumnView<N extends Number> implements Access1D<N>, Iterator<ColumnView<N>> {

    public static <S extends Number> Iterable<ColumnView<S>> makeIterable(final Access2D<S> access) {
        return new ColumnView<>(access).iterable;
    }

    private long myColumn = -1L;
    private final Access2D<N> myDelegate2D;
    private final long myLastColumn;

    final Iterable<ColumnView<N>> iterable = () -> ColumnView.this;

    protected ColumnView(final Access2D<N> access) {
        this(access, -1L);
    }

    ColumnView(final Access2D<N> access, final long column) {

        super();

        myDelegate2D = access;
        myLastColumn = access.countColumns() - 1L;

        myColumn = column;
    }

    public long column() {
        return myColumn;
    }

    public long count() {
        return myDelegate2D.countRows();
    }

    public double doubleValue(final long index) {
        return myDelegate2D.doubleValue(index, myColumn);
    }

    public N get(final long index) {
        return myDelegate2D.get(index, myColumn);
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
        ProgrammingError.throwForUnsupportedOptionalOperation();
    }

    protected void setColumn(final long column) {
        myColumn = column;
    }

}

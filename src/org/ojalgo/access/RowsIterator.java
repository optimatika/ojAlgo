/*
 * Copyright 1997-2013 Optimatika (www.optimatika.se)
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

public class RowsIterator<N extends Number> implements Access1D<N>, Iterator<Access1D<N>> {

    final class RowIterable implements Iterable<Access1D<N>> {

        RowIterable() {
            super();
        }

        public Iterator<Access1D<N>> iterator() {
            return RowsIterator.this;
        }
    }

    public static <S extends Number> Iterable<Access1D<S>> make(final Access2D<S> access) {
        return new RowsIterator<S>(access).iterable;
    }

    private final Access2D<N> myAccess2D;
    private long myCurrentRow = -1L;

    final RowIterable iterable = new RowIterable();

    @SuppressWarnings("unused")
    private RowsIterator() {
        this(null);
    }

    RowsIterator(final Access2D<N> access) {

        super();

        myAccess2D = access;
    }

    public long count() {
        return myAccess2D.countColumns();
    }

    public double doubleValue(final long index) {
        return myAccess2D.doubleValue(myCurrentRow, index);
    }

    public N get(final long index) {
        return myAccess2D.get(myCurrentRow, index);
    }

    public boolean hasNext() {
        return (myCurrentRow + 1L) < myAccess2D.countRows();
    }

    public Iterator<N> iterator() {
        return new Iterator1D<>(this);
    }

    public Access1D<N> next() {
        myCurrentRow++;
        return this;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public int size() {
        return (int) this.count();
    }

}

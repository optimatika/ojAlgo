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

public class ElementView<N extends Number> implements AccessScalar<N>, Iterator<ElementView<N>> {

    public static <S extends Number> Iterable<ElementView<S>> makeIterable(final Access1D<S> access) {
        return new ElementView<S>(access).iterable;
    }

    private final Access1D<N> myAccess;
    private long myIndex = -1L;
    private final long myLastIndex;

    final Iterable<ElementView<N>> iterable = new Iterable<ElementView<N>>() {

        public Iterator<ElementView<N>> iterator() {
            return ElementView.this;
        }

    };

    @SuppressWarnings("unused")
    private ElementView() {
        this(null);
    }

    protected ElementView(final Access1D<N> access, final long index) {

        super();

        myAccess = access;
        myLastIndex = access.count() - 1L;

        this.myIndex = index;
    }

    ElementView(final Access1D<N> access) {
        this(access, -1L);
    }

    public double doubleValue() {
        return myAccess.doubleValue(myIndex);
    }

    public N getNumber() {
        return myAccess.get(myIndex);
    }

    public boolean hasNext() {
        return myIndex < myLastIndex;
    }

    public boolean hasPrevious() {
        return myIndex > 0L;
    }

    public long index() {
        return myIndex;
    }

    public ElementView<N> next() {
        myIndex++;
        return this;
    }

    public ElementView<N> previous() {
        myIndex--;
        return this;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

}

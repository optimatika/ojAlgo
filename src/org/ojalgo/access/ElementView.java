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

public final class ElementView<N extends Number> implements AccessScalar<N>, Iterator<ElementView<N>> {

    public static <S extends Number> Iterable<ElementView<S>> make(final Access1D<S> access) {
        return new ElementView<S>(access).iterable;
    }

    public long index = -1L;

    private final Access1D<N> myAccess1D;

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

        myAccess1D = access;
        this.index = index;
    }

    ElementView(final Access1D<N> access) {
        this(access, -1L);
    }

    public double doubleValue() {
        return myAccess1D.doubleValue(index);
    }

    public N getNumber() {
        return myAccess1D.get(index);
    }

    public boolean hasNext() {
        return (index + 1L) < myAccess1D.count();
    }

    public ElementView<N> next() {
        index++;
        return this;
    }

}

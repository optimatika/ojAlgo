/*
 * Copyright 1997-2024 Optimatika
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
package org.ojalgo.structure;

import java.util.ListIterator;

import org.ojalgo.ProgrammingError;

public final class Iterator1D<N extends Comparable<N>> implements ListIterator<N> {

    private final Access1D<? extends N> myAccess;
    private final long myCount;
    private long myNextIndex;

    public Iterator1D(final Access1D<? extends N> access) {
        this(access, 0L);
    }

    public Iterator1D(final Access1D<? extends N> access, final long cursor) {

        super();

        myAccess = access;
        myCount = access.count();
        myNextIndex = cursor;
    }

    public void add(final N e) {
        ProgrammingError.throwForUnsupportedOptionalOperation();
    }

    public boolean hasNext() {
        return myNextIndex < myCount;
    }

    public boolean hasPrevious() {
        return myNextIndex > 0L;
    }

    public N next() {
        return myAccess.get(myNextIndex++);
    }

    public int nextIndex() {
        return (int) myNextIndex;
    }

    public N previous() {
        return myAccess.get(--myNextIndex);
    }

    public int previousIndex() {
        return (int) (myNextIndex - 1L);
    }

    public void remove() {
        ProgrammingError.throwForUnsupportedOptionalOperation();
    }

    public void set(final N e) {
        ProgrammingError.throwForUnsupportedOptionalOperation();
    }

}

/*
 * Copyright 1997-2019 Optimatika
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

import java.util.Iterator;

import org.ojalgo.ProgrammingError;

public class MatrixView<N extends Number> implements Access2D<N>, Iterable<MatrixView<N>>, Iterator<MatrixView<N>>, Comparable<MatrixView<N>> {

    private final long myColumnsCount;
    private final long myCount;
    private final AccessAnyD<N> myDelegateAnyD;
    private final long myLastOffset;
    private long myOffset;
    private final long myRowsCount;

    protected MatrixView(final AccessAnyD<N> access) {
        this(access, -1L);
    }

    MatrixView(final AccessAnyD<N> access, final long index) {

        super();

        myDelegateAnyD = access;

        myRowsCount = access.count(0);
        myColumnsCount = access.count(1);
        myCount = myRowsCount * myColumnsCount;

        myOffset = index * myCount;
        myLastOffset = myDelegateAnyD.count() - myCount;
    }

    public int compareTo(MatrixView<N> other) {
        return Long.compare(myOffset, other.getOffset());
    }

    public long count() {
        return myCount;
    }

    public long countColumns() {
        return myColumnsCount;
    }

    public long countRows() {
        return myRowsCount;
    }

    public double doubleValue(long row, long col) {
        return myDelegateAnyD.doubleValue(myOffset + Structure2D.index(myRowsCount, row, col));
    }

    public long estimateSize() {
        return (myLastOffset - myOffset) / myCount;
    }

    public N get(long row, long col) {
        return myDelegateAnyD.get(myOffset + Structure2D.index(myRowsCount, row, col));
    }

    public boolean hasNext() {
        return myOffset < myLastOffset;
    }

    public boolean hasPrevious() {
        return myOffset > 0L;
    }

    /**
     * @return The index of the matrix (which matrix are we currently viewing).
     */
    public long index() {
        return myOffset / myCount;
    }

    public MatrixView<N> iterator() {
        return new MatrixView<>(myDelegateAnyD);
    }

    public MatrixView<N> next() {
        myOffset += myCount;
        return this;
    }

    public MatrixView<N> previous() {
        myOffset -= myCount;
        return this;
    }

    public final void remove() {
        ProgrammingError.throwForUnsupportedOptionalOperation();
    }

    @Override
    public final String toString() {
        return Access2D.toString(this);
    }

    protected void setIndex(final long matrix) {
        myOffset = matrix * myCount;
    }

    long getOffset() {
        return myOffset;
    }

}

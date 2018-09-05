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
package org.ojalgo.structure;

import java.util.Iterator;

import org.ojalgo.ProgrammingError;

public class MatrixView<N extends Number> implements Access2D<N>, Iterator<MatrixView<N>> {

    public static <S extends Number> Iterable<MatrixView<S>> makeIterable(final AccessAnyD<S> access) {
        return new MatrixView<>(access).iterable;
    }

    private final AccessAnyD<N> myDelegateAnyD;
    private long myOffset;
    private long myLastOffset;
    private long myRowsCount;
    private long myColumnsCount;
    private long myCount;

    final Iterable<MatrixView<N>> iterable = () -> MatrixView.this;

    protected MatrixView(final AccessAnyD<N> access) {
        this(access, -1L);
    }

    MatrixView(final AccessAnyD<N> access, final long matrix) {

        super();

        myDelegateAnyD = access;

        myRowsCount = access.count(0);
        myColumnsCount = access.count(1);
        myCount = myRowsCount * myColumnsCount;

        myOffset = matrix * myCount;
        myLastOffset = myDelegateAnyD.count() - myOffset;
    }

    public long count() {
        return myCount;
    }

    public boolean hasNext() {
        return myOffset < myLastOffset;
    }

    public boolean hasPrevious() {
        return myOffset > 0L;
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

    public long matrix() {
        return myOffset / myCount;
    }

    protected void setMatrix(final long matrix) {
        myOffset = matrix * myCount;
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

    public N get(long row, long col) {
        return myDelegateAnyD.get(myOffset + Structure2D.index(myRowsCount, row, col));
    }

}

/*
 * Copyright 1997-2022 Optimatika
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
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.ojalgo.ProgrammingError;

public class RowView<N extends Comparable<N>>
        implements Access1D<N>, Iterable<RowView<N>>, Iterator<RowView<N>>, Spliterator<RowView<N>>, Comparable<RowView<N>> {

    static final int CHARACTERISTICS = Spliterator.CONCURRENT | Spliterator.DISTINCT | Spliterator.IMMUTABLE | Spliterator.NONNULL | Spliterator.ORDERED
            | Spliterator.SIZED | Spliterator.SORTED | Spliterator.SUBSIZED;

    private final Access2D<N> myDelegate2D;
    private final long myLastRow;
    private long myRow = -1L;

    private RowView(final Access2D<N> access, final long row, final long lastRow) {

        super();

        myDelegate2D = access;
        myLastRow = access.countRows() - 1L;

        myRow = row;
    }

    protected RowView(final Access2D<N> access) {
        this(access, -1L, access.countRows() - 1L);
    }

    RowView(final Access2D<N> access, final long row) {
        this(access, row, access.countRows() - 1L);
    }

    public int characteristics() {
        return CHARACTERISTICS;
    }

    public int compareTo(final RowView<N> other) {
        return Long.compare(myRow, other.row());
    }

    public long count() {
        return myDelegate2D.countColumns();
    }

    public double doubleValue(final long index) {
        return myDelegate2D.doubleValue(myRow, index);
    }

    public long estimateSize() {
        return myLastRow - myRow;
    }

    public void forEachRemaining(final Consumer<? super RowView<N>> action) {
        Iterator.super.forEachRemaining(action);
    }

    public N get(final long index) {
        return myDelegate2D.get(myRow, index);
    }

    public boolean hasNext() {
        return myRow < myLastRow;
    }

    public boolean hasPrevious() {
        return myRow > 0L;
    }

    public RowView<N> iterator() {
        return new RowView<>(myDelegate2D);
    }

    public RowView<N> next() {
        myRow++;
        return this;
    }

    public RowView<N> previous() {
        myRow--;
        return this;
    }

    public final void remove() {
        ProgrammingError.throwForUnsupportedOptionalOperation();
    }

    public long row() {
        return myRow;
    }

    public Stream<RowView<N>> stream() {
        return StreamSupport.stream(this, false);
    }

    @Override
    public final String toString() {
        return Access1D.toString(this);
    }

    public boolean tryAdvance(final Consumer<? super RowView<N>> action) {
        if (this.hasNext()) {
            action.accept(this.next());
            return true;
        } else {
            return false;
        }
    }

    public Spliterator<RowView<N>> trySplit() {

        final long remaining = myLastRow - myRow;

        if (remaining > 1L) {

            final long split = myRow + (remaining / 2L);

            final RowView<N> retVal = new RowView<>(myDelegate2D, myRow, split);

            myRow = split;

            return retVal;

        } else {

            return null;
        }
    }

    protected void setRow(final long row) {
        myRow = row;
    }

}

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
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.ojalgo.ProgrammingError;

public class ColumnView<N extends Number>
        implements Access1D<N>, Iterable<ColumnView<N>>, Iterator<ColumnView<N>>, Spliterator<ColumnView<N>>, Comparable<ColumnView<N>> {

    static final int CHARACTERISTICS = Spliterator.CONCURRENT | Spliterator.DISTINCT | Spliterator.IMMUTABLE | Spliterator.NONNULL | Spliterator.ORDERED
            | Spliterator.SIZED | Spliterator.SORTED | Spliterator.SUBSIZED;

    private long myColumn = -1L;
    private final Access2D<N> myDelegate2D;
    private final long myLastColumn;

    private ColumnView(final Access2D<N> access, final long column, long lastColumn) {

        super();

        myDelegate2D = access;
        myLastColumn = lastColumn;

        myColumn = column;
    }

    protected ColumnView(final Access2D<N> access) {
        this(access, -1L, access.countColumns() - 1L);
    }

    public int characteristics() {
        return CHARACTERISTICS;
    }

    public long column() {
        return myColumn;
    }

    public int compareTo(ColumnView<N> other) {
        return Long.compare(myColumn, other.column());
    }

    public long count() {
        return myDelegate2D.countRows();
    }

    public double doubleValue(final long index) {
        return myDelegate2D.doubleValue(index, myColumn);
    }

    public long estimateSize() {
        return myLastColumn - myColumn;
    }

    public void forEachRemaining(Consumer<? super ColumnView<N>> action) {
        Iterator.super.forEachRemaining(action);
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

    public ColumnView<N> iterator() {
        return new ColumnView<>(myDelegate2D);
    }

    public ColumnView<N> next() {
        myColumn++;
        return this;
    }

    public ColumnView<N> previous() {
        myColumn--;
        return this;
    }

    public final void remove() {
        ProgrammingError.throwForUnsupportedOptionalOperation();
    }

    public Stream<ColumnView<N>> stream(final boolean parallel) {
        return StreamSupport.stream(this, parallel);
    }

    public boolean tryAdvance(Consumer<? super ColumnView<N>> action) {
        if (this.hasNext()) {
            action.accept(this.next());
            return true;
        } else {
            return false;
        }
    }

    public Spliterator<ColumnView<N>> trySplit() {

        final long remaining = myLastColumn - myColumn;

        if (remaining > 1L) {

            final long split = myColumn + (remaining / 2L);

            final ColumnView<N> retVal = new ColumnView<>(myDelegate2D, myColumn, split);

            myColumn = split;

            return retVal;

        } else {

            return null;
        }
    }

    protected void setColumn(final long column) {
        myColumn = column;
    }

}

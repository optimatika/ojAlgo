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

public final class LongRowColumn implements Comparable<LongRowColumn> {

    public final long column;
    public final long row;

    public LongRowColumn(final long aRow, final long aCol) {

        super();

        row = aRow;
        column = aCol;
    }

    @SuppressWarnings("unused")
    private LongRowColumn() {
        this(-1L, -1L);
    }

    public int compareTo(final LongRowColumn ref) {

        if (column == ref.column) {

            return Long.compare(row, ref.row);

        } else {

            return Long.compare(column, ref.column);
        }
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof LongRowColumn)) {
            return false;
        }
        final LongRowColumn other = (LongRowColumn) obj;
        if (column != other.column) {
            return false;
        }
        if (row != other.row) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + (int) (column ^ (column >>> 32));
        result = (prime * result) + (int) (row ^ (row >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "<" + Long.toString(row) + "," + Long.toString(column) + ">";
    }

}

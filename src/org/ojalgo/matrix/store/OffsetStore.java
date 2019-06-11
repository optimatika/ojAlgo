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
package org.ojalgo.matrix.store;

final class OffsetStore<N extends Number> extends SelectingStore<N> {

    private final int myRowOffset, myColumnOffset; // origin/offset

    OffsetStore(final MatrixStore<N> base, final int rowOffset, final int columnOffset) {

        super(base, base.countRows() - rowOffset, base.countColumns() - columnOffset);

        myRowOffset = rowOffset;
        myColumnOffset = columnOffset;
    }

    OffsetStore(final MatrixStore<N> base, final long rowOffset, final long columnOffset) {

        super(base, base.countRows() - rowOffset, base.countColumns() - columnOffset);

        myRowOffset = Math.toIntExact(rowOffset);
        myColumnOffset = Math.toIntExact(columnOffset);
    }

    public double doubleValue(final long row, final long col) {
        return this.base().doubleValue(myRowOffset + row, myColumnOffset + col);
    }

    public N get(final long row, final long col) {
        return this.base().get(myRowOffset + row, myColumnOffset + col);
    }

}

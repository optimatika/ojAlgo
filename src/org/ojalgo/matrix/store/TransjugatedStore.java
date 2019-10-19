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

import org.ojalgo.ProgrammingError;

abstract class TransjugatedStore<N extends Comparable<N>> extends LogicalStore<N> {

    private TransjugatedStore(final MatrixStore<N> base, final int rows, final int columns) {

        super(base, rows, columns);

        ProgrammingError.throwForIllegalInvocation();
    }

    protected TransjugatedStore(final MatrixStore<N> base) {
        super(base, base.countColumns(), base.countRows());
    }

    public final double doubleValue(final long aRow, final long aCol) {
        return this.base().doubleValue(aCol, aRow);
    }

    public final int firstInColumn(final int col) {
        return this.base().firstInRow(col);
    }

    public final int firstInRow(final int row) {
        return this.base().firstInColumn(row);
    }

    public final MatrixStore<N> getOriginal() {
        return this.base();
    }

    @Override
    public final int limitOfColumn(final int col) {
        return this.base().limitOfRow(col);
    }

    @Override
    public final int limitOfRow(final int row) {
        return this.base().limitOfColumn(row);
    }

}

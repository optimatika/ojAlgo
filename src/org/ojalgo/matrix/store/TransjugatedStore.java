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
package org.ojalgo.matrix.store;

import org.ojalgo.ProgrammingError;

abstract class TransjugatedStore<N extends Number> extends LogicalStore<N> {

    private TransjugatedStore(final int rows, final int columns, final MatrixStore<N> base) {

        super(base, rows, columns);

        ProgrammingError.throwForIllegalInvocation();
    }

    protected TransjugatedStore(final MatrixStore<N> base) {
        super(base, (int) base.countColumns(), (int) base.countRows());
    }

    public final double doubleValue(final long aRow, final long aCol) {
        return this.getBase().doubleValue(aCol, aRow);
    }

    public final int firstInColumn(final int col) {
        return this.getBase().firstInRow(col);
    }

    public final int firstInRow(final int row) {
        return this.getBase().firstInColumn(row);
    }

    public final MatrixStore<N> getOriginal() {
        return this.getBase();
    }

    @Override
    public final int limitOfColumn(final int col) {
        return this.getBase().limitOfRow(col);
    }

    @Override
    public final int limitOfRow(final int row) {
        return this.getBase().limitOfColumn(row);
    }

    @Override
    public void supplyTo(final ElementsConsumer<N> consumer) {
        this.supplyNonZerosTo(consumer);
    }

    @Override
    protected void supplyNonZerosTo(final ElementsConsumer<N> consumer) {
        consumer.fillMatching(this);
    }

}

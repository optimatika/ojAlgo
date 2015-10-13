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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.ojalgo.ProgrammingError;
import org.ojalgo.access.Access1D;
import org.ojalgo.scalar.Scalar;

/**
 * A merger of two {@linkplain MatrixStore} instances by placing one store below the other. The two matrices
 * must have the same number of columns. The columns of the two matrices are logically merged to form new
 * longer columns.
 *
 * @author apete
 */
final class AboveBelowStore<N extends Number> extends DelegatingStore<N> {

    private final MatrixStore<N> myBelow;
    private final int mySplit;

    @SuppressWarnings("unused")
    private AboveBelowStore(final MatrixStore<N> base) {

        this(base, null);

        ProgrammingError.throwForIllegalInvocation();
    }

    AboveBelowStore(final MatrixStore<N> base, final MatrixStore<N> below) {

        super((int) (base.countRows() + below.countRows()), (int) base.countColumns(), base);

        myBelow = below;
        mySplit = (int) base.countRows();

        if (base.countColumns() != below.countColumns()) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * @see org.ojalgo.matrix.store.MatrixStore#doubleValue(long, long)
     */
    public double doubleValue(final long row, final long column) {
        return (row >= mySplit) ? myBelow.doubleValue(row - mySplit, column) : this.getBase().doubleValue(row, column);
    }

    public int firstInColumn(final int col) {
        return this.getBase().firstInColumn(col);
    }

    public int firstInRow(final int row) {
        return (row < mySplit) ? this.getBase().firstInRow(row) : myBelow.firstInRow(row);
    }

    public N get(final long row, final long column) {
        return (row >= mySplit) ? myBelow.get(row - mySplit, column) : this.getBase().get(row, column);
    }

    @Override
    public int limitOfColumn(final int col) {
        return myBelow.limitOfColumn(col);
    }

    @Override
    public int limitOfRow(final int row) {
        return (row < mySplit) ? this.getBase().limitOfRow(row) : myBelow.limitOfRow(row);
    }

    @Override
    public MatrixStore<N> multiply(final Access1D<N> right) {

        final Future<MatrixStore<N>> tmpBaseFuture = this.executeMultiplyRightOnBase(right);

        final MatrixStore<N> tmpLower = myBelow.multiply(right);

        try {
            return new AboveBelowStore<N>(tmpBaseFuture.get(), tmpLower);
        } catch (final InterruptedException | ExecutionException ex) {
            return null;
        }
    }

    public Scalar<N> toScalar(final long row, final long column) {
        return (row >= mySplit) ? myBelow.toScalar(row - mySplit, column) : this.getBase().toScalar(row, column);
    }

    @Override
    protected void supplyNonZerosTo(final ElementsConsumer<N> consumer) {
        consumer.regionByLimits(mySplit, this.getColDim()).fillMatching(this.getBase());
        consumer.regionByOffsets(mySplit, 0).fillMatching(myBelow);
    }

}

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
 * A merger of two {@linkplain MatrixStore} instances by placing one store to the right of the other. The two
 * matrices must have the same number of rows. The rows of the two matrices are logically merged to form new
 * longer rows.
 *
 * @author apete
 */
final class LeftRightStore<N extends Number> extends DelegatingStore<N> {

    private final MatrixStore<N> myRight;
    private final int mySplit;

    @SuppressWarnings("unused")
    private LeftRightStore(final MatrixStore<N> base) {

        this(base, null);

        ProgrammingError.throwForIllegalInvocation();
    }

    LeftRightStore(final MatrixStore<N> base, final MatrixStore<N> right) {

        super((int) base.countRows(), (int) (base.countColumns() + right.countColumns()), base);

        myRight = right;
        mySplit = (int) base.countColumns();

        if (base.countRows() != right.countRows()) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * @see org.ojalgo.matrix.store.MatrixStore#doubleValue(long, long)
     */
    public double doubleValue(final long row, final long column) {
        return (column >= mySplit) ? myRight.doubleValue(row, column - mySplit) : this.getBase().doubleValue(row, column);
    }

    public int firstInColumn(final int col) {
        return (col < mySplit) ? this.getBase().firstInColumn(col) : myRight.firstInColumn(col);
    }

    public int firstInRow(final int row) {
        return this.getBase().firstInRow(row);
    }

    public N get(final long row, final long column) {
        return (column >= mySplit) ? myRight.get(row, column - mySplit) : this.getBase().get(row, column);
    }

    @Override
    public int limitOfColumn(final int col) {
        return (col < mySplit) ? this.getBase().limitOfColumn(col) : myRight.limitOfColumn(col);
    }

    @Override
    public int limitOfRow(final int row) {
        return myRight.limitOfRow(row);
    }

    public MatrixStore<N> multiplyLeft(final Access1D<N> leftMtrx) {

        final Future<MatrixStore<N>> tmpBaseFuture = this.executeMultiplyLeftOnBase(leftMtrx);

        final MatrixStore<N> tmpRight = myRight.multiplyLeft(leftMtrx).get();

        try {
            return new LeftRightStore<N>(tmpBaseFuture.get(), tmpRight);
        } catch (final InterruptedException | ExecutionException ex) {
            return null;
        }
    }

    public Scalar<N> toScalar(final long row, final long column) {
        return (column >= mySplit) ? myRight.toScalar(row, column - mySplit) : this.getBase().toScalar(row, column);
    }

    @Override
    protected void supplyNonZerosTo(final ElementsConsumer<N> consumer) {
        consumer.regionByLimits(this.getRowDim(), mySplit).fillMatching(this.getBase());
        consumer.regionByOffsets(0, mySplit).fillMatching(myRight);
    }

}

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
package org.ojalgo.matrix.store;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.ojalgo.access.Access1D;
import org.ojalgo.scalar.Scalar;

/**
 * A merger of two {@linkplain MatrixStore} instances by placing one store below the other. The two matrices
 * must have the same number of columns. The columns of the two matrices are logically merged to form new
 * longer columns.
 *
 * @author apete
 */
final class AboveBelowStore<N extends Number> extends ComposingStore<N> {

    private final MatrixStore<N> myBelow;
    private final int mySplit;

    AboveBelowStore(final MatrixStore<N> base, final MatrixStore<N> below) {

        super(base, (int) (base.countRows() + below.countRows()), (int) base.countColumns());

        myBelow = below;
        mySplit = (int) base.countRows();

        if (base.countColumns() != below.countColumns()) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * @see org.ojalgo.matrix.store.MatrixStore#doubleValue(long, long)
     */
    public double doubleValue(final long row, final long col) {
        return (row >= mySplit) ? myBelow.doubleValue(row - mySplit, col) : this.getBase().doubleValue(row, col);
    }

    public int firstInColumn(final int col) {
        return this.getBase().firstInColumn(col);
    }

    public int firstInRow(final int row) {
        return (row < mySplit) ? this.getBase().firstInRow(row) : myBelow.firstInRow(row - mySplit);
    }

    public N get(final long row, final long col) {
        return (row >= mySplit) ? myBelow.get(row - mySplit, col) : this.getBase().get(row, col);
    }

    @Override
    public int limitOfColumn(final int col) {
        return mySplit + myBelow.limitOfColumn(col);
    }

    @Override
    public int limitOfRow(final int row) {
        return (row < mySplit) ? this.getBase().limitOfRow(row) : myBelow.limitOfRow(row - mySplit);
    }

    public void multiply(final Access1D<N> right, final ElementsConsumer<N> target) {

        final Future<?> futureAbove = this.executeMultiply(right, target.regionByLimits(mySplit, this.getColDim()));

        myBelow.multiply(right, target.regionByOffsets(mySplit, 0));

        try {
            futureAbove.get();
        } catch (final InterruptedException | ExecutionException ex) {
            ex.printStackTrace(System.err);
        }
    }

    public MatrixStore<N> multiply(final double scalar) {

        final Future<MatrixStore<N>> futureAbove = this.executeMultiply(scalar);

        final MatrixStore<N> below = myBelow.multiply(scalar);

        try {
            return new AboveBelowStore<>(futureAbove.get(), below);
        } catch (final InterruptedException | ExecutionException ex) {
            ex.printStackTrace(System.err);
            return null;
        }
    }

    @Override
    public MatrixStore<N> multiply(final MatrixStore<N> right) {

        final Future<MatrixStore<N>> futureAbove = this.executeMultiply(right);

        final MatrixStore<N> below = myBelow.multiply(right);

        try {
            return new AboveBelowStore<>(futureAbove.get(), below);
        } catch (final InterruptedException | ExecutionException ex) {
            ex.printStackTrace(System.err);
            return null;
        }
    }

    public MatrixStore<N> multiply(final N scalar) {

        final Future<MatrixStore<N>> futureAbove = this.executeMultiply(scalar);

        final MatrixStore<N> below = myBelow.multiply(scalar);

        try {
            return new AboveBelowStore<>(futureAbove.get(), below);
        } catch (final InterruptedException | ExecutionException ex) {
            ex.printStackTrace(System.err);
            return null;
        }
    }

    @Override
    public N multiplyBoth(final Access1D<N> leftAndRight) {
        // TODO Auto-generated method stub
        return super.multiplyBoth(leftAndRight);
    }

    public ElementsSupplier<N> premultiply(final Access1D<N> left) {
        // TODO Auto-generated method stub
        return super.premultiply(left);
    }

    @Override
    public void supplyTo(final ElementsConsumer<N> receiver) {
        this.getBase().supplyTo(receiver.regionByLimits(mySplit, this.getColDim()));
        myBelow.supplyTo(receiver.regionByOffsets(mySplit, 0));
    }

    public Scalar<N> toScalar(final long row, final long column) {
        return (row >= mySplit) ? myBelow.toScalar(row - mySplit, column) : this.getBase().toScalar(row, column);
    }

}

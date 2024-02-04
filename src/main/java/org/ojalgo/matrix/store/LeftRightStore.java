/*
 * Copyright 1997-2024 Optimatika
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

import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access1D;

/**
 * A merger of two {@linkplain MatrixStore} instances by placing one store to the right of the other. The two
 * matrices must have the same number of rows. The rows of the two matrices are logically merged to form new
 * longer rows.
 *
 * @author apete
 */
final class LeftRightStore<N extends Comparable<N>> extends ComposingStore<N> {

    private final MatrixStore<N> myRight;
    private final int mySplit;

    LeftRightStore(final MatrixStore<N> base, final MatrixStore<N> right) {

        super(base, base.countRows(), base.countColumns() + right.countColumns());

        myRight = right;
        mySplit = Math.toIntExact(base.countColumns());

        if (base.countRows() != right.countRows()) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * @see org.ojalgo.matrix.store.MatrixStore#doubleValue(long, long)
     */
    @Override
    public double doubleValue(final int row, final int col) {
        return col >= mySplit ? myRight.doubleValue(row, col - mySplit) : this.base().doubleValue(row, col);
    }

    @Override
    public int firstInColumn(final int col) {
        return col < mySplit ? this.base().firstInColumn(col) : myRight.firstInColumn(col - mySplit);
    }

    @Override
    public int firstInRow(final int row) {
        final int baseFirst = this.base().firstInRow(row);
        return baseFirst < mySplit ? baseFirst : mySplit + myRight.firstInRow(row);
    }

    @Override
    public N get(final int row, final int col) {
        return col >= mySplit ? myRight.get(row, col - mySplit) : this.base().get(row, col);
    }

    @Override
    public int limitOfColumn(final int col) {
        return col < mySplit ? this.base().limitOfColumn(col) : myRight.limitOfColumn(col - mySplit);
    }

    @Override
    public int limitOfRow(final int row) {
        final int rightLimit = myRight.limitOfRow(row);
        return rightLimit == 0 ? this.base().limitOfRow(row) : mySplit + rightLimit;
    }

    @Override
    public void multiply(final Access1D<N> right, final TransformableRegion<N> target) {
        // TODO Auto-generated method stub
        super.multiply(right, target);
    }

    @Override
    public MatrixStore<N> multiply(final double scalar) {

        final Future<MatrixStore<N>> futureLeft = this.executeMultiply(scalar);

        final MatrixStore<N> right = myRight.multiply(scalar);

        try {
            return new LeftRightStore<>(futureLeft.get(), right);
        } catch (final InterruptedException | ExecutionException ex) {
            ex.printStackTrace(System.err);
            return null;
        }
    }

    @Override
    public MatrixStore<N> multiply(final MatrixStore<N> right) {
        // TODO Auto-generated method stub
        return super.multiply(right);
    }

    @Override
    public MatrixStore<N> multiply(final N scalar) {

        final Future<MatrixStore<N>> futureLeft = this.executeMultiply(scalar);

        final MatrixStore<N> right = myRight.multiply(scalar);

        try {
            return new LeftRightStore<>(futureLeft.get(), right);
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

    @Override
    public MatrixStore<N> premultiply(final Access1D<N> left) {

        final Future<ElementsSupplier<N>> futureLeft = this.executePremultiply(left);

        final MatrixStore<N> right = myRight.premultiply(left).collect(this.physical());

        try {
            return new LeftRightStore<>(futureLeft.get().collect(this.physical()), right);
        } catch (final InterruptedException | ExecutionException ex) {
            ex.printStackTrace(System.err);
            return null;
        }
    }

    @Override
    public void supplyTo(final TransformableRegion<N> receiver) {
        this.base().supplyTo(receiver.regionByLimits(this.getRowDim(), mySplit));
        myRight.supplyTo(receiver.regionByOffsets(0, mySplit));
    }

    @Override
    public Scalar<N> toScalar(final long row, final long column) {
        return column >= mySplit ? myRight.toScalar(row, column - mySplit) : this.base().toScalar(row, column);
    }

}

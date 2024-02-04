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

import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access1D;

/**
 * SuperimposedStore
 *
 * @author apete
 */
final class SuperimposedStore<N extends Comparable<N>> extends ComposingStore<N> {

    private final int myColFirst;
    private final int myColLimit;
    private final MatrixStore<N> myDiff;
    private final int myRowFirst;
    private final int myRowLimit;

    SuperimposedStore(final MatrixStore<N> base, final long row, final long col, final MatrixStore<N> diff) {

        super(base, base.countRows(), base.countColumns());

        myRowFirst = Math.toIntExact(row);
        myColFirst = Math.toIntExact(col);

        long diffRowDim = diff.countRows();
        long diffColDim = diff.countColumns();

        myRowLimit = Math.toIntExact(row + diffRowDim);
        myColLimit = Math.toIntExact(col + diffColDim);

        myDiff = diff;
    }

    SuperimposedStore(final MatrixStore<N> base, final MatrixStore<N> diff) {
        this(base, 0L, 0L, diff);
    }

    /**
     * @see org.ojalgo.matrix.store.MatrixStore#doubleValue(long, long)
     */
    @Override
    public double doubleValue(final int row, final int col) {

        double retVal = this.base().doubleValue(row, col);

        if (this.isCovered(row, col)) {
            retVal += myDiff.doubleValue(row - myRowFirst, col - myColFirst);
        }

        return retVal;
    }

    @Override
    public N get(final int row, final int col) {

        N retVal = this.base().get(row, col);

        if (this.isCovered(row, col)) {
            retVal = myDiff.toScalar(row - myRowFirst, col - myColFirst).add(retVal).get();
        }

        return retVal;
    }

    @Override
    public void multiply(final Access1D<N> right, final TransformableRegion<N> target) {
        // TODO Auto-generated method stub
        super.multiply(right, target);
    }

    @Override
    public MatrixStore<N> multiply(final double scalar) {
        // TODO Auto-generated method stub
        return super.multiply(scalar);
    }

    @Override
    public MatrixStore<N> multiply(final MatrixStore<N> right) {
        // TODO Auto-generated method stub
        return super.multiply(right);
    }

    @Override
    public MatrixStore<N> multiply(final N scalar) {
        // TODO Auto-generated method stub
        return super.multiply(scalar);
    }

    @Override
    public N multiplyBoth(final Access1D<N> leftAndRight) {
        // TODO Auto-generated method stub
        return super.multiplyBoth(leftAndRight);
    }

    @Override
    public ElementsSupplier<N> premultiply(final Access1D<N> left) {
        // TODO Auto-generated method stub
        return super.premultiply(left);
    }

    @Override
    public void supplyTo(final TransformableRegion<N> consumer) {
        consumer.fillMatching(this.base());
        consumer.regionByLimits(myRowLimit, myColLimit).regionByOffsets(myRowFirst, myColFirst).modifyMatching(this.physical().function().add(), myDiff);
    }

    @Override
    public Scalar<N> toScalar(final long row, final long column) {

        Scalar<N> retVal = this.base().toScalar(row, column);

        if (this.isCovered(row, column)) {
            retVal = retVal.add(myDiff.get(row - myRowFirst, column - myColFirst));
        }

        return retVal;
    }

    private boolean isCovered(final int row, final int col) {
        return myRowFirst <= row && myColFirst <= col && row < myRowLimit && col < myColLimit;
    }

    private boolean isCovered(final long row, final long col) {
        return this.isCovered(Math.toIntExact(row), Math.toIntExact(col));
    }

}

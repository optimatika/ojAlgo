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

import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access1D;

/**
 * SuperimposedStore
 *
 * @author apete
 */
final class SuperimposedStore<N extends Number> extends ComposingStore<N> {

    private final int myColFirst;
    private final int myColLimit;
    private final MatrixStore<N> myDiff;
    private final int myRowFirst;
    private final int myRowLimit;

    SuperimposedStore(final MatrixStore<N> base, final int row, final int column, final MatrixStore<N> diff) {

        super(base, (int) base.countRows(), (int) base.countColumns());

        myRowFirst = row;
        myColFirst = column;

        final int tmpDiffRowDim = (int) diff.countRows();
        final int tmpDiffColDim = (int) diff.countColumns();

        myRowLimit = row + tmpDiffRowDim;
        myColLimit = column + tmpDiffColDim;

        myDiff = diff;
    }

    SuperimposedStore(final MatrixStore<N> base, final MatrixStore<N> diff) {
        this(base, 0, 0, diff);
    }

    /**
     * @see org.ojalgo.matrix.store.MatrixStore#doubleValue(long, long)
     */
    public double doubleValue(final long row, final long col) {

        double retVal = this.getBase().doubleValue(row, col);

        if (this.isCovered((int) row, (int) col)) {
            retVal += myDiff.doubleValue(row - myRowFirst, col - myColFirst);
        }

        return retVal;
    }

    public N get(final long row, final long col) {

        N retVal = this.getBase().get(row, col);

        if (this.isCovered((int) row, (int) col)) {
            retVal = myDiff.toScalar((int) row - myRowFirst, (int) col - myColFirst).add(retVal).get();
        }

        return retVal;
    }

    public void multiply(final Access1D<N> right, final ElementsConsumer<N> target) {
        // TODO Auto-generated method stub
        super.multiply(right, target);
    }

    public MatrixStore<N> multiply(final double scalar) {
        // TODO Auto-generated method stub
        return super.multiply(scalar);
    }

    public MatrixStore<N> multiply(final MatrixStore<N> right) {
        // TODO Auto-generated method stub
        return super.multiply(right);
    }

    public MatrixStore<N> multiply(final N scalar) {
        // TODO Auto-generated method stub
        return super.multiply(scalar);
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

    public void supplyTo(final ElementsConsumer<N> consumer) {
        consumer.fillMatching(this.getBase());
        consumer.regionByLimits(myRowLimit, myColLimit).regionByOffsets(myRowFirst, myColFirst).modifyMatching(this.physical().function().add(), myDiff);
    }

    public Scalar<N> toScalar(final long row, final long column) {

        Scalar<N> retVal = this.getBase().toScalar(row, column);

        if (this.isCovered((int) row, (int) column)) {
            retVal = retVal.add(myDiff.get(row - myRowFirst, column - myColFirst));
        }

        return retVal;
    }

    private final boolean isCovered(final int row, final int column) {
        return (myRowFirst <= row) && (myColFirst <= column) && (row < myRowLimit) && (column < myColLimit);
    }

}

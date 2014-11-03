/*
 * Copyright 1997-2014 Optimatika (www.optimatika.se)
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
package org.ojalgo.matrix.decomposition;

import org.ojalgo.access.Access2D;
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.function.aggregator.PrimitiveAggregator;
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.RawStore;
import org.ojalgo.type.context.NumberContext;

/**
 * This class adapts JAMA's LUDecomposition to ojAlgo's {@linkplain LU} interface.
 *
 * @author apete
 */
public final class RawLU extends RawDecomposition implements LU<Double> {

    private JamaLU myDelegate;

    /**
     * Not recommended to use this constructor directly. Consider using the static factory method
     * {@linkplain org.ojalgo.matrix.decomposition.LUDecomposition#makeJama()} instead.
     */
    public RawLU() {
        super();
    }

    public Double calculateDeterminant(final Access2D<Double> matrix) {
        this.compute(matrix);
        return this.getDeterminant();
    }

    public boolean computeWithoutPivoting(final MatrixStore<?> matrix) {
        return this.compute(matrix);
    }

    public boolean equals(final MatrixStore<Double> aStore, final NumberContext context) {
        return MatrixUtils.equals(aStore, this, context);
    }

    public Double getDeterminant() {
        return myDelegate.det();
    }

    @Override
    public RawStore getInverse() {
        return this.solve(this.makeEyeStore((int) myDelegate.getL().countRows(), (int) myDelegate.getU().countColumns()));
    }

    public RawStore getL() {
        return new RawStore(myDelegate.getL());
    }

    public int[] getPivotOrder() {
        return myDelegate.getPivot();
    }

    public int getRank() {

        int retVal = 0;

        final MatrixStore<Double> tmpU = this.getU();
        final int tmpMinDim = (int) Math.min(tmpU.countRows(), tmpU.countColumns());

        final AggregatorFunction<Double> tmpLargest = PrimitiveAggregator.LARGEST.get();
        tmpU.visitDiagonal(0L, 0L, tmpLargest);
        final double tmpLargestValue = tmpLargest.doubleValue();

        for (int ij = 0; ij < tmpMinDim; ij++) {
            if (!tmpU.isSmall(ij, ij, tmpLargestValue)) {
                retVal++;
            }
        }

        return retVal;
    }

    public int[] getReducedPivots() {
        // TODO Auto-generated method stub
        return null;
    }

    public RawStore getU() {
        return new RawStore(myDelegate.getU());
    }

    public boolean isAspectRatioNormal() {
        return (int) myDelegate.getL().countRows() >= (int) myDelegate.getU().countColumns();
    }

    public boolean isComputed() {
        return myDelegate != null;
    }

    public boolean isFullSize() {
        return false;
    }

    public boolean isSolvable() {
        return (myDelegate != null) && myDelegate.isNonsingular();
    }

    public boolean isSquareAndNotSingular() {
        return (myDelegate != null) && ((int) myDelegate.getL().countRows() == (int) myDelegate.getU().countColumns()) && myDelegate.isNonsingular();
    }

    public MatrixStore<Double> reconstruct() {
        return MatrixUtils.reconstruct(this);
    }

    public void reset() {
        myDelegate = null;
    }

    @Override
    public String toString() {
        return myDelegate.toString();
    }

    @Override
    boolean compute(final RawStore aDelegate) {

        myDelegate = new JamaLU(aDelegate);

        return this.isComputed();
    }

    @Override
    RawStore solve(final RawStore aRHS) {
        return myDelegate.solve(aRHS);
    }

}

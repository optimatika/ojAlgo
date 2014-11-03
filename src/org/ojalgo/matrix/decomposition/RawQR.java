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
 * This class adapts JAMA's QRDecomposition to ojAlgo's {@linkplain QR} interface.
 *
 * @author apete
 */
public final class RawQR extends RawDecomposition implements QR<Double> {

    private JamaQR myDelegate;

    /**
     * Not recommended to use this constructor directly. Consider using the static factory method
     * {@linkplain org.ojalgo.matrix.decomposition.QRDecomposition#makeJama()} instead.
     */
    public RawQR() {
        super();
    }

    public Double calculateDeterminant(final Access2D<Double> matrix) {
        this.compute(matrix);
        return this.getDeterminant();
    }

    public boolean compute(final Access2D<?> matrix, final boolean fullSize) {
        if (fullSize) {
            throw new IllegalArgumentException("Cannot do full size!");
        } else {
            return this.compute(matrix);
        }
    }

    public boolean equals(final MatrixStore<Double> aStore, final NumberContext context) {
        return MatrixUtils.equals(aStore, this, context);
    }

    public Double getDeterminant() {

        final AggregatorFunction<Double> tmpAggrFunc = PrimitiveAggregator.getCollection().product();

        this.getR().visitDiagonal(0, 0, tmpAggrFunc);

        return tmpAggrFunc.getNumber();
    }

    @Override
    public RawStore getInverse() {
        return this.solve(this.makeEyeStore((int) myDelegate.getQ().countRows(), (int) myDelegate.getR().countColumns()));
    }

    public RawStore getQ() {
        return new RawStore(myDelegate.getQ());
    }

    public RawStore getR() {
        return new RawStore(myDelegate.getR());
    }

    public int getRank() {

        int retVal = 0;

        final MatrixStore<Double> tmpR = this.getR();
        final int tmpMinDim = (int) Math.min(tmpR.countRows(), tmpR.countColumns());

        final AggregatorFunction<Double> tmpLargest = PrimitiveAggregator.LARGEST.get();
        tmpR.visitDiagonal(0L, 0L, tmpLargest);
        final double tmpLargestValue = tmpLargest.doubleValue();

        for (int ij = 0; ij < tmpMinDim; ij++) {
            if (!tmpR.isSmall(ij, ij, tmpLargestValue)) {
                retVal++;
            }
        }

        return retVal;
    }

    public boolean isAspectRatioNormal() {
        return (int) myDelegate.getQ().countRows() >= (int) myDelegate.getR().countColumns();

    }

    public boolean isComputed() {
        return myDelegate != null;
    }

    public boolean isFullColumnRank() {
        return this.isSolvable();
    }

    public boolean isFullSize() {
        return false;
    }

    public boolean isSolvable() {
        return (myDelegate != null) && myDelegate.isFullRank();
    }

    public MatrixStore<Double> reconstruct() {
        return MatrixUtils.reconstruct(this);
    }

    public void reset() {
        myDelegate = null;
    }

    public MatrixStore<Double> solveConjugated(final Access2D<Double> rhs) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String toString() {
        return myDelegate.toString();
    }

    @Override
    boolean compute(final RawStore aDelegate) {

        myDelegate = new JamaQR(aDelegate);

        return this.isComputed();
    }

    @Override
    RawStore solve(final RawStore aRHS) {
        return myDelegate.solve(aRHS);
    }

}

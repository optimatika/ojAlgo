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
package org.ojalgo.matrix.decomposition;

import org.ojalgo.access.Access2D;
import org.ojalgo.access.Structure2D;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.matrix.store.RawStore;
import org.ojalgo.type.context.NumberContext;

/**
 * In many ways similar to InPlaceDecomposition but this class is hardwired to work with double[][] data. Most
 * of it's originates from JAMA, but have been significantly refactored or even (re)written from scratch.
 *
 * @author apete
 */
abstract class RawDecomposition extends AbstractDecomposition<Double> {

    private int myColDim;
    private double[][] myRawInPlaceData;
    private RawStore myRawInPlaceStore;
    private int myRowDim;

    protected RawDecomposition() {
        super();
    }

    @Override
    protected PrimitiveDenseStore allocate(final long numberOfRows, final long numberOfColumns) {
        // TODO Should use RawStore.FACTORY rather than PrimitiveDenseStore.FACTORY
        return PrimitiveDenseStore.FACTORY.makeZero(numberOfRows, numberOfColumns);
    }

    protected boolean checkSymmetry() {
        boolean retVal = myRowDim == myColDim;
        for (int i = 0; retVal && (i < myRowDim); i++) {
            for (int j = 0; retVal && (j < i); j++) {
                retVal &= (NumberContext.compare(myRawInPlaceData[i][j], myRawInPlaceData[j][i]) == 0);
            }
        }
        return retVal;
    }

    @SuppressWarnings("unchecked")
    protected final MatrixStore<Double> collect(final Access2D.Collectable<Double, ? super DecompositionStore<Double>> source) {
        // TODO Should use RawStore.FACTORY rather than PrimitiveDenseStore.FACTORY
        if (source instanceof MatrixStore) {
            return (MatrixStore<Double>) source;
        } else if (source instanceof Access2D) {
            return PrimitiveDenseStore.FACTORY.builder().makeWrapper((Access2D<?>) source).get();
        } else {
            return source.collect(PrimitiveDenseStore.FACTORY);
        }
    }

    protected int getColDim() {
        return myColDim;
    }

    @Override
    protected double getDimensionalEpsilon() {
        return this.getMaxDim() * PrimitiveMath.MACHINE_EPSILON;
    }

    protected int getMaxDim() {
        return Math.max(myRowDim, myColDim);
    }

    protected int getMinDim() {
        return Math.min(myRowDim, myColDim);
    }

    protected double[][] getRawInPlaceData() {
        return myRawInPlaceData;
    }

    protected RawStore getRawInPlaceStore() {
        return myRawInPlaceStore;
    }

    protected int getRowDim() {
        return myRowDim;
    }

    double[][] reset(final Structure2D matrix, final boolean transpose) {

        this.reset();

        final int tmpInputRowDim = (int) matrix.countRows();
        final int tmpInputColDim = (int) matrix.countColumns();

        final int tmpInPlaceRowDim = transpose ? tmpInputColDim : tmpInputRowDim;
        final int tmpInPlaceColDim = transpose ? tmpInputRowDim : tmpInputColDim;

        if ((myRawInPlaceData == null) || (myRowDim != tmpInputRowDim) || (myColDim != tmpInputColDim)) {

            myRawInPlaceStore = RawStore.FACTORY.makeZero(tmpInPlaceRowDim, tmpInPlaceColDim);
            myRawInPlaceData = myRawInPlaceStore.data;

            myRowDim = tmpInputRowDim;
            myColDim = tmpInputColDim;
        }

        this.aspectRatioNormal(tmpInputRowDim >= tmpInputColDim);

        return myRawInPlaceData;
    }

}

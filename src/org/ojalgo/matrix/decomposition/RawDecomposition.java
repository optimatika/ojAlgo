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
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.matrix.store.RawStore;

/**
 * In many ways similar to InPlaceDecomposition but this class is hardwired to work with double[][] data.
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

    public final MatrixStore<Double> invert(final MatrixStore<Double> original) {
        this.compute(original);
        return this.getInverse();
    }

    public final MatrixStore<Double> invert(final MatrixStore<Double> original, final DecompositionStore<Double> preallocated) {
        this.compute(original);
        return this.getInverse(preallocated);
    }

    public final MatrixStore<Double> solve(final Access2D<Double> body, final Access2D<Double> rhs) {
        this.compute(body);
        return this.solve(rhs);
    }

    public final MatrixStore<Double> solve(final Access2D<Double> body, final Access2D<Double> rhs, final DecompositionStore<Double> preallocated) {
        this.compute(body);
        return this.solve(rhs, preallocated);
    }

    /**
     * Makes no use of <code>preallocated</code> at all. Simply delegates to {@link #solve(Access2D)}.
     *
     * @see org.ojalgo.matrix.decomposition.MatrixDecomposition#solve(Access2D,
     *      org.ojalgo.matrix.decomposition.DecompositionStore)
     */
    public MatrixStore<Double> solve(final Access2D<Double> rhs, final DecompositionStore<Double> preallocated) {
        return this.solve(rhs);
    }

    protected final int getColDim() {
        return myColDim;
    }

    protected final int getMinDim() {
        return Math.min(myRowDim, myColDim);
    }

    protected final double[][] getRawInPlaceData() {
        return myRawInPlaceData;
    }

    protected final RawStore getRawInPlaceStore() {
        return myRawInPlaceStore;
    }

    protected final int getRowDim() {
        return myRowDim;
    }

    protected RawStore makeEyeStore(final int aRowDim, final int aColDim) {
        return new RawStore(RawStore.FACTORY.makeEye(aRowDim, aColDim));
    }

    @Override
    protected final DecompositionStore<Double> preallocate(final long numberOfRows, final long numberOfColumns) {
        return PrimitiveDenseStore.FACTORY.makeZero(numberOfRows, numberOfColumns);
    }

    protected final double[][] setRawInPlace(final Access2D<?> matrix) {

        final int tmpRowDim = (int) matrix.countRows();
        final int tmpColDim = (int) matrix.countColumns();

        if ((myRawInPlaceData == null) || (myRowDim != tmpRowDim) || (myColDim != tmpColDim)) {

            myRawInPlaceStore = RawStore.FACTORY.makeZero(tmpRowDim, tmpColDim);
            myRawInPlaceData = myRawInPlaceStore.data;

            myRowDim = tmpRowDim;
            myColDim = tmpColDim;
        }

        this.copy(matrix, tmpRowDim, tmpColDim, myRawInPlaceData);

        this.aspectRatioNormal(tmpRowDim >= tmpColDim);
        this.computed(false);

        return myRawInPlaceData;
    }

    /**
     * Possible to override to, possibly, only copy part of the matrix (or transpose it)
     */
    void copy(final Access2D<?> source, final int rows, final int columns, final double[][] destination) {
        MatrixUtils.copy(source, rows, columns, destination);
    }

}

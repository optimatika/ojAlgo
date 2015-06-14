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
package org.ojalgo.matrix.decomposition;

import org.ojalgo.access.Access2D;
import org.ojalgo.array.ArrayUtils;
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

    protected static RawStore cast(final Access2D<?> access) {
        if (access instanceof RawStore) {
            return ((RawStore) access);
        } else {
            return new RawStore(ArrayUtils.toRawCopyOf(access));
        }
    }

    static double[][] extract(final Access2D<?> access) {
        if (access instanceof RawStore) {
            return ((RawStore) access).data;
        } else {
            return ArrayUtils.toRawCopyOf(access);
        }
    }

    private int myColDim;
    private double[][] myRawInPlaceData;
    private RawStore myRawInPlaceStore;
    private int myRowDim;

    protected RawDecomposition() {
        super();
    }

    public MatrixStore<Double> getInverse() {
        return this.getInverse(this.preallocate(this.getColDim(), this.getColDim()));
    }

    public final MatrixStore<Double> getInverse(final DecompositionStore<Double> preallocated) {
        return this.getInverse((PrimitiveDenseStore) preallocated);
    }

    public final MatrixStore<Double> invert(final MatrixStore<Double> original) {
        this.decompose(original);
        return this.getInverse();
    }

    public final MatrixStore<Double> invert(final MatrixStore<Double> original, final DecompositionStore<Double> preallocated) {
        this.decompose(original);
        return this.getInverse(preallocated);
    }

    public DecompositionStore<Double> preallocate(final Access2D<Double> template) {
        final long tmpCountRows = template.countRows();
        return this.preallocate(tmpCountRows, tmpCountRows);
    }

    public DecompositionStore<Double> preallocate(final Access2D<Double> templateBody, final Access2D<Double> templateRHS) {
        return this.preallocate(templateRHS.countRows(), templateRHS.countColumns());
    }

    public final MatrixStore<Double> solve(final Access2D<Double> rhs) {
        return this.solve(rhs, this.preallocate(this.getRawInPlaceStore(), rhs));
    }

    public final MatrixStore<Double> solve(final Access2D<Double> body, final Access2D<Double> rhs) {
        this.decompose(body);
        return this.solve(rhs);
    }

    public final MatrixStore<Double> solve(final Access2D<Double> body, final Access2D<Double> rhs, final DecompositionStore<Double> preallocated) {
        this.decompose(body);
        return this.solve(rhs, preallocated);
    }

    public final MatrixStore<Double> solve(final Access2D<Double> rhs, final DecompositionStore<Double> preallocated) {
        return this.solve(rhs, (PrimitiveDenseStore) preallocated);
    }

    protected final int getColDim() {
        return myColDim;
    }

    protected abstract MatrixStore<Double> getInverse(final PrimitiveDenseStore preallocated);

    protected final int getMaxDim() {
        return Math.max(myRowDim, myColDim);
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
    protected final PrimitiveDenseStore preallocate(final long numberOfRows, final long numberOfColumns) {
        return PrimitiveDenseStore.FACTORY.makeZero(numberOfRows, numberOfColumns);
    }

    protected final double[][] setRawInPlace(final Access2D<?> matrix, final boolean transpose) {

        final int tmpRowDim = (int) matrix.countRows();
        final int tmpColDim = (int) matrix.countColumns();

        if ((myRawInPlaceData == null) || (myRowDim != tmpRowDim) || (myColDim != tmpColDim)) {

            myRawInPlaceStore = RawStore.FACTORY.makeZero(transpose ? tmpColDim : tmpRowDim, transpose ? tmpRowDim : tmpColDim);
            myRawInPlaceData = myRawInPlaceStore.data;

            myRowDim = tmpRowDim;
            myColDim = tmpColDim;
        }

        if (transpose) {
            this.transpose(matrix, tmpRowDim, tmpColDim, myRawInPlaceData);
        } else {
            this.copy(matrix, tmpRowDim, tmpColDim, myRawInPlaceData);
        }

        this.aspectRatioNormal(tmpRowDim >= tmpColDim);
        this.computed(false);

        return myRawInPlaceData;
    }

    protected abstract MatrixStore<Double> solve(final Access2D<Double> rhs, final PrimitiveDenseStore preallocated);

    /**
     * Possible to override to, possibly, only copy part of the matrix.
     */
    void copy(final Access2D<?> source, final int rows, final int columns, final double[][] destination) {
        MatrixUtils.copy(source, rows, columns, destination);
    }

    /**
     * Possible to override to, possibly, only copy/transpose part of the matrix.
     */
    void transpose(final Access2D<?> source, final int rows, final int columns, final double[][] destination) {
        for (int j = 0; j < columns; j++) {
            final double[] tmpColumn = destination[j];
            for (int i = 0; i < rows; i++) {
                tmpColumn[i] = source.doubleValue(i, j);
            }
        }
    }

    protected final boolean checkSymmetry() {
        boolean retVal = myRowDim == myColDim;
        for (int i = 0; retVal && (i < myRowDim); i++) {
            for (int j = 0; retVal && (j < i); j++) {
                retVal &= (myRawInPlaceData[i][j] == myRawInPlaceData[j][i]);
            }
        }
        return retVal;
    }

}

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
import org.ojalgo.access.Structure2D;
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.matrix.store.ElementsSupplier;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.MatrixStore.Builder;
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

    public MatrixStore<Double> getInverse() {
        return this.doGetInverse(this.preallocate(this.getColDim(), this.getColDim()));
    }

    public final MatrixStore<Double> getInverse(final DecompositionStore<Double> preallocated) {
        return this.doGetInverse((PrimitiveDenseStore) preallocated);
    }

    public final MatrixStore<Double> invert(final Access2D<?> original) {
        return this.invert(original, this.preallocate(original));
    }

    public abstract MatrixStore<Double> invert(final Access2D<?> original, final DecompositionStore<Double> preallocated);

    public DecompositionStore<Double> preallocate(final Structure2D template) {
        final long tmpCountRows = template.countRows();
        return this.preallocate(tmpCountRows, tmpCountRows);
    }

    public DecompositionStore<Double> preallocate(final Structure2D templateBody, final Structure2D templateRHS) {
        return this.preallocate(templateRHS.countRows(), templateRHS.countColumns());
    }

    public final MatrixStore<Double> solve(final Access2D<?> body, final Access2D<?> rhs) {
        return this.solve(body, rhs, this.preallocate(body, rhs));
    }

    public abstract MatrixStore<Double> solve(final Access2D<?> body, final Access2D<?> rhs, final DecompositionStore<Double> preallocated);

    public final MatrixStore<Double> solve(final ElementsSupplier<Double> rhs) {
        return this.solve(rhs, this.preallocate(this.getRawInPlaceStore(), rhs));
    }

    public abstract MatrixStore<Double> solve(final ElementsSupplier<Double> rhs, final DecompositionStore<Double> preallocated);

    protected final boolean checkSymmetry() {
        boolean retVal = myRowDim == myColDim;
        for (int i = 0; retVal && (i < myRowDim); i++) {
            for (int j = 0; retVal && (j < i); j++) {
                retVal &= (myRawInPlaceData[i][j] == myRawInPlaceData[j][i]);
            }
        }
        return retVal;
    }

    protected abstract MatrixStore<Double> doGetInverse(final PrimitiveDenseStore preallocated);

    protected final int getColDim() {
        return myColDim;
    }

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

    @Override
    protected final PrimitiveDenseStore preallocate(final long numberOfRows, final long numberOfColumns) {
        return PrimitiveDenseStore.FACTORY.makeZero(numberOfRows, numberOfColumns);
    }

    /**
     * Possible to override to, possibly, only copy part of the matrix.
     */
    void copy(final Access2D<?> source, final int rows, final int columns, final double[][] destination) {
        MatrixUtils.copy(source, rows, columns, destination);
    }

    final double[][] reset(final Structure2D matrix, final boolean transpose) {

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

}

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

import static org.ojalgo.constant.PrimitiveMath.*;

import org.ojalgo.access.Access2D;
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.matrix.store.LowerHermitianStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.RawStore;
import org.ojalgo.matrix.store.operation.DotProduct;
import org.ojalgo.type.context.NumberContext;

/**
 * This class adapts JAMA's CholeskyDecomposition to ojAlgo's {@linkplain Cholesky} interface.
 *
 * @deprecated v38 This class will be made package private. Use the inteface instead.
 * @author apete
 */
@Deprecated
public final class RawCholesky extends RawDecomposition implements Cholesky<Double> {

    private boolean mySPD = false;

    /**
     * Not recommended to use this constructor directly. Consider using the static factory method
     * {@linkplain org.ojalgo.matrix.decomposition.Cholesky#makeJama()} instead.
     */
    public RawCholesky() {
        super();
    }

    public boolean compute(final Access2D<?> matrix) {

        this.reset();

        final double[][] tmpData = this.setRawInPlace(matrix);

        final int tmpDiagDim = this.getRowDim();
        mySPD = (this.getColDim() == tmpDiagDim);

        double[] tmpRowIJ;
        double[] tmpRowI;

        // Main loop.
        for (int ij = 0; ij < tmpDiagDim; ij++) { // For each row/column, along the diagonal
            tmpRowIJ = tmpData[ij];

            final double tmpD = tmpRowIJ[ij] = Math.sqrt(Math.max(matrix.doubleValue(ij, ij) - DotProduct.invoke(tmpRowIJ, tmpRowIJ, ij), ZERO));
            mySPD &= (tmpD > ZERO);

            for (int i = ij + 1; i < tmpDiagDim; i++) { // Update column below current row
                tmpRowI = tmpData[i];

                tmpRowI[ij] = (matrix.doubleValue(i, ij) - DotProduct.invoke(tmpRowI, tmpRowIJ, ij)) / tmpD;
            }
        }

        return this.computed(true);
    }

    public boolean compute(final Access2D<?> matrix, final boolean checkHermitian) {

        if (checkHermitian) {
            mySPD = MatrixUtils.isHermitian(matrix);
        }

        if (mySPD) {
            return this.compute(matrix);
        } else {
            return this.computed(false);
        }
    }

    public boolean equals(final MatrixStore<Double> aStore, final NumberContext context) {
        return MatrixUtils.equals(aStore, this, context);
    }

    public Double getDeterminant() {

        final double[][] tmpData = this.getRawInPlaceData();

        final int tmpMinDim = this.getMinDim();

        double retVal = 1.0;
        double tmpVal;
        for (int ij = 0; ij < tmpMinDim; ij++) {
            tmpVal = tmpData[ij][ij];
            retVal *= tmpVal * tmpVal;
        }

        return retVal;
    }

    public final MatrixStore<Double> getInverse() {
        return this.getInverse(this.preallocate(this.getRowDim(), this.getRowDim()));
    }

    public MatrixStore<Double> getL() {
        return this.getRawInPlaceStore().builder().triangular(false, false).build();
    }

    public boolean isFullSize() {
        return true;
    }

    public boolean isSolvable() {
        return this.isComputed() && this.isSPD();
    }

    public MatrixStore<Double> solve(final Access2D<Double> rhs) {
        return this.solve(rhs, this.preallocate(this.getRawInPlaceStore(), rhs));
    }

    @Override
    public final MatrixStore<Double> solve(final Access2D<Double> rhs, final DecompositionStore<Double> preallocated) {

        preallocated.fillMatching(rhs);

        final RawStore tmpBody = this.getRawInPlaceStore();

        preallocated.substituteForwards(tmpBody, false, false, false);
        preallocated.substituteBackwards(tmpBody, false, true, false);

        return preallocated;
    }

    public boolean isSPD() {
        return mySPD;
    }

    public final MatrixStore<Double> getInverse(final DecompositionStore<Double> preallocated) {

        final RawStore tmpBody = this.getRawInPlaceStore();

        preallocated.substituteForwards(tmpBody, false, false, true);
        preallocated.substituteBackwards(tmpBody, false, true, true);

        return new LowerHermitianStore<>(preallocated);
    }

    public MatrixStore<Double> reconstruct() {
        return MatrixUtils.reconstruct(this);
    }

    /**
     * Will only copy the lower/left part of the matrix
     *
     * @see org.ojalgo.matrix.decomposition.OldRawDecomposition#copy(org.ojalgo.access.Access2D, int, int,
     *      double[][])
     */
    @Override
    void copy(final Access2D<?> source, final int rows, final int columns, final double[][] destination) {
        //        for (int i = 0; i < rows; i++) {
        //            final double[] tmpRow = destination[i];
        //            for (int j = 0; j <= i; j++) {
        //                tmpRow[j] = source.doubleValue(i, j);
        //            }
        //        }
    }

}

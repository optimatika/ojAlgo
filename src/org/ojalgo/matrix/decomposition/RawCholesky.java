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

import org.ojalgo.ProgrammingError;
import org.ojalgo.access.Access2D;
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.matrix.store.IdentityStore;
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

    public Double calculateDeterminant(final Access2D<Double> matrix) {
        this.compute(matrix);
        return this.getDeterminant();
    }

    public boolean compute(final Access2D<?> matrix) {

        this.reset();

        final double[][] tmpData = this.setRawInPlace(matrix);

        final int tmpRowDim = this.getRowDim();
        mySPD = (this.getColDim() == tmpRowDim);

        // Main loop.
        for (int i = 0; i < tmpRowDim; i++) { // For each row
            final double[] tmpRowI = tmpData[i];
            double tmpVal = ZERO;
            for (int k = 0; k < i; k++) { // For each previous row
                final double[] tmpRowK = tmpData[k];
                double tmpDotProd = DotProduct.invoke(tmpRowI, tmpRowK, k);
                tmpRowI[k] = tmpDotProd = (tmpRowI[k] - tmpDotProd) / tmpRowK[k];
                tmpVal += (tmpDotProd * tmpDotProd);
            }
            tmpVal = tmpData[i][i] - tmpVal;
            mySPD &= (tmpVal > ZERO);
            tmpData[i][i] = Math.sqrt(Math.max(tmpVal, ZERO));
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

    @Override
    public MatrixStore<Double> getInverse() {
        return this.solve(IdentityStore.PRIMITIVE.make(this.getRowDim()));
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

        preallocated.substituteForwards(tmpBody, false, false);
        preallocated.substituteBackwards(tmpBody, true);

        return preallocated;
    }

    public boolean isSPD() {
        return mySPD;
    }

    public MatrixStore<Double> reconstruct() {
        return MatrixUtils.reconstruct(this);
    }

    /**
     * Will only copy the lower/left part of the matrix
     *
     * @see org.ojalgo.matrix.decomposition.RawDecomposition#copy(org.ojalgo.access.Access2D, int, int,
     *      double[][])
     */
    @Override
    void copy(final Access2D<?> source, final int rows, final int columns, final double[][] destination) {
        for (int i = 0; i < rows; i++) {
            final double[] tmpRow = destination[i];
            for (int j = 0; j <= i; j++) {
                tmpRow[j] = source.doubleValue(i, j);
            }
        }
    }

    @Override
    protected boolean compute(final RawStore matrix) {
        ProgrammingError.throwForIllegalInvocation();
        return false;
    }

    @Override
    RawStore solve(final RawStore rhs) {
        // TODO Auto-generated method stub
        return null;
    }
}

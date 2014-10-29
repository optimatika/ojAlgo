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
import org.ojalgo.access.AccessUtils;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.RawStore;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.type.context.NumberContext;

/**
 * This class adapts JAMA's CholeskyDecomposition to ojAlgo's {@linkplain Cholesky} interface.
 *
 * @author apete
 */
public final class RawCholesky extends RawDecomposition implements Cholesky<Double> {

    private JamaCholesky myDelegate;

    /**
     * Not recommended to use this constructor directly. Consider using the static factory method
     * {@linkplain org.ojalgo.matrix.decomposition.CholeskyDecomposition#makeJama()} instead.
     */
    public RawCholesky() {
        super();
    }

    public Double calculateDeterminant(final Access2D<Double> matrix) {
        this.compute(matrix);
        return this.getDeterminant();
    }

    public boolean compute(final Access2D<?> matrix, final boolean checkHermitian) {
        return this.compute(matrix);
    }

    public boolean computeWithCheck(final MatrixStore<?> aStore) {
        return this.compute(aStore);
    }

    public boolean equals(final MatrixStore<Double> aStore, final NumberContext context) {
        return MatrixUtils.equals(aStore, this, context);
    }

    public RawStore getD() {

        final RawStore tmpL = myDelegate.getL();

        final int tmpRowDim = tmpL.getRowDimension();
        final int tmpColDim = tmpL.getColumnDimension();
        final int tmpMinDim = Math.min(tmpRowDim, tmpColDim);

        final RawStore retVal = new RawStore(new RawStore(tmpRowDim, tmpColDim));

        double tmpVal;
        for (int ij = 0; ij < tmpMinDim; ij++) {
            tmpVal = tmpL.get(ij, ij);
            retVal.update(ij, ij, tmpVal * tmpVal);
        }

        return retVal;
    }

    public Double getDeterminant() {

        final MatrixStore<Double> tmpD = this.getD();
        final int tmpMinDim = (int) tmpD.countRows();

        Scalar<Double> retVal = tmpD.toScalar(0, 0);
        for (int ij = 1; ij < tmpMinDim; ij++) {
            retVal = retVal.multiply(tmpD.get(ij, ij));
        }

        return retVal.getNumber();
    }

    @Override
    public RawStore getInverse() {
        return this.solve(this.makeEyeStore(myDelegate.getL().getRowDimension(), myDelegate.getL().getColumnDimension()));
    }

    public RawStore getL() {
        return new RawStore(myDelegate.getL());
    }

    public RawStore getOldL() {

        final RawStore tmpL = myDelegate.getL();

        final int tmpRowDim = tmpL.getRowDimension();
        final int tmpColDim = tmpL.getColumnDimension();

        final RawStore retVal = new RawStore(new RawStore(tmpRowDim, tmpColDim));

        double tmpDiagVal;
        for (int j = 0; j < tmpColDim; j++) {
            tmpDiagVal = tmpL.get(j, j);
            for (int i = j; i < tmpRowDim; i++) {
                retVal.update(i, j, tmpL.get(i, j) / tmpDiagVal);
            }
        }

        return retVal;
    }

    public RawStore getOldU() {
        return this.getOldL().transpose();
    }

    public RawStore getP() {
        return this.makeEyeStore(myDelegate.getL().getRowDimension(), myDelegate.getL().getRowDimension());
        //return MatrixUtils.makeIdentity(PrimitiveDenseStore.FACTORY, myDelegate.getL().getRowDimension());
    }

    public int[] getPivotOrder() {
        return AccessUtils.makeIncreasingRange(0, this.getOldL().getRowDim());
    }

    public RawStore getR() {
        return new RawStore(myDelegate.getL().transpose());
    }

    public int getRank() {

        int retVal = 0;

        final MatrixStore<Double> tmpD = this.getD();
        final int tmpMinDim = (int) tmpD.countRows();

        for (int ij = 0; ij < tmpMinDim; ij++) {
            if (!tmpD.toScalar(ij, ij).isSmall(PrimitiveMath.ONE)) {
                retVal++;
            }
        }

        return retVal;
    }

    public RawStore getRowEchelonForm() {
        return this.getOldU();
    }

    public boolean isAspectRatioNormal() {
        return true;
    }

    public boolean isComputed() {
        return myDelegate != null;
    }

    public boolean isFullSize() {
        return true;
    }

    public boolean isSingular() {

        boolean retVal = true;

        final MatrixStore<Double> tmpD = this.getD();
        final int tmpMinDim = (int) tmpD.countRows();

        for (int ij = 0; retVal && (ij < tmpMinDim); ij++) {
            retVal &= !tmpD.toScalar(ij, ij).isSmall(PrimitiveMath.ONE);
        }

        return !retVal;
    }

    public boolean isSolvable() {
        return (myDelegate != null) && myDelegate.isSPD();
    }

    public boolean isSPD() {
        return this.isSolvable();
    }

    public boolean isSquareAndNotSingular() {

        boolean retVal = true;

        final MatrixStore<Double> tmpD = this.getD();
        final int tmpMinDim = (int) tmpD.countRows();

        for (int ij = 0; retVal && (ij < tmpMinDim); ij++) {
            retVal &= !tmpD.toScalar(ij, ij).isSmall(PrimitiveMath.ONE);
        }

        return retVal;
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
        myDelegate = new JamaCholesky(aDelegate);
        return myDelegate.isSPD();
    }

    @Override
    RawStore solve(final RawStore aRHS) {
        return myDelegate.solve(aRHS);
    }

}

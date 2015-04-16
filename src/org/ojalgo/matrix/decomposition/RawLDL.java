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

import static org.ojalgo.constant.PrimitiveMath.*;

import org.ojalgo.access.Access2D;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.MatrixStore.Builder;
import org.ojalgo.matrix.store.RawStore;
import org.ojalgo.matrix.store.operation.DotProduct;
<<<<<<< HEAD
=======
import org.ojalgo.type.context.NumberContext;
>>>>>>> origin/master

final class RawLDL extends RawDecomposition implements LDL<Double> {

    private boolean mySPD = false;
<<<<<<< HEAD
=======

    public RawLDL() {
        super();
    }

    public boolean compute(final Access2D<?> matrix) {

        this.reset();

        final double[][] tmpData = this.setRawInPlace(matrix);

        final int tmpDiagDim = this.getRowDim();
        mySPD = (this.getColDim() == tmpDiagDim);

        final double[] tmpRowIJ = new double[tmpDiagDim];
        double[] tmpRowI;

        // Main loop.
        for (int ij = 0; ij < tmpDiagDim; ij++) { // For each row/column, along the diagonal
            tmpRowI = tmpData[ij];

            for (int j = 0; j < ij; j++) {
                tmpRowIJ[j] = tmpRowI[j] * tmpData[j][j];
            }

            final double tmpD = tmpRowI[ij] = matrix.doubleValue(ij, ij) - DotProduct.invoke(tmpRowI, tmpRowIJ, ij);
            mySPD &= (tmpD > ZERO);

            for (int i = ij + 1; i < tmpDiagDim; i++) { // Update column below current row
                tmpRowI = tmpData[i];

                tmpRowI[ij] = (matrix.doubleValue(i, ij) - DotProduct.invoke(tmpRowI, tmpRowIJ, ij)) / tmpD;
            }
        }

        return this.computed(true);
    }
>>>>>>> origin/master

    RawLDL() {
        super();
    }

<<<<<<< HEAD
    public boolean compute(final Access2D<?> matrix) {

        this.reset();

        final double[][] tmpData = this.setRawInPlace(matrix);

        final int tmpDiagDim = this.getRowDim();
        mySPD = (this.getColDim() == tmpDiagDim);

        final double[] tmpRowIJ = new double[tmpDiagDim];
        double[] tmpRowI;

        // Main loop.
        for (int ij = 0; ij < tmpDiagDim; ij++) { // For each row/column, along the diagonal
            tmpRowI = tmpData[ij];

            for (int j = 0; j < ij; j++) {
                tmpRowIJ[j] = tmpRowI[j] * tmpData[j][j];
            }

            final double tmpD = tmpRowI[ij] = matrix.doubleValue(ij, ij) - DotProduct.invoke(tmpRowI, tmpRowIJ, ij);
            mySPD &= (tmpD > ZERO);

            for (int i = ij + 1; i < tmpDiagDim; i++) { // Update column below current row
                tmpRowI = tmpData[i];

                tmpRowI[ij] = (matrix.doubleValue(i, ij) - DotProduct.invoke(tmpRowI, tmpRowIJ, ij)) / tmpD;
            }
        }

        return this.computed(true);
    }

    public MatrixStore<Double> getD() {
        return this.getRawInPlaceStore().builder().diagonal(false).build();
    }

    public Double getDeterminant() {

        final double[][] tmpData = this.getRawInPlaceData();

        double retVal = ONE;
        for (int ij = 0; ij < tmpData.length; ij++) {
            retVal *= tmpData[ij][ij];
        }
        return retVal;
    }

    @Override
    public MatrixStore<Double> getInverse() {
=======
    public MatrixStore<Double> getD() {
        return this.getRawInPlaceStore().builder().diagonal(false).build();
    }

    public Double getDeterminant() {

        final double[][] tmpData = this.getRawInPlaceData();

        double retVal = ONE;
        for (int ij = 0; ij < tmpData.length; ij++) {
            retVal *= tmpData[ij][ij];
        }
        return retVal;
    }

    @Override
    public MatrixStore<Double> getInverse() {
        // TODO Auto-generated method stub
        return null;
    }

    public MatrixStore<Double> getL() {
        final RawStore tmpRawInPlaceStore = this.getRawInPlaceStore();
        final Builder<Double> tmpBuilder = tmpRawInPlaceStore.builder();
        final Builder<Double> tmpTriangular = tmpBuilder.triangular(false, true);
        return tmpTriangular.build();
    }

    public int getRank() {
>>>>>>> origin/master
        // TODO Auto-generated method stub
        return 0;
    }

<<<<<<< HEAD
    public MatrixStore<Double> getInverse(final DecompositionStore<Double> preallocated) {

        preallocated.fillAll(ZERO);
        preallocated.fillDiagonal(0L, 0L, ONE);

        final RawStore tmpBody = this.getRawInPlaceStore();

        preallocated.substituteForwards(tmpBody, true, false, true);

        for (int i = 0; i < preallocated.countRows(); i++) {
            preallocated.modifyRow(i, 0, PrimitiveFunction.DIVIDE.second(tmpBody.doubleValue(i, i)));
        }

        preallocated.substituteBackwards(tmpBody, true, true, true);

        return preallocated;
    }

    public MatrixStore<Double> getL() {
        final RawStore tmpRawInPlaceStore = this.getRawInPlaceStore();
        final Builder<Double> tmpBuilder = tmpRawInPlaceStore.builder();
        final Builder<Double> tmpTriangular = tmpBuilder.triangular(false, true);
        return tmpTriangular.build();
=======
    public boolean isSolvable() {
        return this.isComputed() && this.isSquareAndNotSingular();
>>>>>>> origin/master
    }

    public boolean isSPD() {
        return mySPD;
    }

    public boolean isSolvable() {
        return this.isComputed() && this.isSquareAndNotSingular();
    }

    public boolean isSPD() {
        return mySPD;
    }

    public boolean isSquareAndNotSingular() {
        // TODO Auto-generated method stub
        return false;
    }

<<<<<<< HEAD
    public MatrixStore<Double> solve(final Access2D<Double> rhs) {
        return this.solve(rhs, this.preallocate(this.getRawInPlaceStore(), rhs));
=======
    public MatrixStore<Double> reconstruct() {
        // TODO Auto-generated method stub
        return null;
>>>>>>> origin/master
    }

    public MatrixStore<Double> solve(final Access2D<Double> rhs) {
        return this.solve(rhs, this.preallocate(this.getRawInPlaceStore(), rhs));
    }

    @Override
    public final MatrixStore<Double> solve(final Access2D<Double> rhs, final DecompositionStore<Double> preallocated) {

        preallocated.fillMatching(rhs);

        final RawStore tmpBody = this.getRawInPlaceStore();

        preallocated.substituteForwards(tmpBody, true, false, false);

        for (int i = 0; i < preallocated.countRows(); i++) {
            preallocated.modifyRow(i, 0, PrimitiveFunction.DIVIDE.second(tmpBody.doubleValue(i, i)));
        }

        preallocated.substituteBackwards(tmpBody, true, true, false);

        return preallocated;
    }

    /**
     * Doesn't copy anything. Tha input original matrix is copied while computing the decomposition.
     *
     * @see org.ojalgo.matrix.decomposition.RawDecomposition#copy(org.ojalgo.access.Access2D, int, int,
     *      double[][])
     */
    @Override
    void copy(final Access2D<?> source, final int rows, final int columns, final double[][] destination) {
<<<<<<< HEAD
=======
    }

    public MatrixStore<Double> getInverse(final DecompositionStore<Double> preallocated) {

        preallocated.fillAll(ZERO);
        preallocated.fillDiagonal(0L, 0L, ONE);

        final RawStore tmpBody = this.getRawInPlaceStore();

        preallocated.substituteForwards(tmpBody, true, false, true);

        for (int i = 0; i < preallocated.countRows(); i++) {
            preallocated.modifyRow(i, 0, PrimitiveFunction.DIVIDE.second(tmpBody.doubleValue(i, i)));
        }

        preallocated.substituteBackwards(tmpBody, true, true, true);

        return preallocated;
>>>>>>> origin/master
    }

}

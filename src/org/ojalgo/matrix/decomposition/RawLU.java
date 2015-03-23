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
import org.ojalgo.array.ArrayUtils;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.function.aggregator.PrimitiveAggregator;
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.matrix.decomposition.LUDecomposition.Pivot;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.RawStore;
import org.ojalgo.matrix.store.RowsStore;
import org.ojalgo.matrix.store.WrapperStore;
import org.ojalgo.matrix.store.operation.DotProduct;
import org.ojalgo.type.context.NumberContext;

/**
 * This class adapts JAMA's LUDecomposition to ojAlgo's {@linkplain LU} interface.
 *
 * @deprecated v38 This class will be made package private. Use the inteface instead.
 * @author apete
 */
@Deprecated
public final class RawLU extends RawDecomposition implements LU<Double> {

    private Pivot myPivot;

    /**
     * Not recommended to use this constructor directly. Consider using the static factory method
     * {@linkplain org.ojalgo.matrix.decomposition.LU#makeJama()} instead.
     */
    public RawLU() {
        super();
    }

    /**
     * Use a "left-looking", dot-product, Crout/Doolittle algorithm, essentially copied from JAMA.
     *
     * @see org.ojalgo.matrix.decomposition.MatrixDecomposition#compute(org.ojalgo.access.Access2D)
     */
    public boolean compute(final Access2D<?> matrix) {

        this.reset();

        final double[][] tmpData = this.setRawInPlace(matrix);

        final int tmpRowDim = this.getRowDim();
        final int tmpColDim = this.getColDim();

        myPivot = new Pivot(tmpRowDim);

        final double[] tmpColJ = new double[tmpRowDim];

        // Outer loop.
        for (int j = 0; j < tmpColDim; j++) {

            // Make a copy of the j-th column to localize references.
            for (int i = 0; i < tmpRowDim; i++) {
                tmpColJ[i] = tmpData[i][j];
            }

            // Apply previous transformations.
            for (int i = 0; i < tmpRowDim; i++) {
                // Most of the time is spent in the following dot product.
                tmpData[i][j] = tmpColJ[i] -= DotProduct.invoke(tmpData[i], tmpColJ, Math.min(i, j));
            }

            // Find pivot and exchange if necessary.
            int p = j;
            for (int i = j + 1; i < tmpRowDim; i++) {
                if (Math.abs(tmpColJ[i]) > Math.abs(tmpColJ[p])) {
                    p = i;
                }
            }
            if (p != j) {
                ArrayUtils.exchangeRows(tmpData, j, p);
                myPivot.change(j, p);
            }

            // Compute multipliers.
            if (j < tmpRowDim) {
                final double tmpVal = tmpData[j][j];
                if (tmpVal != ZERO) {
                    for (int i = j + 1; i < tmpRowDim; i++) {
                        tmpData[i][j] /= tmpVal;
                    }
                }
            }

        }

        return this.computed(true);
    }

    public boolean computeWithoutPivoting(final MatrixStore<?> matrix) {
        return this.compute(matrix);
    }

    public boolean equals(final MatrixStore<Double> aStore, final NumberContext context) {
        return MatrixUtils.equals(aStore, this, context);
    }

    public Double getDeterminant() {
        final int m = this.getRowDim();
        final int n = this.getColDim();
        if (m != n) {
            throw new IllegalArgumentException("RawStore must be square.");
        }
        final double[][] LU = this.getRawInPlaceData();
        double d = myPivot.signum();
        for (int j = 0; j < n; j++) {
            d *= LU[j][j];
        }
        return d;
    }

    @Override
    public RawStore getInverse() {
        return this.solve(this.makeEyeStore((int) this.getL().countRows(), (int) this.getU().countColumns()));
    }

    public MatrixStore<Double> getL() {
        return this.getRawInPlaceStore().builder().triangular(false, true).build();
    }

    public int[] getPivotOrder() {
        return myPivot.getOrder();
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

    public MatrixStore<Double> getU() {
        return this.getRawInPlaceStore().builder().triangular(true, false).build();
    }

    public boolean isFullSize() {
        return false;
    }

    public boolean isSolvable() {
        return (this != null) && this.isNonsingular();
    }

    public boolean isSquareAndNotSingular() {
        return (this != null) && ((int) this.getL().countRows() == (int) this.getU().countColumns()) && this.isNonsingular();
    }

    public MatrixStore<Double> reconstruct() {
        return MatrixUtils.reconstruct(this);
    }

    @Override
    public MatrixStore<Double> solve(final Access2D<Double> rhs, final DecompositionStore<Double> preallocated) {

        preallocated.fillMatching(new RowsStore<Double>(new WrapperStore<>(preallocated.factory(), rhs), myPivot.getOrder()));

        final MatrixStore<Double> tmpBody = this.getRawInPlaceStore();

        preallocated.substituteForwards(tmpBody, true, false, false);

        preallocated.substituteBackwards(tmpBody, false, false, false);

        return preallocated;
    }

    @Override
    public void reset() {

        super.reset();

        myPivot = null;
    }

    public final MatrixStore<Double> solve(final Access2D<Double> rhs) {
        return this.solve(rhs, this.preallocate(this.getRawInPlaceStore(), rhs));
    }

    @Override
    protected boolean compute(final RawStore matrix) {
        ProgrammingError.throwForIllegalInvocation();
        return false;
    }

    /**
     * Is the matrix nonsingular?
     *
     * @return true if U, and hence A, is nonsingular.
     */
    boolean isNonsingular() {
        final int n = this.getColDim();

        final double[][] LU = this.getRawInPlaceData();
        for (int j = 0; j < n; j++) {
            if (LU[j][j] == 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Solve A*X = B
     *
     * @param B A RawStore with as many rows as A and any number of columns.
     * @return X so that L*U*X = B(piv,:)
     * @exception IllegalArgumentException RawStore row dimensions must agree.
     * @exception RuntimeException RawStore is singular.
     */
    @Override
    RawStore solve(final RawStore B) {

        final double[][] LU = this.getRawInPlaceData();

        final int m = this.getRowDim();
        final int n = this.getColDim();

        if ((int) B.countRows() != m) {
            throw new IllegalArgumentException("RawStore row dimensions must agree.");
        }
        if (!this.isNonsingular()) {
            throw new RuntimeException("RawStore is singular.");
        }

        // Copy right hand side with pivoting
        final int nx = (int) B.countColumns();
        final RawStore Xmat = B.getMatrix(myPivot.getOrder(), 0, nx - 1);
        final double[][] X = Xmat.data;

        // Solve L*Y = B(piv,:)
        for (int k = 0; k < n; k++) {
            for (int i = k + 1; i < n; i++) {
                for (int j = 0; j < nx; j++) {
                    X[i][j] -= X[k][j] * LU[i][k];
                }
            }
        }
        // Solve U*X = Y;
        for (int k = n - 1; k >= 0; k--) {
            for (int j = 0; j < nx; j++) {
                X[k][j] /= LU[k][k];
            }
            for (int i = 0; i < k; i++) {
                for (int j = 0; j < nx; j++) {
                    X[i][j] -= X[k][j] * LU[i][k];
                }
            }
        }
        return Xmat;
    }

    public final MatrixStore<Double> getInverse(final DecompositionStore<Double> preallocated) {

        final int[] tmpPivotOrder = myPivot.getOrder();
        final int tmpRowDim = this.getRowDim();
        for (int i = 0; i < tmpRowDim; i++) {
            preallocated.set(i, tmpPivotOrder[i], PrimitiveMath.ONE);
        }

        final RawStore tmpBody = this.getRawInPlaceStore();

        preallocated.substituteForwards(tmpBody, true, false, !myPivot.isModified());

        preallocated.substituteBackwards(tmpBody, false, false, false);

        return preallocated;
    }

}

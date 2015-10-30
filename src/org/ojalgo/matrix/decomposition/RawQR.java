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
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.function.aggregator.PrimitiveAggregator;
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.matrix.store.ElementsSupplier;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.matrix.store.RawStore;
import org.ojalgo.matrix.store.operation.DotProduct;
import org.ojalgo.matrix.store.operation.SubtractScaledVector;
import org.ojalgo.type.context.NumberContext;

/**
 * This class adapts JAMA's QRDecomposition to ojAlgo's {@linkplain QR} interface. QR Decomposition.
 * <P>
 * For an m-by-n matrix A with m &gt;= n, the QR decomposition is an m-by-n orthogonal matrix Q and an n-by-n
 * upper triangular matrix R so that A = Q*R.
 * <P>
 * The QR decompostion always exists, even if the matrix does not have full rank, so the constructor will
 * never fail. The primary use of the QR decomposition is in the least squares solution of nonsquare systems
 * of simultaneous linear equations. This will fail if isFullRank() returns false.
 */
final class RawQR extends RawDecomposition implements QR<Double> {

    /**
     * Array for internal storage of diagonal of R.
     *
     * @serial diagonal of R.
     */
    private double[] myDiagonalR;

    private boolean myFullSize = false;

    /**
     * Not recommended to use this constructor directly. Consider using the static factory method
     * {@linkplain org.ojalgo.matrix.decomposition.QR#make(Access2D)} instead.
     */
    RawQR() {
        super();
    }

    public Double calculateDeterminant(final Access2D<?> matrix) {

        final double[][] retVal = this.reset(matrix, true);

        MatrixStore.PRIMITIVE.makeWrapper(matrix).transpose().supplyTo(this.getRawInPlaceStore());

        this.doDecompose(retVal);

        return this.getDeterminant();
    }

    /**
     * QR Decomposition, computed by Householder reflections. Structure to access R and the Householder
     * vectors and compute Q.
     *
     * @param A Rectangular matrix
     */
    public boolean decompose(final ElementsSupplier<Double> matrix) {

        final double[][] retVal = this.reset(matrix, true);

        matrix.transpose().supplyTo(this.getRawInPlaceStore());

        return this.doDecompose(retVal);
    }

    public boolean equals(final MatrixStore<Double> aStore, final NumberContext context) {
        return MatrixUtils.equals(aStore, this, context);
    }

    public Double getDeterminant() {

        final AggregatorFunction<Double> tmpAggrFunc = PrimitiveAggregator.getSet().product();

        this.getR().visitDiagonal(0, 0, tmpAggrFunc);

        return tmpAggrFunc.getNumber();
    }

    /**
     * Generate and return the (economy-sized) orthogonal factor
     *
     * @return Q
     */
    public RawStore getQ() {

        final int m = this.getRowDim();
        final int n = this.getColDim();

        final double[][] tmpData = this.getRawInPlaceData();

        final RawStore retVal = new RawStore(m, n);
        final double[][] retData = retVal.data;

        for (int k = n - 1; k >= 0; k--) {
            for (int i = 0; i < m; i++) {
                retData[i][k] = ZERO;
            }
            retData[k][k] = ONE;
            for (int j = k; j < n; j++) {
                if (tmpData[k][k] != 0) {
                    double s = ZERO;
                    for (int i = k; i < m; i++) {
                        s += tmpData[k][i] * retData[i][j];
                    }
                    s = -s / tmpData[k][k];
                    for (int i = k; i < m; i++) {
                        retData[i][j] += s * tmpData[k][i];
                    }
                }
            }
        }
        return retVal;
    }

    /**
     * Return the upper triangular factor
     *
     * @return R
     */
    public MatrixStore<Double> getR() {

        final int tmpColDim = this.getColDim();

        final double[][] tmpData = this.getRawInPlaceData();

        final RawStore retVal = new RawStore(tmpColDim, tmpColDim);
        final double[][] retData = retVal.data;

        double[] tmpRow;
        for (int i = 0; i < tmpColDim; i++) {
            tmpRow = retData[i];
            tmpRow[i] = myDiagonalR[i];
            for (int j = i + 1; j < tmpColDim; j++) {
                tmpRow[j] = tmpData[j][i];
            }
        }

        return retVal;
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

    @Override
    public final MatrixStore<Double> invert(final Access2D<?> original, final DecompositionStore<Double> preallocated) {

        final double[][] tmpData = this.reset(MatrixStore.PRIMITIVE.makeWrapper(original), true);

        MatrixStore.PRIMITIVE.makeWrapper(original).transpose().supplyTo(this.getRawInPlaceStore());

        this.doDecompose(tmpData);

        return this.getInverse(preallocated);
    }

    /**
     * Is the matrix full rank?
     *
     * @return true if R, and hence A, has full rank.
     */
    public boolean isFullColumnRank() {

        final int n = this.getColDim();

        for (int j = 0; j < n; j++) {
            if (myDiagonalR[j] == 0) {
                return false;
            }
        }
        return true;
    }

    public boolean isFullSize() {
        return myFullSize;
    }

    public boolean isSolvable() {
        return this.isFullColumnRank();
    }

    public MatrixStore<Double> reconstruct() {
        return MatrixUtils.reconstruct(this);
    }

    public void setFullSize(final boolean fullSize) {
        myFullSize = fullSize;
    }

    @Override
    public MatrixStore<Double> solve(final Access2D<?> body, final Access2D<?> rhs, final DecompositionStore<Double> preallocated) {

        final double[][] tmpData = this.reset(body, true);

        MatrixStore.PRIMITIVE.makeWrapper(body).transpose().supplyTo(this.getRawInPlaceStore());

        this.doDecompose(tmpData);

        preallocated.fillMatching(rhs);

        return this.doSolve((PrimitiveDenseStore) preallocated);
    }

    @Override
    public MatrixStore<Double> solve(final ElementsSupplier<Double> rhs, final DecompositionStore<Double> preallocated) {

        rhs.supplyTo(preallocated);

        return this.doSolve((PrimitiveDenseStore) preallocated);
    }

    public MatrixStore<Double> solve(final MatrixStore<Double> rhs, final DecompositionStore<Double> preallocated) {
        return this.solve((Access2D<?>) rhs, preallocated);
    }

    /**
     * Makes no use of <code>preallocated</code> at all. Simply delegates to {@link #getInverse()}.
     *
     * @see org.ojalgo.matrix.decomposition.MatrixDecomposition#doGetInverse(org.ojalgo.matrix.decomposition.DecompositionStore)
     */
    @Override
    protected MatrixStore<Double> doGetInverse(final PrimitiveDenseStore preallocated) {

        MatrixStore.PRIMITIVE.makeIdentity(this.getRowDim()).supplyTo(preallocated);

        return this.doSolve(preallocated);
    }

    boolean doDecompose(final double[][] data) {

        final int m = this.getRowDim();
        final int n = this.getColDim();

        myDiagonalR = new double[n];

        double[] tmpColK;
        double nrm;

        // Main loop.
        for (int k = 0; k < n; k++) {

            tmpColK = data[k];

            // Compute 2-norm of k-th column without under/overflow.
            nrm = ZERO;
            for (int i = k; i < m; i++) {
                nrm = Maths.hypot(nrm, tmpColK[i]);
            }

            if (nrm != ZERO) {

                // Form k-th Householder vector.
                if (tmpColK[k] < 0) {
                    nrm = -nrm;
                }
                for (int i = k; i < m; i++) {
                    tmpColK[i] /= nrm;
                }
                tmpColK[k] += ONE;

                // Apply transformation to remaining columns.
                for (int j = k + 1; j < n; j++) {
                    SubtractScaledVector.invoke(data[j], 0, tmpColK, 0, DotProduct.invoke(tmpColK, 0, data[j], 0, k, m) / tmpColK[k], k, m);
                }
            }
            myDiagonalR[k] = -nrm;
        }

        return this.computed(true);
    }

    MatrixStore<Double> doSolve(final PrimitiveDenseStore preallocated) {

        final double[] tmpRHSdata = preallocated.data;

        final int m = this.getRowDim();
        final int n = this.getColDim();
        final int s = (int) preallocated.countColumns();

        if ((int) preallocated.countRows() != m) {
            throw new IllegalArgumentException("RawStore row dimensions must agree.");
        }
        if (!this.isFullColumnRank()) {
            throw new RuntimeException("RawStore is rank deficient.");
        }

        final double[][] tmpData = this.getRawInPlaceData();

        double[] tmpColK;

        // Compute Y = transpose(Q)*B
        for (int k = 0; k < n; k++) {

            tmpColK = tmpData[k];

            for (int j = 0; j < s; j++) {
                final double tmpVal = DotProduct.invoke(tmpColK, 0, tmpRHSdata, m * j, k, m);
                SubtractScaledVector.invoke(tmpRHSdata, m * j, tmpColK, 0, tmpVal / tmpColK[k], k, m);
            }
        }

        // Solve R*X = Y;
        for (int k = n - 1; k >= 0; k--) {

            tmpColK = tmpData[k];
            final double tmpDiagK = myDiagonalR[k];

            for (int j = 0; j < s; j++) {
                tmpRHSdata[k + (j * m)] /= tmpDiagK;
                SubtractScaledVector.invoke(tmpRHSdata, j * m, tmpColK, 0, tmpRHSdata[k + (j * m)], 0, k);
            }
        }

        return preallocated.builder().rows(0, n).build();
    }

}

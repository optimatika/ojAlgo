/*
 * Copyright 1997-2024 Optimatika
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

import static org.ojalgo.function.constant.PrimitiveMath.*;

import org.ojalgo.RecoverableCondition;
import org.ojalgo.array.operation.AXPY;
import org.ojalgo.array.operation.DOT;
import org.ojalgo.array.operation.NRM2;
import org.ojalgo.array.operation.NRMINF;
import org.ojalgo.array.operation.VisitAll;
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.function.aggregator.PrimitiveAggregator;
import org.ojalgo.matrix.operation.HouseholderLeft;
import org.ojalgo.matrix.store.ElementsSupplier;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.matrix.store.RawStore;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Access2D.Collectable;
import org.ojalgo.structure.Structure2D;

/**
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
    private int myNumberOfHouseholderTransformations = 0;

    /**
     * Not recommended to use this constructor directly. Consider using the static factory method
     * {@linkplain org.ojalgo.matrix.decomposition.QR#make(Access2D)} instead.
     */
    RawQR() {
        super();
    }

    public void btran(final PhysicalStore<Double> arg) {

        Primitive64Store preallocated = (Primitive64Store) arg;

        double[] dataRHS = preallocated.data;

        int m = this.getRowDim();
        int n = this.getColDim();

        if (m != n) {
            throw new IllegalArgumentException("Only square matrices!");
        }
        if (preallocated.getRowDim() != m) {
            throw new IllegalArgumentException("Row dimensions must agree!");
        }
        if (!this.isFullRank()) {
            throw new RuntimeException("Rank deficient!");
        }

        double[][] dataInternal = this.getInternalData();

        double[] colK;
        double beta;

        // Solve Rt*y = b;
        for (int k = 0; k < n; k++) {

            colK = dataInternal[k];
            double tmpDiagK = myDiagonalR[k];

            dataRHS[k] -= DOT.invoke(dataRHS, 0, colK, 0, 0, k);
            dataRHS[k] /= tmpDiagK;
        }

        // Compute Y = transpose(Q)*B
        for (int k = n - 1; k >= 0; k--) {

            colK = dataInternal[k];
            beta = ONE / colK[k];

            HouseholderLeft.call(dataRHS, m, 0, colK, k, beta);
        }
    }

    public Double calculateDeterminant(final Access2D<?> matrix) {

        double[][] retVal = this.reset(matrix, true);

        Primitive64Store.FACTORY.makeWrapper(matrix).transpose().supplyTo(this.getInternalStore());

        this.doDecompose(retVal);

        return this.getDeterminant();
    }

    public int countSignificant(final double threshold) {

        int significant = 0;
        for (int ij = 0, limit = myDiagonalR.length; ij < limit; ij++) {
            if (Math.abs(myDiagonalR[ij]) > threshold) {
                significant++;
            }
        }

        return significant;
    }

    /**
     * QR Decomposition, computed by Householder reflections. Structure to access R and the Householder
     * vectors and compute Q.
     *
     * @param matrix Rectangular matrix
     */
    @SuppressWarnings({ "rawtypes" })
    public boolean decompose(final Access2D.Collectable<Double, ? super PhysicalStore<Double>> matrix) {

        double[][] retVal = this.reset(matrix, true);

        if (matrix instanceof ElementsSupplier) {
            ((ElementsSupplier) matrix).transpose().supplyTo(this.getInternalStore());
        } else {
            // TODO Find a better solution
            matrix.collect(RawStore.FACTORY).transpose().supplyTo(this.getInternalStore());
        }

        return this.doDecompose(retVal);
    }

    public Double getDeterminant() {

        AggregatorFunction<Double> aggregator = PrimitiveAggregator.getSet().product();

        VisitAll.visit(myDiagonalR, aggregator);

        if (myNumberOfHouseholderTransformations % 2 != 0) {
            return Double.valueOf(-aggregator.get().doubleValue());
        }

        return aggregator.get();
    }

    public MatrixStore<Double> getInverse() {
        int tmpRowDim = this.getRowDim();
        return this.doGetInverse(this.allocate(tmpRowDim, tmpRowDim));
    }

    public MatrixStore<Double> getInverse(final PhysicalStore<Double> preallocated) {
        return this.doGetInverse((Primitive64Store) preallocated);
    }

    /**
     * Generate and return the (economy-sized) orthogonal factor
     *
     * @return Q
     */
    public RawStore getQ() {

        int m = this.getRowDim();
        int r = this.getMinDim();

        double[][] internalData = this.getInternalData();

        RawStore retVal = RawDecomposition.make(m, r);
        double[][] retData = retVal.data;

        for (int k = r - 1; k >= 0; k--) {
            for (int i = 0; i < m; i++) {
                retData[i][k] = ZERO;
            }
            retData[k][k] = ONE;
            for (int j = k; j < r; j++) {
                if (internalData[k][k] != 0) {
                    double s = ZERO;
                    for (int i = k; i < m; i++) {
                        s += internalData[k][i] * retData[i][j];
                    }
                    s = -s / internalData[k][k];
                    for (int i = k; i < m; i++) {
                        retData[i][j] += s * internalData[k][i];
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

        int n = this.getColDim();
        int r = this.getMinDim();

        double[][] internalData = this.getInternalData();

        RawStore retVal = RawDecomposition.make(r, n);
        double[][] retData = retVal.data;

        double[] tmpRow;
        for (int i = 0; i < r; i++) {
            tmpRow = retData[i];
            tmpRow[i] = myDiagonalR[i];
            for (int j = i + 1; j < n; j++) {
                tmpRow[j] = internalData[j][i];
            }
        }

        return retVal;
    }

    public double getRankThreshold() {

        double largest = MACHINE_SMALLEST;
        for (int ij = 0, limit = myDiagonalR.length; ij < limit; ij++) {
            largest = Math.max(largest, Math.abs(myDiagonalR[ij]));
        }

        double epsilon = this.getDimensionalEpsilon();

        return largest * epsilon;
    }

    public MatrixStore<Double> getSolution(final Collectable<Double, ? super PhysicalStore<Double>> rhs) {
        return this.getSolution(rhs, this.allocate(rhs.countRows(), rhs.countColumns()));
    }

    @Override
    public MatrixStore<Double> getSolution(final Collectable<Double, ? super PhysicalStore<Double>> rhs, final PhysicalStore<Double> preallocated) {

        rhs.supplyTo(preallocated);

        return this.doSolve((Primitive64Store) preallocated);
    }

    @Override
    public MatrixStore<Double> invert(final Access2D<?> original, final PhysicalStore<Double> preallocated) throws RecoverableCondition {

        double[][] tmpData = this.reset(Primitive64Store.FACTORY.makeWrapper(original), true);

        Primitive64Store.FACTORY.makeWrapper(original).transpose().supplyTo(this.getInternalStore());

        this.doDecompose(tmpData);

        if (this.isSolvable()) {
            return this.getInverse(preallocated);
        }
        throw RecoverableCondition.newMatrixNotInvertible();
    }

    public boolean isFullSize() {
        return false;
    }

    @Override
    public boolean isSolvable() {
        return super.isSolvable();
    }

    public PhysicalStore<Double> preallocate(final Structure2D template) {
        return this.allocate(template.countRows(), template.countRows());
    }

    public PhysicalStore<Double> preallocate(final Structure2D templateBody, final Structure2D templateRHS) {
        return this.allocate(templateBody.countRows(), templateRHS.countColumns());
    }

    @Override
    public void reset() {

        super.reset();

        myNumberOfHouseholderTransformations = 0;
    }

    public MatrixStore<Double> solve(final Access2D<?> body, final Access2D<?> rhs) throws RecoverableCondition {
        return this.solve(body, rhs, this.preallocate(body, rhs));
    }

    @Override
    public MatrixStore<Double> solve(final Access2D<?> body, final Access2D<?> rhs, final PhysicalStore<Double> preallocated) throws RecoverableCondition {

        double[][] tmpData = this.reset(body, true);

        Primitive64Store.FACTORY.makeWrapper(body).transpose().supplyTo(this.getInternalStore());

        this.doDecompose(tmpData);

        if (this.isSolvable()) {

            preallocated.fillMatching(rhs);

            return this.doSolve((Primitive64Store) preallocated);

        }
        throw RecoverableCondition.newEquationSystemNotSolvable();
    }

    private boolean doDecompose(final double[][] data) {

        int m = this.getRowDim();
        int r = this.getMinDim();

        myDiagonalR = new double[r];

        double[] colK;
        for (int k = 0; k < r; k++) {
            colK = data[k];

            // Compute Infinity-norm of k-th column
            double norm = NRMINF.invoke(colK, k, m);
            if (norm == ZERO) {
                break;
            }
            // Compute 2-norm of k-th column
            norm = NRM2.invoke(colK, norm, k, m);

            myNumberOfHouseholderTransformations++;

            // Form k-th Householder vector.
            if (colK[k] < 0) {
                norm = -norm;
            }

            for (int i = k; i < m; i++) {
                colK[i] /= norm;
            }
            colK[k] += ONE;

            // Apply transformation to remaining columns
            double hBeta = ONE / colK[k];

            HouseholderLeft.call(data, m, k + 1, colK, k, hBeta);

            myDiagonalR[k] = -norm;
        }

        return this.computed(true);
    }

    /**
     * Makes no use of <code>preallocated</code> at all. Simply delegates to {@link #getInverse()}.
     */
    private MatrixStore<Double> doGetInverse(final Primitive64Store preallocated) {

        Primitive64Store.FACTORY.makeIdentity(this.getRowDim()).supplyTo(preallocated);

        return this.doSolve(preallocated);
    }

    private MatrixStore<Double> doSolve(final Primitive64Store preallocated) {

        double[] dataRHS = preallocated.data;

        int m = this.getRowDim();
        int n = this.getColDim();
        int s = preallocated.getColDim();

        if (preallocated.getRowDim() != m) {
            throw new IllegalArgumentException("Row dimensions must agree!");
        }
        if (!this.isFullRank()) {
            throw new RuntimeException("Rank deficient!");
        }

        double[][] dataInternal = this.getInternalData();

        double[] colK;
        double beta;

        // Compute Y = transpose(Q)*B
        for (int k = 0; k < n; k++) {

            colK = dataInternal[k];
            beta = ONE / colK[k];

            HouseholderLeft.call(dataRHS, m, 0, colK, k, beta);
        }

        // Solve R*X = Y;
        for (int k = n - 1; k >= 0; k--) {

            colK = dataInternal[k];
            double tmpDiagK = myDiagonalR[k];

            for (int j = 0; j < s; j++) {
                dataRHS[k + j * m] /= tmpDiagK;
                AXPY.invoke(dataRHS, j * m, -dataRHS[k + j * m], colK, 0, 0, k);
            }
        }

        return preallocated.limits(n, s);
    }

    @Override
    protected boolean checkSolvability() {
        return this.isAspectRatioNormal() && this.isFullRank();
    }

}

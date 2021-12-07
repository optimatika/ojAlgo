/*
 * Copyright 1997-2021 Optimatika
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
import org.ojalgo.array.operation.VisitAll;
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.function.aggregator.PrimitiveAggregator;
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
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public boolean decompose(final Access2D.Collectable<? super PhysicalStore<Double>> matrix) {

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
            return -aggregator.get();
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
        int n = this.getColDim();
        int r = Math.min(m, n);

        double[][] tmpData = this.getInternalData();

        RawStore retVal = new RawStore(m, r);
        double[][] retData = retVal.data;

        for (int k = r - 1; k >= 0; k--) {
            for (int i = 0; i < m; i++) {
                retData[i][k] = ZERO;
            }
            retData[k][k] = ONE;
            for (int j = k; j < r; j++) {
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

        int m = this.getRowDim();
        int n = this.getColDim();
        int r = Math.min(m, n);

        double[][] tmpData = this.getInternalData();

        RawStore retVal = new RawStore(r, n);
        double[][] retData = retVal.data;

        double[] tmpRow;
        for (int i = 0; i < r; i++) {
            tmpRow = retData[i];
            tmpRow[i] = myDiagonalR[i];
            for (int j = i + 1; j < n; j++) {
                tmpRow[j] = tmpData[j][i];
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

    public MatrixStore<Double> getSolution(final Collectable<? super PhysicalStore<Double>> rhs) {
        DecompositionStore<Double> tmpPreallocated = this.allocate(rhs.countRows(), rhs.countColumns());
        return this.getSolution(rhs, tmpPreallocated);
    }

    @Override
    public MatrixStore<Double> getSolution(final Collectable<? super PhysicalStore<Double>> rhs, final PhysicalStore<Double> preallocated) {

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
        int n = this.getColDim();

        myDiagonalR = new double[n];

        double[] tmpColK;
        double nrm;

        // Main loop.
        for (int k = 0; k < n; k++) {

            tmpColK = data[k];

            // Compute 2-norm of k-th column without under/overflow.
            nrm = ZERO;
            for (int i = k; i < m; i++) {
                double a = nrm;
                nrm = HYPOT.invoke(a, tmpColK[i]);
            }

            if (nrm != ZERO) {

                myNumberOfHouseholderTransformations++;

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
                    AXPY.invoke(data[j], 0, -(DOT.invoke(tmpColK, 0, data[j], 0, k, m) / tmpColK[k]), tmpColK, 0, k, m);
                }
            }
            myDiagonalR[k] = -nrm;
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

        double[] tmpRHSdata = preallocated.data;

        int m = this.getRowDim();
        int n = this.getColDim();
        int s = (int) preallocated.countColumns();

        if ((int) preallocated.countRows() != m) {
            throw new IllegalArgumentException("RawStore row dimensions must agree.");
        }
        if (!this.isFullRank()) {
            throw new RuntimeException("RawStore is rank deficient.");
        }

        double[][] tmpData = this.getInternalData();

        double[] tmpColK;

        // Compute Y = transpose(Q)*B
        for (int k = 0; k < n; k++) {

            tmpColK = tmpData[k];

            for (int j = 0; j < s; j++) {
                double tmpVal = -(DOT.invoke(tmpColK, 0, tmpRHSdata, m * j, k, m) / tmpColK[k]);
                AXPY.invoke(tmpRHSdata, m * j, tmpVal, tmpColK, 0, k, m);
            }
        }

        // Solve R*X = Y;
        for (int k = n - 1; k >= 0; k--) {

            tmpColK = tmpData[k];
            double tmpDiagK = myDiagonalR[k];

            for (int j = 0; j < s; j++) {
                tmpRHSdata[k + j * m] /= tmpDiagK;
                AXPY.invoke(tmpRHSdata, j * m, -tmpRHSdata[k + j * m], tmpColK, 0, 0, k);
            }
        }
        return preallocated.limits(n, (int) preallocated.countColumns());
    }

    @Override
    protected boolean checkSolvability() {
        return this.isAspectRatioNormal() && this.isFullRank();
    }

}

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
import org.ojalgo.array.Array1D;
import org.ojalgo.array.operation.AXPY;
import org.ojalgo.array.operation.DOT;
import org.ojalgo.matrix.decomposition.function.ExchangeColumns;
import org.ojalgo.matrix.decomposition.function.NegateColumn;
import org.ojalgo.matrix.decomposition.function.RotateRight;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.matrix.store.RawStore;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Access2D.Collectable;
import org.ojalgo.structure.Structure2D;

/**
 * <p>
 * Singular Value Decomposition.
 * <P>
 * For an m-by-n matrix A with m &gt;= n, the singular value decomposition is an m-by-n orthogonal matrix U,
 * an n-by-n diagonal matrix S, and an n-by-n orthogonal matrix V so that A = U*S*V'.
 * <P>
 * The singular values, sigma[k] = S[k][k], are ordered so that sigma[0] &gt;= sigma[1] &gt;= ... &gt;=
 * sigma[n-1].
 * <P>
 * The singular value decompostion always exists, so the constructor will never fail. The matrix condition
 * number and the effective numerical rank can be computed from this decomposition.
 * <p>
 *
 * @author apete
 */
final class RawSingularValue extends RawDecomposition implements SingularValue<Double> {

    private double[] e;
    /**
     * Calculation row and column dimensions, possibly transposed from the input
     */
    private int m, n;
    private transient Primitive64Store myPseudoinverse = null;
    private boolean myTransposed;
    /**
     * Arrays for internal storage of U and V.
     */
    private double[][] myUt;
    private double[][] myVt;
    /**
     * Array for internal storage of singular values.
     */
    private double[] s;
    private double[] w;

    /**
     * Not recommended to use this constructor directly. Consider using the static factory method
     * {@linkplain org.ojalgo.matrix.decomposition.SingularValue#make(Access2D)} instead.
     */
    RawSingularValue() {
        super();
    }

    public void btran(final PhysicalStore<Double> arg) {
        arg.fillByMultiplying(this.getInverse().transpose(), arg.copy());
    }

    public boolean computeValuesOnly(final Access2D.Collectable<Double, ? super PhysicalStore<Double>> matrix) {
        return this.doDecompose(matrix, false);
    }

    public int countSignificant(final double threshold) {
        int significant = 0;
        for (int i = 0; i < s.length; i++) {
            if (s[i] > threshold) {
                significant++;
            }
        }
        return significant;
    }

    public boolean decompose(final Access2D.Collectable<Double, ? super PhysicalStore<Double>> matrix) {
        return this.doDecompose(matrix, true);
    }

    public double getCondition() {
        return s[0] / s[n - 1];
    }

    public MatrixStore<Double> getCovariance() {

        MatrixStore<Double> v = this.getV();
        Access1D<Double> values = this.getSingularValues();

        int rank = this.getRank();

        MatrixStore<Double> tmp = v.limits(-1, rank).onColumns(DIVIDE, values).collect(v.physical());

        return tmp.multiply(tmp.transpose());
    }

    public MatrixStore<Double> getD() {
        return RawDecomposition.makeDiagonal(this.getSingularValues()).get();
    }

    public double getFrobeniusNorm() {

        double retVal = ZERO;

        double tmpVal;
        for (int i = n - 1; i >= 0; i--) {
            tmpVal = s[i];
            retVal += tmpVal * tmpVal;
        }

        return SQRT.invoke(retVal);
    }

    @Override
    public MatrixStore<Double> getInverse() {
        return this.doGetInverse(this.allocate(this.getColDim(), this.getRowDim()));
    }

    public MatrixStore<Double> getInverse(final PhysicalStore<Double> preallocated) {
        return this.doGetInverse((Primitive64Store) preallocated);
    }

    public double getKyFanNorm(final int k) {

        double retVal = ZERO;

        for (int i = Math.min(s.length, k) - 1; i >= 0; i--) {
            retVal += s[i];
        }

        return retVal;
    }

    /**
     * Two norm
     *
     * @return max(S)
     */
    public double getOperatorNorm() {
        return s[0];
    }

    public double getRankThreshold() {
        return Math.max(MACHINE_SMALLEST, s[0]) * this.getDimensionalEpsilon();
    }

    public Array1D<Double> getSingularValues() {
        return Array1D.R064.copy(s);
    }

    public void getSingularValues(final double[] values) {
        System.arraycopy(s, 0, values, 0, Math.min(s.length, values.length));
    }

    public MatrixStore<Double> getSolution(final Collectable<Double, ? super PhysicalStore<Double>> rhs) {
        return this.getSolution(rhs, this.allocate(this.getMinDim(), rhs.countColumns()));
    }

    @Override
    public MatrixStore<Double> getSolution(final Collectable<Double, ? super PhysicalStore<Double>> rhs, final PhysicalStore<Double> preallocated) {
        preallocated.fillByMultiplying(this.getInverse(), this.collect(rhs));
        return preallocated;
    }

    public double getTraceNorm() {
        return this.getKyFanNorm(s.length);
    }

    public MatrixStore<Double> getU() {
        return myTransposed ? this.wrap(myVt).transpose() : this.wrap(myUt).transpose();
    }

    public MatrixStore<Double> getV() {
        return myTransposed ? this.wrap(myUt).transpose() : this.wrap(myVt).transpose();
    }

    @Override
    public MatrixStore<Double> invert(final Access2D<?> original, final PhysicalStore<Double> preallocated) throws RecoverableCondition {

        this.doDecompose(original.asCollectable2D(), true);

        if (this.isSolvable()) {
            return this.getInverse(preallocated);
        }
        throw RecoverableCondition.newMatrixNotInvertible();
    }

    public boolean isFullRank() {
        return s[s.length - 1] > this.getRankThreshold();
    }

    public boolean isFullSize() {
        return false;
    }

    public boolean isOrdered() {
        return true;
    }

    @Override
    public boolean isSolvable() {
        return super.isSolvable();
    }

    public PhysicalStore<Double> preallocate(final Structure2D template) {
        return this.allocate(template.countColumns(), template.countRows());
    }

    public PhysicalStore<Double> preallocate(final Structure2D templateBody, final Structure2D templateRHS) {
        return this.allocate(templateBody.countColumns(), templateRHS.countColumns());
    }

    @Override
    public void reset() {

        super.reset();

        myPseudoinverse = null;
    }

    @Override
    public MatrixStore<Double> solve(final Access2D<?> body, final Access2D<?> rhs, final PhysicalStore<Double> preallocated) throws RecoverableCondition {

        this.doDecompose(body.asCollectable2D(), true);

        if (this.isSolvable()) {
            return this.getSolution(rhs.asCollectable2D(), preallocated);
        }
        throw RecoverableCondition.newEquationSystemNotSolvable();
    }

    @Override
    protected boolean checkSolvability() {
        return true;
    }

    boolean doDecompose(final Access2D.Collectable<Double, ? super PhysicalStore<Double>> matrix, final boolean factors) {

        myTransposed = matrix.countRows() < matrix.countColumns();

        final double[][] input = this.reset(matrix, !myTransposed);

        if (myTransposed) {
            matrix.supplyTo(this.getInternalStore());
        } else {
            this.collect(matrix).transpose().supplyTo(this.getInternalStore());
        }

        m = this.getMaxDim();
        n = this.getMinDim();

        if (s == null || s.length != n) {
            s = new double[n];
            e = new double[n];
        }
        if (w == null || w.length != m) {
            w = new double[m];
        }
        if (factors) {
            myUt = input;
            if (myVt == null || myVt.length != n || myVt[0].length != n) {
                myVt = new double[n][n];
            }
        } else {
            myUt = null;
            myVt = null;
        }

        double[] tmpArr;
        double tmpVal;

        double nrm = ZERO;

        // Reduce A to bidiagonal form, storing the diagonal elements
        // in s and the super-diagonal elements in e.

        final int nct = Math.min(m - 1, n); // Number of Column Transformations
        final int nrt = Math.max(0, n - 2); // Number of Row Transformations

        final int limit = Math.max(nct, nrt);
        for (int k = 0; k < limit; k++) {
            tmpArr = input[k];

            if (k < nct) {
                // Compute the transformation for the k-th column and
                // place the k-th diagonal in s[k].

                // Compute 2-norm of k-th column without under/overflow.
                nrm = ZERO;
                for (int i = k; i < m; i++) {
                    nrm = HYPOT.invoke(nrm, tmpArr[i]);
                }

                // Form k-th Householder column-vector.
                if (nrm != ZERO) {
                    if (tmpArr[k] < ZERO) {
                        nrm = -nrm;
                    }
                    for (int i = k; i < m; i++) {
                        tmpArr[i] /= nrm;
                    }
                    tmpArr[k] += ONE;

                    // Apply the transformation to the remaining columns
                    for (int j = k + 1; j < n; j++) {
                        tmpVal = DOT.invoke(tmpArr, 0, input[j], 0, k, m);
                        tmpVal /= tmpArr[k];
                        AXPY.invoke(input[j], 0, -tmpVal, tmpArr, 0, k, m);
                    }
                }
                s[k] = -nrm;
            }

            for (int j = k + 1; j < n; j++) {
                // Place the k-th row of A into e for the
                // subsequent calculation of the row transformation.
                e[j] = input[j][k];
            }

            if (factors && k < nct) {
                // Place the transformation in U for subsequent back multiplication.
                for (int i = k; i < m; i++) {
                    myUt[k][i] = tmpArr[i];
                }
            }

            if (k < nrt) {
                // Compute the k-th row transformation and place the
                // k-th super-diagonal in e[k].

                // Compute 2-norm without under/overflow.
                nrm = ZERO;
                for (int i = k + 1; i < n; i++) {
                    nrm = HYPOT.invoke(nrm, e[i]);
                }

                if (nrm != ZERO) {
                    if (e[k + 1] < ZERO) {
                        nrm = -nrm;
                    }
                    for (int i = k + 1; i < n; i++) {
                        e[i] /= nrm;
                    }
                    e[k + 1] += ONE;

                    // Apply the transformation.
                    for (int i = k + 1; i < m; i++) {
                        w[i] = ZERO;
                    }
                    // ... remining columns
                    for (int j = k + 1; j < n; j++) {
                        AXPY.invoke(w, 0, e[j], input[j], 0, k + 1, m);
                    }
                    for (int j = k + 1; j < n; j++) {
                        AXPY.invoke(input[j], 0, -(e[j] / e[k + 1]), w, 0, k + 1, m);
                    }
                }
                e[k] = -nrm;

                if (factors) {
                    // Place the transformation in V for subsequent back multiplication.
                    for (int i = k + 1; i < n; i++) {
                        myVt[k][i] = e[i];
                    }
                }
            }
        }

        // Set up the final bidiagonal matrix or order p. []
        final int p = n;
        if (nct < n) { // Only happens when m == n, then nct == n-1
            s[nct] = input[nct][nct];
        }
        if (nrt + 1 < p) {
            e[nrt] = input[p - 1][nrt];
        }
        e[p - 1] = ZERO;

        // If required, generate U.
        if (factors) {
            for (int j = nct; j < n; j++) {
                tmpArr = myUt[j];
                for (int i = 0; i < m; i++) {
                    tmpArr[i] = ZERO;
                }
                tmpArr[j] = ONE;
            }
            for (int k = nct - 1; k >= 0; k--) {
                tmpArr = myUt[k];
                if (s[k] != ZERO) {
                    for (int j = k + 1; j < n; j++) {
                        tmpVal = DOT.invoke(tmpArr, 0, myUt[j], 0, k, m);
                        tmpVal /= tmpArr[k];
                        AXPY.invoke(myUt[j], 0, -tmpVal, tmpArr, 0, k, m);
                    }
                    for (int i = 0; i < k; i++) {
                        tmpArr[i] = ZERO;
                    }
                    tmpArr[k] = ONE - tmpArr[k];
                    for (int i = k + 1; i < m; i++) {
                        tmpArr[i] = -tmpArr[i];
                    }
                } else {
                    for (int i = 0; i < m; i++) {
                        tmpArr[i] = ZERO;
                    }
                    tmpArr[k] = ONE;
                }
            }
        }

        // If required, generate V.
        if (factors) {
            for (int k = n - 1; k >= 0; k--) {
                tmpArr = myVt[k];
                if (k < nrt && e[k] != ZERO) {
                    for (int j = k + 1; j < n; j++) {
                        tmpVal = DOT.invoke(tmpArr, 0, myVt[j], 0, k + 1, n);
                        tmpVal /= tmpArr[k + 1];
                        AXPY.invoke(myVt[j], 0, -tmpVal, tmpArr, 0, k + 1, n);
                    }
                }
                for (int i = 0; i < n; i++) {
                    tmpArr[i] = ZERO;
                }
                tmpArr[k] = ONE;
            }
        }

        final RotateRight q1RotR = factors ? (low, high, cos, sin) -> {
            final double[] colLow = myUt[low];
            final double[] colHigh = myUt[high];
            double valLow;
            double valHigh;
            for (int i = 0; i < m; i++) {
                valLow = colLow[i];
                valHigh = colHigh[i];
                colLow[i] = -sin * valHigh + cos * valLow;
                colHigh[i] = cos * valHigh + sin * valLow;
            }
        } : RotateRight.NULL;

        final RotateRight q2RotR = factors ? (low, high, cos, sin) -> {
            final double[] colLow = myVt[low];
            final double[] colHigh = myVt[high];
            double valLow;
            double valHigh;
            for (int i = 0; i < n; i++) {
                valLow = colLow[i];
                valHigh = colHigh[i];
                colLow[i] = -sin * valHigh + cos * valLow;
                colHigh[i] = cos * valHigh + sin * valLow;
            }
        } : RotateRight.NULL;

        final ExchangeColumns q1XchgCols = factors ? (colA, colB) -> {
            final double[] col1 = myUt[colA];
            final double[] col2 = myUt[colB];
            double tmp;
            for (int i = 0; i < m; i++) {
                tmp = col1[i];
                col1[i] = col2[i];
                col2[i] = tmp;
            }
        } : ExchangeColumns.NULL;

        final ExchangeColumns q2XchgCols = factors ? (colA, colB) -> {
            final double[] col1 = myVt[colA];
            final double[] col2 = myVt[colB];
            double tmp;
            for (int i = 0; i < n; i++) {
                tmp = col1[i];
                col1[i] = col2[i];
                col2[i] = tmp;
            }
        } : ExchangeColumns.NULL;

        final NegateColumn q2NegCol = factors ? col -> {
            final double[] column = myVt[col];
            for (int i = 0; i < column.length; i++) {
                column[i] = -column[i];
            }
        } : NegateColumn.NULL;

        SingularValueDecomposition.toDiagonal(s, e, q1RotR, q2RotR, q1XchgCols, q2XchgCols, q2NegCol);

        return this.computed(true);
    }

    MatrixStore<Double> doGetInverse(final Primitive64Store preallocated) {

        if (myPseudoinverse == null) {

            final double[][] tmpQ1t = myTransposed ? myVt : myUt;
            final double[] tmpSingular = s;

            final RawStore tmpMtrx = this.newRawStore(tmpSingular.length, tmpQ1t[0].length);
            final double[][] tmpMtrxData = tmpMtrx.data;

            final double small = this.getRankThreshold();

            for (int i = 0; i < tmpSingular.length; i++) {
                final double tmpVal = tmpSingular[i];
                if (tmpVal > small) {
                    final double[] tmpRow = tmpMtrxData[i];
                    for (int j = 0; j < tmpRow.length; j++) {
                        tmpRow[j] = tmpQ1t[i][j] / tmpVal;
                    }
                }
            }

            MatrixStore<Double> mtrxQ2 = this.getV();
            preallocated.fillByMultiplying(mtrxQ2, tmpMtrx);
            myPseudoinverse = preallocated;
        }

        return myPseudoinverse;
    }

}

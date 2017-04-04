/*
 * Copyright 1997-2016 Optimatika (www.optimatika.se)
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
import static org.ojalgo.function.PrimitiveFunction.*;

import org.ojalgo.access.Access2D;
import org.ojalgo.access.Access2D.Collectable;
import org.ojalgo.access.Stream2D;
import org.ojalgo.access.Structure2D;
import org.ojalgo.array.Array1D;
import org.ojalgo.array.blas.AXPY;
import org.ojalgo.array.blas.DOT;
import org.ojalgo.matrix.store.ElementsSupplier;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.matrix.store.RawStore;
import org.ojalgo.matrix.task.TaskException;
import org.ojalgo.scalar.PrimitiveScalar;

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

    /**
     * Row and column dimensions.
     *
     * @serial row dimension.
     * @serial column dimension.
     */
    private int m, n;

    private transient PrimitiveDenseStore myPseudoinverse = null;

    /**
     * Array for internal storage of singular values.
     *
     * @serial internal storage of singular values.
     */
    private double[] myS;

    private boolean myTransposed;
    /**
     * Arrays for internal storage of U and V.
     *
     * @serial internal storage of U.
     * @serial internal storage of V.
     */
    private double[][] myUt;
    private double[][] myVt;

    /**
     * Not recommended to use this constructor directly. Consider using the static factory method
     * {@linkplain org.ojalgo.matrix.decomposition.SingularValue#make(Access2D)} instead.
     */
    RawSingularValue() {
        super();
    }

    public boolean computeValuesOnly(final ElementsSupplier<Double> matrix) {

        myTransposed = matrix.countRows() < matrix.countColumns();

        final double[][] tmpData = this.reset(matrix.get(), !myTransposed);

        if (myTransposed) {
            matrix.supplyTo(this.getRawInPlaceStore());
        } else {
            matrix.transpose().supplyTo(this.getRawInPlaceStore());
        }

        return this.doDecompose(tmpData, false);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public boolean decompose(final Access2D.Collectable<Double, ? super PhysicalStore<Double>> matrix) {

        myTransposed = matrix.countRows() < matrix.countColumns();

        final double[][] tmpData = this.reset(matrix, !myTransposed);

        if (myTransposed) {
            matrix.supplyTo(this.getRawInPlaceStore());
        } else {
            // TODO Handle case with non Stream2D
            ((Stream2D) matrix).transpose().supplyTo(this.getRawInPlaceStore());
        }

        return this.doDecompose(tmpData, true);
    }

    public double getCondition() {
        return myS[0] / myS[n - 1];
    }

    public MatrixStore<Double> getD() {
        final DiagonalArray1D<Double> tmpDiagonal = new DiagonalArray1D<>(this.getSingularValues(), null, null, ZERO);
        return MatrixStore.PRIMITIVE.makeWrapper(tmpDiagonal).get();
    }

    public double getFrobeniusNorm() {

        double retVal = ZERO;

        double tmpVal;
        for (int i = n - 1; i >= 0; i--) {
            tmpVal = myS[i];
            retVal += tmpVal * tmpVal;
        }

        return SQRT.invoke(retVal);
    }

    @Override
    public MatrixStore<Double> getInverse() {
        return this.doGetInverse(this.allocate(this.getColDim(), this.getRowDim()));
    }

    public MatrixStore<Double> getInverse(final PhysicalStore<Double> preallocated) {
        return this.doGetInverse((PrimitiveDenseStore) preallocated);
    }

    public double getKyFanNorm(final int k) {

        double retVal = ZERO;

        for (int i = Math.min(myS.length, k) - 1; i >= 0; i--) {
            retVal += myS[i];
        }

        return retVal;
    }

    /**
     * Two norm
     *
     * @return max(S)
     */
    public double getOperatorNorm() {
        return myS[0];
    }

    public RawStore getQ1() {
        return myTransposed ? this.getV().transpose() : this.getU().transpose();
    }

    public RawStore getQ2() {
        return myTransposed ? this.getU().transpose() : this.getV().transpose();
    }

    public int getRank() {
        final double eps = POW.invoke(TWO, -52.0);
        final double tol = MAX.invoke(m, n) * (myS[0] * eps);
        int r = 0;
        for (int i = 0; i < myS.length; i++) {
            if (myS[i] > tol) {
                r++;
            }
        }
        return r;
    }

    public Array1D<Double> getSingularValues() {
        return Array1D.PRIMITIVE64.copy(myS);
    }

    public MatrixStore<Double> getSolution(final Collectable<Double, ? super PhysicalStore<Double>> rhs) {
        final DecompositionStore<Double> tmpPreallocated = this.allocate(rhs.countRows(), rhs.countRows());
        return this.getSolution(rhs, tmpPreallocated);
    }

    @Override
    public MatrixStore<Double> getSolution(final Collectable<Double, ? super PhysicalStore<Double>> rhs, final PhysicalStore<Double> preallocated) {
        return this.doGetInverse((PrimitiveDenseStore) preallocated).multiply(this.collect(rhs));
    }

    public double getTraceNorm() {
        return this.getKyFanNorm(myS.length);
    }

    @Override
    public MatrixStore<Double> invert(final Access2D<?> original, final PhysicalStore<Double> preallocated) throws TaskException {

        myTransposed = original.countRows() < original.countColumns();

        final double[][] tmpData = this.reset(original, !myTransposed);

        if (myTransposed) {
            this.getRawInPlaceStore().fillMatching(original);
        } else {
            MatrixStore.PRIMITIVE.makeWrapper(original).transpose().supplyTo(this.getRawInPlaceStore());
        }

        this.doDecompose(tmpData, true);

        if (this.isSolvable()) {
            return this.getInverse(preallocated);
        } else {
            throw TaskException.newNotInvertible();
        }
    }

    public boolean isFullSize() {
        return false;
    }

    public boolean isOrdered() {
        return true;
    }

    public boolean isSolvable() {
        return this.isComputed();
    }

    public PhysicalStore<Double> preallocate(final Structure2D template) {
        return this.allocate(template.countColumns(), template.countRows());
    }

    public PhysicalStore<Double> preallocate(final Structure2D templateBody, final Structure2D templateRHS) {
        return this.allocate(templateBody.countColumns(), templateBody.countRows());
    }

    public MatrixStore<Double> reconstruct() {
        return SingularValue.reconstruct(this);
    }

    @Override
    public void reset() {

        super.reset();

        myPseudoinverse = null;
    }

    public void setFullSize(final boolean fullSize) {
        ;
    }

    @Override
    public MatrixStore<Double> solve(final Access2D<?> body, final Access2D<?> rhs, final PhysicalStore<Double> preallocated) throws TaskException {

        myTransposed = body.countRows() < body.countColumns();

        final double[][] tmpData = this.reset(body, !myTransposed);

        if (myTransposed) {
            this.getRawInPlaceStore().fillMatching(body);
        } else {
            MatrixStore.PRIMITIVE.makeWrapper(body).transpose().supplyTo(this.getRawInPlaceStore());
        }

        this.doDecompose(tmpData, true);

        if (this.isSolvable()) {

            final MatrixStore<Double> tmpRHS = MatrixStore.PRIMITIVE.makeWrapper(rhs).get();
            return this.doGetInverse((PrimitiveDenseStore) preallocated).multiply(tmpRHS);

        } else {
            throw TaskException.newNotSolvable();
        }
    }

    private boolean doDecompose(final double[][] data, final boolean factors) {
        // Derived from JAMA which is derived from LINPACK code

        // Input is possibly transposed so that m >= n always
        m = this.getMaxDim();
        n = this.getMinDim();

        myUt = factors ? new double[n][m] : null;
        myS = new double[n];
        myVt = factors ? new double[n][n] : null;

        final double[] tmpE = new double[n];
        final double[] tmpWork = new double[m];

        double[] tmpAt_k;
        double nrm = ZERO;

        // Reduce A to bidiagonal form, storing the diagonal elements
        // in s and the super-diagonal elements in e.
        final int nct = Math.min(m - 1, n); // Number of Column Transformations
        final int nrt = Math.max(0, n - 2); // Number of Row Transformations
        final int tmpLimK = Math.max(nct, nrt);
        for (int k = 0; k < tmpLimK; k++) {
            tmpAt_k = data[k];

            if (k < nct) {
                // Compute the transformation for the k-th column and
                // place the k-th diagonal in s[k].

                // Compute 2-norm of k-th column without under/overflow.
                nrm = ZERO;
                for (int i = k; i < m; i++) {
                    final double a = nrm;
                    nrm = HYPOT.invoke(a, tmpAt_k[i]);
                }

                // Form k-th Householder column-vector.
                if (nrm != ZERO) {
                    if (tmpAt_k[k] < ZERO) {
                        nrm = -nrm;
                    }
                    for (int i = k; i < m; i++) {
                        tmpAt_k[i] /= nrm;
                    }
                    tmpAt_k[k] += ONE;

                    // Apply the transformation to the remaining columns
                    for (int j = k + 1; j < n; j++) {
                        double t = DOT.invoke(tmpAt_k, 0, data[j], 0, k, m);
                        t = t / tmpAt_k[k];
                        AXPY.invoke(data[j], 0, 1, -t, tmpAt_k, 0, 1, k, m);
                    }
                }
                myS[k] = -nrm;
            }

            for (int j = k + 1; j < n; j++) {
                // Place the k-th row of A into e for the
                // subsequent calculation of the row transformation.
                tmpE[j] = data[j][k];
            }

            if (factors && (k < nct)) {
                // Place the transformation in U for subsequent back multiplication.
                for (int i = k; i < m; i++) {
                    myUt[k][i] = tmpAt_k[i];
                }
            }

            if (k < nrt) {
                // Compute the k-th row transformation and place the
                // k-th super-diagonal in e[k].

                // Compute 2-norm without under/overflow.
                nrm = ZERO;
                for (int i = k + 1; i < n; i++) {
                    final double a = nrm;
                    nrm = HYPOT.invoke(a, tmpE[i]);
                }
                if (nrm != ZERO) {
                    if (tmpE[k + 1] < ZERO) {
                        nrm = -nrm;
                    }
                    for (int i = k + 1; i < n; i++) {
                        tmpE[i] /= nrm;
                    }
                    tmpE[k + 1] += ONE;

                    // Apply the transformation.
                    for (int i = k + 1; i < m; i++) {
                        tmpWork[i] = ZERO;
                    }
                    // ... remining columns
                    for (int j = k + 1; j < n; j++) {
                        AXPY.invoke(tmpWork, 0, 1, -(-tmpE[j]), data[j], 0, 1, k + 1, m);
                    }
                    for (int j = k + 1; j < n; j++) {
                        AXPY.invoke(data[j], 0, 1, -(tmpE[j] / tmpE[k + 1]), tmpWork, 0, 1, k + 1, m);
                    }
                }
                tmpE[k] = -nrm;

                if (factors) {
                    // Place the transformation in V for subsequent back multiplication.
                    for (int i = k + 1; i < n; i++) {
                        myVt[k][i] = tmpE[i];
                    }
                }
            }
        }

        // Set up the final bidiagonal matrix or order p. []
        final int p = n;
        if (nct < n) { // Only happens when m == n, then nct == n-1
            myS[nct] = data[nct][nct];
        }
        //        if (m < p) {
        //            myS[p - 1] = ZERO;
        //        }
        if ((nrt + 1) < p) {
            tmpE[nrt] = data[p - 1][nrt];
        }
        tmpE[p - 1] = ZERO;

        // If required, generate U.
        if (factors) {
            for (int j = nct; j < n; j++) {
                for (int i = 0; i < m; i++) {
                    myUt[j][i] = ZERO;
                }
                myUt[j][j] = ONE;
            }
            for (int k = nct - 1; k >= 0; k--) {
                final double[] tmpUt_k = myUt[k];
                if (myS[k] != ZERO) {
                    for (int j = k + 1; j < n; j++) {
                        double t = DOT.invoke(tmpUt_k, 0, myUt[j], 0, k, m);
                        t = t / tmpUt_k[k];
                        AXPY.invoke(myUt[j], 0, 1, -t, tmpUt_k, 0, 1, k, m);
                    }
                    for (int i = k; i < m; i++) {
                        tmpUt_k[i] = -tmpUt_k[i];
                    }
                    tmpUt_k[k] = ONE + tmpUt_k[k];
                    for (int i = 0; i < (k - 1); i++) {
                        tmpUt_k[i] = ZERO;
                    }
                } else {
                    for (int i = 0; i < m; i++) {
                        tmpUt_k[i] = ZERO;
                    }
                    tmpUt_k[k] = ONE;
                }
            }
        }

        // If required, generate V.
        if (factors) {
            for (int k = n - 1; k >= 0; k--) {
                final double[] tmpVt_k = myVt[k];
                if ((k < nrt) && (tmpE[k] != ZERO)) {
                    for (int j = k + 1; j < n; j++) {
                        double t = DOT.invoke(tmpVt_k, 0, myVt[j], 0, k + 1, n);
                        t = t / tmpVt_k[k + 1];
                        AXPY.invoke(myVt[j], 0, 1, -t, tmpVt_k, 0, 1, k + 1, n);
                    }
                }
                for (int i = 0; i < n; i++) {
                    tmpVt_k[i] = ZERO;
                }
                tmpVt_k[k] = ONE;
            }
        }

        SVD2D.toDiagonal(myS, tmpE, factors, p, myUt, myVt);

        return this.computed(true);
    }

    private MatrixStore<Double> doGetInverse(final PrimitiveDenseStore preallocated) {

        if (myPseudoinverse == null) {

            final double[][] tmpQ1 = this.getQ1().data;
            final double[] tmpSingular = myS;

            final RawStore tmpMtrx = new RawStore(tmpSingular.length, tmpQ1.length);
            final double[][] tmpMtrxData = tmpMtrx.data;

            final double tmpEps = (tmpSingular[0] * MACHINE_EPSILON) * tmpSingular.length;

            for (int i = 0; i < tmpSingular.length; i++) {
                final double tmpVal = tmpSingular[i];
                if (!PrimitiveScalar.isSmall(tmpEps, tmpVal)) {
                    final double[] tmpRow = tmpMtrxData[i];
                    for (int j = 0; j < tmpQ1.length; j++) {
                        tmpRow[j] = tmpQ1[j][i] / tmpVal;
                    }
                }
            }

            preallocated.fillByMultiplying(this.getQ2(), tmpMtrx);
            myPseudoinverse = preallocated;
        }

        return myPseudoinverse;
    }

    /**
     * Return the left singular vectors
     *
     * @return Ut
     */
    RawStore getU() {
        return new RawStore(myUt, n, m);
    }

    /**
     * Return the right singular vectors
     *
     * @return V
     */
    RawStore getV() {
        return new RawStore(myVt, n, n);
    }

}

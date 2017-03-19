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
        int p = n;
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

        // Main iteration loop for the singular values.
        final int pp = p - 1;
        final double eps = POW.invoke(TWO, -52.0);
        final double tiny = POW.invoke(TWO, -966.0);
        while (p > 0) {
            int k, kase;

            // Here is where a test for too many iterations would go.

            // This section of the program inspects for
            // negligible elements in the s and e arrays.  On
            // completion the variables kase and k are set as follows.

            // kase = 1     if s(p) and e[k-1] are negligible and k<p
            // kase = 2     if s(k) is negligible and k<p
            // kase = 3     if e[k-1] is negligible, k<p, and
            //              s(k), ..., s(p) are not negligible (qr step).
            // kase = 4     if e(p-1) is negligible (convergence).

            for (k = p - 2; k >= -1; k--) {
                if (k == -1) {
                    break;
                }
                if (ABS.invoke(tmpE[k]) <= (tiny + (eps * (ABS.invoke(myS[k]) + ABS.invoke(myS[k + 1]))))) {
                    tmpE[k] = ZERO;
                    break;
                }
            }
            if (k == (p - 2)) {
                kase = 4;
            } else {
                int ks;
                for (ks = p - 1; ks >= k; ks--) {
                    if (ks == k) {
                        break;
                    }
                    final double t = (ks != p ? ABS.invoke(tmpE[ks]) : 0.) + (ks != (k + 1) ? ABS.invoke(tmpE[ks - 1]) : 0.);
                    if (ABS.invoke(myS[ks]) <= (tiny + (eps * t))) {
                        myS[ks] = ZERO;
                        break;
                    }
                }
                if (ks == k) {
                    kase = 3;
                } else if (ks == (p - 1)) {
                    kase = 1;
                } else {
                    kase = 2;
                    k = ks;
                }
            }
            k++;

            // Perform the task indicated by kase.
            switch (kase) {

            // Deflate negligible s(p).
            case 1: {
                double f = tmpE[p - 2];
                tmpE[p - 2] = ZERO;
                for (int j = p - 2; j >= k; j--) {
                    final double b = f;
                    double t = HYPOT.invoke(myS[j], b);
                    final double cs = myS[j] / t;
                    final double sn = f / t;
                    myS[j] = t;
                    if (j != k) {
                        f = -sn * tmpE[j - 1];
                        tmpE[j - 1] = cs * tmpE[j - 1];
                    }
                    if (factors) {
                        for (int i = 0; i < n; i++) {
                            // t = (cs * myV[i][j]) + (sn * myV[i][p - 1]);
                            t = (cs * myVt[j][i]) + (sn * myVt[p - 1][i]);
                            // myV[i][p - 1] = (-sn * myV[i][j]) + (cs * myV[i][p - 1]);
                            myVt[p - 1][i] = (-sn * myVt[j][i]) + (cs * myVt[p - 1][i]);
                            // myV[i][j] = t;
                            myVt[j][i] = t;
                        }
                    }
                }
            }
                break;

            // Split at negligible s(k).
            case 2: {
                double f = tmpE[k - 1];
                tmpE[k - 1] = ZERO;
                for (int j = k; j < p; j++) {
                    final double b = f;
                    double t = HYPOT.invoke(myS[j], b);
                    final double cs = myS[j] / t;
                    final double sn = f / t;
                    myS[j] = t;
                    f = -sn * tmpE[j];
                    tmpE[j] = cs * tmpE[j];
                    if (factors) {
                        for (int i = 0; i < m; i++) {
                            // t = (cs * myU[i][j]) + (sn * myU[i][k - 1]);
                            t = (cs * myUt[j][i]) + (sn * myUt[k - 1][i]);
                            // myU[i][k - 1] = (-sn * myU[i][j]) + (cs * myU[i][k - 1]);
                            myUt[k - 1][i] = (-sn * myUt[j][i]) + (cs * myUt[k - 1][i]);
                            // myU[i][j] = t;
                            myUt[j][i] = t;
                        }
                    }
                }
            }
                break;

            // Perform one qr step.
            case 3: {

                // Calculate the shift.
                final double scale = MAX.invoke(
                        MAX.invoke(MAX.invoke(MAX.invoke(ABS.invoke(myS[p - 1]), ABS.invoke(myS[p - 2])), ABS.invoke(tmpE[p - 2])), ABS.invoke(myS[k])),
                        ABS.invoke(tmpE[k]));
                final double sp = myS[p - 1] / scale;
                final double spm1 = myS[p - 2] / scale;
                final double epm1 = tmpE[p - 2] / scale;
                final double sk = myS[k] / scale;
                final double ek = tmpE[k] / scale;
                final double b = (((spm1 + sp) * (spm1 - sp)) + (epm1 * epm1)) / TWO;
                final double c = (sp * epm1) * (sp * epm1);
                double shift = ZERO;
                // if ((b != ZERO) | (c != ZERO)) {
                if ((Double.compare(b, ZERO) != 0) || (Double.compare(c, ZERO) != 0)) {
                    shift = SQRT.invoke((b * b) + c);
                    if (b < ZERO) {
                        shift = -shift;
                    }
                    shift = c / (b + shift);
                }
                double f = ((sk + sp) * (sk - sp)) + shift;
                double g = sk * ek;

                // Chase zeros.
                for (int j = k; j < (p - 1); j++) {
                    final double a = f;
                    final double b1 = g;
                    double t = HYPOT.invoke(a, b1);
                    double cs = f / t;
                    double sn = g / t;
                    if (j != k) {
                        tmpE[j - 1] = t;
                    }
                    f = (cs * myS[j]) + (sn * tmpE[j]);
                    tmpE[j] = (cs * tmpE[j]) - (sn * myS[j]);
                    g = sn * myS[j + 1];
                    myS[j + 1] = cs * myS[j + 1];
                    if (factors) {
                        for (int i = 0; i < n; i++) {
                            // t = (cs * myV[i][j]) + (sn * myV[i][j + 1]);
                            t = (cs * myVt[j][i]) + (sn * myVt[j + 1][i]);
                            // myV[i][j + 1] = (-sn * myV[i][j]) + (cs * myV[i][j + 1]);
                            myVt[j + 1][i] = (-sn * myVt[j][i]) + (cs * myVt[j + 1][i]);
                            // myV[i][j] = t;
                            myVt[j][i] = t;
                        }
                    }
                    final double a1 = f;
                    final double b2 = g;
                    t = HYPOT.invoke(a1, b2);
                    cs = f / t;
                    sn = g / t;
                    myS[j] = t;
                    f = (cs * tmpE[j]) + (sn * myS[j + 1]);
                    myS[j + 1] = (-sn * tmpE[j]) + (cs * myS[j + 1]);
                    g = sn * tmpE[j + 1];
                    tmpE[j + 1] = cs * tmpE[j + 1];
                    if (factors && (j < (m - 1))) {
                        for (int i = 0; i < m; i++) {
                            // t = (cs * myU[i][j]) + (sn * myU[i][j + 1]);
                            t = (cs * myUt[j][i]) + (sn * myUt[j + 1][i]);
                            // myU[i][j + 1] = (-sn * myU[i][j]) + (cs * myU[i][j + 1]);
                            myUt[j + 1][i] = (-sn * myUt[j][i]) + (cs * myUt[j + 1][i]);
                            // myU[i][j] = t;
                            myUt[j][i] = t;
                        }
                    }
                }
                tmpE[p - 2] = f;
            }
                break;

            // Convergence.
            case 4: {

                // Make the singular values positive.
                if (myS[k] <= ZERO) {
                    myS[k] = (myS[k] < ZERO ? -myS[k] : ZERO);
                    if (factors) {
                        for (int i = 0; i <= pp; i++) {
                            // myV[i][k] = -myV[i][k];
                            myVt[k][i] = -myVt[k][i];
                        }
                    }
                }

                // Order the singular values.
                while (k < pp) {
                    if (myS[k] >= myS[k + 1]) {
                        break;
                    }
                    final double t = myS[k];
                    myS[k] = myS[k + 1];
                    myS[k + 1] = t;
                    if (factors && (k < (n - 1))) {
                        tmpAt_k = myVt[k + 1]; // Re-use tmpAt_k for a completely different purpose
                        myVt[k + 1] = myVt[k];
                        myVt[k] = tmpAt_k;
                        //                        for (int i = 0; i < n; i++) {
                        //                            t = myVt[k + 1][i];
                        //                            myVt[k + 1][i] = myVt[k][i];
                        //                            myVt[k][i] = t;
                        //                        }
                    }
                    if (factors && (k < (m - 1))) {
                        tmpAt_k = myUt[k + 1]; // Re-use tmpAt_k for a completely different purpose
                        myUt[k + 1] = myUt[k];
                        myUt[k] = tmpAt_k;
                        //                        for (int i = 0; i < m; i++) {
                        //                            t = myUt[k + 1][i];
                        //                            myUt[k + 1][i] = myUt[k][i];
                        //                            myUt[k][i] = t;
                        //                        }
                    }
                    k++;
                }
                p--;
            }
                break;
            }
        }

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

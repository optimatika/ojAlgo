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
import org.ojalgo.array.Array1D;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.function.aggregator.ComplexAggregator;
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.matrix.store.ElementsSupplier;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.matrix.store.RawStore;
import org.ojalgo.matrix.store.operation.HouseholderHermitian;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.type.TypeUtils;
import org.ojalgo.type.context.NumberContext;

/**
 * Eigenvalues and eigenvectors of a real matrix.
 * <P>
 * If A is symmetric, then A = V*D*V' where the eigenvalue matrix D is diagonal and the eigenvector matrix V
 * is orthogonal. I.e. A = V.times(D.times(V.transpose())) and V.times(V.transpose()) equals the identity
 * matrix.
 * <P>
 * If A is not symmetric, then the eigenvalue matrix D is block diagonal with the real eigenvalues in 1-by-1
 * blocks and any complex eigenvalues, lambda + i*mu, in 2-by-2 blocks, [lambda, mu; -mu, lambda]. The columns
 * of V represent the eigenvectors in the sense that A*V = V*D, i.e. A.times(V) equals V.times(D). The matrix
 * V may be badly conditioned, or even singular, so the validity of the equation A = V*D*inverse(V) depends
 * upon V.cond().
 **/
abstract class RawEigenvalue extends RawDecomposition implements Eigenvalue<Double> {

    static final class Dynamic extends RawEigenvalue {

        Dynamic() {
            super();
        }

        public boolean isHermitian() {
            return this.checkSymmetry();
        }

        @Override
        boolean doDecompose(final double[][] data) {

            if (this.checkSymmetry()) {
                this.doDecomposeSymmetric(data);
            } else {
                this.doDecomposeGeneral(data);
            }

            return this.computed(true);
        }

    }

    static final class General extends RawEigenvalue {

        General() {
            super();
        }

        public boolean isHermitian() {
            return false;
        }

        @Override
        boolean doDecompose(final double[][] data) {

            this.doDecomposeGeneral(data);

            return this.computed(true);
        }

    }

    static final class Symmetric extends RawEigenvalue implements MatrixDecomposition.Solver<Double> {

        Symmetric() {
            super();
        }

        public boolean isHermitian() {
            return true;
        }

        @Override
        boolean doDecompose(final double[][] data) {

            this.doDecomposeSymmetric(data);

            return this.computed(true);
        }

    }

    private transient double cdivr, cdivi;

    /**
     * Arrays for internal storage of eigenvalues.
     *
     * @serial internal storage of eigenvalues.
     */
    private double[] d;
    private double[] e;

    /**
     * Array for internal storage of nonsymmetric Hessenberg form.
     *
     * @serial internal storage of nonsymmetric Hessenberg form.
     */
    private double[][] H;

    private RawStore myInverse;

    /**
     * Row and column dimension (square matrix).
     *
     * @serial matrix dimension.
     */
    private int n;

    /**
     * Array for internal storage of eigenvectors.
     *
     * @serial internal storage of eigenvectors.
     */
    private double[][] Vt;

    protected RawEigenvalue() {
        super();
    }

    public Double calculateDeterminant(final Access2D<?> matrix) {

        final double[][] tmpData = this.reset(matrix, false);

        this.getRawInPlaceStore().fillMatching(matrix);

        this.doDecompose(tmpData);

        return this.getDeterminant();
    }

    public boolean computeValuesOnly(final ElementsSupplier<Double> matrix) {

        final double[][] tmpData = this.reset(matrix, false);

        matrix.supplyTo(this.getRawInPlaceStore());

        return this.doDecompose(tmpData);
    }

    public boolean decompose(final ElementsSupplier<Double> matrix) {

        final double[][] tmpData = this.reset(matrix, false);

        matrix.supplyTo(this.getRawInPlaceStore());

        return this.doDecompose(tmpData);
    }

    public boolean equals(final MatrixStore<Double> aStore, final NumberContext context) {
        return MatrixUtils.equals(aStore, this, context);
    }

    /**
     * Return the block diagonal eigenvalue matrix
     *
     * @return D
     */
    public RawStore getD() {
        final RawStore X = new RawStore(n, n);
        final double[][] D = X.data;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                D[i][j] = ZERO;
            }
            D[i][i] = d[i];
            if (e[i] > 0) {
                D[i][i + 1] = e[i];
            } else if (e[i] < 0) {
                D[i][i - 1] = e[i];
            }
        }
        return X;
    }

    public Double getDeterminant() {

        final AggregatorFunction<ComplexNumber> tmpVisitor = ComplexAggregator.getSet().product();

        this.getEigenvalues().visitAll(tmpVisitor);

        return tmpVisitor.getNumber().doubleValue();
    }

    public Array1D<ComplexNumber> getEigenvalues() {

        final double[] tmpRe = this.getRealEigenvalues();
        final double[] tmpIm = this.getImagEigenvalues();

        final Array1D<ComplexNumber> retVal = Array1D.COMPLEX.makeZero(tmpRe.length);

        for (int i = 0; i < retVal.size(); i++) {
            retVal.set(i, ComplexNumber.of(tmpRe[i], tmpIm[i]));
        }

        retVal.sortDescending();

        return retVal;
    }

    @Override
    public RawStore getInverse() {

        if (myInverse == null) {

            final double[][] tmpQ1 = this.getV().data;
            final double[] tmpEigen = this.getRealEigenvalues();

            final RawStore tmpMtrx = new RawStore(tmpEigen.length, tmpQ1.length);

            for (int i = 0; i < tmpEigen.length; i++) {
                if (TypeUtils.isZero(tmpEigen[i])) {
                    for (int j = 0; j < tmpQ1.length; j++) {
                        tmpMtrx.set(i, j, PrimitiveMath.ZERO);
                    }
                } else {
                    for (int j = 0; j < tmpQ1.length; j++) {
                        tmpMtrx.set(i, j, tmpQ1[j][i] / tmpEigen[i]);
                    }
                }
            }

            myInverse = new RawStore(this.getV().multiply(tmpMtrx));
        }

        return myInverse;
    }

    public ComplexNumber getTrace() {

        final AggregatorFunction<ComplexNumber> tmpVisitor = ComplexAggregator.getSet().sum();

        this.getEigenvalues().visitAll(tmpVisitor);

        return tmpVisitor.getNumber();
    }

    /**
     * Return the eigenvector matrix
     *
     * @return V
     */
    public RawStore getV() {
        return new RawStore(Vt, n, n).transpose();
    }

    @Override
    public MatrixStore<Double> invert(final Access2D<?> original, final DecompositionStore<Double> preallocated) {

        final double[][] tmpData = this.reset(original, false);

        this.getRawInPlaceStore().fillMatching(original);

        this.doDecompose(tmpData);

        return this.getInverse(preallocated);
    }

    public boolean isOrdered() {
        return !this.isHermitian();
    }

    public boolean isSolvable() {
        return this.isComputed() && this.isHermitian();
    }

    public MatrixStore<Double> reconstruct() {
        return MatrixUtils.reconstruct(this);
    }

    @Override
    public void reset() {
        myInverse = null;
    }

    @Override
    public MatrixStore<Double> solve(final Access2D<?> body, final Access2D<?> rhs, final DecompositionStore<Double> preallocated) {

        final double[][] tmpData = this.reset(body, false);

        this.getRawInPlaceStore().fillMatching(body);

        this.doDecompose(tmpData);

        preallocated.fillMatching(rhs);

        return this.getInverse().multiply(preallocated);
    }

    @Override
    public MatrixStore<Double> solve(final ElementsSupplier<Double> rhs, final DecompositionStore<Double> preallocated) {
        return null;
    }

    public MatrixStore<Double> solve(final MatrixStore<Double> rhs, final DecompositionStore<Double> preallocated) {
        return null;
    }

    /**
     * Complex scalar division.
     */
    private void cdiv(final double xr, final double xi, final double yr, final double yi) {
        double r, d;
        if (Math.abs(yr) > Math.abs(yi)) {
            r = yi / yr;
            d = yr + (r * yi);
            cdivr = (xr + (r * xi)) / d;
            cdivi = (xi - (r * xr)) / d;
        } else {
            r = yr / yi;
            d = yi + (r * yr);
            cdivr = ((r * xr) + xi) / d;
            cdivi = ((r * xi) - xr) / d;
        }
    }

    private void hqr2() {

        //  This is derived from the Algol procedure hqr2,
        //  by Martin and Wilkinson, Handbook for Auto. Comp.,
        //  Vol.ii-Linear Algebra, and the corresponding
        //  Fortran subroutine in EISPACK.

        // Initialize

        final int nn = n;
        int n = nn - 1;
        final int low = 0;
        final int high = nn - 1;
        final double eps = MACHINE_EPSILON;
        double exshift = ZERO;
        double p = 0, q = 0, r = 0, s = 0, z = 0, t, w, x, y;

        // Store roots isolated by balanc and compute matrix norm

        double norm = ZERO;
        for (int i = 0; i < nn; i++) {
            if ((i < low) | (i > high)) {
                d[i] = H[i][i];
                e[i] = ZERO;
            }
            for (int j = Math.max(i - 1, 0); j < nn; j++) {
                norm = norm + Math.abs(H[i][j]);
            }
        }

        // Outer loop over eigenvalue index

        int iter = 0;
        while (n >= low) {

            // Look for single small sub-diagonal element

            int l = n;
            while (l > low) {
                s = Math.abs(H[l - 1][l - 1]) + Math.abs(H[l][l]);
                if (s == ZERO) {
                    s = norm;
                }
                if (Math.abs(H[l][l - 1]) < (eps * s)) {
                    break;
                }
                l--;
            }

            // Check for convergence
            // One root found

            if (l == n) {
                H[n][n] = H[n][n] + exshift;
                d[n] = H[n][n];
                e[n] = ZERO;
                n--;
                iter = 0;

                // Two roots found

            } else if (l == (n - 1)) {
                w = H[n][n - 1] * H[n - 1][n];
                p = (H[n - 1][n - 1] - H[n][n]) / TWO;
                q = (p * p) + w;
                z = Math.sqrt(Math.abs(q));
                H[n][n] = H[n][n] + exshift;
                H[n - 1][n - 1] = H[n - 1][n - 1] + exshift;
                x = H[n][n];

                // Real pair

                if (q >= 0) {
                    if (p >= 0) {
                        z = p + z;
                    } else {
                        z = p - z;
                    }
                    d[n - 1] = x + z;
                    d[n] = d[n - 1];
                    if (z != ZERO) {
                        d[n] = x - (w / z);
                    }
                    e[n - 1] = ZERO;
                    e[n] = ZERO;
                    x = H[n][n - 1];
                    s = Math.abs(x) + Math.abs(z);
                    p = x / s;
                    q = z / s;
                    r = Math.sqrt((p * p) + (q * q));
                    p = p / r;
                    q = q / r;

                    // Row modification

                    for (int j = n - 1; j < nn; j++) {
                        z = H[n - 1][j];
                        H[n - 1][j] = (q * z) + (p * H[n][j]);
                        H[n][j] = (q * H[n][j]) - (p * z);
                    }

                    // Column modification

                    for (int i = 0; i <= n; i++) {
                        z = H[i][n - 1];
                        H[i][n - 1] = (q * z) + (p * H[i][n]);
                        H[i][n] = (q * H[i][n]) - (p * z);
                    }

                    // Accumulate transformations

                    for (int i = low; i <= high; i++) {
                        //z = V[i][n - 1];
                        z = Vt[n - 1][i];
                        //V[i][n - 1] = (q * z) + (p * V[i][n]);
                        Vt[n - 1][i] = (q * z) + (p * Vt[n][i]);
                        //V[i][n] = (q * V[i][n]) - (p * z);
                        Vt[n][i] = (q * Vt[n][i]) - (p * z);
                    }

                    // Complex pair

                } else {
                    d[n - 1] = x + p;
                    d[n] = x + p;
                    e[n - 1] = z;
                    e[n] = -z;
                }
                n = n - 2;
                iter = 0;

                // No convergence yet

            } else {

                // Form shift

                x = H[n][n];
                y = ZERO;
                w = ZERO;
                if (l < n) {
                    y = H[n - 1][n - 1];
                    w = H[n][n - 1] * H[n - 1][n];
                }

                // Wilkinson's original ad hoc shift

                if (iter == 10) {
                    exshift += x;
                    for (int i = low; i <= n; i++) {
                        H[i][i] -= x;
                    }
                    s = Math.abs(H[n][n - 1]) + Math.abs(H[n - 1][n - 2]);
                    x = y = 0.75 * s;
                    w = -0.4375 * s * s;
                }

                // MATLAB's new ad hoc shift

                if (iter == 30) {
                    s = (y - x) / TWO;
                    s = (s * s) + w;
                    if (s > 0) {
                        s = Math.sqrt(s);
                        if (y < x) {
                            s = -s;
                        }
                        s = x - (w / (((y - x) / TWO) + s));
                        for (int i = low; i <= n; i++) {
                            H[i][i] -= s;
                        }
                        exshift += s;
                        x = y = w = 0.964;
                    }
                }

                iter = iter + 1; // (Could check iteration count here.)

                // Look for two consecutive small sub-diagonal elements

                int m = n - 2;
                while (m >= l) {
                    z = H[m][m];
                    r = x - z;
                    s = y - z;
                    p = (((r * s) - w) / H[m + 1][m]) + H[m][m + 1];
                    q = H[m + 1][m + 1] - z - r - s;
                    r = H[m + 2][m + 1];
                    s = Math.abs(p) + Math.abs(q) + Math.abs(r);
                    p = p / s;
                    q = q / s;
                    r = r / s;
                    if (m == l) {
                        break;
                    }
                    if ((Math.abs(H[m][m - 1]) * (Math.abs(q) + Math.abs(r))) < (eps
                            * (Math.abs(p) * (Math.abs(H[m - 1][m - 1]) + Math.abs(z) + Math.abs(H[m + 1][m + 1]))))) {
                        break;
                    }
                    m--;
                }

                for (int i = m + 2; i <= n; i++) {
                    H[i][i - 2] = ZERO;
                    if (i > (m + 2)) {
                        H[i][i - 3] = ZERO;
                    }
                }

                // Double QR step involving rows l:n and columns m:n

                for (int k = m; k <= (n - 1); k++) {
                    final boolean notlast = (k != (n - 1));
                    if (k != m) {
                        p = H[k][k - 1];
                        q = H[k + 1][k - 1];
                        r = (notlast ? H[k + 2][k - 1] : ZERO);
                        x = Math.abs(p) + Math.abs(q) + Math.abs(r);
                        if (x == ZERO) {
                            continue;
                        }
                        p = p / x;
                        q = q / x;
                        r = r / x;
                    }

                    s = Math.sqrt((p * p) + (q * q) + (r * r));
                    if (p < 0) {
                        s = -s;
                    }
                    if (s != 0) {
                        if (k != m) {
                            H[k][k - 1] = -s * x;
                        } else if (l != m) {
                            H[k][k - 1] = -H[k][k - 1];
                        }
                        p = p + s;
                        x = p / s;
                        y = q / s;
                        z = r / s;
                        q = q / p;
                        r = r / p;

                        // Row modification
                        for (int j = k; j < nn; j++) {
                            p = H[k][j] + (q * H[k + 1][j]);
                            if (notlast) {
                                p = p + (r * H[k + 2][j]);
                                H[k + 2][j] = H[k + 2][j] - (p * z);
                            }
                            H[k][j] = H[k][j] - (p * x);
                            H[k + 1][j] = H[k + 1][j] - (p * y);
                        }

                        // Column modification
                        for (int i = 0; i <= Math.min(n, k + 3); i++) {
                            p = (x * H[i][k]) + (y * H[i][k + 1]);
                            if (notlast) {
                                p = p + (z * H[i][k + 2]);
                                H[i][k + 2] = H[i][k + 2] - (p * r);
                            }
                            H[i][k] = H[i][k] - p;
                            H[i][k + 1] = H[i][k + 1] - (p * q);
                        }

                        // Accumulate transformations
                        for (int i = low; i <= high; i++) {
                            //p = (x * V[i][k]) + (y * V[i][k + 1]);
                            p = (x * Vt[k][i]) + (y * Vt[k + 1][i]);
                            if (notlast) {
                                //p = p + (z * V[i][k + 2]);
                                p = p + (z * Vt[k + 2][i]);
                                //V[i][k + 2] = V[i][k + 2] - (p * r);
                                Vt[k + 2][i] = Vt[k + 2][i] - (p * r);
                            }
                            //V[i][k] = V[i][k] - p;
                            Vt[k][i] = Vt[k][i] - p;
                            //V[i][k + 1] = V[i][k + 1] - (p * q);
                            Vt[k + 1][i] = Vt[k + 1][i] - (p * q);
                        }
                    } // (s != 0)
                } // k loop
            } // check convergence
        } // while (n >= low)

        // Backsubstitute to find vectors of upper triangular form

        if (norm == ZERO) {
            return;
        }

        for (n = nn - 1; n >= 0; n--) {
            p = d[n];
            q = e[n];

            // Real vector

            if (q == 0) {
                int l = n;
                H[n][n] = ONE;
                for (int i = n - 1; i >= 0; i--) {
                    w = H[i][i] - p;
                    r = ZERO;
                    for (int j = l; j <= n; j++) {
                        r = r + (H[i][j] * H[j][n]);
                    }
                    if (e[i] < ZERO) {
                        z = w;
                        s = r;
                    } else {
                        l = i;
                        if (e[i] == ZERO) {
                            if (w != ZERO) {
                                H[i][n] = -r / w;
                            } else {
                                H[i][n] = -r / (eps * norm);
                            }

                            // Solve real equations

                        } else {
                            x = H[i][i + 1];
                            y = H[i + 1][i];
                            q = ((d[i] - p) * (d[i] - p)) + (e[i] * e[i]);
                            t = ((x * s) - (z * r)) / q;
                            H[i][n] = t;
                            if (Math.abs(x) > Math.abs(z)) {
                                H[i + 1][n] = (-r - (w * t)) / x;
                            } else {
                                H[i + 1][n] = (-s - (y * t)) / z;
                            }
                        }

                        // Overflow control

                        t = Math.abs(H[i][n]);
                        if (((eps * t) * t) > 1) {
                            for (int j = i; j <= n; j++) {
                                H[j][n] = H[j][n] / t;
                            }
                        }
                    }
                }

                // Complex vector

            } else if (q < 0) {
                int l = n - 1;

                // Last vector component imaginary so matrix is triangular

                if (Math.abs(H[n][n - 1]) > Math.abs(H[n - 1][n])) {
                    H[n - 1][n - 1] = q / H[n][n - 1];
                    H[n - 1][n] = -(H[n][n] - p) / H[n][n - 1];
                } else {
                    this.cdiv(ZERO, -H[n - 1][n], H[n - 1][n - 1] - p, q);
                    H[n - 1][n - 1] = cdivr;
                    H[n - 1][n] = cdivi;
                }
                H[n][n - 1] = ZERO;
                H[n][n] = ONE;
                for (int i = n - 2; i >= 0; i--) {
                    double ra, sa, vr, vi;
                    ra = ZERO;
                    sa = ZERO;
                    for (int j = l; j <= n; j++) {
                        ra = ra + (H[i][j] * H[j][n - 1]);
                        sa = sa + (H[i][j] * H[j][n]);
                    }
                    w = H[i][i] - p;

                    if (e[i] < ZERO) {
                        z = w;
                        r = ra;
                        s = sa;
                    } else {
                        l = i;
                        if (e[i] == 0) {
                            this.cdiv(-ra, -sa, w, q);
                            H[i][n - 1] = cdivr;
                            H[i][n] = cdivi;
                        } else {

                            // Solve complex equations

                            x = H[i][i + 1];
                            y = H[i + 1][i];
                            vr = (((d[i] - p) * (d[i] - p)) + (e[i] * e[i])) - (q * q);
                            vi = (d[i] - p) * TWO * q;
                            if ((vr == ZERO) & (vi == ZERO)) {
                                vr = eps * norm * (Math.abs(w) + Math.abs(q) + Math.abs(x) + Math.abs(y) + Math.abs(z));
                            }
                            this.cdiv(((x * r) - (z * ra)) + (q * sa), (x * s) - (z * sa) - (q * ra), vr, vi);
                            H[i][n - 1] = cdivr;
                            H[i][n] = cdivi;
                            if (Math.abs(x) > (Math.abs(z) + Math.abs(q))) {
                                H[i + 1][n - 1] = ((-ra - (w * H[i][n - 1])) + (q * H[i][n])) / x;
                                H[i + 1][n] = (-sa - (w * H[i][n]) - (q * H[i][n - 1])) / x;
                            } else {
                                this.cdiv(-r - (y * H[i][n - 1]), -s - (y * H[i][n]), z, q);
                                H[i + 1][n - 1] = cdivr;
                                H[i + 1][n] = cdivi;
                            }
                        }

                        // Overflow control
                        t = Math.max(Math.abs(H[i][n - 1]), Math.abs(H[i][n]));
                        if (((eps * t) * t) > 1) {
                            for (int j = i; j <= n; j++) {
                                H[j][n - 1] = H[j][n - 1] / t;
                                H[j][n] = H[j][n] / t;
                            }
                        }
                    }
                }
            }
        }

        // Vectors of isolated roots
        for (int i = 0; i < nn; i++) {
            if ((i < low) | (i > high)) {
                for (int j = i; j < nn; j++) {
                    //V[i][j] = H[i][j];
                    Vt[j][i] = H[i][j];
                }
            }
        }

        // Back transformation to get eigenvectors of original matrix
        for (int j = nn - 1; j >= low; j--) {
            for (int i = low; i <= high; i++) {
                z = ZERO;
                for (int k = low; k <= Math.min(j, high); k++) {
                    //z = z + (V[i][k] * H[k][j]);
                    z = z + (Vt[k][i] * H[k][j]);
                }
                //V[i][j] = z;
                Vt[j][i] = z;
            }
        }
    }

    private void orthes() {

        //  This is derived from the Algol procedures orthes and ortran,
        //  by Martin and Wilkinson, Handbook for Auto. Comp.,
        //  Vol.ii-Linear Algebra, and the corresponding
        //  Fortran subroutines in EISPACK.

        final int low = 0;
        final int high = n - 1;

        /**
         * Working storage for nonsymmetric algorithm.
         *
         * @serial working storage for nonsymmetric algorithm.
         */
        final double[] ort = new double[n];

        for (int m = low + 1; m <= (high - 1); m++) {

            // Scale column.

            double scale = ZERO;
            for (int i = m; i <= high; i++) {
                scale = scale + Math.abs(H[i][m - 1]);
            }
            if (scale != ZERO) {

                // Compute Householder transformation.

                double h = ZERO;
                for (int i = high; i >= m; i--) {
                    ort[i] = H[i][m - 1] / scale;
                    h += ort[i] * ort[i];
                }
                double g = Math.sqrt(h);
                if (ort[m] > 0) {
                    g = -g;
                }
                h = h - (ort[m] * g);
                ort[m] = ort[m] - g;

                // Apply Householder similarity transformation
                // H = (I-u*u'/h)*H*(I-u*u')/h)

                for (int j = m; j < n; j++) {
                    double f = ZERO;
                    for (int i = high; i >= m; i--) {
                        f += ort[i] * H[i][j];
                    }
                    f = f / h;
                    for (int i = m; i <= high; i++) {
                        H[i][j] -= f * ort[i];
                    }
                }

                for (int i = 0; i <= high; i++) {
                    double f = ZERO;
                    for (int j = high; j >= m; j--) {
                        f += ort[j] * H[i][j];
                    }
                    f = f / h;
                    for (int j = m; j <= high; j++) {
                        H[i][j] -= f * ort[j];
                    }
                }
                ort[m] = scale * ort[m];
                H[m][m - 1] = scale * g;
            }
        }

        // Accumulate transformations (Algol's ortran).
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                //V[i][j] = (i == j ? ONE : ZERO);
                Vt[j][i] = (i == j ? ONE : ZERO);
            }
        }

        for (int m = high - 1; m >= (low + 1); m--) {
            if (H[m][m - 1] != ZERO) {
                for (int i = m + 1; i <= high; i++) {
                    ort[i] = H[i][m - 1];
                }
                for (int j = m; j <= high; j++) {
                    double g = ZERO;
                    for (int i = m; i <= high; i++) {
                        //g += ort[i] * V[i][j];
                        g += ort[i] * Vt[j][i];
                    }
                    // Double division avoids possible underflow
                    g = (g / ort[m]) / H[m][m - 1];
                    for (int i = m; i <= high; i++) {
                        //V[i][j] += g * ort[i];
                        Vt[j][i] += g * ort[i];
                    }
                }
            }
        }
    }

    private void rot1(final double[] tmpVt_i, final double[] tmpVt_i1, final double c, final double s) {
        double h;
        for (int k = 0; k < n; k++) {
            //h = V[k][i + 1];
            h = tmpVt_i1[k];
            //V[k][i + 1] = (s * V[k][i]) + (c * h);
            tmpVt_i1[k] = (s * tmpVt_i[k]) + (c * h);
            //V[k][i] = (c * V[k][i]) - (s * h);
            tmpVt_i[k] = (c * tmpVt_i[k]) - (s * h);
        }
    }

    private void tql2() {
        //  This is derived from the Algol procedures tql2, by
        //  Bowdler, Martin, Reinsch, and Wilkinson, Handbook for
        //  Auto. Comp., Vol.ii-Linear Algebra, and the corresponding
        //  Fortran subroutine in EISPACK.

        for (int i = 1; i < n; i++) {
            e[i - 1] = e[i];
        }
        e[n - 1] = ZERO;

        double f = ZERO;
        double tst1 = ZERO;
        for (int l = 0; l < n; l++) {

            // Find small subdiagonal element
            tst1 = Math.max(tst1, Math.abs(d[l]) + Math.abs(e[l]));
            int m = l;
            while (m < n) {
                if (Math.abs(e[m]) <= (MACHINE_EPSILON * tst1)) {
                    break;
                }
                m++;
            }

            // If m == l, d[l] is an eigenvalue, otherwise, iterate.
            if (m > l) {
                int iter = 0;
                do {
                    iter = iter + 1; // (Could check iteration count here.)

                    // Compute implicit shift
                    double g = d[l];
                    double p = (d[l + 1] - g) / (TWO * e[l]);
                    double r = Maths.hypot(p, ONE);
                    if (p < 0) {
                        r = -r;
                    }
                    d[l] = e[l] / (p + r);
                    d[l + 1] = e[l] * (p + r);
                    final double dl1 = d[l + 1];
                    double h = g - d[l];
                    for (int i = l + 2; i < n; i++) {
                        d[i] -= h;
                    }
                    f = f + h;

                    // Implicit QL transformation.
                    p = d[m];
                    double c = ONE;
                    double c2 = c;
                    double c3 = c;
                    final double el1 = e[l + 1];
                    double s = ZERO;
                    double s2 = ZERO;
                    for (int i = m - 1; i >= l; i--) {
                        c3 = c2;
                        c2 = c;
                        s2 = s;
                        g = c * e[i];
                        h = c * p;
                        r = Maths.hypot(p, e[i]);
                        e[i + 1] = s * r;
                        s = e[i] / r;
                        c = p / r;
                        p = (c * d[i]) - (s * g);
                        d[i + 1] = h + (s * ((c * g) + (s * d[i])));

                        // Accumulate transformation.
                        this.rot1(Vt[i], Vt[i + 1], c, s);
                    }
                    p = (-s * s2 * c3 * el1 * e[l]) / dl1;
                    e[l] = s * p;
                    d[l] = c * p;

                    // Check for convergence.
                } while (Math.abs(e[l]) > (MACHINE_EPSILON * tst1));
            }
            d[l] = d[l] + f;
            e[l] = ZERO;
        }

        // Sort eigenvalues and corresponding vectors.
        for (int i = 0; i < (n - 1); i++) {

            double[] tmpCol;

            int k = i;
            double p = d[i];
            for (int j = i + 1; j < n; j++) {
                if (d[j] < p) {
                    k = j;
                    p = d[j];
                }
            }
            if (k != i) {
                d[k] = d[i];
                d[i] = p;

                tmpCol = Vt[i];
                Vt[i] = Vt[k];
                Vt[k] = tmpCol;

                //                for (int j = 0; j < n; j++) {
                //                    //p = V[j][i];
                //                    p = Vt[i][j];
                //                    //V[j][i] = V[j][k];
                //                    Vt[i][j] = Vt[k][j];
                //                    //V[j][k] = p;
                //                    Vt[k][j] = p;
                //                }
            }
        }
    }

    @Override
    protected MatrixStore<Double> doGetInverse(final PrimitiveDenseStore preallocated) {
        // TODO Auto-generated method stub
        return null;
    }

    abstract boolean doDecompose(final double[][] data);

    void doDecomposeGeneral(final double[][] data) {

        n = data.length;
        Vt = new double[n][n];
        d = new double[n];
        e = new double[n];

        H = data;

        //        for (int j = 0; j < n; j++) {
        //            for (int i = 0; i < n; i++) {
        //                H[i][j] = A[i][j];
        //            }
        //        }

        // Reduce to Hessenberg form.
        this.orthes();

        // Reduce Hessenberg to real Schur form.
        this.hqr2();

    }

    void doDecomposeSymmetric(final double[][] data) {

        n = data.length;
        Vt = data;
        d = new double[n];
        e = new double[n];

        //        for (int i = 0; i < n; i++) {
        //            for (int j = 0; j < n; j++) {
        //                V[i][j] = A[i][j];
        //            }
        //        }

        // Tridiagonalize.
        HouseholderHermitian.tred2jj(Vt, d, e, true);

        // Diagonalize.
        this.tql2();
    }

    /**
     * Return the imaginary parts of the eigenvalues
     *
     * @return imag(diag(D))
     */
    double[] getImagEigenvalues() {
        return e;
    }

    /**
     * Return the real parts of the eigenvalues
     *
     * @return real(diag(D))
     */
    double[] getRealEigenvalues() {
        return d;
    }

}

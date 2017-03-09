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

import java.util.Optional;

import org.ojalgo.access.Access2D;
import org.ojalgo.access.Access2D.Collectable;
import org.ojalgo.access.Structure2D;
import org.ojalgo.array.Array1D;
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.function.aggregator.ComplexAggregator;
import org.ojalgo.matrix.store.ElementsSupplier;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.RawStore;
import org.ojalgo.matrix.store.operation.HouseholderHermitian;
import org.ojalgo.matrix.task.TaskException;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.PrimitiveScalar;

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
        boolean doDecompose(final double[][] data, final boolean valuesOnly) {

            if (this.checkSymmetry()) {
                this.doDecomposeSymmetric(data, valuesOnly);
            } else {
                this.doDecomposeGeneral(data, valuesOnly);
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
        boolean doDecompose(final double[][] data, final boolean valuesOnly) {

            this.doDecomposeGeneral(data, valuesOnly);

            return this.computed(true);
        }

    }

    static final class Symmetric extends RawEigenvalue implements MatrixDecomposition.Solver<Double> {

        Symmetric() {
            super();
        }

        public MatrixStore<Double> getSolution(final Collectable<Double, ? super PhysicalStore<Double>> rhs) {
            final long numberOfEquations = rhs.countRows();
            final DecompositionStore<Double> tmpPreallocated = this.allocate(numberOfEquations, numberOfEquations);
            return this.getSolution(rhs, tmpPreallocated);
        }

        public boolean isHermitian() {
            return true;
        }

        public PhysicalStore<Double> preallocate(final Structure2D template) {
            final long numberOfEquations = template.countRows();
            return this.allocate(numberOfEquations, numberOfEquations);
        }

        public PhysicalStore<Double> preallocate(final Structure2D templateBody, final Structure2D templateRHS) {
            return this.allocate(templateBody.countRows(), templateRHS.countColumns());
        }

        @Override
        boolean doDecompose(final double[][] data, final boolean valuesOnly) {

            this.doDecomposeSymmetric(data, valuesOnly);

            return this.computed(true);
        }

    }

    private transient double cdivr, cdivi;

    /**
     * Arrays for internal storage of eigenvalues.
     *
     * @serial internal storage of eigenvalues.
     */
    private double[] d = null, e = null;

    private transient MatrixStore<Double> myInverse = null;

    /**
     * Array for internal storage of eigenvectors.
     *
     * @serial internal storage of eigenvectors.
     */
    private double[][] myTransposedV = null;

    /**
     * Row and column dimension (square matrix).
     *
     * @serial matrix dimension.
     */
    private int n;

    protected RawEigenvalue() {
        super();
    }

    public Double calculateDeterminant(final Access2D<?> matrix) {

        final double[][] tmpData = this.reset(matrix, false);

        this.getRawInPlaceStore().fillMatching(matrix);

        this.doDecompose(tmpData, true);

        return this.getDeterminant();
    }

    public boolean computeValuesOnly(final ElementsSupplier<Double> matrix) {

        final double[][] tmpData = this.reset(matrix, false);

        matrix.supplyTo(this.getRawInPlaceStore());

        return this.doDecompose(tmpData, true);
    }

    public boolean decompose(final Access2D.Collectable<Double, ? super PhysicalStore<Double>> matrix) {

        final double[][] tmpData = this.reset(matrix, false);

        matrix.supplyTo(this.getRawInPlaceStore());

        return this.doDecompose(tmpData, false);
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

        final double[] tmpRe = this.getRealParts();
        final double[] tmpIm = this.getImaginaryParts();

        final Array1D<ComplexNumber> retVal = Array1D.COMPLEX.makeZero(tmpRe.length);

        for (int i = 0; i < retVal.size(); i++) {
            retVal.set(i, ComplexNumber.of(tmpRe[i], tmpIm[i]));
        }

        // retVal.sortDescending();

        return retVal;
    }

    public void getEigenvalues(final double[] realParts, final Optional<double[]> imaginaryParts) {

        final int length = realParts.length;

        System.arraycopy(this.getRealParts(), 0, realParts, 0, length);

        if (imaginaryParts.isPresent()) {
            System.arraycopy(this.getImaginaryParts(), 0, imaginaryParts.get(), 0, length);
        }
    }

    public MatrixStore<Double> getInverse() {
        return this.getInverse(this.allocate(n, n));
    }

    public MatrixStore<Double> getInverse(final PhysicalStore<Double> preallocated) {

        if (myInverse == null) {

            final int dim = d.length;

            final RawStore tmpMtrx = new RawStore(dim, dim);

            double max = ONE;

            for (int i = 0; i < dim; i++) {
                final double val = d[i];
                max = MAX.invoke(max, ABS.invoke(val));
                if (PrimitiveScalar.isSmall(max, val)) {
                    for (int j = 0; j < dim; j++) {
                        tmpMtrx.set(i, j, ZERO);
                    }
                } else {
                    final double[] colVi = myTransposedV[i];
                    for (int j = 0; j < dim; j++) {
                        tmpMtrx.set(i, j, colVi[j] / val);
                    }
                }
            }

            myInverse = this.getV().multiply(tmpMtrx);
        }

        return myInverse;
    }

    public MatrixStore<Double> getSolution(final Collectable<Double, ? super PhysicalStore<Double>> rhs, final PhysicalStore<Double> preallocated) {
        return null;
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
    public MatrixStore<Double> getV() {
        return new RawStore(myTransposedV, n, n).logical().transpose().get();
    }

    public MatrixStore<Double> invert(final Access2D<?> original, final PhysicalStore<Double> preallocated) throws TaskException {

        final double[][] tmpData = this.reset(original, false);

        this.getRawInPlaceStore().fillMatching(original);

        this.doDecompose(tmpData, false);

        if (this.isSolvable()) {
            return this.getInverse(preallocated);
        } else {
            throw TaskException.newNotInvertible();
        }
    }

    public boolean isOrdered() {
        return !this.isHermitian();
    }

    public boolean isSolvable() {
        return this.isComputed() && this.isHermitian();
    }

    public MatrixStore<Double> reconstruct() {
        return Eigenvalue.reconstruct(this);
    }

    @Override
    public void reset() {
        myInverse = null;
    }

    public MatrixStore<Double> solve(final Access2D<?> body, final Access2D<?> rhs, final PhysicalStore<Double> preallocated) throws TaskException {

        final double[][] tmpData = this.reset(body, false);

        this.getRawInPlaceStore().fillMatching(body);

        this.doDecompose(tmpData, false);

        if (this.isSolvable()) {

            preallocated.fillMatching(rhs);

            return this.getInverse().multiply(preallocated);

        } else {
            throw TaskException.newNotSolvable();
        }
    }

    public MatrixStore<Double> solve(final MatrixStore<Double> rhs, final DecompositionStore<Double> preallocated) {
        return null;
    }

    /**
     * Complex scalar division.
     */
    private void cdiv(final double xr, final double xi, final double yr, final double yi) {
        double r, d;
        if (ABS.invoke(yr) > ABS.invoke(yi)) {
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

    /**
     * @param mtrxH Array for internal storage of nonsymmetric Hessenberg form.
     */
    private void hqr2(final double[][] mtrxH) {

        //  This is derived from the Algol procedure hqr2,
        //  by Martin and Wilkinson, Handbook for Auto. Comp.,
        //  Vol.ii-Linear Algebra, and the corresponding
        //  Fortran subroutine in EISPACK.

        // Initialize

        final int nn = n;
        int n = nn - 1;
        final int low = 0;
        final int high = nn - 1;
        double exshift = ZERO;
        double p = 0, q = 0, r = 0, s = 0, z = 0, t, w, x, y;

        // Store roots isolated by balanc and compute matrix norm

        double norm = ZERO;
        for (int i = 0; i < nn; i++) {
            if ((i < low) | (i > high)) {
                d[i] = mtrxH[i][i];
                e[i] = ZERO;
            }
            for (int j = Math.max(i - 1, 0); j < nn; j++) {
                norm = norm + ABS.invoke(mtrxH[i][j]);
            }
        }

        // Outer loop over eigenvalue index

        int iter = 0;
        while (n >= low) {

            // Look for single small sub-diagonal element

            int l = n;
            while (l > low) {
                s = ABS.invoke(mtrxH[l - 1][l - 1]) + ABS.invoke(mtrxH[l][l]);
                // if (s == ZERO) {
                if (Double.compare(s, ZERO) == 0) {
                    s = norm;
                }
                if (ABS.invoke(mtrxH[l][l - 1]) < (MACHINE_EPSILON * s)) {
                    break;
                }
                l--;
            }

            // Check for convergence
            // One root found

            if (l == n) {
                mtrxH[n][n] = mtrxH[n][n] + exshift;
                d[n] = mtrxH[n][n];
                e[n] = ZERO;
                n--;
                iter = 0;

                // Two roots found

            } else if (l == (n - 1)) {
                w = mtrxH[n][n - 1] * mtrxH[n - 1][n];
                p = (mtrxH[n - 1][n - 1] - mtrxH[n][n]) / TWO;
                q = (p * p) + w;
                z = SQRT.invoke(ABS.invoke(q));
                mtrxH[n][n] = mtrxH[n][n] + exshift;
                mtrxH[n - 1][n - 1] = mtrxH[n - 1][n - 1] + exshift;
                x = mtrxH[n][n];

                // Real pair

                if (q >= 0) {
                    if (p >= 0) {
                        z = p + z;
                    } else {
                        z = p - z;
                    }
                    d[n - 1] = x + z;
                    d[n] = d[n - 1];
                    // if (z != ZERO) {
                    if (Double.compare(z, ZERO) != 0) {
                        d[n] = x - (w / z);
                    }
                    e[n - 1] = ZERO;
                    e[n] = ZERO;
                    x = mtrxH[n][n - 1];
                    s = ABS.invoke(x) + ABS.invoke(z);
                    p = x / s;
                    q = z / s;
                    r = SQRT.invoke((p * p) + (q * q));
                    p = p / r;
                    q = q / r;

                    // Row modification

                    for (int j = n - 1; j < nn; j++) {
                        z = mtrxH[n - 1][j];
                        mtrxH[n - 1][j] = (q * z) + (p * mtrxH[n][j]);
                        mtrxH[n][j] = (q * mtrxH[n][j]) - (p * z);
                    }

                    // Column modification

                    for (int i = 0; i <= n; i++) {
                        z = mtrxH[i][n - 1];
                        mtrxH[i][n - 1] = (q * z) + (p * mtrxH[i][n]);
                        mtrxH[i][n] = (q * mtrxH[i][n]) - (p * z);
                    }

                    // Accumulate transformations
                    if (myTransposedV != null) {
                        for (int i = low; i <= high; i++) {
                            //z = V[i][n - 1];
                            z = myTransposedV[n - 1][i];
                            //V[i][n - 1] = (q * z) + (p * V[i][n]);
                            myTransposedV[n - 1][i] = (q * z) + (p * myTransposedV[n][i]);
                            //V[i][n] = (q * V[i][n]) - (p * z);
                            myTransposedV[n][i] = (q * myTransposedV[n][i]) - (p * z);
                        }
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

                x = mtrxH[n][n];
                y = ZERO;
                w = ZERO;
                if (l < n) {
                    y = mtrxH[n - 1][n - 1];
                    w = mtrxH[n][n - 1] * mtrxH[n - 1][n];
                }

                // Wilkinson's original ad hoc shift

                if (iter == 10) {
                    exshift += x;
                    for (int i = low; i <= n; i++) {
                        mtrxH[i][i] -= x;
                    }
                    s = ABS.invoke(mtrxH[n][n - 1]) + ABS.invoke(mtrxH[n - 1][n - 2]);
                    x = y = 0.75 * s;
                    w = -0.4375 * s * s;
                }

                // MATLAB's new ad hoc shift

                if (iter == 30) {
                    s = (y - x) / TWO;
                    s = (s * s) + w;
                    if (s > 0) {
                        s = SQRT.invoke(s);
                        if (y < x) {
                            s = -s;
                        }
                        s = x - (w / (((y - x) / TWO) + s));
                        for (int i = low; i <= n; i++) {
                            mtrxH[i][i] -= s;
                        }
                        exshift += s;
                        x = y = w = 0.964;
                    }
                }

                iter = iter + 1; // (Could check iteration count here.)

                // Look for two consecutive small sub-diagonal elements

                int m = n - 2;
                while (m >= l) {
                    z = mtrxH[m][m];
                    r = x - z;
                    s = y - z;
                    p = (((r * s) - w) / mtrxH[m + 1][m]) + mtrxH[m][m + 1];
                    q = mtrxH[m + 1][m + 1] - z - r - s;
                    r = mtrxH[m + 2][m + 1];
                    s = ABS.invoke(p) + ABS.invoke(q) + ABS.invoke(r);
                    p = p / s;
                    q = q / s;
                    r = r / s;
                    if (m == l) {
                        break;
                    }
                    if ((ABS.invoke(mtrxH[m][m - 1]) * (ABS.invoke(q) + ABS.invoke(r))) < (MACHINE_EPSILON
                            * (ABS.invoke(p) * (ABS.invoke(mtrxH[m - 1][m - 1]) + ABS.invoke(z) + ABS.invoke(mtrxH[m + 1][m + 1]))))) {
                        break;
                    }
                    m--;
                }

                for (int i = m + 2; i <= n; i++) {
                    mtrxH[i][i - 2] = ZERO;
                    if (i > (m + 2)) {
                        mtrxH[i][i - 3] = ZERO;
                    }
                }

                // Double QR step involving rows l:n and columns m:n

                for (int k = m; k <= (n - 1); k++) {
                    final boolean notlast = (k != (n - 1));
                    if (k != m) {
                        p = mtrxH[k][k - 1];
                        q = mtrxH[k + 1][k - 1];
                        r = (notlast ? mtrxH[k + 2][k - 1] : ZERO);
                        x = ABS.invoke(p) + ABS.invoke(q) + ABS.invoke(r);
                        // if (x == ZERO) {
                        if (Double.compare(x, ZERO) == 0) {
                            continue;
                        }
                        p = p / x;
                        q = q / x;
                        r = r / x;
                    }

                    s = SQRT.invoke((p * p) + (q * q) + (r * r));
                    if (p < 0) {
                        s = -s;
                    }
                    if (s != 0) {
                        if (k != m) {
                            mtrxH[k][k - 1] = -s * x;
                        } else if (l != m) {
                            mtrxH[k][k - 1] = -mtrxH[k][k - 1];
                        }
                        p = p + s;
                        x = p / s;
                        y = q / s;
                        z = r / s;
                        q = q / p;
                        r = r / p;

                        // Row modification
                        for (int j = k; j < nn; j++) {
                            p = mtrxH[k][j] + (q * mtrxH[k + 1][j]);
                            if (notlast) {
                                p = p + (r * mtrxH[k + 2][j]);
                                mtrxH[k + 2][j] = mtrxH[k + 2][j] - (p * z);
                            }
                            mtrxH[k][j] = mtrxH[k][j] - (p * x);
                            mtrxH[k + 1][j] = mtrxH[k + 1][j] - (p * y);
                        }

                        // Column modification
                        for (int i = 0; i <= Math.min(n, k + 3); i++) {
                            p = (x * mtrxH[i][k]) + (y * mtrxH[i][k + 1]);
                            if (notlast) {
                                p = p + (z * mtrxH[i][k + 2]);
                                mtrxH[i][k + 2] = mtrxH[i][k + 2] - (p * r);
                            }
                            mtrxH[i][k] = mtrxH[i][k] - p;
                            mtrxH[i][k + 1] = mtrxH[i][k + 1] - (p * q);
                        }

                        // Accumulate transformations
                        if (myTransposedV != null) {
                            for (int i = low; i <= high; i++) {
                                //p = (x * V[i][k]) + (y * V[i][k + 1]);
                                p = (x * myTransposedV[k][i]) + (y * myTransposedV[k + 1][i]);
                                if (notlast) {
                                    //p = p + (z * V[i][k + 2]);
                                    p = p + (z * myTransposedV[k + 2][i]);
                                    //V[i][k + 2] = V[i][k + 2] - (p * r);
                                    myTransposedV[k + 2][i] = myTransposedV[k + 2][i] - (p * r);
                                }
                                //V[i][k] = V[i][k] - p;
                                myTransposedV[k][i] = myTransposedV[k][i] - p;
                                //V[i][k + 1] = V[i][k + 1] - (p * q);
                                myTransposedV[k + 1][i] = myTransposedV[k + 1][i] - (p * q);
                            }
                        }

                    } // (s != 0)
                } // k loop
            } // check convergence
        } // while (n >= low)

        // Backsubstitute to find vectors of upper triangular form

        // if (norm == ZERO) {
        if (Double.compare(norm, ZERO) == 0) {
            return;
        }

        for (n = nn - 1; n >= 0; n--) {
            p = d[n];
            q = e[n];

            // Real vector

            if (q == 0) {
                int l = n;
                mtrxH[n][n] = ONE;
                for (int i = n - 1; i >= 0; i--) {
                    w = mtrxH[i][i] - p;
                    r = ZERO;
                    for (int j = l; j <= n; j++) {
                        r = r + (mtrxH[i][j] * mtrxH[j][n]);
                    }
                    if (e[i] < ZERO) {
                        z = w;
                        s = r;
                    } else {
                        l = i;
                        if (e[i] == ZERO) {
                            // if (w != ZERO) {
                            if (Double.compare(w, ZERO) != 0) {
                                mtrxH[i][n] = -r / w;
                            } else {
                                mtrxH[i][n] = -r / (MACHINE_EPSILON * norm);
                            }

                            // Solve real equations

                        } else {
                            x = mtrxH[i][i + 1];
                            y = mtrxH[i + 1][i];
                            q = ((d[i] - p) * (d[i] - p)) + (e[i] * e[i]);
                            t = ((x * s) - (z * r)) / q;
                            mtrxH[i][n] = t;
                            if (ABS.invoke(x) > ABS.invoke(z)) {
                                mtrxH[i + 1][n] = (-r - (w * t)) / x;
                            } else {
                                mtrxH[i + 1][n] = (-s - (y * t)) / z;
                            }
                        }

                        // Overflow control

                        t = ABS.invoke(mtrxH[i][n]);
                        if (((MACHINE_EPSILON * t) * t) > 1) {
                            for (int j = i; j <= n; j++) {
                                mtrxH[j][n] = mtrxH[j][n] / t;
                            }
                        }
                    }
                }

                // Complex vector

            } else if (q < 0) {
                int l = n - 1;

                // Last vector component imaginary so matrix is triangular

                if (ABS.invoke(mtrxH[n][n - 1]) > ABS.invoke(mtrxH[n - 1][n])) {
                    mtrxH[n - 1][n - 1] = q / mtrxH[n][n - 1];
                    mtrxH[n - 1][n] = -(mtrxH[n][n] - p) / mtrxH[n][n - 1];
                } else {
                    this.cdiv(ZERO, -mtrxH[n - 1][n], mtrxH[n - 1][n - 1] - p, q);
                    mtrxH[n - 1][n - 1] = cdivr;
                    mtrxH[n - 1][n] = cdivi;
                }
                mtrxH[n][n - 1] = ZERO;
                mtrxH[n][n] = ONE;
                for (int i = n - 2; i >= 0; i--) {
                    double ra, sa, vr, vi;
                    ra = ZERO;
                    sa = ZERO;
                    for (int j = l; j <= n; j++) {
                        ra = ra + (mtrxH[i][j] * mtrxH[j][n - 1]);
                        sa = sa + (mtrxH[i][j] * mtrxH[j][n]);
                    }
                    w = mtrxH[i][i] - p;

                    if (e[i] < ZERO) {
                        z = w;
                        r = ra;
                        s = sa;
                    } else {
                        l = i;
                        if (e[i] == 0) {
                            this.cdiv(-ra, -sa, w, q);
                            mtrxH[i][n - 1] = cdivr;
                            mtrxH[i][n] = cdivi;
                        } else {

                            // Solve complex equations

                            x = mtrxH[i][i + 1];
                            y = mtrxH[i + 1][i];
                            vr = (((d[i] - p) * (d[i] - p)) + (e[i] * e[i])) - (q * q);
                            vi = (d[i] - p) * TWO * q;
                            // if ((vr == ZERO) & (vi == ZERO)) {
                            if ((Double.compare(vr, ZERO) == 0) && (Double.compare(vi, ZERO) == 0)) {
                                vr = MACHINE_EPSILON * norm * (ABS.invoke(w) + ABS.invoke(q) + ABS.invoke(x) + ABS.invoke(y) + ABS.invoke(z));
                            }
                            this.cdiv(((x * r) - (z * ra)) + (q * sa), (x * s) - (z * sa) - (q * ra), vr, vi);
                            mtrxH[i][n - 1] = cdivr;
                            mtrxH[i][n] = cdivi;
                            if (ABS.invoke(x) > (ABS.invoke(z) + ABS.invoke(q))) {
                                mtrxH[i + 1][n - 1] = ((-ra - (w * mtrxH[i][n - 1])) + (q * mtrxH[i][n])) / x;
                                mtrxH[i + 1][n] = (-sa - (w * mtrxH[i][n]) - (q * mtrxH[i][n - 1])) / x;
                            } else {
                                this.cdiv(-r - (y * mtrxH[i][n - 1]), -s - (y * mtrxH[i][n]), z, q);
                                mtrxH[i + 1][n - 1] = cdivr;
                                mtrxH[i + 1][n] = cdivi;
                            }
                        }

                        // Overflow control
                        t = MAX.invoke(ABS.invoke(mtrxH[i][n - 1]), ABS.invoke(mtrxH[i][n]));
                        if (((MACHINE_EPSILON * t) * t) > 1) {
                            for (int j = i; j <= n; j++) {
                                mtrxH[j][n - 1] = mtrxH[j][n - 1] / t;
                                mtrxH[j][n] = mtrxH[j][n] / t;
                            }
                        }
                    }
                }
            }
        }

        if (myTransposedV != null) {

            // Vectors of isolated roots
            for (int i = 0; i < nn; i++) {
                if ((i < low) | (i > high)) {
                    for (int j = i; j < nn; j++) {
                        //V[i][j] = H[i][j];
                        myTransposedV[j][i] = mtrxH[i][j];
                    }
                }
            }

            // Back transformation to get eigenvectors of original matrix
            for (int j = nn - 1; j >= low; j--) {
                for (int i = low; i <= high; i++) {
                    z = ZERO;
                    for (int k = low; k <= Math.min(j, high); k++) {
                        //z = z + (V[i][k] * H[k][j]);
                        z = z + (myTransposedV[k][i] * mtrxH[k][j]);
                    }
                    //V[i][j] = z;
                    myTransposedV[j][i] = z;
                }
            }
        }
    }

    abstract boolean doDecompose(double[][] data, boolean valuesOnly);

    final void doDecomposeGeneral(final double[][] data, final boolean valuesOnly) {

        n = data.length;

        if ((d == null) || (n != d.length)) {
            if (valuesOnly) {
                myTransposedV = null;
            } else {
                myTransposedV = new double[n][n];
            }
            d = new double[n];
            e = new double[n];
        }

        // Reduce to Hessenberg form.
        EvD2D.orthes(data, myTransposedV, d);

        // Reduce Hessenberg to real Schur form.
        this.hqr2(data);

    }

    final void doDecomposeSymmetric(final double[][] data, final boolean valuesOnly) {

        n = data.length;

        if ((d == null) || (n != d.length)) {
            d = new double[n];
            e = new double[n];
        }
        myTransposedV = valuesOnly ? null : data;

        // Tridiagonalize.
        HouseholderHermitian.tred2jj(data, d, e, !valuesOnly);

        // Diagonalize.
        EvD2D.tql2(d, e, myTransposedV);
    }

    /**
     * Return the imaginary parts of the eigenvalues
     *
     * @return imag(diag(D))
     */
    double[] getImaginaryParts() {
        return e;
    }

    /**
     * Return the real parts of the eigenvalues
     *
     * @return real(diag(D))
     */
    double[] getRealParts() {
        return d;
    }

}

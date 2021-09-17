package org.ojalgo.matrix.decomposition;

import static org.ojalgo.function.constant.PrimitiveMath.*;

import org.ojalgo.type.context.NumberContext;

public abstract class EvD2D {

    /**
     * @param mtrxH Array for internal storage of nonsymmetric Hessenberg form.
     * @param d TODO
     * @param e TODO
     * @param trnspV TODO
     */
    public static void hqr2(final double[][] mtrxH, final double[] d, final double[] e, final double[][] trnspV) {

        //  This is derived from the Algol procedure hqr2,
        //  by Martin and Wilkinson, Handbook for Auto. Comp.,
        //  Vol.ii-Linear Algebra, and the corresponding
        //  Fortran subroutine in EISPACK.

        // Initialize

        final int size = d.length;

        int n = size - 1;

        double exshift = ZERO;
        double p = 0, q = 0, r = 0, s = 0, z = 0, t, w, x, y;

        // Store roots isolated by balanc and compute matrix norm

        double norm = ZERO;
        for (int i = 0; i < size; i++) {
            for (int j = Math.max(i - 1, 0); j < size; j++) {
                norm = norm + ABS.invoke(mtrxH[i][j]);
            }
        }

        // Outer loop over eigenvalue index

        int iter = 0;
        while (n >= 0) {

            // Look for single small sub-diagonal element

            int l = n;
            while (l > 0) {
                s = ABS.invoke(mtrxH[l - 1][l - 1]) + ABS.invoke(mtrxH[l][l]);
                // if (s == ZERO) {
                if (NumberContext.compare(s, ZERO) == 0) {
                    s = norm;
                }
                if (ABS.invoke(mtrxH[l][l - 1]) < MACHINE_EPSILON * s) {
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

            } else if (l == n - 1) {
                w = mtrxH[n][n - 1] * mtrxH[n - 1][n];
                p = (mtrxH[n - 1][n - 1] - mtrxH[n][n]) / TWO;
                q = p * p + w;
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
                    if (NumberContext.compare(z, ZERO) != 0) {
                        d[n] = x - w / z;
                    }
                    e[n - 1] = ZERO;
                    e[n] = ZERO;
                    x = mtrxH[n][n - 1];
                    s = ABS.invoke(x) + ABS.invoke(z);
                    p = x / s;
                    q = z / s;
                    r = SQRT.invoke(p * p + q * q);
                    p = p / r;
                    q = q / r;

                    // Row modification

                    for (int j = n - 1; j < size; j++) {
                        z = mtrxH[n - 1][j];
                        mtrxH[n - 1][j] = q * z + p * mtrxH[n][j];
                        mtrxH[n][j] = q * mtrxH[n][j] - p * z;
                    }

                    // Column modification

                    for (int i = 0; i <= n; i++) {
                        z = mtrxH[i][n - 1];
                        mtrxH[i][n - 1] = q * z + p * mtrxH[i][n];
                        mtrxH[i][n] = q * mtrxH[i][n] - p * z;
                    }

                    // Accumulate transformations
                    if (trnspV != null) {
                        for (int i = 0; i <= size - 1; i++) {
                            //z = V[i][n - 1];
                            z = trnspV[n - 1][i];
                            //V[i][n - 1] = (q * z) + (p * V[i][n]);
                            trnspV[n - 1][i] = q * z + p * trnspV[n][i];
                            //V[i][n] = (q * V[i][n]) - (p * z);
                            trnspV[n][i] = q * trnspV[n][i] - p * z;
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
                y = mtrxH[n - 1][n - 1];
                w = mtrxH[n][n - 1] * mtrxH[n - 1][n];

                // Wilkinson's original ad hoc shift

                if (iter > 0 && iter % 11 == 0) {
                    exshift += x;
                    for (int i = 0; i <= n; i++) {
                        mtrxH[i][i] -= x;
                    }
                    s = ABS.invoke(mtrxH[n][n - 1]) + ABS.invoke(mtrxH[n - 1][n - 2]);
                    x = y = 0.75 * s;
                    w = -0.4375 * s * s;
                }

                // MATLAB's new ad hoc shift

                if (iter > 0 && iter % 31 == 0) {
                    s = (y - x) / TWO;
                    s = s * s + w;
                    if (s > 0) {
                        s = SQRT.invoke(s);
                        if (y < x) {
                            s = -s;
                        }
                        s = x - w / ((y - x) / TWO + s);
                        for (int i = 0; i <= n; i++) {
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
                    p = (r * s - w) / mtrxH[m + 1][m] + mtrxH[m][m + 1];
                    q = mtrxH[m + 1][m + 1] - z - r - s;
                    r = mtrxH[m + 2][m + 1];
                    s = ABS.invoke(p) + ABS.invoke(q) + ABS.invoke(r);
                    p = p / s;
                    q = q / s;
                    r = r / s;
                    if (m == l || ABS.invoke(mtrxH[m][m - 1]) * (ABS.invoke(q) + ABS.invoke(r)) < MACHINE_EPSILON
                            * (ABS.invoke(p) * (ABS.invoke(mtrxH[m - 1][m - 1]) + ABS.invoke(z) + ABS.invoke(mtrxH[m + 1][m + 1])))) {
                        break;
                    }
                    m--;
                }

                for (int i = m + 2; i <= n; i++) {
                    mtrxH[i][i - 2] = ZERO;
                    if (i > m + 2) {
                        mtrxH[i][i - 3] = ZERO;
                    }
                }

                // Double QR step involving rows l:n and columns m:n

                for (int k = m; k <= n - 1; k++) {
                    final boolean notlast = k != n - 1;
                    if (k != m) {
                        p = mtrxH[k][k - 1];
                        q = mtrxH[k + 1][k - 1];
                        r = notlast ? mtrxH[k + 2][k - 1] : ZERO;
                        x = ABS.invoke(p) + ABS.invoke(q) + ABS.invoke(r);
                        // if (x == ZERO) {
                        if (NumberContext.compare(x, ZERO) == 0) {
                            continue;
                        }
                        p = p / x;
                        q = q / x;
                        r = r / x;
                    }

                    s = SQRT.invoke(p * p + q * q + r * r);
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
                        for (int j = k; j < size; j++) {
                            p = mtrxH[k][j] + q * mtrxH[k + 1][j];
                            if (notlast) {
                                p = p + r * mtrxH[k + 2][j];
                                mtrxH[k + 2][j] = mtrxH[k + 2][j] - p * z;
                            }
                            mtrxH[k][j] = mtrxH[k][j] - p * x;
                            mtrxH[k + 1][j] = mtrxH[k + 1][j] - p * y;
                        }

                        // Column modification
                        for (int i = 0; i <= Math.min(n, k + 3); i++) {
                            p = x * mtrxH[i][k] + y * mtrxH[i][k + 1];
                            if (notlast) {
                                p = p + z * mtrxH[i][k + 2];
                                mtrxH[i][k + 2] = mtrxH[i][k + 2] - p * r;
                            }
                            mtrxH[i][k] = mtrxH[i][k] - p;
                            mtrxH[i][k + 1] = mtrxH[i][k + 1] - p * q;
                        }

                        // Accumulate transformations
                        if (trnspV != null) {
                            for (int i = 0; i <= size - 1; i++) {
                                //p = (x * V[i][k]) + (y * V[i][k + 1]);
                                p = x * trnspV[k][i] + y * trnspV[k + 1][i];
                                if (notlast) {
                                    //p = p + (z * V[i][k + 2]);
                                    p = p + z * trnspV[k + 2][i];
                                    //V[i][k + 2] = V[i][k + 2] - (p * r);
                                    trnspV[k + 2][i] = trnspV[k + 2][i] - p * r;
                                }
                                //V[i][k] = V[i][k] - p;
                                trnspV[k][i] = trnspV[k][i] - p;
                                //V[i][k + 1] = V[i][k + 1] - (p * q);
                                trnspV[k + 1][i] = trnspV[k + 1][i] - p * q;
                            }
                        }

                    } // (s != 0)
                } // k loop
            } // check convergence
        } // while (n >= low)

        // Backsubstitute to find vectors of upper triangular form

        // if (norm == ZERO) {
        if (NumberContext.compare(norm, ZERO) == 0) {
            return;
        }

        for (n = size - 1; n >= 0; n--) {
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
                        r = r + mtrxH[i][j] * mtrxH[j][n];
                    }
                    if (e[i] < ZERO) {
                        z = w;
                        s = r;
                    } else {
                        l = i;
                        if (e[i] == ZERO) {
                            // if (w != ZERO) {
                            if (NumberContext.compare(w, ZERO) != 0) {
                                mtrxH[i][n] = -r / w;
                            } else {
                                mtrxH[i][n] = -r / (MACHINE_EPSILON * norm);
                            }

                            // Solve real equations

                        } else {
                            x = mtrxH[i][i + 1];
                            y = mtrxH[i + 1][i];
                            q = (d[i] - p) * (d[i] - p) + e[i] * e[i];
                            t = (x * s - z * r) / q;
                            mtrxH[i][n] = t;
                            if (ABS.invoke(x) > ABS.invoke(z)) {
                                mtrxH[i + 1][n] = (-r - w * t) / x;
                            } else {
                                mtrxH[i + 1][n] = (-s - y * t) / z;
                            }
                        }

                        // Overflow control

                        t = ABS.invoke(mtrxH[i][n]);
                        if (MACHINE_EPSILON * t * t > 1) {
                            for (int j = i; j <= n; j++) {
                                mtrxH[j][n] = mtrxH[j][n] / t;
                            }
                        }
                    }
                }

                // Complex vector

            } else if (q < 0) {
                int l = n - 1;

                final double[] cdiv = new double[2];

                // Last vector component imaginary so matrix is triangular

                if (ABS.invoke(mtrxH[n][n - 1]) > ABS.invoke(mtrxH[n - 1][n])) {
                    mtrxH[n - 1][n - 1] = q / mtrxH[n][n - 1];
                    mtrxH[n - 1][n] = -(mtrxH[n][n] - p) / mtrxH[n][n - 1];
                } else {
                    EvD2D.cdiv(ZERO, -mtrxH[n - 1][n], mtrxH[n - 1][n - 1] - p, q, cdiv);
                    mtrxH[n - 1][n - 1] = cdiv[0];
                    mtrxH[n - 1][n] = cdiv[1];
                }
                mtrxH[n][n - 1] = ZERO;
                mtrxH[n][n] = ONE;
                for (int i = n - 2; i >= 0; i--) {
                    double ra, sa, vr, vi;
                    ra = ZERO;
                    sa = ZERO;
                    for (int j = l; j <= n; j++) {
                        ra = ra + mtrxH[i][j] * mtrxH[j][n - 1];
                        sa = sa + mtrxH[i][j] * mtrxH[j][n];
                    }
                    w = mtrxH[i][i] - p;

                    if (e[i] < ZERO) {
                        z = w;
                        r = ra;
                        s = sa;
                    } else {
                        l = i;
                        if (e[i] == 0) {
                            EvD2D.cdiv(-ra, -sa, w, q, cdiv);
                            mtrxH[i][n - 1] = cdiv[0];
                            mtrxH[i][n] = cdiv[1];
                        } else {

                            // Solve complex equations

                            x = mtrxH[i][i + 1];
                            y = mtrxH[i + 1][i];
                            vr = (d[i] - p) * (d[i] - p) + e[i] * e[i] - q * q;
                            vi = (d[i] - p) * TWO * q;
                            // if ((vr == ZERO) & (vi == ZERO)) {
                            if (NumberContext.compare(vr, ZERO) == 0 && NumberContext.compare(vi, ZERO) == 0) {
                                vr = MACHINE_EPSILON * norm * (ABS.invoke(w) + ABS.invoke(q) + ABS.invoke(x) + ABS.invoke(y) + ABS.invoke(z));
                            }
                            EvD2D.cdiv(x * r - z * ra + q * sa, x * s - z * sa - q * ra, vr, vi, cdiv);
                            mtrxH[i][n - 1] = cdiv[0];
                            mtrxH[i][n] = cdiv[1];
                            if (ABS.invoke(x) > ABS.invoke(z) + ABS.invoke(q)) {
                                mtrxH[i + 1][n - 1] = (-ra - w * mtrxH[i][n - 1] + q * mtrxH[i][n]) / x;
                                mtrxH[i + 1][n] = (-sa - w * mtrxH[i][n] - q * mtrxH[i][n - 1]) / x;
                            } else {
                                EvD2D.cdiv(-r - y * mtrxH[i][n - 1], -s - y * mtrxH[i][n], z, q, cdiv);
                                mtrxH[i + 1][n - 1] = cdiv[0];
                                mtrxH[i + 1][n] = cdiv[1];
                            }
                        }

                        // Overflow control
                        t = MAX.invoke(ABS.invoke(mtrxH[i][n - 1]), ABS.invoke(mtrxH[i][n]));
                        if (MACHINE_EPSILON * t * t > 1) {
                            for (int j = i; j <= n; j++) {
                                mtrxH[j][n - 1] = mtrxH[j][n - 1] / t;
                                mtrxH[j][n] = mtrxH[j][n] / t;
                            }
                        }
                    }
                }
            }
        }

        if (trnspV != null) {

            // Back transformation to get eigenvectors of original matrix
            for (int j = size - 1; j >= 0; j--) {
                for (int i = 0; i <= size - 1; i++) {
                    z = ZERO;
                    for (int k = 0; k <= Math.min(j, size - 1); k++) {
                        //z = z + (V[i][k] * H[k][j]);
                        z = z + trnspV[k][i] * mtrxH[k][j];
                    }
                    //V[i][j] = z;
                    trnspV[j][i] = z;
                }
            }
        }
    }

    /**
     * @param mtrxH Array for internal storage of nonsymmetric Hessenberg form.
     * @param vctrWork Temporary work storage
     */
    public static void orthes(final double[][] mtrxH, final double[][] trnspV, final double[] vctrWork) {

        final int size = vctrWork.length;

        final int sizeM1 = size - 1;
        final int sizeM2 = size - 2;
        for (int m = 0 + 1; m <= sizeM2; m++) {
            final int ij = m - 1;

            // Scale column.
            double scale = ZERO;

            for (int i = m; i <= sizeM1; i++) {
                scale = scale + ABS.invoke(mtrxH[i][ij]);
            }

            // if (scale != ZERO) {
            if (NumberContext.compare(scale, ZERO) != 0) {

                // Compute Householder transformation.

                double h = ZERO;
                for (int i = sizeM1; i >= m; i--) {
                    vctrWork[i] = mtrxH[i][ij] / scale;
                    h += vctrWork[i] * vctrWork[i];
                }
                double g = SQRT.invoke(h);
                if (vctrWork[m] > 0) {
                    g = -g;
                }
                h = h - vctrWork[m] * g;
                vctrWork[m] = vctrWork[m] - g;

                // Apply Householder similarity transformation
                // H = (I-u*u'/h)*H*(I-u*u')/h)

                for (int j = m; j < size; j++) {
                    double f = ZERO;
                    for (int i = sizeM1; i >= m; i--) {
                        f += vctrWork[i] * mtrxH[i][j];
                    }
                    f = f / h;
                    for (int i = m; i <= sizeM1; i++) {
                        mtrxH[i][j] -= f * vctrWork[i];
                    }
                }

                for (int i = 0; i <= sizeM1; i++) {
                    double f = ZERO;
                    for (int j = sizeM1; j >= m; j--) {
                        f += vctrWork[j] * mtrxH[i][j];
                    }
                    f = f / h;
                    for (int j = m; j <= sizeM1; j++) {
                        mtrxH[i][j] -= f * vctrWork[j];
                    }
                }
                vctrWork[m] = scale * vctrWork[m];
                mtrxH[m][ij] = scale * g;
            }
        }

        // Accumulate transformations (Algol's ortran).
        if (trnspV != null) {

            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    //V[i][j] = (i == j ? ONE : ZERO);
                    trnspV[j][i] = i == j ? ONE : ZERO;
                }
            }

            for (int m = sizeM2; m >= 0 + 1; m--) {
                if (mtrxH[m][m - 1] != ZERO) {
                    for (int i = m + 1; i <= sizeM1; i++) {
                        vctrWork[i] = mtrxH[i][m - 1];
                    }
                    for (int j = m; j <= sizeM1; j++) {
                        double g = ZERO;
                        for (int i = m; i <= sizeM1; i++) {
                            //g += ort[i] * V[i][j];
                            g += vctrWork[i] * trnspV[j][i];
                        }
                        // Double division avoids possible underflow
                        g = g / vctrWork[m] / mtrxH[m][m - 1];
                        for (int i = m; i <= sizeM1; i++) {
                            //V[i][j] += g * ort[i];
                            trnspV[j][i] += g * vctrWork[i];
                        }
                    }
                }
            }
        }
    }

    /**
     * Complex scalar division.
     */
    private static void cdiv(final double xr, final double xi, final double yr, final double yi, final double[] cdiv) {
        double r, d;
        if (ABS.invoke(yr) > ABS.invoke(yi)) {
            r = yi / yr;
            d = yr + r * yi;
            cdiv[0] = (xr + r * xi) / d;
            cdiv[1] = (xi - r * xr) / d;
        } else {
            r = yr / yi;
            d = yi + r * yr;
            cdiv[0] = (r * xr + xi) / d;
            cdiv[1] = (r * xi - xr) / d;
        }
    }

}

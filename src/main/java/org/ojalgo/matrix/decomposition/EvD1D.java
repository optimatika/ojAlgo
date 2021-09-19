package org.ojalgo.matrix.decomposition;

import static org.ojalgo.function.constant.PrimitiveMath.*;

import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.type.context.NumberContext;

public abstract class EvD1D {

    /**
     * hqr2
     */
    public static double[][] hqr2(final double[] mtrxH, final double[] mtrxV, final boolean allTheWay) {

        final int tmpDiagDim = SQRT.invoke(mtrxH.length);
        final int tmpDiagDimMinusOne = tmpDiagDim - 1;

        // Store roots isolated by balanc and compute matrix norm
        double tmpVal = ZERO;
        for (int j = 0; j < tmpDiagDim; j++) {
            for (int i = Math.min(j + 1, tmpDiagDim - 1); i >= 0; i--) {
                tmpVal += ABS.invoke(mtrxH[i + tmpDiagDim * j]);
            }
        }
        final double tmpNorm1 = tmpVal;

        final double[] d = new double[tmpDiagDim];
        final double[] e = new double[tmpDiagDim];

        double exshift = ZERO;
        double p = 0, q = 0, r = 0, s = 0, z = 0;

        double w, x, y;
        // Outer loop over eigenvalue index
        int iter = 0;
        int n = tmpDiagDimMinusOne;
        while (n >= 0) {

            // Look for single small sub-diagonal element
            int l = n;
            while (l > 0) {
                s = ABS.invoke(mtrxH[l - 1 + tmpDiagDim * (l - 1)]) + ABS.invoke(mtrxH[l + tmpDiagDim * l]);
                // if (s ==  ZERO) {
                if (NumberContext.compare(s, ZERO) == 0) {
                    s = tmpNorm1;
                }
                if (ABS.invoke(mtrxH[l + tmpDiagDim * (l - 1)]) < MACHINE_EPSILON * s) {
                    break;
                }
                l--;
            }

            // Check for convergence
            // One root found
            if (l == n) {
                mtrxH[n + tmpDiagDim * n] = mtrxH[n + tmpDiagDim * n] + exshift;
                d[n] = mtrxH[n + tmpDiagDim * n];
                e[n] = ZERO;
                n--;
                iter = 0;

                // Two roots found
            } else if (l == n - 1) {
                w = mtrxH[n + tmpDiagDim * (n - 1)] * mtrxH[n - 1 + tmpDiagDim * n];
                p = (mtrxH[n - 1 + tmpDiagDim * (n - 1)] - mtrxH[n + tmpDiagDim * n]) / 2.0;
                q = p * p + w;
                z = SQRT.invoke(ABS.invoke(q));
                mtrxH[n + tmpDiagDim * n] = mtrxH[n + tmpDiagDim * n] + exshift;
                mtrxH[n - 1 + tmpDiagDim * (n - 1)] = mtrxH[n - 1 + tmpDiagDim * (n - 1)] + exshift;
                x = mtrxH[n + tmpDiagDim * n];

                // Real pair
                if (q >= 0) {
                    if (p >= 0) {
                        z = p + z;
                    } else {
                        z = p - z;
                    }
                    d[n - 1] = x + z;
                    d[n] = d[n - 1];
                    // if (z !=  ZERO) {
                    if (NumberContext.compare(z, ZERO) != 0) {
                        d[n] = x - w / z;
                    }
                    e[n - 1] = ZERO;
                    e[n] = ZERO;
                    x = mtrxH[n + tmpDiagDim * (n - 1)];
                    s = ABS.invoke(x) + ABS.invoke(z);
                    p = x / s;
                    q = z / s;
                    r = SQRT.invoke(p * p + q * q);
                    p = p / r;
                    q = q / r;

                    // Row modification
                    for (int j = n - 1; j < tmpDiagDim; j++) {
                        z = mtrxH[n - 1 + tmpDiagDim * j];
                        mtrxH[n - 1 + tmpDiagDim * j] = q * z + p * mtrxH[n + tmpDiagDim * j];
                        mtrxH[n + tmpDiagDim * j] = q * mtrxH[n + tmpDiagDim * j] - p * z;
                    }

                    // Column modification
                    for (int i = 0; i <= n; i++) {
                        z = mtrxH[i + tmpDiagDim * (n - 1)];
                        mtrxH[i + tmpDiagDim * (n - 1)] = q * z + p * mtrxH[i + tmpDiagDim * n];
                        mtrxH[i + tmpDiagDim * n] = q * mtrxH[i + tmpDiagDim * n] - p * z;
                    }

                    // Accumulate transformations
                    for (int i = 0; i <= tmpDiagDimMinusOne; i++) {
                        z = mtrxV[i + tmpDiagDim * (n - 1)];
                        mtrxV[i + tmpDiagDim * (n - 1)] = q * z + p * mtrxV[i + tmpDiagDim * n];
                        mtrxV[i + tmpDiagDim * n] = q * mtrxV[i + tmpDiagDim * n] - p * z;
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
                x = mtrxH[n + tmpDiagDim * n];
                y = ZERO;
                w = ZERO;

                y = mtrxH[n - 1 + tmpDiagDim * (n - 1)];
                w = mtrxH[n + tmpDiagDim * (n - 1)] * mtrxH[n - 1 + tmpDiagDim * n];

                // Wilkinson's original ad hoc shift
                if (iter > 0 && iter % 11 == 0) {
                    exshift += x;
                    for (int i = 0; i <= n; i++) {
                        mtrxH[i + tmpDiagDim * i] -= x;
                    }
                    s = ABS.invoke(mtrxH[n + tmpDiagDim * (n - 1)]) + ABS.invoke(mtrxH[n - 1 + tmpDiagDim * (n - 2)]);
                    x = y = 0.75 * s;
                    w = -0.4375 * s * s;
                }

                // MATLAB's new ad hoc shift
                if (iter > 0 && iter % 31 == 0) {
                    s = (y - x) / 2.0;
                    s = s * s + w;
                    if (s > 0) {
                        s = SQRT.invoke(s);
                        if (y < x) {
                            s = -s;
                        }
                        s = x - w / ((y - x) / 2.0 + s);
                        for (int i = 0; i <= n; i++) {
                            mtrxH[i + tmpDiagDim * i] -= s;
                        }
                        exshift += s;
                        x = y = w = 0.964;
                    }
                }

                iter++; // (Could check iteration count here.)

                // Look for two consecutive small sub-diagonal elements
                int m = n - 2;
                while (m >= l) {
                    z = mtrxH[m + tmpDiagDim * m];
                    r = x - z;
                    s = y - z;
                    p = (r * s - w) / mtrxH[m + 1 + tmpDiagDim * m] + mtrxH[m + tmpDiagDim * (m + 1)];
                    q = mtrxH[m + 1 + tmpDiagDim * (m + 1)] - z - r - s;
                    r = mtrxH[m + 2 + tmpDiagDim * (m + 1)];
                    s = ABS.invoke(p) + ABS.invoke(q) + ABS.invoke(r);
                    p = p / s;
                    q = q / s;
                    r = r / s;
                    if (m == l || ABS.invoke(mtrxH[m + tmpDiagDim * (m - 1)]) * (ABS.invoke(q) + ABS.invoke(r)) < MACHINE_EPSILON * (ABS.invoke(p)
                            * (ABS.invoke(mtrxH[m - 1 + tmpDiagDim * (m - 1)]) + ABS.invoke(z) + ABS.invoke(mtrxH[m + 1 + tmpDiagDim * (m + 1)])))) {
                        break;
                    }
                    m--;
                }

                for (int i = m + 2; i <= n; i++) {
                    mtrxH[i + tmpDiagDim * (i - 2)] = ZERO;
                    if (i > m + 2) {
                        mtrxH[i + tmpDiagDim * (i - 3)] = ZERO;
                    }
                }

                // Double QR step involving rows l:n and columns m:n
                for (int k = m; k <= n - 1; k++) {
                    final boolean notlast = k != n - 1;
                    if (k != m) {
                        p = mtrxH[k + tmpDiagDim * (k - 1)];
                        q = mtrxH[k + 1 + tmpDiagDim * (k - 1)];
                        r = notlast ? mtrxH[k + 2 + tmpDiagDim * (k - 1)] : ZERO;
                        x = ABS.invoke(p) + ABS.invoke(q) + ABS.invoke(r);
                        // if (x ==  ZERO) {
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
                            mtrxH[k + tmpDiagDim * (k - 1)] = -s * x;
                        } else if (l != m) {
                            mtrxH[k + tmpDiagDim * (k - 1)] = -mtrxH[k + tmpDiagDim * (k - 1)];
                        }
                        p = p + s;
                        x = p / s;
                        y = q / s;
                        z = r / s;
                        q = q / p;
                        r = r / p;

                        // Row modification
                        for (int j = k; j < tmpDiagDim; j++) {
                            p = mtrxH[k + tmpDiagDim * j] + q * mtrxH[k + 1 + tmpDiagDim * j];
                            if (notlast) {
                                p = p + r * mtrxH[k + 2 + tmpDiagDim * j];
                                mtrxH[k + 2 + tmpDiagDim * j] = mtrxH[k + 2 + tmpDiagDim * j] - p * z;
                            }
                            mtrxH[k + tmpDiagDim * j] = mtrxH[k + tmpDiagDim * j] - p * x;
                            mtrxH[k + 1 + tmpDiagDim * j] = mtrxH[k + 1 + tmpDiagDim * j] - p * y;
                        }

                        // Column modification
                        for (int i = 0; i <= Math.min(n, k + 3); i++) {
                            p = x * mtrxH[i + tmpDiagDim * k] + y * mtrxH[i + tmpDiagDim * (k + 1)];
                            if (notlast) {
                                p = p + z * mtrxH[i + tmpDiagDim * (k + 2)];
                                mtrxH[i + tmpDiagDim * (k + 2)] = mtrxH[i + tmpDiagDim * (k + 2)] - p * r;
                            }
                            mtrxH[i + tmpDiagDim * k] = mtrxH[i + tmpDiagDim * k] - p;
                            mtrxH[i + tmpDiagDim * (k + 1)] = mtrxH[i + tmpDiagDim * (k + 1)] - p * q;
                        }

                        // Accumulate transformations
                        for (int i = 0; i <= tmpDiagDimMinusOne; i++) {
                            p = x * mtrxV[i + tmpDiagDim * k] + y * mtrxV[i + tmpDiagDim * (k + 1)];
                            if (notlast) {
                                p = p + z * mtrxV[i + tmpDiagDim * (k + 2)];
                                mtrxV[i + tmpDiagDim * (k + 2)] = mtrxV[i + tmpDiagDim * (k + 2)] - p * r;
                            }
                            mtrxV[i + tmpDiagDim * k] = mtrxV[i + tmpDiagDim * k] - p;
                            mtrxV[i + tmpDiagDim * (k + 1)] = mtrxV[i + tmpDiagDim * (k + 1)] - p * q;
                        }
                    } // (s != 0)
                } // k loop
            } // check convergence
        } // while (n >= low)

        // Backsubstitute to find vectors of upper triangular form
        // if (allTheWay && (tmpNorm1 !=  ZERO)) {
        if (allTheWay && NumberContext.compare(tmpNorm1, ZERO) != 0) {
            final int tmpDiagDim1 = SQRT.invoke(mtrxH.length);
            final int tmpDiagDimMinusOne1 = tmpDiagDim1 - 1;

            // BasicLogger.debug("r={}, s={}, z={}", r, s, z);

            double p1;
            double q1;
            double t;
            double w1;
            double x1;
            double y1;

            for (int ij = tmpDiagDimMinusOne1; ij >= 0; ij--) {

                p1 = d[ij];
                q1 = e[ij];

                // Real vector
                if (q1 == 0) {
                    int l = ij;
                    mtrxH[ij + tmpDiagDim1 * ij] = 1.0;
                    for (int i = ij - 1; i >= 0; i--) {
                        w1 = mtrxH[i + tmpDiagDim1 * i] - p1;
                        r = ZERO;
                        for (int j = l; j <= ij; j++) {
                            r = r + mtrxH[i + tmpDiagDim1 * j] * mtrxH[j + tmpDiagDim1 * ij];
                        }
                        if (e[i] < ZERO) {
                            z = w1;
                            s = r;
                        } else {
                            l = i;
                            if (e[i] == ZERO) {
                                // if (w !=  ZERO) {
                                if (NumberContext.compare(w1, ZERO) != 0) {
                                    mtrxH[i + tmpDiagDim1 * ij] = -r / w1;
                                } else {
                                    mtrxH[i + tmpDiagDim1 * ij] = -r / (MACHINE_EPSILON * tmpNorm1);
                                }

                                // Solve real equations
                            } else {
                                x1 = mtrxH[i + tmpDiagDim1 * (i + 1)];
                                y1 = mtrxH[i + 1 + tmpDiagDim1 * i];
                                q1 = (d[i] - p1) * (d[i] - p1) + e[i] * e[i];
                                t = (x1 * s - z * r) / q1;
                                mtrxH[i + tmpDiagDim1 * ij] = t;
                                if (ABS.invoke(x1) > ABS.invoke(z)) {
                                    mtrxH[i + 1 + tmpDiagDim1 * ij] = (-r - w1 * t) / x1;
                                } else {
                                    mtrxH[i + 1 + tmpDiagDim1 * ij] = (-s - y1 * t) / z;
                                }
                            }

                            // Overflow control
                            t = ABS.invoke(mtrxH[i + tmpDiagDim1 * ij]);
                            if (MACHINE_EPSILON * t * t > 1) {
                                for (int j = i; j <= ij; j++) {
                                    mtrxH[j + tmpDiagDim1 * ij] = mtrxH[j + tmpDiagDim1 * ij] / t;
                                }
                            }
                        }
                    }

                    // Complex vector
                } else if (q1 < 0) {
                    int l = ij - 1;

                    // Last vector component imaginary so matrix is triangular
                    if (ABS.invoke(mtrxH[ij + tmpDiagDim1 * (ij - 1)]) > ABS.invoke(mtrxH[ij - 1 + tmpDiagDim1 * ij])) {
                        mtrxH[ij - 1 + tmpDiagDim1 * (ij - 1)] = q1 / mtrxH[ij + tmpDiagDim1 * (ij - 1)];
                        mtrxH[ij - 1 + tmpDiagDim1 * ij] = -(mtrxH[ij + tmpDiagDim1 * ij] - p1) / mtrxH[ij + tmpDiagDim1 * (ij - 1)];
                    } else {

                        final ComplexNumber tmpX = ComplexNumber.of(ZERO, -mtrxH[ij - 1 + tmpDiagDim1 * ij]);
                        final double imaginary = q1;
                        final ComplexNumber tmpY = ComplexNumber.of(mtrxH[ij - 1 + tmpDiagDim1 * (ij - 1)] - p1, imaginary);

                        final ComplexNumber tmpZ = tmpX.divide(tmpY);

                        mtrxH[ij - 1 + tmpDiagDim1 * (ij - 1)] = tmpZ.doubleValue();
                        mtrxH[ij - 1 + tmpDiagDim1 * ij] = tmpZ.i;
                    }
                    mtrxH[ij + tmpDiagDim1 * (ij - 1)] = ZERO;
                    mtrxH[ij + tmpDiagDim1 * ij] = 1.0;
                    for (int i = ij - 2; i >= 0; i--) {
                        double ra, sa, vr, vi;
                        ra = ZERO;
                        sa = ZERO;
                        for (int j = l; j <= ij; j++) {
                            ra = ra + mtrxH[i + tmpDiagDim1 * j] * mtrxH[j + tmpDiagDim1 * (ij - 1)];
                            sa = sa + mtrxH[i + tmpDiagDim1 * j] * mtrxH[j + tmpDiagDim1 * ij];
                        }
                        w1 = mtrxH[i + tmpDiagDim1 * i] - p1;

                        if (e[i] < ZERO) {
                            z = w1;
                            r = ra;
                            s = sa;
                        } else {
                            l = i;
                            if (e[i] == 0) {
                                final ComplexNumber tmpX = ComplexNumber.of(-ra, -sa);
                                final double real = w1;
                                final double imaginary = q1;
                                final ComplexNumber tmpY = ComplexNumber.of(real, imaginary);

                                final ComplexNumber tmpZ = tmpX.divide(tmpY);

                                mtrxH[i + tmpDiagDim1 * (ij - 1)] = tmpZ.doubleValue();
                                mtrxH[i + tmpDiagDim1 * ij] = tmpZ.i;
                            } else {

                                // Solve complex equations
                                x1 = mtrxH[i + tmpDiagDim1 * (i + 1)];
                                y1 = mtrxH[i + 1 + tmpDiagDim1 * i];
                                vr = (d[i] - p1) * (d[i] - p1) + e[i] * e[i] - q1 * q1;
                                vi = (d[i] - p1) * 2.0 * q1;
                                // if ((vr ==  ZERO) & (vi ==  ZERO)) {
                                if (NumberContext.compare(vr, ZERO) == 0 && NumberContext.compare(vi, ZERO) == 0) {
                                    vr = MACHINE_EPSILON * tmpNorm1 * (ABS.invoke(w1) + ABS.invoke(q1) + ABS.invoke(x1) + ABS.invoke(y1) + ABS.invoke(z));
                                }

                                final ComplexNumber tmpX = ComplexNumber.of(x1 * r - z * ra + q1 * sa, x1 * s - z * sa - q1 * ra);
                                final double real = vr;
                                final double imaginary = vi;
                                final ComplexNumber tmpY = ComplexNumber.of(real, imaginary);

                                final ComplexNumber tmpZ = tmpX.divide(tmpY);

                                mtrxH[i + tmpDiagDim1 * (ij - 1)] = tmpZ.doubleValue();
                                mtrxH[i + tmpDiagDim1 * ij] = tmpZ.i;

                                if (ABS.invoke(x1) > ABS.invoke(z) + ABS.invoke(q1)) {
                                    mtrxH[i + 1 + tmpDiagDim1 * (ij - 1)] = (-ra - w1 * mtrxH[i + tmpDiagDim1 * (ij - 1)] + q1 * mtrxH[i + tmpDiagDim1 * ij])
                                            / x1;
                                    mtrxH[i + 1 + tmpDiagDim1 * ij] = (-sa - w1 * mtrxH[i + tmpDiagDim1 * ij] - q1 * mtrxH[i + tmpDiagDim1 * (ij - 1)]) / x1;
                                } else {
                                    final ComplexNumber tmpX1 = ComplexNumber.of(-r - y1 * mtrxH[i + tmpDiagDim1 * (ij - 1)],
                                            -s - y1 * mtrxH[i + tmpDiagDim1 * ij]);
                                    final double real1 = z;
                                    final double imaginary1 = q1;
                                    final ComplexNumber tmpY1 = ComplexNumber.of(real1, imaginary1);

                                    final ComplexNumber tmpZ1 = tmpX1.divide(tmpY1);

                                    mtrxH[i + 1 + tmpDiagDim1 * (ij - 1)] = tmpZ1.doubleValue();
                                    mtrxH[i + 1 + tmpDiagDim1 * ij] = tmpZ1.i;
                                }
                            }

                            // Overflow control
                            t = MAX.invoke(ABS.invoke(mtrxH[i + tmpDiagDim1 * (ij - 1)]), ABS.invoke(mtrxH[i + tmpDiagDim1 * ij]));
                            if (MACHINE_EPSILON * t * t > 1) {
                                for (int j = i; j <= ij; j++) {
                                    mtrxH[j + tmpDiagDim1 * (ij - 1)] = mtrxH[j + tmpDiagDim1 * (ij - 1)] / t;
                                    mtrxH[j + tmpDiagDim1 * ij] = mtrxH[j + tmpDiagDim1 * ij] / t;
                                }
                            }
                        }
                    }
                }
            }

            // Back transformation to get eigenvectors of original matrix
            for (int j = tmpDiagDimMinusOne1; j >= 0; j--) {
                for (int i = 0; i <= tmpDiagDimMinusOne1; i++) {
                    z = ZERO;
                    for (int k = 0; k <= j; k++) {
                        z += mtrxV[i + tmpDiagDim1 * k] * mtrxH[k + tmpDiagDim1 * j];
                    }
                    mtrxV[i + tmpDiagDim1 * j] = z;
                }
            }
        }

        return new double[][] { d, e };
    }

    /**
     * @param mtrxH Array for internal storage of nonsymmetric Hessenberg form.
     * @param vctrWork Temporary work storage
     */
    public static void orthes(final double[] mtrxH, final double[] trnspV, final double[] vctrWork) {

        final int size = vctrWork.length;

        final int sizeM1 = size - 1;
        final int sizeM2 = size - 2;
        for (int ij = 0; ij < sizeM2; ij++) {
            final int m = ij + 1;

            // Scale column.
            double tmpColNorm1 = ZERO;

            for (int i = m; i < size; i++) {
                tmpColNorm1 += ABS.invoke(mtrxH[i + size * ij]);
            }

            // if (tmpColNorm1 !=  ZERO) {
            if (NumberContext.compare(tmpColNorm1, ZERO) != 0) {

                // Compute Householder transformation.
                double tmpInvBeta = ZERO;
                for (int i = sizeM1; i >= m; i--) {
                    vctrWork[i] = mtrxH[i + size * ij] / tmpColNorm1;
                    tmpInvBeta += vctrWork[i] * vctrWork[i];
                }
                double g = SQRT.invoke(tmpInvBeta);
                if (vctrWork[m] > 0) {
                    g = -g;
                }
                tmpInvBeta = tmpInvBeta - vctrWork[m] * g;
                vctrWork[m] = vctrWork[m] - g;

                // Apply Householder similarity transformation
                // H = (I-u*u'/h)*H*(I-u*u')/h)
                for (int j = m; j < size; j++) {
                    double f = ZERO;
                    for (int i = sizeM1; i >= m; i--) {
                        f += vctrWork[i] * mtrxH[i + size * j];
                    }
                    f = f / tmpInvBeta;
                    for (int i = m; i <= sizeM1; i++) {
                        mtrxH[i + size * j] -= f * vctrWork[i];
                    }
                }

                for (int i = 0; i < size; i++) {
                    double f = ZERO;
                    for (int j = sizeM1; j >= m; j--) {
                        f += vctrWork[j] * mtrxH[i + size * j];
                    }
                    f = f / tmpInvBeta;
                    for (int j = m; j < size; j++) {
                        mtrxH[i + size * j] -= f * vctrWork[j];
                    }
                }

                vctrWork[m] = tmpColNorm1 * vctrWork[m];
                mtrxH[m + size * ij] = tmpColNorm1 * g;
            }
        }

        // BasicLogger.debug("Jama H", new PrimitiveDenseStore(tmpDiagDim,
        // tmpDiagDim, aMtrxH));

        // Här borde Hessenberg vara klar
        // Nedan börjar uträkningen av Q

        // Accumulate transformations (Algol's ortran).
        for (int ij = sizeM2; ij >= 1; ij--) {
            final int tmpIndex = ij + size * (ij - 1);
            if (mtrxH[tmpIndex] != ZERO) {
                for (int i = ij + 1; i <= sizeM1; i++) {
                    vctrWork[i] = mtrxH[i + size * (ij - 1)];
                }
                for (int j = ij; j <= sizeM1; j++) {
                    double g = ZERO;
                    for (int i = ij; i <= sizeM1; i++) {
                        g += vctrWork[i] * trnspV[i + size * j];
                    }
                    // Double division avoids possible underflow
                    g = g / vctrWork[ij] / mtrxH[tmpIndex];
                    for (int i = ij; i <= sizeM1; i++) {
                        trnspV[i + size * j] += g * vctrWork[i];
                    }
                }
            } else {
                // BasicLogger.debug("Iter V", new
                // PrimitiveDenseStore(tmpDiagDim, tmpDiagDim, aMtrxV));
            }
        }

        // BasicLogger.debug("Jama V", new PrimitiveDenseStore(tmpDiagDim,
        // tmpDiagDim, aMtrxV));

    }

}

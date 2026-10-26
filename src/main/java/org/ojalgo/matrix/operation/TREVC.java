/*
 * Copyright 1997-2026 Optimatika
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
package org.ojalgo.matrix.operation;

import static org.ojalgo.function.constant.PrimitiveMath.MACHINE_EPSILON;
import static org.ojalgo.function.constant.PrimitiveMath.ONE;
import static org.ojalgo.function.constant.PrimitiveMath.ZERO;

import org.ojalgo.array.operation.ArrayOperation;

/**
 * LAPACK {@code ?trevc}, with SIDE = 'R' and HOWMNY = 'B': the right eigenvectors of the upper
 * quasi-triangular real Schur form T of {@link HSEQR}, back-transformed with the Schur vectors Z:
 *
 * <pre>{@code
 * VR = Z * X
 * }</pre>
 *
 * The eigenvalues, wr and wi, are those returned by {@link HSEQR} with T. A real eigenvalue gives a real
 * eigenvector, column j of VR. A complex conjugate pair, columns j and j+1, gives VR(:,j) + i*VR(:,j+1) for
 * the eigenvalue with the positive imaginary part, and its conjugate for the other. The eigenvectors are not
 * normalised.
 * <p>
 * The eigenvectors X of T are found by back substitution, and overwrite T. There is only a pure Java
 * implementation.
 */
public abstract class TREVC implements LAPACK {

    /**
     * The pure Java implementation.
     * <p>
     * X is upper triangular, and Z*X is formed by {@link TRMM}.
     */
    static final class JVM {

        /**
         * Complex division, (xr + i*xi) / (yr + i*yi), to quotient[0] (real part) and quotient[1].
         */
        private static void divide(final double xr, final double xi, final double yr, final double yi, final double[] quotient) {
            if (Math.abs(yr) > Math.abs(yi)) {
                double r = yi / yr;
                double d = yr + r * yi;
                quotient[0] = (xr + r * xi) / d;
                quotient[1] = (xi - r * xr) / d;
            } else {
                double r = yr / yi;
                double d = yi + r * yr;
                quotient[0] = (r * xr + xi) / d;
                quotient[1] = (r * xi - xr) / d;
            }
        }

        /**
         * The pure Java implementation of
         * {@link TREVC#invoke(int, double[], int, int, double[], int, double[], int, double[], int, int)},
         * without argument checks, for n > 0.
         */
        static void invoke(final int n, final double[] t, final int offsetT, final int ldt, final double[] wr, final int offsetWr, final double[] wi,
                final int offsetWi, final double[] z, final int offsetZ, final int ldz) {

            // The norm (sum of absolute values) of T
            double norm = ZERO;
            for (int j = 0; j < n; j++) {
                for (int i = 0, limit = Math.min(j + 1, n - 1); i <= limit; i++) {
                    norm += Math.abs(t[offsetT + i + j * ldt]);
                }
            }

            if (norm == ZERO) {
                // T is 0, and X the identity
                return;
            }

            double[] quotient = new double[2];
            double r = ZERO, s = ZERO, u = ZERO;

            for (int k = n - 1; k >= 0; k--) {

                double p = wr[offsetWr + k];
                double q = wi[offsetWi + k];
                int columnK = offsetT + k * ldt;

                if (q == ZERO) {

                    // A real vector
                    int l = k;
                    t[columnK + k] = ONE;

                    for (int i = k - 1; i >= 0; i--) {

                        double w = t[offsetT + i + i * ldt] - p;
                        r = ZERO;
                        for (int j = l; j <= k; j++) {
                            r += t[offsetT + i + j * ldt] * t[columnK + j];
                        }

                        if (wi[offsetWi + i] < ZERO) {

                            u = w;
                            s = r;

                        } else {

                            l = i;

                            if (wi[offsetWi + i] == ZERO) {

                                t[columnK + i] = w != ZERO ? -r / w : -r / (MACHINE_EPSILON * norm);

                            } else {

                                // The real equations of a 2-by-2 block
                                double x = t[offsetT + i + (i + 1) * ldt];
                                double y = t[offsetT + i + 1 + i * ldt];
                                double d = wr[offsetWr + i] - p;
                                double modulus2 = d * d + wi[offsetWi + i] * wi[offsetWi + i];
                                double value = (x * s - u * r) / modulus2;
                                t[columnK + i] = value;
                                t[columnK + i + 1] = Math.abs(x) > Math.abs(u) ? (-r - w * value) / x : (-s - y * value) / u;
                            }

                            // Overflow control
                            double largest = Math.abs(t[columnK + i]);
                            if (MACHINE_EPSILON * largest * largest > 1) {
                                for (int j = i; j <= k; j++) {
                                    t[columnK + j] /= largest;
                                }
                            }
                        }
                    }

                } else if (q < ZERO) {

                    // A complex vector: the real part in column k-1, the imaginary part in column k
                    int l = k - 1;
                    int columnPrev = columnK - ldt;

                    // The last vector component imaginary, so that the matrix is triangular
                    if (Math.abs(t[columnPrev + k]) > Math.abs(t[columnK + k - 1])) {
                        t[columnPrev + k - 1] = q / t[columnPrev + k];
                        t[columnK + k - 1] = -(t[columnK + k] - p) / t[columnPrev + k];
                    } else {
                        JVM.divide(ZERO, -t[columnK + k - 1], t[columnPrev + k - 1] - p, q, quotient);
                        t[columnPrev + k - 1] = quotient[0];
                        t[columnK + k - 1] = quotient[1];
                    }
                    t[columnPrev + k] = ZERO;
                    t[columnK + k] = ONE;

                    for (int i = k - 2; i >= 0; i--) {

                        double ra = ZERO;
                        double sa = ZERO;
                        for (int j = l; j <= k; j++) {
                            ra += t[offsetT + i + j * ldt] * t[columnPrev + j];
                            sa += t[offsetT + i + j * ldt] * t[columnK + j];
                        }
                        double w = t[offsetT + i + i * ldt] - p;

                        if (wi[offsetWi + i] < ZERO) {

                            u = w;
                            r = ra;
                            s = sa;

                        } else {

                            l = i;

                            if (wi[offsetWi + i] == ZERO) {

                                JVM.divide(-ra, -sa, w, q, quotient);
                                t[columnPrev + i] = quotient[0];
                                t[columnK + i] = quotient[1];

                            } else {

                                // The complex equations of a 2-by-2 block
                                double x = t[offsetT + i + (i + 1) * ldt];
                                double y = t[offsetT + i + 1 + i * ldt];
                                double d = wr[offsetWr + i] - p;
                                double vr = d * d + wi[offsetWi + i] * wi[offsetWi + i] - q * q;
                                double vi = d * 2.0 * q;
                                if (vr == ZERO && vi == ZERO) {
                                    vr = MACHINE_EPSILON * norm * (Math.abs(w) + Math.abs(q) + Math.abs(x) + Math.abs(y) + Math.abs(u));
                                }
                                JVM.divide(x * r - u * ra + q * sa, x * s - u * sa - q * ra, vr, vi, quotient);
                                t[columnPrev + i] = quotient[0];
                                t[columnK + i] = quotient[1];
                                if (Math.abs(x) > Math.abs(u) + Math.abs(q)) {
                                    t[columnPrev + i + 1] = (-ra - w * t[columnPrev + i] + q * t[columnK + i]) / x;
                                    t[columnK + i + 1] = (-sa - w * t[columnK + i] - q * t[columnPrev + i]) / x;
                                } else {
                                    JVM.divide(-r - y * t[columnPrev + i], -s - y * t[columnK + i], u, q, quotient);
                                    t[columnPrev + i + 1] = quotient[0];
                                    t[columnK + i + 1] = quotient[1];
                                }
                            }

                            // Overflow control
                            double largest = Math.max(Math.abs(t[columnPrev + i]), Math.abs(t[columnK + i]));
                            if (MACHINE_EPSILON * largest * largest > 1) {
                                for (int j = i; j <= k; j++) {
                                    t[columnPrev + j] /= largest;
                                    t[columnK + j] /= largest;
                                }
                            }
                        }
                    }
                }
            }

            // VR = Z * X, X upper triangular
            TRMM.JVM.invoke(false, true, false, false, n, n, ONE, t, offsetT, ldt, z, offsetZ, ldz);
        }

    }

    /**
     * {@code dtrevc} with SIDE = 'R' and HOWMNY = 'B': VR overwrites Z, X overwrites T
     * <p>
     * T, as returned by {@link HSEQR} with the eigenvalues wr and wi, and Z are column-major, stored in their
     * arrays from an offset with a leading dimension. wr(i) and wi(i), i = 1, ..., n, are stored in
     * {@code wr[offsetWr + i - 1]} and {@code wi[offsetWi + i - 1]}.
     *
     * @param n The order of T
     */
    public static void invoke(final int n, final double[] t, final int offsetT, final int ldt, final double[] wr, final int offsetWr, final double[] wi,
            final int offsetWi, final double[] z, final int offsetZ, final int ldz) {

        ArrayOperation.checkArgument("TREVC", "n", n >= 0);
        ArrayOperation.checkArgument("TREVC", "ldt", ldt >= Math.max(1, n));
        ArrayOperation.checkArgument("TREVC", "ldz", ldz >= Math.max(1, n));

        if (n == 0) {
            return;
        }

        JVM.invoke(n, t, offsetT, ldt, wr, offsetWr, wi, offsetWi, z, offsetZ, ldz);
    }

}

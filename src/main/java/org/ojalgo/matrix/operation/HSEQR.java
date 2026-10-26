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
import static org.ojalgo.function.constant.PrimitiveMath.ZERO;

import org.ojalgo.array.operation.ArrayOperation;

/**
 * LAPACK {@code ?hseqr}, with ilo = 1 and ihi = n: the eigenvalues of an n-by-n upper Hessenberg matrix H,
 * and optionally its real Schur form T and the Schur vectors:
 *
 * <pre>{@code
 * H = Z * T * Z ^ T
 * }</pre>
 *
 * T is upper quasi-triangular: 1-by-1 blocks hold the real eigenvalues, and 2-by-2 blocks the complex
 * conjugate pairs. The 2-by-2 blocks of real pairs are reduced to upper triangular form. The elements of H
 * below the first subdiagonal are not referenced on entry, and are set to 0.
 * <p>
 * The eigenvalues are stored, in the order they appear on the diagonal of T, as wr (real parts) and wi
 * (imaginary parts). A complex conjugate pair is stored consecutively, the one with the positive imaginary
 * part first.
 * <p>
 * The implicit double-shift QR method, with exceptional shifts. There is only a pure Java implementation.
 * {@link TREVC} computes the eigenvectors from T and Z.
 */
public abstract class HSEQR implements LAPACK {

    /**
     * The pure Java implementation.
     */
    static final class JVM {

        /**
         * The pure Java implementation of
         * {@link HSEQR#invoke(boolean, boolean, int, double[], int, int, double[], int, double[], int, double[], int, int)},
         * without argument checks, for n > 0.
         */
        static int invoke(final boolean schur, final boolean vectors, final int n, final double[] h, final int offsetH, final int ldh, final double[] wr,
                final int offsetWr, final double[] wi, final int offsetWi, final double[] z, final int offsetZ, final int ldz) {

            for (int j = 0; j < n - 2; j++) {
                for (int i = j + 2; i < n; i++) {
                    h[offsetH + i + j * ldh] = ZERO;
                }
            }

            // The norm (sum of absolute values) of H
            double norm = ZERO;
            for (int j = 0; j < n; j++) {
                for (int i = 0, limit = Math.min(j + 1, n - 1); i <= limit; i++) {
                    norm += Math.abs(h[offsetH + i + j * ldh]);
                }
            }

            int iterationLimit = 30 * Math.max(10, n);
            int iterations = 0;

            double exshift = ZERO;
            double p = ZERO, q = ZERO, r = ZERO, s = ZERO, w, x, y, t = ZERO;

            // The active block is rows and columns l to last
            int iter = 0;
            int last = n - 1;
            while (last >= 0) {

                // A single small subdiagonal element
                int l = last;
                while (l > 0) {
                    s = Math.abs(h[offsetH + l - 1 + (l - 1) * ldh]) + Math.abs(h[offsetH + l + l * ldh]);
                    if (s == ZERO) {
                        s = norm;
                    }
                    if (Math.abs(h[offsetH + l + (l - 1) * ldh]) <= MACHINE_EPSILON * s) {
                        break;
                    }
                    l--;
                }
                if (l > 0) {
                    h[offsetH + l + (l - 1) * ldh] = ZERO;
                }

                // Rows and columns of H updated: all for the Schur form, the active block otherwise
                int firstRow = schur ? 0 : l;
                int columnLimit = schur ? n : last + 1;

                if (l == last) {

                    // One root
                    h[offsetH + last + last * ldh] += exshift;
                    wr[offsetWr + last] = h[offsetH + last + last * ldh];
                    wi[offsetWi + last] = ZERO;
                    last--;
                    iter = 0;

                } else if (l == last - 1) {

                    // Two roots
                    int prev = last - 1;
                    w = h[offsetH + last + prev * ldh] * h[offsetH + prev + last * ldh];
                    p = (h[offsetH + prev + prev * ldh] - h[offsetH + last + last * ldh]) / 2.0;
                    q = p * p + w;
                    t = Math.sqrt(Math.abs(q));
                    h[offsetH + last + last * ldh] += exshift;
                    h[offsetH + prev + prev * ldh] += exshift;
                    x = h[offsetH + last + last * ldh];

                    if (q >= 0) {

                        // A real pair
                        t = p >= 0 ? p + t : p - t;
                        wr[offsetWr + prev] = x + t;
                        wr[offsetWr + last] = t != ZERO ? x - w / t : x + t;
                        wi[offsetWi + prev] = ZERO;
                        wi[offsetWi + last] = ZERO;

                        if (schur || vectors) {

                            x = h[offsetH + last + prev * ldh];
                            s = Math.abs(x) + Math.abs(t);
                            p = x / s;
                            q = t / s;
                            r = Math.sqrt(p * p + q * q);
                            p /= r;
                            q /= r;

                            // Rows prev and last
                            for (int j = prev; j < columnLimit; j++) {
                                double hp = h[offsetH + prev + j * ldh];
                                h[offsetH + prev + j * ldh] = q * hp + p * h[offsetH + last + j * ldh];
                                h[offsetH + last + j * ldh] = q * h[offsetH + last + j * ldh] - p * hp;
                            }

                            // Columns prev and last
                            for (int i = firstRow; i <= last; i++) {
                                double hp = h[offsetH + i + prev * ldh];
                                h[offsetH + i + prev * ldh] = q * hp + p * h[offsetH + i + last * ldh];
                                h[offsetH + i + last * ldh] = q * h[offsetH + i + last * ldh] - p * hp;
                            }

                            // The rotation makes the block upper triangular
                            h[offsetH + last + prev * ldh] = ZERO;

                            if (vectors) {
                                for (int i = 0; i < n; i++) {
                                    double zp = z[offsetZ + i + prev * ldz];
                                    z[offsetZ + i + prev * ldz] = q * zp + p * z[offsetZ + i + last * ldz];
                                    z[offsetZ + i + last * ldz] = q * z[offsetZ + i + last * ldz] - p * zp;
                                }
                            }
                        }

                    } else {

                        // A complex pair
                        wr[offsetWr + prev] = x + p;
                        wr[offsetWr + last] = x + p;
                        wi[offsetWi + prev] = t;
                        wi[offsetWi + last] = -t;
                    }

                    last -= 2;
                    iter = 0;

                } else {

                    if (++iterations > iterationLimit) {
                        return last + 1;
                    }

                    // The shift
                    x = h[offsetH + last + last * ldh];
                    y = h[offsetH + last - 1 + (last - 1) * ldh];
                    w = h[offsetH + last + (last - 1) * ldh] * h[offsetH + last - 1 + last * ldh];

                    // Wilkinson's original ad hoc shift
                    if (iter > 0 && iter % 11 == 0) {
                        exshift += x;
                        for (int i = 0; i <= last; i++) {
                            h[offsetH + i + i * ldh] -= x;
                        }
                        s = Math.abs(h[offsetH + last + (last - 1) * ldh]) + Math.abs(h[offsetH + last - 1 + (last - 2) * ldh]);
                        x = y = 0.75 * s;
                        w = -0.4375 * s * s;
                    }

                    // MATLAB's ad hoc shift
                    if (iter > 0 && iter % 31 == 0) {
                        s = (y - x) / 2.0;
                        s = s * s + w;
                        if (s > 0) {
                            s = Math.sqrt(s);
                            if (y < x) {
                                s = -s;
                            }
                            s = x - w / ((y - x) / 2.0 + s);
                            for (int i = 0; i <= last; i++) {
                                h[offsetH + i + i * ldh] -= s;
                            }
                            exshift += s;
                            x = y = w = 0.964;
                        }
                    }

                    iter++;

                    // Two consecutive small subdiagonal elements
                    int m = last - 2;
                    while (m >= l) {
                        t = h[offsetH + m + m * ldh];
                        r = x - t;
                        s = y - t;
                        p = (r * s - w) / h[offsetH + m + 1 + m * ldh] + h[offsetH + m + (m + 1) * ldh];
                        q = h[offsetH + m + 1 + (m + 1) * ldh] - t - r - s;
                        r = h[offsetH + m + 2 + (m + 1) * ldh];
                        s = Math.abs(p) + Math.abs(q) + Math.abs(r);
                        p /= s;
                        q /= s;
                        r /= s;
                        if (m == l || Math.abs(h[offsetH + m + (m - 1) * ldh]) * (Math.abs(q) + Math.abs(r)) < MACHINE_EPSILON
                                * (Math.abs(p) * (Math.abs(h[offsetH + m - 1 + (m - 1) * ldh]) + Math.abs(t) + Math.abs(h[offsetH + m + 1 + (m + 1) * ldh])))) {
                            break;
                        }
                        m--;
                    }

                    for (int i = m + 2; i <= last; i++) {
                        h[offsetH + i + (i - 2) * ldh] = ZERO;
                        if (i > m + 2) {
                            h[offsetH + i + (i - 3) * ldh] = ZERO;
                        }
                    }

                    // The double QR step, rows l to last and columns m to last of the active block
                    for (int k = m; k <= last - 1; k++) {

                        boolean notlast = k != last - 1;

                        if (k != m) {
                            p = h[offsetH + k + (k - 1) * ldh];
                            q = h[offsetH + k + 1 + (k - 1) * ldh];
                            r = notlast ? h[offsetH + k + 2 + (k - 1) * ldh] : ZERO;
                            x = Math.abs(p) + Math.abs(q) + Math.abs(r);
                            if (x == ZERO) {
                                continue;
                            }
                            p /= x;
                            q /= x;
                            r /= x;
                        }

                        s = Math.sqrt(p * p + q * q + r * r);
                        if (p < 0) {
                            s = -s;
                        }

                        if (s != 0) {

                            if (k != m) {
                                // The reflector zeroes H(k+1,k-1) and H(k+2,k-1)
                                h[offsetH + k + (k - 1) * ldh] = -s * x;
                                h[offsetH + k + 1 + (k - 1) * ldh] = ZERO;
                                if (notlast) {
                                    h[offsetH + k + 2 + (k - 1) * ldh] = ZERO;
                                }
                            } else if (l != m) {
                                h[offsetH + k + (k - 1) * ldh] = -h[offsetH + k + (k - 1) * ldh];
                            }

                            p += s;
                            x = p / s;
                            y = q / s;
                            t = r / s;
                            q /= p;
                            r /= p;

                            // Rows k to k+2
                            for (int j = k; j < columnLimit; j++) {
                                int column = offsetH + j * ldh;
                                p = h[column + k] + q * h[column + k + 1];
                                if (notlast) {
                                    p += r * h[column + k + 2];
                                    h[column + k + 2] -= p * t;
                                }
                                h[column + k] -= p * x;
                                h[column + k + 1] -= p * y;
                            }

                            // Columns k to k+2
                            int columnK = offsetH + k * ldh;
                            for (int i = firstRow, limit = Math.min(last, k + 3); i <= limit; i++) {
                                p = x * h[columnK + i] + y * h[columnK + ldh + i];
                                if (notlast) {
                                    p += t * h[columnK + ldh + ldh + i];
                                    h[columnK + ldh + ldh + i] -= p * r;
                                }
                                h[columnK + i] -= p;
                                h[columnK + ldh + i] -= p * q;
                            }

                            if (vectors) {
                                int zK = offsetZ + k * ldz;
                                for (int i = 0; i < n; i++) {
                                    p = x * z[zK + i] + y * z[zK + ldz + i];
                                    if (notlast) {
                                        p += t * z[zK + ldz + ldz + i];
                                        z[zK + ldz + ldz + i] -= p * r;
                                    }
                                    z[zK + i] -= p;
                                    z[zK + ldz + i] -= p * q;
                                }
                            }
                        }
                    }
                }
            }

            return 0;
        }

    }

    /**
     * {@code dhseqr} with ILO = 1 and IHI = n: the eigenvalues of H, and the Schur form T and the Schur
     * vectors if specified
     * <p>
     * H is column-major, stored in its array from an offset with a leading dimension, and so is Z. wr(i) and
     * wi(i), i = 1, ..., n, are stored in {@code wr[offsetWr + i - 1]} and {@code wi[offsetWi + i - 1]}.
     *
     * @param schur   T overwrites H if true (JOB = 'S'), otherwise the contents of H are unspecified on exit
     *                (JOB = 'E')
     * @param vectors Z is updated if true (COMPZ = 'V'): on entry an orthogonal matrix Q (the identity for
     *                the Schur vectors of H itself), on exit Q*Z. Requires schur. Otherwise Z is not
     *                referenced.
     * @param n       The order of H
     * @return 0, or i > 0 if the QR method failed to converge: the eigenvalues i+1 to n have been found
     */
    public static int invoke(final boolean schur, final boolean vectors, final int n, final double[] h, final int offsetH, final int ldh, final double[] wr,
            final int offsetWr, final double[] wi, final int offsetWi, final double[] z, final int offsetZ, final int ldz) {

        ArrayOperation.checkArgument("HSEQR", "n", n >= 0);
        ArrayOperation.checkArgument("HSEQR", "ldh", ldh >= Math.max(1, n));
        ArrayOperation.checkArgument("HSEQR", "vectors", schur || !vectors);
        ArrayOperation.checkArgument("HSEQR", "ldz", !vectors || ldz >= Math.max(1, n));

        if (n == 0) {
            return 0;
        }

        return JVM.invoke(schur, vectors, n, h, offsetH, ldh, wr, offsetWr, wi, offsetWi, z, offsetZ, ldz);
    }

}

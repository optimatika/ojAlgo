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

import static org.ojalgo.function.constant.PrimitiveMath.*;

import org.ojalgo.array.operation.ArrayOperation;
import org.ojalgo.type.context.NumberContext;

/**
 * LAPACK {@code ?bdsqr}: the singular values, and the plane rotations that make the singular vectors, of an
 * upper bidiagonal n-by-n matrix B, by the implicit QR method with shifts:
 *
 * <pre>{@code
 * B = G * diag(s) * H ^ T
 * }</pre>
 *
 * G and H are products of plane rotations, each reported to a {@link RotateColumns} as it is made. Applied
 * from the right to U and V, they make U * G and V * H; when B = Q^T * A * P, starting with U = Q and V = P
 * gives the singular vectors of A. The singular values are made non-negative, negating the corresponding
 * columns of V, and sorted in descending order, exchanging the corresponding columns of U and V. There is
 * only a pure Java implementation.
 */
public abstract class BDSQR implements LAPACK {

    /**
     * The pure Java implementation.
     */
    static final class JVM {

        /**
         * ≈ 1.6E-291
         */
        private static final double TINY = Math.pow(2.0, -966.0);

        private static void doCase1(final double[] s, final double[] e, final int p, final int k, final RotateColumns q2RotR) {

            double f = e[p - 2];
            e[p - 2] = ZERO;

            double tmp, cos, sin;

            for (int j = p - 2; j > k; j--) {

                tmp = HYPOT.invoke(s[j], f);
                cos = s[j] / tmp;
                sin = f / tmp;
                s[j] = tmp;

                q2RotR.rotateColumns(p - 1, j, cos, sin);

                tmp = e[j - 1];
                f = -sin * tmp;
                e[j - 1] = cos * tmp;
            }

            tmp = HYPOT.invoke(s[k], f);
            cos = s[k] / tmp;
            sin = f / tmp;
            s[k] = tmp;

            q2RotR.rotateColumns(p - 1, k, cos, sin);
        }

        private static void doCase2(final double[] s, final double[] e, final int p, final int k, final RotateColumns mtrxQ1) {

            double f = e[k - 1];
            e[k - 1] = ZERO;

            double tmp, cos, sin;

            for (int j = k; j < p; j++) {

                tmp = HYPOT.invoke(s[j], f);
                cos = s[j] / tmp;
                sin = f / tmp;
                s[j] = tmp;

                mtrxQ1.rotateColumns(k - 1, j, cos, sin);

                tmp = e[j];
                f = -sin * tmp;
                e[j] = cos * tmp;
            }
        }

        private static void doCase3(final double[] s, final double[] e, final int p, final int k, final RotateColumns q1RotR, final RotateColumns q2RotR) {

            // Calculate the shift.
            final double scale = MAX.invoke(
                    MAX.invoke(MAX.invoke(MAX.invoke(ABS.invoke(s[p - 1]), ABS.invoke(s[p - 2])), ABS.invoke(e[p - 2])), ABS.invoke(s[k])), ABS.invoke(e[k]));

            final double s_p1 = s[p - 1] / scale;
            final double s_p2 = s[p - 2] / scale;
            final double e_p2 = e[p - 2] / scale;

            final double s_k = s[k] / scale;
            final double e_k = e[k] / scale;

            final double b = ((s_p2 + s_p1) * (s_p2 - s_p1) + e_p2 * e_p2) / TWO;
            final double c = s_p1 * e_p2 * (s_p1 * e_p2);

            double shift = ZERO;
            if (NumberContext.compare(b, ZERO) != 0 || NumberContext.compare(c, ZERO) != 0) {
                shift = SQRT.invoke(b * b + c);
                if (b < ZERO) {
                    shift = -shift;
                }
                shift = c / (b + shift);
            }

            double f = (s_k + s_p1) * (s_k - s_p1) + shift;
            double g = s_k * e_k;

            double tmp, cos, sin;

            // Chase zeros.
            for (int j = k; j < p - 1; j++) {

                tmp = HYPOT.invoke(f, g);
                cos = f / tmp;
                sin = g / tmp;

                if (j != k) {
                    e[j - 1] = tmp;
                }

                f = cos * s[j] + sin * e[j];
                e[j] = cos * e[j] - sin * s[j];
                g = sin * s[j + 1];
                s[j + 1] = cos * s[j + 1];

                q2RotR.rotateColumns(j + 1, j, cos, sin);

                tmp = HYPOT.invoke(f, g);
                cos = f / tmp;
                sin = g / tmp;
                s[j] = tmp;

                f = cos * e[j] + sin * s[j + 1];
                s[j + 1] = -sin * e[j] + cos * s[j + 1];
                g = sin * e[j + 1];
                e[j + 1] = cos * e[j + 1];

                q1RotR.rotateColumns(j + 1, j, cos, sin);
            }

            e[p - 2] = f;
        }

        private static void doCase4(final double[] s, final int k, final NegateColumn q2NegCol, final ExchangeColumns q1XchgCols,
                final ExchangeColumns q2XchgCols) {

            // Make the singular values positive.
            if (s[k] <= ZERO) {
                s[k] = s[k] < ZERO ? -s[k] : ZERO;
                q2NegCol.negateColumn(k);
            }

            final int size = s.length;
            double tmp;

            // Order the singular values.
            for (int iter = k, next = iter + 1; next < size && s[iter] < s[next]; iter++, next++) {

                tmp = s[iter];
                s[iter] = s[next];
                s[next] = tmp;

                q1XchgCols.exchangeColumns(iter, next);
                q2XchgCols.exchangeColumns(iter, next);
            }
        }

        /**
         * The pure Java implementation of
         * {@link BDSQR#invoke(double[], double[], RotateColumns, RotateColumns, ExchangeColumns, ExchangeColumns, NegateColumn)},
         * without argument checks.
         */
        static void invoke(final double[] s, final double[] e, final RotateColumns q1RotR, final RotateColumns q2RotR, final ExchangeColumns q1XchgCols,
                final ExchangeColumns q2XchgCols, final NegateColumn q2NegCol) {

            int p = s.length;
            while (p > 0) {
                int k, kase;

                // This section of the program inspects for negligible elements in the s and e arrays.
                // On completion the variables kase and k are set as follows:
                //
                // kase = 1 if s[p] and e[k-1] are negligible and k<p => deflate negligible s[p]
                // kase = 2 if s[k] is negligible and k<p => split at negligible s[k]
                // kase = 3 if e[k-1] is negligible, k<p, and s(k)...s(p) are not negligible => perform QR-step
                // kase = 4 if e[p-1] is negligible => convergence

                for (k = p - 2; k >= 0; k--) {
                    if (ABS.invoke(e[k]) <= TINY + MACHINE_EPSILON * (ABS.invoke(s[k]) + ABS.invoke(s[k + 1]))) {
                        e[k] = ZERO;
                        break;
                    }
                }
                if (k == p - 2) {
                    kase = 4;
                } else {
                    int ks;
                    for (ks = p - 1; ks > k; ks--) {
                        final double t = ABS.invoke(e[ks]) + (ks != k + 1 ? ABS.invoke(e[ks - 1]) : ZERO);
                        if (ABS.invoke(s[ks]) <= TINY + MACHINE_EPSILON * t) {
                            s[ks] = ZERO;
                            break;
                        }
                    }
                    if (ks == k) {
                        kase = 3;
                    } else if (ks == p - 1) {
                        kase = 1;
                    } else {
                        kase = 2;
                        k = ks;
                    }
                }
                k++;

                switch (kase) { // Perform the task indicated by kase.

                    // s[p] and e[k-1] are negligible and k<p
                    case 1: // Deflate negligible s[p]

                        JVM.doCase1(s, e, p, k, q2RotR);
                        break;

                    // s[k] is negligible and k<p
                    case 2: // Split at negligible s[k]

                        JVM.doCase2(s, e, p, k, q1RotR);
                        break;

                    // e[k-1] is negligible, k<p, and s(k)...s(p) are not negligible
                    case 3: // Perform QR-step.

                        JVM.doCase3(s, e, p, k, q1RotR, q2RotR);
                        break;

                    // e[p-1] is negligible
                    case 4: // Convergence

                        JVM.doCase4(s, k, q2NegCol, q1XchgCols, q2XchgCols);
                        p--;
                        break;

                    // Should never happen
                    default:

                        throw new IllegalStateException();

                } // switch
            } // while
        }

    }

    /**
     * The singular values of B overwrite d, and e is overwritten. Each plane rotation, combining the columns
     * low and high of U or V, is reported to the {@link RotateColumns} of that matrix:
     *
     * <pre>{@code
     * z(low) := cos * z(low) - sin * z(high)
     * z(high) := sin * z(low) + cos * z(high)
     * }</pre>
     *
     * The singular vectors for d[j] are column j of U and of V.
     *
     * @param d          The diagonal of B, length n
     * @param e          The superdiagonal of B: e[i] = B(i,i+1) for i = 0, ..., n-2, and e[n-1] = 0
     * @param rotationsU Receives the plane rotations of U, {@link RotateColumns#NULL} if not needed
     * @param rotationsV Receives the plane rotations of V, {@link RotateColumns#NULL} if not needed
     * @param exchangeU  Exchanges two columns of U, {@link ExchangeColumns#NULL} if not needed
     * @param exchangeV  Exchanges two columns of V, {@link ExchangeColumns#NULL} if not needed
     * @param negateV    Negates a column of V, {@link NegateColumn#NULL} if not needed
     */
    public static void invoke(final double[] d, final double[] e, final RotateColumns rotationsU, final RotateColumns rotationsV,
            final ExchangeColumns exchangeU, final ExchangeColumns exchangeV, final NegateColumn negateV) {

        ArrayOperation.checkArgument("BDSQR", "e", e.length >= d.length);

        JVM.invoke(d, e, rotationsU, rotationsV, exchangeU, exchangeV, negateV);
    }

}

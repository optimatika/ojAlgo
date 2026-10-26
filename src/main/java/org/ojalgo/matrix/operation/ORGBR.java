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

import static org.ojalgo.function.constant.PrimitiveMath.ONE;
import static org.ojalgo.function.constant.PrimitiveMath.ZERO;

import java.util.Arrays;

import org.ojalgo.array.operation.ArrayOperation;
import org.ojalgo.scalar.Scalar;

/**
 * LAPACK {@code ?orgbr}, for the reflectors of {@link GEBRD} (m >= n): generates one of the orthogonal
 * matrices Q or P<sup>T</sup> of the bidiagonal reduction Q<sup>T</sup> * A * P = B of a matrix A with at
 * least as many rows as columns.
 * <ul>
 * <li>Q of an m-by-k matrix A, m >= k: Q = H(1) * H(2) * ... * H(k), and the first n columns of Q, m >= n >=
 * k, are generated – the Q of {@link ORGQR}.
 * <li>P<sup>T</sup> of a k-by-n matrix A, k >= n: P<sup>T</sup> = G(n-1) * ... * G(2) * G(1), n-by-n. Its
 * first row and column are those of the identity.
 * </ul>
 * Generating Q of a matrix with m < k, or P<sup>T</sup> of a matrix with k < n, the other half of LAPACK's
 * {@code ?orgbr}, is not implemented.
 * <p>
 * For other number types Q and P are unitary, and P<sup>H</sup> is generated in place of P<sup>T</sup>. There
 * is only a pure Java implementation for those.
 */
public abstract class ORGBR implements LAPACK {

    /**
     * The pure Java implementation.
     * <p>
     * Q is formed by {@link ORGQR}. P (not P<sup>T</sup>) is formed by {@link ORGQR} as well, from the
     * reflectors transposed to the columns of a separate matrix, moved one row down and one column to the
     * right; P<sup>T</sup> is then copied back to A.
     */
    static final class JVM {

        /**
         * P = G(1) * G(2) * ... * G(n-1), not P<sup>T</sup>, from the reflectors above the first
         * superdiagonal of the n-by-n matrix A, as returned by {@link GEBRD}, to the n-by-n matrix P
         * (column-major from an offset with a leading dimension). A is not changed. Requires n > 0.
         */
        static void generateP(final int n, final double[] a, final int offsetA, final int lda, final double[] tau, final int offsetTau, final double[] p,
                final int offsetP, final int ldp) {

            for (int j = 0; j < n; j++) {
                int column = offsetP + j * ldp;
                Arrays.fill(p, column, column + n, ZERO);
            }

            p[offsetP] = ONE;

            // Column j of P from row j+1 down: u(j+1:n-1) of G(j-1), stored in A(j-1,j+1:n-1)
            for (int j = 1; j < n; j++) {
                int column = offsetP + j * ldp;
                for (int i = j + 1; i < n; i++) {
                    p[column + i] = a[offsetA + j - 1 + i * lda];
                }
            }

            if (n > 1) {
                ORGQR.JVM.invoke(n - 1, n - 1, n - 1, p, offsetP + 1 + ldp, ldp, tau, offsetTau);
            }
        }

        /**
         * As {@link #generateP(int, double[], int, int, double[], int, double[], int, int)}, for other number
         * types.
         */
        static <N extends Scalar<N>> void generateP(final int n, final N[] a, final int offsetA, final int lda, final N[] tau, final int offsetTau, final N[] p,
                final int offsetP, final int ldp, final Scalar.Factory<N> scalar) {

            N zero = scalar.zero().get();

            for (int j = 0; j < n; j++) {
                int column = offsetP + j * ldp;
                Arrays.fill(p, column, column + n, zero);
            }

            p[offsetP] = scalar.one().get();

            for (int j = 1; j < n; j++) {
                int column = offsetP + j * ldp;
                for (int i = j + 1; i < n; i++) {
                    p[column + i] = a[offsetA + j - 1 + i * lda];
                }
            }

            if (n > 1) {
                ORGQR.JVM.invoke(n - 1, n - 1, n - 1, p, offsetP + 1 + ldp, ldp, tau, offsetTau, scalar);
            }
        }

        /**
         * The pure Java implementation of
         * {@link ORGBR#invoke(boolean, int, int, int, double[], int, int, double[], int)}, without argument
         * checks, for m, n > 0.
         */
        static void invoke(final boolean q, final int m, final int n, final int k, final double[] a, final int offsetA, final int lda, final double[] tau,
                final int offsetTau) {

            if (q) {

                ORGQR.JVM.invoke(m, n, k, a, offsetA, lda, tau, offsetTau);

            } else {

                double[] p = new double[n * n];

                JVM.generateP(n, a, offsetA, lda, tau, offsetTau, p, 0, n);

                for (int j = 0; j < n; j++) {
                    int column = offsetA + j * lda;
                    for (int i = 0; i < n; i++) {
                        a[column + i] = p[j + i * n];
                    }
                }
            }
        }

        /**
         * The pure Java implementation of
         * {@link ORGBR#invoke(boolean, int, int, int, Scalar[], int, int, Scalar[], int, Scalar.Factory)},
         * without argument checks, for m, n > 0.
         */
        static <N extends Scalar<N>> void invoke(final boolean q, final int m, final int n, final int k, final N[] a, final int offsetA, final int lda,
                final N[] tau, final int offsetTau, final Scalar.Factory<N> scalar) {

            if (q) {

                ORGQR.JVM.invoke(m, n, k, a, offsetA, lda, tau, offsetTau, scalar);

            } else {

                N[] p = scalar.newArrayInstance(n * n);

                JVM.generateP(n, a, offsetA, lda, tau, offsetTau, p, 0, n, scalar);

                for (int j = 0; j < n; j++) {
                    int column = offsetA + j * lda;
                    for (int i = 0; i < n; i++) {
                        a[column + i] = p[j + i * n].conjugate().get();
                    }
                }
            }
        }

    }

    /**
     * {@code dorgbr}: Q (q = true) or P<sup>T</sup> (q = false) overwrites A
     * <p>
     * A is column-major, stored in its array from an offset with a leading dimension. On entry, it holds the
     * reflectors as returned by {@link GEBRD}: below the diagonal for Q, above the first superdiagonal for
     * P<sup>T</sup>; the rest of A need not be set. tau(i) is stored in {@code tau[offsetTau + i - 1]}, i =
     * 1, ..., k for Q, and i = 1, ..., n for P<sup>T</sup>.
     *
     * @param q True for Q, false for P<sup>T</sup>
     * @param m The number of rows of Q (m >= n >= k), or of P<sup>T</sup> (m = n)
     * @param n The number of columns of Q or P<sup>T</sup>
     * @param k The number of columns (for Q) or rows (for P<sup>T</sup>, k >= n) of the matrix reduced by
     *          {@link GEBRD}
     */
    public static void invoke(final boolean q, final int m, final int n, final int k, final double[] a, final int offsetA, final int lda, final double[] tau,
            final int offsetTau) {

        ArrayOperation.checkArgument("ORGBR", "n", n >= 0);
        ArrayOperation.checkArgument("ORGBR", "m", q ? m >= n : m == n);
        ArrayOperation.checkArgument("ORGBR", "k", q ? k >= 0 && k <= n : k >= n);
        ArrayOperation.checkArgument("ORGBR", "lda", lda >= Math.max(1, m));

        if (m == 0 || n == 0) {
            return;
        }

        JVM.invoke(q, m, n, k, a, offsetA, lda, tau, offsetTau);
    }

    /**
     * As {@link #invoke(boolean, int, int, int, double[], int, int, double[], int)}, for other number types:
     * Q or P<sup>H</sup> (not P<sup>T</sup>), unitary, for the reflectors as returned by {@link GEBRD} for
     * those types. There is only a pure Java implementation.
     */
    public static <N extends Scalar<N>> void invoke(final boolean q, final int m, final int n, final int k, final N[] a, final int offsetA, final int lda,
            final N[] tau, final int offsetTau, final Scalar.Factory<N> scalar) {

        ArrayOperation.checkArgument("ORGBR", "n", n >= 0);
        ArrayOperation.checkArgument("ORGBR", "m", q ? m >= n : m == n);
        ArrayOperation.checkArgument("ORGBR", "k", q ? k >= 0 && k <= n : k >= n);
        ArrayOperation.checkArgument("ORGBR", "lda", lda >= Math.max(1, m));

        if (m == 0 || n == 0) {
            return;
        }

        JVM.invoke(q, m, n, k, a, offsetA, lda, tau, offsetTau, scalar);
    }

}

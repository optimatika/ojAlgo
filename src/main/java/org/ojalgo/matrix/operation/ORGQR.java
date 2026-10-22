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
import org.ojalgo.array.operation.SCAL;
import org.ojalgo.scalar.Scalar;

/**
 * LAPACK {@code ?orgqr}: generates the m-by-n matrix Q with orthonormal columns, the first n columns of the
 * product of k elementary reflectors returned by {@link GEQRF}:
 *
 * <pre>{@code
 * Q = H(1) * H(2) * ... * H(k)
 * }</pre>
 *
 * m >= n >= k >= 0. With n = min(m,n) of the factorisation that is the Q of the economy size factorisation,
 * and with n = m the full m-by-m orthogonal matrix.
 */
public abstract class ORGQR implements LAPACK {

    /**
     * The pure Java implementation.
     * <p>
     * Unblocked: columns k to n-1 are set to those of the identity, then the reflectors are applied in
     * reverse order, H(i) to columns i+1 to n-1 (split over several threads when that is large, see
     * {@link GEQRF#THRESHOLD}), and column i is formed as H(i) times column i of the identity.
     */
    static final class JVM {

        /**
         * The pure Java implementation of
         * {@link ORGQR#invoke(int, int, int, double[], int, int, double[], int)}, without argument checks,
         * for m >= n > 0 and n >= k >= 0.
         */
        static void invoke(final int m, final int n, final int k, final double[] a, final int offsetA, final int lda, final double[] tau, final int offsetTau) {

            for (int j = k; j < n; j++) {
                int column = offsetA + j * lda;
                Arrays.fill(a, column, column + m, ZERO);
                a[column + j] = ONE;
            }

            for (int i = k - 1; i >= 0; i--) {

                int column = offsetA + i * lda;
                double beta = tau[offsetTau + i];

                if (beta != ZERO) {

                    a[column + i] = ONE;
                    if (i + 1 < n) {
                        GEQRF.JVM.reflect(m, a, column + i, beta, a, offsetA, lda, i, i + 1, n);
                    }
                    if (i + 1 < m) {
                        SCAL.JVM.invoke(m - i - 1, -beta, a, column + i + 1, 1);
                    }
                    a[column + i] = ONE - beta;

                } else {

                    Arrays.fill(a, column + i + 1, column + m, ZERO);
                    a[column + i] = ONE;
                }

                Arrays.fill(a, column, column + i, ZERO);
            }
        }

        /**
         * The pure Java implementation of
         * {@link ORGQR#invoke(int, int, int, Scalar[], int, int, Scalar[], int, Scalar.Factory)}, without
         * argument checks, for m >= n > 0 and n >= k >= 0.
         */
        static <N extends Scalar<N>> void invoke(final int m, final int n, final int k, final N[] a, final int offsetA, final int lda, final N[] tau,
                final int offsetTau, final Scalar.Factory<N> scalar) {

            N zero = scalar.zero().get();
            N one = scalar.one().get();

            for (int j = k; j < n; j++) {
                int column = offsetA + j * lda;
                Arrays.fill(a, column, column + m, zero);
                a[column + j] = one;
            }

            for (int i = k - 1; i >= 0; i--) {

                int column = offsetA + i * lda;
                N beta = tau[offsetTau + i];

                if (beta.norm() != ZERO) {

                    a[column + i] = one;
                    if (i + 1 < n) {
                        GEQRF.JVM.reflect(m, a, column + i, beta, a, offsetA, lda, i, i + 1, n, scalar);
                    }
                    Scalar<N> negated = beta.negate();
                    for (int p = i + 1; p < m; p++) {
                        a[column + p] = negated.multiply(a[column + p]).get();
                    }
                    a[column + i] = scalar.one().subtract(beta).get();

                } else {

                    Arrays.fill(a, column + i + 1, column + m, zero);
                    a[column + i] = one;
                }

                Arrays.fill(a, column, column + i, zero);
            }
        }

    }

    /**
     * {@code dorgqr}: Q overwrites A
     * <p>
     * A is column-major, stored in its array from an offset with a leading dimension. On entry, its first k
     * columns hold the reflectors as returned by {@link GEQRF}, below the diagonal; the rest of A need not be
     * set. tau(i), i = 1, ..., k, is stored in {@code tau[offsetTau + i - 1]}.
     *
     * @param m The number of rows of A and Q
     * @param n The number of columns of A and Q, at most m
     * @param k The number of reflectors, at most n
     */
    public static void invoke(final int m, final int n, final int k, final double[] a, final int offsetA, final int lda, final double[] tau,
            final int offsetTau) {

        ArrayOperation.checkArgument("ORGQR", "m", m >= 0);
        ArrayOperation.checkArgument("ORGQR", "n", n >= 0 && n <= m);
        ArrayOperation.checkArgument("ORGQR", "k", k >= 0 && k <= n);
        ArrayOperation.checkArgument("ORGQR", "lda", lda >= Math.max(1, m));

        if (n == 0) {
            return;
        }

        JVM.invoke(m, n, k, a, offsetA, lda, tau, offsetTau);
    }

    /**
     * As {@link #invoke(int, int, int, double[], int, int, double[], int)}, for other number types: Q is
     * unitary, the product of the reflectors H(i) = I - tau(i) * v * v<sup>H</sup>. There is only a pure Java
     * implementation.
     */
    public static <N extends Scalar<N>> void invoke(final int m, final int n, final int k, final N[] a, final int offsetA, final int lda, final N[] tau,
            final int offsetTau, final Scalar.Factory<N> scalar) {

        ArrayOperation.checkArgument("ORGQR", "m", m >= 0);
        ArrayOperation.checkArgument("ORGQR", "n", n >= 0 && n <= m);
        ArrayOperation.checkArgument("ORGQR", "k", k >= 0 && k <= n);
        ArrayOperation.checkArgument("ORGQR", "lda", lda >= Math.max(1, m));

        if (n == 0) {
            return;
        }

        JVM.invoke(m, n, k, a, offsetA, lda, tau, offsetTau, scalar);
    }

}

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

import static org.ojalgo.function.constant.PrimitiveMath.ZERO;

import org.ojalgo.array.operation.AXPY;
import org.ojalgo.array.operation.ArrayOperation;
import org.ojalgo.array.operation.DOT;
import org.ojalgo.concurrent.DivideAndConquer;
import org.ojalgo.concurrent.Parallelism;
import org.ojalgo.scalar.Scalar;

/**
 * LAPACK {@code ?ormqr}, from the left: overwrites the m-by-n matrix C with
 *
 * <pre>{@code
 * Q * C
 * Q^T * C
 * }</pre>
 *
 * where Q is the m-by-m orthogonal matrix of k elementary reflectors returned by {@link GEQRF}:
 *
 * <pre>{@code
 * Q = H(1) * H(2) * ... * H(k)
 * }</pre>
 *
 * m >= k >= 0. Multiplying from the right, the other half of LAPACK's {@code ?ormqr}, is not implemented.
 * There is only a pure Java implementation.
 */
public abstract class ORMQR implements LAPACK {

    /**
     * The pure Java implementation. Everything it calls is pure Java as well – the {@code JVM}
     * implementations of other operations, never their dispatching {@code invoke} methods (that may call
     * native code).
     * <p>
     * The reflectors are applied one at a time, in reverse order for Q*C. The columns of C are split into
     * parts, done in parallel, when that is large, see {@link ORMQR#THRESHOLD}.
     */
    static final class JVM {

        private static final DivideAndConquer.Divider DIVIDER = Parallelism.CORES.newDivider();

        /**
         * Applies the reflectors, one at a time, to the columns first to limit (exclusive) of C. v(i) = 1 is
         * not read from A.
         */
        private static void multiply(final boolean transpose, final int m, final int k, final double[] a, final int offsetA, final int lda, final double[] tau,
                final int offsetTau, final double[] c, final int offsetC, final int ldc, final int first, final int limit) {
            for (int r = 0; r < k; r++) {

                int i = transpose ? r : k - 1 - r;
                double beta = tau[offsetTau + i];

                if (beta != ZERO) {

                    int count = m - i - 1;
                    int vector = offsetA + i + 1 + i * lda;

                    for (int j = first; j < limit; j++) {
                        int element = offsetC + i + j * ldc;
                        double scale = beta * (c[element] + DOT.JVM.invoke(count, a, vector, 1, c, element + 1, 1));
                        c[element] -= scale;
                        if (count > 0) {
                            AXPY.JVM.invoke(count, -scale, a, vector, 1, c, element + 1, 1);
                        }
                    }
                }
            }
        }

        /**
         * As
         * {@link #multiply(boolean, int, int, double[], int, int, double[], int, double[], int, int, int, int)},
         * for other number types: H(i) = I - tau(i) * v * v<sup>H</sup>, and H(i)<sup>H</sup> = I -
         * conj(tau(i)) * v * v<sup>H</sup>.
         */
        private static <N extends Scalar<N>> void multiply(final boolean transpose, final int m, final int k, final N[] a, final int offsetA, final int lda,
                final N[] tau, final int offsetTau, final N[] c, final int offsetC, final int ldc, final int first, final int limit) {
            for (int r = 0; r < k; r++) {

                int i = transpose ? r : k - 1 - r;
                N beta = tau[offsetTau + i];

                if (!beta.isZero()) {

                    if (transpose) {
                        beta = beta.conjugate().get();
                    }

                    int vector = offsetA + i + i * lda;

                    for (int j = first; j < limit; j++) {
                        int element = offsetC + i + j * ldc;
                        Scalar<N> scale = c[element];
                        for (int p = 1; p < m - i; p++) {
                            scale = scale.add(a[vector + p].conjugate().multiply(c[element + p]));
                        }
                        scale = scale.multiply(beta);
                        c[element] = c[element].subtract(scale).get();
                        for (int p = 1; p < m - i; p++) {
                            c[element + p] = c[element + p].subtract(a[vector + p].multiply(scale)).get();
                        }
                    }
                }
            }
        }

        /**
         * The pure Java implementation of
         * {@link ORMQR#invoke(boolean, int, int, int, double[], int, int, double[], int, double[], int, int)},
         * without argument checks, for m, n > 0 and m >= k >= 0.
         */
        static void invoke(final boolean transpose, final int m, final int n, final int k, final double[] a, final int offsetA, final int lda,
                final double[] tau, final int offsetTau, final double[] c, final int offsetC, final int ldc) {

            // A dot product and an update for each reflector: k*(2m-k) multiply-adds per column
            int chunk = MatrixOperation.chunk(THRESHOLD, k * (2.0 * m - k));

            if (n > chunk) {

                DIVIDER.threshold(chunk).divide(0, n,
                        (first, limit) -> JVM.multiply(transpose, m, k, a, offsetA, lda, tau, offsetTau, c, offsetC, ldc, first, limit));

            } else {

                JVM.multiply(transpose, m, k, a, offsetA, lda, tau, offsetTau, c, offsetC, ldc, 0, n);
            }
        }

        /**
         * The pure Java implementation of
         * {@link ORMQR#invoke(boolean, int, int, int, Scalar[], int, int, Scalar[], int, Scalar[], int, int)},
         * without argument checks, for m, n > 0 and m >= k >= 0.
         */
        static <N extends Scalar<N>> void invoke(final boolean transpose, final int m, final int n, final int k, final N[] a, final int offsetA, final int lda,
                final N[] tau, final int offsetTau, final N[] c, final int offsetC, final int ldc) {

            int chunk = MatrixOperation.chunk(THRESHOLD, k * (2.0 * m - k));

            if (n > chunk) {

                DIVIDER.threshold(chunk).divide(0, n,
                        (first, limit) -> JVM.multiply(transpose, m, k, a, offsetA, lda, tau, offsetTau, c, offsetC, ldc, first, limit));

            } else {

                JVM.multiply(transpose, m, k, a, offsetA, lda, tau, offsetTau, c, offsetC, ldc, 0, n);
            }
        }

    }

    /**
     * The Java implementation does the columns of C in parallel when the number of multiply-adds is larger
     * than this cubed. Each part then gets more than half of that.
     */
    static int THRESHOLD = 40;

    /**
     * {@code dormqr} with SIDE = 'L': C := Q*C or C := Q<sup>T</sup>*C
     * <p>
     * A and C are column-major, stored in their arrays from an offset with a leading dimension. The first k
     * columns of A hold the reflectors as returned by {@link GEQRF}, below the diagonal; the diagonal and
     * above are not referenced. tau(i), i = 1, ..., k, is stored in {@code tau[offsetTau + i - 1]}.
     *
     * @param transpose C := Q<sup>T</sup>*C if true, otherwise C := Q*C
     * @param m         The number of rows of C, and of A
     * @param n         The number of columns of C
     * @param k         The number of reflectors, at most m
     */
    public static void invoke(final boolean transpose, final int m, final int n, final int k, final double[] a, final int offsetA, final int lda,
            final double[] tau, final int offsetTau, final double[] c, final int offsetC, final int ldc) {

        ArrayOperation.checkArgument("ORMQR", "m", m >= 0);
        ArrayOperation.checkArgument("ORMQR", "n", n >= 0);
        ArrayOperation.checkArgument("ORMQR", "k", k >= 0 && k <= m);
        ArrayOperation.checkArgument("ORMQR", "lda", lda >= Math.max(1, m));
        ArrayOperation.checkArgument("ORMQR", "ldc", ldc >= Math.max(1, m));

        if (m == 0 || n == 0 || k == 0) {
            return;
        }

        JVM.invoke(transpose, m, n, k, a, offsetA, lda, tau, offsetTau, c, offsetC, ldc);
    }

    /**
     * As {@link #invoke(boolean, int, int, int, double[], int, int, double[], int, double[], int, int)}, for
     * other number types: Q is unitary, with H(i) = I - tau(i) * v * v<sup>H</sup> applied from the left, and
     * tau(i) = 0 where there is no reflector.
     *
     * @param transpose C := Q<sup>H</sup>*C, with the conjugate transpose, if true, otherwise C := Q*C
     */
    public static <N extends Scalar<N>> void invoke(final boolean transpose, final int m, final int n, final int k, final N[] a, final int offsetA,
            final int lda, final N[] tau, final int offsetTau, final N[] c, final int offsetC, final int ldc) {

        ArrayOperation.checkArgument("ORMQR", "m", m >= 0);
        ArrayOperation.checkArgument("ORMQR", "n", n >= 0);
        ArrayOperation.checkArgument("ORMQR", "k", k >= 0 && k <= m);
        ArrayOperation.checkArgument("ORMQR", "lda", lda >= Math.max(1, m));
        ArrayOperation.checkArgument("ORMQR", "ldc", ldc >= Math.max(1, m));

        if (m == 0 || n == 0 || k == 0) {
            return;
        }

        JVM.invoke(transpose, m, n, k, a, offsetA, lda, tau, offsetTau, c, offsetC, ldc);
    }

}

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
import org.ojalgo.concurrent.DivideAndConquer;
import org.ojalgo.concurrent.Parallelism;
import org.ojalgo.function.FunctionSet;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.scalar.Scalar;

/**
 * LAPACK {@code ?potrf}: Cholesky factorisation of a symmetric positive definite n-by-n matrix A:
 *
 * <pre>{@code
 * A = L * L^T  or  A = U^T * U
 * }</pre>
 *
 * where L is lower triangular and U upper triangular. Only the lower (or upper) triangle of A is read, and
 * the factor overwrites it. The other triangle is not referenced.
 */
public abstract class POTRF implements LAPACK {

    /**
     * The pure Java implementation.
     * <p>
     * Unblocked and right-looking: for each column (of L, or row of U), the square root of the diagonal
     * element is taken, the elements below it (to the right of it) are divided by that, and the trailing
     * triangle is updated (a symmetric rank 1 update), split over several threads when that is large, see
     * {@link POTRF#THRESHOLD}.
     */
    static final class JVM {

        private static final DivideAndConquer.Divider DIVIDER = Parallelism.CORES.newDivider();

        /**
         * Updates the trailing triangle with the multipliers of column (or row) j, split over several threads
         * when that is large, see {@link POTRF#THRESHOLD}.
         */
        private static void update(final boolean upper, final int n, final double[] a, final int offsetA, final int lda, final double[] multipliers,
                final int j) {
            // The columns to update get shorter, half as long as the first on average
            int chunk = MatrixOperation.chunk(THRESHOLD, (n - j - 1) / 2.0);

            if (n - j - 1 > chunk) {

                DIVIDER.threshold(chunk).divide(j + 1, n, (first, limit) -> JVM.updateColumns(upper, n, a, offsetA, lda, multipliers, j, first, limit));

            } else {

                JVM.updateColumns(upper, n, a, offsetA, lda, multipliers, j, j + 1, n);
            }
        }

        /**
         * @see #update(boolean, int, double[], int, int, double[], int)
         */
        private static <N extends Scalar<N>> void update(final boolean upper, final int n, final N[] a, final int offsetA, final int lda, final N[] multipliers,
                final int j) {
            int chunk = MatrixOperation.chunk(THRESHOLD, (n - j - 1) / 2.0);

            if (n - j - 1 > chunk) {

                DIVIDER.threshold(chunk).divide(j + 1, n, (first, limit) -> JVM.updateColumns(upper, n, a, offsetA, lda, multipliers, j, first, limit));

            } else {

                JVM.updateColumns(upper, n, a, offsetA, lda, multipliers, j, j + 1, n);
            }
        }

        /**
         * Updates the trailing triangle, columns first to limit (exclusive), with the multipliers of column
         * (or row) j. Lower: rows k to n-1 of column k. Upper: rows j+1 to k of column k.
         */
        private static void updateColumns(final boolean upper, final int n, final double[] a, final int offsetA, final int lda, final double[] multipliers,
                final int j, final int first, final int limit) {
            for (int k = first; k < limit; k++) {
                if (upper) {
                    AXPY.JVM.invoke(k - j, -multipliers[k], multipliers, j + 1, 1, a, offsetA + k * lda + j + 1, 1);
                } else {
                    AXPY.JVM.invoke(n - k, -multipliers[k], multipliers, k, 1, a, offsetA + k * lda + k, 1);
                }
            }
        }

        /**
         * As {@link #updateColumns(boolean, int, double[], int, int, double[], int, int, int)}, for other
         * number types. Lower: A(i,k) -= m(i)*conj(m(k)). Upper: A(i,k) -= conj(m(i))*m(k).
         */
        private static <N extends Scalar<N>> void updateColumns(final boolean upper, final int n, final N[] a, final int offsetA, final int lda,
                final N[] multipliers, final int j, final int first, final int limit) {
            for (int k = first; k < limit; k++) {
                int column = offsetA + k * lda;
                if (upper) {
                    N factor = multipliers[k];
                    for (int i = j + 1; i <= k; i++) {
                        a[column + i] = a[column + i].subtract(multipliers[i].conjugate().multiply(factor)).get();
                    }
                } else {
                    AXPY.invoke(a, column, multipliers[k].conjugate().negate().get(), multipliers, 0, k, n);
                }
            }
        }

        /**
         * The pure Java implementation of {@link POTRF#invoke(boolean, int, double[], int, int)}, without
         * argument checks, for n > 0.
         */
        static int invoke(final boolean upper, final int n, final double[] a, final int offsetA, final int lda) {

            double[] multipliers = new double[n];

            for (int j = 0; j < n; j++) {

                int diagonal = offsetA + j + j * lda;

                double pivot = a[diagonal];
                if (!(pivot > ZERO)) {
                    return j + 1;
                }

                double root = Math.sqrt(pivot);
                a[diagonal] = root;

                if (upper) {
                    for (int k = j + 1, index = diagonal + lda; k < n; k++, index += lda) {
                        multipliers[k] = a[index] /= root;
                    }
                } else {
                    for (int i = j + 1, index = diagonal + 1; i < n; i++, index++) {
                        multipliers[i] = a[index] /= root;
                    }
                }

                JVM.update(upper, n, a, offsetA, lda, multipliers, j);
            }

            return 0;
        }

        /**
         * The pure Java implementation of
         * {@link POTRF#invoke(boolean, int, Scalar[], int, int, Scalar.Factory, FunctionSet)}, without
         * argument checks, for n > 0.
         */
        static <N extends Scalar<N>> int invoke(final boolean upper, final int n, final N[] a, final int offsetA, final int lda, final Scalar.Factory<N> scalar,
                final FunctionSet<N> function) {

            N[] multipliers = scalar.newArrayInstance(n);
            UnaryFunction<N> sqrt = function.sqrt();

            for (int j = 0; j < n; j++) {

                int diagonal = offsetA + j + j * lda;

                if (!(a[diagonal].doubleValue() > ZERO)) {
                    return j + 1;
                }

                N root = sqrt.invoke(a[diagonal]);
                a[diagonal] = root;

                if (upper) {
                    for (int k = j + 1, index = diagonal + lda; k < n; k++, index += lda) {
                        multipliers[k] = a[index] = a[index].divide(root).get();
                    }
                } else {
                    for (int i = j + 1, index = diagonal + 1; i < n; i++, index++) {
                        multipliers[i] = a[index] = a[index].divide(root).get();
                    }
                }

                JVM.update(upper, n, a, offsetA, lda, multipliers, j);
            }

            return 0;
        }

    }

    /**
     * The Java implementation splits the update of the trailing triangle over several threads when it is
     * larger than this cubed (in multiply-adds). Each part then gets more than half of that.
     */
    static int THRESHOLD = 56;

    /**
     * {@code dpotrf}: A = L*L<sup>T</sup> or A = U<sup>T</sup>*U, the factor overwrites the triangle of A
     * <p>
     * A is column-major, stored in its array from an offset with a leading dimension.
     *
     * @param upper A = U<sup>T</sup>*U, and only the upper triangle is referenced, if true; otherwise A =
     *              L*L<sup>T</sup> and the lower triangle
     * @param n     The order of A
     * @return 0, or k > 0 if the leading minor of order k is not positive definite. The factorisation is then
     *         not completed.
     */
    public static int invoke(final boolean upper, final int n, final double[] a, final int offsetA, final int lda) {

        ArrayOperation.checkArgument("POTRF", "n", n >= 0);
        ArrayOperation.checkArgument("POTRF", "lda", lda >= Math.max(1, n));

        if (n == 0) {
            return 0;
        }

        return JVM.invoke(upper, n, a, offsetA, lda);
    }

    /**
     * As {@link #invoke(boolean, int, double[], int, int)}, for other number types: A is Hermitian, and A =
     * L*L<sup>H</sup> or A = U<sup>H</sup>*U. The real parts of the diagonal elements decide whether A is
     * positive definite. There is only a pure Java implementation.
     *
     * @param function Provides the square root
     */
    public static <N extends Scalar<N>> int invoke(final boolean upper, final int n, final N[] a, final int offsetA, final int lda,
            final Scalar.Factory<N> scalar, final FunctionSet<N> function) {

        ArrayOperation.checkArgument("POTRF", "n", n >= 0);
        ArrayOperation.checkArgument("POTRF", "lda", lda >= Math.max(1, n));

        if (n == 0) {
            return 0;
        }

        return JVM.invoke(upper, n, a, offsetA, lda, scalar, function);
    }

}

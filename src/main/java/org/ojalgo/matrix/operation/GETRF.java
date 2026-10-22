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
import org.ojalgo.scalar.Scalar;

/**
 * LAPACK {@code ?getrf}: LU factorisation of a general m-by-n matrix A, using partial pivoting with row
 * interchanges:
 *
 * <pre>{@code
 * A = P * L * U
 * }</pre>
 *
 * P is a permutation matrix, L is lower triangular with unit diagonal elements (lower trapezoidal if m > n)
 * and U is upper triangular (upper trapezoidal if m < n). The factors overwrite A: L below the diagonal (its
 * unit diagonal is not stored) and U on and above it.
 */
public abstract class GETRF implements LAPACK {

    /**
     * The pure Java implementation.
     * <p>
     * Unblocked and right-looking: for each column, the pivot is the element of largest magnitude on or below
     * the diagonal (the first one if there are several), its row is interchanged with the diagonal row across
     * all columns, the elements below the diagonal are divided by the pivot, and the columns to the right are
     * updated with that column (a rank 1 update), split over several threads when that is large, see
     * {@link GETRF#THRESHOLD}.
     */
    static final class JVM {

        private static final DivideAndConquer.Divider DIVIDER = Parallelism.CORES.newDivider();

        /**
         * Divides the elements below the diagonal in column j by the diagonal element, and updates the
         * columns to the right with them – unless the diagonal element is exactly zero.
         *
         * @return false if the diagonal element is zero
         */
        private static boolean eliminate(final int m, final int n, final double[] a, final int offsetA, final int lda, final double[] multipliers,
                final int j) {

            int column = offsetA + j * lda;
            double diagonal = a[column + j];

            if (diagonal == ZERO) {
                return false;
            }

            for (int i = j + 1; i < m; i++) {
                multipliers[i] = a[column + i] /= diagonal;
            }

            JVM.update(m, n, a, offsetA, lda, multipliers, j);

            return true;
        }

        /**
         * As {@link #eliminate(int, int, double[], int, int, double[], int)}, for other number types: the
         * elements below the diagonal are divided from the right.
         */
        private static <N extends Scalar<N>> boolean eliminate(final int m, final int n, final N[] a, final int offsetA, final int lda, final N[] multipliers,
                final int j) {

            int column = offsetA + j * lda;
            N diagonal = a[column + j];

            if (diagonal.isZero()) {
                return false;
            }

            for (int i = j + 1; i < m; i++) {
                multipliers[i] = a[column + i] = a[column + i].divide(diagonal).get();
            }

            JVM.update(m, n, a, offsetA, lda, multipliers, j);

            return true;
        }

        /**
         * Interchanges rows i and j, across all n columns.
         */
        private static void interchange(final int n, final double[] a, final int offsetA, final int lda, final int i, final int j) {
            for (int k = 0; k < n; k++) {
                int base = offsetA + k * lda;
                double tmp = a[base + i];
                a[base + i] = a[base + j];
                a[base + j] = tmp;
            }
        }

        private static <N extends Scalar<N>> void interchange(final int n, final N[] a, final int offsetA, final int lda, final int i, final int j) {
            for (int k = 0; k < n; k++) {
                int base = offsetA + k * lda;
                N tmp = a[base + i];
                a[base + i] = a[base + j];
                a[base + j] = tmp;
            }
        }

        /**
         * Updates the columns to the right of column j, split over several threads when that is large, see
         * {@link GETRF#THRESHOLD}.
         */
        private static void update(final int m, final int n, final double[] a, final int offsetA, final int lda, final double[] multipliers, final int j) {
            int chunk = MatrixOperation.chunk(THRESHOLD, m - j - 1);

            if (n - j - 1 > chunk) {

                DIVIDER.threshold(chunk).divide(j + 1, n, (first, limit) -> JVM.updateColumns(m, a, offsetA, lda, multipliers, j, first, limit));

            } else {

                JVM.updateColumns(m, a, offsetA, lda, multipliers, j, j + 1, n);
            }
        }

        private static <N extends Scalar<N>> void update(final int m, final int n, final N[] a, final int offsetA, final int lda, final N[] multipliers,
                final int j) {
            int chunk = MatrixOperation.chunk(THRESHOLD, m - j - 1);

            if (n - j - 1 > chunk) {

                DIVIDER.threshold(chunk).divide(j + 1, n, (first, limit) -> JVM.updateColumns(m, a, offsetA, lda, multipliers, j, first, limit));

            } else {

                JVM.updateColumns(m, a, offsetA, lda, multipliers, j, j + 1, n);
            }
        }

        /**
         * Subtracts the multipliers (column j below the diagonal, divided by the pivot) times the row j
         * element from each of the columns first to limit (exclusive), below row j.
         */
        private static void updateColumns(final int m, final double[] a, final int offsetA, final int lda, final double[] multipliers, final int j,
                final int first, final int limit) {
            for (int k = first; k < limit; k++) {
                int column = offsetA + k * lda;
                AXPY.JVM.invoke(m - j - 1, -a[column + j], multipliers, j + 1, 1, a, column + j + 1, 1);
            }
        }

        /**
         * As {@link #updateColumns(int, double[], int, int, double[], int, int, int)}, for other number
         * types: A(i,k) -= m(i)*A(j,k).
         */
        private static <N extends Scalar<N>> void updateColumns(final int m, final N[] a, final int offsetA, final int lda, final N[] multipliers, final int j,
                final int first, final int limit) {
            for (int k = first; k < limit; k++) {
                int column = offsetA + k * lda;
                AXPY.invoke(a, column, a[column + j].negate().get(), multipliers, 0, j + 1, m);
            }
        }

        /**
         * The pure Java implementation of {@link GETRF#invoke(int, int, double[], int, int, int[], int)},
         * without argument checks, for m, n > 0.
         */
        static int invoke(final int m, final int n, final double[] a, final int offsetA, final int lda, final int[] ipiv, final int offsetIpiv) {

            int info = 0;
            double[] multipliers = new double[m];

            for (int j = 0, limit = Math.min(m, n); j < limit; j++) {

                int column = offsetA + j * lda;

                int pivot = j;
                double largest = ZERO;
                for (int i = j; i < m; i++) {
                    double candidate = Math.abs(a[column + i]);
                    if (candidate > largest) {
                        largest = candidate;
                        pivot = i;
                    }
                }

                ipiv[offsetIpiv + j] = pivot + 1;

                if (pivot != j) {
                    JVM.interchange(n, a, offsetA, lda, pivot, j);
                }

                if (!JVM.eliminate(m, n, a, offsetA, lda, multipliers, j) && info == 0) {
                    info = j + 1;
                }
            }

            return info;
        }

        /**
         * The pure Java implementation of
         * {@link GETRF#invoke(int, int, Scalar[], int, int, int[], int, Scalar.Factory)}, without argument
         * checks, for m, n > 0. The pivot is the element of largest norm.
         */
        static <N extends Scalar<N>> int invoke(final int m, final int n, final N[] a, final int offsetA, final int lda, final int[] ipiv, final int offsetIpiv,
                final Scalar.Factory<N> scalar) {

            int info = 0;
            N[] multipliers = scalar.newArrayInstance(m);

            for (int j = 0, limit = Math.min(m, n); j < limit; j++) {

                int column = offsetA + j * lda;

                int pivot = j;
                double largest = ZERO;
                for (int i = j; i < m; i++) {
                    double candidate = a[column + i].norm();
                    if (candidate > largest) {
                        largest = candidate;
                        pivot = i;
                    }
                }

                ipiv[offsetIpiv + j] = pivot + 1;

                if (pivot != j) {
                    JVM.interchange(n, a, offsetA, lda, pivot, j);
                }

                if (!JVM.eliminate(m, n, a, offsetA, lda, multipliers, j) && info == 0) {
                    info = j + 1;
                }
            }

            return info;
        }

        /**
         * The pure Java implementation of {@link GETRF#invokeWithoutPivoting(int, int, double[], int, int)},
         * without argument checks, for m, n > 0.
         */
        static int invokeWithoutPivoting(final int m, final int n, final double[] a, final int offsetA, final int lda) {

            int info = 0;
            double[] multipliers = new double[m];

            for (int j = 0, limit = Math.min(m, n); j < limit; j++) {
                if (!JVM.eliminate(m, n, a, offsetA, lda, multipliers, j) && info == 0) {
                    info = j + 1;
                }
            }

            return info;
        }

        /**
         * The pure Java implementation of
         * {@link GETRF#invokeWithoutPivoting(int, int, Scalar[], int, int, Scalar.Factory)}, without argument
         * checks, for m, n > 0.
         */
        static <N extends Scalar<N>> int invokeWithoutPivoting(final int m, final int n, final N[] a, final int offsetA, final int lda,
                final Scalar.Factory<N> scalar) {

            int info = 0;
            N[] multipliers = scalar.newArrayInstance(m);

            for (int j = 0, limit = Math.min(m, n); j < limit; j++) {
                if (!JVM.eliminate(m, n, a, offsetA, lda, multipliers, j) && info == 0) {
                    info = j + 1;
                }
            }

            return info;
        }

    }

    /**
     * The Java implementation splits the update of the columns to the right of the pivot over several threads
     * when it is larger than this cubed (in multiply-adds). Each part then gets more than half of that.
     */
    static int THRESHOLD = 56;

    /**
     * {@code dgetrf}: A = P*L*U, the factors overwrite A
     * <p>
     * A is column-major, stored in its array from an offset with a leading dimension. P is returned as a
     * sequence of row interchanges, with 1-based row numbers as in LAPACK: for i = 1, ..., min(m,n), row i
     * was interchanged with row ipiv(i), stored in {@code ipiv[offsetIpiv + i - 1]}. Applied in that order to
     * the rows of A, the interchanges give L*U.
     *
     * @param m The number of rows of A
     * @param n The number of columns of A
     * @return 0, or i > 0 if U(i,i) is exactly zero (1-based). The factorisation is then completed, but U is
     *         singular, and solving with it divides by zero.
     */
    public static int invoke(final int m, final int n, final double[] a, final int offsetA, final int lda, final int[] ipiv, final int offsetIpiv) {

        ArrayOperation.checkArgument("GETRF", "m", m >= 0);
        ArrayOperation.checkArgument("GETRF", "n", n >= 0);
        ArrayOperation.checkArgument("GETRF", "lda", lda >= Math.max(1, m));

        if (m == 0 || n == 0) {
            return 0;
        }

        return JVM.invoke(m, n, a, offsetA, lda, ipiv, offsetIpiv);
    }

    /**
     * As {@link #invoke(int, int, double[], int, int, int[], int)}, for other number types: the pivot is the
     * element of largest norm, and L is applied from the left – A = P*L*U holds also when multiplication is
     * not commutative. There is only a pure Java implementation.
     */
    public static <N extends Scalar<N>> int invoke(final int m, final int n, final N[] a, final int offsetA, final int lda, final int[] ipiv,
            final int offsetIpiv, final Scalar.Factory<N> scalar) {

        ArrayOperation.checkArgument("GETRF", "m", m >= 0);
        ArrayOperation.checkArgument("GETRF", "n", n >= 0);
        ArrayOperation.checkArgument("GETRF", "lda", lda >= Math.max(1, m));

        if (m == 0 || n == 0) {
            return 0;
        }

        return JVM.invoke(m, n, a, offsetA, lda, ipiv, offsetIpiv, scalar);
    }

    /**
     * As {@link #invoke(int, int, double[], int, int, int[], int)}, but without row interchanges: A = L*U.
     * Where U(i,i) is exactly zero, the elements below it are left as they are. There is only a pure Java
     * implementation.
     *
     * @return 0, or i > 0 if U(i,i) is exactly zero (1-based) – the first such i
     */
    public static int invokeWithoutPivoting(final int m, final int n, final double[] a, final int offsetA, final int lda) {

        ArrayOperation.checkArgument("GETRF", "m", m >= 0);
        ArrayOperation.checkArgument("GETRF", "n", n >= 0);
        ArrayOperation.checkArgument("GETRF", "lda", lda >= Math.max(1, m));

        if (m == 0 || n == 0) {
            return 0;
        }

        return JVM.invokeWithoutPivoting(m, n, a, offsetA, lda);
    }

    /**
     * As {@link #invokeWithoutPivoting(int, int, double[], int, int)}, for other number types.
     */
    public static <N extends Scalar<N>> int invokeWithoutPivoting(final int m, final int n, final N[] a, final int offsetA, final int lda,
            final Scalar.Factory<N> scalar) {

        ArrayOperation.checkArgument("GETRF", "m", m >= 0);
        ArrayOperation.checkArgument("GETRF", "n", n >= 0);
        ArrayOperation.checkArgument("GETRF", "lda", lda >= Math.max(1, m));

        if (m == 0 || n == 0) {
            return 0;
        }

        return JVM.invokeWithoutPivoting(m, n, a, offsetA, lda, scalar);
    }

}

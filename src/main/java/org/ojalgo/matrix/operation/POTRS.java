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
import org.ojalgo.concurrent.DivideAndConquer;
import org.ojalgo.concurrent.Parallelism;
import org.ojalgo.scalar.Scalar;

/**
 * LAPACK {@code ?potrs}: solves A*X = B, for a symmetric positive definite n-by-n matrix A, using the
 * Cholesky factorisation A = L*L<sup>T</sup> or A = U<sup>T</sup>*U computed by {@link POTRF}. X overwrites
 * B. Also solves A*X = I, writing the inverse of A to a separate matrix.
 */
public abstract class POTRS implements LAPACK {

    /**
     * The pure Java implementation.
     * <p>
     * Each column of B is solved for separately, with the two triangular systems solved by {@link TRSV}. The
     * columns are solved in parallel when that is large, see {@link POTRS#THRESHOLD}. With B = I, the
     * solution is symmetric: column j is solved for from row j on only (both triangular systems), and the
     * rest copied from the rows of the columns before it.
     */
    static final class JVM {

        private static final DivideAndConquer.Divider DIVIDER = Parallelism.CORES.newDivider();

        private static void solve(final boolean upper, final int n, final double[] a, final int offsetA, final int lda, final double[] b, final int offsetB,
                final int ldb, final int first, final int limit) {
            for (int j = first; j < limit; j++) {
                int column = offsetB + j * ldb;
                TRSV.JVM.invoke(upper, upper, false, n, a, offsetA, lda, b, column, 1);
                TRSV.JVM.invoke(upper, !upper, false, n, a, offsetA, lda, b, column, 1);
            }
        }

        private static <N extends Scalar<N>> void solve(final boolean upper, final int n, final N[] a, final int offsetA, final int lda, final N[] b,
                final int offsetB, final int ldb, final int first, final int limit, final Scalar.Factory<N> scalar) {
            for (int j = first; j < limit; j++) {
                int column = offsetB + j * ldb;
                TRSV.JVM.invoke(upper, upper, false, n, a, offsetA, lda, b, column, 1, scalar);
                TRSV.JVM.invoke(upper, !upper, false, n, a, offsetA, lda, b, column, 1, scalar);
            }
        }

        private static void solveIdentity(final boolean upper, final int n, final double[] a, final int offsetA, final int lda, final double[] b,
                final int offsetB, final int ldb, final int first, final int limit) {
            for (int j = first; j < limit; j++) {
                int column = offsetB + j * ldb;
                int trailing = offsetA + j + j * lda;
                Arrays.fill(b, column, column + n, ZERO);
                b[column + j] = ONE;
                TRSV.JVM.invoke(upper, upper, false, n - j, a, trailing, lda, b, column + j, 1);
                TRSV.JVM.invoke(upper, !upper, false, n - j, a, trailing, lda, b, column + j, 1);
            }
        }

        private static <N extends Scalar<N>> void solveIdentity(final boolean upper, final int n, final N[] a, final int offsetA, final int lda, final N[] b,
                final int offsetB, final int ldb, final int first, final int limit, final Scalar.Factory<N> scalar) {
            N zero = scalar.zero().get();
            for (int j = first; j < limit; j++) {
                int column = offsetB + j * ldb;
                int trailing = offsetA + j + j * lda;
                Arrays.fill(b, column, column + n, zero);
                b[column + j] = scalar.one().get();
                TRSV.JVM.invoke(upper, upper, false, n - j, a, trailing, lda, b, column + j, 1, scalar);
                TRSV.JVM.invoke(upper, !upper, false, n - j, a, trailing, lda, b, column + j, 1, scalar);
            }
        }

        /**
         * The pure Java implementation of
         * {@link POTRS#invoke(boolean, int, int, double[], int, int, double[], int, int)}, without argument
         * checks, for n, nrhs > 0.
         */
        static void invoke(final boolean upper, final int n, final int nrhs, final double[] a, final int offsetA, final int lda, final double[] b,
                final int offsetB, final int ldb) {
            int chunk = MatrixOperation.chunk(THRESHOLD, n, n);

            if (nrhs > chunk) {

                DIVIDER.threshold(chunk).divide(0, nrhs, (first, limit) -> JVM.solve(upper, n, a, offsetA, lda, b, offsetB, ldb, first, limit));

            } else {

                JVM.solve(upper, n, a, offsetA, lda, b, offsetB, ldb, 0, nrhs);
            }
        }

        /**
         * The pure Java implementation of
         * {@link POTRS#invoke(boolean, int, int, Scalar[], int, int, Scalar[], int, int, Scalar.Factory)},
         * without argument checks, for n, nrhs > 0.
         */
        static <N extends Scalar<N>> void invoke(final boolean upper, final int n, final int nrhs, final N[] a, final int offsetA, final int lda, final N[] b,
                final int offsetB, final int ldb, final Scalar.Factory<N> scalar) {
            int chunk = MatrixOperation.chunk(THRESHOLD, n, n);

            if (nrhs > chunk) {

                DIVIDER.threshold(chunk).divide(0, nrhs, (first, limit) -> JVM.solve(upper, n, a, offsetA, lda, b, offsetB, ldb, first, limit, scalar));

            } else {

                JVM.solve(upper, n, a, offsetA, lda, b, offsetB, ldb, 0, nrhs, scalar);
            }
        }

        /**
         * The pure Java implementation of
         * {@link POTRS#invokeIdentity(boolean, int, double[], int, int, double[], int, int)}, without
         * argument checks, for n > 0.
         */
        static void invokeIdentity(final boolean upper, final int n, final double[] a, final int offsetA, final int lda, final double[] b, final int offsetB,
                final int ldb) {

            // Both systems are solved from the diagonal on: 1/3 of n*n multiply-adds per column, on average
            int chunk = MatrixOperation.chunk(THRESHOLD, n * n / 3.0);

            if (n > chunk) {

                DIVIDER.threshold(chunk).divide(0, n, (first, limit) -> JVM.solveIdentity(upper, n, a, offsetA, lda, b, offsetB, ldb, first, limit));

            } else {

                JVM.solveIdentity(upper, n, a, offsetA, lda, b, offsetB, ldb, 0, n);
            }

            POTRS.copyLowerToUpper(n, b, offsetB, ldb);
        }

        /**
         * The pure Java implementation of
         * {@link POTRS#invokeIdentity(boolean, int, Scalar[], int, int, Scalar[], int, int, Scalar.Factory)},
         * without argument checks, for n > 0.
         */
        static <N extends Scalar<N>> void invokeIdentity(final boolean upper, final int n, final N[] a, final int offsetA, final int lda, final N[] b,
                final int offsetB, final int ldb, final Scalar.Factory<N> scalar) {

            int chunk = MatrixOperation.chunk(THRESHOLD, n * n / 3.0);

            if (n > chunk) {

                DIVIDER.threshold(chunk).divide(0, n, (first, limit) -> JVM.solveIdentity(upper, n, a, offsetA, lda, b, offsetB, ldb, first, limit, scalar));

            } else {

                JVM.solveIdentity(upper, n, a, offsetA, lda, b, offsetB, ldb, 0, n, scalar);
            }

            POTRS.copyLowerToUpper(n, b, offsetB, ldb);
        }

    }

    /**
     * More right hand sides than this are solved for together. Fewer are solved for one at a time, each with
     * two calls to {@link TRSV}.
     */
    private static final int COLUMNS_THRESHOLD = 3;

    /**
     * The Java implementation solves the columns of B in parallel when n*n*nrhs (the number of multiply-adds)
     * is larger than this cubed. Each part then gets more than half of that.
     */
    static int THRESHOLD = 40;

    /**
     * {@code dpotrs}: solves A*X = B, X overwrites B
     * <p>
     * A and B are column-major, stored in their arrays from an offset with a leading dimension. A holds the
     * factor, L or U, as returned by {@link POTRF}; the other triangle is not referenced.
     *
     * @param upper A = U<sup>T</sup>*U if true, otherwise A = L*L<sup>T</sup>
     * @param n     The order of A
     * @param nrhs  The number of right hand sides (columns of B)
     */
    public static void invoke(final boolean upper, final int n, final int nrhs, final double[] a, final int offsetA, final int lda, final double[] b,
            final int offsetB, final int ldb) {

        ArrayOperation.checkArgument("POTRS", "n", n >= 0);
        ArrayOperation.checkArgument("POTRS", "nrhs", nrhs >= 0);
        ArrayOperation.checkArgument("POTRS", "lda", lda >= Math.max(1, n));
        ArrayOperation.checkArgument("POTRS", "ldb", ldb >= Math.max(1, n));

        if (n == 0 || nrhs == 0) {
            return;
        }

        if (nrhs <= COLUMNS_THRESHOLD) {
            POTRS.solveColumns(upper, n, nrhs, a, offsetA, lda, b, offsetB, ldb);
        } else {
            JVM.invoke(upper, n, nrhs, a, offsetA, lda, b, offsetB, ldb);
        }
    }

    /**
     * As {@link #invoke(boolean, int, int, double[], int, int, double[], int, int)}, for other number types:
     * A is Hermitian, and A = L*L<sup>H</sup> or A = U<sup>H</sup>*U. There is only a pure Java
     * implementation.
     */
    public static <N extends Scalar<N>> void invoke(final boolean upper, final int n, final int nrhs, final N[] a, final int offsetA, final int lda,
            final N[] b, final int offsetB, final int ldb, final Scalar.Factory<N> scalar) {

        ArrayOperation.checkArgument("POTRS", "n", n >= 0);
        ArrayOperation.checkArgument("POTRS", "nrhs", nrhs >= 0);
        ArrayOperation.checkArgument("POTRS", "lda", lda >= Math.max(1, n));
        ArrayOperation.checkArgument("POTRS", "ldb", ldb >= Math.max(1, n));

        if (n == 0 || nrhs == 0) {
            return;
        }

        JVM.invoke(upper, n, nrhs, a, offsetA, lda, b, offsetB, ldb, scalar);
    }

    /**
     * {@code dpotrs} with B = I: X, the inverse of A, is written to B
     * <p>
     * A and B are column-major, stored in their arrays from an offset with a leading dimension. A holds the
     * factor, L or U, as returned by {@link POTRF}; it is not changed, and the other triangle is not
     * referenced. B need not be set on entry. X is exactly symmetric: its upper triangle is a copy of its
     * lower.
     *
     * @param upper A = U<sup>T</sup>*U if true, otherwise A = L*L<sup>T</sup>
     * @param n     The order of A and B
     */
    public static void invokeIdentity(final boolean upper, final int n, final double[] a, final int offsetA, final int lda, final double[] b, final int offsetB,
            final int ldb) {

        ArrayOperation.checkArgument("POTRS", "n", n >= 0);
        ArrayOperation.checkArgument("POTRS", "lda", lda >= Math.max(1, n));
        ArrayOperation.checkArgument("POTRS", "ldb", ldb >= Math.max(1, n));

        if (n == 0) {
            return;
        }

        JVM.invokeIdentity(upper, n, a, offsetA, lda, b, offsetB, ldb);
    }

    /**
     * As {@link #invokeIdentity(boolean, int, double[], int, int, double[], int, int)}, for other number
     * types: X is exactly Hermitian, its upper triangle the conjugate of its lower. There is only a pure Java
     * implementation.
     */
    public static <N extends Scalar<N>> void invokeIdentity(final boolean upper, final int n, final N[] a, final int offsetA, final int lda, final N[] b,
            final int offsetB, final int ldb, final Scalar.Factory<N> scalar) {

        ArrayOperation.checkArgument("POTRS", "n", n >= 0);
        ArrayOperation.checkArgument("POTRS", "lda", lda >= Math.max(1, n));
        ArrayOperation.checkArgument("POTRS", "ldb", ldb >= Math.max(1, n));

        if (n == 0) {
            return;
        }

        JVM.invokeIdentity(upper, n, a, offsetA, lda, b, offsetB, ldb, scalar);
    }

    /**
     * Copies the strictly lower triangle of the n-by-n matrix B to its upper triangle.
     */
    private static void copyLowerToUpper(final int n, final double[] b, final int offsetB, final int ldb) {
        for (int j = 1; j < n; j++) {
            for (int i = 0; i < j; i++) {
                b[offsetB + i + j * ldb] = b[offsetB + j + i * ldb];
            }
        }
    }

    /**
     * Copies the conjugate of the strictly lower triangle of the n-by-n matrix B to its upper triangle.
     */
    private static <N extends Scalar<N>> void copyLowerToUpper(final int n, final N[] b, final int offsetB, final int ldb) {
        for (int j = 1; j < n; j++) {
            for (int i = 0; i < j; i++) {
                b[offsetB + i + j * ldb] = b[offsetB + j + i * ldb].conjugate().get();
            }
        }
    }

    /**
     * The right hand sides one at a time, each with two calls to the dispatching
     * {@link TRSV#invoke(boolean, boolean, boolean, int, double[], int, int, double[], int, int)}.
     */
    private static void solveColumns(final boolean upper, final int n, final int nrhs, final double[] a, final int offsetA, final int lda, final double[] b,
            final int offsetB, final int ldb) {
        for (int j = 0; j < nrhs; j++) {
            int column = offsetB + j * ldb;
            TRSV.invoke(upper, upper, false, n, a, offsetA, lda, b, column, 1);
            TRSV.invoke(upper, !upper, false, n, a, offsetA, lda, b, column, 1);
        }
    }

}

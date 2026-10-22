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

import org.ojalgo.array.operation.ArrayOperation;
import org.ojalgo.concurrent.DivideAndConquer;
import org.ojalgo.concurrent.Parallelism;
import org.ojalgo.scalar.Scalar;

/**
 * LAPACK {@code ?trtrs}: solves A*X = B or A<sup>T</sup>*X = B, for a triangular n-by-n matrix A. X
 * overwrites B. A is first checked for singularity: if a diagonal element is exactly zero, nothing is solved.
 */
public abstract class TRTRS implements LAPACK {

    /**
     * The pure Java implementation.
     * <p>
     * Each column of B is solved for separately, by {@link TRSV}. The columns are solved in parallel when
     * that is large, see {@link TRTRS#THRESHOLD}.
     */
    static final class JVM {

        private static final DivideAndConquer.Divider DIVIDER = Parallelism.CORES.newDivider();

        private static void solve(final boolean upper, final boolean transpose, final boolean unitDiagonal, final int n, final double[] a, final int offsetA,
                final int lda, final double[] b, final int offsetB, final int ldb, final int first, final int limit) {
            for (int j = first; j < limit; j++) {
                TRSV.JVM.invoke(upper, transpose, unitDiagonal, n, a, offsetA, lda, b, offsetB + j * ldb, 1);
            }
        }

        private static <N extends Scalar<N>> void solve(final boolean upper, final boolean transpose, final boolean unitDiagonal, final int n, final N[] a,
                final int offsetA, final int lda, final N[] b, final int offsetB, final int ldb, final int first, final int limit,
                final Scalar.Factory<N> scalar) {
            for (int j = first; j < limit; j++) {
                TRSV.JVM.invoke(upper, transpose, unitDiagonal, n, a, offsetA, lda, b, offsetB + j * ldb, 1, scalar);
            }
        }

        /**
         * The pure Java implementation of
         * {@link TRTRS#invoke(boolean, boolean, boolean, int, int, double[], int, int, double[], int, int)},
         * without argument checks, for n > 0.
         */
        static int invoke(final boolean upper, final boolean transpose, final boolean unitDiagonal, final int n, final int nrhs, final double[] a,
                final int offsetA, final int lda, final double[] b, final int offsetB, final int ldb) {

            int info = TRTRS.singular(unitDiagonal, n, a, offsetA, lda);
            if (info != 0) {
                return info;
            }

            // Half of n*n multiply-adds per column
            int chunk = MatrixOperation.chunk(THRESHOLD, n * n / 2.0);

            if (nrhs > chunk) {

                DIVIDER.threshold(chunk).divide(0, nrhs,
                        (first, limit) -> JVM.solve(upper, transpose, unitDiagonal, n, a, offsetA, lda, b, offsetB, ldb, first, limit));

            } else {

                JVM.solve(upper, transpose, unitDiagonal, n, a, offsetA, lda, b, offsetB, ldb, 0, nrhs);
            }

            return 0;
        }

        /**
         * The pure Java implementation of
         * {@link TRTRS#invoke(boolean, boolean, boolean, int, int, Scalar[], int, int, Scalar[], int, int, Scalar.Factory)},
         * without argument checks, for n > 0.
         */
        static <N extends Scalar<N>> int invoke(final boolean upper, final boolean transpose, final boolean unitDiagonal, final int n, final int nrhs,
                final N[] a, final int offsetA, final int lda, final N[] b, final int offsetB, final int ldb, final Scalar.Factory<N> scalar) {

            int info = TRTRS.singular(unitDiagonal, n, a, offsetA, lda);
            if (info != 0) {
                return info;
            }

            int chunk = MatrixOperation.chunk(THRESHOLD, n * n / 2.0);

            if (nrhs > chunk) {

                DIVIDER.threshold(chunk).divide(0, nrhs,
                        (first, limit) -> JVM.solve(upper, transpose, unitDiagonal, n, a, offsetA, lda, b, offsetB, ldb, first, limit, scalar));

            } else {

                JVM.solve(upper, transpose, unitDiagonal, n, a, offsetA, lda, b, offsetB, ldb, 0, nrhs, scalar);
            }

            return 0;
        }

    }

    /**
     * More right hand sides than this are solved for together. Fewer are solved for one at a time, each with a
     * call to {@link TRSV}.
     */
    private static final int COLUMNS_THRESHOLD = 3;

    /**
     * The Java implementation solves the columns of B in parallel when the number of multiply-adds is larger
     * than this cubed. Each part then gets more than half of that.
     */
    static int THRESHOLD = 48;

    /**
     * {@code dtrtrs}: solves A*X = B or A<sup>T</sup>*X = B, X overwrites B
     * <p>
     * A and B are column-major, stored in their arrays from an offset with a leading dimension. Only the
     * upper or lower triangle of A is read, and not its diagonal if unit.
     *
     * @param upper        A is upper triangular if true, otherwise lower
     * @param transpose    Solves A<sup>T</sup>*X = B if true, otherwise A*X = B
     * @param unitDiagonal A has a unit diagonal (assumed, not read) if true
     * @param n            The order of A
     * @param nrhs         The number of right hand sides (columns of B)
     * @return 0, or i > 0 if A(i,i) is exactly zero (1-based). B is then unchanged.
     */
    public static int invoke(final boolean upper, final boolean transpose, final boolean unitDiagonal, final int n, final int nrhs, final double[] a,
            final int offsetA, final int lda, final double[] b, final int offsetB, final int ldb) {

        ArrayOperation.checkArgument("TRTRS", "n", n >= 0);
        ArrayOperation.checkArgument("TRTRS", "nrhs", nrhs >= 0);
        ArrayOperation.checkArgument("TRTRS", "lda", lda >= Math.max(1, n));
        ArrayOperation.checkArgument("TRTRS", "ldb", ldb >= Math.max(1, n));

        if (n == 0) {
            return 0;
        }

        if (nrhs <= COLUMNS_THRESHOLD) {
            return TRTRS.solveColumns(upper, transpose, unitDiagonal, n, nrhs, a, offsetA, lda, b, offsetB, ldb);
        } else {
            return JVM.invoke(upper, transpose, unitDiagonal, n, nrhs, a, offsetA, lda, b, offsetB, ldb);
        }
    }

    /**
     * As {@link #invoke(boolean, boolean, boolean, int, int, double[], int, int, double[], int, int)}, for
     * other number types: A is applied from the left. There is only a pure Java implementation.
     *
     * @param transpose Solves A<sup>H</sup>*X = B, with the conjugate transpose, if true, otherwise A*X = B
     */
    public static <N extends Scalar<N>> int invoke(final boolean upper, final boolean transpose, final boolean unitDiagonal, final int n, final int nrhs,
            final N[] a, final int offsetA, final int lda, final N[] b, final int offsetB, final int ldb, final Scalar.Factory<N> scalar) {

        ArrayOperation.checkArgument("TRTRS", "n", n >= 0);
        ArrayOperation.checkArgument("TRTRS", "nrhs", nrhs >= 0);
        ArrayOperation.checkArgument("TRTRS", "lda", lda >= Math.max(1, n));
        ArrayOperation.checkArgument("TRTRS", "ldb", ldb >= Math.max(1, n));

        if (n == 0) {
            return 0;
        }

        return JVM.invoke(upper, transpose, unitDiagonal, n, nrhs, a, offsetA, lda, b, offsetB, ldb, scalar);
    }

    /**
     * 0, or the (1-based) index of the first exactly zero diagonal element of A when it is not unit.
     */
    private static int singular(final boolean unitDiagonal, final int n, final double[] a, final int offsetA, final int lda) {
        if (!unitDiagonal) {
            for (int i = 0; i < n; i++) {
                if (a[offsetA + i + i * lda] == ZERO) {
                    return i + 1;
                }
            }
        }
        return 0;
    }

    private static <N extends Scalar<N>> int singular(final boolean unitDiagonal, final int n, final N[] a, final int offsetA, final int lda) {
        if (!unitDiagonal) {
            for (int i = 0; i < n; i++) {
                if (a[offsetA + i + i * lda].isZero()) {
                    return i + 1;
                }
            }
        }
        return 0;
    }

    /**
     * The right hand sides one at a time, each with a call to the dispatching
     * {@link TRSV#invoke(boolean, boolean, boolean, int, double[], int, int, double[], int, int)}.
     */
    private static int solveColumns(final boolean upper, final boolean transpose, final boolean unitDiagonal, final int n, final int nrhs, final double[] a,
            final int offsetA, final int lda, final double[] b, final int offsetB, final int ldb) {

        int info = TRTRS.singular(unitDiagonal, n, a, offsetA, lda);
        if (info != 0) {
            return info;
        }

        for (int j = 0; j < nrhs; j++) {
            TRSV.invoke(upper, transpose, unitDiagonal, n, a, offsetA, lda, b, offsetB + j * ldb, 1);
        }

        return 0;
    }

}

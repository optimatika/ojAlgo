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

import org.ojalgo.array.operation.ArrayOperation;
import org.ojalgo.array.operation.SCAL;
import org.ojalgo.concurrent.DivideAndConquer;
import org.ojalgo.concurrent.Parallelism;

/**
 * BLAS level 3 {@code ?trsm}: solves op(A)*X = alpha*B or X*op(A) = alpha*B for X, where A is triangular,
 * op(A) is A or A<sup>T</sup>, and X and B are m-by-n. X overwrites B.
 */
public abstract class TRSM implements BLAS3 {

    /**
     * The pure Java implementation.
     * <p>
     * Blocked, with {@link TRSM#BLOCK_SIZE} rows (left) or columns (right) of B per block: each block is
     * solved for with the triangular block on the diagonal of A, and the rest of B is updated by
     * {@link GEMM}. The blocks on the diagonal are solved for in parallel when that is large, splitting the
     * columns (left) or rows (right) of B, see {@link TRSM#THRESHOLD}.
     */
    static final class JVM {

        private static final DivideAndConquer.Divider DIVIDER = Parallelism.CORES.newDivider();

        /**
         * Solves op(A)*X = B for X, overwriting B: one block on the diagonal at a time, then the rows of B
         * that remain are updated with that block's solution.
         */
        private static void invokeLeft(final boolean upper, final boolean transposeA, final boolean unitDiagonal, final int m, final int n, final double[] a,
                final int offsetA, final int lda, final double[] b, final int offsetB, final int ldb) {

            if (upper == transposeA) {
                // op(A) is lower triangular – forward, from the first row
                for (int k = 0; k < m; k += BLOCK_SIZE) {
                    int kb = Math.min(BLOCK_SIZE, m - k);
                    JVM.solveDiagonalLeft(upper, transposeA, unitDiagonal, kb, n, a, offsetA + k + k * lda, lda, b, offsetB + k, ldb);
                    int below = m - k - kb;
                    if (below > 0) {
                        int offsetOpA = transposeA ? offsetA + k + (k + kb) * lda : offsetA + k + kb + k * lda;
                        GEMM.JVM.invoke(transposeA, false, below, n, kb, -ONE, a, offsetOpA, lda, b, offsetB + k, ldb, ONE, b, offsetB + k + kb, ldb);
                    }
                }
            } else {
                // op(A) is upper triangular – backward, from the last row
                for (int k = (m - 1) / BLOCK_SIZE * BLOCK_SIZE; k >= 0; k -= BLOCK_SIZE) {
                    int kb = Math.min(BLOCK_SIZE, m - k);
                    JVM.solveDiagonalLeft(upper, transposeA, unitDiagonal, kb, n, a, offsetA + k + k * lda, lda, b, offsetB + k, ldb);
                    if (k > 0) {
                        int offsetOpA = transposeA ? offsetA + k : offsetA + k * lda;
                        GEMM.JVM.invoke(transposeA, false, k, n, kb, -ONE, a, offsetOpA, lda, b, offsetB + k, ldb, ONE, b, offsetB, ldb);
                    }
                }
            }
        }

        /**
         * Solves X*op(A) = B for X, overwriting B: one block on the diagonal at a time, then the columns of B
         * that remain are updated with that block's solution.
         */
        private static void invokeRight(final boolean upper, final boolean transposeA, final boolean unitDiagonal, final int m, final int n, final double[] a,
                final int offsetA, final int lda, final double[] b, final int offsetB, final int ldb) {

            if (upper != transposeA) {
                // op(A) is upper triangular – forward, from the first column
                for (int k = 0; k < n; k += BLOCK_SIZE) {
                    int kb = Math.min(BLOCK_SIZE, n - k);
                    JVM.solveDiagonalRight(upper, transposeA, unitDiagonal, m, kb, a, offsetA + k + k * lda, lda, b, offsetB + k * ldb, ldb);
                    int after = n - k - kb;
                    if (after > 0) {
                        int offsetOpA = transposeA ? offsetA + k + kb + k * lda : offsetA + k + (k + kb) * lda;
                        GEMM.JVM.invoke(false, transposeA, m, after, kb, -ONE, b, offsetB + k * ldb, ldb, a, offsetOpA, lda, ONE, b, offsetB + (k + kb) * ldb,
                                ldb);
                    }
                }
            } else {
                // op(A) is lower triangular – backward, from the last column
                for (int k = (n - 1) / BLOCK_SIZE * BLOCK_SIZE; k >= 0; k -= BLOCK_SIZE) {
                    int kb = Math.min(BLOCK_SIZE, n - k);
                    JVM.solveDiagonalRight(upper, transposeA, unitDiagonal, m, kb, a, offsetA + k + k * lda, lda, b, offsetB + k * ldb, ldb);
                    if (k > 0) {
                        int offsetOpA = transposeA ? offsetA + k * lda : offsetA + k;
                        GEMM.JVM.invoke(false, transposeA, m, k, kb, -ONE, b, offsetB + k * ldb, ldb, a, offsetOpA, lda, ONE, b, offsetB, ldb);
                    }
                }
            }
        }

        /**
         * Solves op(A)*X = B, with the m-by-m A on the diagonal, for the m-by-n block X of B. A transposed A
         * is first copied, so that the kernels always read columns of the triangle.
         */
        private static void solveDiagonalLeft(final boolean upper, final boolean transposeA, final boolean unitDiagonal, final int m, final int n,
                final double[] a, final int offsetA, final int lda, final double[] b, final int offsetB, final int ldb) {

            if (transposeA) {

                double[] opA = new double[m * m];
                for (int j = 0; j < m; j++) {
                    int first = upper ? j : 0;
                    int limit = upper ? m : j + 1;
                    for (int i = first; i < limit; i++) {
                        opA[i + j * m] = a[offsetA + j + i * lda];
                    }
                }

                JVM.solveDiagonalLeft(!upper, false, unitDiagonal, m, n, opA, 0, m, b, offsetB, ldb);

            } else {

                // Half of m*m multiply-adds per column
                int chunk = MatrixOperation.chunk(THRESHOLD, m * m / 2.0);

                if (n <= chunk) {
                    if (upper) {
                        JVM.solveUpperColumns(unitDiagonal, m, 0, n, a, offsetA, lda, b, offsetB, ldb);
                    } else {
                        JVM.solveLowerColumns(unitDiagonal, m, 0, n, a, offsetA, lda, b, offsetB, ldb);
                    }
                } else if (upper) {
                    DIVIDER.threshold(chunk).divide(0, n,
                            (first, limit) -> JVM.solveUpperColumns(unitDiagonal, m, first, limit, a, offsetA, lda, b, offsetB, ldb));
                } else {
                    DIVIDER.threshold(chunk).divide(0, n,
                            (first, limit) -> JVM.solveLowerColumns(unitDiagonal, m, first, limit, a, offsetA, lda, b, offsetB, ldb));
                }
            }
        }

        /**
         * Solves X*op(A) = B, with the n-by-n A on the diagonal, for the m-by-n block X of B.
         */
        private static void solveDiagonalRight(final boolean upper, final boolean transposeA, final boolean unitDiagonal, final int m, final int n,
                final double[] a, final int offsetA, final int lda, final double[] b, final int offsetB, final int ldb) {

            // Half of n*n multiply-adds per row, counted as a quarter: sets of rows need to be larger parts than
            // sets of columns (left)
            int chunk = MatrixOperation.chunk(THRESHOLD, n * n / 8.0);

            if (m > chunk) {
                DIVIDER.threshold(chunk).divide(0, m,
                        (first, limit) -> JVM.solveRightRows(upper, transposeA, unitDiagonal, first, limit, n, a, offsetA, lda, b, offsetB, ldb));
            } else {
                JVM.solveRightRows(upper, transposeA, unitDiagonal, 0, m, n, a, offsetA, lda, b, offsetB, ldb);
            }
        }

        /**
         * Solves A*X = B, A m-by-m lower triangular and not transposed, for the columns {@code first} to
         * {@code limit - 1} of X. Four columns at a time, so that each column of A is read once for all four.
         */
        private static void solveLowerColumns(final boolean unitDiagonal, final int m, final int first, final int limit, final double[] a, final int offsetA,
                final int lda, final double[] b, final int offsetB, final int ldb) {

            int j = first;

            for (; j + 3 < limit; j += 4) {

                int colB0 = offsetB + j * ldb;
                int colB1 = colB0 + ldb;
                int colB2 = colB1 + ldb;
                int colB3 = colB2 + ldb;

                for (int k = 0; k < m; k++) {

                    int colA = offsetA + k * lda;

                    double x0 = b[colB0 + k], x1 = b[colB1 + k], x2 = b[colB2 + k], x3 = b[colB3 + k];
                    if (!unitDiagonal) {
                        double diagonal = a[colA + k];
                        b[colB0 + k] = x0 /= diagonal;
                        b[colB1 + k] = x1 /= diagonal;
                        b[colB2 + k] = x2 /= diagonal;
                        b[colB3 + k] = x3 /= diagonal;
                    }

                    for (int i = k + 1; i < m; i++) {
                        double aik = a[colA + i];
                        b[colB0 + i] = GEMM.JVM.fma(-x0, aik, b[colB0 + i]);
                        b[colB1 + i] = GEMM.JVM.fma(-x1, aik, b[colB1 + i]);
                        b[colB2 + i] = GEMM.JVM.fma(-x2, aik, b[colB2 + i]);
                        b[colB3 + i] = GEMM.JVM.fma(-x3, aik, b[colB3 + i]);
                    }
                }
            }

            for (; j < limit; j++) {

                int colB = offsetB + j * ldb;

                for (int k = 0; k < m; k++) {

                    int colA = offsetA + k * lda;

                    double x = b[colB + k];
                    if (!unitDiagonal) {
                        b[colB + k] = x /= a[colA + k];
                    }

                    for (int i = k + 1; i < m; i++) {
                        b[colB + i] = GEMM.JVM.fma(-x, a[colA + i], b[colB + i]);
                    }
                }
            }
        }

        /**
         * Solves X*op(A) = B, A n-by-n, for the rows {@code first} to {@code limit - 1} of X: one column at a
         * time, updated by {@link GEMV} with the columns already solved for.
         */
        private static void solveRightRows(final boolean upper, final boolean transposeA, final boolean unitDiagonal, final int first, final int limit,
                final int n, final double[] a, final int offsetA, final int lda, final double[] b, final int offsetB, final int ldb) {

            int m = limit - first;
            int rowB = offsetB + first;

            // op(A)(k,j) is a[offsetA + k * incK + j * incJ]
            int incK = transposeA ? lda : 1;
            int incJ = transposeA ? 1 : lda;

            if (upper != transposeA) {
                // op(A) is upper triangular – forward, from the first column
                for (int j = 0; j < n; j++) {
                    int colB = rowB + j * ldb;
                    if (j > 0) {
                        GEMV.JVM.invoke(false, m, j, -ONE, b, rowB, ldb, a, offsetA + j * incJ, incK, ONE, b, colB, 1);
                    }
                    if (!unitDiagonal) {
                        SCAL.scale(b, colB, colB + m, ONE / a[offsetA + j + j * lda]);
                    }
                }
            } else {
                // op(A) is lower triangular – backward, from the last column
                for (int j = n - 1; j >= 0; j--) {
                    int colB = rowB + j * ldb;
                    int after = n - j - 1;
                    if (after > 0) {
                        GEMV.JVM.invoke(false, m, after, -ONE, b, rowB + (j + 1) * ldb, ldb, a, offsetA + (j + 1) * incK + j * incJ, incK, ONE, b, colB, 1);
                    }
                    if (!unitDiagonal) {
                        SCAL.scale(b, colB, colB + m, ONE / a[offsetA + j + j * lda]);
                    }
                }
            }
        }

        /**
         * Solves A*X = B, A m-by-m upper triangular and not transposed, for the columns {@code first} to
         * {@code limit - 1} of X. Four columns at a time, so that each column of A is read once for all four.
         */
        private static void solveUpperColumns(final boolean unitDiagonal, final int m, final int first, final int limit, final double[] a, final int offsetA,
                final int lda, final double[] b, final int offsetB, final int ldb) {

            int j = first;

            for (; j + 3 < limit; j += 4) {

                int colB0 = offsetB + j * ldb;
                int colB1 = colB0 + ldb;
                int colB2 = colB1 + ldb;
                int colB3 = colB2 + ldb;

                for (int k = m - 1; k >= 0; k--) {

                    int colA = offsetA + k * lda;

                    double x0 = b[colB0 + k], x1 = b[colB1 + k], x2 = b[colB2 + k], x3 = b[colB3 + k];
                    if (!unitDiagonal) {
                        double diagonal = a[colA + k];
                        b[colB0 + k] = x0 /= diagonal;
                        b[colB1 + k] = x1 /= diagonal;
                        b[colB2 + k] = x2 /= diagonal;
                        b[colB3 + k] = x3 /= diagonal;
                    }

                    for (int i = 0; i < k; i++) {
                        double aik = a[colA + i];
                        b[colB0 + i] = GEMM.JVM.fma(-x0, aik, b[colB0 + i]);
                        b[colB1 + i] = GEMM.JVM.fma(-x1, aik, b[colB1 + i]);
                        b[colB2 + i] = GEMM.JVM.fma(-x2, aik, b[colB2 + i]);
                        b[colB3 + i] = GEMM.JVM.fma(-x3, aik, b[colB3 + i]);
                    }
                }
            }

            for (; j < limit; j++) {

                int colB = offsetB + j * ldb;

                for (int k = m - 1; k >= 0; k--) {

                    int colA = offsetA + k * lda;

                    double x = b[colB + k];
                    if (!unitDiagonal) {
                        b[colB + k] = x /= a[colA + k];
                    }

                    for (int i = 0; i < k; i++) {
                        b[colB + i] = GEMM.JVM.fma(-x, a[colA + i], b[colB + i]);
                    }
                }
            }

        }

        /**
         * The pure Java implementation of {@link TRSM#invoke}, without argument checks, for m, n > 0.
         */
        static void invoke(final boolean left, final boolean upper, final boolean transposeA, final boolean unitDiagonal, final int m, final int n,
                final double alpha, final double[] a, final int offsetA, final int lda, final double[] b, final int offsetB, final int ldb) {

            if (alpha != ONE) {
                for (int j = 0; j < n; j++) {
                    int colB = offsetB + j * ldb;
                    SCAL.scale(b, colB, colB + m, alpha);
                }
                if (alpha == ZERO) {
                    return;
                }
            }

            if (left) {
                JVM.invokeLeft(upper, transposeA, unitDiagonal, m, n, a, offsetA, lda, b, offsetB, ldb);
            } else {
                JVM.invokeRight(upper, transposeA, unitDiagonal, m, n, a, offsetA, lda, b, offsetB, ldb);
            }
        }

    }

    /**
     * The number of rows (left) or columns (right) of B in each block of the Java implementation.
     */
    static int BLOCK_SIZE = 256;

    /**
     * The Java implementation solves for each block on the diagonal in parallel, splitting the columns (left)
     * or rows (right) of B, when the number of multiply-adds is larger than this cubed. Each part then gets
     * more than half of that.
     */
    static int THRESHOLD = 48;

    /**
     * {@code dtrsm}: solves op(A)*X = alpha*B (left) or X*op(A) = alpha*B (right), X overwrites B
     * <p>
     * All matrices are column-major, stored in their arrays from an offset with a leading dimension. Only the
     * upper or lower triangle of A is read, and not its diagonal if unit. No test for singularity is done.
     * When alpha is 0, A is not read and B need not be set on entry.
     *
     * @param left         op(A) is on the left (m-by-m) if true, otherwise on the right (n-by-n)
     * @param upper        A is upper triangular if true, otherwise lower
     * @param transposeA   op(A) = A<sup>T</sup> if true, otherwise A
     * @param unitDiagonal A has a unit diagonal (assumed, not read) if true
     * @param m            The number of rows of B
     * @param n            The number of columns of B
     */
    public static void invoke(final boolean left, final boolean upper, final boolean transposeA, final boolean unitDiagonal, final int m, final int n,
            final double alpha, final double[] a, final int offsetA, final int lda, final double[] b, final int offsetB, final int ldb) {

        ArrayOperation.checkArgument("TRSM", "m", m >= 0);
        ArrayOperation.checkArgument("TRSM", "n", n >= 0);
        ArrayOperation.checkArgument("TRSM", "lda", lda >= Math.max(1, left ? m : n));
        ArrayOperation.checkArgument("TRSM", "ldb", ldb >= Math.max(1, m));

        if (m == 0 || n == 0) {
            return;
        }

        JVM.invoke(left, upper, transposeA, unitDiagonal, m, n, alpha, a, offsetA, lda, b, offsetB, ldb);
    }

}

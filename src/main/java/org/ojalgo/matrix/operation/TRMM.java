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
 * BLAS level 3 {@code ?trmm}: B := alpha*op(A)*B or B := alpha*B*op(A), where A is triangular, op(A) is A or
 * A<sup>T</sup>, and B is m-by-n.
 */
public abstract class TRMM implements BLAS3 {

    /**
     * The pure Java implementation.
     * <p>
     * Blocked, with {@link TRMM#BLOCK_SIZE} rows (left) or columns (right) of B per block: each block of B
     * first contributes to the rest of B, by {@link GEMM}, and is then multiplied by the triangular block on
     * the diagonal of A. The blocks on the diagonal are multiplied in parallel when that is large, splitting
     * the columns (left) or rows (right) of B, see {@link TRMM#THRESHOLD}.
     */
    static final class JVM {

        private static final DivideAndConquer.Divider DIVIDER = Parallelism.CORES.newDivider();

        /**
         * B := op(A)*B, one block of rows at a time, in the order that leaves the rows still to be read
         * unchanged.
         */
        private static void invokeLeft(final boolean upper, final boolean transposeA, final boolean unitDiagonal, final int m, final int n, final double[] a,
                final int offsetA, final int lda, final double[] b, final int offsetB, final int ldb) {

            if (upper != transposeA) {
                // op(A) is upper triangular – from the first row
                for (int k = 0; k < m; k += BLOCK_SIZE) {
                    int kb = Math.min(BLOCK_SIZE, m - k);
                    if (k > 0) {
                        int offsetOpA = transposeA ? offsetA + k : offsetA + k * lda;
                        GEMM.JVM.invoke(transposeA, false, k, n, kb, ONE, a, offsetOpA, lda, b, offsetB + k, ldb, ONE, b, offsetB, ldb);
                    }
                    JVM.multiplyDiagonalLeft(upper, transposeA, unitDiagonal, kb, n, a, offsetA + k + k * lda, lda, b, offsetB + k, ldb);
                }
            } else {
                // op(A) is lower triangular – from the last row
                for (int k = (m - 1) / BLOCK_SIZE * BLOCK_SIZE; k >= 0; k -= BLOCK_SIZE) {
                    int kb = Math.min(BLOCK_SIZE, m - k);
                    int below = m - k - kb;
                    if (below > 0) {
                        int offsetOpA = transposeA ? offsetA + k + (k + kb) * lda : offsetA + k + kb + k * lda;
                        GEMM.JVM.invoke(transposeA, false, below, n, kb, ONE, a, offsetOpA, lda, b, offsetB + k, ldb, ONE, b, offsetB + k + kb, ldb);
                    }
                    JVM.multiplyDiagonalLeft(upper, transposeA, unitDiagonal, kb, n, a, offsetA + k + k * lda, lda, b, offsetB + k, ldb);
                }
            }
        }

        /**
         * B := B*op(A), one block of columns at a time, in the order that leaves the columns still to be read
         * unchanged.
         */
        private static void invokeRight(final boolean upper, final boolean transposeA, final boolean unitDiagonal, final int m, final int n, final double[] a,
                final int offsetA, final int lda, final double[] b, final int offsetB, final int ldb) {

            if (upper != transposeA) {
                // op(A) is upper triangular – from the last column
                for (int k = (n - 1) / BLOCK_SIZE * BLOCK_SIZE; k >= 0; k -= BLOCK_SIZE) {
                    int kb = Math.min(BLOCK_SIZE, n - k);
                    int after = n - k - kb;
                    if (after > 0) {
                        int offsetOpA = transposeA ? offsetA + k + kb + k * lda : offsetA + k + (k + kb) * lda;
                        GEMM.JVM.invoke(false, transposeA, m, after, kb, ONE, b, offsetB + k * ldb, ldb, a, offsetOpA, lda, ONE, b, offsetB + (k + kb) * ldb,
                                ldb);
                    }
                    JVM.multiplyDiagonalRight(upper, transposeA, unitDiagonal, m, kb, a, offsetA + k + k * lda, lda, b, offsetB + k * ldb, ldb);
                }
            } else {
                // op(A) is lower triangular – from the first column
                for (int k = 0; k < n; k += BLOCK_SIZE) {
                    int kb = Math.min(BLOCK_SIZE, n - k);
                    if (k > 0) {
                        int offsetOpA = transposeA ? offsetA + k * lda : offsetA + k;
                        GEMM.JVM.invoke(false, transposeA, m, k, kb, ONE, b, offsetB + k * ldb, ldb, a, offsetOpA, lda, ONE, b, offsetB, ldb);
                    }
                    JVM.multiplyDiagonalRight(upper, transposeA, unitDiagonal, m, kb, a, offsetA + k + k * lda, lda, b, offsetB + k * ldb, ldb);
                }
            }
        }

        /**
         * B := op(A)*B, with the m-by-m A on the diagonal and the m-by-n block B. A transposed A is first
         * copied, so that the kernels always read columns of the triangle.
         */
        private static void multiplyDiagonalLeft(final boolean upper, final boolean transposeA, final boolean unitDiagonal, final int m, final int n,
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

                JVM.multiplyDiagonalLeft(!upper, false, unitDiagonal, m, n, opA, 0, m, b, offsetB, ldb);

            } else {

                // Half of m*m multiply-adds per column
                int chunk = MatrixOperation.chunk(THRESHOLD, m * m / 2.0);

                if (n <= chunk) {
                    if (upper) {
                        JVM.multiplyUpperColumns(unitDiagonal, m, 0, n, a, offsetA, lda, b, offsetB, ldb);
                    } else {
                        JVM.multiplyLowerColumns(unitDiagonal, m, 0, n, a, offsetA, lda, b, offsetB, ldb);
                    }
                } else if (upper) {
                    DIVIDER.threshold(chunk).divide(0, n,
                            (first, limit) -> JVM.multiplyUpperColumns(unitDiagonal, m, first, limit, a, offsetA, lda, b, offsetB, ldb));
                } else {
                    DIVIDER.threshold(chunk).divide(0, n,
                            (first, limit) -> JVM.multiplyLowerColumns(unitDiagonal, m, first, limit, a, offsetA, lda, b, offsetB, ldb));
                }
            }
        }

        /**
         * B := B*op(A), with the n-by-n A on the diagonal and the m-by-n block B.
         */
        private static void multiplyDiagonalRight(final boolean upper, final boolean transposeA, final boolean unitDiagonal, final int m, final int n,
                final double[] a, final int offsetA, final int lda, final double[] b, final int offsetB, final int ldb) {

            // Half of n*n multiply-adds per row, counted as a quarter: sets of rows need to be larger parts than
            // sets of columns (left)
            int chunk = MatrixOperation.chunk(THRESHOLD, n * n / 8.0);

            if (m > chunk) {
                DIVIDER.threshold(chunk).divide(0, m,
                        (first, limit) -> JVM.multiplyRightRows(upper, transposeA, unitDiagonal, first, limit, n, a, offsetA, lda, b, offsetB, ldb));
            } else {
                JVM.multiplyRightRows(upper, transposeA, unitDiagonal, 0, m, n, a, offsetA, lda, b, offsetB, ldb);
            }
        }

        /**
         * B := A*B, A m-by-m lower triangular and not transposed, for the columns {@code first} to
         * {@code limit - 1} of B. Four columns at a time, so that each column of A is read once for all four.
         */
        private static void multiplyLowerColumns(final boolean unitDiagonal, final int m, final int first, final int limit, final double[] a, final int offsetA,
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
                        b[colB0 + k] = x0 * diagonal;
                        b[colB1 + k] = x1 * diagonal;
                        b[colB2 + k] = x2 * diagonal;
                        b[colB3 + k] = x3 * diagonal;
                    }

                    for (int i = k + 1; i < m; i++) {
                        double aik = a[colA + i];
                        b[colB0 + i] = GEMM.JVM.fma(x0, aik, b[colB0 + i]);
                        b[colB1 + i] = GEMM.JVM.fma(x1, aik, b[colB1 + i]);
                        b[colB2 + i] = GEMM.JVM.fma(x2, aik, b[colB2 + i]);
                        b[colB3 + i] = GEMM.JVM.fma(x3, aik, b[colB3 + i]);
                    }
                }
            }

            for (; j < limit; j++) {

                int colB = offsetB + j * ldb;

                for (int k = m - 1; k >= 0; k--) {

                    int colA = offsetA + k * lda;

                    double x = b[colB + k];

                    if (!unitDiagonal) {
                        b[colB + k] = x * a[colA + k];
                    }

                    for (int i = k + 1; i < m; i++) {
                        b[colB + i] = GEMM.JVM.fma(x, a[colA + i], b[colB + i]);
                    }
                }
            }
        }

        /**
         * B := B*op(A), A n-by-n, for the rows {@code first} to {@code limit - 1} of B: one column at a time,
         * in the order that leaves the columns still to be read unchanged, updated by {@link GEMV}.
         */
        private static void multiplyRightRows(final boolean upper, final boolean transposeA, final boolean unitDiagonal, final int first, final int limit,
                final int n, final double[] a, final int offsetA, final int lda, final double[] b, final int offsetB, final int ldb) {

            int m = limit - first;
            int rowB = offsetB + first;

            // op(A)(k,j) is a[offsetA + k * incK + j * incJ]
            int incK = transposeA ? lda : 1;
            int incJ = transposeA ? 1 : lda;

            if (upper != transposeA) {
                // op(A) is upper triangular – from the last column
                for (int j = n - 1; j >= 0; j--) {
                    int colB = rowB + j * ldb;
                    if (!unitDiagonal) {
                        SCAL.scale(b, colB, colB + m, a[offsetA + j + j * lda]);
                    }
                    if (j > 0) {
                        GEMV.JVM.invoke(false, m, j, ONE, b, rowB, ldb, a, offsetA + j * incJ, incK, ONE, b, colB, 1);
                    }
                }
            } else {
                // op(A) is lower triangular – from the first column
                for (int j = 0; j < n; j++) {
                    int colB = rowB + j * ldb;
                    if (!unitDiagonal) {
                        SCAL.scale(b, colB, colB + m, a[offsetA + j + j * lda]);
                    }
                    int after = n - j - 1;
                    if (after > 0) {
                        GEMV.JVM.invoke(false, m, after, ONE, b, rowB + (j + 1) * ldb, ldb, a, offsetA + (j + 1) * incK + j * incJ, incK, ONE, b, colB, 1);
                    }
                }
            }
        }

        /**
         * B := A*B, A m-by-m upper triangular and not transposed, for the columns {@code first} to
         * {@code limit - 1} of B. Four columns at a time, so that each column of A is read once for all four.
         */
        private static void multiplyUpperColumns(final boolean unitDiagonal, final int m, final int first, final int limit, final double[] a, final int offsetA,
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

                    for (int i = 0; i < k; i++) {
                        double aik = a[colA + i];
                        b[colB0 + i] = GEMM.JVM.fma(x0, aik, b[colB0 + i]);
                        b[colB1 + i] = GEMM.JVM.fma(x1, aik, b[colB1 + i]);
                        b[colB2 + i] = GEMM.JVM.fma(x2, aik, b[colB2 + i]);
                        b[colB3 + i] = GEMM.JVM.fma(x3, aik, b[colB3 + i]);
                    }

                    if (!unitDiagonal) {
                        double diagonal = a[colA + k];
                        b[colB0 + k] = x0 * diagonal;
                        b[colB1 + k] = x1 * diagonal;
                        b[colB2 + k] = x2 * diagonal;
                        b[colB3 + k] = x3 * diagonal;
                    }
                }
            }

            for (; j < limit; j++) {

                int colB = offsetB + j * ldb;

                for (int k = 0; k < m; k++) {

                    int colA = offsetA + k * lda;

                    double x = b[colB + k];

                    for (int i = 0; i < k; i++) {
                        b[colB + i] = GEMM.JVM.fma(x, a[colA + i], b[colB + i]);
                    }

                    if (!unitDiagonal) {
                        b[colB + k] = x * a[colA + k];
                    }
                }
            }

        }

        /**
         * The pure Java implementation of {@link TRMM#invoke}, without argument checks, for m, n > 0.
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
     * The Java implementation multiplies each block on the diagonal in parallel, splitting the columns (left)
     * or rows (right) of B, when the number of multiply-adds is larger than this cubed. Each part then gets
     * more than half of that.
     */
    static int THRESHOLD = 48;

    /**
     * {@code dtrmm}: B := alpha*op(A)*B (left) or B := alpha*B*op(A) (right)
     * <p>
     * All matrices are column-major, stored in their arrays from an offset with a leading dimension. Only the
     * upper or lower triangle of A is read, and not its diagonal if unit. When alpha is 0, A is not read and
     * B need not be set on entry.
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

        ArrayOperation.checkArgument("TRMM", "m", m >= 0);
        ArrayOperation.checkArgument("TRMM", "n", n >= 0);
        ArrayOperation.checkArgument("TRMM", "lda", lda >= Math.max(1, left ? m : n));
        ArrayOperation.checkArgument("TRMM", "ldb", ldb >= Math.max(1, m));

        if (m == 0 || n == 0) {
            return;
        }

        JVM.invoke(left, upper, transposeA, unitDiagonal, m, n, alpha, a, offsetA, lda, b, offsetB, ldb);
    }

}

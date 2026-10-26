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

import java.lang.management.ManagementFactory;

import org.ojalgo.array.operation.ArrayOperation;
import org.ojalgo.array.operation.DOT;
import org.ojalgo.array.operation.SCAL;
import org.ojalgo.concurrent.DivideAndConquer;
import org.ojalgo.concurrent.Parallelism;
import org.ojalgo.machine.Hardware;

import com.sun.management.HotSpotDiagnosticMXBean;

/**
 * BLAS level 3 {@code ?gemm}: C := alpha*op(A)*op(B) + beta*C, where op(X) is X or X<sup>T</sup>, op(A) is
 * m-by-k, op(B) is k-by-n and C is m-by-n.
 */
public abstract class GEMM implements BLAS3 {

    /**
     * The pure Java implementation.
     */
    static final class JVM {

        private static final DivideAndConquer.Divider DIVIDER = Parallelism.CORES.newDivider();
        /**
         * Whether {@link Math#fma(double, double, double)} is a hardware instruction. Without it, it is
         * computed in software and is very slow.
         */
        private static final boolean FMA = JVM.isFmaAvailable();

        /**
         * C(i,j) := alpha*product + beta*C(i,j), where beta 0 means C(i,j) is not read.
         */
        private static void assign(final double[] c, final int index, final double alpha, final double product, final double beta) {
            c[index] = beta == ZERO ? alpha * product : alpha * product + beta * c[index];
        }

        /**
         * C := alpha*op(A)*op(B) + beta*C with cache blocking: op(A) is split into blocks of
         * {@link GEMM#BLOCK_SIZE} rows and columns, and all columns of C are swept through each block before
         * the next, so the block is reused from cache instead of being read from memory again for each group
         * of columns. The columns are split over threads within each block. Requires alpha != 0 and k > 0.
         */
        private static void invokeBlocked(final boolean transposeA, final boolean transposeB, final int m, final int n, final int k, final double alpha,
                final double[] a, final int offsetA, final int lda, final double[] b, final int offsetB, final int ldb, final double beta, final double[] c,
                final int offsetC, final int ldc) {

            int strideL = transposeB ? ldb : 1;
            int blockSize = BLOCK_SIZE;

            for (int l0 = 0; l0 < k; l0 += blockSize) {

                int blockK = Math.min(blockSize, k - l0);
                double blockBeta = l0 == 0 ? beta : ONE;
                int blockOffsetB = offsetB + l0 * strideL;

                for (int i0 = 0; i0 < m; i0 += blockSize) {

                    int blockM = Math.min(blockSize, m - i0);
                    int blockOffsetA = transposeA ? offsetA + l0 + i0 * lda : offsetA + i0 + l0 * lda;
                    int blockOffsetC = offsetC + i0;

                    int chunk = MatrixOperation.chunk(THRESHOLD, blockM, blockK);

                    if (n > chunk) {
                        DIVIDER.threshold(chunk).divide(0, n, (first, limit) -> JVM.invokeColumns(transposeA, transposeB, blockM, first, limit, blockK, alpha,
                                a, blockOffsetA, lda, b, blockOffsetB, ldb, blockBeta, c, blockOffsetC, ldc));
                    } else {
                        JVM.invokeColumns(transposeA, transposeB, blockM, 0, n, blockK, alpha, a, blockOffsetA, lda, b, blockOffsetB, ldb, blockBeta, c,
                                blockOffsetC, ldc);
                    }
                }
            }
        }

        /**
         * C := alpha*A*op(B) + beta*C for the columns {@code firstColumn} to {@code columnLimit - 1} of C,
         * where element (l, j) of op(B) is {@code b[offsetB + l * strideL + j * strideJ]}: (1, ldb) for B and
         * (ldb, 1) for B<sup>T</sup>. Requires alpha != 0 and k > 0.
         * <p>
         * Builds the columns of C by AXPY, 4 columns of C from 4 columns of A in each pass over the rows.
         */
        private static void invokeN(final int m, final int firstColumn, final int columnLimit, final int k, final double alpha, final double[] a,
                final int offsetA, final int lda, final double[] b, final int offsetB, final int strideL, final int strideJ, final double beta,
                final double[] c, final int offsetC, final int ldc) {

            int k4 = k - k % 4;
            int strideL2 = strideL + strideL;
            int strideL3 = strideL2 + strideL;

            int j = firstColumn;
            for (; j + 3 < columnLimit; j += 4) {

                int colB0 = offsetB + j * strideJ;
                int colB1 = colB0 + strideJ;
                int colB2 = colB1 + strideJ;
                int colB3 = colB2 + strideJ;

                int colC0 = offsetC + j * ldc;
                int colC1 = colC0 + ldc;
                int colC2 = colC1 + ldc;
                int colC3 = colC2 + ldc;

                SCAL.scale(c, colC0, colC0 + m, beta);
                SCAL.scale(c, colC1, colC1 + m, beta);
                SCAL.scale(c, colC2, colC2 + m, beta);
                SCAL.scale(c, colC3, colC3 + m, beta);

                int l = 0;
                for (; l < k4; l += 4) {

                    int rowB = l * strideL;
                    double b00 = alpha * b[colB0 + rowB], b10 = alpha * b[colB0 + rowB + strideL];
                    double b20 = alpha * b[colB0 + rowB + strideL2], b30 = alpha * b[colB0 + rowB + strideL3];
                    double b01 = alpha * b[colB1 + rowB], b11 = alpha * b[colB1 + rowB + strideL];
                    double b21 = alpha * b[colB1 + rowB + strideL2], b31 = alpha * b[colB1 + rowB + strideL3];
                    double b02 = alpha * b[colB2 + rowB], b12 = alpha * b[colB2 + rowB + strideL];
                    double b22 = alpha * b[colB2 + rowB + strideL2], b32 = alpha * b[colB2 + rowB + strideL3];
                    double b03 = alpha * b[colB3 + rowB], b13 = alpha * b[colB3 + rowB + strideL];
                    double b23 = alpha * b[colB3 + rowB + strideL2], b33 = alpha * b[colB3 + rowB + strideL3];

                    int colA0 = offsetA + l * lda;
                    int colA1 = colA0 + lda;
                    int colA2 = colA1 + lda;
                    int colA3 = colA2 + lda;

                    for (int i = 0; i < m; i++) {
                        double a0 = a[colA0 + i];
                        double a1 = a[colA1 + i];
                        double a2 = a[colA2 + i];
                        double a3 = a[colA3 + i];
                        c[colC0 + i] = JVM.fma(b30, a3, JVM.fma(b20, a2, JVM.fma(b10, a1, JVM.fma(b00, a0, c[colC0 + i]))));
                        c[colC1 + i] = JVM.fma(b31, a3, JVM.fma(b21, a2, JVM.fma(b11, a1, JVM.fma(b01, a0, c[colC1 + i]))));
                        c[colC2 + i] = JVM.fma(b32, a3, JVM.fma(b22, a2, JVM.fma(b12, a1, JVM.fma(b02, a0, c[colC2 + i]))));
                        c[colC3 + i] = JVM.fma(b33, a3, JVM.fma(b23, a2, JVM.fma(b13, a1, JVM.fma(b03, a0, c[colC3 + i]))));
                    }
                }

                // The 1 to 3 columns of A that remain, in a single pass over the rows
                int rowB = k4 * strideL;
                int colA0 = offsetA + k4 * lda;
                int colA1 = colA0 + lda;
                int colA2 = colA1 + lda;

                switch (k - k4) {
                    case 3: {
                        double b00 = alpha * b[colB0 + rowB], b10 = alpha * b[colB0 + rowB + strideL], b20 = alpha * b[colB0 + rowB + strideL2];
                        double b01 = alpha * b[colB1 + rowB], b11 = alpha * b[colB1 + rowB + strideL], b21 = alpha * b[colB1 + rowB + strideL2];
                        double b02 = alpha * b[colB2 + rowB], b12 = alpha * b[colB2 + rowB + strideL], b22 = alpha * b[colB2 + rowB + strideL2];
                        double b03 = alpha * b[colB3 + rowB], b13 = alpha * b[colB3 + rowB + strideL], b23 = alpha * b[colB3 + rowB + strideL2];
                        for (int i = 0; i < m; i++) {
                            double a0 = a[colA0 + i];
                            double a1 = a[colA1 + i];
                            double a2 = a[colA2 + i];
                            c[colC0 + i] = JVM.fma(b20, a2, JVM.fma(b10, a1, JVM.fma(b00, a0, c[colC0 + i])));
                            c[colC1 + i] = JVM.fma(b21, a2, JVM.fma(b11, a1, JVM.fma(b01, a0, c[colC1 + i])));
                            c[colC2 + i] = JVM.fma(b22, a2, JVM.fma(b12, a1, JVM.fma(b02, a0, c[colC2 + i])));
                            c[colC3 + i] = JVM.fma(b23, a2, JVM.fma(b13, a1, JVM.fma(b03, a0, c[colC3 + i])));
                        }
                        break;
                    }
                    case 2: {
                        double b00 = alpha * b[colB0 + rowB], b10 = alpha * b[colB0 + rowB + strideL];
                        double b01 = alpha * b[colB1 + rowB], b11 = alpha * b[colB1 + rowB + strideL];
                        double b02 = alpha * b[colB2 + rowB], b12 = alpha * b[colB2 + rowB + strideL];
                        double b03 = alpha * b[colB3 + rowB], b13 = alpha * b[colB3 + rowB + strideL];
                        for (int i = 0; i < m; i++) {
                            double a0 = a[colA0 + i];
                            double a1 = a[colA1 + i];
                            c[colC0 + i] = JVM.fma(b10, a1, JVM.fma(b00, a0, c[colC0 + i]));
                            c[colC1 + i] = JVM.fma(b11, a1, JVM.fma(b01, a0, c[colC1 + i]));
                            c[colC2 + i] = JVM.fma(b12, a1, JVM.fma(b02, a0, c[colC2 + i]));
                            c[colC3 + i] = JVM.fma(b13, a1, JVM.fma(b03, a0, c[colC3 + i]));
                        }
                        break;
                    }
                    case 1: {
                        double b0 = alpha * b[colB0 + rowB];
                        double b1 = alpha * b[colB1 + rowB];
                        double b2 = alpha * b[colB2 + rowB];
                        double b3 = alpha * b[colB3 + rowB];
                        for (int i = 0; i < m; i++) {
                            double a0 = a[colA0 + i];
                            c[colC0 + i] = JVM.fma(b0, a0, c[colC0 + i]);
                            c[colC1 + i] = JVM.fma(b1, a0, c[colC1 + i]);
                            c[colC2 + i] = JVM.fma(b2, a0, c[colC2 + i]);
                            c[colC3 + i] = JVM.fma(b3, a0, c[colC3 + i]);
                        }
                        break;
                    }
                    default: {
                        // k is a multiple of 4
                        break;
                    }
                }
            }

            for (; j < columnLimit; j++) {

                int colB = offsetB + j * strideJ;
                int colC = offsetC + j * ldc;

                SCAL.scale(c, colC, colC + m, beta);

                int l = 0;
                for (; l < k4; l += 4) {

                    int rowB = l * strideL;
                    double b0 = alpha * b[colB + rowB];
                    double b1 = alpha * b[colB + rowB + strideL];
                    double b2 = alpha * b[colB + rowB + strideL2];
                    double b3 = alpha * b[colB + rowB + strideL3];

                    int colA0 = offsetA + l * lda;
                    int colA1 = colA0 + lda;
                    int colA2 = colA1 + lda;
                    int colA3 = colA2 + lda;

                    for (int i = 0; i < m; i++) {
                        c[colC + i] = JVM.fma(b3, a[colA3 + i], JVM.fma(b2, a[colA2 + i], JVM.fma(b1, a[colA1 + i], JVM.fma(b0, a[colA0 + i], c[colC + i]))));
                    }
                }

                int rowB = k4 * strideL;
                int colA0 = offsetA + k4 * lda;
                int colA1 = colA0 + lda;
                int colA2 = colA1 + lda;

                switch (k - k4) {
                    case 3: {
                        double b0 = alpha * b[colB + rowB];
                        double b1 = alpha * b[colB + rowB + strideL];
                        double b2 = alpha * b[colB + rowB + strideL2];
                        for (int i = 0; i < m; i++) {
                            c[colC + i] = JVM.fma(b2, a[colA2 + i], JVM.fma(b1, a[colA1 + i], JVM.fma(b0, a[colA0 + i], c[colC + i])));
                        }
                        break;
                    }
                    case 2: {
                        double b0 = alpha * b[colB + rowB];
                        double b1 = alpha * b[colB + rowB + strideL];
                        for (int i = 0; i < m; i++) {
                            c[colC + i] = JVM.fma(b1, a[colA1 + i], JVM.fma(b0, a[colA0 + i], c[colC + i]));
                        }
                        break;
                    }
                    case 1: {
                        double b0 = alpha * b[colB + rowB];
                        for (int i = 0; i < m; i++) {
                            c[colC + i] = JVM.fma(b0, a[colA0 + i], c[colC + i]);
                        }
                        break;
                    }
                    default: {
                        // k is a multiple of 4
                        break;
                    }
                }
            }
        }

        /**
         * Asks HotSpot whether it uses FMA instructions. If it can't be asked, FMA is assumed on ARM64 and
         * x86-64.
         */
        private static boolean isFmaAvailable() {
            try {
                HotSpotDiagnosticMXBean diagnostics = ManagementFactory.getPlatformMXBean(HotSpotDiagnosticMXBean.class);
                if (diagnostics != null) {
                    return Boolean.parseBoolean(diagnostics.getVMOption("UseFMA").getValue());
                }
            } catch (RuntimeException | LinkageError ignore) {
                // Not HotSpot, or no such option
            }
            Hardware.Architecture architecture = Hardware.getArchitecture();
            return architecture == Hardware.Architecture.AARCH64 || architecture == Hardware.Architecture.X86_64;
        }

        /**
         * x*y + z: {@link Math#fma(double, double, double)} when that is a hardware instruction, otherwise a
         * multiplication and an addition. {@link #FMA} is constant, so the choice is made once by the
         * compiler.
         */
        static double fma(final double x, final double y, final double z) {
            return FMA ? Math.fma(x, y, z) : z + x * y;
        }

        /**
         * The pure Java implementation of {@link GEMM#invoke}, without argument checks. Multi-threaded,
         * splitting the columns of C, when m*n*k is larger than {@link GEMM#THRESHOLD} cubed. Cache blocked
         * when A is large, see {@link GEMM#BLOCK_THRESHOLD}. When C is a vector (m or n is 1) this is
         * {@link GEMV}, and when k is 1 it is {@link GER}.
         */
        static void invoke(final boolean transposeA, final boolean transposeB, final int m, final int n, final int k, final double alpha, final double[] a,
                final int offsetA, final int lda, final double[] b, final int offsetB, final int ldb, final double beta, final double[] c, final int offsetC,
                final int ldc) {

            if (alpha != ZERO && k > 0 && (m == 1 || n == 1 || k == 1)) {
                JVM.invokeDegenerate(transposeA, transposeB, m, n, k, alpha, a, offsetA, lda, b, offsetB, ldb, beta, c, offsetC, ldc);
            } else if (alpha != ZERO && m > BLOCK_THRESHOLD && k > BLOCK_THRESHOLD) {
                JVM.invokeBlocked(transposeA, transposeB, m, n, k, alpha, a, offsetA, lda, b, offsetB, ldb, beta, c, offsetC, ldc);
            } else {
                int threshold = MatrixOperation.chunk(THRESHOLD, m, k);
                if (n > threshold) {
                    DIVIDER.threshold(threshold).divide(0, n, (first, limit) -> JVM.invokeColumns(transposeA, transposeB, m, first, limit, k, alpha, a, offsetA,
                            lda, b, offsetB, ldb, beta, c, offsetC, ldc));
                } else {
                    JVM.invokeColumns(transposeA, transposeB, m, 0, n, k, alpha, a, offsetA, lda, b, offsetB, ldb, beta, c, offsetC, ldc);
                }
            }
        }

        /**
         * The columns {@code firstColumn} to {@code columnLimit - 1} of C, picking the kernel for the
         * transpose combination.
         */
        static void invokeColumns(final boolean transposeA, final boolean transposeB, final int m, final int firstColumn, final int columnLimit, final int k,
                final double alpha, final double[] a, final int offsetA, final int lda, final double[] b, final int offsetB, final int ldb, final double beta,
                final double[] c, final int offsetC, final int ldc) {

            if (alpha == ZERO || k == 0) {
                for (int j = firstColumn; j < columnLimit; j++) {
                    int colC = offsetC + j * ldc;
                    SCAL.scale(c, colC, colC + m, beta);
                }
            } else if (!transposeA) {
                if (!transposeB) {
                    JVM.invokeNN(m, firstColumn, columnLimit, k, alpha, a, offsetA, lda, b, offsetB, ldb, beta, c, offsetC, ldc);
                } else {
                    JVM.invokeNT(m, firstColumn, columnLimit, k, alpha, a, offsetA, lda, b, offsetB, ldb, beta, c, offsetC, ldc);
                }
            } else {
                if (!transposeB) {
                    JVM.invokeTN(m, firstColumn, columnLimit, k, alpha, a, offsetA, lda, b, offsetB, ldb, beta, c, offsetC, ldc);
                } else {
                    JVM.invokeTT(m, firstColumn, columnLimit, k, alpha, a, offsetA, lda, b, offsetB, ldb, beta, c, offsetC, ldc);
                }
            }
        }

        /**
         * The shapes that are level 2 operations, for alpha != 0 and k > 0. When n is 1, C is y =
         * alpha*op(A)*x + beta*y with x the column of op(B). When m is 1, C<sup>T</sup> is y =
         * alpha*op(B)<sup>T</sup>*x + beta*y with x the row of op(A). When k is 1, C is scaled by beta and
         * then gets the rank-1 update alpha*x*y<sup>T</sup> with x the column of op(A) and y the row of
         * op(B).
         */
        static void invokeDegenerate(final boolean transposeA, final boolean transposeB, final int m, final int n, final int k, final double alpha,
                final double[] a, final int offsetA, final int lda, final double[] b, final int offsetB, final int ldb, final double beta, final double[] c,
                final int offsetC, final int ldc) {

            if (n == 1) {

                int incX = transposeB ? ldb : 1;

                if (transposeA) {
                    GEMV.JVM.invoke(true, k, m, alpha, a, offsetA, lda, b, offsetB, incX, beta, c, offsetC, 1);
                } else {
                    GEMV.JVM.invoke(false, m, k, alpha, a, offsetA, lda, b, offsetB, incX, beta, c, offsetC, 1);
                }

            } else if (m == 1) {

                int incX = transposeA ? 1 : lda;

                if (transposeB) {
                    GEMV.JVM.invoke(false, n, k, alpha, b, offsetB, ldb, a, offsetA, incX, beta, c, offsetC, ldc);
                } else {
                    GEMV.JVM.invoke(true, k, n, alpha, b, offsetB, ldb, a, offsetA, incX, beta, c, offsetC, ldc);
                }

            } else {

                for (int j = 0; j < n; j++) {
                    int colC = offsetC + j * ldc;
                    SCAL.scale(c, colC, colC + m, beta);
                }

                GER.JVM.invoke(m, n, alpha, a, offsetA, transposeA ? lda : 1, b, offsetB, transposeB ? 1 : ldb, c, offsetC, ldc);
            }
        }

        /**
         * C := alpha*A*B + beta*C for the columns {@code firstColumn} to {@code columnLimit - 1} of C.
         * Requires alpha != 0 and k > 0.
         */
        static void invokeNN(final int m, final int firstColumn, final int columnLimit, final int k, final double alpha, final double[] a, final int offsetA,
                final int lda, final double[] b, final int offsetB, final int ldb, final double beta, final double[] c, final int offsetC, final int ldc) {
            JVM.invokeN(m, firstColumn, columnLimit, k, alpha, a, offsetA, lda, b, offsetB, 1, ldb, beta, c, offsetC, ldc);
        }

        /**
         * C := alpha*A*B<sup>T</sup> + beta*C for the columns {@code firstColumn} to {@code columnLimit - 1}
         * of C. Requires alpha != 0 and k > 0.
         */
        static void invokeNT(final int m, final int firstColumn, final int columnLimit, final int k, final double alpha, final double[] a, final int offsetA,
                final int lda, final double[] b, final int offsetB, final int ldb, final double beta, final double[] c, final int offsetC, final int ldc) {
            JVM.invokeN(m, firstColumn, columnLimit, k, alpha, a, offsetA, lda, b, offsetB, ldb, 1, beta, c, offsetC, ldc);
        }

        /**
         * C := alpha*A<sup>T</sup>*B + beta*C for the columns {@code firstColumn} to {@code columnLimit - 1}
         * of C. Requires alpha != 0 and k > 0. Each 4x4 block of C is accumulated in registers, reading 4
         * columns of A and 4 columns of B along k. Leftover rows and columns use {@link DOT}.
         */
        static void invokeTN(final int m, final int firstColumn, final int columnLimit, final int k, final double alpha, final double[] a, final int offsetA,
                final int lda, final double[] b, final int offsetB, final int ldb, final double beta, final double[] c, final int offsetC, final int ldc) {

            int m4 = m - m % 4;

            int j = firstColumn;
            for (; j + 3 < columnLimit; j += 4) {

                int colB0 = offsetB + j * ldb;
                int colB1 = colB0 + ldb;
                int colB2 = colB1 + ldb;
                int colB3 = colB2 + ldb;

                int colC0 = offsetC + j * ldc;
                int colC1 = colC0 + ldc;
                int colC2 = colC1 + ldc;
                int colC3 = colC2 + ldc;

                for (int i = 0; i < m4; i += 4) {

                    int colA0 = offsetA + i * lda;
                    int colA1 = colA0 + lda;
                    int colA2 = colA1 + lda;
                    int colA3 = colA2 + lda;

                    double c00 = ZERO, c10 = ZERO, c20 = ZERO, c30 = ZERO;
                    double c01 = ZERO, c11 = ZERO, c21 = ZERO, c31 = ZERO;
                    double c02 = ZERO, c12 = ZERO, c22 = ZERO, c32 = ZERO;
                    double c03 = ZERO, c13 = ZERO, c23 = ZERO, c33 = ZERO;

                    for (int l = 0; l < k; l++) {
                        double a0 = a[colA0 + l], a1 = a[colA1 + l], a2 = a[colA2 + l], a3 = a[colA3 + l];
                        double b0 = b[colB0 + l], b1 = b[colB1 + l], b2 = b[colB2 + l], b3 = b[colB3 + l];
                        c00 = JVM.fma(a0, b0, c00);
                        c10 = JVM.fma(a1, b0, c10);
                        c20 = JVM.fma(a2, b0, c20);
                        c30 = JVM.fma(a3, b0, c30);
                        c01 = JVM.fma(a0, b1, c01);
                        c11 = JVM.fma(a1, b1, c11);
                        c21 = JVM.fma(a2, b1, c21);
                        c31 = JVM.fma(a3, b1, c31);
                        c02 = JVM.fma(a0, b2, c02);
                        c12 = JVM.fma(a1, b2, c12);
                        c22 = JVM.fma(a2, b2, c22);
                        c32 = JVM.fma(a3, b2, c32);
                        c03 = JVM.fma(a0, b3, c03);
                        c13 = JVM.fma(a1, b3, c13);
                        c23 = JVM.fma(a2, b3, c23);
                        c33 = JVM.fma(a3, b3, c33);
                    }

                    JVM.assign(c, colC0 + i, alpha, c00, beta);
                    JVM.assign(c, colC0 + i + 1, alpha, c10, beta);
                    JVM.assign(c, colC0 + i + 2, alpha, c20, beta);
                    JVM.assign(c, colC0 + i + 3, alpha, c30, beta);
                    JVM.assign(c, colC1 + i, alpha, c01, beta);
                    JVM.assign(c, colC1 + i + 1, alpha, c11, beta);
                    JVM.assign(c, colC1 + i + 2, alpha, c21, beta);
                    JVM.assign(c, colC1 + i + 3, alpha, c31, beta);
                    JVM.assign(c, colC2 + i, alpha, c02, beta);
                    JVM.assign(c, colC2 + i + 1, alpha, c12, beta);
                    JVM.assign(c, colC2 + i + 2, alpha, c22, beta);
                    JVM.assign(c, colC2 + i + 3, alpha, c32, beta);
                    JVM.assign(c, colC3 + i, alpha, c03, beta);
                    JVM.assign(c, colC3 + i + 1, alpha, c13, beta);
                    JVM.assign(c, colC3 + i + 2, alpha, c23, beta);
                    JVM.assign(c, colC3 + i + 3, alpha, c33, beta);
                }

                for (int i = m4; i < m; i++) {
                    int colA = offsetA + i * lda;
                    JVM.assign(c, colC0 + i, alpha, DOT.JVM.invoke(k, a, colA, 1, b, colB0, 1), beta);
                    JVM.assign(c, colC1 + i, alpha, DOT.JVM.invoke(k, a, colA, 1, b, colB1, 1), beta);
                    JVM.assign(c, colC2 + i, alpha, DOT.JVM.invoke(k, a, colA, 1, b, colB2, 1), beta);
                    JVM.assign(c, colC3 + i, alpha, DOT.JVM.invoke(k, a, colA, 1, b, colB3, 1), beta);
                }
            }

            for (; j < columnLimit; j++) {
                int colB = offsetB + j * ldb;
                int colC = offsetC + j * ldc;
                for (int i = 0; i < m; i++) {
                    JVM.assign(c, colC + i, alpha, DOT.JVM.invoke(k, a, offsetA + i * lda, 1, b, colB, 1), beta);
                }
            }
        }

        /**
         * C := alpha*A<sup>T</sup>*B<sup>T</sup> + beta*C for the columns {@code firstColumn} to
         * {@code columnLimit - 1} of C. Requires alpha != 0 and k > 0. Each 4x4 block of C is accumulated in
         * registers, reading 4 columns of A along k and, for each k, 4 consecutive elements in a column of B.
         * Leftover rows and columns use {@link DOT}.
         */
        static void invokeTT(final int m, final int firstColumn, final int columnLimit, final int k, final double alpha, final double[] a, final int offsetA,
                final int lda, final double[] b, final int offsetB, final int ldb, final double beta, final double[] c, final int offsetC, final int ldc) {

            int m4 = m - m % 4;

            int j = firstColumn;
            for (; j + 3 < columnLimit; j += 4) {

                int colC0 = offsetC + j * ldc;
                int colC1 = colC0 + ldc;
                int colC2 = colC1 + ldc;
                int colC3 = colC2 + ldc;

                for (int i = 0; i < m4; i += 4) {

                    int colA0 = offsetA + i * lda;
                    int colA1 = colA0 + lda;
                    int colA2 = colA1 + lda;
                    int colA3 = colA2 + lda;

                    double c00 = ZERO, c10 = ZERO, c20 = ZERO, c30 = ZERO;
                    double c01 = ZERO, c11 = ZERO, c21 = ZERO, c31 = ZERO;
                    double c02 = ZERO, c12 = ZERO, c22 = ZERO, c32 = ZERO;
                    double c03 = ZERO, c13 = ZERO, c23 = ZERO, c33 = ZERO;

                    for (int l = 0, rowB = offsetB + j; l < k; l++, rowB += ldb) {
                        double a0 = a[colA0 + l], a1 = a[colA1 + l], a2 = a[colA2 + l], a3 = a[colA3 + l];
                        double b0 = b[rowB], b1 = b[rowB + 1], b2 = b[rowB + 2], b3 = b[rowB + 3];
                        c00 = JVM.fma(a0, b0, c00);
                        c10 = JVM.fma(a1, b0, c10);
                        c20 = JVM.fma(a2, b0, c20);
                        c30 = JVM.fma(a3, b0, c30);
                        c01 = JVM.fma(a0, b1, c01);
                        c11 = JVM.fma(a1, b1, c11);
                        c21 = JVM.fma(a2, b1, c21);
                        c31 = JVM.fma(a3, b1, c31);
                        c02 = JVM.fma(a0, b2, c02);
                        c12 = JVM.fma(a1, b2, c12);
                        c22 = JVM.fma(a2, b2, c22);
                        c32 = JVM.fma(a3, b2, c32);
                        c03 = JVM.fma(a0, b3, c03);
                        c13 = JVM.fma(a1, b3, c13);
                        c23 = JVM.fma(a2, b3, c23);
                        c33 = JVM.fma(a3, b3, c33);
                    }

                    JVM.assign(c, colC0 + i, alpha, c00, beta);
                    JVM.assign(c, colC0 + i + 1, alpha, c10, beta);
                    JVM.assign(c, colC0 + i + 2, alpha, c20, beta);
                    JVM.assign(c, colC0 + i + 3, alpha, c30, beta);
                    JVM.assign(c, colC1 + i, alpha, c01, beta);
                    JVM.assign(c, colC1 + i + 1, alpha, c11, beta);
                    JVM.assign(c, colC1 + i + 2, alpha, c21, beta);
                    JVM.assign(c, colC1 + i + 3, alpha, c31, beta);
                    JVM.assign(c, colC2 + i, alpha, c02, beta);
                    JVM.assign(c, colC2 + i + 1, alpha, c12, beta);
                    JVM.assign(c, colC2 + i + 2, alpha, c22, beta);
                    JVM.assign(c, colC2 + i + 3, alpha, c32, beta);
                    JVM.assign(c, colC3 + i, alpha, c03, beta);
                    JVM.assign(c, colC3 + i + 1, alpha, c13, beta);
                    JVM.assign(c, colC3 + i + 2, alpha, c23, beta);
                    JVM.assign(c, colC3 + i + 3, alpha, c33, beta);
                }

                for (int i = m4; i < m; i++) {
                    int colA = offsetA + i * lda;
                    JVM.assign(c, colC0 + i, alpha, DOT.JVM.invoke(k, a, colA, 1, b, offsetB + j, ldb), beta);
                    JVM.assign(c, colC1 + i, alpha, DOT.JVM.invoke(k, a, colA, 1, b, offsetB + j + 1, ldb), beta);
                    JVM.assign(c, colC2 + i, alpha, DOT.JVM.invoke(k, a, colA, 1, b, offsetB + j + 2, ldb), beta);
                    JVM.assign(c, colC3 + i, alpha, DOT.JVM.invoke(k, a, colA, 1, b, offsetB + j + 3, ldb), beta);
                }
            }

            for (; j < columnLimit; j++) {
                int colC = offsetC + j * ldc;
                for (int i = 0; i < m; i++) {
                    JVM.assign(c, colC + i, alpha, DOT.JVM.invoke(k, a, offsetA + i * lda, 1, b, offsetB + j, ldb), beta);
                }
            }
        }

    }

    /**
     * The block size (rows and columns of A) used when the Java implementation is cache blocked.
     */
    static int BLOCK_SIZE = 512;

    /**
     * The Java implementation is cache blocked when both m and k are larger than this.
     */
    static int BLOCK_THRESHOLD = 3_000;

    /**
     * The Java implementation is multi-threaded, splitting the columns of C, when m*n*k is larger than this
     * cubed. Each part then gets more than half of that.
     */
    static int THRESHOLD = 70;

    /**
     * {@code dgemm}: C := alpha*op(A)*op(B) + beta*C
     * <p>
     * All matrices are column-major, stored in their arrays from an offset with a leading dimension (the
     * distance between columns). When beta is 0, C need not be set on entry; when alpha is 0, A and B are not
     * read.
     *
     * @param transposeA op(A) = A<sup>T</sup> if true, otherwise A
     * @param transposeB op(B) = B<sup>T</sup> if true, otherwise B
     * @param m          The number of rows of op(A) and C
     * @param n          The number of columns of op(B) and C
     * @param k          The number of columns of op(A) and rows of op(B)
     */
    public static void invoke(final boolean transposeA, final boolean transposeB, final int m, final int n, final int k, final double alpha, final double[] a,
            final int offsetA, final int lda, final double[] b, final int offsetB, final int ldb, final double beta, final double[] c, final int offsetC,
            final int ldc) {

        ArrayOperation.checkArgument("GEMM", "m", m >= 0);
        ArrayOperation.checkArgument("GEMM", "n", n >= 0);
        ArrayOperation.checkArgument("GEMM", "k", k >= 0);
        ArrayOperation.checkArgument("GEMM", "lda", lda >= Math.max(1, transposeA ? k : m));
        ArrayOperation.checkArgument("GEMM", "ldb", ldb >= Math.max(1, transposeB ? n : k));
        ArrayOperation.checkArgument("GEMM", "ldc", ldc >= Math.max(1, m));

        if (m == 0 || n == 0 || (alpha == ZERO || k == 0) && beta == ONE) {
            return;
        }

        JVM.invoke(transposeA, transposeB, m, n, k, alpha, a, offsetA, lda, b, offsetB, ldb, beta, c, offsetC, ldc);
    }

}

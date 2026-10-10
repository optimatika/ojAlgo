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

import static java.lang.foreign.ValueLayout.*;
import static org.ojalgo.function.constant.PrimitiveMath.ONE;
import static org.ojalgo.function.constant.PrimitiveMath.ZERO;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;
import java.util.function.IntSupplier;

import org.ojalgo.ProgrammingError;
import org.ojalgo.array.operation.AXPY;
import org.ojalgo.array.operation.ArrayOperation;
import org.ojalgo.array.operation.DOT;
import org.ojalgo.array.operation.NativeLibrary;
import org.ojalgo.array.operation.SCAL;
import org.ojalgo.concurrent.DivideAndConquer;
import org.ojalgo.concurrent.Parallelism;
import org.ojalgo.concurrent.ProcessingService;

/**
 * BLAS level 3 {@code ?gemm}: C := alpha*op(A)*op(B) + beta*C, where op(X) is X or X<sup>T</sup>, op(A) is
 * m-by-k, op(B) is k-by-n and C is m-by-n.
 *
 * @see NativeLibrary
 */
public abstract class GEMM implements MatrixOperation {

    /**
     * Native {@code cblas_dgemm}, initialised only when {@link NativeLibrary#isAvailable()}. Linked as a
     * critical function so the Java arrays are used in place, without copying. The JVM can't reach a
     * safepoint (and so can't collect garbage) until the call returns.
     */
    static final class Native {

        static final MethodHandle DGEMM = NativeLibrary.downcall("cblas_dgemm", FunctionDescriptor.ofVoid(JAVA_INT, JAVA_INT, JAVA_INT, JAVA_INT, JAVA_INT,
                JAVA_INT, JAVA_DOUBLE, ADDRESS, JAVA_INT, ADDRESS, JAVA_INT, JAVA_DOUBLE, ADDRESS, JAVA_INT), true);

        static void invoke(final boolean transposeA, final boolean transposeB, final int m, final int n, final int k, final double alpha, final double[] a,
                final int offsetA, final int lda, final double[] b, final int offsetB, final int ldb, final double beta, final double[] c, final int offsetC,
                final int ldc) {

            MemorySegment segmentA = transposeA ? CBLAS.ofMatrix(a, offsetA, k, m, lda) : CBLAS.ofMatrix(a, offsetA, m, k, lda);
            MemorySegment segmentB = transposeB ? CBLAS.ofMatrix(b, offsetB, n, k, ldb) : CBLAS.ofMatrix(b, offsetB, k, n, ldb);
            MemorySegment segmentC = CBLAS.ofMatrix(c, offsetC, m, n, ldc);

            try {
                DGEMM.invokeExact(CBLAS.COLUMN_MAJOR, CBLAS.transpose(transposeA), CBLAS.transpose(transposeB), m, n, k, alpha, segmentA, lda, segmentB, ldb,
                        beta, segmentC, ldc);
            } catch (Throwable cause) {
                throw new ProgrammingError(cause);
            }
        }

    }

    /**
     * Matrix size (m, n and k) from which the native implementation is used, if there is one.
     */
    public static int NATIVE_THRESHOLD = 16;
    public static IntSupplier PARALLELISM = Parallelism.THREADS;
    /**
     * The Java implementation is multi-threaded, splitting the columns of C, when both m and n are larger
     * than this.
     */
    public static int THRESHOLD = 32;

    private static final DivideAndConquer.Divider DIVIDER = ProcessingService.INSTANCE.newDivider();

    static final boolean NATIVE = NativeLibrary.isAvailable() && Native.DGEMM != null;

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

        if (NATIVE && m >= NATIVE_THRESHOLD && n >= NATIVE_THRESHOLD && k >= NATIVE_THRESHOLD) {
            Native.invoke(transposeA, transposeB, m, n, k, alpha, a, offsetA, lda, b, offsetB, ldb, beta, c, offsetC, ldc);
        } else {
            GEMM.invokeJava(transposeA, transposeB, m, n, k, alpha, a, offsetA, lda, b, offsetB, ldb, beta, c, offsetC, ldc);
        }
    }

    /**
     * The columns {@code firstColumn} to {@code columnLimit - 1} of C, picking the kernel for the transpose
     * combination.
     */
    static void invokeColumns(final boolean transposeA, final boolean transposeB, final int m, final int firstColumn, final int columnLimit,
            final int k, final double alpha, final double[] a, final int offsetA, final int lda, final double[] b, final int offsetB, final int ldb,
            final double beta, final double[] c, final int offsetC, final int ldc) {

        if (alpha == ZERO || k == 0) {
            for (int j = firstColumn; j < columnLimit; j++) {
                int colC = offsetC + j * ldc;
                SCAL.scale(c, colC, colC + m, beta);
            }
        } else if (!transposeA) {
            if (!transposeB) {
                GEMM.invokeNN(m, firstColumn, columnLimit, k, alpha, a, offsetA, lda, b, offsetB, ldb, beta, c, offsetC, ldc);
            } else {
                GEMM.invokeNT(m, firstColumn, columnLimit, k, alpha, a, offsetA, lda, b, offsetB, ldb, beta, c, offsetC, ldc);
            }
        } else if (!transposeB) {
            GEMM.invokeTN(m, firstColumn, columnLimit, k, alpha, a, offsetA, lda, b, offsetB, ldb, beta, c, offsetC, ldc);
        } else {
            GEMM.invokeTT(m, firstColumn, columnLimit, k, alpha, a, offsetA, lda, b, offsetB, ldb, beta, c, offsetC, ldc);
        }
    }

    /**
     * The pure Java implementation of {@link #invoke}, without argument checks. Multi-threaded, splitting the
     * columns of C, when both m and n are larger than {@link #THRESHOLD}.
     */
    static void invokeJava(final boolean transposeA, final boolean transposeB, final int m, final int n, final int k, final double alpha,
            final double[] a, final int offsetA, final int lda, final double[] b, final int offsetB, final int ldb, final double beta, final double[] c,
            final int offsetC, final int ldc) {

        if (m > THRESHOLD && n > THRESHOLD) {
            DIVIDER.parallelism(PARALLELISM).threshold(THRESHOLD).divide(0, n, (first, limit) -> GEMM.invokeColumns(transposeA, transposeB, m, first,
                    limit, k, alpha, a, offsetA, lda, b, offsetB, ldb, beta, c, offsetC, ldc));
        } else {
            GEMM.invokeColumns(transposeA, transposeB, m, 0, n, k, alpha, a, offsetA, lda, b, offsetB, ldb, beta, c, offsetC, ldc);
        }
    }

    /**
     * C := alpha*A*B + beta*C for the columns {@code firstColumn} to {@code columnLimit - 1} of C. Requires
     * alpha != 0 and k > 0. One {@link AXPY} per element of B, down a column of A into a column of C.
     */
    static void invokeNN(final int m, final int firstColumn, final int columnLimit, final int k, final double alpha, final double[] a, final int offsetA,
            final int lda, final double[] b, final int offsetB, final int ldb, final double beta, final double[] c, final int offsetC, final int ldc) {

        for (int j = firstColumn; j < columnLimit; j++) {
            int colB = offsetB + j * ldb;
            int colC = offsetC + j * ldc;
            SCAL.scale(c, colC, colC + m, beta);
            for (int l = 0; l < k; l++) {
                AXPY.invoke(c, colC, alpha * b[colB + l], a, offsetA + l * lda, 0, m);
            }
        }
    }

    /**
     * C := alpha*A*B<sup>T</sup> + beta*C for the columns {@code firstColumn} to {@code columnLimit - 1} of C.
     * Requires alpha != 0 and k > 0. As {@link #invokeNN}, but reading B along a
     * row.
     */
    static void invokeNT(final int m, final int firstColumn, final int columnLimit, final int k, final double alpha, final double[] a, final int offsetA,
            final int lda, final double[] b, final int offsetB, final int ldb, final double beta, final double[] c, final int offsetC, final int ldc) {

        for (int j = firstColumn; j < columnLimit; j++) {
            int colC = offsetC + j * ldc;
            SCAL.scale(c, colC, colC + m, beta);
            for (int l = 0; l < k; l++) {
                AXPY.invoke(c, colC, alpha * b[offsetB + j + l * ldb], a, offsetA + l * lda, 0, m);
            }
        }
    }

    /**
     * C := alpha*A<sup>T</sup>*B + beta*C for the columns {@code firstColumn} to {@code columnLimit - 1} of
     * C. Requires alpha != 0 and k > 0. One {@link DOT} per element of C, of a column of A and a column of B.
     */
    static void invokeTN(final int m, final int firstColumn, final int columnLimit, final int k, final double alpha, final double[] a, final int offsetA,
            final int lda, final double[] b, final int offsetB, final int ldb, final double beta, final double[] c, final int offsetC, final int ldc) {

        for (int j = firstColumn; j < columnLimit; j++) {
            int colB = offsetB + j * ldb;
            int colC = offsetC + j * ldc;
            for (int i = 0; i < m; i++) {
                double temp = alpha * DOT.invoke(a, offsetA + i * lda, b, colB, 0, k);
                c[colC + i] = beta == ZERO ? temp : temp + beta * c[colC + i];
            }
        }
    }

    /**
     * C := alpha*A<sup>T</sup>*B<sup>T</sup> + beta*C for the columns {@code firstColumn} to
     * {@code columnLimit - 1} of C. Requires alpha != 0 and k > 0. One strided {@link DOT} per element of C, of
     * a column of A and a row of B.
     */
    static void invokeTT(final int m, final int firstColumn, final int columnLimit, final int k, final double alpha, final double[] a, final int offsetA,
            final int lda, final double[] b, final int offsetB, final int ldb, final double beta, final double[] c, final int offsetC, final int ldc) {

        for (int j = firstColumn; j < columnLimit; j++) {
            int colC = offsetC + j * ldc;
            for (int i = 0; i < m; i++) {
                double temp = alpha * DOT.invoke(a, offsetA + i * lda, 1, b, offsetB + j, ldb, k);
                c[colC + i] = beta == ZERO ? temp : temp + beta * c[colC + i];
            }
        }
    }

}

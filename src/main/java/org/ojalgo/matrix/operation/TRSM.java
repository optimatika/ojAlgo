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

import org.ojalgo.ProgrammingError;
import org.ojalgo.array.operation.AXPY;
import org.ojalgo.array.operation.ArrayOperation;
import org.ojalgo.array.operation.DOT;
import org.ojalgo.array.operation.NativeLibrary;
import org.ojalgo.array.operation.SCAL;

/**
 * BLAS level 3 {@code ?trsm}: solves op(A)*X = alpha*B or X*op(A) = alpha*B for X, where A is triangular,
 * op(A) is A or A<sup>T</sup>, and X and B are m-by-n. X overwrites B.
 *
 * @see NativeLibrary
 */
public abstract class TRSM implements MatrixOperation {

    /**
     * Native {@code cblas_dtrsm}, initialised only when {@link NativeLibrary#isAvailable()}. Linked as a
     * critical function so the Java arrays are used in place, without copying.
     */
    static final class Native {

        static final MethodHandle DTRSM = NativeLibrary.downcall("cblas_dtrsm", FunctionDescriptor.ofVoid(JAVA_INT, JAVA_INT, JAVA_INT, JAVA_INT, JAVA_INT,
                JAVA_INT, JAVA_INT, JAVA_DOUBLE, ADDRESS, JAVA_INT, ADDRESS, JAVA_INT), true);

        static void invoke(final boolean left, final boolean upper, final boolean transposeA, final boolean unitDiagonal, final int m, final int n,
                final double alpha, final double[] a, final int offsetA, final int lda, final double[] b, final int offsetB, final int ldb) {

            int dimA = left ? m : n;

            MemorySegment segmentA = CBLAS.ofMatrix(a, offsetA, dimA, dimA, lda);
            MemorySegment segmentB = CBLAS.ofMatrix(b, offsetB, m, n, ldb);

            try {
                DTRSM.invokeExact(CBLAS.COLUMN_MAJOR, CBLAS.side(left), CBLAS.triangle(upper), CBLAS.transpose(transposeA), CBLAS.diagonal(unitDiagonal), m, n,
                        alpha, segmentA, lda, segmentB, ldb);
            } catch (Throwable cause) {
                throw new ProgrammingError(cause);
            }
        }

    }

    /**
     * Matrix size (m and n) from which the native implementation is used, if there is one.
     */
    public static int NATIVE_THRESHOLD = 16;

    static final boolean NATIVE = NativeLibrary.isAvailable() && Native.DTRSM != null;

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

        if (NATIVE && m >= NATIVE_THRESHOLD && n >= NATIVE_THRESHOLD) {
            Native.invoke(left, upper, transposeA, unitDiagonal, m, n, alpha, a, offsetA, lda, b, offsetB, ldb);
        } else {
            TRSM.invokeJava(left, upper, transposeA, unitDiagonal, m, n, alpha, a, offsetA, lda, b, offsetB, ldb);
        }
    }

    /**
     * The pure Java implementation of {@link #invoke}, without argument checks.
     */
    static void invokeJava(final boolean left, final boolean upper, final boolean transposeA, final boolean unitDiagonal, final int m, final int n,
            final double alpha, final double[] a, final int offsetA, final int lda, final double[] b, final int offsetB, final int ldb) {

        if (alpha == ZERO) {
            for (int j = 0; j < n; j++) {
                int colB = offsetB + j * ldb;
                SCAL.scale(b, colB, colB + m, ZERO);
            }
            return;
        }

        if (left) {

            if (!transposeA) {
                if (upper) {
                    for (int j = 0; j < n; j++) {
                        int colB = offsetB + j * ldb;
                        SCAL.scale(b, colB, colB + m, alpha);
                        for (int k = m - 1; k >= 0; k--) {
                            int colA = offsetA + k * lda;
                            if (!unitDiagonal) {
                                b[colB + k] /= a[colA + k];
                            }
                            AXPY.invoke(b, colB, -b[colB + k], a, colA, 0, k);
                        }
                    }
                } else {
                    for (int j = 0; j < n; j++) {
                        int colB = offsetB + j * ldb;
                        SCAL.scale(b, colB, colB + m, alpha);
                        for (int k = 0; k < m; k++) {
                            int colA = offsetA + k * lda;
                            if (!unitDiagonal) {
                                b[colB + k] /= a[colA + k];
                            }
                            AXPY.invoke(b, colB, -b[colB + k], a, colA, k + 1, m);
                        }
                    }
                }
            } else if (upper) {
                for (int j = 0; j < n; j++) {
                    int colB = offsetB + j * ldb;
                    for (int i = 0; i < m; i++) {
                        int colA = offsetA + i * lda;
                        double temp = alpha * b[colB + i] - DOT.invoke(a, colA, b, colB, 0, i);
                        b[colB + i] = unitDiagonal ? temp : temp / a[colA + i];
                    }
                }
            } else {
                for (int j = 0; j < n; j++) {
                    int colB = offsetB + j * ldb;
                    for (int i = m - 1; i >= 0; i--) {
                        int colA = offsetA + i * lda;
                        double temp = alpha * b[colB + i] - DOT.invoke(a, colA, b, colB, i + 1, m);
                        b[colB + i] = unitDiagonal ? temp : temp / a[colA + i];
                    }
                }
            }

        } else if (!transposeA) {

            if (upper) {
                for (int j = 0; j < n; j++) {
                    int colB = offsetB + j * ldb;
                    SCAL.scale(b, colB, colB + m, alpha);
                    for (int k = 0; k < j; k++) {
                        AXPY.invoke(b, colB, -a[offsetA + k + j * lda], b, offsetB + k * ldb, 0, m);
                    }
                    if (!unitDiagonal) {
                        SCAL.invoke(m, ONE / a[offsetA + j + j * lda], b, colB, 1);
                    }
                }
            } else {
                for (int j = n - 1; j >= 0; j--) {
                    int colB = offsetB + j * ldb;
                    SCAL.scale(b, colB, colB + m, alpha);
                    for (int k = j + 1; k < n; k++) {
                        AXPY.invoke(b, colB, -a[offsetA + k + j * lda], b, offsetB + k * ldb, 0, m);
                    }
                    if (!unitDiagonal) {
                        SCAL.invoke(m, ONE / a[offsetA + j + j * lda], b, colB, 1);
                    }
                }
            }

        } else if (upper) {
            for (int k = n - 1; k >= 0; k--) {
                int colB = offsetB + k * ldb;
                if (!unitDiagonal) {
                    SCAL.invoke(m, ONE / a[offsetA + k + k * lda], b, colB, 1);
                }
                for (int j = 0; j < k; j++) {
                    AXPY.invoke(b, offsetB + j * ldb, -a[offsetA + j + k * lda], b, colB, 0, m);
                }
                SCAL.scale(b, colB, colB + m, alpha);
            }
        } else {
            for (int k = 0; k < n; k++) {
                int colB = offsetB + k * ldb;
                if (!unitDiagonal) {
                    SCAL.invoke(m, ONE / a[offsetA + k + k * lda], b, colB, 1);
                }
                for (int j = k + 1; j < n; j++) {
                    AXPY.invoke(b, offsetB + j * ldb, -a[offsetA + j + k * lda], b, colB, 0, m);
                }
                SCAL.scale(b, colB, colB + m, alpha);
            }
        }
    }

}

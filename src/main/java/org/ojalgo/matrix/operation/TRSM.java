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

import java.lang.foreign.Arena;
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
public abstract class TRSM implements BLAS3 {

    /**
     * The pure Java implementation. Everything it calls is pure Java as well – the {@code JVM}
     * implementations of other operations, never their dispatching {@code invoke} methods (that may call
     * native code).
     */
    static final class JVM {

        /**
         * The pure Java implementation of {@link TRSM#invoke}, without argument checks.
         */
        static void invoke(final boolean left, final boolean upper, final boolean transposeA, final boolean unitDiagonal, final int m, final int n,
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
                                AXPY.JVM.invoke(k, -b[colB + k], a, colA, 1, b, colB, 1);
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
                                AXPY.JVM.invoke(m - k - 1, -b[colB + k], a, colA + k + 1, 1, b, colB + k + 1, 1);
                            }
                        }
                    }
                } else if (upper) {
                    for (int j = 0; j < n; j++) {
                        int colB = offsetB + j * ldb;
                        for (int i = 0; i < m; i++) {
                            int colA = offsetA + i * lda;
                            double temp = alpha * b[colB + i] - DOT.JVM.invoke(i, a, colA, 1, b, colB, 1);
                            b[colB + i] = unitDiagonal ? temp : temp / a[colA + i];
                        }
                    }
                } else {
                    for (int j = 0; j < n; j++) {
                        int colB = offsetB + j * ldb;
                        for (int i = m - 1; i >= 0; i--) {
                            int colA = offsetA + i * lda;
                            double temp = alpha * b[colB + i] - DOT.JVM.invoke(m - i - 1, a, colA + i + 1, 1, b, colB + i + 1, 1);
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
                            AXPY.JVM.invoke(m, -a[offsetA + k + j * lda], b, offsetB + k * ldb, 1, b, colB, 1);
                        }
                        if (!unitDiagonal) {
                            SCAL.JVM.invoke(m, ONE / a[offsetA + j + j * lda], b, colB, 1);
                        }
                    }
                } else {
                    for (int j = n - 1; j >= 0; j--) {
                        int colB = offsetB + j * ldb;
                        SCAL.scale(b, colB, colB + m, alpha);
                        for (int k = j + 1; k < n; k++) {
                            AXPY.JVM.invoke(m, -a[offsetA + k + j * lda], b, offsetB + k * ldb, 1, b, colB, 1);
                        }
                        if (!unitDiagonal) {
                            SCAL.JVM.invoke(m, ONE / a[offsetA + j + j * lda], b, colB, 1);
                        }
                    }
                }

            } else if (upper) {
                for (int k = n - 1; k >= 0; k--) {
                    int colB = offsetB + k * ldb;
                    if (!unitDiagonal) {
                        SCAL.JVM.invoke(m, ONE / a[offsetA + k + k * lda], b, colB, 1);
                    }
                    for (int j = 0; j < k; j++) {
                        AXPY.JVM.invoke(m, -a[offsetA + j + k * lda], b, colB, 1, b, offsetB + j * ldb, 1);
                    }
                    SCAL.scale(b, colB, colB + m, alpha);
                }
            } else {
                for (int k = 0; k < n; k++) {
                    int colB = offsetB + k * ldb;
                    if (!unitDiagonal) {
                        SCAL.JVM.invoke(m, ONE / a[offsetA + k + k * lda], b, colB, 1);
                    }
                    for (int j = k + 1; j < n; j++) {
                        AXPY.JVM.invoke(m, -a[offsetA + j + k * lda], b, colB, 1, b, offsetB + j * ldb, 1);
                    }
                    SCAL.scale(b, colB, colB + m, alpha);
                }
            }
        }

    }

    /**
     * Native {@code cblas_dtrsm}, initialised only when {@link NativeLibrary#isAvailable()}.
     */
    static final class Native {

        private static final FunctionDescriptor DESCRIPTOR = FunctionDescriptor.ofVoid(JAVA_INT, JAVA_INT, JAVA_INT, JAVA_INT, JAVA_INT, JAVA_INT, JAVA_INT,
                JAVA_DOUBLE, ADDRESS, JAVA_INT, ADDRESS, JAVA_INT);

        /**
         * Linked as a critical function, so it is passed the Java arrays in place, without copying. The JVM
         * can't reach a safepoint (and so can't collect garbage) until the call returns.
         */
        static final MethodHandle DTRSM_CRITICAL = NativeLibrary.downcall("cblas_dtrsm", DESCRIPTOR, true);
        /**
         * The same function, not linked as critical. It must be passed native memory, but the JVM can reach
         * safepoints during the call.
         */
        static final MethodHandle DTRSM_OFF_HEAP = NativeLibrary.downcall("cblas_dtrsm", DESCRIPTOR, false);

        private static void invokeCritical(final boolean left, final boolean upper, final boolean transposeA, final boolean unitDiagonal, final int m,
                final int n, final double alpha, final double[] a, final int offsetA, final int lda, final double[] b, final int offsetB, final int ldb) {

            int dimA = left ? m : n;

            MemorySegment segmentA = CBLAS.ofMatrix(a, offsetA, dimA, dimA, lda);
            MemorySegment segmentB = CBLAS.ofMatrix(b, offsetB, m, n, ldb);

            try {
                DTRSM_CRITICAL.invokeExact(CBLAS.COLUMN_MAJOR, CBLAS.side(left), CBLAS.triangle(upper), CBLAS.transpose(transposeA),
                        CBLAS.diagonal(unitDiagonal), m, n, alpha, segmentA, lda, segmentB, ldb);
            } catch (Throwable cause) {
                throw new ProgrammingError(cause);
            }
        }

        /**
         * Copies the matrices to native memory, calls {@link #DTRSM_OFF_HEAP} and copies B back. Requires m,
         * n > 0.
         */
        private static void invokeOffHeap(final boolean left, final boolean upper, final boolean transposeA, final boolean unitDiagonal, final int m,
                final int n, final double alpha, final double[] a, final int offsetA, final int lda, final double[] b, final int offsetB, final int ldb) {

            int dimA = left ? m : n;

            try (Arena arena = Arena.ofConfined()) {

                int copyLDA = CBLAS.leadingDimensionOfCopy(dimA);
                int copyLDB = CBLAS.leadingDimensionOfCopy(m);

                MemorySegment segmentA = CBLAS.copyOfMatrix(arena, a, offsetA, dimA, dimA, lda, copyLDA);
                MemorySegment segmentB = CBLAS.copyOfMatrix(arena, b, offsetB, m, n, ldb, copyLDB);

                DTRSM_OFF_HEAP.invokeExact(CBLAS.COLUMN_MAJOR, CBLAS.side(left), CBLAS.triangle(upper), CBLAS.transpose(transposeA),
                        CBLAS.diagonal(unitDiagonal), m, n, alpha, segmentA, copyLDA, segmentB, copyLDB);

                CBLAS.copyBackMatrix(segmentB, copyLDB, b, offsetB, m, n, ldb);

            } catch (RuntimeException | Error cause) {
                throw cause;
            } catch (Throwable cause) {
                throw new ProgrammingError(cause);
            }
        }

        /**
         * Critical, with the Java arrays in place, or off-heap, with copies in native memory, when
         * {@link TRSM#isOffHeap(double, double, double)}. A must be within its array also when alpha is 0.
         */
        static void invoke(final boolean left, final boolean upper, final boolean transposeA, final boolean unitDiagonal, final int m, final int n,
                final double alpha, final double[] a, final int offsetA, final int lda, final double[] b, final int offsetB, final int ldb) {
            if (TRSM.isOffHeap(m, n, left ? m : n)) {
                Native.invokeOffHeap(left, upper, transposeA, unitDiagonal, m, n, alpha, a, offsetA, lda, b, offsetB, ldb);
            } else {
                Native.invokeCritical(left, upper, transposeA, unitDiagonal, m, n, alpha, a, offsetA, lda, b, offsetB, ldb);
            }
        }

    }

    /**
     * The native implementation is used, if there is one, when the number of multiply-adds is larger than
     * this. Not measured yet – the value is GEMM's.
     */
    private static final double NATIVE_THRESHOLD = 10_000;

    static final boolean NATIVE = NativeLibrary.isAvailable() && Native.DTRSM_CRITICAL != null;
    /**
     * The native implementation is linked as a critical function, and passed the Java arrays in place. A
     * critical call blocks safepoints: garbage collection, in the whole JVM, waits until it returns. Long and
     * compute-intensive calls – the number of multiply-adds larger than {@link #OFF_HEAP_THRESHOLD} and the
     * smaller of m and n larger than this – are instead passed copies of the matrices in native memory, and
     * the function is called without blocking safepoints. The copies then cost little compared to the call.
     * Not measured yet – the values are GEMM's.
     */
    static double OFF_HEAP_DIMENSION = 300;
    static double OFF_HEAP_THRESHOLD = 3E9;

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

        // With alpha 0, A is not read and the Java implementation does what remains
        if (alpha != ZERO && TRSM.isNative(m, n, left ? m : n)) {
            Native.invoke(left, upper, transposeA, unitDiagonal, m, n, alpha, a, offsetA, lda, b, offsetB, ldb);
        } else {
            JVM.invoke(left, upper, transposeA, unitDiagonal, m, n, alpha, a, offsetA, lda, b, offsetB, ldb);
        }
    }

    /**
     * Whether to use the native implementation: there is one, and m*n*m (left) or m*n*n (right) is larger
     * than {@link #NATIVE_THRESHOLD}.
     */
    static boolean isNative(final double m, final double n, final double k) {
        return NATIVE && m * n * k > NATIVE_THRESHOLD;
    }

    /**
     * Whether to call the native implementation with copies of the matrices in native memory, rather than
     * critically with the Java arrays in place, see {@link #OFF_HEAP_DIMENSION}.
     */
    static boolean isOffHeap(final double m, final double n, final double k) {
        return m * n * k > OFF_HEAP_THRESHOLD && Math.min(m, Math.min(n, k)) > OFF_HEAP_DIMENSION;
    }

}

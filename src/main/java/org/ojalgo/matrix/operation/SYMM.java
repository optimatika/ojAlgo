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
import org.ojalgo.array.operation.NativeLibrary;
import org.ojalgo.array.operation.SCAL;

/**
 * BLAS level 3 {@code ?symm}: C := alpha*A*B + beta*C or C := alpha*B*A + beta*C, where A is symmetric and B
 * and C are m-by-n.
 *
 * @see NativeLibrary
 */
public abstract class SYMM implements BLAS3 {

    /**
     * The pure Java implementation. Everything it calls is pure Java as well – the {@code JVM}
     * implementations of other operations, never their dispatching {@code invoke} methods (that may call
     * native code).
     */
    static final class JVM {

        /**
         * The pure Java implementation of {@link SYMM#invoke}, without argument checks.
         */
        static void invoke(final boolean left, final boolean upper, final int m, final int n, final double alpha, final double[] a, final int offsetA,
                final int lda, final double[] b, final int offsetB, final int ldb, final double beta, final double[] c, final int offsetC, final int ldc) {

            if (alpha == ZERO) {
                for (int j = 0; j < n; j++) {
                    int colC = offsetC + j * ldc;
                    SCAL.scale(c, colC, colC + m, beta);
                }
                return;
            }

            if (left) {

                if (upper) {
                    for (int j = 0; j < n; j++) {
                        int colB = offsetB + j * ldb;
                        int colC = offsetC + j * ldc;
                        for (int i = 0; i < m; i++) {
                            int colA = offsetA + i * lda;
                            double temp1 = alpha * b[colB + i];
                            double temp2 = ZERO;
                            for (int k = 0; k < i; k++) {
                                c[colC + k] += temp1 * a[colA + k];
                                temp2 += b[colB + k] * a[colA + k];
                            }
                            double scaled = beta == ZERO ? ZERO : beta * c[colC + i];
                            c[colC + i] = scaled + temp1 * a[colA + i] + alpha * temp2;
                        }
                    }
                } else {
                    for (int j = 0; j < n; j++) {
                        int colB = offsetB + j * ldb;
                        int colC = offsetC + j * ldc;
                        for (int i = m - 1; i >= 0; i--) {
                            int colA = offsetA + i * lda;
                            double temp1 = alpha * b[colB + i];
                            double temp2 = ZERO;
                            for (int k = i + 1; k < m; k++) {
                                c[colC + k] += temp1 * a[colA + k];
                                temp2 += b[colB + k] * a[colA + k];
                            }
                            double scaled = beta == ZERO ? ZERO : beta * c[colC + i];
                            c[colC + i] = scaled + temp1 * a[colA + i] + alpha * temp2;
                        }
                    }
                }

            } else {

                for (int j = 0; j < n; j++) {
                    int colC = offsetC + j * ldc;
                    SCAL.scale(c, colC, colC + m, beta);
                    AXPY.invoke(c, colC, alpha * a[offsetA + j + j * lda], b, offsetB + j * ldb, 0, m);
                    for (int k = 0; k < j; k++) {
                        double element = upper ? a[offsetA + k + j * lda] : a[offsetA + j + k * lda];
                        AXPY.invoke(c, colC, alpha * element, b, offsetB + k * ldb, 0, m);
                    }
                    for (int k = j + 1; k < n; k++) {
                        double element = upper ? a[offsetA + j + k * lda] : a[offsetA + k + j * lda];
                        AXPY.invoke(c, colC, alpha * element, b, offsetB + k * ldb, 0, m);
                    }
                }
            }
        }

    }

    /**
     * Native {@code cblas_dsymm}, initialised only when {@link NativeLibrary#isAvailable()}. Linked as a
     * critical function so the Java arrays are used in place, without copying.
     */
    static final class Native {

        private static final FunctionDescriptor DESCRIPTOR = FunctionDescriptor.ofVoid(JAVA_INT, JAVA_INT, JAVA_INT, JAVA_INT, JAVA_INT, JAVA_DOUBLE, ADDRESS,
                JAVA_INT, ADDRESS, JAVA_INT, JAVA_DOUBLE, ADDRESS, JAVA_INT);

        static final MethodHandle DSYMM = NativeLibrary.downcall("cblas_dsymm", DESCRIPTOR, true);
        /**
         * The same function, not linked as critical. It must be passed native memory, but the JVM can reach
         * safepoints during the call.
         */
        static final MethodHandle DSYMM_OFF_HEAP = NativeLibrary.downcall("cblas_dsymm", DESCRIPTOR, false);

        static void invoke(final boolean left, final boolean upper, final int m, final int n, final double alpha, final double[] a, final int offsetA,
                final int lda, final double[] b, final int offsetB, final int ldb, final double beta, final double[] c, final int offsetC, final int ldc) {

            int dimA = left ? m : n;

            MemorySegment segmentA = CBLAS.ofMatrix(a, offsetA, dimA, dimA, lda);
            MemorySegment segmentB = CBLAS.ofMatrix(b, offsetB, m, n, ldb);
            MemorySegment segmentC = CBLAS.ofMatrix(c, offsetC, m, n, ldc);

            try {
                DSYMM.invokeExact(CBLAS.COLUMN_MAJOR, CBLAS.side(left), CBLAS.triangle(upper), m, n, alpha, segmentA, lda, segmentB, ldb, beta, segmentC, ldc);
            } catch (Throwable cause) {
                throw new ProgrammingError(cause);
            }
        }

        /**
         * Copies the matrices to native memory, calls {@link #DSYMM_OFF_HEAP} and copies C back. When beta is
         * 0, C is not copied to native memory. Requires alpha != 0 (A and B are read) and m, n > 0.
         */
        static void invokeOffHeap(final boolean left, final boolean upper, final int m, final int n, final double alpha, final double[] a, final int offsetA,
                final int lda, final double[] b, final int offsetB, final int ldb, final double beta, final double[] c, final int offsetC, final int ldc) {

            int dimA = left ? m : n;

            try (Arena arena = Arena.ofConfined()) {

                MemorySegment segmentA = CBLAS.copyOfMatrix(arena, a, offsetA, dimA, dimA, lda);
                MemorySegment segmentB = CBLAS.copyOfMatrix(arena, b, offsetB, m, n, ldb);
                MemorySegment segmentC = beta == ZERO ? arena.allocate(JAVA_DOUBLE, (long) m * n) : CBLAS.copyOfMatrix(arena, c, offsetC, m, n, ldc);

                DSYMM_OFF_HEAP.invokeExact(CBLAS.COLUMN_MAJOR, CBLAS.side(left), CBLAS.triangle(upper), m, n, alpha, segmentA, dimA, segmentB, m, beta,
                        segmentC, m);

                CBLAS.copyBackMatrix(segmentC, c, offsetC, m, n, ldc);

            } catch (RuntimeException | Error cause) {
                throw cause;
            } catch (Throwable cause) {
                throw new ProgrammingError(cause);
            }
        }

    }

    /**
     * The native implementation is used, if there is one, when the number of multiply-adds is larger than
     * this. Not measured yet – the value is GEMM's.
     */
    private static final double NATIVE_THRESHOLD = 10_000;
    /**
     * The native implementation is linked as a critical function, and passed the Java arrays in place. A
     * critical call blocks safepoints: garbage collection, in the whole JVM, waits until it returns. Long and
     * compute-intensive calls – the number of multiply-adds larger than {@link #OFF_HEAP_THRESHOLD} and the
     * smaller of m and n larger than this – are instead passed copies of the matrices in native memory, and
     * the function is called without blocking safepoints. The copies then cost little compared to the call.
     * Not measured yet – the values are GEMM's.
     */
    private static final double OFF_HEAP_DIMENSION = 300;
    private static final double OFF_HEAP_THRESHOLD = 3E9;

    static final boolean NATIVE = NativeLibrary.isAvailable() && Native.DSYMM != null;

    /**
     * {@code dsymm}: C := alpha*A*B + beta*C (left) or C := alpha*B*A + beta*C (right)
     * <p>
     * All matrices are column-major, stored in their arrays from an offset with a leading dimension. Only the
     * upper or lower triangle of A is read. When beta is 0, C need not be set on entry; when alpha is 0, A
     * and B are not read.
     *
     * @param left  A is on the left (m-by-m) if true, otherwise on the right (n-by-n)
     * @param upper The upper triangle of A is used if true, otherwise the lower
     * @param m     The number of rows of B and C
     * @param n     The number of columns of B and C
     */
    public static void invoke(final boolean left, final boolean upper, final int m, final int n, final double alpha, final double[] a, final int offsetA,
            final int lda, final double[] b, final int offsetB, final int ldb, final double beta, final double[] c, final int offsetC, final int ldc) {

        ArrayOperation.checkArgument("SYMM", "m", m >= 0);
        ArrayOperation.checkArgument("SYMM", "n", n >= 0);
        ArrayOperation.checkArgument("SYMM", "lda", lda >= Math.max(1, left ? m : n));
        ArrayOperation.checkArgument("SYMM", "ldb", ldb >= Math.max(1, m));
        ArrayOperation.checkArgument("SYMM", "ldc", ldc >= Math.max(1, m));

        if (m == 0 || n == 0 || alpha == ZERO && beta == ONE) {
            return;
        }

        // With alpha 0, A and B are not read and the Java implementation does what remains
        if (alpha != ZERO && SYMM.isNative(m, n, left ? m : n)) {
            if (SYMM.isOffHeap(m, n, left ? m : n)) {
                Native.invokeOffHeap(left, upper, m, n, alpha, a, offsetA, lda, b, offsetB, ldb, beta, c, offsetC, ldc);
            } else {
                Native.invoke(left, upper, m, n, alpha, a, offsetA, lda, b, offsetB, ldb, beta, c, offsetC, ldc);
            }
        } else {
            JVM.invoke(left, upper, m, n, alpha, a, offsetA, lda, b, offsetB, ldb, beta, c, offsetC, ldc);
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

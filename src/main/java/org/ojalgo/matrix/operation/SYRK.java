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
 * BLAS level 3 {@code ?syrk}: C := alpha*A*A<sup>T</sup> + beta*C or C := alpha*A<sup>T</sup>*A + beta*C,
 * where C is symmetric n-by-n and only its upper or lower triangle is updated.
 *
 * @see NativeLibrary
 */
public abstract class SYRK implements BLAS3 {

    /**
     * The pure Java implementation. Everything it calls is pure Java as well – the {@code JVM}
     * implementations of other operations, never their dispatching {@code invoke} methods (that may call
     * native code).
     */
    static final class JVM {

        /**
         * The pure Java implementation of {@link SYRK#invoke}, without argument checks.
         */
        static void invoke(final boolean upper, final boolean transpose, final int n, final int k, final double alpha, final double[] a, final int offsetA,
                final int lda, final double beta, final double[] c, final int offsetC, final int ldc) {

            if (alpha == ZERO || k == 0) {
                for (int j = 0; j < n; j++) {
                    int colC = offsetC + j * ldc;
                    if (upper) {
                        SCAL.scale(c, colC, colC + j + 1, beta);
                    } else {
                        SCAL.scale(c, colC + j, colC + n, beta);
                    }
                }
                return;
            }

            if (!transpose) {
                for (int j = 0; j < n; j++) {
                    int colC = offsetC + j * ldc;
                    int first = upper ? 0 : j;
                    int limit = upper ? j + 1 : n;
                    SCAL.scale(c, colC + first, colC + limit, beta);
                    for (int l = 0; l < k; l++) {
                        int colA = offsetA + l * lda;
                        AXPY.JVM.invoke(limit - first, alpha * a[colA + j], a, colA + first, 1, c, colC + first, 1);
                    }
                }
            } else {
                for (int j = 0; j < n; j++) {
                    int colAj = offsetA + j * lda;
                    int colC = offsetC + j * ldc;
                    int first = upper ? 0 : j;
                    int limit = upper ? j + 1 : n;
                    for (int i = first; i < limit; i++) {
                        double temp = alpha * DOT.JVM.invoke(k, a, offsetA + i * lda, 1, a, colAj, 1);
                        c[colC + i] = beta == ZERO ? temp : temp + beta * c[colC + i];
                    }
                }
            }
        }

    }

    /**
     * Native {@code cblas_dsyrk}, initialised only when {@link NativeLibrary#isAvailable()}.
     */
    static final class Native {

        private static final FunctionDescriptor DESCRIPTOR = FunctionDescriptor.ofVoid(JAVA_INT, JAVA_INT, JAVA_INT, JAVA_INT, JAVA_INT, JAVA_DOUBLE, ADDRESS,
                JAVA_INT, JAVA_DOUBLE, ADDRESS, JAVA_INT);

        /**
         * Linked as a critical function, so it is passed the Java arrays in place, without copying. The JVM
         * can't reach a safepoint (and so can't collect garbage) until the call returns.
         */
        static final MethodHandle DSYRK_CRITICAL = NativeLibrary.downcall("cblas_dsyrk", DESCRIPTOR, true);
        /**
         * The same function, not linked as critical. It must be passed native memory, but the JVM can reach
         * safepoints during the call.
         */
        static final MethodHandle DSYRK_OFF_HEAP = NativeLibrary.downcall("cblas_dsyrk", DESCRIPTOR, false);

        private static void invokeCritical(final boolean upper, final boolean transpose, final int n, final int k, final double alpha, final double[] a,
                final int offsetA, final int lda, final double beta, final double[] c, final int offsetC, final int ldc) {

            MemorySegment segmentA = transpose ? CBLAS.ofMatrix(a, offsetA, k, n, lda) : CBLAS.ofMatrix(a, offsetA, n, k, lda);
            MemorySegment segmentC = CBLAS.ofMatrix(c, offsetC, n, n, ldc);

            try {
                DSYRK_CRITICAL.invokeExact(CBLAS.COLUMN_MAJOR, CBLAS.triangle(upper), CBLAS.transpose(transpose), n, k, alpha, segmentA, lda, beta, segmentC,
                        ldc);
            } catch (Throwable cause) {
                throw new ProgrammingError(cause);
            }
        }

        /**
         * Copies the matrices to native memory, calls {@link #DSYRK_OFF_HEAP} and copies the referenced
         * triangle of C back. When beta is 0, C is not copied to native memory. Requires n, k > 0.
         */
        private static void invokeOffHeap(final boolean upper, final boolean transpose, final int n, final int k, final double alpha, final double[] a,
                final int offsetA, final int lda, final double beta, final double[] c, final int offsetC, final int ldc) {

            try (Arena arena = Arena.ofConfined()) {

                int copyLDA = CBLAS.leadingDimensionOfCopy(transpose ? k : n);
                int copyLDC = CBLAS.leadingDimensionOfCopy(n);

                MemorySegment segmentA = transpose ? CBLAS.copyOfMatrix(arena, a, offsetA, k, n, lda, copyLDA)
                        : CBLAS.copyOfMatrix(arena, a, offsetA, n, k, lda, copyLDA);
                MemorySegment segmentC = beta == ZERO ? CBLAS.allocateMatrix(arena, n, n, copyLDC) : CBLAS.copyOfMatrix(arena, c, offsetC, n, n, ldc, copyLDC);

                DSYRK_OFF_HEAP.invokeExact(CBLAS.COLUMN_MAJOR, CBLAS.triangle(upper), CBLAS.transpose(transpose), n, k, alpha, segmentA, copyLDA, beta,
                        segmentC, copyLDC);

                CBLAS.copyBackTriangle(segmentC, copyLDC, c, offsetC, n, ldc, upper);

            } catch (RuntimeException | Error cause) {
                throw cause;
            } catch (Throwable cause) {
                throw new ProgrammingError(cause);
            }
        }

        /**
         * Critical, with the Java arrays in place, or off-heap, with copies in native memory, when
         * {@link SYRK#isOffHeap(double, double, double)}. A must be within its array also when alpha is 0.
         */
        static void invoke(final boolean upper, final boolean transpose, final int n, final int k, final double alpha, final double[] a, final int offsetA,
                final int lda, final double beta, final double[] c, final int offsetC, final int ldc) {
            if (SYRK.isOffHeap(n, n, k)) {
                Native.invokeOffHeap(upper, transpose, n, k, alpha, a, offsetA, lda, beta, c, offsetC, ldc);
            } else {
                Native.invokeCritical(upper, transpose, n, k, alpha, a, offsetA, lda, beta, c, offsetC, ldc);
            }
        }

    }

    /**
     * The native implementation is used, if there is one, when the number of multiply-adds is larger than
     * this. Not measured yet – the value is GEMM's.
     */
    private static final double NATIVE_THRESHOLD = 10_000;

    static final boolean NATIVE = NativeLibrary.isAvailable() && Native.DSYRK_CRITICAL != null;
    /**
     * The native implementation is linked as a critical function, and passed the Java arrays in place. A
     * critical call blocks safepoints: garbage collection, in the whole JVM, waits until it returns. Long and
     * compute-intensive calls – the number of multiply-adds larger than {@link #OFF_HEAP_THRESHOLD} and the
     * smaller of n and k larger than this – are instead passed copies of the matrices in native memory, and
     * the function is called without blocking safepoints. The copies then cost little compared to the call.
     * Not measured yet – the values are GEMM's.
     */
    static double OFF_HEAP_DIMENSION = 300;
    static double OFF_HEAP_THRESHOLD = 3E9;

    /**
     * {@code dsyrk}: C := alpha*A*A<sup>T</sup> + beta*C (no transpose) or C := alpha*A<sup>T</sup>*A +
     * beta*C (transpose)
     * <p>
     * All matrices are column-major, stored in their arrays from an offset with a leading dimension. Only the
     * upper or lower triangle of C is read and updated. When beta is 0, C need not be set on entry; when
     * alpha is 0, A is not read.
     *
     * @param upper     The upper triangle of C is updated if true, otherwise the lower
     * @param transpose A is k-by-n if true, otherwise n-by-k
     * @param n         The order of C
     * @param k         The number of columns of A (rows if transposed)
     */
    public static void invoke(final boolean upper, final boolean transpose, final int n, final int k, final double alpha, final double[] a, final int offsetA,
            final int lda, final double beta, final double[] c, final int offsetC, final int ldc) {

        ArrayOperation.checkArgument("SYRK", "n", n >= 0);
        ArrayOperation.checkArgument("SYRK", "k", k >= 0);
        ArrayOperation.checkArgument("SYRK", "lda", lda >= Math.max(1, transpose ? k : n));
        ArrayOperation.checkArgument("SYRK", "ldc", ldc >= Math.max(1, n));

        if (n == 0 || (alpha == ZERO || k == 0) && beta == ONE) {
            return;
        }

        // With alpha 0, A is not read and the Java implementation does what remains
        if (alpha != ZERO && SYRK.isNative(n, n, k)) {
            Native.invoke(upper, transpose, n, k, alpha, a, offsetA, lda, beta, c, offsetC, ldc);
        } else {
            JVM.invoke(upper, transpose, n, k, alpha, a, offsetA, lda, beta, c, offsetC, ldc);
        }
    }

    /**
     * Whether to use the native implementation: there is one, and n*n*k is larger than
     * {@link #NATIVE_THRESHOLD}.
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

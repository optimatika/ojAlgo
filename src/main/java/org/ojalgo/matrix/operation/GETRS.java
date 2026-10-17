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

import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_BYTE;
import static java.lang.foreign.ValueLayout.JAVA_INT;
import static java.lang.foreign.ValueLayout.JAVA_LONG;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;

import org.ojalgo.ProgrammingError;
import org.ojalgo.array.operation.ArrayOperation;
import org.ojalgo.array.operation.NativeLibrary;
import org.ojalgo.concurrent.DivideAndConquer;

/**
 * LAPACK {@code ?getrs}: solves A*X = B or A<sup>T</sup>*X = B, for a general n-by-n matrix A, using the LU
 * factorisation A = P*L*U computed by {@link GETRF}. X overwrites B.
 *
 * @see NativeLibrary
 */
public abstract class GETRS implements LAPACK {

    /**
     * The pure Java implementation. Everything it calls is pure Java as well – the {@code JVM}
     * implementations of other operations, never their dispatching {@code invoke} methods (that may call
     * native code).
     * <p>
     * Each column of B is solved for separately, with the row interchanges applied and the two triangular
     * systems solved by {@link TRSV}. The columns are solved in parallel when there are more than
     * {@link GETRS#THRESHOLD} of them.
     */
    static final class JVM {

        private static void interchange(final int n, final int[] ipiv, final int offsetIpiv, final double[] b, final int column, final boolean reverse) {
            for (int k = 0; k < n; k++) {
                int i = reverse ? n - 1 - k : k;
                int row = ipiv[offsetIpiv + i] - 1;
                if (row != i) {
                    double tmp = b[column + i];
                    b[column + i] = b[column + row];
                    b[column + row] = tmp;
                }
            }
        }

        private static void solve(final boolean transpose, final int n, final double[] a, final int offsetA, final int lda, final int[] ipiv,
                final int offsetIpiv, final double[] b, final int offsetB, final int ldb, final int first, final int limit) {
            for (int j = first; j < limit; j++) {
                int column = offsetB + j * ldb;
                if (transpose) {
                    TRSV.JVM.invoke(true, true, false, n, a, offsetA, lda, b, column, 1);
                    TRSV.JVM.invoke(false, true, true, n, a, offsetA, lda, b, column, 1);
                    JVM.interchange(n, ipiv, offsetIpiv, b, column, true);
                } else {
                    JVM.interchange(n, ipiv, offsetIpiv, b, column, false);
                    TRSV.JVM.invoke(false, false, true, n, a, offsetA, lda, b, column, 1);
                    TRSV.JVM.invoke(true, false, false, n, a, offsetA, lda, b, column, 1);
                }
            }
        }

        /**
         * The pure Java implementation of
         * {@link GETRS#invoke(boolean, int, int, double[], int, int, int[], int, double[], int, int)},
         * without argument checks, for n, nrhs > 0.
         */
        static void invoke(final boolean transpose, final int n, final int nrhs, final double[] a, final int offsetA, final int lda, final int[] ipiv,
                final int offsetIpiv, final double[] b, final int offsetB, final int ldb) {
            if (nrhs > THRESHOLD) {

                DivideAndConquer conquerer = new DivideAndConquer() {

                    @Override
                    protected void conquer(final int first, final int limit) {
                        JVM.solve(transpose, n, a, offsetA, lda, ipiv, offsetIpiv, b, offsetB, ldb, first, limit);
                    }
                };

                conquerer.invoke(0, nrhs, THRESHOLD);

            } else {

                JVM.solve(transpose, n, a, offsetA, lda, ipiv, offsetIpiv, b, offsetB, ldb, 0, nrhs);
            }
        }

    }

    /**
     * Native {@code dgetrs}, initialised only when {@link NativeLibrary#isAvailable()}. All arguments are
     * passed by reference, as to any Fortran routine, followed by the length of the character argument.
     */
    static final class Native {

        private static final FunctionDescriptor DESCRIPTOR = FunctionDescriptor.ofVoid(ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS,
                ADDRESS, JAVA_LONG);

        /**
         * Linked as a critical function, so it is passed the Java arrays in place, without copying. The JVM
         * can't reach a safepoint (and so can't collect garbage) until the call returns.
         */
        static final MethodHandle DGETRS = NativeLibrary.downcall("dgetrs_", DESCRIPTOR, true);
        /**
         * The same function, not linked as critical. It must be passed native memory, but the JVM can reach
         * safepoints during the call.
         */
        static final MethodHandle DGETRS_OFF_HEAP = NativeLibrary.downcall("dgetrs_", DESCRIPTOR, false);

        private static byte trans(final boolean transpose) {
            return (byte) (transpose ? 'T' : 'N');
        }

        static void invoke(final boolean transpose, final int n, final int nrhs, final double[] a, final int offsetA, final int lda, final int[] ipiv,
                final int offsetIpiv, final double[] b, final int offsetB, final int ldb) {

            MemorySegment segmentA = CBLAS.ofMatrix(a, offsetA, n, n, lda);
            MemorySegment segmentIPIV = NativeLibrary.ofArray(ipiv, offsetIpiv, n);
            MemorySegment segmentB = CBLAS.ofMatrix(b, offsetB, n, nrhs, ldb);

            try {
                DGETRS.invokeExact(MemorySegment.ofArray(new byte[] { Native.trans(transpose) }), MemorySegment.ofArray(new int[] { n }),
                        MemorySegment.ofArray(new int[] { nrhs }), segmentA, MemorySegment.ofArray(new int[] { lda }), segmentIPIV, segmentB,
                        MemorySegment.ofArray(new int[] { ldb }), MemorySegment.ofArray(new int[1]), 1L);
            } catch (Throwable cause) {
                throw new ProgrammingError(cause);
            }
        }

        /**
         * Copies A, the pivots and B to native memory, calls {@link #DGETRS_OFF_HEAP} and copies X back.
         * Requires n, nrhs > 0.
         */
        static void invokeOffHeap(final boolean transpose, final int n, final int nrhs, final double[] a, final int offsetA, final int lda, final int[] ipiv,
                final int offsetIpiv, final double[] b, final int offsetB, final int ldb) {

            try (Arena arena = Arena.ofConfined()) {

                MemorySegment segmentA = CBLAS.copyOfMatrix(arena, a, offsetA, n, n, lda);
                MemorySegment segmentIPIV = arena.allocateFrom(JAVA_INT, MemorySegment.ofArray(ipiv), JAVA_INT, JAVA_INT.byteSize() * offsetIpiv, n);
                MemorySegment segmentB = CBLAS.copyOfMatrix(arena, b, offsetB, n, nrhs, ldb);
                // The copies are packed: their leading dimensions are n
                MemorySegment segmentN = arena.allocateFrom(JAVA_INT, n);

                DGETRS_OFF_HEAP.invokeExact(arena.allocateFrom(JAVA_BYTE, Native.trans(transpose)), segmentN, arena.allocateFrom(JAVA_INT, nrhs), segmentA,
                        segmentN, segmentIPIV, segmentB, segmentN, arena.allocate(JAVA_INT), 1L);

                CBLAS.copyBackMatrix(segmentB, b, offsetB, n, nrhs, ldb);

            } catch (RuntimeException | Error cause) {
                throw cause;
            } catch (Throwable cause) {
                throw new ProgrammingError(cause);
            }
        }

    }

    /**
     * More right hand sides than this are solved for together: natively, see {@link #NATIVE_THRESHOLD}, or by
     * the Java implementation. Fewer are solved for one at a time, each with the row interchanges and two
     * calls to {@link TRSV} – that decides between its native and Java implementations.
     */
    private static final int COLUMNS_THRESHOLD = 7;
    /**
     * The native implementation is used, if there is one, when n*n*nrhs (the number of multiply-adds) is
     * larger than this.
     */
    private static final double NATIVE_THRESHOLD = 150;
    /**
     * The native implementation is linked as a critical function, and passed the Java arrays in place. A
     * critical call blocks safepoints: garbage collection, in the whole JVM, waits until it returns. Long and
     * compute-intensive calls – n*n*nrhs larger than {@link #OFF_HEAP_THRESHOLD} and the smaller of n and
     * nrhs larger than this – are instead passed copies of the matrices in native memory, and the function is
     * called without blocking safepoints. The copies then cost little compared to the call.
     */
    private static final double OFF_HEAP_DIMENSION = 300;
    private static final double OFF_HEAP_THRESHOLD = 3E9;

    static final boolean NATIVE = NativeLibrary.isAvailable() && Native.DGETRS != null;

    /**
     * The Java implementation solves the columns of B in parallel when there are more than this many of them.
     */
    static int THRESHOLD = 64;

    /**
     * {@code dgetrs}: solves op(A)*X = B, X overwrites B
     * <p>
     * A and B are column-major, stored in their arrays from an offset with a leading dimension. A holds the
     * factors L and U, and ipiv the row interchanges, as returned by {@link GETRF}. No test for singularity
     * is done.
     *
     * @param transpose op(A) = A<sup>T</sup> if true, otherwise A
     * @param n         The order of A
     * @param nrhs      The number of right hand sides (columns of B)
     */
    public static void invoke(final boolean transpose, final int n, final int nrhs, final double[] a, final int offsetA, final int lda, final int[] ipiv,
            final int offsetIpiv, final double[] b, final int offsetB, final int ldb) {

        ArrayOperation.checkArgument("GETRS", "n", n >= 0);
        ArrayOperation.checkArgument("GETRS", "nrhs", nrhs >= 0);
        ArrayOperation.checkArgument("GETRS", "lda", lda >= Math.max(1, n));
        ArrayOperation.checkArgument("GETRS", "ldb", ldb >= Math.max(1, n));

        if (n == 0 || nrhs == 0) {
            return;
        }

        if (nrhs <= COLUMNS_THRESHOLD) {
            GETRS.solveColumns(transpose, n, nrhs, a, offsetA, lda, ipiv, offsetIpiv, b, offsetB, ldb);
        } else if (GETRS.isNative(n, nrhs)) {
            if (GETRS.isOffHeap(n, nrhs)) {
                Native.invokeOffHeap(transpose, n, nrhs, a, offsetA, lda, ipiv, offsetIpiv, b, offsetB, ldb);
            } else {
                Native.invoke(transpose, n, nrhs, a, offsetA, lda, ipiv, offsetIpiv, b, offsetB, ldb);
            }
        } else {
            JVM.invoke(transpose, n, nrhs, a, offsetA, lda, ipiv, offsetIpiv, b, offsetB, ldb);
        }
    }

    /**
     * The right hand sides one at a time, each with the row interchanges and two calls to the dispatching
     * {@link TRSV#invoke(boolean, boolean, boolean, int, double[], int, int, double[], int, int)}.
     */
    private static void solveColumns(final boolean transpose, final int n, final int nrhs, final double[] a, final int offsetA, final int lda, final int[] ipiv,
            final int offsetIpiv, final double[] b, final int offsetB, final int ldb) {
        for (int j = 0; j < nrhs; j++) {
            int column = offsetB + j * ldb;
            if (transpose) {
                TRSV.invoke(true, true, false, n, a, offsetA, lda, b, column, 1);
                TRSV.invoke(false, true, true, n, a, offsetA, lda, b, column, 1);
                JVM.interchange(n, ipiv, offsetIpiv, b, column, true);
            } else {
                JVM.interchange(n, ipiv, offsetIpiv, b, column, false);
                TRSV.invoke(false, false, true, n, a, offsetA, lda, b, column, 1);
                TRSV.invoke(true, false, false, n, a, offsetA, lda, b, column, 1);
            }
        }
    }

    /**
     * Whether to use the native implementation: there is one, and n*n*nrhs is larger than
     * {@link #NATIVE_THRESHOLD}.
     */
    static boolean isNative(final double n, final double nrhs) {
        return NATIVE && n * n * nrhs > NATIVE_THRESHOLD;
    }

    /**
     * Whether to call the native implementation with copies of the matrices in native memory, rather than
     * critically with the Java arrays in place, see {@link #OFF_HEAP_DIMENSION}.
     */
    static boolean isOffHeap(final double n, final double nrhs) {
        return n * n * nrhs > OFF_HEAP_THRESHOLD && Math.min(n, nrhs) > OFF_HEAP_DIMENSION;
    }

}

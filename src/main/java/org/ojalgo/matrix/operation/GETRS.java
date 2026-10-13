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
import java.util.Arrays;

import org.ojalgo.ProgrammingError;
import org.ojalgo.array.operation.ArrayOperation;
import org.ojalgo.array.operation.NativeLibrary;
import org.ojalgo.concurrent.DivideAndConquer;
import org.ojalgo.concurrent.Parallelism;
import org.ojalgo.scalar.Scalar;

/**
 * LAPACK {@code ?getrs}: solves A*X = B or A<sup>T</sup>*X = B, for a general n-by-n matrix A, using the LU
 * factorisation A = P*L*U computed by {@link GETRF}. X overwrites B. Also solves A*X = I, writing the inverse
 * of A to a separate matrix.
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
     * systems solved by {@link TRSV}. The columns are solved in parallel when that is large, see
     * {@link GETRS#THRESHOLD}. With B = I, each column of the identity, with the row interchanges applied,
     * has a single non-zero element; the system with L is solved from that row on.
     */
    static final class JVM {

        private static final DivideAndConquer.Divider DIVIDER = Parallelism.CORES.newDivider();

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

        private static <N extends Scalar<N>> void interchange(final int n, final int[] ipiv, final int offsetIpiv, final N[] b, final int column,
                final boolean reverse) {
            for (int k = 0; k < n; k++) {
                int i = reverse ? n - 1 - k : k;
                int row = ipiv[offsetIpiv + i] - 1;
                if (row != i) {
                    N tmp = b[column + i];
                    b[column + i] = b[column + row];
                    b[column + row] = tmp;
                }
            }
        }

        /**
         * The rows of the identity in the order the interchanges put them, and then, inverted, the row each
         * column's non-zero element ends up in.
         */
        private static int[] positions(final int n, final int[] ipiv, final int offsetIpiv) {

            int[] order = new int[n];
            for (int i = 0; i < n; i++) {
                order[i] = i;
            }
            for (int i = 0; i < n; i++) {
                int row = ipiv[offsetIpiv + i] - 1;
                int tmp = order[i];
                order[i] = order[row];
                order[row] = tmp;
            }

            int[] retVal = new int[n];
            for (int i = 0; i < n; i++) {
                retVal[order[i]] = i;
            }
            return retVal;
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

        private static <N extends Scalar<N>> void solve(final boolean transpose, final int n, final N[] a, final int offsetA, final int lda, final int[] ipiv,
                final int offsetIpiv, final N[] b, final int offsetB, final int ldb, final int first, final int limit, final Scalar.Factory<N> scalar) {
            for (int j = first; j < limit; j++) {
                int column = offsetB + j * ldb;
                if (transpose) {
                    TRSV.JVM.invoke(true, true, false, n, a, offsetA, lda, b, column, 1, scalar);
                    TRSV.JVM.invoke(false, true, true, n, a, offsetA, lda, b, column, 1, scalar);
                    JVM.interchange(n, ipiv, offsetIpiv, b, column, true);
                } else {
                    JVM.interchange(n, ipiv, offsetIpiv, b, column, false);
                    TRSV.JVM.invoke(false, false, true, n, a, offsetA, lda, b, column, 1, scalar);
                    TRSV.JVM.invoke(true, false, false, n, a, offsetA, lda, b, column, 1, scalar);
                }
            }
        }

        private static void solveIdentity(final int n, final double[] a, final int offsetA, final int lda, final int[] position, final double[] b,
                final int offsetB, final int ldb, final int first, final int limit) {
            for (int j = first; j < limit; j++) {
                int column = offsetB + j * ldb;
                int row = position[j];
                Arrays.fill(b, column, column + n, ZERO);
                b[column + row] = ONE;
                TRSV.JVM.invoke(false, false, true, n - row, a, offsetA + row + row * lda, lda, b, column + row, 1);
                TRSV.JVM.invoke(true, false, false, n, a, offsetA, lda, b, column, 1);
            }
        }

        private static <N extends Scalar<N>> void solveIdentity(final int n, final N[] a, final int offsetA, final int lda, final int[] position, final N[] b,
                final int offsetB, final int ldb, final int first, final int limit, final Scalar.Factory<N> scalar) {
            N zero = scalar.zero().get();
            for (int j = first; j < limit; j++) {
                int column = offsetB + j * ldb;
                int row = position[j];
                Arrays.fill(b, column, column + n, zero);
                b[column + row] = scalar.one().get();
                TRSV.JVM.invoke(false, false, true, n - row, a, offsetA + row + row * lda, lda, b, column + row, 1, scalar);
                TRSV.JVM.invoke(true, false, false, n, a, offsetA, lda, b, column, 1, scalar);
            }
        }

        /**
         * The pure Java implementation of
         * {@link GETRS#invoke(boolean, int, int, double[], int, int, int[], int, double[], int, int)},
         * without argument checks, for n, nrhs > 0.
         */
        static void invoke(final boolean transpose, final int n, final int nrhs, final double[] a, final int offsetA, final int lda, final int[] ipiv,
                final int offsetIpiv, final double[] b, final int offsetB, final int ldb) {
            int chunk = MatrixOperation.chunk(THRESHOLD, n, n);

            if (nrhs > chunk) {

                DIVIDER.threshold(chunk).divide(0, nrhs,
                        (first, limit) -> JVM.solve(transpose, n, a, offsetA, lda, ipiv, offsetIpiv, b, offsetB, ldb, first, limit));

            } else {

                JVM.solve(transpose, n, a, offsetA, lda, ipiv, offsetIpiv, b, offsetB, ldb, 0, nrhs);
            }
        }

        /**
         * The pure Java implementation of
         * {@link GETRS#invoke(boolean, int, int, Scalar[], int, int, int[], int, Scalar[], int, int, Scalar.Factory)},
         * without argument checks, for n, nrhs > 0.
         */
        static <N extends Scalar<N>> void invoke(final boolean transpose, final int n, final int nrhs, final N[] a, final int offsetA, final int lda,
                final int[] ipiv, final int offsetIpiv, final N[] b, final int offsetB, final int ldb, final Scalar.Factory<N> scalar) {
            int chunk = MatrixOperation.chunk(THRESHOLD, n, n);

            if (nrhs > chunk) {

                DIVIDER.threshold(chunk).divide(0, nrhs,
                        (first, limit) -> JVM.solve(transpose, n, a, offsetA, lda, ipiv, offsetIpiv, b, offsetB, ldb, first, limit, scalar));

            } else {

                JVM.solve(transpose, n, a, offsetA, lda, ipiv, offsetIpiv, b, offsetB, ldb, 0, nrhs, scalar);
            }
        }

        /**
         * The pure Java implementation of
         * {@link GETRS#invokeIdentity(int, double[], int, int, int[], int, double[], int, int)}, without
         * argument checks, for n > 0.
         */
        static void invokeIdentity(final int n, final double[] a, final int offsetA, final int lda, final int[] ipiv, final int offsetIpiv, final double[] b,
                final int offsetB, final int ldb) {

            int[] position = JVM.positions(n, ipiv, offsetIpiv);

            // The system with L is solved from the non-zero element on: 2/3 of n*n multiply-adds per column, on average
            int chunk = MatrixOperation.chunk(THRESHOLD, 2.0 * n * n / 3.0);

            if (n > chunk) {

                DIVIDER.threshold(chunk).divide(0, n, (first, limit) -> JVM.solveIdentity(n, a, offsetA, lda, position, b, offsetB, ldb, first, limit));

            } else {

                JVM.solveIdentity(n, a, offsetA, lda, position, b, offsetB, ldb, 0, n);
            }
        }

        /**
         * The pure Java implementation of
         * {@link GETRS#invokeIdentity(int, Scalar[], int, int, int[], int, Scalar[], int, int, Scalar.Factory)},
         * without argument checks, for n > 0.
         */
        static <N extends Scalar<N>> void invokeIdentity(final int n, final N[] a, final int offsetA, final int lda, final int[] ipiv, final int offsetIpiv,
                final N[] b, final int offsetB, final int ldb, final Scalar.Factory<N> scalar) {

            int[] position = JVM.positions(n, ipiv, offsetIpiv);

            int chunk = MatrixOperation.chunk(THRESHOLD, 2.0 * n * n / 3.0);

            if (n > chunk) {

                DIVIDER.threshold(chunk).divide(0, n, (first, limit) -> JVM.solveIdentity(n, a, offsetA, lda, position, b, offsetB, ldb, first, limit, scalar));

            } else {

                JVM.solveIdentity(n, a, offsetA, lda, position, b, offsetB, ldb, 0, n, scalar);
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
        static final MethodHandle DGETRS_CRITICAL = NativeLibrary.downcall("dgetrs_", DESCRIPTOR, true);
        /**
         * The same function, not linked as critical. It must be passed native memory, but the JVM can reach
         * safepoints during the call.
         */
        static final MethodHandle DGETRS_OFF_HEAP = NativeLibrary.downcall("dgetrs_", DESCRIPTOR, false);

        private static void invokeCritical(final boolean transpose, final int n, final int nrhs, final double[] a, final int offsetA, final int lda,
                final int[] ipiv, final int offsetIpiv, final double[] b, final int offsetB, final int ldb) {

            MemorySegment segmentA = CBLAS.ofMatrix(a, offsetA, n, n, lda);
            MemorySegment segmentIPIV = NativeLibrary.ofArray(ipiv, offsetIpiv, n);
            MemorySegment segmentB = CBLAS.ofMatrix(b, offsetB, n, nrhs, ldb);

            try {
                DGETRS_CRITICAL.invokeExact(MemorySegment.ofArray(new byte[] { Native.trans(transpose) }), MemorySegment.ofArray(new int[] { n }),
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
        private static void invokeOffHeap(final boolean transpose, final int n, final int nrhs, final double[] a, final int offsetA, final int lda,
                final int[] ipiv, final int offsetIpiv, final double[] b, final int offsetB, final int ldb) {

            try (Arena arena = Arena.ofConfined()) {

                int copyLDA = CBLAS.leadingDimensionOfCopy(n);
                int copyLDB = CBLAS.leadingDimensionOfCopy(n);

                MemorySegment segmentA = CBLAS.copyOfMatrix(arena, a, offsetA, n, n, lda, copyLDA);
                MemorySegment segmentIPIV = arena.allocateFrom(JAVA_INT, MemorySegment.ofArray(ipiv), JAVA_INT, JAVA_INT.byteSize() * offsetIpiv, n);
                MemorySegment segmentB = CBLAS.copyOfMatrix(arena, b, offsetB, n, nrhs, ldb, copyLDB);
                MemorySegment segmentN = arena.allocateFrom(JAVA_INT, n);
                MemorySegment segmentLDA = arena.allocateFrom(JAVA_INT, copyLDA);
                MemorySegment segmentLDB = arena.allocateFrom(JAVA_INT, copyLDB);

                DGETRS_OFF_HEAP.invokeExact(arena.allocateFrom(JAVA_BYTE, Native.trans(transpose)), segmentN, arena.allocateFrom(JAVA_INT, nrhs), segmentA,
                        segmentLDA, segmentIPIV, segmentB, segmentLDB, arena.allocate(JAVA_INT), 1L);

                CBLAS.copyBackMatrix(segmentB, copyLDB, b, offsetB, n, nrhs, ldb);

            } catch (RuntimeException | Error cause) {
                throw cause;
            } catch (Throwable cause) {
                throw new ProgrammingError(cause);
            }
        }

        private static byte trans(final boolean transpose) {
            return (byte) (transpose ? 'T' : 'N');
        }

        /**
         * Critical, with the Java arrays in place, or off-heap, with copies in native memory, when
         * {@link GETRS#isOffHeap(double, double)}.
         */
        static void invoke(final boolean transpose, final int n, final int nrhs, final double[] a, final int offsetA, final int lda, final int[] ipiv,
                final int offsetIpiv, final double[] b, final int offsetB, final int ldb) {
            if (GETRS.isOffHeap(n, nrhs)) {
                Native.invokeOffHeap(transpose, n, nrhs, a, offsetA, lda, ipiv, offsetIpiv, b, offsetB, ldb);
            } else {
                Native.invokeCritical(transpose, n, nrhs, a, offsetA, lda, ipiv, offsetIpiv, b, offsetB, ldb);
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

    static final boolean NATIVE = NativeLibrary.isAvailable() && Native.DGETRS_CRITICAL != null;
    /**
     * The native implementation is linked as a critical function, and passed the Java arrays in place. A
     * critical call blocks safepoints: garbage collection, in the whole JVM, waits until it returns. Long and
     * compute-intensive calls – n*n*nrhs larger than {@link #OFF_HEAP_THRESHOLD} and the smaller of n and
     * nrhs larger than this – are instead passed copies of the matrices in native memory, and the function is
     * called without blocking safepoints. The copies then cost little compared to the call.
     */
    static double OFF_HEAP_DIMENSION = 300;
    static double OFF_HEAP_THRESHOLD = 3E9;

    /**
     * The Java implementation solves the columns of B in parallel when n*n*nrhs (the number of multiply-adds)
     * is larger than this cubed. Each part then gets more than half of that.
     */
    static int THRESHOLD = 40;

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
            Native.invoke(transpose, n, nrhs, a, offsetA, lda, ipiv, offsetIpiv, b, offsetB, ldb);
        } else {
            JVM.invoke(transpose, n, nrhs, a, offsetA, lda, ipiv, offsetIpiv, b, offsetB, ldb);
        }
    }

    /**
     * As {@link #invoke(boolean, int, int, double[], int, int, int[], int, double[], int, int)}, for other
     * number types, with the factors from
     * {@link GETRF#invoke(int, int, Scalar[], int, int, int[], int, Scalar.Factory)}. There is only a pure
     * Java implementation.
     *
     * @param transpose op(A) = A<sup>H</sup>, the conjugate transpose, if true, otherwise A
     */
    public static <N extends Scalar<N>> void invoke(final boolean transpose, final int n, final int nrhs, final N[] a, final int offsetA, final int lda,
            final int[] ipiv, final int offsetIpiv, final N[] b, final int offsetB, final int ldb, final Scalar.Factory<N> scalar) {

        ArrayOperation.checkArgument("GETRS", "n", n >= 0);
        ArrayOperation.checkArgument("GETRS", "nrhs", nrhs >= 0);
        ArrayOperation.checkArgument("GETRS", "lda", lda >= Math.max(1, n));
        ArrayOperation.checkArgument("GETRS", "ldb", ldb >= Math.max(1, n));

        if (n == 0 || nrhs == 0) {
            return;
        }

        JVM.invoke(transpose, n, nrhs, a, offsetA, lda, ipiv, offsetIpiv, b, offsetB, ldb, scalar);
    }

    /**
     * {@code dgetrs} with B = I: X, the inverse of A, is written to B
     * <p>
     * A and B are column-major, stored in their arrays from an offset with a leading dimension. A holds the
     * factors L and U, and ipiv the row interchanges, as returned by {@link GETRF}; they are not changed. B
     * need not be set on entry. No test for singularity is done.
     *
     * @param n The order of A and B
     */
    public static void invokeIdentity(final int n, final double[] a, final int offsetA, final int lda, final int[] ipiv, final int offsetIpiv, final double[] b,
            final int offsetB, final int ldb) {

        ArrayOperation.checkArgument("GETRS", "n", n >= 0);
        ArrayOperation.checkArgument("GETRS", "lda", lda >= Math.max(1, n));
        ArrayOperation.checkArgument("GETRS", "ldb", ldb >= Math.max(1, n));

        if (n == 0) {
            return;
        }

        if (n > COLUMNS_THRESHOLD && GETRS.isNative(n, n)) {
            for (int j = 0; j < n; j++) {
                int column = offsetB + j * ldb;
                Arrays.fill(b, column, column + n, ZERO);
                b[column + j] = ONE;
            }
            Native.invoke(false, n, n, a, offsetA, lda, ipiv, offsetIpiv, b, offsetB, ldb);
        } else {
            JVM.invokeIdentity(n, a, offsetA, lda, ipiv, offsetIpiv, b, offsetB, ldb);
        }
    }

    /**
     * As {@link #invokeIdentity(int, double[], int, int, int[], int, double[], int, int)}, for other number
     * types, with the factors from
     * {@link GETRF#invoke(int, int, Scalar[], int, int, int[], int, Scalar.Factory)}. There is only a pure
     * Java implementation.
     */
    public static <N extends Scalar<N>> void invokeIdentity(final int n, final N[] a, final int offsetA, final int lda, final int[] ipiv, final int offsetIpiv,
            final N[] b, final int offsetB, final int ldb, final Scalar.Factory<N> scalar) {

        ArrayOperation.checkArgument("GETRS", "n", n >= 0);
        ArrayOperation.checkArgument("GETRS", "lda", lda >= Math.max(1, n));
        ArrayOperation.checkArgument("GETRS", "ldb", ldb >= Math.max(1, n));

        if (n == 0) {
            return;
        }

        JVM.invokeIdentity(n, a, offsetA, lda, ipiv, offsetIpiv, b, offsetB, ldb, scalar);
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

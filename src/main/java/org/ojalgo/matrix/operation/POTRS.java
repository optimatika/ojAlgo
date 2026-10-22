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
 * LAPACK {@code ?potrs}: solves A*X = B, for a symmetric positive definite n-by-n matrix A, using the
 * Cholesky factorisation A = L*L<sup>T</sup> or A = U<sup>T</sup>*U computed by {@link POTRF}. X overwrites
 * B. Also solves A*X = I, writing the inverse of A to a separate matrix.
 *
 * @see NativeLibrary
 */
public abstract class POTRS implements LAPACK {

    /**
     * The pure Java implementation. Everything it calls is pure Java as well – the {@code JVM}
     * implementations of other operations, never their dispatching {@code invoke} methods (that may call
     * native code).
     * <p>
     * Each column of B is solved for separately, with the two triangular systems solved by {@link TRSV}. The
     * columns are solved in parallel when that is large, see {@link POTRS#THRESHOLD}. With B = I, the
     * solution is symmetric: column j is solved for from row j on only (both triangular systems), and the
     * rest copied from the rows of the columns before it.
     */
    static final class JVM {

        private static final DivideAndConquer.Divider DIVIDER = Parallelism.CORES.newDivider();

        private static void solve(final boolean upper, final int n, final double[] a, final int offsetA, final int lda, final double[] b, final int offsetB,
                final int ldb, final int first, final int limit) {
            for (int j = first; j < limit; j++) {
                int column = offsetB + j * ldb;
                TRSV.JVM.invoke(upper, upper, false, n, a, offsetA, lda, b, column, 1);
                TRSV.JVM.invoke(upper, !upper, false, n, a, offsetA, lda, b, column, 1);
            }
        }

        private static <N extends Scalar<N>> void solve(final boolean upper, final int n, final N[] a, final int offsetA, final int lda, final N[] b,
                final int offsetB, final int ldb, final int first, final int limit, final Scalar.Factory<N> scalar) {
            for (int j = first; j < limit; j++) {
                int column = offsetB + j * ldb;
                TRSV.JVM.invoke(upper, upper, false, n, a, offsetA, lda, b, column, 1, scalar);
                TRSV.JVM.invoke(upper, !upper, false, n, a, offsetA, lda, b, column, 1, scalar);
            }
        }

        private static void solveIdentity(final boolean upper, final int n, final double[] a, final int offsetA, final int lda, final double[] b,
                final int offsetB, final int ldb, final int first, final int limit) {
            for (int j = first; j < limit; j++) {
                int column = offsetB + j * ldb;
                int trailing = offsetA + j + j * lda;
                Arrays.fill(b, column, column + n, ZERO);
                b[column + j] = ONE;
                TRSV.JVM.invoke(upper, upper, false, n - j, a, trailing, lda, b, column + j, 1);
                TRSV.JVM.invoke(upper, !upper, false, n - j, a, trailing, lda, b, column + j, 1);
            }
        }

        private static <N extends Scalar<N>> void solveIdentity(final boolean upper, final int n, final N[] a, final int offsetA, final int lda, final N[] b,
                final int offsetB, final int ldb, final int first, final int limit, final Scalar.Factory<N> scalar) {
            N zero = scalar.zero().get();
            for (int j = first; j < limit; j++) {
                int column = offsetB + j * ldb;
                int trailing = offsetA + j + j * lda;
                Arrays.fill(b, column, column + n, zero);
                b[column + j] = scalar.one().get();
                TRSV.JVM.invoke(upper, upper, false, n - j, a, trailing, lda, b, column + j, 1, scalar);
                TRSV.JVM.invoke(upper, !upper, false, n - j, a, trailing, lda, b, column + j, 1, scalar);
            }
        }

        /**
         * The pure Java implementation of
         * {@link POTRS#invoke(boolean, int, int, double[], int, int, double[], int, int)}, without argument
         * checks, for n, nrhs > 0.
         */
        static void invoke(final boolean upper, final int n, final int nrhs, final double[] a, final int offsetA, final int lda, final double[] b,
                final int offsetB, final int ldb) {
            int chunk = MatrixOperation.chunk(THRESHOLD, n, n);

            if (nrhs > chunk) {

                DIVIDER.threshold(chunk).divide(0, nrhs, (first, limit) -> JVM.solve(upper, n, a, offsetA, lda, b, offsetB, ldb, first, limit));

            } else {

                JVM.solve(upper, n, a, offsetA, lda, b, offsetB, ldb, 0, nrhs);
            }
        }

        /**
         * The pure Java implementation of
         * {@link POTRS#invoke(boolean, int, int, Scalar[], int, int, Scalar[], int, int, Scalar.Factory)},
         * without argument checks, for n, nrhs > 0.
         */
        static <N extends Scalar<N>> void invoke(final boolean upper, final int n, final int nrhs, final N[] a, final int offsetA, final int lda, final N[] b,
                final int offsetB, final int ldb, final Scalar.Factory<N> scalar) {
            int chunk = MatrixOperation.chunk(THRESHOLD, n, n);

            if (nrhs > chunk) {

                DIVIDER.threshold(chunk).divide(0, nrhs, (first, limit) -> JVM.solve(upper, n, a, offsetA, lda, b, offsetB, ldb, first, limit, scalar));

            } else {

                JVM.solve(upper, n, a, offsetA, lda, b, offsetB, ldb, 0, nrhs, scalar);
            }
        }

        /**
         * The pure Java implementation of
         * {@link POTRS#invokeIdentity(boolean, int, double[], int, int, double[], int, int)}, without
         * argument checks, for n > 0.
         */
        static void invokeIdentity(final boolean upper, final int n, final double[] a, final int offsetA, final int lda, final double[] b, final int offsetB,
                final int ldb) {

            // Both systems are solved from the diagonal on: 1/3 of n*n multiply-adds per column, on average
            int chunk = MatrixOperation.chunk(THRESHOLD, n * n / 3.0);

            if (n > chunk) {

                DIVIDER.threshold(chunk).divide(0, n, (first, limit) -> JVM.solveIdentity(upper, n, a, offsetA, lda, b, offsetB, ldb, first, limit));

            } else {

                JVM.solveIdentity(upper, n, a, offsetA, lda, b, offsetB, ldb, 0, n);
            }

            POTRS.copyLowerToUpper(n, b, offsetB, ldb);
        }

        /**
         * The pure Java implementation of
         * {@link POTRS#invokeIdentity(boolean, int, Scalar[], int, int, Scalar[], int, int, Scalar.Factory)},
         * without argument checks, for n > 0.
         */
        static <N extends Scalar<N>> void invokeIdentity(final boolean upper, final int n, final N[] a, final int offsetA, final int lda, final N[] b,
                final int offsetB, final int ldb, final Scalar.Factory<N> scalar) {

            int chunk = MatrixOperation.chunk(THRESHOLD, n * n / 3.0);

            if (n > chunk) {

                DIVIDER.threshold(chunk).divide(0, n, (first, limit) -> JVM.solveIdentity(upper, n, a, offsetA, lda, b, offsetB, ldb, first, limit, scalar));

            } else {

                JVM.solveIdentity(upper, n, a, offsetA, lda, b, offsetB, ldb, 0, n, scalar);
            }

            POTRS.copyLowerToUpper(n, b, offsetB, ldb);
        }

    }

    /**
     * Native {@code dpotrs}, initialised only when {@link NativeLibrary#isAvailable()}. All arguments are
     * passed by reference, as to any Fortran routine, followed by the length of the character argument.
     */
    static final class Native {

        private static final FunctionDescriptor DESCRIPTOR = FunctionDescriptor.ofVoid(ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS,
                JAVA_LONG);

        /**
         * Linked as a critical function, so it is passed the Java arrays in place, without copying. The JVM
         * can't reach a safepoint (and so can't collect garbage) until the call returns.
         */
        static final MethodHandle DPOTRS_CRITICAL = NativeLibrary.downcall("dpotrs_", DESCRIPTOR, true);
        /**
         * The same function, not linked as critical. It must be passed native memory, but the JVM can reach
         * safepoints during the call.
         */
        static final MethodHandle DPOTRS_OFF_HEAP = NativeLibrary.downcall("dpotrs_", DESCRIPTOR, false);

        private static void invokeCritical(final boolean upper, final int n, final int nrhs, final double[] a, final int offsetA, final int lda,
                final double[] b, final int offsetB, final int ldb) {

            MemorySegment segmentA = CBLAS.ofMatrix(a, offsetA, n, n, lda);
            MemorySegment segmentB = CBLAS.ofMatrix(b, offsetB, n, nrhs, ldb);

            try {
                DPOTRS_CRITICAL.invokeExact(MemorySegment.ofArray(new byte[] { Native.uplo(upper) }), MemorySegment.ofArray(new int[] { n }),
                        MemorySegment.ofArray(new int[] { nrhs }), segmentA, MemorySegment.ofArray(new int[] { lda }), segmentB,
                        MemorySegment.ofArray(new int[] { ldb }), MemorySegment.ofArray(new int[1]), 1L);
            } catch (Throwable cause) {
                throw new ProgrammingError(cause);
            }
        }

        /**
         * Copies A and B to native memory, calls {@link #DPOTRS_OFF_HEAP} and copies X back. Requires n, nrhs
         * > 0.
         */
        private static void invokeOffHeap(final boolean upper, final int n, final int nrhs, final double[] a, final int offsetA, final int lda,
                final double[] b, final int offsetB, final int ldb) {

            try (Arena arena = Arena.ofConfined()) {

                int copyLDA = CBLAS.leadingDimensionOfCopy(n);
                int copyLDB = CBLAS.leadingDimensionOfCopy(n);

                MemorySegment segmentA = CBLAS.copyOfMatrix(arena, a, offsetA, n, n, lda, copyLDA);
                MemorySegment segmentB = CBLAS.copyOfMatrix(arena, b, offsetB, n, nrhs, ldb, copyLDB);
                MemorySegment segmentN = arena.allocateFrom(JAVA_INT, n);
                MemorySegment segmentLDA = arena.allocateFrom(JAVA_INT, copyLDA);
                MemorySegment segmentLDB = arena.allocateFrom(JAVA_INT, copyLDB);

                DPOTRS_OFF_HEAP.invokeExact(arena.allocateFrom(JAVA_BYTE, Native.uplo(upper)), segmentN, arena.allocateFrom(JAVA_INT, nrhs), segmentA,
                        segmentLDA, segmentB, segmentLDB, arena.allocate(JAVA_INT), 1L);

                CBLAS.copyBackMatrix(segmentB, copyLDB, b, offsetB, n, nrhs, ldb);

            } catch (RuntimeException | Error cause) {
                throw cause;
            } catch (Throwable cause) {
                throw new ProgrammingError(cause);
            }
        }

        private static byte uplo(final boolean upper) {
            return (byte) (upper ? 'U' : 'L');
        }

        /**
         * Critical, with the Java arrays in place, or off-heap, with copies in native memory, when
         * {@link POTRS#isOffHeap(double, double)}.
         */
        static void invoke(final boolean upper, final int n, final int nrhs, final double[] a, final int offsetA, final int lda, final double[] b,
                final int offsetB, final int ldb) {
            if (POTRS.isOffHeap(n, nrhs)) {
                Native.invokeOffHeap(upper, n, nrhs, a, offsetA, lda, b, offsetB, ldb);
            } else {
                Native.invokeCritical(upper, n, nrhs, a, offsetA, lda, b, offsetB, ldb);
            }
        }

    }

    /**
     * More right hand sides than this are solved for together: natively, see {@link #NATIVE_THRESHOLD}, or by
     * the Java implementation. Fewer are solved for one at a time, each with two calls to {@link TRSV} – that
     * decides between its native and Java implementations.
     */
    private static final int COLUMNS_THRESHOLD = 3;
    /**
     * The block size (rows and columns) of the copy of the lower triangle of the inverse to its upper
     * triangle.
     */
    private static final int MIRROR_BLOCK = 32;
    /**
     * The native implementation is used, if there is one, when n*n*nrhs (the number of multiply-adds) is
     * larger than this.
     */
    private static final double NATIVE_THRESHOLD = 150;

    static final boolean NATIVE = NativeLibrary.isAvailable() && Native.DPOTRS_CRITICAL != null;
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
     * {@code dpotrs}: solves A*X = B, X overwrites B
     * <p>
     * A and B are column-major, stored in their arrays from an offset with a leading dimension. A holds the
     * factor, L or U, as returned by {@link POTRF}; the other triangle is not referenced.
     *
     * @param upper A = U<sup>T</sup>*U if true, otherwise A = L*L<sup>T</sup>
     * @param n     The order of A
     * @param nrhs  The number of right hand sides (columns of B)
     */
    public static void invoke(final boolean upper, final int n, final int nrhs, final double[] a, final int offsetA, final int lda, final double[] b,
            final int offsetB, final int ldb) {

        ArrayOperation.checkArgument("POTRS", "n", n >= 0);
        ArrayOperation.checkArgument("POTRS", "nrhs", nrhs >= 0);
        ArrayOperation.checkArgument("POTRS", "lda", lda >= Math.max(1, n));
        ArrayOperation.checkArgument("POTRS", "ldb", ldb >= Math.max(1, n));

        if (n == 0 || nrhs == 0) {
            return;
        }

        if (nrhs <= COLUMNS_THRESHOLD) {
            POTRS.solveColumns(upper, n, nrhs, a, offsetA, lda, b, offsetB, ldb);
        } else if (POTRS.isNative(n, nrhs)) {
            Native.invoke(upper, n, nrhs, a, offsetA, lda, b, offsetB, ldb);
        } else {
            JVM.invoke(upper, n, nrhs, a, offsetA, lda, b, offsetB, ldb);
        }
    }

    /**
     * As {@link #invoke(boolean, int, int, double[], int, int, double[], int, int)}, for other number types:
     * A is Hermitian, and A = L*L<sup>H</sup> or A = U<sup>H</sup>*U. There is only a pure Java
     * implementation.
     */
    public static <N extends Scalar<N>> void invoke(final boolean upper, final int n, final int nrhs, final N[] a, final int offsetA, final int lda,
            final N[] b, final int offsetB, final int ldb, final Scalar.Factory<N> scalar) {

        ArrayOperation.checkArgument("POTRS", "n", n >= 0);
        ArrayOperation.checkArgument("POTRS", "nrhs", nrhs >= 0);
        ArrayOperation.checkArgument("POTRS", "lda", lda >= Math.max(1, n));
        ArrayOperation.checkArgument("POTRS", "ldb", ldb >= Math.max(1, n));

        if (n == 0 || nrhs == 0) {
            return;
        }

        JVM.invoke(upper, n, nrhs, a, offsetA, lda, b, offsetB, ldb, scalar);
    }

    /**
     * {@code dpotrs} with B = I: X, the inverse of A, is written to B
     * <p>
     * A and B are column-major, stored in their arrays from an offset with a leading dimension. A holds the
     * factor, L or U, as returned by {@link POTRF}; it is not changed, and the other triangle is not
     * referenced. B need not be set on entry. X is exactly symmetric: its upper triangle is a copy of its
     * lower.
     *
     * @param upper A = U<sup>T</sup>*U if true, otherwise A = L*L<sup>T</sup>
     * @param n     The order of A and B
     */
    public static void invokeIdentity(final boolean upper, final int n, final double[] a, final int offsetA, final int lda, final double[] b, final int offsetB,
            final int ldb) {

        ArrayOperation.checkArgument("POTRS", "n", n >= 0);
        ArrayOperation.checkArgument("POTRS", "lda", lda >= Math.max(1, n));
        ArrayOperation.checkArgument("POTRS", "ldb", ldb >= Math.max(1, n));

        if (n == 0) {
            return;
        }

        if (n > COLUMNS_THRESHOLD && POTRS.isNative(n, n)) {
            for (int j = 0; j < n; j++) {
                int column = offsetB + j * ldb;
                Arrays.fill(b, column, column + n, ZERO);
                b[column + j] = ONE;
            }
            Native.invoke(upper, n, n, a, offsetA, lda, b, offsetB, ldb);
            POTRS.copyLowerToUpper(n, b, offsetB, ldb);
        } else {
            JVM.invokeIdentity(upper, n, a, offsetA, lda, b, offsetB, ldb);
        }
    }

    /**
     * As {@link #invokeIdentity(boolean, int, double[], int, int, double[], int, int)}, for other number
     * types: X is exactly Hermitian, its upper triangle the conjugate of its lower. There is only a pure Java
     * implementation.
     */
    public static <N extends Scalar<N>> void invokeIdentity(final boolean upper, final int n, final N[] a, final int offsetA, final int lda, final N[] b,
            final int offsetB, final int ldb, final Scalar.Factory<N> scalar) {

        ArrayOperation.checkArgument("POTRS", "n", n >= 0);
        ArrayOperation.checkArgument("POTRS", "lda", lda >= Math.max(1, n));
        ArrayOperation.checkArgument("POTRS", "ldb", ldb >= Math.max(1, n));

        if (n == 0) {
            return;
        }

        JVM.invokeIdentity(upper, n, a, offsetA, lda, b, offsetB, ldb, scalar);
    }

    /**
     * Copies the strictly lower triangle of the n-by-n matrix B to its upper triangle, in square blocks of
     * {@link #MIRROR_BLOCK} rows and columns.
     */
    private static void copyLowerToUpper(final int n, final double[] b, final int offsetB, final int ldb) {
        for (int jj = 0; jj < n; jj += MIRROR_BLOCK) {
            int jLimit = Math.min(n, jj + MIRROR_BLOCK);
            for (int ii = 0; ii <= jj; ii += MIRROR_BLOCK) {
                int iLimit = Math.min(n, ii + MIRROR_BLOCK);
                for (int j = jj; j < jLimit; j++) {
                    for (int i = ii, limit = Math.min(iLimit, j); i < limit; i++) {
                        b[offsetB + i + j * ldb] = b[offsetB + j + i * ldb];
                    }
                }
            }
        }
    }

    /**
     * Copies the conjugate of the strictly lower triangle of the n-by-n matrix B to its upper triangle, in
     * square blocks of {@link #MIRROR_BLOCK} rows and columns.
     */
    private static <N extends Scalar<N>> void copyLowerToUpper(final int n, final N[] b, final int offsetB, final int ldb) {
        for (int jj = 0; jj < n; jj += MIRROR_BLOCK) {
            int jLimit = Math.min(n, jj + MIRROR_BLOCK);
            for (int ii = 0; ii <= jj; ii += MIRROR_BLOCK) {
                int iLimit = Math.min(n, ii + MIRROR_BLOCK);
                for (int j = jj; j < jLimit; j++) {
                    for (int i = ii, limit = Math.min(iLimit, j); i < limit; i++) {
                        b[offsetB + i + j * ldb] = b[offsetB + j + i * ldb].conjugate().get();
                    }
                }
            }
        }
    }

    /**
     * The right hand sides one at a time, each with two calls to the dispatching
     * {@link TRSV#invoke(boolean, boolean, boolean, int, double[], int, int, double[], int, int)}.
     */
    private static void solveColumns(final boolean upper, final int n, final int nrhs, final double[] a, final int offsetA, final int lda, final double[] b,
            final int offsetB, final int ldb) {
        for (int j = 0; j < nrhs; j++) {
            int column = offsetB + j * ldb;
            TRSV.invoke(upper, upper, false, n, a, offsetA, lda, b, column, 1);
            TRSV.invoke(upper, !upper, false, n, a, offsetA, lda, b, column, 1);
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

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
import static org.ojalgo.function.constant.PrimitiveMath.*;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;
import java.util.Objects;

import org.ojalgo.ProgrammingError;
import org.ojalgo.array.operation.ArrayOperation;
import org.ojalgo.array.operation.NativeLibrary;
import org.ojalgo.concurrent.DivideAndConquer;
import org.ojalgo.concurrent.Parallelism;
import org.ojalgo.scalar.Scalar;

/**
 * LAPACK {@code ?sytrd}, using the lower triangle: reduces a symmetric n-by-n matrix A to tridiagonal form T
 * by an orthogonal similarity transformation:
 *
 * <pre>{@code
 * Q^T * A * Q = T
 * }</pre>
 *
 * Only the lower triangle of A is read and written. On exit, its diagonal and first subdiagonal hold T, and
 * the elements below the first subdiagonal hold the reflectors that make Q:
 *
 * <pre>{@code
 * Q = H(1) * H(2) * ... * H(n-1)
 * H(i) = I - tau(i) * v * v^T
 * }</pre>
 *
 * v(1:i) = 0 and v(i+1) = 1 (not stored), and v(i+2:n) is stored in A(i+2:n,i). With tau(i) = 0, H(i) is the
 * identity. The diagonal and the subdiagonal of T are also written to d and e. Reducing the upper triangle,
 * the other half of LAPACK's {@code ?sytrd}, is not implemented.
 * <p>
 * For other number types (Hermitian matrices) Q is unitary, H(i) = I - tau(i) * v * v<sup>H</sup>, and the
 * subdiagonal of T is not necessarily real. There is only a pure Java implementation for those.
 * <p>
 * {@link ORGTR} forms Q explicitly.
 *
 * @see NativeLibrary
 */
public abstract class SYTRD implements LAPACK {

    /**
     * The pure Java implementations. Everything they call is pure Java as well – the {@code JVM}
     * implementations of other operations, never their dispatching {@code invoke} methods (that may call
     * native code).
     * <p>
     * Unblocked: for each column, the reflector that zeroes the elements below the first subdiagonal is
     * generated (as by {@link GEQRF}, from the subdiagonal down) and applied from both sides to the trailing
     * triangle: w = tau * (A*v - (tau/2) * (v<sup>H</sup>*A*v) * v), then A = A - v*w<sup>H</sup> -
     * w*v<sup>H</sup>. A*v (by rows) and the update (by columns) are split over several threads when they are
     * large, see {@link SYTRD#THRESHOLD}.
     */
    static final class JVM {

        private static final DivideAndConquer.Divider DIVIDER_COLUMNS = Parallelism.CORES.newDivider();
        private static final DivideAndConquer.Divider DIVIDER_ROWS = Parallelism.THREADS.newDivider();

        /**
         * A = A - v*w<sup>T</sup> - w*v<sup>T</sup>, in the lower triangle of the columns from (inclusive) to
         * (exclusive). v(first) to v(n-1) are stored from {@code v[offsetV]}.
         */
        private static void rank2Columns(final int n, final double[] a, final int offsetA, final int lda, final double[] v, final int offsetV, final double[] w,
                final int first, final int from, final int to) {
            for (int j = from; j < to; j++) {
                int column = offsetA + j * lda;
                double vj = v[offsetV + j - first];
                double wj = w[j];
                for (int i = j; i < n; i++) {
                    a[column + i] -= w[i] * vj + v[offsetV + i - first] * wj;
                }
            }
        }

        /**
         * @see #rank2Columns(int, double[], int, int, double[], int, double[], int, int, int)
         */
        private static <N extends Scalar<N>> void rank2Columns(final int n, final N[] a, final int offsetA, final int lda, final N[] v, final int offsetV,
                final N[] w, final int first, final int from, final int to) {
            for (int j = from; j < to; j++) {
                int column = offsetA + j * lda;
                Scalar<N> vj = v[offsetV + j - first].conjugate();
                Scalar<N> wj = w[j].conjugate();
                for (int i = j; i < n; i++) {
                    a[column + i] = a[column + i].subtract(w[i].multiply(vj).add(v[offsetV + i - first].multiply(wj))).get();
                }
            }
        }

        /**
         * w(i) = (A*v)(i) for the rows from (inclusive) to (exclusive), reading the lower triangle of the
         * trailing part of A, from row and column first. v(first) to v(n-1) are stored from
         * {@code v[offsetV]}.
         */
        private static void symvRows(final int n, final double[] a, final int offsetA, final int lda, final double[] v, final int offsetV, final double[] w,
                final int first, final int from, final int to) {
            for (int i = from; i < to; i++) {
                double sum = ZERO;
                for (int c = first; c < i; c++) {
                    sum += a[offsetA + i + c * lda] * v[offsetV + c - first];
                }
                int column = offsetA + i * lda;
                for (int c = i; c < n; c++) {
                    sum += a[column + c] * v[offsetV + c - first];
                }
                w[i] = sum;
            }
        }

        /**
         * @see #symvRows(int, double[], int, int, double[], int, double[], int, int, int)
         */
        private static <N extends Scalar<N>> void symvRows(final int n, final N[] a, final int offsetA, final int lda, final N[] v, final int offsetV,
                final N[] w, final int first, final int from, final int to, final Scalar.Factory<N> scalar) {
            for (int i = from; i < to; i++) {
                Scalar<N> sum = scalar.zero();
                for (int c = first; c < i; c++) {
                    sum = sum.add(a[offsetA + i + c * lda].multiply(v[offsetV + c - first]));
                }
                int column = offsetA + i * lda;
                for (int c = i; c < n; c++) {
                    sum = sum.add(a[column + c].conjugate().multiply(v[offsetV + c - first]));
                }
                w[i] = sum.get();
            }
        }

        /**
         * Applies the reflector, v(first) to v(n-1) stored from {@code a[offsetV]} (v(first) = 1 included),
         * from both sides to the trailing triangle, from row and column first.
         */
        private static void update(final int n, final double[] a, final int offsetA, final int lda, final int offsetV, final int first, final double tau,
                final double[] w) {

            // A row of A*v, or a column of the update (on average): n-first multiply-adds
            int chunk = MatrixOperation.chunk(THRESHOLD, n - first);
            boolean divide = n - first > chunk;

            if (divide) {
                DIVIDER_ROWS.threshold(chunk).divide(first, n, (from, to) -> JVM.symvRows(n, a, offsetA, lda, a, offsetV, w, first, from, to));
            } else {
                JVM.symvRows(n, a, offsetA, lda, a, offsetV, w, first, first, n);
            }

            double scale = ZERO;
            for (int c = first; c < n; c++) {
                scale += a[offsetV + c - first] * w[c];
            }
            scale *= tau / TWO;
            for (int c = first; c < n; c++) {
                w[c] = tau * (w[c] - scale * a[offsetV + c - first]);
            }

            if (divide) {
                DIVIDER_COLUMNS.threshold(chunk).divide(first, n, (from, to) -> JVM.rank2Columns(n, a, offsetA, lda, a, offsetV, w, first, from, to));
            } else {
                JVM.rank2Columns(n, a, offsetA, lda, a, offsetV, w, first, first, n);
            }
        }

        /**
         * @see #update(int, double[], int, int, int, int, double, double[])
         */
        private static <N extends Scalar<N>> void update(final int n, final N[] a, final int offsetA, final int lda, final int offsetV, final int first,
                final N tau, final N[] w, final Scalar.Factory<N> scalar) {

            int chunk = MatrixOperation.chunk(THRESHOLD, n - first);
            boolean divide = n - first > chunk;

            if (divide) {
                DIVIDER_ROWS.threshold(chunk).divide(first, n, (from, to) -> JVM.symvRows(n, a, offsetA, lda, a, offsetV, w, first, from, to, scalar));
            } else {
                JVM.symvRows(n, a, offsetA, lda, a, offsetV, w, first, first, n, scalar);
            }

            Scalar<N> scale = scalar.zero();
            for (int c = first; c < n; c++) {
                scale = scale.add(a[offsetV + c - first].conjugate().multiply(w[c]));
            }
            scale = scale.multiply(tau).divide(TWO);
            for (int c = first; c < n; c++) {
                w[c] = tau.multiply(w[c].subtract(scale.multiply(a[offsetV + c - first]))).get();
            }

            if (divide) {
                DIVIDER_COLUMNS.threshold(chunk).divide(first, n, (from, to) -> JVM.rank2Columns(n, a, offsetA, lda, a, offsetV, w, first, from, to));
            } else {
                JVM.rank2Columns(n, a, offsetA, lda, a, offsetV, w, first, first, n);
            }
        }

        /**
         * The pure Java implementation of
         * {@link SYTRD#invoke(int, double[], int, int, double[], int, double[], int, double[], int)}, without
         * argument checks, for n > 0.
         */
        static void invoke(final int n, final double[] a, final int offsetA, final int lda, final double[] d, final int offsetD, final double[] e,
                final int offsetE, final double[] tau, final int offsetTau) {

            double[] w = new double[n];

            for (int k = 0; k + 2 < n; k++) {

                int column = offsetA + k * lda;

                double beta = GEQRF.JVM.generate(n, a, column, k + 1);

                tau[offsetTau + k] = beta;

                if (beta != ZERO) {
                    // v(k+1) = 1 temporarily in place of the subdiagonal element, so that the column is v
                    int offsetV = column + k + 1;
                    double subdiagonal = a[offsetV];
                    a[offsetV] = ONE;
                    JVM.update(n, a, offsetA, lda, offsetV, k + 1, beta, w);
                    a[offsetV] = subdiagonal;
                }
            }

            if (n >= 2) {
                tau[offsetTau + n - 2] = ZERO;
            }

            for (int i = 0; i < n; i++) {
                d[offsetD + i] = a[offsetA + i + i * lda];
                if (i + 1 < n) {
                    e[offsetE + i] = a[offsetA + i + 1 + i * lda];
                }
            }
        }

        /**
         * The pure Java implementation of
         * {@link SYTRD#invoke(int, Scalar[], int, int, Scalar[], int, Scalar[], int, Scalar[], int, Scalar.Factory)},
         * without argument checks, for n > 0.
         */
        static <N extends Scalar<N>> void invoke(final int n, final N[] a, final int offsetA, final int lda, final N[] d, final int offsetD, final N[] e,
                final int offsetE, final N[] tau, final int offsetTau, final Scalar.Factory<N> scalar) {

            N[] w = scalar.newArrayInstance(n);
            N zero = scalar.zero().get();

            for (int k = 0; k + 2 < n; k++) {

                int column = offsetA + k * lda;

                N beta = GEQRF.JVM.generate(n, a, column, k + 1, scalar);

                if (beta != null) {
                    tau[offsetTau + k] = beta;
                    int offsetV = column + k + 1;
                    N subdiagonal = a[offsetV];
                    a[offsetV] = scalar.one().get();
                    JVM.update(n, a, offsetA, lda, offsetV, k + 1, beta, w, scalar);
                    a[offsetV] = subdiagonal;
                } else {
                    tau[offsetTau + k] = zero;
                }
            }

            if (n >= 2) {
                tau[offsetTau + n - 2] = zero;
            }

            for (int i = 0; i < n; i++) {
                d[offsetD + i] = a[offsetA + i + i * lda];
                if (i + 1 < n) {
                    e[offsetE + i] = a[offsetA + i + 1 + i * lda];
                }
            }
        }

    }

    /**
     * Native {@code dsytrd}, initialised only when {@link NativeLibrary#isAvailable()}. All arguments are
     * passed by reference, as to any Fortran routine, followed by the length of the character argument. It is
     * not linked as critical: it is passed a copy of the matrix in native memory, and the JVM can reach
     * safepoints (and collect garbage) during the call.
     */
    static final class Native {

        static final MethodHandle DSYTRD_OFF_HEAP = NativeLibrary.downcall("dsytrd_",
                FunctionDescriptor.ofVoid(ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS, JAVA_LONG), false);

        /**
         * Copies A to native memory, asks {@link #DSYTRD_OFF_HEAP} for the optimal workspace size, calls it
         * and copies the lower triangle of A, d, e and tau back. Requires n > 0.
         */
        static void invoke(final int n, final double[] a, final int offsetA, final int lda, final double[] d, final int offsetD, final double[] e,
                final int offsetE, final double[] tau, final int offsetTau) {

            Objects.checkFromIndexSize(offsetD, n, d.length);
            Objects.checkFromIndexSize(offsetE, n - 1, e.length);
            Objects.checkFromIndexSize(offsetTau, n - 1, tau.length);

            try (Arena arena = Arena.ofConfined()) {

                int copyLDA = CBLAS.leadingDimensionOfCopy(n);

                MemorySegment segmentUplo = arena.allocateFrom(JAVA_BYTE, (byte) 'L');
                MemorySegment segmentA = CBLAS.copyOfMatrix(arena, a, offsetA, n, n, lda, copyLDA);
                MemorySegment segmentD = arena.allocate(JAVA_DOUBLE, n);
                MemorySegment segmentE = arena.allocate(JAVA_DOUBLE, Math.max(1, n - 1));
                MemorySegment segmentTau = arena.allocate(JAVA_DOUBLE, Math.max(1, n - 1));
                MemorySegment segmentInfo = arena.allocate(JAVA_INT);
                MemorySegment segmentN = arena.allocateFrom(JAVA_INT, n);
                MemorySegment segmentLDA = arena.allocateFrom(JAVA_INT, copyLDA);

                MemorySegment query = arena.allocate(JAVA_DOUBLE);
                DSYTRD_OFF_HEAP.invokeExact(segmentUplo, segmentN, segmentA, segmentLDA, segmentD, segmentE, segmentTau, query,
                        arena.allocateFrom(JAVA_INT, -1), segmentInfo, 1L);
                int lwork = Math.max(1, (int) query.get(JAVA_DOUBLE, 0L));

                DSYTRD_OFF_HEAP.invokeExact(segmentUplo, segmentN, segmentA, segmentLDA, segmentD, segmentE, segmentTau, arena.allocate(JAVA_DOUBLE, lwork),
                        arena.allocateFrom(JAVA_INT, lwork), segmentInfo, 1L);

                CBLAS.copyBackTriangle(segmentA, copyLDA, a, offsetA, n, lda, false);
                MemorySegment.copy(segmentD, JAVA_DOUBLE, 0L, d, offsetD, n);
                MemorySegment.copy(segmentE, JAVA_DOUBLE, 0L, e, offsetE, n - 1);
                MemorySegment.copy(segmentTau, JAVA_DOUBLE, 0L, tau, offsetTau, n - 1);

            } catch (RuntimeException | Error cause) {
                throw cause;
            } catch (Throwable cause) {
                throw new ProgrammingError(cause);
            }
        }

    }

    /**
     * The native implementation is used, if there is one, when n*n*n is larger than this.
     */
    private static final double NATIVE_THRESHOLD = 2_000_000;

    static final boolean NATIVE = NativeLibrary.isAvailable() && Native.DSYTRD_OFF_HEAP != null;

    /**
     * The Java implementation splits A*v (by rows) and the update of the trailing triangle (by columns) over
     * several threads when they are larger than this cubed (in multiply-adds). Each part then gets more than
     * half of that.
     */
    static int THRESHOLD = 48;

    /**
     * {@code dsytrd} with UPLO = 'L': Q<sup>T</sup>*A*Q = T, T and the reflectors that make Q overwrite the
     * lower triangle of A
     * <p>
     * A is column-major, stored in its array from an offset with a leading dimension; its upper triangle is
     * not referenced. The diagonal of T, d(i), i = 1, ..., n, is stored in {@code d[offsetD + i - 1]}, and
     * its subdiagonal and tau, e(i) and tau(i), i = 1, ..., n-1, in {@code e[offsetE + i - 1]} and
     * {@code tau[offsetTau + i - 1]}.
     *
     * @param n The order of A
     */
    public static void invoke(final int n, final double[] a, final int offsetA, final int lda, final double[] d, final int offsetD, final double[] e,
            final int offsetE, final double[] tau, final int offsetTau) {

        ArrayOperation.checkArgument("SYTRD", "n", n >= 0);
        ArrayOperation.checkArgument("SYTRD", "lda", lda >= Math.max(1, n));

        if (n == 0) {
            return;
        }

        if (SYTRD.isNative(n)) {
            Native.invoke(n, a, offsetA, lda, d, offsetD, e, offsetE, tau, offsetTau);
        } else {
            JVM.invoke(n, a, offsetA, lda, d, offsetD, e, offsetE, tau, offsetTau);
        }
    }

    /**
     * As {@link #invoke(int, double[], int, int, double[], int, double[], int, double[], int)}, for other
     * number types: A is Hermitian, Q unitary, and tau(i) = 0 where there is no reflector. There is only a
     * pure Java implementation.
     */
    public static <N extends Scalar<N>> void invoke(final int n, final N[] a, final int offsetA, final int lda, final N[] d, final int offsetD, final N[] e,
            final int offsetE, final N[] tau, final int offsetTau, final Scalar.Factory<N> scalar) {

        ArrayOperation.checkArgument("SYTRD", "n", n >= 0);
        ArrayOperation.checkArgument("SYTRD", "lda", lda >= Math.max(1, n));

        if (n == 0) {
            return;
        }

        JVM.invoke(n, a, offsetA, lda, d, offsetD, e, offsetE, tau, offsetTau, scalar);
    }

    /**
     * Whether to use the native implementation: there is one, and n*n*n is larger than
     * {@link #NATIVE_THRESHOLD}.
     */
    static boolean isNative(final double n) {
        return NATIVE && n * n * n > NATIVE_THRESHOLD;
    }

}

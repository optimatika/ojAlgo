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
import static java.lang.foreign.ValueLayout.JAVA_DOUBLE;
import static java.lang.foreign.ValueLayout.JAVA_INT;
import static org.ojalgo.function.constant.PrimitiveMath.ONE;
import static org.ojalgo.function.constant.PrimitiveMath.ZERO;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;
import java.util.Arrays;
import java.util.Objects;

import org.ojalgo.ProgrammingError;
import org.ojalgo.array.operation.AXPY;
import org.ojalgo.array.operation.ArrayOperation;
import org.ojalgo.array.operation.NativeLibrary;
import org.ojalgo.concurrent.DivideAndConquer;
import org.ojalgo.concurrent.Parallelism;

/**
 * LAPACK {@code ?gebrd}, for m >= n: reduces a general m-by-n matrix A to upper bidiagonal form B by an
 * orthogonal transformation:
 *
 * <pre>{@code
 * Q^T * A * P = B
 * }</pre>
 *
 * On exit, the diagonal and the first superdiagonal of A hold B, the elements below the diagonal hold the
 * reflectors that make Q, and the elements above the first superdiagonal hold those that make P:
 *
 * <pre>{@code
 * Q = H(1) * H(2) * ... * H(n)
 * P = G(1) * G(2) * ... * G(n-1)
 * H(i) = I - tauq(i) * v * v^T
 * G(i) = I - taup(i) * u * u^T
 * }</pre>
 *
 * v(1:i-1) = 0 and v(i) = 1 (not stored), and v(i+1:m) is stored in A(i+1:m,i). u(1:i) = 0 and u(i+1) = 1
 * (not stored), and u(i+2:n) is stored in A(i,i+2:n). With tauq(i) or taup(i) = 0, H(i) or G(i) is the
 * identity, and taup(n) = 0. The diagonal and the superdiagonal of B are also written to d and e. Reducing a
 * matrix with m < n to lower bidiagonal form, the other half of LAPACK's {@code ?gebrd}, is not implemented
 * (reduce A<sup>T</sup> instead).
 * <p>
 * {@link ORGBR} forms Q and P explicitly.
 *
 * @see NativeLibrary
 */
public abstract class GEBRD implements LAPACK {

    /**
     * The pure Java implementation. Everything it calls is pure Java as well – the {@code JVM}
     * implementations of other operations, never their dispatching {@code invoke} methods (that may call
     * native code).
     * <p>
     * Unblocked: for each column, the reflector H(i) that zeroes the elements below the diagonal is generated
     * and applied from the left to the columns to the right (as by {@link GEQRF}); then the reflector G(i)
     * that zeroes the elements to the right of the superdiagonal is generated from a copy of the row and
     * applied from the right to the rows below: w = A*u, then A = A - tau * w * u<sup>T</sup>. A*u (by rows)
     * and the update (by columns) are split over several threads when they are large, see
     * {@link GEBRD#THRESHOLD}.
     */
    static final class JVM {

        private static final DivideAndConquer.Divider DIVIDER_COLUMNS = Parallelism.CORES.newDivider();
        private static final DivideAndConquer.Divider DIVIDER_ROWS = Parallelism.THREADS.newDivider();

        /**
         * w(i) = (A*u)(i) for the rows from (inclusive) to (exclusive), over the columns first to n-1.
         * u(first) to u(n-1) are stored from {@code u[0]}.
         */
        private static void multiplyRows(final int n, final double[] a, final int offsetA, final int lda, final double[] u, final double[] w, final int first,
                final int from, final int to) {
            Arrays.fill(w, from, to, ZERO);
            for (int c = first; c < n; c++) {
                AXPY.JVM.invoke(to - from, u[c - first], a, offsetA + from + c * lda, 1, w, from, 1);
            }
        }

        /**
         * A = A - tau * w * u<sup>T</sup>, in rows row to m-1 of the columns from (inclusive) to (exclusive).
         * u(first) to u(n-1) are stored from {@code u[0]}.
         */
        private static void rank1Columns(final int m, final double[] a, final int offsetA, final int lda, final double[] u, final double tau, final double[] w,
                final int row, final int first, final int from, final int to) {
            for (int c = from; c < to; c++) {
                AXPY.JVM.invoke(m - row, -tau * u[c - first], w, row, 1, a, offsetA + row + c * lda, 1);
            }
        }

        /**
         * Applies G = I - tau * u * u<sup>T</sup>, from the right, to rows row to m-1 of columns first to n-1
         * of A. u(first) to u(n-1) are stored from {@code u[0]}, u(first) = 1 included.
         */
        private static void reflectRight(final int m, final int n, final double[] a, final int offsetA, final int lda, final int row, final int first,
                final double[] u, final double tau, final double[] w) {

            int rows = m - row;
            int columns = n - first;

            // A row of A*u: columns multiply-adds
            int chunkRows = MatrixOperation.chunk(THRESHOLD, columns);
            if (rows > chunkRows) {
                DIVIDER_ROWS.threshold(chunkRows).divide(row, m, (from, to) -> JVM.multiplyRows(n, a, offsetA, lda, u, w, first, from, to));
            } else {
                JVM.multiplyRows(n, a, offsetA, lda, u, w, first, row, m);
            }

            // A column of the update: rows multiply-adds
            int chunkColumns = MatrixOperation.chunk(THRESHOLD, rows);
            if (columns > chunkColumns) {
                DIVIDER_COLUMNS.threshold(chunkColumns).divide(first, n, (from, to) -> JVM.rank1Columns(m, a, offsetA, lda, u, tau, w, row, first, from, to));
            } else {
                JVM.rank1Columns(m, a, offsetA, lda, u, tau, w, row, first, first, n);
            }
        }

        /**
         * The pure Java implementation of
         * {@link GEBRD#invoke(int, int, double[], int, int, double[], int, double[], int, double[], int, double[], int)},
         * without argument checks, for m >= n > 0.
         */
        static void invoke(final int m, final int n, final double[] a, final int offsetA, final int lda, final double[] d, final int offsetD, final double[] e,
                final int offsetE, final double[] tauq, final int offsetTauq, final double[] taup, final int offsetTaup) {

            double[] u = new double[n];
            double[] w = new double[m];

            for (int i = 0; i < n; i++) {

                int column = offsetA + i * lda;

                double betaQ = i + 1 < m ? GEQRF.JVM.generate(m, a, column, i) : ZERO;

                tauq[offsetTauq + i] = betaQ;
                d[offsetD + i] = a[column + i];

                if (betaQ != ZERO && i + 1 < n) {
                    // v(i) = 1 temporarily in place of the diagonal element, so that the column is v
                    a[column + i] = ONE;
                    GEQRF.JVM.reflect(m, a, column + i, betaQ, a, offsetA, lda, i, i + 1, n);
                    a[column + i] = d[offsetD + i];
                }

                if (i + 1 < n) {

                    // A(i,i+1) to A(i,n-1), copied to u
                    int count = n - i - 1;
                    int superdiagonal = column + i + lda;
                    for (int c = 0; c < count; c++) {
                        u[c] = a[superdiagonal + c * lda];
                    }

                    double betaP = count > 1 ? GEQRF.JVM.generate(count, u, 0, 0) : ZERO;

                    taup[offsetTaup + i] = betaP;
                    e[offsetE + i] = u[0];

                    for (int c = 0; c < count; c++) {
                        a[superdiagonal + c * lda] = u[c];
                    }

                    if (betaP != ZERO && i + 1 < m) {
                        u[0] = ONE;
                        JVM.reflectRight(m, n, a, offsetA, lda, i + 1, i + 1, u, betaP, w);
                    }

                } else {

                    taup[offsetTaup + i] = ZERO;
                }
            }
        }

    }

    /**
     * Native {@code dgebrd}, initialised only when {@link NativeLibrary#isAvailable()}. All arguments are
     * passed by reference, as to any Fortran routine. It is not linked as critical: it is passed a copy of
     * the matrix in native memory, and the JVM can reach safepoints (and collect garbage) during the call.
     */
    static final class Native {

        static final MethodHandle DGEBRD_OFF_HEAP = NativeLibrary.downcall("dgebrd_",
                FunctionDescriptor.ofVoid(ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS), false);

        /**
         * Copies A to native memory, asks {@link #DGEBRD_OFF_HEAP} for the optimal workspace size, calls it
         * and copies A, d, e, tauq and taup back. Requires m >= n > 0.
         */
        static void invoke(final int m, final int n, final double[] a, final int offsetA, final int lda, final double[] d, final int offsetD, final double[] e,
                final int offsetE, final double[] tauq, final int offsetTauq, final double[] taup, final int offsetTaup) {

            Objects.checkFromIndexSize(offsetD, n, d.length);
            Objects.checkFromIndexSize(offsetE, n - 1, e.length);
            Objects.checkFromIndexSize(offsetTauq, n, tauq.length);
            Objects.checkFromIndexSize(offsetTaup, n, taup.length);

            try (Arena arena = Arena.ofConfined()) {

                int copyLDA = CBLAS.leadingDimensionOfCopy(m);

                MemorySegment segmentA = CBLAS.copyOfMatrix(arena, a, offsetA, m, n, lda, copyLDA);
                MemorySegment segmentD = arena.allocate(JAVA_DOUBLE, n);
                MemorySegment segmentE = arena.allocate(JAVA_DOUBLE, Math.max(1, n - 1));
                MemorySegment segmentTauq = arena.allocate(JAVA_DOUBLE, n);
                MemorySegment segmentTaup = arena.allocate(JAVA_DOUBLE, n);
                MemorySegment segmentInfo = arena.allocate(JAVA_INT);
                MemorySegment segmentM = arena.allocateFrom(JAVA_INT, m);
                MemorySegment segmentN = arena.allocateFrom(JAVA_INT, n);
                MemorySegment segmentLDA = arena.allocateFrom(JAVA_INT, copyLDA);

                MemorySegment query = arena.allocate(JAVA_DOUBLE);
                DGEBRD_OFF_HEAP.invokeExact(segmentM, segmentN, segmentA, segmentLDA, segmentD, segmentE, segmentTauq, segmentTaup, query,
                        arena.allocateFrom(JAVA_INT, -1), segmentInfo);
                int lwork = Math.max(m, (int) query.get(JAVA_DOUBLE, 0L));

                DGEBRD_OFF_HEAP.invokeExact(segmentM, segmentN, segmentA, segmentLDA, segmentD, segmentE, segmentTauq, segmentTaup,
                        arena.allocate(JAVA_DOUBLE, lwork), arena.allocateFrom(JAVA_INT, lwork), segmentInfo);

                CBLAS.copyBackMatrix(segmentA, copyLDA, a, offsetA, m, n, lda);
                MemorySegment.copy(segmentD, JAVA_DOUBLE, 0L, d, offsetD, n);
                MemorySegment.copy(segmentE, JAVA_DOUBLE, 0L, e, offsetE, n - 1);
                MemorySegment.copy(segmentTauq, JAVA_DOUBLE, 0L, tauq, offsetTauq, n);
                MemorySegment.copy(segmentTaup, JAVA_DOUBLE, 0L, taup, offsetTaup, n);

            } catch (RuntimeException | Error cause) {
                throw cause;
            } catch (Throwable cause) {
                throw new ProgrammingError(cause);
            }
        }

    }

    /**
     * The native implementation is used, if there is one, when m*n*n is larger than this.
     */
    private static final double NATIVE_THRESHOLD = 1_500_000;

    static final boolean NATIVE = NativeLibrary.isAvailable() && Native.DGEBRD_OFF_HEAP != null;

    /**
     * The Java implementation splits A*u (by rows) and the update from the right (by columns) over several
     * threads when they are larger than this cubed (in multiply-adds). Each part then gets more than half of
     * that. The application of the reflectors from the left is split as by {@link GEQRF}.
     */
    static int THRESHOLD = 48;

    /**
     * {@code dgebrd} for m >= n: Q<sup>T</sup>*A*P = B, B and the reflectors that make Q and P overwrite A
     * <p>
     * A is column-major, stored in its array from an offset with a leading dimension. The diagonal of B,
     * d(i), and tauq(i) and taup(i), i = 1, ..., n, are stored in {@code d[offsetD + i - 1]},
     * {@code tauq[offsetTauq + i - 1]} and {@code taup[offsetTaup + i - 1]}, and the superdiagonal of B,
     * e(i), i = 1, ..., n-1, in {@code e[offsetE + i - 1]}.
     *
     * @param m The number of rows of A, m >= n
     * @param n The number of columns of A
     */
    public static void invoke(final int m, final int n, final double[] a, final int offsetA, final int lda, final double[] d, final int offsetD,
            final double[] e, final int offsetE, final double[] tauq, final int offsetTauq, final double[] taup, final int offsetTaup) {

        ArrayOperation.checkArgument("GEBRD", "n", n >= 0);
        ArrayOperation.checkArgument("GEBRD", "m", m >= n);
        ArrayOperation.checkArgument("GEBRD", "lda", lda >= Math.max(1, m));

        if (n == 0) {
            return;
        }

        if (GEBRD.isNative(m, n)) {
            Native.invoke(m, n, a, offsetA, lda, d, offsetD, e, offsetE, tauq, offsetTauq, taup, offsetTaup);
        } else {
            JVM.invoke(m, n, a, offsetA, lda, d, offsetD, e, offsetE, tauq, offsetTauq, taup, offsetTaup);
        }
    }

    /**
     * Whether to use the native implementation: there is one, and m*n*n is larger than
     * {@link #NATIVE_THRESHOLD}.
     */
    static boolean isNative(final double m, final double n) {
        return NATIVE && m * n * n > NATIVE_THRESHOLD;
    }

}

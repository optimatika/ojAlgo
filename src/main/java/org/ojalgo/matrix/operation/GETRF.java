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
import static java.lang.foreign.ValueLayout.JAVA_INT;
import static org.ojalgo.function.constant.PrimitiveMath.ZERO;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;
import java.util.Objects;

import org.ojalgo.ProgrammingError;
import org.ojalgo.array.operation.AXPY;
import org.ojalgo.array.operation.ArrayOperation;
import org.ojalgo.array.operation.NativeLibrary;
import org.ojalgo.concurrent.DivideAndConquer;

/**
 * LAPACK {@code ?getrf}: LU factorisation of a general m-by-n matrix A, using partial pivoting with row
 * interchanges:
 *
 * <pre>{@code
 * A = P * L * U
 * }</pre>
 *
 * P is a permutation matrix, L is lower triangular with unit diagonal elements (lower trapezoidal if m > n)
 * and U is upper triangular (upper trapezoidal if m < n). The factors overwrite A: L below the diagonal (its
 * unit diagonal is not stored) and U on and above it.
 *
 * @see NativeLibrary
 */
public abstract class GETRF implements LAPACK {

    /**
     * The pure Java implementation. Everything it calls is pure Java as well – the {@code JVM}
     * implementations of other operations, never their dispatching {@code invoke} methods (that may call
     * native code).
     * <p>
     * Unblocked and right-looking: for each column, the pivot is the element of largest magnitude on or below
     * the diagonal (the first one if there are several), its row is interchanged with the diagonal row across
     * all columns, the elements below the diagonal are divided by the pivot, and the columns to the right are
     * updated with that column (a rank 1 update), multi-threaded when there are more than
     * {@link GETRF#THRESHOLD} of them.
     */
    static final class JVM {

        /**
         * Updates the columns to the right of column j, multi-threaded when there are more than
         * {@link GETRF#THRESHOLD} of them.
         */
        private static void update(final int m, final int n, final double[] a, final int offsetA, final int lda, final double[] multipliers, final int j) {
            if (n - j - 1 > THRESHOLD) {

                DivideAndConquer conquerer = new DivideAndConquer() {

                    @Override
                    protected void conquer(final int first, final int limit) {
                        JVM.updateColumns(m, a, offsetA, lda, multipliers, j, first, limit);
                    }
                };

                conquerer.invoke(j + 1, n, THRESHOLD);

            } else {

                JVM.updateColumns(m, a, offsetA, lda, multipliers, j, j + 1, n);
            }
        }

        /**
         * Subtracts the multipliers (column j below the diagonal, divided by the pivot) times the row j
         * element from each of the columns first to limit (exclusive), below row j.
         */
        private static void updateColumns(final int m, final double[] a, final int offsetA, final int lda, final double[] multipliers, final int j,
                final int first, final int limit) {
            for (int k = first; k < limit; k++) {
                int column = offsetA + k * lda;
                AXPY.invoke(a, column, -a[column + j], multipliers, 0, j + 1, m);
            }
        }

        /**
         * The pure Java implementation of {@link GETRF#invoke(int, int, double[], int, int, int[], int)},
         * without argument checks, for m, n > 0.
         */
        static int invoke(final int m, final int n, final double[] a, final int offsetA, final int lda, final int[] ipiv, final int offsetIpiv) {

            int info = 0;
            double[] multipliers = new double[m];

            for (int j = 0, limit = Math.min(m, n); j < limit; j++) {

                int column = offsetA + j * lda;

                int pivot = j;
                double largest = ZERO;
                for (int i = j; i < m; i++) {
                    double candidate = Math.abs(a[column + i]);
                    if (candidate > largest) {
                        largest = candidate;
                        pivot = i;
                    }
                }

                ipiv[offsetIpiv + j] = pivot + 1;

                if (pivot != j) {
                    for (int k = 0; k < n; k++) {
                        int base = offsetA + k * lda;
                        double tmp = a[base + pivot];
                        a[base + pivot] = a[base + j];
                        a[base + j] = tmp;
                    }
                }

                double diagonal = a[column + j];

                if (diagonal != ZERO) {

                    for (int i = j + 1; i < m; i++) {
                        multipliers[i] = a[column + i] /= diagonal;
                    }

                    JVM.update(m, n, a, offsetA, lda, multipliers, j);

                } else if (info == 0) {

                    info = j + 1;
                }
            }

            return info;
        }

    }

    /**
     * Native {@code dgetrf}, initialised only when {@link NativeLibrary#isAvailable()}. All arguments are
     * passed by reference, as to any Fortran routine. It is not linked as critical: it is passed a copy of
     * the matrix in native memory, and the JVM can reach safepoints (and collect garbage) during the call.
     */
    static final class Native {

        static final MethodHandle DGETRF = NativeLibrary.downcall("dgetrf_", FunctionDescriptor.ofVoid(ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS),
                false);

        /**
         * Copies A to native memory, calls {@link #DGETRF} and copies the factors and the pivots back.
         * Requires m, n > 0.
         */
        static int invoke(final int m, final int n, final double[] a, final int offsetA, final int lda, final int[] ipiv, final int offsetIpiv) {

            int minDim = Math.min(m, n);
            Objects.checkFromIndexSize(offsetIpiv, minDim, ipiv.length);

            try (Arena arena = Arena.ofConfined()) {

                MemorySegment segmentA = CBLAS.copyOfMatrix(arena, a, offsetA, m, n, lda);
                MemorySegment segmentIPIV = arena.allocate(JAVA_INT, minDim);
                MemorySegment segmentInfo = arena.allocate(JAVA_INT);
                // The copy is packed: its leading dimension is m
                MemorySegment segmentM = arena.allocateFrom(JAVA_INT, m);

                DGETRF.invokeExact(segmentM, arena.allocateFrom(JAVA_INT, n), segmentA, segmentM, segmentIPIV, segmentInfo);

                CBLAS.copyBackMatrix(segmentA, a, offsetA, m, n, lda);
                MemorySegment.copy(segmentIPIV, JAVA_INT, 0L, ipiv, offsetIpiv, minDim);

                return segmentInfo.get(JAVA_INT, 0L);

            } catch (RuntimeException | Error cause) {
                throw cause;
            } catch (Throwable cause) {
                throw new ProgrammingError(cause);
            }
        }

    }

    /**
     * The native implementation is used, if there is one, when m*n*min(m,n) is larger than this. That is
     * about 3 times the number of multiply-adds for a square matrix.
     */
    private static final double NATIVE_THRESHOLD = 10_000;

    static final boolean NATIVE = NativeLibrary.isAvailable() && Native.DGETRF != null;

    /**
     * The Java implementation multi-threads the update of the columns to the right of the pivot when there
     * are more than this many of them.
     */
    static int THRESHOLD = 256;

    /**
     * {@code dgetrf}: A = P*L*U, the factors overwrite A
     * <p>
     * A is column-major, stored in its array from an offset with a leading dimension. P is returned as a
     * sequence of row interchanges, with 1-based row numbers as in LAPACK: for i = 1, ..., min(m,n), row i
     * was interchanged with row ipiv(i), stored in {@code ipiv[offsetIpiv + i - 1]}. Applied in that order to
     * the rows of A, the interchanges give L*U.
     *
     * @param m The number of rows of A
     * @param n The number of columns of A
     * @return 0, or i > 0 if U(i,i) is exactly zero (1-based). The factorisation is then completed, but U is
     *         singular, and solving with it divides by zero.
     */
    public static int invoke(final int m, final int n, final double[] a, final int offsetA, final int lda, final int[] ipiv, final int offsetIpiv) {

        ArrayOperation.checkArgument("GETRF", "m", m >= 0);
        ArrayOperation.checkArgument("GETRF", "n", n >= 0);
        ArrayOperation.checkArgument("GETRF", "lda", lda >= Math.max(1, m));

        if (m == 0 || n == 0) {
            return 0;
        }

        if (GETRF.isNative(m, n)) {
            return Native.invoke(m, n, a, offsetA, lda, ipiv, offsetIpiv);
        } else {
            return JVM.invoke(m, n, a, offsetA, lda, ipiv, offsetIpiv);
        }
    }

    /**
     * Whether to use the native implementation: there is one, and m*n*min(m,n) is larger than
     * {@link #NATIVE_THRESHOLD}.
     */
    public static boolean isNative(final double m, final double n) {
        return NATIVE && m * n * Math.min(m, n) > NATIVE_THRESHOLD;
    }

}

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
import static java.lang.foreign.ValueLayout.JAVA_DOUBLE;
import static java.lang.foreign.ValueLayout.JAVA_INT;
import static java.lang.foreign.ValueLayout.JAVA_LONG;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;
import java.util.Objects;

import org.ojalgo.ProgrammingError;
import org.ojalgo.array.operation.ArrayOperation;
import org.ojalgo.array.operation.NativeLibrary;

/**
 * LAPACK {@code ?gesdd}: the singular value decomposition of a general m-by-n matrix A:
 *
 * <pre>{@code
 * A = U * diag(s) * V ^ T
 * }</pre>
 *
 * The k = min(m,n) singular values s are non-negative and in descending order, and U and V have orthonormal
 * columns, the left and right singular vectors. Economy size, U is m-by-k and V n-by-k; full size, U is
 * m-by-m and V n-by-n, both orthogonal. V is returned, not V<sup>T</sup> as by LAPACK's {@code ?gesdd}. The
 * native implementation is divide and conquer. The pure Java implementation reduces the matrix to bidiagonal
 * form ({@link GEBRD}), forms the orthogonal factors ({@link ORGQR}, {@link ORGBR}) and diagonalises the
 * bidiagonal matrix by the implicit QR method ({@link BDSQR}). Both decompose A<sup>T</sup> = V * diag(s) *
 * U<sup>T</sup> instead when m < n.
 *
 * @see NativeLibrary
 */
public abstract class GESDD implements LAPACK {

    /**
     * The pure Java implementation. Everything it calls is pure Java as well – the {@code JVM}
     * implementations of other operations, never their dispatching {@code invoke} methods (that may call
     * native code).
     */
    static final class JVM {

        /**
         * Exchanges columns i and j, rows 0 to rows-1, of A.
         */
        private static void exchange(final int rows, final double[] a, final int offsetA, final int lda, final int i, final int j) {
            int colI = offsetA + i * lda;
            int colJ = offsetA + j * lda;
            for (int r = 0; r < rows; r++) {
                double tmp = a[colI + r];
                a[colI + r] = a[colJ + r];
                a[colJ + r] = tmp;
            }
        }

        /**
         * Negates column j, rows 0 to rows-1, of A.
         */
        private static void negate(final int rows, final double[] a, final int offsetA, final int lda, final int j) {
            int colJ = offsetA + j * lda;
            for (int r = 0; r < rows; r++) {
                a[colJ + r] = -a[colJ + r];
            }
        }

        /**
         * Applies the plane rotation of columns low and high, as reported by {@link BDSQR}, to rows 0 to
         * rows-1 of A.
         */
        private static void rotate(final int rows, final double[] a, final int offsetA, final int lda, final int low, final int high, final double cos,
                final double sin) {
            int colL = offsetA + low * lda;
            int colH = offsetA + high * lda;
            for (int r = 0; r < rows; r++) {
                double oldL = a[colL + r];
                double oldH = a[colH + r];
                a[colL + r] = cos * oldL - sin * oldH;
                a[colH + r] = cos * oldH + sin * oldL;
            }
        }

        /**
         * The decomposition of an m-by-n matrix A with m >= n: the left singular vectors to U, m-by-n or
         * m-by-m (full size), and the right singular vectors to V, n-by-n. A is destroyed.
         */
        private static void tall(final boolean vectors, final boolean fullSize, final int m, final int n, final double[] a, final int offsetA, final int lda,
                final double[] s, final int offsetS, final double[] u, final int offsetU, final int ldu, final double[] v, final int offsetV, final int ldv) {

            double[] d = new double[n];
            double[] e = new double[n];
            double[] tauq = new double[n];
            double[] taup = new double[n];

            GEBRD.JVM.invoke(m, n, a, offsetA, lda, d, 0, e, 0, tauq, 0, taup, 0);

            if (vectors) {

                for (int j = 0; j < n; j++) {
                    System.arraycopy(a, offsetA + j + 1 + j * lda, u, offsetU + j + 1 + j * ldu, m - j - 1);
                }
                ORGQR.JVM.invoke(m, fullSize ? m : n, n, u, offsetU, ldu, tauq, 0);

                ORGBR.JVM.generateP(n, a, offsetA, lda, taup, 0, v, offsetV, ldv);

                BDSQR.JVM.invoke(d, e, (low, high, cos, sin) -> JVM.rotate(m, u, offsetU, ldu, low, high, cos, sin),
                        (low, high, cos, sin) -> JVM.rotate(n, v, offsetV, ldv, low, high, cos, sin), (i, j) -> JVM.exchange(m, u, offsetU, ldu, i, j),
                        (i, j) -> JVM.exchange(n, v, offsetV, ldv, i, j), j -> JVM.negate(n, v, offsetV, ldv, j));

            } else {

                BDSQR.JVM.invoke(d, e, RotateColumns.NULL, RotateColumns.NULL, ExchangeColumns.NULL, ExchangeColumns.NULL, NegateColumn.NULL);
            }

            System.arraycopy(d, 0, s, offsetS, n);
        }

        /**
         * The pure Java implementation of
         * {@link GESDD#invoke(boolean, boolean, int, int, double[], int, int, double[], int, double[], int, int, double[], int, int)},
         * without argument checks, for m, n > 0.
         */
        static int invoke(final boolean vectors, final boolean fullSize, final int m, final int n, final double[] a, final int offsetA, final int lda,
                final double[] s, final int offsetS, final double[] u, final int offsetU, final int ldu, final double[] v, final int offsetV, final int ldv) {

            if (m >= n) {

                JVM.tall(vectors, fullSize, m, n, a, offsetA, lda, s, offsetS, u, offsetU, ldu, v, offsetV, ldv);

            } else {

                double[] transposed = new double[n * m];
                for (int j = 0; j < n; j++) {
                    int column = offsetA + j * lda;
                    for (int i = 0; i < m; i++) {
                        transposed[j + i * n] = a[column + i];
                    }
                }

                JVM.tall(vectors, fullSize, n, m, transposed, 0, n, s, offsetS, v, offsetV, ldv, u, offsetU, ldu);
            }

            return 0;
        }

    }

    /**
     * Native {@code dgesdd}, initialised only when {@link NativeLibrary#isAvailable()}. All arguments are
     * passed by reference, as to any Fortran routine, followed by the length of the character argument. It is
     * not linked as critical: it is passed a copy of the matrix in native memory, and the JVM can reach
     * safepoints (and collect garbage) during the call.
     */
    static final class Native {

        static final MethodHandle DGESDD_OFF_HEAP = NativeLibrary.downcall("dgesdd_", FunctionDescriptor.ofVoid(ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS,
                ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS, JAVA_LONG), false);

        /**
         * Native memory holding A<sup>T</sup>, n-by-m with leading dimension ld, where A is m-by-n and stored
         * in its array from an offset with a leading dimension.
         */
        private static MemorySegment copyOfTranspose(final Arena arena, final double[] a, final int offsetA, final int m, final int n, final int lda,
                final int ld) {
            MemorySegment retVal = CBLAS.allocateMatrix(arena, n, m, ld);
            for (int j = 0; j < n; j++) {
                int column = offsetA + j * lda;
                for (int i = 0; i < m; i++) {
                    retVal.setAtIndex(JAVA_DOUBLE, j + (long) i * ld, a[column + i]);
                }
            }
            return retVal;
        }

        /**
         * Copies A, or A<sup>T</sup> if m < n, to native memory, asks {@link #DGESDD_OFF_HEAP} for the
         * optimal workspace size, calls it, and copies the singular values, and the singular vectors if
         * computed, back. A is not changed. Requires m, n > 0.
         */
        static int invoke(final boolean vectors, final boolean fullSize, final int m, final int n, final double[] a, final int offsetA, final int lda,
                final double[] s, final int offsetS, final double[] u, final int offsetU, final int ldu, final double[] v, final int offsetV, final int ldv) {

            int k = Math.min(m, n);

            Objects.checkFromIndexSize(offsetS, k, s.length);
            if (vectors) {
                Objects.checkFromIndexSize(offsetU, ((fullSize ? m : k) - 1) * ldu + m, u.length);
                Objects.checkFromIndexSize(offsetV, ((fullSize ? n : k) - 1) * ldv + n, v.length);
            }

            // The tall matrix decomposed, B = A or A^T: B = L * diag(s) * R^T, L rows-by-columnsL and R k-by-k
            boolean transposed = m < n;
            int rows = transposed ? n : m;
            int columnsL = fullSize ? rows : k;
            double[] left = transposed ? v : u;
            int offsetLeft = transposed ? offsetV : offsetU;
            int ldLeft = transposed ? ldv : ldu;
            double[] right = transposed ? u : v;
            int offsetRight = transposed ? offsetU : offsetV;
            int ldRight = transposed ? ldu : ldv;

            try (Arena arena = Arena.ofConfined()) {

                int copyLDB = CBLAS.leadingDimensionOfCopy(rows);
                int copyLDL = vectors ? CBLAS.leadingDimensionOfCopy(rows) : 1;
                int copyLDRT = vectors ? CBLAS.leadingDimensionOfCopy(k) : 1;

                MemorySegment segmentJobz = arena.allocateFrom(JAVA_BYTE, (byte) (vectors ? fullSize ? 'A' : 'S' : 'N'));
                MemorySegment segmentM = arena.allocateFrom(JAVA_INT, rows);
                MemorySegment segmentN = arena.allocateFrom(JAVA_INT, k);
                MemorySegment segmentB = transposed ? Native.copyOfTranspose(arena, a, offsetA, m, n, lda, copyLDB)
                        : CBLAS.copyOfMatrix(arena, a, offsetA, m, n, lda, copyLDB);
                MemorySegment segmentLDB = arena.allocateFrom(JAVA_INT, copyLDB);
                MemorySegment segmentS = arena.allocate(JAVA_DOUBLE, k);
                MemorySegment segmentL = vectors ? CBLAS.allocateMatrix(arena, rows, columnsL, copyLDL) : arena.allocate(JAVA_DOUBLE);
                MemorySegment segmentLDL = arena.allocateFrom(JAVA_INT, copyLDL);
                MemorySegment segmentRT = vectors ? CBLAS.allocateMatrix(arena, k, k, copyLDRT) : arena.allocate(JAVA_DOUBLE);
                MemorySegment segmentLDRT = arena.allocateFrom(JAVA_INT, copyLDRT);
                MemorySegment segmentIWork = arena.allocate(JAVA_INT, 8L * k);
                MemorySegment segmentInfo = arena.allocate(JAVA_INT);

                MemorySegment query = arena.allocate(JAVA_DOUBLE);
                DGESDD_OFF_HEAP.invokeExact(segmentJobz, segmentM, segmentN, segmentB, segmentLDB, segmentS, segmentL, segmentLDL, segmentRT, segmentLDRT,
                        query, arena.allocateFrom(JAVA_INT, -1), segmentIWork, segmentInfo, 1L);
                int lwork = Math.max(1, (int) query.get(JAVA_DOUBLE, 0L));

                DGESDD_OFF_HEAP.invokeExact(segmentJobz, segmentM, segmentN, segmentB, segmentLDB, segmentS, segmentL, segmentLDL, segmentRT, segmentLDRT,
                        arena.allocate(JAVA_DOUBLE, lwork), arena.allocateFrom(JAVA_INT, lwork), segmentIWork, segmentInfo, 1L);

                int info = segmentInfo.get(JAVA_INT, 0L);

                if (vectors) {
                    CBLAS.copyBackMatrix(segmentL, copyLDL, left, offsetLeft, rows, columnsL, ldLeft);
                    for (int i = 0; i < k; i++) {
                        int column = offsetRight + i * ldRight;
                        for (int j = 0; j < k; j++) {
                            right[column + j] = segmentRT.getAtIndex(JAVA_DOUBLE, i + (long) j * copyLDRT);
                        }
                    }
                }
                MemorySegment.copy(segmentS, JAVA_DOUBLE, 0L, s, offsetS, k);

                return info;

            } catch (RuntimeException | Error cause) {
                throw cause;
            } catch (Throwable cause) {
                throw new ProgrammingError(cause);
            }
        }

    }

    /**
     * The native implementation is used, if there is one, when m*n*min(m,n) is larger than this and the
     * singular vectors are computed.
     */
    private static final double NATIVE_THRESHOLD = 400_000;
    /**
     * The native implementation is used, if there is one, when m*n*min(m,n) is larger than this and only the
     * singular values are computed.
     */
    private static final double NATIVE_THRESHOLD_VALUES = 5_000;

    static final boolean NATIVE = NativeLibrary.isAvailable() && Native.DGESDD_OFF_HEAP != null;

    /**
     * {@code dgesdd} with JOBZ = 'S' (economy size), 'A' (full size) or 'N' (the singular values only), and V
     * instead of V<sup>T</sup>
     * <p>
     * A, U and V are column-major, each stored in its array from an offset with a leading dimension. The
     * singular values s(i), i = 1, ..., min(m,n), in descending order, are stored in
     * {@code s[offsetS + i - 1]}. A is destroyed. With the singular values only, U and V are not referenced
     * (and may be null).
     *
     * @param vectors  The singular vectors are computed if true, otherwise the singular values only
     * @param fullSize U is m-by-m and V n-by-n if true, otherwise m-by-min(m,n) and n-by-min(m,n)
     * @param m        The number of rows of A
     * @param n        The number of columns of A
     * @return 0, or i > 0 if the algorithm failed to converge
     */
    public static int invoke(final boolean vectors, final boolean fullSize, final int m, final int n, final double[] a, final int offsetA, final int lda,
            final double[] s, final int offsetS, final double[] u, final int offsetU, final int ldu, final double[] v, final int offsetV, final int ldv) {

        ArrayOperation.checkArgument("GESDD", "m", m >= 0);
        ArrayOperation.checkArgument("GESDD", "n", n >= 0);
        ArrayOperation.checkArgument("GESDD", "lda", lda >= Math.max(1, m));
        ArrayOperation.checkArgument("GESDD", "ldu", !vectors || ldu >= Math.max(1, m));
        ArrayOperation.checkArgument("GESDD", "ldv", !vectors || ldv >= Math.max(1, n));

        if (m == 0 || n == 0) {
            return 0;
        }

        if (GESDD.isNative(vectors, m, n)) {
            return Native.invoke(vectors, fullSize, m, n, a, offsetA, lda, s, offsetS, u, offsetU, ldu, v, offsetV, ldv);
        } else {
            return JVM.invoke(vectors, fullSize, m, n, a, offsetA, lda, s, offsetS, u, offsetU, ldu, v, offsetV, ldv);
        }
    }

    /**
     * Whether to use the native implementation: there is one, and m*n*min(m,n) is larger than
     * {@link #NATIVE_THRESHOLD} when the singular vectors are computed, {@link #NATIVE_THRESHOLD_VALUES} when
     * only the singular values are.
     */
    public static boolean isNative(final boolean vectors, final double m, final double n) {
        return NATIVE && m * n * Math.min(m, n) > (vectors ? NATIVE_THRESHOLD : NATIVE_THRESHOLD_VALUES);
    }

}

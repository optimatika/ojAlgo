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
import java.util.Objects;

import org.ojalgo.ProgrammingError;
import org.ojalgo.array.operation.AXPY;
import org.ojalgo.array.operation.ArrayOperation;
import org.ojalgo.array.operation.DOT;
import org.ojalgo.array.operation.NativeLibrary;
import org.ojalgo.concurrent.DivideAndConquer;
import org.ojalgo.concurrent.Parallelism;
import org.ojalgo.scalar.Scalar;

/**
 * LAPACK {@code ?geqrf}: QR factorisation of a general m-by-n matrix A:
 *
 * <pre>{@code
 * A = Q * R
 * }</pre>
 *
 * R is upper triangular (upper trapezoidal if m < n) and overwrites A on and above the diagonal. Q is
 * orthogonal, the product of k = min(m,n) elementary reflectors:
 *
 * <pre>{@code
 * Q = H(1) * H(2) * ... * H(k)
 * H(i) = I - tau(i) * v * v^T
 * }</pre>
 *
 * v(1:i-1) = 0 and v(i) = 1 (not stored), and v(i+1:m) is stored below the diagonal in column i of A. With
 * tau(i) = 0, H(i) is the identity.
 * <p>
 * {@link ORGQR} forms Q explicitly, and {@link ORMQR} multiplies a matrix by Q or Q<sup>T</sup>.
 *
 * @see NativeLibrary
 */
public abstract class GEQRF implements LAPACK {

    /**
     * The pure Java implementation. Everything it calls is pure Java as well – the {@code JVM}
     * implementations of other operations, never their dispatching {@code invoke} methods (that may call
     * native code).
     * <p>
     * Unblocked: for each column, the reflector that zeroes the elements below the diagonal is generated and
     * applied to the columns to the right, split over several threads when that is large, see
     * {@link GEQRF#THRESHOLD}. The column is scaled by its element of largest magnitude. If what is below the
     * diagonal is exactly 0, there is no reflector: the column is left as it is and tau(i) = 0.
     */
    static final class JVM {

        private static final DivideAndConquer.Divider DIVIDER = Parallelism.CORES.newDivider();

        /**
         * Applies H = I - tau * v * v<sup>T</sup> to rows row to m-1 of the columns from (inclusive) to
         * (exclusive) of A.
         */
        private static void reflectColumns(final int m, final double[] vector, final int offsetV, final double tau, final double[] a, final int offsetA,
                final int lda, final int row, final int from, final int to) {
            int count = m - row;
            for (int j = from; j < to; j++) {
                int column = offsetA + row + j * lda;
                double scale = tau * DOT.JVM.invoke(count, a, column, 1, vector, offsetV, 1);
                AXPY.JVM.invoke(count, -scale, vector, offsetV, 1, a, column, 1);
            }
        }

        /**
         * @see #reflectColumns(int, double[], int, double, double[], int, int, int, int, int)
         */
        private static <N extends Scalar<N>> void reflectColumns(final int m, final N[] vector, final int offsetV, final N tau, final N[] a, final int offsetA,
                final int lda, final int row, final int from, final int to, final Scalar.Factory<N> scalar) {
            for (int j = from; j < to; j++) {
                int column = offsetA + j * lda;
                Scalar<N> scale = scalar.zero();
                for (int p = row; p < m; p++) {
                    scale = scale.add(vector[offsetV + p - row].conjugate().multiply(a[column + p]));
                }
                scale = scale.multiply(tau);
                for (int p = row; p < m; p++) {
                    a[column + p] = a[column + p].subtract(vector[offsetV + p - row].multiply(scale)).get();
                }
            }
        }

        /**
         * Generates the reflector for the part of a column from row k down (stored in {@code a} from index
         * {@code column}): its first element after the reflection, and v below it, overwrite the column.
         * There is a reflector unless the part below row k is exactly 0; then the column is left as it is.
         * The first element after the reflection is -norm when the k:th element is positive or +0, and +norm
         * when it is negative or -0.
         *
         * @return tau, or 0 when there is no reflector
         */
        static double generate(final int m, final double[] a, final int column, final int k) {

            double tailInf = ZERO;
            for (int i = k + 1; i < m; i++) {
                tailInf = Math.max(tailInf, Math.abs(a[column + i]));
            }

            if (tailInf == ZERO) {
                return ZERO;
            }

            double normInf = Math.max(Math.abs(a[column + k]), tailInf);

            double norm2 = ZERO;
            for (int i = k + 1; i < m; i++) {
                double value = a[column + i] / normInf;
                norm2 += value * value;
            }

            double scale = a[column + k] / normInf;
            norm2 += scale * scale;
            norm2 = Math.sqrt(norm2);

            if (Math.copySign(ONE, scale) < ZERO) {
                a[column + k] = norm2 * normInf;
                scale -= norm2;
            } else {
                a[column + k] = -norm2 * normInf;
                scale += norm2;
            }

            for (int i = k + 1; i < m; i++) {
                a[column + i] = a[column + i] / normInf / scale;
            }

            return Math.abs(scale) / norm2;
        }

        /**
         * As {@link #generate(int, double[], int, int)}, for other number types. The first element after the
         * reflection is -norm times the phase of the k:th element.
         *
         * @return tau, or null when there is no reflector
         */
        static <N extends Scalar<N>> N generate(final int m, final N[] a, final int column, final int k, final Scalar.Factory<N> scalar) {

            double normInf = ZERO;
            double tailInf = ZERO;
            for (int i = k; i < m; i++) {
                double norm = a[column + i].norm();
                normInf = Math.max(normInf, norm);
                if (i > k) {
                    tailInf = Math.max(tailInf, norm);
                }
            }

            if (tailInf == ZERO) {
                return null;
            }

            double norm2 = ZERO;
            for (int i = k + 1; i < m; i++) {
                Scalar<N> value = a[column + i].divide(normInf);
                norm2 += value.norm() * value.norm();
                a[column + i] = value.get();
            }

            Scalar<N> scale = a[column + k].divide(normInf);
            norm2 += scale.norm() * scale.norm();
            norm2 = Math.sqrt(norm2);

            Scalar<N> signum = scale.normalised();
            a[column + k] = signum.negate().multiply(norm2 * normInf).get();
            scale = scale.add(signum.multiply(norm2)).get();

            for (int i = k + 1; i < m; i++) {
                a[column + i] = a[column + i].divide(scale).get();
            }

            return scalar.cast(scale.norm() / norm2);
        }

        /**
         * The pure Java implementation of {@link GEQRF#invoke(int, int, double[], int, int, double[], int)},
         * without argument checks, for m, n > 0.
         */
        static void invoke(final int m, final int n, final double[] a, final int offsetA, final int lda, final double[] tau, final int offsetTau) {

            for (int k = 0, limit = Math.min(m, n); k < limit; k++) {

                int column = offsetA + k * lda;

                double beta = k + 1 < m ? JVM.generate(m, a, column, k) : ZERO;

                tau[offsetTau + k] = beta;

                if (beta != ZERO && k + 1 < n) {
                    // v(k) = 1 temporarily in place of R(k,k), so that the column is v
                    double diagonal = a[column + k];
                    a[column + k] = ONE;
                    JVM.reflect(m, a, column + k, beta, a, offsetA, lda, k, k + 1, n);
                    a[column + k] = diagonal;
                }
            }
        }

        /**
         * The pure Java implementation of
         * {@link GEQRF#invoke(int, int, Scalar[], int, int, Scalar[], int, Scalar.Factory)}, without argument
         * checks, for m, n > 0.
         */
        static <N extends Scalar<N>> void invoke(final int m, final int n, final N[] a, final int offsetA, final int lda, final N[] tau, final int offsetTau,
                final Scalar.Factory<N> scalar) {

            N zero = scalar.zero().get();

            for (int k = 0, limit = Math.min(m, n); k < limit; k++) {

                int column = offsetA + k * lda;

                N beta = k + 1 < m ? JVM.generate(m, a, column, k, scalar) : null;

                tau[offsetTau + k] = beta != null ? beta : zero;

                if (beta != null && k + 1 < n) {
                    // v(k) = 1 temporarily in place of R(k,k), so that the column is v
                    N diagonal = a[column + k];
                    a[column + k] = scalar.one().get();
                    JVM.reflect(m, a, column + k, beta, a, offsetA, lda, k, k + 1, n, scalar);
                    a[column + k] = diagonal;
                }
            }
        }

        /**
         * Applies H = I - tau * v * v<sup>T</sup>, from the left, to rows row to m-1 of the columns from
         * (inclusive) to (exclusive) of A, split over several threads when that is large, see
         * {@link GEQRF#THRESHOLD}. v(row) to v(m-1) are stored from {@code vector[offsetV]}, v(row) = 1
         * included, and must not be in any of those columns.
         */
        static void reflect(final int m, final double[] vector, final int offsetV, final double tau, final double[] a, final int offsetA, final int lda,
                final int row, final int from, final int to) {

            // The dot product and the update: 2 multiply-adds per element
            int chunk = MatrixOperation.chunk(THRESHOLD, 2.0 * (m - row));

            if (to - from > chunk) {

                DIVIDER.threshold(chunk).divide(from, to, (first, limit) -> JVM.reflectColumns(m, vector, offsetV, tau, a, offsetA, lda, row, first, limit));

            } else {

                JVM.reflectColumns(m, vector, offsetV, tau, a, offsetA, lda, row, from, to);
            }
        }

        /**
         * As {@link #reflect(int, double[], int, double, double[], int, int, int, int, int)}, for other
         * number types: H = I - tau * v * v<sup>H</sup>.
         */
        static <N extends Scalar<N>> void reflect(final int m, final N[] vector, final int offsetV, final N tau, final N[] a, final int offsetA, final int lda,
                final int row, final int from, final int to, final Scalar.Factory<N> scalar) {

            int chunk = MatrixOperation.chunk(THRESHOLD, 2.0 * (m - row));

            if (to - from > chunk) {

                DIVIDER.threshold(chunk).divide(from, to,
                        (first, limit) -> JVM.reflectColumns(m, vector, offsetV, tau, a, offsetA, lda, row, first, limit, scalar));

            } else {

                JVM.reflectColumns(m, vector, offsetV, tau, a, offsetA, lda, row, from, to, scalar);
            }
        }

    }

    /**
     * Native {@code dgeqrf}, initialised only when {@link NativeLibrary#isAvailable()}. All arguments are
     * passed by reference, as to any Fortran routine. It is not linked as critical: it is passed a copy of
     * the matrix in native memory, and the JVM can reach safepoints (and collect garbage) during the call.
     */
    static final class Native {

        static final MethodHandle DGEQRF_OFF_HEAP = NativeLibrary.downcall("dgeqrf_",
                FunctionDescriptor.ofVoid(ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS), false);

        /**
         * Copies A to native memory, asks {@link #DGEQRF_OFF_HEAP} for the optimal workspace size, calls it
         * and copies the factors and tau back. Requires m, n > 0.
         */
        static void invoke(final int m, final int n, final double[] a, final int offsetA, final int lda, final double[] tau, final int offsetTau) {

            int minDim = Math.min(m, n);
            Objects.checkFromIndexSize(offsetTau, minDim, tau.length);

            try (Arena arena = Arena.ofConfined()) {

                int copyLDA = CBLAS.leadingDimensionOfCopy(m);

                MemorySegment segmentA = CBLAS.copyOfMatrix(arena, a, offsetA, m, n, lda, copyLDA);
                MemorySegment segmentTau = arena.allocate(JAVA_DOUBLE, minDim);
                MemorySegment segmentInfo = arena.allocate(JAVA_INT);
                MemorySegment segmentM = arena.allocateFrom(JAVA_INT, m);
                MemorySegment segmentLDA = arena.allocateFrom(JAVA_INT, copyLDA);
                MemorySegment segmentN = arena.allocateFrom(JAVA_INT, n);

                MemorySegment query = arena.allocate(JAVA_DOUBLE);
                DGEQRF_OFF_HEAP.invokeExact(segmentM, segmentN, segmentA, segmentLDA, segmentTau, query, arena.allocateFrom(JAVA_INT, -1), segmentInfo);
                int lwork = Math.max(n, (int) query.get(JAVA_DOUBLE, 0L));

                DGEQRF_OFF_HEAP.invokeExact(segmentM, segmentN, segmentA, segmentLDA, segmentTau, arena.allocate(JAVA_DOUBLE, lwork),
                        arena.allocateFrom(JAVA_INT, lwork), segmentInfo);

                CBLAS.copyBackMatrix(segmentA, copyLDA, a, offsetA, m, n, lda);
                MemorySegment.copy(segmentTau, JAVA_DOUBLE, 0L, tau, offsetTau, minDim);

            } catch (RuntimeException | Error cause) {
                throw cause;
            } catch (Throwable cause) {
                throw new ProgrammingError(cause);
            }
        }

    }

    /**
     * The native implementation is used, if there is one, when m*n*min(m,n) is larger than this.
     */
    private static final double NATIVE_THRESHOLD = 300_000;

    static final boolean NATIVE = NativeLibrary.isAvailable() && Native.DGEQRF_OFF_HEAP != null;

    /**
     * The Java implementation splits the application of a reflector to the columns to its right over several
     * threads when that is larger than this cubed (in multiply-adds). Each part then gets more than half of
     * that. Also used by {@link ORGQR}.
     */
    static int THRESHOLD = 48;

    /**
     * {@code dgeqrf}: A = Q*R, R and the reflectors that make Q overwrite A
     * <p>
     * A is column-major, stored in its array from an offset with a leading dimension. tau(i), i = 1, ...,
     * min(m,n), is stored in {@code tau[offsetTau + i - 1]}.
     *
     * @param m The number of rows of A
     * @param n The number of columns of A
     */
    public static void invoke(final int m, final int n, final double[] a, final int offsetA, final int lda, final double[] tau, final int offsetTau) {

        ArrayOperation.checkArgument("GEQRF", "m", m >= 0);
        ArrayOperation.checkArgument("GEQRF", "n", n >= 0);
        ArrayOperation.checkArgument("GEQRF", "lda", lda >= Math.max(1, m));

        if (m == 0 || n == 0) {
            return;
        }

        if (GEQRF.isNative(m, n)) {
            Native.invoke(m, n, a, offsetA, lda, tau, offsetTau);
        } else {
            JVM.invoke(m, n, a, offsetA, lda, tau, offsetTau);
        }
    }

    /**
     * As {@link #invoke(int, int, double[], int, int, double[], int)}, for other number types: Q is unitary,
     * H(i) = I - tau(i) * v * v<sup>H</sup> with tau(i) real, and R(i,i) is -norm times the phase of the
     * element it replaces. tau(i) = 0 where there is no reflector. There is only a pure Java implementation.
     */
    public static <N extends Scalar<N>> void invoke(final int m, final int n, final N[] a, final int offsetA, final int lda, final N[] tau, final int offsetTau,
            final Scalar.Factory<N> scalar) {

        ArrayOperation.checkArgument("GEQRF", "m", m >= 0);
        ArrayOperation.checkArgument("GEQRF", "n", n >= 0);
        ArrayOperation.checkArgument("GEQRF", "lda", lda >= Math.max(1, m));

        if (m == 0 || n == 0) {
            return;
        }

        JVM.invoke(m, n, a, offsetA, lda, tau, offsetTau, scalar);
    }

    /**
     * Whether to use the native implementation: there is one, and m*n*min(m,n) is larger than
     * {@link #NATIVE_THRESHOLD}.
     */
    public static boolean isNative(final double m, final double n) {
        return NATIVE && m * n * Math.min(m, n) > NATIVE_THRESHOLD;
    }

}

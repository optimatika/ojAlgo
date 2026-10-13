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
import org.ojalgo.array.operation.SCAL;
import org.ojalgo.scalar.Scalar;

/**
 * LAPACK {@code ?orgqr}: generates the m-by-n matrix Q with orthonormal columns, the first n columns of the
 * product of k elementary reflectors returned by {@link GEQRF}:
 *
 * <pre>{@code
 * Q = H(1) * H(2) * ... * H(k)
 * }</pre>
 *
 * m >= n >= k >= 0. With n = min(m,n) of the factorisation that is the Q of the economy size factorisation,
 * and with n = m the full m-by-m orthogonal matrix.
 *
 * @see NativeLibrary
 */
public abstract class ORGQR implements LAPACK {

    /**
     * The pure Java implementation. Everything it calls is pure Java as well – the {@code JVM}
     * implementations of other operations, never their dispatching {@code invoke} methods (that may call
     * native code).
     * <p>
     * Unblocked: columns k to n-1 are set to those of the identity, then the reflectors are applied in
     * reverse order, H(i) to columns i+1 to n-1 (split over several threads when that is large, see
     * {@link GEQRF#THRESHOLD}), and column i is formed as H(i) times column i of the identity.
     */
    static final class JVM {

        /**
         * The pure Java implementation of
         * {@link ORGQR#invoke(int, int, int, double[], int, int, double[], int)}, without argument checks,
         * for m >= n > 0 and n >= k >= 0.
         */
        static void invoke(final int m, final int n, final int k, final double[] a, final int offsetA, final int lda, final double[] tau, final int offsetTau) {

            for (int j = k; j < n; j++) {
                int column = offsetA + j * lda;
                Arrays.fill(a, column, column + m, ZERO);
                a[column + j] = ONE;
            }

            for (int i = k - 1; i >= 0; i--) {

                int column = offsetA + i * lda;
                double beta = tau[offsetTau + i];

                if (beta != ZERO) {

                    a[column + i] = ONE;
                    if (i + 1 < n) {
                        GEQRF.JVM.reflect(m, a, column + i, beta, a, offsetA, lda, i, i + 1, n);
                    }
                    if (i + 1 < m) {
                        SCAL.JVM.invoke(m - i - 1, -beta, a, column + i + 1, 1);
                    }
                    a[column + i] = ONE - beta;

                } else {

                    Arrays.fill(a, column + i + 1, column + m, ZERO);
                    a[column + i] = ONE;
                }

                Arrays.fill(a, column, column + i, ZERO);
            }
        }

        /**
         * The pure Java implementation of
         * {@link ORGQR#invoke(int, int, int, Scalar[], int, int, Scalar[], int, Scalar.Factory)}, without
         * argument checks, for m >= n > 0 and n >= k >= 0.
         */
        static <N extends Scalar<N>> void invoke(final int m, final int n, final int k, final N[] a, final int offsetA, final int lda, final N[] tau,
                final int offsetTau, final Scalar.Factory<N> scalar) {

            N zero = scalar.zero().get();
            N one = scalar.one().get();

            for (int j = k; j < n; j++) {
                int column = offsetA + j * lda;
                Arrays.fill(a, column, column + m, zero);
                a[column + j] = one;
            }

            for (int i = k - 1; i >= 0; i--) {

                int column = offsetA + i * lda;
                N beta = tau[offsetTau + i];

                if (beta.norm() != ZERO) {

                    a[column + i] = one;
                    if (i + 1 < n) {
                        GEQRF.JVM.reflect(m, a, column + i, beta, a, offsetA, lda, i, i + 1, n, scalar);
                    }
                    Scalar<N> negated = beta.negate();
                    for (int p = i + 1; p < m; p++) {
                        a[column + p] = negated.multiply(a[column + p]).get();
                    }
                    a[column + i] = scalar.one().subtract(beta).get();

                } else {

                    Arrays.fill(a, column + i + 1, column + m, zero);
                    a[column + i] = one;
                }

                Arrays.fill(a, column, column + i, zero);
            }
        }

    }

    /**
     * Native {@code dorgqr}, initialised only when {@link NativeLibrary#isAvailable()}. All arguments are
     * passed by reference, as to any Fortran routine. It is not linked as critical: it is passed a copy of
     * the matrix in native memory, and the JVM can reach safepoints (and collect garbage) during the call.
     */
    static final class Native {

        static final MethodHandle DORGQR_OFF_HEAP = NativeLibrary.downcall("dorgqr_",
                FunctionDescriptor.ofVoid(ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS), false);

        /**
         * Copies A and tau to native memory, asks {@link #DORGQR_OFF_HEAP} for the optimal workspace size,
         * calls it and copies Q back. Requires m >= n > 0 and n >= k >= 0.
         */
        static void invoke(final int m, final int n, final int k, final double[] a, final int offsetA, final int lda, final double[] tau, final int offsetTau) {

            try (Arena arena = Arena.ofConfined()) {

                int copyLDA = CBLAS.leadingDimensionOfCopy(m);

                MemorySegment segmentA = CBLAS.copyOfMatrix(arena, a, offsetA, m, n, lda, copyLDA);
                MemorySegment segmentTau = arena.allocateFrom(JAVA_DOUBLE, MemorySegment.ofArray(tau), JAVA_DOUBLE, JAVA_DOUBLE.byteSize() * offsetTau, k);
                MemorySegment segmentInfo = arena.allocate(JAVA_INT);
                MemorySegment segmentM = arena.allocateFrom(JAVA_INT, m);
                MemorySegment segmentLDA = arena.allocateFrom(JAVA_INT, copyLDA);
                MemorySegment segmentN = arena.allocateFrom(JAVA_INT, n);
                MemorySegment segmentK = arena.allocateFrom(JAVA_INT, k);

                MemorySegment query = arena.allocate(JAVA_DOUBLE);
                DORGQR_OFF_HEAP.invokeExact(segmentM, segmentN, segmentK, segmentA, segmentLDA, segmentTau, query, arena.allocateFrom(JAVA_INT, -1),
                        segmentInfo);
                int lwork = Math.max(n, (int) query.get(JAVA_DOUBLE, 0L));

                DORGQR_OFF_HEAP.invokeExact(segmentM, segmentN, segmentK, segmentA, segmentLDA, segmentTau, arena.allocate(JAVA_DOUBLE, lwork),
                        arena.allocateFrom(JAVA_INT, lwork), segmentInfo);

                CBLAS.copyBackMatrix(segmentA, copyLDA, a, offsetA, m, n, lda);

            } catch (RuntimeException | Error cause) {
                throw cause;
            } catch (Throwable cause) {
                throw new ProgrammingError(cause);
            }
        }

    }

    /**
     * The native implementation is used, if there is one, when m*n*k is larger than this.
     */
    private static final double NATIVE_THRESHOLD = 300_000;

    static final boolean NATIVE = NativeLibrary.isAvailable() && Native.DORGQR_OFF_HEAP != null;

    /**
     * {@code dorgqr}: Q overwrites A
     * <p>
     * A is column-major, stored in its array from an offset with a leading dimension. On entry, its first k
     * columns hold the reflectors as returned by {@link GEQRF}, below the diagonal; the rest of A need not be
     * set. tau(i), i = 1, ..., k, is stored in {@code tau[offsetTau + i - 1]}.
     *
     * @param m The number of rows of A and Q
     * @param n The number of columns of A and Q, at most m
     * @param k The number of reflectors, at most n
     */
    public static void invoke(final int m, final int n, final int k, final double[] a, final int offsetA, final int lda, final double[] tau,
            final int offsetTau) {

        ArrayOperation.checkArgument("ORGQR", "m", m >= 0);
        ArrayOperation.checkArgument("ORGQR", "n", n >= 0 && n <= m);
        ArrayOperation.checkArgument("ORGQR", "k", k >= 0 && k <= n);
        ArrayOperation.checkArgument("ORGQR", "lda", lda >= Math.max(1, m));

        if (n == 0) {
            return;
        }

        if (ORGQR.isNative(m, n, k)) {
            Native.invoke(m, n, k, a, offsetA, lda, tau, offsetTau);
        } else {
            JVM.invoke(m, n, k, a, offsetA, lda, tau, offsetTau);
        }
    }

    /**
     * As {@link #invoke(int, int, int, double[], int, int, double[], int)}, for other number types: Q is
     * unitary, the product of the reflectors H(i) = I - tau(i) * v * v<sup>H</sup>. There is only a pure Java
     * implementation.
     */
    public static <N extends Scalar<N>> void invoke(final int m, final int n, final int k, final N[] a, final int offsetA, final int lda, final N[] tau,
            final int offsetTau, final Scalar.Factory<N> scalar) {

        ArrayOperation.checkArgument("ORGQR", "m", m >= 0);
        ArrayOperation.checkArgument("ORGQR", "n", n >= 0 && n <= m);
        ArrayOperation.checkArgument("ORGQR", "k", k >= 0 && k <= n);
        ArrayOperation.checkArgument("ORGQR", "lda", lda >= Math.max(1, m));

        if (n == 0) {
            return;
        }

        JVM.invoke(m, n, k, a, offsetA, lda, tau, offsetTau, scalar);
    }

    /**
     * Whether to use the native implementation: there is one, and m*n*k is larger than
     * {@link #NATIVE_THRESHOLD}.
     */
    static boolean isNative(final double m, final double n, final double k) {
        return NATIVE && m * n * k > NATIVE_THRESHOLD;
    }

}

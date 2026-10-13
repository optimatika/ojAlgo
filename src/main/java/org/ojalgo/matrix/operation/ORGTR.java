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
import org.ojalgo.array.operation.ArrayOperation;
import org.ojalgo.array.operation.NativeLibrary;
import org.ojalgo.scalar.Scalar;

/**
 * LAPACK {@code ?orgtr}, for the lower triangle: generates the n-by-n orthogonal matrix Q of the reflectors
 * returned by {@link SYTRD}:
 *
 * <pre>{@code
 * Q = H(1) * H(2) * ... * H(n-1)
 * }</pre>
 *
 * The first row and column of Q are those of the identity, and the rest is the Q of {@link ORGQR} for the
 * (shifted) reflectors. For other number types Q is unitary; there is only a pure Java implementation for
 * those.
 *
 * @see NativeLibrary
 */
public abstract class ORGTR implements LAPACK {

    /**
     * The pure Java implementations. Everything they call is pure Java as well – the {@code JVM}
     * implementations of other operations, never their dispatching {@code invoke} methods (that may call
     * native code).
     * <p>
     * The reflectors are moved one column to the right, the first row and column set to those of the
     * identity, and the rest formed by {@link ORGQR}.
     */
    static final class JVM {

        /**
         * The pure Java implementation of {@link ORGTR#invoke(int, double[], int, int, double[], int)},
         * without argument checks, for n > 0.
         */
        static void invoke(final int n, final double[] a, final int offsetA, final int lda, final double[] tau, final int offsetTau) {

            for (int j = n - 1; j > 0; j--) {
                int column = offsetA + j * lda;
                a[column] = ZERO;
                System.arraycopy(a, column - lda + j + 1, a, column + j + 1, n - j - 1);
            }

            a[offsetA] = ONE;
            for (int i = 1; i < n; i++) {
                a[offsetA + i] = ZERO;
            }

            if (n > 1) {
                ORGQR.JVM.invoke(n - 1, n - 1, n - 1, a, offsetA + 1 + lda, lda, tau, offsetTau);
            }
        }

        /**
         * The pure Java implementation of
         * {@link ORGTR#invoke(int, Scalar[], int, int, Scalar[], int, Scalar.Factory)}, without argument
         * checks, for n > 0.
         */
        static <N extends Scalar<N>> void invoke(final int n, final N[] a, final int offsetA, final int lda, final N[] tau, final int offsetTau,
                final Scalar.Factory<N> scalar) {

            N zero = scalar.zero().get();

            for (int j = n - 1; j > 0; j--) {
                int column = offsetA + j * lda;
                a[column] = zero;
                System.arraycopy(a, column - lda + j + 1, a, column + j + 1, n - j - 1);
            }

            a[offsetA] = scalar.one().get();
            for (int i = 1; i < n; i++) {
                a[offsetA + i] = zero;
            }

            if (n > 1) {
                ORGQR.JVM.invoke(n - 1, n - 1, n - 1, a, offsetA + 1 + lda, lda, tau, offsetTau, scalar);
            }
        }

    }

    /**
     * Native {@code dorgtr}, initialised only when {@link NativeLibrary#isAvailable()}. All arguments are
     * passed by reference, as to any Fortran routine, followed by the length of the character argument. It is
     * not linked as critical: it is passed a copy of the matrix in native memory, and the JVM can reach
     * safepoints (and collect garbage) during the call.
     */
    static final class Native {

        static final MethodHandle DORGTR_OFF_HEAP = NativeLibrary.downcall("dorgtr_",
                FunctionDescriptor.ofVoid(ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS, JAVA_LONG), false);

        /**
         * Copies A and tau to native memory, asks {@link #DORGTR_OFF_HEAP} for the optimal workspace size,
         * calls it and copies Q back. Requires n > 0.
         */
        static void invoke(final int n, final double[] a, final int offsetA, final int lda, final double[] tau, final int offsetTau) {

            try (Arena arena = Arena.ofConfined()) {

                int copyLDA = CBLAS.leadingDimensionOfCopy(n);

                MemorySegment segmentUplo = arena.allocateFrom(JAVA_BYTE, (byte) 'L');
                MemorySegment segmentA = CBLAS.copyOfMatrix(arena, a, offsetA, n, n, lda, copyLDA);
                MemorySegment segmentTau = n > 1
                        ? arena.allocateFrom(JAVA_DOUBLE, MemorySegment.ofArray(tau), JAVA_DOUBLE, JAVA_DOUBLE.byteSize() * offsetTau, n - 1)
                        : arena.allocate(JAVA_DOUBLE);
                MemorySegment segmentInfo = arena.allocate(JAVA_INT);
                MemorySegment segmentN = arena.allocateFrom(JAVA_INT, n);
                MemorySegment segmentLDA = arena.allocateFrom(JAVA_INT, copyLDA);

                MemorySegment query = arena.allocate(JAVA_DOUBLE);
                DORGTR_OFF_HEAP.invokeExact(segmentUplo, segmentN, segmentA, segmentLDA, segmentTau, query, arena.allocateFrom(JAVA_INT, -1), segmentInfo, 1L);
                int lwork = Math.max(Math.max(1, n - 1), (int) query.get(JAVA_DOUBLE, 0L));

                DORGTR_OFF_HEAP.invokeExact(segmentUplo, segmentN, segmentA, segmentLDA, segmentTau, arena.allocate(JAVA_DOUBLE, lwork),
                        arena.allocateFrom(JAVA_INT, lwork), segmentInfo, 1L);

                CBLAS.copyBackMatrix(segmentA, copyLDA, a, offsetA, n, n, lda);

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
    private static final double NATIVE_THRESHOLD = 300_000;

    static final boolean NATIVE = NativeLibrary.isAvailable() && Native.DORGTR_OFF_HEAP != null;

    /**
     * {@code dorgtr} with UPLO = 'L': Q overwrites A
     * <p>
     * A is column-major, stored in its array from an offset with a leading dimension. On entry, the elements
     * below its first subdiagonal hold the reflectors as returned by {@link SYTRD}; the rest of A need not be
     * set. tau(i), i = 1, ..., n-1, is stored in {@code tau[offsetTau + i - 1]}.
     *
     * @param n The order of A and Q
     */
    public static void invoke(final int n, final double[] a, final int offsetA, final int lda, final double[] tau, final int offsetTau) {

        ArrayOperation.checkArgument("ORGTR", "n", n >= 0);
        ArrayOperation.checkArgument("ORGTR", "lda", lda >= Math.max(1, n));

        if (n == 0) {
            return;
        }

        if (ORGTR.isNative(n)) {
            Native.invoke(n, a, offsetA, lda, tau, offsetTau);
        } else {
            JVM.invoke(n, a, offsetA, lda, tau, offsetTau);
        }
    }

    /**
     * As {@link #invoke(int, double[], int, int, double[], int)}, for other number types: Q is unitary, and
     * tau(i) = 0 where there is no reflector. There is only a pure Java implementation.
     */
    public static <N extends Scalar<N>> void invoke(final int n, final N[] a, final int offsetA, final int lda, final N[] tau, final int offsetTau,
            final Scalar.Factory<N> scalar) {

        ArrayOperation.checkArgument("ORGTR", "n", n >= 0);
        ArrayOperation.checkArgument("ORGTR", "lda", lda >= Math.max(1, n));

        if (n == 0) {
            return;
        }

        JVM.invoke(n, a, offsetA, lda, tau, offsetTau, scalar);
    }

    /**
     * Whether to use the native implementation: there is one, and n*n*n is larger than
     * {@link #NATIVE_THRESHOLD}.
     */
    static boolean isNative(final double n) {
        return NATIVE && n * n * n > NATIVE_THRESHOLD;
    }

}

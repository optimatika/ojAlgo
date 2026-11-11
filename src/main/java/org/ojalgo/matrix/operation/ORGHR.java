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

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;

import org.ojalgo.ProgrammingError;
import org.ojalgo.array.operation.ArrayOperation;
import org.ojalgo.array.operation.NativeLibrary;
import org.ojalgo.scalar.Scalar;

/**
 * LAPACK {@code ?orghr}, with ilo = 1 and ihi = n: generates the n-by-n orthogonal matrix Q of the reflectors
 * returned by {@link GEHRD}:
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
public abstract class ORGHR implements LAPACK {

    /**
     * The pure Java implementations. Everything they call is pure Java as well – the {@code JVM}
     * implementations of other operations, never their dispatching {@code invoke} methods (that may call
     * native code).
     * <p>
     * The reflectors are stored as by {@link SYTRD}, and Q is formed as by {@link ORGTR}.
     */
    static final class JVM {

        /**
         * The pure Java implementation of {@link ORGHR#invoke(int, double[], int, int, double[], int)},
         * without argument checks, for n > 0.
         */
        static void invoke(final int n, final double[] a, final int offsetA, final int lda, final double[] tau, final int offsetTau) {
            ORGTR.JVM.invoke(n, a, offsetA, lda, tau, offsetTau);
        }

        /**
         * The pure Java implementation of
         * {@link ORGHR#invoke(int, Scalar[], int, int, Scalar[], int, Scalar.Factory)}, without argument
         * checks, for n > 0.
         */
        static <N extends Scalar<N>> void invoke(final int n, final N[] a, final int offsetA, final int lda, final N[] tau, final int offsetTau,
                final Scalar.Factory<N> scalar) {
            ORGTR.JVM.invoke(n, a, offsetA, lda, tau, offsetTau, scalar);
        }

    }

    /**
     * Native {@code dorghr}, initialised only when {@link NativeLibrary#isAvailable()}. All arguments are
     * passed by reference, as to any Fortran routine. It is not linked as critical: it is passed a copy of
     * the matrix in native memory, and the JVM can reach safepoints (and collect garbage) during the call.
     */
    static final class Native {

        static final MethodHandle DORGHR_OFF_HEAP = NativeLibrary.downcall("dorghr_",
                FunctionDescriptor.ofVoid(ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS), false);

        /**
         * Copies A and tau to native memory, asks {@link #DORGHR_OFF_HEAP} for the optimal workspace size,
         * calls it and copies Q back. Requires n > 0.
         */
        static void invoke(final int n, final double[] a, final int offsetA, final int lda, final double[] tau, final int offsetTau) {

            try (Arena arena = Arena.ofConfined()) {

                int copyLDA = CBLAS.leadingDimensionOfCopy(n);

                MemorySegment segmentN = arena.allocateFrom(JAVA_INT, n);
                MemorySegment segmentILO = arena.allocateFrom(JAVA_INT, 1);
                MemorySegment segmentA = CBLAS.copyOfMatrix(arena, a, offsetA, n, n, lda, copyLDA);
                MemorySegment segmentLDA = arena.allocateFrom(JAVA_INT, copyLDA);
                MemorySegment segmentTau = n > 1
                        ? arena.allocateFrom(JAVA_DOUBLE, MemorySegment.ofArray(tau), JAVA_DOUBLE, JAVA_DOUBLE.byteSize() * offsetTau, n - 1)
                        : arena.allocate(JAVA_DOUBLE);
                MemorySegment segmentInfo = arena.allocate(JAVA_INT);

                MemorySegment query = arena.allocate(JAVA_DOUBLE);
                DORGHR_OFF_HEAP.invokeExact(segmentN, segmentILO, segmentN, segmentA, segmentLDA, segmentTau, query, arena.allocateFrom(JAVA_INT, -1),
                        segmentInfo);
                int lwork = Math.max(Math.max(1, n - 1), (int) query.get(JAVA_DOUBLE, 0L));

                DORGHR_OFF_HEAP.invokeExact(segmentN, segmentILO, segmentN, segmentA, segmentLDA, segmentTau, arena.allocate(JAVA_DOUBLE, lwork),
                        arena.allocateFrom(JAVA_INT, lwork), segmentInfo);

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
    private static final double NATIVE_THRESHOLD = 750_000;

    static final boolean NATIVE = NativeLibrary.isAvailable() && Native.DORGHR_OFF_HEAP != null;

    /**
     * {@code dorghr} with ILO = 1 and IHI = n: Q overwrites A
     * <p>
     * A is column-major, stored in its array from an offset with a leading dimension. On entry, the elements
     * below its first subdiagonal hold the reflectors as returned by {@link GEHRD}; the rest of A need not be
     * set. tau(i), i = 1, ..., n-1, is stored in {@code tau[offsetTau + i - 1]}.
     *
     * @param n The order of A and Q
     */
    public static void invoke(final int n, final double[] a, final int offsetA, final int lda, final double[] tau, final int offsetTau) {

        ArrayOperation.checkArgument("ORGHR", "n", n >= 0);
        ArrayOperation.checkArgument("ORGHR", "lda", lda >= Math.max(1, n));

        if (n == 0) {
            return;
        }

        if (ORGHR.isNative(n)) {
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

        ArrayOperation.checkArgument("ORGHR", "n", n >= 0);
        ArrayOperation.checkArgument("ORGHR", "lda", lda >= Math.max(1, n));

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

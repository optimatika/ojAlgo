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
import org.ojalgo.array.operation.ArrayOperation;
import org.ojalgo.array.operation.NativeLibrary;
import org.ojalgo.array.operation.SCAL;

/**
 * LAPACK {@code ?geev}, with JOBVL = 'N': the eigenvalues of a general n-by-n matrix A, and optionally its
 * right eigenvectors:
 *
 * <pre>{@code
 * A * VR = VR * D
 * }</pre>
 *
 * The eigenvalues are stored as wr (real parts) and wi (imaginary parts), a complex conjugate pair
 * consecutively, the one with the positive imaginary part first. A real eigenvalue has a real eigenvector,
 * column j of VR. For a complex pair, columns j and j+1, VR(:,j) + i*VR(:,j+1) is the eigenvector of the
 * eigenvalue with the positive imaginary part, and its conjugate that of the other. D is block diagonal: the
 * real eigenvalues, and [wr(j), wi(j); -wi(j), wr(j)] for each complex pair. Each eigenvector has Euclidean
 * norm 1, and its largest component is real.
 * <p>
 * The native implementation balances A first. The pure Java implementation reduces A to Hessenberg form
 * ({@link GEHRD}, {@link ORGHR}), and computes the Schur form ({@link HSEQR}) and the eigenvectors
 * ({@link TREVC}), without balancing. The order of the eigenvalues can differ between them.
 *
 * @see NativeLibrary
 */
public abstract class GEEV implements LAPACK {

    /**
     * The pure Java implementation. Everything it calls is pure Java as well – the {@code JVM}
     * implementations of other operations, never their dispatching {@code invoke} methods (that may call
     * native code).
     */
    static final class JVM {

        /**
         * The Euclidean norm of the n elements of x from offset, scaled to avoid overflow.
         */
        private static double norm(final int n, final double[] x, final int offset) {
            double largest = ZERO;
            for (int i = 0; i < n; i++) {
                largest = Math.max(largest, Math.abs(x[offset + i]));
            }
            if (largest == ZERO) {
                return ZERO;
            }
            double sum = ZERO;
            for (int i = 0; i < n; i++) {
                double scaled = x[offset + i] / largest;
                sum += scaled * scaled;
            }
            return largest * Math.sqrt(sum);
        }

        /**
         * Scales each eigenvector to Euclidean norm 1, and rotates each complex one so that its largest
         * component is real.
         */
        private static void normalise(final int n, final double[] wi, final int offsetWi, final double[] vr, final int offsetVr, final int ldvr) {

            for (int j = 0; j < n; j++) {

                double imaginary = wi[offsetWi + j];
                int real = offsetVr + j * ldvr;

                if (imaginary == ZERO) {

                    SCAL.JVM.invoke(n, ONE / JVM.norm(n, vr, real), vr, real, 1);

                } else if (imaginary > ZERO) {

                    int complex = real + ldvr;

                    double scale = ONE / Math.hypot(JVM.norm(n, vr, real), JVM.norm(n, vr, complex));
                    SCAL.JVM.invoke(n, scale, vr, real, 1);
                    SCAL.JVM.invoke(n, scale, vr, complex, 1);

                    int k = 0;
                    double largest = -ONE;
                    for (int i = 0; i < n; i++) {
                        double re = vr[real + i];
                        double im = vr[complex + i];
                        double modulus2 = re * re + im * im;
                        if (modulus2 > largest) {
                            largest = modulus2;
                            k = i;
                        }
                    }

                    // Multiplied by cos - i*sin: the plane rotation that makes component k real
                    double f = vr[real + k];
                    double g = vr[complex + k];
                    double cos;
                    double sin;
                    if (g == ZERO) {
                        cos = ONE;
                        sin = ZERO;
                    } else if (f == ZERO) {
                        cos = ZERO;
                        sin = Math.signum(g);
                    } else {
                        double hypot = Math.hypot(f, g);
                        cos = Math.abs(f) / hypot;
                        sin = Math.copySign(ONE, f) * g / hypot;
                    }

                    for (int i = 0; i < n; i++) {
                        double re = vr[real + i];
                        double im = vr[complex + i];
                        vr[real + i] = cos * re + sin * im;
                        vr[complex + i] = cos * im - sin * re;
                    }
                    vr[complex + k] = ZERO;
                }
            }
        }

        /**
         * The pure Java implementation of
         * {@link GEEV#invoke(boolean, int, double[], int, int, double[], int, double[], int, double[], int, int)},
         * without argument checks, for n > 0.
         */
        static int invoke(final boolean vectors, final int n, final double[] a, final int offsetA, final int lda, final double[] wr, final int offsetWr,
                final double[] wi, final int offsetWi, final double[] vr, final int offsetVr, final int ldvr) {

            double[] tau = new double[n];

            GEHRD.JVM.invoke(n, a, offsetA, lda, tau, 0);

            if (!vectors) {
                return HSEQR.JVM.invoke(false, false, n, a, offsetA, lda, wr, offsetWr, wi, offsetWi, null, 0, 1);
            }

            for (int j = 0; j < n; j++) {
                System.arraycopy(a, offsetA + j * lda, vr, offsetVr + j * ldvr, n);
            }
            ORGHR.JVM.invoke(n, vr, offsetVr, ldvr, tau, 0);

            int info = HSEQR.JVM.invoke(true, true, n, a, offsetA, lda, wr, offsetWr, wi, offsetWi, vr, offsetVr, ldvr);

            if (info == 0) {
                TREVC.JVM.invoke(n, a, offsetA, lda, wr, offsetWr, wi, offsetWi, vr, offsetVr, ldvr);
                JVM.normalise(n, wi, offsetWi, vr, offsetVr, ldvr);
            }

            return info;
        }

    }

    /**
     * Native {@code dgeev}, initialised only when {@link NativeLibrary#isAvailable()}. All arguments are
     * passed by reference, as to any Fortran routine, followed by the lengths of the character arguments. It
     * is not linked as critical: it is passed a copy of the matrix in native memory, and the JVM can reach
     * safepoints (and collect garbage) during the call.
     */
    static final class Native {

        static final MethodHandle DGEEV_OFF_HEAP = NativeLibrary.downcall("dgeev_", FunctionDescriptor.ofVoid(ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS,
                ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS, JAVA_LONG, JAVA_LONG), false);

        /**
         * Copies A to native memory, asks {@link #DGEEV_OFF_HEAP} for the optimal workspace size, calls it,
         * and copies the eigenvalues, and the eigenvectors if computed, back. A is not changed. Requires n >
         * 0.
         */
        static int invoke(final boolean vectors, final int n, final double[] a, final int offsetA, final int lda, final double[] wr, final int offsetWr,
                final double[] wi, final int offsetWi, final double[] vr, final int offsetVr, final int ldvr) {

            Objects.checkFromIndexSize(offsetWr, n, wr.length);
            Objects.checkFromIndexSize(offsetWi, n, wi.length);

            try (Arena arena = Arena.ofConfined()) {

                int copyLDA = CBLAS.leadingDimensionOfCopy(n);

                MemorySegment segmentJobVL = arena.allocateFrom(JAVA_BYTE, (byte) 'N');
                MemorySegment segmentJobVR = arena.allocateFrom(JAVA_BYTE, (byte) (vectors ? 'V' : 'N'));
                MemorySegment segmentN = arena.allocateFrom(JAVA_INT, n);
                MemorySegment segmentA = CBLAS.copyOfMatrix(arena, a, offsetA, n, n, lda, copyLDA);
                MemorySegment segmentLDA = arena.allocateFrom(JAVA_INT, copyLDA);
                MemorySegment segmentWR = arena.allocate(JAVA_DOUBLE, n);
                MemorySegment segmentWI = arena.allocate(JAVA_DOUBLE, n);
                MemorySegment segmentVL = arena.allocate(JAVA_DOUBLE);
                MemorySegment segmentOne = arena.allocateFrom(JAVA_INT, 1);
                MemorySegment segmentVR = vectors ? CBLAS.allocateMatrix(arena, n, n, copyLDA) : arena.allocate(JAVA_DOUBLE);
                MemorySegment segmentLDVR = vectors ? segmentLDA : segmentOne;
                MemorySegment segmentInfo = arena.allocate(JAVA_INT);

                MemorySegment query = arena.allocate(JAVA_DOUBLE);
                DGEEV_OFF_HEAP.invokeExact(segmentJobVL, segmentJobVR, segmentN, segmentA, segmentLDA, segmentWR, segmentWI, segmentVL, segmentOne, segmentVR,
                        segmentLDVR, query, arena.allocateFrom(JAVA_INT, -1), segmentInfo, 1L, 1L);
                int lwork = Math.max((vectors ? 4 : 3) * n, (int) query.get(JAVA_DOUBLE, 0L));

                DGEEV_OFF_HEAP.invokeExact(segmentJobVL, segmentJobVR, segmentN, segmentA, segmentLDA, segmentWR, segmentWI, segmentVL, segmentOne, segmentVR,
                        segmentLDVR, arena.allocate(JAVA_DOUBLE, lwork), arena.allocateFrom(JAVA_INT, lwork), segmentInfo, 1L, 1L);

                int info = segmentInfo.get(JAVA_INT, 0L);

                MemorySegment.copy(segmentWR, JAVA_DOUBLE, 0L, wr, offsetWr, n);
                MemorySegment.copy(segmentWI, JAVA_DOUBLE, 0L, wi, offsetWi, n);
                if (vectors) {
                    CBLAS.copyBackMatrix(segmentVR, copyLDA, vr, offsetVr, n, n, ldvr);
                }

                return info;

            } catch (RuntimeException | Error cause) {
                throw cause;
            } catch (Throwable cause) {
                throw new ProgrammingError(cause);
            }
        }

    }

    /**
     * The native implementation is used, if there is one, when n*n*n is larger than this – the same with or
     * without the eigenvectors.
     */
    private static final double NATIVE_THRESHOLD = 3_000_000;

    static final boolean NATIVE = NativeLibrary.isAvailable() && Native.DGEEV_OFF_HEAP != null;

    /**
     * {@code dgeev} with JOBVL = 'N': the eigenvalues, and the right eigenvectors if specified
     * <p>
     * A, and VR, are column-major, stored in their arrays from an offset with a leading dimension. wr(i) and
     * wi(i), i = 1, ..., n, are stored in {@code wr[offsetWr + i - 1]} and {@code wi[offsetWi + i - 1]}. A is
     * overwritten (its contents on exit are unspecified).
     *
     * @param vectors The eigenvectors are computed, to VR, if true (JOBVR = 'V'), otherwise VR is not
     *                referenced (JOBVR = 'N')
     * @param n       The order of A
     * @return 0, or i > 0 if the QR method failed to converge: no eigenvectors have been computed, and the
     *         eigenvalues i+1 to n are those that were found
     */
    public static int invoke(final boolean vectors, final int n, final double[] a, final int offsetA, final int lda, final double[] wr, final int offsetWr,
            final double[] wi, final int offsetWi, final double[] vr, final int offsetVr, final int ldvr) {

        ArrayOperation.checkArgument("GEEV", "n", n >= 0);
        ArrayOperation.checkArgument("GEEV", "lda", lda >= Math.max(1, n));
        ArrayOperation.checkArgument("GEEV", "ldvr", ldvr >= (vectors ? Math.max(1, n) : 1));

        if (n == 0) {
            return 0;
        }

        if (GEEV.isNative(n)) {
            return Native.invoke(vectors, n, a, offsetA, lda, wr, offsetWr, wi, offsetWi, vr, offsetVr, ldvr);
        } else {
            return JVM.invoke(vectors, n, a, offsetA, lda, wr, offsetWr, wi, offsetWi, vr, offsetVr, ldvr);
        }
    }

    /**
     * Whether to use the native implementation: there is one, and n*n*n is larger than
     * {@link #NATIVE_THRESHOLD}.
     */
    static boolean isNative(final double n) {
        return NATIVE && n * n * n > NATIVE_THRESHOLD;
    }

}

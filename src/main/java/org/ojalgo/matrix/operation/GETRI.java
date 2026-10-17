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

import static org.ojalgo.function.constant.PrimitiveMath.ONE;
import static org.ojalgo.function.constant.PrimitiveMath.ZERO;

import java.util.Arrays;

import org.ojalgo.array.operation.ArrayOperation;
import org.ojalgo.concurrent.DivideAndConquer;

/**
 * LAPACK {@code ?getri}: the inverse of a general n-by-n matrix A, using the LU factorisation A = P*L*U
 * computed by {@link GETRF}. The inverse overwrites the factors.
 */
public abstract class GETRI implements LAPACK {

    /**
     * The pure Java implementation. Everything it calls is pure Java as well – the {@code JVM}
     * implementations of other operations, never their dispatching {@code invoke} methods (that may call
     * native code).
     * <p>
     * Solves A*X = I, column by column, with a copy of the factors. Each column of the identity, with the row
     * interchanges applied, has a single non-zero element; the system with L is solved from that row on, and
     * the system with U in full, both by {@link TRSV}. The columns are solved in parallel when there are more
     * than {@link GETRI#THRESHOLD} of them.
     */
    static final class JVM {

        private static void solve(final int n, final double[] factors, final int[] position, final double[] a, final int offsetA, final int lda,
                final int first, final int limit) {
            for (int j = first; j < limit; j++) {
                int column = offsetA + j * lda;
                int row = position[j];
                Arrays.fill(a, column, column + n, ZERO);
                a[column + row] = ONE;
                TRSV.JVM.invoke(false, false, true, n - row, factors, row + row * n, n, a, column + row, 1);
                TRSV.JVM.invoke(true, false, false, n, factors, 0, n, a, column, 1);
            }
        }

        /**
         * The pure Java implementation of {@link GETRI#invoke(int, double[], int, int, int[], int)}, without
         * argument checks, for n > 0.
         */
        static int invoke(final int n, final double[] a, final int offsetA, final int lda, final int[] ipiv, final int offsetIpiv) {

            int singular = GETRI.indexOfZeroOnDiagonal(n, a, offsetA, lda);
            if (singular != 0) {
                return singular;
            }

            double[] factors = new double[n * n];
            for (int j = 0; j < n; j++) {
                System.arraycopy(a, offsetA + j * lda, factors, j * n, n);
            }

            // The rows of the identity in the order the interchanges put them, and then, inverted, the row
            // each column's non-zero element ends up in
            int[] order = new int[n];
            for (int i = 0; i < n; i++) {
                order[i] = i;
            }
            for (int i = 0; i < n; i++) {
                int row = ipiv[offsetIpiv + i] - 1;
                int tmp = order[i];
                order[i] = order[row];
                order[row] = tmp;
            }
            int[] position = new int[n];
            for (int i = 0; i < n; i++) {
                position[order[i]] = i;
            }

            if (n > THRESHOLD) {

                DivideAndConquer conquerer = new DivideAndConquer() {

                    @Override
                    protected void conquer(final int first, final int limit) {
                        JVM.solve(n, factors, position, a, offsetA, lda, first, limit);
                    }
                };

                conquerer.invoke(0, n, THRESHOLD);

            } else {

                JVM.solve(n, factors, position, a, offsetA, lda, 0, n);
            }

            return 0;
        }

    }

    /**
     * The native implementation: solves A*X = I with the native {@link GETRS}, and copies X over the factors.
     */
    static final class Native {

        /**
         * Requires n > 0.
         */
        static int invoke(final int n, final double[] a, final int offsetA, final int lda, final int[] ipiv, final int offsetIpiv) {

            int singular = GETRI.indexOfZeroOnDiagonal(n, a, offsetA, lda);
            if (singular != 0) {
                return singular;
            }

            double[] inverse = new double[n * n];
            for (int i = 0; i < n; i++) {
                inverse[i + i * n] = ONE;
            }

            if (GETRS.isOffHeap(n, n)) {
                GETRS.Native.invokeOffHeap(false, n, n, a, offsetA, lda, ipiv, offsetIpiv, inverse, 0, n);
            } else {
                GETRS.Native.invoke(false, n, n, a, offsetA, lda, ipiv, offsetIpiv, inverse, 0, n);
            }

            for (int j = 0; j < n; j++) {
                System.arraycopy(inverse, j * n, a, offsetA + j * lda, n);
            }

            return 0;
        }

    }

    /**
     * The native implementation is used, if there is one, when n*n*n is larger than this.
     */
    private static final double NATIVE_THRESHOLD = 500;

    static final boolean NATIVE = GETRS.NATIVE;

    /**
     * The Java implementation solves the columns of the inverse in parallel when there are more than this
     * many of them.
     */
    static int THRESHOLD = 64;

    /**
     * {@code dgetri}: A := A<sup>-1</sup>, from its factors
     * <p>
     * A is column-major, stored in its array from an offset with a leading dimension. On entry, A holds the
     * factors L and U, and ipiv the row interchanges, as returned by {@link GETRF}. On exit, A holds the
     * inverse of the original matrix – unless U is singular.
     *
     * @param n The order of A
     * @return 0, or i > 0 if U(i,i) is exactly zero (1-based). The matrix is then singular, its inverse is
     *         not computed, and A is left unchanged.
     */
    public static int invoke(final int n, final double[] a, final int offsetA, final int lda, final int[] ipiv, final int offsetIpiv) {

        ArrayOperation.checkArgument("GETRI", "n", n >= 0);
        ArrayOperation.checkArgument("GETRI", "lda", lda >= Math.max(1, n));

        if (n == 0) {
            return 0;
        }

        if (GETRI.isNative(n)) {
            return Native.invoke(n, a, offsetA, lda, ipiv, offsetIpiv);
        } else {
            return JVM.invoke(n, a, offsetA, lda, ipiv, offsetIpiv);
        }
    }

    /**
     * @return The (1-based) index of the first exactly zero element on the diagonal, or 0 if there is none
     */
    private static int indexOfZeroOnDiagonal(final int n, final double[] a, final int offsetA, final int lda) {
        for (int i = 0; i < n; i++) {
            if (a[offsetA + i + i * lda] == ZERO) {
                return i + 1;
            }
        }
        return 0;
    }

    /**
     * Whether to use the native implementation: there is one, and n*n*n is larger than
     * {@link #NATIVE_THRESHOLD}.
     */
    static boolean isNative(final double n) {
        return NATIVE && n * n * n > NATIVE_THRESHOLD;
    }

}

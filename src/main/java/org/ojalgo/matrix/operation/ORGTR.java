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

import org.ojalgo.array.operation.ArrayOperation;
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
 */
public abstract class ORGTR implements LAPACK {

    /**
     * The pure Java implementations.
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

        JVM.invoke(n, a, offsetA, lda, tau, offsetTau);
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

}

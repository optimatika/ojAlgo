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

import org.ojalgo.array.operation.ArrayOperation;
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
 */
public abstract class ORGHR implements LAPACK {

    /**
     * The pure Java implementations.
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

        JVM.invoke(n, a, offsetA, lda, tau, offsetTau);
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

}

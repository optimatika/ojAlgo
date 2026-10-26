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
 * LAPACK {@code ?gehrd}, with ilo = 1 and ihi = n: reduces a general n-by-n matrix A to upper Hessenberg form
 * H by an orthogonal similarity transformation:
 *
 * <pre>{@code
 * Q^T * A * Q = H
 * }</pre>
 *
 * On exit, the upper triangle and the first subdiagonal of A hold H, and the elements below the first
 * subdiagonal hold the reflectors that make Q:
 *
 * <pre>{@code
 * Q = H(1) * H(2) * ... * H(n-1)
 * H(i) = I - tau(i) * v * v^T
 * }</pre>
 *
 * v(1:i) = 0 and v(i+1) = 1 (not stored), and v(i+2:n) is stored in A(i+2:n,i). With tau(i) = 0, H(i) is the
 * identity, and tau(n-1) = 0. Reducing only the rows and columns ilo to ihi of a balanced matrix, the general
 * case of LAPACK's {@code ?gehrd}, is not implemented.
 * <p>
 * For other number types Q is unitary and H(i) = I - tau(i) * v * v<sup>H</sup>, with tau(i) real. There is
 * only a pure Java implementation for those.
 * <p>
 * {@link ORGHR} forms Q explicitly.
 */
public abstract class GEHRD implements LAPACK {

    /**
     * The pure Java implementations.
     * <p>
     * Unblocked: for each column, the reflector that zeroes the elements below the first subdiagonal is
     * generated, as by {@link GEQRF}, and applied from the right to all rows, as by {@link GEBRD}, and from
     * the left to the columns to the right, as by {@link GEQRF}.
     */
    static final class JVM {

        /**
         * The pure Java implementation of {@link GEHRD#invoke(int, double[], int, int, double[], int)},
         * without argument checks, for n > 0.
         */
        static void invoke(final int n, final double[] a, final int offsetA, final int lda, final double[] tau, final int offsetTau) {

            double[] v = new double[n];
            double[] w = new double[n];

            for (int i = 0; i < n - 1; i++) {

                int column = offsetA + i * lda;

                double beta = i + 2 < n ? GEQRF.JVM.generate(n, a, column, i + 1) : ZERO;

                tau[offsetTau + i] = beta;

                if (beta != ZERO) {
                    // v(i+1) = 1, and v(i+2) to v(n-1) from below the subdiagonal
                    v[0] = ONE;
                    System.arraycopy(a, column + i + 2, v, 1, n - i - 2);
                    GEBRD.JVM.reflectRight(n, n, a, offsetA, lda, 0, i + 1, v, beta, w);
                    GEQRF.JVM.reflect(n, v, 0, beta, a, offsetA, lda, i + 1, i + 1, n);
                }
            }
        }

        /**
         * The pure Java implementation of
         * {@link GEHRD#invoke(int, Scalar[], int, int, Scalar[], int, Scalar.Factory)}, without argument
         * checks, for n > 0.
         */
        static <N extends Scalar<N>> void invoke(final int n, final N[] a, final int offsetA, final int lda, final N[] tau, final int offsetTau,
                final Scalar.Factory<N> scalar) {

            N zero = scalar.zero().get();

            N[] v = scalar.newArrayInstance(n);
            N[] w = scalar.newArrayInstance(n);

            for (int i = 0; i < n - 1; i++) {

                int column = offsetA + i * lda;

                N beta = i + 2 < n ? GEQRF.JVM.generate(n, a, column, i + 1, scalar) : null;

                tau[offsetTau + i] = beta != null ? beta : zero;

                if (beta != null) {
                    v[0] = scalar.one().get();
                    System.arraycopy(a, column + i + 2, v, 1, n - i - 2);
                    GEBRD.JVM.reflectRight(n, n, a, offsetA, lda, 0, i + 1, v, beta, w, scalar);
                    GEQRF.JVM.reflect(n, v, 0, beta, a, offsetA, lda, i + 1, i + 1, n, scalar);
                }
            }
        }

    }

    /**
     * {@code dgehrd} with ILO = 1 and IHI = n: Q<sup>T</sup>*A*Q = H, H and the reflectors that make Q
     * overwrite A
     * <p>
     * A is column-major, stored in its array from an offset with a leading dimension. tau(i), i = 1, ...,
     * n-1, is stored in {@code tau[offsetTau + i - 1]}.
     *
     * @param n The order of A
     */
    public static void invoke(final int n, final double[] a, final int offsetA, final int lda, final double[] tau, final int offsetTau) {

        ArrayOperation.checkArgument("GEHRD", "n", n >= 0);
        ArrayOperation.checkArgument("GEHRD", "lda", lda >= Math.max(1, n));

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

        ArrayOperation.checkArgument("GEHRD", "n", n >= 0);
        ArrayOperation.checkArgument("GEHRD", "lda", lda >= Math.max(1, n));

        if (n == 0) {
            return;
        }

        JVM.invoke(n, a, offsetA, lda, tau, offsetTau, scalar);
    }

}

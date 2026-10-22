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

import static org.ojalgo.function.constant.PrimitiveMath.ZERO;

import org.ojalgo.array.operation.AXPY;
import org.ojalgo.array.operation.ArrayOperation;

/**
 * BLAS level 2 {@code ?ger}: A := alpha*x*y<sup>T</sup> + A, a rank-1 update of the m-by-n matrix A.
 */
public abstract class GER implements BLAS2 {

    /**
     * The pure Java implementation.
     */
    static final class JVM {

        /**
         * The pure Java implementation of {@link GER#invoke}, without argument checks, for alpha != 0. One
         * AXPY per column of A, with FMA.
         */
        static void invoke(final int m, final int n, final double alpha, final double[] x, final int offsetX, final int incX, final double[] y,
                final int offsetY, final int incY, final double[] a, final int offsetA, final int lda) {

            int jy = incY < 0 ? offsetY - (n - 1) * incY : offsetY;

            if (incX != 1) {
                for (int j = 0; j < n; j++, jy += incY) {
                    AXPY.JVM.invoke(m, alpha * y[jy], x, offsetX, incX, a, offsetA + j * lda, 1);
                }
                return;
            }

            for (int j = 0; j < n; j++, jy += incY) {
                double temp = alpha * y[jy];
                int colA = offsetA + j * lda;
                for (int i = 0; i < m; i++) {
                    a[colA + i] = GEMM.JVM.fma(temp, x[offsetX + i], a[colA + i]);
                }
            }
        }

    }

    /**
     * {@code dger}: A := alpha*x*y<sup>T</sup> + A
     * <p>
     * A is column-major, stored in its array from an offset with a leading dimension. The vectors are stored
     * from an offset with an increment; as in BLAS, a negative increment walks the vector backwards, and the
     * offset is still the lowest index accessed.
     *
     * @param m The number of rows of A, and elements of x
     * @param n The number of columns of A, and elements of y
     */
    public static void invoke(final int m, final int n, final double alpha, final double[] x, final int offsetX, final int incX, final double[] y,
            final int offsetY, final int incY, final double[] a, final int offsetA, final int lda) {

        ArrayOperation.checkArgument("GER", "m", m >= 0);
        ArrayOperation.checkArgument("GER", "n", n >= 0);
        ArrayOperation.checkArgument("GER", "incX", incX != 0);
        ArrayOperation.checkArgument("GER", "incY", incY != 0);
        ArrayOperation.checkArgument("GER", "lda", lda >= Math.max(1, m));

        if (m == 0 || n == 0 || alpha == ZERO) {
            return;
        }

        JVM.invoke(m, n, alpha, x, offsetX, incX, y, offsetY, incY, a, offsetA, lda);
    }

}

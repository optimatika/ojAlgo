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

import org.ojalgo.array.operation.AXPY;
import org.ojalgo.array.operation.ArrayOperation;
import org.ojalgo.array.operation.DOT;
import org.ojalgo.array.operation.SCAL;

/**
 * BLAS level 3 {@code ?syrk}: C := alpha*A*A<sup>T</sup> + beta*C or C := alpha*A<sup>T</sup>*A + beta*C,
 * where C is symmetric n-by-n and only its upper or lower triangle is updated.
 */
public abstract class SYRK implements BLAS3 {

    /**
     * The pure Java implementation.
     */
    static final class JVM {

        /**
         * The pure Java implementation of {@link SYRK#invoke}, without argument checks.
         */
        static void invoke(final boolean upper, final boolean transpose, final int n, final int k, final double alpha, final double[] a, final int offsetA,
                final int lda, final double beta, final double[] c, final int offsetC, final int ldc) {

            if (alpha == ZERO || k == 0) {
                for (int j = 0; j < n; j++) {
                    int colC = offsetC + j * ldc;
                    if (upper) {
                        SCAL.scale(c, colC, colC + j + 1, beta);
                    } else {
                        SCAL.scale(c, colC + j, colC + n, beta);
                    }
                }
                return;
            }

            if (!transpose) {
                for (int j = 0; j < n; j++) {
                    int colC = offsetC + j * ldc;
                    int first = upper ? 0 : j;
                    int limit = upper ? j + 1 : n;
                    SCAL.scale(c, colC + first, colC + limit, beta);
                    for (int l = 0; l < k; l++) {
                        int colA = offsetA + l * lda;
                        AXPY.JVM.invoke(limit - first, alpha * a[colA + j], a, colA + first, 1, c, colC + first, 1);
                    }
                }
            } else {
                for (int j = 0; j < n; j++) {
                    int colAj = offsetA + j * lda;
                    int colC = offsetC + j * ldc;
                    int first = upper ? 0 : j;
                    int limit = upper ? j + 1 : n;
                    for (int i = first; i < limit; i++) {
                        double temp = alpha * DOT.JVM.invoke(k, a, offsetA + i * lda, 1, a, colAj, 1);
                        c[colC + i] = beta == ZERO ? temp : temp + beta * c[colC + i];
                    }
                }
            }
        }

    }

    /**
     * {@code dsyrk}: C := alpha*A*A<sup>T</sup> + beta*C (no transpose) or C := alpha*A<sup>T</sup>*A +
     * beta*C (transpose)
     * <p>
     * All matrices are column-major, stored in their arrays from an offset with a leading dimension. Only the
     * upper or lower triangle of C is read and updated. When beta is 0, C need not be set on entry; when
     * alpha is 0, A is not read.
     *
     * @param upper     The upper triangle of C is updated if true, otherwise the lower
     * @param transpose A is k-by-n if true, otherwise n-by-k
     * @param n         The order of C
     * @param k         The number of columns of A (rows if transposed)
     */
    public static void invoke(final boolean upper, final boolean transpose, final int n, final int k, final double alpha, final double[] a, final int offsetA,
            final int lda, final double beta, final double[] c, final int offsetC, final int ldc) {

        ArrayOperation.checkArgument("SYRK", "n", n >= 0);
        ArrayOperation.checkArgument("SYRK", "k", k >= 0);
        ArrayOperation.checkArgument("SYRK", "lda", lda >= Math.max(1, transpose ? k : n));
        ArrayOperation.checkArgument("SYRK", "ldc", ldc >= Math.max(1, n));

        if (n == 0 || (alpha == ZERO || k == 0) && beta == ONE) {
            return;
        }

        JVM.invoke(upper, transpose, n, k, alpha, a, offsetA, lda, beta, c, offsetC, ldc);
    }

}

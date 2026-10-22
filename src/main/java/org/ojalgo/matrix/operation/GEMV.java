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
import org.ojalgo.concurrent.DivideAndConquer;
import org.ojalgo.concurrent.Parallelism;

/**
 * BLAS level 2 {@code ?gemv}: y := alpha*op(A)*x + beta*y, where A is m-by-n and op(A) is A or A<sup>T</sup>.
 */
public abstract class GEMV implements BLAS2 {

    /**
     * The pure Java implementation.
     */
    static final class JVM {

        private static final DivideAndConquer.Divider DIVIDER = Parallelism.CORES.newDivider();

        /**
         * y(index) := alpha*product + beta*y(index), where beta 0 means y(index) is not read.
         */
        private static void assign(final double[] y, final int index, final double alpha, final double product, final double beta) {
            y[index] = beta == ZERO ? alpha * product : alpha * product + beta * y[index];
        }

        /**
         * The number of elements of y for one part when the Java implementation is multi-threaded: at least
         * {@link GEMV#THRESHOLD}, and as many as it takes to cover {@link GEMV#THRESHOLD} squared elements of
         * A, given the other dimension of A. The operation is limited by memory bandwidth, so many small
         * parts don't pay.
         */
        private static int chunk(final int other) {
            double cover = Math.ceil((double) THRESHOLD * THRESHOLD / other);
            return (int) Math.min(Integer.MAX_VALUE, Math.max(THRESHOLD, cover));
        }

        /**
         * y := alpha*A*x + y, as a column sweep: 4 columns of A in each pass over y, with FMA.
         */
        private static void invokeN(final int m, final int n, final double alpha, final double[] a, final int offsetA, final int lda, final double[] x,
                final int offsetX, final int incX, final double[] y, final int offsetY, final int incY) {

            int jx = incX < 0 ? offsetX - (n - 1) * incX : offsetX;

            if (incY != 1) {
                for (int j = 0; j < n; j++, jx += incX) {
                    AXPY.JVM.invoke(m, alpha * x[jx], a, offsetA + j * lda, 1, y, offsetY, incY);
                }
                return;
            }

            int n4 = n - n % 4;

            int j = 0;
            for (; j < n4; j += 4, jx += 4 * incX) {

                double x0 = alpha * x[jx];
                double x1 = alpha * x[jx + incX];
                double x2 = alpha * x[jx + 2 * incX];
                double x3 = alpha * x[jx + 3 * incX];

                int colA0 = offsetA + j * lda;
                int colA1 = colA0 + lda;
                int colA2 = colA1 + lda;
                int colA3 = colA2 + lda;

                for (int i = 0; i < m; i++) {
                    y[offsetY + i] = GEMM.JVM.fma(x3, a[colA3 + i],
                            GEMM.JVM.fma(x2, a[colA2 + i], GEMM.JVM.fma(x1, a[colA1 + i], GEMM.JVM.fma(x0, a[colA0 + i], y[offsetY + i]))));
                }
            }

            for (; j < n; j++, jx += incX) {
                double x0 = alpha * x[jx];
                int colA = offsetA + j * lda;
                for (int i = 0; i < m; i++) {
                    y[offsetY + i] = GEMM.JVM.fma(x0, a[colA + i], y[offsetY + i]);
                }
            }
        }

        /**
         * y := alpha*A<sup>T</sup>*x + beta*y, for alpha != 0, as dot products of the columns of A with x.
         * Each element of y is assigned once, so beta is applied then (beta 0 means y is not read). Short
         * columns (m < 256) are done 4 at a time, sharing each load of x, with one accumulator each. Longer
         * columns get one {@link DOT} each: then reading 4 columns at once costs more in cache than sharing x
         * saves.
         */
        private static void invokeT(final int m, final int n, final double alpha, final double[] a, final int offsetA, final int lda, final double[] x,
                final int offsetX, final int incX, final double beta, final double[] y, final int offsetY, final int incY) {

            int jy = incY < 0 ? offsetY - (n - 1) * incY : offsetY;

            if (incX != 1) {
                for (int j = 0; j < n; j++, jy += incY) {
                    JVM.assign(y, jy, alpha, DOT.JVM.invoke(m, a, offsetA + j * lda, 1, x, offsetX, incX), beta);
                }
                return;
            }

            int n4 = m < 256 ? n - n % 4 : 0;

            int j = 0;
            for (; j < n4; j += 4, jy += 4 * incY) {

                int colA0 = offsetA + j * lda;
                int colA1 = colA0 + lda;
                int colA2 = colA1 + lda;
                int colA3 = colA2 + lda;

                double sum0 = ZERO, sum1 = ZERO, sum2 = ZERO, sum3 = ZERO;
                for (int i = 0; i < m; i++) {
                    double xi = x[offsetX + i];
                    sum0 = GEMM.JVM.fma(a[colA0 + i], xi, sum0);
                    sum1 = GEMM.JVM.fma(a[colA1 + i], xi, sum1);
                    sum2 = GEMM.JVM.fma(a[colA2 + i], xi, sum2);
                    sum3 = GEMM.JVM.fma(a[colA3 + i], xi, sum3);
                }

                JVM.assign(y, jy, alpha, sum0, beta);
                JVM.assign(y, jy + incY, alpha, sum1, beta);
                JVM.assign(y, jy + 2 * incY, alpha, sum2, beta);
                JVM.assign(y, jy + 3 * incY, alpha, sum3, beta);
            }

            for (; j < n; j++, jy += incY) {
                JVM.assign(y, jy, alpha, DOT.JVM.invoke(m, a, offsetA + j * lda, 1, x, offsetX, 1), beta);
            }
        }

        /**
         * The pure Java implementation of {@link GEMV#invoke}, without argument checks. Multi-threaded,
         * splitting the elements of y, when y and A are large, see {@link GEMV#THRESHOLD}.
         */
        static void invoke(final boolean transpose, final int m, final int n, final double alpha, final double[] a, final int offsetA, final int lda,
                final double[] x, final int offsetX, final int incX, final double beta, final double[] y, final int offsetY, final int incY) {

            int length = transpose ? n : m;
            int chunk = JVM.chunk(transpose ? m : n);

            if (alpha == ZERO) {
                SCAL.scale(length, beta, y, offsetY, incY);
            } else if (incY == 1 && length > chunk) {
                if (transpose) {
                    DIVIDER.threshold(chunk).divide(0, n, (first, limit) -> JVM.invokeT(m, limit - first, alpha, a, offsetA + first * lda, lda, x, offsetX,
                            incX, beta, y, offsetY + first, 1));
                } else {
                    DIVIDER.threshold(chunk).divide(0, m, (first, limit) -> {
                        SCAL.scale(limit - first, beta, y, offsetY + first, 1);
                        JVM.invokeN(limit - first, n, alpha, a, offsetA + first, lda, x, offsetX, incX, y, offsetY + first, 1);
                    });
                }
            } else if (transpose) {
                JVM.invokeT(m, n, alpha, a, offsetA, lda, x, offsetX, incX, beta, y, offsetY, incY);
            } else {
                SCAL.scale(m, beta, y, offsetY, incY);
                JVM.invokeN(m, n, alpha, a, offsetA, lda, x, offsetX, incX, y, offsetY, incY);
            }
        }

    }

    /**
     * The Java implementation is multi-threaded, splitting the elements of y, when y has increment 1 and more
     * elements than max(this, this squared divided by the other dimension of A). The divider splits in
     * halves, so each part gets more than half of that.
     */
    static int THRESHOLD = 320;

    /**
     * {@code dgemv}: y := alpha*op(A)*x + beta*y
     * <p>
     * A is column-major, stored in its array from an offset with a leading dimension. The vectors are stored
     * from an offset with an increment; as in BLAS, a negative increment walks the vector backwards, and the
     * offset is still the lowest index accessed. When beta is 0, y need not be set on entry.
     *
     * @param transpose op(A) = A<sup>T</sup> if true, otherwise A
     * @param m         The number of rows of A
     * @param n         The number of columns of A
     */
    public static void invoke(final boolean transpose, final int m, final int n, final double alpha, final double[] a, final int offsetA, final int lda,
            final double[] x, final int offsetX, final int incX, final double beta, final double[] y, final int offsetY, final int incY) {

        ArrayOperation.checkArgument("GEMV", "m", m >= 0);
        ArrayOperation.checkArgument("GEMV", "n", n >= 0);
        ArrayOperation.checkArgument("GEMV", "lda", lda >= Math.max(1, m));
        ArrayOperation.checkArgument("GEMV", "incX", incX != 0);
        ArrayOperation.checkArgument("GEMV", "incY", incY != 0);

        if (m == 0 || n == 0 || alpha == ZERO && beta == ONE) {
            return;
        }

        JVM.invoke(transpose, m, n, alpha, a, offsetA, lda, x, offsetX, incX, beta, y, offsetY, incY);
    }

}

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

/**
 * LAPACK {@code ?potri}: the inverse of a symmetric positive definite n-by-n matrix A, from its Cholesky
 * factorisation A = L*L<sup>T</sup> or A = U<sup>T</sup>*U computed by {@link POTRF}:
 *
 * <pre>{@code
 * A^-1 = L^-T * L^-1  or  A^-1 = U^-1 * U^-T
 * }</pre>
 *
 * The inverse overwrites the factor, in the same triangle. The other triangle is not referenced. There is
 * only a pure Java implementation.
 */
public abstract class POTRI implements LAPACK {

    /**
     * The pure Java implementation.
     * <p>
     * Unblocked and single-threaded, as {@code ?trti2} followed by {@code ?lauu2}: the factor is inverted in
     * place, a column at a time, and then multiplied by its own transpose, also in place. Only the dot
     * products along the rows of U, for the diagonal of U*U<sup>T</sup>, are strided; every other inner loop
     * runs down a column. The loops are written out rather than calling
     * {@link org.ojalgo.array.operation.AXPY} and {@link org.ojalgo.array.operation.DOT}: the vectors are
     * short, a third of n on average, and the calls cost more than they save (1.3 times for n = 16).
     */
    static final class JVM {

        /**
         * L := L<sup>-1</sup>, from the last column to the first: each column, below the diagonal, is
         * multiplied by the trailing triangle, already inverted, and by minus the inverted diagonal element.
         */
        private static void invertLower(final int n, final double[] a, final int offsetA, final int lda) {
            for (int j = n - 1; j >= 0; j--) {
                int column = offsetA + j * lda;
                double diagonal = ONE / a[column + j];
                a[column + j] = diagonal;
                for (int k = n - 1; k > j; k--) {
                    double element = a[column + k];
                    if (element != ZERO) {
                        int columnK = offsetA + k * lda;
                        for (int i = k + 1; i < n; i++) {
                            a[column + i] += element * a[columnK + i];
                        }
                        a[column + k] = element * a[columnK + k];
                    }
                }
                for (int i = j + 1; i < n; i++) {
                    a[column + i] *= -diagonal;
                }
            }
        }

        /**
         * U := U<sup>-1</sup>, from the first column to the last: each column, above the diagonal, is
         * multiplied by the leading triangle, already inverted, and by minus the inverted diagonal element.
         */
        private static void invertUpper(final int n, final double[] a, final int offsetA, final int lda) {
            for (int j = 0; j < n; j++) {
                int column = offsetA + j * lda;
                double diagonal = ONE / a[column + j];
                a[column + j] = diagonal;
                for (int k = 0; k < j; k++) {
                    double element = a[column + k];
                    if (element != ZERO) {
                        int columnK = offsetA + k * lda;
                        for (int i = 0; i < k; i++) {
                            a[column + i] += element * a[columnK + i];
                        }
                        a[column + k] = element * a[columnK + k];
                    }
                }
                for (int i = 0; i < j; i++) {
                    a[column + i] *= -diagonal;
                }
            }
        }

        /**
         * L := L<sup>T</sup>*L, in the lower triangle: each element is the dot product of two columns, from
         * its row on. Column by column, and down each column, so that nothing is overwritten before it has
         * been used.
         */
        private static void multiplyLower(final int n, final double[] a, final int offsetA, final int lda) {
            for (int j = 0; j < n; j++) {
                int columnJ = offsetA + j * lda;
                for (int i = j; i < n; i++) {
                    int columnI = offsetA + i * lda;
                    double sum = ZERO;
                    for (int k = i; k < n; k++) {
                        sum += a[columnI + k] * a[columnJ + k];
                    }
                    a[columnJ + i] = sum;
                }
            }
        }

        /**
         * U := U*U<sup>T</sup>, in the upper triangle: column by column, from the first, the column above the
         * diagonal is itself times its diagonal element plus the columns to its right, each times its element
         * on the diagonal's row. The diagonal element is the dot product of its row with itself.
         */
        private static void multiplyUpper(final int n, final double[] a, final int offsetA, final int lda) {
            for (int i = 0; i < n; i++) {
                int columnI = offsetA + i * lda;
                double diagonal = a[columnI + i];
                double sum = ZERO;
                for (int k = i, index = columnI + i; k < n; k++, index += lda) {
                    sum += a[index] * a[index];
                }
                for (int r = 0; r < i; r++) {
                    a[columnI + r] *= diagonal;
                }
                for (int k = i + 1; k < n; k++) {
                    int columnK = offsetA + k * lda;
                    double element = a[columnK + i];
                    for (int r = 0; r < i; r++) {
                        a[columnI + r] += element * a[columnK + r];
                    }
                }
                a[columnI + i] = sum;
            }
        }

        /**
         * The pure Java implementation of {@link POTRI#invoke(boolean, int, double[], int, int)}, without
         * argument checks, for n > 0.
         */
        static int invoke(final boolean upper, final int n, final double[] a, final int offsetA, final int lda) {

            for (int j = 0; j < n; j++) {
                if (a[offsetA + j + j * lda] == ZERO) {
                    return j + 1;
                }
            }

            if (upper) {
                JVM.invertUpper(n, a, offsetA, lda);
                JVM.multiplyUpper(n, a, offsetA, lda);
            } else {
                JVM.invertLower(n, a, offsetA, lda);
                JVM.multiplyLower(n, a, offsetA, lda);
            }

            return 0;
        }

    }

    /**
     * {@code dpotri}: A<sup>-1</sup> overwrites the triangle of A that holds the factor
     * <p>
     * A is column-major, stored in its array from an offset with a leading dimension, and holds the factor, L
     * or U, as returned by {@link POTRF}.
     *
     * @param upper A = U<sup>T</sup>*U, and only the upper triangle is referenced, if true; otherwise A =
     *              L*L<sup>T</sup> and the lower triangle
     * @param n     The order of A
     * @return 0, or k > 0 if the k-th diagonal element of the factor is zero, and A is singular. A is then
     *         not changed.
     */
    public static int invoke(final boolean upper, final int n, final double[] a, final int offsetA, final int lda) {

        ArrayOperation.checkArgument("POTRI", "n", n >= 0);
        ArrayOperation.checkArgument("POTRI", "lda", lda >= Math.max(1, n));

        if (n == 0) {
            return 0;
        }

        return JVM.invoke(upper, n, a, offsetA, lda);
    }

}

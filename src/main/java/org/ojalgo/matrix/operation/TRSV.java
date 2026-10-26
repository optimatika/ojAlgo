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
import org.ojalgo.scalar.Scalar;

/**
 * BLAS level 2 {@code ?trsv}: solves op(A)*x = b for x, where A is n-by-n triangular and op(A) is A or
 * A<sup>T</sup>. x overwrites b.
 * <p>
 * The pure Java kernels for contiguous x are the ones ojAlgo's single right-hand-side substitutions (ftran
 * and btran of LU and Cholesky) were tuned with:
 * <ul>
 * <li>U<sup>T</sup>*x = b (forwards) sums each row with a single accumulator. A row uses the element solved
 * just before only in its last term, so consecutive rows overlap.
 * <li>L*x = b (forwards) does the same up to order {@link #COLUMN_SWEEP_THRESHOLD}, and above that it is a
 * column sweep, as U*x = b. A row of the column-major L is strided: those loads can't be vectorised, and with
 * a leading dimension that is a power of 2 they all map to the same few cache sets.
 * <li>U*x = b (backwards) is a column sweep, skipping zero elements.
 * <li>L<sup>T</sup>*x = b (backwards) sums each row from the far end, so the element solved just before is
 * used in the last term. Summed the other way, every row has to wait for the previous one before it can
 * start, and the solve becomes latency-bound (more than twice as slow).
 * </ul>
 */
public abstract class TRSV implements BLAS2 {

    /**
     * The pure Java implementation.
     */
    static final class JVM {

        /**
         * L*x = b, forwards, each row summed with a single accumulator – or, when n is larger than
         * {@link TRSV#COLUMN_SWEEP_THRESHOLD}, as a column sweep that skips zero elements of x.
         */
        private static void solveL(final boolean unitDiagonal, final int n, final double[] a, final int offsetA, final int lda, final double[] x,
                final int offsetX) {
            if (n > COLUMN_SWEEP_THRESHOLD) {
                for (int j = 0; j < n; j++) {
                    if (!unitDiagonal) {
                        x[offsetX + j] /= a[offsetA + j + j * lda];
                    }
                    double solved = x[offsetX + j];
                    if (solved != ZERO) {
                        AXPY.JVM.invoke(n - 1 - j, -solved, a, offsetA + j + 1 + j * lda, 1, x, offsetX + j + 1, 1);
                    }
                }
            } else {
                for (int j = 0; j < n; j++) {
                    double sum = 0D;
                    for (int i = 0, ia = offsetA + j, ix = offsetX; i < j; i++, ia += lda, ix++) {
                        sum += a[ia] * x[ix];
                    }
                    double solved = x[offsetX + j] - sum;
                    if (!unitDiagonal) {
                        solved /= a[offsetA + j + j * lda];
                    }
                    x[offsetX + j] = solved;
                }
            }
        }

        /**
         * L*x = b, forwards, each row summed with a single accumulator – or, when n is larger than
         * {@link TRSV#COLUMN_SWEEP_THRESHOLD}, as a column sweep that skips zero elements of x.
         */
        private static void solveL(final boolean unitDiagonal, final int n, final float[] a, final int offsetA, final int lda, final float[] x,
                final int offsetX) {
            if (n > COLUMN_SWEEP_THRESHOLD) {
                for (int j = 0; j < n; j++) {
                    if (!unitDiagonal) {
                        x[offsetX + j] /= a[offsetA + j + j * lda];
                    }
                    float solved = x[offsetX + j];
                    if (solved != 0F) {
                        AXPY.invoke(x, offsetX, -solved, a, offsetA + j * lda, j + 1, n);
                    }
                }
            } else {
                for (int j = 0; j < n; j++) {
                    float sum = 0F;
                    for (int i = 0, ia = offsetA + j, ix = offsetX; i < j; i++, ia += lda, ix++) {
                        sum += a[ia] * x[ix];
                    }
                    float solved = x[offsetX + j] - sum;
                    if (!unitDiagonal) {
                        solved /= a[offsetA + j + j * lda];
                    }
                    x[offsetX + j] = solved;
                }
            }
        }

        /**
         * L<sup>T</sup>*x = b, backwards, each row (a column of L) summed from the far end with a single
         * accumulator.
         */
        private static void solveLT(final boolean unitDiagonal, final int n, final double[] a, final int offsetA, final int lda, final double[] x,
                final int offsetX) {
            for (int j = n - 1; j >= 0; j--) {
                double sum = 0D;
                for (int i = n - 1, ia = offsetA + j * lda + n - 1; i > j; i--, ia--) {
                    sum += a[ia] * x[offsetX + i];
                }
                double solved = x[offsetX + j] - sum;
                if (!unitDiagonal) {
                    solved /= a[offsetA + j + j * lda];
                }
                x[offsetX + j] = solved;
            }
        }

        /**
         * L<sup>T</sup>*x = b, backwards, each row (a column of L) summed in order with a single accumulator.
         */
        private static void solveLT(final boolean unitDiagonal, final int n, final float[] a, final int offsetA, final int lda, final float[] x,
                final int offsetX) {
            for (int j = n - 1; j >= 0; j--) {
                float sum = 0F;
                for (int i = j + 1, ia = offsetA + j * lda + j + 1; i < n; i++, ia++) {
                    sum += a[ia] * x[offsetX + i];
                }
                float solved = x[offsetX + j] - sum;
                if (!unitDiagonal) {
                    solved /= a[offsetA + j + j * lda];
                }
                x[offsetX + j] = solved;
            }
        }

        /**
         * Any increment, as in the reference implementation.
         */
        private static void solveStrided(final boolean upper, final boolean transpose, final boolean unitDiagonal, final int n, final double[] a,
                final int offsetA, final int lda, final double[] x, final int offsetX, final int incX) {

            int start = incX > 0 ? offsetX : offsetX - (n - 1) * incX;

            if (!transpose) {
                if (upper) {
                    for (int j = n - 1; j >= 0; j--) {
                        int jx = start + j * incX;
                        if (!unitDiagonal) {
                            x[jx] /= a[offsetA + j + j * lda];
                        }
                        double temp = x[jx];
                        for (int i = 0; i < j; i++) {
                            x[start + i * incX] -= temp * a[offsetA + i + j * lda];
                        }
                    }
                } else {
                    for (int j = 0; j < n; j++) {
                        int jx = start + j * incX;
                        if (!unitDiagonal) {
                            x[jx] /= a[offsetA + j + j * lda];
                        }
                        double temp = x[jx];
                        for (int i = j + 1; i < n; i++) {
                            x[start + i * incX] -= temp * a[offsetA + i + j * lda];
                        }
                    }
                }
            } else if (upper) {
                for (int j = 0; j < n; j++) {
                    double temp = x[start + j * incX];
                    for (int i = 0; i < j; i++) {
                        temp -= a[offsetA + i + j * lda] * x[start + i * incX];
                    }
                    x[start + j * incX] = unitDiagonal ? temp : temp / a[offsetA + j + j * lda];
                }
            } else {
                for (int j = n - 1; j >= 0; j--) {
                    double temp = x[start + j * incX];
                    for (int i = n - 1; i > j; i--) {
                        temp -= a[offsetA + i + j * lda] * x[start + i * incX];
                    }
                    x[start + j * incX] = unitDiagonal ? temp : temp / a[offsetA + j + j * lda];
                }
            }
        }

        private static void solveStrided(final boolean upper, final boolean transpose, final boolean unitDiagonal, final int n, final float[] a,
                final int offsetA, final int lda, final float[] x, final int offsetX, final int incX) {

            int start = incX > 0 ? offsetX : offsetX - (n - 1) * incX;

            if (!transpose) {
                if (upper) {
                    for (int j = n - 1; j >= 0; j--) {
                        int jx = start + j * incX;
                        if (!unitDiagonal) {
                            x[jx] /= a[offsetA + j + j * lda];
                        }
                        float temp = x[jx];
                        for (int i = 0; i < j; i++) {
                            x[start + i * incX] -= temp * a[offsetA + i + j * lda];
                        }
                    }
                } else {
                    for (int j = 0; j < n; j++) {
                        int jx = start + j * incX;
                        if (!unitDiagonal) {
                            x[jx] /= a[offsetA + j + j * lda];
                        }
                        float temp = x[jx];
                        for (int i = j + 1; i < n; i++) {
                            x[start + i * incX] -= temp * a[offsetA + i + j * lda];
                        }
                    }
                }
            } else if (upper) {
                for (int j = 0; j < n; j++) {
                    float temp = x[start + j * incX];
                    for (int i = 0; i < j; i++) {
                        temp -= a[offsetA + i + j * lda] * x[start + i * incX];
                    }
                    x[start + j * incX] = unitDiagonal ? temp : temp / a[offsetA + j + j * lda];
                }
            } else {
                for (int j = n - 1; j >= 0; j--) {
                    float temp = x[start + j * incX];
                    for (int i = n - 1; i > j; i--) {
                        temp -= a[offsetA + i + j * lda] * x[start + i * incX];
                    }
                    x[start + j * incX] = unitDiagonal ? temp : temp / a[offsetA + j + j * lda];
                }
            }
        }

        /**
         * U*x = b, backwards, as a column sweep that skips zero elements of x.
         */
        private static void solveU(final boolean unitDiagonal, final int n, final double[] a, final int offsetA, final int lda, final double[] x,
                final int offsetX) {
            for (int j = n - 1; j >= 0; j--) {
                if (!unitDiagonal) {
                    x[offsetX + j] /= a[offsetA + j + j * lda];
                }
                double solved = x[offsetX + j];
                if (solved != ZERO) {
                    AXPY.JVM.invoke(j, -solved, a, offsetA + j * lda, 1, x, offsetX, 1);
                }
            }
        }

        /**
         * U*x = b, backwards, as a column sweep that skips zero elements of x.
         */
        private static void solveU(final boolean unitDiagonal, final int n, final float[] a, final int offsetA, final int lda, final float[] x,
                final int offsetX) {
            for (int j = n - 1; j >= 0; j--) {
                if (!unitDiagonal) {
                    x[offsetX + j] /= a[offsetA + j + j * lda];
                }
                float solved = x[offsetX + j];
                if (solved != 0F) {
                    AXPY.invoke(x, offsetX, -solved, a, offsetA + j * lda, 0, j);
                }
            }
        }

        /**
         * U<sup>T</sup>*x = b, forwards, each row (a column of U) summed with a single accumulator.
         */
        private static void solveUT(final boolean unitDiagonal, final int n, final double[] a, final int offsetA, final int lda, final double[] x,
                final int offsetX) {
            for (int j = 0; j < n; j++) {
                double sum = 0D;
                for (int i = 0, ia = offsetA + j * lda, ix = offsetX; i < j; i++, ia++, ix++) {
                    sum += a[ia] * x[ix];
                }
                double solved = x[offsetX + j] - sum;
                if (!unitDiagonal) {
                    solved /= a[offsetA + j + j * lda];
                }
                x[offsetX + j] = solved;
            }
        }

        private static void solveUT(final boolean unitDiagonal, final int n, final float[] a, final int offsetA, final int lda, final float[] x,
                final int offsetX) {
            for (int j = 0; j < n; j++) {
                float sum = 0F;
                for (int i = 0, ia = offsetA + j * lda, ix = offsetX; i < j; i++, ia++, ix++) {
                    sum += a[ia] * x[ix];
                }
                float solved = x[offsetX + j] - sum;
                if (!unitDiagonal) {
                    solved /= a[offsetA + j + j * lda];
                }
                x[offsetX + j] = solved;
            }
        }

        /**
         * The pure Java implementation of
         * {@link TRSV#invoke(boolean, boolean, boolean, int, double[], int, int, double[], int, int)},
         * without argument checks, for n > 0.
         */
        static void invoke(final boolean upper, final boolean transpose, final boolean unitDiagonal, final int n, final double[] a, final int offsetA,
                final int lda, final double[] x, final int offsetX, final int incX) {
            if (incX != 1) {
                JVM.solveStrided(upper, transpose, unitDiagonal, n, a, offsetA, lda, x, offsetX, incX);
            } else if (transpose) {
                if (upper) {
                    JVM.solveUT(unitDiagonal, n, a, offsetA, lda, x, offsetX);
                } else {
                    JVM.solveLT(unitDiagonal, n, a, offsetA, lda, x, offsetX);
                }
            } else if (upper) {
                JVM.solveU(unitDiagonal, n, a, offsetA, lda, x, offsetX);
            } else {
                JVM.solveL(unitDiagonal, n, a, offsetA, lda, x, offsetX);
            }
        }

        /**
         * The pure Java implementation of
         * {@link TRSV#invoke(boolean, boolean, boolean, int, float[], int, int, float[], int, int)}, without
         * argument checks, for n > 0.
         */
        static void invoke(final boolean upper, final boolean transpose, final boolean unitDiagonal, final int n, final float[] a, final int offsetA,
                final int lda, final float[] x, final int offsetX, final int incX) {
            if (incX != 1) {
                JVM.solveStrided(upper, transpose, unitDiagonal, n, a, offsetA, lda, x, offsetX, incX);
            } else if (transpose) {
                if (upper) {
                    JVM.solveUT(unitDiagonal, n, a, offsetA, lda, x, offsetX);
                } else {
                    JVM.solveLT(unitDiagonal, n, a, offsetA, lda, x, offsetX);
                }
            } else {
                if (upper) {
                    JVM.solveU(unitDiagonal, n, a, offsetA, lda, x, offsetX);
                } else {
                    JVM.solveL(unitDiagonal, n, a, offsetA, lda, x, offsetX);
                }
            }
        }

        /**
         * The pure Java implementation of
         * {@link TRSV#invoke(boolean, boolean, boolean, int, Scalar[], int, int, Scalar[], int, int, Scalar.Factory)},
         * without argument checks, for n > 0. Any increment: each element of x is solved for as its element
         * of b minus a sum, accumulated in order, of products with the element of op(A) on the left, and then
         * divided from the left by the diagonal element.
         */
        static <N extends Scalar<N>> void invoke(final boolean upper, final boolean transpose, final boolean unitDiagonal, final int n, final N[] a,
                final int offsetA, final int lda, final N[] x, final int offsetX, final int incX, final Scalar.Factory<N> scalar) {

            int start = incX > 0 ? offsetX : offsetX - (n - 1) * incX;
            boolean forwards = upper == transpose;

            for (int k = 0; k < n; k++) {

                int j = forwards ? k : n - 1 - k;

                Scalar<N> sum = scalar.zero();
                for (int i = forwards ? 0 : j + 1, limit = forwards ? j : n; i < limit; i++) {
                    N element = transpose ? a[offsetA + i + j * lda].conjugate().get() : a[offsetA + j + i * lda];
                    sum = sum.add(element.multiply(x[start + i * incX]));
                }
                Scalar<N> solved = x[start + j * incX].subtract(sum);

                if (!unitDiagonal) {
                    N diagonal = transpose ? a[offsetA + j + j * lda].conjugate().get() : a[offsetA + j + j * lda];
                    // From the left: diagonal^-1 * solved
                    solved = solved.conjugate().divide(diagonal.conjugate()).conjugate();
                }

                x[start + j * incX] = solved.get();
            }
        }

    }

    /**
     * The pure Java L*x = b, with contiguous x, sums each row with a single accumulator up to this order, and
     * is a column sweep above it. The row sums are faster while L is small, the column sweep from n ≈ 50 –
     * more than 5 times faster for n = 512.
     */
    private static final int COLUMN_SWEEP_THRESHOLD = 40;

    /**
     * {@code dtrsv}: solves op(A)*x = b, x overwrites b
     * <p>
     * A is column-major, stored in its array from an offset with a leading dimension. Only the upper or lower
     * triangle of A is read, and not its diagonal if unit. x is stored from an offset with an increment; as
     * in BLAS, a negative increment walks the vector backwards, and the offset is still the lowest index
     * accessed. No test for singularity is done.
     *
     * @param upper        A is upper triangular if true, otherwise lower
     * @param transpose    op(A) = A<sup>T</sup> if true, otherwise A
     * @param unitDiagonal A has a unit diagonal (assumed, not read) if true
     * @param n            The order of A
     */
    public static void invoke(final boolean upper, final boolean transpose, final boolean unitDiagonal, final int n, final double[] a, final int offsetA,
            final int lda, final double[] x, final int offsetX, final int incX) {

        ArrayOperation.checkArgument("TRSV", "n", n >= 0);
        ArrayOperation.checkArgument("TRSV", "lda", lda >= Math.max(1, n));
        ArrayOperation.checkArgument("TRSV", "incX", incX != 0);

        if (n == 0) {
            return;
        }

        JVM.invoke(upper, transpose, unitDiagonal, n, a, offsetA, lda, x, offsetX, incX);
    }

    /**
     * {@code strsv}
     *
     * @see #invoke(boolean, boolean, boolean, int, double[], int, int, double[], int, int)
     */
    public static void invoke(final boolean upper, final boolean transpose, final boolean unitDiagonal, final int n, final float[] a, final int offsetA,
            final int lda, final float[] x, final int offsetX, final int incX) {

        ArrayOperation.checkArgument("TRSV", "n", n >= 0);
        ArrayOperation.checkArgument("TRSV", "lda", lda >= Math.max(1, n));
        ArrayOperation.checkArgument("TRSV", "incX", incX != 0);

        if (n == 0) {
            return;
        }

        JVM.invoke(upper, transpose, unitDiagonal, n, a, offsetA, lda, x, offsetX, incX);
    }

    /**
     * As {@link #invoke(boolean, boolean, boolean, int, double[], int, int, double[], int, int)}, for other
     * number types: op(A) is A or A<sup>H</sup>, the conjugate transpose, and A is applied from the left.
     * There is only a pure Java implementation.
     *
     * @param transpose op(A) = A<sup>H</sup> if true, otherwise A
     */
    public static <N extends Scalar<N>> void invoke(final boolean upper, final boolean transpose, final boolean unitDiagonal, final int n, final N[] a,
            final int offsetA, final int lda, final N[] x, final int offsetX, final int incX, final Scalar.Factory<N> scalar) {

        ArrayOperation.checkArgument("TRSV", "n", n >= 0);
        ArrayOperation.checkArgument("TRSV", "lda", lda >= Math.max(1, n));
        ArrayOperation.checkArgument("TRSV", "incX", incX != 0);

        if (n == 0) {
            return;
        }

        JVM.invoke(upper, transpose, unitDiagonal, n, a, offsetA, lda, x, offsetX, incX, scalar);
    }

}

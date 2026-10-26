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

import org.ojalgo.array.operation.AXPY;
import org.ojalgo.array.operation.ArrayOperation;
import org.ojalgo.concurrent.DivideAndConquer;
import org.ojalgo.concurrent.Parallelism;
import org.ojalgo.scalar.Scalar;

/**
 * LAPACK {@code ?gebrd}, for m >= n: reduces a general m-by-n matrix A to upper bidiagonal form B by an
 * orthogonal transformation:
 *
 * <pre>{@code
 * Q^T * A * P = B
 * }</pre>
 *
 * On exit, the diagonal and the first superdiagonal of A hold B, the elements below the diagonal hold the
 * reflectors that make Q, and the elements above the first superdiagonal hold those that make P:
 *
 * <pre>{@code
 * Q = H(1) * H(2) * ... * H(n)
 * P = G(1) * G(2) * ... * G(n-1)
 * H(i) = I - tauq(i) * v * v^T
 * G(i) = I - taup(i) * u * u^T
 * }</pre>
 *
 * v(1:i-1) = 0 and v(i) = 1 (not stored), and v(i+1:m) is stored in A(i+1:m,i). u(1:i) = 0 and u(i+1) = 1
 * (not stored), and u(i+2:n) is stored in A(i,i+2:n). With tauq(i) or taup(i) = 0, H(i) or G(i) is the
 * identity, and taup(n) = 0. The diagonal and the superdiagonal of B are also written to d and e. Reducing a
 * matrix with m < n to lower bidiagonal form, the other half of LAPACK's {@code ?gebrd}, is not implemented
 * (reduce A<sup>T</sup> instead).
 * <p>
 * For other number types Q and P are unitary, and B is not necessarily real. There is only a pure Java
 * implementation for those.
 * <p>
 * {@link ORGBR} forms Q and P explicitly.
 */
public abstract class GEBRD implements LAPACK {

    /**
     * The pure Java implementation.
     * <p>
     * Unblocked: for each column, the reflector H(i) that zeroes the elements below the diagonal is generated
     * and applied from the left to the columns to the right (as by {@link GEQRF}); then the reflector G(i)
     * that zeroes the elements to the right of the superdiagonal is generated from a copy of the row and
     * applied from the right to the rows below: w = A*u, then A = A - tau * w * u<sup>T</sup>. A*u (by rows)
     * and the update (by columns) are split over several threads when they are large, see
     * {@link GEBRD#THRESHOLD}.
     */
    static final class JVM {

        private static final DivideAndConquer.Divider DIVIDER_COLUMNS = Parallelism.CORES.newDivider();
        private static final DivideAndConquer.Divider DIVIDER_ROWS = Parallelism.THREADS.newDivider();

        /**
         * w(i) = (A*u)(i) for the rows from (inclusive) to (exclusive), over the columns first to n-1.
         * u(first) to u(n-1) are stored from {@code u[0]}.
         */
        private static void multiplyRows(final int n, final double[] a, final int offsetA, final int lda, final double[] u, final double[] w, final int first,
                final int from, final int to) {
            Arrays.fill(w, from, to, ZERO);
            for (int c = first; c < n; c++) {
                AXPY.JVM.invoke(to - from, u[c - first], a, offsetA + from + c * lda, 1, w, from, 1);
            }
        }

        /**
         * @see #multiplyRows(int, double[], int, int, double[], double[], int, int, int)
         */
        private static <N extends Scalar<N>> void multiplyRows(final int n, final N[] a, final int offsetA, final int lda, final N[] u, final N[] w,
                final int first, final int from, final int to, final Scalar.Factory<N> scalar) {
            for (int r = from; r < to; r++) {
                Scalar<N> sum = scalar.zero();
                for (int c = first; c < n; c++) {
                    sum = sum.add(a[offsetA + r + c * lda].multiply(u[c - first]));
                }
                w[r] = sum.get();
            }
        }

        /**
         * A = A - tau * w * u<sup>T</sup>, in rows row to m-1 of the columns from (inclusive) to (exclusive).
         * u(first) to u(n-1) are stored from {@code u[0]}.
         */
        private static void rank1Columns(final int m, final double[] a, final int offsetA, final int lda, final double[] u, final double tau, final double[] w,
                final int row, final int first, final int from, final int to) {
            for (int c = from; c < to; c++) {
                AXPY.JVM.invoke(m - row, -tau * u[c - first], w, row, 1, a, offsetA + row + c * lda, 1);
            }
        }

        /**
         * A = A - tau * w * u<sup>H</sup>
         *
         * @see #rank1Columns(int, double[], int, int, double[], double, double[], int, int, int, int)
         */
        private static <N extends Scalar<N>> void rank1Columns(final int m, final N[] a, final int offsetA, final int lda, final N[] u, final N tau,
                final N[] w, final int row, final int first, final int from, final int to) {
            for (int c = from; c < to; c++) {
                N factor = tau.multiply(u[c - first].conjugate()).get();
                int column = offsetA + c * lda;
                for (int r = row; r < m; r++) {
                    a[column + r] = a[column + r].subtract(w[r].multiply(factor)).get();
                }
            }
        }

        /**
         * The pure Java implementation of
         * {@link GEBRD#invoke(int, int, double[], int, int, double[], int, double[], int, double[], int, double[], int)},
         * without argument checks, for m >= n > 0.
         */
        static void invoke(final int m, final int n, final double[] a, final int offsetA, final int lda, final double[] d, final int offsetD, final double[] e,
                final int offsetE, final double[] tauq, final int offsetTauq, final double[] taup, final int offsetTaup) {

            double[] u = new double[n];
            double[] w = new double[m];

            for (int i = 0; i < n; i++) {

                int column = offsetA + i * lda;

                double betaQ = i + 1 < m ? GEQRF.JVM.generate(m, a, column, i) : ZERO;

                tauq[offsetTauq + i] = betaQ;
                d[offsetD + i] = a[column + i];

                if (betaQ != ZERO && i + 1 < n) {
                    // v(i) = 1 temporarily in place of the diagonal element, so that the column is v
                    a[column + i] = ONE;
                    GEQRF.JVM.reflect(m, a, column + i, betaQ, a, offsetA, lda, i, i + 1, n);
                    a[column + i] = d[offsetD + i];
                }

                if (i + 1 < n) {

                    // A(i,i+1) to A(i,n-1), copied to u
                    int count = n - i - 1;
                    int superdiagonal = column + i + lda;
                    for (int c = 0; c < count; c++) {
                        u[c] = a[superdiagonal + c * lda];
                    }

                    double betaP = count > 1 ? GEQRF.JVM.generate(count, u, 0, 0) : ZERO;

                    taup[offsetTaup + i] = betaP;
                    e[offsetE + i] = u[0];

                    for (int c = 0; c < count; c++) {
                        a[superdiagonal + c * lda] = u[c];
                    }

                    if (betaP != ZERO && i + 1 < m) {
                        u[0] = ONE;
                        JVM.reflectRight(m, n, a, offsetA, lda, i + 1, i + 1, u, betaP, w);
                    }

                } else {

                    taup[offsetTaup + i] = ZERO;
                }
            }
        }

        /**
         * The pure Java implementation of
         * {@link GEBRD#invoke(int, int, Scalar[], int, int, Scalar[], int, Scalar[], int, Scalar[], int, Scalar[], int, Scalar.Factory)},
         * without argument checks, for m >= n > 0.
         */
        static <N extends Scalar<N>> void invoke(final int m, final int n, final N[] a, final int offsetA, final int lda, final N[] d, final int offsetD,
                final N[] e, final int offsetE, final N[] tauq, final int offsetTauq, final N[] taup, final int offsetTaup, final Scalar.Factory<N> scalar) {

            N zero = scalar.zero().get();
            N one = scalar.one().get();

            N[] u = scalar.newArrayInstance(n);
            N[] w = scalar.newArrayInstance(m);

            for (int i = 0; i < n; i++) {

                int column = offsetA + i * lda;

                N betaQ = i + 1 < m ? GEQRF.JVM.generate(m, a, column, i, scalar) : null;

                tauq[offsetTauq + i] = betaQ != null ? betaQ : zero;
                d[offsetD + i] = a[column + i];

                if (betaQ != null && i + 1 < n) {
                    a[column + i] = one;
                    GEQRF.JVM.reflect(m, a, column + i, betaQ, a, offsetA, lda, i, i + 1, n, scalar);
                    a[column + i] = d[offsetD + i];
                }

                if (i + 1 < n) {

                    // The conjugate of A(i,i+1) to A(i,n-1), copied to u: x*G = (G*x^H)^H
                    int count = n - i - 1;
                    int superdiagonal = column + i + lda;
                    for (int c = 0; c < count; c++) {
                        u[c] = a[superdiagonal + c * lda].conjugate().get();
                    }

                    N betaP = count > 1 ? GEQRF.JVM.generate(count, u, 0, 0, scalar) : null;

                    taup[offsetTaup + i] = betaP != null ? betaP : zero;
                    e[offsetE + i] = u[0].conjugate().get();

                    a[superdiagonal] = e[offsetE + i];
                    for (int c = 1; c < count; c++) {
                        a[superdiagonal + c * lda] = u[c];
                    }

                    if (betaP != null && i + 1 < m) {
                        u[0] = one;
                        JVM.reflectRight(m, n, a, offsetA, lda, i + 1, i + 1, u, betaP, w, scalar);
                    }

                } else {

                    taup[offsetTaup + i] = zero;
                }
            }
        }

        /**
         * Applies G = I - tau * u * u<sup>T</sup>, from the right, to rows row to m-1 of columns first to n-1
         * of A. u(first) to u(n-1) are stored from {@code u[0]}, u(first) = 1 included. w is workspace, at
         * least m long.
         */
        static void reflectRight(final int m, final int n, final double[] a, final int offsetA, final int lda, final int row, final int first, final double[] u,
                final double tau, final double[] w) {

            int rows = m - row;
            int columns = n - first;

            // A row of A*u: columns multiply-adds
            int chunkRows = MatrixOperation.chunk(THRESHOLD, columns);
            if (rows > chunkRows) {
                DIVIDER_ROWS.threshold(chunkRows).divide(row, m, (from, to) -> JVM.multiplyRows(n, a, offsetA, lda, u, w, first, from, to));
            } else {
                JVM.multiplyRows(n, a, offsetA, lda, u, w, first, row, m);
            }

            // A column of the update: rows multiply-adds
            int chunkColumns = MatrixOperation.chunk(THRESHOLD, rows);
            if (columns > chunkColumns) {
                DIVIDER_COLUMNS.threshold(chunkColumns).divide(first, n, (from, to) -> JVM.rank1Columns(m, a, offsetA, lda, u, tau, w, row, first, from, to));
            } else {
                JVM.rank1Columns(m, a, offsetA, lda, u, tau, w, row, first, first, n);
            }
        }

        /**
         * As {@link #reflectRight(int, int, double[], int, int, int, int, double[], double, double[])}, for
         * other number types: G = I - tau * u * u<sup>H</sup>, with tau real.
         */
        static <N extends Scalar<N>> void reflectRight(final int m, final int n, final N[] a, final int offsetA, final int lda, final int row, final int first,
                final N[] u, final N tau, final N[] w, final Scalar.Factory<N> scalar) {

            int rows = m - row;
            int columns = n - first;

            int chunkRows = MatrixOperation.chunk(THRESHOLD, columns);
            if (rows > chunkRows) {
                DIVIDER_ROWS.threshold(chunkRows).divide(row, m, (from, to) -> JVM.multiplyRows(n, a, offsetA, lda, u, w, first, from, to, scalar));
            } else {
                JVM.multiplyRows(n, a, offsetA, lda, u, w, first, row, m, scalar);
            }

            int chunkColumns = MatrixOperation.chunk(THRESHOLD, rows);
            if (columns > chunkColumns) {
                DIVIDER_COLUMNS.threshold(chunkColumns).divide(first, n, (from, to) -> JVM.rank1Columns(m, a, offsetA, lda, u, tau, w, row, first, from, to));
            } else {
                JVM.rank1Columns(m, a, offsetA, lda, u, tau, w, row, first, first, n);
            }
        }

    }

    /**
     * The Java implementation splits A*u (by rows) and the update from the right (by columns) over several
     * threads when they are larger than this cubed (in multiply-adds). Each part then gets more than half of
     * that. The application of the reflectors from the left is split as by {@link GEQRF}.
     */
    static int THRESHOLD = 48;

    /**
     * {@code dgebrd} for m >= n: Q<sup>T</sup>*A*P = B, B and the reflectors that make Q and P overwrite A
     * <p>
     * A is column-major, stored in its array from an offset with a leading dimension. The diagonal of B,
     * d(i), and tauq(i) and taup(i), i = 1, ..., n, are stored in {@code d[offsetD + i - 1]},
     * {@code tauq[offsetTauq + i - 1]} and {@code taup[offsetTaup + i - 1]}, and the superdiagonal of B,
     * e(i), i = 1, ..., n-1, in {@code e[offsetE + i - 1]}.
     *
     * @param m The number of rows of A, m >= n
     * @param n The number of columns of A
     */
    public static void invoke(final int m, final int n, final double[] a, final int offsetA, final int lda, final double[] d, final int offsetD,
            final double[] e, final int offsetE, final double[] tauq, final int offsetTauq, final double[] taup, final int offsetTaup) {

        ArrayOperation.checkArgument("GEBRD", "n", n >= 0);
        ArrayOperation.checkArgument("GEBRD", "m", m >= n);
        ArrayOperation.checkArgument("GEBRD", "lda", lda >= Math.max(1, m));

        if (n == 0) {
            return;
        }

        JVM.invoke(m, n, a, offsetA, lda, d, offsetD, e, offsetE, tauq, offsetTauq, taup, offsetTaup);
    }

    /**
     * As
     * {@link #invoke(int, int, double[], int, int, double[], int, double[], int, double[], int, double[], int)},
     * for other number types: Q and P are unitary, H(i) = I - tauq(i) * v * v<sup>H</sup> and G(i) = I -
     * taup(i) * u * u<sup>H</sup>, with tauq(i) and taup(i) real and 0 where there is no reflector. The
     * diagonal and superdiagonal of B are not necessarily real. There is only a pure Java implementation.
     */
    public static <N extends Scalar<N>> void invoke(final int m, final int n, final N[] a, final int offsetA, final int lda, final N[] d, final int offsetD,
            final N[] e, final int offsetE, final N[] tauq, final int offsetTauq, final N[] taup, final int offsetTaup, final Scalar.Factory<N> scalar) {

        ArrayOperation.checkArgument("GEBRD", "n", n >= 0);
        ArrayOperation.checkArgument("GEBRD", "m", m >= n);
        ArrayOperation.checkArgument("GEBRD", "lda", lda >= Math.max(1, m));

        if (n == 0) {
            return;
        }

        JVM.invoke(m, n, a, offsetA, lda, d, offsetD, e, offsetE, tauq, offsetTauq, taup, offsetTaup, scalar);
    }

}

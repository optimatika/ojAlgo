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

/**
 * LAPACK {@code ?gesdd}: the singular value decomposition of a general m-by-n matrix A:
 *
 * <pre>{@code
 * A = U * diag(s) * V ^ T
 * }</pre>
 *
 * The k = min(m,n) singular values s are non-negative and in descending order, and U and V have orthonormal
 * columns, the left and right singular vectors. Economy size, U is m-by-k and V n-by-k; full size, U is
 * m-by-m and V n-by-n, both orthogonal. V is returned, not V<sup>T</sup> as by LAPACK's {@code ?gesdd}. The
 * matrix is reduced to bidiagonal form ({@link GEBRD}), the orthogonal factors are formed ({@link ORGQR},
 * {@link ORGBR}) and the bidiagonal matrix is diagonalised by the implicit QR method ({@link BDSQR}).
 * A<sup>T</sup> = V * diag(s) * U<sup>T</sup> is decomposed instead when m < n.
 */
public abstract class GESDD implements LAPACK {

    /**
     * The pure Java implementation.
     */
    static final class JVM {

        /**
         * Exchanges columns i and j, rows 0 to rows-1, of A.
         */
        private static void exchange(final int rows, final double[] a, final int offsetA, final int lda, final int i, final int j) {
            int colI = offsetA + i * lda;
            int colJ = offsetA + j * lda;
            for (int r = 0; r < rows; r++) {
                double tmp = a[colI + r];
                a[colI + r] = a[colJ + r];
                a[colJ + r] = tmp;
            }
        }

        /**
         * Negates column j, rows 0 to rows-1, of A.
         */
        private static void negate(final int rows, final double[] a, final int offsetA, final int lda, final int j) {
            int colJ = offsetA + j * lda;
            for (int r = 0; r < rows; r++) {
                a[colJ + r] = -a[colJ + r];
            }
        }

        /**
         * Applies the plane rotation of columns low and high, as reported by {@link BDSQR}, to rows 0 to
         * rows-1 of A.
         */
        private static void rotate(final int rows, final double[] a, final int offsetA, final int lda, final int low, final int high, final double cos,
                final double sin) {
            int colL = offsetA + low * lda;
            int colH = offsetA + high * lda;
            for (int r = 0; r < rows; r++) {
                double oldL = a[colL + r];
                double oldH = a[colH + r];
                a[colL + r] = cos * oldL - sin * oldH;
                a[colH + r] = cos * oldH + sin * oldL;
            }
        }

        /**
         * The decomposition of an m-by-n matrix A with m >= n: the left singular vectors to U, m-by-n or
         * m-by-m (full size), and the right singular vectors to V, n-by-n. A is destroyed.
         */
        private static void tall(final boolean vectors, final boolean fullSize, final int m, final int n, final double[] a, final int offsetA, final int lda,
                final double[] s, final int offsetS, final double[] u, final int offsetU, final int ldu, final double[] v, final int offsetV, final int ldv) {

            double[] d = new double[n];
            double[] e = new double[n];
            double[] tauq = new double[n];
            double[] taup = new double[n];

            GEBRD.JVM.invoke(m, n, a, offsetA, lda, d, 0, e, 0, tauq, 0, taup, 0);

            if (vectors) {

                for (int j = 0; j < n; j++) {
                    System.arraycopy(a, offsetA + j + 1 + j * lda, u, offsetU + j + 1 + j * ldu, m - j - 1);
                }
                ORGQR.JVM.invoke(m, fullSize ? m : n, n, u, offsetU, ldu, tauq, 0);

                ORGBR.JVM.generateP(n, a, offsetA, lda, taup, 0, v, offsetV, ldv);

                BDSQR.JVM.invoke(d, e, (low, high, cos, sin) -> JVM.rotate(m, u, offsetU, ldu, low, high, cos, sin),
                        (low, high, cos, sin) -> JVM.rotate(n, v, offsetV, ldv, low, high, cos, sin), (i, j) -> JVM.exchange(m, u, offsetU, ldu, i, j),
                        (i, j) -> JVM.exchange(n, v, offsetV, ldv, i, j), j -> JVM.negate(n, v, offsetV, ldv, j));

            } else {

                BDSQR.JVM.invoke(d, e, RotateColumns.NULL, RotateColumns.NULL, ExchangeColumns.NULL, ExchangeColumns.NULL, NegateColumn.NULL);
            }

            System.arraycopy(d, 0, s, offsetS, n);
        }

        /**
         * The pure Java implementation of
         * {@link GESDD#invoke(boolean, boolean, int, int, double[], int, int, double[], int, double[], int, int, double[], int, int)},
         * without argument checks, for m, n > 0.
         */
        static int invoke(final boolean vectors, final boolean fullSize, final int m, final int n, final double[] a, final int offsetA, final int lda,
                final double[] s, final int offsetS, final double[] u, final int offsetU, final int ldu, final double[] v, final int offsetV, final int ldv) {

            if (m >= n) {

                JVM.tall(vectors, fullSize, m, n, a, offsetA, lda, s, offsetS, u, offsetU, ldu, v, offsetV, ldv);

            } else {

                double[] transposed = new double[n * m];
                for (int j = 0; j < n; j++) {
                    int column = offsetA + j * lda;
                    for (int i = 0; i < m; i++) {
                        transposed[j + i * n] = a[column + i];
                    }
                }

                JVM.tall(vectors, fullSize, n, m, transposed, 0, n, s, offsetS, v, offsetV, ldv, u, offsetU, ldu);
            }

            return 0;
        }

    }

    /**
     * {@code dgesdd} with JOBZ = 'S' (economy size), 'A' (full size) or 'N' (the singular values only), and V
     * instead of V<sup>T</sup>
     * <p>
     * A, U and V are column-major, each stored in its array from an offset with a leading dimension. The
     * singular values s(i), i = 1, ..., min(m,n), in descending order, are stored in
     * {@code s[offsetS + i - 1]}. A is destroyed. With the singular values only, U and V are not referenced
     * (and may be null).
     *
     * @param vectors  The singular vectors are computed if true, otherwise the singular values only
     * @param fullSize U is m-by-m and V n-by-n if true, otherwise m-by-min(m,n) and n-by-min(m,n)
     * @param m        The number of rows of A
     * @param n        The number of columns of A
     * @return 0, or i > 0 if the algorithm failed to converge
     */
    public static int invoke(final boolean vectors, final boolean fullSize, final int m, final int n, final double[] a, final int offsetA, final int lda,
            final double[] s, final int offsetS, final double[] u, final int offsetU, final int ldu, final double[] v, final int offsetV, final int ldv) {

        ArrayOperation.checkArgument("GESDD", "m", m >= 0);
        ArrayOperation.checkArgument("GESDD", "n", n >= 0);
        ArrayOperation.checkArgument("GESDD", "lda", lda >= Math.max(1, m));
        ArrayOperation.checkArgument("GESDD", "ldu", !vectors || ldu >= Math.max(1, m));
        ArrayOperation.checkArgument("GESDD", "ldv", !vectors || ldv >= Math.max(1, n));

        if (m == 0 || n == 0) {
            return 0;
        }

        return JVM.invoke(vectors, fullSize, m, n, a, offsetA, lda, s, offsetS, u, offsetU, ldu, v, offsetV, ldv);
    }

}

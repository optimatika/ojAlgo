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

import java.util.Arrays;

import org.ojalgo.array.operation.ArrayOperation;
import org.ojalgo.scalar.Scalar;

/**
 * LAPACK {@code ?syevd}, for the lower triangle: all eigenvalues, and optionally the eigenvectors, of a
 * symmetric n-by-n matrix A:
 *
 * <pre>{@code
 * A = Z * diag(w) * Z ^ T
 * }</pre>
 *
 * Z is orthogonal, and the eigenvalues w are in ascending order. Only the lower triangle of A is read. A is
 * reduced to tridiagonal form ({@link SYTRD}), Q is formed ({@link ORGTR}) and the tridiagonal matrix is
 * diagonalised by the implicit QL method ({@link STEQR}). For other number types A is Hermitian and Z
 * unitary.
 */
public abstract class SYEVD implements LAPACK {

    /**
     * The pure Java implementations.
     */
    static final class JVM {

        /**
         * Exchanges columns i and j of the n-by-n matrix A.
         */
        private static void exchange(final int n, final double[] a, final int offsetA, final int lda, final int i, final int j) {
            int colI = offsetA + i * lda;
            int colJ = offsetA + j * lda;
            for (int r = 0; r < n; r++) {
                double tmp = a[colI + r];
                a[colI + r] = a[colJ + r];
                a[colJ + r] = tmp;
            }
        }

        private static <N extends Scalar<N>> void exchange(final int n, final N[] a, final int offsetA, final int lda, final int i, final int j) {
            int colI = offsetA + i * lda;
            int colJ = offsetA + j * lda;
            for (int r = 0; r < n; r++) {
                N tmp = a[colI + r];
                a[colI + r] = a[colJ + r];
                a[colJ + r] = tmp;
            }
        }

        /**
         * Applies the plane rotation of columns low and high, as reported by {@link STEQR}, to the n-by-n
         * matrix A.
         */
        private static void rotate(final int n, final double[] a, final int offsetA, final int lda, final int low, final int high, final double cos,
                final double sin) {
            int colL = offsetA + low * lda;
            int colH = offsetA + high * lda;
            for (int r = 0; r < n; r++) {
                double oldL = a[colL + r];
                double oldH = a[colH + r];
                a[colL + r] = cos * oldL - sin * oldH;
                a[colH + r] = cos * oldH + sin * oldL;
            }
        }

        private static <N extends Scalar<N>> void rotate(final int n, final N[] a, final int offsetA, final int lda, final int low, final int high,
                final double cos, final double sin, final Scalar.Factory<N> scalar) {
            N c = scalar.cast(cos);
            N s = scalar.cast(sin);
            int colL = offsetA + low * lda;
            int colH = offsetA + high * lda;
            for (int r = 0; r < n; r++) {
                N oldL = a[colL + r];
                N oldH = a[colH + r];
                a[colL + r] = c.multiply(oldL).subtract(s.multiply(oldH)).get();
                a[colH + r] = c.multiply(oldH).add(s.multiply(oldL)).get();
            }
        }

        /**
         * The pure Java implementation of
         * {@link SYEVD#invoke(boolean, int, double[], int, int, double[], int)}, without argument checks, for
         * n > 0.
         */
        static int invoke(final boolean vectors, final int n, final double[] a, final int offsetA, final int lda, final double[] w, final int offsetW) {

            double[] e = new double[n];
            double[] tau = new double[n];

            SYTRD.JVM.invoke(n, a, offsetA, lda, w, offsetW, e, 0, tau, 0);

            double[] d = Arrays.copyOfRange(w, offsetW, offsetW + n);

            if (vectors) {
                ORGTR.JVM.invoke(n, a, offsetA, lda, tau, 0);
                STEQR.JVM.invoke(d, e, (low, high, cos, sin) -> JVM.rotate(n, a, offsetA, lda, low, high, cos, sin));
            } else {
                STEQR.JVM.invoke(d, e, RotateColumns.NULL);
            }

            // Ascending, with the eigenvectors following
            for (int i = 0; i < n - 1; i++) {
                int k = i;
                double min = d[i];
                for (int j = i + 1; j < n; j++) {
                    if (d[j] < min) {
                        k = j;
                        min = d[j];
                    }
                }
                if (k != i) {
                    d[k] = d[i];
                    d[i] = min;
                    if (vectors) {
                        JVM.exchange(n, a, offsetA, lda, i, k);
                    }
                }
            }

            System.arraycopy(d, 0, w, offsetW, n);

            return 0;
        }

        /**
         * The pure Java implementation of
         * {@link SYEVD#invoke(boolean, int, Scalar[], int, int, double[], int, Scalar.Factory)}, without
         * argument checks, for n > 0. A unitary diagonal scaling makes the tridiagonal matrix real before it
         * is diagonalised; its phases are applied to the columns of Q.
         */
        static <N extends Scalar<N>> int invoke(final boolean vectors, final int n, final N[] a, final int offsetA, final int lda, final double[] w,
                final int offsetW, final Scalar.Factory<N> scalar) {

            N zero = scalar.zero().get();

            N[] diagonal = scalar.newArrayInstance(n);
            N[] subdiagonal = scalar.newArrayInstance(n);
            N[] tau = scalar.newArrayInstance(n);
            Arrays.fill(subdiagonal, zero);
            Arrays.fill(tau, zero);

            SYTRD.JVM.invoke(n, a, offsetA, lda, diagonal, 0, subdiagonal, 0, tau, 0, scalar);

            // The phases p(j), p(0) = 1, with conj(p(j+1)) * subdiagonal(j) * p(j) real
            N[] phases = null;
            for (int j = 0; j < n - 1; j++) {
                Scalar<N> phase = subdiagonal[j].normalised();
                if (!phase.subtract(scalar.cast(phase.doubleValue())).isSmall(ONE)) {
                    subdiagonal[j] = subdiagonal[j].divide(phase).get();
                    if (j + 2 < n) {
                        subdiagonal[j + 1] = subdiagonal[j + 1].multiply(phase).get();
                    }
                    if (phases == null) {
                        phases = scalar.newArrayInstance(n);
                    }
                    phases[j + 1] = phase.get();
                }
            }

            double[] d = new double[n];
            double[] e = new double[n];
            for (int j = 0; j < n; j++) {
                d[j] = diagonal[j].doubleValue();
                e[j] = subdiagonal[j].doubleValue();
            }

            if (vectors) {

                ORGTR.JVM.invoke(n, a, offsetA, lda, tau, 0, scalar);

                if (phases != null) {
                    for (int j = 1; j < n; j++) {
                        N phase = phases[j];
                        if (phase != null) {
                            for (int i = 0, index = offsetA + j * lda; i < n; i++, index++) {
                                a[index] = a[index].multiply(phase).get();
                            }
                        }
                    }
                }

                STEQR.JVM.invoke(d, e, (low, high, cos, sin) -> JVM.rotate(n, a, offsetA, lda, low, high, cos, sin, scalar));

            } else {

                STEQR.JVM.invoke(d, e, RotateColumns.NULL);
            }

            // Ascending, with the eigenvectors following
            for (int i = 0; i < n - 1; i++) {
                int k = i;
                double min = d[i];
                for (int j = i + 1; j < n; j++) {
                    if (d[j] < min) {
                        k = j;
                        min = d[j];
                    }
                }
                if (k != i) {
                    d[k] = d[i];
                    d[i] = min;
                    if (vectors) {
                        JVM.exchange(n, a, offsetA, lda, i, k);
                    }
                }
            }

            System.arraycopy(d, 0, w, offsetW, n);

            return 0;
        }

    }

    /**
     * {@code dsyevd} with UPLO = 'L'
     * <p>
     * A is column-major, stored in its array from an offset with a leading dimension; its upper triangle is
     * not referenced. The eigenvalues w(i), i = 1, ..., n, in ascending order, are stored in
     * {@code w[offsetW + i - 1]}. With the eigenvectors, column i of Z, for w(i), overwrites column i of A;
     * otherwise A is destroyed.
     *
     * @param vectors The eigenvectors are computed if true, otherwise the eigenvalues only
     * @param n       The order of A
     * @return 0, or i > 0 if the algorithm failed to converge
     */
    public static int invoke(final boolean vectors, final int n, final double[] a, final int offsetA, final int lda, final double[] w, final int offsetW) {

        ArrayOperation.checkArgument("SYEVD", "n", n >= 0);
        ArrayOperation.checkArgument("SYEVD", "lda", lda >= Math.max(1, n));

        if (n == 0) {
            return 0;
        }

        return JVM.invoke(vectors, n, a, offsetA, lda, w, offsetW);
    }

    /**
     * As {@link #invoke(boolean, int, double[], int, int, double[], int)}, but A is not changed: the
     * eigenvectors, if computed, are written to Z, column-major from an offset with a leading dimension. Z is
     * also workspace; with the eigenvalues only, its contents are unspecified. Z may be A.
     */
    public static int invoke(final boolean vectors, final int n, final double[] a, final int offsetA, final int lda, final double[] w, final int offsetW,
            final double[] z, final int offsetZ, final int ldz) {

        ArrayOperation.checkArgument("SYEVD", "n", n >= 0);
        ArrayOperation.checkArgument("SYEVD", "lda", lda >= Math.max(1, n));
        ArrayOperation.checkArgument("SYEVD", "ldz", ldz >= Math.max(1, n));

        if (n == 0) {
            return 0;
        }

        if (z != a || offsetZ != offsetA || ldz != lda) {
            for (int j = 0; j < n; j++) {
                System.arraycopy(a, offsetA + j + j * lda, z, offsetZ + j + j * ldz, n - j);
            }
        }

        return JVM.invoke(vectors, n, z, offsetZ, ldz, w, offsetW);
    }

    /**
     * As {@link #invoke(boolean, int, double[], int, int, double[], int)}, for other number types: A is
     * Hermitian, Z unitary, and the eigenvalues are real. There is only a pure Java implementation.
     */
    public static <N extends Scalar<N>> int invoke(final boolean vectors, final int n, final N[] a, final int offsetA, final int lda, final double[] w,
            final int offsetW, final Scalar.Factory<N> scalar) {

        ArrayOperation.checkArgument("SYEVD", "n", n >= 0);
        ArrayOperation.checkArgument("SYEVD", "lda", lda >= Math.max(1, n));

        if (n == 0) {
            return 0;
        }

        return JVM.invoke(vectors, n, a, offsetA, lda, w, offsetW, scalar);
    }

}

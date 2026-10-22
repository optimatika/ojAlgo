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

import java.util.Arrays;
import java.util.Random;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.function.FunctionSet;
import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.scalar.Scalar;

/**
 * The overloads for other number types of {@link GEQRF}, {@link GETRF}, {@link GETRS}, {@link ORGQR},
 * {@link ORMQR}, {@link POTRF}, {@link POTRS}, {@link TRSV} and {@link TRTRS} against their definitions, as
 * in {@link LAPACKTest}, with complex numbers and quaternions. Quaternion multiplication does not commute, so
 * this also checks that the factors of every product are in the right order. All elements have nonzero
 * imaginary parts. The matrices are stored from an offset with padding, and elements outside them must be
 * left untouched. The operation thresholds are minimised, so that the multi-threaded code is run too.
 */
public class LAPACKGenericTest {

    /**
     * A column-major matrix, stored in its array from an offset with a leading dimension.
     */
    static final class Matrix<N extends Scalar<N>> {

        final int columns;
        final N[] data;
        final int ld;
        final int offset;
        final int rows;

        /**
         * Random elements, also in the padding and before the offset
         */
        Matrix(final Type<N> type, final Random random, final int rows, final int columns) {
            this.rows = rows;
            this.columns = columns;
            offset = 3;
            ld = rows + 2;
            data = type.scalar.newArrayInstance(offset + ld * columns + 2);
            for (int p = 0; p < data.length; p++) {
                data[p] = type.generator.apply(random);
            }
        }

        N get(final int row, final int col) {
            return data[offset + row + col * ld];
        }

        void set(final int row, final int col, final N value) {
            data[offset + row + col * ld] = value;
        }

    }

    static final class Type<N extends Scalar<N>> {

        final FunctionSet<N> function;
        final Function<Random, N> generator;
        final String name;
        final Scalar.Factory<N> scalar;

        Type(final String name, final GenericStore.Factory<N> factory, final Function<Random, N> generator) {
            this.name = name;
            this.generator = generator;
            scalar = factory.scalar();
            function = factory.function();
        }

    }

    private static final boolean[] BOOLEANS = { false, true };

    private static final Type<ComplexNumber> C128 = new Type<>("C128", GenericStore.C128,
            random -> ComplexNumber.of(random.nextDouble() - 0.5, random.nextDouble() - 0.5));

    private static final Type<Quaternion> H256 = new Type<>("H256", GenericStore.H256,
            random -> Quaternion.of(random.nextDouble() - 0.5, random.nextDouble() - 0.5, random.nextDouble() - 0.5, random.nextDouble() - 0.5));

    private static final int SENTINEL = -7;

    /**
     * m, n
     */
    private static final int[][] SIZES = { { 1, 1 }, { 1, 6 }, { 6, 1 }, { 3, 3 }, { 7, 4 }, { 4, 7 }, { 17, 17 }, { 30, 25 }, { 25, 30 } };

    /**
     * n, nrhs
     */
    private static final int[][] SIZES_SOLVE = { { 1, 1 }, { 3, 1 }, { 6, 4 }, { 17, 1 }, { 17, 5 }, { 40, 33 } };

    private static final double TOLERANCE = 1E-12;

    /**
     * The Frobenius norm of expected - actual must be small relative to the scale (at least 1).
     */
    private static <N extends Scalar<N>> void assertClose(final String message, final N[][] expected, final N[][] actual, final double scale) {
        double sum = 0.0;
        for (int i = 0; i < expected.length; i++) {
            for (int j = 0; j < expected[i].length; j++) {
                double norm = expected[i][j].subtract(actual[i][j]).norm();
                sum += norm * norm;
            }
        }
        double error = Math.sqrt(sum);
        TestUtils.assertTrue(message + ": " + error + " !<< " + scale, error <= TOLERANCE * Math.max(1.0, scale));
    }

    /**
     * Elements of the array outside the matrix must be the same as before.
     */
    private static <N extends Scalar<N>> void assertOutsideUnchanged(final String message, final N[] before, final Matrix<N> matrix) {
        for (int p = 0; p < before.length; p++) {
            int index = p - matrix.offset;
            boolean inside = index >= 0 && index % matrix.ld < matrix.rows && index / matrix.ld < matrix.columns;
            if (!inside) {
                TestUtils.assertTrue(message + ", changed outside the matrix at " + p, before[p] == matrix.data[p]);
            }
        }
    }

    private static <N extends Scalar<N>> N[][] copy(final Type<N> type, final Matrix<N> matrix) {
        N[][] retVal = LAPACKGenericTest.make(type, matrix.rows, matrix.columns);
        for (int i = 0; i < matrix.rows; i++) {
            for (int j = 0; j < matrix.columns; j++) {
                retVal[i][j] = matrix.get(i, j);
            }
        }
        return retVal;
    }

    private static <N extends Scalar<N>> void doTestGEQRF(final Type<N> type, final int m, final int n) {

        String message = "GEQRF " + type.name + " " + m + "x" + n;
        Random random = new Random(m + 31 * n);

        Matrix<N> a = new Matrix<>(type, random, m, n);
        // No reflector for the first column
        for (int i = 1; i < m; i++) {
            a.set(i, 0, type.scalar.zero().get());
        }
        N[][] original = LAPACKGenericTest.copy(type, a);
        N[] before = a.data.clone();

        int k = Math.min(m, n);
        N[] tau = type.scalar.newArrayInstance(k + 4);
        Arrays.fill(tau, type.scalar.one().get());
        N sentinel = tau[0];

        GEQRF.invoke(m, n, a.data, a.offset, a.ld, tau, 2, type.scalar);

        LAPACKGenericTest.assertOutsideUnchanged(message, before, a);
        TestUtils.assertTrue(message + ", tau outside", tau[0] == sentinel && tau[1] == sentinel && tau[k + 2] == sentinel && tau[k + 3] == sentinel);

        // Q, m-by-m, from ORGQR with the reflectors copied to a separate matrix
        Matrix<N> q = new Matrix<>(type, random, m, m);
        for (int j = 0; j < k; j++) {
            for (int i = 0; i < m; i++) {
                q.set(i, j, a.get(i, j));
            }
        }
        N[] beforeQ = q.data.clone();
        ORGQR.invoke(m, m, k, q.data, q.offset, q.ld, tau, 2, type.scalar);
        LAPACKGenericTest.assertOutsideUnchanged(message + ", ORGQR", beforeQ, q);

        N[][] explicitQ = LAPACKGenericTest.copy(type, q);
        N[][] r = LAPACKGenericTest.make(type, m, n);
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                r[i][j] = i <= j ? a.get(i, j) : type.scalar.zero().get();
            }
        }

        LAPACKGenericTest.assertClose(message + ", Q^H Q = I", LAPACKGenericTest.identity(type, m),
                LAPACKGenericTest.multiply(type, explicitQ, true, explicitQ, false), Math.sqrt(m));
        LAPACKGenericTest.assertClose(message + ", Q R = A", original, LAPACKGenericTest.multiply(type, explicitQ, false, r, false),
                LAPACKGenericTest.norm(original));

        // ORMQR, both ways, compared to the explicit Q
        for (boolean transpose : BOOLEANS) {
            for (int nrhs : new int[] { 1, 4 }) {
                Matrix<N> c = new Matrix<>(type, random, m, nrhs);
                N[][] expected = LAPACKGenericTest.multiply(type, explicitQ, transpose, LAPACKGenericTest.copy(type, c), false);
                N[] beforeC = c.data.clone();
                ORMQR.invoke(transpose, m, nrhs, k, a.data, a.offset, a.ld, tau, 2, c.data, c.offset, c.ld);
                LAPACKGenericTest.assertOutsideUnchanged(message + ", ORMQR", beforeC, c);
                LAPACKGenericTest.assertClose(message + ", ORMQR transpose=" + transpose, expected, LAPACKGenericTest.copy(type, c),
                        LAPACKGenericTest.norm(expected));
            }
        }
    }

    private static <N extends Scalar<N>> void doTestGETRF(final Type<N> type, final boolean pivoting, final int m, final int n) {

        String message = "GETRF " + type.name + " pivoting=" + pivoting + " " + m + "x" + n;
        Random random = new Random(m + 31 * n);

        Matrix<N> a = new Matrix<>(type, random, m, n);
        int k = Math.min(m, n);
        if (!pivoting) {
            for (int i = 0; i < k; i++) {
                a.set(i, i, a.get(i, i).add(type.scalar.cast(m + n)).get());
            }
        }
        N[][] original = LAPACKGenericTest.copy(type, a);
        N[] before = a.data.clone();

        int[] ipiv = new int[k + 4];
        Arrays.fill(ipiv, SENTINEL);

        int info;
        if (pivoting) {
            info = GETRF.invoke(m, n, a.data, a.offset, a.ld, ipiv, 2, type.scalar);
        } else {
            info = GETRF.invokeWithoutPivoting(m, n, a.data, a.offset, a.ld, type.scalar);
            for (int j = 0; j < k; j++) {
                ipiv[2 + j] = j + 1;
            }
        }

        TestUtils.assertEquals(message + ", info", 0, info);
        LAPACKGenericTest.assertOutsideUnchanged(message, before, a);
        TestUtils.assertTrue(message + ", ipiv outside", ipiv[0] == SENTINEL && ipiv[1] == SENTINEL && ipiv[k + 2] == SENTINEL && ipiv[k + 3] == SENTINEL);

        N[][] lower = LAPACKGenericTest.make(type, m, k);
        N[][] upper = LAPACKGenericTest.make(type, k, n);
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < k; j++) {
                lower[i][j] = i > j ? a.get(i, j) : i == j ? type.scalar.one().get() : type.scalar.zero().get();
                if (pivoting) {
                    TestUtils.assertTrue(message + ", |L| > 1", lower[i][j].norm() <= 1.0);
                }
            }
        }
        for (int i = 0; i < k; i++) {
            for (int j = 0; j < n; j++) {
                upper[i][j] = i <= j ? a.get(i, j) : type.scalar.zero().get();
            }
        }

        N[][] product = LAPACKGenericTest.multiply(type, lower, false, upper, false);
        for (int j = k - 1; j >= 0; j--) {
            int row = ipiv[2 + j] - 1;
            TestUtils.assertTrue(message + ", ipiv " + j, row >= j && row < m);
            N[] tmp = product[row];
            product[row] = product[j];
            product[j] = tmp;
        }

        LAPACKGenericTest.assertClose(message + ", P L U = A", original, product, LAPACKGenericTest.norm(original));
    }

    private static <N extends Scalar<N>> void doTestGETRS(final Type<N> type, final boolean transpose, final int n, final int nrhs) {

        String message = "GETRS " + type.name + " transpose=" + transpose + " " + n + "x" + nrhs;
        Random random = new Random(n + 31 * nrhs);

        Matrix<N> a = new Matrix<>(type, random, n, n);
        N[][] original = LAPACKGenericTest.copy(type, a);
        int[] ipiv = new int[n];
        TestUtils.assertEquals(message + ", info", 0, GETRF.invoke(n, n, a.data, a.offset, a.ld, ipiv, 0, type.scalar));
        N[] factors = a.data.clone();

        Matrix<N> b = new Matrix<>(type, random, n, nrhs);
        N[][] rhs = LAPACKGenericTest.copy(type, b);
        N[] before = b.data.clone();

        GETRS.invoke(transpose, n, nrhs, a.data, a.offset, a.ld, ipiv, 0, b.data, b.offset, b.ld, type.scalar);

        TestUtils.assertTrue(message + ", A changed", Arrays.equals(factors, a.data));
        LAPACKGenericTest.assertOutsideUnchanged(message, before, b);
        N[][] solution = LAPACKGenericTest.copy(type, b);
        LAPACKGenericTest.assertClose(message, rhs, LAPACKGenericTest.multiply(type, original, transpose, solution, false),
                LAPACKGenericTest.norm(original) * LAPACKGenericTest.norm(solution) + LAPACKGenericTest.norm(rhs));

        if (!transpose && nrhs == 1) {

            Matrix<N> x = new Matrix<>(type, random, n, n);
            N[] beforeX = x.data.clone();

            GETRS.invokeIdentity(n, a.data, a.offset, a.ld, ipiv, 0, x.data, x.offset, x.ld, type.scalar);

            LAPACKGenericTest.assertOutsideUnchanged(message + ", identity", beforeX, x);
            N[][] inverse = LAPACKGenericTest.copy(type, x);
            LAPACKGenericTest.assertClose(message + ", A X = I", LAPACKGenericTest.identity(type, n),
                    LAPACKGenericTest.multiply(type, original, false, inverse, false), LAPACKGenericTest.norm(original) * LAPACKGenericTest.norm(inverse));
        }
    }

    private static <N extends Scalar<N>> void doTestPOTRF(final Type<N> type, final boolean upper, final int n) {

        String message = "POTRF " + type.name + " upper=" + upper + " " + n;
        Random random = new Random(n);

        N[][] original = LAPACKGenericTest.newHermitianPositiveDefinite(type, random, n);
        Matrix<N> a = LAPACKGenericTest.newTriangle(type, random, original, upper);
        N[] before = a.data.clone();

        TestUtils.assertEquals(message + ", info", 0, POTRF.invoke(upper, n, a.data, a.offset, a.ld, type.scalar, type.function));

        LAPACKGenericTest.assertOutsideUnchanged(message, before, a);

        N[][] factor = LAPACKGenericTest.make(type, n, n);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                boolean inside = upper ? i <= j : i >= j;
                if (inside) {
                    factor[i][j] = a.get(i, j);
                } else {
                    factor[i][j] = type.scalar.zero().get();
                    TestUtils.assertTrue(message + ", other triangle changed", before[a.offset + i + j * a.ld] == a.get(i, j));
                }
            }
        }

        N[][] product = upper ? LAPACKGenericTest.multiply(type, factor, true, factor, false) : LAPACKGenericTest.multiply(type, factor, false, factor, true);
        LAPACKGenericTest.assertClose(message, original, product, LAPACKGenericTest.norm(original));

        // Not positive definite at row/column 2 (1-based)
        if (n > 2) {
            Matrix<N> b = LAPACKGenericTest.newTriangle(type, random, original, upper);
            b.set(1, 1, type.scalar.cast(-1.0));
            TestUtils.assertEquals(message + ", not positive definite", 2, POTRF.invoke(upper, n, b.data, b.offset, b.ld, type.scalar, type.function));
        }
    }

    private static <N extends Scalar<N>> void doTestPOTRS(final Type<N> type, final boolean upper, final int n, final int nrhs) {

        String message = "POTRS " + type.name + " upper=" + upper + " " + n + "x" + nrhs;
        Random random = new Random(n + 31 * nrhs);

        N[][] original = LAPACKGenericTest.newHermitianPositiveDefinite(type, random, n);
        Matrix<N> a = LAPACKGenericTest.newTriangle(type, random, original, upper);
        TestUtils.assertEquals(message + ", info", 0, POTRF.invoke(upper, n, a.data, a.offset, a.ld, type.scalar, type.function));
        N[] factor = a.data.clone();

        Matrix<N> b = new Matrix<>(type, random, n, nrhs);
        N[][] rhs = LAPACKGenericTest.copy(type, b);
        N[] before = b.data.clone();

        POTRS.invoke(upper, n, nrhs, a.data, a.offset, a.ld, b.data, b.offset, b.ld, type.scalar);

        TestUtils.assertTrue(message + ", A changed", Arrays.equals(factor, a.data));
        LAPACKGenericTest.assertOutsideUnchanged(message, before, b);
        N[][] solution = LAPACKGenericTest.copy(type, b);
        LAPACKGenericTest.assertClose(message, rhs, LAPACKGenericTest.multiply(type, original, false, solution, false),
                LAPACKGenericTest.norm(original) * LAPACKGenericTest.norm(solution) + LAPACKGenericTest.norm(rhs));

        if (nrhs == 1) {

            Matrix<N> x = new Matrix<>(type, random, n, n);
            N[] beforeX = x.data.clone();

            POTRS.invokeIdentity(upper, n, a.data, a.offset, a.ld, x.data, x.offset, x.ld, type.scalar);

            LAPACKGenericTest.assertOutsideUnchanged(message + ", identity", beforeX, x);
            N[][] inverse = LAPACKGenericTest.copy(type, x);
            LAPACKGenericTest.assertClose(message + ", A X = I", LAPACKGenericTest.identity(type, n),
                    LAPACKGenericTest.multiply(type, original, false, inverse, false), LAPACKGenericTest.norm(original) * LAPACKGenericTest.norm(inverse));
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < i; j++) {
                    TestUtils.assertTrue(message + ", not Hermitian", inverse[j][i].equals(inverse[i][j].conjugate().get()));
                }
            }
        }
    }

    private static <N extends Scalar<N>> void doTestTRSV(final Type<N> type, final boolean upper, final boolean transpose, final boolean unitDiagonal,
            final int n, final int incX) {

        String message = "TRSV " + type.name + " upper=" + upper + " transpose=" + transpose + " unit=" + unitDiagonal + " " + n + " incX=" + incX;
        Random random = new Random(n + 31 * incX);

        Matrix<N> a = LAPACKGenericTest.newTriangular(type, random, n);
        N[] factor = a.data.clone();

        int stride = Math.abs(incX);
        int offsetX = 2;
        N[] x = type.scalar.newArrayInstance(offsetX + (n - 1) * stride + 1 + 2);
        for (int p = 0; p < x.length; p++) {
            x[p] = type.generator.apply(random);
        }
        N[] before = x.clone();
        int start = incX > 0 ? offsetX : offsetX + (n - 1) * stride;

        TRSV.invoke(upper, transpose, unitDiagonal, n, a.data, a.offset, a.ld, x, offsetX, incX, type.scalar);

        TestUtils.assertTrue(message + ", A changed", Arrays.equals(factor, a.data));

        N[][] solution = LAPACKGenericTest.make(type, n, 1);
        N[][] rhs = LAPACKGenericTest.make(type, n, 1);
        boolean[] element = new boolean[x.length];
        for (int i = 0; i < n; i++) {
            int index = start + i * incX;
            element[index] = true;
            solution[i][0] = x[index];
            rhs[i][0] = before[index];
        }
        for (int p = 0; p < x.length; p++) {
            if (!element[p]) {
                TestUtils.assertTrue(message + ", changed outside x at " + p, before[p] == x[p]);
            }
        }

        N[][] triangle = LAPACKGenericTest.triangle(type, a, upper, unitDiagonal);
        LAPACKGenericTest.assertClose(message, rhs, LAPACKGenericTest.multiply(type, triangle, transpose, solution, false),
                LAPACKGenericTest.norm(triangle) * LAPACKGenericTest.norm(solution) + LAPACKGenericTest.norm(rhs));
    }

    private static <N extends Scalar<N>> void doTestTRTRS(final Type<N> type, final boolean upper, final boolean transpose, final boolean unitDiagonal,
            final int n, final int nrhs) {

        String message = "TRTRS " + type.name + " upper=" + upper + " transpose=" + transpose + " unit=" + unitDiagonal + " " + n + "x" + nrhs;
        Random random = new Random(n + 31 * nrhs);

        Matrix<N> a = LAPACKGenericTest.newTriangular(type, random, n);
        N[] factor = a.data.clone();

        Matrix<N> b = new Matrix<>(type, random, n, nrhs);
        N[][] rhs = LAPACKGenericTest.copy(type, b);
        N[] before = b.data.clone();

        TestUtils.assertEquals(message + ", info", 0,
                TRTRS.invoke(upper, transpose, unitDiagonal, n, nrhs, a.data, a.offset, a.ld, b.data, b.offset, b.ld, type.scalar));

        TestUtils.assertTrue(message + ", A changed", Arrays.equals(factor, a.data));
        LAPACKGenericTest.assertOutsideUnchanged(message, before, b);
        N[][] solution = LAPACKGenericTest.copy(type, b);
        N[][] triangle = LAPACKGenericTest.triangle(type, a, upper, unitDiagonal);
        LAPACKGenericTest.assertClose(message, rhs, LAPACKGenericTest.multiply(type, triangle, transpose, solution, false),
                LAPACKGenericTest.norm(triangle) * LAPACKGenericTest.norm(solution) + LAPACKGenericTest.norm(rhs));

        if (!unitDiagonal && n > 1) {
            // Singular: nothing is solved
            a.set(n - 1, n - 1, type.scalar.zero().get());
            Matrix<N> c = new Matrix<>(type, random, n, nrhs);
            N[] beforeC = c.data.clone();
            TestUtils.assertEquals(message + ", singular", n,
                    TRTRS.invoke(upper, transpose, unitDiagonal, n, nrhs, a.data, a.offset, a.ld, c.data, c.offset, c.ld, type.scalar));
            TestUtils.assertTrue(message + ", B changed", Arrays.equals(beforeC, c.data));
        }
    }

    private static <N extends Scalar<N>> N[][] identity(final Type<N> type, final int dim) {
        N[][] retVal = LAPACKGenericTest.make(type, dim, dim);
        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                retVal[i][j] = i == j ? type.scalar.one().get() : type.scalar.zero().get();
            }
        }
        return retVal;
    }

    @SuppressWarnings("unchecked")
    private static <N extends Scalar<N>> N[][] make(final Type<N> type, final int rows, final int columns) {
        return (N[][]) java.lang.reflect.Array.newInstance(type.scalar.zero().get().getClass(), rows, columns);
    }

    /**
     * op(left) * op(right), naively and with the elements of op(left) on the left in each product, where op
     * is the conjugate transpose if specified
     */
    private static <N extends Scalar<N>> N[][] multiply(final Type<N> type, final N[][] left, final boolean conjugateLeft, final N[][] right,
            final boolean conjugateRight) {

        int rows = conjugateLeft ? left[0].length : left.length;
        int inner = conjugateLeft ? left.length : left[0].length;
        int columns = conjugateRight ? right.length : right[0].length;

        N[][] retVal = LAPACKGenericTest.make(type, rows, columns);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                Scalar<N> sum = type.scalar.zero();
                for (int p = 0; p < inner; p++) {
                    N l = conjugateLeft ? left[p][i].conjugate().get() : left[i][p];
                    N r = conjugateRight ? right[j][p].conjugate().get() : right[p][j];
                    sum = sum.add(l.multiply(r));
                }
                retVal[i][j] = sum.get();
            }
        }
        return retVal;
    }

    /**
     * F<sup>H</sup> F + n I, with F random
     */
    private static <N extends Scalar<N>> N[][] newHermitianPositiveDefinite(final Type<N> type, final Random random, final int n) {
        N[][] factor = LAPACKGenericTest.make(type, n, n);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                factor[i][j] = type.generator.apply(random);
            }
        }
        N[][] retVal = LAPACKGenericTest.multiply(type, factor, true, factor, false);
        for (int i = 0; i < n; i++) {
            // Exactly real on the diagonal
            retVal[i][i] = type.scalar.cast(retVal[i][i].doubleValue() + n);
        }
        return retVal;
    }

    /**
     * The upper or lower triangle of the matrix, stored with random elements in the other (strict) triangle
     */
    private static <N extends Scalar<N>> Matrix<N> newTriangle(final Type<N> type, final Random random, final N[][] matrix, final boolean upper) {
        int n = matrix.length;
        Matrix<N> retVal = new Matrix<>(type, random, n, n);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (upper ? i <= j : i >= j) {
                    retVal.set(i, j, matrix[i][j]);
                }
            }
        }
        return retVal;
    }

    /**
     * Random, with a dominant diagonal – the solutions of triangular systems with it stay small
     */
    private static <N extends Scalar<N>> Matrix<N> newTriangular(final Type<N> type, final Random random, final int n) {
        Matrix<N> retVal = new Matrix<>(type, random, n, n);
        for (int i = 0; i < n; i++) {
            retVal.set(i, i, retVal.get(i, i).add(type.scalar.cast(n + 1)).get());
        }
        return retVal;
    }

    /**
     * Frobenius
     */
    private static <N extends Scalar<N>> double norm(final N[][] matrix) {
        double sum = 0.0;
        for (N[] row : matrix) {
            for (N element : row) {
                double value = element.norm();
                sum += value * value;
            }
        }
        return Math.sqrt(sum);
    }

    /**
     * The upper or lower triangle of the stored matrix, with a unit diagonal if specified – what the solvers
     * read
     */
    private static <N extends Scalar<N>> N[][] triangle(final Type<N> type, final Matrix<N> a, final boolean upper, final boolean unitDiagonal) {
        int n = a.rows;
        N[][] retVal = LAPACKGenericTest.make(type, n, n);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j && unitDiagonal) {
                    retVal[i][j] = type.scalar.one().get();
                } else if (upper ? i <= j : i >= j) {
                    retVal[i][j] = a.get(i, j);
                } else {
                    retVal[i][j] = type.scalar.zero().get();
                }
            }
        }
        return retVal;
    }

    @BeforeEach
    public void minimiseAllBranchLimits() {
        TestUtils.minimiseAllBranchLimits();
    }

    @Test
    public void testGEQRF() {
        for (int[] size : SIZES) {
            LAPACKGenericTest.doTestGEQRF(C128, size[0], size[1]);
            LAPACKGenericTest.doTestGEQRF(H256, size[0], size[1]);
        }
    }

    @Test
    public void testGETRF() {
        for (boolean pivoting : BOOLEANS) {
            for (int[] size : SIZES) {
                LAPACKGenericTest.doTestGETRF(C128, pivoting, size[0], size[1]);
                LAPACKGenericTest.doTestGETRF(H256, pivoting, size[0], size[1]);
            }
        }
    }

    @Test
    public void testGETRS() {
        for (boolean transpose : BOOLEANS) {
            for (int[] size : SIZES_SOLVE) {
                LAPACKGenericTest.doTestGETRS(C128, transpose, size[0], size[1]);
                LAPACKGenericTest.doTestGETRS(H256, transpose, size[0], size[1]);
            }
        }
    }

    @Test
    public void testPOTRF() {
        for (boolean upper : BOOLEANS) {
            for (int n : new int[] { 1, 2, 7, 17, 40 }) {
                LAPACKGenericTest.doTestPOTRF(C128, upper, n);
                LAPACKGenericTest.doTestPOTRF(H256, upper, n);
            }
        }
    }

    @Test
    public void testPOTRS() {
        for (boolean upper : BOOLEANS) {
            for (int[] size : SIZES_SOLVE) {
                LAPACKGenericTest.doTestPOTRS(C128, upper, size[0], size[1]);
                LAPACKGenericTest.doTestPOTRS(H256, upper, size[0], size[1]);
            }
        }
    }

    @Test
    public void testTRSV() {
        for (boolean upper : BOOLEANS) {
            for (boolean transpose : BOOLEANS) {
                for (boolean unitDiagonal : BOOLEANS) {
                    for (int n : new int[] { 1, 2, 5, 17 }) {
                        for (int incX : new int[] { 1, -1, 2, -3 }) {
                            LAPACKGenericTest.doTestTRSV(C128, upper, transpose, unitDiagonal, n, incX);
                            LAPACKGenericTest.doTestTRSV(H256, upper, transpose, unitDiagonal, n, incX);
                        }
                    }
                }
            }
        }
    }

    @Test
    public void testTRTRS() {
        for (boolean upper : BOOLEANS) {
            for (boolean transpose : BOOLEANS) {
                for (boolean unitDiagonal : BOOLEANS) {
                    for (int[] size : SIZES_SOLVE) {
                        LAPACKGenericTest.doTestTRTRS(C128, upper, transpose, unitDiagonal, size[0], size[1]);
                        LAPACKGenericTest.doTestTRTRS(H256, upper, transpose, unitDiagonal, size[0], size[1]);
                    }
                }
            }
        }
    }

}

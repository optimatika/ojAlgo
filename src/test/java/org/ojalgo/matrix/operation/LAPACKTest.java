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

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.operation.BLASLevel3Test.Implementation;
import org.ojalgo.matrix.operation.BLASLevel3Test.Storage;

/**
 * The LAPACK operations ({@link GEQRF}, {@link GETRF}, {@link GETRS}, {@link ORGQR}, {@link ORGTR},
 * {@link ORMQR}, {@link POTRF}, {@link POTRS}, {@link SYTRD} and {@link TRTRS}) against their definitions:
 * the factors multiplied back, and any row interchanges undone, give the original matrix; the solution
 * multiplied by the matrix (or its transpose) gives the right hand side; the inverse multiplied by the matrix
 * gives the identity. The orthogonal Q of a QR factorisation, or of a tridiagonal reduction, is formed in the
 * test by applying the reflectors to the identity, and compared with what {@link ORGQR} or {@link ORGTR}
 * generates and with how {@link ORMQR} multiplies. Matrices with offsets and padding as well as matrices
 * filling their arrays exactly. Each case is run through the pure Java implementation and the dispatching
 * {@code invoke}. Elements outside the matrices, and outside the range of the pivots, must be left untouched,
 * and inputs must not change. For the symmetric positive definite matrices, the triangle that is not
 * referenced is filled with NaN.
 */
public class LAPACKTest {

    private static final boolean[] BOOLEANS = { false, true };
    private static final int OFFSET = 2;
    private static final int SENTINEL = -7;
    /**
     * m, n – the largest makes the Java implementation multi-threaded.
     */
    private static final int[][] SIZES = { { 1, 1 }, { 1, 6 }, { 6, 1 }, { 3, 3 }, { 7, 4 }, { 4, 7 }, { 17, 17 }, { 30, 25 }, { 25, 30 }, { 60, 45 },
            { 45, 60 }, { 80, 80 }, { 270, 260 } };
    /**
     * n, for GETRS with B = I – the largest makes the Java implementation multi-threaded.
     */
    private static final int[] SIZES_IDENTITY = { 1, 2, 7, 17, 40, 80 };
    /**
     * m, k, nrhs, for ORMQR – the largest makes the Java implementation multi-threaded.
     */
    private static final int[][] SIZES_ORMQR = { { 1, 1, 1 }, { 5, 0, 3 }, { 3, 3, 1 }, { 6, 4, 4 }, { 17, 17, 1 }, { 17, 10, 5 }, { 40, 40, 1 },
            { 40, 33, 33 }, { 80, 80, 70 } };
    /**
     * n, nrhs, for GETRS – the largest makes the Java implementation multi-threaded.
     */
    private static final int[][] SIZES_SOLVE = { { 1, 1 }, { 3, 1 }, { 6, 4 }, { 17, 1 }, { 17, 5 }, { 40, 1 }, { 40, 33 }, { 80, 70 } };
    /**
     * n, for POTRF – the largest makes the Java implementation multi-threaded.
     */
    private static final int[] SIZES_SPD = { 1, 2, 7, 17, 40, 150 };
    /**
     * n, for SYTRD and ORGTR – the largest makes the Java implementations multi-threaded.
     */
    private static final int[] SIZES_SYTRD = { 1, 2, 3, 7, 17, 40, 150, 340 };

    /**
     * Checks the factors and the row interchanges returned for the matrix that was stored in
     * {@code original}.
     */
    private static void assertFactorisation(final String message, final double[] original, final Storage a, final int m, final int n, final int[] ipiv,
            final int offsetIpiv) {

        int minDim = Math.min(m, n);

        double[] outsideBefore = original.clone();
        double[] outsideAfter = a.data.clone();
        for (int j = 0; j < n; j++) {
            for (int i = 0; i < m; i++) {
                outsideBefore[a.index(i, j)] = 0.0;
                outsideAfter[a.index(i, j)] = 0.0;
            }
        }
        TestUtils.assertTrue(message + " outside the matrix changed", Arrays.equals(outsideBefore, outsideAfter));

        for (int p = 0; p < ipiv.length; p++) {
            int j = p - offsetIpiv;
            if (j >= 0 && j < minDim) {
                TestUtils.assertTrue(message + " ipiv " + j, ipiv[p] >= j + 1 && ipiv[p] <= m);
            } else {
                TestUtils.assertEquals(message + " outside the pivots changed", SENTINEL, ipiv[p]);
            }
        }

        for (int k = 0; k < minDim; k++) {
            for (int i = k + 1; i < m; i++) {
                TestUtils.assertTrue(message + " |L| > 1", Math.abs(a.get(i, k)) <= 1.0);
            }
        }

        double[][] product = new double[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                double sum = 0.0;
                for (int k = 0, limit = Math.min(Math.min(i, j) + 1, minDim); k < limit; k++) {
                    sum += (i == k ? 1.0 : a.get(i, k)) * a.get(k, j);
                }
                product[i][j] = sum;
            }
        }
        for (int j = minDim - 1; j >= 0; j--) {
            int row = ipiv[offsetIpiv + j] - 1;
            double[] tmp = product[row];
            product[row] = product[j];
            product[j] = tmp;
        }

        double[] expected = new double[m * n];
        double[] actual = new double[m * n];
        for (int j = 0; j < n; j++) {
            for (int i = 0; i < m; i++) {
                expected[i + j * m] = original[a.index(i, j)];
                actual[i + j * m] = product[i][j];
            }
        }
        BLASLevel3Test.assertArray(message, expected, actual);
    }

    /**
     * Elements of the array outside the rows-by-columns matrix must be unchanged.
     */
    private static void assertOutsideUnchanged(final String message, final double[] before, final Storage matrix, final int rows, final int columns) {
        double[] outsideBefore = before.clone();
        double[] outsideAfter = matrix.data.clone();
        for (int j = 0; j < columns; j++) {
            for (int i = 0; i < rows; i++) {
                outsideBefore[matrix.index(i, j)] = 0.0;
                outsideAfter[matrix.index(i, j)] = 0.0;
            }
        }
        TestUtils.assertTrue(message + " outside the matrix changed", Arrays.equals(outsideBefore, outsideAfter));
    }

    /**
     * Checks the QR factorisation returned for the matrix that was stored in {@code original}: tau(i) is 0 or
     * between 1 and 2, the reflectors make an orthogonal Q, and Q*R is the original matrix.
     */
    private static void assertQR(final String message, final double[] original, final Storage a, final int m, final int n, final double[] tau,
            final int offsetTau) {

        int minDim = Math.min(m, n);

        LAPACKTest.assertOutsideUnchanged(message, original, a, m, n);

        for (int p = 0; p < tau.length; p++) {
            int i = p - offsetTau;
            if (i >= 0 && i < minDim) {
                TestUtils.assertTrue(message + " tau " + i, tau[p] == 0.0 || tau[p] >= 1.0 && tau[p] <= 2.0);
            } else {
                TestUtils.assertTrue(message + " outside tau changed", tau[p] == SENTINEL);
            }
        }

        double[][] q = LAPACKTest.explicitQ(a, tau, offsetTau, m, minDim);

        double[] identity = new double[m * m];
        double[] product = new double[m * m];
        for (int j = 0; j < m; j++) {
            for (int i = 0; i < m; i++) {
                double sum = 0.0;
                for (int p = 0; p < m; p++) {
                    sum += q[p][i] * q[p][j];
                }
                identity[i + j * m] = i == j ? 1.0 : 0.0;
                product[i + j * m] = sum;
            }
        }
        BLASLevel3Test.assertArray(message + " Q^T*Q", identity, product);

        double[] expected = new double[m * n];
        double[] actual = new double[m * n];
        for (int j = 0; j < n; j++) {
            for (int i = 0; i < m; i++) {
                double sum = 0.0;
                for (int p = 0, limit = Math.min(j + 1, minDim); p < limit; p++) {
                    sum += q[i][p] * a.get(p, j);
                }
                expected[i + j * m] = original[a.index(i, j)];
                actual[i + j * m] = sum;
            }
        }
        BLASLevel3Test.assertArray(message, expected, actual);
    }

    /**
     * The m-by-m orthogonal matrix Q = H(1)*H(2)*...*H(k), from the reflectors below the diagonal of A and
     * tau, as returned by GEQRF: the reflectors applied, in reverse order, to the identity.
     */
    private static double[][] explicitQ(final Storage a, final double[] tau, final int offsetTau, final int m, final int k) {
        return LAPACKTest.explicitQ(a, tau, offsetTau, m, k, 0);
    }

    /**
     * As {@link #explicitQ(Storage, double[], int, int, int)}, with reflector i starting shift rows below the
     * diagonal: v(i + shift) = 1, and the rest of v below it in column i of A. As returned by SYTRD with
     * shift 1.
     */
    private static double[][] explicitQ(final Storage a, final double[] tau, final int offsetTau, final int m, final int k, final int shift) {

        double[][] retVal = new double[m][m];
        for (int i = 0; i < m; i++) {
            retVal[i][i] = 1.0;
        }

        for (int i = k - 1; i >= 0; i--) {
            double beta = tau[offsetTau + i];
            int first = i + shift;
            for (int j = 0; j < m; j++) {
                double sum = retVal[first][j];
                for (int p = first + 1; p < m; p++) {
                    sum += a.get(p, i) * retVal[p][j];
                }
                retVal[first][j] -= beta * sum;
                for (int p = first + 1; p < m; p++) {
                    retVal[p][j] -= beta * sum * a.get(p, i);
                }
            }
        }

        return retVal;
    }

    private static void invokeGEQRF(final Implementation implementation, final int m, final int n, final Storage a, final double[] tau, final int offsetTau) {
        switch (implementation) {
            case JAVA:
                GEQRF.JVM.invoke(m, n, a.data, a.offset, a.ld, tau, offsetTau);
                break;
            default:
                GEQRF.invoke(m, n, a.data, a.offset, a.ld, tau, offsetTau);
                break;
        }
    }

    private static int invokeGETRF(final Implementation implementation, final int m, final int n, final Storage a, final int[] ipiv, final int offsetIpiv) {
        switch (implementation) {
            case JAVA:
                return GETRF.JVM.invoke(m, n, a.data, a.offset, a.ld, ipiv, offsetIpiv);
            default:
                return GETRF.invoke(m, n, a.data, a.offset, a.ld, ipiv, offsetIpiv);
        }
    }

    private static int[] newPivots(final int count, final boolean tight) {
        int[] retVal = new int[tight ? count : OFFSET + count + OFFSET];
        Arrays.fill(retVal, SENTINEL);
        return retVal;
    }

    /**
     * A symmetric positive definite matrix, stored in one triangle; the other (strict) triangle is filled
     * with NaN.
     */
    private static Storage newSPD(final Random random, final int n, final boolean upper, final boolean tight) {

        Storage retVal = new Storage(random, n, n, tight);

        double[][] factor = new double[n][n];
        for (double[] row : factor) {
            for (int j = 0; j < n; j++) {
                row[j] = random.nextDouble() - 0.5;
            }
        }

        for (int j = 0; j < n; j++) {
            for (int i = 0; i < n; i++) {
                if (upper ? i <= j : i >= j) {
                    double sum = i == j ? n : 0.0;
                    for (int p = 0; p < n; p++) {
                        sum += factor[p][i] * factor[p][j];
                    }
                    retVal.set(i, j, sum);
                } else {
                    retVal.set(i, j, Double.NaN);
                }
            }
        }

        return retVal;
    }

    private static double[] newTau(final int count, final boolean tight) {
        double[] retVal = new double[tight ? count : OFFSET + count + OFFSET];
        Arrays.fill(retVal, SENTINEL);
        return retVal;
    }

    /**
     * A triangular matrix with a dominant diagonal; the other (strict) triangle, and the diagonal if unit,
     * are filled with NaN.
     */
    private static Storage newTriangular(final Random random, final int n, final boolean upper, final boolean unitDiagonal, final boolean tight) {
        Storage retVal = new Storage(random, n, n, tight);
        for (int j = 0; j < n; j++) {
            for (int i = 0; i < n; i++) {
                if (i == j) {
                    retVal.set(i, j, unitDiagonal ? Double.NaN : 2.0 + random.nextDouble());
                } else if (upper ? i > j : i < j) {
                    retVal.set(i, j, Double.NaN);
                } else {
                    retVal.set(i, j, retVal.get(i, j) / n);
                }
            }
        }
        return retVal;
    }

    /**
     * Element of the full symmetric matrix stored in one triangle of data.
     */
    private static double symmetric(final double[] data, final Storage matrix, final boolean upper, final int row, final int col) {
        boolean stored = upper ? row <= col : row >= col;
        return stored ? data[matrix.index(row, col)] : data[matrix.index(col, row)];
    }

    static void doTestGEQRF(final Implementation implementation, final boolean tight, final int m, final int n) {

        Storage a = new Storage(new Random(m + 31 * n), m, n, tight);
        double[] original = a.data.clone();
        int offsetTau = tight ? 0 : OFFSET;
        double[] tau = LAPACKTest.newTau(Math.min(m, n), tight);

        LAPACKTest.invokeGEQRF(implementation, m, n, a, tau, offsetTau);

        LAPACKTest.assertQR("GEQRF " + implementation + " tight=" + tight + " " + m + "x" + n, original, a, m, n, tau, offsetTau);
    }

    static void doTestGETRF(final Implementation implementation, final boolean tight, final int m, final int n) {

        Storage a = new Storage(new Random(m + 31 * n), m, n, tight);
        double[] original = a.data.clone();
        int offsetIpiv = tight ? 0 : OFFSET;
        int[] ipiv = LAPACKTest.newPivots(Math.min(m, n), tight);

        int info = LAPACKTest.invokeGETRF(implementation, m, n, a, ipiv, offsetIpiv);

        String message = "GETRF " + implementation + " tight=" + tight + " " + m + "x" + n;
        TestUtils.assertEquals(message + " info", 0, info);
        LAPACKTest.assertFactorisation(message, original, a, m, n, ipiv, offsetIpiv);
    }

    static void doTestGETRS(final Implementation implementation, final boolean tight, final boolean transpose, final int n, final int nrhs) {

        Random random = new Random(n + 31 * nrhs);

        Storage a = new Storage(random, n, n, tight);
        double[] original = a.data.clone();
        int offsetIpiv = tight ? 0 : OFFSET;
        int[] ipiv = LAPACKTest.newPivots(n, tight);
        GETRF.JVM.invoke(n, n, a.data, a.offset, a.ld, ipiv, offsetIpiv);
        double[] factors = a.data.clone();
        int[] pivots = ipiv.clone();

        Storage b = new Storage(random, n, nrhs, tight);
        double[] rhs = b.data.clone();

        switch (implementation) {
            case JAVA:
                GETRS.JVM.invoke(transpose, n, nrhs, a.data, a.offset, a.ld, ipiv, offsetIpiv, b.data, b.offset, b.ld);
                break;
            default:
                GETRS.invoke(transpose, n, nrhs, a.data, a.offset, a.ld, ipiv, offsetIpiv, b.data, b.offset, b.ld);
                break;
        }

        String message = "GETRS " + implementation + " tight=" + tight + " trans=" + transpose + " " + n + "x" + nrhs;
        BLASLevel3Test.assertUnchanged(message, factors, a.data);
        TestUtils.assertEquals(message + " ipiv changed", pivots, ipiv);

        double[] actual = b.data.clone();
        for (int j = 0; j < nrhs; j++) {
            for (int i = 0; i < n; i++) {
                double sum = 0.0;
                for (int k = 0; k < n; k++) {
                    sum += (transpose ? original[a.index(k, i)] : original[a.index(i, k)]) * b.get(k, j);
                }
                actual[b.index(i, j)] = sum;
            }
        }
        BLASLevel3Test.assertArray(message, rhs, actual);
    }

    static void doTestGETRSIdentity(final Implementation implementation, final boolean tight, final int n) {

        Random random = new Random(n);

        Storage a = new Storage(random, n, n, tight);
        double[] original = a.data.clone();
        int offsetIpiv = tight ? 0 : OFFSET;
        int[] ipiv = LAPACKTest.newPivots(n, tight);
        GETRF.JVM.invoke(n, n, a.data, a.offset, a.ld, ipiv, offsetIpiv);
        double[] factors = a.data.clone();
        int[] pivots = ipiv.clone();

        // B need not be set on entry
        Storage x = new Storage(random, n, n, tight);
        double[] before = x.data.clone();

        if (implementation == Implementation.JAVA) {
            GETRS.JVM.invokeIdentity(n, a.data, a.offset, a.ld, ipiv, offsetIpiv, x.data, x.offset, x.ld);
        } else {
            GETRS.invokeIdentity(n, a.data, a.offset, a.ld, ipiv, offsetIpiv, x.data, x.offset, x.ld);
        }

        String message = "GETRS identity " + implementation + " tight=" + tight + " " + n;
        BLASLevel3Test.assertUnchanged(message, factors, a.data);
        TestUtils.assertEquals(message + " ipiv changed", pivots, ipiv);

        double[] expected = before.clone();
        double[] actual = x.data.clone();
        for (int j = 0; j < n; j++) {
            for (int i = 0; i < n; i++) {
                double sum = 0.0;
                for (int k = 0; k < n; k++) {
                    sum += original[a.index(i, k)] * x.get(k, j);
                }
                expected[x.index(i, j)] = i == j ? 1.0 : 0.0;
                actual[x.index(i, j)] = sum;
            }
        }
        BLASLevel3Test.assertArray(message, expected, actual);
    }

    /**
     * Q, m-by-n, from k reflectors – the factorisation of an m-by-k matrix, stored in the first k columns of
     * an m-by-n one.
     */
    static void doTestORGQR(final Implementation implementation, final boolean tight, final int m, final int n, final int k) {

        Random random = new Random(m + 31 * n + 961 * k);

        Storage a = new Storage(random, m, n, tight);
        int offsetTau = tight ? 0 : OFFSET;
        double[] tau = LAPACKTest.newTau(k, tight);
        GEQRF.JVM.invoke(m, k, a.data, a.offset, a.ld, tau, offsetTau);
        double[] factors = a.data.clone();
        double[] reflectors = tau.clone();
        double[][] q = LAPACKTest.explicitQ(a, tau, offsetTau, m, k);

        switch (implementation) {
            case JAVA:
                ORGQR.JVM.invoke(m, n, k, a.data, a.offset, a.ld, tau, offsetTau);
                break;
            default:
                ORGQR.invoke(m, n, k, a.data, a.offset, a.ld, tau, offsetTau);
                break;
        }

        String message = "ORGQR " + implementation + " tight=" + tight + " " + m + "x" + n + " k=" + k;
        BLASLevel3Test.assertUnchanged(message, reflectors, tau);
        LAPACKTest.assertOutsideUnchanged(message, factors, a, m, n);

        double[] expected = new double[m * n];
        double[] actual = new double[m * n];
        for (int j = 0; j < n; j++) {
            for (int i = 0; i < m; i++) {
                expected[i + j * m] = q[i][j];
                actual[i + j * m] = a.get(i, j);
            }
        }
        BLASLevel3Test.assertArray(message, expected, actual);
    }

    static void doTestORMQR(final Implementation implementation, final boolean tight, final boolean transpose, final int m, final int k, final int nrhs) {

        Random random = new Random(m + 31 * k + 961 * nrhs);

        Storage a = new Storage(random, m, k, tight);
        int offsetTau = tight ? 0 : OFFSET;
        double[] tau = LAPACKTest.newTau(k, tight);
        GEQRF.JVM.invoke(m, k, a.data, a.offset, a.ld, tau, offsetTau);
        double[] factors = a.data.clone();
        double[] reflectors = tau.clone();
        double[][] q = LAPACKTest.explicitQ(a, tau, offsetTau, m, k);

        Storage c = new Storage(random, m, nrhs, tight);
        double[] before = c.data.clone();

        if (implementation == Implementation.JAVA) {
            ORMQR.JVM.invoke(transpose, m, nrhs, k, a.data, a.offset, a.ld, tau, offsetTau, c.data, c.offset, c.ld);
        } else {
            ORMQR.invoke(transpose, m, nrhs, k, a.data, a.offset, a.ld, tau, offsetTau, c.data, c.offset, c.ld);
        }

        String message = "ORMQR " + implementation + " tight=" + tight + " trans=" + transpose + " " + m + "x" + nrhs + " k=" + k;
        BLASLevel3Test.assertUnchanged(message, factors, a.data);
        BLASLevel3Test.assertUnchanged(message, reflectors, tau);
        LAPACKTest.assertOutsideUnchanged(message, before, c, m, nrhs);

        double[] expected = new double[m * nrhs];
        double[] actual = new double[m * nrhs];
        for (int j = 0; j < nrhs; j++) {
            for (int i = 0; i < m; i++) {
                double sum = 0.0;
                for (int p = 0; p < m; p++) {
                    sum += (transpose ? q[p][i] : q[i][p]) * before[c.index(p, j)];
                }
                expected[i + j * m] = sum;
                actual[i + j * m] = c.get(i, j);
            }
        }
        BLASLevel3Test.assertArray(message, expected, actual);
    }

    /**
     * Q from the reflectors returned by SYTRD (using the lower triangle).
     */
    static void doTestORGTR(final Implementation implementation, final boolean tight, final int n) {

        Storage a = LAPACKTest.newSPD(new Random(n), n, false, tight);
        int offset = tight ? 0 : OFFSET;
        double[] d = new double[offset + n + offset];
        double[] e = new double[offset + n + offset];
        double[] tau = LAPACKTest.newTau(Math.max(0, n - 1), tight);
        SYTRD.JVM.invoke(n, a.data, a.offset, a.ld, d, offset, e, offset, tau, offset);
        double[] reflectors = a.data.clone();
        double[] taus = tau.clone();
        double[][] q = LAPACKTest.explicitQ(a, tau, offset, n, n - 1, 1);

        switch (implementation) {
            case JAVA:
                ORGTR.JVM.invoke(n, a.data, a.offset, a.ld, tau, offset);
                break;
            default:
                ORGTR.invoke(n, a.data, a.offset, a.ld, tau, offset);
                break;
        }

        String message = "ORGTR " + implementation + " tight=" + tight + " " + n;
        BLASLevel3Test.assertUnchanged(message, taus, tau);
        LAPACKTest.assertOutsideUnchanged(message, reflectors, a, n, n);

        double[] expected = new double[n * n];
        double[] actual = new double[n * n];
        for (int j = 0; j < n; j++) {
            for (int i = 0; i < n; i++) {
                expected[i + j * n] = q[i][j];
                actual[i + j * n] = a.get(i, j);
            }
        }
        BLASLevel3Test.assertArray(message, expected, actual);
    }

    static void doTestPOTRF(final Implementation implementation, final boolean tight, final boolean upper, final int n) {

        Storage a = LAPACKTest.newSPD(new Random(n), n, upper, tight);
        double[] original = a.data.clone();

        int info;
        switch (implementation) {
            case JAVA:
                info = POTRF.JVM.invoke(upper, n, a.data, a.offset, a.ld);
                break;
            default:
                info = POTRF.invoke(upper, n, a.data, a.offset, a.ld);
                break;
        }

        String message = "POTRF " + implementation + " tight=" + tight + " upper=" + upper + " " + n;
        TestUtils.assertEquals(message + " info", 0, info);

        // L*L^T (or U^T*U) in the referenced triangle, everything else untouched
        double[] expected = original.clone();
        double[] actual = a.data.clone();
        for (int j = 0; j < n; j++) {
            for (int i = 0; i < n; i++) {
                if (upper ? i <= j : i >= j) {
                    double sum = 0.0;
                    for (int k = 0, limit = Math.min(i, j); k <= limit; k++) {
                        sum += upper ? a.get(k, i) * a.get(k, j) : a.get(i, k) * a.get(j, k);
                    }
                    actual[a.index(i, j)] = sum;
                }
            }
        }
        BLASLevel3Test.assertArray(message, expected, actual);
    }

    static void doTestPOTRS(final Implementation implementation, final boolean tight, final boolean upper, final int n, final int nrhs) {

        Random random = new Random(n + 31 * nrhs);

        Storage a = LAPACKTest.newSPD(random, n, upper, tight);
        double[] original = a.data.clone();
        POTRF.JVM.invoke(upper, n, a.data, a.offset, a.ld);
        double[] factor = a.data.clone();

        Storage b = new Storage(random, n, nrhs, tight);
        double[] rhs = b.data.clone();

        switch (implementation) {
            case JAVA:
                POTRS.JVM.invoke(upper, n, nrhs, a.data, a.offset, a.ld, b.data, b.offset, b.ld);
                break;
            default:
                POTRS.invoke(upper, n, nrhs, a.data, a.offset, a.ld, b.data, b.offset, b.ld);
                break;
        }

        String message = "POTRS " + implementation + " tight=" + tight + " upper=" + upper + " " + n + "x" + nrhs;
        BLASLevel3Test.assertUnchanged(message, factor, a.data);

        double[] actual = b.data.clone();
        for (int j = 0; j < nrhs; j++) {
            for (int i = 0; i < n; i++) {
                double sum = 0.0;
                for (int k = 0; k < n; k++) {
                    sum += LAPACKTest.symmetric(original, a, upper, i, k) * b.get(k, j);
                }
                actual[b.index(i, j)] = sum;
            }
        }
        BLASLevel3Test.assertArray(message, rhs, actual);
    }

    static void doTestPOTRSIdentity(final Implementation implementation, final boolean tight, final boolean upper, final int n) {

        Random random = new Random(n);

        Storage a = LAPACKTest.newSPD(random, n, upper, tight);
        double[] original = a.data.clone();
        POTRF.JVM.invoke(upper, n, a.data, a.offset, a.ld);
        double[] factor = a.data.clone();

        // B need not be set on entry
        Storage x = new Storage(random, n, n, tight);
        double[] before = x.data.clone();

        if (implementation == Implementation.JAVA) {
            POTRS.JVM.invokeIdentity(upper, n, a.data, a.offset, a.ld, x.data, x.offset, x.ld);
        } else {
            POTRS.invokeIdentity(upper, n, a.data, a.offset, a.ld, x.data, x.offset, x.ld);
        }

        String message = "POTRS identity " + implementation + " tight=" + tight + " upper=" + upper + " " + n;
        BLASLevel3Test.assertUnchanged(message, factor, a.data);

        double[] expected = before.clone();
        double[] actual = x.data.clone();
        for (int j = 0; j < n; j++) {
            for (int i = 0; i < n; i++) {
                TestUtils.assertTrue(message + " not symmetric", Double.compare(x.get(i, j), x.get(j, i)) == 0);
                double sum = 0.0;
                for (int k = 0; k < n; k++) {
                    sum += LAPACKTest.symmetric(original, a, upper, i, k) * x.get(k, j);
                }
                expected[x.index(i, j)] = i == j ? 1.0 : 0.0;
                actual[x.index(i, j)] = sum;
            }
        }
        BLASLevel3Test.assertArray(message, expected, actual);
    }

    /**
     * The reflectors make an orthogonal Q, and Q*T*Q<sup>T</sup> is the original matrix. Only the lower
     * triangle is read and written; the upper is NaN.
     */
    static void doTestSYTRD(final Implementation implementation, final boolean tight, final int n) {

        Storage a = LAPACKTest.newSPD(new Random(n), n, false, tight);
        double[] original = a.data.clone();
        int offset = tight ? 0 : OFFSET;
        double[] d = LAPACKTest.newTau(n, tight);
        double[] e = LAPACKTest.newTau(Math.max(0, n - 1), tight);
        double[] tau = LAPACKTest.newTau(Math.max(0, n - 1), tight);

        switch (implementation) {
            case JAVA:
                SYTRD.JVM.invoke(n, a.data, a.offset, a.ld, d, offset, e, offset, tau, offset);
                break;
            default:
                SYTRD.invoke(n, a.data, a.offset, a.ld, d, offset, e, offset, tau, offset);
                break;
        }

        String message = "SYTRD " + implementation + " tight=" + tight + " " + n;

        double[] outsideBefore = original.clone();
        double[] outsideAfter = a.data.clone();
        for (int j = 0; j < n; j++) {
            for (int i = j; i < n; i++) {
                outsideBefore[a.index(i, j)] = 0.0;
                outsideAfter[a.index(i, j)] = 0.0;
            }
        }
        TestUtils.assertTrue(message + " outside the lower triangle changed", Arrays.equals(outsideBefore, outsideAfter));

        for (int p = 0; p < d.length; p++) {
            int i = p - offset;
            if (i >= 0 && i < n) {
                TestUtils.assertTrue(message + " d " + i, d[p] == a.get(i, i));
            } else {
                TestUtils.assertTrue(message + " outside d changed", d[p] == SENTINEL);
            }
        }
        for (int p = 0; p < tau.length; p++) {
            int i = p - offset;
            if (i >= 0 && i < n - 1) {
                TestUtils.assertTrue(message + " e " + i, e[p] == a.get(i + 1, i));
                TestUtils.assertTrue(message + " tau " + i, tau[p] == 0.0 || tau[p] >= 1.0 && tau[p] <= 2.0);
            } else {
                TestUtils.assertTrue(message + " outside e changed", e[p] == SENTINEL);
                TestUtils.assertTrue(message + " outside tau changed", tau[p] == SENTINEL);
            }
        }

        double[][] q = LAPACKTest.explicitQ(a, tau, offset, n, n - 1, 1);

        double[] identity = new double[n * n];
        double[] product = new double[n * n];
        double[] expected = new double[n * n];
        double[] actual = new double[n * n];
        for (int j = 0; j < n; j++) {
            for (int i = 0; i < n; i++) {
                double qq = 0.0;
                double qtq = 0.0;
                for (int p = 0; p < n; p++) {
                    qq += q[p][i] * q[p][j];
                    // (T*Q^T)(p,j) = sum over r of T(p,r)*Q(j,r), T tridiagonal
                    double tq = 0.0;
                    for (int r = Math.max(0, p - 1); r <= Math.min(n - 1, p + 1); r++) {
                        double t = r == p ? d[offset + p] : e[offset + Math.min(p, r)];
                        tq += t * q[j][r];
                    }
                    qtq += q[i][p] * tq;
                }
                identity[i + j * n] = i == j ? 1.0 : 0.0;
                product[i + j * n] = qq;
                expected[i + j * n] = LAPACKTest.symmetric(original, a, false, i, j);
                actual[i + j * n] = qtq;
            }
        }
        BLASLevel3Test.assertArray(message + " Q^T*Q", identity, product);
        BLASLevel3Test.assertArray(message, expected, actual);
    }

    static void doTestTRTRS(final Implementation implementation, final boolean tight, final boolean upper, final boolean transpose, final boolean unit,
            final int n, final int nrhs) {

        Random random = new Random(n + 31 * nrhs);

        Storage a = LAPACKTest.newTriangular(random, n, upper, unit, tight);
        double[] matrix = a.data.clone();

        Storage b = new Storage(random, n, nrhs, tight);
        double[] rhs = b.data.clone();

        int info;
        switch (implementation) {
            case JAVA:
                info = TRTRS.JVM.invoke(upper, transpose, unit, n, nrhs, a.data, a.offset, a.ld, b.data, b.offset, b.ld);
                break;
            default:
                info = TRTRS.invoke(upper, transpose, unit, n, nrhs, a.data, a.offset, a.ld, b.data, b.offset, b.ld);
                break;
        }

        String message = "TRTRS " + implementation + " tight=" + tight + " upper=" + upper + " trans=" + transpose + " unit=" + unit + " " + n + "x" + nrhs;
        TestUtils.assertEquals(message + " info", 0, info);
        BLASLevel3Test.assertUnchanged(message, matrix, a.data);

        double[] actual = b.data.clone();
        for (int j = 0; j < nrhs; j++) {
            for (int i = 0; i < n; i++) {
                double sum = 0.0;
                for (int p = 0; p < n; p++) {
                    int row = transpose ? p : i;
                    int col = transpose ? i : p;
                    double element = row == col && unit ? 1.0 : (upper ? row <= col : row >= col) ? a.get(row, col) : 0.0;
                    sum += element * b.get(p, j);
                }
                actual[b.index(i, j)] = sum;
            }
        }
        BLASLevel3Test.assertArray(message, rhs, actual);
    }

    @Test
    public void testGEQRF() {
        for (Implementation implementation : Implementation.values()) {
            for (boolean tight : BOOLEANS) {
                for (int[] size : SIZES) {
                    LAPACKTest.doTestGEQRF(implementation, tight, size[0], size[1]);
                }
            }
        }
    }

    @Test
    public void testGEQRFArgumentChecks() {

        double[] data = new double[16];
        double[] tau = new double[4];

        TestUtils.assertThrows(IllegalArgumentException.class, () -> GEQRF.invoke(-1, 2, data, 0, 1, tau, 0));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> GEQRF.invoke(2, -1, data, 0, 2, tau, 0));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> GEQRF.invoke(4, 4, data, 0, 3, tau, 0));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> GEQRF.invoke(0, 4, data, 0, 0, tau, 0));

        // Nothing to factorise, nothing is accessed
        GEQRF.invoke(0, 4, new double[0], 0, 1, new double[0], 0);
        GEQRF.invoke(4, 0, new double[0], 0, 4, new double[0], 0);
    }

    /**
     * A zero column gives tau = 0 (no reflection), and the factorisation is completed.
     */
    @Test
    public void testGEQRFZeroColumn() {
        for (Implementation implementation : Implementation.values()) {
            for (int[] size : new int[][] { { 12, 12 }, { 60, 50 } }) {

                int m = size[0];
                int n = size[1];
                String message = "GEQRF zero column " + implementation + " " + m + "x" + n;

                Storage a = new Storage(new Random(m), m, n, false);
                for (int i = 0; i < m; i++) {
                    a.set(i, 3, 0.0);
                }
                double[] original = a.data.clone();
                double[] tau = LAPACKTest.newTau(Math.min(m, n), false);

                LAPACKTest.invokeGEQRF(implementation, m, n, a, tau, OFFSET);

                TestUtils.assertTrue(message + " tau", tau[OFFSET + 3] == 0.0);
                LAPACKTest.assertQR(message, original, a, m, n, tau, OFFSET);
            }
        }
    }

    @Test
    public void testGETRF() {
        for (Implementation implementation : Implementation.values()) {
            for (boolean tight : BOOLEANS) {
                for (int[] size : SIZES) {
                    LAPACKTest.doTestGETRF(implementation, tight, size[0], size[1]);
                }
            }
        }
    }

    @Test
    public void testGETRFArgumentChecks() {

        double[] data = new double[16];
        int[] ipiv = new int[4];

        TestUtils.assertThrows(IllegalArgumentException.class, () -> GETRF.invoke(-1, 2, data, 0, 1, ipiv, 0));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> GETRF.invoke(2, -1, data, 0, 2, ipiv, 0));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> GETRF.invoke(4, 4, data, 0, 3, ipiv, 0));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> GETRF.invoke(0, 4, data, 0, 0, ipiv, 0));

        // Nothing to factorise, nothing is accessed
        TestUtils.assertEquals(0, GETRF.invoke(0, 4, new double[0], 0, 1, new int[0], 0));
        TestUtils.assertEquals(0, GETRF.invoke(4, 0, new double[0], 0, 4, new int[0], 0));
    }

    /**
     * An exactly zero pivot is reported (1-based), and the factorisation is completed.
     */
    @Test
    public void testGETRFSingular() {
        for (Implementation implementation : Implementation.values()) {
            for (int[] size : new int[][] { { 12, 12 }, { 60, 50 } }) {

                int m = size[0];
                int n = size[1];
                String message = "GETRF singular " + implementation + " " + m + "x" + n;

                Storage a = new Storage(new Random(m), m, n, false);
                for (int i = 0; i < m; i++) {
                    a.set(i, 3, 0.0);
                }
                double[] original = a.data.clone();
                int[] ipiv = LAPACKTest.newPivots(Math.min(m, n), false);

                TestUtils.assertEquals(message + " info", 4, LAPACKTest.invokeGETRF(implementation, m, n, a, ipiv, OFFSET));
                LAPACKTest.assertFactorisation(message, original, a, m, n, ipiv, OFFSET);

                Storage zero = new Storage(new Random(n), m, n, false);
                for (int j = 0; j < n; j++) {
                    for (int i = 0; i < m; i++) {
                        zero.set(i, j, 0.0);
                    }
                }
                original = zero.data.clone();
                ipiv = LAPACKTest.newPivots(Math.min(m, n), false);

                TestUtils.assertEquals(message + " zero info", 1, LAPACKTest.invokeGETRF(implementation, m, n, zero, ipiv, OFFSET));
                LAPACKTest.assertFactorisation(message + " zero", original, zero, m, n, ipiv, OFFSET);
            }
        }
    }

    /**
     * Without row interchanges: diagonally dominant matrices, and one with an exactly zero column, reported
     * (1-based) as an exactly zero U(i,i).
     */
    @Test
    public void testGETRFWithoutPivoting() {
        for (Implementation implementation : new Implementation[] { Implementation.DISPATCH, Implementation.JAVA }) {
            for (boolean tight : BOOLEANS) {
                for (int[] size : SIZES) {

                    int m = size[0];
                    int n = size[1];
                    int minDim = Math.min(m, n);
                    String message = "GETRF without pivoting " + implementation + " tight=" + tight + " " + m + "x" + n;

                    for (boolean singular : BOOLEANS) {

                        Storage a = new Storage(new Random(m + 31 * n), m, n, tight);
                        for (int i = 0; i < minDim; i++) {
                            a.set(i, i, a.get(i, i) + m + n);
                        }
                        if (singular && n > 3) {
                            for (int i = 0; i < m; i++) {
                                a.set(i, 3, 0.0);
                            }
                        }
                        double[] original = a.data.clone();

                        int info = implementation == Implementation.JAVA ? GETRF.JVM.invokeWithoutPivoting(m, n, a.data, a.offset, a.ld)
                                : GETRF.invokeWithoutPivoting(m, n, a.data, a.offset, a.ld);

                        TestUtils.assertEquals(message + " info", singular && n > 3 && m > 3 ? 4 : 0, info);

                        int[] ipiv = LAPACKTest.newPivots(minDim, true);
                        for (int j = 0; j < minDim; j++) {
                            ipiv[j] = j + 1;
                        }
                        LAPACKTest.assertFactorisation(message + " singular=" + singular, original, a, m, n, ipiv, 0);
                    }
                }
            }
        }
    }

    @Test
    public void testGETRS() {
        for (Implementation implementation : Implementation.values()) {
            for (boolean tight : BOOLEANS) {
                for (boolean transpose : BOOLEANS) {
                    for (int[] size : SIZES_SOLVE) {
                        LAPACKTest.doTestGETRS(implementation, tight, transpose, size[0], size[1]);
                    }
                }
            }
        }
    }

    @Test
    public void testGETRSArgumentChecks() {

        double[] data = new double[16];
        int[] ipiv = { 1, 2, 3, 4 };

        TestUtils.assertThrows(IllegalArgumentException.class, () -> GETRS.invoke(false, -1, 1, data, 0, 1, ipiv, 0, data, 0, 1));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> GETRS.invoke(false, 4, -1, data, 0, 4, ipiv, 0, data, 0, 4));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> GETRS.invoke(false, 4, 1, data, 0, 3, ipiv, 0, data, 0, 4));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> GETRS.invoke(false, 4, 1, data, 0, 4, ipiv, 0, data, 0, 3));

        TestUtils.assertThrows(IllegalArgumentException.class, () -> GETRS.invokeIdentity(-1, data, 0, 1, ipiv, 0, data, 0, 1));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> GETRS.invokeIdentity(4, data, 0, 3, ipiv, 0, data, 0, 4));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> GETRS.invokeIdentity(4, data, 0, 4, ipiv, 0, data, 0, 3));

        // Nothing to solve, nothing is accessed
        GETRS.invoke(false, 0, 3, new double[0], 0, 1, new int[0], 0, new double[0], 0, 1);
        GETRS.invoke(true, 4, 0, new double[0], 0, 4, new int[0], 0, new double[0], 0, 4);
        GETRS.invokeIdentity(0, new double[0], 0, 1, new int[0], 0, new double[0], 0, 1);
    }

    /**
     * The inverse, written to a separate matrix.
     */
    @Test
    public void testGETRSIdentity() {
        for (Implementation implementation : Implementation.values()) {
            for (boolean tight : BOOLEANS) {
                for (int n : SIZES_IDENTITY) {
                    LAPACKTest.doTestGETRSIdentity(implementation, tight, n);
                }
            }
        }
    }

    /**
     * Economy size (n = k = min(m,n) of the factorisation) and full size (n = m) Q.
     */
    @Test
    public void testORGQR() {
        for (Implementation implementation : Implementation.values()) {
            for (boolean tight : BOOLEANS) {
                for (int[] size : SIZES) {
                    int m = size[0];
                    int k = Math.min(m, size[1]);
                    LAPACKTest.doTestORGQR(implementation, tight, m, k, k);
                    LAPACKTest.doTestORGQR(implementation, tight, m, m, k);
                }
                LAPACKTest.doTestORGQR(implementation, tight, 5, 3, 0);
            }
        }
    }

    @Test
    public void testORGQRArgumentChecks() {

        double[] data = new double[16];
        double[] tau = new double[4];

        TestUtils.assertThrows(IllegalArgumentException.class, () -> ORGQR.invoke(-1, 0, 0, data, 0, 1, tau, 0));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> ORGQR.invoke(3, 4, 2, data, 0, 3, tau, 0));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> ORGQR.invoke(4, 3, 4, data, 0, 4, tau, 0));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> ORGQR.invoke(4, 3, -1, data, 0, 4, tau, 0));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> ORGQR.invoke(4, 4, 4, data, 0, 3, tau, 0));

        // Nothing to generate, nothing is accessed
        ORGQR.invoke(4, 0, 0, new double[0], 0, 4, new double[0], 0);
        ORGQR.invoke(0, 0, 0, new double[0], 0, 1, new double[0], 0);
    }

    @Test
    public void testORMQR() {
        for (Implementation implementation : Implementation.values()) {
            for (boolean tight : BOOLEANS) {
                for (boolean transpose : BOOLEANS) {
                    for (int[] size : SIZES_ORMQR) {
                        LAPACKTest.doTestORMQR(implementation, tight, transpose, size[0], size[1], size[2]);
                    }
                }
            }
        }
    }

    @Test
    public void testORMQRArgumentChecks() {

        double[] data = new double[16];
        double[] tau = new double[4];

        TestUtils.assertThrows(IllegalArgumentException.class, () -> ORMQR.invoke(false, -1, 1, 0, data, 0, 1, tau, 0, data, 0, 1));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> ORMQR.invoke(false, 4, -1, 4, data, 0, 4, tau, 0, data, 0, 4));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> ORMQR.invoke(false, 4, 1, 5, data, 0, 4, tau, 0, data, 0, 4));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> ORMQR.invoke(false, 4, 1, 4, data, 0, 3, tau, 0, data, 0, 4));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> ORMQR.invoke(false, 4, 1, 4, data, 0, 4, tau, 0, data, 0, 3));

        // Nothing to multiply, nothing is accessed
        ORMQR.invoke(true, 0, 3, 0, new double[0], 0, 1, new double[0], 0, new double[0], 0, 1);
        ORMQR.invoke(true, 4, 0, 4, new double[0], 0, 4, new double[0], 0, new double[0], 0, 4);
        ORMQR.invoke(true, 4, 3, 0, new double[0], 0, 4, new double[0], 0, new double[0], 0, 4);
    }

    @Test
    public void testORGTR() {
        for (Implementation implementation : Implementation.values()) {
            for (boolean tight : BOOLEANS) {
                for (int n : SIZES_SYTRD) {
                    LAPACKTest.doTestORGTR(implementation, tight, n);
                }
            }
        }
    }

    @Test
    public void testORGTRArgumentChecks() {

        double[] data = new double[16];
        double[] tau = new double[3];

        TestUtils.assertThrows(IllegalArgumentException.class, () -> ORGTR.invoke(-1, data, 0, 1, tau, 0));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> ORGTR.invoke(4, data, 0, 3, tau, 0));

        // Nothing to generate, nothing is accessed
        ORGTR.invoke(0, new double[0], 0, 1, new double[0], 0);
    }

    @Test
    public void testPOTRF() {
        for (Implementation implementation : Implementation.values()) {
            for (boolean tight : BOOLEANS) {
                for (boolean upper : BOOLEANS) {
                    for (int n : SIZES_SPD) {
                        LAPACKTest.doTestPOTRF(implementation, tight, upper, n);
                    }
                }
            }
        }
    }

    @Test
    public void testPOTRFArgumentChecks() {

        double[] data = new double[16];

        TestUtils.assertThrows(IllegalArgumentException.class, () -> POTRF.invoke(false, -1, data, 0, 1));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> POTRF.invoke(true, 4, data, 0, 3));

        // Nothing to factorise, nothing is accessed
        TestUtils.assertEquals(0, POTRF.invoke(false, 0, new double[0], 0, 1));
    }

    /**
     * A leading minor that is not positive definite is reported, and the factorisation stopped there.
     */
    @Test
    public void testPOTRFNotPositiveDefinite() {
        for (Implementation implementation : Implementation.values()) {
            for (boolean upper : BOOLEANS) {
                for (int n : new int[] { 12, 60 }) {

                    Storage a = LAPACKTest.newSPD(new Random(n), n, upper, false);
                    a.set(3, 3, -n * n);

                    int info;
                    switch (implementation) {
                        case JAVA:
                            info = POTRF.JVM.invoke(upper, n, a.data, a.offset, a.ld);
                            break;
                        default:
                            info = POTRF.invoke(upper, n, a.data, a.offset, a.ld);
                            break;
                    }

                    TestUtils.assertEquals("POTRF " + implementation + " upper=" + upper + " " + n, 4, info);
                }
            }
        }
    }

    @Test
    public void testPOTRS() {
        for (Implementation implementation : Implementation.values()) {
            for (boolean tight : BOOLEANS) {
                for (boolean upper : BOOLEANS) {
                    for (int[] size : SIZES_SOLVE) {
                        LAPACKTest.doTestPOTRS(implementation, tight, upper, size[0], size[1]);
                    }
                }
            }
        }
    }

    @Test
    public void testPOTRSArgumentChecks() {

        double[] data = new double[16];

        TestUtils.assertThrows(IllegalArgumentException.class, () -> POTRS.invoke(false, -1, 1, data, 0, 1, data, 0, 1));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> POTRS.invoke(false, 4, -1, data, 0, 4, data, 0, 4));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> POTRS.invoke(false, 4, 1, data, 0, 3, data, 0, 4));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> POTRS.invoke(false, 4, 1, data, 0, 4, data, 0, 3));

        TestUtils.assertThrows(IllegalArgumentException.class, () -> POTRS.invokeIdentity(false, -1, data, 0, 1, data, 0, 1));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> POTRS.invokeIdentity(false, 4, data, 0, 3, data, 0, 4));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> POTRS.invokeIdentity(false, 4, data, 0, 4, data, 0, 3));

        // Nothing to solve, nothing is accessed
        POTRS.invoke(false, 0, 3, new double[0], 0, 1, new double[0], 0, 1);
        POTRS.invoke(true, 4, 0, new double[0], 0, 4, new double[0], 0, 4);
        POTRS.invokeIdentity(false, 0, new double[0], 0, 1, new double[0], 0, 1);
    }

    /**
     * The inverse, written to a separate matrix, exactly symmetric.
     */
    @Test
    public void testPOTRSIdentity() {
        for (Implementation implementation : Implementation.values()) {
            for (boolean tight : BOOLEANS) {
                for (boolean upper : BOOLEANS) {
                    for (int n : SIZES_IDENTITY) {
                        LAPACKTest.doTestPOTRSIdentity(implementation, tight, upper, n);
                    }
                }
            }
        }
    }

    @Test
    public void testSYTRD() {
        for (Implementation implementation : Implementation.values()) {
            for (boolean tight : BOOLEANS) {
                for (int n : SIZES_SYTRD) {
                    LAPACKTest.doTestSYTRD(implementation, tight, n);
                }
            }
        }
    }

    @Test
    public void testSYTRDArgumentChecks() {

        double[] data = new double[16];
        double[] vector = new double[4];

        TestUtils.assertThrows(IllegalArgumentException.class, () -> SYTRD.invoke(-1, data, 0, 1, vector, 0, vector, 0, vector, 0));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> SYTRD.invoke(4, data, 0, 3, vector, 0, vector, 0, vector, 0));

        // Nothing to reduce, nothing is accessed
        SYTRD.invoke(0, new double[0], 0, 1, new double[0], 0, new double[0], 0, new double[0], 0);
    }

    @Test
    public void testTRTRS() {
        for (Implementation implementation : Implementation.values()) {
            for (boolean tight : BOOLEANS) {
                for (boolean upper : BOOLEANS) {
                    for (boolean transpose : BOOLEANS) {
                        for (boolean unit : BOOLEANS) {
                            for (int[] size : SIZES_SOLVE) {
                                LAPACKTest.doTestTRTRS(implementation, tight, upper, transpose, unit, size[0], size[1]);
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    public void testTRTRSArgumentChecks() {

        double[] data = new double[16];

        TestUtils.assertThrows(IllegalArgumentException.class, () -> TRTRS.invoke(true, false, false, -1, 1, data, 0, 1, data, 0, 1));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> TRTRS.invoke(true, false, false, 4, -1, data, 0, 4, data, 0, 4));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> TRTRS.invoke(true, false, false, 4, 1, data, 0, 3, data, 0, 4));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> TRTRS.invoke(true, false, false, 4, 1, data, 0, 4, data, 0, 3));

        // Nothing to solve, nothing is accessed
        TestUtils.assertEquals(0, TRTRS.invoke(true, false, false, 0, 3, new double[0], 0, 1, new double[0], 0, 1));
    }

    /**
     * An exactly zero diagonal element is reported (1-based), and nothing is solved. With a unit diagonal,
     * the diagonal is not read.
     */
    @Test
    public void testTRTRSSingular() {
        for (Implementation implementation : Implementation.values()) {
            for (boolean upper : BOOLEANS) {
                for (int nrhs : new int[] { 1, 5 }) {

                    int n = 12;
                    String message = "TRTRS singular " + implementation + " upper=" + upper + " " + n + "x" + nrhs;

                    Storage a = LAPACKTest.newTriangular(new Random(n), n, upper, false, false);
                    a.set(4, 4, 0.0);
                    a.set(7, 7, 0.0);

                    Storage b = new Storage(new Random(nrhs), n, nrhs, false);
                    double[] rhs = b.data.clone();

                    int info;
                    switch (implementation) {
                        case JAVA:
                            info = TRTRS.JVM.invoke(upper, false, false, n, nrhs, a.data, a.offset, a.ld, b.data, b.offset, b.ld);
                            break;
                        default:
                            info = TRTRS.invoke(upper, false, false, n, nrhs, a.data, a.offset, a.ld, b.data, b.offset, b.ld);
                            break;
                    }

                    TestUtils.assertEquals(message + " info", 5, info);
                    TestUtils.assertTrue(message + " B changed", Arrays.equals(rhs, b.data));

                    if (implementation == Implementation.DISPATCH) {
                        TestUtils.assertEquals(message + " unit info", 0,
                                TRTRS.invoke(upper, false, true, n, nrhs, a.data, a.offset, a.ld, b.data, b.offset, b.ld));
                    }
                }
            }
        }
    }

}

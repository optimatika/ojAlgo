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
import org.ojalgo.matrix.operation.BLASLevel3Test.OffHeapLimits;
import org.ojalgo.matrix.operation.BLASLevel3Test.Storage;

/**
 * The LAPACK operations ({@link GEBRD}, {@link GEQRF}, {@link GESDD}, {@link GETRF}, {@link GETRS},
 * {@link ORGBR}, {@link ORGQR}, {@link ORGTR}, {@link ORMQR}, {@link POTRF}, {@link POTRI}, {@link POTRS},
 * {@link SYEVD}, {@link SYTRD} and {@link TRTRS}) against their definitions: the factors multiplied back, and
 * any row interchanges undone, give the original matrix; the solution multiplied by the matrix (or its
 * transpose) gives the right hand side; the inverse multiplied by the matrix gives the identity; the
 * eigenvectors are orthonormal, and A*Z = Z*diag(w); the singular vectors are orthonormal, and U*diag(s)*V^T
 * is the original matrix. The orthogonal Q of a QR factorisation, or of a tridiagonal, Hessenberg or
 * bidiagonal reduction (and P of the latter), is formed in the test by applying the reflectors to the
 * identity, and compared with what {@link ORGQR}, {@link ORGTR}, {@link ORGHR} or {@link ORGBR} generates and
 * with how {@link ORMQR} multiplies. Matrices with offsets and padding as well as matrices filling their
 * arrays exactly. Each case is run through the pure Java implementation, the dispatching {@code invoke}, and
 * the native implementation(s) when there is one (run with {@code --enable-native-access=ojalgo,ALL-UNNAMED}
 * to include it). Elements outside the matrices, and outside the range of the pivots, must be left untouched,
 * and inputs must not change. For the symmetric positive definite matrices, the triangle that is not
 * referenced is filled with NaN.
 */
public class LAPACKTest {

    private static final boolean[] BOOLEANS = { false, true };
    private static final int OFFSET = 2;
    private static final int SENTINEL = -7;
    /**
     * m, n – the larger ones pass the native threshold, and the largest makes the Java implementation
     * multi-threaded.
     */
    private static final int[][] SIZES = { { 1, 1 }, { 1, 6 }, { 6, 1 }, { 3, 3 }, { 7, 4 }, { 4, 7 }, { 17, 17 }, { 30, 25 }, { 25, 30 }, { 60, 45 },
            { 45, 60 }, { 80, 80 }, { 270, 260 } };
    /**
     * m, n with m >= n, for GEBRD and ORGBR – the larger ones pass the native thresholds, and the largest
     * makes the Java implementations multi-threaded.
     */
    private static final int[][] SIZES_GEBRD = { { 1, 1 }, { 6, 1 }, { 3, 3 }, { 7, 4 }, { 17, 17 }, { 30, 25 }, { 60, 45 }, { 80, 80 }, { 150, 120 },
            { 350, 340 } };
    /**
     * n, for GEEV, HSEQR and TREVC – the largest passes the native thresholds, and makes the Java
     * implementations multi-threaded.
     */
    private static final int[] SIZES_GEEV = { 1, 2, 3, 7, 17, 40, 150 };
    /**
     * n, for GETRS with B = I – the larger ones pass the native threshold, and the largest makes the Java
     * implementation multi-threaded.
     */
    private static final int[] SIZES_IDENTITY = { 1, 2, 7, 17, 40, 80 };
    /**
     * n, for POTRS with B = I – the dispatching invokeIdentity inverts with POTRI up to 75, and solves for
     * the columns (multi-threaded) above that.
     */
    private static final int[] SIZES_IDENTITY_SPD = { 1, 2, 7, 17, 40, 75, 76, 150 };
    /**
     * m, k, nrhs, for ORMQR – the largest makes the Java implementation multi-threaded.
     */
    private static final int[][] SIZES_ORMQR = { { 1, 1, 1 }, { 5, 0, 3 }, { 3, 3, 1 }, { 6, 4, 4 }, { 17, 17, 1 }, { 17, 10, 5 }, { 40, 40, 1 },
            { 40, 33, 33 }, { 80, 80, 70 } };
    /**
     * n, nrhs, for GETRS – the larger ones pass the native threshold, and the largest makes the Java
     * implementation multi-threaded.
     */
    private static final int[][] SIZES_SOLVE = { { 1, 1 }, { 3, 1 }, { 6, 4 }, { 17, 1 }, { 17, 5 }, { 40, 1 }, { 40, 33 }, { 80, 70 } };
    /**
     * n, for POTRF – the larger ones pass the native threshold, and the largest makes the Java implementation
     * multi-threaded.
     */
    private static final int[] SIZES_SPD = { 1, 2, 7, 17, 40, 150 };
    /**
     * n, for SYTRD, ORGTR, GEHRD and ORGHR – the larger ones pass the native thresholds, and the largest
     * makes the Java implementations multi-threaded.
     */
    private static final int[] SIZES_SYTRD = { 1, 2, 3, 7, 17, 40, 150, 340 };

    /**
     * Elements of the array outside the rows-by-columns matrix must be unchanged.
     */
    /**
     * M*VR = VR*D, with D block diagonal as for GEEV and TREVC: the real eigenvalues, and [wr, wi; -wi, wr]
     * for each complex pair. M is n-by-n, column-major and tight.
     */
    private static void assertEigenvectors(final String message, final double[] matrix, final int n, final double[] wr, final double[] wi, final int offset,
            final Storage vr) {
        double[] expected = new double[n * n];
        double[] actual = new double[n * n];
        for (int j = 0; j < n; j++) {
            double imaginary = wi[offset + j];
            for (int i = 0; i < n; i++) {
                double mv = 0.0;
                for (int k = 0; k < n; k++) {
                    mv += matrix[i + k * n] * vr.get(k, j);
                }
                double vd = vr.get(i, j) * wr[offset + j];
                if (imaginary > 0.0) {
                    vd -= vr.get(i, j + 1) * imaginary;
                } else if (imaginary < 0.0) {
                    vd -= vr.get(i, j - 1) * imaginary;
                }
                expected[i + j * n] = mv;
                actual[i + j * n] = vd;
            }
        }
        BLASLevel3Test.assertArray(message + " M*VR = VR*D", expected, actual);
    }

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
     * The columns of the matrix are orthonormal.
     */
    private static void assertOrthonormal(final String message, final Storage matrix, final int rows, final int columns) {
        double[] identity = new double[columns * columns];
        double[] product = new double[columns * columns];
        for (int j = 0; j < columns; j++) {
            for (int i = 0; i < columns; i++) {
                double sum = 0.0;
                for (int p = 0; p < rows; p++) {
                    sum += matrix.get(p, i) * matrix.get(p, j);
                }
                identity[i + j * columns] = i == j ? 1.0 : 0.0;
                product[i + j * columns] = sum;
            }
        }
        BLASLevel3Test.assertArray(message + " X^T*X", identity, product);
    }

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
     * The same eigenvalues, in any order.
     */
    private static void assertSameEigenvalues(final String message, final double[] wr1, final double[] wi1, final double[] wr2, final double[] wi2,
            final int offset, final int n) {
        double scale = 1.0;
        for (int i = 0; i < n; i++) {
            scale = Math.max(scale, Math.hypot(wr1[offset + i], wi1[offset + i]));
        }
        boolean[] matched = new boolean[n];
        for (int i = 0; i < n; i++) {
            int closest = -1;
            double distance = Double.POSITIVE_INFINITY;
            for (int j = 0; j < n; j++) {
                double d = Math.hypot(wr1[offset + i] - wr2[offset + j], wi1[offset + i] - wi2[offset + j]);
                if (!matched[j] && d < distance) {
                    closest = j;
                    distance = d;
                }
            }
            matched[closest] = true;
            TestUtils.assertTrue(message + " eigenvalue " + i, distance <= 1E-10 * scale);
        }
    }

    /**
     * The n-by-n orthogonal matrix P = G(1)*G(2)*...*G(n-1), from the reflectors above the first
     * superdiagonal of A and tau, as returned by GEBRD: u(i+1) = 1, and the rest of u to the right of it in
     * row i of A. The reflectors applied, in reverse order, to the identity.
     */
    private static double[][] explicitP(final Storage a, final double[] tau, final int offsetTau, final int n) {

        double[][] retVal = new double[n][n];
        for (int i = 0; i < n; i++) {
            retVal[i][i] = 1.0;
        }

        for (int i = n - 2; i >= 0; i--) {
            double beta = tau[offsetTau + i];
            int first = i + 1;
            for (int j = 0; j < n; j++) {
                double sum = retVal[first][j];
                for (int p = first + 1; p < n; p++) {
                    sum += a.get(i, p) * retVal[p][j];
                }
                retVal[first][j] -= beta * sum;
                for (int p = first + 1; p < n; p++) {
                    retVal[p][j] -= beta * sum * a.get(i, p);
                }
            }
        }

        return retVal;
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
            case JAVA   -> GEQRF.JVM.invoke(m, n, a.data, a.offset, a.ld, tau, offsetTau);
            case NATIVE -> GEQRF.Native.invoke(m, n, a.data, a.offset, a.ld, tau, offsetTau);
            default     -> GEQRF.invoke(m, n, a.data, a.offset, a.ld, tau, offsetTau);
        }
    }

    private static int invokeGETRF(final Implementation implementation, final int m, final int n, final Storage a, final int[] ipiv, final int offsetIpiv) {
        return switch (implementation) {
            case JAVA   -> GETRF.JVM.invoke(m, n, a.data, a.offset, a.ld, ipiv, offsetIpiv);
            case NATIVE -> GETRF.Native.invoke(m, n, a.data, a.offset, a.ld, ipiv, offsetIpiv);
            default     -> GETRF.invoke(m, n, a.data, a.offset, a.ld, ipiv, offsetIpiv);
        };
    }

    /**
     * @param offHeap Whether the native implementation has an off-heap variant
     */
    private static boolean isAvailable(final Implementation implementation, final boolean nativeAvailable, final boolean offHeap) {
        return switch (implementation) {
            case NATIVE   -> nativeAvailable;
            case OFF_HEAP -> nativeAvailable && offHeap;
            default       -> true;
        };
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

    static void doTestGEBRD(final Implementation implementation, final boolean tight, final int m, final int n) {

        Storage a = new Storage(new Random(m + 31 * n), m, n, tight);
        double[] original = a.data.clone();
        int offset = tight ? 0 : OFFSET;
        double[] d = LAPACKTest.newTau(n, tight);
        double[] e = LAPACKTest.newTau(Math.max(0, n - 1), tight);
        double[] tauq = LAPACKTest.newTau(n, tight);
        double[] taup = LAPACKTest.newTau(n, tight);

        switch (implementation) {
            case JAVA   -> GEBRD.JVM.invoke(m, n, a.data, a.offset, a.ld, d, offset, e, offset, tauq, offset, taup, offset);
            case NATIVE -> GEBRD.Native.invoke(m, n, a.data, a.offset, a.ld, d, offset, e, offset, tauq, offset, taup, offset);
            default     -> GEBRD.invoke(m, n, a.data, a.offset, a.ld, d, offset, e, offset, tauq, offset, taup, offset);
        }

        String message = "GEBRD " + implementation + " tight=" + tight + " " + m + "x" + n;

        LAPACKTest.assertOutsideUnchanged(message, original, a, m, n);

        for (int p = 0; p < d.length; p++) {
            int i = p - offset;
            if (i >= 0 && i < n) {
                TestUtils.assertTrue(message + " d " + i, d[p] == a.get(i, i));
                TestUtils.assertTrue(message + " tauq " + i, tauq[p] == 0.0 || tauq[p] >= 1.0 && tauq[p] <= 2.0);
                TestUtils.assertTrue(message + " taup " + i, taup[p] == 0.0 || i < n - 1 && taup[p] >= 1.0 && taup[p] <= 2.0);
            } else {
                TestUtils.assertTrue(message + " outside d changed", d[p] == SENTINEL);
                TestUtils.assertTrue(message + " outside tauq changed", tauq[p] == SENTINEL);
                TestUtils.assertTrue(message + " outside taup changed", taup[p] == SENTINEL);
            }
        }
        for (int p = 0; p < e.length; p++) {
            int i = p - offset;
            if (i >= 0 && i < n - 1) {
                TestUtils.assertTrue(message + " e " + i, e[p] == a.get(i, i + 1));
            } else {
                TestUtils.assertTrue(message + " outside e changed", e[p] == SENTINEL);
            }
        }

        double[][] q = LAPACKTest.explicitQ(a, tauq, offset, m, n);
        double[][] pp = LAPACKTest.explicitP(a, taup, offset, n);

        double[] identityQ = new double[m * m];
        double[] productQ = new double[m * m];
        for (int j = 0; j < m; j++) {
            for (int i = 0; i < m; i++) {
                double sum = 0.0;
                for (int p = 0; p < m; p++) {
                    sum += q[p][i] * q[p][j];
                }
                identityQ[i + j * m] = i == j ? 1.0 : 0.0;
                productQ[i + j * m] = sum;
            }
        }
        BLASLevel3Test.assertArray(message + " Q^T*Q", identityQ, productQ);

        double[] identityP = new double[n * n];
        double[] productP = new double[n * n];
        for (int j = 0; j < n; j++) {
            for (int i = 0; i < n; i++) {
                double sum = 0.0;
                for (int p = 0; p < n; p++) {
                    sum += pp[p][i] * pp[p][j];
                }
                identityP[i + j * n] = i == j ? 1.0 : 0.0;
                productP[i + j * n] = sum;
            }
        }
        BLASLevel3Test.assertArray(message + " P^T*P", identityP, productP);

        // Q^T*A*P = B
        double[][] qtA = new double[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                double sum = 0.0;
                for (int p = 0; p < m; p++) {
                    sum += q[p][i] * original[a.index(p, j)];
                }
                qtA[i][j] = sum;
            }
        }
        double[] expected = new double[m * n];
        double[] actual = new double[m * n];
        for (int j = 0; j < n; j++) {
            for (int i = 0; i < m; i++) {
                double sum = 0.0;
                for (int p = 0; p < n; p++) {
                    sum += qtA[i][p] * pp[p][j];
                }
                expected[i + j * m] = i == j ? d[offset + i] : j == i + 1 ? e[offset + i] : 0.0;
                actual[i + j * m] = sum;
            }
        }
        BLASLevel3Test.assertArray(message + " Q^T*A*P", expected, actual);
    }

    static void doTestGEEV(final Implementation implementation, final boolean tight, final int n) {

        Random random = new Random(n);
        Storage a = new Storage(random, n, n, tight);
        double[] matrix = new double[n * n];
        for (int j = 0; j < n; j++) {
            for (int i = 0; i < n; i++) {
                matrix[i + j * n] = a.get(i, j);
            }
        }
        double[] before = a.data.clone();
        int offset = tight ? 0 : OFFSET;
        double[] wr = LAPACKTest.newTau(n, tight);
        double[] wi = LAPACKTest.newTau(n, tight);
        Storage vr = new Storage(random, n, n, tight);
        double[] beforeVR = vr.data.clone();

        int info = switch (implementation) {
            case JAVA   -> GEEV.JVM.invoke(true, n, a.data, a.offset, a.ld, wr, offset, wi, offset, vr.data, vr.offset, vr.ld);
            case NATIVE -> GEEV.Native.invoke(true, n, a.data, a.offset, a.ld, wr, offset, wi, offset, vr.data, vr.offset, vr.ld);
            default     -> GEEV.invoke(true, n, a.data, a.offset, a.ld, wr, offset, wi, offset, vr.data, vr.offset, vr.ld);
        };

        String message = "GEEV " + implementation + " tight=" + tight + " " + n;
        TestUtils.assertEquals(message + " info", 0, info);
        LAPACKTest.assertOutsideUnchanged(message, before, a, n, n);
        LAPACKTest.assertOutsideUnchanged(message + " VR", beforeVR, vr, n, n);
        for (int p = 0; p < wr.length; p++) {
            if (p < offset || p >= offset + n) {
                TestUtils.assertTrue(message + " outside wr or wi changed", wr[p] == SENTINEL && wi[p] == SENTINEL);
            }
        }

        // Complex conjugate pairs, the positive imaginary part first
        for (int j = 0; j < n; j++) {
            double imaginary = wi[offset + j];
            if (imaginary > 0.0) {
                TestUtils.assertTrue(message + " pair " + j, j + 1 < n && wr[offset + j + 1] == wr[offset + j] && wi[offset + j + 1] == -imaginary);
            } else if (imaginary < 0.0) {
                TestUtils.assertTrue(message + " pair " + j, j > 0 && wi[offset + j - 1] == -imaginary);
            }
        }

        LAPACKTest.assertEigenvectors(message, matrix, n, wr, wi, offset, vr);

        // Euclidean norm 1, and (one of) the largest components real
        for (int j = 0; j < n; j++) {
            double imaginary = wi[offset + j];
            if (imaginary == 0.0) {
                double sum = 0.0;
                for (int i = 0; i < n; i++) {
                    sum += vr.get(i, j) * vr.get(i, j);
                }
                TestUtils.assertEquals(message + " norm " + j, 1.0, Math.sqrt(sum), 1E-14);
            } else if (imaginary > 0.0) {
                double sum = 0.0;
                double largest = 0.0;
                for (int i = 0; i < n; i++) {
                    double modulus2 = vr.get(i, j) * vr.get(i, j) + vr.get(i, j + 1) * vr.get(i, j + 1);
                    sum += modulus2;
                    largest = Math.max(largest, modulus2);
                }
                TestUtils.assertEquals(message + " norm " + j, 1.0, Math.sqrt(sum), 1E-14);
                boolean real = false;
                for (int i = 0; i < n; i++) {
                    double modulus2 = vr.get(i, j) * vr.get(i, j) + vr.get(i, j + 1) * vr.get(i, j + 1);
                    real |= vr.get(i, j + 1) == 0.0 && modulus2 >= largest * (1.0 - 1E-12);
                }
                TestUtils.assertTrue(message + " largest component real " + j, real);
            }
        }

        // The eigenvalues only: the same eigenvalues
        Storage b = new Storage(random, n, n, tight);
        for (int j = 0; j < n; j++) {
            for (int i = 0; i < n; i++) {
                b.set(i, j, matrix[i + j * n]);
            }
        }
        double[] valuesR = LAPACKTest.newTau(n, tight);
        double[] valuesI = LAPACKTest.newTau(n, tight);

        int infoValues = switch (implementation) {
            case JAVA   -> GEEV.JVM.invoke(false, n, b.data, b.offset, b.ld, valuesR, offset, valuesI, offset, null, 0, 1);
            case NATIVE -> GEEV.Native.invoke(false, n, b.data, b.offset, b.ld, valuesR, offset, valuesI, offset, null, 0, 1);
            default     -> GEEV.invoke(false, n, b.data, b.offset, b.ld, valuesR, offset, valuesI, offset, null, 0, 1);
        };

        TestUtils.assertEquals(message + " info, values only", 0, infoValues);
        LAPACKTest.assertSameEigenvalues(message + ", values only", wr, wi, valuesR, valuesI, offset, n);
    }

    static void doTestGEHRD(final Implementation implementation, final boolean tight, final int n) {

        Storage a = new Storage(new Random(n), n, n, tight);
        double[] original = a.data.clone();
        int offset = tight ? 0 : OFFSET;
        double[] tau = LAPACKTest.newTau(Math.max(0, n - 1), tight);

        switch (implementation) {
            case JAVA   -> GEHRD.JVM.invoke(n, a.data, a.offset, a.ld, tau, offset);
            case NATIVE -> GEHRD.Native.invoke(n, a.data, a.offset, a.ld, tau, offset);
            default     -> GEHRD.invoke(n, a.data, a.offset, a.ld, tau, offset);
        }

        String message = "GEHRD " + implementation + " tight=" + tight + " " + n;
        LAPACKTest.assertOutsideUnchanged(message, original, a, n, n);

        for (int p = 0; p < tau.length; p++) {
            int i = p - offset;
            if (i >= 0 && i < n - 1) {
                TestUtils.assertTrue(message + " tau " + i, tau[p] == 0.0 || tau[p] >= 1.0 && tau[p] <= 2.0);
            } else {
                TestUtils.assertTrue(message + " outside tau changed", tau[p] == SENTINEL);
            }
        }

        double[][] q = LAPACKTest.explicitQ(a, tau, offset, n, n - 1, 1);

        // H*Q^T, H the upper Hessenberg part of A
        double[][] hq = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                double sum = 0.0;
                for (int r = Math.max(0, i - 1); r < n; r++) {
                    sum += a.get(i, r) * q[j][r];
                }
                hq[i][j] = sum;
            }
        }

        double[] identity = new double[n * n];
        double[] product = new double[n * n];
        double[] expected = new double[n * n];
        double[] actual = new double[n * n];
        for (int j = 0; j < n; j++) {
            for (int i = 0; i < n; i++) {
                double qq = 0.0;
                double qhq = 0.0;
                for (int p = 0; p < n; p++) {
                    qq += q[p][i] * q[p][j];
                    qhq += q[i][p] * hq[p][j];
                }
                identity[i + j * n] = i == j ? 1.0 : 0.0;
                product[i + j * n] = qq;
                expected[i + j * n] = original[a.index(i, j)];
                actual[i + j * n] = qhq;
            }
        }
        BLASLevel3Test.assertArray(message + " Q^T*Q", identity, product);
        BLASLevel3Test.assertArray(message, expected, actual);
    }

    static void doTestGEQRF(final Implementation implementation, final boolean tight, final int m, final int n) {

        Storage a = new Storage(new Random(m + 31 * n), m, n, tight);
        double[] original = a.data.clone();
        int offsetTau = tight ? 0 : OFFSET;
        double[] tau = LAPACKTest.newTau(Math.min(m, n), tight);

        LAPACKTest.invokeGEQRF(implementation, m, n, a, tau, offsetTau);

        LAPACKTest.assertQR("GEQRF " + implementation + " tight=" + tight + " " + m + "x" + n, original, a, m, n, tau, offsetTau);
    }

    static void doTestGESDD(final Implementation implementation, final boolean tight, final boolean vectors, final boolean fullSize, final int m, final int n) {

        Storage a = new Storage(new Random(m + 31 * n), m, n, tight);
        double[] original = a.data.clone();
        int k = Math.min(m, n);
        int offset = tight ? 0 : OFFSET;
        double[] s = LAPACKTest.newTau(k, tight);

        int columnsU = fullSize ? m : k;
        int columnsV = fullSize ? n : k;
        Storage u = new Storage(new Random(m), m, columnsU, tight);
        Storage v = new Storage(new Random(n), n, columnsV, tight);
        double[] beforeU = u.data.clone();
        double[] beforeV = v.data.clone();

        int info = switch (implementation) {
            case JAVA   -> GESDD.JVM.invoke(vectors, fullSize, m, n, a.data, a.offset, a.ld, s, offset, u.data, u.offset, u.ld, v.data, v.offset, v.ld);
            case NATIVE -> GESDD.Native.invoke(vectors, fullSize, m, n, a.data, a.offset, a.ld, s, offset, u.data, u.offset, u.ld, v.data, v.offset, v.ld);
            default     -> GESDD.invoke(vectors, fullSize, m, n, a.data, a.offset, a.ld, s, offset, u.data, u.offset, u.ld, v.data, v.offset, v.ld);
        };

        String message = "GESDD " + implementation + " tight=" + tight + " vectors=" + vectors + " fullSize=" + fullSize + " " + m + "x" + n;
        TestUtils.assertEquals(message + " info", 0, info);

        for (int p = 0; p < s.length; p++) {
            int i = p - offset;
            if (i < 0 || i >= k) {
                TestUtils.assertTrue(message + " outside s changed", s[p] == SENTINEL);
            } else {
                TestUtils.assertTrue(message + " negative", s[p] >= 0.0);
                TestUtils.assertTrue(message + " not descending", i == 0 || s[p - 1] >= s[p]);
            }
        }

        // The Frobenius norm is that of the original
        double sumOfSquares = 0.0;
        for (int j = 0; j < n; j++) {
            for (int i = 0; i < m; i++) {
                double value = original[a.index(i, j)];
                sumOfSquares += value * value;
            }
        }
        double sumOfSquaredValues = 0.0;
        for (int i = 0; i < k; i++) {
            sumOfSquaredValues += s[offset + i] * s[offset + i];
        }
        BLASLevel3Test.assertArray(message + " norm", new double[] { sumOfSquares }, new double[] { sumOfSquaredValues });

        if (vectors) {

            LAPACKTest.assertOutsideUnchanged(message + " U", beforeU, u, m, columnsU);
            LAPACKTest.assertOutsideUnchanged(message + " V", beforeV, v, n, columnsV);

            LAPACKTest.assertOrthonormal(message + " U", u, m, columnsU);
            LAPACKTest.assertOrthonormal(message + " V", v, n, columnsV);

            double[] expected = new double[m * n];
            double[] actual = new double[m * n];
            for (int j = 0; j < n; j++) {
                for (int i = 0; i < m; i++) {
                    double sum = 0.0;
                    for (int p = 0; p < k; p++) {
                        sum += u.get(i, p) * s[offset + p] * v.get(j, p);
                    }
                    expected[i + j * m] = original[a.index(i, j)];
                    actual[i + j * m] = sum;
                }
            }
            BLASLevel3Test.assertArray(message + " U*S*V^T", expected, actual);

        } else {

            BLASLevel3Test.assertUnchanged(message + " U", beforeU, u.data);
            BLASLevel3Test.assertUnchanged(message + " V", beforeV, v.data);
        }
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
            case JAVA             -> GETRS.JVM.invoke(transpose, n, nrhs, a.data, a.offset, a.ld, ipiv, offsetIpiv, b.data, b.offset, b.ld);
            case NATIVE, OFF_HEAP -> {
                try (OffHeapLimits limits = OffHeapLimits.of(implementation)) {
                    GETRS.Native.invoke(transpose, n, nrhs, a.data, a.offset, a.ld, ipiv, offsetIpiv, b.data, b.offset, b.ld);
                }
            }
            default               -> GETRS.invoke(transpose, n, nrhs, a.data, a.offset, a.ld, ipiv, offsetIpiv, b.data, b.offset, b.ld);
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
    /**
     * An m-by-n matrix, m >= n, reduced by GEBRD; then Q (m-by-n, or m-by-m if full) or P^T (n-by-n)
     * generated from it.
     */
    static void doTestHSEQR(final boolean tight, final int n) {

        Random random = new Random(n);
        Storage h = new Storage(random, n, n, tight);
        double[] hessenberg = new double[n * n];
        for (int j = 0; j < n; j++) {
            for (int i = 0; i <= Math.min(j + 1, n - 1); i++) {
                hessenberg[i + j * n] = h.get(i, j);
            }
        }
        double[] before = h.data.clone();
        int offset = tight ? 0 : OFFSET;
        double[] wr = LAPACKTest.newTau(n, tight);
        double[] wi = LAPACKTest.newTau(n, tight);
        Storage z = new Storage(random, n, n, tight);
        for (int j = 0; j < n; j++) {
            for (int i = 0; i < n; i++) {
                z.set(i, j, i == j ? 1.0 : 0.0);
            }
        }
        double[] beforeZ = z.data.clone();

        int info = HSEQR.invoke(true, true, n, h.data, h.offset, h.ld, wr, offset, wi, offset, z.data, z.offset, z.ld);

        String message = "HSEQR tight=" + tight + " " + n;
        TestUtils.assertEquals(message + " info", 0, info);
        LAPACKTest.assertOutsideUnchanged(message, before, h, n, n);
        LAPACKTest.assertOutsideUnchanged(message + " Z", beforeZ, z, n, n);
        for (int p = 0; p < wr.length; p++) {
            if (p < offset || p >= offset + n) {
                TestUtils.assertTrue(message + " outside wr or wi changed", wr[p] == SENTINEL && wi[p] == SENTINEL);
            }
        }

        // T is 0 below the subdiagonal, and on it except for the complex pairs
        double scale = 1.0;
        for (int j = 0; j < n; j++) {
            for (int i = j + 1; i < n; i++) {
                boolean block = i == j + 1 && wi[offset + j] > 0.0;
                TestUtils.assertTrue(message + " T(" + i + "," + j + ")", block || h.get(i, j) == 0.0);
            }
            scale = Math.max(scale, Math.hypot(wr[offset + j], wi[offset + j]));
        }

        // The eigenvalues are those of the blocks on the diagonal of T
        for (int j = 0; j < n; j++) {
            double imaginary = wi[offset + j];
            if (imaginary == 0.0) {
                TestUtils.assertEquals(message + " eigenvalue " + j, wr[offset + j], h.get(j, j), 1E-12 * scale);
            } else if (imaginary > 0.0) {
                double trace = h.get(j, j) + h.get(j + 1, j + 1);
                double determinant = h.get(j, j) * h.get(j + 1, j + 1) - h.get(j, j + 1) * h.get(j + 1, j);
                TestUtils.assertEquals(message + " trace " + j, 2.0 * wr[offset + j], trace, 1E-12 * scale);
                TestUtils.assertEquals(message + " determinant " + j, wr[offset + j] * wr[offset + j] + imaginary * imaginary, determinant,
                        1E-12 * scale * scale);
            }
        }

        // Z is orthogonal, and Z*T*Z^T = H
        LAPACKTest.assertOrthonormal(message, z, n, n);
        double[] product = new double[n * n];
        for (int j = 0; j < n; j++) {
            for (int i = 0; i < n; i++) {
                double tz = 0.0;
                for (int p = Math.max(0, i - 1); p < n; p++) {
                    tz += h.get(i, p) * z.get(j, p);
                }
                product[i + j * n] = tz;
            }
        }
        double[] actual = new double[n * n];
        for (int j = 0; j < n; j++) {
            for (int i = 0; i < n; i++) {
                double ztz = 0.0;
                for (int p = 0; p < n; p++) {
                    ztz += z.get(i, p) * product[p + j * n];
                }
                actual[i + j * n] = ztz;
            }
        }
        BLASLevel3Test.assertArray(message + " Z*T*Z^T = H", hessenberg, actual);

        // The eigenvalues only: the same eigenvalues
        Storage g = new Storage(random, n, n, tight);
        for (int j = 0; j < n; j++) {
            for (int i = 0; i <= Math.min(j + 1, n - 1); i++) {
                g.set(i, j, hessenberg[i + j * n]);
            }
        }
        double[] valuesR = LAPACKTest.newTau(n, tight);
        double[] valuesI = LAPACKTest.newTau(n, tight);
        TestUtils.assertEquals(message + " info, values only", 0,
                HSEQR.invoke(false, false, n, g.data, g.offset, g.ld, valuesR, offset, valuesI, offset, null, 0, 1));
        LAPACKTest.assertSameEigenvalues(message + ", values only", wr, wi, valuesR, valuesI, offset, n);
    }

    static void doTestORGBR(final Implementation implementation, final boolean tight, final boolean q, final boolean full, final int m, final int n) {

        int columns = q && full ? m : n;
        Storage a = new Storage(new Random(m + 31 * n), m, columns, tight);
        int offset = tight ? 0 : OFFSET;
        double[] d = new double[offset + n + offset];
        double[] e = new double[offset + n + offset];
        double[] tauq = LAPACKTest.newTau(n, tight);
        double[] taup = LAPACKTest.newTau(n, tight);
        GEBRD.JVM.invoke(m, n, a.data, a.offset, a.ld, d, offset, e, offset, tauq, offset, taup, offset);
        double[] reflectors = a.data.clone();
        double[] tau = q ? tauq : taup;
        double[] taus = tau.clone();
        double[][] explicit = q ? LAPACKTest.explicitQ(a, tauq, offset, m, n) : LAPACKTest.explicitP(a, taup, offset, n);

        int rows = q ? m : n;
        int k = q ? n : m;

        switch (implementation) {
            case JAVA   -> ORGBR.JVM.invoke(q, rows, columns, k, a.data, a.offset, a.ld, tau, offset);
            case NATIVE -> ORGBR.Native.invoke(q, rows, columns, k, a.data, a.offset, a.ld, tau, offset);
            default     -> ORGBR.invoke(q, rows, columns, k, a.data, a.offset, a.ld, tau, offset);
        }

        String message = "ORGBR " + implementation + " tight=" + tight + " q=" + q + " full=" + full + " " + m + "x" + n;
        BLASLevel3Test.assertUnchanged(message, taus, tau);
        LAPACKTest.assertOutsideUnchanged(message, reflectors, a, rows, columns);

        double[] expected = new double[rows * columns];
        double[] actual = new double[rows * columns];
        for (int j = 0; j < columns; j++) {
            for (int i = 0; i < rows; i++) {
                expected[i + j * rows] = q ? explicit[i][j] : explicit[j][i];
                actual[i + j * rows] = a.get(i, j);
            }
        }
        BLASLevel3Test.assertArray(message, expected, actual);
    }

    static void doTestORGHR(final Implementation implementation, final boolean tight, final int n) {

        Storage a = new Storage(new Random(n), n, n, tight);
        int offset = tight ? 0 : OFFSET;
        double[] tau = LAPACKTest.newTau(Math.max(0, n - 1), tight);
        GEHRD.JVM.invoke(n, a.data, a.offset, a.ld, tau, offset);
        double[] reflectors = a.data.clone();
        double[] taus = tau.clone();
        double[][] q = LAPACKTest.explicitQ(a, tau, offset, n, n - 1, 1);

        switch (implementation) {
            case JAVA   -> ORGHR.JVM.invoke(n, a.data, a.offset, a.ld, tau, offset);
            case NATIVE -> ORGHR.Native.invoke(n, a.data, a.offset, a.ld, tau, offset);
            default     -> ORGHR.invoke(n, a.data, a.offset, a.ld, tau, offset);
        }

        String message = "ORGHR " + implementation + " tight=" + tight + " " + n;
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
            case JAVA   -> ORGQR.JVM.invoke(m, n, k, a.data, a.offset, a.ld, tau, offsetTau);
            case NATIVE -> ORGQR.Native.invoke(m, n, k, a.data, a.offset, a.ld, tau, offsetTau);
            default     -> ORGQR.invoke(m, n, k, a.data, a.offset, a.ld, tau, offsetTau);
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
            case JAVA   -> ORGTR.JVM.invoke(n, a.data, a.offset, a.ld, tau, offset);
            case NATIVE -> ORGTR.Native.invoke(n, a.data, a.offset, a.ld, tau, offset);
            default     -> ORGTR.invoke(n, a.data, a.offset, a.ld, tau, offset);
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

    static void doTestPOTRF(final Implementation implementation, final boolean tight, final boolean upper, final int n) {

        Storage a = LAPACKTest.newSPD(new Random(n), n, upper, tight);
        double[] original = a.data.clone();

        int info = switch (implementation) {
            case JAVA   -> POTRF.JVM.invoke(upper, n, a.data, a.offset, a.ld);
            case NATIVE -> POTRF.Native.invoke(upper, n, a.data, a.offset, a.ld);
            default     -> POTRF.invoke(upper, n, a.data, a.offset, a.ld);
        };

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

    /**
     * The inverse, in the triangle that held the factor, multiplied by A gives the identity. The other
     * triangle (NaN), and everything outside A, is left untouched.
     */
    static void doTestPOTRI(final Implementation implementation, final boolean tight, final boolean upper, final int n) {

        Storage a = LAPACKTest.newSPD(new Random(n), n, upper, tight);
        double[] original = a.data.clone();
        POTRF.JVM.invoke(upper, n, a.data, a.offset, a.ld);
        double[] factor = a.data.clone();

        int info = switch (implementation) {
            case JAVA -> POTRI.JVM.invoke(upper, n, a.data, a.offset, a.ld);
            default   -> POTRI.invoke(upper, n, a.data, a.offset, a.ld);
        };

        String message = "POTRI " + implementation + " tight=" + tight + " upper=" + upper + " " + n;
        TestUtils.assertEquals(message + " info", 0, info);

        double[] expected = factor.clone();
        double[] actual = a.data.clone();
        for (int j = 0; j < n; j++) {
            for (int i = 0; i < n; i++) {
                if (upper ? i <= j : i >= j) {
                    double sum = 0.0;
                    for (int k = 0; k < n; k++) {
                        sum += LAPACKTest.symmetric(a.data, a, upper, i, k) * LAPACKTest.symmetric(original, a, upper, k, j);
                    }
                    expected[a.index(i, j)] = i == j ? 1.0 : 0.0;
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
            case JAVA             -> POTRS.JVM.invoke(upper, n, nrhs, a.data, a.offset, a.ld, b.data, b.offset, b.ld);
            case NATIVE, OFF_HEAP -> {
                try (OffHeapLimits limits = OffHeapLimits.of(implementation)) {
                    POTRS.Native.invoke(upper, n, nrhs, a.data, a.offset, a.ld, b.data, b.offset, b.ld);
                }
            }
            default               -> POTRS.invoke(upper, n, nrhs, a.data, a.offset, a.ld, b.data, b.offset, b.ld);
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
    /**
     * With separate, the overload that leaves A unchanged and writes the eigenvectors to a separate Z.
     */
    /**
     * T, symmetric tridiagonal with the diagonal and the subdiagonal given: with the rotations applied to Z =
     * I, T*Z = Z*diag(d) and Z is orthogonal. The eigenvalues only are the same, exactly, and the trace and
     * Frobenius norm are those of T (scaled, so that the squares neither overflow nor underflow).
     */
    static void doTestSTEQR(final String message, final double[] diagonal, final double[] subdiagonal) {

        int n = diagonal.length;

        double[] e = new double[n];
        System.arraycopy(subdiagonal, 0, e, 0, n - 1);
        double[] d = diagonal.clone();
        double[] z = new double[n * n];
        for (int j = 0; j < n; j++) {
            z[j + j * n] = 1.0;
        }

        STEQR.invoke(d, e, (low, high, cos, sin) -> {
            for (int r = 0; r < n; r++) {
                double zLow = z[r + low * n];
                double zHigh = z[r + high * n];
                z[r + low * n] = cos * zLow - sin * zHigh;
                z[r + high * n] = sin * zLow + cos * zHigh;
            }
        });

        double[] valuesOnly = diagonal.clone();
        double[] work = new double[n];
        System.arraycopy(subdiagonal, 0, work, 0, n - 1);
        STEQR.invoke(valuesOnly, work, RotateColumns.NULL);
        TestUtils.assertTrue(message + " values only", Arrays.equals(d, valuesOnly));

        double scale = Double.MIN_NORMAL;
        for (int i = 0; i < n; i++) {
            scale = Math.max(scale, Math.abs(diagonal[i]));
            if (i + 1 < n) {
                scale = Math.max(scale, Math.abs(subdiagonal[i]));
            }
        }
        double trace = 0.0;
        double sumOfSquares = 0.0;
        double sumOfValues = 0.0;
        double sumOfSquaredValues = 0.0;
        for (int i = 0; i < n; i++) {
            trace += diagonal[i] / scale;
            sumOfSquares += (diagonal[i] / scale) * (diagonal[i] / scale);
            if (i + 1 < n) {
                sumOfSquares += 2.0 * (subdiagonal[i] / scale) * (subdiagonal[i] / scale);
            }
            sumOfValues += d[i] / scale;
            sumOfSquaredValues += (d[i] / scale) * (d[i] / scale);
        }
        BLASLevel3Test.assertArray(message + " trace and norm", new double[] { trace, sumOfSquares }, new double[] { sumOfValues, sumOfSquaredValues });

        double[] identity = new double[n * n];
        double[] product = new double[n * n];
        double[] expected = new double[n * n];
        double[] actual = new double[n * n];
        for (int j = 0; j < n; j++) {
            for (int i = 0; i < n; i++) {
                double ztz = 0.0;
                for (int p = 0; p < n; p++) {
                    ztz += z[p + i * n] * z[p + j * n];
                }
                double tz = diagonal[i] * z[i + j * n];
                if (i > 0) {
                    tz += subdiagonal[i - 1] * z[i - 1 + j * n];
                }
                if (i + 1 < n) {
                    tz += subdiagonal[i] * z[i + 1 + j * n];
                }
                identity[i + j * n] = i == j ? 1.0 : 0.0;
                product[i + j * n] = ztz;
                expected[i + j * n] = z[i + j * n] * d[j];
                actual[i + j * n] = tz;
            }
        }
        BLASLevel3Test.assertArray(message + " Z^T*Z", identity, product);
        BLASLevel3Test.assertArray(message + " T*Z", expected, actual);
    }

    static void doTestSYEVD(final Implementation implementation, final boolean tight, final boolean vectors, final boolean separate, final int n) {

        // Symmetric indefinite, stored in the lower triangle (the upper triangle is NaN)
        Storage a = LAPACKTest.newSPD(new Random(n), n, false, tight);
        double shift = 0.0;
        for (int i = 0; i < n; i++) {
            shift += a.get(i, i) / n;
        }
        for (int i = 0; i < n; i++) {
            a.set(i, i, a.get(i, i) - shift);
        }
        double[] original = a.data.clone();
        int offset = tight ? 0 : OFFSET;
        double[] w = LAPACKTest.newTau(n, tight);

        Storage z = separate ? new Storage(new Random(n + 1), n, n, tight) : a;
        double[] beforeZ = z.data.clone();

        int info;
        if (separate) {
            info = switch (implementation) {
                case NATIVE -> SYEVD.Native.invoke(vectors, n, a.data, a.offset, a.ld, w, offset, z.data, z.offset, z.ld);
                default     -> SYEVD.invoke(vectors, n, a.data, a.offset, a.ld, w, offset, z.data, z.offset, z.ld);
            };
        } else {
            info = switch (implementation) {
                case JAVA   -> SYEVD.JVM.invoke(vectors, n, a.data, a.offset, a.ld, w, offset);
                case NATIVE -> SYEVD.Native.invoke(vectors, n, a.data, a.offset, a.ld, w, offset);
                default     -> SYEVD.invoke(vectors, n, a.data, a.offset, a.ld, w, offset);
            };
        }

        String message = "SYEVD " + implementation + " tight=" + tight + " vectors=" + vectors + " separate=" + separate + " " + n;
        TestUtils.assertEquals(message + " info", 0, info);

        if (separate) {
            TestUtils.assertTrue(message + " A changed", Arrays.equals(original, a.data));
        }

        for (int p = 0; p < w.length; p++) {
            int i = p - offset;
            if (i < 0 || i >= n) {
                TestUtils.assertTrue(message + " outside w changed", w[p] == SENTINEL);
            } else if (i > 0) {
                TestUtils.assertTrue(message + " not ascending", w[p - 1] <= w[p]);
            }
        }

        // The trace and the Frobenius norm are those of the original
        double trace = 0.0;
        double sumOfSquares = 0.0;
        double sumOfValues = 0.0;
        double sumOfSquaredValues = 0.0;
        for (int j = 0; j < n; j++) {
            trace += LAPACKTest.symmetric(original, a, false, j, j);
            for (int i = 0; i < n; i++) {
                double value = LAPACKTest.symmetric(original, a, false, i, j);
                sumOfSquares += value * value;
            }
            sumOfValues += w[offset + j];
            sumOfSquaredValues += w[offset + j] * w[offset + j];
        }
        BLASLevel3Test.assertArray(message + " trace and norm", new double[] { trace, sumOfSquares }, new double[] { sumOfValues, sumOfSquaredValues });

        if (vectors) {

            LAPACKTest.assertOutsideUnchanged(message, beforeZ, z, n, n);

            double[] identity = new double[n * n];
            double[] product = new double[n * n];
            double[] expected = new double[n * n];
            double[] actual = new double[n * n];
            for (int j = 0; j < n; j++) {
                for (int i = 0; i < n; i++) {
                    double ztz = 0.0;
                    double az = 0.0;
                    for (int p = 0; p < n; p++) {
                        ztz += z.get(p, i) * z.get(p, j);
                        az += LAPACKTest.symmetric(original, a, false, i, p) * z.get(p, j);
                    }
                    identity[i + j * n] = i == j ? 1.0 : 0.0;
                    product[i + j * n] = ztz;
                    expected[i + j * n] = z.get(i, j) * w[offset + j];
                    actual[i + j * n] = az;
                }
            }
            BLASLevel3Test.assertArray(message + " Z^T*Z", identity, product);
            BLASLevel3Test.assertArray(message + " A*Z", expected, actual);
        }
    }

    static void doTestSYTRD(final Implementation implementation, final boolean tight, final int n) {

        Storage a = LAPACKTest.newSPD(new Random(n), n, false, tight);
        double[] original = a.data.clone();
        int offset = tight ? 0 : OFFSET;
        double[] d = LAPACKTest.newTau(n, tight);
        double[] e = LAPACKTest.newTau(Math.max(0, n - 1), tight);
        double[] tau = LAPACKTest.newTau(Math.max(0, n - 1), tight);

        switch (implementation) {
            case JAVA   -> SYTRD.JVM.invoke(n, a.data, a.offset, a.ld, d, offset, e, offset, tau, offset);
            case NATIVE -> SYTRD.Native.invoke(n, a.data, a.offset, a.ld, d, offset, e, offset, tau, offset);
            default     -> SYTRD.invoke(n, a.data, a.offset, a.ld, d, offset, e, offset, tau, offset);
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

    static void doTestTREVC(final boolean tight, final int n) {

        Random random = new Random(n);
        Storage t = new Storage(random, n, n, tight);
        double[] hessenberg = new double[n * n];
        for (int j = 0; j < n; j++) {
            for (int i = 0; i <= Math.min(j + 1, n - 1); i++) {
                hessenberg[i + j * n] = t.get(i, j);
            }
        }
        int offset = tight ? 0 : OFFSET;
        double[] wr = LAPACKTest.newTau(n, tight);
        double[] wi = LAPACKTest.newTau(n, tight);
        Storage z = new Storage(random, n, n, tight);
        for (int j = 0; j < n; j++) {
            for (int i = 0; i < n; i++) {
                z.set(i, j, i == j ? 1.0 : 0.0);
            }
        }
        HSEQR.JVM.invoke(true, true, n, t.data, t.offset, t.ld, wr, offset, wi, offset, z.data, z.offset, z.ld);
        double[] beforeT = t.data.clone();
        double[] beforeZ = z.data.clone();
        double[] eigenvalues = wr.clone();

        TREVC.invoke(n, t.data, t.offset, t.ld, wr, offset, wi, offset, z.data, z.offset, z.ld);

        String message = "TREVC tight=" + tight + " " + n;
        BLASLevel3Test.assertUnchanged(message + " wr", eigenvalues, wr);
        LAPACKTest.assertOutsideUnchanged(message, beforeT, t, n, n);
        LAPACKTest.assertOutsideUnchanged(message + " VR", beforeZ, z, n, n);
        LAPACKTest.assertEigenvectors(message, hessenberg, n, wr, wi, offset, z);
    }

    static void doTestTRTRS(final Implementation implementation, final boolean tight, final boolean upper, final boolean transpose, final boolean unit,
            final int n, final int nrhs) {

        Random random = new Random(n + 31 * nrhs);

        Storage a = LAPACKTest.newTriangular(random, n, upper, unit, tight);
        double[] matrix = a.data.clone();

        Storage b = new Storage(random, n, nrhs, tight);
        double[] rhs = b.data.clone();

        int info = switch (implementation) {
            case JAVA             -> TRTRS.JVM.invoke(upper, transpose, unit, n, nrhs, a.data, a.offset, a.ld, b.data, b.offset, b.ld);
            case NATIVE, OFF_HEAP -> {
                try (OffHeapLimits limits = OffHeapLimits.of(implementation)) {
                    yield TRTRS.Native.invoke(upper, transpose, unit, n, nrhs, a.data, a.offset, a.ld, b.data, b.offset, b.ld);
                }
            }
            default               -> TRTRS.invoke(upper, transpose, unit, n, nrhs, a.data, a.offset, a.ld, b.data, b.offset, b.ld);
        };

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
    public void testGEBRD() {
        for (Implementation implementation : Implementation.values()) {
            if (LAPACKTest.isAvailable(implementation, GEBRD.NATIVE, false)) {
                for (boolean tight : BOOLEANS) {
                    for (int[] size : SIZES_GEBRD) {
                        LAPACKTest.doTestGEBRD(implementation, tight, size[0], size[1]);
                    }
                }
            }
        }
    }

    @Test
    public void testGEBRDArgumentChecks() {

        double[] data = new double[16];
        double[] vector = new double[4];

        TestUtils.assertThrows(IllegalArgumentException.class, () -> GEBRD.invoke(4, -1, data, 0, 4, vector, 0, vector, 0, vector, 0, vector, 0));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> GEBRD.invoke(3, 4, data, 0, 3, vector, 0, vector, 0, vector, 0, vector, 0));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> GEBRD.invoke(4, 4, data, 0, 3, vector, 0, vector, 0, vector, 0, vector, 0));

        // Nothing to reduce, nothing is accessed
        GEBRD.invoke(0, 0, new double[0], 0, 1, new double[0], 0, new double[0], 0, new double[0], 0, new double[0], 0);

        if (GEBRD.NATIVE) {

            Storage a = new Storage(new Random(4), 4, 4, true);
            double[] before = a.data.clone();

            // Detected before the native function is called
            TestUtils.assertThrows(IndexOutOfBoundsException.class,
                    () -> GEBRD.Native.invoke(4, 4, a.data, 0, 4, new double[3], 0, vector, 0, vector, 0, vector, 0));
            TestUtils.assertThrows(IndexOutOfBoundsException.class,
                    () -> GEBRD.Native.invoke(4, 4, a.data, 0, 4, vector, 0, new double[2], 0, vector, 0, vector, 0));
            TestUtils.assertThrows(IndexOutOfBoundsException.class,
                    () -> GEBRD.Native.invoke(4, 4, a.data, 0, 4, vector, 0, vector, 0, new double[3], 0, vector, 0));
            TestUtils.assertThrows(IndexOutOfBoundsException.class,
                    () -> GEBRD.Native.invoke(4, 4, a.data, 0, 4, vector, 0, vector, 0, vector, 0, new double[3], 0));
            TestUtils.assertThrows(IndexOutOfBoundsException.class,
                    () -> GEBRD.Native.invoke(4, 4, new double[15], 0, 4, vector, 0, vector, 0, vector, 0, vector, 0));

            TestUtils.assertTrue("A changed", Arrays.equals(before, a.data));
        }
    }

    @Test
    public void testGEEV() {
        for (Implementation implementation : Implementation.values()) {
            if (LAPACKTest.isAvailable(implementation, GEEV.NATIVE, false)) {
                for (boolean tight : BOOLEANS) {
                    for (int n : SIZES_GEEV) {
                        LAPACKTest.doTestGEEV(implementation, tight, n);
                    }
                }
            }
        }
    }

    @Test
    public void testGEEVArgumentChecks() {

        double[] data = new double[16];
        double[] w = new double[4];

        TestUtils.assertThrows(IllegalArgumentException.class, () -> GEEV.invoke(true, -1, data, 0, 1, w, 0, w, 0, data, 0, 1));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> GEEV.invoke(true, 4, data, 0, 3, w, 0, w, 0, data, 0, 4));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> GEEV.invoke(true, 4, data, 0, 4, w, 0, w, 0, data, 0, 3));

        // Nothing to compute, nothing is accessed
        GEEV.invoke(true, 0, new double[0], 0, 1, new double[0], 0, new double[0], 0, new double[0], 0, 1);

        if (GEEV.NATIVE) {

            Storage a = new Storage(new Random(4), 4, 4, true);
            double[] before = a.data.clone();

            // Detected before the native function is called
            TestUtils.assertThrows(IndexOutOfBoundsException.class,
                    () -> GEEV.Native.invoke(true, 4, a.data, 0, 4, new double[3], 0, w, 0, new double[16], 0, 4));
            TestUtils.assertThrows(IndexOutOfBoundsException.class, () -> GEEV.Native.invoke(true, 4, new double[15], 0, 4, w, 0, w, 0, new double[16], 0, 4));

            TestUtils.assertTrue("A changed", Arrays.equals(before, a.data));
        }
    }

    @Test
    public void testGEHRD() {
        for (Implementation implementation : Implementation.values()) {
            if (LAPACKTest.isAvailable(implementation, GEHRD.NATIVE, false)) {
                for (boolean tight : BOOLEANS) {
                    for (int n : SIZES_SYTRD) {
                        LAPACKTest.doTestGEHRD(implementation, tight, n);
                    }
                }
            }
        }
    }

    @Test
    public void testGEHRDArgumentChecks() {

        double[] data = new double[16];
        double[] tau = new double[3];

        TestUtils.assertThrows(IllegalArgumentException.class, () -> GEHRD.invoke(-1, data, 0, 1, tau, 0));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> GEHRD.invoke(4, data, 0, 3, tau, 0));

        // Nothing to reduce, nothing is accessed
        GEHRD.invoke(0, new double[0], 0, 1, new double[0], 0);

        if (GEHRD.NATIVE) {

            Storage a = new Storage(new Random(4), 4, 4, true);
            double[] before = a.data.clone();

            // Detected before the native function is called
            TestUtils.assertThrows(IndexOutOfBoundsException.class, () -> GEHRD.Native.invoke(4, a.data, 0, 4, new double[2], 0));
            TestUtils.assertThrows(IndexOutOfBoundsException.class, () -> GEHRD.Native.invoke(4, new double[15], 0, 4, tau, 0));

            TestUtils.assertTrue("A changed", Arrays.equals(before, a.data));
        }
    }

    @Test
    public void testGEQRF() {
        for (Implementation implementation : Implementation.values()) {
            if (LAPACKTest.isAvailable(implementation, GEQRF.NATIVE, false)) {
                for (boolean tight : BOOLEANS) {
                    for (int[] size : SIZES) {
                        LAPACKTest.doTestGEQRF(implementation, tight, size[0], size[1]);
                    }
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

        if (GEQRF.NATIVE) {

            Storage a = new Storage(new Random(4), 4, 4, true);
            double[] before = a.data.clone();

            // Detected before the native function is called
            TestUtils.assertThrows(IndexOutOfBoundsException.class, () -> GEQRF.Native.invoke(4, 4, a.data, 0, 4, new double[3], 0));
            TestUtils.assertThrows(IndexOutOfBoundsException.class, () -> GEQRF.Native.invoke(4, 4, new double[15], 0, 4, tau, 0));

            TestUtils.assertTrue("A changed", Arrays.equals(before, a.data));
        }
    }

    /**
     * A zero column gives tau = 0 (no reflection), and the factorisation is completed.
     */
    @Test
    public void testGEQRFZeroColumn() {
        for (Implementation implementation : Implementation.values()) {
            if (LAPACKTest.isAvailable(implementation, GEQRF.NATIVE, false)) {
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
    }

    @Test
    public void testGESDD() {
        for (Implementation implementation : Implementation.values()) {
            if (LAPACKTest.isAvailable(implementation, GESDD.NATIVE, false)) {
                for (boolean tight : BOOLEANS) {
                    for (boolean vectors : BOOLEANS) {
                        for (boolean fullSize : BOOLEANS) {
                            if (vectors || !fullSize) {
                                for (int[] size : SIZES) {
                                    LAPACKTest.doTestGESDD(implementation, tight, vectors, fullSize, size[0], size[1]);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    public void testGESDDArgumentChecks() {

        double[] data = new double[16];
        double[] vector = new double[4];

        TestUtils.assertThrows(IllegalArgumentException.class, () -> GESDD.invoke(true, false, -1, 4, data, 0, 1, vector, 0, data, 0, 1, data, 0, 4));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> GESDD.invoke(true, false, 4, -1, data, 0, 4, vector, 0, data, 0, 4, data, 0, 1));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> GESDD.invoke(true, false, 4, 4, data, 0, 3, vector, 0, data, 0, 4, data, 0, 4));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> GESDD.invoke(true, false, 4, 4, data, 0, 4, vector, 0, data, 0, 3, data, 0, 4));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> GESDD.invoke(true, false, 4, 4, data, 0, 4, vector, 0, data, 0, 4, data, 0, 3));

        // The singular values only, U and V are not referenced
        TestUtils.assertEquals(0, GESDD.invoke(false, false, 4, 4, data.clone(), 0, 4, vector, 0, null, 0, 1, null, 0, 1));

        // Nothing to decompose, nothing is accessed
        TestUtils.assertEquals(0, GESDD.invoke(true, false, 0, 4, new double[0], 0, 1, new double[0], 0, new double[0], 0, 1, new double[0], 0, 4));

        if (GESDD.NATIVE) {

            Storage a = new Storage(new Random(4), 4, 4, true);
            double[] before = a.data.clone();
            double[] matrix = new double[16];

            // Detected before the native function is called
            TestUtils.assertThrows(IndexOutOfBoundsException.class,
                    () -> GESDD.Native.invoke(true, false, 4, 4, a.data, 0, 4, new double[3], 0, matrix, 0, 4, matrix, 0, 4));
            TestUtils.assertThrows(IndexOutOfBoundsException.class,
                    () -> GESDD.Native.invoke(true, false, 4, 4, a.data, 0, 4, vector, 0, new double[15], 0, 4, matrix, 0, 4));
            TestUtils.assertThrows(IndexOutOfBoundsException.class,
                    () -> GESDD.Native.invoke(true, false, 4, 4, a.data, 0, 4, vector, 0, matrix, 0, 4, new double[15], 0, 4));
            TestUtils.assertThrows(IndexOutOfBoundsException.class,
                    () -> GESDD.Native.invoke(true, false, 4, 4, new double[15], 0, 4, vector, 0, matrix, 0, 4, matrix, 0, 4));
            TestUtils.assertThrows(IndexOutOfBoundsException.class,
                    () -> GESDD.Native.invoke(true, false, 3, 5, new double[14], 0, 3, new double[3], 0, matrix, 0, 3, matrix, 0, 5));

            TestUtils.assertTrue("A changed", Arrays.equals(before, a.data));
        }
    }

    @Test
    public void testGETRF() {
        for (Implementation implementation : Implementation.values()) {
            if (LAPACKTest.isAvailable(implementation, GETRF.NATIVE, false)) {
                for (boolean tight : BOOLEANS) {
                    for (int[] size : SIZES) {
                        LAPACKTest.doTestGETRF(implementation, tight, size[0], size[1]);
                    }
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

        if (GETRF.NATIVE) {

            Storage a = new Storage(new Random(4), 4, 4, true);
            double[] before = a.data.clone();
            int[] tooShort = new int[3];

            // Detected before the native function is called
            TestUtils.assertThrows(IndexOutOfBoundsException.class, () -> GETRF.Native.invoke(4, 4, a.data, 0, 4, tooShort, 0));
            TestUtils.assertThrows(IndexOutOfBoundsException.class, () -> GETRF.Native.invoke(4, 4, new double[15], 0, 4, ipiv, 0));

            TestUtils.assertTrue("A changed", Arrays.equals(before, a.data));
        }
    }

    /**
     * An exactly zero pivot is reported (1-based), and the factorisation is completed.
     */
    @Test
    public void testGETRFSingular() {
        for (Implementation implementation : Implementation.values()) {
            if (LAPACKTest.isAvailable(implementation, GETRF.NATIVE, false)) {
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
            if (LAPACKTest.isAvailable(implementation, GETRS.NATIVE, true)) {
                for (boolean tight : BOOLEANS) {
                    for (boolean transpose : BOOLEANS) {
                        for (int[] size : SIZES_SOLVE) {
                            LAPACKTest.doTestGETRS(implementation, tight, transpose, size[0], size[1]);
                        }
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

        if (GETRS.NATIVE) {

            double[] b = { 1.0, 2.0, 3.0, 4.0 };

            // Detected before the native function is called
            TestUtils.assertThrows(IndexOutOfBoundsException.class, () -> GETRS.Native.invoke(false, 4, 1, data, 0, 4, new int[3], 0, b, 0, 4));
            TestUtils.assertThrows(IndexOutOfBoundsException.class, () -> GETRS.Native.invoke(false, 4, 2, data, 0, 4, ipiv, 0, b, 0, 4));
            try (OffHeapLimits limits = OffHeapLimits.of(Implementation.OFF_HEAP)) {
                TestUtils.assertThrows(IndexOutOfBoundsException.class, () -> GETRS.Native.invoke(false, 4, 1, data, 0, 4, new int[3], 0, b, 0, 4));
                TestUtils.assertThrows(IndexOutOfBoundsException.class, () -> GETRS.Native.invoke(false, 4, 2, data, 0, 4, ipiv, 0, b, 0, 4));
            }

            TestUtils.assertTrue("B changed", Arrays.equals(new double[] { 1.0, 2.0, 3.0, 4.0 }, b));

            // The native implementation, reached through the dispatch, with too few pivots
            TestUtils.assertThrows(IndexOutOfBoundsException.class, () -> GETRS.invokeIdentity(8, new double[64], 0, 8, new int[7], 0, new double[64], 0, 8));
        }
    }

    /**
     * The inverse, written to a separate matrix. The native implementation is reached through the dispatch,
     * for n larger than 7.
     */
    @Test
    public void testGETRSIdentity() {
        for (Implementation implementation : Implementation.values()) {
            if (LAPACKTest.isAvailable(implementation, false, false)) {
                for (boolean tight : BOOLEANS) {
                    for (int n : SIZES_IDENTITY) {
                        LAPACKTest.doTestGETRSIdentity(implementation, tight, n);
                    }
                }
            }
        }
    }

    /**
     * Economy size (n = k = min(m,n) of the factorisation) and full size (n = m) Q.
     */
    @Test
    public void testHSEQR() {
        for (boolean tight : BOOLEANS) {
            for (int n : SIZES_GEEV) {
                LAPACKTest.doTestHSEQR(tight, n);
            }
        }
    }

    @Test
    public void testHSEQRArgumentChecks() {

        double[] data = new double[16];
        double[] w = new double[4];

        TestUtils.assertThrows(IllegalArgumentException.class, () -> HSEQR.invoke(true, true, -1, data, 0, 1, w, 0, w, 0, data, 0, 1));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> HSEQR.invoke(true, true, 4, data, 0, 3, w, 0, w, 0, data, 0, 4));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> HSEQR.invoke(true, true, 4, data, 0, 4, w, 0, w, 0, data, 0, 3));
        // The Schur vectors require the Schur form
        TestUtils.assertThrows(IllegalArgumentException.class, () -> HSEQR.invoke(false, true, 4, data, 0, 4, w, 0, w, 0, data, 0, 4));

        // Nothing to compute, nothing is accessed
        TestUtils.assertEquals(0, HSEQR.invoke(true, true, 0, new double[0], 0, 1, new double[0], 0, new double[0], 0, new double[0], 0, 1));
    }

    @Test
    public void testORGBR() {
        for (Implementation implementation : Implementation.values()) {
            if (LAPACKTest.isAvailable(implementation, ORGBR.NATIVE, false)) {
                for (boolean tight : BOOLEANS) {
                    for (int[] size : SIZES_GEBRD) {
                        LAPACKTest.doTestORGBR(implementation, tight, true, false, size[0], size[1]);
                        LAPACKTest.doTestORGBR(implementation, tight, true, true, size[0], size[1]);
                        LAPACKTest.doTestORGBR(implementation, tight, false, false, size[0], size[1]);
                    }
                }
            }
        }
    }

    @Test
    public void testORGBRArgumentChecks() {

        double[] data = new double[16];
        double[] tau = new double[4];

        TestUtils.assertThrows(IllegalArgumentException.class, () -> ORGBR.invoke(true, 4, -1, 0, data, 0, 4, tau, 0));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> ORGBR.invoke(true, 3, 4, 4, data, 0, 3, tau, 0));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> ORGBR.invoke(true, 4, 3, 4, data, 0, 4, tau, 0));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> ORGBR.invoke(false, 4, 3, 4, data, 0, 4, tau, 0));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> ORGBR.invoke(false, 4, 4, 3, data, 0, 4, tau, 0));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> ORGBR.invoke(true, 4, 4, 4, data, 0, 3, tau, 0));

        // Nothing to generate, nothing is accessed
        ORGBR.invoke(true, 0, 0, 0, new double[0], 0, 1, new double[0], 0);

        if (ORGBR.NATIVE) {

            Storage a = new Storage(new Random(4), 4, 4, true);
            double[] before = a.data.clone();

            // Detected before the native function is called
            TestUtils.assertThrows(IndexOutOfBoundsException.class, () -> ORGBR.Native.invoke(true, 4, 4, 4, a.data, 0, 4, new double[3], 0));
            TestUtils.assertThrows(IndexOutOfBoundsException.class, () -> ORGBR.Native.invoke(false, 4, 4, 4, a.data, 0, 4, new double[3], 0));
            TestUtils.assertThrows(IndexOutOfBoundsException.class, () -> ORGBR.Native.invoke(true, 4, 4, 4, new double[15], 0, 4, tau, 0));

            TestUtils.assertTrue("A changed", Arrays.equals(before, a.data));
        }
    }

    @Test
    public void testORGHR() {
        for (Implementation implementation : Implementation.values()) {
            if (LAPACKTest.isAvailable(implementation, ORGHR.NATIVE, false)) {
                for (boolean tight : BOOLEANS) {
                    for (int n : SIZES_SYTRD) {
                        LAPACKTest.doTestORGHR(implementation, tight, n);
                    }
                }
            }
        }
    }

    @Test
    public void testORGHRArgumentChecks() {

        double[] data = new double[16];
        double[] tau = new double[3];

        TestUtils.assertThrows(IllegalArgumentException.class, () -> ORGHR.invoke(-1, data, 0, 1, tau, 0));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> ORGHR.invoke(4, data, 0, 3, tau, 0));

        // Nothing to generate, nothing is accessed
        ORGHR.invoke(0, new double[0], 0, 1, new double[0], 0);

        if (ORGHR.NATIVE) {

            Storage a = new Storage(new Random(4), 4, 4, true);
            double[] before = a.data.clone();

            // Detected before the native function is called
            TestUtils.assertThrows(IndexOutOfBoundsException.class, () -> ORGHR.Native.invoke(4, a.data, 0, 4, new double[2], 0));
            TestUtils.assertThrows(IndexOutOfBoundsException.class, () -> ORGHR.Native.invoke(4, new double[15], 0, 4, tau, 0));

            TestUtils.assertTrue("A changed", Arrays.equals(before, a.data));
        }
    }

    @Test
    public void testORGQR() {
        for (Implementation implementation : Implementation.values()) {
            if (LAPACKTest.isAvailable(implementation, ORGQR.NATIVE, false)) {
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

        if (ORGQR.NATIVE) {

            Storage a = new Storage(new Random(4), 4, 4, true);
            double[] before = a.data.clone();

            // Detected before the native function is called
            TestUtils.assertThrows(IndexOutOfBoundsException.class, () -> ORGQR.Native.invoke(4, 4, 4, a.data, 0, 4, new double[3], 0));
            TestUtils.assertThrows(IndexOutOfBoundsException.class, () -> ORGQR.Native.invoke(4, 4, 4, new double[15], 0, 4, tau, 0));

            TestUtils.assertTrue("A changed", Arrays.equals(before, a.data));
        }
    }

    @Test
    public void testORGTR() {
        for (Implementation implementation : Implementation.values()) {
            if (LAPACKTest.isAvailable(implementation, ORGTR.NATIVE, false)) {
                for (boolean tight : BOOLEANS) {
                    for (int n : SIZES_SYTRD) {
                        LAPACKTest.doTestORGTR(implementation, tight, n);
                    }
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

        if (ORGTR.NATIVE) {

            Storage a = new Storage(new Random(4), 4, 4, true);
            double[] before = a.data.clone();

            // Detected before the native function is called
            TestUtils.assertThrows(IndexOutOfBoundsException.class, () -> ORGTR.Native.invoke(4, a.data, 0, 4, new double[2], 0));
            TestUtils.assertThrows(IndexOutOfBoundsException.class, () -> ORGTR.Native.invoke(4, new double[15], 0, 4, tau, 0));

            TestUtils.assertTrue("A changed", Arrays.equals(before, a.data));
        }
    }

    @Test
    public void testORMQR() {
        for (Implementation implementation : Implementation.values()) {
            if (LAPACKTest.isAvailable(implementation, false, false)) {
                for (boolean tight : BOOLEANS) {
                    for (boolean transpose : BOOLEANS) {
                        for (int[] size : SIZES_ORMQR) {
                            LAPACKTest.doTestORMQR(implementation, tight, transpose, size[0], size[1], size[2]);
                        }
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
    public void testPOTRF() {
        for (Implementation implementation : Implementation.values()) {
            if (LAPACKTest.isAvailable(implementation, POTRF.NATIVE, false)) {
                for (boolean tight : BOOLEANS) {
                    for (boolean upper : BOOLEANS) {
                        for (int n : SIZES_SPD) {
                            LAPACKTest.doTestPOTRF(implementation, tight, upper, n);
                        }
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

        if (POTRF.NATIVE) {
            // Detected before the native function is called
            TestUtils.assertThrows(IndexOutOfBoundsException.class, () -> POTRF.Native.invoke(false, 4, new double[15], 0, 4));
        }
    }

    /**
     * A leading minor that is not positive definite is reported, and the factorisation stopped there.
     */
    @Test
    public void testPOTRFNotPositiveDefinite() {
        for (Implementation implementation : Implementation.values()) {
            if (LAPACKTest.isAvailable(implementation, POTRF.NATIVE, false)) {
                for (boolean upper : BOOLEANS) {
                    for (int n : new int[] { 12, 60 }) {

                        Storage a = LAPACKTest.newSPD(new Random(n), n, upper, false);
                        a.set(3, 3, -n * n);

                        int info = switch (implementation) {
                            case JAVA   -> POTRF.JVM.invoke(upper, n, a.data, a.offset, a.ld);
                            case NATIVE -> POTRF.Native.invoke(upper, n, a.data, a.offset, a.ld);
                            default     -> POTRF.invoke(upper, n, a.data, a.offset, a.ld);
                        };

                        TestUtils.assertEquals("POTRF " + implementation + " upper=" + upper + " " + n, 4, info);
                    }
                }
            }
        }
    }

    @Test
    public void testPOTRI() {
        for (Implementation implementation : Implementation.values()) {
            if (LAPACKTest.isAvailable(implementation, false, false)) {
                for (boolean tight : BOOLEANS) {
                    for (boolean upper : BOOLEANS) {
                        for (int n : SIZES_SPD) {
                            LAPACKTest.doTestPOTRI(implementation, tight, upper, n);
                        }
                    }
                }
            }
        }
    }

    @Test
    public void testPOTRIArgumentChecks() {

        double[] data = new double[16];

        TestUtils.assertThrows(IllegalArgumentException.class, () -> POTRI.invoke(false, -1, data, 0, 1));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> POTRI.invoke(true, 4, data, 0, 3));

        // Nothing to invert, nothing is accessed
        TestUtils.assertEquals(0, POTRI.invoke(false, 0, new double[0], 0, 1));
    }

    /**
     * A zero on the diagonal of the factor is reported, and A left unchanged.
     */
    @Test
    public void testPOTRISingular() {
        for (boolean upper : BOOLEANS) {
            for (int n : new int[] { 12, 60 }) {

                Storage a = LAPACKTest.newSPD(new Random(n), n, upper, false);
                POTRF.JVM.invoke(upper, n, a.data, a.offset, a.ld);
                a.set(3, 3, 0.0);
                double[] before = a.data.clone();

                String message = "POTRI upper=" + upper + " " + n;
                TestUtils.assertEquals(message, 4, POTRI.invoke(upper, n, a.data, a.offset, a.ld));
                BLASLevel3Test.assertUnchanged(message, before, a.data);
            }
        }
    }

    @Test
    public void testPOTRS() {
        for (Implementation implementation : Implementation.values()) {
            if (LAPACKTest.isAvailable(implementation, POTRS.NATIVE, true)) {
                for (boolean tight : BOOLEANS) {
                    for (boolean upper : BOOLEANS) {
                        for (int[] size : SIZES_SOLVE) {
                            LAPACKTest.doTestPOTRS(implementation, tight, upper, size[0], size[1]);
                        }
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

        if (POTRS.NATIVE) {

            double[] b = { 1.0, 2.0, 3.0, 4.0 };

            // Detected before the native function is called
            TestUtils.assertThrows(IndexOutOfBoundsException.class, () -> POTRS.Native.invoke(false, 4, 2, data, 0, 4, b, 0, 4));
            TestUtils.assertThrows(IndexOutOfBoundsException.class, () -> POTRS.Native.invoke(false, 4, 1, new double[15], 0, 4, b, 0, 4));
            try (OffHeapLimits limits = OffHeapLimits.of(Implementation.OFF_HEAP)) {
                TestUtils.assertThrows(IndexOutOfBoundsException.class, () -> POTRS.Native.invoke(false, 4, 2, data, 0, 4, b, 0, 4));
                TestUtils.assertThrows(IndexOutOfBoundsException.class, () -> POTRS.Native.invoke(false, 4, 1, new double[15], 0, 4, b, 0, 4));
            }

            TestUtils.assertTrue("B changed", Arrays.equals(new double[] { 1.0, 2.0, 3.0, 4.0 }, b));
        }
    }

    /**
     * The inverse, written to a separate matrix, exactly symmetric. The native implementation is reached
     * through the dispatch, for n larger than 3.
     */
    @Test
    public void testPOTRSIdentity() {
        for (Implementation implementation : Implementation.values()) {
            if (LAPACKTest.isAvailable(implementation, false, false)) {
                for (boolean tight : BOOLEANS) {
                    for (boolean upper : BOOLEANS) {
                        for (int n : SIZES_IDENTITY_SPD) {
                            LAPACKTest.doTestPOTRSIdentity(implementation, tight, upper, n);
                        }
                    }
                }
            }
        }
    }

    @Test
    public void testSTEQR() {
        for (int n : new int[] { 1, 2, 3, 7, 17, 40, 150 }) {
            Random random = new Random(n);
            double[] d = new double[n];
            double[] e = new double[n];
            for (int i = 0; i < n; i++) {
                d[i] = 2.0 * random.nextDouble() - 1.0;
                e[i] = i + 1 < n ? 2.0 * random.nextDouble() - 1.0 : 0.0;
            }
            LAPACKTest.doTestSTEQR("STEQR " + n, d, e);
        }
    }

    /**
     * Blocks that split off as 2-by-2, solved in closed form: random ones, and with a = c, a = -c, a = c = 0,
     * a tiny b, negative and mixed signs.
     */
    @Test
    public void testSTEQR2x2() {

        double[][] blocks = { { 0.3, -0.7, 0.9 }, { 0.5, 0.25, 0.5 }, { -0.4, 0.6, 0.4 }, { 0.0, 1.0, 0.0 }, { 1.0, 1E-9, 2.0 }, { -2.0, -0.5, -1.0 },
                { 3.0, -2.0, -1.0 }, { -1.0, 4.0, 3.0 }, { 2.0, 1E-15, 2.0 }, { -0.1, 0.1, -0.1 } };

        for (double[] block : blocks) {
            LAPACKTest.doTestSTEQR("STEQR 2x2 " + Arrays.toString(block), new double[] { block[0], block[2] }, new double[] { block[1], 0.0 });
        }

        // All of them, as the diagonal blocks of one matrix: each splits off in turn
        int n = 2 * blocks.length;
        double[] d = new double[n];
        double[] e = new double[n];
        for (int k = 0; k < blocks.length; k++) {
            d[2 * k] = blocks[k][0];
            e[2 * k] = blocks[k][1];
            d[2 * k + 1] = blocks[k][2];
        }
        LAPACKTest.doTestSTEQR("STEQR 2x2 blocks", d, e);
    }

    /**
     * Scaled to where the squares in the plane rotations overflow or underflow, around their square roots,
     * and with elements of very different magnitude.
     */
    @Test
    public void testSTEQRScaled() {

        int n = 17;

        for (double scale : new double[] { 1E-300, 1E-154, 1E154, 1E300 }) {
            Random random = new Random(n);
            double[] d = new double[n];
            double[] e = new double[n];
            for (int i = 0; i < n; i++) {
                d[i] = scale * (2.0 * random.nextDouble() - 1.0);
                e[i] = i + 1 < n ? scale * (2.0 * random.nextDouble() - 1.0) : 0.0;
            }
            LAPACKTest.doTestSTEQR("STEQR scale " + scale, d, e);
        }

        Random random = new Random(n);
        double[] d = new double[n];
        double[] e = new double[n];
        for (int i = 0; i < n; i++) {
            d[i] = Math.pow(10.0, 16 * i - 130) * (2.0 * random.nextDouble() - 1.0);
            e[i] = i + 1 < n ? Math.pow(10.0, 16 * i - 122) * (2.0 * random.nextDouble() - 1.0) : 0.0;
        }
        LAPACKTest.doTestSTEQR("STEQR graded", d, e);
    }

    @Test
    public void testSYEVD() {
        for (Implementation implementation : Implementation.values()) {
            if (LAPACKTest.isAvailable(implementation, SYEVD.NATIVE, false)) {
                for (boolean tight : BOOLEANS) {
                    for (boolean vectors : BOOLEANS) {
                        for (int n : SIZES_SYTRD) {
                            LAPACKTest.doTestSYEVD(implementation, tight, vectors, false, n);
                            if (implementation != Implementation.JAVA) {
                                LAPACKTest.doTestSYEVD(implementation, tight, vectors, true, n);
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    public void testSYEVDArgumentChecks() {

        double[] data = new double[16];
        double[] vector = new double[4];

        TestUtils.assertThrows(IllegalArgumentException.class, () -> SYEVD.invoke(true, -1, data, 0, 1, vector, 0));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> SYEVD.invoke(true, 4, data, 0, 3, vector, 0));

        // Nothing to decompose, nothing is accessed
        TestUtils.assertEquals(0, SYEVD.invoke(true, 0, new double[0], 0, 1, new double[0], 0));

        if (SYEVD.NATIVE) {

            Storage a = new Storage(new Random(4), 4, 4, true);
            double[] before = a.data.clone();

            // Detected before the native function is called
            TestUtils.assertThrows(IndexOutOfBoundsException.class, () -> SYEVD.Native.invoke(true, 4, a.data, 0, 4, new double[3], 0));
            TestUtils.assertThrows(IndexOutOfBoundsException.class, () -> SYEVD.Native.invoke(true, 4, new double[15], 0, 4, vector, 0));

            TestUtils.assertTrue("A changed", Arrays.equals(before, a.data));
        }
    }

    @Test
    public void testSYTRD() {
        for (Implementation implementation : Implementation.values()) {
            if (LAPACKTest.isAvailable(implementation, SYTRD.NATIVE, false)) {
                for (boolean tight : BOOLEANS) {
                    for (int n : SIZES_SYTRD) {
                        LAPACKTest.doTestSYTRD(implementation, tight, n);
                    }
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

        if (SYTRD.NATIVE) {

            Storage a = new Storage(new Random(4), 4, 4, true);
            double[] before = a.data.clone();

            // Detected before the native function is called
            TestUtils.assertThrows(IndexOutOfBoundsException.class, () -> SYTRD.Native.invoke(4, a.data, 0, 4, new double[3], 0, vector, 0, vector, 0));
            TestUtils.assertThrows(IndexOutOfBoundsException.class, () -> SYTRD.Native.invoke(4, a.data, 0, 4, vector, 0, new double[2], 0, vector, 0));
            TestUtils.assertThrows(IndexOutOfBoundsException.class, () -> SYTRD.Native.invoke(4, a.data, 0, 4, vector, 0, vector, 0, new double[2], 0));
            TestUtils.assertThrows(IndexOutOfBoundsException.class, () -> SYTRD.Native.invoke(4, new double[15], 0, 4, vector, 0, vector, 0, vector, 0));

            TestUtils.assertTrue("A changed", Arrays.equals(before, a.data));
        }
    }

    @Test
    public void testTREVC() {
        for (boolean tight : BOOLEANS) {
            for (int n : SIZES_GEEV) {
                LAPACKTest.doTestTREVC(tight, n);
            }
        }
    }

    @Test
    public void testTREVCArgumentChecks() {

        double[] data = new double[16];
        double[] w = new double[4];

        TestUtils.assertThrows(IllegalArgumentException.class, () -> TREVC.invoke(-1, data, 0, 1, w, 0, w, 0, data, 0, 1));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> TREVC.invoke(4, data, 0, 3, w, 0, w, 0, data, 0, 4));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> TREVC.invoke(4, data, 0, 4, w, 0, w, 0, data, 0, 3));

        // Nothing to compute, nothing is accessed
        TREVC.invoke(0, new double[0], 0, 1, new double[0], 0, new double[0], 0, new double[0], 0, 1);
    }

    @Test
    public void testTRTRS() {
        for (Implementation implementation : Implementation.values()) {
            if (LAPACKTest.isAvailable(implementation, TRTRS.NATIVE, true)) {
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

        if (TRTRS.NATIVE) {

            double[] a = new double[16];
            for (int i = 0; i < 4; i++) {
                a[i + i * 4] = 1.0;
            }
            double[] b = { 1.0, 2.0, 3.0, 4.0 };

            // Detected before the native function is called
            TestUtils.assertThrows(IndexOutOfBoundsException.class, () -> TRTRS.Native.invoke(true, false, false, 4, 2, a, 0, 4, b, 0, 4));
            TestUtils.assertThrows(IndexOutOfBoundsException.class, () -> TRTRS.Native.invoke(true, false, false, 4, 1, new double[15], 0, 4, b, 0, 4));
            try (OffHeapLimits limits = OffHeapLimits.of(Implementation.OFF_HEAP)) {
                TestUtils.assertThrows(IndexOutOfBoundsException.class, () -> TRTRS.Native.invoke(true, false, false, 4, 2, a, 0, 4, b, 0, 4));
                TestUtils.assertThrows(IndexOutOfBoundsException.class, () -> TRTRS.Native.invoke(true, false, false, 4, 1, new double[15], 0, 4, b, 0, 4));
            }

            TestUtils.assertTrue("B changed", Arrays.equals(new double[] { 1.0, 2.0, 3.0, 4.0 }, b));
        }
    }

    /**
     * An exactly zero diagonal element is reported (1-based), and nothing is solved. With a unit diagonal,
     * the diagonal is not read.
     */
    @Test
    public void testTRTRSSingular() {
        for (Implementation implementation : Implementation.values()) {
            if (LAPACKTest.isAvailable(implementation, TRTRS.NATIVE, true)) {
                for (boolean upper : BOOLEANS) {
                    for (int nrhs : new int[] { 1, 5 }) {

                        int n = 12;
                        String message = "TRTRS singular " + implementation + " upper=" + upper + " " + n + "x" + nrhs;

                        Storage a = LAPACKTest.newTriangular(new Random(n), n, upper, false, false);
                        a.set(4, 4, 0.0);
                        a.set(7, 7, 0.0);

                        Storage b = new Storage(new Random(nrhs), n, nrhs, false);
                        double[] rhs = b.data.clone();

                        int info = switch (implementation) {
                            case JAVA             -> TRTRS.JVM.invoke(upper, false, false, n, nrhs, a.data, a.offset, a.ld, b.data, b.offset, b.ld);
                            case NATIVE, OFF_HEAP -> {
                                try (OffHeapLimits limits = OffHeapLimits.of(implementation)) {
                                    yield TRTRS.Native.invoke(upper, false, false, n, nrhs, a.data, a.offset, a.ld, b.data, b.offset, b.ld);
                                }
                            }
                            default               -> TRTRS.invoke(upper, false, false, n, nrhs, a.data, a.offset, a.ld, b.data, b.offset, b.ld);
                        };

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

}

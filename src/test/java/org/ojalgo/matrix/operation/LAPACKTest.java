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
 * The LAPACK operations ({@link GETRF}, {@link GETRS} and {@link GETRI}) against their definitions: the
 * factors multiplied back, and the row interchanges undone, give the original matrix; the solution multiplied
 * by the matrix (or its transpose) gives the right hand side; the inverse multiplied by the matrix gives the
 * identity. Matrices with offsets and padding as well as matrices filling their arrays exactly. Each case is
 * run through the pure Java implementation, the dispatching {@code invoke}, and the native implementation(s)
 * when there is one (run with {@code --enable-native-access=ojalgo,ALL-UNNAMED} to include it). Elements
 * outside the matrices, and outside the range of the pivots, must be left untouched, and inputs must not
 * change.
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
     * n, for GETRI – the larger ones pass the native threshold, and the largest makes the Java implementation
     * multi-threaded.
     */
    private static final int[] SIZES_INVERSE = { 1, 2, 7, 17, 40, 80 };
    /**
     * n, nrhs, for GETRS – the larger ones pass the native threshold, and the largest makes the Java
     * implementation multi-threaded.
     */
    private static final int[][] SIZES_SOLVE = { { 1, 1 }, { 3, 1 }, { 6, 4 }, { 17, 1 }, { 17, 5 }, { 40, 1 }, { 40, 33 }, { 80, 70 } };

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

    private static int invokeGETRF(final Implementation implementation, final int m, final int n, final Storage a, final int[] ipiv, final int offsetIpiv) {
        return switch (implementation) {
            case JAVA   -> GETRF.JVM.invoke(m, n, a.data, a.offset, a.ld, ipiv, offsetIpiv);
            case NATIVE -> GETRF.Native.invoke(m, n, a.data, a.offset, a.ld, ipiv, offsetIpiv);
            default     -> GETRF.invoke(m, n, a.data, a.offset, a.ld, ipiv, offsetIpiv);
        };
    }

    private static int invokeGETRI(final Implementation implementation, final int n, final Storage a, final int[] ipiv, final int offsetIpiv) {
        return switch (implementation) {
            case JAVA   -> GETRI.JVM.invoke(n, a.data, a.offset, a.ld, ipiv, offsetIpiv);
            case NATIVE -> GETRI.Native.invoke(n, a.data, a.offset, a.ld, ipiv, offsetIpiv);
            default     -> GETRI.invoke(n, a.data, a.offset, a.ld, ipiv, offsetIpiv);
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

    static void doTestGETRI(final Implementation implementation, final boolean tight, final int n) {

        Storage a = new Storage(new Random(n), n, n, tight);
        double[] original = a.data.clone();
        int offsetIpiv = tight ? 0 : OFFSET;
        int[] ipiv = LAPACKTest.newPivots(n, tight);
        GETRF.JVM.invoke(n, n, a.data, a.offset, a.ld, ipiv, offsetIpiv);
        int[] pivots = ipiv.clone();

        int info = LAPACKTest.invokeGETRI(implementation, n, a, ipiv, offsetIpiv);

        String message = "GETRI " + implementation + " tight=" + tight + " " + n;
        TestUtils.assertEquals(message + " info", 0, info);
        TestUtils.assertEquals(message + " ipiv changed", pivots, ipiv);

        double[] expected = original.clone();
        double[] actual = a.data.clone();
        for (int j = 0; j < n; j++) {
            for (int i = 0; i < n; i++) {
                double sum = 0.0;
                for (int k = 0; k < n; k++) {
                    sum += original[a.index(i, k)] * a.get(k, j);
                }
                expected[a.index(i, j)] = i == j ? 1.0 : 0.0;
                actual[a.index(i, j)] = sum;
            }
        }
        BLASLevel3Test.assertArray(message, expected, actual);
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
            case JAVA     -> GETRS.JVM.invoke(transpose, n, nrhs, a.data, a.offset, a.ld, ipiv, offsetIpiv, b.data, b.offset, b.ld);
            case NATIVE   -> GETRS.Native.invoke(transpose, n, nrhs, a.data, a.offset, a.ld, ipiv, offsetIpiv, b.data, b.offset, b.ld);
            case OFF_HEAP -> GETRS.Native.invokeOffHeap(transpose, n, nrhs, a.data, a.offset, a.ld, ipiv, offsetIpiv, b.data, b.offset, b.ld);
            default       -> GETRS.invoke(transpose, n, nrhs, a.data, a.offset, a.ld, ipiv, offsetIpiv, b.data, b.offset, b.ld);
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

    @Test
    public void testGETRI() {
        for (Implementation implementation : Implementation.values()) {
            if (LAPACKTest.isAvailable(implementation, GETRI.NATIVE, false)) {
                for (boolean tight : BOOLEANS) {
                    for (int n : SIZES_INVERSE) {
                        LAPACKTest.doTestGETRI(implementation, tight, n);
                    }
                }
            }
        }
    }

    @Test
    public void testGETRIArgumentChecks() {

        double[] data = new double[16];
        int[] ipiv = { 1, 2, 3, 4 };

        TestUtils.assertThrows(IllegalArgumentException.class, () -> GETRI.invoke(-1, data, 0, 1, ipiv, 0));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> GETRI.invoke(4, data, 0, 3, ipiv, 0));

        // Nothing to invert, nothing is accessed
        TestUtils.assertEquals(0, GETRI.invoke(0, new double[0], 0, 1, new int[0], 0));

        if (GETRI.NATIVE) {

            Storage a = new Storage(new Random(4), 4, 4, true);
            double[] before = a.data.clone();

            // Detected before the native function is called
            TestUtils.assertThrows(IndexOutOfBoundsException.class, () -> GETRI.Native.invoke(4, a.data, 0, 4, new int[3], 0));
            // A zero on the diagonal is reported (as singular) before the rest of A is read – the copy has none
            TestUtils.assertThrows(IndexOutOfBoundsException.class, () -> GETRI.Native.invoke(4, Arrays.copyOf(a.data, 15), 0, 4, ipiv, 0));

            TestUtils.assertTrue("A changed", Arrays.equals(before, a.data));
        }
    }

    /**
     * An exactly zero diagonal element of U is reported (1-based), and A is left unchanged.
     */
    @Test
    public void testGETRISingular() {
        for (Implementation implementation : Implementation.values()) {
            if (LAPACKTest.isAvailable(implementation, GETRI.NATIVE, false)) {
                for (int n : new int[] { 12, 40 }) {

                    String message = "GETRI singular " + implementation + " " + n;

                    Storage a = new Storage(new Random(n), n, n, false);
                    for (int i = 0; i < n; i++) {
                        a.set(i, 3, 0.0);
                    }
                    int[] ipiv = LAPACKTest.newPivots(n, false);
                    TestUtils.assertEquals(message + " GETRF info", 4, GETRF.JVM.invoke(n, n, a.data, a.offset, a.ld, ipiv, OFFSET));
                    double[] factors = a.data.clone();

                    TestUtils.assertEquals(message + " info", 4, LAPACKTest.invokeGETRI(implementation, n, a, ipiv, OFFSET));
                    BLASLevel3Test.assertUnchanged(message, factors, a.data);
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

        // Nothing to solve, nothing is accessed
        GETRS.invoke(false, 0, 3, new double[0], 0, 1, new int[0], 0, new double[0], 0, 1);
        GETRS.invoke(true, 4, 0, new double[0], 0, 4, new int[0], 0, new double[0], 0, 4);

        if (GETRS.NATIVE) {

            double[] b = { 1.0, 2.0, 3.0, 4.0 };

            // Detected before the native function is called
            TestUtils.assertThrows(IndexOutOfBoundsException.class, () -> GETRS.Native.invoke(false, 4, 1, data, 0, 4, new int[3], 0, b, 0, 4));
            TestUtils.assertThrows(IndexOutOfBoundsException.class, () -> GETRS.Native.invoke(false, 4, 2, data, 0, 4, ipiv, 0, b, 0, 4));
            TestUtils.assertThrows(IndexOutOfBoundsException.class, () -> GETRS.Native.invokeOffHeap(false, 4, 1, data, 0, 4, new int[3], 0, b, 0, 4));
            TestUtils.assertThrows(IndexOutOfBoundsException.class, () -> GETRS.Native.invokeOffHeap(false, 4, 2, data, 0, 4, ipiv, 0, b, 0, 4));

            TestUtils.assertTrue("B changed", Arrays.equals(new double[] { 1.0, 2.0, 3.0, 4.0 }, b));
        }
    }

}

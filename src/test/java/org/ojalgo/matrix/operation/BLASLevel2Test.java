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

import java.util.Random;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.operation.BLASLevel3Test.Implementation;
import org.ojalgo.matrix.operation.BLASLevel3Test.Storage;

/**
 * The level 2 BLAS operations ({@link GEMV}, {@link GER} and {@link TRSV}) against a plain evaluation of
 * their definitions (for TRSV, the solution multiplied back): both transpose options, upper and lower, unit
 * diagonal or not, positive, negative and unit increments (a negative increment walks the vector backwards,
 * with the offset still the lowest index), alpha and beta 0, 1 and other, matrices with offsets and padding
 * as well as matrices filling their arrays exactly, and vector shapes. Each case is run through the pure Java
 * implementation, the dispatching {@code invoke}, and the native implementation when there is one (run with
 * {@code --enable-native-access=ojalgo,ALL-UNNAMED} to include it). Elements that are not part of the result
 * must be left untouched, inputs must not change, and a y that need not be set (beta 0) is filled with NaN.
 */
public class BLASLevel2Test {

    private static final double[] ALPHAS = { 0.0, 1.0, 0.7 };
    private static final double[] BETAS = { 0.0, 1.0, -0.3 };
    private static final boolean[] BOOLEANS = { false, true };
    private static final int[] INCREMENTS = { 1, 2, -1, -3 };
    private static final int OFFSET = 3;
    /**
     * m, n
     */
    private static final int[][] SIZES = { { 1, 1 }, { 1, 7 }, { 7, 1 }, { 3, 5 }, { 17, 9 }, { 40, 33 } };
    /**
     * n, for TRSV
     */
    private static final int[] SIZES_SQUARE = { 1, 2, 7, 17, 40, 130 };

    /**
     * The index of element i of a vector of n elements stored from offset with increment inc, as in BLAS.
     */
    private static int index(final int i, final int n, final int inc) {
        return inc >= 0 ? OFFSET + i * inc : OFFSET + (n - 1 - i) * -inc;
    }

    private static boolean isAvailable(final Implementation implementation, final boolean nativeAvailable) {
        return implementation != Implementation.OFF_HEAP && (implementation != Implementation.NATIVE || nativeAvailable);
    }

    private static double[] newVector(final Random random, final int n, final int inc) {
        double[] retVal = new double[OFFSET + Math.max(0, n - 1) * Math.abs(inc) + 1 + 2];
        for (int p = 0; p < retVal.length; p++) {
            retVal[p] = random.nextDouble() - 0.5;
        }
        return retVal;
    }

    static void doTestGEMV(final Implementation implementation, final boolean tight, final boolean transpose, final int m, final int n, final int incX,
            final int incY, final double alpha, final double beta) {

        Random random = new Random(m + 31 * n + 961 * incX + 29791 * incY);

        Storage a = new Storage(random, m, n, tight);
        int lengthX = transpose ? m : n;
        int lengthY = transpose ? n : m;
        double[] x = BLASLevel2Test.newVector(random, lengthX, incX);
        double[] y = BLASLevel2Test.newVector(random, lengthY, incY);

        double[] expected = y.clone();
        for (int i = 0; i < lengthY; i++) {
            double sum = 0.0;
            for (int l = 0; l < lengthX; l++) {
                sum += (transpose ? a.get(l, i) : a.get(i, l)) * x[BLASLevel2Test.index(l, lengthX, incX)];
            }
            int iy = BLASLevel2Test.index(i, lengthY, incY);
            expected[iy] = alpha * sum + (beta == 0.0 ? 0.0 : beta * y[iy]);
            if (beta == 0.0) {
                y[iy] = Double.NaN;
            }
        }

        double[] aBefore = a.data.clone();
        double[] xBefore = x.clone();

        switch (implementation) {
            case JAVA:
                GEMV.JVM.invoke(transpose, m, n, alpha, a.data, a.offset, a.ld, x, OFFSET, incX, beta, y, OFFSET, incY);
                break;
            case NATIVE:
                GEMV.Native.invoke(transpose, m, n, alpha, a.data, a.offset, a.ld, x, OFFSET, incX, beta, y, OFFSET, incY);
                break;
            default:
                GEMV.invoke(transpose, m, n, alpha, a.data, a.offset, a.ld, x, OFFSET, incX, beta, y, OFFSET, incY);
                break;
        }

        String message = "GEMV " + implementation + " tight=" + tight + " trans=" + transpose + " " + m + "x" + n + " incX=" + incX + " incY=" + incY
                + " alpha=" + alpha + " beta=" + beta;
        BLASLevel3Test.assertArray(message, expected, y);
        BLASLevel3Test.assertUnchanged(message, aBefore, a.data);
        BLASLevel3Test.assertUnchanged(message, xBefore, x);
    }

    static void doTestGER(final Implementation implementation, final boolean tight, final int m, final int n, final int incX, final int incY,
            final double alpha) {

        Random random = new Random(m + 31 * n + 961 * incX + 29791 * incY);

        Storage a = new Storage(random, m, n, tight);
        double[] x = BLASLevel2Test.newVector(random, m, incX);
        double[] y = BLASLevel2Test.newVector(random, n, incY);

        double[] expected = a.data.clone();
        for (int j = 0; j < n; j++) {
            for (int i = 0; i < m; i++) {
                expected[a.index(i, j)] += alpha * x[BLASLevel2Test.index(i, m, incX)] * y[BLASLevel2Test.index(j, n, incY)];
            }
        }

        double[] xBefore = x.clone();
        double[] yBefore = y.clone();

        switch (implementation) {
            case JAVA:
                if (alpha != 0.0) {
                    GER.JVM.invoke(m, n, alpha, x, OFFSET, incX, y, OFFSET, incY, a.data, a.offset, a.ld);
                }
                break;
            case NATIVE:
                GER.Native.invoke(m, n, alpha, x, OFFSET, incX, y, OFFSET, incY, a.data, a.offset, a.ld);
                break;
            default:
                GER.invoke(m, n, alpha, x, OFFSET, incX, y, OFFSET, incY, a.data, a.offset, a.ld);
                break;
        }

        String message = "GER " + implementation + " tight=" + tight + " " + m + "x" + n + " incX=" + incX + " incY=" + incY + " alpha=" + alpha;
        BLASLevel3Test.assertArray(message, expected, a.data);
        BLASLevel3Test.assertUnchanged(message, xBefore, x);
        BLASLevel3Test.assertUnchanged(message, yBefore, y);
    }

    static void doTestTRSV(final Implementation implementation, final boolean tight, final boolean upper, final boolean transpose, final boolean unitDiagonal,
            final int n, final int incX) {

        Random random = new Random(n + 31 * incX);

        Storage a = BLASLevel3Test.newTriangular(random, n, upper, unitDiagonal, tight);
        double[] x = BLASLevel2Test.newVector(random, n, incX);

        double[] rhs = x.clone();
        double[] aBefore = a.data.clone();

        switch (implementation) {
            case JAVA:
                TRSV.JVM.invoke(upper, transpose, unitDiagonal, n, a.data, a.offset, a.ld, x, OFFSET, incX);
                break;
            case NATIVE:
                TRSV.Native.invoke(upper, transpose, unitDiagonal, n, a.data, a.offset, a.ld, x, OFFSET, incX);
                break;
            default:
                TRSV.invoke(upper, transpose, unitDiagonal, n, a.data, a.offset, a.ld, x, OFFSET, incX);
                break;
        }

        double[] expected = rhs.clone();
        double[] actual = rhs.clone();
        for (int i = 0; i < n; i++) {
            double sum = 0.0;
            for (int l = 0; l < n; l++) {
                sum += BLASLevel3Test.triangular(a, upper, transpose, unitDiagonal, i, l) * x[BLASLevel2Test.index(l, n, incX)];
            }
            actual[BLASLevel2Test.index(i, n, incX)] = sum;
        }

        String message = "TRSV " + implementation + " tight=" + tight + " upper=" + upper + " trans=" + transpose + " unit=" + unitDiagonal + " n=" + n
                + " incX=" + incX;
        BLASLevel3Test.assertArray(message, expected, actual);
        for (int p = 0; p < rhs.length; p++) {
            if (p < OFFSET || p > OFFSET + (n - 1) * Math.abs(incX) || (p - OFFSET) % incX != 0) {
                TestUtils.assertEquals(message + " outside x at " + p, rhs[p], x[p]);
            }
        }
        BLASLevel3Test.assertUnchanged(message, aBefore, a.data);

        if (implementation != Implementation.NATIVE) {

            float[] aFloat = new float[a.data.length];
            for (int p = 0; p < aFloat.length; p++) {
                aFloat[p] = (float) a.data[p];
            }
            float[] xFloat = new float[rhs.length];
            for (int p = 0; p < xFloat.length; p++) {
                xFloat[p] = (float) rhs[p];
            }

            if (implementation == Implementation.JAVA) {
                TRSV.JVM.invoke(upper, transpose, unitDiagonal, n, aFloat, a.offset, a.ld, xFloat, OFFSET, incX);
            } else {
                TRSV.invoke(upper, transpose, unitDiagonal, n, aFloat, a.offset, a.ld, xFloat, OFFSET, incX);
            }

            double magnitude = 0.0;
            for (int p = 0; p < x.length; p++) {
                magnitude = Math.max(magnitude, Math.abs(x[p]));
            }
            for (int p = 0; p < x.length; p++) {
                TestUtils.assertEquals(message + " float at " + p, x[p], xFloat[p], 1E-5 * magnitude);
            }
        }
    }

    @Test
    public void testGEMV() {
        for (Implementation implementation : Implementation.values()) {
            if (BLASLevel2Test.isAvailable(implementation, GEMV.NATIVE)) {
                for (boolean tight : BOOLEANS) {
                    for (boolean transpose : BOOLEANS) {
                        for (int[] size : SIZES) {
                            for (int incX : INCREMENTS) {
                                for (int incY : INCREMENTS) {
                                    for (double alpha : ALPHAS) {
                                        for (double beta : BETAS) {
                                            BLASLevel2Test.doTestGEMV(implementation, tight, transpose, size[0], size[1], incX, incY, alpha, beta);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * With alpha 0, A and x are not read: GEMV must work with empty arrays for them, also at sizes where the
     * native implementation is used otherwise. y is scaled by beta (and not read when beta is 0).
     */
    @Test
    public void testGEMVAlphaZero() {

        double[] none = new double[0];
        int m = 120, n = 100;

        for (boolean transpose : BOOLEANS) {
            int length = transpose ? n : m;
            for (double beta : new double[] { 0.0, 0.5 }) {

                double[] y = BLASLevel2Test.newVector(new Random(length), length, 1);
                double[] expected = y.clone();
                for (int i = 0; i < length; i++) {
                    int iy = BLASLevel2Test.index(i, length, 1);
                    expected[iy] = beta == 0.0 ? 0.0 : beta * y[iy];
                    if (beta == 0.0) {
                        y[iy] = Double.NaN;
                    }
                }

                GEMV.invoke(transpose, m, n, 0.0, none, 0, m, none, 0, 1, beta, y, OFFSET, 1);

                BLASLevel3Test.assertArray("GEMV trans=" + transpose + " beta=" + beta, expected, y);
            }
        }
    }

    /**
     * The multi-threaded Java implementation, with the threshold lowered so that it splits at test sizes,
     * including skinny shapes and increments other than 1 (which are not split).
     */
    @Test
    public void testGEMVParallel() {

        int threshold = GEMV.THRESHOLD;

        try {

            GEMV.THRESHOLD = 4;

            for (boolean tight : BOOLEANS) {
                for (boolean transpose : BOOLEANS) {
                    for (int[] size : new int[][] { { 70, 45 }, { 9, 60 }, { 60, 9 } }) {
                        for (int incY : new int[] { 1, -2 }) {
                            for (double alpha : ALPHAS) {
                                for (double beta : BETAS) {
                                    BLASLevel2Test.doTestGEMV(Implementation.JAVA, tight, transpose, size[0], size[1], 1, incY, alpha, beta);
                                }
                            }
                        }
                    }
                }
            }

        } finally {
            GEMV.THRESHOLD = threshold;
        }
    }

    @Test
    public void testGER() {
        for (Implementation implementation : Implementation.values()) {
            if (BLASLevel2Test.isAvailable(implementation, GER.NATIVE)) {
                for (boolean tight : BOOLEANS) {
                    for (int[] size : SIZES) {
                        for (int incX : INCREMENTS) {
                            for (int incY : INCREMENTS) {
                                for (double alpha : ALPHAS) {
                                    BLASLevel2Test.doTestGER(implementation, tight, size[0], size[1], incX, incY, alpha);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    public void testTRSV() {
        for (Implementation implementation : Implementation.values()) {
            if (BLASLevel2Test.isAvailable(implementation, TRSV.NATIVE)) {
                for (boolean tight : BOOLEANS) {
                    for (boolean upper : BOOLEANS) {
                        for (boolean transpose : BOOLEANS) {
                            for (boolean unitDiagonal : BOOLEANS) {
                                for (int n : SIZES_SQUARE) {
                                    for (int incX : INCREMENTS) {
                                        BLASLevel2Test.doTestTRSV(implementation, tight, upper, transpose, unitDiagonal, n, incX);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}

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
package org.ojalgo.array.operation;

import java.util.Arrays;
import java.util.Random;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.type.context.NumberContext;

/**
 * The BLAS form of the level 1 operations ({@link DOT}, {@link AXPY}, {@link SCAL}) against a plain
 * evaluation of their definitions: positive, negative and unit increments (a negative increment walks the
 * vector backwards, with the offset still the lowest index), offsets, and lengths 0, short and long enough to
 * pass the native thresholds. Each case is run through the pure Java implementation, the dispatching
 * {@code invoke}, and the native implementation when there is one (run with
 * {@code --enable-native-access=ojalgo,ALL-UNNAMED} to include it). Elements that are not part of the vector
 * must be left untouched.
 */
public class BLASLevel1Test {

    enum Implementation {
        DISPATCH, JAVA, NATIVE;
    }

    private static final NumberContext ACCURACY = NumberContext.of(12);
    private static final int[] INCREMENTS = { 1, 2, -1, -3 };
    private static final int[] LENGTHS = { 0, 1, 5, 17, 300 };
    private static final int OFFSET = 3;

    /**
     * The index of element i of a vector stored from offset with increment inc, as in BLAS.
     */
    private static int index(final int i, final int n, final int offset, final int inc) {
        return inc >= 0 ? offset + i * inc : offset + (n - 1 - i) * -inc;
    }

    private static double[] newVector(final Random random, final int n, final int inc) {
        double[] retVal = new double[OFFSET + Math.max(0, n - 1) * Math.abs(inc) + 1 + 2];
        for (int p = 0; p < retVal.length; p++) {
            retVal[p] = random.nextDouble() - 0.5;
        }
        return retVal;
    }

    @Test
    public void testAXPY() {
        for (Implementation implementation : Implementation.values()) {
            if (implementation == Implementation.NATIVE && !AXPY.NATIVE) {
                continue;
            }
            for (int n : LENGTHS) {
                for (int incX : INCREMENTS) {
                    for (int incY : INCREMENTS) {
                        for (double alpha : new double[] { 0.0, 1.0, -0.7 }) {

                            Random random = new Random(n + 31 * incX + 961 * incY);
                            double[] x = BLASLevel1Test.newVector(random, n, incX);
                            double[] y = BLASLevel1Test.newVector(random, n, incY);
                            double[] xBefore = x.clone();

                            double[] expected = y.clone();
                            for (int i = 0; i < n; i++) {
                                int iy = BLASLevel1Test.index(i, n, OFFSET, incY);
                                expected[iy] += alpha * x[BLASLevel1Test.index(i, n, OFFSET, incX)];
                            }

                            switch (implementation) {
                                case JAVA:
                                    if (n > 0) {
                                        AXPY.JVM.invoke(n, alpha, x, OFFSET, incX, y, OFFSET, incY);
                                    }
                                    break;
                                case NATIVE:
                                    if (n > 0) {
                                        AXPY.Native.invoke(n, alpha, x, OFFSET, incX, y, OFFSET, incY);
                                    }
                                    break;
                                default:
                                    AXPY.invoke(n, alpha, x, OFFSET, incX, y, OFFSET, incY);
                                    break;
                            }

                            String message = "AXPY " + implementation + " n=" + n + " incX=" + incX + " incY=" + incY + " alpha=" + alpha;
                            for (int p = 0; p < y.length; p++) {
                                TestUtils.assertEquals(message + " at " + p, expected[p], y[p], ACCURACY);
                            }
                            TestUtils.assertTrue(message + " x changed", Arrays.equals(xBefore, x));
                        }
                    }
                }
            }
        }
    }

    @Test
    public void testDOT() {
        for (Implementation implementation : Implementation.values()) {
            if (implementation == Implementation.NATIVE && !DOT.NATIVE) {
                continue;
            }
            for (int n : LENGTHS) {
                for (int incX : INCREMENTS) {
                    for (int incY : INCREMENTS) {

                        Random random = new Random(n + 31 * incX + 961 * incY);
                        double[] x = BLASLevel1Test.newVector(random, n, incX);
                        double[] y = BLASLevel1Test.newVector(random, n, incY);

                        double expected = 0.0;
                        double magnitude = 0.0;
                        for (int i = 0; i < n; i++) {
                            double term = x[BLASLevel1Test.index(i, n, OFFSET, incX)] * y[BLASLevel1Test.index(i, n, OFFSET, incY)];
                            expected += term;
                            magnitude += Math.abs(term);
                        }

                        double actual = switch (implementation) {
                            case JAVA   -> n > 0 ? DOT.JVM.invoke(n, x, OFFSET, incX, y, OFFSET, incY) : 0.0;
                            case NATIVE -> n > 0 ? DOT.Native.invoke(n, x, OFFSET, incX, y, OFFSET, incY) : 0.0;
                            default     -> DOT.invoke(n, x, OFFSET, incX, y, OFFSET, incY);
                        };
                        String message = "DOT " + implementation + " n=" + n + " incX=" + incX + " incY=" + incY;
                        TestUtils.assertTrue(message + ": " + expected + " != " + actual, ACCURACY.isSmall(Math.max(magnitude, 1.0), actual - expected));
                    }
                }
            }
        }
    }

    @Test
    public void testSCAL() {
        for (Implementation implementation : Implementation.values()) {
            if (implementation == Implementation.NATIVE && !SCAL.NATIVE) {
                continue;
            }
            for (int n : LENGTHS) {
                for (int incX : INCREMENTS) {
                    for (double alpha : new double[] { 0.0, 1.0, -0.7 }) {

                        Random random = new Random(n + 31 * incX);
                        double[] x = BLASLevel1Test.newVector(random, n, incX);

                        double[] expected = x.clone();
                        if (incX > 0) {
                            for (int i = 0; i < n; i++) {
                                expected[OFFSET + i * incX] *= alpha;
                            }
                        }

                        switch (implementation) {
                            case JAVA:
                                if (n > 0 && incX > 0) {
                                    SCAL.JVM.invoke(n, alpha, x, OFFSET, incX);
                                }
                                break;
                            case NATIVE:
                                if (n > 0 && incX > 0) {
                                    SCAL.Native.invoke(n, alpha, x, OFFSET, incX);
                                }
                                break;
                            default:
                                SCAL.invoke(n, alpha, x, OFFSET, incX);
                                break;
                        }

                        String message = "SCAL " + implementation + " n=" + n + " incX=" + incX + " alpha=" + alpha;
                        for (int p = 0; p < x.length; p++) {
                            TestUtils.assertEquals(message + " at " + p, expected[p], x[p], ACCURACY);
                        }
                    }
                }
            }
        }
    }

}

/*
 * Copyright 1997-2025 Optimatika
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
package org.ojalgo.function.special;

import static org.ojalgo.function.constant.PrimitiveMath.MAX;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.function.constant.PrimitiveMath;

public class MissingMathTest {

    @Test
    public void testAtan2() {

        double max = 0.0;
        for (int yi = -999; yi <= 999; yi++) {
            double y = yi / 100D;

            for (int xi = -999; xi <= 999; xi++) {
                double x = xi / 100D;

                double expected = Math.atan2(y, x);
                double actual = MissingMath.atan2(y, x);

                double absErr = Math.abs(actual - expected);
                double relErr = absErr / Math.abs(expected);

                if (absErr > max) {
                    max = absErr;
                }
                if (relErr > max) {
                    max = relErr;
                }
            }
        }

        // BasicLogger.DEBUG.println("Max error: {} @ y={}, x={}", max, ye, xe);
        TestUtils.assertTrue(max < 3E-4);
    }

    @Test
    public void testGCD() {

        int[] numbers = new int[30];

        for (int i = 0; i < numbers.length; i++) {

            int base = PrimitiveMath.getPrimeNumber(i);

            for (int j = 0; j < numbers.length; j++) {
                numbers[j] = base * PrimitiveMath.getPrimeNumber(j);
            }

            TestUtils.assertEquals(base, MissingMath.gcd(base * base, numbers));
        }
    }

    @Test
    public void testMagnitude() {

        for (int exp = -32; exp <= 32; exp++) {

            BigDecimal number = BigDecimal.valueOf(1L, -exp);

            TestUtils.assertEquals(exp, Math.toIntExact(Math.round(Math.log10(number.doubleValue()))));

            TestUtils.assertEquals(exp, MissingMath.magnitude(number));
        }

        for (int exp = -32; exp <= 32; exp++) {
            TestUtils.assertEquals(0, MissingMath.magnitude(BigDecimal.valueOf(0L, -exp)));
        }

        for (int exp = -32; exp <= 32; exp++) {
            for (long base = 1L; base <= 9L; base++) {
                TestUtils.assertEquals(exp, MissingMath.magnitude(BigDecimal.valueOf(base, -exp)));
            }
        }

        TestUtils.assertEquals(6, MissingMath.magnitude(BigDecimal.valueOf(1_000_000)));
        TestUtils.assertEquals(3, MissingMath.magnitude(BigDecimal.valueOf(1_000)));
        TestUtils.assertEquals(2, MissingMath.magnitude(BigDecimal.valueOf(100)));
        TestUtils.assertEquals(1, MissingMath.magnitude(BigDecimal.valueOf(10)));
        TestUtils.assertEquals(0, MissingMath.magnitude(BigDecimal.valueOf(1)));
        TestUtils.assertEquals(0, MissingMath.magnitude(BigDecimal.valueOf(0)));
        TestUtils.assertEquals(-1, MissingMath.magnitude(BigDecimal.valueOf(0.1)));
        TestUtils.assertEquals(-2, MissingMath.magnitude(BigDecimal.valueOf(0.01)));
        TestUtils.assertEquals(-3, MissingMath.magnitude(BigDecimal.valueOf(0.001)));
        TestUtils.assertEquals(-6, MissingMath.magnitude(BigDecimal.valueOf(0.000001)));

        TestUtils.assertEquals(2, MissingMath.magnitude(BigDecimal.valueOf(999)));
        TestUtils.assertEquals(2, MissingMath.magnitude(BigDecimal.valueOf(-100)));
        TestUtils.assertEquals(-2, MissingMath.magnitude(BigDecimal.valueOf(0.09)));
        TestUtils.assertEquals(-2, MissingMath.magnitude(BigDecimal.valueOf(-0.01)));

        for (int exp = -32; exp <= 32; exp++) {

            double scale = 0.1 + 0.8 * Math.random(); // Range [0.1,0.9)
            double value = scale * Math.pow(10.0, exp);
            BigDecimal number = BigDecimal.valueOf(value);

            int expected = exp - 1;
            int actual = MissingMath.magnitude(number);

            TestUtils.assertEquals(expected, actual);
        }
    }

    @Test
    public void testMax() {
        TestUtils.assertEquals(9, MissingMath.max(9, 0, -9));
        TestUtils.assertEquals(9, MissingMath.max(-9, 0, 9));
    }

    @Test
    public void testMin() {
        TestUtils.assertEquals(-9, MissingMath.min(9, 0, -9));
        TestUtils.assertEquals(-9, MissingMath.min(-9, 0, 9));
    }

    @Test
    public void testMinMax() {

        TestUtils.assertEquals(Math.min(2, -78), MissingMath.min(2, -78));
        TestUtils.assertEquals(MAX.invoke(2, -78), MissingMath.max(2, -78));

        TestUtils.assertEquals(67, MissingMath.max(new int[] { 67 }));
        TestUtils.assertEquals(67, MissingMath.min(new int[] { 67 }));

        TestUtils.assertEquals(MissingMath.max(67, -76), MissingMath.max(new int[] { 67, -76 }));
        TestUtils.assertEquals(MissingMath.min(67, -76), MissingMath.min(new int[] { 67, -76 }));

        TestUtils.assertEquals(MissingMath.max(0, 67, -76), MissingMath.max(new int[] { 0, 67, -76 }));
        TestUtils.assertEquals(MissingMath.min(0, 67, -76), MissingMath.min(new int[] { 0, 67, -76 }));

        TestUtils.assertEquals(MissingMath.max(0, 67, -76, 80), MissingMath.max(new int[] { 0, 67, -76, 80 }));
        TestUtils.assertEquals(MissingMath.min(0, 67, -76, -80), MissingMath.min(new int[] { 0, 67, -76, -80 }));

        TestUtils.assertEquals(MissingMath.max(80, 0, 67, -76), MissingMath.max(new int[] { 80, 0, 67, -76 }));
        TestUtils.assertEquals(MissingMath.min(-80, 0, 67, -76), MissingMath.min(new int[] { -80, 0, 67, -76 }));

        TestUtils.assertEquals(80, MissingMath.max(new int[] { 0, 67, -76, 80 }));
        TestUtils.assertEquals(-80, MissingMath.min(new int[] { 0, 67, -76, -80 }));

        TestUtils.assertEquals(80, MissingMath.max(new int[] { 80, 0, 67, -76 }));
        TestUtils.assertEquals(-80, MissingMath.min(new int[] { -80, 0, 67, -76 }));

    }

    @Test
    public void testPower() {

        TestUtils.assertEquals(1L, MissingMath.power(2L, 0));
        TestUtils.assertEquals(2L, MissingMath.power(2L, 1));
        TestUtils.assertEquals(4L, MissingMath.power(2L, 2));
        TestUtils.assertEquals(8L, MissingMath.power(2L, 3));
        TestUtils.assertEquals(16L, MissingMath.power(2L, 4));
        TestUtils.assertEquals(32L, MissingMath.power(2L, 5));
        TestUtils.assertEquals(64L, MissingMath.power(2L, 6));
    }

}

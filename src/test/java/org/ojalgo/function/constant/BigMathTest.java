/*
 * Copyright 1997-2024 Optimatika
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
package org.ojalgo.function.constant;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.function.special.MissingMath;

public class BigMathTest extends FunctionConstantTests {

    /**
     * Corresponding to binary 256 octuple precision
     * <p>
     * https://en.wikipedia.org/wiki/IEEE_754
     */
    private static final MathContext ACCURACY = new MathContext(71, RoundingMode.HALF_EVEN);
    /**
     * For when the the exact expected value is not known.
     */
    private static final MathContext ROUGHLY = new MathContext(12, RoundingMode.HALF_EVEN);

    private static void doTestLogAndExp(final BigDecimal expected) {

        BigDecimal tmp = BigMath.LOG.invoke(expected);
        BigDecimal actual = BigMath.EXP.invoke(tmp);

        TestUtils.assertEquals(expected, actual, ACCURACY);
    }

    @Test
    public void testCos() {

        BigDecimal arg;
        BigDecimal expected;

        // 1.0

        arg = BigMath.ZERO;
        expected = BigMath.ONE;
        TestUtils.assertEquals(expected, MissingMath.cos(arg), ACCURACY);
        TestUtils.assertEquals(expected, MissingMath.cos(arg.negate()), ACCURACY);

        arg = BigMath.TWO_PI;
        TestUtils.assertEquals(expected, MissingMath.cos(arg), ACCURACY);
        TestUtils.assertEquals(expected, MissingMath.cos(arg.negate()), ACCURACY);

        // -1.0

        arg = BigMath.PI;
        expected = BigMath.NEG;
        TestUtils.assertEquals(expected, MissingMath.cos(arg), ACCURACY);
        TestUtils.assertEquals(expected, MissingMath.cos(arg.negate()), ACCURACY);

        // 0.0

        arg = BigMath.HALF_PI;
        expected = BigMath.ZERO;
        TestUtils.assertEquals(expected, MissingMath.cos(arg), ACCURACY);
        TestUtils.assertEquals(expected, MissingMath.cos(arg.negate()), ACCURACY);

        // ± 0.866

        arg = BigMath.DIVIDE.invoke(BigMath.PI, BigMath.SIX);
        expected = BigMath.DIVIDE.invoke(BigMath.SQRT.invoke(BigMath.THREE), BigMath.TWO);
        TestUtils.assertEquals(expected, MissingMath.cos(arg), ACCURACY);
        TestUtils.assertEquals(expected, MissingMath.cos(arg.negate()), ACCURACY);

        arg = BigMath.DIVIDE.invoke(BigMath.MULTIPLY.invoke(BigMath.FIVE, BigMath.PI), BigMath.SIX);
        expected = expected.negate();
        TestUtils.assertEquals(expected, MissingMath.cos(arg), ACCURACY);
        TestUtils.assertEquals(expected, MissingMath.cos(arg.negate()), ACCURACY);

        // ± 0.707

        arg = BigMath.DIVIDE.invoke(BigMath.PI, BigMath.FOUR);
        expected = BigMath.DIVIDE.invoke(BigMath.ONE, BigMath.SQRT.invoke(BigMath.TWO));
        TestUtils.assertEquals(expected, MissingMath.cos(arg), ACCURACY);
        TestUtils.assertEquals(expected, MissingMath.cos(arg.negate()), ACCURACY);

        arg = BigMath.DIVIDE.invoke(BigMath.MULTIPLY.invoke(BigMath.THREE, BigMath.PI), BigMath.FOUR);
        expected = expected.negate();
        TestUtils.assertEquals(expected, MissingMath.cos(arg), ACCURACY);
        TestUtils.assertEquals(expected, MissingMath.cos(arg.negate()), ACCURACY);

        // ± 0.5

        arg = BigMath.DIVIDE.invoke(BigMath.PI, BigMath.THREE);
        expected = BigMath.HALF;
        TestUtils.assertEquals(expected, MissingMath.cos(arg), ACCURACY);
        TestUtils.assertEquals(expected, MissingMath.cos(arg.negate()), ACCURACY);

        arg = BigMath.DIVIDE.invoke(BigMath.TWO_PI, BigMath.THREE);
        expected = expected.negate();
        TestUtils.assertEquals(expected, MissingMath.cos(arg), ACCURACY);
        TestUtils.assertEquals(expected, MissingMath.cos(arg.negate()), ACCURACY);
    }

    @Test
    public void testExpAtFifty() {
        BigDecimal expected = new BigDecimal("5.184705528587073e21");
        BigDecimal arg = BigMath.HUNDRED.divide(BigMath.TWO);
        BigDecimal actual = BigMath.EXP.invoke(arg);
        TestUtils.assertEquals(expected, actual, ROUGHLY);
    }

    @Test
    public void testExpAtHundred() {
        BigDecimal expected = new BigDecimal("2.688117141816136e43");
        BigDecimal arg = BigMath.HUNDRED;
        BigDecimal actual = BigMath.EXP.invoke(arg);
        TestUtils.assertEquals(expected, actual, ROUGHLY);
    }

    @Test
    public void testExpAtHundredth() {
        BigDecimal expected = new BigDecimal("1.010050167084168");
        BigDecimal arg = BigMath.HUNDREDTH;
        BigDecimal actual = BigMath.EXP.invoke(arg);
        TestUtils.assertEquals(expected, actual, ROUGHLY);
    }

    @Test
    public void testExpAtNegativeHundred() {
        BigDecimal expected = new BigDecimal("3.720075976020836e-44");
        BigDecimal arg = BigMath.HUNDRED.negate();
        BigDecimal actual = BigMath.EXP.invoke(arg);
        TestUtils.assertEquals(expected, actual, ROUGHLY);
    }

    @Test
    public void testExpAtNegativeHundredth() {
        BigDecimal expected = new BigDecimal("0.990049833749168");
        BigDecimal arg = BigMath.HUNDREDTH.negate();
        BigDecimal actual = BigMath.EXP.invoke(arg);
        TestUtils.assertEquals(expected, actual, ROUGHLY);
    }

    @Test
    public void testExpAtOne() {
        TestUtils.assertEquals(BigMath.E, BigMath.EXP.invoke(BigMath.ONE), ACCURACY);
    }

    @Test
    public void testExpAtZero() {
        TestUtils.assertEquals(BigMath.ONE, BigMath.EXP.invoke(BigMath.ZERO), ACCURACY);
    }

    @Test
    public void testInfinitySum() {

        BigDecimal actual = BigMath.SMALLEST_POSITIVE_INFINITY.add(BigMath.SMALLEST_NEGATIVE_INFINITY);

        TestUtils.assertTrue(actual.compareTo(BigMath.ZERO) == 0);
    }

    @Test
    public void testLogAndExp() {

        BigMathTest.doTestLogAndExp(BigMath.ONE);

        BigMathTest.doTestLogAndExp(BigMath.TEN);
        BigMathTest.doTestLogAndExp(BigMath.HUNDRED);
        BigMathTest.doTestLogAndExp(BigMath.THOUSAND);

        BigMathTest.doTestLogAndExp(BigMath.TENTH);
        BigMathTest.doTestLogAndExp(BigMath.HUNDREDTH);
        BigMathTest.doTestLogAndExp(BigMath.THOUSANDTH);
    }

    @Test
    public void testLogAtE() {
        TestUtils.assertEquals(BigMath.ONE, BigMath.LOG.invoke(BigMath.E), ACCURACY);
    }

    @Test
    public void testLogAtOne() {
        TestUtils.assertEquals(BigMath.ZERO, BigMath.LOG.invoke(BigMath.ONE), ACCURACY);
    }

    @Test
    public void testNegativeInfinity() {

        double actual = BigMath.SMALLEST_NEGATIVE_INFINITY.doubleValue();

        TestUtils.assertTrue(Double.isInfinite(actual));

        TestUtils.assertLessThan(0.0, actual);
    }

    @Test
    public void testPositiveInfinity() {

        double actual = BigMath.SMALLEST_POSITIVE_INFINITY.doubleValue();

        TestUtils.assertTrue(Double.isInfinite(actual));

        TestUtils.assertMoreThan(0.0, actual);
    }

    @Test
    public void testPowerAndRoot() {

        BigDecimal expected = BigMath.E;

        for (int p = 1; p < 10; p++) {
            BigDecimal power = BigMath.POWER.invoke(expected, p);
            BigDecimal actual = BigMath.ROOT.invoke(power, p);
            TestUtils.assertEquals(expected, actual, ACCURACY);
        }
    }

    @Test
    public void testRootAndPower() {

        BigDecimal expected = BigMath.PI;

        for (int p = 1; p < 10; p++) {
            BigDecimal root = BigMath.ROOT.invoke(expected, p);
            BigDecimal actual = BigMath.POWER.invoke(root, p);
            TestUtils.assertEquals(expected, actual, ACCURACY);
        }
    }

    @Test
    public void testSqrt() {

        BigDecimal expected = BigMath.TEN;

        BigDecimal sqrt = BigMath.SQRT.invoke(expected);

        BigDecimal actual = sqrt.multiply(sqrt);

        TestUtils.assertEquals(expected, actual, ACCURACY);
    }

}

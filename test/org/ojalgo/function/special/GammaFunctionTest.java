/*
 * Copyright 1997-2019 Optimatika
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

import static org.ojalgo.function.constant.PrimitiveMath.*;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.type.StandardType;
import org.ojalgo.type.context.NumberContext;

public class GammaFunctionTest {

    @Test
    public void testCompareImplementations() {

        int lim = 50;
        for (int a = 1; a < lim; a++) {

            double intResult = GammaFunction.gamma(a);
            double doubleResult = GammaFunction.gamma((double) a);
            ComplexNumber complexResult = GammaFunction.gamma(ComplexNumber.valueOf(a));

            TestUtils.assertEquals(intResult, doubleResult);
            TestUtils.assertEquals(intResult, complexResult);
        }
    }

    @Test
    public void testCompareLogarithmicImplementations() {

        int lim = 50;
        for (int a = 1; a < lim; a++) {

            double intResult = GammaFunction.Logarithmic.gamma(a);
            double doubleResult = GammaFunction.Logarithmic.gamma((double) a);
            ComplexNumber complexResult = GammaFunction.Logarithmic.gamma(ComplexNumber.valueOf(a));

            TestUtils.assertEquals(intResult, doubleResult);
            TestUtils.assertEquals(intResult, complexResult);
        }
    }

    @Test
    public void testGammaFunction() {

        final double tmpEps = 0.000005;

        // From a table of values 1.0 <= x <= 2.0
        TestUtils.assertEquals(ONE, GammaFunction.gamma(1.0), 1E-14 / THREE);
        TestUtils.assertEquals(0.95135, GammaFunction.gamma(1.10), tmpEps);
        TestUtils.assertEquals(0.91817, GammaFunction.gamma(1.20), tmpEps);
        TestUtils.assertEquals(0.89747, GammaFunction.gamma(1.30), tmpEps);
        TestUtils.assertEquals(0.88726, GammaFunction.gamma(1.40), tmpEps);
        TestUtils.assertEquals(0.88623, GammaFunction.gamma(1.50), tmpEps);
        TestUtils.assertEquals(0.89352, GammaFunction.gamma(1.60), tmpEps);
        TestUtils.assertEquals(0.90864, GammaFunction.gamma(1.70), tmpEps);
        TestUtils.assertEquals(0.93138, GammaFunction.gamma(1.80), tmpEps);
        TestUtils.assertEquals(0.96177, GammaFunction.gamma(1.90), tmpEps);
        TestUtils.assertEquals(ONE, GammaFunction.gamma(2.0), 1E-14 / THREE);

        // Values larger than 2.0 and smaller than 1.0
        TestUtils.assertEquals("π", GammaFunction.gamma(PI), (PI - ONE) * (PI - TWO) * GammaFunction.gamma(PI - TWO), 1E-14 / THREE);
        TestUtils.assertEquals("0.5", GammaFunction.gamma(HALF), GammaFunction.gamma(HALF + ONE) / HALF, 1E-14 / THREE);
        TestUtils.assertEquals("0.25", GammaFunction.gamma(QUARTER), GammaFunction.gamma(QUARTER + ONE) / QUARTER, 1E-14 / THREE);
        TestUtils.assertEquals("0.1", GammaFunction.gamma(TENTH), GammaFunction.gamma(TENTH + ONE) / TENTH, tmpEps);
        TestUtils.assertEquals("0.01", GammaFunction.gamma(HUNDREDTH), GammaFunction.gamma(HUNDREDTH + ONE) / HUNDREDTH, tmpEps);
        TestUtils.assertEquals("0.001", GammaFunction.gamma(THOUSANDTH), GammaFunction.gamma(THOUSANDTH + ONE) / THOUSANDTH, tmpEps);

        // Should align with n! for positve integers
        for (int n = 0; n < 10; n++) {
            TestUtils.assertEquals("n!:" + n, MissingMath.factorial(n), GammaFunction.gamma(n + ONE), tmpEps);
        }

        // Negative values
        TestUtils.assertEquals("-0.5", GammaFunction.gamma(-0.5), GammaFunction.gamma(HALF) / (-0.5), tmpEps);
        TestUtils.assertEquals("-1.5", GammaFunction.gamma(-1.5), GammaFunction.gamma(HALF) / (-1.5 * -0.5), tmpEps);
        TestUtils.assertEquals("-2.5", GammaFunction.gamma(-2.5), GammaFunction.gamma(HALF) / (-2.5 * -1.5 * -0.5), tmpEps);
        TestUtils.assertEquals("-3.5", GammaFunction.gamma(-3.5), GammaFunction.gamma(HALF) / (-3.5 * -2.5 * -1.5 * -0.5), tmpEps);
        TestUtils.assertEquals("-4.5", GammaFunction.gamma(-4.5), GammaFunction.gamma(HALF) / (-4.5 * -3.5 * -2.5 * -1.5 * -0.5), tmpEps);

        // Should be undefined for 0, -1, -2, -3...
        for (int n = 0; n < 10; n++) {
            TestUtils.assertTrue("-" + n, Double.isNaN(GammaFunction.gamma(NEG * n)));
        }

        final NumberContext tmpEval = new NumberContext(10, 10);

        // Positive half integer
        for (int n = 0; n < 10; n++) {
            TestUtils.assertEquals(n + ".5", (SQRT_PI * MissingMath.factorial(2 * n)) / (POW.invoke(FOUR, n) * MissingMath.factorial(n)),
                    GammaFunction.gamma(n + HALF), tmpEval);
        }

    }

    @Test
    public void testIntIncompleteParts() {

        int lim = 50;
        for (int n = 1; n < lim; n++) {

            for (int e = -2; e <= 2; e++) {
                double x = Math.pow(TEN, e);

                double complete = GammaFunction.gamma(n);

                double lower = GammaFunction.Incomplete.lower(n, x);
                double upper = GammaFunction.Incomplete.upper(n, x);

                TestUtils.assertEquals(complete, lower + upper);
            }
        }
    }

    @Test
    public void testIntIncompleteSteps() {

        NumberContext accuracy = StandardType.MATH_032;

        int lim = 5;
        for (int n = 1; n < lim; n++) {

            for (int e = -1; e <= 1; e++) {
                double x = Math.pow(TEN, e);

                double lowExp = (n * GammaFunction.Incomplete.lower(n, x)) - (Math.pow(x, n) * Math.exp(-x));
                double lowAct = GammaFunction.Incomplete.lower(n + 1, x);

                TestUtils.assertEquals(lowExp, lowAct, accuracy);

                double uppExp = (n * GammaFunction.Incomplete.upper(n, x)) + (Math.pow(x, n) * Math.exp(-x));
                double uppAct = GammaFunction.Incomplete.upper(n + 1, x);

                TestUtils.assertEquals(uppExp, uppAct, accuracy);
            }
        }
    }

    @Test
    public void testLogarithmic() {

        int lim = 50;
        for (int a = 1; a < lim; a++) {
            double x = a / TEN;

            double expected = Math.log(GammaFunction.gamma(x));
            double actual = GammaFunction.Logarithmic.gamma(x);

            TestUtils.assertEquals(expected, actual);
        }
    }

    @Test
    public void testSpecialCeseLimitZero() {

        double x = ZERO;

        int lim = 50;
        for (int n = 1; n < lim; n++) {

            double complete = GammaFunction.gamma(n);

            double lower = GammaFunction.Incomplete.lower(n, x);
            double upper = GammaFunction.Incomplete.upper(n, x);

            TestUtils.assertEquals(complete, upper);
            TestUtils.assertEquals(ZERO, lower);
            TestUtils.assertEquals(complete, lower + upper);
        }
    }

    @Test
    public void testSpecialCeseValueOne() {

        int n = 1;

        for (int e = -2; e <= 2; e++) {
            double x = Math.pow(TEN, e);

            double lowExp = ONE - Math.exp(-x);
            double lowAct = GammaFunction.Incomplete.lower(n, x);

            TestUtils.assertEquals(lowExp, lowAct);

            double uppExp = Math.exp(-x);
            double uppAct = GammaFunction.Incomplete.upper(n, x);

            TestUtils.assertEquals(uppExp, uppAct);
        }
    }

}

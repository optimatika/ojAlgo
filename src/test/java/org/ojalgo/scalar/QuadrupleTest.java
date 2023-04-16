/*
 * Copyright 1997-2023 Optimatika
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
package org.ojalgo.scalar;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.function.constant.BigMath;
import org.ojalgo.function.special.MissingMath;
import org.ojalgo.type.context.NumberContext;

public class QuadrupleTest extends ScalarTests {

    private static final NumberContext NC_EVAL = NumberContext.of(31).withoutScale();

    static void doTestValueOfToBigDecimal(final BigDecimal value) {

        BigDecimal expected = NC_EVAL.enforce(value);

        BigDecimal actual = NC_EVAL.enforce(Quadruple.valueOf(expected).toBigDecimal());

        TestUtils.assertEquals(expected, actual, NC_EVAL);

        TestUtils.assertTrue(actual.compareTo(expected) == 0);
    }

    @Test
    public void testAdd() {

        BigDecimal pi = BigMath.PI;
        BigDecimal e = BigMath.E;

        Quadruple left = Quadruple.valueOf(pi);
        Quadruple right = Quadruple.valueOf(e);

        Quadruple sum = left.add(right);

        BigDecimal expected = NC_EVAL.enforce(pi.add(e));
        BigDecimal actual = NC_EVAL.enforce(sum.toBigDecimal());

        TestUtils.assertEquals(expected, actual, NC_EVAL);

        TestUtils.assertTrue(actual.compareTo(expected) == 0);
    }

    @Test
    public void testCreateAndExtract() {
        QuadrupleTest.doTestValueOfToBigDecimal(BigMath.E);
        QuadrupleTest.doTestValueOfToBigDecimal(BigMath.PI);
        QuadrupleTest.doTestValueOfToBigDecimal(BigMath.E.negate());
        QuadrupleTest.doTestValueOfToBigDecimal(BigMath.PI.negate());
    }

    @Test
    public void testDivide() {

        for (int n = 1; n <= 10; n++) {

            Quadruple numerator = Quadruple.valueOf(n);

            for (int d = 1; d <= 10; d++) {

                Quadruple denominator = Quadruple.valueOf(d);

                Quadruple quotient = numerator.divide(denominator);

                BigDecimal expected = NC_EVAL.enforce(MissingMath.divide(BigDecimal.valueOf(n), BigDecimal.valueOf(d)));
                BigDecimal actual = NC_EVAL.enforce(quotient.toBigDecimal());

                TestUtils.assertEquals(expected, actual, NC_EVAL);

                TestUtils.assertTrue(actual.compareTo(expected) == 0);
            }
        }
    }

    @Test
    public void testInvert() {

        for (int i = 1; i <= 10; i++) {

            Quadruple original = Quadruple.valueOf(i);

            Quadruple inverse = original.invert();

            BigDecimal expected = NC_EVAL.enforce(MissingMath.divide(BigDecimal.ONE, BigDecimal.valueOf(i)));
            BigDecimal actual = NC_EVAL.enforce(inverse.toBigDecimal());

            TestUtils.assertEquals(expected, actual, NC_EVAL);

            TestUtils.assertTrue(actual.compareTo(expected) == 0);
        }

    }

    @Test
    public void testMultiply() {

        BigDecimal pi = BigMath.PI;
        BigDecimal e = BigMath.E;

        Quadruple left = Quadruple.valueOf(pi);
        Quadruple right = Quadruple.valueOf(e);

        Quadruple product = left.multiply(right);

        BigDecimal expected = NC_EVAL.enforce(pi.multiply(e));
        BigDecimal actual = NC_EVAL.enforce(product.toBigDecimal());

        TestUtils.assertEquals(expected, actual, NC_EVAL);

        TestUtils.assertTrue(actual.compareTo(expected) == 0);
    }

    @Test
    public void testSubtract() {

        BigDecimal pi = BigMath.PI;
        BigDecimal e = BigMath.E;

        Quadruple left = Quadruple.valueOf(pi);
        Quadruple right = Quadruple.valueOf(e);

        Quadruple difference = left.subtract(right);

        BigDecimal expected = NC_EVAL.enforce(pi.subtract(e));
        BigDecimal actual = NC_EVAL.enforce(difference.toBigDecimal());

        TestUtils.assertEquals(expected, actual, NC_EVAL);

        TestUtils.assertTrue(actual.compareTo(expected) == 0);
    }

}

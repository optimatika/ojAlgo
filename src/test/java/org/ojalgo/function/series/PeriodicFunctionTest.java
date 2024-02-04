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
package org.ojalgo.function.series;

import static org.ojalgo.function.constant.PrimitiveMath.*;

import java.util.function.DoubleUnaryOperator;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;

public class PeriodicFunctionTest extends SeriesFunctionTests {

    private static final double NUDGE = TEN * MACHINE_EPSILON;

    @Test
    public void testOriginAndPeriod() {

        DoubleUnaryOperator shape = arg -> arg >= FOUR ? ONE : ZERO;

        PeriodicFunction function = new PeriodicFunction(THREE, shape, TWO);

        TestUtils.assertEquals(ZERO, function.invoke(3.5));
        TestUtils.assertEquals(ONE, function.invoke(4.5));

        TestUtils.assertEquals(ZERO, function.invoke(1.5));
        TestUtils.assertEquals(ONE, function.invoke(2.5));

        TestUtils.assertEquals(ZERO, function.invoke(5.5));
        TestUtils.assertEquals(ONE, function.invoke(6.5));

        TestUtils.assertEquals(ZERO, function.invoke(-0.5));
        TestUtils.assertEquals(ONE, function.invoke(0.5));

        TestUtils.assertEquals(ZERO, function.invoke(-2.5));
        TestUtils.assertEquals(ONE, function.invoke(-1.5));

        TestUtils.assertEquals(ZERO, function.invoke(7.5));
        TestUtils.assertEquals(ONE, function.invoke(8.5));
    }

    @Test
    public void testSawtooth() {

        PeriodicFunction function = PeriodicFunction.SAWTOOTH;

        for (int i = -3; i <= 3; i++) {

            double shift = TWO_PI * i;

            TestUtils.assertEquals(ZERO, function.invoke(shift + ZERO));
            TestUtils.assertEquals(HALF, function.invoke(shift + HALF_PI));
            TestUtils.assertEquals(ONE, function.invoke(shift + PI - NUDGE));
            TestUtils.assertEquals(NEG, function.invoke(shift + PI + NUDGE));
            TestUtils.assertEquals(-HALF, function.invoke(shift + PI + HALF_PI));
            TestUtils.assertEquals(ZERO, function.invoke(shift + TWO_PI));
        }
    }

    @Test
    public void testSine() {

        PeriodicFunction function = PeriodicFunction.SINE;

        for (int i = -3; i <= 3; i++) {

            double shift = TWO_PI * i;

            TestUtils.assertEquals(Math.sin(ZERO), function.invoke(shift + ZERO));
            TestUtils.assertEquals(Math.sin(HALF_PI), function.invoke(shift + HALF_PI));
            TestUtils.assertEquals(Math.sin(PI), function.invoke(shift + PI));
            TestUtils.assertEquals(Math.sin(PI + HALF_PI), function.invoke(shift + PI + HALF_PI));
            TestUtils.assertEquals(Math.sin(TWO_PI), function.invoke(shift + TWO_PI));
        }
    }

    @Test
    public void testSquare() {

        PeriodicFunction function = PeriodicFunction.SQUARE;

        for (int i = -3; i <= 3; i++) {

            double shift = TWO_PI * i;

            TestUtils.assertEquals(ONE, function.invoke(shift + ZERO + NUDGE));
            TestUtils.assertEquals(ONE, function.invoke(shift + HALF_PI));
            TestUtils.assertEquals(ONE, function.invoke(shift + PI - NUDGE));
            TestUtils.assertEquals(NEG, function.invoke(shift + PI + NUDGE));
            TestUtils.assertEquals(NEG, function.invoke(shift + PI + HALF_PI));
            TestUtils.assertEquals(NEG, function.invoke(shift + TWO_PI - NUDGE));
        }
    }

    @Test
    public void testTriangle() {

        PeriodicFunction function = PeriodicFunction.TRIANGLE;

        for (int i = -3; i <= 3; i++) {

            double shift = TWO_PI * i;

            TestUtils.assertEquals(ZERO, function.invoke(shift + ZERO));
            TestUtils.assertEquals(ONE, function.invoke(shift + HALF_PI));
            TestUtils.assertEquals(ZERO, function.invoke(shift + PI));
            TestUtils.assertEquals(NEG, function.invoke(shift + PI + HALF_PI));
            TestUtils.assertEquals(ZERO, function.invoke(shift + TWO_PI));
        }
    }

}

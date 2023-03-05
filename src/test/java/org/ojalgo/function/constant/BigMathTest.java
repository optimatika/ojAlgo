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
package org.ojalgo.function.constant;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;

public class BigMathTest extends FunctionConstantTests {

    @Test
    public void testInfinitySum() {

        BigDecimal actual = BigMath.SMALLEST_POSITIVE_INFINITY.add(BigMath.SMALLEST_NEGATIVE_INFINITY);

        TestUtils.assertTrue(actual.compareTo(BigMath.ZERO) == 0);
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

}

/*
 * Copyright 1997-2022 Optimatika
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

import static org.ojalgo.function.constant.PrimitiveMath.*;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;

public class ComplexNumberTest extends ScalarTests {

    @Test
    public void testModulus() {

        TestUtils.assertEquals(ONE, ComplexNumber.valueOf(NEG).getModulus());
        TestUtils.assertEquals(NaN, ComplexNumber.NaN.getModulus());
        TestUtils.assertEquals(FIVE, ComplexNumber.of(FOUR, THREE).getModulus());

    }

    @Test
    public void testPower() {

        ComplexNumber base = ComplexNumber.TWO;

        TestUtils.assertEquals(1L, base.power(0));
        TestUtils.assertEquals(2L, base.power(1));
        TestUtils.assertEquals(4L, base.power(2));
        TestUtils.assertEquals(8L, base.power(3));
        TestUtils.assertEquals(16L, base.power(4));
        TestUtils.assertEquals(32L, base.power(5));
        TestUtils.assertEquals(64L, base.power(6));
    }

}

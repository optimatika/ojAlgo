/*
 * Copyright 1997-2020 Optimatika
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

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.function.special.PowerOf2.IntPower;
import org.ojalgo.function.special.PowerOf2.LongPower;

public class PowerOf2Test {

    @Test
    public void testPowerOfInt2() {

        int value = 1;
        TestUtils.assertEquals(value, PowerOf2.powerOfInt2(0));
        for (int i = 1; i < 31; i++) {
            value *= 2;
            TestUtils.assertEquals(value, PowerOf2.powerOfInt2(i));
            TestUtils.assertEquals(PowerOf2.exponent(value), PowerOf2.find(value));
            IntPower power = PowerOf2.getIntPower(i);
            TestUtils.assertEquals(1, power.divide(value));
            TestUtils.assertEquals(0, power.modulo(value));
        }
    }

    @Test
    public void testPowerOfLong2() {

        long value = 1L;
        TestUtils.assertEquals(value, PowerOf2.powerOfLong2(0));
        for (int i = 1; i < 63; i++) {
            value *= 2L;
            TestUtils.assertEquals(value, PowerOf2.powerOfLong2(i));
            TestUtils.assertEquals(PowerOf2.exponent(value), PowerOf2.find(value));
            LongPower power = PowerOf2.getLongPower(i);
            TestUtils.assertEquals(1L, power.divide(value));
            TestUtils.assertEquals(0L, power.modulo(value));
        }
    }

}

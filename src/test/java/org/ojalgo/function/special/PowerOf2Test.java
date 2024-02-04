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
package org.ojalgo.function.special;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.function.special.PowerOf2.IntPower;
import org.ojalgo.function.special.PowerOf2.LongPower;

public class PowerOf2Test {

    @Test
    public void testAdjustDown() {

        TestUtils.assertEquals(1, PowerOf2.adjustDown(1));
        TestUtils.assertEquals(2, PowerOf2.adjustDown(2));
        TestUtils.assertEquals(4, PowerOf2.adjustDown(4));
        TestUtils.assertEquals(8, PowerOf2.adjustDown(8));
        TestUtils.assertEquals(16, PowerOf2.adjustDown(16));
        TestUtils.assertEquals(32, PowerOf2.adjustDown(32));
        TestUtils.assertEquals(64, PowerOf2.adjustDown(64));
        TestUtils.assertEquals(128, PowerOf2.adjustDown(128));
        TestUtils.assertEquals(256, PowerOf2.adjustDown(256));
        TestUtils.assertEquals(512, PowerOf2.adjustDown(512));
        TestUtils.assertEquals(1024, PowerOf2.adjustDown(1024));

        TestUtils.assertEquals(2, PowerOf2.adjustDown(3));
        TestUtils.assertEquals(4, PowerOf2.adjustDown(5));
        TestUtils.assertEquals(4, PowerOf2.adjustDown(6));
        TestUtils.assertEquals(4, PowerOf2.adjustDown(7));
        TestUtils.assertEquals(512, PowerOf2.adjustDown(747));
    }

    @Test
    public void testAdjustUp() {

        TestUtils.assertEquals(1, PowerOf2.adjustUp(1));
        TestUtils.assertEquals(2, PowerOf2.adjustUp(2));
        TestUtils.assertEquals(4, PowerOf2.adjustUp(4));
        TestUtils.assertEquals(8, PowerOf2.adjustUp(8));
        TestUtils.assertEquals(16, PowerOf2.adjustUp(16));
        TestUtils.assertEquals(32, PowerOf2.adjustUp(32));
        TestUtils.assertEquals(64, PowerOf2.adjustUp(64));
        TestUtils.assertEquals(128, PowerOf2.adjustUp(128));
        TestUtils.assertEquals(256, PowerOf2.adjustUp(256));
        TestUtils.assertEquals(512, PowerOf2.adjustUp(512));
        TestUtils.assertEquals(1024, PowerOf2.adjustUp(1024));

        TestUtils.assertEquals(1, PowerOf2.adjustUp(0));
        TestUtils.assertEquals(4, PowerOf2.adjustUp(3));
        TestUtils.assertEquals(8, PowerOf2.adjustUp(5));
        TestUtils.assertEquals(8, PowerOf2.adjustUp(6));
        TestUtils.assertEquals(8, PowerOf2.adjustUp(7));
        TestUtils.assertEquals(1024, PowerOf2.adjustUp(747));
    }

    @Test
    public void testIsPowerOf2() {

        TestUtils.assertEquals(true, PowerOf2.isPowerOf2(1));
        TestUtils.assertEquals(true, PowerOf2.isPowerOf2(2));
        TestUtils.assertEquals(true, PowerOf2.isPowerOf2(4));
        TestUtils.assertEquals(true, PowerOf2.isPowerOf2(8));
        TestUtils.assertEquals(true, PowerOf2.isPowerOf2(16));
        TestUtils.assertEquals(true, PowerOf2.isPowerOf2(32));
        TestUtils.assertEquals(true, PowerOf2.isPowerOf2(64));
        TestUtils.assertEquals(true, PowerOf2.isPowerOf2(128));
        TestUtils.assertEquals(true, PowerOf2.isPowerOf2(256));
        TestUtils.assertEquals(true, PowerOf2.isPowerOf2(512));
        TestUtils.assertEquals(true, PowerOf2.isPowerOf2(1024));

        TestUtils.assertEquals(false, PowerOf2.isPowerOf2(0));
        TestUtils.assertEquals(false, PowerOf2.isPowerOf2(3));
        TestUtils.assertEquals(false, PowerOf2.isPowerOf2(5));
        TestUtils.assertEquals(false, PowerOf2.isPowerOf2(6));
        TestUtils.assertEquals(false, PowerOf2.isPowerOf2(7));
        TestUtils.assertEquals(false, PowerOf2.isPowerOf2(747));

        TestUtils.assertEquals(false, PowerOf2.isPowerOf2(-1));
        TestUtils.assertEquals(false, PowerOf2.isPowerOf2(-2));
        TestUtils.assertEquals(false, PowerOf2.isPowerOf2(-4));
        TestUtils.assertEquals(false, PowerOf2.isPowerOf2(-8));
    }

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

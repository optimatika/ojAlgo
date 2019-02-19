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
package org.ojalgo.machine;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;

public class TestBasicMachine {
    @Test
    public void testEquals_memoryUnequal() {
        final BasicMachine a = new BasicMachine(4_756_927_793_668_063_236L, 0);
        final BasicMachine b = new BasicMachine(4_756_927_793_668_128_771L, 0);
        TestUtils.assertFalse(a.equals(b));
    }

    @Test
    public void testEquals_threadsUnequal() {
        final BasicMachine a = new BasicMachine(4_756_927_793_617_797_114L, 2);
        final BasicMachine b = new BasicMachine(4_756_927_793_617_797_114L, 0);
        TestUtils.assertFalse(a.equals(b));
    }

    @Test
    public void testEquals_objectNull() {
        final BasicMachine a = new BasicMachine(4_756_927_793_668_063_236L, 0);
        final Object b = null;
        TestUtils.assertFalse(a.equals(b));
    }

    @Test
    public void testEquals_equal() {
        final BasicMachine a = new BasicMachine(4_756_927_793_617_797_114L, 0);
        final BasicMachine b = new BasicMachine(4_756_927_793_617_797_114L, 0);
        TestUtils.assertTrue(a.equals(b));
    }

    @Test
    public void testToString() {
        TestUtils.assertEquals("5MB/9threads", new BasicMachine(5_242_896L, 9).toString());
        TestUtils.assertEquals("5MB/1thread", new BasicMachine(5_242_896L, 1).toString());
        TestUtils.assertEquals("0kB/6threads", new BasicMachine(-1L, 6).toString());
        TestUtils.assertEquals("0kB/1thread", new BasicMachine(0L, 1).toString());
        TestUtils.assertEquals("2GB/1thread", new BasicMachine(3_000_000_737L, 1).toString());
        TestUtils.assertEquals("2GB/17threads", new BasicMachine(3_000_000_737L, 17).toString());
        TestUtils.assertEquals("10737418291776bytes/1thread", new BasicMachine(10_737_418_291_776L, 1).toString());
        TestUtils.assertEquals("10737418291776bytes/9threads", new BasicMachine(10_737_418_291_776L, 9).toString());
    }

    @Test
    public void testHashCode() {
        TestUtils.assertEquals(0, new BasicMachine(-3_854_940_542_643_240_991L, 0).hashCode());
        TestUtils.assertEquals(162530749, new BasicMachine(5_242_896L, 12).hashCode());
    }
}

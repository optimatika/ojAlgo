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
package org.ojalgo.concurrent;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;

public class ParallelismSupplierTest {

    static final ParallelismSupplier INIT9 = () -> 9;

    @Test
    public void testDivideBy() {

        TestUtils.assertEquals(9, INIT9.divideBy(-10));
        TestUtils.assertEquals(9, INIT9.divideBy(-1));
        TestUtils.assertEquals(9, INIT9.divideBy(0));
        TestUtils.assertEquals(9, INIT9.divideBy(1));
        TestUtils.assertEquals(5, INIT9.divideBy(2));
        TestUtils.assertEquals(3, INIT9.divideBy(3));
        TestUtils.assertEquals(3, INIT9.divideBy(4));
        TestUtils.assertEquals(2, INIT9.divideBy(5));
        TestUtils.assertEquals(2, INIT9.divideBy(6));
        TestUtils.assertEquals(2, INIT9.divideBy(7));
        TestUtils.assertEquals(2, INIT9.divideBy(8));
        TestUtils.assertEquals(1, INIT9.divideBy(9));
        TestUtils.assertEquals(1, INIT9.divideBy(10));
        TestUtils.assertEquals(1, INIT9.divideBy(100));

        TestUtils.assertEquals(5, INIT9.halve());
    }

    @Test
    public void testAdjustUpDown() {

        TestUtils.assertEquals(16, INIT9.adjustUp());
        TestUtils.assertEquals(8, INIT9.adjustDown());
    }

    @Test
    public void testMinMax() {

        TestUtils.assertEquals(8, INIT9.limit(8));
        TestUtils.assertEquals(10, INIT9.require(10));
    }

    @Test
    public void testIncrementDecrement() {

        TestUtils.assertEquals(8, INIT9.decrement());
        TestUtils.assertEquals(10, INIT9.increment());

        TestUtils.assertEquals(9, INIT9.increment().decrement());
    }

}

/*
 * Copyright 1997-2016 Optimatika (www.optimatika.se)
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
package org.ojalgo.array;

import org.ojalgo.TestUtils;

/**
 * NumberListTest
 *
 * @author apete
 */
public class NumberListTest extends ArrayTests {

    public NumberListTest() {
        super();
    }

    public NumberListTest(final String aName) {
        super(aName);
    }

    public void testGrowCapacity() {

        final NumberList<Double> tmNumberList = NumberList.makePrimitive();

        TestUtils.assertEquals(0L, tmNumberList.count());
        TestUtils.assertEquals(16L, tmNumberList.capacity());

        for (long i = 0L; i <= 16; i++) {
            tmNumberList.add(i);
        }

        TestUtils.assertEquals(17L, tmNumberList.count());
        TestUtils.assertEquals(32L, tmNumberList.capacity());

        for (long i = 17L; i <= 16_384L; i++) {
            tmNumberList.add(i);
        }

        TestUtils.assertEquals(16_385L, tmNumberList.count());
        TestUtils.assertEquals(16_384L * 2L, tmNumberList.capacity());
    }

}

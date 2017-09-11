/*
 * Copyright 1997-2017 Optimatika (www.optimatika.se)
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

        final DenseCapacityStrategy<Double> tmpStrategy = new DenseCapacityStrategy<>(Primitive64Array.FACTORY);
        final long initial = tmpStrategy.initial();
        final long chunk = tmpStrategy.chunk();

        final NumberList<Double> tmNumberList = NumberList.factory(Primitive64Array.FACTORY).make();
        TestUtils.assertEquals(0L, tmNumberList.count());

        for (long i = 0L; i <= initial; i++) {
            TestUtils.assertEquals(initial, tmNumberList.capacity());
            tmNumberList.add(i);
        }
        TestUtils.assertEquals(initial + 1L, tmNumberList.count());

        for (long i = initial + 1L; i < chunk; i++) {
            tmNumberList.add(i);
        }
        TestUtils.assertEquals(chunk, tmNumberList.capacity());

        tmNumberList.add(chunk);
        TestUtils.assertEquals(chunk * 2L, tmNumberList.capacity());
        TestUtils.assertEquals(chunk + 1L, tmNumberList.count());

        for (long i = 0L; i < chunk; i++) {
            tmNumberList.add(i);
        }
        TestUtils.assertEquals(chunk * 3L, tmNumberList.capacity());
        TestUtils.assertEquals((2L * chunk) + 1L, tmNumberList.count());
    }

}

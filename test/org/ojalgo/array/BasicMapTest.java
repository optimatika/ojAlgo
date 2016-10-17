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

import java.util.Map.Entry;

import org.ojalgo.TestUtils;

/**
 * BasicMapTest
 *
 * @author apete
 */
public class BasicMapTest extends ArrayTests {

    public BasicMapTest() {
        super();
    }

    public BasicMapTest(final String aName) {
        super(aName);
    }

    public void testSubmap() {

        final LongToNumberMap<Double> tmpMap = LongToNumberMap.makePrimitive();

        tmpMap.put(10, 1);
        tmpMap.put(20, 2);
        tmpMap.put(50, 5);
        tmpMap.put(100, 1);
        tmpMap.put(200, 2);
        tmpMap.put(500, 5);
        tmpMap.put(1000, 1);

        final LongToNumberMap<Double> tmpHeadMap = tmpMap.headMap(100L);
        TestUtils.assertEquals(3, tmpHeadMap.size());
        double tmpHeadSum = 0.0;
        for (final Entry<Long, Double> tmpEntry : tmpHeadMap.entrySet()) {
            tmpHeadSum += tmpEntry.getValue();
        }
        TestUtils.assertEquals(8.0, tmpHeadSum);

        final LongToNumberMap<Double> tmpTailMap = tmpMap.tailMap(100L);
        TestUtils.assertEquals(4, tmpTailMap.size());
        double tmpTailSum = 0.0;
        for (final Entry<Long, Double> tmpEntry : tmpTailMap.entrySet()) {
            tmpTailSum += tmpEntry.getValue();
        }
        TestUtils.assertEquals(9.0, tmpTailSum);

        final LongToNumberMap<Double> tmpSubMap = tmpMap.subMap(35, 350);
        TestUtils.assertEquals(3, tmpSubMap.size());
        double tmpSubSum = 0.0;
        for (final Entry<Long, Double> tmpEntry : tmpSubMap.entrySet()) {
            tmpSubSum += tmpEntry.getValue();
        }
        TestUtils.assertEquals(8.0, tmpSubSum);

        TestUtils.assertEquals(7, tmpMap.size());
        double tmpKeySum = 0.0;
        double tmpValSum = 0.0;
        for (final Entry<Long, Double> tmpEntry : tmpMap.entrySet()) {
            tmpKeySum += tmpEntry.getKey();
            tmpValSum += tmpEntry.getValue();
        }
        TestUtils.assertEquals(1880.0, tmpKeySum);
        TestUtils.assertEquals(17.0, tmpValSum);

        tmpKeySum = 0.0;
        for (final Long tmpKey : tmpMap.keySet()) {
            tmpKeySum += tmpKey;
        }
        TestUtils.assertEquals(1880.0, tmpKeySum);

        tmpValSum = 0.0;
        for (final Double tmpValue : tmpMap.values()) {
            tmpValSum += tmpValue;
        }
        TestUtils.assertEquals(17.0, tmpValSum);

    }

}

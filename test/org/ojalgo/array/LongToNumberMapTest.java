/*
 * Copyright 1997-2018 Optimatika
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
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.type.context.NumberContext;

/**
 * LongToNumberMap
 *
 * @author apete
 */
public class LongToNumberMapTest {

    private static final NumberContext CONTEXT = new NumberContext();
    private static final Random RANDOM = new Random();

    @Test
    public void testAlignCapacity() {
        TestUtils.assertEquals(1, 1L << PrimitiveMath.powerOf2Larger(-1L));
        TestUtils.assertEquals(16, 1L << PrimitiveMath.powerOf2Larger(16L));
        TestUtils.assertEquals(512, 1L << PrimitiveMath.powerOf2Larger(365L));
        TestUtils.assertEquals(16_384, 1L << PrimitiveMath.powerOf2Larger(16_384L));
    }

    @Test
    public void testCompareWithTreeMap() {

        final LongToNumberMap<Double> primit64Map = LongToNumberMap.factory(Primitive64Array.FACTORY).make();
        final LongToNumberMap<Double> direct64Map = LongToNumberMap.factory(BufferArray.DIRECT64).make();

        final SortedMap<Long, Double> expectedMap = new TreeMap<>();

        for (long index = 0L; index < 1_000; index++) {

            final Double value = RANDOM.nextDouble();

            primit64Map.put(index, value);
            direct64Map.put(index, value);
            expectedMap.put(index, value);
        }

        for (int c = 0; c < 1_000; c++) {

            final Long index = Long.valueOf(RANDOM.nextInt(1_000));
            final Double value = RANDOM.nextDouble();

            primit64Map.put(index, value);
            direct64Map.put(index, value);
            expectedMap.put(index, value);
        }

        for (int c = 0; c < 1_000; c++) {

            final Long index = Long.valueOf(RANDOM.nextInt(1_000));

            primit64Map.remove(index);
            direct64Map.remove(index);
            expectedMap.remove(index);
        }

        for (long index = 0L; index < 100; index++) {
            primit64Map.remove(index);
            direct64Map.remove(index);
            expectedMap.remove(index);
        }

        TestUtils.assertEquals(expectedMap.size(), primit64Map.size());
        TestUtils.assertEquals(expectedMap.size(), direct64Map.size());

        for (final Entry<Long, Double> entry : expectedMap.entrySet()) {
            final double expectedValue = entry.getValue().doubleValue();
            TestUtils.assertEquals(expectedValue, primit64Map.get(entry.getKey()).doubleValue(), CONTEXT);
            TestUtils.assertEquals(expectedValue, primit64Map.doubleValue(entry.getKey()), CONTEXT);
            TestUtils.assertEquals(expectedValue, direct64Map.get(entry.getKey()).doubleValue(), CONTEXT);
            TestUtils.assertEquals(expectedValue, direct64Map.doubleValue(entry.getKey()), CONTEXT);
        }

    }

    @Test
    public void testSubmap() {

        final LongToNumberMap<Double> tmpMap = LongToNumberMap.factory(Primitive64Array.FACTORY).make();

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
        TestUtils.assertEquals(tmpSubMap.values(), tmpMap.values(35, 350));

        final LongToNumberMap<Double> tmpSubMap2 = tmpMap.subMap(50, 350);
        TestUtils.assertEquals(3, tmpSubMap2.size());
        double tmpSubSum2 = 0.0;
        for (final Entry<Long, Double> tmpEntry : tmpSubMap2.entrySet()) {
            tmpSubSum2 += tmpEntry.getValue();
        }
        TestUtils.assertEquals(8.0, tmpSubSum2);
        TestUtils.assertEquals(tmpSubMap.values(), tmpMap.values(50, 350));

        final LongToNumberMap<Double> tmpSubMap3 = tmpMap.subMap(35, 500);
        TestUtils.assertEquals(3, tmpSubMap3.size());
        double tmpSubSum3 = 0.0;
        for (final Entry<Long, Double> tmpEntry : tmpSubMap3.entrySet()) {
            tmpSubSum3 += tmpEntry.getValue();
        }
        TestUtils.assertEquals(8.0, tmpSubSum3);
        TestUtils.assertEquals(tmpSubMap.values(), tmpMap.values(35, 500));

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

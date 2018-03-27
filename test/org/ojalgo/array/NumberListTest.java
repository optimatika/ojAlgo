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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.type.context.NumberContext;

/**
 * NumberListTest
 *
 * @author apete
 */
public class NumberListTest {

    private static final NumberContext CONTEXT = new NumberContext();
    private static final Random RANDOM = new Random();

    @Test
    public void testCompareWithArrayList() {

        final NumberList<Double> primit64List = NumberList.factory(Primitive64Array.FACTORY).make();
        final NumberList<Double> direct64List = NumberList.factory(BufferArray.DIRECT64).make();

        final List<Double> expectedList = new ArrayList<>();

        for (int c = 0; c < 10_000; c++) {

            final Double value = RANDOM.nextDouble();

            primit64List.add(value);
            direct64List.add(value);
            expectedList.add(value);
        }

        for (int c = 0; c < 100; c++) {

            final int index = RANDOM.nextInt(10_000);
            final Double value = RANDOM.nextDouble();

            primit64List.add(index, value);
            direct64List.add(index, value);
            expectedList.add(index, value);
        }

        for (int c = 0; c < 100; c++) {

            final int index = RANDOM.nextInt(10_000);
            final Double value = RANDOM.nextDouble();

            primit64List.set(index, value);
            direct64List.set(index, value);
            expectedList.set(index, value);
        }

        for (int c = 0; c < 100; c++) {

            final int index = RANDOM.nextInt(10_000);

            primit64List.remove(index);
            direct64List.remove(index);
            expectedList.remove(index);
        }

        final Set<Double> toAdd = new HashSet<>();
        final Set<Double> toRemove = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            toAdd.add(RANDOM.nextDouble());
            toRemove.add(RANDOM.nextDouble());
        }
        primit64List.addAll(toAdd);
        direct64List.addAll(toAdd);
        expectedList.addAll(toAdd);
        primit64List.removeAll(toRemove);
        direct64List.removeAll(toRemove);
        expectedList.removeAll(toRemove);

        TestUtils.assertEquals(expectedList.size(), primit64List.size());
        TestUtils.assertEquals(expectedList.size(), direct64List.size());

        for (int i = 0; i < expectedList.size(); i++) {
            TestUtils.assertEquals(expectedList.get(i).doubleValue(), primit64List.get(i).doubleValue(), CONTEXT);
            TestUtils.assertEquals(expectedList.get(i).doubleValue(), primit64List.doubleValue(i), CONTEXT);
            TestUtils.assertEquals(expectedList.get(i).doubleValue(), direct64List.get(i).doubleValue(), CONTEXT);
            TestUtils.assertEquals(expectedList.get(i).doubleValue(), direct64List.doubleValue(i), CONTEXT);
        }

    }

    @Test
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

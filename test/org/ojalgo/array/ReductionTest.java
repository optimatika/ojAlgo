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

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.random.RandomUtils;

public class ReductionTest {

    public ReductionTest() {
        super();
    }

    @Test
    public void testAnyD() {

        long[] structure = new long[] { 6, 7, 4, 5, 2, 3, 1 };

        double total = RandomUtils.factorial(7);

        ArrayAnyD<Double> array = ArrayAnyD.PRIMITIVE64.makeZero(structure);
        array.fillAll(1.0);

        for (int d = 0; d < structure.length; d++) {
            Array1D<Double> reduced = array.reduce(d, Aggregator.SUM);
            TestUtils.assertEquals(structure[d], reduced.count());
            double expected = total / structure[d];
            for (int i = 0; i < reduced.length; i++) {
                TestUtils.assertEquals(expected, reduced.doubleValue(i));
            }
        }
    }

}

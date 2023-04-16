/*
 * Copyright 1997-2023 Optimatika
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
package org.ojalgo.function.aggregator;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.array.ArrayAnyD;
import org.ojalgo.function.PrimitiveFunction;

public class AggregatorFunctionTest extends FunctionAggregatorTests {

    private static final PrimitiveFunction.Predicate FINITE = Double::isFinite;

    @Test
    public void testIsFinitite() {

        ArrayAnyD<Double> array1 = ArrayAnyD.R064.make(9, 5, 7, 3);
        ArrayAnyD<Double> array2 = ArrayAnyD.R064.make(9, 5, 7, 3);

        array1.fillAll(1.0);
        array2.fillAll(Double.NaN);

        array1.set(new long[] { 1, 1, 1, 1 }, Double.NaN);
        array2.set(new long[] { 1, 1, 1, 1 }, 1.0);

        AggregatorFunction<Double> sum = PrimitiveAggregator.getSet().sum().filter(FINITE);
        array1.visitAll(sum);
        array2.visitAll(sum);
        TestUtils.assertEquals(9 * 5 * 7 * 3, sum.intValue());

        AggregatorFunction<Double> cardinality = PrimitiveAggregator.getSet().cardinality().filter(FINITE);
        array1.visitAll(cardinality);
        array2.visitAll(cardinality);
        TestUtils.assertEquals(9 * 5 * 7 * 3, cardinality.intValue());

        AggregatorFunction<Double> sum2 = PrimitiveAggregator.getSet().sum2().filter(FINITE);
        array1.visitAll(sum2);
        array2.visitAll(sum2);
        TestUtils.assertEquals(9 * 5 * 7 * 3, sum2.intValue());

        AggregatorFunction<Double> norm2 = PrimitiveAggregator.getSet().norm2().filter(FINITE);
        array2.visitAll(norm2);
        TestUtils.assertEquals(1, norm2.intValue());
    }

}

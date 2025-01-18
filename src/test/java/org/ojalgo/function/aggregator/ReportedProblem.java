/*
 * Copyright 1997-2025 Optimatika
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

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.array.Array1D;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.structure.Access1D;

public class ReportedProblem extends FunctionAggregatorTests {

    @Test
    public void testAggregatorMaximumDoesNotWorkForNegativeNumbers() {

        Array1D<Double> prim64Arr = Array1D.R064.make(9);
        for (int i = 0; i < prim64Arr.size(); i++) {
            prim64Arr.set(i, i - 100.0);
        }

        Array1D<BigDecimal> bigArr = Array1D.R256.copy((Access1D<?>) prim64Arr);
        Array1D<RationalNumber> rtnlArray = Array1D.Q128.copy((Access1D<?>) prim64Arr);
        Array1D<Double> prim32Arr = Array1D.R032.copy((Access1D<?>) prim64Arr);

        TestUtils.assertEquals(-92.0, prim64Arr.aggregateAll(Aggregator.MAXIMUM).doubleValue());
        TestUtils.assertEquals(-92.0, bigArr.aggregateAll(Aggregator.MAXIMUM).doubleValue());
        TestUtils.assertEquals(-92.0, rtnlArray.aggregateAll(Aggregator.MAXIMUM).doubleValue());
        TestUtils.assertEquals(-92.0, prim32Arr.aggregateAll(Aggregator.MAXIMUM).doubleValue());

        TestUtils.assertEquals(-100.0, prim64Arr.aggregateAll(Aggregator.MINIMUM).doubleValue());
        TestUtils.assertEquals(-100.0, bigArr.aggregateAll(Aggregator.MINIMUM).doubleValue());
        TestUtils.assertEquals(-100.0, rtnlArray.aggregateAll(Aggregator.MINIMUM).doubleValue());
        TestUtils.assertEquals(-100.0, prim32Arr.aggregateAll(Aggregator.MINIMUM).doubleValue());

        Array1D<ComplexNumber> cmplxArr = Array1D.C128.copy((Access1D<?>) prim64Arr);
        Array1D<Quaternion> quatArr = Array1D.H256.copy((Access1D<?>) prim64Arr);

        TestUtils.assertEquals(100.0, prim64Arr.aggregateAll(Aggregator.LARGEST).doubleValue());
        TestUtils.assertEquals(100.0, bigArr.aggregateAll(Aggregator.LARGEST).doubleValue());
        TestUtils.assertEquals(100.0, rtnlArray.aggregateAll(Aggregator.LARGEST).doubleValue());
        TestUtils.assertEquals(100.0, prim32Arr.aggregateAll(Aggregator.LARGEST).doubleValue());
        TestUtils.assertEquals(100.0, cmplxArr.aggregateAll(Aggregator.LARGEST).doubleValue());
        TestUtils.assertEquals(100.0, quatArr.aggregateAll(Aggregator.LARGEST).doubleValue());

        TestUtils.assertEquals(92.0, prim64Arr.aggregateAll(Aggregator.SMALLEST).doubleValue());
        TestUtils.assertEquals(92.0, bigArr.aggregateAll(Aggregator.SMALLEST).doubleValue());
        TestUtils.assertEquals(92.0, rtnlArray.aggregateAll(Aggregator.SMALLEST).doubleValue());
        TestUtils.assertEquals(92.0, prim32Arr.aggregateAll(Aggregator.SMALLEST).doubleValue());
        TestUtils.assertEquals(92.0, cmplxArr.aggregateAll(Aggregator.SMALLEST).doubleValue());
        TestUtils.assertEquals(92.0, quatArr.aggregateAll(Aggregator.SMALLEST).doubleValue());
    }

}

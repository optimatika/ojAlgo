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
package org.ojalgo.array;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.random.Uniform;

public class SparseTest extends ArrayTests {

    @Test
    public void testAggregateSumDifferentWays() {

        long dim = 1000L;
        long dim2 = dim * dim;
        final long count = dim2;

        SparseArray<Double> plain = SparseArray.factory(PrimitiveR064.FACTORY).limit(count).make();
        Array1D<Double> array1D = Array1D.PRIMITIVE64.make(dim2);
        Array2D<Double> array2D = Array2D.PRIMITIVE64.make(dim, dim);

        for (int i = 0; i < 100; i++) {
            long index = Uniform.randomInteger(dim2);
            plain.set(index, 1.0);
            array1D.set(index, 1.0);
            array2D.set(index, 1.0);
        }

        double expected = plain.nonzeros().stream().mapToDouble(nz -> nz.doubleValue()).sum();

        double stream1D = array1D.nonzeros().stream().mapToDouble(nz -> nz.doubleValue()).sum();
        double stream2D = array2D.nonzeros().stream().mapToDouble(nz -> nz.doubleValue()).sum();

        TestUtils.assertEquals(expected, stream1D);
        TestUtils.assertEquals(expected, stream2D);

        TestUtils.assertEquals(expected, array1D.aggregateAll(Aggregator.SUM).doubleValue());
        TestUtils.assertEquals(expected, array2D.aggregateAll(Aggregator.SUM).doubleValue());
    }

    @Test
    @Tag("slow")
    @Tag("unstable")
    public void testRandomAccess() {

        long dim = 100_000L;

        SparseArray<Double> array = SparseArray.factory(PrimitiveR064.FACTORY).limit(dim * dim).make();

        for (long i = 0L; i < dim; i++) {
            array.set(Uniform.randomInteger(dim * dim), 1.0);
        }

        double sumOfAll = 0D;
        for (long i = 0L, limit = array.count(); i < limit; i++) {
            sumOfAll += array.doubleValue(i);
        }

        // There is of course a chanse the same random index was generated more
        // than once (when setting the values). In that case the test will fail.
        TestUtils.assertEquals(dim, sumOfAll);
    }

}

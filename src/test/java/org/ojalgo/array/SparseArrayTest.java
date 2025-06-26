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
package org.ojalgo.array;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.array.SparseArray.NonzeroView;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.random.Uniform;
import org.ojalgo.structure.ElementView1D;
import org.ojalgo.structure.ElementView2D;

public class SparseArrayTest extends ArrayTests {

    @Test
    public void testAggregateSumDifferentWays() {

        int dim = 1000;
        int dim2 = dim * dim;

        SparseArray<Double> plain = SparseArray.factory(ArrayR064.FACTORY).make(dim2);
        Array1D<Double> array1D = Array1D.R064.make(dim2);
        Array2D<Double> array2D = Array2D.R064.make(dim, dim);

        for (int i = 0; i < 100; i++) {
            long index = Uniform.randomInteger(dim2);
            plain.set(index, 1.0);
            array1D.set(index, 1.0);
            array2D.set(index, 1.0);
        }

        double expected = plain.nonzeros().stream().mapToDouble(NonzeroView::doubleValue).sum();

        double stream1D = array1D.nonzeros().stream().mapToDouble(ElementView1D::doubleValue).sum();
        double stream2D = array2D.nonzeros().stream().mapToDouble(ElementView2D::doubleValue).sum();

        TestUtils.assertEquals(expected, stream1D);
        TestUtils.assertEquals(expected, stream2D);

        TestUtils.assertEquals(expected, array1D.aggregateAll(Aggregator.SUM).doubleValue());
        TestUtils.assertEquals(expected, array2D.aggregateAll(Aggregator.SUM).doubleValue());
    }

    @Test
    public void testExchange() {

        SparseArray<Double> array = SparseArray.factory(ArrayR064.FACTORY).make(10);

        array.set(2, 5.0);
        array.set(7, 3.0);

        array.exchange(2, 7);

        Assertions.assertEquals(3.0, array.doubleValue(2));
        Assertions.assertEquals(5.0, array.doubleValue(7));
    }

    @Test
    public void testExchangeBothNonzero() {

        SparseArray<Double> array = SparseArray.factory(ArrayR064.FACTORY).make(10);

        array.set(2, 5.0);
        array.set(7, 3.0);

        Assertions.assertEquals(5.0, array.doubleValue(2));
        Assertions.assertEquals(3.0, array.doubleValue(7));

        array.exchange(2, 7);

        Assertions.assertEquals(3.0, array.doubleValue(2));
        Assertions.assertEquals(5.0, array.doubleValue(7));
    }

    @Test
    public void testExchangeBothZero() {

        SparseArray<Double> array = SparseArray.factory(ArrayR064.FACTORY).make(10);

        // Both positions are zero (not stored)
        Assertions.assertEquals(0.0, array.doubleValue(2));
        Assertions.assertEquals(0.0, array.doubleValue(7));

        array.exchange(2, 7);

        // Should still be zero
        Assertions.assertEquals(0.0, array.doubleValue(2));
        Assertions.assertEquals(0.0, array.doubleValue(7));
    }

    @Test
    public void testExchangeMaintainsSparseness() {

        SparseArray<Double> array = SparseArray.factory(ArrayR064.FACTORY).make(100);

        array.set(10, 1.0);
        array.set(20, 2.0);
        array.set(30, 3.0);

        int initialNonzeros = array.countNonzeros();

        array.exchange(10, 20);

        Assertions.assertEquals(initialNonzeros, array.countNonzeros());
        Assertions.assertEquals(2.0, array.doubleValue(10));
        Assertions.assertEquals(1.0, array.doubleValue(20));
        Assertions.assertEquals(3.0, array.doubleValue(30));
    }

    @Test
    public void testExchangeOneZero() {

        SparseArray<Double> array = SparseArray.factory(ArrayR064.FACTORY).make(10);

        array.set(2, 5.0);
        // Position 7 is zero (not stored)

        Assertions.assertEquals(5.0, array.doubleValue(2));
        Assertions.assertEquals(0.0, array.doubleValue(7));

        array.exchange(2, 7);

        Assertions.assertEquals(0.0, array.doubleValue(2));
        Assertions.assertEquals(5.0, array.doubleValue(7));
    }

    @Test
    public void testExchangeSameIndex() {

        SparseArray<Double> array = SparseArray.factory(ArrayR064.FACTORY).make(10);

        array.set(2, 5.0);

        Assertions.assertEquals(5.0, array.doubleValue(2));

        array.exchange(2, 2);

        // Should remain unchanged
        Assertions.assertEquals(5.0, array.doubleValue(2));
    }

    @Test
    public void testIndexOfLargest() {

        BasicArray<Double> sparseArray = SparseArray.factory(ArrayR064.FACTORY).make(1_000_000L);

        for (int i = 0; i < 100; i++) {
            long index = Uniform.randomInteger(1_000_000L);
            sparseArray.set(index, Math.random());
        }

        long index = Uniform.randomInteger(1_000_000L);
        sparseArray.set(index, -2.0);

        TestUtils.assertEquals(index, sparseArray.indexOfLargest());
    }

    @Test
    public void testPutLastAppendsCorrectly() {

        SparseArray<Double> arr = SparseArray.factory(ArrayR064.FACTORY).make(10);

        arr.set(2, 1.0);
        arr.putLast(5, 2.0);
        arr.putLast(9, 3.0);

        TestUtils.assertEquals(1.0, arr.doubleValue(2));
        TestUtils.assertEquals(2.0, arr.doubleValue(5));
        TestUtils.assertEquals(3.0, arr.doubleValue(9));
        TestUtils.assertEquals(0.0, arr.doubleValue(0));
        TestUtils.assertEquals(0.0, arr.doubleValue(8));
    }

    @Test
    public void testPutLastSkipsZero() {

        SparseArray<Double> arr = SparseArray.factory(ArrayR064.FACTORY).make(10);
        arr.putLast(1, 0.0);
        TestUtils.assertEquals(0, arr.countNonzeros());
    }

    @Test
    @Tag("slow")
    @Tag("unstable")
    public void testRandomAccess() {

        long dim = 100_000L;

        BasicArray<Double> array = SparseArray.factory(ArrayR064.FACTORY).make(dim * dim);

        for (long i = 0L; i < dim; i++) {
            array.set(Uniform.randomInteger(dim * dim), 1.0);
        }

        double sumOfAll = 0D;
        for (long i = 0L, limit = array.count(); i < limit; i++) {
            sumOfAll += array.doubleValue(i);
        }

        // There is of course a chance the same random index was generated more
        // than once (when setting the values). In that case the test will fail.
        TestUtils.assertEquals(dim, sumOfAll);
    }

}

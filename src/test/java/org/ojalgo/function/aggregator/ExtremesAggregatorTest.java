/*
 * Copyright 1997-2026 Optimatika
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
import java.util.List;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.array.ArrayR064;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Quadruple;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.scalar.RationalNumber;

/**
 * {@link Aggregator#MAXIMUM}, {@link Aggregator#MINIMUM}, {@link Aggregator#LARGEST} and
 * {@link Aggregator#SMALLEST}: an empty input gives 0, otherwise the actual result – also when that is
 * infinite.
 */
public class ExtremesAggregatorTest extends FunctionAggregatorTests {

    private static final double INF = Double.POSITIVE_INFINITY;

    @SafeVarargs
    private static <N extends Comparable<N>> double aggregate(final AggregatorFunction<N> function, final N... values) {
        for (N value : values) {
            function.invoke(value);
        }
        return function.doubleValue();
    }

    private static <N extends Comparable<N>> void assertEmptyIsZero(final AggregatorSet<N> set) {
        ExtremesAggregatorTest.assertIdentical(0.0, set.maximum().doubleValue());
        ExtremesAggregatorTest.assertIdentical(0.0, set.minimum().doubleValue());
        ExtremesAggregatorTest.assertIdentical(0.0, set.largest().doubleValue());
        ExtremesAggregatorTest.assertIdentical(0.0, set.smallest().doubleValue());
    }

    private static void assertIdentical(final double expected, final double actual) {
        TestUtils.assertEquals(expected + " != " + actual, Double.doubleToLongBits(expected), Double.doubleToLongBits(actual));
    }

    private static <N extends Comparable<N>> void assertResetIsEmpty(final AggregatorSet<N> set, final N value) {
        for (AggregatorFunction<N> function : List.of(set.maximum(), set.minimum(), set.largest(), set.smallest())) {
            function.invoke(value);
            ExtremesAggregatorTest.assertIdentical(0.0, function.reset().doubleValue());
        }
    }

    @Test
    public void testEmptyIsZero() {
        ExtremesAggregatorTest.assertEmptyIsZero(PrimitiveAggregator.getSet());
        ExtremesAggregatorTest.assertEmptyIsZero(QuadrupleAggregator.getSet());
        ExtremesAggregatorTest.assertEmptyIsZero(BigAggregator.getSet());
        ExtremesAggregatorTest.assertEmptyIsZero(RationalAggregator.getSet());
        ExtremesAggregatorTest.assertEmptyIsZero(ComplexAggregator.getSet());
        ExtremesAggregatorTest.assertEmptyIsZero(QuaternionAggregator.getSet());
    }

    @Test
    public void testFinite() {

        PrimitiveAggregator primitive = PrimitiveAggregator.getSet();
        ExtremesAggregatorTest.assertIdentical(-3.0, ExtremesAggregatorTest.aggregate(primitive.maximum(), -5.0, -3.0));
        ExtremesAggregatorTest.assertIdentical(3.0, ExtremesAggregatorTest.aggregate(primitive.minimum(), 5.0, 3.0));
        ExtremesAggregatorTest.assertIdentical(1.0, ExtremesAggregatorTest.aggregate(primitive.smallest(), 0.0, 2.0, -1.0));
        ExtremesAggregatorTest.assertIdentical(0.0, ExtremesAggregatorTest.aggregate(primitive.smallest(), 0.0, 0.0));

        BigAggregator big = BigAggregator.getSet();
        ExtremesAggregatorTest.assertIdentical(-3.0, ExtremesAggregatorTest.aggregate(big.maximum(), BigDecimal.valueOf(-5), BigDecimal.valueOf(-3)));
        ExtremesAggregatorTest.assertIdentical(3.0, ExtremesAggregatorTest.aggregate(big.minimum(), BigDecimal.valueOf(5), BigDecimal.valueOf(3)));
        ExtremesAggregatorTest.assertIdentical(1.0,
                ExtremesAggregatorTest.aggregate(big.smallest(), BigDecimal.ZERO, BigDecimal.valueOf(2), BigDecimal.valueOf(-1)));

        QuadrupleAggregator quadruple = QuadrupleAggregator.getSet();
        ExtremesAggregatorTest.assertIdentical(-3.0, ExtremesAggregatorTest.aggregate(quadruple.maximum(), Quadruple.valueOf(-5.0), Quadruple.valueOf(-3.0)));

        RationalAggregator rational = RationalAggregator.getSet();
        ExtremesAggregatorTest.assertIdentical(-3.0,
                ExtremesAggregatorTest.aggregate(rational.maximum(), RationalNumber.valueOf(-5.0), RationalNumber.valueOf(-3.0)));
    }

    @Test
    public void testInfinite() {

        PrimitiveAggregator primitive = PrimitiveAggregator.getSet();
        ExtremesAggregatorTest.assertIdentical(INF, ExtremesAggregatorTest.aggregate(primitive.minimum(), INF, INF));
        ExtremesAggregatorTest.assertIdentical(-INF, ExtremesAggregatorTest.aggregate(primitive.minimum(), -INF, 1.0));
        ExtremesAggregatorTest.assertIdentical(-INF, ExtremesAggregatorTest.aggregate(primitive.maximum(), -INF, -INF));
        ExtremesAggregatorTest.assertIdentical(INF, ExtremesAggregatorTest.aggregate(primitive.maximum(), INF, 1.0));
        ExtremesAggregatorTest.assertIdentical(INF, ExtremesAggregatorTest.aggregate(primitive.smallest(), INF, -INF));
        ExtremesAggregatorTest.assertIdentical(INF, ExtremesAggregatorTest.aggregate(primitive.largest(), -INF, 1.0));

        QuadrupleAggregator quadruple = QuadrupleAggregator.getSet();
        ExtremesAggregatorTest.assertIdentical(INF, ExtremesAggregatorTest.aggregate(quadruple.minimum(), Quadruple.POSITIVE_INFINITY));
        ExtremesAggregatorTest.assertIdentical(-INF, ExtremesAggregatorTest.aggregate(quadruple.minimum(), Quadruple.NEGATIVE_INFINITY, Quadruple.ONE));
        ExtremesAggregatorTest.assertIdentical(-INF, ExtremesAggregatorTest.aggregate(quadruple.maximum(), Quadruple.NEGATIVE_INFINITY));

        RationalAggregator rational = RationalAggregator.getSet();
        ExtremesAggregatorTest.assertIdentical(INF, ExtremesAggregatorTest.aggregate(rational.minimum(), RationalNumber.POSITIVE_INFINITY));
        ExtremesAggregatorTest.assertIdentical(-INF, ExtremesAggregatorTest.aggregate(rational.maximum(), RationalNumber.NEGATIVE_INFINITY));
    }

    /**
     * {@link R064Store#aggregateAll(Aggregator)} computes these directly on the array – it must agree with
     * the aggregator functions.
     */
    @Test
    public void testR064StoreAgreesWithAggregatorFunctions() {

        double[][] sets = { {}, { 1.0, -2.0, 3.0 }, { INF, INF }, { -INF, -INF }, { -INF, 1.0, INF }, { 0.0, 0.0 }, { -0.0, 0.0 } };
        Aggregator[] aggregators = { Aggregator.LARGEST, Aggregator.MAXIMUM, Aggregator.MINIMUM, Aggregator.NORM1, Aggregator.NORM2, Aggregator.SUM,
                Aggregator.SUM2 };

        for (double[] set : sets) {

            R064Store store = R064Store.FACTORY.make(set.length, 1);
            for (int i = 0; i < set.length; i++) {
                store.set(i, set[i]);
            }
            ArrayR064 array = ArrayR064.wrap(set.clone());

            for (Aggregator aggregator : aggregators) {
                ExtremesAggregatorTest.assertIdentical(array.aggregateAll(aggregator).doubleValue(), store.aggregateAll(aggregator).doubleValue());
            }
        }
    }

    @Test
    public void testResetIsEmpty() {
        ExtremesAggregatorTest.assertResetIsEmpty(PrimitiveAggregator.getSet(), Double.valueOf(-7.0));
        ExtremesAggregatorTest.assertResetIsEmpty(QuadrupleAggregator.getSet(), Quadruple.valueOf(-7.0));
        ExtremesAggregatorTest.assertResetIsEmpty(BigAggregator.getSet(), BigDecimal.valueOf(-7));
        ExtremesAggregatorTest.assertResetIsEmpty(RationalAggregator.getSet(), RationalNumber.valueOf(-7.0));
        ExtremesAggregatorTest.assertResetIsEmpty(ComplexAggregator.getSet(), ComplexNumber.valueOf(-7.0));
        ExtremesAggregatorTest.assertResetIsEmpty(QuaternionAggregator.getSet(), Quaternion.valueOf(-7.0));
    }

}

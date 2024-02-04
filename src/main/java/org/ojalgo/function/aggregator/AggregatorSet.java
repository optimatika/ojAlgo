/*
 * Copyright 1997-2024 Optimatika
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

/**
 * Do not cache instances of this class! The methods {@linkplain BigAggregator#getSet()},
 * {@linkplain ComplexAggregator#getSet()} and {@linkplain PrimitiveAggregator#getSet()} return threadlocal
 * instances, and when you access the individual aggregators they are {@linkplain AggregatorFunction#reset()}
 * for you.
 *
 * @author apete
 */
public abstract class AggregatorSet<N extends Comparable<N>> {

    protected AggregatorSet() {
        super();
    }

    /**
     * Average value
     */
    public abstract AggregatorFunction<N> average();

    /**
     * Count of non-zero elements
     */
    public abstract AggregatorFunction<N> cardinality();

    public final AggregatorFunction<N> get(final Aggregator aggregator) {

        switch (aggregator) {

        case AVERAGE:

            return this.average();

        case CARDINALITY:

            return this.cardinality();

        case LARGEST:

            return this.largest();

        case MAXIMUM:

            return this.maximum();

        case MINIMUM:

            return this.minimum();

        case NORM1:

            return this.norm1();

        case NORM2:

            return this.norm2();

        case PRODUCT:

            return this.product();

        case PRODUCT2:

            return this.product2();

        case SMALLEST:

            return this.smallest();

        case SUM:

            return this.sum();

        case SUM2:

            return this.sum2();

        default:

            throw new IllegalArgumentException();
        }
    }

    /**
     * Largest absolute value
     */
    public abstract AggregatorFunction<N> largest();

    /**
     * Max value
     */
    public abstract AggregatorFunction<N> maximum();

    /**
     * Min value
     */
    public abstract AggregatorFunction<N> minimum();

    /**
     * Sum of absolute values
     */
    public abstract AggregatorFunction<N> norm1();

    /**
     * Square root of sum of squared values
     */
    public abstract AggregatorFunction<N> norm2();

    /**
     * Running product
     */
    public abstract AggregatorFunction<N> product();

    /**
     * Running product of squares
     */
    public abstract AggregatorFunction<N> product2();

    /**
     * Smallest non-zero absolute value
     */
    public abstract AggregatorFunction<N> smallest();

    /**
     * Running sum
     */
    public abstract AggregatorFunction<N> sum();

    /**
     * Running sum of squares
     */
    public abstract AggregatorFunction<N> sum2();

}

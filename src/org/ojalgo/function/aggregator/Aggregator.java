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
package org.ojalgo.function.aggregator;

import java.math.BigDecimal;

import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.scalar.RationalNumber;

public enum Aggregator {

    CARDINALITY, LARGEST, MAXIMUM, MINIMUM, NORM1, NORM2, PRODUCT, PRODUCT2, SMALLEST, SUM, SUM2;

    /**
     * @deprecated v42 Use {@link #getFunction(AggregatorSet)} instead.
     */
    @Deprecated
    public final AggregatorFunction<BigDecimal> getBigFunction() {
        return this.getFunction(BigAggregator.getSet());
    }

    /**
     * @deprecated v42 Use {@link #getFunction(AggregatorSet)} instead.
     */
    @Deprecated
    public final AggregatorFunction<ComplexNumber> getComplexFunction() {
        return this.getFunction(ComplexAggregator.getSet());
    }

    public final <N extends Number> AggregatorFunction<N> getFunction(final AggregatorSet<N> collection) {
        return collection.get(this);
    }

    /**
     * @deprecated v42 Use {@link #getFunction(AggregatorSet)} instead.
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public final <N extends Number> AggregatorFunction<N> getFunction(final Class<?> scalarType) {
        if (double.class.isAssignableFrom(scalarType) || Double.class.isAssignableFrom(scalarType)) {
            return (AggregatorFunction<N>) this.getFunction(PrimitiveAggregator.getSet());
        } else if (ComplexNumber.class.isAssignableFrom(scalarType)) {
            return (AggregatorFunction<N>) this.getFunction(ComplexAggregator.getSet());
        } else if (BigDecimal.class.isAssignableFrom(scalarType)) {
            return (AggregatorFunction<N>) this.getFunction(BigAggregator.getSet());
        } else if (RationalNumber.class.isAssignableFrom(scalarType)) {
            return (AggregatorFunction<N>) this.getFunction(RationalAggregator.getSet());
        } else if (Quaternion.class.isAssignableFrom(scalarType)) {
            return (AggregatorFunction<N>) this.getFunction(QuaternionAggregator.getSet());
        } else {
            return null;
        }
    }

    /**
     * @deprecated v42 Use {@link #getFunction(AggregatorSet)} instead.
     */
    @Deprecated
    public final AggregatorFunction<Double> getPrimitiveFunction() {
        return this.getFunction(PrimitiveAggregator.getSet());
    }

    /**
     * @deprecated v42 Use {@link #getFunction(AggregatorSet)} instead.
     */
    @Deprecated
    public final AggregatorFunction<Quaternion> getQuaternionFunction() {
        return this.getFunction(QuaternionAggregator.getSet());
    }

    /**
     * @deprecated v42 Use {@link #getFunction(AggregatorSet)} instead.
     */
    @Deprecated
    public final AggregatorFunction<RationalNumber> getRationalFunction() {
        return this.getFunction(RationalAggregator.getSet());
    }

}

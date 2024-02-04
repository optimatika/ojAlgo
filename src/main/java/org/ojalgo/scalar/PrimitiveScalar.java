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
package org.ojalgo.scalar;

import java.math.BigDecimal;
import java.math.MathContext;

import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.type.NumberDefinition;
import org.ojalgo.type.context.NumberContext;
import org.ojalgo.type.context.NumberContext.Enforceable;

public final class PrimitiveScalar implements Scalar<Double>, Enforceable<PrimitiveScalar> {

    public static final Scalar.Factory<Double> FACTORY = new Scalar.Factory<>() {

        @Override
        public Double cast(final Comparable<?> number) {
            return NumberDefinition.doubleValue(number);
        }

        @Override
        public Double cast(final double value) {
            return value;
        }

        @Override
        public PrimitiveScalar convert(final Comparable<?> number) {
            return PrimitiveScalar.valueOf(number);
        }

        @Override
        public PrimitiveScalar convert(final double value) {
            return PrimitiveScalar.of(value);
        }

        @Override
        public PrimitiveScalar one() {
            return ONE;
        }

        @Override
        public PrimitiveScalar zero() {
            return ZERO;
        }

    };

    public static final PrimitiveScalar NaN = new PrimitiveScalar(PrimitiveMath.NaN);
    public static final PrimitiveScalar NEG = new PrimitiveScalar(PrimitiveMath.NEG);
    public static final PrimitiveScalar NEGATIVE_INFINITY = new PrimitiveScalar(PrimitiveMath.NEGATIVE_INFINITY);
    public static final PrimitiveScalar ONE = new PrimitiveScalar(PrimitiveMath.ONE);
    public static final PrimitiveScalar POSITIVE_INFINITY = new PrimitiveScalar(PrimitiveMath.POSITIVE_INFINITY);
    public static final PrimitiveScalar TWO = new PrimitiveScalar(PrimitiveMath.TWO);
    public static final PrimitiveScalar ZERO = new PrimitiveScalar(PrimitiveMath.ZERO);

    static final NumberContext CONTEXT = NumberContext.ofMath(MathContext.DECIMAL64);

    public static boolean isAbsolute(final double value) {
        return value >= PrimitiveMath.ZERO;
    }

    public static boolean isInfinite(final double value) {
        return Double.isInfinite(value);
    }

    public static boolean isNaN(final double value) {
        return Double.isNaN(value);
    }

    public static boolean isSmall(final double comparedTo, final double value) {
        return PrimitiveScalar.CONTEXT.isSmall(comparedTo, value);
    }

    public static PrimitiveScalar of(final double value) {
        return new PrimitiveScalar(value);
    }

    public static PrimitiveScalar valueOf(final Comparable<?> number) {
        return PrimitiveScalar.of(NumberDefinition.doubleValue(number));
    }

    public static PrimitiveScalar valueOf(final double value) {
        return PrimitiveScalar.of(value);
    }

    private final double myValue;

    public PrimitiveScalar() {

        super();

        myValue = PrimitiveMath.ZERO;
    }

    private PrimitiveScalar(final double value) {

        super();

        myValue = value;
    }

    @Override
    public PrimitiveScalar add(final double arg) {
        return new PrimitiveScalar(myValue + arg);
    }

    @Override
    public PrimitiveScalar add(final Double arg) {
        return new PrimitiveScalar(myValue + arg);
    }

    @Override
    public PrimitiveScalar add(final float scalarAddend) {
        return this.add((double) scalarAddend);
    }

    @Override
    public int compareTo(final Double reference) {
        return NumberContext.compare(myValue, reference);
    }

    @Override
    public PrimitiveScalar conjugate() {
        return this;
    }

    @Override
    public PrimitiveScalar divide(final double arg) {
        return new PrimitiveScalar(myValue / arg);
    }

    @Override
    public PrimitiveScalar divide(final Double arg) {
        return new PrimitiveScalar(myValue / arg);
    }

    @Override
    public PrimitiveScalar divide(final float scalarDivisor) {
        return this.divide((double) scalarDivisor);
    }

    @Override
    public double doubleValue() {
        return myValue;
    }

    @Override
    public PrimitiveScalar enforce(final NumberContext context) {
        return new PrimitiveScalar(context.enforce(myValue));
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PrimitiveScalar)) {
            return false;
        }
        PrimitiveScalar other = (PrimitiveScalar) obj;
        if (Double.doubleToLongBits(myValue) != Double.doubleToLongBits(other.myValue)) {
            return false;
        }
        return true;
    }

    @Override
    public float floatValue() {
        return (float) myValue;
    }

    @Override
    public Double get() {
        return myValue;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(myValue);
        return (prime * result) + (int) (temp ^ (temp >>> 32));
    }

    @Override
    public int intValue() {
        return (int) myValue;
    }

    @Override
    public PrimitiveScalar invert() {
        return new PrimitiveScalar(PrimitiveMath.ONE / myValue);
    }

    @Override
    public boolean isAbsolute() {
        return PrimitiveScalar.isAbsolute(myValue);
    }

    @Override
    public boolean isSmall(final double comparedTo) {
        return PrimitiveScalar.isSmall(comparedTo, myValue);
    }

    @Override
    public long longValue() {
        return (long) myValue;
    }

    @Override
    public PrimitiveScalar multiply(final double arg) {
        return new PrimitiveScalar(myValue * arg);
    }

    @Override
    public PrimitiveScalar multiply(final Double arg) {
        return new PrimitiveScalar(myValue * arg);
    }

    @Override
    public PrimitiveScalar multiply(final float scalarMultiplicand) {
        return this.multiply((double) scalarMultiplicand);
    }

    @Override
    public PrimitiveScalar negate() {
        return new PrimitiveScalar(-myValue);
    }

    @Override
    public double norm() {
        return PrimitiveMath.ABS.invoke(myValue);
    }

    @Override
    public PrimitiveScalar power(final int power) {

        double retVal = PrimitiveMath.ONE;

        for (int p = 0; p < power; p++) {
            retVal *= myValue;
        }

        return new PrimitiveScalar(retVal);
    }

    @Override
    public PrimitiveScalar signum() {
        return new PrimitiveScalar(PrimitiveMath.SIGNUM.invoke(myValue));
    }

    @Override
    public PrimitiveScalar subtract(final double arg) {
        return new PrimitiveScalar(myValue - arg);
    }

    @Override
    public PrimitiveScalar subtract(final Double arg) {
        return new PrimitiveScalar(myValue - arg);
    }

    @Override
    public PrimitiveScalar subtract(final float scalarSubtrahend) {
        return this.subtract((double) scalarSubtrahend);
    }

    @Override
    public BigDecimal toBigDecimal() {
        return new BigDecimal(myValue, PrimitiveScalar.CONTEXT.getMathContext());
    }

    @Override
    public String toString() {
        return Double.toString(myValue);
    }

    @Override
    public String toString(final NumberContext context) {
        return context.enforce(this.toBigDecimal()).toString();
    }

}

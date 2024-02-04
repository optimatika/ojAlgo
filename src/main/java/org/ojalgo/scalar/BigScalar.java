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

import org.ojalgo.function.constant.BigMath;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.type.TypeUtils;
import org.ojalgo.type.context.NumberContext;
import org.ojalgo.type.context.NumberContext.Enforceable;

/**
 * A {@link BigDecimal} based implementation of the {@link Scalar} interface. Most/all other implementations
 * are based on primitive components. This implementation exists for historical reasons, and is now primarily
 * used for tests.
 */
public final class BigScalar implements Scalar<BigDecimal>, Enforceable<BigScalar> {

    public static final Scalar.Factory<BigDecimal> FACTORY = new Scalar.Factory<>() {

        @Override
        public BigDecimal cast(final Comparable<?> number) {
            return TypeUtils.toBigDecimal(number);
        }

        @Override
        public BigDecimal cast(final double value) {
            return new BigDecimal(value);
        }

        @Override
        public BigScalar convert(final Comparable<?> number) {
            return BigScalar.valueOf(number);
        }

        @Override
        public BigScalar convert(final double value) {
            return BigScalar.valueOf(value);
        }

        @Override
        public BigScalar one() {
            return ONE;
        }

        @Override
        public BigScalar zero() {
            return ZERO;
        }

    };

    public static final BigScalar NEG = new BigScalar(BigDecimal.ONE.negate());
    public static final BigScalar ONE = new BigScalar(BigDecimal.ONE);
    public static final BigScalar TWO = new BigScalar(BigDecimal.ONE.add(BigDecimal.ONE));
    public static final BigScalar ZERO = new BigScalar(BigDecimal.ZERO);

    static final NumberContext CONTEXT = NumberContext.ofMath(MathContext.DECIMAL128);

    public static boolean isAbsolute(final BigDecimal value) {
        return value.signum() >= 0;
    }

    public static boolean isSmall(final double comparedTo, final BigDecimal value) {
        return (value.signum() == 0) || BigScalar.CONTEXT.isSmall(comparedTo, value.doubleValue());
    }

    public static BigScalar of(final BigDecimal value) {
        return new BigScalar(value);
    }

    public static BigScalar valueOf(final Comparable<?> number) {

        if (number != null) {

            if (number instanceof BigScalar) {

                return (BigScalar) number;

            } else {

                return new BigScalar(TypeUtils.toBigDecimal(number));
            }

        } else {

            return ZERO;
        }
    }

    public static BigScalar valueOf(final double value) {
        return new BigScalar(BigDecimal.valueOf(value));
    }

    private final BigDecimal myNumber;

    public BigScalar() {

        super();

        myNumber = BigMath.ZERO;
    }

    private BigScalar(final BigDecimal number) {

        super();

        myNumber = number;
    }

    @Override
    public BigScalar add(final BigDecimal arg) {
        return new BigScalar(myNumber.add(arg));
    }

    @Override
    public BigScalar add(final double arg) {
        return this.add(new BigDecimal(arg));
    }

    @Override
    public BigScalar add(final float scalarAddend) {
        return this.add((double) scalarAddend);
    }

    @Override
    public int compareTo(final BigDecimal reference) {
        return myNumber.compareTo(reference);
    }

    @Override
    public BigScalar conjugate() {
        return this;
    }

    @Override
    public BigScalar divide(final BigDecimal arg) {
        return new BigScalar(myNumber.divide(arg, BigScalar.CONTEXT.getMathContext()));
    }

    @Override
    public BigScalar divide(final double arg) {
        return this.divide(new BigDecimal(arg));
    }

    @Override
    public BigScalar divide(final float scalarDivisor) {
        return this.divide((double) scalarDivisor);
    }

    @Override
    public double doubleValue() {
        return myNumber.doubleValue();
    }

    @Override
    public BigScalar enforce(final NumberContext context) {
        return new BigScalar(context.enforce(myNumber));
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BigScalar)) {
            return false;
        }
        BigScalar other = (BigScalar) obj;
        if (myNumber == null) {
            if (other.myNumber != null) {
                return false;
            }
        } else if (!myNumber.equals(other.myNumber)) {
            return false;
        }
        return true;
    }

    @Override
    public float floatValue() {
        return myNumber.floatValue();
    }

    @Override
    public BigDecimal get() {
        return myNumber;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        return (prime * result) + ((myNumber == null) ? 0 : myNumber.hashCode());
    }

    @Override
    public int intValue() {
        return myNumber.intValueExact();
    }

    @Override
    public BigScalar invert() {
        return ONE.divide(myNumber);
    }

    @Override
    public boolean isAbsolute() {
        return BigScalar.isAbsolute(myNumber);
    }

    @Override
    public boolean isSmall(final double comparedTo) {
        return BigScalar.CONTEXT.isSmall(comparedTo, this.doubleValue());
    }

    @Override
    public long longValue() {
        return myNumber.longValueExact();
    }

    @Override
    public BigScalar multiply(final BigDecimal arg) {
        return new BigScalar(myNumber.multiply(arg));
    }

    @Override
    public BigScalar multiply(final double arg) {
        return this.multiply(new BigDecimal(arg));
    }

    @Override
    public BigScalar multiply(final float scalarMultiplicand) {
        return this.multiply((double) scalarMultiplicand);
    }

    @Override
    public BigScalar negate() {
        return new BigScalar(myNumber.negate());
    }

    @Override
    public double norm() {
        return PrimitiveMath.ABS.invoke(myNumber.doubleValue());
    }

    @Override
    public BigScalar power(final int power) {

        BigScalar retVal = ONE;

        for (int p = 0; p < power; p++) {
            retVal = retVal.multiply(myNumber);
        }

        return retVal;
    }

    @Override
    public BigScalar signum() {
        return new BigScalar(BigMath.SIGNUM.invoke(myNumber));
    }

    @Override
    public BigScalar subtract(final BigDecimal arg) {
        return new BigScalar(myNumber.subtract(arg));
    }

    @Override
    public BigScalar subtract(final double arg) {
        return this.subtract(new BigDecimal(arg));
    }

    @Override
    public BigScalar subtract(final float scalarSubtrahend) {
        return this.subtract((double) scalarSubtrahend);
    }

    @Override
    public BigDecimal toBigDecimal() {
        return myNumber;
    }

    @Override
    public String toString() {
        return myNumber.toString();
    }

    @Override
    public String toString(final NumberContext context) {
        return context.enforce(myNumber).toString();
    }

}

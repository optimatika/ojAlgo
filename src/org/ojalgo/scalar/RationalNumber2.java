/*
 * Copyright 1997-2017 Optimatika
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

import static org.ojalgo.function.PrimitiveFunction.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.type.TypeUtils;
import org.ojalgo.type.context.NumberContext;
import org.ojalgo.type.context.NumberContext.Enforceable;

final class RationalNumber2 extends Number implements Scalar<RationalNumber2>, Enforceable<RationalNumber2> {

    public static final Scalar.Factory<RationalNumber2> FACTORY = new Scalar.Factory<RationalNumber2>() {

        public RationalNumber2 cast(final double value) {
            return RationalNumber2.valueOf(value);
        }

        public RationalNumber2 cast(final Number number) {
            return RationalNumber2.valueOf(number);
        }

        public RationalNumber2 convert(final double value) {
            return RationalNumber2.valueOf(value);
        }

        public RationalNumber2 convert(final Number number) {
            return RationalNumber2.valueOf(number);
        }

        public RationalNumber2 one() {
            return ONE;
        }

        public RationalNumber2 zero() {
            return ZERO;
        }

    };

    public static final RationalNumber2 NaN = new RationalNumber2(0L, 0L);
    public static final RationalNumber2 NEG = new RationalNumber2(-1L, 1L);
    public static final RationalNumber2 NEGATIVE_INFINITY = new RationalNumber2(-1L, 0L);
    public static final RationalNumber2 ONE = new RationalNumber2(1L, 1L);
    public static final RationalNumber2 POSITIVE_INFINITY = new RationalNumber2(1L, 0L);
    public static final RationalNumber2 TWO = new RationalNumber2(2L, 1L);
    public static final RationalNumber2 ZERO = new RationalNumber2(0L, 1L);

    private static final String DIVIDE = " / ";
    private static final String LEFT = "(";
    private static final long LIMIT = Math.round(Math.sqrt(Long.MAX_VALUE));

    private static final String RIGHT = ")";;

    /**
     * Greatest Common Denominator
     */
    public static BigInteger gcd(final BigInteger value1, final BigInteger value2) {
        return value1.gcd(value2);
    }

    /**
     * Greatest Common Denominator
     */
    public static int gcd(int value1, int value2) {

        int retVal = 1;

        value1 = Math.abs(value1);
        value2 = Math.abs(value2);

        int tmpMax = Math.max(value1, value2);
        int tmpMin = Math.min(value1, value2);

        while (tmpMin != 0) {
            retVal = tmpMin;
            tmpMin = tmpMax % tmpMin;
            tmpMax = retVal;
        }

        return retVal;
    }

    /**
     * Greatest Common Denominator
     */
    public static long gcd(long value1, long value2) {

        long retVal = 1L;

        value1 = Math.abs(value1);
        value2 = Math.abs(value2);

        long tmpMax = Math.max(value1, value2);
        long tmpMin = Math.min(value1, value2);

        while (tmpMin != 0L) {
            retVal = tmpMin;
            tmpMin = tmpMax % tmpMin;
            tmpMax = retVal;
        }

        return retVal;
    }

    public static boolean isAbsolute(final RationalNumber2 value) {
        return value.isAbsolute();
    }

    public static boolean isInfinite(final RationalNumber2 value) {
        return ((value.getNumerator() != 0L) && (value.getDenominator() == 0L));
    }

    public static boolean isNaN(final RationalNumber2 value) {
        return ((value.getNumerator() == 0L) && (value.getDenominator() == 0L));
    }

    public static boolean isSmall(final double comparedTo, final RationalNumber2 value) {
        return value.isSmall(comparedTo);
    }

    public static RationalNumber2 of(final long numerator, final long denominator) {

        if (numerator == 0L) {
            return new RationalNumber2(numerator, 1L);
        }

        long tmpGCD = RationalNumber2.gcd(numerator, denominator);
        if (tmpGCD != 1L) {
            return new RationalNumber2(numerator / tmpGCD, denominator / tmpGCD);
        } else if (denominator > LIMIT) {
            tmpGCD = Math.round(SQRT.invoke(denominator));
            return new RationalNumber2(numerator / tmpGCD, denominator / tmpGCD);
        } else {
            return new RationalNumber2(numerator, denominator);
        }
    }

    public static RationalNumber2 valueOf(final double value) {

        if (Double.isNaN(value)) {
            return NaN;
        } else if (value == Double.POSITIVE_INFINITY) {
            return POSITIVE_INFINITY;
        } else if (value == Double.NEGATIVE_INFINITY) {
            return NEGATIVE_INFINITY;
        }

        final long valBits = Double.doubleToLongBits(value);

        final int sign = ((valBits >> 63) == 0 ? 1 : -1);
        int exponent = (int) ((valBits >> 52) & 0x7ffL);
        final long significand = (exponent == 0 ? (valBits & ((1L << 52) - 1)) << 1 : (valBits & ((1L << 52) - 1)) | (1L << 52));
        exponent -= 1075;

        if (exponent < 0) {
            return RationalNumber2.of(sign * significand, 1L << exponent);
        } else {
            return RationalNumber2.of(sign * significand * (1L << exponent), 1L);
        }
    }

    public static RationalNumber2 valueOf(final Number number) {

        if (number != null) {

            if (number instanceof RationalNumber2) {

                return (RationalNumber2) number;

            } else {

                final BigDecimal tmpBigDecimal = TypeUtils.toBigDecimal(number);

                BigInteger tmpNumerator;
                BigInteger tmpDenominator;

                final int tmpScale = tmpBigDecimal.scale();

                if (tmpScale < 0) {

                    tmpNumerator = tmpBigDecimal.unscaledValue().multiply(BigInteger.TEN.pow(-tmpScale));
                    tmpDenominator = BigInteger.ONE;

                } else {

                    final BigInteger tmpNumer = tmpBigDecimal.unscaledValue();
                    final BigInteger tmpDenom = BigInteger.TEN.pow(tmpScale);

                    final BigInteger tmpGCD = tmpNumer.gcd(tmpDenom);

                    if (tmpGCD.compareTo(BigInteger.ONE) == 1) {
                        tmpNumerator = tmpNumer.divide(tmpGCD);
                        tmpDenominator = tmpDenom.divide(tmpGCD);
                    } else {
                        tmpNumerator = tmpNumer;
                        tmpDenominator = tmpDenom;
                    }
                }

                return new RationalNumber2(tmpNumerator.longValue(), tmpDenominator.longValue());
            }

        } else {

            return ZERO;
        }
    }

    private static String toString(final RationalNumber2 aNmbr) {

        final StringBuilder retVal = new StringBuilder(LEFT);

        retVal.append(aNmbr.getNumerator());
        retVal.append(DIVIDE);
        retVal.append(aNmbr.getDenominator());

        return retVal.append(RIGHT).toString();
    }

    private transient BigDecimal myDecimal = null;

    private final long myDenominator;
    private final long myNumerator;

    private RationalNumber2() {
        this(0L, 1L);
    }

    private RationalNumber2(final long numerator, final long denominator) {

        super();

        if (denominator < 0L) {
            throw new IllegalArgumentException();
        }

        if (denominator > LIMIT) {
            throw new IllegalArgumentException();
        }

        myNumerator = numerator;
        myDenominator = denominator;
    }

    public RationalNumber2 add(final double arg) {
        return this.add(RationalNumber2.valueOf(arg));
    }

    public RationalNumber2 add(final RationalNumber2 arg) {

        if (myDenominator == arg.getDenominator()) {

            return new RationalNumber2(myNumerator + arg.getNumerator(), myDenominator);

        } else {

            final long tmpNumer = (myNumerator * arg.getDenominator()) + (arg.getNumerator() * myDenominator);
            final long tmpDenom = myDenominator * arg.getDenominator();

            return RationalNumber2.of(tmpNumer, tmpDenom);
        }
    }

    public int compareTo(final RationalNumber2 reference) {
        return this.toBigDecimal().compareTo(reference.toBigDecimal());
    }

    public RationalNumber2 conjugate() {
        return this;
    }

    public RationalNumber2 divide(final double arg) {
        return this.divide(RationalNumber2.valueOf(arg));
    }

    public RationalNumber2 divide(final RationalNumber2 arg) {

        final long tmpNumer = myNumerator * arg.getDenominator();
        final long tmpDenom = myDenominator * arg.getNumerator();

        return RationalNumber2.of(tmpNumer, tmpDenom);

    }

    @Override
    public double doubleValue() {
        return this.toBigDecimal().doubleValue();
    }

    public RationalNumber2 enforce(final NumberContext context) {
        return RationalNumber2.valueOf(this.toBigDecimal(context.getMathContext()));
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof RationalNumber2)) {
            return false;
        }
        final RationalNumber2 other = (RationalNumber2) obj;
        if (myDenominator != other.myDenominator) {
            return false;
        }
        if (myNumerator != other.myNumerator) {
            return false;
        }
        return true;
    }

    @Override
    public float floatValue() {
        return this.toBigDecimal().floatValue();
    }

    public RationalNumber2 getNumber() {
        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + (int) (myDenominator ^ (myDenominator >>> 32));
        result = (prime * result) + (int) (myNumerator ^ (myNumerator >>> 32));
        return result;
    }

    @Override
    public int intValue() {
        return this.toBigDecimal().intValue();
    }

    public RationalNumber2 invert() {
        return new RationalNumber2(myDenominator, myNumerator);
    }

    public boolean isAbsolute() {
        return myNumerator >= 0L;
    }

    public boolean isSmall(final double comparedTo) {
        return BigScalar.CONTEXT.isSmall(comparedTo, this.doubleValue());
    }

    @Override
    public long longValue() {
        return this.toBigDecimal().longValue();
    }

    public RationalNumber2 multiply(final double arg) {
        return this.multiply(RationalNumber2.valueOf(arg));
    }

    public RationalNumber2 multiply(final RationalNumber2 arg) {

        final long tmpNumer = myNumerator * arg.getNumerator();
        final long tmpDenom = myDenominator * arg.getDenominator();

        return RationalNumber2.of(tmpNumer, tmpDenom);

    }

    public RationalNumber2 negate() {
        return new RationalNumber2(-myNumerator, myDenominator);
    }

    public double norm() {
        return ABS.invoke(this.doubleValue());
    }

    public RationalNumber2 signum() {
        if (RationalNumber2.isSmall(PrimitiveMath.ONE, this)) {
            return ZERO;
        } else if (this.sign() == -1) {
            return ONE.negate();
        } else {
            return ONE;
        }
    }

    public RationalNumber2 subtract(final double arg) {
        return this.subtract(RationalNumber2.valueOf(arg));
    }

    public RationalNumber2 subtract(final RationalNumber2 arg) {

        if (myDenominator == arg.getDenominator()) {

            return new RationalNumber2(myNumerator - arg.getNumerator(), myDenominator);

        } else {

            final long tmpNumer = (myNumerator * arg.getDenominator()) - (arg.getNumerator() * myDenominator);
            final long tmpDenom = myDenominator * arg.getDenominator();

            return RationalNumber2.of(tmpNumer, tmpDenom);
        }
    }

    public BigDecimal toBigDecimal() {
        if (myDecimal == null) {
            myDecimal = this.toBigDecimal(BigScalar.CONTEXT.getMathContext());
        }
        return myDecimal;
    }

    @Override
    public String toString() {
        return RationalNumber2.toString(this);
    }

    public String toString(final NumberContext context) {
        return RationalNumber2.toString(this.enforce(context));
    }

    private int sign() {
        if (myNumerator < 0L) {
            return -1;
        } else if (myNumerator > 0L) {
            return 1;
        } else {
            return 0;
        }
    }

    private BigDecimal toBigDecimal(final MathContext context) {
        return new BigDecimal(myNumerator).divide(new BigDecimal(myDenominator), context);
    }

    long getDenominator() {
        return myDenominator;
    }

    long getNumerator() {
        return myNumerator;
    }

}

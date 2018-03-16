/*
 * Copyright 1997-2018 Optimatika
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

public final class RationalNumber extends Number implements Scalar<RationalNumber>, Enforceable<RationalNumber> {

    public static final Scalar.Factory<RationalNumber> FACTORY = new Scalar.Factory<RationalNumber>() {

        public RationalNumber cast(final double value) {
            return RationalNumber.valueOf(value);
        }

        public RationalNumber cast(final Number number) {
            return RationalNumber.valueOf(number);
        }

        public RationalNumber convert(final double value) {
            return RationalNumber.valueOf(value);
        }

        public RationalNumber convert(final Number number) {
            return RationalNumber.valueOf(number);
        }

        public RationalNumber one() {
            return ONE;
        }

        public RationalNumber zero() {
            return ZERO;
        }

    };

    public static final RationalNumber MAX = new RationalNumber(Long.MAX_VALUE, 1L);
    public static final RationalNumber MIN = new RationalNumber(Long.MIN_VALUE, 1L);
    public static final RationalNumber NaN = new RationalNumber(0L, 0L);
    public static final RationalNumber NEG = new RationalNumber(-1L, 1L);
    public static final RationalNumber NEGATIVE_INFINITY = new RationalNumber(-1L, 0L);
    public static final RationalNumber ONE = new RationalNumber(1L, 1L);
    public static final RationalNumber POSITIVE_INFINITY = new RationalNumber(1L, 0L);
    public static final RationalNumber TWO = new RationalNumber(2L, 1L);
    public static final RationalNumber ZERO = new RationalNumber(0L, 1L);

    private static final String DIVIDE = " / ";
    private static final String LEFT = "(";
    private static final int MAX_BITS = BigInteger.valueOf(Long.MAX_VALUE).bitLength();
    private static final String RIGHT = ")";
    private static final long SAFE_LIMIT = Math.round(Math.sqrt(Long.MAX_VALUE / 2L));

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

    public static boolean isAbsolute(final RationalNumber value) {
        return value.isAbsolute();
    }

    public static boolean isInfinite(final RationalNumber value) {
        return value.getNumerator() != 0L && value.getDenominator() == 0L;
    }

    public static boolean isNaN(final RationalNumber value) {
        return value.getNumerator() == 0L && value.getDenominator() == 0L;
    }

    public static boolean isSmall(final double comparedTo, final RationalNumber value) {
        return value.isSmall(comparedTo);
    }

    public static RationalNumber of(final long numerator, final long denominator) {

        if (denominator == 0L) {
            if (numerator > 0L) {
                return POSITIVE_INFINITY;
            } else if (numerator < 0L) {
                return NEGATIVE_INFINITY;
            } else {
                return NaN;
            }
        } else if (numerator == 0L) {
            return ZERO;
        }

        final long gcd = RationalNumber.gcd(numerator, denominator);
        if (gcd != 1L) {
            return new RationalNumber(numerator / gcd, denominator / gcd);
        } else {
            return new RationalNumber(numerator, denominator);
        }
    }

    public static RationalNumber valueOf(final double value) {

        if (Double.isNaN(value)) {
            return NaN;
        } else if (value == Double.POSITIVE_INFINITY) {
            return POSITIVE_INFINITY;
        } else if (value == Double.NEGATIVE_INFINITY) {
            return NEGATIVE_INFINITY;
        }

        final long bits = Double.doubleToLongBits(value);

        // Please refer to {@link Double#doubleToLongBits(long)} javadoc
        final int s = ((bits >> 63) == 0) ? 1 : -1;
        final int e = (int) ((bits >> 52) & 0x7ffL);
        long m = (e == 0) ?
                (bits & 0xfffffffffffffL) << 1 :
                (bits & 0xfffffffffffffL) | 0x10000000000000L;
        // Now we're looking for s * m * 2^{e - 1075}, 1075 being bias of 1023 plus 52 positions of binary fraction

        int exponent = e - 1075;

        if (exponent >= 0) {
            return new RationalNumber(s * (m << exponent), 1);
        }

        while ((m & 1) == 0 && exponent < 0) {
            m >>= 1;
            exponent++;
        }
        return new RationalNumber(s * m, 1L << -exponent);
    }

    public static RationalNumber valueOf(final Number number) {

        if (number != null) {

            if (number instanceof RationalNumber) {

                return (RationalNumber) number;

            } else if (number instanceof Double) {

                return RationalNumber.valueOf(number.doubleValue());

            } else {

                final BigDecimal tmpBigDecimal = TypeUtils.toBigDecimal(number);

                BigInteger retNumer;
                BigInteger retDenom;

                final int scale = tmpBigDecimal.scale();

                if (scale < 0) {

                    retNumer = tmpBigDecimal.unscaledValue().multiply(BigInteger.TEN.pow(-scale));
                    retDenom = BigInteger.ONE;

                } else {

                    retNumer = tmpBigDecimal.unscaledValue();
                    retDenom = BigInteger.TEN.pow(scale);

                    final BigInteger gcd = retNumer.gcd(retDenom);
                    if (gcd.compareTo(BigInteger.ONE) == 1) {
                        retNumer = retNumer.divide(gcd);
                        retDenom = retDenom.divide(gcd);
                    }
                }

                final int bits = Math.max(retNumer.bitLength(), retDenom.bitLength());

                if (bits > MAX_BITS) {
                    final int shift = bits - MAX_BITS;
                    retNumer = retNumer.shiftRight(shift);
                    retDenom = retDenom.shiftRight(shift);
                }

                //                final BigDecimal recreated = BigFunction.DIVIDE.invoke(new BigDecimal(retNumer), new BigDecimal(retDenom));
                //                if (recreated.plus(MathContext.DECIMAL32).compareTo(tmpBigDecimal.plus(MathContext.DECIMAL32)) != 0) {
                //                    BasicLogger.debug("{} != {}", tmpBigDecimal, recreated);
                //                }

                return new RationalNumber(retNumer.longValue(), retDenom.longValue());
            }

        } else {

            return ZERO;
        }
    }

    private static String toString(final RationalNumber aNmbr) {

        final StringBuilder retVal = new StringBuilder(LEFT);

        retVal.append(aNmbr.getNumerator());
        retVal.append(DIVIDE);
        retVal.append(aNmbr.getDenominator());

        return retVal.append(RIGHT).toString();
    }

    static RationalNumber add(final RationalNumber arg1, final RationalNumber arg2) {

        final BigInteger numer1 = BigInteger.valueOf(arg1.getNumerator());
        final BigInteger denom1 = BigInteger.valueOf(arg1.getDenominator());

        final BigInteger numer2 = BigInteger.valueOf(arg2.getNumerator());
        final BigInteger denom2 = BigInteger.valueOf(arg2.getDenominator());

        BigInteger retNumer = numer1.multiply(denom2).add(numer2.multiply(denom1));
        BigInteger retDenom = denom1.multiply(denom2);

        final BigInteger gcd = retNumer.gcd(retDenom);
        if (gcd.compareTo(BigInteger.ONE) == 1) {
            retNumer = retNumer.divide(gcd);
            retDenom = retDenom.divide(gcd);
        }

        final int bits = Math.max(retNumer.bitLength(), retDenom.bitLength());

        if (bits > MAX_BITS) {
            final int shift = bits - MAX_BITS;
            retNumer = retNumer.shiftRight(shift);
            retDenom = retDenom.shiftRight(shift);
        }

        return RationalNumber.of(retNumer.longValueExact(), retDenom.longValueExact());
    }

    static RationalNumber divide(final RationalNumber arg1, final RationalNumber arg2) {

        final BigInteger numer1 = BigInteger.valueOf(arg1.getNumerator());
        final BigInteger denom1 = BigInteger.valueOf(arg1.getDenominator());

        final BigInteger numer2 = BigInteger.valueOf(arg2.getNumerator());
        final BigInteger denom2 = BigInteger.valueOf(arg2.getDenominator());

        BigInteger retNumer = numer1.multiply(denom2);
        BigInteger retDenom = denom1.multiply(numer2);

        if (retDenom.signum() > 0) {
            retNumer = retNumer.negate();
            retDenom = retDenom.negate();
        }

        final BigInteger gcd = retNumer.gcd(retDenom);
        if (gcd.compareTo(BigInteger.ONE) == 1) {
            retNumer = retNumer.divide(gcd);
            retDenom = retDenom.divide(gcd);
        }

        final int bits = Math.max(retNumer.bitLength(), retDenom.bitLength());

        if (bits > MAX_BITS) {
            final int shift = bits - MAX_BITS;
            retNumer = retNumer.shiftRight(shift);
            retDenom = retDenom.shiftRight(shift);
        }

        return RationalNumber.of(retNumer.longValueExact(), retDenom.longValueExact());
    }

    static RationalNumber multiply(final RationalNumber arg1, final RationalNumber arg2) {

        final BigInteger numer1 = BigInteger.valueOf(arg1.getNumerator());
        final BigInteger denom1 = BigInteger.valueOf(arg1.getDenominator());

        final BigInteger numer2 = BigInteger.valueOf(arg2.getNumerator());
        final BigInteger denom2 = BigInteger.valueOf(arg2.getDenominator());

        BigInteger retNumer = numer1.multiply(numer2);
        BigInteger retDenom = denom1.multiply(denom2);

        if (retDenom.signum() < 0) {
            retNumer = retNumer.negate();
            retDenom = retDenom.negate();
        }

        final BigInteger gcd = retNumer.gcd(retDenom);
        if (gcd.compareTo(BigInteger.ONE) == 1) {
            retNumer = retNumer.divide(gcd);
            retDenom = retDenom.divide(gcd);
        }

        final int bits = Math.max(retNumer.bitLength(), retDenom.bitLength());

        if (bits > MAX_BITS) {
            final int shift = bits - MAX_BITS;
            retNumer = retNumer.shiftRight(shift);
            retDenom = retDenom.shiftRight(shift);
        }

        return RationalNumber.of(retNumer.longValueExact(), retDenom.longValueExact());
    }

    static RationalNumber subtract(final RationalNumber arg1, final RationalNumber arg2) {

        final BigInteger numer1 = BigInteger.valueOf(arg1.getNumerator());
        final BigInteger denom1 = BigInteger.valueOf(arg1.getDenominator());

        final BigInteger numer2 = BigInteger.valueOf(arg2.getNumerator());
        final BigInteger denom2 = BigInteger.valueOf(arg2.getDenominator());

        BigInteger retNumer = numer1.multiply(denom2).subtract(numer2.multiply(denom1));
        BigInteger retDenom = denom1.multiply(denom2);

        final BigInteger gcd = retNumer.gcd(retDenom);
        if (gcd.compareTo(BigInteger.ONE) == 1) {
            retNumer = retNumer.divide(gcd);
            retDenom = retDenom.divide(gcd);
        }

        final int bits = Math.max(retNumer.bitLength(), retDenom.bitLength());

        if (bits > MAX_BITS) {
            final int shift = bits - MAX_BITS;
            retNumer = retNumer.shiftRight(shift);
            retDenom = retDenom.shiftRight(shift);
        }

        return RationalNumber.of(retNumer.longValueExact(), retDenom.longValueExact());
    }

    static RationalNumber valueOf2(final double value) {

        if (Double.isNaN(value)) {
            return NaN;
        } else if (value == Double.POSITIVE_INFINITY) {
            return POSITIVE_INFINITY;
        } else if (value == Double.NEGATIVE_INFINITY) {
            return NEGATIVE_INFINITY;
        } else if (NumberContext.compare(value, 0.0) == 0) {
            return ZERO;
        } else if (NumberContext.compare(value, 1.0) == 0) {
            return ONE;
        } else if (NumberContext.compare(value, -1.0) == 0) {
            return NEG;
        } else if (NumberContext.compare(value, 2.0) == 0) {
            return TWO;
        }

        final long numer;
        final long denom;

        if (Math.abs(value) > 1.0) {
            numer = Long.MAX_VALUE;
            denom = Math.max(Math.round(numer / value), 1L);
        } else {
            denom = Long.MAX_VALUE;
            numer = Math.round(denom * value);
        }

        final long gcd = RationalNumber.gcd(numer, denom);
        if (gcd != 1L) {
            return new RationalNumber(numer / gcd, denom / gcd);
        } else {
            return new RationalNumber(numer, denom);
        }
    }

    private transient BigDecimal myDecimal = null;
    private final long myDenominator;

    private final long myNumerator;

    public RationalNumber() {
        this(0L, 1L);
    }

    private RationalNumber(final long numerator, final long denominator) {
        if (denominator < 0L) {
            myNumerator = -numerator;
            myDenominator = -denominator;
        } else {
            myNumerator = numerator;
            myDenominator = denominator;
        }

        if ((denominator == 0L) && (Math.abs(numerator) > 1L)) {
            throw new ArithmeticException("n / 0, where abs(n) > 1");
        }
    }

    public RationalNumber add(final double arg) {
        return this.add(RationalNumber.valueOf(arg));
    }

    public RationalNumber add(final RationalNumber arg) {

        if (Math.max(this.size(), arg.size()) <= SAFE_LIMIT) {

            if (myDenominator == arg.getDenominator()) {

                return new RationalNumber(myNumerator + arg.getNumerator(), myDenominator);

            } else {

                final long retNumer = (myNumerator * arg.getDenominator()) + (arg.getNumerator() * myDenominator);
                final long retDenom = myDenominator * arg.getDenominator();

                return RationalNumber.of(retNumer, retDenom);
            }

        } else {

            return RationalNumber.add(this, arg);
        }
    }

    public int compareTo(final RationalNumber reference) {
        return this.toBigDecimal().compareTo(reference.toBigDecimal());
    }

    public RationalNumber conjugate() {
        return this;
    }

    public RationalNumber divide(final double arg) {
        return this.divide(RationalNumber.valueOf(arg));
    }

    public RationalNumber divide(final RationalNumber arg) {

        if (Math.max(this.size(), arg.size()) <= SAFE_LIMIT) {

            final long retNumer = myNumerator * arg.getDenominator();
            final long retDenom = myDenominator * arg.getNumerator();

            return RationalNumber.of(retNumer, retDenom);

        } else {

            return RationalNumber.divide(this, arg);
        }
    }

    @Override
    public double doubleValue() {
        return this.toBigDecimal().doubleValue();
    }

    public RationalNumber enforce(final NumberContext context) {
        return RationalNumber.valueOf(this.toBigDecimal(context.getMathContext()));
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof RationalNumber)) {
            return false;
        }
        final RationalNumber other = (RationalNumber) obj;
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

    public RationalNumber get() {
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

    public RationalNumber invert() {
        return new RationalNumber(myDenominator, myNumerator);
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

    public RationalNumber multiply(final double arg) {
        return this.multiply(RationalNumber.valueOf(arg));
    }

    public RationalNumber multiply(final RationalNumber arg) {

        if (Math.max(this.size(), arg.size()) <= SAFE_LIMIT) {

            final long retNumer = myNumerator * arg.getNumerator();
            final long retDenom = myDenominator * arg.getDenominator();

            return RationalNumber.of(retNumer, retDenom);

        } else {

            return RationalNumber.multiply(this, arg);
        }
    }

    public RationalNumber negate() {
        return new RationalNumber(-myNumerator, myDenominator);
    }

    public double norm() {
        return ABS.invoke(this.doubleValue());
    }

    public RationalNumber signum() {
        if (RationalNumber.isSmall(PrimitiveMath.ONE, this)) {
            return ZERO;
        } else if (this.sign() == -1) {
            return ONE.negate();
        } else {
            return ONE;
        }
    }

    public RationalNumber subtract(final double arg) {
        return this.subtract(RationalNumber.valueOf(arg));
    }

    public RationalNumber subtract(final RationalNumber arg) {

        if (Math.max(this.size(), arg.size()) <= SAFE_LIMIT) {

            if (myDenominator == arg.getDenominator()) {

                return new RationalNumber(myNumerator - arg.getNumerator(), myDenominator);

            } else {

                final long retNumer = (myNumerator * arg.getDenominator()) - (arg.getNumerator() * myDenominator);
                final long retDenom = myDenominator * arg.getDenominator();

                return RationalNumber.of(retNumer, retDenom);
            }

        } else {

            return RationalNumber.subtract(this, arg);
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
        return RationalNumber.toString(this);
    }

    public String toString(final NumberContext context) {
        return RationalNumber.toString(this.enforce(context));
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

    private long size() {
        return Math.max(Math.abs(myNumerator), myDenominator);
    }

    private BigDecimal toBigDecimal(final MathContext context) {
        if(RationalNumber.isNaN(this)) {
            return BigDecimal.valueOf(Double.POSITIVE_INFINITY);
        }
        return new BigDecimal(myNumerator).divide(new BigDecimal(myDenominator), context);
    }

    long getDenominator() {
        return myDenominator;
    }

    long getNumerator() {
        return myNumerator;
    }

}

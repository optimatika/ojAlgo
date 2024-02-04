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
import java.math.BigInteger;
import java.math.MathContext;

import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.type.TypeUtils;
import org.ojalgo.type.context.NumberContext;

public final class RationalNumber implements SelfDeclaringScalar<RationalNumber> {

    public static final Scalar.Factory<RationalNumber> FACTORY = new Scalar.Factory<>() {

        @Override
        public RationalNumber cast(final Comparable<?> number) {
            return RationalNumber.valueOf(number);
        }

        @Override
        public RationalNumber cast(final double value) {
            return RationalNumber.valueOf(value);
        }

        @Override
        public RationalNumber convert(final Comparable<?> number) {
            return RationalNumber.valueOf(number);
        }

        @Override
        public RationalNumber convert(final double value) {
            return RationalNumber.valueOf(value);
        }

        @Override
        public RationalNumber one() {
            return ONE;
        }

        @Override
        public RationalNumber zero() {
            return ZERO;
        }

    };

    public static final RationalNumber MAX_VALUE = new RationalNumber(Long.MAX_VALUE, 1L);
    public static final RationalNumber MIN_VALUE = new RationalNumber(Long.MIN_VALUE, 1L);
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

    public static boolean isAbsolute(final RationalNumber value) {
        return value.isAbsolute();
    }

    public static boolean isInfinite(final RationalNumber value) {
        return ((value.getNumerator() != 0L) && (value.getDenominator() == 0L));
    }

    public static boolean isNaN(final RationalNumber value) {
        return ((value.getNumerator() == 0L) && (value.getDenominator() == 0L));
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

    public static RationalNumber parse(final CharSequence plainNumberString) {
        String string = plainNumberString.toString();
        BigDecimal number = new BigDecimal(string);
        return RationalNumber.valueOf(number);
    }

    public static RationalNumber rational(final double d) {
        if (d < 0) {
            return RationalNumber.rational(-d, 1.0, 39).negate();
        } else {
            return RationalNumber.rational(d, 1.0, 39);
        }
    }

    public static RationalNumber valueOf(final Comparable<?> number) {

        if (number == null) {
            return ZERO;
        }

        if (number instanceof RationalNumber) {

            return (RationalNumber) number;

        } else {

            BigDecimal tmpBigD = TypeUtils.toBigDecimal(number);

            BigInteger retNumer;
            BigInteger retDenom;

            int scale = tmpBigD.scale();

            if (scale < 0) {

                retNumer = tmpBigD.unscaledValue().multiply(BigInteger.TEN.pow(-scale));
                retDenom = BigInteger.ONE;

            } else {

                retNumer = tmpBigD.unscaledValue();
                retDenom = BigInteger.TEN.pow(scale);

                BigInteger gcd = retNumer.gcd(retDenom);
                if (gcd.compareTo(BigInteger.ONE) > 0) {
                    retNumer = retNumer.divide(gcd);
                    retDenom = retDenom.divide(gcd);
                }
            }

            int bits = Math.max(retNumer.bitLength(), retDenom.bitLength());

            if (bits > MAX_BITS) {
                int shift = bits - MAX_BITS;
                retNumer = retNumer.shiftRight(shift);
                retDenom = retDenom.shiftRight(shift);
            }

            //                final BigDecimal recreated = BigFunction.DIVIDE.invoke(new BigDecimal(retNumer), new BigDecimal(retDenom));
            //                if (recreated.plus(MathContext.DECIMAL32).compareTo(tmpBigDecimal.plus(MathContext.DECIMAL32)) != 0) {
            //                    BasicLogger.debug("{} != {}", tmpBigDecimal, recreated);
            //                }

            return new RationalNumber(retNumer.longValue(), retDenom.longValue());
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
        long m = (e == 0) ? (bits & 0xfffffffffffffL) << 1 : (bits & 0xfffffffffffffL) | 0x10000000000000L;
        // Now we're looking for s * m * 2^{e - 1075}, 1075 being bias of 1023 plus 52 positions of binary fraction

        int exponent = e - 1075;

        if (exponent >= 0) {
            final long numerator = m << exponent;
            if ((numerator >> exponent) != m) {
                return s > 0 ? RationalNumber.POSITIVE_INFINITY : RationalNumber.NEGATIVE_INFINITY;
            }
            return new RationalNumber(s * numerator, 1L);
        }

        // Since denominator is a power of 2, GCD can only be power of two, so we simplify by dividing by 2 repeatedly
        while (((m & 1) == 0) && (exponent < 0)) {
            m >>= 1;
            exponent++;
        }
        // Avoiding the the denominator overflow
        if (-exponent >= MAX_BITS) {
            final BigInteger denom = BigInteger.ONE.shiftLeft(-exponent);
            final BigInteger maxlong = BigInteger.valueOf(Long.MAX_VALUE);
            final BigInteger factor = denom.divide(maxlong);
            if (factor.compareTo(maxlong) > 0) {
                return ZERO;
            }
            return new RationalNumber((s * m) / factor.longValueExact(), Long.MAX_VALUE);
        }
        return new RationalNumber(s * m, 1L << -exponent);
    }

    public static RationalNumber valueOf(final long value) {
        return RationalNumber.fromLong(value);
    }

    private static RationalNumber add(final RationalNumber arg1, final RationalNumber arg2) {

        final BigInteger numer1 = BigInteger.valueOf(arg1.getNumerator());
        final BigInteger denom1 = BigInteger.valueOf(arg1.getDenominator());

        final BigInteger numer2 = BigInteger.valueOf(arg2.getNumerator());
        final BigInteger denom2 = BigInteger.valueOf(arg2.getDenominator());

        final BigInteger retNumer = numer1.multiply(denom2).add(numer2.multiply(denom1));
        final BigInteger retDenom = denom1.multiply(denom2);

        return RationalNumber.of(retNumer, retDenom);
    }

    private static RationalNumber divide(final RationalNumber arg1, final RationalNumber arg2) {

        final BigInteger numer1 = BigInteger.valueOf(arg1.getNumerator());
        final BigInteger denom1 = BigInteger.valueOf(arg1.getDenominator());

        final BigInteger numer2 = BigInteger.valueOf(arg2.getNumerator());
        final BigInteger denom2 = BigInteger.valueOf(arg2.getDenominator());

        final BigInteger retNumer = numer1.multiply(denom2);
        final BigInteger retDenom = denom1.multiply(numer2);

        return RationalNumber.of(retNumer, retDenom);
    }

    private static RationalNumber fromLong(final long d) {
        return RationalNumber.of(d, 1L);
    }

    /**
     * Greatest Common Denominator
     * <p>
     * It uses Python-style gcd, with the sign of gcd equal to sign of b; that enables us to simplify
     * fractions in one step
     */
    private static long gcd(final long a, final long b) {

        long retVal = 1L;

        final long value1 = Math.abs(a);
        final long value2 = Math.abs(b);

        long tmpMax = Math.max(value1, value2);
        long tmpMin = Math.min(value1, value2);

        while (tmpMin != 0L) {
            retVal = tmpMin;
            tmpMin = tmpMax % tmpMin;
            tmpMax = retVal;
        }

        return b < 0 ? -retVal : retVal;
    }

    private static RationalNumber multiply(final RationalNumber arg1, final RationalNumber arg2) {

        final BigInteger numer1 = BigInteger.valueOf(arg1.getNumerator());
        final BigInteger denom1 = BigInteger.valueOf(arg1.getDenominator());

        final BigInteger numer2 = BigInteger.valueOf(arg2.getNumerator());
        final BigInteger denom2 = BigInteger.valueOf(arg2.getDenominator());

        final BigInteger retNumer = numer1.multiply(numer2);
        final BigInteger retDenom = denom1.multiply(denom2);

        return RationalNumber.of(retNumer, retDenom);
    }

    private static RationalNumber of(BigInteger numer, BigInteger denom) {

        final BigInteger gcd = numer.gcd(denom);
        if (gcd.compareTo(BigInteger.ONE) > 0) {
            numer = numer.divide(gcd);
            denom = denom.divide(gcd);
        }

        if (denom.signum() == -1) {
            numer = numer.negate();
            denom = denom.negate();
        }

        final int bits = Math.max(numer.bitLength(), denom.bitLength());
        if (bits > MAX_BITS) {
            final int shift = bits - MAX_BITS;
            numer = numer.shiftRight(shift);
            denom = denom.shiftRight(shift);
        }

        return RationalNumber.of(numer.longValueExact(), denom.longValueExact());
    }

    private static RationalNumber rational(final double d, final double error, final int depthLimit) {
        assert (d >= 0);
        if (d > Long.MAX_VALUE) {
            throw new ArithmeticException("Cannot fit a double into long!");
        }
        final double a = Math.floor(d);
        final RationalNumber approximation = RationalNumber.fromLong((long) a);
        final double remainder = d - a;
        final double newError = error * remainder;
        if ((newError > PrimitiveMath.MACHINE_EPSILON) && (depthLimit > 0)) {
            final RationalNumber rationalRemainder = RationalNumber.rational(1.0 / remainder, newError, depthLimit - 1).invert();
            return approximation.add(rationalRemainder);
        } else {
            return approximation;
        }
    }

    private static RationalNumber subtract(final RationalNumber arg1, final RationalNumber arg2) {

        final BigInteger numer1 = BigInteger.valueOf(arg1.getNumerator());
        final BigInteger denom1 = BigInteger.valueOf(arg1.getDenominator());

        final BigInteger numer2 = BigInteger.valueOf(arg2.getNumerator());
        final BigInteger denom2 = BigInteger.valueOf(arg2.getDenominator());

        final BigInteger retNumer = numer1.multiply(denom2).subtract(numer2.multiply(denom1));
        final BigInteger retDenom = denom1.multiply(denom2);

        return RationalNumber.of(retNumer, retDenom);
    }

    private static String toString(final RationalNumber aNmbr) {

        final StringBuilder retVal = new StringBuilder(LEFT);

        retVal.append(aNmbr.getNumerator());
        retVal.append(DIVIDE);
        retVal.append(aNmbr.getDenominator());

        return retVal.append(RIGHT).toString();
    }

    private transient BigDecimal myDecimal = null;
    private final long myDenominator;

    private final long myNumerator;

    public RationalNumber() {
        this(0L, 1L);
    }

    private RationalNumber(final long numerator, final long denominator) {
        assert denominator >= 0;
        myNumerator = numerator;
        myDenominator = denominator;

        if ((denominator == 0L) && (Math.abs(numerator) > 1L)) {
            throw new ArithmeticException("n / 0, where abs(n) > 1");
        }
    }

    @Override
    public RationalNumber add(final double arg) {
        return this.add(RationalNumber.valueOf(arg));
    }

    @Override
    public RationalNumber add(final RationalNumber arg) {
        if (this.isNaN() || arg.isNaN()) {
            return NaN;
        }

        if (this.isInfinite()) {
            if (!RationalNumber.isInfinite(arg) || (this.sign() == arg.sign())) {
                return this;
            } else {
                return NaN;
            }
        }

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

    @Override
    public int compareTo(final RationalNumber reference) {

        final long refNumer = reference.getNumerator();
        final long refDenom = reference.getDenominator();

        if (refDenom == 0L) {
            if (myDenominator == 0L) {
                return Long.compare(myNumerator, refNumer);
            } else if (refNumer > 0L) {
                return -1;
            } else {
                return 1;
            }
        } else if (myDenominator == 0L) {
            if (myNumerator > 0L) {
                return 1;
            } else {
                return -1;
            }
        } else {
            return this.toBigDecimal().compareTo(reference.toBigDecimal());
        }
    }

    @Override
    public RationalNumber conjugate() {
        return this;
    }

    @Override
    public RationalNumber divide(final double arg) {
        return this.divide(RationalNumber.valueOf(arg));
    }

    @Override
    public RationalNumber divide(final RationalNumber arg) {
        if (this.isNaN() || arg.isNaN()) {
            return NaN;
        }

        if (this.isInfinite()) {
            if (!RationalNumber.isInfinite(arg)) {
                return arg.sign() > 0 ? this : this.negate();
            } else {
                return NaN;
            }
        }

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
        if (myDenominator == 0L) {
            switch (Long.compare(myNumerator, 0L)) {
            case 1:
                return Double.POSITIVE_INFINITY;
            case -1:
                return Double.NEGATIVE_INFINITY;
            default:
                return Double.NaN;
            }
        }
        return this.toBigDecimal().doubleValue();
    }

    @Override
    public RationalNumber enforce(final NumberContext context) {
        return RationalNumber.valueOf(this.toBigDecimal(context.getMathContext()));
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof RationalNumber)) {
            return false;
        }
        RationalNumber other = (RationalNumber) obj;
        if ((myDenominator != other.myDenominator) || (myNumerator != other.myNumerator)) {
            return false;
        }
        return true;
    }

    @Override
    public float floatValue() {
        return this.toBigDecimal().floatValue();
    }

    @Override
    public RationalNumber get() {
        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + (int) (myDenominator ^ (myDenominator >>> 32));
        return (prime * result) + (int) (myNumerator ^ (myNumerator >>> 32));
    }

    @Override
    public int intValue() {
        return this.toBigDecimal().intValue();
    }

    @Override
    public RationalNumber invert() {
        return this.sign() >= 0 ? new RationalNumber(myDenominator, myNumerator) : new RationalNumber(-myDenominator, -myNumerator);
    }

    @Override
    public boolean isAbsolute() {
        return myNumerator >= 0L;
    }

    @Override
    public boolean isSmall(final double comparedTo) {
        return BigScalar.CONTEXT.isSmall(comparedTo, this.doubleValue());
    }

    @Override
    public long longValue() {
        return this.toBigDecimal().longValue();
    }

    @Override
    public RationalNumber multiply(final double arg) {
        return this.multiply(RationalNumber.valueOf(arg));
    }

    @Override
    public RationalNumber multiply(final RationalNumber arg) {
        if (this.isNaN() || arg.isNaN()) {
            return NaN;
        }

        if (this.isInfinite()) {
            return arg.sign() > 0 ? this : this.negate();
        }

        if (Math.max(this.size(), arg.size()) <= SAFE_LIMIT) {

            final long retNumer = myNumerator * arg.getNumerator();
            final long retDenom = myDenominator * arg.getDenominator();

            return RationalNumber.of(retNumer, retDenom);

        } else {

            return RationalNumber.multiply(this, arg);
        }
    }

    @Override
    public RationalNumber negate() {
        return new RationalNumber(-myNumerator, myDenominator);
    }

    @Override
    public double norm() {
        return PrimitiveMath.ABS.invoke(this.doubleValue());
    }

    @Override
    public RationalNumber power(final int power) {

        RationalNumber retVal = ONE;

        for (int p = 0; p < power; p++) {
            retVal = retVal.multiply(this);
        }

        return retVal;
    }

    @Override
    public RationalNumber signum() {
        if (!this.isInfinite() && RationalNumber.isSmall(PrimitiveMath.ONE, this)) {
            return ZERO;
        } else if (this.sign() == -1) {
            return NEG;
        } else {
            return ONE;
        }
    }

    @Override
    public RationalNumber subtract(final double arg) {
        return this.subtract(RationalNumber.valueOf(arg));
    }

    @Override
    public RationalNumber subtract(final RationalNumber arg) {
        if (this.isNaN() || arg.isNaN()) {
            return NaN;
        }

        if (this.isInfinite()) {
            if (!arg.isInfinite() || (this.sign() != arg.sign())) {
                return this;
            } else {
                return NaN;
            }
        }

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

    @Override
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

    @Override
    public String toString(final NumberContext context) {
        return RationalNumber.toString(this.enforce(context));
    }

    private boolean isInfinite() {
        return RationalNumber.isInfinite(this);
    }

    private boolean isNaN() {
        return RationalNumber.isNaN(this);
    }

    private int sign() {
        return Long.compare(myNumerator, 0L);
    }

    private long size() {
        return Math.max(Math.abs(myNumerator), myDenominator);
    }

    private BigDecimal toBigDecimal(final MathContext context) {
        if (myDenominator == 0L) {
            throw new NumberFormatException();
        } else {
            return new BigDecimal(myNumerator).divide(new BigDecimal(myDenominator), context);
        }
    }

    long getDenominator() {
        return myDenominator;
    }

    long getNumerator() {
        return myNumerator;
    }

}

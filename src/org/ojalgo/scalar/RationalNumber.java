/*
 * Copyright 1997-2015 Optimatika (www.optimatika.se)
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

    public static final RationalNumber NaN = new RationalNumber(BigInteger.ZERO, BigInteger.ZERO);
    public static final RationalNumber NEG = new RationalNumber(BigInteger.ONE.negate(), BigInteger.ONE);
    public static final RationalNumber NEGATIVE_INFINITY = new RationalNumber(BigInteger.ONE.negate(), BigInteger.ZERO);
    public static final RationalNumber ONE = new RationalNumber(BigInteger.ONE, BigInteger.ONE);
    public static final RationalNumber POSITIVE_INFINITY = new RationalNumber(BigInteger.ONE, BigInteger.ZERO);
    public static final RationalNumber TWO = new RationalNumber(BigInteger.ONE.add(BigInteger.ONE), BigInteger.ONE);
    public static final RationalNumber ZERO = new RationalNumber(BigInteger.ZERO, BigInteger.ONE);

    private static final String DIVIDE = " / ";
    private static final String LEFT = "(";
    private static final String RIGHT = ")";

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
        return ((value.getNumerator().signum() != 0) && (value.getDenominator().signum() == 0));
    }

    public static boolean isNaN(final RationalNumber value) {
        return ((value.getNumerator().signum() == 0) && (value.getDenominator().signum() == 0));
    }

    public static boolean isSmall(final double comparedTo, final RationalNumber value) {
        return value.isSmall(comparedTo);
    }

    public static RationalNumber of(final long numerator, final long denominator) {

        BigInteger tmpNumerator;
        BigInteger tmpDenominator;

        long tmpGCD = RationalNumber.gcd(numerator, denominator);

        if (denominator < 0L) {
            tmpGCD = -tmpGCD;
        }

        if (tmpGCD != 1L) {
            tmpNumerator = BigInteger.valueOf(numerator / tmpGCD);
            tmpDenominator = BigInteger.valueOf(denominator / tmpGCD);
        } else {
            tmpNumerator = BigInteger.valueOf(numerator);
            tmpDenominator = BigInteger.valueOf(denominator);
        }

        return new RationalNumber(tmpNumerator, tmpDenominator);
    }

    public static RationalNumber valueOf(final double value) {
        return RationalNumber.valueOf(BigDecimal.valueOf(value));
    }

    public static RationalNumber valueOf(final Number number) {

        if (number != null) {

            if (number instanceof RationalNumber) {

                return (RationalNumber) number;

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

                return new RationalNumber(tmpNumerator, tmpDenominator);
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

    private transient BigDecimal myDecimal = null;

    private final BigInteger myDenominator;
    private final BigInteger myNumerator;

    private RationalNumber() {
        this(BigInteger.ZERO, BigInteger.ONE);
    }

    private RationalNumber(final BigInteger numerator, final BigInteger denominator) {

        super();

        if (denominator.signum() >= 0) {
            myNumerator = numerator;
            myDenominator = denominator;
        } else {
            myNumerator = numerator.negate();
            myDenominator = denominator.negate();
        }
    }

    public RationalNumber add(final double arg) {
        return this.add(RationalNumber.valueOf(arg));
    }

    public RationalNumber add(final RationalNumber arg) {

        if (myDenominator.equals(arg.getDenominator())) {

            return new RationalNumber(myNumerator.add(arg.getNumerator()), myDenominator);

        } else {

            final BigInteger tmpNumer = myNumerator.multiply(arg.getDenominator()).add(arg.getNumerator().multiply(myDenominator));
            final BigInteger tmpDenom = myDenominator.multiply(arg.getDenominator());

            final BigInteger tmpGCD = tmpNumer.gcd(tmpDenom);

            if (tmpGCD.compareTo(BigInteger.ONE) == 1) {
                return new RationalNumber(tmpNumer.divide(tmpGCD), tmpDenom.divide(tmpGCD));
            } else {
                return new RationalNumber(tmpNumer, tmpDenom);
            }
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

        if (myNumerator.equals(arg.getNumerator())) {

            return new RationalNumber(arg.getDenominator(), myDenominator);

        } else if (myDenominator.equals(arg.getDenominator())) {

            return new RationalNumber(myNumerator, arg.getNumerator());

        } else {

            final BigInteger tmpNumer = myNumerator.multiply(arg.getDenominator());
            final BigInteger tmpDenom = myDenominator.multiply(arg.getNumerator());

            final BigInteger tmpGCD = tmpNumer.gcd(tmpDenom);

            if (tmpGCD.compareTo(BigInteger.ONE) == 1) {
                return new RationalNumber(tmpNumer.divide(tmpGCD), tmpDenom.divide(tmpGCD));
            } else {
                return new RationalNumber(tmpNumer, tmpDenom);
            }
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
        if (myDenominator == null) {
            if (other.myDenominator != null) {
                return false;
            }
        } else if (!myDenominator.equals(other.myDenominator)) {
            return false;
        }
        if (myNumerator == null) {
            if (other.myNumerator != null) {
                return false;
            }
        } else if (!myNumerator.equals(other.myNumerator)) {
            return false;
        }
        return true;
    }

    @Override
    public float floatValue() {
        return this.toBigDecimal().floatValue();
    }

    public RationalNumber getNumber() {
        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((myDenominator == null) ? 0 : myDenominator.hashCode());
        result = (prime * result) + ((myNumerator == null) ? 0 : myNumerator.hashCode());
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
        return (myNumerator.signum() >= 0) && (myDenominator.signum() > 0);
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

        if (myNumerator.equals(arg.getDenominator())) {

            return new RationalNumber(arg.getNumerator(), myDenominator);

        } else if (myDenominator.equals(arg.getNumerator())) {

            return new RationalNumber(myNumerator, arg.getDenominator());

        } else {

            final BigInteger tmpNumer = myNumerator.multiply(arg.getNumerator());
            final BigInteger tmpDenom = myDenominator.multiply(arg.getDenominator());

            final BigInteger tmpGCD = tmpNumer.gcd(tmpDenom);

            if (tmpGCD.compareTo(BigInteger.ONE) == 1) {
                return new RationalNumber(tmpNumer.divide(tmpGCD), tmpDenom.divide(tmpGCD));
            } else {
                return new RationalNumber(tmpNumer, tmpDenom);
            }
        }
    }

    public RationalNumber negate() {
        return new RationalNumber(myNumerator.negate(), myDenominator);
    }

    public double norm() {
        return Math.abs(this.doubleValue());
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

        if (myDenominator.equals(arg.getDenominator())) {

            return new RationalNumber(myNumerator.subtract(arg.getNumerator()), myDenominator);

        } else {

            final BigInteger tmpNumer = myNumerator.multiply(arg.getDenominator()).subtract(arg.getNumerator().multiply(myDenominator));
            final BigInteger tmpDenom = myDenominator.multiply(arg.getDenominator());

            final BigInteger tmpGCD = tmpNumer.gcd(tmpDenom);

            if (tmpGCD.compareTo(BigInteger.ONE) == 1) {
                return new RationalNumber(tmpNumer.divide(tmpGCD), tmpDenom.divide(tmpGCD));
            } else {
                return new RationalNumber(tmpNumer, tmpDenom);
            }
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
        return myNumerator.signum() * myDenominator.signum();
    }

    private BigDecimal toBigDecimal(final MathContext context) {
        return new BigDecimal(myNumerator).divide(new BigDecimal(myDenominator), context);
    }

    BigInteger getDenominator() {
        return myDenominator;
    }

    BigInteger getNumerator() {
        return myNumerator;
    }

}

/*
 * Copyright 1997-2019 Optimatika
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
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.type.TypeUtils;
import org.ojalgo.type.context.NumberContext;
import org.ojalgo.type.context.NumberContext.Enforceable;

final class OldRationalNumber extends Number implements Scalar<OldRationalNumber>, Enforceable<OldRationalNumber> {

    public static final Scalar.Factory<OldRationalNumber> FACTORY = new Scalar.Factory<OldRationalNumber>() {

        public OldRationalNumber cast(final double value) {
            return OldRationalNumber.valueOf(value);
        }

        public OldRationalNumber cast(final Number number) {
            return OldRationalNumber.valueOf(number);
        }

        public OldRationalNumber convert(final double value) {
            return OldRationalNumber.valueOf(value);
        }

        public OldRationalNumber convert(final Number number) {
            return OldRationalNumber.valueOf(number);
        }

        public OldRationalNumber one() {
            return ONE;
        }

        public OldRationalNumber zero() {
            return ZERO;
        }

    };

    public static final OldRationalNumber NaN = new OldRationalNumber(BigInteger.ZERO, BigInteger.ZERO);
    public static final OldRationalNumber NEG = new OldRationalNumber(BigInteger.ONE.negate(), BigInteger.ONE);
    public static final OldRationalNumber NEGATIVE_INFINITY = new OldRationalNumber(BigInteger.ONE.negate(), BigInteger.ZERO);
    public static final OldRationalNumber ONE = new OldRationalNumber(BigInteger.ONE, BigInteger.ONE);
    public static final OldRationalNumber POSITIVE_INFINITY = new OldRationalNumber(BigInteger.ONE, BigInteger.ZERO);
    public static final OldRationalNumber TWO = new OldRationalNumber(BigInteger.ONE.add(BigInteger.ONE), BigInteger.ONE);
    public static final OldRationalNumber ZERO = new OldRationalNumber(BigInteger.ZERO, BigInteger.ONE);

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

    public static boolean isAbsolute(final OldRationalNumber value) {
        return value.isAbsolute();
    }

    public static boolean isInfinite(final OldRationalNumber value) {
        return ((value.getNumerator().signum() != 0) && (value.getDenominator().signum() == 0));
    }

    public static boolean isNaN(final OldRationalNumber value) {
        return ((value.getNumerator().signum() == 0) && (value.getDenominator().signum() == 0));
    }

    public static boolean isSmall(final double comparedTo, final OldRationalNumber value) {
        return value.isSmall(comparedTo);
    }

    public static OldRationalNumber of(final long numerator, final long denominator) {

        BigInteger tmpNumerator;
        BigInteger tmpDenominator;

        long tmpGCD = OldRationalNumber.gcd(numerator, denominator);

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

        return new OldRationalNumber(tmpNumerator, tmpDenominator);
    }

    public static OldRationalNumber valueOf(final double value) {
        return OldRationalNumber.valueOf(BigDecimal.valueOf(value));
    }

    public static OldRationalNumber valueOf(final Number number) {

        if (number != null) {

            if (number instanceof OldRationalNumber) {

                return (OldRationalNumber) number;

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

                return new OldRationalNumber(tmpNumerator, tmpDenominator);
            }

        } else {

            return ZERO;
        }
    }

    private static String toString(final OldRationalNumber aNmbr) {

        final StringBuilder retVal = new StringBuilder(LEFT);

        retVal.append(aNmbr.getNumerator());
        retVal.append(DIVIDE);
        retVal.append(aNmbr.getDenominator());

        return retVal.append(RIGHT).toString();
    }

    private transient BigDecimal myDecimal = null;

    private final BigInteger myDenominator;
    private final BigInteger myNumerator;

    public OldRationalNumber() {
        this(BigInteger.ZERO, BigInteger.ONE);
    }

    private OldRationalNumber(BigInteger numerator, BigInteger denominator) {

        super();

        while (numerator.abs().max(denominator.abs()).bitCount() > 64) {
            numerator = numerator.divide(BigInteger.TEN);
            denominator = denominator.divide(BigInteger.TEN);
        }

        if (denominator.signum() >= 0) {
            myNumerator = numerator;
            myDenominator = denominator;
        } else {
            myNumerator = numerator.negate();
            myDenominator = denominator.negate();
        }
    }

    public OldRationalNumber add(final double arg) {
        return this.add(OldRationalNumber.valueOf(arg));
    }

    public OldRationalNumber add(final OldRationalNumber arg) {

        if (myDenominator.equals(arg.getDenominator())) {

            return new OldRationalNumber(myNumerator.add(arg.getNumerator()), myDenominator);

        } else {

            final BigInteger tmpNumer = myNumerator.multiply(arg.getDenominator()).add(arg.getNumerator().multiply(myDenominator));
            final BigInteger tmpDenom = myDenominator.multiply(arg.getDenominator());

            final BigInteger tmpGCD = tmpNumer.gcd(tmpDenom);

            if (tmpGCD.compareTo(BigInteger.ONE) == 1) {
                return new OldRationalNumber(tmpNumer.divide(tmpGCD), tmpDenom.divide(tmpGCD));
            } else {
                return new OldRationalNumber(tmpNumer, tmpDenom);
            }
        }
    }

    public int compareTo(final OldRationalNumber reference) {
        return this.toBigDecimal().compareTo(reference.toBigDecimal());
    }

    public OldRationalNumber conjugate() {
        return this;
    }

    public OldRationalNumber divide(final double arg) {
        return this.divide(OldRationalNumber.valueOf(arg));
    }

    public OldRationalNumber divide(final OldRationalNumber arg) {

        if (myNumerator.equals(arg.getNumerator())) {

            return new OldRationalNumber(arg.getDenominator(), myDenominator);

        } else if (myDenominator.equals(arg.getDenominator())) {

            return new OldRationalNumber(myNumerator, arg.getNumerator());

        } else {

            final BigInteger tmpNumer = myNumerator.multiply(arg.getDenominator());
            final BigInteger tmpDenom = myDenominator.multiply(arg.getNumerator());

            final BigInteger tmpGCD = tmpNumer.gcd(tmpDenom);

            if (tmpGCD.compareTo(BigInteger.ONE) == 1) {
                return new OldRationalNumber(tmpNumer.divide(tmpGCD), tmpDenom.divide(tmpGCD));
            } else {
                return new OldRationalNumber(tmpNumer, tmpDenom);
            }
        }
    }

    @Override
    public double doubleValue() {
        return this.toBigDecimal().doubleValue();
    }

    public OldRationalNumber enforce(final NumberContext context) {
        return OldRationalNumber.valueOf(this.toBigDecimal(context.getMathContext()));
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof OldRationalNumber)) {
            return false;
        }
        final OldRationalNumber other = (OldRationalNumber) obj;
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

    public OldRationalNumber get() {
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

    public OldRationalNumber invert() {
        return new OldRationalNumber(myDenominator, myNumerator);
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

    public OldRationalNumber multiply(final double arg) {
        return this.multiply(OldRationalNumber.valueOf(arg));
    }

    public OldRationalNumber multiply(final OldRationalNumber arg) {

        if (myNumerator.equals(arg.getDenominator())) {

            return new OldRationalNumber(arg.getNumerator(), myDenominator);

        } else if (myDenominator.equals(arg.getNumerator())) {

            return new OldRationalNumber(myNumerator, arg.getDenominator());

        } else {

            final BigInteger tmpNumer = myNumerator.multiply(arg.getNumerator());
            final BigInteger tmpDenom = myDenominator.multiply(arg.getDenominator());

            final BigInteger tmpGCD = tmpNumer.gcd(tmpDenom);

            if (tmpGCD.compareTo(BigInteger.ONE) == 1) {
                return new OldRationalNumber(tmpNumer.divide(tmpGCD), tmpDenom.divide(tmpGCD));
            } else {
                return new OldRationalNumber(tmpNumer, tmpDenom);
            }
        }
    }

    public OldRationalNumber negate() {
        return new OldRationalNumber(myNumerator.negate(), myDenominator);
    }

    public double norm() {
        return PrimitiveFunction.ABS.invoke(this.doubleValue());
    }

    public OldRationalNumber signum() {
        if (OldRationalNumber.isSmall(PrimitiveMath.ONE, this)) {
            return ZERO;
        } else if (this.sign() == -1) {
            return ONE.negate();
        } else {
            return ONE;
        }
    }

    public OldRationalNumber subtract(final double arg) {
        return this.subtract(OldRationalNumber.valueOf(arg));
    }

    public OldRationalNumber subtract(final OldRationalNumber arg) {

        if (myDenominator.equals(arg.getDenominator())) {

            return new OldRationalNumber(myNumerator.subtract(arg.getNumerator()), myDenominator);

        } else {

            final BigInteger tmpNumer = myNumerator.multiply(arg.getDenominator()).subtract(arg.getNumerator().multiply(myDenominator));
            final BigInteger tmpDenom = myDenominator.multiply(arg.getDenominator());

            final BigInteger tmpGCD = tmpNumer.gcd(tmpDenom);

            if (tmpGCD.compareTo(BigInteger.ONE) == 1) {
                return new OldRationalNumber(tmpNumer.divide(tmpGCD), tmpDenom.divide(tmpGCD));
            } else {
                return new OldRationalNumber(tmpNumer, tmpDenom);
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
        return OldRationalNumber.toString(this);
    }

    public String toString(final NumberContext context) {
        return OldRationalNumber.toString(this.enforce(context));
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

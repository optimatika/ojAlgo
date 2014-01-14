/*
 * Copyright 1997-2013 Optimatika (www.optimatika.se)
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

import org.ojalgo.type.TypeUtils;
import org.ojalgo.type.context.NumberContext;
import org.ojalgo.type.context.NumberContext.Enforceable;

public final class RationalNumber extends AbstractScalar<RationalNumber> implements Enforceable<RationalNumber> {

    public static final NumberContext PRECISION = NumberContext.getMath(MathContext.DECIMAL128).newScale(32);

    public static final Scalar.Factory<RationalNumber> FACTORY = new Scalar.Factory<RationalNumber>() {

        public RationalNumber cast(final double value) {
            return new RationalNumber(value);
        }

        public RationalNumber cast(final Number number) {
            return TypeUtils.toRationalNumber(number);
        }

        public RationalNumber convert(final double value) {
            return new RationalNumber(value);
        }

        public RationalNumber convert(final Number number) {
            return TypeUtils.toRationalNumber(number);
        }

        public RationalNumber one() {
            return ONE;
        }

        public RationalNumber zero() {
            return ZERO;
        }

    };

    public static final boolean IS_REAL = true;

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
    public static BigInteger gcd(final BigInteger aValue1, final BigInteger aValue2) {
        return aValue1.gcd(aValue2);
    }

    /**
     * Greatest Common Denominator
     */
    public static int gcd(int aValue1, int aValue2) {

        int retVal = 1;

        aValue1 = Math.abs(aValue1);
        aValue2 = Math.abs(aValue2);

        int tmpMax = Math.max(aValue1, aValue2);
        int tmpMin = Math.min(aValue1, aValue2);

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
    public static long gcd(long aValue1, long aValue2) {

        long retVal = 1L;

        aValue1 = Math.abs(aValue1);
        aValue2 = Math.abs(aValue2);

        long tmpMax = Math.max(aValue1, aValue2);
        long tmpMin = Math.min(aValue1, aValue2);

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
        return value.isInfinite();
    }

    public static boolean isNaN(final RationalNumber value) {
        return value.isNaN();
    }

    public static boolean isPositive(final RationalNumber value) {
        return value.isPositive();
    }

    public static boolean isZero(final RationalNumber value) {
        return value.isZero();
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

    public RationalNumber(final BigDecimal aNmbr) {

        super();

        final int tmpScale = aNmbr.scale();

        if (tmpScale < 0) {

            myNumerator = aNmbr.unscaledValue().multiply(BigInteger.TEN.pow(-tmpScale));
            myDenominator = BigInteger.ONE;

        } else {

            final BigInteger tmpNumer = aNmbr.unscaledValue();
            final BigInteger tmpDenom = BigInteger.TEN.pow(tmpScale);

            final BigInteger tmpGCD = tmpNumer.gcd(tmpDenom);

            if (tmpGCD.compareTo(BigInteger.ONE) == 1) {
                myNumerator = tmpNumer.divide(tmpGCD);
                myDenominator = tmpDenom.divide(tmpGCD);
            } else {
                myNumerator = tmpNumer;
                myDenominator = tmpDenom;
            }
        }
    }

    public RationalNumber(final double aNmbr) {
        this(new BigDecimal(aNmbr, MathContext.DECIMAL64));
    }

    public RationalNumber(final int aNumerator, final int aDenominator) {

        super();

        final long tmpGCD = RationalNumber.gcd(aNumerator, aDenominator);

        if (tmpGCD > 1) {
            myNumerator = BigInteger.valueOf(aNumerator / tmpGCD);
            myDenominator = BigInteger.valueOf(aDenominator / tmpGCD);
        } else {
            myNumerator = BigInteger.valueOf(aNumerator);
            myDenominator = BigInteger.valueOf(aDenominator);
        }
    }

    public RationalNumber(final long aNumerator, final long aDenominator) {

        super();

        final long tmpGCD = RationalNumber.gcd(aNumerator, aDenominator);

        if (tmpGCD > 1L) {
            myNumerator = BigInteger.valueOf(aNumerator / tmpGCD);
            myDenominator = BigInteger.valueOf(aDenominator / tmpGCD);
        } else {
            myNumerator = BigInteger.valueOf(aNumerator);
            myDenominator = BigInteger.valueOf(aDenominator);
        }
    }

    RationalNumber() {

        super();

        myNumerator = BigInteger.ZERO;
        myDenominator = BigInteger.ONE;
    }

    RationalNumber(final BigInteger aNumerator, final BigInteger aDenominator) {

        super();

        myNumerator = aNumerator;
        myDenominator = aDenominator;
    }

    public RationalNumber add(final double arg) {
        return this.add(new RationalNumber(arg));
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
        return this.divide(new RationalNumber(arg));
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
        return new RationalNumber(this.toBigDecimal(context.getMathContext()));
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

    public boolean isInfinite() {
        return ((myNumerator.signum() != 0) && (myDenominator.signum() == 0));
    }

    public boolean isNaN() {
        return ((myNumerator.signum() == 0) && (myDenominator.signum() == 0));
    }

    public boolean isPositive() {
        return ((myNumerator.signum() > 0) && (myDenominator.signum() > 0)) && !this.isZero();
    }

    public boolean isReal() {
        return IS_REAL;
    }

    public boolean isZero() {
        return ((myNumerator.signum() == 0) && (myDenominator.signum() > 0)) || BigScalar.isZero(this.toBigDecimal());
    }

    @Override
    public long longValue() {
        return this.toBigDecimal().longValue();
    }

    public RationalNumber multiply(final double arg) {
        return this.multiply(new RationalNumber(arg));
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
        if (this.isZero()) {
            return ZERO;
        } else if (this.sign() == -1) {
            return ONE.negate();
        } else {
            return ONE;
        }
    }

    public RationalNumber subtract(final double arg) {
        return this.subtract(new RationalNumber(arg));
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
            myDecimal = this.toBigDecimal(PRECISION.getMathContext());
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

    private BigInteger getDenominator() {
        return myDenominator;
    }

    private BigInteger getNumerator() {
        return myNumerator;
    }

    private int sign() {
        return myNumerator.signum() * myDenominator.signum();
    }

    private BigDecimal toBigDecimal(final MathContext context) {
        return new BigDecimal(myNumerator).divide(new BigDecimal(myDenominator), context);
    }

}

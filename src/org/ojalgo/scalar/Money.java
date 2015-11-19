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
import java.math.MathContext;

import org.ojalgo.type.context.NumberContext;
import org.ojalgo.type.context.NumberContext.Enforceable;

public final class Money extends Number implements Scalar<Money>, Enforceable<Money> {

    public static final Scalar.Factory<Money> FACTORY = new Scalar.Factory<Money>() {

        public Money cast(final double value) {
            return Money.valueOf(value);
        }

        public Money cast(final Number number) {
            return Money.valueOf(number);
        }

        public Money convert(final double value) {
            return Money.valueOf(value);
        }

        public Money convert(final Number number) {
            return Money.valueOf(number);
        }

        public Money one() {
            return ONE;
        }

        public Money zero() {
            return ZERO;
        }

    };

    private static final double DOUBLE_DENOMINATOR = 1000_000D;
    private static final long LONG_DENOMINATOR = 1000_000L;

    public static final Money NEG = new Money(-LONG_DENOMINATOR);
    public static final Money ONE = new Money(LONG_DENOMINATOR);
    public static final Money TWO = new Money(LONG_DENOMINATOR + LONG_DENOMINATOR);
    public static final Money ZERO = new Money();

    static final NumberContext CONTEXT = BigScalar.CONTEXT.newPrecision(19).newScale(4);

    public static boolean isAbsolute(final Money value) {
        return value.isAbsolute();
    }

    public static boolean isInfinite(final Money value) {
        return false;
    }

    public static boolean isNaN(final Money value) {
        return false;
    }

    public static boolean isSmall(final double comparedTo, final Money value) {
        return value.isSmall(comparedTo);
    }

    public static Money valueOf(final double value) {
        return new Money(value * DOUBLE_DENOMINATOR);
    }

    public static Money valueOf(final Number number) {

        if (number != null) {

            if (number instanceof Money) {

                return (Money) number;

            } else {

                return new Money(number.doubleValue() * Money.DOUBLE_DENOMINATOR);
            }

        } else {

            return ZERO;
        }
    }

    private static String toString(final Money scalar) {
        return Double.toString(scalar.doubleValue());
    }

    private transient BigDecimal myDecimal = null;

    private final long myNumerator;

    private Money() {

        super();

        myNumerator = 0L;
    }

    private Money(final double numerator) {

        super();

        myNumerator = Math.round(numerator);
    }

    private Money(final long numerator) {

        super();

        myNumerator = numerator;
    }

    public Money add(final double arg) {
        return new Money(myNumerator + (arg * DOUBLE_DENOMINATOR));
    }

    public Money add(final Money arg) {
        return new Money(myNumerator + arg.getNumerator());
    }

    public int compareTo(final Money reference) {
        return Long.compare(myNumerator, reference.getNumerator());
    }

    public Money conjugate() {
        return this;
    }

    public Money divide(final double arg) {
        return new Money(myNumerator / arg);
    }

    public Money divide(final Money arg) {
        return new Money((myNumerator * LONG_DENOMINATOR) / arg.getNumerator());
    }

    @Override
    public double doubleValue() {
        return myNumerator / DOUBLE_DENOMINATOR;
    }

    public Money enforce(final NumberContext context) {
        if (context.getScale() < 4) {
            return Money.valueOf(context.enforce(this.doubleValue()));
        } else {
            return this;
        }
    }

    @Override
    public float floatValue() {
        return (float) this.doubleValue();
    }

    public Money getNumber() {
        return this;
    }

    @Override
    public int intValue() {
        return (int) this.doubleValue();
    }

    public Money invert() {
        return new Money((LONG_DENOMINATOR * LONG_DENOMINATOR) / myNumerator);
    }

    public boolean isAbsolute() {
        return myNumerator >= 0L;
    }

    public boolean isSmall(final double comparedTo) {
        return CONTEXT.isSmall(comparedTo, this.doubleValue());
    }

    @Override
    public long longValue() {
        return this.toBigDecimal().longValue();
    }

    public Money multiply(final double arg) {
        return new Money(myNumerator * arg);
    }

    public Money multiply(final Money arg) {
        return new Money((myNumerator * arg.getNumerator()) / LONG_DENOMINATOR);
    }

    public Money negate() {
        return new Money(-myNumerator);
    }

    public double norm() {
        return Math.abs(this.doubleValue());
    }

    public Money signum() {
        if (myNumerator == 0L) {
            return ZERO;
        } else if (myNumerator < 0L) {
            return NEG;
        } else {
            return ONE;
        }
    }

    public Money subtract(final double arg) {
        return new Money(myNumerator - (arg * DOUBLE_DENOMINATOR));
    }

    public Money subtract(final Money arg) {
        return new Money(myNumerator - arg.getNumerator());
    }

    public BigDecimal toBigDecimal() {
        if (myDecimal == null) {
            myDecimal = this.toBigDecimal(CONTEXT.getMathContext());
        }
        return myDecimal;
    }

    @Override
    public String toString() {
        return Money.toString(this);
    }

    public String toString(final NumberContext context) {
        return Money.toString(this.enforce(context));
    }

    private BigDecimal toBigDecimal(final MathContext context) {
        return new BigDecimal(myNumerator).divide(new BigDecimal(LONG_DENOMINATOR), context);
    }

    long getNumerator() {
        return myNumerator;
    }

}

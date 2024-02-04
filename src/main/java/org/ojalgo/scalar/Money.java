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

import org.ojalgo.type.NumberDefinition;

/**
 * An example {@link ExactDecimal} implementation corresponding to the SQL data type MONEY or DECIMAL(19,4).
 *
 * @author apete
 */
public final class Money extends ExactDecimal<Money> {

    public static final Descriptor DESCRIPTOR = new Descriptor(4);

    public static final Scalar.Factory<Money> FACTORY = new ExactDecimal.Factory<>() {

        @Override
        public Money cast(final Comparable<?> number) {
            return Money.valueOf(number);
        }

        @Override
        public Money cast(final double value) {
            return Money.valueOf(value);
        }

        @Override
        public Money convert(final Comparable<?> number) {
            return Money.valueOf(number);
        }

        @Override
        public Money convert(final double value) {
            return Money.valueOf(value);
        }

        @Override
        public Descriptor descriptor() {
            return DESCRIPTOR;
        }

        @Override
        public Money one() {
            return ONE;
        }

        @Override
        public Money zero() {
            return ZERO;
        }

    };

    private static final double DOUBLE_DENOMINATOR = 10_000D;
    private static final long LONG_DENOMINATOR = 10_000L;

    public static final Money NEG = new Money(-LONG_DENOMINATOR);
    public static final Money ONE = new Money(LONG_DENOMINATOR);
    public static final Money TWO = new Money(LONG_DENOMINATOR + LONG_DENOMINATOR);
    public static final Money ZERO = new Money();

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

    public static Money valueOf(final Comparable<?> number) {

        if (number != null) {

            if (number instanceof Money) {

                return (Money) number;

            } else {

                return Money.valueOf(NumberDefinition.doubleValue(number));
            }

        } else {

            return ZERO;
        }
    }

    public static Money valueOf(final double value) {
        return new Money(Math.round(value * DOUBLE_DENOMINATOR));
    }

    public Money() {
        super(0L);
    }

    Money(final long numerator) {
        super(numerator);
    }

    @Override
    protected Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    protected Money wrap(final long numerator) {
        return new Money(numerator);
    }

}

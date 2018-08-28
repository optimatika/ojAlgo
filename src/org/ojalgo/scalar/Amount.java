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

/**
 * (currency) amount as in "amount = price * quatity"
 *
 * @author apete
 */
final class Amount extends ExactDecimal<Amount> {

    public static final Descriptor DESCRIPTOR = new Descriptor(2);

    public static final Scalar.Factory<Amount> FACTORY = new Scalar.Factory<Amount>() {

        public Amount cast(final double value) {
            return Amount.valueOf(value);
        }

        public Amount cast(final Number number) {
            return Amount.valueOf(number);
        }

        public Amount convert(final double value) {
            return Amount.valueOf(value);
        }

        public Amount convert(final Number number) {
            return Amount.valueOf(number);
        }

        public Amount one() {
            return ONE;
        }

        public Amount zero() {
            return ZERO;
        }

    };

    private static final double DOUBLE_DENOMINATOR = 100D;
    private static final long LONG_DENOMINATOR = 100L;

    public static final Amount NEG = new Amount(-LONG_DENOMINATOR);
    public static final Amount ONE = new Amount(LONG_DENOMINATOR);
    public static final Amount TWO = new Amount(LONG_DENOMINATOR + LONG_DENOMINATOR);
    public static final Amount ZERO = new Amount();

    public static Amount valueOf(final double value) {
        return new Amount(Math.round(value * DOUBLE_DENOMINATOR));
    }

    public static Amount valueOf(final Number number) {

        if (number != null) {

            if (number instanceof Amount) {

                return (Amount) number;

            } else {

                return Amount.valueOf(number.doubleValue());
            }

        } else {

            return ZERO;
        }
    }

    public Amount() {
        super(0L);
    }

    Amount(long numerator) {
        super(numerator);
    }

    public Quantity divide(Price price) {
        return new Quantity(Quantity.DESCRIPTOR.multiply(this, price));
    }

    public Price divide(Quantity quanntity) {
        return new Price(Price.DESCRIPTOR.multiply(this, quanntity));
    }

    public Amount multiply(Price rate) {
        return new Amount(Amount.DESCRIPTOR.multiply(this, rate));
    }

    @Override
    protected Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    protected Amount wrap(long numerator) {
        return new Amount(numerator);
    }

}

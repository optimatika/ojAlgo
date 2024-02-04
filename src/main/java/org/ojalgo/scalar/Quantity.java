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

/**
 * quantity as in "amount = price * quatity"
 *
 * @author apete
 */
public final class Quantity extends ExactDecimal<Quantity> {

    public static final Descriptor DESCRIPTOR = new Descriptor(6);

    public static final Scalar.Factory<Quantity> FACTORY = new ExactDecimal.Factory<>() {

        public Quantity cast(final double value) {
            return Quantity.valueOf(value);
        }

        public Quantity cast(final Comparable<?> number) {
            return Quantity.valueOf(number);
        }

        public Quantity convert(final double value) {
            return Quantity.valueOf(value);
        }

        public Quantity convert(final Comparable<?> number) {
            return Quantity.valueOf(number);
        }

        public Descriptor descriptor() {
            return DESCRIPTOR;
        }

        public Quantity one() {
            return ONE;
        }

        public Quantity zero() {
            return ZERO;
        }

    };

    private static final double DOUBLE_DENOMINATOR = 1_000_000D;
    private static final long LONG_DENOMINATOR = 1_000_000L;

    public static final Quantity NEG = new Quantity(-LONG_DENOMINATOR);
    public static final Quantity ONE = new Quantity(LONG_DENOMINATOR);
    public static final Quantity TWO = new Quantity(LONG_DENOMINATOR + LONG_DENOMINATOR);
    public static final Quantity ZERO = new Quantity();

    public static Quantity valueOf(final double value) {
        return new Quantity(Math.round(value * DOUBLE_DENOMINATOR));
    }

    public static Quantity valueOf(final Comparable<?> number) {

        if (number == null) {
            return ZERO;
        }

        if (number instanceof Quantity) {
            return (Quantity) number;
        }

        return Quantity.valueOf(Scalar.doubleValue(number));
    }

    public Quantity() {
        super(0L);
    }

    Quantity(final long numerator) {
        super(numerator);
    }

    public Amount multiply(final Price price) {
        return new Amount(Amount.DESCRIPTOR.multiply(this, price));
    }

    @Override
    protected Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    protected Quantity wrap(final long numerator) {
        return new Quantity(numerator);
    }

}

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
 * price or exchange rate as in "amount = price * quatity"
 *
 * @author apete
 */
final class ExactScale8 extends ExactDecimal<ExactScale8> {

    public static final Descriptor DESCRIPTOR = new Descriptor(8);

    public static final Scalar.Factory<ExactScale8> FACTORY = new Scalar.Factory<ExactScale8>() {

        public ExactScale8 cast(final double value) {
            return ExactScale8.valueOf(value);
        }

        public ExactScale8 cast(final Number number) {
            return ExactScale8.valueOf(number);
        }

        public ExactScale8 convert(final double value) {
            return ExactScale8.valueOf(value);
        }

        public ExactScale8 convert(final Number number) {
            return ExactScale8.valueOf(number);
        }

        public ExactScale8 one() {
            return ONE;
        }

        public ExactScale8 zero() {
            return ZERO;
        }

    };

    private static final double DOUBLE_DENOMINATOR = 100_000_000D;
    private static final long LONG_DENOMINATOR = 100_000_000L;

    public static final ExactScale8 NEG = new ExactScale8(-LONG_DENOMINATOR);
    public static final ExactScale8 ONE = new ExactScale8(LONG_DENOMINATOR);
    public static final ExactScale8 TWO = new ExactScale8(LONG_DENOMINATOR + LONG_DENOMINATOR);
    public static final ExactScale8 ZERO = new ExactScale8();

    public static ExactScale8 valueOf(final double value) {
        return new ExactScale8(Math.round(value * DOUBLE_DENOMINATOR));
    }

    public static ExactScale8 valueOf(final Number number) {

        if (number != null) {

            if (number instanceof ExactScale8) {

                return (ExactScale8) number;

            } else {

                return ExactScale8.valueOf(number.doubleValue());
            }

        } else {

            return ZERO;
        }
    }

    public ExactScale8() {
        super(0L);
    }

    private ExactScale8(long numerator) {
        super(numerator);
    }

    @Override
    protected ExactScale8 wrap(long numerator) {
        return new ExactScale8(numerator);
    }

    @Override
    protected Descriptor descriptor() {
        return DESCRIPTOR;
    }

}

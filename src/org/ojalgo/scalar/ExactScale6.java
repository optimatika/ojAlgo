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
 * quantity as in "amount = price * quatity"
 *
 * @author apete
 */
final class ExactScale6 extends ExactDecimal<ExactScale6> {

    public static final Descriptor DESCRIPTOR = new Descriptor(6);

    public static final Scalar.Factory<ExactScale6> FACTORY = new Scalar.Factory<ExactScale6>() {

        public ExactScale6 cast(final double value) {
            return ExactScale6.valueOf(value);
        }

        public ExactScale6 cast(final Number number) {
            return ExactScale6.valueOf(number);
        }

        public ExactScale6 convert(final double value) {
            return ExactScale6.valueOf(value);
        }

        public ExactScale6 convert(final Number number) {
            return ExactScale6.valueOf(number);
        }

        public ExactScale6 one() {
            return ONE;
        }

        public ExactScale6 zero() {
            return ZERO;
        }

    };

    private static final double DOUBLE_DENOMINATOR = 1_000_000D;
    private static final long LONG_DENOMINATOR = 1_000_000L;

    public static final ExactScale6 NEG = new ExactScale6(-LONG_DENOMINATOR);
    public static final ExactScale6 ONE = new ExactScale6(LONG_DENOMINATOR);
    public static final ExactScale6 TWO = new ExactScale6(LONG_DENOMINATOR + LONG_DENOMINATOR);
    public static final ExactScale6 ZERO = new ExactScale6();

    public static ExactScale6 valueOf(final double value) {
        return new ExactScale6(Math.round(value * DOUBLE_DENOMINATOR));
    }

    public static ExactScale6 valueOf(final Number number) {

        if (number != null) {

            if (number instanceof ExactScale6) {

                return (ExactScale6) number;

            } else {

                return ExactScale6.valueOf(number.doubleValue());
            }

        } else {

            return ZERO;
        }
    }

    public ExactScale6() {
        super(0L);
    }

    private ExactScale6(long numerator) {
        super(numerator);
    }

    @Override
    protected ExactScale6 wrap(long numerator) {
        return new ExactScale6(numerator);
    }

    @Override
    protected Descriptor descriptor() {
        return DESCRIPTOR;
    }

}

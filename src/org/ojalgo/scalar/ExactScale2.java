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
final class ExactScale2 extends ExactDecimal<ExactScale2> {

    public static final Descriptor DESCRIPTOR = new Descriptor(2);

    public static final Scalar.Factory<ExactScale2> FACTORY = new Scalar.Factory<ExactScale2>() {

        public ExactScale2 cast(final double value) {
            return ExactScale2.valueOf(value);
        }

        public ExactScale2 cast(final Number number) {
            return ExactScale2.valueOf(number);
        }

        public ExactScale2 convert(final double value) {
            return ExactScale2.valueOf(value);
        }

        public ExactScale2 convert(final Number number) {
            return ExactScale2.valueOf(number);
        }

        public ExactScale2 one() {
            return ONE;
        }

        public ExactScale2 zero() {
            return ZERO;
        }

    };

    private static final double DOUBLE_DENOMINATOR = 100D;
    private static final long LONG_DENOMINATOR = 100L;

    public static final ExactScale2 NEG = new ExactScale2(-LONG_DENOMINATOR);
    public static final ExactScale2 ONE = new ExactScale2(LONG_DENOMINATOR);
    public static final ExactScale2 TWO = new ExactScale2(LONG_DENOMINATOR + LONG_DENOMINATOR);
    public static final ExactScale2 ZERO = new ExactScale2();

    public static ExactScale2 valueOf(final double value) {
        return new ExactScale2(Math.round(value * DOUBLE_DENOMINATOR));
    }

    public static ExactScale2 valueOf(final Number number) {

        if (number != null) {

            if (number instanceof ExactScale2) {

                return (ExactScale2) number;

            } else {

                return ExactScale2.valueOf(number.doubleValue());
            }

        } else {

            return ZERO;
        }
    }

    public ExactScale2() {
        super(0L);
    }

    private ExactScale2(long numerator) {
        super(numerator);
    }

    @Override
    protected ExactScale2 wrap(long numerator) {
        return new ExactScale2(numerator);
    }

    @Override
    protected Descriptor descriptor() {
        return DESCRIPTOR;
    }

}

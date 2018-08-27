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
 * money (sql type money)
 *
 * @author apete
 */
final class ExactScale4 extends ExactDecimal<ExactScale4> {

    public static final Descriptor DESCRIPTOR = new Descriptor(4);

    public static final Scalar.Factory<ExactScale4> FACTORY = new Scalar.Factory<ExactScale4>() {

        public ExactScale4 cast(final double value) {
            return ExactScale4.valueOf(value);
        }

        public ExactScale4 cast(final Number number) {
            return ExactScale4.valueOf(number);
        }

        public ExactScale4 convert(final double value) {
            return ExactScale4.valueOf(value);
        }

        public ExactScale4 convert(final Number number) {
            return ExactScale4.valueOf(number);
        }

        public ExactScale4 one() {
            return ONE;
        }

        public ExactScale4 zero() {
            return ZERO;
        }

    };

    private static final double DOUBLE_DENOMINATOR = 10_000D;
    private static final long LONG_DENOMINATOR = 10_000L;

    public static final ExactScale4 NEG = new ExactScale4(-LONG_DENOMINATOR);
    public static final ExactScale4 ONE = new ExactScale4(LONG_DENOMINATOR);
    public static final ExactScale4 TWO = new ExactScale4(LONG_DENOMINATOR + LONG_DENOMINATOR);
    public static final ExactScale4 ZERO = new ExactScale4();

    public static boolean isAbsolute(final ExactScale4 value) {
        return value.isAbsolute();
    }

    public static boolean isInfinite(final ExactScale4 value) {
        return false;
    }

    public static boolean isNaN(final ExactScale4 value) {
        return false;
    }

    public static boolean isSmall(final double comparedTo, final ExactScale4 value) {
        return value.isSmall(comparedTo);
    }

    public static ExactScale4 valueOf(final double value) {
        return new ExactScale4(Math.round(value * DOUBLE_DENOMINATOR));
    }

    public static ExactScale4 valueOf(final Number number) {

        if (number != null) {

            if (number instanceof ExactScale4) {

                return (ExactScale4) number;

            } else {

                return ExactScale4.valueOf(number.doubleValue());
            }

        } else {

            return ZERO;
        }
    }

    public ExactScale4() {
        super(0L);
    }

    private ExactScale4(final long numerator) {
        super(numerator);
    }

    @Override
    protected ExactScale4 wrap(long numerator) {
        return new ExactScale4(numerator);
    }

    @Override
    protected Descriptor descriptor() {
        return DESCRIPTOR;
    }

}

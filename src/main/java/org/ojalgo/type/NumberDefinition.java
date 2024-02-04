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
package org.ojalgo.type;

import java.util.Objects;

/**
 * An interface defining a "Number". Had {@link java.lang.Number} been an interface...
 */
public interface NumberDefinition {

    static boolean booleanValue(final Comparable<?> number) {

        Objects.requireNonNull(number);

        return NumberDefinition.toBoolean(NumberDefinition.intValue(number));
    }

    static byte byteValue(final Comparable<?> number) {

        Objects.requireNonNull(number);

        if (number instanceof NumberDefinition) {
            return ((NumberDefinition) number).byteValue();
        } else if (number instanceof Number) {
            return ((Number) number).byteValue();
        } else {
            return Byte.MIN_VALUE;
        }
    }

    static double doubleValue(final Comparable<?> number) {

        Objects.requireNonNull(number);

        if (number instanceof NumberDefinition) {
            return ((NumberDefinition) number).doubleValue();
        } else if (number instanceof Number) {
            return ((Number) number).doubleValue();
        } else {
            return Double.NaN;
        }
    }

    static float floatValue(final Comparable<?> number) {

        Objects.requireNonNull(number);

        if (number instanceof NumberDefinition) {
            return ((NumberDefinition) number).floatValue();
        } else if (number instanceof Number) {
            return ((Number) number).floatValue();
        } else {
            return Float.NaN;
        }
    }

    static int intValue(final Comparable<?> number) {

        Objects.requireNonNull(number);

        if (number instanceof NumberDefinition) {
            return ((NumberDefinition) number).intValue();
        } else if (number instanceof Number) {
            return ((Number) number).intValue();
        } else {
            return Integer.MIN_VALUE;
        }
    }

    static long longValue(final Comparable<?> number) {

        Objects.requireNonNull(number);

        if (number instanceof NumberDefinition) {
            return ((NumberDefinition) number).longValue();
        } else if (number instanceof Number) {
            return ((Number) number).longValue();
        } else {
            return Long.MIN_VALUE;
        }
    }

    /**
     * Parses a {@link CharSequence} into a double. Only plain and simple numbers are supported – no
     * scientific notation, no hexadecimal notation, no octal notation, no binary notation, no fractions, no
     * exponents, no grouping, no leading or trailing whitespace, no commas, no underscores, no currency
     * symbols, no NaN, no positive or negative infinity or anything else fancy – just plain and simple
     * numbers.
     * <P>
     * If the {@link CharSequence} is null a {@link NullPointerException} is thrown.
     * <P>
     * If it's empty, length zero, zero is returned.
     * <P>
     * If it contains any characters other than '+', '-', '.', '0', '1', '2', '3', '4', '5', '6', '7', '8' or
     * '9' the return value is undefined.
     * <P>
     * If any of the characters '+', '-' or '.' appears more than once the return value is undefined.
     * <P>
     * If both '+' and '-' appear the return value is undefined.
     * <P>
     * If there are more than 16 digits after the decimal point the return value is undefined.
     * <p>
     * This method does not throw any exceptions (other than that eventual {@link NullPointerException}). Nor
     * does it do any error checking.
     * <P>
     * This method is more than 10 times faster than {@link Double#parseDouble(String)}. It takes a
     * {@link CharSequence}, rather than {@link String}, as input. Further more it does not create any garbage
     * (no intermediate objects created).
     */
    static double parseDouble(final CharSequence sequence) {

        long unscaled = 0L;
        boolean negative = false;
        int decimalplaces = Integer.MIN_VALUE;

        for (int i = 0, length = sequence.length(); i < length; i++) {

            char digit = sequence.charAt(i);

            switch (digit) {
            case '+':
                negative = false;
                break;
            case '-':
                negative = true;
                break;
            case '.':
                decimalplaces = 0;
                break;
            default:
                unscaled = 10L * unscaled + digit - '0';
                decimalplaces++;
                break;
            }
        }

        double retVal = unscaled;

        switch (decimalplaces) {
        case 16:
            retVal /= 10_000_000_000_000_000D;
            break;
        case 15:
            retVal /= 1_000_000_000_000_000D;
            break;
        case 14:
            retVal /= 100_000_000_000_000D;
            break;
        case 13:
            retVal /= 10_000_000_000_000D;
            break;
        case 12:
            retVal /= 1_000_000_000_000D;
            break;
        case 11:
            retVal /= 100_000_000_000D;
            break;
        case 10:
            retVal /= 10_000_000_000D;
            break;
        case 9:
            retVal /= 1_000_000_000D;
            break;
        case 8:
            retVal /= 100_000_000D;
            break;
        case 7:
            retVal /= 10_000_000D;
            break;
        case 6:
            retVal /= 1_000_000D;
            break;
        case 5:
            retVal /= 100_000D;
            break;
        case 4:
            retVal /= 10_000D;
            break;
        case 3:
            retVal /= 1_000D;
            break;
        case 2:
            retVal /= 100D;
            break;
        case 1:
            retVal /= 10D;
            break;
        default:
            break;
        }

        if (negative) {
            return -retVal;
        } else {
            return retVal;
        }
    }

    /**
     * Similar to {@link #parseDouble(CharSequence)} but for float, and thus limited to 7 digits after the
     * decimal point. It is almost 10 times faster than {@link Float#parseFloat(String)}.
     * 
     * @see #parseDouble(CharSequence)
     */
    static float parseFloat(final CharSequence sequence) {

        int unscaled = 0;
        boolean negative = false;
        int decimalplaces = Integer.MIN_VALUE;

        for (int i = 0, length = sequence.length(); i < length; i++) {

            char digit = sequence.charAt(i);

            switch (digit) {
            case '+':
                negative = false;
                break;
            case '-':
                negative = true;
                break;
            case '.':
                decimalplaces = 0;
                break;
            default:
                unscaled = 10 * unscaled + digit - '0';
                decimalplaces++;
                break;
            }
        }

        float retVal = unscaled;

        switch (decimalplaces) {
        case 7:
            retVal /= 10_000_000F;
            break;
        case 6:
            retVal /= 1_000_000F;
            break;
        case 5:
            retVal /= 100_000F;
            break;
        case 4:
            retVal /= 10_000F;
            break;
        case 3:
            retVal /= 1_000F;
            break;
        case 2:
            retVal /= 100F;
            break;
        case 1:
            retVal /= 10F;
            break;
        default:
            break;
        }

        if (negative) {
            return -retVal;
        } else {
            return retVal;
        }
    }

    /**
     * Similar to {@link #parseDouble(CharSequence)} and {@link #parseLong(CharSequence)} but for int.
     * 
     * @see #parseDouble(CharSequence)
     * @see #parseLong(CharSequence)
     */
    static int parseInt(final CharSequence sequence) {

        int retVal = 0;
        boolean negative = false;

        for (int i = 0, length = sequence.length(); i < length; i++) {

            char digit = sequence.charAt(i);

            switch (digit) {
            case '+':
                negative = false;
                break;
            case '-':
                negative = true;
                break;
            default:
                retVal = 10 * retVal + digit - '0';
                break;
            }
        }

        if (negative) {
            return -retVal;
        } else {
            return retVal;
        }
    }

    /**
     * Similar to {@link #parseDouble(CharSequence)} but for long. Performs roughly the same as
     * {@link Long#parseLong(String)}, but takes a {@link CharSequence}, rather than {@link String}, as input.
     * 
     * @see #parseDouble(CharSequence)
     */
    static long parseLong(final CharSequence sequence) {

        long retVal = 0L;
        boolean negative = false;

        for (int i = 0, length = sequence.length(); i < length; i++) {

            char digit = sequence.charAt(i);

            switch (digit) {
            case '+':
                negative = false;
                break;
            case '-':
                negative = true;
                break;
            default:
                retVal = 10L * retVal + digit - '0';
                break;
            }
        }

        if (negative) {
            return -retVal;
        } else {
            return retVal;
        }
    }

    /**
     * Similar to {@link #parseDouble(CharSequence)} and {@link #parseLong(CharSequence)} but for short.
     * 
     * @see #parseDouble(CharSequence)
     * @see #parseLong(CharSequence)
     */
    static short parseShort(final CharSequence sequence) {
        return (short) NumberDefinition.parseInt(sequence);
    }

    static short shortValue(final Comparable<?> number) {

        Objects.requireNonNull(number);

        if (number instanceof NumberDefinition) {
            return ((NumberDefinition) number).shortValue();
        } else if (number instanceof Number) {
            return ((Number) number).shortValue();
        } else {
            return Short.MIN_VALUE;
        }
    }

    static boolean toBoolean(final int value) {
        return value != 0;
    }

    static int toInt(final double value) {
        return Math.round((float) value);
    }

    static int toInt(final float value) {
        return Math.round(value);
    }

    static long toLong(final double value) {
        return Math.round(value);
    }

    default boolean booleanValue() {
        return NumberDefinition.toBoolean(this.intValue());
    }

    default byte byteValue() {
        return (byte) this.intValue();
    }

    double doubleValue();

    default float floatValue() {
        return (float) this.doubleValue();
    }

    default int intValue() {
        return NumberDefinition.toInt(this.floatValue());
    }

    default long longValue() {
        return NumberDefinition.toLong(this.doubleValue());
    }

    default short shortValue() {
        return (short) this.intValue();
    }

}

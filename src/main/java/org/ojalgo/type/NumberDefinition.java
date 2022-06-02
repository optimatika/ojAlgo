/*
 * Copyright 1997-2022 Optimatika
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

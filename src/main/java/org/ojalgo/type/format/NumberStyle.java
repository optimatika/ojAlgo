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
package org.ojalgo.type.format;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.function.LongFunction;

public enum NumberStyle {

    CURRENCY, GENERAL, INTEGER, PERCENT, SCIENTIFIC;

    static final class IntegerToUniformString {

        private static final String PADDING = "00000000000000000000";

        static int numberOfDigits(final long number) {

            if (number == 0) {
                return 1;
            }

            long nmbr = number;
            int count = 0;
            while (nmbr != 0) {
                nmbr /= 10;
                count++;
            }
            return count;
        }

        static String toStringWithFixedNumberOfDigits(final long value, final int numberOfDigits) {
            String retVal = Long.toString(value);
            retVal = PADDING + retVal;
            return retVal.substring(retVal.length() - numberOfDigits);
        }

        private final int myMaxNumberOfDigits;

        IntegerToUniformString(final long rangeSize) {
            super();
            myMaxNumberOfDigits = IntegerToUniformString.numberOfDigits(rangeSize - 1);
        }

        public String toString(final long value) {
            return IntegerToUniformString.toStringWithFixedNumberOfDigits(value, myMaxNumberOfDigits);
        }

    }

    /**
     * Similar to {@link NumberStyle#toUniformString(long, long)} but return a "formatter" with the specified
     * range size.
     *
     * @param rangeSize The max number of integers
     * @return An integer-to-string function
     */
    public static LongFunction<String> newUniformFormatter(final long rangeSize) {
        IntegerToUniformString formatter = new IntegerToUniformString(rangeSize);
        return formatter::toString;
    }

    /**
     * Creates String:s like "0067" and "0004" rather than "67" and "4" given an integer range (max number of
     * integers).
     */
    public static String toUniformString(final long value, final long rangeSize) {

        int maxNumberOfDigits = IntegerToUniformString.numberOfDigits(rangeSize - 1);

        return IntegerToUniformString.toStringWithFixedNumberOfDigits(value, maxNumberOfDigits);
    }

    public NumberFormat getFormat() {
        return this.getFormat(Locale.getDefault());
    }

    public NumberFormat getFormat(final Locale locale) {

        switch (this) {

        case CURRENCY:

            return NumberFormat.getCurrencyInstance(locale != null ? locale : Locale.getDefault());

        case INTEGER:

            return NumberFormat.getIntegerInstance(locale != null ? locale : Locale.getDefault());

        case PERCENT:

            return NumberFormat.getPercentInstance(locale != null ? locale : Locale.getDefault());

        default:

            return NumberFormat.getInstance(locale != null ? locale : Locale.getDefault());
        }
    }

}

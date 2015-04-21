/*
 * Copyright 1997-2015 Optimatika (www.optimatika.se)
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

public enum NumberStyle {

    CURRENCY, GENERAL, INTEGER, PERCENT, SCIENTIFIC;

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

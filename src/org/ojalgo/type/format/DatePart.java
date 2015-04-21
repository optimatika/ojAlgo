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

import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Locale;

public enum DatePart {

    DATE, DATETIME, TIME;

    public Format getFormat() {
        return this.getFormat(DateStyle.SHORT, Locale.getDefault());
    }

    public Format getFormat(final DateStyle style) {
        return this.getFormat(style, Locale.getDefault());
    }

    public Format getFormat(final DateStyle style, final Locale locale) {

        final DateStyle tmpStyle = style != null ? style : DateStyle.SHORT;
        final Locale tmpLocale = locale != null ? locale : Locale.getDefault();

        switch (tmpStyle) {

        case SQL:

            switch (this) {

            case DATE:

                return new SimpleDateFormat("yyyy-MM-dd");

            case TIME:

                return new SimpleDateFormat("HH:mm:ss");

            default:

                return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            }

        default:

            switch (this) {

            case DATE:

                return DateFormat.getDateInstance(tmpStyle.intValue(), tmpLocale);

            case TIME:

                return DateFormat.getTimeInstance(tmpStyle.intValue(), tmpLocale);

            default:

                return DateFormat.getDateTimeInstance(tmpStyle.intValue(), tmpStyle.intValue(), tmpLocale);
            }
        }
    }

    public Format getFormat(final Locale locale) {
        return this.getFormat(DateStyle.SHORT, locale);
    }

}

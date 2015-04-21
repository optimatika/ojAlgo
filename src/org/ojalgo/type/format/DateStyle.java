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
import java.util.Locale;

public enum DateStyle {

    FULL(DateFormat.FULL), LONG(DateFormat.LONG), MEDIUM(DateFormat.MEDIUM), SHORT(DateFormat.SHORT), SQL(Integer.MAX_VALUE);

    private int myIntValue;

    DateStyle(final int aStyleValue) {
        myIntValue = aStyleValue;
    }

    public Format getFormat() {
        return DatePart.DATETIME.getFormat(this, Locale.getDefault());
    }

    public Format getFormat(final DatePart aPart) {
        return aPart.getFormat(this, Locale.getDefault());
    }

    public Format getFormat(final DatePart aPart, final Locale aLocale) {
        return aPart.getFormat(this, aLocale);
    }

    public Format getFormat(final Locale aLocale) {
        return DatePart.DATETIME.getFormat(this, aLocale);
    }

    /**
     * @return {@linkplain DateFormat#FULL}, {@linkplain DateFormat#LONG}, {@linkplain DateFormat#MEDIUM} or
     *         {@linkplain DateFormat#SHORT}
     */
    public int intValue() {
        return myIntValue;
    }

}

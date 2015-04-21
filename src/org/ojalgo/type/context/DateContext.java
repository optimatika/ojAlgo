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
package org.ojalgo.type.context;

import java.text.Format;
import java.util.Date;
import java.util.Locale;

import org.ojalgo.ProgrammingError;
import org.ojalgo.type.CalendarDateUnit;
import org.ojalgo.type.StandardType;
import org.ojalgo.type.TypeUtils;
import org.ojalgo.type.format.DatePart;
import org.ojalgo.type.format.DateStyle;

/**
 * DateContext
 * 
 * @author apete
 */
public final class DateContext extends FormatContext<Date> {

    private static final DatePart DEFAULT_PART = DatePart.DATETIME;
    private static final DateStyle DEFAULT_STYLE = DateStyle.SHORT;

    public static Format toFormat(final DatePart part, final DateStyle style, final Locale locale) {
        return part != null ? part.getFormat(style, locale) : DEFAULT_PART.getFormat(style, locale);
    }

    private DatePart myPart = DEFAULT_PART;
    private DateStyle myStyle = DEFAULT_STYLE;

    public DateContext() {
        super(StandardType.SQL_DATETIME.getFormat());
    }

    public DateContext(final DatePart part) {
        this(part, DEFAULT_STYLE, Locale.getDefault());
    }

    public DateContext(final DatePart part, final DateStyle style, final Locale locale) {

        super(part != null ? part.getFormat(style, locale) : DEFAULT_PART.getFormat(style, locale));

        myPart = part != null ? part : DEFAULT_PART;
        myStyle = style != null ? style : DEFAULT_STYLE;
    }

    private DateContext(final Format format) {
        super(format);
        ProgrammingError.throwForIllegalInvocation();
    }

    @Override
    public Date enforce(final Date object) {

        switch (myPart) {

        case DATE:

            return TypeUtils.makeSqlDate(object.getTime());

        case TIME:

            return TypeUtils.makeSqlTime(object.getTime());

        default:

            return TypeUtils.makeSqlTimestamp(object.getTime());
        }
    }

    public DatePart getPart() {
        return myPart;
    }

    public DateStyle getStyle() {
        return myStyle;
    }

    public CalendarDateUnit getUnit() {

        switch (myPart) {

        case DATE:

            return CalendarDateUnit.DAY;

        default:

            return CalendarDateUnit.SECOND;
        }
    }

    public TypeContext<Date> newFormat(final DatePart part, final DateStyle style, final Locale locale) {

        final DatePart tmpPart = part != null ? part : this.getPart();
        final DateStyle tmpStyle = style != null ? style : this.getStyle();
        final Locale tmpLocale = locale != null ? locale : Locale.getDefault();

        return this.newFormat(DateContext.toFormat(tmpPart, tmpStyle, tmpLocale));
    }

    @Override
    protected void configureFormat(final Format format, final Object object) {
        // No need to do anything
    }

    @Override
    protected String handleFormatException(final Format format, final Object object) {
        return "";
    }

    @Override
    protected Date handleParseException(final Format format, final String string) {
        return new Date();
    }

}

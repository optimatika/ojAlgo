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
package org.ojalgo.type.context;

import java.text.Format;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Locale;

import org.ojalgo.type.CalendarDate;
import org.ojalgo.type.CalendarDateUnit;
import org.ojalgo.type.StandardType;
import org.ojalgo.type.format.DatePart;
import org.ojalgo.type.format.DateStyle;

/**
 * This {@link TypeContext} deals with the old {@link java.util.Date}. There is another implementation that
 * tries to deal with the newer date time API – {@link org.ojalgo.type.context.TemporalContext}.
 *
 * @author apete
 */
public final class DateContext extends FormatContext<Date> {

    private static final DatePart DEFAULT_PART = DatePart.DATETIME;
    private static final DateStyle DEFAULT_STYLE = DateStyle.SHORT;

    public static Format toFormat(final DatePart part, final DateStyle style, final Locale locale) {
        return part != null ? part.getFormat(style, locale) : DEFAULT_PART.getFormat(style, locale);
    }

    private final DatePart myPart;
    private final DateStyle myStyle;

    public DateContext() {

        super(StandardType.SQL_DATETIME.getFormat());

        myPart = DEFAULT_PART;
        myStyle = DEFAULT_STYLE;
    }

    public DateContext(final DatePart part) {
        this(part, DEFAULT_STYLE, Locale.getDefault());
    }

    public DateContext(final DatePart part, final DateStyle style, final Locale locale) {

        super(part != null ? part.getFormat(style, locale) : DEFAULT_PART.getFormat(style, locale));

        myPart = part != null ? part : DEFAULT_PART;
        myStyle = style != null ? style : DEFAULT_STYLE;
    }

    @Override
    public Date enforce(final Date object) {

        switch (myPart) {

        case DATE:

            LocalDate tmpDateOnly = new CalendarDate(object.getTime()).toLocalDate(ZoneOffset.UTC);

            return new Date(tmpDateOnly.getYear() - 1900, tmpDateOnly.getMonthValue() - 1, tmpDateOnly.getDayOfMonth());

        case TIME:

            LocalTime tmpTimeOnly = new CalendarDate(object.getTime()).toLocalTime(ZoneOffset.UTC);

            return new Date(0, 0, 1, tmpTimeOnly.getHour(), tmpTimeOnly.getMinute(), tmpTimeOnly.getSecond());

        default:

            long tmpDateAndTime = new CalendarDate(object.getTime()).millis;

            return new Date(tmpDateAndTime);
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

    public TypeContext<Date> withFormat(final DatePart part, final DateStyle style, final Locale locale) {

        DatePart tmpPart = part != null ? part : this.getPart();
        DateStyle tmpStyle = style != null ? style : this.getStyle();
        Locale tmpLocale = locale != null ? locale : Locale.getDefault();

        return this.withFormat(DateContext.toFormat(tmpPart, tmpStyle, tmpLocale));
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

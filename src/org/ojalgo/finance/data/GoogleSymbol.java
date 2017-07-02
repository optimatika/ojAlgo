/*
 * Copyright 1997-2017 Optimatika (www.optimatika.se)
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
package org.ojalgo.finance.data;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.ojalgo.RecoverableCondition;
import org.ojalgo.netio.ASCII;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.netio.ResourceLocator;
import org.ojalgo.type.CalendarDateUnit;
import org.ojalgo.type.context.GenericContext;

public class GoogleSymbol extends DataSource<GoogleSymbol.Data> {

    public static final class Data extends DatePrice {

        public double close;
        public double high;
        public double low;
        public double open;
        public double volume;

        protected Data(final Calendar calendar) {
            super(calendar);
        }

        protected Data(final Date date) {
            super(date);
        }

        protected Data(final long millis) {
            super(millis);
        }

        protected Data(final String sqlString) throws RecoverableCondition {
            super(sqlString);
        }

        @Override
        public double getPrice() {
            return close;
        }

    }

    private static final String CSV = "csv";
    private static final String DAILY = "daily";
    private static final GenericContext<Date> DATE_FORMAT = new GenericContext<>(new SimpleDateFormat("dd-MMM-yy", Locale.US));
    private static final String FINANCE_GOOGLE_COM = "finance.google.com";
    private static final String FINANCE_HISTORICAL = "/finance/historical";
    private static final String HISTPERIOD = "histperiod";
    private static final String JAN_2_1970 = "Jan+2,+1970";
    private static final String OUTPUT = "output";
    private static final String Q = "q";
    private static final String STARTDATE = "startdate";
    private static final String WEEKLY = "weekly";

    public GoogleSymbol(final String symbol) {
        this(symbol, CalendarDateUnit.DAY);
    }

    public GoogleSymbol(final String symbol, final CalendarDateUnit resolution) {

        super(FINANCE_GOOGLE_COM, symbol, resolution);

        final ResourceLocator tmpResourceLocator = this.getResourceLocator();

        tmpResourceLocator.cookies(null);

        tmpResourceLocator.path(FINANCE_HISTORICAL);
        tmpResourceLocator.parameter(Q, symbol);
        tmpResourceLocator.parameter(STARTDATE, JAN_2_1970);
        switch (resolution) {
        case WEEK:
            tmpResourceLocator.parameter(HISTPERIOD, WEEKLY);
            break;
        default:
            tmpResourceLocator.parameter(HISTPERIOD, DAILY);
            break;
        }
        tmpResourceLocator.parameter(OUTPUT, CSV);
    }

    @Override
    public GoogleSymbol.Data parse(final String line) throws RecoverableCondition {

        Data retVal = null;

        try {

            int tmpInclusiveBegin = 0;
            int tmpExclusiveEnd = line.indexOf(ASCII.COMMA, tmpInclusiveBegin);
            String tmpString = line.substring(tmpInclusiveBegin, tmpExclusiveEnd);
            final Calendar tmpCalendar = new GregorianCalendar();
            tmpCalendar.setTime(DATE_FORMAT.parse(tmpString));
            this.getResolution().round(tmpCalendar);
            retVal = new Data(tmpCalendar);

            tmpInclusiveBegin = tmpExclusiveEnd + 1;
            tmpExclusiveEnd = line.indexOf(ASCII.COMMA, tmpInclusiveBegin);
            tmpString = line.substring(tmpInclusiveBegin, tmpExclusiveEnd);
            try {
                retVal.open = Double.parseDouble(tmpString);
            } catch (final NumberFormatException ex) {
                retVal.open = Double.NaN;
            }

            tmpInclusiveBegin = tmpExclusiveEnd + 1;
            tmpExclusiveEnd = line.indexOf(ASCII.COMMA, tmpInclusiveBegin);
            tmpString = line.substring(tmpInclusiveBegin, tmpExclusiveEnd);
            try {
                retVal.high = Double.parseDouble(tmpString);
            } catch (final NumberFormatException ex) {
                retVal.high = Double.NaN;
            }

            tmpInclusiveBegin = tmpExclusiveEnd + 1;
            tmpExclusiveEnd = line.indexOf(ASCII.COMMA, tmpInclusiveBegin);
            tmpString = line.substring(tmpInclusiveBegin, tmpExclusiveEnd);
            try {
                retVal.low = Double.parseDouble(tmpString);
            } catch (final NumberFormatException ex) {
                retVal.low = Double.NaN;
            }

            tmpInclusiveBegin = tmpExclusiveEnd + 1;
            tmpExclusiveEnd = line.indexOf(ASCII.COMMA, tmpInclusiveBegin);
            tmpString = line.substring(tmpInclusiveBegin, tmpExclusiveEnd);
            try {
                retVal.close = Double.parseDouble(tmpString);
            } catch (final NumberFormatException ex) {
                retVal.close = Double.NaN;
            }

            tmpInclusiveBegin = tmpExclusiveEnd + 1;
            tmpString = line.substring(tmpInclusiveBegin);
            try {
                retVal.volume = Double.parseDouble(tmpString);
            } catch (final NumberFormatException ex) {
                retVal.volume = Double.NaN;
            }

        } catch (final Exception exception) {

            retVal = null;
        }

        return retVal;
    }

    @Override
    void handleException(final String symbol, final CalendarDateUnit resolution, final ResourceLocator locator, Exception exception) {
        BasicLogger.error("Problem downloading from Google!");
        BasicLogger.error("Symbol & Resolution: {} & {}", symbol, resolution);
        BasicLogger.error("Resource locator: {}", locator);
    }

}

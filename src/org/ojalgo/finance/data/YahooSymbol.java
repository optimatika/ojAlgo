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

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import org.ojalgo.RecoverableCondition;
import org.ojalgo.netio.ASCII;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.netio.ResourceLocator;
import org.ojalgo.type.CalendarDateUnit;

/**
 * YahooSymbol
 *
 * @author apete
 */
public class YahooSymbol extends DataSource<YahooSymbol.Data> {

    public static final class Data extends DatePrice {

        public double adjustedClose;
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
            return adjustedClose;
        }

    }

    private static final String HOST = "query1.finance.yahoo.com";
    private static final String PATH = "/v7/finance/download/";

    public YahooSymbol(final String symbol) {
        this(symbol, CalendarDateUnit.DAY);
    }

    public YahooSymbol(final String symbol, final CalendarDateUnit resolution) {

        super(symbol, resolution);

        // https://finance.yahoo.com/quote/AAPL

        final ResourceLocator tmpResourceLocator = new ResourceLocator();
        tmpResourceLocator.setScheme("https");
        tmpResourceLocator.setHost("finance.yahoo.com");
        tmpResourceLocator.setPath("/quote/" + symbol);

        final String tmpStr = "CrumbStore\":{\"crumb\":\"";

        String crumb = null;

        String tmpLine;
        int from, to;
        try (final BufferedReader tmpBufferedReader = new BufferedReader(tmpResourceLocator.getStreamReader())) {
            while ((crumb == null) && ((tmpLine = tmpBufferedReader.readLine()) != null)) {

                if ((from = tmpLine.indexOf(tmpStr)) >= 0) {
                    to = tmpLine.indexOf("\"}", from);
                    BasicLogger.debug(tmpLine);
                    crumb = tmpLine.substring(from + tmpStr.length(), to);
                }

            }
        } catch (final IOException exception) {
            exception.printStackTrace();
        }
        BasicLogger.debug(crumb);

        this.setHost(HOST);
        this.setPath(PATH + symbol);
        switch (resolution) {
        case MONTH:
            this.addQueryParameter("interval", 1 + "m");
            break;
        case WEEK:
            this.addQueryParameter("interval", 1 + "w");
            break;
        default:
            this.addQueryParameter("interval", 1 + "d");
            break;
        }
        this.addQueryParameter("events", "history");
        this.addQueryParameter("crumb", crumb);

    }

    @Override
    public YahooSymbol.Data parse(final String line) throws RecoverableCondition {

        Data retVal = null;

        try {

            int tmpInclusiveBegin = 0;
            int tmpExclusiveEnd = line.indexOf(ASCII.COMMA, tmpInclusiveBegin);
            String tmpString = line.substring(tmpInclusiveBegin, tmpExclusiveEnd);
            retVal = new Data(tmpString);

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
                retVal.open = Double.NaN;
            }

            tmpInclusiveBegin = tmpExclusiveEnd + 1;
            tmpExclusiveEnd = line.indexOf(ASCII.COMMA, tmpInclusiveBegin);
            tmpString = line.substring(tmpInclusiveBegin, tmpExclusiveEnd);
            try {
                retVal.low = Double.parseDouble(tmpString);
            } catch (final NumberFormatException ex) {
                retVal.open = Double.NaN;
            }

            tmpInclusiveBegin = tmpExclusiveEnd + 1;
            tmpExclusiveEnd = line.indexOf(ASCII.COMMA, tmpInclusiveBegin);
            tmpString = line.substring(tmpInclusiveBegin, tmpExclusiveEnd);
            try {
                retVal.close = Double.parseDouble(tmpString);
            } catch (final NumberFormatException ex) {
                retVal.open = Double.NaN;
            }

            tmpInclusiveBegin = tmpExclusiveEnd + 1;
            tmpExclusiveEnd = line.indexOf(ASCII.COMMA, tmpInclusiveBegin);
            tmpString = line.substring(tmpInclusiveBegin, tmpExclusiveEnd);
            try {
                retVal.volume = Double.parseDouble(tmpString);
            } catch (final NumberFormatException ex) {
                retVal.open = Double.NaN;
            }

            tmpInclusiveBegin = tmpExclusiveEnd + 1;
            tmpString = line.substring(tmpInclusiveBegin);
            try {
                retVal.adjustedClose = Double.parseDouble(tmpString);
            } catch (final NumberFormatException ex) {
                retVal.open = Double.NaN;
            }

        } catch (final Exception exception) {

            retVal = null;
        }

        return retVal;
    }

}

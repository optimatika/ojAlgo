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
package org.ojalgo.finance.data;

import java.util.Calendar;
import java.util.Date;

import org.ojalgo.netio.ASCII;
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

        protected Data(final Date aDate) {
            super(aDate);
        }

        protected Data(final long aDate) {
            super(aDate);
        }

        protected Data(final String sqlString) {
            super(sqlString);
        }

        protected Data(final Calendar aDate) {
            super(aDate);
        }

        @Override
        public double getPrice() {
            return adjustedClose;
        }

    }

    private static final String CSV = ".csv";
    private static final String D = "d";
    private static final String G = "g";
    private static final String ICHART_FINANCE_YAHOO_COM = "ichart.finance.yahoo.com";
    private static final String IGNORE = "ignore";
    private static final String M = "m";
    private static final String S = "s";
    private static final String TABLE_CSV = "/table.csv";
    private static final String W = "w";

    public YahooSymbol(final String aSymbol) {
        this(aSymbol, CalendarDateUnit.DAY);
    }

    public YahooSymbol(final String aSymbol, final CalendarDateUnit aResolution) {

        super(aSymbol, aResolution);

        this.setHost(ICHART_FINANCE_YAHOO_COM);
        this.setPath(TABLE_CSV);
        this.addQueryParameter(S, aSymbol);
        switch (aResolution) {
        case MONTH:
            this.addQueryParameter(G, M);
            break;
        case WEEK:
            this.addQueryParameter(G, W);
            break;
        default:
            this.addQueryParameter(G, D);
            break;
        }
        this.addQueryParameter(IGNORE, CSV);
    }

    @Override
    protected YahooSymbol.Data parse(final String aLine) {

        Data retVal = null;

        int tmpInclusiveBegin = 0;
        int tmpExclusiveEnd = aLine.indexOf(ASCII.COMMA, tmpInclusiveBegin);
        String tmpString = aLine.substring(tmpInclusiveBegin, tmpExclusiveEnd);
        retVal = new Data(tmpString);

        tmpInclusiveBegin = tmpExclusiveEnd + 1;
        tmpExclusiveEnd = aLine.indexOf(ASCII.COMMA, tmpInclusiveBegin);
        tmpString = aLine.substring(tmpInclusiveBegin, tmpExclusiveEnd);
        retVal.open = Double.parseDouble(tmpString);

        tmpInclusiveBegin = tmpExclusiveEnd + 1;
        tmpExclusiveEnd = aLine.indexOf(ASCII.COMMA, tmpInclusiveBegin);
        tmpString = aLine.substring(tmpInclusiveBegin, tmpExclusiveEnd);
        retVal.high = Double.parseDouble(tmpString);

        tmpInclusiveBegin = tmpExclusiveEnd + 1;
        tmpExclusiveEnd = aLine.indexOf(ASCII.COMMA, tmpInclusiveBegin);
        tmpString = aLine.substring(tmpInclusiveBegin, tmpExclusiveEnd);
        retVal.low = Double.parseDouble(tmpString);

        tmpInclusiveBegin = tmpExclusiveEnd + 1;
        tmpExclusiveEnd = aLine.indexOf(ASCII.COMMA, tmpInclusiveBegin);
        tmpString = aLine.substring(tmpInclusiveBegin, tmpExclusiveEnd);
        retVal.close = Double.parseDouble(tmpString);

        tmpInclusiveBegin = tmpExclusiveEnd + 1;
        tmpExclusiveEnd = aLine.indexOf(ASCII.COMMA, tmpInclusiveBegin);
        tmpString = aLine.substring(tmpInclusiveBegin, tmpExclusiveEnd);
        retVal.volume = Double.parseDouble(tmpString);

        tmpInclusiveBegin = tmpExclusiveEnd + 1;
        tmpString = aLine.substring(tmpInclusiveBegin);
        retVal.adjustedClose = Double.parseDouble(tmpString);

        return retVal;
    }

}

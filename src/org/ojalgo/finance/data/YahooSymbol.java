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
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.ojalgo.RecoverableCondition;
import org.ojalgo.netio.ASCII;
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

    private static final CookieManager COOKIE_MANAGER;
    private static final String MATCH_BEGIN = "CrumbStore\":{\"crumb\":\"";
    private static final String MATCH_END = "\"}";

    static {

        final CookieStore tmpCS = ResourceLocator.DEFAULT_COOKIE_MANAGER.getCookieStore();

        COOKIE_MANAGER = new CookieManager(new CookieStore() {

            public void add(final URI uri, final HttpCookie cookie) {
                if (cookie.getMaxAge() == 0L) {
                    cookie.setMaxAge(-1L);
                }
                tmpCS.add(uri, cookie);
            }

            public List<HttpCookie> get(final URI uri) {
                return tmpCS.get(uri);
            }

            public List<HttpCookie> getCookies() {
                return tmpCS.getCookies();
            }

            public List<URI> getURIs() {
                return tmpCS.getURIs();
            }

            public boolean remove(final URI uri, final HttpCookie cookie) {
                return tmpCS.remove(uri, cookie);
            }

            public boolean removeAll() {
                return tmpCS.removeAll();
            }

        }, CookiePolicy.ACCEPT_ALL);

    }

    public YahooSymbol(final String symbol) {
        this(symbol, CalendarDateUnit.DAY);
    }

    public YahooSymbol(final String symbol, final CalendarDateUnit resolution) {

        super("query1.finance.yahoo.com", symbol, resolution);

        // https://finance.yahoo.com/quote/AAPL

        final ResourceLocator tmpResourceLocator = new ResourceLocator("finance.yahoo.com");
        tmpResourceLocator.path("/quote/" + symbol);
        tmpResourceLocator.cookies(COOKIE_MANAGER);

        String crumb = null;
        String tmpLine;
        int begin, end;
        try (final BufferedReader tmpBufferedReader = new BufferedReader(tmpResourceLocator.getStreamReader())) {
            while ((crumb == null) && ((tmpLine = tmpBufferedReader.readLine()) != null)) {
                if ((begin = tmpLine.indexOf(MATCH_BEGIN)) >= 0) {
                    end = tmpLine.indexOf(MATCH_END, begin);
                    crumb = tmpLine.substring(begin + MATCH_BEGIN.length(), end);
                }
            }
        } catch (final IOException exception) {
            exception.printStackTrace();
        }

        this.getResourceLocator().path("/v7/finance/download/" + symbol);
        switch (resolution) {
        case MONTH:
            this.getResourceLocator().parameter("interval", 1 + "mo");
            break;
        case WEEK:
            this.getResourceLocator().parameter("interval", 1 + "wk");
            break;
        default:
            this.getResourceLocator().parameter("interval", 1 + "d");
            break;
        }
        this.getResourceLocator().parameter("events", "history");

        final Instant now = Instant.now();

        this.getResourceLocator().parameter("period1", Long.toString(now.minusSeconds(60L * 60L * 24 * 366L * 10L).getEpochSecond()));
        this.getResourceLocator().parameter("period2", Long.toString(now.getEpochSecond()));
        this.getResourceLocator().parameter("crumb", crumb);
        this.getResourceLocator().cookies(COOKIE_MANAGER);

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
            tmpExclusiveEnd = line.indexOf(ASCII.COMMA, tmpInclusiveBegin);
            tmpString = line.substring(tmpInclusiveBegin, tmpExclusiveEnd);
            try {
                retVal.adjustedClose = Double.parseDouble(tmpString);
            } catch (final NumberFormatException ex) {
                retVal.adjustedClose = Double.NaN;
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

}

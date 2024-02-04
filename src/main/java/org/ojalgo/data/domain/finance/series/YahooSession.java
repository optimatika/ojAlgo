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
package org.ojalgo.data.domain.finance.series;

import java.io.InputStream;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.ojalgo.netio.ServiceClient;
import org.ojalgo.netio.ServiceClient.Response;
import org.ojalgo.type.CalendarDateDuration;
import org.ojalgo.type.CalendarDateUnit;

/**
 * Fetch historical financial time series data from Yahoo Finance: https://finance.yahoo.com
 *
 * @see https://finance.yahoo.com
 * @author apete
 */
public final class YahooSession {

    public static final class Fetcher implements DataFetcher {

        private final CalendarDateUnit myResolution;
        private final ServiceClient.Session mySession;
        private final String mySymbol;

        Fetcher(final ServiceClient.Session session, final String symbol, final CalendarDateUnit resolution) {
            super();
            mySession = session;
            mySymbol = symbol;
            myResolution = resolution;
        }

        public InputStream getInputStream() {

            // https://query1.finance.yahoo.com/v7/finance/download/AAPL?period1=345427200&period2=1663718400&interval=1d&events=history&includeAdjustedClose=true

            ServiceClient.Request request = mySession.newRequest().host("query1.finance.yahoo.com").path("/v7/finance/download/" + mySymbol);

            Instant now = Instant.now();
            Instant past = now.minus(YahooSession.DURATION_30_YEARS.toDurationInMillis(), ChronoUnit.MILLIS);

            request.query("period1", Long.toString(past.getEpochSecond()));
            request.query("period2", Long.toString(now.getEpochSecond()));

            switch (myResolution) {
            case MONTH:
                request.query("interval", 1 + "mo");
                break;
            case WEEK:
                request.query("interval", 1 + "wk");
                break;
            default:
                request.query("interval", 1 + "d");
                break;
            }

            request.query("events", "history");
            request.query("includeAdjustedClose", "true");

            Response<InputStream> response = request.send(BodyHandlers.ofInputStream());

            if (response.isResponseOK()) {
                return response.getBody();
            } else {
                return InputStream.nullInputStream();
            }
        }

        public CalendarDateUnit getResolution() {
            return myResolution;
        }

        public String getSymbol() {
            return mySymbol;
        }

    }

    private static final CalendarDateDuration DURATION_30_YEARS = new CalendarDateDuration(30, CalendarDateUnit.YEAR);

    private final ServiceClient.Session mySession = ServiceClient.newSession();

    public YahooSession() {
        super();
    }

    public Fetcher newFetcher(final String symbol, final CalendarDateUnit resolution) {
        return new Fetcher(mySession, symbol, resolution);
    }

}

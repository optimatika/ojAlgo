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

import java.util.List;

import org.ojalgo.TestUtils;
import org.ojalgo.series.CalendarDateSeries;
import org.ojalgo.series.CoordinationSet;
import org.ojalgo.type.CalendarDateUnit;
import org.ojalgo.type.context.NumberContext;

/**
 * SymbolDataTest
 *
 * @author apete
 */
public class SymbolDataTest extends FinanceDataTests {

    public SymbolDataTest() {
        super();
    }

    public SymbolDataTest(final String arg0) {
        super(arg0);
    }

    public void testDailyComparison() {

        final String tmpYahooSymbol = "AAPL";
        final String tmpGoogleSymbol = "NASDAQ:AAPL";

        final YahooSymbol tmpYahooSource = new YahooSymbol(tmpYahooSymbol, CalendarDateUnit.DAY);
        final CalendarDateSeries<Double> tmpYahooPrices = tmpYahooSource.getPriceSeries();

        final GoogleSymbol tmpGoogleSource = new GoogleSymbol(tmpGoogleSymbol, CalendarDateUnit.DAY);
        final CalendarDateSeries<Double> tmpGooglePrices = tmpGoogleSource.getPriceSeries();

        CoordinationSet<Double> tmpCoordinator = new CoordinationSet<>();
        tmpCoordinator.put(tmpYahooPrices);
        tmpCoordinator.put(tmpGooglePrices);
        tmpCoordinator.complete();
        tmpCoordinator = tmpCoordinator.prune();
        final CalendarDateSeries<Double> tmpPrunedYahoo = tmpCoordinator.get(tmpYahooSymbol);
        final CalendarDateSeries<Double> tmpPrunedGoogle = tmpCoordinator.get(tmpGoogleSymbol);

        TestUtils.assertEquals("count", tmpPrunedYahoo.size(), tmpPrunedGoogle.size());
        TestUtils.assertEquals("Last Value", tmpPrunedYahoo.lastValue(), tmpPrunedGoogle.lastValue(), NumberContext.getGeneral(8, 14));
        // Doesn't work. Goggle and Yahoo seemsto have different data
        // JUnitUtils.assertEquals("First Value", tmpPrunedYahoo.firstValue(), tmpPrunedGoogle.firstValue(), PrimitiveMath.IS_ZERO);

        //        for (final CalendarDate tmpKey : tmpCoordinator.getAllContainedKeys()) {
        //            final double tmpYahooValue = tmpPrunedYahoo.get(tmpKey);
        //            final double tmpGoogleValue = tmpPrunedGoogle.get(tmpKey);
        //            if (tmpYahooValue != tmpGoogleValue) {
        //                BasicLogger.logDebug("Date={} Yahoo={} Google={}", tmpKey, tmpYahooValue, tmpGoogleValue);
        //            }
        //        }
    }

    public void testGoogleDaily() {

        final GoogleSymbol tmpGoogle = new GoogleSymbol("NASDAQ:AAPL", CalendarDateUnit.DAY);
        final List<? extends DatePrice> tmpRows = tmpGoogle.getHistoricalPrices();
        if (tmpRows.size() <= 1) {
            TestUtils.fail("No data!");
        }
    }

    public void testGoogleWeekly() {

        final GoogleSymbol tmpGoogle = new GoogleSymbol("NASDAQ:AAPL", CalendarDateUnit.WEEK);
        final List<? extends DatePrice> tmpRows = tmpGoogle.getHistoricalPrices();
        if (tmpRows.size() <= 1) {
            TestUtils.fail("No data!");
        }
    }

    public void testYahooDaily() {

        final YahooSymbol tmpYahoo = new YahooSymbol("AAPL", CalendarDateUnit.DAY);
        final List<? extends DatePrice> tmpRows = tmpYahoo.getHistoricalPrices();
        if (tmpRows.size() <= 1) {
            TestUtils.fail("No data!");
        }
    }

    public void testYahooMonthly() {

        final YahooSymbol tmpYahoo = new YahooSymbol("AAPL", CalendarDateUnit.MONTH);
        final List<? extends DatePrice> tmpRows = tmpYahoo.getHistoricalPrices();
        if (tmpRows.size() <= 1) {
            TestUtils.fail("No data!");
        }
    }

    public void testYahooWeekly() {

        final YahooSymbol tmpYahoo = new YahooSymbol("AAPL", CalendarDateUnit.WEEK);
        final List<? extends DatePrice> tmpRows = tmpYahoo.getHistoricalPrices();
        if (tmpRows.size() <= 1) {
            TestUtils.fail("No data!");
        }
    }
}

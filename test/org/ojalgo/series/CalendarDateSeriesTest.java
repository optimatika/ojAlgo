/* 
 * Copyright 1997-2016 Optimatika (www.optimatika.se)
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
package org.ojalgo.series;

import java.util.List;

import org.ojalgo.TestUtils;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.finance.data.DatePrice;
import org.ojalgo.finance.data.YahooSymbol;
import org.ojalgo.type.CalendarDateUnit;

/**
 * SymbolDataTest
 * 
 * @author apete
 */
public class CalendarDateSeriesTest extends SeriesTests {

    public CalendarDateSeriesTest() {
        super();
    }

    public CalendarDateSeriesTest(final String arg0) {
        super(arg0);
    }

    public void testResample() {

        final YahooSymbol tmpYahooSymbol = new YahooSymbol("AAPL", CalendarDateUnit.DAY);
        final List<YahooSymbol.Data> tmpHistoricalPrices = tmpYahooSymbol.getHistoricalPrices();
        final double tmpLastPrice = tmpHistoricalPrices.get(tmpHistoricalPrices.size() - 1).getPrice();

        final CalendarDateSeries<Double> tmpDaySeries = new CalendarDateSeries<Double>(CalendarDateUnit.DAY).name("Day");
        final CalendarDateSeries<Double> tmpWeekSeries = new CalendarDateSeries<Double>(CalendarDateUnit.WEEK).name("Week");
        final CalendarDateSeries<Double> tmpMonthSeries = new CalendarDateSeries<Double>(CalendarDateUnit.MONTH).name("Month");
        final CalendarDateSeries<Double> tmpQuarterSeries = new CalendarDateSeries<Double>(CalendarDateUnit.QUARTER).name("Quarter");
        final CalendarDateSeries<Double> tmpYearSeries = new CalendarDateSeries<Double>(CalendarDateUnit.YEAR).name("Year");
        final CalendarDateSeries<Double> tmpDecadeSeries = new CalendarDateSeries<Double>(CalendarDateUnit.DECADE).name("Decade");
        final CalendarDateSeries<Double> tmpCenturySeries = new CalendarDateSeries<Double>(CalendarDateUnit.CENTURY).name("Century");
        final CalendarDateSeries<Double> tmpMilleniumSeries = new CalendarDateSeries<Double>(CalendarDateUnit.MILLENIUM).name("MIllenium");

        for (final DatePrice tmpDatePrice : tmpHistoricalPrices) {
            tmpDaySeries.put(tmpDatePrice.key, tmpDatePrice.getPrice());
            tmpWeekSeries.put(tmpDatePrice.key, tmpDatePrice.getPrice());
            tmpMonthSeries.put(tmpDatePrice.key, tmpDatePrice.getPrice());
            tmpQuarterSeries.put(tmpDatePrice.key, tmpDatePrice.getPrice());
            tmpYearSeries.put(tmpDatePrice.key, tmpDatePrice.getPrice());
            tmpDecadeSeries.put(tmpDatePrice.key, tmpDatePrice.getPrice());
            tmpCenturySeries.put(tmpDatePrice.key, tmpDatePrice.getPrice());
            tmpMilleniumSeries.put(tmpDatePrice.key, tmpDatePrice.getPrice());
        }

        tmpDaySeries.complete();
        tmpWeekSeries.complete();
        tmpMonthSeries.complete();
        tmpQuarterSeries.complete();
        tmpYearSeries.complete();
        tmpDecadeSeries.complete();
        tmpCenturySeries.complete();
        tmpMilleniumSeries.complete();

        TestUtils.assertEquals("Day Series Last Value", tmpLastPrice, tmpDaySeries.lastValue(), PrimitiveMath.IS_ZERO);
        TestUtils.assertEquals("Week Series Last Value", tmpLastPrice, tmpWeekSeries.lastValue(), PrimitiveMath.IS_ZERO);
        TestUtils.assertEquals("Month Series Last Value", tmpLastPrice, tmpMonthSeries.lastValue(), PrimitiveMath.IS_ZERO);
        TestUtils.assertEquals("Quarter Series Last Value", tmpLastPrice, tmpQuarterSeries.lastValue(), PrimitiveMath.IS_ZERO);
        TestUtils.assertEquals("Year Series Last Value", tmpLastPrice, tmpYearSeries.lastValue(), PrimitiveMath.IS_ZERO);
        TestUtils.assertEquals("Decade Series Last Value", tmpLastPrice, tmpDecadeSeries.lastValue(), PrimitiveMath.IS_ZERO);
        TestUtils.assertEquals("Century Series Last Value", tmpLastPrice, tmpCenturySeries.lastValue(), PrimitiveMath.IS_ZERO);
        TestUtils.assertEquals("Millenium Series Last Value", tmpLastPrice, tmpMilleniumSeries.lastValue(), PrimitiveMath.IS_ZERO);

        TestUtils.assertEquals(tmpMilleniumSeries, tmpCenturySeries.resample(CalendarDateUnit.MILLENIUM));
        TestUtils.assertEquals(tmpMilleniumSeries, tmpDecadeSeries.resample(CalendarDateUnit.MILLENIUM));
        TestUtils.assertEquals(tmpMilleniumSeries, tmpYearSeries.resample(CalendarDateUnit.MILLENIUM));
        TestUtils.assertEquals(tmpMilleniumSeries, tmpQuarterSeries.resample(CalendarDateUnit.MILLENIUM));
        TestUtils.assertEquals(tmpMilleniumSeries, tmpMonthSeries.resample(CalendarDateUnit.MILLENIUM));
        TestUtils.assertEquals(tmpMilleniumSeries, tmpDaySeries.resample(CalendarDateUnit.MILLENIUM));

        TestUtils.assertEquals(tmpCenturySeries, tmpDecadeSeries.resample(CalendarDateUnit.CENTURY));
        TestUtils.assertEquals(tmpCenturySeries, tmpYearSeries.resample(CalendarDateUnit.CENTURY));
        TestUtils.assertEquals(tmpCenturySeries, tmpQuarterSeries.resample(CalendarDateUnit.CENTURY));
        TestUtils.assertEquals(tmpCenturySeries, tmpMonthSeries.resample(CalendarDateUnit.CENTURY));
        TestUtils.assertEquals(tmpCenturySeries, tmpDaySeries.resample(CalendarDateUnit.CENTURY));

        TestUtils.assertEquals(tmpDecadeSeries, tmpYearSeries.resample(CalendarDateUnit.DECADE));
        TestUtils.assertEquals(tmpDecadeSeries, tmpQuarterSeries.resample(CalendarDateUnit.DECADE));
        TestUtils.assertEquals(tmpDecadeSeries, tmpMonthSeries.resample(CalendarDateUnit.DECADE));
        TestUtils.assertEquals(tmpDecadeSeries, tmpDaySeries.resample(CalendarDateUnit.DECADE));

        TestUtils.assertEquals(tmpYearSeries, tmpQuarterSeries.resample(CalendarDateUnit.YEAR));
        TestUtils.assertEquals(tmpYearSeries, tmpMonthSeries.resample(CalendarDateUnit.YEAR));
        TestUtils.assertEquals(tmpYearSeries, tmpDaySeries.resample(CalendarDateUnit.YEAR));

        TestUtils.assertEquals(tmpQuarterSeries, tmpMonthSeries.resample(CalendarDateUnit.QUARTER));
        TestUtils.assertEquals(tmpQuarterSeries, tmpDaySeries.resample(CalendarDateUnit.QUARTER));

        TestUtils.assertEquals(tmpMonthSeries, tmpDaySeries.resample(CalendarDateUnit.MONTH));

        TestUtils.assertEquals(tmpWeekSeries, tmpDaySeries.resample(CalendarDateUnit.WEEK));
    }

}

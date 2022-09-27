/*
 * Copyright 1997-2022 Optimatika
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

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.random.Uniform;
import org.ojalgo.series.CalendarDateSeries;
import org.ojalgo.type.CalendarDateUnit;

/**
 * @author apete
 */
public class BasicSeriesTest extends FinanceSeriesTests {

    private static final File FILE = new File("./src/test/resources/org/ojalgo/data/domain/finance/series/Yahoo-AAPL-daily.csv");

    public BasicSeriesTest() {
        super();
    }

    @Test
    public void testDoubleKeys() {

        int dim = 1000;
        Uniform tmpUniform = new Uniform(0, Double.MAX_VALUE);

        double[] keys = new double[dim];
        long[] indices = new long[dim];

        for (int i = 0; i < dim; i++) {
            keys[i] = tmpUniform.doubleValue();
            indices[i] = Double.doubleToLongBits(keys[i]);
        }

        Arrays.sort(keys);
        Arrays.sort(indices);

        for (int i = 0; i < dim; i++) {
            TestUtils.assertEquals(keys[i], Double.longBitsToDouble(indices[i]));
        }
    }

    @Test
    public void testResample() {

        DataSource dataSource = DataSource.newFileReader(FILE, YahooParser.INSTANCE);
        List<DatePrice> historicalPrices = dataSource.getHistoricalPrices();
        double lastPrice = historicalPrices.get(historicalPrices.size() - 1).getPrice();

        CalendarDateSeries<Double> tmpDaySeries = dataSource.getCalendarDateSeries(CalendarDateUnit.DAY).name("Day");
        CalendarDateSeries<Double> tmpWeekSeries = dataSource.getCalendarDateSeries(CalendarDateUnit.WEEK).name("Week");
        CalendarDateSeries<Double> tmpMonthSeries = dataSource.getCalendarDateSeries(CalendarDateUnit.MONTH).name("Month");
        CalendarDateSeries<Double> tmpQuarterSeries = dataSource.getCalendarDateSeries(CalendarDateUnit.QUARTER).name("Quarter");
        CalendarDateSeries<Double> tmpYearSeries = dataSource.getCalendarDateSeries(CalendarDateUnit.YEAR).name("Year");
        CalendarDateSeries<Double> tmpDecadeSeries = dataSource.getCalendarDateSeries(CalendarDateUnit.DECADE).name("Decade");
        CalendarDateSeries<Double> tmpCenturySeries = dataSource.getCalendarDateSeries(CalendarDateUnit.CENTURY).name("Century");
        CalendarDateSeries<Double> tmpMilleniumSeries = dataSource.getCalendarDateSeries(CalendarDateUnit.MILLENIUM).name("MIllenium");

        tmpDaySeries.complete();
        tmpWeekSeries.complete();
        tmpMonthSeries.complete();
        tmpQuarterSeries.complete();
        tmpYearSeries.complete();
        tmpDecadeSeries.complete();
        tmpCenturySeries.complete();
        tmpMilleniumSeries.complete();

        double delta = 1E-14 / PrimitiveMath.THREE;

        TestUtils.assertEquals("Day Series Last Value", lastPrice, tmpDaySeries.lastValue(), delta);
        TestUtils.assertEquals("Week Series Last Value", lastPrice, tmpWeekSeries.lastValue(), delta);
        TestUtils.assertEquals("Month Series Last Value", lastPrice, tmpMonthSeries.lastValue(), delta);
        TestUtils.assertEquals("Quarter Series Last Value", lastPrice, tmpQuarterSeries.lastValue(), delta);
        TestUtils.assertEquals("Year Series Last Value", lastPrice, tmpYearSeries.lastValue(), delta);
        TestUtils.assertEquals("Decade Series Last Value", lastPrice, tmpDecadeSeries.lastValue(), delta);
        TestUtils.assertEquals("Century Series Last Value", lastPrice, tmpCenturySeries.lastValue(), delta);
        TestUtils.assertEquals("Millenium Series Last Value", lastPrice, tmpMilleniumSeries.lastValue(), delta);

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

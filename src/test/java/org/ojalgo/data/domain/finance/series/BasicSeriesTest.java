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

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.random.Uniform;
import org.ojalgo.series.CalendarDateSeries;
import org.ojalgo.type.CalendarDateUnit;

/**
 * @author apete
 */
@Disabled
public class BasicSeriesTest extends FinanceDataTests {

    public BasicSeriesTest() {
        super();
    }

    @Test
    public void testDoubleKeys() {

        final int dim = 1000;
        final Uniform tmpUniform = new Uniform(0, Double.MAX_VALUE);

        final double[] keys = new double[dim];
        final long[] indices = new long[dim];

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

        final DataSource dataSource = DataSource.newIEXTrading("AAPL");
        final List<DatePrice> historicalPrices = dataSource.getHistoricalPrices();
        final double lastPrice = historicalPrices.get(historicalPrices.size() - 1).getPrice();

        final CalendarDateSeries<Double> tmpDaySeries = dataSource.getCalendarDateSeries(CalendarDateUnit.DAY).name("Day");
        final CalendarDateSeries<Double> tmpWeekSeries = dataSource.getCalendarDateSeries(CalendarDateUnit.WEEK).name("Week");
        final CalendarDateSeries<Double> tmpMonthSeries = dataSource.getCalendarDateSeries(CalendarDateUnit.MONTH).name("Month");
        final CalendarDateSeries<Double> tmpQuarterSeries = dataSource.getCalendarDateSeries(CalendarDateUnit.QUARTER).name("Quarter");
        final CalendarDateSeries<Double> tmpYearSeries = dataSource.getCalendarDateSeries(CalendarDateUnit.YEAR).name("Year");
        final CalendarDateSeries<Double> tmpDecadeSeries = dataSource.getCalendarDateSeries(CalendarDateUnit.DECADE).name("Decade");
        final CalendarDateSeries<Double> tmpCenturySeries = dataSource.getCalendarDateSeries(CalendarDateUnit.CENTURY).name("Century");
        final CalendarDateSeries<Double> tmpMilleniumSeries = dataSource.getCalendarDateSeries(CalendarDateUnit.MILLENIUM).name("MIllenium");

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

        TestUtils.assertEquals(tmpMilleniumSeries.asPrimitive(), tmpCenturySeries.resample(CalendarDateUnit.MILLENIUM).asPrimitive());
        TestUtils.assertEquals(tmpMilleniumSeries.asPrimitive(), tmpDecadeSeries.resample(CalendarDateUnit.MILLENIUM).asPrimitive());
        TestUtils.assertEquals(tmpMilleniumSeries.asPrimitive(), tmpYearSeries.resample(CalendarDateUnit.MILLENIUM).asPrimitive());
        TestUtils.assertEquals(tmpMilleniumSeries.asPrimitive(), tmpQuarterSeries.resample(CalendarDateUnit.MILLENIUM).asPrimitive());
        TestUtils.assertEquals(tmpMilleniumSeries.asPrimitive(), tmpMonthSeries.resample(CalendarDateUnit.MILLENIUM).asPrimitive());
        TestUtils.assertEquals(tmpMilleniumSeries.asPrimitive(), tmpDaySeries.resample(CalendarDateUnit.MILLENIUM).asPrimitive());

        TestUtils.assertEquals(tmpCenturySeries.asPrimitive(), tmpDecadeSeries.resample(CalendarDateUnit.CENTURY).asPrimitive());
        TestUtils.assertEquals(tmpCenturySeries.asPrimitive(), tmpYearSeries.resample(CalendarDateUnit.CENTURY).asPrimitive());
        TestUtils.assertEquals(tmpCenturySeries.asPrimitive(), tmpQuarterSeries.resample(CalendarDateUnit.CENTURY).asPrimitive());
        TestUtils.assertEquals(tmpCenturySeries.asPrimitive(), tmpMonthSeries.resample(CalendarDateUnit.CENTURY).asPrimitive());
        TestUtils.assertEquals(tmpCenturySeries.asPrimitive(), tmpDaySeries.resample(CalendarDateUnit.CENTURY).asPrimitive());

        TestUtils.assertEquals(tmpDecadeSeries.asPrimitive(), tmpYearSeries.resample(CalendarDateUnit.DECADE).asPrimitive());
        TestUtils.assertEquals(tmpDecadeSeries.asPrimitive(), tmpQuarterSeries.resample(CalendarDateUnit.DECADE).asPrimitive());
        TestUtils.assertEquals(tmpDecadeSeries.asPrimitive(), tmpMonthSeries.resample(CalendarDateUnit.DECADE).asPrimitive());
        TestUtils.assertEquals(tmpDecadeSeries.asPrimitive(), tmpDaySeries.resample(CalendarDateUnit.DECADE).asPrimitive());

        TestUtils.assertEquals(tmpYearSeries.asPrimitive(), tmpQuarterSeries.resample(CalendarDateUnit.YEAR).asPrimitive());
        TestUtils.assertEquals(tmpYearSeries.asPrimitive(), tmpMonthSeries.resample(CalendarDateUnit.YEAR).asPrimitive());
        TestUtils.assertEquals(tmpYearSeries.asPrimitive(), tmpDaySeries.resample(CalendarDateUnit.YEAR).asPrimitive());

        TestUtils.assertEquals(tmpQuarterSeries.asPrimitive(), tmpMonthSeries.resample(CalendarDateUnit.QUARTER).asPrimitive());
        TestUtils.assertEquals(tmpQuarterSeries.asPrimitive(), tmpDaySeries.resample(CalendarDateUnit.QUARTER).asPrimitive());

        TestUtils.assertEquals(tmpMonthSeries.asPrimitive(), tmpDaySeries.resample(CalendarDateUnit.MONTH).asPrimitive());

        TestUtils.assertEquals(tmpWeekSeries.asPrimitive(), tmpDaySeries.resample(CalendarDateUnit.WEEK).asPrimitive());
    }

}

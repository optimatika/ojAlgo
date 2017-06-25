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
package org.ojalgo.series;

import java.util.Arrays;
import java.util.List;

import org.ojalgo.TestUtils;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.finance.data.DatePrice;
import org.ojalgo.finance.data.YahooSymbol;
import org.ojalgo.random.Uniform;
import org.ojalgo.series.BasicSeries.NaturallySequenced;
import org.ojalgo.type.CalendarDate;
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

    public void testResample() {

        final YahooSymbol tmpYahooSymbol = new YahooSymbol("AAPL", CalendarDateUnit.DAY);
        final List<YahooSymbol.Data> tmpHistoricalPrices = tmpYahooSymbol.getHistoricalPrices();
        final double tmpLastPrice = tmpHistoricalPrices.get(tmpHistoricalPrices.size() - 1).getPrice();

        final CalendarDateSeries<Double> tmpDaySeries = new CalendarDateSeries<Double>(CalendarDateUnit.DAY).name("Day");
        final NaturallySequenced<CalendarDate, Double> tmpWeekSeries = new CalendarDateSeries<Double>(CalendarDateUnit.WEEK).name("Week");
        final CalendarDateSeries<Double> tmpMonthSeries = new CalendarDateSeries<Double>(CalendarDateUnit.MONTH).name("Month");
        final CalendarDateSeries<Double> tmpQuarterSeries = new CalendarDateSeries<Double>(CalendarDateUnit.QUARTER).name("Quarter");
        final CalendarDateSeries<Double> tmpYearSeries = new CalendarDateSeries<Double>(CalendarDateUnit.YEAR).name("Year");
        final CalendarDateSeries<Double> tmpDecadeSeries = new CalendarDateSeries<Double>(CalendarDateUnit.DECADE).name("Decade");
        final CalendarDateSeries<Double> tmpCenturySeries = new CalendarDateSeries<Double>(CalendarDateUnit.CENTURY).name("Century");
        final NaturallySequenced<CalendarDate, Double> tmpMilleniumSeries = new CalendarDateSeries<Double>(CalendarDateUnit.MILLENIUM).name("MIllenium");

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

        TestUtils.assertEquals("Day Series Last Value", tmpLastPrice, tmpDaySeries.lastValue(), 1E-14 / PrimitiveMath.THREE);
        TestUtils.assertEquals("Week Series Last Value", tmpLastPrice, tmpWeekSeries.lastValue(), 1E-14 / PrimitiveMath.THREE);
        TestUtils.assertEquals("Month Series Last Value", tmpLastPrice, tmpMonthSeries.lastValue(), 1E-14 / PrimitiveMath.THREE);
        TestUtils.assertEquals("Quarter Series Last Value", tmpLastPrice, tmpQuarterSeries.lastValue(), 1E-14 / PrimitiveMath.THREE);
        TestUtils.assertEquals("Year Series Last Value", tmpLastPrice, tmpYearSeries.lastValue(), 1E-14 / PrimitiveMath.THREE);
        TestUtils.assertEquals("Decade Series Last Value", tmpLastPrice, tmpDecadeSeries.lastValue(), 1E-14 / PrimitiveMath.THREE);
        TestUtils.assertEquals("Century Series Last Value", tmpLastPrice, tmpCenturySeries.lastValue(), 1E-14 / PrimitiveMath.THREE);
        TestUtils.assertEquals("Millenium Series Last Value", tmpLastPrice, tmpMilleniumSeries.lastValue(), 1E-14 / PrimitiveMath.THREE);

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

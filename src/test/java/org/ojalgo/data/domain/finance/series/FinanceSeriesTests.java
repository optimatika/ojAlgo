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

import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ojalgo.TestUtils;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.random.LogNormal;
import org.ojalgo.random.SampleSet;
import org.ojalgo.random.process.GeometricBrownianMotion;
import org.ojalgo.series.CalendarDateSeries;
import org.ojalgo.series.primitive.PrimitiveSeries;
import org.ojalgo.type.CalendarDateUnit;

/**
 * @author apete
 */
public abstract class FinanceSeriesTests {

    static boolean DEBUG = false;

    static {

        if (DEBUG) {

            ConsoleHandler handler = new ConsoleHandler();
            handler.setLevel(Level.FINE);

            Logger logger = Logger.getLogger("");
            logger.setLevel(Level.FINE);
            logger.setUseParentHandlers(false);
            logger.addHandler(handler);
        }
    }

    static void assertAtLeastExpectedItems(final FinanceData dataSource, final int expected) {

        List<DatePrice> rows = dataSource.getHistoricalPrices();

        if (rows.size() <= 0) {
            TestUtils.fail("No data!");
        } else if (rows.size() < expected) {
            TestUtils.fail("Less data than usual! only got: " + rows.size());
        }
    }

    static void doTestDeriveDistribution(final DataSource dataSource) {

        CalendarDateSeries<Double> weekSeries = dataSource.getCalendarDateSeries(CalendarDateUnit.WEEK);
        CalendarDateSeries<Double> monthSeries = dataSource.getCalendarDateSeries(CalendarDateUnit.MONTH);

        PrimitiveSeries dataW = weekSeries.asPrimitive();
        PrimitiveSeries dataM = monthSeries.asPrimitive();

        SampleSet sampleSetW = SampleSet.wrap(dataW.log().differences());
        SampleSet sampleSetM = SampleSet.wrap(dataM.log().differences());

        double weeksPerYear = CalendarDateUnit.WEEK.convert(1.0, CalendarDateUnit.YEAR);
        double monthsPerYear = CalendarDateUnit.MONTH.convert(1.0, CalendarDateUnit.YEAR);

        GeometricBrownianMotion procW = GeometricBrownianMotion.estimate(dataW, 1. / weeksPerYear);
        procW.setValue(1.0);
        GeometricBrownianMotion procM = GeometricBrownianMotion.estimate(dataM, 1.0 / monthsPerYear);
        procM.setValue(1.0);

        double delta = 1E-13 / PrimitiveMath.THREE;

        LogNormal expDistr = new LogNormal(sampleSetW.getMean() * weeksPerYear, sampleSetW.getStandardDeviation() * PrimitiveMath.SQRT.invoke(weeksPerYear));
        LogNormal actDistr = procW.getDistribution(1.0);

        TestUtils.assertEquals("Weekly Expected", expDistr.getExpected(), actDistr.getExpected(), delta);
        TestUtils.assertEquals("Weekly Var", expDistr.getVariance(), actDistr.getVariance(), delta);
        TestUtils.assertEquals("Weekly StdDev", expDistr.getStandardDeviation(), actDistr.getStandardDeviation(), delta);

        expDistr = new LogNormal(sampleSetM.getMean() * monthsPerYear, sampleSetM.getStandardDeviation() * PrimitiveMath.SQRT.invoke(monthsPerYear));
        actDistr = procM.getDistribution(1.0);

        TestUtils.assertEquals("Monthly Expected", expDistr.getExpected(), actDistr.getExpected(), delta);
        TestUtils.assertEquals("Monthly Var", expDistr.getVariance(), actDistr.getVariance(), delta);
        TestUtils.assertEquals("Monthly StdDev", expDistr.getStandardDeviation(), actDistr.getStandardDeviation(), delta);
    }

    protected FinanceSeriesTests() {
        super();
    }

}

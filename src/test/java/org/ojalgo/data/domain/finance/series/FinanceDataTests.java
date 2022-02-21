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
public abstract class FinanceDataTests {

    static final boolean DEBUG = false;

    static {

        if (DEBUG) {

            final ConsoleHandler handler = new ConsoleHandler();
            handler.setLevel(Level.FINE);

            final Logger logger = Logger.getLogger("");
            logger.setLevel(Level.FINE);
            logger.setUseParentHandlers(false);
            logger.addHandler(handler);
        }
    }

    static void assertAtLeastExpectedItems(final FinanceData dataSource, final int expected) {

        final List<DatePrice> rows = dataSource.getHistoricalPrices();

        if (rows.size() <= 0) {
            TestUtils.fail("No data!");
        } else if (rows.size() < expected) {
            TestUtils.fail("Less data than usual! only got: " + rows.size());
        }
    }

    static void doTestDeriveDistribution(final DataSource dataSource) {

        final CalendarDateSeries<Double> yearSeries = dataSource.getCalendarDateSeries(CalendarDateUnit.YEAR);
        final CalendarDateSeries<Double> monthSeries = dataSource.getCalendarDateSeries(CalendarDateUnit.MONTH);

        final PrimitiveSeries dataY = yearSeries.asPrimitive();
        final PrimitiveSeries dataM = monthSeries.asPrimitive();

        final SampleSet sampleSetY = SampleSet.wrap(dataY.log().differences());
        final SampleSet sampleSetM = SampleSet.wrap(dataM.log().differences());

        final GeometricBrownianMotion procY = GeometricBrownianMotion.estimate(dataY, 1.0);
        procY.setValue(1.0);
        final GeometricBrownianMotion procM = GeometricBrownianMotion.estimate(dataM, 1.0 / 12.0);
        procM.setValue(1.0);

        double delta = 1E-14 / PrimitiveMath.THREE;

        LogNormal expDistr = new LogNormal(sampleSetY.getMean(), sampleSetY.getStandardDeviation());
        LogNormal actDistr = procY.getDistribution(1.0);

        TestUtils.assertEquals("Yearly Expected", expDistr.getExpected(), actDistr.getExpected(), delta);
        TestUtils.assertEquals("Yearly Var", expDistr.getVariance(), actDistr.getVariance(), delta);
        TestUtils.assertEquals("Yearly StdDev", expDistr.getStandardDeviation(), actDistr.getStandardDeviation(), delta);

        expDistr = new LogNormal(sampleSetM.getMean() * 12.0, sampleSetM.getStandardDeviation() * PrimitiveMath.SQRT.invoke(12.0));
        actDistr = procM.getDistribution(1.0);

        TestUtils.assertEquals("Monthly Expected", expDistr.getExpected(), actDistr.getExpected(), delta);
        TestUtils.assertEquals("Monthly Var", expDistr.getVariance(), actDistr.getVariance(), delta);
        TestUtils.assertEquals("Monthly StdDev", expDistr.getStandardDeviation(), actDistr.getStandardDeviation(), delta);
    }

    protected FinanceDataTests() {
        super();
    }

}

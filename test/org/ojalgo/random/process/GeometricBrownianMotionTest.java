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
package org.ojalgo.random.process;

import static org.ojalgo.constant.PrimitiveMath.*;

import java.util.List;

import org.ojalgo.TestUtils;
import org.ojalgo.array.PrimitiveArray;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.finance.data.DatePrice;
import org.ojalgo.finance.data.YahooSymbol;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.random.ContinuousDistribution;
import org.ojalgo.random.LogNormal;
import org.ojalgo.random.Normal;
import org.ojalgo.random.RandomUtils;
import org.ojalgo.random.SampleSet;
import org.ojalgo.series.CalendarDateSeries;
import org.ojalgo.series.primitive.DataSeries;
import org.ojalgo.type.CalendarDateUnit;
import org.ojalgo.type.context.NumberContext;

/**
 * RandomNumberTest
 *
 * @author apete
 */
public class GeometricBrownianMotionTest extends RandomProcessTests {

    public GeometricBrownianMotionTest() {
        super();
    }

    public GeometricBrownianMotionTest(final String someName) {
        super(someName);
    }

    public void testConfidenceIntervals() {

        for (int c = 1; c < 20; c++) {

            final double tmpConfidence = ONE - (c / (TEN + TEN));

            for (int m = 0; m <= 2; m++) {

                final double tmpExpected = PrimitiveFunction.POW.invoke(TEN, m);

                for (int s = -2; s <= 2; s++) {

                    final double tmpVariance = PrimitiveFunction.POW.invoke(TEN, s);

                    final GeometricBrownianMotion tmpProcess = GeometricBrownianMotion.make(tmpExpected, tmpVariance);

                    for (int t = 1; t < 10; t++) {

                        final ContinuousDistribution tmpDistribution = tmpProcess.getDistribution(t);
                        final double tmpOneSideRemainder = (ONE - tmpConfidence) / TWO;

                        final double tmpDistrUpper = tmpDistribution.getQuantile(ONE - tmpOneSideRemainder);
                        final double tmpDistrLower = tmpDistribution.getQuantile(tmpOneSideRemainder);

                        final double tmpProcUpper = tmpProcess.getUpperConfidenceQuantile(t, tmpConfidence);
                        final double tmpProcLower = tmpProcess.getLowerConfidenceQuantile(t, tmpConfidence);

                        //final double tmpIsZero = PrimitiveMath.IS_ZERO * 100000;
                        TestUtils.assertEquals(tmpDistrUpper, tmpProcUpper);
                        TestUtils.assertEquals(tmpDistrLower, tmpProcLower);

                        if (RandomProcessTests.DEBUG) {
                            BasicLogger.debug("Expected={}\tVariance={}\tConfidence={}\tHorizon={}", tmpExpected, tmpVariance, tmpConfidence, t);
                            BasicLogger.debug("\tDistrib\tLower={}\t<\tUpper={}", tmpDistrLower, tmpDistrUpper);
                            BasicLogger.debug("\tProcess\tLower={}\t<\tUpper={}", tmpProcLower, tmpProcUpper);
                        }
                    }
                }
            }
        }
    }

    public void testDistributionConsistency() {

        final double tmpError = new NumberContext(7, 9).epsilon();

        GeometricBrownianMotion tmpProcess;
        LogNormal tmpDistribution;

        for (int tmpCreateHorizon = 1; tmpCreateHorizon < 10; tmpCreateHorizon++) {

            for (double tmpExpected = 1.0; tmpExpected <= 2.0; tmpExpected += 0.1) {
                for (double tmpVariance = 0.0; tmpVariance <= 1.0; tmpVariance += 0.1) {

                    tmpProcess = GeometricBrownianMotion.make(tmpExpected, tmpVariance, tmpCreateHorizon);

                    TestUtils.assertEquals(tmpExpected, tmpProcess.getExpected(tmpCreateHorizon), tmpError);
                    TestUtils.assertEquals(tmpVariance, tmpProcess.getVariance(tmpCreateHorizon), tmpError);

                    tmpDistribution = tmpProcess.getDistribution(tmpCreateHorizon);

                    TestUtils.assertEquals(tmpExpected, tmpDistribution.getExpected(), tmpError);
                    TestUtils.assertEquals(tmpVariance, tmpDistribution.getVariance(), tmpError);

                    for (int tmpTestHorison = 0; tmpTestHorison < 10; tmpTestHorison++) {

                        tmpDistribution = tmpProcess.getDistribution(tmpTestHorison);

                        TestUtils.assertEquals(tmpDistribution.getExpected(), tmpProcess.getExpected(tmpTestHorison), tmpError);
                        TestUtils.assertEquals(tmpDistribution.getVariance(), tmpProcess.getVariance(tmpTestHorison), tmpError);
                        TestUtils.assertEquals(tmpDistribution.getStandardDeviation(), tmpProcess.getStandardDeviation(tmpTestHorison), tmpError);
                        TestUtils.assertEquals(tmpDistribution.getUpperConfidenceQuantile(0.95), tmpProcess.getUpperConfidenceQuantile(tmpTestHorison, 0.95),
                                tmpError);
                        TestUtils.assertEquals(tmpDistribution.getLowerConfidenceQuantile(0.95), tmpProcess.getLowerConfidenceQuantile(tmpTestHorison, 0.95),
                                tmpError);
                        TestUtils.assertEquals(tmpDistribution.getUpperConfidenceQuantile(0.05), tmpProcess.getUpperConfidenceQuantile(tmpTestHorison, 0.05),
                                tmpError);
                        TestUtils.assertEquals(tmpDistribution.getLowerConfidenceQuantile(0.05), tmpProcess.getLowerConfidenceQuantile(tmpTestHorison, 0.05),
                                tmpError);
                    }
                }
            }
        }
    }

    public void testLogNormal() {

        final int tmpPeriods = 10000;

        final double tmpFactoryExpected = 1.05;
        final double tmpFactoryStdDev = PrimitiveFunction.ABS.invoke(new Normal(0.0, (tmpFactoryExpected - ONE)).doubleValue());
        final Normal tmpFactoryDistr = new Normal(tmpFactoryExpected, tmpFactoryStdDev);
        TestUtils.assertEquals("Factory Expected", tmpFactoryExpected, tmpFactoryDistr.getExpected(), 1E-14 / PrimitiveMath.THREE);
        TestUtils.assertEquals("Factory Std Dev", tmpFactoryStdDev, tmpFactoryDistr.getStandardDeviation(), 1E-14 / PrimitiveMath.THREE);

        final PrimitiveArray tmpRawValues = PrimitiveArray.make(tmpPeriods + 1);
        tmpRawValues.data[0] = ONE;
        for (int t = 1; t < tmpRawValues.count(); t++) {
            tmpRawValues.data[t] = tmpRawValues.data[t - 1] * tmpFactoryDistr.doubleValue();
        }

        final PrimitiveArray tmpQuotient = PrimitiveArray.make(tmpPeriods);
        final PrimitiveArray tmpLogDiffs = PrimitiveArray.make(tmpPeriods);
        for (int t = 0; t < tmpPeriods; t++) {
            tmpQuotient.data[t] = tmpRawValues.data[t + 1] / tmpRawValues.data[t];
            tmpLogDiffs.data[t] = PrimitiveFunction.LOG.invoke(tmpRawValues.data[t + 1]) - PrimitiveFunction.LOG.invoke(tmpRawValues.data[t]);
        }
        final SampleSet tmpQuotientSet = SampleSet.wrap(tmpQuotient);
        final SampleSet tmpLogDiffsSet = SampleSet.wrap(tmpLogDiffs);

        final GeometricBrownianMotion tmpProcess = GeometricBrownianMotion.estimate(tmpRawValues, ONE);

        final Normal tmpQuotienDistr = new Normal(tmpQuotientSet.getMean(), tmpQuotientSet.getStandardDeviation());
        final LogNormal tmpLogDiffDistr = new LogNormal(tmpLogDiffsSet.getMean(), tmpLogDiffsSet.getStandardDeviation());
        final LogNormal tmpProcessDistr = tmpProcess.getDistribution(ONE);

        TestUtils.assertEquals("Expected", tmpLogDiffDistr.getExpected(), tmpProcessDistr.getExpected(), 1E-14 / PrimitiveMath.THREE);
        TestUtils.assertEquals("Geometric Mean", tmpLogDiffDistr.getGeometricMean(), tmpProcessDistr.getGeometricMean(), 1E-14 / PrimitiveMath.THREE);
        TestUtils.assertEquals("Geometric Standard Deviation", tmpLogDiffDistr.getGeometricStandardDeviation(), tmpProcessDistr.getGeometricStandardDeviation(),
                1E-14 / PrimitiveMath.THREE);
        TestUtils.assertEquals("Standard Deviation", tmpLogDiffDistr.getStandardDeviation(), tmpProcessDistr.getStandardDeviation(),
                1E-14 / PrimitiveMath.THREE);
        TestUtils.assertEquals("Variance", tmpLogDiffDistr.getVariance(), tmpProcessDistr.getVariance(), 1E-14 / PrimitiveMath.THREE);

        double tmpFactoryVal = tmpFactoryDistr.getExpected();
        double tmpQuotienVal = tmpQuotienDistr.getExpected();
        double tmpLogDiffVal = tmpLogDiffDistr.getExpected();
        double tmpProcessVal = tmpProcessDistr.getExpected();
        double tmpGeometrVal = tmpProcessDistr.getGeometricMean();
        if (RandomProcessTests.DEBUG) {
            this.logDebug("Expected", tmpFactoryVal, tmpQuotienVal, tmpLogDiffVal, tmpProcessVal, tmpGeometrVal);
        }
        final double tmpDeltaExpected = (1E-14 / THREE) * THOUSAND * THOUSAND * THOUSAND * HUNDRED;
        TestUtils.assertEquals(tmpQuotienVal, tmpLogDiffVal, tmpDeltaExpected);
        TestUtils.assertEquals(tmpQuotienVal, tmpProcessVal, tmpDeltaExpected);
        TestUtils.assertEquals(true, tmpGeometrVal <= tmpProcessVal);

        tmpFactoryVal = tmpFactoryDistr.getStandardDeviation();
        tmpQuotienVal = tmpQuotienDistr.getStandardDeviation();
        tmpLogDiffVal = tmpLogDiffDistr.getStandardDeviation();
        tmpProcessVal = tmpProcessDistr.getStandardDeviation();
        tmpGeometrVal = tmpProcessDistr.getGeometricStandardDeviation();
        if (RandomProcessTests.DEBUG) {
            this.logDebug("Std Dev", tmpFactoryVal, tmpQuotienVal, tmpLogDiffVal, tmpProcessVal, tmpGeometrVal);
        }
        final double tmpDeltaStdDev = (1E-14 / THREE) * THOUSAND * THOUSAND * THOUSAND * THOUSAND;
        TestUtils.assertEquals(tmpQuotienVal, tmpLogDiffVal, tmpDeltaStdDev);
        TestUtils.assertEquals(tmpQuotienVal, tmpProcessVal, tmpDeltaStdDev);

        tmpFactoryVal = tmpFactoryDistr.getVariance();
        tmpQuotienVal = tmpQuotienDistr.getVariance();
        tmpLogDiffVal = tmpLogDiffDistr.getVariance();
        tmpProcessVal = tmpProcessDistr.getVariance();
        tmpGeometrVal = tmpProcessDistr.getGeometricStandardDeviation() * tmpLogDiffDistr.getGeometricStandardDeviation();
        if (RandomProcessTests.DEBUG) {
            this.logDebug("Var", tmpFactoryVal, tmpQuotienVal, tmpLogDiffVal, tmpProcessVal, tmpGeometrVal);
        }
        final double tmpDeltaVar = (1E-14 / THREE) * THOUSAND * THOUSAND * THOUSAND * HUNDRED;
        TestUtils.assertEquals(tmpQuotienVal, tmpLogDiffVal, tmpDeltaVar);
        TestUtils.assertEquals(tmpQuotienVal, tmpProcessVal, tmpDeltaVar);

        tmpFactoryVal = tmpRawValues.data[tmpPeriods];
        tmpQuotienVal = PrimitiveFunction.POW.invoke(tmpQuotienDistr.getExpected(), tmpPeriods);
        tmpLogDiffVal = PrimitiveFunction.POW.invoke(tmpLogDiffDistr.getExpected(), tmpPeriods);
        tmpProcessVal = tmpProcess.getExpected(tmpPeriods);
        tmpGeometrVal = PrimitiveFunction.POW.invoke(tmpProcessDistr.getGeometricMean(), tmpPeriods);
        if (RandomProcessTests.DEBUG) {
            this.logDebug("Final Value", tmpFactoryVal, tmpQuotienVal, tmpLogDiffVal, tmpProcessVal, tmpGeometrVal);
        }
        final double tmpDeltaFinal = (1E-14 / THREE) * THOUSAND;
        TestUtils.assertEquals(ONE, tmpGeometrVal / tmpFactoryVal, tmpDeltaFinal);
    }

    public void testWikipediaCases() {

        new GeometricBrownianMotion(1.0, 0.2);
        final GeometricBrownianMotion tmpGreenProc = new GeometricBrownianMotion(0.5, 0.5);

        final GeometricBrownianMotion tmpProc = tmpGreenProc;

        for (int t = 1; t <= 100; t++) {

            final double tmpStep = t / HUNDRED;

            final LogNormal tmpDist = tmpProc.getDistribution(tmpStep);

            RandomUtils.erfi(0.95);

            final double tmpProcUpper = tmpProc.getUpperConfidenceQuantile(tmpStep, 0.95);
            final double tmpProcFact = PrimitiveFunction.SQRT.invoke(tmpProcUpper / tmpProc.getLowerConfidenceQuantile(tmpStep, 0.95));
            final double tmpDistUpper = tmpDist.getQuantile(0.975);
            final double tmpDistFact = PrimitiveFunction.SQRT.invoke(tmpDistUpper / tmpDist.getQuantile(0.025));

            if (RandomProcessTests.DEBUG) {
                BasicLogger.debug("Step={} ProcFact={} DistFact={} ProcMedian={} DistMedian={} expected={} median={}", tmpStep, tmpProcFact, tmpDistFact,
                        tmpProcUpper / tmpProcFact, tmpDistUpper / tmpDistFact, tmpDist.getExpected(), tmpDist.getGeometricMean());
            }
        }

    }

    public void testYahooWeeklyAAPL() {

        final YahooSymbol tmpYahoo = new YahooSymbol("AAPL", CalendarDateUnit.WEEK);

        final List<? extends DatePrice> tmpRows = tmpYahoo.getHistoricalPrices();

        final CalendarDateSeries<Double> tmpDaySeries = new CalendarDateSeries<>(CalendarDateUnit.DAY);
        tmpDaySeries.putAll(tmpRows);
        final CalendarDateSeries<Double> tmpYearSeries = tmpDaySeries.resample(CalendarDateUnit.YEAR);
        final CalendarDateSeries<Double> tmpMonthSeries = tmpDaySeries.resample(CalendarDateUnit.MONTH);

        final DataSeries tmpDataY = tmpYearSeries.getDataSeries();
        final DataSeries tmpDataM = tmpMonthSeries.getDataSeries();

        final SampleSet tmpSetY = SampleSet.wrap(tmpDataY.log().differences());
        final SampleSet tmpSetM = SampleSet.wrap(tmpDataM.log().differences());

        final GeometricBrownianMotion tmpProcY = GeometricBrownianMotion.estimate(tmpDataY, 1.0);
        tmpProcY.setValue(1.0);
        final GeometricBrownianMotion tmpProcM = GeometricBrownianMotion.estimate(tmpDataM, 1.0 / 12.0);
        tmpProcM.setValue(1.0);

        LogNormal tmpExpDistr = new LogNormal(tmpSetY.getMean(), tmpSetY.getStandardDeviation());
        LogNormal tmpActDistr = tmpProcY.getDistribution(1.0);

        TestUtils.assertEquals("Yearly Expected", tmpExpDistr.getExpected(), tmpActDistr.getExpected(), 1E-14 / PrimitiveMath.THREE);
        TestUtils.assertEquals("Yearly Var", tmpExpDistr.getVariance(), tmpActDistr.getVariance(), 1E-14 / PrimitiveMath.THREE);
        TestUtils.assertEquals("Yearly StdDev", tmpExpDistr.getStandardDeviation(), tmpActDistr.getStandardDeviation(), 1E-14 / PrimitiveMath.THREE);

        tmpExpDistr = new LogNormal(tmpSetM.getMean() * 12.0, tmpSetM.getStandardDeviation() * PrimitiveFunction.SQRT.invoke(12.0));
        tmpActDistr = tmpProcM.getDistribution(1.0);

        TestUtils.assertEquals("Monthly Expected", tmpExpDistr.getExpected(), tmpActDistr.getExpected(), 1E-14 / PrimitiveMath.THREE);
        TestUtils.assertEquals("Monthly Var", tmpExpDistr.getVariance(), tmpActDistr.getVariance(), 1E-14 / PrimitiveMath.THREE);
        TestUtils.assertEquals("Monthly StdDev", tmpExpDistr.getStandardDeviation(), tmpActDistr.getStandardDeviation(), 1E-14 / PrimitiveMath.THREE);
    }

    private void logDebug(final String aLabel, final double aGiven, final double aPlain, final double aLog, final double aProc, final double aGeom) {
        if (RandomProcessTests.DEBUG) {
            BasicLogger.debug("{}: given={}, plain={}, log={}, proc={}, geo={}", aLabel, aGiven, aPlain, aLog, aProc, aGeom);
        }
    }

}

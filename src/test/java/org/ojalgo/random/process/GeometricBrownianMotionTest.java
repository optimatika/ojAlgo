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
package org.ojalgo.random.process;

import static org.ojalgo.function.constant.PrimitiveMath.*;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.array.Primitive64Array;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.function.special.ErrorFunction;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.random.ContinuousDistribution;
import org.ojalgo.random.LogNormal;
import org.ojalgo.random.Normal;
import org.ojalgo.random.SampleSet;
import org.ojalgo.type.context.NumberContext;

/**
 * RandomNumberTest
 *
 * @author apete
 */
public class GeometricBrownianMotionTest extends RandomProcessTests {

    @Test
    public void testConfidenceIntervals() {

        for (int c = 1; c < 20; c++) {

            double tmpConfidence = ONE - c / (TEN + TEN);

            for (int m = 0; m <= 2; m++) {

                double tmpExpected = PrimitiveMath.POW.invoke(TEN, m);

                for (int s = -2; s <= 2; s++) {

                    double tmpVariance = PrimitiveMath.POW.invoke(TEN, s);

                    GeometricBrownianMotion tmpProcess = GeometricBrownianMotion.make(tmpExpected, tmpVariance);

                    for (int t = 1; t < 10; t++) {

                        ContinuousDistribution tmpDistribution = tmpProcess.getDistribution(t);
                        double tmpOneSideRemainder = (ONE - tmpConfidence) / TWO;

                        double tmpDistrUpper = tmpDistribution.getQuantile(ONE - tmpOneSideRemainder);
                        double tmpDistrLower = tmpDistribution.getQuantile(tmpOneSideRemainder);

                        double tmpProcUpper = tmpProcess.getUpperConfidenceQuantile(t, tmpConfidence);
                        double tmpProcLower = tmpProcess.getLowerConfidenceQuantile(t, tmpConfidence);

                        // double tmpIsZero = PrimitiveMath.IS_ZERO * 100000;
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

    @Test
    public void testDistributionConsistency() {

        double tmpError = NumberContext.of(7, 9).epsilon();

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

    @Test
    @Tag("unstable")
    public void testLogNormal() {

        int tmpPeriods = 10000;

        double tmpFactoryExpected = 1.05;
        double tmpFactoryStdDev = PrimitiveMath.ABS.invoke(new Normal(0.0, tmpFactoryExpected - ONE).doubleValue());
        Normal tmpFactoryDistr = new Normal(tmpFactoryExpected, tmpFactoryStdDev);
        TestUtils.assertEquals("Factory Expected", tmpFactoryExpected, tmpFactoryDistr.getExpected(), 1E-14 / PrimitiveMath.THREE);
        TestUtils.assertEquals("Factory Std Dev", tmpFactoryStdDev, tmpFactoryDistr.getStandardDeviation(), 1E-14 / PrimitiveMath.THREE);

        Primitive64Array tmpRawValues = Primitive64Array.make(tmpPeriods + 1);
        tmpRawValues.data[0] = ONE;
        for (int t = 1; t < tmpRawValues.count(); t++) {
            tmpRawValues.data[t] = tmpRawValues.data[t - 1] * tmpFactoryDistr.doubleValue();
        }

        Primitive64Array tmpQuotient = Primitive64Array.make(tmpPeriods);
        Primitive64Array tmpLogDiffs = Primitive64Array.make(tmpPeriods);
        for (int t = 0; t < tmpPeriods; t++) {
            tmpQuotient.data[t] = tmpRawValues.data[t + 1] / tmpRawValues.data[t];
            tmpLogDiffs.data[t] = PrimitiveMath.LOG.invoke(tmpRawValues.data[t + 1]) - PrimitiveMath.LOG.invoke(tmpRawValues.data[t]);
        }
        SampleSet tmpQuotientSet = SampleSet.wrap(tmpQuotient);
        SampleSet tmpLogDiffsSet = SampleSet.wrap(tmpLogDiffs);

        GeometricBrownianMotion tmpProcess = GeometricBrownianMotion.estimate(tmpRawValues, ONE);

        Normal tmpQuotienDistr = new Normal(tmpQuotientSet.getMean(), tmpQuotientSet.getStandardDeviation());
        LogNormal tmpLogDiffDistr = new LogNormal(tmpLogDiffsSet.getMean(), tmpLogDiffsSet.getStandardDeviation());
        LogNormal tmpProcessDistr = tmpProcess.getDistribution(ONE);

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
        double tmpDeltaExpected = 1E-14 / THREE * THOUSAND * THOUSAND * THOUSAND * HUNDRED;
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
        double tmpDeltaStdDev = 1E-14 / THREE * THOUSAND * THOUSAND * THOUSAND * THOUSAND;
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
        double tmpDeltaVar = 1E-14 / THREE * THOUSAND * THOUSAND * THOUSAND * HUNDRED;
        TestUtils.assertEquals(tmpQuotienVal, tmpLogDiffVal, tmpDeltaVar);
        TestUtils.assertEquals(tmpQuotienVal, tmpProcessVal, tmpDeltaVar);

        tmpFactoryVal = tmpRawValues.data[tmpPeriods];
        tmpQuotienVal = PrimitiveMath.POW.invoke(tmpQuotienDistr.getExpected(), tmpPeriods);
        tmpLogDiffVal = PrimitiveMath.POW.invoke(tmpLogDiffDistr.getExpected(), tmpPeriods);
        tmpProcessVal = tmpProcess.getExpected(tmpPeriods);
        tmpGeometrVal = PrimitiveMath.POW.invoke(tmpProcessDistr.getGeometricMean(), tmpPeriods);
        if (RandomProcessTests.DEBUG) {
            this.logDebug("Final Value", tmpFactoryVal, tmpQuotienVal, tmpLogDiffVal, tmpProcessVal, tmpGeometrVal);
        }
        double tmpDeltaFinal = 1E-14 / THREE * THOUSAND;
        TestUtils.assertEquals(ONE, tmpGeometrVal / tmpFactoryVal, tmpDeltaFinal);
    }

    @Test
    public void testWikipediaCases() {

        new GeometricBrownianMotion(1.0, 0.2);
        GeometricBrownianMotion tmpGreenProc = new GeometricBrownianMotion(0.5, 0.5);

        GeometricBrownianMotion tmpProc = tmpGreenProc;

        for (int t = 1; t <= 100; t++) {

            double tmpStep = t / HUNDRED;

            LogNormal tmpDist = tmpProc.getDistribution(tmpStep);

            ErrorFunction.erfi(0.95);

            double tmpProcUpper = tmpProc.getUpperConfidenceQuantile(tmpStep, 0.95);
            double tmpProcFact = PrimitiveMath.SQRT.invoke(tmpProcUpper / tmpProc.getLowerConfidenceQuantile(tmpStep, 0.95));
            double tmpDistUpper = tmpDist.getQuantile(0.975);
            double tmpDistFact = PrimitiveMath.SQRT.invoke(tmpDistUpper / tmpDist.getQuantile(0.025));

            if (RandomProcessTests.DEBUG) {
                BasicLogger.debug("Step={} ProcFact={} DistFact={} ProcMedian={} DistMedian={} expected={} median={}", tmpStep, tmpProcFact, tmpDistFact,
                        tmpProcUpper / tmpProcFact, tmpDistUpper / tmpDistFact, tmpDist.getExpected(), tmpDist.getGeometricMean());
            }
        }

    }

    private void logDebug(final String aLabel, final double aGiven, final double aPlain, final double aLog, final double aProc, final double aGeom) {
        if (RandomProcessTests.DEBUG) {
            BasicLogger.debug("{}: given={}, plain={}, log={}, proc={}, geo={}", aLabel, aGiven, aPlain, aLog, aProc, aGeom);
        }
    }

}

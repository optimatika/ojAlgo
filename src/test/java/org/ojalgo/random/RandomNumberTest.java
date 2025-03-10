/*
 * Copyright 1997-2025 Optimatika
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
package org.ojalgo.random;

import static org.ojalgo.function.constant.PrimitiveMath.*;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.array.ArrayR064;
import org.ojalgo.function.special.ErrorFunction;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.scalar.PrimitiveScalar;
import org.ojalgo.series.CalendarDateSeries;
import org.ojalgo.structure.Access1D;
import org.ojalgo.type.CalendarDate;
import org.ojalgo.type.CalendarDateUnit;
import org.ojalgo.type.context.NumberContext;

/**
 * RandomNumberTest
 *
 * @author apete
 * @author Chris Lucas
 */
public class RandomNumberTest extends RandomTests {

    /**
     * A wrapper for two-parameter random numbers to make it easier to generalize tests. Easy to extend to
     * single-parameter random numbers, or just apply as-is by having one throw-away parameter.
     */
    abstract static class Dist2 {

        public abstract RandomNumber getDist(double p0, double p1);
    }

    /**
     * Erlang's first argument is an int. For convenience, this takes the int value of the corresponding
     * double's floor, which should be the nearest lower int, according to the javadoc.
     */
    static class Dist2Erlang extends Dist2 {

        @Override
        public RandomNumber getDist(final double p0, final double p1) {
            if (p0 < 1.0) {
                throw new IllegalArgumentException("The first argument must be at least 1");
            }
            return new Erlang((int) FLOOR.invoke(p0), p1);
        }
    }

    static class Dist2Gamma extends Dist2 {

        @Override
        public RandomNumber getDist(final double p0, final double p1) {
            return new Gamma(p0, p1);
        }
    }

    static class Dist2Norm extends Dist2 {

        @Override
        public RandomNumber getDist(final double p0, final double p1) {
            return new Normal(p0, p1);
        }
    }

    static class Dist2Weibull extends Dist2 {

        @Override
        public RandomNumber getDist(final double p0, final double p1) {
            return new Weibull(p0, p1);
        }
    }

    public static void compareDensity(final ContinuousDistribution expected, final ContinuousDistribution actual) {
        RandomNumberTest.compareDensity(expected, actual, NumberContext.of(6));
    }

    public static void compareDensity(final ContinuousDistribution expected, final ContinuousDistribution actual, final NumberContext accuracy) {
        for (int d = -20; d < 21; d++) { // -2 .. 2
            double value = d / 10.0;
            double e = expected.getDensity(value);
            double a = actual.getDensity(value);
            TestUtils.assertEquals(e, a, accuracy);
        }
    }

    public static void compareDistribution(final ContinuousDistribution expected, final ContinuousDistribution actual) {
        RandomNumberTest.compareDistribution(expected, actual, NumberContext.of(6));
    }

    public static void compareDistribution(final ContinuousDistribution expected, final ContinuousDistribution actual, final NumberContext accuracy) {
        for (int d = -20; d < 21; d++) { // -2 .. 2
            double value = d / 10.0;
            double e = expected.getDistribution(-value);
            double a = actual.getDistribution(-value);
            TestUtils.assertEquals(e, a, accuracy);
        }
    }

    public static void compareQuantile(final ContinuousDistribution expected, final ContinuousDistribution actual) {
        RandomNumberTest.compareQuantile(expected, actual, NumberContext.of(6));
    }

    public static void compareQuantile(final ContinuousDistribution expected, final ContinuousDistribution actual, final NumberContext accuracy) {
        for (int t = 1; t < 10; t++) { // 0.1 .. 0.9
            double probability = t / 10.0;
            double e = expected.getQuantile(probability);
            double a = actual.getQuantile(probability);
            TestUtils.assertEquals(e, a, accuracy);
        }
    }

    /**
     * Tests that the error function implementation returns correct confidence intervals for +/- 6 standard
     * deviations. They are all correct to at least 10 decimal places.
     */
    @Test
    public void testERF() {

        double tmpStdDevCount;
        double tmpConfidence;
        double tmpError = NumberContext.of(7, 12).epsilon();

        tmpStdDevCount = ZERO;
        tmpConfidence = ZERO;
        TestUtils.assertEquals(tmpConfidence, ErrorFunction.erf(tmpStdDevCount / SQRT_TWO), tmpError);
        TestUtils.assertEquals(-tmpConfidence, ErrorFunction.erf(-tmpStdDevCount / SQRT_TWO), tmpError);

        tmpStdDevCount = ONE;
        tmpConfidence = 0.682689492137;
        TestUtils.assertEquals(tmpConfidence, ErrorFunction.erf(tmpStdDevCount / SQRT_TWO), tmpError);
        TestUtils.assertEquals(-tmpConfidence, ErrorFunction.erf(-tmpStdDevCount / SQRT_TWO), tmpError);

        tmpStdDevCount = TWO;
        tmpConfidence = 0.954499736104;
        TestUtils.assertEquals(tmpConfidence, ErrorFunction.erf(tmpStdDevCount / SQRT_TWO), tmpError);
        TestUtils.assertEquals(-tmpConfidence, ErrorFunction.erf(-tmpStdDevCount / SQRT_TWO), tmpError);

        tmpStdDevCount = THREE;
        tmpConfidence = 0.997300203937;
        TestUtils.assertEquals(tmpConfidence, ErrorFunction.erf(tmpStdDevCount / SQRT_TWO), tmpError);
        TestUtils.assertEquals(-tmpConfidence, ErrorFunction.erf(-tmpStdDevCount / SQRT_TWO), tmpError);

        tmpStdDevCount = FOUR;
        tmpConfidence = 0.999936657516;
        TestUtils.assertEquals(tmpConfidence, ErrorFunction.erf(tmpStdDevCount / SQRT_TWO), tmpError);
        TestUtils.assertEquals(-tmpConfidence, ErrorFunction.erf(-tmpStdDevCount / SQRT_TWO), tmpError);

        tmpStdDevCount = FIVE;
        tmpConfidence = 0.999999426697;
        TestUtils.assertEquals(tmpConfidence, ErrorFunction.erf(tmpStdDevCount / SQRT_TWO), tmpError);
        TestUtils.assertEquals(-tmpConfidence, ErrorFunction.erf(-tmpStdDevCount / SQRT_TWO), tmpError);

        tmpError = NumberContext.of(7, 8).epsilon();

        tmpStdDevCount = SIX;
        tmpConfidence = 0.999999998027;
        TestUtils.assertEquals(tmpConfidence, ErrorFunction.erf(tmpStdDevCount / SQRT_TWO), tmpError);
        TestUtils.assertEquals(-tmpConfidence, ErrorFunction.erf(-tmpStdDevCount / SQRT_TWO), tmpError);
    }

    @Test
    public void testERFandERFI() {

        double precision = 1E-14;
        double expected = -1.5;
        double actual;

        while (expected <= 1.5) {
            actual = ErrorFunction.erfi(ErrorFunction.erf(expected));
            TestUtils.assertEquals(expected, actual, precision);
            expected += 0.1;
        }

        if (DEBUG) {
            for (int i = -10; i <= 10; i++) {
                double d = i / 2.0;
                double erf = ErrorFunction.erf(d);
                double erfi = ErrorFunction.erfi(erf);
                double err = Math.abs(erfi - d);
                double mag = Math.abs(erfi);
                BasicLogger.debug("{} => {} => {} : {} : {}", d, erf, erfi, err, err / mag);
            }
        }
    }

    @Test
    public void testERFI() {

        double tmpConfidenceLevel;
        double tmpExpected;
        NumberContext tmpNewScale = NumberContext.of(2, 5);

        tmpConfidenceLevel = 0.80;
        tmpExpected = 1.28155;
        TestUtils.assertEquals(tmpExpected, SQRT_TWO * ErrorFunction.erfi(tmpConfidenceLevel), tmpNewScale);

        tmpConfidenceLevel = 0.90;
        tmpExpected = 1.64485;
        TestUtils.assertEquals(tmpExpected, SQRT_TWO * ErrorFunction.erfi(tmpConfidenceLevel), tmpNewScale);

        tmpConfidenceLevel = 0.95;
        tmpExpected = 1.95996;
        TestUtils.assertEquals(tmpExpected, SQRT_TWO * ErrorFunction.erfi(tmpConfidenceLevel), tmpNewScale);

        tmpConfidenceLevel = 0.98;
        tmpExpected = 2.32635;
        TestUtils.assertEquals(tmpExpected, SQRT_TWO * ErrorFunction.erfi(tmpConfidenceLevel), tmpNewScale);

        tmpConfidenceLevel = 0.99;
        tmpExpected = 2.57583;
        TestUtils.assertEquals(tmpExpected, SQRT_TWO * ErrorFunction.erfi(tmpConfidenceLevel), tmpNewScale);

        tmpConfidenceLevel = 0.995;
        tmpExpected = 2.80703;
        TestUtils.assertEquals(tmpExpected, SQRT_TWO * ErrorFunction.erfi(tmpConfidenceLevel), tmpNewScale);

        tmpConfidenceLevel = 0.998;
        tmpExpected = 3.09023;
        TestUtils.assertEquals(tmpExpected, SQRT_TWO * ErrorFunction.erfi(tmpConfidenceLevel), tmpNewScale);

        tmpConfidenceLevel = 0.999;
        tmpExpected = 3.29052;
        TestUtils.assertEquals(tmpExpected, SQRT_TWO * ErrorFunction.erfi(tmpConfidenceLevel), tmpNewScale);
    }

    @Test
    @Tag("unstable")
    public void testErlang() {
        // Erlang is a special case of the gamma where the count is an integer -- verify that this is true for
        // current implementation.
        for (double theta = .01; theta <= 10.0; theta = theta * 10.0) {
            for (int i = 1; i < 10; i++) {
                Erlang erl = new Erlang(i, theta);
                Gamma gam = new Gamma(i, theta);
                TestUtils.assertEquals("Gamma should match erlang for integer k", erl.getVariance(), gam.getVariance(), MACHINE_SMALLEST);
                TestUtils.assertEquals("Gamma should match erlang for integer k", erl.getExpected(), gam.getExpected(), MACHINE_SMALLEST);
            }
        }
        // param 1 is cast to integers by Dist2Erlang
        this.testDist2(new Dist2Erlang(), new double[] { 1, .01 }, new double[] { 5, 10 }, new double[] { 5, 100 }, 2500000, .05);
    }

    @Test
    @Tag("unstable")
    public void testGamma() {
        // TODO 15% error seems a little high
        this.testDist2(new Dist2Gamma(), new double[] { .01, .01 }, new double[] { 10, 10 }, new double[] { 100, 100 }, 200000, .15);
    }

    @Test
    public void testGeometricMeanAndStandardDeviation() {

        int tmpSize = 1000;

        double tmpFactoryExpected = 1.05;
        double tmpFactoryStdDev = ABS.invoke(new Normal(0.0, tmpFactoryExpected - ONE).doubleValue());
        Normal tmpFactoryDistr = new Normal(tmpFactoryExpected, tmpFactoryStdDev);
        TestUtils.assertEquals("Factory Expected", tmpFactoryExpected, tmpFactoryDistr.getExpected(), 1E-14 / THREE);
        TestUtils.assertEquals("Factory Std Dev", tmpFactoryStdDev, tmpFactoryDistr.getStandardDeviation(), 1E-14 / THREE);

        ArrayR064 tmpRawValues = ArrayR064.make(tmpSize);
        ArrayR064 tmpLogValues = ArrayR064.make(tmpSize);
        for (int i = 0; i < tmpSize; i++) {
            tmpRawValues.data[i] = tmpFactoryDistr.doubleValue();
            tmpLogValues.data[i] = LOG.invoke(tmpRawValues.data[i]);
        }
        SampleSet tmpLogValuesSet = SampleSet.wrap(tmpLogValues);
        LogNormal tmpLogDistribut = new LogNormal(tmpLogValuesSet.getMean(), tmpLogValuesSet.getStandardDeviation());

        double tmpGeometricMean = tmpLogDistribut.getGeometricMean();
        double tmpGeometricStandardDeviation = tmpLogDistribut.getGeometricStandardDeviation();

        double tmpRawProduct = ONE;
        for (int i = 0; i < tmpSize; i++) {
            tmpRawProduct *= tmpRawValues.data[i];
        }
        TestUtils.assertEquals(tmpGeometricMean, POW.invoke(tmpRawProduct, ONE / tmpSize), 1E-14 / THREE);

        double tmpLogSum = ZERO;
        for (int i = 0; i < tmpSize; i++) {
            tmpLogSum += tmpLogValues.data[i];
        }
        TestUtils.assertEquals(tmpGeometricMean, EXP.invoke(tmpLogSum / tmpSize), 1E-14 / THREE);

        double tmpLogGeoMean = LOG.invoke(tmpGeometricMean);

        double tmpVal;
        double tmpSumSqrDiff = ZERO;
        for (int i = 0; i < tmpSize; i++) {
            tmpVal = tmpLogValues.data[i] - tmpLogGeoMean;
            tmpSumSqrDiff += tmpVal * tmpVal;
        }
        TestUtils.assertEquals(tmpGeometricStandardDeviation / tmpGeometricStandardDeviation,
                EXP.invoke(SQRT.invoke(tmpSumSqrDiff / tmpSize)) / tmpGeometricStandardDeviation, 0.0001);
        // Check that the geometric standard deviation is within ±0.01% of what it should be.
    }

    @Test
    @Tag("unstable")
    public void testLogNormal() {

        RandomNumber tmpRandomNumber = new Normal(ONE, TENTH);
        double tmpValue = HUNDRED;
        CalendarDateSeries<Double> tmpSeries = new CalendarDateSeries<>();
        for (int i = 0; i < 1000; i++) {
            // tmpSeries.put(i, tmpValue);
            tmpSeries.put(CalendarDate.now().step(i, CalendarDateUnit.DAY), tmpValue); // TODO
            tmpValue *= tmpRandomNumber.doubleValue();
        }
        double[] someValues1 = tmpSeries.asPrimitive().toRawCopy1D();
        int tmpSize1 = someValues1.length - 1;

        double[] retVal1 = new double[tmpSize1];

        for (int i1 = 0; i1 < tmpSize1; i1++) {
            retVal1[i1] = someValues1[i1 + 1] / someValues1[i1];
        }

        SampleSet tmpQuotients = SampleSet.wrap(Access1D.wrap(retVal1));
        double[] someValues = tmpSeries.asPrimitive().toRawCopy1D();
        int tmpSize = someValues.length - 1;

        double[] retVal = new double[tmpSize];

        for (int i = 0; i < tmpSize; i++) {
            retVal[i] = LOG.invoke(someValues[i + 1] / someValues[i]);
        }
        SampleSet tmpLogChanges = SampleSet.wrap(Access1D.wrap(retVal));

        double accuracy = TENTH / TWO;

        // Quotient distribution parameters within 5% of the generating distribution
        TestUtils.assertEquals(ONE, tmpQuotients.getMean() / tmpRandomNumber.getExpected(), accuracy);
        TestUtils.assertEquals(ONE, tmpQuotients.getStandardDeviation() / tmpRandomNumber.getStandardDeviation(), accuracy);

        Normal tmpNormal = new Normal(tmpQuotients.getMean(), tmpQuotients.getStandardDeviation());
        LogNormal tmpLogNormal = new LogNormal(tmpLogChanges.getMean(), tmpLogChanges.getStandardDeviation());

        // LogNormal (logarithmic changes) parameters within 5% of the Normal (quotients) parameters
        TestUtils.assertEquals(ONE, tmpLogNormal.getExpected() / tmpNormal.getExpected(), accuracy);
        TestUtils.assertEquals(ONE, tmpLogNormal.getStandardDeviation() / tmpNormal.getStandardDeviation(), accuracy);
    }

    @Test
    public void testNorm2() {
        // Sample means differ from expectation by an amount greater than anticipated given the large samples.
        // May be due to
        // rounding error. For comparison, matlab's estimates for the mean of the normal for large samples
        // tend to be
        // within 2 percent of the mean.
        this.testDist2(new Dist2Norm(), new double[] { .01, .01 }, new double[] { 10, 10 }, new double[] { 100, 100 }, 1500000, .05);
    }

    @Test
    public void testNormal() {

        double[] tmpStdDevCount = { ZERO, ONE, TWO, THREE, FOUR, FIVE, SIX }; // ± this number of std devs
        double[] tmpConfidence = { ZERO, 0.682689492137, 0.954499736104, 0.997300203937, 0.999936657516, 0.999999426697, 0.999999998027 };

        Normal tmpDistribution = new Normal(TEN, PI);

        for (int c = 0; c < 4; c++) { // Can't handle the more extrem values

            double tmpHalfSideRemainder = (ONE - tmpConfidence[c]) / TWO;

            double tmpUpperBound = tmpDistribution.getQuantile(ONE - tmpHalfSideRemainder);
            double tmpLowerBound = tmpDistribution.getQuantile(tmpHalfSideRemainder);

            double tmpExpected = tmpStdDevCount[c];
            double tmpActual = (tmpUpperBound - tmpLowerBound) / TWO_PI; // Std Dev is PI

            TestUtils.assertEquals(tmpExpected, tmpActual, 5.0E-3);
        }
    }

    @Test
    @Tag("unstable")
    public void testSampledMean() {

        RandomNumber[] tmpRndNmbrs = { new Exponential(), new LogNormal(), new Normal(), new Uniform(), new Binomial(), new Geometric(), new Poisson(),
                new Erlang(), new Gamma(), new Weibull() };

        for (int d = 0; d < tmpRndNmbrs.length; d++) {

            RandomNumber tmpDistr = tmpRndNmbrs[d];

            SampleSet tmpSamples = SampleSet.make(tmpDistr, 1000);

            String tmpDistrName = tmpDistr.getClass().getSimpleName();
            double tmpDistrValue = tmpDistr.getExpected();
            double tmpSampledValue = tmpSamples.getMean();
            double tmpQuotient = tmpSampledValue / tmpDistrValue;

            double tmpExpected = PrimitiveScalar.isSmall(ONE, tmpDistrValue) ? tmpDistrValue : ONE;
            double tmpActual = PrimitiveScalar.isSmall(ONE, tmpDistrValue) ? tmpSampledValue : tmpQuotient;

            // BasicLogger.logDebug("Name={}: Value={} <=> Sampled={} == Quotient={}", tmpDistrName,
            // tmpDistrValue, tmpSampledValue, tmpQuotient);

            TestUtils.assertEquals(tmpDistrName, tmpExpected, tmpActual, TENTH);
        }

    }

    @Test
    public void testSettingSeed() {
        // Given
        Normal normal = new Normal();
        // When... failed
        normal.setSeed(1L);
        // Should not throw an exception, and the value should not change
        TestUtils.assertEquals(1.561581040188955, normal.doubleValue());
    }

    @Test
    public void testTDistributionFreedomCases() {

        RandomNumberTest.compareDensity(new TDistribution.Degree1(), new TDistribution(ONE));
        RandomNumberTest.compareDensity(new TDistribution.Degree2(), new TDistribution(TWO));
        RandomNumberTest.compareDensity(new TDistribution.Degree3(), new TDistribution(THREE));
        RandomNumberTest.compareDensity(new TDistribution.Degree4(), new TDistribution(FOUR));
        RandomNumberTest.compareDensity(new TDistribution.Degree5(), new TDistribution(FIVE));
        RandomNumberTest.compareDensity(new TDistribution.DegreeInfinity(), new TDistribution(Double.MAX_VALUE), NumberContext.of(2));

        RandomNumberTest.compareDistribution(new TDistribution.Degree1(), new TDistribution(ONE), NumberContext.of(2));
        RandomNumberTest.compareDistribution(new TDistribution.Degree2(), new TDistribution(TWO), NumberContext.of(2));
        RandomNumberTest.compareDistribution(new TDistribution.Degree3(), new TDistribution(THREE), NumberContext.of(2));
        RandomNumberTest.compareDistribution(new TDistribution.Degree4(), new TDistribution(FOUR), NumberContext.of(2));
        RandomNumberTest.compareDistribution(new TDistribution.Degree5(), new TDistribution(FIVE), NumberContext.of(2));
        RandomNumberTest.compareDistribution(new TDistribution.DegreeInfinity(), new TDistribution(Double.MAX_VALUE), NumberContext.of(1));

        // RandomNumberTest.compareQuantile(new TDistribution.Degree1(), new TDistribution(ONE));
        // RandomNumberTest.compareQuantile(new TDistribution.Degree2(), new TDistribution(TWO));
        // RandomNumberTest.compareQuantile(new TDistribution.Degree3(), new TDistribution(THREE));
        // RandomNumberTest.compareQuantile(new TDistribution.Degree4(), new TDistribution(FOUR));
        // RandomNumberTest.compareQuantile(new TDistribution.Degree5(), new TDistribution(FIVE));
        // RandomNumberTest.compareQuantile(new TDistribution.DegreeInfinity(), new
        // TDistribution(Double.MAX_VALUE));
    }

    @Test
    @Tag("unstable")
    public void testVariance() {

        double stdDev = TEN;
        double expectedVar = HUNDRED;
        int nbSamples = 10_000;
        double accuracy = ONE; // Within 1% of the expected

        SampleSet sampleSet = SampleSet.make(Normal.of(PI, stdDev), nbSamples);

        double actualVar = sampleSet.getVariance();
        TestUtils.assertEquals(expectedVar, actualVar, accuracy); // Won't always pass - it's random...

        actualVar = sampleSet.getSumOfSquares() / (nbSamples - 1);
        TestUtils.assertEquals(expectedVar, actualVar, accuracy);

        double[] tmpValues = sampleSet.getValues();
        double s = ZERO, s2 = ZERO;
        for (double tmpVal : tmpValues) {
            s += tmpVal;
            s2 += tmpVal * tmpVal;
        }

        actualVar = SampleSet.calculateVariance(s, s2, nbSamples);
        TestUtils.assertEquals(expectedVar, actualVar, accuracy); // TODO Large numerical difference
    }

    @Test
    public void testWeibull() {
        for (double i = .01; i <= 10.0; i = i * 10) {
            for (double j = .01; j <= 100.0; j = j * 10) {
                Weibull w0 = new Weibull(i, j);
                Weibull w1 = new Weibull(i, j);
                // There are analytic solutions available for mean and variance of Weibull, users will
                // probably assume they're being used.
                TestUtils.assertEquals("Weibull distribution's mean should be deterministic and precise.", w0.getExpected(), w1.getExpected());
                TestUtils.assertEquals("Weibull distribution's variance should be deterministic and precise.", w0.getVariance(), w1.getVariance());
            }
        }
        this.testDist2(new Dist2Weibull(), new double[] { .01, .5 }, new double[] { 10, 2 }, new double[] { 100, 4 }, 2500000, .05);
    }

    @Test
    public void testWeibullWithShape1() {

        // Weibull with shape=1.0 shoud be equivalent to Exponential with the same lambda
        double tmpEpsilon = 1E-14 / THREE * THOUSAND * TEN;

        for (double lambda = HUNDREDTH; lambda <= HUNDRED; lambda *= TEN) {
            Exponential tmpExpected = new Exponential(lambda);
            Weibull tmpActual = new Weibull(lambda, ONE);
            TestUtils.assertEquals("Expected/Mean, lambda=" + lambda, tmpExpected.getExpected(), tmpActual.getExpected(), tmpEpsilon);
            TestUtils.assertEquals("Variance, lambda=" + lambda, tmpExpected.getVariance(), tmpActual.getVariance(), tmpEpsilon);
        }
    }

    void testDist2(final Dist2 dist, final double[] min, final double[] mult, final double[] max, final int samples, final double accuracyBound) {

        for (double p0 = min[0]; p0 <= max[0]; p0 *= mult[0]) {
            for (double p1 = min[1]; p1 <= max[1]; p1 *= mult[1]) {
                RandomNumber tmpDistribution = dist.getDist(p0, p1);
                SampleSet tmpSamples = SampleSet.make(tmpDistribution, samples);
                // Used to estimate an upper bound on how much the sample should deviate from the analytic
                // expected value.
                double stErr = SQRT.invoke(tmpDistribution.getVariance() / samples);

                // Within 4 standard errors of the mean. False failures should be rare under this scheme.
                TestUtils.assertEquals("Sample mean was " + tmpSamples.getMean() + ", distribution mean was " + tmpDistribution.getExpected() + ".",
                        tmpSamples.getMean(), tmpDistribution.getExpected(), 4.0 * stErr);
                // Variance of variance is not always available or easy to obtain. accuracyBound is intended
                // as a rough estimate.
                TestUtils.assertEquals(ONE, tmpSamples.getVariance() / tmpDistribution.getVariance(), accuracyBound);
            }
        }
    }

}

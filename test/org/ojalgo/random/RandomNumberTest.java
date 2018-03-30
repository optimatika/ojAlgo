/*
 * Copyright 1997-2018 Optimatika
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

import static org.ojalgo.constant.PrimitiveMath.*;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.access.Access1D;
import org.ojalgo.array.Primitive64Array;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.scalar.PrimitiveScalar;
import org.ojalgo.series.CalendarDateSeries;
import org.ojalgo.type.context.NumberContext;

/**
 * RandomNumberTest
 *
 * @author apete
 * @author Chris Lucas
 */
public class RandomNumberTest {

    // A wrapper for two-parameter random numbers to make it easier to generalize tests. Easy to extend to single-parameter random numbers,
    // or just apply as-is by having one throw-away parameter.
    private abstract class Dist2 {

        public abstract RandomNumber getDist(double p0, double p1);
    }

    // Erlang's first argument is an int. For convenience, this takes the int value of the corresponding double's floor, which
    // should be the nearest lower int, according to the javadoc.
    private class Dist2Erlang extends Dist2 {

        @Override
        public RandomNumber getDist(final double p0, final double p1) {
            if (p0 < 1.0) {
                throw new IllegalArgumentException("The first argument must be at least 1");
            }
            return new Erlang((int) PrimitiveFunction.FLOOR.invoke(p0), p1);
        }
    }

    private class Dist2Gamma extends Dist2 {

        @Override
        public RandomNumber getDist(final double p0, final double p1) {
            return new Gamma(p0, p1);
        }
    }

    private class Dist2Norm extends Dist2 {

        @Override
        public RandomNumber getDist(final double p0, final double p1) {
            return new Normal(p0, p1);
        }
    }

    private class Dist2Weibull extends Dist2 {

        @Override
        public RandomNumber getDist(final double p0, final double p1) {
            return new Weibull(p0, p1);
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
        double tmpError = new NumberContext(7, 12).epsilon();

        tmpStdDevCount = ZERO;
        tmpConfidence = ZERO;
        TestUtils.assertEquals(tmpConfidence, RandomUtils.erf(tmpStdDevCount / SQRT_TWO), tmpError);
        TestUtils.assertEquals(-tmpConfidence, RandomUtils.erf(-tmpStdDevCount / SQRT_TWO), tmpError);

        tmpStdDevCount = ONE;
        tmpConfidence = 0.682689492137;
        TestUtils.assertEquals(tmpConfidence, RandomUtils.erf(tmpStdDevCount / SQRT_TWO), tmpError);
        TestUtils.assertEquals(-tmpConfidence, RandomUtils.erf(-tmpStdDevCount / SQRT_TWO), tmpError);

        tmpStdDevCount = TWO;
        tmpConfidence = 0.954499736104;
        TestUtils.assertEquals(tmpConfidence, RandomUtils.erf(tmpStdDevCount / SQRT_TWO), tmpError);
        TestUtils.assertEquals(-tmpConfidence, RandomUtils.erf(-tmpStdDevCount / SQRT_TWO), tmpError);

        tmpStdDevCount = THREE;
        tmpConfidence = 0.997300203937;
        TestUtils.assertEquals(tmpConfidence, RandomUtils.erf(tmpStdDevCount / SQRT_TWO), tmpError);
        TestUtils.assertEquals(-tmpConfidence, RandomUtils.erf(-tmpStdDevCount / SQRT_TWO), tmpError);

        tmpStdDevCount = FOUR;
        tmpConfidence = 0.999936657516;
        TestUtils.assertEquals(tmpConfidence, RandomUtils.erf(tmpStdDevCount / SQRT_TWO), tmpError);
        TestUtils.assertEquals(-tmpConfidence, RandomUtils.erf(-tmpStdDevCount / SQRT_TWO), tmpError);

        tmpStdDevCount = FIVE;
        tmpConfidence = 0.999999426697;
        TestUtils.assertEquals(tmpConfidence, RandomUtils.erf(tmpStdDevCount / SQRT_TWO), tmpError);
        TestUtils.assertEquals(-tmpConfidence, RandomUtils.erf(-tmpStdDevCount / SQRT_TWO), tmpError);

        tmpError = new NumberContext(7, 8).epsilon();

        tmpStdDevCount = SIX;
        tmpConfidence = 0.999999998027;
        TestUtils.assertEquals(tmpConfidence, RandomUtils.erf(tmpStdDevCount / SQRT_TWO), tmpError);
        TestUtils.assertEquals(-tmpConfidence, RandomUtils.erf(-tmpStdDevCount / SQRT_TWO), tmpError);
    }

    @Test
    public void testERFandERFI() {

        final double tmpError = 1E-14 / PrimitiveMath.THREE;
        double tmpExpected = -1.5;
        double tmpActual;

        while (tmpExpected <= 1.5) {

            tmpActual = RandomUtils.erfi(RandomUtils.erf(tmpExpected));

            TestUtils.assertEquals(tmpExpected, tmpActual, tmpError);

            tmpExpected += 0.5;
        }
    }

    @Test
    public void testERFI() {

        double tmpConfidenceLevel;
        double tmpExpected;
        final NumberContext tmpNewScale = new NumberContext(2, 5);

        tmpConfidenceLevel = 0.80;
        tmpExpected = 1.28155;
        TestUtils.assertEquals(tmpExpected, SQRT_TWO * RandomUtils.erfi(tmpConfidenceLevel), tmpNewScale);

        tmpConfidenceLevel = 0.90;
        tmpExpected = 1.64485;
        TestUtils.assertEquals(tmpExpected, SQRT_TWO * RandomUtils.erfi(tmpConfidenceLevel), tmpNewScale);

        tmpConfidenceLevel = 0.95;
        tmpExpected = 1.95996;
        TestUtils.assertEquals(tmpExpected, SQRT_TWO * RandomUtils.erfi(tmpConfidenceLevel), tmpNewScale);

        tmpConfidenceLevel = 0.98;
        tmpExpected = 2.32635;
        TestUtils.assertEquals(tmpExpected, SQRT_TWO * RandomUtils.erfi(tmpConfidenceLevel), tmpNewScale);

        tmpConfidenceLevel = 0.99;
        tmpExpected = 2.57583;
        TestUtils.assertEquals(tmpExpected, SQRT_TWO * RandomUtils.erfi(tmpConfidenceLevel), tmpNewScale);

        tmpConfidenceLevel = 0.995;
        tmpExpected = 2.80703;
        TestUtils.assertEquals(tmpExpected, SQRT_TWO * RandomUtils.erfi(tmpConfidenceLevel), tmpNewScale);

        tmpConfidenceLevel = 0.998;
        tmpExpected = 3.09023;
        TestUtils.assertEquals(tmpExpected, SQRT_TWO * RandomUtils.erfi(tmpConfidenceLevel), tmpNewScale);

        tmpConfidenceLevel = 0.999;
        tmpExpected = 3.29052;
        TestUtils.assertEquals(tmpExpected, SQRT_TWO * RandomUtils.erfi(tmpConfidenceLevel), tmpNewScale);
    }

    @Test
    public void testErlang() {
        // Erlang is a special case of the gamma where the count is an integer -- verify that this is true for current implementation.
        for (double theta = .01; theta <= 10.0; theta = theta * 10.0) {
            for (int i = 1; i < 10; i++) {
                final Erlang erl = new Erlang(i, theta);
                final Gamma gam = new Gamma(i, theta);
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
    public void testGammaFunction() {

        final double tmpEps = 0.000005;

        // From a table of values 1.0 <= x <= 2.0
        TestUtils.assertEquals(ONE, RandomUtils.gamma(1.0), 1E-14 / THREE);
        TestUtils.assertEquals(0.95135, RandomUtils.gamma(1.10), tmpEps);
        TestUtils.assertEquals(0.91817, RandomUtils.gamma(1.20), tmpEps);
        TestUtils.assertEquals(0.89747, RandomUtils.gamma(1.30), tmpEps);
        TestUtils.assertEquals(0.88726, RandomUtils.gamma(1.40), tmpEps);
        TestUtils.assertEquals(0.88623, RandomUtils.gamma(1.50), tmpEps);
        TestUtils.assertEquals(0.89352, RandomUtils.gamma(1.60), tmpEps);
        TestUtils.assertEquals(0.90864, RandomUtils.gamma(1.70), tmpEps);
        TestUtils.assertEquals(0.93138, RandomUtils.gamma(1.80), tmpEps);
        TestUtils.assertEquals(0.96177, RandomUtils.gamma(1.90), tmpEps);
        TestUtils.assertEquals(ONE, RandomUtils.gamma(2.0), 1E-14 / THREE);

        // Values larger than 2.0 and smaller than 1.0
        TestUtils.assertEquals("π", RandomUtils.gamma(PI), (PI - ONE) * (PI - TWO) * RandomUtils.gamma(PI - TWO), 1E-14 / THREE);
        TestUtils.assertEquals("0.5", RandomUtils.gamma(HALF), RandomUtils.gamma(HALF + ONE) / HALF, 1E-14 / THREE);
        TestUtils.assertEquals("0.25", RandomUtils.gamma(QUARTER), RandomUtils.gamma(QUARTER + ONE) / QUARTER, 1E-14 / THREE);
        TestUtils.assertEquals("0.1", RandomUtils.gamma(TENTH), RandomUtils.gamma(TENTH + ONE) / TENTH, tmpEps);
        TestUtils.assertEquals("0.01", RandomUtils.gamma(HUNDREDTH), RandomUtils.gamma(HUNDREDTH + ONE) / HUNDREDTH, tmpEps);
        TestUtils.assertEquals("0.001", RandomUtils.gamma(THOUSANDTH), RandomUtils.gamma(THOUSANDTH + ONE) / THOUSANDTH, tmpEps);

        // Should align with n! for positve integers
        for (int n = 0; n < 10; n++) {
            TestUtils.assertEquals("n!:" + n, RandomUtils.factorial(n), RandomUtils.gamma(n + ONE), tmpEps);
        }

        // Negative values
        TestUtils.assertEquals("-0.5", RandomUtils.gamma(-0.5), RandomUtils.gamma(HALF) / (-0.5), tmpEps);
        TestUtils.assertEquals("-1.5", RandomUtils.gamma(-1.5), RandomUtils.gamma(HALF) / (-1.5 * -0.5), tmpEps);
        TestUtils.assertEquals("-2.5", RandomUtils.gamma(-2.5), RandomUtils.gamma(HALF) / (-2.5 * -1.5 * -0.5), tmpEps);
        TestUtils.assertEquals("-3.5", RandomUtils.gamma(-3.5), RandomUtils.gamma(HALF) / (-3.5 * -2.5 * -1.5 * -0.5), tmpEps);
        TestUtils.assertEquals("-4.5", RandomUtils.gamma(-4.5), RandomUtils.gamma(HALF) / (-4.5 * -3.5 * -2.5 * -1.5 * -0.5), tmpEps);

        // Should be undefined for 0, -1, -2, -3...
        for (int n = 0; n < 10; n++) {
            TestUtils.assertTrue("-" + n, Double.isNaN(RandomUtils.gamma(NEG * n)));
        }

        final NumberContext tmpEval = new NumberContext(10, 10);

        // Positive half integer
        for (int n = 0; n < 10; n++) {
            TestUtils.assertEquals(n + ".5", (SQRT_PI * RandomUtils.factorial(2 * n)) / (PrimitiveFunction.POW.invoke(FOUR, n) * RandomUtils.factorial(n)),
                    RandomUtils.gamma(n + HALF), tmpEval);
        }

    }

    @Test
    @Tag("unstable")
    public void testGeometricMeanAndStandardDeviation() {

        final int tmpSize = 1000;

        final double tmpFactoryExpected = 1.05;
        final double tmpFactoryStdDev = PrimitiveFunction.ABS.invoke(new Normal(0.0, (tmpFactoryExpected - ONE)).doubleValue());
        final Normal tmpFactoryDistr = new Normal(tmpFactoryExpected, tmpFactoryStdDev);
        TestUtils.assertEquals("Factory Expected", tmpFactoryExpected, tmpFactoryDistr.getExpected(), 1E-14 / PrimitiveMath.THREE);
        TestUtils.assertEquals("Factory Std Dev", tmpFactoryStdDev, tmpFactoryDistr.getStandardDeviation(), 1E-14 / PrimitiveMath.THREE);

        final Primitive64Array tmpRawValues = Primitive64Array.make(tmpSize);
        final Primitive64Array tmpLogValues = Primitive64Array.make(tmpSize);
        for (int i = 0; i < tmpSize; i++) {
            tmpRawValues.data[i] = tmpFactoryDistr.doubleValue();
            tmpLogValues.data[i] = PrimitiveFunction.LOG.invoke(tmpRawValues.data[i]);
        }
        final SampleSet tmpLogValuesSet = SampleSet.wrap(tmpLogValues);
        final LogNormal tmpLogDistribut = new LogNormal(tmpLogValuesSet.getMean(), tmpLogValuesSet.getStandardDeviation());

        final double tmpGeometricMean = tmpLogDistribut.getGeometricMean();
        final double tmpGeometricStandardDeviation = tmpLogDistribut.getGeometricStandardDeviation();

        double tmpRawProduct = ONE;
        for (int i = 0; i < tmpSize; i++) {
            tmpRawProduct *= tmpRawValues.data[i];
        }
        TestUtils.assertEquals(tmpGeometricMean, PrimitiveFunction.POW.invoke(tmpRawProduct, ONE / tmpSize), 1E-14 / PrimitiveMath.THREE);

        double tmpLogSum = ZERO;
        for (int i = 0; i < tmpSize; i++) {
            tmpLogSum += tmpLogValues.data[i];
        }
        TestUtils.assertEquals(tmpGeometricMean, PrimitiveFunction.EXP.invoke(tmpLogSum / tmpSize), 1E-14 / PrimitiveMath.THREE);

        final double tmpLogGeoMean = PrimitiveFunction.LOG.invoke(tmpGeometricMean);

        double tmpVal;
        double tmpSumSqrDiff = ZERO;
        for (int i = 0; i < tmpSize; i++) {
            tmpVal = tmpLogValues.data[i] - tmpLogGeoMean;
            tmpSumSqrDiff += (tmpVal * tmpVal);
        }
        TestUtils.assertEquals(tmpGeometricStandardDeviation / tmpGeometricStandardDeviation,
                PrimitiveFunction.EXP.invoke(PrimitiveFunction.SQRT.invoke(tmpSumSqrDiff / tmpSize)) / tmpGeometricStandardDeviation, 0.00005);
        // Check that the geometric standard deviation is within ±0.005% of what it should be.
    }

    @Test
    @Disabled("Underscored before JUnit 5")
    public void testLogNormal() {

        final double tmpAccuracy = TENTH / THREE;

        final RandomNumber tmpRandomNumber = new Normal(ONE, TENTH);
        double tmpValue = HUNDRED;
        final CalendarDateSeries<Double> tmpSeries = new CalendarDateSeries<>();
        for (int i = 0; i < 1000; i++) {
            tmpSeries.put(i, tmpValue);
            tmpValue *= tmpRandomNumber.doubleValue();
        }
        final double[] someValues1 = tmpSeries.asPrimitive().toRawCopy1D();
        final int tmpSize1 = someValues1.length - 1;

        final double[] retVal1 = new double[tmpSize1];

        for (int i1 = 0; i1 < tmpSize1; i1++) {
            retVal1[i1] = someValues1[i1 + 1] / someValues1[i1];
        }

        final SampleSet tmpQuotients = SampleSet.wrap(Access1D.wrap(retVal1));
        final double[] someValues = tmpSeries.asPrimitive().toRawCopy1D();
        final int tmpSize = someValues.length - 1;

        final double[] retVal = new double[tmpSize];

        for (int i = 0; i < tmpSize; i++) {
            retVal[i] = PrimitiveFunction.LOG.invoke(someValues[i + 1] / someValues[i]);
        }
        final SampleSet tmpLogChanges = SampleSet.wrap(Access1D.wrap(retVal));

        // Quotient distribution parameters within 3% of the generating distribution
        TestUtils.assertEquals(ONE, tmpQuotients.getMean() / tmpRandomNumber.getExpected(), tmpAccuracy);
        TestUtils.assertEquals(ONE, tmpQuotients.getStandardDeviation() / tmpRandomNumber.getStandardDeviation(), tmpAccuracy);

        final Normal tmpNormal = new Normal(tmpQuotients.getMean(), tmpQuotients.getStandardDeviation());
        final LogNormal tmpLogNormal = new LogNormal(tmpLogChanges.getMean(), tmpLogChanges.getStandardDeviation());

        // LogNormal (logarithmic changes) parameters within 3% of the Normal (quotients) parameters
        TestUtils.assertEquals(ONE, tmpLogNormal.getExpected() / tmpNormal.getExpected(), tmpAccuracy);
        TestUtils.assertEquals(ONE, tmpLogNormal.getStandardDeviation() / tmpNormal.getStandardDeviation(), tmpAccuracy);
    }

    // Sample means differ from expectation by an amount greater than anticipated given the large samples. May be due to
    // rounding error. For comparison, matlab's estimates for the mean of the normal for large samples tend to be
    // within 2 percent of the mean.
    @Test
    public void testNorm2() {
        this.testDist2(new Dist2Norm(), new double[] { .01, .01 }, new double[] { 10, 10 }, new double[] { 100, 100 }, 1500000, .05);
    }

    @Test
    public void testNormal() {

        final double[] tmpStdDevCount = new double[] { ZERO, ONE, TWO, THREE, FOUR, FIVE, SIX }; // ± this number of std devs
        final double[] tmpConfidence = new double[] { ZERO, 0.682689492137, 0.954499736104, 0.997300203937, 0.999936657516, 0.999999426697, 0.999999998027 };

        final Normal tmpDistribution = new Normal(TEN, PI);

        for (int c = 0; c < 4; c++) { // Can't handle the more extrem values

            final double tmpHalfSideRemainder = (ONE - tmpConfidence[c]) / TWO;

            final double tmpUpperBound = tmpDistribution.getQuantile(ONE - tmpHalfSideRemainder);
            final double tmpLowerBound = tmpDistribution.getQuantile(tmpHalfSideRemainder);

            final double tmpExpected = tmpStdDevCount[c];
            final double tmpActual = (tmpUpperBound - tmpLowerBound) / (TWO_PI); // Std Dev is PI

            TestUtils.assertEquals(tmpExpected, tmpActual, 5.0E-3);
        }

    }

    @Test
    @Tag("unstable")
    public void testSampledMean() {

        final RandomNumber[] tmpRndNmbrs = new RandomNumber[] { new Exponential(), new LogNormal(), new Normal(), new Uniform(), new Binomial(),
                new Geometric(), new Poisson(), new Erlang(), new Gamma(), new Weibull() };

        for (int d = 0; d < tmpRndNmbrs.length; d++) {

            final RandomNumber tmpDistr = tmpRndNmbrs[d];

            final SampleSet tmpSamples = SampleSet.make(tmpDistr, 1000);

            final String tmpDistrName = tmpDistr.getClass().getSimpleName();
            final double tmpDistrValue = tmpDistr.getExpected();
            final double tmpSampledValue = tmpSamples.getMean();
            final double tmpQuotient = tmpSampledValue / tmpDistrValue;

            final double tmpExpected = PrimitiveScalar.isSmall(PrimitiveMath.ONE, tmpDistrValue) ? tmpDistrValue : ONE;
            final double tmpActual = PrimitiveScalar.isSmall(PrimitiveMath.ONE, tmpDistrValue) ? tmpSampledValue : tmpQuotient;

            //            BasicLogger.logDebug("Name={}: Value={} <=> Sampled={} == Quotient={}", tmpDistrName, tmpDistrValue, tmpSampledValue, tmpQuotient);

            TestUtils.assertEquals(tmpDistrName, tmpExpected, tmpActual, TENTH);
        }

    }

    @Test
    @Tag("unstable")
    public void testVariance() {

        final double tmpStdDev = TEN;
        double tmpExpectedVar = tmpStdDev * tmpStdDev;

        final SampleSet tmpSampleSet = SampleSet.make(new Normal(PI, tmpStdDev), 10000);

        double tmpActualVar = tmpSampleSet.getVariance();

        TestUtils.assertEquals(tmpExpectedVar, tmpActualVar, PrimitiveFunction.SQRT.invoke(TEN)); // Won't always pass - it's random...

        tmpExpectedVar = tmpSampleSet.getSumOfSquares() / (tmpSampleSet.size() - 1);

        TestUtils.assertEquals(tmpExpectedVar, tmpActualVar, 1E-14 / THREE);

        final double[] tmpValues = tmpSampleSet.getValues();
        double s = ZERO, s2 = ZERO;
        for (final double tmpVal : tmpValues) {
            s += tmpVal;
            s2 += tmpVal * tmpVal;
        }

        tmpActualVar = RandomUtils.calculateVariance(s, s2, tmpValues.length);

        TestUtils.assertEquals(tmpExpectedVar, tmpActualVar, THOUSAND * (1E-14 / THREE)); // TODO Large numerical difference, which is better?
    }

    @Test
    public void testWeibull() {
        for (double i = .01; i <= 10.0; i = i * 10) {
            for (double j = .01; j <= 100.0; j = j * 10) {
                final Weibull w0 = new Weibull(i, j);
                final Weibull w1 = new Weibull(i, j);
                // There are analytic solutions available for mean and variance of Weibull, users will probably assume they're being used.
                TestUtils.assertEquals("Weibull distribution's mean should be deterministic and precise.", w0.getExpected(), w1.getExpected());
                TestUtils.assertEquals("Weibull distribution's variance should be deterministic and precise.", w0.getVariance(), w1.getVariance());
            }
        }
        this.testDist2(new Dist2Weibull(), new double[] { .01, .5 }, new double[] { 10, 2 }, new double[] { 100, 4 }, 2500000, .05);
    }

    @Test
    public void testWeibullWithShape1() {

        // Weibull with shape=1.0 shoud be equivalent to Exponential with the same lambda
        final double tmpEpsilon = (1E-14 / THREE) * THOUSAND * TEN;

        for (double lambda = HUNDREDTH; lambda <= HUNDRED; lambda *= TEN) {
            final Exponential tmpExpected = new Exponential(lambda);
            final Weibull tmpActual = new Weibull(lambda, ONE);
            TestUtils.assertEquals("Expected/Mean, lambda=" + lambda, tmpExpected.getExpected(), tmpActual.getExpected(), tmpEpsilon);
            TestUtils.assertEquals("Variance, lambda=" + lambda, tmpExpected.getVariance(), tmpActual.getVariance(), tmpEpsilon);
        }
    }

    void testDist2(final Dist2 dist, final double[] min, final double[] mult, final double[] max, final int samples, final double accuracyBound) {

        for (double p0 = min[0]; p0 <= max[0]; p0 *= mult[0]) {
            for (double p1 = min[1]; p1 <= max[1]; p1 *= mult[1]) {
                final RandomNumber tmpDistribution = dist.getDist(p0, p1);
                final SampleSet tmpSamples = SampleSet.make(tmpDistribution, samples);
                // Used to estimate an upper bound on how much the sample should deviate from the analytic expected value.
                final double stErr = PrimitiveFunction.SQRT.invoke(tmpDistribution.getVariance() / samples);

                // Within 4 standard errors of the mean. False failures should be rare under this scheme.
                TestUtils.assertEquals("Sample mean was " + tmpSamples.getMean() + ", distribution mean was " + tmpDistribution.getExpected() + ".",
                        tmpSamples.getMean(), tmpDistribution.getExpected(), 4.0 * stErr);
                // Variance of variance is not always available or easy to obtain. accuracyBound is intended as a rough estimate.
                TestUtils.assertEquals(ONE, tmpSamples.getVariance() / tmpDistribution.getVariance(), accuracyBound);
            }
        }
    }

}

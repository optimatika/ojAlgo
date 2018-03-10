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
package org.ojalgo.random.process;

import static org.ojalgo.constant.PrimitiveMath.*;

import org.ojalgo.access.Access1D;
import org.ojalgo.array.Array1D;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.random.LogNormal;
import org.ojalgo.random.RandomUtils;
import org.ojalgo.random.SampleSet;

/**
 * Diffusion process defined by a stochastic differential equation: dX = r X dt + s X dW A stochastic process
 * is said to follow a geometric Brownian motion if it satisfies this stochastic differential equation.
 *
 * @author apete
 */
public final class GeometricBrownianMotion extends AbstractProcess<LogNormal> {

    private static final WienerProcess GENERATOR = new WienerProcess();

    /**
     * @param seriesOfSamples A series of samples, evenly spaced in time.
     * @param samplePeriod The amount of time (in which ever unit you prefer) between each sample in the
     *        series.
     */
    public static GeometricBrownianMotion estimate(final Access1D<?> seriesOfSamples, final double samplePeriod) {

        final int tmpSizeMinusOne = (int) (seriesOfSamples.count() - 1);
        final Array1D<Double> tmpLogDiffSeries = Array1D.PRIMITIVE64.makeZero(tmpSizeMinusOne);
        for (int i = 0; i < tmpSizeMinusOne; i++) {
            tmpLogDiffSeries.set(i, PrimitiveFunction.LOG.invoke(seriesOfSamples.doubleValue(i + 1) / seriesOfSamples.doubleValue(i)));
        }
        final SampleSet tmpSampleSet = SampleSet.wrap(tmpLogDiffSeries);

        final double tmpExp = tmpSampleSet.getMean();
        final double tmpVar = tmpSampleSet.getVariance();

        final double tmpDiff = PrimitiveFunction.SQRT.invoke(tmpVar / samplePeriod);
        final double tmpDrift = (tmpExp / samplePeriod) + ((tmpDiff * tmpDiff) / TWO);

        final GeometricBrownianMotion retVal = new GeometricBrownianMotion(tmpDrift, tmpDiff);
        retVal.setValue(seriesOfSamples.doubleValue(0)); // TODO Seems more natural to set it to the last value, but then some tests fail (need to look into why.)
        return retVal;
    }

    /**
     * Assuming initial value = 1.0 and horizon = 1.0.
     */
    public static GeometricBrownianMotion make(final double expected, final double variance) {
        return GeometricBrownianMotion.make(ONE, expected, variance, ONE);
    }

    /**
     * Assuming initial value = 1.0.
     */
    public static GeometricBrownianMotion make(final double expected, final double variance, final double horizon) {
        return GeometricBrownianMotion.make(ONE, expected, variance, horizon);
    }

    /**
     * @param initialValue The process initial value.
     * @param expectedFutureValue An expected value (sometime in the future).
     * @param aVariance The variance of that future value.
     * @param aHorizon When do you expect that value?
     */
    public static GeometricBrownianMotion make(final double initialValue, final double expectedFutureValue, final double aVariance, final double aHorizon) {

        final double tmpDrift = PrimitiveFunction.LOG.invoke(expectedFutureValue / initialValue) / aHorizon;
        final double tmpDiff = PrimitiveFunction.SQRT
                .invoke(PrimitiveFunction.LOG1P.invoke(aVariance / (expectedFutureValue * expectedFutureValue)) / aHorizon);

        final GeometricBrownianMotion retVal = new GeometricBrownianMotion(tmpDrift, tmpDiff);

        retVal.setValue(initialValue);

        return retVal;
    }

    private final double myDiffusionFunction;
    private final double myLocalDrift;

    public GeometricBrownianMotion(final double localDrift, final double diffusionFunction) {

        super();

        this.setValue(ONE);

        myLocalDrift = localDrift;
        myDiffusionFunction = diffusionFunction;
    }

    @SuppressWarnings("unused")
    private GeometricBrownianMotion() {
        this(ZERO, ZERO);
    }

    /**
     * @param convertionFactor A step size change factor.
     */
    public GeometricBrownianMotion convert(final double convertionFactor) {

        final double tmpDrift = myLocalDrift * convertionFactor;
        final double tmpDiff = myDiffusionFunction * PrimitiveFunction.SQRT.invoke(convertionFactor);

        return new GeometricBrownianMotion(tmpDrift, tmpDiff);
    }

    public LogNormal getDistribution(final double evaluationPoint) {

        final double tmpVar = this.getDistributionVariance(evaluationPoint);

        final double tmpLocation = this.getDistributionLocation(evaluationPoint, tmpVar);

        final double tmpScale = PrimitiveFunction.SQRT.invoke(tmpVar);

        return new LogNormal(tmpLocation, tmpScale);
    }

    private final double getDistributionLocation(final double stepSize, final double variance) {
        return (PrimitiveFunction.LOG.invoke(this.getValue()) + (myLocalDrift * stepSize)) - (HALF * variance);
    }

    private final double getDistributionVariance(final double stepSize) {
        return myDiffusionFunction * myDiffusionFunction * stepSize;
    }

    @Override
    protected double getNormalisedRandomIncrement() {
        return GENERATOR.getNormalisedRandomIncrement();
    }

    @Override
    protected double step(final double currentValue, final double stepSize, final double normalisedRandomIncrement) {

        final double tmpDetPart = (myLocalDrift - ((myDiffusionFunction * myDiffusionFunction) / TWO)) * stepSize;
        final double tmpRandPart = myDiffusionFunction * PrimitiveFunction.SQRT.invoke(stepSize) * normalisedRandomIncrement;

        final double retVal = currentValue * PrimitiveFunction.EXP.invoke(tmpDetPart + tmpRandPart);
        this.setValue(retVal);
        return retVal;
    }

    /**
     * Expected future value
     */
    @Override
    double getExpected(final double stepSize) {
        return this.getValue() * PrimitiveFunction.EXP.invoke(myLocalDrift * stepSize);
    }

    @Override
    double getLowerConfidenceQuantile(final double stepSize, final double confidence) {

        final double tmpVar = this.getDistributionVariance(stepSize);

        final double tmpLocation = this.getDistributionLocation(stepSize, tmpVar);

        final double tmpScale = PrimitiveFunction.SQRT.invoke(tmpVar);

        return PrimitiveFunction.EXP.invoke(tmpLocation - (tmpScale * SQRT_TWO * RandomUtils.erfi(confidence)));
    }

    @Override
    double getStandardDeviation(final double stepSize) {
        return PrimitiveFunction.SQRT.invoke(this.getVariance(stepSize));
    }

    @Override
    double getUpperConfidenceQuantile(final double stepSize, final double confidence) {

        final double tmpVar = this.getDistributionVariance(stepSize);

        final double tmpLocation = this.getDistributionLocation(stepSize, tmpVar);

        final double tmpScale = PrimitiveFunction.SQRT.invoke(tmpVar);

        return PrimitiveFunction.EXP.invoke(tmpLocation + (tmpScale * SQRT_TWO * RandomUtils.erfi(confidence)));
    }

    @Override
    double getVariance(final double stepSize) {
        return this.getValue() * this.getValue() * PrimitiveFunction.EXP.invoke(TWO * myLocalDrift * stepSize)
                * PrimitiveFunction.EXPM1.invoke(this.getDistributionVariance(stepSize));
    }

}

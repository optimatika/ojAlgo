/*
 * Copyright 1997-2015 Optimatika (www.optimatika.se)
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
     * @param aSeriesOfSamples A series of samples, evenly spaced in time.
     * @param aSamplePeriod The amount of time (in which ever unit you prefer) between each sample in the
     *        series.
     */
    public static GeometricBrownianMotion estimate(final Access1D<?> aSeriesOfSamples, final double aSamplePeriod) {

        final int tmpSizeMinusOne = (int) (aSeriesOfSamples.count() - 1);
        final Array1D<Double> tmpLogDiffSeries = Array1D.PRIMITIVE.makeZero(tmpSizeMinusOne);
        for (int i = 0; i < tmpSizeMinusOne; i++) {
            tmpLogDiffSeries.set(i, Math.log(aSeriesOfSamples.doubleValue(i + 1) / aSeriesOfSamples.doubleValue(i)));
        }
        final SampleSet tmpSampleSet = SampleSet.wrap(tmpLogDiffSeries);

        final double tmpExp = tmpSampleSet.getMean();
        final double tmpVar = tmpSampleSet.getVariance();

        final double tmpDiff = Math.sqrt(tmpVar / aSamplePeriod);
        final double tmpDrift = (tmpExp / aSamplePeriod) + ((tmpDiff * tmpDiff) / TWO);

        final GeometricBrownianMotion retVal = new GeometricBrownianMotion(tmpDrift, tmpDiff);
        retVal.setValue(aSeriesOfSamples.doubleValue(0));
        return retVal;
    }

    /**
     * Assuming initial value = 1.0 and horizon = 1.0.
     */
    public static GeometricBrownianMotion make(final double aExpected, final double aVariance) {
        return GeometricBrownianMotion.make(ONE, aExpected, aVariance, ONE);
    }

    /**
     * Assuming initial value = 1.0.
     */
    public static GeometricBrownianMotion make(final double aExpected, final double aVariance, final double aHorizon) {
        return GeometricBrownianMotion.make(ONE, aExpected, aVariance, aHorizon);
    }

    /**
     * @param initialValue The process initial value.
     * @param expectedFutureValue An expected value (sometime in the future).
     * @param aVariance The variance of that future value.
     * @param aHorizon When do you expect that value?
     */
    public static GeometricBrownianMotion make(final double initialValue, final double expectedFutureValue, final double aVariance, final double aHorizon) {

        final double tmpDrift = Math.log(expectedFutureValue / initialValue) / aHorizon;
        final double tmpDiff = Math.sqrt(Math.log1p(aVariance / (expectedFutureValue * expectedFutureValue)) / aHorizon);

        final GeometricBrownianMotion retVal = new GeometricBrownianMotion(tmpDrift, tmpDiff);

        retVal.setValue(initialValue);

        return retVal;
    }

    private final double myDiffusionFunction;
    private final double myLocalDrift;

    public GeometricBrownianMotion(final double aLocalDrift, final double aDiffusionFunction) {

        super();

        this.setValue(ONE);

        myLocalDrift = aLocalDrift;
        myDiffusionFunction = aDiffusionFunction;
    }

    @SuppressWarnings("unused")
    private GeometricBrownianMotion() {
        this(ZERO, ZERO);
    }

    /**
     * @param aConvertionFactor A step size change factor.
     */
    public GeometricBrownianMotion convert(final double aConvertionFactor) {

        final double tmpDrift = myLocalDrift * aConvertionFactor;
        final double tmpDiff = myDiffusionFunction * Math.sqrt(aConvertionFactor);

        return new GeometricBrownianMotion(tmpDrift, tmpDiff);
    }

    public LogNormal getDistribution(final double evaluationPoint) {

        final double tmpVar = this.getDistributionVariance(evaluationPoint);

        final double tmpLocation = this.getDistributionLocation(evaluationPoint, tmpVar);

        final double tmpScale = Math.sqrt(tmpVar);

        return new LogNormal(tmpLocation, tmpScale);
    }

    private final double getDistributionLocation(final double aStepSize, final double aVariance) {
        return (Math.log(this.getValue()) + (myLocalDrift * aStepSize)) - (HALF * aVariance);
    }

    private final double getDistributionVariance(final double aStepSize) {
        return myDiffusionFunction * myDiffusionFunction * aStepSize;
    }

    @Override
    protected double getNormalisedRandomIncrement() {
        return GENERATOR.getNormalisedRandomIncrement();
    }

    @Override
    protected double step(final double currentValue, final double stepSize, final double normalisedRandomIncrement) {

        final double tmpDetPart = (myLocalDrift - ((myDiffusionFunction * myDiffusionFunction) / TWO)) * stepSize;
        final double tmpRandPart = myDiffusionFunction * Math.sqrt(stepSize) * normalisedRandomIncrement;

        final double retVal = currentValue * Math.exp(tmpDetPart + tmpRandPart);
        this.setValue(retVal);
        return retVal;
    }

    /**
     * Expected future value
     */
    @Override
    double getExpected(final double aStepSize) {
        return this.getValue() * Math.exp(myLocalDrift * aStepSize);
    }

    @Override
    double getLowerConfidenceQuantile(final double aStepSize, final double aConfidence) {

        final double tmpVar = this.getDistributionVariance(aStepSize);

        final double tmpLocation = this.getDistributionLocation(aStepSize, tmpVar);

        final double tmpScale = Math.sqrt(tmpVar);

        return Math.exp(tmpLocation - (tmpScale * SQRT_TWO * RandomUtils.erfi(aConfidence)));
    }

    @Override
    double getStandardDeviation(final double aStepSize) {
        return Math.sqrt(this.getVariance(aStepSize));
    }

    @Override
    double getUpperConfidenceQuantile(final double aStepSize, final double aConfidence) {

        final double tmpVar = this.getDistributionVariance(aStepSize);

        final double tmpLocation = this.getDistributionLocation(aStepSize, tmpVar);

        final double tmpScale = Math.sqrt(tmpVar);

        return Math.exp(tmpLocation + (tmpScale * SQRT_TWO * RandomUtils.erfi(aConfidence)));
    }

    @Override
    double getVariance(final double aStepSize) {
        return this.getValue() * this.getValue() * Math.exp(TWO * myLocalDrift * aStepSize) * Math.expm1(this.getDistributionVariance(aStepSize));
    }

}

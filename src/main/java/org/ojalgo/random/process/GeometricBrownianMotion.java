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
package org.ojalgo.random.process;

import static org.ojalgo.function.constant.PrimitiveMath.*;

import org.ojalgo.array.Array1D;
import org.ojalgo.function.special.ErrorFunction;
import org.ojalgo.random.LogNormal;
import org.ojalgo.random.SampleSet;
import org.ojalgo.structure.Access1D;

/**
 * Diffusion process defined by a stochastic differential equation:
 *
 * <pre>
 * dX = r X dt + s X dW
 * </pre>
 *
 * A stochastic process is said to follow a geometric Brownian motion if it satisfies this stochastic
 * differential equation.
 *
 * @author apete
 */
public final class GeometricBrownianMotion extends SingleValueBasedProcess<LogNormal> implements Process1D.ComponentProcess<LogNormal> {

    private static final WienerProcess GENERATOR = new WienerProcess();

    /**
     * @param seriesOfSamples A series of samples, evenly spaced in time.
     * @param samplePeriod The amount of time (in which ever unit you prefer) between each sample in the
     *        series.
     */
    public static GeometricBrownianMotion estimate(final Access1D<?> seriesOfSamples, final double samplePeriod) {

        int sizeMinusOne = seriesOfSamples.size() - 1;
        Array1D<Double> logDiffSeries = Array1D.R064.make(sizeMinusOne);
        for (int i = 0; i < sizeMinusOne; i++) {
            logDiffSeries.set(i, LOG.invoke(seriesOfSamples.doubleValue(i + 1) / seriesOfSamples.doubleValue(i)));
        }
        SampleSet sampleSet = SampleSet.wrap(logDiffSeries);

        double tmpExp = sampleSet.getMean();
        double tmpVar = sampleSet.getVariance();

        double tmpDiff = SQRT.invoke(tmpVar / samplePeriod);
        double tmpDrift = (tmpExp / samplePeriod) + ((tmpDiff * tmpDiff) / TWO);

        GeometricBrownianMotion retVal = new GeometricBrownianMotion(tmpDrift, tmpDiff);
        retVal.setValue(seriesOfSamples.doubleValue(sizeMinusOne));
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

        double tmpDrift = LOG.invoke(expectedFutureValue / initialValue) / aHorizon;
        double tmpDiff = SQRT.invoke(LOG1P.invoke(aVariance / (expectedFutureValue * expectedFutureValue)) / aHorizon);

        GeometricBrownianMotion retVal = new GeometricBrownianMotion(tmpDrift, tmpDiff);

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

        double tmpDrift = myLocalDrift * convertionFactor;
        double tmpDiff = myDiffusionFunction * SQRT.invoke(convertionFactor);

        return new GeometricBrownianMotion(tmpDrift, tmpDiff);
    }

    public LogNormal getDistribution(final double evaluationPoint) {

        double tmpVar = this.getDistributionVariance(evaluationPoint);

        double tmpLocation = this.getDistributionLocation(evaluationPoint, tmpVar);

        double tmpScale = SQRT.invoke(tmpVar);

        return new LogNormal(tmpLocation, tmpScale);
    }

    public double getValue() {
        return this.getCurrentValue();
    }

    public void setValue(final double newValue) {
        this.setCurrentValue(newValue);
    }

    @Override
    public double step(final double stepSize, final double standardGaussianInnovation) {
        return this.doStep(stepSize, standardGaussianInnovation);
    }

    private double getDistributionLocation(final double stepSize, final double variance) {
        return (LOG.invoke(this.getValue()) + (myLocalDrift * stepSize)) - (HALF * variance);
    }

    private double getDistributionVariance(final double stepSize) {
        return myDiffusionFunction * myDiffusionFunction * stepSize;
    }

    @Override
    double doStep(final double stepSize, final double normalisedRandomIncrement) {

        double detPart = (myLocalDrift - ((myDiffusionFunction * myDiffusionFunction) / TWO)) * stepSize;
        double randPart = myDiffusionFunction * SQRT.invoke(stepSize) * normalisedRandomIncrement;

        double retVal = this.getCurrentValue() * EXP.invoke(detPart + randPart);
        this.setCurrentValue(retVal);
        return retVal;
    }

    /**
     * Expected future value
     */
    @Override
    double getExpected(final double stepSize) {
        return this.getValue() * EXP.invoke(myLocalDrift * stepSize);
    }

    @Override
    double getLowerConfidenceQuantile(final double stepSize, final double confidence) {

        double tmpVar = this.getDistributionVariance(stepSize);

        double tmpLocation = this.getDistributionLocation(stepSize, tmpVar);

        double tmpScale = SQRT.invoke(tmpVar);

        return EXP.invoke(tmpLocation - (tmpScale * SQRT_TWO * ErrorFunction.erfi(confidence)));
    }

    @Override
    double getNormalisedRandomIncrement() {
        return GENERATOR.getNormalisedRandomIncrement();
    }

    @Override
    double getStandardDeviation(final double stepSize) {
        return SQRT.invoke(this.getVariance(stepSize));
    }

    @Override
    double getUpperConfidenceQuantile(final double stepSize, final double confidence) {

        double tmpVar = this.getDistributionVariance(stepSize);

        double tmpLocation = this.getDistributionLocation(stepSize, tmpVar);

        double tmpScale = SQRT.invoke(tmpVar);

        return EXP.invoke(tmpLocation + (tmpScale * SQRT_TWO * ErrorFunction.erfi(confidence)));
    }

    @Override
    double getVariance(final double stepSize) {
        return this.getValue() * this.getValue() * EXP.invoke(TWO * myLocalDrift * stepSize) * EXPM1.invoke(this.getDistributionVariance(stepSize));
    }
}

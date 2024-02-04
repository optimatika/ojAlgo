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

import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.random.Normal;
import org.ojalgo.random.Normal1D;

/**
 * A Gaussian process is a {@link RandomProcess} where each variable has a normal distribution. In addition,
 * every finite collection of those variables has a multivariate normal distribution.
 * <P>
 * Prior to calling {@linkplain #getDistribution(double)} or {@linkplain #simulate(int, int, double)} you must
 * call {@linkplain #addObservation(Double, double)} one or more times.
 *
 * @author apete
 */
public final class GaussianProcess extends MultipleValuesBasedProcess<Normal> implements Process1D.ComponentProcess<Normal> {

    private static final Normal GENERATOR = new Normal();

    private final GaussianField<Double> myDelegate;

    public GaussianProcess(final GaussianField.Covariance<Double> covarFunc) {

        super();

        myDelegate = new GaussianField<>(covarFunc, this.getObservations());
    }

    public GaussianProcess(final GaussianField.Mean<Double> meanFunc, final GaussianField.Covariance<Double> covarFunc) {

        super();

        myDelegate = new GaussianField<>(meanFunc, covarFunc, this.getObservations());
    }

    @SuppressWarnings("unused")
    private GaussianProcess() {
        this(null, null);
    }

    public void calibrate() {
        myDelegate.calibrate();
    }

    public Normal getDistribution(final double evaluationPoint) {

        Normal1D tmpVal = this.getDistribution(new Double[] { evaluationPoint });

        double tmpLocation = tmpVal.getExpected().doubleValue(0);
        double tmpScale = tmpVal.getStandardDeviation().doubleValue(0);

        return new Normal(tmpLocation, tmpScale);
    }

    public Normal1D getDistribution(final Double... evaluationPoint) {
        return myDelegate.getDistribution(false, evaluationPoint);
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

    @Override
    double doStep(final double stepSize, final double normalisedRandomIncrement) {

        Normal distr = this.getDistribution(stepSize);

        double retVal = (normalisedRandomIncrement * distr.getStandardDeviation()) + distr.getExpected();
        this.addObservation(this.getObservations().last().getKey() + stepSize, retVal);
        return retVal;
    }

    MatrixStore<Double> getCovariances() {
        return myDelegate.getC22().reconstruct();
    }

    @Override
    double getExpected(final double stepSize) {
        return this.getDistribution(stepSize).getExpected();
    }

    @Override
    double getLowerConfidenceQuantile(final double stepSize, final double confidence) {
        return this.getDistribution(stepSize).getLowerConfidenceQuantile(confidence);
    }

    @Override
    double getNormalisedRandomIncrement() {
        return GENERATOR.doubleValue();
    }

    @Override
    double getStandardDeviation(final double stepSize) {
        return this.getDistribution(stepSize).getStandardDeviation();
    }

    @Override
    double getUpperConfidenceQuantile(final double stepSize, final double confidence) {
        return this.getDistribution(stepSize).getUpperConfidenceQuantile(confidence);
    }

    @Override
    double getVariance(final double stepSize) {
        return this.getDistribution(stepSize).getVariance();
    }

}

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

import java.util.concurrent.ThreadLocalRandom;

import org.ojalgo.function.special.ErrorFunction;
import org.ojalgo.random.Normal;
import org.ojalgo.random.scedasticity.ARCH;
import org.ojalgo.random.scedasticity.GARCH;
import org.ojalgo.random.scedasticity.ScedasticityModel;
import org.ojalgo.structure.Access1D;

/**
 * Process with fixed mean and (possibly) fluctuating variance given by a {@link ScedasticityModel}.
 *
 * @author apete
 */
public final class StationaryNormalProcess extends SingleValueBasedProcess<Normal> implements Process1D.ComponentProcess<Normal> {

    public static StationaryNormalProcess estimateARCH(final Access1D<?> series, final int q) {
        return new StationaryNormalProcess(ARCH.estimate(series, q));
    }

    public static StationaryNormalProcess estimateGARCH(final Access1D<?> series, final int p, final int q) {
        return new StationaryNormalProcess(GARCH.estimate(series, p, q));
    }

    public static StationaryNormalProcess of(final ScedasticityModel scedasticityModel) {
        return new StationaryNormalProcess(scedasticityModel);
    }

    private final ScedasticityModel myScedasticityModel;

    StationaryNormalProcess(final ScedasticityModel scedasticityModel) {
        super();
        myScedasticityModel = scedasticityModel;
    }

    public Normal getDistribution(final double evaluationPoint) {
        return Normal.of(this.getExpected(evaluationPoint), this.getStandardDeviation(evaluationPoint));
    }

    public double getValue() {
        return this.getCurrentValue();
    }

    public void setValue(final double newValue) {
        this.setCurrentValue(newValue);
        myScedasticityModel.update(newValue);
    }

    public double step() {
        return this.step(ONE);
    }

    public double step(final double stepSize, final double standardGaussianInnovation) {
        return this.doStep(stepSize, standardGaussianInnovation);
    }

    @Override
    double doStep(final double stepSize, final double normalisedRandomIncrement) {

        double stdDev = this.getStandardDeviation(stepSize);

        double error = stdDev * normalisedRandomIncrement;

        double retVal = this.getExpected(stepSize) + error;

        this.setValue(retVal);

        return retVal;
    }

    @Override
    double getExpected(final double stepSize) {
        return myScedasticityModel.getMean();
    }

    @Override
    double getLowerConfidenceQuantile(final double stepSize, final double confidence) {
        return this.getExpected(stepSize) - (this.getStandardDeviation(stepSize) * SQRT_TWO * ErrorFunction.erfi(confidence));
    }

    @Override
    double getNormalisedRandomIncrement() {
        return ThreadLocalRandom.current().nextGaussian();
    }

    @Override
    double getStandardDeviation(final double stepSize) {
        return SQRT.invoke(this.getVariance(stepSize));
    }

    @Override
    double getUpperConfidenceQuantile(final double stepSize, final double confidence) {
        return this.getExpected(stepSize) + (this.getStandardDeviation(stepSize) * SQRT_TWO * ErrorFunction.erfi(confidence));
    }

    @Override
    double getVariance(final double stepSize) {
        return stepSize * myScedasticityModel.getVariance();
    }

}

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

import org.ojalgo.random.Normal;
import org.ojalgo.random.RandomUtils;

public final class WienerProcess extends AbstractProcess<Normal> {

    private static final Normal GENERATOR = new Normal();

    public WienerProcess() {

        super();

        this.setValue(ZERO);
    }

    @SuppressWarnings("unused")
    private WienerProcess(final double initialValue) {

        super();

        this.setValue(initialValue);
    }

    public Normal getDistribution(final double evaluationPoint) {
        return new Normal(this.getValue(), Math.sqrt(evaluationPoint));
    }

    @Override
    protected double getNormalisedRandomIncrement() {
        return GENERATOR.doubleValue();
    }

    @Override
    protected double step(final double currentValue, final double stepSize, final double normalisedRandomIncrement) {
        final double retVal = currentValue + (Math.sqrt(stepSize) * normalisedRandomIncrement);
        this.setValue(retVal);
        return retVal;
    }

    @Override
    double getExpected(final double aStepSize) {
        return this.getValue();
    }

    @Override
    double getLowerConfidenceQuantile(final double aStepSize, final double aConfidence) {
        return this.getValue() - (Math.sqrt(aStepSize) * SQRT_TWO * RandomUtils.erfi(aConfidence));
    }

    @Override
    double getStandardDeviation(final double aStepSize) {
        return Math.sqrt(aStepSize);
    }

    @Override
    double getUpperConfidenceQuantile(final double aStepSize, final double aConfidence) {
        return this.getValue() + (Math.sqrt(aStepSize) * SQRT_TWO * RandomUtils.erfi(aConfidence));
    }

    @Override
    double getVariance(final double aStepSize) {
        return aStepSize;
    }

}

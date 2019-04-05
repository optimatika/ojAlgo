/*
 * Copyright 1997-2019 Optimatika
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

import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.function.special.ErrorFunction;
import org.ojalgo.random.Normal;

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
        return new Normal(this.getValue(), PrimitiveFunction.SQRT.invoke(evaluationPoint));
    }

    @Override
    protected double getNormalisedRandomIncrement() {
        return GENERATOR.doubleValue();
    }

    @Override
    protected double step(final double currentValue, final double stepSize, final double normalisedRandomIncrement) {
        final double retVal = currentValue + (PrimitiveFunction.SQRT.invoke(stepSize) * normalisedRandomIncrement);
        this.setValue(retVal);
        return retVal;
    }

    @Override
    double getExpected(final double stepSize) {
        return this.getValue();
    }

    @Override
    double getLowerConfidenceQuantile(final double stepSize, final double confidence) {
        return this.getValue() - (PrimitiveFunction.SQRT.invoke(stepSize) * SQRT_TWO * ErrorFunction.erfi(confidence));
    }

    @Override
    double getStandardDeviation(final double stepSize) {
        return PrimitiveFunction.SQRT.invoke(stepSize);
    }

    @Override
    double getUpperConfidenceQuantile(final double stepSize, final double confidence) {
        return this.getValue() + (PrimitiveFunction.SQRT.invoke(stepSize) * SQRT_TWO * ErrorFunction.erfi(confidence));
    }

    @Override
    double getVariance(final double stepSize) {
        return stepSize;
    }

}

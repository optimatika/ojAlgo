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

import static org.ojalgo.function.constant.PrimitiveMath.ONE;

import org.ojalgo.random.ContinuousDistribution;
import org.ojalgo.random.Distribution;

abstract class AbstractProcess<D extends Distribution> implements RandomProcess<D> {

    AbstractProcess() {
        super();
    }

    /**
     * Equivalent to calling {@link RandomProcess#getDistribution(double)} with argumant <code>1.0</code>, and
     * then {@link Distribution#getExpected()}.
     */
    public final double getExpected() {
        return this.getExpected(ONE);
    }

    /**
     * The same thing can be achieved by first calling {@link RandomProcess#getDistribution(double)} with
     * argumant <code>1.0</code>, and then {@link ContinuousDistribution#getQuantile(double)} (but with
     * different input argument).
     */
    public final double getLowerConfidenceQuantile(final double confidence) {
        return this.getLowerConfidenceQuantile(ONE, confidence);
    }

    /**
     * Equivalent to calling {@link RandomProcess#getDistribution(double)} with argumant <code>1.0</code>, and
     * then {@link Distribution#getStandardDeviation()}.
     */
    public final double getStandardDeviation() {
        return this.getStandardDeviation(ONE);
    }

    /**
     * The same thing can be achieved by first calling {@link RandomProcess#getDistribution(double)} with
     * argumant <code>1.0</code>, and then {@link ContinuousDistribution#getQuantile(double)} (but with
     * different input argument).
     */
    public final double getUpperConfidenceQuantile(final double confidence) {
        return this.getUpperConfidenceQuantile(ONE, confidence);
    }

    /**
     * Equivalent to calling {@link RandomProcess#getDistribution(double)} with argumant <code>1.0</code>, and
     * then {@link Distribution#getVariance()}.
     */
    public final double getVariance() {
        return this.getVariance(ONE);
    }

    abstract double doStep(double stepSize, double normalisedRandomIncrement);

    abstract double getCurrentValue();

    abstract double getExpected(double stepSize);

    abstract double getLowerConfidenceQuantile(double stepSize, double confidence);

    abstract double getNormalisedRandomIncrement();

    abstract double getStandardDeviation(double stepSize);

    abstract double getUpperConfidenceQuantile(double stepSize, double confidence);

    abstract double getVariance(double stepSize);

    abstract void setCurrentValue(final double currentValue);

    final double step(final double stepSize) {
        return this.doStep(stepSize, this.getNormalisedRandomIncrement());
    }

}

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
package org.ojalgo.random;

import static org.ojalgo.function.constant.PrimitiveMath.*;

import org.ojalgo.array.Array1D;
import org.ojalgo.structure.Access1D;

/**
 * A continuous distribution in which the logarithm of a variable has a normal distribution. A log normal
 * distribution results if the variable is the product of a large number of independent,
 * identically-distributed variables in the same way that a normal distribution results if the variable is the
 * sum of a large number of independent, identically-distributed variables.
 *
 * @author apete
 */
public class LogNormal extends AbstractContinuous {

    public static LogNormal estimate(final Access1D<?> rawSamples) {

        final int size = rawSamples.size();

        final Array1D<Double> logSamples = Array1D.R064.make(size);

        for (int i = 0; i < size; i++) {
            logSamples.set(i, LOG.invoke(rawSamples.doubleValue(i)));
        }

        final SampleSet sampleSet = SampleSet.wrap(logSamples);

        return new LogNormal(sampleSet.getMean(), sampleSet.getStandardDeviation());
    }

    public static LogNormal make(final double mean, final double variance) {

        final double tmpVar = LOG1P.invoke(variance / (mean * mean));

        final double location = LOG.invoke(mean) - (HALF * tmpVar);
        final double scale = SQRT.invoke(tmpVar);

        return new LogNormal(location, scale);
    }

    private final Normal myNormal;

    public LogNormal() {
        this(ZERO, ONE);
    }

    /**
     * The location and scale parameters are the mean and standard deviation of the variable's logarithm (by
     * definition, the variable's logarithm is normally distributed).
     */
    public LogNormal(final double location, final double scale) {

        super();

        myNormal = new Normal(location, scale);
    }

    public double getDensity(final double value) {
        return myNormal.getDensity(LOG.invoke(value)) / value;
    }

    public double getDistribution(final double value) {
        return myNormal.getDistribution(LOG.invoke(value));
    }

    public double getExpected() {
        return EXP.invoke(myNormal.getExpected() + (myNormal.getVariance() * HALF));
    }

    /**
     * The geometric mean is also the median
     */
    public double getGeometricMean() {
        return EXP.invoke(myNormal.getExpected());
    }

    public double getGeometricStandardDeviation() {
        return EXP.invoke(myNormal.getStandardDeviation());
    }

    public double getQuantile(final double probability) {

        this.checkProbabilty(probability);

        return EXP.invoke(myNormal.getQuantile(probability));
    }

    @Override
    public double getVariance() {
        final double tmpVariance = myNormal.getVariance();
        return EXPM1.invoke(tmpVariance) * EXP.invoke((TWO * myNormal.getExpected()) + tmpVariance);
    }

    @Override
    public void setSeed(final long seed) {
        myNormal.setSeed(seed);
    }

    @Override
    protected double generate() {
        return EXP.invoke(myNormal.generate());
    }

}

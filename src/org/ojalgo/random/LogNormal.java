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

import org.ojalgo.access.Access1D;
import org.ojalgo.array.Array1D;
import org.ojalgo.function.PrimitiveFunction;

/**
 * A continuous distribution in which the logarithm of a variable has a normal distribution. A log normal
 * distribution results if the variable is the product of a large number of independent,
 * identically-distributed variables in the same way that a normal distribution results if the variable is the
 * sum of a large number of independent, identically-distributed variables.
 *
 * @author apete
 */
public class LogNormal extends AbstractContinuous {

    private static final long serialVersionUID = 2175858399667617840L;

    public static LogNormal estimate(final Access1D<?> rawSamples) {

        final int tmpSize = (int) rawSamples.count();

        final Array1D<Double> tmpLogSamples = Array1D.PRIMITIVE64.makeZero(tmpSize);

        for (int i = 0; i < tmpSize; i++) {
            tmpLogSamples.set(i, PrimitiveFunction.LOG.invoke(rawSamples.doubleValue(i)));
        }

        final SampleSet tmpSampleSet = SampleSet.wrap(tmpLogSamples);

        return new LogNormal(tmpSampleSet.getMean(), tmpSampleSet.getStandardDeviation());
    }

    public static LogNormal make(final double aExpected, final double aVariance) {

        final double tmpVar = PrimitiveFunction.LOG1P.invoke(aVariance / (aExpected * aExpected));

        final double tmpMean = PrimitiveFunction.LOG.invoke(aExpected) - (HALF * tmpVar);
        final double tmpStdDev = PrimitiveFunction.SQRT.invoke(tmpVar);

        return new LogNormal(tmpMean, tmpStdDev);
    }

    private final Normal myNormal;

    public LogNormal() {
        this(ZERO, ONE);
    }

    /**
     * The aMean and aStdDev parameters are the mean and standard deviation of the variable's logarithm (by
     * definition, the variable's logarithm is normally distributed).
     */
    public LogNormal(final double aMean, final double aStdDev) {

        super();

        myNormal = new Normal(aMean, aStdDev);
    }

    public double getDistribution(final double value) {
        return myNormal.getDistribution(PrimitiveFunction.LOG.invoke(value));
    }

    public double getExpected() {
        return PrimitiveFunction.EXP.invoke(myNormal.getExpected() + (myNormal.getVariance() * HALF));
    }

    /**
     * The geometric mean is also the median
     */
    public double getGeometricMean() {
        return PrimitiveFunction.EXP.invoke(myNormal.getExpected());
    }

    public double getGeometricStandardDeviation() {
        return PrimitiveFunction.EXP.invoke(myNormal.getStandardDeviation());
    }

    public double getProbability(final double value) {
        return myNormal.getProbability(PrimitiveFunction.LOG.invoke(value)) / value;
    }

    public double getQuantile(final double probality) {

        this.checkProbabilty(probality);

        return PrimitiveFunction.EXP.invoke(myNormal.getQuantile(probality));
    }

    @Override
    public double getVariance() {
        final double tmpVariance = myNormal.getVariance();
        return PrimitiveFunction.EXPM1.invoke(tmpVariance) * PrimitiveFunction.EXP.invoke((TWO * myNormal.getExpected()) + tmpVariance);
    }

    @Override
    public void setSeed(final long seed) {
        myNormal.setSeed(seed);
    }

    @Override
    protected double generate() {
        return PrimitiveFunction.EXP.invoke(myNormal.generate());
    }

}

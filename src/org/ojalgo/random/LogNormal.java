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
package org.ojalgo.random;

import static org.ojalgo.constant.PrimitiveMath.*;

import org.ojalgo.access.Access1D;
import org.ojalgo.array.Array1D;

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

        final Array1D<Double> tmpLogSamples = Array1D.PRIMITIVE.makeZero(tmpSize);

        for (int i = 0; i < tmpSize; i++) {
            tmpLogSamples.set(i, Math.log(rawSamples.doubleValue(i)));
        }

        final SampleSet tmpSampleSet = SampleSet.wrap(tmpLogSamples);

        return new LogNormal(tmpSampleSet.getMean(), tmpSampleSet.getStandardDeviation());
    }

    public static LogNormal make(final double aExpected, final double aVariance) {

        final double tmpVar = Math.log1p(aVariance / (aExpected * aExpected));

        final double tmpMean = Math.log(aExpected) - (HALF * tmpVar);
        final double tmpStdDev = Math.sqrt(tmpVar);

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

    public double getDistribution(final double aValue) {
        return myNormal.getDistribution(Math.log(aValue));
    }

    public double getExpected() {
        return Math.exp(myNormal.getExpected() + (myNormal.getVariance() * HALF));
    }

    /**
     * The geometric mean is also the median
     */
    public double getGeometricMean() {
        return Math.exp(myNormal.getExpected());
    }

    public double getGeometricStandardDeviation() {
        return Math.exp(myNormal.getStandardDeviation());
    }

    public double getProbability(final double aValue) {
        return myNormal.getProbability(Math.log(aValue)) / aValue;
    }

    public double getQuantile(final double aProbality) {

        this.checkProbabilty(aProbality);

        return Math.exp(myNormal.getQuantile(aProbality));
    }

    @Override
    public double getVariance() {
        final double tmpVariance = myNormal.getVariance();
        return Math.expm1(tmpVariance) * Math.exp((TWO * myNormal.getExpected()) + tmpVariance);
    }

    @Override
    protected double generate() {
        return Math.exp(myNormal.generate());
    }

}

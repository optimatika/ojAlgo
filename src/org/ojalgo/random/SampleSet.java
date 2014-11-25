/*
 * Copyright 1997-2014 Optimatika (www.optimatika.se)
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

import java.util.Arrays;

import org.ojalgo.ProgrammingError;
import org.ojalgo.access.Access1D;
import org.ojalgo.array.ArrayUtils;
import org.ojalgo.array.PrimitiveArray;
import org.ojalgo.constant.PrimitiveMath;

public final class SampleSet implements Access1D<Double> {

    public static SampleSet make(final RandomNumber aRndmNmbr, final int aSize) {

        final PrimitiveArray retVal = PrimitiveArray.make(aSize);

        for (int i = 0; i < aSize; i++) {
            retVal.data[i] = aRndmNmbr.doubleValue();
        }

        return new SampleSet(retVal);
    }

    public static SampleSet wrap(final Access1D<?> someSamples) {
        return new SampleSet(someSamples);
    }

    private transient double myMean = Double.NaN;
    private transient double myMedian = Double.NaN;
    private final Access1D<?> mySamples;
    private transient double myVariance = Double.NaN;

    @SuppressWarnings("unused")
    private SampleSet() {

        this(null);

        ProgrammingError.throwForIllegalInvocation();
    }

    SampleSet(final Access1D<?> someValues) {

        super();

        mySamples = someValues;

        this.reset();
    }

    public long count() {
        return mySamples.count();
    }

    public double doubleValue(final long index) {
        return mySamples.doubleValue(index);
    }

    public Double get(final int index) {
        return mySamples.doubleValue(index);
    }

    public Double get(final long index) {
        return mySamples.doubleValue(index);
    }

    public double getCorrelation(final SampleSet aSet) {

        double retVal = PrimitiveMath.ZERO;

        final double tmpCovar = this.getCovariance(aSet);

        if (tmpCovar != PrimitiveMath.ZERO) {

            final double tmpThisStdDev = this.getStandardDeviation();
            final double tmpThatStdDev = aSet.getStandardDeviation();

            retVal = tmpCovar / (tmpThisStdDev * tmpThatStdDev);
        }

        return retVal;
    }

    public double getCovariance(final SampleSet aSet) {

        double retVal = PrimitiveMath.ZERO;

        final double tmpThisMean = this.getMean();
        final double tmpThatMean = aSet.getMean();

        final int tmpCount = (int) Math.min(mySamples.count(), aSet.count());

        final Access1D<?> tmpValues = aSet.getSamples();

        for (int i = 0; i < tmpCount; i++) {
            retVal += (mySamples.doubleValue(i) - tmpThisMean) * (tmpValues.doubleValue(i) - tmpThatMean);
        }

        retVal /= (tmpCount - 1);

        return retVal;
    }

    public double getFirst() {
        return mySamples.doubleValue(0);
    }

    /**
     * max(abs(value))
     */
    public double getLargest() {

        double retVal = PrimitiveMath.ZERO;

        for (int i = 0; i < mySamples.count(); i++) {
            retVal = Math.max(retVal, Math.abs(mySamples.doubleValue(i)));
        }

        return retVal;
    }

    public double getLast() {
        return mySamples.doubleValue(mySamples.count() - 1);
    }

    /**
     * max(value)
     */
    public double getMaximum() {

        double retVal = PrimitiveMath.NEGATIVE_INFINITY;

        for (int i = 0; i < mySamples.count(); i++) {
            retVal = Math.max(retVal, mySamples.doubleValue(i));
        }

        return retVal;
    }

    public double getMean() {

        if (Double.isNaN(myMean)) {

            myMean = PrimitiveMath.ZERO;

            for (int i = 0; i < mySamples.count(); i++) {
                myMean += mySamples.doubleValue(i);
            }

            myMean /= mySamples.count();
        }

        return myMean;
    }

    public double getMedian() {

        if (Double.isNaN(myMedian)) {

            final double[] tmpCopy = ArrayUtils.toRawCopyOf(mySamples);

            Arrays.sort(tmpCopy);

            myMedian = tmpCopy[(int) (mySamples.count() / 2)];
        }

        return myMedian;
    }

    /**
     * min(value)
     */
    public double getMinimum() {

        double retVal = PrimitiveMath.POSITIVE_INFINITY;

        for (int i = 0; i < mySamples.count(); i++) {
            retVal = Math.min(retVal, mySamples.doubleValue(i));
        }

        return retVal;
    }

    /**
     * min(abs(value))
     */
    public double getSmallest() {

        double retVal = PrimitiveMath.POSITIVE_INFINITY;

        for (int i = 0; i < mySamples.count(); i++) {
            retVal = Math.min(retVal, Math.abs(mySamples.doubleValue(i)));
        }

        return retVal;
    }

    public double getStandardDeviation() {
        return Math.sqrt(this.getVariance());
    }

    /**
     * <p>
     * &quot;Sum of squares is a concept that permeates much of inferential statistics and descriptive statistics. More
     * properly, it is "the sum of the squared deviations". Mathematically, it is an unscaled, or unadjusted measure of
     * dispersion (also called variability). When scaled for the number of degrees of freedom, it estimates the
     * variance, or spread of the observations about their mean value.&quot;
     * </p>
     * <a href="http://en.wikipedia.org/wiki/Sum_of_squares">Wikipedia</a>
     */
    public double getSumOfSquares() {

        double retVal = PrimitiveMath.ZERO;

        final double tmpMean = this.getMean();
        double tmpVal;
        final int tmpLimit = (int) mySamples.count();
        for (int i = 0; i < tmpLimit; i++) {
            tmpVal = mySamples.doubleValue(i) - tmpMean;
            retVal += (tmpVal * tmpVal);
        }

        return retVal;
    }

    /**
     * @return A copy of the internal data (the samples).
     */
    public double[] getValues() {
        return ArrayUtils.toRawCopyOf(mySamples);
    }

    public double getVariance() {

        if (Double.isNaN(myVariance)) {
            myVariance = this.getCovariance(this);
        }

        return myVariance;
    }

    public void reset() {
        myMean = Double.NaN;
        myMedian = Double.NaN;
        myVariance = Double.NaN;
    }

    public int size() {
        return (int) mySamples.count();
    }

    @Override
    public String toString() {
        return "Sample set size: " + this.count() + ", Mean: " + this.getMean() + ", Median: " + this.getMedian() + ", Variance: " + this.getVariance()
                + ", Standard Deviation: " + this.getStandardDeviation() + ", Minimum: " + this.getMinimum() + ", Maximum: " + this.getMaximum();
    }

    Access1D<?> getSamples() {
        return mySamples;
    }

}

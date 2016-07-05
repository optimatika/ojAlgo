/*
 * Copyright 1997-2016 Optimatika (www.optimatika.se)
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
import org.ojalgo.array.PrimitiveArray;
import org.ojalgo.constant.PrimitiveMath;

public final class SampleSet implements Access1D<Double> {

    public static SampleSet make(final RandomNumber randomNumber, final int size) {

        final PrimitiveArray retVal = PrimitiveArray.make(size);

        for (int i = 0; i < size; i++) {
            retVal.data[i] = randomNumber.doubleValue();
        }

        return new SampleSet(retVal);
    }

    public static SampleSet wrap(final Access1D<?> someSamples) {
        return new SampleSet(someSamples);
    }

    private transient double myMean = Double.NaN;
    private transient double myMedian = Double.NaN;
    private Access1D<?> mySamples;
    private transient double myVariance = Double.NaN;

    @SuppressWarnings("unused")
    private SampleSet() {

        this(null);

        ProgrammingError.throwForIllegalInvocation();
    }

    SampleSet(final Access1D<?> samples) {

        super();

        mySamples = samples;

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

    public double getCorrelation(final SampleSet anotherSampleSet) {

        double retVal = PrimitiveMath.ZERO;

        final double tmpCovar = this.getCovariance(anotherSampleSet);

        if (tmpCovar != PrimitiveMath.ZERO) {

            final double tmpThisStdDev = this.getStandardDeviation();
            final double tmpThatStdDev = anotherSampleSet.getStandardDeviation();

            retVal = tmpCovar / (tmpThisStdDev * tmpThatStdDev);
        }

        return retVal;
    }

    public double getCovariance(final SampleSet anotherSampleSet) {

        double retVal = PrimitiveMath.ZERO;

        final double tmpThisMean = this.getMean();
        final double tmpThatMean = anotherSampleSet.getMean();

        final int tmpCount = (int) Math.min(mySamples.count(), anotherSampleSet.count());

        final Access1D<?> tmpValues = anotherSampleSet.getSamples();

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
        return mySamples.doubleValue(mySamples.count() - 1L);
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

    /**
     * Potentially expensive as it requires copying and sorting of the samples.
     */
    public double getMedian() {

        if (Double.isNaN(myMedian)) {

            final double[] tmpCopy = mySamples.toRawCopy1D();

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
     * The standard score is the (signed) number of standard deviations an observation or datum is above the
     * mean. Thus, a positive standard score indicates a datum above the mean, while a negative standard score
     * indicates a datum below the mean. It is a dimensionless quantity obtained by subtracting the population
     * mean from an individual raw score and then dividing the difference by the population standard
     * deviation.
     *
     * @see <a href="https://en.wikipedia.org/wiki/Standard_score">WikipediA</a>
     */
    public double getStandardScore(final long index) {
        return (this.doubleValue(index) - this.getMean()) / this.getStandardDeviation();
    }

    /**
     * Sum of squares is a concept that permeates much of inferential statistics and descriptive statistics.
     * More properly, it is "the sum of the squared deviations". Mathematically, it is an unscaled, or
     * unadjusted measure of dispersion (also called variability). When scaled for the number of degrees of
     * freedom, it estimates the variance, or spread of the observations about their mean value.
     *
     * @see <a href="http://en.wikipedia.org/wiki/Sum_of_squares">WikipediA</a>
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
        return mySamples.toRawCopy1D();
    }

    public double getVariance() {

        if (Double.isNaN(myVariance)) {
            myVariance = this.getCovariance(this);
        }

        return myVariance;
    }

    /**
     * If the underlying {@link Access1D} of samples is modified you must reset the sample set before using.
     */
    public void reset() {
        myMean = Double.NaN;
        myMedian = Double.NaN;
        myVariance = Double.NaN;
    }

    public int size() {
        return (int) mySamples.count();
    }

    /**
     * Replace the underlying samples and reset the sample set.
     */
    public void swap(final SampleSet samples) {
        mySamples = samples;
        this.reset();
    }

    @Override
    public String toString() {
        return "Sample set Size=" + this.count() + ", Mean=" + this.getMean() + ", Median=" + this.getMedian() + ", Var=" + this.getVariance() + ", StdDev="
                + this.getStandardDeviation() + ", Min=" + this.getMinimum() + ", Max=" + this.getMaximum();
    }

    Access1D<?> getSamples() {
        return mySamples;
    }

}

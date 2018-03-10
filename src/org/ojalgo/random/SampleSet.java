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
import static org.ojalgo.function.PrimitiveFunction.*;

import java.util.Arrays;

import org.ojalgo.ProgrammingError;
import org.ojalgo.access.Access1D;
import org.ojalgo.array.Primitive64Array;
import org.ojalgo.type.context.NumberContext;

public final class SampleSet implements Access1D<Double> {

    public static SampleSet make() {
        return new SampleSet(Primitive64Array.make(4));
    }

    public static SampleSet make(final RandomNumber randomNumber, final int size) {

        final Primitive64Array retVal = Primitive64Array.make(size);
        final double[] tmpData = retVal.data;

        for (int i = 0; i < size; i++) {
            tmpData[i] = randomNumber.doubleValue();
        }

        return new SampleSet(retVal);
    }

    public static SampleSet wrap(final Access1D<?> someSamples) {
        return new SampleSet(someSamples);
    }

    private transient double myMax = NaN;
    private transient double myMean = NaN;
    private transient double myMin = NaN;
    private transient double myQuartile1 = NaN;
    private transient double myQuartile2 = NaN;
    private transient double myQuartile3 = NaN;
    private Access1D<?> mySamples;
    private transient double[] mySortedCopy = null;
    private transient double myVariance = NaN;

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

    public Double get(final long index) {
        return mySamples.doubleValue(index);
    }

    public double getCorrelation(final SampleSet anotherSampleSet) {

        double retVal = ZERO;

        final double tmpCovar = this.getCovariance(anotherSampleSet);

        // if (tmpCovar != ZERO) {
        if (NumberContext.compare(tmpCovar, ZERO) != 0) {

            final double tmpThisStdDev = this.getStandardDeviation();
            final double tmpThatStdDev = anotherSampleSet.getStandardDeviation();

            retVal = tmpCovar / (tmpThisStdDev * tmpThatStdDev);
        }

        return retVal;
    }

    public double getCovariance(final SampleSet anotherSampleSet) {

        double retVal = ZERO;

        final double tmpThisMean = this.getMean();
        final double tmpThatMean = anotherSampleSet.getMean();

        final long tmpLimit = Math.min(mySamples.count(), anotherSampleSet.count());

        final Access1D<?> tmpValues = anotherSampleSet.getSamples();

        for (long i = 0L; i < tmpLimit; i++) {
            retVal += (mySamples.doubleValue(i) - tmpThisMean) * (tmpValues.doubleValue(i) - tmpThatMean);
        }

        retVal /= (tmpLimit - 1L);

        return retVal;
    }

    public double getFirst() {
        if (mySamples.count() > 0L) {
            return mySamples.doubleValue(0);
        } else {
            return ZERO;
        }
    }

    public double getInterquartileRange() {
        return this.getQuartile3() - this.getQuartile1();
    }

    /**
     * max(abs(value))
     */
    public double getLargest() {

        double retVal = ZERO;

        final long tmpLimit = mySamples.count();
        for (long i = 0L; i < tmpLimit; i++) {
            retVal = MAX.invoke(retVal, ABS.invoke(mySamples.doubleValue(i)));
        }

        return retVal;
    }

    public double getLast() {
        if (mySamples.count() > 0L) {
            return mySamples.doubleValue(mySamples.count() - 1L);
        } else {
            return ZERO;
        }
    }

    /**
     * max(value)
     */
    public double getMaximum() {

        if (Double.isNaN(myMax)) {

            myMax = NEGATIVE_INFINITY;

            final long tmpLimit = mySamples.count();
            for (long i = 0L; i < tmpLimit; i++) {
                myMax = MAX.invoke(myMax, mySamples.doubleValue(i));
            }
        }

        return myMax;
    }

    public double getMean() {

        if (Double.isNaN(myMean)) {

            myMean = ZERO;

            final long tmpLimit = mySamples.count();
            for (long i = 0L; i < tmpLimit; i++) {
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
        return this.getQuartile2();
    }

    /**
     * min(value)
     */
    public double getMinimum() {

        if (Double.isNaN(myMin)) {

            myMin = POSITIVE_INFINITY;

            final long tmpLimit = mySamples.count();
            for (long i = 0L; i < tmpLimit; i++) {
                myMin = MIN.invoke(myMin, mySamples.doubleValue(i));
            }
        }

        return myMin;
    }

    /**
     * Potentially expensive as it requires copying and sorting of the samples.
     */
    public double getQuartile1() {

        if (Double.isNaN(myQuartile1)) {
            this.calculateQuartiles();
        }

        return myQuartile1;
    }

    /**
     * Potentially expensive as it requires copying and sorting of the samples.
     */
    public double getQuartile2() {

        if (Double.isNaN(myQuartile2)) {
            this.calculateQuartiles();
        }

        return myQuartile2;
    }

    /**
     * Potentially expensive as it requires copying and sorting of the samples.
     */
    public double getQuartile3() {

        if (Double.isNaN(myQuartile3)) {
            this.calculateQuartiles();
        }

        return myQuartile3;
    }

    /**
     * min(abs(value))
     */
    public double getSmallest() {

        double retVal = POSITIVE_INFINITY;

        final long tmpLimit = mySamples.count();
        for (long i = 0L; i < tmpLimit; i++) {
            retVal = MIN.invoke(retVal, ABS.invoke(mySamples.doubleValue(i)));
        }

        return retVal;
    }

    public double getStandardDeviation() {
        return SQRT.invoke(this.getVariance());
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

        double retVal = ZERO;

        final double tmpMean = this.getMean();
        double tmpVal;
        final long tmpLimit = mySamples.count();
        for (long i = 0L; i < tmpLimit; i++) {
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

        myMin = NaN;
        myMax = NaN;

        myMean = NaN;
        myVariance = NaN;

        myQuartile1 = NaN;
        myQuartile2 = NaN;
        myQuartile3 = NaN;

        if (mySortedCopy != null) {
            Arrays.fill(mySortedCopy, Double.POSITIVE_INFINITY);
        }
    }

    public int size() {
        return (int) mySamples.count();
    }

    /**
     * Replace the underlying samples and reset the sample set.
     */
    public void swap(final Access1D<?> samples) {
        ProgrammingError.throwIfNull(samples);
        mySamples = samples;
        this.reset();
    }

    @Override
    public String toString() {
        return "Sample set Size=" + this.count() + ", Mean=" + this.getMean() + ", Var=" + this.getVariance() + ", StdDev=" + this.getStandardDeviation()
                + ", Min=" + this.getMinimum() + ", Max=" + this.getMaximum();
    }

    private void calculateQuartiles() {

        final int tmpSize = (int) this.getSamples().count();
        final double[] tmpSortedCopy = this.getSortedCopy();

        switch (tmpSize) {

        case 0:

            myMin = ZERO;
            myMax = ZERO;

            myQuartile1 = ZERO;
            myQuartile2 = ZERO;
            myQuartile3 = ZERO;

            break;

        case 1:

            myMin = tmpSortedCopy[0];
            myMax = tmpSortedCopy[0];

            myQuartile1 = tmpSortedCopy[0];
            myQuartile2 = tmpSortedCopy[0];
            myQuartile3 = tmpSortedCopy[0];

            break;

        default:

            myMin = tmpSortedCopy[0];
            myMax = tmpSortedCopy[tmpSize - 1];

            final int n = tmpSize / 4;
            final int r = tmpSize % 4;

            switch (r) {

            case 1:

                myQuartile1 = (0.25 * tmpSortedCopy[n - 1]) + (0.75 * tmpSortedCopy[n]);
                myQuartile2 = tmpSortedCopy[2 * n];
                myQuartile3 = (0.75 * tmpSortedCopy[3 * n]) + (0.25 * tmpSortedCopy[(3 * n) + 1]);

                break;

            case 2:

                myQuartile1 = tmpSortedCopy[n];
                myQuartile2 = (0.5 * tmpSortedCopy[2 * n]) + (0.5 * tmpSortedCopy[(2 * n) + 1]);
                myQuartile3 = tmpSortedCopy[(3 * n) + 1];

                break;

            case 3:

                myQuartile1 = (0.75 * tmpSortedCopy[n]) + (0.25 * tmpSortedCopy[n + 1]);
                myQuartile2 = tmpSortedCopy[(2 * n) + 1];
                myQuartile3 = (0.25 * tmpSortedCopy[(3 * n) + 1]) + (0.75 * tmpSortedCopy[(3 * n) + 2]);

                break;

            default:

                myQuartile1 = (0.5 * tmpSortedCopy[n - 1]) + (0.5 * tmpSortedCopy[n]);
                myQuartile2 = (0.5 * tmpSortedCopy[(2 * n) - 1]) + (0.5 * tmpSortedCopy[2 * n]);
                myQuartile3 = (0.5 * tmpSortedCopy[(3 * n) - 1]) + (0.5 * tmpSortedCopy[3 * n]);

                break;
            }

            break;
        }
    }

    Access1D<?> getSamples() {
        return mySamples;
    }

    double[] getSortedCopy() {

        final Access1D<?> tmpSamples = this.getSamples();
        final int tmpSamplesCount = (int) tmpSamples.count();

        if ((mySortedCopy == null) || (mySortedCopy.length < tmpSamplesCount) || (mySortedCopy.length == 0)) {
            mySortedCopy = tmpSamples.toRawCopy1D();
            Arrays.parallelSort(mySortedCopy);
        } else if (mySortedCopy[0] == Double.POSITIVE_INFINITY) {
            for (int i = 0; i < tmpSamplesCount; i++) {
                mySortedCopy[i] = tmpSamples.doubleValue(i);
            }
            Arrays.parallelSort(mySortedCopy, 0, tmpSamplesCount);
        }

        return mySortedCopy;
    }

}

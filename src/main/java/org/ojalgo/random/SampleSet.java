/*
 * Copyright 1997-2025 Optimatika
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

import java.util.Arrays;
import java.util.stream.Collector;

import org.ojalgo.ProgrammingError;
import org.ojalgo.array.ArrayR064;
import org.ojalgo.array.DenseArray;
import org.ojalgo.array.NumberList;
import org.ojalgo.array.NumberList.ListFactory;
import org.ojalgo.array.operation.FillMatchingSingle;
import org.ojalgo.structure.Access1D;
import org.ojalgo.type.context.NumberContext;
import org.ojalgo.type.function.TwoStepMapper;

public final class SampleSet implements Access1D<Double> {

    public static final class CombineableSet<N extends Comparable<N>> implements TwoStepMapper.Combineable<N, SampleSet, SampleSet.CombineableSet<N>> {

        private final ListFactory<N> myFactory;
        private final NumberList<N> myNumbers;

        CombineableSet(final ListFactory<N> factory) {
            super();
            myFactory = factory;
            myNumbers = myFactory.make();
        }

        @Override
        public void combine(final CombineableSet<N> other) {
            myNumbers.addAll(other.getNumbers());
        }

        public void consume(final double item) {
            myNumbers.add(item);
        }

        @Override
        public void consume(final N item) {
            myNumbers.add(item);
        }

        @Override
        public SampleSet getResults() {
            return SampleSet.wrap(myNumbers);
        }

        public CombineableSet<N> newInstance() {
            return new CombineableSet<>(myFactory);
        }

        @Override
        public void reset() {
            myNumbers.reset();
        }

        NumberList<N> getNumbers() {
            return myNumbers;
        }

    }

    /**
     * @param sumOfValues The sum of all values in a sample set
     * @param sumOfSquaredValues The sum of all squared values, in a sample set
     * @param numberOfValues The number of values in the sample set
     * @return The sample set's variance
     */
    public static double calculateVariance(final double sumOfValues, final double sumOfSquaredValues, final int numberOfValues) {
        return (numberOfValues * sumOfSquaredValues - sumOfValues * sumOfValues) / (numberOfValues * (numberOfValues - 1));
    }

    /**
     * Create a sample set from counting occurrences of difference values in the iterable.
     */
    public static <T> SampleSet from(final Iterable<T> keys) {

        FrequencyMap<T> frequencies = new FrequencyMap<>();

        for (T key : keys) {
            frequencies.increment(key);
        }

        return frequencies.sample();
    }

    public static SampleSet make() {
        return new SampleSet(ArrayR064.make(4));
    }

    public static SampleSet make(final RandomNumber randomNumber, final int size) {

        ArrayR064 retVal = ArrayR064.make(size);
        double[] tmpData = retVal.data;

        for (int i = 0; i < size; i++) {
            tmpData[i] = randomNumber.doubleValue();
        }

        return new SampleSet(retVal);
    }

    public static Collector<Double, CombineableSet<Double>, SampleSet> newCollector() {
        return SampleSet.newCollector(ArrayR064.FACTORY);
    }

    public static <N extends Comparable<N>> Collector<N, CombineableSet<N>, SampleSet> newCollector(final DenseArray.Factory<N> factory) {
        return TwoStepMapper.Combineable.newCollector(() -> SampleSet.newCombineableSet(factory));
    }

    public static CombineableSet<Double> newCombineableSet() {
        return SampleSet.newCombineableSet(ArrayR064.FACTORY);
    }

    public static <N extends Comparable<N>> CombineableSet<N> newCombineableSet(final DenseArray.Factory<N> factory) {
        return new CombineableSet<>(NumberList.factory(factory));
    }

    public static SampleSet wrap(final Access1D<?> samples) {
        return new SampleSet(samples);
    }

    public static SampleSet wrap(final double... samples) {
        return SampleSet.wrap(ArrayR064.wrap(samples));
    }

    private transient double myMax = NaN;
    private transient double myMean = NaN;
    private transient double myMin = NaN;
    private transient double myQuartile1 = NaN;
    private transient double myQuartile2 = NaN;
    private transient double myQuartile3 = NaN;
    private Access1D<?> mySamples;
    private transient double[] mySortedCopy = null;
    private transient double myStandardDeviation = NaN;
    private transient double myVariance = NaN;

    SampleSet(final Access1D<?> samples) {

        super();

        mySamples = samples;

        this.reset();
    }

    @Override
    public long count() {
        return mySamples.count();
    }

    @Override
    public double doubleValue(final int index) {
        return mySamples.doubleValue(index);
    }

    @Override
    public Double get(final long index) {
        return Double.valueOf(mySamples.doubleValue(index));
    }

    public double getCorrelation(final SampleSet other) {

        double retVal = ZERO;

        double covar = this.getCovariance(other);

        if (NumberContext.compare(covar, ZERO) != 0) {

            double thisStdDev = this.getStandardDeviation();
            double thatStdDev = other.getStandardDeviation();

            retVal = covar / (thisStdDev * thatStdDev);
        }

        return retVal;
    }

    public double getCovariance(final SampleSet other) {

        double retVal = ZERO;

        double thisMean = this.getMean();
        double thatMean = other.getMean();

        Access1D<?> otherValues = other.getSamples();

        int nbSamples = Math.min(mySamples.size(), other.size());

        for (int i = 0; i < nbSamples; i++) {
            retVal += (mySamples.doubleValue(i) - thisMean) * (otherValues.doubleValue(i) - thatMean);
        }

        retVal /= nbSamples - 1;

        return retVal;
    }

    public double getFirst() {
        if (mySamples.size() > 0) {
            return mySamples.doubleValue(0);
        }
        return ZERO;
    }

    public double getInterquartileRange() {
        return this.getQuartile3() - this.getQuartile1();
    }

    /**
     * max(abs(value))
     */
    public double getLargest() {

        double retVal = ZERO;

        for (int i = 0, limit = mySamples.size(); i < limit; i++) {
            retVal = Math.max(retVal, Math.abs(mySamples.doubleValue(i)));
        }

        return retVal;
    }

    public double getLast() {
        int nbSamples = mySamples.size();
        if (nbSamples > 0) {
            return mySamples.doubleValue(nbSamples - 1);
        }
        return ZERO;
    }

    /**
     * max(value)
     */
    public double getMaximum() {

        if (Double.isNaN(myMax)) {

            myMax = NEGATIVE_INFINITY;

            for (int i = 0, limit = mySamples.size(); i < limit; i++) {
                myMax = Math.max(myMax, mySamples.doubleValue(i));
            }
        }

        return myMax;
    }

    public double getMean() {

        if (Double.isNaN(myMean)) {

            myMean = ZERO;

            int nbSamples = mySamples.size();

            for (int i = 0; i < nbSamples; i++) {
                myMean += mySamples.doubleValue(i);
            }

            myMean /= nbSamples;
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
     * The mean of the highest and lowest values. (Max + Min) / 2
     */
    public double getMidrange() {
        return (this.getMaximum() + this.getMinimum()) / TWO;
    }

    /**
     * min(value)
     */
    public double getMinimum() {

        if (Double.isNaN(myMin)) {

            myMin = POSITIVE_INFINITY;

            for (int i = 0, limit = mySamples.size(); i < limit; i++) {
                myMin = Math.min(myMin, mySamples.doubleValue(i));
            }
        }

        return myMin;
    }

    /**
     * https://en.wikipedia.org/wiki/Quartile
     * <p>
     * Potentially expensive as it requires copying and sorting of the samples.
     */
    public double getQuartile1() {

        if (Double.isNaN(myQuartile1)) {
            this.calculateQuartiles();
        }

        return myQuartile1;
    }

    /**
     * https://en.wikipedia.org/wiki/Quartile
     * <p>
     * Potentially expensive as it requires copying and sorting of the samples.
     */
    public double getQuartile2() {

        if (Double.isNaN(myQuartile2)) {
            this.calculateQuartiles();
        }

        return myQuartile2;
    }

    /**
     * https://en.wikipedia.org/wiki/Quartile
     * <p>
     * Potentially expensive as it requires copying and sorting of the samples.
     */
    public double getQuartile3() {

        if (Double.isNaN(myQuartile3)) {
            this.calculateQuartiles();
        }

        return myQuartile3;
    }

    /**
     * The difference between the highest and lowest values. Max - Min
     */
    public double getRange() {
        return this.getMaximum() - this.getMinimum();
    }

    /**
     * min(abs(value))
     */
    public double getSmallest() {

        double retVal = POSITIVE_INFINITY;

        for (int i = 0, limit = mySamples.size(); i < limit; i++) {
            retVal = Math.min(retVal, Math.abs(mySamples.doubleValue(i)));
        }

        return retVal;
    }

    public double getStandardDeviation() {

        if (Double.isNaN(myStandardDeviation)) {
            myStandardDeviation = Math.sqrt(this.getVariance());
        }

        return myStandardDeviation;
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
    public double getStandardScore(final int index) {
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

        double mean = this.getMean();
        double deviation;
        for (int i = 0, limit = mySamples.size(); i < limit; i++) {
            deviation = mySamples.doubleValue(i) - mean;
            retVal += deviation * deviation;
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
        myStandardDeviation = NaN;

        myQuartile1 = NaN;
        myQuartile2 = NaN;
        myQuartile3 = NaN;

        if (mySortedCopy != null) {
            Arrays.fill(mySortedCopy, Double.POSITIVE_INFINITY);
        }
    }

    @Override
    public int size() {
        return mySamples.size();
    }

    /**
     * Replace the underlying samples and reset the sample set.
     */
    public SampleSet swap(final Access1D<?> samples) {
        ProgrammingError.throwIfNull(samples);
        mySamples = samples;
        this.reset();
        return this;
    }

    public SampleSet swap(final double... samples) {
        return this.swap(ArrayR064.wrap(samples));
    }

    @Override
    public String toString() {
        return "Sample set Size=" + this.size() + ", Mean=" + this.getMean() + ", Var=" + this.getVariance() + ", StdDev=" + this.getStandardDeviation()
                + ", Min=" + this.getMinimum() + ", Max=" + this.getMaximum();
    }

    private void calculateQuartiles() {

        int nbSamples = this.getSamples().size();
        double[] sortedCopy = this.getSortedCopy();

        switch (nbSamples) {

            case 0:

                myMin = ZERO;
                myMax = ZERO;

                myQuartile1 = ZERO;
                myQuartile2 = ZERO;
                myQuartile3 = ZERO;

                break;

            case 1:

                myMin = sortedCopy[0];
                myMax = sortedCopy[0];

                myQuartile1 = sortedCopy[0];
                myQuartile2 = sortedCopy[0];
                myQuartile3 = sortedCopy[0];

                break;

            default:

                myMin = sortedCopy[0];
                myMax = sortedCopy[nbSamples - 1];

                int n = nbSamples / 4;
                int r = nbSamples % 4;

                switch (r) {

                    case 1:

                        myQuartile1 = 0.25 * sortedCopy[n - 1] + 0.75 * sortedCopy[n];
                        myQuartile2 = sortedCopy[2 * n];
                        myQuartile3 = 0.75 * sortedCopy[3 * n] + 0.25 * sortedCopy[3 * n + 1];

                        break;

                    case 2:

                        myQuartile1 = sortedCopy[n];
                        myQuartile2 = 0.5 * sortedCopy[2 * n] + 0.5 * sortedCopy[2 * n + 1];
                        myQuartile3 = sortedCopy[3 * n + 1];

                        break;

                    case 3:

                        myQuartile1 = 0.75 * sortedCopy[n] + 0.25 * sortedCopy[n + 1];
                        myQuartile2 = sortedCopy[2 * n + 1];
                        myQuartile3 = 0.25 * sortedCopy[3 * n + 1] + 0.75 * sortedCopy[3 * n + 2];

                        break;

                    default:

                        myQuartile1 = 0.5 * sortedCopy[n - 1] + 0.5 * sortedCopy[n];
                        myQuartile2 = 0.5 * sortedCopy[2 * n - 1] + 0.5 * sortedCopy[2 * n];
                        myQuartile3 = 0.5 * sortedCopy[3 * n - 1] + 0.5 * sortedCopy[3 * n];

                        break;
                }

                break;
        }
    }

    Access1D<?> getSamples() {
        return mySamples;
    }

    double[] getSortedCopy() {

        Access1D<?> samples = this.getSamples();
        int nbSamples = samples.size();

        if (mySortedCopy == null || mySortedCopy.length < nbSamples || mySortedCopy.length == 0) {
            mySortedCopy = samples.toRawCopy1D();
            Arrays.sort(mySortedCopy);
        } else if (mySortedCopy[0] == Double.POSITIVE_INFINITY) {
            FillMatchingSingle.fill(mySortedCopy, samples);
            Arrays.sort(mySortedCopy, 0, nbSamples);
        }

        return mySortedCopy;
    }

}

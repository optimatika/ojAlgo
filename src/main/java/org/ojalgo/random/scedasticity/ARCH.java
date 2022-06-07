/*
 * Copyright 1997-2022 Optimatika
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
package org.ojalgo.random.scedasticity;

import static org.ojalgo.function.constant.PrimitiveMath.*;

import java.util.Arrays;

import org.ojalgo.random.SampleSet;
import org.ojalgo.series.primitive.PrimitiveSeries;

public final class ARCH extends AbstractScedasticity {

    /**
     * @see #newInstance(int, double, double)
     */
    public static ARCH newInstance(final int q) {
        return ARCH.newInstance(q, ZERO, AbstractScedasticity.DEFAULT_VARIANCE);
    }

    /**
     * Will create an instance configured with default parameters. What these are may change in the future.
     * You're better of estimating suitable paramaters for your use case and then set {@link #base(double)}
     * and {@link #errorWeights(double...)}.
     */
    public static ARCH newInstance(final int q, final double mean, final double variance) {

        ARCH retVal = new ARCH(q);

        retVal.base(variance / TWELVE);

        double[] errorWeights = new double[q];
        AbstractScedasticity.average(errorWeights, ELEVEN / TWELVE);
        retVal.errorWeights(errorWeights);

        retVal.initialise(mean, variance);

        return retVal;
    }

    /**
     * @see #newInstance(int, double, double)
     */
    public static ARCH newInstance(final int q, final PrimitiveSeries values) {

        SampleSet statistics = SampleSet.wrap(values);

        double mean = statistics.getMean();
        double variance = statistics.getVariance();

        return ARCH.newInstance(q, mean, variance);
    }

    private double myBase = ZERO;
    private double myMean = ZERO;
    private final double[] mySquaredErrors;
    private final double[] myWeights;

    public ARCH(final int q) {

        super();

        mySquaredErrors = new double[q];
        myWeights = new double[q];
    }

    public ARCH base(final double base) {

        if (base <= ZERO) {
            throw new IllegalArgumentException();
        }

        myBase = base;

        return this;
    }

    public ARCH errorWeights(final double... lagged) {
        Arrays.fill(myWeights, ZERO);
        for (int i = 0, limit = Math.min(myWeights.length, lagged.length); i < limit; i++) {
            double tmpVal = lagged[i];
            if (tmpVal < ZERO) {
                throw new IllegalArgumentException();
            }
            myWeights[i] = tmpVal;
        }
        return this;
    }

    public double getMean() {
        return myMean;
    }

    public double getVariance() {

        double retVal = myBase;

        for (int i = 0, limit = Math.min(myWeights.length, mySquaredErrors.length); i < limit; i++) {
            retVal += myWeights[i] * mySquaredErrors[i];
        }

        return retVal;
    }

    public void initialise(final double mean, final double variance) {
        myMean = mean;
        Arrays.fill(mySquaredErrors, variance);
    }

    public void update(final double value) {
        double error = value - myMean;
        double squared = error * error;
        for (int i = mySquaredErrors.length - 1; i > 0; i--) {
            mySquaredErrors[i] = mySquaredErrors[i - 1];
        }
        mySquaredErrors[0] = squared;
    }

}

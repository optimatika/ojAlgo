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
package org.ojalgo.random.scedasticity;

import static org.ojalgo.function.constant.PrimitiveMath.*;

import java.util.Arrays;

import org.ojalgo.random.SampleSet;
import org.ojalgo.structure.Access1D;

public final class ARCH extends AbstractScedasticity {

    /**
     * Parameter estimation using heuristics (not max likelihood).
     *
     * @param series Series to adapt to
     * @param q Number of lagged squared error terms
     * @return Ready to use ARCH model
     */
    public static ARCH estimate(final Access1D<?> series, final int q) {

        SampleSet ss = SampleSet.wrap(series);
        double mean = ss.getMean();
        double variance = ss.getVariance();

        ARCH model = ARCH.newInstance(q);

        Access1D<?> parameters = AbstractScedasticity.parameters(series, mean, q);

        double base = variance / TWELVE;

        double[] errorWeights = new double[q];
        for (int i = 0; i < q; i++) {
            double weight = ELEVEN_TWELFTHS * parameters.doubleValue(i);
            if (weight < ZERO) {
                base += weight * variance;
            } else {
                errorWeights[i] = weight;
            }
        }

        model.base(base);
        model.errorWeights(errorWeights);

        model.initialise(mean, variance);

        return model;
    }

    /**
     * @see #newInstance(int, double, double)
     */
    public static ARCH newInstance(final int q) {
        return ARCH.newInstance(q, ZERO, DEFAULT_VARIANCE);
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
        AbstractScedasticity.average(errorWeights, ELEVEN_TWELFTHS);
        retVal.errorWeights(errorWeights);

        retVal.initialise(mean, variance);

        return retVal;
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

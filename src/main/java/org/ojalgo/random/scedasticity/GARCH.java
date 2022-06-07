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

public final class GARCH extends AbstractScedasticity {

    /**
     * @see #newInstance(int, int, double, double)
     */
    public static GARCH newInstance(final int p, final int q) {
        return GARCH.newInstance(p, q, ZERO, AbstractScedasticity.DEFAULT_VARIANCE);
    }

    /**
     * Will create an instance configured with default parameters. What these are may change in the future.
     * You're better of estimating suitable paramaters for your use case and then set {@link #base(double)},
     * {@link #errorWeights(double...)} and {@link #varianceWeights(double...)}.
     */
    public static GARCH newInstance(final int p, final int q, final double mean, final double variance) {

        GARCH retVal = new GARCH(p, q);

        retVal.base(variance / TWELVE);

        double[] errorWeights = new double[q];
        AbstractScedasticity.average(errorWeights, ONE / TWELVE);
        retVal.errorWeights(errorWeights);

        double[] varianceWeights = new double[p];
        AbstractScedasticity.decreasing(varianceWeights, TEN / TWELVE);
        retVal.varianceWeights(varianceWeights);

        retVal.initialise(mean, variance);

        return retVal;
    }

    /**
     * @see #newInstance(int, int, double, double)
     */
    public static GARCH newInstance(final int p, final int q, final PrimitiveSeries values) {

        SampleSet statistics = SampleSet.wrap(values);

        double mean = statistics.getMean();
        double variance = statistics.getVariance();

        return GARCH.newInstance(p, q, mean, variance);
    }

    private final ARCH myARCH;
    private final double[] myVariances;
    private final double[] myWeights;

    public GARCH(final int p, final int q) {

        super();

        myARCH = new ARCH(q);

        myVariances = new double[p];
        myWeights = new double[p];
    }

    public GARCH base(final double base) {
        myARCH.base(base);
        return this;
    }

    public GARCH errorWeights(final double... lagged) {
        myARCH.errorWeights(lagged);
        return this;
    }

    public double getMean() {
        return myARCH.getMean();
    }

    public double getVariance() {

        double retVal = myARCH.getVariance();

        for (int i = 0, limit = Math.min(myWeights.length, myVariances.length); i < limit; i++) {
            retVal += myWeights[i] * myVariances[i];
        }

        return retVal;
    }

    public void initialise(final double mean, final double variance) {
        myARCH.initialise(mean, variance);
        Arrays.fill(myVariances, variance);
    }

    public void update(final double value) {

        double variance = this.getVariance();

        myARCH.update(value);

        for (int i = myVariances.length - 1; i > 0; i--) {
            myVariances[i] = myVariances[i - 1];
        }
        myVariances[0] = variance;
    }

    public GARCH varianceWeights(final double... lagged) {
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

}

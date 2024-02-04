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

import static org.ojalgo.function.constant.PrimitiveMath.ZERO;

public final class Homoscedastic extends AbstractScedasticity {

    /**
     * @see #newInstance(double, double)
     */
    public static Homoscedastic newInstance() {
        return Homoscedastic.newInstance(ZERO, DEFAULT_VARIANCE);
    }

    public static Homoscedastic newInstance(final double mean, final double variance) {

        Homoscedastic retVal = new Homoscedastic();

        retVal.initialise(mean, variance);

        return retVal;
    }

    private double myMean = ZERO;
    private double myVariance = DEFAULT_VARIANCE;

    public double getMean() {
        return myMean;
    }

    public double getVariance() {
        return myVariance;
    }

    public void initialise(final double mean, final double variance) {
        myMean = mean;
        myVariance = variance;
    }

    public void update(final double value) {
        // Nothing to do for homoscedastic
    }

}

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

/**
 * Distribution of length of life when no aging. Describes the time between events in a Poisson process, i.e.
 * a process in which events occur continuously and independently at a constant average rate. It is the
 * continuous analogue of the geometric distribution.
 *
 * @author apete
 */
public class Exponential extends AbstractContinuous {

    private static final long serialVersionUID = -720007692511649669L;

    private final double myRate; // lamda

    public Exponential() {
        this(ONE);
    }

    public Exponential(final double aRate) {

        super();

        myRate = aRate;
    }

    public double getDistribution(final double aValue) {
        if (aValue < ZERO) {
            return ZERO;
        } else {
            return ONE - Math.exp(-myRate * aValue);
        }
    }

    public double getExpected() {
        return ONE / myRate;
    }

    public double getProbability(final double aValue) {
        if (aValue < ZERO) {
            return ZERO;
        } else {
            return myRate * Math.exp(-myRate * aValue);
        }
    }

    public double getQuantile(final double aProbality) {

        this.checkProbabilty(aProbality);

        return Math.log(ONE - aProbality) / -myRate;
    }

    @Override
    public double getStandardDeviation() {
        return ONE / myRate;
    }

    @Override
    protected double generate() {
        return -Math.log(this.random().nextDouble()) / myRate;
    }

}

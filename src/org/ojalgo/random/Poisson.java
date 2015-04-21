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
 * The Poisson distribution is a discrete probability distribution that expresses the probability of a given
 * number of events occurring in a fixed interval of time and/or space if these events occur with a known
 * average rate and independently of the time since the last event. (The Poisson distribution can also be used
 * for the number of events in other specified intervals such as distance, area or volume.) Distribution of
 * number of points in random point process under certain simple assumptions. Approximation to the binomial
 * distribution when aCount is large and aProbability is small. aLambda = aCount * aProbability.
 *
 * @author apete
 */
public class Poisson extends AbstractDiscrete {

    private static final long serialVersionUID = -5382163736545207782L;

    private final double myLambda; // rate or intensity

    public Poisson() {
        this(ONE);
    }

    public Poisson(final double aLambda) {

        super();

        myLambda = aLambda;
    }

    public double getExpected() {
        return myLambda;
    }

    public double getProbability(final int aVal) {
        return (Math.exp(-myLambda) * Math.pow(myLambda, aVal)) / RandomUtils.factorial(aVal);
    }

    @Override
    public double getVariance() {
        return myLambda;
    }

    @Override
    protected double generate() {

        int retVal = -1;
        double tmpVal = ZERO;

        while (tmpVal <= ONE) {

            retVal++;

            tmpVal -= Math.log(this.random().nextDouble()) / myLambda;
        }

        return retVal;
    }

}

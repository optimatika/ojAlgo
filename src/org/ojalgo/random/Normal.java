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
 * Under general conditions, the sum of a large number of random variables is approximately normally
 * distributed (the central limit theorem).
 *
 * @author apete
 */
public class Normal extends AbstractContinuous {

    private static final long serialVersionUID = 7164712313114018919L;

    private final double myLocation;
    private final double myScale;

    public Normal() {
        this(ZERO, ONE);
    }

    public Normal(final double aLocation, final double aScale) {

        super();

        myLocation = aLocation;
        myScale = aScale;
    }

    public double getDistribution(final double aValue) {
        return (ONE + RandomUtils.erf((aValue - myLocation) / (myScale * SQRT_TWO))) / TWO;
    }

    public double getExpected() {
        return myLocation;
    }

    public double getProbability(final double aValue) {

        final double tmpVal = (aValue - myLocation) / myScale;

        return Math.exp((tmpVal * tmpVal) / -TWO) / (myScale * SQRT_TWO_PI);
    }

    public double getQuantile(final double aProbality) {

        this.checkProbabilty(aProbality);

        return (myScale * SQRT_TWO * RandomUtils.erfi((TWO * aProbality) - ONE)) + myLocation;
    }

    @Override
    public double getStandardDeviation() {
        return myScale;
    }

    @Override
    protected double generate() {
        return (this.random().nextGaussian() * myScale) + myLocation;
    }

}

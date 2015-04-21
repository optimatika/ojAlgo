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
 * The number of required trials until an event with probability aProbability occurs has a geometric
 * distribution.
 *
 * @author apete
 */
public class Geometric extends AbstractDiscrete {

    private static final long serialVersionUID = 1324905651790774444L;

    private final double myProbability;

    public Geometric() {
        this(HALF);
    }

    public Geometric(final double aProbability) {

        super();

        myProbability = aProbability;
    }

    public double getExpected() {
        return ONE / myProbability;
    }

    public double getProbability(final int aVal) {
        return myProbability * Math.pow(ONE - myProbability, aVal - ONE);
    }

    @Override
    public double getVariance() {
        return (ONE - myProbability) / (myProbability * myProbability);
    }

    @Override
    protected double generate() {

        int retVal = 1;

        while ((this.random().nextDouble() + myProbability) <= ONE) {
            retVal++;
        }

        return retVal;
    }

}

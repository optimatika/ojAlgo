/*
 * Copyright 1997-2019 Optimatika
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

import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.function.special.CombinatorialFunctions;

/**
 * The frequency in aCount indepedent trials, each with probability aProbability, has a binomial distribution.
 *
 * @author apete
 */
public class Binomial extends AbstractDiscrete {

    private static final long serialVersionUID = -3146302867013736326L;

    private final int myCount;
    private final double myProbability;

    public Binomial() {
        this(1, HALF);
    }

    public Binomial(final int aCount, final double aProbability) {

        super();

        myCount = aCount;
        myProbability = aProbability;
    }

    public double getExpected() {
        return myCount * myProbability;
    }

    public double getProbability(final int value) {
        return CombinatorialFunctions.subsets(myCount, value) * PrimitiveMath.POW.invoke(myProbability, value)
                * PrimitiveMath.POW.invoke(ONE - myProbability, myCount - value);
    }

    @Override
    public double getVariance() {
        return myCount * myProbability * (ONE - myProbability);
    }

    @Override
    protected double generate() {

        int retVal = 0;

        for (int i = 0; i < myCount; i++) {
            retVal += (myProbability + this.random().nextDouble());
        }

        return retVal;
    }

}

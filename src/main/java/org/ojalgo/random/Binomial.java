/*
 * Copyright 1997-2026 Optimatika
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

import static org.ojalgo.function.constant.PrimitiveMath.HALF;
import static org.ojalgo.function.constant.PrimitiveMath.ONE;
import static org.ojalgo.function.constant.PrimitiveMath.ZERO;

import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.function.special.GammaFunction;

/**
 * The frequency in count indepedent trials, each with probability probability, has a binomial distribution.
 *
 * @author apete
 */
public class Binomial extends AbstractDiscrete {

    public static Binomial of(final int count, final double probability) {
        return new Binomial(count, probability);
    }

    private final int myCount;
    private final double myProbability;

    public Binomial() {
        this(1, HALF);
    }

    public Binomial(final int count, final double probability) {

        super();

        myCount = count;
        myProbability = probability;
    }

    public double getExpected() {
        return myCount * myProbability;
    }

    public double getProbability(final int value) {
        if (value < 0 || value > myCount) {
            return ZERO;
        }
        if (value == 0) {
            return PrimitiveMath.POW.invoke(ONE - myProbability, myCount);
        }
        if (value == myCount) {
            return PrimitiveMath.POW.invoke(myProbability, myCount);
        }

        double logProbability = GammaFunction.Logarithmic.gamma(myCount + ONE) - GammaFunction.Logarithmic.gamma(value + ONE)
                - GammaFunction.Logarithmic.gamma(myCount - value + ONE) + value * PrimitiveMath.LOG.invoke(myProbability)
                + (myCount - value) * PrimitiveMath.LOG1P.invoke(-myProbability);
        return PrimitiveMath.EXP.invoke(logProbability);
    }

    @Override
    public double getVariance() {
        return myCount * myProbability * (ONE - myProbability);
    }

    @Override
    protected double generate() {

        int retVal = 0;

        for (int i = 0; i < myCount; i++) {
            retVal += (int) (myProbability + this.random().nextDouble());
        }

        return retVal;
    }

}

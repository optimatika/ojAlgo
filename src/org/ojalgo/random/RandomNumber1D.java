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

import org.ojalgo.access.Access2D;
import org.ojalgo.array.Array1D;

abstract class RandomNumber1D {

    private final Random1D myRandom;

    protected RandomNumber1D(final Access2D<?> correlations) {

        super();

        myRandom = new Random1D(correlations);
    }

    public abstract Array1D<Double> getExpected();

    /**
     * Subclasses must override either getStandardDeviation() or getVariance()!
     * 
     * @see org.ojalgo.random.Distribution#getStandardDeviation()
     * @see org.ojalgo.random.Distribution#getVariance()
     */
    public Array1D<Double> getStandardDeviation() {

        final Array1D<Double> tmpVar = this.getVariance();

        final int tmpLength = tmpVar.size();

        final Array1D<Double> retVal = Array1D.PRIMITIVE.makeZero(tmpLength);

        for (int i = 0; i < tmpLength; i++) {
            retVal.set(i, Math.sqrt(tmpVar.doubleValue(i)));
        }

        return retVal;
    }

    /**
     * Subclasses must override either getStandardDeviation() or getVariance()!
     * 
     * @see org.ojalgo.random.Distribution#getStandardDeviation()
     * @see org.ojalgo.random.Distribution#getVariance()
     */
    public Array1D<Double> getVariance() {

        final Array1D<Double> tmpStdDev = this.getStandardDeviation();

        final int tmpLength = tmpStdDev.size();

        final Array1D<Double> retVal = Array1D.PRIMITIVE.makeZero(tmpLength);

        double tmpVal;
        for (int i = 0; i < tmpLength; i++) {
            tmpVal = tmpStdDev.doubleValue(i);
            retVal.set(i, tmpVal * tmpVal);
        }

        return retVal;
    }

    protected final Random1D random() {
        return myRandom;
    }

}

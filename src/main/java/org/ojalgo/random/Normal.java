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
package org.ojalgo.random;

import static org.ojalgo.function.constant.PrimitiveMath.*;

import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.function.special.ErrorFunction;

/**
 * Under general conditions, the sum of a large number of random variables is approximately normally
 * distributed (the central limit theorem).
 *
 * @author apete
 */
public class Normal extends AbstractContinuous {

    public static Normal of(final double location, final double scale) {
        return new Normal(location, scale);
    }

    public static Normal standard() {
        return new Normal();
    }

    private final double myLocation;
    private final double myScale;

    public Normal() {
        this(ZERO, ONE);
    }

    public Normal(final double location, final double scale) {

        super();

        myLocation = location;
        myScale = scale;
    }

    public double getDensity(final double value) {

        final double tmpVal = (value - myLocation) / myScale;

        return PrimitiveMath.EXP.invoke((tmpVal * tmpVal) / -TWO) / (myScale * SQRT_TWO_PI);
    }

    public double getDistribution(final double value) {
        return (ONE + ErrorFunction.erf((value - myLocation) / (myScale * SQRT_TWO))) / TWO;
    }

    public double getExpected() {
        return myLocation;
    }

    public double getQuantile(final double probability) {

        this.checkProbabilty(probability);

        return (myScale * SQRT_TWO * ErrorFunction.erfi((TWO * probability) - ONE)) + myLocation;
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

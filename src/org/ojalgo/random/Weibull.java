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
 * Useful as length of life distribution in reliability theory.
 * 
 * @author apete
 */
public class Weibull extends RandomNumber {

    private static final long serialVersionUID = 7315696913427382955L;

    private final double myShape; // beta
    private final double myRate; // lamda

    public Weibull() {
        this(ONE, ONE);
    }

    public Weibull(final double aLambda, final double aBeta) {

        super();

        myRate = aLambda;
        myShape = aBeta;
    }

    public double getExpected() {
        return RandomUtils.gamma(ONE + (ONE / myShape)) / myRate;
    }

    @Override
    public double getVariance() {

        final double tmpA = RandomUtils.gamma(ONE + (TWO / myShape));
        final double tmpB = RandomUtils.gamma(ONE + (ONE / myShape));

        return (tmpA - (tmpB * tmpB)) / (myRate * myRate);
    }

    @Override
    protected double generate() {
        return Math.pow(-Math.log(this.random().nextDouble()), ONE / myShape) / myRate;
    }

}

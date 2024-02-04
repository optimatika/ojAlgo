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

/**
 * Certain waiting times. Rounding errors.
 *
 * @author apete
 */
public class Uniform extends AbstractContinuous {

    public static Uniform of(final double lower, final double range) {
        return new Uniform(lower, range);
    }

    /**
     * @return An integer: 0 <= ? < limit
     */
    public static int randomInteger(final int limit) {
        return (int) PrimitiveMath.FLOOR.invoke(limit * Math.random());
    }

    /**
     * @return An integer: lower <= ? < higher
     */
    public static int randomInteger(final int lower, final int higher) {
        return lower + Uniform.randomInteger(higher - lower);
    }

    /**
     * @return An integer: 0 &lt;= ? &lt; limit
     */
    public static long randomInteger(final long limit) {
        return (long) PrimitiveMath.FLOOR.invoke(limit * Math.random());
    }

    public static Uniform standard() {
        return new Uniform();
    }

    private final double myLower;
    private final double myRange;

    public Uniform() {
        this(ZERO, ONE);
    }

    public Uniform(final double lower, final double range) {

        super();

        if (range <= ZERO) {
            throw new IllegalArgumentException("The range must be larger than 0.0!");
        }

        myLower = lower;
        myRange = range;
    }

    public double getDensity(final double value) {

        double retVal = ZERO;

        if ((myLower <= value) && (value <= (myLower + myRange))) {
            retVal = ONE / myRange;
        }

        return retVal;
    }

    public double getDistribution(final double value) {

        double retVal = ZERO;

        if ((value <= (myLower + myRange)) && (myLower <= value)) {
            retVal = (value - myLower) / myRange;
        } else if (myLower <= value) {
            retVal = ONE;
        }

        return retVal;
    }

    public double getExpected() {
        return myLower + (myRange / TWO);
    }

    public double getQuantile(final double probability) {

        this.checkProbabilty(probability);

        return myLower + (probability * myRange);
    }

    @Override
    public double getVariance() {
        return (myRange * myRange) / TWELVE;
    }

    @Override
    protected double generate() {
        return myLower + (myRange * this.random().nextDouble());
    }
}

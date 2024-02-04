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

/**
 * https://en.wikipedia.org/wiki/Cauchy_distribution
 *
 * @author apete
 */
public class Cauchy extends AbstractContinuous {

    public static Cauchy of(final double location, final double scale) {
        return new Cauchy(location, scale);
    }

    public static Cauchy standard() {
        return new Cauchy();
    }

    private final double myLocation;
    private final double myScale;

    public Cauchy() {
        this(ZERO, ONE);
    }

    public Cauchy(final double location, final double scale) {
        super();
        myLocation = location;
        myScale = scale;
    }

    public double getDensity(final double value) {
        return ONE / (PI * myScale * (ONE + Math.pow((value - myLocation) / myScale, TWO)));
    }

    public double getDistribution(final double value) {
        return HALF + Math.atan((value - myLocation) / myScale) / PI;
    }

    public double getExpected() {
        return NaN;
    }

    public double getQuantile(final double probability) {
        return myLocation + myScale * Math.tan(PI * (probability - HALF));
    }

    @Override
    public double getStandardDeviation() {
        return NaN;
    }

    @Override
    public double getVariance() {
        return NaN;
    }

}

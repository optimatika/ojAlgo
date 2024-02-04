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
package org.ojalgo.function.series;

import static org.ojalgo.function.constant.PrimitiveMath.*;

import java.util.function.DoubleUnaryOperator;

import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.function.PrimitiveFunction.SampleDomain;

/**
 * A periodic function is a function that repeats its values in regular intervals or periods. The most
 * important examples are the trigonometric functions, which repeat over intervals of length 2π radians.
 * <p>
 * This class allows you to create a periodic function from any other function. The base function only needs
 * to be defined over the interval specified in this class, The resulting function will repeat the base
 * function in regular periods. The default period is 2π, but it's possible to specify any other period. The
 * default origin is zero, but it's possible to specify any other origin.
 */
public final class PeriodicFunction implements PrimitiveFunction.Unary {

    /**
     * https://en.wikipedia.org/wiki/Sawtooth_wave
     * <p>
     * Domain: (-∞, ∞) by repeating/shifting the definition interval [0, 2π).<br>
     * Range: [-1, 1].
     */
    public static final PeriodicFunction SAWTOOTH = PeriodicFunction.of(arg -> Math.atan(Math.tan(arg / TWO)) / HALF_PI);
    /**
     * https://en.wikipedia.org/wiki/Sine_wave
     * <p>
     * Domain: (-∞, ∞) by repeating/shifting the definition interval [0, 2π).<br>
     * Range: [-1, 1].
     */
    public static final PeriodicFunction SINE = PeriodicFunction.of(SIN);
    /**
     * https://en.wikipedia.org/wiki/Square_wave
     * <p>
     * Domain: (-∞, ∞) by repeating/shifting the definition interval [0, 2π).<br>
     * Range: [-1, 1].
     */
    public static final PeriodicFunction SQUARE = PeriodicFunction.of(arg -> Math.signum(Math.sin(arg)));
    /**
     * https://en.wikipedia.org/wiki/Triangle_wave
     * <p>
     * Domain: (-∞, ∞) by repeating/shifting the definition interval [0, 2π).<br>
     * Range: [-1, 1].
     */
    public static final PeriodicFunction TRIANGLE = PeriodicFunction.of(arg -> Math.asin(Math.sin(arg)) / HALF_PI);

    public static PeriodicFunction of(final double origin, final DoubleUnaryOperator shape) {
        return new PeriodicFunction(origin, shape, TWO_PI);
    }

    /**
     * Origin at zero, and period 2π [0, 2π).
     */
    public static PeriodicFunction of(final DoubleUnaryOperator shape) {
        return new PeriodicFunction(ZERO, shape, TWO_PI);
    }

    public static DoubleUnaryOperator of(final DoubleUnaryOperator shape, final double period) {
        return new PeriodicFunction(ZERO, shape, period);
    }

    /**
     * Origin at -π, and period 2π [-π, π).
     */
    public static PeriodicFunction ofCentered(final DoubleUnaryOperator shape) {
        return new PeriodicFunction(-PI, shape, TWO_PI);
    }

    private final double myOrigin;
    private final double myPeriod;
    private final DoubleUnaryOperator myShape;

    PeriodicFunction(final double origin, final DoubleUnaryOperator shape, final double period) {
        super();
        myOrigin = origin;
        myPeriod = period;
        myShape = shape;
    }

    public SampleDomain getSampleDomain(final int nbSamples) {
        return new SampleDomain(myPeriod, nbSamples);
    }

    @Override
    public double invoke(final double arg) {
        double local = arg;
        while (local < myOrigin) {
            local += myPeriod;
        }
        while (local >= myOrigin + myPeriod) {
            local -= myPeriod;
        }
        return myShape.applyAsDouble(local);
    }

    /**
     * interval = [origin, origin+period)
     */
    public PeriodicFunction withInterval(final double origin, final double period) {
        return new PeriodicFunction(origin, myShape, period);
    }

    /**
     * interval = [origin, origin+period)
     */
    public PeriodicFunction withOrigin(final double origin) {
        return new PeriodicFunction(origin, myShape, myPeriod);
    }

    /**
     * interval = [origin, origin+period)
     */
    public PeriodicFunction withPeriod(final double period) {
        return new PeriodicFunction(myOrigin, myShape, period);
    }

    /**
     * The shape is the function that is repeated in regular intervals.
     */
    public PeriodicFunction withShape(final DoubleUnaryOperator shape) {
        return new PeriodicFunction(myOrigin, shape, myPeriod);
    }

}

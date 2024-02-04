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

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.type.ComparableNumber;

/**
 * RandomNumber
 *
 * @author apete
 */
public abstract class RandomNumber implements Distribution, NullaryFunction<Double>, ComparableNumber<RandomNumber> {

    private Random myRandom = null;

    protected RandomNumber() {
        super();
    }

    public int compareTo(final RandomNumber o) {
        return Double.compare(this.getExpected(), o.getExpected());
    }

    @Override
    public final double doubleValue() {
        return this.generate();
    }

    @Override
    public final float floatValue() {
        return (float) this.generate();
    }

    /**
     * Subclasses must override either getStandardDeviation() or getVariance()!
     *
     * @see org.ojalgo.random.Distribution#getStandardDeviation()
     * @see org.ojalgo.random.Distribution#getVariance()
     */
    public double getStandardDeviation() {
        return PrimitiveMath.SQRT.invoke(this.getVariance());
    }

    /**
     * Subclasses must override either getStandardDeviation() or getVariance()!
     *
     * @see org.ojalgo.random.Distribution#getStandardDeviation()
     * @see org.ojalgo.random.Distribution#getVariance()
     */
    public double getVariance() {
        final double stdDev = this.getStandardDeviation();
        return stdDev * stdDev;
    }

    @Override
    public final int intValue() {
        return (int) this.longValue();
    }

    public final Double invoke() {
        return this.generate();
    }

    @Override
    public final long longValue() {
        return Math.round(this.generate());
    }

    public SampleSet newSampleSet(final int numberOfSamples) {
        return SampleSet.make(this, numberOfSamples);
    }

    /**
     * Lets you choose between different {@link Random} implementations:
     * <ul>
     * <li>{@link java.util.Random}
     * <li>{@link java.util.concurrent.ThreadLocalRandom}
     * <li>{@link java.security.SecureRandom}
     * <li>...
     * </ul>
     */
    public void setRandom(final Random random) {
        myRandom = random;
    }

    public void setSeed(final long seed) {
        this.setRandom(new Random(seed));
    }

    @Override
    public String toString() {
        return this.getExpected() + "±" + this.getStandardDeviation();
    }

    protected void checkProbabilty(final double probabilty) {
        if ((probabilty < ZERO) || (ONE < probabilty)) {
            throw new IllegalArgumentException("Probabilty must be [0,1]");
        }
    }

    protected abstract double generate();

    protected final Random random() {
        if (myRandom != null) {
            return myRandom;
        }
        return ThreadLocalRandom.current();
    }
}

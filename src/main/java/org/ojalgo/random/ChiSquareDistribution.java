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

import org.ojalgo.function.special.GammaFunction;
import org.ojalgo.function.special.MissingMath;

public class ChiSquareDistribution extends AbstractContinuous {

    static final class Degree2 extends ChiSquareDistribution {

        Degree2() {
            super(TWO);
        }

        @Override
        public double getDistribution(final double value) {
            return -Math.expm1(-value / TWO);
        }

        @Override
        double calculateDensity(final double value) {
            return Math.exp(-value / TWO) / TWO;
        }

    }

    static final class NormalApproximation extends ChiSquareDistribution {

        private final Normal myApproximation;

        NormalApproximation(final double degreesOfFreedom) {

            super(degreesOfFreedom);

            myApproximation = new Normal(degreesOfFreedom, Math.sqrt(TWO * degreesOfFreedom));
        }

        @Override
        public double getDistribution(final double value) {
            return myApproximation.getDistribution(value);
        }

        @Override
        public double getExpected() {
            return myApproximation.getExpected();
        }

        @Override
        public double getQuantile(final double probability) {
            return myApproximation.getQuantile(probability);
        }

        @Override
        public double getStandardDeviation() {
            return myApproximation.getStandardDeviation();
        }

        @Override
        public double getVariance() {
            return myApproximation.getVariance();
        }

        @Override
        double calculateDensity(final double value) {
            return myApproximation.getDensity(value);
        }

    }

    private static final double _0_0001 = 0.0001;

    static final Normal NORMAL = new Normal();

    public static ChiSquareDistribution of(final int degreesOfFreedom) {
        if (degreesOfFreedom == 2) {
            return new Degree2();
        } else if (degreesOfFreedom > 50) {
            return new NormalApproximation(degreesOfFreedom);
        } else {
            return new ChiSquareDistribution(degreesOfFreedom);
        }
    }

    private final double myDegreesOfFreedom;

    ChiSquareDistribution(final double degreesOfFreedom) {
        super();
        myDegreesOfFreedom = degreesOfFreedom;
    }

    @Override
    public final double getDensity(final double value) {
        if (value <= ZERO) {
            return ZERO;
        } else {
            return this.calculateDensity(value);
        }
    }

    public double getDistribution(final double value) {
        return GammaFunction.Regularized.lower(myDegreesOfFreedom / TWO, value / TWO);
    }

    public double getExpected() {
        return myDegreesOfFreedom;
    }

    public double getQuantile(final double probability) {

        double retVal = this.approximateQuantile(probability);

        if (Double.isInfinite(retVal)) {
            return retVal;
        }

        double reverse = this.getDistribution(retVal);

        double lower = retVal, higher = retVal;

        if ((probability - reverse) > _0_0001) {
            do {
                higher *= TWO;
            } while (this.getDistribution(higher) <= probability);
        } else if ((reverse - probability) > _0_0001) {
            do {
                lower /= TWO;
            } while (this.getDistribution(lower) >= probability);
        } else {
            return retVal;
        }

        do {
            retVal = (lower + higher) / TWO;
            reverse = this.getDistribution(retVal);
            if (reverse < probability) {
                lower = retVal;
            } else if (reverse > probability) {
                higher = retVal;
            }
        } while (Math.abs(reverse - probability) >= _0_0001);

        return retVal;
    }

    @Override
    public double getVariance() {
        return TWO * myDegreesOfFreedom;
    }

    private double approximateQuantile(final double probability) {
        return MissingMath.power(NORMAL.getQuantile(probability) + Math.sqrt((TWO * myDegreesOfFreedom) - ONE), 2) / TWO;
    }

    double calculateDensity(final double value) {

        double halfFreedom = myDegreesOfFreedom / TWO;

        return (Math.pow(value, halfFreedom - ONE) * Math.exp(-value / TWO)) / (Math.pow(TWO, halfFreedom) * GammaFunction.gamma(halfFreedom));
    }

}

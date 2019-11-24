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

import org.ojalgo.function.special.GammaFunction;
import org.ojalgo.function.special.HypergeometricFunction;

public class TDistribution extends AbstractContinuous {

    static final class Degree1 extends TDistribution {

        private final Cauchy myCauchy = new Cauchy();

        Degree1() {
            super(ONE);
        }

        @Override
        public double getDensity(final double value) {
            return myCauchy.getDensity(value);
        }

        @Override
        public double getDistribution(final double value) {
            return myCauchy.getDistribution(value);
        }

        @Override
        public double getQuantile(final double probability) {
            return myCauchy.getQuantile(probability);
        }

        @Override
        protected double generate() {
            return myCauchy.generate();
        }

    }

    static final class Degree2 extends TDistribution {

        Degree2() {
            super(TWO);
        }

        @Override
        public double getDensity(final double value) {
            return ONE / Math.pow(TWO + (value * value), THREE / TWO);
        }

        @Override
        public double getDistribution(final double value) {
            return HALF + (value / (TWO * Math.sqrt(TWO + (value * value))));
        }

        @Override
        public double getQuantile(final double probability) {
            double alpha = FOUR * probability * (ONE - probability);
            return TWO * (probability - HALF) * Math.sqrt(TWO / alpha);
        }

    }

    static final class Degree3 extends TDistribution {

        Degree3() {
            super(THREE);
        }

        @Override
        public double getDensity(final double value) {
            return (SIX * Math.sqrt(THREE)) / (PI * Math.pow(THREE + (value * value), TWO));
        }

    }

    static final class Degree4 extends TDistribution {

        Degree4() {
            super(FOUR);
        }

        @Override
        public double getQuantile(final double probability) {
            double alpha = FOUR * probability * (ONE - probability);
            double sqrt = Math.sqrt(alpha);
            double q = Math.cos(THIRD * Math.acos(sqrt)) / sqrt;
            return Math.signum(probability - HALF) * TWO * Math.sqrt(q - ONE);
        }

    }

    static final class DegreeInfinity extends TDistribution {

        private final Normal myNormal = new Normal();

        DegreeInfinity() {
            super(POSITIVE_INFINITY);
        }

        @Override
        public double getDensity(final double value) {
            return myNormal.getDensity(value);
        }

        @Override
        public double getDistribution(final double value) {
            return myNormal.getDistribution(value);
        }

        @Override
        public double getQuantile(final double probability) {
            return myNormal.getQuantile(probability);
        }

        @Override
        protected double generate() {
            return myNormal.generate();
        }

    }

    /**
     * @deprecated v48
     */
    @Deprecated
    public static TDistribution make(final int degreesOfFreedom) {
        return TDistribution.of(degreesOfFreedom);
    }

    public static TDistribution of(final int degreesOfFreedom) {
        switch (degreesOfFreedom) {
        case 1:
            return new Degree1();
        case 2:
            return new Degree2();
        case 3:
            return new Degree3();
        case 4:
            return new Degree4();
        case Integer.MAX_VALUE:
            return new DegreeInfinity();
        default:
            return new TDistribution(degreesOfFreedom);
        }
    }

    /**
     * The density and distribution functions share a common constant factor
     */
    private final double myConstant;
    private final double myDegreesOfFreedom;

    TDistribution(final double degreesOfFreedom) {
        super();
        myDegreesOfFreedom = Math.min(100, degreesOfFreedom);
        myConstant = GammaFunction.gamma((myDegreesOfFreedom + ONE) / TWO)
                / (Math.sqrt(myDegreesOfFreedom * PI) * GammaFunction.gamma(myDegreesOfFreedom / TWO));
    }

    public double getDensity(final double value) {
        return myConstant * Math.pow(ONE + ((value * value) / myDegreesOfFreedom), (NEG - myDegreesOfFreedom) / TWO);
    }

    public double getDistribution(final double value) {
        return HALF + (value * myConstant
                * HypergeometricFunction.hypergeometric(HALF, (myDegreesOfFreedom - ONE) / TWO, THREE / TWO, -(value * value) / myDegreesOfFreedom));
    }

    public double getExpected() {
        if (myDegreesOfFreedom > ONE) {
            return ZERO;
        } else {
            return NaN;
        }
    }

    public double getQuantile(final double probability) {
        // TODO Auto-generated method stub
        return NaN;
    }

    @Override
    public double getVariance() {
        if (myDegreesOfFreedom > TWO) {
            return myDegreesOfFreedom / (myDegreesOfFreedom - TWO);
        } else if (myDegreesOfFreedom > ONE) {
            return POSITIVE_INFINITY;
        } else {
            return NaN;
        }
    }

}

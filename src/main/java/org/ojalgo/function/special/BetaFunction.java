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
package org.ojalgo.function.special;

import static org.ojalgo.function.constant.PrimitiveMath.ONE;

import org.ojalgo.scalar.ComplexNumber;

public abstract class BetaFunction {

    public static abstract class Incomplete extends BetaFunction {

        public static ComplexNumber beta(final double limit, final ComplexNumber a, final ComplexNumber b) {
            // TODO Implement it!
            return ComplexNumber.NaN;
        }

        public static double beta(final double limit, final double a, final double b) {

            double tmp = ONE;
            double sum = tmp / a;

            // This implementation isn't very good numerically,
            // but icreasing the number of iterations doesn't seem to help much.
            for (int n = 1; n < 100; n++) {
                tmp *= ((n - b) / n) * limit;
                sum += tmp / (a + n);
            }

            // return Math.pow(limit, a) * sum;
            return Math.exp((a * Math.log(limit)) + Math.log(sum));
        }

        public static double beta(final double limit, final int a, final int b) {
            return Incomplete.beta(limit, (double) a, (double) b);
        }

    }

    public static abstract class Regularized extends BetaFunction {

        public static ComplexNumber beta(final double limit, final ComplexNumber a, final ComplexNumber b) {
            return BetaFunction.Incomplete.beta(limit, a, b).divide(BetaFunction.beta(a, b));
        }

        public static double beta(final double limit, final double a, final double b) {
            return BetaFunction.Incomplete.beta(limit, a, b) / BetaFunction.beta(a, b);
        }

        public static double beta(final double limit, final int a, final int b) {
            return BetaFunction.Incomplete.beta(limit, a, b) / BetaFunction.beta(a, b);
        }

    }

    public static ComplexNumber beta(final ComplexNumber a, final ComplexNumber b) {
        return GammaFunction.gamma(a).multiply(GammaFunction.gamma(b)).divide(GammaFunction.gamma(a.add(b)));
    }

    public static double beta(final double a, final double b) {
        return (GammaFunction.gamma(a) * GammaFunction.gamma(b)) / GammaFunction.gamma(a + b);
    }

    public static double beta(final int a, final int b) {
        return (GammaFunction.gamma(a) * GammaFunction.gamma(b)) / GammaFunction.gamma(a + b);
    }

}

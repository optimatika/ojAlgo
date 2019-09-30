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
package org.ojalgo.function.special;

import static org.ojalgo.function.constant.PrimitiveMath.*;

import org.ojalgo.scalar.ComplexNumber;

public abstract class GammaFunction {

    public static abstract class Incomplete extends GammaFunction {

        public static long lower(final int n, double limit) {
            return 0L;
        }

        public static long upper(final int n, double limit) {
            return 0L;
        }

        public static double lower(final double x, double limit) {
            return NaN;
        }

        public static double upper(final double x, double limit) {
            return NaN;
        }

        public static ComplexNumber lower(final ComplexNumber z, double limit) {
            return null;
        }

        public static ComplexNumber upper(final ComplexNumber z, double limit) {
            return null;
        }

    }

    public static abstract class Regularized extends GammaFunction {

        public static ComplexNumber lower(final ComplexNumber z, double limit) {
            return GammaFunction.Incomplete.lower(z, limit).divide(GammaFunction.gamma(z));
        }

        public static ComplexNumber upper(final ComplexNumber z, double limit) {
            return GammaFunction.Incomplete.upper(z, limit).divide(GammaFunction.gamma(z));
        }

        public static double lower(final double x, double limit) {
            return GammaFunction.Incomplete.lower(x, limit) / GammaFunction.gamma(x);
        }

        public static double upper(final double x, double limit) {
            return GammaFunction.Incomplete.upper(x, limit) / GammaFunction.gamma(x);
        }

        public static long lower(final int n, double limit) {
            return GammaFunction.Incomplete.lower(n, limit) / GammaFunction.gamma(n);
        }

        public static long upper(final int n, double limit) {
            return GammaFunction.Incomplete.upper(n, limit) / GammaFunction.gamma(n);
        }

    }

    /**
     * For the Lanczos approximation of the gamma function
     */
    private static final double[] L9 = { 0.99999999999980993227684700473478, 676.520368121885098567009190444019, -1259.13921672240287047156078755283,
            771.3234287776530788486528258894, -176.61502916214059906584551354, 12.507343278686904814458936853, -0.13857109526572011689554707,
            9.984369578019570859563e-6, 1.50563273514931155834e-7 };

    public static ComplexNumber gamma(final ComplexNumber z) {
        // TODO Implemennt it!
        return null;
    }

    /**
     * Lanczos approximation. The abritray constant is 7, and there are 9 coefficients used. Essentially the
     * algorithm is taken from <a href="http://en.wikipedia.org/wiki/Lanczos_approximation">WikipediA</a> ,
     * but it's modified a bit and I found more exact coefficients somewhere else.
     */
    public static double gamma(final double x) {

        if ((x <= ZERO) && (ABS.invoke(x % ONE) < MACHINE_EPSILON)) {

            return NaN;

        } else {

            if (x < HALF) {

                return PI / (SIN.invoke(PI * x) * GammaFunction.gamma(ONE - x));

            } else {

                final double x1 = x - ONE;
                final double x7 = x1 + (7 + HALF);

                double x9 = L9[0];
                for (int i = 1; i < L9.length; i++) {
                    x9 += L9[i] / (x1 + i);
                }

                return SQRT_TWO_PI * POW.invoke(x7, x1 + HALF) * EXP.invoke(-x7) * x9;
            }
        }
    }

    public static long gamma(final int n) {
        if (n < 1) {
            throw new IllegalArgumentException();
        }
        return Math.round(CombinatorialFunctions.factorial(n - 1));
    }

}

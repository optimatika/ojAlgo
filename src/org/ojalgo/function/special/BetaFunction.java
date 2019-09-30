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

public abstract class BetaFunction {

    public static abstract class Incomplete extends BetaFunction {

        public static double beta(double z, double a, double b) {

            double tmp = ONE;
            double incr = tmp / a;
            double sum = incr;

            for (int n = 1; (n < 100) && (Math.abs(incr) < MACHINE_EPSILON); n++) {
                tmp *= ((n - b) * z) / n;
                incr = tmp / (a + n);
                sum += incr;
            }

            return Math.pow(z, a) * sum;
        }

        public static double beta(double z, double a, int b) {
            return 0D;
        }

    }

    public static abstract class Regularized extends BetaFunction {

        public static double beta(double z, double a, double b) {
            return BetaFunction.Incomplete.beta(z, a, b) / BetaFunction.beta(a, b);
        }

        public static double beta(double z, double a, int b) {
            return BetaFunction.Incomplete.beta(z, a, b) / BetaFunction.beta(a, b);
        }

    }

    public static double beta(double a, double b) {
        return (GammaFunction.gamma(a) * GammaFunction.gamma(b)) / GammaFunction.gamma(a + b);
    }

    public static double beta(int a, int b) {
        return (CombinatorialFunctions.factorial(a - 1) * CombinatorialFunctions.factorial(b - 1)) / CombinatorialFunctions.factorial((a + b) - 1);
    }

}

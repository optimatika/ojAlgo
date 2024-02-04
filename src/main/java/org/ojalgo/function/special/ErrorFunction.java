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

import static org.ojalgo.function.constant.PrimitiveMath.*;

public abstract class ErrorFunction {

    private static final double[] C;

    static {

        C = new double[1000];

        ErrorFunction.C[0] = ONE;
        for (int k = 1; k < ErrorFunction.C.length; k++) {
            ErrorFunction.C[k] = ZERO;
            for (int m = 0; m <= (k - 1); m++) {
                ErrorFunction.C[k] += (ErrorFunction.C[m] * ErrorFunction.C[k - 1 - m]) / ((m + 1) * ((2 * m) + 1));
            }
        }
    }

    /**
     * Error Function <br>
     * <a href="http://en.wikipedia.org/wiki/Error_function">erf()&nbsp;@&nbsp;Wikipedia</a> <br>
     * <a href="http://mathworld.wolfram.com/Erf.html">erf()&nbsp;@&nbsp;Wolfram MathWorld</a>
     */
    public static double erf(final double arg) {

        if (arg < -FOUR) {

            return NEG;

        } else if (arg > FOUR) {

            return ONE;

        } else {

            double retVal = ZERO;
            final double squared = arg * arg;
            double tmpVal;

            for (int n = 0; n <= 100; n++) {
                tmpVal = arg / ((2 * n) + 1);
                for (int i = 1; i <= n; i++) {
                    tmpVal *= -squared / i;
                }
                retVal += tmpVal;
            }

            return (TWO * retVal) / SQRT_PI;
        }
    }

    /**
     * Complementary Error Function <br>
     * <a href="http://en.wikipedia.org/wiki/Error_function">erf()&nbsp;@&nbsp;Wikipedia</a> <br>
     * <a href="http://mathworld.wolfram.com/Erf.html">erf()&nbsp;@&nbsp;Wolfram MathWorld</a>
     */
    public static double erfc(final double arg) {
        return ONE - ErrorFunction.erf(arg);
    }

    /**
     * Inverse Error Function <br>
     * <a href="http://en.wikipedia.org/wiki/Error_function">erf()&nbsp;@&nbsp;Wikipedia</a> <br>
     * <a href="http://mathworld.wolfram.com/Erf.html">erf()&nbsp;@&nbsp;Wolfram MathWorld</a>
     */
    public static double erfi(final double arg) {

        if (Math.abs(arg) > ONE) {
            return NaN;
        } else if (arg == NEG) {
            return NEGATIVE_INFINITY;
        } else if (arg == ONE) {
            return POSITIVE_INFINITY;
        } else {

            double retVal = ZERO;

            double base = (SQRT_PI * arg) / TWO;
            for (int k = 500; k >= 0; k--) {
                int kk1 = (2 * k) + 1;
                double power = Math.pow(base, kk1);
                retVal += (C[k] / kk1) * power;
            }

            return retVal;
        }
    }

    private ErrorFunction() {}

}

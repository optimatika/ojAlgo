/*
 * Copyright 1997-2015 Optimatika (www.optimatika.se)
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

import static org.ojalgo.constant.PrimitiveMath.*;

public abstract class RandomUtils {

    private static final double[] C;

    static {

        C = new double[1000];

        C[0] = ONE;
        for (int k = 1; k < C.length; k++) {
            C[k] = ZERO;
            for (int m = 0; m <= (k - 1); m++) {
                C[k] += (C[m] * C[k - 1 - m]) / ((m + 1) * ((2 * m) + 1));
            }
        }
    }

    /**
     * For the Lanczos approximation of the gamma function
     */
    private static final double[] L9 = { 0.99999999999980993227684700473478, 676.520368121885098567009190444019, -1259.13921672240287047156078755283,
            771.3234287776530788486528258894, -176.61502916214059906584551354, 12.507343278686904814458936853, -0.13857109526572011689554707,
            9.984369578019570859563e-6, 1.50563273514931155834e-7 };

    /**
     * @param aSumOfValues The sum of all values in a sample set
     * @param aSumOfSquaredValues The sum of all squared values, in a sample set
     * @param aValuesCount The number of values in the sample set
     * @return The sample set's variance
     */
    public static double calculateVariance(final double aSumOfValues, final double aSumOfSquaredValues, final int aValuesCount) {
        return ((aValuesCount * aSumOfSquaredValues) - (aSumOfValues * aSumOfValues)) / (aValuesCount * (aValuesCount - 1));
    }

    /**
     * Error Function <br>
     * <a href="http://en.wikipedia.org/wiki/Error_function">erf()&nbsp;@&nbsp;Wikipedia</a> <br>
     * <a href="http://mathworld.wolfram.com/Erf.html">erf()&nbsp;@&nbsp;Wolfram MathWorld</a>
     */
    public static double erf(final double anArg) {

        double retVal = ZERO;
        final double tmpSqr = anArg * anArg;
        double tmpVal;

        for (int n = 0; n <= 60; n++) {
            tmpVal = anArg / ((2 * n) + 1);
            for (int i = 1; i <= n; i++) {
                tmpVal *= -tmpSqr / i;
            }
            retVal += tmpVal;
        }

        return (TWO * retVal) / SQRT_PI;
    }

    /**
     * Complementary Error Function <br>
     * <a href="http://en.wikipedia.org/wiki/Error_function">erf()&nbsp;@&nbsp;Wikipedia</a> <br>
     * <a href="http://mathworld.wolfram.com/Erf.html">erf()&nbsp;@&nbsp;Wolfram MathWorld</a>
     */
    public static double erfc(final double anArg) {
        return ONE - RandomUtils.erf(anArg);
    }

    /**
     * Inverse Error Function <br>
     * <a href="http://en.wikipedia.org/wiki/Error_function">erf()&nbsp;@&nbsp;Wikipedia</a> <br>
     * <a href="http://mathworld.wolfram.com/Erf.html">erf()&nbsp;@&nbsp;Wolfram MathWorld</a>
     */
    public static double erfi(final double anArg) {

        double retVal = ZERO;

        for (int k = 500; k >= 0; k--) {
            retVal += (C[k] * (Math.pow((SQRT_PI * anArg) / TWO, (2 * k) + 1))) / ((2 * k) + 1);
        }

        return retVal;
    }

    public static double factorial(final int aVal) {

        double retVal = ONE;

        for (int i = 2; i <= aVal; i++) {
            retVal *= i;
        }

        return retVal;
    }

    /**
     * Lanczos approximation. The abritray constant is 7, and there are 9 coefficients used. Essentially the
     * algorithm is taken from <a href="http://en.wikipedia.org/wiki/Lanczos_approximation">WikipediA</a> ,
     * but it's modified a bit and I found more exact coefficients somewhere else.
     */
    public static double gamma(final double arg) {

        if ((arg <= ZERO) && (Math.abs(arg % ONE) < MACHINE_EPSILON)) {

            return NaN;

        } else {

            if (arg < HALF) {

                return PI / (Math.sin(PI * arg) * RandomUtils.gamma(ONE - arg));

            } else {

                final double z = arg - ONE;

                double x = L9[0];
                for (int i = 1; i < L9.length; i++) {
                    x += L9[i] / (z + i);
                }

                final double t = z + (7 + HALF);

                return SQRT_TWO_PI * Math.pow(t, z + HALF) * Math.exp(-t) * x;
            }
        }
    }

    /**
     * @param n The number of elements in the set
     * @param k A vector of subset sizes the sum of which must equal the size of the full set
     * @return The number of ways the set can be partioned in to subsets of the given sizes
     */
    public static int partitions(final int n, final int[] k) {
        int retVal = (int) RandomUtils.factorial(n);
        for (int i = 0; i < k.length; i++) {
            retVal /= RandomUtils.factorial(k[i]);
        }
        return retVal;
    }

    /**
     * @param n The number of elements in the set
     * @return The number of permutations of the set
     */
    public static int permutations(final int n) {
        return (int) RandomUtils.factorial(n);
    }

    /**
     * @param n The number of elements in the set
     * @param k The number of elements in the subset
     * @return The number of subsets to the set
     */
    public static int subsets(final int n, final int k) {
        return (int) (RandomUtils.factorial(n) / (RandomUtils.factorial(k) * RandomUtils.factorial(n - k)));
    }

    /**
     * @param n The number of elements in the set
     * @param k The size of the tuple
     * @return The number of ordered k-tuples (variations) of the set
     */
    public static int variations(final int n, final int k) {
        return (int) (RandomUtils.factorial(n) / RandomUtils.factorial(n - k));
    }

    private RandomUtils() {
        super();
    }
}

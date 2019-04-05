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

import org.ojalgo.function.special.CombinatorialFunctions;
import org.ojalgo.function.special.ErrorFunction;
import org.ojalgo.function.special.GammaFunction;

/**
 * RandomUtils
 *
 * @deprecated v48
 * @author apete
 */
@Deprecated
public abstract class RandomUtils {

    /**
     * @param sumOfValues The sum of all values in a sample set
     * @param sumOfSquaredValues The sum of all squared values, in a sample set
     * @param numberOfValues The number of values in the sample set
     * @return The sample set's variance
     * @deprecated v48 Use {@link SampleSet#calculateVariance(double,double,int)} instead
     */
    @Deprecated
    public static double calculateVariance(final double sumOfValues, final double sumOfSquaredValues, final int numberOfValues) {
        return SampleSet.calculateVariance(sumOfValues, sumOfSquaredValues, numberOfValues);
    }

    /**
     * Error Function <br>
     * <a href="http://en.wikipedia.org/wiki/Error_function">erf()&nbsp;@&nbsp;Wikipedia</a> <br>
     * <a href="http://mathworld.wolfram.com/Erf.html">erf()&nbsp;@&nbsp;Wolfram MathWorld</a>
     *
     * @deprecated v48 Use {@link ErrorFunction#erf(double)} instead
     */
    @Deprecated
    public static double erf(final double arg) {
        return ErrorFunction.erf(arg);
    }

    /**
     * Complementary Error Function <br>
     * <a href="http://en.wikipedia.org/wiki/Error_function">erf()&nbsp;@&nbsp;Wikipedia</a> <br>
     * <a href="http://mathworld.wolfram.com/Erf.html">erf()&nbsp;@&nbsp;Wolfram MathWorld</a>
     *
     * @deprecated v48 Use {@link ErrorFunction#erfc(double)} instead
     */
    @Deprecated
    public static double erfc(final double anArg) {
        return ErrorFunction.erfc(anArg);
    }

    /**
     * Inverse Error Function <br>
     * <a href="http://en.wikipedia.org/wiki/Error_function">erf()&nbsp;@&nbsp;Wikipedia</a> <br>
     * <a href="http://mathworld.wolfram.com/Erf.html">erf()&nbsp;@&nbsp;Wolfram MathWorld</a>
     *
     * @deprecated v48 Use {@link ErrorFunction#erfi(double)} instead
     */
    @Deprecated
    public static double erfi(final double arg) {
        return ErrorFunction.erfi(arg);
    }

    /**
     * @deprecated v48 Use {@link CombinatorialFunctions#factorial(int)} instead
     */
    @Deprecated
    public static double factorial(final int aVal) {
        return CombinatorialFunctions.factorial(aVal);
    }

    /**
     * Lanczos approximation. The abritray constant is 7, and there are 9 coefficients used. Essentially the
     * algorithm is taken from <a href="http://en.wikipedia.org/wiki/Lanczos_approximation">WikipediA</a> ,
     * but it's modified a bit and I found more exact coefficients somewhere else.
     *
     * @deprecated v48 Use {@link GammaFunction#gamma(double)} instead
     */
    @Deprecated
    public static double gamma(final double arg) {
        return GammaFunction.gamma(arg);
    }

    /**
     * @param n The number of elements in the set
     * @param k A vector of subset sizes the sum of which must equal the size of the full set
     * @return The number of ways the set can be partioned in to subsets of the given sizes
     * @deprecated v48 Use {@link CombinatorialFunctions#partitions(int,int[])} instead
     */
    @Deprecated
    public static int partitions(final int n, final int[] k) {
        return CombinatorialFunctions.partitions(n, k);
    }

    /**
     * @param n The number of elements in the set
     * @return The number of permutations of the set
     * @deprecated v48 Use {@link CombinatorialFunctions#permutations(int)} instead
     */
    @Deprecated
    public static int permutations(final int n) {
        return CombinatorialFunctions.permutations(n);
    }

    /**
     * @param n The number of elements in the set
     * @param k The number of elements in the subset
     * @return The number of subsets to the set
     * @deprecated v48 Use {@link CombinatorialFunctions#subsets(int,int)} instead
     */
    @Deprecated
    public static int subsets(final int n, final int k) {
        return CombinatorialFunctions.subsets(n, k);
    }

    /**
     * @param n The number of elements in the set
     * @param k The size of the tuple
     * @return The number of ordered k-tuples (variations) of the set
     * @deprecated v48 Use {@link CombinatorialFunctions#variations(int,int)} instead
     */
    @Deprecated
    public static int variations(final int n, final int k) {
        return CombinatorialFunctions.variations(n, k);
    }

    private RandomUtils() {
        super();
    }
}

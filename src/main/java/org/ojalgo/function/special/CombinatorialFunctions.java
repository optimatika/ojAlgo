/*
 * Copyright 1997-2026 Optimatika
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

import static org.ojalgo.function.constant.PrimitiveMath.NEGATIVE_INFINITY;
import static org.ojalgo.function.constant.PrimitiveMath.ONE;

/**
 * https://reference.wolfram.com/language/tutorial/CombinatorialFunctions.html
 * <p>
 * The counts are exact. Where there is no way to form the subsets, partitions or tuples the count is 0. A
 * negative number of elements in the set throws an {@link IllegalArgumentException}.
 *
 * @author apete
 */
public abstract class CombinatorialFunctions {

    /**
     * The natural logarithms of the counts, computed from the logarithmic gamma function. They are defined
     * also where the counts do not fit in a long. Where a count is 0 its logarithm is negative infinity.
     */
    public static abstract class Logarithmic {

        /**
         * @see CombinatorialFunctions#partitions(int, int[])
         */
        public static double partitions(final int n, final int[] k) {

            if (!CombinatorialFunctions.isPartition(n, k)) {
                return NEGATIVE_INFINITY;
            }

            double retVal = GammaFunction.Logarithmic.gamma(n + ONE);
            for (int i = 0; i < k.length; i++) {
                retVal -= GammaFunction.Logarithmic.gamma(k[i] + ONE);
            }
            return retVal;
        }

        /**
         * @see CombinatorialFunctions#permutations(int)
         */
        public static double permutations(final int n) {

            CombinatorialFunctions.validate(n);

            return GammaFunction.Logarithmic.gamma(n + ONE);
        }

        /**
         * @see CombinatorialFunctions#subsets(int, int)
         */
        public static double subsets(final int n, final int k) {

            CombinatorialFunctions.validate(n);

            if (k < 0 || k > n) {
                return NEGATIVE_INFINITY;
            }

            return GammaFunction.Logarithmic.gamma(n + ONE) - GammaFunction.Logarithmic.gamma(k + ONE) - GammaFunction.Logarithmic.gamma(n - k + ONE);
        }

        /**
         * @see CombinatorialFunctions#variations(int, int)
         */
        public static double variations(final int n, final int k) {

            CombinatorialFunctions.validate(n);

            if (k < 0 || k > n) {
                return NEGATIVE_INFINITY;
            }

            return GammaFunction.Logarithmic.gamma(n + ONE) - GammaFunction.Logarithmic.gamma(n - k + ONE);
        }

    }

    /**
     * @param n The number of elements in the set
     * @param k A vector of subset sizes the sum of which must equal the size of the full set
     * @return The number of ways the set can be partitioned in to subsets of the given sizes, 0 if any size
     *         is negative or the sizes do not sum to n
     * @throws ArithmeticException if the result does not fit in a long
     */
    public static long partitions(final int n, final int[] k) {

        if (!CombinatorialFunctions.isPartition(n, k)) {
            return 0L;
        }

        long retVal = 1L;
        int remaining = n;
        for (int i = 0; i < k.length; i++) {
            retVal = Math.multiplyExact(retVal, CombinatorialFunctions.subsets(remaining, k[i]));
            remaining -= k[i];
        }
        return retVal;
    }

    /**
     * @param n The number of elements in the set
     * @return The number of permutations of the set
     * @throws ArithmeticException if the result does not fit in a long
     */
    public static long permutations(final int n) {

        CombinatorialFunctions.validate(n);

        long retVal = 1L;
        for (int i = 2; i <= n; i++) {
            retVal = Math.multiplyExact(retVal, i);
        }
        return retVal;
    }

    /**
     * @param n The number of elements in the set
     * @param k The number of elements in the subset
     * @return The number of subsets of the set, 0 if k < 0 or k > n
     * @throws ArithmeticException if the result does not fit in a long
     */
    public static long subsets(final int n, final int k) {

        CombinatorialFunctions.validate(n);

        if (k < 0 || k > n) {
            return 0L;
        }

        int m = Math.min(k, n - k);

        long retVal = 1L;
        for (int i = 1; i <= m; i++) {
            // C(n-m+i, i) = C(n-m+i-1, i-1) * (n-m+i) / i, the division is exact
            long gcd = MissingMath.gcd(retVal, i);
            retVal = Math.multiplyExact(retVal / gcd, (n - m + i) / (i / gcd));
        }
        return retVal;
    }

    /**
     * @param n The number of elements in the set
     * @param k The size of the tuple
     * @return The number of ordered k-tuples (variations) of the set, 0 if k < 0 or k > n
     * @throws ArithmeticException if the result does not fit in a long
     */
    public static long variations(final int n, final int k) {

        CombinatorialFunctions.validate(n);

        if (k < 0 || k > n) {
            return 0L;
        }

        long retVal = 1L;
        for (int i = 0; i < k; i++) {
            retVal = Math.multiplyExact(retVal, n - i);
        }
        return retVal;
    }

    private static boolean isPartition(final int n, final int[] k) {

        CombinatorialFunctions.validate(n);

        long sum = 0L;
        for (int i = 0; i < k.length; i++) {
            if (k[i] < 0) {
                return false;
            }
            sum += k[i];
        }
        return sum == n;
    }

    private static void validate(final int n) {
        if (n < 0) {
            throw new IllegalArgumentException();
        }
    }

}

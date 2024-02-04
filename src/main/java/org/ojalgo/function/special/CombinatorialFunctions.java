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

/**
 * https://reference.wolfram.com/language/tutorial/CombinatorialFunctions.html
 *
 * @author apete
 */
public abstract class CombinatorialFunctions {

    /**
     * @param n The number of elements in the set
     * @param k A vector of subset sizes the sum of which must equal the size of the full set
     * @return The number of ways the set can be partitioned in to subsets of the given sizes
     */
    public static long partitions(final int n, final int[] k) {
        double retVal = MissingMath.factorial(n);
        for (int i = 0, limit = k.length; i < limit; i++) {
            retVal /= MissingMath.factorial(k[i]);
        }
        return Math.round(retVal);
    }

    /**
     * @param n The number of elements in the set
     * @return The number of permutations of the set
     */
    public static long permutations(final int n) {
        return Math.round(MissingMath.factorial(n));
    }

    /**
     * @param n The number of elements in the set
     * @param k The number of elements in the subset
     * @return The number of subsets to the set
     */
    public static long subsets(final int n, final int k) {
        return Math.round(MissingMath.factorial(n) / (MissingMath.factorial(k) * MissingMath.factorial(n - k)));
    }

    /**
     * @param n The number of elements in the set
     * @param k The size of the tuple
     * @return The number of ordered k-tuples (variations) of the set
     */
    public static long variations(final int n, final int k) {
        return Math.round(MissingMath.factorial(n) / MissingMath.factorial(n - k));
    }

}

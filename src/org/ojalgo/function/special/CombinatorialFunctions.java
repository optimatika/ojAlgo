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

import static org.ojalgo.constant.PrimitiveMath.*;

/**
 * https://reference.wolfram.com/language/tutorial/CombinatorialFunctions.html
 *
 * @author apete
 */
public abstract class CombinatorialFunctions {

    public static double factorial(final int arg) {

        double retVal = ONE;

        for (int i = 2; i <= arg; i++) {
            retVal *= i;
        }

        return retVal;
    }

    /**
     * @param n The number of elements in the set
     * @param k A vector of subset sizes the sum of which must equal the size of the full set
     * @return The number of ways the set can be partioned in to subsets of the given sizes
     */
    public static int partitions(final int n, final int[] k) {
        int retVal = (int) CombinatorialFunctions.factorial(n);
        for (int i = 0; i < k.length; i++) {
            retVal /= CombinatorialFunctions.factorial(k[i]);
        }
        return retVal;
    }

    /**
     * @param n The number of elements in the set
     * @return The number of permutations of the set
     */
    public static int permutations(final int n) {
        return (int) CombinatorialFunctions.factorial(n);
    }

    /**
     * @param n The number of elements in the set
     * @param k The number of elements in the subset
     * @return The number of subsets to the set
     */
    public static int subsets(final int n, final int k) {
        return (int) (CombinatorialFunctions.factorial(n) / (CombinatorialFunctions.factorial(k) * CombinatorialFunctions.factorial(n - k)));
    }

    /**
     * @param n The number of elements in the set
     * @param k The size of the tuple
     * @return The number of ordered k-tuples (variations) of the set
     */
    public static int variations(final int n, final int k) {
        return (int) (CombinatorialFunctions.factorial(n) / CombinatorialFunctions.factorial(n - k));
    }

    private CombinatorialFunctions() {
    }

}

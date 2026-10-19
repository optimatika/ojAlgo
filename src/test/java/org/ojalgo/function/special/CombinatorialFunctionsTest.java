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
import static org.ojalgo.function.constant.PrimitiveMath.ZERO;

import java.math.BigInteger;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;

public class CombinatorialFunctionsTest {

    private static BigInteger binomial(final int n, final int k) {
        BigInteger retVal = BigInteger.ONE;
        for (int i = 0; i < k; i++) {
            retVal = retVal.multiply(BigInteger.valueOf(n - i)).divide(BigInteger.valueOf(i + 1));
        }
        return retVal;
    }

    private static BigInteger factorial(final int n) {
        BigInteger retVal = BigInteger.ONE;
        for (int i = 2; i <= n; i++) {
            retVal = retVal.multiply(BigInteger.valueOf(i));
        }
        return retVal;
    }

    private static boolean fits(final BigInteger value) {
        return value.bitLength() < Long.SIZE;
    }

    private static double ln(final BigInteger value) {
        int shift = Math.max(0, value.bitLength() - 60);
        return Math.log(value.shiftRight(shift).doubleValue()) + shift * Math.log(2.0);
    }

    @Test
    public void testLogarithmic() {

        for (int n = 0; n <= 300; n += 7) {
            BigInteger nFactorial = CombinatorialFunctionsTest.factorial(n);

            TestUtils.assertEquals(CombinatorialFunctionsTest.ln(nFactorial), CombinatorialFunctions.Logarithmic.permutations(n), 1E-10);

            for (int k = 0; k <= n; k += 3) {
                BigInteger subsets = CombinatorialFunctionsTest.binomial(n, k);
                BigInteger variations = nFactorial.divide(CombinatorialFunctionsTest.factorial(n - k));

                TestUtils.assertEquals(CombinatorialFunctionsTest.ln(subsets), CombinatorialFunctions.Logarithmic.subsets(n, k), 1E-10);
                TestUtils.assertEquals(CombinatorialFunctionsTest.ln(variations), CombinatorialFunctions.Logarithmic.variations(n, k), 1E-10);
            }
        }

        TestUtils.assertEquals(CombinatorialFunctionsTest.ln(CombinatorialFunctionsTest.binomial(2000, 1000)),
                CombinatorialFunctions.Logarithmic.subsets(2000, 1000), 1E-9);
        TestUtils.assertEquals(CombinatorialFunctionsTest.ln(CombinatorialFunctionsTest.binomial(100, 50)),
                CombinatorialFunctions.Logarithmic.partitions(100, new int[] { 50, 50 }), 1E-10);
        TestUtils.assertEquals(Math.log(2520.0), CombinatorialFunctions.Logarithmic.partitions(10, new int[] { 2, 3, 5 }), 1E-10);

        TestUtils.assertEquals(NEGATIVE_INFINITY, CombinatorialFunctions.Logarithmic.subsets(5, 6), ZERO);
        TestUtils.assertEquals(NEGATIVE_INFINITY, CombinatorialFunctions.Logarithmic.subsets(5, -1), ZERO);
        TestUtils.assertEquals(NEGATIVE_INFINITY, CombinatorialFunctions.Logarithmic.variations(5, 6), ZERO);
        TestUtils.assertEquals(NEGATIVE_INFINITY, CombinatorialFunctions.Logarithmic.partitions(4, new int[] { 1, 1 }), ZERO);

        TestUtils.assertThrows(IllegalArgumentException.class, () -> CombinatorialFunctions.Logarithmic.subsets(-1, 0));
    }

    @Test
    public void testPartitions() {

        TestUtils.assertEquals(2520L, CombinatorialFunctions.partitions(10, new int[] { 2, 3, 5 }));

        BigInteger expected = CombinatorialFunctionsTest.factorial(30).divide(CombinatorialFunctionsTest.factorial(10).pow(3));
        TestUtils.assertEquals(expected.longValueExact(), CombinatorialFunctions.partitions(30, new int[] { 10, 10, 10 }));

        TestUtils.assertEquals(1L, CombinatorialFunctions.partitions(0, new int[0]));
        TestUtils.assertEquals(0L, CombinatorialFunctions.partitions(3, new int[0]));
        TestUtils.assertEquals(0L, CombinatorialFunctions.partitions(4, new int[] { 1, 1 }));
        TestUtils.assertEquals(0L, CombinatorialFunctions.partitions(4, new int[] { -1, 5 }));

        TestUtils.assertThrows(ArithmeticException.class, () -> CombinatorialFunctions.partitions(100, new int[] { 50, 50 }));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> CombinatorialFunctions.partitions(-1, new int[0]));
    }

    @Test
    public void testPermutations() {

        for (int n = 0; n <= 25; n++) {
            BigInteger expected = CombinatorialFunctionsTest.factorial(n);
            int size = n;
            if (CombinatorialFunctionsTest.fits(expected)) {
                TestUtils.assertEquals(expected.longValueExact(), CombinatorialFunctions.permutations(size));
            } else {
                TestUtils.assertThrows(ArithmeticException.class, () -> CombinatorialFunctions.permutations(size));
            }
        }

        TestUtils.assertThrows(IllegalArgumentException.class, () -> CombinatorialFunctions.permutations(-1));
    }

    @Test
    public void testSubsets() {

        for (int n = 0; n <= 70; n++) {
            for (int k = 0; k <= n; k++) {
                BigInteger expected = CombinatorialFunctionsTest.binomial(n, k);
                int size = n;
                int subset = k;
                if (CombinatorialFunctionsTest.fits(expected)) {
                    TestUtils.assertEquals(expected.longValueExact(), CombinatorialFunctions.subsets(size, subset));
                } else {
                    TestUtils.assertThrows(ArithmeticException.class, () -> CombinatorialFunctions.subsets(size, subset));
                }
            }
        }

        TestUtils.assertEquals(1_313_400L, CombinatorialFunctions.subsets(200, 3));
        TestUtils.assertEquals(CombinatorialFunctionsTest.binomial(Integer.MAX_VALUE, 2).longValueExact(),
                CombinatorialFunctions.subsets(Integer.MAX_VALUE, 2));
        TestUtils.assertThrows(ArithmeticException.class, () -> CombinatorialFunctions.subsets(Integer.MAX_VALUE, 3));

        TestUtils.assertEquals(1L, CombinatorialFunctions.subsets(0, 0));
        TestUtils.assertEquals(0L, CombinatorialFunctions.subsets(5, -1));
        TestUtils.assertEquals(0L, CombinatorialFunctions.subsets(5, 6));

        TestUtils.assertThrows(IllegalArgumentException.class, () -> CombinatorialFunctions.subsets(-1, 0));
    }

    @Test
    public void testVariations() {

        for (int n = 0; n <= 30; n++) {
            BigInteger nFactorial = CombinatorialFunctionsTest.factorial(n);
            for (int k = 0; k <= n; k++) {
                BigInteger expected = nFactorial.divide(CombinatorialFunctionsTest.factorial(n - k));
                int size = n;
                int tuple = k;
                if (CombinatorialFunctionsTest.fits(expected)) {
                    TestUtils.assertEquals(expected.longValueExact(), CombinatorialFunctions.variations(size, tuple));
                } else {
                    TestUtils.assertThrows(ArithmeticException.class, () -> CombinatorialFunctions.variations(size, tuple));
                }
            }
        }

        TestUtils.assertEquals((long) Integer.MAX_VALUE * (Integer.MAX_VALUE - 1), CombinatorialFunctions.variations(Integer.MAX_VALUE, 2));

        TestUtils.assertEquals(1L, CombinatorialFunctions.variations(0, 0));
        TestUtils.assertEquals(0L, CombinatorialFunctions.variations(5, -1));
        TestUtils.assertEquals(0L, CombinatorialFunctions.variations(5, 6));

        TestUtils.assertThrows(IllegalArgumentException.class, () -> CombinatorialFunctions.variations(-1, 0));
    }

}

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
package org.ojalgo.constant;

public abstract class PrimitiveMath {

    public static final double ZERO = BigMath.ZERO.doubleValue();
    public static final double ONE = BigMath.ONE.doubleValue();
    public static final double TWO = BigMath.TWO.doubleValue();
    public static final double THREE = BigMath.THREE.doubleValue();
    public static final double FOUR = BigMath.FOUR.doubleValue();
    public static final double FIVE = BigMath.FIVE.doubleValue();
    public static final double SIX = BigMath.SIX.doubleValue();
    public static final double SEVEN = BigMath.SEVEN.doubleValue();
    public static final double EIGHT = BigMath.EIGHT.doubleValue();
    public static final double NINE = BigMath.NINE.doubleValue();
    public static final double TEN = BigMath.TEN.doubleValue();
    public static final double ELEVEN = BigMath.ELEVEN.doubleValue();
    public static final double TWELVE = BigMath.TWELVE.doubleValue();
    public static final double HUNDRED = BigMath.HUNDRED.doubleValue();
    public static final double THOUSAND = BigMath.THOUSAND.doubleValue();

    public static final double NEG = BigMath.NEG.doubleValue();

    public static final double HALF = BigMath.HALF.doubleValue();
    public static final double THIRD = BigMath.THIRD.doubleValue();
    public static final double QUARTER = BigMath.QUARTER.doubleValue();
    public static final double FITH = BigMath.FITH.doubleValue();
    public static final double SIXTH = BigMath.SIXTH.doubleValue();
    public static final double SEVENTH = BigMath.SEVENTH.doubleValue();
    public static final double EIGHTH = BigMath.EIGHTH.doubleValue();
    public static final double NINTH = BigMath.NINTH.doubleValue();
    public static final double TENTH = BigMath.TENTH.doubleValue();
    public static final double ELEVENTH = BigMath.ELEVENTH.doubleValue();
    public static final double TWELFTH = BigMath.TWELFTH.doubleValue();
    public static final double HUNDREDTH = BigMath.HUNDREDTH.doubleValue();
    public static final double THOUSANDTH = BigMath.THOUSANDTH.doubleValue();

    public static final double E = BigMath.E.doubleValue();
    public static final double PI = BigMath.PI.doubleValue();

    public static final double HALF_PI = BigMath.HALF_PI.doubleValue();
    public static final double TWO_PI = BigMath.TWO_PI.doubleValue();

    public static final double SQRT_TWO = BigMath.SQRT_TWO.doubleValue();
    public static final double SQRT_PI = BigMath.SQRT_PI.doubleValue();
    public static final double SQRT_TWO_PI = BigMath.SQRT_TWO_PI.doubleValue();

    /**
     * ≈ 1.6E-291
     *
     * @deprecated v37 Will at least be renamed
     */
    @Deprecated
    public static final double TINY = Math.pow(2.0, -966.0);
    /**
     * ≈ 3.33E-15
     *
     * @deprecated v37 Bad idea!
     */
    @Deprecated
    public static final double IS_ZERO = 1E-14 / THREE;
    public static final double POSITIVE_INFINITY = Double.POSITIVE_INFINITY;
    public static final double NEGATIVE_INFINITY = Double.NEGATIVE_INFINITY;
    public static final double NaN = Double.NaN;
    public static final double MACHINE_LARGEST = Double.MAX_VALUE;
    /**
     * Refers to "min normal" rather than "min value"
     */
    public static final double MACHINE_SMALLEST = Double.MIN_NORMAL;
    public static final double MACHINE_EPSILON = Math.pow(2.0, -52.0);
    private static final int[] PRIME = new int[] { 2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97, 101, 103,
            107, 109, 113, 127, 131, 137, 139, 149, 151, 157, 163, 167, 173, 179, 181, 191, 193, 197, 199, 211, 223, 227, 229, 233, 239, 241, 251, 257, 263,
            269, 271 };

    public static final int[] POWERS_OF_2 = new int[] { 1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192 };

    public static final int getPrimeNumber(final int index) {
        return PRIME[index];
    }

    private PrimitiveMath() {
        super();
    }

}

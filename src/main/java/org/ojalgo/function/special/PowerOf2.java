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

import java.util.Arrays;

import org.ojalgo.function.constant.PrimitiveMath;

public abstract class PowerOf2 {

    public static final class IntPower extends PowerOf2 {

        public final int value;
        private final int myModuloMask;

        IntPower(final int exp) {
            super(exp);
            value = INT_POWERS[exp];
            myModuloMask = value - 1;
        }

        public int divide(final int dividend) {
            return dividend >> exponent;
        }

        public int getModuloMask() {
            return myModuloMask;
        }

        public int modulo(final int dividend) {
            return dividend & myModuloMask;
        }

    }

    public static final class LongPower extends PowerOf2 {

        public final long value;
        private final long myModuloMask;

        LongPower(final int exp) {
            super(exp);
            value = LONG_POWERS[exp];
            myModuloMask = value - 1L;
        }

        public long divide(final long dividend) {
            return dividend >> exponent;
        }

        public long getModuloMask() {
            return myModuloMask;
        }

        public long modulo(final long dividend) {
            return dividend & myModuloMask;
        }

    }

    public static final int MAX_INT;
    public static final long MAX_LONG;

    private static final int[] INT_POWERS = new int[31];
    private static double LN2 = Math.log(PrimitiveMath.TWO);
    private static final long[] LONG_POWERS = new long[63];

    static {

        INT_POWERS[0] = 1;
        for (int p = 1; p < INT_POWERS.length; p++) {
            INT_POWERS[p] = INT_POWERS[p - 1] * 2;
        }
        MAX_INT = INT_POWERS[INT_POWERS.length - 1];

        LONG_POWERS[0] = 1L;
        for (int p = 1; p < LONG_POWERS.length; p++) {
            LONG_POWERS[p] = LONG_POWERS[p - 1] * 2L;
        }
        MAX_LONG = LONG_POWERS[LONG_POWERS.length - 1];
    }

    /**
     * @see PowerOf2#largestNotGreaterThan(long)
     */
    public static long adjustDown(final double value) {
        return PowerOf2.largestNotGreaterThan(Math.round(value));
    }

    /**
     * @see PowerOf2#largestNotGreaterThan(int)
     */
    public static int adjustDown(final float value) {
        return PowerOf2.largestNotGreaterThan(Math.round(value));
    }

    /**
     * @see PowerOf2#smallestNotLessThan(long)
     */
    public static long adjustUp(final double value) {
        return PowerOf2.smallestNotLessThan(Math.round(value));
    }

    /**
     * @see PowerOf2#smallestNotLessThan(int)
     */
    public static int adjustUp(final float value) {
        return PowerOf2.smallestNotLessThan(Math.round(value));
    }

    public static int exponent(final double value) {
        return MissingMath.roundToInt(Math.log(value) / LN2);
    }

    /**
     * If the input value is a power of 2 then the exponent is returned, if not a negative number is returned.
     */
    public static int find(final int value) {
        return Arrays.binarySearch(INT_POWERS, value);
    }

    /**
     * @see PowerOf2#find(int)
     */
    public static int find(final long value) {
        return Arrays.binarySearch(LONG_POWERS, value);
    }

    public static IntPower getIntPower(final int exponent) {
        return new IntPower(exponent);
    }

    public static LongPower getLongPower(final int exponent) {
        return new LongPower(exponent);
    }

    public static boolean isPowerOf2(final int value) {
        return value > 0 && (value & value - 1) == 0;
    }

    public static boolean isPowerOf2(final long value) {
        return value > 0L && (value & value - 1L) == 0L;
    }

    /**
     * @return The largest power of 2 that is less than or equal to the input (not greater than)
     */
    public static int largestNotGreaterThan(final int value) {
        if (value <= 0) {
            throw new ArithmeticException();
        } else {
            return Integer.highestOneBit(value);
        }
    }

    /**
     * @return The largest power of 2 that is less than or equal to the input (not greater than)
     */
    public static long largestNotGreaterThan(final long value) {
        if (value <= 0L) {
            throw new ArithmeticException();
        } else {
            return Long.highestOneBit(value);
        }
    }

    /**
     * @return The smallest integer exponent so that 2^exp &gt;= value.
     */
    public static int powerOf2Larger(final int value) {
        final int index = Arrays.binarySearch(INT_POWERS, value);
        return index >= 0 ? index : Math.min(-(index + 1), 30);
    }

    /**
     * @return The smallest integer exponent so that 2^exp &gt;= value.
     */
    public static int powerOf2Larger(final long value) {
        final int index = Arrays.binarySearch(LONG_POWERS, value);
        return index >= 0 ? index : Math.min(-(index + 1), 62);
    }

    /**
     * @return The largest integer exponent so that 2^exp &lt;= value.
     */
    public static int powerOf2Smaller(final int value) {
        final int index = Arrays.binarySearch(INT_POWERS, value);
        return index >= 0 ? index : Math.max(-(index + 2), 0);
    }

    /**
     * @return The largest integer exponent so that 2^exp &lt;= value.
     */
    public static int powerOf2Smaller(final long value) {
        final int index = Arrays.binarySearch(LONG_POWERS, value);
        return index >= 0 ? index : Math.max(-(index + 2), 0);
    }

    public static int powerOfInt2(final int exponent) {
        return INT_POWERS[exponent];
    }

    public static long powerOfLong2(final int exponent) {
        return LONG_POWERS[exponent];
    }

    /**
     * @return The smallest power of 2 that is greater than or equal to the input (not less than)
     */
    public static int smallestNotLessThan(final int value) {
        if (value < 0) {
            throw new ArithmeticException();
        } else if (value == 0) {
            return 1;
        } else {
            int candidate = Integer.highestOneBit(value);
            if (candidate == Integer.lowestOneBit(value)) {
                return candidate;
            } else {
                return candidate << 1;
            }
        }
    }

    /**
     * @return The smallest power of 2 that is greater than or equal to the input (not less than)
     */
    public static long smallestNotLessThan(final long value) {
        if (value < 0L) {
            throw new ArithmeticException();
        } else if (value == 0L) {
            return 1L;
        } else {
            long candidate = Long.highestOneBit(value);
            if (candidate == Long.lowestOneBit(value)) {
                return candidate;
            } else {
                return candidate << 1;
            }
        }
    }

    public final int exponent;

    PowerOf2(final int exp) {
        super();
        exponent = exp;
    }

}

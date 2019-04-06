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
package org.ojalgo.function.constant;

import java.util.Arrays;

import org.ojalgo.ProgrammingError;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.function.special.MissingMath;
import org.ojalgo.scalar.PrimitiveScalar;

public abstract class PrimitiveMath {

    public static class Prefix {

        public static final double YOCTO = BigMath.Prefix.YOCTO.doubleValue();
        public static final double ZEPTO = BigMath.Prefix.ZEPTO.doubleValue();
        public static final double ATTO = BigMath.Prefix.ATTO.doubleValue();
        public static final double FEMTO = BigMath.Prefix.FEMTO.doubleValue();
        public static final double PICO = BigMath.Prefix.PICO.doubleValue();
        public static final double NANO = BigMath.Prefix.NANO.doubleValue();
        public static final double MICRO = BigMath.Prefix.MICRO.doubleValue();
        public static final double MILLI = BigMath.Prefix.MILLI.doubleValue();
        public static final double CENTI = BigMath.Prefix.CENTI.doubleValue();
        public static final double DECI = BigMath.Prefix.DECI.doubleValue();
        public static final double DEKA = BigMath.Prefix.DEKA.doubleValue();
        public static final double HECTO = BigMath.Prefix.HECTO.doubleValue();
        public static final double KILO = BigMath.Prefix.KILO.doubleValue();
        public static final double MEGA = BigMath.Prefix.MEGA.doubleValue();
        public static final double GIGA = BigMath.Prefix.GIGA.doubleValue();
        public static final double TERA = BigMath.Prefix.TERA.doubleValue();
        public static final double PETA = BigMath.Prefix.PETA.doubleValue();
        public static final double EXA = BigMath.Prefix.EXA.doubleValue();
        public static final double ZETTA = BigMath.Prefix.ZETTA.doubleValue();
        public static final double YOTTA = BigMath.Prefix.YOTTA.doubleValue();

    }

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

    public static final double TWO_THIRDS = BigMath.TWO_THIRDS.doubleValue();
    public static final double THREE_QUARTERS = BigMath.THREE_QUARTERS.doubleValue();

    public static final double E = BigMath.E.doubleValue();
    public static final double PI = BigMath.PI.doubleValue();
    public static final double GOLDEN_RATIO = BigMath.GOLDEN_RATIO.doubleValue();

    public static final double HALF_PI = BigMath.HALF_PI.doubleValue();
    public static final double TWO_PI = BigMath.TWO_PI.doubleValue();

    public static final double SQRT_TWO = BigMath.SQRT_TWO.doubleValue();
    public static final double SQRT_PI = BigMath.SQRT_PI.doubleValue();
    public static final double SQRT_TWO_PI = BigMath.SQRT_TWO_PI.doubleValue();

    public static final double NaN = Double.NaN;
    public static final double POSITIVE_INFINITY = Double.POSITIVE_INFINITY;
    public static final double NEGATIVE_INFINITY = Double.NEGATIVE_INFINITY;
    public static final double MACHINE_LARGEST = Double.MAX_VALUE;
    /**
     * Refers to "min normal" rather than "min value"
     */
    public static final double MACHINE_SMALLEST = Double.MIN_NORMAL;
    public static final double MACHINE_EPSILON = Math.pow(2.0, -52.0);
    public static final double RELATIVELY_SMALL = Math.pow(2.0, -26.0);
    /**
     * ≈ 1.6E-291
     */
    @Deprecated
    public static final double TINY = Math.pow(2.0, -966.0);

    private static final int[] PRIME = new int[] { 2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97, 101, 103,
            107, 109, 113, 127, 131, 137, 139, 149, 151, 157, 163, 167, 173, 179, 181, 191, 193, 197, 199, 211, 223, 227, 229, 233, 239, 241, 251, 257, 263,
            269, 271 };

    public static final long[] POWERS_OF_2 = new long[63];
    static {
        POWERS_OF_2[0] = 1L;
        for (int p = 1; p < POWERS_OF_2.length; p++) {
            POWERS_OF_2[p] = POWERS_OF_2[p - 1] * 2L;
        }
    }

    public static final int getPrimeNumber(final int index) {
        return PRIME[index];
    }

    public static final boolean isPowerOf2(final long value) {
        return Arrays.binarySearch(POWERS_OF_2, value) >= 0;
    }

    /**
     * @return The smallest integer exponent so that 2^exp &gt;= value.
     */
    public static final int powerOf2Larger(final long value) {
        final int index = Arrays.binarySearch(POWERS_OF_2, value);
        return index >= 0 ? index : Math.min(-(index + 1), 62);
    }

    /**
     * @return The largest integer exponent so that 2^exp &lt;= value.
     */
    public static final int powerOf2Smaller(final long value) {
        final int index = Arrays.binarySearch(POWERS_OF_2, value);
        return index >= 0 ? index : Math.max(-(index + 2), 0);
    }

    public static final PrimitiveFunction.Unary ABS = new PrimitiveFunction.Unary() {
    
        public final double invoke(final double arg) {
            return Math.abs(arg);
        }
    
    };
    public static final PrimitiveFunction.Unary ACOS = new PrimitiveFunction.Unary() {
    
        public final double invoke(final double arg) {
            return Math.acos(arg);
        }
    
    };
    public static final PrimitiveFunction.Unary ACOSH = new PrimitiveFunction.Unary() {
    
        public final double invoke(final double arg) {
            return MissingMath.acosh(arg);
        }
    
    };
    public static final PrimitiveFunction.Binary ADD = new PrimitiveFunction.Binary() {
    
        @Override
        public final double invoke(final double arg1, final double arg2) {
            return arg1 + arg2;
        }
    
    };
    public static final PrimitiveFunction.Unary ASIN = new PrimitiveFunction.Unary() {
    
        public final double invoke(final double arg) {
            return Math.asin(arg);
        }
    
    };
    public static final PrimitiveFunction.Unary ASINH = new PrimitiveFunction.Unary() {
    
        public final double invoke(final double arg) {
            return MissingMath.asinh(arg);
        }
    
    };
    public static final PrimitiveFunction.Unary ATAN = new PrimitiveFunction.Unary() {
    
        public final double invoke(final double arg) {
            return Math.atan(arg);
        }
    
    };
    public static final PrimitiveFunction.Binary ATAN2 = new PrimitiveFunction.Binary() {
    
        public final double invoke(final double arg1, final double arg2) {
            return Math.atan2(arg1, arg2);
        }
    
    };
    public static final PrimitiveFunction.Unary ATANH = new PrimitiveFunction.Unary() {
    
        public final double invoke(final double arg) {
            return MissingMath.atanh(arg);
        }
    
    };
    public static final PrimitiveFunction.Unary CARDINALITY = new PrimitiveFunction.Unary() {
    
        public final double invoke(final double arg) {
            return PrimitiveScalar.isSmall(ONE, arg) ? ZERO : ONE;
        }
    
    };
    public static final PrimitiveFunction.Unary CBRT = new PrimitiveFunction.Unary() {
    
        public final double invoke(final double arg) {
            return Math.cbrt(arg);
        }
    
    };
    public static final PrimitiveFunction.Unary CEIL = new PrimitiveFunction.Unary() {
    
        public final double invoke(final double arg) {
            return Math.ceil(arg);
        }
    
    };
    public static final PrimitiveFunction.Unary CONJUGATE = new PrimitiveFunction.Unary() {
    
        public final double invoke(final double arg) {
            return arg;
        }
    
    };
    public static final PrimitiveFunction.Unary COS = new PrimitiveFunction.Unary() {
    
        public final double invoke(final double arg) {
            return Math.cos(arg);
        }
    
    };
    public static final PrimitiveFunction.Unary COSH = new PrimitiveFunction.Unary() {
    
        public final double invoke(final double arg) {
            return Math.cosh(arg);
        }
    
    };
    public static final PrimitiveFunction.Binary DIVIDE = new PrimitiveFunction.Binary() {
    
        @Override
        public final double invoke(final double arg1, final double arg2) {
            return arg1 / arg2;
        }
    
    };
    public static final PrimitiveFunction.Unary EXP = new PrimitiveFunction.Unary() {
    
        public final double invoke(final double arg) {
            return Math.exp(arg);
        }
    
    };
    public static final PrimitiveFunction.Unary EXPM1 = new PrimitiveFunction.Unary() {
    
        public final double invoke(final double arg) {
            return Math.expm1(arg);
        }
    
    };
    public static final PrimitiveFunction.Unary FLOOR = new PrimitiveFunction.Unary() {
    
        public final double invoke(final double arg) {
            return Math.floor(arg);
        }
    
    };
    public static final PrimitiveFunction.Binary HYPOT = new PrimitiveFunction.Binary() {
    
        @Override
        public final double invoke(final double arg1, final double arg2) {
    
            if (Double.isNaN(arg1) || Double.isNaN(arg2)) {
                return NaN;
            }
    
            final double abs1 = ABS.invoke(arg1);
            final double abs2 = ABS.invoke(arg2);
    
            double retVal = ZERO;
    
            if (abs1 > abs2) {
                retVal = abs1 * SQRT1PX2.invoke(abs2 / abs1);
            } else if (abs2 > ZERO) {
                retVal = abs2 * SQRT1PX2.invoke(abs1 / abs2);
            }
    
            return retVal;
        }
    
    };
    public static final PrimitiveFunction.Unary INVERT = new PrimitiveFunction.Unary() {
    
        public final double invoke(final double arg) {
            return ONE / arg;
        }
    
    };
    public static final PrimitiveFunction.Unary LOG = new PrimitiveFunction.Unary() {
    
        public final double invoke(final double arg) {
            return Math.log(arg);
        }
    
    };
    public static final PrimitiveFunction.Unary LOG10 = new PrimitiveFunction.Unary() {
    
        public final double invoke(final double arg) {
            return Math.log10(arg);
        }
    
    };
    public static final PrimitiveFunction.Unary LOG1P = new PrimitiveFunction.Unary() {
    
        public final double invoke(final double arg) {
            return Math.log1p(arg);
        }
    
    };
    public static final PrimitiveFunction.Unary LOGISTIC = new PrimitiveFunction.Unary() {
    
        public final double invoke(final double arg) {
            return MissingMath.logistic(arg);
        }
    
    };
    public static final PrimitiveFunction.Unary LOGIT = new PrimitiveFunction.Unary() {
    
        public final double invoke(final double arg) {
            return MissingMath.logit(arg);
        }
    
    };
    public static final PrimitiveFunction.Binary MAX = new PrimitiveFunction.Binary() {
    
        @Override
        public final double invoke(final double arg1, final double arg2) {
            return Math.max(arg1, arg2);
        }
    
    };
    public static final PrimitiveFunction.Binary MIN = new PrimitiveFunction.Binary() {
    
        @Override
        public final double invoke(final double arg1, final double arg2) {
            return Math.min(arg1, arg2);
        }
    
    };
    public static final PrimitiveFunction.Binary MULTIPLY = new PrimitiveFunction.Binary() {
    
        @Override
        public final double invoke(final double arg1, final double arg2) {
            return arg1 * arg2;
        }
    
    };
    public static final PrimitiveFunction.Unary NEGATE = new PrimitiveFunction.Unary() {
    
        public final double invoke(final double arg) {
            return -arg;
        }
    
    };
    public static final PrimitiveFunction.Binary POW = new PrimitiveFunction.Binary() {
    
        @Override
        public final double invoke(final double arg1, final double arg2) {
            return Math.pow(arg1, arg2);
        }
    
    };
    public static final PrimitiveFunction.Parameter POWER = new PrimitiveFunction.Parameter() {
    
        @Override
        public final double invoke(final double arg, int param) {
    
            if (param < 0) {
    
                return INVERT.invoke(POWER.invoke(arg, -param));
    
            } else {
    
                double retVal = ONE;
    
                while (param > 0) {
                    retVal = retVal * arg;
                    param--;
                }
    
                return retVal;
            }
        }
    
    };
    public static final PrimitiveFunction.Unary RINT = new PrimitiveFunction.Unary() {
    
        public final double invoke(final double arg) {
            return Math.rint(arg);
        }
    
    };
    public static final PrimitiveFunction.Parameter ROOT = new PrimitiveFunction.Parameter() {
    
        @Override
        public final double invoke(final double arg, final int param) {
    
            if (param != 0) {
                return POW.invoke(arg, ONE / param);
            } else {
                throw new IllegalArgumentException();
            }
        }
    
    };
    public static final PrimitiveFunction.Parameter SCALE = new PrimitiveFunction.Parameter() {
    
        @Override
        public final double invoke(final double arg, int param) {
    
            if (param < 0) {
                throw new ProgrammingError("Cannot have exponents smaller than zero.");
            }
    
            long tmpFactor = 1l;
            final long tmp10 = (long) TEN;
    
            while (param > 0) {
                tmpFactor *= tmp10;
                param--;
            }
    
            return RINT.invoke(tmpFactor * arg) / tmpFactor;
        }
    
    };
    public static final PrimitiveFunction.Unary SIGNUM = new PrimitiveFunction.Unary() {
    
        public final double invoke(final double arg) {
            return Math.signum(arg);
        }
    
    };
    public static final PrimitiveFunction.Unary SIN = new PrimitiveFunction.Unary() {
    
        public final double invoke(final double arg) {
            return Math.sin(arg);
        }
    
    };
    public static final PrimitiveFunction.Unary SINH = new PrimitiveFunction.Unary() {
    
        public final double invoke(final double arg) {
            return Math.sinh(arg);
        }
    
    };
    public static final PrimitiveFunction.Unary SQRT = new PrimitiveFunction.Unary() {
    
        public final double invoke(final double arg) {
            return Math.sqrt(arg);
        }
    
    };
    public static final PrimitiveFunction.Unary SQRT1PX2 = new PrimitiveFunction.Unary() {
    
        public final double invoke(final double arg) {
            return SQRT.invoke(ONE + (arg * arg));
        }
    
    };
    public static final PrimitiveFunction.Binary SUBTRACT = new PrimitiveFunction.Binary() {
    
        @Override
        public final double invoke(final double arg1, final double arg2) {
            return arg1 - arg2;
        }
    
    };
    public static final PrimitiveFunction.Unary TAN = new PrimitiveFunction.Unary() {
    
        public final double invoke(final double arg) {
            return Math.tan(arg);
        }
    
    };
    public static final PrimitiveFunction.Unary TANH = new PrimitiveFunction.Unary() {
    
        public final double invoke(final double arg) {
            return Math.tanh(arg);
        }
    
    };
    public static final PrimitiveFunction.Unary VALUE = new PrimitiveFunction.Unary() {
    
        public final double invoke(final double arg) {
            return arg;
        }
    
    };

}

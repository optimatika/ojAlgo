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

import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.function.special.MissingMath;
import org.ojalgo.function.special.PowerOf2;
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

    /*
     * The lambdas below should not (cannot) reference each other. Implementations must be written in terms of
     * java.lang.Math and/or org.ojalgo.function.special.MissingMath.
     */

    public static final PrimitiveFunction.Unary ABS = arg -> Math.abs(arg);
    public static final PrimitiveFunction.Unary ACOS = arg -> Math.acos(arg);
    public static final PrimitiveFunction.Unary ACOSH = arg -> MissingMath.acosh(arg);
    public static final PrimitiveFunction.Binary ADD = (arg1, arg2) -> arg1 + arg2;
    public static final PrimitiveFunction.Unary ASIN = arg -> Math.asin(arg);
    public static final PrimitiveFunction.Unary ASINH = arg -> MissingMath.asinh(arg);
    public static final PrimitiveFunction.Unary ATAN = arg -> Math.atan(arg);
    public static final PrimitiveFunction.Binary ATAN2 = (arg1, arg2) -> Math.atan2(arg1, arg2);
    public static final PrimitiveFunction.Unary ATANH = arg -> MissingMath.atanh(arg);
    public static final PrimitiveFunction.Unary CARDINALITY = arg -> PrimitiveScalar.isSmall(ONE, arg) ? ZERO : ONE;
    public static final PrimitiveFunction.Unary CBRT = arg -> Math.cbrt(arg);
    public static final PrimitiveFunction.Unary CEIL = arg -> Math.ceil(arg);
    public static final PrimitiveFunction.Unary CONJUGATE = arg -> arg;
    public static final PrimitiveFunction.Unary COS = arg -> Math.cos(arg);
    public static final PrimitiveFunction.Unary COSH = arg -> Math.cosh(arg);
    public static final PrimitiveFunction.Binary DIVIDE = (arg1, arg2) -> arg1 / arg2;
    public static final PrimitiveFunction.Unary EXP = arg -> Math.exp(arg);
    public static final PrimitiveFunction.Unary EXPM1 = arg -> Math.expm1(arg);
    public static final PrimitiveFunction.Unary FLOOR = arg -> Math.floor(arg);
    public static final PrimitiveFunction.Binary HYPOT = (arg1, arg2) -> MissingMath.hypot(arg1, arg2);
    public static final PrimitiveFunction.Unary INVERT = arg -> ONE / arg;
    public static final PrimitiveFunction.Unary LOG = arg -> Math.log(arg);
    public static final PrimitiveFunction.Unary LOG10 = arg -> Math.log10(arg);
    public static final PrimitiveFunction.Unary LOG1P = arg -> Math.log1p(arg);
    public static final PrimitiveFunction.Unary LOGISTIC = arg -> MissingMath.logistic(arg);
    public static final PrimitiveFunction.Unary LOGIT = arg -> MissingMath.logit(arg);
    public static final PrimitiveFunction.Binary MAX = (arg1, arg2) -> Math.max(arg1, arg2);
    public static final PrimitiveFunction.Binary MIN = (arg1, arg2) -> Math.min(arg1, arg2);
    public static final PrimitiveFunction.Binary MULTIPLY = (arg1, arg2) -> arg1 * arg2;
    public static final PrimitiveFunction.Unary NEGATE = arg -> -arg;
    public static final PrimitiveFunction.Binary POW = (arg1, arg2) -> Math.pow(arg1, arg2);
    public static final PrimitiveFunction.Parameter POWER = (arg, param) -> MissingMath.power(arg, param);
    public static final PrimitiveFunction.Unary RINT = arg -> Math.rint(arg);
    public static final PrimitiveFunction.Parameter ROOT = (arg, param) -> MissingMath.root(arg, param);
    public static final PrimitiveFunction.Parameter SCALE = (arg, param) -> MissingMath.scale(arg, param);
    public static final PrimitiveFunction.Unary SIGNUM = arg -> Math.signum(arg);
    public static final PrimitiveFunction.Unary SIN = arg -> Math.sin(arg);
    public static final PrimitiveFunction.Unary SINH = arg -> Math.sinh(arg);
    public static final PrimitiveFunction.Unary SQRT = arg -> Math.sqrt(arg);
    public static final PrimitiveFunction.Unary SQRT1PX2 = arg -> MissingMath.sqrt1px2(arg);
    public static final PrimitiveFunction.Binary SUBTRACT = (arg1, arg2) -> arg1 - arg2;
    public static final PrimitiveFunction.Unary TAN = arg -> Math.tan(arg);
    public static final PrimitiveFunction.Unary TANH = arg -> Math.tanh(arg);
    public static final PrimitiveFunction.Unary VALUE = arg -> arg;

    public static final int getPrimeNumber(final int index) {
        return PRIME[index];
    }

    /**
     * @deprecated v48 Use {@link PowerOf2#isPowerOf2(long)} instead
     */
    @Deprecated
    public static final boolean isPowerOf2(final long value) {
        return PowerOf2.isPowerOf2(value);
    }

    /**
     * @return The smallest integer exponent so that 2^exp &gt;= value.
     * @deprecated v48 Use {@link PowerOf2#powerOf2Larger(long)} instead
     */
    @Deprecated
    public static final int powerOf2Larger(final long value) {
        return PowerOf2.powerOf2Larger(value);
    }

    /**
     * @return The largest integer exponent so that 2^exp &lt;= value.
     * @deprecated v48 Use {@link PowerOf2#powerOf2Smaller(long)} instead
     */
    @Deprecated
    public static final int powerOf2Smaller(final long value) {
        return PowerOf2.powerOf2Smaller(value);
    }

}

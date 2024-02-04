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
package org.ojalgo.function.constant;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.ojalgo.function.BigFunction;
import org.ojalgo.function.special.MissingMath;

public abstract class BigMath {

    public static class Prefix {

        public static final BigDecimal YOCTO = new BigDecimal("0.000000000000000000000001");
        public static final BigDecimal ZEPTO = new BigDecimal("0.000000000000000000001");
        public static final BigDecimal ATTO = new BigDecimal("0.000000000000000001");
        public static final BigDecimal FEMTO = new BigDecimal("0.000000000000001");
        public static final BigDecimal PICO = new BigDecimal("0.000000000001");
        public static final BigDecimal NANO = new BigDecimal("0.000000001");
        public static final BigDecimal MICRO = new BigDecimal("0.000001");
        public static final BigDecimal MILLI = new BigDecimal("0.001");
        public static final BigDecimal CENTI = new BigDecimal("0.01");
        public static final BigDecimal DECI = new BigDecimal("0.1");
        public static final BigDecimal DEKA = new BigDecimal("10");
        public static final BigDecimal HECTO = new BigDecimal("100");
        public static final BigDecimal KILO = new BigDecimal("1000");
        public static final BigDecimal MEGA = new BigDecimal("1000000");
        public static final BigDecimal GIGA = new BigDecimal("1000000000");
        public static final BigDecimal TERA = new BigDecimal("1000000000000");
        public static final BigDecimal PETA = new BigDecimal("1000000000000000");
        public static final BigDecimal EXA = new BigDecimal("1000000000000000000");
        public static final BigDecimal ZETTA = new BigDecimal("1000000000000000000000");
        public static final BigDecimal YOTTA = new BigDecimal("1000000000000000000000000");

    }

    public static final BigDecimal ZERO = new BigDecimal("0");
    public static final BigDecimal ONE = new BigDecimal("1");
    public static final BigDecimal TWO = new BigDecimal("2");
    public static final BigDecimal THREE = new BigDecimal("3");
    public static final BigDecimal FOUR = new BigDecimal("4");
    public static final BigDecimal FIVE = new BigDecimal("5");
    public static final BigDecimal SIX = new BigDecimal("6");
    public static final BigDecimal SEVEN = new BigDecimal("7");
    public static final BigDecimal EIGHT = new BigDecimal("8");
    public static final BigDecimal NINE = new BigDecimal("9");
    public static final BigDecimal TEN = new BigDecimal("10");
    public static final BigDecimal ELEVEN = new BigDecimal("11");
    public static final BigDecimal TWELVE = new BigDecimal("12");
    public static final BigDecimal HUNDRED = new BigDecimal("100");
    public static final BigDecimal THOUSAND = new BigDecimal("1000");

    public static final BigDecimal NEG = ONE.negate();

    public static final BigDecimal HALF = MissingMath.divide(ONE, TWO);
    public static final BigDecimal THIRD = MissingMath.divide(ONE, THREE);
    public static final BigDecimal QUARTER = MissingMath.divide(ONE, FOUR);
    public static final BigDecimal FITH = MissingMath.divide(ONE, FIVE);
    public static final BigDecimal SIXTH = MissingMath.divide(ONE, SIX);
    public static final BigDecimal SEVENTH = MissingMath.divide(ONE, SEVEN);
    public static final BigDecimal EIGHTH = MissingMath.divide(ONE, EIGHT);
    public static final BigDecimal NINTH = MissingMath.divide(ONE, NINE);
    public static final BigDecimal TENTH = MissingMath.divide(ONE, TEN);
    public static final BigDecimal ELEVENTH = MissingMath.divide(ONE, ELEVEN);
    public static final BigDecimal TWELFTH = MissingMath.divide(ONE, TWELVE);
    public static final BigDecimal HUNDREDTH = MissingMath.divide(ONE, HUNDRED);
    public static final BigDecimal THOUSANDTH = MissingMath.divide(ONE, THOUSAND);

    public static final BigDecimal TWO_THIRDS = MissingMath.divide(TWO, THREE);
    public static final BigDecimal THREE_QUARTERS = MissingMath.divide(THREE, FOUR);

    public static final BigDecimal E = new BigDecimal("2.71828182845904523536028747135266249775724709369996");
    public static final BigDecimal PI = new BigDecimal("3.14159265358979323846264338327950288419716939937511");
    public static final BigDecimal GOLDEN_RATIO = new BigDecimal("1.61803398874989484820458683436563811772030917980576");

    public static final BigDecimal HALF_PI = HALF.multiply(PI);
    public static final BigDecimal TWO_PI = TWO.multiply(PI);

    public static final BigDecimal SQRT_TWO = MissingMath.root(TWO, 2);
    public static final BigDecimal SQRT_PI = MissingMath.root(PI, 2);
    public static final BigDecimal SQRT_TWO_PI = MissingMath.root(TWO_PI, 2);

    /**
     * Corresponding to {@link Long#MIN_VALUE}.
     */
    public static final BigDecimal VERY_NEGATIVE = new BigDecimal(Long.MIN_VALUE);
    /**
     * Corresponding to {@link Long#MAX_VALUE}.
     */
    public static final BigDecimal VERY_POSITIVE = new BigDecimal(Long.MAX_VALUE);

    /**
     * The smallest possible number that will be translated to positive infinity when converted to a primitive
     * double (something close to that at least). The key feature is that it is translated to
     * {@link Double#POSITIVE_INFINITY}.
     */
    public static final BigDecimal SMALLEST_POSITIVE_INFINITY = new BigDecimal(Double.MAX_VALUE).add(new BigDecimal(Double.MAX_VALUE).movePointLeft(16));
    /**
     * {@link #SMALLEST_POSITIVE_INFINITY} negated – is translated to {@link Double#NEGATIVE_INFINITY}.
     */
    public static final BigDecimal SMALLEST_NEGATIVE_INFINITY = SMALLEST_POSITIVE_INFINITY.negate();

    /*
     * The lambdas below should not (cannot) reference each other. Delegate to some other 'type' or code in
     * org.ojalgo.function.special.MissingMath.
     */

    public static final BigFunction.Unary ABS = BigDecimal::abs;
    public static final BigFunction.Unary ACOS = arg -> BigDecimal.valueOf(Math.acos(arg.doubleValue()));
    public static final BigFunction.Unary ACOSH = arg -> BigDecimal.valueOf(MissingMath.acosh(arg.doubleValue()));
    public static final BigFunction.Binary ADD = BigDecimal::add;
    public static final BigFunction.Unary ASIN = arg -> BigDecimal.valueOf(Math.asin(arg.doubleValue()));
    public static final BigFunction.Unary ASINH = arg -> BigDecimal.valueOf(MissingMath.asinh(arg.doubleValue()));
    public static final BigFunction.Unary ATAN = arg -> BigDecimal.valueOf(Math.atan(arg.doubleValue()));
    public static final BigFunction.Binary ATAN2 = (arg1, arg2) -> BigDecimal.valueOf(Math.atan2(arg1.doubleValue(), arg2.doubleValue()));
    public static final BigFunction.Unary ATANH = arg -> BigDecimal.valueOf(MissingMath.atanh(arg.doubleValue()));
    public static final BigFunction.Unary CARDINALITY = arg -> arg.signum() == 0 ? ZERO : ONE;
    public static final BigFunction.Unary CBRT = arg -> MissingMath.root(arg, 3);
    public static final BigFunction.Unary CEIL = arg -> arg.setScale(0, RoundingMode.CEILING);
    public static final BigFunction.Unary CONJUGATE = arg -> arg;
    public static final BigFunction.Unary COS = arg -> BigDecimal.valueOf(Math.cos(arg.doubleValue()));
    public static final BigFunction.Unary COSH = arg -> BigDecimal.valueOf(Math.cosh(arg.doubleValue()));
    public static final BigFunction.Binary DIVIDE = MissingMath::divide;
    public static final BigFunction.Unary EXP = arg -> BigDecimal.valueOf(Math.exp(arg.doubleValue()));
    public static final BigFunction.Unary EXPM1 = arg -> BigDecimal.valueOf(Math.expm1(arg.doubleValue()));
    public static final BigFunction.Unary FLOOR = arg -> arg.setScale(0, RoundingMode.FLOOR);
    public static final BigFunction.Binary HYPOT = MissingMath::hypot;
    public static final BigFunction.Unary INVERT = arg -> MissingMath.divide(ONE, arg);
    public static final BigFunction.Unary LOG = arg -> BigDecimal.valueOf(Math.log(arg.doubleValue()));
    public static final BigFunction.Unary LOG10 = arg -> BigDecimal.valueOf(Math.log10(arg.doubleValue()));
    public static final BigFunction.Unary LOG1P = arg -> BigDecimal.valueOf(Math.log1p(arg.doubleValue()));
    public static final BigFunction.Unary LOGISTIC = arg -> BigDecimal.valueOf(MissingMath.logistic(arg.doubleValue()));
    public static final BigFunction.Unary LOGIT = arg -> BigDecimal.valueOf(MissingMath.logit(arg.doubleValue()));
    public static final BigFunction.Binary MAX = BigDecimal::max;
    public static final BigFunction.Binary MIN = BigDecimal::min;
    public static final BigFunction.Binary MULTIPLY = BigDecimal::multiply;
    public static final BigFunction.Unary NEGATE = BigDecimal::negate;
    public static final BigFunction.Binary POW = MissingMath::pow;
    public static final BigFunction.Parameter POWER = MissingMath::power;
    public static final BigFunction.Unary RINT = arg -> arg.setScale(0, RoundingMode.HALF_EVEN);
    public static final BigFunction.Parameter ROOT = MissingMath::root;
    public static final BigFunction.Parameter SCALE = (arg, param) -> arg.setScale(param, RoundingMode.HALF_EVEN);
    public static final BigFunction.Unary SIGNUM = MissingMath::signum;
    public static final BigFunction.Unary SIN = arg -> BigDecimal.valueOf(Math.sin(arg.doubleValue()));
    public static final BigFunction.Unary SINH = arg -> BigDecimal.valueOf(Math.sinh(arg.doubleValue()));
    public static final BigFunction.Unary SQRT = arg -> MissingMath.root(arg, 2);
    public static final BigFunction.Unary SQRT1PX2 = arg -> MissingMath.root(ONE.add(arg.multiply(arg)), 2);
    public static final BigFunction.Binary SUBTRACT = BigDecimal::subtract;
    public static final BigFunction.Unary TAN = arg -> BigDecimal.valueOf(Math.tan(arg.doubleValue()));
    public static final BigFunction.Unary TANH = MissingMath::tanh;
    public static final BigFunction.Unary VALUE = arg -> arg;

}

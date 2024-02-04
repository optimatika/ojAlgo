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

import org.ojalgo.function.QuadrupleFunction;
import org.ojalgo.scalar.Quadruple;

public abstract class QuadrupleMath {

    public static class Prefix {

        public static final Quadruple YOCTO = Quadruple.valueOf(BigMath.Prefix.YOCTO);
        public static final Quadruple ZEPTO = Quadruple.valueOf(BigMath.Prefix.ZEPTO);
        public static final Quadruple ATTO = Quadruple.valueOf(BigMath.Prefix.ATTO);
        public static final Quadruple FEMTO = Quadruple.valueOf(BigMath.Prefix.FEMTO);
        public static final Quadruple PICO = Quadruple.valueOf(BigMath.Prefix.PICO);
        public static final Quadruple NANO = Quadruple.valueOf(BigMath.Prefix.NANO);
        public static final Quadruple MICRO = Quadruple.valueOf(BigMath.Prefix.MICRO);
        public static final Quadruple MILLI = Quadruple.valueOf(BigMath.Prefix.MILLI);
        public static final Quadruple CENTI = Quadruple.valueOf(BigMath.Prefix.CENTI);
        public static final Quadruple DECI = Quadruple.valueOf(BigMath.Prefix.DECI);
        public static final Quadruple DEKA = Quadruple.valueOf(BigMath.Prefix.DEKA);
        public static final Quadruple HECTO = Quadruple.valueOf(BigMath.Prefix.HECTO);
        public static final Quadruple KILO = Quadruple.valueOf(BigMath.Prefix.KILO);
        public static final Quadruple MEGA = Quadruple.valueOf(BigMath.Prefix.MEGA);
        public static final Quadruple GIGA = Quadruple.valueOf(BigMath.Prefix.GIGA);
        public static final Quadruple TERA = Quadruple.valueOf(BigMath.Prefix.TERA);
        public static final Quadruple PETA = Quadruple.valueOf(BigMath.Prefix.PETA);
        public static final Quadruple EXA = Quadruple.valueOf(BigMath.Prefix.EXA);
        public static final Quadruple ZETTA = Quadruple.valueOf(BigMath.Prefix.ZETTA);
        public static final Quadruple YOTTA = Quadruple.valueOf(BigMath.Prefix.YOTTA);

    }

    public static final Quadruple ZERO = Quadruple.valueOf(BigMath.ZERO);
    public static final Quadruple ONE = Quadruple.valueOf(BigMath.ONE);
    public static final Quadruple TWO = Quadruple.valueOf(BigMath.TWO);
    public static final Quadruple THREE = Quadruple.valueOf(BigMath.THREE);
    public static final Quadruple FOUR = Quadruple.valueOf(BigMath.FOUR);
    public static final Quadruple FIVE = Quadruple.valueOf(BigMath.FIVE);
    public static final Quadruple SIX = Quadruple.valueOf(BigMath.SIX);
    public static final Quadruple SEVEN = Quadruple.valueOf(BigMath.SEVEN);
    public static final Quadruple EIGHT = Quadruple.valueOf(BigMath.EIGHT);
    public static final Quadruple NINE = Quadruple.valueOf(BigMath.NINE);
    public static final Quadruple TEN = Quadruple.valueOf(BigMath.TEN);
    public static final Quadruple ELEVEN = Quadruple.valueOf(BigMath.ELEVEN);
    public static final Quadruple TWELVE = Quadruple.valueOf(BigMath.TWELVE);
    public static final Quadruple HUNDRED = Quadruple.valueOf(BigMath.HUNDRED);
    public static final Quadruple THOUSAND = Quadruple.valueOf(BigMath.THOUSAND);

    public static final Quadruple NEG = Quadruple.valueOf(BigMath.NEG);

    public static final Quadruple HALF = Quadruple.valueOf(BigMath.HALF);
    public static final Quadruple THIRD = Quadruple.valueOf(BigMath.THIRD);
    public static final Quadruple QUARTER = Quadruple.valueOf(BigMath.QUARTER);
    public static final Quadruple FITH = Quadruple.valueOf(BigMath.FITH);
    public static final Quadruple SIXTH = Quadruple.valueOf(BigMath.SIXTH);
    public static final Quadruple SEVENTH = Quadruple.valueOf(BigMath.SEVENTH);
    public static final Quadruple EIGHTH = Quadruple.valueOf(BigMath.EIGHTH);
    public static final Quadruple NINTH = Quadruple.valueOf(BigMath.NINTH);
    public static final Quadruple TENTH = Quadruple.valueOf(BigMath.TENTH);
    public static final Quadruple ELEVENTH = Quadruple.valueOf(BigMath.ELEVENTH);
    public static final Quadruple TWELFTH = Quadruple.valueOf(BigMath.TWELFTH);
    public static final Quadruple HUNDREDTH = Quadruple.valueOf(BigMath.HUNDREDTH);
    public static final Quadruple THOUSANDTH = Quadruple.valueOf(BigMath.THOUSANDTH);

    public static final Quadruple TWO_THIRDS = Quadruple.valueOf(BigMath.TWO_THIRDS);
    public static final Quadruple THREE_QUARTERS = Quadruple.valueOf(BigMath.THREE_QUARTERS);

    public static final Quadruple E = Quadruple.valueOf(BigMath.E);
    public static final Quadruple PI = Quadruple.valueOf(BigMath.PI);
    public static final Quadruple GOLDEN_RATIO = Quadruple.valueOf(BigMath.GOLDEN_RATIO);

    public static final Quadruple HALF_PI = Quadruple.valueOf(BigMath.HALF_PI);
    public static final Quadruple TWO_PI = Quadruple.valueOf(BigMath.TWO_PI);

    public static final Quadruple SQRT_TWO = Quadruple.valueOf(BigMath.SQRT_TWO);
    public static final Quadruple SQRT_PI = Quadruple.valueOf(BigMath.SQRT_PI);
    public static final Quadruple SQRT_TWO_PI = Quadruple.valueOf(BigMath.SQRT_TWO_PI);

    public static final Quadruple NaN = Quadruple.NaN;
    public static final Quadruple POSITIVE_INFINITY = Quadruple.POSITIVE_INFINITY;
    public static final Quadruple NEGATIVE_INFINITY = Quadruple.NEGATIVE_INFINITY;

    /*
     * The lambdas below should not (cannot) reference each other. Delegate to some other 'type' or code in
     * org.ojalgo.function.special.MissingMath.
     */

    public static final QuadrupleFunction.Unary ABS = arg -> ((arg.compareTo(Quadruple.ZERO) < 0) ? arg.negate() : arg);
    public static final QuadrupleFunction.Unary ACOS = arg -> Quadruple.valueOf(BigMath.ACOS.invoke(arg.toBigDecimal()));
    public static final QuadrupleFunction.Unary ACOSH = arg -> QuadrupleMath.LOG
            .invoke(arg.add(QuadrupleMath.SQRT.invoke(arg.multiply(arg).subtract(Quadruple.ONE))));
    public static final QuadrupleFunction.Binary ADD = Quadruple::add;
    public static final QuadrupleFunction.Unary ASIN = arg -> Quadruple.valueOf(BigMath.ASIN.invoke(arg.toBigDecimal()));
    public static final QuadrupleFunction.Unary ASINH = arg -> QuadrupleMath.LOG
            .invoke(arg.add(QuadrupleMath.SQRT.invoke(arg.multiply(arg).add(Quadruple.ONE))));
    public static final QuadrupleFunction.Unary ATAN = arg -> Quadruple.valueOf(BigMath.ATAN.invoke(arg.toBigDecimal()));
    public static final QuadrupleFunction.Binary ATAN2 = (arg1, arg2) -> Quadruple.valueOf(BigMath.ATAN2.invoke(arg1.toBigDecimal(), arg2.toBigDecimal()));
    public static final QuadrupleFunction.Unary ATANH = arg -> QuadrupleMath.LOG.invoke(arg.add(Quadruple.ONE).divide(Quadruple.ONE.subtract(arg)))
            .divide(Quadruple.TWO);
    public static final QuadrupleFunction.Unary CARDINALITY = arg -> arg.compareTo(Quadruple.ZERO) == 0 ? Quadruple.ZERO : Quadruple.ONE;
    public static final QuadrupleFunction.Unary CBRT = arg -> Quadruple.valueOf(BigMath.CBRT.invoke(arg.toBigDecimal()));
    public static final QuadrupleFunction.Unary CEIL = arg -> Quadruple.valueOf(BigMath.CEIL.invoke(arg.toBigDecimal()));
    public static final QuadrupleFunction.Unary CONJUGATE = Quadruple::conjugate;
    public static final QuadrupleFunction.Unary COS = arg -> Quadruple.valueOf(BigMath.COS.invoke(arg.toBigDecimal()));
    public static final QuadrupleFunction.Unary COSH = arg -> QuadrupleMath.EXP.invoke(arg).add(QuadrupleMath.EXP.invoke(arg.negate())).divide(Quadruple.TWO);
    public static final QuadrupleFunction.Binary DIVIDE = (arg1, arg2) -> arg1.divide(arg2);
    public static final QuadrupleFunction.Unary EXP = arg -> Quadruple.valueOf(BigMath.EXP.invoke(arg.toBigDecimal()));
    public static final QuadrupleFunction.Unary EXPM1 = arg -> Quadruple.valueOf(BigMath.EXPM1.invoke(arg.toBigDecimal()));
    public static final QuadrupleFunction.Unary FLOOR = arg -> Quadruple.valueOf(BigMath.FLOOR.invoke(arg.toBigDecimal()));
    public static final QuadrupleFunction.Binary HYPOT = (arg1, arg2) -> Quadruple.valueOf(BigMath.HYPOT.invoke(arg1.toBigDecimal(), arg2.toBigDecimal()));
    public static final QuadrupleFunction.Unary INVERT = Quadruple::invert;
    public static final QuadrupleFunction.Unary LOG = arg -> Quadruple.valueOf(BigMath.LOG.invoke(arg.toBigDecimal()));
    public static final QuadrupleFunction.Unary LOG10 = arg -> Quadruple.valueOf(BigMath.LOG10.invoke(arg.toBigDecimal()));
    public static final QuadrupleFunction.Unary LOG1P = arg -> Quadruple.valueOf(BigMath.LOG1P.invoke(arg.toBigDecimal()));
    public static final QuadrupleFunction.Unary LOGISTIC = arg -> Quadruple.valueOf(BigMath.LOGISTIC.invoke(arg.toBigDecimal()));
    public static final QuadrupleFunction.Unary LOGIT = arg -> Quadruple.valueOf(BigMath.LOGIT.invoke(arg.toBigDecimal()));
    public static final QuadrupleFunction.Binary MAX = (arg1, arg2) -> ((arg1.compareTo(arg2) >= 0) ? arg1 : arg2);
    public static final QuadrupleFunction.Binary MIN = (arg1, arg2) -> ((arg1.compareTo(arg2) <= 0) ? arg1 : arg2);
    public static final QuadrupleFunction.Binary MULTIPLY = Quadruple::multiply;
    public static final QuadrupleFunction.Unary NEGATE = Quadruple::negate;
    public static final QuadrupleFunction.Binary POW = (arg1, arg2) -> EXP.invoke(LOG.invoke(arg1).multiply(arg2));
    public static final QuadrupleFunction.Parameter POWER = (arg, param) -> Quadruple.valueOf(BigMath.POWER.invoke(arg.toBigDecimal(), param));
    public static final QuadrupleFunction.Unary RINT = arg -> Quadruple.valueOf(BigMath.RINT.invoke(arg.toBigDecimal()));
    public static final QuadrupleFunction.Parameter ROOT = (arg, param) -> Quadruple.valueOf(BigMath.ROOT.invoke(arg.toBigDecimal(), param));
    public static final QuadrupleFunction.Parameter SCALE = (arg, param) -> Quadruple.valueOf(BigMath.SCALE.invoke(arg.toBigDecimal(), param));
    public static final QuadrupleFunction.Unary SIGNUM = Quadruple::signum;
    public static final QuadrupleFunction.Unary SIN = arg -> Quadruple.valueOf(BigMath.SIN.invoke(arg.toBigDecimal()));
    public static final QuadrupleFunction.Unary SINH = arg -> EXP.invoke(arg).subtract(EXP.invoke(arg.negate())).divide(Quadruple.TWO);
    public static final QuadrupleFunction.Unary SQRT = arg -> Quadruple.valueOf(BigMath.SQRT.invoke(arg.toBigDecimal()));
    public static final QuadrupleFunction.Unary SQRT1PX2 = arg -> SQRT.invoke(Quadruple.ONE.add(arg.multiply(arg)));
    public static final QuadrupleFunction.Binary SUBTRACT = Quadruple::subtract;
    public static final QuadrupleFunction.Unary TAN = arg -> Quadruple.valueOf(BigMath.TAN.invoke(arg.toBigDecimal()));
    public static final QuadrupleFunction.Unary TANH = arg -> Quadruple.valueOf(BigMath.TANH.invoke(arg.toBigDecimal()));
    public static final QuadrupleFunction.Unary VALUE = arg -> arg;

}

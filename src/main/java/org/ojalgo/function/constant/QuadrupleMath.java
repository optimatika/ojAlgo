/*
 * Copyright 1997-2022 Optimatika
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

    public static final QuadrupleFunction.Unary ABS = arg -> {
        if (arg.compareTo(Quadruple.ZERO) < 0) {
            return arg.negate();
        }
        return arg;
    };
    public static final QuadrupleFunction.Unary ACOS = arg -> {

        final BigDecimal tmpArg = arg.toBigDecimal();

        final BigDecimal tmpRet = BigMath.ACOS.invoke(tmpArg);

        return Quadruple.valueOf(tmpRet);
    };
    public static final QuadrupleFunction.Unary ACOSH = arg -> {

        final Quadruple tmpNmbr = arg.multiply(arg).subtract(Quadruple.ONE);

        return QuadrupleMath.LOG.invoke(arg.add(QuadrupleMath.SQRT.invoke(tmpNmbr)));
    };
    public static final QuadrupleFunction.Binary ADD = (arg1, arg2) -> arg1.add(arg2);
    public static final QuadrupleFunction.Unary ASIN = arg -> {

        final BigDecimal tmpArg = arg.toBigDecimal();

        final BigDecimal tmpRet = BigMath.ASIN.invoke(tmpArg);

        return Quadruple.valueOf(tmpRet);
    };
    public static final QuadrupleFunction.Unary ASINH = arg -> {

        final Quadruple tmpNmbr = arg.multiply(arg).add(Quadruple.ONE);

        return QuadrupleMath.LOG.invoke(arg.add(QuadrupleMath.SQRT.invoke(tmpNmbr)));
    };
    public static final QuadrupleFunction.Unary ATAN = arg -> {

        final BigDecimal tmpArg = arg.toBigDecimal();

        final BigDecimal tmpRet = BigMath.ATAN.invoke(tmpArg);

        return Quadruple.valueOf(tmpRet);
    };
    public static final QuadrupleFunction.Binary ATAN2 = (arg1, arg2) -> {

        final BigDecimal tmpArg1 = arg1.toBigDecimal();
        final BigDecimal tmpArg2 = arg2.toBigDecimal();

        final BigDecimal tmpResult = BigMath.ATAN2.invoke(tmpArg1, tmpArg2);

        return Quadruple.valueOf(tmpResult);
    };
    public static final QuadrupleFunction.Unary ATANH = arg -> {

        final Quadruple tmpNmbr = arg.add(Quadruple.ONE).divide(Quadruple.ONE.subtract(arg));

        return QuadrupleMath.LOG.invoke(tmpNmbr).divide(Quadruple.TWO);
    };
    public static final QuadrupleFunction.Unary CARDINALITY = arg -> arg.compareTo(Quadruple.ZERO) == 0 ? Quadruple.ZERO : Quadruple.ONE;
    public static final QuadrupleFunction.Unary CBRT = arg -> {

        final BigDecimal tmpArg = arg.toBigDecimal();

        final BigDecimal tmpRet = BigMath.CBRT.invoke(tmpArg);

        return Quadruple.valueOf(tmpRet);
    };
    public static final QuadrupleFunction.Unary CEIL = arg -> {

        final BigDecimal tmpArg = arg.toBigDecimal();

        final BigDecimal tmpRet = BigMath.CEIL.invoke(tmpArg);

        return Quadruple.valueOf(tmpRet);
    };
    public static final QuadrupleFunction.Unary CONJUGATE = Quadruple::conjugate;
    public static final QuadrupleFunction.Unary COS = arg -> {

        final BigDecimal tmpArg = arg.toBigDecimal();

        final BigDecimal tmpRet = BigMath.COS.invoke(tmpArg);

        return Quadruple.valueOf(tmpRet);
    };
    public static final QuadrupleFunction.Unary COSH = arg -> QuadrupleMath.EXP.invoke(arg).add(QuadrupleMath.EXP.invoke(arg.negate())).divide(Quadruple.TWO);
    public static final QuadrupleFunction.Binary DIVIDE = (arg1, arg2) -> arg1.divide(arg2);
    public static final QuadrupleFunction.Unary EXP = arg -> {

        final BigDecimal tmpArg = arg.toBigDecimal();

        final BigDecimal tmpRet = BigMath.EXP.invoke(tmpArg);

        return Quadruple.valueOf(tmpRet);
    };
    public static final QuadrupleFunction.Unary EXPM1 = arg -> {

        final BigDecimal tmpArg = arg.toBigDecimal();

        final BigDecimal tmpRet = BigMath.EXPM1.invoke(tmpArg);

        return Quadruple.valueOf(tmpRet);
    };
    public static final QuadrupleFunction.Unary FLOOR = arg -> {

        final BigDecimal tmpArg = arg.toBigDecimal();

        final BigDecimal tmpRet = BigMath.FLOOR.invoke(tmpArg);

        return Quadruple.valueOf(tmpRet);
    };
    public static final QuadrupleFunction.Binary HYPOT = (arg1, arg2) -> {

        final BigDecimal tmpArg1 = arg1.toBigDecimal();
        final BigDecimal tmpArg2 = arg2.toBigDecimal();

        final BigDecimal tmpResult = BigMath.HYPOT.invoke(tmpArg1, tmpArg2);

        return Quadruple.valueOf(tmpResult);
    };
    public static final QuadrupleFunction.Unary INVERT = Quadruple::invert;
    public static final QuadrupleFunction.Unary LOG = arg -> {

        final BigDecimal tmpArg = arg.toBigDecimal();

        final BigDecimal tmpRet = BigMath.LOG.invoke(tmpArg);

        return Quadruple.valueOf(tmpRet);
    };
    public static final QuadrupleFunction.Unary LOG10 = arg -> {

        final BigDecimal tmpArg = arg.toBigDecimal();

        final BigDecimal tmpRet = BigMath.LOG10.invoke(tmpArg);

        return Quadruple.valueOf(tmpRet);
    };
    public static final QuadrupleFunction.Unary LOG1P = arg -> {

        final BigDecimal tmpArg = arg.toBigDecimal();

        final BigDecimal tmpRet = BigMath.LOG1P.invoke(tmpArg);

        return Quadruple.valueOf(tmpRet);
    };
    public static final QuadrupleFunction.Unary LOGISTIC = arg -> Quadruple.valueOf(BigMath.LOGISTIC.invoke(arg.toBigDecimal()));
    public static final QuadrupleFunction.Unary LOGIT = arg -> Quadruple.valueOf(BigMath.LOGIT.invoke(arg.toBigDecimal()));
    public static final QuadrupleFunction.Binary MAX = (arg1, arg2) -> {

        Quadruple retVal = null;

        if (arg1.compareTo(arg2) >= 0) {
            retVal = arg1;
        } else {
            retVal = arg2;
        }

        return retVal;
    };
    public static final QuadrupleFunction.Binary MIN = (arg1, arg2) -> {

        Quadruple retVal = null;

        if (arg1.compareTo(arg2) <= 0) {
            retVal = arg1;
        } else {
            retVal = arg2;
        }

        return retVal;
    };
    public static final QuadrupleFunction.Binary MULTIPLY = (arg1, arg2) -> arg1.multiply(arg2);
    public static final QuadrupleFunction.Unary NEGATE = Quadruple::negate;
    public static final QuadrupleFunction.Binary POW = (arg1, arg2) -> EXP.invoke(LOG.invoke(arg1).multiply(arg2));
    public static final QuadrupleFunction.Parameter POWER = (arg, param) -> {

        final BigDecimal tmpArg = arg.toBigDecimal();

        final BigDecimal tmpRet = BigMath.POWER.invoke(tmpArg, param);

        return Quadruple.valueOf(tmpRet);
    };
    public static final QuadrupleFunction.Unary RINT = arg -> {

        final BigDecimal tmpArg = arg.toBigDecimal();

        final BigDecimal tmpRet = BigMath.RINT.invoke(tmpArg);

        return Quadruple.valueOf(tmpRet);
    };
    public static final QuadrupleFunction.Parameter ROOT = (arg, param) -> {

        final BigDecimal tmpArg = arg.toBigDecimal();

        final BigDecimal tmpRet = BigMath.ROOT.invoke(tmpArg, param);

        return Quadruple.valueOf(tmpRet);
    };
    public static final QuadrupleFunction.Parameter SCALE = (arg, param) -> {

        final BigDecimal tmpArg = arg.toBigDecimal();

        final BigDecimal tmpRet = BigMath.SCALE.invoke(tmpArg, param);

        return Quadruple.valueOf(tmpRet);
    };
    public static final QuadrupleFunction.Unary SIGNUM = Quadruple::signum;
    public static final QuadrupleFunction.Unary SIN = arg -> {

        final BigDecimal tmpArg = arg.toBigDecimal();

        final BigDecimal tmpRet = BigMath.SIN.invoke(tmpArg);

        return Quadruple.valueOf(tmpRet);
    };
    public static final QuadrupleFunction.Unary SINH = arg -> EXP.invoke(arg).subtract(EXP.invoke(arg.negate())).divide(Quadruple.TWO);
    public static final QuadrupleFunction.Unary SQRT = arg -> {

        final BigDecimal tmpArg = arg.toBigDecimal();

        final BigDecimal tmpRet = BigMath.SQRT.invoke(tmpArg);

        return Quadruple.valueOf(tmpRet);
    };
    public static final QuadrupleFunction.Unary SQRT1PX2 = arg -> SQRT.invoke(Quadruple.ONE.add(arg.multiply(arg)));
    public static final QuadrupleFunction.Binary SUBTRACT = (arg1, arg2) -> arg1.subtract(arg2);
    public static final QuadrupleFunction.Unary TAN = arg -> {

        final BigDecimal tmpArg = arg.toBigDecimal();

        final BigDecimal tmpRet = BigMath.TAN.invoke(tmpArg);

        return Quadruple.valueOf(tmpRet);
    };
    public static final QuadrupleFunction.Unary TANH = arg -> {

        Quadruple retVal;

        final Quadruple tmpPlus = EXP.invoke(arg);
        final Quadruple tmpMinus = EXP.invoke(arg.negate());

        final Quadruple tmpDividend = tmpPlus.subtract(tmpMinus);
        final Quadruple tmpDivisor = tmpPlus.add(tmpMinus);

        if (tmpDividend.equals(tmpDivisor)) {
            retVal = Quadruple.ONE;
        } else if (tmpDividend.equals(tmpDivisor.negate())) {
            retVal = Quadruple.ONE.negate();
        } else {
            retVal = tmpDividend.divide(tmpDivisor);
        }

        return retVal;
    };
    public static final QuadrupleFunction.Unary VALUE = arg -> arg;

}

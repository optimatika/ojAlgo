/*
 * Copyright 1997-2020 Optimatika
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
package org.ojalgo.core.function.constant;

import java.math.BigDecimal;

import org.ojalgo.core.function.RationalFunction;
import org.ojalgo.core.scalar.RationalNumber;

public abstract class RationalMath {

    public static class Prefix {

        public static final RationalNumber YOCTO = RationalNumber.valueOf(BigMath.Prefix.YOCTO);
        public static final RationalNumber ZEPTO = RationalNumber.valueOf(BigMath.Prefix.ZEPTO);
        public static final RationalNumber ATTO = RationalNumber.valueOf(BigMath.Prefix.ATTO);
        public static final RationalNumber FEMTO = RationalNumber.valueOf(BigMath.Prefix.FEMTO);
        public static final RationalNumber PICO = RationalNumber.valueOf(BigMath.Prefix.PICO);
        public static final RationalNumber NANO = RationalNumber.valueOf(BigMath.Prefix.NANO);
        public static final RationalNumber MICRO = RationalNumber.valueOf(BigMath.Prefix.MICRO);
        public static final RationalNumber MILLI = RationalNumber.valueOf(BigMath.Prefix.MILLI);
        public static final RationalNumber CENTI = RationalNumber.valueOf(BigMath.Prefix.CENTI);
        public static final RationalNumber DECI = RationalNumber.valueOf(BigMath.Prefix.DECI);
        public static final RationalNumber DEKA = RationalNumber.valueOf(BigMath.Prefix.DEKA);
        public static final RationalNumber HECTO = RationalNumber.valueOf(BigMath.Prefix.HECTO);
        public static final RationalNumber KILO = RationalNumber.valueOf(BigMath.Prefix.KILO);
        public static final RationalNumber MEGA = RationalNumber.valueOf(BigMath.Prefix.MEGA);
        public static final RationalNumber GIGA = RationalNumber.valueOf(BigMath.Prefix.GIGA);
        public static final RationalNumber TERA = RationalNumber.valueOf(BigMath.Prefix.TERA);
        public static final RationalNumber PETA = RationalNumber.valueOf(BigMath.Prefix.PETA);
        public static final RationalNumber EXA = RationalNumber.valueOf(BigMath.Prefix.EXA);
        public static final RationalNumber ZETTA = RationalNumber.valueOf(BigMath.Prefix.ZETTA);
        public static final RationalNumber YOTTA = RationalNumber.valueOf(BigMath.Prefix.YOTTA);

    }

    public static final RationalNumber ZERO = RationalNumber.valueOf(BigMath.ZERO);
    public static final RationalNumber ONE = RationalNumber.valueOf(BigMath.ONE);
    public static final RationalNumber TWO = RationalNumber.valueOf(BigMath.TWO);
    public static final RationalNumber THREE = RationalNumber.valueOf(BigMath.THREE);
    public static final RationalNumber FOUR = RationalNumber.valueOf(BigMath.FOUR);
    public static final RationalNumber FIVE = RationalNumber.valueOf(BigMath.FIVE);
    public static final RationalNumber SIX = RationalNumber.valueOf(BigMath.SIX);
    public static final RationalNumber SEVEN = RationalNumber.valueOf(BigMath.SEVEN);
    public static final RationalNumber EIGHT = RationalNumber.valueOf(BigMath.EIGHT);
    public static final RationalNumber NINE = RationalNumber.valueOf(BigMath.NINE);
    public static final RationalNumber TEN = RationalNumber.valueOf(BigMath.TEN);
    public static final RationalNumber ELEVEN = RationalNumber.valueOf(BigMath.ELEVEN);
    public static final RationalNumber TWELVE = RationalNumber.valueOf(BigMath.TWELVE);
    public static final RationalNumber HUNDRED = RationalNumber.valueOf(BigMath.HUNDRED);
    public static final RationalNumber THOUSAND = RationalNumber.valueOf(BigMath.THOUSAND);

    public static final RationalNumber NEG = RationalNumber.valueOf(BigMath.NEG);

    public static final RationalNumber HALF = RationalNumber.valueOf(BigMath.HALF);
    public static final RationalNumber THIRD = RationalNumber.valueOf(BigMath.THIRD);
    public static final RationalNumber QUARTER = RationalNumber.valueOf(BigMath.QUARTER);
    public static final RationalNumber FITH = RationalNumber.valueOf(BigMath.FITH);
    public static final RationalNumber SIXTH = RationalNumber.valueOf(BigMath.SIXTH);
    public static final RationalNumber SEVENTH = RationalNumber.valueOf(BigMath.SEVENTH);
    public static final RationalNumber EIGHTH = RationalNumber.valueOf(BigMath.EIGHTH);
    public static final RationalNumber NINTH = RationalNumber.valueOf(BigMath.NINTH);
    public static final RationalNumber TENTH = RationalNumber.valueOf(BigMath.TENTH);
    public static final RationalNumber ELEVENTH = RationalNumber.valueOf(BigMath.ELEVENTH);
    public static final RationalNumber TWELFTH = RationalNumber.valueOf(BigMath.TWELFTH);
    public static final RationalNumber HUNDREDTH = RationalNumber.valueOf(BigMath.HUNDREDTH);
    public static final RationalNumber THOUSANDTH = RationalNumber.valueOf(BigMath.THOUSANDTH);

    public static final RationalNumber TWO_THIRDS = RationalNumber.valueOf(BigMath.TWO_THIRDS);
    public static final RationalNumber THREE_QUARTERS = RationalNumber.valueOf(BigMath.THREE_QUARTERS);

    public static final RationalNumber E = RationalNumber.valueOf(BigMath.E);
    public static final RationalNumber PI = RationalNumber.valueOf(BigMath.PI);
    public static final RationalNumber GOLDEN_RATIO = RationalNumber.valueOf(BigMath.GOLDEN_RATIO);

    public static final RationalNumber HALF_PI = RationalNumber.valueOf(BigMath.HALF_PI);
    public static final RationalNumber TWO_PI = RationalNumber.valueOf(BigMath.TWO_PI);

    public static final RationalNumber SQRT_TWO = RationalNumber.valueOf(BigMath.SQRT_TWO);
    public static final RationalNumber SQRT_PI = RationalNumber.valueOf(BigMath.SQRT_PI);
    public static final RationalNumber SQRT_TWO_PI = RationalNumber.valueOf(BigMath.SQRT_TWO_PI);

    public static final RationalNumber NaN = RationalNumber.NaN;
    public static final RationalNumber POSITIVE_INFINITY = RationalNumber.POSITIVE_INFINITY;
    public static final RationalNumber NEGATIVE_INFINITY = RationalNumber.NEGATIVE_INFINITY;

    /*
     * The lambdas below should not (cannot) reference each other. Delegate to some other 'type' or code in
     * org.ojalgo.function.special.MissingMath.
     */

    public static final RationalFunction.Unary ABS = arg -> {
        if (arg.compareTo(RationalNumber.ZERO) == -1) {
            return arg.negate();
        } else {
            return arg;
        }
    };
    public static final RationalFunction.Unary ACOS = arg -> {

        final BigDecimal tmpArg = arg.toBigDecimal();

        final BigDecimal tmpRet = BigMath.ACOS.invoke(tmpArg);

        return RationalNumber.valueOf(tmpRet);
    };
    public static final RationalFunction.Unary ACOSH = arg -> {

        final RationalNumber tmpNmbr = arg.multiply(arg).subtract(RationalNumber.ONE);

        return RationalMath.LOG.invoke(arg.add(RationalMath.SQRT.invoke(tmpNmbr)));
    };
    public static final RationalFunction.Binary ADD = (arg1, arg2) -> arg1.add(arg2);
    public static final RationalFunction.Unary ASIN = arg -> {

        final BigDecimal tmpArg = arg.toBigDecimal();

        final BigDecimal tmpRet = BigMath.ASIN.invoke(tmpArg);

        return RationalNumber.valueOf(tmpRet);
    };
    public static final RationalFunction.Unary ASINH = arg -> {

        final RationalNumber tmpNmbr = arg.multiply(arg).add(RationalNumber.ONE);

        return RationalMath.LOG.invoke(arg.add(RationalMath.SQRT.invoke(tmpNmbr)));
    };
    public static final RationalFunction.Unary ATAN = arg -> {

        final BigDecimal tmpArg = arg.toBigDecimal();

        final BigDecimal tmpRet = BigMath.ATAN.invoke(tmpArg);

        return RationalNumber.valueOf(tmpRet);
    };
    public static final RationalFunction.Binary ATAN2 = (arg1, arg2) -> {

        final BigDecimal tmpArg1 = arg1.toBigDecimal();
        final BigDecimal tmpArg2 = arg2.toBigDecimal();

        final BigDecimal tmpResult = BigMath.ATAN2.invoke(tmpArg1, tmpArg2);

        return RationalNumber.valueOf(tmpResult);
    };
    public static final RationalFunction.Unary ATANH = arg -> {

        final RationalNumber tmpNmbr = arg.add(RationalNumber.ONE).divide(RationalNumber.ONE.subtract(arg));

        return RationalMath.LOG.invoke(tmpNmbr).divide(RationalNumber.TWO);
    };
    public static final RationalFunction.Unary CARDINALITY = arg -> arg.compareTo(RationalNumber.ZERO) == 0 ? RationalNumber.ZERO : RationalNumber.ONE;
    public static final RationalFunction.Unary CBRT = arg -> {

        final BigDecimal tmpArg = arg.toBigDecimal();

        final BigDecimal tmpRet = BigMath.CBRT.invoke(tmpArg);

        return RationalNumber.valueOf(tmpRet);
    };
    public static final RationalFunction.Unary CEIL = arg -> {

        final BigDecimal tmpArg = arg.toBigDecimal();

        final BigDecimal tmpRet = BigMath.CEIL.invoke(tmpArg);

        return RationalNumber.valueOf(tmpRet);
    };
    public static final RationalFunction.Unary CONJUGATE = RationalNumber::conjugate;
    public static final RationalFunction.Unary COS = arg -> {

        final BigDecimal tmpArg = arg.toBigDecimal();

        final BigDecimal tmpRet = BigMath.COS.invoke(tmpArg);

        return RationalNumber.valueOf(tmpRet);
    };
    public static final RationalFunction.Unary COSH = arg -> (RationalMath.EXP.invoke(arg).add(RationalMath.EXP.invoke(arg.negate())))
            .divide(RationalNumber.TWO);
    public static final RationalFunction.Binary DIVIDE = (arg1, arg2) -> arg1.divide(arg2);
    public static final RationalFunction.Unary EXP = arg -> {

        final BigDecimal tmpArg = arg.toBigDecimal();

        final BigDecimal tmpRet = BigMath.EXP.invoke(tmpArg);

        return RationalNumber.valueOf(tmpRet);
    };
    public static final RationalFunction.Unary EXPM1 = arg -> {

        final BigDecimal tmpArg = arg.toBigDecimal();

        final BigDecimal tmpRet = BigMath.EXPM1.invoke(tmpArg);

        return RationalNumber.valueOf(tmpRet);
    };
    public static final RationalFunction.Unary FLOOR = arg -> {

        final BigDecimal tmpArg = arg.toBigDecimal();

        final BigDecimal tmpRet = BigMath.FLOOR.invoke(tmpArg);

        return RationalNumber.valueOf(tmpRet);
    };
    public static final RationalFunction.Binary HYPOT = (arg1, arg2) -> {

        final BigDecimal tmpArg1 = arg1.toBigDecimal();
        final BigDecimal tmpArg2 = arg2.toBigDecimal();

        final BigDecimal tmpResult = BigMath.HYPOT.invoke(tmpArg1, tmpArg2);

        return RationalNumber.valueOf(tmpResult);
    };
    public static final RationalFunction.Unary INVERT = RationalNumber::invert;
    public static final RationalFunction.Unary LOG = arg -> {

        final BigDecimal tmpArg = arg.toBigDecimal();

        final BigDecimal tmpRet = BigMath.LOG.invoke(tmpArg);

        return RationalNumber.valueOf(tmpRet);
    };
    public static final RationalFunction.Unary LOG10 = arg -> {

        final BigDecimal tmpArg = arg.toBigDecimal();

        final BigDecimal tmpRet = BigMath.LOG10.invoke(tmpArg);

        return RationalNumber.valueOf(tmpRet);
    };
    public static final RationalFunction.Unary LOG1P = arg -> {

        final BigDecimal tmpArg = arg.toBigDecimal();

        final BigDecimal tmpRet = BigMath.LOG1P.invoke(tmpArg);

        return RationalNumber.valueOf(tmpRet);
    };
    public static final RationalFunction.Unary LOGISTIC = arg -> RationalNumber.valueOf(BigMath.LOGISTIC.invoke(arg.toBigDecimal()));
    public static final RationalFunction.Unary LOGIT = arg -> RationalNumber.valueOf(BigMath.LOGIT.invoke(arg.toBigDecimal()));
    public static final RationalFunction.Binary MAX = (arg1, arg2) -> {

        RationalNumber retVal = null;

        if (arg1.compareTo(arg2) >= 0) {
            retVal = arg1;
        } else {
            retVal = arg2;
        }

        return retVal;
    };
    public static final RationalFunction.Binary MIN = (arg1, arg2) -> {

        RationalNumber retVal = null;

        if (arg1.compareTo(arg2) <= 0) {
            retVal = arg1;
        } else {
            retVal = arg2;
        }

        return retVal;
    };
    public static final RationalFunction.Binary MULTIPLY = (arg1, arg2) -> arg1.multiply(arg2);
    public static final RationalFunction.Unary NEGATE = RationalNumber::negate;
    public static final RationalFunction.Binary POW = (arg1, arg2) -> EXP.invoke(LOG.invoke(arg1).multiply(arg2));
    public static final RationalFunction.Parameter POWER = (arg, param) -> {

        final BigDecimal tmpArg = arg.toBigDecimal();

        final BigDecimal tmpRet = BigMath.POWER.invoke(tmpArg, param);

        return RationalNumber.valueOf(tmpRet);
    };
    public static final RationalFunction.Unary RINT = arg -> {

        final BigDecimal tmpArg = arg.toBigDecimal();

        final BigDecimal tmpRet = BigMath.RINT.invoke(tmpArg);

        return RationalNumber.valueOf(tmpRet);
    };
    public static final RationalFunction.Parameter ROOT = (arg, param) -> {

        final BigDecimal tmpArg = arg.toBigDecimal();

        final BigDecimal tmpRet = BigMath.ROOT.invoke(tmpArg, param);

        return RationalNumber.valueOf(tmpRet);
    };
    public static final RationalFunction.Parameter SCALE = (arg, param) -> {

        final BigDecimal tmpArg = arg.toBigDecimal();

        final BigDecimal tmpRet = BigMath.SCALE.invoke(tmpArg, param);

        return RationalNumber.valueOf(tmpRet);
    };
    public static final RationalFunction.Unary SIGNUM = RationalNumber::signum;
    public static final RationalFunction.Unary SIN = arg -> {

        final BigDecimal tmpArg = arg.toBigDecimal();

        final BigDecimal tmpRet = BigMath.SIN.invoke(tmpArg);

        return RationalNumber.valueOf(tmpRet);
    };
    public static final RationalFunction.Unary SINH = arg -> (EXP.invoke(arg).subtract(EXP.invoke(arg.negate()))).divide(RationalNumber.TWO);
    public static final RationalFunction.Unary SQRT = arg -> {

        final BigDecimal tmpArg = arg.toBigDecimal();

        final BigDecimal tmpRet = BigMath.SQRT.invoke(tmpArg);

        return RationalNumber.valueOf(tmpRet);
    };
    public static final RationalFunction.Unary SQRT1PX2 = arg -> SQRT.invoke(RationalNumber.ONE.add(arg.multiply(arg)));
    public static final RationalFunction.Binary SUBTRACT = (arg1, arg2) -> arg1.subtract(arg2);
    public static final RationalFunction.Unary TAN = arg -> {

        final BigDecimal tmpArg = arg.toBigDecimal();

        final BigDecimal tmpRet = BigMath.TAN.invoke(tmpArg);

        return RationalNumber.valueOf(tmpRet);
    };
    public static final RationalFunction.Unary TANH = arg -> {

        RationalNumber retVal;

        final RationalNumber tmpPlus = EXP.invoke(arg);
        final RationalNumber tmpMinus = EXP.invoke(arg.negate());

        final RationalNumber tmpDividend = tmpPlus.subtract(tmpMinus);
        final RationalNumber tmpDivisor = tmpPlus.add(tmpMinus);

        if (tmpDividend.equals(tmpDivisor)) {
            retVal = RationalNumber.ONE;
        } else if (tmpDividend.equals(tmpDivisor.negate())) {
            retVal = RationalNumber.ONE.negate();
        } else {
            retVal = tmpDividend.divide(tmpDivisor);
        }

        return retVal;
    };
    public static final RationalFunction.Unary VALUE = arg -> arg;

}

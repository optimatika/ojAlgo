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
package org.ojalgo.function.constant;

import java.math.BigDecimal;

import org.ojalgo.function.RationalFunction;
import org.ojalgo.scalar.RationalNumber;

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

    public static final RationalFunction.Unary ABS = new RationalFunction.Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {
            if (arg.compareTo(RationalNumber.ZERO) == -1) {
                return arg.negate();
            } else {
                return arg;
            }
        }
    };
    public static final RationalFunction.Unary ACOS = new RationalFunction.Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {

            final BigDecimal tmpArg = arg.toBigDecimal();

            final BigDecimal tmpRet = BigMath.ACOS.invoke(tmpArg);

            return RationalNumber.valueOf(tmpRet);
        }
    };
    public static final RationalFunction.Unary ACOSH = new RationalFunction.Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {

            final RationalNumber tmpNmbr = arg.multiply(arg).subtract(RationalNumber.ONE);

            return LOG.invoke(arg.add(SQRT.invoke(tmpNmbr)));
        }
    };
    public static final RationalFunction.Binary ADD = new RationalFunction.Binary() {

        @Override
        public final RationalNumber invoke(final RationalNumber arg1, final RationalNumber arg2) {
            return arg1.add(arg2);
        }
    };
    public static final RationalFunction.Unary ASIN = new RationalFunction.Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {

            final BigDecimal tmpArg = arg.toBigDecimal();

            final BigDecimal tmpRet = BigMath.ASIN.invoke(tmpArg);

            return RationalNumber.valueOf(tmpRet);
        }
    };
    public static final RationalFunction.Unary ASINH = new RationalFunction.Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {

            final RationalNumber tmpNmbr = arg.multiply(arg).add(RationalNumber.ONE);

            return LOG.invoke(arg.add(SQRT.invoke(tmpNmbr)));
        }
    };
    public static final RationalFunction.Unary ATAN = new RationalFunction.Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {

            final BigDecimal tmpArg = arg.toBigDecimal();

            final BigDecimal tmpRet = BigMath.ATAN.invoke(tmpArg);

            return RationalNumber.valueOf(tmpRet);
        }
    };
    public static final RationalFunction.Binary ATAN2 = new RationalFunction.Binary() {

        @Override
        public final RationalNumber invoke(final RationalNumber arg1, final RationalNumber arg2) {

            final BigDecimal tmpArg1 = arg1.toBigDecimal();
            final BigDecimal tmpArg2 = arg2.toBigDecimal();

            final BigDecimal tmpResult = BigMath.ATAN2.invoke(tmpArg1, tmpArg2);

            return RationalNumber.valueOf(tmpResult);
        }
    };
    public static final RationalFunction.Unary ATANH = new RationalFunction.Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {

            final RationalNumber tmpNmbr = arg.add(RationalNumber.ONE).divide(RationalNumber.ONE.subtract(arg));

            return LOG.invoke(tmpNmbr).divide(RationalNumber.TWO);
        }
    };
    public static final RationalFunction.Unary CARDINALITY = new RationalFunction.Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {
            return arg.compareTo(RationalNumber.ZERO) == 0 ? RationalNumber.ZERO : RationalNumber.ONE;
        }
    };
    public static final RationalFunction.Unary CBRT = new RationalFunction.Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {

            final BigDecimal tmpArg = arg.toBigDecimal();

            final BigDecimal tmpRet = BigMath.CBRT.invoke(tmpArg);

            return RationalNumber.valueOf(tmpRet);
        }

    };
    public static final RationalFunction.Unary CEIL = new RationalFunction.Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {

            final BigDecimal tmpArg = arg.toBigDecimal();

            final BigDecimal tmpRet = BigMath.CEIL.invoke(tmpArg);

            return RationalNumber.valueOf(tmpRet);
        }
    };
    public static final RationalFunction.Unary CONJUGATE = new RationalFunction.Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {
            return arg.conjugate();
        }
    };
    public static final RationalFunction.Unary COS = new RationalFunction.Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {

            final BigDecimal tmpArg = arg.toBigDecimal();

            final BigDecimal tmpRet = BigMath.COS.invoke(tmpArg);

            return RationalNumber.valueOf(tmpRet);
        }
    };
    public static final RationalFunction.Unary COSH = new RationalFunction.Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {
            return (EXP.invoke(arg).add(EXP.invoke(arg.negate()))).divide(RationalNumber.TWO);
        }
    };
    public static final RationalFunction.Binary DIVIDE = new RationalFunction.Binary() {

        @Override
        public final RationalNumber invoke(final RationalNumber arg1, final RationalNumber arg2) {
            return arg1.divide(arg2);
        }
    };
    public static final RationalFunction.Unary EXP = new RationalFunction.Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {

            final BigDecimal tmpArg = arg.toBigDecimal();

            final BigDecimal tmpRet = BigMath.EXP.invoke(tmpArg);

            return RationalNumber.valueOf(tmpRet);
        }
    };
    public static final RationalFunction.Unary EXPM1 = new RationalFunction.Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {

            final BigDecimal tmpArg = arg.toBigDecimal();

            final BigDecimal tmpRet = BigMath.EXPM1.invoke(tmpArg);

            return RationalNumber.valueOf(tmpRet);
        }
    };
    public static final RationalFunction.Unary FLOOR = new RationalFunction.Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {

            final BigDecimal tmpArg = arg.toBigDecimal();

            final BigDecimal tmpRet = BigMath.FLOOR.invoke(tmpArg);

            return RationalNumber.valueOf(tmpRet);
        }
    };
    public static final RationalFunction.Binary HYPOT = new RationalFunction.Binary() {

        @Override
        public final RationalNumber invoke(final RationalNumber arg1, final RationalNumber arg2) {

            final BigDecimal tmpArg1 = arg1.toBigDecimal();
            final BigDecimal tmpArg2 = arg2.toBigDecimal();

            final BigDecimal tmpResult = BigMath.HYPOT.invoke(tmpArg1, tmpArg2);

            return RationalNumber.valueOf(tmpResult);
        }
    };
    public static final RationalFunction.Unary INVERT = new RationalFunction.Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {
            return arg.invert();
        }
    };
    public static final RationalFunction.Unary LOG = new RationalFunction.Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {

            final BigDecimal tmpArg = arg.toBigDecimal();

            final BigDecimal tmpRet = BigMath.LOG.invoke(tmpArg);

            return RationalNumber.valueOf(tmpRet);
        }
    };
    public static final RationalFunction.Unary LOG10 = new RationalFunction.Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {

            final BigDecimal tmpArg = arg.toBigDecimal();

            final BigDecimal tmpRet = BigMath.LOG10.invoke(tmpArg);

            return RationalNumber.valueOf(tmpRet);
        }
    };
    public static final RationalFunction.Unary LOG1P = new RationalFunction.Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {

            final BigDecimal tmpArg = arg.toBigDecimal();

            final BigDecimal tmpRet = BigMath.LOG1P.invoke(tmpArg);

            return RationalNumber.valueOf(tmpRet);
        }
    };
    public static final RationalFunction.Unary LOGISTIC = new RationalFunction.Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {
            return RationalNumber.valueOf(BigMath.LOGISTIC.invoke(arg.toBigDecimal()));
        }

    };
    public static final RationalFunction.Unary LOGIT = new RationalFunction.Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {
            return RationalNumber.valueOf(BigMath.LOGIT.invoke(arg.toBigDecimal()));
        }

    };
    public static final RationalFunction.Binary MAX = new RationalFunction.Binary() {

        @Override
        public final RationalNumber invoke(final RationalNumber arg1, final RationalNumber arg2) {

            RationalNumber retVal = null;

            if (arg1.compareTo(arg2) >= 0) {
                retVal = arg1;
            } else {
                retVal = arg2;
            }

            return retVal;
        }
    };
    public static final RationalFunction.Binary MIN = new RationalFunction.Binary() {

        @Override
        public final RationalNumber invoke(final RationalNumber arg1, final RationalNumber arg2) {

            RationalNumber retVal = null;

            if (arg1.compareTo(arg2) <= 0) {
                retVal = arg1;
            } else {
                retVal = arg2;
            }

            return retVal;
        }
    };
    public static final RationalFunction.Binary MULTIPLY = new RationalFunction.Binary() {

        @Override
        public final RationalNumber invoke(final RationalNumber arg1, final RationalNumber arg2) {
            return arg1.multiply(arg2);
        }
    };
    public static final RationalFunction.Unary NEGATE = new RationalFunction.Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {
            return arg.negate();
        }
    };
    public static final RationalFunction.Binary POW = new RationalFunction.Binary() {

        @Override
        public final RationalNumber invoke(final RationalNumber arg1, final RationalNumber arg2) {
            return EXP.invoke(LOG.invoke(arg1).multiply(arg2));
        }
    };
    public static final RationalFunction.Parameter POWER = new RationalFunction.Parameter() {

        @Override
        public final RationalNumber invoke(final RationalNumber arg, final int param) {

            final BigDecimal tmpArg = arg.toBigDecimal();

            final BigDecimal tmpRet = BigMath.POWER.invoke(tmpArg, param);

            return RationalNumber.valueOf(tmpRet);
        }
    };
    public static final RationalFunction.Unary RINT = new RationalFunction.Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {

            final BigDecimal tmpArg = arg.toBigDecimal();

            final BigDecimal tmpRet = BigMath.RINT.invoke(tmpArg);

            return RationalNumber.valueOf(tmpRet);
        }
    };
    public static final RationalFunction.Parameter ROOT = new RationalFunction.Parameter() {

        @Override
        public final RationalNumber invoke(final RationalNumber arg, final int param) {

            final BigDecimal tmpArg = arg.toBigDecimal();

            final BigDecimal tmpRet = BigMath.ROOT.invoke(tmpArg, param);

            return RationalNumber.valueOf(tmpRet);
        }
    };
    public static final RationalFunction.Parameter SCALE = new RationalFunction.Parameter() {

        @Override
        public final RationalNumber invoke(final RationalNumber arg, final int param) {

            final BigDecimal tmpArg = arg.toBigDecimal();

            final BigDecimal tmpRet = BigMath.SCALE.invoke(tmpArg, param);

            return RationalNumber.valueOf(tmpRet);
        }
    };
    public static final RationalFunction.Unary SIGNUM = new RationalFunction.Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {
            return arg.signum();
        }
    };
    public static final RationalFunction.Unary SIN = new RationalFunction.Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {

            final BigDecimal tmpArg = arg.toBigDecimal();

            final BigDecimal tmpRet = BigMath.SIN.invoke(tmpArg);

            return RationalNumber.valueOf(tmpRet);
        }
    };
    public static final RationalFunction.Unary SINH = new RationalFunction.Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {
            return (EXP.invoke(arg).subtract(EXP.invoke(arg.negate()))).divide(RationalNumber.TWO);
        }
    };
    public static final RationalFunction.Unary SQRT = new RationalFunction.Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {

            final BigDecimal tmpArg = arg.toBigDecimal();

            final BigDecimal tmpRet = BigMath.SQRT.invoke(tmpArg);

            return RationalNumber.valueOf(tmpRet);
        }
    };
    public static final RationalFunction.Unary SQRT1PX2 = new RationalFunction.Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {
            return SQRT.invoke(RationalNumber.ONE.add(arg.multiply(arg)));
        }
    };
    public static final RationalFunction.Binary SUBTRACT = new RationalFunction.Binary() {

        @Override
        public final RationalNumber invoke(final RationalNumber arg1, final RationalNumber arg2) {
            return arg1.subtract(arg2);
        }
    };
    public static final RationalFunction.Unary TAN = new RationalFunction.Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {

            final BigDecimal tmpArg = arg.toBigDecimal();

            final BigDecimal tmpRet = BigMath.TAN.invoke(tmpArg);

            return RationalNumber.valueOf(tmpRet);
        }
    };
    public static final RationalFunction.Unary TANH = new RationalFunction.Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {

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
        }
    };
    public static final RationalFunction.Unary VALUE = new RationalFunction.Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {
            return arg;
        }
    };

}

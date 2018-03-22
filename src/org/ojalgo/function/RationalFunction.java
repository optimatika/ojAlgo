/*
 * Copyright 1997-2018 Optimatika
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
package org.ojalgo.function;

import java.math.BigDecimal;

import org.ojalgo.function.aggregator.AggregatorSet;
import org.ojalgo.function.aggregator.RationalAggregator;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.type.context.NumberContext;

/**
 * RationalFunction
 *
 * @author apete
 */
public final class RationalFunction extends FunctionSet<RationalNumber> {

    @FunctionalInterface
    public static interface Binary extends BinaryFunction<RationalNumber> {

        default double invoke(final double arg1, final double arg2) {
            return this.invoke(RationalNumber.valueOf(arg1), RationalNumber.valueOf(arg2)).doubleValue();
        }

    }

    @FunctionalInterface
    public static interface Parameter extends ParameterFunction<RationalNumber> {

        default double invoke(final double arg, final int param) {
            return this.invoke(RationalNumber.valueOf(arg), param).doubleValue();
        }

    }

    @FunctionalInterface
    public static interface Unary extends UnaryFunction<RationalNumber> {

        default double invoke(final double arg) {
            return this.invoke(RationalNumber.valueOf(arg)).doubleValue();
        }

    }

    public static final Unary ABS = new Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {
            if (arg.compareTo(RationalNumber.ZERO) == -1) {
                return arg.negate();
            } else {
                return arg;
            }
        }
    };

    public static final Unary ACOS = new Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {

            final BigDecimal tmpArg = arg.toBigDecimal();

            final BigDecimal tmpRet = BigFunction.ACOS.invoke(tmpArg);

            return RationalNumber.valueOf(tmpRet);
        }
    };

    public static final Unary ACOSH = new Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {

            final RationalNumber tmpNmbr = arg.multiply(arg).subtract(RationalNumber.ONE);

            return LOG.invoke(arg.add(SQRT.invoke(tmpNmbr)));
        }
    };

    public static final Binary ADD = new Binary() {

        @Override
        public final RationalNumber invoke(final RationalNumber arg1, final RationalNumber arg2) {
            return arg1.add(arg2);
        }
    };

    public static final Unary ASIN = new Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {

            final BigDecimal tmpArg = arg.toBigDecimal();

            final BigDecimal tmpRet = BigFunction.ASIN.invoke(tmpArg);

            return RationalNumber.valueOf(tmpRet);
        }
    };

    public static final Unary ASINH = new Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {

            final RationalNumber tmpNmbr = arg.multiply(arg).add(RationalNumber.ONE);

            return LOG.invoke(arg.add(SQRT.invoke(tmpNmbr)));
        }
    };

    public static final Unary ATAN = new Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {

            final BigDecimal tmpArg = arg.toBigDecimal();

            final BigDecimal tmpRet = BigFunction.ATAN.invoke(tmpArg);

            return RationalNumber.valueOf(tmpRet);
        }
    };

    public static final Binary ATAN2 = new Binary() {

        @Override
        public final RationalNumber invoke(final RationalNumber arg1, final RationalNumber arg2) {

            final BigDecimal tmpArg1 = arg1.toBigDecimal();
            final BigDecimal tmpArg2 = arg2.toBigDecimal();

            final BigDecimal tmpResult = BigFunction.ATAN2.invoke(tmpArg1, tmpArg2);

            return RationalNumber.valueOf(tmpResult);
        }
    };

    public static final Unary ATANH = new Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {

            final RationalNumber tmpNmbr = arg.add(RationalNumber.ONE).divide(RationalNumber.ONE.subtract(arg));

            return LOG.invoke(tmpNmbr).divide(RationalNumber.TWO);
        }
    };

    public static final Unary CARDINALITY = new Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {
            return arg.compareTo(RationalNumber.ZERO) == 0 ? RationalNumber.ZERO : RationalNumber.ONE;
        }
    };

    public static final Unary CBRT = new Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {

            final BigDecimal tmpArg = arg.toBigDecimal();

            final BigDecimal tmpRet = BigFunction.CBRT.invoke(tmpArg);

            return RationalNumber.valueOf(tmpRet);
        }

    };

    public static final Unary CEIL = new Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {

            final BigDecimal tmpArg = arg.toBigDecimal();

            final BigDecimal tmpRet = BigFunction.CEIL.invoke(tmpArg);

            return RationalNumber.valueOf(tmpRet);
        }
    };

    public static final Unary CONJUGATE = new Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {
            return arg.conjugate();
        }
    };

    public static final Unary COS = new Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {

            final BigDecimal tmpArg = arg.toBigDecimal();

            final BigDecimal tmpRet = BigFunction.COS.invoke(tmpArg);

            return RationalNumber.valueOf(tmpRet);
        }
    };

    public static final Unary COSH = new Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {
            return (EXP.invoke(arg).add(EXP.invoke(arg.negate()))).divide(RationalNumber.TWO);
        }
    };

    public static final Binary DIVIDE = new Binary() {

        @Override
        public final RationalNumber invoke(final RationalNumber arg1, final RationalNumber arg2) {
            return arg1.divide(arg2);
        }
    };

    public static final Unary EXP = new Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {

            final BigDecimal tmpArg = arg.toBigDecimal();

            final BigDecimal tmpRet = BigFunction.EXP.invoke(tmpArg);

            return RationalNumber.valueOf(tmpRet);
        }
    };

    public static final Unary EXPM1 = new Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {

            final BigDecimal tmpArg = arg.toBigDecimal();

            final BigDecimal tmpRet = BigFunction.EXPM1.invoke(tmpArg);

            return RationalNumber.valueOf(tmpRet);
        }
    };

    public static final Unary FLOOR = new Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {

            final BigDecimal tmpArg = arg.toBigDecimal();

            final BigDecimal tmpRet = BigFunction.FLOOR.invoke(tmpArg);

            return RationalNumber.valueOf(tmpRet);
        }
    };

    public static final Binary HYPOT = new Binary() {

        @Override
        public final RationalNumber invoke(final RationalNumber arg1, final RationalNumber arg2) {

            final BigDecimal tmpArg1 = arg1.toBigDecimal();
            final BigDecimal tmpArg2 = arg2.toBigDecimal();

            final BigDecimal tmpResult = BigFunction.HYPOT.invoke(tmpArg1, tmpArg2);

            return RationalNumber.valueOf(tmpResult);
        }
    };

    public static final Unary INVERT = new Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {
            return arg.invert();
        }
    };

    public static final Unary LOG = new Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {

            final BigDecimal tmpArg = arg.toBigDecimal();

            final BigDecimal tmpRet = BigFunction.LOG.invoke(tmpArg);

            return RationalNumber.valueOf(tmpRet);
        }
    };

    public static final Unary LOG10 = new Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {

            final BigDecimal tmpArg = arg.toBigDecimal();

            final BigDecimal tmpRet = BigFunction.LOG10.invoke(tmpArg);

            return RationalNumber.valueOf(tmpRet);
        }
    };

    public static final Unary LOG1P = new Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {

            final BigDecimal tmpArg = arg.toBigDecimal();

            final BigDecimal tmpRet = BigFunction.LOG1P.invoke(tmpArg);

            return RationalNumber.valueOf(tmpRet);
        }
    };

    public static final Unary LOGISTIC = new Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {
            return RationalNumber.valueOf(BigFunction.LOGISTIC.invoke(arg.toBigDecimal()));
        }

    };

    public static final Unary LOGIT = new Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {
            return RationalNumber.valueOf(BigFunction.LOGIT.invoke(arg.toBigDecimal()));
        }

    };

    public static final Binary MAX = new Binary() {

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

    public static final Binary MIN = new Binary() {

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

    public static final Binary MULTIPLY = new Binary() {

        @Override
        public final RationalNumber invoke(final RationalNumber arg1, final RationalNumber arg2) {
            return arg1.multiply(arg2);
        }
    };

    public static final Unary NEGATE = new Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {
            return arg.negate();
        }
    };

    public static final Binary POW = new Binary() {

        @Override
        public final RationalNumber invoke(final RationalNumber arg1, final RationalNumber arg2) {
            return EXP.invoke(LOG.invoke(arg1).multiply(arg2));
        }
    };

    public static final Parameter POWER = new Parameter() {

        @Override
        public final RationalNumber invoke(final RationalNumber arg, final int param) {

            final BigDecimal tmpArg = arg.toBigDecimal();

            final BigDecimal tmpRet = BigFunction.POWER.invoke(tmpArg, param);

            return RationalNumber.valueOf(tmpRet);
        }
    };

    public static final Unary RINT = new Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {

            final BigDecimal tmpArg = arg.toBigDecimal();

            final BigDecimal tmpRet = BigFunction.RINT.invoke(tmpArg);

            return RationalNumber.valueOf(tmpRet);
        }
    };

    public static final Parameter ROOT = new Parameter() {

        @Override
        public final RationalNumber invoke(final RationalNumber arg, final int param) {

            final BigDecimal tmpArg = arg.toBigDecimal();

            final BigDecimal tmpRet = BigFunction.ROOT.invoke(tmpArg, param);

            return RationalNumber.valueOf(tmpRet);
        }
    };

    public static final Parameter SCALE = new Parameter() {

        @Override
        public final RationalNumber invoke(final RationalNumber arg, final int param) {

            final BigDecimal tmpArg = arg.toBigDecimal();

            final BigDecimal tmpRet = BigFunction.SCALE.invoke(tmpArg, param);

            return RationalNumber.valueOf(tmpRet);
        }
    };

    public static final Unary SIGNUM = new Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {
            return arg.signum();
        }
    };

    public static final Unary SIN = new Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {

            final BigDecimal tmpArg = arg.toBigDecimal();

            final BigDecimal tmpRet = BigFunction.SIN.invoke(tmpArg);

            return RationalNumber.valueOf(tmpRet);
        }
    };

    public static final Unary SINH = new Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {
            return (EXP.invoke(arg).subtract(EXP.invoke(arg.negate()))).divide(RationalNumber.TWO);
        }
    };

    public static final Unary SQRT = new Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {

            final BigDecimal tmpArg = arg.toBigDecimal();

            final BigDecimal tmpRet = BigFunction.SQRT.invoke(tmpArg);

            return RationalNumber.valueOf(tmpRet);
        }
    };

    public static final Unary SQRT1PX2 = new Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {
            return SQRT.invoke(RationalNumber.ONE.add(arg.multiply(arg)));
        }
    };

    public static final Binary SUBTRACT = new Binary() {

        @Override
        public final RationalNumber invoke(final RationalNumber arg1, final RationalNumber arg2) {
            return arg1.subtract(arg2);
        }
    };

    public static final Unary TAN = new Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {

            final BigDecimal tmpArg = arg.toBigDecimal();

            final BigDecimal tmpRet = BigFunction.TAN.invoke(tmpArg);

            return RationalNumber.valueOf(tmpRet);
        }
    };

    public static final Unary TANH = new Unary() {

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

    public static final Unary VALUE = new Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {
            return arg;
        }
    };

    private static final RationalFunction SET = new RationalFunction();

    public static RationalFunction getSet() {
        return SET;
    }

    private RationalFunction() {
        super();
    }

    @Override
    public UnaryFunction<RationalNumber> abs() {
        return ABS;
    }

    @Override
    public UnaryFunction<RationalNumber> acos() {
        return ACOS;
    }

    @Override
    public UnaryFunction<RationalNumber> acosh() {
        return ACOSH;
    }

    @Override
    public BinaryFunction<RationalNumber> add() {
        return ADD;
    }

    @Override
    public AggregatorSet<RationalNumber> aggregator() {
        return RationalAggregator.getSet();
    }

    @Override
    public UnaryFunction<RationalNumber> asin() {
        return ASIN;
    }

    @Override
    public UnaryFunction<RationalNumber> asinh() {
        return ASINH;
    }

    @Override
    public UnaryFunction<RationalNumber> atan() {
        return ATAN;
    }

    @Override
    public BinaryFunction<RationalNumber> atan2() {
        return ATAN2;
    }

    @Override
    public UnaryFunction<RationalNumber> atanh() {
        return ATANH;
    }

    @Override
    public UnaryFunction<RationalNumber> cardinality() {
        return CARDINALITY;
    }

    @Override
    public UnaryFunction<RationalNumber> cbrt() {
        return CBRT;
    }

    @Override
    public UnaryFunction<RationalNumber> ceil() {
        return CEIL;
    }

    @Override
    public UnaryFunction<RationalNumber> conjugate() {
        return CONJUGATE;
    }

    @Override
    public UnaryFunction<RationalNumber> cos() {
        return COS;
    }

    @Override
    public UnaryFunction<RationalNumber> cosh() {
        return COSH;
    }

    @Override
    public BinaryFunction<RationalNumber> divide() {
        return DIVIDE;
    }

    @Override
    public Unary enforce(final NumberContext context) {
        return t -> RationalNumber.valueOf(context.enforce(t));
    }

    @Override
    public UnaryFunction<RationalNumber> exp() {
        return EXP;
    }

    @Override
    public UnaryFunction<RationalNumber> expm1() {
        return EXPM1;
    }

    @Override
    public UnaryFunction<RationalNumber> floor() {
        return FLOOR;
    }

    @Override
    public BinaryFunction<RationalNumber> hypot() {
        return HYPOT;
    }

    @Override
    public UnaryFunction<RationalNumber> invert() {
        return INVERT;
    }

    @Override
    public UnaryFunction<RationalNumber> log() {
        return LOG;
    }

    @Override
    public UnaryFunction<RationalNumber> log10() {
        return LOG10;
    }

    @Override
    public UnaryFunction<RationalNumber> log1p() {
        return LOG1P;
    }

    @Override
    public UnaryFunction<RationalNumber> logistic() {
        return LOGISTIC;
    }

    @Override
    public UnaryFunction<RationalNumber> logit() {
        return LOGIT;
    }

    @Override
    public BinaryFunction<RationalNumber> max() {
        return MAX;
    }

    @Override
    public BinaryFunction<RationalNumber> min() {
        return MIN;
    }

    @Override
    public BinaryFunction<RationalNumber> multiply() {
        return MULTIPLY;
    }

    @Override
    public UnaryFunction<RationalNumber> negate() {
        return NEGATE;
    }

    @Override
    public BinaryFunction<RationalNumber> pow() {
        return POW;
    }

    @Override
    public ParameterFunction<RationalNumber> power() {
        return POWER;
    }

    @Override
    public UnaryFunction<RationalNumber> rint() {
        return RINT;
    }

    @Override
    public ParameterFunction<RationalNumber> root() {
        return ROOT;
    }

    @Override
    public ParameterFunction<RationalNumber> scale() {
        return SCALE;
    }

    @Override
    public UnaryFunction<RationalNumber> signum() {
        return SIGNUM;
    }

    @Override
    public UnaryFunction<RationalNumber> sin() {
        return SIN;
    }

    @Override
    public UnaryFunction<RationalNumber> sinh() {
        return SINH;
    }

    @Override
    public UnaryFunction<RationalNumber> sqrt() {
        return SQRT;
    }

    @Override
    public UnaryFunction<RationalNumber> sqrt1px2() {
        return SQRT1PX2;
    }

    @Override
    public BinaryFunction<RationalNumber> subtract() {
        return SUBTRACT;
    }

    @Override
    public UnaryFunction<RationalNumber> tan() {
        return TAN;
    }

    @Override
    public UnaryFunction<RationalNumber> tanh() {
        return TANH;
    }

    @Override
    public UnaryFunction<RationalNumber> value() {
        return VALUE;
    }

}

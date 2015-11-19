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
package org.ojalgo.function;

import java.math.BigDecimal;

import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.type.TypeUtils;

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

    public static RationalFunction getSet() {
        return SET;
    }

    public static final UnaryFunction<RationalNumber> ABS = new Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {
            if (arg.compareTo(RationalNumber.ZERO) == -1) {
                return arg.negate();
            } else {
                return arg;
            }
        }
    };

    public static final UnaryFunction<RationalNumber> ACOS = new Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {

            final BigDecimal tmpArg = TypeUtils.toBigDecimal(arg);

            final BigDecimal tmpRet = BigFunction.ACOS.invoke(tmpArg);

            return RationalNumber.valueOf(tmpRet);
        }
    };

    public static final UnaryFunction<RationalNumber> ACOSH = new Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {

            final RationalNumber tmpNmbr = arg.multiply(arg).subtract(RationalNumber.ONE);

            return RationalFunction.LOG.invoke(arg.add(RationalFunction.SQRT.invoke(tmpNmbr)));
        }
    };

    public static final BinaryFunction<RationalNumber> ADD = new Binary() {

        @Override
        public final RationalNumber invoke(final RationalNumber arg1, final RationalNumber arg2) {
            return arg1.add(arg2);
        }
    };

    public static final UnaryFunction<RationalNumber> ASIN = new Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {

            final BigDecimal tmpArg = TypeUtils.toBigDecimal(arg);

            final BigDecimal tmpRet = BigFunction.ASIN.invoke(tmpArg);

            return RationalNumber.valueOf(tmpRet);
        }
    };

    public static final UnaryFunction<RationalNumber> ASINH = new Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {

            final RationalNumber tmpNmbr = arg.multiply(arg).add(RationalNumber.ONE);

            return RationalFunction.LOG.invoke(arg.add(RationalFunction.SQRT.invoke(tmpNmbr)));
        }
    };

    public static final UnaryFunction<RationalNumber> ATAN = new Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {

            final BigDecimal tmpArg = TypeUtils.toBigDecimal(arg);

            final BigDecimal tmpRet = BigFunction.ATAN.invoke(tmpArg);

            return RationalNumber.valueOf(tmpRet);
        }
    };

    public static final UnaryFunction<RationalNumber> ATANH = new Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {

            final RationalNumber tmpNmbr = arg.add(RationalNumber.ONE).divide(RationalNumber.ONE.subtract(arg));

            return RationalFunction.LOG.invoke(tmpNmbr).divide(RationalNumber.TWO);
        }
    };

    public static final UnaryFunction<RationalNumber> CARDINALITY = new Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {
            return arg.compareTo(RationalNumber.ZERO) == 0 ? RationalNumber.ZERO : RationalNumber.ONE;
        }
    };

    public static final UnaryFunction<RationalNumber> CONJUGATE = new Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {
            return arg.conjugate();
        }
    };

    public static final UnaryFunction<RationalNumber> COS = new Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {

            final BigDecimal tmpArg = TypeUtils.toBigDecimal(arg);

            final BigDecimal tmpRet = BigFunction.COS.invoke(tmpArg);

            return RationalNumber.valueOf(tmpRet);
        }
    };

    public static final UnaryFunction<RationalNumber> COSH = new Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {
            return (EXP.invoke(arg).add(EXP.invoke(arg.negate()))).divide(RationalNumber.TWO);
        }
    };

    public static final BinaryFunction<RationalNumber> DIVIDE = new Binary() {

        @Override
        public final RationalNumber invoke(final RationalNumber arg1, final RationalNumber arg2) {
            return arg1.divide(arg2);
        }
    };

    public static final UnaryFunction<RationalNumber> EXP = new Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {

            final BigDecimal tmpArg = TypeUtils.toBigDecimal(arg);

            final BigDecimal tmpRet = BigFunction.EXP.invoke(tmpArg);

            return RationalNumber.valueOf(tmpRet);
        }
    };

    public static final UnaryFunction<RationalNumber> EXPM1 = new Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {

            final BigDecimal tmpArg = TypeUtils.toBigDecimal(arg);

            final BigDecimal tmpRet = BigFunction.EXPM1.invoke(tmpArg);

            return RationalNumber.valueOf(tmpRet);
        }
    };

    public static final BinaryFunction<RationalNumber> HYPOT = new Binary() {

        @Override
        public final RationalNumber invoke(final RationalNumber arg1, final RationalNumber arg2) {

            final BigDecimal tmpArg1 = arg1.toBigDecimal();
            final BigDecimal tmpArg2 = arg2.toBigDecimal();

            final BigDecimal tmpResult = BigFunction.HYPOT.invoke(tmpArg1, tmpArg2);

            return RationalNumber.valueOf(tmpResult);
        }
    };

    public static final UnaryFunction<RationalNumber> INVERT = new Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {
            return arg.invert();
        }
    };

    public static final UnaryFunction<RationalNumber> LOG = new Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {

            final BigDecimal tmpArg = TypeUtils.toBigDecimal(arg);

            final BigDecimal tmpRet = BigFunction.LOG.invoke(tmpArg);

            return RationalNumber.valueOf(tmpRet);
        }
    };

    public static final UnaryFunction<RationalNumber> LOG10 = new Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {

            final BigDecimal tmpArg = TypeUtils.toBigDecimal(arg);

            final BigDecimal tmpRet = BigFunction.LOG10.invoke(tmpArg);

            return RationalNumber.valueOf(tmpRet);
        }
    };

    public static final UnaryFunction<RationalNumber> LOG1P = new Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {

            final BigDecimal tmpArg = TypeUtils.toBigDecimal(arg);

            final BigDecimal tmpRet = BigFunction.LOG1P.invoke(tmpArg);

            return RationalNumber.valueOf(tmpRet);
        }
    };

    public static final BinaryFunction<RationalNumber> MAX = new Binary() {

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

    public static final BinaryFunction<RationalNumber> MIN = new Binary() {

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

    public static final BinaryFunction<RationalNumber> MULTIPLY = new Binary() {

        @Override
        public final RationalNumber invoke(final RationalNumber arg1, final RationalNumber arg2) {
            return arg1.multiply(arg2);
        }
    };

    public static final UnaryFunction<RationalNumber> NEGATE = new Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {
            return arg.negate();
        }
    };

    public static final BinaryFunction<RationalNumber> POW = new Binary() {

        @Override
        public final RationalNumber invoke(final RationalNumber arg1, final RationalNumber arg2) {
            return EXP.invoke(LOG.invoke(arg1).multiply(arg2));
        }
    };

    public static final ParameterFunction<RationalNumber> POWER = new Parameter() {

        @Override
        public final RationalNumber invoke(final RationalNumber arg, final int param) {

            final BigDecimal tmpArg = arg.toBigDecimal();

            final BigDecimal tmpRet = BigFunction.POWER.invoke(tmpArg, param);

            return RationalNumber.valueOf(tmpRet);
        }
    };

    public static final ParameterFunction<RationalNumber> ROOT = new Parameter() {

        @Override
        public final RationalNumber invoke(final RationalNumber arg, final int param) {

            final BigDecimal tmpArg = TypeUtils.toBigDecimal(arg);

            final BigDecimal tmpRet = BigFunction.ROOT.invoke(tmpArg, param);

            return RationalNumber.valueOf(tmpRet);
        }
    };

    public static final ParameterFunction<RationalNumber> SCALE = new Parameter() {

        @Override
        public final RationalNumber invoke(final RationalNumber arg, final int param) {

            final BigDecimal tmpArg = TypeUtils.toBigDecimal(arg);

            final BigDecimal tmpRet = BigFunction.SCALE.invoke(tmpArg, param);

            return RationalNumber.valueOf(tmpRet);
        }
    };

    public static final UnaryFunction<RationalNumber> SIGNUM = new Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {
            return arg.signum();
        }
    };

    public static final UnaryFunction<RationalNumber> SIN = new Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {

            final BigDecimal tmpArg = TypeUtils.toBigDecimal(arg);

            final BigDecimal tmpRet = BigFunction.SIN.invoke(tmpArg);

            return RationalNumber.valueOf(tmpRet);
        }
    };

    public static final UnaryFunction<RationalNumber> SINH = new Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {
            return (RationalFunction.EXP.invoke(arg).subtract(RationalFunction.EXP.invoke(arg.negate()))).divide(RationalNumber.TWO);
        }
    };

    public static final UnaryFunction<RationalNumber> SQRT = new Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {

            final BigDecimal tmpArg = TypeUtils.toBigDecimal(arg);

            final BigDecimal tmpRet = BigFunction.SQRT.invoke(tmpArg);

            return RationalNumber.valueOf(tmpRet);
        }
    };

    public static final UnaryFunction<RationalNumber> SQRT1PX2 = new Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {
            return SQRT.invoke(RationalNumber.ONE.add(arg.multiply(arg)));
        }
    };

    public static final BinaryFunction<RationalNumber> SUBTRACT = new Binary() {

        @Override
        public final RationalNumber invoke(final RationalNumber arg1, final RationalNumber arg2) {
            return arg1.subtract(arg2);
        }
    };

    public static final UnaryFunction<RationalNumber> TAN = new Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {

            final BigDecimal tmpArg = TypeUtils.toBigDecimal(arg);

            final BigDecimal tmpRet = BigFunction.TAN.invoke(tmpArg);

            return RationalNumber.valueOf(tmpRet);
        }
    };

    public static final UnaryFunction<RationalNumber> TANH = new Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {

            RationalNumber retVal;

            final RationalNumber tmpPlus = RationalFunction.EXP.invoke(arg);
            final RationalNumber tmpMinus = RationalFunction.EXP.invoke(arg.negate());

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

    public static final UnaryFunction<RationalNumber> VALUE = new Unary() {

        public final RationalNumber invoke(final RationalNumber arg) {
            return arg;
        }
    };

    private static final RationalFunction SET = new RationalFunction();

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
    public UnaryFunction<RationalNumber> atanh() {
        return ATANH;
    }

    @Override
    public UnaryFunction<RationalNumber> cardinality() {
        return CARDINALITY;
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
    public UnaryFunction<RationalNumber> exp() {
        return EXP;
    }

    @Override
    public UnaryFunction<RationalNumber> expm1() {
        return EXPM1;
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

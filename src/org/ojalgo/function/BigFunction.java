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

import static org.ojalgo.constant.BigMath.*;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import org.ojalgo.function.aggregator.AggregatorSet;
import org.ojalgo.function.aggregator.BigAggregator;
import org.ojalgo.type.context.NumberContext;

/**
 * Only the reference type parameter (BigDecimal) methods are actually implemented. The methods with the
 * primitive parameters (double) should create a BigDecimal and then delegate to the primitive methods (and do
 * nothing else). If possible the implementations should be pure BigDecimal arithmatic without rounding. If
 * rounding is necessary MathContext.DECIMAL128 should be used. If BigDecimal arithmatic is not possible at
 * all the implementation should delegate to PrimitiveFunction.
 *
 * @author apete
 */
public final class BigFunction extends FunctionSet<BigDecimal> {

    @FunctionalInterface
    public static interface Binary extends BinaryFunction<BigDecimal> {

        default double invoke(final double arg1, final double arg2) {
            return this.invoke(BigDecimal.valueOf(arg1), BigDecimal.valueOf(arg2)).doubleValue();
        }

    }

    @FunctionalInterface
    public static interface Parameter extends ParameterFunction<BigDecimal> {

        default double invoke(final double arg, final int param) {
            return this.invoke(BigDecimal.valueOf(arg), param).doubleValue();
        }

    }

    @FunctionalInterface
    public static interface Unary extends UnaryFunction<BigDecimal> {

        default double invoke(final double arg) {
            return this.invoke(BigDecimal.valueOf(arg)).doubleValue();
        }

    }

    public static final Unary ABS = new Unary() {

        public final BigDecimal invoke(final BigDecimal arg) {
            return arg.abs();
        }

    };

    public static final Unary ACOS = new Unary() {

        public final BigDecimal invoke(final BigDecimal arg) {
            return BigDecimal.valueOf(PrimitiveFunction.ACOS.invoke(arg.doubleValue()));
        }

    };

    public static final Unary ACOSH = new Unary() {

        public final BigDecimal invoke(final BigDecimal arg) {
            return BigDecimal.valueOf(PrimitiveFunction.ACOSH.invoke(arg.doubleValue()));
        }

    };

    public static final Binary ADD = new Binary() {

        @Override
        public final BigDecimal invoke(final BigDecimal arg1, final BigDecimal arg2) {
            return arg1.add(arg2);
        }

    };

    public static final Unary ASIN = new Unary() {

        public final BigDecimal invoke(final BigDecimal arg) {
            return BigDecimal.valueOf(PrimitiveFunction.ASIN.invoke(arg.doubleValue()));
        }

    };

    public static final Unary ASINH = new Unary() {

        public final BigDecimal invoke(final BigDecimal arg) {
            return BigDecimal.valueOf(PrimitiveFunction.ASINH.invoke(arg.doubleValue()));
        }

    };

    public static final Unary ATAN = new Unary() {

        public final BigDecimal invoke(final BigDecimal arg) {
            return BigDecimal.valueOf(PrimitiveFunction.ATAN.invoke(arg.doubleValue()));
        }

    };

    public static final Binary ATAN2 = new Binary() {

        public final BigDecimal invoke(final BigDecimal arg1, final BigDecimal arg2) {
            return BigDecimal.valueOf(PrimitiveFunction.ATAN2.invoke(arg1.doubleValue(), arg2.doubleValue()));
        }

    };

    public static final Unary ATANH = new Unary() {

        public final BigDecimal invoke(final BigDecimal arg) {
            return BigDecimal.valueOf(PrimitiveFunction.ATANH.invoke(arg.doubleValue()));
        }

    };

    public static final Unary CARDINALITY = new Unary() {

        public final BigDecimal invoke(final BigDecimal arg) {
            return arg.signum() == 0 ? ZERO : ONE;
        }

    };

    public static final Unary CBRT = new Unary() {

        public final BigDecimal invoke(final BigDecimal arg) {
            return ROOT.invoke(arg, 3);
        }

    };

    public static final Unary CEIL = new Unary() {

        public final BigDecimal invoke(final BigDecimal arg) {
            return arg.setScale(0, RoundingMode.CEILING);
        }

    };

    public static final Unary CONJUGATE = new Unary() {

        public final BigDecimal invoke(final BigDecimal arg) {
            return arg;
        }

    };

    public static final Unary COS = new Unary() {

        public final BigDecimal invoke(final BigDecimal arg) {
            return BigDecimal.valueOf(PrimitiveFunction.COS.invoke(arg.doubleValue()));
        }

    };

    public static final Unary COSH = new Unary() {

        public final BigDecimal invoke(final BigDecimal arg) {
            return BigDecimal.valueOf(PrimitiveFunction.COSH.invoke(arg.doubleValue()));
        }

    };

    public static final Binary DIVIDE = new Binary() {

        @Override
        public final BigDecimal invoke(final BigDecimal arg1, final BigDecimal arg2) {
            return arg1.divide(arg2, CONTEXT);
        }

    };

    public static final Unary EXP = new Unary() {

        public final BigDecimal invoke(final BigDecimal arg) {
            return BigDecimal.valueOf(PrimitiveFunction.EXP.invoke(arg.doubleValue()));
        }

    };

    public static final Unary EXPM1 = new Unary() {

        public final BigDecimal invoke(final BigDecimal arg) {
            return BigDecimal.valueOf(PrimitiveFunction.EXPM1.invoke(arg.doubleValue()));
        }

    };

    public static final Unary FLOOR = new Unary() {

        public final BigDecimal invoke(final BigDecimal arg) {
            return arg.setScale(0, RoundingMode.FLOOR);
        }

    };

    public static final Binary HYPOT = new Binary() {

        @Override
        public final BigDecimal invoke(final BigDecimal arg1, final BigDecimal arg2) {
            return SQRT.invoke(arg1.multiply(arg1).add(arg2.multiply(arg2)));
        }

    };

    public static final Unary INVERT = new Unary() {

        public final BigDecimal invoke(final BigDecimal arg) {
            return DIVIDE.invoke(ONE, arg);
        }

    };

    public static final Unary LOG = new Unary() {

        public final BigDecimal invoke(final BigDecimal arg) {
            return BigDecimal.valueOf(PrimitiveFunction.LOG.invoke(arg.doubleValue()));
        }

    };

    public static final Unary LOG10 = new Unary() {

        public final BigDecimal invoke(final BigDecimal arg) {
            return BigDecimal.valueOf(PrimitiveFunction.LOG10.invoke(arg.doubleValue()));
        }

    };

    public static final Unary LOG1P = new Unary() {

        public final BigDecimal invoke(final BigDecimal arg) {
            return BigDecimal.valueOf(PrimitiveFunction.LOG1P.invoke(arg.doubleValue()));
        }

    };

    public static final Unary LOGISTIC = new Unary() {

        public final BigDecimal invoke(final BigDecimal arg) {
            return BigDecimal.valueOf(PrimitiveFunction.LOGISTIC.invoke(arg.doubleValue()));
        }

    };

    public static final Unary LOGIT = new Unary() {

        public final BigDecimal invoke(final BigDecimal arg) {
            return BigDecimal.valueOf(PrimitiveFunction.LOGIT.invoke(arg.doubleValue()));
        }

    };

    public static final Binary MAX = new Binary() {

        @Override
        public final BigDecimal invoke(final BigDecimal arg1, final BigDecimal arg2) {
            return arg1.max(arg2);
        }

    };

    public static final Binary MIN = new Binary() {

        @Override
        public final BigDecimal invoke(final BigDecimal arg1, final BigDecimal arg2) {
            return arg1.min(arg2);
        }

    };

    public static final Binary MULTIPLY = new Binary() {

        @Override
        public final BigDecimal invoke(final BigDecimal arg1, final BigDecimal arg2) {
            return arg1.multiply(arg2);
        }

    };

    public static final Unary NEGATE = new Unary() {

        public final BigDecimal invoke(final BigDecimal arg) {
            return arg.negate();
        }

    };

    public static final Binary POW = new Binary() {

        @Override
        public final BigDecimal invoke(final BigDecimal arg1, final BigDecimal arg2) {
            if (arg1.signum() == 0) {
                return ZERO;
            } else if (arg2.signum() == 0) {
                return ONE;
            } else if (arg2.compareTo(ONE) == 0) {
                return arg1;
            } else if (arg1.signum() == -1) {
                throw new IllegalArgumentException();
            } else {
                return EXP.invoke(LOG.invoke(arg1).multiply(arg2));
            }
        }

    };

    public static final Parameter POWER = new Parameter() {

        @Override
        public final BigDecimal invoke(final BigDecimal arg, final int param) {
            return arg.pow(param);
        }

    };

    public static final Unary RINT = new Unary() {

        @Override
        public final BigDecimal invoke(final BigDecimal arg) {
            return arg.setScale(0, CONTEXT.getRoundingMode());
        }

    };

    public static final Parameter ROOT = new Parameter() {

        @Override
        public final BigDecimal invoke(final BigDecimal arg, final int param) {

            if (param <= 0) {

                throw new IllegalArgumentException();

            } else if (param == 1) {

                return arg;

            } else if (param == 2) {

                return SQRT.invoke(arg);

            } else {

                final BigDecimal tmpArg = arg.round(CONTEXT);
                final BigDecimal tmpParam = BigDecimal.valueOf(param);

                BigDecimal retVal = ZERO;
                final double tmpDoubleArg = arg.doubleValue();
                if (!Double.isInfinite(tmpDoubleArg) && !Double.isNaN(tmpDoubleArg)) {
                    retVal = BigDecimal.valueOf(PrimitiveFunction.ROOT.invoke(tmpDoubleArg, param)); // Intial guess
                }

                BigDecimal tmpShouldBeZero;
                while ((tmpShouldBeZero = retVal.pow(param, CONTEXT).subtract(tmpArg)).signum() != 0) {
                    retVal = retVal.subtract(tmpShouldBeZero.divide(tmpParam.multiply(retVal.pow(param - 1)), CONTEXT));
                }

                return retVal;
            }
        }

    };

    public static final Parameter SCALE = new Parameter() {

        @Override
        public final BigDecimal invoke(final BigDecimal arg, final int param) {
            return arg.setScale(param, CONTEXT.getRoundingMode());
        }

    };

    public static final Unary SIGNUM = new Unary() {

        public final BigDecimal invoke(final BigDecimal arg) {
            switch (arg.signum()) {
            case 1:
                return ONE;
            case -1:
                return ONE.negate();
            default:
                return ZERO;
            }
        }

    };

    public static final Unary SIN = new Unary() {

        public final BigDecimal invoke(final BigDecimal arg) {
            return BigDecimal.valueOf(PrimitiveFunction.SIN.invoke(arg.doubleValue()));
        }

    };

    public static final Unary SINH = new Unary() {

        public final BigDecimal invoke(final BigDecimal arg) {
            return BigDecimal.valueOf(PrimitiveFunction.SINH.invoke(arg.doubleValue()));
        }

    };

    public static final Unary SQRT = new Unary() {

        public final BigDecimal invoke(final BigDecimal arg) {

            final BigDecimal tmpArg = arg.round(CONTEXT);

            BigDecimal retVal = ZERO;
            final double tmpDoubleArg = arg.doubleValue();
            if (!Double.isInfinite(tmpDoubleArg) && !Double.isNaN(tmpDoubleArg)) {
                retVal = BigDecimal.valueOf(PrimitiveFunction.SQRT.invoke(tmpDoubleArg)); // Intial guess
            }

            BigDecimal tmpShouldBeZero;
            while ((tmpShouldBeZero = retVal.multiply(retVal, CONTEXT).subtract(tmpArg)).signum() != 0) {
                retVal = retVal.subtract(tmpShouldBeZero.divide(TWO.multiply(retVal), CONTEXT));
            }

            return retVal;
        }

    };

    public static final Unary SQRT1PX2 = new Unary() {

        public final BigDecimal invoke(final BigDecimal arg) {
            return SQRT.invoke(ONE.add(arg.multiply(arg)));
        }

    };

    public static final Binary SUBTRACT = new Binary() {

        @Override
        public final BigDecimal invoke(final BigDecimal arg1, final BigDecimal arg2) {
            return arg1.subtract(arg2);
        }

    };

    public static final Unary TAN = new Unary() {

        public final BigDecimal invoke(final BigDecimal arg) {
            return BigDecimal.valueOf(PrimitiveFunction.TAN.invoke(arg.doubleValue()));
        }

    };

    public static final Unary TANH = new Unary() {

        public final BigDecimal invoke(final BigDecimal arg) {
            return BigDecimal.valueOf(PrimitiveFunction.TANH.invoke(arg.doubleValue()));
        }

    };

    public static final Unary VALUE = new Unary() {

        public final BigDecimal invoke(final BigDecimal arg) {
            return arg;
        }

    };

    private static final MathContext CONTEXT = MathContext.DECIMAL128;
    private static final BigFunction SET = new BigFunction();

    public static BigFunction getSet() {
        return SET;
    }

    private BigFunction() {
        super();
    }

    @Override
    public UnaryFunction<BigDecimal> abs() {
        return ABS;
    }

    @Override
    public UnaryFunction<BigDecimal> acos() {
        return ACOS;
    }

    @Override
    public UnaryFunction<BigDecimal> acosh() {
        return ACOSH;
    }

    @Override
    public BinaryFunction<BigDecimal> add() {
        return ADD;
    }

    @Override
    public AggregatorSet<BigDecimal> aggregator() {
        return BigAggregator.getSet();
    }

    @Override
    public UnaryFunction<BigDecimal> asin() {
        return ASIN;
    }

    @Override
    public UnaryFunction<BigDecimal> asinh() {
        return ASINH;
    }

    @Override
    public UnaryFunction<BigDecimal> atan() {
        return ATAN;
    }

    @Override
    public BinaryFunction<BigDecimal> atan2() {
        return ATAN2;
    }

    @Override
    public UnaryFunction<BigDecimal> atanh() {
        return ATANH;
    }

    @Override
    public UnaryFunction<BigDecimal> cardinality() {
        return CARDINALITY;
    }

    @Override
    public UnaryFunction<BigDecimal> cbrt() {
        return CBRT;
    }

    @Override
    public UnaryFunction<BigDecimal> ceil() {
        return CEIL;
    }

    @Override
    public UnaryFunction<BigDecimal> conjugate() {
        return CONJUGATE;
    }

    @Override
    public UnaryFunction<BigDecimal> cos() {
        return COS;
    }

    @Override
    public UnaryFunction<BigDecimal> cosh() {
        return COSH;
    }

    @Override
    public BinaryFunction<BigDecimal> divide() {
        return DIVIDE;
    }

    @Override
    public Unary enforce(final NumberContext context) {
        return t -> context.enforce(t);
    }

    @Override
    public UnaryFunction<BigDecimal> exp() {
        return EXP;
    }

    @Override
    public UnaryFunction<BigDecimal> expm1() {
        return EXPM1;
    }

    @Override
    public UnaryFunction<BigDecimal> floor() {
        return FLOOR;
    }

    @Override
    public BinaryFunction<BigDecimal> hypot() {
        return HYPOT;
    }

    @Override
    public UnaryFunction<BigDecimal> invert() {
        return INVERT;
    }

    @Override
    public UnaryFunction<BigDecimal> log() {
        return LOG;
    }

    @Override
    public UnaryFunction<BigDecimal> log10() {
        return LOG10;
    }

    @Override
    public UnaryFunction<BigDecimal> log1p() {
        return LOG1P;
    }

    @Override
    public UnaryFunction<BigDecimal> logistic() {
        return LOGISTIC;
    }

    @Override
    public UnaryFunction<BigDecimal> logit() {
        return LOGIT;
    }

    @Override
    public BinaryFunction<BigDecimal> max() {
        return MAX;
    }

    @Override
    public BinaryFunction<BigDecimal> min() {
        return MIN;
    }

    @Override
    public BinaryFunction<BigDecimal> multiply() {
        return MULTIPLY;
    }

    @Override
    public UnaryFunction<BigDecimal> negate() {
        return NEGATE;
    }

    @Override
    public BinaryFunction<BigDecimal> pow() {
        return POW;
    }

    @Override
    public ParameterFunction<BigDecimal> power() {
        return POWER;
    }

    @Override
    public UnaryFunction<BigDecimal> rint() {
        return RINT;
    }

    @Override
    public ParameterFunction<BigDecimal> root() {
        return ROOT;
    }

    @Override
    public ParameterFunction<BigDecimal> scale() {
        return SCALE;
    }

    @Override
    public UnaryFunction<BigDecimal> signum() {
        return SIGNUM;
    }

    @Override
    public UnaryFunction<BigDecimal> sin() {
        return SIN;
    }

    @Override
    public UnaryFunction<BigDecimal> sinh() {
        return SINH;
    }

    @Override
    public UnaryFunction<BigDecimal> sqrt() {
        return SQRT;
    }

    @Override
    public UnaryFunction<BigDecimal> sqrt1px2() {
        return SQRT1PX2;
    }

    @Override
    public BinaryFunction<BigDecimal> subtract() {
        return SUBTRACT;
    }

    @Override
    public UnaryFunction<BigDecimal> tan() {
        return TAN;
    }

    @Override
    public UnaryFunction<BigDecimal> tanh() {
        return TANH;
    }

    @Override
    public UnaryFunction<BigDecimal> value() {
        return VALUE;
    }

}

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

import static org.ojalgo.constant.PrimitiveMath.*;

import org.ojalgo.function.aggregator.AggregatorSet;
import org.ojalgo.function.aggregator.ComplexAggregator;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.PrimitiveScalar;
import org.ojalgo.type.context.NumberContext;

public final class ComplexFunction extends FunctionSet<ComplexNumber> {

    @FunctionalInterface
    public static interface Binary extends BinaryFunction<ComplexNumber> {

        default double invoke(final double arg1, final double arg2) {
            return this.invoke(ComplexNumber.valueOf(arg1), ComplexNumber.valueOf(arg2)).doubleValue();
        }

    }

    @FunctionalInterface
    public static interface Parameter extends ParameterFunction<ComplexNumber> {

        default double invoke(final double arg, final int param) {
            return this.invoke(ComplexNumber.valueOf(arg), param).doubleValue();
        }

    }

    @FunctionalInterface
    public static interface Unary extends UnaryFunction<ComplexNumber> {

        default double invoke(final double arg) {
            return this.invoke(ComplexNumber.valueOf(arg)).doubleValue();
        }

    }

    public static final Unary ABS = new Unary() {

        public final ComplexNumber invoke(final ComplexNumber arg) {
            return ComplexNumber.valueOf(arg.norm());
        }

    };

    public static final Unary ACOS = new Unary() {

        public final ComplexNumber invoke(final ComplexNumber arg) {

            final ComplexNumber tmpSqrt = SQRT.invoke(ComplexNumber.ONE.subtract(arg.multiply(arg)));

            final ComplexNumber tmpNmbr = arg.add(ComplexNumber.I.multiply(tmpSqrt));

            final ComplexNumber tmpLog = LOG.invoke(tmpNmbr);

            return tmpLog.multiply(ComplexNumber.I).negate();
        }

    };

    public static final Unary ACOSH = new Unary() {

        public final ComplexNumber invoke(final ComplexNumber arg) {
            return LOG.invoke(arg.add(SQRT.invoke(arg.multiply(arg).subtract(ONE))));
        }

    };

    public static final Binary ADD = new Binary() {

        @Override
        public final ComplexNumber invoke(final ComplexNumber arg1, final ComplexNumber arg2) {
            return arg1.add(arg2);
        }

    };

    public static final Unary ASIN = new Unary() {

        public final ComplexNumber invoke(final ComplexNumber arg) {

            ComplexNumber tmpNmbr = SQRT.invoke(ComplexNumber.ONE.subtract(POWER.invoke(arg, 2)));

            tmpNmbr = ComplexNumber.I.multiply(arg).add(tmpNmbr);
            final ComplexNumber aNumber = tmpNmbr;

            return LOG.invoke(aNumber).multiply(ComplexNumber.I).negate();
        }

    };

    public static final Unary ASINH = new Unary() {

        public final ComplexNumber invoke(final ComplexNumber arg) {

            final ComplexNumber tmpNmbr = arg.multiply(arg).add(ONE);

            return LOG.invoke(arg.add(SQRT.invoke(tmpNmbr)));
        }

    };

    public static final Unary ATAN = new Unary() {

        public final ComplexNumber invoke(final ComplexNumber arg) {

            final ComplexNumber tmpNmbr = ComplexNumber.I.add(arg).divide(ComplexNumber.I.subtract(arg));

            return LOG.invoke(tmpNmbr).multiply(ComplexNumber.I).divide(TWO);
        }

    };

    public static final Binary ATAN2 = new Binary() {

        public final ComplexNumber invoke(final ComplexNumber arg1, final ComplexNumber arg2) {
            return ATAN.invoke(arg1.divide(arg2));
        }

    };

    public static final Unary ATANH = new Unary() {

        public final ComplexNumber invoke(final ComplexNumber arg) {

            final ComplexNumber tmpNmbr = arg.add(ONE).divide(ComplexNumber.ONE.subtract(arg));

            return LOG.invoke(tmpNmbr).divide(TWO);
        }

    };

    public static final Unary CARDINALITY = new Unary() {

        public final ComplexNumber invoke(final ComplexNumber arg) {
            return PrimitiveScalar.isSmall(ONE, arg.norm()) ? ComplexNumber.ZERO : ComplexNumber.ONE;
        }

    };

    public static final Unary CBRT = new Unary() {

        public final ComplexNumber invoke(final ComplexNumber arg) {

            final double retMod = PrimitiveFunction.CBRT.invoke(arg.norm());
            final double retArg = arg.phase() * THIRD;

            return ComplexNumber.makePolar(retMod, retArg);
        }

    };

    public static final Unary CEIL = new Unary() {

        @Override
        public final ComplexNumber invoke(final ComplexNumber arg) {
            final double tmpRe = PrimitiveFunction.CEIL.invoke(arg.doubleValue());
            final double tmpIm = PrimitiveFunction.CEIL.invoke(arg.i);
            return ComplexNumber.of(tmpRe, tmpIm);
        }

    };

    public static final Unary CONJUGATE = new Unary() {

        public final ComplexNumber invoke(final ComplexNumber arg) {
            return arg.conjugate();
        }

    };

    public static final Unary COS = new Unary() {

        public final ComplexNumber invoke(final ComplexNumber arg) {
            return COSH.invoke(arg.multiply(ComplexNumber.I));
        }

    };

    public static final Unary COSH = new Unary() {

        public final ComplexNumber invoke(final ComplexNumber arg) {
            return (EXP.invoke(arg).add(EXP.invoke(arg.negate()))).divide(TWO);
        }

    };

    public static final Binary DIVIDE = new Binary() {

        @Override
        public final ComplexNumber invoke(final ComplexNumber arg1, final ComplexNumber arg2) {
            return arg1.divide(arg2);
        }

    };

    public static final Unary EXP = new Unary() {

        public final ComplexNumber invoke(final ComplexNumber arg) {

            final double tmpNorm = PrimitiveFunction.EXP.invoke(arg.doubleValue());
            final double tmpPhase = arg.i;

            return ComplexNumber.makePolar(tmpNorm, tmpPhase);
        }

    };

    public static final Unary EXPM1 = new Unary() {

        public final ComplexNumber invoke(final ComplexNumber arg) {

            final double retMod = PrimitiveFunction.EXPM1.invoke(arg.doubleValue());
            final double retArg = arg.i;

            return ComplexNumber.makePolar(retMod, retArg);
        }

    };

    public static final Unary FLOOR = new Unary() {

        @Override
        public final ComplexNumber invoke(final ComplexNumber arg) {
            final double tmpRe = PrimitiveFunction.FLOOR.invoke(arg.doubleValue());
            final double tmpIm = PrimitiveFunction.FLOOR.invoke(arg.i);
            return ComplexNumber.of(tmpRe, tmpIm);
        }

    };

    public static final Binary HYPOT = new Binary() {

        @Override
        public final ComplexNumber invoke(final ComplexNumber arg1, final ComplexNumber arg2) {
            return ComplexNumber.valueOf(PrimitiveFunction.HYPOT.invoke(arg1.norm(), arg2.norm()));
        }

    };

    public static final Unary INVERT = new Unary() {

        public final ComplexNumber invoke(final ComplexNumber arg) {
            return POWER.invoke(arg, -1);
        }

    };

    public static final Unary LOG = new Unary() {

        public final ComplexNumber invoke(final ComplexNumber arg) {

            final double tmpRe = PrimitiveFunction.LOG.invoke(arg.norm());
            final double tmpIm = arg.phase();

            return ComplexNumber.of(tmpRe, tmpIm);
        }

    };

    public static final Unary LOG10 = new Unary() {

        public final ComplexNumber invoke(final ComplexNumber arg) {

            final double retRe = PrimitiveFunction.LOG10.invoke(arg.norm());
            final double retIm = arg.phase();

            return ComplexNumber.of(retRe, retIm);
        }

    };

    public static final Unary LOG1P = new Unary() {

        public final ComplexNumber invoke(final ComplexNumber arg) {

            final double retRe = PrimitiveFunction.LOG1P.invoke(arg.norm());
            final double retIm = arg.phase();

            return ComplexNumber.of(retRe, retIm);
        }

    };

    public static final Unary LOGISTIC = new Unary() {

        public final ComplexNumber invoke(final ComplexNumber arg) {
            // return ONE / (ONE + Math.exp(-arg));
            return ComplexNumber.ONE.divide(ComplexNumber.ONE.add(EXP.invoke(arg.negate())));
        }

    };

    public static final Unary LOGIT = new Unary() {

        public final ComplexNumber invoke(final ComplexNumber arg) {
            // return Math.log(ONE / (ONE - arg));
            return LOG.invoke(ComplexNumber.ONE.divide(ComplexNumber.ONE.subtract(arg)));
        }

    };

    public static final Binary MAX = new Binary() {

        @Override
        public final ComplexNumber invoke(final ComplexNumber arg1, final ComplexNumber arg2) {

            ComplexNumber retVal = null;

            if (arg1.norm() >= arg2.norm()) {
                retVal = arg1;
            } else {
                retVal = arg2;
            }

            return retVal;
        }

    };

    public static final Binary MIN = new Binary() {

        @Override
        public final ComplexNumber invoke(final ComplexNumber arg1, final ComplexNumber arg2) {

            ComplexNumber retVal = null;

            if (arg1.norm() <= arg2.norm()) {
                retVal = arg1;
            } else {
                retVal = arg2;
            }

            return retVal;
        }

    };

    public static final Binary MULTIPLY = new Binary() {

        @Override
        public final ComplexNumber invoke(final ComplexNumber arg1, final ComplexNumber arg2) {
            return arg1.multiply(arg2);
        }

    };

    public static final Unary NEGATE = new Unary() {

        public final ComplexNumber invoke(final ComplexNumber arg) {
            return arg.negate();
        }

    };

    public static final Binary POW = new Binary() {

        @Override
        public final ComplexNumber invoke(final ComplexNumber arg1, final ComplexNumber arg2) {
            return EXP.invoke(LOG.invoke(arg1).multiply(arg2));
        }

    };

    public static final Parameter POWER = new Parameter() {

        @Override
        public final ComplexNumber invoke(final ComplexNumber arg, final int param) {

            final double retMod = PrimitiveFunction.POWER.invoke(arg.norm(), param);
            final double retArg = arg.phase() * param;

            return ComplexNumber.makePolar(retMod, retArg);
        }

    };

    public static final Unary RINT = new Unary() {

        @Override
        public final ComplexNumber invoke(final ComplexNumber arg) {
            final double tmpRe = PrimitiveFunction.RINT.invoke(arg.doubleValue());
            final double tmpIm = PrimitiveFunction.RINT.invoke(arg.i);
            return ComplexNumber.of(tmpRe, tmpIm);
        }

    };

    public static final Parameter ROOT = new Parameter() {

        @Override
        public final ComplexNumber invoke(final ComplexNumber arg, final int param) {

            if (param != 0) {

                final double tmpExp = ONE / param;

                final double retMod = PrimitiveFunction.POW.invoke(arg.norm(), tmpExp);
                final double retArg = arg.phase() * tmpExp;

                return ComplexNumber.makePolar(retMod, retArg);

            } else {

                throw new IllegalArgumentException();
            }
        }

    };

    public static final Parameter SCALE = new Parameter() {

        @Override
        public final ComplexNumber invoke(final ComplexNumber arg, final int param) {
            final double tmpRe = PrimitiveFunction.SCALE.invoke(arg.doubleValue(), param);
            final double tmpIm = PrimitiveFunction.SCALE.invoke(arg.i, param);
            return ComplexNumber.of(tmpRe, tmpIm);
        }

    };

    public static final Unary SIGNUM = new Unary() {

        public final ComplexNumber invoke(final ComplexNumber arg) {
            return arg.signum();
        }

    };

    public static final Unary SIN = new Unary() {

        public final ComplexNumber invoke(final ComplexNumber arg) {
            return SINH.invoke(arg.multiply(ComplexNumber.I)).multiply(ComplexNumber.I.negate());
        }

    };

    public static final Unary SINH = new Unary() {

        public final ComplexNumber invoke(final ComplexNumber arg) {
            return (EXP.invoke(arg).subtract(EXP.invoke(arg.negate()))).divide(TWO);
        }

    };

    public static final Unary SQRT = new Unary() {

        public final ComplexNumber invoke(final ComplexNumber arg) {

            final double retMod = PrimitiveFunction.SQRT.invoke(arg.norm());
            final double retArg = arg.phase() * HALF;

            return ComplexNumber.makePolar(retMod, retArg);
        }

    };

    public static final Unary SQRT1PX2 = new Unary() {

        public final ComplexNumber invoke(final ComplexNumber arg) {
            return SQRT.invoke(ComplexNumber.ONE.add(arg.multiply(arg)));
        }

    };

    public static final Binary SUBTRACT = new Binary() {

        @Override
        public final ComplexNumber invoke(final ComplexNumber arg1, final ComplexNumber arg2) {
            return arg1.subtract(arg2);
        }

    };

    public static final Unary TAN = new Unary() {

        public final ComplexNumber invoke(final ComplexNumber arg) {
            return TANH.invoke(arg.multiply(ComplexNumber.I)).multiply(ComplexNumber.I.negate());
        }

    };

    public static final Unary TANH = new Unary() {

        public final ComplexNumber invoke(final ComplexNumber arg) {

            ComplexNumber retVal;

            final ComplexNumber tmpPlus = EXP.invoke(arg);
            final ComplexNumber tmpMinus = EXP.invoke(arg.negate());

            final ComplexNumber tmpDividend = tmpPlus.subtract(tmpMinus);
            final ComplexNumber tmpDivisor = tmpPlus.add(tmpMinus);

            if (tmpDividend.equals(tmpDivisor)) {
                retVal = ComplexNumber.ONE;
            } else if (tmpDividend.equals(tmpDivisor.negate())) {
                retVal = ComplexNumber.ONE.negate();
            } else {
                retVal = tmpDividend.divide(tmpDivisor);
            }

            return retVal;
        }

    };

    public static final Unary VALUE = new Unary() {

        public final ComplexNumber invoke(final ComplexNumber arg) {
            return arg;
        }

    };

    private static final ComplexFunction SET = new ComplexFunction();

    public static ComplexFunction getSet() {
        return SET;
    }

    private ComplexFunction() {
        super();
    }

    @Override
    public UnaryFunction<ComplexNumber> abs() {
        return ABS;
    }

    @Override
    public UnaryFunction<ComplexNumber> acos() {
        return ACOS;
    }

    @Override
    public UnaryFunction<ComplexNumber> acosh() {
        return ACOSH;
    }

    @Override
    public BinaryFunction<ComplexNumber> add() {
        return ADD;
    }

    @Override
    public AggregatorSet<ComplexNumber> aggregator() {
        return ComplexAggregator.getSet();
    }

    @Override
    public UnaryFunction<ComplexNumber> asin() {
        return ASIN;
    }

    @Override
    public UnaryFunction<ComplexNumber> asinh() {
        return ASINH;
    }

    @Override
    public UnaryFunction<ComplexNumber> atan() {
        return ATAN;
    }

    @Override
    public BinaryFunction<ComplexNumber> atan2() {
        return ATAN2;
    }

    @Override
    public UnaryFunction<ComplexNumber> atanh() {
        return ATANH;
    }

    @Override
    public UnaryFunction<ComplexNumber> cardinality() {
        return CARDINALITY;
    }

    @Override
    public UnaryFunction<ComplexNumber> cbrt() {
        return CBRT;
    }

    @Override
    public UnaryFunction<ComplexNumber> ceil() {
        return CEIL;
    }

    @Override
    public UnaryFunction<ComplexNumber> conjugate() {
        return CONJUGATE;
    }

    @Override
    public UnaryFunction<ComplexNumber> cos() {
        return COS;
    }

    @Override
    public UnaryFunction<ComplexNumber> cosh() {
        return COSH;
    }

    @Override
    public BinaryFunction<ComplexNumber> divide() {
        return DIVIDE;
    }

    @Override
    public Unary enforce(final NumberContext context) {
        return t -> ComplexNumber.valueOf(context.enforce(t));
    }

    @Override
    public UnaryFunction<ComplexNumber> exp() {
        return EXP;
    }

    @Override
    public UnaryFunction<ComplexNumber> expm1() {
        return EXPM1;
    }

    @Override
    public UnaryFunction<ComplexNumber> floor() {
        return FLOOR;
    }

    @Override
    public BinaryFunction<ComplexNumber> hypot() {
        return HYPOT;
    }

    @Override
    public UnaryFunction<ComplexNumber> invert() {
        return INVERT;
    }

    @Override
    public UnaryFunction<ComplexNumber> log() {
        return LOG;
    }

    @Override
    public UnaryFunction<ComplexNumber> log10() {
        return LOG10;
    }

    @Override
    public UnaryFunction<ComplexNumber> log1p() {
        return LOG1P;
    }

    @Override
    public UnaryFunction<ComplexNumber> logistic() {
        return LOGISTIC;
    }

    @Override
    public UnaryFunction<ComplexNumber> logit() {
        return LOGIT;
    }

    @Override
    public BinaryFunction<ComplexNumber> max() {
        return MAX;
    }

    @Override
    public BinaryFunction<ComplexNumber> min() {
        return MIN;
    }

    @Override
    public BinaryFunction<ComplexNumber> multiply() {
        return MULTIPLY;
    }

    @Override
    public UnaryFunction<ComplexNumber> negate() {
        return NEGATE;
    }

    @Override
    public BinaryFunction<ComplexNumber> pow() {
        return POW;
    }

    @Override
    public ParameterFunction<ComplexNumber> power() {
        return POWER;
    }

    @Override
    public UnaryFunction<ComplexNumber> rint() {
        return RINT;
    }

    @Override
    public ParameterFunction<ComplexNumber> root() {
        return ROOT;
    }

    @Override
    public ParameterFunction<ComplexNumber> scale() {
        return SCALE;
    }

    @Override
    public UnaryFunction<ComplexNumber> signum() {
        return SIGNUM;
    }

    @Override
    public UnaryFunction<ComplexNumber> sin() {
        return SIN;
    }

    @Override
    public UnaryFunction<ComplexNumber> sinh() {
        return SINH;
    }

    @Override
    public UnaryFunction<ComplexNumber> sqrt() {
        return SQRT;
    }

    @Override
    public UnaryFunction<ComplexNumber> sqrt1px2() {
        return SQRT1PX2;
    }

    @Override
    public BinaryFunction<ComplexNumber> subtract() {
        return SUBTRACT;
    }

    @Override
    public UnaryFunction<ComplexNumber> tan() {
        return TAN;
    }

    @Override
    public UnaryFunction<ComplexNumber> tanh() {
        return TANH;
    }

    @Override
    public UnaryFunction<ComplexNumber> value() {
        return VALUE;
    }

}

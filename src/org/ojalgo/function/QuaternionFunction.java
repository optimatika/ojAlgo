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
package org.ojalgo.function;

import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.aggregator.AggregatorSet;
import org.ojalgo.function.aggregator.QuaternionAggregator;
import org.ojalgo.scalar.PrimitiveScalar;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.type.context.NumberContext;

public final class QuaternionFunction extends FunctionSet<Quaternion> {

    @FunctionalInterface
    public static interface Binary extends BinaryFunction<Quaternion> {

        default double invoke(final double arg1, final double arg2) {
            return this.invoke(Quaternion.valueOf(arg1), Quaternion.valueOf(arg2)).doubleValue();
        }

    }

    @FunctionalInterface
    public static interface Consumer extends VoidFunction<Quaternion> {

        default void invoke(final double arg) {
            this.invoke(Quaternion.valueOf(arg));
        }

    }

    @FunctionalInterface
    public static interface Parameter extends ParameterFunction<Quaternion> {

        default double invoke(final double arg, final int param) {
            return this.invoke(Quaternion.valueOf(arg), param).doubleValue();
        }

    }

    @FunctionalInterface
    public static interface Predicate extends PredicateFunction<Quaternion> {

        default boolean invoke(final double arg) {
            return this.invoke(Quaternion.valueOf(arg));
        }

    }

    @FunctionalInterface
    public static interface Unary extends UnaryFunction<Quaternion> {

        default double invoke(final double arg) {
            return this.invoke(Quaternion.valueOf(arg)).doubleValue();
        }

    }

    public static final Unary ABS = new Unary() {

        public final Quaternion invoke(final Quaternion arg) {
            return Quaternion.valueOf(arg.norm());
        }

    };

    public static final Unary ACOS = new Unary() {

        public final Quaternion invoke(final Quaternion arg) {

            //            final Quaternion tmpSqrt = SQRT.invoke(Quaternion.ONE.subtract(arg.multiply(arg)));
            //
            //            final Quaternion tmpNmbr1 = arg.subtract(arg.getPureVersor().multiply(tmpSqrt));
            //
            //            final Quaternion tmpLog1 = LOG.invoke(tmpNmbr1);
            //
            //            return tmpLog1.multiply(arg.getPureVersor()).negate();

            return arg.getPureVersor().negate().multiply(ACOSH.invoke(arg));
        }

    };

    public static final Unary ACOSH = new Unary() {

        public final Quaternion invoke(final Quaternion arg) {
            return LOG.invoke(arg.add(SQRT.invoke(arg.multiply(arg).subtract(PrimitiveMath.ONE))));
        }

    };

    public static final Binary ADD = new Binary() {

        @Override
        public final Quaternion invoke(final Quaternion arg1, final Quaternion arg2) {
            return arg1.add(arg2);
        }

    };

    public static final Unary ASIN = new Unary() {

        public final Quaternion invoke(final Quaternion arg) {

            Quaternion tmpNmbr = SQRT.invoke(Quaternion.ONE.subtract(POWER.invoke(arg, 2)));

            tmpNmbr = Quaternion.I.multiply(arg).add(tmpNmbr);
            final Quaternion aNumber = tmpNmbr;

            return LOG.invoke(aNumber).multiply(Quaternion.I).negate();
        }

    };

    public static final Unary ASINH = new Unary() {

        public final Quaternion invoke(final Quaternion arg) {

            final Quaternion tmpNmbr = arg.multiply(arg).add(PrimitiveMath.ONE);

            return LOG.invoke(arg.add(SQRT.invoke(tmpNmbr)));
        }

    };

    public static final Unary ATAN = new Unary() {

        public final Quaternion invoke(final Quaternion arg) {

            final Quaternion tmpNmbr = Quaternion.I.add(arg).divide(Quaternion.I.subtract(arg));

            return LOG.invoke(tmpNmbr).multiply(Quaternion.I).divide(PrimitiveMath.TWO);
        }

    };

    public static final Binary ATAN2 = new Binary() {

        @Override
        public final Quaternion invoke(final Quaternion arg1, final Quaternion arg2) {
            return ATAN.invoke(arg1.divide(arg2));
        }

    };

    public static final Unary ATANH = new Unary() {

        public final Quaternion invoke(final Quaternion arg) {

            final Quaternion tmpNmbr = arg.add(PrimitiveMath.ONE).divide(Quaternion.ONE.subtract(arg));

            return LOG.invoke(tmpNmbr).divide(PrimitiveMath.TWO);
        }

    };

    public static final Unary CARDINALITY = new Unary() {

        public final Quaternion invoke(final Quaternion arg) {
            return PrimitiveScalar.isSmall(PrimitiveMath.ONE, arg.norm()) ? Quaternion.ZERO : Quaternion.ONE;
        }

    };

    public static final Unary CBRT = new Unary() {

        public final Quaternion invoke(final Quaternion arg) {
            return ROOT.invoke(arg, 3);
        }

    };

    public static final Unary CEIL = new Unary() {

        @Override
        public final Quaternion invoke(final Quaternion arg) {
            final double tmpScalar = PrimitiveFunction.CEIL.invoke(arg.scalar());
            final double tmpI = PrimitiveFunction.CEIL.invoke(arg.i);
            final double tmpJ = PrimitiveFunction.CEIL.invoke(arg.j);
            final double tmpK = PrimitiveFunction.CEIL.invoke(arg.k);
            return Quaternion.of(tmpScalar, tmpI, tmpJ, tmpK);
        }

    };

    public static final Unary CONJUGATE = new Unary() {

        public final Quaternion invoke(final Quaternion arg) {
            return arg.conjugate();
        }

    };

    public static final Unary COS = new Unary() {

        public final Quaternion invoke(final Quaternion arg) {
            return COSH.invoke(arg.multiply(Quaternion.I));
        }

    };

    public static final Unary COSH = new Unary() {

        public final Quaternion invoke(final Quaternion arg) {
            return (EXP.invoke(arg).add(EXP.invoke(arg.negate()))).divide(PrimitiveMath.TWO);
        }

    };

    public static final Binary DIVIDE = new Binary() {

        @Override
        public final Quaternion invoke(final Quaternion arg1, final Quaternion arg2) {
            return arg1.divide(arg2);
        }

    };

    public static final Unary EXP = new Unary() {

        public final Quaternion invoke(final Quaternion arg) {

            if (arg.isReal()) {

                final double tmpScalar = PrimitiveFunction.EXP.invoke(arg.scalar());

                return Quaternion.valueOf(tmpScalar);

            } else {

                final double tmpNorm = PrimitiveFunction.EXP.invoke(arg.scalar());
                final double[] tmpUnit = arg.unit();
                final double tmpPhase = arg.getVectorLength();

                return Quaternion.makePolar(tmpNorm, tmpUnit, tmpPhase);
            }

            // final double tmpNorm = PrimitiveFunction.EXP.invoke(arg.doubleValue());
            // final double tmpPhase = arg.i;
            //
            // return ComplexNumber.makePolar(tmpNorm, tmpPhase);
        }

    };

    public static final Unary EXPM1 = new Unary() {

        public final Quaternion invoke(final Quaternion arg) {
            return EXP.invoke(arg).subtract(_1_0);
        }

    };

    public static final Unary FLOOR = new Unary() {

        @Override
        public final Quaternion invoke(final Quaternion arg) {
            final double tmpScalar = PrimitiveFunction.FLOOR.invoke(arg.scalar());
            final double tmpI = PrimitiveFunction.FLOOR.invoke(arg.i);
            final double tmpJ = PrimitiveFunction.FLOOR.invoke(arg.j);
            final double tmpK = PrimitiveFunction.FLOOR.invoke(arg.k);
            return Quaternion.of(tmpScalar, tmpI, tmpJ, tmpK);
        }

    };

    public static final Binary HYPOT = new Binary() {

        @Override
        public final Quaternion invoke(final Quaternion arg1, final Quaternion arg2) {
            return Quaternion.valueOf(PrimitiveFunction.HYPOT.invoke(arg1.norm(), arg2.norm()));
        }

    };

    public static final Unary INVERT = new Unary() {

        public final Quaternion invoke(final Quaternion arg) {
            return POWER.invoke(arg, -1);
        }

    };

    public static final Unary LOG = new Unary() {

        public final Quaternion invoke(final Quaternion arg) {

            final double tmpNorm = arg.norm();
            final double[] tmpUnitVector = arg.unit();
            final double tmpPhase = PrimitiveFunction.ACOS.invoke(arg.scalar() / tmpNorm);

            final double tmpScalar = PrimitiveFunction.LOG.invoke(tmpNorm);
            final double tmpI = tmpUnitVector[0] * tmpPhase;
            final double tmpJ = tmpUnitVector[1] * tmpPhase;
            final double tmpK = tmpUnitVector[2] * tmpPhase;

            return Quaternion.of(tmpScalar, tmpI, tmpJ, tmpK);
        }

    };

    public static final Unary LOG10 = new Unary() {

        public final Quaternion invoke(final Quaternion arg) {
            return LOG.invoke(arg).divide(PrimitiveFunction.LOG.invoke(10.0));
        }

    };

    public static final Unary LOG1P = new Unary() {

        public final Quaternion invoke(final Quaternion arg) {
            return LOG.invoke(arg.add(_1_0));
        }

    };

    public static final Unary LOGISTIC = new Unary() {

        public final Quaternion invoke(final Quaternion arg) {
            return Quaternion.ONE.divide(Quaternion.ONE.add(EXP.invoke(arg.negate())));
        }

    };

    public static final Unary LOGIT = new Unary() {

        public final Quaternion invoke(final Quaternion arg) {
            return LOG.invoke(Quaternion.ONE.divide(Quaternion.ONE.subtract(arg)));
        }

    };

    public static final Binary MAX = new Binary() {

        @Override
        public final Quaternion invoke(final Quaternion arg1, final Quaternion arg2) {

            Quaternion retVal = null;

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
        public final Quaternion invoke(final Quaternion arg1, final Quaternion arg2) {

            Quaternion retVal = null;

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
        public final Quaternion invoke(final Quaternion arg1, final Quaternion arg2) {
            return arg1.multiply(arg2);
        }

    };

    public static final Unary NEGATE = new Unary() {

        public final Quaternion invoke(final Quaternion arg) {
            return arg.negate();
        }

    };

    public static final Binary POW = new Binary() {

        @Override
        public final Quaternion invoke(final Quaternion arg1, final Quaternion arg2) {
            return EXP.invoke(LOG.invoke(arg1).multiply(arg2));
        }

    };

    public static final Parameter POWER = new Parameter() {

        @Override
        public final Quaternion invoke(final Quaternion arg, final int param) {

            final Quaternion tmpInvoke = LOG.invoke(arg);
            final Quaternion tmpMultiply = tmpInvoke.multiply(param);
            return EXP.invoke(tmpMultiply);
        }

    };

    public static final Unary RINT = new Unary() {

        @Override
        public final Quaternion invoke(final Quaternion arg) {
            final double tmpScalar = PrimitiveFunction.RINT.invoke(arg.scalar());
            final double tmpI = PrimitiveFunction.RINT.invoke(arg.i);
            final double tmpJ = PrimitiveFunction.RINT.invoke(arg.j);
            final double tmpK = PrimitiveFunction.RINT.invoke(arg.k);
            return Quaternion.of(tmpScalar, tmpI, tmpJ, tmpK);
        }

    };

    public static final Parameter ROOT = new Parameter() {

        @Override
        public final Quaternion invoke(final Quaternion arg, final int param) {

            if (param != 0) {

                return EXP.invoke(LOG.invoke(arg).divide(param));

            } else {

                throw new IllegalArgumentException();
            }
        }

    };

    public static final Parameter SCALE = new Parameter() {

        @Override
        public final Quaternion invoke(final Quaternion arg, final int param) {
            final double tmpScalar = PrimitiveFunction.SCALE.invoke(arg.scalar(), param);
            final double tmpI = PrimitiveFunction.SCALE.invoke(arg.i, param);
            final double tmpJ = PrimitiveFunction.SCALE.invoke(arg.j, param);
            final double tmpK = PrimitiveFunction.SCALE.invoke(arg.k, param);
            return Quaternion.of(tmpScalar, tmpI, tmpJ, tmpK);
        }

    };

    public static final Unary SIGNUM = new Unary() {

        public final Quaternion invoke(final Quaternion arg) {
            return arg.signum();
        }

    };

    public static final Unary SIN = new Unary() {

        public final Quaternion invoke(final Quaternion arg) {
            return SINH.invoke(arg.multiply(Quaternion.I)).multiply(Quaternion.I.negate());
        }

    };

    public static final Unary SINH = new Unary() {

        public final Quaternion invoke(final Quaternion arg) {
            return (EXP.invoke(arg).subtract(EXP.invoke(arg.negate()))).divide(PrimitiveMath.TWO);
        }

    };

    public static final Unary SQRT = new Unary() {

        public final Quaternion invoke(final Quaternion arg) {
            return ROOT.invoke(arg, 2);
        }

    };

    public static final Unary SQRT1PX2 = new Unary() {

        public final Quaternion invoke(final Quaternion arg) {
            return SQRT.invoke(Quaternion.ONE.add(arg.multiply(arg)));
        }

    };

    public static final Binary SUBTRACT = new Binary() {

        @Override
        public final Quaternion invoke(final Quaternion arg1, final Quaternion arg2) {
            return arg1.subtract(arg2);
        }

    };

    public static final Unary TAN = new Unary() {

        public final Quaternion invoke(final Quaternion arg) {
            return TANH.invoke(arg.multiply(Quaternion.I)).multiply(Quaternion.I.negate());
        }

    };

    public static final Unary TANH = new Unary() {

        public final Quaternion invoke(final Quaternion arg) {

            Quaternion retVal;

            final Quaternion tmpPlus = EXP.invoke(arg);
            final Quaternion tmpMinus = EXP.invoke(arg.negate());

            final Quaternion tmpDividend = tmpPlus.subtract(tmpMinus);
            final Quaternion tmpDivisor = tmpPlus.add(tmpMinus);

            if (tmpDividend.equals(tmpDivisor)) {
                retVal = Quaternion.ONE;
            } else if (tmpDividend.equals(tmpDivisor.negate())) {
                retVal = Quaternion.ONE.negate();
            } else {
                retVal = tmpDividend.divide(tmpDivisor);
            }

            return retVal;
        }

    };

    public static final Unary VALUE = new Unary() {

        public final Quaternion invoke(final Quaternion arg) {
            return arg;
        }

    };

    private static final double _1_0 = 1.0;

    private static final QuaternionFunction SET = new QuaternionFunction();

    public static QuaternionFunction getSet() {
        return SET;
    }

    private QuaternionFunction() {
        super();
    }

    @Override
    public UnaryFunction<Quaternion> abs() {
        return ABS;
    }

    @Override
    public UnaryFunction<Quaternion> acos() {
        return ACOS;
    }

    @Override
    public UnaryFunction<Quaternion> acosh() {
        return ACOSH;
    }

    @Override
    public BinaryFunction<Quaternion> add() {
        return ADD;
    }

    @Override
    public AggregatorSet<Quaternion> aggregator() {
        return QuaternionAggregator.getSet();
    }

    @Override
    public UnaryFunction<Quaternion> asin() {
        return ASIN;
    }

    @Override
    public UnaryFunction<Quaternion> asinh() {
        return ASINH;
    }

    @Override
    public UnaryFunction<Quaternion> atan() {
        return ATAN;
    }

    @Override
    public BinaryFunction<Quaternion> atan2() {
        return ATAN2;
    }

    @Override
    public UnaryFunction<Quaternion> atanh() {
        return ATANH;
    }

    @Override
    public UnaryFunction<Quaternion> cardinality() {
        return CARDINALITY;
    }

    @Override
    public UnaryFunction<Quaternion> cbrt() {
        return CBRT;
    }

    @Override
    public UnaryFunction<Quaternion> ceil() {
        return CEIL;
    }

    @Override
    public UnaryFunction<Quaternion> conjugate() {
        return CONJUGATE;
    }

    @Override
    public UnaryFunction<Quaternion> cos() {
        return COS;
    }

    @Override
    public UnaryFunction<Quaternion> cosh() {
        return COSH;
    }

    @Override
    public BinaryFunction<Quaternion> divide() {
        return DIVIDE;
    }

    @Override
    public Unary enforce(final NumberContext context) {
        return t -> Quaternion.valueOf(context.enforce(t));
    }

    @Override
    public UnaryFunction<Quaternion> exp() {
        return EXP;
    }

    @Override
    public UnaryFunction<Quaternion> expm1() {
        return EXPM1;
    }

    @Override
    public UnaryFunction<Quaternion> floor() {
        return FLOOR;
    }

    @Override
    public BinaryFunction<Quaternion> hypot() {
        return HYPOT;
    }

    @Override
    public UnaryFunction<Quaternion> invert() {
        return INVERT;
    }

    @Override
    public UnaryFunction<Quaternion> log() {
        return LOG;
    }

    @Override
    public UnaryFunction<Quaternion> log10() {
        return LOG10;
    }

    @Override
    public UnaryFunction<Quaternion> log1p() {
        return LOG1P;
    }

    @Override
    public UnaryFunction<Quaternion> logistic() {
        return LOGISTIC;
    }

    @Override
    public UnaryFunction<Quaternion> logit() {
        return LOGIT;
    }

    @Override
    public BinaryFunction<Quaternion> max() {
        return MAX;
    }

    @Override
    public BinaryFunction<Quaternion> min() {
        return MIN;
    }

    @Override
    public BinaryFunction<Quaternion> multiply() {
        return MULTIPLY;
    }

    @Override
    public UnaryFunction<Quaternion> negate() {
        return NEGATE;
    }

    @Override
    public BinaryFunction<Quaternion> pow() {
        return POW;
    }

    @Override
    public ParameterFunction<Quaternion> power() {
        return POWER;
    }

    @Override
    public UnaryFunction<Quaternion> rint() {
        return RINT;
    }

    @Override
    public ParameterFunction<Quaternion> root() {
        return ROOT;
    }

    @Override
    public ParameterFunction<Quaternion> scale() {
        return SCALE;
    }

    @Override
    public UnaryFunction<Quaternion> signum() {
        return SIGNUM;
    }

    @Override
    public UnaryFunction<Quaternion> sin() {
        return SIN;
    }

    @Override
    public UnaryFunction<Quaternion> sinh() {
        return SINH;
    }

    @Override
    public UnaryFunction<Quaternion> sqrt() {
        return SQRT;
    }

    @Override
    public UnaryFunction<Quaternion> sqrt1px2() {
        return SQRT1PX2;
    }

    @Override
    public BinaryFunction<Quaternion> subtract() {
        return SUBTRACT;
    }

    @Override
    public UnaryFunction<Quaternion> tan() {
        return TAN;
    }

    @Override
    public UnaryFunction<Quaternion> tanh() {
        return TANH;
    }

    @Override
    public UnaryFunction<Quaternion> value() {
        return VALUE;
    }

}

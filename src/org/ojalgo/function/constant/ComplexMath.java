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
package org.ojalgo.function.constant;

import static org.ojalgo.function.constant.PrimitiveMath.*;

import org.ojalgo.function.ComplexFunction;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.PrimitiveScalar;

public abstract class ComplexMath {

    public static final ComplexFunction.Unary ABS = new ComplexFunction.Unary() {

        public final ComplexNumber invoke(final ComplexNumber arg) {
            return ComplexNumber.valueOf(arg.norm());
        }

    };
    public static final ComplexFunction.Unary ACOS = new ComplexFunction.Unary() {

        public final ComplexNumber invoke(final ComplexNumber arg) {

            final ComplexNumber tmpSqrt = SQRT.invoke(ComplexNumber.ONE.subtract(arg.multiply(arg)));

            final ComplexNumber tmpNmbr = arg.add(ComplexNumber.I.multiply(tmpSqrt));

            final ComplexNumber tmpLog = LOG.invoke(tmpNmbr);

            return tmpLog.multiply(ComplexNumber.I).negate();
        }

    };
    public static final ComplexFunction.Unary ACOSH = new ComplexFunction.Unary() {

        public final ComplexNumber invoke(final ComplexNumber arg) {
            return LOG.invoke(arg.add(SQRT.invoke(arg.multiply(arg).subtract(ONE))));
        }

    };
    public static final ComplexFunction.Binary ADD = new ComplexFunction.Binary() {

        @Override
        public final ComplexNumber invoke(final ComplexNumber arg1, final ComplexNumber arg2) {
            return arg1.add(arg2);
        }

    };
    public static final ComplexFunction.Unary ASIN = new ComplexFunction.Unary() {

        public final ComplexNumber invoke(final ComplexNumber arg) {

            ComplexNumber tmpNmbr = SQRT.invoke(ComplexNumber.ONE.subtract(POWER.invoke(arg, 2)));

            tmpNmbr = ComplexNumber.I.multiply(arg).add(tmpNmbr);
            final ComplexNumber aNumber = tmpNmbr;

            return LOG.invoke(aNumber).multiply(ComplexNumber.I).negate();
        }

    };
    public static final ComplexFunction.Unary ASINH = new ComplexFunction.Unary() {

        public final ComplexNumber invoke(final ComplexNumber arg) {

            final ComplexNumber tmpNmbr = arg.multiply(arg).add(ONE);

            return LOG.invoke(arg.add(SQRT.invoke(tmpNmbr)));
        }

    };
    public static final ComplexFunction.Unary ATAN = new ComplexFunction.Unary() {

        public final ComplexNumber invoke(final ComplexNumber arg) {

            final ComplexNumber tmpNmbr = ComplexNumber.I.add(arg).divide(ComplexNumber.I.subtract(arg));

            return LOG.invoke(tmpNmbr).multiply(ComplexNumber.I).divide(TWO);
        }

    };
    public static final ComplexFunction.Binary ATAN2 = new ComplexFunction.Binary() {

        public final ComplexNumber invoke(final ComplexNumber arg1, final ComplexNumber arg2) {
            return ATAN.invoke(arg1.divide(arg2));
        }

    };
    public static final ComplexFunction.Unary ATANH = new ComplexFunction.Unary() {

        public final ComplexNumber invoke(final ComplexNumber arg) {

            final ComplexNumber tmpNmbr = arg.add(ONE).divide(ComplexNumber.ONE.subtract(arg));

            return LOG.invoke(tmpNmbr).divide(TWO);
        }

    };
    public static final ComplexFunction.Unary CARDINALITY = new ComplexFunction.Unary() {

        public final ComplexNumber invoke(final ComplexNumber arg) {
            return PrimitiveScalar.isSmall(ONE, arg.norm()) ? ComplexNumber.ZERO : ComplexNumber.ONE;
        }

    };
    public static final ComplexFunction.Unary CBRT = new ComplexFunction.Unary() {

        public final ComplexNumber invoke(final ComplexNumber arg) {

            final double retMod = PrimitiveMath.CBRT.invoke(arg.norm());
            final double retArg = arg.phase() * THIRD;

            return ComplexNumber.makePolar(retMod, retArg);
        }

    };
    public static final ComplexFunction.Unary CEIL = new ComplexFunction.Unary() {

        @Override
        public final ComplexNumber invoke(final ComplexNumber arg) {
            final double tmpRe = PrimitiveMath.CEIL.invoke(arg.doubleValue());
            final double tmpIm = PrimitiveMath.CEIL.invoke(arg.i);
            return ComplexNumber.of(tmpRe, tmpIm);
        }

    };
    public static final ComplexFunction.Unary CONJUGATE = new ComplexFunction.Unary() {

        public final ComplexNumber invoke(final ComplexNumber arg) {
            return arg.conjugate();
        }

    };
    public static final ComplexFunction.Unary COS = new ComplexFunction.Unary() {

        public final ComplexNumber invoke(final ComplexNumber arg) {
            return COSH.invoke(arg.multiply(ComplexNumber.I));
        }

    };
    public static final ComplexFunction.Unary COSH = new ComplexFunction.Unary() {

        public final ComplexNumber invoke(final ComplexNumber arg) {
            return (EXP.invoke(arg).add(EXP.invoke(arg.negate()))).divide(TWO);
        }

    };
    public static final ComplexFunction.Binary DIVIDE = new ComplexFunction.Binary() {

        @Override
        public final ComplexNumber invoke(final ComplexNumber arg1, final ComplexNumber arg2) {
            return arg1.divide(arg2);
        }

    };
    public static final ComplexFunction.Unary EXP = new ComplexFunction.Unary() {

        public final ComplexNumber invoke(final ComplexNumber arg) {

            final double tmpNorm = PrimitiveMath.EXP.invoke(arg.doubleValue());
            final double tmpPhase = arg.i;

            return ComplexNumber.makePolar(tmpNorm, tmpPhase);
        }

    };
    public static final ComplexFunction.Unary EXPM1 = new ComplexFunction.Unary() {

        public final ComplexNumber invoke(final ComplexNumber arg) {

            final double retMod = PrimitiveMath.EXPM1.invoke(arg.doubleValue());
            final double retArg = arg.i;

            return ComplexNumber.makePolar(retMod, retArg);
        }

    };
    public static final ComplexFunction.Unary FLOOR = new ComplexFunction.Unary() {

        @Override
        public final ComplexNumber invoke(final ComplexNumber arg) {
            final double tmpRe = PrimitiveMath.FLOOR.invoke(arg.doubleValue());
            final double tmpIm = PrimitiveMath.FLOOR.invoke(arg.i);
            return ComplexNumber.of(tmpRe, tmpIm);
        }

    };
    public static final ComplexFunction.Binary HYPOT = new ComplexFunction.Binary() {

        @Override
        public final ComplexNumber invoke(final ComplexNumber arg1, final ComplexNumber arg2) {
            return ComplexNumber.valueOf(PrimitiveMath.HYPOT.invoke(arg1.norm(), arg2.norm()));
        }

    };
    public static final ComplexFunction.Unary INVERT = new ComplexFunction.Unary() {

        public final ComplexNumber invoke(final ComplexNumber arg) {
            return POWER.invoke(arg, -1);
        }

    };
    public static final ComplexFunction.Unary LOG = new ComplexFunction.Unary() {

        public final ComplexNumber invoke(final ComplexNumber arg) {

            final double tmpRe = PrimitiveMath.LOG.invoke(arg.norm());
            final double tmpIm = arg.phase();

            return ComplexNumber.of(tmpRe, tmpIm);
        }

    };
    public static final ComplexFunction.Unary LOG10 = new ComplexFunction.Unary() {

        public final ComplexNumber invoke(final ComplexNumber arg) {

            final double retRe = PrimitiveMath.LOG10.invoke(arg.norm());
            final double retIm = arg.phase();

            return ComplexNumber.of(retRe, retIm);
        }

    };
    public static final ComplexFunction.Unary LOG1P = new ComplexFunction.Unary() {

        public final ComplexNumber invoke(final ComplexNumber arg) {

            final double retRe = PrimitiveMath.LOG1P.invoke(arg.norm());
            final double retIm = arg.phase();

            return ComplexNumber.of(retRe, retIm);
        }

    };
    public static final ComplexFunction.Unary LOGISTIC = new ComplexFunction.Unary() {

        public final ComplexNumber invoke(final ComplexNumber arg) {
            // return ONE / (ONE + Math.exp(-arg));
            return ComplexNumber.ONE.divide(ComplexNumber.ONE.add(EXP.invoke(arg.negate())));
        }

    };
    public static final ComplexFunction.Unary LOGIT = new ComplexFunction.Unary() {

        public final ComplexNumber invoke(final ComplexNumber arg) {
            // return Math.log(ONE / (ONE - arg));
            return LOG.invoke(ComplexNumber.ONE.divide(ComplexNumber.ONE.subtract(arg)));
        }

    };
    public static final ComplexFunction.Binary MAX = new ComplexFunction.Binary() {

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
    public static final ComplexFunction.Binary MIN = new ComplexFunction.Binary() {

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
    public static final ComplexFunction.Binary MULTIPLY = new ComplexFunction.Binary() {

        @Override
        public final ComplexNumber invoke(final ComplexNumber arg1, final ComplexNumber arg2) {
            return arg1.multiply(arg2);
        }

    };
    public static final ComplexFunction.Unary NEGATE = new ComplexFunction.Unary() {

        public final ComplexNumber invoke(final ComplexNumber arg) {
            return arg.negate();
        }

    };
    public static final ComplexFunction.Binary POW = new ComplexFunction.Binary() {

        @Override
        public final ComplexNumber invoke(final ComplexNumber arg1, final ComplexNumber arg2) {
            return EXP.invoke(LOG.invoke(arg1).multiply(arg2));
        }

    };
    public static final ComplexFunction.Parameter POWER = new ComplexFunction.Parameter() {

        @Override
        public final ComplexNumber invoke(final ComplexNumber arg, final int param) {

            final double retMod = PrimitiveMath.POWER.invoke(arg.norm(), param);
            final double retArg = arg.phase() * param;

            return ComplexNumber.makePolar(retMod, retArg);
        }

    };
    public static final ComplexFunction.Unary RINT = new ComplexFunction.Unary() {

        @Override
        public final ComplexNumber invoke(final ComplexNumber arg) {
            final double tmpRe = PrimitiveMath.RINT.invoke(arg.doubleValue());
            final double tmpIm = PrimitiveMath.RINT.invoke(arg.i);
            return ComplexNumber.of(tmpRe, tmpIm);
        }

    };
    public static final ComplexFunction.Parameter ROOT = new ComplexFunction.Parameter() {

        @Override
        public final ComplexNumber invoke(final ComplexNumber arg, final int param) {

            if (param != 0) {

                final double tmpExp = ONE / param;

                final double retMod = PrimitiveMath.POW.invoke(arg.norm(), tmpExp);
                final double retArg = arg.phase() * tmpExp;

                return ComplexNumber.makePolar(retMod, retArg);

            } else {

                throw new IllegalArgumentException();
            }
        }

    };
    public static final ComplexFunction.Parameter SCALE = new ComplexFunction.Parameter() {

        @Override
        public final ComplexNumber invoke(final ComplexNumber arg, final int param) {
            final double tmpRe = PrimitiveMath.SCALE.invoke(arg.doubleValue(), param);
            final double tmpIm = PrimitiveMath.SCALE.invoke(arg.i, param);
            return ComplexNumber.of(tmpRe, tmpIm);
        }

    };
    public static final ComplexFunction.Unary SIGNUM = new ComplexFunction.Unary() {

        public final ComplexNumber invoke(final ComplexNumber arg) {
            return arg.signum();
        }

    };
    public static final ComplexFunction.Unary SIN = new ComplexFunction.Unary() {

        public final ComplexNumber invoke(final ComplexNumber arg) {
            return SINH.invoke(arg.multiply(ComplexNumber.I)).multiply(ComplexNumber.I.negate());
        }

    };
    public static final ComplexFunction.Unary SINH = new ComplexFunction.Unary() {

        public final ComplexNumber invoke(final ComplexNumber arg) {
            return (EXP.invoke(arg).subtract(EXP.invoke(arg.negate()))).divide(TWO);
        }

    };
    public static final ComplexFunction.Unary SQRT = new ComplexFunction.Unary() {

        public final ComplexNumber invoke(final ComplexNumber arg) {

            final double retMod = PrimitiveMath.SQRT.invoke(arg.norm());
            final double retArg = arg.phase() * HALF;

            return ComplexNumber.makePolar(retMod, retArg);
        }

    };
    public static final ComplexFunction.Unary SQRT1PX2 = new ComplexFunction.Unary() {

        public final ComplexNumber invoke(final ComplexNumber arg) {
            return SQRT.invoke(ComplexNumber.ONE.add(arg.multiply(arg)));
        }

    };
    public static final ComplexFunction.Binary SUBTRACT = new ComplexFunction.Binary() {

        @Override
        public final ComplexNumber invoke(final ComplexNumber arg1, final ComplexNumber arg2) {
            return arg1.subtract(arg2);
        }

    };
    public static final ComplexFunction.Unary TAN = new ComplexFunction.Unary() {

        public final ComplexNumber invoke(final ComplexNumber arg) {
            return TANH.invoke(arg.multiply(ComplexNumber.I)).multiply(ComplexNumber.I.negate());
        }

    };
    public static final ComplexFunction.Unary TANH = new ComplexFunction.Unary() {

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
    public static final ComplexFunction.Unary VALUE = new ComplexFunction.Unary() {

        public final ComplexNumber invoke(final ComplexNumber arg) {
            return arg;
        }

    };

}

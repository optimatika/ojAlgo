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

import org.ojalgo.function.QuaternionFunction;
import org.ojalgo.scalar.PrimitiveScalar;
import org.ojalgo.scalar.Quaternion;

public abstract class QuaternionMath {

    public static final QuaternionFunction.Unary ABS = new QuaternionFunction.Unary() {
    
        public final Quaternion invoke(final Quaternion arg) {
            return Quaternion.valueOf(arg.norm());
        }
    
    };
    public static final QuaternionFunction.Unary ACOS = new QuaternionFunction.Unary() {
    
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
    public static final QuaternionFunction.Unary ACOSH = new QuaternionFunction.Unary() {
    
        public final Quaternion invoke(final Quaternion arg) {
            return LOG.invoke(arg.add(SQRT.invoke(arg.multiply(arg).subtract(PrimitiveMath.ONE))));
        }
    
    };
    public static final QuaternionFunction.Binary ADD = new QuaternionFunction.Binary() {
    
        @Override
        public final Quaternion invoke(final Quaternion arg1, final Quaternion arg2) {
            return arg1.add(arg2);
        }
    
    };
    public static final QuaternionFunction.Unary ASIN = new QuaternionFunction.Unary() {
    
        public final Quaternion invoke(final Quaternion arg) {
    
            Quaternion tmpNmbr = SQRT.invoke(Quaternion.ONE.subtract(POWER.invoke(arg, 2)));
    
            tmpNmbr = Quaternion.I.multiply(arg).add(tmpNmbr);
            final Quaternion aNumber = tmpNmbr;
    
            return LOG.invoke(aNumber).multiply(Quaternion.I).negate();
        }
    
    };
    public static final QuaternionFunction.Unary ASINH = new QuaternionFunction.Unary() {
    
        public final Quaternion invoke(final Quaternion arg) {
    
            final Quaternion tmpNmbr = arg.multiply(arg).add(PrimitiveMath.ONE);
    
            return LOG.invoke(arg.add(SQRT.invoke(tmpNmbr)));
        }
    
    };
    public static final QuaternionFunction.Unary ATAN = new QuaternionFunction.Unary() {
    
        public final Quaternion invoke(final Quaternion arg) {
    
            final Quaternion tmpNmbr = Quaternion.I.add(arg).divide(Quaternion.I.subtract(arg));
    
            return LOG.invoke(tmpNmbr).multiply(Quaternion.I).divide(PrimitiveMath.TWO);
        }
    
    };
    public static final QuaternionFunction.Binary ATAN2 = new QuaternionFunction.Binary() {
    
        @Override
        public final Quaternion invoke(final Quaternion arg1, final Quaternion arg2) {
            return ATAN.invoke(arg1.divide(arg2));
        }
    
    };
    public static final QuaternionFunction.Unary ATANH = new QuaternionFunction.Unary() {
    
        public final Quaternion invoke(final Quaternion arg) {
    
            final Quaternion tmpNmbr = arg.add(PrimitiveMath.ONE).divide(Quaternion.ONE.subtract(arg));
    
            return LOG.invoke(tmpNmbr).divide(PrimitiveMath.TWO);
        }
    
    };
    public static final QuaternionFunction.Unary CARDINALITY = new QuaternionFunction.Unary() {
    
        public final Quaternion invoke(final Quaternion arg) {
            return PrimitiveScalar.isSmall(PrimitiveMath.ONE, arg.norm()) ? Quaternion.ZERO : Quaternion.ONE;
        }
    
    };
    public static final QuaternionFunction.Unary CBRT = new QuaternionFunction.Unary() {
    
        public final Quaternion invoke(final Quaternion arg) {
            return ROOT.invoke(arg, 3);
        }
    
    };
    public static final QuaternionFunction.Unary CEIL = new QuaternionFunction.Unary() {
    
        @Override
        public final Quaternion invoke(final Quaternion arg) {
            final double tmpScalar = PrimitiveMath.CEIL.invoke(arg.scalar());
            final double tmpI = PrimitiveMath.CEIL.invoke(arg.i);
            final double tmpJ = PrimitiveMath.CEIL.invoke(arg.j);
            final double tmpK = PrimitiveMath.CEIL.invoke(arg.k);
            return Quaternion.of(tmpScalar, tmpI, tmpJ, tmpK);
        }
    
    };
    public static final QuaternionFunction.Unary CONJUGATE = new QuaternionFunction.Unary() {
    
        public final Quaternion invoke(final Quaternion arg) {
            return arg.conjugate();
        }
    
    };
    public static final QuaternionFunction.Unary COS = new QuaternionFunction.Unary() {
    
        public final Quaternion invoke(final Quaternion arg) {
            return COSH.invoke(arg.multiply(Quaternion.I));
        }
    
    };
    public static final QuaternionFunction.Unary COSH = new QuaternionFunction.Unary() {
    
        public final Quaternion invoke(final Quaternion arg) {
            return (EXP.invoke(arg).add(EXP.invoke(arg.negate()))).divide(PrimitiveMath.TWO);
        }
    
    };
    public static final QuaternionFunction.Binary DIVIDE = new QuaternionFunction.Binary() {
    
        @Override
        public final Quaternion invoke(final Quaternion arg1, final Quaternion arg2) {
            return arg1.divide(arg2);
        }
    
    };
    public static final QuaternionFunction.Unary EXP = new QuaternionFunction.Unary() {
    
        public final Quaternion invoke(final Quaternion arg) {
    
            if (arg.isReal()) {
    
                final double tmpScalar = PrimitiveMath.EXP.invoke(arg.scalar());
    
                return Quaternion.valueOf(tmpScalar);
    
            } else {
    
                final double tmpNorm = PrimitiveMath.EXP.invoke(arg.scalar());
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
    public static final QuaternionFunction.Unary EXPM1 = new QuaternionFunction.Unary() {
    
        public final Quaternion invoke(final Quaternion arg) {
            return EXP.invoke(arg).subtract(1.0);
        }
    
    };
    public static final QuaternionFunction.Unary FLOOR = new QuaternionFunction.Unary() {
    
        @Override
        public final Quaternion invoke(final Quaternion arg) {
            final double tmpScalar = PrimitiveMath.FLOOR.invoke(arg.scalar());
            final double tmpI = PrimitiveMath.FLOOR.invoke(arg.i);
            final double tmpJ = PrimitiveMath.FLOOR.invoke(arg.j);
            final double tmpK = PrimitiveMath.FLOOR.invoke(arg.k);
            return Quaternion.of(tmpScalar, tmpI, tmpJ, tmpK);
        }
    
    };
    public static final QuaternionFunction.Binary HYPOT = new QuaternionFunction.Binary() {
    
        @Override
        public final Quaternion invoke(final Quaternion arg1, final Quaternion arg2) {
            return Quaternion.valueOf(PrimitiveMath.HYPOT.invoke(arg1.norm(), arg2.norm()));
        }
    
    };
    public static final QuaternionFunction.Unary INVERT = new QuaternionFunction.Unary() {
    
        public final Quaternion invoke(final Quaternion arg) {
            return POWER.invoke(arg, -1);
        }
    
    };
    public static final QuaternionFunction.Unary LOG = new QuaternionFunction.Unary() {
    
        public final Quaternion invoke(final Quaternion arg) {
    
            final double tmpNorm = arg.norm();
            final double[] tmpUnitVector = arg.unit();
            final double tmpPhase = PrimitiveMath.ACOS.invoke(arg.scalar() / tmpNorm);
    
            final double tmpScalar = PrimitiveMath.LOG.invoke(tmpNorm);
            final double tmpI = tmpUnitVector[0] * tmpPhase;
            final double tmpJ = tmpUnitVector[1] * tmpPhase;
            final double tmpK = tmpUnitVector[2] * tmpPhase;
    
            return Quaternion.of(tmpScalar, tmpI, tmpJ, tmpK);
        }
    
    };
    public static final QuaternionFunction.Unary LOG10 = new QuaternionFunction.Unary() {
    
        public final Quaternion invoke(final Quaternion arg) {
            return LOG.invoke(arg).divide(PrimitiveMath.LOG.invoke(10.0));
        }
    
    };
    public static final QuaternionFunction.Unary LOG1P = new QuaternionFunction.Unary() {
    
        public final Quaternion invoke(final Quaternion arg) {
            return LOG.invoke(arg.add(1.0));
        }
    
    };
    public static final QuaternionFunction.Unary LOGISTIC = new QuaternionFunction.Unary() {
    
        public final Quaternion invoke(final Quaternion arg) {
            return Quaternion.ONE.divide(Quaternion.ONE.add(EXP.invoke(arg.negate())));
        }
    
    };
    public static final QuaternionFunction.Unary LOGIT = new QuaternionFunction.Unary() {
    
        public final Quaternion invoke(final Quaternion arg) {
            return LOG.invoke(Quaternion.ONE.divide(Quaternion.ONE.subtract(arg)));
        }
    
    };
    public static final QuaternionFunction.Binary MAX = new QuaternionFunction.Binary() {
    
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
    public static final QuaternionFunction.Binary MIN = new QuaternionFunction.Binary() {
    
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
    public static final QuaternionFunction.Binary MULTIPLY = new QuaternionFunction.Binary() {
    
        @Override
        public final Quaternion invoke(final Quaternion arg1, final Quaternion arg2) {
            return arg1.multiply(arg2);
        }
    
    };
    public static final QuaternionFunction.Unary NEGATE = new QuaternionFunction.Unary() {
    
        public final Quaternion invoke(final Quaternion arg) {
            return arg.negate();
        }
    
    };
    public static final QuaternionFunction.Binary POW = new QuaternionFunction.Binary() {
    
        @Override
        public final Quaternion invoke(final Quaternion arg1, final Quaternion arg2) {
            return EXP.invoke(LOG.invoke(arg1).multiply(arg2));
        }
    
    };
    public static final QuaternionFunction.Parameter POWER = new QuaternionFunction.Parameter() {
    
        @Override
        public final Quaternion invoke(final Quaternion arg, final int param) {
    
            final Quaternion tmpInvoke = LOG.invoke(arg);
            final Quaternion tmpMultiply = tmpInvoke.multiply(param);
            return EXP.invoke(tmpMultiply);
        }
    
    };
    public static final QuaternionFunction.Unary RINT = new QuaternionFunction.Unary() {
    
        @Override
        public final Quaternion invoke(final Quaternion arg) {
            final double tmpScalar = PrimitiveMath.RINT.invoke(arg.scalar());
            final double tmpI = PrimitiveMath.RINT.invoke(arg.i);
            final double tmpJ = PrimitiveMath.RINT.invoke(arg.j);
            final double tmpK = PrimitiveMath.RINT.invoke(arg.k);
            return Quaternion.of(tmpScalar, tmpI, tmpJ, tmpK);
        }
    
    };
    public static final QuaternionFunction.Parameter ROOT = new QuaternionFunction.Parameter() {
    
        @Override
        public final Quaternion invoke(final Quaternion arg, final int param) {
    
            if (param != 0) {
    
                return EXP.invoke(LOG.invoke(arg).divide(param));
    
            } else {
    
                throw new IllegalArgumentException();
            }
        }
    
    };
    public static final QuaternionFunction.Parameter SCALE = new QuaternionFunction.Parameter() {
    
        @Override
        public final Quaternion invoke(final Quaternion arg, final int param) {
            final double tmpScalar = PrimitiveMath.SCALE.invoke(arg.scalar(), param);
            final double tmpI = PrimitiveMath.SCALE.invoke(arg.i, param);
            final double tmpJ = PrimitiveMath.SCALE.invoke(arg.j, param);
            final double tmpK = PrimitiveMath.SCALE.invoke(arg.k, param);
            return Quaternion.of(tmpScalar, tmpI, tmpJ, tmpK);
        }
    
    };
    public static final QuaternionFunction.Unary SIGNUM = new QuaternionFunction.Unary() {
    
        public final Quaternion invoke(final Quaternion arg) {
            return arg.signum();
        }
    
    };
    public static final QuaternionFunction.Unary SIN = new QuaternionFunction.Unary() {
    
        public final Quaternion invoke(final Quaternion arg) {
            return SINH.invoke(arg.multiply(Quaternion.I)).multiply(Quaternion.I.negate());
        }
    
    };
    public static final QuaternionFunction.Unary SINH = new QuaternionFunction.Unary() {
    
        public final Quaternion invoke(final Quaternion arg) {
            return (EXP.invoke(arg).subtract(EXP.invoke(arg.negate()))).divide(PrimitiveMath.TWO);
        }
    
    };
    public static final QuaternionFunction.Unary SQRT = new QuaternionFunction.Unary() {
    
        public final Quaternion invoke(final Quaternion arg) {
            return ROOT.invoke(arg, 2);
        }
    
    };
    public static final QuaternionFunction.Unary SQRT1PX2 = new QuaternionFunction.Unary() {
    
        public final Quaternion invoke(final Quaternion arg) {
            return SQRT.invoke(Quaternion.ONE.add(arg.multiply(arg)));
        }
    
    };
    public static final QuaternionFunction.Binary SUBTRACT = new QuaternionFunction.Binary() {
    
        @Override
        public final Quaternion invoke(final Quaternion arg1, final Quaternion arg2) {
            return arg1.subtract(arg2);
        }
    
    };
    public static final QuaternionFunction.Unary TAN = new QuaternionFunction.Unary() {
    
        public final Quaternion invoke(final Quaternion arg) {
            return TANH.invoke(arg.multiply(Quaternion.I)).multiply(Quaternion.I.negate());
        }
    
    };
    public static final QuaternionFunction.Unary TANH = new QuaternionFunction.Unary() {
    
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
    public static final QuaternionFunction.Unary VALUE = new QuaternionFunction.Unary() {
    
        public final Quaternion invoke(final Quaternion arg) {
            return arg;
        }
    
    };

}

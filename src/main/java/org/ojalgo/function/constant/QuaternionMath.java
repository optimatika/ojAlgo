/*
 * Copyright 1997-2024 Optimatika
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

    /*
     * The lambdas below should not (cannot) reference each other. Delegate to some other 'type' or code in
     * org.ojalgo.function.special.MissingMath.
     */

    public static final QuaternionFunction.Unary ABS = arg -> Quaternion.valueOf(arg.norm());
    public static final QuaternionFunction.Unary ACOS = arg -> arg.getPureVersor().negate().multiply(QuaternionMath.ACOSH.invoke(arg));
    public static final QuaternionFunction.Unary ACOSH = arg -> QuaternionMath.LOG
            .invoke(arg.add(QuaternionMath.SQRT.invoke(arg.multiply(arg).subtract(PrimitiveMath.ONE))));
    public static final QuaternionFunction.Binary ADD = Quaternion::add;
    public static final QuaternionFunction.Unary ASIN = arg -> {

        Quaternion tmpNmbr = QuaternionMath.SQRT.invoke(Quaternion.ONE.subtract(QuaternionMath.POWER.invoke(arg, 2)));

        tmpNmbr = Quaternion.I.multiply(arg).add(tmpNmbr);
        final Quaternion aNumber = tmpNmbr;

        return QuaternionMath.LOG.invoke(aNumber).multiply(Quaternion.I).negate();
    };
    public static final QuaternionFunction.Unary ASINH = arg -> {

        final Quaternion tmpNmbr = arg.multiply(arg).add(PrimitiveMath.ONE);

        return QuaternionMath.LOG.invoke(arg.add(QuaternionMath.SQRT.invoke(tmpNmbr)));
    };
    public static final QuaternionFunction.Unary ATAN = arg -> {

        final Quaternion tmpNmbr = Quaternion.I.add(arg).divide(Quaternion.I.subtract(arg));

        return QuaternionMath.LOG.invoke(tmpNmbr).multiply(Quaternion.I).divide(PrimitiveMath.TWO);
    };
    public static final QuaternionFunction.Binary ATAN2 = (arg1, arg2) -> ATAN.invoke(arg1.divide(arg2));
    public static final QuaternionFunction.Unary ATANH = arg -> {

        final Quaternion tmpNmbr = arg.add(PrimitiveMath.ONE).divide(Quaternion.ONE.subtract(arg));

        return QuaternionMath.LOG.invoke(tmpNmbr).divide(PrimitiveMath.TWO);
    };
    public static final QuaternionFunction.Unary CARDINALITY = arg -> PrimitiveScalar.isSmall(PrimitiveMath.ONE, arg.norm()) ? Quaternion.ZERO : Quaternion.ONE;
    public static final QuaternionFunction.Unary CBRT = arg -> QuaternionMath.ROOT.invoke(arg, 3);
    public static final QuaternionFunction.Unary CEIL = arg -> {
        final double tmpScalar = PrimitiveMath.CEIL.invoke(arg.scalar());
        final double tmpI = PrimitiveMath.CEIL.invoke(arg.i);
        final double tmpJ = PrimitiveMath.CEIL.invoke(arg.j);
        final double tmpK = PrimitiveMath.CEIL.invoke(arg.k);
        return Quaternion.of(tmpScalar, tmpI, tmpJ, tmpK);
    };
    public static final QuaternionFunction.Unary CONJUGATE = Quaternion::conjugate;
    public static final QuaternionFunction.Unary COS = arg -> QuaternionMath.COSH.invoke(arg.multiply(Quaternion.I));
    public static final QuaternionFunction.Unary COSH = arg -> QuaternionMath.EXP.invoke(arg).add(QuaternionMath.EXP.invoke(arg.negate()))
            .divide(PrimitiveMath.TWO);
    public static final QuaternionFunction.Binary DIVIDE = Quaternion::divide;
    public static final QuaternionFunction.Unary EXP = arg -> {

        if (arg.isReal()) {

            final double tmpScalar = PrimitiveMath.EXP.invoke(arg.scalar());

            return Quaternion.valueOf(tmpScalar);

        }
        final double tmpNorm = PrimitiveMath.EXP.invoke(arg.scalar());
        final double[] tmpUnit = arg.unit();
        final double tmpPhase = arg.getVectorLength();

        return Quaternion.makePolar(tmpNorm, tmpUnit, tmpPhase);

        // final double tmpNorm = PrimitiveFunction.EXP.invoke(arg.doubleValue());
        // final double tmpPhase = arg.i;
        //
        // return ComplexNumber.makePolar(tmpNorm, tmpPhase);
    };
    public static final QuaternionFunction.Unary EXPM1 = arg -> EXP.invoke(arg).subtract(1.0);
    public static final QuaternionFunction.Unary FLOOR = arg -> {
        final double tmpScalar = PrimitiveMath.FLOOR.invoke(arg.scalar());
        final double tmpI = PrimitiveMath.FLOOR.invoke(arg.i);
        final double tmpJ = PrimitiveMath.FLOOR.invoke(arg.j);
        final double tmpK = PrimitiveMath.FLOOR.invoke(arg.k);
        return Quaternion.of(tmpScalar, tmpI, tmpJ, tmpK);
    };
    public static final QuaternionFunction.Binary HYPOT = (arg1, arg2) -> Quaternion.valueOf(PrimitiveMath.HYPOT.invoke(arg1.norm(), arg2.norm()));
    public static final QuaternionFunction.Unary INVERT = arg -> QuaternionMath.POWER.invoke(arg, -1);
    public static final QuaternionFunction.Unary LOG = arg -> {

        final double tmpNorm = arg.norm();
        final double[] tmpUnitVector = arg.unit();
        final double tmpPhase = PrimitiveMath.ACOS.invoke(arg.scalar() / tmpNorm);

        final double tmpScalar = PrimitiveMath.LOG.invoke(tmpNorm);
        final double tmpI = tmpUnitVector[0] * tmpPhase;
        final double tmpJ = tmpUnitVector[1] * tmpPhase;
        final double tmpK = tmpUnitVector[2] * tmpPhase;

        return Quaternion.of(tmpScalar, tmpI, tmpJ, tmpK);
    };
    public static final QuaternionFunction.Unary LOG10 = arg -> LOG.invoke(arg).divide(PrimitiveMath.LOG.invoke(10.0));
    public static final QuaternionFunction.Unary LOG1P = arg -> LOG.invoke(arg.add(1.0));
    public static final QuaternionFunction.Unary LOGISTIC = arg -> Quaternion.ONE.divide(Quaternion.ONE.add(EXP.invoke(arg.negate())));
    public static final QuaternionFunction.Unary LOGIT = arg -> LOG.invoke(Quaternion.ONE.divide(Quaternion.ONE.subtract(arg)));
    public static final QuaternionFunction.Binary MAX = (arg1, arg2) -> arg1.compareTo(arg2) > 0 ? arg1 : arg2;
    public static final QuaternionFunction.Binary MIN = (arg1, arg2) -> arg1.compareTo(arg2) < 0 ? arg1 : arg2;
    public static final QuaternionFunction.Binary MULTIPLY = Quaternion::multiply;
    public static final QuaternionFunction.Unary NEGATE = Quaternion::negate;
    public static final QuaternionFunction.Binary POW = (arg1, arg2) -> EXP.invoke(LOG.invoke(arg1).multiply(arg2));
    public static final QuaternionFunction.Parameter POWER = (arg, param) -> {

        final Quaternion tmpInvoke = LOG.invoke(arg);
        final Quaternion tmpMultiply = tmpInvoke.multiply(param);
        return EXP.invoke(tmpMultiply);
    };
    public static final QuaternionFunction.Unary RINT = arg -> {
        final double tmpScalar = PrimitiveMath.RINT.invoke(arg.scalar());
        final double tmpI = PrimitiveMath.RINT.invoke(arg.i);
        final double tmpJ = PrimitiveMath.RINT.invoke(arg.j);
        final double tmpK = PrimitiveMath.RINT.invoke(arg.k);
        return Quaternion.of(tmpScalar, tmpI, tmpJ, tmpK);
    };
    public static final QuaternionFunction.Parameter ROOT = (arg, param) -> {

        if (param != 0) {

            return EXP.invoke(LOG.invoke(arg).divide(param));

        }
        throw new IllegalArgumentException();
    };
    public static final QuaternionFunction.Parameter SCALE = (arg, param) -> {
        final double tmpScalar = PrimitiveMath.SCALE.invoke(arg.scalar(), param);
        final double tmpI = PrimitiveMath.SCALE.invoke(arg.i, param);
        final double tmpJ = PrimitiveMath.SCALE.invoke(arg.j, param);
        final double tmpK = PrimitiveMath.SCALE.invoke(arg.k, param);
        return Quaternion.of(tmpScalar, tmpI, tmpJ, tmpK);
    };
    public static final QuaternionFunction.Unary SIGNUM = Quaternion::signum;
    public static final QuaternionFunction.Unary SIN = arg -> QuaternionMath.SINH.invoke(arg.multiply(Quaternion.I)).multiply(Quaternion.I.negate());
    public static final QuaternionFunction.Unary SINH = arg -> EXP.invoke(arg).subtract(EXP.invoke(arg.negate())).divide(PrimitiveMath.TWO);
    public static final QuaternionFunction.Unary SQRT = arg -> ROOT.invoke(arg, 2);
    public static final QuaternionFunction.Unary SQRT1PX2 = arg -> SQRT.invoke(Quaternion.ONE.add(arg.multiply(arg)));
    public static final QuaternionFunction.Binary SUBTRACT = Quaternion::subtract;
    public static final QuaternionFunction.Unary TAN = arg -> QuaternionMath.TANH.invoke(arg.multiply(Quaternion.I)).multiply(Quaternion.I.negate());
    public static final QuaternionFunction.Unary TANH = arg -> {

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
    };
    public static final QuaternionFunction.Unary VALUE = arg -> arg;

}

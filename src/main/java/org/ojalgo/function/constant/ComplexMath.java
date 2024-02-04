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

import static org.ojalgo.function.constant.PrimitiveMath.*;

import org.ojalgo.function.ComplexFunction;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.PrimitiveScalar;

public abstract class ComplexMath {

    /*
     * The lambdas below should not (cannot) reference each other. Delegate to some other 'type' or code in
     * org.ojalgo.function.special.MissingMath.
     */

    public static final ComplexFunction.Unary ABS = arg -> ComplexNumber.valueOf(arg.norm());
    public static final ComplexFunction.Unary ACOS = arg -> {

        final ComplexNumber tmpSqrt = ComplexMath.SQRT.invoke(ComplexNumber.ONE.subtract(arg.multiply(arg)));

        final ComplexNumber tmpNmbr = arg.add(ComplexNumber.I.multiply(tmpSqrt));

        final ComplexNumber tmpLog = ComplexMath.LOG.invoke(tmpNmbr);

        return tmpLog.multiply(ComplexNumber.I).negate();
    };
    public static final ComplexFunction.Unary ACOSH = arg -> ComplexMath.LOG.invoke(arg.add(ComplexMath.SQRT.invoke(arg.multiply(arg).subtract(ONE))));
    public static final ComplexFunction.Binary ADD = ComplexNumber::add;
    public static final ComplexFunction.Unary ASIN = arg -> {

        ComplexNumber tmpNmbr = ComplexMath.SQRT.invoke(ComplexNumber.ONE.subtract(ComplexMath.POWER.invoke(arg, 2)));

        tmpNmbr = ComplexNumber.I.multiply(arg).add(tmpNmbr);
        final ComplexNumber aNumber = tmpNmbr;

        return ComplexMath.LOG.invoke(aNumber).multiply(ComplexNumber.I).negate();
    };
    public static final ComplexFunction.Unary ASINH = arg -> {

        final ComplexNumber tmpNmbr = arg.multiply(arg).add(ONE);

        return ComplexMath.LOG.invoke(arg.add(ComplexMath.SQRT.invoke(tmpNmbr)));
    };
    public static final ComplexFunction.Unary ATAN = arg -> {

        final ComplexNumber tmpNmbr = ComplexNumber.I.add(arg).divide(ComplexNumber.I.subtract(arg));

        return ComplexMath.LOG.invoke(tmpNmbr).multiply(ComplexNumber.I).divide(TWO);
    };
    public static final ComplexFunction.Binary ATAN2 = (arg1, arg2) -> ATAN.invoke(arg1.divide(arg2));
    public static final ComplexFunction.Unary ATANH = arg -> {

        final ComplexNumber tmpNmbr = arg.add(ONE).divide(ComplexNumber.ONE.subtract(arg));

        return ComplexMath.LOG.invoke(tmpNmbr).divide(TWO);
    };
    public static final ComplexFunction.Unary CARDINALITY = arg -> PrimitiveScalar.isSmall(ONE, arg.norm()) ? ComplexNumber.ZERO : ComplexNumber.ONE;
    public static final ComplexFunction.Unary CBRT = arg -> {

        final double retMod = PrimitiveMath.CBRT.invoke(arg.norm());
        final double retArg = arg.phase() * THIRD;

        return ComplexNumber.makePolar(retMod, retArg);
    };
    public static final ComplexFunction.Unary CEIL = arg -> {
        final double tmpRe = PrimitiveMath.CEIL.invoke(arg.doubleValue());
        final double tmpIm = PrimitiveMath.CEIL.invoke(arg.i);
        return ComplexNumber.of(tmpRe, tmpIm);
    };
    public static final ComplexFunction.Unary CONJUGATE = ComplexNumber::conjugate;
    public static final ComplexFunction.Unary COS = arg -> ComplexMath.COSH.invoke(arg.multiply(ComplexNumber.I));
    public static final ComplexFunction.Unary COSH = arg -> ComplexMath.EXP.invoke(arg).add(ComplexMath.EXP.invoke(arg.negate())).divide(TWO);
    public static final ComplexFunction.Binary DIVIDE = ComplexNumber::divide;
    public static final ComplexFunction.Unary EXP = arg -> {

        final double tmpNorm = PrimitiveMath.EXP.invoke(arg.doubleValue());
        final double tmpPhase = arg.i;

        return ComplexNumber.makePolar(tmpNorm, tmpPhase);
    };
    public static final ComplexFunction.Unary EXPM1 = arg -> {

        final double retMod = PrimitiveMath.EXPM1.invoke(arg.doubleValue());
        final double retArg = arg.i;

        return ComplexNumber.makePolar(retMod, retArg);
    };
    public static final ComplexFunction.Unary FLOOR = arg -> {
        final double tmpRe = PrimitiveMath.FLOOR.invoke(arg.doubleValue());
        final double tmpIm = PrimitiveMath.FLOOR.invoke(arg.i);
        return ComplexNumber.of(tmpRe, tmpIm);
    };
    public static final ComplexFunction.Binary HYPOT = (arg1, arg2) -> ComplexNumber.valueOf(PrimitiveMath.HYPOT.invoke(arg1.norm(), arg2.norm()));
    public static final ComplexFunction.Unary INVERT = arg -> ComplexMath.POWER.invoke(arg, -1);
    public static final ComplexFunction.Unary LOG = arg -> {

        final double tmpRe = PrimitiveMath.LOG.invoke(arg.norm());
        final double tmpIm = arg.phase();

        return ComplexNumber.of(tmpRe, tmpIm);
    };
    public static final ComplexFunction.Unary LOG10 = arg -> {

        final double retRe = PrimitiveMath.LOG10.invoke(arg.norm());
        final double retIm = arg.phase();

        return ComplexNumber.of(retRe, retIm);
    };
    public static final ComplexFunction.Unary LOG1P = arg -> {

        final double retRe = PrimitiveMath.LOG1P.invoke(arg.norm());
        final double retIm = arg.phase();

        return ComplexNumber.of(retRe, retIm);
    };
    public static final ComplexFunction.Unary LOGISTIC = arg -> ComplexNumber.ONE.divide(ComplexNumber.ONE.add(EXP.invoke(arg.negate())));
    public static final ComplexFunction.Unary LOGIT = arg -> LOG.invoke(ComplexNumber.ONE.divide(ComplexNumber.ONE.subtract(arg)));
    public static final ComplexFunction.Binary MAX = (arg1, arg2) -> arg1.compareTo(arg2) > 0 ? arg1 : arg2;
    public static final ComplexFunction.Binary MIN = (arg1, arg2) -> arg1.compareTo(arg2) < 0 ? arg1 : arg2;
    public static final ComplexFunction.Binary MULTIPLY = ComplexNumber::multiply;
    public static final ComplexFunction.Unary NEGATE = ComplexNumber::negate;
    public static final ComplexFunction.Binary POW = (arg1, arg2) -> EXP.invoke(LOG.invoke(arg1).multiply(arg2));
    public static final ComplexFunction.Parameter POWER = (arg, param) -> {

        final double retMod = PrimitiveMath.POWER.invoke(arg.norm(), param);
        final double retArg = arg.phase() * param;

        return ComplexNumber.makePolar(retMod, retArg);
    };
    public static final ComplexFunction.Unary RINT = arg -> {
        final double tmpRe = PrimitiveMath.RINT.invoke(arg.doubleValue());
        final double tmpIm = PrimitiveMath.RINT.invoke(arg.i);
        return ComplexNumber.of(tmpRe, tmpIm);
    };
    public static final ComplexFunction.Parameter ROOT = (arg, param) -> {

        if (param != 0) {

            final double tmpExp = ONE / param;

            final double retMod = PrimitiveMath.POW.invoke(arg.norm(), tmpExp);
            final double retArg = arg.phase() * tmpExp;

            return ComplexNumber.makePolar(retMod, retArg);

        }
        throw new IllegalArgumentException();
    };
    public static final ComplexFunction.Parameter SCALE = (arg, param) -> {
        final double tmpRe = PrimitiveMath.SCALE.invoke(arg.doubleValue(), param);
        final double tmpIm = PrimitiveMath.SCALE.invoke(arg.i, param);
        return ComplexNumber.of(tmpRe, tmpIm);
    };
    public static final ComplexFunction.Unary SIGNUM = ComplexNumber::signum;
    public static final ComplexFunction.Unary SIN = arg -> ComplexMath.SINH.invoke(arg.multiply(ComplexNumber.I)).multiply(ComplexNumber.I.negate());
    public static final ComplexFunction.Unary SINH = arg -> EXP.invoke(arg).subtract(EXP.invoke(arg.negate())).divide(TWO);
    public static final ComplexFunction.Unary SQRT = arg -> {

        final double retMod = PrimitiveMath.SQRT.invoke(arg.norm());
        final double retArg = arg.phase() * HALF;

        return ComplexNumber.makePolar(retMod, retArg);
    };
    public static final ComplexFunction.Unary SQRT1PX2 = arg -> SQRT.invoke(ComplexNumber.ONE.add(arg.multiply(arg)));
    public static final ComplexFunction.Binary SUBTRACT = ComplexNumber::subtract;
    public static final ComplexFunction.Unary TAN = arg -> ComplexMath.TANH.invoke(arg.multiply(ComplexNumber.I)).multiply(ComplexNumber.I.negate());
    public static final ComplexFunction.Unary TANH = arg -> {

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
    };
    public static final ComplexFunction.Unary VALUE = arg -> arg;

}

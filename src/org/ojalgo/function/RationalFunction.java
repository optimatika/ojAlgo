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

import org.ojalgo.function.aggregator.AggregatorSet;
import org.ojalgo.function.aggregator.RationalAggregator;
import org.ojalgo.function.constant.RationalMath;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.type.context.NumberContext;

/**
 * RationalFunction
 *
 * @author apete
 */
public final class RationalFunction extends FunctionSet<RationalNumber> {

    @FunctionalInterface
    public interface Binary extends BinaryFunction<RationalNumber> {

        default double invoke(final double arg1, final double arg2) {
            return this.invoke(RationalNumber.valueOf(arg1), RationalNumber.valueOf(arg2)).doubleValue();
        }

        default float invoke(final float arg1, final float arg2) {
            return this.invoke(RationalNumber.valueOf(arg1), RationalNumber.valueOf(arg2)).floatValue();
        }

    }

    @FunctionalInterface
    public interface Consumer extends VoidFunction<RationalNumber> {

        default void invoke(final double arg) {
            this.invoke(RationalNumber.valueOf(arg));
        }

        default void invoke(final float arg) {
            this.invoke(RationalNumber.valueOf(arg));
        }

    }

    @FunctionalInterface
    public interface Parameter extends ParameterFunction<RationalNumber> {

        default double invoke(final double arg, final int param) {
            return this.invoke(RationalNumber.valueOf(arg), param).doubleValue();
        }

        default float invoke(final float arg, final int param) {
            return this.invoke(RationalNumber.valueOf(arg), param).floatValue();
        }

    }

    @FunctionalInterface
    public interface Predicate extends PredicateFunction<RationalNumber> {

        default boolean invoke(final double arg) {
            return this.invoke(RationalNumber.valueOf(arg));
        }

        default boolean invoke(final float arg) {
            return this.invoke(RationalNumber.valueOf(arg));
        }

    }

    @FunctionalInterface
    public interface Unary extends UnaryFunction<RationalNumber> {

        default double invoke(final double arg) {
            return this.invoke(RationalNumber.valueOf(arg)).doubleValue();
        }

        default float invoke(final float arg) {
            return this.invoke(RationalNumber.valueOf(arg)).floatValue();
        }

    }

    /**
     * @deprecated v48 Use {@link RationalMath#ABS} instead
     */
    @Deprecated
    public static final Unary ABS = RationalMath.ABS;

    /**
     * @deprecated v48 Use {@link RationalMath#ACOS} instead
     */
    @Deprecated
    public static final Unary ACOS = RationalMath.ACOS;

    /**
     * @deprecated v48 Use {@link RationalMath#ACOSH} instead
     */
    @Deprecated
    public static final Unary ACOSH = RationalMath.ACOSH;

    /**
     * @deprecated v48 Use {@link RationalMath#ADD} instead
     */
    @Deprecated
    public static final Binary ADD = RationalMath.ADD;

    /**
     * @deprecated v48 Use {@link RationalMath#ASIN} instead
     */
    @Deprecated
    public static final Unary ASIN = RationalMath.ASIN;

    /**
     * @deprecated v48 Use {@link RationalMath#ASINH} instead
     */
    @Deprecated
    public static final Unary ASINH = RationalMath.ASINH;

    /**
     * @deprecated v48 Use {@link RationalMath#ATAN} instead
     */
    @Deprecated
    public static final Unary ATAN = RationalMath.ATAN;

    /**
     * @deprecated v48 Use {@link RationalMath#ATAN2} instead
     */
    @Deprecated
    public static final Binary ATAN2 = RationalMath.ATAN2;

    /**
     * @deprecated v48 Use {@link RationalMath#ATANH} instead
     */
    @Deprecated
    public static final Unary ATANH = RationalMath.ATANH;

    /**
     * @deprecated v48 Use {@link RationalMath#CARDINALITY} instead
     */
    @Deprecated
    public static final Unary CARDINALITY = RationalMath.CARDINALITY;

    /**
     * @deprecated v48 Use {@link RationalMath#CBRT} instead
     */
    @Deprecated
    public static final Unary CBRT = RationalMath.CBRT;

    /**
     * @deprecated v48 Use {@link RationalMath#CEIL} instead
     */
    @Deprecated
    public static final Unary CEIL = RationalMath.CEIL;

    /**
     * @deprecated v48 Use {@link RationalMath#CONJUGATE} instead
     */
    @Deprecated
    public static final Unary CONJUGATE = RationalMath.CONJUGATE;

    /**
     * @deprecated v48 Use {@link RationalMath#COS} instead
     */
    @Deprecated
    public static final Unary COS = RationalMath.COS;

    /**
     * @deprecated v48 Use {@link RationalMath#COSH} instead
     */
    @Deprecated
    public static final Unary COSH = RationalMath.COSH;

    /**
     * @deprecated v48 Use {@link RationalMath#DIVIDE} instead
     */
    @Deprecated
    public static final Binary DIVIDE = RationalMath.DIVIDE;

    /**
     * @deprecated v48 Use {@link RationalMath#EXP} instead
     */
    @Deprecated
    public static final Unary EXP = RationalMath.EXP;

    /**
     * @deprecated v48 Use {@link RationalMath#EXPM1} instead
     */
    @Deprecated
    public static final Unary EXPM1 = RationalMath.EXPM1;

    /**
     * @deprecated v48 Use {@link RationalMath#FLOOR} instead
     */
    @Deprecated
    public static final Unary FLOOR = RationalMath.FLOOR;

    /**
     * @deprecated v48 Use {@link RationalMath#HYPOT} instead
     */
    @Deprecated
    public static final Binary HYPOT = RationalMath.HYPOT;

    /**
     * @deprecated v48 Use {@link RationalMath#INVERT} instead
     */
    @Deprecated
    public static final Unary INVERT = RationalMath.INVERT;

    /**
     * @deprecated v48 Use {@link RationalMath#LOG} instead
     */
    @Deprecated
    public static final Unary LOG = RationalMath.LOG;

    /**
     * @deprecated v48 Use {@link RationalMath#LOG10} instead
     */
    @Deprecated
    public static final Unary LOG10 = RationalMath.LOG10;

    /**
     * @deprecated v48 Use {@link RationalMath#LOG1P} instead
     */
    @Deprecated
    public static final Unary LOG1P = RationalMath.LOG1P;

    /**
     * @deprecated v48 Use {@link RationalMath#LOGISTIC} instead
     */
    @Deprecated
    public static final Unary LOGISTIC = RationalMath.LOGISTIC;

    /**
     * @deprecated v48 Use {@link RationalMath#LOGIT} instead
     */
    @Deprecated
    public static final Unary LOGIT = RationalMath.LOGIT;

    /**
     * @deprecated v48 Use {@link RationalMath#MAX} instead
     */
    @Deprecated
    public static final Binary MAX = RationalMath.MAX;

    /**
     * @deprecated v48 Use {@link RationalMath#MIN} instead
     */
    @Deprecated
    public static final Binary MIN = RationalMath.MIN;

    /**
     * @deprecated v48 Use {@link RationalMath#MULTIPLY} instead
     */
    @Deprecated
    public static final Binary MULTIPLY = RationalMath.MULTIPLY;

    /**
     * @deprecated v48 Use {@link RationalMath#NEGATE} instead
     */
    @Deprecated
    public static final Unary NEGATE = RationalMath.NEGATE;

    /**
     * @deprecated v48 Use {@link RationalMath#POW} instead
     */
    @Deprecated
    public static final Binary POW = RationalMath.POW;

    /**
     * @deprecated v48 Use {@link RationalMath#POWER} instead
     */
    @Deprecated
    public static final Parameter POWER = RationalMath.POWER;

    /**
     * @deprecated v48 Use {@link RationalMath#RINT} instead
     */
    @Deprecated
    public static final Unary RINT = RationalMath.RINT;

    /**
     * @deprecated v48 Use {@link RationalMath#ROOT} instead
     */
    @Deprecated
    public static final Parameter ROOT = RationalMath.ROOT;

    /**
     * @deprecated v48 Use {@link RationalMath#SCALE} instead
     */
    @Deprecated
    public static final Parameter SCALE = RationalMath.SCALE;

    /**
     * @deprecated v48 Use {@link RationalMath#SIGNUM} instead
     */
    @Deprecated
    public static final Unary SIGNUM = RationalMath.SIGNUM;

    /**
     * @deprecated v48 Use {@link RationalMath#SIN} instead
     */
    @Deprecated
    public static final Unary SIN = RationalMath.SIN;

    /**
     * @deprecated v48 Use {@link RationalMath#SINH} instead
     */
    @Deprecated
    public static final Unary SINH = RationalMath.SINH;

    /**
     * @deprecated v48 Use {@link RationalMath#SQRT} instead
     */
    @Deprecated
    public static final Unary SQRT = RationalMath.SQRT;

    /**
     * @deprecated v48 Use {@link RationalMath#SQRT1PX2} instead
     */
    @Deprecated
    public static final Unary SQRT1PX2 = RationalMath.SQRT1PX2;

    /**
     * @deprecated v48 Use {@link RationalMath#SUBTRACT} instead
     */
    @Deprecated
    public static final Binary SUBTRACT = RationalMath.SUBTRACT;

    /**
     * @deprecated v48 Use {@link RationalMath#TAN} instead
     */
    @Deprecated
    public static final Unary TAN = RationalMath.TAN;

    /**
     * @deprecated v48 Use {@link RationalMath#TANH} instead
     */
    @Deprecated
    public static final Unary TANH = RationalMath.TANH;

    /**
     * @deprecated v48 Use {@link RationalMath#VALUE} instead
     */
    @Deprecated
    public static final Unary VALUE = RationalMath.VALUE;

    private static final RationalFunction SET = new RationalFunction();

    public static RationalFunction getSet() {
        return SET;
    }

    private RationalFunction() {
        super();
    }

    @Override
    public UnaryFunction<RationalNumber> abs() {
        return RationalMath.ABS;
    }

    @Override
    public UnaryFunction<RationalNumber> acos() {
        return RationalMath.ACOS;
    }

    @Override
    public UnaryFunction<RationalNumber> acosh() {
        return RationalMath.ACOSH;
    }

    @Override
    public BinaryFunction<RationalNumber> add() {
        return RationalMath.ADD;
    }

    @Override
    public AggregatorSet<RationalNumber> aggregator() {
        return RationalAggregator.getSet();
    }

    @Override
    public UnaryFunction<RationalNumber> asin() {
        return RationalMath.ASIN;
    }

    @Override
    public UnaryFunction<RationalNumber> asinh() {
        return RationalMath.ASINH;
    }

    @Override
    public UnaryFunction<RationalNumber> atan() {
        return RationalMath.ATAN;
    }

    @Override
    public BinaryFunction<RationalNumber> atan2() {
        return RationalMath.ATAN2;
    }

    @Override
    public UnaryFunction<RationalNumber> atanh() {
        return RationalMath.ATANH;
    }

    @Override
    public UnaryFunction<RationalNumber> cardinality() {
        return RationalMath.CARDINALITY;
    }

    @Override
    public UnaryFunction<RationalNumber> cbrt() {
        return RationalMath.CBRT;
    }

    @Override
    public UnaryFunction<RationalNumber> ceil() {
        return RationalMath.CEIL;
    }

    @Override
    public UnaryFunction<RationalNumber> conjugate() {
        return RationalMath.CONJUGATE;
    }

    @Override
    public UnaryFunction<RationalNumber> cos() {
        return RationalMath.COS;
    }

    @Override
    public UnaryFunction<RationalNumber> cosh() {
        return RationalMath.COSH;
    }

    @Override
    public BinaryFunction<RationalNumber> divide() {
        return RationalMath.DIVIDE;
    }

    @Override
    public Unary enforce(final NumberContext context) {
        return t -> RationalNumber.valueOf(context.enforce(t));
    }

    @Override
    public UnaryFunction<RationalNumber> exp() {
        return RationalMath.EXP;
    }

    @Override
    public UnaryFunction<RationalNumber> expm1() {
        return RationalMath.EXPM1;
    }

    @Override
    public UnaryFunction<RationalNumber> floor() {
        return RationalMath.FLOOR;
    }

    @Override
    public BinaryFunction<RationalNumber> hypot() {
        return RationalMath.HYPOT;
    }

    @Override
    public UnaryFunction<RationalNumber> invert() {
        return RationalMath.INVERT;
    }

    @Override
    public UnaryFunction<RationalNumber> log() {
        return RationalMath.LOG;
    }

    @Override
    public UnaryFunction<RationalNumber> log10() {
        return RationalMath.LOG10;
    }

    @Override
    public UnaryFunction<RationalNumber> log1p() {
        return RationalMath.LOG1P;
    }

    @Override
    public UnaryFunction<RationalNumber> logistic() {
        return RationalMath.LOGISTIC;
    }

    @Override
    public UnaryFunction<RationalNumber> logit() {
        return RationalMath.LOGIT;
    }

    @Override
    public BinaryFunction<RationalNumber> max() {
        return RationalMath.MAX;
    }

    @Override
    public BinaryFunction<RationalNumber> min() {
        return RationalMath.MIN;
    }

    @Override
    public BinaryFunction<RationalNumber> multiply() {
        return RationalMath.MULTIPLY;
    }

    @Override
    public UnaryFunction<RationalNumber> negate() {
        return RationalMath.NEGATE;
    }

    @Override
    public BinaryFunction<RationalNumber> pow() {
        return RationalMath.POW;
    }

    @Override
    public ParameterFunction<RationalNumber> power() {
        return RationalMath.POWER;
    }

    @Override
    public UnaryFunction<RationalNumber> rint() {
        return RationalMath.RINT;
    }

    @Override
    public ParameterFunction<RationalNumber> root() {
        return RationalMath.ROOT;
    }

    @Override
    public ParameterFunction<RationalNumber> scale() {
        return RationalMath.SCALE;
    }

    @Override
    public UnaryFunction<RationalNumber> signum() {
        return RationalMath.SIGNUM;
    }

    @Override
    public UnaryFunction<RationalNumber> sin() {
        return RationalMath.SIN;
    }

    @Override
    public UnaryFunction<RationalNumber> sinh() {
        return RationalMath.SINH;
    }

    @Override
    public UnaryFunction<RationalNumber> sqrt() {
        return RationalMath.SQRT;
    }

    @Override
    public UnaryFunction<RationalNumber> sqrt1px2() {
        return RationalMath.SQRT1PX2;
    }

    @Override
    public BinaryFunction<RationalNumber> subtract() {
        return RationalMath.SUBTRACT;
    }

    @Override
    public UnaryFunction<RationalNumber> tan() {
        return RationalMath.TAN;
    }

    @Override
    public UnaryFunction<RationalNumber> tanh() {
        return RationalMath.TANH;
    }

    @Override
    public UnaryFunction<RationalNumber> value() {
        return RationalMath.VALUE;
    }

}

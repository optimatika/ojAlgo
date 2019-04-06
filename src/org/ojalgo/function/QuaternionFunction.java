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
import org.ojalgo.function.aggregator.QuaternionAggregator;
import org.ojalgo.function.constant.QuaternionMath;
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

    /**
     * @deprecated Use {@link QuaternionMath#ABS} instead
     */
    public static final Unary ABS = QuaternionMath.ABS;

    /**
     * @deprecated Use {@link QuaternionMath#ACOS} instead
     */
    public static final Unary ACOS = QuaternionMath.ACOS;

    /**
     * @deprecated Use {@link QuaternionMath#ACOSH} instead
     */
    public static final Unary ACOSH = QuaternionMath.ACOSH;

    /**
     * @deprecated Use {@link QuaternionMath#ADD} instead
     */
    public static final Binary ADD = QuaternionMath.ADD;

    /**
     * @deprecated Use {@link QuaternionMath#ASIN} instead
     */
    public static final Unary ASIN = QuaternionMath.ASIN;

    /**
     * @deprecated Use {@link QuaternionMath#ASINH} instead
     */
    public static final Unary ASINH = QuaternionMath.ASINH;

    /**
     * @deprecated Use {@link QuaternionMath#ATAN} instead
     */
    public static final Unary ATAN = QuaternionMath.ATAN;

    /**
     * @deprecated Use {@link QuaternionMath#ATAN2} instead
     */
    public static final Binary ATAN2 = QuaternionMath.ATAN2;

    /**
     * @deprecated Use {@link QuaternionMath#ATANH} instead
     */
    public static final Unary ATANH = QuaternionMath.ATANH;

    /**
     * @deprecated Use {@link QuaternionMath#CARDINALITY} instead
     */
    public static final Unary CARDINALITY = QuaternionMath.CARDINALITY;

    /**
     * @deprecated Use {@link QuaternionMath#CBRT} instead
     */
    public static final Unary CBRT = QuaternionMath.CBRT;

    /**
     * @deprecated Use {@link QuaternionMath#CEIL} instead
     */
    public static final Unary CEIL = QuaternionMath.CEIL;

    /**
     * @deprecated Use {@link QuaternionMath#CONJUGATE} instead
     */
    public static final Unary CONJUGATE = QuaternionMath.CONJUGATE;

    /**
     * @deprecated Use {@link QuaternionMath#COS} instead
     */
    public static final Unary COS = QuaternionMath.COS;

    /**
     * @deprecated Use {@link QuaternionMath#COSH} instead
     */
    public static final Unary COSH = QuaternionMath.COSH;

    /**
     * @deprecated Use {@link QuaternionMath#DIVIDE} instead
     */
    public static final Binary DIVIDE = QuaternionMath.DIVIDE;

    /**
     * @deprecated Use {@link QuaternionMath#EXP} instead
     */
    public static final Unary EXP = QuaternionMath.EXP;

    /**
     * @deprecated Use {@link QuaternionMath#EXPM1} instead
     */
    public static final Unary EXPM1 = QuaternionMath.EXPM1;

    /**
     * @deprecated Use {@link QuaternionMath#FLOOR} instead
     */
    public static final Unary FLOOR = QuaternionMath.FLOOR;

    /**
     * @deprecated Use {@link QuaternionMath#HYPOT} instead
     */
    public static final Binary HYPOT = QuaternionMath.HYPOT;

    /**
     * @deprecated Use {@link QuaternionMath#INVERT} instead
     */
    public static final Unary INVERT = QuaternionMath.INVERT;

    /**
     * @deprecated Use {@link QuaternionMath#LOG} instead
     */
    public static final Unary LOG = QuaternionMath.LOG;

    /**
     * @deprecated Use {@link QuaternionMath#LOG10} instead
     */
    public static final Unary LOG10 = QuaternionMath.LOG10;

    /**
     * @deprecated Use {@link QuaternionMath#LOG1P} instead
     */
    public static final Unary LOG1P = QuaternionMath.LOG1P;

    /**
     * @deprecated Use {@link QuaternionMath#LOGISTIC} instead
     */
    public static final Unary LOGISTIC = QuaternionMath.LOGISTIC;

    /**
     * @deprecated Use {@link QuaternionMath#LOGIT} instead
     */
    public static final Unary LOGIT = QuaternionMath.LOGIT;

    /**
     * @deprecated Use {@link QuaternionMath#MAX} instead
     */
    public static final Binary MAX = QuaternionMath.MAX;

    /**
     * @deprecated Use {@link QuaternionMath#MIN} instead
     */
    public static final Binary MIN = QuaternionMath.MIN;

    /**
     * @deprecated Use {@link QuaternionMath#MULTIPLY} instead
     */
    public static final Binary MULTIPLY = QuaternionMath.MULTIPLY;

    /**
     * @deprecated Use {@link QuaternionMath#NEGATE} instead
     */
    public static final Unary NEGATE = QuaternionMath.NEGATE;

    /**
     * @deprecated Use {@link QuaternionMath#POW} instead
     */
    public static final Binary POW = QuaternionMath.POW;

    /**
     * @deprecated Use {@link QuaternionMath#POWER} instead
     */
    public static final Parameter POWER = QuaternionMath.POWER;

    /**
     * @deprecated Use {@link QuaternionMath#RINT} instead
     */
    public static final Unary RINT = QuaternionMath.RINT;

    /**
     * @deprecated Use {@link QuaternionMath#ROOT} instead
     */
    public static final Parameter ROOT = QuaternionMath.ROOT;

    /**
     * @deprecated Use {@link QuaternionMath#SCALE} instead
     */
    public static final Parameter SCALE = QuaternionMath.SCALE;

    /**
     * @deprecated Use {@link QuaternionMath#SIGNUM} instead
     */
    public static final Unary SIGNUM = QuaternionMath.SIGNUM;

    /**
     * @deprecated Use {@link QuaternionMath#SIN} instead
     */
    public static final Unary SIN = QuaternionMath.SIN;

    /**
     * @deprecated Use {@link QuaternionMath#SINH} instead
     */
    public static final Unary SINH = QuaternionMath.SINH;

    /**
     * @deprecated Use {@link QuaternionMath#SQRT} instead
     */
    public static final Unary SQRT = QuaternionMath.SQRT;

    /**
     * @deprecated Use {@link QuaternionMath#SQRT1PX2} instead
     */
    public static final Unary SQRT1PX2 = QuaternionMath.SQRT1PX2;

    /**
     * @deprecated Use {@link QuaternionMath#SUBTRACT} instead
     */
    public static final Binary SUBTRACT = QuaternionMath.SUBTRACT;

    /**
     * @deprecated Use {@link QuaternionMath#TAN} instead
     */
    public static final Unary TAN = QuaternionMath.TAN;

    /**
     * @deprecated Use {@link QuaternionMath#TANH} instead
     */
    public static final Unary TANH = QuaternionMath.TANH;

    /**
     * @deprecated Use {@link QuaternionMath#VALUE} instead
     */
    public static final Unary VALUE = QuaternionMath.VALUE;

    private static final QuaternionFunction SET = new QuaternionFunction();

    public static QuaternionFunction getSet() {
        return SET;
    }

    private QuaternionFunction() {
        super();
    }

    @Override
    public UnaryFunction<Quaternion> abs() {
        return QuaternionMath.ABS;
    }

    @Override
    public UnaryFunction<Quaternion> acos() {
        return QuaternionMath.ACOS;
    }

    @Override
    public UnaryFunction<Quaternion> acosh() {
        return QuaternionMath.ACOSH;
    }

    @Override
    public BinaryFunction<Quaternion> add() {
        return QuaternionMath.ADD;
    }

    @Override
    public AggregatorSet<Quaternion> aggregator() {
        return QuaternionAggregator.getSet();
    }

    @Override
    public UnaryFunction<Quaternion> asin() {
        return QuaternionMath.ASIN;
    }

    @Override
    public UnaryFunction<Quaternion> asinh() {
        return QuaternionMath.ASINH;
    }

    @Override
    public UnaryFunction<Quaternion> atan() {
        return QuaternionMath.ATAN;
    }

    @Override
    public BinaryFunction<Quaternion> atan2() {
        return QuaternionMath.ATAN2;
    }

    @Override
    public UnaryFunction<Quaternion> atanh() {
        return QuaternionMath.ATANH;
    }

    @Override
    public UnaryFunction<Quaternion> cardinality() {
        return QuaternionMath.CARDINALITY;
    }

    @Override
    public UnaryFunction<Quaternion> cbrt() {
        return QuaternionMath.CBRT;
    }

    @Override
    public UnaryFunction<Quaternion> ceil() {
        return QuaternionMath.CEIL;
    }

    @Override
    public UnaryFunction<Quaternion> conjugate() {
        return QuaternionMath.CONJUGATE;
    }

    @Override
    public UnaryFunction<Quaternion> cos() {
        return QuaternionMath.COS;
    }

    @Override
    public UnaryFunction<Quaternion> cosh() {
        return QuaternionMath.COSH;
    }

    @Override
    public BinaryFunction<Quaternion> divide() {
        return QuaternionMath.DIVIDE;
    }

    @Override
    public Unary enforce(final NumberContext context) {
        return t -> Quaternion.valueOf(context.enforce(t));
    }

    @Override
    public UnaryFunction<Quaternion> exp() {
        return QuaternionMath.EXP;
    }

    @Override
    public UnaryFunction<Quaternion> expm1() {
        return QuaternionMath.EXPM1;
    }

    @Override
    public UnaryFunction<Quaternion> floor() {
        return QuaternionMath.FLOOR;
    }

    @Override
    public BinaryFunction<Quaternion> hypot() {
        return QuaternionMath.HYPOT;
    }

    @Override
    public UnaryFunction<Quaternion> invert() {
        return QuaternionMath.INVERT;
    }

    @Override
    public UnaryFunction<Quaternion> log() {
        return QuaternionMath.LOG;
    }

    @Override
    public UnaryFunction<Quaternion> log10() {
        return QuaternionMath.LOG10;
    }

    @Override
    public UnaryFunction<Quaternion> log1p() {
        return QuaternionMath.LOG1P;
    }

    @Override
    public UnaryFunction<Quaternion> logistic() {
        return QuaternionMath.LOGISTIC;
    }

    @Override
    public UnaryFunction<Quaternion> logit() {
        return QuaternionMath.LOGIT;
    }

    @Override
    public BinaryFunction<Quaternion> max() {
        return QuaternionMath.MAX;
    }

    @Override
    public BinaryFunction<Quaternion> min() {
        return QuaternionMath.MIN;
    }

    @Override
    public BinaryFunction<Quaternion> multiply() {
        return QuaternionMath.MULTIPLY;
    }

    @Override
    public UnaryFunction<Quaternion> negate() {
        return QuaternionMath.NEGATE;
    }

    @Override
    public BinaryFunction<Quaternion> pow() {
        return QuaternionMath.POW;
    }

    @Override
    public ParameterFunction<Quaternion> power() {
        return QuaternionMath.POWER;
    }

    @Override
    public UnaryFunction<Quaternion> rint() {
        return QuaternionMath.RINT;
    }

    @Override
    public ParameterFunction<Quaternion> root() {
        return QuaternionMath.ROOT;
    }

    @Override
    public ParameterFunction<Quaternion> scale() {
        return QuaternionMath.SCALE;
    }

    @Override
    public UnaryFunction<Quaternion> signum() {
        return QuaternionMath.SIGNUM;
    }

    @Override
    public UnaryFunction<Quaternion> sin() {
        return QuaternionMath.SIN;
    }

    @Override
    public UnaryFunction<Quaternion> sinh() {
        return QuaternionMath.SINH;
    }

    @Override
    public UnaryFunction<Quaternion> sqrt() {
        return QuaternionMath.SQRT;
    }

    @Override
    public UnaryFunction<Quaternion> sqrt1px2() {
        return QuaternionMath.SQRT1PX2;
    }

    @Override
    public BinaryFunction<Quaternion> subtract() {
        return QuaternionMath.SUBTRACT;
    }

    @Override
    public UnaryFunction<Quaternion> tan() {
        return QuaternionMath.TAN;
    }

    @Override
    public UnaryFunction<Quaternion> tanh() {
        return QuaternionMath.TANH;
    }

    @Override
    public UnaryFunction<Quaternion> value() {
        return QuaternionMath.VALUE;
    }

}

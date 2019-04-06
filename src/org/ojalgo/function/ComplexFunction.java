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

import static org.ojalgo.function.constant.PrimitiveMath.*;

import org.ojalgo.function.aggregator.AggregatorSet;
import org.ojalgo.function.aggregator.ComplexAggregator;
import org.ojalgo.function.constant.ComplexMath;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.type.context.NumberContext;

public final class ComplexFunction extends FunctionSet<ComplexNumber> {

    @FunctionalInterface
    public static interface Binary extends BinaryFunction<ComplexNumber> {

        default double invoke(final double arg1, final double arg2) {
            return this.invoke(ComplexNumber.valueOf(arg1), ComplexNumber.valueOf(arg2)).doubleValue();
        }

    }

    @FunctionalInterface
    public static interface Consumer extends VoidFunction<ComplexNumber> {

        default void invoke(final double arg) {
            this.invoke(ComplexNumber.valueOf(arg));
        }

    }

    @FunctionalInterface
    public static interface Parameter extends ParameterFunction<ComplexNumber> {

        default double invoke(final double arg, final int param) {
            return this.invoke(ComplexNumber.valueOf(arg), param).doubleValue();
        }

    }

    @FunctionalInterface
    public static interface Predicate extends PredicateFunction<ComplexNumber> {

        default boolean invoke(final double arg) {
            return this.invoke(ComplexNumber.valueOf(arg));
        }

    }

    @FunctionalInterface
    public static interface Unary extends UnaryFunction<ComplexNumber> {

        default double invoke(final double arg) {
            return this.invoke(ComplexNumber.valueOf(arg)).doubleValue();
        }

    }

    /**
     * @deprecated Use {@link ComplexMath#ABS} instead
     */
    public static final Unary ABS = ComplexMath.ABS;

    /**
     * @deprecated Use {@link ComplexMath#ACOS} instead
     */
    public static final Unary ACOS = ComplexMath.ACOS;

    /**
     * @deprecated Use {@link ComplexMath#ACOSH} instead
     */
    public static final Unary ACOSH = ComplexMath.ACOSH;

    /**
     * @deprecated Use {@link ComplexMath#ADD} instead
     */
    public static final Binary ADD = ComplexMath.ADD;

    /**
     * @deprecated Use {@link ComplexMath#ASIN} instead
     */
    public static final Unary ASIN = ComplexMath.ASIN;

    /**
     * @deprecated Use {@link ComplexMath#ASINH} instead
     */
    public static final Unary ASINH = ComplexMath.ASINH;

    /**
     * @deprecated Use {@link ComplexMath#ATAN} instead
     */
    public static final Unary ATAN = ComplexMath.ATAN;

    /**
     * @deprecated Use {@link ComplexMath#ATAN2} instead
     */
    public static final Binary ATAN2 = ComplexMath.ATAN2;

    /**
     * @deprecated Use {@link ComplexMath#ATANH} instead
     */
    public static final Unary ATANH = ComplexMath.ATANH;

    /**
     * @deprecated Use {@link ComplexMath#CARDINALITY} instead
     */
    public static final Unary CARDINALITY = ComplexMath.CARDINALITY;

    /**
     * @deprecated Use {@link ComplexMath#CBRT} instead
     */
    public static final Unary CBRT = ComplexMath.CBRT;

    /**
     * @deprecated Use {@link ComplexMath#CEIL} instead
     */
    public static final Unary CEIL = ComplexMath.CEIL;

    /**
     * @deprecated Use {@link ComplexMath#CONJUGATE} instead
     */
    public static final Unary CONJUGATE = ComplexMath.CONJUGATE;

    /**
     * @deprecated Use {@link ComplexMath#COS} instead
     */
    public static final Unary COS = ComplexMath.COS;

    /**
     * @deprecated Use {@link ComplexMath#COSH} instead
     */
    public static final Unary COSH = ComplexMath.COSH;

    /**
     * @deprecated Use {@link ComplexMath#DIVIDE} instead
     */
    public static final Binary DIVIDE = ComplexMath.DIVIDE;

    /**
     * @deprecated Use {@link ComplexMath#EXP} instead
     */
    public static final Unary EXP = ComplexMath.EXP;

    /**
     * @deprecated Use {@link ComplexMath#EXPM1} instead
     */
    public static final Unary EXPM1 = ComplexMath.EXPM1;

    /**
     * @deprecated Use {@link ComplexMath#FLOOR} instead
     */
    public static final Unary FLOOR = ComplexMath.FLOOR;

    /**
     * @deprecated Use {@link ComplexMath#HYPOT} instead
     */
    public static final Binary HYPOT = ComplexMath.HYPOT;

    /**
     * @deprecated Use {@link ComplexMath#INVERT} instead
     */
    public static final Unary INVERT = ComplexMath.INVERT;

    /**
     * @deprecated Use {@link ComplexMath#LOG} instead
     */
    public static final Unary LOG = ComplexMath.LOG;

    /**
     * @deprecated Use {@link ComplexMath#LOG10} instead
     */
    public static final Unary LOG10 = ComplexMath.LOG10;

    /**
     * @deprecated Use {@link ComplexMath#LOG1P} instead
     */
    public static final Unary LOG1P = ComplexMath.LOG1P;

    /**
     * @deprecated Use {@link ComplexMath#LOGISTIC} instead
     */
    public static final Unary LOGISTIC = ComplexMath.LOGISTIC;

    /**
     * @deprecated Use {@link ComplexMath#LOGIT} instead
     */
    public static final Unary LOGIT = ComplexMath.LOGIT;

    /**
     * @deprecated Use {@link ComplexMath#MAX} instead
     */
    public static final Binary MAX = ComplexMath.MAX;

    /**
     * @deprecated Use {@link ComplexMath#MIN} instead
     */
    public static final Binary MIN = ComplexMath.MIN;

    /**
     * @deprecated Use {@link ComplexMath#MULTIPLY} instead
     */
    public static final Binary MULTIPLY = ComplexMath.MULTIPLY;

    /**
     * @deprecated Use {@link ComplexMath#NEGATE} instead
     */
    public static final Unary NEGATE = ComplexMath.NEGATE;

    /**
     * @deprecated Use {@link ComplexMath#POW} instead
     */
    public static final Binary POW = ComplexMath.POW;

    /**
     * @deprecated Use {@link ComplexMath#POWER} instead
     */
    public static final Parameter POWER = ComplexMath.POWER;

    /**
     * @deprecated Use {@link ComplexMath#RINT} instead
     */
    public static final Unary RINT = ComplexMath.RINT;

    /**
     * @deprecated Use {@link ComplexMath#ROOT} instead
     */
    public static final Parameter ROOT = ComplexMath.ROOT;

    /**
     * @deprecated Use {@link ComplexMath#SCALE} instead
     */
    public static final Parameter SCALE = ComplexMath.SCALE;

    /**
     * @deprecated Use {@link ComplexMath#SIGNUM} instead
     */
    public static final Unary SIGNUM = ComplexMath.SIGNUM;

    /**
     * @deprecated Use {@link ComplexMath#SIN} instead
     */
    public static final Unary SIN = ComplexMath.SIN;

    /**
     * @deprecated Use {@link ComplexMath#SINH} instead
     */
    public static final Unary SINH = ComplexMath.SINH;

    /**
     * @deprecated Use {@link ComplexMath#SQRT} instead
     */
    public static final Unary SQRT = ComplexMath.SQRT;

    /**
     * @deprecated Use {@link ComplexMath#SQRT1PX2} instead
     */
    public static final Unary SQRT1PX2 = ComplexMath.SQRT1PX2;

    /**
     * @deprecated Use {@link ComplexMath#SUBTRACT} instead
     */
    public static final Binary SUBTRACT = ComplexMath.SUBTRACT;

    /**
     * @deprecated Use {@link ComplexMath#TAN} instead
     */
    public static final Unary TAN = ComplexMath.TAN;

    /**
     * @deprecated Use {@link ComplexMath#TANH} instead
     */
    public static final Unary TANH = ComplexMath.TANH;

    /**
     * @deprecated Use {@link ComplexMath#VALUE} instead
     */
    public static final Unary VALUE = ComplexMath.VALUE;

    private static final ComplexFunction SET = new ComplexFunction();

    public static ComplexFunction getSet() {
        return SET;
    }

    private ComplexFunction() {
        super();
    }

    @Override
    public UnaryFunction<ComplexNumber> abs() {
        return ComplexMath.ABS;
    }

    @Override
    public UnaryFunction<ComplexNumber> acos() {
        return ComplexMath.ACOS;
    }

    @Override
    public UnaryFunction<ComplexNumber> acosh() {
        return ComplexMath.ACOSH;
    }

    @Override
    public BinaryFunction<ComplexNumber> add() {
        return ComplexMath.ADD;
    }

    @Override
    public AggregatorSet<ComplexNumber> aggregator() {
        return ComplexAggregator.getSet();
    }

    @Override
    public UnaryFunction<ComplexNumber> asin() {
        return ComplexMath.ASIN;
    }

    @Override
    public UnaryFunction<ComplexNumber> asinh() {
        return ComplexMath.ASINH;
    }

    @Override
    public UnaryFunction<ComplexNumber> atan() {
        return ComplexMath.ATAN;
    }

    @Override
    public BinaryFunction<ComplexNumber> atan2() {
        return ComplexMath.ATAN2;
    }

    @Override
    public UnaryFunction<ComplexNumber> atanh() {
        return ComplexMath.ATANH;
    }

    @Override
    public UnaryFunction<ComplexNumber> cardinality() {
        return ComplexMath.CARDINALITY;
    }

    @Override
    public UnaryFunction<ComplexNumber> cbrt() {
        return ComplexMath.CBRT;
    }

    @Override
    public UnaryFunction<ComplexNumber> ceil() {
        return ComplexMath.CEIL;
    }

    @Override
    public UnaryFunction<ComplexNumber> conjugate() {
        return ComplexMath.CONJUGATE;
    }

    @Override
    public UnaryFunction<ComplexNumber> cos() {
        return ComplexMath.COS;
    }

    @Override
    public UnaryFunction<ComplexNumber> cosh() {
        return ComplexMath.COSH;
    }

    @Override
    public BinaryFunction<ComplexNumber> divide() {
        return ComplexMath.DIVIDE;
    }

    @Override
    public Unary enforce(final NumberContext context) {
        return t -> ComplexNumber.valueOf(context.enforce(t));
    }

    @Override
    public UnaryFunction<ComplexNumber> exp() {
        return ComplexMath.EXP;
    }

    @Override
    public UnaryFunction<ComplexNumber> expm1() {
        return ComplexMath.EXPM1;
    }

    @Override
    public UnaryFunction<ComplexNumber> floor() {
        return ComplexMath.FLOOR;
    }

    @Override
    public BinaryFunction<ComplexNumber> hypot() {
        return ComplexMath.HYPOT;
    }

    @Override
    public UnaryFunction<ComplexNumber> invert() {
        return ComplexMath.INVERT;
    }

    @Override
    public UnaryFunction<ComplexNumber> log() {
        return ComplexMath.LOG;
    }

    @Override
    public UnaryFunction<ComplexNumber> log10() {
        return ComplexMath.LOG10;
    }

    @Override
    public UnaryFunction<ComplexNumber> log1p() {
        return ComplexMath.LOG1P;
    }

    @Override
    public UnaryFunction<ComplexNumber> logistic() {
        return ComplexMath.LOGISTIC;
    }

    @Override
    public UnaryFunction<ComplexNumber> logit() {
        return ComplexMath.LOGIT;
    }

    @Override
    public BinaryFunction<ComplexNumber> max() {
        return ComplexMath.MAX;
    }

    @Override
    public BinaryFunction<ComplexNumber> min() {
        return ComplexMath.MIN;
    }

    @Override
    public BinaryFunction<ComplexNumber> multiply() {
        return ComplexMath.MULTIPLY;
    }

    @Override
    public UnaryFunction<ComplexNumber> negate() {
        return ComplexMath.NEGATE;
    }

    @Override
    public BinaryFunction<ComplexNumber> pow() {
        return ComplexMath.POW;
    }

    @Override
    public ParameterFunction<ComplexNumber> power() {
        return ComplexMath.POWER;
    }

    @Override
    public UnaryFunction<ComplexNumber> rint() {
        return ComplexMath.RINT;
    }

    @Override
    public ParameterFunction<ComplexNumber> root() {
        return ComplexMath.ROOT;
    }

    @Override
    public ParameterFunction<ComplexNumber> scale() {
        return ComplexMath.SCALE;
    }

    @Override
    public UnaryFunction<ComplexNumber> signum() {
        return ComplexMath.SIGNUM;
    }

    @Override
    public UnaryFunction<ComplexNumber> sin() {
        return ComplexMath.SIN;
    }

    @Override
    public UnaryFunction<ComplexNumber> sinh() {
        return ComplexMath.SINH;
    }

    @Override
    public UnaryFunction<ComplexNumber> sqrt() {
        return ComplexMath.SQRT;
    }

    @Override
    public UnaryFunction<ComplexNumber> sqrt1px2() {
        return ComplexMath.SQRT1PX2;
    }

    @Override
    public BinaryFunction<ComplexNumber> subtract() {
        return ComplexMath.SUBTRACT;
    }

    @Override
    public UnaryFunction<ComplexNumber> tan() {
        return ComplexMath.TAN;
    }

    @Override
    public UnaryFunction<ComplexNumber> tanh() {
        return ComplexMath.TANH;
    }

    @Override
    public UnaryFunction<ComplexNumber> value() {
        return ComplexMath.VALUE;
    }

}

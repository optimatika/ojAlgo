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
import org.ojalgo.function.aggregator.PrimitiveAggregator;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.type.context.NumberContext;

/**
 * Only the primitive parameter (double) methods are actually implemented. The methods with the reference type
 * parameters (Double) should delegate to the primitive methods (and do nothing else). The various
 * implementations should delegate as much as possible to {@link java.lang.Math} and/or built-in Java
 * operators.
 *
 * @author apete
 */
public final class PrimitiveFunction extends FunctionSet<Double> {

    @FunctionalInterface
    public static interface Binary extends BinaryFunction<Double> {

        default Double invoke(final Double arg1, final Double arg2) {
            return this.invoke(arg1.doubleValue(), arg2.doubleValue());
        }

    }

    @FunctionalInterface
    public static interface Consumer extends VoidFunction<Double> {

        default void invoke(final Double arg) {
            this.invoke(arg.doubleValue());
        }

    }

    @FunctionalInterface
    public static interface Parameter extends ParameterFunction<Double> {

        default Double invoke(final Double arg, final int param) {
            return this.invoke(arg.doubleValue(), param);
        }

    }

    @FunctionalInterface
    public static interface Predicate extends PredicateFunction<Double> {

        default boolean invoke(final Double arg) {
            return this.invoke(arg.doubleValue());
        }

    }

    @FunctionalInterface
    public static interface Unary extends UnaryFunction<Double> {

        default Double invoke(final Double arg) {
            return this.invoke(arg.doubleValue());
        }

    }

    /**
     * @deprecated v48 Use {@link PrimitiveMath#ABS} instead
     */
    @Deprecated
    public static final Unary ABS = PrimitiveMath.ABS;

    /**
     * @deprecated v48 Use {@link PrimitiveMath#ACOS} instead
     */
    @Deprecated
    public static final Unary ACOS = PrimitiveMath.ACOS;

    /**
     * @deprecated v48 Use {@link PrimitiveMath#ACOSH} instead
     */
    @Deprecated
    public static final Unary ACOSH = PrimitiveMath.ACOSH;

    /**
     * @deprecated v48 Use {@link PrimitiveMath#ADD} instead
     */
    @Deprecated
    public static final Binary ADD = PrimitiveMath.ADD;

    /**
     * @deprecated v48 Use {@link PrimitiveMath#ASIN} instead
     */
    @Deprecated
    public static final Unary ASIN = PrimitiveMath.ASIN;

    /**
     * @deprecated v48 Use {@link PrimitiveMath#ASINH} instead
     */
    @Deprecated
    public static final Unary ASINH = PrimitiveMath.ASINH;

    /**
     * @deprecated v48 Use {@link PrimitiveMath#ATAN} instead
     */
    @Deprecated
    public static final Unary ATAN = PrimitiveMath.ATAN;

    /**
     * @deprecated v48 Use {@link PrimitiveMath#ATAN2} instead
     */
    @Deprecated
    public static final Binary ATAN2 = PrimitiveMath.ATAN2;

    /**
     * @deprecated v48 Use {@link PrimitiveMath#ATANH} instead
     */
    @Deprecated
    public static final Unary ATANH = PrimitiveMath.ATANH;

    /**
     * @deprecated v48 Use {@link PrimitiveMath#CARDINALITY} instead
     */
    @Deprecated
    public static final Unary CARDINALITY = PrimitiveMath.CARDINALITY;

    /**
     * @deprecated v48 Use {@link PrimitiveMath#CBRT} instead
     */
    @Deprecated
    public static final Unary CBRT = PrimitiveMath.CBRT;

    /**
     * @deprecated v48 Use {@link PrimitiveMath#CEIL} instead
     */
    @Deprecated
    public static final Unary CEIL = PrimitiveMath.CEIL;

    /**
     * @deprecated v48 Use {@link PrimitiveMath#CONJUGATE} instead
     */
    @Deprecated
    public static final Unary CONJUGATE = PrimitiveMath.CONJUGATE;

    /**
     * @deprecated v48 Use {@link PrimitiveMath#COS} instead
     */
    @Deprecated
    public static final Unary COS = PrimitiveMath.COS;

    /**
     * @deprecated v48 Use {@link PrimitiveMath#COSH} instead
     */
    @Deprecated
    public static final Unary COSH = PrimitiveMath.COSH;

    /**
     * @deprecated v48 Use {@link PrimitiveMath#DIVIDE} instead
     */
    @Deprecated
    public static final Binary DIVIDE = PrimitiveMath.DIVIDE;

    /**
     * @deprecated v48 Use {@link PrimitiveMath#EXP} instead
     */
    @Deprecated
    public static final Unary EXP = PrimitiveMath.EXP;

    /**
     * @deprecated v48 Use {@link PrimitiveMath#EXPM1} instead
     */
    @Deprecated
    public static final Unary EXPM1 = PrimitiveMath.EXPM1;

    /**
     * @deprecated v48 Use {@link PrimitiveMath#FLOOR} instead
     */
    @Deprecated
    public static final Unary FLOOR = PrimitiveMath.FLOOR;

    /**
     * @deprecated v48 Use {@link PrimitiveMath#HYPOT} instead
     */
    @Deprecated
    public static final Binary HYPOT = PrimitiveMath.HYPOT;

    /**
     * @deprecated v48 Use {@link PrimitiveMath#INVERT} instead
     */
    @Deprecated
    public static final Unary INVERT = PrimitiveMath.INVERT;

    /**
     * @deprecated v48 Use {@link PrimitiveMath#LOG} instead
     */
    @Deprecated
    public static final Unary LOG = PrimitiveMath.LOG;

    /**
     * @deprecated v48 Use {@link PrimitiveMath#LOG10} instead
     */
    @Deprecated
    public static final Unary LOG10 = PrimitiveMath.LOG10;

    /**
     * @deprecated v48 Use {@link PrimitiveMath#LOG1P} instead
     */
    @Deprecated
    public static final Unary LOG1P = PrimitiveMath.LOG1P;

    /**
     * @deprecated v48 Use {@link PrimitiveMath#LOGISTIC} instead
     */
    @Deprecated
    public static final Unary LOGISTIC = PrimitiveMath.LOGISTIC;

    /**
     * @deprecated v48 Use {@link PrimitiveMath#LOGIT} instead
     */
    @Deprecated
    public static final Unary LOGIT = PrimitiveMath.LOGIT;

    /**
     * @deprecated v48 Use {@link PrimitiveMath#MAX} instead
     */
    @Deprecated
    public static final Binary MAX = PrimitiveMath.MAX;

    /**
     * @deprecated v48 Use {@link PrimitiveMath#MIN} instead
     */
    @Deprecated
    public static final Binary MIN = PrimitiveMath.MIN;

    /**
     * @deprecated v48 Use {@link PrimitiveMath#MULTIPLY} instead
     */
    @Deprecated
    public static final Binary MULTIPLY = PrimitiveMath.MULTIPLY;

    /**
     * @deprecated v48 Use {@link PrimitiveMath#NEGATE} instead
     */
    @Deprecated
    public static final Unary NEGATE = PrimitiveMath.NEGATE;

    /**
     * @deprecated v48 Use {@link PrimitiveMath#POW} instead
     */
    @Deprecated
    public static final Binary POW = PrimitiveMath.POW;

    /**
     * @deprecated v48 Use {@link PrimitiveMath#POWER} instead
     */
    @Deprecated
    public static final Parameter POWER = PrimitiveMath.POWER;

    /**
     * @deprecated v48 Use {@link PrimitiveMath#RINT} instead
     */
    @Deprecated
    public static final Unary RINT = PrimitiveMath.RINT;

    /**
     * @deprecated v48 Use {@link PrimitiveMath#ROOT} instead
     */
    @Deprecated
    public static final Parameter ROOT = PrimitiveMath.ROOT;

    /**
     * @deprecated v48 Use {@link PrimitiveMath#SCALE} instead
     */
    @Deprecated
    public static final Parameter SCALE = PrimitiveMath.SCALE;

    /**
     * @deprecated v48 Use {@link PrimitiveMath#SIGNUM} instead
     */
    @Deprecated
    public static final Unary SIGNUM = PrimitiveMath.SIGNUM;

    /**
     * @deprecated v48 Use {@link PrimitiveMath#SIN} instead
     */
    @Deprecated
    public static final Unary SIN = PrimitiveMath.SIN;

    /**
     * @deprecated v48 Use {@link PrimitiveMath#SINH} instead
     */
    @Deprecated
    public static final Unary SINH = PrimitiveMath.SINH;

    /**
     * @deprecated v48 Use {@link PrimitiveMath#SQRT} instead
     */
    @Deprecated
    public static final Unary SQRT = PrimitiveMath.SQRT;

    /**
     * @deprecated v48 Use {@link PrimitiveMath#SQRT1PX2} instead
     */
    @Deprecated
    public static final Unary SQRT1PX2 = PrimitiveMath.SQRT1PX2;

    /**
     * @deprecated v48 Use {@link PrimitiveMath#SUBTRACT} instead
     */
    @Deprecated
    public static final Binary SUBTRACT = PrimitiveMath.SUBTRACT;

    /**
     * @deprecated v48 Use {@link PrimitiveMath#TAN} instead
     */
    @Deprecated
    public static final Unary TAN = PrimitiveMath.TAN;

    /**
     * @deprecated v48 Use {@link PrimitiveMath#TANH} instead
     */
    @Deprecated
    public static final Unary TANH = PrimitiveMath.TANH;

    /**
     * @deprecated v48 Use {@link PrimitiveMath#VALUE} instead
     */
    @Deprecated
    public static final Unary VALUE = PrimitiveMath.VALUE;

    private static final PrimitiveFunction SET = new PrimitiveFunction();

    public static PrimitiveFunction getSet() {
        return SET;
    }

    private PrimitiveFunction() {
        super();
    }

    @Override
    public UnaryFunction<Double> abs() {
        return PrimitiveMath.ABS;
    }

    @Override
    public UnaryFunction<Double> acos() {
        return PrimitiveMath.ACOS;
    }

    @Override
    public UnaryFunction<Double> acosh() {
        return PrimitiveMath.ACOSH;
    }

    @Override
    public BinaryFunction<Double> add() {
        return PrimitiveMath.ADD;
    }

    @Override
    public AggregatorSet<Double> aggregator() {
        return PrimitiveAggregator.getSet();
    }

    @Override
    public UnaryFunction<Double> asin() {
        return PrimitiveMath.ASIN;
    }

    @Override
    public UnaryFunction<Double> asinh() {
        return PrimitiveMath.ASINH;
    }

    @Override
    public UnaryFunction<Double> atan() {
        return PrimitiveMath.ATAN;
    }

    @Override
    public BinaryFunction<Double> atan2() {
        return PrimitiveMath.ATAN2;
    }

    @Override
    public UnaryFunction<Double> atanh() {
        return PrimitiveMath.ATANH;
    }

    @Override
    public UnaryFunction<Double> cardinality() {
        return PrimitiveMath.CARDINALITY;
    }

    @Override
    public UnaryFunction<Double> cbrt() {
        return PrimitiveMath.CBRT;
    }

    @Override
    public UnaryFunction<Double> ceil() {
        return PrimitiveMath.CEIL;
    }

    @Override
    public UnaryFunction<Double> conjugate() {
        return PrimitiveMath.CONJUGATE;
    }

    @Override
    public UnaryFunction<Double> cos() {
        return PrimitiveMath.COS;
    }

    @Override
    public UnaryFunction<Double> cosh() {
        return PrimitiveMath.COSH;
    }

    @Override
    public BinaryFunction<Double> divide() {
        return PrimitiveMath.DIVIDE;
    }

    @Override
    public Unary enforce(final NumberContext context) {
        return t -> context.enforce(t);
    }

    @Override
    public UnaryFunction<Double> exp() {
        return PrimitiveMath.EXP;
    }

    @Override
    public UnaryFunction<Double> expm1() {
        return PrimitiveMath.EXPM1;
    }

    @Override
    public UnaryFunction<Double> floor() {
        return PrimitiveMath.FLOOR;
    }

    @Override
    public BinaryFunction<Double> hypot() {
        return PrimitiveMath.HYPOT;
    }

    @Override
    public UnaryFunction<Double> invert() {
        return PrimitiveMath.INVERT;
    }

    @Override
    public UnaryFunction<Double> log() {
        return PrimitiveMath.LOG;
    }

    @Override
    public UnaryFunction<Double> log10() {
        return PrimitiveMath.LOG10;
    }

    @Override
    public UnaryFunction<Double> log1p() {
        return PrimitiveMath.LOG1P;
    }

    @Override
    public UnaryFunction<Double> logistic() {
        return PrimitiveMath.LOGISTIC;
    }

    @Override
    public UnaryFunction<Double> logit() {
        return PrimitiveMath.LOGIT;
    }

    @Override
    public BinaryFunction<Double> max() {
        return PrimitiveMath.MAX;
    }

    @Override
    public BinaryFunction<Double> min() {
        return PrimitiveMath.MIN;
    }

    @Override
    public BinaryFunction<Double> multiply() {
        return PrimitiveMath.MULTIPLY;
    }

    @Override
    public UnaryFunction<Double> negate() {
        return PrimitiveMath.NEGATE;
    }

    @Override
    public BinaryFunction<Double> pow() {
        return PrimitiveMath.POW;
    }

    @Override
    public ParameterFunction<Double> power() {
        return PrimitiveMath.POWER;
    }

    @Override
    public UnaryFunction<Double> rint() {
        return PrimitiveMath.RINT;
    }

    @Override
    public ParameterFunction<Double> root() {
        return PrimitiveMath.ROOT;
    }

    @Override
    public ParameterFunction<Double> scale() {
        return PrimitiveMath.SCALE;
    }

    @Override
    public UnaryFunction<Double> signum() {
        return PrimitiveMath.SIGNUM;
    }

    @Override
    public UnaryFunction<Double> sin() {
        return PrimitiveMath.SIN;
    }

    @Override
    public UnaryFunction<Double> sinh() {
        return PrimitiveMath.SINH;
    }

    @Override
    public UnaryFunction<Double> sqrt() {
        return PrimitiveMath.SQRT;
    }

    @Override
    public UnaryFunction<Double> sqrt1px2() {
        return PrimitiveMath.SQRT1PX2;
    }

    @Override
    public BinaryFunction<Double> subtract() {
        return PrimitiveMath.SUBTRACT;
    }

    @Override
    public UnaryFunction<Double> tan() {
        return PrimitiveMath.TAN;
    }

    @Override
    public UnaryFunction<Double> tanh() {
        return PrimitiveMath.TANH;
    }

    @Override
    public UnaryFunction<Double> value() {
        return PrimitiveMath.VALUE;
    }

}

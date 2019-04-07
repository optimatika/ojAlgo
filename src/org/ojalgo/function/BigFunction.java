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

import java.math.BigDecimal;

import org.ojalgo.function.aggregator.AggregatorSet;
import org.ojalgo.function.aggregator.BigAggregator;
import org.ojalgo.function.constant.BigMath;
import org.ojalgo.type.context.NumberContext;

/**
 * Only the reference type parameter (BigDecimal) methods are actually implemented. The methods with the
 * primitive parameters (double) should create a BigDecimal and then delegate to the primitive methods (and do
 * nothing else). If possible the implementations should be pure BigDecimal arithmatic without rounding. If
 * rounding is necessary MathContext.DECIMAL128 should be used. If BigDecimal arithmatic is not possible at
 * all the implementation should delegate to PrimitiveFunction.
 *
 * @author apete
 */
public final class BigFunction extends FunctionSet<BigDecimal> {

    @FunctionalInterface
    public static interface Binary extends BinaryFunction<BigDecimal> {

        default double invoke(final double arg1, final double arg2) {
            return this.invoke(BigDecimal.valueOf(arg1), BigDecimal.valueOf(arg2)).doubleValue();
        }

    }

    @FunctionalInterface
    public static interface Consumer extends VoidFunction<BigDecimal> {

        default void invoke(final double arg) {
            this.invoke(BigDecimal.valueOf(arg));
        }

    }

    @FunctionalInterface
    public static interface Parameter extends ParameterFunction<BigDecimal> {

        default double invoke(final double arg, final int param) {
            return this.invoke(BigDecimal.valueOf(arg), param).doubleValue();
        }

    }

    @FunctionalInterface
    public static interface Predicate extends PredicateFunction<BigDecimal> {

        default boolean invoke(final double arg) {
            return this.invoke(BigDecimal.valueOf(arg));
        }

    }

    @FunctionalInterface
    public static interface Unary extends UnaryFunction<BigDecimal> {

        default double invoke(final double arg) {
            return this.invoke(BigDecimal.valueOf(arg)).doubleValue();
        }

    }

    /**
     * @deprecated v48 Use {@link BigMath#ABS} instead
     */
    @Deprecated
    public static final Unary ABS = BigMath.ABS;

    /**
     * @deprecated v48 Use {@link BigMath#ACOS} instead
     */
    @Deprecated
    public static final Unary ACOS = BigMath.ACOS;

    /**
     * @deprecated v48 Use {@link BigMath#ACOSH} instead
     */
    @Deprecated
    public static final Unary ACOSH = BigMath.ACOSH;

    /**
     * @deprecated v48 Use {@link BigMath#ADD} instead
     */
    @Deprecated
    public static final Binary ADD = BigMath.ADD;

    /**
     * @deprecated v48 Use {@link BigMath#ASIN} instead
     */
    @Deprecated
    public static final Unary ASIN = BigMath.ASIN;

    /**
     * @deprecated v48 Use {@link BigMath#ASINH} instead
     */
    @Deprecated
    public static final Unary ASINH = BigMath.ASINH;

    /**
     * @deprecated v48 Use {@link BigMath#ATAN} instead
     */
    @Deprecated
    public static final Unary ATAN = BigMath.ATAN;

    /**
     * @deprecated v48 Use {@link BigMath#ATAN2} instead
     */
    @Deprecated
    public static final Binary ATAN2 = BigMath.ATAN2;

    /**
     * @deprecated v48 Use {@link BigMath#ATANH} instead
     */
    @Deprecated
    public static final Unary ATANH = BigMath.ATANH;

    /**
     * @deprecated v48 Use {@link BigMath#CARDINALITY} instead
     */
    @Deprecated
    public static final Unary CARDINALITY = BigMath.CARDINALITY;

    /**
     * @deprecated v48 Use {@link BigMath#CBRT} instead
     */
    @Deprecated
    public static final Unary CBRT = BigMath.CBRT;

    /**
     * @deprecated v48 Use {@link BigMath#CEIL} instead
     */
    @Deprecated
    public static final Unary CEIL = BigMath.CEIL;

    /**
     * @deprecated v48 Use {@link BigMath#CONJUGATE} instead
     */
    @Deprecated
    public static final Unary CONJUGATE = BigMath.CONJUGATE;

    /**
     * @deprecated v48 Use {@link BigMath#COS} instead
     */
    @Deprecated
    public static final Unary COS = BigMath.COS;

    /**
     * @deprecated v48 Use {@link BigMath#COSH} instead
     */
    @Deprecated
    public static final Unary COSH = BigMath.COSH;

    /**
     * @deprecated v48 Use {@link BigMath#DIVIDE} instead
     */
    @Deprecated
    public static final Binary DIVIDE = BigMath.DIVIDE;

    /**
     * @deprecated v48 Use {@link BigMath#EXP} instead
     */
    @Deprecated
    public static final Unary EXP = BigMath.EXP;

    /**
     * @deprecated v48 Use {@link BigMath#EXPM1} instead
     */
    @Deprecated
    public static final Unary EXPM1 = BigMath.EXPM1;

    /**
     * @deprecated v48 Use {@link BigMath#FLOOR} instead
     */
    @Deprecated
    public static final Unary FLOOR = BigMath.FLOOR;

    /**
     * @deprecated v48 Use {@link BigMath#HYPOT} instead
     */
    @Deprecated
    public static final Binary HYPOT = BigMath.HYPOT;

    /**
     * @deprecated v48 Use {@link BigMath#INVERT} instead
     */
    @Deprecated
    public static final Unary INVERT = BigMath.INVERT;

    /**
     * @deprecated v48 Use {@link BigMath#LOG} instead
     */
    @Deprecated
    public static final Unary LOG = BigMath.LOG;

    /**
     * @deprecated v48 Use {@link BigMath#LOG10} instead
     */
    @Deprecated
    public static final Unary LOG10 = BigMath.LOG10;

    /**
     * @deprecated v48 Use {@link BigMath#LOG1P} instead
     */
    @Deprecated
    public static final Unary LOG1P = BigMath.LOG1P;

    /**
     * @deprecated v48 Use {@link BigMath#LOGISTIC} instead
     */
    @Deprecated
    public static final Unary LOGISTIC = BigMath.LOGISTIC;

    /**
     * @deprecated v48 Use {@link BigMath#LOGIT} instead
     */
    @Deprecated
    public static final Unary LOGIT = BigMath.LOGIT;

    /**
     * @deprecated v48 Use {@link BigMath#MAX} instead
     */
    @Deprecated
    public static final Binary MAX = BigMath.MAX;

    /**
     * @deprecated v48 Use {@link BigMath#MIN} instead
     */
    @Deprecated
    public static final Binary MIN = BigMath.MIN;

    /**
     * @deprecated v48 Use {@link BigMath#MULTIPLY} instead
     */
    @Deprecated
    public static final Binary MULTIPLY = BigMath.MULTIPLY;

    /**
     * @deprecated v48 Use {@link BigMath#NEGATE} instead
     */
    @Deprecated
    public static final Unary NEGATE = BigMath.NEGATE;

    /**
     * @deprecated v48 Use {@link BigMath#POW} instead
     */
    @Deprecated
    public static final Binary POW = BigMath.POW;

    /**
     * @deprecated v48 Use {@link BigMath#POWER} instead
     */
    @Deprecated
    public static final Parameter POWER = BigMath.POWER;

    /**
     * @deprecated v48 Use {@link BigMath#RINT} instead
     */
    @Deprecated
    public static final Unary RINT = BigMath.RINT;

    /**
     * @deprecated v48 Use {@link BigMath#ROOT} instead
     */
    @Deprecated
    public static final Parameter ROOT = BigMath.ROOT;

    /**
     * @deprecated v48 Use {@link BigMath#SCALE} instead
     */
    @Deprecated
    public static final Parameter SCALE = BigMath.SCALE;

    /**
     * @deprecated v48 Use {@link BigMath#SIGNUM} instead
     */
    @Deprecated
    public static final Unary SIGNUM = BigMath.SIGNUM;

    /**
     * @deprecated v48 Use {@link BigMath#SIN} instead
     */
    @Deprecated
    public static final Unary SIN = BigMath.SIN;

    /**
     * @deprecated v48 Use {@link BigMath#SINH} instead
     */
    @Deprecated
    public static final Unary SINH = BigMath.SINH;

    /**
     * @deprecated v48 Use {@link BigMath#SQRT} instead
     */
    @Deprecated
    public static final Unary SQRT = BigMath.SQRT;

    /**
     * @deprecated v48 Use {@link BigMath#SQRT1PX2} instead
     */
    @Deprecated
    public static final Unary SQRT1PX2 = BigMath.SQRT1PX2;

    /**
     * @deprecated v48 Use {@link BigMath#SUBTRACT} instead
     */
    @Deprecated
    public static final Binary SUBTRACT = BigMath.SUBTRACT;

    /**
     * @deprecated v48 Use {@link BigMath#TAN} instead
     */
    @Deprecated
    public static final Unary TAN = BigMath.TAN;

    /**
     * @deprecated v48 Use {@link BigMath#TANH} instead
     */
    @Deprecated
    public static final Unary TANH = BigMath.TANH;

    /**
     * @deprecated v48 Use {@link BigMath#VALUE} instead
     */
    @Deprecated
    public static final Unary VALUE = BigMath.VALUE;

    private static final BigFunction SET = new BigFunction();

    public static BigFunction getSet() {
        return SET;
    }

    private BigFunction() {
        super();
    }

    @Override
    public UnaryFunction<BigDecimal> abs() {
        return BigMath.ABS;
    }

    @Override
    public UnaryFunction<BigDecimal> acos() {
        return BigMath.ACOS;
    }

    @Override
    public UnaryFunction<BigDecimal> acosh() {
        return BigMath.ACOSH;
    }

    @Override
    public BinaryFunction<BigDecimal> add() {
        return BigMath.ADD;
    }

    @Override
    public AggregatorSet<BigDecimal> aggregator() {
        return BigAggregator.getSet();
    }

    @Override
    public UnaryFunction<BigDecimal> asin() {
        return BigMath.ASIN;
    }

    @Override
    public UnaryFunction<BigDecimal> asinh() {
        return BigMath.ASINH;
    }

    @Override
    public UnaryFunction<BigDecimal> atan() {
        return BigMath.ATAN;
    }

    @Override
    public BinaryFunction<BigDecimal> atan2() {
        return BigMath.ATAN2;
    }

    @Override
    public UnaryFunction<BigDecimal> atanh() {
        return BigMath.ATANH;
    }

    @Override
    public UnaryFunction<BigDecimal> cardinality() {
        return BigMath.CARDINALITY;
    }

    @Override
    public UnaryFunction<BigDecimal> cbrt() {
        return BigMath.CBRT;
    }

    @Override
    public UnaryFunction<BigDecimal> ceil() {
        return BigMath.CEIL;
    }

    @Override
    public UnaryFunction<BigDecimal> conjugate() {
        return BigMath.CONJUGATE;
    }

    @Override
    public UnaryFunction<BigDecimal> cos() {
        return BigMath.COS;
    }

    @Override
    public UnaryFunction<BigDecimal> cosh() {
        return BigMath.COSH;
    }

    @Override
    public BinaryFunction<BigDecimal> divide() {
        return BigMath.DIVIDE;
    }

    @Override
    public Unary enforce(final NumberContext context) {
        return t -> context.enforce(t);
    }

    @Override
    public UnaryFunction<BigDecimal> exp() {
        return BigMath.EXP;
    }

    @Override
    public UnaryFunction<BigDecimal> expm1() {
        return BigMath.EXPM1;
    }

    @Override
    public UnaryFunction<BigDecimal> floor() {
        return BigMath.FLOOR;
    }

    @Override
    public BinaryFunction<BigDecimal> hypot() {
        return BigMath.HYPOT;
    }

    @Override
    public UnaryFunction<BigDecimal> invert() {
        return BigMath.INVERT;
    }

    @Override
    public UnaryFunction<BigDecimal> log() {
        return BigMath.LOG;
    }

    @Override
    public UnaryFunction<BigDecimal> log10() {
        return BigMath.LOG10;
    }

    @Override
    public UnaryFunction<BigDecimal> log1p() {
        return BigMath.LOG1P;
    }

    @Override
    public UnaryFunction<BigDecimal> logistic() {
        return BigMath.LOGISTIC;
    }

    @Override
    public UnaryFunction<BigDecimal> logit() {
        return BigMath.LOGIT;
    }

    @Override
    public BinaryFunction<BigDecimal> max() {
        return BigMath.MAX;
    }

    @Override
    public BinaryFunction<BigDecimal> min() {
        return BigMath.MIN;
    }

    @Override
    public BinaryFunction<BigDecimal> multiply() {
        return BigMath.MULTIPLY;
    }

    @Override
    public UnaryFunction<BigDecimal> negate() {
        return BigMath.NEGATE;
    }

    @Override
    public BinaryFunction<BigDecimal> pow() {
        return BigMath.POW;
    }

    @Override
    public ParameterFunction<BigDecimal> power() {
        return BigMath.POWER;
    }

    @Override
    public UnaryFunction<BigDecimal> rint() {
        return BigMath.RINT;
    }

    @Override
    public ParameterFunction<BigDecimal> root() {
        return BigMath.ROOT;
    }

    @Override
    public ParameterFunction<BigDecimal> scale() {
        return BigMath.SCALE;
    }

    @Override
    public UnaryFunction<BigDecimal> signum() {
        return BigMath.SIGNUM;
    }

    @Override
    public UnaryFunction<BigDecimal> sin() {
        return BigMath.SIN;
    }

    @Override
    public UnaryFunction<BigDecimal> sinh() {
        return BigMath.SINH;
    }

    @Override
    public UnaryFunction<BigDecimal> sqrt() {
        return BigMath.SQRT;
    }

    @Override
    public UnaryFunction<BigDecimal> sqrt1px2() {
        return BigMath.SQRT1PX2;
    }

    @Override
    public BinaryFunction<BigDecimal> subtract() {
        return BigMath.SUBTRACT;
    }

    @Override
    public UnaryFunction<BigDecimal> tan() {
        return BigMath.TAN;
    }

    @Override
    public UnaryFunction<BigDecimal> tanh() {
        return BigMath.TANH;
    }

    @Override
    public UnaryFunction<BigDecimal> value() {
        return BigMath.VALUE;
    }

}

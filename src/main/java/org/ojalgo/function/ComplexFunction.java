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
package org.ojalgo.function;

import org.ojalgo.function.aggregator.AggregatorSet;
import org.ojalgo.function.aggregator.ComplexAggregator;
import org.ojalgo.function.constant.ComplexMath;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.type.context.NumberContext;

public final class ComplexFunction extends FunctionSet<ComplexNumber> {

    @FunctionalInterface
    public interface Binary extends BinaryFunction<ComplexNumber> {

        default double invoke(final double arg1, final double arg2) {
            return this.invoke(ComplexNumber.valueOf(arg1), ComplexNumber.valueOf(arg2)).doubleValue();
        }

        default ComplexNumber invoke(final ComplexNumber arg1, final double arg2) {
            return this.invoke(arg1, ComplexNumber.valueOf(arg2));
        }

        default float invoke(final float arg1, final float arg2) {
            return this.invoke(ComplexNumber.valueOf(arg1), ComplexNumber.valueOf(arg2)).floatValue();
        }

    }

    @FunctionalInterface
    public interface Consumer extends VoidFunction<ComplexNumber> {

        default void invoke(final double arg) {
            this.invoke(ComplexNumber.valueOf(arg));
        }

        default void invoke(final float arg) {
            this.invoke(ComplexNumber.valueOf(arg));
        }

    }

    @FunctionalInterface
    public interface Parameter extends ParameterFunction<ComplexNumber> {

        default double invoke(final double arg, final int param) {
            return this.invoke(ComplexNumber.valueOf(arg), param).doubleValue();
        }

        default float invoke(final float arg, final int param) {
            return this.invoke(ComplexNumber.valueOf(arg), param).floatValue();
        }

    }

    @FunctionalInterface
    public interface Predicate extends PredicateFunction<ComplexNumber> {

        default boolean invoke(final double arg) {
            return this.invoke(ComplexNumber.valueOf(arg));
        }

        default boolean invoke(final float arg) {
            return this.invoke(ComplexNumber.valueOf(arg));
        }

    }

    @FunctionalInterface
    public interface Unary extends UnaryFunction<ComplexNumber> {

        default double invoke(final double arg) {
            return this.invoke(ComplexNumber.valueOf(arg)).doubleValue();
        }

        default float invoke(final float arg) {
            return this.invoke(ComplexNumber.valueOf(arg)).floatValue();
        }

    }

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

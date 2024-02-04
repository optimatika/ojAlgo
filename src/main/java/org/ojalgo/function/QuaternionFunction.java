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
import org.ojalgo.function.aggregator.QuaternionAggregator;
import org.ojalgo.function.constant.QuaternionMath;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.type.context.NumberContext;

public final class QuaternionFunction extends FunctionSet<Quaternion> {

    @FunctionalInterface
    public interface Binary extends BinaryFunction<Quaternion> {

        default double invoke(final double arg1, final double arg2) {
            return this.invoke(Quaternion.valueOf(arg1), Quaternion.valueOf(arg2)).doubleValue();
        }

        default Quaternion invoke(final Quaternion arg1, final double arg2) {
            return this.invoke(arg1, Quaternion.valueOf(arg2));
        }

        default float invoke(final float arg1, final float arg2) {
            return this.invoke(Quaternion.valueOf(arg1), Quaternion.valueOf(arg2)).floatValue();
        }

    }

    @FunctionalInterface
    public interface Consumer extends VoidFunction<Quaternion> {

        default void invoke(final double arg) {
            this.invoke(Quaternion.valueOf(arg));
        }

        default void invoke(final float arg) {
            this.invoke(Quaternion.valueOf(arg));
        }

    }

    @FunctionalInterface
    public interface Parameter extends ParameterFunction<Quaternion> {

        default double invoke(final double arg, final int param) {
            return this.invoke(Quaternion.valueOf(arg), param).doubleValue();
        }

        default float invoke(final float arg, final int param) {
            return this.invoke(Quaternion.valueOf(arg), param).floatValue();
        }

    }

    @FunctionalInterface
    public interface Predicate extends PredicateFunction<Quaternion> {

        default boolean invoke(final double arg) {
            return this.invoke(Quaternion.valueOf(arg));
        }

        default boolean invoke(final float arg) {
            return this.invoke(Quaternion.valueOf(arg));
        }

    }

    @FunctionalInterface
    public interface Unary extends UnaryFunction<Quaternion> {

        default double invoke(final double arg) {
            return this.invoke(Quaternion.valueOf(arg)).doubleValue();
        }

        default float invoke(final float arg) {
            return this.invoke(Quaternion.valueOf(arg)).floatValue();
        }

    }

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

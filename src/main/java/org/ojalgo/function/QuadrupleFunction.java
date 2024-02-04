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
import org.ojalgo.function.aggregator.QuadrupleAggregator;
import org.ojalgo.function.constant.QuadrupleMath;
import org.ojalgo.scalar.Quadruple;
import org.ojalgo.type.context.NumberContext;

/**
 * RationalFunction
 *
 * @author apete
 */
public final class QuadrupleFunction extends FunctionSet<Quadruple> {

    @FunctionalInterface
    public interface Binary extends BinaryFunction<Quadruple> {

        default Quadruple invoke(final Quadruple arg1, final double arg2) {
            return this.invoke(arg1, Quadruple.valueOf(arg2));
        }

        default double invoke(final double arg1, final double arg2) {
            return this.invoke(Quadruple.valueOf(arg1), Quadruple.valueOf(arg2)).doubleValue();
        }

        default float invoke(final float arg1, final float arg2) {
            return this.invoke(Quadruple.valueOf(arg1), Quadruple.valueOf(arg2)).floatValue();
        }

    }

    @FunctionalInterface
    public interface Consumer extends VoidFunction<Quadruple> {

        default void invoke(final double arg) {
            this.invoke(Quadruple.valueOf(arg));
        }

        default void invoke(final float arg) {
            this.invoke(Quadruple.valueOf(arg));
        }

    }

    @FunctionalInterface
    public interface Parameter extends ParameterFunction<Quadruple> {

        default double invoke(final double arg, final int param) {
            return this.invoke(Quadruple.valueOf(arg), param).doubleValue();
        }

        default float invoke(final float arg, final int param) {
            return this.invoke(Quadruple.valueOf(arg), param).floatValue();
        }

    }

    @FunctionalInterface
    public interface Predicate extends PredicateFunction<Quadruple> {

        default boolean invoke(final double arg) {
            return this.invoke(Quadruple.valueOf(arg));
        }

        default boolean invoke(final float arg) {
            return this.invoke(Quadruple.valueOf(arg));
        }

    }

    @FunctionalInterface
    public interface Unary extends UnaryFunction<Quadruple> {

        default double invoke(final double arg) {
            return this.invoke(Quadruple.valueOf(arg)).doubleValue();
        }

        default float invoke(final float arg) {
            return this.invoke(Quadruple.valueOf(arg)).floatValue();
        }

    }

    private static final QuadrupleFunction SET = new QuadrupleFunction();

    public static QuadrupleFunction getSet() {
        return SET;
    }

    private QuadrupleFunction() {
        super();
    }

    @Override
    public UnaryFunction<Quadruple> abs() {
        return QuadrupleMath.ABS;
    }

    @Override
    public UnaryFunction<Quadruple> acos() {
        return QuadrupleMath.ACOS;
    }

    @Override
    public UnaryFunction<Quadruple> acosh() {
        return QuadrupleMath.ACOSH;
    }

    @Override
    public BinaryFunction<Quadruple> add() {
        return QuadrupleMath.ADD;
    }

    @Override
    public AggregatorSet<Quadruple> aggregator() {
        return QuadrupleAggregator.getSet();
    }

    @Override
    public UnaryFunction<Quadruple> asin() {
        return QuadrupleMath.ASIN;
    }

    @Override
    public UnaryFunction<Quadruple> asinh() {
        return QuadrupleMath.ASINH;
    }

    @Override
    public UnaryFunction<Quadruple> atan() {
        return QuadrupleMath.ATAN;
    }

    @Override
    public BinaryFunction<Quadruple> atan2() {
        return QuadrupleMath.ATAN2;
    }

    @Override
    public UnaryFunction<Quadruple> atanh() {
        return QuadrupleMath.ATANH;
    }

    @Override
    public UnaryFunction<Quadruple> cardinality() {
        return QuadrupleMath.CARDINALITY;
    }

    @Override
    public UnaryFunction<Quadruple> cbrt() {
        return QuadrupleMath.CBRT;
    }

    @Override
    public UnaryFunction<Quadruple> ceil() {
        return QuadrupleMath.CEIL;
    }

    @Override
    public UnaryFunction<Quadruple> conjugate() {
        return QuadrupleMath.CONJUGATE;
    }

    @Override
    public UnaryFunction<Quadruple> cos() {
        return QuadrupleMath.COS;
    }

    @Override
    public UnaryFunction<Quadruple> cosh() {
        return QuadrupleMath.COSH;
    }

    @Override
    public BinaryFunction<Quadruple> divide() {
        return QuadrupleMath.DIVIDE;
    }

    @Override
    public Unary enforce(final NumberContext context) {
        return t -> Quadruple.valueOf(context.enforce(t));
    }

    @Override
    public UnaryFunction<Quadruple> exp() {
        return QuadrupleMath.EXP;
    }

    @Override
    public UnaryFunction<Quadruple> expm1() {
        return QuadrupleMath.EXPM1;
    }

    @Override
    public UnaryFunction<Quadruple> floor() {
        return QuadrupleMath.FLOOR;
    }

    @Override
    public BinaryFunction<Quadruple> hypot() {
        return QuadrupleMath.HYPOT;
    }

    @Override
    public UnaryFunction<Quadruple> invert() {
        return QuadrupleMath.INVERT;
    }

    @Override
    public UnaryFunction<Quadruple> log() {
        return QuadrupleMath.LOG;
    }

    @Override
    public UnaryFunction<Quadruple> log10() {
        return QuadrupleMath.LOG10;
    }

    @Override
    public UnaryFunction<Quadruple> log1p() {
        return QuadrupleMath.LOG1P;
    }

    @Override
    public UnaryFunction<Quadruple> logistic() {
        return QuadrupleMath.LOGISTIC;
    }

    @Override
    public UnaryFunction<Quadruple> logit() {
        return QuadrupleMath.LOGIT;
    }

    @Override
    public BinaryFunction<Quadruple> max() {
        return QuadrupleMath.MAX;
    }

    @Override
    public BinaryFunction<Quadruple> min() {
        return QuadrupleMath.MIN;
    }

    @Override
    public BinaryFunction<Quadruple> multiply() {
        return QuadrupleMath.MULTIPLY;
    }

    @Override
    public UnaryFunction<Quadruple> negate() {
        return QuadrupleMath.NEGATE;
    }

    @Override
    public BinaryFunction<Quadruple> pow() {
        return QuadrupleMath.POW;
    }

    @Override
    public ParameterFunction<Quadruple> power() {
        return QuadrupleMath.POWER;
    }

    @Override
    public UnaryFunction<Quadruple> rint() {
        return QuadrupleMath.RINT;
    }

    @Override
    public ParameterFunction<Quadruple> root() {
        return QuadrupleMath.ROOT;
    }

    @Override
    public ParameterFunction<Quadruple> scale() {
        return QuadrupleMath.SCALE;
    }

    @Override
    public UnaryFunction<Quadruple> signum() {
        return QuadrupleMath.SIGNUM;
    }

    @Override
    public UnaryFunction<Quadruple> sin() {
        return QuadrupleMath.SIN;
    }

    @Override
    public UnaryFunction<Quadruple> sinh() {
        return QuadrupleMath.SINH;
    }

    @Override
    public UnaryFunction<Quadruple> sqrt() {
        return QuadrupleMath.SQRT;
    }

    @Override
    public UnaryFunction<Quadruple> sqrt1px2() {
        return QuadrupleMath.SQRT1PX2;
    }

    @Override
    public BinaryFunction<Quadruple> subtract() {
        return QuadrupleMath.SUBTRACT;
    }

    @Override
    public UnaryFunction<Quadruple> tan() {
        return QuadrupleMath.TAN;
    }

    @Override
    public UnaryFunction<Quadruple> tanh() {
        return QuadrupleMath.TANH;
    }

    @Override
    public UnaryFunction<Quadruple> value() {
        return QuadrupleMath.VALUE;
    }

}

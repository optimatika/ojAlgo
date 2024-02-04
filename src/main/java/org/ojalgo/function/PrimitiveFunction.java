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
import org.ojalgo.function.aggregator.PrimitiveAggregator;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.function.special.PowerOf2;
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
    public interface Binary extends BinaryFunction<Double> {

        @Override
        default Double invoke(final Double arg1, final double arg2) {
            return Double.valueOf(this.invoke(arg1.doubleValue(), arg2));
        }

        @Override
        default Double invoke(final Double arg1, final Double arg2) {
            return Double.valueOf(this.invoke(arg1.doubleValue(), arg2.doubleValue()));
        }

    }

    @FunctionalInterface
    public interface Consumer extends VoidFunction<Double> {

        @Override
        default void invoke(final Double arg) {
            this.invoke(arg.doubleValue());
        }

    }

    @FunctionalInterface
    public interface Parameter extends ParameterFunction<Double> {

        @Override
        default Double invoke(final Double arg, final int param) {
            return Double.valueOf(this.invoke(arg.doubleValue(), param));
        }

    }

    @FunctionalInterface
    public interface Predicate extends PredicateFunction<Double> {

        @Override
        default boolean invoke(final Double arg) {
            return this.invoke(arg.doubleValue());
        }

    }

    public static final class SampleDomain {

        private final double myIncrement;
        private final double myPeriod;
        private final int myNumberOfSamples;

        public SampleDomain(final double period, final int nbSamples) {
            super();
            myPeriod = period;
            myNumberOfSamples = nbSamples;
            myIncrement = period / nbSamples;
        }

        /**
         * Adjusts the number of samples to the smallest power of 2 that is not less than the current number
         * of samples.
         */
        public SampleDomain adjustToPowerOf2() {
            return new SampleDomain(myPeriod, PowerOf2.smallestNotLessThan(myNumberOfSamples));
        }

        public double argumant(final int index) {
            return index * myIncrement;
        }

        public double[] arguments() {
            double[] retVal = new double[myNumberOfSamples];
            for (int i = 0; i < myNumberOfSamples; i++) {
                retVal[i] = i * myIncrement;
            }
            return retVal;
        }

        public double increment() {
            return myIncrement;
        }

        public double period() {
            return myPeriod;
        }

        public int size() {
            return myNumberOfSamples;
        }

    }

    @FunctionalInterface
    public interface Unary extends UnaryFunction<Double> {

        @Override
        default Double invoke(final Double arg) {
            return Double.valueOf(this.invoke(arg.doubleValue()));
        }

    }

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

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
import org.ojalgo.type.context.NumberContext;

/**
 * A predefined/standard set of functions.
 *
 * @author apete
 */
public abstract class FunctionSet<N extends Comparable<N>> {

    protected FunctionSet() {
        super();
    }

    /**
     * @see Math#abs(double)
     */
    public abstract UnaryFunction<N> abs();

    /**
     * @see Math#acos(double)
     */
    public abstract UnaryFunction<N> acos();

    public abstract UnaryFunction<N> acosh();

    /**
     * +
     */
    public abstract BinaryFunction<N> add();

    public abstract AggregatorSet<N> aggregator();

    /**
     * @see Math#asin(double)
     */
    public abstract UnaryFunction<N> asin();

    public abstract UnaryFunction<N> asinh();

    /**
     * @see Math#atan(double)
     */
    public abstract UnaryFunction<N> atan();

    /**
     * @see Math#atan2(double,double)
     */
    public abstract BinaryFunction<N> atan2();

    public abstract UnaryFunction<N> atanh();

    public abstract UnaryFunction<N> cardinality();

    /**
     * @see Math#cbrt(double)
     */
    public abstract UnaryFunction<N> cbrt();

    /**
     * @see Math#ceil(double)
     */
    public abstract UnaryFunction<N> ceil();

    public abstract UnaryFunction<N> conjugate();

    /**
     * @see Math#cos(double)
     */
    public abstract UnaryFunction<N> cos();

    /**
     * @see Math#cosh(double)
     */
    public abstract UnaryFunction<N> cosh();

    /**
     * /
     */
    public abstract BinaryFunction<N> divide();

    public abstract UnaryFunction<N> enforce(NumberContext context);

    /**
     * @see Math#exp(double)
     */
    public abstract UnaryFunction<N> exp();

    /**
     * @see Math#expm1(double)
     */
    public abstract UnaryFunction<N> expm1();

    /**
     * @see Math#floor(double)
     */
    public abstract UnaryFunction<N> floor();

    /**
     * @see Math#hypot(double, double)
     */
    public abstract BinaryFunction<N> hypot();

    public abstract UnaryFunction<N> invert();

    /**
     * @see Math#log(double)
     */
    public abstract UnaryFunction<N> log();

    /**
     * @see Math#log10(double)
     */
    public abstract UnaryFunction<N> log10();

    /**
     * @see Math#log1p(double)
     */
    public abstract UnaryFunction<N> log1p();

    /**
     * Standard logistic sigmoid function
     */
    public abstract UnaryFunction<N> logistic();

    public abstract UnaryFunction<N> logit();

    /**
     * @see Math#max(double, double)
     */
    public abstract BinaryFunction<N> max();

    /**
     * @see Math#min(double, double)
     */
    public abstract BinaryFunction<N> min();

    /**
     * *
     */
    public abstract BinaryFunction<N> multiply();

    public abstract UnaryFunction<N> negate();

    /**
     * @see Math#pow(double, double)
     */
    public abstract BinaryFunction<N> pow();

    public abstract ParameterFunction<N> power();

    /**
     * @see Math#rint(double)
     */
    public abstract UnaryFunction<N> rint();

    public abstract ParameterFunction<N> root();

    public abstract ParameterFunction<N> scale();

    /**
     * @see Math#signum(double)
     */
    public abstract UnaryFunction<N> signum();

    /**
     * @see Math#sin(double)
     */
    public abstract UnaryFunction<N> sin();

    /**
     * @see Math#sinh(double)
     */
    public abstract UnaryFunction<N> sinh();

    /**
     * @see Math#sqrt(double)
     */
    public abstract UnaryFunction<N> sqrt();

    /**
     * @return sqrt(1.0 + x<sup>2</sup>)
     */
    public abstract UnaryFunction<N> sqrt1px2();

    /**
     * -
     */
    public abstract BinaryFunction<N> subtract();

    /**
     * @see Math#tan(double)
     */
    public abstract UnaryFunction<N> tan();

    /**
     * @see Math#tanh(double)
     */
    public abstract UnaryFunction<N> tanh();

    public abstract UnaryFunction<N> value();

}

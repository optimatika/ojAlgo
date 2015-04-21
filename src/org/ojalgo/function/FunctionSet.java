/*
 * Copyright 1997-2015 Optimatika (www.optimatika.se)
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

/**
 * A predefined/standard set of functions.
 *
 * @author apete
 */
public abstract class FunctionSet<N extends Number> {

    protected FunctionSet() {
        super();
    }

    /**
     * @see StrictMath#abs(double)
     */
    public abstract UnaryFunction<N> abs();

    /**
     * @see StrictMath#acos(double)
     */
    public abstract UnaryFunction<N> acos();

    public abstract UnaryFunction<N> acosh();

    /**
     * +
     */
    public abstract BinaryFunction<N> add();

    /**
     * @see StrictMath#asin(double)
     */
    public abstract UnaryFunction<N> asin();

    public abstract UnaryFunction<N> asinh();

    /**
     * @see StrictMath#atan(double)
     */
    public abstract UnaryFunction<N> atan();

    public abstract UnaryFunction<N> atanh();

    public abstract UnaryFunction<N> cardinality();

    public abstract UnaryFunction<N> conjugate();

    /**
     * @see StrictMath#cos(double)
     */
    public abstract UnaryFunction<N> cos();

    /**
     * @see StrictMath#cosh(double)
     */
    public abstract UnaryFunction<N> cosh();

    /**
     * /
     */
    public abstract BinaryFunction<N> divide();

    /**
     * @see StrictMath#exp(double)
     */
    public abstract UnaryFunction<N> exp();

    /**
     * @see StrictMath#expm1(double)
     */
    public abstract UnaryFunction<N> expm1();

    /**
     * @see StrictMath#hypot(double, double)
     */
    public abstract BinaryFunction<N> hypot();

    public abstract UnaryFunction<N> invert();

    /**
     * @see StrictMath#log(double)
     */
    public abstract UnaryFunction<N> log();

    /**
     * @see StrictMath#log10(double)
     */
    public abstract UnaryFunction<N> log10();

    /**
     * @see StrictMath#log1p(double)
     */
    public abstract UnaryFunction<N> log1p();

    /**
     * @see StrictMath#max(double, double)
     */
    public abstract BinaryFunction<N> max();

    /**
     * @see StrictMath#min(double, double)
     */
    public abstract BinaryFunction<N> min();

    /**
     * *
     */
    public abstract BinaryFunction<N> multiply();

    public abstract UnaryFunction<N> negate();

    /**
     * @see StrictMath#pow(double, double)
     */
    public abstract BinaryFunction<N> pow();

    public abstract ParameterFunction<N> power();

    public abstract ParameterFunction<N> root();

    public abstract ParameterFunction<N> scale();

    /**
     * @see StrictMath#signum(double)
     */
    public abstract UnaryFunction<N> signum();

    /**
     * @see StrictMath#sin(double)
     */
    public abstract UnaryFunction<N> sin();

    /**
     * @see StrictMath#sinh(double)
     */
    public abstract UnaryFunction<N> sinh();

    /**
     * @see StrictMath#sqrt(double)
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
     * @see StrictMath#tan(double)
     */
    public abstract UnaryFunction<N> tan();

    /**
     * @see StrictMath#tanh(double)
     */
    public abstract UnaryFunction<N> tanh();

    public abstract UnaryFunction<N> value();

}

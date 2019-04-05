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

import java.util.function.BinaryOperator;
import java.util.function.DoubleBinaryOperator;

import org.ojalgo.ProgrammingError;

public interface BinaryFunction<N extends Number> extends BasicFunction, BinaryOperator<N>, DoubleBinaryOperator {

    /**
     * A {@linkplain BinaryFunction} with a set/fixed first argument.
     *
     * @author apete
     */
    public static final class FixedFirst<N extends Number> implements UnaryFunction<N> {

        private final BinaryFunction<N> myFunction;
        private final N myNumber;
        private final double myValue;

        @SuppressWarnings("unchecked")
        FixedFirst(final double arg1, final BinaryFunction<N> function) {

            super();

            myFunction = function;

            myNumber = (N) Double.valueOf(arg1);
            myValue = arg1;
        }

        FixedFirst(final N arg1, final BinaryFunction<N> function) {

            super();

            myFunction = function;

            myNumber = arg1;
            myValue = arg1.doubleValue();
        }

        public final double doubleValue() {
            return myValue;
        }

        public final BinaryFunction<N> getFunction() {
            return myFunction;
        }

        public final N getNumber() {
            return myNumber;
        }

        public final double invoke(final double arg2) {
            return myFunction.invoke(myValue, arg2);
        }

        public final N invoke(final N arg2) {
            return myFunction.invoke(myNumber, arg2);
        }

    }

    /**
     * A {@linkplain BinaryFunction} with a set/fixed second argument.
     *
     * @author apete
     */
    public static final class FixedSecond<N extends Number> implements UnaryFunction<N> {

        private final BinaryFunction<N> myFunction;
        private final N myNumber;
        private final double myValue;

        @SuppressWarnings("unchecked")
        FixedSecond(final BinaryFunction<N> function, final double arg2) {

            super();

            myFunction = function;

            myNumber = (N) Double.valueOf(arg2);
            myValue = arg2;
        }

        FixedSecond(final BinaryFunction<N> function, final N arg2) {

            super();

            myFunction = function;

            myNumber = arg2;
            myValue = arg2.doubleValue();
        }

        public final double doubleValue() {
            return myValue;
        }

        public final BinaryFunction<N> getFunction() {
            return myFunction;
        }

        public final N getNumber() {
            return myNumber;
        }

        public final double invoke(final double arg1) {
            return myFunction.invoke(arg1, myValue);
        }

        public final N invoke(final N arg1) {
            return myFunction.invoke(arg1, myNumber);
        }

    }

    public abstract double invoke(double arg1, double arg2);

    public abstract N invoke(N arg1, N arg2);

    default BinaryFunction<N> andThen(final UnaryFunction<N> after) {
        ProgrammingError.throwIfNull(after);
        return new BinaryFunction<N>() {

            public double invoke(final double arg1, final double arg2) {
                return after.invoke(BinaryFunction.this.invoke(arg1, arg2));
            }

            public N invoke(final N arg1, final N arg2) {
                return after.invoke(BinaryFunction.this.invoke(arg1, arg2));
            }

        };
    }

    /**
     * @see java.util.function.BiFunction#apply(java.lang.Object, java.lang.Object)
     */
    default N apply(final N arg1, final N arg2) {
        return this.invoke(arg1, arg2);
    }

    /**
     * @see java.util.function.DoubleBinaryOperator#applyAsDouble(double, double)
     */
    default double applyAsDouble(final double arg1, final double arg2) {
        return this.invoke(arg1, arg2);
    }

    /**
     * To allow syntax like <code>array.modifyAll(DIVIDE.by(3.0));</code>
     *
     * @see #second(double)
     */
    default UnaryFunction<N> by(final double arg2) {
        return this.second(arg2);
    }

    /**
     * @see #first(Number)
     */
    default UnaryFunction<N> first(final double arg1) {
        return new FixedFirst<>(arg1, this);
    }

    /**
     * Turns this binary function into a unary function with the first argument fixed/locked to the specified
     * value.
     *
     * @param arg1 The first argument of the binary function.
     * @return The resulting unary function.
     */
    default UnaryFunction<N> first(final N arg1) {
        return new FixedFirst<>(arg1, this);
    }

    /**
     * @see #second(Number)
     */
    default UnaryFunction<N> second(final double arg2) {
        return new FixedSecond<>(this, arg2);
    }

    /**
     * Turns this binary function into a unary function with the second argument fixed/locked to the specified
     * value.
     *
     * @param arg2 The second argument of the binary function.
     * @return The resulting unary function.
     */
    default UnaryFunction<N> second(final N arg2) {
        return new FixedSecond<>(this, arg2);
    }

}

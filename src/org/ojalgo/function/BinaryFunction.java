/*
 * Copyright 1997-2014 Optimatika (www.optimatika.se)
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

public abstract class BinaryFunction<N extends Number> implements Function<N> {

    /**
     * A {@linkplain BinaryFunction} with a set/fixed first argument.
     *
     * @author apete
     */
    public static final class FixedFirst<N extends Number> implements UnaryFunction<N> {

        private final BinaryFunction<N> myFunction;
        private final N myNumber;
        private final double myValue;

        @SuppressWarnings("unused")
        private FixedFirst() {
            this(null, null);
        }

        @SuppressWarnings("unchecked")
        FixedFirst(final double aFirstArg, final BinaryFunction<N> aFunc) {

            super();

            myFunction = aFunc;

            myNumber = (N) Double.valueOf(aFirstArg);
            myValue = aFirstArg;
        }

        FixedFirst(final N aFirstArg, final BinaryFunction<N> aFunc) {

            super();

            myFunction = aFunc;

            myNumber = aFirstArg;
            myValue = aFirstArg.doubleValue();
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

        public final double invoke(final double aSecondArg) {
            return myFunction.invoke(myValue, aSecondArg);
        }

        public final N invoke(final N aSecondArg) {
            return myFunction.invoke(myNumber, aSecondArg);
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

        @SuppressWarnings("unused")
        private FixedSecond() {
            this(null, null);
        }

        @SuppressWarnings("unchecked")
        FixedSecond(final BinaryFunction<N> aFunc, final double aSecondArg) {

            super();

            myFunction = aFunc;

            myNumber = (N) Double.valueOf(aSecondArg);
            myValue = aSecondArg;
        }

        FixedSecond(final BinaryFunction<N> aFunc, final N aSecondArg) {

            super();

            myFunction = aFunc;

            myNumber = aSecondArg;
            myValue = aSecondArg.doubleValue();
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

        public final double invoke(final double aFirstArg) {
            return myFunction.invoke(aFirstArg, myValue);
        }

        public final N invoke(final N aFirstArg) {
            return myFunction.invoke(aFirstArg, myNumber);
        }

    }

    protected BinaryFunction() {
        super();
    }

    public final UnaryFunction<N> first(final double arg1) {
        return new FixedFirst<N>(arg1, this);
    }

    public final UnaryFunction<N> first(final N arg1) {
        return new FixedFirst<N>(arg1, this);
    }

    public abstract double invoke(double arg1, double arg2);

    public abstract N invoke(N arg1, N arg2);

    public final UnaryFunction<N> second(final double arg2) {
        return new FixedSecond<N>(this, arg2);
    }

    public final UnaryFunction<N> second(final N arg2) {
        return new FixedSecond<N>(this, arg2);
    }

}

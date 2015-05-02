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

import java.util.function.BiFunction;

public interface ParameterFunction<N extends Number> extends BasicFunction<N>, BiFunction<N, Integer, N> {

    /**
     * A {@linkplain ParameterFunction} with a set/fixed parameter.
     *
     * @author apete
     */
    public static final class FixedParameter<N extends Number> implements UnaryFunction<N> {

        private final ParameterFunction<N> myFunction;
        private final int myParameter;

        @SuppressWarnings("unused")
        private FixedParameter() {
            this(null, 0);
        }

        FixedParameter(final ParameterFunction<N> function, final int param) {

            super();

            myFunction = function;
            myParameter = param;
        }

        public final ParameterFunction<N> getFunction() {
            return myFunction;
        }

        public final int getParameter() {
            return myParameter;
        }

        public final double invoke(final double arg) {
            return myFunction.invoke(arg, myParameter);
        }

        public final N invoke(final N arg) {
            return myFunction.invoke(arg, myParameter);
        }

    }

    public abstract double invoke(double arg, int param);

    public abstract N invoke(N arg, int param);

    default N apply(final N arg, final Integer param) {
        return this.invoke(arg, param);
    }

    /**
     * Turns this parameter function into a unary function with the parameter fixed/locked to the specified
     * value.
     *
     * @param param The parameter of the parameter function.
     * @return The resulting unary function.
     */
    default UnaryFunction<N> parameter(final int param) {
        return new FixedParameter<N>(this, param);
    }

}

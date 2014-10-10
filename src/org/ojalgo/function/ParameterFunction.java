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

public abstract class ParameterFunction<N extends Number> implements BasicFunction<N> {

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

        FixedParameter(final ParameterFunction<N> aFunc, final int aParam) {

            super();

            myFunction = aFunc;
            myParameter = aParam;
        }

        public final ParameterFunction<N> getFunction() {
            return myFunction;
        }

        public final int getParameter() {
            return myParameter;
        }

        public final double invoke(final double aFirstArg) {
            return myFunction.invoke(aFirstArg, myParameter);
        }

        public final N invoke(final N aFirstArg) {
            return myFunction.invoke(aFirstArg, myParameter);
        }

    }

    protected ParameterFunction() {
        super();
    }

    public abstract double invoke(double arg, int param);

    public abstract N invoke(N arg, int param);

    public final UnaryFunction<N> parameter(final int param) {
        return new FixedParameter<N>(this, param);
    }

}

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

import java.util.function.DoubleUnaryOperator;
import java.util.function.UnaryOperator;

import org.ojalgo.ProgrammingError;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.scalar.PrimitiveScalar;

public interface UnaryFunction<N extends Comparable<N>> extends BasicFunction, UnaryOperator<N>, DoubleUnaryOperator {

    static <N extends Comparable<N>> boolean isZeroModified(final UnaryFunction<N> function) {
        return !PrimitiveScalar.isSmall(PrimitiveMath.ONE, function.invoke(PrimitiveMath.ZERO));
    }

    default UnaryFunction<N> andThen(final UnaryFunction<N> after) {
        ProgrammingError.throwIfNull(after);
        return new UnaryFunction<N>() {

            public double invoke(final double arg) {
                return after.invoke(UnaryFunction.this.invoke(arg));
            }

            public float invoke(final float arg) {
                return after.invoke(UnaryFunction.this.invoke(arg));
            }

            public N invoke(final N arg) {
                return after.invoke(UnaryFunction.this.invoke(arg));
            }

        };
    }

    default N apply(final N arg) {
        return this.invoke(arg);
    }

    default double applyAsDouble(final double arg) {
        return this.invoke(arg);
    }

    default UnaryFunction<N> compose(final UnaryFunction<N> before) {
        ProgrammingError.throwIfNull(before);
        return new UnaryFunction<N>() {

            public double invoke(final double arg) {
                return UnaryFunction.this.invoke(before.invoke(arg));
            }

            public N invoke(final N arg) {
                return UnaryFunction.this.invoke(before.invoke(arg));
            }

            public float invoke(float arg) {
                return UnaryFunction.this.invoke(before.invoke(arg));
            }

        };
    }

    double invoke(double arg);

    float invoke(float arg);

    N invoke(N arg);

}

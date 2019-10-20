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

import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

import org.ojalgo.ProgrammingError;
import org.ojalgo.structure.AccessScalar;

public interface NullaryFunction<N extends Comparable<N>> extends BasicFunction, Supplier<N>, DoubleSupplier, AccessScalar<N> {

    default NullaryFunction<N> andThen(final UnaryFunction<N> after) {
        ProgrammingError.throwIfNull(after);
        return new NullaryFunction<N>() {

            public double doubleValue() {
                return after.invoke(NullaryFunction.this.doubleValue());
            }

            public N invoke() {
                return after.invoke(NullaryFunction.this.invoke());
            }

        };
    }

    double doubleValue();

    default N get() {
        return this.invoke();
    }

    default double getAsDouble() {
        return this.doubleValue();
    }

    N invoke();

}

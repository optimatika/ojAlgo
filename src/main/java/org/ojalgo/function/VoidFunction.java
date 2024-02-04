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

import java.util.function.Consumer;
import java.util.function.DoubleConsumer;

import org.ojalgo.ProgrammingError;

public interface VoidFunction<N extends Comparable<N>> extends BasicFunction, Consumer<N>, DoubleConsumer {

    default void accept(final double arg) {
        this.invoke(arg);
    }

    default void accept(final N arg) {
        this.invoke(arg);
    }

    default VoidFunction<N> compose(final UnaryFunction<N> before) {
        ProgrammingError.throwIfNull(before);
        return new VoidFunction<N>() {

            public void invoke(final double arg) {
                VoidFunction.this.invoke(before.invoke(arg));
            }

            public void invoke(final float arg) {
                VoidFunction.this.invoke(before.invoke(arg));
            }

            public void invoke(final N arg) {
                VoidFunction.this.invoke(before.invoke(arg));
            }

        };
    }

    default void invoke(final byte arg) {
        this.invoke((double) arg);
    }

    void invoke(double arg);

    default void invoke(final float arg) {
        this.invoke((double) arg);
    }

    default void invoke(final int arg) {
        this.invoke((double) arg);
    }

    default void invoke(final long arg) {
        this.invoke((double) arg);
    }

    void invoke(N arg);

    default void invoke(final short arg) {
        this.invoke((double) arg);
    }

}

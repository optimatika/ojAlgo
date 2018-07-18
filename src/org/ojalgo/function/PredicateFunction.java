/*
 * Copyright 1997-2018 Optimatika
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

import java.util.function.DoublePredicate;
import java.util.function.Predicate;

public interface PredicateFunction<N extends Number> extends BasicFunction<N>, Predicate<N>, DoublePredicate {

    boolean invoke(double arg);

    boolean invoke(N arg);

    default PredicateFunction<N> negate() {
        return new PredicateFunction<N>() {

            public boolean invoke(double arg) {
                return !PredicateFunction.this.invoke(arg);
            }

            public boolean invoke(N arg) {
                return !PredicateFunction.this.invoke(arg);
            }

        };
    }

    default boolean test(final double arg) {
        return this.invoke(arg);
    }

    default boolean test(final N arg) {
        return this.invoke(arg);
    }

}

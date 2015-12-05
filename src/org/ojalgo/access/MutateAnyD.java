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
package org.ojalgo.access;

import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.UnaryFunction;

/**
 * N-dimensional mutator methods
 *
 * @author apete
 */
public interface MutateAnyD extends StructureAnyD, Mutate1D {

    interface Fillable<N extends Number> extends MutateAnyD, Mutate1D.Fillable<N> {

        void fillOne(long[] reference, N value);

        void fillOne(long[] reference, NullaryFunction<N> supplier);

        default void fillRange(final long first, final long limit, final N value) {
            for (long i = first; i < limit; i++) {
                this.fillOne(i, value);
            }
        }

        default void fillRange(final long first, final long limit, final NullaryFunction<N> supplier) {
            for (long i = first; i < limit; i++) {
                this.fillOne(i, supplier);
            }
        }

    }

    interface Modifiable<N extends Number> extends MutateAnyD, Mutate1D.Modifiable<N> {

        void modifyOne(long[] reference, UnaryFunction<N> function);

        default void modifyRange(final long first, final long limit, final UnaryFunction<N> function) {
            for (long i = first; i < limit; i++) {
                this.modifyOne(i, function);
            }
        }

    }

    default void add(final long index, final double addend) {
        this.add(AccessUtils.reference(index, this.shape()), addend);
    }

    default void add(final long index, final Number addend) {
        this.add(AccessUtils.reference(index, this.shape()), addend);
    }

    void add(long[] reference, double addend);

    void add(long[] reference, Number addend);

    default void set(final long index, final double value) {
        this.set(AccessUtils.reference(index, this.shape()), value);
    }

    default void set(final long index, final Number value) {
        this.set(AccessUtils.reference(index, this.shape()), value);
    }

    void set(long[] reference, double value);

    void set(long[] reference, Number value);

}

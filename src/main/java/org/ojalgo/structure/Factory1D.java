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
package org.ojalgo.structure;

import java.util.List;

import org.ojalgo.function.NullaryFunction;

public interface Factory1D<I extends Structure1D> extends FactorySupplement {

    /**
     * Should only be implemented by factories that always (or primarily) produce dense structures.
     *
     * @author apete
     */
    interface Dense<I extends Structure1D> extends Factory1D<I> {

        I copy(Access1D<?> source);

        I copy(Comparable<?>[] source);

        I copy(double... source);

        I copy(List<? extends Comparable<?>> source);

        I makeFilled(long count, NullaryFunction<?> supplier);

        default I makeFilled(final Structure1D shape, final NullaryFunction<?> supplier) {
            return this.makeFilled(shape.count(), supplier);
        }

    }

    /**
     * For when the structures can be either dense or sparse.
     *
     * @author apete
     */
    interface MayBeSparse<I extends Structure1D, DR extends Mutate1D.ModifiableReceiver<?>, SR extends Mutate1D.Receiver<?>> extends Factory1D<I> {

        DR makeDense(long count);

        default DR makeDense(final Structure1D shape) {
            return this.makeDense(shape.count());
        }

        SR makeSparse(long count);

        default SR makeSparse(final Structure1D shape) {
            return this.makeSparse(shape.count());
        }

    }

    default I make(final int count) {
        return this.make((long) count);
    }

    I make(long count);

    default I make(final Structure1D shape) {
        return this.make(shape.count());
    }

}

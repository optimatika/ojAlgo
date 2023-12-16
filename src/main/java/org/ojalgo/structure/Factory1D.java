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

    interface Builder<I extends Structure1D> extends Mutate1D {

        I build();

    }

    /**
     * For when the structures can be either dense or sparse.
     *
     * @author apete
     */
    interface MayBeSparse<I extends Structure1D, DENSE extends Builder<I>, SPARSE extends Builder<I>> extends TwoStep<I, DENSE> {

        /**
         * @deprecated v54 Use {@link #newDenseBuilder(long)} instead
         */
        @Deprecated
        default DENSE makeDense(final long count) {
            return this.newDenseBuilder(count);
        }

        /**
         * @deprecated v54 Use {@link #newDenseBuilder(Structure1D)} instead
         */
        @Deprecated
        default DENSE makeDense(final Structure1D shape) {
            return this.newDenseBuilder(shape.count());
        }

        /**
         * @deprecated v54 Use {@link #newSparseBuilder(long)} instead
         */
        @Deprecated
        default SPARSE makeSparse(final long count) {
            return this.newSparseBuilder(count);
        }

        /**
         * @deprecated v54 Use {@link #newSparseBuilder(Structure1D)} instead
         */
        @Deprecated
        default SPARSE makeSparse(final Structure1D shape) {
            return this.newSparseBuilder(shape.count());
        }

        @Override
        default DENSE newBuilder(final long count) {
            return this.newDenseBuilder(count);
        }

        DENSE newDenseBuilder(long count);

        SPARSE newSparseBuilder(long count);

    }

    public interface TwoStep<I extends Structure1D, B extends Builder<I>> extends Factory1D<I> {

        default I copy(final Access1D<?> source) {
            long count = source.count();
            B builder = this.newBuilder(count);
            for (long i = 0L; i < count; i++) {
                builder.set(i, source.get(i));
            }
            return builder.build();
        }

        default I copy(final Comparable<?>... source) {
            int length = source.length;
            B builder = this.newBuilder(length);
            for (int i = 0; i < length; i++) {
                builder.set(i, source[i]);
            }
            return builder.build();
        }

        default I copy(final double[] source) {
            int length = source.length;
            B builder = this.newBuilder(length);
            for (int i = 0; i < length; i++) {
                builder.set(i, source[i]);
            }
            return builder.build();
        }

        default I copy(final List<? extends Comparable<?>> source) {
            int size = source.size();
            B builder = this.newBuilder(size);
            for (int i = 0; i < size; i++) {
                builder.set(i, source.get(i));
            }
            return builder.build();
        }

        @Override
        default I make(final int size) {
            B builder = this.newBuilder(size);
            return builder.build();
        }

        @Override
        default I make(final long count) {
            B builder = this.newBuilder(count);
            return builder.build();

        }

        @Override
        default I make(final Structure1D shape) {
            B builder = this.newBuilder(shape.count());
            return builder.build();
        }

        default I makeFilled(final long count, final NullaryFunction<?> supplier) {
            B builder = this.newBuilder(count);
            for (long i = 0L; i < count; i++) {
                builder.set(i, supplier.get());
            }
            return builder.build();
        }

        /**
         * @deprecated v54 Use {@link #makeFilled(long, NullaryFunction)} instead
         */
        @Deprecated
        default I makeFilled(final Structure1D shape, final NullaryFunction<?> supplier) {
            return this.makeFilled(shape.count(), supplier);
        }

        B newBuilder(long count);

    }

    I make(int size);

    default I make(final long count) {
        return this.make(Math.toIntExact(count));
    }

    default I make(final Structure1D shape) {
        return this.make(shape.count());
    }

}

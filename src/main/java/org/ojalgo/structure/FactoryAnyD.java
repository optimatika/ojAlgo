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

import org.ojalgo.function.FunctionSet;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.scalar.Scalar.Factory;
import org.ojalgo.type.math.MathType;

public interface FactoryAnyD<I extends StructureAnyD> extends FactorySupplement {

    interface Builder<I extends StructureAnyD> extends MutateAnyD {

        I build();

    }

    /**
     * For when the structures can be either dense or sparse.
     *
     * @author apete
     */
    interface MayBeSparse<I extends StructureAnyD, DENSE extends Builder<I>, SPARSE extends Builder<I>> extends TwoStep<I, DENSE> {

        /**
         * @deprecated v54 Use {@link #newDenseBuilder(long...)} instead
         */
        @Deprecated
        default DENSE makeDense(final long... shape) {
            return this.newDenseBuilder(shape);
        }

        /**
         * @deprecated v54 Use {@link #newDenseBuilder(StructureAnyD)} instead
         */
        @Deprecated
        default DENSE makeDense(final StructureAnyD shape) {
            return this.newDenseBuilder(shape.shape());
        }

        /**
         * @deprecated v54 Use {@link #newSparseBuilder(long...)} instead
         */
        @Deprecated
        default SPARSE makeSparse(final long... shape) {
            return this.newSparseBuilder(shape);
        }

        /**
         * @deprecated v54 Use {@link #newSparseBuilder(StructureAnyD)} instead
         */
        @Deprecated
        default SPARSE makeSparse(final StructureAnyD shape) {
            return this.newSparseBuilder(shape.shape());
        }

        @Override
        default DENSE newBuilder(final long... shape) {
            return this.newDenseBuilder(shape);
        }

        DENSE newDenseBuilder(long... shape);

        SPARSE newSparseBuilder(long... shape);

    }

    public interface TwoStep<I extends StructureAnyD, B extends Builder<I>> extends FactoryAnyD<I> {

        default I copy(final AccessAnyD<?> source) {
            B builder = this.newBuilder(source.shape());
            for (long i = 0L, count = source.count(); i < count; i++) {
                builder.set(i, source.get(i));
            }
            return builder.build();
        }

        @Override
        default I make(final int... shape) {
            B builder = this.newBuilder(Structure1D.toLongIndexes(shape));
            return builder.build();
        }

        @Override
        default I make(final long... shape) {
            B builder = this.newBuilder(shape);
            return builder.build();

        }

        @Override
        default I make(final StructureAnyD shape) {
            B builder = this.newBuilder(shape.shape());
            return builder.build();
        }

        default I makeFilled(final long[] shape, final NullaryFunction<?> supplier) {
            B builder = this.newBuilder(shape);
            for (long i = 0L, count = StructureAnyD.count(shape); i < count; i++) {
                builder.set(i, supplier.get());
            }
            return builder.build();
        }

        /**
         * @deprecated v54 Use {@link #makeFilled(StructureAnyD, NullaryFunction)} instead
         */
        @Deprecated
        default I makeFilled(final StructureAnyD shape, final NullaryFunction<?> supplier) {
            return this.makeFilled(shape.shape(), supplier);
        }

        B newBuilder(long... shape);

    }

    default Factory1D<I> asFactory1D() {
        return new Factory1D<>() {

            public FunctionSet<?> function() {
                return FactoryAnyD.this.function();
            }

            public MathType getMathType() {
                return FactoryAnyD.this.getMathType();
            }

            public I make(final int size) {
                return FactoryAnyD.this.make(size);
            }

            public I make(final long count) {
                return FactoryAnyD.this.make(count);
            }

            public I make(final Structure1D shape) {
                return FactoryAnyD.this.make(shape.count());
            }

            public Factory<?> scalar() {
                return FactoryAnyD.this.scalar();
            }

        };
    }

    I make(int... shape);

    default I make(final long... shape) {
        return this.make(Structure1D.toIntIndexes(shape));
    }

    default I make(final StructureAnyD shape) {
        return this.make(shape.shape());
    }

}

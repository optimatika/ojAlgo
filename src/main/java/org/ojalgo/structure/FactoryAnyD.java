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

    /**
     * Should only be implemented by factories that always (or primarily) produce dense structures.
     *
     * @author apete
     */
    interface Dense<I extends StructureAnyD> extends FactoryAnyD<I> {

        I copy(AccessAnyD<?> source);

        I makeFilled(long[] structure, NullaryFunction<?> supplier);

        default I makeFilled(final StructureAnyD shape, final NullaryFunction<?> supplier) {
            return this.makeFilled(shape.shape(), supplier);
        }

    }

    /**
     * For when the structures can be either dense or sparse.
     *
     * @author apete
     */
    interface MayBeSparse<I extends StructureAnyD, DR extends MutateAnyD.ModifiableReceiver<?>, SR extends MutateAnyD.Receiver<?>> extends FactoryAnyD<I> {

        DR makeDense(long... structure);

        default DR makeDense(final StructureAnyD shape) {
            return this.makeDense(shape.shape());
        }

        SR makeSparse(long... structure);

        default SR makeSparse(final StructureAnyD shape) {
            return this.makeSparse(shape.shape());
        }

    }

    default Factory1D<I> asFactory1D() {
        return new Factory1D<>() {

            public FunctionSet<?> function() {
                return FactoryAnyD.this.function();
            }

            public I make(final long count) {
                return FactoryAnyD.this.make(count);
            }

            public Factory<?> scalar() {
                return FactoryAnyD.this.scalar();
            }

            public MathType getMathType() {
                return FactoryAnyD.this.getMathType();
            }

        };
    }

    default I make(final int... structure) {
        long[] shape = new long[structure.length];
        for (int i = 0; i < shape.length; i++) {
            shape[i] = structure[i];
        }
        return this.make(shape);
    }

    I make(long... structure);

    default I make(final StructureAnyD shape) {
        return this.make(shape.shape());
    }

}

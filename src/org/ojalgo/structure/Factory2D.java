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
package org.ojalgo.structure;

import java.util.List;

import org.ojalgo.function.NullaryFunction;

public interface Factory2D<I extends Structure2D> extends FactorySupplement {

    /**
     * Should only be implemented by factories that always produce dense structures.
     *
     * @author apete
     */
    interface Dense<I extends Structure2D> extends Factory2D<I> {

        default I column(final double... elements) {
            return this.columns(elements);
        }

        I columns(Access1D<?>... source);

        I columns(double[]... source);

        @SuppressWarnings("unchecked")
        I columns(List<? extends Comparable<?>>... source);

        I columns(Comparable<?>[]... source);

        I copy(Access2D<?> source);

        I makeFilled(long rows, long columns, NullaryFunction<?> supplier);

        default I makeFilled(final Structure2D shape, final NullaryFunction<?> supplier) {
            return this.makeFilled(shape.countRows(), shape.countColumns(), supplier);
        }

        default I row(final double... elements) {
            return this.rows(elements);
        }

        I rows(Access1D<?>... source);

        I rows(double[]... source);

        @SuppressWarnings("unchecked")
        I rows(List<? extends Comparable<?>>... source);

        I rows(Comparable<?>[]... source);

    }

    /**
     * For when the structures can be either dense or sparse.
     *
     * @author apete
     */
    interface MayBeSparse<I extends Structure2D, DR extends Mutate2D.ModifiableReceiver<?>, SR extends Mutate2D.ModifiableReceiver<?>> extends Factory2D<I> {

        DR makeDense(long rows, long columns);

        default DR makeDense(final Structure2D shape) {
            return this.makeDense(shape.countRows(), shape.countColumns());
        }

        SR makeSparse(long rows, long columns);

        default SR makeSparse(final Structure2D shape) {
            return this.makeSparse(shape.countRows(), shape.countColumns());
        }

    }

    I make(long rows, long columns);

    default I make(final Structure2D shape) {
        return this.make(shape.countRows(), shape.countColumns());
    }

    /**
     * @deprecated v48 Use {@link #make(long,long)} instead
     */
    @Deprecated
    default I makeZero(final long rows, final long columns) {
        return this.make(rows, columns);
    }

    /**
     * @deprecated v48 Use {@link #make(Structure2D)} instead
     */
    @Deprecated
    default I makeZero(final Structure2D shape) {
        return this.make(shape);
    }

}

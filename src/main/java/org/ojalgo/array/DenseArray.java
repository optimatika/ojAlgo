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
package org.ojalgo.array;

import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Factory1D;
import org.ojalgo.structure.StructureAnyD;

/**
 * Each and every element occupies memory and holds a value.
 *
 * @author apete
 */
public abstract class DenseArray<N extends Comparable<N>> extends BasicArray<N> implements Factory1D.Builder<DenseArray<N>> {

    public static abstract class Factory<N extends Comparable<N>> extends ArrayFactory<N, DenseArray<N>>
            implements Factory1D.TwoStep<DenseArray<N>, DenseArray<N>> {

        @Override
        public DenseArray<N> newBuilder(final long count) {
            return this.makeToBeFilled(count);
        }

        @Override
        long getCapacityLimit() {
            return PlainArray.MAX_SIZE;
        }

        final long getElementSize() {
            return this.getMathType().getTotalMemory();
        }

        abstract DenseArray<N> makeDenseArray(long size);

        @Override
        final DenseArray<N> makeStructuredZero(final long... structure) {

            final long total = StructureAnyD.count(structure);

            if (total > this.getCapacityLimit()) {

                throw new IllegalArgumentException();

            } else {

                return this.makeDenseArray(total);
            }
        }

        @Override
        final DenseArray<N> makeToBeFilled(final long... structure) {

            final long total = StructureAnyD.count(structure);

            if (total > this.getCapacityLimit()) {

                throw new IllegalArgumentException();

            } else {

                return this.makeDenseArray(total);
            }
        }

    }

    protected DenseArray(final DenseArray.Factory<N> factory) {
        super(factory);
    }

    @Override
    public DenseArray<N> build() {
        return this;
    }

    abstract void modify(long extIndex, int intIndex, Access1D<N> left, BinaryFunction<N> function);

    abstract void modify(long extIndex, int intIndex, BinaryFunction<N> function, Access1D<N> right);

    abstract void modify(long extIndex, int intIndex, UnaryFunction<N> function);
}

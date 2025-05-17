/*
 * Copyright 1997-2025 Optimatika
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
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Factory1D;
import org.ojalgo.type.math.MathType;

/**
 * Each and every element occupies memory and holds a value.
 *
 * @author apete
 */
public abstract class DenseArray<N extends Comparable<N>> extends BasicArray<N> implements Factory1D.Builder<DenseArray<N>> {

    public abstract static class Factory<N extends Comparable<N>, A extends DenseArray<N>> extends BaseFactory<N, A> {

        protected Factory(final MathType mathType) {
            super(mathType);
        }

        public A copy(final Access1D<?> values) {
            A retVal = this.make(values);
            for (long i = 0L, limit = values.count(); i < limit; i++) {
                retVal.set(i, values.get(i));
            }
            return retVal;
        }

        public A copy(final double[] values) {
            A retVal = this.make(values.length);
            for (int i = 0, limit = values.length; i < limit; i++) {
                retVal.set(i, values[i]);
            }
            return retVal;
        }

        public A makeFilled(final int size, final NullaryFunction<?> supplier) {
            A retVal = this.make(size);
            for (int i = 0; i < size; i++) {
                retVal.set(i, supplier.doubleValue());
            }
            return retVal;
        }

    }

    protected DenseArray(final DenseArray.Factory<N, ?> factory) {
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

/*
 * Copyright 1997-2016 Optimatika (www.optimatika.se)
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

import java.util.ArrayList;

import org.ojalgo.access.Access1D;
import org.ojalgo.access.AccessUtils;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.scalar.Scalar;

public abstract class DenseArray<N extends Number> extends BasicArray<N> {

    public static abstract class DenseFactory<N extends Number> extends ArrayFactory<N, DenseArray<N>> {

        abstract long getElementSize();

        abstract DenseArray<N> make(int size);

        @Override
        final DenseArray<N> makeStructuredZero(final long... structure) {
            return this.make((int) AccessUtils.count(structure));
        }

        @Override
        final DenseArray<N> makeToBeFilled(final long... structure) {
            return this.make((int) AccessUtils.count(structure));
        }

        abstract Scalar<N> zero();

    }

    /**
     * Exists as a private constant in {@link ArrayList}. The Oracle JVM seems to actually be limited at
     * Integer.MAX_VALUE - 2 but other JVM:s may have different limits.
     */
    public static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    protected DenseArray() {
        super();
    }

    protected abstract void modify(int index, Access1D<N> left, BinaryFunction<N> function);

    protected abstract void modify(int index, BinaryFunction<N> function, Access1D<N> right);

    protected abstract void modify(int index, UnaryFunction<N> function);

    protected abstract DenseArray<N> newInstance(int capacity);

}

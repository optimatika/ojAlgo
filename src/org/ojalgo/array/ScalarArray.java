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
package org.ojalgo.array;

import java.util.Arrays;
import java.util.Comparator;

import org.ojalgo.access.Mutate1D;
import org.ojalgo.array.blas.AMAX;
import org.ojalgo.array.blas.AXPY;
import org.ojalgo.scalar.Scalar;

/**
 * A one- and/or arbitrary-dimensional array of {@linkplain org.ojalgo.scalar.Scalar}.
 *
 * @author apete
 */
public abstract class ScalarArray<N extends Number & Scalar<N>> extends ReferenceTypeArray<N> {

    protected ScalarArray(DenseArray.Factory<N> factory, final int length) {
        super(factory, length);
    }

    protected ScalarArray(DenseArray.Factory<N> factory, final N[] data) {
        super(factory, data);
    }

    public final void axpy(final double a, final Mutate1D y) {
        AXPY.invoke(y, a, data);
    }

    @Override
    public final void sortAscending() {
        Arrays.parallelSort(data);
    }

    @Override
    public final void sortDescending() {
        Arrays.parallelSort(data, Comparator.reverseOrder());
    }

    @Override
    protected final void add(final int index, final double addend) {
        this.fillOne(index, this.get(index).add(this.valueOf(addend)).get());
    }

    @Override
    protected final void add(final int index, final Number addend) {
        this.fillOne(index, this.get(index).add(this.valueOf(addend)).get());
    }

    @Override
    protected final int indexOfLargest(final int first, final int limit, final int step) {
        return AMAX.invoke(data, first, limit, step);
    }

    @Override
    protected final boolean isAbsolute(final int index) {
        return data[index].isAbsolute();
    }

    @Override
    protected final boolean isSmall(final int index, final double comparedTo) {
        return data[index].isSmall(comparedTo);
    }

}

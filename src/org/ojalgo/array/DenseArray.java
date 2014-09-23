/*
 * Copyright 1997-2014 Optimatika (www.optimatika.se)
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

import java.util.RandomAccess;

import org.ojalgo.access.Access1D;
import org.ojalgo.access.AccessUtils;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.ParameterFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.scalar.Scalar;

/**
 * Represents a single dense array - casts long indices to int.
 *
 * @author apete
 */
abstract class DenseArray<N extends Number> extends BasicArray<N> implements RandomAccess {

    static abstract class DenseFactory<N extends Number> extends ArrayFactory<N> {

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

    DenseArray() {
        super();
    }

    public final long count() {
        return this.size();
    }

    public final double doubleValue(final long index) {
        return this.doubleValue((int) index);
    }

    public final void fillAll(final N number) {
        this.fill(0, this.size(), 1, number);
    }

    public final void fillRange(final long first, final long limit, final N number) {
        this.fill(first, limit, 1L, number);
    }

    public final N get(final long index) {
        return this.get((int) index);
    }

    /**
     * @see Scalar#isAbsolute()
     */
    public final boolean isAbsolute(final long index) {
        return this.isZero((int) index);
    }

    /**
     * @see Scalar#isPositive()
     */
    public final boolean isPositive(final long index) {
        return this.isPositive((int) index);
    }

    /**
     * @see Scalar#isZero()
     */
    public final boolean isZero(final long index) {
        return this.isZero((int) index);
    }

    public final void set(final long index, final double value) {
        this.set((int) index, value);
    }

    public final void set(final long index, final Number number) {
        this.set((int) index, number);
    }

    protected abstract double doubleValue(final int index);

    protected abstract void exchange(int firstA, int firstB, int step, int count);

    @Override
    protected final void exchange(final long firstA, final long firstB, final long step, final long count) {
        this.exchange((int) firstA, (int) firstB, (int) step, (int) count);
    }

    protected abstract void fill(final int first, final int limit, final Access1D<N> left, final BinaryFunction<N> function, final Access1D<N> right);

    protected abstract void fill(final int first, final int limit, final Access1D<N> left, final BinaryFunction<N> function, final N right);

    protected abstract void fill(int first, int limit, int step, N value);

    protected abstract void fill(final int first, final int limit, final N left, final BinaryFunction<N> function, final Access1D<N> right);

    @Override
    protected final void fill(final long first, final long limit, final long step, final N value) {
        this.fill((int) first, (int) limit, (int) step, value);
    }

    protected abstract N get(final int index);

    protected abstract int indexOfLargest(int first, int limit, int step);

    @Override
    protected final long indexOfLargest(final long first, final long limit, final long step) {
        return this.indexOfLargest((int) first, (int) limit, (int) step);
    }

    /**
     * @see Scalar#isAbsolute()
     */
    protected abstract boolean isAbsolute(int index);

    /**
     * @see Scalar#isPositive()
     */
    protected abstract boolean isPositive(int index);

    /**
     * @see Scalar#isZero()
     */
    protected abstract boolean isZero(int index);

    protected abstract boolean isZeros(int first, int limit, int step);

    @Override
    protected final boolean isZeros(final long first, final long limit, final long step) {
        return this.isZeros((int) first, (int) limit, (int) step);
    }

    protected abstract void modify(int index, Access1D<N> left, BinaryFunction<N> function);

    protected abstract void modify(int index, BinaryFunction<N> function, Access1D<N> right);

    protected abstract void modify(int first, int limit, int step, Access1D<N> left, BinaryFunction<N> function);

    protected abstract void modify(int first, int limit, int step, BinaryFunction<N> function, Access1D<N> right);

    protected abstract void modify(int first, int limit, int step, BinaryFunction<N> function, N right);

    protected abstract void modify(int first, int limit, int step, N left, BinaryFunction<N> function);

    protected abstract void modify(int first, int limit, int step, ParameterFunction<N> function, int parameter);

    protected abstract void modify(int first, int limit, int step, UnaryFunction<N> function);

    protected abstract void modify(int index, UnaryFunction<N> function);

    @Override
    protected final void modify(final long first, final long limit, final long step, final Access1D<N> left, final BinaryFunction<N> function) {
        this.modify((int) first, (int) limit, (int) step, left, function);
    }

    @Override
    protected final void modify(final long first, final long limit, final long step, final BinaryFunction<N> function, final Access1D<N> right) {
        this.modify((int) first, (int) limit, (int) step, function, right);
    }

    @Override
    protected final void modify(final long first, final long limit, final long step, final UnaryFunction<N> function) {
        this.modify((int) first, (int) limit, (int) step, function);
    }

    /**
     * @see java.util.Arrays#binarySearch(Object[], Object)
     * @see #sortAscending()
     * @throws UnsupportedOperationException if the this operation is not supported by this implementation/subclass
     */
    protected abstract int searchAscending(N number);

    protected abstract void set(final int index, final double value);

    protected abstract void set(final int index, final Number number);

    protected abstract int size();

    /**
     * @see java.util.Arrays#sort(Object[])
     * @see #searchAscending(Number)
     * @throws UnsupportedOperationException if the this operation is not supported by this implementation/subclass
     */
    protected abstract void sortAscending();

    protected abstract void visit(int first, int limit, int step, VoidFunction<N> visitor);

    protected abstract void visit(int index, VoidFunction<N> visitor);

    @Override
    protected final void visit(final long first, final long limit, final long step, final VoidFunction<N> visitor) {
        this.visit((int) first, (int) limit, (int) step, visitor);
    }

    abstract DenseArray<N> newInstance(int capacity);

}

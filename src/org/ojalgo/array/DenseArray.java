/*
 * Copyright 1997-2015 Optimatika (www.optimatika.se)
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
import java.util.Spliterator;

import org.ojalgo.access.Access1D;
import org.ojalgo.access.AccessUtils;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.NullaryFunction;
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

    static final int CHARACTERISTICS = Spliterator.ORDERED | Spliterator.IMMUTABLE;

    DenseArray() {
        super();
    }

    public void add(final long index, final double addend) {
        this.add((int) index, addend);
    }

    public void add(final long index, final Number addend) {
        this.add((int) index, addend);
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

    public final void fillAll(final NullaryFunction<N> supplier) {
        this.fill(0, this.size(), 1, supplier);
    }

    public void fillOne(final long index, final N value) {
        this.fillOne((int) index, value);
    }

    public void fillOne(final long index, final NullaryFunction<N> supplier) {
        this.fillOne((int) index, supplier);
    }

    public final void fillOneMatching(final long index, final Access1D<?> values, final long valueIndex) {
        this.fillOneMatching((int) index, values, valueIndex);
    }

    public final void fillRange(final long first, final long limit, final N number) {
        this.fill(first, limit, 1L, number);
    }

    public final void fillRange(final long first, final long limit, final NullaryFunction<N> supplier) {
        this.fill(first, limit, 1L, supplier);
    }

    public final N get(final long index) {
        return this.get((int) index);
    }

    /**
     * @see Scalar#isAbsolute()
     */
    public final boolean isAbsolute(final long index) {
        return this.isAbsolute((int) index);
    }

    /**
     * @see Scalar#isSmall(double)
     */
    public final boolean isSmall(final long index, final double comparedTo) {
        return this.isSmall((int) index, comparedTo);
    }

    public final void modifyOne(final long index, final UnaryFunction<N> function) {
        this.modify((int) index, function);
    }

    public final void set(final long index, final double value) {
        this.set((int) index, value);
    }

    public final void set(final long index, final Number number) {
        this.set((int) index, number);
    }

    public void visitOne(final long index, final VoidFunction<N> visitor) {
        this.visitOne((int) index, visitor);
    }

    private final boolean isSmall(final int first, final int limit, final int step, final double comparedTo) {

        boolean retVal = true;

        for (int i = first; retVal && (i < limit); i += step) {
            retVal &= this.isSmall(i, comparedTo);
        }

        return retVal;
    }

    protected abstract void add(int index, double addend);

    protected abstract void add(int index, Number addend);

    protected abstract double doubleValue(final int index);

    protected abstract void exchange(int firstA, int firstB, int step, int count);

    @Override
    protected final void exchange(final long firstA, final long firstB, final long step, final long count) {
        this.exchange((int) firstA, (int) firstB, (int) step, (int) count);
    }

    protected abstract void fill(final int first, final int limit, final Access1D<N> left, final BinaryFunction<N> function, final Access1D<N> right);

    protected abstract void fill(final int first, final int limit, final Access1D<N> left, final BinaryFunction<N> function, final N right);

    protected abstract void fill(int first, int limit, int step, N value);

    protected abstract void fill(int first, int limit, int step, NullaryFunction<N> supplier);

    protected abstract void fill(final int first, final int limit, final N left, final BinaryFunction<N> function, final Access1D<N> right);

    @Override
    protected final void fill(final long first, final long limit, final long step, final N value) {
        this.fill((int) first, (int) limit, (int) step, value);
    }

    @Override
    protected final void fill(final long first, final long limit, final long step, final NullaryFunction<N> supplier) {
        this.fill((int) first, (int) limit, (int) step, supplier);
    }

    protected abstract void fillOne(int index, N value);

    protected abstract void fillOne(int index, NullaryFunction<N> supplier);

    protected abstract void fillOneMatching(final int index, final Access1D<?> values, final long valueIndex);

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
     * @see Scalar#isSmall(double)
     */
    protected abstract boolean isSmall(int index, double comparedTo);

    @Override
    protected final boolean isSmall(final long first, final long limit, final long step, final double comparedTo) {
        return this.isSmall((int) first, (int) limit, (int) step, comparedTo);
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
     * @throws UnsupportedOperationException if the this operation is not supported by this
     *         implementation/subclass
     */
    protected abstract int searchAscending(N number);

    protected abstract void set(final int index, final double value);

    protected abstract void set(final int index, final Number number);

    protected abstract int size();

    /**
     * @see java.util.Arrays#sort(Object[])
     * @see #searchAscending(Number)
     * @throws UnsupportedOperationException if the this operation is not supported by this
     *         implementation/subclass
     */
    protected abstract void sortAscending();

    protected abstract void visit(int first, int limit, int step, VoidFunction<N> visitor);

    @Override
    protected final void visit(final long first, final long limit, final long step, final VoidFunction<N> visitor) {
        this.visit((int) first, (int) limit, (int) step, visitor);
    }

    protected abstract void visitOne(final int index, final VoidFunction<N> visitor);

    abstract DenseArray<N> newInstance(int capacity);

}

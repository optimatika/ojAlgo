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

import java.util.RandomAccess;
import java.util.Spliterator;

import org.ojalgo.access.Access1D;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.ParameterFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.scalar.Scalar;

/**
 * <p>
 * Array class limited by integer (int, not long) indices. Typically this will be a plain java array as in
 * <code>double[]</code>. The long indices from the various method arguments are cast to int.
 * </p>
 *
 * @author apete
 */
abstract class PlainArray<N extends Number> extends DenseArray<N> implements RandomAccess {

    static final int CHARACTERISTICS = Spliterator.ORDERED | Spliterator.IMMUTABLE;

    PlainArray(DenseArray.Factory<N> factory, final int size) {

        super(factory);

        if (size > MAX_ARRAY_SIZE) {
            throw new IllegalArgumentException("Array too large!");
        }
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

    public final void fillOne(final long index, final Access1D<?> values, final long valueIndex) {
        this.fillOne((int) index, values, valueIndex);
    }

    public void fillOne(final long index, final N value) {
        this.fillOne((int) index, value);
    }

    public void fillOne(final long index, final NullaryFunction<N> supplier) {
        this.fillOne((int) index, supplier);
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

    public final void modifyOne(final long index, final UnaryFunction<N> modifier) {
        this.modifyOne((int) index, modifier);
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

    protected abstract void fillOne(final int index, final Access1D<?> values, final long valueIndex);

    protected abstract void fillOne(int index, N value);

    protected abstract void fillOne(int index, NullaryFunction<N> supplier);

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

    protected abstract void modify(int first, int limit, int step, Access1D<N> left, BinaryFunction<N> function);

    protected abstract void modify(int first, int limit, int step, BinaryFunction<N> function, Access1D<N> right);

    protected abstract void modify(int first, int limit, int step, BinaryFunction<N> function, N right);

    protected abstract void modify(int first, int limit, int step, N left, BinaryFunction<N> function);

    protected abstract void modify(int first, int limit, int step, ParameterFunction<N> function, int parameter);

    protected abstract void modify(int first, int limit, int step, UnaryFunction<N> function);

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

    protected abstract void modifyOne(final int index, final UnaryFunction<N> modifier);

    protected abstract int searchAscending(final N number);

    protected abstract void set(final int index, final double value);

    protected abstract void set(final int index, final Number number);

    protected abstract int size();

    protected abstract void sortAscending();

    protected abstract void sortDescending();

    protected abstract void visit(int first, int limit, int step, VoidFunction<N> visitor);

    @Override
    protected final void visit(final long first, final long limit, final long step, final VoidFunction<N> visitor) {
        this.visit((int) first, (int) limit, (int) step, visitor);
    }

    protected abstract void visitOne(final int index, final VoidFunction<N> visitor);

}

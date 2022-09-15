/*
 * Copyright 1997-2022 Optimatika
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
import java.util.RandomAccess;
import java.util.Spliterator;

import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access1D;

/**
 * Array class limited by integer (int, not long) indices. Typically this will be a plain java array as in
 * <code>double[]</code>. This class terminates/implements all methods with long arguments, casts the long
 * arguments to int and delegates to new protected abstract methods with the int arguments. No new public
 * methods are declared here.
 *
 * @author apete
 */
public abstract class PlainArray<N extends Comparable<N>> extends DenseArray<N> implements RandomAccess {

    static final int CHARACTERISTICS = Spliterator.ORDERED | Spliterator.IMMUTABLE;

    private final int mySize;

    /**
     * Exists as a private constant in {@link ArrayList}. The Oracle JVM seems to actually be limited at
     * Integer.MAX_VALUE - 2, but other JVM:s may have different limits.
     */
    public static final int MAX_SIZE = Integer.MAX_VALUE - 8;

    PlainArray(final DenseArray.Factory<N> factory, final int size) {

        super(factory);

        if (size > PlainArray.MAX_SIZE) {
            throw new IllegalArgumentException("Array too large!");
        }

        mySize = size;
    }

    public final int size() {
        return mySize;
    }

    @Override
    public final void add(final long index, final Comparable<?> addend) {
        this.add(Math.toIntExact(index), addend);
    }

    @Override
    public final void add(final long index, final double addend) {
        this.add(Math.toIntExact(index), addend);
    }

    @Override
    public final void add(final long index, final float addend) {
        this.add(Math.toIntExact(index), addend);
    }

    @Override
    public final byte byteValue(final long index) {
        // No Math.toIntExact() here, be as direct as possible
        return this.byteValue((int) index);
    }

    @Override
    public final long count() {
        return this.size();
    }

    @Override
    public final double doubleValue(final long index) {
        // No Math.toIntExact() here, be as direct as possible
        return this.doubleValue((int) index);
    }

    @Override
    public final void fillAll(final N number) {
        this.fill(0, this.size(), 1, number);
    }

    @Override
    public final void fillAll(final NullaryFunction<?> supplier) {
        this.fill(0, this.size(), 1, supplier);
    }

    @Override
    public final void fillOne(final long index, final Access1D<?> values, final long valueIndex) {
        this.fillOne(Math.toIntExact(index), values, valueIndex);
    }

    @Override
    public final void fillOne(final long index, final N value) {
        this.fillOne(Math.toIntExact(index), value);
    }

    @Override
    public final void fillOne(final long index, final NullaryFunction<?> supplier) {
        this.fillOne(Math.toIntExact(index), supplier);
    }

    @Override
    public final void fillRange(final long first, final long limit, final N number) {
        this.fill(first, limit, 1L, number);
    }

    @Override
    public final void fillRange(final long first, final long limit, final NullaryFunction<?> supplier) {
        this.fill(first, limit, 1L, supplier);
    }

    @Override
    public final float floatValue(final long index) {
        // No Math.toIntExact() here, be as direct as possible
        return this.floatValue((int) index);
    }

    @Override
    public final N get(final long index) {
        // No Math.toIntExact() here, be as direct as possible
        return this.get((int) index);
    }

    @Override
    public final int intValue(final long index) {
        // No Math.toIntExact() here, be as direct as possible
        return this.intValue((int) index);
    }

    @Override
    public final long longValue(final long index) {
        // No Math.toIntExact() here, be as direct as possible
        return this.longValue((int) index);
    }

    @Override
    public final void modifyOne(final long index, final UnaryFunction<N> modifier) {
        this.modifyOne(Math.toIntExact(index), modifier);
    }

    @Override
    public final void set(final long index, final Comparable<?> number) {
        // No Math.toIntExact() here, be as direct as possible
        this.set((int) index, number);
    }

    @Override
    public final void set(final long index, final double value) {
        // No Math.toIntExact() here, be as direct as possible
        this.set((int) index, value);
    }

    @Override
    public final void set(final long index, final float value) {
        // No Math.toIntExact() here, be as direct as possible
        this.set((int) index, value);
    }

    @Override
    public final short shortValue(final long index) {
        // No Math.toIntExact() here, be as direct as possible
        return this.shortValue((int) index);
    }

    @Override
    public final void visitOne(final long index, final VoidFunction<N> visitor) {
        this.visitOne(Math.toIntExact(index), visitor);
    }

    private final boolean isSmall(final int first, final int limit, final int step, final double comparedTo) {

        boolean retVal = true;

        for (int i = first; retVal && i < limit; i += step) {
            retVal &= this.isSmall(i, comparedTo);
        }

        return retVal;
    }

    protected abstract void add(int index, Comparable<?> addend);

    protected abstract void add(int index, double addend);

    protected abstract void add(int index, float addend);

    protected abstract byte byteValue(int index);

    protected double doubleValue(final int index) {
        return this.floatValue(index);
    }

    protected abstract void exchange(int firstA, int firstB, int step, int count);

    @Override
    protected final void exchange(final long firstA, final long firstB, final long step, final long count) {
        this.exchange(Math.toIntExact(firstA), Math.toIntExact(firstB), Math.toIntExact(step), Math.toIntExact(count));
    }

    protected abstract void fill(int first, int limit, int step, N value);

    protected abstract void fill(int first, int limit, int step, NullaryFunction<?> supplier);

    @Override
    protected final void fill(final long first, final long limit, final long step, final N value) {
        this.fill(Math.toIntExact(first), Math.toIntExact(limit), Math.toIntExact(step), value);
    }

    @Override
    protected final void fill(final long first, final long limit, final long step, final NullaryFunction<?> supplier) {
        this.fill(Math.toIntExact(first), Math.toIntExact(limit), Math.toIntExact(step), supplier);
    }

    protected abstract void fillOne(final int index, final Access1D<?> values, final long valueIndex);

    protected abstract void fillOne(int index, N value);

    protected abstract void fillOne(int index, NullaryFunction<?> supplier);

    protected abstract float floatValue(final int index);

    protected abstract N get(final int index);

    protected abstract int indexOfLargest(int first, int limit, int step);

    @Override
    protected final long indexOfLargest(final long first, final long limit, final long step) {
        return this.indexOfLargest(Math.toIntExact(first), Math.toIntExact(limit), Math.toIntExact(step));
    }

    protected int intValue(final int index) {
        return this.shortValue(index);
    }

    /**
     * @see Scalar#isAbsolute()
     */
    protected abstract boolean isAbsolute(int index);

    /**
     * @see Scalar#isSmall(double)
     */
    protected abstract boolean isSmall(int index, double comparedTo);

    protected long longValue(final int index) {
        return this.intValue(index);
    }

    protected abstract void modify(int first, int limit, int step, Access1D<N> left, BinaryFunction<N> function);

    protected abstract void modify(int first, int limit, int step, BinaryFunction<N> function, Access1D<N> right);

    protected abstract void modify(int first, int limit, int step, UnaryFunction<N> function);

    @Override
    protected final void modify(final long first, final long limit, final long step, final Access1D<N> left, final BinaryFunction<N> function) {
        this.modify(Math.toIntExact(first), Math.toIntExact(limit), Math.toIntExact(step), left, function);
    }

    @Override
    protected final void modify(final long first, final long limit, final long step, final BinaryFunction<N> function, final Access1D<N> right) {
        this.modify(Math.toIntExact(first), Math.toIntExact(limit), Math.toIntExact(step), function, right);
    }

    @Override
    protected final void modify(final long first, final long limit, final long step, final UnaryFunction<N> function) {
        this.modify(Math.toIntExact(first), Math.toIntExact(limit), Math.toIntExact(step), function);
    }

    protected abstract void modifyOne(final int index, final UnaryFunction<N> modifier);

    protected abstract int searchAscending(final N number);

    protected abstract void set(final int index, final Comparable<?> number);

    protected abstract void set(final int index, final double value);

    protected abstract void set(final int index, final float value);

    protected short shortValue(final int index) {
        return this.byteValue(index);
    }

    protected abstract void sortAscending();

    protected abstract void sortDescending();

    protected abstract void visit(int first, int limit, int step, VoidFunction<N> visitor);

    @Override
    protected final void visit(final long first, final long limit, final long step, final VoidFunction<N> visitor) {
        this.visit(Math.toIntExact(first), Math.toIntExact(limit), Math.toIntExact(step), visitor);
    }

    protected abstract void visitOne(final int index, final VoidFunction<N> visitor);

}

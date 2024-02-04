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

import java.util.Arrays;
import java.util.Spliterator;
import java.util.Spliterators;

import org.ojalgo.array.operation.COPY;
import org.ojalgo.array.operation.Exchange;
import org.ojalgo.array.operation.FillAll;
import org.ojalgo.array.operation.FillMatchingSingle;
import org.ojalgo.array.operation.OperationBinary;
import org.ojalgo.array.operation.OperationUnary;
import org.ojalgo.array.operation.OperationVoid;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.function.special.MissingMath;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Mutate1D;

/**
 * A one- and/or arbitrary-dimensional array of {@linkplain java.lang.Comparable}.
 *
 * @author apete
 */
public abstract class ReferenceTypeArray<N extends Comparable<N>> extends PlainArray<N> implements Mutate1D.Sortable {

    public final N[] data;

    ReferenceTypeArray(final DenseArray.Factory<N> factory, final int length) {

        super(factory, length);

        Scalar.Factory<N> scalarFactory = factory.scalar();

        data = scalarFactory.newArrayInstance(length);

        N zero = scalarFactory.zero().get();
        Arrays.fill(data, zero);
    }

    ReferenceTypeArray(final DenseArray.Factory<N> factory, final N[] data) {

        super(factory, data.length);

        this.data = data;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj) || !(obj instanceof ReferenceTypeArray)) {
            return false;
        }
        ReferenceTypeArray<?> other = (ReferenceTypeArray<?>) obj;
        if (!Arrays.equals(data, other.data)) {
            return false;
        }
        return true;
    }

    @Override
    public void fillMatching(final Access1D<?> values) {
        FillMatchingSingle.fill(data, values, this.factory().scalar());
    }

    @Override
    public void fillMatching(final Access1D<N> left, final BinaryFunction<N> function, final Access1D<N> right) {
        int limit = MissingMath.toMinIntExact(this.count(), left.count(), right.count());
        for (int i = 0; i < limit; i++) {
            data[i] = function.invoke(left.get(i), right.get(i));
        }
    }

    @Override
    public void fillMatching(final UnaryFunction<N> function, final Access1D<N> arguments) {
        int limit = MissingMath.toMinIntExact(this.count(), arguments.count());
        for (int i = 0; i < limit; i++) {
            data[i] = function.invoke(arguments.get(i));
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        return prime * result + Arrays.hashCode(data);
    }

    @Override
    public final void reset() {
        Arrays.fill(data, this.valueOf(PrimitiveMath.ZERO));
    }

    public final Spliterator<N> spliterator() {
        return Spliterators.spliterator(data, 0, data.length, PlainArray.CHARACTERISTICS);
    }

    protected final N[] copyOfData() {
        return COPY.copyOf(data);
    }

    @Override
    protected final void exchange(final int firstA, final int firstB, final int step, final int count) {
        Exchange.exchange(data, firstA, firstB, step, count);
    }

    @Override
    protected final void fill(final int first, final int limit, final int step, final N value) {
        FillAll.fill(data, first, limit, step, value);
    }

    @Override
    protected final void fill(final int first, final int limit, final int step, final NullaryFunction<?> supplier) {
        FillAll.fill(data, first, limit, step, supplier, this.factory().scalar());
    }

    @Override
    protected final void fillOne(final int index, final N value) {
        data[index] = value;

    }

    @Override
    protected final void fillOne(final int index, final NullaryFunction<?> supplier) {
        data[index] = this.valueOf(supplier.get());
    }

    @Override
    public final N get(final int index) {
        return data[index];
    }

    @Override
    protected final void modify(final int first, final int limit, final int step, final Access1D<N> left, final BinaryFunction<N> function) {
        OperationBinary.invoke(data, first, limit, step, left, function, this);
    }

    @Override
    protected final void modify(final int first, final int limit, final int step, final BinaryFunction<N> function, final Access1D<N> right) {
        OperationBinary.invoke(data, first, limit, step, this, function, right);
    }

    @Override
    protected final void modify(final int first, final int limit, final int step, final UnaryFunction<N> function) {
        OperationUnary.invoke(data, first, limit, step, this, function);
    }

    @Override
    protected final void modifyOne(final int index, final UnaryFunction<N> modifier) {
        data[index] = modifier.invoke(data[index]);
    }

    @Override
    protected final int searchAscending(final N value) {
        return Arrays.binarySearch(data, value);
    }

    @Override
    protected final void set(final int index, final Comparable<?> value) {
        data[index] = this.valueOf(value);
    }

    @Override
    public final void set(final int index, final double value) {
        data[index] = this.valueOf(value);
    }

    @Override
    public final void set(final int index, final float value) {
        data[index] = this.valueOf(value);
    }

    @Override
    protected final void visit(final int first, final int limit, final int step, final VoidFunction<N> visitor) {
        OperationVoid.invoke(data, first, limit, step, visitor);
    }

    @Override
    protected void visitOne(final int index, final VoidFunction<N> visitor) {
        visitor.invoke(data[index]);
    }

    @Override
    final void modify(final long extIndex, final int intIndex, final Access1D<N> left, final BinaryFunction<N> function) {
        data[intIndex] = function.invoke(left.get(extIndex), data[intIndex]);
    }

    @Override
    final void modify(final long extIndex, final int intIndex, final BinaryFunction<N> function, final Access1D<N> right) {
        data[intIndex] = function.invoke(data[intIndex], right.get(extIndex));
    }

    @Override
    final void modify(final long extIndex, final int intIndex, final UnaryFunction<N> function) {
        data[intIndex] = function.invoke(data[intIndex]);
    }

    final N valueOf(final Comparable<?> number) {
        return this.factory().scalar().cast(number);
    }

    final N valueOf(final double value) {
        return this.factory().scalar().cast(value);
    }

    final N valueOf(final float value) {
        return this.factory().scalar().cast(value);
    }

}

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

import static org.ojalgo.constant.PrimitiveMath.*;

import java.util.Arrays;
import java.util.Spliterator;
import java.util.Spliterators;

import org.ojalgo.access.Access1D;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.ParameterFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.machine.MemoryEstimator;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.type.TypeUtils;

/**
 * A one- and/or arbitrary-dimensional array of {@linkplain org.ojalgo.scalar.Quaternion}.
 *
 * @author apete
 */
public class QuaternionArray extends DenseArray<Quaternion> {

    static final long ELEMENT_SIZE = MemoryEstimator.estimateObject(Quaternion.class);

    static final DenseFactory<Quaternion> FACTORY = new DenseFactory<Quaternion>() {

        @Override
        long getElementSize() {
            return ELEMENT_SIZE;
        }

        @Override
        DenseArray<Quaternion> make(final int size) {
            return QuaternionArray.make(size);
        }

        @Override
        Scalar<Quaternion> zero() {
            return Quaternion.ZERO;
        }

    };

    public static final QuaternionArray make(final int size) {
        return new QuaternionArray(size);
    }

    public static final SegmentedArray<Quaternion> makeSegmented(final long count) {
        return SegmentedArray.make(FACTORY, count);
    }

    public static final QuaternionArray wrap(final Quaternion[] data) {
        return new QuaternionArray(data);
    }

    protected static void exchange(final Quaternion[] data, final int firstA, final int firstB, final int step, final int aCount) {

        int tmpIndexA = firstA;
        int tmpIndexB = firstB;

        Quaternion tmpVal;

        for (int i = 0; i < aCount; i++) {

            tmpVal = data[tmpIndexA];
            data[tmpIndexA] = data[tmpIndexB];
            data[tmpIndexB] = tmpVal;

            tmpIndexA += step;
            tmpIndexB += step;
        }
    }

    protected static void fill(final Quaternion[] data, final Access1D<?> value) {
        final int tmpLimit = (int) Math.min(data.length, value.count());
        for (int i = 0; i < tmpLimit; i++) {
            data[i] = TypeUtils.toQuaternion(value.get(i));
        }
    }

    protected static void fill(final Quaternion[] data, final int first, final int limit, final int step, final Quaternion value) {
        for (int i = first; i < limit; i += step) {
            data[i] = value;
        }
    }

    protected static void fill(final Quaternion[] data, final int first, final int limit, final int step, final NullaryFunction<Quaternion> supplier) {
        for (int i = first; i < limit; i += step) {
            data[i] = supplier.invoke();
        }
    }

    protected static void invoke(final Quaternion[] data, final int first, final int limit, final int step, final Access1D<Quaternion> left,
            final BinaryFunction<Quaternion> function, final Access1D<Quaternion> right) {
        for (int i = first; i < limit; i += step) {
            data[i] = function.invoke(left.get(i), right.get(i));
        }
    }

    protected static void invoke(final Quaternion[] data, final int first, final int limit, final int step, final Access1D<Quaternion> left,
            final BinaryFunction<Quaternion> function, final Quaternion right) {
        for (int i = first; i < limit; i += step) {
            data[i] = function.invoke(left.get(i), right);
        }
    }

    protected static void invoke(final Quaternion[] data, final int first, final int limit, final int step, final Access1D<Quaternion> value,
            final ParameterFunction<Quaternion> function, final int aParam) {
        for (int i = first; i < limit; i += step) {
            data[i] = function.invoke(value.get(i), aParam);
        }
    }

    protected static void invoke(final Quaternion[] data, final int first, final int limit, final int step, final Access1D<Quaternion> value,
            final UnaryFunction<Quaternion> function) {
        for (int i = first; i < limit; i += step) {
            data[i] = function.invoke(value.get(i));
        }
    }

    protected static void invoke(final Quaternion[] data, final int first, final int limit, final int step, final Quaternion left,
            final BinaryFunction<Quaternion> function, final Access1D<Quaternion> right) {
        for (int i = first; i < limit; i += step) {
            data[i] = function.invoke(left, right.get(i));
        }
    }

    protected static void invoke(final Quaternion[] data, final int first, final int limit, final int step, final VoidFunction<Quaternion> aVisitor) {
        for (int i = first; i < limit; i += step) {
            aVisitor.invoke(data[i]);
        }
    }

    public final Quaternion[] data;

    protected QuaternionArray(final Quaternion[] data) {

        super();

        this.data = data;
    }

    protected QuaternionArray(final int size) {

        super();

        data = new Quaternion[size];
        this.fill(0, size, 1, Quaternion.ZERO);
    }

    @Override
    public boolean equals(final Object anObj) {
        if (anObj instanceof QuaternionArray) {
            return Arrays.equals(data, ((QuaternionArray) anObj).data);
        } else {
            return super.equals(anObj);
        }
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }

    public Spliterator<Quaternion> spliterator() {
        return Spliterators.spliterator(data, 0, data.length, DenseArray.CHARACTERISTICS);
    }

    protected final Quaternion[] copyOfData() {
        return ArrayUtils.copyOf(data);
    }

    @Override
    protected double doubleValue(final int index) {
        return data[index].doubleValue();
    }

    @Override
    protected final void exchange(final int firstA, final int firstB, final int step, final int count) {
        QuaternionArray.exchange(data, firstA, firstB, step, count);
    }

    protected void fill(final Access1D<?> value) {
        QuaternionArray.fill(data, value);
    }

    @Override
    protected final void fill(final int first, final int limit, final Access1D<Quaternion> left, final BinaryFunction<Quaternion> function,
            final Access1D<Quaternion> right) {
        QuaternionArray.invoke(data, first, limit, 1, left, function, right);
    }

    @Override
    protected final void fill(final int first, final int limit, final Access1D<Quaternion> left, final BinaryFunction<Quaternion> function,
            final Quaternion right) {
        QuaternionArray.invoke(data, first, limit, 1, left, function, right);
    }

    @Override
    protected final void fill(final int first, final int limit, final Quaternion left, final BinaryFunction<Quaternion> function,
            final Access1D<Quaternion> right) {
        QuaternionArray.invoke(data, first, limit, 1, left, function, right);
    }

    @Override
    protected final void fill(final int first, final int limit, final int step, final Quaternion value) {
        QuaternionArray.fill(data, first, limit, step, value);
    }

    @Override
    protected final void fill(final int first, final int limit, final int step, final NullaryFunction<Quaternion> supplier) {
        QuaternionArray.fill(data, first, limit, step, supplier);
    }

    @Override
    protected final Quaternion get(final int index) {
        return data[index];
    }

    @Override
    protected final int indexOfLargest(final int first, final int limit, final int step) {

        int retVal = first;
        double tmpLargest = ZERO;
        double tmpValue;

        for (int i = first; i < limit; i += step) {
            tmpValue = data[i].norm();
            if (tmpValue > tmpLargest) {
                tmpLargest = tmpValue;
                retVal = i;
            }
        }

        return retVal;
    }

    @Override
    protected boolean isAbsolute(final int index) {
        return Quaternion.isAbsolute(data[index]);
    }

    @Override
    protected boolean isSmall(final int index, final double comparedTo) {
        return Quaternion.isSmall(comparedTo, data[index]);
    }

    @Override
    protected void modify(final int index, final Access1D<Quaternion> left, final BinaryFunction<Quaternion> function) {
        data[index] = function.invoke(left.get(index), data[index]);
    }

    @Override
    protected void modify(final int index, final BinaryFunction<Quaternion> function, final Access1D<Quaternion> right) {
        data[index] = function.invoke(data[index], right.get(index));
    }

    @Override
    protected final void modify(final int first, final int limit, final int step, final Access1D<Quaternion> left, final BinaryFunction<Quaternion> function) {
        QuaternionArray.invoke(data, first, limit, step, left, function, this);
    }

    @Override
    protected final void modify(final int first, final int limit, final int step, final BinaryFunction<Quaternion> function, final Access1D<Quaternion> right) {
        QuaternionArray.invoke(data, first, limit, step, this, function, right);
    }

    @Override
    protected final void modify(final int first, final int limit, final int step, final BinaryFunction<Quaternion> function, final Quaternion right) {
        QuaternionArray.invoke(data, first, limit, step, this, function, right);
    }

    @Override
    protected final void modify(final int first, final int limit, final int step, final Quaternion left, final BinaryFunction<Quaternion> function) {
        QuaternionArray.invoke(data, first, limit, step, left, function, this);
    }

    @Override
    protected final void modify(final int first, final int limit, final int step, final ParameterFunction<Quaternion> function, final int parameter) {
        QuaternionArray.invoke(data, first, limit, step, this, function, parameter);
    }

    @Override
    protected final void modify(final int first, final int limit, final int step, final UnaryFunction<Quaternion> function) {
        QuaternionArray.invoke(data, first, limit, step, this, function);
    }

    @Override
    protected void modify(final int index, final UnaryFunction<Quaternion> function) {
        data[index] = function.invoke(data[index]);
    }

    /**
     * @see org.ojalgo.array.BasicArray#searchAscending(java.lang.Number)
     */
    @Override
    protected final int searchAscending(final Quaternion value) {
        return Arrays.binarySearch(data, value);
    }

    @Override
    protected final void set(final int index, final double value) {
        data[index] = Quaternion.valueOf(value);
    }

    @Override
    protected final void set(final int index, final Number value) {
        data[index] = TypeUtils.toQuaternion(value);
    }

    @Override
    protected int size() {
        return data.length;
    }

    @Override
    protected final void sortAscending() {
        Arrays.sort(data);
    }

    @Override
    protected final Quaternion toScalar(final long index) {
        return data[(int) index];
    }

    @Override
    protected final void visit(final int first, final int limit, final int step, final VoidFunction<Quaternion> visitor) {
        QuaternionArray.invoke(data, first, limit, step, visitor);
    }

    @Override
    protected final void visit(final int index, final VoidFunction<Quaternion> visitor) {
        visitor.invoke(data[index]);
    }

    @Override
    boolean isPrimitive() {
        return false;
    }

    @Override
    DenseArray<Quaternion> newInstance(final int capacity) {
        return new QuaternionArray(capacity);
    }

    @Override
    protected void modifyOne(final int index, final UnaryFunction<Quaternion> function) {
        data[index] = function.invoke(data[index]);
    }

}

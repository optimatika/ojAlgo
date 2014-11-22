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

import static org.ojalgo.constant.PrimitiveMath.*;

import java.util.Arrays;
import java.util.Spliterator;
import java.util.Spliterators;

import org.ojalgo.access.Access1D;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.ParameterFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.machine.MemoryEstimator;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.type.TypeUtils;

/**
 * A one- and/or arbitrary-dimensional array of {@linkplain org.ojalgo.scalar.ComplexNumber}.
 *
 * @author apete
 */
public class ComplexArray extends DenseArray<ComplexNumber> {

    static final long ELEMENT_SIZE = MemoryEstimator.estimateObject(ComplexNumber.class);

    static final DenseFactory<ComplexNumber> FACTORY = new DenseFactory<ComplexNumber>() {

        @Override
        long getElementSize() {
            return ELEMENT_SIZE;
        }

        @Override
        DenseArray<ComplexNumber> make(final int size) {
            return ComplexArray.make(size);
        }

        @Override
        Scalar<ComplexNumber> zero() {
            return ComplexNumber.ZERO;
        }

    };

    public static final ComplexArray make(final int size) {
        return new ComplexArray(size);
    }

    public static final SegmentedArray<ComplexNumber> makeSegmented(final long count) {
        return SegmentedArray.COMPLEX.makeSegmented(FACTORY, count);
    }

    public static final ComplexArray wrap(final ComplexNumber[] data) {
        return new ComplexArray(data);
    }

    protected static void exchange(final ComplexNumber[] data, final int firstA, final int firstB, final int step, final int aCount) {

        int tmpIndexA = firstA;
        int tmpIndexB = firstB;

        ComplexNumber tmpVal;

        for (int i = 0; i < aCount; i++) {

            tmpVal = data[tmpIndexA];
            data[tmpIndexA] = data[tmpIndexB];
            data[tmpIndexB] = tmpVal;

            tmpIndexA += step;
            tmpIndexB += step;
        }
    }

    protected static void fill(final ComplexNumber[] data, final Access1D<?> value) {
        final int tmpLimit = (int) Math.min(data.length, value.count());
        for (int i = 0; i < tmpLimit; i++) {
            data[i] = TypeUtils.toComplexNumber(value.get(i));
        }
    }

    protected static void fill(final ComplexNumber[] data, final int first, final int limit, final int step, final ComplexNumber value) {
        for (int i = first; i < limit; i += step) {
            data[i] = value;
        }
    }

    protected static void invoke(final ComplexNumber[] data, final int first, final int limit, final int step, final Access1D<ComplexNumber> left,
            final BinaryFunction<ComplexNumber> function, final Access1D<ComplexNumber> right) {
        for (int i = first; i < limit; i += step) {
            data[i] = function.invoke(left.get(i), right.get(i));
        }
    }

    protected static void invoke(final ComplexNumber[] data, final int first, final int limit, final int step, final Access1D<ComplexNumber> left,
            final BinaryFunction<ComplexNumber> function, final ComplexNumber right) {
        for (int i = first; i < limit; i += step) {
            data[i] = function.invoke(left.get(i), right);
        }
    }

    protected static void invoke(final ComplexNumber[] data, final int first, final int limit, final int step, final Access1D<ComplexNumber> value,
            final ParameterFunction<ComplexNumber> function, final int aParam) {
        for (int i = first; i < limit; i += step) {
            data[i] = function.invoke(value.get(i), aParam);
        }
    }

    protected static void invoke(final ComplexNumber[] data, final int first, final int limit, final int step, final Access1D<ComplexNumber> value,
            final UnaryFunction<ComplexNumber> function) {
        for (int i = first; i < limit; i += step) {
            data[i] = function.invoke(value.get(i));
        }
    }

    protected static void invoke(final ComplexNumber[] data, final int first, final int limit, final int step, final ComplexNumber left,
            final BinaryFunction<ComplexNumber> function, final Access1D<ComplexNumber> right) {
        for (int i = first; i < limit; i += step) {
            data[i] = function.invoke(left, right.get(i));
        }
    }

    protected static void invoke(final ComplexNumber[] data, final int first, final int limit, final int step, final VoidFunction<ComplexNumber> aVisitor) {
        for (int i = first; i < limit; i += step) {
            aVisitor.invoke(data[i]);
        }
    }

    public final ComplexNumber[] data;

    protected ComplexArray(final ComplexNumber[] data) {

        super();

        this.data = data;
    }

    protected ComplexArray(final int size) {

        super();

        data = new ComplexNumber[size];
        this.fill(0, size, 1, ComplexNumber.ZERO);
    }

    @Override
    public boolean equals(final Object anObj) {
        if (anObj instanceof ComplexArray) {
            return Arrays.equals(data, ((ComplexArray) anObj).data);
        } else {
            return super.equals(anObj);
        }
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }

    public Spliterator<ComplexNumber> spliterator() {
        return Spliterators.spliterator(data, 0, data.length, DenseArray.CHARACTERISTICS);
    }

    protected final ComplexNumber[] copyOfData() {
        return ArrayUtils.copyOf(data);
    }

    @Override
    protected double doubleValue(final int index) {
        return data[index].doubleValue();
    }

    @Override
    protected final void exchange(final int firstA, final int firstB, final int step, final int count) {
        ComplexArray.exchange(data, firstA, firstB, step, count);
    }

    protected void fill(final Access1D<?> value) {
        ComplexArray.fill(data, value);
    }

    @Override
    protected final void fill(final int first, final int limit, final Access1D<ComplexNumber> left, final BinaryFunction<ComplexNumber> function,
            final Access1D<ComplexNumber> right) {
        ComplexArray.invoke(data, first, limit, 1, left, function, right);
    }

    @Override
    protected final void fill(final int first, final int limit, final Access1D<ComplexNumber> left, final BinaryFunction<ComplexNumber> function,
            final ComplexNumber right) {
        ComplexArray.invoke(data, first, limit, 1, left, function, right);
    }

    @Override
    protected final void fill(final int first, final int limit, final ComplexNumber left, final BinaryFunction<ComplexNumber> function,
            final Access1D<ComplexNumber> right) {
        ComplexArray.invoke(data, first, limit, 1, left, function, right);
    }

    @Override
    protected final void fill(final int first, final int limit, final int step, final ComplexNumber value) {
        ComplexArray.fill(data, first, limit, step, value);
    }

    @Override
    protected final ComplexNumber get(final int index) {
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
        return ComplexNumber.isAbsolute(data[index]);
    }

    @Override
    protected boolean isSmall(final int index, final double comparedTo) {
        return ComplexNumber.isSmall(comparedTo, data[index]);
    }

    @Override
    protected void modify(final int index, final Access1D<ComplexNumber> left, final BinaryFunction<ComplexNumber> function) {
        // TODO Auto-generated method stub
    }

    @Override
    protected void modify(final int index, final BinaryFunction<ComplexNumber> function, final Access1D<ComplexNumber> right) {
        // TODO Auto-generated method stub
    }

    @Override
    protected final void modify(final int first, final int limit, final int step, final Access1D<ComplexNumber> left,
            final BinaryFunction<ComplexNumber> function) {
        ComplexArray.invoke(data, first, limit, step, left, function, this);
    }

    @Override
    protected final void modify(final int first, final int limit, final int step, final BinaryFunction<ComplexNumber> function,
            final Access1D<ComplexNumber> right) {
        ComplexArray.invoke(data, first, limit, step, this, function, right);
    }

    @Override
    protected final void modify(final int first, final int limit, final int step, final BinaryFunction<ComplexNumber> function, final ComplexNumber right) {
        ComplexArray.invoke(data, first, limit, step, this, function, right);
    }

    @Override
    protected final void modify(final int first, final int limit, final int step, final ComplexNumber left, final BinaryFunction<ComplexNumber> function) {
        ComplexArray.invoke(data, first, limit, step, left, function, this);
    }

    @Override
    protected final void modify(final int first, final int limit, final int step, final ParameterFunction<ComplexNumber> function, final int parameter) {
        ComplexArray.invoke(data, first, limit, step, this, function, parameter);
    }

    @Override
    protected final void modify(final int first, final int limit, final int step, final UnaryFunction<ComplexNumber> function) {
        ComplexArray.invoke(data, first, limit, step, this, function);
    }

    @Override
    protected void modify(final int index, final UnaryFunction<ComplexNumber> function) {
        data[index] = function.invoke(data[index]);
    }

    /**
     * @see org.ojalgo.array.BasicArray#searchAscending(java.lang.Number)
     */
    @Override
    protected final int searchAscending(final ComplexNumber value) {
        return Arrays.binarySearch(data, value);
    }

    @Override
    protected final void set(final int index, final double value) {
        data[index] = ComplexNumber.valueOf(value);
    }

    @Override
    protected final void set(final int index, final Number value) {
        data[index] = TypeUtils.toComplexNumber(value);
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
    protected final ComplexNumber toScalar(final long index) {
        return data[(int) index];
    }

    @Override
    protected final void visit(final int first, final int limit, final int step, final VoidFunction<ComplexNumber> visitor) {
        ComplexArray.invoke(data, first, limit, step, visitor);
    }

    @Override
    protected final void visit(final int index, final VoidFunction<ComplexNumber> visitor) {
        visitor.invoke(data[index]);
    }

    @Override
    boolean isPrimitive() {
        return false;
    }

    @Override
    DenseArray<ComplexNumber> newInstance(final int capacity) {
        return new ComplexArray(capacity);
    }

}

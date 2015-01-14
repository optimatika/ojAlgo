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

import static org.ojalgo.constant.BigMath.*;

import java.math.BigDecimal;
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
import org.ojalgo.scalar.BigScalar;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.type.TypeUtils;

/**
 * A one- and/or arbitrary-dimensional array of {@linkplain java.math.BigDecimal}.
 *
 * @author apete
 */
public class BigArray extends DenseArray<BigDecimal> {

    static final long ELEMENT_SIZE = MemoryEstimator.estimateObject(BigDecimal.class);

    static final DenseFactory<BigDecimal> FACTORY = new DenseFactory<BigDecimal>() {

        @Override
        long getElementSize() {
            return ELEMENT_SIZE;
        }

        @Override
        DenseArray<BigDecimal> make(final int size) {
            return BigArray.make(size);
        }

        @Override
        Scalar<BigDecimal> zero() {
            return BigScalar.ZERO;
        }

    };

    public static final BigArray make(final int size) {
        return new BigArray(size);
    }

    public static final SegmentedArray<BigDecimal> makeSegmented(final long count) {
        return SegmentedArray.BIG.makeSegmented(FACTORY, count);
    }

    public static final BigArray wrap(final BigDecimal[] data) {
        return new BigArray(data);
    }

    protected static void exchange(final BigDecimal[] data, final int aFirstA, final int aFirstB, final int step, final int aCount) {

        int tmpIndexA = aFirstA;
        int tmpIndexB = aFirstB;

        BigDecimal tmpVal;

        for (int i = 0; i < aCount; i++) {

            tmpVal = data[tmpIndexA];
            data[tmpIndexA] = data[tmpIndexB];
            data[tmpIndexB] = tmpVal;

            tmpIndexA += step;
            tmpIndexB += step;
        }
    }

    protected static void fill(final BigDecimal[] data, final Access1D<?> values) {
        final int tmpLimit = (int) Math.min(data.length, values.count());
        for (int i = 0; i < tmpLimit; i++) {
            data[i] = TypeUtils.toBigDecimal(values.get(i));
        }
    }

    protected static void fill(final BigDecimal[] data, final int first, final int limit, final int step, final BigDecimal number) {
        for (int i = first; i < limit; i += step) {
            data[i] = number;
        }
    }

    protected static void fill(final BigDecimal[] data, final int first, final int limit, final int step, final NullaryFunction<BigDecimal> supplier) {
        for (int i = first; i < limit; i += step) {
            data[i] = supplier.invoke();
        }
    }

    protected static void invoke(final BigDecimal[] data, final int first, final int limit, final int step, final Access1D<BigDecimal> left,
            final BinaryFunction<BigDecimal> function, final Access1D<BigDecimal> right) {
        for (int i = first; i < limit; i += step) {
            data[i] = function.invoke(left.get(i), right.get(i));
        }
    }

    protected static void invoke(final BigDecimal[] data, final int first, final int limit, final int step, final Access1D<BigDecimal> left,
            final BinaryFunction<BigDecimal> function, final BigDecimal right) {
        for (int i = first; i < limit; i += step) {
            data[i] = function.invoke(left.get(i), right);
        }
    }

    protected static void invoke(final BigDecimal[] data, final int first, final int limit, final int step, final Access1D<BigDecimal> values,
            final ParameterFunction<BigDecimal> function, final int aParam) {
        for (int i = first; i < limit; i += step) {
            data[i] = function.invoke(values.get(i), aParam);
        }
    }

    protected static void invoke(final BigDecimal[] data, final int first, final int limit, final int step, final Access1D<BigDecimal> values,
            final UnaryFunction<BigDecimal> function) {
        for (int i = first; i < limit; i += step) {
            data[i] = function.invoke(values.get(i));
        }
    }

    protected static void invoke(final BigDecimal[] data, final int first, final int limit, final int step, final BigDecimal left,
            final BinaryFunction<BigDecimal> function, final Access1D<BigDecimal> right) {
        for (int i = first; i < limit; i += step) {
            data[i] = function.invoke(left, right.get(i));
        }
    }

    protected static void invoke(final BigDecimal[] data, final int first, final int limit, final int step, final VoidFunction<BigDecimal> visitor) {
        for (int i = first; i < limit; i += step) {
            visitor.invoke(data[i]);
        }
    }

    public final BigDecimal[] data;

    protected BigArray(final BigDecimal[] data) {

        super();

        this.data = data;
    }

    protected BigArray(final int size) {

        super();

        data = new BigDecimal[size];
        this.fill(0, size, 1, ZERO);
    }

    @Override
    public boolean equals(final Object anObj) {
        if (anObj instanceof BigArray) {
            return Arrays.equals(data, ((BigArray) anObj).data);
        } else {
            return super.equals(anObj);
        }
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }

    public Spliterator<BigDecimal> spliterator() {
        return Spliterators.spliterator(data, 0, data.length, DenseArray.CHARACTERISTICS);
    }

    protected final BigDecimal[] copyOfData() {
        return ArrayUtils.copyOf(data);
    }

    @Override
    protected final double doubleValue(final int index) {
        return data[index].doubleValue();
    }

    @Override
    protected final void exchange(final int firstA, final int firstB, final int step, final int count) {
        BigArray.exchange(data, firstA, firstB, step, count);
    }

    protected void fill(final Access1D<?> values) {
        BigArray.fill(data, values);
    }

    @Override
    protected final void fill(final int first, final int limit, final Access1D<BigDecimal> left, final BinaryFunction<BigDecimal> function,
            final Access1D<BigDecimal> right) {
        BigArray.invoke(data, first, limit, 1, left, function, right);
    }

    @Override
    protected final void fill(final int first, final int limit, final Access1D<BigDecimal> left, final BinaryFunction<BigDecimal> function,
            final BigDecimal right) {
        BigArray.invoke(data, first, limit, 1, left, function, right);
    }

    @Override
    protected final void fill(final int first, final int limit, final BigDecimal left, final BinaryFunction<BigDecimal> function,
            final Access1D<BigDecimal> right) {
        BigArray.invoke(data, first, limit, 1, left, function, right);
    }

    @Override
    protected final void fill(final int first, final int limit, final int step, final BigDecimal value) {
        BigArray.fill(data, first, limit, step, value);
    }

    @Override
    protected final void fill(final int first, final int limit, final int step, final NullaryFunction<BigDecimal> supplier) {
        BigArray.fill(data, first, limit, step, supplier);
    }

    @Override
    protected final BigDecimal get(final int index) {
        return data[index];
    }

    @Override
    protected final int indexOfLargest(final int first, final int limit, final int step) {

        int retVal = first;
        BigDecimal tmpLargest = ZERO;
        BigDecimal tmpValue;

        for (int i = first; i < limit; i += step) {
            tmpValue = data[i].abs();
            if (tmpValue.compareTo(tmpLargest) == 1) {
                tmpLargest = tmpValue;
                retVal = i;
            }
        }

        return retVal;
    }

    @Override
    protected boolean isAbsolute(final int index) {
        return BigScalar.isAbsolute(data[index]);
    }

    @Override
    protected boolean isSmall(final int index, final double comparedTo) {
        return BigScalar.isSmall(comparedTo, data[index]);
    }

    @Override
    protected void modify(final int index, final Access1D<BigDecimal> left, final BinaryFunction<BigDecimal> function) {
        // TODO Auto-generated method stub
    }

    @Override
    protected void modify(final int index, final BinaryFunction<BigDecimal> function, final Access1D<BigDecimal> right) {
        // TODO Auto-generated method stub
    }

    @Override
    protected final void modify(final int first, final int limit, final int step, final Access1D<BigDecimal> left, final BinaryFunction<BigDecimal> function) {
        BigArray.invoke(data, first, limit, step, left, function, this);
    }

    @Override
    protected final void modify(final int first, final int limit, final int step, final BigDecimal left, final BinaryFunction<BigDecimal> function) {
        BigArray.invoke(data, first, limit, step, left, function, this);
    }

    @Override
    protected final void modify(final int first, final int limit, final int step, final BinaryFunction<BigDecimal> function, final Access1D<BigDecimal> right) {
        BigArray.invoke(data, first, limit, step, this, function, right);
    }

    @Override
    protected final void modify(final int first, final int limit, final int step, final BinaryFunction<BigDecimal> function, final BigDecimal right) {
        BigArray.invoke(data, first, limit, step, this, function, right);
    }

    @Override
    protected final void modify(final int first, final int limit, final int step, final ParameterFunction<BigDecimal> function, final int parameter) {
        BigArray.invoke(data, first, limit, step, this, function, parameter);
    }

    @Override
    protected final void modify(final int first, final int limit, final int step, final UnaryFunction<BigDecimal> function) {
        BigArray.invoke(data, first, limit, step, this, function);
    }

    @Override
    protected void modify(final int index, final UnaryFunction<BigDecimal> function) {
        data[index] = function.invoke(data[index]);
    }

    /**
     * @see org.ojalgo.array.BasicArray#searchAscending(java.lang.Number)
     */
    @Override
    protected final int searchAscending(final BigDecimal number) {
        return Arrays.binarySearch(data, number);
    }

    @Override
    protected final void set(final int index, final double value) {
        data[index] = new BigDecimal(value);
    }

    @Override
    protected final void set(final int index, final Number value) {
        data[index] = TypeUtils.toBigDecimal(value);
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
    protected final Scalar<BigDecimal> toScalar(final long index) {
        return new BigScalar(data[(int) index]);
    }

    @Override
    protected final void visit(final int first, final int limit, final int step, final VoidFunction<BigDecimal> visitor) {
        BigArray.invoke(data, first, limit, step, visitor);
    }

    @Override
    protected final void visit(final int index, final VoidFunction<BigDecimal> visitor) {
        visitor.invoke(data[index]);
    }

    @Override
    boolean isPrimitive() {
        return false;
    }

    @Override
    DenseArray<BigDecimal> newInstance(final int capacity) {
        return new BigArray(capacity);
    }

}

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
import java.util.Spliterator.OfDouble;
import java.util.Spliterators;
import java.util.stream.DoubleStream;
import java.util.stream.StreamSupport;

import org.ojalgo.access.Access1D;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.BinaryFunction.FixedFirst;
import org.ojalgo.function.BinaryFunction.FixedSecond;
import org.ojalgo.function.FunctionUtils;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.ParameterFunction;
import org.ojalgo.function.ParameterFunction.FixedParameter;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.machine.JavaType;
import org.ojalgo.scalar.PrimitiveScalar;
import org.ojalgo.scalar.Scalar;

/**
 * A one- and/or arbitrary-dimensional array of double.
 *
 * @author apete
 */
public class PrimitiveArray extends DenseArray<Double> {

    static final long ELEMENT_SIZE = JavaType.DOUBLE.memory();

    static final DenseFactory<Double> FACTORY = new DenseFactory<Double>() {

        @Override
        public final BasicArray<Double> makeFilled(final long count, final NullaryFunction<?> supplier) {
            final int tmpSize = (int) count;
            final double[] tmpData = new double[tmpSize];
            for (int i = 0; i < tmpSize; i++) {
                tmpData[i] = supplier.doubleValue();
            }
            return new PrimitiveArray(tmpData);
        }

        @Override
        long getElementSize() {
            return ELEMENT_SIZE;
        }

        @Override
        DenseArray<Double> make(final int size) {
            return PrimitiveArray.make(size);
        }

        @Override
        Scalar<Double> zero() {
            return PrimitiveScalar.ZERO;
        }

    };

    public static final PrimitiveArray make(final int size) {
        return new PrimitiveArray(size);
    }

    public static final SegmentedArray<Double> makeSegmented(final int size) {
        return SegmentedArray.make(FACTORY, size);
    }

    public static final SegmentedArray<Double> makeSegmented(final long count) {
        return SegmentedArray.make(FACTORY, count);
    }

    public static final PrimitiveArray wrap(final double[] data) {
        return new PrimitiveArray(data);
    }

    private static void add(final double[] data, final int first, final int limit, final int step, final double left, final double[] right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left + right[i];
        }
    }

    private static void add(final double[] data, final int first, final int limit, final int step, final double[] left, final double right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left[i] + right;
        }
    }

    private static void add(final double[] data, final int first, final int limit, final int step, final double[] left, final double[] right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left[i] + right[i];
        }
    }

    private static void divide(final double[] data, final int first, final int limit, final int step, final double left, final double[] right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left / right[i];
        }
    }

    private static void divide(final double[] data, final int first, final int limit, final int step, final double[] left, final double right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left[i] / right;
        }
    }

    private static void divide(final double[] data, final int first, final int limit, final int step, final double[] left, final double[] right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left[i] / right[i];
        }
    }

    private static void multiply(final double[] data, final int first, final int limit, final int step, final double left, final double[] right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left * right[i];
        }
    }

    private static void multiply(final double[] data, final int first, final int limit, final int step, final double[] left, final double right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left[i] * right;
        }
    }

    private static void multiply(final double[] data, final int first, final int limit, final int step, final double[] left, final double[] right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left[i] * right[i];
        }
    }

    private static void negate(final double[] data, final int first, final int limit, final int step, final double[] values) {
        for (int i = first; i < limit; i += step) {
            data[i] = -values[i];
        }
    }

    private static void subtract(final double[] data, final int first, final int limit, final int step, final double left, final double[] right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left - right[i];
        }
    }

    private static void subtract(final double[] data, final int first, final int limit, final int step, final double[] left, final double right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left[i] - right;
        }
    }

    private static void subtract(final double[] data, final int first, final int limit, final int step, final double[] left, final double[] right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left[i] - right[i];
        }
    }

    protected static void exchange(final double[] data, final int firstA, final int firstB, final int step, final int count) {

        int tmpIndexA = firstA;
        int tmpIndexB = firstB;

        double tmpVal;

        for (int i = 0; i < count; i++) {

            tmpVal = data[tmpIndexA];
            data[tmpIndexA] = data[tmpIndexB];
            data[tmpIndexB] = tmpVal;

            tmpIndexA += step;
            tmpIndexB += step;
        }
    }

    protected static void fill(final double[] data, final Access1D<?> values) {
        final int tmpLimit = (int) Math.min(data.length, values.count());
        for (int i = 0; i < tmpLimit; i++) {
            data[i] = values.doubleValue(i);
        }
    }

    protected static void fill(final double[] data, final int first, final int limit, final int step, final double value) {
        for (int i = first; i < limit; i += step) {
            data[i] = value;
        }
    }

    protected static void fill(final double[] data, final int first, final int limit, final int step, final NullaryFunction<Double> supplier) {
        for (int i = first; i < limit; i += step) {
            data[i] = supplier.doubleValue();
        }
    }

    protected static void invoke(final double[] data, final int first, final int limit, final int step, final Access1D<Double> left,
            final BinaryFunction<Double> function, final Access1D<Double> right) {
        if ((left instanceof PrimitiveArray) && (right instanceof PrimitiveArray)) {
            PrimitiveArray.invoke(data, first, limit, step, ((PrimitiveArray) left).data, function, ((PrimitiveArray) right).data);
        } else {
            for (int i = first; i < limit; i += step) {
                data[i] = function.invoke(left.doubleValue(i), right.doubleValue(i));
            }
        }
    }

    protected static void invoke(final double[] data, final int first, final int limit, final int step, final Access1D<Double> left,
            final BinaryFunction<Double> function, final double right) {
        if (left instanceof PrimitiveArray) {
            PrimitiveArray.invoke(data, first, limit, step, ((PrimitiveArray) left).data, function, right);
        } else {
            for (int i = first; i < limit; i += step) {
                data[i] = function.invoke(left.doubleValue(i), right);
            }
        }
    }

    protected static void invoke(final double[] data, final int first, final int limit, final int step, final Access1D<Double> values,
            final ParameterFunction<Double> function, final int aParam) {
        if (values instanceof PrimitiveArray) {
            PrimitiveArray.invoke(data, first, limit, step, ((PrimitiveArray) values).data, function, aParam);
        } else {
            for (int i = first; i < limit; i += step) {
                data[i] = function.invoke(values.doubleValue(i), aParam);
            }
        }
    }

    protected static void invoke(final double[] data, final int first, final int limit, final int step, final Access1D<Double> values,
            final UnaryFunction<Double> function) {
        if (values instanceof PrimitiveArray) {
            PrimitiveArray.invoke(data, first, limit, step, ((PrimitiveArray) values).data, function);
        } else {
            for (int i = first; i < limit; i += step) {
                data[i] = function.invoke(values.doubleValue(i));
            }
        }
    }

    protected static void invoke(final double[] data, final int first, final int limit, final int step, final double left,
            final BinaryFunction<Double> function, final Access1D<Double> right) {
        if (right instanceof PrimitiveArray) {
            PrimitiveArray.invoke(data, first, limit, step, left, function, ((PrimitiveArray) right).data);
        } else {
            for (int i = first; i < limit; i += step) {
                data[i] = function.invoke(left, right.doubleValue(i));
            }
        }
    }

    protected static void invoke(final double[] data, final int first, final int limit, final int step, final VoidFunction<Double> aVisitor) {
        for (int i = first; i < limit; i += step) {
            aVisitor.invoke(data[i]);
        }
    }

    static void invoke(final double[] data, final int first, final int limit, final int step, final double left, final BinaryFunction<Double> function,
            final double[] right) {
        if (function == PrimitiveFunction.ADD) {
            PrimitiveArray.add(data, first, limit, step, left, right);
        } else if (function == PrimitiveFunction.DIVIDE) {
            PrimitiveArray.divide(data, first, limit, step, left, right);
        } else if (function == PrimitiveFunction.MULTIPLY) {
            PrimitiveArray.multiply(data, first, limit, step, left, right);
        } else if (function == PrimitiveFunction.SUBTRACT) {
            PrimitiveArray.subtract(data, first, limit, step, left, right);
        } else {
            for (int i = first; i < limit; i += step) {
                data[i] = function.invoke(left, right[i]);
            }
        }
    }

    static void invoke(final double[] data, final int first, final int limit, final int step, final double[] left, final BinaryFunction<Double> function,
            final double right) {
        if (function == PrimitiveFunction.ADD) {
            PrimitiveArray.add(data, first, limit, step, left, right);
        } else if (function == PrimitiveFunction.DIVIDE) {
            PrimitiveArray.divide(data, first, limit, step, left, right);
        } else if (function == PrimitiveFunction.MULTIPLY) {
            PrimitiveArray.multiply(data, first, limit, step, left, right);
        } else if (function == PrimitiveFunction.SUBTRACT) {
            PrimitiveArray.subtract(data, first, limit, step, left, right);
        } else {
            for (int i = first; i < limit; i += step) {
                data[i] = function.invoke(left[i], right);
            }
        }
    }

    static void invoke(final double[] data, final int first, final int limit, final int step, final double[] left, final BinaryFunction<Double> function,
            final double[] right) {
        if (function == PrimitiveFunction.ADD) {
            PrimitiveArray.add(data, first, limit, step, left, right);
        } else if (function == PrimitiveFunction.DIVIDE) {
            PrimitiveArray.divide(data, first, limit, step, left, right);
        } else if (function == PrimitiveFunction.MULTIPLY) {
            PrimitiveArray.multiply(data, first, limit, step, left, right);
        } else if (function == PrimitiveFunction.SUBTRACT) {
            PrimitiveArray.subtract(data, first, limit, step, left, right);
        } else {
            for (int i = first; i < limit; i += step) {
                data[i] = function.invoke(left[i], right[i]);
            }
        }
    }

    static void invoke(final double[] data, final int first, final int limit, final int step, final double[] values, final ParameterFunction<Double> function,
            final int aParam) {
        for (int i = first; i < limit; i += step) {
            data[i] = function.invoke(values[i], aParam);
        }
    }

    static void invoke(final double[] data, final int first, final int limit, final int step, final double[] values, final UnaryFunction<Double> function) {
        if (function == PrimitiveFunction.NEGATE) {
            PrimitiveArray.negate(data, first, limit, step, values);
        } else if (function instanceof FixedFirst<?>) {
            final FixedFirst<Double> tmpFunc = (FixedFirst<Double>) function;
            PrimitiveArray.invoke(data, first, limit, step, tmpFunc.doubleValue(), tmpFunc.getFunction(), values);
        } else if (function instanceof FixedSecond<?>) {
            final FixedSecond<Double> tmpFunc = (FixedSecond<Double>) function;
            PrimitiveArray.invoke(data, first, limit, step, values, tmpFunc.getFunction(), tmpFunc.doubleValue());
        } else if (function instanceof FixedParameter<?>) {
            final FixedParameter<Double> tmpFunc = (FixedParameter<Double>) function;
            PrimitiveArray.invoke(data, first, limit, step, values, tmpFunc.getFunction(), tmpFunc.getParameter());
        } else {
            for (int i = first; i < limit; i += step) {
                data[i] = function.invoke(values[i]);
            }
        }
    }

    public final double[] data;

    /**
     * Array not copied! No checking!
     */
    protected PrimitiveArray(final double[] data) {

        super();

        this.data = data;
    }

    protected PrimitiveArray(final int size) {

        super();

        data = new double[size];
    }

    @Override
    public boolean equals(final Object anObj) {
        if (anObj instanceof PrimitiveArray) {
            return Arrays.equals(data, ((PrimitiveArray) anObj).data);
        } else {
            return super.equals(anObj);
        }
    }

    public final void fillMatching(final Access1D<?> values) {
        PrimitiveArray.fill(data, values);
    }

    public final void fillMatching(final Access1D<Double> left, final BinaryFunction<Double> function, final Access1D<Double> right) {
        final int tmpLimit = (int) FunctionUtils.min(this.count(), left.count(), right.count());
        PrimitiveArray.invoke(data, 0, tmpLimit, 1, left, function, right);
    }

    public final void fillMatching(final UnaryFunction<Double> function, final Access1D<Double> arguments) {
        final int tmpLimit = (int) FunctionUtils.min(this.count(), arguments.count());
        PrimitiveArray.invoke(data, 0, tmpLimit, 1, arguments, function);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }

    public OfDouble spliterator() {
        return Spliterators.spliterator(data, 0, data.length, DenseArray.CHARACTERISTICS);
    }

    public DoubleStream stream(final boolean parallel) {
        return StreamSupport.doubleStream(this.spliterator(), parallel);
    }

    @Override
    protected void add(final int index, final double addend) {
        data[index] += addend;
    }

    @Override
    protected void add(final int index, final Number addend) {
        data[index] += addend.doubleValue();
    }

    protected final double[] copyOfData() {
        return ArrayUtils.copyOf(data);
    }

    @Override
    protected final double doubleValue(final int index) {
        return data[index];
    }

    @Override
    protected final void exchange(final int firstA, final int firstB, final int step, final int count) {
        PrimitiveArray.exchange(data, firstA, firstB, step, count);
    }

    @Override
    protected final void fill(final int first, final int limit, final Access1D<Double> left, final BinaryFunction<Double> function,
            final Access1D<Double> right) {
        PrimitiveArray.invoke(data, first, limit, 1, left, function, right);
    }

    @Override
    protected final void fill(final int first, final int limit, final Access1D<Double> left, final BinaryFunction<Double> function, final Double right) {
        PrimitiveArray.invoke(data, first, limit, 1, left, function, right);
    }

    @Override
    protected final void fill(final int first, final int limit, final Double left, final BinaryFunction<Double> function, final Access1D<Double> right) {
        PrimitiveArray.invoke(data, first, limit, 1, left, function, right);
    }

    @Override
    protected final void fill(final int first, final int limit, final int step, final Double value) {
        PrimitiveArray.fill(data, first, limit, step, value);
    }

    @Override
    protected final void fill(final int first, final int limit, final int step, final NullaryFunction<Double> supplier) {
        PrimitiveArray.fill(data, first, limit, step, supplier);
    }

    @Override
    protected void fillOne(final int index, final Double value) {
        data[index] = value;
    }

    @Override
    protected void fillOne(final int index, final NullaryFunction<Double> supplier) {
        data[index] = supplier.doubleValue();
    }

    @Override
    protected void fillOneMatching(final int index, final Access1D<?> values, final long valueIndex) {
        data[index] = values.doubleValue(valueIndex);
    }

    @Override
    protected final Double get(final int index) {
        return data[index];
    }

    @Override
    protected final int indexOfLargest(final int first, final int limit, final int step) {

        int retVal = first;
        double tmpLargest = ZERO;
        double tmpValue;

        for (int i = first; i < limit; i += step) {
            tmpValue = Math.abs(data[i]);
            if (tmpValue > tmpLargest) {
                tmpLargest = tmpValue;
                retVal = i;
            }
        }

        return retVal;
    }

    @Override
    protected boolean isAbsolute(final int index) {
        return PrimitiveScalar.isAbsolute(data[index]);
    }

    @Override
    protected boolean isSmall(final int index, final double comparedTo) {
        return PrimitiveScalar.isSmall(comparedTo, data[index]);
    }

    @Override
    protected void modify(final int index, final Access1D<Double> left, final BinaryFunction<Double> function) {
        data[index] = function.invoke(left.doubleValue(index), data[index]);
    }

    @Override
    protected void modify(final int index, final BinaryFunction<Double> function, final Access1D<Double> right) {
        data[index] = function.invoke(data[index], right.doubleValue(index));
    }

    @Override
    protected final void modify(final int first, final int limit, final int step, final Access1D<Double> left, final BinaryFunction<Double> function) {
        PrimitiveArray.invoke(data, first, limit, step, left, function, this);
    }

    @Override
    protected final void modify(final int first, final int limit, final int step, final BinaryFunction<Double> function, final Access1D<Double> right) {
        PrimitiveArray.invoke(data, first, limit, step, this, function, right);
    }

    @Override
    protected final void modify(final int first, final int limit, final int step, final BinaryFunction<Double> function, final Double right) {
        PrimitiveArray.invoke(data, first, limit, step, data, function, right);
    }

    @Override
    protected final void modify(final int first, final int limit, final int step, final Double left, final BinaryFunction<Double> function) {
        PrimitiveArray.invoke(data, first, limit, step, left, function, data);
    }

    @Override
    protected final void modify(final int first, final int limit, final int step, final ParameterFunction<Double> function, final int parameter) {
        PrimitiveArray.invoke(data, first, limit, step, data, function, parameter);
    }

    @Override
    protected final void modify(final int first, final int limit, final int step, final UnaryFunction<Double> function) {
        PrimitiveArray.invoke(data, first, limit, step, this, function);
    }

    @Override
    protected void modify(final int index, final UnaryFunction<Double> function) {
        data[index] = function.invoke(data[index]);
    }

    @Override
    protected final int searchAscending(final Double aNmbr) {
        return Arrays.binarySearch(data, aNmbr.doubleValue());
    }

    @Override
    protected final void set(final int index, final double value) {
        data[index] = value;
    }

    @Override
    protected final void set(final int index, final Number value) {
        data[index] = value.doubleValue();
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
    protected final void visit(final int first, final int limit, final int step, final VoidFunction<Double> visitor) {
        PrimitiveArray.invoke(data, first, limit, step, visitor);
    }

    @Override
    protected void visitOne(final int index, final VoidFunction<Double> visitor) {
        visitor.invoke(data[index]);
    }

    @Override
    boolean isPrimitive() {
        return true;
    }

    @Override
    DenseArray<Double> newInstance(final int capacity) {
        return new PrimitiveArray(capacity);
    }

    OfDouble split() {
        return Spliterators.spliterator(data, 0);
    }

}

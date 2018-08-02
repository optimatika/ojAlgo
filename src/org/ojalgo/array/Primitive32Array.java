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

import static org.ojalgo.constant.PrimitiveMath.*;

import java.util.Arrays;

import org.ojalgo.access.Access1D;
import org.ojalgo.access.Mutate1D;
import org.ojalgo.array.blas.AMAX;
import org.ojalgo.array.blas.AXPY;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.BinaryFunction.FixedFirst;
import org.ojalgo.function.BinaryFunction.FixedSecond;
import org.ojalgo.function.FunctionSet;
import org.ojalgo.function.FunctionUtils;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.ParameterFunction;
import org.ojalgo.function.ParameterFunction.FixedParameter;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.function.aggregator.AggregatorSet;
import org.ojalgo.function.aggregator.PrimitiveAggregator;
import org.ojalgo.machine.JavaType;
import org.ojalgo.scalar.PrimitiveScalar;
import org.ojalgo.scalar.Scalar;

/**
 * A one- and/or arbitrary-dimensional array of double.
 *
 * @author apete
 */
public class Primitive32Array extends PrimitiveArray {

    public static final DenseArray.Factory<Double> FACTORY = new DenseArray.Factory<Double>() {

        @Override
        public AggregatorSet<Double> aggregator() {
            return PrimitiveAggregator.getSet();
        }

        @Override
        public FunctionSet<Double> function() {
            return PrimitiveFunction.getSet();
        }

        @Override
        public Scalar.Factory<Double> scalar() {
            return PrimitiveScalar.FACTORY;
        }

        @Override
        long getElementSize() {
            return ELEMENT_SIZE;
        }

        @Override
        PlainArray<Double> make(final long size) {
            return Primitive32Array.make((int) size);
        }

    };

    static final long ELEMENT_SIZE = JavaType.FLOAT.memory();

    public static final Primitive32Array make(final int size) {
        return new Primitive32Array(size);
    }

    public static final Primitive32Array wrap(final float... data) {
        return new Primitive32Array(data);
    }

    private static void add(final float[] data, final int first, final int limit, final int step, final float left, final float[] right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left + right[i];
        }
    }

    private static void add(final float[] data, final int first, final int limit, final int step, final float[] left, final float right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left[i] + right;
        }
    }

    private static void add(final float[] data, final int first, final int limit, final int step, final float[] left, final float[] right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left[i] + right[i];
        }
    }

    private static void divide(final float[] data, final int first, final int limit, final int step, final float left, final float[] right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left / right[i];
        }
    }

    private static void divide(final float[] data, final int first, final int limit, final int step, final float[] left, final float right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left[i] / right;
        }
    }

    private static void divide(final float[] data, final int first, final int limit, final int step, final float[] left, final float[] right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left[i] / right[i];
        }
    }

    private static void multiply(final float[] data, final int first, final int limit, final int step, final float left, final float[] right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left * right[i];
        }
    }

    private static void multiply(final float[] data, final int first, final int limit, final int step, final float[] left, final float right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left[i] * right;
        }
    }

    private static void multiply(final float[] data, final int first, final int limit, final int step, final float[] left, final float[] right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left[i] * right[i];
        }
    }

    private static void negate(final float[] data, final int first, final int limit, final int step, final float[] values) {
        for (int i = first; i < limit; i += step) {
            data[i] = -values[i];
        }
    }

    private static void subtract(final float[] data, final int first, final int limit, final int step, final float left, final float[] right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left - right[i];
        }
    }

    private static void subtract(final float[] data, final int first, final int limit, final int step, final float[] left, final float right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left[i] - right;
        }
    }

    private static void subtract(final float[] data, final int first, final int limit, final int step, final float[] left, final float[] right) {
        for (int i = first; i < limit; i += step) {
            data[i] = left[i] - right[i];
        }
    }

    protected static void exchange(final float[] data, final int firstA, final int firstB, final int step, final int count) {

        int tmpIndexA = firstA;
        int tmpIndexB = firstB;

        float tmpVal;

        for (int i = 0; i < count; i++) {

            tmpVal = data[tmpIndexA];
            data[tmpIndexA] = data[tmpIndexB];
            data[tmpIndexB] = tmpVal;

            tmpIndexA += step;
            tmpIndexB += step;
        }
    }

    protected static void fill(final float[] data, final Access1D<?> values) {
        final int tmpLimit = (int) Math.min(data.length, values.count());
        for (int i = 0; i < tmpLimit; i++) {
            data[i] = (float) values.doubleValue(i);
        }
    }

    protected static void fill(final float[] data, final int first, final int limit, final int step, final float value) {
        for (int i = first; i < limit; i += step) {
            data[i] = value;
        }
    }

    protected static void fill(final float[] data, final int first, final int limit, final int step, final NullaryFunction<Double> supplier) {
        for (int i = first; i < limit; i += step) {
            data[i] = (float) supplier.doubleValue();
        }
    }

    protected static void invoke(final float[] data, final int first, final int limit, final int step, final Access1D<Double> left,
            final BinaryFunction<Double> function, final Access1D<Double> right) {
        if ((left instanceof Primitive32Array) && (right instanceof Primitive32Array)) {
            Primitive32Array.invoke(data, first, limit, step, ((Primitive32Array) left).data, function, ((Primitive32Array) right).data);
        } else {
            for (int i = first; i < limit; i += step) {
                data[i] = (float) function.invoke(left.doubleValue(i), right.doubleValue(i));
            }
        }
    }

    protected static void invoke(final float[] data, final int first, final int limit, final int step, final Access1D<Double> left,
            final BinaryFunction<Double> function, final double right) {
        if (left instanceof Primitive32Array) {
            Primitive32Array.invoke(data, first, limit, step, ((Primitive32Array) left).data, function, right);
        } else {
            for (int i = first; i < limit; i += step) {
                data[i] = (float) function.invoke(left.doubleValue(i), right);
            }
        }
    }

    protected static void invoke(final float[] data, final int first, final int limit, final int step, final Access1D<Double> values,
            final ParameterFunction<Double> function, final int aParam) {
        if (values instanceof Primitive32Array) {
            Primitive32Array.invoke(data, first, limit, step, ((Primitive32Array) values).data, function, aParam);
        } else {
            for (int i = first; i < limit; i += step) {
                data[i] = (float) function.invoke(values.doubleValue(i), aParam);
            }
        }
    }

    protected static void invoke(final float[] data, final int first, final int limit, final int step, final Access1D<Double> values,
            final UnaryFunction<Double> function) {
        if (values instanceof Primitive32Array) {
            Primitive32Array.invoke(data, first, limit, step, ((Primitive32Array) values).data, function);
        } else {
            for (int i = first; i < limit; i += step) {
                data[i] = (float) function.invoke(values.doubleValue(i));
            }
        }
    }

    protected static void invoke(final float[] data, final int first, final int limit, final int step, final double left, final BinaryFunction<Double> function,
            final Access1D<Double> right) {
        if (right instanceof Primitive32Array) {
            Primitive32Array.invoke(data, first, limit, step, left, function, ((Primitive32Array) right).data);
        } else {
            for (int i = first; i < limit; i += step) {
                data[i] = (float) function.invoke(left, right.doubleValue(i));
            }
        }
    }

    protected static void invoke(final float[] data, final int first, final int limit, final int step, final VoidFunction<Double> aVisitor) {
        for (int i = first; i < limit; i += step) {
            aVisitor.invoke(data[i]);
        }
    }

    static void invoke(final float[] data, final int first, final int limit, final int step, final double left, final BinaryFunction<Double> function,
            final float[] right) {
        if (function == PrimitiveFunction.ADD) {
            Primitive32Array.add(data, first, limit, step, (float) left, right);
        } else if (function == PrimitiveFunction.DIVIDE) {
            Primitive32Array.divide(data, first, limit, step, (float) left, right);
        } else if (function == PrimitiveFunction.MULTIPLY) {
            Primitive32Array.multiply(data, first, limit, step, (float) left, right);
        } else if (function == PrimitiveFunction.SUBTRACT) {
            Primitive32Array.subtract(data, first, limit, step, (float) left, right);
        } else {
            for (int i = first; i < limit; i += step) {
                data[i] = (float) function.invoke(left, right[i]);
            }
        }
    }

    static void invoke(final float[] data, final int first, final int limit, final int step, final float[] left, final BinaryFunction<Double> function,
            final double right) {
        if (function == PrimitiveFunction.ADD) {
            Primitive32Array.add(data, first, limit, step, left, (float) right);
        } else if (function == PrimitiveFunction.DIVIDE) {
            Primitive32Array.divide(data, first, limit, step, left, (float) right);
        } else if (function == PrimitiveFunction.MULTIPLY) {
            Primitive32Array.multiply(data, first, limit, step, left, (float) right);
        } else if (function == PrimitiveFunction.SUBTRACT) {
            Primitive32Array.subtract(data, first, limit, step, left, (float) right);
        } else {
            for (int i = first; i < limit; i += step) {
                data[i] = (float) function.invoke(left[i], right);
            }
        }
    }

    static void invoke(final float[] data, final int first, final int limit, final int step, final float[] left, final BinaryFunction<Double> function,
            final float[] right) {
        if (function == PrimitiveFunction.ADD) {
            Primitive32Array.add(data, first, limit, step, left, right);
        } else if (function == PrimitiveFunction.DIVIDE) {
            Primitive32Array.divide(data, first, limit, step, left, right);
        } else if (function == PrimitiveFunction.MULTIPLY) {
            Primitive32Array.multiply(data, first, limit, step, left, right);
        } else if (function == PrimitiveFunction.SUBTRACT) {
            Primitive32Array.subtract(data, first, limit, step, left, right);
        } else {
            for (int i = first; i < limit; i += step) {
                data[i] = (float) function.invoke(left[i], right[i]);
            }
        }
    }

    static void invoke(final float[] data, final int first, final int limit, final int step, final float[] values, final ParameterFunction<Double> function,
            final int aParam) {
        for (int i = first; i < limit; i += step) {
            data[i] = (float) function.invoke(values[i], aParam);
        }
    }

    static void invoke(final float[] data, final int first, final int limit, final int step, final float[] values, final UnaryFunction<Double> function) {
        if (function == PrimitiveFunction.NEGATE) {
            Primitive32Array.negate(data, first, limit, step, values);
        } else if (function instanceof FixedFirst<?>) {
            final FixedFirst<Double> tmpFunc = (FixedFirst<Double>) function;
            Primitive32Array.invoke(data, first, limit, step, tmpFunc.doubleValue(), tmpFunc.getFunction(), values);
        } else if (function instanceof FixedSecond<?>) {
            final FixedSecond<Double> tmpFunc = (FixedSecond<Double>) function;
            Primitive32Array.invoke(data, first, limit, step, values, tmpFunc.getFunction(), tmpFunc.doubleValue());
        } else if (function instanceof FixedParameter<?>) {
            final FixedParameter<Double> tmpFunc = (FixedParameter<Double>) function;
            Primitive32Array.invoke(data, first, limit, step, values, tmpFunc.getFunction(), tmpFunc.getParameter());
        } else {
            for (int i = first; i < limit; i += step) {
                data[i] = (float) function.invoke(values[i]);
            }
        }
    }

    public final float[] data;

    /**
     * Array not copied! No checking!
     */
    protected Primitive32Array(final float[] data) {

        super(FACTORY, data.length);

        this.data = data;
    }

    protected Primitive32Array(final int size) {

        super(FACTORY, size);

        data = new float[size];
    }

    public void axpy(final double a, final Mutate1D y) {
        AXPY.invoke(y, a, data);
    }

    @Override
    public double dot(final Access1D<?> vector) {

        double retVal = ZERO;

        for (int i = 0, limit = Math.min(data.length, (int) vector.count()); i < limit; i++) {
            retVal += data[i] * vector.doubleValue(i);
        }

        return retVal;
    }

    @Override
    public boolean equals(final Object anObj) {
        if (anObj instanceof Primitive32Array) {
            return Arrays.equals(data, ((Primitive32Array) anObj).data);
        } else {
            return super.equals(anObj);
        }
    }

    public void fillMatching(final Access1D<?> values) {
        Primitive32Array.fill(data, values);
    }

    public void fillMatching(final Access1D<Double> left, final BinaryFunction<Double> function, final Access1D<Double> right) {
        final int tmpLimit = (int) FunctionUtils.min(this.count(), left.count(), right.count());
        Primitive32Array.invoke(data, 0, tmpLimit, 1, left, function, right);
    }

    public void fillMatching(final UnaryFunction<Double> function, final Access1D<Double> arguments) {
        final int tmpLimit = (int) FunctionUtils.min(this.count(), arguments.count());
        Primitive32Array.invoke(data, 0, tmpLimit, 1, arguments, function);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }

    @Override
    public final void sortAscending() {
        Arrays.parallelSort(data);
    }

    @Override
    public void sortDescending() {
        Primitive32Array.negate(data, 0, data.length, 1, data);
        Arrays.parallelSort(data);
        Primitive32Array.negate(data, 0, data.length, 1, data);
    }

    @Override
    protected void add(final int index, final double addend) {
        data[index] += addend;
    }

    @Override
    protected void add(final int index, final Number addend) {
        data[index] += addend.doubleValue();
    }

    protected final float[] copyOfData() {
        return Raw1D.copyOf(data);
    }

    @Override
    protected final double doubleValue(final int index) {
        return data[index];
    }

    @Override
    protected final void exchange(final int firstA, final int firstB, final int step, final int count) {
        Primitive32Array.exchange(data, firstA, firstB, step, count);
    }

    @Override
    protected final void fill(final int first, final int limit, final Access1D<Double> left, final BinaryFunction<Double> function,
            final Access1D<Double> right) {
        Primitive32Array.invoke(data, first, limit, 1, left, function, right);
    }

    @Override
    protected final void fill(final int first, final int limit, final Access1D<Double> left, final BinaryFunction<Double> function, final Double right) {
        Primitive32Array.invoke(data, first, limit, 1, left, function, right);
    }

    @Override
    protected final void fill(final int first, final int limit, final Double left, final BinaryFunction<Double> function, final Access1D<Double> right) {
        Primitive32Array.invoke(data, first, limit, 1, left, function, right);
    }

    @Override
    protected final void fill(final int first, final int limit, final int step, final Double value) {
        Primitive32Array.fill(data, first, limit, step, value.floatValue());
    }

    @Override
    protected final void fill(final int first, final int limit, final int step, final NullaryFunction<Double> supplier) {
        Primitive32Array.fill(data, first, limit, step, supplier);
    }

    @Override
    protected void fillOne(final int index, final Access1D<?> values, final long valueIndex) {
        data[index] = (float) values.doubleValue(valueIndex);
    }

    @Override
    protected void fillOne(final int index, final Double value) {
        data[index] = value.floatValue();
    }

    @Override
    protected void fillOne(final int index, final NullaryFunction<Double> supplier) {
        data[index] = (float) supplier.doubleValue();
    }

    @Override
    protected final Double get(final int index) {
        return Double.valueOf(data[index]);
    }

    @Override
    protected final int indexOfLargest(final int first, final int limit, final int step) {
        return AMAX.invoke(data, first, limit, step);
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
    protected final void modify(final int first, final int limit, final int step, final Access1D<Double> left, final BinaryFunction<Double> function) {
        Primitive32Array.invoke(data, first, limit, step, left, function, this);
    }

    @Override
    protected final void modify(final int first, final int limit, final int step, final BinaryFunction<Double> function, final Access1D<Double> right) {
        Primitive32Array.invoke(data, first, limit, step, this, function, right);
    }

    @Override
    protected final void modify(final int first, final int limit, final int step, final BinaryFunction<Double> function, final Double right) {
        Primitive32Array.invoke(data, first, limit, step, data, function, right);
    }

    @Override
    protected final void modify(final int first, final int limit, final int step, final Double left, final BinaryFunction<Double> function) {
        Primitive32Array.invoke(data, first, limit, step, left, function, data);
    }

    @Override
    protected final void modify(final int first, final int limit, final int step, final ParameterFunction<Double> function, final int parameter) {
        Primitive32Array.invoke(data, first, limit, step, data, function, parameter);
    }

    @Override
    protected final void modify(final int first, final int limit, final int step, final UnaryFunction<Double> function) {
        Primitive32Array.invoke(data, first, limit, step, this, function);
    }

    @Override
    protected final void modifyOne(final int index, final UnaryFunction<Double> modifier) {
        data[index] = (float) modifier.invoke(data[index]);
    }

    @Override
    protected final int searchAscending(final Double number) {
        return Arrays.binarySearch(data, number.floatValue());
    }

    @Override
    protected final void set(final int index, final double value) {
        data[index] = (float) value;
    }

    @Override
    protected final void set(final int index, final Number value) {
        data[index] = value.floatValue();
    }

    @Override
    protected int size() {
        return data.length;
    }

    @Override
    protected final void visit(final int first, final int limit, final int step, final VoidFunction<Double> visitor) {
        Primitive32Array.invoke(data, first, limit, step, visitor);
    }

    @Override
    protected void visitOne(final int index, final VoidFunction<Double> visitor) {
        visitor.invoke(data[index]);
    }

    @Override
    void modify(final long extIndex, final int intIndex, final Access1D<Double> left, final BinaryFunction<Double> function) {
        data[intIndex] = (float) function.invoke(left.doubleValue(extIndex), data[intIndex]);
    }

    @Override
    void modify(final long extIndex, final int intIndex, final BinaryFunction<Double> function, final Access1D<Double> right) {
        data[intIndex] = (float) function.invoke(data[intIndex], right.doubleValue(extIndex));
    }

    @Override
    void modify(final long extIndex, final int intIndex, final UnaryFunction<Double> function) {
        data[intIndex] = (float) function.invoke(data[intIndex]);
    }

}

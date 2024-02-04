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

import static org.ojalgo.function.constant.PrimitiveMath.ZERO;

import java.util.Arrays;

import org.ojalgo.array.operation.*;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.FunctionSet;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.function.aggregator.AggregatorSet;
import org.ojalgo.function.aggregator.PrimitiveAggregator;
import org.ojalgo.function.special.MissingMath;
import org.ojalgo.scalar.PrimitiveScalar;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Mutate1D;
import org.ojalgo.type.NumberDefinition;
import org.ojalgo.type.math.MathType;

/**
 * A one- and/or arbitrary-dimensional array of double.
 *
 * @author apete
 */
public class ArrayR032 extends PrimitiveArray {

    public static final DenseArray.Factory<Double> FACTORY = new DenseArray.Factory<>() {

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
        public MathType getMathType() {
            return MathType.R032;
        }

        @Override
        PlainArray<Double> makeDenseArray(final long size) {
            return ArrayR032.make((int) size);
        }

    };

    public static ArrayR032 make(final int size) {
        return new ArrayR032(size);
    }

    public static ArrayR032 wrap(final float... data) {
        return new ArrayR032(data);
    }

    public final float[] data;

    /**
     * Array not copied! No checking!
     */
    protected ArrayR032(final float[] data) {

        super(FACTORY, data.length);

        this.data = data;
    }

    protected ArrayR032(final int size) {

        super(FACTORY, size);

        data = new float[size];
    }

    @Override
    public void axpy(final double a, final Mutate1D.Modifiable<?> y) {
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
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj) || !(obj instanceof ArrayR032)) {
            return false;
        }
        ArrayR032 other = (ArrayR032) obj;
        if (!Arrays.equals(data, other.data)) {
            return false;
        }
        return true;
    }

    @Override
    public void fillMatching(final Access1D<?> values) {
        if (values instanceof ArrayR032) {
            FillMatchingSingle.fill(data, ((ArrayR032) values).data);
        } else {
            FillMatchingSingle.fill(data, values);
        }
    }

    @Override
    public void fillMatching(final Access1D<Double> left, final BinaryFunction<Double> function, final Access1D<Double> right) {
        int limit = MissingMath.toMinIntExact(this.count(), left.count(), right.count());
        OperationBinary.invoke(data, 0, limit, 1, left, function, right);
    }

    @Override
    public void fillMatching(final UnaryFunction<Double> function, final Access1D<Double> arguments) {
        int limit = MissingMath.toMinIntExact(this.count(), arguments.count());
        OperationUnary.invoke(data, 0, limit, 1, arguments, function);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        return prime * result + Arrays.hashCode(data);
    }

    @Override
    public final void reset() {
        Arrays.fill(data, 0.0F);
    }

    @Override
    public void sortAscending() {
        Arrays.parallelSort(data);
    }

    @Override
    public void sortDescending() {
        CorePrimitiveOperation.negate(data, 0, data.length, 1, data);
        Arrays.parallelSort(data);
        CorePrimitiveOperation.negate(data, 0, data.length, 1, data);
    }

    @Override
    public void supplyTo(final Mutate1D receiver) {
        int limit = Math.min(data.length, receiver.size());
        for (int i = 0; i < limit; i++) {
            receiver.set(i, data[i]);
        }
    }

    @Override
    protected void add(final int index, final Comparable<?> addend) {
        data[index] += NumberDefinition.floatValue(addend);
    }

    @Override
    protected void add(final int index, final double addend) {
        data[index] += (float) addend;
    }

    @Override
    protected void add(final int index, final float addend) {
        data[index] += addend;
    }

    @Override
    public byte byteValue(final int index) {
        return (byte) Math.round(data[index]);
    }

    protected final float[] copyOfData() {
        return COPY.copyOf(data);
    }

    @Override
    public final double doubleValue(final int index) {
        return data[index];
    }

    @Override
    protected final void exchange(final int firstA, final int firstB, final int step, final int count) {
        Exchange.exchange(data, firstA, firstB, step, count);
    }

    @Override
    protected final void fill(final int first, final int limit, final int step, final Double value) {
        FillAll.fill(data, first, limit, step, value.floatValue());
    }

    @Override
    protected final void fill(final int first, final int limit, final int step, final NullaryFunction<?> supplier) {
        FillAll.fill(data, first, limit, step, supplier);
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
    protected void fillOne(final int index, final NullaryFunction<?> supplier) {
        data[index] = supplier.floatValue();
    }

    @Override
    public float floatValue(final int index) {
        return data[index];
    }

    @Override
    public final Double get(final int index) {
        return Double.valueOf(data[index]);
    }

    @Override
    protected final int indexOfLargest(final int first, final int limit, final int step) {
        return AMAX.invoke(data, first, limit, step);
    }

    @Override
    public int intValue(final int index) {
        return Math.round(data[index]);
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
        OperationBinary.invoke(data, first, limit, step, left, function, this);
    }

    @Override
    protected final void modify(final int first, final int limit, final int step, final BinaryFunction<Double> function, final Access1D<Double> right) {
        OperationBinary.invoke(data, first, limit, step, this, function, right);
    }

    @Override
    protected final void modify(final int first, final int limit, final int step, final UnaryFunction<Double> function) {
        OperationUnary.invoke(data, first, limit, step, this, function);
    }

    @Override
    protected final void modifyOne(final int index, final UnaryFunction<Double> modifier) {
        data[index] = modifier.invoke(data[index]);
    }

    @Override
    protected final int searchAscending(final Double number) {
        return Arrays.binarySearch(data, number.floatValue());
    }

    @Override
    protected final void set(final int index, final Comparable<?> value) {
        data[index] = Scalar.floatValue(value);
    }

    @Override
    public final void set(final int index, final double value) {
        data[index] = (float) value;
    }

    @Override
    public final void set(final int index, final float value) {
        data[index] = value;
    }

    @Override
    public short shortValue(final int index) {
        return (short) Math.round(data[index]);
    }

    @Override
    protected final void visit(final int first, final int limit, final int step, final VoidFunction<Double> visitor) {
        OperationVoid.invoke(data, first, limit, step, visitor);
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
        data[intIndex] = function.invoke(data[intIndex]);
    }

    @Override
    public void set(final int index, final long value) {
        data[index] = value;
    }

}

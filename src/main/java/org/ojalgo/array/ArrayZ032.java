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

import org.ojalgo.array.operation.AMAX;
import org.ojalgo.array.operation.CorePrimitiveOperation;
import org.ojalgo.array.operation.Exchange;
import org.ojalgo.array.operation.FillAll;
import org.ojalgo.array.operation.OperationBinary;
import org.ojalgo.array.operation.OperationUnary;
import org.ojalgo.array.operation.OperationVoid;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.FunctionSet;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.function.aggregator.AggregatorSet;
import org.ojalgo.function.aggregator.PrimitiveAggregator;
import org.ojalgo.scalar.PrimitiveScalar;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access1D;
import org.ojalgo.type.NumberDefinition;
import org.ojalgo.type.math.MathType;

/**
 * A one- and/or arbitrary-dimensional array of double.
 *
 * @author apete
 */
public class ArrayZ032 extends PrimitiveArray {

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
            return MathType.Z032;
        }

        @Override
        PlainArray<Double> makeDenseArray(final long size) {
            return ArrayZ032.make((int) size);
        }

    };

    public static ArrayZ032 make(final int size) {
        return new ArrayZ032(size);
    }

    public static ArrayZ032 wrap(final int... data) {
        return new ArrayZ032(data);
    }

    public final int[] data;

    protected ArrayZ032(final int size) {

        super(FACTORY, size);

        data = new int[size];
    }

    /**
     * Array not copied! No checking!
     */
    protected ArrayZ032(final int[] data) {

        super(FACTORY, data.length);

        this.data = data;
    }

    @Override
    public void reset() {
        Arrays.fill(data, 0);
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
    protected void add(final int index, final Comparable<?> addend) {
        data[index] += NumberDefinition.intValue(addend);
    }

    @Override
    protected void add(final int index, final double addend) {
        data[index] += (int) Math.round(addend);
    }

    @Override
    protected void add(final int index, final int addend) {
        data[index] += addend;
    }

    @Override
    public byte byteValue(final int index) {
        return (byte) data[index];
    }

    @Override
    public double doubleValue(final int index) {
        return data[index];
    }

    @Override
    protected void exchange(final int firstA, final int firstB, final int step, final int count) {
        Exchange.exchange(data, firstA, firstB, step, count);
    }

    @Override
    protected void fill(final int first, final int limit, final int step, final Double value) {
        FillAll.fill(data, first, limit, step, value.intValue());
    }

    @Override
    protected void fill(final int first, final int limit, final int step, final NullaryFunction<?> supplier) {
        FillAll.fill(data, first, limit, step, supplier);
    }

    @Override
    protected void fillOne(final int index, final Access1D<?> values, final long valueIndex) {
        data[index] = values.intValue(valueIndex);
    }

    @Override
    protected void fillOne(final int index, final Double value) {
        data[index] = value.intValue();
    }

    @Override
    protected void fillOne(final int index, final NullaryFunction<?> supplier) {
        data[index] = supplier.intValue();
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
    protected int indexOfLargest(final int first, final int limit, final int step) {
        return AMAX.invoke(data, first, limit, step);
    }

    @Override
    public int intValue(final int index) {
        return data[index];
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
    protected void modify(final int first, final int limit, final int step, final Access1D<Double> left, final BinaryFunction<Double> function) {
        OperationBinary.invoke(data, first, limit, step, left, function, this);
    }

    @Override
    protected void modify(final int first, final int limit, final int step, final BinaryFunction<Double> function, final Access1D<Double> right) {
        OperationBinary.invoke(data, first, limit, step, this, function, right);
    }

    @Override
    protected void modify(final int first, final int limit, final int step, final UnaryFunction<Double> function) {
        OperationUnary.invoke(data, first, limit, step, this, function);
    }

    @Override
    protected void modifyOne(final int index, final UnaryFunction<Double> modifier) {
        data[index] = modifier.invoke(data[index]);
    }

    @Override
    protected int searchAscending(final Double number) {
        return Arrays.binarySearch(data, number.intValue());
    }

    @Override
    protected void set(final int index, final Comparable<?> number) {
        data[index] = Scalar.intValue(number);
    }

    @Override
    public void set(final int index, final double value) {
        data[index] = (int) Math.round(value);
    }

    @Override
    public void set(final int index, final float value) {
        data[index] = Math.round(value);
    }

    @Override
    public short shortValue(final int index) {
        return (short) data[index];
    }

    @Override
    protected void visit(final int first, final int limit, final int step, final VoidFunction<Double> visitor) {
        OperationVoid.invoke(data, first, limit, step, visitor);
    }

    @Override
    protected void visitOne(final int index, final VoidFunction<Double> visitor) {
        visitor.invoke(data[index]);
    }

    @Override
    void modify(final long extIndex, final int intIndex, final Access1D<Double> left, final BinaryFunction<Double> function) {
        data[intIndex] = function.invoke(left.intValue(extIndex), data[intIndex]);
    }

    @Override
    void modify(final long extIndex, final int intIndex, final BinaryFunction<Double> function, final Access1D<Double> right) {
        data[intIndex] = function.invoke(data[intIndex], right.intValue(extIndex));
    }

    @Override
    void modify(final long extIndex, final int intIndex, final UnaryFunction<Double> function) {
        data[intIndex] = function.invoke(data[intIndex]);
    }

    @Override
    public void set(final int index, final long value) {
        data[index] = (int) value;
    }

    @Override
    public void set(final int index, final int value) {
        data[index] = value;
    }

}

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

import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.FunctionSet;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.ParameterFunction;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.function.aggregator.AggregatorSet;
import org.ojalgo.function.aggregator.PrimitiveAggregator;
import org.ojalgo.machine.JavaType;
import org.ojalgo.scalar.PrimitiveScalar;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access1D;

/**
 * A one- and/or arbitrary-dimensional array of double.
 *
 * @author apete
 */
public class PrimitiveZ008 extends PrimitiveArray {

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
        long getElementSize() {
            return ELEMENT_SIZE;
        }

        @Override
        PlainArray<Double> makeDenseArray(final long size) {
            return PrimitiveZ008.make((int) size);
        }

    };

    static final long ELEMENT_SIZE = JavaType.LONG.memory();

    public static PrimitiveZ008 make(final int size) {
        return new PrimitiveZ008(size);
    }

    public static PrimitiveZ008 wrap(final byte... data) {
        return new PrimitiveZ008(data);
    }

    public final byte[] data;

    /**
     * Array not copied! No checking!
     */
    protected PrimitiveZ008(final byte[] data) {

        super(FACTORY, data.length);

        this.data = data;
    }

    protected PrimitiveZ008(final int size) {

        super(FACTORY, size);

        data = new byte[size];
    }

    @Override
    public void sortAscending() {
        // TODO Auto-generated method stub

    }

    @Override
    public void sortDescending() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void add(final int index, final Comparable<?> addend) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void add(final int index, final double addend) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void add(final int index, final float addend) {
        // TODO Auto-generated method stub

    }

    @Override
    protected byte byteValue(final int index) {
        return data[index];
    }

    @Override
    protected double doubleValue(final int index) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    protected void exchange(final int firstA, final int firstB, final int step, final int count) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void fill(final int first, final int limit, final Access1D<Double> left, final BinaryFunction<Double> function, final Access1D<Double> right) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void fill(final int first, final int limit, final Access1D<Double> left, final BinaryFunction<Double> function, final Double right) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void fill(final int first, final int limit, final Double left, final BinaryFunction<Double> function, final Access1D<Double> right) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void fill(final int first, final int limit, final int step, final Double value) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void fill(final int first, final int limit, final int step, final NullaryFunction<?> supplier) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void fillOne(final int index, final Access1D<?> values, final long valueIndex) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void fillOne(final int index, final Double value) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void fillOne(final int index, final NullaryFunction<?> supplier) {
        // TODO Auto-generated method stub

    }

    @Override
    protected float floatValue(final int index) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    protected Double get(final int index) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected int indexOfLargest(final int first, final int limit, final int step) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    protected boolean isAbsolute(final int index) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    protected boolean isSmall(final int index, final double comparedTo) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    protected void modify(final int first, final int limit, final int step, final Access1D<Double> left, final BinaryFunction<Double> function) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void modify(final int first, final int limit, final int step, final BinaryFunction<Double> function, final Access1D<Double> right) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void modify(final int first, final int limit, final int step, final BinaryFunction<Double> function, final Double right) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void modify(final int first, final int limit, final int step, final Double left, final BinaryFunction<Double> function) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void modify(final int first, final int limit, final int step, final ParameterFunction<Double> function, final int parameter) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void modify(final int first, final int limit, final int step, final UnaryFunction<Double> function) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void modifyOne(final int index, final UnaryFunction<Double> modifier) {
        // TODO Auto-generated method stub

    }

    @Override
    protected int searchAscending(final Double number) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    protected void set(final int index, final Comparable<?> number) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void set(final int index, final double value) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void set(final int index, final float value) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void visit(final int first, final int limit, final int step, final VoidFunction<Double> visitor) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void visitOne(final int index, final VoidFunction<Double> visitor) {
        // TODO Auto-generated method stub

    }

    @Override
    void modify(final long extIndex, final int intIndex, final Access1D<Double> left, final BinaryFunction<Double> function) {
        // TODO Auto-generated method stub

    }

    @Override
    void modify(final long extIndex, final int intIndex, final BinaryFunction<Double> function, final Access1D<Double> right) {
        // TODO Auto-generated method stub

    }

    @Override
    void modify(final long extIndex, final int intIndex, final UnaryFunction<Double> function) {
        // TODO Auto-generated method stub

    }

}

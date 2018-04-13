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
import static org.ojalgo.function.PrimitiveFunction.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

import org.ojalgo.ProgrammingError;
import org.ojalgo.access.Access1D;
import org.ojalgo.access.StructureAnyD;
import org.ojalgo.constant.PrimitiveMath;
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

/**
 * <p>
 * The odd member among the array implementations. It allows to create arrays based on memory mapped files or
 * direct buffers.
 * </p>
 *
 * @author apete
 */
public abstract class BufferArray extends PlainArray<Double> {

    static final class DoubleBufferArray extends BufferArray {

        private final DoubleBuffer myDoubleBuffer;

        DoubleBufferArray(final DoubleBuffer buffer, final RandomAccessFile file) {

            super(DIRECT64, buffer, file);

            myDoubleBuffer = buffer;
        }

        @Override
        protected double doubleValue(final int index) {
            return myDoubleBuffer.get(index);
        }

        @Override
        protected void set(final int index, final double value) {
            myDoubleBuffer.put(index, value);
        }

    }

    static final class FloatBufferArray extends BufferArray {

        private final FloatBuffer myFloatBuffer;

        FloatBufferArray(final FloatBuffer buffer, final RandomAccessFile file) {

            super(DIRECT32, buffer, file);

            myFloatBuffer = buffer;
        }

        @Override
        protected double doubleValue(final int index) {
            return myFloatBuffer.get(index);
        }

        @Override
        protected void set(final int index, final double value) {
            myFloatBuffer.put(index, (float) value);
        }

    }

    public static final DenseArray.Factory<Double> DIRECT32 = new DenseArray.Factory<Double>() {

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
        long getCapacityLimit() {
            return MAX_ARRAY_SIZE / FLOAT_ELEMENT_SIZE;
        }

        @Override
        long getElementSize() {
            return FLOAT_ELEMENT_SIZE;
        }

        @Override
        DenseArray<Double> make(final long size) {
            final int tmpSize = (int) size;
            final ByteBuffer tmpAllocateDirect = ByteBuffer.allocateDirect(tmpSize * 4);
            return new FloatBufferArray(tmpAllocateDirect.asFloatBuffer(), null);
        }

    };

    public static final DenseArray.Factory<Double> DIRECT64 = new DenseArray.Factory<Double>() {

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
        long getCapacityLimit() {
            return MAX_ARRAY_SIZE / DOUBLE_ELEMENT_SIZE;
        }

        @Override
        long getElementSize() {
            return DOUBLE_ELEMENT_SIZE;
        }

        @Override
        DenseArray<Double> make(final long size) {
            final int tmpSize = (int) size;
            final ByteBuffer tmpAllocateDirect = ByteBuffer.allocateDirect(tmpSize * 8);
            return new DoubleBufferArray(tmpAllocateDirect.asDoubleBuffer(), null);
        }

    };

    static final long DOUBLE_ELEMENT_SIZE = JavaType.DOUBLE.memory();
    static final long FLOAT_ELEMENT_SIZE = JavaType.FLOAT.memory();

    public static Array1D<Double> make(final File file, final long count) {
        return BufferArray.create(file, count).wrapInArray1D();
    }

    public static ArrayAnyD<Double> make(final File file, final long... structure) {
        return BufferArray.create(file, structure).wrapInArrayAnyD(structure);
    }

    public static Array2D<Double> make(final File file, final long rows, final long columns) {
        return BufferArray.create(file, rows, columns).wrapInArray2D(rows);
    }

    public static BufferArray make(final int capacity) {
        return new DoubleBufferArray(DoubleBuffer.allocate(capacity), null);
    }

    public static BufferArray wrap(final DoubleBuffer data) {
        return new DoubleBufferArray(data, null);
    }

    public static BufferArray wrap(final FloatBuffer data) {
        return new FloatBufferArray(data, null);
    }

    private static BasicArray<Double> create(final File file, final long... structure) {

        final long tmpCount = StructureAnyD.count(structure);

        DoubleBuffer tmpDoubleBuffer = null;

        try {

            final RandomAccessFile tmpRandomAccessFile = new RandomAccessFile(file, "rw");

            final FileChannel tmpFileChannel = tmpRandomAccessFile.getChannel();

            final long tmpSize = DOUBLE_ELEMENT_SIZE * tmpCount;

            if (tmpCount > (1L << 8)) {

                final DenseArray.Factory<Double> tmpFactory = new DenseArray.Factory<Double>() {

                    long offset = 0L;

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
                        return DOUBLE_ELEMENT_SIZE;
                    }

                    @Override
                    PlainArray<Double> make(final long size) {

                        final long tmpSize2 = size * DOUBLE_ELEMENT_SIZE;
                        try {

                            final MappedByteBuffer tmpMap = tmpFileChannel.map(MapMode.READ_WRITE, offset, tmpSize2);
                            tmpMap.order(ByteOrder.nativeOrder());
                            return new DoubleBufferArray(tmpMap.asDoubleBuffer(), tmpRandomAccessFile);
                        } catch (final IOException exception) {
                            throw new RuntimeException(exception);
                        } finally {
                            offset += tmpSize2;
                        }
                    }

                };

                return tmpFactory.makeSegmented(structure);

            } else {

                final MappedByteBuffer tmpMappedByteBuffer = tmpFileChannel.map(FileChannel.MapMode.READ_WRITE, 0L, tmpSize);
                tmpMappedByteBuffer.order(ByteOrder.nativeOrder());

                tmpDoubleBuffer = tmpMappedByteBuffer.asDoubleBuffer();

                return new DoubleBufferArray(tmpDoubleBuffer, tmpRandomAccessFile);
            }

        } catch (final FileNotFoundException exception) {
            throw new RuntimeException(exception);
        } catch (final IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    protected static void fill(final BufferArray data, final Access1D<?> value) {
        final int tmpLimit = (int) Math.min(data.count(), value.count());
        for (int i = 0; i < tmpLimit; i++) {
            data.set(i, value.doubleValue(i));
        }
    }

    protected static void fill(final BufferArray data, final int first, final int limit, final int step, final double value) {
        for (int i = first; i < limit; i += step) {
            data.set(i, value);
        }
    }

    protected static void fill(final BufferArray data, final int first, final int limit, final int step, final NullaryFunction<?> supplier) {
        for (int i = first; i < limit; i += step) {
            data.set(i, supplier.doubleValue());
        }
    }

    protected static void invoke(final BufferArray data, final int first, final int limit, final int step, final Access1D<Double> left,
            final BinaryFunction<Double> function, final Access1D<Double> right) {
        for (int i = first; i < limit; i += step) {
            data.set(i, function.invoke(left.get(i), right.get(i)));
        }
    }

    protected static void invoke(final BufferArray data, final int first, final int limit, final int step, final Access1D<Double> left,
            final BinaryFunction<Double> function, final double right) {
        for (int i = first; i < limit; i += step) {
            data.set(i, function.invoke(left.doubleValue(i), right));
        }
    }

    protected static void invoke(final BufferArray data, final int first, final int limit, final int step, final Access1D<Double> value,
            final ParameterFunction<Double> function, final int aParam) {
        for (int i = first; i < limit; i += step) {
            data.set(i, function.invoke(value.doubleValue(i), aParam));
        }
    }

    protected static void invoke(final BufferArray data, final int first, final int limit, final int step, final Access1D<Double> value,
            final UnaryFunction<Double> function) {
        for (int i = first; i < limit; i += step) {
            data.set(i, function.invoke(value.doubleValue(i)));
        }
    }

    protected static void invoke(final BufferArray data, final int first, final int limit, final int step, final double left,
            final BinaryFunction<Double> function, final Access1D<Double> right) {
        for (int i = first; i < limit; i += step) {
            data.set(i, function.invoke(left, right.doubleValue(i)));
        }
    }

    protected static void invoke(final BufferArray data, final int first, final int limit, final int step, final VoidFunction<Double> visitor) {
        for (int i = first; i < limit; i += step) {
            visitor.invoke(data.get(i));
        }
    }

    private final Buffer myBuffer;
    private final RandomAccessFile myFile;

    BufferArray(DenseArray.Factory<Double> factory, final Buffer buffer, final RandomAccessFile file) {

        super(factory, buffer.capacity());

        myBuffer = buffer;
        myFile = file;

    }

    public void close() {
        if (myFile != null) {
            try {
                myFile.close();
            } catch (final IOException exception) {
                exception.printStackTrace();
            }
        }
    }

    @Override
    public void reset() {
        this.fillAll(PrimitiveMath.ZERO);
        myBuffer.clear();
    }

    @Override
    protected void add(final int index, final double addend) {
        this.set(index, this.doubleValue(index) + addend);
    }

    @Override
    protected void add(final int index, final Number addend) {
        this.set(index, this.doubleValue(index) + addend.doubleValue());
    }

    @Override
    protected void exchange(final int firstA, final int firstB, final int step, final int count) {

        int tmpIndexA = firstA;
        int tmpIndexB = firstB;

        double tmpVal;

        for (int i = 0; i < count; i++) {

            tmpVal = this.doubleValue(tmpIndexA);
            this.set(tmpIndexA, this.doubleValue(tmpIndexB));
            this.set(tmpIndexB, tmpVal);

            tmpIndexA += step;
            tmpIndexB += step;
        }
    }

    @Override
    protected void fill(final int first, final int limit, final Access1D<Double> left, final BinaryFunction<Double> function, final Access1D<Double> right) {
        BufferArray.invoke(this, first, limit, 1, left, function, right);
    }

    @Override
    protected void fill(final int first, final int limit, final Access1D<Double> left, final BinaryFunction<Double> function, final Double right) {
        BufferArray.invoke(this, first, limit, 1, left, function, right);
    }

    @Override
    protected void fill(final int first, final int limit, final Double left, final BinaryFunction<Double> function, final Access1D<Double> right) {
        BufferArray.invoke(this, first, limit, 1, left, function, right);
    }

    @Override
    protected void fill(final int first, final int limit, final int step, final Double value) {
        BufferArray.fill(this, first, limit, step, value);
    }

    @Override
    protected void fill(final int first, final int limit, final int step, final NullaryFunction<Double> supplier) {
        BufferArray.fill(this, first, limit, step, supplier);
    }

    @Override
    protected void fillOne(final int index, final Access1D<?> values, final long valueIndex) {
        this.set(index, values.doubleValue(valueIndex));
    }

    @Override
    protected void fillOne(final int index, final Double value) {
        this.set(index, value);
    }

    @Override
    protected void fillOne(final int index, final NullaryFunction<Double> supplier) {
        this.set(index, supplier.doubleValue());
    }

    @Override
    protected Double get(final int index) {
        return this.doubleValue(index);
    }

    @Override
    protected int indexOfLargest(final int first, final int limit, final int step) {

        int retVal = first;
        double tmpLargest = ZERO;
        double tmpValue;

        for (int i = first; i < limit; i += step) {
            tmpValue = ABS.invoke(this.doubleValue(i));
            if (tmpValue > tmpLargest) {
                tmpLargest = tmpValue;
                retVal = i;
            }
        }

        return retVal;
    }

    @Override
    protected boolean isAbsolute(final int index) {
        return PrimitiveScalar.isAbsolute(this.doubleValue(index));
    }

    @Override
    protected boolean isSmall(final int index, final double comparedTo) {
        return PrimitiveScalar.isSmall(comparedTo, this.doubleValue(index));
    }

    @Override
    protected void modify(final int first, final int limit, final int step, final Access1D<Double> left, final BinaryFunction<Double> function) {
        BufferArray.invoke(this, first, limit, step, left, function, this);
    }

    @Override
    protected void modify(final int first, final int limit, final int step, final BinaryFunction<Double> function, final Access1D<Double> right) {
        BufferArray.invoke(this, first, limit, step, this, function, right);
    }

    @Override
    protected void modify(final int first, final int limit, final int step, final BinaryFunction<Double> function, final Double right) {
        BufferArray.invoke(this, first, limit, step, this, function, right);
    }

    @Override
    protected void modify(final int first, final int limit, final int step, final Double left, final BinaryFunction<Double> function) {
        BufferArray.invoke(this, first, limit, step, left, function, this);
    }

    @Override
    protected void modify(final int first, final int limit, final int step, final ParameterFunction<Double> function, final int parameter) {
        BufferArray.invoke(this, first, limit, step, this, function, parameter);
    }

    @Override
    protected void modify(final int first, final int limit, final int step, final UnaryFunction<Double> function) {
        BufferArray.invoke(this, first, limit, step, this, function);
    }

    @Override
    protected void modifyOne(final int index, final UnaryFunction<Double> modifier) {
        this.set(index, modifier.invoke(this.doubleValue(index)));
    }

    @Override
    protected int searchAscending(final Double number) {
        // TODO Auto-generated method stub
        return -1;
    }

    @Override
    protected void set(final int index, final Number value) {
        this.set(index, value.doubleValue());
    }

    @Override
    protected int size() {
        return myBuffer.capacity();
    }

    @Override
    protected void sortAscending() {
        ProgrammingError.throwForUnsupportedOptionalOperation();
    }

    @Override
    protected void sortDescending() {
        ProgrammingError.throwForUnsupportedOptionalOperation();
    }

    @Override
    protected void visit(final int first, final int limit, final int step, final VoidFunction<Double> visitor) {
        BufferArray.invoke(this, first, limit, step, visitor);
    }

    @Override
    protected void visitOne(final int index, final VoidFunction<Double> visitor) {
        visitor.invoke(this.doubleValue(index));
    }

    @Override
    boolean isPrimitive() {
        return true;
    }

    @Override
    void modify(final long extIndex, final int intIndex, final Access1D<Double> left, final BinaryFunction<Double> function) {
        this.set(intIndex, function.invoke(left.doubleValue(extIndex), this.doubleValue(intIndex)));
    }

    @Override
    void modify(final long extIndex, final int intIndex, final BinaryFunction<Double> function, final Access1D<Double> right) {
        this.set(intIndex, function.invoke(this.doubleValue(intIndex), right.doubleValue(extIndex)));
    }

    @Override
    void modify(final long extIndex, final int intIndex, final UnaryFunction<Double> function) {
        this.set(intIndex, function.invoke(this.doubleValue(intIndex)));
    }

}

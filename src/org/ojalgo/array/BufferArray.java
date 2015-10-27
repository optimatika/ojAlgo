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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

import org.ojalgo.access.Access1D;
import org.ojalgo.access.AccessUtils;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.ParameterFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.machine.JavaType;
import org.ojalgo.scalar.PrimitiveScalar;

/**
 * A one- and/or arbitrary-dimensional array of double.
 *
 * @author apete
 */
public class BufferArray extends DenseArray<Double> {

    static long ELEMENT_SIZE = JavaType.DOUBLE.memory();

    static long MAX = 1L << 8;

    public static Array1D<Double> make(final File file, final long count) {
        return BufferArray.create(file, count).asArray1D();
    }

    public static ArrayAnyD<Double> make(final File file, final long... structure) {
        return BufferArray.create(file, structure).asArrayAnyD(structure);
    }

    public static Array2D<Double> make(final File file, final long rows, final long columns) {
        return BufferArray.create(file, rows, columns).asArray2D(rows);
    }

    public static BasicArray<Double> make(final int capacity) {
        return new BufferArray(DoubleBuffer.allocate(capacity), null);
    }

    public static BufferArray wrap(final DoubleBuffer data) {
        return new BufferArray(data, null);
    }

    private static BasicArray<Double> create(final File file, final long... structure) {

        final long tmpCount = AccessUtils.count(structure);

        DoubleBuffer tmpDoubleBuffer = null;

        try {

            final RandomAccessFile tmpRandomAccessFile = new RandomAccessFile(file, "rw");

            final FileChannel tmpFileChannel = tmpRandomAccessFile.getChannel();

            final long tmpSize = ELEMENT_SIZE * tmpCount;

            if (tmpCount > MAX) {

                final DenseFactory<Double> tmpFactory = new DenseFactory<Double>() {

                    long offset = 0L;

                    @Override
                    long getElementSize() {
                        return ELEMENT_SIZE;
                    }

                    @Override
                    DenseArray<Double> make(final int size) {

                        final long tmpSize2 = size * ELEMENT_SIZE;
                        try {

                            final MappedByteBuffer tmpMap = tmpFileChannel.map(MapMode.READ_WRITE, offset, tmpSize2);
                            tmpMap.order(ByteOrder.nativeOrder());
                            return new BufferArray(tmpMap.asDoubleBuffer(), tmpRandomAccessFile);
                        } catch (final IOException exception) {
                            throw new RuntimeException(exception);
                        } finally {
                            offset += tmpSize2;
                        }
                    }

                    @Override
                    PrimitiveScalar zero() {
                        return PrimitiveScalar.ZERO;
                    }

                };

                return SegmentedArray.make(tmpFactory, structure);

            } else {

                final MappedByteBuffer tmpMappedByteBuffer = tmpFileChannel.map(FileChannel.MapMode.READ_WRITE, 0L, tmpSize);
                tmpMappedByteBuffer.order(ByteOrder.nativeOrder());

                tmpDoubleBuffer = tmpMappedByteBuffer.asDoubleBuffer();

                return new BufferArray(tmpDoubleBuffer, tmpRandomAccessFile);
            }

        } catch (final FileNotFoundException exception) {
            throw new RuntimeException(exception);
        } catch (final IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    protected static void fill(final DoubleBuffer data, final Access1D<?> value) {
        final int tmpLimit = (int) Math.min(data.capacity(), value.count());
        for (int i = 0; i < tmpLimit; i++) {
            data.put(i, value.doubleValue(i));
        }
    }

    protected static void fill(final DoubleBuffer data, final int first, final int limit, final int step, final double value) {
        for (int i = first; i < limit; i += step) {
            data.put(i, value);
        }
    }

    protected static void fill(final DoubleBuffer data, final int first, final int limit, final int step, final NullaryFunction<?> supplier) {
        for (int i = first; i < limit; i += step) {
            data.put(i, supplier.doubleValue());
        }
    }

    protected static void invoke(final DoubleBuffer data, final int first, final int limit, final int step, final Access1D<Double> left,
            final BinaryFunction<Double> function, final Access1D<Double> right) {
        for (int i = first; i < limit; i += step) {
            data.put(i, function.invoke(left.get(i), right.get(i)));
        }
    }

    protected static void invoke(final DoubleBuffer data, final int first, final int limit, final int step, final Access1D<Double> left,
            final BinaryFunction<Double> function, final double right) {
        for (int i = first; i < limit; i += step) {
            data.put(i, function.invoke(left.doubleValue(i), right));
        }
    }

    protected static void invoke(final DoubleBuffer data, final int first, final int limit, final int step, final Access1D<Double> value,
            final ParameterFunction<Double> function, final int aParam) {
        for (int i = first; i < limit; i += step) {
            data.put(i, function.invoke(value.doubleValue(i), aParam));
        }
    }

    protected static void invoke(final DoubleBuffer data, final int first, final int limit, final int step, final Access1D<Double> value,
            final UnaryFunction<Double> function) {
        for (int i = first; i < limit; i += step) {
            data.put(i, function.invoke(value.doubleValue(i)));
        }
    }

    protected static void invoke(final DoubleBuffer data, final int first, final int limit, final int step, final double left,
            final BinaryFunction<Double> function, final Access1D<Double> right) {
        for (int i = first; i < limit; i += step) {
            data.put(i, function.invoke(left, right.doubleValue(i)));
        }
    }

    protected static void invoke(final DoubleBuffer data, final int first, final int limit, final int step, final VoidFunction<Double> visitor) {
        for (int i = first; i < limit; i += step) {
            visitor.invoke(data.get(i));
        }
    }

    private final DoubleBuffer myBuffer;
    private final RandomAccessFile myFile;

    private BufferArray(final DoubleBuffer buffer, final RandomAccessFile file) {

        super();

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
    protected void add(final int index, final double addend) {
        myBuffer.put(index, myBuffer.get(index) + addend);
    }

    @Override
    protected void add(final int index, final Number addend) {
        myBuffer.put(index, myBuffer.get(index) + addend.doubleValue());
    }

    @Override
    protected double doubleValue(final int index) {
        return myBuffer.get(index);
    }

    @Override
    protected void exchange(final int firstA, final int firstB, final int step, final int count) {

        int tmpIndexA = firstA;
        int tmpIndexB = firstB;

        double tmpVal;

        for (int i = 0; i < count; i++) {

            tmpVal = myBuffer.get(tmpIndexA);
            myBuffer.put(tmpIndexA, myBuffer.get(tmpIndexB));
            myBuffer.put(tmpIndexB, tmpVal);

            tmpIndexA += step;
            tmpIndexB += step;
        }
    }

    @Override
    protected void fill(final int first, final int limit, final Access1D<Double> left, final BinaryFunction<Double> function, final Access1D<Double> right) {
        BufferArray.invoke(myBuffer, first, limit, 1, left, function, right);
    }

    @Override
    protected void fill(final int first, final int limit, final Access1D<Double> left, final BinaryFunction<Double> function, final Double right) {
        BufferArray.invoke(myBuffer, first, limit, 1, left, function, right);
    }

    @Override
    protected void fill(final int first, final int limit, final Double left, final BinaryFunction<Double> function, final Access1D<Double> right) {
        BufferArray.invoke(myBuffer, first, limit, 1, left, function, right);
    }

    @Override
    protected void fill(final int first, final int limit, final int step, final Double value) {
        BufferArray.fill(myBuffer, first, limit, step, value);
    }

    @Override
    protected void fill(final int first, final int limit, final int step, final NullaryFunction<Double> supplier) {
        BufferArray.fill(myBuffer, first, limit, step, supplier);
    }

    @Override
    protected void fillOne(final int index, final Double value) {
        myBuffer.put(index, value);
    }

    @Override
    protected void fillOne(final int index, final NullaryFunction<Double> supplier) {
        myBuffer.put(index, supplier.doubleValue());
    }

    @Override
    protected void finalize() throws Throwable {

        super.finalize();

        if (myFile != null) {
            this.close();
        }
    }

    @Override
    protected Double get(final int index) {
        return myBuffer.get(index);
    }

    @Override
    protected int indexOfLargest(final int first, final int limit, final int step) {

        int retVal = first;
        double tmpLargest = ZERO;
        double tmpValue;

        for (int i = first; i < limit; i += step) {
            tmpValue = Math.abs(myBuffer.get(i));
            if (tmpValue > tmpLargest) {
                tmpLargest = tmpValue;
                retVal = i;
            }
        }

        return retVal;
    }

    @Override
    protected boolean isAbsolute(final int index) {
        return PrimitiveScalar.isAbsolute(myBuffer.get(index));
    }

    @Override
    protected boolean isSmall(final int index, final double comparedTo) {
        return PrimitiveScalar.isSmall(comparedTo, myBuffer.get(index));
    }

    @Override
    protected void modify(final int index, final Access1D<Double> left, final BinaryFunction<Double> function) {
        // TODO Auto-generated method stub
    }

    @Override
    protected void modify(final int index, final BinaryFunction<Double> function, final Access1D<Double> right) {
        // TODO Auto-generated method stub
    }

    @Override
    protected void modify(final int first, final int limit, final int step, final Access1D<Double> left, final BinaryFunction<Double> function) {
        BufferArray.invoke(myBuffer, first, limit, step, left, function, this);
    }

    @Override
    protected void modify(final int first, final int limit, final int step, final BinaryFunction<Double> function, final Access1D<Double> right) {
        BufferArray.invoke(myBuffer, first, limit, step, this, function, right);
    }

    @Override
    protected void modify(final int first, final int limit, final int step, final BinaryFunction<Double> function, final Double right) {
        BufferArray.invoke(myBuffer, first, limit, step, this, function, right);
    }

    @Override
    protected void modify(final int first, final int limit, final int step, final Double left, final BinaryFunction<Double> function) {
        BufferArray.invoke(myBuffer, first, limit, step, left, function, this);
    }

    @Override
    protected void modify(final int first, final int limit, final int step, final ParameterFunction<Double> function, final int parameter) {
        BufferArray.invoke(myBuffer, first, limit, step, this, function, parameter);
    }

    @Override
    protected void modify(final int first, final int limit, final int step, final UnaryFunction<Double> function) {
        BufferArray.invoke(myBuffer, first, limit, step, this, function);
    }

    @Override
    protected void modify(final int index, final UnaryFunction<Double> function) {
        myBuffer.put(index, function.invoke(myBuffer.get(index)));
    }

    @Override
    protected int searchAscending(final Double number) {
        // TODO Auto-generated method stub
        return -1;
    }

    @Override
    protected void set(final int index, final double value) {
        myBuffer.put(index, value);
    }

    @Override
    protected void set(final int index, final Number value) {
        myBuffer.put(index, value.doubleValue());
    }

    @Override
    protected int size() {
        return myBuffer.capacity();
    }

    @Override
    protected void sortAscending() {

    }

    @Override
    protected void visit(final int first, final int limit, final int step, final VoidFunction<Double> visitor) {
        BufferArray.invoke(myBuffer, first, limit, step, visitor);
    }

    @Override
    protected void visitOne(final int index, final VoidFunction<Double> visitor) {
        visitor.invoke(myBuffer.get(index));
    }

    @Override
    boolean isPrimitive() {
        return true;
    }

    @Override
    DenseArray<Double> newInstance(final int capacity) {
        return null;
        // return new MyTestArray(capacity);
    }

    @Override
    protected void fillOneMatching(final int index, final Access1D<?> values, final long valueIndex) {
        myBuffer.put(index, values.doubleValue(valueIndex));
    }

}

/*
 * Copyright 1997-2025 Optimatika
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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import org.ojalgo.ProgrammingError;
import org.ojalgo.array.operation.AMAX;
import org.ojalgo.array.operation.FillAll;
import org.ojalgo.array.operation.OperationBinary;
import org.ojalgo.array.operation.OperationUnary;
import org.ojalgo.array.operation.OperationVoid;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.scalar.PrimitiveScalar;
import org.ojalgo.structure.Access1D;
import org.ojalgo.type.NumberDefinition;
import org.ojalgo.type.math.MathType;

/**
 * <p>
 * The odd member among the array implementations. It allows to create arrays based on memory mapped files or
 * direct buffers.
 * </p>
 *
 * @author apete
 */
public abstract class BufferArray extends PlainArray<Double> implements AutoCloseable {

    public static final class Factory extends PlainArray.Factory<Double, BufferArray> {

        private final BufferConstructor myConstructor;

        Factory(final MathType mathType, final BufferConstructor constructor) {
            super(mathType);
            myConstructor = constructor;
        }

        @Override
        public BufferArray make(final int size) {
            // TODO Auto-generated method stub
            return this.make((long) size);
        }

        @Override
        public BufferArray make(final long size) {
            int capacity = Math.toIntExact(size * this.getElementSize());
            ByteBuffer buffer = ByteBuffer.allocateDirect(capacity);
            return myConstructor.newInstance(this, buffer, null);
        }

        public BufferArray newMapped(final File file, final long size) {

            long count = this.getElementSize() * size;

            FileChannel fileChannel;
            MappedByteBuffer buffer;
            try {
                fileChannel = new RandomAccessFile(file, "rw").getChannel();
                buffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0L, count);
            } catch (IOException cause) {
                throw new RuntimeException(cause);
            }

            return myConstructor.newInstance(this, buffer, fileChannel);
        }

        @Override
        long getCapacityLimit() {
            return Integer.MAX_VALUE / this.getElementSize();
        }

    }

    @FunctionalInterface
    interface BufferConstructor {

        BufferArray newInstance(BufferArray.Factory factory, ByteBuffer buffer, AutoCloseable closeable);

    }

    public static final BufferArray.Factory R032 = new BufferArray.Factory(MathType.R032, BufferR032::new);
    public static final BufferArray.Factory R064 = new BufferArray.Factory(MathType.R064, BufferR064::new);
    public static final BufferArray.Factory Z008 = new BufferArray.Factory(MathType.Z008, BufferZ008::new);
    public static final BufferArray.Factory Z016 = new BufferArray.Factory(MathType.Z016, BufferZ016::new);
    public static final BufferArray.Factory Z032 = new BufferArray.Factory(MathType.Z032, BufferZ032::new);
    public static final BufferArray.Factory Z064 = new BufferArray.Factory(MathType.Z064, BufferZ064::new);

    private final Buffer myBuffer;
    private final AutoCloseable myFile;

    BufferArray(final BufferArray.Factory factory, final Buffer buffer, final AutoCloseable file) {

        super(factory, Math.toIntExact(buffer.capacity() / factory.getElementSize()));

        myBuffer = buffer;
        myFile = file;
    }

    @Override
    public void close() {
        if (myFile != null) {
            try {
                myFile.close();
            } catch (Exception cause) {
                throw new RuntimeException(cause);
            }
        }
    }

    @Override
    public Double get(final int index) {
        return Double.valueOf(this.doubleValue(index));
    }

    @Override
    public void reset() {
        this.fillAll(PrimitiveMath.ZERO);
        myBuffer.clear();
    }

    @Override
    protected final void add(final int index, final byte addend) {
        this.set(index, this.byteValue(index) + addend);
    }

    @Override
    protected final void add(final int index, final double addend) {
        this.set(index, this.doubleValue(index) + addend);
    }

    @Override
    protected final void add(final int index, final float addend) {
        this.set(index, this.floatValue(index) + addend);
    }

    @Override
    protected final void add(final int index, final int addend) {
        this.set(index, this.intValue(index) + addend);
    }

    @Override
    protected final void add(final int index, final long addend) {
        this.set(index, this.longValue(index) + addend);
    }

    @Override
    protected final void add(final int index, final short addend) {
        this.set(index, this.shortValue(index) + addend);
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
    protected void fill(final int first, final int limit, final int step, final Double value) {
        FillAll.fill(this, first, limit, step, value);
    }

    @Override
    protected void fill(final int first, final int limit, final int step, final NullaryFunction<?> supplier) {
        FillAll.fill(this, first, limit, step, supplier);
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
    protected int indexOfLargest(final int first, final int limit, final int step) {
        return AMAX.invoke(this, first, limit, step);
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
        OperationBinary.invoke(this, first, limit, step, left, function, this);
    }

    @Override
    protected void modify(final int first, final int limit, final int step, final BinaryFunction<Double> function, final Access1D<Double> right) {
        OperationBinary.invoke(this, first, limit, step, this, function, right);
    }

    @Override
    protected void modify(final int first, final int limit, final int step, final UnaryFunction<Double> function) {
        OperationUnary.invoke(this, first, limit, step, this, function);
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
    protected void set(final int index, final Comparable<?> value) {
        this.set(index, NumberDefinition.doubleValue(value));
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
        OperationVoid.invoke(this, first, limit, step, visitor);
    }

    @Override
    protected void visitOne(final int index, final VoidFunction<Double> visitor) {
        visitor.invoke(this.doubleValue(index));
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

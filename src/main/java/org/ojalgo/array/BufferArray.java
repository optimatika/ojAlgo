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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

import org.ojalgo.ProgrammingError;
import org.ojalgo.array.operation.AMAX;
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
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.scalar.PrimitiveScalar;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Structure1D;
import org.ojalgo.structure.StructureAnyD;
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

    public static final class Factory extends DenseArray.Factory<Double> {

        private final BufferConstructor myConstructor;
        private final MathType myMathType;

        Factory(final MathType mathType, final BufferConstructor constructor) {
            super();
            myMathType = mathType;
            myConstructor = constructor;
        }

        @Override
        public FunctionSet<Double> function() {
            return PrimitiveFunction.getSet();
        }

        public MappedFileFactory newMapped(final File file) {
            return new MappedFileFactory(this, file);
        }

        @Override
        public Scalar.Factory<Double> scalar() {
            return PrimitiveScalar.FACTORY;
        }

        @Override
        AggregatorSet<Double> aggregator() {
            return PrimitiveAggregator.getSet();
        }

        @Override
        long getCapacityLimit() {
            return PlainArray.MAX_SIZE / this.getElementSize();
        }

        @Override
        BufferArray makeDenseArray(final long size) {
            int capacity = Math.toIntExact(size * this.getElementSize());
            ByteBuffer buffer = ByteBuffer.allocateDirect(capacity);
            return myConstructor.newInstance(this, buffer, null);
        }

        /**
         * Signature matching {@link BufferConstructor}.
         */
        BufferArray newInstance(final Factory factory, final ByteBuffer buffer, final AutoCloseable closeable) {
            return myConstructor.newInstance(factory, buffer, closeable);
        }

        @Override
        public MathType getMathType() {
            return myMathType;
        }

    }

    public static final class MappedFileFactory extends DenseArray.Factory<Double> {

        private final File myFile;
        private final Factory myTypeFactory;

        MappedFileFactory(final Factory typeFactory, final File file) {
            super();
            myTypeFactory = typeFactory;
            myFile = file;
        }

        @Override
        public FunctionSet<Double> function() {
            return myTypeFactory.function();
        }

        @Override
        public BufferArray makeFilled(final Structure1D shape, final NullaryFunction<?> supplier) {
            return (BufferArray) super.makeFilled(shape, supplier);
        }

        @Override
        public BufferArray copy(final Access1D<?> source) {
            return (BufferArray) super.copy(source);
        }

        @Override
        public BufferArray copy(final Comparable<?>... source) {
            return (BufferArray) super.copy(source);
        }

        @Override
        public BufferArray copy(final double... source) {
            return (BufferArray) super.copy(source);
        }

        @Override
        public BufferArray copy(final List<? extends Comparable<?>> source) {
            return (BufferArray) super.copy(source);
        }

        @Override
        public BufferArray make(final long count) {
            return (BufferArray) super.make(count);
        }

        @Override
        SegmentedArray<Double> makeSegmented(final long... structure) {
            return super.makeSegmented(structure);
        }

        @Override
        public BufferArray make(final int count) {
            return (BufferArray) super.make(count);
        }

        @Override
        public BufferArray make(final Structure1D shape) {
            return (BufferArray) super.make(shape);
        }

        @Override
        public BufferArray makeFilled(final long count, final NullaryFunction<?> supplier) {
            return (BufferArray) super.makeFilled(count, supplier);
        }

        @Override
        public Scalar.Factory<Double> scalar() {
            return myTypeFactory.scalar();
        }

        @Override
        AggregatorSet<Double> aggregator() {
            return myTypeFactory.aggregator();
        }

        @Override
        BufferArray makeDenseArray(final long size) {

            long count = myTypeFactory.getElementSize() * size;

            FileChannel fileChannel;
            MappedByteBuffer buffer;
            try {
                fileChannel = new RandomAccessFile(myFile, "rw").getChannel();
                buffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0L, count);
            } catch (IOException cause) {
                throw new RuntimeException(cause);
            }

            return myTypeFactory.newInstance(myTypeFactory, buffer, fileChannel);
        }

        @Override
        public MathType getMathType() {
            return myTypeFactory.getMathType();
        }

    }

    @FunctionalInterface
    interface BufferConstructor {

        BufferArray newInstance(BufferArray.Factory factory, ByteBuffer buffer, AutoCloseable closeable);

    }

    public static final Factory R032 = new Factory(MathType.R032, BufferR032::new);
    public static final Factory R064 = new Factory(MathType.R064, BufferR064::new);
    public static final Factory Z008 = new Factory(MathType.Z008, BufferZ008::new);
    public static final Factory Z016 = new Factory(MathType.Z016, BufferZ016::new);
    public static final Factory Z032 = new Factory(MathType.Z032, BufferZ032::new);
    public static final Factory Z064 = new Factory(MathType.Z064, BufferZ064::new);

    /**
     * @deprecated Use {@link #R032} instead
     */
    @Deprecated
    public static final Factory DIRECT32 = R032;
    /**
     * @deprecated Use {@link #R064} instead
     */
    @Deprecated
    public static final Factory DIRECT64 = R064;

    /**
     * @deprecated v52 Use {@link #R064} and {@link MappedFileFactory#make(long)} instead.
     */
    @Deprecated
    public static Array1D<Double> make(final File file, final long count) {
        return R064.newMapped(file).make(count).wrapInArray1D();
    }

    /**
     * @deprecated v52 Use {@link #R064} and {@link MappedFileFactory#make(long)} instead.
     */
    @Deprecated
    public static ArrayAnyD<Double> make(final File file, final long... structure) {
        return R064.newMapped(file).make(StructureAnyD.count(structure)).wrapInArrayAnyD(structure);
    }

    /**
     * @deprecated v52 Use {@link #R064} and {@link MappedFileFactory#make(long)} instead.
     */
    @Deprecated
    public static Array2D<Double> make(final File file, final long rows, final long columns) {
        return R064.newMapped(file).make(rows * columns).wrapInArray2D(rows);
    }

    /**
     * @deprecated v52 Use {@link #R064} and {@link MappedFileFactory#make(long)} instead.
     */
    @Deprecated
    public static DenseArray<Double> make(final int capacity) {
        return R064.make(capacity);
    }

    /**
     * @deprecated v52 Use {@link #R064} and {@link MappedFileFactory#make(long)} instead.
     */
    @Deprecated
    public static BufferArray wrap(final ByteBuffer data) {
        return new BufferR064(BufferArray.R064, data, null);
    }

    private final Buffer myBuffer;
    private final AutoCloseable myFile;

    BufferArray(final Factory factory, final Buffer buffer, final AutoCloseable file) {

        super(factory, buffer.capacity());

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
    public void reset() {
        this.fillAll(PrimitiveMath.ZERO);
        myBuffer.clear();
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
    protected final void add(final int index, final long addend) {
        this.set(index, this.longValue(index) + addend);
    }

    @Override
    protected final void add(final int index, final int addend) {
        this.set(index, this.intValue(index) + addend);
    }

    @Override
    protected final void add(final int index, final short addend) {
        this.set(index, this.shortValue(index) + addend);
    }

    @Override
    protected final void add(final int index, final byte addend) {
        this.set(index, this.byteValue(index) + addend);
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
    public Double get(final int index) {
        return Double.valueOf(this.doubleValue(index));
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

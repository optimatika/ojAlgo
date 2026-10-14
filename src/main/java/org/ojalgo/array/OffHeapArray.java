/*
 * Copyright 1997-2026 Optimatika
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
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.ref.Cleaner;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.function.BiFunction;

import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.structure.Access1D;
import org.ojalgo.type.math.MathType;

/**
 * Off heap memory array, backed by a {@link MemorySegment}. The memory is either allocated by
 * {@link Factory#make(long)}, or a memory mapped file from {@link Factory#newMapped(File, long)}. Values are
 * stored in native byte order.
 * <p>
 * Allocated memory is zero initialised, and released by the garbage collector. A memory mapped file is
 * unmapped when the array is closed, or when it is garbage collected if it was never closed. Accessing the
 * values of a closed array throws an {@link IllegalStateException}.
 *
 * @author apete
 */
public abstract class OffHeapArray extends DenseArray<Double> implements AutoCloseable {

    public static final class Factory extends DenseArray.Factory<Double, OffHeapArray> {

        private final BiFunction<MemorySegment, Arena, OffHeapArray> myConstructor;
        private final ValueLayout myLayout;

        Factory(final MathType mathType, final ValueLayout layout, final BiFunction<MemorySegment, Arena, OffHeapArray> constructor) {
            super(mathType);
            myLayout = layout;
            myConstructor = constructor;
        }

        @Override
        public OffHeapArray make(final int size) {
            return this.make((long) size);
        }

        @Override
        public OffHeapArray make(final long size) {
            return myConstructor.apply(Arena.ofAuto().allocate(myLayout, size), null);
        }

        /**
         * Memory map a file, creating it if it does not exist and growing it if it holds fewer than
         * {@code size} elements. Existing values are kept. Close the array to unmap the file.
         */
        public OffHeapArray newMapped(final File file, final long size) {

            Arena arena = Arena.ofShared();

            MemorySegment segment;
            try (FileChannel channel = FileChannel.open(file.toPath(), StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
                segment = channel.map(FileChannel.MapMode.READ_WRITE, 0L, size * myLayout.byteSize(), arena);
            } catch (IOException cause) {
                arena.close();
                throw new RuntimeException(cause);
            }

            return myConstructor.apply(segment, arena);
        }

        @Override
        long getCapacityLimit() {
            return Long.MAX_VALUE / myLayout.byteSize();
        }

    }

    public static final OffHeapArray.Factory R032 = new OffHeapArray.Factory(MathType.R032, ValueLayout.JAVA_FLOAT, OffHeapR032::new);
    public static final OffHeapArray.Factory R064 = new OffHeapArray.Factory(MathType.R064, ValueLayout.JAVA_DOUBLE, OffHeapR064::new);
    public static final OffHeapArray.Factory Z008 = new OffHeapArray.Factory(MathType.Z008, ValueLayout.JAVA_BYTE, OffHeapZ008::new);
    public static final OffHeapArray.Factory Z016 = new OffHeapArray.Factory(MathType.Z016, ValueLayout.JAVA_SHORT, OffHeapZ016::new);
    public static final OffHeapArray.Factory Z032 = new OffHeapArray.Factory(MathType.Z032, ValueLayout.JAVA_INT, OffHeapZ032::new);
    public static final OffHeapArray.Factory Z064 = new OffHeapArray.Factory(MathType.Z064, ValueLayout.JAVA_LONG, OffHeapZ064::new);

    private static final Cleaner CLEANER = Cleaner.create();

    /**
     * Closes the arena of a memory mapped file – null when the memory is released by the garbage collector.
     */
    private final Cleaner.Cleanable myCleanable;
    private final long myCount;

    /**
     * @param segment The memory holding the values
     * @param arena The arena to close when this array is closed, or null if the garbage collector releases
     *        the memory
     */
    protected OffHeapArray(final OffHeapArray.Factory factory, final MemorySegment segment, final Arena arena) {

        super(factory);

        myCount = segment.byteSize() / factory.getElementSize();
        myCleanable = arena != null ? CLEANER.register(this, arena::close) : null;
    }

    @Override
    public final void add(final int index, final double addend) {
        this.set(index, this.doubleValue(index) + addend);
    }

    @Override
    public final void add(final long index, final byte addend) {
        this.set(index, this.byteValue(index) + addend);
    }

    @Override
    public final void add(final long index, final double addend) {
        this.set(index, this.doubleValue(index) + addend);
    }

    @Override
    public final void add(final long index, final float addend) {
        this.set(index, this.floatValue(index) + addend);
    }

    @Override
    public final void add(final long index, final int addend) {
        this.set(index, this.intValue(index) + addend);
    }

    @Override
    public final void add(final long index, final long addend) {
        this.set(index, this.longValue(index) + addend);
    }

    @Override
    public final void add(final long index, final short addend) {
        this.set(index, this.shortValue(index) + addend);
    }

    /**
     * Unmaps a memory mapped file. Does nothing for allocated memory, which the garbage collector releases.
     */
    @Override
    public void close() {
        if (myCleanable != null) {
            myCleanable.clean();
        }
    }

    @Override
    public final long count() {
        return myCount;
    }

    @Override
    public void fillAll(final Double value) {
        this.fill(0L, this.count(), 1L, value);
    }

    @Override
    public Double get(final long index) {
        return Double.valueOf(this.doubleValue(index));
    }

    @Override
    public void modifyOne(final long index, final UnaryFunction<Double> modifier) {
        this.set(index, modifier.invoke(this.doubleValue(index)));
    }

    @Override
    public final int size() {
        return Math.toIntExact(myCount);
    }

    @Override
    public void visitOne(final long index, final VoidFunction<Double> visitor) {
        visitor.accept(this.doubleValue(index));
    }

    @Override
    protected void exchange(final long firstA, final long firstB, final long step, final long count) {

        long tmpIndexA = firstA;
        long tmpIndexB = firstB;

        double tmpVal;

        for (long i = 0; i < count; i++) {

            tmpVal = this.doubleValue(tmpIndexA);
            this.set(tmpIndexA, this.doubleValue(tmpIndexB));
            this.set(tmpIndexB, tmpVal);

            tmpIndexA += step;
            tmpIndexB += step;
        }
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

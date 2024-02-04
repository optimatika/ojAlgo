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

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;

import org.ojalgo.function.NullaryFunction;
import org.ojalgo.structure.Mutate1D;
import org.ojalgo.type.NumberDefinition;

final class BufferR064 extends BufferArray {

    private final DoubleBuffer myBuffer;

    BufferR064(final BufferArray.Factory factory, final ByteBuffer buffer, final AutoCloseable closeable) {
        this(factory, buffer.asDoubleBuffer(), closeable);
    }

    BufferR064(final BufferArray.Factory factory, final DoubleBuffer buffer, final AutoCloseable closeable) {
        super(factory, buffer, closeable);
        myBuffer = buffer;
    }

    @Override
    public void supplyTo(final Mutate1D receiver) {
        int limit = Math.min(this.size(), receiver.size());
        for (int i = 0; i < limit; i++) {
            receiver.set(i, this.doubleValue(i));
        }
    }

    @Override
    public byte byteValue(final int index) {
        return (byte) Math.round(myBuffer.get(index));
    }

    @Override
    public double doubleValue(final int index) {
        return myBuffer.get(index);
    }

    @Override
    protected void fillOne(final int index, final NullaryFunction<?> supplier) {
        myBuffer.put(index, supplier.doubleValue());
    }

    @Override
    public float floatValue(final int index) {
        return (float) myBuffer.get(index);
    }

    @Override
    public int intValue(final int index) {
        return (int) Math.round(myBuffer.get(index));
    }

    @Override
    public long longValue(final int index) {
        return Math.round(myBuffer.get(index));
    }

    @Override
    public void set(final int index, final double value) {
        myBuffer.put(index, value);
    }

    @Override
    public void set(final int index, final long value) {
        myBuffer.put(index, value);
    }

    @Override
    protected void add(final int index, final Comparable<?> addend) {
        this.set(index, this.doubleValue(index) + NumberDefinition.doubleValue(addend));
    }
}

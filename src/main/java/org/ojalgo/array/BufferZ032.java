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

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.ojalgo.function.NullaryFunction;
import org.ojalgo.type.NumberDefinition;

final class BufferZ032 extends BufferArray {

    private final IntBuffer myBuffer;

    BufferZ032(final BufferArray.Factory factory, final ByteBuffer buffer, final AutoCloseable closeable) {
        this(factory, buffer.asIntBuffer(), closeable);
    }

    BufferZ032(final BufferArray.Factory factory, final IntBuffer buffer, final AutoCloseable closeable) {
        super(factory, buffer, closeable);
        myBuffer = buffer;
    }

    @Override
    protected byte byteValue(final int index) {
        return (byte) myBuffer.get(index);
    }

    @Override
    protected void fillOne(final int index, final NullaryFunction<?> supplier) {
        myBuffer.put(index, supplier.intValue());
    }

    @Override
    protected float floatValue(final int index) {
        return myBuffer.get(index);
    }

    @Override
    protected int intValue(final int index) {
        return myBuffer.get(index);
    }

    @Override
    protected void set(final int index, final double value) {
        myBuffer.put(index, (int) Math.round(value));
    }

    @Override
    protected short shortValue(final int index) {
        return (short) myBuffer.get(index);
    }

    @Override
    protected void set(final int index, final int value) {
        myBuffer.put(index, value);
    }

    @Override
    protected void set(final int index, final long value) {
        myBuffer.put(index, (int) value);
    }

    @Override
    protected void add(final int index, final Comparable<?> addend) {
        this.set(index, this.intValue(index) + NumberDefinition.intValue(addend));
    }
}

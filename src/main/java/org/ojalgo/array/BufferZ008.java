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

import org.ojalgo.function.NullaryFunction;
import org.ojalgo.type.NumberDefinition;

final class BufferZ008 extends BufferArray {

    private final ByteBuffer myBuffer;

    BufferZ008(final BufferArray.Factory factory, final ByteBuffer buffer, final AutoCloseable closeable) {
        super(factory, buffer, closeable);
        myBuffer = buffer;
    }

    @Override
    public byte byteValue(final int index) {
        return myBuffer.get(index);
    }

    @Override
    protected void fillOne(final int index, final NullaryFunction<?> supplier) {
        myBuffer.put(index, supplier.byteValue());
    }

    @Override
    public float floatValue(final int index) {
        return myBuffer.get(index);
    }

    @Override
    public void set(final int index, final double value) {
        myBuffer.put(index, (byte) Math.round(value));
    }

    @Override
    public void set(final int index, final long value) {
        myBuffer.put(index, (byte) value);
    }

    @Override
    public void set(final int index, final byte value) {
        myBuffer.put(index, value);
    }

    @Override
    protected void add(final int index, final Comparable<?> addend) {
        this.set(index, this.byteValue(index) + NumberDefinition.byteValue(addend));
    }
}

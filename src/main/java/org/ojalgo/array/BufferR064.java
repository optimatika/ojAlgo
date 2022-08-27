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

import java.nio.DoubleBuffer;

import org.ojalgo.function.NullaryFunction;
import org.ojalgo.structure.Mutate1D;

final class BufferR064 extends BufferArray {

    private final DoubleBuffer myBuffer;

    BufferR064(final DoubleBuffer buffer, final AutoCloseable file) {

        super(DIRECT64, buffer, file);

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
    protected byte byteValue(final int index) {
        return (byte) Math.round(myBuffer.get(index));
    }

    @Override
    protected double doubleValue(final int index) {
        return myBuffer.get(index);
    }

    @Override
    protected void fillOne(final int index, final NullaryFunction<?> supplier) {
        myBuffer.put(index, supplier.doubleValue());
    }

    @Override
    protected float floatValue(final int index) {
        return (float) myBuffer.get(index);
    }

    @Override
    protected int intValue(final int index) {
        return (int) Math.round(myBuffer.get(index));
    }

    @Override
    protected long longValue(final int index) {
        return Math.round(myBuffer.get(index));
    }

    @Override
    protected void set(final int index, final double value) {
        myBuffer.put(index, value);
    }

    @Override
    protected void set(final int index, final float value) {
        myBuffer.put(index, value);
    }
}

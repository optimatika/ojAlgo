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

import java.nio.IntBuffer;

import org.ojalgo.function.NullaryFunction;

final class BufferZ032 extends BufferArray {

    private final IntBuffer myBuffer;

    public BufferZ032(final Factory<Double> factory, final IntBuffer buffer, final AutoCloseable file) {
        super(factory, buffer, file);
        myBuffer = buffer;
    }

    @Override
    protected byte byteValue(final int index) {
        return (byte) myBuffer.get(index);
    }

    @Override
    protected void fillOne(final int index, final NullaryFunction<?> supplier) {
        // TODO Auto-generated method stub

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
        // TODO Auto-generated method stub

    }

    @Override
    protected void set(final int index, final float value) {
        // TODO Auto-generated method stub

    }

    @Override
    protected short shortValue(final int index) {
        return (short) myBuffer.get(index);
    }

}

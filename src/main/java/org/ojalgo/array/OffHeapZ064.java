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

import org.ojalgo.scalar.Scalar;
import org.ojalgo.type.NativeMemory;

final class OffHeapZ064 extends OffHeapArray {

    private final long myPointer;

    OffHeapZ064(final long count) {

        super(OffHeapArray.Z064, count);

        myPointer = NativeMemory.allocateLongArray(this, count);
    }

    @Override
    public void add(final long index, final Comparable<?> addend) {
        this.add(index, Scalar.longValue(addend));
    }

    @Override
    public double doubleValue(final long index) {
        return NativeMemory.getLong(myPointer, index);
    }

    @Override
    public float floatValue(final long index) {
        return NativeMemory.getLong(myPointer, index);
    }

    @Override
    public long longValue(final long index) {
        return NativeMemory.getLong(myPointer, index);
    }

    @Override
    public double doubleValue(final int index) {
        return NativeMemory.getLong(myPointer, index);
    }

    @Override
    public float floatValue(final int index) {
        return NativeMemory.getLong(myPointer, index);
    }

    @Override
    public long longValue(final int index) {
        return NativeMemory.getLong(myPointer, index);
    }

    @Override
    public void reset() {
        NativeMemory.initialiseLongArray(myPointer, this.count());
    }

    @Override
    public void set(final long index, final Comparable<?> value) {
        this.set(index, Scalar.longValue(value));
    }

    @Override
    public void set(final long index, final double value) {
        NativeMemory.setLong(myPointer, index, Math.round(value));
    }

    @Override
    public void set(final long index, final float value) {
        NativeMemory.setLong(myPointer, index, Math.round(value));
    }

    @Override
    public void set(final long index, final long value) {
        NativeMemory.setLong(myPointer, index, value);
    }

    @Override
    public void set(final int index, final double value) {
        NativeMemory.setLong(myPointer, index, Math.round(value));
    }

}

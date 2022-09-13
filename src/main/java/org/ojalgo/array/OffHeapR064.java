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

import org.ojalgo.scalar.Scalar;
import org.ojalgo.type.NativeMemory;
import org.ojalgo.type.math.MathType;

final class OffHeapR064 extends OffHeapArray {

    static final MathType TYPE = MathType.R064;

    private final long myPointer;

    OffHeapR064(final long count) {

        super(OffHeapArray.R064, count);

        myPointer = NativeMemory.allocateDoubleArray(this, count);
    }

    public double doubleValue(final long index) {
        return NativeMemory.getDouble(myPointer, index);
    }

    public float floatValue(final long index) {
        return (float) NativeMemory.getDouble(myPointer, index);
    }

    public void set(final long index, final double value) {
        NativeMemory.setDouble(myPointer, index, value);
    }

    public void set(final long index, final float value) {
        NativeMemory.setDouble(myPointer, index, value);
    }

    public void add(final long index, final Comparable<?> addend) {
        this.add(index, Scalar.doubleValue(addend));
    }

    public void set(final long index, final Comparable<?> value) {
        this.set(index, Scalar.doubleValue(value));
    }

}

/*
 * Copyright 1997-2013 Optimatika (www.optimatika.se)
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

import org.ojalgo.function.UnaryFunction;

import sun.misc.Unsafe;

public class NativeArray extends UnsafeArray {

    private final long data;

    public NativeArray(final long count) {

        super(count);

        data = UNSAFE.allocateMemory(Unsafe.ARRAY_DOUBLE_INDEX_SCALE * count);
    }

    @Override
    public double doubleValue(final long index) {
        return UNSAFE.getDouble(data + (index * Unsafe.ARRAY_DOUBLE_INDEX_SCALE));
    }

    @Override
    public void modify(final UnaryFunction<Double> function) {
        final long tmpLimit = data + (this.count() * Unsafe.ARRAY_DOUBLE_INDEX_SCALE);
        for (long addr = data; addr < tmpLimit; addr += Unsafe.ARRAY_DOUBLE_INDEX_SCALE) {
            UNSAFE.putDouble(addr, function.invoke(UNSAFE.getDouble(addr)));
        }
    }

    @Override
    public void set(final long index, final double value) {
        UNSAFE.putDouble(data + (index * Unsafe.ARRAY_DOUBLE_INDEX_SCALE), value);
    }

    @Override
    protected void finalize() throws Throwable {
        UNSAFE.freeMemory(data);
    }
}

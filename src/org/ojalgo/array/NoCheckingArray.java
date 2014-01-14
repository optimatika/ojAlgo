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

public class NoCheckingArray extends UnsafeArray {

    private final double[] data;

    public NoCheckingArray(final long count) {

        super(count);

        data = new double[(int) count];
    }

    @Override
    public double doubleValue(final long index) {
        return UNSAFE.getDouble(data, Unsafe.ARRAY_DOUBLE_BASE_OFFSET + (Unsafe.ARRAY_DOUBLE_INDEX_SCALE * index));
    }

    @Override
    public void modify(final UnaryFunction<Double> function) {
        final long tmpLimit = Unsafe.ARRAY_DOUBLE_BASE_OFFSET + (Unsafe.ARRAY_DOUBLE_INDEX_SCALE * data.length);
        for (long addr = Unsafe.ARRAY_DOUBLE_BASE_OFFSET; addr < tmpLimit; addr += Unsafe.ARRAY_DOUBLE_INDEX_SCALE) {
            UNSAFE.putDouble(data, addr, function.invoke(UNSAFE.getDouble(data, addr)));
        }
    }

    @Override
    public void set(final long index, final double value) {
        UNSAFE.putDouble(data, Unsafe.ARRAY_DOUBLE_BASE_OFFSET + (Unsafe.ARRAY_DOUBLE_INDEX_SCALE * index), value);
    }

}

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

import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import org.ojalgo.scalar.Scalar;

final class OffHeapZ032 extends OffHeapArray {

    private final MemorySegment mySegment;

    OffHeapZ032(final MemorySegment segment, final Arena arena) {

        super(OffHeapArray.Z032, segment, arena);

        mySegment = segment;
    }

    @Override
    public void add(final long index, final Comparable<?> addend) {
        this.add(index, Scalar.intValue(addend));
    }

    @Override
    public double doubleValue(final int index) {
        return mySegment.getAtIndex(JAVA_INT, index);
    }

    @Override
    public double doubleValue(final long index) {
        return mySegment.getAtIndex(JAVA_INT, index);
    }

    @Override
    public float floatValue(final int index) {
        return mySegment.getAtIndex(JAVA_INT, index);
    }

    @Override
    public float floatValue(final long index) {
        return mySegment.getAtIndex(JAVA_INT, index);
    }

    @Override
    public int intValue(final int index) {
        return mySegment.getAtIndex(JAVA_INT, index);
    }

    @Override
    public int intValue(final long index) {
        return mySegment.getAtIndex(JAVA_INT, index);
    }

    @Override
    public void reset() {
        mySegment.fill((byte) 0);
    }

    @Override
    public void set(final int index, final double value) {
        mySegment.setAtIndex(JAVA_INT, index, Math.toIntExact(Math.round(value)));
    }

    @Override
    public void set(final long index, final Comparable<?> value) {
        this.set(index, Scalar.intValue(value));
    }

    @Override
    public void set(final long index, final double value) {
        mySegment.setAtIndex(JAVA_INT, index, Math.toIntExact(Math.round(value)));
    }

    @Override
    public void set(final long index, final float value) {
        mySegment.setAtIndex(JAVA_INT, index, Math.round(value));
    }

    @Override
    public void set(final long index, final int value) {
        mySegment.setAtIndex(JAVA_INT, index, value);
    }

}

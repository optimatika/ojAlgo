/*
 * Copyright 1997-2025 Optimatika
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

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.random.Uniform;

/**
 * AbstractArrayTest
 *
 * @author apete
 */
public abstract class PrimitiveBasicArrayTest extends ArrayTests {

    static final int COUNT = 100;
    static final long[] INDICES = new long[10];

    static {
        for (int i = 0; i < INDICES.length; i++) {
            INDICES[i] = Uniform.randomInteger(COUNT);
        }
        Arrays.sort(INDICES);
    }

    /**
     * Will suggest an initial capacity (for a SparseArray) given the total count.
     */
    static int capacity(final long count) {

        double capacity = count;

        while (capacity > PlainArray.MAX_SIZE) {
            capacity = PrimitiveMath.SQRT.invoke(capacity);
        }

        capacity = PrimitiveMath.SQRT.invoke(capacity);
        return 2 * (int) capacity;
    }

    static void setMultiple(final BasicArray<Double> array, final long[] indices) {
        for (long i : indices) {
            array.set(i, 1.0);
        }
    }

    @Test
    public void testHugeSparse() {
        this.doTest(SparseArray.factory(ArrayR064.FACTORY).initial(COUNT).make(COUNT));
    }

    @Test
    public void testOffHeapR032() {
        DenseArray<Double> array = OffHeapArray.R032.make(COUNT);
        array.fillAll(0.0);
        this.doTest(array);
    }

    @Test
    public void testOffHeapR064() {
        DenseArray<Double> array = OffHeapArray.R064.make(COUNT);
        array.fillAll(0.0);
        this.doTest(array);
    }

    @Test
    public void testPrimitiveR032() {
        this.doTest(ArrayR032.make(COUNT));
    }

    @Test
    public void testPrimitiveR064() {
        this.doTest(ArrayR064.make(COUNT));
    }

    @Test
    public void testPrimitiveZ008() {
        this.doTest(ArrayZ008.make(COUNT));
    }

    @Test
    public void testPrimitiveZ016() {
        this.doTest(ArrayZ016.make(COUNT));
    }

    @Test
    public void testPrimitiveZ032() {
        this.doTest(ArrayZ032.make(COUNT));
    }

    @Test
    public void testPrimitiveZ064() {
        this.doTest(ArrayZ064.make(COUNT));
    }

    @Test
    public void testSegmentedPrimitive() {
        this.doTest(ArrayR064.FACTORY.makeSegmented(COUNT));
    }

    @Test
    public void testSegmentedSparse() {
        this.doTest(new BasicArray.Factory<>(ArrayR064.FACTORY).makeSegmented(COUNT));
    }

    @Test
    public void testSparse() {
        this.doTest(SparseArray.factory(ArrayR064.FACTORY).initial(PrimitiveBasicArrayTest.capacity(COUNT)).make(COUNT));
    }

    abstract void doTest(final BasicArray<Double> array);

}

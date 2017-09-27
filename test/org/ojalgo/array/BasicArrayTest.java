/*
 * Copyright 1997-2017 Optimatika
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

import org.ojalgo.random.Uniform;

/**
 * AbstractArrayTest
 *
 * @author apete
 */
public abstract class BasicArrayTest extends ArrayTests {

    static final int COUNT = 100;
    static final long[] INDICES = new long[10];

    static {
        for (int i = 0; i < INDICES.length; i++) {
            INDICES[i] = Uniform.randomInteger(COUNT);
        }
        Arrays.sort(INDICES);
    }

    public BasicArrayTest() {
        super();
    }

    public BasicArrayTest(final String aName) {
        super(aName);
    }

    public void testHugeSparse() {
        this.doTest(SparseArray.factory(Primitive64Array.FACTORY, Long.MAX_VALUE).initial(COUNT).make());
    }

    public void testPrimitive32() {
        this.doTest(Primitive32Array.make(COUNT));
    }

    public void testPrimitive64() {
        this.doTest(Primitive64Array.make(COUNT));
    }

    public void testSegmentedPrimitive() {
        //this.doTest(SegmentedArray.make(PrimitiveArray.FACTORY, COUNT));
        this.doTest(Primitive64Array.FACTORY.makeSegmented((long) COUNT));
    }

    public void testSegmentedSparse() {
        //this.doTest(SparseArray.makePrimitiveSegmented(COUNT));
        this.doTest(BasicArray.factory(Primitive64Array.FACTORY).makeSegmented(COUNT));
    }

    public void testSparse() {
        final long count = COUNT;
        this.doTest(SparseArray.factory(Primitive64Array.FACTORY, count).initial(DenseCapacityStrategy.capacity(count)).make());
    }

    abstract void doTest(final BasicArray<Double> array);

}

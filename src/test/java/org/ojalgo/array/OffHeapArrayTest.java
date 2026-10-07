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

import java.io.File;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.ojalgo.TestUtils;
import org.ojalgo.random.Uniform;

/**
 * Allocated and memory mapped {@link OffHeapArray}.
 *
 * @author apete
 */
public class OffHeapArrayTest extends ArrayTests {

    private static void doTest(final BasicArray<Double> array, final int size) {

        TestUtils.assertEquals(size, array.count());

        Uniform random = new Uniform();

        for (int i = 0; i < 100; i++) {

            long index = Uniform.randomInteger(size);

            double expected = random.doubleValue();

            array.set(index, expected);

            TestUtils.assertEquals(expected, array.doubleValue(index));
        }
    }

    @TempDir
    public File tempDir;

    @Test
    public void testAllocatedMemoryIsZero() {

        int size = 5000;

        for (OffHeapArray.Factory factory : new OffHeapArray.Factory[] { OffHeapArray.R032, OffHeapArray.R064, OffHeapArray.Z008, OffHeapArray.Z016,
                OffHeapArray.Z032, OffHeapArray.Z064 }) {

            OffHeapArray array = factory.make(size);

            TestUtils.assertEquals(size, array.count());

            for (int i = 0; i < size; i++) {
                TestUtils.assertEquals(0.0, array.doubleValue(i));
            }
        }
    }

    @Test
    public void testClosedMappedFileThrows() {

        File file = new File(tempDir, "Closed");

        OffHeapArray array = OffHeapArray.R064.newMapped(file, 100);
        array.set(7, 3.14);
        array.close();
        array.close();

        TestUtils.assertThrows(IllegalStateException.class, () -> array.doubleValue(7));
    }

    @Test
    public void testMappedFileKeepsValues() {

        File file = new File(tempDir, "Kept");

        int size = 5000;

        try (OffHeapArray array = OffHeapArray.Z064.newMapped(file, size)) {
            for (long i = 0L; i < size; i++) {
                array.set(i, 3L * i - 7L);
            }
        }

        TestUtils.assertEquals(size * 8L, file.length());

        try (OffHeapArray array = OffHeapArray.Z064.newMapped(file, size)) {
            for (long i = 0L; i < size; i++) {
                TestUtils.assertEquals(3L * i - 7L, array.longValue(i));
            }
        }
    }

    @Test
    public void testRandomGetSet() {

        int size = 5000;

        DenseArray<Double> array = OffHeapArray.R064.make(size);

        OffHeapArrayTest.doTest(array, size);
    }

    @Test
    public void testRandomGetSetOnMappedFile() {

        File file = new File(tempDir, "MMF");

        int size = 5000;

        try (OffHeapArray array = OffHeapArray.R064.newMapped(file, size)) {
            OffHeapArrayTest.doTest(array, size);
        }
    }

}

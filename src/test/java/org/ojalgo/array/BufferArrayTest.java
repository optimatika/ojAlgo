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

import java.io.File;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.ojalgo.TestUtils;
import org.ojalgo.random.Uniform;

/**
 * AbstractArrayTest
 *
 * @author apete
 */
public class BufferArrayTest extends ArrayTests {

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
    public void testRandomGetSet() {

        int size = 5000;
        final int capacity = size;

        DenseArray<Double> array = BufferArray.R064.make(capacity);

        BufferArrayTest.doTest(array, size);
    }

    @Test
    public void testRandomGetSetOnMappedFile() {

        File file = new File(tempDir, "MMF");

        int size = 5000;
        final int capacity = size;

        try (BufferArray array = BufferArray.R064.newMapped(file).make(capacity)) {
            BufferArrayTest.doTest(array, size);
        }
    }

}

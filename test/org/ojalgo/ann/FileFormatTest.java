/*
 * Copyright 1997-2020 Optimatika
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
package org.ojalgo.ann;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.ann.ArtificialNeuralNetwork.Activator;
import org.ojalgo.array.Primitive64Array;
import org.ojalgo.matrix.store.MatrixStore;

public class FileFormatTest extends ANNTest {

    private static final File TEST_ROOT = new File("FileFormatTest");

    @AfterAll
    public static void doAfterAll() throws IOException {
        FileFormatTest.delete(TEST_ROOT);
    }

    static void delete(File file) throws IOException {

        if (file.isDirectory()) {
            File[] listFiles = file.listFiles();
            if (listFiles != null) {
                for (File content : listFiles) {
                    FileFormatTest.delete(content);
                }
            }
        }

        if (!file.delete()) {
            throw new IOException("Failed to delete file: " + file);
        }
    }

    private File invocationDir;

    @BeforeEach
    public void doBeforeEach() {
        invocationDir = new File(TEST_ROOT, Long.toString(System.currentTimeMillis()));
        invocationDir.mkdirs();
    }

    @Test
    public void testWriteAndReadBack() {

        File file = new File(invocationDir, "ojalgo.ann");

        NetworkBuilder builder = ArtificialNeuralNetwork.builder(5, 6, 3, 9, 2).activators(Activator.IDENTITY, Activator.RECTIFIER, Activator.SIGMOID,
                Activator.TANH);

        ArtificialNeuralNetwork ann1 = builder.get();
        ann1.writeTo(file);
        ArtificialNeuralNetwork ann2 = ArtificialNeuralNetwork.from(file);

        Primitive64Array input = Primitive64Array.wrap(0.1, 0.2, 0.3, 0.4, 0.5);

        MatrixStore<Double> expected = ann1.invoke(input);
        MatrixStore<Double> actual = ann2.invoke(input);

        // BasicLogger.debug("Expected", expected);
        // BasicLogger.debug("Actual", actual);

        TestUtils.assertEquals(expected, actual);
    }

}

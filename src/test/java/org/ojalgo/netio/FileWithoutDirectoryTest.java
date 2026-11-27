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
package org.ojalgo.netio;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;

/**
 * Files given by a bare name, with no directory part, are relative to the working directory. Their parent
 * file is null.
 */
public class FileWithoutDirectoryTest extends NetioTests {

    /**
     * Reading a file that does not exist fails with a {@link FileNotFoundException} and does not create its
     * (missing) directory.
     */
    @Test
    public void testReadMissingFileCreatesNoDirectory() throws IOException {

        File directory = Files.createTempDirectory("ojAlgo").toFile();
        File missingDirectory = new File(directory, "missing");
        File missingFile = new File(missingDirectory, "missing.txt");

        try {

            RuntimeException thrown = TestUtils.assertThrows(RuntimeException.class, () -> TextLineReader.of(missingFile));

            TestUtils.assertTrue(thrown.getCause() instanceof FileNotFoundException);
            TestUtils.assertFalse(missingDirectory.exists());

        } finally {
            FromFileReader.delete(directory);
        }
    }

    @Test
    public void testWriteAndReadBareName() throws IOException {

        File absolute = File.createTempFile("ojAlgo", ".txt", new File("").getAbsoluteFile());
        File bare = new File(absolute.getName());

        try {

            TestUtils.assertNull(bare.getParentFile());

            try (TextLineWriter writer = TextLineWriter.of(bare)) {
                writer.write("first");
                writer.write("second");
            }

            try (TextLineReader reader = TextLineReader.of(bare)) {
                TestUtils.assertEquals("first", reader.read());
                TestUtils.assertEquals("second", reader.read());
                TestUtils.assertNull(reader.read());
            }

        } finally {
            absolute.delete();
        }
    }

}

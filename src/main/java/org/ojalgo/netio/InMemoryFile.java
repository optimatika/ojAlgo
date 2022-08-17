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
package org.ojalgo.netio;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;

/**
 * An in-memory "file". Can be used with some {@link ToFileWriter} implementations to dynamically create file
 * contents that only exist in memory (for download).
 * <p>
 * Feed an instance of {@link InMemoryFile} rather than {@link File} to a {@link ToFileWriter} that support
 * doing so (like the {@link TextLineWriter}), and keep a reference to the {@link InMemoryFile} instance. When
 * done writing you get the (file) contents from that instance.
 *
 * @author apete
 */
public class InMemoryFile {

    private final ByteArrayOutputStream myOutputStream = new ByteArrayOutputStream();

    public byte[] getContentsAsByteArray() {
        return myOutputStream.toByteArray();
    }

    public String getContentsAsString() {
        return myOutputStream.toString();
    }

    public OutputStream getOutputStream() {
        return myOutputStream;
    }

}

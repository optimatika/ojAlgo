/*
 * Copyright 1997-2024 Optimatika
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

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

final class ByteBufferBackedInputStream extends InputStream {

    private final ByteBuffer myBuffer;

    ByteBufferBackedInputStream(final ByteBuffer buffer) {
        myBuffer = buffer;
    }

    @Override
    public int available() {
        return myBuffer.remaining();
    }

    @Override
    public int read() throws IOException {
        return myBuffer.hasRemaining() ? myBuffer.get() & 0xFF : -1;
    }

    @Override
    public int read(final byte[] bytes, final int off, final int len) throws IOException {
        if (!myBuffer.hasRemaining()) {
            return -1;
        }
        int length = Math.min(len, myBuffer.remaining());
        myBuffer.get(bytes, off, length);
        return length;
    }

}

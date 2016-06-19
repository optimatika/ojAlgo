/*
 * Copyright 1997-2016 Optimatika (www.optimatika.se)
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

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

public final class BufferedOutputStreamWriter extends BufferedWriter {

    public BufferedOutputStreamWriter(final OutputStream aStream) {
        super(new OutputStreamWriter(aStream));
    }

    public BufferedOutputStreamWriter(final OutputStream aStream, final Charset aCharset) {
        super(new OutputStreamWriter(aStream, aCharset));
    }

    public BufferedOutputStreamWriter(final OutputStream aStream, final Charset aCharset, final int aSize) {
        super(new OutputStreamWriter(aStream, aCharset), aSize);
    }

    public BufferedOutputStreamWriter(final OutputStream aStream, final CharsetEncoder anEncoder) {
        super(new OutputStreamWriter(aStream, anEncoder));
    }

    public BufferedOutputStreamWriter(final OutputStream aStream, final CharsetEncoder anEncoder, final int aSize) {
        super(new OutputStreamWriter(aStream, anEncoder), aSize);
    }

    public BufferedOutputStreamWriter(final OutputStream aStream, final int aSize) {
        super(new OutputStreamWriter(aStream), aSize);
    }

    public BufferedOutputStreamWriter(final OutputStream aStream, final String aCharsetName) throws UnsupportedEncodingException {
        super(new OutputStreamWriter(aStream, aCharsetName));
    }

    public BufferedOutputStreamWriter(final OutputStream aStream, final String aCharsetName, final int aSize) throws UnsupportedEncodingException {
        super(new OutputStreamWriter(aStream, aCharsetName), aSize);
    }

    protected BufferedOutputStreamWriter(final Writer aWriter) {
        super(aWriter);
    }

    protected BufferedOutputStreamWriter(final Writer aWriter, final int aSize) {
        super(aWriter, aSize);
    }

}

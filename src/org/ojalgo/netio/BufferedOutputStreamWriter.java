/*
 * Copyright 1997-2015 Optimatika (www.optimatika.se)
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

    public BufferedOutputStreamWriter(OutputStream aStream) {
        super(new OutputStreamWriter(aStream));
    }

    public BufferedOutputStreamWriter(OutputStream aStream, Charset aCharset) {
        super(new OutputStreamWriter(aStream, aCharset));
    }

    public BufferedOutputStreamWriter(OutputStream aStream, Charset aCharset, int aSize) {
        super(new OutputStreamWriter(aStream, aCharset), aSize);
    }

    public BufferedOutputStreamWriter(OutputStream aStream, CharsetEncoder anEncoder) {
        super(new OutputStreamWriter(aStream, anEncoder));
    }

    public BufferedOutputStreamWriter(OutputStream aStream, CharsetEncoder anEncoder, int aSize) {
        super(new OutputStreamWriter(aStream, anEncoder), aSize);
    }

    public BufferedOutputStreamWriter(OutputStream aStream, int aSize) {
        super(new OutputStreamWriter(aStream), aSize);
    }

    public BufferedOutputStreamWriter(OutputStream aStream, String aCharsetName) throws UnsupportedEncodingException {
        super(new OutputStreamWriter(aStream, aCharsetName));
    }

    public BufferedOutputStreamWriter(OutputStream aStream, String aCharsetName, int aSize) throws UnsupportedEncodingException {
        super(new OutputStreamWriter(aStream, aCharsetName), aSize);
    }

    protected BufferedOutputStreamWriter(Writer aWriter, int aSize) {
        super(aWriter, aSize);
    }

    protected BufferedOutputStreamWriter(Writer aWriter) {
        super(aWriter);
    }

}

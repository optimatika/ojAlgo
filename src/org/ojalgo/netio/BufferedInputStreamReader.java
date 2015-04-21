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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

public final class BufferedInputStreamReader extends BufferedReader {

    public BufferedInputStreamReader(InputStream aStream) {
        super(new InputStreamReader(aStream));
    }

    public BufferedInputStreamReader(InputStream aStream, Charset aCharset) {
        super(new InputStreamReader(aStream, aCharset));
    }

    public BufferedInputStreamReader(InputStream aStream, Charset aCharset, int aSize) {
        super(new InputStreamReader(aStream, aCharset), aSize);
    }

    public BufferedInputStreamReader(InputStream aStream, CharsetDecoder aDecoder) {
        super(new InputStreamReader(aStream, aDecoder));
    }

    public BufferedInputStreamReader(InputStream aStream, CharsetDecoder aDecoder, int aSize) {
        super(new InputStreamReader(aStream, aDecoder), aSize);
    }

    public BufferedInputStreamReader(InputStream aStream, int aSize) {
        super(new InputStreamReader(aStream), aSize);
    }

    public BufferedInputStreamReader(InputStream aStream, String aCharsetName) throws UnsupportedEncodingException {
        super(new InputStreamReader(aStream, aCharsetName));
    }

    public BufferedInputStreamReader(InputStream aStream, String aCharsetName, int aSize) throws UnsupportedEncodingException {
        super(new InputStreamReader(aStream, aCharsetName), aSize);
    }

    protected BufferedInputStreamReader(Reader aReader) {
        super(aReader);
    }

    protected BufferedInputStreamReader(Reader aReader, int aSize) {
        super(aReader, aSize);
    }

}

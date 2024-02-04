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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;

/**
 * An in-memory "file" that can be used with some {@link ToFileWriter} and/or {@link FromFileReaderr}
 * implementations instead of a {@link File}.
 * <p>
 * To dynamically create file contents that only exist in memory (for download): Feed an instance of
 * {@link InMemoryFile} rather than {@link File} to a {@link ToFileWriter} that support doing so (like the
 * {@link TextLineWriter}), and keep a reference to the {@link InMemoryFile} instance. When done writing you
 * get the (file) contents from that instance.
 * <p>
 * To parse some data you have in memory: Create an instance of {@link InMemoryFile} using one of the
 * constructors that take <code>byte[]</code> or {@link String} as input, and feed that to a
 * {@link FromFileReader} that support doing so (like the {@link TextLineReader}).
 * <p>
 * Note that you can obtain both {@link OutputStream} and {@link InputStream} instances from an
 * {@link InMemoryFile} – you can write to AND (later) read from the same instance.
 * <p>
 * This class is essentially a <code>byte[]</code> wrapper making use of {@link ByteArrayInputStream} and
 * {@link ByteArrayOutputStream}.
 *
 * @author apete
 */
public class InMemoryFile {

    private byte[] myInitialContents;
    private ByteArrayInputStream myInputStream = null;
    private String myName = null;
    private ByteArrayOutputStream myOutputStream = null;
    private String myType = null;

    public InMemoryFile() {
        super();
        myInitialContents = null;
        myOutputStream = null;
        myInputStream = null;
    }

    public InMemoryFile(final byte[] contents) {
        super();
        myInitialContents = contents;
        myOutputStream = null;
        myInputStream = null;
    }

    public InMemoryFile(final String contents) {
        super();
        myInitialContents = contents.getBytes();
        myOutputStream = null;
        myInputStream = null;
    }

    public byte[] getContentsAsByteArray() {
        if (myInitialContents != null) {
            return myInitialContents;
        } else if (myOutputStream != null) {
            return myOutputStream.toByteArray();
        } else {
            throw new IllegalStateException();
        }
    }

    public String getContentsAsString() {
        if (myInitialContents != null) {
            return new String(myInitialContents);
        } else if (myOutputStream != null) {
            return myOutputStream.toString();
        } else {
            throw new IllegalStateException();
        }
    }

    /**
     * File Name
     */
    public Optional<String> getName() {
        return Optional.ofNullable(myName);
    }

    public InMemoryFile name(final String name) {
        this.setName(name);
        return this;
    }

    public InMemoryFile type(final String type) {
        this.setType(type);
        return this;
    }

    /**
     * MIME Type
     */
    public Optional<String> getType() {
        return Optional.ofNullable(myType);
    }

    /**
     * Creates a new {@link InputStream} with each invocation (but keeps a reference to it for later internal
     * use). The actual "input" is taken from the contents provided to the constructor or what was later
     * written to an {@link OutputStream} obtained via {@link #newOutputStream()}.
     */
    public InputStream newInputStream() {

        byte[] contents = this.getContentsAsByteArray();
        myInputStream = new ByteArrayInputStream(contents);
        myOutputStream = null;

        return myInputStream;
    }

    /**
     * Creates a new {@link OutputStream} with each invocation (but keeps a reference to it for later internal
     * use). Calling this method also clears any/all cached previous contents – you're expected to create new
     * content with this.
     */
    public OutputStream newOutputStream() {

        myOutputStream = new ByteArrayOutputStream();
        myInputStream = null;
        myInitialContents = null;

        return myOutputStream;
    }

    public void setName(final String name) {
        myName = name;
    }

    public void setType(final String type) {
        myType = type;
    }

}

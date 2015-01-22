/*
 * Copyright 1997-2014 Optimatika (www.optimatika.se)
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
import java.io.OutputStream;
import java.io.Serializable;
import java.io.Writer;
import java.nio.CharBuffer;
import java.util.Arrays;

import org.ojalgo.netio.BasicLogger.Appender;
import org.ojalgo.netio.BasicLogger.GenericAppender;

/**
 * A circular char buffer - an {@linkplain Appendable} {@linkplain CharSequence} that always hold exactly 65536
 * characters. Whenever you append something the oldest entry gets overwritten.
 *
 * @author apete
 */
public class CharacterRing implements CharSequence, Appendable, Serializable {

    static final class RingStream extends OutputStream {

        private final CharacterRing myRing;

        RingStream(final CharacterRing ring) {

            super();

            myRing = ring;
        }

        @Override
        public void write(final int b) throws IOException {
            myRing.append((char) b);
        }

    }

    static final class RingWriter extends Writer {

        private final CharacterRing myRing;

        RingWriter(final CharacterRing ring) {

            super(ring);

            myRing = ring;
        }

        @Override
        public void close() throws IOException {
            ;
        }

        @Override
        public void flush() throws IOException {
            ;
        }

        @Override
        public void write(final char[] cbuf, final int off, final int len) throws IOException {
            for (int i = 0; i < len; i++) {
                myRing.append(cbuf[off + i]);
            }
        }

    }

    public static final int length = Character.MAX_VALUE + 1;

    private final char[] myCharacters;
    private char myCursor = 0;

    public CharacterRing() {

        super();

        myCharacters = new char[length];
        myCursor = 0;
    }

    @Override
    public CharacterRing append(final char c) throws IOException {
        myCharacters[myCursor++] = c;
        return this;
    }

    @Override
    public CharacterRing append(final CharSequence csq) throws IOException {
        return this.append(csq, 0, csq.length());
    }

    @Override
    public CharacterRing append(final CharSequence csq, final int start, final int end) throws IOException {
        for (int i = start; i < end; i++) {
            this.append(csq.charAt(i));
        }
        return this;
    }

    public Appender asAppender() {
        return new GenericAppender(this);
    }

    public OutputStream asOutputStream() {
        return new RingStream(this);
    }

    public Writer asWriter() {
        return new RingWriter(this);
    }

    @Override
    public char charAt(final int index) {
        return myCharacters[(myCursor + index) % length];
    }

    public void clear() {
        Arrays.fill(myCharacters, ASCII.NULL);
        myCursor = 0;
    }

    public void flush(final Appendable target) {
        try {
            final int tmpCursor = myCursor;
            char tmpChar;
            for (int i = tmpCursor; i < length; i++) {
                tmpChar = myCharacters[i];
                if (tmpChar != ASCII.NULL) {
                    target.append(tmpChar);
                }
            }
            for (int i = 0; i < tmpCursor; i++) {
                tmpChar = myCharacters[i];
                if (tmpChar != ASCII.NULL) {
                    target.append(tmpChar);
                }
            }
        } catch (final IOException exception) {
            exception.printStackTrace();
        }
    }

    public int indexOfFirst(final char c) {

        int retVal = -1;

        final char tmpCursor = myCursor;
        for (int i = tmpCursor; (retVal < 0) && (i < length); i++) {
            if (myCharacters[i] == c) {
                retVal = i - tmpCursor;
            }
        }
        for (int i = 0; (retVal < 0) && (i < tmpCursor); i++) {
            if (myCharacters[i] == c) {
                retVal = i + tmpCursor;
            }
        }

        return retVal;
    }

    public int indexOfLast(final char c) {

        int retVal = -1;

        final char tmpCursor = myCursor;
        for (int i = tmpCursor - 1; (retVal < 0) && (i >= 0); i--) {
            if (myCharacters[i] == c) {
                retVal = i + tmpCursor;
            }
        }
        for (int i = length - 1; (retVal < 0) && (i >= tmpCursor); i--) {
            if (myCharacters[i] == c) {
                retVal = i - tmpCursor;
            }
        }

        return retVal;
    }

    @Override
    public int length() {
        return length;
    }

    @Override
    public CharSequence subSequence(final int start, final int end) {
        return CharBuffer.wrap(this, start, end);
    }

    @Override
    public String toString() {

        final char tmpCursor = myCursor;

        final String tmpFirstPart = String.valueOf(myCharacters, tmpCursor, length - tmpCursor);
        final String tmpSecondPart = String.valueOf(myCharacters, 0, tmpCursor);

        return tmpFirstPart + tmpSecondPart;
    }

}

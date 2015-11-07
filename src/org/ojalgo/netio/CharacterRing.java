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

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.Writer;
import java.nio.CharBuffer;
import java.util.Arrays;

import org.ojalgo.netio.BasicLogger.Printer;

/**
 * A circular char buffer - an {@linkplain Appendable} {@linkplain CharSequence} that always hold exactly
 * 65536 characters. Whenever you append something the oldest entry gets overwritten.
 *
 * @author apete
 */
public final class CharacterRing implements CharSequence, Appendable, Serializable, BasicLogger.Buffer {

    public static final class OutputStreamBuffer extends OutputStream implements BasicLogger.Buffer {

        private final CharacterRing myRing;

        OutputStreamBuffer(final CharacterRing ring) {

            super();

            myRing = ring;
        }

        public void clear() {
            myRing.clear();
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof OutputStreamBuffer)) {
                return false;
            }
            final OutputStreamBuffer other = (OutputStreamBuffer) obj;
            if (myRing == null) {
                if (other.myRing != null) {
                    return false;
                }
            } else if (!myRing.equals(other.myRing)) {
                return false;
            }
            return true;
        }

        public void flush(final Printer printer) {
            myRing.flush(printer);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = (prime * result) + ((myRing == null) ? 0 : myRing.hashCode());
            return result;
        }

        @Override
        public void write(final int b) throws IOException {
            myRing.append((char) b);
        }

    }

    public static final class PrinterBuffer extends BasicLogger.AppendablePrinter implements BasicLogger.Buffer {

        private final CharacterRing myRing;

        PrinterBuffer(final CharacterRing ring) {

            super(ring);

            myRing = ring;
        }

        public void clear() {
            myRing.clear();
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof PrinterBuffer)) {
                return false;
            }
            final PrinterBuffer other = (PrinterBuffer) obj;
            if (myRing == null) {
                if (other.myRing != null) {
                    return false;
                }
            } else if (!myRing.equals(other.myRing)) {
                return false;
            }
            return true;
        }

        public void flush(final Printer printer) {
            myRing.flush(printer);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = (prime * result) + ((myRing == null) ? 0 : myRing.hashCode());
            return result;
        }

    }

    public static final class WriterBuffer extends Writer implements BasicLogger.Buffer {

        private final CharacterRing myRing;

        WriterBuffer(final CharacterRing ring) {

            super(ring);

            myRing = ring;
        }

        public void clear() {
            myRing.clear();
        }

        @Override
        public void close() throws IOException {
            ;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof WriterBuffer)) {
                return false;
            }
            final WriterBuffer other = (WriterBuffer) obj;
            if (myRing == null) {
                if (other.myRing != null) {
                    return false;
                }
            } else if (!myRing.equals(other.myRing)) {
                return false;
            }
            return true;
        }

        @Override
        public void flush() throws IOException {
            ;
        }

        public void flush(final Printer printer) {
            myRing.flush(printer);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = (prime * result) + ((myRing == null) ? 0 : myRing.hashCode());
            return result;
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

    public OutputStreamBuffer asOutputStream() {
        return new OutputStreamBuffer(this);
    }

    public PrinterBuffer asPrinter() {
        return new PrinterBuffer(this);
    }

    public WriterBuffer asWriter() {
        return new WriterBuffer(this);
    }

    @Override
    public char charAt(final int index) {
        return myCharacters[(myCursor + index) % length];
    }

    public void clear() {
        Arrays.fill(myCharacters, ASCII.NULL);
        myCursor = 0;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof CharacterRing)) {
            return false;
        }
        final CharacterRing other = (CharacterRing) obj;
        if (!Arrays.equals(myCharacters, other.myCharacters)) {
            return false;
        }
        if (myCursor != other.myCursor) {
            return false;
        }
        return true;
    }

    public void flush(final BasicLogger.Printer receiver) {

        final int tmpCursor = myCursor;
        char tmpChar;
        for (int i = tmpCursor; i < length; i++) {
            tmpChar = myCharacters[i];
            if (tmpChar != ASCII.NULL) {
                receiver.print(tmpChar);
            }
        }
        for (int i = 0; i < tmpCursor; i++) {
            tmpChar = myCharacters[i];
            if (tmpChar != ASCII.NULL) {
                receiver.print(tmpChar);
            }
        }

        this.clear();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + Arrays.hashCode(myCharacters);
        result = (prime * result) + myCursor;
        return result;
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

    char getCursor() {
        return myCursor;
    }

}

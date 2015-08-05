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
import java.util.Locale;

import org.ojalgo.access.Access2D;
import org.ojalgo.netio.BasicLogger.Appender;
import org.ojalgo.netio.BasicLogger.GenericAppender;
import org.ojalgo.netio.BasicLogger.LoggerCache;
import org.ojalgo.type.context.NumberContext;

/**
 * A circular char buffer - an {@linkplain Appendable} {@linkplain CharSequence} that always hold exactly
 * 65536 characters. Whenever you append something the oldest entry gets overwritten.
 *
 * @author apete
 */
public class CharacterRing implements CharSequence, Appendable, Serializable {

    static final class DelimitedCache implements LoggerCache {

        private final char myDelimiter;
        private final CharacterRing myRing;
        private final BasicLogger.Appender myRingAsAppender;

        DelimitedCache(final CharacterRing ring) {
            this(ring, ASCII.CR);
        }

        DelimitedCache(final CharacterRing ring, final char delimiter) {

            super();

            myRing = ring;
            myRingAsAppender = ring.asAppender();

            myDelimiter = delimiter;
        }

        public char charAt(final int index) {
            // TODO Auto-generated method stub

            final int tmpFirst = this.indexOfFirst();

            return myRing.charAt(tmpFirst + index);
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof DelimitedCache)) {
                return false;
            }
            final DelimitedCache other = (DelimitedCache) obj;
            if (myRing == null) {
                if (other.myRing != null) {
                    return false;
                }
            } else if (!myRing.equals(other.myRing)) {
                return false;
            }
            if (myDelimiter != other.myDelimiter) {
                return false;
            }
            return true;
        }

        public void flush(final Appender appender) {
            appender.print(this.toString());
            myRing.clear();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = (prime * result) + ((myRing == null) ? 0 : myRing.hashCode());
            result = (prime * result) + myDelimiter;
            return result;
        }

        public int length() {
            // TODO Auto-generated method stub

            final int tmpFirst = this.indexOfFirst();
            final int tmpLimit = this.indexOfLimit();

            return tmpLimit - tmpFirst;
        }

        public void print(final boolean b) {
            myRingAsAppender.print(myDelimiter);
            myRingAsAppender.print(b);
        }

        public void print(final char c) {
            myRingAsAppender.print(myDelimiter);
            myRingAsAppender.print(c);
        }

        public void print(final char[] ca) {
            myRingAsAppender.print(myDelimiter);
            myRingAsAppender.print(ca);
        }

        public void print(final double d) {
            myRingAsAppender.print(myDelimiter);
            myRingAsAppender.print(d);
        }

        public void print(final float f) {
            myRingAsAppender.print(myDelimiter);
            myRingAsAppender.print(f);
        }

        public void print(final int i) {
            myRingAsAppender.print(myDelimiter);
            myRingAsAppender.print(i);
        }

        public void print(final long l) {
            myRingAsAppender.print(myDelimiter);
            myRingAsAppender.print(l);
        }

        public void print(final Object obj) {
            myRingAsAppender.print(myDelimiter);
            myRingAsAppender.print(obj);
        }

        public void print(final String str) {
            myRingAsAppender.print(myDelimiter);
            myRingAsAppender.print(str);
        }

        public void print(final String message, final Object... args) {
            myRingAsAppender.print(myDelimiter);
            myRingAsAppender.print(message, args);
        }

        public Appender printf(final Locale locale, final String format, final Object... args) {
            return myRingAsAppender.printf(locale, format, args);
        }

        public Appender printf(final String format, final Object... args) {
            return myRingAsAppender.printf(format, args);
        }

        public void println() {
            myRingAsAppender.print(myDelimiter);
            myRingAsAppender.println();
        }

        public void println(final boolean b) {
            myRingAsAppender.print(myDelimiter);
            myRingAsAppender.println(b);
        }

        public void println(final char c) {
            myRingAsAppender.print(myDelimiter);
            myRingAsAppender.println(c);
        }

        public void println(final char[] ca) {
            myRingAsAppender.print(myDelimiter);
            myRingAsAppender.println(ca);
        }

        public void println(final double d) {
            myRingAsAppender.print(myDelimiter);
            myRingAsAppender.println(d);
        }

        public void println(final float f) {
            myRingAsAppender.print(myDelimiter);
            myRingAsAppender.println(f);
        }

        public void println(final int i) {
            myRingAsAppender.print(myDelimiter);
            myRingAsAppender.println(i);
        }

        public void println(final long l) {
            myRingAsAppender.print(myDelimiter);
            myRingAsAppender.println(l);
        }

        public void println(final Object obj) {
            myRingAsAppender.print(myDelimiter);
            myRingAsAppender.println(obj);
        }

        public void println(final String str) {
            myRingAsAppender.print(myDelimiter);
            myRingAsAppender.println(str);
        }

        public void println(final String message, final Object... args) {
            myRingAsAppender.print(myDelimiter);
            myRingAsAppender.println(message, args);
        }

        public void printmtrx(final String message, final Access2D<?> matrix) {
            myRingAsAppender.print(myDelimiter);
            myRingAsAppender.printmtrx(message, matrix);
        }

        public void printmtrx(final String message, final Access2D<?> matrix, final NumberContext context) {
            myRingAsAppender.print(myDelimiter);
            myRingAsAppender.printmtrx(message, matrix, context);
        }

        public CharSequence subSequence(final int start, final int end) {
            // TODO Auto-generated method stub

            final int tmpFirst = this.indexOfFirst();

            return myRing.subSequence(tmpFirst + start, tmpFirst + end);
        }

        @Override
        public String toString() {
            // TODO Auto-generated method stub

            final int tmpFirst = this.indexOfFirst();
            final int tmpLimit = this.indexOfLimit();

            return myRing.subSequence(tmpFirst, tmpLimit).toString();
        }

        private int indexOfFirst() {
            return myRing.indexOfFirst(myDelimiter) + 1;
        }

        private int indexOfLimit() {
            return myRing.getCursor();
        }

    }

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

    public LoggerCache asLoggerCache() {
        return new DelimitedCache(this);
    }

    public LoggerCache asLoggerCache(final char entryDelimiter) {
        return new DelimitedCache(this, entryDelimiter);
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

            this.clear();

        } catch (final IOException exception) {
            exception.printStackTrace();
        }
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

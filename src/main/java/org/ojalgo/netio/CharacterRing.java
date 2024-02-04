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
import java.io.Writer;
import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.Formatter;
import java.util.Locale;
import java.util.Optional;

/**
 * A circular char buffer - an {@linkplain Appendable} {@linkplain CharSequence} that always hold exactly
 * 65536 characters. Whenever you append something the oldest entry gets overwritten.
 *
 * @author apete
 */
public final class CharacterRing implements CharSequence, Appendable, BasicLogger.Buffer {

    public static final class RingLogger implements BasicLogger, BasicLogger.Buffer {

        private transient Formatter myFormatter;
        private final CharacterRing myRing;

        RingLogger() {

            super();

            myRing = new CharacterRing();
        }

        public Optional<Writer> asWriter() {
            return Optional.empty();
        }

        public void clear() {
            myRing.clear();
        }

        public void flush(final Appendable receiver) {
            myRing.flush(receiver);
        }

        public void flush(final BasicLogger receiver) {
            myRing.flush(receiver);
        }

        public void print(final boolean value) {
            try {
                myRing.append(String.valueOf(value));
            } catch (IOException cause) {
                throw new RuntimeException(cause);
            }
        }

        public void print(final byte value) {
            try {
                myRing.append(String.valueOf(value));
            } catch (IOException cause) {
                throw new RuntimeException(cause);
            }
        }

        public void print(final char value) {
            try {
                myRing.append(String.valueOf(value));
            } catch (IOException cause) {
                throw new RuntimeException(cause);
            }
        }

        public void print(final double value) {
            try {
                myRing.append(String.valueOf(value));
            } catch (IOException cause) {
                throw new RuntimeException(cause);
            }
        }

        public void print(final float value) {
            try {
                myRing.append(String.valueOf(value));
            } catch (IOException cause) {
                throw new RuntimeException(cause);
            }
        }

        public void print(final int value) {
            try {
                myRing.append(String.valueOf(value));
            } catch (IOException cause) {
                throw new RuntimeException(cause);
            }
        }

        public void print(final long value) {
            try {
                myRing.append(String.valueOf(value));
            } catch (IOException cause) {
                throw new RuntimeException(cause);
            }
        }

        public void print(final Object object) {
            try {
                myRing.append(String.valueOf(object));
            } catch (IOException cause) {
                throw new RuntimeException(cause);
            }
        }

        public void print(final short value) {
            try {
                myRing.append(String.valueOf(value));
            } catch (IOException cause) {
                throw new RuntimeException(cause);
            }
        }

        public void print(final Throwable throwable) {
            throwable.printStackTrace();
        }

        public void printf(final String format, final Object... args) {
            if (myFormatter == null || myFormatter.locale() != Locale.getDefault()) {
                myFormatter = new Formatter(myRing);
            }
            myFormatter.format(Locale.getDefault(), format, args);
        }

        public void println() {
            try {
                myRing.append(ASCII.LF);
            } catch (IOException cause) {
                throw new RuntimeException(cause);
            }
        }

    }

    public static int length = Character.MAX_VALUE + 1;

    public static RingLogger newRingLogger() {
        return new RingLogger();
    }

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
        if ((obj == null) || !(obj instanceof CharacterRing)) {
            return false;
        }
        CharacterRing other = (CharacterRing) obj;
        if (!Arrays.equals(myCharacters, other.myCharacters) || (myCursor != other.myCursor)) {
            return false;
        }
        return true;
    }

    public void flush(final Appendable receiver) {
        try {
            synchronized (receiver) {
                int cursor = myCursor;
                char tmpChar;
                for (int i = cursor; i < length; i++) {
                    tmpChar = myCharacters[i];
                    if (tmpChar != ASCII.NULL) {
                        receiver.append(tmpChar);
                    }
                }
                for (int i = 0; i < cursor; i++) {
                    tmpChar = myCharacters[i];
                    if (tmpChar != ASCII.NULL) {
                        receiver.append(tmpChar);
                    }
                }
                this.clear();
            }
        } catch (IOException cause) {
            throw new RuntimeException(cause);
        }
    }

    public void flush(final BasicLogger receiver) {
        synchronized (receiver) {
            int cursor = myCursor;
            char tmpChar;
            for (int i = cursor; i < length; i++) {
                tmpChar = myCharacters[i];
                if (tmpChar != ASCII.NULL) {
                    receiver.print(tmpChar);
                }
            }
            for (int i = 0; i < cursor; i++) {
                tmpChar = myCharacters[i];
                if (tmpChar != ASCII.NULL) {
                    receiver.print(tmpChar);
                }
            }
            this.clear();
        }
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = (prime * result) + Arrays.hashCode(myCharacters);
        return (prime * result) + myCursor;
    }

    public int indexOfFirst(final char c) {

        int retVal = -1;

        char cursor = myCursor;
        for (int i = cursor; (retVal < 0) && (i < length); i++) {
            if (myCharacters[i] == c) {
                retVal = i - cursor;
            }
        }
        for (int i = 0; (retVal < 0) && (i < cursor); i++) {
            if (myCharacters[i] == c) {
                retVal = i + cursor;
            }
        }

        return retVal;
    }

    public int indexOfLast(final char c) {

        int retVal = -1;

        char cursor = myCursor;
        for (int i = cursor - 1; (retVal < 0) && (i >= 0); i--) {
            if (myCharacters[i] == c) {
                retVal = i + cursor;
            }
        }
        for (int i = length - 1; (retVal < 0) && (i >= cursor); i--) {
            if (myCharacters[i] == c) {
                retVal = i - cursor;
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

        char cursor = myCursor;

        String firstPart = String.valueOf(myCharacters, cursor, length - cursor);
        String secondPart = String.valueOf(myCharacters, 0, cursor);

        return firstPart + secondPart;
    }

    char getCursor() {
        return myCursor;
    }

}

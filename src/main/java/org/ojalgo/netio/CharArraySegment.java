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

public abstract class CharArraySegment implements CharSequence {

    static final class RootSegment extends CharArraySegment {

        RootSegment(final char[] array, final int start, final int end) {
            super(array, start, end);
        }

        @Override
        int start() {
            return start;
        }

    }

    static final class Subsegment extends CharArraySegment {

        private final CharArraySegment myParent;

        Subsegment(final CharArraySegment parent, final int start, final int end) {
            super(parent, start, end);
            myParent = parent;
        }

        @Override
        int start() {
            return myParent.start() + start;
        }

    }

    public static CharArraySegment valueOf(final char[] array) {
        return new RootSegment(array, 0, array.length);
    }

    public static CharArraySegment valueOf(final char[] array, final int start, final int end) {
        return new RootSegment(array, start, end);
    }

    private final char[] myArray;

    int end;
    int start;

    CharArraySegment(final char[] array, final int start, final int end) {

        super();

        myArray = array;

        this.start = start;
        this.end = end;
    }

    CharArraySegment(final CharArraySegment parent, final int start, final int end) {

        super();

        myArray = parent.getArray();

        this.start = start;
        this.end = end;
    }

    public final char charAt(final int index) {
        return myArray[this.start() + index];
    }

    public final int length() {
        return end - start;
    }

    public final CharArraySegment subSequence(final int start, final int end) {
        return new Subsegment(this, start, end);
    }

    @Override
    public final String toString() {
        return new String(myArray, this.start(), this.length());
    }

    final char[] getArray() {
        return myArray;
    }

    abstract int start();

}

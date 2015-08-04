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

public class RingLogger implements CharSequence {

    private final CharacterRing myRing = new CharacterRing();

    public RingLogger() {
        super();
    }

    public char charAt(final int index) {
        return myRing.charAt(index);
    }

    public void flush(final Appendable target) {

        final int tmpFirst = myRing.indexOfFirst(ASCII.LF);
        final int tmpLimit = myRing.indexOfLast(ASCII.CR);

        if (tmpLimit > tmpFirst) {
            try {
                target.append(this);
            } catch (final IOException exception) {
                exception.printStackTrace();
            }
        }
    }

    public int length() {

        final int tmpFirst = myRing.indexOfFirst(ASCII.LF);
        final int tmpLimit = myRing.indexOfLast(ASCII.CR);

        return tmpLimit - tmpFirst;
    }

    public void log(final CharSequence chars) {
        try {
            myRing.append(ASCII.LF);
            myRing.append(chars);
            myRing.append(ASCII.CR);
        } catch (final Exception exception) {
            exception.printStackTrace();
        }
    }

    public CharSequence subSequence(final int start, final int end) {
        return myRing.subSequence(start, end);
    }

}

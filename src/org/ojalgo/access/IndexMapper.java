/*
 * Copyright 1997-2017 Optimatika (www.optimatika.se)
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
package org.ojalgo.access;

import java.util.ArrayList;
import java.util.List;

public interface IndexMapper<T extends Comparable<? super T>> {

    final class AnyIndex<T extends Comparable<? super T>> implements IndexMapper<T> {

        private final List<T> myKeys = new ArrayList<>();

        AnyIndex() {
            super();
        }

        public synchronized long toIndex(final T key) {
            long retVal = myKeys.indexOf(key);
            if (retVal < 0L) {
                retVal = myKeys.size();
                myKeys.add(key);
            }
            return retVal;
        }

        public T toKey(final long index) {
            return myKeys.get((int) index);
        }

    }

    /**
     * @return A very simple implementation - you better come up with something else.
     */
    public static <T extends Comparable<? super T>> IndexMapper<T> make() {
        return new AnyIndex<>();
    }

    long toIndex(T key);

    T toKey(long index);

}

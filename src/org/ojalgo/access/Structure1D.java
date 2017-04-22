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

/**
 * A (fixed size) 1-dimensional data structure.
 *
 * @author apete
 */
public interface Structure1D {

    @FunctionalInterface
    public interface IndexCallback {

        /**
         * @param index Index
         */
        void call(final long index);

    }

    static void loopMatching(final Structure1D structureA, final Structure1D structureB, final IndexCallback callback) {
        final long tmpLimit = Math.min(structureA.count(), structureB.count());
        Structure1D.loopRange(0L, tmpLimit, callback);
    }

    static void loopRange(final long first, final long limit, final IndexCallback callback) {
        for (long i = first; i < limit; i++) {
            callback.call(i);
        }
    }

    /**
     * @return The total number of elements in this structure.
     */
    long count();

    default void loopAll(final IndexCallback callback) {
        Structure1D.loopRange(0L, this.count(), callback);
    }

}

/*
 * Copyright 1997-2018 Optimatika
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

/**
 * A (fixed size) 1-dimensional data structure.
 *
 * @author apete
 */
public interface Structure1D {

    class BasicMapper<T> implements IndexMapper<T> {

        private final List<T> myKeys = new ArrayList<>();

        BasicMapper() {
            super();
        }

        public synchronized long toIndex(final T key) {
            long retVal = myKeys.indexOf(key);
            if (retVal < 0L) {
                retVal = this.indexForNewKey(key);
            }
            return retVal;
        }

        public final T toKey(final long index) {
            return myKeys.get((int) index);
        }

        final long indexForNewKey(final T newKey) {
            final long retVal = myKeys.size();
            myKeys.add(newKey);
            return retVal;
        }

    }

    @FunctionalInterface
    public interface IndexCallback {

        /**
         * @param index Index
         */
        void call(final long index);

    }

    public interface IndexMapper<T> {

        /**
         * This default implementation assumes that the index is incremented by 1 when incrementing the key to
         * the next value.
         *
         * @param key The value to increment
         * @return The next (incremented) value
         */
        default T next(final T key) {
            return this.toKey(this.toIndex(key) + 1L);
        }

        /**
         * This default implementation assumes that the index is decremented by 1 when decrementing the key to
         * the previous value.
         *
         * @param key The value to decrement
         * @return The previous (decremented) value
         */
        default T previous(final T key) {
            return this.toKey(this.toIndex(key) - 1L);
        }

        long toIndex(T key);

        T toKey(long index);

    }

    public final class IntIndex implements Comparable<IntIndex> {

        public final int index;

        public IntIndex(final int anIndex) {

            super();

            index = anIndex;
        }

        @SuppressWarnings("unused")
        private IntIndex() {
            this(-1);
        }

        public int compareTo(final IntIndex ref) {
            return Integer.compare(index, ref.index);
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof IntIndex)) {
                return false;
            }
            final IntIndex other = (IntIndex) obj;
            if (index != other.index) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = (prime * result) + index;
            return result;
        }

        @Override
        public String toString() {
            return Integer.toString(index);
        }

    }

    public final class LongIndex implements Comparable<LongIndex> {

        public final long index;

        public LongIndex(final long anIndex) {

            super();

            index = anIndex;
        }

        @SuppressWarnings("unused")
        private LongIndex() {
            this(-1L);
        }

        public int compareTo(final LongIndex ref) {
            return Long.compare(index, ref.index);
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof LongIndex)) {
                return false;
            }
            final LongIndex other = (LongIndex) obj;
            if (index != other.index) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = (prime * result) + (int) (index ^ (index >>> 32));
            return result;
        }

        @Override
        public String toString() {
            return Long.toString(index);
        }

    }

    @FunctionalInterface
    public interface LoopCallback {

        /**
         * for(long i = first; i < limit; i += step)
         *
         * @param first The initial value
         * @param limit The value limit
         * @param step The increment size
         */
        void call(long first, long limit, long step);

    }

    static void loopMatching(final Structure1D structureA, final Structure1D structureB, final IndexCallback callback) {
        final long limit = Math.min(structureA.count(), structureB.count());
        Structure1D.loopRange(0L, limit, callback);
    }

    static void loopRange(final long first, final long limit, final IndexCallback callback) {
        for (long i = first; i < limit; i++) {
            callback.call(i);
        }
    }

    /**
     * @return A very simple implementation - you better come up with something else.
     */
    static <T> IndexMapper<T> mapper() {
        return new BasicMapper<>();
    }

    /**
     * @return The total number of elements in this structure.
     */
    long count();

    default void loopAll(final IndexCallback callback) {
        Structure1D.loopRange(0L, this.count(), callback);
    }

}

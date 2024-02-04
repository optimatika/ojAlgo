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
package org.ojalgo.structure;

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
            return myKeys.get(Math.toIntExact(index));
        }

        final long indexForNewKey(final T newKey) {
            final long retVal = myKeys.size();
            myKeys.add(newKey);
            return retVal;
        }

    }

    /**
     * @deprecated v53 Will be removed!
     */
    @Deprecated
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

        /**
         * For each key (any instance of that type) there is a corresponding index value – 1 or more key
         * instances will be mapped to each index value.
         */
        long toIndex(T key);

        /**
         * In most cases it should be safe to assume that the input index value is valid (matching what would
         * be created by {@link #toIndex(Object)}).
         */
        T toKey(long index);

    }

    public final class IntIndex implements Comparable<IntIndex> {

        public static IntIndex of(final int index) {
            return new IntIndex(index);
        }

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
            if ((obj == null) || !(obj instanceof IntIndex)) {
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
            return Integer.hashCode(index);
        }

        @Override
        public String toString() {
            return Integer.toString(index);
        }

    }

    interface Logical<S extends Structure1D, B extends Logical<S, B>> extends Structure1D {

        B after(S after);

        B before(S before);

    }

    public final class LongIndex implements Comparable<LongIndex> {

        public static LongIndex of(final long index) {
            return new LongIndex(index);
        }

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
            if ((obj == null) || !(obj instanceof LongIndex)) {
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
            return Long.hashCode(index);
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

    static int index(final long index) {
        return Math.toIntExact(index);
    }

    /**
     * @deprecated v53 Will be removed!
     */
    @Deprecated
    static void loopMatching(final Structure1D structureA, final Structure1D structureB, final IndexCallback callback) {
        long limit = Math.min(structureA.count(), structureB.count());
        for (long i = 0L; i < limit; i++) {
            callback.call(i);
        }
    }

    /**
     * @deprecated v53 Will be removed!
     */
    @Deprecated
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

    static int[] newDecreasingRange(final int first, final int count) {
        final int[] retVal = new int[count];
        for (int i = 0; i < count; i++) {
            retVal[i] = first - i;
        }
        return retVal;
    }

    static long[] newDecreasingRange(final long first, final int count) {
        final long[] retVal = new long[count];
        for (int i = 0; i < count; i++) {
            retVal[i] = first - i;
        }
        return retVal;
    }

    static int[] newIncreasingRange(final int first, final int count) {
        final int[] retVal = new int[count];
        for (int i = 0; i < count; i++) {
            retVal[i] = first + i;
        }
        return retVal;
    }

    static long[] newIncreasingRange(final long first, final int count) {
        final long[] retVal = new long[count];
        for (int i = 0; i < count; i++) {
            retVal[i] = first + i;
        }
        return retVal;
    }

    static long[] replaceNullOrEmptyWithFull(final long[] suggested, final int fullSize) {
        if (suggested != null && suggested.length > 0) {
            return suggested;
        } else {
            return Structure1D.newIncreasingRange(0L, fullSize);
        }
    }

    static int[] toIntIndexes(final long[] indexes) {
        int[] retVal = new int[indexes.length];
        for (int i = 0; i < indexes.length; i++) {
            retVal[i] = Math.toIntExact(indexes[i]);
        }
        return retVal;
    }

    static long[] toLongIndexes(final int[] indexes) {
        long[] retVal = new long[indexes.length];
        for (int i = 0; i < indexes.length; i++) {
            retVal[i] = indexes[i];
        }
        return retVal;
    }

    /**
     * @return The total number of elements in this structure.
     */
    long count();

    /**
     * @deprecated v53 Will be removed!
     */
    @Deprecated
    default void loopAll(final IndexCallback callback) {
        for (long i = 0L; i < this.count(); i++) {
            callback.call(i);
        }
    }

    default int size() {
        return Math.toIntExact(this.count());
    }

}

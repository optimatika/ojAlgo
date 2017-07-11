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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public interface IndexMapper<T> {

    class AnyIndex<T> implements IndexMapper<T> {

        private final List<T> myKeys = new ArrayList<>();

        AnyIndex() {
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

    public class Keys2D<ROW_TYPE, COL_TYPE> {

        private final IndexMapper<COL_TYPE> myColumnMapper;
        private final IndexMapper<ROW_TYPE> myRowMapper;
        private final long myStructure;

        protected Keys2D(final IndexMapper<ROW_TYPE> rowMapper, final long maxNumberOfRows, final IndexMapper<COL_TYPE> columnMapper) {
            super();
            myRowMapper = rowMapper;
            myStructure = maxNumberOfRows;
            myColumnMapper = columnMapper;
        }

        public COL_TYPE toColumnKey(final long index) {
            final long col = Structure2D.column(index, myStructure);
            return myColumnMapper.toKey(col);
        }

        public long toIndex(final ROW_TYPE rowKey, final COL_TYPE colKey) {

            final long row = myRowMapper.toIndex(rowKey);
            final long col = myColumnMapper.toIndex(colKey);

            return Structure2D.index(myStructure, row, col);
        }

        public ROW_TYPE toRowKey(final long index) {
            final long row = Structure2D.row(index, myStructure);
            return myRowMapper.toKey(row);
        }

    }

    public class KeysAnyD implements IndexMapper<Object[]> {

        private final IndexMapper<Object>[] myMappers;
        private final long[] myStructure;

        protected KeysAnyD(final IndexMapper<Object>[] mappers, final long[] structure) {
            super();
            myMappers = mappers;
            myStructure = structure;
        }

        public long toIndex(final Object[] keys) {

            final long[] ref = new long[keys.length];

            for (int i = 0; i < ref.length; i++) {
                ref[i] = myMappers[i].toIndex(keys[i]);
            }

            return StructureAnyD.index(myStructure, ref);
        }

        public Object[] toKey(final long index) {

            final long[] ref = StructureAnyD.reference(index, myStructure);

            final Object[] retVal = new Object[ref.length];

            for (int i = 0; i < ref.length; i++) {
                retVal[i] = myMappers[i].toKey(ref[i]);

            }
            return retVal;
        }

        @SuppressWarnings("unchecked")
        public <T extends Comparable<? super T>> T toKey(final long index, final int dim) {
            final long[] ref = StructureAnyD.reference(index, myStructure);
            return (T) myMappers[dim].toKey(ref[dim]);
        }

    }

    final class LargerIndex<T> extends AnyIndex<T> {

        private final Map<T, Long> myIndices = new ConcurrentHashMap<>();

        LargerIndex() {
            super();
        }

        @Override
        public long toIndex(final T key) {
            return myIndices.computeIfAbsent(key, k -> this.indexForNewKey(k)).longValue();
        }

    }

    /**
     * @return A very simple implementation - you better come up with something else.
     */
    public static <T> IndexMapper<T> make() {
        return IndexMapper.make(false);
    }

    /**
     * @return A very simple implementation - you better come up with something else.
     */
    public static <T> IndexMapper<T> make(final boolean larger) {
        return larger ? new LargerIndex<>() : new AnyIndex<>();
    }

    public static IndexMapper.KeysAnyD make(final IndexMapper<Object>[] mappers, final long[] structure) {
        return new IndexMapper.KeysAnyD(mappers, structure);
    }

    public static <ROW_TYPE, COL_TYPE> IndexMapper.Keys2D<ROW_TYPE, COL_TYPE> make(final IndexMapper<ROW_TYPE> rowMappwer, final long maxNumberOfRows,
            final IndexMapper<COL_TYPE> columnMappwer) {
        return new IndexMapper.Keys2D<>(rowMappwer, maxNumberOfRows, columnMappwer);
    }

    /**
     * This default implementation assumes that the index is incremented by 1 when incrementing the key to the
     * next value.
     *
     * @param key The value to increment
     * @return The next (incremented) value
     */
    default T next(final T key) {
        return this.toKey(this.toIndex(key) + 1L);
    }

    /**
     * This default implementation assumes that the index is decremented by 1 when decrementing the key to the
     * previous value.
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

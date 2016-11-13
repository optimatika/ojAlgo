/*
 * Copyright 1997-2016 Optimatika (www.optimatika.se)
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
package org.ojalgo.series;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.ojalgo.access.IndexMapper;
import org.ojalgo.array.ArrayFactory;
import org.ojalgo.array.LongToNumberMap;
import org.ojalgo.series.primitive.PrimitiveSeries;

final class SparseSeries<K extends Comparable<? super K>, N extends Number> extends NewAbstractSeries<K, N, SparseSeries<K, N>> {

    private final LongToNumberMap<N> myDelegate;

    SparseSeries(final ArrayFactory<N> arrayFactory, final IndexMapper<K> indexMapper) {
        super(indexMapper);
        myDelegate = new LongToNumberMap<>(arrayFactory);
    }

    SparseSeries(final IndexMapper<K> indexMapper, final LongToNumberMap<N> delegate) {
        super(indexMapper);
        myDelegate = delegate;
    }

    public double doubleValue(final K key) {
        return myDelegate.doubleValue(indexMapper.toIndex(key));
    }

    @Override
    public Set<Map.Entry<K, N>> entrySet() {
        return new AbstractSet<Map.Entry<K, N>>() {

            @Override
            public Iterator<Map.Entry<K, N>> iterator() {

                final Iterator<Map.Entry<Long, N>> tmpDelegateIterator = myDelegate.entrySet().iterator();

                return new Iterator<Map.Entry<K, N>>() {

                    public boolean hasNext() {
                        return tmpDelegateIterator.hasNext();

                    }

                    public Map.Entry<K, N> next() {

                        final Map.Entry<Long, N> tmpDelegateNext = tmpDelegateIterator.next();

                        return new Map.Entry<K, N>() {

                            public K getKey() {
                                return indexMapper.toKey(tmpDelegateNext.getKey());
                            }

                            public N getValue() {
                                return tmpDelegateNext.getValue();
                            }

                            public N setValue(final N value) {
                                return tmpDelegateNext.setValue(value);
                            }

                        };

                    }
                };
            }

            @Override
            public int size() {
                return myDelegate.size();
            }
        };
    }

    public K firstKey() {
        return indexMapper.toKey(myDelegate.firstKey());
    }

    @SuppressWarnings("unchecked")
    @Override
    public N get(final Object key) {
        if (key instanceof Comparable<?>) {
            return myDelegate.get(indexMapper.toIndex((K) key));
        } else {
            return null;
        }
    }

    public PrimitiveSeries getPrimitiveSeries() {
        return PrimitiveSeries.wrap(myDelegate.values());
    }

    public K lastKey() {
        return indexMapper.toKey(myDelegate.lastKey());
    }

    public K nextKey() {
        return indexMapper.toKey(myDelegate.lastKey() + 1L);
    }

    public double put(final K key, final double value) {

        final long tmpIndex = indexMapper.toIndex(key);

        return myDelegate.put(tmpIndex, value);
    }

    @Override
    public N put(final K key, final N value) {

        final long tmpIndex = indexMapper.toIndex(key);

        return myDelegate.put(tmpIndex, value);
    }

    @Override
    public SparseSeries<K, N> subMap(final K fromKey, final K toKey) {
        final long tmpFromIndex = indexMapper.toIndex(fromKey);
        final long tmpToIndex = indexMapper.toIndex(toKey);
        final LongToNumberMap<N> tmpSubMap = myDelegate.subMap(tmpFromIndex, tmpToIndex);
        return new SparseSeries<>(indexMapper, tmpSubMap);
    }

}

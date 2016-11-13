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

import org.ojalgo.access.ElementView1D;
import org.ojalgo.access.IndexMapper;
import org.ojalgo.array.ArrayFactory;
import org.ojalgo.array.NumberList;
import org.ojalgo.series.primitive.PrimitiveSeries;

final class DenseSeries<K extends Comparable<? super K>, N extends Number> extends NewAbstractSeries<K, N, DenseSeries<K, N>> {

    private final NumberList<N> myDelegate;

    DenseSeries(final ArrayFactory<N> arrayFactory, final IndexMapper<K> indexMapper) {
        super(indexMapper);
        myDelegate = new NumberList<>(arrayFactory);
    }

    DenseSeries(final IndexMapper<K> indexMapper, final NumberList<N> delegate) {
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

                final Iterator<ElementView1D<N, ?>> tmpDelegateIterator = (Iterator<ElementView1D<N, ?>>) myDelegate.elements().iterator();

                return new Iterator<Map.Entry<K, N>>() {

                    public boolean hasNext() {
                        return tmpDelegateIterator.hasNext();

                    }

                    public Map.Entry<K, N> next() {

                        final ElementView1D<N, ?> tmpDelegateNext = tmpDelegateIterator.next();

                        return new Map.Entry<K, N>() {

                            public K getKey() {
                                return indexMapper.toKey(tmpDelegateNext.index());
                            }

                            public N getValue() {
                                return tmpDelegateNext.getNumber();
                            }

                            public N setValue(final N value) {
                                throw new UnsupportedOperationException();
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
        return indexMapper.toKey(0L);
    }

    @SuppressWarnings("unchecked")
    @Override
    public N get(final Object key) {
        return myDelegate.get(indexMapper.toIndex((K) key));
    }

    public PrimitiveSeries getPrimitiveSeries() {
        return PrimitiveSeries.wrap(myDelegate);
    }

    public K lastKey() {
        return indexMapper.toKey(myDelegate.count() - 1L);
    }

    public K nextKey() {
        return indexMapper.toKey(myDelegate.count());
    }

    public double put(final K key, final double value) {

        final long tmpIndex = indexMapper.toIndex(key);

        final double tmpOldVal = myDelegate.doubleValue(tmpIndex);

        myDelegate.set(tmpIndex, value);

        return tmpOldVal;
    }

    @Override
    public N put(final K key, final N value) {

        final long tmpIndex = indexMapper.toIndex(key);

        final N tmpOldVal = myDelegate.get(tmpIndex);

        myDelegate.set(tmpIndex, value);

        return tmpOldVal;
    }

    @Override
    public DenseSeries<K, N> subMap(final K fromKey, final K toKey) {
        return new DenseSeries<>(indexMapper, myDelegate.subList((int) indexMapper.toIndex(fromKey), (int) indexMapper.toIndex(toKey)));
    }

}

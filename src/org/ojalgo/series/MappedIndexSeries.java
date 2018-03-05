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
package org.ojalgo.series;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.ojalgo.access.Structure1D;
import org.ojalgo.array.DenseArray;
import org.ojalgo.array.LongToNumberMap;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.netio.ASCII;
import org.ojalgo.series.primitive.PrimitiveSeries;
import org.ojalgo.type.CalendarDate.Resolution;
import org.ojalgo.type.ColourData;
import org.ojalgo.type.TypeUtils;

final class MappedIndexSeries<K extends Comparable<? super K>, N extends Number> extends AbstractMap<K, N> implements BasicSeries.NaturallySequenced<K, N> {

    static final Structure1D.IndexMapper<Double> MAPPER = new Structure1D.IndexMapper<Double>() {

        public long toIndex(final Double key) {
            return MappedIndexSeries.toIndex(key);
        }

        public Double toKey(final long index) {
            return MappedIndexSeries.toKey(index);
        }

    };

    static long toIndex(final double key) {
        if (key >= PrimitiveMath.ZERO) {
            return Double.doubleToLongBits(key);
        } else {
            throw new IllegalArgumentException("Negative keys not supported!");
        }
    }

    static double toKey(final long index) {
        return Double.longBitsToDouble(index);
    }

    private final BinaryFunction<N> myAccumulator;
    private ColourData myColour = null;
    private final LongToNumberMap<N> myDelegate;
    private final IndexMapper<K> myMapper;
    private String myName = null;

    MappedIndexSeries(final DenseArray.Factory<N> arrayFactory, final IndexMapper<K> indexMapper, final BinaryFunction<N> accumulator) {
        super();
        myDelegate = LongToNumberMap.factory(arrayFactory).make();
        myMapper = indexMapper;
        myAccumulator = accumulator;
    }

    MappedIndexSeries(final IndexMapper<K> indexMapper, final LongToNumberMap<N> delegate, final BinaryFunction<N> accumulator) {
        super();
        myDelegate = delegate;
        myMapper = indexMapper;
        myAccumulator = accumulator;
    }

    public PrimitiveSeries asPrimitive() {
        return PrimitiveSeries.wrap(myDelegate.values());
    }

    public MappedIndexSeries<K, N> colour(final ColourData colour) {
        this.setColour(colour);
        return this;
    }

    public Comparator<? super K> comparator() {
        return null;
    }

    public double doubleValue(final K key) {
        return myDelegate.doubleValue(myMapper.toIndex(key));
    }

    public double doubleValue(final long index) {
        return myDelegate.doubleValue(index);
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
                                return myMapper.toKey(tmpDelegateNext.getKey());
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
        return myMapper.toKey(myDelegate.firstKey());
    }

    public N firstValue() {
        return this.get(this.firstKey());
    }

    public N get(final K key) {
        return myDelegate.get(myMapper.toIndex(key));
    }

    public N get(final long index) {
        return myDelegate.get(index);
    }

    @SuppressWarnings("unchecked")
    @Override
    public N get(final Object key) {
        if (key instanceof Comparable<?>) {
            return myDelegate.get(myMapper.toIndex((K) key));
        } else {
            return null;
        }
    }

    public ColourData getColour() {
        if (myColour == null) {
            myColour = ColourData.random();
        }
        return myColour;
    }

    public String getName() {
        if (myName == null) {
            myName = UUID.randomUUID().toString();
        }
        return myName;
    }

    @Override
    public MappedIndexSeries<K, N> headMap(final K toKey) {
        return this.subMap(this.firstKey(), toKey);
    }

    public K lastKey() {
        return myMapper.toKey(myDelegate.lastKey());
    }

    public N lastValue() {
        return this.get(this.lastKey());
    }

    public IndexMapper<K> mapper() {
        return myMapper;
    }

    public MappedIndexSeries<K, N> name(final String name) {
        this.setName(name);
        return this;
    }

    public K nextKey() {
        return myMapper.toKey(myDelegate.lastKey() + 1L);
    }

    public double put(final K key, final double value) {
        return this.put(myMapper.toIndex(key), value);
    }

    @Override
    public N put(final K key, final N value) {
        return this.put(myMapper.toIndex(key), value);
    }

    public double put(final long index, final double value) {
        if (myAccumulator != null) {
            return myDelegate.mix(index, myAccumulator, value);
        } else {
            return myDelegate.put(index, value);
        }
    }

    public N put(final long index, final N value) {
        if (myAccumulator != null) {
            return myDelegate.mix(index, myAccumulator, value);
        } else {
            return myDelegate.put(index, value);
        }
    }

    public MappedIndexSeries<K, N> resample(final K firstKey, final K lastKey, final Resolution resolution) {
        return this.subMap(firstKey, this.step(lastKey)).resample(resolution);
    }

    public MappedIndexSeries<K, N> resample(final Resolution resolution) {

        final long conversion = resolution.toDurationInMillis();

        return new MappedIndexSeries<>(new IndexMapper<K>() {

            public long toIndex(final K key) {
                return myMapper.toIndex(key) / conversion;
            }

            public K toKey(final long index) {
                return myMapper.toKey(index * conversion);
            }

        }, myDelegate, myAccumulator);
    }

    public K step(final K key) {
        return myMapper.next(key);
    }

    @Override
    public MappedIndexSeries<K, N> subMap(final K fromKey, final K toKey) {
        final long tmpFromIndex = myMapper.toIndex(fromKey);
        final long tmpToIndex = myMapper.toIndex(toKey);
        final LongToNumberMap<N> tmpSubMap = myDelegate.subMap(tmpFromIndex, tmpToIndex);
        return new MappedIndexSeries<>(myMapper, tmpSubMap, myAccumulator);
    }

    public MappedIndexSeries<K, N> tailMap(final K fromKey) {
        return this.subMap(fromKey, this.nextKey());
    }

    @Override
    public String toString() {

        final StringBuilder retVal = new StringBuilder();

        if (myName != null) {
            retVal.append(myName);
            retVal.append(ASCII.NBSP);
        }

        if (myColour != null) {
            retVal.append(TypeUtils.toHexString(myColour.getRGB()));
            retVal.append(ASCII.NBSP);
        }

        if (this.size() <= 30) {
            retVal.append(super.toString());
        } else {
            retVal.append("First:");
            retVal.append(this.firstKey());
            retVal.append(ASCII.EQUALS);
            retVal.append(this.firstValue());
            retVal.append(ASCII.NBSP);
            retVal.append("Last:");
            retVal.append(this.lastKey());
            retVal.append(ASCII.EQUALS);
            retVal.append(this.lastValue());
            retVal.append(ASCII.NBSP);
            retVal.append("Size:");
            retVal.append(this.size());
        }

        return retVal.toString();
    }

    void setColour(final ColourData colour) {
        myColour = colour;
    }

    void setName(final String name) {
        myName = name;
    }

}

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
package org.ojalgo.series;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.UnaryOperator;

import org.ojalgo.array.DenseArray;
import org.ojalgo.array.LongToNumberMap;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.netio.ASCII;
import org.ojalgo.series.primitive.PrimitiveSeries;
import org.ojalgo.structure.Structure1D;
import org.ojalgo.structure.Structure1D.IndexMapper;
import org.ojalgo.type.ColourData;
import org.ojalgo.type.TypeUtils;

final class MappedIndexSeries<K extends Comparable<? super K>, N extends Comparable<N>> extends AbstractMap<K, N>
        implements BasicSeries.NaturallySequenced<K, N> {

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

    MappedIndexSeries(final DenseArray.Factory<N> denseArrayFactory, final IndexMapper<K> indexMapper, final BinaryFunction<N> accumulator) {
        super();
        myDelegate = LongToNumberMap.factory(denseArrayFactory).make();
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

    public void complete() {
        this.complete(key -> myMapper.next(key));
    }

    public double doubleValue(final K key) {
        return myDelegate.doubleValue(myMapper.toIndex(key));
    }

    @Override
    public Set<Map.Entry<K, N>> entrySet() {
        return new AbstractSet<Map.Entry<K, N>>() {

            @Override
            public Iterator<Map.Entry<K, N>> iterator() {

                Iterator<Map.Entry<Long, N>> tmpDelegateIterator = myDelegate.entrySet().iterator();

                return new Iterator<Map.Entry<K, N>>() {

                    public boolean hasNext() {
                        return tmpDelegateIterator.hasNext();
                    }

                    public Map.Entry<K, N> next() {

                        Map.Entry<Long, N> tmpDelegateNext = tmpDelegateIterator.next();

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
        long index = myMapper.toIndex(key);
        if (myAccumulator != null) {
            return myDelegate.mix(index, myAccumulator, value);
        } else {
            return myDelegate.put(index, value);
        }
    }

    @Override
    public N put(final K key, final N value) {
        long index = myMapper.toIndex(key);
        if (myAccumulator != null) {
            return myDelegate.mix(index, myAccumulator, value);
        } else {
            return myDelegate.put(index, value);
        }
    }

    public BasicSeries<K, N> resample(final UnaryOperator<K> keyTranslator) {

        MappedIndexSeries<K, N> retVal = new MappedIndexSeries<>(myMapper, this.newDelegateInstance(), this.getAccumulator());

        retVal.setColour(this.getColour());
        retVal.setName(this.getName());

        for (Map.Entry<K, N> entry : this.entrySet()) {
            K key = keyTranslator.apply(entry.getKey());
            N value = entry.getValue();
            retVal.put(key, value);
        }

        return retVal;
    }

    public void setColour(final ColourData colour) {
        myColour = colour;
    }

    public void setName(final String name) {
        myName = name;
    }

    public K step(final K key) {
        return myMapper.next(key);
    }

    @Override
    public MappedIndexSeries<K, N> subMap(final K fromKey, final K toKey) {
        long fromIndex = myMapper.toIndex(fromKey);
        long toIndex = myMapper.toIndex(toKey);
        LongToNumberMap<N> delegateSubMap = myDelegate.subMap(fromIndex, toIndex);
        return new MappedIndexSeries<>(myMapper, delegateSubMap, myAccumulator);
    }

    public MappedIndexSeries<K, N> tailMap(final K fromKey) {
        return this.subMap(fromKey, this.nextKey());
    }

    @Override
    public String toString() {

        StringBuilder retVal = new StringBuilder();

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

    /**
     * A "hack" that will create a new empty delegate {@link LongToNumberMap} instance.
     */
    private LongToNumberMap<N> newDelegateInstance() {
        return myDelegate.headMap(Long.MIN_VALUE);
    }

    BinaryFunction<N> getAccumulator() {
        return myAccumulator;
    }

}

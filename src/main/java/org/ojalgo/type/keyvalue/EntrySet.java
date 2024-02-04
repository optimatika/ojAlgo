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
package org.ojalgo.type.keyvalue;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Spliterator;

import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Mutate1D;
import org.ojalgo.type.NumberDefinition;
import org.ojalgo.type.PrimitiveNumber;

/**
 * Allows you to wrap and treat two arrays as a {@link Collection} of key-value pairs. Implements both
 * {@link List} and {@link Set} but does not in any way test or enforce uniqueness – that's up to the user of
 * this class.
 *
 * @author apete
 */
public abstract class EntrySet<K, V> extends AbstractList<Map.Entry<K, V>> implements EntryList<K, V>, Set<Map.Entry<K, V>> {

    static final class EntryView<K, V> implements Map.Entry<K, V> {

        private final EntrySet<K, V> mySet;
        int index = 0;

        EntryView(final EntrySet<K, V> set) {
            super();
            mySet = set;
        }

        @Override
        public K getKey() {
            return mySet.getKey(index);
        }

        @Override
        public V getValue() {
            return mySet.getValue(index);
        }

        @Override
        public V setValue(final V value) {
            V old = mySet.getValue(index);
            mySet.setValue(index, value);
            return old;
        }

    }

    static abstract class KeyedPrimitives<K> extends EntrySet<K, PrimitiveNumber> implements Access1D<PrimitiveNumber>, Mutate1D {

        KeyedPrimitives(final K[] keys) {
            super(keys);
        }

        @Override
        public final long count() {
            return this.size();
        }

        @Override
        public final PrimitiveNumber get(final long index) {
            return this.getValue(Math.toIntExact(index));
        }

    }

    static final class ObjectByte<K> extends KeyedPrimitives<K> {

        private final byte[] myValues;

        ObjectByte(final K[] keys, final byte[] values) {
            super(keys);
            myValues = values;
        }

        public void add(final long index, final Comparable<?> addend) {
            myValues[Math.toIntExact(index)] += NumberDefinition.byteValue(addend);
        }

        public void add(final long index, final double addend) {
            myValues[Math.toIntExact(index)] += (byte) addend;
        }

        public void add(final long index, final float addend) {
            int intIndex = Math.toIntExact(index);
            myValues[intIndex] = (byte) (myValues[intIndex] + addend);
        }

        @Override
        public byte byteValue(final int index) {
            return myValues[index];
        }

        @Override
        public double doubleValue(final int index) {
            return myValues[index];
        }

        @Override
        public float floatValue(final int index) {
            return myValues[index];
        }

        @Override
        public EntryPair<K, PrimitiveNumber> get(final int index) {
            return EntryPair.of(this.getKey(index), myValues[index]);
        }

        @Override
        public PrimitiveNumber getValue(final int index) {
            return PrimitiveNumber.of(myValues[index]);
        }

        @Override
        public int intValue(final int index) {
            return myValues[index];
        }

        @Override
        public long longValue(final int index) {
            return myValues[index];
        }

        @Override
        public void set(final long index, final Comparable<?> value) {
            myValues[Math.toIntExact(index)] = NumberDefinition.byteValue(value);
        }

        @Override
        public void set(final int index, final double value) {
            myValues[index] = (byte) value;
        }

        @Override
        public void set(final int index, final float value) {
            myValues[index] = (byte) value;
        }

        @Override
        public void setValue(final int index, final PrimitiveNumber value) {
            myValues[index] = value.byteValue();
        }

        @Override
        public short shortValue(final long index) {
            return myValues[Math.toIntExact(index)];
        }

    }

    static final class ObjectDouble<K> extends KeyedPrimitives<K> {

        private final double[] myValues;

        ObjectDouble(final K[] keys, final double[] values) {
            super(keys);
            myValues = values;
        }

        public void add(final long index, final Comparable<?> addend) {
            myValues[Math.toIntExact(index)] += NumberDefinition.doubleValue(addend);
        }

        public void add(final long index, final double addend) {
            myValues[Math.toIntExact(index)] += addend;
        }

        public void add(final long index, final float addend) {
            myValues[Math.toIntExact(index)] += addend;
        }

        @Override
        public double doubleValue(final int index) {
            return myValues[index];
        }

        @Override
        public EntryPair<K, PrimitiveNumber> get(final int index) {
            return EntryPair.of(this.getKey(index), myValues[index]);
        }

        @Override
        public PrimitiveNumber getValue(final int index) {
            return PrimitiveNumber.of(myValues[index]);
        }

        @Override
        public void set(final long index, final Comparable<?> value) {
            myValues[Math.toIntExact(index)] = NumberDefinition.doubleValue(value);
        }

        @Override
        public void set(final int index, final double value) {
            myValues[index] = value;
        }

        @Override
        public void set(final int index, final float value) {
            myValues[index] = value;
        }

        @Override
        public void setValue(final int index, final PrimitiveNumber value) {
            myValues[index] = value.doubleValue();
        }

    }

    static final class ObjectFloat<K> extends KeyedPrimitives<K> {

        private final float[] myValues;

        ObjectFloat(final K[] keys, final float[] values) {
            super(keys);
            myValues = values;
        }

        public void add(final long index, final Comparable<?> addend) {
            myValues[Math.toIntExact(index)] += NumberDefinition.floatValue(addend);
        }

        public void add(final long index, final double addend) {
            myValues[Math.toIntExact(index)] += (float) addend;
        }

        public void add(final long index, final float addend) {
            myValues[Math.toIntExact(index)] += addend;
        }

        @Override
        public double doubleValue(final int index) {
            return myValues[index];
        }

        @Override
        public float floatValue(final int index) {
            return myValues[index];
        }

        @Override
        public EntryPair<K, PrimitiveNumber> get(final int index) {
            return EntryPair.of(this.getKey(index), myValues[index]);
        }

        @Override
        public PrimitiveNumber getValue(final int index) {
            return PrimitiveNumber.of(myValues[index]);
        }

        @Override
        public void set(final long index, final Comparable<?> value) {
            myValues[Math.toIntExact(index)] = NumberDefinition.floatValue(value);
        }

        @Override
        public void set(final int index, final double value) {
            myValues[index] = (float) value;
        }

        @Override
        public void set(final int index, final float value) {
            myValues[index] = value;
        }

        @Override
        public void setValue(final int index, final PrimitiveNumber value) {
            myValues[index] = value.floatValue();
        }

    }

    static final class ObjectInt<K> extends KeyedPrimitives<K> {

        private final int[] myValues;

        ObjectInt(final K[] keys, final int[] values) {
            super(keys);
            myValues = values;
        }

        public void add(final long index, final Comparable<?> addend) {
            myValues[Math.toIntExact(index)] += NumberDefinition.intValue(addend);
        }

        public void add(final long index, final double addend) {
            myValues[Math.toIntExact(index)] += (int) addend;
        }

        public void add(final long index, final float addend) {
            int intIndex = Math.toIntExact(index);
            myValues[intIndex] = (int) (myValues[intIndex] + addend);
        }

        @Override
        public double doubleValue(final int index) {
            return myValues[index];
        }

        @Override
        public float floatValue(final int index) {
            return myValues[index];
        }

        @Override
        public EntryPair<K, PrimitiveNumber> get(final int index) {
            return EntryPair.of(this.getKey(index), myValues[index]);
        }

        @Override
        public PrimitiveNumber getValue(final int index) {
            return PrimitiveNumber.of(myValues[index]);
        }

        @Override
        public int intValue(final int index) {
            return myValues[index];
        }

        @Override
        public long longValue(final int index) {
            return myValues[index];
        }

        @Override
        public void set(final long index, final Comparable<?> value) {
            myValues[Math.toIntExact(index)] = NumberDefinition.intValue(value);
        }

        @Override
        public void set(final int index, final double value) {
            myValues[index] = (int) value;
        }

        @Override
        public void set(final int index, final float value) {
            myValues[index] = (int) value;
        }

        @Override
        public void setValue(final int index, final PrimitiveNumber value) {
            myValues[index] = value.intValue();
        }

    }

    static final class ObjectLong<K> extends KeyedPrimitives<K> {

        private final long[] myValues;

        ObjectLong(final K[] keys, final long[] values) {
            super(keys);
            myValues = values;
        }

        public void add(final long index, final Comparable<?> addend) {
            myValues[Math.toIntExact(index)] += NumberDefinition.longValue(addend);
        }

        public void add(final long index, final double addend) {
            myValues[Math.toIntExact(index)] += (long) addend;
        }

        public void add(final int index, final float addend) {
            int intIndex = Math.toIntExact(index);
            myValues[intIndex] = (long) (myValues[intIndex] + addend);
        }

        @Override
        public double doubleValue(final int index) {
            return myValues[index];
        }

        @Override
        public float floatValue(final int index) {
            return myValues[index];
        }

        @Override
        public EntryPair<K, PrimitiveNumber> get(final int index) {
            return EntryPair.of(this.getKey(index), myValues[index]);
        }

        @Override
        public PrimitiveNumber getValue(final int index) {
            return PrimitiveNumber.of(myValues[index]);
        }

        @Override
        public long longValue(final long index) {
            return myValues[Math.toIntExact(index)];
        }

        @Override
        public void set(final long index, final Comparable<?> value) {
            myValues[Math.toIntExact(index)] = NumberDefinition.longValue(value);
        }

        @Override
        public void set(final int index, final double value) {
            myValues[index] = (long) value;
        }

        @Override
        public void set(final int index, final float value) {
            myValues[index] = (long) value;
        }

        @Override
        public void setValue(final int index, final PrimitiveNumber value) {
            myValues[index] = value.longValue();
        }

    }

    static final class ObjectObject<K, V> extends EntrySet<K, V> {

        private final V[] myValues;

        ObjectObject(final K[] keys, final V[] values) {
            super(keys);
            myValues = values;
        }

        @Override
        public EntryPair<K, V> get(final int index) {
            return EntryPair.of(this.getKey(index), myValues[index]);
        }

        @Override
        public V getValue(final int index) {
            return myValues[index];
        }

        @Override
        public void setValue(final int index, final V value) {
            myValues[index] = value;
        }

    }

    static final class ObjectShort<K> extends KeyedPrimitives<K> {

        private final short[] myValues;

        ObjectShort(final K[] keys, final short[] values) {
            super(keys);
            myValues = values;
        }

        public void add(final long index, final Comparable<?> addend) {
            myValues[Math.toIntExact(index)] += NumberDefinition.shortValue(addend);
        }

        public void add(final long index, final double addend) {
            myValues[Math.toIntExact(index)] += (short) addend;
        }

        public void add(final long index, final float addend) {
            int intIndex = Math.toIntExact(index);
            myValues[intIndex] = (short) (myValues[intIndex] + addend);
        }

        @Override
        public double doubleValue(final int index) {
            return myValues[index];
        }

        @Override
        public float floatValue(final int index) {
            return myValues[index];
        }

        @Override
        public EntryPair<K, PrimitiveNumber> get(final int index) {
            return EntryPair.of(this.getKey(index), myValues[index]);
        }

        @Override
        public PrimitiveNumber getValue(final int index) {
            return PrimitiveNumber.of(myValues[index]);
        }

        @Override
        public int intValue(final long index) {
            return myValues[Math.toIntExact(index)];
        }

        @Override
        public long longValue(final long index) {
            return myValues[Math.toIntExact(index)];
        }

        @Override
        public void set(final long index, final Comparable<?> value) {
            myValues[Math.toIntExact(index)] = NumberDefinition.shortValue(value);
        }

        @Override
        public void set(final long index, final double value) {
            myValues[Math.toIntExact(index)] = (short) value;
        }

        @Override
        public void set(final long index, final float value) {
            myValues[Math.toIntExact(index)] = (short) value;
        }

        @Override
        public void setValue(final int index, final PrimitiveNumber value) {
            myValues[index] = value.shortValue();
        }

        @Override
        public short shortValue(final long index) {
            return myValues[Math.toIntExact(index)];
        }

        @Override
        public void set(final int index, final double value) {
            myValues[index] = (short) value;
        }

    }

    static final class ViewingIterator<K, V> implements Iterator<Map.Entry<K, V>> {

        private final int myLastIndex;
        private final EntryView<K, V> myView;

        ViewingIterator(final EntrySet<K, V> set) {
            super();
            myView = new EntryView<>(set);
            myView.index = -1;
            myLastIndex = set.size() - 1;
        }

        @Override
        public boolean hasNext() {
            return myView.index < myLastIndex;
        }

        @Override
        public Entry<K, V> next() {
            myView.index++;
            return myView;
        }

    }

    private final K[] myKeys;

    EntrySet(final K[] keys) {
        super();
        myKeys = keys;
    }

    @Override
    public abstract EntryPair<K, V> get(int index);

    @Override
    public final K getKey(final int index) {
        return myKeys[index];
    }

    @Override
    public final EntryPair<K, V> getPair(final int index) {
        return this.get(index);
    }

    @Override
    public abstract V getValue(int index);

    @Override
    public final Iterator<Entry<K, V>> iterator() {
        return new ViewingIterator<>(this);
    }

    public abstract void setValue(int index, V value);

    @Override
    public final int size() {
        return myKeys.length;
    }

    @Override
    public Spliterator<Entry<K, V>> spliterator() {
        return super.spliterator();
    }

}

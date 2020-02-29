/*
 * Copyright 1997-2020 Optimatika
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

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.ojalgo.structure.Access1D;
import org.ojalgo.type.PrimitiveNumber;

public abstract class FakeSet<K, V> extends AbstractSet<Map.Entry<K, V>> {

    static final class EntryView<K, V> implements Map.Entry<K, V> {

        private final FakeSet<K, V> mySet;
        int index = 0;

        EntryView(FakeSet<K, V> set) {
            super();
            mySet = set;
        }

        public K getKey() {
            return mySet.getKey(index);
        }

        public V getValue() {
            return mySet.getValue(index);
        }

        public V setValue(V value) {
            V old = mySet.getValue(index);
            mySet.setValue(index, value);
            return old;
        }

    }

    static abstract class KeyedPrimitives<K> extends FakeSet<K, PrimitiveNumber> implements Access1D<PrimitiveNumber> {

        KeyedPrimitives(K[] keys) {
            super(keys);
        }

        public final long count() {
            return this.size();
        }

        public final PrimitiveNumber get(long index) {
            return this.getValue(Math.toIntExact(index));
        }

    }

    static class ObjectByte<K> extends KeyedPrimitives<K> {

        private final byte[] myValues;

        ObjectByte(K[] keys, byte[] values) {
            super(keys);
            myValues = values;
        }

        public byte byteValue(long index) {
            return myValues[Math.toIntExact(index)];
        }

        public double doubleValue(long index) {
            return myValues[Math.toIntExact(index)];
        }

        public float floatValue(long index) {
            return myValues[Math.toIntExact(index)];
        }

        @Override
        public EntryPair<K, PrimitiveNumber> get(int index) {
            return EntryPair.of(this.getKey(index), myValues[index]);
        }

        @Override
        public PrimitiveNumber getValue(int index) {
            return PrimitiveNumber.of(myValues[index]);
        }

        public int intValue(long index) {
            return myValues[Math.toIntExact(index)];
        }

        public long longValue(long index) {
            return myValues[Math.toIntExact(index)];
        }

        @Override
        public void setValue(int index, PrimitiveNumber value) {
            myValues[index] = value.byteValue();
        }

        public short shortValue(long index) {
            return myValues[Math.toIntExact(index)];
        }

    }

    static class ObjectDouble<K> extends KeyedPrimitives<K> {

        private final double[] myValues;

        ObjectDouble(K[] keys, double[] values) {
            super(keys);
            myValues = values;
        }

        public double doubleValue(long index) {
            return myValues[Math.toIntExact(index)];
        }

        @Override
        public EntryPair<K, PrimitiveNumber> get(int index) {
            return EntryPair.of(this.getKey(index), myValues[index]);
        }

        @Override
        public PrimitiveNumber getValue(int index) {
            return PrimitiveNumber.of(myValues[index]);
        }

        @Override
        public void setValue(int index, PrimitiveNumber value) {
            myValues[index] = value.doubleValue();
        }

    }

    static class ObjectFloat<K> extends KeyedPrimitives<K> {

        private final float[] myValues;

        ObjectFloat(K[] keys, float[] values) {
            super(keys);
            myValues = values;
        }

        public double doubleValue(long index) {
            return myValues[Math.toIntExact(index)];
        }

        public float floatValue(long index) {
            return myValues[Math.toIntExact(index)];
        }

        @Override
        public EntryPair<K, PrimitiveNumber> get(int index) {
            return EntryPair.of(this.getKey(index), myValues[index]);
        }

        @Override
        public PrimitiveNumber getValue(int index) {
            return PrimitiveNumber.of(myValues[index]);
        }

        @Override
        public void setValue(int index, PrimitiveNumber value) {
            myValues[index] = value.floatValue();
        }

    }

    static class ObjectInt<K> extends KeyedPrimitives<K> {

        private final int[] myValues;

        ObjectInt(K[] keys, int[] values) {
            super(keys);
            myValues = values;
        }

        public double doubleValue(long index) {
            return myValues[Math.toIntExact(index)];
        }

        public float floatValue(long index) {
            return myValues[Math.toIntExact(index)];
        }

        @Override
        public EntryPair<K, PrimitiveNumber> get(int index) {
            return EntryPair.of(this.getKey(index), myValues[index]);
        }

        @Override
        public PrimitiveNumber getValue(int index) {
            return PrimitiveNumber.of(myValues[index]);
        }

        public int intValue(long index) {
            return myValues[Math.toIntExact(index)];
        }

        public long longValue(long index) {
            return myValues[Math.toIntExact(index)];
        }

        @Override
        public void setValue(int index, PrimitiveNumber value) {
            myValues[index] = value.intValue();
        }

    }

    static class ObjectLong<K> extends KeyedPrimitives<K> {

        private final long[] myValues;

        ObjectLong(K[] keys, long[] values) {
            super(keys);
            myValues = values;
        }

        public double doubleValue(long index) {
            return myValues[Math.toIntExact(index)];
        }

        public float floatValue(long index) {
            return myValues[Math.toIntExact(index)];
        }

        @Override
        public EntryPair<K, PrimitiveNumber> get(int index) {
            return EntryPair.of(this.getKey(index), myValues[index]);
        }

        @Override
        public PrimitiveNumber getValue(int index) {
            return PrimitiveNumber.of(myValues[index]);
        }

        public long longValue(long index) {
            return myValues[Math.toIntExact(index)];
        }

        @Override
        public void setValue(int index, PrimitiveNumber value) {
            myValues[index] = value.longValue();
        }

    }

    static final class ObjectObject<K, V> extends FakeSet<K, V> {

        private final V[] myValues;

        ObjectObject(K[] keys, V[] values) {
            super(keys);
            myValues = values;
        }

        @Override
        public EntryPair<K, V> get(int index) {
            return EntryPair.of(this.getKey(index), myValues[index]);
        }

        @Override
        public V getValue(int index) {
            return myValues[index];
        }

        @Override
        public void setValue(int index, V value) {
            myValues[index] = value;
        }

    }

    static class ObjectShort<K> extends KeyedPrimitives<K> {

        private final short[] myValues;

        ObjectShort(K[] keys, short[] values) {
            super(keys);
            myValues = values;
        }

        public double doubleValue(long index) {
            return myValues[Math.toIntExact(index)];
        }

        public float floatValue(long index) {
            return myValues[Math.toIntExact(index)];
        }

        @Override
        public EntryPair<K, PrimitiveNumber> get(int index) {
            return EntryPair.of(this.getKey(index), myValues[index]);
        }

        @Override
        public PrimitiveNumber getValue(int index) {
            return PrimitiveNumber.of(myValues[index]);
        }

        public int intValue(long index) {
            return myValues[Math.toIntExact(index)];
        }

        public long longValue(long index) {
            return myValues[Math.toIntExact(index)];
        }

        @Override
        public void setValue(int index, PrimitiveNumber value) {
            myValues[index] = value.shortValue();
        }

        public short shortValue(long index) {
            return myValues[Math.toIntExact(index)];
        }

    }

    static final class ViewingIterator<K, V> implements Iterator<Map.Entry<K, V>> {

        private final int myLastIndex;
        private final EntryView<K, V> myView;

        ViewingIterator(FakeSet<K, V> set) {
            super();
            myView = new EntryView<>(set);
            myView.index = -1;
            myLastIndex = set.size() - 1;
        }

        public boolean hasNext() {
            return myView.index < myLastIndex;
        }

        public Entry<K, V> next() {
            myView.index++;
            return myView;
        }

    }

    public static <K> FakeSet<K, PrimitiveNumber> of(K[] keys, byte[] values) {
        return new FakeSet.ObjectByte<>(keys, values);
    }

    public static <K> FakeSet<K, PrimitiveNumber> of(K[] keys, double[] values) {
        return new FakeSet.ObjectDouble<>(keys, values);
    }

    public static <K> FakeSet<K, PrimitiveNumber> of(K[] keys, float[] values) {
        return new FakeSet.ObjectFloat<>(keys, values);
    }

    public static <K> FakeSet<K, PrimitiveNumber> of(K[] keys, int[] values) {
        return new FakeSet.ObjectInt<>(keys, values);
    }

    public static <K> FakeSet<K, PrimitiveNumber> of(K[] keys, long[] values) {
        return new FakeSet.ObjectLong<>(keys, values);
    }

    public static <K> FakeSet<K, PrimitiveNumber> of(K[] keys, short[] values) {
        return new FakeSet.ObjectShort<>(keys, values);
    }

    public static <K, V> FakeSet<K, V> of(K[] keys, V[] values) {
        return new FakeSet.ObjectObject<>(keys, values);
    }

    private final K[] myKeys;

    FakeSet(K[] keys) {
        super();
        myKeys = keys;
    }

    public final Map<K, V> asMap() {
        return new FakeMap<>(this);
    }

    public abstract EntryPair<K, V> get(int index);

    public final K getKey(int index) {
        return myKeys[index];
    }

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

}

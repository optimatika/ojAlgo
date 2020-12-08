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
package org.ojalgo.core.type.keyvalue;

import java.lang.reflect.Array;
import java.util.AbstractMap;
import java.util.Map;

import org.ojalgo.core.structure.Access1D;
import org.ojalgo.core.structure.Mutate1D;
import org.ojalgo.core.type.PrimitiveNumber;

/**
 * Wrap two arrays (of keys and values) and treat the result as a {@link Map}. There is no check that the
 * supplied array of keys does not contain duplicates – the index is the real "key".
 *
 * @author apete
 */
public class IndexedMap<K, V> extends AbstractMap<K, V> implements Paired<K, V> {

    public static final class MappedPrimitives<K> extends IndexedMap<K, PrimitiveNumber> implements Access1D<PrimitiveNumber>, Mutate1D {

        private final EntrySet.KeyedPrimitives<K> myEntries;

        MappedPrimitives(final EntrySet.KeyedPrimitives<K> entries) {
            super(entries);
            myEntries = entries;
        }

        public byte byteValue(final long index) {
            return myEntries.byteValue(index);
        }

        public long count() {
            return myEntries.count();
        }

        public double doubleValue(final long index) {
            return myEntries.doubleValue(index);
        }

        public float floatValue(final long index) {
            return myEntries.floatValue(index);
        }

        public PrimitiveNumber get(final long index) {
            return myEntries.get(index);
        }

        public int intValue(final long index) {
            return myEntries.intValue(index);
        }

        public long longValue(final long index) {
            return myEntries.longValue(index);
        }

        public void set(final long index, final Comparable<?> value) {
            myEntries.set(index, value);
        }

        public void set(final long index, final double value) {
            myEntries.set(index, value);
        }

        public void set(final long index, final float value) {
            myEntries.set(index, value);
        }

        public short shortValue(final long index) {
            return myEntries.shortValue(index);
        }

    }

    public static <K extends Enum<K>> IndexedMap.MappedPrimitives<K> of(final Class<K> keyType, final byte defaultValue) {

        K[] keys = keyType.getEnumConstants();
        byte[] values = new byte[keys.length];

        for (int i = 0; i < values.length; i++) {
            values[i] = defaultValue;
        }

        return new IndexedMap.MappedPrimitives<>(new EntrySet.ObjectByte<>(keys, values));
    }

    public static <K extends Enum<K>, V> IndexedMap<K, V> of(final Class<K> keyType, final Class<V> valueType) {

        K[] keys = keyType.getEnumConstants();
        @SuppressWarnings("unchecked")
        V[] values = (V[]) Array.newInstance(valueType, keys.length);

        for (int i = 0; i < values.length; i++) {
            try {
                values[i] = valueType.newInstance();
            } catch (InstantiationException | IllegalAccessException exception) {
                values[i] = null;
            }
        }

        return new IndexedMap<>(new EntrySet.ObjectObject<>(keys, values));
    }

    public static <K extends Enum<K>> IndexedMap.MappedPrimitives<K> of(final Class<K> keyType, final double defaultValue) {

        K[] keys = keyType.getEnumConstants();
        double[] values = new double[keys.length];

        for (int i = 0; i < values.length; i++) {
            values[i] = defaultValue;
        }

        return new IndexedMap.MappedPrimitives<>(new EntrySet.ObjectDouble<>(keys, values));
    }

    public static <K extends Enum<K>> IndexedMap.MappedPrimitives<K> of(final Class<K> keyType, final float defaultValue) {

        K[] keys = keyType.getEnumConstants();
        float[] values = new float[keys.length];

        for (int i = 0; i < values.length; i++) {
            values[i] = defaultValue;
        }

        return new IndexedMap.MappedPrimitives<>(new EntrySet.ObjectFloat<>(keys, values));
    }

    public static <K extends Enum<K>> IndexedMap.MappedPrimitives<K> of(final Class<K> keyType, final int defaultValue) {

        K[] keys = keyType.getEnumConstants();
        int[] values = new int[keys.length];

        for (int i = 0; i < values.length; i++) {
            values[i] = defaultValue;
        }

        return new IndexedMap.MappedPrimitives<>(new EntrySet.ObjectInt<>(keys, values));
    }

    public static <K extends Enum<K>> IndexedMap.MappedPrimitives<K> of(final Class<K> keyType, final long defaultValue) {

        K[] keys = keyType.getEnumConstants();
        long[] values = new long[keys.length];

        for (int i = 0; i < values.length; i++) {
            values[i] = defaultValue;
        }

        return new IndexedMap.MappedPrimitives<>(new EntrySet.ObjectLong<>(keys, values));
    }

    public static <K extends Enum<K>> IndexedMap.MappedPrimitives<K> of(final Class<K> keyType, final short defaultValue) {

        K[] keys = keyType.getEnumConstants();
        short[] values = new short[keys.length];

        for (int i = 0; i < values.length; i++) {
            values[i] = defaultValue;
        }

        return new IndexedMap.MappedPrimitives<>(new EntrySet.ObjectShort<>(keys, values));
    }

    public static <K extends Enum<K>, V> IndexedMap<K, V> of(final Class<K> keyType, final V defaultValue) {

        K[] keys = keyType.getEnumConstants();
        @SuppressWarnings("unchecked")
        V[] values = (V[]) Array.newInstance(defaultValue.getClass(), keys.length);

        for (int i = 0; i < values.length; i++) {
            values[i] = defaultValue;
        }

        return new IndexedMap<>(new EntrySet.ObjectObject<>(keys, values));
    }

    private final EntrySet<K, V> myEntrySet;

    IndexedMap(final EntrySet<K, V> entries) {
        super();
        myEntrySet = entries;
    }

    @Override
    public EntrySet<K, V> entrySet() {
        return myEntrySet;
    }

    public K getKey(final int index) {
        return myEntrySet.getKey(index);
    }

    public EntryPair<K, V> getPair(final int index) {
        return myEntrySet.getPair(index);
    }

    public V getValue(final int index) {
        return myEntrySet.getValue(index);
    }

    @Override
    public int size() {
        return myEntrySet.size();
    }

}

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

import java.lang.reflect.Array;
import java.util.AbstractMap;
import java.util.Map;

import org.ojalgo.type.PrimitiveNumber;
import org.ojalgo.type.keyvalue.EntrySet.ObjectByte;
import org.ojalgo.type.keyvalue.EntrySet.ObjectDouble;
import org.ojalgo.type.keyvalue.EntrySet.ObjectFloat;
import org.ojalgo.type.keyvalue.EntrySet.ObjectInt;
import org.ojalgo.type.keyvalue.EntrySet.ObjectLong;
import org.ojalgo.type.keyvalue.EntrySet.ObjectObject;
import org.ojalgo.type.keyvalue.EntrySet.ObjectShort;

/**
 * Wrap two arrays (of keys and values) and treat the result as a {@link Map}. There is no check that the
 * supplied array of keys does not contain duplicates – the index is the real "key".
 *
 * @author apete
 */
public final class IndexedMap<K, V> extends AbstractMap<K, V> implements Paired<K, V> {

    public static <K extends Enum<K>> IndexedMap<K, PrimitiveNumber> of(Class<K> keyType, byte defaultValue) {
    
        K[] keys = keyType.getEnumConstants();
        byte[] values = new byte[keys.length];
    
        for (int i = 0; i < values.length; i++) {
            values[i] = defaultValue;
        }
    
        return new IndexedMap<>(new EntrySet.ObjectByte<>(keys, values));
    }

    public static <K extends Enum<K>, V> IndexedMap<K, V> of(Class<K> keyType, Class<V> valueType) {
    
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

    public static <K extends Enum<K>> IndexedMap<K, PrimitiveNumber> of(Class<K> keyType, double defaultValue) {
    
        K[] keys = keyType.getEnumConstants();
        double[] values = new double[keys.length];
    
        for (int i = 0; i < values.length; i++) {
            values[i] = defaultValue;
        }
    
        return new IndexedMap<>(new EntrySet.ObjectDouble<>(keys, values));
    }

    public static <K extends Enum<K>> IndexedMap<K, PrimitiveNumber> of(Class<K> keyType, float defaultValue) {
    
        K[] keys = keyType.getEnumConstants();
        float[] values = new float[keys.length];
    
        for (int i = 0; i < values.length; i++) {
            values[i] = defaultValue;
        }
    
        return new IndexedMap<>(new EntrySet.ObjectFloat<>(keys, values));
    }

    public static <K extends Enum<K>> IndexedMap<K, PrimitiveNumber> of(Class<K> keyType, int defaultValue) {
    
        K[] keys = keyType.getEnumConstants();
        int[] values = new int[keys.length];
    
        for (int i = 0; i < values.length; i++) {
            values[i] = defaultValue;
        }
    
        return new IndexedMap<>(new EntrySet.ObjectInt<>(keys, values));
    }

    public static <K extends Enum<K>> IndexedMap<K, PrimitiveNumber> of(Class<K> keyType, long defaultValue) {
    
        K[] keys = keyType.getEnumConstants();
        long[] values = new long[keys.length];
    
        for (int i = 0; i < values.length; i++) {
            values[i] = defaultValue;
        }
    
        return new IndexedMap<>(new EntrySet.ObjectLong<>(keys, values));
    }

    public static <K extends Enum<K>> IndexedMap<K, PrimitiveNumber> of(Class<K> keyType, short defaultValue) {
    
        K[] keys = keyType.getEnumConstants();
        short[] values = new short[keys.length];
    
        for (int i = 0; i < values.length; i++) {
            values[i] = defaultValue;
        }
    
        return new IndexedMap<>(new EntrySet.ObjectShort<>(keys, values));
    }

    public static <K extends Enum<K>, V> IndexedMap<K, V> of(Class<K> keyType, V defaultValue) {
    
        K[] keys = keyType.getEnumConstants();
        @SuppressWarnings("unchecked")
        V[] values = (V[]) Array.newInstance(defaultValue.getClass(), keys.length);
    
        for (int i = 0; i < values.length; i++) {
            values[i] = defaultValue;
        }
    
        return new IndexedMap<>(new EntrySet.ObjectObject<>(keys, values));
    }

    private final EntrySet<K, V> myEntrySet;

    IndexedMap(EntrySet<K, V> entries) {
        super();
        myEntrySet = entries;
    }

    @Override
    public EntrySet<K, V> entrySet() {
        return myEntrySet;
    }

    public K getKey(int index) {
        return myEntrySet.getKey(index);
    }

    public EntryPair<K, V> getPair(int index) {
        return myEntrySet.getPair(index);
    }

    public V getValue(int index) {
        return myEntrySet.getValue(index);
    }

    @Override
    public int size() {
        return myEntrySet.size();
    }

}

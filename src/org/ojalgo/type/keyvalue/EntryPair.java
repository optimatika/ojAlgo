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

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.ojalgo.type.NumberDefinition;
import org.ojalgo.type.PrimitiveNumber;

/**
 * Singleton (immutable) {@link Map}:s with primitive valued specialisations.
 *
 * @author apete
 */
public interface EntryPair<K, V> extends Map<K, V>, Map.Entry<K, V> {

    interface KeyedPrimitive<K> extends EntryPair<K, PrimitiveNumber>, PrimitiveNumber {

        default Set<Entry<K, PrimitiveNumber>> entrySet() {
            return Collections.singleton(this);
        }

        default PrimitiveNumber getValue() {
            return this;
        }

        default Collection<PrimitiveNumber> values() {
            return Collections.singleton(this);
        }
    }

    final class ObjectByte<K> implements KeyedPrimitive<K> {

        private final K myKey;
        private final byte myValue;

        ObjectByte(K key, byte value) {
            super();
            myKey = Objects.requireNonNull(key);
            myValue = value;
        }

        public byte byteValue() {
            return myValue;
        }

        public int compareTo(PrimitiveNumber other) {
            return Byte.compare(myValue, other.byteValue());
        }

        public boolean containsKey(Object key) {
            return myKey.equals(key);
        }

        public boolean containsValue(Object value) {
            if ((value != null) && (value instanceof Comparable<?>)) {
                return NumberDefinition.byteValue((Comparable<?>) value) == myValue;
            } else {
                return false;
            }
        }

        public double doubleValue() {
            return myValue;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof ObjectInt)) {
                return false;
            }
            ObjectInt<?> other = (ObjectInt<?>) obj;
            if (myKey == null) {
                if (other.myKey != null) {
                    return false;
                }
            } else if (!myKey.equals(other.myKey)) {
                return false;
            }
            if (myValue != other.myValue) {
                return false;
            }
            return true;
        }

        public PrimitiveNumber get(Object key) {
            if (myKey.equals(key)) {
                return this;
            } else {
                return null;
            }
        }

        public K getKey() {
            return myKey;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = (prime * result) + ((myKey == null) ? 0 : myKey.hashCode());
            result = (prime * result) + myValue;
            return result;
        }

        public int intValue() {
            return myValue;
        }

        public Set<K> keySet() {
            return Collections.singleton(myKey);
        }

        public long longValue() {
            return myValue;
        }

    }

    class ObjectDouble<K> implements KeyedPrimitive<K> {

        private final K myKey;
        private final double myValue;

        ObjectDouble(K key, double value) {
            super();
            myKey = Objects.requireNonNull(key);
            myValue = value;
        }

        public int compareTo(PrimitiveNumber other) {
            return Double.compare(myValue, other.doubleValue());
        }

        public boolean containsKey(Object key) {
            return myKey.equals(key);
        }

        public boolean containsValue(Object value) {
            if ((value != null) && (value instanceof Comparable<?>)) {
                return NumberDefinition.doubleValue((Comparable<?>) value) == myValue;
            } else {
                return false;
            }
        }

        public double doubleValue() {
            return myValue;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof ObjectDouble)) {
                return false;
            }
            ObjectDouble<?> other = (ObjectDouble<?>) obj;
            if (myKey == null) {
                if (other.myKey != null) {
                    return false;
                }
            } else if (!myKey.equals(other.myKey)) {
                return false;
            }
            if (Double.doubleToLongBits(myValue) != Double.doubleToLongBits(other.myValue)) {
                return false;
            }
            return true;
        }

        public PrimitiveNumber get(Object key) {
            if (myKey.equals(key)) {
                return this;
            } else {
                return null;
            }
        }

        public K getKey() {
            return myKey;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = (prime * result) + ((myKey == null) ? 0 : myKey.hashCode());
            long temp;
            temp = Double.doubleToLongBits(myValue);
            result = (prime * result) + (int) (temp ^ (temp >>> 32));
            return result;
        }

        public Set<K> keySet() {
            return Collections.singleton(myKey);
        }

    }

    final class ObjectFloat<K> implements KeyedPrimitive<K> {

        private final K myKey;
        private final float myValue;

        ObjectFloat(K key, float value) {
            super();
            myKey = Objects.requireNonNull(key);
            myValue = value;
        }

        public int compareTo(PrimitiveNumber other) {
            return Float.compare(myValue, other.floatValue());
        }

        public boolean containsKey(Object key) {
            return myKey.equals(key);
        }

        public boolean containsValue(Object value) {
            if ((value != null) && (value instanceof Comparable<?>)) {
                return NumberDefinition.floatValue((Comparable<?>) value) == myValue;
            } else {
                return false;
            }
        }

        public double doubleValue() {
            return myValue;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof ObjectFloat)) {
                return false;
            }
            ObjectFloat<?> other = (ObjectFloat<?>) obj;
            if (myKey == null) {
                if (other.myKey != null) {
                    return false;
                }
            } else if (!myKey.equals(other.myKey)) {
                return false;
            }
            if (Float.floatToIntBits(myValue) != Float.floatToIntBits(other.myValue)) {
                return false;
            }
            return true;
        }

        public float floatValue() {
            return myValue;
        }

        public PrimitiveNumber get(Object key) {
            if (myKey.equals(key)) {
                return this;
            } else {
                return null;
            }
        }

        public K getKey() {
            return myKey;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = (prime * result) + ((myKey == null) ? 0 : myKey.hashCode());
            result = (prime * result) + Float.floatToIntBits(myValue);
            return result;
        }

        public Set<K> keySet() {
            return Collections.singleton(myKey);
        }

    }

    final class ObjectInt<K> implements KeyedPrimitive<K> {

        private final K myKey;
        private final int myValue;

        ObjectInt(K key, int value) {
            super();
            myKey = Objects.requireNonNull(key);
            myValue = value;
        }

        public int compareTo(PrimitiveNumber other) {
            return Integer.compare(myValue, other.intValue());
        }

        public boolean containsKey(Object key) {
            return myKey.equals(key);
        }

        public boolean containsValue(Object value) {
            if ((value != null) && (value instanceof Comparable<?>)) {
                return NumberDefinition.intValue((Comparable<?>) value) == myValue;
            } else {
                return false;
            }
        }

        public double doubleValue() {
            return myValue;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof ObjectInt)) {
                return false;
            }
            ObjectInt<?> other = (ObjectInt<?>) obj;
            if (myKey == null) {
                if (other.myKey != null) {
                    return false;
                }
            } else if (!myKey.equals(other.myKey)) {
                return false;
            }
            if (myValue != other.myValue) {
                return false;
            }
            return true;
        }

        public PrimitiveNumber get(Object key) {
            if (myKey.equals(key)) {
                return this;
            } else {
                return null;
            }
        }

        public K getKey() {
            return myKey;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = (prime * result) + ((myKey == null) ? 0 : myKey.hashCode());
            result = (prime * result) + myValue;
            return result;
        }

        public int intValue() {
            return myValue;
        }

        public Set<K> keySet() {
            return Collections.singleton(myKey);
        }

        public long longValue() {
            return myValue;
        }

    }

    final class ObjectLong<K> implements KeyedPrimitive<K> {

        private final K myKey;
        private final long myValue;

        ObjectLong(K key, long value) {
            super();
            myKey = Objects.requireNonNull(key);
            myValue = value;
        }

        public int compareTo(PrimitiveNumber other) {
            return Long.compare(myValue, other.longValue());
        }

        public boolean containsKey(Object key) {
            return myKey.equals(key);
        }

        public boolean containsValue(Object value) {
            if ((value != null) && (value instanceof Comparable<?>)) {
                return NumberDefinition.longValue((Comparable<?>) value) == myValue;
            } else {
                return false;
            }
        }

        public double doubleValue() {
            return myValue;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof ObjectLong)) {
                return false;
            }
            ObjectLong<?> other = (ObjectLong<?>) obj;
            if (myKey == null) {
                if (other.myKey != null) {
                    return false;
                }
            } else if (!myKey.equals(other.myKey)) {
                return false;
            }
            if (myValue != other.myValue) {
                return false;
            }
            return true;
        }

        public PrimitiveNumber get(Object key) {
            if (myKey.equals(key)) {
                return this;
            } else {
                return null;
            }
        }

        public K getKey() {
            return myKey;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = (prime * result) + ((myKey == null) ? 0 : myKey.hashCode());
            result = (prime * result) + (int) (myValue ^ (myValue >>> 32));
            return result;
        }

        public int intValue() {
            return Math.toIntExact(myValue);
        }

        public Set<K> keySet() {
            return Collections.singleton(myKey);
        }

        public long longValue() {
            return myValue;
        }

    }

    final class ObjectObject<K, V> implements EntryPair<K, V> {

        private final K myKey;
        private final V myValue;

        ObjectObject(K key, V value) {
            super();
            myKey = Objects.requireNonNull(key);
            myValue = Objects.requireNonNull(value);
        }

        public boolean containsKey(Object key) {
            return myKey.equals(key);
        }

        public boolean containsValue(Object value) {
            return myValue.equals(value);
        }

        public Set<Entry<K, V>> entrySet() {
            return Collections.singleton(this);
        }

        public V get(Object key) {
            if (myKey.equals(key)) {
                return myValue;
            } else {
                return null;
            }
        }

        public K getKey() {
            return myKey;
        }

        public V getValue() {
            return myValue;
        }

        public Set<K> keySet() {
            return Collections.singleton(myKey);
        }

        public Collection<V> values() {
            return Collections.singleton(myValue);
        }

    }

    final class ObjectShort<K> implements KeyedPrimitive<K> {

        private final K myKey;
        private final short myValue;

        ObjectShort(K key, short value) {
            super();
            myKey = Objects.requireNonNull(key);
            myValue = value;
        }

        public int compareTo(PrimitiveNumber other) {
            return Short.compare(myValue, other.shortValue());
        }

        public boolean containsKey(Object key) {
            return myKey.equals(key);
        }

        public boolean containsValue(Object value) {
            if ((value != null) && (value instanceof Comparable<?>)) {
                return NumberDefinition.shortValue((Comparable<?>) value) == myValue;
            } else {
                return false;
            }
        }

        public double doubleValue() {
            return myValue;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof ObjectInt)) {
                return false;
            }
            ObjectInt<?> other = (ObjectInt<?>) obj;
            if (myKey == null) {
                if (other.myKey != null) {
                    return false;
                }
            } else if (!myKey.equals(other.myKey)) {
                return false;
            }
            if (myValue != other.myValue) {
                return false;
            }
            return true;
        }

        public PrimitiveNumber get(Object key) {
            if (myKey.equals(key)) {
                return this;
            } else {
                return null;
            }
        }

        public K getKey() {
            return myKey;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = (prime * result) + ((myKey == null) ? 0 : myKey.hashCode());
            result = (prime * result) + myValue;
            return result;
        }

        public int intValue() {
            return myValue;
        }

        public Set<K> keySet() {
            return Collections.singleton(myKey);
        }

        public long longValue() {
            return myValue;
        }

        public short shortValue() {
            return myValue;
        }

    }

    static <K> KeyedPrimitive<K> of(K key, byte value) {
        return new ObjectByte<>(key, value);
    }

    static <K> KeyedPrimitive<K> of(K key, double value) {
        return new ObjectDouble<>(key, value);
    }

    static <K> KeyedPrimitive<K> of(K key, float value) {
        return new ObjectFloat<>(key, value);
    }

    static <K> KeyedPrimitive<K> of(K key, int value) {
        return new ObjectInt<>(key, value);
    }

    static <K> KeyedPrimitive<K> of(K key, long value) {
        return new ObjectLong<>(key, value);
    }

    static <K> KeyedPrimitive<K> of(K key, short value) {
        return new ObjectShort<>(key, value);
    }

    static <K, V> ObjectObject<K, V> of(K key, V value) {
        return new ObjectObject<>(key, value);
    }

    default void clear() {
        throw new UnsupportedOperationException();
    }

    default boolean isEmpty() {
        return false;
    }

    default V put(K key, V value) {
        throw new UnsupportedOperationException();
    }

    default void putAll(Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException();
    }

    default V remove(Object key) {
        throw new UnsupportedOperationException();
    }

    default V setValue(V value) {
        throw new UnsupportedOperationException();
    }

    default int size() {
        return 1;
    }
}

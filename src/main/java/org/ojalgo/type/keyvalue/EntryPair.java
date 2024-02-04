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
public interface EntryPair<K, V> extends KeyValue<K, V>, Map<K, V>, Map.Entry<K, V> {

    interface KeyedPrimitive<K> extends EntryPair<K, PrimitiveNumber>, PrimitiveNumber {

        @Override
        default PrimitiveNumber getValue() {
            return this;
        }

        @Override
        default PrimitiveNumber right() {
            return this;
        }

        @Override
        default PrimitiveNumber second() {
            return this;
        }

        @Override
        default Collection<PrimitiveNumber> values() {
            return Collections.singleton(this);
        }
    }

    final class ObjectByte<K> implements KeyedPrimitive<K> {

        private final K myKey;
        private final byte myValue;

        ObjectByte(final K key, final byte value) {
            super();
            myKey = Objects.requireNonNull(key);
            myValue = value;
        }

        @Override
        public byte byteValue() {
            return myValue;
        }

        @Override
        public int compareTo(final PrimitiveNumber other) {
            return Byte.compare(myValue, other.byteValue());
        }

        @Override
        public boolean containsKey(final Object key) {
            return myKey.equals(key);
        }

        @Override
        public boolean containsValue(final Object value) {
            if (value instanceof Comparable<?>) {
                return NumberDefinition.byteValue((Comparable<?>) value) == myValue;
            }
            return false;
        }

        @Override
        public double doubleValue() {
            return myValue;
        }

        @Override
        public boolean equals(final Object obj) {
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

        @Override
        public K first() {
            return myKey;
        }

        @Override
        public PrimitiveNumber get(final Object key) {
            if (myKey.equals(key)) {
                return this;
            }
            return null;
        }

        @Override
        public K getKey() {
            return myKey;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (myKey == null ? 0 : myKey.hashCode());
            return prime * result + myValue;
        }

        @Override
        public int intValue() {
            return myValue;
        }

        @Override
        public Set<K> keySet() {
            return Collections.singleton(myKey);
        }

        @Override
        public K left() {
            return myKey;
        }

        @Override
        public long longValue() {
            return myValue;
        }

        @Override
        public String toString() {
            return myKey + "=" + myValue;
        }

    }

    final class ObjectDouble<K> implements KeyedPrimitive<K> {

        private final K myKey;
        private final double myValue;

        ObjectDouble(final K key, final double value) {
            super();
            myKey = Objects.requireNonNull(key);
            myValue = value;
        }

        @Override
        public int compareTo(final PrimitiveNumber other) {
            return Double.compare(myValue, other.doubleValue());
        }

        @Override
        public boolean containsKey(final Object key) {
            return myKey.equals(key);
        }

        @Override
        public boolean containsValue(final Object value) {
            if (value instanceof Comparable<?>) {
                return NumberDefinition.doubleValue((Comparable<?>) value) == myValue;
            }
            return false;
        }

        @Override
        public double doubleValue() {
            return myValue;
        }

        @Override
        public boolean equals(final Object obj) {
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

        @Override
        public K first() {
            return myKey;
        }

        @Override
        public PrimitiveNumber get(final Object key) {
            if (myKey.equals(key)) {
                return this;
            }
            return null;
        }

        @Override
        public K getKey() {
            return myKey;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (myKey == null ? 0 : myKey.hashCode());
            long temp;
            temp = Double.doubleToLongBits(myValue);
            return prime * result + (int) (temp ^ temp >>> 32);
        }

        @Override
        public Set<K> keySet() {
            return Collections.singleton(myKey);
        }

        @Override
        public K left() {
            return myKey;
        }

        @Override
        public String toString() {
            return myKey + "=" + myValue;
        }

    }

    final class ObjectFloat<K> implements KeyedPrimitive<K> {

        private final K myKey;
        private final float myValue;

        ObjectFloat(final K key, final float value) {
            super();
            myKey = Objects.requireNonNull(key);
            myValue = value;
        }

        @Override
        public int compareTo(final PrimitiveNumber other) {
            return Float.compare(myValue, other.floatValue());
        }

        @Override
        public boolean containsKey(final Object key) {
            return myKey.equals(key);
        }

        @Override
        public boolean containsValue(final Object value) {
            if (value instanceof Comparable<?>) {
                return NumberDefinition.floatValue((Comparable<?>) value) == myValue;
            }
            return false;
        }

        @Override
        public double doubleValue() {
            return myValue;
        }

        @Override
        public boolean equals(final Object obj) {
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

        @Override
        public K first() {
            return myKey;
        }

        @Override
        public float floatValue() {
            return myValue;
        }

        @Override
        public PrimitiveNumber get(final Object key) {
            if (myKey.equals(key)) {
                return this;
            }
            return null;
        }

        @Override
        public K getKey() {
            return myKey;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (myKey == null ? 0 : myKey.hashCode());
            return prime * result + Float.floatToIntBits(myValue);
        }

        @Override
        public Set<K> keySet() {
            return Collections.singleton(myKey);
        }

        @Override
        public K left() {
            return myKey;
        }

        @Override
        public String toString() {
            return myKey + "=" + myValue;
        }

    }

    final class ObjectInt<K> implements KeyedPrimitive<K> {

        private final K myKey;
        private final int myValue;

        ObjectInt(final K key, final int value) {
            super();
            myKey = Objects.requireNonNull(key);
            myValue = value;
        }

        @Override
        public int compareTo(final PrimitiveNumber other) {
            return Integer.compare(myValue, other.intValue());
        }

        @Override
        public boolean containsKey(final Object key) {
            return myKey.equals(key);
        }

        @Override
        public boolean containsValue(final Object value) {
            if (value instanceof Comparable<?>) {
                return NumberDefinition.intValue((Comparable<?>) value) == myValue;
            }
            return false;
        }

        @Override
        public double doubleValue() {
            return myValue;
        }

        @Override
        public boolean equals(final Object obj) {
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

        @Override
        public K first() {
            return myKey;
        }

        @Override
        public PrimitiveNumber get(final Object key) {
            if (myKey.equals(key)) {
                return this;
            }
            return null;
        }

        @Override
        public K getKey() {
            return myKey;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (myKey == null ? 0 : myKey.hashCode());
            return prime * result + myValue;
        }

        @Override
        public int intValue() {
            return myValue;
        }

        @Override
        public Set<K> keySet() {
            return Collections.singleton(myKey);
        }

        @Override
        public K left() {
            return myKey;
        }

        @Override
        public long longValue() {
            return myValue;
        }

        @Override
        public String toString() {
            return myKey + "=" + myValue;
        }

    }

    final class ObjectLong<K> implements KeyedPrimitive<K> {

        private final K myKey;
        private final long myValue;

        ObjectLong(final K key, final long value) {
            super();
            myKey = Objects.requireNonNull(key);
            myValue = value;
        }

        @Override
        public int compareTo(final PrimitiveNumber other) {
            return Long.compare(myValue, other.longValue());
        }

        @Override
        public boolean containsKey(final Object key) {
            return myKey.equals(key);
        }

        @Override
        public boolean containsValue(final Object value) {
            if (value instanceof Comparable<?>) {
                return NumberDefinition.longValue((Comparable<?>) value) == myValue;
            }
            return false;
        }

        @Override
        public double doubleValue() {
            return myValue;
        }

        @Override
        public boolean equals(final Object obj) {
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

        @Override
        public K first() {
            return myKey;
        }

        @Override
        public PrimitiveNumber get(final Object key) {
            if (myKey.equals(key)) {
                return this;
            }
            return null;
        }

        @Override
        public K getKey() {
            return myKey;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (myKey == null ? 0 : myKey.hashCode());
            return prime * result + (int) (myValue ^ myValue >>> 32);
        }

        @Override
        public int intValue() {
            return Math.toIntExact(myValue);
        }

        @Override
        public Set<K> keySet() {
            return Collections.singleton(myKey);
        }

        @Override
        public K left() {
            return myKey;
        }

        @Override
        public long longValue() {
            return myValue;
        }

        @Override
        public String toString() {
            return myKey + "=" + myValue;
        }

    }

    final class ObjectObject<K, V> implements EntryPair<K, V> {

        private final K myKey;
        private final V myValue;

        ObjectObject(final K key, final V value) {
            super();
            myKey = Objects.requireNonNull(key);
            myValue = Objects.requireNonNull(value);
        }

        @Override
        public boolean containsKey(final Object key) {
            return myKey.equals(key);
        }

        @Override
        public boolean containsValue(final Object value) {
            return myValue.equals(value);
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof ObjectObject)) {
                return false;
            }
            ObjectObject<?, ?> other = (ObjectObject<?, ?>) obj;
            if (myKey == null) {
                if (other.myKey != null) {
                    return false;
                }
            } else if (!myKey.equals(other.myKey)) {
                return false;
            }
            if (myValue == null) {
                if (other.myValue != null) {
                    return false;
                }
            } else if (!myValue.equals(other.myValue)) {
                return false;
            }
            return true;
        }

        @Override
        public K first() {
            return myKey;
        }

        @Override
        public V get(final Object key) {
            if (myKey.equals(key)) {
                return myValue;
            }
            return null;
        }

        @Override
        public K getKey() {
            return myKey;
        }

        @Override
        public V getValue() {
            return myValue;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (myKey == null ? 0 : myKey.hashCode());
            return prime * result + (myValue == null ? 0 : myValue.hashCode());
        }

        @Override
        public Set<K> keySet() {
            return Collections.singleton(myKey);
        }

        @Override
        public K left() {
            return myKey;
        }

        @Override
        public V right() {
            return myValue;
        }

        @Override
        public V second() {
            return myValue;
        }

        @Override
        public String toString() {
            return myKey + "=" + myValue;
        }

        @Override
        public Collection<V> values() {
            return Collections.singleton(myValue);
        }

    }

    final class ObjectShort<K> implements KeyedPrimitive<K> {

        private final K myKey;
        private final short myValue;

        ObjectShort(final K key, final short value) {
            super();
            myKey = Objects.requireNonNull(key);
            myValue = value;
        }

        @Override
        public int compareTo(final PrimitiveNumber other) {
            return Short.compare(myValue, other.shortValue());
        }

        @Override
        public boolean containsKey(final Object key) {
            return myKey.equals(key);
        }

        @Override
        public boolean containsValue(final Object value) {
            if (value instanceof Comparable<?>) {
                return NumberDefinition.shortValue((Comparable<?>) value) == myValue;
            }
            return false;
        }

        @Override
        public double doubleValue() {
            return myValue;
        }

        @Override
        public boolean equals(final Object obj) {
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

        @Override
        public K first() {
            return myKey;
        }

        @Override
        public PrimitiveNumber get(final Object key) {
            if (myKey.equals(key)) {
                return this;
            }
            return null;
        }

        @Override
        public K getKey() {
            return myKey;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (myKey == null ? 0 : myKey.hashCode());
            return prime * result + myValue;
        }

        @Override
        public int intValue() {
            return myValue;
        }

        @Override
        public Set<K> keySet() {
            return Collections.singleton(myKey);
        }

        @Override
        public K left() {
            return myKey;
        }

        @Override
        public long longValue() {
            return myValue;
        }

        @Override
        public short shortValue() {
            return myValue;
        }

        @Override
        public String toString() {
            return myKey + "=" + myValue;
        }

    }

    static <K> KeyedPrimitive<K> of(final K key, final byte value) {
        return new ObjectByte<>(key, value);
    }

    static <K> KeyedPrimitive<K> of(final K key, final double value) {
        return new ObjectDouble<>(key, value);
    }

    static <K> KeyedPrimitive<K> of(final K key, final float value) {
        return new ObjectFloat<>(key, value);
    }

    static <K> KeyedPrimitive<K> of(final K key, final int value) {
        return new ObjectInt<>(key, value);
    }

    static <K> KeyedPrimitive<KeyValue.Dual<K>> of(final K key1, final K key2, final byte value) {
        return EntryPair.of(new KeyValue.Dual<>(key1, key2), value);
    }

    static <K> KeyedPrimitive<KeyValue.Dual<K>> of(final K key1, final K key2, final double value) {
        return EntryPair.of(new KeyValue.Dual<>(key1, key2), value);
    }

    static <K> KeyedPrimitive<KeyValue.Dual<K>> of(final K key1, final K key2, final float value) {
        return EntryPair.of(new KeyValue.Dual<>(key1, key2), value);
    }

    static <K> KeyedPrimitive<KeyValue.Dual<K>> of(final K key1, final K key2, final int value) {
        return EntryPair.of(new KeyValue.Dual<>(key1, key2), value);
    }

    static <K> KeyedPrimitive<KeyValue.Dual<K>> of(final K key1, final K key2, final long value) {
        return EntryPair.of(new KeyValue.Dual<>(key1, key2), value);
    }

    static <K> KeyedPrimitive<KeyValue.Dual<K>> of(final K key1, final K key2, final short value) {
        return EntryPair.of(new KeyValue.Dual<>(key1, key2), value);
    }

    static <K, V> EntryPair<KeyValue.Dual<K>, V> of(final K key1, final K key2, final V value) {
        return EntryPair.of(new KeyValue.Dual<>(key1, key2), value);
    }

    static <K> KeyedPrimitive<K> of(final K key, final long value) {
        return new ObjectLong<>(key, value);
    }

    static <K> KeyedPrimitive<K> of(final K key, final short value) {
        return new ObjectShort<>(key, value);
    }

    static <K, V> EntryPair<K, V> of(final K key, final V value) {
        return new ObjectObject<>(key, value);
    }

    default KeyedPrimitive<EntryPair<K, V>> asKeyTo(final byte value) {
        return EntryPair.of(this, value);
    }

    default KeyedPrimitive<EntryPair<K, V>> asKeyTo(final double value) {
        return EntryPair.of(this, value);
    }

    default KeyedPrimitive<EntryPair<K, V>> asKeyTo(final float value) {
        return EntryPair.of(this, value);
    }

    default KeyedPrimitive<EntryPair<K, V>> asKeyTo(final int value) {
        return EntryPair.of(this, value);
    }

    default KeyedPrimitive<EntryPair<K, V>> asKeyTo(final long value) {
        return EntryPair.of(this, value);
    }

    default KeyedPrimitive<EntryPair<K, V>> asKeyTo(final short value) {
        return EntryPair.of(this, value);
    }

    default <T> EntryPair<EntryPair<K, V>, T> asKeyTo(final T value) {
        return EntryPair.of(this, value);
    }

    default <T> EntryPair<T, EntryPair<K, V>> asValueTo(final T key) {
        return EntryPair.of(key, this);
    }

    @Override
    default void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    default Set<Entry<K, V>> entrySet() {
        return Collections.singleton(this);
    }

    @Override
    default boolean isEmpty() {
        return false;
    }

    @Override
    default V put(final K key, final V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    default void putAll(final Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    default V remove(final Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    default V setValue(final V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    default int size() {
        return 1;
    }

}

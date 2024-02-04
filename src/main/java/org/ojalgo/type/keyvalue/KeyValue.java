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

import java.util.Map;

import org.ojalgo.type.PrimitiveNumber;

/**
 * A pair, like {@link Map.Entry} without {@link Map.Entry#setValue(Object)}.
 *
 * @author apete
 */
public interface KeyValue<K, V> {

    /**
     * A pair of the same type.
     *
     * @author apete
     */
    final class Dual<T> implements KeyValue<T, T> {

        public final T first;
        public final T second;

        Dual(final T obj1, final T obj2) {

            super();

            first = obj1;
            second = obj2;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Dual)) {
                return false;
            }
            Dual<?> other = (Dual<?>) obj;
            if (first == null) {
                if (other.first != null) {
                    return false;
                }
            } else if (!first.equals(other.first)) {
                return false;
            }
            if (second == null) {
                if (other.second != null) {
                    return false;
                }
            } else if (!second.equals(other.second)) {
                return false;
            }
            return true;
        }

        @Override
        public T first() {
            return first;
        }

        @Override
        public T getKey() {
            return first;
        }

        @Override
        public T getValue() {
            return second;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (first == null ? 0 : first.hashCode());
            return prime * result + (second == null ? 0 : second.hashCode());
        }

        @Override
        public T left() {
            return first;
        }

        @Override
        public T right() {
            return second;
        }

        @Override
        public T second() {
            return second;
        }

    }

    static <K> KeyValue<K, PrimitiveNumber> of(final K key, final byte value) {
        return EntryPair.of(key, value);
    }

    static <K> KeyValue<K, PrimitiveNumber> of(final K key, final double value) {
        return EntryPair.of(key, value);
    }

    static <K> KeyValue<K, PrimitiveNumber> of(final K key, final float value) {
        return EntryPair.of(key, value);
    }

    static <K> KeyValue<K, PrimitiveNumber> of(final K key, final int value) {
        return EntryPair.of(key, value);
    }

    static <K> KeyValue<KeyValue.Dual<K>, PrimitiveNumber> of(final K key1, final K key2, final byte value) {
        return EntryPair.of(key1, key2, value);
    }

    static <K> KeyValue<KeyValue.Dual<K>, PrimitiveNumber> of(final K key1, final K key2, final double value) {
        return EntryPair.of(key1, key2, value);
    }

    static <K> KeyValue<KeyValue.Dual<K>, PrimitiveNumber> of(final K key1, final K key2, final float value) {
        return EntryPair.of(key1, key2, value);
    }

    static <K> KeyValue<KeyValue.Dual<K>, PrimitiveNumber> of(final K key1, final K key2, final int value) {
        return EntryPair.of(key1, key2, value);
    }

    static <K> KeyValue<KeyValue.Dual<K>, PrimitiveNumber> of(final K key1, final K key2, final long value) {
        return EntryPair.of(key1, key2, value);
    }

    static <K> KeyValue<KeyValue.Dual<K>, PrimitiveNumber> of(final K key1, final K key2, final short value) {
        return EntryPair.of(key1, key2, value);
    }

    static <K, V> KeyValue<KeyValue.Dual<K>, V> of(final K key1, final K key2, final V value) {
        return EntryPair.of(key1, key2, value);
    }

    static <K> KeyValue<K, PrimitiveNumber> of(final K key, final long value) {
        return EntryPair.of(key, value);
    }

    static <K> KeyValue<K, PrimitiveNumber> of(final K key, final short value) {
        return EntryPair.of(key, value);
    }

    static <K, V> KeyValue<K, V> of(final K key, final V value) {
        return EntryPair.of(key, value);
    }

    default K first() {
        return this.getKey();
    }

    K getKey();

    V getValue();

    default K left() {
        return this.getKey();
    }

    default V right() {
        return this.getValue();
    }

    default V second() {
        return this.getValue();
    }

}

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
package org.ojalgo.type.keyvalue;

import org.ojalgo.netio.ASCII;

public final class ComparableToObject<K extends Comparable<? super K>, V extends Object> implements KeyValue<K, V> {

    public final K key;
    public final V value;

    public ComparableToObject(final K aKey, final V avalue) {

        super();

        key = aKey;
        value = avalue;
    }

    @SuppressWarnings("unused")
    private ComparableToObject() {
        this(null, null);
    }

    public int compareTo(final KeyValue<K, ?> aReference) {
        return key.compareTo(aReference.getKey());
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ComparableToObject)) {
            return false;
        }
        final ComparableToObject<?, ?> other = (ComparableToObject<?, ?>) obj;
        if (key == null) {
            if (other.key != null) {
                return false;
            }
        } else if (!key.equals(other.key)) {
            return false;
        }
        return true;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((key == null) ? 0 : key.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return String.valueOf(key) + String.valueOf(ASCII.EQUALS) + String.valueOf(value);
    }

}

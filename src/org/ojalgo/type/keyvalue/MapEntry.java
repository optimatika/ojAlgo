/*
 * Copyright 1997-2015 Optimatika (www.optimatika.se)
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

import org.ojalgo.netio.ASCII;

/**
 * This class is primarily an implementation of {@linkplain Map.Entry}, but also implemnts
 * {@linkplain KeyValue}.
 * 
 * @author apete
 */
public final class MapEntry<K extends Comparable<K>, V extends Object> implements Map.Entry<K, V>, KeyValue<K, V> {

    private final K myKey;
    private final Map<K, V> myMap;
    private V myValue;

    public MapEntry(final K aKey, final V aValue) {

        super();

        myKey = aKey;
        myMap = null;
        myValue = aValue;
    }

    public MapEntry(final Map.Entry<K, V> anEntry) {

        super();

        myKey = anEntry.getKey();
        myMap = null;
        myValue = anEntry.getValue();
    }

    public MapEntry(final Map<K, V> aMap, final K aKey) {

        super();

        myKey = aKey;
        myMap = aMap;
        myValue = null;
    }

    @SuppressWarnings("unused")
    private MapEntry() {
        this(null);
    }

    public int compareTo(final KeyValue<K, ?> aReference) {
        return myKey.compareTo(aReference.getKey());
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof MapEntry)) {
            return false;
        }
        final MapEntry<?, ?> other = (MapEntry<?, ?>) obj;
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

    public K getKey() {
        return myKey;
    }

    public V getValue() {
        if (myValue != null) {
            return myValue;
        } else if (myMap != null) {
            return myMap.get(myKey);
        } else {
            return null;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((myKey == null) ? 0 : myKey.hashCode());
        result = (prime * result) + ((myValue == null) ? 0 : myValue.hashCode());
        return result;
    }

    public V setValue(final V aValue) {

        final V retVal = this.getValue();

        myValue = aValue;

        if (myMap != null) {
            myMap.put(myKey, aValue);
        }

        return retVal;
    }

    @Override
    public String toString() {
        return String.valueOf(myKey) + String.valueOf(ASCII.EQUALS) + String.valueOf(myValue);
    }

}

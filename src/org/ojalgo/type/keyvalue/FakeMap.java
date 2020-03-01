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

import java.util.AbstractMap;

import org.ojalgo.type.PrimitiveNumber;

/**
 * A variation of {@link FakeSet}.
 *
 * @author apete
 */
public final class FakeMap<K, V> extends AbstractMap<K, V> {

    public static <K> FakeMap<K, PrimitiveNumber> of(K[] keys, byte[] values) {
        return new FakeMap<>(new FakeSet.ObjectByte<>(keys, values));
    }

    public static <K> FakeMap<K, PrimitiveNumber> of(K[] keys, double[] values) {
        return new FakeMap<>(new FakeSet.ObjectDouble<>(keys, values));
    }

    public static <K> FakeMap<K, PrimitiveNumber> of(K[] keys, float[] values) {
        return new FakeMap<>(new FakeSet.ObjectFloat<>(keys, values));
    }

    public static <K> FakeMap<K, PrimitiveNumber> of(K[] keys, int[] values) {
        return new FakeMap<>(new FakeSet.ObjectInt<>(keys, values));
    }

    public static <K> FakeMap<K, PrimitiveNumber> of(K[] keys, long[] values) {
        return new FakeMap<>(new FakeSet.ObjectLong<>(keys, values));
    }

    public static <K> FakeMap<K, PrimitiveNumber> of(K[] keys, short[] values) {
        return new FakeMap<>(new FakeSet.ObjectShort<>(keys, values));
    }

    public static <K, V> FakeMap<K, V> of(K[] keys, V[] values) {
        return new FakeMap<>(new FakeSet.ObjectObject<>(keys, values));
    }

    private final FakeSet<K, V> myEntries;

    FakeMap(FakeSet<K, V> entries) {
        super();
        myEntries = entries;
    }

    @Override
    public FakeSet<K, V> entrySet() {
        return myEntries;
    }

    public EntryPair<K, V> getEntry(int index) {
        return myEntries.get(index);
    }

    public K getKey(int index) {
        return myEntries.getKey(index);
    }

    public V getValue(int index) {
        return myEntries.getValue(index);
    }

    @Override
    public int size() {
        return myEntries.size();
    }

}

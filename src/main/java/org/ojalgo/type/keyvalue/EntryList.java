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

import java.util.List;
import java.util.Map;

import org.ojalgo.type.PrimitiveNumber;

public interface EntryList<K, V> extends List<Map.Entry<K, V>>, Paired<K, V> {

    static <K> EntryList<K, PrimitiveNumber> wrap(final K[] keys, final byte[] values) {
        return new EntrySet.ObjectByte<>(keys, values);
    }

    static <K> EntryList<K, PrimitiveNumber> wrap(final K[] keys, final double[] values) {
        return new EntrySet.ObjectDouble<>(keys, values);
    }

    static <K> EntryList<K, PrimitiveNumber> wrap(final K[] keys, final float[] values) {
        return new EntrySet.ObjectFloat<>(keys, values);
    }

    static <K> EntryList<K, PrimitiveNumber> wrap(final K[] keys, final int[] values) {
        return new EntrySet.ObjectInt<>(keys, values);
    }

    static <K> EntryList<K, PrimitiveNumber> wrap(final K[] keys, final long[] values) {
        return new EntrySet.ObjectLong<>(keys, values);
    }

    static <K> EntryList<K, PrimitiveNumber> wrap(final K[] keys, final short[] values) {
        return new EntrySet.ObjectShort<>(keys, values);
    }

    static <K, V> EntryList<K, V> wrap(final K[] keys, final V[] values) {
        return new EntrySet.ObjectObject<>(keys, values);
    }

}

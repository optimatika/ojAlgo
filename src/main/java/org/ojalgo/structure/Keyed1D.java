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
package org.ojalgo.structure;

public final class Keyed1D<K, N extends Comparable<N>> implements Structure1D {

    private final IndexMapper<K> myMapper;
    private final Access1D<N> myStructure;

    Keyed1D(final Access1D<N> structure, final IndexMapper<K> indexMapper) {
        super();
        myStructure = structure;
        myMapper = indexMapper;
    }

    public byte byteValue(final K key) {
        return myStructure.byteValue(myMapper.toIndex(key));
    }

    public long count() {
        return myStructure.count();
    }

    public double doubleValue(final K key) {
        return myStructure.doubleValue(myMapper.toIndex(key));
    }

    public float floatValue(final K key) {
        return myStructure.floatValue(myMapper.toIndex(key));
    }

    public N get(final K key) {
        return myStructure.get(myMapper.toIndex(key));
    }

    public int intValue(final K key) {
        return myStructure.intValue(myMapper.toIndex(key));
    }

    public long longValue(final K key) {
        return myStructure.longValue(myMapper.toIndex(key));
    }

    public void set(final K key, final byte value) {
        if (myStructure instanceof Mutate1D) {
            ((Mutate1D) myStructure).set(myMapper.toIndex(key), value);
        } else {
            throw new IllegalStateException();
        }
    }

    public void set(final K key, final Comparable<?> value) {
        if (myStructure instanceof Mutate1D) {
            ((Mutate1D) myStructure).set(myMapper.toIndex(key), value);
        } else {
            throw new IllegalStateException();
        }
    }

    public void set(final K key, final double value) {
        if (myStructure instanceof Mutate1D) {
            ((Mutate1D) myStructure).set(myMapper.toIndex(key), value);
        } else {
            throw new IllegalStateException();
        }
    }

    public void set(final K key, final float value) {
        if (myStructure instanceof Mutate1D) {
            ((Mutate1D) myStructure).set(myMapper.toIndex(key), value);
        } else {
            throw new IllegalStateException();
        }
    }

    public void set(final K key, final int value) {
        if (myStructure instanceof Mutate1D) {
            ((Mutate1D) myStructure).set(myMapper.toIndex(key), value);
        } else {
            throw new IllegalStateException();
        }
    }

    public void set(final K key, final long value) {
        if (myStructure instanceof Mutate1D) {
            ((Mutate1D) myStructure).set(myMapper.toIndex(key), value);
        } else {
            throw new IllegalStateException();
        }
    }

    public void set(final K key, final short value) {
        if (myStructure instanceof Mutate1D) {
            ((Mutate1D) myStructure).set(myMapper.toIndex(key), value);
        } else {
            throw new IllegalStateException();
        }
    }

    public short shortValue(final K key) {
        return myStructure.shortValue(myMapper.toIndex(key));
    }

}

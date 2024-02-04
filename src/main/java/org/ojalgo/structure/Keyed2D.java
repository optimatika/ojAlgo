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

import org.ojalgo.structure.Structure2D.RowColumnMapper;

public final class Keyed2D<R, C, N extends Comparable<N>> implements Structure1D {

    private final RowColumnMapper<R, C> myMapper;
    private final Access2D<N> myStructure;

    Keyed2D(final Access2D<N> structure, final RowColumnMapper<R, C> indexMapper) {
        super();
        myStructure = structure;
        myMapper = indexMapper;
    }

    public byte byteValue(final R row, final C col) {
        return myStructure.byteValue(myMapper.toRowIndex(row), myMapper.toColumnIndex(col));
    }

    public long count() {
        return myStructure.count();
    }

    public double doubleValue(final R row, final C col) {
        return myStructure.doubleValue(myMapper.toRowIndex(row), myMapper.toColumnIndex(col));
    }

    public float floatValue(final R row, final C col) {
        return myStructure.floatValue(myMapper.toRowIndex(row), myMapper.toColumnIndex(col));
    }

    public N get(final R row, final C col) {
        return myStructure.get(myMapper.toRowIndex(row), myMapper.toColumnIndex(col));
    }

    public int intValue(final R row, final C col) {
        return myStructure.intValue(myMapper.toRowIndex(row), myMapper.toColumnIndex(col));
    }

    public long longValue(final R row, final C col) {
        return myStructure.longValue(myMapper.toIndex(row, col));
    }

    public void set(final R row, final C col, final byte value) {
        if (myStructure instanceof Mutate1D) {
            ((Mutate1D) myStructure).set(myMapper.toIndex(row, col), value);
        } else {
            throw new IllegalStateException();
        }
    }

    public void set(final R row, final C col, final Comparable<?> value) {
        if (myStructure instanceof Mutate1D) {
            ((Mutate1D) myStructure).set(myMapper.toIndex(row, col), value);
        } else {
            throw new IllegalStateException();
        }
    }

    public void set(final R row, final C col, final double value) {
        if (myStructure instanceof Mutate1D) {
            ((Mutate1D) myStructure).set(myMapper.toIndex(row, col), value);
        } else {
            throw new IllegalStateException();
        }
    }

    public void set(final R row, final C col, final float value) {
        if (myStructure instanceof Mutate1D) {
            ((Mutate1D) myStructure).set(myMapper.toIndex(row, col), value);
        } else {
            throw new IllegalStateException();
        }
    }

    public void set(final R row, final C col, final int value) {
        if (myStructure instanceof Mutate1D) {
            ((Mutate1D) myStructure).set(myMapper.toIndex(row, col), value);
        } else {
            throw new IllegalStateException();
        }
    }

    public void set(final R row, final C col, final long value) {
        if (myStructure instanceof Mutate1D) {
            ((Mutate1D) myStructure).set(myMapper.toIndex(row, col), value);
        } else {
            throw new IllegalStateException();
        }
    }

    public void set(final R row, final C col, final short value) {
        if (myStructure instanceof Mutate1D) {
            ((Mutate1D) myStructure).set(myMapper.toIndex(row, col), value);
        } else {
            throw new IllegalStateException();
        }
    }

    public short shortValue(final R row, final C col) {
        return myStructure.shortValue(myMapper.toRowIndex(row), myMapper.toColumnIndex(col));
    }

}

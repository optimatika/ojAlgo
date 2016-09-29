/*
 * Copyright 1997-2016 Optimatika (www.optimatika.se)
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
package org.ojalgo.collection;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.ojalgo.access.Access1D;
import org.ojalgo.access.Mutate1D;
import org.ojalgo.array.Array1D;

public final class PrimitiveDoubleList implements List<Double>, Access1D<Double>, Mutate1D {

    private long myActualCount = 0L;
    private final Array1D<Double> myStorage;

    public PrimitiveDoubleList() {

        super();

        myStorage = Array1D.PRIMITIVE.makeZero(999);
    }

    PrimitiveDoubleList(final Array1D<Double> storage) {

        super();

        myStorage = storage;
        myActualCount = storage.count();
    }

    public boolean add(final double e) {
        myActualCount++;
        return false;
    }

    public boolean add(final Double e) {
        return this.add(e.doubleValue());
    }

    public void add(final int index, final Double element) {
        throw new UnsupportedOperationException();
    }

    public void add(final long index, final double addend) {
        if (index >= myActualCount) {
            throw new ArrayIndexOutOfBoundsException();
        } else {
            myStorage.add(index, addend);
        }
    }

    public void add(final long index, final Number addend) {
        if (index >= myActualCount) {
            throw new ArrayIndexOutOfBoundsException();
        } else {
            myStorage.add(index, addend);
        }
    }

    public boolean addAll(final Collection<? extends Double> c) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean addAll(final int index, final Collection<? extends Double> c) {
        // TODO Auto-generated method stub
        return false;
    }

    public void clear() {
        myActualCount = 0L;
    }

    public boolean contains(final Object o) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean containsAll(final Collection<?> c) {
        // TODO Auto-generated method stub
        return false;
    }

    public long count() {
        return myActualCount;
    }

    public double doubleValue(final long index) {
        if (index >= myActualCount) {
            throw new ArrayIndexOutOfBoundsException();
        } else {
            return myStorage.doubleValue(index);
        }
    }

    public Double get(final int index) {
        return this.doubleValue(index);
    }

    public Double get(final long index) {
        if (index >= myActualCount) {
            throw new ArrayIndexOutOfBoundsException();
        } else {
            return myStorage.get(index);
        }
    }

    public int indexOf(final Object o) {
        // TODO Auto-generated method stub
        return 0;
    }

    public boolean isEmpty() {
        return myActualCount == 0L;
    }

    public Iterator<Double> iterator() {
        return Access1D.super.iterator();
    }

    public int lastIndexOf(final Object o) {
        // TODO Auto-generated method stub
        return 0;
    }

    public ListIterator<Double> listIterator() {
        // TODO Auto-generated method stub
        return null;
    }

    public ListIterator<Double> listIterator(final int index) {
        // TODO Auto-generated method stub
        return null;
    }

    public Double remove(final int index) {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean remove(final Object o) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean removeAll(final Collection<?> c) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean retainAll(final Collection<?> c) {
        // TODO Auto-generated method stub
        return false;
    }

    public Double set(final int index, final Double element) {
        throw new UnsupportedOperationException();
    }

    public void set(final long index, final double value) {
        if (index >= myActualCount) {
            throw new ArrayIndexOutOfBoundsException();
        } else {
            myStorage.set(index, value);
        }
    }

    public void set(final long index, final Number value) {
        if (index >= myActualCount) {
            throw new ArrayIndexOutOfBoundsException();
        } else {
            myStorage.set(index, value);
        }
    }

    public int size() {
        return (int) myActualCount;
    }

    public List<Double> subList(final int fromIndex, final int toIndex) {
        // TODO Auto-generated method stub
        return null;
    }

    public Object[] toArray() {
        // TODO Auto-generated method stub
        return null;
    }

    public <T> T[] toArray(final T[] a) {
        // TODO Auto-generated method stub
        return null;
    }

}

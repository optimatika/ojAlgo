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
import org.ojalgo.array.ArrayFactory;
import org.ojalgo.array.BasicArray;
import org.ojalgo.array.ComplexArray;
import org.ojalgo.array.PrimitiveArray;
import org.ojalgo.array.SegmentedArray;
import org.ojalgo.scalar.ComplexNumber;

public final class BasicList<N extends Number> implements List<N>, Access1D<N>, Mutate1D {

    private static long INITIAL_CAPACITY = 16L;

    private static int SEGMENT_BITS = 14;

    private static long SEGMENT_CAPACITY = 16_384L;

    public static BasicList<ComplexNumber> makeComplexe() {
        return new BasicList<ComplexNumber>(ComplexArray.FACTORY);
    }

    public static BasicList<Double> makePrimitive() {
        return new BasicList<Double>(PrimitiveArray.FACTORY);
    }

    private long myActualCount;
    private final ArrayFactory<N> myArrayFactory;
    private BasicArray<N> myStorage;

    BasicList(ArrayFactory<N> arrayFactory) {

        super();

        myArrayFactory = arrayFactory;

        myStorage = arrayFactory.makeZero(INITIAL_CAPACITY);
        myActualCount = 0L;
    }

    public boolean add(final double e) {

        this.ensureCapacity();

        myStorage.set(myActualCount++, e);

        return true;
    }

    private void ensureCapacity() {

        if (myStorage.count() > myActualCount) {
            // It fits, just add to the end

        } else if ((myStorage.count() % SEGMENT_CAPACITY) == 0L) {
            // Doesn't fit, grow by 1 segment, then add

            if (myStorage instanceof SegmentedArray) {
                myStorage = ((SegmentedArray<N>) myStorage).grow();
            } else if (myStorage.count() == SEGMENT_CAPACITY) {
                myStorage = myArrayFactory.wrapAsSegments(myStorage, myArrayFactory.makeZero(SEGMENT_CAPACITY));
            } else {
                throw new IllegalStateException();
            }

        } else {
            // Doesn't fit, grow by doubling the capacity, then add

            BasicArray<N> tmpStorage = myArrayFactory.makeZero(myStorage.count() * 2L);
            tmpStorage.fillMatching(myStorage);
            myStorage = tmpStorage;
        }
    }

    public void add(int index, N element) {
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

    public boolean add(N e) {

        this.ensureCapacity();

        myStorage.set(myActualCount++, e);

        return true;
    }

    public boolean addAll(Collection<? extends N> c) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean addAll(int index, Collection<? extends N> c) {
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

    public N get(final int index) {
        if (index >= myActualCount) {
            throw new ArrayIndexOutOfBoundsException();
        } else {
            return myStorage.get(index);
        }
    }

    public N get(final long index) {
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

    public Iterator<N> iterator() {
        return Access1D.super.iterator();
    }

    public int lastIndexOf(final Object o) {
        // TODO Auto-generated method stub
        return 0;
    }

    public ListIterator<N> listIterator() {
        // TODO Auto-generated method stub
        return null;
    }

    public ListIterator<N> listIterator(final int index) {
        // TODO Auto-generated method stub
        return null;
    }

    public N remove(final int index) {
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

    public N set(int index, N element) {
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

    public List<N> subList(final int fromIndex, final int toIndex) {
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

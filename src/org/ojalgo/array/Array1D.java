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
package org.ojalgo.array;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.AbstractList;
import java.util.List;
import java.util.RandomAccess;
import java.util.Spliterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

import org.ojalgo.access.Access1D;
import org.ojalgo.access.Factory1D;
import org.ojalgo.access.Mutate1D;
import org.ojalgo.array.BasicArray.BasicFactory;
import org.ojalgo.array.DenseArray.DenseFactory;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.scalar.Scalar;

/**
 * Array1D
 *
 * @author apete
 */
public final class Array1D<N extends Number> extends AbstractList<N> implements Access1D<N>, Access1D.Elements, Access1D.IndexOf, Mutate1D,
        Mutate1D.Fillable<N>, Mutate1D.Modifiable<N>, Mutate1D.BiModifiable<N>, Access1D.Visitable<N>, Access1D.Sliceable<N>, RandomAccess, Serializable {

    public static abstract class Factory<N extends Number> implements Factory1D<Array1D<N>> {

        public final Array1D<N> copy(final Access1D<?> source) {
            return this.delegate().copy(source).asArray1D();
        }

        public final Array1D<N> copy(final double... source) {
            return this.delegate().copy(source).asArray1D();
        }

        public final Array1D<N> copy(final List<? extends Number> source) {
            return this.delegate().copy(source).asArray1D();
        }

        public final Array1D<N> copy(final Number... source) {
            return this.delegate().copy(source).asArray1D();
        }

        public final Array1D<N> makeFilled(final long count, final NullaryFunction<?> supplier) {
            return this.delegate().makeFilled(count, supplier).asArray1D();
        }

        public final Array1D<N> makeZero(final long count) {
            return this.delegate().makeZero(count).asArray1D();
        }

        public final Array1D<N> wrap(final BasicArray<N> array) {
            return array.asArray1D();
        }

        abstract BasicArray.BasicFactory<N> delegate();

    }

    static final class QuickAscendingSorter extends RecursiveAction {

        private final long high;
        private final long low;
        private final Array1D<?> myArray;

        private QuickAscendingSorter(final Array1D<?> array, final long low, final long high) {
            super();
            myArray = array;
            this.low = low;
            this.high = high;
        }

        QuickAscendingSorter(final Array1D<?> array) {
            this(array, 0L, array.count() - 1L);
        }

        @Override
        protected void compute() {

            long i = low, j = high;

            final double pivot = myArray.doubleValue(low + ((high - low) / 2));

            while (i <= j) {

                while (myArray.doubleValue(i) < pivot) {
                    i++;
                }
                while (myArray.doubleValue(j) > pivot) {
                    j--;
                }

                if (i <= j) {
                    myArray.exchange(i, j);
                    i++;
                    j--;
                }
            }

            QuickAscendingSorter tmpPartL = null;
            QuickAscendingSorter tmpPartH = null;

            if (low < j) {
                tmpPartL = new QuickAscendingSorter(myArray, low, j);
                tmpPartL.fork();
            }
            if (i < high) {
                tmpPartH = new QuickAscendingSorter(myArray, i, high);
                tmpPartH.fork();
            }
            if (tmpPartL != null) {
                tmpPartL.join();
            }
            if (tmpPartH != null) {
                tmpPartH.join();
            }
        }

    }

    static final class QuickDescendingSorter extends RecursiveAction {

        private final long high;
        private final long low;
        private final Array1D<?> myArray;

        private QuickDescendingSorter(final Array1D<?> array, final long low, final long high) {
            super();
            myArray = array;
            this.low = low;
            this.high = high;
        }

        QuickDescendingSorter(final Array1D<?> array) {
            this(array, 0L, array.count() - 1L);
        }

        @Override
        protected void compute() {

            long i = low, j = high;

            final double pivot = myArray.doubleValue(low + ((high - low) / 2));

            while (i <= j) {

                while (myArray.doubleValue(i) > pivot) {
                    i++;
                }
                while (myArray.doubleValue(j) < pivot) {
                    j--;
                }

                if (i <= j) {
                    myArray.exchange(i, j);
                    i++;
                    j--;
                }
            }

            QuickDescendingSorter tmpPartL = null;
            QuickDescendingSorter tmpPartH = null;

            if (low < j) {
                tmpPartL = new QuickDescendingSorter(myArray, low, j);
                tmpPartL.fork();
            }
            if (i < high) {
                tmpPartH = new QuickDescendingSorter(myArray, i, high);
                tmpPartH.fork();
            }
            if (tmpPartL != null) {
                tmpPartL.join();
            }
            if (tmpPartH != null) {
                tmpPartH.join();
            }
        }

    }

    public static final Factory<BigDecimal> BIG = new Factory<BigDecimal>() {

        @Override
        BasicArray.BasicFactory<BigDecimal> delegate() {
            return BasicArray.BIG;
        }

    };

    public static final Factory<ComplexNumber> COMPLEX = new Factory<ComplexNumber>() {

        @Override
        BasicArray.BasicFactory<ComplexNumber> delegate() {
            return BasicArray.COMPLEX;
        }

    };

    public static final Factory<Double> PRIMITIVE = new Factory<Double>() {

        @Override
        BasicArray.BasicFactory<Double> delegate() {
            return BasicArray.PRIMITIVE;
        }

    };

    public static final Factory<Quaternion> QUATERNION = new Factory<Quaternion>() {

        @Override
        BasicArray.BasicFactory<Quaternion> delegate() {
            return BasicArray.QUATERNION;
        }

    };

    public static final Factory<RationalNumber> RATIONAL = new Factory<RationalNumber>() {

        @Override
        BasicArray.BasicFactory<RationalNumber> delegate() {
            return BasicArray.RATIONAL;
        }

    };

    public static <N extends Number> Array1D.Factory<N> factory(final DenseFactory<N> delegate) {

        final BasicFactory<N> tmpDelegate = BasicArray.factory(delegate);

        return new Array1D.Factory<N>() {

            @Override
            BasicFactory<N> delegate() {
                return tmpDelegate;
            }

        };
    }

    public final long length;

    private final BasicArray<N> myDelegate;
    private final long myFirst;
    private final long myLimit;
    private final long myStep;

    @SuppressWarnings("unused")
    private Array1D() {
        this(null);
    }

    Array1D(final BasicArray<N> delegate) {
        this(delegate, 0L, delegate.count(), 1L);
    }

    Array1D(final BasicArray<N> delegate, final long first, final long limit, final long step) {

        super();

        myDelegate = delegate;

        myFirst = first;
        myLimit = limit;
        myStep = step;

        length = (myLimit - myFirst) / myStep;
    }

    public void add(final long index, final double addend) {
        final long tmpIndex = myFirst + (myStep * index);
        myDelegate.add(tmpIndex, addend);
    }

    public void add(final long index, final Number addend) {
        final long tmpIndex = myFirst + (myStep * index);
        myDelegate.add(tmpIndex, addend);
    }

    @Override
    public void clear() {
        myDelegate.reset();
    }

    @Override
    public boolean contains(final Object obj) {
        return this.indexOf(obj) != -1;
    }

    @SuppressWarnings("unchecked")
    public Array1D<N> copy() {

        BasicArray<N> retVal = null;

        if (myDelegate instanceof Primitive64Array) {

            retVal = (BasicArray<N>) new Primitive64Array((int) length);

            for (long i = 0L; i < length; i++) {
                retVal.set(i, this.doubleValue(i));
            }

            return new Array1D<>(retVal);

        } else if (myDelegate instanceof ComplexArray) {

            retVal = (BasicArray<N>) new ComplexArray((int) length);

            for (long i = 0L; i < length; i++) {
                retVal.set(i, this.get(i));
            }

            return new Array1D<>(retVal);

        } else if (myDelegate instanceof BigArray) {

            retVal = (BasicArray<N>) new BigArray((int) length);

            for (long i = 0L; i < length; i++) {
                retVal.set(i, this.get(i));
            }

            return new Array1D<>(retVal);

        } else {

            return null;
        }
    }

    /**
     * Creates a copy of this containing only the selected elements, in the specified order.
     */
    @SuppressWarnings("unchecked")
    public Array1D<N> copy(final int... indices) {

        BasicArray<N> retVal = null;

        final int tmpLength = indices.length;

        if (myDelegate instanceof Primitive64Array) {

            retVal = (BasicArray<N>) new Primitive64Array(tmpLength);

            for (int i = 0; i < tmpLength; i++) {
                retVal.set(i, this.doubleValue(indices[i]));
            }

            return new Array1D<>(retVal);

        } else if (myDelegate instanceof ComplexArray) {

            retVal = (BasicArray<N>) new ComplexArray(tmpLength);

            for (int i = 0; i < tmpLength; i++) {
                retVal.set(i, this.get(indices[i]));
            }

            return new Array1D<>(retVal);

        } else if (myDelegate instanceof BigArray) {

            retVal = (BasicArray<N>) new BigArray(tmpLength);

            for (int i = 0; i < tmpLength; i++) {
                retVal.set(i, this.get(indices[i]));
            }

            return new Array1D<>(retVal);

        } else {

            return null;
        }
    }

    public long count() {
        return length;
    }

    public double doubleValue(final long index) {
        return myDelegate.doubleValue(myFirst + (myStep * index));
    }

    public void fillAll(final N value) {
        myDelegate.fill(myFirst, myLimit, myStep, value);
    }

    public void fillAll(final NullaryFunction<N> supplier) {
        myDelegate.fill(myFirst, myLimit, myStep, supplier);
    }

    public void fillOne(final long index, final Access1D<?> values, final long valueIndex) {
        myDelegate.fillOne(myFirst + (myStep * index), values, valueIndex);
    }

    public void fillOne(final long index, final N value) {
        final long tmpIndex = myFirst + (myStep * index);
        myDelegate.fillOne(tmpIndex, value);
    }

    public void fillOne(final long index, final NullaryFunction<N> supplier) {
        final long tmpIndex = myFirst + (myStep * index);
        myDelegate.fillOne(tmpIndex, supplier);
    }

    public void fillRange(final long first, final long limit, final N value) {
        final long tmpFirst = myFirst + (myStep * first);
        final long tmpLimit = myFirst + (myStep * limit);
        myDelegate.fill(tmpFirst, tmpLimit, myStep, value);
    }

    public void fillRange(final long first, final long limit, final NullaryFunction<N> supplier) {
        final long tmpFirst = myFirst + (myStep * first);
        final long tmpLimit = myFirst + (myStep * limit);
        myDelegate.fill(tmpFirst, tmpLimit, myStep, supplier);
    }

    @Override
    public N get(final int index) {
        return myDelegate.get(myFirst + (myStep * index));
    }

    public N get(final long index) {
        return myDelegate.get(myFirst + (myStep * index));
    }

    @Override
    public int indexOf(final Object obj) {
        final int tmpLength = (int) length;
        if (obj == null) {
            for (int i = 0; i < tmpLength; i++) {
                if (this.get(i) == null) {
                    return i;
                }
            }
        } else if (obj instanceof Number) {
            for (int i = 0; i < tmpLength; i++) {
                if (obj.equals(this.get(i))) {
                    return i;
                }
            }
        }
        return -1;
    }

    public long indexOfLargest() {
        return this.indexOfLargestInRange(myFirst, myLimit);
    }

    public long indexOfLargestInRange(final long first, final long limit) {
        return (myDelegate.indexOfLargest(myFirst + (myStep * first), myFirst + (myStep * limit), myStep) - myFirst) / myStep;
    }

    /**
     * @see Scalar#isAbsolute()
     */
    public boolean isAbsolute(final long index) {
        return myDelegate.isAbsolute(myFirst + (myStep * index));
    }

    public boolean isAllSmall(final double comparedTo) {
        return myDelegate.isSmall(myFirst, myLimit, myStep, comparedTo);
    }

    @Override
    public boolean isEmpty() {
        return length == 0;
    }

    /**
     * @see Scalar#isSmall(double)
     */
    public boolean isSmall(final long index, final double comparedTo) {
        return myDelegate.isSmall(myFirst + (myStep * index), comparedTo);
    }

    public void modifyAll(final UnaryFunction<N> modifier) {
        myDelegate.modify(myFirst, myLimit, myStep, modifier);
    }

    public void modifyMatching(final Access1D<N> left, final BinaryFunction<N> function) {
        final long tmpLength = Math.min(length, left.count());
        if (myDelegate instanceof Primitive64Array) {
            for (long i = 0L; i < tmpLength; i++) {
                this.set(i, function.invoke(left.doubleValue(i), this.doubleValue(i)));
            }
        } else {
            for (long i = 0L; i < tmpLength; i++) {
                this.set(i, function.invoke(left.get(i), this.get(i)));
            }
        }
    }

    public void modifyMatching(final BinaryFunction<N> function, final Access1D<N> right) {
        final long tmpLength = Math.min(length, right.count());
        if (myDelegate instanceof Primitive64Array) {
            for (long i = 0L; i < tmpLength; i++) {
                this.set(i, function.invoke(this.doubleValue(i), right.doubleValue(i)));
            }
        } else {
            for (long i = 0L; i < tmpLength; i++) {
                this.set(i, function.invoke(this.get(i), right.get(i)));
            }
        }
    }

    public void modifyOne(final long index, final UnaryFunction<N> modifier) {
        myDelegate.modifyOne(myFirst + (myStep * index), modifier);
    }

    public void modifyRange(final long first, final long limit, final UnaryFunction<N> modifier) {
        final long tmpFirst = myFirst + (myStep * first);
        final long tmpLimit = myFirst + (myStep * limit);
        myDelegate.modify(tmpFirst, tmpLimit, myStep, modifier);
    }

    @Override
    public N set(final int index, final Number value) {
        final long tmpIndex = myFirst + (myStep * index);
        final N retVal = myDelegate.get(tmpIndex);
        myDelegate.set(tmpIndex, value);
        return retVal;
    }

    public void set(final long index, final double value) {
        myDelegate.set(myFirst + (myStep * index), value);
    }

    public void set(final long index, final Number value) {
        myDelegate.set(myFirst + (myStep * index), value);
    }

    @Override
    public int size() {
        return (int) length;
    }

    public Array1D<N> sliceRange(final long first, final long limit) {
        return new Array1D<>(myDelegate, myFirst + (myStep * first), myFirst + (myStep * limit), myStep);
    }

    public void sortAscending() {

        if ((myDelegate instanceof PlainArray<?>) && (this.count() == myDelegate.count())) {

            ((PlainArray<N>) myDelegate).sortAscending();

        } else {

            //this.sortAscending(0L, this.count() - 1L);

            try {
                ForkJoinPool.commonPool().submit(new QuickAscendingSorter(this)).get();
            } catch (InterruptedException | ExecutionException exception) {
                exception.printStackTrace();
            }
        }
    }

    public void sortDescending() {

        if ((myDelegate instanceof PlainArray<?>) && (this.count() == myDelegate.count())) {

            ((PlainArray<N>) myDelegate).sortDescending();

        } else {

            //this.sortDescending(0L, this.count() - 1L);

            try {
                ForkJoinPool.commonPool().submit(new QuickDescendingSorter(this)).get();
            } catch (InterruptedException | ExecutionException exception) {
                exception.printStackTrace();
            }
        }
    }

    public Spliterator<N> spliterator() {
        return myDelegate.spliterator();
    }

    @Override
    public Array1D<N> subList(final int first, final int limit) {
        return this.sliceRange(first, limit);
    }

    public void visitAll(final VoidFunction<N> visitor) {
        myDelegate.visit(myFirst, myLimit, myStep, visitor);
    }

    public void visitOne(final long index, final VoidFunction<N> visitor) {
        myDelegate.visitOne(myFirst + (myStep * index), visitor);
    }

    public void visitRange(final long first, final long limit, final VoidFunction<N> visitor) {
        final long tmpFirst = myFirst + (myStep * first);
        final long tmpLimit = myFirst + (myStep * limit);
        myDelegate.visit(tmpFirst, tmpLimit, myStep, visitor);
    }

    final void exchange(final long indexA, final long indexB) {

        if (myDelegate.isPrimitive()) {

            final double tmpVal = this.doubleValue(indexA);
            this.set(indexA, this.doubleValue(indexB));
            this.set(indexB, tmpVal);

        } else {

            final N tmpVal = this.get(indexA);
            this.set(indexA, this.get(indexB));
            this.set(indexB, tmpVal);
        }
    }

    BasicArray<N> getDelegate() {
        return myDelegate;
    }

    void sortAscending(final long low, final long high) {

        long i = low, j = high;

        final double pivot = this.doubleValue(low + ((high - low) / 2));

        while (i <= j) {

            while (this.doubleValue(i) < pivot) {
                i++;
            }
            while (this.doubleValue(j) > pivot) {
                j--;
            }

            if (i <= j) {
                this.exchange(i, j);
                i++;
                j--;
            }
        }

        if (low < j) {
            this.sortAscending(low, j);
        }
        if (i < high) {
            this.sortAscending(i, high);
        }
    }

    void sortDescending(final long low, final long high) {

        long i = low, j = high;

        final double pivot = this.doubleValue(low + ((high - low) / 2));

        while (i <= j) {

            while (this.doubleValue(i) > pivot) {
                i++;
            }
            while (this.doubleValue(j) < pivot) {
                j--;
            }

            if (i <= j) {
                this.exchange(i, j);
                i++;
                j--;
            }
        }

        if (low < j) {
            this.sortDescending(low, j);
        }
        if (i < high) {
            this.sortDescending(i, high);
        }
    }

}

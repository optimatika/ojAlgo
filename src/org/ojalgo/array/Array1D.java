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

import org.ojalgo.ProgrammingError;
import org.ojalgo.access.Access1D;
import org.ojalgo.access.Factory1D;
import org.ojalgo.access.Mutate1D;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.FunctionSet;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.scalar.Scalar;

/**
 * Array1D
 *
 * @author apete
 */
public final class Array1D<N extends Number> extends AbstractList<N>
        implements Access1D<N>, Access1D.Elements, Access1D.IndexOf, Access1D.Sliceable<N>, Access1D.Visitable<N>, Access1D.Aggregatable<N>,
        Mutate1D.Receiver<N>, Mutate1D.BiModifiable<N>, Mutate1D.Modifiable<N>, Mutate1D.Mixable<N>, Mutate1D.Sortable, RandomAccess, Serializable {

    public static final class Factory<N extends Number> implements Factory1D<Array1D<N>> {

        private final BasicArray.Factory<N> myDelegate;

        Factory(final DenseArray.Factory<N> denseArray) {
            super();
            myDelegate = BasicArray.factory(denseArray);
        }

        public final Array1D<N> copy(final Access1D<?> source) {
            return myDelegate.copy(source).wrapInArray1D();
        }

        public final Array1D<N> copy(final double... source) {
            return myDelegate.copy(source).wrapInArray1D();
        }

        public final Array1D<N> copy(final List<? extends Number> source) {
            return myDelegate.copy(source).wrapInArray1D();
        }

        public final Array1D<N> copy(final Number... source) {
            return myDelegate.copy(source).wrapInArray1D();
        }

        @Override
        public final FunctionSet<N> function() {
            return myDelegate.function();
        }

        public final Array1D<N> makeFilled(final long count, final NullaryFunction<?> supplier) {
            return myDelegate.makeFilled(count, supplier).wrapInArray1D();
        }

        public final Array1D<N> makeZero(final long count) {
            return myDelegate.makeZero(count).wrapInArray1D();
        }

        @Override
        public final Scalar.Factory<N> scalar() {
            return myDelegate.scalar();
        }

        public final Array1D<N> wrap(final BasicArray<N> array) {
            return array.wrapInArray1D();
        }

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

    public static final Factory<BigDecimal> BIG = new Factory<>(BigArray.FACTORY);
    public static final Factory<ComplexNumber> COMPLEX = new Factory<>(ComplexArray.FACTORY);
    public static final Factory<Double> DIRECT32 = new Factory<>(BufferArray.DIRECT32);
    public static final Factory<Double> DIRECT64 = new Factory<>(BufferArray.DIRECT64);
    public static final Factory<Double> PRIMITIVE32 = new Factory<>(Primitive32Array.FACTORY);
    public static final Factory<Double> PRIMITIVE64 = new Factory<>(Primitive64Array.FACTORY);
    public static final Factory<Quaternion> QUATERNION = new Factory<>(QuaternionArray.FACTORY);
    public static final Factory<RationalNumber> RATIONAL = new Factory<>(RationalArray.FACTORY);

    public static <N extends Number> Array1D.Factory<N> factory(final DenseArray.Factory<N> denseFactory) {
        return new Array1D.Factory<>(denseFactory);
    }

    public final long length;

    private final BasicArray<N> myDelegate;
    private final long myFirst;
    private final long myLimit;
    private final long myStep;

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

    public N aggregateRange(long first, long limit, Aggregator aggregator) {
        AggregatorFunction<N> visitor = aggregator.getFunction(myDelegate.factory().aggregator());
        this.visitRange(first, limit, visitor);
        return visitor.get();
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

        if (myDelegate.isPrimitive()) {

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

        if (myDelegate.isPrimitive()) {

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

    public double mix(final long index, final BinaryFunction<N> mixer, final double addend) {
        ProgrammingError.throwIfNull(mixer);
        synchronized (myDelegate) {
            final double oldValue = this.doubleValue(index);
            final double newValue = mixer.invoke(oldValue, addend);
            this.set(index, newValue);
            return newValue;
        }
    }

    public N mix(final long index, final BinaryFunction<N> mixer, final N addend) {
        ProgrammingError.throwIfNull(mixer);
        synchronized (myDelegate) {
            final N oldValue = this.get(index);
            final N newValue = mixer.invoke(oldValue, addend);
            this.set(index, newValue);
            return newValue;
        }
    }

    public void modifyAll(final UnaryFunction<N> modifier) {
        myDelegate.modify(myFirst, myLimit, myStep, modifier);
    }

    public void modifyMatching(final Access1D<N> left, final BinaryFunction<N> function) {
        final long tmpLength = Math.min(length, left.count());
        if (myDelegate.isPrimitive()) {
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
        if (myDelegate.isPrimitive()) {
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

        if ((myDelegate instanceof Mutate1D.Sortable) && (this.count() == myDelegate.count())) {

            ((Mutate1D.Sortable) myDelegate).sortAscending();

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

        if ((myDelegate instanceof Mutate1D.Sortable) && (this.count() == myDelegate.count())) {

            ((Mutate1D.Sortable) myDelegate).sortDescending();

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

    void exchange(final long indexA, final long indexB) {

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

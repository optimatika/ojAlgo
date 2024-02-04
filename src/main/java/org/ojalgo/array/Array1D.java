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
package org.ojalgo.array;

import java.math.BigDecimal;
import java.util.AbstractList;
import java.util.List;
import java.util.RandomAccess;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

import org.ojalgo.ProgrammingError;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.FunctionSet;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Quadruple;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Factory1D;
import org.ojalgo.structure.Mutate1D;
import org.ojalgo.structure.Transformation1D;
import org.ojalgo.tensor.TensorFactory1D;
import org.ojalgo.type.math.MathType;

/**
 * Array1D
 *
 * @author apete
 */
public final class Array1D<N extends Comparable<N>> extends AbstractList<N> implements Access1D.Visitable<N>, Access1D.Aggregatable<N>, Access1D.Sliceable<N>,
        Access1D.Collectable<N, Mutate1D>, Mutate1D.ModifiableReceiver<N>, Mutate1D.Mixable<N>, Mutate1D.Sortable, RandomAccess {

    public static final class Factory<N extends Comparable<N>>
            implements Factory1D.Dense<Array1D<N>>, Factory1D.MayBeSparse<Array1D<N>, Array1D<N>, Array1D<N>> {

        private final BasicArray.Factory<N> myDelegate;

        Factory(final DenseArray.Factory<N> denseArray) {
            super();
            myDelegate = new BasicArray.Factory<>(denseArray);
        }

        @Override
        public Array1D<N> copy(final Access1D<?> source) {
            return myDelegate.copy(source).wrapInArray1D();
        }

        @Override
        public Array1D<N> copy(final Comparable<?>[] source) {
            return myDelegate.copy(source).wrapInArray1D();
        }

        @Override
        public Array1D<N> copy(final double... source) {
            return myDelegate.copy(source).wrapInArray1D();
        }

        @Override
        public Array1D<N> copy(final List<? extends Comparable<?>> source) {
            return myDelegate.copy(source).wrapInArray1D();
        }

        @Override
        public FunctionSet<N> function() {
            return myDelegate.function();
        }

        @Override
        public MathType getMathType() {
            return myDelegate.getMathType();
        }

        @Override
        public Array1D<N> make(final long count) {
            return this.makeDense(count);
        }

        @Override
        public Array1D<N> makeDense(final long count) {
            return myDelegate.makeToBeFilled(count).wrapInArray1D();
        }

        @Override
        public Array1D<N> makeFilled(final long count, final NullaryFunction<?> supplier) {
            return myDelegate.makeFilled(count, supplier).wrapInArray1D();
        }

        @Override
        public Array1D<N> makeSparse(final long count) {
            return myDelegate.makeStructuredZero(count).wrapInArray1D();
        }

        @Override
        public Scalar.Factory<N> scalar() {
            return myDelegate.scalar();
        }

        public TensorFactory1D<N, Array1D<N>> tensor() {
            return TensorFactory1D.of(this);
        }

        public Array1D<N> wrap(final BasicArray<N> array) {
            return array.wrapInArray1D();
        }

    }

    static final class QuickAscendingSorter extends RecursiveAction {

        private static final long serialVersionUID = 1L;

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

            double pivot = myArray.doubleValue(low + (high - low) / 2);

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

        private static final long serialVersionUID = 1L;

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

            double pivot = myArray.doubleValue(low + (high - low) / 2);

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

    public static final Factory<Double> R032 = Array1D.factory(ArrayR032.FACTORY);
    public static final Factory<Double> R064 = Array1D.factory(ArrayR064.FACTORY);
    public static final Factory<Quadruple> R128 = Array1D.factory(ArrayR128.FACTORY);
    public static final Factory<BigDecimal> R256 = Array1D.factory(ArrayR256.FACTORY);
    /**
     * @deprecated v52 Use {@link #R256} instead
     */
    @Deprecated
    public static final Factory<BigDecimal> BIG = R256;
    public static final Factory<ComplexNumber> C128 = Array1D.factory(ArrayC128.FACTORY);
    /**
     * @deprecated v52 Use {@link #C128} instead
     */
    @Deprecated
    public static final Factory<ComplexNumber> COMPLEX = C128;
    /**
     * @deprecated v52 Use {@link #factory(DenseArray.Factory)} instead
     */
    @Deprecated
    public static final Factory<Double> DIRECT32 = Array1D.factory(BufferArray.DIRECT32);
    /**
     * @deprecated v52 Use {@link #factory(DenseArray.Factory)} instead
     */
    @Deprecated
    public static final Factory<Double> DIRECT64 = Array1D.factory(BufferArray.DIRECT64);
    public static final Factory<Quaternion> H256 = Array1D.factory(ArrayH256.FACTORY);
    /**
     * @deprecated v52 Use {@link #R032} instead
     */
    @Deprecated
    public static final Factory<Double> PRIMITIVE32 = R032;
    /**
     * @deprecated v52 Use {@link #R064} instead
     */
    @Deprecated
    public static final Factory<Double> PRIMITIVE64 = R064;
    public static final Factory<RationalNumber> Q128 = Array1D.factory(ArrayQ128.FACTORY);
    /**
     * @deprecated v52 Use {@link #H256} instead
     */
    @Deprecated
    public static final Factory<Quaternion> QUATERNION = H256;
    /**
     * @deprecated v52 Use {@link #Q128} instead
     */
    @Deprecated
    public static final Factory<RationalNumber> RATIONAL = Q128;
    public static final Factory<Double> Z008 = Array1D.factory(ArrayZ008.FACTORY);
    public static final Factory<Double> Z016 = Array1D.factory(ArrayZ016.FACTORY);
    public static final Factory<Double> Z032 = Array1D.factory(ArrayZ032.FACTORY);
    public static final Factory<Double> Z064 = Array1D.factory(ArrayZ064.FACTORY);

    public static <N extends Comparable<N>> Array1D.Factory<N> factory(final DenseArray.Factory<N> denseFactory) {
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

    @Override
    public void add(final long index, final byte addend) {
        myDelegate.add(this.convert(index), addend);
    }

    @Override
    public void add(final long index, final Comparable<?> addend) {
        myDelegate.add(this.convert(index), addend);
    }

    @Override
    public void add(final long index, final double addend) {
        myDelegate.add(this.convert(index), addend);
    }

    @Override
    public void add(final long index, final float addend) {
        myDelegate.add(this.convert(index), addend);
    }

    @Override
    public void add(final long index, final int addend) {
        myDelegate.add(this.convert(index), addend);
    }

    @Override
    public void add(final long index, final long addend) {
        myDelegate.add(this.convert(index), addend);
    }

    @Override
    public void add(final long index, final short addend) {
        myDelegate.add(this.convert(index), addend);
    }

    @Override
    public N aggregateRange(final long first, final long limit, final Aggregator aggregator) {
        AggregatorFunction<N> visitor = aggregator.getFunction(myDelegate.factory().aggregator());
        this.visitRange(first, limit, visitor);
        return visitor.get();
    }

    @Override
    public byte byteValue(final int index) {
        return myDelegate.byteValue(this.convert(index));
    }

    @Override
    public byte byteValue(final long index) {
        return myDelegate.byteValue(this.convert(index));
    }

    @Override
    public void clear() {
        myDelegate.reset();
    }

    @Override
    public boolean contains(final Object obj) {
        return this.indexOf(obj) != -1;
    }

    @Override
    public long count() {
        return length;
    }

    @Override
    public double doubleValue(final int index) {
        return myDelegate.doubleValue(this.convert(index));
    }

    @Override
    public double doubleValue(final long index) {
        return myDelegate.doubleValue(this.convert(index));
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj) || !(obj instanceof Array1D)) {
            return false;
        }
        Array1D<?> other = (Array1D<?>) obj;
        if (length != other.length || myFirst != other.myFirst || myLimit != other.myLimit || myStep != other.myStep) {
            return false;
        }
        if (myDelegate == null) {
            if (other.myDelegate != null) {
                return false;
            }
        } else if (!myDelegate.equals(other.myDelegate)) {
            return false;
        }
        return true;
    }

    @Override
    public void fillAll(final N value) {
        myDelegate.fill(myFirst, myLimit, myStep, value);
    }

    @Override
    public void fillAll(final NullaryFunction<?> supplier) {
        myDelegate.fill(myFirst, myLimit, myStep, supplier);
    }

    @Override
    public void fillOne(final long index, final Access1D<?> values, final long valueIndex) {
        myDelegate.fillOne(this.convert(index), values, valueIndex);
    }

    @Override
    public void fillOne(final long index, final N value) {
        myDelegate.fillOne(this.convert(index), value);
    }

    @Override
    public void fillOne(final long index, final NullaryFunction<?> supplier) {
        myDelegate.fillOne(this.convert(index), supplier);
    }

    @Override
    public void fillRange(final long first, final long limit, final N value) {
        myDelegate.fill(this.convert(first), this.convert(limit), myStep, value);
    }

    @Override
    public void fillRange(final long first, final long limit, final NullaryFunction<?> supplier) {
        myDelegate.fill(this.convert(first), this.convert(limit), myStep, supplier);
    }

    @Override
    public float floatValue(final int index) {
        return myDelegate.floatValue(this.convert(index));
    }

    @Override
    public float floatValue(final long index) {
        return myDelegate.floatValue(this.convert(index));
    }

    @Override
    public N get(final int index) {
        return myDelegate.get(this.convert(index));
    }

    @Override
    public N get(final long index) {
        return myDelegate.get(this.convert(index));
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = super.hashCode();
        result = prime * result + (int) (length ^ length >>> 32);
        result = prime * result + (myDelegate == null ? 0 : myDelegate.hashCode());
        result = prime * result + (int) (myFirst ^ myFirst >>> 32);
        result = prime * result + (int) (myLimit ^ myLimit >>> 32);
        return prime * result + (int) (myStep ^ myStep >>> 32);
    }

    @Override
    public int indexOf(final Object obj) {
        int tmpLength = this.size();
        if (obj == null) {
            for (int i = 0; i < tmpLength; i++) {
                if (this.get(i) == null) {
                    return i;
                }
            }
        } else if (obj instanceof Comparable) {
            for (int i = 0; i < tmpLength; i++) {
                if (obj.equals(this.get(i))) {
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    public long indexOfLargest() {
        long first = this.convert(myFirst);
        long limit = this.convert(myLimit);
        long step = myStep;
        return (myDelegate.indexOfLargest(first, limit, step) - first) / step;
    }

    @Override
    public int intValue(final int index) {
        return myDelegate.intValue(this.convert(index));
    }

    @Override
    public int intValue(final long index) {
        return myDelegate.intValue(this.convert(index));
    }

    @Override
    public boolean isEmpty() {
        return length == 0;
    }

    @Override
    public long longValue(final int index) {
        return myDelegate.longValue(this.convert(index));
    }

    @Override
    public long longValue(final long index) {
        return myDelegate.longValue(this.convert(index));
    }

    @Override
    public double mix(final long index, final BinaryFunction<N> mixer, final double addend) {
        ProgrammingError.throwIfNull(mixer);
        synchronized (myDelegate) {
            double oldValue = this.doubleValue(index);
            double newValue = mixer.invoke(oldValue, addend);
            this.set(index, newValue);
            return newValue;
        }
    }

    @Override
    public N mix(final long index, final BinaryFunction<N> mixer, final N addend) {
        ProgrammingError.throwIfNull(mixer);
        synchronized (myDelegate) {
            N oldValue = this.get(index);
            N newValue = mixer.invoke(oldValue, addend);
            this.set(index, newValue);
            return newValue;
        }
    }

    @Override
    public void modifyAll(final UnaryFunction<N> modifier) {
        myDelegate.modify(myFirst, myLimit, myStep, modifier);
    }

    @Override
    public void modifyAny(final Transformation1D<N> modifier) {
        modifier.transform(this);
    }

    @Override
    public void modifyMatching(final Access1D<N> left, final BinaryFunction<N> function) {
        long limit = Math.min(length, left.count());
        if (myDelegate.isPrimitive()) {
            for (long i = 0L; i < limit; i++) {
                this.set(i, function.invoke(left.doubleValue(i), this.doubleValue(i)));
            }
        } else {
            for (long i = 0L; i < limit; i++) {
                this.set(i, function.invoke(left.get(i), this.get(i)));
            }
        }
    }

    @Override
    public void modifyMatching(final BinaryFunction<N> function, final Access1D<N> right) {
        long limit = Math.min(length, right.count());
        if (myDelegate.isPrimitive()) {
            for (long i = 0L; i < limit; i++) {
                this.set(i, function.invoke(this.doubleValue(i), right.doubleValue(i)));
            }
        } else {
            for (long i = 0L; i < limit; i++) {
                this.set(i, function.invoke(this.get(i), right.get(i)));
            }
        }
    }

    @Override
    public void modifyOne(final long index, final UnaryFunction<N> modifier) {
        myDelegate.modifyOne(this.convert(index), modifier);
    }

    @Override
    public void modifyRange(final long first, final long limit, final UnaryFunction<N> modifier) {
        myDelegate.modify(this.convert(first), this.convert(limit), myStep, modifier);
    }

    @Override
    public void reset() {
        myDelegate.reset();
    }

    @Override
    public void set(final int index, final byte value) {
        myDelegate.set(this.convert(index), value);
    }

    @Override
    public void set(final int index, final double value) {
        myDelegate.set(this.convert(index), value);
    }

    @Override
    public void set(final int index, final float value) {
        myDelegate.set(this.convert(index), value);
    }

    @Override
    public void set(final int index, final int value) {
        myDelegate.set(this.convert(index), value);
    }

    @Override
    public void set(final int index, final long value) {
        myDelegate.set(this.convert(index), value);
    }

    @Override
    public N set(final int index, final N value) {
        long tmpIndex = this.convert(index);
        N retVal = myDelegate.get(tmpIndex);
        myDelegate.set(tmpIndex, value);
        return retVal;
    }

    @Override
    public void set(final int index, final short value) {
        myDelegate.set(this.convert(index), value);
    }

    @Override
    public void set(final long index, final byte value) {
        myDelegate.set(this.convert(index), value);
    }

    @Override
    public void set(final long index, final Comparable<?> value) {
        myDelegate.set(this.convert(index), value);
    }

    @Override
    public void set(final long index, final double value) {
        myDelegate.set(this.convert(index), value);
    }

    @Override
    public void set(final long index, final float value) {
        myDelegate.set(this.convert(index), value);
    }

    @Override
    public void set(final long index, final int value) {
        myDelegate.set(this.convert(index), value);
    }

    @Override
    public void set(final long index, final long value) {
        myDelegate.set(this.convert(index), value);
    }

    @Override
    public void set(final long index, final short value) {
        myDelegate.set(this.convert(index), value);
    }

    @Override
    public short shortValue(final int index) {
        return myDelegate.shortValue(this.convert(index));
    }

    @Override
    public short shortValue(final long index) {
        return myDelegate.shortValue(this.convert(index));
    }

    @Override
    public int size() {
        return Math.toIntExact(length);
    }

    @Override
    public Array1D<N> sliceRange(final long first, final long limit) {
        return new Array1D<>(myDelegate, this.convert(first), this.convert(limit), myStep);
    }

    @Override
    public void sortAscending() {

        if (myDelegate instanceof Mutate1D.Sortable && this.count() == myDelegate.count()) {

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

    @Override
    public void sortDescending() {

        if (myDelegate instanceof Mutate1D.Sortable && this.count() == myDelegate.count()) {

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

    @Override
    public Array1D<N> subList(final int first, final int limit) {
        return this.sliceRange(first, limit);
    }

    @Override
    public void supplyTo(final Mutate1D receiver) {
        long limit = Math.min(length, receiver.count());
        if (myDelegate.isPrimitive()) {
            for (long i = 0L; i < limit; i++) {
                receiver.set(i, this.doubleValue(i));
            }
        } else {
            for (long i = 0L; i < limit; i++) {
                receiver.set(i, this.get(i));
            }
        }
    }

    @Override
    public String toString() {
        return Access1D.toString(this);
    }

    @Override
    public void visitAll(final VoidFunction<N> visitor) {
        myDelegate.visit(myFirst, myLimit, myStep, visitor);
    }

    @Override
    public void visitOne(final long index, final VoidFunction<N> visitor) {
        myDelegate.visitOne(this.convert(index), visitor);
    }

    @Override
    public void visitRange(final long first, final long limit, final VoidFunction<N> visitor) {
        myDelegate.visit(this.convert(first), this.convert(limit), myStep, visitor);
    }

    /**
     * Convert an external (public API) index to the corresponding internal
     */
    private long convert(final long index) {
        return myFirst + myStep * index;
    }

    void exchange(final long indexA, final long indexB) {

        if (myDelegate.isPrimitive()) {

            double tmpVal = this.doubleValue(indexA);
            this.set(indexA, this.doubleValue(indexB));
            this.set(indexB, tmpVal);

        } else {

            N tmpVal = this.get(indexA);
            this.set(indexA, this.get(indexB));
            this.set(indexB, tmpVal);
        }
    }

    BasicArray<N> getDelegate() {
        return myDelegate;
    }

    void sortAscending(final long low, final long high) {

        long i = low, j = high;

        double pivot = this.doubleValue(low + (high - low) / 2);

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

        double pivot = this.doubleValue(low + (high - low) / 2);

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

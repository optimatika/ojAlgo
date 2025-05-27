/*
 * Copyright 1997-2025 Optimatika
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import org.ojalgo.ProgrammingError;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Iterator1D;
import org.ojalgo.structure.Mutate1D;

/**
 * Think of this as an {@link ArrayList} that can only contain numbers, but with a few extra features. Among
 * other things it can be arbitrarily large (using long indices rather than int) and contain primitive double
 * values. And of course it integrates perfectly with everything else in ojAlgo.
 *
 * @author apete
 */
public final class NumberList<N extends Comparable<N>> implements List<N>, RandomAccess, Access1D<N>, Access1D.Visitable<N>, Mutate1D, Mutate1D.Mixable<N> {

    public static final class ListFactory<N extends Comparable<N>> {

        private final GrowthStrategy myGrowthStrategy;
        private final DenseArray.Factory<N, ?> myDenseFactory;

        ListFactory(final DenseArray.Factory<N, ?> denseFactory) {
            myGrowthStrategy = new GrowthStrategy(denseFactory.getMathType());
            myDenseFactory = denseFactory;
        }

        /**
         * @param chunk Defines a capacity break point. Below this point the capacity is doubled when needed.
         *              Above it, it is grown by adding one "chunk" at the time. Must be a power of 2. (The
         *              builder will enforce that for you.)
         * @return this
         */
        public ListFactory<N> chunk(final long chunk) {
            myGrowthStrategy.chunk(chunk);
            return this;
        }

        /**
         * @param initial Sets the initial capacity of the "arrays" to be created using this factory.
         * @return this
         */
        public ListFactory<N> initial(final long initial) {
            myGrowthStrategy.initial(initial);
            return this;
        }

        public NumberList<N> make() {
            return new NumberList<>(myDenseFactory, this.myGrowthStrategy);
        }

        /**
         * With very large data structures, particularly sparse ones, the underlying (dense) storage is
         * segmented. (Very large arrays are implemented as an array of arrays.) This determines the
         * size/length of one such segment. Must be a multiple of the chunk size as well as a power of 2. (The
         * builder will enforce this for you.)
         */
        public ListFactory<N> segment(final long segment) {
            myGrowthStrategy.segment(segment);
            return this;
        }

    }

    public static <N extends Comparable<N>> Collector<N, NumberList<N>, NumberList<N>> collector(final DenseArray.Factory<N, ?> arrayFactory) {

        Supplier<NumberList<N>> supplier = () -> NumberList.factory(arrayFactory).make();
        BiConsumer<NumberList<N>, N> accumulator = NumberList::add;
        BinaryOperator<NumberList<N>> combiner = (part1, part2) -> {
            part1.addAll(part2);
            return part1;
        };
        Function<NumberList<N>, NumberList<N>> finisher = Function.identity();

        return Collector.of(supplier, accumulator, combiner, finisher, Collector.Characteristics.IDENTITY_FINISH);
    }

    public static <N extends Comparable<N>> ListFactory<N> factory(final DenseArray.Factory<N, ?> arrayFactory) {
        return new ListFactory<>(arrayFactory);
    }

    private long myActualCount;
    private final DenseArray.Factory<N, ?> myDenseFactory;
    private final GrowthStrategy myGrowthStrategy;
    private BasicArray<N> myStorage;

    NumberList(final DenseArray.Factory<N, ?> denseFactory, final GrowthStrategy growthStrategy) {
        this(denseFactory, growthStrategy, growthStrategy.makeInitial(denseFactory::make), 0L);
    }

    NumberList(final DenseArray.Factory<N, ?> denseFactory, final GrowthStrategy growthStrategy, final BasicArray<N> storage, final long actualCount) {

        super();

        myDenseFactory = denseFactory;
        myGrowthStrategy = growthStrategy;

        myStorage = storage;
        myActualCount = actualCount;
    }

    public boolean add(final double element) {

        this.ensureCapacity();

        myStorage.set(myActualCount++, element);

        return true;
    }

    public boolean add(final float element) {

        this.ensureCapacity();

        myStorage.set(myActualCount++, element);

        return true;
    }

    @Override
    public void add(final int index, final N element) {

        this.ensureCapacity();

        for (long i = myActualCount - 1; i >= index; i--) {
            myStorage.set(i + 1, myStorage.get(i));
        }
        myStorage.set(index, element);

        myActualCount++;
    }

    @Override
    public boolean add(final N element) {

        this.ensureCapacity();

        myStorage.set(myActualCount++, element);

        return true;
    }

    @Override
    public boolean addAll(final Collection<? extends N> elements) {
        for (final N tmpElement : elements) {
            this.add(tmpElement);
        }
        return true;
    }

    public boolean addAll(final double[] elements) {
        for (final double tmpElement : elements) {
            this.add(tmpElement);
        }
        return true;
    }

    @Override
    public boolean addAll(final int index, final Collection<? extends N> elements) {
        int counter = 0;
        for (final N value : elements) {
            this.add(index + counter++, value);
        }
        return elements.size() > 0;
    }

    public N aggregateRange(final long first, final long limit, final Aggregator aggregator) {
        AggregatorFunction<N> visitor = aggregator.getFunction(myStorage.factory().aggregator());
        this.visitRange(first, limit, visitor);
        return visitor.get();
    }

    /**
     * The current capacity of the underlying data structure. The capacity is always greater than or equal to
     * the current number of items in the list. When you add items to the list the capacity may have to grow.
     */
    public long capacity() {
        return myStorage.count();
    }

    @Override
    public void clear() {
        myActualCount = 0L;
        myStorage.reset();
    }

    @Override
    public boolean contains(final Object object) {
        if (object instanceof Comparable) {
            return this.indexOf(object) >= 0;
        } else {
            return false;
        }
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
        for (final Object tmpObject : c) {
            if (!this.contains(tmpObject)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public long count() {
        return myActualCount;
    }

    @Override
    public double doubleValue(final int index) {
        if (index >= myActualCount) {
            throw new ArrayIndexOutOfBoundsException();
        } else {
            return myStorage.doubleValue(index);
        }
    }

    @Override
    public double doubleValue(final long index) {
        if (index >= myActualCount) {
            throw new ArrayIndexOutOfBoundsException();
        } else {
            return myStorage.doubleValue(index);
        }
    }

    @Override
    public N get(final int index) {
        if (index >= myActualCount) {
            throw new ArrayIndexOutOfBoundsException();
        } else {
            return myStorage.get(index);
        }
    }

    @Override
    public N get(final long index) {
        if (index >= myActualCount) {
            throw new ArrayIndexOutOfBoundsException();
        } else {
            return myStorage.get(index);
        }
    }

    @Override
    public int indexOf(final Object object) {
        final ListIterator<N> tmpIterator = this.listIterator();
        if (object == null) {
            while (tmpIterator.hasNext()) {
                if (tmpIterator.next() == null) {
                    return tmpIterator.previousIndex();
                }
            }
        } else {
            while (tmpIterator.hasNext()) {
                if (object.equals(tmpIterator.next())) {
                    return tmpIterator.previousIndex();
                }
            }
        }
        return -1;
    }

    @Override
    public boolean isEmpty() {
        return myActualCount == 0L;
    }

    @Override
    public Iterator<N> iterator() {
        return new Iterator1D<>(this);
    }

    @Override
    public int lastIndexOf(final Object object) {
        final ListIterator<N> tmpIterator = this.listIterator(this.size());
        if (object == null) {
            while (tmpIterator.hasPrevious()) {
                if (tmpIterator.previous() == null) {
                    return tmpIterator.nextIndex();
                }
            }
        } else {
            while (tmpIterator.hasPrevious()) {
                if (object.equals(tmpIterator.previous())) {
                    return tmpIterator.nextIndex();
                }
            }
        }
        return -1;
    }

    @Override
    public ListIterator<N> listIterator() {
        return new Iterator1D<>(this);
    }

    @Override
    public ListIterator<N> listIterator(final int index) {
        return new Iterator1D<>(this, index);
    }

    @Override
    public double mix(final long index, final BinaryFunction<N> mixer, final double addend) {
        ProgrammingError.throwIfNull(mixer);
        if (index >= myActualCount) {
            throw new ArrayIndexOutOfBoundsException();
        } else {
            synchronized (myStorage) {
                final double oldValue = myStorage.doubleValue(index);
                final double newValue = mixer.invoke(oldValue, addend);
                myStorage.set(index, newValue);
                return newValue;
            }
        }
    }

    @Override
    public N mix(final long index, final BinaryFunction<N> mixer, final N addend) {
        ProgrammingError.throwIfNull(mixer);
        if (index >= myActualCount) {
            throw new ArrayIndexOutOfBoundsException();
        } else {
            synchronized (myStorage) {
                final N oldValue = myStorage.get(index);
                final N newValue = mixer.invoke(oldValue, addend);
                myStorage.set(index, newValue);
                return newValue;
            }
        }
    }

    @Override
    public N remove(final int index) {

        final N oldValue = myStorage.get(index);

        myActualCount--;

        for (long i = index; i < myActualCount; i++) {
            myStorage.set(i, myStorage.get(i + 1));
        }

        return oldValue;
    }

    @Override
    public boolean remove(final Object o) {
        final int index = this.indexOf(o);
        if (index >= 0) {
            this.remove(index);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        boolean retVal = false;
        for (final Object o : c) {
            retVal &= this.remove(o);
        }
        return retVal;
    }

    @Override
    public boolean retainAll(final Collection<?> onlyKeep) {
        boolean retVal = false;
        final Object[] values = this.toArray();
        for (final Object v : values) {
            if (!onlyKeep.contains(v)) {
                retVal &= this.remove(v);
            }
        }
        return retVal;
    }

    @Override
    public void set(final int index, final double value) {
        if (index >= myActualCount) {
            throw new ArrayIndexOutOfBoundsException();
        } else {
            myStorage.set(index, value);
        }
    }

    @Override
    public N set(final int index, final N element) {
        if (index >= myActualCount) {
            throw new ArrayIndexOutOfBoundsException();
        } else {
            final N previous = myStorage.get(index);
            myStorage.set(index, element);
            return previous;
        }
    }

    @Override
    public void set(final long index, final Comparable<?> value) {
        if (index >= myActualCount) {
            throw new ArrayIndexOutOfBoundsException();
        } else {
            myStorage.set(index, value);
        }
    }

    @Override
    public void set(final long index, final double value) {
        if (index >= myActualCount) {
            throw new ArrayIndexOutOfBoundsException();
        } else {
            myStorage.set(index, value);
        }
    }

    @Override
    public void set(final long index, final float value) {
        if (index >= myActualCount) {
            throw new ArrayIndexOutOfBoundsException();
        } else {
            myStorage.set(index, value);
        }
    }

    @Override
    public int size() {
        return (int) myActualCount;
    }

    @Override
    public NumberList<N> subList(final int fromIndex, final int toIndex) {
        final NumberList<N> retVal = new NumberList<>(myDenseFactory, myGrowthStrategy);
        if (myStorage instanceof ArrayR064) {
            for (int i = 0; i < toIndex; i++) {
                retVal.add(this.doubleValue(i));
            }
        } else {
            for (int i = 0; i < toIndex; i++) {
                retVal.add(this.get(i));
            }
        }
        return retVal;
    }

    @Override
    public Object[] toArray() {
        return this.toArray(new Object[this.size()]);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(final T[] array) {
        for (int i = 0; i < array.length; i++) {
            array[i] = (T) myStorage.get(i);
        }
        return array;
    }

    @Override
    public String toString() {
        return Access1D.toString(this);
    }

    @Override
    public void visitOne(final long index, final VoidFunction<N> visitor) {
        if (index >= myActualCount) {
            throw new ArrayIndexOutOfBoundsException();
        } else {
            myStorage.visitOne(index, visitor);
        }
    }

    private void ensureCapacity() {

        if (myStorage.count() > myActualCount) {
            // It fits, just add to the end

        } else if (myGrowthStrategy.isSegmented(myActualCount + 1L)) {
            // Doesn't fit, create or grow segment, then add

            if (myStorage instanceof SegmentedArray) {
                myStorage = ((SegmentedArray<N>) myStorage).grow();
            } else {
                BasicArray<N>[] segments = (BasicArray<N>[]) new BasicArray<?>[] { myStorage, myGrowthStrategy.makeChunk(myDenseFactory::make) };
                myStorage = new SegmentedArray<>(segments, myDenseFactory);
            }

        } else {
            // Doesn't fit, grow, then add

            long newTotalCount = myGrowthStrategy.grow(myActualCount);

            BasicArray<N> newStorage = myDenseFactory.make(newTotalCount);
            newStorage.fillMatching(myStorage);
            myStorage = newStorage;
        }
    }

}

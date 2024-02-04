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

import java.util.List;

import org.ojalgo.function.VoidFunction;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.type.NumberDefinition;
import org.ojalgo.type.context.NumberContext;

/**
 * 1-dimensional accessor (get) methods. The nested interfaces declare additional methods that implicitly
 * require that the elements have been accessed, but they do not extends the main/outer interface. A
 * 1D-structure can be vistiable, aggregatable and/or expose various element properties without allowing
 * explicit access to its elements.
 *
 * @author apete
 */
public interface Access1D<N extends Comparable<N>> extends Structure1D {

    /**
     * This interface complements {@linkplain Visitable} but does not extend it. It's a feature to be able to
     * be aggregatable but not necessarily visitable in that it does not require generic input parameters.
     *
     * @author apete
     */
    public interface Aggregatable<N extends Comparable<N>> extends Structure1D {

        default N aggregateAll(final Aggregator aggregator) {
            return this.aggregateRange(0L, this.count(), aggregator);
        }

        N aggregateRange(long first, long limit, Aggregator aggregator);

        long indexOfLargest();

    }

    public interface Collectable<N extends Comparable<N>, R extends Mutate1D> extends Structure1D {

        default <I extends R> I collect(final Factory1D<I> factory) {

            I retVal = factory.make(this.count());

            this.supplyTo(retVal);

            return retVal;
        }

        void supplyTo(R receiver);

    }

    public static final class ElementView<N extends Comparable<N>> implements ElementView1D<N, ElementView<N>> {

        private long myCursor;
        private long myLastCursor;
        private final Access1D<N> myValues;

        private ElementView(final Access1D<N> values, final long initial, final long last) {

            super();

            myValues = values;
            myCursor = initial;
            myLastCursor = last;
        }

        ElementView(final Access1D<N> values) {
            this(values, -1L, values.count() - 1L);
        }

        @Override
        public double doubleValue() {
            return myValues.doubleValue(myCursor);
        }

        @Override
        public long estimateSize() {
            return myLastCursor - myCursor;
        }

        @Override
        public N get() {
            return myValues.get(myCursor);
        }

        @Override
        public boolean hasNext() {
            return myCursor < myLastCursor;
        }

        @Override
        public boolean hasPrevious() {
            return myCursor > 0;
        }

        @Override
        public long index() {
            return myCursor;
        }

        @Override
        public ElementView<N> iterator() {
            return new ElementView<>(myValues);
        }

        @Override
        public ElementView<N> next() {
            myCursor++;
            return this;
        }

        @Override
        public ElementView<N> previous() {
            myCursor--;
            return this;
        }

        @Override
        public String toString() {
            return myCursor + " = " + myValues.get(myCursor);
        }

        @Override
        public ElementView<N> trySplit() {

            long remaining = myLastCursor - myCursor;

            if (remaining > 1L) {

                long split = myCursor + remaining / 2L;

                ElementView<N> retVal = new ElementView<>(myValues, myCursor, split);

                myCursor = split;

                return retVal;
            }

            return null;
        }

    }

    public static final class SelectionView<N extends Comparable<N>> implements Access1D<N>, Collectable<N, Mutate1D> {

        private final Access1D<N> myFullData;
        private final long[] mySelection;

        SelectionView(final Access1D<N> fullData, final long[] selection) {

            super();

            myFullData = fullData;
            mySelection = selection;
        }

        @Override
        public long count() {
            return mySelection.length;
        }

        @Override
        public double doubleValue(final int index) {
            return myFullData.doubleValue(mySelection[index]);
        }

        @Override
        public N get(final long index) {
            return myFullData.get(mySelection[Math.toIntExact(index)]);
        }

        @Override
        public void supplyTo(final Mutate1D receiver) {
            for (int i = 0, limit = mySelection.length; i < limit; i++) {
                receiver.set(i, myFullData.get(mySelection[i]));
            }
        }

        @Override
        public String toString() {
            return Access1D.toString(this);
        }

    }

    public interface Sliceable<N extends Comparable<N>> extends Structure1D {

        Access1D<N> sliceRange(long first, long limit);

    }

    public interface Visitable<N extends Comparable<N>> extends Structure1D {

        default void visitAll(final VoidFunction<N> visitor) {
            this.visitRange(0L, this.count(), visitor);
        }

        void visitOne(long index, VoidFunction<N> visitor);

        default void visitRange(final long first, final long limit, final VoidFunction<N> visitor) {
            for (long i = first; i < limit; i++) {
                this.visitOne(i, visitor);
            }
        }

    }

    static Access1D<Double> asPrimitive1D(final Access1D<?> access) {
        return new Access1D<>() {

            public long count() {
                return access.count();
            }

            public double doubleValue(final int index) {
                return access.doubleValue(index);
            }

            public Double get(final long index) {
                return Double.valueOf(access.doubleValue(index));
            }

            @Override
            public String toString() {
                return Access1D.toString(this);
            }

        };
    }

    /**
     * Tests if the two data structures are numerically equal to the given accuracy. (Only works with real
     * numbers, and can't handle more than "double precision".) You have to implement your own version to
     * handle other cases.
     */
    static boolean equals(final Access1D<?> accessA, final Access1D<?> accessB, final NumberContext accuracy) {

        long length = accessA.count();

        if (length != accessB.count()) {
            return false;
        }

        double magnitudeA = 0D;
        for (long i = 0; i < length; i++) {
            magnitudeA = Math.max(magnitudeA, Math.abs(accessA.doubleValue(i)));
        }

        double magnitudeB = 0D;
        for (long i = 0; i < length; i++) {
            magnitudeB = Math.max(magnitudeB, Math.abs(accessB.doubleValue(i)));
        }

        if (accuracy.isDifferent(magnitudeA, magnitudeB)) {
            return false;
        }

        double magnitude = Math.max(magnitudeA, magnitudeB);

        for (long i = 0; i < length; i++) {
            if (!accuracy.isSmall(magnitude, accessA.doubleValue(i) - accessB.doubleValue(i))) {
                return false;
            }
        }

        return true;
    }

    static String toString(final Access1D<?> access) {
        int size = access.size();
        switch (size) {
        case 0:
            return "{ }";
        case 1:
            return "{ " + access.get(0) + " }";
        default:
            StringBuilder builder = new StringBuilder();
            builder.append("{ ");
            builder.append(access.get(0));
            for (int i = 1; i < size; i++) {
                builder.append(", ");
                builder.append(access.get(i));
            }
            builder.append(" }");
            return builder.toString();
        }
    }

    static Access1D<Double> wrap(final double... target) {
        return new Access1D<>() {

            public long count() {
                return target.length;
            }

            public double doubleValue(final int index) {
                return target[index];
            }

            public Double get(final long index) {
                return Double.valueOf(this.doubleValue(index));
            }

            @Override
            public String toString() {
                return Access1D.toString(this);
            }

        };
    }

    static <N extends Comparable<N>> Access1D<N> wrap(final List<? extends N> target) {
        return new Access1D<>() {

            public long count() {
                return target.size();
            }

            public double doubleValue(final int index) {
                return NumberDefinition.doubleValue(target.get(index));
            }

            public N get(final long index) {
                return target.get(Math.toIntExact(index));
            }

            @Override
            public String toString() {
                return Access1D.toString(this);
            }

        };
    }

    static <N extends Comparable<N>> Access1D<N> wrap(final N[] target) {
        return new Access1D<>() {

            public long count() {
                return target.length;
            }

            public double doubleValue(final int index) {
                return NumberDefinition.doubleValue(target[index]);
            }

            public N get(final long index) {
                return target[Math.toIntExact(index)];
            }

            @Override
            public String toString() {
                return Access1D.toString(this);
            }

        };
    }

    /**
     * Transforms this {@link Access1D} to a {@link Access1D.Collectable} of a different {@link Comparable}
     * type.
     */
    default <NN extends Comparable<NN>, R extends Mutate1D.Receiver<NN>> Collectable<NN, R> asCollectable1D() {
        return new Collectable<>() {

            public long count() {
                return Access1D.this.count();
            }

            public void supplyTo(final R receiver) {
                receiver.accept(Access1D.this);
                //                receiver.reset();
                //                Access1D.this.nonzeros().forEach(nz -> receiver.set(nz.index(), nz.doubleValue()));
            }

        };
    }

    default <K> Keyed1D<K, N> asKeyed1D(final IndexMapper<K> indexMapper) {
        return new Keyed1D<>(this, indexMapper);
    }

    /**
     * Will calculate y = y + a x, will add "a" times "this" to "y"
     *
     * @param a The scale
     * @param y The "vector" to update
     */
    default void axpy(final double a, final Mutate1D.Modifiable<?> y) {
        for (ElementView1D<N, ?> element : this.nonzeros()) {
            y.add(element.index(), a * element.doubleValue());
        }
    }

    default byte byteValue(final int index) {
        return (byte) this.shortValue(index);
    }

    default byte byteValue(final long index) {
        return this.byteValue(Math.toIntExact(index));
    }

    /**
     * Will calculate and return the dot product of this 1D-structure and another input 1D-vector.
     *
     * @param vector Another 1D-structure
     * @return The dot product
     */
    default double dot(final Access1D<?> vector) {

        double retVal = 0D;

        for (ElementView1D<N, ?> element : this.nonzeros()) {
            retVal += element.doubleValue() * vector.doubleValue(element.index());
        }

        return retVal;
    }

    double doubleValue(int index);

    default double doubleValue(final long index) {
        return this.doubleValue(Math.toIntExact(index));
    }

    /**
     * Returns an Iterable of ElementView1D. It allows to iterate over the instance's element "positions"
     * without actually extracting the elements (unless you explicitly do so).
     */
    default ElementView1D<N, ?> elements() {
        return new Access1D.ElementView<>(this);
    }

    default float floatValue(final int index) {
        return (float) this.doubleValue(index);
    }

    default float floatValue(final long index) {
        return this.floatValue(Math.toIntExact(index));
    }

    N get(long index);

    default int intValue(final int index) {
        return (int) this.longValue(index);
    }

    default int intValue(final long index) {
        return this.intValue(Math.toIntExact(index));
    }

    default long longValue(final int index) {
        return Math.round(this.doubleValue(index));
    }

    default long longValue(final long index) {
        return this.longValue(Math.toIntExact(index));
    }

    /**
     * Similar to {@link #elements()} but avoids elements that are structurally known to be zero. (That does
     * not eliminate all zero-values from this view.) With an arbitrary (dense) unstructured implementation
     * the {@link #nonzeros()} and {@link #elements()} methods do the same thing! Only some specific
     * implementations are able to actually exploit structure/sparsity to view fewer elements.
     */
    default ElementView1D<N, ?> nonzeros() {
        return this.elements();
    }

    /**
     * Creates a view of the underlying data structure of only the selected elements.
     */
    default Access1D<N> select(final long... selection) {
        return new Access1D.SelectionView<>(this, selection);
    }

    default short shortValue(final int index) {
        return (short) this.intValue(index);
    }

    default short shortValue(final long index) {
        return this.shortValue(Math.toIntExact(index));
    }

    default void supplyTo(final double[] receiver) {
        int limit = Math.min(receiver.length, (int) this.count());
        for (int i = 0; i < limit; i++) {
            receiver[i] = this.doubleValue(i);
        }
    }

    default double[] toRawCopy1D() {

        int tmpLength = (int) this.count();

        double[] retVal = new double[tmpLength];

        this.supplyTo(retVal);

        return retVal;
    }

}

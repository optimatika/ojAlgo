/*
 * Copyright 1997-2021 Optimatika
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
import java.util.concurrent.atomic.DoubleAdder;

import org.ojalgo.function.VoidFunction;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.scalar.Scalar;
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

    }

    public interface Collectable<R extends Mutate1D> extends Structure1D {

        default <I extends R> I collect(final Factory1D<I> factory) {

            I retVal = factory.make(this.count());

            this.supplyTo(retVal);

            return retVal;
        }

        void supplyTo(R receiver);

    }

    /**
     * @deprecated v48 Will be removed
     */
    @Deprecated
    public interface Elements extends Structure1D {

        /**
         * @see Scalar#isAbsolute()
         * @deprecated v48 Will be removed
         */
        @Deprecated
        boolean isAbsolute(long index);

        /**
         * @see Scalar#isSmall(double)
         * @deprecated v48 Will be removed
         */
        @Deprecated
        default boolean isAllSmall(final double comparedTo) {
            boolean retVal = true;
            for (long i = 0L, limit = this.count(); retVal && i < limit; i++) {
                retVal &= this.isSmall(i, comparedTo);
            }
            return retVal;
        }

        /**
         * @see Scalar#isSmall(double)
         * @deprecated v48 Will be removed
         */
        @Deprecated
        boolean isSmall(long index, double comparedTo);

    }

    public static final class ElementView<N extends Comparable<N>> implements ElementView1D<N, ElementView<N>> {

        private long myCursor;
        private final long myLastCursor;
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

        public double doubleValue() {
            return myValues.doubleValue(myCursor);
        }

        public long estimateSize() {
            return myLastCursor - myCursor;
        }

        public N get() {
            return myValues.get(myCursor);
        }

        public boolean hasNext() {
            return myCursor < myLastCursor;
        }

        public boolean hasPrevious() {
            return myCursor > 0;
        }

        public long index() {
            return myCursor;
        }

        public ElementView<N> iterator() {
            return new ElementView<>(myValues);
        }

        public ElementView<N> next() {
            myCursor++;
            return this;
        }

        public ElementView<N> previous() {
            myCursor--;
            return this;
        }

        @Override
        public String toString() {
            return myCursor + " = " + myValues.get(myCursor);
        }

        public ElementView<N> trySplit() {

            final long remaining = myLastCursor - myCursor;

            if (remaining > 1L) {

                final long split = myCursor + remaining / 2L;

                final ElementView<N> retVal = new ElementView<>(myValues, myCursor, split);

                myCursor = split;

                return retVal;

            }
            return null;
        }

    }

    public interface IndexOf extends Structure1D {

        default long indexOfLargest() {
            return this.indexOfLargestInRange(0L, this.count());
        }

        /**
         * @deprecated v48 Will be removed
         */
        @Deprecated
        long indexOfLargestInRange(final long first, final long limit);

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
            Structure1D.loopRange(first, limit, i -> this.visitOne(i, visitor));
        }

    }

    static Access1D<Double> asPrimitive1D(final Access1D<?> access) {
        return new Access1D<Double>() {

            public long count() {
                return access.count();
            }

            public double doubleValue(final long index) {
                return access.doubleValue(index);
            }

            public Double get(final long index) {
                return access.doubleValue(index);
            }

            @Override
            public String toString() {
                return Access1D.toString(this);
            }

        };
    }

    /**
     * Tests if the two data strauctures are numerically equal to the given accuracy. (Only works with real
     * numbers, and can't handle more than "double precision".)
     */
    static boolean equals(final Access1D<?> accessA, final Access1D<?> accessB, final NumberContext accuracy) {

        long length = accessA.count();

        boolean retVal = length == accessB.count();

        for (int i = 0; retVal && i < length; i++) {
            retVal &= !accuracy.isDifferent(accessA.doubleValue(i), accessB.doubleValue(i));
        }

        return retVal;
    }

    /**
     * @deprecated v49 Implement your own
     */
    @Deprecated
    static int hashCode(final Access1D<?> access) {
        final int limit = access.size();
        int retVal = limit + 31;
        for (int ij = 0; ij < limit; ij++) {
            retVal *= access.intValue(ij);
        }
        return retVal;
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
        return new Access1D<Double>() {

            public long count() {
                return target.length;
            }

            public double doubleValue(final long index) {
                return target[Math.toIntExact(index)];
            }

            public Double get(final long index) {
                return this.doubleValue(index);
            }

            @Override
            public String toString() {
                return Access1D.toString(this);
            }

        };
    }

    static <N extends Comparable<N>> Access1D<N> wrap(final List<? extends N> target) {
        return new Access1D<N>() {

            public long count() {
                return target.size();
            }

            public double doubleValue(final long index) {
                return NumberDefinition.doubleValue(target.get((int) index));
            }

            public N get(final long index) {
                return target.get((int) index);
            }

            @Override
            public String toString() {
                return Access1D.toString(this);
            }

        };
    }

    static <N extends Comparable<N>> Access1D<N> wrap(final N[] target) {
        return new Access1D<N>() {

            public long count() {
                return target.length;
            }

            public double doubleValue(final long index) {
                return NumberDefinition.doubleValue(target[(int) index]);
            }

            public N get(final long index) {
                return target[(int) index];
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
    default <NN extends Comparable<NN>, R extends Mutate1D.Receiver<NN>> Collectable<R> asCollectable1D() {
        return new Collectable<R>() {

            public long count() {
                return Access1D.this.count();
            }

            public void supplyTo(final R receiver) {
                receiver.accept(Access1D.this);
            }

        };
    }

    /**
     * Will calculate y = y + a x, will add "a" times "this" to "y"
     *
     * @param a The scale
     * @param y The "vector" to update
     */
    default void axpy(final double a, final Mutate1D.Modifiable<?> y) {
        Structure1D.loopMatching(this, y, i -> y.add(i, a * this.doubleValue(i)));
    }

    default byte byteValue(final long index) {
        return (byte) this.shortValue(index);
    }

    /**
     * Will calculate and return the dot product of this 1D-structure and another input 1D-vector.
     *
     * @param vector Another 1D-structure
     * @return The dot product
     */
    default double dot(final Access1D<?> vector) {
        final DoubleAdder retVal = new DoubleAdder();
        Structure1D.loopMatching(this, vector, i -> retVal.add(this.doubleValue(i) * vector.doubleValue(i)));
        return retVal.doubleValue();
    }

    double doubleValue(long index);

    /**
     * Returns an Iterable of ElementView1D. It allows to iterate over the instance's element "positions"
     * without actually extracting the elements (unless you explicitly do so).
     */
    default ElementView1D<N, ?> elements() {
        return new Access1D.ElementView<>(this);
    }

    default float floatValue(final long index) {
        return (float) this.doubleValue(index);
    }

    N get(long index);

    default int intValue(final long index) {
        return (int) this.longValue(index);
    }

    default long longValue(final long index) {
        return Math.round(this.doubleValue(index));
    }

    default ElementView1D<N, ?> nonzeros() {
        return this.elements();
    }

    default short shortValue(final long index) {
        return (short) this.intValue(index);
    }

    default void supplyTo(final double[] receiver) {
        final int limit = Math.min(receiver.length, (int) this.count());
        for (int i = 0; i < limit; i++) {
            receiver[i] = this.doubleValue(i);
        }
    }

    default double[] toRawCopy1D() {

        final int tmpLength = (int) this.count();

        final double[] retVal = new double[tmpLength];

        this.supplyTo(retVal);

        return retVal;
    }

}

/*
 * Copyright 1997-2017 Optimatika (www.optimatika.se)
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
package org.ojalgo.access;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.stream.BaseStream;
import java.util.stream.StreamSupport;

import org.ojalgo.function.VoidFunction;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.type.TypeUtils;
import org.ojalgo.type.context.NumberContext;

/**
 * 1-dimensional accessor (get) methods. The nested interfaces declare additional methods that indirectly
 * requires that the elements has been accessed, but they do not extends the main/outer interface. A
 * 1D-structure can be vistiable, aggregatable and/or expose various element properties without allowing
 * explicit access to its elements.
 *
 * @author apete
 */
public interface Access1D<N extends Number> extends Structure1D, Iterable<N> {

    public interface Aggregatable<N extends Number> extends Structure1D {

        default N aggregateAll(final Aggregator aggregator) {
            return this.aggregateRange(0L, this.count(), aggregator);
        }

        N aggregateRange(long first, long limit, Aggregator aggregator);

    }

    public interface Collectable<N extends Number, R extends Mutate1D.Receiver<N>> extends Structure1D {

        default <I extends R> I collect(final Factory1D<I> factory) {

            final I retVal = factory.makeZero(this.count());

            this.supplyTo(retVal);

            return retVal;
        }

        void supplyTo(R receiver);

    }

    public interface Elements extends Structure1D {

        /**
         * @see Scalar#isAbsolute()
         */
        boolean isAbsolute(long index);

        /**
         * @see Scalar#isSmall(double)
         */
        default boolean isAllSmall(final double comparedTo) {
            boolean retVal = true;
            final long tmpLimit = this.count();
            for (long i = 0L; retVal && (i < tmpLimit); i++) {
                retVal &= this.isSmall(i, comparedTo);
            }
            return retVal;
        }

        /**
         * @see Scalar#isSmall(double)
         */
        boolean isSmall(long index, double comparedTo);

    }

    public static final class ElementView<N extends Number> implements ElementView1D<N, ElementView<N>> {

        private long myCursor = -1;
        private final long myLastCursor;
        private final Access1D<N> myValues;

        ElementView(final Access1D<N> values) {

            super();

            myValues = values;
            myLastCursor = values.count() - 1;
        }

        public double doubleValue() {
            return myValues.doubleValue(myCursor);
        }

        public N getNumber() {
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

        public ElementView<N> next() {
            myCursor++;
            return this;
        }

        public ElementView<N> previous() {
            myCursor--;
            return this;
        }

    }

    public interface IndexOf extends Structure1D {

        default long indexOfLargest() {
            return this.indexOfLargestInRange(0L, this.count());
        }

        long indexOfLargestInRange(final long first, final long limit);

    }

    public interface Sliceable<N extends Number> extends Structure1D {

        Access1D<N> sliceRange(long first, long limit);

    }

    public interface Visitable<N extends Number> extends Structure1D {

        default void visitAll(final VoidFunction<N> visitor) {
            this.visitRange(0L, this.count(), visitor);
        }

        void visitOne(long index, VoidFunction<N> visitor);

        default void visitRange(final long first, final long limit, final VoidFunction<N> visitor) {
            Structure1D.loopRange(first, limit, i -> this.visitOne(i, visitor));
        }

    }

    static Access1D<BigDecimal> asBig1D(final Access1D<?> access) {
        return new Access1D<BigDecimal>() {

            public long count() {
                return access.count();
            }

            public double doubleValue(final long index) {
                return access.doubleValue(index);
            }

            public BigDecimal get(final long index) {
                return TypeUtils.toBigDecimal(access.get(index));
            }

        };
    }

    static Access1D<ComplexNumber> asComplex1D(final Access1D<?> access) {
        return new Access1D<ComplexNumber>() {

            public long count() {
                return access.count();
            }

            public double doubleValue(final long index) {
                return access.doubleValue(index);
            }

            public ComplexNumber get(final long index) {
                return ComplexNumber.valueOf(access.get(index));
            }

        };
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

        };
    }

    static Access1D<Quaternion> asQuaternion1D(final Access1D<?> access) {
        return new Access1D<Quaternion>() {

            public long count() {
                return access.count();
            }

            public double doubleValue(final long index) {
                return access.doubleValue(index);
            }

            public Quaternion get(final long index) {
                return Quaternion.valueOf(access.get(index));
            }

        };
    }

    static Access1D<RationalNumber> asRational1D(final Access1D<?> access) {
        return new Access1D<RationalNumber>() {

            public long count() {
                return access.count();
            }

            public double doubleValue(final long index) {
                return access.doubleValue(index);
            }

            public RationalNumber get(final long index) {
                return RationalNumber.valueOf(access.get(index));
            }

        };
    }

    @SuppressWarnings("unchecked")
    static boolean equals(final Access1D<?> accessA, final Access1D<?> accessB, final NumberContext context) {

        final long tmpLength = accessA.count();

        boolean retVal = tmpLength == accessB.count();

        if ((accessA.get(0) instanceof ComplexNumber) && (accessB.get(0) instanceof ComplexNumber)) {

            final Access1D<ComplexNumber> tmpAccessA = (Access1D<ComplexNumber>) accessA;
            final Access1D<ComplexNumber> tmpAccessB = (Access1D<ComplexNumber>) accessB;

            for (int i = 0; retVal && (i < tmpLength); i++) {
                retVal &= !context.isDifferent(tmpAccessA.get(i).getReal(), tmpAccessB.get(i).getReal());
                retVal &= !context.isDifferent(tmpAccessA.get(i).i, tmpAccessB.get(i).i);
            }

        } else {

            for (int i = 0; retVal && (i < tmpLength); i++) {
                retVal &= !context.isDifferent(accessA.doubleValue(i), accessB.doubleValue(i));
            }
        }

        return retVal;
    }

    static int hashCode(final Access1D<?> access) {
        final int tmpSize = (int) access.count();
        int retVal = tmpSize + 31;
        for (int ij = 0; ij < tmpSize; ij++) {
            retVal *= access.doubleValue(ij);
        }
        return retVal;
    }

    static Access1D<Double> wrapAccess1D(final double[] target) {
        return new Access1D<Double>() {

            public long count() {
                return target.length;
            }

            public double doubleValue(final long index) {
                return target[(int) index];
            }

            public Double get(final long index) {
                return target[(int) index];
            }

        };
    }

    static <N extends Number> Access1D<N> wrapAccess1D(final List<? extends N> target) {
        return new Access1D<N>() {

            public long count() {
                return target.size();
            }

            public double doubleValue(final long index) {
                return target.get((int) index).doubleValue();
            }

            public N get(final long index) {
                return target.get((int) index);
            }

        };
    }

    static <N extends Number> Access1D<N> wrapAccess1D(final N[] target) {
        return new Access1D<N>() {

            public long count() {
                return target.length;
            }

            public double doubleValue(final long index) {
                return target[(int) index].doubleValue();
            }

            public N get(final long index) {
                return target[(int) index];
            }

        };
    }

    default <NN extends Number, R extends Mutate1D.Receiver<NN>> Collectable<NN, R> asCollectable1D() {
        return new Collectable<NN, R>() {

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
    default void axpy(final double a, final Mutate1D y) {
        Structure1D.loopMatching(this, y, i -> y.add(i, a * this.doubleValue(i)));
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

    N get(long index);

    default Iterator<N> iterator() {
        return new Iterator1D<>(this);
    }

    default BaseStream<N, ? extends BaseStream<N, ?>> stream(final boolean parallel) {
        return StreamSupport.stream(this.spliterator(), parallel);
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

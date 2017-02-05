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
package org.ojalgo.access;

import java.util.Iterator;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.stream.BaseStream;
import java.util.stream.StreamSupport;

import org.ojalgo.function.VoidFunction;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.scalar.Scalar;

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

    default double[] toRawCopy1D() {

        final int tmpLength = (int) this.count();

        final double[] retVal = new double[tmpLength];

        for (int i = 0; i < tmpLength; i++) {
            retVal[i] = this.doubleValue(i);
        }

        return retVal;
    }

}

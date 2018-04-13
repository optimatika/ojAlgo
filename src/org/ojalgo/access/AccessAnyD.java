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
package org.ojalgo.access;

import org.ojalgo.function.VoidFunction;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.type.context.NumberContext;

/**
 * N-dimensional accessor methods
 *
 * @see Access1D
 * @author apete
 */
public interface AccessAnyD<N extends Number> extends StructureAnyD, Access1D<N> {

    public interface Aggregatable<N extends Number> extends StructureAnyD, Access1D.Aggregatable<N> {

        Number aggregateSet(int dimension, long dimensionalIndex, Aggregator aggregator);

        Number aggregateSet(long[] initial, int dimension, Aggregator aggregator);

        default void reduce(int dimension, Aggregator aggregator, Mutate1D receiver) {
            final long count1 = this.count(dimension);
            final long count2 = receiver.count();
            for (long i = 0L, limit = Math.min(count1, count2); i < limit; i++) {
                receiver.set(i, this.aggregateSet(dimension, i, aggregator));
            }
        }

    }

    public interface Collectable<N extends Number, R extends MutateAnyD.Receiver<N>> extends StructureAnyD {

        default <I extends R> I collect(final FactoryAnyD<I> factory) {

            final I retVal = factory.makeZero(this.shape());

            this.supplyTo(retVal);

            return retVal;
        }

        void supplyTo(R receiver);

    }

    public interface Elements extends StructureAnyD, Access1D.Elements {

        /**
         * @see Scalar#isAbsolute()
         */
        boolean isAbsolute(long[] reference);

        /**
         * @see Scalar#isSmall(double)
         */
        boolean isSmall(long[] reference, double comparedTo);

    }

    public interface IndexOf extends StructureAnyD, Access1D.IndexOf {

    }

    public interface Sliceable<N extends Number> extends StructureAnyD, Access1D.Sliceable<N> {

        Access1D<N> sliceSet(final long[] initial, final int dimension);

    }

    public interface Visitable<N extends Number> extends StructureAnyD, Access1D.Visitable<N> {

        void visitOne(long[] reference, VoidFunction<N> visitor);

        void visitSet(int dimension, long dimensionalIndex, VoidFunction<N> visitor);

        void visitSet(long[] initial, int dimension, VoidFunction<N> visitor);

    }

    static AccessAnyD<Double> asPrimitiveAnyD(final AccessAnyD<?> access) {
        return new AccessAnyD<Double>() {

            public long count() {
                return access.count();
            }

            public long count(final int dimension) {
                return access.count(dimension);
            }

            public double doubleValue(final long index) {
                return access.doubleValue(index);
            }

            public double doubleValue(final long[] ref) {
                return access.doubleValue(ref);
            }

            public Double get(final long index) {
                return access.doubleValue(index);
            }

            public Double get(final long[] ref) {
                return access.doubleValue(ref);
            }

            public long[] shape() {
                return access.shape();
            }

        };
    }

    static boolean equals(final AccessAnyD<?> accessA, final AccessAnyD<?> accessB, final NumberContext context) {

        boolean retVal = true;
        int d = 0;
        long tmpCount;

        do {
            tmpCount = accessA.count(d);
            retVal &= tmpCount == accessB.count(d);
            d++;
        } while (retVal && ((d <= 3) || (tmpCount > 1)));

        return retVal && Access1D.equals(accessA, accessB, context);
    }

    default <NN extends Number, R extends MutateAnyD.Receiver<NN>> Collectable<NN, R> asCollectableAnyD() {
        return new Collectable<NN, R>() {

            public long count(final int dimension) {
                return AccessAnyD.this.count(dimension);
            }

            public long[] shape() {
                return AccessAnyD.this.shape();
            }

            public void supplyTo(final R receiver) {
                receiver.accept(AccessAnyD.this);
            }

        };
    }

    /**
     * Will pass through each matching element position calling the {@code through} function. What happens is
     * entirely dictated by how you implement the callback.
     */
    default double doubleValue(final long index) {
        return this.doubleValue(StructureAnyD.reference(index, this.shape()));
    }

    double doubleValue(long[] ref);

    default N get(final long index) {
        return this.get(StructureAnyD.reference(index, this.shape()));
    }

    N get(long[] ref);

}

/*
 * Copyright 1997-2022 Optimatika
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

import org.ojalgo.array.operation.AMAX;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.FunctionSet;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.function.aggregator.AggregatorSet;
import org.ojalgo.function.special.PowerOf2;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Mutate1D;
import org.ojalgo.structure.StructureAnyD;

/**
 * <p>
 * A BasicArray is 1-dimensional, but designed to easily be extended or encapsulated, and then treated as
 * arbitrary-dimensional. It stores/handles (any subclass of) {@linkplain java.lang.Comparable} elements
 * depending on the subclass/implementation.
 * </p>
 * <p>
 * This abstract class defines a set of methods to access and modify array elements. It does not "know"
 * anything about linear algebra or similar.
 * </p>
 *
 * @author apete
 */
public abstract class BasicArray<N extends Comparable<N>> implements Access1D<N>, Access1D.Aggregatable<N>, Access1D.Visitable<N>, Mutate1D,
        Mutate1D.Fillable<N>, Mutate1D.Modifiable<N>, Access1D.Collectable<N, Mutate1D> {

    public static final class Factory<N extends Comparable<N>> extends ArrayFactory<N, BasicArray<N>> {

        private static final long SPARSE_SEGMENTATION_LIMIT = PowerOf2.powerOfLong2(46);

        private final DenseArray.Factory<N> myDenseFactory;

        Factory(final org.ojalgo.array.DenseArray.Factory<N> denseFactory) {
            super();
            myDenseFactory = denseFactory;
        }

        @Override
        public AggregatorSet<N> aggregator() {
            return myDenseFactory.aggregator();
        }

        @Override
        public FunctionSet<N> function() {
            return myDenseFactory.function();
        }

        @Override
        public Scalar.Factory<N> scalar() {
            return myDenseFactory.scalar();
        }

        @Override
        long getCapacityLimit() {
            return Long.MAX_VALUE;
        }

        @Override
        BasicArray<N> makeStructuredZero(final long... structure) {

            long total = StructureAnyD.count(structure);

            DenseCapacityStrategy<N> strategy = this.strategy();

            if (total > SPARSE_SEGMENTATION_LIMIT) {

                return this.makeSegmented(structure);

            }
            if (strategy.isChunked(total)) {

                return new SparseArray<>(strategy.limit(total));

            }
            return strategy.make(total);
        }

        @Override
        BasicArray<N> makeToBeFilled(final long... structure) {

            long total = StructureAnyD.count(structure);

            DenseCapacityStrategy<N> strategy = this.strategy();

            if (strategy.isSegmented(total)) {

                return strategy.makeSegmented(total);

            }
            return strategy.make(total);
        }

        DenseCapacityStrategy<N> strategy() {
            return new DenseCapacityStrategy<>(myDenseFactory);
        }

    }

    private final ArrayFactory<N, ?> myFactory;

    @SuppressWarnings("unused")
    private BasicArray() {
        this(null);
    }

    protected BasicArray(final ArrayFactory<N, ?> factory) {
        super();
        myFactory = factory;
    }

    public N aggregateRange(final long first, final long limit, final Aggregator aggregator) {

        AggregatorFunction<N> visitor = aggregator.getFunction(myFactory.aggregator());

        this.visitRange(first, limit, visitor);

        return visitor.get();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BasicArray)) {
            return false;
        }
        BasicArray other = (BasicArray) obj;
        if (myFactory == null) {
            if (other.myFactory != null) {
                return false;
            }
        } else if (!myFactory.equals(other.myFactory)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        return prime * result + (myFactory == null ? 0 : myFactory.hashCode());
    }

    public long indexOfLargest() {
        return this.indexOfLargest(0L, this.count(), 1L);
    }

    public void modifyAll(final UnaryFunction<N> modifier) {
        this.modify(0L, this.count(), 1L, modifier);
    }

    public void modifyMatching(final Access1D<N> left, final BinaryFunction<N> function) {
        long limit = Math.min(left.count(), this.count());
        this.modify(0L, limit, 1L, left, function);
    }

    public void modifyMatching(final BinaryFunction<N> function, final Access1D<N> right) {
        long limit = Math.min(this.count(), right.count());
        this.modify(0L, limit, 1L, function, right);
    }

    public void modifyRange(final long first, final long limit, final UnaryFunction<N> modifier) {
        this.modify(first, limit, 1L, modifier);
    }

    public void supplyTo(final Mutate1D receiver) {
        long limit = Math.min(this.count(), receiver.count());
        for (long i = 0; i < limit; i++) {
            receiver.set(i, this.get(i));
        }
    }

    @Override
    public String toString() {
        return Access1D.toString(this);
    }

    public void visitAll(final VoidFunction<N> visitor) {
        this.visit(0L, this.count(), 1L, visitor);
    }

    public void visitRange(final long first, final long limit, final VoidFunction<N> visitor) {
        this.visit(first, limit, 1L, visitor);
    }

    protected abstract void exchange(long firstA, long firstB, long step, long count);

    protected abstract void fill(long first, long limit, long step, N value);

    protected abstract void fill(long first, long limit, long step, NullaryFunction<?> supplier);

    protected long indexOfLargest(final long first, final long limit, final long step) {
        return AMAX.invoke(this, first, limit, step);
    }

    protected abstract void modify(long first, long limit, long step, Access1D<N> left, BinaryFunction<N> function);

    protected abstract void modify(long first, long limit, long step, BinaryFunction<N> function, Access1D<N> right);

    protected abstract void modify(long first, long limit, long step, UnaryFunction<N> function);

    protected abstract void visit(long first, long limit, long step, VoidFunction<N> visitor);

    /**
     * A utility facade that conveniently/consistently presents the {@linkplain org.ojalgo.array.BasicArray}
     * as a one-dimensional array. Note that you will modify the actual array by accessing it through this
     * facade.
     */
    protected final Array1D<N> wrapInArray1D() {
        return new Array1D<>(this);
    }

    /**
     * A utility facade that conveniently/consistently presents the {@linkplain org.ojalgo.array.BasicArray}
     * as a two-dimensional array. Note that you will modify the actual array by accessing it through this
     * facade.
     */
    protected final Array2D<N> wrapInArray2D(final long structure) {
        return new Array2D<>(this, structure);
    }

    /**
     * A utility facade that conveniently/consistently presents the {@linkplain org.ojalgo.array.BasicArray}
     * as a multi-dimensional array. Note that you will modify the actual array by accessing it through this
     * facade.
     */
    protected final ArrayAnyD<N> wrapInArrayAnyD(final long[] structure) {
        return new ArrayAnyD<>(this, structure);
    }

    final ArrayFactory<N, ?> factory() {
        return myFactory;
    }

    /**
     * Primitive (double) elements
     */
    abstract boolean isPrimitive();

}

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

import org.ojalgo.array.operation.AMAX;
import org.ojalgo.array.operation.Exchange;
import org.ojalgo.array.operation.FillAll;
import org.ojalgo.array.operation.OperationBinary;
import org.ojalgo.array.operation.OperationUnary;
import org.ojalgo.array.operation.OperationVoid;
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
import org.ojalgo.structure.Factory1D;
import org.ojalgo.structure.Mutate1D;
import org.ojalgo.structure.StructureAnyD;
import org.ojalgo.type.math.MathType;

/**
 * <p>
 * A BasicArray is 1-dimensional, but designed to easily be extended or encapsulated, and then treated as
 * arbitrary-dimensional. It stores/handles (any subclass of) {@linkplain java.lang.Comparable} elements
 * depending on the subclass/implementation.
 * <p>
 * This abstract class defines a set of methods to access and modify array elements. It does not "know"
 * anything about linear algebra or similar.
 *
 * @author apete
 */
public abstract class BasicArray<N extends Comparable<N>> implements Access1D<N>, Access1D.Aggregatable<N>, Access1D.Visitable<N>, Mutate1D,
        Mutate1D.Fillable<N>, Mutate1D.Modifiable<N>, Access1D.Collectable<N, Mutate1D> {

    public static final class Factory<N extends Comparable<N>> extends BaseFactory<N, BasicArray<N>> {

        private final DenseArray.Factory<N, ?> myDenseFactory;
        private final GrowthStrategy myDenseStrategy;
        private final GrowthStrategy mySparseStrategy;

        Factory(final DenseArray.Factory<N, ?> denseFactory) {

            super(denseFactory.getMathType());

            myDenseFactory = denseFactory;

            myDenseStrategy = GrowthStrategy.from(denseFactory.getMathType()).segment(PowerOf2.powerOfLong2(23));
            mySparseStrategy = GrowthStrategy.from(denseFactory.getMathType()).segment(PowerOf2.powerOfLong2(46));
        }

        @Override
        public BasicArray<N> make(final int size) {
            return this.makeToBeFilled(size);
        }

        @Override
        public BasicArray<N> make(final long size) {
            return this.makeStructuredZero(size);
        }

        @Override
        long getCapacityLimit() {
            return Long.MAX_VALUE;
        }

        /**
         * Most likely sparse, and then also segmented.
         */
        BasicArray<N> makeStructuredZero(final long... structure) {

            long total = StructureAnyD.count(structure);

            if (mySparseStrategy.isSegmented(total)) {

                return SegmentedArray.newInstance(this, structure);

            } else if (mySparseStrategy.isChunked(total) && myDenseFactory instanceof PlainArray.Factory) {

                return new SparseArray<>((PlainArray.Factory<N, ?>) myDenseFactory, mySparseStrategy, Math.toIntExact(total));

            } else {

                return myDenseFactory.make(total);
            }
        }

        /**
         * Maybe segmented, but dense.
         */
        BasicArray<N> makeToBeFilled(final long... structure) {

            long total = StructureAnyD.count(structure);

            if (myDenseStrategy.isSegmented(total)) {
                return SegmentedArray.newInstance(this, total);
            } else {
                return myDenseFactory.make(total);
            }
        }

    }

    abstract static class BaseFactory<N extends Comparable<N>, A extends BasicArray<N>> implements Factory1D<A> {

        private final AggregatorSet<N> myAggregator;
        private final FunctionSet<N> myFunction;
        private final MathType myMathType;
        private final Scalar.Factory<N> myScalar;

        BaseFactory(final MathType mathType) {

            super();

            myMathType = mathType;

            myScalar = myMathType.getScalarFactory();
            myFunction = myMathType.getFunctionSet();
            myAggregator = myFunction.aggregator();
        }

        public final AggregatorSet<N> aggregator() {
            return myAggregator;
        }

        @Override
        public final FunctionSet<N> function() {
            return myFunction;
        }

        @Override
        public final MathType getMathType() {
            return myMathType;
        }

        @Override
        public final Scalar.Factory<N> scalar() {
            return myScalar;
        }

        /**
         * Max number of elements in this array.
         */
        abstract long getCapacityLimit();

        final long getElementSize() {
            return myMathType.getTotalMemory();
        }

    }

    private final BaseFactory<N, ?> myFactory;

    @SuppressWarnings("unused")
    private BasicArray() {
        this(null);
    }

    protected BasicArray(final BaseFactory<N, ?> factory) {
        super();
        myFactory = factory;
    }

    @Override
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
        BasicArray<?> other = (BasicArray<?>) obj;
        if (myFactory == null) {
            if (other.myFactory != null) {
                return false;
            }
        } else if (!myFactory.equals(other.myFactory)) {
            return false;
        }
        return true;
    }

    public final MathType getMathType() {
        return myFactory.getMathType();
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        return prime * result + (myFactory == null ? 0 : myFactory.hashCode());
    }

    @Override
    public long indexOfLargest() {
        return this.indexOfLargest(0L, this.count(), 1L);
    }

    public final boolean isPrimitive() {
        return myFactory.getMathType().isPrimitive();
    }

    @Override
    public void modifyAll(final UnaryFunction<N> modifier) {
        this.modify(0L, this.count(), 1L, modifier);
    }

    @Override
    public void modifyMatching(final Access1D<N> left, final BinaryFunction<N> function) {
        long limit = Math.min(left.count(), this.count());
        this.modify(0L, limit, 1L, left, function);
    }

    @Override
    public void modifyMatching(final BinaryFunction<N> function, final Access1D<N> right) {
        long limit = Math.min(this.count(), right.count());
        this.modify(0L, limit, 1L, function, right);
    }

    @Override
    public void modifyRange(final long first, final long limit, final UnaryFunction<N> modifier) {
        this.modify(first, limit, 1L, modifier);
    }

    @Override
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

    @Override
    public void visitAll(final VoidFunction<N> visitor) {
        this.visit(0L, this.count(), 1L, visitor);
    }

    @Override
    public void visitRange(final long first, final long limit, final VoidFunction<N> visitor) {
        this.visit(first, limit, 1L, visitor);
    }

    protected void exchange(final long firstA, final long firstB, final long step, final long count) {
        Exchange.exchange(this, firstA, firstB, step, count);
    }

    protected void fill(final long first, final long limit, final long step, final N value) {
        FillAll.fill(this, first, limit, step, value);
    }

    protected void fill(final long first, final long limit, final long step, final NullaryFunction<?> supplier) {
        FillAll.fill(this, first, limit, step, supplier);
    }

    protected long indexOfLargest(final long first, final long limit, final long step) {
        return AMAX.invoke(this, first, limit, step);
    }

    protected void modify(final long first, final long limit, final long step, final Access1D<N> left, final BinaryFunction<N> function) {
        OperationBinary.invoke(this, first, limit, step, left, function, this);
    }

    protected void modify(final long first, final long limit, final long step, final BinaryFunction<N> function, final Access1D<N> right) {
        OperationBinary.invoke(this, first, limit, step, this, function, right);
    }

    protected void modify(final long first, final long limit, final long step, final UnaryFunction<N> function) {
        OperationUnary.invoke(this, first, limit, step, this, function);
    }

    protected void visit(final long first, final long limit, final long step, final VoidFunction<N> visitor) {
        OperationVoid.invoke(this, first, limit, step, visitor);
    }

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

    final BaseFactory<N, ?> factory() {
        return myFactory;
    }

}

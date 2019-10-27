/*
 * Copyright 1997-2019 Optimatika
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
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

import org.ojalgo.ProgrammingError;
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
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.AccessAnyD;
import org.ojalgo.structure.FactoryAnyD;
import org.ojalgo.structure.MutateAnyD;
import org.ojalgo.structure.StructureAnyD;
import org.ojalgo.structure.TransformationAnyD;

/**
 * ArrayAnyD
 *
 * @author apete
 */
public final class ArrayAnyD<N extends Comparable<N>>
        implements AccessAnyD<N>, AccessAnyD.Visitable<N>, AccessAnyD.Aggregatable<N>, AccessAnyD.Sliceable<N>, AccessAnyD.Elements, AccessAnyD.IndexOf,
        StructureAnyD.ReducibleTo1D<Array1D<N>>, StructureAnyD.ReducibleTo2D<Array2D<N>>, MutateAnyD.ModifiableReceiver<N>, MutateAnyD.Mixable<N> {

    public static final class Factory<N extends Comparable<N>> implements FactoryAnyD.MayBeSparse<ArrayAnyD<N>, ArrayAnyD<N>, ArrayAnyD<N>> {

        private final BasicArray.Factory<N> myDelegate;

        Factory(final DenseArray.Factory<N> denseArray) {
            super();
            myDelegate = new BasicArray.Factory<>(denseArray);
        }

        /**
         * @deprecated v48 Use {@link ArrayAnyD#fillMatching(Access1D)}
         */
        @Deprecated
        public ArrayAnyD<N> copy(final AccessAnyD<?> source) {
            return myDelegate.copy(source).wrapInArrayAnyD(source.shape());
        }

        @Override
        public FunctionSet<N> function() {
            return myDelegate.function();
        }

        /**
         * @deprecated v48 Use {@link ArrayAnyD#fillAll(NullaryFunction)}
         */
        @Deprecated
        public ArrayAnyD<N> makeFilled(final long[] structure, final NullaryFunction<?> supplier) {
            return myDelegate.makeFilled(StructureAnyD.count(structure), supplier).wrapInArrayAnyD(structure);
        }

        public ArrayAnyD<N> makeSparse(final long... structure) {
            return myDelegate.makeStructuredZero(structure).wrapInArrayAnyD(structure);
        }

        @Override
        public ArrayAnyD<N> make(final long... structure) {
            return this.makeDense(structure);
        }

        @Override
        public Scalar.Factory<N> scalar() {
            return myDelegate.scalar();
        }

        public ArrayAnyD<N> makeDense(long... structure) {
            return myDelegate.makeToBeFilled(structure).wrapInArrayAnyD(structure);
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

    public static <N extends Comparable<N>> ArrayAnyD.Factory<N> factory(final DenseArray.Factory<N> denseArray) {
        return new ArrayAnyD.Factory<>(denseArray);
    }

    private final BasicArray<N> myDelegate;
    private final long[] myStructure;

    @SuppressWarnings("unused")
    private ArrayAnyD() {
        this(null, new long[0]);
    }

    ArrayAnyD(final BasicArray<N> delegate, final long[] structure) {

        super();

        myDelegate = delegate;
        myStructure = structure;
    }

    @Override
    public void add(final long index, final double addend) {
        myDelegate.add(index, addend);
    }

    @Override
    public void add(final long index, final float addend) {
        myDelegate.add(index, addend);
    }

    @Override
    public void add(final long index, final Comparable<?> addend) {
        myDelegate.add(index, addend);
    }

    @Override
    public void add(final long[] reference, final double addend) {
        myDelegate.add(StructureAnyD.index(myStructure, reference), addend);
    }

    @Override
    public void add(final long[] reference, final float addend) {
        myDelegate.add(StructureAnyD.index(myStructure, reference), addend);
    }

    @Override
    public void add(final long[] reference, final Comparable<?> addend) {
        myDelegate.add(StructureAnyD.index(myStructure, reference), addend);
    }

    @Override
    public N aggregateRange(final long first, final long limit, final Aggregator aggregator) {
        AggregatorFunction<N> visitor = aggregator.getFunction(myDelegate.factory().aggregator());
        this.visitRange(first, limit, visitor);
        return visitor.get();
    }

    @Override
    public N aggregateSet(final int dimension, final long dimensionalIndex, final Aggregator aggregator) {
        AggregatorFunction<N> visitor = aggregator.getFunction(myDelegate.factory().aggregator());
        this.visitSet(dimension, dimensionalIndex, visitor);
        return visitor.get();
    }

    @Override
    public N aggregateSet(final long[] initial, final int dimension, final Aggregator aggregator) {
        AggregatorFunction<N> visitor = aggregator.getFunction(myDelegate.factory().aggregator());
        this.visitSet(initial, dimension, visitor);
        return visitor.get();
    }

    /**
     * Flattens this abitrary dimensional array to a one dimensional array. The (internal/actual) array is not
     * copied, it is just accessed through a different adaptor.
     *
     * @deprecated v39 Not needed
     */
    @Deprecated
    public Array1D<N> asArray1D() {
        return myDelegate.wrapInArray1D();
    }

    public void clear() {
        myDelegate.reset();
    }

    @Override
    public long count() {
        return myDelegate.count();
    }

    @Override
    public long count(final int dimension) {
        return StructureAnyD.count(myStructure, dimension);
    }

    @Override
    public double doubleValue(final long index) {
        return myDelegate.doubleValue(index);
    }

    @Override
    public double doubleValue(final long[] ref) {
        return myDelegate.doubleValue(StructureAnyD.index(myStructure, ref));
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof ArrayAnyD) {
            final ArrayAnyD<N> tmpObj = (ArrayAnyD<N>) obj;
            return Arrays.equals(myStructure, tmpObj.shape()) && myDelegate.equals(tmpObj.getDelegate());
        } else {
            return super.equals(obj);
        }
    }

    @Override
    public void fillAll(final N value) {
        myDelegate.fill(0L, this.count(), 1L, value);
    }

    @Override
    public void fillAll(final NullaryFunction<?> supplier) {
        myDelegate.fill(0L, this.count(), 1L, supplier);
    }

    @Override
    public void fillOne(final long index, final Access1D<?> values, final long valueIndex) {
        myDelegate.fillOne(index, values, valueIndex);
    }

    @Override
    public void fillOne(final long index, final N value) {
        myDelegate.fillOne(index, value);
    }

    @Override
    public void fillOne(final long index, final NullaryFunction<?> supplier) {
        myDelegate.fillOne(index, supplier);
    }

    @Override
    public void fillOne(final long[] reference, final N value) {
        myDelegate.fillOne(StructureAnyD.index(myStructure, reference), value);
    }

    @Override
    public void fillOne(final long[] reference, final NullaryFunction<?> supplier) {
        myDelegate.fillOne(StructureAnyD.index(myStructure, reference), supplier);
    }

    @Override
    public void fillRange(final long first, final long limit, final N value) {
        myDelegate.fill(first, limit, 1L, value);
    }

    @Override
    public void fillRange(final long first, final long limit, final NullaryFunction<?> supplier) {
        myDelegate.fill(first, limit, 1L, supplier);
    }

    @Override
    public void fillSet(final int dimension, final long dimensionalIndex, final N value) {
        this.loop(dimension, dimensionalIndex, (f, l, s) -> myDelegate.fill(f, l, s, value));
    }

    @Override
    public void fillSet(final int dimension, final long dimensionalIndex, final NullaryFunction<?> supplier) {
        this.loop(dimension, dimensionalIndex, (f, l, s) -> myDelegate.fill(f, l, s, supplier));
    }

    @Override
    public void fillSet(final long[] initial, final int dimension, final N value) {
        this.loop(initial, dimension, (f, l, s) -> myDelegate.fill(f, l, s, value));
    }

    @Override
    public void fillSet(final long[] initial, final int dimension, final NullaryFunction<?> supplier) {
        this.loop(initial, dimension, (f, l, s) -> myDelegate.fill(f, l, s, supplier));
    }

    @Override
    public N get(final long index) {
        return myDelegate.get(index);
    }

    @Override
    public N get(final long[] ref) {
        return myDelegate.get(StructureAnyD.index(myStructure, ref));
    }

    @Override
    public int hashCode() {
        return myDelegate.hashCode();
    }

    @Override
    public long indexOfLargest() {
        return myDelegate.indexOfLargest();
    }

    @Override
    public long indexOfLargestInRange(final long first, final long limit) {
        return myDelegate.indexOfLargestInRange(first, limit);
    }

    @Override
    public boolean isAbsolute(final long index) {
        return myDelegate.isAbsolute(index);
    }

    /**
     * @see Scalar#isAbsolute()
     */
    @Override
    public boolean isAbsolute(final long[] reference) {
        return myDelegate.isAbsolute(StructureAnyD.index(myStructure, reference));
    }

    @Override
    public boolean isSmall(final long index, final double comparedTo) {
        return myDelegate.isSmall(index, comparedTo);
    }

    @Override
    public boolean isSmall(final long[] reference, final double comparedTo) {
        return myDelegate.isSmall(StructureAnyD.index(myStructure, reference), comparedTo);
    }

    @Override
    public double mix(final long[] reference, final BinaryFunction<N> mixer, final double addend) {
        ProgrammingError.throwIfNull(mixer);
        synchronized (myDelegate) {
            final double oldValue = this.doubleValue(reference);
            final double newValue = mixer.invoke(oldValue, addend);
            this.set(reference, newValue);
            return newValue;
        }
    }

    @Override
    public N mix(final long[] reference, final BinaryFunction<N> mixer, final N addend) {
        ProgrammingError.throwIfNull(mixer);
        synchronized (myDelegate) {
            final N oldValue = this.get(reference);
            final N newValue = mixer.invoke(oldValue, addend);
            this.set(reference, newValue);
            return newValue;
        }
    }

    @Override
    public void modifyAll(final UnaryFunction<N> modifier) {
        myDelegate.modify(0L, this.count(), 1L, modifier);
    }

    @Override
    public void modifyAny(final TransformationAnyD<N> modifier) {
        modifier.transform(this);
    }

    @Override
    public void modifyMatching(final Access1D<N> left, final BinaryFunction<N> function) {
        myDelegate.modify(0L, this.count(), 1L, left, function);
    }

    @Override
    public void modifyMatching(final BinaryFunction<N> function, final Access1D<N> right) {
        myDelegate.modify(0L, this.count(), 1L, function, right);
    }

    @Override
    public void modifyOne(final long index, final UnaryFunction<N> modifier) {
        myDelegate.modifyOne(index, modifier);
    }

    @Override
    public void modifyOne(final long[] reference, final UnaryFunction<N> modifier) {
        myDelegate.modifyOne(StructureAnyD.index(myStructure, reference), modifier);
    }

    @Override
    public void modifyRange(final long first, final long limit, final UnaryFunction<N> modifier) {
        myDelegate.modify(first, limit, 1L, modifier);
    }

    @Override
    public void modifySet(final int dimension, final long dimensionalIndex, final UnaryFunction<N> modifier) {
        this.loop(dimension, dimensionalIndex, (f, l, s) -> myDelegate.modify(f, l, s, modifier));
    }

    @Override
    public void modifySet(final long[] initial, final int dimension, final UnaryFunction<N> modifier) {
        this.loop(initial, dimension, (f, l, s) -> myDelegate.modify(f, l, s, modifier));
    }

    @Override
    public int rank() {
        return myStructure.length;
    }

    @Override
    public Array1D<N> reduce(final int dimension, final Aggregator aggregator) {
        final long reduceToCount = StructureAnyD.count(myStructure, dimension);
        Array1D<N> retVal = myDelegate.factory().makeZero(reduceToCount).wrapInArray1D();
        this.reduce(dimension, aggregator, retVal);
        return retVal;
    }

    @Override
    public Array2D<N> reduce(final int rowDimension, final int columnDimension, final Aggregator aggregator) {

        long[] structure = this.shape();

        long numberOfRows = structure[rowDimension];
        long numberOfColumns = structure[columnDimension];

        AggregatorFunction<N> visitor = aggregator.getFunction(myDelegate.factory().aggregator());

        final boolean primitive = myDelegate.isPrimitive();

        Array2D<N> retVal = myDelegate.factory().makeZero(numberOfRows * numberOfColumns).wrapInArray2D(numberOfRows);

        for (long j = 0L; j < numberOfColumns; j++) {
            final long colInd = j;

            for (long i = 0L; i < numberOfRows; i++) {
                final long rowInd = i;

                visitor.reset();
                this.loop(reference -> (reference[rowDimension] == rowInd) && (reference[columnDimension] == colInd), index -> this.visitOne(index, visitor));
                if (primitive) {
                    retVal.set(rowInd, colInd, visitor.doubleValue());
                } else {
                    retVal.set(rowInd, colInd, visitor.get());
                }
            }
        }

        return retVal;
    }

    @Override
    public void set(final long index, final double value) {
        myDelegate.set(index, value);
    }

    @Override
    public void set(final long index, final float value) {
        myDelegate.set(index, value);
    }

    @Override
    public void set(final long index, final Comparable<?> value) {
        myDelegate.set(index, value);
    }

    @Override
    public void set(final long[] reference, final double value) {
        myDelegate.set(StructureAnyD.index(myStructure, reference), value);
    }

    @Override
    public void set(final long[] reference, final float value) {
        myDelegate.set(StructureAnyD.index(myStructure, reference), value);
    }

    @Override
    public void set(final long[] reference, final Comparable<?> value) {
        myDelegate.set(StructureAnyD.index(myStructure, reference), value);
    }

    @Override
    public long[] shape() {
        return myStructure;
    }

    @Override
    public Array1D<N> sliceRange(final long first, final long limit) {
        return myDelegate.wrapInArray1D().sliceRange(first, limit);
    }

    @Override
    public Array1D<N> sliceSet(final long[] initial, final int dimension) {

        AtomicLong first = new AtomicLong();
        AtomicLong limit = new AtomicLong();
        AtomicLong step = new AtomicLong();

        this.loop(initial, dimension, (f, l, s) -> {
            first.set(f);
            limit.set(l);
            step.set(s);
        });

        return new Array1D<>(myDelegate, first.longValue(), limit.longValue(), step.longValue());
    }

    @Override
    public String toString() {

        final StringBuilder retVal = new StringBuilder();

        retVal.append('<');
        retVal.append(myStructure[0]);
        for (int i = 1; i < myStructure.length; i++) {
            retVal.append('x');
            retVal.append(myStructure[i]);
        }
        retVal.append('>');

        final int tmpLength = (int) this.count();
        if ((tmpLength >= 1) && (tmpLength <= 100)) {
            retVal.append(' ');
            retVal.append(myDelegate.toString());
        }

        return retVal.toString();
    }

    @Override
    public void visitAll(final VoidFunction<N> visitor) {
        myDelegate.visit(0L, this.count(), 1L, visitor);
    }

    @Override
    public void visitOne(final long index, final VoidFunction<N> visitor) {
        myDelegate.visitOne(index, visitor);
    }

    @Override
    public void visitOne(final long[] reference, final VoidFunction<N> visitor) {
        myDelegate.visitOne(StructureAnyD.index(myStructure, reference), visitor);
    }

    @Override
    public void visitRange(final long first, final long limit, final VoidFunction<N> visitor) {
        myDelegate.visit(first, limit, 1L, visitor);
    }

    @Override
    public void visitSet(final int dimension, final long dimensionalIndex, final VoidFunction<N> visitor) {
        this.loop(dimension, dimensionalIndex, (f, l, s) -> myDelegate.visit(f, l, s, visitor));
    }

    @Override
    public void visitSet(final long[] initial, final int dimension, final VoidFunction<N> visitor) {
        this.loop(initial, dimension, (f, l, s) -> myDelegate.visit(f, l, s, visitor));
    }

    BasicArray<N> getDelegate() {
        return myDelegate;
    }

}

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
import org.ojalgo.scalar.Quadruple;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.AccessAnyD;
import org.ojalgo.structure.FactoryAnyD;
import org.ojalgo.structure.MutateAnyD;
import org.ojalgo.structure.StructureAnyD;
import org.ojalgo.structure.TransformationAnyD;
import org.ojalgo.tensor.TensorFactoryAnyD;
import org.ojalgo.type.math.MathType;

/**
 * ArrayAnyD
 *
 * @author apete
 */
public final class ArrayAnyD<N extends Comparable<N>> implements AccessAnyD.Visitable<N>, AccessAnyD.Aggregatable<N>, AccessAnyD.Sliceable<N>,
        StructureAnyD.ReducibleTo1D<Array1D<N>>, StructureAnyD.ReducibleTo2D<Array2D<N>>, AccessAnyD.Collectable<N, MutateAnyD>,
        MutateAnyD.ModifiableReceiver<N>, MutateAnyD.Mixable<N>, StructureAnyD.Reshapable {

    public static final class Factory<N extends Comparable<N>>
            implements FactoryAnyD.Dense<ArrayAnyD<N>>, FactoryAnyD.MayBeSparse<ArrayAnyD<N>, ArrayAnyD<N>, ArrayAnyD<N>> {

        private final BasicArray.Factory<N> myDelegate;

        Factory(final DenseArray.Factory<N> denseArray) {
            super();
            myDelegate = new BasicArray.Factory<>(denseArray);
        }

        @Override
        public ArrayAnyD<N> copy(final AccessAnyD<?> source) {
            return myDelegate.copy(source).wrapInArrayAnyD(source.shape());
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
        public ArrayAnyD<N> make(final long... structure) {
            return this.makeDense(structure);
        }

        @Override
        public ArrayAnyD<N> makeDense(final long... structure) {
            return myDelegate.makeToBeFilled(structure).wrapInArrayAnyD(structure);
        }

        @Override
        public ArrayAnyD<N> makeFilled(final long[] structure, final NullaryFunction<?> supplier) {

            BasicArray<N> toBeFilled = myDelegate.makeToBeFilled(structure);

            toBeFilled.fillAll(supplier);

            return toBeFilled.wrapInArrayAnyD(structure);
        }

        @Override
        public ArrayAnyD<N> makeSparse(final long... structure) {
            return myDelegate.makeStructuredZero(structure).wrapInArrayAnyD(structure);
        }

        @Override
        public Scalar.Factory<N> scalar() {
            return myDelegate.scalar();
        }

        public TensorFactoryAnyD<N, ArrayAnyD<N>> tensor() {
            return TensorFactoryAnyD.of(this);
        }

    }

    public static final Factory<Double> R032 = ArrayAnyD.factory(ArrayR032.FACTORY);
    public static final Factory<Double> R064 = ArrayAnyD.factory(ArrayR064.FACTORY);
    public static final Factory<Quadruple> R128 = ArrayAnyD.factory(ArrayR128.FACTORY);
    public static final Factory<BigDecimal> R256 = ArrayAnyD.factory(ArrayR256.FACTORY);
    /**
     * @deprecated v52 Use {@link #R256} instead
     */
    @Deprecated
    public static final Factory<BigDecimal> BIG = R256;
    public static final Factory<ComplexNumber> C128 = ArrayAnyD.factory(ArrayC128.FACTORY);
    /**
     * @deprecated v52 Use {@link #C128} instead
     */
    @Deprecated
    public static final Factory<ComplexNumber> COMPLEX = C128;
    /**
     * @deprecated v52 Use {@link #factory(DenseArray.Factory)} instead
     */
    @Deprecated
    public static final Factory<Double> DIRECT32 = ArrayAnyD.factory(BufferArray.DIRECT32);
    /**
     * @deprecated v52 Use {@link #factory(DenseArray.Factory)} instead
     */
    @Deprecated
    public static final Factory<Double> DIRECT64 = ArrayAnyD.factory(BufferArray.DIRECT64);
    public static final Factory<Quaternion> H256 = ArrayAnyD.factory(ArrayH256.FACTORY);
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
    public static final Factory<RationalNumber> Q128 = ArrayAnyD.factory(ArrayQ128.FACTORY);
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
    public static final Factory<Double> Z008 = ArrayAnyD.factory(ArrayZ008.FACTORY);
    public static final Factory<Double> Z016 = ArrayAnyD.factory(ArrayZ016.FACTORY);
    public static final Factory<Double> Z032 = ArrayAnyD.factory(ArrayZ032.FACTORY);
    public static final Factory<Double> Z064 = ArrayAnyD.factory(ArrayZ064.FACTORY);

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
    public void add(final long index, final byte addend) {
        myDelegate.add(index, addend);
    }

    @Override
    public void add(final long index, final Comparable<?> addend) {
        myDelegate.add(index, addend);
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
    public void add(final long index, final int addend) {
        myDelegate.add(index, addend);
    }

    @Override
    public void add(final long index, final long addend) {
        myDelegate.add(index, addend);
    }

    @Override
    public void add(final long index, final short addend) {
        myDelegate.add(index, addend);
    }

    @Override
    public void add(final long[] reference, final byte addend) {
        myDelegate.add(StructureAnyD.index(myStructure, reference), addend);
    }

    @Override
    public void add(final long[] reference, final Comparable<?> addend) {
        myDelegate.add(StructureAnyD.index(myStructure, reference), addend);
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
    public void add(final long[] reference, final int addend) {
        myDelegate.add(StructureAnyD.index(myStructure, reference), addend);
    }

    @Override
    public void add(final long[] reference, final long addend) {
        myDelegate.add(StructureAnyD.index(myStructure, reference), addend);
    }

    @Override
    public void add(final long[] reference, final short addend) {
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

    @Override
    public byte byteValue(final int index) {
        return myDelegate.byteValue(index);
    }

    @Override
    public byte byteValue(final long index) {
        return myDelegate.byteValue(index);
    }

    @Override
    public byte byteValue(final long... ref) {
        return myDelegate.byteValue(StructureAnyD.index(myStructure, ref));
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
    public double doubleValue(final int index) {
        return myDelegate.doubleValue(index);
    }

    @Override
    public double doubleValue(final long index) {
        return myDelegate.doubleValue(index);
    }

    @Override
    public double doubleValue(final long... ref) {
        return myDelegate.doubleValue(StructureAnyD.index(myStructure, ref));
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ArrayAnyD)) {
            return false;
        }
        ArrayAnyD<?> other = (ArrayAnyD<?>) obj;
        if (!Arrays.equals(myStructure, other.myStructure)) {
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
    public ArrayAnyD<N> expand(final int rank) {

        int r = Math.max(this.rank(), rank);
        long[] shape = new long[r];

        for (int d = 0; d < r; d++) {
            shape[d] = this.count(d);
        }

        return this.reshape(shape);
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

    /**
     * Flattens this abitrary dimensional array to a one dimensional array. The (internal/actual) array is not
     * copied, it is just accessed through a different adaptor.
     *
     * @see org.ojalgo.structure.StructureAnyD.Reshapable#flatten()
     */
    @Override
    public Array1D<N> flatten() {
        return myDelegate.wrapInArray1D();
    }

    @Override
    public float floatValue(final int index) {
        return myDelegate.floatValue(index);
    }

    @Override
    public float floatValue(final long index) {
        return myDelegate.floatValue(index);
    }

    @Override
    public float floatValue(final long... ref) {
        return myDelegate.floatValue(StructureAnyD.index(myStructure, ref));
    }

    @Override
    public N get(final long index) {
        return myDelegate.get(index);
    }

    @Override
    public N get(final long... ref) {
        return myDelegate.get(StructureAnyD.index(myStructure, ref));
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + (myDelegate == null ? 0 : myDelegate.hashCode());
        return prime * result + Arrays.hashCode(myStructure);
    }

    @Override
    public long indexOfLargest() {
        return myDelegate.indexOfLargest();
    }

    @Override
    public int intValue(final int index) {
        return myDelegate.intValue(index);
    }

    @Override
    public int intValue(final long index) {
        return myDelegate.intValue(index);
    }

    @Override
    public int intValue(final long... ref) {
        return myDelegate.intValue(StructureAnyD.index(myStructure, ref));
    }

    @Override
    public long longValue(final int index) {
        return myDelegate.longValue(index);
    }

    @Override
    public long longValue(final long index) {
        return myDelegate.longValue(index);
    }

    @Override
    public long longValue(final long... ref) {
        return myDelegate.longValue(StructureAnyD.index(myStructure, ref));
    }

    @Override
    public double mix(final long[] reference, final BinaryFunction<N> mixer, final double addend) {
        ProgrammingError.throwIfNull(mixer);
        synchronized (myDelegate) {
            double oldValue = this.doubleValue(reference);
            double newValue = mixer.invoke(oldValue, addend);
            this.set(reference, newValue);
            return newValue;
        }
    }

    @Override
    public N mix(final long[] reference, final BinaryFunction<N> mixer, final N addend) {
        ProgrammingError.throwIfNull(mixer);
        synchronized (myDelegate) {
            N oldValue = this.get(reference);
            N newValue = mixer.invoke(oldValue, addend);
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
        long reduceToCount = StructureAnyD.count(myStructure, dimension);
        Array1D<N> retVal = myDelegate.factory().make(reduceToCount).wrapInArray1D();
        this.reduce(dimension, aggregator, retVal);
        return retVal;
    }

    @Override
    public Array2D<N> reduce(final int rowDim, final int colDim, final Aggregator aggregator) {

        long[] structure = this.shape();

        long nbRows = structure[rowDim];
        long nbCols = structure[colDim];

        AggregatorFunction<N> visitor = aggregator.getFunction(myDelegate.factory().aggregator());

        boolean primitive = myDelegate.isPrimitive();

        Array2D<N> retVal = myDelegate.factory().make(nbRows * nbCols).wrapInArray2D(nbRows);

        for (long j = 0L; j < nbCols; j++) {
            final long col = j;

            for (long i = 0L; i < nbRows; i++) {
                final long row = i;

                visitor.reset();
                this.loopReferences(reference -> reference[rowDim] == row && reference[colDim] == col, reference -> this.visitOne(reference, visitor));
                if (primitive) {
                    retVal.set(row, col, visitor.doubleValue());
                } else {
                    retVal.set(row, col, visitor.get());
                }
            }
        }

        return retVal;
    }

    @Override
    public void reset() {
        myDelegate.reset();
    }

    @Override
    public ArrayAnyD<N> reshape(final long... shape) {
        if (StructureAnyD.count(shape) != this.count()) {
            throw new IllegalArgumentException();
        }
        return myDelegate.wrapInArrayAnyD(shape);
    }

    @Override
    public void set(final int index, final byte value) {
        myDelegate.set(index, value);
    }

    @Override
    public void set(final int index, final double value) {
        myDelegate.set(index, value);
    }

    @Override
    public void set(final int index, final float value) {
        myDelegate.set(index, value);
    }

    @Override
    public void set(final int index, final int value) {
        myDelegate.set(index, value);
    }

    @Override
    public void set(final int index, final long value) {
        myDelegate.set(index, value);
    }

    @Override
    public void set(final int index, final short value) {
        myDelegate.set(index, value);
    }

    @Override
    public void set(final long index, final byte value) {
        myDelegate.set(index, value);
    }

    @Override
    public void set(final long index, final Comparable<?> value) {
        myDelegate.set(index, value);
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
    public void set(final long index, final int value) {
        myDelegate.set(index, value);
    }

    @Override
    public void set(final long index, final long value) {
        myDelegate.set(index, value);
    }

    @Override
    public void set(final long index, final short value) {
        myDelegate.set(index, value);
    }

    @Override
    public void set(final long[] reference, final byte value) {
        myDelegate.set(StructureAnyD.index(myStructure, reference), value);
    }

    @Override
    public void set(final long[] reference, final Comparable<?> value) {
        myDelegate.set(StructureAnyD.index(myStructure, reference), value);
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
    public void set(final long[] reference, final int value) {
        myDelegate.set(StructureAnyD.index(myStructure, reference), value);
    }

    @Override
    public void set(final long[] reference, final long value) {
        myDelegate.set(StructureAnyD.index(myStructure, reference), value);
    }

    @Override
    public void set(final long[] reference, final short value) {
        myDelegate.set(StructureAnyD.index(myStructure, reference), value);
    }

    @Override
    public long[] shape() {
        return myStructure;
    }

    @Override
    public short shortValue(final int index) {
        return myDelegate.shortValue(index);
    }

    @Override
    public short shortValue(final long index) {
        return myDelegate.shortValue(index);
    }

    @Override
    public short shortValue(final long... ref) {
        return myDelegate.shortValue(StructureAnyD.index(myStructure, ref));
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
    public ArrayAnyD<N> squeeze() {

        long[] oldShape = this.shape();

        int notOne = 0;
        for (int i = 0; i < oldShape.length; i++) {
            if (oldShape[i] > 1) {
                notOne++;
            }
        }

        if (notOne == oldShape.length) {
            return this;
        }
        long[] shape = new long[notOne];

        for (int i = 0, d = 0; i < oldShape.length; i++) {
            long length = oldShape[i];
            if (length > 1) {
                shape[d++] = length;
            }
        }

        return this.reshape(shape);
    }

    @Override
    public void supplyTo(final MutateAnyD receiver) {
        myDelegate.supplyTo(receiver);
    }

    @Override
    public String toString() {

        StringBuilder retVal = new StringBuilder();

        retVal.append('<');
        retVal.append(myStructure[0]);
        for (int i = 1; i < myStructure.length; i++) {
            retVal.append('x');
            retVal.append(myStructure[i]);
        }
        retVal.append('>');

        int tmpLength = (int) this.count();
        if (tmpLength >= 1 && tmpLength <= 100) {
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

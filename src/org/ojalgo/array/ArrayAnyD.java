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
package org.ojalgo.array;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Arrays;

import org.ojalgo.ProgrammingError;
import org.ojalgo.access.Access1D;
import org.ojalgo.access.AccessAnyD;
import org.ojalgo.access.FactoryAnyD;
import org.ojalgo.access.MutateAnyD;
import org.ojalgo.access.StructureAnyD;
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

/**
 * ArrayAnyD
 *
 * @author apete
 */
public final class ArrayAnyD<N extends Number> implements AccessAnyD<N>, AccessAnyD.Elements, AccessAnyD.IndexOf, AccessAnyD.Visitable<N>,
        AccessAnyD.Aggregatable<N>, StructureAnyD.Reducible<Array1D<N>>, AccessAnyD.Sliceable<N>, MutateAnyD, MutateAnyD.Fillable<N>, MutateAnyD.Modifiable<N>,
        MutateAnyD.BiModifiable<N>, MutateAnyD.Mixable<N>, Serializable {

    public static final class Factory<N extends Number> implements FactoryAnyD<ArrayAnyD<N>> {

        private final BasicArray.Factory<N> myDelegate;

        Factory(final DenseArray.Factory<N> denseArray) {
            super();
            myDelegate = BasicArray.factory(denseArray);
        }

        public final ArrayAnyD<N> copy(final AccessAnyD<?> source) {
            return myDelegate.copy(source).wrapInArrayAnyD(source.shape());
        }

        @Override
        public final FunctionSet<N> function() {
            return myDelegate.function();
        }

        public final ArrayAnyD<N> makeFilled(final long[] structure, final NullaryFunction<?> supplier) {
            return myDelegate.makeFilled(StructureAnyD.count(structure), supplier).wrapInArrayAnyD(structure);
        }

        public final ArrayAnyD<N> makeZero(final long... structure) {
            return myDelegate.makeStructuredZero(structure).wrapInArrayAnyD(structure);
        }

        @Override
        public final Scalar.Factory<N> scalar() {
            return myDelegate.scalar();
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

    public static <N extends Number> ArrayAnyD.Factory<N> factory(final DenseArray.Factory<N> denseArray) {
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

    public void add(final long index, final double addend) {
        myDelegate.add(index, addend);
    }

    public void add(final long index, final Number addend) {
        myDelegate.add(index, addend);
    }

    public void add(final long[] reference, final double addend) {
        myDelegate.add(StructureAnyD.index(myStructure, reference), addend);
    }

    public void add(final long[] reference, final Number addend) {
        myDelegate.add(StructureAnyD.index(myStructure, reference), addend);
    }

    public Number aggregate(int dimension, long dimensionalIndex, Aggregator aggregator) {
        AggregatorFunction<N> visitor = aggregator.getFunction(myDelegate.factory().aggregator());
        this.visit(dimension, dimensionalIndex, visitor);
        return visitor.get();
    }

    public N aggregateRange(long first, long limit, Aggregator aggregator) {
        AggregatorFunction<N> visitor = aggregator.getFunction(myDelegate.factory().aggregator());
        this.visitRange(first, limit, visitor);
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

    public long count() {
        return myDelegate.count();
    }

    public long count(final int dimension) {
        return StructureAnyD.count(myStructure, dimension);
    }

    public double doubleValue(final long index) {
        return myDelegate.doubleValue(index);
    }

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

    public void fillAll(final N value) {
        myDelegate.fill(0L, this.count(), 1L, value);
    }

    public void fillAll(final NullaryFunction<N> supplier) {
        myDelegate.fill(0L, this.count(), 1L, supplier);
    }

    public void fillOne(final long index, final Access1D<?> values, final long valueIndex) {
        myDelegate.fillOne(index, values, valueIndex);
    }

    public void fillOne(final long index, final N value) {
        myDelegate.fillOne(index, value);
    }

    public void fillOne(final long index, final NullaryFunction<N> supplier) {
        myDelegate.fillOne(index, supplier);
    }

    public void fillOne(final long[] reference, final N value) {
        myDelegate.fillOne(StructureAnyD.index(myStructure, reference), value);
    }

    public void fillOne(final long[] reference, final NullaryFunction<N> supplier) {
        myDelegate.fillOne(StructureAnyD.index(myStructure, reference), supplier);
    }

    public void fillRange(final long first, final long limit, final N value) {
        myDelegate.fill(first, limit, 1L, value);
    }

    public void fillRange(final long first, final long limit, final NullaryFunction<N> supplier) {
        myDelegate.fill(first, limit, 1L, supplier);
    }

    public void fillSet(final long[] first, final int dimension, final N number) {

        final long tmpCount = StructureAnyD.count(myStructure, dimension) - first[dimension];

        final long tmpFirst = StructureAnyD.index(myStructure, first);
        final long tmpStep = StructureAnyD.step(myStructure, dimension);
        final long tmpLimit = tmpFirst + (tmpStep * tmpCount);

        myDelegate.fill(tmpFirst, tmpLimit, tmpStep, number);
    }

    public N get(final long index) {
        return myDelegate.get(index);
    }

    public N get(final long[] ref) {
        return myDelegate.get(StructureAnyD.index(myStructure, ref));
    }

    @Override
    public int hashCode() {
        return myDelegate.hashCode();
    }

    public long indexOfLargest() {
        return myDelegate.indexOfLargest();
    }

    public long indexOfLargestInRange(final long first, final long limit) {
        return myDelegate.indexOfLargestInRange(first, limit);
    }

    public boolean isAbsolute(final long index) {
        return myDelegate.isAbsolute(index);
    }

    /**
     * @see Scalar#isAbsolute()
     */
    public boolean isAbsolute(final long[] reference) {
        return myDelegate.isAbsolute(StructureAnyD.index(myStructure, reference));
    }

    public boolean isSmall(final long index, final double comparedTo) {
        return myDelegate.isSmall(index, comparedTo);
    }

    public boolean isSmall(final long[] reference, final double comparedTo) {
        return myDelegate.isSmall(StructureAnyD.index(myStructure, reference), comparedTo);
    }

    public double mix(final long[] reference, final BinaryFunction<N> mixer, final double addend) {
        ProgrammingError.throwIfNull(mixer);
        synchronized (myDelegate) {
            final double oldValue = this.doubleValue(reference);
            final double newValue = mixer.invoke(oldValue, addend);
            this.set(reference, newValue);
            return newValue;
        }
    }

    public N mix(final long[] reference, final BinaryFunction<N> mixer, final N addend) {
        ProgrammingError.throwIfNull(mixer);
        synchronized (myDelegate) {
            final N oldValue = this.get(reference);
            final N newValue = mixer.invoke(oldValue, addend);
            this.set(reference, newValue);
            return newValue;
        }
    }

    public void modifyAll(final UnaryFunction<N> modifier) {
        myDelegate.modify(0L, this.count(), 1L, modifier);
    }

    public void modifyMatching(final Access1D<N> left, final BinaryFunction<N> function) {
        myDelegate.modify(0L, this.count(), 1L, left, function);
    }

    public void modifyMatching(final BinaryFunction<N> function, final Access1D<N> right) {
        myDelegate.modify(0L, this.count(), 1L, function, right);
    }

    public void modifyOne(final long index, final UnaryFunction<N> modifier) {
        myDelegate.modifyOne(index, modifier);
    }

    public void modifyOne(final long[] reference, final UnaryFunction<N> modifier) {
        myDelegate.modifyOne(StructureAnyD.index(myStructure, reference), modifier);
    }

    public void modifyRange(final long first, final long limit, final UnaryFunction<N> modifier) {
        myDelegate.modify(first, limit, 1L, modifier);
    }

    public void modifySet(final long[] first, final int dimension, final UnaryFunction<N> function) {

        final long tmpCount = StructureAnyD.count(myStructure, dimension) - first[dimension];

        final long tmpFirst = StructureAnyD.index(myStructure, first);
        final long tmpStep = StructureAnyD.step(myStructure, dimension);
        final long tmpLimit = tmpFirst + (tmpStep * tmpCount);

        myDelegate.modify(tmpFirst, tmpLimit, tmpStep, function);
    }

    public int rank() {
        return myStructure.length;
    }

    public Array1D<N> reduce(int dimension, Aggregator aggregator) {
        Array1D<N> retVal = myDelegate.factory().makeZero(StructureAnyD.count(myStructure, dimension)).wrapInArray1D();
        this.reduce(dimension, aggregator, retVal);
        return retVal;
    }

    public void set(final long index, final double value) {
        myDelegate.set(index, value);
    }

    public void set(final long index, final Number value) {
        myDelegate.set(index, value);
    }

    public void set(final long[] reference, final double value) {
        myDelegate.set(StructureAnyD.index(myStructure, reference), value);
    }

    public void set(final long[] reference, final Number value) {
        myDelegate.set(StructureAnyD.index(myStructure, reference), value);
    }

    public long[] shape() {
        return myStructure;
    }

    public Array1D<N> slice(final long[] first, final int dimension) {

        final long tmpCount = StructureAnyD.count(myStructure, dimension) - first[dimension];

        final long tmpFirst = StructureAnyD.index(myStructure, first);
        final long tmpStep = StructureAnyD.step(myStructure, dimension);
        final long tmpLimit = tmpFirst + (tmpStep * tmpCount);

        return new Array1D<>(myDelegate, tmpFirst, tmpLimit, tmpStep);
    }

    public Array1D<N> sliceRange(final long first, final long limit) {
        return myDelegate.wrapInArray1D().sliceRange(first, limit);
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

    public void visit(int dimension, long dimensionalIndex, VoidFunction<N> visitor) {

        long outerStep = StructureAnyD.step(myStructure, dimension + 1);
        long innerStep = StructureAnyD.step(myStructure, dimension);

        final long totalCount = this.count();
        final long dimenCount = this.count(dimension);
        final long dimenRange = innerStep * dimenCount;

        for (long first = innerStep * dimensionalIndex; first < totalCount; first += outerStep) {
            myDelegate.visit(first, first + dimenRange, innerStep, visitor);
        }
    }

    public void visitAll(final VoidFunction<N> visitor) {
        myDelegate.visit(0L, this.count(), 1L, visitor);
    }

    public void visitOne(final long index, final VoidFunction<N> visitor) {
        myDelegate.visitOne(index, visitor);
    }

    public void visitOne(final long[] reference, final VoidFunction<N> visitor) {
        myDelegate.visitOne(StructureAnyD.index(myStructure, reference), visitor);
    }

    public void visitRange(final long first, final long limit, final VoidFunction<N> visitor) {
        myDelegate.visit(first, limit, 1L, visitor);
    }

    public void visitSet(final long[] first, final int dimension, final VoidFunction<N> visitor) {

        final long tmpCount = StructureAnyD.count(myStructure, dimension) - first[dimension];

        final long tmpFirst = StructureAnyD.index(myStructure, first);
        final long tmpStep = StructureAnyD.step(myStructure, dimension);
        final long tmpLimit = tmpFirst + (tmpStep * tmpCount);

        myDelegate.visit(tmpFirst, tmpLimit, tmpStep, visitor);
    }

    final BasicArray<N> getDelegate() {
        return myDelegate;
    }

}

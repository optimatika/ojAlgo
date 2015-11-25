/*
 * Copyright 1997-2015 Optimatika (www.optimatika.se)
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
import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;
import java.util.RandomAccess;
import java.util.Spliterator;
import java.util.Spliterators;

import org.ojalgo.access.Access1D;
import org.ojalgo.array.BasicArray.BasicFactory;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.scalar.Scalar;

/**
 * Array1D
 *
 * @author apete
 */
public final class Array1D<N extends Number> extends AbstractList<N> implements Access1D<N>, Access1D.Elements, Access1D.IndexOf, Access1D.Fillable<N>,
        Access1D.Modifiable<N>, Access1D.Visitable<N>, Access1D.Sliceable<N>, RandomAccess, Serializable {

    public static abstract class Factory<N extends Number> implements Access1D.Factory<Array1D<N>> {

        public Array1D<N> copy(final Access1D<?> source) {

            final long tmpCount = source.count();

            final BasicArray<N> tmpDelegate = this.delegate().makeToBeFilled(tmpCount);

            for (long i = 0L; i < tmpCount; i++) {
                tmpDelegate.set(i, source.get(i));
            }

            return tmpDelegate.asArray1D();
        }

        public Array1D<N> copy(final double... source) {

            final int tmpLength = source.length;

            final BasicArray<N> tmpDelegate = this.delegate().makeToBeFilled(tmpLength);

            for (int i = 0; i < tmpLength; i++) {
                tmpDelegate.set(i, source[i]);
            }

            return tmpDelegate.asArray1D();
        }

        public final Array1D<N> copy(final List<? extends Number> source) {

            final int tmpSize = source.size();

            final BasicArray<N> tmpDelegate = this.delegate().makeToBeFilled(tmpSize);

            for (int i = 0; i < tmpSize; i++) {
                tmpDelegate.set(i, source.get(i));
            }

            return tmpDelegate.asArray1D();
        }

        public final Array1D<N> copy(final Number... source) {

            final int tmpLength = source.length;

            final BasicArray<N> tmpDelegate = this.delegate().makeToBeFilled(tmpLength);

            for (int i = 0; i < tmpLength; i++) {
                tmpDelegate.set(i, source[i]);
            }

            return tmpDelegate.asArray1D();
        }

        public final Array1D<N> makeFilled(final long count, final NullaryFunction<?> supplier) {

            final BasicArray<N> tmpDelegate = this.delegate().makeToBeFilled(count);

            for (int i = 0; i < count; i++) {
                tmpDelegate.set(i, supplier.get());
            }

            return tmpDelegate.asArray1D();
        }

        public final Array1D<N> makeZero(final long count) {
            return this.delegate().makeZero(count).asArray1D();
        }

        public final Array1D<N> wrap(final BasicArray<N> array) {
            return array.asArray1D();
        }

        abstract BasicArray.BasicFactory<N> delegate();

    }

    public static final Factory<BigDecimal> BIG = new Factory<BigDecimal>() {

        @Override
        BasicFactory<BigDecimal> delegate() {
            return BasicArray.BIG;
        }

    };

    public static final Factory<ComplexNumber> COMPLEX = new Factory<ComplexNumber>() {

        @Override
        BasicFactory<ComplexNumber> delegate() {
            return BasicArray.COMPLEX;
        }

    };

    public static final Factory<Double> PRIMITIVE = new Factory<Double>() {

        @Override
        public Array1D<Double> copy(final Access1D<?> source) {

            final long tmpCount = source.count();

            final BasicArray<Double> tmpDelegate = this.delegate().makeToBeFilled(tmpCount);

            for (long i = 0L; i < tmpCount; i++) {
                tmpDelegate.set(i, source.doubleValue(i));
            }

            return tmpDelegate.asArray1D();
        }

        @Override
        public Array1D<Double> copy(final double... source) {
            return new PrimitiveArray(source).asArray1D();
        }

        @Override
        BasicFactory<Double> delegate() {
            return BasicArray.PRIMITIVE;
        }

    };

    public static final Factory<Quaternion> QUATERNION = new Factory<Quaternion>() {

        @Override
        BasicFactory<Quaternion> delegate() {
            return BasicArray.QUATERNION;
        }

    };

    public static final Factory<RationalNumber> RATIONAL = new Factory<RationalNumber>() {

        @Override
        BasicFactory<RationalNumber> delegate() {
            return BasicArray.RATIONAL;
        }

    };

    @SuppressWarnings("unchecked")
    private static <T extends Number> T[] copyAndSort(final Array1D<T> anArray) {

        final int tmpLength = (int) anArray.length;
        final T[] retVal = (T[]) new Number[tmpLength];

        for (int i = 0; i < tmpLength; i++) {
            retVal[i] = anArray.get(i);
        }

        Arrays.sort(retVal);

        return retVal;
    }

    public final long length;

    private final BasicArray<N> myDelegate;

    private final long myFirst;
    private final long myLimit;
    private final long myStep;

    @SuppressWarnings("unused")
    private Array1D() {
        this(null);
    }

    Array1D(final BasicArray<N> delegate) {
        this(delegate, 0L, delegate.count(), 1L);
    }

    Array1D(final BasicArray<N> delegate, final long first, final long limit, final long step) {

        super();

        myDelegate = delegate;

        myFirst = first;
        myLimit = limit;
        myStep = step;

        length = (myLimit - myFirst) / myStep;
    }

    public void add(final long index, final double addend) {
        final long tmpIndex = myFirst + (myStep * index);
        myDelegate.add(tmpIndex, addend);
    }

    public void add(final long index, final Number addend) {
        final long tmpIndex = myFirst + (myStep * index);
        myDelegate.add(tmpIndex, addend);
    }

    @Override
    public boolean contains(final Object obj) {
        return this.indexOf(obj) != -1;
    }

    @SuppressWarnings("unchecked")
    public Array1D<N> copy() {

        BasicArray<N> retVal = null;

        if (myDelegate instanceof PrimitiveArray) {

            retVal = (BasicArray<N>) new PrimitiveArray((int) length);

            for (long i = 0; i < length; i++) {
                retVal.set(i, this.doubleValue(i));
            }

            return new Array1D<N>(retVal);

        } else if (myDelegate instanceof ComplexArray) {

            retVal = (BasicArray<N>) new ComplexArray((int) length);

            for (long i = 0; i < length; i++) {
                retVal.set(i, this.get(i));
            }

            return new Array1D<N>(retVal);

        } else if (myDelegate instanceof BigArray) {

            retVal = (BasicArray<N>) new BigArray((int) length);

            for (long i = 0; i < length; i++) {
                retVal.set(i, this.get(i));
            }

            return new Array1D<N>(retVal);

        } else {

            return null;
        }
    }

    /**
     * Creates a copy of this containing only the selected elements, in the specified order.
     */
    @SuppressWarnings("unchecked")
    public Array1D<N> copy(final int... indices) {

        BasicArray<N> retVal = null;

        final int tmpLength = indices.length;

        if (myDelegate instanceof PrimitiveArray) {

            retVal = (BasicArray<N>) new PrimitiveArray(tmpLength);

            for (int i = 0; i < tmpLength; i++) {
                retVal.set(i, this.doubleValue(indices[i]));
            }

            return new Array1D<N>(retVal);

        } else if (myDelegate instanceof ComplexArray) {

            retVal = (BasicArray<N>) new ComplexArray(tmpLength);

            for (int i = 0; i < tmpLength; i++) {
                retVal.set(i, this.get(indices[i]));
            }

            return new Array1D<N>(retVal);

        } else if (myDelegate instanceof BigArray) {

            retVal = (BasicArray<N>) new BigArray(tmpLength);

            for (int i = 0; i < tmpLength; i++) {
                retVal.set(i, this.get(indices[i]));
            }

            return new Array1D<N>(retVal);

        } else {

            return null;
        }
    }

    public long count() {
        return length;
    }

    public double doubleValue(final long index) {
        return myDelegate.doubleValue(myFirst + (myStep * index));
    }

    public void fillAll(final N value) {
        myDelegate.fill(myFirst, myLimit, myStep, value);
    }

    public void fillAll(final NullaryFunction<N> supplier) {
        myDelegate.fill(myFirst, myLimit, myStep, supplier);
    }

    public void fillOne(final long index, final N value) {
        final long tmpIndex = myFirst + (myStep * index);
        myDelegate.fillOne(tmpIndex, value);
    }

    public void fillOne(final long index, final NullaryFunction<N> supplier) {
        final long tmpIndex = myFirst + (myStep * index);
        myDelegate.fillOne(tmpIndex, supplier);
    }

    public void fillOneMatching(final long index, final Access1D<?> values, final long valueIndex) {
        myDelegate.fillOneMatching(myFirst + (myStep * index), values, valueIndex);
    }

    public void fillRange(final long first, final long limit, final N value) {
        final long tmpFirst = myFirst + (myStep * first);
        final long tmpLimit = myFirst + (myStep * limit);
        myDelegate.fill(tmpFirst, tmpLimit, myStep, value);
    }

    public void fillRange(final long first, final long limit, final NullaryFunction<N> supplier) {
        final long tmpFirst = myFirst + (myStep * first);
        final long tmpLimit = myFirst + (myStep * limit);
        myDelegate.fill(tmpFirst, tmpLimit, myStep, supplier);
    }

    @Override
    public N get(final int index) {
        return myDelegate.get(myFirst + (myStep * index));
    }

    public N get(final long index) {
        return myDelegate.get(myFirst + (myStep * index));
    }

    @Override
    public int indexOf(final Object obj) {
        final int tmpLength = (int) length;
        if (obj == null) {
            for (int i = 0; i < tmpLength; i++) {
                if (this.get(i) == null) {
                    return i;
                }
            }
        } else if (obj instanceof Number) {
            for (int i = 0; i < tmpLength; i++) {
                if (obj.equals(this.get(i))) {
                    return i;
                }
            }
        }
        return -1;
    }

    public long indexOfLargest() {
        return this.indexOfLargestInRange(myFirst, myLimit);
    }

    public long indexOfLargestInRange(final long first, final long limit) {
        return (myDelegate.indexOfLargest(myFirst + (myStep * first), myFirst + (myStep * limit), myStep) - myFirst) / myStep;
    }

    /**
     * @see Scalar#isAbsolute()
     */
    public boolean isAbsolute(final long index) {
        return myDelegate.isAbsolute(myFirst + (myStep * index));
    }

    /**
     * @deprecated v39
     */
    @Deprecated
    public boolean isAllZeros() {
        return myDelegate.isSmall(myFirst, myLimit, myStep, PrimitiveMath.ONE);
    }

    @Override
    public boolean isEmpty() {
        return length == 0;
    }

    /**
     * @deprecated v39
     */
    @Deprecated
    public boolean isRangeZeros(final long first, final long limit) {
        return myDelegate.isSmall((myFirst + (myStep * first)), (myFirst + (myStep * limit)), myStep, PrimitiveMath.ONE);
    }

    /**
     * @see Scalar#isSmall(double)
     */
    public boolean isSmall(final long index, final double comparedTo) {
        return myDelegate.isSmall(myFirst + (myStep * index), comparedTo);
    }

    public void modifyAll(final UnaryFunction<N> function) {
        myDelegate.modify(myFirst, myLimit, myStep, function);
    }

    public void modifyMatching(final Access1D<N> left, final BinaryFunction<N> function) {
        final long tmpLength = Math.min(length, left.count());
        if (myDelegate instanceof PrimitiveArray) {
            for (long i = 0L; i < tmpLength; i++) {
                this.set(i, function.invoke(left.doubleValue(i), this.doubleValue(i)));
            }
        } else {
            for (long i = 0L; i < tmpLength; i++) {
                this.set(i, function.invoke(left.get(i), this.get(i)));
            }
        }
    }

    public void modifyMatching(final BinaryFunction<N> function, final Access1D<N> right) {
        final long tmpLength = Math.min(length, right.count());
        if (myDelegate instanceof PrimitiveArray) {
            for (long i = 0; i < tmpLength; i++) {
                this.set(i, function.invoke(this.doubleValue(i), right.doubleValue(i)));
            }
        } else {
            for (long i = 0; i < tmpLength; i++) {
                this.set(i, function.invoke(this.get(i), right.get(i)));
            }
        }
    }

    public void modifyOne(final long index, final UnaryFunction<N> function) {
        myDelegate.modifyOne(myFirst + (myStep * index), function);
    }

    public void modifyRange(final long first, final long limit, final UnaryFunction<N> function) {
        final long tmpFirst = myFirst + (myStep * first);
        final long tmpLimit = myFirst + (myStep * limit);
        myDelegate.modify(tmpFirst, tmpLimit, myStep, function);
    }

    /**
     * Assumes you have first called {@linkplain #sortAscending()}.
     *
     * @deprecated v39
     */
    @Deprecated
    public int searchAscending(final N key) {

        if (myDelegate instanceof DenseArray<?>) {

            if (this.count() != myDelegate.count()) {

                final int tmpLength = (int) length;

                final Number[] tmpArray = new Number[tmpLength];

                for (int i = 0; i < tmpLength; i++) {
                    tmpArray[i] = this.get(i);
                }

                return Arrays.binarySearch(tmpArray, key);

            } else {

                return ((DenseArray<N>) myDelegate).searchAscending(key);
            }

        } else {

            throw new UnsupportedOperationException();
        }
    }

    /**
     * Asssumes you have first called {@linkplain #sortDescending()}.
     *
     * @deprecated v39
     */
    @Deprecated
    public int searchDescending(final N key) {

        if (myDelegate instanceof DenseArray<?>) {

            final int tmpLength = (int) length;
            final Number[] tmpArray = new Number[tmpLength];

            for (int i = 0; i < tmpLength; i++) {
                tmpArray[i] = this.get(tmpLength - 1 - i);
            }

            final int tmpInd = Arrays.binarySearch(tmpArray, key);

            if (tmpInd >= 0) {
                return tmpLength - 1 - tmpInd;
            } else if (tmpInd < -1) {
                return -tmpLength - tmpInd - 1;
            } else {
                return -1;
            }

        } else {

            throw new UnsupportedOperationException();
        }
    }

    @Override
    public N set(final int index, final Number value) {
        final long tmpIndex = myFirst + (myStep * index);
        final N retVal = myDelegate.get(tmpIndex);
        myDelegate.set(tmpIndex, value);
        return retVal;
    }

    public void set(final long index, final double value) {
        myDelegate.set(myFirst + (myStep * index), value);
    }

    public void set(final long index, final Number value) {
        myDelegate.set(myFirst + (myStep * index), value);
    }

    @Override
    public int size() {
        return (int) length;
    }

    public Array1D<N> sliceRange(final long first, final long limit) {
        return new Array1D<N>(myDelegate, myFirst + (myStep * first), myFirst + (myStep * limit), myStep);
    }

    /**
     * @deprecated v39
     */
    @Deprecated
    public void sortAscending() {

        if (myDelegate instanceof DenseArray<?>) {

            if (this.count() != myDelegate.count()) {

                final N[] tmpArray = Array1D.copyAndSort(this);

                final int tmpLength = (int) length;
                for (int i = 0; i < tmpLength; i++) {
                    this.set(i, tmpArray[i]);
                }

            } else {

                ((DenseArray<N>) myDelegate).sortAscending();
            }

        } else {

            throw new UnsupportedOperationException();
        }
    }

    /**
     * @deprecated v39
     */
    @Deprecated
    public void sortDescending() {

        if (myDelegate instanceof DenseArray<?>) {

            final N[] tmpArray = Array1D.copyAndSort(this);

            final int tmpLength = (int) length;
            for (int i = 0; i < tmpLength; i++) {
                this.set(i, tmpArray[tmpLength - 1 - i]);
            }

        } else {

            throw new UnsupportedOperationException();
        }
    }

    public Spliterator<N> spliterator() {
        return Spliterators.spliterator(this, Spliterator.ORDERED | Spliterator.IMMUTABLE);
    }

    @Override
    public Array1D<N> subList(final int first, final int limit) {
        return this.sliceRange(first, limit);
    }

    /**
     * @deprecated v39 Use {@link #toRawCopy1D()} instead
     */
    @Deprecated
    public double[] toRawCopy() {

        final int tmpLength = (int) length;
        final double[] retVal = new double[tmpLength];

        for (int i = 0; i < tmpLength; i++) {
            retVal[i] = this.doubleValue(i);
        }

        return retVal;
    }

    public void visitAll(final VoidFunction<N> visitor) {
        myDelegate.visit(myFirst, myLimit, myStep, visitor);
    }

    public void visitOne(final long index, final VoidFunction<N> visitor) {
        myDelegate.visitOne(myFirst + (myStep * index), visitor);
    }

    public void visitRange(final long first, final long limit, final VoidFunction<N> visitor) {
        final long tmpFirst = myFirst + (myStep * first);
        final long tmpLimit = myFirst + (myStep * limit);
        myDelegate.visit(tmpFirst, tmpLimit, myStep, visitor);
    }

    BasicArray<N> getDelegate() {
        return myDelegate;
    }

}

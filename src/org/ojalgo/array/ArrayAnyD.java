/*
 * Copyright 1997-2014 Optimatika (www.optimatika.se)
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
import java.util.Iterator;

import org.ojalgo.access.AccessAnyD;
import org.ojalgo.access.AccessUtils;
import org.ojalgo.access.Iterator1D;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.ParameterFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.random.RandomNumber;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.type.TypeUtils;

/**
 * ArrayAnyD
 * 
 * @author apete
 */
public final class ArrayAnyD<N extends Number> implements AccessAnyD<N>, AccessAnyD.Elements, AccessAnyD.Fillable<N>, AccessAnyD.Modifiable<N>,
        AccessAnyD.Visitable<N>, Serializable {

    public static final Factory<ArrayAnyD<BigDecimal>> BIG = new Factory<ArrayAnyD<BigDecimal>>() {

        public ArrayAnyD<BigDecimal> copy(final AccessAnyD<?> aSource) {

            final int tmpSize = (int) aSource.count();

            final BigDecimal[] tmpData = new BigDecimal[tmpSize];
            final long[] tmpStructure = AccessUtils.structure(aSource);

            for (int i = 0; i < tmpSize; i++) {
                tmpData[i] = TypeUtils.toBigDecimal(aSource.get(i));
            }

            return new BigArray(tmpData).asArrayAnyD(tmpStructure);
        }

        public ArrayAnyD<BigDecimal> makeRandom(final long[] structure, final RandomNumber distribution) {

            final int tmpSize = (int) AccessUtils.count(structure);

            final BigDecimal[] tmpData = new BigDecimal[tmpSize];

            for (int i = 0; i < tmpSize; i++) {
                tmpData[i] = TypeUtils.toBigDecimal(distribution.doubleValue());
            }

            return new BigArray(tmpData).asArrayAnyD(structure);
        }

        public ArrayAnyD<BigDecimal> makeZero(final long... aStructure) {
            final int tmpSize = (int) AccessUtils.count(aStructure);
            return new BigArray(tmpSize).asArrayAnyD(aStructure);
        }

    };

    public static final Factory<ArrayAnyD<ComplexNumber>> COMPLEX = new Factory<ArrayAnyD<ComplexNumber>>() {

        public ArrayAnyD<ComplexNumber> copy(final AccessAnyD<?> aSource) {

            final int tmpSize = (int) aSource.count();

            final ComplexNumber[] tmpData = new ComplexNumber[tmpSize];
            final long[] tmpStructure = AccessUtils.structure(aSource);

            for (int i = 0; i < tmpSize; i++) {
                tmpData[i] = TypeUtils.toComplexNumber(aSource.get(i));
            }

            return new ComplexArray(tmpData).asArrayAnyD(tmpStructure);
        }

        public ArrayAnyD<ComplexNumber> makeRandom(final long[] structure, final RandomNumber distribution) {

            final int tmpSize = (int) AccessUtils.count(structure);

            final ComplexNumber[] tmpData = new ComplexNumber[tmpSize];

            for (int i = 0; i < tmpSize; i++) {
                tmpData[i] = TypeUtils.toComplexNumber(distribution.doubleValue());
            }

            return new ComplexArray(tmpData).asArrayAnyD(structure);
        }

        public ArrayAnyD<ComplexNumber> makeZero(final long... aStructure) {
            final int tmpSize = (int) AccessUtils.count(aStructure);
            return new ComplexArray(tmpSize).asArrayAnyD(aStructure);
        }

    };

    public static final Factory<ArrayAnyD<Double>> PRIMITIVE = new Factory<ArrayAnyD<Double>>() {

        public ArrayAnyD<Double> copy(final AccessAnyD<?> aSource) {

            final int tmpSize = (int) aSource.count();

            final double[] tmpData = new double[tmpSize];
            final long[] tmpStructure = AccessUtils.structure(aSource);

            for (int i = 0; i < tmpSize; i++) {
                tmpData[i] = aSource.doubleValue(i);
            }

            return new PrimitiveArray(tmpData).asArrayAnyD(tmpStructure);
        }

        public ArrayAnyD<Double> makeRandom(final long[] structure, final RandomNumber aRndm) {

            final int tmpSize = (int) AccessUtils.count(structure);

            final double[] tmpData = new double[tmpSize];

            for (int i = 0; i < tmpSize; i++) {
                tmpData[i] = aRndm.doubleValue();
            }

            return new PrimitiveArray(tmpData).asArrayAnyD(structure);
        }

        public ArrayAnyD<Double> makeZero(final long... aStructure) {
            final int tmpSize = (int) AccessUtils.count(aStructure);
            return new PrimitiveArray(tmpSize).asArrayAnyD(aStructure);
        }

    };

    public static final Factory<ArrayAnyD<RationalNumber>> RATIONAL = new Factory<ArrayAnyD<RationalNumber>>() {

        public ArrayAnyD<RationalNumber> copy(final AccessAnyD<?> aSource) {

            final int tmpSize = (int) aSource.count();

            final BasicArray<RationalNumber> tmpBase = new RationalArray(tmpSize);

            tmpBase.fill(aSource);

            final long[] tmpStructure = AccessUtils.structure(aSource);

            return tmpBase.asArrayAnyD(tmpStructure);
        }

        public ArrayAnyD<RationalNumber> makeRandom(final long[] structure, final RandomNumber distribution) {

            final ArrayAnyD<RationalNumber> retVal = this.makeZero(structure);

            final long tmpSize = AccessUtils.count(structure);
            for (long i = 0; i < tmpSize; i++) {
                retVal.set(i, distribution.doubleValue());
            }

            return retVal;
        }

        public ArrayAnyD<RationalNumber> makeZero(final long... aStructure) {
            final int tmpSize = (int) AccessUtils.count(aStructure);
            return new RationalArray(tmpSize).asArrayAnyD(aStructure);
        }

    };

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

    /**
     * Flattens this abitrary dimensional array to a one dimensional array. The (internal/actual) array is not copied,
     * it is just accessed through a different adaptor.
     */
    public Array1D<N> asArray1D() {
        return myDelegate.asArray1D();
    }

    public long count() {
        return myDelegate.count();
    }

    public long count(final int dimension) {
        return AccessUtils.count(myStructure, dimension);
    }

    public double doubleValue(final long index) {
        return myDelegate.doubleValue(index);
    }

    public double doubleValue(final long[] reference) {
        return myDelegate.doubleValue(AccessUtils.index(myStructure, reference));
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof ArrayAnyD) {
            final ArrayAnyD<N> tmpObj = (ArrayAnyD<N>) obj;
            return Arrays.equals(myStructure, tmpObj.structure()) && myDelegate.equals(tmpObj.getDelegate());
        } else {
            return super.equals(obj);
        }
    }

    public void fillAll(final N number) {
        myDelegate.fill(0L, this.count(), 1L, number);
    }

    public void fillRange(final long first, final long limit, final N value) {
        myDelegate.fill(first, limit, 1L, value);
    }

    public void fillSet(final long[] first, final int dimension, final N number) {

        final long tmpCount = AccessUtils.count(myStructure, dimension) - first[dimension];

        final long tmpFirst = AccessUtils.index(myStructure, first);
        final long tmpStep = AccessUtils.step(myStructure, dimension);
        final long tmpLimit = tmpFirst * tmpStep * tmpCount;

        myDelegate.fill(tmpFirst, tmpLimit, tmpStep, number);
    }

    public N get(final long index) {
        return myDelegate.get(index);
    }

    public N get(final long[] reference) {
        return myDelegate.get(AccessUtils.index(myStructure, reference));
    }

    @Override
    public int hashCode() {
        return myDelegate.hashCode();
    }

    public boolean isAbsolute(final long index) {
        return myDelegate.isAbsolute(index);
    }

    /**
     * @see Scalar#isAbsolute()
     */
    public boolean isAbsolute(final long[] reference) {
        return myDelegate.isAbsolute(AccessUtils.index(myStructure, reference));
    }

    public boolean isAllZeros() {
        return myDelegate.isZeros(0L, myDelegate.count(), 1L);
    }

    public boolean isInfinite(final long index) {
        return myDelegate.isInfinite(index);
    }

    public boolean isInfinite(final long[] reference) {
        return myDelegate.isInfinite(AccessUtils.index(myStructure, reference));
    }

    public boolean isNaN(final long index) {
        return myDelegate.isNaN(index);
    }

    public boolean isNaN(final long[] reference) {
        return myDelegate.isNaN(AccessUtils.index(myStructure, reference));
    }

    public boolean isPositive(final long index) {
        return myDelegate.isPositive(index);
    }

    public boolean isPositive(final long[] reference) {
        return myDelegate.isPositive(AccessUtils.index(myStructure, reference));
    }

    public boolean isReal(final long index) {
        return myDelegate.isReal(index);
    }

    /**
     * @see Scalar#isReal()
     */
    public boolean isReal(final long[] reference) {
        return myDelegate.isReal(AccessUtils.index(myStructure, reference));
    }

    public boolean isZero(final long index) {
        return myDelegate.isZero(index);
    }

    /**
     * @see Scalar#isZero()
     */
    public boolean isZero(final long[] reference) {
        return myDelegate.isZero(AccessUtils.index(myStructure, reference));
    }

    public boolean isZeros(final long[] first, final int dimension) {

        final long tmpCount = AccessUtils.count(myStructure, dimension) - first[dimension];

        final long tmpFirst = AccessUtils.index(myStructure, first);
        final long tmpStep = AccessUtils.step(myStructure, dimension);
        final long tmpLimit = tmpFirst * tmpStep * tmpCount;

        return myDelegate.isZeros(tmpFirst, tmpLimit, tmpStep);
    }

    public Iterator<N> iterator() {
        return new Iterator1D<N>(this);
    }

    public void modifyAll(final UnaryFunction<N> function) {
        myDelegate.modify(0L, this.count(), 1L, function);
    }

    public void modifyMatching(final ArrayAnyD<N> left, final BinaryFunction<N> function) {
        myDelegate.modify(0L, this.count(), 1L, left.getDelegate(), function);
    }

    public void modifyMatching(final BinaryFunction<N> function, final ArrayAnyD<N> right) {
        myDelegate.modify(0L, this.count(), 1L, function, right.getDelegate());
    }

    public void modifyRange(final long first, final long limit, final UnaryFunction<N> function) {
        myDelegate.modify(first, limit, 1L, function);
    }

    public void modifySet(final long[] first, final int dimension, final UnaryFunction<N> function) {

        final long tmpCount = AccessUtils.count(myStructure, dimension) - first[dimension];

        final long tmpFirst = AccessUtils.index(myStructure, first);
        final long tmpStep = AccessUtils.step(myStructure, dimension);
        final long tmpLimit = tmpFirst * tmpStep * tmpCount;

        myDelegate.modify(tmpFirst, tmpLimit, tmpStep, function);
    }

    public int rank() {
        return myStructure.length;
    }

    public void set(final long index, final double value) {
        myDelegate.set(index, value);
    }

    public void set(final long index, final Number value) {
        myDelegate.set(index, value);
    }

    public void set(final long[] reference, final double value) {
        myDelegate.set(AccessUtils.index(myStructure, reference), value);
    }

    public void set(final long[] reference, final Number value) {
        myDelegate.set(AccessUtils.index(myStructure, reference), value);
    }

    public Array1D<N> slice(final long[] first, final int dimension) {

        final long tmpCount = AccessUtils.count(myStructure, dimension) - first[dimension];

        final long tmpFirst = AccessUtils.index(myStructure, first);
        final long tmpStep = AccessUtils.step(myStructure, dimension);
        final long tmpLimit = tmpFirst * tmpStep * tmpCount;

        return new Array1D<N>(myDelegate, tmpFirst, tmpLimit, tmpStep);
    }

    public long[] structure() {
        return myStructure;
    }

    public Scalar<N> toScalar(final long... reference) {
        return myDelegate.toScalar(AccessUtils.index(myStructure, reference));
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

    public void visitAll(final VoidFunction<N> visitor) {
        myDelegate.visit(0L, this.count(), 1L, visitor);
    }

    public void visitRange(final long first, final long limit, final VoidFunction<N> visitor) {
        myDelegate.visit(first, limit, 1L, visitor);
    }

    public void visitSet(final long[] first, final int dimension, final VoidFunction<N> visitor) {

        final long tmpCount = AccessUtils.count(myStructure, dimension) - first[dimension];

        final long tmpFirst = AccessUtils.index(myStructure, first);
        final long tmpStep = AccessUtils.step(myStructure, dimension);
        final long tmpLimit = tmpFirst * tmpStep * tmpCount;

        myDelegate.visit(tmpFirst, tmpLimit, tmpStep, visitor);
    }

    final BasicArray<N> getDelegate() {
        return myDelegate;
    }

}

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
import java.util.Arrays;

import org.ojalgo.access.Access1D;
import org.ojalgo.access.AccessAnyD;
import org.ojalgo.access.AccessUtils;
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
 * ArrayAnyD
 *
 * @author apete
 */
public final class ArrayAnyD<N extends Number> implements AccessAnyD<N>, AccessAnyD.Elements, AccessAnyD.IndexOf, AccessAnyD.Fillable<N>,
        AccessAnyD.Modifiable<N>, AccessAnyD.Visitable<N>, AccessAnyD.Sliceable<N>, Serializable {

    public static abstract class Factory<N extends Number> implements AccessAnyD.Factory<ArrayAnyD<N>> {

        public ArrayAnyD<N> copy(final AccessAnyD<?> source) {

            final long[] tmpStructure = source.shape();

            final BasicArray<N> tmpDelegate = this.delegate().makeToBeFilled(tmpStructure);

            final long tmpCount = source.count();
            for (long index = 0L; index < tmpCount; index++) {
                tmpDelegate.set(index, source.get(index));
            }

            return tmpDelegate.asArrayAnyD(tmpStructure);
        }

        public final ArrayAnyD<N> makeFilled(final long[] structure, final NullaryFunction<?> supplier) {

            final BasicArray<N> tmpDelegate = this.delegate().makeToBeFilled(structure);

            final long tmpCount = AccessUtils.count(structure);
            for (long index = 0L; index < tmpCount; index++) {
                tmpDelegate.set(index, supplier.get());
            }

            return tmpDelegate.asArrayAnyD(structure);
        }

        public final ArrayAnyD<N> makeZero(final long... structure) {
            return this.delegate().makeStructuredZero(structure).asArrayAnyD(structure);
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
        public ArrayAnyD<Double> copy(final AccessAnyD<?> source) {

            final long[] tmpStructure = source.shape();

            final BasicArray<Double> tmpDelegate = this.delegate().makeToBeFilled(tmpStructure);

            final long tmpCount = source.count();
            for (long index = 0L; index < tmpCount; index++) {
                tmpDelegate.set(index, source.doubleValue(index));
            }

            return tmpDelegate.asArrayAnyD(tmpStructure);
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
        myDelegate.add(AccessUtils.index(myStructure, reference), addend);
    }

    public void add(final long[] reference, final Number addend) {
        myDelegate.add(AccessUtils.index(myStructure, reference), addend);
    }

    /**
     * Flattens this abitrary dimensional array to a one dimensional array. The (internal/actual) array is not
     * copied, it is just accessed through a different adaptor.
     *
     * @deprecated v39 Not needed
     */
    @Deprecated
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

    public void fillOne(final long index, final N value) {
        myDelegate.fillOne(index, value);
    }

    public void fillOne(final long index, final NullaryFunction<N> supplier) {
        myDelegate.fillOne(index, supplier);
    }

    public void fillOne(final long[] reference, final N value) {
        myDelegate.fillOne(AccessUtils.index(myStructure, reference), value);
    }

    public void fillOne(final long[] reference, final NullaryFunction<N> supplier) {
        myDelegate.fillOne(AccessUtils.index(myStructure, reference), supplier);
    }

    public void fillOneMatching(final long index, final Access1D<?> values, final long valueIndex) {
        myDelegate.fillOneMatching(index, values, valueIndex);
    }

    public void fillRange(final long first, final long limit, final N value) {
        myDelegate.fill(first, limit, 1L, value);
    }

    public void fillRange(final long first, final long limit, final NullaryFunction<N> supplier) {
        myDelegate.fill(first, limit, 1L, supplier);
    }

    public void fillSet(final long[] first, final int dimension, final N number) {

        final long tmpCount = AccessUtils.count(myStructure, dimension) - first[dimension];

        final long tmpFirst = AccessUtils.index(myStructure, first);
        final long tmpStep = AccessUtils.step(myStructure, dimension);
        final long tmpLimit = tmpFirst + (tmpStep * tmpCount);

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

    /**
     * @deprecated v39
     */
    @Deprecated
    public boolean isAllZeros() {
        return myDelegate.isSmall(0L, myDelegate.count(), 1L, PrimitiveMath.ONE);
    }

    public boolean isSmall(final long index, final double comparedTo) {
        return myDelegate.isSmall(index, comparedTo);
    }

    public boolean isSmall(final long[] reference, final double comparedTo) {
        return myDelegate.isSmall(AccessUtils.index(myStructure, reference), comparedTo);
    }

    /**
     * @deprecated v39
     */
    @Deprecated
    public boolean isZeros(final long[] first, final int dimension) {

        final long tmpCount = AccessUtils.count(myStructure, dimension) - first[dimension];

        final long tmpFirst = AccessUtils.index(myStructure, first);
        final long tmpStep = AccessUtils.step(myStructure, dimension);
        final long tmpLimit = tmpFirst + (tmpStep * tmpCount);

        return myDelegate.isSmall(tmpFirst, tmpLimit, tmpStep, PrimitiveMath.ONE);
    }

    public void modifyAll(final UnaryFunction<N> function) {
        myDelegate.modify(0L, this.count(), 1L, function);
    }

    public void modifyMatching(final Access1D<N> left, final BinaryFunction<N> function) {
        myDelegate.modify(0L, this.count(), 1L, left, function);
    }

    public void modifyMatching(final BinaryFunction<N> function, final Access1D<N> right) {
        myDelegate.modify(0L, this.count(), 1L, function, right);
    }

    public void modifyOne(final long index, final UnaryFunction<N> function) {
        myDelegate.modifyOne(index, function);
    }

    public void modifyOne(final long[] reference, final UnaryFunction<N> function) {
        myDelegate.modifyOne(AccessUtils.index(myStructure, reference), function);
    }

    public void modifyRange(final long first, final long limit, final UnaryFunction<N> function) {
        myDelegate.modify(first, limit, 1L, function);
    }

    public void modifySet(final long[] first, final int dimension, final UnaryFunction<N> function) {

        final long tmpCount = AccessUtils.count(myStructure, dimension) - first[dimension];

        final long tmpFirst = AccessUtils.index(myStructure, first);
        final long tmpStep = AccessUtils.step(myStructure, dimension);
        final long tmpLimit = tmpFirst + (tmpStep * tmpCount);

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

    public long[] shape() {
        return myStructure;
    }

    public Array1D<N> slice(final long[] first, final int dimension) {

        final long tmpCount = AccessUtils.count(myStructure, dimension) - first[dimension];

        final long tmpFirst = AccessUtils.index(myStructure, first);
        final long tmpStep = AccessUtils.step(myStructure, dimension);
        final long tmpLimit = tmpFirst + (tmpStep * tmpCount);

        return new Array1D<N>(myDelegate, tmpFirst, tmpLimit, tmpStep);
    }

    public Array1D<N> sliceRange(final long first, final long limit) {
        return myDelegate.asArray1D().sliceRange(first, limit);
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

    public void visitOne(final long index, final VoidFunction<N> visitor) {
        myDelegate.visitOne(index, visitor);
    }

    public void visitOne(final long[] reference, final VoidFunction<N> visitor) {
        myDelegate.visitOne(AccessUtils.index(myStructure, reference), visitor);
    }

    public void visitRange(final long first, final long limit, final VoidFunction<N> visitor) {
        myDelegate.visit(first, limit, 1L, visitor);
    }

    public void visitSet(final long[] first, final int dimension, final VoidFunction<N> visitor) {

        final long tmpCount = AccessUtils.count(myStructure, dimension) - first[dimension];

        final long tmpFirst = AccessUtils.index(myStructure, first);
        final long tmpStep = AccessUtils.step(myStructure, dimension);
        final long tmpLimit = tmpFirst + (tmpStep * tmpCount);

        myDelegate.visit(tmpFirst, tmpLimit, tmpStep, visitor);
    }

    final BasicArray<N> getDelegate() {
        return myDelegate;
    }

    public long indexOfLargest() {
        return myDelegate.indexOfLargest();
    }

    public long indexOfLargestInRange(final long first, final long limit) {
        return myDelegate.indexOfLargestInRange(first, limit);
    }

}

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

import java.util.Arrays;

import org.ojalgo.access.Access1D;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.ParameterFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.machine.MemoryEstimator;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.type.TypeUtils;

/**
 * A one- and/or arbitrary-dimensional array of {@linkplain org.ojalgo.scalar.RationalNumber}.
 *
 * @author apete
 */
public class RationalArray extends DenseArray<RationalNumber> {

    static final long ELEMENT_SIZE = MemoryEstimator.estimateObject(RationalNumber.class);

    static final DenseFactory<RationalNumber> FACTORY = new DenseFactory<RationalNumber>() {

        @Override
        long getElementSize() {
            return ELEMENT_SIZE;
        }

        @Override
        DenseArray<RationalNumber> make(final int size) {
            return RationalArray.make(size);
        }

        @Override
        Scalar<RationalNumber> zero() {
            return RationalNumber.ZERO;
        }
    };

    public static final RationalArray make(final int size) {
        return new RationalArray(size);
    }

    public static final SegmentedArray<RationalNumber> makeSegmented(final long count) {
        return SegmentedArray.RATIONAL.makeSegmented(FACTORY, count);
    }

    public static final RationalArray wrap(final RationalNumber[] data) {
        return new RationalArray(data);
    }

    protected static void exchange(final RationalNumber[] aData, final int aFirstA, final int aFirstB, final int aStep, final int aCount) {

        int tmpIndexA = aFirstA;
        int tmpIndexB = aFirstB;

        RationalNumber tmpVal;

        for (int i = 0; i < aCount; i++) {

            tmpVal = aData[tmpIndexA];
            aData[tmpIndexA] = aData[tmpIndexB];
            aData[tmpIndexB] = tmpVal;

            tmpIndexA += aStep;
            tmpIndexB += aStep;
        }
    }

    protected static void fill(final RationalNumber[] aData, final Access1D<?> anArg) {
        final int tmpLimit = (int) Math.min(aData.length, anArg.count());
        for (int i = 0; i < tmpLimit; i++) {
            aData[i] = TypeUtils.toRationalNumber(anArg.get(i));
        }
    }

    protected static void fill(final RationalNumber[] aData, final int aFirst, final int aLimit, final int aStep, final RationalNumber aNmbr) {
        for (int i = aFirst; i < aLimit; i += aStep) {
            aData[i] = aNmbr;
        }
    }

    protected static void invoke(final RationalNumber[] aData, final int aFirst, final int aLimit, final int aStep, final Access1D<RationalNumber> aLeftArg,
            final BinaryFunction<RationalNumber> aFunc, final Access1D<RationalNumber> aRightArg) {
        for (int i = aFirst; i < aLimit; i += aStep) {
            aData[i] = aFunc.invoke(aLeftArg.get(i), aRightArg.get(i));
        }
    }

    protected static void invoke(final RationalNumber[] aData, final int aFirst, final int aLimit, final int aStep, final Access1D<RationalNumber> aLeftArg,
            final BinaryFunction<RationalNumber> aFunc, final RationalNumber aRightArg) {
        for (int i = aFirst; i < aLimit; i += aStep) {
            aData[i] = aFunc.invoke(aLeftArg.get(i), aRightArg);
        }
    }

    protected static void invoke(final RationalNumber[] aData, final int aFirst, final int aLimit, final int aStep, final Access1D<RationalNumber> anArg,
            final ParameterFunction<RationalNumber> aFunc, final int aParam) {
        for (int i = aFirst; i < aLimit; i += aStep) {
            aData[i] = aFunc.invoke(anArg.get(i), aParam);
        }
    }

    protected static void invoke(final RationalNumber[] aData, final int aFirst, final int aLimit, final int aStep, final Access1D<RationalNumber> anArg,
            final UnaryFunction<RationalNumber> aFunc) {
        for (int i = aFirst; i < aLimit; i += aStep) {
            aData[i] = aFunc.invoke(anArg.get(i));
        }
    }

    protected static void invoke(final RationalNumber[] aData, final int aFirst, final int aLimit, final int aStep, final RationalNumber aLeftArg,
            final BinaryFunction<RationalNumber> aFunc, final Access1D<RationalNumber> aRightArg) {
        for (int i = aFirst; i < aLimit; i += aStep) {
            aData[i] = aFunc.invoke(aLeftArg, aRightArg.get(i));
        }
    }

    protected static void invoke(final RationalNumber[] aData, final int aFirst, final int aLimit, final int aStep, final VoidFunction<RationalNumber> aVisitor) {
        for (int i = aFirst; i < aLimit; i += aStep) {
            aVisitor.invoke(aData[i]);
        }
    }

    public final RationalNumber[] data;

    protected RationalArray(final int size) {

        super();

        data = new RationalNumber[size];
        this.fill(0, size, 1, RationalNumber.ZERO);
    }

    protected RationalArray(final RationalNumber[] data) {

        super();

        this.data = data;
    }

    @Override
    public boolean equals(final Object anObj) {
        if (anObj instanceof RationalArray) {
            return Arrays.equals(data, ((RationalArray) anObj).data);
        } else {
            return super.equals(anObj);
        }
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }

    protected final RationalNumber[] copyOfData() {
        return ArrayUtils.copyOf(data);
    }

    @Override
    protected final double doubleValue(final int index) {
        return data[index].doubleValue();
    }

    @Override
    protected final void exchange(final int firstA, final int firstB, final int step, final int count) {
        RationalArray.exchange(data, firstA, firstB, step, count);
    }

    protected void fill(final Access1D<?> anArg) {
        RationalArray.fill(data, anArg);
    }

    @Override
    protected final void fill(final int aFirst, final int aLimit, final Access1D<RationalNumber> aLeftArg, final BinaryFunction<RationalNumber> aFunc,
            final Access1D<RationalNumber> aRightArg) {
        RationalArray.invoke(data, aFirst, aLimit, 1, aLeftArg, aFunc, aRightArg);
    }

    @Override
    protected final void fill(final int aFirst, final int aLimit, final Access1D<RationalNumber> aLeftArg, final BinaryFunction<RationalNumber> aFunc,
            final RationalNumber aRightArg) {
        RationalArray.invoke(data, aFirst, aLimit, 1, aLeftArg, aFunc, aRightArg);
    }

    @Override
    protected final void fill(final int first, final int limit, final int step, final RationalNumber value) {
        RationalArray.fill(data, first, limit, step, value);
    }

    @Override
    protected final void fill(final int aFirst, final int aLimit, final RationalNumber aLeftArg, final BinaryFunction<RationalNumber> aFunc,
            final Access1D<RationalNumber> aRightArg) {
        RationalArray.invoke(data, aFirst, aLimit, 1, aLeftArg, aFunc, aRightArg);
    }

    @Override
    protected final RationalNumber get(final int index) {
        return data[index];
    }

    @Override
    protected final int indexOfLargest(final int first, final int limit, final int step) {

        int retVal = first;
        RationalNumber tmpLargest = RationalNumber.ZERO;
        RationalNumber tmpValue;

        for (int i = first; i < limit; i += step) {
            tmpValue = data[i];
            if (tmpValue.compareTo(tmpLargest) == 1) {
                tmpLargest = tmpValue;
                retVal = i;
            }
        }

        return retVal;
    }

    @Override
    protected final boolean isAbsolute(final int index) {
        return RationalNumber.isAbsolute(data[index]);
    }

    @Override
    protected final boolean isPositive(final int index) {
        return RationalNumber.isPositive(data[index]);
    }

    @Override
    protected final boolean isZero(final int index) {
        return data[index].isZero();
    }

    @Override
    protected final boolean isZeros(final int first, final int limit, final int step) {

        boolean retVal = true;

        for (int i = first; retVal && (i < limit); i += step) {
            retVal &= this.isZero(i);
        }

        return retVal;
    }

    @Override
    protected void modify(final int index, final Access1D<RationalNumber> left, final BinaryFunction<RationalNumber> function) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void modify(final int index, final BinaryFunction<RationalNumber> function, final Access1D<RationalNumber> right) {
        // TODO Auto-generated method stub

    }

    @Override
    protected final void modify(final int aFirst, final int aLimit, final int aStep, final Access1D<RationalNumber> aLeftArg,
            final BinaryFunction<RationalNumber> aFunc) {
        RationalArray.invoke(data, aFirst, aLimit, aStep, aLeftArg, aFunc, this);
    }

    @Override
    protected final void modify(final int aFirst, final int aLimit, final int aStep, final BinaryFunction<RationalNumber> aFunc,
            final Access1D<RationalNumber> aRightArg) {
        RationalArray.invoke(data, aFirst, aLimit, aStep, this, aFunc, aRightArg);
    }

    @Override
    protected final void modify(final int aFirst, final int aLimit, final int aStep, final BinaryFunction<RationalNumber> aFunc, final RationalNumber aRightArg) {
        RationalArray.invoke(data, aFirst, aLimit, aStep, this, aFunc, aRightArg);
    }

    @Override
    protected final void modify(final int first, final int limit, final int step, final ParameterFunction<RationalNumber> func, final int param) {
        RationalArray.invoke(data, first, limit, step, this, func, param);
    }

    @Override
    protected final void modify(final int aFirst, final int aLimit, final int aStep, final RationalNumber aLeftArg, final BinaryFunction<RationalNumber> aFunc) {
        RationalArray.invoke(data, aFirst, aLimit, aStep, aLeftArg, aFunc, this);
    }

    @Override
    protected final void modify(final int first, final int limit, final int step, final UnaryFunction<RationalNumber> func) {
        RationalArray.invoke(data, first, limit, step, this, func);
    }

    @Override
    protected void modify(final int index, final UnaryFunction<RationalNumber> func) {
        data[index] = func.invoke(data[index]);
    }

    /**
     * @see org.ojalgo.array.BasicArray#searchAscending(java.lang.Number)
     */
    @Override
    protected final int searchAscending(final RationalNumber aNmbr) {
        return Arrays.binarySearch(data, aNmbr);
    }

    @Override
    protected final void set(final int index, final double value) {
        data[index] = new RationalNumber(value);
    }

    @Override
    protected final void set(final int index, final Number value) {
        data[index] = TypeUtils.toRationalNumber(value);
    }

    @Override
    protected int size() {
        return data.length;
    }

    @Override
    protected final void sortAscending() {
        Arrays.sort(data);
    }

    @Override
    protected final RationalNumber toScalar(final long index) {
        return data[(int) index];
    }

    @Override
    protected final void visit(final int first, final int limit, final int step, final VoidFunction<RationalNumber> visitor) {
        RationalArray.invoke(data, first, limit, step, visitor);
    }

    @Override
    protected final void visit(final int index, final VoidFunction<RationalNumber> visitor) {
        visitor.invoke(data[index]);
    }

    @Override
    boolean isPrimitive() {
        return false;
    }

    @Override
    DenseArray<RationalNumber> newInstance(final int capacity) {
        return new RationalArray(capacity);
    }

}

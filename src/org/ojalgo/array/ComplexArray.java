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

import static org.ojalgo.constant.PrimitiveMath.*;

import java.util.Arrays;

import org.ojalgo.access.Access1D;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.ParameterFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.type.TypeUtils;

/**
 * A one- and/or arbitrary-dimensional array of {@linkplain org.ojalgo.scalar.ComplexNumber}.
 * 
 * @see PrimitiveArray
 * @author apete
 */
public class ComplexArray extends DenseArray<ComplexNumber> {

    public static final ComplexArray make(final int size) {
        return new ComplexArray(size);
    }

    public static final ComplexArray wrap(final ComplexNumber[] data) {
        return new ComplexArray(data);
    }

    protected static void exchange(final ComplexNumber[] aData, final int aFirstA, final int aFirstB, final int aStep, final int aCount) {

        int tmpIndexA = aFirstA;
        int tmpIndexB = aFirstB;

        ComplexNumber tmpVal;

        for (int i = 0; i < aCount; i++) {

            tmpVal = aData[tmpIndexA];
            aData[tmpIndexA] = aData[tmpIndexB];
            aData[tmpIndexB] = tmpVal;

            tmpIndexA += aStep;
            tmpIndexB += aStep;
        }
    }

    protected static void fill(final ComplexNumber[] aData, final Access1D<?> anArg) {
        final int tmpLimit = (int) Math.min(aData.length, anArg.count());
        for (int i = 0; i < tmpLimit; i++) {
            aData[i] = TypeUtils.toComplexNumber(anArg.get(i));
        }
    }

    protected static void fill(final ComplexNumber[] aData, final int aFirst, final int aLimit, final int aStep, final ComplexNumber aNmbr) {
        for (int i = aFirst; i < aLimit; i += aStep) {
            aData[i] = aNmbr;
        }
    }

    protected static void invoke(final ComplexNumber[] aData, final int aFirst, final int aLimit, final int aStep, final Access1D<ComplexNumber> aLeftArg,
            final BinaryFunction<ComplexNumber> aFunc, final Access1D<ComplexNumber> aRightArg) {
        for (int i = aFirst; i < aLimit; i += aStep) {
            aData[i] = aFunc.invoke(aLeftArg.get(i), aRightArg.get(i));
        }
    }

    protected static void invoke(final ComplexNumber[] aData, final int aFirst, final int aLimit, final int aStep, final Access1D<ComplexNumber> aLeftArg,
            final BinaryFunction<ComplexNumber> aFunc, final ComplexNumber aRightArg) {
        for (int i = aFirst; i < aLimit; i += aStep) {
            aData[i] = aFunc.invoke(aLeftArg.get(i), aRightArg);
        }
    }

    protected static void invoke(final ComplexNumber[] aData, final int aFirst, final int aLimit, final int aStep, final Access1D<ComplexNumber> anArg,
            final ParameterFunction<ComplexNumber> aFunc, final int aParam) {
        for (int i = aFirst; i < aLimit; i += aStep) {
            aData[i] = aFunc.invoke(anArg.get(i), aParam);
        }
    }

    protected static void invoke(final ComplexNumber[] aData, final int aFirst, final int aLimit, final int aStep, final Access1D<ComplexNumber> anArg,
            final UnaryFunction<ComplexNumber> aFunc) {
        for (int i = aFirst; i < aLimit; i += aStep) {
            aData[i] = aFunc.invoke(anArg.get(i));
        }
    }

    protected static void invoke(final ComplexNumber[] aData, final int aFirst, final int aLimit, final int aStep, final ComplexNumber aLeftArg,
            final BinaryFunction<ComplexNumber> aFunc, final Access1D<ComplexNumber> aRightArg) {
        for (int i = aFirst; i < aLimit; i += aStep) {
            aData[i] = aFunc.invoke(aLeftArg, aRightArg.get(i));
        }
    }

    protected static void invoke(final ComplexNumber[] aData, final int aFirst, final int aLimit, final int aStep, final VoidFunction<ComplexNumber> aVisitor) {
        for (int i = aFirst; i < aLimit; i += aStep) {
            aVisitor.invoke(aData[i]);
        }
    }

    public final ComplexNumber[] data;

    protected ComplexArray(final ComplexNumber[] data) {

        super();

        this.data = data;
    }

    protected ComplexArray(final int size) {

        super();

        data = new ComplexNumber[size];
        this.fill(0, size, 1, ComplexNumber.ZERO);
    }

    @Override
    public boolean equals(final Object anObj) {
        if (anObj instanceof ComplexArray) {
            return Arrays.equals(data, ((ComplexArray) anObj).data);
        } else {
            return super.equals(anObj);
        }
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }

    protected final ComplexNumber[] copyOfData() {
        return ArrayUtils.copyOf(data);
    }

    @Override
    protected double doubleValue(final int index) {
        return data[index].doubleValue();
    }

    @Override
    protected final void exchange(final int firstA, final int firstB, final int step, final int count) {
        ComplexArray.exchange(data, firstA, firstB, step, count);
    }

    @Override
    protected void fill(final Access1D<?> anArg) {
        ComplexArray.fill(data, anArg);
    }

    @Override
    protected final void fill(final int aFirst, final int aLimit, final Access1D<ComplexNumber> aLeftArg, final BinaryFunction<ComplexNumber> aFunc,
            final Access1D<ComplexNumber> aRightArg) {
        ComplexArray.invoke(data, aFirst, aLimit, 1, aLeftArg, aFunc, aRightArg);
    }

    @Override
    protected final void fill(final int aFirst, final int aLimit, final Access1D<ComplexNumber> aLeftArg, final BinaryFunction<ComplexNumber> aFunc,
            final ComplexNumber aRightArg) {
        ComplexArray.invoke(data, aFirst, aLimit, 1, aLeftArg, aFunc, aRightArg);
    }

    @Override
    protected final void fill(final int aFirst, final int aLimit, final ComplexNumber aLeftArg, final BinaryFunction<ComplexNumber> aFunc,
            final Access1D<ComplexNumber> aRightArg) {
        ComplexArray.invoke(data, aFirst, aLimit, 1, aLeftArg, aFunc, aRightArg);
    }

    @Override
    protected final void fill(final int first, final int limit, final int step, final ComplexNumber value) {
        ComplexArray.fill(data, first, limit, step, value);
    }

    @Override
    protected final ComplexNumber get(final int index) {
        return data[index];
    }

    @Override
    protected final int getIndexOfLargest(final int first, final int limit, final int step) {

        int retVal = first;
        double tmpLargest = ZERO;
        double tmpValue;

        for (int i = first; i < limit; i += step) {
            tmpValue = data[i].norm();
            if (tmpValue > tmpLargest) {
                tmpLargest = tmpValue;
                retVal = i;
            }
        }

        return retVal;
    }

    @Override
    protected final boolean isAbsolute(final int index) {
        return ComplexNumber.isAbsolute(data[index]);
    }

    @Override
    protected final boolean isInfinite(final int index) {
        return ComplexNumber.isInfinite(data[index]);
    }

    @Override
    protected final boolean isNaN(final int index) {
        return ComplexNumber.isNaN(data[index]);
    }

    @Override
    protected final boolean isPositive(final int index) {
        return ComplexNumber.isPositive(data[index]);
    }

    @Override
    protected final boolean isReal(final int index) {
        return ComplexNumber.isReal(data[index]);
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
    protected void modify(final int index, final Access1D<ComplexNumber> left, final BinaryFunction<ComplexNumber> function) {
        // TODO Auto-generated method stub
    }

    @Override
    protected void modify(final int index, final BinaryFunction<ComplexNumber> function, final Access1D<ComplexNumber> right) {
        // TODO Auto-generated method stub
    }

    @Override
    protected final void modify(final int aFirst, final int aLimit, final int aStep, final Access1D<ComplexNumber> aLeftArg,
            final BinaryFunction<ComplexNumber> aFunc) {
        ComplexArray.invoke(data, aFirst, aLimit, aStep, aLeftArg, aFunc, this);
    }

    @Override
    protected final void modify(final int aFirst, final int aLimit, final int aStep, final BinaryFunction<ComplexNumber> aFunc,
            final Access1D<ComplexNumber> aRightArg) {
        ComplexArray.invoke(data, aFirst, aLimit, aStep, this, aFunc, aRightArg);
    }

    @Override
    protected final void modify(final int aFirst, final int aLimit, final int aStep, final BinaryFunction<ComplexNumber> aFunc, final ComplexNumber aRightArg) {
        ComplexArray.invoke(data, aFirst, aLimit, aStep, this, aFunc, aRightArg);
    }

    @Override
    protected final void modify(final int aFirst, final int aLimit, final int aStep, final ComplexNumber aLeftArg, final BinaryFunction<ComplexNumber> aFunc) {
        ComplexArray.invoke(data, aFirst, aLimit, aStep, aLeftArg, aFunc, this);
    }

    @Override
    protected final void modify(final int first, final int limit, final int step, final ParameterFunction<ComplexNumber> func, final int param) {
        ComplexArray.invoke(data, first, limit, step, this, func, param);
    }

    @Override
    protected final void modify(final int first, final int limit, final int step, final UnaryFunction<ComplexNumber> func) {
        ComplexArray.invoke(data, first, limit, step, this, func);
    }

    @Override
    protected void modify(final int index, final UnaryFunction<ComplexNumber> func) {
        data[index] = func.invoke(data[index]);
    }

    /**
     * @see org.ojalgo.array.BasicArray#searchAscending(java.lang.Number)
     */
    @Override
    protected final int searchAscending(final ComplexNumber aNmbr) {
        return Arrays.binarySearch(data, aNmbr);
    }

    @Override
    protected final void set(final int index, final double value) {
        data[index] = ComplexNumber.makeReal(value);
    }

    @Override
    protected final void set(final int index, final Number value) {
        data[index] = TypeUtils.toComplexNumber(value);
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
    protected final ComplexNumber toScalar(final long index) {
        return data[(int) index];
    }

    @Override
    protected final void visit(final int first, final int limit, final int step, final VoidFunction<ComplexNumber> visitor) {
        ComplexArray.invoke(data, first, limit, step, visitor);
    }

    @Override
    protected final void visit(final int index, final VoidFunction<ComplexNumber> visitor) {
        visitor.invoke(data[index]);
    }

    @Override
    boolean isPrimitive() {
        return false;
    }

    @Override
    DenseArray<ComplexNumber> newInstance(final int capacity) {
        return new ComplexArray(capacity);
    }

}

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

import static org.ojalgo.constant.PrimitiveMath.*;

import java.util.Arrays;

import org.ojalgo.access.Access1D;
import org.ojalgo.function.FunctionUtils;
import org.ojalgo.machine.MemoryEstimator;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Scalar;

/**
 * A one- and/or arbitrary-dimensional array of {@linkplain org.ojalgo.scalar.ComplexNumber}.
 *
 * @author apete
 */
public class ComplexArray extends ReferenceTypeArray<ComplexNumber> {

    static final long ELEMENT_SIZE = MemoryEstimator.estimateObject(ComplexNumber.class);

    static final DenseFactory<ComplexNumber> FACTORY = new DenseFactory<ComplexNumber>() {

        @Override
        long getElementSize() {
            return ELEMENT_SIZE;
        }

        @Override
        DenseArray<ComplexNumber> make(final int size) {
            return ComplexArray.make(size);
        }

        @Override
        Scalar<ComplexNumber> zero() {
            return ComplexNumber.ZERO;
        }

    };

    public static final ComplexArray make(final int size) {
        return new ComplexArray(size);
    }

    public static final SegmentedArray<ComplexNumber> makeSegmented(final long count) {
        return SegmentedArray.make(FACTORY, count);
    }

    public static final ComplexArray wrap(final ComplexNumber[] data) {
        return new ComplexArray(data);
    }

    protected ComplexArray(final ComplexNumber[] data) {

        super(data);

    }

    protected ComplexArray(final int size) {

        super(new ComplexNumber[size]);

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

    public final void fillMatching(final Access1D<?> values) {
        final int tmpLimit = (int) FunctionUtils.min(this.count(), values.count());
        for (int i = 0; i < tmpLimit; i++) {
            data[i] = ComplexNumber.valueOf(values.get(i));
        }
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }

    @Override
    protected final void add(final int index, final double addend) {
        this.fillOne(index, this.get(index).add(this.valueOf(addend)));
    }

    @Override
    protected final void add(final int index, final Number addend) {
        this.fillOne(index, this.get(index).add(this.valueOf(addend)));
    }

    @Override
    protected int indexOfLargest(final int first, final int limit, final int step) {

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
    protected boolean isAbsolute(final int index) {
        return ComplexNumber.isAbsolute(data[index]);
    }

    @Override
    protected boolean isSmall(final int index, final double comparedTo) {
        return ComplexNumber.isSmall(comparedTo, data[index]);
    }

    @Override
    DenseArray<ComplexNumber> newInstance(final int capacity) {
        return new ComplexArray(capacity);
    }

    @Override
    ComplexNumber valueOf(final double value) {
        return ComplexNumber.valueOf(value);
    }

    @Override
    ComplexNumber valueOf(final Number number) {
        return ComplexNumber.valueOf(number);
    }

}

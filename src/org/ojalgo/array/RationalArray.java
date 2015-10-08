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

import java.util.Arrays;

import org.ojalgo.access.Access1D;
import org.ojalgo.function.FunctionUtils;
import org.ojalgo.machine.MemoryEstimator;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.scalar.Scalar;

/**
 * A one- and/or arbitrary-dimensional array of {@linkplain org.ojalgo.scalar.RationalNumber}.
 *
 * @author apete
 */
public class RationalArray extends ReferenceTypeArray<RationalNumber> {

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
        return SegmentedArray.make(FACTORY, count);
    }

    public static final RationalArray wrap(final RationalNumber[] data) {
        return new RationalArray(data);
    }

    protected RationalArray(final int size) {

        super(new RationalNumber[size]);

        this.fill(0, size, 1, RationalNumber.ZERO);
    }

    protected RationalArray(final RationalNumber[] data) {

        super(data);

    }

    @Override
    public boolean equals(final Object anObj) {
        if (anObj instanceof RationalArray) {
            return Arrays.equals(data, ((RationalArray) anObj).data);
        } else {
            return super.equals(anObj);
        }
    }

    public final void fillMatching(final Access1D<?> values) {
        final int tmpLimit = (int) FunctionUtils.min(this.count(), values.count());
        for (int i = 0; i < tmpLimit; i++) {
            data[i] = RationalNumber.valueOf(values.get(i));
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
    protected boolean isAbsolute(final int index) {
        return RationalNumber.isAbsolute(data[index]);
    }

    @Override
    protected boolean isSmall(final int index, final double comparedTo) {
        return RationalNumber.isSmall(comparedTo, data[index]);
    }

    @Override
    DenseArray<RationalNumber> newInstance(final int capacity) {
        return new RationalArray(capacity);
    }

    @Override
    final RationalNumber valueOf(final double value) {
        return RationalNumber.valueOf(value);
    }

    @Override
    final RationalNumber valueOf(final Number number) {
        return RationalNumber.valueOf(number);
    }

}

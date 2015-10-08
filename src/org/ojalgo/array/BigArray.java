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

import static org.ojalgo.constant.BigMath.*;

import java.math.BigDecimal;
import java.util.Arrays;

import org.ojalgo.access.Access1D;
import org.ojalgo.function.FunctionUtils;
import org.ojalgo.machine.MemoryEstimator;
import org.ojalgo.scalar.BigScalar;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.type.TypeUtils;

/**
 * A one- and/or arbitrary-dimensional array of {@linkplain java.math.BigDecimal}.
 *
 * @author apete
 */
public class BigArray extends ReferenceTypeArray<BigDecimal> {

    static final long ELEMENT_SIZE = MemoryEstimator.estimateObject(BigDecimal.class);

    static final DenseFactory<BigDecimal> FACTORY = new DenseFactory<BigDecimal>() {

        @Override
        long getElementSize() {
            return ELEMENT_SIZE;
        }

        @Override
        DenseArray<BigDecimal> make(final int size) {
            return BigArray.make(size);
        }

        @Override
        Scalar<BigDecimal> zero() {
            return BigScalar.ZERO;
        }

    };

    public static final BigArray make(final int size) {
        return new BigArray(size);
    }

    public static final SegmentedArray<BigDecimal> makeSegmented(final long count) {
        return SegmentedArray.make(FACTORY, count);
    }

    public static final BigArray wrap(final BigDecimal[] data) {
        return new BigArray(data);
    }

    protected BigArray(final BigDecimal[] data) {

        super(data);

    }

    protected BigArray(final int size) {

        super(new BigDecimal[size]);

        this.fill(0, size, 1, ZERO);
    }

    @Override
    public boolean equals(final Object other) {
        if (other instanceof BigArray) {
            return Arrays.equals(data, ((BigArray) other).data);
        } else {
            return super.equals(other);
        }
    }

    public final void fillMatching(final Access1D<?> values) {
        final int tmpLimit = (int) FunctionUtils.min(this.count(), values.count());
        for (int i = 0; i < tmpLimit; i++) {
            data[i] = TypeUtils.toBigDecimal(values.get(i));
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
        BigDecimal tmpLargest = ZERO;
        BigDecimal tmpValue;

        for (int i = first; i < limit; i += step) {
            tmpValue = data[i].abs();
            if (tmpValue.compareTo(tmpLargest) == 1) {
                tmpLargest = tmpValue;
                retVal = i;
            }
        }

        return retVal;
    }

    @Override
    protected boolean isAbsolute(final int index) {
        return BigScalar.isAbsolute(data[index]);
    }

    @Override
    protected boolean isSmall(final int index, final double comparedTo) {
        return BigScalar.isSmall(comparedTo, data[index]);
    }

    @Override
    DenseArray<BigDecimal> newInstance(final int capacity) {
        return new BigArray(capacity);
    }

    @Override
    BigDecimal valueOf(final double value) {
        return BigDecimal.valueOf(value);
    }

    @Override
    BigDecimal valueOf(final Number number) {
        return TypeUtils.toBigDecimal(number);
    }

}

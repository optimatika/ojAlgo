/*
 * Copyright 1997-2016 Optimatika (www.optimatika.se)
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
import java.util.Comparator;

import org.ojalgo.access.Access1D;
import org.ojalgo.function.FunctionSet;
import org.ojalgo.function.FunctionUtils;
import org.ojalgo.function.RationalFunction;
import org.ojalgo.function.aggregator.AggregatorSet;
import org.ojalgo.function.aggregator.RationalAggregator;
import org.ojalgo.machine.MemoryEstimator;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.scalar.Scalar;

/**
 * A one- and/or arbitrary-dimensional array of {@linkplain org.ojalgo.scalar.RationalNumber}.
 *
 * @author apete
 */
public class RationalArray extends ScalarArray<RationalNumber> {

    public static final DenseArray.Factory<RationalNumber> FACTORY = new DenseArray.Factory<RationalNumber>() {

        @Override
        long getElementSize() {
            return ELEMENT_SIZE;
        }

        @Override
        PlainArray<RationalNumber> make(final long size) {
            return RationalArray.make((int) size);
        }

        @Override
        public FunctionSet<RationalNumber> function() {
            return RationalFunction.getSet();
        }

        @Override
        public AggregatorSet<RationalNumber> aggregator() {
            return RationalAggregator.getSet();
        }

        @Override
        public Scalar.Factory<RationalNumber> scalar() {
            return RationalNumber.FACTORY;
        }

    };

    static final long ELEMENT_SIZE = MemoryEstimator.estimateObject(RationalNumber.class);

    public static final RationalArray make(final int size) {
        return new RationalArray(size);
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
    protected boolean isAbsolute(final int index) {
        return RationalNumber.isAbsolute(data[index]);
    }

    @Override
    protected boolean isSmall(final int index, final double comparedTo) {
        return RationalNumber.isSmall(comparedTo, data[index]);
    }

    @Override
    protected final void sortAscending() {
        Arrays.parallelSort(data);
    }

    @Override
    protected void sortDescending() {
        Arrays.parallelSort(data, Comparator.reverseOrder());
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

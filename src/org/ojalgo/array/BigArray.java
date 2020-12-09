/*
 * Copyright 1997-2020 Optimatika
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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;

import org.ojalgo.array.operation.AMAX;
import org.ojalgo.array.operation.AXPY;
import org.ojalgo.function.BigFunction;
import org.ojalgo.function.FunctionSet;
import org.ojalgo.function.aggregator.AggregatorSet;
import org.ojalgo.function.aggregator.BigAggregator;
import org.ojalgo.machine.MemoryEstimator;
import org.ojalgo.scalar.BigScalar;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Mutate1D;

/**
 * A one- and/or arbitrary-dimensional array of {@linkplain java.math.BigDecimal}.
 *
 * @author apete
 */
public class BigArray extends ReferenceTypeArray<BigDecimal> {

    public static final DenseArray.Factory<BigDecimal> FACTORY = new DenseArray.Factory<BigDecimal>() {

        @Override
        public AggregatorSet<BigDecimal> aggregator() {
            return BigAggregator.getSet();
        }

        @Override
        public FunctionSet<BigDecimal> function() {
            return BigFunction.getSet();
        }

        @Override
        public Scalar.Factory<BigDecimal> scalar() {
            return BigScalar.FACTORY;
        }

        @Override
        long getElementSize() {
            return ELEMENT_SIZE;
        }

        @Override
        PlainArray<BigDecimal> makeDenseArray(final long size) {
            return BigArray.make((int) size);
        }

    };

    static final long ELEMENT_SIZE = MemoryEstimator.estimateObject(BigDecimal.class);

    public static final BigArray make(final int size) {
        return new BigArray(size);
    }

    public static final BigArray wrap(final BigDecimal... data) {
        return new BigArray(data);
    }

    protected BigArray(final BigDecimal[] data) {
        super(FACTORY, data);
    }

    protected BigArray(final int size) {
        super(FACTORY, size);
    }

    @Override
    public final void axpy(final double a, final Mutate1D.Modifiable<?> y) {
        AXPY.invoke(y, a, data);
    }

    @Override
    public final void sortAscending() {
        Arrays.parallelSort(data);
    }

    @Override
    public void sortDescending() {
        Arrays.parallelSort(data, Comparator.reverseOrder());
    }

    @Override
    protected final void add(final int index, final Comparable<?> addend) {
        this.fillOne(index, this.get(index).add(this.valueOf(addend)));
    }

    @Override
    protected final void add(final int index, final double addend) {
        this.fillOne(index, this.get(index).add(this.valueOf(addend)));
    }

    @Override
    protected final void add(final int index, final float addend) {
        this.fillOne(index, this.get(index).add(this.valueOf(addend)));
    }

    @Override
    protected final double doubleValue(final int index) {
        return data[index].doubleValue();
    }

    @Override
    protected final void fillOne(final int index, final Access1D<?> values, final long valueIndex) {
        data[index] = this.valueOf(values.get(valueIndex));
    }

    @Override
    protected final float floatValue(final int index) {
        return data[index].floatValue();
    }

    @Override
    protected final int indexOfLargest(final int first, final int limit, final int step) {
        return AMAX.invoke(data, first, limit, step);
    }

    @Override
    protected boolean isAbsolute(final int index) {
        return BigScalar.isAbsolute(data[index]);
    }

    @Override
    protected boolean isSmall(final int index, final double comparedTo) {
        return BigScalar.isSmall(comparedTo, data[index]);
    }

}

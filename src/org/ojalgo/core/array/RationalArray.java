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
package org.ojalgo.core.array;

import org.ojalgo.core.function.FunctionSet;
import org.ojalgo.core.function.RationalFunction;
import org.ojalgo.core.function.aggregator.AggregatorSet;
import org.ojalgo.core.function.aggregator.RationalAggregator;
import org.ojalgo.core.machine.MemoryEstimator;
import org.ojalgo.core.scalar.RationalNumber;
import org.ojalgo.core.scalar.Scalar;

/**
 * A one- and/or arbitrary-dimensional array of {@linkplain org.ojalgo.core.scalar.RationalNumber}.
 *
 * @author apete
 */
public class RationalArray extends ScalarArray<RationalNumber> {

    public static final DenseArray.Factory<RationalNumber> FACTORY = new DenseArray.Factory<RationalNumber>() {

        @Override
        public AggregatorSet<RationalNumber> aggregator() {
            return RationalAggregator.getSet();
        }

        @Override
        public FunctionSet<RationalNumber> function() {
            return RationalFunction.getSet();
        }

        @Override
        public Scalar.Factory<RationalNumber> scalar() {
            return RationalNumber.FACTORY;
        }

        @Override
        long getElementSize() {
            return ELEMENT_SIZE;
        }

        @Override
        PlainArray<RationalNumber> makeDenseArray(final long size) {
            return RationalArray.make((int) size);
        }

    };

    static final long ELEMENT_SIZE = MemoryEstimator.estimateObject(RationalNumber.class);

    public static final RationalArray make(final int size) {
        return new RationalArray(size);
    }

    public static final RationalArray wrap(final RationalNumber... data) {
        return new RationalArray(data);
    }

    protected RationalArray(final int size) {
        super(FACTORY, size);
    }

    protected RationalArray(final RationalNumber[] data) {
        super(FACTORY, data);
    }

}

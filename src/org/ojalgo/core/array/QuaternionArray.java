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
import org.ojalgo.core.function.QuaternionFunction;
import org.ojalgo.core.function.aggregator.AggregatorSet;
import org.ojalgo.core.function.aggregator.QuaternionAggregator;
import org.ojalgo.core.machine.MemoryEstimator;
import org.ojalgo.core.scalar.Quaternion;
import org.ojalgo.core.scalar.Scalar;

/**
 * A one- and/or arbitrary-dimensional array of {@linkplain org.ojalgo.core.scalar.Quaternion}.
 *
 * @author apete
 */
public class QuaternionArray extends ScalarArray<Quaternion> {

    public static final DenseArray.Factory<Quaternion> FACTORY = new DenseArray.Factory<Quaternion>() {

        @Override
        public AggregatorSet<Quaternion> aggregator() {
            return QuaternionAggregator.getSet();
        }

        @Override
        public FunctionSet<Quaternion> function() {
            return QuaternionFunction.getSet();
        }

        @Override
        public Scalar.Factory<Quaternion> scalar() {
            return Quaternion.FACTORY;
        }

        @Override
        long getElementSize() {
            return ELEMENT_SIZE;
        }

        @Override
        PlainArray<Quaternion> makeDenseArray(final long size) {
            return QuaternionArray.make((int) size);
        }

    };

    static final long ELEMENT_SIZE = MemoryEstimator.estimateObject(Quaternion.class);

    public static final QuaternionArray make(final int size) {
        return new QuaternionArray(size);
    }

    public static final QuaternionArray wrap(final Quaternion... data) {
        return new QuaternionArray(data);
    }

    protected QuaternionArray(final int size) {
        super(FACTORY, size);
    }

    protected QuaternionArray(final Quaternion[] data) {
        super(FACTORY, data);
    }

}

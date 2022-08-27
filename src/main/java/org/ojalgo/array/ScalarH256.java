/*
 * Copyright 1997-2022 Optimatika
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

import org.ojalgo.function.FunctionSet;
import org.ojalgo.function.QuaternionFunction;
import org.ojalgo.function.aggregator.AggregatorSet;
import org.ojalgo.function.aggregator.QuaternionAggregator;
import org.ojalgo.machine.MemoryEstimator;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.scalar.Scalar;

/**
 * A one- and/or arbitrary-dimensional array of {@linkplain org.ojalgo.scalar.Quaternion}.
 *
 * @author apete
 */
public class ScalarH256 extends ScalarArray<Quaternion> {

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
            return ScalarH256.make((int) size);
        }

    };

    static final long ELEMENT_SIZE = MemoryEstimator.estimateObject(Quaternion.class);

    public static final ScalarH256 make(final int size) {
        return new ScalarH256(size);
    }

    public static final ScalarH256 wrap(final Quaternion... data) {
        return new ScalarH256(data);
    }

    protected ScalarH256(final int size) {
        super(FACTORY, size);
    }

    protected ScalarH256(final Quaternion[] data) {
        super(FACTORY, data);
    }

}

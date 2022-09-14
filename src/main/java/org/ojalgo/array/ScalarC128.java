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

import org.ojalgo.function.ComplexFunction;
import org.ojalgo.function.FunctionSet;
import org.ojalgo.function.aggregator.AggregatorSet;
import org.ojalgo.function.aggregator.ComplexAggregator;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.type.math.MathType;

/**
 * A one- and/or arbitrary-dimensional array of {@linkplain org.ojalgo.scalar.ComplexNumber}.
 *
 * @author apete
 */
public class ScalarC128 extends ScalarArray<ComplexNumber> {

    public static final DenseArray.Factory<ComplexNumber> FACTORY = new DenseArray.Factory<>() {

        @Override
        public AggregatorSet<ComplexNumber> aggregator() {
            return ComplexAggregator.getSet();
        }

        @Override
        public FunctionSet<ComplexNumber> function() {
            return ComplexFunction.getSet();
        }

        @Override
        public Scalar.Factory<ComplexNumber> scalar() {
            return ComplexNumber.FACTORY;
        }

        @Override
        MathType getMathType() {
            return MathType.C128;
        }

        @Override
        PlainArray<ComplexNumber> makeDenseArray(final long size) {
            return ScalarC128.make((int) size);
        }

    };

    public static ScalarC128 make(final int size) {
        return new ScalarC128(size);
    }

    public static ScalarC128 wrap(final ComplexNumber... data) {
        return new ScalarC128(data);
    }

    protected ScalarC128(final ComplexNumber[] data) {
        super(FACTORY, data);
    }

    protected ScalarC128(final int size) {
        super(FACTORY, size);
    }

}

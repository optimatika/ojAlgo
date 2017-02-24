/*
 * Copyright 1997-2017 Optimatika (www.optimatika.se)
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
package org.ojalgo.tensor;

import org.ojalgo.access.AccessAnyD;
import org.ojalgo.algebra.NormedVectorSpace;
import org.ojalgo.array.Primitive64Array;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.function.aggregator.PrimitiveAggregator;

public interface Tensor<N extends Number> extends AccessAnyD<N>, NormedVectorSpace<Tensor<N>, N> {

    static Tensor<Double> makePrimitive(final int rank, final int dimensions) {
        return new AnyTensor<Double>(rank, dimensions, Primitive64Array.FACTORY, PrimitiveFunction.getSet(), PrimitiveAggregator.getSet());
    }

    default long count(final int dimension) {
        return this.dimensions();
    }

    int dimensions();

    int rank();

}

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

import java.util.Arrays;

import org.ojalgo.array.ArrayAnyD;
import org.ojalgo.array.DenseArray;

final class AnyTensor<N extends Number> implements Tensor<N> {

    private final ArrayAnyD<N> myArray;
    private final long myDimension;
    private final int myRank;
    private final long[] myShape;

    AnyTensor(final int rank, final long dimension, final DenseArray.Factory<N> factory) {

        super();

        myRank = rank;
        myDimension = dimension;
        myShape = new long[rank];
        Arrays.fill(myShape, dimension);

        myArray = ArrayAnyD.factory(factory).makeZero(myShape);
    }

    public long dimension() {
        return myDimension;
    }

    public double doubleValue(final long[] ref) {
        return myArray.doubleValue(ref);
    }

    public N get(final long[] ref) {
        return myArray.get(ref);
    }

    public int rank() {
        return myRank;
    }

    public long[] shape() {
        return myShape;
    }

}

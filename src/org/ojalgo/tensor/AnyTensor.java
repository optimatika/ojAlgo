/*
 * Copyright 1997-2018 Optimatika
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
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.aggregator.AggregatorFunction;

final class AnyTensor<N extends Number> implements Tensor<N> {

    private final ArrayAnyD<N> myArray;
    private final DenseArray.Factory<N> myArrayFactory;
    private final int myDimensions;
    private final int myRank;

    AnyTensor(final int rank, final int dimensions, final DenseArray.Factory<N> arrayFactory) {

        super();

        myRank = rank;
        myDimensions = dimensions;
        final long[] shape = new long[rank];
        Arrays.fill(shape, dimensions);

        myArray = ArrayAnyD.factory(arrayFactory).makeZero(shape);

        myArrayFactory = arrayFactory;
    }

    public Tensor<N> add(final Tensor<N> addend) {

        final AnyTensor<N> retVal = new AnyTensor<>(myRank, myDimensions, myArrayFactory);
        final ArrayAnyD<N> retArray = retVal.getArray();

        retArray.loopAll((final long i) -> retArray.set(i, this.doubleValue(i) + addend.doubleValue(i)));

        return retVal;
    }

    public Tensor<N> conjugate() {

        final AnyTensor<N> retVal = new AnyTensor<>(myRank, myDimensions, myArrayFactory);
        final ArrayAnyD<N> retArray = retVal.getArray();

        final long[] traspRef = retVal.shape().clone();
        final long max = myDimensions - 1L;

        retArray.loopAll((final long[] ref) -> {
            for (int i = 0; i < traspRef.length; i++) {
                traspRef[i] = max - ref[i];
            }
            retArray.set(traspRef, myArray.doubleValue(ref));
        });

        return retVal;
    }

    public long count() {
        return (long) Math.pow(myDimensions, myRank);
    }

    public int dimensions() {
        return myDimensions;
    }

    public double doubleValue(final long[] ref) {
        return myArray.doubleValue(ref);
    }

    public N get(final long[] ref) {
        return myArray.get(ref);
    }

    public boolean isSmall(final double comparedTo) {
        return myArray.isAllSmall(comparedTo);
    }

    public Tensor<N> multiply(final double scalarMultiplicand) {

        final AnyTensor<N> retVal = new AnyTensor<>(myRank, myDimensions, myArrayFactory);
        final ArrayAnyD<N> retArray = retVal.getArray();

        retArray.modifyAll(myArrayFactory.function().multiply().second(scalarMultiplicand));

        return retVal;
    }

    public Tensor<N> multiply(final N scalarMultiplicand) {

        final AnyTensor<N> retVal = new AnyTensor<>(myRank, myDimensions, myArrayFactory);
        final ArrayAnyD<N> retArray = retVal.getArray();

        retArray.modifyAll(myArrayFactory.function().multiply().second(scalarMultiplicand));

        return retVal;
    }

    public Tensor<N> negate() {

        final AnyTensor<N> retVal = new AnyTensor<>(myRank, myDimensions, myArrayFactory);
        final ArrayAnyD<N> retArray = retVal.getArray();

        retArray.modifyAll(myArrayFactory.function().negate());

        return retVal;
    }

    public double norm() {
        final AggregatorFunction<N> tmpNorm2 = myArrayFactory.aggregator().norm2();
        myArray.visitAll(tmpNorm2);
        return tmpNorm2.doubleValue();
    }

    public int rank() {
        return myRank;
    }

    public long[] shape() {
        return myArray.shape();
    }

    public Tensor<N> signum() {
        return this.multiply(PrimitiveMath.ONE / this.norm());
    }

    ArrayAnyD<N> getArray() {
        return myArray;
    }

}

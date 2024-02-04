/*
 * Copyright 1997-2024 Optimatika
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
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.structure.AccessAnyD;
import org.ojalgo.structure.FactoryAnyD;
import org.ojalgo.structure.MutateAnyD;
import org.ojalgo.type.math.MathType;

public final class AnyTensor<N extends Comparable<N>> extends ArrayBasedTensor<N, AnyTensor<N>> implements AccessAnyD<N>, MutateAnyD.Receiver<N> {

    static final class Factory<N extends Comparable<N>> extends ArrayBasedTensor.Factory<N> implements FactoryAnyD<AnyTensor<N>> {

        private final ArrayAnyD.Factory<N> myFactory;

        Factory(final DenseArray.Factory<N> arrayFactory) {

            super(arrayFactory);

            myFactory = ArrayAnyD.factory(arrayFactory);
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Factory)) {
                return false;
            }
            Factory other = (Factory) obj;
            if (myFactory == null) {
                if (other.myFactory != null) {
                    return false;
                }
            } else if (!myFactory.equals(other.myFactory)) {
                return false;
            }
            return true;
        }

        public MathType getMathType() {
            return myFactory.getMathType();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            return prime * result + (myFactory == null ? 0 : myFactory.hashCode());
        }

        public AnyTensor<N> make(final long... structure) {

            int rank = structure.length;
            long dimensions = structure[0];

            if (rank <= 0 || dimensions <= 0L) {
                throw new IllegalArgumentException();
            }
            for (int i = 1; i < rank; i++) {
                if (structure[i] != dimensions) {
                    throw new IllegalArgumentException();
                }
            }

            return new AnyTensor<>(myFactory, rank, Math.toIntExact(dimensions));
        }

    }

    public static <N extends Comparable<N>> TensorFactoryAnyD<N, AnyTensor<N>> factory(final DenseArray.Factory<N> arrayFactory) {
        return new TensorFactoryAnyD<>(new AnyTensor.Factory<>(arrayFactory));
    }

    private final ArrayAnyD<N> myArray;
    private final ArrayAnyD.Factory<N> myFactory;

    AnyTensor(final ArrayAnyD.Factory<N> factory, final int rank, final int dimensions) {

        super(rank, dimensions, factory.function(), factory.scalar());

        long[] shape = new long[rank];
        Arrays.fill(shape, dimensions);

        myFactory = factory;
        myArray = factory.make(shape);
    }

    public AnyTensor<N> add(final AnyTensor<N> addend) {

        AnyTensor<N> retVal = this.newSameShape();

        this.add(retVal.getArray(), myArray, addend);

        return retVal;
    }

    public byte byteValue(final long... ref) {
        return myArray.byteValue(ref);
    }

    public AnyTensor<N> conjugate() {

        AnyTensor<N> retVal = this.newSameShape();
        ArrayAnyD<N> array = retVal.getArray();

        long[] transp = retVal.shape().clone();
        int max = this.rank() - 1;

        array.loopAllReferences(ref -> {
            for (int i = 0; i < transp.length; i++) {
                transp[max - i] = ref[i];
            }
            array.set(transp, myArray.doubleValue(ref));
        });

        return retVal;
    }

    public long count(final int dimension) {
        return myArray.count(dimension);
    }

    public double doubleValue(final long... ref) {
        return myArray.doubleValue(ref);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj) || !(obj instanceof AnyTensor)) {
            return false;
        }
        AnyTensor other = (AnyTensor) obj;
        if (myArray == null) {
            if (other.myArray != null) {
                return false;
            }
        } else if (!myArray.equals(other.myArray)) {
            return false;
        }
        if (myFactory == null) {
            if (other.myFactory != null) {
                return false;
            }
        } else if (!myFactory.equals(other.myFactory)) {
            return false;
        }
        return true;
    }

    public void fillSet(final int dimension, final long dimensionalIndex, final N value) {
        myArray.fillSet(dimension, dimensionalIndex, value);
    }

    public void fillSet(final int dimension, final long dimensionalIndex, final NullaryFunction<?> supplier) {
        myArray.fillSet(dimension, dimensionalIndex, supplier);
    }

    public void fillSet(final long[] initial, final int dimension, final N value) {
        myArray.fillSet(initial, dimension, value);
    }

    public void fillSet(final long[] initial, final int dimension, final NullaryFunction<?> supplier) {
        myArray.fillSet(initial, dimension, supplier);
    }

    public float floatValue(final long... ref) {
        return myArray.floatValue(ref);
    }

    public N get(final long... ref) {
        return myArray.get(ref);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (myArray == null ? 0 : myArray.hashCode());
        return prime * result + (myFactory == null ? 0 : myFactory.hashCode());
    }

    public int intValue(final long... ref) {
        return myArray.intValue(ref);
    }

    public long longValue(final long... ref) {
        return myArray.longValue(ref);
    }

    public AnyTensor<N> multiply(final double scalarMultiplicand) {

        AnyTensor<N> retVal = this.newSameShape();

        this.multiply(retVal.getArray(), scalarMultiplicand, myArray);

        return retVal;
    }

    public AnyTensor<N> multiply(final N scalarMultiplicand) {

        AnyTensor<N> retVal = this.newSameShape();

        this.multiply(retVal.getArray(), scalarMultiplicand, myArray);

        return retVal;
    }

    public AnyTensor<N> negate() {

        AnyTensor<N> retVal = this.newSameShape();

        this.negate(retVal.getArray(), myArray);

        return retVal;
    }

    public double norm() {
        return this.norm(myArray);
    }

    public void set(final long[] reference, final byte value) {
        myArray.set(reference, value);
    }

    public void set(final long[] reference, final Comparable<?> value) {
        myArray.set(reference, value);
    }

    public void set(final long[] reference, final double value) {
        myArray.set(reference, value);
    }

    public void set(final long[] reference, final float value) {
        myArray.set(reference, value);
    }

    public void set(final long[] reference, final int value) {
        myArray.set(reference, value);
    }

    public void set(final long[] reference, final long value) {
        myArray.set(reference, value);
    }

    public void set(final long[] reference, final short value) {
        myArray.set(reference, value);
    }

    public long[] shape() {
        return myArray.shape();
    }

    public short shortValue(final long... ref) {
        return myArray.shortValue(ref);
    }

    @Override
    public String toString() {
        return AccessAnyD.toString(myArray);
    }

    ArrayAnyD<N> getArray() {
        return myArray;
    }

    @Override
    AnyTensor<N> newSameShape() {
        return new AnyTensor<>(myFactory, this.rank(), this.dimensions());
    }

}

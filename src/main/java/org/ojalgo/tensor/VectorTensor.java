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
package org.ojalgo.tensor;

import org.ojalgo.array.Array1D;
import org.ojalgo.array.DenseArray;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Factory1D;
import org.ojalgo.structure.Mutate1D;

public final class VectorTensor<N extends Comparable<N>> extends ArrayBasedTensor<N, VectorTensor<N>> implements Access1D<N>, Mutate1D.Receiver<N> {

    static final class Factory<N extends Comparable<N>> extends ArrayBasedTensor.Factory<N> implements Factory1D<VectorTensor<N>> {

        private final Array1D.Factory<N> myFactory;

        Factory(final DenseArray.Factory<N> arrayFactory) {

            super(arrayFactory);

            myFactory = Array1D.factory(arrayFactory);
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!super.equals(obj) || !(obj instanceof Factory)) {
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

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            return prime * result + (myFactory == null ? 0 : myFactory.hashCode());
        }

        public VectorTensor<N> make(final long count) {
            return new VectorTensor<>(myFactory, Math.toIntExact(count));
        }

    }

    public static <N extends Comparable<N>> TensorFactory1D<N, VectorTensor<N>> factory(final DenseArray.Factory<N> arrayFactory) {
        return new TensorFactory1D<>(new VectorTensor.Factory<>(arrayFactory));
    }

    private final Array1D<N> myArray;
    private final Array1D.Factory<N> myFactory;

    VectorTensor(final Array1D.Factory<N> factory, final int dimensions) {

        super(1, dimensions, factory.function(), factory.scalar());

        myFactory = factory;
        myArray = factory.make(dimensions);
    }

    public VectorTensor<N> add(final VectorTensor<N> addend) {

        VectorTensor<N> retVal = this.newSameShape();

        this.add(retVal.getArray(), myArray, addend);

        return retVal;
    }

    public byte byteValue(final long index) {
        return myArray.byteValue(index);
    }

    public VectorTensor<N> conjugate() {
        return this;
    }

    public long count() {
        return myArray.count();
    }

    public double doubleValue(final long index) {
        return myArray.doubleValue(index);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj) || !(obj instanceof VectorTensor)) {
            return false;
        }
        VectorTensor other = (VectorTensor) obj;
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

    public void fillOne(final long index, final N value) {
        myArray.fillOne(index, value);
    }

    public void fillOne(final long index, final NullaryFunction<?> supplier) {
        myArray.fillOne(index, supplier);
    }

    public float floatValue(final long index) {
        return myArray.floatValue(index);
    }

    public N get(final long index) {
        return myArray.get(index);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (myArray == null ? 0 : myArray.hashCode());
        return prime * result + (myFactory == null ? 0 : myFactory.hashCode());
    }

    public int intValue(final long index) {
        return myArray.intValue(index);
    }

    public long longValue(final long index) {
        return myArray.longValue(index);
    }

    public VectorTensor<N> multiply(final double scalarMultiplicand) {

        VectorTensor<N> retVal = this.newSameShape();

        this.multiply(retVal.getArray(), scalarMultiplicand, myArray);

        return retVal;
    }

    public VectorTensor<N> multiply(final N scalarMultiplicand) {

        VectorTensor<N> retVal = this.newSameShape();

        this.multiply(retVal.getArray(), scalarMultiplicand, myArray);

        return retVal;
    }

    public VectorTensor<N> negate() {

        VectorTensor<N> retVal = this.newSameShape();

        this.negate(retVal.getArray(), myArray);

        return retVal;
    }

    public double norm() {
        return this.norm(myArray);
    }

    public void set(final long index, final byte value) {
        myArray.set(index, value);
    }

    public void set(final long index, final Comparable<?> value) {
        myArray.set(index, value);
    }

    public void set(final long index, final double value) {
        myArray.set(index, value);
    }

    public void set(final long index, final float value) {
        myArray.set(index, value);
    }

    public void set(final long index, final int value) {
        myArray.set(index, value);
    }

    public void set(final long index, final long value) {
        myArray.set(index, value);
    }

    public void set(final long index, final short value) {
        myArray.set(index, value);
    }

    public short shortValue(final long index) {
        return myArray.shortValue(index);
    }

    @Override
    public String toString() {
        return Access1D.toString(myArray);
    }

    Array1D<N> getArray() {
        return myArray;
    }

    @Override
    VectorTensor<N> newSameShape() {
        return new VectorTensor<>(myFactory, this.dimensions());
    }

}

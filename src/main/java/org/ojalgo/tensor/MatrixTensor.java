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

import org.ojalgo.array.Array2D;
import org.ojalgo.array.DenseArray;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Factory2D;
import org.ojalgo.structure.Mutate2D;
import org.ojalgo.type.math.MathType;

public final class MatrixTensor<N extends Comparable<N>> extends ArrayBasedTensor<N, MatrixTensor<N>> implements Access2D<N>, Mutate2D.Receiver<N> {

    static final class Factory<N extends Comparable<N>> extends ArrayBasedTensor.Factory<N> implements Factory2D<MatrixTensor<N>> {

        private final Array2D.Factory<N> myFactory;

        Factory(final DenseArray.Factory<N> arrayFactory) {

            super(arrayFactory);

            myFactory = Array2D.factory(arrayFactory);
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
        public MathType getMathType() {
            return myFactory.getMathType();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            return prime * result + (myFactory == null ? 0 : myFactory.hashCode());
        }

        @Override
        public MatrixTensor<N> make(final long rows, final long columns) {
            if (rows != columns) {
                throw new IllegalArgumentException();
            }
            return new MatrixTensor<>(myFactory, Math.toIntExact(rows));
        }

    }

    public static <N extends Comparable<N>> TensorFactory2D<N, MatrixTensor<N>> factory(final DenseArray.Factory<N> arrayFactory) {
        return new TensorFactory2D<>(new MatrixTensor.Factory<>(arrayFactory));
    }

    private final Array2D<N> myArray;
    private final Array2D.Factory<N> myFactory;

    MatrixTensor(final Array2D.Factory<N> factory, final int dimensions) {

        super(2, dimensions, factory.function(), factory.scalar());

        myFactory = factory;
        myArray = factory.make(dimensions, dimensions);
    }

    @Override
    public MatrixTensor<N> add(final MatrixTensor<N> addend) {

        MatrixTensor<N> retVal = this.newSameShape();

        this.add(retVal.getArray(), myArray, addend);

        return retVal;
    }

    @Override
    public byte byteValue(final int row, final int col) {
        return myArray.byteValue(row, col);
    }

    @Override
    public byte byteValue(final long row, final long col) {
        return myArray.byteValue(row, col);
    }

    @Override
    public MatrixTensor<N> conjugate() {

        MatrixTensor<N> retVal = this.newSameShape();
        Array2D<N> array = retVal.getArray();

        for (int j = 0; j < this.dimensions(); j++) {
            for (int i = 0; i < this.dimensions(); i++) {
                array.set(i, j, myArray.get(j, i));
            }
        }

        return retVal;
    }

    @Override
    public long count() {
        return myArray.count();
    }

    @Override
    public long countColumns() {
        return myArray.countColumns();
    }

    @Override
    public long countRows() {
        return myArray.countRows();
    }

    @Override
    public double doubleValue(final int row, final int col) {
        return myArray.doubleValue(row, col);
    }

    @Override
    public double doubleValue(final long row, final long col) {
        return myArray.doubleValue(row, col);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj) || !(obj instanceof MatrixTensor)) {
            return false;
        }
        MatrixTensor other = (MatrixTensor) obj;
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

    @Override
    public float floatValue(final int row, final int col) {
        return myArray.floatValue(row, col);
    }

    @Override
    public float floatValue(final long row, final long col) {
        return myArray.floatValue(row, col);
    }

    @Override
    public N get(final long row, final long col) {
        return myArray.get(row, col);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (myArray == null ? 0 : myArray.hashCode());
        return prime * result + (myFactory == null ? 0 : myFactory.hashCode());
    }

    @Override
    public int intValue(final int row, final int col) {
        return myArray.intValue(row, col);
    }

    @Override
    public int intValue(final long row, final long col) {
        return myArray.intValue(row, col);
    }

    @Override
    public long longValue(final int row, final int col) {
        return myArray.longValue(row, col);
    }

    @Override
    public long longValue(final long row, final long col) {
        return myArray.longValue(row, col);
    }

    @Override
    public MatrixTensor<N> multiply(final double scalarMultiplicand) {

        MatrixTensor<N> retVal = this.newSameShape();

        this.multiply(retVal.getArray(), scalarMultiplicand, myArray);

        return retVal;
    }

    @Override
    public MatrixTensor<N> multiply(final N scalarMultiplicand) {

        MatrixTensor<N> retVal = this.newSameShape();

        this.multiply(retVal.getArray(), scalarMultiplicand, myArray);

        return retVal;
    }

    @Override
    public MatrixTensor<N> negate() {

        MatrixTensor<N> retVal = this.newSameShape();

        this.negate(retVal.getArray(), myArray);

        return retVal;
    }

    @Override
    public double norm() {
        return this.norm(myArray);
    }

    @Override
    public void set(final int row, final int col, final byte value) {
        myArray.set(row, col, value);
    }

    @Override
    public void set(final int row, final int col, final double value) {
        myArray.set(row, col, value);
    }

    @Override
    public void set(final int row, final int col, final float value) {
        myArray.set(row, col, value);
    }

    @Override
    public void set(final int row, final int col, final int value) {
        myArray.set(row, col, value);
    }

    @Override
    public void set(final int row, final int col, final long value) {
        myArray.set(row, col, value);
    }

    @Override
    public void set(final int row, final int col, final short value) {
        myArray.set(row, col, value);
    }

    @Override
    public void set(final long row, final long col, final byte value) {
        myArray.set(row, col, value);
    }

    @Override
    public void set(final long row, final long col, final Comparable<?> value) {
        myArray.set(row, col, value);
    }

    @Override
    public void set(final long row, final long col, final double value) {
        myArray.set(row, col, value);
    }

    @Override
    public void set(final long row, final long col, final float value) {
        myArray.set(row, col, value);
    }

    @Override
    public void set(final long row, final long col, final int value) {
        myArray.set(row, col, value);
    }

    @Override
    public void set(final long row, final long col, final long value) {
        myArray.set(row, col, value);
    }

    @Override
    public void set(final long row, final long col, final short value) {
        myArray.set(row, col, value);
    }

    @Override
    public short shortValue(final int row, final int col) {
        return myArray.shortValue(row, col);
    }

    @Override
    public short shortValue(final long row, final long col) {
        return myArray.shortValue(row, col);
    }

    @Override
    public String toString() {
        return Access2D.toString(myArray);
    }

    Array2D<N> getArray() {
        return myArray;
    }

    @Override
    MatrixTensor<N> newSameShape() {
        return new MatrixTensor<>(myFactory, this.dimensions());
    }

}

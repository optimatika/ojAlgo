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

import org.ojalgo.function.FunctionSet;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.scalar.Scalar.Factory;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Factory2D;
import org.ojalgo.structure.Mutate2D;
import org.ojalgo.type.math.MathType;

public final class TensorFactory2D<N extends Comparable<N>, T extends Mutate2D> implements Factory2D<T> {

    public static <N extends Comparable<N>, T extends Mutate2D> TensorFactory2D<N, T> of(final Factory2D<T> factory) {
        return new TensorFactory2D<>(factory);
    }

    private final Factory2D<T> myFactory;

    TensorFactory2D(final Factory2D<T> factory) {

        super();

        myFactory = factory;
    }

    public T copy(final Access2D<N> elements) {

        T retVal = myFactory.make(elements.countRows(), elements.countColumns());

        for (long i = 0; i < elements.count(); i++) {
            retVal.set(i, elements.get(i));
        }

        return retVal;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj) || !(obj instanceof TensorFactory2D)) {
            return false;
        }
        TensorFactory2D other = (TensorFactory2D) obj;
        if (myFactory == null) {
            if (other.myFactory != null) {
                return false;
            }
        } else if (!myFactory.equals(other.myFactory)) {
            return false;
        }
        return true;
    }

    public FunctionSet<N> function() {
        return (FunctionSet<N>) myFactory.function();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        return prime * result + (myFactory == null ? 0 : myFactory.hashCode());
    }

    public T identity(final int dimensions) {

        T retVal = myFactory.make(dimensions, dimensions);

        N one = this.scalar().cast(1.0);

        for (int ij = 0; ij < dimensions; ij++) {
            retVal.set(ij, ij, one);
        }

        return retVal;
    }

    public T make(final long rows, final long columns) {
        return myFactory.make(rows, columns);
    }

    /**
     * Same as {@link TensorFactoryAnyD#product(Access1D...)} but explicitly for rank 2.
     */
    public T product(final Access1D<N> vector1, final Access1D<N> vector2) {

        long rows = vector1.count();
        long cols = vector2.count();

        T retVal = myFactory.make(rows, cols);

        for (long j = 0; j < cols; j++) {
            for (long i = 0; i < rows; i++) {
                retVal.set(i, j, vector1.doubleValue(i) * vector2.doubleValue(j));
            }
        }

        return retVal;
    }

    public T power2(final Access1D<N> vector) {
        return this.product(vector, vector);
    }

    /**
     * The Kronecker matrix product / matrix tensor product
     */
    public T kronecker(final Access2D<N> matrix1, final Access2D<N> matrix2) {

        long rows1 = matrix1.countRows();
        long cols1 = matrix1.countColumns();
        long rows2 = matrix2.countRows();
        long cols2 = matrix2.countColumns();
        long rows = rows1 * rows2;
        long cols = cols1 * cols2;

        T retVal = myFactory.make(rows, cols);

        long i, j;

        for (long j1 = 0; j1 < cols1; j1++) {
            for (long j2 = 0; j2 < cols2; j2++) {
                j = j1 * cols2 + j2;

                for (long i1 = 0; i1 < rows1; i1++) {

                    double val1 = matrix1.doubleValue(i1, j1);

                    for (long i2 = 0; i2 < rows2; i2++) {
                        i = i1 * rows2 + i2;

                        double val2 = matrix2.doubleValue(i2, j2);

                        retVal.set(i, j, val1 * val2);
                    }
                }
            }
        }

        return retVal;
    }

    public Scalar.Factory<N> scalar() {
        return (Factory<N>) myFactory.scalar();
    }

    /**
     * Will create a block diagonal tensor using the input matrices as blocks in the supplied order.
     */
    public T blocks(final Access2D<N>... matrices) {

        long rows = 0;
        long cols = 0;
        for (Access2D<N> matrix : matrices) {
            rows += matrix.countRows();
            cols += matrix.countColumns();
        }

        T retVal = myFactory.make(rows, cols);

        long rowOffset = 0L;
        long colOffset = 0L;
        for (Access2D<N> matrix : matrices) {

            long m = matrix.countRows();
            long n = matrix.countColumns();

            for (int j = 0; j < n; j++) {
                for (int i = 0; i < m; i++) {
                    retVal.set(rowOffset + i, colOffset + j, matrix.get(i, j));
                }
            }

            rowOffset += m;
            colOffset += n;
        }

        return retVal;
    }

    public MathType getMathType() {
        return myFactory.getMathType();
    }

}

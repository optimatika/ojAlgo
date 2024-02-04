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
package org.ojalgo.matrix.store;

import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;

/**
 * IdentityStore
 *
 * @author apete
 */
final class IdentityStore<N extends Comparable<N>> extends FactoryStore<N> {

    IdentityStore(final PhysicalStore.Factory<N, ?> factory, final int dimension) {
        super(factory, dimension, dimension);
    }

    IdentityStore(final PhysicalStore.Factory<N, ?> factory, final long dimension) {
        super(factory, dimension, dimension);
    }

    @Override
    public MatrixStore<N> conjugate() {
        return this;
    }

    @Override
    public double doubleValue(final int aRow, final int aCol) {
        if (aRow == aCol) {
            return PrimitiveMath.ONE;
        }
        return PrimitiveMath.ZERO;
    }

    @Override
    public int firstInColumn(final int col) {
        return col;
    }

    @Override
    public int firstInRow(final int row) {
        return row;
    }

    @Override
    public N get(final int aRow, final int aCol) {
        if (aRow == aCol) {
            return this.one().get();
        }
        return this.zero().get();
    }

    @Override
    public int limitOfColumn(final int col) {
        return col + 1;
    }

    @Override
    public int limitOfRow(final int row) {
        return row + 1;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void multiply(final Access1D<N> right, final TransformableRegion<N> target) {
        if (right instanceof Access2D.Collectable) {
            ((Access2D.Collectable<N, TransformableRegion<N>>) right).supplyTo(target);
        } else {
            super.multiply(right, target);
        }
    }

    @Override
    public MatrixStore<N> multiply(final double scalar) {

        final SparseStore<N> retVal = SparseStore.makeSparse(this.physical(), this);

        retVal.fillDiagonal(this.physical().scalar().cast(scalar));

        return retVal;
    }

    @Override
    public MatrixStore<N> multiply(final MatrixStore<N> right) {
        return right.copy();
    }

    @Override
    public MatrixStore<N> multiply(final N scalar) {

        final SparseStore<N> retVal = SparseStore.makeSparse(this.physical(), this);

        retVal.fillDiagonal(scalar);

        return retVal;
    }

    @Override
    public N multiplyBoth(final Access1D<N> leftAndRight) {
        // TODO Auto-generated method stub
        return super.multiplyBoth(leftAndRight);
    }

    @Override
    public ElementsSupplier<N> premultiply(final Access1D<N> left) {
        // TODO Auto-generated method stub
        return super.premultiply(left);
    }

    @Override
    public void supplyTo(final TransformableRegion<N> receiver) {

        receiver.reset();

        receiver.fillDiagonal(this.one().get());
    }

    @Override
    public Scalar<N> toScalar(final long row, final long column) {
        if (row == column) {
            return this.one();
        }
        return this.zero();
    }

    @Override
    public MatrixStore<N> transpose() {
        return this;
    }

}

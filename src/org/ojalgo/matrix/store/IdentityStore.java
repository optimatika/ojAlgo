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
package org.ojalgo.matrix.store;

import org.ojalgo.ProgrammingError;
import org.ojalgo.access.Access1D;
import org.ojalgo.access.Access2D;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.scalar.Scalar;

/**
 * IdentityStore
 *
 * @author apete
 */
final class IdentityStore<N extends Number> extends FactoryStore<N> {

    private IdentityStore(final org.ojalgo.matrix.store.PhysicalStore.Factory<N, ?> factory, final int rowsCount, final int columnsCount) {
        super(factory, rowsCount, columnsCount);
        ProgrammingError.throwForIllegalInvocation();
    }

    IdentityStore(final PhysicalStore.Factory<N, ?> factory, final int dimension) {
        super(factory, dimension, dimension);
    }

    @Override
    public MatrixStore<N> conjugate() {
        return this;
    }

    public double doubleValue(final long aRow, final long aCol) {
        if (aRow == aCol) {
            return PrimitiveMath.ONE;
        } else {
            return PrimitiveMath.ZERO;
        }
    }

    public int firstInColumn(final int col) {
        return col;
    }

    public int firstInRow(final int row) {
        return row;
    }

    public N get(final long aRow, final long aCol) {
        if (aRow == aCol) {
            return this.physical().scalar().one().get();
        } else {
            return this.physical().scalar().zero().get();
        }
    }

    @Override
    public int limitOfColumn(final int col) {
        return col + 1;
    }

    @Override
    public int limitOfRow(final int row) {
        return row + 1;
    }

    @SuppressWarnings("unchecked")
    public void multiply(final Access1D<N> right, final ElementsConsumer<N> target) {
        if (right instanceof Access2D.Collectable) {
            ((Access2D.Collectable<N, ElementsConsumer<N>>) right).supplyTo(target);
        } else {
            super.multiply(right, target);
        }
    }

    public MatrixStore<N> multiply(final double scalar) {

        final SparseStore<N> retVal = new SparseStore<>(this.physical(), this.getRowDim(), this.getColDim());

        retVal.loopDiagonal(0L, 0L, (r, c) -> retVal.set(r, c, scalar));

        return retVal;
    }

    @Override
    public MatrixStore<N> multiply(final MatrixStore<N> right) {
        return right.copy();
    }

    public MatrixStore<N> multiply(final N scalar) {

        final SparseStore<N> retVal = new SparseStore<>(this.physical(), this.getRowDim(), this.getColDim());

        retVal.loopDiagonal(0L, 0L, (r, c) -> retVal.set(r, c, scalar));

        return retVal;
    }

    @Override
    public N multiplyBoth(final Access1D<N> leftAndRight) {
        // TODO Auto-generated method stub
        return super.multiplyBoth(leftAndRight);
    }

    public ElementsSupplier<N> premultiply(final Access1D<N> left) {
        // TODO Auto-generated method stub
        return super.premultiply(left);
    }

    public void supplyTo(final ElementsConsumer<N> receiver) {

        receiver.reset();

        receiver.fillDiagonal(this.physical().scalar().one().get());
    }

    public Scalar<N> toScalar(final long row, final long column) {
        if (row == column) {
            return this.physical().scalar().one();
        } else {
            return this.physical().scalar().zero();
        }
    }

    @Override
    public MatrixStore<N> transpose() {
        return this;
    }

}

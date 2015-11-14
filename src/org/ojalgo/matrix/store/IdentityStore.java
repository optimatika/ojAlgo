/*
 * Copyright 1997-2015 Optimatika (www.optimatika.se)
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
            return this.factory().scalar().one().getNumber();
        } else {
            return this.factory().scalar().zero().getNumber();
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

    @Override
    public MatrixStore<N> multiply(final Access1D<N> right) {
        if (this.getColDim() == right.count()) {
            return this.factory().columns(right);
        } else if (right instanceof MatrixStore<?>) {
            return ((MatrixStore<N>) right).copy();
        } else {
            return super.multiply(right);
        }
    }

    public Scalar<N> toScalar(final long row, final long column) {
        if (row == column) {
            return this.factory().scalar().one();
        } else {
            return this.factory().scalar().zero();
        }
    }

    @Override
    public MatrixStore<N> transpose() {
        return this;
    }

    @Override
    protected void supplyNonZerosTo(final ElementsConsumer<N> consumer) {
        consumer.fillDiagonal(0L, 0L, this.factory().scalar().one().getNumber());
    }

}

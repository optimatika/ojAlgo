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

import org.ojalgo.access.Access1D;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.scalar.Scalar;

/**
 * ZeroStore
 *
 * @author apete
 */
final class ZeroStore<N extends Number> extends FactoryStore<N> {

    private final N myNumberZero;
    private final Scalar<N> myScalarZero;

    ZeroStore(final PhysicalStore.Factory<N, ?> factory, final int rowsCount, final int columnsCount) {

        super(factory, rowsCount, columnsCount);

        myScalarZero = factory.scalar().zero();
        myNumberZero = myScalarZero.getNumber();
    }

    @Override
    public MatrixStore<N> add(final MatrixStore<N> addend) {
        return addend;
    }

    @Override
    public MatrixStore<N> conjugate() {
        return new ZeroStore<N>(this.factory(), this.getColDim(), this.getRowDim());
    }

    @Override
    public double doubleValue(final long anInd) {
        return PrimitiveMath.ZERO;
    }

    public double doubleValue(final long aRow, final long aCol) {
        return PrimitiveMath.ZERO;
    }

    public int firstInColumn(final int col) {
        return this.getRowDim();
    }

    public int firstInRow(final int row) {
        return this.getColDim();
    }

    public N get(final long aRow, final long aCol) {
        return myNumberZero;
    }

    @Override
    public int limitOfColumn(final int col) {
        return 0;
    }

    @Override
    public int limitOfRow(final int row) {
        return 0;
    }

    @Override
    public ZeroStore<N> multiply(final Access1D<N> right) {
        return new ZeroStore<N>(this.factory(), this.getRowDim(), (int) (right.count() / this.getColDim()));
    }

    @Override
    public MatrixStore<N> scale(final N scalar) {
        return this;
    }

    public Scalar<N> toScalar(final long row, final long column) {
        return myScalarZero;
    }

    @Override
    public MatrixStore<N> transpose() {
        return new ZeroStore<N>(this.factory(), this.getColDim(), this.getRowDim());
    }

    @Override
    protected void supplyNonZerosTo(final ElementsConsumer<N> consumer) {
    }

}

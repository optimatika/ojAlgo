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
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.scalar.Scalar;

final class UpperTriangularStore<N extends Number> extends ShadingStore<N> {

    private final boolean myUnitDiagonal;

    @SuppressWarnings("unused")
    private UpperTriangularStore(final int aRowDim, final int aColDim, final MatrixStore<N> base) {

        this(base, true);

        ProgrammingError.throwForIllegalInvocation();
    }

    UpperTriangularStore(final MatrixStore<N> base, final boolean unitDiagonal) {

        super((int) Math.min(base.countRows(), base.countColumns()), (int) base.countColumns(), base);

        myUnitDiagonal = unitDiagonal;
    }

    public double doubleValue(final long row, final long col) {
        if (row > col) {
            return PrimitiveMath.ZERO;
        } else if (myUnitDiagonal && (row == col)) {
            return PrimitiveMath.ONE;
        } else {
            return this.getBase().doubleValue(row, col);
        }
    }

    public int firstInRow(final int row) {
        return row;
    }

    public N get(final long row, final long col) {
        if (row > col) {
            return this.factory().scalar().zero().getNumber();
        } else if (myUnitDiagonal && (row == col)) {
            return this.factory().scalar().one().getNumber();
        } else {
            return this.getBase().get(row, col);
        }
    }

    @Override
    public int limitOfColumn(final int col) {
        return col + 1;
    }

    public Scalar<N> toScalar(final long row, final long col) {
        if (row > col) {
            return this.factory().scalar().zero();
        } else if (myUnitDiagonal && (row == col)) {
            return this.factory().scalar().one();
        } else {
            return this.getBase().toScalar(row, col);
        }
    }

}

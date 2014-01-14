/*
 * Copyright 1997-2013 Optimatika (www.optimatika.se)
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

/**
 * A Hessenberg matrix is one that is "almost" triangular. An upper Hessenberg matrix has zero entries below the first
 * subdiagonal.
 * 
 * @author apete
 */
public final class UpperHessenbergStore<N extends Number> extends ShadingStore<N> {

    public UpperHessenbergStore(final MatrixStore<N> aBase) {
        super((int) Math.min(aBase.countRows(), aBase.countColumns()), (int) aBase.countColumns(), aBase);
    }

    @SuppressWarnings("unused")
    private UpperHessenbergStore(final int aRowDim, final int aColDim, final MatrixStore<N> aBase) {

        this(aBase);

        ProgrammingError.throwForIllegalInvocation();
    }

    public double doubleValue(final long aRow, final long aCol) {
        if (aRow > (aCol + 1)) {
            return PrimitiveMath.ZERO;
        } else {
            return this.getBase().doubleValue(aRow, aCol);
        }
    }

    public N get(final long aRow, final long aCol) {
        if (aRow > (aCol + 1)) {
            return this.factory().scalar().zero().getNumber();
        } else {
            return this.getBase().get(aRow, aCol);
        }
    }

    public boolean isLowerLeftShaded() {
        return true;
    }

    public boolean isUpperRightShaded() {
        return false;
    }

    public Scalar<N> toScalar(final long row, final long column) {
        if (row > (column + 1)) {
            return this.factory().scalar().zero();
        } else {
            return this.getBase().toScalar(row, column);
        }
    }
}

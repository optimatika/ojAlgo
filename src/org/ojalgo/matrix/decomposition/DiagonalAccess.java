/*
 * Copyright 1997-2016 Optimatika (www.optimatika.se)
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
package org.ojalgo.matrix.decomposition;

import org.ojalgo.access.Access2D;
import org.ojalgo.array.Array1D;
import org.ojalgo.constant.PrimitiveMath;

final class DiagonalAccess<N extends Number> implements Access2D<N> {

    private final int myDimension;
    private final N myZero;

    final Array1D<N> mainDiagonal;
    final Array1D<N> subdiagonal;
    final Array1D<N> superdiagonal;

    @SuppressWarnings("unused")
    private DiagonalAccess() {
        this(null, null, null, null);
    }

    DiagonalAccess(final Array1D<N> mainDiag, final Array1D<N> superdiag, final Array1D<N> subdiag, final N zero) {

        super();

        mainDiagonal = mainDiag;
        superdiagonal = superdiag;
        subdiagonal = subdiag;

        myZero = zero;

        if (mainDiag != null) {
            myDimension = mainDiag.size();
        } else if (superdiag != null) {
            myDimension = superdiag.size() + 1;
        } else {
            myDimension = subdiag.size() + 1;
        }
    }

    public long countColumns() {
        return myDimension;
    }

    public long countRows() {
        return myDimension;
    }

    public double doubleValue(final long row, final long col) {
        if ((mainDiagonal != null) && (row == col)) {
            return mainDiagonal.doubleValue(row);
        } else if ((superdiagonal != null) && ((col - row) == 1L)) {
            return superdiagonal.doubleValue(row);
        } else if ((subdiagonal != null) && ((row - col) == 1L)) {
            return subdiagonal.doubleValue(col);
        } else {
            return PrimitiveMath.ZERO;
        }
    }

    public N get(final long row, final long col) {
        if ((mainDiagonal != null) && (row == col)) {
            return mainDiagonal.get(row);
        } else if ((superdiagonal != null) && ((col - row) == 1L)) {
            return superdiagonal.get(row);
        } else if ((subdiagonal != null) && ((row - col) == 1L)) {
            return subdiagonal.get(col);
        } else {
            return myZero;
        }
    }

    @Override
    public String toString() {
        return "DiagonalAccess [mainDiagonal=" + mainDiagonal + ", subdiagonal=" + subdiagonal + ", superdiagonal=" + superdiagonal + "]";
    }

    DiagonalAccess<N> columns(final int first, final int limit) {

        final Array1D<N> tmpMainDiagonal = mainDiagonal != null ? mainDiagonal.subList(first, limit) : null;
        final Array1D<N> tmpSuperdiagonal = superdiagonal != null ? superdiagonal.subList(Math.max(first - 1, 0), limit - 1) : null;
        final Array1D<N> tmpSubdiagonal = subdiagonal != null ? subdiagonal.subList(first, Math.min(limit, myDimension - 1)) : null;

        return new DiagonalAccess<>(tmpMainDiagonal, tmpSuperdiagonal, tmpSubdiagonal, myZero);
    }

    int getDimension() {
        return myDimension;
    }

    DiagonalAccess<N> rows(final int first, final int limit) {

        final Array1D<N> tmpMainDiagonal = mainDiagonal != null ? mainDiagonal.subList(first, limit) : null;
        final Array1D<N> tmpSuperdiagonal = superdiagonal != null ? superdiagonal.subList(first, Math.min(limit, myDimension - 1)) : null;
        final Array1D<N> tmpSubdiagonal = subdiagonal != null ? subdiagonal.subList(Math.max(first - 1, 0), limit - 1) : null;

        return new DiagonalAccess<>(tmpMainDiagonal, tmpSuperdiagonal, tmpSubdiagonal, myZero);
    }

    DiagonalAccess<N> transpose() {
        return new DiagonalAccess<>(mainDiagonal, subdiagonal, superdiagonal, myZero);
    }

}

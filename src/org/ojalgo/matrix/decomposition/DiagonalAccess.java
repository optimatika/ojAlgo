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
package org.ojalgo.matrix.decomposition;

import org.ojalgo.access.Access2D;
import org.ojalgo.access.AccessUtils;
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

    DiagonalAccess(final Array1D<N> aMainDiagonal, final Array1D<N> aSuperdiagonal, final Array1D<N> aSubdiagonal, final N aZero) {

        super();

        mainDiagonal = aMainDiagonal;
        superdiagonal = aSuperdiagonal;
        subdiagonal = aSubdiagonal;

        myZero = aZero;

        if (aMainDiagonal != null) {
            myDimension = aMainDiagonal.size();
        } else if (aSuperdiagonal != null) {
            myDimension = aSuperdiagonal.size() + 1;
        } else {
            myDimension = aSubdiagonal.size() + 1;
        }
    }

    public long count() {
        return (int) this.count();
    }

    public long countColumns() {
        return myDimension;
    }

    public long countRows() {
        return myDimension;
    }

    public double doubleValue(final long anInd) {
        return this.doubleValue(AccessUtils.row((int) anInd, myDimension), AccessUtils.column((int) anInd, myDimension));
    }

    public double doubleValue(final long aRow, final long aCol) {
        if ((mainDiagonal != null) && (aRow == aCol)) {
            return mainDiagonal.doubleValue(aRow);
        } else if ((superdiagonal != null) && ((aCol - aRow) == 1)) {
            return superdiagonal.doubleValue(aRow);
        } else if ((subdiagonal != null) && ((aRow - aCol) == 1)) {
            return subdiagonal.doubleValue(aCol);
        } else {
            return PrimitiveMath.ZERO;
        }
    }

    public N get(final long index) {
        return this.get(AccessUtils.row(index, myDimension), AccessUtils.column(index, myDimension));
    }

    public N get(final long aRow, final long aCol) {
        if ((mainDiagonal != null) && (aRow == aCol)) {
            return mainDiagonal.get(aRow);
        } else if ((superdiagonal != null) && ((aCol - aRow) == 1)) {
            return superdiagonal.get(aRow);
        } else if ((subdiagonal != null) && ((aRow - aCol) == 1)) {
            return subdiagonal.get(aCol);
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

        return new DiagonalAccess<N>(tmpMainDiagonal, tmpSuperdiagonal, tmpSubdiagonal, myZero);
    }

    int getDimension() {
        return myDimension;
    }

    DiagonalAccess<N> rows(final int first, final int limit) {

        final Array1D<N> tmpMainDiagonal = mainDiagonal != null ? mainDiagonal.subList(first, limit) : null;
        final Array1D<N> tmpSuperdiagonal = superdiagonal != null ? superdiagonal.subList(first, Math.min(limit, myDimension - 1)) : null;
        final Array1D<N> tmpSubdiagonal = subdiagonal != null ? subdiagonal.subList(Math.max(first - 1, 0), limit - 1) : null;

        return new DiagonalAccess<N>(tmpMainDiagonal, tmpSuperdiagonal, tmpSubdiagonal, myZero);
    }

    DiagonalAccess<N> transpose() {
        return new DiagonalAccess<N>(mainDiagonal, subdiagonal, superdiagonal, myZero);
    }

}

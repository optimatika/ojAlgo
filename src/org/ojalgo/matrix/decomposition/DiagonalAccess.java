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
package org.ojalgo.matrix.decomposition;

import org.ojalgo.access.Access1D;
import org.ojalgo.access.Access2D;
import org.ojalgo.access.Mutate1D;
import org.ojalgo.constant.PrimitiveMath;

abstract class DiagonalAccess<N extends Number, D extends Access1D<N> & Mutate1D> implements Access2D<N> {

    private final int myDimension;
    private final N myZero;

    final D mainDiagonal;
    final D subdiagonal;
    final D superdiagonal;

    @SuppressWarnings("unused")
    private DiagonalAccess() {
        this(null, null, null, null);
    }

    DiagonalAccess(final D mainDiag, final D superdiag, final D subdiag, final N zero) {

        super();

        mainDiagonal = mainDiag;
        superdiagonal = superdiag;
        subdiagonal = subdiag;

        myZero = zero;

        if (mainDiag != null) {
            myDimension = (int) mainDiag.count();
        } else if (superdiag != null) {
            myDimension = ((int) superdiag.count()) + 1;
        } else {
            myDimension = ((int) subdiag.count()) + 1;
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

    int getDimension() {
        return myDimension;
    }

    N getZero() {
        return myZero;
    }

}

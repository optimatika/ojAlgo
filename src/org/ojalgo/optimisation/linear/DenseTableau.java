/*
 * Copyright 1997-2017 Optimatika (www.optimatika.se)
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
package org.ojalgo.optimisation.linear;

import static org.ojalgo.constant.PrimitiveMath.*;
import static org.ojalgo.function.PrimitiveFunction.*;

import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.matrix.store.PrimitiveDenseStore;

final class DenseTableau extends SimplexTableau {

    private final PrimitiveDenseStore myTransposed;

    DenseTableau(final SparseTableau sparse) {

        super();

        myTransposed = sparse.transpose();
    }

    public long countColumns() {
        return myTransposed.countRows();
    }

    public long countRows() {
        return myTransposed.countColumns();
    }

    public double doubleValue(final long row, final long col) {
        return myTransposed.doubleValue(col, row);
    }

    public Double get(final long row, final long col) {
        return myTransposed.get(col, row);
    }

    @Override
    protected void pivot(final IterationPoint iterationPoint) {

        final int row = iterationPoint.row;
        final int col = iterationPoint.col;

        final double pivotElement = myTransposed.doubleValue(col, row);

        for (int i = 0; i < myTransposed.countColumns(); i++) {
            // TODO Stop updating phase 1 objective when in phase 2.
            if (i != row) {
                final double colVal = myTransposed.doubleValue(col, i);
                if (colVal != ZERO) {
                    myTransposed.caxpy(-colVal / pivotElement, row, i, 0);
                }
            }
        }

        if (PrimitiveFunction.ABS.invoke(pivotElement) < ONE) {
            myTransposed.modifyColumn(0, row, DIVIDE.second(pivotElement));
        } else if (pivotElement != ONE) {
            myTransposed.modifyColumn(0, row, MULTIPLY.second(ONE / pivotElement));
        }
    }

}

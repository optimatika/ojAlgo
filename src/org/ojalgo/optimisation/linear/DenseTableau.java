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

import org.ojalgo.array.Array1D;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;

final class DenseTableau extends SimplexTableau {

    private final PrimitiveDenseStore myTransposed;

    DenseTableau(final SparseTableau sparse) {

        super(sparse.countConstraints(), sparse.countProblemVariables(), sparse.countSlackVariables());

        myTransposed = sparse.transpose();
    }

    DenseTableau(final LinearSolver.Builder matrices) {

        super(matrices.countEqualityConstraints(), matrices.countVariables(), 0);

        final int tmpConstraintsCount = this.countConstraints();
        final int tmpVariablesCount = this.countVariables();

        final MatrixStore.LogicalBuilder<Double> tmpTableauBuilder = MatrixStore.PRIMITIVE.makeZero(1, 1);
        tmpTableauBuilder.left(matrices.getC().transpose().logical().right(MatrixStore.PRIMITIVE.makeZero(1, tmpConstraintsCount).get()).get());

        if (tmpConstraintsCount >= 1) {
            tmpTableauBuilder.above(matrices.getAE(), MatrixStore.PRIMITIVE.makeIdentity(tmpConstraintsCount).get(), matrices.getBE());
        }
        tmpTableauBuilder.below(MatrixStore.PRIMITIVE.makeZero(1, tmpVariablesCount).get(),
                PrimitiveDenseStore.FACTORY.makeFilled(1, tmpConstraintsCount, new NullaryFunction<Double>() {

                    public double doubleValue() {
                        return ONE;
                    }

                    public Double get() {
                        return ONE;
                    }

                    public double getAsDouble() {
                        return ONE;
                    }

                    public Double invoke() {
                        return ONE;
                    }
                }));
        //myTransposedTableau = (PrimitiveDenseStore) tmpTableauBuilder.build().transpose().copy();
        myTransposed = PrimitiveDenseStore.FACTORY.transpose(tmpTableauBuilder.get());
        // myTableau = LinearSolver.make(myTransposedTableau);

        for (int i = 0; i < tmpConstraintsCount; i++) {

            myTransposed.caxpy(NEG, i, tmpConstraintsCount + 1, 0);

        }

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

    @Override
    protected Array1D<Double> sliceConstraintsRHS() {
        return myTransposed.sliceRow(this.countVariablesTotally()).subList(0, this.countConstraints());
    }

    @Override
    protected Array1D<Double> sliceTableauColumn(final int col) {
        return myTransposed.sliceRow(col).subList(0, this.countConstraints());
    }

    @Override
    protected Array1D<Double> sliceTableauRow(final int row) {
        return myTransposed.sliceColumn(row).subList(0, this.countVariablesTotally());
    }

}

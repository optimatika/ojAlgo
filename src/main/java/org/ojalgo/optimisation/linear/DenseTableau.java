/*
 * Copyright 1997-2024 Optimatika
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

import static org.ojalgo.function.constant.PrimitiveMath.*;

import java.util.Arrays;

import org.ojalgo.array.ArrayR064;
import org.ojalgo.array.operation.AXPY;
import org.ojalgo.array.operation.CorePrimitiveOperation;
import org.ojalgo.array.operation.IndexOf;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.ElementView1D;
import org.ojalgo.structure.Primitive1D;
import org.ojalgo.structure.Primitive2D;

final class DenseTableau extends SimplexTableau {

    private double[] myAuxiliaryRow = null;
    private final int myColDim;
    private final double[][] myTableau;

    DenseTableau(final int mm, final int nn) {
        this(new LinearStructure(mm, nn));
    }

    DenseTableau(final LinearStructure linearStructure) {

        super(linearStructure);

        int nbRows = m + 1;
        int nbCols = n + 1;

        myTableau = new double[nbRows][nbCols];
        myColDim = nbCols;

        myAuxiliaryRow = new double[nbCols];
    }

    @Override
    public double doubleValue(final int row, final int col) {
        if (row == m + 1) {
            return myAuxiliaryRow[col];
        } else {
            return myTableau[row][col];
        }
    }

    @Override
    public int getColDim() {
        return myColDim;
    }

    @Override
    public int getRowDim() {
        if (myAuxiliaryRow != null) {
            return myTableau.length + 1;
        } else {
            return myTableau.length;
        }
    }

    @Override
    public void set(final int row, final int col, final double value) {
        myTableau[row][col] = value;
    }

    private void doPivot(final int row, final int col, final double[] pivotRow) {

        for (int i = 0, limit = myTableau.length; i < limit; i++) {
            if (i != row) {
                double[] dataRow = myTableau[i];
                double colVal = dataRow[col];
                if (colVal != ZERO) {
                    AXPY.invoke(dataRow, 0, -colVal, pivotRow, 0, 0, myColDim);
                }
            }
        }

        if (myAuxiliaryRow != null) {
            AXPY.invoke(myAuxiliaryRow, 0, -myAuxiliaryRow[col], pivotRow, 0, 0, myColDim);
        }
    }

    private void scale(final double[] body, final int col) {
        double pivotElement = body[col];
        if (pivotElement != ONE) {
            CorePrimitiveOperation.divide(body, 0, myColDim, 1, body, pivotElement);
        }
    }

    @Override
    protected void doPivot(final int row, final int col) {

        double[] pivotRow = myTableau[row];

        this.scale(pivotRow, col);

        this.doPivot(row, col, pivotRow);
    }

    @Override
    protected void shiftColumn(final int col, final double shift) {
        super.shiftColumn(col, shift);
        for (int i = 0; i < m; i++) {
            myTableau[i][n] -= shift * myTableau[i][col];
        }
    }

    @Override
    void copyBasicSolution(final double[] solution) {
        for (int i = 0; i < included.length; i++) {
            solution[included[i]] = myTableau[i][n];
        }
    }

    @Override
    void copyObjective() {
        myAuxiliaryRow = Arrays.copyOf(myTableau[m], myColDim);
    }

    @Override
    double extractValue() {

        double retVal = -myTableau[m][n];

        int[] lower = this.getExcludedLower();
        for (int i = 0; i < lower.length; i++) {
            double bound = this.getLowerBound(lower[i]);
            if (bound != ZERO) {
                retVal += bound * myTableau[m][lower[i]];
            }
        }

        int[] upper = this.getExcludedUpper();
        for (int i = 0; i < upper.length; i++) {
            double bound = this.getUpperBound(upper[i]);
            if (bound != ZERO) {
                retVal += bound * myTableau[m][upper[i]];
            }
        }

        return retVal;
    }

    @Override
    boolean fixVariable(final int index, final double value) {

        int row = IndexOf.indexOf(included, index);

        if (row < 0) {
            return false;
        }

        // Diff begin

        // Array1D<Double> currentRow = myTransposed.sliceColumn(row);
        ArrayR064 currentRow = ArrayR064.wrap(myTableau[row]);
        double currentRHS = currentRow.doubleValue(myColDim - 1);

        final ArrayR064 auxiliaryRow = ArrayR064.make(myColDim);
        if (currentRHS > value) {
            currentRow.axpy(NEG, auxiliaryRow);
            auxiliaryRow.set(index, ZERO);
            auxiliaryRow.set(myColDim - 1, value - currentRHS);
        } else if (currentRHS < value) {
            currentRow.axpy(ONE, auxiliaryRow);
            auxiliaryRow.set(index, ZERO);
            auxiliaryRow.set(myColDim - 1, currentRHS - value);
        } else {
            return true;
        }

        // Diff end

        Access1D<Double> objectiveRow = this.sliceTableauRow(m);

        int pivotCol = this.findNextPivotColumn(auxiliaryRow, objectiveRow);

        if (pivotCol < 0) {
            // TODO Problem infeasible?
            // Probably better to return true here, and have the subsequest solver.solve() return INFEASIBLE
            return false;
        }

        // Diff begin

        this.scale(auxiliaryRow.data, pivotCol);

        this.doPivot(-1, pivotCol, auxiliaryRow.data);

        // myTransposed.fillColumn(row, auxiliaryRow);
        myTableau[row] = auxiliaryRow.data;

        // Diff end

        for (ElementView1D<Double, ?> elem : this.sliceConstraintsRHS().elements()) {
            if (elem.doubleValue() < ZERO) {
                return false;
            }
        }

        this.update(row, pivotCol);

        return true;
    }

    @Override
    double getInfeasibility() {
        if (myAuxiliaryRow != null) {
            return myAuxiliaryRow[n];
        } else {
            return ZERO;
        }
    }

    @Override
    double getValue() {
        return myTableau[m][n];
    }

    @Override
    Primitive2D newConstraintsBody() {

        return new Primitive2D() {

            @Override
            public double doubleValue(final int row, final int col) {
                return myTableau[row][col];
            }

            @Override
            public int getColDim() {
                return structure.countVariables();
            }

            @Override
            public int getRowDim() {
                return m;
            }

            @Override
            public void set(final int row, final int col, final double value) {

                myTableau[row][col] = value;

                if (row >= structure.nbIdty) {
                    myAuxiliaryRow[col] -= value;
                }
            }

        };
    }

    @Override
    Primitive1D newConstraintsRHS() {

        return new Primitive1D() {

            @Override
            public double doubleValue(final int index) {
                return myTableau[index][n];
            }

            @Override
            public void set(final int index, final double value) {

                if (structure.nbArti > 0) {
                    myTableau[index][n - m + index] = ONE;
                }

                myTableau[index][n] = value;

                if (index >= structure.nbIdty) {
                    myAuxiliaryRow[n] -= value;
                }
            }

            @Override
            public int size() {
                return m;
            }

        };
    }

    @Override
    Primitive1D newObjective() {

        return new Primitive1D() {

            @Override
            public double doubleValue(final int index) {
                return myTableau[m][index];
            }

            @Override
            public void set(final int index, final double value) {
                myTableau[m][index] = value;
            }

            @Override
            public int size() {
                return structure.countModelVariables();
            }

        };
    }

    @Override
    void restoreObjective() {
        myTableau[m] = myAuxiliaryRow;
        myAuxiliaryRow = null;
    }

}
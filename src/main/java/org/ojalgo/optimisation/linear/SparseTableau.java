/*
 * Copyright 1997-2025 Optimatika
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

import org.ojalgo.array.Array1D;
import org.ojalgo.array.ArrayR064;
import org.ojalgo.array.DenseArray;
import org.ojalgo.array.PrimitiveArray;
import org.ojalgo.array.SparseArray;
import org.ojalgo.array.operation.IndexOf;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.ElementView1D;
import org.ojalgo.structure.Primitive1D;
import org.ojalgo.structure.Primitive2D;

final class SparseTableau extends SimplexTableau {

    private static final Array1D.Factory<Double> ARRAY1D_FACTORY = Array1D.factory(ArrayR064.FACTORY);
    private static final PrimitiveArray.Factory DENSE_FACTORY = ArrayR064.FACTORY;

    private static double scale(final SparseArray<Double> body, final double rhs, final int col) {

        double pivotElement = body.doubleValue(col);

        if (pivotElement == ZERO) {
            throw new IllegalStateException("Pivot element is zero!");
        } else if (pivotElement != ONE) {
            UnaryFunction<Double> modifier = DIVIDE.by(pivotElement);
            body.modifyAll(modifier);
            return modifier.invoke(rhs);
        } else {
            return rhs;
        }
    }

    private final SparseArray<Double>[] myBody;
    private final Array1D<Double> myObjective;
    private DenseArray<Double> myPhase1Objective;
    private double myPhase1Value = ZERO;
    private final Array1D<Double> myRHS;
    private final SparseArray.SparseFactory<Double> mySparseFactory;
    private double myValue = ZERO;

    SparseTableau(final int mm, final int nn) {
        this(new LinearStructure(mm, nn));
    }

    SparseTableau(final LinearStructure linearStructure) {

        super(linearStructure);

        mySparseFactory = SparseArray.factory(ArrayR064.FACTORY).initial(Math.max(5L, Math.round(Math.sqrt(m))));

        // Including artificial variables
        myBody = new SparseArray[m];
        for (int r = 0; r < m; r++) {
            myBody[r] = mySparseFactory.make(n);
        }

        myRHS = SparseTableau.ARRAY1D_FACTORY.make(m);

        myObjective = SparseTableau.ARRAY1D_FACTORY.make(n);
        myPhase1Objective = SparseTableau.DENSE_FACTORY.make(n);
    }

    @Override
    public double doubleValue(final int row, final int col) {

        if (row < m) {
            if (col < n) {
                return myBody[row].doubleValue(col);
            } else {
                return myRHS.doubleValue(row);
            }
        } else if (row == m) {
            if (col < n) {
                return myObjective.doubleValue(col);
            }
            return myValue;
        } else if (col < n) {
            return myPhase1Objective.doubleValue(col);
        } else {
            return myPhase1Value;
        }
    }

    @Override
    public int getColDim() {
        return n + 1;
    }

    @Override
    public int getRowDim() {
        if (myPhase1Objective != null) {
            return m + 2;
        } else {
            return m + 1;
        }
    }

    @Override
    public void set(final int row, final int col, final double value) {

        if (row < m) {
            if (col < n) {
                myBody[row].set(col, value);
            } else {
                myRHS.set(row, value);
            }
        } else if (row == m) {
            if (col < n) {
                myObjective.set(col, value);
            } else {
                myValue = value;
            }
        } else if (col < n) {
            myPhase1Objective.set(col, value);
        } else {
            myPhase1Value = value;
        }
    }

    private void doPivot(final int row, final int col, final SparseArray<Double> body, final double rhs) {

        double colVal;

        for (int i = 0, limit = myBody.length; i < limit; i++) {
            if (i != row) {
                SparseArray<Double> rowY = myBody[i];
                colVal = -rowY.doubleValue(col);
                if (colVal != ZERO && !PRECISION.isZero(colVal)) {
                    body.axpy(colVal, rowY);
                    myRHS.add(i, colVal * rhs);
                }
            }
        }

        colVal = -myObjective.doubleValue(col);
        if (colVal != ZERO && (myPhase1Objective != null || !PRECISION.isZero(colVal))) {
            body.axpy(colVal, myObjective);
            myValue += colVal * rhs;
        }

        if (myPhase1Objective != null) {
            colVal = -myPhase1Objective.doubleValue(col);
            if (colVal != ZERO) {
                body.axpy(colVal, myPhase1Objective);
                myPhase1Value += colVal * rhs;
            }
        }
    }

    @Override
    protected void doPivot(final int row, final int col) {

        SparseArray<Double> pivotRowBody = myBody[row];
        double pivotRowRHS = myRHS.doubleValue(row);

        pivotRowRHS = SparseTableau.scale(pivotRowBody, pivotRowRHS, col);
        myRHS.set(row, pivotRowRHS);

        this.doPivot(row, col, pivotRowBody, pivotRowRHS);
    }

    @Override
    protected void shiftColumn(final int col, final double shift) {
        super.shiftColumn(col, shift);
        for (int i = 0; i < m; i++) {
            double element = myBody[i].doubleValue(col);
            if (element != ZERO) {
                myRHS.add(i, -shift * element);
            }
        }
    }

    @Override
    void copyBasicSolution(final double[] solution) {
        for (int i = 0; i < included.length; i++) {
            solution[included[i]] = myRHS.doubleValue(i);
        }
    }

    @Override
    double extractValue() {

        double retVal = -myValue;

        for (int je = 0; je < excluded.length; je++) {
            int j = excluded[je];
            ColumnState columnState = this.getColumnState(j);

            if (columnState == ColumnState.LOWER) {

                double lowerBound = this.getLowerBound(j);
                if (lowerBound != ZERO) {
                    retVal += lowerBound * myObjective.doubleValue(j);
                }

            } else if (columnState == ColumnState.UPPER) {

                double upperBound = this.getUpperBound(j);
                if (upperBound != ZERO) {
                    retVal += upperBound * myObjective.doubleValue(j);
                }
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

        SparseArray<Double> currentRow = myBody[row];
        double currentRHS = myRHS.doubleValue(row);

        SparseArray<Double> auxiliaryRow = mySparseFactory.make(n);
        double auxiliaryRHS = ZERO;

        if (currentRHS > value) {
            currentRow.axpy(NEG, auxiliaryRow);
            auxiliaryRow.set(index, ZERO);
            auxiliaryRHS = value - currentRHS;
        } else if (currentRHS < value) {
            currentRow.axpy(ONE, auxiliaryRow);
            auxiliaryRow.set(index, ZERO);
            auxiliaryRHS = currentRHS - value;
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

        auxiliaryRHS = SparseTableau.scale(auxiliaryRow, auxiliaryRHS, pivotCol);

        this.doPivot(-1, pivotCol, auxiliaryRow, auxiliaryRHS);

        myBody[row] = auxiliaryRow;
        myRHS.set(row, auxiliaryRHS);

        // Diff end

        for (ElementView1D<Double, ?> elem : this.sliceConstraintsRHS().elements()) {
            if (elem.doubleValue() < ZERO) {
                return false;
            }
        }

        this.update(row, pivotCol);

        return true;
    }

    /**
     * @return The phase 1 objective function value
     */
    @Override
    double getInfeasibility() {
        if (myPhase1Objective != null) {
            return myPhase1Value;
        } else {
            return ZERO;
        }
    }

    @Override
    double getReducedCost(final int je) {
        if (myPhase1Objective != null) {
            return myPhase1Objective.doubleValue(excluded[je]);
        } else {
            return myObjective.doubleValue(excluded[je]);
        }
    }

    @Override
    double getValue() {
        return myValue;
    }

    @Override
    Primitive2D newConstraintsBody() {

        int base = structure.nbIdty;
        int limit = structure.countVariables();

        return new Primitive2D() {

            @Override
            public double doubleValue(final int row, final int col) {
                return myBody[row].doubleValue(col);
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

                myBody[row].set(col, value);

                if (row >= base && col < limit) {
                    myPhase1Objective.add(col, -value);
                }
            }

        };
    }

    @Override
    Primitive1D newConstraintsRHS() {

        int base = structure.nbIdty;

        return new Primitive1D() {

            @Override
            public double doubleValue(final int index) {
                return myRHS.doubleValue(index);
            }

            @Override
            public void set(final int index, final double value) {

                if (structure.nbArti > 0) {
                    myBody[index].set(n - m + index, ONE);
                }

                myRHS.set(index, value);

                if (index >= base) {
                    myPhase1Value -= value;
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
                return myObjective.doubleValue(index);
            }

            @Override
            public void set(final int index, final double value) {
                myObjective.set(index, value);
            }

            @Override
            public int size() {
                return structure.countModelVariables();
            }

        };
    }

    @Override
    Primitive1D phase1() {

        if (myPhase1Objective == null) {
            myPhase1Objective = SparseTableau.DENSE_FACTORY.make(n);
        }

        return new Primitive1D() {

            @Override
            public double doubleValue(final int index) {
                return myPhase1Objective.doubleValue(index);
            }

            @Override
            public void set(final int index, final double value) {
                myPhase1Objective.set(index, value);
            }

            @Override
            public int size() {
                return structure.countModelVariables();
            }

        };
    }

    @Override
    void removePhase1() {
        myPhase1Objective = null;
    }

}

/*
 * Copyright 1997-2022 Optimatika
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

import org.ojalgo.array.operation.AXPY;
import org.ojalgo.array.operation.CorePrimitiveOperation;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.linear.SimplexSolver.EnterInfo;
import org.ojalgo.optimisation.linear.SimplexSolver.ExitInfo;
import org.ojalgo.optimisation.linear.SimplexSolver.IterDescr;
import org.ojalgo.structure.Access2D;

final class TableauStore extends SimplexStore implements Access2D<Double> {

    private static void pivotRow(final double[] dataRow, final int col, final double[] pivotRow, final int length) {
        double dataElement = dataRow[col];
        if (dataElement != ZERO) {
            AXPY.invoke(dataRow, 0, -dataElement, pivotRow, 0, 0, length);
        }
    }

    static TableauStore build(final ExpressionsBasedModel model) {
        return SimplexStore.build(model, TableauStore::new);
    }

    static TableauStore build(final LinearSolver.GeneralBuilder builder, final int... basis) {
        return SimplexStore.build(builder, TableauStore::new, basis);
    }

    private final int myColDim;
    private transient Primitive2D myConstraintsBody = null;
    private transient Primitive1D myConstraintsRHS = null;
    private double[] myCopiedObjectiveRow = null;
    private transient Primitive1D myObjective = null;
    private final double[][] myTableau;

    TableauStore(final int mm, final int nn) {
        this(new SimplexStructure(mm, nn));
    }

    TableauStore(final SimplexStructure structure) {

        super(structure);

        myTableau = new double[m + 1][n + 1];

        myColDim = n + 1;
    }

    public long countColumns() {
        return this.getColDim();
    }

    public long countRows() {
        return this.getRowDim();
    }

    public double doubleValue(final long row, final long col) {
        return this.doubleValue(Math.toIntExact(row), Math.toIntExact(col));
    }

    public Double get(final long row, final long col) {
        return Double.valueOf(this.doubleValue(row, col));
    }

    public int getColDim() {
        return myColDim;
    }

    public int getRowDim() {
        return myTableau.length;
    }

    private Primitive2D newConstraintsBody() {

        double[][] store = myTableau;

        return new Primitive2D() {

            @Override
            public double doubleValue(final int row, final int col) {
                return store[row][col];
            }

            @Override
            public int getColDim() {
                return TableauStore.this.n;
            }

            @Override
            public int getRowDim() {
                return TableauStore.this.m;
            }

            @Override
            public void set(final int row, final int col, final double value) {
                store[row][col] = value;
            }

        };
    }

    private Primitive1D newConstraintsRHS() {

        double[][] store = myTableau;

        return new Primitive1D() {

            @Override
            public double doubleValue(final int index) {
                return store[index][TableauStore.this.n];
            }

            @Override
            public void set(final int index, final double value) {
                store[index][TableauStore.this.n] = value;
            }

            @Override
            public int size() {
                return TableauStore.this.m;
            }

        };
    }

    private Primitive1D newObjective() {

        double[][] store = myTableau;

        return new Primitive1D() {

            @Override
            public double doubleValue(final int index) {
                return store[TableauStore.this.m][index];
            }

            @Override
            public void set(final int index, final double value) {
                store[TableauStore.this.m][index] = value;
            }

            @Override
            public int size() {
                return TableauStore.this.structure().nbProbVars;
            }

        };
    }

    private void pivot(final int row, final int col) {

        double[] pivotRow = myTableau[row];
        double pivotElement = pivotRow[col];

        if (pivotElement != ONE) {
            CorePrimitiveOperation.divide(pivotRow, 0, myColDim, 1, pivotRow, pivotElement);
        }

        for (int i = 0, limit = myTableau.length; i < limit; i++) {
            if (i != row) {
                TableauStore.pivotRow(myTableau[i], col, pivotRow, myColDim);
            }
        }

        if (myCopiedObjectiveRow != null) {
            TableauStore.pivotRow(myCopiedObjectiveRow, col, pivotRow, myColDim);
        }
    }

    @Override
    protected void pivot(final IterDescr iteration) {

        int row = iteration.exit.index;
        int col = iteration.enter.column();

        this.pivot(row, col);

        super.pivot(iteration);
    }

    @Override
    protected void shiftColumn(final int col, final double shift) {
        super.shiftColumn(col, shift);
        for (int i = 0; i < m; i++) {
            myTableau[i][n] -= shift * myTableau[i][col];
        }
    }

    @Override
    void calculateDualDirection(final ExitInfo exit) {
        // With a tableau all calculations are continuously done when pivoting
    }

    @Override
    void calculateIteration() {
        // With a tableau all calculations are continuously done when pivoting
    }

    @Override
    void calculateIteration(final IterDescr iteration) {
        // With a tableau all calculations are continuously done when pivoting
    }

    @Override
    void calculatePrimalDirection(final EnterInfo enter) {
        // With a tableau all calculations are continuously done when pivoting
    }

    /**
     * The area of the tableau corresponding to the constraints' body.
     *
     * @see org.ojalgo.optimisation.linear.SimplexStore#constraintsBody()
     */
    @Override
    Primitive2D constraintsBody() {
        if (myConstraintsBody == null) {
            myConstraintsBody = this.newConstraintsBody();
        }
        return myConstraintsBody;
    }

    /**
     * The area of the tableau corresponding to the constraints' RHS.
     *
     * @see org.ojalgo.optimisation.linear.SimplexStore#constraintsRHS()
     */
    @Override
    Primitive1D constraintsRHS() {
        if (myConstraintsRHS == null) {
            myConstraintsRHS = this.newConstraintsRHS();
        }
        return myConstraintsRHS;
    }

    @Override
    void copyBasicSolution(final double[] solution) {
        for (int i = 0; i < included.length; i++) {
            solution[included[i]] = myTableau[i][n];
        }
    }

    @Override
    void copyObjective() {
        myCopiedObjectiveRow = Arrays.copyOf(myTableau[m], myColDim);
    }

    double doubleValue(final int row, final int col) {
        return myTableau[row][col];
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
    double getCost(final int j) {
        return myTableau[m][j];
    }

    @Override
    double getInfeasibility(final int i) {

        int ii = included[i];

        double xi = myTableau[i][n];
        double lb = this.getLowerBound(ii);
        double ub = this.getUpperBound(ii);

        // BasicLogger.debug(1, "{}({}): {} < {} < {}", ii, i, lb, xi, ub);

        if (xi < lb) {
            return xi - lb; // Negative, lower bound infeasibility
        } else if (xi > ub) {
            return xi - ub; // Positive, upper bound infeasibility
        } else {
            return ZERO; // No infeasibility
        }
    }

    @Override
    double getReducedCost(final int je) {
        return myTableau[m][excluded[je]];
    }

    @Override
    double getTableauElement(final ExitInfo exit, final int je) {
        return myTableau[exit.index][excluded[je]];
    }

    @Override
    double getTableauElement(final int i, final EnterInfo enter) {
        return myTableau[i][enter.column()];
    }

    @Override
    double getTableauRHS(final int i) {
        return myTableau[i][n];
    }

    /**
     * The area of the tableau corresponding to the objective function.
     *
     * @see org.ojalgo.optimisation.linear.SimplexStore#objective()
     */
    @Override
    Primitive1D objective() {
        if (myObjective == null) {
            myObjective = this.newObjective();
        }
        return myObjective;
    }

    @Override
    void resetBasis(final int[] newBasis) {

        super.resetBasis(newBasis);

        for (int i = 0; i < newBasis.length; i++) {
            this.pivot(i, newBasis[i]);
        }
    }

    @Override
    void restoreObjective() {
        myTableau[m] = myCopiedObjectiveRow;
        myCopiedObjectiveRow = null;
    }

    void set(final int row, final int col, final double value) {
        myTableau[row][col] = value;
    }

}

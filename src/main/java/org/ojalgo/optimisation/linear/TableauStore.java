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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.ojalgo.array.operation.AXPY;
import org.ojalgo.array.operation.CorePrimitiveOperation;
import org.ojalgo.equation.Equation;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation.ProblemStructure;
import org.ojalgo.optimisation.linear.SimplexSolver.EnterInfo;
import org.ojalgo.optimisation.linear.SimplexSolver.ExitInfo;
import org.ojalgo.optimisation.linear.SimplexSolver.IterDescr;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Mutate2D;
import org.ojalgo.type.NumberDefinition;
import org.ojalgo.type.context.NumberContext;

final class TableauStore extends SimplexStore implements Access2D<Double>, Mutate2D {

    static enum FeatureSet {
        /**
         * Used by {@link SimplexTableauSolver}.
         */
        CLASSIC,
        /**
         * Used by {@link SimplexSolver}.
         */
        COMPACT;
    }

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
    private final FeatureSet myFeatureSet;
    private transient Primitive1D myObjective = null;
    private final double[][] myTableau;

    TableauStore(final int mm, final int nn) {
        this(new LinearStructure(mm, nn), FeatureSet.COMPACT);
    }

    TableauStore(final LinearStructure linearStructure) {
        this(linearStructure, FeatureSet.COMPACT);
    }

    TableauStore(final LinearStructure linearStructure, final FeatureSet featureSet) {

        super(linearStructure);

        myTableau = new double[m + 1][n + 1];

        myColDim = n + 1;

        myFeatureSet = featureSet;
    }

    @Override
    public long countColumns() {
        return this.getColDim();
    }

    @Override
    public long countRows() {
        return this.getRowDim();
    }

    @Override
    public double doubleValue(final int row, final int col) {
        return myTableau[row][col];
    }

    @Override
    public Double get(final long row, final long col) {
        return Double.valueOf(this.doubleValue(row, col));
    }

    @Override
    public int getColDim() {
        return myColDim;
    }

    @Override
    public int getRowDim() {
        return myTableau.length;
    }

    @Override
    public void set(final int row, final int col, final double value) {
        myTableau[row][col] = value;
    }

    @Override
    public final void set(final long row, final long col, final Comparable<?> value) {
        this.set(row, col, NumberDefinition.doubleValue(value));
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
                return n;
            }

            @Override
            public int getRowDim() {
                return m;
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
                return store[index][n];
            }

            @Override
            public void set(final int index, final double value) {
                store[index][n] = value;
            }

            @Override
            public int size() {
                return m;
            }

        };
    }

    private Primitive1D newObjective() {

        double[][] store = myTableau;

        return new Primitive1D() {

            @Override
            public double doubleValue(final int index) {
                return store[m][index];
            }

            @Override
            public void set(final int index, final double value) {
                store[m][index] = value;
            }

            @Override
            public int size() {
                return structure.countModelVariables();
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
    Collection<Equation> generateCutCandidates(final double[] solution, final boolean[] integer, final boolean[] negated, final NumberContext tolerance,
            final double fractionality) {

        int nbModVars = structure.countModelVariables();

        List<Equation> retVal = new ArrayList<>();

        Primitive1D constraintsRHS = this.constraintsRHS();

        double[] solRHS = new double[solution.length];
        for (int i = 0; i < m; i++) {
            int j = included[i];
            solRHS[j] = constraintsRHS.doubleValue(i);
        }
        if (ProblemStructure.DEBUG) {
            BasicLogger.debug("RHS: {}", Arrays.toString(solRHS));
            BasicLogger.debug("Bas: {}", Arrays.toString(included));
        }
        for (int j = 0; j < negated.length; j++) {
            //if (this.getEntityMap().isNegated(j)) {
            if (this.isNegated(j)) {
                negated[j] = true;
            } else {
                negated[j] = false;
            }
        }

        for (int i = 0; i < m; i++) {
            int j = included[i];

            Primitive1D sliceBodyRow = this.sliceBodyRow(i);

            // double rhs = sliceBodyRow.dot(Access1D.wrap(solution));
            double rhs = constraintsRHS.doubleValue(i);
            //double rhs = solution[j];

            if (j >= 0 && j < nbModVars && integer[j] && !tolerance.isInteger(rhs)) {

                Equation maybe = TableauCutGenerator.doGomoryMixedInteger(sliceBodyRow, j, rhs, integer, fractionality, negated, excluded);

                if (maybe != null) {
                    retVal.add(maybe);
                }
            }
        }

        return retVal;
    }

    int getBasisColumnIndex(final int basisRowIndex) {
        return included[basisRowIndex];
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

    Primitive1D sliceBodyRow(final int row) {

        return new Primitive1D() {

            @Override
            public double doubleValue(final int index) {
                return TableauStore.this.doubleValue(row, index);
            }

            @Override
            public void set(final int index, final double value) {
                TableauStore.this.set(row, index, value);
            }

            @Override
            public int size() {
                return TableauStore.this.n;
            }

        };
    }

    /**
     * @return An array of the dual variable values (of the original problem, never phase 1).
     */
    @Override
    final Primitive1D sliceDualVariables() {

        int base = n - m;

        return new Primitive1D() {

            @Override
            public double doubleValue(final int index) {
                return myObjective.doubleValue(base + index);
            }

            @Override
            public void set(final int index, final double value) {
                myObjective.set(base + index, value);
            }

            @Override
            public int size() {
                return n - base;
            }

        };
    }

}

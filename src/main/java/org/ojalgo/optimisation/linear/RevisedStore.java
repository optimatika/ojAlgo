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

import static org.ojalgo.function.constant.PrimitiveMath.ONE;
import static org.ojalgo.function.constant.PrimitiveMath.ZERO;

import java.util.Arrays;

import org.ojalgo.array.operation.AXPY;
import org.ojalgo.matrix.store.R064CSC;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.matrix.store.RowsSupplier;
import org.ojalgo.optimisation.linear.SimplexSolver.EnterInfo;
import org.ojalgo.optimisation.linear.SimplexSolver.ExitInfo;
import org.ojalgo.optimisation.linear.SimplexSolver.IterDescr;
import org.ojalgo.structure.Mutate1D;
import org.ojalgo.structure.Mutate2D;
import org.ojalgo.structure.Primitive1D;

/**
 * Revised simplex data structure. Instead of materialising the full tableau, this class stores the original
 * constraint matrix and maintains the basis inverse in factored form through a {@link BasisRepresentation}.
 * Tableau elements (pivot rows/columns, reduced costs, dual variables) are computed on demand by multiplying
 * through the factored inverse.
 * <p>
 * This approach is more memory-efficient and often faster for larger or sparser problems than
 * {@link DenseTableau}. It is the backing data structure for the {@link SimplexSolver} family
 * ({@link PhasedSimplexSolver}, {@link DualSimplexSolver}, {@link PrimalSimplexSolver}).
 */
final class RevisedStore extends SimplexStore {

    private static R064Store newColumn(final int nbRows) {
        return R064Store.FACTORY.make(nbRows, 1);
    }

    private static RowsSupplier<Double> newMatrix(final int nbRows, final int nbCols) {
        RowsSupplier<Double> retVal = R064Store.FACTORY.makeRowsSupplier(nbCols);
        retVal.addRows(nbRows);
        return retVal;
    }

    private static R064Store newRow(final int nbCols) {
        return R064Store.FACTORY.make(1, nbCols);
    }

    /**
     * Pivot row for dual simplex. Contains coefficients of non-basic variables in the tableau row for the
     * exiting basic variable. Updated incrementally with each dual simplex iteration.
     */
    private final double[] a;

    /**
     * Reduced costs for non-basic variables. Shows objective change per unit increase in a non-basic
     * variable. Updated incrementally with each iteration and pivot.
     */
    private final double[] d;

    /**
     * Dual variables (Lagrange multipliers) for constraints. Computed as π = B^(-T) * c_B. Updated as needed
     * for reporting or solution extraction.
     */
    private final double[] l;

    /**
     * Set after the basic solution has been fully computed (via {@link #refreshBasicSolution()}). Cleared on
     * {@link #resetBasis} so that {@link #setToLower}/{@link #setToUpper} skip redundant ftran calls during
     * the subsequent setup phase — {@link #prepareToIterate()} will recompute x from scratch anyway.
     */
    private boolean myBasicSolutionReady = false;

    /**
     * Complete constraint matrix A (all variables). Mutable during build; frozen to {@link #myConstraintsCSC}
     * before iteration.
     */
    private final RowsSupplier<Double> myConstraintsBody;

    /**
     * Frozen compressed-sparse-column form of the constraint matrix. Created from {@link #myConstraintsBody}
     * in {@link #doneBuilding()} and used for all column-access operations during the solve loop.
     */
    private R064CSC myConstraintsCSC;

    /**
     * Right-hand side vector b of Ax = b. Updated when bounds are shifted. Used to compute the current basic
     * solution.
     */
    private final R064Store myConstraintsRHS;

    /**
     * Inverse of the current basis matrix B^(-1). Maintained and updated using factorization techniques.
     * Updated when the basis changes or is reset. Initialised in {@link #doneBuilding()} once the constraint
     * matrix density is known.
     */
    private BasisRepresentation myInvBasis;

    /**
     * Set when the basis representation was fully refactored (rather than incrementally updated). Both the
     * basic solution x and reduced costs d are recomputed from scratch after refactorisation, since the fresh
     * factors make the recompute accurate. Between refactorisations, x and d are maintained incrementally.
     */
    private boolean myRefactored = false;

    /**
     * Objective function coefficients c for all variables. Static during solve. Used to compute duals and
     * objective value.
     */
    private final R064Store myObjective;

    /**
     * Phase-1 objective function (sum of artificial variables). Used only during phase-1. Created and set up
     * at the start of phase-1, and set to null after phase-1.
     */
    private R064Store myPhase1Objective = null;

    /**
     * Scratch buffer for row-scatter transpose-multiply. Length {@code n}, reused across iterations.
     */
    private final double[] w;

    /**
     * Current basic solution x_B. Values of basic variables in the current iteration. Updated incrementally
     * with each iteration and pivot.
     */
    private final double[] x;

    /**
     * Direction vector for entering variable in primal simplex. Shows how basic variables change when
     * entering variable increases. Updated incrementally with each iteration.
     */
    private final double[] y;

    /**
     * Temporary storage vector for various computations, especially rows of the inverse basis matrix. Reused
     * to avoid memory allocation. Updated as needed for intermediate calculations.
     */
    private final double[] z;

    RevisedStore(final int mm, final int nn) {
        this(new LinearStructure(mm, nn));
    }

    RevisedStore(final LinearStructure linearStructure) {

        super(linearStructure);

        myObjective = RevisedStore.newColumn(n);
        myConstraintsBody = RevisedStore.newMatrix(m, n);
        myConstraintsRHS = RevisedStore.newColumn(m);

        x = new double[m];
        y = new double[m];
        z = new double[m];
        l = new double[m];
        w = new double[n];

        d = new double[n - m];
        a = new double[n - m];
    }

    private void doBodyRow(final int row, final double[] destination) {

        Arrays.fill(z, ZERO);
        z[row] = ONE;
        myInvBasis.btran(z); // i:th row of inv B

        Arrays.fill(w, ZERO);

        for (int i = 0; i < m; i++) {
            double li = z[i];
            if (li != ZERO) {
                myConstraintsBody.getRow(i).axpy(li, w);
            }
        }

        for (int je = 0, lim = excluded.length; je < lim; je++) {
            int col = excluded[je];
            if (!this.isArtificial(col)) {
                destination[je] = w[col];
            }
        }
    }

    private double nonBasicValue(final int col, final ColumnState state) {
        if (state == ColumnState.LOWER) {
            return this.getLowerBound(col);
        } else if (state == ColumnState.UPPER) {
            return this.getUpperBound(col);
        } else {
            return ZERO;
        }
    }

    /**
     * Recompute x = B^{-1}(b - N*x_N) from scratch, eliminating accumulated rounding errors.
     */
    private void refreshBasicSolution() {

        System.arraycopy(myConstraintsRHS.data, 0, x, 0, m);

        for (int je = 0; je < excluded.length; je++) {
            int j = excluded[je];
            double v = this.nonBasicValue(j, this.getColumnState(j));
            if (v != ZERO) {
                myConstraintsCSC.axpy(j, -v, x);
            }
        }

        myInvBasis.ftran(x);
        myBasicSolutionReady = true;
    }

    private void updateDualsAndReducedCosts() {

        R064Store objective = myPhase1Objective != null ? myPhase1Objective : myObjective;
        double[] objData = objective.data;

        for (int ji = 0, lim = included.length; ji < lim; ji++) {
            l[ji] = objData[included[ji]];
        }
        myInvBasis.btran(l);

        for (int je = 0, lim = excluded.length; je < lim; je++) {
            int col = excluded[je];
            if (this.isArtificial(col)) {
                d[je] = objData[col];
            } else {
                d[je] = objData[col] - myConstraintsCSC.dot(col, l);
            }
        }
    }

    @Override
    protected void pivot(final IterDescr iteration) {

        int iterExitInd = iteration.exit.index;
        int iterEnterCol = iteration.enter.column();

        super.pivot(iteration);

        if (myInvBasis.update(myConstraintsCSC, included, iterExitInd, iterEnterCol)) {
            myRefactored = true;
        }
    }

    @Override
    void calculateDualDirection(final ExitInfo exit) {
        this.doBodyRow(exit.index, a);
    }

    @Override
    void calculateIteration(final SimplexSolver.IterDescr iteration) {

        int exit = iteration.exit.index;
        int enter = iteration.enter.index;

        if (iteration.isBasisUpdate()) {

            if (myRefactored) {
                this.updateDualsAndReducedCosts();
            } else {
                double stepD = d[enter] / a[enter];
                AXPY.invoke(d, -stepD, a);
                d[enter] = -stepD;
            }
        }

        if (myRefactored) {
            this.refreshBasicSolution();
            myRefactored = false;
        } else {
            // Post-pivot: exit.column() = old entering column, enter.column() = old exiting column
            int enterCol = iteration.exit.column();
            int exitCol = iteration.enter.column();

            double enterValue = this.nonBasicValue(enterCol, iteration.enter.from);
            double exitBound = this.nonBasicValue(exitCol, iteration.exit.to);

            double theta = (x[exit] - exitBound) / y[exit];
            AXPY.invoke(x, -theta, y);
            x[exit] = enterValue + theta;
        }
    }

    @Override
    void calculatePrimalDirection(final EnterInfo enter) {
        myConstraintsCSC.supplyTo(enter.column(), y);
        myInvBasis.ftran(y);
    }

    @Override
    Mutate2D constraintsBody() {
        return myConstraintsBody;
    }

    @Override
    Mutate1D constraintsRHS() {
        return myConstraintsRHS;
    }

    @Override
    void copyBasicSolution(final double[] solution) {
        for (int ji = 0; ji < included.length; ji++) {
            int j = included[ji];
            solution[j] = x[ji];
        }
    }

    @Override
    void doneBuilding() {
        myConstraintsCSC = myConstraintsBody.toCSC();
        myInvBasis = BasisRepresentation.newInstance(myConstraintsCSC);
    }

    @Override
    double[] extractSolution() {

        double[] retVal = new double[n];

        for (int je = 0; je < excluded.length; je++) {
            int j = excluded[je];
            ColumnState columnState = this.getColumnState(j);

            if (columnState == ColumnState.LOWER) {
                double lb = this.getLowerBound(j);
                retVal[j] = Double.isFinite(lb) ? lb : ZERO;
            } else if (columnState == ColumnState.UPPER) {
                double ub = this.getUpperBound(j);
                retVal[j] = Double.isFinite(ub) ? ub : ZERO;
            }
        }

        for (int ji = 0; ji < included.length; ji++) {
            retVal[included[ji]] = x[ji];
        }

        return retVal;
    }

    @Override
    double extractValue() {

        double retVal = ZERO;

        for (int ji = 0; ji < included.length; ji++) {
            retVal += x[ji] * myObjective.doubleValue(included[ji]);
        }

        for (int je = 0; je < excluded.length; je++) {
            int j = excluded[je];
            double v = this.nonBasicValue(j, this.getColumnState(j));
            if (v != ZERO) {
                retVal += v * myObjective.doubleValue(j);
            }
        }

        return retVal;
    }

    @Override
    double getCost(final int j) {
        return myObjective.doubleValue(j);
    }

    @Override
    double getCurrentElement(final ExitInfo exit, final int je) {
        return a[je];
    }

    @Override
    double getCurrentElement(final int i, final EnterInfo enter) {
        return y[i];
    }

    @Override
    double getCurrentRHS(final int i) {
        return x[i];
    }

    @Override
    double getInfeasibility(final int i) {

        int ii = included[i];

        double xi = x[i];
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
        return d[je];
    }

    @Override
    Mutate1D objective() {
        return myObjective;
    }

    @Override
    R064Store phase1() {

        if (myPhase1Objective == null) {
            myPhase1Objective = RevisedStore.newColumn(n);
        }

        return myPhase1Objective;
    }

    @Override
    void prepareToIterate() {
        myInvBasis.reset(myConstraintsCSC, included);
        this.updateDualsAndReducedCosts();
        this.refreshBasicSolution();
    }

    @Override
    void removePhase1() {
        myPhase1Objective = null;
        this.updateDualsAndReducedCosts();
    }

    @Override
    void resetBasis(final int[] basis) {

        super.resetBasis(basis);

        myInvBasis.reset(myConstraintsCSC, included);
        myBasicSolutionReady = false;
    }

    @Override
    void setToLower(final int col) {
        ColumnState prevState = this.getColumnState(col);
        this.lower(col);
        if (myBasicSolutionReady) {
            double delta = this.getLowerBound(col) - this.nonBasicValue(col, prevState);
            if (delta != ZERO) {
                myConstraintsCSC.supplyTo(col, y);
                myInvBasis.ftran(y);
                AXPY.invoke(x, -delta, y);
            }
        }
    }

    @Override
    void setToUpper(final int col) {
        ColumnState prevState = this.getColumnState(col);
        this.upper(col);
        if (myBasicSolutionReady) {
            double delta = this.getUpperBound(col) - this.nonBasicValue(col, prevState);
            if (delta != ZERO) {
                myConstraintsCSC.supplyTo(col, y);
                myInvBasis.ftran(y);
                AXPY.invoke(x, -delta, y);
            }
        }
    }

    @Override
    void setupClassicPhase1Objective() {

        int base = structure.nbIdty;

        if (myPhase1Objective == null) {
            myPhase1Objective = RevisedStore.newRow(myObjective.size());
        }

        int nbVariables = structure.countVariables();

        for (int j = 0; j < nbVariables; j++) {
            double sum = ZERO;
            for (int k = myConstraintsCSC.pointers[j], limit = myConstraintsCSC.pointers[j + 1]; k < limit; k++) {
                if (myConstraintsCSC.indices[k] >= base) {
                    sum += myConstraintsCSC.values[k];
                }
            }
            myPhase1Objective.set(j, -sum);
        }
    }

    @Override
    Primitive1D sliceBodyRow(final int i) {

        double[] exclPart = new double[n - m];

        this.doBodyRow(i, exclPart);

        Primitive1D retVal = Primitive1D.newInstance(n);
        for (int je = 0; je < excluded.length; je++) {
            retVal.set(excluded[je], exclPart[je]);
        }
        return retVal;
    }

    @Override
    Primitive1D sliceDualVariables() {

        this.updateDualsAndReducedCosts();

        return new Primitive1D() {

            @Override
            public double doubleValue(final int index) {
                return -l[index];
            }

            @Override
            public void set(final int index, final double value) {
                throw new UnsupportedOperationException();
            }

            @Override
            public int size() {
                return m;
            }

        };
    }

    @Override
    void updateDualEdgeWeights(final IterDescr iteration) {

        int p = iteration.exit.index;
        int je = iteration.enter.index;

        double pivotElement = a[je];

        if (Math.abs(pivotElement) > 1e-9) {

            double w_p = edgeWeights[p];

            for (int i = 0; i < included.length; i++) {

                if (i != p) {
                    double ratio = y[i] / pivotElement;
                    edgeWeights[i] += ratio * ratio * w_p;
                }
            }

            edgeWeights[p] = ONE;
        }
    }

    @Override
    void updatePrimalEdgeWeights(final IterDescr iteration) {

        int p = iteration.enter.index;

        double pivotElement = a[p];

        if (Math.abs(pivotElement) > 1e-9) {

            double w_p = edgeWeights[p];

            for (int je = 0; je < excluded.length; je++) {

                if (je != p) {
                    int column = excluded[je];
                    // Skip artificial variables in edge weight updates to match doExclTranspMult optimization
                    if (!this.isArtificial(column)) {
                        double ratio = a[je] / pivotElement;
                        edgeWeights[je] += ratio * ratio * w_p;
                    }
                    // Artificial variables keep their current edge weight (typically 1.0)
                }
            }

            edgeWeights[p] = ONE;
        }
    }

    @Override
    boolean updateRange(final int index, final double lower, final double upper) {
        this.setBounds(index, lower, upper);
        return true;
    }

}
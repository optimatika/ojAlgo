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

import org.ojalgo.array.SparseArray;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.matrix.store.ColumnsSupplier;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.optimisation.linear.SimplexSolver.EnterInfo;
import org.ojalgo.optimisation.linear.SimplexSolver.ExitInfo;
import org.ojalgo.optimisation.linear.SimplexSolver.IterDescr;
import org.ojalgo.structure.Mutate1D;
import org.ojalgo.structure.Mutate2D;
import org.ojalgo.structure.Primitive1D;

final class RevisedStore extends SimplexStore {

    private static BasisRepresentation newBasisRepresentation(final int nbConstraints) {
        // return new ProductFormInverse(nbConstraints, Prefix.MICRO); // 0 + 0
        // return new DecomposedInverse(false, nbConstraints); // 1 + 0
        return new DecomposedInverse(true, nbConstraints); // 0 + 0
    }

    private static R064Store newColumn(final int nbRows) {
        return R064Store.FACTORY.make(nbRows, 1);
    }

    private static ColumnsSupplier<Double> newMatrix(final int nbRows, final int nbCols) {
        ColumnsSupplier<Double> retVal = R064Store.FACTORY.makeColumnsSupplier(nbRows);
        retVal.addColumns(nbCols);
        return retVal;
    }

    private static R064Store newRow(final int nbCols) {
        return R064Store.FACTORY.make(1, nbCols);
    }

    /**
     * Pivot row for dual simplex. Contains coefficients of non-basic variables in the tableau row for the
     * exiting basic variable. Updated incrementally with each dual simplex iteration.
     */
    private final PhysicalStore<Double> a;

    /**
     * Reduced costs for non-basic variables. Shows objective change per unit increase in a non-basic
     * variable. Updated incrementally with each iteration and pivot.
     */
    private final R064Store d;

    /**
     * Dual variables (Lagrange multipliers) for constraints. Computed as π = B^(-T) * c_B. Updated as needed
     * for reporting or solution extraction.
     */
    private final R064Store l;

    /**
     * Current basis matrix B (columns of constraint matrix for basic variables). Not explicitly stored;
     * computed from constraint matrix and current basis indices. Updated when the basis changes.
     */
    private final MatrixStore<Double> myBasis;

    /**
     * Complete constraint matrix A (all variables). Static during solve. Used for column access and basis
     * updates.
     */
    private final ColumnsSupplier<Double> myConstraintsBody;

    /**
     * View of a single column from the constraint matrix. Reused for efficient access to columns during
     * computations.
     */
    private final ColumnsSupplier.SingleView<Double> myConstraintsColumn;

    /**
     * Right-hand side vector b of Ax = b. Updated when bounds are shifted. Used to compute the current basic
     * solution.
     */
    private final R064Store myConstraintsRHS;

    /**
     * Inverse of the current basis matrix B^(-1). Maintained and updated using factorization techniques.
     * Updated when the basis changes or is reset.
     */
    private final BasisRepresentation myInvBasis;

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
     * Temporary storage for reduced cost calculations. Used as intermediate storage during iterations and
     * dual variable updates.
     */
    private final R064Store r;

    /**
     * Current basic solution x_B. Values of basic variables in the current iteration. Updated incrementally
     * with each iteration and pivot.
     */
    private final R064Store x;

    /**
     * Direction vector for entering variable in primal simplex. Shows how basic variables change when
     * entering variable increases. Updated incrementally with each iteration.
     */
    private final R064Store y;

    /**
     * Temporary storage vector for various computations, especially rows of the inverse basis matrix. Reused
     * to avoid memory allocation. Updated as needed for intermediate calculations.
     */
    private final R064Store z;

    RevisedStore(final int mm, final int nn) {
        this(new LinearStructure(mm, nn));
    }

    RevisedStore(final LinearStructure linearStructure) {

        super(linearStructure);

        myObjective = RevisedStore.newColumn(n);
        myConstraintsBody = RevisedStore.newMatrix(m, n);
        myConstraintsRHS = RevisedStore.newColumn(m);

        myConstraintsColumn = myConstraintsBody.columns();

        x = RevisedStore.newColumn(m);
        y = RevisedStore.newColumn(m);
        z = RevisedStore.newColumn(m);
        l = RevisedStore.newColumn(m);

        d = RevisedStore.newColumn(n - m);
        a = RevisedStore.newColumn(n - m);
        r = RevisedStore.newColumn(n - m);

        myBasis = myConstraintsBody.columns(included);
        myInvBasis = RevisedStore.newBasisRepresentation(m);
    }

    private void doBodyRow(final int i, final PhysicalStore<Double> destination) {

        z.reset();
        z.set(i, ONE);
        myInvBasis.btran(z); // i:th row of inv B

        this.doExclTranspMult(z, destination);
    }

    private void doExclTranspMult(final MatrixStore<Double> lambda, final PhysicalStore<Double> results) {
        for (int je = 0; je < excluded.length; je++) {
            myConstraintsColumn.goToColumn(excluded[je]);
            results.set(je, myConstraintsColumn.dot(lambda));
        }
    }

    private void updateDualsAndReducedCosts() {
        R064Store objective = myPhase1Objective != null ? myPhase1Objective : myObjective;
        myInvBasis.btran(objective.rows(included), l);
        this.doExclTranspMult(l, r);
        d.fillMatching(objective.rows(excluded), SUBTRACT, r);
    }

    @Override
    protected void pivot(final IterDescr iteration) {

        int iterExitInd = iteration.exit.index;
        SparseArray<Double> iterEnterCol = myConstraintsBody.getColumn(iteration.enter.column());

        super.pivot(iteration);

        myInvBasis.update(myBasis, iterExitInd, iterEnterCol);
    }

    @Override
    protected void shiftColumn(final int col, final double shift) {
        super.shiftColumn(col, shift);
        myConstraintsBody.column(col).axpy(-shift, myConstraintsRHS);
        myInvBasis.ftran(myConstraintsRHS, x);
    }

    @Override
    void calculateDualDirection(final ExitInfo exit) {
        this.doBodyRow(exit.index, a);
    }

    @Override
    void calculateIteration(final SimplexSolver.IterDescr iteration, final double shift) {

        int exit = iteration.exit.index;
        int enter = iteration.enter.index;

        if (iteration.isBasisUpdate()) {
            // For basis updates, we need to update both x and d

            // Update reduced costs
            double stepD = d.doubleValue(enter) / a.doubleValue(enter);
            a.axpy(-stepD, d);
            d.set(enter, -stepD);
        }

        if (shift == ZERO) {

            double exitX = x.doubleValue(exit);
            double enterY = y.doubleValue(exit);
            double stepX = exitX / enterY;
            y.axpy(-stepX, x);
            x.set(exit, stepX);

        } else {

            myInvBasis.ftran(myConstraintsRHS, x);
        }
    }

    @Override
    void calculatePrimalDirection(final EnterInfo enter) {
        myConstraintsColumn.goToColumn(enter.column());
        myInvBasis.ftran(myConstraintsColumn, y);
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
            solution[j] = x.doubleValue(ji);
        }
    }

    @Override
    double extractValue() {

        double retVal = ZERO;

        double[] solution = this.extractSolution();

        for (int i = 0; i < solution.length; i++) {
            retVal += solution[i] * myObjective.doubleValue(i);
        }

        return retVal;
    }

    @Override
    double getCost(final int j) {
        return myObjective.doubleValue(j);
    }

    @Override
    double getCurrentElement(final ExitInfo exit, final int je) {
        return a.doubleValue(je);
    }

    @Override
    double getCurrentElement(final int i, final EnterInfo enter) {
        return y.doubleValue(i);
    }

    @Override
    double getCurrentRHS(final int i) {
        return x.doubleValue(i);
    }

    @Override
    double getInfeasibility(final int i) {

        int ii = included[i];

        double xi = x.doubleValue(i);
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
        return d.doubleValue(je);
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
        myInvBasis.reset(myBasis);
        this.updateDualsAndReducedCosts();
        myInvBasis.ftran(myConstraintsRHS, x);
    }

    @Override
    void removePhase1() {
        myPhase1Objective = null;
        this.updateDualsAndReducedCosts();
    }

    @Override
    void resetBasis(final int[] basis) {

        super.resetBasis(basis);

        myInvBasis.reset(myBasis);
    }

    @Override
    void setupClassicPhase1Objective() {

        int base = structure.nbIdty;

        if (myPhase1Objective == null) {
            myPhase1Objective = RevisedStore.newRow(myObjective.size());
        }

        int nbVariables = structure.countVariables();

        for (int j = 0; j < nbVariables; j++) {
            double sum = myConstraintsBody.aggregateColumn(base, j, Aggregator.SUM).doubleValue();
            myPhase1Objective.set(j, -sum);
        }

        // double sum = myConstraintsRHS.aggregateRange(structure.nbIdty, m, Aggregator.SUM).doubleValue();
        // myAlternativeValue = -sum;
    }

    @Override
    Primitive1D sliceBodyRow(final int i) {

        R064Store exclPart = RevisedStore.newColumn(n - m);

        this.doBodyRow(i, exclPart);

        Primitive1D retVal = Primitive1D.newInstance(n);
        for (int je = 0; je < excluded.length; je++) {
            retVal.set(excluded[je], exclPart.doubleValue(je));
        }
        return retVal;
    }

    @Override
    Primitive1D sliceDualVariables() {

        this.updateDualsAndReducedCosts();

        return new Primitive1D() {

            @Override
            public double doubleValue(final int index) {
                return -l.doubleValue(index);
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

        double pivotElement = a.doubleValue(je);

        if (Math.abs(pivotElement) > 1e-9) {

            double w_p = edgeWeights[p];

            for (int i = 0; i < included.length; i++) {

                if (i != p) {
                    double ratio = y.doubleValue(i) / pivotElement;
                    edgeWeights[i] += ratio * ratio * w_p;
                }
            }

            edgeWeights[p] = ONE;
        }
    }

    @Override
    void updatePrimalEdgeWeights(final IterDescr iteration) {

        int p = iteration.enter.index;

        double pivotElement = a.doubleValue(p);

        if (Math.abs(pivotElement) > 1e-9) {

            double w_p = edgeWeights[p];

            for (int je = 0; je < excluded.length; je++) {

                if (je != p) {
                    double ratio = a.doubleValue(je) / pivotElement;
                    edgeWeights[je] += ratio * ratio * w_p;
                }
            }

            edgeWeights[p] = ONE;
        }
    }

}

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.ojalgo.equation.Equation;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.linear.SimplexSolver.EnterInfo;
import org.ojalgo.optimisation.linear.SimplexSolver.ExitInfo;
import org.ojalgo.optimisation.linear.SimplexSolver.IterDescr;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.ElementView1D;
import org.ojalgo.structure.Mutate2D;
import org.ojalgo.structure.Primitive1D;
import org.ojalgo.structure.Primitive2D;
import org.ojalgo.type.NumberDefinition;
import org.ojalgo.type.context.NumberContext;

abstract class SimplexTableau extends SimplexStore implements Access2D<Double>, Mutate2D {

    /**
     * Used when pivoting to identify rows with elements that are already very close to zero (to avoid
     * updating those rows).
     */
    static final NumberContext PRECISION = NumberContext.of(15);

    static Function<LinearStructure, SimplexTableau> newTableauFactory(final Optimisation.Options options) {

        if (Boolean.TRUE.equals(options.sparse)) {
            return SparseTableau::new;
        } else {
            return DenseTableau::new;
        }
    }

    private transient Primitive2D myConstraintsBody = null;
    private transient Primitive1D myConstraintsRHS = null;
    private transient Primitive1D myObjective = null;

    SimplexTableau(final LinearStructure linearStructure) {
        super(linearStructure);
    }

    @Override
    public final Double get(final long row, final long col) {
        return Double.valueOf(this.doubleValue(row, col));
    }

    @Override
    public final void set(final long row, final long col, final Comparable<?> value) {
        this.set(row, col, NumberDefinition.doubleValue(value));
    }

    @Override
    public String toString() {
        return super.toString();
    }

    /**
     * Perform the pivot operation on the tableau – only. Various auxiliary bookkeeping should NOT be done
     * here.
     */
    protected abstract void doPivot(int row, int col);

    @Override
    protected final void pivot(final SimplexSolver.IterDescr iteration) {

        int row = iteration.exit.index;
        int col = iteration.enter.column();

        this.doPivot(row, col);

        super.pivot(iteration);
    }

    protected final void pivot(final SimplexTableauSolver.IterationPoint iteration) {

        int row = iteration.row;
        int col = iteration.col;

        this.doPivot(row, col);

        this.update(row, col);
    }

    @Override
    final void calculateDualDirection(final ExitInfo exit) {
        // With a tableau all calculations are continuously done when pivoting
    }

    @Override
    void calculateIteration(final IterDescr iteration, final double shift) {
        //
    }

    @Override
    final void calculatePrimalDirection(final EnterInfo enter) {
        // With a tableau all calculations are continuously done when pivoting
    }

    /**
     * The area of the tableau corresponding to the constraints' body.
     *
     * @see org.ojalgo.optimisation.linear.SimplexStore#constraintsBody()
     */
    @Override
    final Primitive2D constraintsBody() {
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
    final Primitive1D constraintsRHS() {
        if (myConstraintsRHS == null) {
            myConstraintsRHS = this.newConstraintsRHS();
        }
        return myConstraintsRHS;
    }

    final int findNextPivotColumn(final Access1D<Double> auxiliaryRow, final Access1D<Double> objectiveRow) {

        int retVal = -1;
        double minQuotient = MACHINE_LARGEST;

        for (ElementView1D<Double, ?> nz : auxiliaryRow.nonzeros()) {
            final int i = (int) nz.index();
            if (i >= structure.countVariables()) {
                break;
            }
            final double denominator = nz.doubleValue();
            if (denominator < -1E-8) {
                double numerator = objectiveRow.doubleValue(i);
                double quotient = Math.abs(numerator / denominator);
                if (quotient < minQuotient) {
                    minQuotient = quotient;
                    retVal = i;
                }
            }
        }

        return retVal;
    }

    abstract boolean fixVariable(int index, double value);

    /**
     * Simplified version of {@link #generateCutCandidates(boolean[], NumberContext, double, double[])} for
     * specific use with {@link SimplexTableauSolver}. {@link SimplexSolver} and {@link SimplexTableauSolver}
     * can both use {@link SimplexTableau}, but they use them differently.
     */
    final Collection<Equation> generateCutCandidates(final boolean[] integer, final NumberContext accuracy, final double fractionality) {

        if (this.countRemainingArtificials() > 0) {
            return Collections.emptyList();
        }

        int nbVars = integer.length;

        if (nbVars != structure.countVariables()) {
            BasicLogger.debug("generateCutCandidates: integer.length != structure.countVariables()");
        }

        List<Equation> retVal = new ArrayList<>();

        for (int i = 0; i < m; i++) {
            int j = included[i];

            double rhs = this.getCurrentRHS(i);

            if (j >= 0 && j < nbVars && integer[j] && !accuracy.isInteger(rhs)) {

                Equation maybe = TableauCutGenerator.doGomoryMixedInteger(this.sliceBodyRow(i), j, rhs, fractionality, excluded, integer);

                if (maybe != null) {
                    retVal.add(maybe);
                }
            }
        }

        return retVal;
    }

    @Override
    final double getCost(final int j) {
        // return myTableau[m][j];
        return this.objective().doubleValue(j);
    }

    @Override
    final double getCurrentElement(final ExitInfo exit, final int je) {
        // return myTableau[exit.index][excluded[je]];
        return this.constraintsBody().doubleValue(exit.index, excluded[je]);
    }

    @Override
    final double getCurrentElement(final int i, final EnterInfo enter) {
        // return myTableau[i][enter.column()];
        return this.constraintsBody().doubleValue(i, enter.column());
    }

    @Override
    final double getCurrentRHS(final int i) {
        // return myTableau[i][n];
        return this.constraintsRHS().doubleValue(i);
    }

    /**
     * @return The phase 1 objective function value
     */
    abstract double getInfeasibility();

    @Override
    final double getInfeasibility(final int i) {

        int j = included[i];

        double xi = this.getCurrentRHS(i);
        double lb = this.getLowerBound(j);
        double ub = this.getUpperBound(j);

        // BasicLogger.debug(1, "{}({}): {} < {} < {}", ii, i, lb, xi, ub);

        if (xi < lb) {
            return xi - lb; // Negative, lower bound infeasibility
        } else if (xi > ub) {
            return xi - ub; // Positive, upper bound infeasibility
        } else {
            return ZERO; // No infeasibility
        }
    }

    /**
     * @return The (phase 2) objective function value
     */
    abstract double getValue();

    abstract Primitive2D newConstraintsBody();

    abstract Primitive1D newConstraintsRHS();

    abstract Primitive1D newObjective();

    abstract Primitive1D newPhase1();

    final SimplexTableauSolver newSimplexTableauSolver(final Optimisation.Options optimisationOptions) {
        return new SimplexTableauSolver(this, optimisationOptions);
    }

    /**
     * The area of the tableau corresponding to the objective function.
     *
     * @see org.ojalgo.optimisation.linear.SimplexStore#objective()
     */
    @Override
    final Primitive1D objective() {
        if (myObjective == null) {
            myObjective = this.newObjective();
        }
        return myObjective;
    }

    @Override
    void prepareToIterate() {
        // TODO Auto-generated method stub
            }

    final Primitive1D phase1() {
        return this.newPhase1();
    }

    @Override
    final void resetBasis(final int[] newBasis) {

        super.resetBasis(newBasis);

        for (int i = 0; i < newBasis.length; i++) {
            this.doPivot(i, newBasis[i]);
        }
    }

    @Override
    final void setupClassicPhase1Objective() {
        // Happens while the tableau is initially built
    }

    final Primitive1D sliceBodyColumn(final int col) {

        Primitive2D body = this.constraintsBody();

        return new Primitive1D() {

            @Override
            public double doubleValue(final int index) {
                return body.doubleValue(index, col);
            }

            @Override
            public void set(final int index, final double value) {
                body.set(index, col, value);
            }

            @Override
            public int size() {
                return m;
            }

        };
    }

    @Override
    final Primitive1D sliceBodyRow(final int row) {

        Primitive2D body = this.constraintsBody();

        return new Primitive1D() {

            @Override
            public double doubleValue(final int index) {
                return body.doubleValue(row, index);
            }

            @Override
            public void set(final int index, final double value) {
                body.set(row, index, value);
            }

            @Override
            public int size() {
                return n;
            }

        };
    }

    final Primitive1D sliceConstraintsRHS() {
        return this.constraintsRHS();
    }

    /**
     * @return An array of the dual variable values (of the original problem, never phase 1).
     */
    @Override
    final Primitive1D sliceDualVariables() {

        Primitive1D slice = this.objective();
        int base = n - m;

        return new Primitive1D() {

            @Override
            public double doubleValue(final int index) {
                return slice.doubleValue(base + index);
            }

            @Override
            public void set(final int index, final double value) {
                slice.set(base + index, value);
            }

            @Override
            public int size() {
                return m;
            }

        };
    }

    final Primitive1D sliceTableauColumn(final int col) {

        return new Primitive1D() {

            @Override
            public double doubleValue(final int index) {
                return SimplexTableau.this.doubleValue(index, col);
            }

            @Override
            public void set(final int index, final double value) {
                SimplexTableau.this.set(index, col, value);
            }

            @Override
            public int size() {
                return SimplexTableau.this.getRowDim();
            }

        };
    }

    final Primitive1D sliceTableauRow(final int row) {

        return new Primitive1D() {

            @Override
            public double doubleValue(final int index) {
                return SimplexTableau.this.doubleValue(row, index);
            }

            @Override
            public void set(final int index, final double value) {
                SimplexTableau.this.set(row, index, value);
            }

            @Override
            public int size() {
                return SimplexTableau.this.getColDim();
            }

        };
    }

    @Override
    void updateDualEdgeWeights(final IterDescr iteration) {

        int p = iteration.exit.index;
        int j = iteration.enter.column();

        double pivotElement = this.doubleValue(p, j);

        if (Math.abs(pivotElement) > 1e-9) {

            double w_p = edgeWeights[p];

            for (int i = 0; i < included.length; i++) {

                if (i != p) {
                    double ratio = this.doubleValue(i, j) / pivotElement;
                    edgeWeights[i] += ratio * ratio * w_p;
                }
            }

            edgeWeights[p] = ONE;
        }
    }

    @Override
    void updatePrimalEdgeWeights(final IterDescr iteration) {

        int i = iteration.exit.index;
        int p = iteration.enter.index;

        double pivotElement = this.doubleValue(i, excluded[p]);

        if (Math.abs(pivotElement) > 1e-9) {

            double w_p = edgeWeights[p];

            for (int je = 0; je < excluded.length; je++) {

                if (je != p) {
                    double ratio = this.doubleValue(i, excluded[je]) / pivotElement;
                    edgeWeights[je] += ratio * ratio * w_p;
                }
            }

            edgeWeights[p] = ONE;
        }
    }

    /**
     * The current, phase 1 or 2, objective function value
     */
    final double value(final boolean phase1) {
        if (phase1) {
            return this.getInfeasibility();
        } else {
            return this.getValue();
        }
    }

}

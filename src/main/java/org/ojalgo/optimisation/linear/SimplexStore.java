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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.ojalgo.equation.Equation;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.Options;
import org.ojalgo.optimisation.integer.IntegerSolver;
import org.ojalgo.optimisation.linear.SimplexSolver.EnterInfo;
import org.ojalgo.optimisation.linear.SimplexSolver.ExitInfo;
import org.ojalgo.optimisation.linear.SimplexSolver.IterDescr;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Mutate1D;
import org.ojalgo.structure.Mutate2D;
import org.ojalgo.structure.Primitive1D;
import org.ojalgo.structure.Structure1D;
import org.ojalgo.type.EnumPartition;
import org.ojalgo.type.context.NumberContext;

/**
 * Abstract internal data structure shared by the simplex solver implementations. Holds the constraint matrix,
 * objective, variable bounds, basis partition, and current solution state needed to execute simplex
 * iterations.
 * <p>
 * Two concrete families extend this class:
 * <ul>
 * <li>{@link SimplexTableau} — stores the full (or sparse) simplex tableau explicitly. Used by
 * {@link SimplexTableauSolver}.
 * <li>{@link RevisedStore} — maintains the basis inverse in factored form via a {@link BasisRepresentation},
 * computing tableau elements on demand. Used by {@link SimplexSolver} and its subclasses.
 * </ul>
 * Non-basic variables are partitioned into {@link ColumnState#LOWER}, {@link ColumnState#UPPER}, or
 * {@link ColumnState#UNBOUNDED}; basic variables are {@link ColumnState#BASIS}. The partition is updated on
 * each pivot.
 */
abstract class SimplexStore {

    enum ColumnState {

        /**
         * Basic variables
         */
        BASIS('B'),
        /**
         * Variables at their lower bound - may increase
         */
        LOWER('L'),
        /**
         * Used for unbounded variables that are not (yet) in the basis. Assigned value 0.0 and may either
         * increase or decrease.
         */
        UNBOUNDED('–'),
        /**
         * Variables at their upper bound – may decrease
         */
        UPPER('U');

        private final String myKey;

        ColumnState(final char key) {
            myKey = String.valueOf(key);
        }

        String key() {
            return myKey;
        }
    }

    static Function<LinearStructure, SimplexStore> newStoreFactory(final Options options) {

        return structure -> {

            long size = structure.getProblemSize();
            double ratio = structure.getProblemRatio();

            if (Boolean.TRUE.equals(options.sparse)) {
                return new RevisedStore(structure);
            } else if (Boolean.FALSE.equals(options.sparse)) {
                return new DenseTableau(structure);
            } else {
                if ((size > 2_400_000L && ratio > 3.5) || size >= 25_000_000L || ratio >= 11.0) {
                    return new RevisedStore(structure);
                } else {
                    return new DenseTableau(structure);
                }
            }
        };
    }

    private final double[] myLowerBounds;
    private final EnumPartition<SimplexStore.ColumnState> myPartition;
    private int myRemainingArtificials;
    private final List<String> myToStringList = new ArrayList<>();
    private final double[] myUpperBounds;

    /**
     * Either the primal or dual devex edge weights, depending on the algorithm used. Sized so that it can
     * hold either.
     */
    final double[] edgeWeights;
    /**
     * excluded == not in the basis
     */
    final int[] excluded;
    /**
     * included == in the basis
     */
    final int[] included;
    /**
     * The number of constraints (upper, lower and equality)
     */
    final int m;
    /**
     * The number of variables totally (all kinds)
     */
    final int n;
    final LinearStructure structure;

    SimplexStore(final LinearStructure linearStructure) {

        super();

        structure = linearStructure;

        m = linearStructure.countConstraints();
        n = linearStructure.countVariablesTotally();

        myLowerBounds = new double[n];
        myUpperBounds = new double[n];

        excluded = Structure1D.newIncreasingRange(0, n - m);
        included = Structure1D.newIncreasingRange(n - m, m);

        myPartition = new EnumPartition<>(n, ColumnState.BASIS);

        myRemainingArtificials = linearStructure.nbArti;

        edgeWeights = new double[Math.max(m, n - m)];
    }

    @Override
    public String toString() {

        myToStringList.clear();

        for (int i = 0; i < myPartition.size(); i++) {
            myToStringList.add(myPartition.get(i).key());
        }

        return myToStringList.toString();
    }

    private final SimplexStore basis(final int index) {
        myPartition.update(index, ColumnState.BASIS);
        return this;
    }

    private <S extends SimplexSolver> S newSolver(final BiFunction<Optimisation.Options, SimplexStore, S> constructor, final Optimisation.Options options,
            final int... basis) {
        this.doneBuilding();
        S solver = constructor.apply(options, this);
        if (basis.length > 0) {
            solver.basis(basis);
        }
        return solver;
    }

    protected void pivot(final SimplexSolver.IterDescr iteration) {

        ExitInfo exit = iteration.exit;
        EnterInfo enter = iteration.enter;

        if (this.isArtificial(exit.column())) {
            --myRemainingArtificials;
        }

        this.updateBasis(exit.index, exit.to, enter.index);
    }

    /**
     * Directly set the internal (possibly shifted) bounds for a variable. For use by subclasses that manage
     * their own bound shifting.
     */
    protected void setBounds(final int index, final double lower, final double upper) {
        myLowerBounds[index] = lower;
        myUpperBounds[index] = upper;
    }

    /**
     * Directly shift the internal bounds for a variable. For use by subclasses that implement shifting.
     */
    protected void shiftBounds(final int col, final double shift) {
        myLowerBounds[col] -= shift;
        myUpperBounds[col] -= shift;
    }

    abstract void calculateDualDirection(ExitInfo exit);

    /**
     * Post-pivot processing: updates the basic solution and reduced costs after a pivot or bound flip.
     * Subclasses implement the mechanics appropriate to their representation.
     */
    abstract void calculateIteration(IterDescr iteration);

    abstract void calculatePrimalDirection(EnterInfo enter);

    /**
     * The simplex' constraints body (including the parts corresponding to slack and artificial variables).
     */
    abstract Mutate2D constraintsBody();

    /**
     * The simplex' constraints RHS.
     */
    abstract Mutate1D constraintsRHS();

    abstract void copyBasicSolution(double[] solution);

    /**
     * The number of artificial variables in the basis.
     */
    final int countRemainingArtificials() {
        return myRemainingArtificials;
    }

    /**
     * Called once after the constraint matrix, RHS, and objective have been fully populated and before any
     * solver operations (basis reset, shift, iteration). Subclasses may override to freeze mutable build-time
     * data structures into efficient solve-time representations.
     */
    void doneBuilding() {
        // no-op by default
    }

    /**
     * Extract the dual variables (Lagrange multipliers) for all constraints into the given array. The array
     * must have length {@link #m}.
     */
    final void extractDualVariables(final double[] target) {
        Primitive1D duals = this.sliceDualVariables();
        for (int i = 0; i < m; i++) {
            target[i] = duals.doubleValue(i);
        }
    }

    /**
     * Extract the reduced costs for all variables into the given array. The array must have length
     * {@link #n}. Basic variables get 0; non-basic variables get their reduced cost from the excluded array.
     */
    final void extractReducedCosts(final double[] target) {
        for (int ji = 0; ji < included.length; ji++) {
            target[included[ji]] = ZERO;
        }
        for (int je = 0; je < excluded.length; je++) {
            target[excluded[je]] = this.getReducedCost(je);
        }
    }

    abstract double[] extractSolution();

    abstract double extractValue();

    Equation generateCut(final Primitive1D body, final int index, final double rhs, final double fractionality, final int[] excluded, final boolean[] integers,
            final double[] lowers, final double[] uppers) {
        return TableauCutGenerator.doGomoryMixedInteger(body, index, rhs, fractionality, excluded, integers, lowers, uppers);
    }

    /**
     * When {@link SimplexSolver} is used as node solver for {@link IntegerSolver} this method generates cut
     * candidates.
     */
    final Collection<Equation> generateCutCandidates(final boolean[] integer, final NumberContext accuracy, final double fractionality) {

        if (myRemainingArtificials > 0) {
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

                Equation maybe = this.generateCut(this.sliceBodyRow(i), j, rhs, fractionality, excluded, integer, myLowerBounds, myUpperBounds);

                if (maybe != null) {
                    retVal.add(maybe);
                }
            }
        }

        return retVal;
    }

    final ColumnState getColumnState(final int index) {
        return myPartition.get(index);
    }

    abstract double getCost(int i);

    /**
     * The current (tableau) constraint body element.
     */
    abstract double getCurrentElement(ExitInfo exit, int je);

    /**
     * The current (tableau) constraint body element.
     */
    abstract double getCurrentElement(int i, EnterInfo enter);

    /**
     * The current (tableau) constraint RHS.
     */
    abstract double getCurrentRHS(int i);

    abstract double getInfeasibility(int i);

    final double getLowerBound(final int index) {
        return myLowerBounds[index];
    }

    final double[] getLowerBounds() {
        return myLowerBounds;
    }

    /**
     * The "distance" from the current basic value to the lower bound.
     */
    final double getLowerGap(final int i) {

        double xi = this.getCurrentRHS(i);
        double lb = this.getLowerBound(included[i]);

        if (xi > lb) {
            return xi - lb;
        } else {
            return ZERO;
        }
    }

    /**
     * The lower bound in original (unshifted) space. Subclasses that shift bounds must override.
     */
    double getOriginalLowerBound(final int index) {
        return myLowerBounds[index];
    }

    /**
     * The upper bound in original (unshifted) space. Subclasses that shift bounds must override.
     */
    double getOriginalUpperBound(final int index) {
        return myUpperBounds[index];
    }

    /**
     * {@link #getUpperBound(int)} minus {@link #getLowerBound(int)}
     */
    final double getRange(final int index) {
        return myUpperBounds[index] - myLowerBounds[index];
    }

    abstract double getReducedCost(int je);

    final double getUpperBound(final int index) {
        return myUpperBounds[index];
    }

    final double[] getUpperBounds() {
        return myUpperBounds;
    }

    /**
     * The "distance" from the current basic value to the upper bound.
     */
    final double getUpperGap(final int i) {

        double xi = this.getCurrentRHS(i);
        double ub = this.getUpperBound(included[i]);

        if (ub > xi) {
            return ub - xi;
        } else {
            return ZERO;
        }
    }

    final boolean isArtificial(final int col) {
        return structure.isArtificialVariable(col);
    }

    final boolean isExcluded(final int index) {
        return !myPartition.is(index, ColumnState.BASIS);
    }

    final boolean isIncluded(final int index) {
        return myPartition.is(index, ColumnState.BASIS);
    }

    final boolean isNegated(final int j) {
        return myUpperBounds[j] <= ZERO && myLowerBounds[j] < ZERO;
    }

    /**
     * The problem is small enough to be explicitly printed/logged – log the entire tableau at each iteration
     * when debugging.
     */
    final boolean isPrintable() {
        return structure.countVariablesTotally() <= 32;
    }

    /**
     * Are there any artificial variables in the basis?
     */
    final boolean isRemainingArtificials() {
        return myRemainingArtificials > 0;
    }

    final SimplexStore lower(final int index) {
        myPartition.update(index, ColumnState.LOWER);
        return this;
    }

    final DualSimplexSolver newDualSimplexSolver(final Optimisation.Options options, final int... basis) {
        return this.newSolver(DualSimplexSolver::new, options, basis);

    }

    final PhasedSimplexSolver newPhasedSimplexSolver(final Optimisation.Options options, final int... basis) {
        return this.newSolver(PhasedSimplexSolver::new, options, basis);

    }

    final PrimalSimplexSolver newPrimalSimplexSolver(final Optimisation.Options options, final int... basis) {
        return this.newSolver(PrimalSimplexSolver::new, options, basis);
    }

    /**
     * The objective function.
     */
    abstract Mutate1D objective();

    /**
     * The phase-1 objective function.
     */
    abstract <T extends Mutate1D & Access1D<Double>> T phase1();

    abstract void prepareToIterate();

    abstract void removePhase1();

    /**
     * Everything that is not in the basis is set to be in at lower bound.
     */
    void resetBasis(final int[] newBasis) {

        if (newBasis.length != m) {
            throw new IllegalStateException();
        }

        myPartition.reset(ColumnState.LOWER);

        for (int i = 0; i < newBasis.length; i++) {
            myPartition.update(newBasis[i], ColumnState.BASIS);
            included[i] = newBasis[i];
        }

        myPartition.extract(ColumnState.BASIS, true, excluded);
    }

    void resetEdgeWeights() {
        Arrays.fill(edgeWeights, ONE);
    }

    /**
     * Set the non-basic variable to its lower bound. Updates the partition and performs any subclass-specific
     * bookkeeping (e.g. shifting for tableau, value tracking for revised).
     */
    abstract void setToLower(int col);

    /**
     * Set the non-basic variable to its upper bound. Updates the partition and performs any subclass-specific
     * bookkeeping.
     */
    abstract void setToUpper(int col);

    abstract void setupClassicPhase1Objective();

    abstract Primitive1D sliceBodyRow(final int row);

    abstract Primitive1D sliceDualVariables();

    final SimplexStore unbounded(final int index) {
        myPartition.update(index, ColumnState.UNBOUNDED);
        return this;
    }

    final void update(final int exit, final int exclEnter) {

        int inclExit = included[exit];
        if (inclExit >= 0) {
            this.lower(inclExit);
        }
        if (this.isArtificial(inclExit)) {
            --myRemainingArtificials;
        }

        if (exclEnter >= 0) {
            this.basis(exclEnter);
        }

        included[exit] = exclEnter;
        myPartition.extract(ColumnState.BASIS, true, excluded);
    }

    final void updateBasis(final int exit, final ColumnState exitToBound, final int enter) {

        int inclExit = included[exit];
        int exclEnter = excluded[enter];

        if (exitToBound == ColumnState.LOWER) {
            this.lower(inclExit);
        } else if (exitToBound == ColumnState.UPPER) {
            this.upper(inclExit);
        } else {
            throw new IllegalArgumentException();
        }

        this.basis(exclEnter);

        included[exit] = exclEnter;
        excluded[enter] = inclExit;
    }

    /**
     * Update edge weights for basic (included) variables.
     */
    abstract void updateDualEdgeWeights(final IterDescr iteration);

    /**
     * Update edge weights for non-basic (excluded) variables.
     */
    abstract void updatePrimalEdgeWeights(final IterDescr iteration);

    /**
     * Accepts bounds in original (unshifted) space and updates the internal representation.
     */
    abstract boolean updateRange(int index, double lower, double upper);

    final SimplexStore upper(final int index) {
        myPartition.update(index, ColumnState.UPPER);
        return this;
    }

}

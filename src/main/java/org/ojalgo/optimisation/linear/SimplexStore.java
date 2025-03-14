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

import static org.ojalgo.function.constant.PrimitiveMath.ZERO;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.ojalgo.equation.Equation;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.Options;
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

            if (Boolean.TRUE.equals(options.sparse)
                    || !Boolean.FALSE.equals(options.sparse) && Math.max(structure.countModelVariables(), structure.countConstraints()) > 2_000) {
                return new RevisedStore(structure);
            } else {
                return new DenseTableau(structure);
            }
        };
    }

    private transient int[] myExcludedLower = null;
    private transient int[] myExcludedUnbounded = null;
    private transient int[] myExcludedUpper = null;
    private final double[] myLowerBounds;
    private final EnumPartition<SimplexStore.ColumnState> myPartition;
    private int myRemainingArtificials;
    private final List<String> myToStringList = new ArrayList<>();
    private final double[] myUpperBounds;

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

    protected void shiftColumn(final int col, final double shift) {
        myLowerBounds[col] -= shift;
        myUpperBounds[col] -= shift;
    }

    abstract void calculateDualDirection(ExitInfo exit);

    abstract void calculateIteration();

    abstract void calculateIteration(IterDescr iteration, double shift);

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

    final double[] extractSolution() {

        double[] retVal = new double[n];

        int[] lower = this.getExcludedLower();
        for (int i = 0; i < lower.length; i++) {
            int j = lower[i];
            double value = this.getLowerBound(j);
            if (Double.isFinite(value)) {
                retVal[j] = value;
            } else {
                retVal[j] = ZERO;
            }
        }

        int[] upper = this.getExcludedUpper();
        for (int i = 0; i < upper.length; i++) {
            int j = upper[i];
            double value = this.getUpperBound(j);
            if (Double.isFinite(value)) {
                retVal[j] = value;
            } else {
                retVal[j] = ZERO;
            }
        }

        this.copyBasicSolution(retVal);

        return retVal;
    }

    abstract double extractValue();

    final Collection<Equation> generateCutCandidates(final boolean[] integer, final NumberContext accuracy, final double fractionality, final double[] shift) {

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

                Equation maybe = TableauCutGenerator.doGomoryMixedInteger(this.sliceBodyRow(i), j, rhs, fractionality, excluded, integer, myLowerBounds,
                        myUpperBounds, shift);

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

    /**
     * Indices of columns/variables at their lower bounds.
     */
    final int[] getExcludedLower() {
        int count = myPartition.count(ColumnState.LOWER);
        if (myExcludedLower == null || myExcludedLower.length != count) {
            myExcludedLower = new int[count];
        }
        myPartition.extract(ColumnState.LOWER, false, myExcludedLower);
        return myExcludedLower;
    }

    /**
     * Indices of unbounded columns/variables.
     */
    final int[] getExcludedUnbounded() {
        int count = myPartition.count(ColumnState.UNBOUNDED);
        if (myExcludedUnbounded == null || myExcludedUnbounded.length != count) {
            myExcludedUnbounded = new int[count];
        }
        myPartition.extract(ColumnState.UNBOUNDED, false, myExcludedUnbounded);
        return myExcludedUnbounded;
    }

    /**
     * Indices of columns/variables at their upper bounds.
     */
    final int[] getExcludedUpper() {
        int count = myPartition.count(ColumnState.UPPER);
        if (myExcludedUpper == null || myExcludedUpper.length != count) {
            myExcludedUpper = new int[count];
        }
        myPartition.extract(ColumnState.UPPER, false, myExcludedUpper);
        return myExcludedUpper;
    }

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

    boolean updateRange(final int index, final double lower, final double upper) {
        myLowerBounds[index] = lower;
        myUpperBounds[index] = upper;
        return true;
    }

    final SimplexStore upper(final int index) {
        myPartition.update(index, ColumnState.UPPER);
        return this;
    }

}

/*
 * Copyright 1997-2021 Optimatika
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

import org.ojalgo.array.Array1D;
import org.ojalgo.array.LongToNumberMap;
import org.ojalgo.array.Primitive64Array;
import org.ojalgo.array.SparseArray.NonzeroView;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.GenericSolver;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.linear.SimplexTableau.IterationPoint;
import org.ojalgo.structure.Access1D;
import org.ojalgo.type.context.NumberContext;

/**
 * The general recommendation is to construct optimisation problems using {@linkplain ExpressionsBasedModel}
 * and not worry about solver details.
 *
 * @author apete
 */
public abstract class SimplexSolver extends LinearSolver {

    interface AlgorithmStore {

    }

    private static final NumberContext DEGENERATE = ACCURACY.withScale(8);
    private static final NumberContext PHASE1 = ACCURACY.withScale(7);
    private static final NumberContext PIVOT = ACCURACY.withScale(8);
    private static final NumberContext RATIO = ACCURACY.withScale(8);
    private static final NumberContext WEIGHT = ACCURACY.withPrecision(8).withScale(10);

    private LongToNumberMap<Double> myFixedVariables = null;
    private final IterationPoint myPoint;
    private final SimplexTableau myTableau;

    SimplexSolver(final SimplexTableau tableau, final Optimisation.Options solverOptions) {

        super(solverOptions);

        myTableau = tableau;

        myPoint = new IterationPoint();

        if (this.isLogProgress()) {
            this.log("");
            this.log("Created SimplexSolver");
            this.log("countVariables: {}", tableau.countVariables());
            this.log("countProblemVariables: {}", tableau.countProblemVariables());
            this.log("countSlackVariables: {}", tableau.countSlackVariables());
            this.log("countArtificialVariables: {}", tableau.countArtificialVariables());
            this.log("countVariablesTotally: {}", tableau.countVariablesTotally());
            this.log("countConstraints: {}", tableau.countConstraints());
            this.log("countBasicArtificials: {}", tableau.countBasicArtificials());
            this.log("countBasisDeficit: {}", tableau.countBasisDeficit());
        }

        if (this.isLogDebug() && this.isTableauPrintable()) {
            this.logDebugTableau("Tableau Created");
        }
    }

    public boolean fixVariable(final int index, final double value) {

        if (value < ZERO) {
            return false;
        }

        boolean retVal = myTableau.fixVariable(index, value);

        if (retVal) {
            if (myFixedVariables == null) {
                myFixedVariables = LongToNumberMap.factory(Primitive64Array.FACTORY).make();
            }
            myFixedVariables.put(index, value);
            myPoint.returnToPhase1();
        }

        return retVal;
    }

    public Result solve(final Result kickStarter) {

        if (this.isLogDebug() && this.isTableauPrintable()) {
            this.logDebugTableau("Initial Tableau");
        }

        this.resetIterationsCount();

        while (this.isIterationAllowed() && this.needsAnotherIteration()) {

            this.performIteration(myPoint);

            this.incrementIterationsCount();

            if (this.isLogDebug() && this.isTableauPrintable()) {
                this.logDebugTableau("Tableau Iteration");
            }
        }

        return this.buildResult();
    }

    private int getRowObjective() {
        return myPoint.isPhase1() ? myTableau.countConstraints() + 1 : myTableau.countConstraints();
    }

    private boolean isTableauPrintable() {
        return myTableau.count() <= 512L;
    }

    private void logDebugTableau(final String message) {
        this.log(message + "; Basics: " + Arrays.toString(myTableau.getBasis()), myTableau);
        // this.debug("New/alt " + message + "; Basics: " + Arrays.toString(myBasis), myTableau);
    }

    private double objective() {
        return -myTableau.doubleValue(this.getRowObjective(), myTableau.countConstraints() + myTableau.countVariables());
    }

    private int phase() {
        return myPoint.isPhase2() ? 2 : 1;
    }

    @Override
    protected Result buildResult() {
        return super.buildResult().multipliers(this.extractMultipliers());
    }

    @Override
    protected double evaluateFunction(final Access1D<?> solution) {
        return this.objective();
    }

    protected Access1D<?> extractMultipliers() {

        Access1D<Double> duals = myTableau.sliceDualVariables();
        boolean[] negative = myTableau.negative;

        return new Access1D<Double>() {

            public long count() {
                return negative.length;
            }

            public double doubleValue(final long index) {
                int i = Math.toIntExact(index);
                return negative[i] ? -duals.doubleValue(index) : duals.doubleValue(index);
            }

            public Double get(final long index) {
                return this.doubleValue(index);
            }

            @Override
            public String toString() {
                return Access1D.toString(this);
            }
        };
    }

    /**
     * Extract solution MatrixStore from the tableau
     */
    @Override
    protected Access1D<?> extractSolution() {

        int colRHS = myTableau.countConstraints() + myTableau.countVariables();

        Primitive64Store solution = Primitive64Store.FACTORY.make(myTableau.countVariables(), 1);

        int numberOfConstraints = myTableau.countConstraints();
        for (int row = 0; row < numberOfConstraints; row++) {
            int variableIndex = myTableau.getBasisColumnIndex(row);
            if (variableIndex >= 0) {
                solution.set(variableIndex, myTableau.doubleValue(row, colRHS));
            }
        }

        if (myFixedVariables != null) {
            for (NonzeroView<Double> entry : myFixedVariables.nonzeros()) {
                solution.set(entry.index(), entry.doubleValue());
            }
        }

        return solution;
    }

    @Override
    protected boolean initialise(final Result kickStarter) {
        return false;
    }

    @Override
    protected boolean needsAnotherIteration() {

        if (this.isLogDebug()) {
            this.log();
            this.log("Needs Another Iteration? Phase={} Artificials={} Objective={}", this.phase(), myTableau.countBasisDeficit(), this.objective());
        }

        boolean retVal = false;
        myPoint.reset();

        if (myPoint.isPhase1()) {

            double phaseOneValue = myTableau.doubleValue(this.getRowObjective(), myTableau.countConstraints() + myTableau.countVariables());

            if (!myTableau.isBasicArtificials() || PHASE1.isZero(phaseOneValue)) {

                if (this.isLogDebug()) {
                    this.log();
                    this.log("Switching to Phase2 with {} artificial variable(s) still in the basis and infeasibility {}.", myTableau.countBasicArtificials(),
                            phaseOneValue);
                    this.log();
                }

                myPoint.switchToPhase2();
                this.setState(Optimisation.State.FEASIBLE);
            }
        }

        myPoint.col = this.findNextPivotCol();

        if (myPoint.col >= 0) {

            myPoint.row = this.findNextPivotRow();

            if (myPoint.row >= 0) {

                retVal = true;

            } else {

                if (myPoint.isPhase2()) {
                    this.setState(State.UNBOUNDED);
                } else {
                    this.setState(State.INFEASIBLE);
                }

                retVal = false;
            }

        } else {

            if (myPoint.isPhase1()) {
                this.setState(State.INFEASIBLE);
            } else {
                this.setState(State.OPTIMAL);
            }

            retVal = false;
        }

        if (this.isLogDebug()) {
            if (retVal) {
                this.log("\n==>>\tRow: {},\tExit: {},\tColumn/Enter: {}.\n", myPoint.row, myTableau.getBasisColumnIndex(myPoint.row), myPoint.col);
            } else {
                this.log("\n==>>\tNo more iterations needed/possible.\n");
            }
        }

        return retVal;
    }

    protected boolean validate() {

        boolean retVal = true;
        this.setState(State.VALID);

        return retVal;
    }

    int findNextPivotCol() {

        int rowObjective = this.getRowObjective();
        int[] excluded = myTableau.getExcluded();

        boolean phase1 = myPoint.isPhase1();
        boolean phase2 = myPoint.isPhase2();

        if (this.isLogDebug()) {
            if (options.validate) {
                Access1D<Double> sliceTableauRow = myTableau.sliceTableauRow(rowObjective);
                double[] exclVals = new double[excluded.length];
                for (int i = 0; i < exclVals.length; i++) {
                    exclVals[i] = sliceTableauRow.doubleValue(excluded[i]);
                }
                this.log("\nfindNextPivotCol (index of most negative value) among these:\n{}", Arrays.toString(exclVals));
            } else {
                this.log("\nfindNextPivotCol");
            }
        }

        int retVal = -1;

        int tmpCol;
        double tmpVal;
        double minVal = phase2 ? -GenericSolver.ACCURACY.epsilon() : ZERO;

        for (int e = 0; e < excluded.length; e++) {
            tmpCol = excluded[e];
            tmpVal = myTableau.doubleValue(rowObjective, tmpCol);
            if (tmpVal < minVal && (retVal < 0 || WEIGHT.isDifferent(minVal, tmpVal))) {
                retVal = tmpCol;
                minVal = tmpVal;
                if (this.isLogDebug()) {
                    this.log("Col: {}\t=>\tReduced Contribution Weight: {}.", tmpCol, tmpVal);
                }
            }
        }

        return retVal;
    }

    int findNextPivotRow() {

        int numerCol = myTableau.countConstraints() + myTableau.countVariables();
        int denomCol = myPoint.col;

        boolean phase1 = myPoint.isPhase1();
        boolean phase2 = myPoint.isPhase2();

        if (this.isLogDebug()) {
            if (options.validate) {
                Access1D<Double> numerators = myTableau.sliceTableauColumn(numerCol);
                Access1D<Double> denominators = myTableau.sliceTableauColumn(denomCol);
                Array1D<Double> ratios = Array1D.PRIMITIVE64.copy(numerators);
                ratios.modifyMatching(DIVIDE, denominators);
                this.log("\nfindNextPivotRow (smallest positive ratio) among these:\nNumerators={}\nDenominators={}\nRatios={}", numerators, denominators,
                        ratios);
            } else {
                this.log("\nfindNextPivotRow");
            }
        }

        int retVal = -1;
        double numer = NaN, denom = NaN, ratio = NaN, minRatio = MACHINE_LARGEST, curDenom = MACHINE_SMALLEST;

        int constraintsCount = myTableau.countConstraints();
        for (int i = 0; i < constraintsCount; i++) {

            // Numerator/RHS: Should always be >=0.0, but very small numbers may "accidentally" get a negative sign.
            numer = ABS.invoke(myTableau.doubleValue(i, numerCol));

            // Denominator/Pivot
            denom = myTableau.doubleValue(i, denomCol);

            // Phase 2, artificial variable still in basis & RHS ≈ 0.0
            int basisColumnIndex = myTableau.getBasisColumnIndex(i);
            boolean artificial = basisColumnIndex < 0;
            boolean degenerate = artificial && DEGENERATE.isZero(numer);
            boolean specialCase = phase2 && degenerate;

            if (specialCase) {
                ratio = ZERO;
            } else {
                ratio = numer / denom;
            }

            if ((denom > ZERO || specialCase) && !PIVOT.isZero(denom)) {

                if (ratio >= ZERO && (ratio < minRatio || !RATIO.isDifferent(minRatio, ratio) && denom > curDenom)) {

                    retVal = i;
                    minRatio = ratio;
                    // curDenom = denom;
                    curDenom = degenerate ? Math.max(denom, ONE) : denom;

                    if (this.isLogDebug()) {
                        this.log("Row: {}\t=>\tRatio: {},\tNumerator/RHS: {}, \tDenominator/Pivot: {},\tArtificial: {}.", i, ratio, numer, denom, artificial);
                    }
                }
            }
        }

        return retVal;
    }

    void performIteration(final IterationPoint pivot) {

        double tmpPivotElement = myTableau.doubleValue(pivot.row, pivot.col);
        int tmpColRHS = myTableau.countConstraints() + myTableau.countVariables();
        double tmpPivotRHS = myTableau.doubleValue(pivot.row, tmpColRHS);

        myTableau.pivot(pivot);

        if (this.isLogDebug()) {
            this.log("Iteration Point <{},{}>\tPivot: {} => {}\tRHS: {} => {}.", pivot.row, pivot.col, tmpPivotElement,
                    myTableau.doubleValue(pivot.row, pivot.col), tmpPivotRHS, myTableau.doubleValue(pivot.row, tmpColRHS));
        }

        if (options.validate) {

            // Right-most column of the tableau
            Array1D<Double> colRHS = myTableau.sliceConstraintsRHS();

            double tmpRHS;
            double minRHS = Double.MAX_VALUE;
            for (int i = 0; i < colRHS.size(); i++) {
                tmpRHS = colRHS.doubleValue(i);
                if (tmpRHS < minRHS) {
                    minRHS = tmpRHS;
                    if (minRHS < ZERO) {
                        this.log("Negative RHS {} @ Row: {}", minRHS, i);
                    }
                }
            }

            if (minRHS < ZERO && !GenericSolver.ACCURACY.isZero(minRHS)) {
                if (this.isLogDebug()) {
                    this.log("Entire RHS columns: {}", colRHS);
                    this.log();
                }
            }

        }
    }

}

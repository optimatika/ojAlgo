/*
 * Copyright 1997-2019 Optimatika
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
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.function.aggregator.PrimitiveAggregator;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.GenericSolver;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.linear.SimplexTableau.IterationPoint;
import org.ojalgo.structure.Access1D;
import org.ojalgo.type.context.NumberContext;

/**
 * LinearSolver solves optimisation problems of the (LP standard) form:
 * <p>
 * min [C]<sup>T</sup>[X]<br>
 * when [AE][X] == [BE]<br>
 * and 0 &lt;= [X]<br>
 * and 0 &lt;= [BE]
 * </p>
 * A Linear Program is in Standard Form if:
 * <ul>
 * <li>All constraints are equality constraints.</li>
 * <li>All variables have a nonnegativity sign restriction.</li>
 * </ul>
 * <p>
 * Further it is required here that the constraint right hand sides are nonnegative (nonnegative elements in
 * [BE]).
 * </p>
 * <p>
 * The general recommendation is to construct optimisation problems using {@linkplain ExpressionsBasedModel}
 * and not worry about solver details. If you do want to instantiate a linear solver directly use the
 * {@linkplain org.ojalgo.optimisation.linear.LinearSolver.Builder} class. It will return an appropriate
 * subclass for you.
 * </p>
 *
 * @author apete
 */
public abstract class SimplexSolver extends LinearSolver {

    interface AlgorithmStore {

    }

    private static final NumberContext PHASE1 = ACCURACY.withScale(8);
    private static final NumberContext RATIO = ACCURACY.withScale(8);

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
        return myTableau.sliceDualVariables();
    }

    /**
     * Extract solution MatrixStore from the tableau
     */
    @Override
    protected Access1D<?> extractSolution() {

        int colRHS = myTableau.countConstraints() + myTableau.countVariables();

        PrimitiveDenseStore solution = PrimitiveDenseStore.FACTORY.makeZero(myTableau.countVariables(), 1);

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
            this.log("\nNeeds Another Iteration? Phase={} Artificials={} Objective={}", this.phase(), myTableau.countBasisDeficit(), this.objective());
        }

        boolean retVal = false;
        myPoint.reset();

        if (myPoint.isPhase1()) {

            double phaseOneValue = myTableau.doubleValue(this.getRowObjective(), myTableau.countConstraints() + myTableau.countVariables());

            if (!myTableau.isBasicArtificials() || PHASE1.isZero(phaseOneValue)) {

                if (this.isLogDebug()) {
                    this.log("\nSwitching to Phase2 with {} artificial variable(s) still in the basis.\n", myTableau.countBasicArtificials());
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
                    retVal = false;

                } else {

                    this.setState(State.INFEASIBLE);
                    retVal = false;
                }
            }

        } else {

            if (myPoint.isPhase1()) {

                this.setState(State.INFEASIBLE);
                retVal = false;

            } else {

                this.setState(State.OPTIMAL);
                retVal = false;
            }
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

        int[] tmpExcluded = myTableau.getExcluded();

        if (this.isLogDebug()) {
            if (options.validate) {
                this.log("\nfindNextPivotCol (index of most negative value) among these:\n{}",
                        Array1D.PRIMITIVE64.copy(myTableau.sliceTableauRow(this.getRowObjective())).copy(tmpExcluded));
            } else {
                this.log("\nfindNextPivotCol");
            }
        }

        int retVal = -1;

        double tmpVal;
        double tmpMinVal = myPoint.isPhase2() ? -GenericSolver.ACCURACY.epsilon() : ZERO;
        //double tmpMinVal = ZERO;

        int tmpCol;

        for (int e = 0; e < tmpExcluded.length; e++) {
            tmpCol = tmpExcluded[e];
            // tmpVal = myTransposedTableau.doubleValue(tmpCol, myPoint.getRowObjective());
            tmpVal = myTableau.doubleValue(this.getRowObjective(), tmpCol);
            if (tmpVal < tmpMinVal) {
                retVal = tmpCol;
                tmpMinVal = tmpVal;
                if (this.isLogDebug()) {
                    this.log("Col: {}\t=>\tReduced Contribution Weight: {}.", tmpCol, tmpVal);
                }
            }
        }

        return retVal;
    }

    int findNextPivotRow() {

        int tmpNumerCol = myTableau.countConstraints() + myTableau.countVariables();
        int tmpDenomCol = myPoint.col;

        if (this.isLogDebug()) {
            if (options.validate) {
                Access1D<Double> tmpNumerators = myTableau.sliceTableauColumn(tmpNumerCol);
                Access1D<Double> tmpDenominators = myTableau.sliceTableauColumn(tmpDenomCol);
                Array1D<Double> tmpRatios = Array1D.PRIMITIVE64.copy(tmpNumerators);
                tmpRatios.modifyMatching(DIVIDE, tmpDenominators);
                this.log("\nfindNextPivotRow (smallest positive ratio) among these:\nNumerators={}\nDenominators={}\nRatios={}", tmpNumerators, tmpDenominators,
                        tmpRatios);
            } else {
                this.log("\nfindNextPivotRow");
            }
        }

        int retVal = -1;
        double numer = NaN, denom = NaN, ratio = NaN, minRatio = MACHINE_LARGEST;

        int tmpConstraintsCount = myTableau.countConstraints();

        boolean tmpPhase2 = myPoint.isPhase2();

        for (int i = 0; i < tmpConstraintsCount; i++) {

            // Phase 2 with artificials still in the basis
            boolean specialCase = tmpPhase2 && (myTableau.getBasisColumnIndex(i) < 0);

            denom = myTableau.doubleValue(i, tmpDenomCol);

            // Should always be >=0.0, but very small numbers may "accidentally" get a negative sign.
            numer = ABS.invoke(myTableau.doubleValue(i, tmpNumerCol));

            if (RATIO.isSmall(numer, denom)) {

                ratio = MACHINE_LARGEST;

            } else {

                if (specialCase) {
                    if (RATIO.isSmall(denom, numer)) {
                        ratio = MACHINE_EPSILON;
                    } else {
                        ratio = MACHINE_LARGEST;
                    }
                } else {
                    ratio = numer / denom;
                }
            }

            if ((specialCase || (denom > ZERO)) && (ratio >= ZERO) && (ratio < minRatio)) {

                retVal = i;
                minRatio = ratio;

                if (this.isLogDebug()) {
                    this.log("Row: {}\t=>\tRatio: {},\tNumerator/RHS: {}, \tDenominator/Pivot: {}.", i, ratio, numer, denom);
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
            Array1D<Double> tmpRHS = myTableau.sliceConstraintsRHS();

            AggregatorFunction<Double> tmpMinAggr = PrimitiveAggregator.getSet().minimum();
            tmpRHS.visitAll(tmpMinAggr);
            double tmpMinVal = tmpMinAggr.doubleValue();

            if ((tmpMinVal < ZERO) && !GenericSolver.ACCURACY.isZero(tmpMinVal)) {
                this.log("\nNegative RHS! {}", tmpMinVal);
                if (this.isLogDebug()) {
                    this.log("Entire RHS columns: {}\n", tmpRHS);
                }
            }

        }
    }

}

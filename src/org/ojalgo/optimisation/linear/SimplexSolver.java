/*
 * Copyright 1997-2018 Optimatika
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

import static org.ojalgo.constant.PrimitiveMath.*;
import static org.ojalgo.function.PrimitiveFunction.*;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.ojalgo.OjAlgoUtils;
import org.ojalgo.access.Access1D;
import org.ojalgo.access.Mutate1D;
import org.ojalgo.access.Mutate2D;
import org.ojalgo.access.Structure1D.IntIndex;
import org.ojalgo.array.Array1D;
import org.ojalgo.array.SparseArray;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.function.aggregator.PrimitiveAggregator;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.matrix.store.RowsSupplier;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.optimisation.convex.ConvexSolver;
import org.ojalgo.optimisation.linear.SimplexTableau.IterationPoint;
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
 * {@linkplain Builder} class. It will return an appropriate subclass for you.
 * </p>
 *
 * @author apete
 */
public final class SimplexSolver extends LinearSolver {

    interface AlgorithmStore {

    }

    static SimplexTableau build(final ConvexSolver.Builder convex) {

        final int numbVars = convex.countVariables();
        final int numbEqus = convex.countEqualityConstraints();
        final int numbInes = convex.countInequalityConstraints();

        final SimplexTableau retVal = SimplexTableau.make(numbEqus + numbInes, numbVars + numbVars, numbInes);

        final Mutate1D obj = retVal.objective();

        final MatrixStore<Double> convexC = convex.getC();

        for (int v = 0; v < numbVars; v++) {
            final double val = convexC.doubleValue(v);
            obj.set(v, -val);
            obj.set(numbVars + v, val);
        }

        final Mutate2D constrBody = retVal.constraintsBody();
        final Mutate1D constrRHS = retVal.constraintsRHS();

        final MatrixStore<Double> convexAE = convex.getAE();
        final MatrixStore<Double> convexBE = convex.getBE();

        for (int i = 0; i < numbEqus; i++) {
            final double rhs = convexBE.doubleValue(i);
            final boolean neg = NumberContext.compare(rhs, ZERO) < 0;
            for (int j = 0; j < numbVars; j++) {
                final double val = convexAE.doubleValue(i, j);
                constrBody.set(i, j, neg ? -val : val);
                constrBody.set(i, numbVars + j, neg ? val : -val);
            }
            constrRHS.set(i, neg ? -rhs : rhs);
        }

        final RowsSupplier<Double> convexAI = convex.getAI();
        final MatrixStore<Double> convexBI = convex.getBI();

        for (int i = 0; i < numbInes; i++) {
            final int r = i;
            final SparseArray<Double> row = convexAI.getRow(r);
            final double rhs = convexBI.doubleValue(r);
            final boolean neg = NumberContext.compare(rhs, ZERO) < 0;
            row.nonzeros().forEach(nz -> constrBody.set(numbEqus + r, nz.index(), neg ? -nz.doubleValue() : nz.doubleValue()));
            row.nonzeros().forEach(nz -> constrBody.set(numbEqus + r, numbVars + nz.index(), neg ? nz.doubleValue() : -nz.doubleValue()));
            constrBody.set(numbEqus + r, numbVars + numbVars + r, neg ? NEG : ONE);
            constrRHS.set(numbEqus + i, neg ? -rhs : rhs);
        }

        return retVal;
    }

    static SimplexTableau build(final ExpressionsBasedModel model) {

        final List<Variable> tmpPosVariables = model.getPositiveVariables();
        final List<Variable> tmpNegVariables = model.getNegativeVariables();
        final Set<IntIndex> tmpFixVariables = model.getFixedVariables();

        final Expression tmpObjFunc = model.objective().compensate(tmpFixVariables);

        final List<Expression> tmpExprsEq = model.constraints().filter(c -> c.isEqualityConstraint() && !c.isAnyQuadraticFactorNonZero())
                .collect(Collectors.toList());
        final List<Expression> tmpExprsLo = model.constraints().filter(c -> c.isLowerConstraint() && !c.isAnyQuadraticFactorNonZero())
                .collect(Collectors.toList());
        final List<Expression> tmpExprsUp = model.constraints().filter(c -> c.isUpperConstraint() && !c.isAnyQuadraticFactorNonZero())
                .collect(Collectors.toList());

        final List<Variable> tmpVarsPosLo = model.bounds().filter(v -> v.isPositive() && v.isLowerConstraint() && (v.getLowerLimit().signum() > 0))
                .collect(Collectors.toList());
        final List<Variable> tmpVarsPosUp = model.bounds().filter(v -> v.isPositive() && v.isUpperConstraint() && (v.getUpperLimit().signum() > 0))
                .collect(Collectors.toList());

        final List<Variable> tmpVarsNegLo = model.bounds().filter(v -> v.isNegative() && v.isLowerConstraint() && (v.getLowerLimit().signum() < 0))
                .collect(Collectors.toList());
        final List<Variable> tmpVarsNegUp = model.bounds().filter(v -> v.isNegative() && v.isUpperConstraint() && (v.getUpperLimit().signum() < 0))
                .collect(Collectors.toList());

        final int tmpConstraiCount = tmpExprsEq.size() + tmpExprsLo.size() + tmpExprsUp.size() + tmpVarsPosLo.size() + tmpVarsPosUp.size() + tmpVarsNegLo.size()
                + tmpVarsNegUp.size();
        final int tmpProblVarCount = tmpPosVariables.size() + tmpNegVariables.size();
        final int tmpSlackVarCount = tmpExprsLo.size() + tmpExprsUp.size() + tmpVarsPosLo.size() + tmpVarsPosUp.size() + tmpVarsNegLo.size()
                + tmpVarsNegUp.size();
        final SimplexTableau retVal = SimplexTableau.make(tmpConstraiCount, tmpProblVarCount, tmpSlackVarCount);

        final int tmpPosVarsBaseIndex = 0;
        final int tmpNegVarsBaseIndex = tmpPosVarsBaseIndex + tmpPosVariables.size();
        final int tmpSlaVarsBaseIndex = tmpNegVarsBaseIndex + tmpNegVariables.size();

        for (final IntIndex tmpKey : tmpObjFunc.getLinearKeySet()) {

            final double tmpFactor = model.isMaximisation() ? -tmpObjFunc.getAdjustedLinearFactor(tmpKey) : tmpObjFunc.getAdjustedLinearFactor(tmpKey);

            final int tmpPosInd = model.indexOfPositiveVariable(tmpKey.index);
            if (tmpPosInd >= 0) {
                retVal.objective().set(tmpPosInd, tmpFactor);
            }

            final int tmpNegInd = model.indexOfNegativeVariable(tmpKey.index);
            if (tmpNegInd >= 0) {
                retVal.objective().set(tmpNegVarsBaseIndex + tmpNegInd, -tmpFactor);
            }
        }

        int tmpConstrBaseIndex = 0;
        int tmpCurrentSlackVarIndex = tmpSlaVarsBaseIndex;

        final int tmpExprsEqLength = tmpExprsEq.size();
        for (int c = 0; c < tmpExprsEqLength; c++) {

            final Expression tmpExpr = tmpExprsEq.get(c).compensate(tmpFixVariables);
            final double tmpRHS = tmpExpr.getAdjustedLowerLimit();

            if (tmpRHS < ZERO) {

                retVal.constraintsRHS().set(tmpConstrBaseIndex + c, -tmpRHS);

                for (final IntIndex tmpKey : tmpExpr.getLinearKeySet()) {

                    final double tmpFactor = tmpExpr.getAdjustedLinearFactor(tmpKey);

                    final int tmpPosInd = model.indexOfPositiveVariable(tmpKey.index);
                    if (tmpPosInd >= 0) {
                        retVal.constraintsBody().set(tmpConstrBaseIndex + c, tmpPosVarsBaseIndex + tmpPosInd, -tmpFactor);
                    }

                    final int tmpNegInd = model.indexOfNegativeVariable(tmpKey.index);
                    if (tmpNegInd >= 0) {
                        retVal.constraintsBody().set(tmpConstrBaseIndex + c, tmpNegVarsBaseIndex + tmpNegInd, tmpFactor);
                    }
                }

            } else {

                retVal.constraintsRHS().set(tmpConstrBaseIndex + c, tmpRHS);

                for (final IntIndex tmpKey : tmpExpr.getLinearKeySet()) {

                    final double tmpFactor = tmpExpr.getAdjustedLinearFactor(tmpKey);

                    final int tmpPosInd = model.indexOfPositiveVariable(tmpKey.index);
                    if (tmpPosInd >= 0) {
                        retVal.constraintsBody().set(tmpConstrBaseIndex + c, tmpPosVarsBaseIndex + tmpPosInd, tmpFactor);
                    }

                    final int tmpNegInd = model.indexOfNegativeVariable(tmpKey.index);
                    if (tmpNegInd >= 0) {
                        retVal.constraintsBody().set(tmpConstrBaseIndex + c, tmpNegVarsBaseIndex + tmpNegInd, -tmpFactor);
                    }
                }
            }
        }
        tmpConstrBaseIndex += tmpExprsEqLength;

        final int tmpExprsLoLength = tmpExprsLo.size();
        for (int c = 0; c < tmpExprsLoLength; c++) {

            final Expression tmpExpr = tmpExprsLo.get(c).compensate(tmpFixVariables);
            final double tmpRHS = tmpExpr.getAdjustedLowerLimit();

            if (tmpRHS < ZERO) {

                retVal.constraintsRHS().set(tmpConstrBaseIndex + c, -tmpRHS);

                for (final IntIndex tmpKey : tmpExpr.getLinearKeySet()) {

                    final double tmpFactor = tmpExpr.getAdjustedLinearFactor(tmpKey);

                    final int tmpPosInd = model.indexOfPositiveVariable(tmpKey.index);
                    if (tmpPosInd >= 0) {
                        retVal.constraintsBody().set(tmpConstrBaseIndex + c, tmpPosVarsBaseIndex + tmpPosInd, -tmpFactor);
                    }

                    final int tmpNegInd = model.indexOfNegativeVariable(tmpKey.index);
                    if (tmpNegInd >= 0) {
                        retVal.constraintsBody().set(tmpConstrBaseIndex + c, tmpNegVarsBaseIndex + tmpNegInd, tmpFactor);
                    }
                }

                retVal.constraintsBody().set(tmpConstrBaseIndex + c, tmpCurrentSlackVarIndex++, ONE);

            } else {

                retVal.constraintsRHS().set(tmpConstrBaseIndex + c, tmpRHS);

                for (final IntIndex tmpKey : tmpExpr.getLinearKeySet()) {

                    final double tmpFactor = tmpExpr.getAdjustedLinearFactor(tmpKey);

                    final int tmpPosInd = model.indexOfPositiveVariable(tmpKey.index);
                    if (tmpPosInd >= 0) {
                        retVal.constraintsBody().set(tmpConstrBaseIndex + c, tmpPosVarsBaseIndex + tmpPosInd, tmpFactor);
                    }

                    final int tmpNegInd = model.indexOfNegativeVariable(tmpKey.index);
                    if (tmpNegInd >= 0) {
                        retVal.constraintsBody().set(tmpConstrBaseIndex + c, tmpNegVarsBaseIndex + tmpNegInd, -tmpFactor);
                    }
                }

                retVal.constraintsBody().set(tmpConstrBaseIndex + c, tmpCurrentSlackVarIndex++, NEG);
            }
        }
        tmpConstrBaseIndex += tmpExprsLoLength;

        final int tmpExprsUpLength = tmpExprsUp.size();
        for (int c = 0; c < tmpExprsUpLength; c++) {

            final Expression tmpExpr = tmpExprsUp.get(c).compensate(tmpFixVariables);
            final double tmpRHS = tmpExpr.getAdjustedUpperLimit();

            if (tmpRHS < ZERO) {

                retVal.constraintsRHS().set(tmpConstrBaseIndex + c, -tmpRHS);

                for (final IntIndex tmpKey : tmpExpr.getLinearKeySet()) {

                    final double tmpFactor = tmpExpr.getAdjustedLinearFactor(tmpKey);

                    final int tmpPosInd = model.indexOfPositiveVariable(tmpKey.index);
                    if (tmpPosInd >= 0) {
                        retVal.constraintsBody().set(tmpConstrBaseIndex + c, tmpPosVarsBaseIndex + tmpPosInd, -tmpFactor);
                    }

                    final int tmpNegInd = model.indexOfNegativeVariable(tmpKey.index);
                    if (tmpNegInd >= 0) {
                        retVal.constraintsBody().set(tmpConstrBaseIndex + c, tmpNegVarsBaseIndex + tmpNegInd, tmpFactor);
                    }
                }

                retVal.constraintsBody().set(tmpConstrBaseIndex + c, tmpCurrentSlackVarIndex++, NEG);

            } else {

                retVal.constraintsRHS().set(tmpConstrBaseIndex + c, tmpRHS);

                for (final IntIndex tmpKey : tmpExpr.getLinearKeySet()) {

                    final double tmpFactor = tmpExpr.getAdjustedLinearFactor(tmpKey);

                    final int tmpPosInd = model.indexOfPositiveVariable(tmpKey.index);
                    if (tmpPosInd >= 0) {
                        retVal.constraintsBody().set(tmpConstrBaseIndex + c, tmpPosVarsBaseIndex + tmpPosInd, tmpFactor);
                    }

                    final int tmpNegInd = model.indexOfNegativeVariable(tmpKey.index);
                    if (tmpNegInd >= 0) {
                        retVal.constraintsBody().set(tmpConstrBaseIndex + c, tmpNegVarsBaseIndex + tmpNegInd, -tmpFactor);
                    }
                }

                retVal.constraintsBody().set(tmpConstrBaseIndex + c, tmpCurrentSlackVarIndex++, ONE);
            }
        }
        tmpConstrBaseIndex += tmpExprsUpLength;

        final int tmpVarsPosLoLength = tmpVarsPosLo.size();
        for (int c = 0; c < tmpVarsPosLoLength; c++) {
            final Variable tmpVar = tmpVarsPosLo.get(c);

            retVal.constraintsRHS().set(tmpConstrBaseIndex + c, tmpVar.getAdjustedLowerLimit());

            final int tmpKey = model.indexOf(tmpVar);

            final double tmpFactor = tmpVar.getAdjustmentFactor();

            final int tmpPosInd = model.indexOfPositiveVariable(tmpKey);
            if (tmpPosInd >= 0) {
                retVal.constraintsBody().set(tmpConstrBaseIndex + c, tmpPosVarsBaseIndex + tmpPosInd, tmpFactor);
            }

            final int tmpNegInd = model.indexOfNegativeVariable(tmpKey);
            if (tmpNegInd >= 0) {
                retVal.constraintsBody().set(tmpConstrBaseIndex + c, tmpNegVarsBaseIndex + tmpNegInd, -tmpFactor);
            }

            retVal.constraintsBody().set(tmpConstrBaseIndex + c, tmpCurrentSlackVarIndex++, NEG);
        }
        tmpConstrBaseIndex += tmpVarsPosLoLength;

        final int tmpVarsPosUpLength = tmpVarsPosUp.size();
        for (int c = 0; c < tmpVarsPosUpLength; c++) {
            final Variable tmpVar = tmpVarsPosUp.get(c);

            retVal.constraintsRHS().set(tmpConstrBaseIndex + c, tmpVar.getAdjustedUpperLimit());

            final int tmpKey = model.indexOf(tmpVar);

            final double tmpFactor = tmpVar.getAdjustmentFactor();

            final int tmpPosInd = model.indexOfPositiveVariable(tmpKey);
            if (tmpPosInd >= 0) {
                retVal.constraintsBody().set(tmpConstrBaseIndex + c, tmpPosVarsBaseIndex + tmpPosInd, tmpFactor);
            }

            final int tmpNegInd = model.indexOfNegativeVariable(tmpKey);
            if (tmpNegInd >= 0) {
                retVal.constraintsBody().set(tmpConstrBaseIndex + c, tmpNegVarsBaseIndex + tmpNegInd, -tmpFactor);
            }

            retVal.constraintsBody().set(tmpConstrBaseIndex + c, tmpCurrentSlackVarIndex++, ONE);
        }
        tmpConstrBaseIndex += tmpVarsPosUpLength;

        final int tmpVarsNegLoLength = tmpVarsNegLo.size();
        for (int c = 0; c < tmpVarsNegLoLength; c++) {
            final Variable tmpVar = tmpVarsNegLo.get(c);

            retVal.constraintsRHS().set(tmpConstrBaseIndex + c, -tmpVar.getAdjustedLowerLimit());

            final int tmpKey = model.indexOf(tmpVar);

            final double tmpFactor = tmpVar.getAdjustmentFactor();

            final int tmpPosInd = model.indexOfPositiveVariable(tmpKey);
            if (tmpPosInd >= 0) {
                retVal.constraintsBody().set(tmpConstrBaseIndex + c, tmpPosVarsBaseIndex + tmpPosInd, -tmpFactor);
            }

            final int tmpNegInd = model.indexOfNegativeVariable(tmpKey);
            if (tmpNegInd >= 0) {
                retVal.constraintsBody().set(tmpConstrBaseIndex + c, tmpNegVarsBaseIndex + tmpNegInd, tmpFactor);
            }

            retVal.constraintsBody().set(tmpConstrBaseIndex + c, tmpCurrentSlackVarIndex++, ONE);
        }
        tmpConstrBaseIndex += tmpVarsNegLoLength;

        final int tmpVarsNegUpLength = tmpVarsNegUp.size();
        for (int c = 0; c < tmpVarsNegUpLength; c++) {
            final Variable tmpVar = tmpVarsNegUp.get(c);

            retVal.constraintsRHS().set(tmpConstrBaseIndex + c, -tmpVar.getAdjustedUpperLimit());

            final int tmpKey = model.indexOf(tmpVar);

            final double tmpFactor = tmpVar.getAdjustmentFactor();

            final int tmpPosInd = model.indexOfPositiveVariable(tmpKey);
            if (tmpPosInd >= 0) {
                retVal.constraintsBody().set(tmpConstrBaseIndex + c, tmpPosVarsBaseIndex + tmpPosInd, -tmpFactor);
            }

            final int tmpNegInd = model.indexOfNegativeVariable(tmpKey);
            if (tmpNegInd >= 0) {
                retVal.constraintsBody().set(tmpConstrBaseIndex + c, tmpNegVarsBaseIndex + tmpNegInd, tmpFactor);
            }

            retVal.constraintsBody().set(tmpConstrBaseIndex + c, tmpCurrentSlackVarIndex++, NEG);
        }
        tmpConstrBaseIndex += tmpVarsNegUpLength;

        //        BasicLogger.DEBUG.printmtrx("Sparse", retVal);
        //        BasicLogger.DEBUG.printmtrx("Dense", retVal.toDense());

        if (retVal.getOvercapacity() <= OjAlgoUtils.ENVIRONMENT.getCacheElements(8L)) {
            return retVal.toDense();
        } else {
            return retVal;
        }
    }

    private final IterationPoint myPoint;

    private final SimplexTableau myTableau;

    SimplexSolver(final SimplexTableau tableau, final Optimisation.Options solverOptions) {

        super(solverOptions);

        myTableau = tableau;

        myPoint = new IterationPoint();

        if (this.isDebug() && this.isTableauPrintable()) {
            this.logDebugTableau("Tableau Created");
        }
    }

    public Result solve(final Result kickStarter) {

        if (this.isDebug() && this.isTableauPrintable()) {
            this.logDebugTableau("Initial Tableau");
        }

        while (this.needsAnotherIteration()) {

            this.performIteration(myPoint);

            if (this.isDebug() && this.isTableauPrintable()) {
                this.logDebugTableau("Tableau Iteration");
            }
        }

        return this.buildResult();
    }

    private int getRowObjective() {
        return myPoint.isPhase1() ? myTableau.countConstraints() + 1 : myTableau.countConstraints();
    }

    private final boolean isTableauPrintable() {
        return myTableau.count() <= 512L;
    }

    private final void logDebugTableau(final String message) {
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
        return super.buildResult().multipliers(myTableau.sliceDualVariables());
    }

    @Override
    protected double evaluateFunction(final Access1D<?> solution) {
        return this.objective();
    }

    /**
     * Extract solution MatrixStore from the tableau
     */
    @Override
    protected PhysicalStore<Double> extractSolution() {

        final int colRHS = myTableau.countConstraints() + myTableau.countVariables();

        final PrimitiveDenseStore solution = PrimitiveDenseStore.FACTORY.makeZero(myTableau.countVariables(), 1);

        final int numberOfConstraints = myTableau.countConstraints();
        for (int row = 0; row < numberOfConstraints; row++) {
            final int variableIndex = myTableau.getBasis(row);
            if (variableIndex >= 0) {
                solution.set(variableIndex, myTableau.doubleValue(row, colRHS));
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

        if (this.isDebug()) {
            this.log("\nNeeds Another Iteration? Phase={} Artificials={} Objective={}", this.phase(), myTableau.countBasisDeficit(), this.objective());
        }

        boolean retVal = false;
        myPoint.reset();

        if (myPoint.isPhase1()) {

            // final double tmpPhaseOneValue = myTransposedTableau.doubleValue(this.countConstraints() + this.countVariables(), myPoint.getRowObjective());
            final double tmpPhaseOneValue = myTableau.doubleValue(this.getRowObjective(), myTableau.countConstraints() + myTableau.countVariables());

            if (!myTableau.isBasicArtificials() || options.feasibility.isZero(tmpPhaseOneValue)) {

                if (this.isDebug()) {
                    this.log("\nSwitching to Phase2 with {} artificial variable(s) still in the basis.\n", myTableau.countBasicArtificials());
                    // this.debug("Reduced artificial costs:\n{}", this.sliceTableauRow(myPoint.getRowObjective()).copy(this.getExcluded()));
                }

                myPoint.switchToPhase2();
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

        if (this.isDebug()) {
            if (retVal) {
                this.log("\n==>>\tRow: {},\tExit: {},\tColumn/Enter: {}.\n", myPoint.row, myTableau.getBasis(myPoint.row), myPoint.col);
            } else {
                this.log("\n==>>\tNo more iterations needed/possible.\n");
            }
        }

        return retVal;
    }

    protected boolean validate() {

        final boolean retVal = true;
        this.setState(State.VALID);

        return retVal;
    }

    int findNextPivotCol() {

        final int[] tmpExcluded = myTableau.getExcluded();

        if (this.isDebug()) {
            if (options.validate) {
                this.log("\nfindNextPivotCol (index of most negative value) among these:\n{}",
                        Array1D.PRIMITIVE64.copy(myTableau.sliceTableauRow(this.getRowObjective())).copy(tmpExcluded));
            } else {
                this.log("\nfindNextPivotCol");
            }
        }

        int retVal = -1;

        double tmpVal;
        double tmpMinVal = myPoint.isPhase2() ? -options.feasibility.epsilon() : ZERO;
        //double tmpMinVal = ZERO;

        int tmpCol;

        for (int e = 0; e < tmpExcluded.length; e++) {
            tmpCol = tmpExcluded[e];
            // tmpVal = myTransposedTableau.doubleValue(tmpCol, myPoint.getRowObjective());
            tmpVal = myTableau.doubleValue(this.getRowObjective(), tmpCol);
            if (tmpVal < tmpMinVal) {
                retVal = tmpCol;
                tmpMinVal = tmpVal;
                if (this.isDebug()) {
                    this.log("Col: {}\t=>\tReduced Contribution Weight: {}.", tmpCol, tmpVal);
                }
            }
        }

        return retVal;
    }

    int findNextPivotRow() {

        final int tmpNumerCol = myTableau.countConstraints() + myTableau.countVariables();
        final int tmpDenomCol = myPoint.col;

        if (this.isDebug()) {
            if (options.validate) {
                final Access1D<Double> tmpNumerators = myTableau.sliceTableauColumn(tmpNumerCol);
                final Access1D<Double> tmpDenominators = myTableau.sliceTableauColumn(tmpDenomCol);
                final Array1D<Double> tmpRatios = Array1D.PRIMITIVE64.copy(tmpNumerators);
                tmpRatios.modifyMatching(DIVIDE, tmpDenominators);
                this.log("\nfindNextPivotRow (smallest positive ratio) among these:\nNumerators={}\nDenominators={}\nRatios={}", tmpNumerators, tmpDenominators,
                        tmpRatios);
            } else {
                this.log("\nfindNextPivotRow");
            }
        }

        int retVal = -1;
        double tmpNumer = NaN, tmpDenom = NaN, tmpRatio = NaN;
        double tmpMinRatio = MACHINE_LARGEST;

        final int tmpConstraintsCount = myTableau.countConstraints();

        final boolean tmpPhase2 = myPoint.isPhase2();

        for (int i = 0; i < tmpConstraintsCount; i++) {

            // Phase 2 with artificials still in the basis
            final boolean tmpSpecialCase = tmpPhase2 && (myTableau.getBasis(i) < 0);

            // tmpDenom = myTransposedTableau.doubleValue(tmpDenomCol, i);
            tmpDenom = myTableau.doubleValue(i, tmpDenomCol);

            // Should always be >=0.0, but very small numbers may "accidentally" get a negative sign.
            // tmpNumer = PrimitiveFunction.ABS.invoke(myTransposedTableau.doubleValue(tmpNumerCol, i));
            tmpNumer = PrimitiveFunction.ABS.invoke(myTableau.doubleValue(i, tmpNumerCol));

            if (options.feasibility.isSmall(tmpNumer, tmpDenom)) {

                tmpRatio = MACHINE_LARGEST;

            } else {

                if (tmpSpecialCase) {
                    if (options.feasibility.isSmall(tmpDenom, tmpNumer)) {
                        tmpRatio = MACHINE_EPSILON;
                    } else {
                        tmpRatio = MACHINE_LARGEST;
                    }
                } else {
                    tmpRatio = tmpNumer / tmpDenom;
                }
            }

            if ((tmpSpecialCase || (tmpDenom > ZERO)) && (tmpRatio >= ZERO) && (tmpRatio < tmpMinRatio)) {

                retVal = i;
                tmpMinRatio = tmpRatio;

                if (this.isDebug()) {
                    this.log("Row: {}\t=>\tRatio: {},\tNumerator/RHS: {}, \tDenominator/Pivot: {}.", i, tmpRatio, tmpNumer, tmpDenom);
                }
            }
        }

        return retVal;
    }

    void performIteration(final IterationPoint pivot) {

        final double tmpPivotElement = myTableau.doubleValue(pivot.row, pivot.col);
        final int tmpColRHS = myTableau.countConstraints() + myTableau.countVariables();
        final double tmpPivotRHS = myTableau.doubleValue(pivot.row, tmpColRHS);

        myTableau.pivot(pivot);

        if (this.isDebug()) {
            this.log("Iteration Point <{},{}>\tPivot: {} => {}\tRHS: {} => {}.", pivot.row, pivot.col, tmpPivotElement,
                    myTableau.doubleValue(pivot.row, pivot.col), tmpPivotRHS, myTableau.doubleValue(pivot.row, tmpColRHS));
        }

        if (options.validate) {

            // Right-most column of the tableau
            final Array1D<Double> tmpRHS = myTableau.sliceConstraintsRHS();

            final AggregatorFunction<Double> tmpMinAggr = PrimitiveAggregator.getSet().minimum();
            tmpRHS.visitAll(tmpMinAggr);
            final double tmpMinVal = tmpMinAggr.doubleValue();

            if ((tmpMinVal < ZERO) && !options.feasibility.isZero(tmpMinVal)) {
                this.log("\nNegative RHS! {}", tmpMinVal);
                if (this.isDebug()) {
                    this.log("Entire RHS columns: {}\n", tmpRHS);
                }
            }

        }
    }


}

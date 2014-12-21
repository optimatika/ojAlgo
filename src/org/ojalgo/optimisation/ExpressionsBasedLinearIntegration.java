/*
 * Copyright 1997-2014 Optimatika (www.optimatika.se)
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
package org.ojalgo.optimisation;

import static org.ojalgo.constant.PrimitiveMath.*;

import java.util.List;
import java.util.Set;

import org.ojalgo.access.AccessUtils;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.matrix.store.ZeroStore;
import org.ojalgo.optimisation.Expression.Index;
import org.ojalgo.optimisation.ExpressionsBasedModel.Integration;
import org.ojalgo.optimisation.linear.LinearSolver;

final class ExpressionsBasedLinearIntegration extends Integration<LinearSolver> {

    public LinearSolver build(final ExpressionsBasedModel model) {

        final LinearSolver.Builder tmpBuilder = LinearSolver.getBuilder();

        LinearSolver.copy(model, tmpBuilder);

        return tmpBuilder.build(model.options);
    }

    public Result toModelState(final ExpressionsBasedModel model, final Result solverState) {

        final List<Variable> tmpFreeVariables = model.getFreeVariables();
        final Set<Index> tmpFixedVariables = model.getFixedVariables();

        final PrimitiveDenseStore tmpModelSolution = PrimitiveDenseStore.FACTORY.makeZero(tmpFixedVariables.size() + tmpFreeVariables.size(), 1);

        final int tmpModelSolutionSize = (int) tmpModelSolution.count();
        final int tmpVariablesSize = model.getVariables().size();
        if (tmpModelSolutionSize != tmpVariablesSize) {
            throw new IllegalStateException();
        }

        for (final Index tmpFixed : tmpFixedVariables) {
            tmpModelSolution.set(tmpFixed.index, 0, model.getVariable(tmpFixed.index).getValue().doubleValue());
        }

        final List<Variable> tmpPositive = model.getPositiveVariables();
        for (int p = 0; p < tmpPositive.size(); p++) {
            final int tmpIndex = model.indexOf(tmpPositive.get(p));
            tmpModelSolution.set(tmpIndex, 0, solverState.doubleValue(p));
        }

        final List<Variable> tmpNegative = model.getNegativeVariables();
        for (int n = 0; n < tmpNegative.size(); n++) {
            final int tmpIndex = model.indexOf(tmpNegative.get(n));
            tmpModelSolution.set(tmpIndex, 0, tmpModelSolution.doubleValue(tmpIndex) - solverState.doubleValue(tmpPositive.size() + n));
        }

        return new Result(solverState.getState(), solverState.getValue(), tmpModelSolution);
    }

    public Result extractSolverState(final ExpressionsBasedModel model) {

        final List<Variable> tmpPosVariables = model.getPositiveVariables();
        final List<Variable> tmpNegVariables = model.getNegativeVariables();
        final Set<Index> tmpFixVariables = model.getFixedVariables();

        final List<Expression> tmpExprsEq = model.selectExpressionsLinearEquality();
        final List<Expression> tmpExprsLo = model.selectExpressionsLinearLower();
        final List<Expression> tmpExprsUp = model.selectExpressionsLinearUpper();

        final List<Variable> tmpVarsPosLo = model.selectVariablesPositiveLower();
        final List<Variable> tmpVarsPosUp = model.selectVariablesPositiveUpper();

        final List<Variable> tmpVarsNegLo = model.selectVariablesNegativeLower();
        final List<Variable> tmpVarsNegUp = model.selectVariablesNegativeUpper();

        final int tmpConstraiCount = tmpExprsEq.size() + tmpExprsLo.size() + tmpExprsUp.size() + tmpVarsPosLo.size() + tmpVarsPosUp.size()
                + tmpVarsNegLo.size() + tmpVarsNegUp.size();
        final int tmpProblVarCount = tmpPosVariables.size() + tmpNegVariables.size();
        final int tmpSlackVarCount = tmpExprsLo.size() + tmpExprsUp.size() + tmpVarsPosLo.size() + tmpVarsPosUp.size() + tmpVarsNegLo.size()
                + tmpVarsNegUp.size();
        final int tmpTotalVarCount = tmpProblVarCount + tmpSlackVarCount;

        final int[] tmpBasis = AccessUtils.makeIncreasingRange(-tmpConstraiCount, tmpConstraiCount);

        final Optimisation.Result tmpKickStarter = new Optimisation.Result(Optimisation.State.UNEXPLORED, Double.NaN, ZeroStore.makePrimitive(tmpTotalVarCount,
                1));

        final int tmpPosVarsBaseIndex = 0;
        final int tmpNegVarsBaseIndex = tmpPosVarsBaseIndex + tmpPosVariables.size();
        final int tmpSlaVarsBaseIndex = tmpNegVarsBaseIndex + tmpNegVariables.size();

        int tmpConstrBaseIndex = 0;
        final int tmpCurrentSlackVarIndex = tmpSlaVarsBaseIndex;

        final int tmpExprsEqLength = tmpExprsEq.size();

        tmpConstrBaseIndex += tmpExprsEqLength;

        final int tmpExprsLoLength = tmpExprsLo.size();
        for (int c = 0; c < tmpExprsLoLength; c++) {

            final Expression tmpExpr = tmpExprsLo.get(c);
            final double tmpRHS = tmpExpr.getCompensatedLowerLimit(tmpFixVariables);

            if (tmpRHS < ZERO) {

                tmpBasis[tmpConstrBaseIndex + c] = tmpCurrentSlackVarIndex;

            } else {

            }
        }
        tmpConstrBaseIndex += tmpExprsLoLength;

        final int tmpExprsUpLength = tmpExprsUp.size();
        for (int c = 0; c < tmpExprsUpLength; c++) {

            final Expression tmpExpr = tmpExprsUp.get(c);
            final double tmpRHS = tmpExpr.getCompensatedUpperLimit(tmpFixVariables);

            if (tmpRHS < ZERO) {

            } else {

                tmpBasis[tmpConstrBaseIndex + c] = tmpCurrentSlackVarIndex;

            }
        }
        tmpConstrBaseIndex += tmpExprsUpLength;

        final int tmpVarsPosLoLength = tmpVarsPosLo.size();

        tmpConstrBaseIndex += tmpVarsPosLoLength;

        final int tmpVarsPosUpLength = tmpVarsPosUp.size();
        for (int c = 0; c < tmpVarsPosUpLength; c++) {

            tmpBasis[tmpConstrBaseIndex + c] = tmpCurrentSlackVarIndex;

        }
        tmpConstrBaseIndex += tmpVarsPosUpLength;

        final int tmpVarsNegLoLength = tmpVarsNegLo.size();
        for (int c = 0; c < tmpVarsNegLoLength; c++) {

            tmpBasis[tmpConstrBaseIndex + c] = tmpCurrentSlackVarIndex;

        }
        tmpConstrBaseIndex += tmpVarsNegLoLength;

        final int tmpVarsNegUpLength = tmpVarsNegUp.size();

        tmpConstrBaseIndex += tmpVarsNegUpLength;

        return tmpKickStarter;
    }

}

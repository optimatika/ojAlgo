/*
 * Copyright 1997-2015 Optimatika (www.optimatika.se)
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

import java.util.List;

import org.ojalgo.access.IntIndex;
import org.ojalgo.array.PrimitiveArray;
import org.ojalgo.optimisation.linear.LinearSolver;

final class ExpressionsBasedLinearIntegration extends ExpressionsBasedModel.Integration<LinearSolver> {

    public LinearSolver build(final ExpressionsBasedModel model) {

        final LinearSolver.Builder tmpBuilder = LinearSolver.getBuilder();

        LinearSolver.copy(model, tmpBuilder);

        return tmpBuilder.build(model.options);
    }

    public boolean isCapable(final ExpressionsBasedModel model) {
        return !(model.isAnyVariableInteger() || model.isAnyExpressionQuadratic());
    }

    @Override
    public Result toModelState(final Result solverState, final ExpressionsBasedModel model) {

        final PrimitiveArray tmpModelSolution = PrimitiveArray.make(model.countVariables());

        for (final IntIndex tmpFixed : model.getFixedVariables()) {
            tmpModelSolution.set(tmpFixed.index, model.getVariable(tmpFixed.index).getValue().doubleValue());
        }

        final List<Variable> tmpPositives = model.getPositiveVariables();
        for (int p = 0; p < tmpPositives.size(); p++) {
            final Variable tmpVariable = tmpPositives.get(p);
            final int tmpIndex = model.indexOf(tmpVariable);
            tmpModelSolution.set(tmpIndex, solverState.doubleValue(p));
        }

        final List<Variable> tmpNegatives = model.getNegativeVariables();
        for (int n = 0; n < tmpNegatives.size(); n++) {
            final Variable tmpVariable = tmpNegatives.get(n);
            final int tmpIndex = model.indexOf(tmpVariable);
            tmpModelSolution.set(tmpIndex, tmpModelSolution.doubleValue(tmpIndex) - solverState.doubleValue(tmpPositives.size() + n));
        }

        return new Result(solverState.getState(), solverState.getValue(), tmpModelSolution);
    }

    @Override
    public Result toSolverState(final Result modelState, final ExpressionsBasedModel model) {

        final List<Variable> tmpPositives = model.getPositiveVariables();
        final List<Variable> tmpNegatives = model.getNegativeVariables();

        final int tmpCountPositives = tmpPositives.size();
        final int tmpCountNegatives = tmpNegatives.size();

        final PrimitiveArray tmpSolverSolution = PrimitiveArray.make(tmpCountPositives + tmpCountNegatives);

        for (int p = 0; p < tmpCountPositives; p++) {
            final Variable tmpVariable = tmpPositives.get(p);
            final int tmpIndex = model.indexOf(tmpVariable);
            tmpSolverSolution.set(p, Math.max(modelState.doubleValue(tmpIndex), 0.0));
        }

        for (int n = 0; n < tmpCountNegatives; n++) {
            final Variable tmpVariable = tmpNegatives.get(n);
            final int tmpIndex = model.indexOf(tmpVariable);
            tmpSolverSolution.set(tmpCountPositives + n, Math.max(-modelState.doubleValue(tmpIndex), 0.0));
        }

        return new Result(modelState.getState(), modelState.getValue(), tmpSolverSolution);
    }

}

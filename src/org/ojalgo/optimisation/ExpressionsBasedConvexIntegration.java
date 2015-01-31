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

import java.util.List;

import org.ojalgo.array.PrimitiveArray;
import org.ojalgo.optimisation.Expression.Index;
import org.ojalgo.optimisation.ExpressionsBasedModel.Integration;
import org.ojalgo.optimisation.convex.ConvexSolver;

final class ExpressionsBasedConvexIntegration extends Integration<ConvexSolver> {

    public ConvexSolver build(final ExpressionsBasedModel model) {

        final ConvexSolver.Builder tmpBuilder = ConvexSolver.getBuilder();

        ConvexSolver.copy(model, tmpBuilder);

        return tmpBuilder.build(model.options);
    }

    public Result toModelState(final Result solverState, final ExpressionsBasedModel model) {

        final PrimitiveArray tmpModelSolution = PrimitiveArray.make(model.countVariables());

        for (final Index tmpFixed : model.getFixedVariables()) {
            tmpModelSolution.set(tmpFixed.index, model.getVariable(tmpFixed.index).getValue().doubleValue());
        }

        final List<Variable> tmpFreeVariables = model.getFreeVariables();
        for (int f = 0; f < tmpFreeVariables.size(); f++) {
            final Variable tmpVariable = tmpFreeVariables.get(f);
            final int tmpIndex = model.indexOf(tmpVariable);
            tmpModelSolution.set(tmpIndex, solverState.doubleValue(f));
        }

        return new Result(solverState.getState(), solverState.getValue(), tmpModelSolution);
    }

    public Result toSolverState(final Result modelState, final ExpressionsBasedModel model) {

        final List<Variable> tmpFreeVariables = model.getFreeVariables();

        final PrimitiveArray tmpSolverSolution = PrimitiveArray.make(tmpFreeVariables.size());
        final double[] tmpData = tmpSolverSolution.data;

        for (int i = 0; i < tmpData.length; i++) {
            final Variable tmpVariable = tmpFreeVariables.get(i);
            final int tmpIndex = model.indexOf(tmpVariable);
            tmpData[i] = modelState.doubleValue(tmpIndex);
        }

        return new Result(modelState.getState(), modelState.getValue(), tmpSolverSolution);
    }

}

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

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import org.ojalgo.array.Array1D;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.optimisation.Expression.Index;
import org.ojalgo.optimisation.ExpressionsBasedModel.Integration;
import org.ojalgo.optimisation.convex.ConvexSolver;

final class ExpressionsBasedConvexIntegration extends Integration<ConvexSolver> {

    public ConvexSolver build(final ExpressionsBasedModel model) {

        final ConvexSolver.Builder tmpBuilder = ConvexSolver.getBuilder();

        ConvexSolver.copy(model, tmpBuilder);

        return tmpBuilder.build(model.options);
    }

    public Result toModelState(final ExpressionsBasedModel model, final Result solverState) {

        final List<Variable> tmpFreeVariables = model.getFreeVariables();
        final Set<Index> tmpFixedVariables = model.getFixedVariables();

        final PrimitiveDenseStore retVal = PrimitiveDenseStore.FACTORY.makeZero(tmpFixedVariables.size() + tmpFreeVariables.size(), 1);

        for (final Index tmpVariable : tmpFixedVariables) {
            retVal.set(tmpVariable.index, 0, model.getVariable(tmpVariable.index).getValue().doubleValue());
        }

        for (int i = 0; i < tmpFreeVariables.size(); i++) {
            final Variable tmpVariable = tmpFreeVariables.get(i);
            final int tmpIndexOf = model.indexOf(tmpVariable);
            retVal.set(tmpIndexOf, 0, solverState.doubleValue(i));

        }

        return new Result(solverState.getState(), solverState.getValue(), retVal);
    }

    public Result extractSolverState(final ExpressionsBasedModel model) {

        final List<Variable> tmpFreeVariables = model.getFreeVariables();
        final int tmpSize = tmpFreeVariables.size();

        final Array1D<Double> tmpModelSolution = Array1D.PRIMITIVE.makeZero(tmpSize);
        for (int i = 0; i < tmpSize; i++) {
            final BigDecimal tmpValue = tmpFreeVariables.get(i).getValue();
            if (tmpValue != null) {
                tmpModelSolution.set(i, tmpValue.doubleValue());
            }
        }

        return new Optimisation.Result(Optimisation.State.UNEXPLORED, Double.NaN, tmpModelSolution);
    }

}

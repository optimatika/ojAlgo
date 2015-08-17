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

final class MathProgSysGenericIntegration extends MathProgSysModel.Integration<GenericSolver> {

    public GenericSolver build(final MathProgSysModel model) {
        final ExpressionsBasedModel tmpDelegate = model.getExpressionsBasedModel();
        return (GenericSolver) tmpDelegate.getIntegration().build(tmpDelegate);
    }

    public Result extractSolverState(final MathProgSysModel model) {
        final ExpressionsBasedModel tmpDelegate = model.getExpressionsBasedModel();
        return tmpDelegate.getIntegration().extractSolverState(tmpDelegate);
    }

    public boolean isCapable(final MathProgSysModel model) {
        return !model.getExpressionsBasedModel().isAnyExpressionQuadratic();
    }

    public Result toModelState(final Result solverState, final MathProgSysModel model) {
        final ExpressionsBasedModel tmpDelegate = model.getExpressionsBasedModel();
        return tmpDelegate.getIntegration().toModelState(solverState, tmpDelegate);
    }

    public Result toSolverState(final Result modelState, final MathProgSysModel model) {
        final ExpressionsBasedModel tmpDelegate = model.getExpressionsBasedModel();
        return tmpDelegate.getIntegration().toSolverState(modelState, tmpDelegate);
    }

}

/*
 * Copyright 1997-2022 Optimatika
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
package org.ojalgo.optimisation.integer;

import org.ojalgo.function.constant.BigMath;
import org.ojalgo.function.multiary.MultiaryFunction;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.GenericSolver;
import org.ojalgo.optimisation.integer.IntegerStrategy.GMICutConfiguration;
import org.ojalgo.structure.Access1D;

/**
 * An alternative MIP solver using Gomory mixed integer cuts – purely iterative with no branching. This solver
 * is only used for some cut generation tests. The solver to use for you MIP models is {@link IntegerSolver}.
 *
 * @author apete
 */
final class GomorySolver extends GenericSolver {

    static final class ModelIntegration extends ExpressionsBasedModel.Integration<GomorySolver> {

        public GomorySolver build(final ExpressionsBasedModel model) {
            return new GomorySolver(model);
        }

        public boolean isCapable(final ExpressionsBasedModel model) {
            return !model.isAnyConstraintQuadratic();
        }

        @Override
        public Result toModelState(final Result solverState, final ExpressionsBasedModel model) {
            return solverState;
        }

        @Override
        public Result toSolverState(final Result modelState, final ExpressionsBasedModel model) {
            return modelState;
        }

        @Override
        protected boolean isSolutionMapped() {
            return false;
        }

    }

    private static final GMICutConfiguration GMI_CUT_CONFIGURATION = new GMICutConfiguration().withFractionality(0.01).withViolation(BigMath.HUNDRED);

    private final MultiaryFunction.TwiceDifferentiable<Double> myFunction;
    private final ExpressionsBasedModel myIntegerModel;

    GomorySolver(final ExpressionsBasedModel model) {

        super(model.options);

        myIntegerModel = model.simplify();
        myFunction = myIntegerModel.limitObjective(null, null).toFunction();
    }

    public Result solve(final Result kickStarter) {

        ModelStrategy strategy = IntegerStrategy.DEFAULT.withGMICutConfiguration(GMI_CUT_CONFIGURATION).newModelStrategy(myIntegerModel);

        ExpressionsBasedModel iteratorModel = myIntegerModel.snapshot();
        iteratorModel.relax(true);
        NodeSolver iterativeSolver = iteratorModel.prepare(NodeSolver::new);

        Result retVal = iterativeSolver.solve();

        while (retVal.getState().isFeasible() && !myIntegerModel.validate(retVal)) {
            iterativeSolver.generateCuts(strategy);
            retVal = iterativeSolver.solve();
            if (this.isLogDebug()) {
                this.log("Iteration: {}", retVal);
            }
        }

        return retVal;
    }

    @Override
    protected double evaluateFunction(final Access1D<?> solution) {
        if (myFunction != null && solution != null && myFunction.arity() == solution.count()) {
            return myFunction.invoke(Access1D.asPrimitive1D(solution)).doubleValue();
        }
        return Double.NaN;
    }

    @Override
    protected Access1D<?> extractSolution() {
        return myIntegerModel.getVariableValues();
    }

}

/*
 * Copyright 1997-2024 Optimatika
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
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.integer.IntegerStrategy.GMICutConfiguration;
import org.ojalgo.structure.Access1D;

/**
 * An alternative MIP solver using Gomory Mixed Integer (GMI) cuts – purely iterative with no branching. This
 * solver is only used for some cut generation tests. The solver to use for your MIP models is
 * {@link IntegerSolver}.
 *
 * @author apete
 */
public final class GomorySolver extends GenericSolver {

    public static final class ModelIntegration extends ExpressionsBasedModel.Integration<GomorySolver> {

        @Override
        public GomorySolver build(final ExpressionsBasedModel model) {

            GomorySolver solver = new GomorySolver(model);

            if (model.options.validate) {
                solver.setValidator(this.newValidator(model));
            }

            return solver;
        }

        @Override
        public boolean isCapable(final ExpressionsBasedModel model) {
            return model.isAnyVariableInteger() && !model.isAnyConstraintQuadratic();
        }

    }

    public static final ExpressionsBasedModel.Integration<GomorySolver> INTEGRATION = new GomorySolver.ModelIntegration();

    private static final GMICutConfiguration GMI_CUT_CONFIGURATION = new GMICutConfiguration().withFractionality(0.01).withViolation(BigMath.HUNDRED);

    private final MultiaryFunction.TwiceDifferentiable<Double> myFunction;
    private final ExpressionsBasedModel myIntegerModel;

    GomorySolver(final ExpressionsBasedModel model) {

        super(model.options);

        myIntegerModel = model.simplify();
        myFunction = myIntegerModel.limitObjective(null, null).toFunction();
    }

    @Override
    public Result solve(final Result kickStarter) {

        ModelStrategy strategy = IntegerStrategy.DEFAULT.withGMICutConfiguration(GMI_CUT_CONFIGURATION).newModelStrategy(myIntegerModel);

        ExpressionsBasedModel iteratorModel = myIntegerModel.snapshot();
        NodeSolver iterativeSolver = iteratorModel.prepare(NodeSolver::new);

        Result retVal = iterativeSolver.solve();
        this.incrementIterationsCount();
        if (this.isLogProgress()) {
            this.log();
            this.log("Iteration {}: {}", this.countIterations(), retVal);
        }
        while (retVal.getState().isFeasible() && !myIntegerModel.validate(retVal)) {
            iterativeSolver.generateCuts(strategy);
            retVal = iterativeSolver.solve();
            this.incrementIterationsCount();
            if (this.isLogProgress()) {
                this.log();
                this.log("Iteration {}: {}", this.countIterations(), retVal);
            }
        }

        return retVal.withValue(this.evaluateFunction(retVal));
    }

    protected Optimisation.Result buildResult() {

        Access1D<?> solution = this.extractSolution();
        double value = this.evaluateFunction(solution);
        Optimisation.State state = this.getState();

        return new Optimisation.Result(state, value, solution);
    }

    protected double evaluateFunction(final Access1D<?> solution) {
        if (myFunction != null && solution != null && myFunction.arity() == solution.count()) {
            return myFunction.invoke(Access1D.asPrimitive1D(solution)).doubleValue();
        }
        return Double.NaN;
    }

    protected Access1D<?> extractSolution() {
        return myIntegerModel.getVariableValues();
    }

}

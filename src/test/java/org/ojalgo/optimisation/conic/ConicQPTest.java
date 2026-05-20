package org.ojalgo.optimisation.conic;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.type.context.NumberContext;

/**
 * Forward-compatible test for an unconstrained positive definite quadratic objective. Focus: solution
 * correctness (stationary point), objective value, independence from iteration strategy.
 */
public class ConicQPTest {

    private static final NumberContext TOL = NumberContext.of(10, 8);

    @Test
    public void solvesSimpleUnconstrainedQP() {
        // minimise 0.5*(4 x^2 + y^2) + (2 x + y); analytic optimum x* = (-0.5, -1).
        ExpressionsBasedModel model = new ExpressionsBasedModel();
        Variable x = model.addVariable("x").weight(2.0); // linear term 2x
        Variable y = model.addVariable("y").weight(1.0); // linear term 1y
        // Add quadratic terms via expression contributing to objective.
        model.addExpression("quad").set(x, x, 4.0).set(y, y, 1.0).weight(1.0);

        Optimisation.Result result = ConicSolver.INTEGRATION.build(model).solve();

        TestUtils.assertTrue(result.getState().isFeasible() || result.getState().isOptimal() || result.getState().isApproximate());
        double xOpt = result.doubleValue(model.indexOf(x));
        double yOpt = result.doubleValue(model.indexOf(y));
        TestUtils.assertEquals(TOL.enforce(-0.5), TOL.enforce(xOpt));
        TestUtils.assertEquals(TOL.enforce(-1.0), TOL.enforce(yOpt));
        // Objective value at optimum equals -1.
        TestUtils.assertEquals(TOL.enforce(-1.0), TOL.enforce(result.getValue()));
    }
}
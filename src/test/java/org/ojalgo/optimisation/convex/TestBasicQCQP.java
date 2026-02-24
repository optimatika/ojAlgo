package org.ojalgo.optimisation.convex;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.ExpressionsBasedModel.Integration;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.State;
import org.ojalgo.optimisation.OptimisationCase;
import org.ojalgo.optimisation.TestBasic;
import org.ojalgo.optimisation.Variable;

/**
 * A set of basic Quadratically Constrained Quadratic Programme (QCQP) models. These tests are disabled until
 * QCQP support is properly implemented in solvers.
 */
public class TestBasicQCQP extends OptimisationConvexTests implements TestBasic {

    /**
     * Ball constraint in R²: minimize x₀ subject to x₀² + x₁² <= 1. The optimal solution is at x₀ = -1, x₁ =
     * 0 with objective value -1.
     */
    static OptimisationCase caseBallConstraint() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x0 = model.addVariable("x0").weight(1.0);
        Variable x1 = model.addVariable("x1");

        Expression ball = model.addExpression("ball");
        ball.set(x0, x0, 1.0);
        ball.set(x1, x1, 1.0);
        ball.upper(1.0);

        Optimisation.Result result = Optimisation.Result.of(-1.0, State.OPTIMAL, -1.0, 0.0);

        return OptimisationCase.of(model, Optimisation.Sense.MIN, result);
    }

    /**
     * Markowitz max-return with variance cap: maximize return subject to variance <= cap. This is a linear
     * objective with a quadratic constraint (QCQP).
     */
    static OptimisationCase caseMarkowitzVarianceCap() {

        double[] mu = { 0.05, 0.10, 0.15 };
        double[] var = { 0.01, 0.02, 0.03 };
        double varianceCap = 0.02;

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable[] w = new Variable[mu.length];
        for (int i = 0; i < mu.length; i++) {
            w[i] = model.addVariable("w" + i).lower(0.0).upper(1.0).weight(mu[i]);
        }

        Expression budget = model.addExpression("budget");
        for (Variable v : w) {
            budget.set(v, 1.0);
        }
        budget.level(1.0);

        Expression variance = model.addExpression("variance");
        for (int i = 0; i < var.length; i++) {
            variance.set(w[i], w[i], var[i]);
        }
        variance.upper(varianceCap);

        // OPTIMAL 0.1399999999993075 @ { 2.086E-11, 0.19999999997225, 0.80000000000693 }
        Optimisation.Result result = Optimisation.Result.of(0.14, State.OPTIMAL, 0.0, 0.2, 0.8);

        return OptimisationCase.of(model, Optimisation.Sense.MAX, result);
    }

    @Test
    public void testBallConstraint() {
        OptimisationCase testCase = TestBasicQCQP.caseBallConstraint();
        for (Integration<?> integration : this.integrations()) {
            testCase.assertResult(integration);
        }
    }

    @Test
    public void testMarkowitzVarianceCap() {
        OptimisationCase testCase = TestBasicQCQP.caseMarkowitzVarianceCap();
        for (Integration<?> integration : this.integrations()) {
            testCase.assertResult(integration);
        }
    }

    protected List<ExpressionsBasedModel.Integration<?>> integrations() {
        return List.of();
    }

}

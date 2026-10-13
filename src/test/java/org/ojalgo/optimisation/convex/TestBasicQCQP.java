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
import org.ojalgo.type.context.NumberContext;

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
     * A coupled quadratic form, where the Hessian is not diagonal: minimize -x - y subject to x² + xy + y² <=
     * 3. By symmetry x = y, so 3x² <= 3 gives x = y = 1 and objective value -2.
     * <p>
     * At the optimum the objective is flat along the constraint boundary, so the solution is only accurate to
     * about the square root of the solver's tolerance.
     */
    static OptimisationCase caseCoupledQuadraticForm() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x = model.addVariable("x").lower(0.0).upper(10.0).weight(-1.0);
        Variable y = model.addVariable("y").lower(0.0).upper(10.0).weight(-1.0);

        Expression coupled = model.addExpression("coupled");
        coupled.set(x, x, 1.0);
        coupled.set(x, y, 1.0);
        coupled.set(y, y, 1.0);
        coupled.upper(3.0);

        Optimisation.Result result = Optimisation.Result.of(-2.0, State.OPTIMAL, 1.0, 1.0);

        return OptimisationCase.of(model, Optimisation.Sense.MIN, result).accuracy(NumberContext.of(4));
    }

    /**
     * A lower-limited quadratic constraint with a linear term, which is convex only after negation: minimize
     * x subject to -x² + 4x >= 3, 0 <= x <= 10. The feasible region is [1,3], so the optimum is x = 1.
     */
    static OptimisationCase caseLinearTermInNegatedQuadraticConstraint() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x = model.addVariable("x").lower(0.0).upper(10.0).weight(1.0);

        Expression negated = model.addExpression("negated");
        negated.set(x, x, -1.0);
        negated.set(x, 4.0);
        negated.lower(3.0);

        Optimisation.Result result = Optimisation.Result.of(1.0, State.OPTIMAL, 1.0);

        return OptimisationCase.of(model, Optimisation.Sense.MIN, result);
    }

    /**
     * A quadratic constraint with a linear term in it: minimize -x subject to x² + 2x <= 3, 0 <= x <= 10. The
     * constraint binds at x = 1. Ignoring the linear term would give x = sqrt(3).
     */
    static OptimisationCase caseLinearTermInQuadraticConstraint() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x = model.addVariable("x").lower(0.0).upper(10.0).weight(-1.0);

        Expression quadratic = model.addExpression("quadratic");
        quadratic.set(x, x, 1.0);
        quadratic.set(x, 2.0);
        quadratic.upper(3.0);

        Optimisation.Result result = Optimisation.Result.of(-1.0, State.OPTIMAL, 1.0);

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

    /**
     * A rank-deficient but still convex quadratic form - not positive definite, only semidefinite: minimize
     * -x - 2y subject to (x + y)² <= 4, 0 <= x, y <= 10. The optimum is x = 0, y = 2 with objective value -4.
     */
    static OptimisationCase casePositiveSemidefiniteQuadraticForm() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x = model.addVariable("x").lower(0.0).upper(10.0).weight(-1.0);
        Variable y = model.addVariable("y").lower(0.0).upper(10.0).weight(-2.0);

        Expression semidefinite = model.addExpression("semidefinite");
        semidefinite.set(x, x, 1.0);
        semidefinite.set(x, y, 2.0);
        semidefinite.set(y, y, 1.0);
        semidefinite.upper(4.0);

        Optimisation.Result result = Optimisation.Result.of(-4.0, State.OPTIMAL, 0.0, 2.0);

        return OptimisationCase.of(model, Optimisation.Sense.MIN, result);
    }

    /**
     * Both the objective and the constraint quadratic: minimize ½(x₀² + 0.8x₀x₁ + x₁²) subject to (x₀ + 2)² +
     * (x₁ + 2)² <= 1.75². The constraint binds, and by symmetry x₀ = x₁ = -2 + 1.75/√2. (The QCQP example
     * from JOptimizer's documentation.)
     * <p>
     * At the optimum the objective is flat along the constraint boundary, so the solution is only accurate to
     * about the square root of the solver's tolerance.
     */
    static OptimisationCase caseQuadraticObjectiveAndConstraint() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x0 = model.addVariable("x0");
        Variable x1 = model.addVariable("x1");

        Expression objective = model.addExpression("objective").weight(0.5);
        objective.set(x0, x0, 1.0);
        objective.set(x0, x1, 0.8);
        objective.set(x1, x1, 1.0);

        // (x₀ + 2)² + (x₁ + 2)² <= 1.75² expanded
        Expression circle = model.addExpression("circle");
        circle.set(x0, x0, 1.0);
        circle.set(x1, x1, 1.0);
        circle.set(x0, 4.0);
        circle.set(x1, 4.0);
        circle.upper(1.75 * 1.75 - 8.0);

        double optimum = -2.0 + 1.75 / Math.sqrt(2.0);

        Optimisation.Result result = Optimisation.Result.of(1.4 * optimum * optimum, State.OPTIMAL, optimum, optimum);

        return OptimisationCase.of(model, Optimisation.Sense.MIN, result).accuracy(NumberContext.of(4));
    }

    @Test
    public void testBallConstraint() {
        OptimisationCase testCase = TestBasicQCQP.caseBallConstraint();
        for (Integration<?> integration : this.integrations()) {
            testCase.assertResult(integration);
        }
    }

    @Test
    public void testCoupledQuadraticForm() {
        OptimisationCase testCase = TestBasicQCQP.caseCoupledQuadraticForm();
        for (Integration<?> integration : this.integrations()) {
            testCase.assertResult(integration);
        }
    }

    @Test
    public void testLinearTermInNegatedQuadraticConstraint() {
        OptimisationCase testCase = TestBasicQCQP.caseLinearTermInNegatedQuadraticConstraint();
        for (Integration<?> integration : this.integrations()) {
            testCase.assertResult(integration);
        }
    }

    @Test
    public void testLinearTermInQuadraticConstraint() {
        OptimisationCase testCase = TestBasicQCQP.caseLinearTermInQuadraticConstraint();
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

    @Test
    public void testPositiveSemidefiniteQuadraticForm() {
        OptimisationCase testCase = TestBasicQCQP.casePositiveSemidefiniteQuadraticForm();
        for (Integration<?> integration : this.integrations()) {
            testCase.assertResult(integration);
        }
    }

    @Test
    public void testQuadraticObjectiveAndConstraint() {
        OptimisationCase testCase = TestBasicQCQP.caseQuadraticObjectiveAndConstraint();
        for (Integration<?> integration : this.integrations()) {
            testCase.assertResult(integration);
        }
    }

    protected List<ExpressionsBasedModel.Integration<?>> integrations() {
        return List.of();
    }

}

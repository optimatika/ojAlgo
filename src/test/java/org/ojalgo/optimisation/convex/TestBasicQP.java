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
 * A set of basic QP test models to test things any/all QP solver should be able to handle. Just small models
 * with no extreme numerical difficulties. Must be constrained QP models (no LP, no unconstrained).
 */
public class TestBasicQP extends OptimisationConvexTests implements TestBasic {

    /**
     * Asymmetric QP: minimize x² + 2y² + x + 2y subject to x + y >= 1, x >= 0, y >= 0. The optimal solution
     * is at x = 5/6, y = 1/6 with objective value 23/12.
     */
    static OptimisationCase caseAsymmetricQP() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x = model.addVariable("x").lower(0.0);
        Variable y = model.addVariable("y").lower(0.0);

        Expression objective = model.addExpression("obj");
        objective.set(x, x, 1.0);
        objective.set(y, y, 2.0);
        objective.set(x, 1.0);
        objective.set(y, 2.0);
        objective.weight(1.0);

        Expression constraint = model.addExpression("c1");
        constraint.set(x, 1.0);
        constraint.set(y, 1.0);
        constraint.lower(1.0);

        Optimisation.Result result = Optimisation.Result.of(23.0 / 12.0, State.OPTIMAL, 5.0 / 6.0, 1.0 / 6.0);

        return OptimisationCase.of(model, Optimisation.Sense.MIN, result).accuracy(NumberContext.of(6));
    }

    /**
     * min 0.5*x'Px + q'x where P = [[4,1],[1,2]] and q = [1,1]. Constraint: x0 + x1 = 1 with bounds 0 <= x0
     * <= 0.7 and 0 <= x1 <= 0.7.
     */
    static OptimisationCase caseBasicQPTestData() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x0 = model.addVariable("x0").lower(0.0).upper(0.7);
        Variable x1 = model.addVariable("x1").lower(0.0).upper(0.7);

        Expression quadratic = model.addExpression("quadratic");
        quadratic.set(x0, x0, 4.0);
        quadratic.set(x0, x1, 2.0);
        quadratic.set(x1, x1, 2.0);
        quadratic.weight(0.5);

        Expression linear = model.addExpression("linear");
        linear.set(x0, 1.0);
        linear.set(x1, 1.0);
        linear.weight(1.0);

        Expression eq = model.addExpression("eq");
        eq.set(x0, 1.0);
        eq.set(x1, 1.0);
        eq.level(1.0);

        Optimisation.Result result = Optimisation.Result.of(1.88, State.OPTIMAL, 0.3, 0.7);

        return OptimisationCase.of(model, Optimisation.Sense.MIN, result);
    }

    static OptimisationCase caseBoundedQPAsLP() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x = model.addVariable("X").lower(0.0).upper(10.0);
        Variable y = model.addVariable("Y").lower(0.0).upper(10.0);

        Expression objective = model.addExpression("obj");
        objective.set(x, -1.0);
        objective.set(y, -2.0);
        objective.set(x, x, 1.0e-4);
        objective.set(y, y, 1.0e-4);
        objective.weight(1.0);

        Expression constraint = model.addExpression("c1");
        constraint.set(x, 1.0);
        constraint.set(y, 1.0);
        constraint.upper(8.0);

        Optimisation.Result result = Optimisation.Result.of(-15.9936, State.OPTIMAL, 0.0, 8.0);

        return OptimisationCase.of(model, Optimisation.Sense.MIN, result);
    }

    /**
     * Markowitz minimum variance portfolio: minimize variance subject to budget constraint and return floor.
     * This is a standard QP with quadratic objective and linear constraints.
     * <p>
     * 3 assets with returns [0.05, 0.10, 0.15] and diagonal covariance [0.01, 0.02, 0.03]. Target return >=
     * 0.09.
     */
    static OptimisationCase caseMarkowitzMinVariance() {

        double[] mu = { 0.05, 0.10, 0.15 };
        double[] var = { 0.01, 0.02, 0.03 };
        double targetReturn = 0.09;

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable w0 = model.addVariable("w0").lower(0.0).upper(1.0);
        Variable w1 = model.addVariable("w1").lower(0.0).upper(1.0);
        Variable w2 = model.addVariable("w2").lower(0.0).upper(1.0);

        // Budget constraint: w0 + w1 + w2 = 1
        model.addExpression("budget").set(w0, 1.0).set(w1, 1.0).set(w2, 1.0).level(1.0);

        // Return constraint: mu'w >= targetReturn
        model.addExpression("return").set(w0, mu[0]).set(w1, mu[1]).set(w2, mu[2]).lower(targetReturn);

        // Objective: minimize variance = w'*diag(var)*w
        Expression variance = model.addExpression("variance");
        variance.set(w0, w0, var[0]);
        variance.set(w1, w1, var[1]);
        variance.set(w2, w2, var[2]);
        variance.weight(1.0);

        // Optimal variance = 0.0057, solution w = [0.45, 0.30, 0.25]
        // Verified: 0.01*0.2025 + 0.02*0.09 + 0.03*0.0625 = 0.0057
        Optimisation.Result result = Optimisation.Result.of(0.0057, State.OPTIMAL, 0.45, 0.30, 0.25);

        return OptimisationCase.of(model, Optimisation.Sense.MIN, result).accuracy(NumberContext.of(2));
    }

    /**
     * Minimal constrained QP: minimize 0.5*(x² + y²) subject to x + y >= 1, with x >= 0, y >= 0. The optimal
     * solution is at x = y = 0.5 with objective value 0.25.
     */
    static OptimisationCase caseMinimalQP() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x = model.addVariable("x").lower(0.0);
        Variable y = model.addVariable("y").lower(0.0);

        Expression objective = model.addExpression("obj");
        objective.set(x, x, 1.0);
        objective.set(y, y, 1.0);
        objective.weight(0.5);

        Expression constraint = model.addExpression("c1");
        constraint.set(x, 1.0);
        constraint.set(y, 1.0);
        constraint.lower(1.0);

        Optimisation.Result result = Optimisation.Result.of(0.25, State.OPTIMAL, 0.5, 0.5);

        return OptimisationCase.of(model, Optimisation.Sense.MIN, result).accuracy(NumberContext.of(6));
    }

    static OptimisationCase caseQPWithOnlyBounds() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x = model.addVariable("X").lower(-5.0).upper(5.0);
        Variable y = model.addVariable("Y").lower(-5.0).upper(5.0);

        Expression objective = model.addExpression("obj");
        objective.set(x, x, 1.0);
        objective.set(y, y, 1.0);
        objective.set(x, -1.0);
        objective.set(y, 3.0);
        objective.weight(1.0);

        Optimisation.Result result = Optimisation.Result.of(-2.5, State.OPTIMAL, 0.5, -1.5);

        return OptimisationCase.of(model, Optimisation.Sense.MIN, result).accuracy(NumberContext.of(6));
    }

    static OptimisationCase caseQPWithTwoSidedConstraints() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x = model.addVariable("X").lower(0.0).upper(10.0);
        Variable y = model.addVariable("Y").lower(0.0).upper(10.0);

        Expression objective = model.addExpression("obj");
        objective.set(x, x, 0.5);
        objective.set(y, y, 1.0);
        objective.set(x, -1.0);
        objective.set(y, -3.0);
        objective.weight(1.0);

        Expression c1 = model.addExpression("c1");
        c1.set(x, 1.0);
        c1.set(y, 1.0);
        c1.lower(2.0);
        c1.upper(6.0);

        Optimisation.Result result = Optimisation.Result.of(-2.75, State.OPTIMAL, 1.0, 1.5);

        return OptimisationCase.of(model, Optimisation.Sense.MIN, result).accuracy(NumberContext.of(5));
    }

    static OptimisationCase caseQuadraticWithAllConstraintTypes() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x = model.addVariable("X").lower(-10.0).upper(10.0);
        Variable y = model.addVariable("Y").lower(-10.0).upper(10.0);

        Expression objective = model.addExpression("obj");
        objective.set(x, x, 2.0);
        objective.set(y, y, 1.0);
        objective.set(x, 1.0);
        objective.set(y, -1.0);
        objective.weight(1.0);

        Expression eq = model.addExpression("eq");
        eq.set(x, 1.0);
        eq.set(y, 1.0);
        eq.level(1.0);

        Expression le = model.addExpression("le");
        le.set(x, 1.0);
        le.set(y, -1.0);
        le.upper(2.0);

        Expression ge = model.addExpression("ge");
        ge.set(x, -1.0);
        ge.upper(1.0);

        Expression rng = model.addExpression("rng");
        rng.set(y, 1.0);
        rng.lower(0.5);
        rng.upper(3.0);

        Optimisation.Result result = Optimisation.Result.of(0.0, State.OPTIMAL, 0.0, 1.0);

        return OptimisationCase.of(model, Optimisation.Sense.MIN, result).accuracy(NumberContext.of(5));
    }

    static OptimisationCase caseSimpleLinearConstraint() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x = model.addVariable("X").lower(0.0).upper(10.0);
        Variable y = model.addVariable("Y").lower(0.0).upper(10.0);

        Expression objective = model.addExpression("obj");
        objective.set(x, 1.0);
        objective.set(y, 2.0);
        objective.set(x, x, 1.0);
        objective.set(y, y, 1.0);
        objective.weight(1.0);

        Expression c1 = model.addExpression("c1");
        c1.set(x, 1.0);
        c1.set(y, 1.0);
        c1.upper(3.0);

        Optimisation.Result result = Optimisation.Result.of(0.0, State.OPTIMAL, 0.0, 0.0);

        return OptimisationCase.of(model, Optimisation.Sense.MIN, result).accuracy(NumberContext.of(5));
    }

    static OptimisationCase caseSimpleQP() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x = model.addVariable("X").lower(0.0).upper(10.0);
        Variable y = model.addVariable("Y").lower(0.0).upper(10.0);

        Expression objective = model.addExpression("obj");
        objective.set(x, x, 1.0);
        objective.set(y, y, 1.0);
        objective.set(x, -2.0);
        objective.set(y, -4.0);
        objective.weight(1.0);

        Expression constraint = model.addExpression("c1");
        constraint.set(x, 1.0);
        constraint.set(y, 1.0);
        constraint.upper(5.0);

        Optimisation.Result result = Optimisation.Result.of(-5.0, State.OPTIMAL, 1.0, 2.0);

        return OptimisationCase.of(model, Optimisation.Sense.MIN, result).accuracy(NumberContext.of(3));
    }

    static OptimisationCase caseUnconstrainedQuadraticWithBounds() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x = model.addVariable("X").lower(0.0).upper(10.0);
        Variable y = model.addVariable("Y").lower(0.0).upper(10.0);

        Expression objective = model.addExpression("obj");
        objective.set(x, 1.0);
        objective.set(y, 2.0);
        objective.set(x, x, 1.0);
        objective.set(y, y, 1.0);
        objective.weight(1.0);

        Optimisation.Result result = Optimisation.Result.of(0.0, State.OPTIMAL, 0.0, 0.0);

        return OptimisationCase.of(model, Optimisation.Sense.MIN, result).accuracy(NumberContext.of(5));
    }

    @Test
    public void testAsymmetricQP() {
        OptimisationCase testCase = TestBasicQP.caseAsymmetricQP();
        for (Integration<?> integration : this.integrations()) {
            testCase.assertResult(integration);
        }
    }

    @Test
    public void testBasicQPTestData() {
        OptimisationCase testCase = TestBasicQP.caseBasicQPTestData();
        for (Integration<?> integration : this.integrations()) {
            testCase.assertResult(integration);
        }
    }

    @Test
    public void testBoundedQPAsLP() {
        OptimisationCase testCase = TestBasicQP.caseBoundedQPAsLP();
        for (Integration<?> integration : this.integrations()) {
            testCase.assertResult(integration);
        }
    }

    @Test
    public void testMarkowitzMinVariance() {
        OptimisationCase testCase = TestBasicQP.caseMarkowitzMinVariance();
        for (Integration<?> integration : this.integrations()) {
            testCase.assertResult(integration);
        }
    }

    @Test
    public void testMinimalQP() {
        OptimisationCase testCase = TestBasicQP.caseMinimalQP();
        for (Integration<?> integration : this.integrations()) {
            testCase.assertResult(integration);
        }
    }

    @Test
    public void testQPWithOnlyBounds() {
        OptimisationCase testCase = TestBasicQP.caseQPWithOnlyBounds();
        for (Integration<?> integration : this.integrations()) {
            testCase.assertResult(integration);
        }
    }

    @Test
    public void testQPWithTwoSidedConstraints() {
        OptimisationCase testCase = TestBasicQP.caseQPWithTwoSidedConstraints();
        for (Integration<?> integration : this.integrations()) {
            testCase.assertResult(integration);
        }
    }

    @Test
    public void testQuadraticWithAllConstraintTypes() {
        OptimisationCase testCase = TestBasicQP.caseQuadraticWithAllConstraintTypes();
        for (Integration<?> integration : this.integrations()) {
            testCase.assertResult(integration);
        }
    }

    @Test
    public void testSimpleLinearConstraint() {
        OptimisationCase testCase = TestBasicQP.caseSimpleLinearConstraint();
        for (Integration<?> integration : this.integrations()) {
            testCase.assertResult(integration);
        }
    }

    @Test
    public void testSimpleQP() {
        OptimisationCase testCase = TestBasicQP.caseSimpleQP();
        for (Integration<?> integration : this.integrations()) {
            testCase.assertResult(integration);
        }
    }

    @Test
    public void testUnconstrainedQuadraticWithBounds() {
        OptimisationCase testCase = TestBasicQP.caseUnconstrainedQuadraticWithBounds();
        for (Integration<?> integration : this.integrations()) {
            testCase.assertResult(integration);
        }
    }

    protected List<ExpressionsBasedModel.Integration<?>> integrations() {
        return List.of(VARIANTS);
    }

}

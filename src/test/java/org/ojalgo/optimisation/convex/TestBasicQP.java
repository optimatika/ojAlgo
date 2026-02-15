package org.ojalgo.optimisation.convex;

import org.junit.jupiter.api.Test;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.ExpressionsBasedModel.Integration;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.State;
import org.ojalgo.optimisation.OptimisationCase;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.type.context.NumberContext;

/**
 * A set of basic QP test models to test things any/all QP solver should be able to handle. Just small models
 * with no extreme numerical difficulties. Must be constrained QP models (no LP, no unconstrained).
 */
class TestBasicQP extends OptimisationConvexTests {

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

    /**
     * Minimise x² + y² - x - 3y with integer variables and bounds [0, 5]. The continuous optimum is at (0.5,
     * 1.5), and the integer optimum is at (1, 1) with objective value -2.
     */
    static OptimisationCase caseSimpleIntegerQP() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x = model.addVariable("X").lower(0).upper(5).integer(true);
        Variable y = model.addVariable("Y").lower(0).upper(5).integer(true);

        Expression objective = model.addExpression("obj");
        objective.set(x, x, 1.0);
        objective.set(y, y, 1.0);
        objective.set(x, -1.0);
        objective.set(y, -3.0);
        objective.weight(1.0);

        Optimisation.Result result = Optimisation.Result.of(-2.0, State.OPTIMAL, 1, 1);

        return OptimisationCase.of(model, Optimisation.Sense.MIN, result).accuracy(NumberContext.of(6));
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
    void testBasicQPTestData() {
        OptimisationCase testCase = TestBasicQP.caseBasicQPTestData();
        for (Integration<?> integration : VARIANTS) {
            testCase.assertResult(integration);
        }
    }

    @Test
    void testBoundedQPAsLP() {
        OptimisationCase testCase = TestBasicQP.caseBoundedQPAsLP();
        for (Integration<?> integration : VARIANTS) {
            testCase.assertResult(integration);
        }
    }

    @Test
    void testQPWithOnlyBounds() {
        OptimisationCase testCase = TestBasicQP.caseQPWithOnlyBounds();
        for (Integration<?> integration : VARIANTS) {
            testCase.assertResult(integration);
        }
    }

    @Test
    void testQPWithTwoSidedConstraints() {
        OptimisationCase testCase = TestBasicQP.caseQPWithTwoSidedConstraints();
        for (Integration<?> integration : VARIANTS) {
            testCase.assertResult(integration);
        }
    }

    @Test
    void testQuadraticWithAllConstraintTypes() {
        OptimisationCase testCase = TestBasicQP.caseQuadraticWithAllConstraintTypes();
        for (Integration<?> integration : VARIANTS) {
            testCase.assertResult(integration);
        }
    }

    @Test
    void testSimpleIntegerQP() {
        OptimisationCase testCase = TestBasicQP.caseSimpleIntegerQP();
        testCase.assertResult();
        testCase.model.options.convex().algorithm(ConvexSolver.Algorithm.ACTIVE_SET);
        testCase.assertResult();
        testCase.model.options.convex().algorithm(ConvexSolver.Algorithm.ADMM);
        testCase.assertResult();
    }

    @Test
    void testSimpleLinearConstraint() {
        OptimisationCase testCase = TestBasicQP.caseSimpleLinearConstraint();
        for (Integration<?> integration : VARIANTS) {
            testCase.assertResult(integration);
        }
    }

    @Test
    void testSimpleQP() {
        OptimisationCase testCase = TestBasicQP.caseSimpleQP();
        for (Integration<?> integration : VARIANTS) {
            testCase.assertResult(integration);
        }
    }

    @Test
    void testUnconstrainedQuadraticWithBounds() {
        OptimisationCase testCase = TestBasicQP.caseUnconstrainedQuadraticWithBounds();
        for (Integration<?> integration : VARIANTS) {
            testCase.assertResult(integration);
        }
    }

}

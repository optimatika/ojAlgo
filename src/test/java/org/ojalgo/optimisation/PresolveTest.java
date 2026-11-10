package org.ojalgo.optimisation;

import java.math.BigDecimal;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.optimisation.Optimisation.State;

public class PresolveTest extends OptimisationTests {

    private static ExpressionsBasedModel makeModel690(final Optimisation.Environment environment) {
        ExpressionsBasedModel model = environment.newModel();
        Variable x = model.newVariable("x").lower(0).upper(1).weight(1);
        Variable y = model.newVariable("y").lower(0).upper(1);
        model.newExpression("SUM").set(x, 1).set(y, 1).level(1);
        model.newExpression("XXX").set(x, new BigDecimal("1e-16")).set(y, 1).upper(0);
        return model;
    }

    /**
     * max 2e20 x + z subject to 1e20 x + z <= 2, 0 <= x <= upper and 0 <= z <= 1
     */
    private static ExpressionsBasedModel makeModel709(final double upper) {
        ExpressionsBasedModel model = new ExpressionsBasedModel();
        Variable x = model.newVariable("x").lower(0).upper(upper).weight(2e20);
        Variable z = model.newVariable("z").lower(0).upper(1).weight(1);
        model.newExpression("ROW").set(x, 1e20).set(z, 1).upper(2);
        return model;
    }

    /**
     * https://github.com/optimatika/ojAlgo/issues/663
     * <p>
     * Was a problem with the pre-solve logic. A purely quadratic expression was passed to a pre-solver that
     * only works for linear expressions. This caused an ArithmeticException and a complete failure.
     * <p>
     * Further this model is infeasible and should be recognised as such by the pre-solver alone, without
     * invoking any solver. That the model solves (is reported infeasible) is tested in
     * {@code ConvexUserFiles}.
     */
    @Test
    void testGitHubIssue663() {

        ExpressionsBasedModel model = ModelFileTest.makeModel("usersupplied", "GitHub663.ebm", false);

        model.simplify();

        TestUtils.assertTrue(model.isInfeasible());
    }

    /**
     * https://github.com/optimatika/ojAlgo/issues/690
     * <p>
     * https://github.com/optimatika/ojAlgo/discussions/691
     */
    @Test
    void testGitHubIssue690a() {

        Optimisation.Environment environment = Optimisation.newEnvironment();

        State withPresolve = PresolveTest.makeModel690(environment).minimise().getState();

        environment.clearPresolvers();

        State withoutPresolve = PresolveTest.makeModel690(environment).minimise().getState();

        TestUtils.assertTrue(withPresolve.isOptimal());
        TestUtils.assertTrue(withoutPresolve.isOptimal());
    }

    /**
     * https://github.com/optimatika/ojAlgo/issues/690
     * <p>
     * https://github.com/optimatika/ojAlgo/discussions/691
     * <p>
     * The pre-solver used to (falsely) declare this model infeasible. That the model solves is tested in
     * {@code LinearUserFiles}.
     */
    @Test
    void testGitHubIssue690b() {

        ExpressionsBasedModel model = ModelFileTest.makeModel("usersupplied", "GitHub690.ebm", false);

        model.simplify();

        TestUtils.assertFalse(model.isInfeasible());
    }

    /**
     * https://github.com/optimatika/ojAlgo/issues/709
     * <p>
     * Presolve derives x <= 2e-20 from the constraint, and fixes x at 1e-20 since that range is within the
     * feasibility tolerance. With the coefficients 1e20 and 2e20 the range is worth 2 in the constraint and 4
     * in the objective – the optimum is 4 at (2e-20, 0), but 3 at (1e-20, 1) is reported. That is accepted
     * (the model is badly scaled), but presolve should print a warning. It does so regardless of whether the
     * small range is given (upper = 3e-20) or derived (upper = 1). A small range that is negligible also when
     * multiplied by the coefficients does not give a warning.
     */
    @Test
    void testGitHubIssue709() {

        for (double upper : new double[] { 3e-20, 1 }) {

            ExpressionsBasedModel model = PresolveTest.makeModel709(upper);

            TestUtils.assertTrue(model.isScalingWarningPending());

            model.simplify();

            TestUtils.assertTrue(model.getVariable(0).isFixed());
            TestUtils.assertFalse(model.isScalingWarningPending());
        }

        ExpressionsBasedModel model = new ExpressionsBasedModel();
        Variable x = model.newVariable("x").lower(0).upper(1).weight(1);
        Variable z = model.newVariable("z").lower(0).upper(1).weight(1);
        model.newExpression("ROW").set(x, 1).set(z, 1).upper(2e-12);

        model.simplify();

        TestUtils.assertTrue(x.isFixed());
        TestUtils.assertTrue(model.isScalingWarningPending());
    }

    /**
     * Presolve writes its conclusions (infeasibility and redundancy flags, tightened variable limits) into
     * the model, and nothing undoes them when the model is changed and solved again. A known, long-standing,
     * problem. Here the first solve is infeasible, and the second, after the limits have been relaxed, still
     * reports infeasible. This is what {@code RevisedSimplexSolverTest.testShiftingRange} used to run into
     * with some random data. Tagged unstable (it fails) until that is fixed.
     */
    @Test
    @Tag("unstable")
    void testResolveAfterInfeasible() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x = model.newVariable("x").lower(2).upper(3).weight(1);
        Variable y = model.newVariable("y").lower(0).weight(1);

        model.newExpression("LIMIT").set(x, 1).set(y, 1).upper(1);

        TestUtils.assertStateInfeasible(model.minimise());

        x.lower(0).upper(1);

        Result result = model.minimise();

        TestUtils.assertStateNotLessThanOptimal(result);
        TestUtils.assertEquals(0.0, result.getValue());
    }

}

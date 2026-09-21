package org.ojalgo.optimisation;

import java.math.BigDecimal;

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
     * https://github.com/optimatika/ojAlgo/issues/663
     * <p>
     * Was a problem with the pre-solve logic. A purely quadratic expression was passed to a pre-solver that
     * only works for linear expressions. This caused an ArithmeticException and a complete failure.
     * <p>
     * Further this model is infeasible and should be recognised as such (by the pre-solver).
     */
    @Test
    void testGitHubIssue663() {

        ExpressionsBasedModel model = ModelFileTest.makeModel("usersupplied", "GitHub663.ebm", false);

        Result result = model.minimise();

        TestUtils.assertStateInfeasible(result);
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
     */
    @Test
    void testGitHubIssue690b() {

        ExpressionsBasedModel model = ModelFileTest.makeModel("usersupplied", "GitHub690.ebm", false);

        Result result = model.minimise();

        TestUtils.assertStateNotLessThanFeasible(result);
    }

}

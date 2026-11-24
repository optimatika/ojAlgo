package org.ojalgo.optimisation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

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
     * Fixing one variable leads to the next one being fixed, and so on – in whatever order the constraints
     * are checked. The value of each variable is one more than that of the previous one, and the total
     * depends on all of them. If there's a contradiction anywhere, the model is infeasible.
     */
    @Test
    void testChain() {

        int nbVariables = 50;
        int first = 3;

        for (long seed = 1L; seed <= 10L; seed++) {

            // The names decide in which order the constraints are checked
            List<Integer> names = new ArrayList<>();
            for (int i = 0; i < nbVariables; i++) {
                names.add(i);
            }
            Collections.shuffle(names, new Random(seed));

            for (boolean feasible : new boolean[] { true, false }) {

                ExpressionsBasedModel model = new ExpressionsBasedModel();

                Variable total = model.newVariable("total").lower(0).weight(1);
                Expression sum = model.newExpression("SUM").level(0).set(total, -1);

                Variable[] chain = new Variable[nbVariables];
                for (int i = 0; i < nbVariables; i++) {
                    chain[i] = model.newVariable("x" + i).lower(0).upper(1000);
                    sum.set(chain[i], 1);
                    if (i == 0) {
                        model.newExpression("C" + names.get(i)).set(chain[i], 2).level(2 * first);
                    } else {
                        model.newExpression("C" + names.get(i)).set(chain[i], 1).set(chain[i - 1], -1).level(1);
                    }
                }
                if (!feasible) {
                    model.newExpression("LAST").set(chain[nbVariables - 1], 1).level(first + nbVariables);
                }

                model.simplify();

                TestUtils.assertEquals(!feasible, model.isInfeasible());

                if (feasible) {
                    int expected = 0;
                    for (int i = 0; i < nbVariables; i++) {
                        TestUtils.assertTrue(chain[i].isFixed());
                        TestUtils.assertEquals(first + i, chain[i].getValue().doubleValue());
                        expected += first + i;
                    }
                    TestUtils.assertTrue(total.isFixed());
                    TestUtils.assertEquals(expected, total.getValue().doubleValue());
                } else {
                    TestUtils.assertStateInfeasible(model.minimise());
                }
            }
        }
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

    /**
     * A variable that is in no constraint, with its lower limit above its upper, makes the model infeasible.
     * The UNREFERENCED presolver used to fix such a variable at one of its limits, and the model was solved
     * as if the other limit was not there.
     */
    @Test
    void testUnreferencedCrossedLimits() {

        for (boolean objective : new boolean[] { false, true }) {
            for (boolean integer : new boolean[] { false, true }) {
                for (boolean withConstraint : new boolean[] { false, true }) {

                    ExpressionsBasedModel model = new ExpressionsBasedModel();

                    Variable x = model.newVariable("x").lower(5).upper(3).integer(integer);
                    if (objective) {
                        x.weight(1);
                    }
                    Variable y = model.newVariable("y").lower(0).upper(10).weight(1);
                    if (withConstraint) {
                        model.newExpression("C").set(y, 1).upper(20);
                    }

                    TestUtils.assertStateInfeasible(model.maximise());
                    TestUtils.assertStateInfeasible(model.minimise());
                }
            }
        }
    }

}

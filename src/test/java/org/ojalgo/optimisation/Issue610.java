package org.ojalgo.optimisation;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.netio.BasicLogger;

/**
 * https://github.com/optimatika/ojAlgo/issues/650
 */
class Issue610 extends OptimisationTests {

    static ExpressionsBasedModel makeModel() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x0 = model.addVariable("x0").lower(0.0).upper(1.0);
        Variable x1 = model.addVariable("x1").lower(0.0).upper(1.0);
        Variable x2 = model.addVariable("x2").lower(0.0).upper(1.0);
        Variable x3 = model.addVariable("x3").lower(0.0).upper(1.0);
        Variable x4 = model.addVariable("x4").lower(0.0).upper(1.0);
        Variable x5 = model.addVariable("x5").lower(0.0).upper(1.1);

        x0.weight(-0.3);
        x1.weight(-1.0);
        x2.weight(-0.3753);
        x3.weight(-0.1357);
        x4.weight(-0.3753);
        x5.weight(100000.0);

        // eq_0: x2 = 0.3
        Expression eq0 = model.addExpression("eq_0").level(0.3);
        eq0.set(x2, 1.0);

        // eq_1: x3 = 0.1
        Expression eq1 = model.addExpression("eq_1").level(0.1);
        eq1.set(x3, 1.0);

        // eq_2: x0 + x1 + x2 + x3 + x4 = 1.0
        Expression eq2 = model.addExpression("eq_2").level(1.0);
        eq2.set(x0, 1.0);
        eq2.set(x1, 1.0);
        eq2.set(x2, 1.0);
        eq2.set(x3, 1.0);
        eq2.set(x4, 1.0);

        // Vincoli di disuguaglianza Gx ≤ h

        // ineq_1: -0.4*x0 + 0.6*x1 - 0.4*x2 + 0.6*x3 - 0.4*x4 ≤ 0
        Expression ineq1 = model.addExpression("ineq_1").upper(0.0);
        ineq1.set(x0, -0.4);
        ineq1.set(x1, 0.6);
        ineq1.set(x2, -0.4);
        ineq1.set(x3, 0.6);
        ineq1.set(x4, -0.4);

        // ineq_2: -x0 ≤ 0 (equivalente a x0 ≥ 0, già coperto dai bounds)
        Expression ineq2 = model.addExpression("ineq_2").upper(0.0);
        ineq2.set(x0, -1.0);

        // ineq_3: -0.1*x0 - 0.1*x1 - 0.1*x2 + 0.9*x3 - 0.1*x4 ≤ 0
        Expression ineq3 = model.addExpression("ineq_3").upper(0.0);
        ineq3.set(x0, -0.1);
        ineq3.set(x1, -0.1);
        ineq3.set(x2, -0.1);
        ineq3.set(x3, 0.9);
        ineq3.set(x4, -0.1);

        // ineq_4: x4 - x5 ≤ 0
        Expression ineq4 = model.addExpression("ineq_4").upper(0.0);
        ineq4.set(x4, 1.0);
        ineq4.set(x5, -1.0);

        // ineq_5: -x4 ≤ 0 (equivalente a x4 ≥ 0, già coperto dai bounds)
        Expression ineq5 = model.addExpression("ineq_5").upper(0.0);
        ineq5.set(x4, -1.0);

        // ineq_6: -x1 ≤ 0 (equivalente a x1 ≥ 0, già coperto dai bounds)
        Expression ineq6 = model.addExpression("ineq_6").upper(0.0);
        ineq6.set(x1, -1.0);

        return model;
    }

    @Test
    void testReduction() {

        ExpressionsBasedModel model = Issue610.makeModel();

        boolean eq_2 = model.getExpression("eq_2") != null;
        boolean ineq_3 = model.getExpression("ineq_3") != null;

        TestUtils.assertTrue(eq_2 && ineq_3);

        ExpressionsBasedModel reduced = model.simplify().reduce().simplify();

        if (DEBUG) {
            BasicLogger.debug(model);
            BasicLogger.debug();
            BasicLogger.debug(reduced);
        }

        eq_2 = reduced.getExpression("eq_2") != null;
        ineq_3 = reduced.getExpression("ineq_3") != null;

        TestUtils.assertTrue(eq_2 ^ ineq_3);
    }

    @Test
    void testSimplifyCopyFalseFalse() {
        Issue610.makeModel().simplify().copy(false, false);
    }

    @Test
    void testSimplifyCopyFalseTrue() {
        Issue610.makeModel().simplify().copy(false, true);
    }

    @Test
    void testSimplifyCopyTrueFalse() {
        Issue610.makeModel().simplify().copy(true, false);
    }

    @Test
    void testSimplifyCopyTrueTrue() {
        Issue610.makeModel().simplify().copy(true, true);
    }

}

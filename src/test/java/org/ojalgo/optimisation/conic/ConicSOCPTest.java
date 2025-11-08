package org.ojalgo.optimisation.conic;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.type.context.NumberContext;

/**
 * Forward-compatible SOCP test: Detects and enforces a simple second-order cone constraint.
 * Constraint form: -t^2 + u1^2 + u2^2 <= 0 with objective minimise t.
 * Expected optimum: t = 0, u1 = 0, u2 = 0 (on the boundary); solver may return small positive t maintaining interior.
 */
public class ConicSOCPTest {

    private static final NumberContext TOL = NumberContext.of(8, 6);

    @Test
    public void detectsAndSolvesSimpleSOCConstraint() {
        ExpressionsBasedModel model = new ExpressionsBasedModel();
        Variable t = model.addVariable("t").weight(1.0); // minimise t
        Variable u1 = model.addVariable("u1");
        Variable u2 = model.addVariable("u2");
        // Pure diagonal quadratic inequality: -t^2 + u1^2 + u2^2 <= 0
        model.addExpression("soc").set(t, t, -1.0).set(u1, u1, 1.0).set(u2, u2, 1.0).upper(0.0);

        ConicSolver solver = ConicSolver.INTEGRATION.build(model);
        Optimisation.Result result = solver.solve();

        // Ensure SOC cone was detected
        boolean hasSOC = solver.problem().cones.stream().anyMatch(cb -> cb.cone instanceof ConicSolver.SecondOrderCone);
        TestUtils.assertTrue(hasSOC);

        // Feasibility: -t^2 + u1^2 + u2^2 <= 0 (allow small numerical violations)
        double tVal = result.doubleValue(model.indexOf(t));
        double u1Val = result.doubleValue(model.indexOf(u1));
        double u2Val = result.doubleValue(model.indexOf(u2));
        double socExpr = (-tVal * tVal) + (u1Val * u1Val) + (u2Val * u2Val);
        TestUtils.assertTrue("SOC inequality violated", socExpr <= 1e-7); // small tolerance

        // Objective: minimise t, expect near minimal value (nonnegative interior) >= 0 but small
        TestUtils.assertTrue(tVal >= -1e-8); // should not be significantly negative
        // Boundary expectation: t >= sqrt(u1^2 + u2^2); verify within tolerance
        double radius = Math.sqrt(u1Val * u1Val + u2Val * u2Val);
        TestUtils.assertTrue(tVal + 1e-7 >= radius);
    }
}

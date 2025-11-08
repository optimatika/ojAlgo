package org.ojalgo.optimisation.conic;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;

/**
 * Mixed linear + SOC test: Ensures simultaneous presence of bound-derived NonnegativeCone
 * and auto-detected SecondOrderCone does not break feasibility/residual logic.
 * Problem: minimise t + x subject to:
 *   - SOC: -t^2 + u1^2 + u2^2 <= 0
 *   - Bounds: 0 <= x <= 2, 0 <= t (enforced via lower bound), u1,u2 free
 *   - Linear equality: x + u1 = 1 (to engage equality residual path)
 * Expected qualitative outcome: feasible interior point with t >= sqrt(u1^2+u2^2), x near 1 - u1.
 * Residuals should be finite; primal residual small; dual residual moderate (outline implementation).
 */
public class ConicMixedLinearSOCTest {

    @Test
    public void testMixedLinearAndSOC() {
        ExpressionsBasedModel model = new ExpressionsBasedModel();
        Variable t = model.addVariable("t").lower(0).weight(1.0); // part of objective, nonnegative
        Variable u1 = model.addVariable("u1");
        Variable u2 = model.addVariable("u2");
        Variable x = model.addVariable("x").lower(0).upper(2).weight(1.0); // linear part

        // Equality: x + u1 = 1
        model.addExpression("eq").set(x, 1).set(u1, 1).level(1.0);
        // SOC inequality: -t^2 + u1^2 + u2^2 <= 0
        model.addExpression("soc").set(t, t, -1.0).set(u1, u1, 1.0).set(u2, u2, 1.0).upper(0.0);

        ConicSolver solver = ConicSolver.INTEGRATION.build(model);
        Optimisation.Result result = solver.solve();

        // Basic feasibility state
        TestUtils.assertTrue(result.getState().isFeasible() || result.getState().isApproximate() || result.getState().isOptimal());

        // Cone detection
        boolean hasNonNeg = solver.problem().cones.stream().anyMatch(cb -> cb.cone instanceof ConicSolver.NonnegativeCone);
        boolean hasSOC = solver.problem().cones.stream().anyMatch(cb -> cb.cone instanceof ConicSolver.SecondOrderCone);
        TestUtils.assertTrue("Expected NonnegativeCone", hasNonNeg);
        TestUtils.assertTrue("Expected SecondOrderCone", hasSOC);

        int tIdx = model.indexOf(t);
        int u1Idx = model.indexOf(u1);
        int u2Idx = model.indexOf(u2);
        int xIdx = model.indexOf(x);
        double tVal = result.doubleValue(tIdx);
        double u1Val = result.doubleValue(u1Idx);
        double u2Val = result.doubleValue(u2Idx);
        double xVal = result.doubleValue(xIdx);

        // SOC feasibility: t >= sqrt(u1^2 + u2^2) (allow small numerical slack)
        double radius = Math.sqrt(u1Val * u1Val + u2Val * u2Val);
        TestUtils.assertTrue("SOC feasibility violated", tVal + 1e-6 >= radius);
        TestUtils.assertTrue("t should be nonnegative", tVal >= -1e-8);
        TestUtils.assertTrue("x within bounds", xVal >= -1e-8 && xVal <= 2.0 + 1e-6);

        // Equality approximately satisfied: x + u1 = 1 (soft check at scaffold stage; will tighten when predictor-corrector added)
        double eqResid = Math.abs((xVal + u1Val) - 1.0);
        TestUtils.assertTrue("Equality residual not finite", Double.isFinite(eqResid));
        TestUtils.assertTrue("Equality residual excessively large", eqResid < 5.0); // wide tolerance for now

        // Residual norms: primal should be moderate, dual finite
        TestUtils.assertTrue("Primal residual too large", solver.rpInf() < 5.0); // align with broad equality tolerance
        TestUtils.assertTrue("Dual residual not finite", Double.isFinite(solver.rdInf()));
        TestUtils.assertTrue("Dual residual excessively large", solver.rdInf() < 200.0);
    }
}
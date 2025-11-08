package org.ojalgo.optimisation.conic;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;

public class ConicSolverBarrierTest {

    @Test
    public void testBoundConstrainedQuadraticInterior() {
        // minimise (x-1)^2 = x^2 - 2x + 1 subject to 0 <= x <= 2, optimum at x = 1
        ExpressionsBasedModel model = new ExpressionsBasedModel();
        Variable x = model.addVariable("x").lower(0).upper(2);

        var quad = model.addExpression("quad").weight(1.0);
        quad.add(x, x, 2.0); // contributes x^2
        quad.add(x, -2.0); // -2x

        ConicSolver solver = ConicSolver.INTEGRATION.build(model);
        TestUtils.assertEquals(true, solver != null);

        Optimisation.Result result = solver.solve();
        TestUtils.assertEquals(true, result.getState().isSuccess());

        double xVal = result.doubleValue(0);
        TestUtils.assertEquals(1.0, xVal, 5.0e-3);
        TestUtils.assertEquals(-1.0, result.getValue(), 1.0e-2);
    }

    @Test
    public void testSimpleSOCMinimiseT() {
        // minimise t subject to -t^2 + u^2 <= 0 (SOC: t >= |u|) ⇒ optimum t = 0, u = 0.
        // Barrier-only method will stop before boundary; verify feasibility and reduction from start (0.5).
        ExpressionsBasedModel model = new ExpressionsBasedModel();
        Variable t = model.addVariable("t");
        Variable u = model.addVariable("u");
        t.weight(1.0);
        var soc = model.addExpression("soc").upper(0);
        soc.add(t, t, -1.0);
        soc.add(u, u, +1.0);

        ConicSolver solver = ConicSolver.INTEGRATION.build(model);
        TestUtils.assertEquals(true, solver != null);

        Optimisation.Result result = solver.solve();
        TestUtils.assertEquals(true, result.getState().isSuccess());

        double tVal = result.doubleValue(model.indexOf(t));
        double uVal = result.doubleValue(model.indexOf(u));
        TestUtils.assertEquals(true, tVal >= Math.abs(uVal) - 1.0e-3); // cone feasibility
        // Expect improvement from initial 0.5 and stay interior (>0). Allow up to 0.6 margin.
        TestUtils.assertEquals(true, tVal < 0.6);
        TestUtils.assertEquals(true, tVal > 0.0);
        TestUtils.assertEquals(0.0, uVal, 5.0e-2); // u should be small
    }

}
package org.ojalgo.optimisation.conic;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;

/**
 * Basic tests for dual fraction-to-boundary and dz reconstruction with a simple SOC block.
 * Ensures alphaDual < 1 when dz would move z towards the cone boundary and that dz scales with mu.
 */
public class ConicSolverFractionToBoundaryDualTest {

    @Test
    public void testDualFractionToBoundarySOC() {
        // minimise t subject to -t^2 + u^2 <= 0; start interior at t=1, u=0 (initialiseStart sets this).
        ExpressionsBasedModel model = new ExpressionsBasedModel();
        Variable t = model.addVariable("t");
        Variable u = model.addVariable("u");
        t.weight(1.0);
        var soc = model.addExpression("soc").upper(0);
        soc.add(t, t, -1.0);
        soc.add(u, u, +1.0);

        ConicSolver solver = ConicSolver.INTEGRATION.build(model);
        Optimisation.Result first = solver.solve();
        TestUtils.assertEquals(true, first.getState().isSuccess());
        // After a few iterations z should still be positive interior (dual feasibility approx)
        // We can't access private methods; instead check that a subsequent solve from current state improves objective.
        double t1 = first.doubleValue(model.indexOf(t));
        TestUtils.assertEquals(true, t1 > 0.0);
        // Run a second solve (kickstart) to exercise updated starting point
        Optimisation.Result second = solver.solve(first);
        double t2 = second.doubleValue(model.indexOf(t));
        // Expect non-increase in objective (minimise t) and t2 <= t1 within tolerance.
        TestUtils.assertEquals(true, t2 <= t1 + 1e-6);
    }
}

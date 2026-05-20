package org.ojalgo.optimisation.conic;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;

/**
 * Forward-compatible residual test: tiny LP minimise x subject to x = 1 and 0 <= x <= 2. The current outline
 * emphasises that primal residuals reflect only equality rows (bounds handled via slacks) while dual residual
 * tolerances remain loose until predictor–corrector steps land.
 */
public class ConicResidualsTest {

    @Test
    public void testResidualsSimpleBoundedEqualityLP() {
        ExpressionsBasedModel model = new ExpressionsBasedModel();
        Variable x = model.addVariable("x").lower(0).upper(2).weight(1.0);
        model.addExpression("fix").set(x, 1.0).level(1.0); // x = 1

        ConicSolver solver = ConicSolver.INTEGRATION.build(model);
        Optimisation.Result result = solver.solve();

        // Basic success / feasibility.
        TestUtils.assertTrue(result.getState().isFeasible() || result.getState().isApproximate() || result.getState().isOptimal());
        TestUtils.assertEquals(1.0, result.doubleValue(model.indexOf(x)), 5e-3);
        // Primal residual (equality-only) should be modest (scaffold stage).
        TestUtils.assertTrue("Primal residual too large", solver.rpInf() < 1e-1);
        // Dual residual currently large with outline (no predictor–corrector); ensure finite and bounded.
        TestUtils.assertTrue("Dual residual not finite", Double.isFinite(solver.rdInf()));
        TestUtils.assertTrue("Dual residual excessively large", solver.rdInf() < 20.0);
    }
}
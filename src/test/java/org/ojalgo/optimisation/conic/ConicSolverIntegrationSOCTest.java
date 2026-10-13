package org.ojalgo.optimisation.conic;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.type.context.NumberContext;

/**
 * Tests automatic SOC detection in {@link ConicSolver.ModelIntegration} from quadratic inequality constraints
 * of the canonical form sum(u_i^2) - t^2 <= 0 with only diagonal quadratic terms.
 */
public class ConicSolverIntegrationSOCTest {

    private static final NumberContext TOL = NumberContext.of(7, 6);

    /**
     * SOC with 1 u component: minimise t subject to u^2 - t^2 <= 0 and u = 1. Expected t = 1, u = 1.
     */
    @Test
    public void testAutoDetectSOC1D() {
        ExpressionsBasedModel model = new ExpressionsBasedModel();
        Variable t = model.addVariable("t");
        Variable u = model.addVariable("u");
        t.lower(BigDecimal.ZERO); // Ensure proper SOC form: t >= 0
        t.weight(BigDecimal.ONE);
        u.lower(BigDecimal.ONE).upper(BigDecimal.ONE); // Fix u = 1 using bounds (safer for integration)
        model.addExpression("soc1").set(u, u, BigDecimal.ONE).set(t, t, BigDecimal.valueOf(-1)).upper(BigDecimal.ZERO);
        ConicSolver solver = ConicSolver.INTEGRATION.build(model);
        TestUtils.assertTrue("Solver integration should accept model", solver != null);
        Optimisation.Result res = solver.solve();
        Optimisation.Result mapped = ConicSolver.INTEGRATION.toModelState(res, model);
        int tIdx = model.getVariables().indexOf(t);
        int uIdx = model.getVariables().indexOf(u);
        TestUtils.assertTrue(tIdx >= 0 && uIdx >= 0);
        double tVal = mapped.doubleValue(tIdx);
        double uVal = mapped.doubleValue(uIdx);
        TestUtils.assertEquals(1.0, uVal, TOL);
        TestUtils.assertEquals(1.0, tVal, TOL);
        TestUtils.assertTrue("SOC constraint should be satisfied", tVal * tVal >= uVal * uVal - 1e-6);
    }

    /**
     * SOC with 2 u components: minimise t subject to u1^2 + u2^2 - t^2 <= 0 and u1=0.6, u2=0.8. Expected t =
     * sqrt(0.6^2+0.8^2) = 1.
     */
    @Test
    public void testAutoDetectSOC2D() {
        ExpressionsBasedModel model = new ExpressionsBasedModel();
        Variable t = model.addVariable("t");
        Variable u1 = model.addVariable("u1");
        Variable u2 = model.addVariable("u2");
        t.lower(BigDecimal.ZERO); // Ensure proper SOC form: t >= 0
        t.weight(BigDecimal.ONE);
        u1.lower(BigDecimal.valueOf(0.6)).upper(BigDecimal.valueOf(0.6)); // Fix u1
        u2.lower(BigDecimal.valueOf(0.8)).upper(BigDecimal.valueOf(0.8)); // Fix u2
        model.addExpression("soc2").set(u1, u1, BigDecimal.ONE).set(u2, u2, BigDecimal.ONE).set(t, t, BigDecimal.valueOf(-1)).upper(BigDecimal.ZERO);
        ConicSolver solver = ConicSolver.INTEGRATION.build(model);
        TestUtils.assertTrue("Solver integration should accept model", solver != null);
        Optimisation.Result res = solver.solve();
        Optimisation.Result mapped = ConicSolver.INTEGRATION.toModelState(res, model);
        int tIdx = model.getVariables().indexOf(t);
        int u1Idx = model.getVariables().indexOf(u1);
        int u2Idx = model.getVariables().indexOf(u2);
        TestUtils.assertTrue(tIdx >= 0 && u1Idx >= 0 && u2Idx >= 0);
        double tVal = mapped.doubleValue(tIdx);
        double u1Val = mapped.doubleValue(u1Idx);
        double u2Val = mapped.doubleValue(u2Idx);
        double radius = Math.sqrt(u1Val * u1Val + u2Val * u2Val);
        TestUtils.assertEquals(0.6, u1Val, TOL);
        TestUtils.assertEquals(0.8, u2Val, TOL);
        TestUtils.assertEquals(radius, tVal, NumberContext.of(7, 3));
        TestUtils.assertTrue("SOC boundary satisfied", tVal >= radius - 1e-6);
    }

}

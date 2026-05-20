package org.ojalgo.optimisation.conic;

import static org.ojalgo.function.constant.BigMath.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.type.context.NumberContext;

/**
 * "Next step" development tests for {@link ConicSolver}. These represent incremental capability targets
 * beyond the initial skeleton implementation.
 * <p>
 * 1) Pure (convex) unconstrained quadratic objective with unique minimiser. 2) Simple bounded linear program
 * (box constrained variable) – ensure handling of simultaneous lower & upper bounds.
 * <p>
 * The current solver is approximate; tests use tolerances rather than strict equality.
 */
public class ConicSolverDevelopmentTest {

    private static final NumberContext ACCURACY = NumberContext.of(7, 6);

    /**
     * Minimise x subject to 0 <= x <= 1. Expected minimiser at x = 0. Uses variable bounds only (no explicit
     * expressions) -> tests bound to inequality conversion.
     */
    @Test
    public void testBoundedLinearVariable() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();
        Variable x = model.addVariable("x").lower(ZERO).upper(ONE).weight(ONE);

        ConicSolver solver = ConicSolver.INTEGRATION.build(model);
        TestUtils.assertTrue("Integration rejected capable model", solver != null);

        Optimisation.Result solverState = solver.solve(null);
        Optimisation.Result modelState = ConicSolver.INTEGRATION.toModelState(solverState, model);

        double xVal = modelState.doubleValue(0);

        // Allow small positive offset due to barrier interior requirement
        TestUtils.assertTrue("x should be close to lower bound", xVal <= 1e-3);
        TestUtils.assertTrue("x should be non-negative", xVal >= -1e-9);
    }

    /**
     * Minimise (x - 1)^2 + (y - 2)^2 subject to x + y = 3. Expected minimiser x=1, y=2. Exercises equality
     * KKT path without inequalities.
     */
    @Test
    public void testEqualityConstrainedQuadratic() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();
        Variable x = model.addVariable("x");
        Variable y = model.addVariable("y");

        Expression obj = model.addExpression("OBJ").weight(ONE);
        obj.set(x, BigDecimal.valueOf(-2));
        obj.set(y, BigDecimal.valueOf(-4));
        obj.set(x, x, BigDecimal.valueOf(2));
        obj.set(y, y, BigDecimal.valueOf(2));

        // Equality: x + y = 3
        model.addExpression("EQ").set(x, 1).set(y, 1).level(BigDecimal.valueOf(3));

        ConicSolver solver = ConicSolver.INTEGRATION.build(model);
        TestUtils.assertTrue("Integration rejected capable model", solver != null);

        Optimisation.Result solverState = solver.solve(null);
        Optimisation.Result modelState = ConicSolver.INTEGRATION.toModelState(solverState, model);

        double xVal = modelState.doubleValue(0);
        double yVal = modelState.doubleValue(1);

        TestUtils.assertEquals(1.0, xVal, ACCURACY);
        TestUtils.assertEquals(2.0, yVal, ACCURACY);
        TestUtils.assertEquals(3.0, xVal + yVal, NumberContext.of(7, 6));

        double objVal = modelState.getValue();
        TestUtils.assertEquals(-5.0, objVal, NumberContext.of(7, 4));
    }

    /**
     * Next step: inequality constrained convex QP with an active linear inequality. Minimise (x - 1)^2 + (y -
     * 2)^2 subject to x + y <= 2. Expected minimiser is the projection onto the boundary: x=0.5, y=1.5.
     */
    @Test
    public void testInequalityConstrainedQuadraticActive() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();
        Variable x = model.addVariable("x");
        Variable y = model.addVariable("y");

        Expression obj = model.addExpression("OBJ").weight(ONE);
        obj.set(x, BigDecimal.valueOf(-2));
        obj.set(y, BigDecimal.valueOf(-4));
        obj.set(x, x, BigDecimal.valueOf(2));
        obj.set(y, y, BigDecimal.valueOf(2));

        // Inequality: x + y <= 2
        model.addExpression("INEQ").set(x, 1).set(y, 1).upper(BigDecimal.valueOf(2));

        ConicSolver solver = ConicSolver.INTEGRATION.build(model);
        TestUtils.assertTrue("Integration rejected capable model", solver != null);

        Optimisation.Result solverState = solver.solve(null);
        Optimisation.Result modelState = ConicSolver.INTEGRATION.toModelState(solverState, model);

        double xVal = modelState.doubleValue(0);
        double yVal = modelState.doubleValue(1);

        TestUtils.assertEquals(0.5, xVal, NumberContext.of(7, 3));
        TestUtils.assertEquals(1.5, yVal, NumberContext.of(7, 3));
        TestUtils.assertTrue("Inequality should be nearly active", (xVal + yVal) <= 2.0 + 1e-6);
        TestUtils.assertTrue("Interior barrier keeps it strictly feasible", (xVal + yVal) < 2.0 + 1e-6);
    }

    /**
     * Simple SOC: minimise -x subject to [1; x] ∈ Q₂ ⇔ |x| < 1, optimum at x = 1. Uses the low-level builder
     * API to assemble (A,b,c) and a single SOC block.
     */
    @Test
    public void testSimpleSOC1D() {
        // Variables: x ∈ R
        int n = 1;
        // Inequalities (2 rows): s = b - A x, with s in SOC of size 2
        // Choose s0 = 1 (A row 0 = 0, b0 = 1) and s1 = x (A row 1 = -1, b1 = 0)
        R064Store A = R064Store.FACTORY.make(2, n);
        A.set(0, 0, 0.0);
        A.set(1, 0, -1.0);
        R064Store b = R064Store.FACTORY.make(2, 1);
        b.set(0, 0, 1.0);
        b.set(1, 0, 0.0);
        // Objective: minimise -x (drive x to the upper boundary 1)
        R064Store c = R064Store.FACTORY.make(n, 1);
        c.set(0, 0, -1.0);

        ConicSolver.Builder builder = new ConicSolver.Builder().A(A).b(b).c(c).addCone(new ConicSolver.SecondOrderCone(2));

        ConicSolver solver = new ConicSolver(builder.build(), new Optimisation.Options());
        Optimisation.Result res = solver.solve();

        double x = res.doubleValue(0);
        TestUtils.assertTrue("x should be ≤ 1", x <= 1.0 + 1e-6);
        TestUtils.assertTrue("x should be close to the SOC upper boundary", x >= 0.9);
    }

    /**
     * Multi-dimensional SOC: minimise -(x1 + x2)/2 subject to [1; x1; x2] ∈ Q3. Expected solution near
     * boundary with x1≈x2≈1/√2 ≈ 0.7071.
     */
    @Test
    public void testSimpleSOC2D() {
        int n = 2;
        // s0 = 1, s1 = x1, s2 = x2
        R064Store A = R064Store.FACTORY.make(3, n);
        A.set(0, 0, 0.0);
        A.set(0, 1, 0.0);
        A.set(1, 0, -1.0);
        A.set(1, 1, 0.0);
        A.set(2, 0, 0.0);
        A.set(2, 1, -1.0);
        R064Store b = R064Store.FACTORY.make(3, 1);
        b.set(0, 0, 1.0);
        b.set(1, 0, 0.0);
        b.set(2, 0, 0.0);
        R064Store c = R064Store.FACTORY.make(n, 1);
        c.set(0, 0, -0.5);
        c.set(1, 0, -0.5);
        ConicSolver.Builder builder = new ConicSolver.Builder().A(A).b(b).c(c).addCone(new ConicSolver.SecondOrderCone(3));
        ConicSolver solver = new ConicSolver(builder.build(), new Optimisation.Options());
        Optimisation.Result res = solver.solve();
        double x1 = res.doubleValue(0);
        double x2 = res.doubleValue(1);
        double radius2 = x1 * x1 + x2 * x2;
        TestUtils.assertTrue("Within cone boundary", radius2 <= 1.0 + 1e-6);
        TestUtils.assertTrue("Near boundary", radius2 >= 0.90);
        TestUtils.assertEquals(Math.sqrt(0.5), x1, NumberContext.of(7, 2));
        TestUtils.assertEquals(Math.sqrt(0.5), x2, NumberContext.of(7, 2));
    }

    /**
     * Minimise (x - 1)^2 + (y - 2)^2. Equivalent to 0.5 x^T Q x + c^T x with Q = 2I and c = [-2,-4]. Expected
     * minimiser: x = 1, y = 2. Objective optimum value: ~0.
     */
    @Test
    public void testUnconstrainedQuadratic() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();
        Variable x = model.addVariable("x");
        Variable y = model.addVariable("y");

        // Build quadratic objective expression
        Expression obj = model.addExpression("OBJ").weight(ONE);
        obj.set(x, BigDecimal.valueOf(-2)); // linear factors c
        obj.set(y, BigDecimal.valueOf(-4));
        obj.set(x, x, BigDecimal.valueOf(2)); // Q diagonal entries (0.5 * 2 = 1)
        obj.set(y, y, BigDecimal.valueOf(2));

        ConicSolver solver = ConicSolver.INTEGRATION.build(model);
        TestUtils.assertTrue("Integration rejected capable model", solver != null);

        Optimisation.Result solverState = solver.solve(null);
        Optimisation.Result modelState = ConicSolver.INTEGRATION.toModelState(solverState, model);

        double xVal = modelState.doubleValue(0);
        double yVal = modelState.doubleValue(1);

        // Check proximity to optimum
        TestUtils.assertEquals(1.0, xVal, ACCURACY);
        TestUtils.assertEquals(2.0, yVal, ACCURACY);

        // Objective value near modelled minimum (constant term not stored => -5)
        double objVal = modelState.getValue();
        TestUtils.assertEquals(-5.0, objVal, NumberContext.of(7, 4));
    }

}

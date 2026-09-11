/*
 * Copyright 1997-2026 Optimatika
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.ojalgo.optimisation.linear;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.ExpressionsBasedModel.EntityMap;
import org.ojalgo.optimisation.ExpressionsBasedModel.Integration;
import org.ojalgo.optimisation.ModelEntity;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.ConstraintType;
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.optimisation.UpdatableSolver;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.optimisation.integer.IntegerSolver;
import org.ojalgo.type.context.NumberContext;
import org.ojalgo.type.keyvalue.EntryPair;

/**
 * Tests {@link UpdatableSolver} methods ({@code fixVariable}, {@code updateRange}, {@code getDualMultiplier},
 * {@code getReducedGradient}, {@code getEntityMap}) as exposed by solver integrations built from
 * {@link ExpressionsBasedModel}.
 * <p>
 * Also tests that an LP integration can serve as a sub-solver for {@link IntegerSolver} by registering it
 * with a capability predicate that excludes integer models.
 * <p>
 * Subclasses override {@link #integrations()} to plug in external solver integrations.
 */
public class IntegrationUpdatableSolverTest extends OptimisationLinearTests {

    private static final NumberContext ACCURACY = NumberContext.of(6);

    /**
     * Build a solver from the integration and cast to UpdatableSolver; fails the test if the solver does not
     * implement UpdatableSolver.
     */
    private static UpdatableSolver buildUpdatableSolver(final Integration<?> integration, final ExpressionsBasedModel model) {
        Optimisation.Solver solver = integration.build(model);
        TestUtils.assertTrue("Solver must implement UpdatableSolver", solver instanceof UpdatableSolver);
        return (UpdatableSolver) solver;
    }

    /**
     * <pre>
     * min  -2*x0 - 3*x1 + x2 - 5*x3
     * s.t. 2*x0 + 3*x1 +  x2 + 2*x3 = 17    (equality)
     *      3*x0 +  x1        + 2*x3 <= 9      (inequality 0)
     *       x0       + 2*x2 + 3*x3 <= 8       (inequality 1)
     *      0 <= x0 <= 4,  1 <= x1 <= 5,  0 <= x2 <= 3,  0 <= x3 <= 3
     *      all integer
     * </pre>
     */
    private static ExpressionsBasedModel newSmallMIPModel() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x0 = model.newVariable("x0").lower(0).upper(4).integer().weight(-2);
        Variable x1 = model.newVariable("x1").lower(1).upper(5).integer().weight(-3);
        Variable x2 = model.newVariable("x2").lower(0).upper(3).integer().weight(1);
        Variable x3 = model.newVariable("x3").lower(0).upper(3).integer().weight(-5);

        model.newExpression("eq").set(x0, 2).set(x1, 3).set(x2, 1).set(x3, 2).level(17);
        model.newExpression("iq0").set(x0, 3).set(x1, 1).set(x3, 2).upper(9);
        model.newExpression("iq1").set(x0, 1).set(x2, 2).set(x3, 3).upper(8);

        return model;
    }

    /**
     * <pre>
     * min  -2*x - 3*y
     * s.t.   x +   y <= 4   (constraint 0)
     *        x + 3*y <= 6   (constraint 1)
     *        x, y in [0, 10]
     * </pre>
     *
     * Optimal: x=3, y=1, obj=-9. Both constraints bind. Duals: 3/2, 1/2. Reduced gradients: 0, 0.
     */
    private static ExpressionsBasedModel newTwoConstraintModel() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x = model.newVariable("x").lower(0).upper(10).weight(-2);
        Variable y = model.newVariable("y").lower(0).upper(10).weight(-3);

        model.newExpression("c0").set(x, 1).set(y, 1).upper(4);
        model.newExpression("c1").set(x, 1).set(y, 3).upper(6);

        return model;
    }

    /**
     * <pre>
     * min  -2*x - 3*y - z
     * s.t.  x +  y + z <= 4   (constraint 0)
     *       x + 3y + z <= 6   (constraint 1)
     *       x, y, z in [0, 10]
     * </pre>
     *
     * Optimal: x=3, y=1, z=0, obj=-9. Both constraints bind. z is non-basic at its lower bound with
     * |reduced gradient| = 1.0.
     */
    private static ExpressionsBasedModel newThreeVariableModel() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x = model.newVariable("x").lower(0).upper(10).weight(-2);
        Variable y = model.newVariable("y").lower(0).upper(10).weight(-3);
        Variable z = model.newVariable("z").lower(0).upper(10).weight(-1);

        model.newExpression("c0").set(x, 1).set(y, 1).set(z, 1).upper(4);
        model.newExpression("c1").set(x, 1).set(y, 3).set(z, 1).upper(6);

        return model;
    }

    /**
     * <pre>
     * min  -x - 2y
     * s.t.  1 <= x + y <= 3    (ranged: both lower and upper, not equality)
     *            x + 3y <= 6   (upper only)
     *       x, y in [0, 10]
     * </pre>
     *
     * Optimal: x=1.5, y=1.5, obj=-4.5. The ranged constraint binds at its upper bound (x+y=3) and the
     * upper-only constraint also binds (x+3y=6). The lower bound of the ranged constraint does not bind.
     */
    private static ExpressionsBasedModel newRangedConstraintModel() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x = model.newVariable("x").lower(0).upper(10).weight(-1);
        Variable y = model.newVariable("y").lower(0).upper(10).weight(-2);

        model.newExpression("range").set(x, 1).set(y, 1).lower(1).upper(3);
        model.newExpression("ub").set(x, 1).set(y, 3).upper(6);

        return model;
    }

    /**
     * Registers each LP integration as a sub-solver (via environment + capability predicate) and solves a
     * small MIP through {@link IntegerSolver}, verifying the optimal integer solution.
     */
    @Test
    public void testAsIntegerSubSolver() {

        for (Integration<?> integration : this.integrations()) {

            Optimisation.Environment environment = Optimisation.newEnvironment();
            environment.addIntegration(integration.withCapabilityPredicate(m -> !m.isAnyVariableInteger()));

            ExpressionsBasedModel model = environment.newModel();
            model.options.time_abort = 30_000L;

            Variable x0 = model.newVariable("x0").lower(0).upper(4).integer().weight(-2);
            Variable x1 = model.newVariable("x1").lower(1).upper(5).integer().weight(-3);
            Variable x2 = model.newVariable("x2").lower(0).upper(3).integer().weight(1);
            Variable x3 = model.newVariable("x3").lower(0).upper(3).integer().weight(-5);

            model.newExpression("eq").set(x0, 2).set(x1, 3).set(x2, 1).set(x3, 2).level(17);
            model.newExpression("iq0").set(x0, 3).set(x1, 1).set(x3, 2).upper(9);
            model.newExpression("iq1").set(x0, 1).set(x2, 2).set(x3, 3).upper(8);

            Result result = model.minimise();

            String tag = integration.toString();

            TestUtils.assertTrue(tag + " MIP should be optimal", result.getState().isOptimal());

            // Verify integrality
            for (int j = 0; j < 4; j++) {
                double val = result.doubleValue(j);
                TestUtils.assertEquals(tag + " x" + j + " integer", Math.rint(val), val, ACCURACY);
            }

            // Verify feasibility of the equality constraint
            double eqLHS = 2.0 * result.doubleValue(0) + 3.0 * result.doubleValue(1) + result.doubleValue(2) + 2.0 * result.doubleValue(3);
            TestUtils.assertEquals(tag + " equality", 17.0, eqLHS, ACCURACY);
        }
    }

    /**
     * Verifies that the solver produces correct duals after tightening bounds and re-solving — simulating
     * what happens at each branch-and-bound node.
     */
    @Test
    public void testDualAfterUpdateRange() {

        for (Integration<?> integration : this.integrations()) {

            ExpressionsBasedModel model = IntegrationUpdatableSolverTest.newTwoConstraintModel();
            UpdatableSolver solver = IntegrationUpdatableSolverTest.buildUpdatableSolver(integration, model);

            solver.solve(null);

            solver.fixVariable(0, 2.0);
            Result result = solver.solve(null);
            TestUtils.assertStateNotLessThanOptimal(result);

            String tag = integration.toString();

            // Constraint 1 binding (x + 3y <= 6, with x=2 => y=4/3): its dual must be non-zero
            double dual1 = solver.getDualMultiplier(1);
            TestUtils.assertTrue(tag + " |dual1| > 0 after fix", Math.abs(dual1) > 1E-8);

            // Basic variable y: reduced gradient should be zero
            TestUtils.assertEquals(tag + " rc1 (basic y)", 0.0, solver.getReducedGradient(1), ACCURACY);
        }
    }

    /**
     * Both constraints bind at the optimum; duals must be non-zero with equal sign (the sign convention
     * varies across solvers — ojAlgo uses positive duals for binding {@code <=} constraints in MIN, while
     * CPLEX/COPT/Gurobi/Xpress use negative). The absolute values must be 3/2 and 1/2.
     */
    @Test
    public void testDualMultiplier() {

        for (Integration<?> integration : this.integrations()) {

            ExpressionsBasedModel model = IntegrationUpdatableSolverTest.newTwoConstraintModel();
            UpdatableSolver solver = IntegrationUpdatableSolverTest.buildUpdatableSolver(integration, model);

            Result result = solver.solve(null);
            TestUtils.assertStateNotLessThanOptimal(result);

            String tag = integration.toString();

            double dual0 = solver.getDualMultiplier(0);
            double dual1 = solver.getDualMultiplier(1);

            TestUtils.assertEquals(tag + " |dual0|", 1.5, Math.abs(dual0), ACCURACY);
            TestUtils.assertEquals(tag + " |dual1|", 0.5, Math.abs(dual1), ACCURACY);

            // Both duals must have the same sign (both constraints are <= and both bind)
            TestUtils.assertTrue(tag + " dual signs consistent", Math.signum(dual0) == Math.signum(dual1));
        }
    }

    @Test
    public void testFixVariable() {

        for (Integration<?> integration : this.integrations()) {

            ExpressionsBasedModel model = IntegrationUpdatableSolverTest.newTwoConstraintModel();
            UpdatableSolver solver = IntegrationUpdatableSolverTest.buildUpdatableSolver(integration, model);

            Result initial = solver.solve(null);
            TestUtils.assertStateNotLessThanOptimal(initial);

            boolean fixed = solver.fixVariable(0, 2.0);
            TestUtils.assertTrue(integration + " fixVariable should succeed", fixed);

            Result updated = solver.solve(null);
            TestUtils.assertStateNotLessThanOptimal(updated);

            String tag = integration.toString();

            TestUtils.assertEquals(tag + " fixed x", 2.0, updated.doubleValue(0), ACCURACY);
            TestUtils.assertEquals(tag + " y after fix", 4.0 / 3.0, updated.doubleValue(1), ACCURACY);
            TestUtils.assertEquals(tag + " obj after fix", -8.0, updated.getValue(), ACCURACY);
        }
    }

    @Test
    public void testGetEntityMap() {

        for (Integration<?> integration : this.integrations()) {

            ExpressionsBasedModel model = IntegrationUpdatableSolverTest.newTwoConstraintModel();
            UpdatableSolver solver = IntegrationUpdatableSolverTest.buildUpdatableSolver(integration, model);

            Optional<EntityMap> optEntityMap = solver.getEntityMap();
            TestUtils.assertTrue(integration + " getEntityMap should be present", optEntityMap.isPresent());

            EntityMap entityMap = optEntityMap.get();

            TestUtils.assertTrue(integration + " countModelVariables > 0", entityMap.countModelVariables() > 0);
            TestUtils.assertTrue(integration + " countVariables > 0", entityMap.countVariables() > 0);
            TestUtils.assertTrue(integration + " countEqualityConstraints + countInequalityConstraints > 0",
                    entityMap.countEqualityConstraints() + entityMap.countInequalityConstraints() > 0);
        }
    }

    @Test
    public void testReducedGradient() {

        for (Integration<?> integration : this.integrations()) {

            ExpressionsBasedModel model = IntegrationUpdatableSolverTest.newTwoConstraintModel();
            UpdatableSolver solver = IntegrationUpdatableSolverTest.buildUpdatableSolver(integration, model);

            Result result = solver.solve(null);
            TestUtils.assertStateNotLessThanOptimal(result);

            String tag = integration.toString();

            TestUtils.assertEquals(tag + " rc0 (basic)", 0.0, solver.getReducedGradient(0), ACCURACY);
            TestUtils.assertEquals(tag + " rc1 (basic)", 0.0, solver.getReducedGradient(1), ACCURACY);
        }
    }

    /**
     * z is non-basic at its lower bound, so its reduced gradient must be non-zero (|rc_z| = 1.0).
     */
    @Test
    public void testReducedGradientNonZero() {

        for (Integration<?> integration : this.integrations()) {

            ExpressionsBasedModel model = IntegrationUpdatableSolverTest.newThreeVariableModel();
            UpdatableSolver solver = IntegrationUpdatableSolverTest.buildUpdatableSolver(integration, model);

            Result result = solver.solve(null);
            TestUtils.assertStateNotLessThanOptimal(result);

            String tag = integration.toString();

            TestUtils.assertEquals(tag + " |rc_z| (non-basic)", 1.0, Math.abs(solver.getReducedGradient(2)), ACCURACY);
        }
    }

    @Test
    public void testUpdateRange() {

        for (Integration<?> integration : this.integrations()) {

            ExpressionsBasedModel model = IntegrationUpdatableSolverTest.newTwoConstraintModel();
            UpdatableSolver solver = IntegrationUpdatableSolverTest.buildUpdatableSolver(integration, model);

            Result initial = solver.solve(null);
            TestUtils.assertStateNotLessThanOptimal(initial);

            boolean updated = solver.updateRange(0, 0.0, 1.0);
            TestUtils.assertTrue(integration + " updateRange should succeed", updated);

            Result result = solver.solve(null);
            TestUtils.assertStateNotLessThanOptimal(result);

            String tag = integration.toString();

            TestUtils.assertTrue(tag + " x <= 1", result.doubleValue(0) <= 1.0 + 1E-8);
        }
    }

    /**
     * Verifies that a model with a ranged expression (both lower and upper bounds, not equality) solves
     * correctly, and that the {@link EntityMap} properly accounts for the ranged constraint(s).
     * <p>
     * Solvers that split a ranged expression into two rows (one {@code >=}, one {@code <=}) will report more
     * constraints than solvers that use a native range row. The test accepts both representations.
     */
    @Test
    public void testRangedExpression() {

        for (Integration<?> integration : this.integrations()) {

            ExpressionsBasedModel model = IntegrationUpdatableSolverTest.newRangedConstraintModel();
            UpdatableSolver solver = IntegrationUpdatableSolverTest.buildUpdatableSolver(integration, model);

            Result result = solver.solve(null);
            TestUtils.assertStateNotLessThanOptimal(result);

            String tag = integration.toString();

            TestUtils.assertEquals(tag + " x", 1.5, result.doubleValue(0), ACCURACY);
            TestUtils.assertEquals(tag + " y", 1.5, result.doubleValue(1), ACCURACY);
            TestUtils.assertEquals(tag + " obj", -4.5, result.getValue(), ACCURACY);

            Optional<EntityMap> optMap = solver.getEntityMap();
            TestUtils.assertTrue(tag + " EntityMap present", optMap.isPresent());

            EntityMap map = optMap.get();

            TestUtils.assertTrue(tag + " variables > 0", map.countVariables() > 0);
            TestUtils.assertTrue(tag + " modelVariables > 0", map.countModelVariables() > 0);

            int totalConstraints = map.countConstraints();
            TestUtils.assertTrue(tag + " constraints >= 2", totalConstraints >= 2);
            TestUtils.assertEquals(tag + " eq + ineq = total", totalConstraints,
                    map.countEqualityConstraints() + map.countInequalityConstraints());
            TestUtils.assertEquals(tag + " no equality constraints", 0, map.countEqualityConstraints());

            for (int c = 0; c < totalConstraints; c++) {
                EntryPair<ModelEntity<?>, ConstraintType> entry = map.getConstraint(c);
                TestUtils.assertNotNull(entry);
                String name = entry.getKey().getName();
                TestUtils.assertTrue(tag + " constraint " + c + " maps to known expression (" + name + ")",
                        "range".equals(name) || "ub".equals(name));
            }
        }
    }

    /**
     * Verifies that duals are correctly retrieved for a model with a ranged expression. At the optimum, the
     * upper side of the range and the upper-only constraint both bind, so their duals must be non-zero.
     */
    @Test
    public void testRangedExpressionDuals() {

        for (Integration<?> integration : this.integrations()) {

            ExpressionsBasedModel model = IntegrationUpdatableSolverTest.newRangedConstraintModel();
            UpdatableSolver solver = IntegrationUpdatableSolverTest.buildUpdatableSolver(integration, model);

            Result result = solver.solve(null);
            TestUtils.assertStateNotLessThanOptimal(result);

            String tag = integration.toString();

            EntityMap map = solver.getEntityMap().get();
            int totalConstraints = map.countConstraints();

            int nonZeroDuals = 0;
            for (int c = 0; c < totalConstraints; c++) {
                double dual = solver.getDualMultiplier(c);
                if (Math.abs(dual) > 1E-8) {
                    nonZeroDuals++;
                }
            }

            TestUtils.assertTrue(tag + " at least 2 non-zero duals (2 binding constraints)", nonZeroDuals >= 2);
        }
    }

    /**
     * Verifies that {@code fixVariable} and re-solve work correctly in the presence of a ranged expression.
     */
    @Test
    public void testRangedExpressionFixVariable() {

        for (Integration<?> integration : this.integrations()) {

            ExpressionsBasedModel model = IntegrationUpdatableSolverTest.newRangedConstraintModel();
            UpdatableSolver solver = IntegrationUpdatableSolverTest.buildUpdatableSolver(integration, model);

            solver.solve(null);

            boolean fixed = solver.fixVariable(0, 0.0);
            TestUtils.assertTrue(integration + " fixVariable should succeed", fixed);

            Result result = solver.solve(null);
            TestUtils.assertStateNotLessThanOptimal(result);

            String tag = integration.toString();

            TestUtils.assertEquals(tag + " fixed x=0", 0.0, result.doubleValue(0), ACCURACY);

            double y = result.doubleValue(1);
            TestUtils.assertTrue(tag + " y in ranged [1,3]", y >= 1.0 - 1E-8 && y <= 3.0 + 1E-8);
            TestUtils.assertTrue(tag + " x+3y <= 6", 0.0 + 3.0 * y <= 6.0 + 1E-8);

            TestUtils.assertEquals(tag + " obj after fix", -4.0, result.getValue(), ACCURACY);
        }
    }

    /**
     * Returns the integrations to test. The base class returns the default ojAlgo LinearSolver. Subclasses
     * override to return external solver integrations.
     */
    protected List<Integration<?>> integrations() {
        return List.of(LinearSolver.INTEGRATION);
    }

}

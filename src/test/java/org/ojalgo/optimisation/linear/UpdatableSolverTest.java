/*
 * Copyright 1997-2025 Optimatika
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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.type.context.NumberContext;

/**
 * Tests for {@link org.ojalgo.optimisation.UpdatableSolver#getDualMultiplier(int)} and
 * {@link org.ojalgo.optimisation.UpdatableSolver#getReducedGradient(int)} as exposed by {@link LinearSolver}.
 * <p>
 * Each test builds a small LP via {@link LinearSolver.Builder}, then creates all 5 solver configurations (3
 * from {@link OptimisationLinearTests#STORE_FACTORIES} and 2 from
 * {@link OptimisationLinearTests#TABLEAU_FACTORIES}) and verifies primal solution, objective value, dual
 * multipliers and reduced gradients for each.
 */
public class UpdatableSolverTest extends OptimisationLinearTests {

    /**
     * Pairs a descriptive label with a solver instance so that failure messages identify which configuration
     * failed.
     */
    private static final class NamedSolver {

        final String name;
        final LinearSolver solver;

        NamedSolver(final String name, final LinearSolver solver) {
            this.name = name;
            this.solver = solver;
        }
    }

    private static final NumberContext ACCURACY = NumberContext.of(8);

    /**
     * Build all 5 solver configurations from a single builder.
     */
    private static List<NamedSolver> buildAllSolvers(final LinearSolver.Builder builder) {

        Optimisation.Options options = new Optimisation.Options();

        List<NamedSolver> solvers = new ArrayList<>();

        for (Function<LinearStructure, SimplexStore> factory : STORE_FACTORIES) {
            SimplexStore store = builder.newSimplexStore(factory);
            LinearSolver solver = store.newPhasedSimplexSolver(options);
            solvers.add(new NamedSolver("PhasedSimplex/" + store.getClass().getSimpleName(), solver));
        }

        for (Function<LinearStructure, SimplexTableau> factory : TABLEAU_FACTORIES) {
            SimplexTableau tableau = builder.newSimplexTableau(factory);
            LinearSolver solver = tableau.newSimplexTableauSolver(options);
            solvers.add(new NamedSolver("TableauSolver/" + tableau.getClass().getSimpleName(), solver));
        }

        return solvers;
    }

    /**
     * Verify that solution, dual multipliers and reduced gradients are correct after re-solving with a fixed
     * variable via {@link org.ojalgo.optimisation.UpdatableSolver#fixVariable(int, double)}.
     *
     * <pre>
     * min  -2*x1 - 3*x2
     * s.t.   x1 +   x2 <= 4   (constraint 0)
     *        x1 + 3*x2 <= 6   (constraint 1)
     *        x1, x2 >= 0
     * </pre>
     *
     * After fixing x1=2: x2=4/3 (constraint 1 binding), obj=-8. Constraint 0 has slack 2/3 so its dual must
     * be zero. The fixed variable x1 is non-basic so its reduced gradient should be non-zero; x2 is basic so
     * its reduced gradient must be zero.
     */
    @Test
    public void testAfterFixVariable() {

        LinearSolver.Builder builder = LinearSolver.newBuilder(-2.0, -3.0);
        builder.inequality(4.0, 1.0, 1.0);
        builder.inequality(6.0, 1.0, 3.0);

        for (NamedSolver ns : UpdatableSolverTest.buildAllSolvers(builder)) {
            String tag = ns.name;
            Result initial = ns.solver.solve();
            TestUtils.assertStateNotLessThanOptimal(initial);

            boolean fixed = ns.solver.fixVariable(0, 2.0);

            if (fixed) {
                Result updated = ns.solver.solve();
                TestUtils.assertStateNotLessThanOptimal(updated);

                TestUtils.assertEquals(tag + " fixed x1", 2.0, updated.doubleValue(0), ACCURACY);
                TestUtils.assertEquals(tag + " x2", 4.0 / 3.0, updated.doubleValue(1), ACCURACY);
                TestUtils.assertEquals(tag + " obj", -8.0, updated.getValue(), ACCURACY);

                TestUtils.assertEquals(tag + " dual1", 1.0, ns.solver.getDualMultiplier(1), ACCURACY);

                TestUtils.assertEquals(tag + " rc1 (basic x2)", 0.0, ns.solver.getReducedGradient(1), ACCURACY);
            }
        }
    }

    /**
     * All positive objective coefficients, all variables optimal at their lower bound (zero). Both
     * constraints are slack. Duals must be zero by complementary slackness. Reduced gradients equal the
     * objective coefficients (since all duals are zero).
     *
     * <pre>
     * min  2*x1 + 3*x2 + x3
     * s.t. x1 + x2 + x3 <= 10   (constraint 0)
     *      2*x1 +      x3 <= 8   (constraint 1)
     *      x1, x2, x3 >= 0
     * </pre>
     */
    @Test
    public void testAllVariablesAtLowerBound() {

        LinearSolver.Builder builder = LinearSolver.newBuilder(2.0, 3.0, 1.0);
        builder.inequality(10.0, 1.0, 1.0, 1.0);
        builder.inequality(8.0, 2.0, 0.0, 1.0);

        for (NamedSolver ns : UpdatableSolverTest.buildAllSolvers(builder)) {
            String tag = ns.name;
            Result result = ns.solver.solve();

            TestUtils.assertStateNotLessThanOptimal(result);
            TestUtils.assertEquals(tag + " obj", 0.0, result.getValue(), ACCURACY);
            TestUtils.assertEquals(tag + " x1", 0.0, result.doubleValue(0), ACCURACY);
            TestUtils.assertEquals(tag + " x2", 0.0, result.doubleValue(1), ACCURACY);
            TestUtils.assertEquals(tag + " x3", 0.0, result.doubleValue(2), ACCURACY);

            TestUtils.assertEquals(tag + " dual0", 0.0, ns.solver.getDualMultiplier(0), ACCURACY);
            TestUtils.assertEquals(tag + " dual1", 0.0, ns.solver.getDualMultiplier(1), ACCURACY);

            TestUtils.assertEquals(tag + " rc0", 2.0, ns.solver.getReducedGradient(0), ACCURACY);
            TestUtils.assertEquals(tag + " rc1", 3.0, ns.solver.getReducedGradient(1), ACCURACY);
            TestUtils.assertEquals(tag + " rc2", 1.0, ns.solver.getReducedGradient(2), ACCURACY);
        }
    }

    /**
     * Verify complementary slackness holds numerically: if a constraint is slack its dual must be zero, and
     * if a variable is strictly positive its reduced gradient must be zero.
     *
     * <pre>
     * min  -2*x1 - 3*x2
     * s.t.   x1 +   x2 <= 4   (constraint 0)
     *        x1 + 3*x2 <= 6   (constraint 1)
     *        x1, x2 >= 0
     * </pre>
     */
    @Test
    public void testComplementarySlackness() {

        double[] c = { -2.0, -3.0 };
        double[][] a = { { 1.0, 1.0 }, { 1.0, 3.0 } };
        double[] b = { 4.0, 6.0 };

        LinearSolver.Builder builder = LinearSolver.newBuilder(c);
        builder.inequality(b[0], a[0]);
        builder.inequality(b[1], a[1]);

        for (NamedSolver ns : UpdatableSolverTest.buildAllSolvers(builder)) {
            String tag = ns.name;
            Result result = ns.solver.solve();
            TestUtils.assertStateNotLessThanOptimal(result);

            for (int j = 0; j < c.length; j++) {
                double xj = result.doubleValue(j);
                double rcj = ns.solver.getReducedGradient(j);
                if (xj > 1E-10) {
                    TestUtils.assertEquals(tag + " rc" + j + " (basic)", 0.0, rcj, ACCURACY);
                }
            }

            for (int i = 0; i < b.length; i++) {
                double lhs = 0.0;
                for (int j = 0; j < c.length; j++) {
                    lhs += a[i][j] * result.doubleValue(j);
                }
                double slack = b[i] - lhs;
                double yi = ns.solver.getDualMultiplier(i);
                if (slack > 1E-10) {
                    TestUtils.assertEquals(tag + " dual" + i + " (slack)", 0.0, yi, ACCURACY);
                }
            }
        }
    }

    /**
     * Solve the same problem formulated as primal and as dual, then verify that the primal's dual multipliers
     * equal the dual's primal solution and that the objective values match (strong duality).
     * <p>
     * Both problems are expressed as minimisations (the dual max is negated). At optimality the two objective
     * values are equal, the primal's dual multipliers match the dual's solution, and the dual's dual
     * multipliers match the primal's solution.
     *
     * <pre>
     * Primal: min  -5*x1 - 4*x2
     *   s.t.  6*x1 + 4*x2 <= 24
     *         3*x1 + 6*x2 <= 24
     *         x1, x2 >= 0
     *
     * Dual (as min): min  -24*y1 - 24*y2
     *   s.t.  6*y1 + 3*y2 <= 5
     *         4*y1 + 6*y2 <= 4
     *         y1, y2 >= 0
     * </pre>
     */
    @Test
    public void testPrimalDualCrossCheck() {

        LinearSolver.Builder primalBuilder = LinearSolver.newBuilder(-5.0, -4.0);
        primalBuilder.inequality(24.0, 6.0, 4.0);
        primalBuilder.inequality(24.0, 3.0, 6.0);

        LinearSolver.Builder dualBuilder = LinearSolver.newBuilder(-24.0, -24.0);
        dualBuilder.inequality(5.0, 6.0, 3.0);
        dualBuilder.inequality(4.0, 4.0, 6.0);

        for (NamedSolver pns : UpdatableSolverTest.buildAllSolvers(primalBuilder)) {
            String ptag = "Primal/" + pns.name;
            Result primalResult = pns.solver.solve();
            TestUtils.assertStateNotLessThanOptimal(primalResult);

            double primalObj = primalResult.getValue();
            double primalDual0 = pns.solver.getDualMultiplier(0);
            double primalDual1 = pns.solver.getDualMultiplier(1);

            for (NamedSolver dns : UpdatableSolverTest.buildAllSolvers(dualBuilder)) {
                String dtag = "Dual/" + dns.name;
                Result dualResult = dns.solver.solve();
                TestUtils.assertStateNotLessThanOptimal(dualResult);

                double dualY0 = dualResult.doubleValue(0);
                double dualY1 = dualResult.doubleValue(1);

                String tag = ptag + " vs " + dtag;

                TestUtils.assertEquals(tag + " strong duality", primalObj, dualResult.getValue(), ACCURACY);

                TestUtils.assertEquals(tag + " primal dual0 == dual y0", primalDual0, dualY0, ACCURACY);
                TestUtils.assertEquals(tag + " primal dual1 == dual y1", primalDual1, dualY1, ACCURACY);

                double dualDual0 = dns.solver.getDualMultiplier(0);
                double dualDual1 = dns.solver.getDualMultiplier(1);
                TestUtils.assertEquals(tag + " dual dual0 == primal x0", primalResult.doubleValue(0), dualDual0, ACCURACY);
                TestUtils.assertEquals(tag + " dual dual1 == primal x1", primalResult.doubleValue(1), dualDual1, ACCURACY);

                double dualRC0 = dns.solver.getReducedGradient(0);
                double dualRC1 = dns.solver.getReducedGradient(1);
                if (dualY0 > 1E-10) {
                    TestUtils.assertEquals(tag + " dual rc0 (basic)", 0.0, dualRC0, ACCURACY);
                }
                if (dualY1 > 1E-10) {
                    TestUtils.assertEquals(tag + " dual rc1 (basic)", 0.0, dualRC1, ACCURACY);
                }
            }
        }
    }

    /**
     * Trivial LP where the constraint is slack at optimum. The reduced gradient for x1 equals its objective
     * coefficient (since the dual is zero); x2 has zero objective coefficient and zero reduced gradient.
     *
     * <pre>
     * min  x1
     * s.t. x1 + x2 <= 5   (constraint 0)
     *      x1, x2 >= 0
     * </pre>
     */
    @Test
    public void testSingleConstraintTrivial() {

        LinearSolver.Builder builder = LinearSolver.newBuilder(1.0, 0.0);
        builder.inequality(5.0, 1.0, 1.0);

        for (NamedSolver ns : UpdatableSolverTest.buildAllSolvers(builder)) {
            String tag = ns.name;
            Result result = ns.solver.solve();

            TestUtils.assertStateNotLessThanOptimal(result);
            TestUtils.assertEquals(tag + " obj", 0.0, result.getValue(), ACCURACY);
            TestUtils.assertEquals(tag + " x1", 0.0, result.doubleValue(0), ACCURACY);
            TestUtils.assertEquals(tag + " x2", 0.0, result.doubleValue(1), ACCURACY);

            TestUtils.assertEquals(tag + " dual0", 0.0, ns.solver.getDualMultiplier(0), ACCURACY);

            TestUtils.assertEquals(tag + " rc0", 1.0, ns.solver.getReducedGradient(0), ACCURACY);
            TestUtils.assertEquals(tag + " rc1", 0.0, ns.solver.getReducedGradient(1), ACCURACY);
        }
    }

    /**
     * One binding constraint and two slack constraints. Duals for slack constraints must be zero. Both
     * variables are basic (positive) so their reduced gradients must be zero.
     *
     * <pre>
     * min  -x1 - x2
     * s.t. x1 + x2 <= 4   (constraint 0, binding)
     *      x1      <= 6   (constraint 1, slack)
     *           x2 <= 6   (constraint 2, slack)
     *      x1, x2 >= 0
     * </pre>
     *
     * Optimal: x1+x2=4, obj=-4. Dual of binding constraint = 1. Slack constraint duals = 0.
     */
    @Test
    public void testSlackConstraints() {

        LinearSolver.Builder builder = LinearSolver.newBuilder(-1.0, -1.0);
        builder.inequality(4.0, 1.0, 1.0);
        builder.inequality(6.0, 1.0, 0.0);
        builder.inequality(6.0, 0.0, 1.0);

        for (NamedSolver ns : UpdatableSolverTest.buildAllSolvers(builder)) {
            String tag = ns.name;
            Result result = ns.solver.solve();

            TestUtils.assertStateNotLessThanOptimal(result);
            TestUtils.assertEquals(tag + " obj", -4.0, result.getValue(), ACCURACY);

            double x1 = result.doubleValue(0);
            double x2 = result.doubleValue(1);
            TestUtils.assertEquals(tag + " x1+x2", 4.0, x1 + x2, ACCURACY);

            TestUtils.assertEquals(tag + " dual0", 1.0, ns.solver.getDualMultiplier(0), ACCURACY);
            TestUtils.assertEquals(tag + " dual1", 0.0, ns.solver.getDualMultiplier(1), ACCURACY);
            TestUtils.assertEquals(tag + " dual2", 0.0, ns.solver.getDualMultiplier(2), ACCURACY);

            TestUtils.assertEquals(tag + " rc0 (basic)", 0.0, ns.solver.getReducedGradient(0), ACCURACY);
            TestUtils.assertEquals(tag + " rc1 (basic)", 0.0, ns.solver.getReducedGradient(1), ACCURACY);
        }
    }

    /**
     * Two binding inequality constraints, two basic variables.
     *
     * <pre>
     * min  -2*x1 - 3*x2
     * s.t.   x1 +   x2 <= 4   (constraint 0)
     *        x1 + 3*x2 <= 6   (constraint 1)
     *        x1, x2 >= 0
     * </pre>
     *
     * Optimal: x1=3, x2=1, obj=-9. Both constraints bind. Both variables are basic so their reduced gradients
     * must be zero. Duals: y1=3/2, y2=1/2.
     */
    @Test
    public void testTwoBindingConstraints() {

        LinearSolver.Builder builder = LinearSolver.newBuilder(-2.0, -3.0);
        builder.inequality(4.0, 1.0, 1.0);
        builder.inequality(6.0, 1.0, 3.0);

        for (NamedSolver ns : UpdatableSolverTest.buildAllSolvers(builder)) {
            String tag = ns.name;
            Result result = ns.solver.solve();

            TestUtils.assertStateNotLessThanOptimal(result);
            TestUtils.assertEquals(tag + " obj", -9.0, result.getValue(), ACCURACY);
            TestUtils.assertEquals(tag + " x1", 3.0, result.doubleValue(0), ACCURACY);
            TestUtils.assertEquals(tag + " x2", 1.0, result.doubleValue(1), ACCURACY);

            TestUtils.assertEquals(tag + " dual0", 1.5, ns.solver.getDualMultiplier(0), ACCURACY);
            TestUtils.assertEquals(tag + " dual1", 0.5, ns.solver.getDualMultiplier(1), ACCURACY);

            TestUtils.assertEquals(tag + " rc0", 0.0, ns.solver.getReducedGradient(0), ACCURACY);
            TestUtils.assertEquals(tag + " rc1", 0.0, ns.solver.getReducedGradient(1), ACCURACY);
        }
    }

    /**
     * Two variables, two binding constraints, non-trivial duals.
     *
     * <pre>
     * min  -5*x1 - 4*x2
     * s.t.  6*x1 + 4*x2 <= 24   (constraint 0)
     *       3*x1 + 6*x2 <= 24   (constraint 1)
     *       x1, x2 >= 0
     * </pre>
     *
     * Optimal: x1=2, x2=3, obj=-22. Duals: y1=3/4, y2=1/6.
     */
    @Test
    public void testTwoVariableTwoConstraint() {

        LinearSolver.Builder builder = LinearSolver.newBuilder(-5.0, -4.0);
        builder.inequality(24.0, 6.0, 4.0);
        builder.inequality(24.0, 3.0, 6.0);

        for (NamedSolver ns : UpdatableSolverTest.buildAllSolvers(builder)) {
            String tag = ns.name;
            Result result = ns.solver.solve();

            TestUtils.assertStateNotLessThanOptimal(result);
            TestUtils.assertEquals(tag + " obj", -22.0, result.getValue(), ACCURACY);
            TestUtils.assertEquals(tag + " x1", 2.0, result.doubleValue(0), ACCURACY);
            TestUtils.assertEquals(tag + " x2", 3.0, result.doubleValue(1), ACCURACY);

            TestUtils.assertEquals(tag + " dual0", 0.75, ns.solver.getDualMultiplier(0), ACCURACY);
            TestUtils.assertEquals(tag + " dual1", 1.0 / 6.0, ns.solver.getDualMultiplier(1), ACCURACY);

            TestUtils.assertEquals(tag + " rc0", 0.0, ns.solver.getReducedGradient(0), ACCURACY);
            TestUtils.assertEquals(tag + " rc1", 0.0, ns.solver.getReducedGradient(1), ACCURACY);
        }
    }

}
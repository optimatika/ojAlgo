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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.Sense;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.structure.Access1D;
import org.ojalgo.type.context.NumberContext;
import org.ojalgo.type.keyvalue.KeyValue;

/**
 * Tests that the reduced costs reported by {@link Optimisation.Result#getReducedGradient()} match
 * known/expected values for a variety of small LP models, and that all configured solver integrations agree
 * on those values.
 * <p>
 * Subclasses (e.g. for HiGHS) add their solver integration to the list returned by
 * {@link #getAllIntegrations()}.
 */
public class ReducedCostsTest extends OptimisationLinearTests {

    private static final NumberContext ACCURACY = NumberContext.of(8);

    /**
     * Asserts the dual conditions for a primal-optimal LP solution:
     * <ul>
     * <li>For a variable strictly interior to its bounds (basic at non-bound value): rc ~ 0.</li>
     * <li>For a variable at its lower bound: rc >= 0 in MIN, rc <= 0 in MAX.</li>
     * <li>For a variable at its upper bound: rc <= 0 in MIN, rc >= 0 in MAX.</li>
     * <li>For a fixed variable (LB == UB): no constraint on rc.</li>
     * </ul>
     */
    private static void assertComplementarySlackness(final String id, final ExpressionsBasedModel model, final double[] rc, final Sense sense) {

        for (int i = 0; i < rc.length; i++) {
            Variable v = model.getVariable(i);

            if (v.isEqualityConstraint()) {
                continue; // fixed: rc unconstrained
            }

            BigDecimal value = v.getValue();
            BigDecimal lb = v.getLowerLimit();
            BigDecimal ub = v.getUpperLimit();
            double r = rc[i];

            boolean atLB = lb != null && value != null && ACCURACY.isZero(value.subtract(lb).doubleValue());
            boolean atUB = ub != null && value != null && ACCURACY.isZero(value.subtract(ub).doubleValue());

            if (!atLB && !atUB) {
                TestUtils.assertEquals(id + " rc[" + i + "] should be 0 (basic interior at x=" + value + ")", 0.0, r, ACCURACY);
            } else if (atLB) {
                double signed = sense == Sense.MIN ? r : -r;
                TestUtils.assertTrue(id + " rc[" + i + "]=" + r + " at LB violates dual feasibility for " + sense, signed >= -ACCURACY.error(0.0));
            } else { // atUB
                double signed = sense == Sense.MIN ? r : -r;
                TestUtils.assertTrue(id + " rc[" + i + "]=" + r + " at UB violates dual feasibility for " + sense, signed <= ACCURACY.error(0.0));
            }
        }
    }

    private static double[] extractRc(final Optimisation.Result result, final int nbVars, final String id) {

        Optional<Supplier<Access1D<?>>> rg = result.getReducedGradient();
        TestUtils.assertTrue(id + ": result.getReducedGradient() should be present", rg.isPresent());

        Access1D<?> g = rg.get().get();
        TestUtils.assertTrue(id + ": gradient must contain at least one entry per model variable", g.count() >= nbVars);

        double[] out = new double[nbVars];
        for (int i = 0; i < nbVars; i++) {
            out[i] = g.doubleValue(i);
        }
        return out;
    }

    /**
     * MIN with an equality constraint.
     *
     * <pre>
     * min  3x + 2y
     * s.t. x + y = 5
     *      x, y in [0, 10]
     * </pre>
     *
     * Optimum: x=0, y=5, value=10. Dual lambda on the equality satisfies rc_y = 0: 2 - 1 * lambda = 0 =>
     * lambda = 2. rc_x = 3 - 1 * 2 = +1.
     */
    @Test
    public void testEqualityConstraint() {

        Supplier<ExpressionsBasedModel> factory = () -> {
            ExpressionsBasedModel model = new ExpressionsBasedModel();
            Variable x = model.newVariable("x").lower(0).upper(10).weight(3);
            Variable y = model.newVariable("y").lower(0).upper(10).weight(2);
            model.newExpression("eq").set(x, 1).set(y, 1).level(5);
            return model;
        };

        this.doVerify("equalityConstraint", factory, Sense.MIN, 10.0, new double[] { 0.0, 5.0 }, new double[] { 1.0, 0.0 });
    }

    /**
     * MIN with a presolvable variable that gets fixed by the combination of variable bounds and a constraint.
     *
     * <pre>
     * min  x + 3y
     * s.t. x + y <= 10
     *      y     >= 5
     *      x in [0, 10], y in [0, 5]
     * </pre>
     *
     * The bound y <= 5 and constraint y >= 5 force y = 5. Optimum: x=0, y=5, value=15.
     * <p>
     * Implementations differ on the exact reduced cost assigned to a fixed variable, so this test only
     * asserts that all integrations agree among themselves (consistency), not against a fixed expected
     * vector.
     */
    @Test
    public void testFixedByPresolve() {

        Supplier<ExpressionsBasedModel> factory = () -> {
            ExpressionsBasedModel model = new ExpressionsBasedModel();
            Variable x = model.newVariable("x").lower(0).upper(10).weight(1);
            Variable y = model.newVariable("y").lower(0).upper(5).weight(3);
            model.newExpression("c1").set(x, 1).set(y, 1).upper(10);
            model.newExpression("c2").set(y, 1).lower(5);
            return model;
        };

        this.doVerifyConsistency("fixedByPresolve", factory, Sense.MIN, 15.0, new double[] { 0.0, 5.0 });
    }

    /**
     * Free variable (no bounds) with cost coefficient driving the optimum to the active constraint.
     *
     * <pre>
     * min  -y + x
     * s.t. y <= 3
     *      x = 0     (used only to anchor the otherwise-free x)
     *      x in (-inf, +inf), y in [-inf, 5]
     * </pre>
     *
     * Free x is fixed by the equality x = 0 (so its rc reconstructs to c_x = 1). y's UB tightens from 5 to 3
     * via presolve folding c1, putting y at the (tightened) UB with rc = -1 in MIN. value=-3.
     */
    @Test
    public void testFreeVariable() {

        Supplier<ExpressionsBasedModel> factory = () -> {
            ExpressionsBasedModel model = new ExpressionsBasedModel();
            Variable x = model.newVariable("x").weight(1); // unbounded
            Variable y = model.newVariable("y").upper(5).weight(-1); // unbounded below, capped above
            model.newExpression("c1").set(y, 1).upper(3);
            model.newExpression("eq").set(x, 1).level(0);
            return model;
        };

        this.doVerify("freeVariable", factory, Sense.MIN, -3.0, new double[] { 0.0, 3.0 }, new double[] { 1.0, -1.0 });
    }

    /**
     * MAX version of {@link #testMinInequalities}. Same degenerate optimum; bounded-variable rc flips sign
     * with MAX convention: rc = [0, 1] (y at UB => rc >= 0 in MAX).
     *
     * <pre>
     * max   x + 2y
     * s.t. x + y <= 4
     *      x     <= 3
     *      x, y in [0, 10]
     * </pre>
     */
    @Test
    public void testMaxInequalities() {

        Supplier<ExpressionsBasedModel> factory = () -> {
            ExpressionsBasedModel model = new ExpressionsBasedModel();
            Variable x = model.newVariable("x").lower(0).upper(10).weight(1);
            Variable y = model.newVariable("y").lower(0).upper(10).weight(2);
            model.newExpression("c1").set(x, 1).set(y, 1).upper(4);
            model.newExpression("c2").set(x, 1).upper(3);
            return model;
        };

        this.doVerify("maxInequalities", factory, Sense.MAX, 8.0, new double[] { 0.0, 4.0 }, new double[] { 0.0, 1.0 });
    }

    /**
     * MIN with inequalities. The optimum at (x=0, y=4) is degenerate: x sits at its LB and y at the
     * presolve-tightened UB (y <= 4 deducible from x >= 0 and x+y <= 4). The bounded-variable simplex and
     * HiGHS both place x basic-at-zero and y non-basic-at-UB, yielding rc = [0, -1] in MIN convention. The
     * split-variable primal tableau places x non-basic-at-LB and y basic-at-4, yielding rc = [1, 0] — equally
     * valid; relaxed checks accept both.
     *
     * <pre>
     * min  -x - 2y
     * s.t. x + y <= 4
     *      x     <= 3
     *      x, y in [0, 10]
     * </pre>
     */
    @Test
    public void testMinInequalities() {

        Supplier<ExpressionsBasedModel> factory = () -> {
            ExpressionsBasedModel model = new ExpressionsBasedModel();
            Variable x = model.newVariable("x").lower(0).upper(10).weight(-1);
            Variable y = model.newVariable("y").lower(0).upper(10).weight(-2);
            model.newExpression("c1").set(x, 1).set(y, 1).upper(4);
            model.newExpression("c2").set(x, 1).upper(3);
            return model;
        };

        this.doVerify("minInequalities", factory, Sense.MIN, -8.0, new double[] { 0.0, 4.0 }, new double[] { 0.0, -1.0 });
    }

    /**
     * Variable with negative bounds, exercising the split-variable (positive + negative) path on the primal
     * tableau and the bounded-variable path on the dual.
     *
     * <pre>
     * min  x + 2y
     * s.t. x + y >= 0
     *      x in [-5, 5], y in [-3, 3]
     * </pre>
     *
     * Optimum: maximise -x - 2y. y at LB=-3 (rc <= 0 in MAX of negated => >= 0 in MIN), then x: x + (-3) >= 0
     * => x >= 3. To minimise x: x = 3 (basic, c1 active). value = 3 - 6 = -3.
     */
    @Test
    public void testNegativeBounds() {

        Supplier<ExpressionsBasedModel> factory = () -> {
            ExpressionsBasedModel model = new ExpressionsBasedModel();
            Variable x = model.newVariable("x").lower(-5).upper(5).weight(1);
            Variable y = model.newVariable("y").lower(-3).upper(3).weight(2);
            model.newExpression("c1").set(x, 1).set(y, 1).lower(0);
            return model;
        };

        this.doVerify("negativeBounds", factory, Sense.MIN, -3.0, new double[] { 3.0, -3.0 }, new double[] { 0.0, 1.0 });
    }

    /**
     * Tall LP (many more constraints than variables). Three single-variable cap constraints get folded into
     * variable bounds by presolve; the budget constraint c1 binds at the optimum.
     *
     * <pre>
     * min  -3x - 2y - z
     * s.t. x + y + z <= 10  (c1: budget)
     *      x         <=  3  (c2)
     *      y         <=  4  (c3)
     *      x, y, z in [0, 10]
     * </pre>
     *
     * Optimum: x=3 (UB), y=4 (UB), z=3 (basic), value=-20. c1 active; rc_z = 0 (basic), rc_x = -2, rc_y = -1.
     */
    @Test
    public void testTallModel() {

        Supplier<ExpressionsBasedModel> factory = () -> {
            ExpressionsBasedModel model = new ExpressionsBasedModel();
            Variable x = model.newVariable("x").lower(0).upper(10).weight(-3);
            Variable y = model.newVariable("y").lower(0).upper(10).weight(-2);
            Variable z = model.newVariable("z").lower(0).upper(10).weight(-1);
            model.newExpression("c1").set(x, 1).set(y, 1).set(z, 1).upper(10);
            model.newExpression("c2").set(x, 1).upper(3);
            model.newExpression("c3").set(y, 1).upper(4);
            return model;
        };

        this.doVerify("tallModel", factory, Sense.MIN, -20.0, new double[] { 3.0, 4.0, 3.0 }, new double[] { -2.0, -1.0, 0.0 });
    }

    /**
     * MIN with a variable non-basic at its upper bound.
     *
     * <pre>
     * min  -3x - 2y
     * s.t. x + y <= 10
     *      x in [0, 4], y in [0, 10]
     * </pre>
     *
     * Optimum: x=4 (upper bound, non-basic), y=6 (interior, basic), value=-24. c1 active => lambda_1 = -2.
     * rc_x = -3 - 1 * (-2) = -1 (rc <= 0 at UB in MIN). rc_y = -2 - 1 * (-2) = 0.
     */
    @Test
    public void testVariableAtUpperBound() {

        Supplier<ExpressionsBasedModel> factory = () -> {
            ExpressionsBasedModel model = new ExpressionsBasedModel();
            Variable x = model.newVariable("x").lower(0).upper(4).weight(-3);
            Variable y = model.newVariable("y").lower(0).upper(10).weight(-2);
            model.newExpression("c1").set(x, 1).set(y, 1).upper(10);
            return model;
        };

        this.doVerify("variableAtUpperBound", factory, Sense.MIN, -24.0, new double[] { 4.0, 6.0 }, new double[] { -1.0, 0.0 });
    }

    /**
     * Wide LP (many more variables than constraints). Five items, a single budget cap. The optimum picks the
     * two most-negative-cost items at their UB and fills the budget with a partial third item.
     *
     * <pre>
     * min  -x1 - 2 x2 - 3 x3 - 4 x4 - 10 x5
     * s.t. x1 + x2 + x3 + x4 + x5 <= 8
     *      each x_i in [0, 5]
     * </pre>
     *
     * Optimum: x5=5 (UB), x4=3 (basic), x1=x2=x3=0 (LB). value=-62. lambda_c1=-4. rc_x1 = +3, rc_x2 = +2,
     * rc_x3 = +1, rc_x4 = 0, rc_x5 = -6.
     */
    @Test
    public void testWideModel() {

        Supplier<ExpressionsBasedModel> factory = () -> {
            ExpressionsBasedModel model = new ExpressionsBasedModel();
            double[] weights = { -1, -2, -3, -4, -10 };
            for (int i = 0; i < weights.length; i++) {
                model.newVariable("x" + (i + 1)).lower(0).upper(5).weight(weights[i]);
            }
            Expression budget = model.newExpression("budget").upper(8);
            for (int i = 0; i < weights.length; i++) {
                budget.set(i, 1);
            }
            return model;
        };

        this.doVerify("wideModel", factory, Sense.MIN, -62.0, new double[] { 0.0, 0.0, 0.0, 3.0, 5.0 }, new double[] { 3.0, 2.0, 1.0, 0.0, -6.0 });
    }

    /**
     * Runs the given model through both {@link #getStrictIntegrations()} (asserting that reduced costs match
     * {@code expectedRc} exactly) and {@link #getAllIntegrations()} (asserting only that reduced costs are
     * valid dual solutions — i.e. obey complementary slackness against the primal solution).
     * <p>
     * Primal and dual simplex paths can legitimately report different rc values for the same LP optimum when
     * a model variable sits at its variable bound: the bounded-variable (dual) simplex assigns rc to the
     * variable; the split-variable (primal) tableau assigns rc to the corresponding bound-as-slack. Both are
     * valid dual solutions, so the strict comparison is restricted to the bounded-variable paths and the
     * relaxed check uses complementary slackness for the rest.
     */
    private void doVerify(final String name, final Supplier<ExpressionsBasedModel> factory, final Sense sense, final double expectedValue,
            final double[] expectedSolution, final double[] expectedRc) {

        // Strict: exact rc match against bounded-variable paths (dual ojAlgo variants + HiGHS).
        for (KeyValue<String, ExpressionsBasedModel.Integration<LinearSolver>> entry : this.getStrictIntegrations()) {

            String id = name + "/" + entry.getKey() + " (strict)";
            ExpressionsBasedModel model = factory.get();
            Optimisation.Result result = sense == Sense.MIN ? model.minimise(entry.getValue()) : model.maximise(entry.getValue());

            TestUtils.assertStateNotLessThanOptimal(result);
            TestUtils.assertEquals(id + " value", expectedValue, result.getValue(), ACCURACY);
            for (int i = 0; i < expectedSolution.length; i++) {
                TestUtils.assertEquals(id + " x[" + i + "]", expectedSolution[i], result.doubleValue(i), ACCURACY);
            }
            double[] rc = ReducedCostsTest.extractRc(result, expectedSolution.length, id);
            for (int i = 0; i < expectedRc.length; i++) {
                TestUtils.assertEquals(id + " rc[" + i + "]", expectedRc[i], rc[i], ACCURACY);
            }
        }

        // Relaxed: every integration must produce an rc that obeys complementary slackness.
        for (KeyValue<String, ExpressionsBasedModel.Integration<LinearSolver>> entry : this.getAllIntegrations()) {

            String id = name + "/" + entry.getKey() + " (relaxed)";
            ExpressionsBasedModel model = factory.get();
            Optimisation.Result result = sense == Sense.MIN ? model.minimise(entry.getValue()) : model.maximise(entry.getValue());

            TestUtils.assertStateNotLessThanOptimal(result);
            TestUtils.assertEquals(id + " value", expectedValue, result.getValue(), ACCURACY);
            for (int i = 0; i < expectedSolution.length; i++) {
                TestUtils.assertEquals(id + " x[" + i + "]", expectedSolution[i], result.doubleValue(i), ACCURACY);
            }
            double[] rc = ReducedCostsTest.extractRc(result, expectedSolution.length, id);
            ReducedCostsTest.assertComplementarySlackness(id, model, rc, sense);
        }
    }

    /**
     * Like {@link #doVerify} but does not assert a fixed expected rc vector — only that every integration
     * produces the same rc values. Used for cases where the exact rc is implementation-defined (e.g.
     * fixed-by-presolve variables).
     */
    private void doVerifyConsistency(final String name, final Supplier<ExpressionsBasedModel> factory, final Sense sense, final double expectedValue,
            final double[] expectedSolution) {

        double[] referenceRc = null;
        String referenceId = null;

        for (KeyValue<String, ExpressionsBasedModel.Integration<LinearSolver>> entry : this.getAllIntegrations()) {

            String id = name + "/" + entry.getKey();
            ExpressionsBasedModel.Integration<LinearSolver> integration = entry.getValue();

            ExpressionsBasedModel model = factory.get();
            Optimisation.Result result = sense == Sense.MIN ? model.minimise(integration) : model.maximise(integration);

            TestUtils.assertStateNotLessThanOptimal(result);
            TestUtils.assertEquals(id + " value", expectedValue, result.getValue(), ACCURACY);
            for (int i = 0; i < expectedSolution.length; i++) {
                TestUtils.assertEquals(id + " x[" + i + "]", expectedSolution[i], result.doubleValue(i), ACCURACY);
            }

            double[] rc = ReducedCostsTest.extractRc(result, expectedSolution.length, id);

            if (referenceRc == null) {
                referenceRc = rc;
                referenceId = id;
            } else {
                for (int i = 0; i < rc.length; i++) {
                    TestUtils.assertEquals(id + " rc[" + i + "] vs " + referenceId, referenceRc[i], rc[i], ACCURACY);
                }
            }
        }
    }

    /**
     * Returns the list of solver integrations to exercise. The base class returns all standard ojAlgo
     * {@link LinearSolver} variants (primal/dual, sparse/dense). Subclasses may override to add or replace
     * integrations.
     */
    protected List<KeyValue<String, ExpressionsBasedModel.Integration<LinearSolver>>> getAllIntegrations() {

        return OptimisationLinearTests.INTEGRATIONS;
    }

    /**
     * Returns the subset of {@link #getAllIntegrations()} that uses the bounded-variable simplex
     * representation (the newer dual path). These paths assign rc to variables at their variable bounds —
     * matching HiGHS' convention — and are subject to strict (exact-value) checks. Subclasses may extend this
     * list (e.g. to add HiGHS).
     */
    protected List<KeyValue<String, ExpressionsBasedModel.Integration<LinearSolver>>> getStrictIntegrations() {

        List<KeyValue<String, ExpressionsBasedModel.Integration<LinearSolver>>> retVal = new ArrayList<>();
        for (KeyValue<String, ExpressionsBasedModel.Integration<LinearSolver>> entry : OptimisationLinearTests.INTEGRATIONS) {
            if (entry.getKey().contains("Dual-")) {
                retVal.add(entry);
            }
        }
        return retVal;
    }
}

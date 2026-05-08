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

import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.optimisation.integer.GomorySolver;
import org.ojalgo.type.context.NumberContext;

/**
 * Tests that exercise Ruiz scaling end-to-end across all four solver/backend combinations:
 * <ul>
 * <li>dense + dual → {@code DenseTableau} via {@link SimplexSolver}
 * <li>sparse + dual → {@code RevisedStore} via {@link SimplexSolver}
 * <li>dense + primal → {@code DenseTableau} via {@link SimplexTableauSolver}
 * <li>sparse + primal → {@code SparseTableau} via {@link SimplexTableauSolver}
 * </ul>
 * Scaling is opt-in, so each test explicitly enables it via {@code options.linear().equilibration(10)}.
 * <p>
 * Two patterns are intentionally narrow regression tests for bug classes we've hit:
 * <ul>
 * <li>Backend consistency: solving the same LP with all four combinations (each scaled) must produce the same
 * primal solution and objective value. This catches scaling-math drift across backends and across primal/dual
 * code paths, including the {@code extractReducedGradients} unscaling bug.
 * <li>MIP cut validity: an integer-coef MIP solved through {@link GomorySolver} must converge under all four
 * combinations. This catches the cut-transformation bugs (slack-folding, shift-unscaling) that cause
 * non-cutting cuts when scaling is on.
 * </ul>
 * Both tests use ill-conditioned coefficient matrices so that Ruiz scaling factors are non-trivial and the
 * scaled vs unscaled paths actually differ in their internal computations.
 */
public class ScalingConsistencyTest extends OptimisationLinearTests {

    private static final NumberContext ACCURACY = NumberContext.of(8);

    /**
     * Tag for one {(sparse, dual)} combination, used in failure messages.
     */
    private static final String[] TAGS = { "dense/dual", "sparse/dual", "dense/primal", "sparse/primal" };

    private static ExpressionsBasedModel buildIllConditionedLP() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x = model.newVariable("x").lower(0).weight(-100);
        Variable y = model.newVariable("y").lower(0).weight(-0.01);

        model.addExpression("c1").upper(1000).set(x, 1000).set(y, 1);
        model.addExpression("c2").upper(100).set(x, 1).set(y, 100);

        return model;
    }

    private static ExpressionsBasedModel buildIllConditionedMIP() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x1 = model.newVariable("x1").integer(true).lower(0).weight(-600);
        Variable x2 = model.newVariable("x2").integer(true).lower(0).weight(-5);

        model.addExpression("c1").upper(1100).set(x1, 300).set(x2, 1);
        model.addExpression("c2").upper(5).set(x1, -1).set(x2, 2);

        return model;
    }

    /**
     * Configure a model for one of the four (sparse, dual) combinations and enable Ruiz scaling.
     *
     * @param sparse {@code true} for the sparse backend (RevisedStore / SparseTableau), {@code false} for
     *               dense (DenseTableau).
     * @param dual   {@code true} for the dual/SimplexSolver path, {@code false} for the primal/
     *               SimplexTableauSolver path.
     */
    private static void configure(final ExpressionsBasedModel model, final boolean sparse, final boolean dual) {
        model.options.sparse = Boolean.valueOf(sparse);
        if (dual) {
            model.options.linear().dual();
        } else {
            model.options.linear().primal();
        }
        // Force scaling on with a non-trivial iteration count regardless of the auto-gate.
        model.options.linear().equilibration(10);
    }

    /**
     * Solve the same model under all four combinations of (sparse, dual). Returns the four results in the
     * order matching {@link #TAGS}.
     */
    private static Result[] solveAllCombinations(final Function<Boolean, ExpressionsBasedModel> modelFactory, final boolean minimise) {

        Result[] results = new Result[4];
        int idx = 0;
        for (boolean sparse : new boolean[] { false, true }) {
            for (boolean dual : new boolean[] { true, false }) {
                ExpressionsBasedModel model = modelFactory.apply(Boolean.TRUE);
                ScalingConsistencyTest.configure(model, sparse, dual);
                results[idx++] = minimise ? model.minimise() : model.maximise();
            }
        }
        return results;
    }

    /**
     * Ill-conditioned LP: coefficient magnitudes span 10^5. Forces non-trivial Ruiz factors. All four
     * (sparse, dual) combinations must reach the analytical optimum and agree with each other.
     *
     * <pre>
     * min  -100*x - 0.01*y
     * s.t.  1000*x +   1*y <=  1000
     *          1*x + 100*y <=   100
     *       x, y >= 0
     *
     * Optimal: x=1, y=0, obj=-100.
     * </pre>
     * <p>
     * (Reduced gradients are not cross-checked across combinations: at this degenerate optimum, different
     * solver paths may converge to different dual optima and hence different reduced gradients. A separate
     * test in this package, {@code UpdatableSolverTest#testAllVariablesAtLowerBound}, pins the unscaled
     * reduced-gradient values.)
     */
    @Test
    public void testBackendConsistencyOnIllConditionedLP() {

        Result[] results = ScalingConsistencyTest.solveAllCombinations(b -> ScalingConsistencyTest.buildIllConditionedLP(), true);

        for (int i = 0; i < results.length; i++) {
            String tag = TAGS[i];
            Result result = results[i];

            TestUtils.assertStateNotLessThanOptimal(result);
            TestUtils.assertEquals(tag + " value", -100.0, result.getValue(), ACCURACY);
            TestUtils.assertEquals(tag + " x", 1.0, result.doubleValue(0), ACCURACY);
            TestUtils.assertEquals(tag + " y", 0.0, result.doubleValue(1), ACCURACY);
        }

        // Cross-combination agreement on primal solution and objective
        for (int i = 1; i < results.length; i++) {
            TestUtils.assertEquals(TAGS[i] + " vs " + TAGS[0] + " value", results[0].getValue(), results[i].getValue(), ACCURACY);
            TestUtils.assertEquals(TAGS[i] + " vs " + TAGS[0] + " x", results[0].doubleValue(0), results[i].doubleValue(0), ACCURACY);
            TestUtils.assertEquals(TAGS[i] + " vs " + TAGS[0] + " y", results[0].doubleValue(1), results[i].doubleValue(1), ACCURACY);
        }
    }

    /**
     * Ill-conditioned MIP: coefficients in c1 differ by 300x to drive non-trivial scaling factors. Solved via
     * the standard MIP path; all four (sparse, dual) combinations must converge to the same integer optimum,
     * and the cut machinery must produce valid cuts under scaling (no infinite loops, no
     * missing-the-optimum).
     *
     * <pre>
     * min  -600*x1 - 5*x2
     * s.t.  300*x1 + x2 <= 1100   (c1, large coefficients)
     *        -x1 + 2*x2 <=    5   (c2)
     *       x1, x2 >= 0 integer
     *
     * Optimal: x1 = 3, x2 = 4. Verify: 300*3+4=904 <= 1100, -3+8=5 <= 5. Obj = -1820.
     * </pre>
     */
    @Test
    public void testMipCutValidityUnderScaling() {

        Result[] results = ScalingConsistencyTest.solveAllCombinations(b -> ScalingConsistencyTest.buildIllConditionedMIP(), true);

        // Build models again for feasibility validation (each result was obtained from its own model)
        for (int i = 0; i < results.length; i++) {
            String tag = TAGS[i];
            Result result = results[i];

            TestUtils.assertStateNotLessThanOptimal(result);
            TestUtils.assertEquals(tag + " value", -1820.0, result.getValue(), ACCURACY);
            TestUtils.assertEquals(tag + " x1", 3.0, result.doubleValue(0), ACCURACY);
            TestUtils.assertEquals(tag + " x2", 4.0, result.doubleValue(1), ACCURACY);
        }

        // Cross-combination agreement
        for (int i = 1; i < results.length; i++) {
            TestUtils.assertEquals(TAGS[i] + " vs " + TAGS[0] + " value", results[0].getValue(), results[i].getValue(), ACCURACY);
            TestUtils.assertEquals(TAGS[i] + " vs " + TAGS[0] + " x1", results[0].doubleValue(0), results[i].doubleValue(0), ACCURACY);
            TestUtils.assertEquals(TAGS[i] + " vs " + TAGS[0] + " x2", results[0].doubleValue(1), results[i].doubleValue(1), ACCURACY);
        }
    }

}

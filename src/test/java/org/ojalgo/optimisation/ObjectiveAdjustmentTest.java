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
package org.ojalgo.optimisation;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.type.context.NumberContext;

/**
 * Covers the objective-adjustment plumbing that lets {@link ExpressionsBasedModel.Integration} integrations
 * (in particular the LP simplex builders) report a correct objective value even when the solver only sees a
 * reduced problem. Two contributions get stashed on the model and added back in
 * {@link ExpressionsBasedModel.Integration#expandFreeToFull(Result, ExpressionsBasedModel,
 * org.ojalgo.array.DenseArray.Factory, java.util.Optional, Optimisation.Sense) expandFreeToFull}:
 * <ol>
 * <li>The model's {@linkplain ExpressionsBasedModel#getObjectiveConstant() objective constant} (e.g. an
 * MPS-loaded objective offset).
 * <li>The contribution {@code Σ c_v · x_v} of variables that the presolver fixes.
 * </ol>
 * Both used to be silently dropped because {@code SimplexSolver.build} / {@code SimplexTableauSolver.build}
 * push only the compensated objective's <em>linear</em> coefficients into the simplex's {@code c} vector and
 * never the expression's constant.
 */
public class ObjectiveAdjustmentTest extends OptimisationTests {

    private static final NumberContext ACCURACY = NumberContext.of(7, 6);

    /**
     * Build a model whose objective is a single variable {@code x}, where the presolver fixes {@code x}
     * indirectly via the equality {@code x + y == 5} combined with a user-fixed {@code y == 2}. After
     * presolve, the LP that gets handed to the simplex has no objective coefficients on any free variable —
     * its job is purely to verify feasibility on the remaining free variable {@code z}. The model-level
     * objective value is, however, exactly {@code x's} fixed value (3).
     * <p>
     * The extra free variable {@code z} (with constraint {@code z >= 1}) is there to keep
     * {@link ExpressionsBasedModel#isFixed()} false, so the {@link IntermediateSolver}'s all-fixed
     * short-circuit doesn't bypass the simplex.
     */
    private static ExpressionsBasedModel buildSingleVarObjectiveModel() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x = model.newVariable("x").lower(0).upper(100).weight(1);
        Variable y = model.newVariable("y").level(2);
        Variable z = model.newVariable("z").lower(0).upper(10);

        // After compensate with y=2, this becomes x == 3, which the ZERO_ONE_TWO presolver folds into a
        // tightened bound on x — fixing it.
        model.newExpression("link").set(x, 1).set(y, 1).level(5);

        // Keeps z referenced and free.
        model.newExpression("zlo").set(z, 1).lower(1);

        return model;
    }

    private static void assertObjectiveAdjustmentApplied(final Result result, final double expected, final ExpressionsBasedModel model) {

        TestUtils.assertStateNotLessThanOptimal(result);
        TestUtils.assertEquals(expected, result.getValue(), ACCURACY);
        // Cross-check: re-evaluating the model objective on the returned solution must match the reported
        // value (this is the canonical definition of "correct objective value").
        TestUtils.assertEquals(expected, model.objective().evaluate(result).doubleValue(), ACCURACY);
    }

    /**
     * Combination case: MPS-style {@code objectiveConstant} <em>and</em> a presolve-fixed objective
     * variable. Exercises the slow path in {@link Expression#compensate(java.util.Set) compensate}, where
     * the adjustment is the sum of both contributions.
     */
    @Test
    public void testObjectiveConstantAndFixedVariable() {

        ExpressionsBasedModel model = ObjectiveAdjustmentTest.buildSingleVarObjectiveModel();
        // Stash a non-zero objective constant on top of the single-variable objective. The reported value
        // must include both the fixed x contribution (3) and this constant (7.5).
        model.newExpression("c0").weight(1).addObjectiveConstant(BigDecimal.valueOf(7.5));

        Result result = model.minimise();

        ObjectiveAdjustmentTest.assertObjectiveAdjustmentApplied(result, 3.0 + 7.5, model);
    }

    /**
     * MPS-style {@code objectiveConstant} with no fixed variables — exercises the fast-path branch in
     * {@link Expression#compensate(java.util.Set) compensate} which now has to write the model's
     * objective adjustment.
     */
    @Test
    public void testObjectiveConstantOnlyMinimise() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable z = model.newVariable("z").lower(0).upper(5).weight(1);
        // Keep z bounded away from its lower bound so the simplex's reported value is non-zero — this way
        // a missing adjustment wouldn't be hidden by a coincidental zero.
        model.newExpression("zlo").set(z, 1).lower(2);

        // Equivalent to an MPS file's objective offset.
        model.newExpression("c0").weight(1).addObjectiveConstant(BigDecimal.valueOf(7.5));

        Result result = model.minimise();

        ObjectiveAdjustmentTest.assertObjectiveAdjustmentApplied(result, 2.0 + 7.5, model);
    }

    /**
     * Maximisation variant — tests that the model-sense adjustment is added <em>after</em>
     * {@code expandFreeToFull}'s {@code withNegatedValue()} sign flip, so the answer comes out in model
     * sense regardless of solver direction.
     */
    @Test
    public void testSingleVarObjectiveFixedByPresolveMaximise() {

        ExpressionsBasedModel model = ObjectiveAdjustmentTest.buildSingleVarObjectiveModel();

        Result result = model.maximise();

        // x is pinned to 3 by presolve, so max x = 3 regardless of what z does.
        ObjectiveAdjustmentTest.assertObjectiveAdjustmentApplied(result, 3.0, model);
    }

    /**
     * Core scenario for the LP-relaxation-in-choco use case: the objective is just one variable, the
     * presolver fixes that variable via constraint propagation, the simplex sees an objective ≡ 0 on the
     * remaining free variables, yet the model-level objective value must come back as the fixed value
     * itself.
     */
    @Test
    public void testSingleVarObjectiveFixedByPresolveMinimise() {

        ExpressionsBasedModel model = ObjectiveAdjustmentTest.buildSingleVarObjectiveModel();

        Result result = model.minimise();

        ObjectiveAdjustmentTest.assertObjectiveAdjustmentApplied(result, 3.0, model);
    }

}

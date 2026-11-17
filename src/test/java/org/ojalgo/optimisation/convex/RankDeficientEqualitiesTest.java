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
package org.ojalgo.optimisation.convex;

import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.ExpressionsBasedModel.Integration;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.optimisation.Optimisation.State;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.type.context.NumberContext;

/**
 * Equality constraints that are linearly dependent – a duplicated constraint, one that is a linear
 * combination of others, or one that becomes so when a variable is fixed (by branching, for instance). If
 * they are consistent, the problem is the same as without the dependent constraint. If not, the problem is
 * infeasible.
 * <p>
 * Consistent, the solver for equality constraints only used to report INFEASIBLE, the active set solvers
 * FAILED, and the null space projection variant OPTIMAL with a constraint violated.
 * <p>
 * All models are min (x-1)<sup>2</sup> + (y-2)<sup>2</sup> + (z-3)<sup>2</sup> + (w-1)<sup>2</sup> - 15 when
 * x + y = 1 and y + z = 1, plus a third (dependent) equality constraint. With y = t that's 3 t<sup>2</sup> +
 * 8 - 15, and the solution is x = z = 1 - t, w = 1, with t = 0 free and t = 0.5 bounded (y >= 0.5, and the
 * other variables within [-10, 10]). The models are built without presolve – the problem the solvers get is
 * the one stated.
 */
public class RankDeficientEqualitiesTest extends OptimisationConvexTests {

    private static final NumberContext ACCURACY = NumberContext.of(7);

    private static final Integration<?>[] ACTIVE_SET = { ConfiguredIntegration.ASET_PLAIN_D, ConfiguredIntegration.ASET_PLAIN_S,
            ConfiguredIntegration.ASET_PROJ_D, ConfiguredIntegration.ASET_PROJ_S };

    private static void doTestConsistent(final Function<Boolean, ExpressionsBasedModel> factory) {

        for (boolean bounded : new boolean[] { false, true }) {

            double t = bounded ? 0.5 : 0.0;

            for (int v = 0; v < ACTIVE_SET.length; v++) {

                String id = (bounded ? "Bounded" : "Free") + " ACTIVE_SET[" + v + "]";

                ExpressionsBasedModel model = factory.apply(bounded);

                Result result = model.minimise(ACTIVE_SET[v]);

                TestUtils.assertTrue(id + " " + result, result.getState().isOptimal());
                TestUtils.assertTrue(id, model.validate(result));

                TestUtils.assertEquals(id, 1.0 - t, result.doubleValue(0), ACCURACY);
                TestUtils.assertEquals(id, t, result.doubleValue(1), ACCURACY);
                TestUtils.assertEquals(id, 1.0 - t, result.doubleValue(2), ACCURACY);
                TestUtils.assertEquals(id, 1.0, result.doubleValue(3), ACCURACY);

                TestUtils.assertEquals(id, 3.0 * t * t - 7.0, result.getValue(), ACCURACY);
            }
        }
    }

    private static void doTestInconsistent(final Function<Boolean, ExpressionsBasedModel> factory) {

        for (boolean bounded : new boolean[] { false, true }) {

            for (int v = 0; v < ACTIVE_SET.length; v++) {

                String id = (bounded ? "Bounded" : "Free") + " ACTIVE_SET[" + v + "]";

                ExpressionsBasedModel model = factory.apply(bounded);

                Result result = model.minimise(ACTIVE_SET[v]);

                TestUtils.assertEquals(id, State.INFEASIBLE, result.getState());
            }
        }
    }

    /**
     * x, y, z and w (in that order), the objective and the first two equality constraints.
     */
    private static ExpressionsBasedModel newModel(final boolean bounded) {

        Optimisation.Environment environment = Optimisation.newEnvironment();
        environment.clearPresolvers();

        ExpressionsBasedModel model = environment.newModel();

        Variable x = model.newVariable("x");
        Variable y = model.newVariable("y");
        Variable z = model.newVariable("z");
        Variable w = model.newVariable("w");

        if (bounded) {
            x.lower(-10).upper(10);
            y.lower(0.5).upper(10);
            z.lower(-10).upper(10);
            w.lower(-10).upper(10);
        }

        model.newExpression("Objective").weight(1).set(x, x, 1).set(y, y, 1).set(z, z, 1).set(w, w, 1).set(x, -2).set(y, -4).set(z, -6).set(w, -2);

        model.newExpression("First").level(1).set(x, 1).set(y, 1);
        model.newExpression("Second").level(1).set(y, 1).set(z, 1);

        return model;
    }

    /**
     * Third: x + y = rhs, the same as the first when rhs = 1
     */
    private static ExpressionsBasedModel newModelWithDuplicate(final boolean bounded, final double rhs) {

        ExpressionsBasedModel model = RankDeficientEqualitiesTest.newModel(bounded);

        Variable x = model.getVariable(0);
        Variable y = model.getVariable(1);

        model.newExpression("Third").level(rhs).set(x, 1).set(y, 1);

        return model;
    }

    /**
     * Third: x + 2 y + z + q = 2.5 with q fixed – the sum of the first and the second when q = 0.5
     */
    private static ExpressionsBasedModel newModelWithFixedVariable(final boolean bounded, final double level) {

        ExpressionsBasedModel model = RankDeficientEqualitiesTest.newModel(bounded);

        Variable x = model.getVariable(0);
        Variable y = model.getVariable(1);
        Variable z = model.getVariable(2);
        Variable q = model.newVariable("q").level(level);

        model.newExpression("Third").level(2.5).set(x, 1).set(y, 2).set(z, 1).set(q, 1);

        return model;
    }

    /**
     * Third: x + 2 y + z = rhs, the sum of the first and the second when rhs = 2
     */
    private static ExpressionsBasedModel newModelWithLinearCombination(final boolean bounded, final double rhs) {

        ExpressionsBasedModel model = RankDeficientEqualitiesTest.newModel(bounded);

        Variable x = model.getVariable(0);
        Variable y = model.getVariable(1);
        Variable z = model.getVariable(2);

        model.newExpression("Third").level(rhs).set(x, 1).set(y, 2).set(z, 1);

        return model;
    }

    @Test
    public void testDuplicate() {
        RankDeficientEqualitiesTest.doTestConsistent(bounded -> RankDeficientEqualitiesTest.newModelWithDuplicate(bounded, 1.0));
    }

    @Test
    public void testDuplicateInconsistent() {
        RankDeficientEqualitiesTest.doTestInconsistent(bounded -> RankDeficientEqualitiesTest.newModelWithDuplicate(bounded, 1.5));
    }

    @Test
    public void testFixedVariable() {
        RankDeficientEqualitiesTest.doTestConsistent(bounded -> RankDeficientEqualitiesTest.newModelWithFixedVariable(bounded, 0.5));
    }

    @Test
    public void testFixedVariableInconsistent() {
        RankDeficientEqualitiesTest.doTestInconsistent(bounded -> RankDeficientEqualitiesTest.newModelWithFixedVariable(bounded, 0.0));
    }

    @Test
    public void testLinearCombination() {
        RankDeficientEqualitiesTest.doTestConsistent(bounded -> RankDeficientEqualitiesTest.newModelWithLinearCombination(bounded, 2.0));
    }

    @Test
    public void testLinearCombinationInconsistent() {
        RankDeficientEqualitiesTest.doTestInconsistent(bounded -> RankDeficientEqualitiesTest.newModelWithLinearCombination(bounded, 3.0));
    }

}

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
package org.ojalgo.optimisation;

import java.util.Random;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.optimisation.convex.ConvexSolver;
import org.ojalgo.optimisation.linear.LinearSolver;
import org.ojalgo.type.context.NumberContext;

/**
 * An integration wrapped by
 * {@link ExpressionsBasedModel.Integration#withOptionsModifier(java.util.function.Consumer)} (or
 * {@link ExpressionsBasedModel.Integration#withCapabilityPredicate(java.util.function.Predicate)}) did not
 * delegate {@link ExpressionsBasedModel.Integration#getIndexInSolver(ExpressionsBasedModel, Variable)}. The
 * default, the model index, is not the solver index when there are fixed variables. The branch-and-bound
 * algorithm updates node bounds in place, and fixes variables by reduced cost, using that index. With a
 * wrapped LP integration in the environment, about 1 in 8 small random MILPs got a wrong "optimal" value, and
 * some threw ArrayIndexOutOfBoundsException.
 */
public class ConfiguredIntegrationTest extends OptimisationTests {

    /**
     * 8 integer variables in [0, 5], with positive objective weights, and 3 random less-than-or-equal
     * constraints with positive coefficients – to be maximised.
     */
    private static ExpressionsBasedModel newRandomModel(final Optimisation.Environment environment, final long seed) {

        Random random = new Random(seed);

        ExpressionsBasedModel model = environment.newModel();

        Variable[] variables = new Variable[8];
        for (int j = 0; j < variables.length; j++) {
            variables[j] = model.newVariable("x" + j).integer().lower(0).upper(5).weight(1 + random.nextInt(9));
        }

        for (int i = 0; i < 3; i++) {
            Expression constraint = model.newExpression("c" + i).upper(10 + random.nextInt(20));
            for (Variable variable : variables) {
                constraint.set(variable, 1 + random.nextInt(5) + random.nextDouble());
            }
        }

        return model;
    }

    /**
     * The wrapped integration maps model variables to solver indices exactly as the original: the fixed
     * variable is not in the solver, and the others are shifted one position.
     */
    @Test
    public void testIndexInSolver() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable fixed = model.newVariable("Fixed").level(1).weight(1);
        Variable first = model.newVariable("First").lower(0).upper(1).weight(2);
        Variable second = model.newVariable("Second").lower(0).upper(1).weight(3);

        model.newExpression("Sum").upper(1.5).set(fixed, 1).set(first, 1).set(second, 1);

        ExpressionsBasedModel.Integration<?>[] originals = { LinearSolver.INTEGRATION, ConvexSolver.INTEGRATION };

        for (ExpressionsBasedModel.Integration<?> original : originals) {

            ExpressionsBasedModel.Integration<?> modified = original.withOptionsModifier(options -> {});
            ExpressionsBasedModel.Integration<?> predicated = original.withCapabilityPredicate(m -> true);

            for (Variable variable : model.getVariables()) {

                int expected = original.getIndexInSolver(model, variable);

                TestUtils.assertEquals(variable.getName(), expected, modified.getIndexInSolver(model, variable));
                TestUtils.assertEquals(variable.getName(), expected, predicated.getIndexInSolver(model, variable));
            }
        }
    }

    /**
     * Small random MILPs solved with a wrapped LP integration in the environment have the same optimal value
     * as with the default integrations.
     */
    @Test
    public void testMixedIntegerWithConfiguredIntegration() {

        Optimisation.Environment plain = Optimisation.newEnvironment();

        Optimisation.Environment configured = Optimisation.newEnvironment();
        configured.addIntegration(LinearSolver.INTEGRATION.withOptionsModifier(options -> {}));

        for (long seed = 0L; seed < 20L; seed++) {

            String id = "Seed " + seed;

            Optimisation.Result expected = ConfiguredIntegrationTest.newRandomModel(plain, seed).maximise();

            ExpressionsBasedModel model = ConfiguredIntegrationTest.newRandomModel(configured, seed);
            Optimisation.Result actual = model.maximise();

            TestUtils.assertStateNotLessThanOptimal(actual);
            TestUtils.assertTrue(id, model.validate(actual));
            TestUtils.assertEquals(id, expected.getValue(), actual.getValue(), NumberContext.of(8));
        }
    }

}

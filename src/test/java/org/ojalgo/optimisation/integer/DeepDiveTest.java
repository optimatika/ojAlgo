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
package org.ojalgo.optimisation.integer;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;

/**
 * Models with very deep dives: the depth must not be limited by the size of the stack, and a dive has to stop
 * when the time is up.
 */
public class DeepDiveTest extends OptimisationIntegerTests {

    /**
     * Cuts would solve it at the root
     */
    private static final IntegerStrategy.ConfigurableStrategy NO_CUTS = IntegerStrategy.DEFAULT
            .withCutConfiguration(IntegerStrategy.DEFAULT.getCutConfiguration().withTypes());

    /**
     * 2x - 2y + 2z - s = 1 with x, y and z integer, and s between 0 and 0.5, has no integer solution. But the
     * LP relaxation is feasible, with one of them fractional, until they have come to their limits: each
     * branching moves one of them a step, so the dive is as deep as the limit. (With only x and y bound
     * propagation takes many steps at a time, but not with 3 variables.)
     */
    private static ExpressionsBasedModel newModel(final int limit) {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x = model.newVariable("x").lower(0).upper(limit).integer().weight(1);
        Variable y = model.newVariable("y").lower(0).upper(limit).integer();
        Variable z = model.newVariable("z").lower(0).upper(limit).integer();
        Variable s = model.newVariable("s").lower(0).upper(0.5);

        Expression link = model.newExpression("link").level(1);
        link.set(x, 2);
        link.set(y, -2);
        link.set(z, 2);
        link.set(s, -1);

        model.options.parallelism(1);
        model.options.integer(NO_CUTS);

        return model;
    }

    /**
     * A dive that is 12 000 nodes deep
     */
    @Test
    public void testDepth() {

        int[] depth = new int[1];

        ExpressionsBasedModel model = DeepDiveTest.newModel(12_000);
        model.options.integer(NO_CUTS.withModelStrategyFactory((m, strategy) -> new ModelStrategy.AbstractStrategy(m, strategy) {

            @Override
            protected void onNodeSolved(final NodeKey key, final Optimisation.Result child, final double childObj, final boolean minimisation) {
                depth[0] = Math.max(depth[0], key.depth);
                super.onNodeSolved(key, child, childObj, minimisation);
            }

        }));

        Optimisation.Result result = model.minimise();

        TestUtils.assertEquals(Optimisation.State.INFEASIBLE, result.getState());
        TestUtils.assertTrue("Depth " + depth[0], depth[0] > 11_000);
    }

    /**
     * A dive that would take far too long, with a time limit
     */
    @Test
    public void testTimeLimit() {

        ExpressionsBasedModel model = DeepDiveTest.newModel(1_000_000_000);
        model.options.time_abort = 500L;

        long start = System.nanoTime();
        Optimisation.Result result = model.minimise();
        long millis = (System.nanoTime() - start) / 1_000_000L;

        TestUtils.assertEquals(Optimisation.State.FAILED, result.getState()); // Nothing found, and nothing proven
        TestUtils.assertTrue("Took " + millis + " ms, with a limit of 500", millis < 10_000L);
    }

}

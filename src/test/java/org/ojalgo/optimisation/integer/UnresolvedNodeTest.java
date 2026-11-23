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
 * A node that can't be solved, or branched, leaves its subtree unexplored – it does not stop the whole
 * search. The result is optimal only if that subtree could not contain a better solution.
 * <p>
 * The models: two binary items in a knapsack, {@code b1} and {@code b2}, and an integer variable {@code x}
 * with {@code 2x + b2 + y = 6000000001}, where {@code y >= 0} is continuous and costs 1 per unit. In the LP
 * relaxation {@code b1 = 1} and {@code b2} is fractional, and the dive continues with {@code b2 = 0} first.
 * There {@code x = 3000000000.5}: fractional, but outside the int range, so it can't be branched on – the
 * node is left unresolved, with its LP value 5 as bound. With {@code b2 = 1} there is an integer solution,
 * {@code b1 = 0} and {@code x = 3000000000}. Before, the unresolved node stopped the search, with no solution
 * found (FAILED). No cuts, as those would change the LP relaxation.
 */
public class UnresolvedNodeTest extends OptimisationIntegerTests {

    private static ExpressionsBasedModel newModel(final double valueB2) {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable b1 = model.newVariable("b1").binary().weight(5);
        Variable b2 = model.newVariable("b2").binary().weight(valueB2);
        Variable x = model.newVariable("x").lower(0).integer();
        Variable y = model.newVariable("y").lower(0).weight(-1);

        Expression knapsack = model.newExpression("knapsack").upper(4);
        knapsack.set(b1, 3);
        knapsack.set(b2, 3.5);

        Expression link = model.newExpression("link").level(6000000001L);
        link.set(x, 2);
        link.set(b2, 1);
        link.set(y, 1);

        model.options.parallelism(1);
        model.options.integer(IntegerStrategy.DEFAULT.withCutConfiguration(IntegerStrategy.DEFAULT.getCutConfiguration().withTypes()));

        return model;
    }

    /**
     * The unresolved subtree has bound 5, the best solution found is 5: that subtree can't contain anything
     * better, so the solution is optimal.
     */
    @Test
    public void testOptimalDespiteUnresolvedNode() {

        ExpressionsBasedModel model = UnresolvedNodeTest.newModel(5);

        Optimisation.Result result = model.maximise();

        TestUtils.assertStateNotLessThanOptimal(result);
        TestUtils.assertEquals(5.0, result.getValue(), 1E-9);
        TestUtils.assertEquals(0.0, result.doubleValue(0), 1E-9);
        TestUtils.assertEquals(1.0, result.doubleValue(1), 1E-9);
        TestUtils.assertEquals(3000000000.0, result.doubleValue(2), 1E-9);
    }

    /**
     * The unresolved subtree has bound 5, the best solution found is 4.5: that subtree could contain
     * something better, so the solution is only feasible.
     */
    @Test
    public void testSearchContinuesAfterUnresolvedNode() {

        ExpressionsBasedModel model = UnresolvedNodeTest.newModel(4.5);

        Optimisation.Result result = model.maximise();

        TestUtils.assertEquals(Optimisation.State.FEASIBLE, result.getState());
        TestUtils.assertEquals(4.5, result.getValue(), 1E-9);
        TestUtils.assertEquals(0.0, result.doubleValue(0), 1E-9);
        TestUtils.assertEquals(1.0, result.doubleValue(1), 1E-9);
        TestUtils.assertEquals(3000000000.0, result.doubleValue(2), 1E-9);
    }

}

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

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.ExpressionsBasedModel.EntityMap;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;

/**
 * The slack of a row with integer coefficients on integer variables is integer valued only if the row's
 * limit is an integer too. Getting this wrong makes the GMI cut generator treat a continuous slack as
 * integer and produce invalid cuts (an objective cutoff row {@code c'x <= 34.000001} was the original
 * case).
 */
public class SlackIntegralityTest extends OptimisationLinearTests {

    /**
     * Builds the solver, then moves the limit of "ROW" to {@code limit} (building the solver tightens the
     * model, which rounds the limit of an integer row; a limit changed afterwards, like an objective cutoff
     * that is updated as incumbents are found, is what the integer mask must cope with).
     *
     * @return Whether the slack of the expression named "ROW" is marked integer
     */
    private static boolean rowSlackIsInteger(final ExpressionsBasedModel model, final double limit) {

        SimplexTableau tableau = SimplexTableauSolver.build(model, DenseTableau::new);
        SimplexTableauSolver solver = tableau.newSimplexTableauSolver(new Optimisation.Options());
        EntityMap map = solver.getEntityMap().get();

        model.getExpression("ROW").upper(limit);

        boolean[] integers = map.integers(model);
        int nbModelVars = map.countModelVariables();

        for (int i = 0; i < map.countSlackVariables(); i++) {
            if ("ROW".equals(map.getSlack(i).getKey().getName())) {
                return integers[nbModelVars + i];
            }
        }

        throw new IllegalStateException("No slack for ROW");
    }

    private static ExpressionsBasedModel makeModel() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x = model.newVariable("X").integer().lower(0).upper(10).weight(1);
        Variable y = model.newVariable("Y").integer().lower(0).upper(10).weight(1);

        model.newExpression("ROW").set(x, 2).set(y, 3).upper(9);

        return model;
    }

    @Test
    public void testIntegerLimit() {
        TestUtils.assertTrue(SlackIntegralityTest.rowSlackIsInteger(SlackIntegralityTest.makeModel(), 7));
    }

    @Test
    public void testNonIntegerLimit() {
        TestUtils.assertFalse(SlackIntegralityTest.rowSlackIsInteger(SlackIntegralityTest.makeModel(), 7.5));
    }

    /**
     * Tightening an already analysed integer row must round a limit that was changed after the analysis.
     */
    @Test
    public void testTightenAfterLimitChange() {

        ExpressionsBasedModel model = SlackIntegralityTest.makeModel();

        TestUtils.assertTrue(model.getExpression("ROW").isInteger());

        model.getExpression("ROW").upper(7.5).tighten();

        TestUtils.assertEquals(7, model.getExpression("ROW").getUpperLimit().intValueExact());
    }

}

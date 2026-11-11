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
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;

/**
 * The integer variables of an integer solution have whole number values with scale 0: 75, not 75.0, as
 * {@link java.math.BigDecimal#toPlainString()} and the result's {@code toString()} print them. The integer
 * solution can come from either of two places in {@link IntegerSolver} – snapping a node's integer variables
 * to integers, or rounding them – and both are covered.
 */
public class IntegerValueScaleTest extends OptimisationIntegerTests {

    private static void assertWholeNumbers(final ExpressionsBasedModel model, final Variable chairs, final Variable tables) {

        Optimisation.Result result = model.maximise();

        TestUtils.assertEquals(Optimisation.State.OPTIMAL, result.getState());
        TestUtils.assertEquals(7375.0, result.doubleValue());

        TestUtils.assertEquals("75", chairs.getValue().toPlainString());
        TestUtils.assertEquals("50", tables.getValue().toPlainString());
        TestUtils.assertEquals("75", result.get(0).toPlainString());
        TestUtils.assertEquals("50", result.get(1).toPlainString());
    }

    /**
     * The midpoints of the variables' bounds, 56 and 40, are feasible and become the first incumbent. The
     * optimum then comes from snapping the root node's integer variables to integers.
     */
    @Test
    public void testSnappedIntegers() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable chairs = model.newIntegerVariable("Chairs").lower(0);
        Variable tables = model.newIntegerVariable("Tables").lower(0);

        model.newConstraint("Machine hours").upper(400).set(chairs, 2).set(tables, 5);
        model.newConstraint("Wood").upper(450).set(chairs, 4).set(tables, 3);

        model.getObjective().set(chairs, 45).set(tables, 80);

        IntegerValueScaleTest.assertWholeNumbers(model, chairs, tables);
    }

    /**
     * With the midpoints of the bounds infeasible there is no first incumbent, and the optimum comes from
     * rounding the root node's solution.
     */
    @Test
    public void testRoundedIntegers() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable chairs = model.newIntegerVariable("Chairs").lower(0);
        Variable tables = model.newIntegerVariable("Tables").lower(0);

        model.newConstraint("Machine hours").upper(400).set(chairs, 2).set(tables, 5);
        model.newConstraint("Wood").upper(450).set(chairs, 4).set(tables, 3);
        model.newConstraint("Minimum").lower(120).set(chairs, 1).set(tables, 1);

        model.getObjective().set(chairs, 45).set(tables, 80);

        IntegerValueScaleTest.assertWholeNumbers(model, chairs, tables);
    }

}

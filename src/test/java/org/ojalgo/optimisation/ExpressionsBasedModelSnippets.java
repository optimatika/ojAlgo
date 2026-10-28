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

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;

/**
 * The examples in the Javadoc of {@link ExpressionsBasedModel}, included with {@code {@snippet}}. The code
 * between the region markers is what the Javadoc shows; the assertions after it check that it still does what
 * the comments say.
 */
public class ExpressionsBasedModelSnippets {

    @Test
    public void testInteger() {

        // @start region="integer"
        ExpressionsBasedModel model = new ExpressionsBasedModel();

        // Maximise 5a + 4b, with a and b whole numbers
        Variable a = model.newVariable("a").lower(0).integer().weight(5);
        Variable b = model.newVariable("b").lower(0).integer().weight(4);

        // subject to 6a + 4b <= 24 and a + 2b <= 6
        model.newExpression("first").upper(24).set(a, 6).set(b, 4);
        model.newExpression("second").upper(6).set(a, 1).set(b, 2);

        Optimisation.Result result = model.maximise(); // a = 4, b = 0 and 20 (without integer(): 3, 1.5 and 21)
        // @end

        TestUtils.assertEquals(Optimisation.State.OPTIMAL, result.getState());
        TestUtils.assertEquals(20.0, result.getValue());
        TestUtils.assertEquals(4.0, result.doubleValue(0));
        TestUtils.assertEquals(0.0, result.doubleValue(1));

        a.integer(false);
        b.integer(false);
        Optimisation.Result relaxed = model.maximise();
        TestUtils.assertEquals(21.0, relaxed.getValue());
        TestUtils.assertEquals(3.0, relaxed.doubleValue(0));
        TestUtils.assertEquals(1.5, relaxed.doubleValue(1));
    }

    @Test
    public void testIntroduction() {

        // @start region="introduction"
        ExpressionsBasedModel model = new ExpressionsBasedModel();

        // Maximise 3x + 2y
        Variable x = model.newVariable("x").lower(0).upper(3).weight(3);
        Variable y = model.newVariable("y").lower(0).weight(2);

        // subject to x + y <= 4 and x + 3y <= 6
        model.newExpression("first").upper(4).set(x, 1).set(y, 1);
        model.newExpression("second").upper(6).set(x, 1).set(y, 3);

        Optimisation.Result result = model.maximise();

        Optimisation.State state = result.getState(); // OPTIMAL
        double value = result.getValue(); // 11
        double xValue = result.doubleValue(0); // 3 – the variables in the order they were created
        // @end

        TestUtils.assertEquals(Optimisation.State.OPTIMAL, state);
        TestUtils.assertEquals(11.0, value);
        TestUtils.assertEquals(3.0, xValue);
        TestUtils.assertEquals(1.0, result.doubleValue(1));
    }

    @Test
    public void testQuadratic() {

        // @start region="quadratic"
        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x = model.newVariable("x");
        Variable y = model.newVariable("y");

        // Minimise x² + y² - 2x - 4y: quadratic and linear terms in the same expression
        model.newExpression("objective").weight(1).set(x, x, 1).set(y, y, 1).set(x, -2).set(y, -4);

        // subject to x + y <= 2
        model.newExpression("limit").upper(2).set(x, 1).set(y, 1);

        Optimisation.Result result = model.minimise(); // x = 0.5, y = 1.5 and -4.5
        // @end

        TestUtils.assertEquals(Optimisation.State.OPTIMAL, result.getState());
        TestUtils.assertEquals(-4.5, result.getValue());
        TestUtils.assertEquals(0.5, result.doubleValue(0));
        TestUtils.assertEquals(1.5, result.doubleValue(1));
    }

}

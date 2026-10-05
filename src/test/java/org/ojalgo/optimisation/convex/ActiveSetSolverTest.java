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

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.ExpressionsBasedModel.Integration;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.type.context.NumberContext;

public class ActiveSetSolverTest extends OptimisationConvexTests {

    private static final NumberContext ACCURACY = NumberContext.of(7);

    /**
     * min 1/2 x0<sup>2</sup> + 1/2 q x1<sup>2</sup> - x0 - x1, with x1 <= 1E6 (active) - the solution is x0 = 1,
     * x1 = 1E6. The active set solver starts from the LP solution x0 = 0, x1 = 1E6. The step to x0 = 1 used to
     * be ignored as "too small" because it was compared to the largest component of the solution (1E6).
     */
    @Test
    public void testStepInSmallVariableWhenOtherIsLarge() {

        for (Integration<?> integration : VARIANTS) {
            for (double q : new double[] { 1E-9, 0.0 }) {

                ExpressionsBasedModel model = new ExpressionsBasedModel();
                Variable x0 = model.newVariable("x0").weight(-1.0);
                Variable x1 = model.newVariable("x1").weight(-1.0).upper(1E6);
                model.newExpression("Q").weight(0.5).set(x0, x0, 1.0).set(x1, x1, q);

                Optimisation.Result result = model.minimise(integration);

                TestUtils.assertStateNotLessThanOptimal(result);
                TestUtils.assertEquals(1.0, result.doubleValue(0), ACCURACY);
                TestUtils.assertEquals(1E6, result.doubleValue(1), ACCURACY);
            }
        }
    }

}

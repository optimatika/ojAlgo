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
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.type.context.NumberContext;

/**
 * Tests GMI cut generation with non-basic integer variables at their upper bounds.
 * <p>
 * Model: max 5*x1 + 4*x2 + 3*x3 subject to 2*x1 + 3*x2 + x3 <= 4, all binary.
 * <p>
 * LP relaxation optimal: x1=1 (at UB), x2=2/3 (fractional, basic), x3=1 (at UB). MIP optimal: x1=1, x2=0,
 * x3=1, obj=8.
 * <p>
 * The GMI cut from x2's fractional value involves non-basic variables x1 and x3 at their upper bounds. Before
 * the fix, RevisedStore used {@code uppers[j] <= 0} for negVar determination, which is wrong for unshifted
 * variables (binary UB=1 gives negVar=false when the variable IS at its upper bound).
 */
public class GMICutDiagnosticTest extends OptimisationIntegerTests {

    private static final NumberContext ACCURACY = NumberContext.of(8);

    private static void doTest(final ExpressionsBasedModel model) {

        Result expected = Result.of(8.0, Optimisation.State.OPTIMAL, 1, 0, 1);

        model.options.validate = true;

        Result result = model.maximise();

        TestUtils.assertStateNotLessThanOptimal(result);
        TestUtils.assertResult(expected, result, ACCURACY);
    }

    static ExpressionsBasedModel makeModel() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x1 = model.newVariable("x1").binary().weight(5);
        Variable x2 = model.newVariable("x2").binary().weight(4);
        Variable x3 = model.newVariable("x3").binary().weight(3);

        model.addExpression("c1").upper(4).set(x1, 2).set(x2, 3).set(x3, 1);

        return model;
    }

    @Test
    public void testDualDense() {
        ExpressionsBasedModel model = GMICutDiagnosticTest.makeModel();
        model.options.linear().dual();
        model.options.sparse = Boolean.FALSE;
        GMICutDiagnosticTest.doTest(model);
    }

    @Test
    public void testDualSparse() {
        ExpressionsBasedModel model = GMICutDiagnosticTest.makeModel();
        model.options.linear().dual();
        model.options.sparse = Boolean.TRUE;
        GMICutDiagnosticTest.doTest(model);
    }

    @Test
    public void testPrimalDense() {
        ExpressionsBasedModel model = GMICutDiagnosticTest.makeModel();
        model.options.linear().primal();
        model.options.sparse = Boolean.FALSE;
        GMICutDiagnosticTest.doTest(model);
    }

    @Test
    public void testPrimalSparse() {
        ExpressionsBasedModel model = GMICutDiagnosticTest.makeModel();
        model.options.linear().primal();
        model.options.sparse = Boolean.TRUE;
        GMICutDiagnosticTest.doTest(model);
    }

}

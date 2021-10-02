/*
 * Copyright 1997-2021 Optimatika
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

import static org.ojalgo.function.constant.BigMath.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.optimisation.Optimisation.State;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.type.context.NumberContext;

/**
 * http://www.ee.ucla.edu/ee236a/lectures/intlp.pdf (UCLAee236aCase.pdf)
 *
 * @see #testRelaxedNodeP09()
 * @author apete
 */
public class UCLAee236aCase extends OptimisationIntegerTests {

    private static final Optimisation.Result EXPECTED_MIP = Result.of(-10.0, State.OPTIMAL, 2.0, 2.0);
    /**
     * Solutions in the pdf are given with 2 decimals (3 digits).
     */
    private static final NumberContext PRECISION = NumberContext.of(3, 2);

    private static ExpressionsBasedModel makeOriginalRootModel() {

        Variable[] tmpVariables = new Variable[] { new Variable("X1").lower(ZERO).weight(TWO.negate()).integer(true),
                new Variable("X2").lower(ZERO).weight(THREE.negate()).integer(true) };

        ExpressionsBasedModel retVal = new ExpressionsBasedModel(tmpVariables);
        retVal.setMinimisation();

        Expression exprC1 = retVal.addExpression("C1");
        for (int i = 0; i < retVal.countVariables(); i++) {
            exprC1.set(i, new BigDecimal[] { TWO.multiply(NINTH), QUARTER }[i]);
        }
        exprC1.upper(ONE);

        Expression exprC2 = retVal.addExpression("C2");
        for (int i = 0; i < retVal.countVariables(); i++) {
            exprC2.set(i, new BigDecimal[] { SEVENTH, THIRD }[i]);
        }
        exprC2.upper(ONE);

        return retVal;
    }

    /**
     * http://www.ee.ucla.edu/ee236a/lectures/intlp.pdf
     */
    @Test
    public void testFullMIP() {

        Result expected = EXPECTED_MIP;

        ExpressionsBasedModel model = UCLAee236aCase.makeOriginalRootModel();

        if (DEBUG) {
            model.options.debug(IntegerSolver.class);
        }

        Optimisation.Result result = model.minimise();

        TestUtils.assertStateAndSolution(expected, result, PRECISION);
        TestUtils.assertEquals(expected.getValue(), result.getValue(), PRECISION);
    }

    @Test
    public void testRelaxedButConstrainedToOptimalMIP() {

        Result expected = EXPECTED_MIP;

        ExpressionsBasedModel model = UCLAee236aCase.makeOriginalRootModel();
        model.relax(false);
        model.getVariable(0).lower(TWO);
        model.getVariable(0).upper(TWO);
        model.getVariable(1).lower(TWO);
        model.getVariable(1).upper(TWO);

        Optimisation.Result result = model.minimise();

        TestUtils.assertStateAndSolution(expected, result, PRECISION);
        TestUtils.assertEquals(expected.getValue(), result.getValue(), PRECISION);
    }

    /**
     * P0
     */
    @Test
    public void testRelaxedNodeP00() {

        Result expected = Result.of(-10.56, Optimisation.State.OPTIMAL, 2.17, 2.07);

        ExpressionsBasedModel model = UCLAee236aCase.makeOriginalRootModel();
        model.relax(false);

        Optimisation.Result result = model.minimise();

        TestUtils.assertStateAndSolution(expected, result, PRECISION);
        TestUtils.assertEquals(expected.getValue(), result.getValue(), PRECISION);
    }

    /**
     * P1
     */
    @Test
    public void testRelaxedNodeP01() {

        Result expected = Result.of(-10.43, Optimisation.State.OPTIMAL, 2.00, 2.14);

        ExpressionsBasedModel model = UCLAee236aCase.makeOriginalRootModel();
        model.relax(false);
        model.getVariable(0).upper(TWO);

        Optimisation.Result result = model.minimise();

        TestUtils.assertStateAndSolution(expected, result, PRECISION);
        TestUtils.assertEquals(expected.getValue(), result.getValue(), PRECISION);
    }

    /**
     * P2
     */
    @Test
    public void testRelaxedNodeP02() {

        Optimisation.Result expected = Result.of(-10.0, State.OPTIMAL, 3.00, 1.33);

        ExpressionsBasedModel model = UCLAee236aCase.makeOriginalRootModel();
        model.relax(false);
        model.getVariable(0).lower(THREE);

        Optimisation.Result result = model.minimise();

        TestUtils.assertStateAndSolution(expected, result, PRECISION);
        TestUtils.assertEquals(expected.getValue(), result.getValue(), PRECISION);
    }

    /**
     * P3
     */
    @Test
    public void testRelaxedNodeP03() {

        Result expected = Result.of(-10.00, Optimisation.State.OPTIMAL, 2.00, 2.00);

        ExpressionsBasedModel model = UCLAee236aCase.makeOriginalRootModel();
        model.relax(false);
        model.getVariable(0).upper(TWO);
        model.getVariable(1).upper(TWO);

        Optimisation.Result result = model.minimise();

        TestUtils.assertStateAndSolution(expected, result, PRECISION);
        TestUtils.assertEquals(expected.getValue(), result.getValue(), PRECISION);
    }

    /**
     * P4
     */
    @Test
    public void testRelaxedNodeP04() {

        Result expected = Result.of(-9.00, Optimisation.State.OPTIMAL, 0.00, 3.00);

        ExpressionsBasedModel model = UCLAee236aCase.makeOriginalRootModel();
        model.relax(false);
        model.getVariable(0).upper(TWO);
        model.getVariable(1).lower(THREE);

        Optimisation.Result result = model.minimise();

        TestUtils.assertStateAndSolution(expected, result, PRECISION);
        TestUtils.assertEquals(expected.getValue(), result.getValue(), PRECISION);
    }

    /**
     * P5
     */
    @Test
    public void testRelaxedNodeP05() {

        Result expected = Result.of(-9.75, Optimisation.State.OPTIMAL, 3.38, 1.00);

        ExpressionsBasedModel model = UCLAee236aCase.makeOriginalRootModel();
        model.relax(false);
        model.getVariable(0).lower(THREE);
        model.getVariable(1).upper(ONE);

        Optimisation.Result result = model.minimise();

        TestUtils.assertStateAndSolution(expected, result, PRECISION);
        TestUtils.assertEquals(expected.getValue(), result.getValue(), PRECISION);
    }

    /**
     * P6
     */
    @Test
    public void testRelaxedNodeP06() {

        Result expected = Result.of(Double.NaN, State.INFEASIBLE, 0.0, 0.0);

        ExpressionsBasedModel model = UCLAee236aCase.makeOriginalRootModel();
        model.relax(false);
        model.getVariable(0).lower(THREE);
        model.getVariable(1).lower(TWO);

        Optimisation.Result result = model.minimise();

        TestUtils.assertEquals(expected.getState(), result.getState());
    }

    /**
     * P7
     */
    @Test
    public void testRelaxedNodeP07() {

        Result expected = Result.of(-9.00, Optimisation.State.OPTIMAL, 3.00, 1.00);

        ExpressionsBasedModel model = UCLAee236aCase.makeOriginalRootModel();
        model.relax(false);
        model.getVariable(0).lower(THREE);
        model.getVariable(1).upper(ONE);
        model.getVariable(0).upper(THREE);

        Optimisation.Result result = model.minimise();

        TestUtils.assertStateAndSolution(expected, result, PRECISION);
        TestUtils.assertEquals(expected.getValue(), result.getValue(), PRECISION);
    }

    /**
     * P8
     */
    @Test
    public void testRelaxedNodeP08() {

        Result expected = Result.of(-9.33, Optimisation.State.OPTIMAL, 4.00, 0.44);

        ExpressionsBasedModel model = UCLAee236aCase.makeOriginalRootModel();
        model.relax(false);
        model.getVariable(0).lower(THREE);
        model.getVariable(1).upper(ONE);
        model.getVariable(0).lower(FOUR);

        Optimisation.Result result = model.minimise();

        TestUtils.assertStateAndSolution(expected, result, PRECISION);
        TestUtils.assertEquals(expected.getValue(), result.getValue(), PRECISION);
    }

    /**
     * P9
     */
    @Test
    public void testRelaxedNodeP09() {

        Result expected = Result.of(-9.00, Optimisation.State.OPTIMAL, 4.50, 0.00);

        ExpressionsBasedModel model = UCLAee236aCase.makeOriginalRootModel();
        model.relax(false);
        model.getVariable(0).lower(THREE);
        model.getVariable(1).upper(ONE);
        model.getVariable(0).lower(FOUR);
        model.getVariable(1).upper(ZERO);

        Optimisation.Result result = model.minimise();

        TestUtils.assertStateAndSolution(expected, result, PRECISION);
        TestUtils.assertEquals(expected.getValue(), result.getValue(), PRECISION);
    }

    /**
     * P10
     */
    @Test
    public void testRelaxedNodeP10() {

        Result expected = Result.of(Double.NaN, State.INFEASIBLE, 0.0, 0.0);

        ExpressionsBasedModel model = UCLAee236aCase.makeOriginalRootModel();
        model.relax(false);
        model.getVariable(0).lower(THREE);
        model.getVariable(1).upper(ONE);
        model.getVariable(0).lower(FOUR);
        model.getVariable(1).lower(ONE);

        Optimisation.Result result = model.minimise();

        TestUtils.assertEquals(expected.getState(), result.getState());
    }

    /**
     * P11
     */
    @Test
    public void testRelaxedNodeP11() {

        Result expected = Result.of(-8.00, State.OPTIMAL, 4.00, 0.00);

        ExpressionsBasedModel model = UCLAee236aCase.makeOriginalRootModel();
        model.relax(false);
        model.getVariable(0).lower(THREE);
        model.getVariable(1).upper(ONE);
        model.getVariable(0).lower(FOUR);
        model.getVariable(1).upper(ZERO);
        model.getVariable(0).upper(FOUR);

        Optimisation.Result result = model.minimise();

        TestUtils.assertStateAndSolution(expected, result, PRECISION);
        TestUtils.assertEquals(expected.getValue(), result.getValue(), PRECISION);
    }

    /**
     * P12
     */
    @Test
    public void testRelaxedNodeP12() {

        Result expected = Result.of(Double.NaN, State.INFEASIBLE, 0.0, 0.0);

        ExpressionsBasedModel model = UCLAee236aCase.makeOriginalRootModel();
        model.relax(false);
        model.getVariable(0).lower(THREE);
        model.getVariable(1).upper(ONE);
        model.getVariable(0).lower(FOUR);
        model.getVariable(1).upper(ZERO);
        model.getVariable(0).lower(FIVE);

        Optimisation.Result result = model.minimise();

        TestUtils.assertEquals(expected.getState(), result.getState());
    }

}

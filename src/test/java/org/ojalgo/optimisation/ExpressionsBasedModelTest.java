/*
 * Copyright 1997-2022 Optimatika
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

import static org.ojalgo.function.constant.BigMath.*;

import java.math.BigDecimal;
import java.util.Collections;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.function.constant.BigMath;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.type.context.NumberContext;

public class ExpressionsBasedModelTest extends OptimisationTests {

    /**
     * https://github.com/optimatika/ojAlgo-extensions/issues/3 <br>
     * "compensating" didn't work because of an incorrectly used stream - did peek(...) instead of map(...).
     */
    @Test
    public void testCompensate() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();
        model.addVariable(Variable.make("X1").lower(0).upper(5).weight(1));
        model.addVariable(Variable.make("X2").lower(0).upper(5).weight(1));
        model.addVariable(Variable.make("X3").level(4).weight(1));

        Expression expression = model.addExpression("MAX5").upper(5);
        expression.set(0, 1).set(1, 1).set(2, 1);

        Optimisation.Result result = model.maximise();

        TestUtils.assertTrue(model.validate(result));

        TestUtils.assertTrue(result.getState().isOptimal());

        TestUtils.assertEquals(5.0, result.getValue(), PrimitiveMath.MACHINE_EPSILON);

        TestUtils.assertEquals(1.0, result.doubleValue(0) + result.doubleValue(1), PrimitiveMath.MACHINE_EPSILON);
        TestUtils.assertEquals(4.0, result.doubleValue(2), PrimitiveMath.MACHINE_EPSILON);
    }

    /**
     * https://github.com/optimatika/ojAlgo-extensions/issues/1 Reported as a problem with the CPLEX
     * integration
     */
    @Test
    public void testFixedVariables() {

        ExpressionsBasedModel test = new ExpressionsBasedModel();
        test.addVariable(Variable.make("V1").level(0.5));
        test.addVariable(Variable.make("V2").lower(0).upper(5).weight(2));
        test.addVariable(Variable.make("V3").lower(0).upper(1).weight(1));
        Expression expressions = test.addExpression("E1").lower(0).upper(2);
        expressions.set(1, 1).set(2, 1);

        Optimisation.Result minResult = test.minimise();
        TestUtils.assertTrue(test.validate(minResult));
        TestUtils.assertEquals(Optimisation.State.OPTIMAL, minResult.getState());
        TestUtils.assertEquals(0.0, minResult.getValue(), PrimitiveMath.MACHINE_EPSILON);
        TestUtils.assertEquals(0.5, minResult.doubleValue(0), PrimitiveMath.MACHINE_EPSILON);
        TestUtils.assertEquals(0.0, minResult.doubleValue(1), PrimitiveMath.MACHINE_EPSILON);
        TestUtils.assertEquals(0.0, minResult.doubleValue(2), PrimitiveMath.MACHINE_EPSILON);

        Optimisation.Result maxResult = test.maximise();
        TestUtils.assertTrue(test.validate(maxResult));
        TestUtils.assertEquals(Optimisation.State.OPTIMAL, maxResult.getState());
        TestUtils.assertEquals(4.0, maxResult.getValue(), PrimitiveMath.MACHINE_EPSILON);
        TestUtils.assertEquals(0.5, maxResult.doubleValue(0), PrimitiveMath.MACHINE_EPSILON);
        TestUtils.assertEquals(2.0, maxResult.doubleValue(1), PrimitiveMath.MACHINE_EPSILON);
        TestUtils.assertEquals(0.0, maxResult.doubleValue(2), PrimitiveMath.MACHINE_EPSILON);
    }

    /**
     * https://github.com/optimatika/ojAlgo-extensions/issues/2 <br>
     * Reported as a problem with the Gurobi integration. The problem is unbounded. Many solvers do not return
     * a feasible solution in such case - even if they could.
     */
    @Test
    @Tag("unstable")
    public void testGitHubIssue2() {

        Variable[] objective = new Variable[] { new Variable("X1").weight(0.8), new Variable("X2").weight(0.2), new Variable("X3").weight(0.7),
                new Variable("X4").weight(0.3), new Variable("X5").weight(0.6), new Variable("X6").weight(0.4) };

        ExpressionsBasedModel model = new ExpressionsBasedModel(objective);

        model.addExpression("C1").set(0, 1).set(2, 1).set(4, 1).level(23);
        model.addExpression("C2").set(1, 1).set(3, 1).set(5, 1).level(23);
        model.addExpression("C3").set(0, 1).lower(10);
        model.addExpression("C4").set(2, 1).lower(8);
        model.addExpression("C5").set(4, 1).lower(5);

        Optimisation.Result result = model.maximise();

        // A valid solution of 25.8 can be produced with:
        //     X1=10, X2=0, X3=8, X4=0, X5=5, X6=23
        TestUtils.assertEquals(25.8, result.getValue(), 0.001);
    }

    @Test
    public void testIntegerRounding() {

        ExpressionsBasedModel ebm = new ExpressionsBasedModel();
        Variable var1 = ebm.addVariable("X1");
        Variable var2 = ebm.addVariable("X2");

        Expression exp1 = ebm.addExpression("Exp1").set(var1, 3).set(var2, 6).upper(13.5);
        exp1.doIntegerRounding();
        TestUtils.assertEquals(BigMath.ONE, exp1.get(var1));
        TestUtils.assertEquals(BigMath.TWO, exp1.get(var2));
        TestUtils.assertEquals(BigMath.FOUR, exp1.getUpperLimit());

        Expression exp2 = ebm.addExpression("Exp2").set(var1, 30).set(var2, 60).upper(135);
        exp2.doIntegerRounding();
        TestUtils.assertEquals(BigMath.ONE, exp2.get(var1));
        TestUtils.assertEquals(BigMath.TWO, exp2.get(var2));
        TestUtils.assertEquals(BigMath.FOUR, exp2.getUpperLimit());

        Expression exp3 = ebm.addExpression("Exp3").set(var1, 0.3).set(var2, 0.6).upper(1.35);
        exp3.doIntegerRounding();
        TestUtils.assertEquals(BigMath.ONE, exp3.get(var1));
        TestUtils.assertEquals(BigMath.TWO, exp3.get(var2));
        TestUtils.assertEquals(BigMath.FOUR, exp3.getUpperLimit());

        Expression exp4 = ebm.addExpression("Exp4").set(var1, 0.3).set(var2, 60).upper(1.35);
        exp4.doIntegerRounding();
        TestUtils.assertEquals(BigMath.ONE, exp4.get(var1));
        TestUtils.assertEquals(BigMath.TWO.multiply(BigMath.HUNDRED), exp4.get(var2));
        TestUtils.assertEquals(BigMath.FOUR, exp4.getUpperLimit());

        Expression exp5 = ebm.addExpression("Exp5").set(var1, 30).set(var2, 0.6).upper(1.35);
        exp5.doIntegerRounding(); // How to get 50, 1 and 2?
        TestUtils.assertEquals(BigMath.ONE.multiply(BigMath.HUNDRED), exp5.get(var1)); // 50
        TestUtils.assertEquals(BigMath.TWO, exp5.get(var2)); // 1
        TestUtils.assertEquals(BigMath.FOUR, exp5.getUpperLimit()); // 2
    }

    @Test
    public void testMPStestprob() {

        Variable tmpXONE = new Variable("XONE").weight(ONE).lower(ZERO).upper(FOUR);
        Variable tmpYTWO = new Variable("YTWO").weight(FOUR).lower(NEG).upper(ONE);
        Variable tmpZTHREE = new Variable("ZTHREE").weight(NINE).lower(ZERO).upper(null);

        Variable[] tmpVariables = new Variable[] { tmpXONE, tmpYTWO, tmpZTHREE };

        ExpressionsBasedModel tmpModel = new ExpressionsBasedModel(tmpVariables);

        BigDecimal[] tmpFactorsLIM1 = new BigDecimal[] { ONE, ONE, ZERO };
        Expression tmpLIM1 = tmpModel.addExpression("LIM1");
        for (int v = 0; v < tmpVariables.length; v++) {
            tmpLIM1.set(v, tmpFactorsLIM1[v]);
        }
        tmpLIM1.upper(FIVE.add(TENTH));

        BigDecimal[] tmpFactorsLIM2 = new BigDecimal[] { ONE, ZERO, ONE };
        Expression tmpLIM2 = tmpModel.addExpression("LIM2");
        for (int v = 0; v < tmpVariables.length; v++) {
            tmpLIM2.set(v, tmpFactorsLIM2[v]);
        }
        tmpLIM2.lower(TEN.add(TENTH));

        BigDecimal[] tmpFactorsMYEQN = new BigDecimal[] { ZERO, ONE.negate(), ONE };
        Expression tmpMYEQN = tmpModel.addExpression("MYEQN");
        for (int v = 0; v < tmpVariables.length; v++) {
            tmpMYEQN.set(v, tmpFactorsMYEQN[v]);
        }
        tmpMYEQN.level(SEVEN);

        TestUtils.assertTrue(tmpModel.validate());

        Result tmpMinRes = tmpModel.minimise();
        Result tmpMaxRes = tmpModel.maximise();

        if (OptimisationTests.DEBUG) {
            BasicLogger.debug(tmpMinRes);
            BasicLogger.debug(tmpMaxRes);
        }

        TestUtils.assertTrue(tmpModel.validate(tmpMinRes));
        TestUtils.assertTrue(tmpModel.validate(tmpMaxRes));

        tmpXONE.integer(true);
        tmpYTWO.integer(true);
        tmpZTHREE.integer(true);

        TestUtils.assertFalse(tmpModel.validate(tmpMinRes)); // Not integer solution
        TestUtils.assertTrue(tmpModel.validate(tmpMaxRes)); // Integer solution

        tmpYTWO.lower(ONE).upper(NEG);

        TestUtils.assertFalse(tmpModel.validate());
    }

    @Test
    public void testPresolverCase2() {

        NumberContext precision = NumberContext.of(14, 12);
        Collections.emptySet();
        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable varX = model.addVariable("X").lower(ONE);
        Variable varY = model.addVariable("Y");
        Variable varZ = model.addVariable("Z").lower(ZERO);
        Variable varA = model.addVariable("A").upper(THREE);

        Expression expr3 = model.addExpression("Test3").lower(ZERO);

        expr3.set(varX, TWO.negate());
        expr3.set(varA, NEG);

        Presolvers.ZERO_ONE_TWO.simplify(expr3, expr3.getLinearKeySet(), expr3.getLowerLimit(), expr3.getUpperLimit(), precision);

        TestUtils.assertEquals(TWO.negate(), varA.getUpperLimit(), precision);

        Expression expr2 = model.addExpression("Test2").lower(ZERO);

        expr2.set(varX, TWO.negate());
        expr2.set(varY, NEG);

        Presolvers.ZERO_ONE_TWO.simplify(expr2, expr2.getLinearKeySet(), expr2.getLowerLimit(), expr2.getUpperLimit(), precision);

        TestUtils.assertEquals(TWO.negate(), varY.getUpperLimit(), precision);

        Expression expr1 = model.addExpression("Test1").lower(ZERO);

        expr1.set(varX, TWO.negate());
        expr1.set(varZ, ONE);

        Presolvers.ZERO_ONE_TWO.simplify(expr1, expr1.getLinearKeySet(), expr1.getLowerLimit(), expr1.getUpperLimit(), precision);

        TestUtils.assertEquals(TWO, varZ.getLowerLimit(), precision);

    }

    @Test
    public void testSimplyLowerAndUpperBounds() {

        double precision = 0.00001;

        ExpressionsBasedModel model = new ExpressionsBasedModel();
        model.addVariable(Variable.make("A").level(2).weight(3));
        model.addVariable(Variable.make("B").lower(1).upper(3).weight(2));
        model.addVariable(Variable.make("C").lower(0).upper(4).weight(1));
        model.addExpression("SUM").set(0, 1).set(1, 1).set(2, 1).level(6);

        Optimisation.Result minResult = model.minimise();
        TestUtils.assertTrue(model.validate(minResult));
        TestUtils.assertTrue(minResult.getState().isOptimal());

        TestUtils.assertEquals(11, minResult.getValue(), precision);
        TestUtils.assertEquals(2, minResult.doubleValue(0), precision);
        TestUtils.assertEquals(1, minResult.doubleValue(1), precision);
        TestUtils.assertEquals(3, minResult.doubleValue(2), precision);

        Optimisation.Result maxResult = model.maximise();
        TestUtils.assertTrue(model.validate(maxResult));
        TestUtils.assertTrue(maxResult.getState().isOptimal());
        TestUtils.assertEquals(13, maxResult.getValue(), precision);
        TestUtils.assertEquals(2, maxResult.doubleValue(0), precision);
        TestUtils.assertEquals(3, maxResult.doubleValue(1), precision);
        TestUtils.assertEquals(1, maxResult.doubleValue(2), precision);
    }

}

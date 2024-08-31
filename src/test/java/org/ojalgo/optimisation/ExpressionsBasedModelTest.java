/*
 * Copyright 1997-2024 Optimatika
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

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.function.constant.BigMath;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.optimisation.linear.LinearSolver;
import org.ojalgo.structure.Structure1D.IntIndex;
import org.ojalgo.type.context.NumberContext;

public class ExpressionsBasedModelTest extends OptimisationTests {

    @Test
    public void testAddingVariableToExpression() {
        ExpressionsBasedModel model = new ExpressionsBasedModel();
        Variable x1 = model.newVariable("x1").lower(0).upper(20).weight(1);
        Expression expr = model.newExpression("10 * x1 = 100").lower(100).upper(100);
        expr.add(x1, 4); // add term (4 * x1) to expression
        expr.add(x1, 6); // add term (6 * x1) to expression which now becomes (10 * x1)
        Optimisation.Result result = model.minimise();
        BigDecimal x1Result = result.get(model.indexOf(x1));
        TestUtils.assertEquals(BigDecimal.valueOf(10), x1Result);
    }

    /**
     * https://github.com/optimatika/ojAlgo-extensions/issues/3 <br>
     * "compensating" didn't work because of an incorrectly used stream - did peek(...) instead of map(...).
     */
    @Test
    public void testCompensate() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();
        model.newVariable("X1").lower(0).upper(5).weight(1);
        model.newVariable("X2").lower(0).upper(5).weight(1);
        model.newVariable("X3").level(4).weight(1);

        Expression expression = model.newExpression("MAX5").upper(5);
        expression.set(0, 1).set(1, 1).set(2, 1);

        Optimisation.Result result = model.maximise();

        TestUtils.assertTrue(model.validate(result));

        TestUtils.assertTrue(result.getState().isOptimal());

        TestUtils.assertEquals(5.0, result.getValue(), PrimitiveMath.MACHINE_EPSILON);

        TestUtils.assertEquals(1.0, result.doubleValue(0) + result.doubleValue(1), PrimitiveMath.MACHINE_EPSILON);
        TestUtils.assertEquals(4.0, result.doubleValue(2), PrimitiveMath.MACHINE_EPSILON);
    }

    @Test
    public void testExpressionSetAdd() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable var0 = model.addVariable();
        Variable var1 = model.addVariable();
        Variable var2 = model.addVariable();

        Expression expr = model.addExpression();

        expr.set(var1, BigMath.ONE);
        expr.set(var2, BigMath.TWO);

        expr.add(var0, BigMath.TWO);
        expr.add(1, BigMath.THREE);
        expr.add(var2, BigMath.FOUR);

        TestUtils.assertEquals(BigMath.TWO, expr.get(IntIndex.of(0), false));
        TestUtils.assertEquals(BigMath.FOUR, expr.get(IntIndex.of(1), false));
        TestUtils.assertEquals(BigMath.SIX, expr.get(IntIndex.of(2), false));

        TestUtils.assertEquals(BigMath.TWO, expr.get(var0.getIndex(), false));
        TestUtils.assertEquals(BigMath.FOUR, expr.get(var1.getIndex(), false));
        TestUtils.assertEquals(BigMath.SIX, expr.get(var2.getIndex(), false));
    }

    /**
     * https://github.com/optimatika/ojAlgo-extensions/issues/1 Reported as a problem with the CPLEX
     * integration
     */
    @Test
    public void testFixedVariables() {

        ExpressionsBasedModel test = new ExpressionsBasedModel();
        test.newVariable("V1").level(0.5);
        test.newVariable("V2").lower(0).upper(5).weight(2);
        test.newVariable("V3").lower(0).upper(1).weight(1);
        Expression expressions = test.newExpression("E1").lower(0).upper(2);
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
    @Disabled
    public void testGitHubIssue2() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable[] objective = { model.newVariable("X1").weight(0.8), model.newVariable("X2").weight(0.2), model.newVariable("X3").weight(0.7),
                model.newVariable("X4").weight(0.3), model.newVariable("X5").weight(0.6), model.newVariable("X6").weight(0.4) };

        model.newExpression("C1").set(0, 1).set(2, 1).set(4, 1).level(23);
        model.newExpression("C2").set(1, 1).set(3, 1).set(5, 1).level(23);
        model.newExpression("C3").set(0, 1).lower(10);
        model.newExpression("C4").set(2, 1).lower(8);
        model.newExpression("C5").set(4, 1).lower(5);

        Optimisation.Result result = model.maximise();

        // A valid solution of 25.8 can be produced with:
        //     X1=10, X2=0, X3=8, X4=0, X5=5, X6=23
        TestUtils.assertEquals(25.8, result.getValue(), 0.001);
    }

    @Test
    public void testIntegerRounding() {

        ExpressionsBasedModel ebm = new ExpressionsBasedModel();
        Variable var1 = ebm.newVariable("X1").integer();
        Variable var2 = ebm.newVariable("X2").integer();

        Expression exp1 = ebm.newExpression("Exp1").set(var1, 3).set(var2, 6).upper(13.5);
        exp1.doIntegerRounding();
        TestUtils.assertEquals(BigMath.THREE, exp1.get(var1));
        TestUtils.assertEquals(BigMath.SIX, exp1.get(var2));
        TestUtils.assertEquals(BigMath.TWELVE, exp1.getUpperLimit());

        Expression exp2 = ebm.newExpression("Exp2").set(var1, 30).set(var2, 60).upper(135);
        exp2.doIntegerRounding();
        TestUtils.assertEquals(BigMath.THREE.multiply(BigMath.TEN), exp2.get(var1));
        TestUtils.assertEquals(BigMath.SIX.multiply(BigMath.TEN), exp2.get(var2));
        TestUtils.assertEquals(BigMath.TWELVE.multiply(BigMath.TEN), exp2.getUpperLimit());

        Expression exp3 = ebm.newExpression("Exp3").set(var1, 0.3).set(var2, 0.6).upper(1.35);
        exp3.doIntegerRounding();
        TestUtils.assertEquals(BigMath.THREE.divide(BigMath.TEN), exp3.get(var1));
        TestUtils.assertEquals(BigMath.SIX.divide(BigMath.TEN), exp3.get(var2));
        TestUtils.assertEquals(BigMath.TWELVE.divide(BigMath.TEN), exp3.getUpperLimit());

        Expression exp4 = ebm.newExpression("Exp4").set(var1, 0.3).set(var2, 60).upper(1.35);
        exp4.doIntegerRounding();
        TestUtils.assertEquals(BigMath.THREE.divide(BigMath.TEN), exp4.get(var1));
        TestUtils.assertEquals(BigMath.SIX.multiply(BigMath.TEN), exp4.get(var2));
        TestUtils.assertEquals(BigDecimal.valueOf(1.2), exp4.getUpperLimit());

        Expression exp5 = ebm.newExpression("Exp5").set(var1, 30).set(var2, 0.6).upper(1.35);
        exp5.doIntegerRounding(); // How to get 50, 1 and 2?
        TestUtils.assertEquals(BigMath.THREE.multiply(BigMath.TEN), exp5.get(var1)); // 50
        TestUtils.assertEquals(BigMath.SIX.divide(BigMath.TEN), exp5.get(var2)); // 1
        TestUtils.assertEquals(BigDecimal.valueOf(1.2), exp5.getUpperLimit()); // 2
    }

    @Test
    public void testMPStestprob() {

        ExpressionsBasedModel tmpModel = new ExpressionsBasedModel();

        Variable tmpXONE = tmpModel.newVariable("XONE").weight(ONE).lower(ZERO).upper(FOUR);
        Variable tmpYTWO = tmpModel.newVariable("YTWO").weight(FOUR).lower(NEG).upper(ONE);
        Variable tmpZTHREE = tmpModel.newVariable("ZTHREE").weight(NINE).lower(ZERO).upper(null);

        Variable[] tmpVariables = { tmpXONE, tmpYTWO, tmpZTHREE };

        BigDecimal[] tmpFactorsLIM1 = { ONE, ONE, ZERO };
        Expression tmpLIM1 = tmpModel.newExpression("LIM1");
        for (int v = 0; v < tmpVariables.length; v++) {
            tmpLIM1.set(v, tmpFactorsLIM1[v]);
        }
        tmpLIM1.upper(FIVE.add(TENTH));

        BigDecimal[] tmpFactorsLIM2 = { ONE, ZERO, ONE };
        Expression tmpLIM2 = tmpModel.newExpression("LIM2");
        for (int v = 0; v < tmpVariables.length; v++) {
            tmpLIM2.set(v, tmpFactorsLIM2[v]);
        }
        tmpLIM2.lower(TEN.add(TENTH));

        BigDecimal[] tmpFactorsMYEQN = { ZERO, ONE.negate(), ONE };
        Expression tmpMYEQN = tmpModel.newExpression("MYEQN");
        for (int v = 0; v < tmpVariables.length; v++) {
            tmpMYEQN.set(v, tmpFactorsMYEQN[v]);
        }
        tmpMYEQN.level(SEVEN);

        TestUtils.assertTrue(tmpModel.validate());

        if (OptimisationTests.DEBUG) {
            BasicLogger.debug(tmpModel);
            tmpModel.options.debug(LinearSolver.class);
        }

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

        Variable varX = model.newVariable("X").lower(ONE);
        Variable varY = model.newVariable("Y");
        Variable varZ = model.newVariable("Z").lower(ZERO);
        Variable varA = model.newVariable("A").upper(THREE);

        Expression expr3 = model.newExpression("Test3").lower(ZERO);

        expr3.set(varX, TWO.negate());
        expr3.set(varA, NEG);

        Presolvers.ZERO_ONE_TWO.simplify(expr3, expr3.getLinearKeySet(), expr3.getLowerLimit(), expr3.getUpperLimit(), precision);

        TestUtils.assertEquals(TWO.negate(), varA.getUpperLimit(), precision);

        Expression expr2 = model.newExpression("Test2").lower(ZERO);

        expr2.set(varX, TWO.negate());
        expr2.set(varY, NEG);

        Presolvers.ZERO_ONE_TWO.simplify(expr2, expr2.getLinearKeySet(), expr2.getLowerLimit(), expr2.getUpperLimit(), precision);

        TestUtils.assertEquals(TWO.negate(), varY.getUpperLimit(), precision);

        Expression expr1 = model.newExpression("Test1").lower(ZERO);

        expr1.set(varX, TWO.negate());
        expr1.set(varZ, ONE);

        Presolvers.ZERO_ONE_TWO.simplify(expr1, expr1.getLinearKeySet(), expr1.getLowerLimit(), expr1.getUpperLimit(), precision);

        TestUtils.assertEquals(TWO, varZ.getLowerLimit(), precision);

    }

    /**
     * https://github.com/optimatika/ojAlgo/issues/415
     */
    @Test
    public void testSimpleInfeasibleSimplification() {

        //

        ExpressionsBasedModel original1 = new ExpressionsBasedModel();

        Variable x1 = original1.newVariable("x").weight(1.0);

        Expression c01 = original1.newExpression("c0").upper(1);
        c01.set(x1, 1);

        Expression c11 = original1.newExpression("c1").lower(2);
        c11.set(x1, 1);

        ExpressionsBasedModel simplified1 = original1.simplify();

        TestUtils.assertEquals(Optimisation.State.INFEASIBLE, simplified1.minimise().getState());
        TestUtils.assertEquals(Optimisation.State.INFEASIBLE, simplified1.maximise().getState());

        //

        ExpressionsBasedModel original2 = new ExpressionsBasedModel();

        Variable x2 = original2.newVariable("x").weight(1.0);

        Expression c02 = original2.newExpression("c0").level(1);
        c02.set(x2, 1);

        Expression c12 = original2.newExpression("c1").level(2);
        c12.set(x2, 1);

        ExpressionsBasedModel simplified2 = original2.simplify();

        TestUtils.assertEquals(Optimisation.State.INFEASIBLE, simplified2.minimise().getState());
        TestUtils.assertEquals(Optimisation.State.INFEASIBLE, simplified2.maximise().getState());
    }

    @Test
    public void testSimplyLowerAndUpperBounds() {

        double precision = 0.00001;

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable varA = model.newVariable("A").level(2).weight(3);
        Variable varB = model.newVariable("B").lower(1).upper(3).weight(2);
        Variable varC = model.newVariable("C").lower(0).upper(4).weight(1);

        model.newExpression("SUM").set(varA, 1).set(varB, 1).set(varC, 1).level(6);

        if (DEBUG) {
            BasicLogger.debug(model);
            model.options.debug(LinearSolver.class);
        }

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

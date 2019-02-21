/*
 * Copyright 1997-2018 Optimatika
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

import static org.ojalgo.constant.BigMath.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.constant.BigMath;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.structure.Structure1D.IntIndex;
import org.ojalgo.type.context.NumberContext;

public class ExpressionsBasedModelTest {

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

        final Variable tmpXONE = new Variable("XONE").weight(ONE).lower(ZERO).upper(FOUR);
        final Variable tmpYTWO = new Variable("YTWO").weight(FOUR).lower(NEG).upper(ONE);
        final Variable tmpZTHREE = new Variable("ZTHREE").weight(NINE).lower(ZERO).upper(null);

        final Variable[] tmpVariables = new Variable[] { tmpXONE, tmpYTWO, tmpZTHREE };

        final ExpressionsBasedModel tmpModel = new ExpressionsBasedModel(tmpVariables);

        final BigDecimal[] tmpFactorsLIM1 = new BigDecimal[] { ONE, ONE, ZERO };
        final Expression tmpLIM1 = tmpModel.addExpression("LIM1");
        for (int v = 0; v < tmpVariables.length; v++) {
            tmpLIM1.set(v, tmpFactorsLIM1[v]);
        }
        tmpLIM1.upper(FIVE.add(TENTH));

        final BigDecimal[] tmpFactorsLIM2 = new BigDecimal[] { ONE, ZERO, ONE };
        final Expression tmpLIM2 = tmpModel.addExpression("LIM2");
        for (int v = 0; v < tmpVariables.length; v++) {
            tmpLIM2.set(v, tmpFactorsLIM2[v]);
        }
        tmpLIM2.lower(TEN.add(TENTH));

        final BigDecimal[] tmpFactorsMYEQN = new BigDecimal[] { ZERO, ONE.negate(), ONE };
        final Expression tmpMYEQN = tmpModel.addExpression("MYEQN");
        for (int v = 0; v < tmpVariables.length; v++) {
            tmpMYEQN.set(v, tmpFactorsMYEQN[v]);
        }
        tmpMYEQN.level(SEVEN);

        TestUtils.assertTrue(tmpModel.validate());

        final Result tmpMinRes = tmpModel.minimise();
        final Result tmpMaxRes = tmpModel.maximise();

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

        NumberContext precision = NumberContext.getGeneral(14, 12);
        Set<IntIndex> fixedVariables = Collections.emptySet();
        BigDecimal fixedValue = ZERO;

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

}

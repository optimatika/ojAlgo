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
package org.ojalgo.optimisation.integer;

import static org.ojalgo.constant.BigMath.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.array.BigArray;
import org.ojalgo.constant.BigMath;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.optimisation.Optimisation.State;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.type.context.NumberContext;

public class IntegerProblems {

    /**
     * 20120227: Forgot to document what the problem was. Now I just check there is an optimal solution.
     */
    @Test
    public void testP20100412() {

        final boolean tmpDebug = false;

        final ExpressionsBasedModel tmpModel = OptimisationIntegerData.buildModelForP20100412();

        if (tmpDebug) {
            BasicLogger.debug("Model Before");
            BasicLogger.debug(tmpModel.toString());

        }
        final Result tmpResult = tmpModel.maximise();
        if (tmpDebug) {
            BasicLogger.debug("Model After");
            BasicLogger.debug(tmpModel.toString());
        }

        TestUtils.assertTrue(tmpResult.getState().isOptimal());
    }

    /**
     * If the relaxed problem was infeasible you got a NullPointerException instead of a result indicating
     * that the problem is infeasible.
     */
    @Test
    public void testP20111010() {

        final Variable[] tmpVariables = new Variable[] { Variable.makeBinary("X").weight(ONE), Variable.makeBinary("Y").weight(ONE),
                Variable.makeBinary("Z").weight(ONE) };

        final ExpressionsBasedModel tmpModel = new ExpressionsBasedModel(tmpVariables);

        final Expression tmpC1 = tmpModel.addExpression("C1");
        for (int i = 0; i < tmpModel.countVariables(); i++) {
            tmpC1.set(i, ONE);
        }

        tmpC1.level(ONE);

        final Expression tmpC2 = tmpModel.addExpression("C2");
        for (int i = 0; i < tmpModel.countVariables(); i++) {
            tmpC2.set(i, ONE);
        }
        tmpC2.level(TWO);

        final Expression tmpC3 = tmpModel.addExpression("C3");
        for (int i = 0; i < tmpModel.countVariables(); i++) {
            tmpC3.set(i, ONE);
        }
        tmpC3.level(THREE);

        //tmpModel.options.debug(LinearSolver.class);

        final Optimisation.Result tmpResult = tmpModel.minimise();

        TestUtils.assertEquals(State.INFEASIBLE, tmpResult.getState());
    }

    /**
     * The IP solver returns infeasible solutions, but the problem seems to actually be with the LP solver.
     * Don't know what the actual solution is. Just check that the solver terminates normally and that the
     * solution is in fact feasible/valid.
     */
    @Test
    @Tag("slow")
    public void testP20130225() {

        final ExpressionsBasedModel tmpIntegerModel = P20130225.makeModel();
        final ExpressionsBasedModel tmpRelaxedModel = tmpIntegerModel.relax(false);

        final Optimisation.Result tmpRelaxedResult = tmpRelaxedModel.minimise();
        TestUtils.assertEquals("Solution To Relaxed Problem Not Optimal!", Optimisation.State.OPTIMAL, tmpRelaxedResult.getState());
        TestUtils.assertTrue("Solution To Relaxed Problem Not Valid!", tmpRelaxedModel.validate(tmpRelaxedResult));

        final Optimisation.Result tmpIntegerResult = tmpIntegerModel.minimise();
        TestUtils.assertEquals("Integer Solution Not Optimal!", Optimisation.State.OPTIMAL, tmpIntegerResult.getState());
        TestUtils.assertTrue("Integer Solution Not Valid!", tmpIntegerModel.validate(tmpIntegerResult));
    }

    /**
     * apete's implementation of the original problem description.
     * <a href="http://bugzilla.optimatika.se/show_bug.cgi?id=178">BugZilla</a>
     */
    @Test
    public void testP20130409a() {

        final Variable[] tmpVariables = new Variable[] { new Variable("x1").lower(BigMath.ZERO).weight(BigMath.ONE),
                new Variable("x2013").lower(BigMath.ZERO).integer(true), new Variable("x2014").lower(BigMath.ZERO).integer(true) };

        final ExpressionsBasedModel tmpModel = new ExpressionsBasedModel(tmpVariables);

        final Expression tmpExpr1 = tmpModel.addExpression("Expr1");
        tmpExpr1.set(0, -1);
        tmpExpr1.set(1, 5100);
        tmpExpr1.set(2, -5000);
        tmpExpr1.upper(BigMath.ZERO);

        final Expression tmpExpr2 = tmpModel.addExpression("Expr2");
        tmpExpr2.set(0, 1);
        tmpExpr2.set(1, 5100);
        tmpExpr2.set(2, -5000);
        tmpExpr2.lower(BigMath.ZERO);

        final Expression tmpExpr3 = tmpModel.addExpression("Expr3");
        tmpExpr3.set(1, 5000);
        tmpExpr3.set(2, 5000);
        tmpExpr3.level(new BigDecimal(19105000));

        final BigArray tmpExpSol = BigArray
                .wrap(new BigDecimal[] { BigDecimal.valueOf(4200.000000000075), BigDecimal.valueOf(1892), BigDecimal.valueOf(1929) });

        TestUtils.assertTrue("Expected Solution Not Valid", tmpModel.validate(tmpExpSol));

        //tmpModel.options.debug(GenericSolver.class);
        //tmpModel.options.problem = NumberContext.getGeneral(12);

        final Result tmpResult = tmpModel.minimise();

        // BasicLogger.debug(tmpResult.toString());

        TestUtils.assertEquals("Solution Not Correct", tmpExpSol, tmpResult, new NumberContext(8, 8));
        TestUtils.assertTrue("Solver State Not Optimal", tmpResult.getState().isOptimal());
    }

    /**
     * Test case sent in by the user / problem reporter
     * <a href="http://bugzilla.optimatika.se/show_bug.cgi?id=178">BugZilla</a>
     */
    @Test
    public void testP20130409b() {

        final Variable x1 = Variable.make("x1");
        final Variable x2013 = Variable.make("x2013");
        final Variable x2014 = Variable.make("x2014");
        final Variable x2015 = Variable.make("x2015");
        x2013.setInteger(true);
        x2014.setInteger(true);
        x2015.setInteger(true);

        final ExpressionsBasedModel tmpModel = new ExpressionsBasedModel();
        tmpModel.addVariable(x1);
        tmpModel.addVariable(x2013);
        tmpModel.addVariable(x2014);
        tmpModel.addVariable(x2015);

        final Expression obj = tmpModel.addExpression("obj");
        obj.set(x1, 1);
        obj.weight(BigDecimal.valueOf(1));

        final Expression c1 = tmpModel.addExpression("c1");
        c1.set(x1, 1);
        c1.lower(BigDecimal.valueOf(0));

        final Expression c2 = tmpModel.addExpression("c2");
        c2.set(x2014, -5000);
        c2.set(x2013, 5100);
        c2.set(x1, -1);
        c2.upper(BigDecimal.valueOf(0));

        final Expression c3 = tmpModel.addExpression("c3");
        c3.set(x2014, -5000);
        c3.set(x2013, 5100);
        c3.set(x1, 1);
        c3.lower(BigDecimal.valueOf(0));

        final Expression c4 = tmpModel.addExpression("c4");
        c4.set(x2014, 150);
        c4.set(x2013, 5100);
        c4.set(x2015, -5000);
        c4.set(x1, -1);
        c4.upper(BigDecimal.valueOf(0));

        final Expression c5 = tmpModel.addExpression("c5");
        c5.set(x2014, 150);
        c5.set(x2013, 5100);
        c5.set(x2015, -5000);
        c5.set(x1, 1);
        c5.lower(BigDecimal.valueOf(0));

        final Expression c6 = tmpModel.addExpression("c6");
        c6.set(x2015, 5000);
        c6.set(x2014, 5000);
        c6.set(x2013, 5000);
        c6.level(BigDecimal.valueOf(19105000));

        final BigArray tmpExpSol = BigArray
                .wrap(new BigDecimal[] { BigDecimal.valueOf(4849.999999997941), BigDecimal.valueOf(1245), BigDecimal.valueOf(1269), BigDecimal.valueOf(1307) });

        TestUtils.assertTrue("Expected Solution Not Valid", tmpModel.validate(tmpExpSol));

        //tmpModel.options.debug(IntegerSolver.class);
        //tmpModel.options.problem = NumberContext.getGeneral(8);

        final Result tmpResult = tmpModel.minimise();

        // BasicLogger.debug(tmpResult.toString());

        TestUtils.assertEquals("Solution Not Correct", tmpExpSol, tmpResult, new NumberContext(8, 8));
        TestUtils.assertTrue("Solver State Not Optimal", tmpResult.getState().isOptimal());
    }

    /**
     * <a href="http://bugzilla.optimatika.se/show_bug.cgi?id=211">BugZilla-211</a>
     */
    @Test
    public void testP20140819() {

        final ExpressionsBasedModel tmpModel = new ExpressionsBasedModel();

        final double[] tmpWeights = new double[] { 2691.5357279536333, 2600.760150603986, 2605.8958795795374, 2606.7208332501104, 2715.0757845953835,
                2602.194912040238, 2606.0069468717575, 2609.0385816244316, 2750.0520522057927, 2602.048261785581, 2600.507229973181, 2602.046307869504,
                2721.343937605796, 2601.7367414553805, 2600.595318433882, 2599.405979211142 };

        for (int v = 0; v < tmpWeights.length; v++) {
            tmpModel.addVariable(Variable.make("x" + v).integer(true).lower(0).upper(414).weight(tmpWeights[v]));
        }

        // 117 <= 30 30 30 30 0 4 0 0 0 4 0 0 0 4 0 0 <= 14868
        // 36 <= 0 4 0 0 40 40 40 40 0 0 4 0 0 0 4 0 <= 170569
        // 341 <= 0 0 8 0 0 0 8 0 68 68 68 68 0 0 0 5 <= 140833
        // 413 <= 0 0 0 8 0 0 0 9 0 0 0 6 59 59 59 59 <= 48321

        final int[] tmpLower = new int[] { 117, 36, 341, 413 };
        final int[] tmpUpper = new int[] { 14868, 170569, 140833, 48321 };
        final int[][] tmpFactors = new int[4][];
        tmpFactors[0] = new int[] { 30, 30, 30, 30, 0, 4, 0, 0, 0, 4, 0, 0, 0, 4, 0, 0 };
        tmpFactors[1] = new int[] { 0, 4, 0, 0, 40, 40, 40, 40, 0, 0, 4, 0, 0, 0, 4, 0 };
        tmpFactors[2] = new int[] { 0, 0, 8, 0, 0, 0, 8, 0, 68, 68, 68, 68, 0, 0, 0, 5 };
        tmpFactors[3] = new int[] { 0, 0, 0, 8, 0, 0, 0, 9, 0, 0, 0, 6, 59, 59, 59, 59 };

        for (int c = 0; c < tmpFactors.length; c++) {
            final Expression tmpExpr = tmpModel.addExpression("C" + c);
            tmpExpr.lower(tmpLower[c]).upper(tmpUpper[c]);
            for (int v = 0; v < tmpFactors[c].length; v++) {
                tmpExpr.set(v, tmpFactors[c][v]);
            }
        }

        // tmpModel.options.debug(IntegerSolver.class);

        final Result tmpResult = tmpModel.minimise();

        if (OptimisationIntegerTests.DEBUG) {
            BasicLogger.debug(tmpResult);
            BasicLogger.debug(tmpModel);
        }

        TestUtils.assertStateNotLessThanOptimal(tmpResult);

        TestUtils.assertTrue(tmpModel.validate(tmpResult));
    }

    @Test
    public void testP20150127full() {

        final ExpressionsBasedModel tmpModel = P20150127a.getModel();

        final Optimisation.Result tmpResult = tmpModel.minimise();

        // Model should be solvable (e.g. x=201, y=-10)?!
        TestUtils.assertStateNotLessThanFeasible(tmpResult);

        final BigDecimal tmpSolX = tmpResult.get(0);
        final BigDecimal tmpSolY = tmpResult.get(1);
        final int tmpIntX = tmpSolX.setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
        final int tmpIntY = tmpSolY.setScale(0, BigDecimal.ROUND_HALF_UP).intValue();

        if (OptimisationIntegerTests.DEBUG) {
            BasicLogger.debug("x = " + tmpSolX + " ~ " + tmpIntX);
            BasicLogger.debug("y = " + tmpSolY + " ~ " + tmpIntY);
        }

        TestUtils.assertTrue("Solution not valid!", tmpModel.validate(tmpResult));

        // Verify solution
        for (final int[] tmpCoeff : P20150127a.getCoefficients()) {
            final int tmpValue = (tmpCoeff[0] * tmpIntX) + (tmpCoeff[1] * tmpIntY);
            final BigDecimal tmpExact = tmpSolX.multiply(BigDecimal.valueOf(tmpCoeff[0])).add(tmpSolY.multiply(BigDecimal.valueOf(tmpCoeff[1])));
            if (tmpValue >= 0) {
                TestUtils.fail(tmpCoeff[0] + "*x + " + tmpCoeff[1] + "*y = " + tmpValue + " must be negative (exact: " + tmpExact + ")");
            }
        }
    }

    @Test
    public void testP20150127infeasibleNode() {

        final ExpressionsBasedModel tmpModel = P20150127b.getModel(true, false);

        final Optimisation.Result tmpResult = tmpModel.minimise();

        // Model is infeasible, and must be reported as such
        TestUtils.assertStateLessThanFeasible(tmpResult);
    }

    @Test
    public void testP20160701() {
        P20160701.main(new String[0]);
    }

}

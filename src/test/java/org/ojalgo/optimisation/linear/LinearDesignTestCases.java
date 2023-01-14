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
package org.ojalgo.optimisation.linear;

import static org.ojalgo.function.constant.BigMath.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.array.ArrayR256;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.function.constant.BigMath;
import org.ojalgo.matrix.MatrixQ128;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PhysicalStore.Factory;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.optimisation.Optimisation.State;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.structure.Access1D;
import org.ojalgo.type.context.NumberContext;

public class LinearDesignTestCases extends OptimisationLinearTests {

    static ExpressionsBasedModel buildOldKnapsackTestModel() {

        Variable tmpVar;
        Variable[] tmpVariables = new Variable[8];

        tmpVar = new Variable("QRHEDGE");
        tmpVar.weight(BigMath.ZERO);
        tmpVar.lower(BigMath.ZERO);
        tmpVar.upper(BigMath.ZERO);
        tmpVariables[0] = tmpVar;

        tmpVar = new Variable("QKORT");
        tmpVar.weight(new BigDecimal("0.0345"));
        tmpVar.lower(BigMath.ZERO);
        tmpVar.upper(BigMath.ONE);
        tmpVariables[1] = tmpVar;

        tmpVar = new Variable("QHEDGE");
        tmpVar.weight(new BigDecimal("0.04"));
        tmpVar.lower(new BigDecimal("0.1846"));
        tmpVar.upper(new BigDecimal("0.1846"));
        tmpVariables[2] = tmpVar;

        tmpVar = new Variable("QLÅNG");
        tmpVar.weight(new BigDecimal("0.0412"));
        tmpVar.lower(BigMath.ZERO);
        tmpVar.upper(BigMath.ONE);
        tmpVariables[3] = tmpVar;

        tmpVar = new Variable("QFF");
        tmpVar.weight(new BigDecimal("0.069575"));
        tmpVar.lower(BigMath.ZERO);
        tmpVar.upper(BigMath.ZERO);
        tmpVariables[4] = tmpVar;

        tmpVar = new Variable("QGLOBAL");
        tmpVar.weight(new BigDecimal("0.0738"));
        tmpVar.lower(BigMath.ZERO);
        tmpVar.upper(BigMath.ONE);
        tmpVariables[5] = tmpVar;

        tmpVar = new Variable("QSVERIGE");
        tmpVar.weight(new BigDecimal("0.1288"));
        tmpVar.lower(BigMath.ZERO);
        tmpVar.upper(BigMath.ONE);
        tmpVariables[6] = tmpVar;

        tmpVar = new Variable("QFF2");
        tmpVar.weight(new BigDecimal("2.3"));
        tmpVar.lower(BigMath.ZERO);
        tmpVar.upper(BigMath.ZERO);
        tmpVariables[7] = tmpVar;

        ExpressionsBasedModel retVal = new ExpressionsBasedModel(tmpVariables);
        int tmpLength = retVal.countVariables();

        Expression retVal2 = retVal.newExpression("100%");

        for (int i = 0; i < tmpLength; i++) {
            retVal2.set(i, ONE);
        }
        Expression retVal1 = retVal2;

        retVal1.lower(BigMath.ONE);
        retVal1.upper(BigMath.ONE);

        return retVal;
    }

    /**
     * http://math.uww.edu/~mcfarlat/s-prob.htm
     */
    @Test
    public void test1LinearModelCase() {

        Variable[] variables = { new Variable("X1").lower(ZERO).weight(ONE), new Variable("X2").lower(ZERO).weight(TWO),
                new Variable("X3").lower(ZERO).weight(ONE.negate()) };

        ExpressionsBasedModel model = new ExpressionsBasedModel(variables);

        Expression exprC1 = model.newExpression("C1");
        for (int i = 0; i < model.countVariables(); i++) {
            exprC1.set(i, new BigDecimal[] { TWO, ONE, ONE }[i]);
        }
        exprC1.upper(new BigDecimal("14.0"));

        Expression exprC2 = model.newExpression("C2");
        for (int i = 0; i < model.countVariables(); i++) {
            exprC2.set(i, new BigDecimal[] { FOUR, TWO, THREE }[i]);
        }
        exprC2.upper(new BigDecimal("28.0"));

        Expression exprC3 = model.newExpression("C3");
        for (int i = 0; i < model.countVariables(); i++) {
            exprC3.set(i, new BigDecimal[] { TWO, FIVE, FIVE }[i]);
        }
        exprC3.upper(new BigDecimal("30.0"));

        if (DEBUG) {
            model.options.debug(LinearSolver.class);
        }

        Optimisation.Result actual = Result.of(13.0, State.OPTIMAL, 5.0, 4.0, 0.0);

        Optimisation.Result expected = model.maximise();

        TestUtils.assertStateAndSolution(expected, actual);
    }

    /**
     * http://www.stats.ox.ac.uk/~yu/4_Simplex_II.pdf
     */
    @Test
    public void test2LinearModelCase() {

        Variable[] tmpVariables = { new Variable("X1").lower(ZERO).weight(THREE), new Variable("X2").lower(ZERO).weight(ZERO),
                new Variable("X3").lower(ZERO).weight(ONE) };

        ExpressionsBasedModel tmpModel = new ExpressionsBasedModel(tmpVariables);

        Expression tmpExprC1 = tmpModel.newExpression("C1");
        for (int i = 0; i < tmpModel.countVariables(); i++) {
            tmpExprC1.set(i, new BigDecimal[] { ONE, TWO, ONE }[i]);
        }
        tmpExprC1.level(TEN);

        Expression tmpExprC2 = tmpModel.newExpression("C2");
        for (int i = 0; i < tmpModel.countVariables(); i++) {
            tmpExprC2.set(i, new BigDecimal[] { ONE, TWO.negate(), TWO }[i]);
        }
        tmpExprC2.level(SIX);

        Optimisation.Result tmpResult = tmpModel.maximise();
        MatrixQ128 tmpSolution = MatrixQ128.FACTORY.columns(tmpResult);

        PhysicalStore<Double> tmpExpX = Primitive64Store.FACTORY.rows(new double[][] { { 8.0 }, { 1.0 }, { 0.0 } });
        PhysicalStore<Double> tmpActX = Primitive64Store.FACTORY.copy(tmpSolution.rows(0, 1, 2));

        TestUtils.assertEquals(tmpExpX, tmpActX);
    }

    /**
     * http://optlab-server.sce.carleton.ca/POAnimations/TwoPhase.aspx
     */
    @Test
    public void test3LinearModelCase() {

        Variable[] tmpVariables = { new Variable("X1").lower(ZERO).weight(TEN.add(FIVE)), new Variable("X2").lower(ZERO).weight(TEN) };

        ExpressionsBasedModel tmpModel = new ExpressionsBasedModel(tmpVariables);

        Expression tmpExprC1 = tmpModel.newExpression("C1");
        for (int i = 0; i < tmpModel.countVariables(); i++) {
            tmpExprC1.set(i, new BigDecimal[] { ONE, ZERO }[i]);
        }
        tmpExprC1.upper(TWO);

        Expression tmpExprC2 = tmpModel.newExpression("C2");
        for (int i = 0; i < tmpModel.countVariables(); i++) {
            tmpExprC2.set(i, new BigDecimal[] { ZERO, ONE }[i]);
        }
        tmpExprC2.upper(THREE);

        Expression tmpExprC3 = tmpModel.newExpression("C3");
        for (int i = 0; i < tmpModel.countVariables(); i++) {
            tmpExprC3.set(i, new BigDecimal[] { ONE, ONE }[i]);
        }
        tmpExprC3.level(FOUR);

        Optimisation.Result tmpResult = tmpModel.maximise();
        MatrixQ128 tmpSolution = MatrixQ128.FACTORY.columns(tmpResult);

        PhysicalStore<Double> tmpExpX = Primitive64Store.FACTORY.rows(new double[][] { { 2.0 }, { 2.0 } });
        PhysicalStore<Double> tmpActX = Primitive64Store.FACTORY.copy(tmpSolution.rows(0, 1));

        TestUtils.assertEquals(tmpExpX, tmpActX);
    }

    /**
     * http://www.saintmarys.edu/~psmith/338act14.html
     */
    @Test
    public void test4LinearModelCase() {

        Variable[] tmpVariables = { new Variable("X1").lower(ZERO).weight(ONE.negate()), new Variable("X2").lower(ZERO).weight(ONE),
                new Variable("X3").lower(ZERO).weight(ZERO) };

        ExpressionsBasedModel tmpModel = new ExpressionsBasedModel(tmpVariables);

        Expression tmpExprC1 = tmpModel.newExpression("C1");
        for (int i = 0; i < tmpModel.countVariables(); i++) {
            tmpExprC1.set(i, new BigDecimal[] { SIX, ONE.negate(), ZERO }[i]);
        }
        tmpExprC1.upper(TEN);

        Expression tmpExprC2 = tmpModel.newExpression("C2");
        for (int i = 0; i < tmpModel.countVariables(); i++) {
            tmpExprC2.set(i, new BigDecimal[] { ONE, FIVE, ZERO }[i]);
        }
        tmpExprC2.lower(FOUR);

        Expression tmpExprC3 = tmpModel.newExpression("C3");
        for (int i = 0; i < tmpModel.countVariables(); i++) {
            tmpExprC3.set(i, new BigDecimal[] { ONE, FIVE, ONE }[i]);
        }
        tmpExprC3.level(FIVE);

        Optimisation.Result tmpResult = tmpModel.minimise();
        MatrixQ128 tmpSolution = MatrixQ128.FACTORY.columns(tmpResult);

        PhysicalStore<Double> tmpExpX = Primitive64Store.FACTORY.rows(new double[][] { { 1.74 }, { 0.45 }, { 1.0 } });
        PhysicalStore<Double> tmpActX = Primitive64Store.FACTORY.copy(tmpSolution.rows(0, 1, 2));
        tmpActX.modifyAll(NumberContext.of(7, 2).getFunction(PrimitiveFunction.getSet()));

        TestUtils.assertEquals(tmpExpX, tmpActX);
    }

    /**
     * http://www.maths.bris.ac.uk/~maxmr/opt/sm2.pdf
     */
    @Test
    public void test5LinearModelCase() {

        Variable[] tmpVariables = { new Variable("X1").lower(ZERO).weight(TWO), new Variable("X2").lower(ZERO).weight(THREE) };

        ExpressionsBasedModel tmpModel = new ExpressionsBasedModel(tmpVariables);

        Expression tmpExprC1 = tmpModel.newExpression("C1");
        for (int i = 0; i < tmpModel.countVariables(); i++) {
            tmpExprC1.set(i, new BigDecimal[] { HALF, QUARTER }[i]);
        }
        tmpExprC1.upper(FOUR);

        Expression tmpExprC2 = tmpModel.newExpression("C2");
        for (int i = 0; i < tmpModel.countVariables(); i++) {
            tmpExprC2.set(i, new BigDecimal[] { ONE, THREE }[i]);
        }
        tmpExprC2.lower(TEN.add(TEN));

        Expression tmpExprC3 = tmpModel.newExpression("C3");
        for (int i = 0; i < tmpModel.countVariables(); i++) {
            tmpExprC3.set(i, new BigDecimal[] { ONE, ONE }[i]);
        }
        tmpExprC3.level(TEN);

        Optimisation.Result tmpResult = tmpModel.minimise();
        MatrixQ128 tmpSolution = MatrixQ128.FACTORY.columns(tmpResult);

        PhysicalStore<Double> tmpExpX = Primitive64Store.FACTORY.rows(new double[][] { { 5.0 }, { 5.0 } });
        PhysicalStore<Double> tmpActX = Primitive64Store.FACTORY.copy(tmpSolution.rows(0, 1));

        TestUtils.assertEquals(tmpExpX, tmpActX);
    }

    /**
     * Simplest possible unbounded model
     */
    @Test
    public void test6LinearModelCase() {

        Variable[] tmpVariables = { new Variable("X1").lower(ZERO).weight(ONE), new Variable("X2").lower(ZERO).weight(TWO),
                new Variable("X3").lower(ZERO).weight(THREE) };

        ExpressionsBasedModel tmpModel = new ExpressionsBasedModel(tmpVariables);

        Optimisation.Result tmpResult = tmpModel.maximise();

        TestUtils.assertEquals(State.UNBOUNDED, tmpResult.getState());
    }

    /**
     * http://math.uww.edu/~mcfarlat/ns-prob.htm
     */
    @Test
    public void test7LinearModelCase() {

        Variable[] variables = { new Variable("X1").lower(ZERO).weight(TWO), new Variable("X2").lower(ZERO).weight(THREE) };

        ExpressionsBasedModel model = new ExpressionsBasedModel(variables);

        BigDecimal[] paramC1 = { ONE, ONE };
        Expression exprC1 = model.newExpression("C1");
        for (int i = 0; i < model.countVariables(); i++) {
            exprC1.set(i, paramC1[i]);
        }
        exprC1.upper(TEN);

        BigDecimal[] paramC2 = { ONE, TWO };
        Expression exprC2 = model.newExpression("C2");
        for (int i = 0; i < model.countVariables(); i++) {
            exprC2.set(i, paramC2[i]);
        }
        exprC2.lower(TWELVE);

        BigDecimal[] paramC3 = { TWO, ONE };
        Expression exprC3 = model.newExpression("C3");
        for (int i = 0; i < model.countVariables(); i++) {
            exprC3.set(i, paramC3[i]);
        }
        exprC3.lower(TWELVE);

        if (DEBUG) {
            model.options.debug(LinearSolver.class);
        }

        Optimisation.Result expected = Optimisation.Result.of(State.OPTIMAL, 4.0, 4.0);
        Optimisation.Result actual = model.minimise();

        TestUtils.assertStateAndSolution(expected, actual);
    }

    /**
     * http://books.google.com/books?id=3iZznGJq4ZMC page 192
     */
    @Test
    public void test8LinearModelCase() {

        Variable[] variables = { new Variable("X1").lower(ZERO).weight(ONE), new Variable("X2").lower(ZERO).weight(TWO),
                new Variable("X3").lower(ZERO).weight(ONE) };

        ExpressionsBasedModel model = new ExpressionsBasedModel(variables);

        BigDecimal[] valsC1 = { THREE, ONE, NEG };
        Expression exprC1 = model.newExpression("C1");
        for (int i = 0; i < model.countVariables(); i++) {
            exprC1.set(i, valsC1[i]);
        }
        exprC1.level(TEN.add(FIVE));

        BigDecimal[] valsC2 = { EIGHT, FOUR, NEG };
        Expression exprC2 = model.newExpression("C2");
        for (int i = 0; i < model.countVariables(); i++) {
            exprC2.set(i, valsC2[i]);
        }
        exprC2.level(FIVE.multiply(TEN));

        BigDecimal[] valsC3 = { TWO, TWO, ONE };
        Expression exprC3 = model.newExpression("C3");
        for (int i = 0; i < model.countVariables(); i++) {
            exprC3.set(i, valsC3[i]);
        }
        exprC3.level(TEN.add(TEN));

        Optimisation.Result result = model.maximise();

        TestUtils.assertStateNotLessThanFeasible(result);
    }

    /**
     * http://web.mit.edu/15.053/www/AMP-Chapter-04.pdf
     * https://web.fe.up.pt/~mac/ensino/docs/OT20122013/Chapter%204%20-%20Duality%20in%20Linear%20Programming.pdf
     */
    @Test
    public void testDuality() {

        Factory<Double, Primitive64Store> factory = Primitive64Store.FACTORY;

        Primitive64Store expPrimSol = factory.rows(new double[] { 36.0, 0.0, 6.0 });
        Primitive64Store expDualSol = factory.rows(new double[] { 11.0, 0.5 });
        double expOptVal = 294.0;

        LinearSolver.StandardBuilder primal = LinearSolver.newStandardBuilder();

        // Negated since actual problem is max and algorithm expects min
        Primitive64Store pC = factory.make(5, 1);
        pC.set(0, -6.0);
        pC.set(1, -14.0);
        pC.set(2, -13.0);
        pC.set(3, 0);
        pC.set(4, 0);

        primal.objective(pC);

        Primitive64Store pAE = factory.make(2, 5);
        pAE.set(0, 0, 0.5);
        pAE.set(0, 1, 2.0);
        pAE.set(0, 2, 1.0);
        pAE.set(0, 3, 1.0);
        pAE.set(0, 4, 0.0);
        pAE.set(1, 0, 1.0);
        pAE.set(1, 1, 2.0);
        pAE.set(1, 2, 4.0);
        pAE.set(1, 3, 0.0);
        pAE.set(1, 4, 1.0);

        Primitive64Store pBE = factory.make(2, 1);
        pBE.set(0, 24.0);
        pBE.set(1, 60.0);

        primal.equalities(pAE, pBE);

        LinearSolver primalSolver = primal.build();
        // primalSolver.options.debug(LinearSolver.class);
        Result pRes = primalSolver.solve();
        Access1D<?> pMultipliers = factory.columns(pRes.getMultipliers().get());

        TestUtils.assertStateNotLessThanOptimal(pRes);
        // Negated since actual problem is max and algorithm expects min
        TestUtils.assertEquals(expOptVal, -pRes.getValue());
        for (int i = 0; i < expPrimSol.count(); i++) {
            TestUtils.assertEquals(expPrimSol.doubleValue(i), pRes.doubleValue(i));
        }
        for (int i = 0; i < expDualSol.count(); i++) {
            // Negated since actual problem is max and algorithm expects min
            TestUtils.assertEquals(expDualSol.doubleValue(i), -pMultipliers.doubleValue(i));
        }

        LinearSolver.StandardBuilder dual = LinearSolver.newStandardBuilder();

        Primitive64Store dC = factory.make(5, 1);
        dC.set(0, 24.0);
        dC.set(1, 60.0);
        dC.set(2, 0.0);
        dC.set(3, 0.0);
        dC.set(4, 0.0);

        dual.objective(dC);

        Primitive64Store dAE = factory.make(3, 5);
        dAE.set(0, 0, 0.5);
        dAE.set(0, 1, 1.0);
        dAE.set(0, 2, -1.0);
        dAE.set(0, 3, 0.0);
        dAE.set(0, 4, 0.0);
        dAE.set(1, 0, 2.0);
        dAE.set(1, 1, 2.0);
        dAE.set(1, 2, 0.0);
        dAE.set(1, 3, -1.0);
        dAE.set(1, 4, 0.0);
        dAE.set(2, 0, 1.0);
        dAE.set(2, 1, 4.0);
        dAE.set(2, 2, 0.0);
        dAE.set(2, 3, 0.0);
        dAE.set(2, 4, -1.0);

        Primitive64Store dBE = factory.make(3, 1);
        dBE.set(0, 6.0);
        dBE.set(1, 14.0);
        dBE.set(2, 13.0);

        dual.equalities(dAE, dBE);

        LinearSolver dualSolver = dual.build();
        // dualSolver.options.debug(LinearSolver.class);
        Result dRes = dualSolver.solve();
        Access1D<?> dMultipliers = factory.columns(dRes.getMultipliers().get());

        TestUtils.assertStateNotLessThanOptimal(dRes);
        TestUtils.assertEquals(expOptVal, dRes.getValue());
        for (int i = 0; i < expDualSol.count(); i++) {
            TestUtils.assertEquals(expDualSol.doubleValue(i), dRes.doubleValue(i));
        }
        for (int i = 0; i < expPrimSol.count(); i++) {
            TestUtils.assertEquals(expPrimSol.doubleValue(i), dMultipliers.doubleValue(i));
        }
    }

    @Test
    public void testInfeasibleCase() {

        Variable[] tmpVariables = { new Variable("X1").lower(ONE).weight(ONE), new Variable("X2").lower(ONE).weight(TWO),
                new Variable("X3").lower(ONE).weight(THREE) };

        ExpressionsBasedModel tmpModel = new ExpressionsBasedModel(tmpVariables);

        Expression tmpExprC1 = tmpModel.newExpression("C1");
        for (int i = 0; i < tmpModel.countVariables(); i++) {
            tmpExprC1.set(i, ONE);
        }
        tmpExprC1.upper(TWO);

        Optimisation.Result tmpResult = tmpModel.maximise();

        TestUtils.assertFalse(tmpResult.getState().isFeasible());

    }

    @Test
    public void testMaxOldKnapsackTestModel() {

        ExpressionsBasedModel tmpModel = LinearDesignTestCases.buildOldKnapsackTestModel();

        Optimisation.Result tmpResult = tmpModel.maximise();
        MatrixQ128 tmpSolution = MatrixQ128.FACTORY.columns(tmpResult);

        MatrixStore<Double> tmpExpX = Primitive64Store.FACTORY
                .rows(new double[][] { { 0.0 }, { 0.0 }, { 0.1846 }, { 0.0 }, { 0.0 }, { 0.0 }, { 0.8154 }, { 0.0 } });
        MatrixStore<Double> tmpActX = Primitive64Store.FACTORY.copy(tmpSolution.rows(0, 1, 2, 3, 4, 5, 6, 7));

        TestUtils.assertEquals(tmpExpX, tmpActX);
    }

    @Test
    public void testMinOldKnapsackTestModel() {

        ExpressionsBasedModel tmpModel = LinearDesignTestCases.buildOldKnapsackTestModel();

        Optimisation.Result tmpResult = tmpModel.minimise();
        MatrixQ128 tmpSolution = MatrixQ128.FACTORY.columns(tmpResult);

        MatrixStore<Double> tmpExpX = Primitive64Store.FACTORY
                .rows(new double[][] { { 0.0 }, { 0.8154 }, { 0.1846 }, { 0.0 }, { 0.0 }, { 0.0 }, { 0.0 }, { 0.0 } });
        MatrixStore<Double> tmpActX = Primitive64Store.FACTORY.copy(tmpSolution.rows(0, 1, 2, 3, 4, 5, 6, 7));

        TestUtils.assertEquals(tmpExpX, tmpActX);
    }

    /**
     * A specific node of {@linkplain org.ojalgo.optimisation.integer.IntegerProblems#testP20130409b}. Based
     * on some changes in ExpressionBasedModel and/or IntegerSolver some nodes started to fail as UNBOUNDED.
     * Which seems unreasonable. Must be a problem with either ExpressionBasedModel or LinearSolver. Test case
     * sent in by the user / problem reporter
     * <a href="http://bugzilla.optimatika.se/show_bug.cgi?id=178">BugZilla</a>
     */
    @Test
    public void testP20130409b() {

        Variable x1 = Variable.make("x1");
        Variable x2013 = Variable.make("x2013");
        Variable x2014 = Variable.make("x2014");
        Variable x2015 = Variable.make("x2015");
        //      x2013.setInteger(true);
        //      x2014.setInteger(true);
        //      x2015.setInteger(true);
        x2013.lower(BigDecimal.valueOf(1245L));
        x2014.lower(BigDecimal.valueOf(1269L));

        ExpressionsBasedModel model = new ExpressionsBasedModel();
        model.addVariable(x1);
        model.addVariable(x2013);
        model.addVariable(x2014);
        model.addVariable(x2015);

        Expression obj = model.newExpression("obj");
        obj.set(x1, 1);
        obj.weight(BigDecimal.valueOf(1));

        Expression c1 = model.newExpression("c1");
        c1.set(x1, 1);
        c1.lower(BigDecimal.valueOf(0));

        Expression c2 = model.newExpression("c2");
        c2.set(x2014, -5000);
        c2.set(x2013, 5100);
        c2.set(x1, -1);
        c2.upper(BigDecimal.valueOf(0));

        Expression c3 = model.newExpression("c3");
        c3.set(x2014, -5000);
        c3.set(x2013, 5100);
        c3.set(x1, 1);
        c3.lower(BigDecimal.valueOf(0));

        Expression c4 = model.newExpression("c4");
        c4.set(x2014, 150);
        c4.set(x2013, 5100);
        c4.set(x2015, -5000);
        c4.set(x1, -1);
        c4.upper(BigDecimal.valueOf(0));

        Expression c5 = model.newExpression("c5");
        c5.set(x2014, 150);
        c5.set(x2013, 5100);
        c5.set(x2015, -5000);
        c5.set(x1, 1);
        c5.lower(BigDecimal.valueOf(0));

        Expression c6 = model.newExpression("c6");
        c6.set(x2015, 5000);
        c6.set(x2014, 5000);
        c6.set(x2013, 5000);
        c6.level(BigDecimal.valueOf(19105000));

        ArrayR256 expected = ArrayR256.wrap(BigDecimal.valueOf(4850), BigDecimal.valueOf(1245), BigDecimal.valueOf(1269), BigDecimal.valueOf(1307));

        TestUtils.assertTrue("Expected Solution Not Valid", model.validate(expected));

        if (DEBUG) {
            model.options.debug(LinearSolver.class);
        }

        Result actual = model.minimise();

        if (DEBUG) {
            BasicLogger.debug("Expected: {}", expected);
            BasicLogger.debug("Actual: {}", actual);
        }

        TestUtils.assertStateNotLessThanOptimal(actual);

        TestUtils.assertTrue("Actual Solution Not Valid", model.validate(actual));

        TestUtils.assertEquals(expected, actual);
    }

    @Test
    public void testUnboundedCase() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x1 = model.newVariable("X1").weight(ONE);
        Variable x2 = model.newVariable("X2").weight(TWO);
        Variable x3 = model.newVariable("X3").weight(THREE);

        model.newExpression("C1").set(x1, ONE).set(x2, ONE).set(x3, ONE).level(ONE);

        if (DEBUG) {
            model.options.debug(LinearSolver.class);
        }

        Optimisation.Result expected = Optimisation.Result.of(1, State.UNBOUNDED, 1, 0, 0);

        Optimisation.Result actualMin = model.minimise();
        TestUtils.assertEquals(expected.getState(), actualMin.getState());

        Optimisation.Result actualMax = model.maximise();
        TestUtils.assertEquals(expected.getState(), actualMax.getState());
    }

}

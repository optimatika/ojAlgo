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
package org.ojalgo.optimisation.linear;

import static org.ojalgo.constant.BigMath.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.access.Access1D;
import org.ojalgo.array.BigArray;
import org.ojalgo.constant.BigMath;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.matrix.BasicMatrix;
import org.ojalgo.matrix.RationalMatrix;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PhysicalStore.Factory;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.optimisation.Optimisation.State;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.type.context.NumberContext;

public class LinearDesignTestCases {

    /**
     * http://math.uww.edu/~mcfarlat/s-prob.htm
     */
    @Test
    public void test1LinearModelCase() {

        final Variable[] tmpVariables = new Variable[] { new Variable("X1").lower(ZERO).weight(ONE), new Variable("X2").lower(ZERO).weight(TWO),
                new Variable("X3").lower(ZERO).weight(ONE.negate()) };

        final ExpressionsBasedModel tmpModel = new ExpressionsBasedModel(tmpVariables);

        final Expression retExprC1 = tmpModel.addExpression("C1");
        for (int i = 0; i < tmpModel.countVariables(); i++) {
            retExprC1.set(i, new BigDecimal[] { TWO, ONE, ONE }[i]);
        }
        retExprC1.upper(new BigDecimal("14.0"));

        final Expression tmpExprC2 = tmpModel.addExpression("C2");
        for (int i = 0; i < tmpModel.countVariables(); i++) {
            tmpExprC2.set(i, new BigDecimal[] { FOUR, TWO, THREE }[i]);
        }
        tmpExprC2.upper(new BigDecimal("28.0"));

        final Expression tmpExprC3 = tmpModel.addExpression("C3");
        for (int i = 0; i < tmpModel.countVariables(); i++) {
            tmpExprC3.set(i, new BigDecimal[] { TWO, FIVE, FIVE }[i]);
        }
        tmpExprC3.upper(new BigDecimal("30.0"));

        final Optimisation.Result tmpResult = tmpModel.maximise();
        final BasicMatrix tmpSolution = RationalMatrix.FACTORY.columns(tmpResult);

        final PhysicalStore<Double> tmpExpX = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 5.0 }, { 4.0 }, { 0.0 } });
        final PhysicalStore<Double> tmpActX = PrimitiveDenseStore.FACTORY.copy(tmpSolution.selectRows(new int[] { 0, 1, 2 }));

        TestUtils.assertEquals(tmpExpX, tmpActX);
    }

    /**
     * http://www.stats.ox.ac.uk/~yu/4_Simplex_II.pdf
     */
    @Test
    public void test2LinearModelCase() {

        final Variable[] tmpVariables = new Variable[] { new Variable("X1").lower(ZERO).weight(THREE), new Variable("X2").lower(ZERO).weight(ZERO),
                new Variable("X3").lower(ZERO).weight(ONE) };

        final ExpressionsBasedModel tmpModel = new ExpressionsBasedModel(tmpVariables);

        final Expression tmpExprC1 = tmpModel.addExpression("C1");
        for (int i = 0; i < tmpModel.countVariables(); i++) {
            tmpExprC1.set(i, new BigDecimal[] { ONE, TWO, ONE }[i]);
        }
        tmpExprC1.level(TEN);

        final Expression tmpExprC2 = tmpModel.addExpression("C2");
        for (int i = 0; i < tmpModel.countVariables(); i++) {
            tmpExprC2.set(i, new BigDecimal[] { ONE, TWO.negate(), TWO }[i]);
        }
        tmpExprC2.level(SIX);

        final Optimisation.Result tmpResult = tmpModel.maximise();
        final BasicMatrix tmpSolution = RationalMatrix.FACTORY.columns(tmpResult);

        final PhysicalStore<Double> tmpExpX = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 8.0 }, { 1.0 }, { 0.0 } });
        final PhysicalStore<Double> tmpActX = PrimitiveDenseStore.FACTORY.copy(tmpSolution.selectRows(new int[] { 0, 1, 2 }));

        TestUtils.assertEquals(tmpExpX, tmpActX);
    }

    /**
     * http://optlab-server.sce.carleton.ca/POAnimations/TwoPhase.aspx
     */
    @Test
    public void test3LinearModelCase() {

        final Variable[] tmpVariables = new Variable[] { new Variable("X1").lower(ZERO).weight(TEN.add(FIVE)), new Variable("X2").lower(ZERO).weight(TEN) };

        final ExpressionsBasedModel tmpModel = new ExpressionsBasedModel(tmpVariables);

        final Expression tmpExprC1 = tmpModel.addExpression("C1");
        for (int i = 0; i < tmpModel.countVariables(); i++) {
            tmpExprC1.set(i, new BigDecimal[] { ONE, ZERO }[i]);
        }
        tmpExprC1.upper(TWO);

        final Expression tmpExprC2 = tmpModel.addExpression("C2");
        for (int i = 0; i < tmpModel.countVariables(); i++) {
            tmpExprC2.set(i, new BigDecimal[] { ZERO, ONE }[i]);
        }
        tmpExprC2.upper(THREE);

        final Expression tmpExprC3 = tmpModel.addExpression("C3");
        for (int i = 0; i < tmpModel.countVariables(); i++) {
            tmpExprC3.set(i, new BigDecimal[] { ONE, ONE }[i]);
        }
        tmpExprC3.level(FOUR);

        final Optimisation.Result tmpResult = tmpModel.maximise();
        final BasicMatrix tmpSolution = RationalMatrix.FACTORY.columns(tmpResult);

        final PhysicalStore<Double> tmpExpX = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 2.0 }, { 2.0 } });
        final PhysicalStore<Double> tmpActX = PrimitiveDenseStore.FACTORY.copy(tmpSolution.selectRows(new int[] { 0, 1 }));

        TestUtils.assertEquals(tmpExpX, tmpActX);
    }

    /**
     * http://www.saintmarys.edu/~psmith/338act14.html
     */
    @Test
    public void test4LinearModelCase() {

        final Variable[] tmpVariables = new Variable[] { new Variable("X1").lower(ZERO).weight(ONE.negate()), new Variable("X2").lower(ZERO).weight(ONE),
                new Variable("X3").lower(ZERO).weight(ZERO) };

        final ExpressionsBasedModel tmpModel = new ExpressionsBasedModel(tmpVariables);

        final Expression tmpExprC1 = tmpModel.addExpression("C1");
        for (int i = 0; i < tmpModel.countVariables(); i++) {
            tmpExprC1.set(i, new BigDecimal[] { SIX, ONE.negate(), ZERO }[i]);
        }
        tmpExprC1.upper(TEN);

        final Expression tmpExprC2 = tmpModel.addExpression("C2");
        for (int i = 0; i < tmpModel.countVariables(); i++) {
            tmpExprC2.set(i, new BigDecimal[] { ONE, FIVE, ZERO }[i]);
        }
        tmpExprC2.lower(FOUR);

        final Expression tmpExprC3 = tmpModel.addExpression("C3");
        for (int i = 0; i < tmpModel.countVariables(); i++) {
            tmpExprC3.set(i, new BigDecimal[] { ONE, FIVE, ONE }[i]);
        }
        tmpExprC3.level(FIVE);

        final Optimisation.Result tmpResult = tmpModel.minimise();
        final BasicMatrix tmpSolution = RationalMatrix.FACTORY.columns(tmpResult);

        final PhysicalStore<Double> tmpExpX = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 1.74 }, { 0.45 }, { 1.0 } });
        final PhysicalStore<Double> tmpActX = PrimitiveDenseStore.FACTORY.copy(tmpSolution.selectRows(new int[] { 0, 1, 2 }));
        tmpActX.modifyAll(new NumberContext(7, 2).getFunction(PrimitiveFunction.getSet()));

        TestUtils.assertEquals(tmpExpX, tmpActX);
    }

    /**
     * http://www.maths.bris.ac.uk/~maxmr/opt/sm2.pdf
     */
    @Test
    public void test5LinearModelCase() {

        final Variable[] tmpVariables = new Variable[] { new Variable("X1").lower(ZERO).weight(TWO), new Variable("X2").lower(ZERO).weight(THREE) };

        final ExpressionsBasedModel tmpModel = new ExpressionsBasedModel(tmpVariables);

        final Expression tmpExprC1 = tmpModel.addExpression("C1");
        for (int i = 0; i < tmpModel.countVariables(); i++) {
            tmpExprC1.set(i, new BigDecimal[] { HALF, QUARTER }[i]);
        }
        tmpExprC1.upper(FOUR);

        final Expression tmpExprC2 = tmpModel.addExpression("C2");
        for (int i = 0; i < tmpModel.countVariables(); i++) {
            tmpExprC2.set(i, new BigDecimal[] { ONE, THREE }[i]);
        }
        tmpExprC2.lower(TEN.add(TEN));

        final Expression tmpExprC3 = tmpModel.addExpression("C3");
        for (int i = 0; i < tmpModel.countVariables(); i++) {
            tmpExprC3.set(i, new BigDecimal[] { ONE, ONE }[i]);
        }
        tmpExprC3.level(TEN);

        final Optimisation.Result tmpResult = tmpModel.minimise();
        final BasicMatrix tmpSolution = RationalMatrix.FACTORY.columns(tmpResult);

        final PhysicalStore<Double> tmpExpX = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 5.0 }, { 5.0 } });
        final PhysicalStore<Double> tmpActX = PrimitiveDenseStore.FACTORY.copy(tmpSolution.selectRows(new int[] { 0, 1 }));

        TestUtils.assertEquals(tmpExpX, tmpActX);
    }

    /**
     * Simplest possible unbounded model
     */
    @Test
    public void test6LinearModelCase() {

        final Variable[] tmpVariables = new Variable[] { new Variable("X1").lower(ZERO).weight(ONE), new Variable("X2").lower(ZERO).weight(TWO),
                new Variable("X3").lower(ZERO).weight(THREE) };

        final ExpressionsBasedModel tmpModel = new ExpressionsBasedModel(tmpVariables);

        final Optimisation.Result tmpResult = tmpModel.maximise();

        TestUtils.assertEquals(State.UNBOUNDED, tmpResult.getState());
    }

    /**
     * http://math.uww.edu/~mcfarlat/ns-prob.htm
     */
    @Test
    public void test7LinearModelCase() {

        final Variable[] tmpVariables = new Variable[] { new Variable("X1").lower(ZERO).weight(TWO), new Variable("X2").lower(ZERO).weight(THREE) };

        final ExpressionsBasedModel tmpModel = new ExpressionsBasedModel(tmpVariables);

        final Expression tmpExprC1 = tmpModel.addExpression("C1");
        for (int i = 0; i < tmpModel.countVariables(); i++) {
            tmpExprC1.set(i, new BigDecimal[] { ONE, ONE }[i]);
        }
        tmpExprC1.upper(TEN);

        final Expression tmpExprC2 = tmpModel.addExpression("C2");
        for (int i = 0; i < tmpModel.countVariables(); i++) {
            tmpExprC2.set(i, new BigDecimal[] { ONE, TWO }[i]);
        }
        tmpExprC2.lower(TWELVE);

        final Expression tmpExprC3 = tmpModel.addExpression("C3");
        for (int i = 0; i < tmpModel.countVariables(); i++) {
            tmpExprC3.set(i, new BigDecimal[] { TWO, ONE }[i]);
        }
        tmpExprC3.lower(TWELVE);

        final Optimisation.Result tmpResult = tmpModel.minimise();
        final BasicMatrix tmpSolution = RationalMatrix.FACTORY.columns(tmpResult);

        final PhysicalStore<Double> tmpExpX = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 4.0 }, { 4.0 } });
        final PhysicalStore<Double> tmpActX = PrimitiveDenseStore.FACTORY.copy(tmpSolution.selectRows(new int[] { 0, 1 }));

        TestUtils.assertEquals(tmpExpX, tmpActX);
    }

    /**
     * http://books.google.com/books?id=3iZznGJq4ZMC page 192
     */
    @Test
    public void test8LinearModelCase() {

        final Variable[] tmpVariables = new Variable[] { new Variable("X1").lower(ZERO).weight(ONE), new Variable("X2").lower(ZERO).weight(TWO),
                new Variable("X3").lower(ZERO).weight(ONE) };

        final ExpressionsBasedModel tmpModel = new ExpressionsBasedModel(tmpVariables);

        final Expression tmpExprC1 = tmpModel.addExpression("C1");
        for (int i = 0; i < tmpModel.countVariables(); i++) {
            tmpExprC1.set(i, new BigDecimal[] { THREE, ONE, NEG }[i]);
        }
        tmpExprC1.level(TEN.add(FIVE));

        final Expression tmpExprC2 = tmpModel.addExpression("C2");
        for (int i = 0; i < tmpModel.countVariables(); i++) {
            tmpExprC2.set(i, new BigDecimal[] { EIGHT, FOUR, NEG }[i]);
        }
        tmpExprC2.level(FIVE.multiply(TEN));

        final Expression tmpExprC3 = tmpModel.addExpression("C3");
        for (int i = 0; i < tmpModel.countVariables(); i++) {
            tmpExprC3.set(i, new BigDecimal[] { TWO, TWO, ONE }[i]);
        }
        tmpExprC3.level(TEN.add(TEN));

        final Optimisation.Result tmpResult = tmpModel.maximise();

        TestUtils.assertTrue(tmpResult.getState().isFeasible());
    }

    /**
     * http://web.mit.edu/15.053/www/AMP-Chapter-04.pdf
     * https://web.fe.up.pt/~mac/ensino/docs/OT20122013/Chapter%204%20-%20Duality%20in%20Linear%20Programming.pdf
     */
    @Test
    public void testDuality() {

        final Factory<Double, PrimitiveDenseStore> factory = PrimitiveDenseStore.FACTORY;

        final PrimitiveDenseStore expPrimSol = factory.rows(new double[] { 36.0, 0.0, 6.0 });
        final PrimitiveDenseStore expDualSol = factory.rows(new double[] { 11.0, 0.5 });
        final double expOptVal = 294.0;

        final LinearSolver.Builder primal = LinearSolver.getBuilder();

        // Negated since actual problem is max and algorithm expects min
        final PrimitiveDenseStore pC = factory.makeZero(5, 1);
        pC.set(0, -6.0);
        pC.set(1, -14.0);
        pC.set(2, -13.0);
        pC.set(3, 0);
        pC.set(4, 0);

        primal.objective(pC);

        final PrimitiveDenseStore pAE = factory.makeZero(2, 5);
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

        final PrimitiveDenseStore pBE = factory.makeZero(2, 1);
        pBE.set(0, 24.0);
        pBE.set(1, 60.0);

        primal.equalities(pAE, pBE);

        final LinearSolver primalSolver = primal.build();
        // primalSolver.options.debug(LinearSolver.class);
        final Result pRes = primalSolver.solve();
        final Access1D<?> pMultipliers = factory.columns(pRes.getMultipliers().get());

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

        final LinearSolver.Builder dual = LinearSolver.getBuilder();

        final PrimitiveDenseStore dC = factory.makeZero(5, 1);
        dC.set(0, 24.0);
        dC.set(1, 60.0);
        dC.set(2, 0.0);
        dC.set(3, 0.0);
        dC.set(4, 0.0);

        dual.objective(dC);

        final PrimitiveDenseStore dAE = factory.makeZero(3, 5);
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

        final PrimitiveDenseStore dBE = factory.makeZero(3, 1);
        dBE.set(0, 6.0);
        dBE.set(1, 14.0);
        dBE.set(2, 13.0);

        dual.equalities(dAE, dBE);

        final LinearSolver dualSolver = dual.build();
        // dualSolver.options.debug(LinearSolver.class);
        final Result dRes = dualSolver.solve();
        final Access1D<?> dMultipliers = factory.columns(dRes.getMultipliers().get());

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

        final Variable[] tmpVariables = new Variable[] { new Variable("X1").lower(ONE).weight(ONE), new Variable("X2").lower(ONE).weight(TWO),
                new Variable("X3").lower(ONE).weight(THREE) };

        final ExpressionsBasedModel tmpModel = new ExpressionsBasedModel(tmpVariables);

        final Expression tmpExprC1 = tmpModel.addExpression("C1");
        for (int i = 0; i < tmpModel.countVariables(); i++) {
            tmpExprC1.set(i, ONE);
        }
        tmpExprC1.upper(TWO);

        final Optimisation.Result tmpResult = tmpModel.maximise();

        TestUtils.assertFalse(tmpResult.getState().isFeasible());

    }

    @Test
    public void testMaxOldKnapsackTestModel() {

        final ExpressionsBasedModel tmpModel = this.buildOldKnapsackTestModel();

        final Optimisation.Result tmpResult = tmpModel.maximise();
        final BasicMatrix tmpSolution = RationalMatrix.FACTORY.columns(tmpResult);

        final MatrixStore<Double> tmpExpX = PrimitiveDenseStore.FACTORY
                .rows(new double[][] { { 0.0 }, { 0.0 }, { 0.1846 }, { 0.0 }, { 0.0 }, { 0.0 }, { 0.8154 }, { 0.0 } });
        final MatrixStore<Double> tmpActX = PrimitiveDenseStore.FACTORY.copy(tmpSolution.selectRows(new int[] { 0, 1, 2, 3, 4, 5, 6, 7 }));

        TestUtils.assertEquals(tmpExpX, tmpActX);
    }

    @Test
    public void testMinOldKnapsackTestModel() {

        final ExpressionsBasedModel tmpModel = this.buildOldKnapsackTestModel();

        final Optimisation.Result tmpResult = tmpModel.minimise();
        final BasicMatrix tmpSolution = RationalMatrix.FACTORY.columns(tmpResult);

        final MatrixStore<Double> tmpExpX = PrimitiveDenseStore.FACTORY
                .rows(new double[][] { { 0.0 }, { 0.8154 }, { 0.1846 }, { 0.0 }, { 0.0 }, { 0.0 }, { 0.0 }, { 0.0 } });
        final MatrixStore<Double> tmpActX = PrimitiveDenseStore.FACTORY.copy(tmpSolution.selectRows(new int[] { 0, 1, 2, 3, 4, 5, 6, 7 }));

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

        final Variable x1 = Variable.make("x1");
        final Variable x2013 = Variable.make("x2013");
        final Variable x2014 = Variable.make("x2014");
        final Variable x2015 = Variable.make("x2015");
        //      x2013.setInteger(true);
        //      x2014.setInteger(true);
        //      x2015.setInteger(true);
        x2013.lower(BigDecimal.valueOf(1245L));
        x2014.lower(BigDecimal.valueOf(1269L));

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

        // tmpModel.options.debug(LinearSolver.class);
        //tmpModel.options.problem = NumberContext.getGeneral(8);

        final Result tmpResult = tmpModel.minimise();

        // BasicLogger.debug(tmpResult.toString());

        TestUtils.assertEquals("Solution Not Correct", tmpExpSol, tmpResult, new NumberContext(8, 8));
        TestUtils.assertTrue("Solver State Not Optimal", tmpResult.getState().isOptimal());
    }

    @Test
    public void testUnboundedCase() {

        final Variable[] tmpVariables = new Variable[] { new Variable("X1").weight(ONE), new Variable("X2").weight(TWO), new Variable("X3").weight(THREE) };

        final ExpressionsBasedModel tmpModel = new ExpressionsBasedModel(tmpVariables);

        final Expression tmpExprC1 = tmpModel.addExpression("C1");
        for (int i = 0; i < tmpModel.countVariables(); i++) {
            tmpExprC1.set(i, ONE);
        }
        tmpExprC1.level(ONE);

        final Optimisation.Result tmpMinResult = tmpModel.maximise();

        TestUtils.assertTrue(tmpMinResult.getState().isFeasible());
        TestUtils.assertFalse(tmpMinResult.getState().isOptimal());
        TestUtils.assertTrue(tmpMinResult.getState().isFailure());
        TestUtils.assertTrue(tmpModel.validate(tmpMinResult));
        TestUtils.assertEquals(Optimisation.State.UNBOUNDED, tmpMinResult.getState());

        final Optimisation.Result tmpMaxResult = tmpModel.maximise();

        TestUtils.assertTrue(tmpMaxResult.getState().isFeasible());
        TestUtils.assertFalse(tmpMaxResult.getState().isOptimal());
        TestUtils.assertTrue(tmpMaxResult.getState().isFailure());
        TestUtils.assertTrue(tmpModel.validate(tmpMaxResult));
        TestUtils.assertEquals(Optimisation.State.UNBOUNDED, tmpMaxResult.getState());
    }

    private ExpressionsBasedModel buildOldKnapsackTestModel() {

        Variable tmpVar;
        final Variable[] tmpVariables = new Variable[8];

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

        final ExpressionsBasedModel retVal = new ExpressionsBasedModel(tmpVariables);
        final int tmpLength = retVal.countVariables();

        final Expression retVal2 = retVal.addExpression("100%");

        for (int i = 0; i < tmpLength; i++) {
            retVal2.set(i, ONE);
        }
        final Expression retVal1 = retVal2;

        retVal1.lower(BigMath.ONE);
        retVal1.upper(BigMath.ONE);

        return retVal;
    }

}

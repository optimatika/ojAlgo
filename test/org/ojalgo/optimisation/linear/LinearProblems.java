/*
 * Copyright 1997-2019 Optimatika
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
import org.ojalgo.array.BigArray;
import org.ojalgo.function.constant.BigMath;
import org.ojalgo.matrix.PrimitiveMatrix;
import org.ojalgo.matrix.RationalMatrix;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.optimisation.Optimisation.State;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.optimisation.integer.OptimisationIntegerData;
import org.ojalgo.optimisation.integer.P20150127b;
import org.ojalgo.type.TypeUtils;
import org.ojalgo.type.context.NumberContext;

public class LinearProblems extends OptimisationLinearTests {

    /**
     * https://github.com/optimatika/ojAlgo/issues/117 <br>
     * Problem was getting different state after second solve.
     */
    @Test
    public void precisionTestDoubleRunInfeasible() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();
        Variable d35 = model.addVariable("d35");
        Variable d56 = model.addVariable("d56");
        Variable d57 = model.addVariable("d57");

        // d35 = 0.0;
        d35.level(0.0);

        // d56 - d35 = -2000.0400000000002;
        Expression expression1 = model.addExpression("d56 - d35");
        expression1.set(d56, 1.0);
        expression1.set(d35, -1.0);
        expression1.level(-2000.0400000000002);

        // d57 - d56 = 0.0;
        Expression expression2 = model.addExpression("d57 - d56");
        expression2.set(d57, 1.0);
        expression2.set(d56, -1.0);
        expression2.level(0.0);

        // minimize: 1.0 d57;
        d57.weight(1.0);

        Optimisation.Result result1 = model.minimise();

        TestUtils.assertStateNotLessThanOptimal(result1);

        Optimisation.Result result2 = model.minimise();

        TestUtils.assertStateAndSolution(result1, result2);
    }

    @Test
    public void testMath286() {

        Variable tmpX1 = new Variable("X1").weight(TENTH.multiply(EIGHT)).lower(TEN);
        Variable tmpX2 = new Variable("X2").weight(TENTH.multiply(TWO)).lower(ZERO);
        Variable tmpX3 = new Variable("X3").weight(TENTH.multiply(SEVEN)).lower(EIGHT);
        Variable tmpX4 = new Variable("X4").weight(TENTH.multiply(THREE)).lower(ZERO);
        Variable tmpX5 = new Variable("X5").weight(TENTH.multiply(SIX)).lower(FIVE);
        Variable tmpX6 = new Variable("X6").weight(TENTH.multiply(FOUR)).lower(ZERO);

        Variable[] tmpFullVars = new Variable[] { tmpX1.copy(), tmpX2.copy(), tmpX3.copy(), tmpX4.copy(), tmpX5.copy(), tmpX6.copy() };
        Variable[] tmpOddVars = new Variable[] { tmpX1.copy(), tmpX3.copy(), tmpX5.copy() };
        Variable[] tmpEvenVars = new Variable[] { tmpX2.copy(), tmpX4.copy(), tmpX6.copy() };

        ExpressionsBasedModel tmpFullModel = new ExpressionsBasedModel(tmpFullVars);
        //tmpFullModel.setMaximisation();

        ExpressionsBasedModel tmpOddModel = new ExpressionsBasedModel(tmpOddVars);
        //tmpOddModel.setMaximisation();

        ExpressionsBasedModel tmpEvenModel = new ExpressionsBasedModel(tmpEvenVars);
        //tmpEvenModel.setMaximisation();

        //        tmpFullModel.options.debug(LinearSolver.class);
        //        tmpOddModel.options.debug(LinearSolver.class);
        //        tmpEvenModel.options.debug(LinearSolver.class);

        BigDecimal tmpRHS = new BigDecimal("23.0");
        int tmpLength = tmpFullModel.countVariables();

        Expression retVal = tmpFullModel.addExpression("C1");

        for (int i = 0; i < tmpLength; i++) {
            retVal.set(i, new BigDecimal[] { ONE, ZERO, ONE, ZERO, ONE, ZERO }[i]);
        }

        Expression tmpAddWeightExpression = retVal;
        tmpAddWeightExpression.level(tmpRHS);
        int tmpLength1 = tmpOddModel.countVariables();

        Expression retVal1 = tmpOddModel.addExpression("C1");

        for (int i = 0; i < tmpLength1; i++) {
            retVal1.set(i, new BigDecimal[] { ONE, ONE, ONE }[i]);
        }
        Expression tmpAddWeightExpression2 = retVal1;
        tmpAddWeightExpression2.level(tmpRHS);
        int tmpLength2 = tmpFullModel.countVariables();

        Expression retVal2 = tmpFullModel.addExpression("C2");

        for (int i = 0; i < tmpLength2; i++) {
            retVal2.set(i, new BigDecimal[] { ZERO, ONE, ZERO, ONE, ZERO, ONE }[i]);
        }

        Expression tmpAddWeightExpression3 = retVal2;
        tmpAddWeightExpression3.level(tmpRHS);
        int tmpLength3 = tmpEvenModel.countVariables();

        Expression retVal3 = tmpEvenModel.addExpression("C2");

        for (int i = 0; i < tmpLength3; i++) {
            retVal3.set(i, new BigDecimal[] { ONE, ONE, ONE }[i]);
        }
        Expression tmpAddWeightExpression4 = retVal3;
        tmpAddWeightExpression4.level(tmpRHS);

        Expression tmpFullObjective = tmpFullModel.objective();
        Expression tmpOddObjective = tmpOddModel.objective();
        Expression tmpEvenObjective = tmpEvenModel.objective();

        // A valid solution of 25.8 can be produced with:
        // X1=10, X2=0, X3=8, X4=0, X5=5, X6=23
        BigDecimal tmpClaimedValue = new BigDecimal("25.8");
        PrimitiveMatrix.DenseReceiver tmpBuilder = PrimitiveMatrix.FACTORY.makeDense(6, 1);
        tmpBuilder.set(0, 0, 10);
        tmpBuilder.set(2, 0, 8);
        tmpBuilder.set(4, 0, 5);
        tmpBuilder.set(5, 0, 23);
        PrimitiveMatrix tmpFullSolution = tmpBuilder.build();
        int[] someRows = { 0, 2, 4 };
        PrimitiveMatrix tmpOddSolution = tmpFullSolution.logical().rows(someRows).get();
        int[] someRows1 = { 1, 3, 5 };
        PrimitiveMatrix tmpEvenSolution = tmpFullSolution.logical().rows(someRows1).get();
        TestUtils.assertEquals("Claimed solution not valid!", true, tmpFullModel.validate(BigArray.FACTORY.copy(tmpFullSolution), new NumberContext(7, 6)));
        Double tmpActualValue = tmpFullObjective.toFunction().invoke(PrimitiveDenseStore.FACTORY.copy(tmpFullSolution));
        //  BigDecimal tmpActualValue = TypeUtils.toBigDecimal(tmpObjectiveValue);
        //JUnitUtils.assertEquals("Claimed objective value wrong!", 0, tmpClaimedValue.compareTo(tmpActualValue));
        TestUtils.assertEquals(tmpClaimedValue, tmpActualValue, new NumberContext(7, 6));

        // Start validating ojAlgo results

        Optimisation.Result tmpEvenResult = tmpEvenModel.maximise();
        Optimisation.Result tmpOddResult = tmpOddModel.maximise();
        Optimisation.Result tmpFullResult = tmpFullModel.maximise();

        TestUtils.assertEquals(true, tmpEvenModel.validate(tmpEvenResult, new NumberContext(7, 6)));
        TestUtils.assertEquals(true, tmpOddModel.validate(tmpOddResult, new NumberContext(7, 6)));
        TestUtils.assertEquals(true, tmpFullModel.validate(tmpFullResult, new NumberContext(7, 6)));
        int[] someRows2 = { 0, 1, 2 };

        TestUtils.assertEquals(tmpEvenSolution, RationalMatrix.FACTORY.columns(tmpEvenResult).logical().rows(someRows2).get(), new NumberContext(7, 6));
        int[] someRows3 = { 0, 1, 2 };
        TestUtils.assertEquals(tmpOddSolution, RationalMatrix.FACTORY.columns(tmpOddResult).logical().rows(someRows3).get(), new NumberContext(7, 6));
        int[] someRows4 = { 0, 1, 2, 3, 4, 5 };
        TestUtils.assertEquals(tmpFullSolution, RationalMatrix.FACTORY.columns(tmpFullResult).logical().rows(someRows4).get(), new NumberContext(7, 6));
        int[] someRows5 = { 0, 1, 2 };

        BigDecimal tmpEvenValue = new NumberContext(7, 6).enforce(TypeUtils.toBigDecimal(tmpEvenObjective.toFunction()
                .invoke(PrimitiveDenseStore.FACTORY.copy(PrimitiveMatrix.FACTORY.columns(tmpEvenResult).logical().rows(someRows5).get()))));
        int[] someRows6 = { 0, 1, 2 };
        BigDecimal tmpOddValue = new NumberContext(7, 6).enforce(TypeUtils.toBigDecimal(tmpOddObjective.toFunction()
                .invoke(PrimitiveDenseStore.FACTORY.copy(PrimitiveMatrix.FACTORY.columns(tmpOddResult).logical().rows(someRows6).get()))));
        int[] someRows7 = { 0, 1, 2, 3, 4, 5 };
        BigDecimal tmpFullValue = new NumberContext(7, 6).enforce(TypeUtils.toBigDecimal(tmpFullObjective.toFunction()
                .invoke(PrimitiveDenseStore.FACTORY.copy(PrimitiveMatrix.FACTORY.columns(tmpFullResult).logical().rows(someRows7).get()))));

        TestUtils.assertEquals(0, tmpFullValue.compareTo(tmpEvenValue.add(tmpOddValue)));
        TestUtils.assertEquals(0, tmpClaimedValue.compareTo(tmpFullValue));
    }

    /**
     * Didn't recognise this as an infeasible problem.
     */
    @Test
    public void testP20100412() {

        ExpressionsBasedModel tmpModel = OptimisationIntegerData.buildModelForP20100412().relax(true);
        //tmpModel.relax(); // Relax the integer constraints
        tmpModel.getVariable(1).lower(ONE); // Set branch state

        State tmpResultState = tmpModel.maximise().getState();

        TestUtils.assertFalse("Should be INFEASIBLE", tmpResultState == State.FEASIBLE);
    }

    /**
     * Depending on how the constraints were constructed the solver could fail to solve and report the problem
     * to be unbounded.
     */
    @Test
    public void testP20111010() {

        Variable[] tmpVariables = new Variable[] { new Variable("X").lower(ZERO).weight(ONE), new Variable("Y").lower(ZERO).weight(ZERO),
                new Variable("Z").lower(ZERO).weight(ZERO) };
        ExpressionsBasedModel tmpModel = new ExpressionsBasedModel(tmpVariables);

        Expression tmpExprC1 = tmpModel.addExpression("C1");
        tmpExprC1.level(ZERO);
        tmpExprC1.set(0, ONE);

        Expression tmpExprC2 = tmpModel.addExpression("C2");
        tmpExprC2.level(ZERO);
        tmpExprC2.set(0, ONE);
        tmpExprC2.set(1, NEG);

        Expression tmpExprC3 = tmpModel.addExpression("C3");
        tmpExprC3.level(ZERO);
        tmpExprC3.set(0, ONE);
        tmpExprC3.set(2, NEG);

        PrimitiveMatrix tmpExpectedSolution = PrimitiveMatrix.FACTORY.makeZero(3, 1);

        Optimisation.Result tmpResult11 = tmpModel.minimise();
        //TestUtils.assertEquals(tmpExpectedState, tmpResult11.getState());
        TestUtils.assertStateNotLessThanOptimal(tmpResult11);
        TestUtils.assertEquals(tmpExpectedSolution, RationalMatrix.FACTORY.columns(tmpResult11));

        tmpExprC2.set(0, NEG);
        tmpExprC2.set(1, ONE);

        tmpExprC3.set(0, ONE);
        tmpExprC3.set(2, NEG);

        Optimisation.Result tmpResultN1 = tmpModel.minimise();
        //TestUtils.assertEquals(tmpExpectedState, tmpResultN1.getState());
        TestUtils.assertStateNotLessThanOptimal(tmpResultN1);
        TestUtils.assertEquals(tmpExpectedSolution, RationalMatrix.FACTORY.columns(tmpResultN1));

        tmpExprC2.set(0, ONE);
        tmpExprC2.set(1, NEG);

        tmpExprC3.set(0, NEG);
        tmpExprC3.set(2, ONE);

        Optimisation.Result tmpResult1N = tmpModel.minimise();
        //TestUtils.assertEquals(tmpExpectedState, tmpResult1N.getState());
        TestUtils.assertStateNotLessThanOptimal(tmpResult1N);
        TestUtils.assertEquals(tmpExpectedSolution, RationalMatrix.FACTORY.columns(tmpResult1N));

        tmpExprC2.set(0, NEG);
        tmpExprC2.set(1, ONE);

        tmpExprC3.set(0, NEG);
        tmpExprC3.set(2, ONE);

        Optimisation.Result tmpResultNN = tmpModel.minimise();
        //TestUtils.assertEquals(tmpExpectedState, tmpResultNN.getState());
        TestUtils.assertStateNotLessThanOptimal(tmpResultNN);
        TestUtils.assertEquals(tmpExpectedSolution, RationalMatrix.FACTORY.columns(tmpResultNN));
    }

    /**
     * Problemet var att en av noderna som IntegerSolver genererade var infeasible, men det misslyckades
     * LinearSolver med att identifiera och returnerade en felaktig lösning som OPTIMAL. Detta testfall
     * motsvarar
     */
    @Test
    public void testP20150127() {

        ExpressionsBasedModel tmpModel = P20150127b.getModel(true, true);

        // tmpModel.options.debug(LinearSolver.class);
        // Kan få testfallet att gå igenom, men dåsmäller andra testfall
        // tmpModel.options.objective = tmpModel.options.objective.newScale(8);

        Result tmpResult = tmpModel.minimise();

        TestUtils.assertStateLessThanFeasible(tmpResult); // Should be infeasible
        TestUtils.assertFalse(tmpModel.validate(tmpResult));
    }

    /**
     * https://github.com/optimatika/ojAlgo/issues/61
     */
    @Test
    public void testP20180310_61() {

        Variable x = Variable.make("x").lower(0);
        Variable y = Variable.make("y").lower(0);

        ExpressionsBasedModel model = new ExpressionsBasedModel();
        model.addVariable(x);
        model.addVariable(y);

        model.addExpression("first").set(x, 2).set(y, 3).upper(1);
        model.addExpression("second").set(x, -2).set(y, 3).lower(1);

        BigArray expected = BigArray.wrap(BigMath.ZERO, BigMath.THIRD);

        Optimisation.Result result = model.maximise();
        TestUtils.assertEquals(expected, result);
        TestUtils.assertStateNotLessThanOptimal(result);

    }

    /**
     * https://github.com/optimatika/ojAlgo/issues/62
     */
    @Test
    public void testP20180310_62() {

        Variable x = Variable.make("x").lower(0).weight(1);
        Variable y = Variable.make("y").lower(0).weight(0);

        ExpressionsBasedModel model = new ExpressionsBasedModel();
        model.addVariable(x);
        model.addVariable(y);

        model.addExpression("first").set(x, 0).set(y, 1).lower(1);
        model.addExpression("second").set(x, 0).set(y, 1).upper(-1);

        TestUtils.assertEquals(Optimisation.State.INFEASIBLE, model.maximise().getState());

    }

    /**
     * https://github.com/optimatika/ojAlgo/issues/64
     */
    @Test
    public void testP20180311_64() {

        Variable x = Variable.make("x").lower(0).weight(3);
        Variable y = Variable.make("y").lower(0).weight(-2);

        ExpressionsBasedModel model = new ExpressionsBasedModel();
        model.addVariable(x);
        model.addVariable(y);

        model.addExpression().set(x, -1).set(y, 0).lower(0);
        model.addExpression().set(x, -1).set(y, 3).level(2);

        BigArray expected = BigArray.wrap(BigMath.ZERO, BigMath.TWO.multiply(BigMath.THIRD));

        Optimisation.Result result = model.maximise();
        TestUtils.assertEquals(expected, result);
        TestUtils.assertStateNotLessThanOptimal(result);

    }

    /**
     * https://github.com/optimatika/ojAlgo/issues/66
     */
    @Test
    public void testP20180311_66() {

        Variable x = Variable.make("x").lower(0).weight(2);
        Variable y = Variable.make("y").lower(0).weight(-1);
        Variable z = Variable.make("z").lower(0).weight(4);

        ExpressionsBasedModel model = new ExpressionsBasedModel();
        model.addVariable(x);
        model.addVariable(y);
        model.addVariable(z);

        model.addExpression().set(x, 3).set(y, 2).set(z, 2).upper(0);
        model.addExpression().set(x, 0).set(y, 3).set(z, -2).lower(2);

        Optimisation.Result result = model.maximise();

        TestUtils.assertEquals(Optimisation.State.INFEASIBLE, result.getState());
        TestUtils.assertFalse(model.validate(result));
    }

    /**
     * https://github.com/optimatika/ojAlgo/issues/69
     */
    @Test
    public void testP20180312_69() {

        Variable x = Variable.make("x").lower(0).weight(3);
        Variable y = Variable.make("y").lower(0).weight(2);
        Variable z = Variable.make("z").lower(0).weight(-2);

        ExpressionsBasedModel model = new ExpressionsBasedModel();
        model.addVariable(x);
        model.addVariable(y);
        model.addVariable(z);

        model.addExpression().set(x, 0).set(y, 1).set(z, -2).level(4);
        model.addExpression().set(x, 0).set(y, 4).set(z, -2).upper(1);

        //  ExpressionsBasedModel.clearPresolvers();

        Optimisation.Result result = model.maximise();

        TestUtils.assertFalse(model.validate(result));
        TestUtils.assertEquals(Optimisation.State.INFEASIBLE, result.getState());

    }

    /**
     * https://github.com/optimatika/ojAlgo/issues/70
     */
    @Test
    public void testP20180314_70() {

        Variable x = Variable.make("x").lower(0).weight(-2);
        Variable y = Variable.make("y").lower(0).weight(-2);

        ExpressionsBasedModel model = new ExpressionsBasedModel();
        model.addVariable(x);
        model.addVariable(y);

        model.addExpression().set(x, 3).set(y, 0).lower(2);
        model.addExpression().set(x, 1).set(y, 2).lower(-5);
        model.addExpression().set(x, 3).set(y, 1).upper(2);

        BigArray expected = BigArray.wrap(BigMath.TWO.multiply(BigMath.THIRD), BigMath.ZERO);
        TestUtils.assertTrue(model.validate(expected));

        Optimisation.Result solution = model.maximise();
        TestUtils.assertTrue(model.validate(solution));

        TestUtils.assertStateNotLessThanOptimal(solution);
    }

}

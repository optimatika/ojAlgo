/*
 * Copyright 1997-2014 Optimatika (www.optimatika.se)
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

import org.ojalgo.TestUtils;
import org.ojalgo.matrix.BasicMatrix;
import org.ojalgo.matrix.BigMatrix;
import org.ojalgo.matrix.PrimitiveMatrix;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.State;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.optimisation.integer.OptimisationIntegerData;
import org.ojalgo.type.TypeUtils;
import org.ojalgo.type.context.NumberContext;

public class ReportedProblems extends OptimisationLinearTests {

    public ReportedProblems() {
        super();
    }

    public ReportedProblems(final String arg0) {
        super(arg0);
    }

    public void testMath286() {

        final Variable tmpX1 = new Variable("X1").weight(TENTH.multiply(EIGHT)).lower(TEN);
        final Variable tmpX2 = new Variable("X2").weight(TENTH.multiply(TWO)).lower(ZERO);
        final Variable tmpX3 = new Variable("X3").weight(TENTH.multiply(SEVEN)).lower(EIGHT);
        final Variable tmpX4 = new Variable("X4").weight(TENTH.multiply(THREE)).lower(ZERO);
        final Variable tmpX5 = new Variable("X5").weight(TENTH.multiply(SIX)).lower(FIVE);
        final Variable tmpX6 = new Variable("X6").weight(TENTH.multiply(FOUR)).lower(ZERO);

        final Variable[] tmpFullVars = new Variable[] { tmpX1.copy(), tmpX2.copy(), tmpX3.copy(), tmpX4.copy(), tmpX5.copy(), tmpX6.copy() };
        final Variable[] tmpOddVars = new Variable[] { tmpX1.copy(), tmpX3.copy(), tmpX5.copy() };
        final Variable[] tmpEvenVars = new Variable[] { tmpX2.copy(), tmpX4.copy(), tmpX6.copy() };

        final ExpressionsBasedModel tmpFullModel = new ExpressionsBasedModel(tmpFullVars);
        //tmpFullModel.setMaximisation();

        final ExpressionsBasedModel tmpOddModel = new ExpressionsBasedModel(tmpOddVars);
        //tmpOddModel.setMaximisation();

        final ExpressionsBasedModel tmpEvenModel = new ExpressionsBasedModel(tmpEvenVars);
        //tmpEvenModel.setMaximisation();

        //        tmpFullModel.options.debug(LinearSolver.class);
        //        tmpOddModel.options.debug(LinearSolver.class);
        //        tmpEvenModel.options.debug(LinearSolver.class);

        final BigDecimal tmpRHS = new BigDecimal("23.0");
        final int tmpLength = tmpFullModel.countVariables();

        final Expression retVal = tmpFullModel.addExpression("C1");

        for (int i = 0; i < tmpLength; i++) {
            retVal.setLinearFactor(i, new BigDecimal[] { ONE, ZERO, ONE, ZERO, ONE, ZERO }[i]);
        }

        final Expression tmpAddWeightExpression = retVal;
        tmpAddWeightExpression.level(tmpRHS);
        final int tmpLength1 = tmpOddModel.countVariables();

        final Expression retVal1 = tmpOddModel.addExpression("C1");

        for (int i = 0; i < tmpLength1; i++) {
            retVal1.setLinearFactor(i, new BigDecimal[] { ONE, ONE, ONE }[i]);
        }
        final Expression tmpAddWeightExpression2 = retVal1;
        tmpAddWeightExpression2.level(tmpRHS);
        final int tmpLength2 = tmpFullModel.countVariables();

        final Expression retVal2 = tmpFullModel.addExpression("C2");

        for (int i = 0; i < tmpLength2; i++) {
            retVal2.setLinearFactor(i, new BigDecimal[] { ZERO, ONE, ZERO, ONE, ZERO, ONE }[i]);
        }

        final Expression tmpAddWeightExpression3 = retVal2;
        tmpAddWeightExpression3.level(tmpRHS);
        final int tmpLength3 = tmpEvenModel.countVariables();

        final Expression retVal3 = tmpEvenModel.addExpression("C2");

        for (int i = 0; i < tmpLength3; i++) {
            retVal3.setLinearFactor(i, new BigDecimal[] { ONE, ONE, ONE }[i]);
        }
        final Expression tmpAddWeightExpression4 = retVal3;
        tmpAddWeightExpression4.level(tmpRHS);

        final Expression tmpFullObjective = tmpFullModel.getObjectiveExpression();
        final Expression tmpOddObjective = tmpOddModel.getObjectiveExpression();
        final Expression tmpEvenObjective = tmpEvenModel.getObjectiveExpression();

        // A valid solution of 25.8 can be produced with:
        // X1=10, X2=0, X3=8, X4=0, X5=5, X6=23
        final BigDecimal tmpClaimedValue = new BigDecimal("25.8");
        final BasicMatrix tmpFullSolution = PrimitiveMatrix.FACTORY.getBuilder(6, 1).set(0, 0, 10).set(2, 0, 8).set(4, 0, 5).set(5, 0, 23).build();
        final BasicMatrix tmpOddSolution = tmpFullSolution.selectRows(0, 2, 4);
        final BasicMatrix tmpEvenSolution = tmpFullSolution.selectRows(1, 3, 5);
        TestUtils.assertEquals("Claimed solution not valid!", true, tmpFullModel.validate(tmpFullSolution.toBigStore(), new NumberContext(7, 6)));
        final Double tmpActualValue = tmpFullObjective.toFunction().invoke(tmpFullSolution.toPrimitiveStore());
        //final BigDecimal tmpActualValue = TypeUtils.toBigDecimal(tmpObjectiveValue);
        //JUnitUtils.assertEquals("Claimed objective value wrong!", 0, tmpClaimedValue.compareTo(tmpActualValue));
        TestUtils.assertEquals(tmpClaimedValue, tmpActualValue, new NumberContext(7, 6));

        // Start validating ojAlgo results

        final Optimisation.Result tmpEvenResult = tmpEvenModel.maximise();
        final Optimisation.Result tmpOddResult = tmpOddModel.maximise();
        final Optimisation.Result tmpFullResult = tmpFullModel.maximise();

        TestUtils.assertEquals(true, tmpEvenModel.validate(tmpEvenResult, new NumberContext(7, 6)));
        TestUtils.assertEquals(true, tmpOddModel.validate(tmpOddResult, new NumberContext(7, 6)));
        TestUtils.assertEquals(true, tmpFullModel.validate(tmpFullResult, new NumberContext(7, 6)));

        TestUtils.assertEquals(tmpEvenSolution, BigMatrix.FACTORY.columns(tmpEvenResult).selectRows(0, 1, 2), new NumberContext(7, 6));
        TestUtils.assertEquals(tmpOddSolution, BigMatrix.FACTORY.columns(tmpOddResult).selectRows(0, 1, 2), new NumberContext(7, 6));
        TestUtils.assertEquals(tmpFullSolution, BigMatrix.FACTORY.columns(tmpFullResult).selectRows(0, 1, 2, 3, 4, 5), new NumberContext(7, 6));

        final BigDecimal tmpEvenValue = new NumberContext(7, 6).enforce(TypeUtils.toBigDecimal(tmpEvenObjective.toFunction().invoke(
                PrimitiveMatrix.FACTORY.columns(tmpEvenResult).selectRows(0, 1, 2).toPrimitiveStore())));
        final BigDecimal tmpOddValue = new NumberContext(7, 6).enforce(TypeUtils.toBigDecimal(tmpOddObjective.toFunction().invoke(
                PrimitiveMatrix.FACTORY.columns(tmpOddResult).selectRows(0, 1, 2).toPrimitiveStore())));
        final BigDecimal tmpFullValue = new NumberContext(7, 6).enforce(TypeUtils.toBigDecimal(tmpFullObjective.toFunction().invoke(
                PrimitiveMatrix.FACTORY.columns(tmpFullResult).selectRows(0, 1, 2, 3, 4, 5).toPrimitiveStore())));

        TestUtils.assertEquals(0, tmpFullValue.compareTo(tmpEvenValue.add(tmpOddValue)));
        TestUtils.assertEquals(0, tmpClaimedValue.compareTo(tmpFullValue));
    }

    /**
     * Didn't recognise this as an infeasible problem.
     */
    public void testP20100412() {

        final ExpressionsBasedModel tmpModel = OptimisationIntegerData.buildModelForP20100412().relax(true);
        //tmpModel.relax(); // Relax the integer constraints
        tmpModel.getVariable(1).lower(ONE); // Set branch state

        final State tmpResultState = tmpModel.maximise().getState();

        TestUtils.assertFalse("Should be INFEASIBLE", tmpResultState == State.FEASIBLE);
    }

    /**
     * Depending on how the constraints were constructed the solver could fail to solve and report the problem to be
     * unbounded.
     */
    public void testP20111010() {

        final Variable[] tmpVariables = new Variable[] { new Variable("X").lower(ZERO).weight(ONE), new Variable("Y").lower(ZERO).weight(ZERO),
                new Variable("Z").lower(ZERO).weight(ZERO) };
        final ExpressionsBasedModel tmpModel = new ExpressionsBasedModel(tmpVariables);

        final Expression tmpExprC1 = tmpModel.addExpression("C1");
        tmpExprC1.level(ZERO);
        tmpExprC1.setLinearFactor(0, ONE);

        final Expression tmpExprC2 = tmpModel.addExpression("C2");
        tmpExprC2.level(ZERO);
        tmpExprC2.setLinearFactor(0, ONE);
        tmpExprC2.setLinearFactor(1, NEG);

        final Expression tmpExprC3 = tmpModel.addExpression("C3");
        tmpExprC3.level(ZERO);
        tmpExprC3.setLinearFactor(0, ONE);
        tmpExprC3.setLinearFactor(2, NEG);

        final State tmpExpectedState = State.OPTIMAL;
        final BasicMatrix tmpExpectedSolution = PrimitiveMatrix.FACTORY.makeZero(3, 1);

        final Optimisation.Result tmpResult11 = tmpModel.minimise();
        //TestUtils.assertEquals(tmpExpectedState, tmpResult11.getState());
        TestUtils.assertStateNotLessThanOptimal(tmpResult11);
        TestUtils.assertEquals(tmpExpectedSolution, BigMatrix.FACTORY.columns(tmpResult11));

        tmpExprC2.setLinearFactor(0, NEG);
        tmpExprC2.setLinearFactor(1, ONE);

        tmpExprC3.setLinearFactor(0, ONE);
        tmpExprC3.setLinearFactor(2, NEG);

        final Optimisation.Result tmpResultN1 = tmpModel.minimise();
        //TestUtils.assertEquals(tmpExpectedState, tmpResultN1.getState());
        TestUtils.assertStateNotLessThanOptimal(tmpResultN1);
        TestUtils.assertEquals(tmpExpectedSolution, BigMatrix.FACTORY.columns(tmpResultN1));

        tmpExprC2.setLinearFactor(0, ONE);
        tmpExprC2.setLinearFactor(1, NEG);

        tmpExprC3.setLinearFactor(0, NEG);
        tmpExprC3.setLinearFactor(2, ONE);

        final Optimisation.Result tmpResult1N = tmpModel.minimise();
        //TestUtils.assertEquals(tmpExpectedState, tmpResult1N.getState());
        TestUtils.assertStateNotLessThanOptimal(tmpResult1N);
        TestUtils.assertEquals(tmpExpectedSolution, BigMatrix.FACTORY.columns(tmpResult1N));

        tmpExprC2.setLinearFactor(0, NEG);
        tmpExprC2.setLinearFactor(1, ONE);

        tmpExprC3.setLinearFactor(0, NEG);
        tmpExprC3.setLinearFactor(2, ONE);

        final Optimisation.Result tmpResultNN = tmpModel.minimise();
        //TestUtils.assertEquals(tmpExpectedState, tmpResultNN.getState());
        TestUtils.assertStateNotLessThanOptimal(tmpResultNN);
        TestUtils.assertEquals(tmpExpectedSolution, BigMatrix.FACTORY.columns(tmpResultNN));
    }

}

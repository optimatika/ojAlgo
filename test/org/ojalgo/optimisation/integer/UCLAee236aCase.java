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

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.State;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.type.context.NumberContext;

/**
 * http://www.ee.ucla.edu/ee236a/lectures/intlp.pdf
 *
 * @author apete
 */
public class UCLAee236aCase extends OptimisationIntegerTests {

    private static final NumberContext PRECISION = new NumberContext(2, 2);

    private static ExpressionsBasedModel makeOriginalRootModel() {

        final Variable[] tmpVariables = new Variable[] { new Variable("X1").lower(ZERO).weight(TWO.negate()).integer(true),
                new Variable("X2").lower(ZERO).weight(THREE.negate()).integer(true) };

        final ExpressionsBasedModel retVal = new ExpressionsBasedModel(tmpVariables);
        retVal.setMinimisation();

        final Expression tmpExprC1 = retVal.addExpression("C1");
        for (int i = 0; i < retVal.countVariables(); i++) {
            tmpExprC1.set(i, new BigDecimal[] { TWO.multiply(NINTH), QUARTER }[i]);
        }
        tmpExprC1.upper(ONE);

        final Expression tmpExprC2 = retVal.addExpression("C2");
        for (int i = 0; i < retVal.countVariables(); i++) {
            tmpExprC2.set(i, new BigDecimal[] { SEVENTH, THIRD }[i]);
        }
        tmpExprC2.upper(ONE);

        return retVal;
    }

    /**
     * http://www.ee.ucla.edu/ee236a/lectures/intlp.pdf
     */
    @Test
    public void testFullMIP() {

        final ExpressionsBasedModel tmpModel = UCLAee236aCase.makeOriginalRootModel();

        final Optimisation.Result tmpResult = tmpModel.minimise();

        TestUtils.assertEquals(State.OPTIMAL, tmpResult.getState());

        final PrimitiveDenseStore tmpExpX = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 2.0 }, { 2.0 } });

        TestUtils.assertEquals(tmpExpX, tmpResult, PRECISION);
    }

    @Test
    public void testRelaxedButConstrainedToOptimalMIP() {

        final ExpressionsBasedModel tmpModel = UCLAee236aCase.makeOriginalRootModel().relax(true);

        tmpModel.getVariable(0).lower(TWO);
        tmpModel.getVariable(0).upper(TWO);
        tmpModel.getVariable(1).lower(TWO);
        tmpModel.getVariable(1).upper(TWO);

        final Optimisation.Result tmpResult = tmpModel.minimise();

        //TestUtils.assertEquals(State.OPTIMAL, tmpResult.getState());
        TestUtils.assertStateNotLessThanOptimal(tmpResult);

        final PrimitiveDenseStore tmpExpX = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 2.0 }, { 2.0 } });

        TestUtils.assertEquals(tmpExpX, tmpResult, PRECISION);
    }

    /**
     * P0
     */
    @Test
    public void testRelaxedNodeP00() {

        final ExpressionsBasedModel tmpModel = UCLAee236aCase.makeOriginalRootModel().relax(true);

        final Optimisation.Result tmpResult = tmpModel.minimise();

        TestUtils.assertEquals(State.OPTIMAL, tmpResult.getState());

        final PrimitiveDenseStore tmpExpX = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 2.17 }, { 2.07 } });

        TestUtils.assertEquals(tmpExpX, tmpResult, PRECISION);

        // The lecture notes states -10.56, but I get -10.55. One of us is rounding incorrectly...
        TestUtils.assertEquals(-10.55, tmpModel.minimise().getValue(), PRECISION);
    }

    /**
     * P1
     */
    @Test
    public void testRelaxedNodeP01() {

        final ExpressionsBasedModel tmpModel = UCLAee236aCase.makeOriginalRootModel().relax(true);
        tmpModel.getVariable(0).upper(TWO);

        final Optimisation.Result tmpResult = tmpModel.minimise();

        TestUtils.assertEquals(State.OPTIMAL, tmpResult.getState());

        final PrimitiveDenseStore tmpExpX = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 2.00 }, { 2.14 } });

        TestUtils.assertEquals(tmpExpX, tmpResult, PRECISION);

        TestUtils.assertEquals(-10.43, tmpModel.minimise().getValue(), PRECISION);
    }

    /**
     * P2
     */
    @Test
    public void testRelaxedNodeP02() {

        final ExpressionsBasedModel tmpModel = UCLAee236aCase.makeOriginalRootModel().relax(true);
        tmpModel.getVariable(0).lower(THREE);

        final Optimisation.Result tmpResult = tmpModel.minimise();

        TestUtils.assertEquals(State.OPTIMAL, tmpResult.getState());

        final PrimitiveDenseStore tmpExpX = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 3.00 }, { 1.33 } });

        TestUtils.assertEquals(tmpExpX, tmpResult, PRECISION);

        TestUtils.assertEquals(-10.00, tmpModel.minimise().getValue(), PRECISION);
    }

    /**
     * P3
     */
    @Test
    public void testRelaxedNodeP03() {

        final ExpressionsBasedModel tmpModel = UCLAee236aCase.makeOriginalRootModel().relax(true);
        tmpModel.getVariable(0).upper(TWO);
        tmpModel.getVariable(1).upper(TWO);

        final Optimisation.Result tmpResult = tmpModel.minimise();

        TestUtils.assertEquals(State.OPTIMAL, tmpResult.getState());

        final PrimitiveDenseStore tmpExpX = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 2.00 }, { 2.00 } });

        TestUtils.assertEquals(tmpExpX, tmpResult, PRECISION);

        TestUtils.assertEquals(-10.00, tmpModel.minimise().getValue(), PRECISION);
    }

    /**
     * P4
     */
    @Test
    public void testRelaxedNodeP04() {

        final ExpressionsBasedModel tmpModel = UCLAee236aCase.makeOriginalRootModel().relax(true);
        tmpModel.getVariable(0).upper(TWO);
        tmpModel.getVariable(1).lower(THREE);

        //        tmpModel.options.debug_stream = BasicLogger.DEBUG;
        //        tmpModel.options.debug_solver = LinearSolver.class;
        //        tmpModel.options.validate = true;

        final Optimisation.Result tmpResult = tmpModel.minimise();

        TestUtils.assertStateNotLessThanOptimal(tmpResult);

        final PrimitiveDenseStore tmpExpX = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 0.00 }, { 3.00 } });

        TestUtils.assertEquals(tmpExpX, tmpResult, PRECISION);

        TestUtils.assertEquals(-9.00, tmpModel.minimise().getValue(), PRECISION);
    }

    /**
     * P5
     */
    @Test
    public void testRelaxedNodeP05() {

        final ExpressionsBasedModel tmpModel = UCLAee236aCase.makeOriginalRootModel().relax(true);
        tmpModel.getVariable(0).lower(THREE);
        tmpModel.getVariable(1).upper(ONE);

        final Optimisation.Result tmpResult = tmpModel.minimise();

        TestUtils.assertEquals(State.OPTIMAL, tmpResult.getState());

        final PrimitiveDenseStore tmpExpX = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 3.38 }, { 1.00 } });

        TestUtils.assertEquals(tmpExpX, tmpResult, PRECISION);

        TestUtils.assertEquals(-9.75, tmpModel.minimise().getValue(), PRECISION);
    }

    /**
     * P6
     */
    @Test
    public void testRelaxedNodeP06() {

        final ExpressionsBasedModel tmpModel = UCLAee236aCase.makeOriginalRootModel().relax(true);
        tmpModel.getVariable(0).lower(THREE);
        tmpModel.getVariable(1).lower(TWO);

        final Optimisation.Result tmpResult = tmpModel.minimise();

        TestUtils.assertEquals(State.INFEASIBLE, tmpResult.getState());
    }

    /**
     * P7
     */
    @Test
    public void testRelaxedNodeP07() {

        final ExpressionsBasedModel tmpModel = UCLAee236aCase.makeOriginalRootModel().relax(true);
        tmpModel.getVariable(0).lower(THREE);
        tmpModel.getVariable(1).upper(ONE);
        tmpModel.getVariable(0).upper(THREE);

        final Optimisation.Result tmpResult = tmpModel.minimise();

        TestUtils.assertEquals(State.OPTIMAL, tmpResult.getState());

        final PrimitiveDenseStore tmpExpX = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 3.00 }, { 1.00 } });

        TestUtils.assertEquals(tmpExpX, tmpResult, PRECISION);

        TestUtils.assertEquals(-9.00, tmpModel.minimise().getValue(), PRECISION);
    }

    /**
     * P8
     */
    @Test
    public void testRelaxedNodeP08() {

        final ExpressionsBasedModel tmpModel = UCLAee236aCase.makeOriginalRootModel().relax(true);
        tmpModel.getVariable(0).lower(THREE);
        tmpModel.getVariable(1).upper(ONE);
        tmpModel.getVariable(0).lower(FOUR);

        final Optimisation.Result tmpResult = tmpModel.minimise();

        TestUtils.assertEquals(State.OPTIMAL, tmpResult.getState());

        final PrimitiveDenseStore tmpExpX = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 4.00 }, { 0.44 } });

        TestUtils.assertEquals(tmpExpX, tmpResult, PRECISION);

        TestUtils.assertEquals(-9.33, tmpModel.minimise().getValue(), PRECISION);
    }

    /**
     * P9
     */
    @Test
    public void testRelaxedNodeP09() {

        final ExpressionsBasedModel tmpModel = UCLAee236aCase.makeOriginalRootModel().relax(true);
        tmpModel.getVariable(0).lower(THREE);
        tmpModel.getVariable(1).upper(ONE);
        tmpModel.getVariable(0).lower(FOUR);
        tmpModel.getVariable(1).upper(ZERO);

        final Optimisation.Result tmpResult = tmpModel.minimise();

        TestUtils.assertEquals(State.OPTIMAL, tmpResult.getState());

        final PrimitiveDenseStore tmpExpX = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 4.50 }, { 0.00 } });

        TestUtils.assertEquals(tmpExpX, tmpResult, PRECISION);

        TestUtils.assertEquals(-9.00, tmpModel.minimise().getValue(), PRECISION);
    }

    /**
     * P10
     */
    @Test
    public void testRelaxedNodeP10() {

        final ExpressionsBasedModel tmpModel = UCLAee236aCase.makeOriginalRootModel().relax(true);
        tmpModel.getVariable(0).lower(THREE);
        tmpModel.getVariable(1).upper(ONE);
        tmpModel.getVariable(0).lower(FOUR);
        tmpModel.getVariable(1).lower(ONE);

        final Optimisation.Result tmpResult = tmpModel.minimise();

        TestUtils.assertEquals(State.INFEASIBLE, tmpResult.getState());
    }

    /**
     * P11
     */
    @Test
    public void testRelaxedNodeP11() {

        final ExpressionsBasedModel tmpModel = UCLAee236aCase.makeOriginalRootModel().relax(true);
        tmpModel.getVariable(0).lower(THREE);
        tmpModel.getVariable(1).upper(ONE);
        tmpModel.getVariable(0).lower(FOUR);
        tmpModel.getVariable(1).upper(ZERO);
        tmpModel.getVariable(0).upper(FOUR);

        final Optimisation.Result tmpResult = tmpModel.minimise();

        //TestUtils.assertEquals(State.OPTIMAL, tmpResult.getState());
        TestUtils.assertStateNotLessThanOptimal(tmpResult);

        final PrimitiveDenseStore tmpExpX = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 4.00 }, { 0.00 } });

        TestUtils.assertEquals(tmpExpX, tmpResult, PRECISION);

        TestUtils.assertEquals(-8.00, tmpModel.minimise().getValue(), PRECISION);
    }

    /**
     * P12
     */
    @Test
    public void testRelaxedNodeP12() {

        final ExpressionsBasedModel tmpModel = UCLAee236aCase.makeOriginalRootModel().relax(true);
        tmpModel.getVariable(0).lower(THREE);
        tmpModel.getVariable(1).upper(ONE);
        tmpModel.getVariable(0).lower(FOUR);
        tmpModel.getVariable(1).upper(ZERO);
        tmpModel.getVariable(0).lower(FIVE);

        final Optimisation.Result tmpResult = tmpModel.minimise();

        TestUtils.assertEquals(State.INFEASIBLE, tmpResult.getState());
    }

}

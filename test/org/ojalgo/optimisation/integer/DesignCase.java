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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.optimisation.linear.LinearSolver;

public class DesignCase {

    /**
     * http://www.ohio.edu/people/melkonia/math3050/slides/IPextendedintro.ppt Slide 8
     */
    @Test
    public void testFacilityLocation() {

        final ArrayList<Variable> tmpVariables = new ArrayList<>();
        tmpVariables.add(Variable.makeBinary("Factory in LA").weight(9));
        tmpVariables.add(Variable.makeBinary("Factory in SF").weight(5));
        tmpVariables.add(Variable.makeBinary("Warehouse in LA").weight(6));
        tmpVariables.add(Variable.makeBinary("Warehouse in SF").weight(4));

        final ExpressionsBasedModel tmpModel = new ExpressionsBasedModel();
        tmpModel.addVariables(tmpVariables);

        final Expression tmpBudgetCost = tmpModel.addExpression("Budget").upper(10);
        tmpBudgetCost.set(tmpVariables.get(0), 6);
        tmpBudgetCost.set(tmpVariables.get(1), 3);
        tmpBudgetCost.set(tmpVariables.get(2), 5);
        tmpBudgetCost.set(tmpVariables.get(3), 2);

        //tmpModel.options.debug(GenericSolver.class);

        final Result tmpResult = tmpModel.maximise();

        TestUtils.assertStateNotLessThanOptimal(tmpResult);

        TestUtils.assertEquals(15.0, tmpResult.getValue());

        TestUtils.assertEquals(0.0, tmpResult.doubleValue(0));
        TestUtils.assertEquals(1.0, tmpResult.doubleValue(1));
        TestUtils.assertEquals(1.0, tmpResult.doubleValue(2));
        TestUtils.assertEquals(1.0, tmpResult.doubleValue(3));
    }

    public void testSOS() {

        final ExpressionsBasedModel model = new ExpressionsBasedModel();

        final List<Expression> activities = new ArrayList<>();
        for (int h = 0; h < 24; h++) {
            final Expression expr = model.addExpression("Only1@" + h).upper(1);
            activities.add(expr);

        }
        for (int a = 0; a < 10; a++) {

            for (int h = 0; h < 24; h++) {

                final Set<Variable> orderedSet = new HashSet<Variable>();

                // Activity 'a' is "on" at hour 'h', or not
                final Variable variable = model.addVariable("A" + a + "-H" + h).binary().weight(Math.abs(12 - h));
                orderedSet.add(variable);

                model.addSpecialOrderedSet(orderedSet, 0, 3);

                activities.get(h).set(variable, 1);
            }
        }

        model.options.debug(LinearSolver.class);

        final Result result = model.minimise();

        BasicLogger.debug(result);
        BasicLogger.debug(model);

    }

}

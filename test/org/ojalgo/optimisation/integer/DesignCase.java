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

    @Test
    public void testSOS() {

        final ExpressionsBasedModel model = new ExpressionsBasedModel();

        final List<Variable> starts = new ArrayList<>();
        final List<Variable> works = new ArrayList<>();

        final Set<Variable> orderedSet = new HashSet<Variable>();

        for (int h = 0; h < 24; h++) {

            final Variable start = model.addVariable("S" + h).binary();
            starts.add(start);

            final Variable work = model.addVariable("W" + h).binary().weight(Math.random());
            works.add(work);

            orderedSet.add(work);
        }

        model.addSpecialOrderedSet(orderedSet, 3, 3);

        for (int h = 0; h < 21; h++) {

            final Expression expr = model.addExpression("Start" + h);
            expr.upper(0);

            expr.set(starts.get(h), 3);

            expr.set(works.get(h), -1);
            expr.set(works.get(h + 1), -1);
            expr.set(works.get(h + 2), -1);
        }
        for (int h = 21; h < 24; h++) {
            starts.get(h).level(0);
        }

        model.addExpression("One start").level(1).setLinearFactorsSimple(starts);

        model.options.debug(IntegerSolver.class);

        final Result result = model.minimise();

        BasicLogger.debug(result);
        BasicLogger.debug(model);

    }

}

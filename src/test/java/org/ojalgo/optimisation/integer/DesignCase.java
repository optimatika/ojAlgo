/*
 * Copyright 1997-2021 Optimatika
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

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
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

    /**
     * Don't remember where this comes from. Most likely there was a problem entering paramerets like
     * 1.7976931348623157E308
     */
    @Test
    public void testSimpleTSP() {

        final int n = 6;

        final double[][] c = new double[n][n];
        c[0][0] = 1.7976931348623157E308;
        c[0][1] = 141.4213562373095;
        c[0][2] = 223.60679774997897;
        c[0][3] = 223.60679774997897;
        c[0][4] = 141.4213562373095;
        c[0][5] = 156.63604262201076;
        c[1][0] = 141.4213562373095;
        c[1][1] = 1.7976931348623157E308;
        c[1][2] = 100.0;
        c[1][3] = 223.60679774997897;
        c[1][4] = 200.0;
        c[1][5] = 219.25609608009617;
        c[2][0] = 223.60679774997897;
        c[2][1] = 100.0;
        c[2][2] = 1.7976931348623157E308;
        c[2][3] = 200.0;
        c[2][4] = 223.60679774997897;
        c[2][5] = 319.2543607976003;
        c[3][0] = 223.60679774997897;
        c[3][1] = 223.60679774997897;
        c[3][2] = 200.0;
        c[3][3] = 1.7976931348623157E308;
        c[3][4] = 100.0;
        c[3][5] = 377.5537017276938;
        c[4][0] = 141.4213562373095;
        c[4][1] = 200.0;
        c[4][2] = 223.60679774997897;
        c[4][3] = 100.0;
        c[4][4] = 1.7976931348623157E308;
        c[4][5] = 297.81988930943544;
        c[5][0] = 156.63604262201076;
        c[5][1] = 219.25609608009617;
        c[5][2] = 319.2543607976003;
        c[5][3] = 377.5537017276938;
        c[5][4] = 297.81988930943544;
        c[5][5] = 1.7976931348623157E308;

        final ExpressionsBasedModel model = new ExpressionsBasedModel();

        //DECISION VARIABLES
        final Variable[][] x = new Variable[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                x[i][j] = Variable.make("x" + i + "_" + j).binary().weight(c[i][j]);
                model.addVariable(x[i][j]);
            }
        }
        final Variable[] u = new Variable[n];
        for (int i = 1; i < n; i++) {
            u[i] = new Variable("u" + i);
            model.addVariable(u[i]);
        }

        //CONSTRAINTS
        //forall(i in cities)
        //flow_out:
        //sum(j in cities : i!=j) x[i][j]==1;
        for (int i = 0; i < n; i++) {
            final Expression constraint_line = model.addExpression("constraint_line" + i).lower(1).upper(1);
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    constraint_line.set(x[i][j], 1);
                }
            }
        }

        //forall(j in cities)
        //flow_in:
        //sum(i in cities : i!=j) x[i][j]==1;
        for (int j = 0; j < n; j++) {
            final Expression constraint_column = model.addExpression("constraint_column" + j).lower(1).upper(1);
            for (int i = 0; i < n; i++) {
                if (i != j) {
                    constraint_column.set(x[i][j], 1);
                }
            }
        }

        //forall(i in cities: i>=1, j in cities: j>=1)
        //subroute:
        //u[i]-u[j]+n*x[i][j] <= n-1;
        for (int i = 1; i < n; i++) {
            for (int j = 1; j < n; j++) {
                if (i != j) {
                    final Expression constraint_subroute = model.addExpression("constraint_subroute" + i + "_" + j).upper(n - 1);
                    constraint_subroute.set(u[i], 1);
                    constraint_subroute.set(u[j], -1);
                    constraint_subroute.set(x[i][j], n);
                }
            }
        }

        final Optimisation.Result result = model.minimise();

        TestUtils.assertStateNotLessThanOptimal(result);
        TestUtils.assertTrue(model.validate(result));
        TestUtils.assertEquals(917.3134949394167, result.getValue());
    }

    /**
     * Essentilly this test case just verifies that the SOS presolver doesn't screw things up.
     */
    @Test
    public void testSOS() {

        final ExpressionsBasedModel model = new ExpressionsBasedModel();

        final List<Variable> starts1 = new ArrayList<>();
        final List<Variable> works1 = new ArrayList<>();

        final List<Variable> starts2 = new ArrayList<>();
        final List<Variable> works2 = new ArrayList<>();

        final Set<Variable> orderedSet1 = new HashSet<>();

        final Set<Variable> orderedSet2 = new HashSet<>();

        for (int h = 0; h < 24; h++) {

            final Variable start1 = model.addVariable("Start activity A at " + h).binary();
            starts1.add(start1);

            final Variable start2 = model.addVariable("Start activity B at " + h).binary();
            starts2.add(start2);

            final Variable work1 = model.addVariable("Activity A ongoing at " + h).binary().weight(Math.random());
            works1.add(work1);

            orderedSet1.add(work1);

            final Variable work2 = model.addVariable("Activity B ongoing at " + h).binary().weight(Math.random());
            works2.add(work2);

            orderedSet2.add(work2);

            model.addExpression("Maximum one ongoing activity at " + h).upper(1).set(work1, 1).set(work2, 1);
        }

        model.addSpecialOrderedSet(orderedSet1, 3, 3);
        model.addSpecialOrderedSet(orderedSet2, 3, 3);

        for (int h = 0; h < 21; h++) {

            final Expression expr1 = model.addExpression("Finish A when started at " + h);
            expr1.upper(0);

            expr1.set(starts1.get(h), 3);

            expr1.set(works1.get(h), -1);
            expr1.set(works1.get(h + 1), -1);
            expr1.set(works1.get(h + 2), -1);

            final Expression expr2 = model.addExpression("Finish B when started at " + h);
            expr2.upper(0);

            expr2.set(starts2.get(h), 3);

            expr2.set(works2.get(h), -1);
            expr2.set(works2.get(h + 1), -1);
            expr2.set(works2.get(h + 2), -1);

        }
        for (int h = 21; h < 24; h++) {
            starts1.get(h).level(0);
            starts2.get(h).level(0);
        }

        model.addExpression("Only start activity A once").level(1).setLinearFactorsSimple(starts1);
        model.addExpression("Only start activity B once").level(1).setLinearFactorsSimple(starts2);

        final Result resultMin = model.minimise();

        TestUtils.assertStateNotLessThanOptimal(resultMin);
        TestUtils.assertTrue(resultMin.getValue() >= 0.0);
        TestUtils.assertTrue(resultMin.getValue() <= 6.0);

        final Result resultMax = model.maximise();

        TestUtils.assertStateNotLessThanOptimal(resultMax);
        TestUtils.assertTrue(resultMax.getValue() >= 0.0);
        TestUtils.assertTrue(resultMax.getValue() <= 6.0);

        TestUtils.assertTrue(resultMin.getValue() <= resultMax.getValue());
    }

}

/*
 * Copyright 1997-2023 Optimatika
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.optimisation.Optimisation.State;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.type.context.NumberContext;

public class DesignCase extends OptimisationIntegerTests {

    public static ExpressionsBasedModel makeBranchAndCutSimpleExampleModel() {
        ExpressionsBasedModel model = new ExpressionsBasedModel();
        Variable x1 = model.newVariable("x1").integer(true).lower(0).weight(-6);
        Variable x2 = model.newVariable("x2").integer(true).lower(0).weight(-5);
        model.addExpression().upper(11).set(x1, 3).set(x2, 1);
        model.addExpression().upper(5).set(x1, -1).set(x2, 2);
        return model;
    }

    /**
     * Test based on exaple from IE 511: Integer Programming, Spring 2021 22 Apr, 2021 Lecture 25: Mixed
     * Integer Cuts Lecturer: Karthik Chandrasekaran Scribe: Karthik
     * <p>
     * lecture-25.pdf
     * <p>
     * Actually a bad example:
     * <ol>
     * <li>There is a mistake in the "Gomory Mixed Integer Cut" defintion (equation 25.2) – missed a minus
     * sign.
     * <li>When suggesting a cut the example does not use the cut definition directly, and it seems states an
     * incorrect final simplex row. Which results in not being able to reproduce the same cut here as in the
     * example.
     * <li>Just test that the GomorySolver can solve this.
     */
    @Test
    public void testBadMixedIntegerCutExample() {

        ExpressionsBasedModel mMIP = new ExpressionsBasedModel();
        Variable x = mMIP.newVariable("x").integer(false).lower(0).weight(1);
        Variable y = mMIP.newVariable("y").integer(true).lower(0).weight(-4);
        mMIP.newExpression("C1").upper(14).set(x, -2).set(y, 7);
        mMIP.newExpression("C2").upper(3).set(x, 1);
        mMIP.newExpression("C3").upper(3).set(x, -2).set(y, 2);

        ExpressionsBasedModel mLP = mMIP.snapshot();
        mLP.relax(false);
        NodeSolver sLP = mLP.prepare(NodeSolver::new);

        Result expRootLP = Result.of(-59.0 / 7.0, State.OPTIMAL, 3.0, 20.0 / 7.0);
        TestUtils.assertTrue(mLP.validate(expRootLP, BasicLogger.ERROR));
        Result actRootLP = sLP.solve();
        TestUtils.assertStateAndSolution(expRootLP, actRootLP);

        Result expMIP = Result.of(-7.5, State.OPTIMAL, 0.5, 2.0);
        GomorySolver gomorySolver = new GomorySolver(mMIP);
        Result actMIP = gomorySolver.solve();
        TestUtils.assertStateAndSolution(expMIP, actMIP);
    }

    /**
     * Test based on a simple example in Branch-and-Cut Algorithms for Combinatorial Optimization Problems1
     * John E. Mitchell Mathematical Sciences Rensselaer Polytechnic Institute Troy, NY, USA email:
     * mitchj@rpi.edu http://www.math.rpi.edu/ ̃mitchj April 19, 1999, revised September 7, 1999.
     * <p>
     * bc_hao.pdf
     */
    @Test
    public void testBranchAndCutSimpleExample() {

        // Eg0
        ExpressionsBasedModel mEg0 = DesignCase.makeBranchAndCutSimpleExampleModel();

        Result expEg0 = Result.of(-28, State.OPTIMAL, 3.0, 2.0);

        // First test if the GomorySolver can solve this
        GomorySolver sEg0 = new GomorySolver(mEg0);
        if (DEBUG) {
            sEg0.options.debug(GomorySolver.class);
        }
        Result actEg0 = sEg0.solve();
        TestUtils.assertStateAndSolution(expEg0, actEg0, NumberContext.of(11));

        // In the example the relaxed Eg0 was solved to:
        Result expEg0LP = Result.of(-232.0 / 7.0, State.OPTIMAL, 17.0 / 7.0, 26.0 / 7.0);
        ExpressionsBasedModel mEg0LP = mEg0.copy();
        mEg0LP.relax();
        Result actEg0LP = mEg0LP.minimise();
        TestUtils.assertStateAndSolution(expEg0LP, actEg0LP);

        // Branching on x1 to create Eg1 and Eg2

        // Eg1
        ExpressionsBasedModel mEg1LP = mEg0LP.copy();
        mEg1LP.getVariable(0).lower(3);
        Result actEg1LP = mEg1LP.minimise();
        TestUtils.assertStateAndSolution(expEg0, actEg1LP); // Finds the integer solution

        // Eg2
        Result expEg2LP = Result.of(-29.5, State.OPTIMAL, 2.0, 3.5);
        ExpressionsBasedModel mEg2LP = mEg0LP.copy();
        mEg2LP.getVariable(0).upper(2);
        Result actEg2LP = mEg2LP.minimise();
        TestUtils.assertStateAndSolution(expEg2LP, actEg2LP);

        // Add integer constrains again and verify that the GomorySolver can take it from here
        ExpressionsBasedModel mEg2 = mEg2LP.copy();
        mEg2.getVariable(0).integer(true);
        mEg2.getVariable(1).integer(true);
        GomorySolver sEg2 = new GomorySolver(mEg2);
        Result actEg2 = sEg2.solve();
        TestUtils.assertStateNotLessThanOptimal(actEg2); // Found a solution
        TestUtils.assertTrue(actEg2.getValue() > expEg0.getValue()); // but not as good as the other branch
    }

    /**
     * <p>
     * Example from a presentation by John E. Mitchell titled Gomory Cutting Planes.
     * <p>
     * Primarily tests "Expressing the cut in the original variables"
     */
    @Test
    public void testExpressingTheCutInTheOriginalVariables() {

        ExpressionsBasedModel orgModel = new ExpressionsBasedModel();
        Variable x1 = orgModel.newVariable("x1").lower(0).weight(-1);
        Variable x2 = orgModel.newVariable("x2").lower(0).weight(-1);
        Expression c3 = orgModel.newExpression("C3").upper(20).set(x1, 2).set(x2, 5);
        Expression c4 = orgModel.newExpression("C4").upper(17).set(x1, 4).set(x2, 3);

        ExpressionsBasedModel slackModel = orgModel.copy();
        Variable x3 = slackModel.newVariable("x3").lower(0);
        Variable x4 = slackModel.newVariable("x4").lower(0);
        slackModel.getExpression("C3").lower(20).set(x3, 1);
        slackModel.getExpression("C4").lower(17).set(x4, 1);

        Result orgResult = orgModel.minimise();
        Result slackResult = slackModel.minimise();

        TestUtils.assertEquals(orgResult.doubleValue(0), slackResult.doubleValue(0));
        TestUtils.assertEquals(orgResult.doubleValue(1), slackResult.doubleValue(1));

        slackModel.newExpression("CUT_A").lower(2.0 / 7.0).set(x3, 2.0 / 7.0).set(x4, 6.0 / 7.0);
        slackModel.newExpression("CUT_B").lower(11.0 / 14.0).set(x3, 11.0 / 14.0).set(x4, 5.0 / 14.0);

        slackResult = slackModel.minimise();

        TestUtils.assertEquals(2.0, slackResult.doubleValue(0));
        TestUtils.assertEquals(3.0, slackResult.doubleValue(1));

        Expression cutA = orgModel.newExpression("CUT_A").lower(2.0 / 7.0);

        BigDecimal factor = BigDecimal.valueOf(2.0 / 7.0).negate();
        BigDecimal limit = c3.getUpperLimit();
        BigDecimal shift = limit.multiply(factor);

        cutA.shift(shift);
        c3.addTo(cutA, factor);

        BigDecimal factor2 = BigDecimal.valueOf(6.0 / 7.0).negate();
        BigDecimal limit2 = c4.getUpperLimit();
        BigDecimal shift2 = limit2.multiply(factor2);

        cutA.shift(shift2);
        c4.addTo(cutA, factor2);

        // x1 + x2 ≤ 5
        TestUtils.assertEquals(-20, cutA.getLowerLimit());
        TestUtils.assertEquals(-4, cutA.get(x1));
        TestUtils.assertEquals(-4, cutA.get(x2));

        Expression cutB = orgModel.newExpression("CUT_B").lower(11.0 / 14.0);

        BigDecimal factor5 = BigDecimal.valueOf(11.0 / 14.0).negate();
        BigDecimal limit5 = c3.getUpperLimit();
        BigDecimal shift5 = limit5.multiply(factor5);

        cutB.shift(shift5);
        c3.addTo(cutB, factor5);

        BigDecimal factor7 = BigDecimal.valueOf(5.0 / 14.0).negate();
        BigDecimal limit7 = c4.getUpperLimit();
        BigDecimal shift7 = limit7.multiply(factor7);

        cutB.shift(shift7);
        c4.addTo(cutB, factor7);

        // 3x1 + 5x2 ≤ 21
        TestUtils.assertEquals(-21, cutB.getLowerLimit());
        TestUtils.assertEquals(-3, cutB.get(x1));
        TestUtils.assertEquals(-5, cutB.get(x2));

        orgResult = orgModel.minimise();

        TestUtils.assertEquals(2.0, orgResult.doubleValue(0));
        TestUtils.assertEquals(3.0, orgResult.doubleValue(1));
    }

    /**
     * https://people.ohio.edu/melkonia/math3050/slides/IPextendedintro.ppt
     * <p>
     * http://www.ohio.edu/people/melkonia/math3050/slides/IPextendedintro.ppt Slide 8
     */
    @Test
    public void testFacilityLocation() {

        ArrayList<Variable> variables = new ArrayList<>();
        variables.add(Variable.makeBinary("Factory in LA").weight(9));
        variables.add(Variable.makeBinary("Factory in SF").weight(5));
        variables.add(Variable.makeBinary("Warehouse in LA").weight(6));
        variables.add(Variable.makeBinary("Warehouse in SF").weight(4));

        ExpressionsBasedModel model = new ExpressionsBasedModel();
        model.addVariables(variables);

        Expression budgetCost = model.newExpression("Budget").upper(10);
        budgetCost.set(variables.get(0), 6);
        budgetCost.set(variables.get(1), 3);
        budgetCost.set(variables.get(2), 5);
        budgetCost.set(variables.get(3), 2);

        if (DEBUG) {
            BasicLogger.debug(model);
            model.options.debug(Optimisation.Solver.class);
        }

        Result result = model.maximise();

        TestUtils.assertStateNotLessThanOptimal(result);

        TestUtils.assertEquals(15.0, result.getValue());

        TestUtils.assertEquals(0.0, result.doubleValue(0));
        TestUtils.assertEquals(1.0, result.doubleValue(1));
        TestUtils.assertEquals(1.0, result.doubleValue(2));
        TestUtils.assertEquals(1.0, result.doubleValue(3));
    }

    /**
     * Essentilly this test case just verifies that the SOS presolver doesn't screw things up.
     */
    @Test
    public void testSOS() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        List<Variable> starts1 = new ArrayList<>();
        List<Variable> works1 = new ArrayList<>();

        List<Variable> starts2 = new ArrayList<>();
        List<Variable> works2 = new ArrayList<>();

        Set<Variable> orderedSet1 = new HashSet<>();

        Set<Variable> orderedSet2 = new HashSet<>();

        for (int h = 0; h < 24; h++) {

            Variable start1 = model.newVariable("Start activity A at " + h).binary();
            starts1.add(start1);

            Variable start2 = model.newVariable("Start activity B at " + h).binary();
            starts2.add(start2);

            Variable work1 = model.newVariable("Activity A ongoing at " + h).binary().weight(Math.random());
            works1.add(work1);

            orderedSet1.add(work1);

            Variable work2 = model.newVariable("Activity B ongoing at " + h).binary().weight(Math.random());
            works2.add(work2);

            orderedSet2.add(work2);

            model.newExpression("Maximum one ongoing activity at " + h).upper(1).set(work1, 1).set(work2, 1);
        }

        model.addSpecialOrderedSet(orderedSet1, 3, 3);
        model.addSpecialOrderedSet(orderedSet2, 3, 3);

        for (int h = 0; h < 21; h++) {

            Expression expr1 = model.newExpression("Finish A when started at " + h);
            expr1.upper(0);

            expr1.set(starts1.get(h), 3);

            expr1.set(works1.get(h), -1);
            expr1.set(works1.get(h + 1), -1);
            expr1.set(works1.get(h + 2), -1);

            Expression expr2 = model.newExpression("Finish B when started at " + h);
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

        model.newExpression("Only start activity A once").level(1).setLinearFactorsSimple(starts1);
        model.newExpression("Only start activity B once").level(1).setLinearFactorsSimple(starts2);

        Result resultMin = model.minimise();

        TestUtils.assertStateNotLessThanOptimal(resultMin);
        TestUtils.assertTrue(resultMin.getValue() >= 0.0);
        TestUtils.assertTrue(resultMin.getValue() <= 6.0);

        Result resultMax = model.maximise();

        TestUtils.assertStateNotLessThanOptimal(resultMax);
        TestUtils.assertTrue(resultMax.getValue() >= 0.0);
        TestUtils.assertTrue(resultMax.getValue() <= 6.0);

        TestUtils.assertTrue(resultMin.getValue() <= resultMax.getValue());
    }

}

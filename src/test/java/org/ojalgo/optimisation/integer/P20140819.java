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
package org.ojalgo.optimisation.integer;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.ExpressionsBasedModel.Intermediate;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.optimisation.Optimisation.State;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.optimisation.linear.LinearSolver;

/**
 * A user reported that ojAlgo had problems solving this. (results unstable between execution, and
 * solution/state sometimes incorrect).
 */
public class P20140819 extends OptimisationIntegerTests {

    /**
     * 20201217: Solution obtained using CPLEX
     */
    static Optimisation.Result CPLEX_RESULTS = Result.of(41601.41860675984, State.OPTIMAL, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 0, 7);

    private static void doTestInfeasibleNode(final ExpressionsBasedModel model, final int[] lower, final int[] upper) {

        model.relax(true);

        for (int v = 0; v < upper.length; v++) {
            model.getVariable(v).lower(lower[v]).upper(upper[v]);
        }

        Result result = model.minimise();

        if (OptimisationIntegerTests.DEBUG) {
            BasicLogger.debug(result);
            BasicLogger.debug(model);
        }

        TestUtils.assertEquals(State.INFEASIBLE, result.getState());
    }

    private static void doTestRelaxedAtSpecificNode(final ExpressionsBasedModel model, final int[] lower, final int[] upper) {

        model.relax(true);

        for (int v = 0; v < upper.length; v++) {
            model.getVariable(v).lower(lower[v]).upper(upper[v]);
        }

        if (DEBUG) {
            model.options.debug(Optimisation.Solver.class);
        }

        Result result = model.minimise();

        if (OptimisationIntegerTests.DEBUG) {
            BasicLogger.debug(result);
            BasicLogger.debug(model);
        }

        TestUtils.assertSolutionValid(model, result);
        TestUtils.assertStateNotLessThanOptimal(result);
        TestUtils.assertNotMoreThan(CPLEX_RESULTS.getValue(), result.getValue());
    }

    private static void doTestToMatchExpected(final ExpressionsBasedModel model) {

        if (DEBUG) {
            model.options.debug(IntegerSolver.class);
        }

        Result result = model.minimise();

        if (DEBUG) {
            BasicLogger.debug(result);
            BasicLogger.debug(model);
        }

        TestUtils.assertStateAndSolution(CPLEX_RESULTS, result);
    }

    static ExpressionsBasedModel makeModel() {

        ExpressionsBasedModel retVal = new ExpressionsBasedModel();

        double[] weights = new double[] { 2691.5357279536333, 2600.760150603986, 2605.8958795795374, 2606.7208332501104, 2715.0757845953835, 2602.194912040238,
                2606.0069468717575, 2609.0385816244316, 2750.0520522057927, 2602.048261785581, 2600.507229973181, 2602.046307869504, 2721.343937605796,
                2601.7367414553805, 2600.595318433882, 2599.405979211142 };

        for (int v = 0; v < weights.length; v++) {
            retVal.addVariable(Variable.make("x" + v).integer(true).lower(0).upper(414).weight(weights[v]));
        }

        // 117 <= 30 30 30 30 0 4 0 0 0 4 0 0 0 4 0 0 <= 14868
        // 36 <= 0 4 0 0 40 40 40 40 0 0 4 0 0 0 4 0 <= 170569
        // 341 <= 0 0 8 0 0 0 8 0 68 68 68 68 0 0 0 5 <= 140833
        // 413 <= 0 0 0 8 0 0 0 9 0 0 0 6 59 59 59 59 <= 48321

        int[] lower = new int[] { 117, 36, 341, 413 };
        int[] upper = new int[] { 14868, 170569, 140833, 48321 };
        int[][] factors = new int[4][];
        factors[0] = new int[] { 30, 30, 30, 30, 0, 4, 0, 0, 0, 4, 0, 0, 0, 4, 0, 0 };
        factors[1] = new int[] { 0, 4, 0, 0, 40, 40, 40, 40, 0, 0, 4, 0, 0, 0, 4, 0 };
        factors[2] = new int[] { 0, 0, 8, 0, 0, 0, 8, 0, 68, 68, 68, 68, 0, 0, 0, 5 };
        factors[3] = new int[] { 0, 0, 0, 8, 0, 0, 0, 9, 0, 0, 0, 6, 59, 59, 59, 59 };

        for (int c = 0; c < factors.length; c++) {
            Expression tmpExpr = retVal.addExpression("C" + c);
            tmpExpr.lower(lower[c]).upper(upper[c]);
            for (int v = 0; v < factors[c].length; v++) {
                tmpExpr.set(v, factors[c][v]);
            }
        }

        return retVal;
    }

    /**
     * Just a branch(ing) that seemed interesting at some point...
     *
     * <pre>
     * 8 (5) 7=0.09999999999999998 38573.68831776007 [0=0<414, 1=0<414, 2=0<414, 3=0<414, 4=0<414, 5=0<414, 6=0<414, 7=1<414, 8=0<414, 9=0<5, 10=0<0, 11=0<0, 12=0<414, 13=0<414, 14=0<414, 15=0<414]
     * Still hope, branching on 2 @ 0.12500000000000178 >>> 0 <= x2 (2605.89588) <= 414
     * </pre>
     */
    @Test
    public void testBranchingOn2() {

        ExpressionsBasedModel integerModel = P20140819.makeModel();

        int[] lower = new int[] { 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0 };
        int[] upper = new int[] { 414, 414, 414, 414, 414, 414, 414, 414, 414, 5, 0, 0, 414, 414, 414, 414 };

        ExpressionsBasedModel parentModel = integerModel.copy(true);
        P20140819.doTestRelaxedAtSpecificNode(parentModel, lower, upper);

        ExpressionsBasedModel lowerModel = integerModel.copy(true);
        lower[2] = 0;
        upper[2] = 0;
        P20140819.doTestRelaxedAtSpecificNode(lowerModel, lower, upper);

        ExpressionsBasedModel upperModel = integerModel.copy(true);
        lower[2] = 1;
        upper[2] = 414;
        P20140819.doTestRelaxedAtSpecificNode(upperModel, lower, upper);
    }

    /**
     * Tweaked the CPLEX integration so that the ojAlgo {@link IntegerSolver} would handle the integer part
     * and CPLEX solve the LP at each node. Also cleared all ojAlgo presolvers.Then took note of the 9 first
     * nodes that CPLEX reported as infeasible.<br>
     * <br>
     * 15 (13) 6=0.12500000000000178 39086.517558237 [0=0<414, 1=0<414, 2=0<0, 3=0<414, 4=0<414, 5=0<414,
     * 6=0<0, 7=1<414, 8=0<0, 9=0<5, 10=0<0, 11=0<0, 12=0<414, 13=0<414, 14=0<414, 15=0<0]
     */
    @Test
    public void testInfeasibleNode1() {

        ExpressionsBasedModel model = P20140819.makeModel();

        int[] lower = new int[] { 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0 };
        int[] upper = new int[] { 414, 414, 0, 414, 414, 414, 0, 414, 0, 5, 0, 0, 414, 414, 414, 0 };

        P20140819.doTestInfeasibleNode(model, lower, upper);
    }

    /**
     * 31 (29) 5=0.25000000000000133 44899.212478491245 [0=0<0, 1=0<0, 2=0<0, 3=0<2, 4=0<414, 5=0<0, 6=0<414,
     * 7=1<414, 8=0<414, 9=6<6, 10=0<414, 11=0<414, 12=0<414, 13=0<8, 14=0<414, 15=0<414]
     *
     * @see P20140819#testInfeasibleNode1()
     */
    @Test
    public void testInfeasibleNode2() {

        ExpressionsBasedModel model = P20140819.makeModel();

        int[] lower = new int[] { 0, 0, 0, 0, 0, 0, 0, 1, 0, 6, 0, 0, 0, 0, 0, 0 };
        int[] upper = new int[] { 0, 0, 0, 2, 414, 0, 414, 414, 414, 6, 414, 414, 414, 8, 414, 414 };

        P20140819.doTestInfeasibleNode(model, lower, upper);
    }

    /**
     * 47 (45) 6=0.12500000000005684 39159.115850268216 [0=0<414, 1=0<414, 2=0<0, 3=0<414, 4=0<414, 5=0<414,
     * 6=0<0, 7=1<414, 8=0<0, 9=0<4, 10=0<0, 11=1<1, 12=0<414, 13=0<414, 14=0<414, 15=0<0]
     *
     * @see P20140819#testInfeasibleNode1()
     */
    @Test
    public void testInfeasibleNode3() {

        ExpressionsBasedModel model = P20140819.makeModel();

        int[] lower = new int[] { 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0 };
        int[] upper = new int[] { 414, 414, 0, 414, 414, 414, 0, 414, 0, 4, 0, 1, 414, 414, 414, 0 };

        P20140819.doTestInfeasibleNode(model, lower, upper);
    }

    /**
     * 61 (60) 15=0.20000000000000284 39126.030304871405 [0=0<414, 1=0<414, 2=0<0, 3=0<414, 4=0<414, 5=0<414,
     * 6=0<0, 7=1<414, 8=0<0, 9=0<4, 10=1<1, 11=0<0, 12=0<414, 13=0<414, 14=0<414, 15=0<0]
     *
     * @see P20140819#testInfeasibleNode1()
     */
    @Test
    public void testInfeasibleNode4() {

        ExpressionsBasedModel model = P20140819.makeModel();

        int[] lower = new int[] { 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0 };
        int[] upper = new int[] { 414, 414, 0, 414, 414, 414, 0, 414, 0, 4, 1, 0, 414, 414, 414, 0 };

        P20140819.doTestInfeasibleNode(model, lower, upper);
    }

    /**
     * 77 (75) 15=0.2 38949.69783865135 [0=0<414, 1=0<0, 2=0<0, 3=0<2, 4=0<414, 5=1<414, 6=0<0, 7=0<0, 8=0<0,
     * 9=5<5, 10=0<0, 11=0<0, 12=0<414, 13=0<414, 14=0<414, 15=0<0]
     *
     * @see P20140819#testInfeasibleNode1()
     */
    @Test
    public void testInfeasibleNode5() {

        ExpressionsBasedModel model = P20140819.makeModel();

        int[] lower = new int[] { 0, 0, 0, 0, 0, 1, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0 };
        int[] upper = new int[] { 414, 0, 0, 2, 414, 414, 0, 0, 0, 5, 0, 0, 414, 414, 414, 0 };

        P20140819.doTestInfeasibleNode(model, lower, upper);
    }

    /**
     * 93 (91) 5=0.2500000000000002 44892.11532184435 [0=0<0, 1=0<1, 2=1<1, 3=0<0, 4=0<414, 5=0<0, 6=0<414,
     * 7=1<414, 8=0<414, 9=5<5, 10=0<0, 11=0<0, 12=0<414, 13=7<9, 14=0<414, 15=0<414]
     *
     * @see P20140819#testInfeasibleNode1()
     */
    @Test
    public void testInfeasibleNode6() {

        ExpressionsBasedModel model = P20140819.makeModel();

        int[] lower = new int[] { 0, 0, 1, 0, 0, 0, 0, 1, 0, 5, 0, 0, 0, 7, 0, 0 };
        int[] upper = new int[] { 0, 1, 1, 0, 414, 0, 414, 414, 414, 5, 0, 0, 414, 9, 414, 414 };

        P20140819.doTestInfeasibleNode(model, lower, upper);
    }

    /**
     * 101 (99) 6=0.12500000000000178 39540.58034803606 [0=0<414, 1=0<414, 2=0<0, 3=0<414, 4=0<414, 5=0<414,
     * 6=0<0, 7=1<414, 8=1<1, 9=0<4, 10=0<0, 11=0<0, 12=0<414, 13=0<414, 14=0<414, 15=0<0]
     *
     * @see P20140819#testInfeasibleNode1()
     */
    @Test
    public void testInfeasibleNode7() {

        ExpressionsBasedModel model = P20140819.makeModel();

        int[] lower = new int[] { 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0 };
        int[] upper = new int[] { 414, 414, 0, 414, 414, 414, 0, 414, 1, 4, 0, 0, 414, 414, 414, 0 };

        P20140819.doTestInfeasibleNode(model, lower, upper);
    }

    /**
     * 115 (113) 5=0.2499999999999991 47486.38557207994 [0=0<0, 1=0<2, 2=0<0, 3=0<0, 4=0<414, 5=0<0, 6=0<414,
     * 7=1<414, 8=0<0, 9=5<5, 10=0<0, 11=0<0, 12=0<414, 13=6<9, 14=0<414, 15=1<414]
     *
     * @see P20140819#testInfeasibleNode1()
     */
    @Test
    public void testInfeasibleNode8() {

        ExpressionsBasedModel model = P20140819.makeModel();

        int[] lower = new int[] { 0, 0, 0, 0, 0, 0, 0, 1, 0, 5, 0, 0, 0, 6, 0, 1 };
        int[] upper = new int[] { 0, 2, 0, 0, 414, 0, 414, 414, 0, 5, 0, 0, 414, 9, 414, 414 };

        P20140819.doTestInfeasibleNode(model, lower, upper);
    }

    /**
     * 127 (125) 5=0.249999999999998 47504.9079050328 [0=0<0, 1=0<0, 2=0<0, 3=0<2, 4=0<414, 5=0<0, 6=1<414,
     * 7=1<414, 8=0<0, 9=5<5, 10=0<0, 11=0<0, 12=0<414, 13=0<9, 14=0<414, 15=0<0]
     *
     * @see P20140819#testInfeasibleNode1()
     */
    @Test
    public void testInfeasibleNode9() {

        ExpressionsBasedModel model = P20140819.makeModel();

        int[] lower = new int[] { 0, 0, 0, 0, 0, 0, 1, 1, 0, 5, 0, 0, 0, 0, 0, 0 };
        int[] upper = new int[] { 0, 0, 0, 2, 414, 0, 414, 414, 0, 5, 0, 0, 414, 9, 414, 0 };

        P20140819.doTestInfeasibleNode(model, lower, upper);
    }

    @Test
    public void testOriginalFullModel() {

        ExpressionsBasedModel model = P20140819.makeModel();

        // model.options.mip_defer = 0.0;
        // model.options.debug(LinearSolver.class);

        P20140819.doTestToMatchExpected(model);
    }

    @Test
    public void testP20140819fix1() {

        ExpressionsBasedModel expModel = P20140819.makeModel();

        int[] lowerBoundsExp = new int[] { 0, 0, 0, 0, 0, 1, 0, 0, 0, 5, 0, 0, 0, 7, 0, 0 };
        int[] upperBoundsExp = new int[] { 0, 2, 0, 0, 414, 1, 414, 414, 414, 5, 414, 0, 414, 8, 414, 414 };
        for (int v = 0; v < upperBoundsExp.length; v++) {
            expModel.getVariable(v).integer(false).lower(lowerBoundsExp[v]).upper(upperBoundsExp[v]);
        }

        Result expResult = expModel.minimise();

        TestUtils.assertStateLessThanFeasible(expResult);
        TestUtils.assertFalse(expModel.validate(expResult));

        ExpressionsBasedModel actModel = P20140819.makeModel();

        int[] lowerBoundsAct = new int[] { 0, 0, 0, 0, 0, 1, 0, 0, 0, 5, 0, 0, 0, 7, 0, 0 };
        int[] upperBoundsAct = new int[] { 0, 2, 0, 0, 414, 414, 414, 414, 414, 5, 414, 0, 414, 8, 414, 414 };
        for (int v = 0; v < upperBoundsAct.length; v++) {
            actModel.getVariable(v).integer(false).lower(lowerBoundsAct[v]).upper(upperBoundsAct[v]);
        }

        actModel.setMinimisation();
        Intermediate intermediate = actModel.prepare();
        Result nodeResult = intermediate.solve();

        TestUtils.assertStateNotLessThanOptimal(nodeResult);
        TestUtils.assertTrue(actModel.validate(nodeResult, BasicLogger.DEBUG));

        Variable variableToFix = actModel.getVariable(5);
        variableToFix.lower(1).upper(1);
        intermediate.update(variableToFix);

        Result fixedResult = intermediate.solve();

        TestUtils.assertStateLessThanFeasible(fixedResult);
        TestUtils.assertFalse(expModel.validate(fixedResult));
    }

    @Test
    public void testP20140819fix2() {

        ExpressionsBasedModel nodeModel = P20140819.makeModel();

        int[] nodeLowerBounds = new int[] { 1, 0, 0, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 7, 0, 0 };
        int[] nodeUpperBounds = new int[] { 1, 1, 0, 0, 414, 0, 414, 414, 414, 5, 414, 0, 414, 414, 414, 414 };
        for (int v = 0; v < nodeUpperBounds.length; v++) {
            nodeModel.getVariable(v).integer(false).lower(nodeLowerBounds[v]).upper(nodeUpperBounds[v]);
        }

        Result nodeResult = nodeModel.minimise();

        TestUtils.assertStateNotLessThanOptimal(nodeResult);
        TestUtils.assertTrue(nodeModel.validate(nodeResult, BasicLogger.DEBUG));

        ExpressionsBasedModel parentModel = P20140819.makeModel();

        int[] parentLowerBounds = new int[] { 1, 0, 0, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 7, 0, 0 };
        int[] parentUpperBounds = new int[] { 414, 1, 0, 0, 414, 0, 414, 414, 414, 5, 414, 0, 414, 414, 414, 414 };
        for (int v = 0; v < parentUpperBounds.length; v++) {
            parentModel.getVariable(v).integer(false).lower(parentLowerBounds[v]).upper(parentUpperBounds[v]);
        }

        if (DEBUG) {
            BasicLogger.debug(parentModel);
            parentModel.options.debug(LinearSolver.class);
        }

        parentModel.setMinimisation();
        Intermediate intermediate = parentModel.prepare();
        Result parentResult = intermediate.solve();

        if (DEBUG) {
            BasicLogger.debug(parentResult);
        }

        TestUtils.assertStateNotLessThanOptimal(parentResult);
        TestUtils.assertTrue(parentModel.validate(parentResult, BasicLogger.DEBUG));

        Variable variableToFix = parentModel.getVariable(0);
        variableToFix.lower(1).upper(1);
        intermediate.update(variableToFix);

        Result fixedResult = intermediate.solve();

        TestUtils.assertStateNotLessThanOptimal(fixedResult);
        TestUtils.assertTrue(fixedResult.getValue() >= parentResult.getValue());
        TestUtils.assertTrue(nodeModel.validate(fixedResult, BasicLogger.DEBUG));
        TestUtils.assertStateAndSolution(nodeResult, fixedResult);
    }

    @Test
    public void testP20140819fix3() {

        ExpressionsBasedModel nodeModel = P20140819.makeModel();

        int[] nodeLowerBounds = new int[] { 0, 0, 0, 0, 0, 2, 0, 1, 0, 0, 0, 0, 0, 7, 0, 0 };
        int[] nodeUpperBounds = new int[] { 0, 0, 2, 0, 414, 414, 414, 414, 414, 5, 0, 0, 414, 7, 414, 414 };
        for (int v = 0; v < nodeUpperBounds.length; v++) {
            nodeModel.getVariable(v).integer(false).lower(nodeLowerBounds[v]).upper(nodeUpperBounds[v]);
        }

        Result nodeResult = nodeModel.minimise();

        TestUtils.assertStateNotLessThanOptimal(nodeResult);
        TestUtils.assertTrue(nodeModel.validate(nodeResult, BasicLogger.DEBUG));

        ExpressionsBasedModel parentModel = P20140819.makeModel();

        int[] parentLowerBounds = new int[] { 0, 0, 0, 0, 0, 2, 0, 1, 0, 0, 0, 0, 0, 7, 0, 0 };
        int[] parentUpperBounds = new int[] { 0, 0, 2, 0, 414, 414, 414, 414, 414, 5, 0, 0, 414, 8, 414, 414 };
        for (int v = 0; v < parentUpperBounds.length; v++) {
            parentModel.getVariable(v).integer(false).lower(parentLowerBounds[v]).upper(parentUpperBounds[v]);
        }

        parentModel.setMinimisation();
        Intermediate intermediate = parentModel.prepare();
        Result parentResult = intermediate.solve();

        TestUtils.assertStateNotLessThanOptimal(parentResult);
        TestUtils.assertTrue(parentModel.validate(parentResult, BasicLogger.DEBUG));

        Variable variableToFix = parentModel.getVariable(13);
        variableToFix.lower(7).upper(7);
        intermediate.update(variableToFix);

        Result fixedResult = intermediate.solve();

        TestUtils.assertStateNotLessThanOptimal(fixedResult);
        TestUtils.assertTrue(nodeModel.validate(fixedResult, BasicLogger.DEBUG));
        TestUtils.assertStateAndSolution(nodeResult, fixedResult);
    }

    /**
     * <pre>
     * Branch&Bound Node
     * 75 (73) 15=0.19999999999998863 38949.697838651235 [0=0<414, 1=0<0, 2=0<0, 3=0<2, 4=0<414, 5=1<414, 6=0<0, 7=0<0, 8=0<0, 9=5<5, 10=0<0, 11=0<0, 12=0<414, 13=0<414, 14=0<414, 15=0<0]
     * Solutions=0 Nodes/Iterations=0 { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 }
     * Node Result: { 0.2028248587570723, 0.0, 0.0, 2.0, 0.0, 1.0, 0.0, 0.0, 0.0, 5.0, 0.0, 0.0, 0.0, 6.728813559321958, 0.0, 0.0 }
     * Node solved to optimality!
     * 340.0 ! 341 <= C2 <= 140833
     * Node solution marked as OPTIMAL, but is actually INVALID/INFEASIBLE/FAILED. Stop this branch!
     * Integer indices: [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15]
     * Lower bounds: [0, 0, 0, 0, 0, 1, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0]
     * Upper bounds: [414, 0, 0, 2, 414, 414, 0, 0, 0, 5, 0, 0, 414, 414, 414, 0]
     * </pre>
     */
    @Test
    public void testProblematicNodeB() {

        ExpressionsBasedModel model = P20140819.makeModel();

        int[] lower = new int[] { 0, 0, 0, 0, 0, 1, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0 };
        int[] upper = new int[] { 414, 0, 0, 2, 414, 414, 0, 0, 0, 5, 0, 0, 414, 414, 414, 0 };

        P20140819.doTestInfeasibleNode(model, lower, upper);
    }

    @Test
    public void testRelaxedAtSpecificNode1() {

        ExpressionsBasedModel model = P20140819.makeModel();

        int[] lower = new int[] { 0, 0, 0, 0, 0, 0, 0, 1, 0, 6, 0, 0, 0, 0, 0, 0 };
        int[] upper = new int[] { 0, 0, 0, 2, 414, 0, 414, 414, 414, 6, 414, 414, 414, 8, 414, 414 };

        P20140819.doTestInfeasibleNode(model, lower, upper);
    }

    @Test
    public void testRelaxedButConstrainedToOptimal() {

        ExpressionsBasedModel model = P20140819.makeModel().copy(true);

        for (int v = 0; v < CPLEX_RESULTS.size(); v++) {
            model.getVariable(v).level(CPLEX_RESULTS.longValue(v));
        }

        P20140819.doTestToMatchExpected(model);
    }

    @Test
    public void testVariablesLevelSet() {

        ExpressionsBasedModel model = P20140819.makeModel();

        for (int v = 0; v < CPLEX_RESULTS.size(); v++) {
            model.getVariable(v).level(CPLEX_RESULTS.longValue(v));
        }

        P20140819.doTestToMatchExpected(model);
    }

    @Test
    public void testVariablesLowerSet() {

        ExpressionsBasedModel model = P20140819.makeModel();

        for (int v = 0; v < CPLEX_RESULTS.size(); v++) {
            model.getVariable(v).lower(CPLEX_RESULTS.longValue(v));
        }

        P20140819.doTestToMatchExpected(model);
    }

    @Test
    public void testVariablesNonzeroLevelSet() {

        ExpressionsBasedModel model = P20140819.makeModel();

        for (int v = 0; v < CPLEX_RESULTS.size(); v++) {
            long level = CPLEX_RESULTS.longValue(v);
            if (level != 0L) {
                model.getVariable(v).level(level);
            }
        }

        P20140819.doTestToMatchExpected(model);
    }

    @Test
    public void testVariablesUpperSet() {

        ExpressionsBasedModel model = P20140819.makeModel();

        for (int v = 0; v < CPLEX_RESULTS.size(); v++) {
            model.getVariable(v).upper(CPLEX_RESULTS.longValue(v));
        }

        P20140819.doTestToMatchExpected(model);
    }

    @Test
    public void validateSolutionFromCPLEX() {

        ExpressionsBasedModel model = P20140819.makeModel();

        TestUtils.assertTrue(model.validate(CPLEX_RESULTS, BasicLogger.DEBUG));
    }

}

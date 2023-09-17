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

import static org.ojalgo.function.constant.BigMath.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.function.constant.BigMath;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.optimisation.Optimisation.State;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.optimisation.linear.LinearProblems;
import org.ojalgo.type.context.NumberContext;

public class IntegerProblems extends OptimisationIntegerTests {

    /**
     * <pre>
     * OPTIMAL -97.59 @ { 0, 1, 0, 1, 0, 1, 1, 0 }
     * ############################################
     * 0 <= X0: 0 (-4836) <= 1
     * 0 <= X1: 1 (-4824) <= 1
     * 0 <= X2: 0 (1921) <= 1
     * 0 <= X3: 1 (1929.9) <= 1
     * 0 <= X4: 0 (2517) <= 1
     * 0 <= X5: 1 (2526.51) <= 1
     * 0 <= X6: 1 (270) <= 1
     * 0 <= X7: 0 (267.5) <= 1
     * 1 <= EXPR2: 1.0 <= 1
     * 1 <= EXPR1: 1.0 <= 1
     * 1 <= EXPR3: 1.0 <= 1
     * 1 <= EXPR0: 1.0 <= 1
     * ############################################
     * </pre>
     */
    public static ExpressionsBasedModel makeModelGitHub513() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable varA1 = model.addVariable().weight(-4836.0).binary();
        Variable varA2 = model.addVariable().weight(-4824.0).binary();
        Variable varB1 = model.addVariable().weight(1921.0).binary();
        Variable varB2 = model.addVariable().weight(1929.9).binary();
        Variable varC1 = model.addVariable().weight(2517.0).binary();
        Variable varC2 = model.addVariable().weight(2526.51).binary();
        Variable varD1 = model.addVariable().weight(270.0).binary();
        Variable varD2 = model.addVariable().weight(267.5).binary();

        varA1.setValue(0);
        varA2.setValue(1);
        varB1.setValue(0);
        varB2.setValue(1);
        varC1.setValue(0);
        varC2.setValue(1);
        varD1.setValue(1);
        varD2.setValue(0);

        Expression constraintA = model.addExpression().level(1);
        constraintA.set(varA1, 1);
        constraintA.set(varA2, 1);
        Expression constraintB = model.addExpression().level(1);
        constraintB.set(varB1, 1);
        constraintB.set(varB2, 1);
        Expression constraintC = model.addExpression().level(1);
        constraintC.set(varC1, 1);
        constraintC.set(varC2, 1);
        Expression constraintD = model.addExpression().level(1);
        constraintD.set(varD1, 1);
        constraintD.set(varD2, 1);

        return model;
    }

    /**
     * https://github.com/optimatika/ojAlgo/discussions/513
     * <p>
     * There was a case when the `IntegerSolver` returned an incorrect solution (constraint breaking) but
     * reported it to be optimal. It was actually the `LinearSolver` that malfunctioned, but behaviour in the
     * `IntegerSolver` that generated the problematic node model. Fixed this problem by 1) making sure the
     * `LinearSolver` handles that case, and 2) altered the problematic behaviour in the `IntegerSolver` to be
     * "safer".
     *
     * @see LinearProblems#testGitHub513()
     */
    @Test
    public void testGitHub513() {

        ExpressionsBasedModel model = IntegerProblems.makeModelGitHub513();

        Result expected = Result.of(-97.59, State.OPTIMAL, 0, 1, 0, 1, 0, 1, 1, 0);

        Result actual = model.maximise();

        if (DEBUG) {
            BasicLogger.debug(actual);
            BasicLogger.debug(model);
            model.validate(actual, BasicLogger.DEBUG);
        }

        TestUtils.assertStateAndSolution(expected, actual);
    }

    /**
     * 20120227: Forgot to document what the problem was. Now I just check there is an optimal solution.
     */
    @Test
    public void testP20100412() {

        boolean tmpDebug = false;

        ExpressionsBasedModel tmpModel = OptimisationIntegerData.buildModelForP20100412();

        if (tmpDebug) {
            BasicLogger.debug("Model Before");
            BasicLogger.debug(tmpModel.toString());

        }
        Result tmpResult = tmpModel.maximise();
        if (tmpDebug) {
            BasicLogger.debug("Model After");
            BasicLogger.debug(tmpModel.toString());
        }

        TestUtils.assertTrue(tmpResult.getState().isOptimal());
    }

    /**
     * If the relaxed problem was infeasible you got a NullPointerException instead of a result indicating
     * that the problem is infeasible.
     */
    @Test
    public void testP20111010() {

        Variable[] tmpVariables = { Variable.makeBinary("X").weight(ONE), Variable.makeBinary("Y").weight(ONE), Variable.makeBinary("Z").weight(ONE) };

        ExpressionsBasedModel tmpModel = new ExpressionsBasedModel(tmpVariables);

        Expression tmpC1 = tmpModel.newExpression("C1");
        for (int i = 0; i < tmpModel.countVariables(); i++) {
            tmpC1.set(i, ONE);
        }

        tmpC1.level(ONE);

        Expression tmpC2 = tmpModel.newExpression("C2");
        for (int i = 0; i < tmpModel.countVariables(); i++) {
            tmpC2.set(i, ONE);
        }
        tmpC2.level(TWO);

        Expression tmpC3 = tmpModel.newExpression("C3");
        for (int i = 0; i < tmpModel.countVariables(); i++) {
            tmpC3.set(i, ONE);
        }
        tmpC3.level(THREE);

        // tmpModel.options.progress(IntegerSolver.class);

        Optimisation.Result tmpResult = tmpModel.minimise();

        TestUtils.assertEquals(State.INFEASIBLE, tmpResult.getState());
    }

    /**
     * The IP solver returns infeasible solutions, but the problem seems to actually be with the LP solver.
     * Don't know what the actual solution is. Just check that the solver terminates normally and that the
     * solution is in fact feasible/valid.
     */
    @Test
    @Tag("slow")
    public void testP20130225() {

        ExpressionsBasedModel tmpIntegerModel = P20130225.makeModel();
        ExpressionsBasedModel tmpRelaxedModel = tmpIntegerModel.copy(true);

        Optimisation.Result tmpRelaxedResult = tmpRelaxedModel.minimise();
        TestUtils.assertEquals("Solution To Relaxed Problem Not Optimal!", Optimisation.State.OPTIMAL, tmpRelaxedResult.getState());
        TestUtils.assertTrue("Solution To Relaxed Problem Not Valid!", tmpRelaxedModel.validate(tmpRelaxedResult));

        Optimisation.Result tmpIntegerResult = tmpIntegerModel.minimise();
        TestUtils.assertEquals("Integer Solution Not Optimal!", Optimisation.State.OPTIMAL, tmpIntegerResult.getState());
        TestUtils.assertTrue("Integer Solution Not Valid!", tmpIntegerModel.validate(tmpIntegerResult));
    }

    /**
     * apete's implementation of the original problem description.
     */
    @Test
    public void testP20130409a() {

        Variable[] variables = { new Variable("x1").lower(BigMath.ZERO).weight(BigMath.ONE), new Variable("x2013").lower(BigMath.ZERO).integer(),
                new Variable("x2014").lower(BigMath.ZERO).integer() };

        ExpressionsBasedModel model = new ExpressionsBasedModel(variables);

        Expression expr1 = model.newExpression("Expr1");
        expr1.set(0, -1);
        expr1.set(1, 5100);
        expr1.set(2, -5000);
        expr1.upper(BigMath.ZERO);

        Expression expr2 = model.newExpression("Expr2");
        expr2.set(0, 1);
        expr2.set(1, 5100);
        expr2.set(2, -5000);
        expr2.lower(BigMath.ZERO);

        Expression expr3 = model.newExpression("Expr3");
        expr3.set(1, 5000);
        expr3.set(2, 5000);
        expr3.level(new BigDecimal(19105000));

        Optimisation.Result expected = Optimisation.Result.of(4200, Optimisation.State.OPTIMAL, 4200, 1892, 1929);
        TestUtils.assertTrue("Expected Solution Not Valid", model.validate(expected));

        if (DEBUG) {
            model.options.debug(Optimisation.Solver.class);
        }

        Result actual = model.minimise();

        TestUtils.assertStateAndSolution(expected, actual, NumberContext.of(8));
    }

    /**
     * Test case as sent in by the user / problem reporter
     */
    @Test
    public void testP20130409b() {

        Variable x1 = Variable.make("x1");
        Variable x2013 = Variable.makeInteger("x2013");
        Variable x2014 = Variable.makeInteger("x2014");
        Variable x2015 = Variable.makeInteger("x2015");

        ExpressionsBasedModel model = new ExpressionsBasedModel();
        model.addVariable(x1);
        model.addVariable(x2013);
        model.addVariable(x2014);
        model.addVariable(x2015);

        Expression obj = model.newExpression("obj");
        obj.set(x1, 1);
        obj.weight(1);

        Expression c1 = model.newExpression("c1");
        c1.set(x1, 1);
        c1.lower(0);

        Expression c2 = model.newExpression("c2");
        c2.set(x2014, -5000);
        c2.set(x2013, 5100);
        c2.set(x1, -1);
        c2.upper(0);

        Expression c3 = model.newExpression("c3");
        c3.set(x2014, -5000);
        c3.set(x2013, 5100);
        c3.set(x1, 1);
        c3.lower(0);

        Expression c4 = model.newExpression("c4");
        c4.set(x2014, 150);
        c4.set(x2013, 5100);
        c4.set(x2015, -5000);
        c4.set(x1, -1);
        c4.upper(0);

        Expression c5 = model.newExpression("c5");
        c5.set(x2014, 150);
        c5.set(x2013, 5100);
        c5.set(x2015, -5000);
        c5.set(x1, 1);
        c5.lower(0);

        Expression c6 = model.newExpression("c6");
        c6.set(x2015, 5000);
        c6.set(x2014, 5000);
        c6.set(x2013, 5000);
        c6.level(19105000);

        Optimisation.Result expected = Result.of(4850, State.OPTIMAL, 4850, 1245, 1269, 1307);
        TestUtils.assertTrue("Expected Solution Not Valid", model.validate(expected));

        if (DEBUG) {
            model.options.debug(Optimisation.Solver.class);
        }

        Result actual = model.minimise();

        if (DEBUG) {
            BasicLogger.debug(actual.toString());
        }

        TestUtils.assertStateAndSolution(expected, actual);
    }

    @Test
    public void testP20150127full() {

        ExpressionsBasedModel tmpModel = P20150127a.getModel();

        Optimisation.Result tmpResult = tmpModel.minimise();

        // Model should be solvable (e.g. x=201, y=-10)?!
        TestUtils.assertStateNotLessThanFeasible(tmpResult);

        BigDecimal tmpSolX = tmpResult.get(0);
        BigDecimal tmpSolY = tmpResult.get(1);
        int tmpIntX = tmpSolX.setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
        int tmpIntY = tmpSolY.setScale(0, BigDecimal.ROUND_HALF_UP).intValue();

        if (OptimisationIntegerTests.DEBUG) {
            BasicLogger.debug("x = " + tmpSolX + " ~ " + tmpIntX);
            BasicLogger.debug("y = " + tmpSolY + " ~ " + tmpIntY);
        }

        TestUtils.assertTrue("Solution not valid!", tmpModel.validate(tmpResult));

        // Verify solution
        for (int[] tmpCoeff : P20150127a.getCoefficients()) {
            int tmpValue = tmpCoeff[0] * tmpIntX + tmpCoeff[1] * tmpIntY;
            BigDecimal tmpExact = tmpSolX.multiply(BigDecimal.valueOf(tmpCoeff[0])).add(tmpSolY.multiply(BigDecimal.valueOf(tmpCoeff[1])));
            if (tmpValue >= 0) {
                TestUtils.fail(tmpCoeff[0] + "*x + " + tmpCoeff[1] + "*y = " + tmpValue + " must be negative (exact: " + tmpExact + ")");
            }
        }
    }

    @Test
    public void testP20150127infeasibleNode() {

        ExpressionsBasedModel tmpModel = P20150127b.getModel(true, false);

        Optimisation.Result tmpResult = tmpModel.minimise();

        // Model is infeasible, and must be reported as such
        TestUtils.assertStateLessThanFeasible(tmpResult);
    }

    /**
     * <p>
     * I am trying to call Ojalgo 40 from AnyLogic 7.3.2 (http://www.anylogic..com/downloads) on Ubuntu 16.04
     * in order to solve a Traveling Salesman Problem, but Ojalgo sometimes stops on a feasible solution
     * before the optimum. The following code works well without Anylogic and always finds 917.31 as optimal
     * solution: (Simply copy/paste the following code in a file called "Tsp.java" in order to test it.)
     * </p>
     * <p>
     * Next, I try to run the same code in the "On startup" section of the "Agent actions" of the "Main" agent
     * in an AnyLogic model. (Click on the project name "Ojalgo" to change the location of ojalgo-40.0.0.jar,
     * like in the attached file.) Unfortunately, the obtained result is not always 917.31, but also sometimes
     * 1099.22 and 1161.84. I do not understand why the solution randomly changes. I also call Cplex from
     * AnyLogic to solve this same problem, which always returns the optimal solution, hence the problem seems
     * not to be due to AnyLogic. As shown by the above Java code, the problem is not due to Ojalgo as well,
     * but only related to the coupling of AnyLogic and Ojalgo. Thank you very much for Ojalgo and your help!
     * </p>
     * <p>
     * apete: ExpressionsBasedModel has a feature that automatically rescales model parameters (to maximize
     * numerical accuracy) before invoking the solver. The current implementation of that feature (apparently)
     * doesn?t work very well with extremely large parameters in the model. I have now modified the behavior
     * of that feature to not scale anything when/if there are extremely large or small parameters present. As
     * far as I can see that solves the problem with your model.
     * </p>
     * <p>
     * apete (later): Have also improved the presolve functionality to fix (not-include) uncorrelated and/or
     * unbounded variables. (Doesn't handle every case, but this one a a few more.) This was the real fix for
     * this problem!
     * </p>
     */
    @Test
    public void testSimpleTSP20160701() {

        int n = 6;
        double[][] c = new double[n][n];
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

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        //DECISION VARIABLES
        Variable[][] x = new Variable[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                x[i][j] = model.newVariable("x" + i + "_" + j).binary().weight(c[i][j]);
            }
        }
        Variable[] u = new Variable[n];
        for (int i = 1; i < n; i++) {
            u[i] = model.newVariable("u" + i);
        }

        //CONSTRAINTS
        //forall(i in cities)
        //flow_out:
        //sum(j in cities : i!=j) x[i][j]==1;
        for (int i = 0; i < n; i++) {
            Expression constraint_line = model.newExpression("constraint_line_" + i).lower(1).upper(1);
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
            Expression constraint_column = model.newExpression("constraint_column_" + j).lower(1).upper(1);
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
                    Expression constraint_subroute = model.newExpression("constraint_subroute_" + i + "_" + j).upper(n - 1);
                    constraint_subroute.set(u[i], 1);
                    constraint_subroute.set(u[j], -1);
                    constraint_subroute.set(x[i][j], n);
                }
            }
        }

        if (OptimisationIntegerTests.DEBUG) {
            model.options.debug(IntegerSolver.class);
        }

        Optimisation.Result result = model.minimise();

        if (OptimisationIntegerTests.DEBUG) {
            System.out.print("u=\n\t  ");
            for (int i = 1; i < n; i++) {
                System.out.print(u[i].getValue().intValue() + " ");
            }
            System.out.print("\nx=\n\t");
            for (int i = 0; i < n; i++) {
                System.out.print(i + " ");
            }
            System.out.println();
            for (int i = 0; i < n; i++) {
                System.out.print(i + "\t");
                for (int j = 0; j < n; j++) {
                    System.out.print(x[i][j].getValue().intValue() + " ");
                }
                System.out.println();
            }
            System.out.println("\nResult = " + result);
        }

        TestUtils.assertStateNotLessThanOptimal(result);
        TestUtils.assertTrue(model.validate(result));
        TestUtils.assertEquals(917.31349493942, result.getValue());
    }

    /**
     * https://github.com/optimatika/ojAlgo/issues/309
     */
    @Test
    public void testSimplificationGitHubIssue309() {

        NumberContext precision = NumberContext.of(14, 12);
        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable varX = model.newVariable("X").binary();
        Variable varY = model.newVariable("Y").binary();

        // (X <= Y * 0.5)  equivalent to  (0 <= Y * 0.5 - X)
        // only one solution: X = 0, Y = 1
        model.newExpression("X < Y * 0.5").lower(ZERO).set(varX, NEG).set(varY, HALF);
        // objective function
        model.newExpression("sum").weight(ONE).set(varX, ONE).set(varY, ONE);

        if (DEBUG) {
            BasicLogger.debug("Original 1 model");
            BasicLogger.debug(model);
        }

        Result result = model.maximise();

        ExpressionsBasedModel simplifiedModel = model.simplify();

        if (DEBUG) {
            BasicLogger.debug();
            BasicLogger.debug("Original 2 model");
            BasicLogger.debug(model);
            BasicLogger.debug();
            BasicLogger.debug("Simplified model");
            BasicLogger.debug(simplifiedModel);
        }

        Result resultWithSimplification = simplifiedModel.maximise();

        TestUtils.assertStateAndSolution(result, resultWithSimplification, precision);

        TestUtils.assertStateNotLessThanOptimal(resultWithSimplification);
        TestUtils.assertEquals(1.0, resultWithSimplification.getValue(), precision);
    }

    /**
     * 2 iterations simply isn't enough for anything to complete normally. The (originally) returned state
     * INFEASIBLE is misleading. Either it should be FEASIBLE/OPTIMAL as the returned solutions happens to be
     * (total coincident) or UNEXPLORED/FAILED to reflect what the solver managed to do.
     *
     * @see https://github.com/optimatika/ojAlgo/issues/310
     */
    @Test
    public void testStatusForAbortedOptimizationGitHubIssue310() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        // model.options.debug(Optimisation.Solver.class);
        model.options.iterations_abort = 2;

        Variable varX = model.newVariable("X").binary();
        Variable varY = model.newVariable("Y").binary();

        // two possible solutions: X = 0, Y = 1 or X = 1, Y = 0
        Expression constraint = model.newExpression("X + Y <= 1").upper(ONE).set(varX, ONE).set(varY, ONE);

        // function maximized by X = 1
        Expression objectiveFunction = model.newExpression("X").weight(ONE).set(varX, ONE);

        Result result = model.maximise();

        if (DEBUG) {
            BasicLogger.debug(result);
        }

        TestUtils.assertNotEquals(result.getState(), Optimisation.State.INFEASIBLE);
    }

}

/*
 * Copyright 1997-2026 Optimatika
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
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.Timeout.ThreadMode;
import org.ojalgo.TestUtils;
import org.ojalgo.array.ArrayR256;
import org.ojalgo.function.constant.BigMath;
import org.ojalgo.matrix.MatrixQ128;
import org.ojalgo.matrix.MatrixR064;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.ModelFileTest;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.optimisation.Optimisation.Sense;
import org.ojalgo.optimisation.Optimisation.State;
import org.ojalgo.optimisation.OptimisationCase;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.optimisation.integer.IntegerProblems;
import org.ojalgo.optimisation.integer.OptimisationIntegerData;
import org.ojalgo.optimisation.integer.P20150127b;
import org.ojalgo.type.TypeUtils;
import org.ojalgo.type.context.NumberContext;

public class LinearProblems extends OptimisationLinearTests {

    private static final NumberContext ACCURACY = NumberContext.of(7, 6);

    /**
     * https://github.com/optimatika/ojAlgo/issues/546
     */
    static OptimisationCase makeGitHub546() {

        // INFEASIBLE 8.5755E8 @ { 7.84804E+8, 0, 0, 7.2746E+7 }
        // OPTIMAL    8.5755E8 @ { 7.84804E+8, 0, 0, 7.2746E+7 }

        Optimisation.Result result = Result.of(8.5755E8, State.OPTIMAL, 7.84804E+8, 0, 0, 7.2746E+7);

        ExpressionsBasedModel model = ModelFileTest.makeModel("usersupplied", "GitHub546.ebm", false);
        // The old/classic solver cannot handle this problem.
        // (That was the original bug report.)
        // Have to force use of the newer dual solver.
        model.options.linear().dual();

        return OptimisationCase.of(model, Sense.MIN, result);
    }

    /**
     * https://github.com/optimatika/ojAlgo/issues/117
     * <p>
     * Problem was getting different state after second solve.
     */
    @Test
    public void precisionTestDoubleRunInfeasible() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();
        Variable d35 = model.newVariable("d35");
        Variable d56 = model.newVariable("d56");
        Variable d57 = model.newVariable("d57");

        // d35 = 0.0;
        d35.level(0.0);

        // d56 - d35 = -2000.0400000000002;
        Expression expression1 = model.newExpression("d56 - d35");
        expression1.set(d56, 1.0);
        expression1.set(d35, -1.0);
        expression1.level(-2000.0400000000002);

        // d57 - d56 = 0.0;
        Expression expression2 = model.newExpression("d57 - d56");
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

    /**
     * https://github.com/optimatika/ojAlgo/discussions/513
     * <p>
     * The key problem was that the LP solver failed to solve a (the root) sub-problem. It failed in the worst
     * possible way – returned a constraint breaking solution but still reported it to be optimal. This test
     * case reproduces that node model.
     */
    @Test
    public void testGitHub513() {

        ExpressionsBasedModel model = IntegerProblems.makeModelGitHub513();

        Result solutionMIP = Result.of(-97.59, State.OPTIMAL, 0, 1, 0, 1, 0, 1, 1, 0);

        // nudge 9.758999999999999E-5 -> lower constraint = -97.58990241

        // Convert to LP node model
        model.relax();
        model.limitObjective(BigDecimal.valueOf(-97.58990241), null);

        if (DEBUG) {
            model.options.debug(LinearSolver.class);
        }

        Result actual = model.maximise();

        if (DEBUG) {
            BasicLogger.debug(actual);
            BasicLogger.debug(model);
            model.validate(actual, BasicLogger.DEBUG);
        }

        TestUtils.assertStateLessThanFeasible(actual);
    }

    /**
     * https://github.com/optimatika/ojAlgo/issues/546
     */
    @Test
    public void testGitHub546() {
        LinearProblems.makeGitHub546().assertResult();
    }

    /**
     * https://github.com/optimatika/ojAlgo/issues/707
     * <p>
     * Minimise c x subject to a x >= 1 and x >= 0. The solution x = 1 / a is an ordinary number, but the dual
     * ratio c / a overflows. No candidate passed the ratio test and it never returned.
     */
    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = ThreadMode.SEPARATE_THREAD)
    public void testGitHub707OverflowingRatio() {

        double[][] coefficients = { { 1E306, 1E-4 }, { 1E300, 1E-10 }, { 1E308, 0.1 }, { 1E308, 0.01 }, { 1E308, 0.5 } };

        for (Boolean sparse : new Boolean[] { Boolean.FALSE, Boolean.TRUE }) {
            for (double[] pair : coefficients) {

                Optimisation.Options options = new Optimisation.Options();
                options.sparse = sparse;

                LinearSolver.Builder builder = LinearSolver.newGeneralBuilder(new double[] { pair[0] });
                builder.inequality(-1.0, new double[] { -pair[1] });
                builder.lower(new double[] { 0.0 });
                builder.upper(new double[] { Double.POSITIVE_INFINITY });

                Result result = builder.build(options).solve(null);

                TestUtils.assertEquals(State.OPTIMAL, result.getState());
                TestUtils.assertEquals(1.0 / pair[1], result.doubleValue(0), ACCURACY);
                TestUtils.assertTrue(Double.isInfinite(result.getValue()));
            }
        }
    }

    /**
     * https://github.com/optimatika/ojAlgo/issues/707
     * <p>
     * The solution is x = 10, y = 0, but the dual value of the second constraint, 1E312, overflows. The
     * tableau is then left with reduced costs that are NaN, so no candidate can enter. That must end the
     * solve as failed. First the ratio test never returned, and without that the iterations went round
     * without being counted. The revised simplex recomputes the reduced costs and finds the solution.
     */
    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = ThreadMode.SEPARATE_THREAD)
    public void testGitHub707UndefinedRatio() {

        for (Boolean sparse : new Boolean[] { Boolean.FALSE, Boolean.TRUE }) {

            Optimisation.Options options = new Optimisation.Options();
            options.sparse = sparse;

            LinearSolver.Builder builder = LinearSolver.newGeneralBuilder(new double[] { 0.0, -1E305 });
            builder.inequality(-1.0, new double[] { -0.1, -1E-7 });
            builder.inequality(0.0, new double[] { 0.0, 1E-7 });
            builder.lower(new double[] { 0.0, 0.0 });
            builder.upper(new double[] { Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY });

            Result result = builder.build(options).solve(null);

            if (sparse.booleanValue()) {
                TestUtils.assertEquals(State.OPTIMAL, result.getState());
                TestUtils.assertEquals(10.0, result.doubleValue(0), ACCURACY);
                TestUtils.assertEquals(0.0, result.doubleValue(1), ACCURACY);
            } else {
                TestUtils.assertEquals(State.FAILED, result.getState());
            }
        }
    }

    @Test
    public void testMath286() {

        ExpressionsBasedModel modFull = new ExpressionsBasedModel();
        ExpressionsBasedModel modOdd = new ExpressionsBasedModel();
        ExpressionsBasedModel modEven = new ExpressionsBasedModel();

        modFull.newVariable("X1").weight(TENTH.multiply(EIGHT)).lower(TEN);
        modFull.newVariable("X2").weight(TENTH.multiply(TWO)).lower(ZERO);
        modFull.newVariable("X3").weight(TENTH.multiply(SEVEN)).lower(EIGHT);
        modFull.newVariable("X4").weight(TENTH.multiply(THREE)).lower(ZERO);
        modFull.newVariable("X5").weight(TENTH.multiply(SIX)).lower(FIVE);
        modFull.newVariable("X6").weight(TENTH.multiply(FOUR)).lower(ZERO);

        modOdd.newVariable("X1").weight(TENTH.multiply(EIGHT)).lower(TEN);
        modEven.newVariable("X2").weight(TENTH.multiply(TWO)).lower(ZERO);
        modOdd.newVariable("X3").weight(TENTH.multiply(SEVEN)).lower(EIGHT);
        modEven.newVariable("X4").weight(TENTH.multiply(THREE)).lower(ZERO);
        modOdd.newVariable("X5").weight(TENTH.multiply(SIX)).lower(FIVE);
        modEven.newVariable("X6").weight(TENTH.multiply(FOUR)).lower(ZERO);

        //        modFull.options.debug(LinearSolver.class);
        //        modOdd.options.debug(LinearSolver.class);
        //        modEven.options.debug(LinearSolver.class);

        BigDecimal tmpRHS = new BigDecimal("23.0");

        Expression constr1Full = modFull.newExpression("C1");
        for (int i = 0; i < modFull.countVariables(); i++) {
            constr1Full.set(i, new BigDecimal[] { ONE, ZERO, ONE, ZERO, ONE, ZERO }[i]);
        }
        constr1Full.level(tmpRHS);

        Expression constr1Odd = modOdd.newExpression("C1");
        for (int i = 0; i < modOdd.countVariables(); i++) {
            constr1Odd.set(i, new BigDecimal[] { ONE, ONE, ONE }[i]);
        }
        constr1Odd.level(tmpRHS);

        Expression constr2Full = modFull.newExpression("C2");
        for (int i = 0; i < modFull.countVariables(); i++) {
            constr2Full.set(i, new BigDecimal[] { ZERO, ONE, ZERO, ONE, ZERO, ONE }[i]);
        }
        constr2Full.level(tmpRHS);

        Expression constr2Even = modEven.newExpression("C2");
        for (int i = 0; i < modEven.countVariables(); i++) {
            constr2Even.set(i, new BigDecimal[] { ONE, ONE, ONE }[i]);
        }
        constr2Even.level(tmpRHS);

        // A valid solution of 25.8 can be produced with:
        // X1=10, X2=0, X3=8, X4=0, X5=5, X6=23
        BigDecimal claimedValue = new BigDecimal("25.8");

        MatrixR064.DenseReceiver solutionBuilder = MatrixR064.FACTORY.newDenseBuilder(6, 1);
        solutionBuilder.set(0, 0, 10);
        solutionBuilder.set(2, 0, 8);
        solutionBuilder.set(4, 0, 5);
        solutionBuilder.set(5, 0, 23);

        MatrixR064 claimedSolutionFull = solutionBuilder.get();
        MatrixR064 claimedSolutionOdd = claimedSolutionFull.rows(0, 2, 4);
        MatrixR064 claimedSolutionEven = claimedSolutionFull.rows(1, 3, 5);

        TestUtils.assertEquals("Claimed solution not valid!", true, modFull.validate(ArrayR256.FACTORY.copy(claimedSolutionFull), ACCURACY));

        Double tmpActualValue = modFull.objective().toFunction().invoke(R064Store.FACTORY.copy(claimedSolutionFull));
        //  BigDecimal tmpActualValue = TypeUtils.toBigDecimal(tmpObjectiveValue);
        //JUnitUtils.assertEquals("Claimed objective value wrong!", 0, tmpClaimedValue.compareTo(tmpActualValue));
        TestUtils.assertEquals(claimedValue, tmpActualValue, ACCURACY);

        // Start validating ojAlgo results

        Optimisation.Result tmpEvenResult = modEven.maximise();
        Optimisation.Result tmpOddResult = modOdd.maximise();
        Optimisation.Result tmpFullResult = modFull.maximise();

        TestUtils.assertEquals(true, modEven.validate(tmpEvenResult, ACCURACY));
        TestUtils.assertEquals(true, modOdd.validate(tmpOddResult, ACCURACY));
        TestUtils.assertEquals(true, modFull.validate(tmpFullResult, ACCURACY));
        int[] someRows2 = { 0, 1, 2 };

        TestUtils.assertEquals(claimedSolutionEven, MatrixQ128.FACTORY.column(tmpEvenResult).rows(someRows2), ACCURACY);
        int[] someRows3 = { 0, 1, 2 };
        TestUtils.assertEquals(claimedSolutionOdd, MatrixQ128.FACTORY.column(tmpOddResult).rows(someRows3), ACCURACY);
        int[] someRows4 = { 0, 1, 2, 3, 4, 5 };
        TestUtils.assertEquals(claimedSolutionFull, MatrixQ128.FACTORY.column(tmpFullResult).rows(someRows4), ACCURACY);
        int[] someRows5 = { 0, 1, 2 };

        BigDecimal tmpEvenValue = ACCURACY.enforce(TypeUtils
                .toBigDecimal(modEven.objective().toFunction().invoke(R064Store.FACTORY.copy(MatrixR064.FACTORY.column(tmpEvenResult).rows(someRows5)))));
        int[] someRows6 = { 0, 1, 2 };
        BigDecimal tmpOddValue = ACCURACY.enforce(TypeUtils
                .toBigDecimal(modOdd.objective().toFunction().invoke(R064Store.FACTORY.copy(MatrixR064.FACTORY.column(tmpOddResult).rows(someRows6)))));
        int[] someRows7 = { 0, 1, 2, 3, 4, 5 };
        BigDecimal tmpFullValue = ACCURACY.enforce(TypeUtils
                .toBigDecimal(modFull.objective().toFunction().invoke(R064Store.FACTORY.copy(MatrixR064.FACTORY.column(tmpFullResult).rows(someRows7)))));

        TestUtils.assertEquals(0, tmpFullValue.compareTo(tmpEvenValue.add(tmpOddValue)));
        TestUtils.assertEquals(0, claimedValue.compareTo(tmpFullValue));
    }

    /**
     * https://github.com/vagmcs/Optimus/issues/43
     * <p>
     * The problem is unbounded (not infeasible). A feasible solution is x=120 and y=80.
     * <p>
     * In this case ojAlgo finds a feasible solution, but then somehow fails to identify that it's unbounded
     * and instead returns a solution with an infinite value.
     */
    @Test
    public void testOptimus43() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        if (DEBUG) {
            model.options.debug(LinearSolver.class);
        }

        Variable x = model.newVariable("x").weight(-2);
        Variable y = model.newVariable("y").lower(80).upper(170).weight(5);

        model.addExpression().set(x, 1).set(y, 1).lower(200);

        Result result = model.minimise();

        TestUtils.assertEquals(State.UNBOUNDED, result.getState());
    }

    /**
     * Didn't recognise this as an infeasible problem.
     */
    @Test
    public void testP20100412() {

        ExpressionsBasedModel tmpModel = OptimisationIntegerData.buildModelForP20100412();
        tmpModel.relax(false); // Relax the integer constraints
        tmpModel.getVariable(1).lower(ONE); // Set branch state

        State tmpResultState = tmpModel.maximise().getState();

        TestUtils.assertFalse("Should be INFEASIBLE", tmpResultState.isFeasible());
    }

    /**
     * Depending on how the constraints were constructed the solver could fail to solve and report the problem
     * to be unbounded.
     */
    @Test
    public void testP20111010() {

        ExpressionsBasedModel tmpModel = new ExpressionsBasedModel();

        Variable[] tmpVariables = { tmpModel.newVariable("X").lower(ZERO).weight(ONE), tmpModel.newVariable("Y").lower(ZERO).weight(ZERO),
                tmpModel.newVariable("Z").lower(ZERO).weight(ZERO) };

        Expression tmpExprC1 = tmpModel.newExpression("C1");
        tmpExprC1.level(ZERO);
        tmpExprC1.set(0, ONE);

        Expression tmpExprC2 = tmpModel.newExpression("C2");
        tmpExprC2.level(ZERO);
        tmpExprC2.set(0, ONE);
        tmpExprC2.set(1, NEG);

        Expression tmpExprC3 = tmpModel.newExpression("C3");
        tmpExprC3.level(ZERO);
        tmpExprC3.set(0, ONE);
        tmpExprC3.set(2, NEG);

        MatrixR064 tmpExpectedSolution = MatrixR064.FACTORY.make(3, 1);

        Optimisation.Result tmpResult11 = tmpModel.minimise();
        //TestUtils.assertEquals(tmpExpectedState, tmpResult11.getState());
        TestUtils.assertStateNotLessThanOptimal(tmpResult11);
        TestUtils.assertEquals(tmpExpectedSolution, MatrixQ128.FACTORY.column(tmpResult11));

        tmpExprC2.set(0, NEG);
        tmpExprC2.set(1, ONE);

        tmpExprC3.set(0, ONE);
        tmpExprC3.set(2, NEG);

        Optimisation.Result tmpResultN1 = tmpModel.minimise();
        //TestUtils.assertEquals(tmpExpectedState, tmpResultN1.getState());
        TestUtils.assertStateNotLessThanOptimal(tmpResultN1);
        TestUtils.assertEquals(tmpExpectedSolution, MatrixQ128.FACTORY.column(tmpResultN1));

        tmpExprC2.set(0, ONE);
        tmpExprC2.set(1, NEG);

        tmpExprC3.set(0, NEG);
        tmpExprC3.set(2, ONE);

        Optimisation.Result tmpResult1N = tmpModel.minimise();
        //TestUtils.assertEquals(tmpExpectedState, tmpResult1N.getState());
        TestUtils.assertStateNotLessThanOptimal(tmpResult1N);
        TestUtils.assertEquals(tmpExpectedSolution, MatrixQ128.FACTORY.column(tmpResult1N));

        tmpExprC2.set(0, NEG);
        tmpExprC2.set(1, ONE);

        tmpExprC3.set(0, NEG);
        tmpExprC3.set(2, ONE);

        Optimisation.Result tmpResultNN = tmpModel.minimise();
        //TestUtils.assertEquals(tmpExpectedState, tmpResultNN.getState());
        TestUtils.assertStateNotLessThanOptimal(tmpResultNN);
        TestUtils.assertEquals(tmpExpectedSolution, MatrixQ128.FACTORY.column(tmpResultNN));
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

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x = model.newVariable("x").lower(0);
        Variable y = model.newVariable("y").lower(0);

        model.newExpression("first").set(x, 2).set(y, 3).upper(1);
        model.newExpression("second").set(x, -2).set(y, 3).lower(1);

        ArrayR256 expected = ArrayR256.wrap(BigMath.ZERO, BigMath.THIRD);

        Optimisation.Result result = model.maximise();
        TestUtils.assertEquals(expected, result);
        TestUtils.assertStateNotLessThanOptimal(result);

    }

    /**
     * https://github.com/optimatika/ojAlgo/issues/62
     */
    @Test
    public void testP20180310_62() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x = model.newVariable("x").lower(0).weight(1);
        Variable y = model.newVariable("y").lower(0).weight(0);

        model.newExpression("first").set(x, 0).set(y, 1).lower(1);
        model.newExpression("second").set(x, 0).set(y, 1).upper(-1);

        TestUtils.assertEquals(Optimisation.State.INFEASIBLE, model.maximise().getState());

    }

    /**
     * https://github.com/optimatika/ojAlgo/issues/64
     */
    @Test
    public void testP20180311_64() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x = model.newVariable("x").lower(0).weight(3);
        Variable y = model.newVariable("y").lower(0).weight(-2);

        model.addExpression().set(x, -1).set(y, 0).lower(0);
        model.addExpression().set(x, -1).set(y, 3).level(2);

        ArrayR256 expected = ArrayR256.wrap(BigMath.ZERO, BigMath.TWO.multiply(BigMath.THIRD));

        Optimisation.Result result = model.maximise();
        TestUtils.assertEquals(expected, result);
        TestUtils.assertStateNotLessThanOptimal(result);

    }

    /**
     * https://github.com/optimatika/ojAlgo/issues/66
     */
    @Test
    public void testP20180311_66() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x = model.newVariable("x").lower(0).weight(2);
        Variable y = model.newVariable("y").lower(0).weight(-1);
        Variable z = model.newVariable("z").lower(0).weight(4);

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

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x = model.newVariable("x").lower(0).weight(3);
        Variable y = model.newVariable("y").lower(0).weight(2);
        Variable z = model.newVariable("z").lower(0).weight(-2);

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

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x = model.newVariable("x").lower(0).weight(-2);
        Variable y = model.newVariable("y").lower(0).weight(-2);

        model.addExpression().set(x, 3).set(y, 0).lower(2);
        model.addExpression().set(x, 1).set(y, 2).lower(-5);
        model.addExpression().set(x, 3).set(y, 1).upper(2);

        ArrayR256 expected = ArrayR256.wrap(DIVIDE.invoke(TWO, THREE), ZERO);
        TestUtils.assertTrue(model.validate(expected));

        Optimisation.Result solution = model.maximise();
        TestUtils.assertTrue(model.validate(solution));
        TestUtils.assertEquals(expected, solution);

        TestUtils.assertStateNotLessThanOptimal(solution);
    }

    /**
     * Found by fuzzing, while looking into https://github.com/optimatika/ojAlgo/issues/707
     * <p>
     * x2 enters with a bound flip over its range, 356331. The element of x3, and later x4, in x2's column is
     * too small to be a pivot, so it does not limit the step, but over that range it takes the variable out
     * of its bounds. The dual simplex brings it back, the primal simplex flips again, and so on. The sparse
     * store went round forever, and the dense store reported OPTIMAL with x4 = -1.1e-4. Whatever the state,
     * the solution must be within the bounds and satisfy the constraints. The optimum is 0.39491859...
     */
    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = ThreadMode.SEPARATE_THREAD)
    public void testPrimalBoundFlipCycle() {

        double[] c = { -1.419051247930093E-8, 0.0, 0.0015114711587145223, 197037.15723156036, 0.0 };
        double[][] a = { { 0.0, 3.67494795234008E-4, -0.003828243808149872, 0.0, 0.104682991481246 },
                { 0.003081735838449668, -0.19032054886173633, 0.0, -5.448601027569494E-7, 3.4400158486544325E-7 },
                { -0.023935414898928475, -6.348950784136911E-8, 5.15606884292762E-9, -8.099317167025764E8, 3.674234116022251E7 } };
        double[] b = { -1.0, 0.0, -1.0 };
        double[] lower = { Double.NEGATIVE_INFINITY, -8.707076491882005E7, 0.0, 0.0, 0.0 };
        double[] upper = { Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 356331.5203991972, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY };

        for (Boolean sparse : new Boolean[] { Boolean.FALSE, Boolean.TRUE }) {

            Optimisation.Options options = new Optimisation.Options();
            options.sparse = sparse;

            LinearSolver.Builder builder = LinearSolver.newGeneralBuilder(c);
            for (int i = 0; i < a.length; i++) {
                builder.inequality(b[i], a[i]);
            }
            builder.lower(lower);
            builder.upper(upper);

            Result result = builder.build(options).solve(null);

            TestUtils.assertTrue(result.getState().isFeasible());

            for (int j = 0; j < c.length; j++) {
                double x = result.doubleValue(j);
                TestUtils.assertTrue(x >= lower[j] - 1E-8);
                TestUtils.assertTrue(x <= upper[j] + 1E-8);
            }

            for (int i = 0; i < a.length; i++) {
                double activity = 0.0;
                double terms = 1.0;
                for (int j = 0; j < c.length; j++) {
                    activity += a[i][j] * result.doubleValue(j);
                    terms += Math.abs(a[i][j] * result.doubleValue(j));
                }
                TestUtils.assertTrue(activity - b[i] <= 1E-8 * terms);
            }

            if (result.getState().isOptimal()) {
                TestUtils.assertEquals(0.39491859279821495, result.getValue(), ACCURACY);
            }
        }
    }

}

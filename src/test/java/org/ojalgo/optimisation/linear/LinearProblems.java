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
     * Minimise c x subject to the rows (equalities or <=) and the bounds, with both stores. The result must
     * be UNBOUNDED, with a solution that satisfies the rows and the bounds (1E-9 relative).
     */
    private static void assertUnboundedAndFeasible(final double[] c, final double[][] rows, final double[] rhs, final boolean[] eq, final double[] lower,
            final double[] upper) {

        double tolerance = 1E-9;

        for (Boolean sparse : new Boolean[] { Boolean.FALSE, Boolean.TRUE }) {

            Optimisation.Options options = new Optimisation.Options();
            options.sparse = sparse;

            LinearSolver.Builder builder = LinearSolver.newBuilder(c);
            for (int i = 0; i < rows.length; i++) {
                if (eq[i]) {
                    builder.equality(rhs[i], rows[i]);
                } else {
                    builder.inequality(rhs[i], rows[i]);
                }
            }
            builder.lower(lower);
            builder.upper(upper);

            Result result = builder.build(options).solve(null);

            TestUtils.assertEquals(State.UNBOUNDED, result.getState());

            for (int j = 0; j < c.length; j++) {
                double x = result.doubleValue(j);
                double margin = tolerance * (1.0 + Math.abs(x));
                TestUtils.assertTrue("x" + j + " = " + x, lower[j] - margin <= x && x <= upper[j] + margin);
            }

            for (int i = 0; i < rows.length; i++) {
                double activity = 0.0;
                double magnitude = 1.0 + Math.abs(rhs[i]);
                for (int j = 0; j < c.length; j++) {
                    activity += rows[i][j] * result.doubleValue(j);
                    magnitude += Math.abs(rows[i][j] * result.doubleValue(j));
                }
                double violation = eq[i] ? Math.abs(activity - rhs[i]) : activity - rhs[i];
                TestUtils.assertTrue("row " + i + " = " + activity, violation <= tolerance * magnitude);
            }
        }
    }

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
     * The solution is x = 10, y = 0. When y enters, the element of the second constraint, 1E-7, is below the
     * absolute pivot tolerance. That row was skipped, and the step of 1E7 that the first row allowed took its
     * slack 1 past its bound. Then the dual value 1E312 overflowed, and the dense tableau was left with
     * reduced costs that were NaN, so no candidate could enter. That must end the solve as failed. First the
     * ratio test never returned, and without that the iterations went round without being counted. The
     * revised simplex recomputed the reduced costs and found the solution. Since issue 716 the second row
     * bounds the step, and both find the solution directly. {@link #testGitHub707UndefinedRatioBelowFloor()}
     * still takes that step.
     * <p>
     * https://github.com/optimatika/ojAlgo/issues/716
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

            TestUtils.assertEquals(State.OPTIMAL, result.getState());
            TestUtils.assertEquals(10.0, result.doubleValue(0), ACCURACY);
            TestUtils.assertEquals(0.0, result.doubleValue(1), ACCURACY);
        }
    }

    /**
     * https://github.com/optimatika/ojAlgo/issues/707
     * <p>
     * As {@link #testGitHub707UndefinedRatio()}, but the element of y in the second constraint is 1E-12,
     * below the smallest element that bounds the primal step. The step of 1E7 is taken, the dual value 1E312
     * overflows, and the dense tableau is left with reduced costs that are NaN, so no candidate can enter.
     * The revised simplex can not restore feasibility either. Both must end the solve as failed, and not go
     * round without end.
     */
    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = ThreadMode.SEPARATE_THREAD)
    public void testGitHub707UndefinedRatioBelowFloor() {

        for (Boolean sparse : new Boolean[] { Boolean.FALSE, Boolean.TRUE }) {

            Optimisation.Options options = new Optimisation.Options();
            options.sparse = sparse;

            LinearSolver.Builder builder = LinearSolver.newGeneralBuilder(new double[] { 0.0, -1E305 });
            builder.inequality(-1.0, new double[] { -0.1, -1E-7 });
            builder.inequality(0.0, new double[] { 0.0, 1E-12 });
            builder.lower(new double[] { 0.0, 0.0 });
            builder.upper(new double[] { Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY });

            Result result = builder.build(options).solve(null);

            TestUtils.assertEquals(State.FAILED, result.getState());
        }
    }

    /**
     * https://github.com/optimatika/ojAlgo/issues/713
     * <p>
     * Minimise x subject to x - k y = 0, y - k z = 0 and z >= 1000, with x, y and z free. The solution is z =
     * 1000, y = 1000 k and x = 1000 k^2. The last dual iteration has the artificial of the first row leaving
     * at 1000 k^2, and the only candidate that can enter is x, with element 1. The row also has the element
     * -k^2 of the slack, which is at its lower bound and can not enter. Judged relative to that element, x
     * was rejected as too small, and the result was INFEASIBLE for k >= 1E5.
     */
    @Test
    public void testGitHub713() {

        for (Boolean sparse : new Boolean[] { Boolean.FALSE, Boolean.TRUE }) {
            for (double k : new double[] { 1E4, 1E5, 1E6, 1E7 }) {

                Optimisation.Options options = new Optimisation.Options();
                options.sparse = sparse;

                LinearSolver.Builder builder = LinearSolver.newBuilder(1.0, 0.0, 0.0);
                builder.equality(0.0, 1.0, -k, 0.0);
                builder.equality(0.0, 0.0, 1.0, -k);
                builder.inequality(-1000.0, 0.0, 0.0, -1.0);
                builder.lower(Double.NEGATIVE_INFINITY);
                builder.upper(Double.POSITIVE_INFINITY);

                Result result = builder.build(options).solve(null);

                TestUtils.assertEquals(State.OPTIMAL, result.getState());
                TestUtils.assertEquals(1000.0 * k * k, result.doubleValue(0), ACCURACY);
                TestUtils.assertEquals(1000.0 * k, result.doubleValue(1), ACCURACY);
                TestUtils.assertEquals(1000.0, result.doubleValue(2), ACCURACY);
                TestUtils.assertEquals(1000.0 * k * k, result.getValue(), ACCURACY);
            }
        }
    }

    /**
     * Found when fixing https://github.com/optimatika/ojAlgo/issues/713
     * <p>
     * An infeasible LP with small integer coefficients. On the dense tableau the leaving row ends up with
     * rounding residue, 2.8E-17, as the elements of the candidates that can enter, while the slack that can
     * not enter has the element 1/3. Those residues must count as zero, also when they are the largest
     * elements among the candidates. Pivoting on one gave values around 1E19 and OPTIMAL.
     */
    @Test
    public void testGitHub713RoundingResidue() {

        for (Boolean sparse : new Boolean[] { Boolean.FALSE, Boolean.TRUE }) {

            Optimisation.Options options = new Optimisation.Options();
            options.sparse = sparse;

            LinearSolver.Builder builder = LinearSolver.newBuilder(0.0, 7.0, 8.0, 1.0, 0.0, 3.0);
            builder.inequality(-1000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 3.0);
            builder.inequality(0.0, 8.0, 0.0, 0.0, 9.0, -2.0, -2.0);
            builder.equality(1000.0, 7.0, -1.0, 9.0, 7.0, -8.0, 2.0);
            builder.lower(0.0);
            builder.upper(9.0, 2.0, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);

            Result result = builder.build(options).solve(null);

            TestUtils.assertEquals(State.INFEASIBLE, result.getState());
        }
    }

    /**
     * https://github.com/optimatika/ojAlgo/issues/715
     * <p>
     * Minimise x subject to a x <= 0, with x free and a < 0. The solution is x = 0. When x enters, the only
     * row that bounds it has the element a. For |a| below the absolute pivot tolerance, 5E-7, that row was
     * rejected, and the result was UNBOUNDED.
     */
    @Test
    public void testGitHub715() {

        double[] coefficients = { -1.0, -Math.scalb(1.0, -20), -Math.scalb(1.0, -21), -1E-8, -1E-10 };

        for (Boolean sparse : new Boolean[] { Boolean.FALSE, Boolean.TRUE }) {
            for (double a : coefficients) {

                Optimisation.Options options = new Optimisation.Options();
                options.sparse = sparse;

                LinearSolver.Builder builder = LinearSolver.newBuilder(1.0);
                builder.inequality(0.0, a);
                builder.lower(Double.NEGATIVE_INFINITY);
                builder.upper(Double.POSITIVE_INFINITY);

                Result result = builder.build(options).solve(null);

                TestUtils.assertEquals(State.OPTIMAL, result.getState());
                TestUtils.assertEquals(0.0, result.doubleValue(0), ACCURACY);
                TestUtils.assertEquals(0.0, result.getValue(), ACCURACY);
            }
        }
    }

    /**
     * https://github.com/optimatika/ojAlgo/issues/715
     * <p>
     * The same through {@link ExpressionsBasedModel}: minimise x subject to -a x + y <= 0, y + w >= 0 and y -
     * w >= 0, with x, y and w free. The solution is x = y = w = 0. Presolve can not bound x, and after the
     * row scaling x's element in the first row was still below the absolute pivot tolerance for a <= 1E-11.
     * The result was UNBOUNDED.
     */
    @Test
    public void testGitHub715Model() {

        for (Boolean sparse : new Boolean[] { Boolean.FALSE, Boolean.TRUE }) {
            for (double a : new double[] { 1E-6, 1E-9, 1E-11, 1E-12 }) {

                ExpressionsBasedModel model = new ExpressionsBasedModel();
                Variable x = model.newVariable("x").weight(1.0);
                Variable y = model.newVariable("y");
                Variable w = model.newVariable("w");
                model.newExpression("r1").set(x, -a).set(y, 1.0).upper(0.0);
                model.newExpression("r2").set(y, 1.0).set(w, 1.0).lower(0.0);
                model.newExpression("r3").set(y, 1.0).set(w, -1.0).lower(0.0);
                model.options.sparse = sparse;

                Result result = model.minimise();

                TestUtils.assertEquals(State.OPTIMAL, result.getState());
                TestUtils.assertEquals(0.0, result.doubleValue(0), ACCURACY);
                TestUtils.assertEquals(0.0, result.getValue(), ACCURACY);
                TestUtils.assertTrue(model.validate(result));
            }
        }
    }

    /**
     * Found when fixing https://github.com/optimatika/ojAlgo/issues/715
     * <p>
     * Three unbounded LPs where the primal ratio test, having rejected the rows that bound the step for their
     * small elements, could still pivot on one of them:
     * <ol>
     * <li>The basic variable of that row is already 6E-14 below its bound. Pivoting on the element, 1.6E-10,
     * moved the entering variable back by 4E-4, past its own bound. The result was INFEASIBLE.
     * <li>The element is 6.9E-9 and the step 9E15. A basic variable whose element was too small to count went
     * from 0.013 to 1386, past its upper bound 7.8.
     * <li>The element is 2.4E-14 and the step zero, but on the dense tableau the rounding error in the row's
     * right hand side, divided by the element, moved the entering variable by 1E6.
     * </ol>
     */
    @Test
    public void testGitHub715SmallPivotSteps() {

        double inf = Double.POSITIVE_INFINITY;

        LinearProblems.assertUnboundedAndFeasible(new double[] { -590.6046954963886, -2.506369230745158E-10, -0.001331642229264097 },
                new double[][] { { 9.57551596915589E-9, 22029.767467885635, 0.0 }, { 1.506984893827462E8, -0.010579311844792747, -2453702.9919798137 },
                        { 0.0, 1.53725979802288E-4, 0.0 } },
                new double[] { 0.0, 1000.0, 0.0 }, new boolean[] { false, false, false }, new double[] { -0.020548941513589505, 0.0, 0.0 },
                new double[] { inf, inf, inf });

        LinearProblems.assertUnboundedAndFeasible(new double[] { 0.0, 945.3794988992867, 0.0, 0.0, 0.0 },
                new double[][] { { -75577.46238154735, 0.0, 0.0, -1.6921265741588134, 0.0 },
                        { 65.14532793746437, -1636.2589965027373, 536387.9983587967, -14.20665282128471, -1.0848109998174924 }, { 0.0, 0.0, 0.0, 0.0, 0.0 },
                        { 0.0, 40.196894556014435, -120964.39771485061, -3885544.5345059116, 0.0 } },
                new double[] { -1000.0, 0.0, 0.0, -5316.914876937166 }, new boolean[] { true, false, true, false },
                new double[] { 0.0, -inf, -1.2659774301013087, -6.190842291646521E7, 0.0 },
                new double[] { 7.77845743380529, inf, 3828.514017948034, inf, inf });

        LinearProblems.assertUnboundedAndFeasible(
                new double[] { -0.029941390264074, -0.05054691590901175, -9.750827299327114E-6, 0.0, -0.6510216858322819, 0.0 },
                new double[][] { { -0.024672919720106292, 2.121499674611514E-5, 8.905696085196319E8, 0.0, 0.0, 23909.425241297242 },
                        { 0.0, 0.011544250761553444, -0.010496518837671076, 6.44953019197965E7, 7.693951957699648E-10, 0.0041469698564149775 } },
                new double[] { 0.0, -1000.0 }, new boolean[] { true, true }, new double[] { -inf, -inf, 0.0, -inf, -0.015390590534648002, 0.0 },
                new double[] { inf, inf, 2.135895376128294E8, inf, 0.011970081060978802, inf });
    }

    /**
     * https://github.com/optimatika/ojAlgo/issues/716
     * <p>
     * The reported LP (17 variables, 379 rows) reduced to 4 variables and 8 rows, one of them an empty
     * equality. The exact optimum is -21404.075890816253. The slack of the sixth row enters with reduced cost
     * -2.5E-6, and the largest element of its column is 0.037. The basic slack of the fourth row has the
     * element 1.9E-7, below the absolute pivot tolerance, so it did not bound the step. The other rows
     * allowed a step of 6.3E6, that took that slack 0.29 past its bound. The dual iterations restored
     * feasibility at the same basis, and after repeating this the result was FEASIBLE -21392.17.
     */
    @Test
    public void testGitHub716() {

        double inf = Double.POSITIVE_INFINITY;

        double[] c = { 40.1928439158, -36.3011175881, -0.0144356764049, -0.484592387013 };
        double[][] rows = { { -4.31593441055, 0.0, 0.25, 0.0 }, { -0.128289921592, 0.0, 8.0, 0.0 }, { -5.52188191383E-8, 0.0, 1.6777216E7, 0.0 },
                { -1.47942277024, 0.5, 0.0, 0.0 }, { -0.761108495821, 4.0, 0.0, 0.0 }, { -2.62049497286E7, 0.0, 0.0, 1048576.0 },
                { -3.98448355339, 0.0, 0.0, 0.125 }, { 0.0, 0.0, 0.0, 0.0 } };
        double[] rhs = { 4.72051852201, 240.780631639, 5.05403734024E8, 13.7481516961, 119.523897196, 4.40110088915E10, 5245.20560741, 0.0 };
        double[] lower = { 0.0, 0.0, 0.0, -inf };
        double[] upper = { 8.0, inf, inf, 1759703.656406453 };

        for (Boolean sparse : new Boolean[] { Boolean.FALSE, Boolean.TRUE }) {

            Optimisation.Options options = new Optimisation.Options();
            options.sparse = sparse;

            LinearSolver.Builder builder = LinearSolver.newBuilder(c);
            for (int i = 0; i < rows.length - 1; i++) {
                builder.inequality(rhs[i], rows[i]);
            }
            builder.equality(rhs[rows.length - 1], rows[rows.length - 1]);
            builder.lower(lower);
            builder.upper(upper);

            Result result = builder.build(options).solve(null);

            TestUtils.assertEquals(State.OPTIMAL, result.getState());
            TestUtils.assertEquals(-21404.075890816253, result.getValue(), ACCURACY);
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
     * Found by fuzzing with coefficients and bounds up to 1e308, while looking into
     * https://github.com/optimatika/ojAlgo/issues/707
     * <p>
     * Products of such numbers overflow, and the revised simplex was left with infinite reduced costs or
     * basic values that are not a number. In the first problem the primal iterations alternated between two
     * bases, in the second the dual iterations went round in a cycle. The iterations were counted, but with
     * the default iteration limit, Integer.MAX_VALUE, a solve would have taken about 15 minutes. Now the
     * repeated states are detected, and the solve ends with state FAILED after about 100 iterations.
     */
    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = ThreadMode.SEPARATE_THREAD)
    public void testOverflowCycle() {

        Optimisation.Options options = new Optimisation.Options();
        options.sparse = Boolean.TRUE;

        LinearSolver.Builder primalCycle = LinearSolver.newGeneralBuilder(new double[] { -7.486683749795589E-4, 1.3689496021833852E301 });
        primalCycle.equality(0.0, new double[] { -3.0901651107204555E-7, 9.975098623443677E-4 });
        primalCycle.inequality(0.11054399030447953, new double[] { 9.726565894471063E302, 7.094893249080399E-4 });
        primalCycle.inequality(1.0, new double[] { -4.086347150690167E306, -0.01045559678029427 });
        primalCycle.inequality(1.0, new double[] { 4.466217439932457E-7, 6.428961651723008E305 });
        primalCycle.lower(new double[] { 0.0, Double.NEGATIVE_INFINITY });
        primalCycle.upper(new double[] { 0.242077359309372, Double.POSITIVE_INFINITY });

        LinearSolver.Builder dualCycle = LinearSolver.newGeneralBuilder(new double[] { -4.436959560554985E306, 2.2663657742544865E307 });
        dualCycle.inequality(0.0, new double[] { 2.182383910475976E305, -1.6818761208188305E307 });
        dualCycle.inequality(1.0, new double[] { 0.0, 4.7230489434892127E-8 });
        dualCycle.inequality(1.1508376262086011E300, new double[] { 3.5057930019571866E-10, 0.14011422957719283 });
        dualCycle.lower(new double[] { 0.0, 0.0 });
        dualCycle.upper(new double[] { Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY });

        for (LinearSolver.Builder builder : new LinearSolver.Builder[] { primalCycle, dualCycle }) {

            Result result = builder.build(options).solve(null);

            if (DEBUG) {
                BasicLogger.debug(result);
            }

            TestUtils.assertEquals(State.FAILED, result.getState());
        }
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

    /**
     * A small, badly scaled LP from randomised tests. The optimum is 5.090314612529642 (exact, from rational
     * arithmetic), at x = (10000, 565591.18, -2), but the dual simplex reported UNBOUNDED.
     * <p>
     * In the primal iterations that follow the dual ones, every element of the entering column was small by
     * PIVOT. Such an element bounds the step only if the step the other rows allow would take its basic
     * variable past its bound, and that was only considered when the other rows bound the step. Here none of
     * them did, and an unbounded step was assumed.
     */
    @Test
    public void testSmallColumnElementsBoundStep() {

        for (Boolean sparse : new Boolean[] { null, Boolean.FALSE, Boolean.TRUE }) {

            ExpressionsBasedModel model = new ExpressionsBasedModel();
            model.options.sparse = sparse;

            Variable x0 = model.newVariable("x0").lower(-0.008).upper(10000.0);
            Variable x1 = model.newVariable("x1").lower(100.0).weight(9.0E-6);
            Variable x2 = model.newVariable("x2").lower(-2.0).upper(2000.0).weight(3.0E-6);

            model.newExpression("r0").upper(-703013114.2936316).set(x1, -7000000.0).set(x2, -0.006);
            model.newExpression("r1").lower(-11.269823583399205).set(x0, 3.0E-6).set(x1, -1.9999999999999998E-5).set(x2, -0.006);

            Result result = model.maximise();

            if (DEBUG) {
                BasicLogger.debug("sparse={}: {}", sparse, result);
            }

            TestUtils.assertEquals(State.OPTIMAL, result.getState());
            TestUtils.assertEquals(5.090314612529642, result.getValue(), NumberContext.of(8));
            TestUtils.assertTrue(model.validate(result));
        }
    }

}

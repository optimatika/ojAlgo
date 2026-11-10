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
package org.ojalgo.optimisation.integer;

import static org.ojalgo.function.constant.BigMath.*;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.Timeout.ThreadMode;
import org.ojalgo.TestUtils;
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
     * <pre>{@code
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
     * }</pre>
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
     * https://github.com/optimatika/ojAlgo/issues/708
     * <p>
     * The branch-and-bound node bounds are int values. Variable bounds outside the int range were truncated
     * to their low 32 bits: x in [-1e10, 1e10] was solved with x >= -1410065408, and x in [3e9, 4e9] became
     * infeasible. It depended on whether presolve settled the variable before branch-and-bound. Such bounds
     * are now left to the variable, the node has no bound.
     */
    @Test
    public void testGitHub708BoundForms() {

        NumberContext accuracy = NumberContext.of(12);

        double[][] boxes = { { -1E10, 1E10 }, { 3E9, 4E9 } };

        for (double[] box : boxes) {
            for (boolean viaExpression : new boolean[] { false, true }) {
                for (boolean withRow : new boolean[] { false, true }) {

                    ExpressionsBasedModel model = new ExpressionsBasedModel();

                    Variable x = model.newVariable("x").integer(true).lower(box[0]).upper(box[1]);
                    if (viaExpression) {
                        model.newExpression("objective").weight(ONE).set(x, ONE);
                    } else {
                        x.weight(ONE);
                    }
                    if (withRow) {
                        model.newExpression("row").upper(box[1]).set(x, ONE);
                    }

                    Result result = model.minimise();

                    if (DEBUG) {
                        BasicLogger.debug("[{}, {}] expression={} row={}: {}", box[0], box[1], viaExpression, withRow, result);
                    }

                    TestUtils.assertEquals(State.OPTIMAL, result.getState());
                    TestUtils.assertEquals(box[0], result.getValue(), accuracy);
                }
            }
        }
    }

    /**
     * https://github.com/optimatika/ojAlgo/issues/708
     * <p>
     * max x + y, x + y <= 5e9, integer x in [0, 6e9], y in [0, 1]. The optimum is 5e9. The upper bound of x,
     * 5e9 after presolve, was truncated to 705032704 in the node bounds, and the reported optimum was
     * 705032705.
     */
    @Test
    public void testGitHub708IntegerBounds() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x = model.newVariable("x").lower(ZERO).upper(6E9).integer(true).weight(ONE);
        Variable y = model.newVariable("y").lower(ZERO).upper(ONE).weight(ONE);
        model.newExpression("row").upper(5E9).set(x, ONE).set(y, ONE);

        Result result = model.maximise();

        if (DEBUG) {
            BasicLogger.debug(result);
        }

        TestUtils.assertEquals(State.OPTIMAL, result.getState());
        TestUtils.assertEquals(5E9, result.getValue(), NumberContext.of(12));
        TestUtils.assertTrue(model.validate(result));
    }

    /**
     * https://github.com/optimatika/ojAlgo/issues/708
     * <p>
     * min z, x - y - z = 0, b <= x + y <= b + 4, integer x and y without bounds, continuous z. With z in
     * [0.2, 0.4] there is no integer solution, with z in [0.2, 1.4] the optimum is z = 1. The LP solution has
     * x - y = 0.2, so x or y is fractional, near b / 2. When that is outside the int range there can be no
     * node bounds for the branches, and the solver stops with FAILED. Previously it branched at the int
     * limit, then created children identical to their parent, and ended with a StackOverflowError.
     */
    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = ThreadMode.SEPARATE_THREAD)
    public void testGitHub708NotBranchable() {

        for (double base : new double[] { 2E9, 6E9, -6E9 }) {
            for (double zUpper : new double[] { 0.4, 1.4 }) {

                ExpressionsBasedModel model = new ExpressionsBasedModel();

                Variable x = model.newVariable("x").integer(true);
                Variable y = model.newVariable("y").integer(true);
                Variable z = model.newVariable("z").lower(0.2).upper(zUpper).weight(ONE);
                model.newExpression("difference").level(ZERO).set(x, ONE).set(y, NEG).set(z, NEG);
                model.newExpression("sum").lower(base).upper(base + 4.0).set(x, ONE).set(y, ONE);

                Result result = model.minimise();

                if (DEBUG) {
                    BasicLogger.debug("b={} z<={}: {}", base, zUpper, result);
                }

                if (Math.abs(base) >= Integer.MAX_VALUE) {
                    TestUtils.assertEquals(State.FAILED, result.getState());
                } else if (zUpper < 1.0) {
                    TestUtils.assertEquals(State.INFEASIBLE, result.getState());
                } else {
                    TestUtils.assertEquals(State.OPTIMAL, result.getState());
                    TestUtils.assertEquals(1.0, result.getValue(), NumberContext.of(8));
                }
            }
        }
    }

    /**
     * https://github.com/optimatika/ojAlgo/issues/708
     * <p>
     * Integer x without bounds, integer y >= 0. min y subject to 2x - y = r has the optimum y = 1, and min y
     * subject to r <= 2x - y, 2x <= r + 3 has the optimum y = 0. Strong branching at the root used the int
     * range limits, the node representation of "no bound", as actual bounds on x, and a bound derived from an
     * infeasible probe was converted from long to int and wrapped. With |r| > 2^32 three of the four results
     * were wrong: INFEASIBLE twice, and OPTIMAL at y = 8589934593.
     */
    @Test
    public void testGitHub708UnboundedVariables() {

        for (double rhs : new double[] { 2E9 + 1.0, -2E9 - 1.0, 6E9 + 1.0, -6E9 - 1.0 }) {
            for (boolean equality : new boolean[] { false, true }) {

                ExpressionsBasedModel model = new ExpressionsBasedModel();

                Variable x = model.newVariable("x").integer(true);
                Variable y = model.newVariable("y").integer(true).lower(ZERO).weight(ONE);
                if (equality) {
                    model.newExpression("row").level(rhs).set(x, TWO).set(y, NEG);
                } else {
                    model.newExpression("row").lower(rhs).set(x, TWO).set(y, NEG);
                    model.newExpression("cap").upper(rhs + 3.0).set(x, TWO);
                }

                Result result = model.minimise();

                if (DEBUG) {
                    BasicLogger.debug("r={} equality={}: {}", rhs, equality, result);
                }

                TestUtils.assertEquals(State.OPTIMAL, result.getState());
                TestUtils.assertEquals(equality ? 1.0 : 0.0, result.getValue(), NumberContext.of(12));
                TestUtils.assertTrue(model.validate(result));
            }
        }
    }

    /**
     * https://github.com/optimatika/ojAlgo/issues/710
     * <p>
     * x0 - x1 = r with x0 in [0, 120] and x1 >= 0, x1 - x3 = 0 with x3 integer in [0, 12]. With r within 1e-9
     * of 120 presolve fixed x1 at 5e-12 (the middle of [0, 120 - r], zero at the feasibility precision), and
     * then the integer x3 at the same 5e-12. Integer rounding of the bounds of x3 gave [1, 0], and the model
     * was reported INFEASIBLE. The optimum is 0, at x3 = 0.
     */
    @Test
    public void testGitHub710() {

        for (double rhs : new double[] { 120.0, 119.99999999, 119.999999999, 119.99999999999 }) {

            ExpressionsBasedModel model = new ExpressionsBasedModel();

            Variable x0 = model.newVariable("x0").lower(ZERO).upper(120.0);
            Variable x1 = model.newVariable("x1").lower(ZERO);
            Variable x2 = model.newVariable("x2");
            Variable x3 = model.newVariable("x3").lower(ZERO).upper(12.0).integer(true);
            model.newVariable("x4").lower(ZERO).upper(ONE).integer(true).weight(NEG);

            model.newExpression("r0").level(rhs).set(x0, ONE).set(x1, NEG);
            model.newExpression("r1").level(248.0).set(x2, ONE);
            model.newExpression("r2").level(ZERO).set(x1, ONE).set(x3, NEG);

            Result result = model.maximise();

            if (DEBUG) {
                BasicLogger.debug("r={}: {}", rhs, result);
            }

            TestUtils.assertEquals(State.OPTIMAL, result.getState());
            TestUtils.assertEquals(0.0, result.getValue(), NumberContext.of(12));
            TestUtils.assertEquals(0.0, result.doubleValue(3), NumberContext.of(12));
            TestUtils.assertTrue(model.validate(result));
        }
    }

    /**
     * https://github.com/optimatika/ojAlgo/issues/710
     * <p>
     * N <= x <= N + 1 with integer x and N = 123456789012345. The bounds N and N + 1 are equal at the
     * feasibility precision, 12 significant digits, and presolve fixed x at N + 0.5. With another integer
     * variable y, rounding the bounds of x gave [N + 1, N], and the model was reported INFEASIBLE. The
     * optimum is x = N + 1 when maximising, x = N when minimising.
     */
    @Test
    public void testGitHub710LargeIntegerRange() {

        NumberContext exact = NumberContext.of(16);

        double level = 123456789012345.0;

        for (boolean other : new boolean[] { false, true }) {
            for (boolean maximise : new boolean[] { false, true }) {

                ExpressionsBasedModel model = new ExpressionsBasedModel();

                Variable x = model.newVariable("x").integer(true).lower(ZERO).weight(ONE);
                model.newExpression("range").lower(level).upper(level + 1.0).set(x, ONE);
                if (other) {
                    Variable y = model.newVariable("y").integer(true).lower(ZERO).upper(3.0).weight(ONE);
                    model.newExpression("cap").upper(2.5).set(y, ONE);
                }

                Result result = maximise ? model.maximise() : model.minimise();

                if (DEBUG) {
                    BasicLogger.debug("other={} maximise={}: {}", other, maximise, result);
                }

                TestUtils.assertEquals(State.OPTIMAL, result.getState());
                TestUtils.assertEquals(maximise ? level + 1.0 : level, result.doubleValue(0), exact);
                TestUtils.assertTrue(model.validate(result));
            }
        }
    }

    /**
     * https://github.com/optimatika/ojAlgo/issues/710
     * <p>
     * Presolve rounds integer values: an integer variable fixed by an equality constraint, and derived bounds
     * of integer variables. That rounding was done at the feasibility precision, 12 significant digits, and
     * integers with more digits were moved: x = 123456789012345 was fixed at 123456789012000, max x subject
     * to x <= 123456789012600 gave 123456789013000, and min x subject to x >= 123456789012345 gave
     * 123456789012000. The results were OPTIMAL and passed validation, since they are equal at 12 digits.
     */
    @Test
    public void testGitHub710LargeIntegers() {

        NumberContext exact = NumberContext.of(16);

        for (double level : new double[] { 123456789012345.0, 123456789012600.0, -123456789012345.0 }) {

            ExpressionsBasedModel fixed = new ExpressionsBasedModel();
            Variable x = fixed.newVariable("x").integer(true).weight(ONE);
            fixed.newExpression("fixed").level(level).set(x, ONE);

            ExpressionsBasedModel capped = new ExpressionsBasedModel();
            Variable u = capped.newVariable("u").integer(true).weight(ONE);
            capped.newExpression("capped").upper(level).set(u, ONE);

            ExpressionsBasedModel floored = new ExpressionsBasedModel();
            Variable w = floored.newVariable("w").integer(true).weight(ONE);
            floored.newExpression("floored").lower(level).set(w, ONE);

            Result resultFixed = fixed.minimise();
            Result resultCapped = capped.maximise();
            Result resultFloored = floored.minimise();

            if (DEBUG) {
                BasicLogger.debug("{}: fixed {}, capped {}, floored {}", level, resultFixed, resultCapped, resultFloored);
            }

            TestUtils.assertTrue(resultFixed.getState().isOptimal());
            TestUtils.assertEquals(level, resultFixed.doubleValue(0), exact);

            TestUtils.assertEquals(State.OPTIMAL, resultCapped.getState());
            TestUtils.assertEquals(level, resultCapped.doubleValue(0), exact);

            TestUtils.assertEquals(State.OPTIMAL, resultFloored.getState());
            TestUtils.assertEquals(level, resultFloored.doubleValue(0), exact);
        }
    }

    /**
     * https://github.com/optimatika/ojAlgo/issues/711
     * <p>
     * max 1e200 x subject to x - 1e200 y <= 1e200, x >= 0, y integer in [0, 3] (and min -1e200 x). The
     * optimum is at x = 4e200, y = 3, where the objective value, 4e400, overflows a double. Limiting the
     * objective by that incumbent threw NumberFormatException. The result should be the same as for the LP
     * relaxation: OPTIMAL with an infinite value.
     */
    @Test
    public void testGitHub711() {

        for (Optimisation.Sense sense : new Optimisation.Sense[] { Optimisation.Sense.MAX, Optimisation.Sense.MIN }) {

            ExpressionsBasedModel model = new ExpressionsBasedModel();

            Variable x = model.newVariable("x").lower(ZERO).weight(sense == Optimisation.Sense.MAX ? 1e200 : -1e200);
            Variable y = model.newVariable("y").lower(ZERO).upper(3.0).integer(true);

            model.newExpression("row").upper(1e200).set(x, ONE).set(y, -1e200);

            Result result = sense == Optimisation.Sense.MAX ? model.maximise() : model.minimise();

            if (DEBUG) {
                BasicLogger.debug("{}: {}", sense, result);
            }

            TestUtils.assertEquals(State.OPTIMAL, result.getState());
            TestUtils.assertEquals(sense == Optimisation.Sense.MAX ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY, result.getValue());
            TestUtils.assertEquals(4e200, result.doubleValue(0), NumberContext.of(12));
            TestUtils.assertEquals(3.0, result.doubleValue(1), NumberContext.of(12));
            TestUtils.assertTrue(model.validate(result));
        }
    }

    /**
     * https://github.com/optimatika/ojAlgo/issues/711
     * <p>
     * Integer values beyond the range of long, about 9.2e18, failed the integrality check of validation. The
     * integer solver discarded such solutions: max x with integer x in [0, 1e19] was reported INFEASIBLE, and
     * so was the issue's model with x integer – max 1e200 x subject to x - 1e200 y <= 1e200, x >= 0, y in [0,
     * 3.5], with the optimum at x = 4.5e200, y = 3.5 (x = 4e200, y = 3 with y integer too).
     */
    @Test
    public void testGitHub711LargeIntegers() {

        NumberContext exact = NumberContext.of(16);

        for (boolean maximise : new boolean[] { false, true }) {

            ExpressionsBasedModel model = new ExpressionsBasedModel();

            model.newVariable("x").integer(true).lower(maximise ? ZERO : -1e19).upper(maximise ? 1e19 : ZERO).weight(ONE);

            Result result = maximise ? model.maximise() : model.minimise();

            if (DEBUG) {
                BasicLogger.debug("maximise={}: {}", maximise, result);
            }

            TestUtils.assertEquals(State.OPTIMAL, result.getState());
            TestUtils.assertEquals(maximise ? 1e19 : -1e19, result.doubleValue(0), exact);
            TestUtils.assertTrue(model.validate(result));
        }

        for (boolean integerY : new boolean[] { false, true }) {

            ExpressionsBasedModel model = new ExpressionsBasedModel();

            Variable x = model.newVariable("x").integer(true).lower(ZERO).weight(1e200);
            Variable y = model.newVariable("y").integer(integerY).lower(ZERO).upper(3.5);

            model.newExpression("row").upper(1e200).set(x, ONE).set(y, -1e200);

            Result result = model.maximise();

            if (DEBUG) {
                BasicLogger.debug("integerY={}: {}", integerY, result);
            }

            TestUtils.assertEquals(State.OPTIMAL, result.getState());
            TestUtils.assertEquals(Double.POSITIVE_INFINITY, result.getValue());
            TestUtils.assertEquals(integerY ? 4e200 : 4.5e200, result.doubleValue(0), NumberContext.of(12));
            TestUtils.assertEquals(integerY ? 3.0 : 3.5, result.doubleValue(1), NumberContext.of(12));
            TestUtils.assertTrue(model.validate(result));
        }
    }

    /**
     * https://github.com/optimatika/ojAlgo/issues/712
     * <p>
     * In the issue's model a node with the optimum in its subtree was pruned as infeasible. After a cut
     * round, node presolve fixed a continuous variable at the midpoint of a range within the feasibility
     * tolerance, [0, 2.8e-10], and an equality constraint carried that to another continuous variable: 60 +
     * 1.4e-10 instead of 60. A constraint with that variable fixed, and only integer variables left, had its
     * limits rounded exactly to multiples of the integer step, 0.625: -6.0000000000139 steps became [-6, -7],
     * and the node was INFEASIBLE.
     * <p>
     * Here 0.0625 y - 0.625 a - 1.25 b - 2.5 c = 0 with binary a, b, c and y = 60 + 1e-10 (fixed directly, or
     * by presolve as in the issue). The only solution is a = 0, b = c = 1, but the model was reported
     * INFEASIBLE.
     */
    @Test
    public void testGitHub712() {

        for (boolean presolved : new boolean[] { false, true }) {
            for (boolean maximise : new boolean[] { false, true }) {

                ExpressionsBasedModel model = new ExpressionsBasedModel();

                Variable a = model.newVariable("a").binary().weight(ONE);
                Variable b = model.newVariable("b").binary().weight(ONE);
                Variable c = model.newVariable("c").binary().weight(ONE);
                Variable y = model.newVariable("y").lower(ZERO).upper(100.0);

                if (presolved) {
                    Variable x = model.newVariable("x").lower(ZERO).upper(30.0);
                    model.newExpression("tiny").upper(5e-11).set(x, 0.36);
                    model.newExpression("link").level(3.75).set(y, 0.0625).set(x, -0.0625);
                } else {
                    y.level(60.0000000001);
                }

                model.newExpression("steps").level(ZERO).set(y, 0.0625).set(a, -0.625).set(b, -1.25).set(c, -2.5);

                Result result = maximise ? model.maximise() : model.minimise();

                if (DEBUG) {
                    BasicLogger.debug("presolved={} maximise={}: {}", presolved, maximise, result);
                }

                TestUtils.assertEquals(State.OPTIMAL, result.getState());
                TestUtils.assertEquals(2.0, result.getValue(), NumberContext.of(12));
                TestUtils.assertEquals(0.0, result.doubleValue(0), NumberContext.of(12));
                TestUtils.assertEquals(1.0, result.doubleValue(1), NumberContext.of(12));
                TestUtils.assertEquals(1.0, result.doubleValue(2), NumberContext.of(12));
                TestUtils.assertTrue(model.validate(result));
            }
        }
    }

    /**
     * https://github.com/optimatika/ojAlgo/issues/714
     * <p>
     * max x - y with x free or x >= 0, binary y, and optionally a redundant constraint y <= 10. x = t, y = 0
     * is feasible for every t, so the model is unbounded, but it was reported INFEASIBLE.
     */
    @Test
    public void testGitHub714() {

        for (boolean lowerOnX : new boolean[] { false, true }) {
            for (boolean withRow : new boolean[] { false, true }) {

                ExpressionsBasedModel model = new ExpressionsBasedModel();

                Variable x = model.newVariable("x").weight(ONE);
                if (lowerOnX) {
                    x.lower(ZERO);
                }
                Variable y = model.newVariable("y").binary().weight(NEG);
                if (withRow) {
                    model.newExpression("row").upper(TEN).set(y, ONE);
                }

                Result result = model.maximise();

                if (DEBUG) {
                    BasicLogger.debug("lowerOnX={} withRow={}: {}", lowerOnX, withRow, result);
                }

                TestUtils.assertEquals(State.UNBOUNDED, result.getState());
                TestUtils.assertTrue(model.validate(result));
            }
        }
    }

    /**
     * Small models from randomised tests, each built around a feasible point. The optima are exact, from a
     * branch and bound in rational arithmetic. Gomory mixed-integer cuts cut off feasible solutions: the
     * first model was reported INFEASIBLE, the third and fourth OPTIMAL at worse values, and the second threw
     * NumberFormatException.
     * <p>
     * With the dense tableau a non-basic variable is shifted to the bound it is at. For a variable with only
     * one finite bound, such as the integer x3 in [-5, inf) of the first model, the shift was not taken back
     * when the cut was converted to the model's variables, and neither was the sign of a variable at an upper
     * bound of 0. A non-basic free variable, as in the second model, gives no valid cut. With the revised
     * simplex store its infinite bound gave an infinite right-hand side.
     * <p>
     * The primal solver splits a variable that can be negative in two parts, x = x+ - x-, both non-negative.
     * With both parts non-basic, x = 0, the cut term c+ x+ + c- x- is not linear in x, and converting it to
     * (c+ - c-) x gave an invalid cut. The fifth model, with x0 in [-3, inf) and a free x1, was reported
     * INFEASIBLE.
     */
    @Test
    public void testGomoryMixedIntegerCuts() {

        double inf = Double.POSITIVE_INFINITY;

        // Per variable: lower, upper, weight and 1 if integer
        double[][][] variables = { { { 10, 17, 0, 1 }, { 8, 18, 2, 0 }, { 0, 4, 7, 0 }, { -5, inf, 0, 1 } },
                { { -inf, inf, 0, 0 }, { -9, inf, 0, 0 }, { 8, 12, 8, 1 }, { -inf, inf, 0, 1 } },
                { { 8, 18, 0, 1 }, { 0, 8, 0, 1 }, { -2, inf, -7, 1 }, { -3, inf, 7, 0 }, { 0, 4, 0, 0 } },
                { { -4, inf, -3, 1 }, { 2, inf, 0, 1 }, { 0, inf, 0, 0 }, { 0, 9, 0, 1 }, { -inf, inf, 8, 1 }, { -8, inf, 1, 0 } },
                { { -3, inf, 0, 1 }, { -inf, inf, 0, 1 }, { 8, 15, 1, 0 }, { 0, 3, -2, 1 }, { 1, inf, 7, 0 } } };
        // Per row: lower, upper and the coefficients
        double[][][] rows = {
                { { -inf, 0.5, -3, 6, -2, 0 }, { 173.5, 173.5, 9, 2, 1, 0 }, { 2, inf, 3, -8, 0, -5 }, { -inf, 114, 3, 8, -8, -1 },
                        { -217.75, inf, -9, -7, 0, 0 } },
                { { -31.25, -31.25, 8, 5, 2, -9 } }, { { -inf, -38.25, 0, -3, 4, 0, -7 }, { 31.75, inf, 3, -6, 0, 0, 9 }, { -inf, 45, 0, 8, 2, -5, 0 } },
                { { -inf, 47, 0, -2, 0, 4, 0, -5 }, { -inf, -18.75, 0, 5, 9, 0, 5, 4 }, { -inf, -16, 6, -1, 8, 0, -3, 4 }, { -inf, 35, 0, -7, 0, 8, 0, 0 },
                        { -inf, 66, -3, 4, 0, 0, -9, 0 }, { 77.5, inf, -8, 0, 2, 4, 0, -5 } },
                { { -inf, 3, 0, 0, 0, 5, 0 }, { -inf, 14, 6, -4, 0, 0, 0 }, { 16, 16, 4, -6, 0, 2, 0 }, { -inf, 9, 0, 0, 0, 1, 1 } } };
        boolean[] maximise = { true, false, false, false, false };
        double[] optimum = { 44.5, 64.0, -18.2, -39.0, 15.0 };

        for (int c = 0; c < optimum.length; c++) {
            for (boolean primal : new boolean[] { false, true }) {
                for (Boolean sparse : new Boolean[] { null, Boolean.FALSE, Boolean.TRUE }) {

                    ExpressionsBasedModel model = new ExpressionsBasedModel();
                    model.options.sparse = sparse;
                    if (primal) {
                        model.options.linear().primal();
                    }

                    Variable[] x = new Variable[variables[c].length];
                    for (int j = 0; j < x.length; j++) {
                        double[] v = variables[c][j];
                        x[j] = model.newVariable("x" + j);
                        if (v[2] != 0.0) {
                            x[j].weight(v[2]);
                        }
                        if (Double.isFinite(v[0])) {
                            x[j].lower(v[0]);
                        }
                        if (Double.isFinite(v[1])) {
                            x[j].upper(v[1]);
                        }
                        if (v[3] != 0.0) {
                            x[j].integer(true);
                        }
                    }

                    for (int i = 0; i < rows[c].length; i++) {
                        double[] r = rows[c][i];
                        Expression row = model.newExpression("r" + i);
                        for (int j = 0; j < x.length; j++) {
                            if (r[2 + j] != 0.0) {
                                row.set(x[j], r[2 + j]);
                            }
                        }
                        if (Double.isFinite(r[0])) {
                            row.lower(r[0]);
                        }
                        if (Double.isFinite(r[1])) {
                            row.upper(r[1]);
                        }
                    }

                    Result result = maximise[c] ? model.maximise() : model.minimise();

                    if (DEBUG) {
                        BasicLogger.debug("model {}, primal={}, sparse={}: {}", c, primal, sparse, result);
                    }

                    TestUtils.assertEquals(State.OPTIMAL, result.getState());
                    TestUtils.assertEquals(optimum[c], result.getValue(), NumberContext.of(8));
                    TestUtils.assertTrue(model.validate(result));
                }
            }
        }
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

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        model.newVariable("X").binary().weight(ONE);
        model.newVariable("Y").binary().weight(ONE);
        model.newVariable("Z").binary().weight(ONE);

        Expression tmpC1 = model.newExpression("C1");
        for (int i = 0; i < model.countVariables(); i++) {
            tmpC1.set(i, ONE);
        }

        tmpC1.level(ONE);

        Expression tmpC2 = model.newExpression("C2");
        for (int i = 0; i < model.countVariables(); i++) {
            tmpC2.set(i, ONE);
        }
        tmpC2.level(TWO);

        Expression tmpC3 = model.newExpression("C3");
        for (int i = 0; i < model.countVariables(); i++) {
            tmpC3.set(i, ONE);
        }
        tmpC3.level(THREE);

        // tmpModel.options.progress(IntegerSolver.class);

        Optimisation.Result tmpResult = model.minimise();

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

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable[] variables = { model.newVariable("x1").lower(ZERO).weight(ONE), model.newVariable("x2013").lower(ZERO).integer(),
                model.newVariable("x2014").lower(ZERO).integer() };

        Expression expr1 = model.newExpression("Expr1");
        expr1.set(0, -1);
        expr1.set(1, 5100);
        expr1.set(2, -5000);
        expr1.upper(ZERO);

        Expression expr2 = model.newExpression("Expr2");
        expr2.set(0, 1);
        expr2.set(1, 5100);
        expr2.set(2, -5000);
        expr2.lower(ZERO);

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

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x1 = model.newVariable("x1");
        Variable x2013 = model.newVariable("x2013").integer();
        Variable x2014 = model.newVariable("x2014").integer();
        Variable x2015 = model.newVariable("x2015").integer();

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
     * A small model from randomised tests, solved with a strategy whose only node priority is breadth first,
     * or depth first. The optimum is 485/3. Optimality within the gap tolerance is proven against the best
     * bound of the open nodes, taken from the view of the queue ordered by objective value. Without a worker
     * using that ordering there was no such view, and the search threw NullPointerException.
     */
    @Test
    public void testPriorityDefinitionsWithoutObjectiveOrder() {

        for (Comparator<NodeKey> priority : List.of(NodeKey.BREADTH_FIRST_SEARCH, NodeKey.DEPTH_FIRST_SEARCH)) {

            ExpressionsBasedModel model = new ExpressionsBasedModel();
            model.options.integer(IntegerStrategy.DEFAULT.withPriorityDefinitions(priority));

            Variable x0 = model.newVariable("x0").integer().lower(1).upper(7).weight(8);
            Variable x1 = model.newVariable("x1").integer().upper(2);
            Variable x2 = model.newVariable("x2").integer().lower(5).weight(9);
            Variable x3 = model.newVariable("x3").integer().lower(-3).upper(5);
            Variable x4 = model.newVariable("x4").upper(7).weight(-2);
            Variable x5 = model.newVariable("x5").weight(3);

            model.newExpression("r0").lower(79.5).set(x1, 9).set(x2, 9).set(x4, -4).set(x5, -9);
            model.newExpression("r1").upper(104).set(x1, -5).set(x2, 9).set(x3, 4);
            model.newExpression("r2").lower(-66.75).set(x1, 5).set(x2, -5).set(x4, 9).set(x5, -4);
            model.newExpression("r3").upper(-68.75).set(x0, 5).set(x1, 5).set(x2, -8).set(x4, 1).set(x5, 2);
            model.newExpression("r4").upper(62.75).set(x0, -1).set(x1, 8).set(x2, 8).set(x3, 5).set(x4, -9);
            model.newExpression("r5").upper(-20).set(x0, -6).set(x3, -3).set(x5, -4);

            Result result = model.maximise();

            if (DEBUG) {
                BasicLogger.debug("{}: {}", priority, result);
            }

            TestUtils.assertEquals(State.OPTIMAL, result.getState());
            TestUtils.assertEquals(485.0 / 3.0, result.getValue(), NumberContext.of(8));
            TestUtils.assertTrue(model.validate(result));
        }
    }

    /**
     * A small model from randomised tests, with the primal solver. The optimum is 6, at x = (-2, 3, 2, 5, 0),
     * but 9 was reported OPTIMAL.
     * <p>
     * Reduced cost fixing tightens one bound at a time and passes each change to the node solver. The primal
     * solver can not update a range in place, so it is rebuilt, and the reduced costs of the following
     * variables were read from the new, not yet solved, solver. At a node with x0 <= -2, x2 in [1, 2] and x3
     * in [4, 6], where the reduced cost of x2 is 0, that fixed x2 <= 1 and cut off the optimum. The first
     * part checks that node's relaxation directly: after the bound update the reduced costs must be 0 (not
     * solved) or what they were.
     */
    @Test
    public void testReducedCostFixing() {

        ExpressionsBasedModel node = new ExpressionsBasedModel();
        node.options.linear().primal();

        Variable[] y = new Variable[5];
        y[0] = node.newVariable("x0").weight(-3).upper(-2);
        y[1] = node.newVariable("x1").lower(3);
        y[2] = node.newVariable("x2").lower(1).upper(2);
        y[3] = node.newVariable("x3").lower(4).upper(6);
        y[4] = node.newVariable("x4").weight(4).lower(0).upper(6);
        node.newExpression("r0").level(-61).set(y[0], 9).set(y[2], -4).set(y[3], -7).set(y[4], -1);

        NodeSolver solver = node.prepare(Optimisation.Sense.MIN, NodeSolver::new);

        Result relaxed = solver.solve(null);
        TestUtils.assertEquals(State.OPTIMAL, relaxed.getState());
        TestUtils.assertEquals(6.0, relaxed.getValue(), 1E-9);

        double[] before = new double[y.length];
        for (int j = 0; j < y.length; j++) {
            before[j] = solver.getReducedGradient(j);
        }

        y[0].lower(-3);
        solver.update(y[0]);

        for (int j = 0; j < y.length; j++) {
            double after = solver.getReducedGradient(j);
            if (DEBUG) {
                BasicLogger.debug("x{}: {} -> {}", j, before[j], after);
            }
            TestUtils.assertTrue(after == 0.0 || after == before[j]);
        }

        ExpressionsBasedModel model = new ExpressionsBasedModel();
        model.options.linear().primal();

        Variable x0 = model.newVariable("x0").integer().weight(-3);
        model.newVariable("x1").lower(3);
        Variable x2 = model.newVariable("x2").integer().upper(2);
        Variable x3 = model.newVariable("x3").integer().lower(-9).upper(6);
        Variable x4 = model.newVariable("x4").integer().weight(4).lower(0).upper(6);
        model.newExpression("r0").level(-61).set(x0, 9).set(x2, -4).set(x3, -7).set(x4, -1);

        Result result = model.minimise();

        if (DEBUG) {
            BasicLogger.debug("MIP: {}", result);
        }

        TestUtils.assertEquals(State.OPTIMAL, result.getState());
        TestUtils.assertEquals(6.0, result.getValue(), NumberContext.of(8));
        TestUtils.assertTrue(model.validate(result));
    }

    /**
     * Reduced from a random model with coefficients over the whole double range. The root node solution has x
     * = 1.989e167, an integer, and y infinite: r1 needs y >= 1.989e372, which is not a double. The node
     * solution was rounded, to look for an integer solution, and converting y to BigDecimal threw
     * NumberFormatException. There is no solution in double values.
     */
    @Test
    public void testRoundingInfiniteValue() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x = model.newVariable("x").integer(true);
        Variable y = model.newVariable("y");

        model.newExpression("r0").level(2.5738882534603326E256).set(x, 1.2939807349841934E89);
        model.newExpression("r1").upper(1E202).set(x, 1E232).set(y, -1E27);

        Result result = model.minimise();

        if (DEBUG) {
            BasicLogger.debug(result);
        }

        TestUtils.assertStateLessThanFeasible(result);
    }

    /**
     * I am trying to call Ojalgo 40 from AnyLogic 7.3.2 (http://www.anylogic..com/downloads) on Ubuntu 16.04
     * in order to solve a Traveling Salesman Problem, but Ojalgo sometimes stops on a feasible solution
     * before the optimum. The following code works well without Anylogic and always finds 917.31 as optimal
     * solution: (Simply copy/paste the following code in a file called "Tsp.java" in order to test it.)
     * <p>
     * Next, I try to run the same code in the "On startup" section of the "Agent actions" of the "Main" agent
     * in an AnyLogic model. (Click on the project name "Ojalgo" to change the location of ojalgo-40.0.0.jar,
     * like in the attached file.) Unfortunately, the obtained result is not always 917.31, but also sometimes
     * 1099.22 and 1161.84. I do not understand why the solution randomly changes. I also call Cplex from
     * AnyLogic to solve this same problem, which always returns the optimal solution, hence the problem seems
     * not to be due to AnyLogic. As shown by the above Java code, the problem is not due to Ojalgo as well,
     * but only related to the coupling of AnyLogic and Ojalgo. Thank you very much for Ojalgo and your help!
     * <p>
     * apete: ExpressionsBasedModel has a feature that automatically rescales model parameters (to maximize
     * numerical accuracy) before invoking the solver. The current implementation of that feature (apparently)
     * doesn?t work very well with extremely large parameters in the model. I have now modified the behavior
     * of that feature to not scale anything when/if there are extremely large or small parameters present. As
     * far as I can see that solves the problem with your model.
     * <p>
     * apete (later): Have also improved the presolve functionality to fix (not-include) uncorrelated and/or
     * unbounded variables. (Doesn't handle every case, but this one a a few more.) This was the real fix for
     * this problem!
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
     * The model of {@link #testWorkersWaitForNodes()}, with a single worker. That worker orders the nodes by
     * objective value, and every node has the value 36. Ties were broken by displacement only: the x1 <= -k
     * branches (0.875) always came before the x4 <= 2k - 3 branches (0.75), which have integer relaxations,
     * and the search ended at the time limit with FEASIBLE 41. Ties are now broken by depth first.
     */
    @Test
    public void testSingleWorkerObjectiveTies() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();
        model.options.linear().primal();
        model.options.parallelism(1);
        model.options.time_abort = 5_000L;

        Variable x0 = model.newVariable("x0").integer().lower(9).weight(4);
        Variable x1 = model.newVariable("x1").integer().upper(1);
        Variable x2 = model.newVariable("x2").integer().lower(0).weight(5);
        model.newVariable("x3").integer().lower(6).upper(9);
        Variable x4 = model.newVariable("x4").integer().lower(4);
        model.newExpression("r0").lower(-72).set(x0, -9).set(x1, -8).set(x2, 5).set(x4, -4);

        Result result = model.minimise();

        if (DEBUG) {
            BasicLogger.debug("{}", result);
        }

        TestUtils.assertEquals(State.OPTIMAL, result.getState());
        TestUtils.assertEquals(36.0, result.getValue(), NumberContext.of(8));
        TestUtils.assertTrue(model.validate(result));
    }

    /**
     * 2 iterations simply isn't enough for anything to complete normally. The (originally) returned state
     * INFEASIBLE is misleading. Either it should be FEASIBLE/OPTIMAL as the returned solutions happens to be
     * (total coincident) or UNEXPLORED/FAILED to reflect what the solver managed to do.
     *
     * @see "https://github.com/optimatika/ojAlgo/issues/310"
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

    /**
     * The LP relaxations of these models are unbounded, and there are integer solutions, so the models are
     * unbounded. They were reported INFEASIBLE: the integer solver treated the unbounded root node as an
     * infeasible node. With rational data, a model with an unbounded LP relaxation is either infeasible or
     * unbounded (R.R. Meyer, 1974), and it is unbounded if there is an integer solution.
     * <ul>
     * <li>max x + y, |x - y| <= 1, x + y >= 0.5, integer x and y without bounds
     * <li>max x, integer x >= 0
     * <li>max x + y, x - 2y <= 0.5, x + y >= 0.3, integer x >= 0, y >= 0, where the LP solution, x = 0.5, is
     * not an integer solution
     * </ul>
     */
    @Test
    public void testUnboundedRelaxationFeasible() {

        ExpressionsBasedModel band = new ExpressionsBasedModel();
        Variable bandX = band.newVariable("x").integer(true).weight(ONE);
        Variable bandY = band.newVariable("y").integer(true).weight(ONE);
        band.newExpression("d1").upper(ONE).set(bandX, ONE).set(bandY, NEG);
        band.newExpression("d2").upper(ONE).set(bandY, ONE).set(bandX, NEG);
        band.newExpression("sum").lower(HALF).set(bandX, ONE).set(bandY, ONE);

        ExpressionsBasedModel single = new ExpressionsBasedModel();
        single.newVariable("x").integer(true).lower(ZERO).weight(ONE);

        ExpressionsBasedModel mixed = new ExpressionsBasedModel();
        Variable mixedX = mixed.newVariable("x").integer(true).lower(ZERO).weight(ONE);
        Variable mixedY = mixed.newVariable("y").lower(ZERO).weight(ONE);
        mixed.newExpression("r").upper(HALF).set(mixedX, ONE).set(mixedY, TWO.negate());
        mixed.newExpression("s").lower(0.3).set(mixedX, ONE).set(mixedY, ONE);

        for (ExpressionsBasedModel model : new ExpressionsBasedModel[] { band, single, mixed }) {

            Result relaxed = model.copy(true).maximise();
            Result result = model.maximise();

            if (DEBUG) {
                BasicLogger.debug("{} -> {}", relaxed, result);
            }

            TestUtils.assertEquals(State.UNBOUNDED, relaxed.getState());
            TestUtils.assertEquals(State.UNBOUNDED, result.getState());
            TestUtils.assertTrue(model.validate(result));
        }
    }

    /**
     * The LP relaxations of these models are unbounded, but there are no integer solutions, so the models are
     * infeasible.
     * <ul>
     * <li>max w, x + y = 1.5, w >= x, integer x and y in [0, 1], w >= 0
     * <li>max x + y, x - y = z, z in [0.2, 0.4], integer x and y without bounds
     * </ul>
     */
    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = ThreadMode.SEPARATE_THREAD)
    public void testUnboundedRelaxationInfeasible() {

        ExpressionsBasedModel parity = new ExpressionsBasedModel();
        Variable parityX = parity.newVariable("x").integer(true).lower(ZERO).upper(ONE);
        Variable parityY = parity.newVariable("y").integer(true).lower(ZERO).upper(ONE);
        Variable parityW = parity.newVariable("w").lower(ZERO).weight(ONE);
        parity.newExpression("half").level(1.5).set(parityX, ONE).set(parityY, ONE);
        parity.newExpression("link").lower(ZERO).set(parityW, ONE).set(parityX, NEG);

        ExpressionsBasedModel band = new ExpressionsBasedModel();
        Variable bandX = band.newVariable("x").integer(true).weight(ONE);
        Variable bandY = band.newVariable("y").integer(true).weight(ONE);
        Variable bandZ = band.newVariable("z").lower(0.2).upper(0.4);
        band.newExpression("difference").level(ZERO).set(bandX, ONE).set(bandY, NEG).set(bandZ, NEG);

        for (ExpressionsBasedModel model : new ExpressionsBasedModel[] { parity, band }) {

            Result relaxed = model.copy(true).maximise();
            Result result = model.maximise();

            if (DEBUG) {
                BasicLogger.debug("{} -> {}", relaxed, result);
            }

            TestUtils.assertEquals(State.UNBOUNDED, relaxed.getState());
            TestUtils.assertEquals(State.INFEASIBLE, result.getState());
        }
    }

    /**
     * A small model from randomised tests, with the primal solver. The optimum is 36, the LP bound, at e.g. x
     * = (9, -4, 0, 6, 4). x1 has no lower bound and x4 no upper bound, and neither is in the objective
     * function. Every node relaxation has the value 36, and branching alternates x1 <= -k and x4 >= 2k - 2
     * without end, while the other branches, x4 <= 2k - 3, have integer relaxations.
     * <p>
     * A worker that found the queue empty stopped, even while another worker was still processing a node. In
     * this narrow tree only one worker remained, and when that was the minimum objective or depth first
     * worker the integer branches were never processed – the time limit ended the search with FEASIBLE 41.
     * Which worker remains varies, so the model is solved a few times.
     */
    @Test
    public void testWorkersWaitForNodes() {

        for (int r = 0; r < 3; r++) {

            ExpressionsBasedModel model = new ExpressionsBasedModel();
            model.options.linear().primal();
            model.options.parallelism(3);
            model.options.time_abort = 5_000L;

            Variable x0 = model.newVariable("x0").integer().lower(9).weight(4);
            Variable x1 = model.newVariable("x1").integer().upper(1);
            Variable x2 = model.newVariable("x2").integer().lower(0).weight(5);
            model.newVariable("x3").integer().lower(6).upper(9);
            Variable x4 = model.newVariable("x4").integer().lower(4);
            model.newExpression("r0").lower(-72).set(x0, -9).set(x1, -8).set(x2, 5).set(x4, -4);

            Result result = model.minimise();

            if (DEBUG) {
                BasicLogger.debug("{}: {}", r, result);
            }

            TestUtils.assertEquals(State.OPTIMAL, result.getState());
            TestUtils.assertEquals(36.0, result.getValue(), NumberContext.of(8));
            TestUtils.assertTrue(model.validate(result));
        }
    }

}

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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.structure.Structure1D.IntIndex;

/**
 * Generated cuts don't cut off any integer solution, not even by a rounding error. The coefficients are
 * rounded, and which way is safe depends on the bounds of the variable: they used to be rounded up, also for
 * variables that can be negative. A model with a cut like that, and everything else fixed by presolve at the
 * optimal solution, was not valid, and the result was FAILED.
 */
public class CutRoundingTest extends OptimisationIntegerTests {

    private static List<int[]> enumerate(final int[] lower, final int[] upper, final int[][] factors, final int[] limits) {

        List<int[]> retVal = new ArrayList<>();

        int[] values = lower.clone();
        int position = 0;
        while (position < values.length) {
            boolean feasible = true;
            for (int i = 0; i < limits.length; i++) {
                int body = 0;
                for (int j = 0; j < values.length; j++) {
                    body += factors[i][j] * values[j];
                }
                feasible &= body <= limits[i];
            }
            if (feasible) {
                retVal.add(values.clone());
            }
            position = 0;
            while (position < values.length && ++values[position] > upper[position]) {
                values[position] = lower[position];
                position++;
            }
        }

        return retVal;
    }

    /**
     * All integer solutions of small random models, checked (exactly) with all cuts generated at the root.
     */
    @Test
    public void testNothingCutOff() {

        int nbCuts = 0;
        int nbChecked = 0;

        for (int seed = 1; seed <= 1000; seed++) {

            Random random = new Random(seed);

            int nbVariables = 3 + random.nextInt(2);
            int nbConstraints = 1 + random.nextInt(4);

            int[] lower = new int[nbVariables];
            int[] upper = new int[nbVariables];
            int[] weights = new int[nbVariables];
            int[][] factors = new int[nbConstraints][nbVariables];
            int[] limits = new int[nbConstraints];

            ExpressionsBasedModel model = new ExpressionsBasedModel();

            for (int j = 0; j < nbVariables; j++) {
                // Both, only negative, or only positive
                lower[j] = random.nextInt(9) - 6;
                upper[j] = lower[j] + 1 + random.nextInt(5);
                weights[j] = random.nextInt(21) - 10;
                model.newVariable("x" + j).integer().lower(lower[j]).upper(upper[j]).weight(weights[j]);
            }
            for (int i = 0; i < nbConstraints; i++) {
                limits[i] = random.nextInt(21) - 10;
                Expression row = model.newExpression("row" + i).upper(limits[i]);
                for (int j = 0; j < nbVariables; j++) {
                    factors[i][j] = random.nextInt(15) - 7;
                    row.set(j, factors[i][j]);
                }
            }

            ModelStrategy strategy = IntegerStrategy.DEFAULT.newModelStrategy(model);
            ExpressionsBasedModel relaxed = model.snapshot();
            NodeSolver solver = relaxed.prepare(Optimisation.Sense.MAX, NodeSolver::new);

            Set<String> names = new HashSet<>();
            for (Expression expression : relaxed.getExpressions()) {
                names.add(expression.getName());
            }

            if (!solver.solve().getState().isOptimal() || solver.generateCuts(strategy, new CutStatistics(Optimisation.Sense.MAX), true) == null) {
                continue;
            }

            List<int[]> solutions = CutRoundingTest.enumerate(lower, upper, factors, limits);

            for (Expression cut : relaxed.getExpressions()) {

                if (names.contains(cut.getName())) {
                    continue;
                }
                nbCuts++;

                for (int[] solution : solutions) {

                    BigDecimal body = BigDecimal.ZERO;
                    for (Entry<IntIndex, BigDecimal> entry : cut.getLinearEntrySet()) {
                        body = body.add(entry.getValue().multiply(BigDecimal.valueOf(solution[entry.getKey().index])));
                    }

                    String message = "Seed " + seed + ": " + cut + " is " + body;
                    if (cut.getLowerLimit() != null) {
                        TestUtils.assertTrue(message, body.compareTo(cut.getLowerLimit()) >= 0);
                    }
                    if (cut.getUpperLimit() != null) {
                        TestUtils.assertTrue(message, body.compareTo(cut.getUpperLimit()) <= 0);
                    }
                    nbChecked++;
                }
            }
        }

        TestUtils.assertTrue("Cuts " + nbCuts + ", checked " + nbChecked, nbCuts > 400 && nbChecked > 10_000);
    }

    /**
     * Presolve fixes all variables, with the cuts of the second round, at the optimal solution. The result
     * was FAILED (the second model only with the primal LP solver).
     */
    @Test
    public void testFixedByCuts() {

        for (boolean primal : new boolean[] { false, true }) {

            ExpressionsBasedModel model = new ExpressionsBasedModel();
            Variable x0 = model.newVariable("x0").integer().lower(-2).upper(1).weight(8);
            Variable x1 = model.newVariable("x1").integer().lower(-3).upper(-2).weight(10);
            Variable x2 = model.newVariable("x2").integer().lower(-4).upper(2).weight(8);
            Variable x3 = model.newVariable("x3").integer().lower(-2).upper(2).weight(-5);
            model.newExpression("r0").level(-4).set(x0, -6).set(x1, -1).set(x3, 2);
            model.newExpression("r1").lower(0).upper(6).set(x0, 7).set(x1, 4).set(x2, 4).set(x3, -4);
            model.newExpression("r2").upper(8).set(x0, -1).set(x1, -2).set(x2, -4).set(x3, -3);
            if (primal) {
                model.options.linear().primal();
            }

            Optimisation.Result result = model.maximise();

            TestUtils.assertStateNotLessThanOptimal(result);
            TestUtils.assertEquals(-4.0, result.getValue(), 1E-9);

            model = new ExpressionsBasedModel();
            x0 = model.newVariable("x0").integer().lower(-3).upper(3).weight(2);
            x1 = model.newVariable("x1").integer().lower(-3).upper(0).weight(-9);
            x2 = model.newVariable("x2").integer().lower(-3).upper(-2).weight(-3);
            x3 = model.newVariable("x3").integer().lower(-3).upper(2).weight(6);
            Variable x4 = model.newVariable("x4").integer().lower(-3).upper(1).weight(-7);
            model.newExpression("r0").level(9).set(x0, 7).set(x2, 2).set(x3, 2);
            model.newExpression("r1").lower(-8).set(x2, -3);
            model.newExpression("r2").upper(-8).set(x0, 6).set(x1, 5).set(x2, 3).set(x3, 7);
            model.newExpression("r3").lower(2).set(x1, -1).set(x2, 4).set(x3, -2).set(x4, 5);
            if (primal) {
                model.options.linear().primal();
            }

            result = model.maximise();

            TestUtils.assertStateNotLessThanOptimal(result);
            TestUtils.assertEquals(17.0, result.getValue(), 1E-9);
        }
    }

}

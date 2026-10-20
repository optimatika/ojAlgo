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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.structure.Mutate1D;
import org.ojalgo.type.context.NumberContext;

/**
 * A solver instance is solved, then some variable ranges are updated in place with
 * {@link LinearSolver#updateRange(int, double, double)}, and it is solved again. This is what a branch and
 * bound search does. Every solve must terminate within {@link #ITERATIONS_ABORT} iterations, and be either
 * OPTIMAL, with a solution that satisfies the rows and ranges, or INFEASIBLE, with an infeasibility
 * certificate (GitHub issue 701). After an INFEASIBLE solve the range updates are refused, and the instance
 * is built anew on the current ranges, as a search would. The last solve must also agree with a solver built
 * from scratch on the final ranges.
 * <p>
 * The problems are linear relaxations of constraint-programming models solved by choco-solver. They have
 * integer ranges and a single-variable objective to minimise.
 * <ul>
 * <li>{@link #STEINER}: a Steiner tree model (579 variables, 70 equalities, 219 inequalities). All 55 range
 * updates are relaxations, and the final problem is feasible. The second solve returned INFEASIBLE (GitHub
 * issue 699).
 * <li>{@link #MULTI_KNAPSACK}: a multi-knapsack model (92 variables, 61 equalities). Of the 40 range updates,
 * 9 fix a variable, and the final problem is infeasible. The second solve never terminated (GitHub issue
 * 700).
 * <li>{@link #MULTI_KNAPSACK_2}, {@link #MULTI_KNAPSACK_3}: the same model, with 33 and 35 other range
 * updates, and infeasible final problems. Every reduced cost but one was zero, and the dual iterations of the
 * second solve cycled (GitHub issue 700, after the first fix).
 * <li>{@link #HC_PIZZA}: an HCPizza model (877 variables, 21 equalities, 100 inequalities). 1,000 solves, all
 * optimal, with 27,549 range updates in between. Most solves need only a pivot or two. Recorded to measure
 * the time of such cheap warm solves (GitHub issue 700), here their values must sum to {@link #HC_PIZZA_SUM}.
 * <li>{@link #COINS_GRID}: a CoinsGrid model (507 variables, 67 equalities), with 164 range updates. The
 * second solve needed 30 times more iterations than 57.1.0 did (GitHub issue 700).
 * <li>{@link #COINS_GRID_COLD}, {@link #COINS_GRID_COLD_2}, {@link #COINS_GRID_COLD_3}: the same model, other
 * ranges, a single solve of a fresh instance. Most dual pivots of phase 1 were degenerate, and the solves
 * needed up to 15,000 iterations (GitHub issue 700).
 * <li>{@link #FM3_TIGHTENING}: an fm3 model, reduced (94 variables, 8 equalities, 53 inequalities, some rows
 * with coefficients above 10^6). A solve, 4 range updates that all tighten, and a second solve. The final
 * problem is feasible, but the second solve returned INFEASIBLE with the sparse store, with a certificate
 * that was not one (GitHub issue 704).
 * <li>{@link #FM3_FAILED}: the same model, partly reduced (244 variables, 25 equalities, 477 inequalities),
 * 20 solves with 373 range updates in between. Solves 7 and 8 are infeasible, a row with only fixed variables
 * reads 1 <= 0, but were reported OPTIMAL. The last solve, on a feasible problem, returned FAILED with the
 * sparse store (GitHub issue 704).
 * </ul>
 * File format, one item per line:
 *
 * <pre>{@code
 * n N                  number of variables
 * obj J C              objective: minimise C * x[J]
 * eq RHS J:A J:A ...   sum A * x[J] == RHS
 * le RHS J:A J:A ...   sum A * x[J] <= RHS
 * bnd J LO UP          initial range LO <= x[J] <= UP
 * solve                solve with the solver built from the lines above
 * upd J LO UP          range update applied in place to that same solver
 * }</pre>
 */
public class RangeUpdateWarmStartTest extends OptimisationLinearTests {

    private static final class Scenario {

        final List<double[]> eqFactors = new ArrayList<>();
        final List<int[]> eqIndices = new ArrayList<>();
        final List<Double> eqRHS = new ArrayList<>();
        final List<double[]> leFactors = new ArrayList<>();
        final List<int[]> leIndices = new ArrayList<>();
        final List<Double> leRHS = new ArrayList<>();
        double[] lower;
        int nbVariables;
        double objectiveFactor;
        int objectiveIndex;
        /**
         * {index, lower, upper}, or null for a solve
         */
        final List<double[]> steps = new ArrayList<>();
        double[] upper;

        LinearSolver.Builder newBuilder(final double[] lowerBounds, final double[] upperBounds) {
            LinearSolver.Builder builder = LinearSolver.newBuilder(new double[nbVariables]);
            builder.objective(objectiveIndex, objectiveFactor);
            for (int i = 0; i < eqRHS.size(); i++) {
                Mutate1D body = builder.equality(eqRHS.get(i).doubleValue());
                for (int k = 0; k < eqIndices.get(i).length; k++) {
                    body.set(eqIndices.get(i)[k], eqFactors.get(i)[k]);
                }
            }
            for (int i = 0; i < leRHS.size(); i++) {
                Mutate1D body = builder.inequality(leRHS.get(i).doubleValue());
                for (int k = 0; k < leIndices.get(i).length; k++) {
                    body.set(leIndices.get(i)[k], leFactors.get(i)[k]);
                }
            }
            for (int j = 0; j < nbVariables; j++) {
                builder.range(j, lowerBounds[j], upperBounds[j]);
            }
            return builder;
        }

        /**
         * In the solver's order: the inequalities first, then the equalities.
         */
        double[] rhs() {
            int nbInequalities = leRHS.size();
            double[] rhs = new double[nbInequalities + eqRHS.size()];
            for (int i = 0; i < nbInequalities; i++) {
                rhs[i] = leRHS.get(i).doubleValue();
            }
            for (int i = 0; i < eqRHS.size(); i++) {
                rhs[nbInequalities + i] = eqRHS.get(i).doubleValue();
            }
            return rhs;
        }

        /**
         * In the solver's order: the inequalities first, then the equalities.
         */
        double[][] rows() {
            int nbInequalities = leRHS.size();
            double[][] rows = new double[nbInequalities + eqRHS.size()][nbVariables];
            for (int i = 0; i < nbInequalities; i++) {
                for (int k = 0; k < leIndices.get(i).length; k++) {
                    rows[i][leIndices.get(i)[k]] = leFactors.get(i)[k];
                }
            }
            for (int i = 0; i < eqRHS.size(); i++) {
                for (int k = 0; k < eqIndices.get(i).length; k++) {
                    rows[nbInequalities + i][eqIndices.get(i)[k]] = eqFactors.get(i)[k];
                }
            }
            return rows;
        }
    }

    private static final NumberContext ACCURACY = NumberContext.of(8);
    private static final String COINS_GRID = "/optimisation/usersupplied/ChocoCoinsGridRangeUpdate.txt";
    private static final String COINS_GRID_COLD = "/optimisation/usersupplied/ChocoCoinsGridCold.txt";
    private static final String COINS_GRID_COLD_2 = "/optimisation/usersupplied/ChocoCoinsGridCold2.txt";
    private static final String COINS_GRID_COLD_3 = "/optimisation/usersupplied/ChocoCoinsGridCold3.txt";
    /**
     * Relative to the size of the terms of a row (at least 1), or to the bound of a range. Warm solves that
     * follow many range updates, on the dense store, have residuals up to about 1e-7 relative.
     */
    private static final NumberContext FEASIBILITY = NumberContext.of(7);
    private static final String FM3_FAILED = "/optimisation/usersupplied/ChocoFm3RangeUpdate.txt";
    private static final String FM3_TIGHTENING = "/optimisation/usersupplied/ChocoFm3RangeTightening.txt";
    private static final String HC_PIZZA = "/optimisation/usersupplied/ChocoHCPizzaRangeUpdate.txt";
    /**
     * The sum of the 1,000 optimal values of {@link #HC_PIZZA}, the same with every ojAlgo version since
     * 57.1.0.
     */
    private static final double HC_PIZZA_SUM = -84533.0;
    /**
     * Each solve needs fewer than 1,000 iterations, but some needed more than 5,000 before the fixes for
     * GitHub issue 700. Without a limit, a solve that does not terminate would run until the default time
     * limit of one day.
     */
    private static final int ITERATIONS_ABORT = 5_000;
    private static final String MULTI_KNAPSACK = "/optimisation/usersupplied/ChocoMultiKnapsackRangeUpdate.txt";
    private static final String MULTI_KNAPSACK_2 = "/optimisation/usersupplied/ChocoMultiKnapsackRangeUpdate2.txt";
    private static final String MULTI_KNAPSACK_3 = "/optimisation/usersupplied/ChocoMultiKnapsackRangeUpdate3.txt";
    private static final String STEINER = "/optimisation/usersupplied/ChocoSteinerRangeRelaxation.txt";

    /**
     * The solution satisfies every row and range, by {@link #FEASIBILITY}.
     */
    private static void assertFeasible(final double[][] rows, final double[] rhs, final int nbInequalities, final double[] lower, final double[] upper,
            final Optimisation.Result result) {

        for (int j = 0; j < lower.length; j++) {
            double value = result.doubleValue(j);
            double violation = Math.max(lower[j] - value, value - upper[j]);
            double magnitude = Math.max(1.0, Math.max(Math.abs(lower[j]), Math.abs(upper[j])));
            TestUtils.assertTrue("Range " + j + " violated by " + violation, violation <= 0.0 || FEASIBILITY.isSmall(magnitude, violation));
        }

        for (int i = 0; i < rows.length; i++) {
            double body = 0.0;
            double magnitude = Math.abs(rhs[i]);
            for (int j = 0; j < lower.length; j++) {
                double term = rows[i][j] * result.doubleValue(j);
                body += term;
                magnitude += Math.abs(term);
            }
            double violation = i < nbInequalities ? body - rhs[i] : Math.abs(body - rhs[i]);
            TestUtils.assertTrue("Row " + i + " violated by " + violation, violation <= 0.0 || FEASIBILITY.isSmall(Math.max(1.0, magnitude), violation));
        }
    }

    /**
     * @param state The expected state of the last solve
     * @return The sum of the values of the optimal solves
     */
    private static double doTest(final String resource, final Optimisation.State state, final Boolean sparse) {

        Scenario scenario = RangeUpdateWarmStartTest.read(resource);

        double[][] rows = scenario.rows();
        double[] rhs = scenario.rhs();
        int nbInequalities = scenario.leRHS.size();

        Optimisation.Options options = new Optimisation.Options();
        options.linear().dual();
        options.sparse = sparse;
        options.iterations_abort = ITERATIONS_ABORT;

        double[] lower = scenario.lower.clone();
        double[] upper = scenario.upper.clone();

        LinearSolver warm = scenario.newBuilder(lower, upper).build(options);

        Optimisation.Result actual = null;
        double sum = 0.0;
        for (double[] step : scenario.steps) {
            if (step == null) {
                actual = warm.solve();
                if (actual.getState().isOptimal()) {
                    RangeUpdateWarmStartTest.assertFeasible(rows, rhs, nbInequalities, lower, upper, actual);
                    sum += actual.getValue();
                } else {
                    InfeasibilityCertificateTest.assertCertificate(rows, rhs, nbInequalities, lower, upper, actual, warm);
                }
            } else {
                int index = (int) step[0];
                lower[index] = step[1];
                upper[index] = step[2];
                if (!warm.updateRange(index, step[1], step[2])) {
                    // Refused after an INFEASIBLE solve
                    warm = scenario.newBuilder(lower, upper).build(options);
                }
            }
        }

        Optimisation.Options reference = new Optimisation.Options();
        reference.linear().dual();
        reference.sparse = Boolean.FALSE;
        reference.iterations_abort = ITERATIONS_ABORT;

        LinearSolver cold = scenario.newBuilder(lower, upper).build(reference);
        Optimisation.Result expected = cold.solve();

        TestUtils.assertEquals(state, expected.getState());
        TestUtils.assertEquals(state, actual.getState());

        if (state.isOptimal()) {
            TestUtils.assertEquals(expected.getValue(), actual.getValue(), ACCURACY);
        } else if (state == Optimisation.State.INFEASIBLE) {
            InfeasibilityCertificateTest.assertCertificate(rows, rhs, nbInequalities, lower, upper, expected, cold);
        }

        return sum;
    }

    private static Scenario read(final String resource) {

        Scenario scenario = new Scenario();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(RangeUpdateWarmStartTest.class.getResourceAsStream(resource), StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {

                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                String[] parts = line.split(" ");

                switch (parts[0]) {
                    case "n":
                        scenario.nbVariables = Integer.parseInt(parts[1]);
                        scenario.lower = new double[scenario.nbVariables];
                        scenario.upper = new double[scenario.nbVariables];
                        break;
                    case "obj":
                        scenario.objectiveIndex = Integer.parseInt(parts[1]);
                        scenario.objectiveFactor = Double.parseDouble(parts[2]);
                        break;
                    case "eq":
                    case "le":
                        int[] indices = new int[parts.length - 2];
                        double[] factors = new double[parts.length - 2];
                        for (int k = 2; k < parts.length; k++) {
                            String[] entry = parts[k].split(":");
                            indices[k - 2] = Integer.parseInt(entry[0]);
                            factors[k - 2] = Double.parseDouble(entry[1]);
                        }
                        if ("eq".equals(parts[0])) {
                            scenario.eqRHS.add(Double.valueOf(parts[1]));
                            scenario.eqIndices.add(indices);
                            scenario.eqFactors.add(factors);
                        } else {
                            scenario.leRHS.add(Double.valueOf(parts[1]));
                            scenario.leIndices.add(indices);
                            scenario.leFactors.add(factors);
                        }
                        break;
                    case "bnd":
                        scenario.lower[Integer.parseInt(parts[1])] = Double.parseDouble(parts[2]);
                        scenario.upper[Integer.parseInt(parts[1])] = Double.parseDouble(parts[3]);
                        break;
                    case "upd":
                        scenario.steps.add(new double[] { Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3]) });
                        break;
                    case "solve":
                        scenario.steps.add(null);
                        break;
                    default:
                        throw new IllegalArgumentException(line);
                }
            }

        } catch (IOException cause) {
            throw new RuntimeException(cause);
        }

        return scenario;
    }

    @Test
    public void testCoinsGridCold2Default() {
        RangeUpdateWarmStartTest.doTest(COINS_GRID_COLD_2, Optimisation.State.OPTIMAL, null);
    }

    @Test
    public void testCoinsGridCold2Dense() {
        RangeUpdateWarmStartTest.doTest(COINS_GRID_COLD_2, Optimisation.State.OPTIMAL, Boolean.FALSE);
    }

    @Test
    public void testCoinsGridCold2Sparse() {
        RangeUpdateWarmStartTest.doTest(COINS_GRID_COLD_2, Optimisation.State.OPTIMAL, Boolean.TRUE);
    }

    @Test
    public void testCoinsGridCold3Default() {
        RangeUpdateWarmStartTest.doTest(COINS_GRID_COLD_3, Optimisation.State.OPTIMAL, null);
    }

    @Test
    public void testCoinsGridCold3Dense() {
        RangeUpdateWarmStartTest.doTest(COINS_GRID_COLD_3, Optimisation.State.OPTIMAL, Boolean.FALSE);
    }

    @Test
    public void testCoinsGridCold3Sparse() {
        RangeUpdateWarmStartTest.doTest(COINS_GRID_COLD_3, Optimisation.State.OPTIMAL, Boolean.TRUE);
    }

    @Test
    public void testCoinsGridColdDefault() {
        RangeUpdateWarmStartTest.doTest(COINS_GRID_COLD, Optimisation.State.OPTIMAL, null);
    }

    @Test
    public void testCoinsGridColdDense() {
        RangeUpdateWarmStartTest.doTest(COINS_GRID_COLD, Optimisation.State.OPTIMAL, Boolean.FALSE);
    }

    @Test
    public void testCoinsGridColdSparse() {
        RangeUpdateWarmStartTest.doTest(COINS_GRID_COLD, Optimisation.State.OPTIMAL, Boolean.TRUE);
    }

    @Test
    public void testCoinsGridDefault() {
        RangeUpdateWarmStartTest.doTest(COINS_GRID, Optimisation.State.OPTIMAL, null);
    }

    @Test
    public void testCoinsGridDense() {
        RangeUpdateWarmStartTest.doTest(COINS_GRID, Optimisation.State.OPTIMAL, Boolean.FALSE);
    }

    @Test
    public void testCoinsGridSparse() {
        RangeUpdateWarmStartTest.doTest(COINS_GRID, Optimisation.State.OPTIMAL, Boolean.TRUE);
    }

    @Test
    public void testFm3FailedDefault() {
        RangeUpdateWarmStartTest.doTest(FM3_FAILED, Optimisation.State.OPTIMAL, null);
    }

    @Test
    public void testFm3FailedDense() {
        RangeUpdateWarmStartTest.doTest(FM3_FAILED, Optimisation.State.OPTIMAL, Boolean.FALSE);
    }

    @Test
    public void testFm3FailedSparse() {
        RangeUpdateWarmStartTest.doTest(FM3_FAILED, Optimisation.State.OPTIMAL, Boolean.TRUE);
    }

    @Test
    public void testFm3TighteningDefault() {
        RangeUpdateWarmStartTest.doTest(FM3_TIGHTENING, Optimisation.State.OPTIMAL, null);
    }

    @Test
    public void testFm3TighteningDense() {
        RangeUpdateWarmStartTest.doTest(FM3_TIGHTENING, Optimisation.State.OPTIMAL, Boolean.FALSE);
    }

    @Test
    public void testFm3TighteningSparse() {
        RangeUpdateWarmStartTest.doTest(FM3_TIGHTENING, Optimisation.State.OPTIMAL, Boolean.TRUE);
    }

    @Test
    public void testHCPizzaDefault() {
        double sum = RangeUpdateWarmStartTest.doTest(HC_PIZZA, Optimisation.State.OPTIMAL, null);
        TestUtils.assertEquals(HC_PIZZA_SUM, sum, ACCURACY);
    }

    @Test
    public void testHCPizzaDense() {
        double sum = RangeUpdateWarmStartTest.doTest(HC_PIZZA, Optimisation.State.OPTIMAL, Boolean.FALSE);
        TestUtils.assertEquals(HC_PIZZA_SUM, sum, ACCURACY);
    }

    @Test
    public void testHCPizzaSparse() {
        double sum = RangeUpdateWarmStartTest.doTest(HC_PIZZA, Optimisation.State.OPTIMAL, Boolean.TRUE);
        TestUtils.assertEquals(HC_PIZZA_SUM, sum, ACCURACY);
    }

    @Test
    public void testMultiKnapsack2Default() {
        RangeUpdateWarmStartTest.doTest(MULTI_KNAPSACK_2, Optimisation.State.INFEASIBLE, null);
    }

    @Test
    public void testMultiKnapsack2Dense() {
        RangeUpdateWarmStartTest.doTest(MULTI_KNAPSACK_2, Optimisation.State.INFEASIBLE, Boolean.FALSE);
    }

    @Test
    public void testMultiKnapsack2Sparse() {
        RangeUpdateWarmStartTest.doTest(MULTI_KNAPSACK_2, Optimisation.State.INFEASIBLE, Boolean.TRUE);
    }

    @Test
    public void testMultiKnapsack3Default() {
        RangeUpdateWarmStartTest.doTest(MULTI_KNAPSACK_3, Optimisation.State.INFEASIBLE, null);
    }

    @Test
    public void testMultiKnapsack3Dense() {
        RangeUpdateWarmStartTest.doTest(MULTI_KNAPSACK_3, Optimisation.State.INFEASIBLE, Boolean.FALSE);
    }

    @Test
    public void testMultiKnapsack3Sparse() {
        RangeUpdateWarmStartTest.doTest(MULTI_KNAPSACK_3, Optimisation.State.INFEASIBLE, Boolean.TRUE);
    }

    @Test
    public void testMultiKnapsackDefault() {
        RangeUpdateWarmStartTest.doTest(MULTI_KNAPSACK, Optimisation.State.INFEASIBLE, null);
    }

    @Test
    public void testMultiKnapsackDense() {
        RangeUpdateWarmStartTest.doTest(MULTI_KNAPSACK, Optimisation.State.INFEASIBLE, Boolean.FALSE);
    }

    @Test
    public void testMultiKnapsackSparse() {
        RangeUpdateWarmStartTest.doTest(MULTI_KNAPSACK, Optimisation.State.INFEASIBLE, Boolean.TRUE);
    }

    @Test
    public void testSteinerDefault() {
        RangeUpdateWarmStartTest.doTest(STEINER, Optimisation.State.OPTIMAL, null);
    }

    @Test
    public void testSteinerDense() {
        RangeUpdateWarmStartTest.doTest(STEINER, Optimisation.State.OPTIMAL, Boolean.FALSE);
    }

    @Test
    public void testSteinerSparse() {
        RangeUpdateWarmStartTest.doTest(STEINER, Optimisation.State.OPTIMAL, Boolean.TRUE);
    }

}

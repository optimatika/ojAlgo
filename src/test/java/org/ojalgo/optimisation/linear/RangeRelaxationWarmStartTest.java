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
 * A solver instance is solved, then some variable ranges are relaxed in place with
 * {@link LinearSolver#updateRange(int, double, double)}, and it is solved again. This is what a branch and
 * bound search does when it backtracks. The second solve must agree with a solver built from scratch on the
 * relaxed ranges.
 * <p>
 * The problem (579 variables, 70 equalities, 219 inequalities, integer ranges, a single-variable objective to
 * minimise) is the linear relaxation of a constraint-programming model solved by choco-solver. All 55 range
 * updates are relaxations, and all are accepted by the solver.
 * <p>
 * File format, one item per line:
 *
 * <pre>
 * n N                  number of variables
 * obj J C              objective: minimise C * x[J]
 * eq RHS J:A J:A ...   sum A * x[J] == RHS
 * le RHS J:A J:A ...   sum A * x[J] <= RHS
 * bnd J LO UP          initial range LO <= x[J] <= UP
 * solve                solve with the solver built from the lines above
 * upd J LO UP          range update applied in place to that same solver
 * </pre>
 */
public class RangeRelaxationWarmStartTest extends OptimisationLinearTests {

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
    }

    private static final NumberContext ACCURACY = NumberContext.of(8);
    private static final String RESOURCE = "/optimisation/usersupplied/ChocoSteinerRangeRelaxation.txt";

    private static void doTest(final Boolean sparse) {

        Scenario scenario = RangeRelaxationWarmStartTest.read();

        Optimisation.Options options = new Optimisation.Options();
        options.linear().dual();
        options.sparse = sparse;

        double[] lower = scenario.lower.clone();
        double[] upper = scenario.upper.clone();

        LinearSolver warm = scenario.newBuilder(lower, upper).build(options);

        Optimisation.Result actual = null;
        for (double[] step : scenario.steps) {
            if (step == null) {
                actual = warm.solve();
                TestUtils.assertStateNotLessThanOptimal(actual);
            } else {
                int index = (int) step[0];
                lower[index] = step[1];
                upper[index] = step[2];
                TestUtils.assertTrue(warm.updateRange(index, step[1], step[2]));
            }
        }

        Optimisation.Options reference = new Optimisation.Options();
        reference.linear().dual();
        reference.sparse = Boolean.FALSE;

        Optimisation.Result expected = scenario.newBuilder(lower, upper).build(reference).solve();
        TestUtils.assertStateNotLessThanOptimal(expected);

        TestUtils.assertEquals(expected.getValue(), actual.getValue(), ACCURACY);
    }

    private static Scenario read() {

        Scenario scenario = new Scenario();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(RangeRelaxationWarmStartTest.class.getResourceAsStream(RESOURCE), StandardCharsets.UTF_8))) {

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
    public void testDualDefault() {
        RangeRelaxationWarmStartTest.doTest(null);
    }

    @Test
    public void testDualDense() {
        RangeRelaxationWarmStartTest.doTest(Boolean.FALSE);
    }

    @Test
    public void testDualSparse() {
        RangeRelaxationWarmStartTest.doTest(Boolean.TRUE);
    }

}

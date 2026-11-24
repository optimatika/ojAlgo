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

import static org.ojalgo.function.constant.PrimitiveMath.*;

import java.util.Random;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.State;
import org.ojalgo.structure.Mutate1D;
import org.ojalgo.type.context.NumberContext;

/**
 * Range updates, in place, that give a variable an infinite limit or take one away. When a non-basic variable
 * lost the limit it was at, it was placed at infinity: the next solve returned OPTIMAL with the value NaN.
 * And a non-basic variable without limits stayed at 0 when it got some, also if 0 was outside the new range.
 */
public class InfiniteRangeUpdateTest extends OptimisationLinearTests {

    private static final NumberContext ACCURACY = NumberContext.of(8);

    private static void assertSolved(final double expected, final LinearSolver solver) {
        Optimisation.Result result = solver.solve();
        TestUtils.assertStateNotLessThanOptimal(result);
        TestUtils.assertEquals(expected, result.getValue(), ACCURACY);
    }

    private static double[] newRange(final Random random) {

        double lower = -random.nextInt(6);
        double upper = lower + random.nextInt(9);

        return switch (random.nextInt(8)) {
            case 0  -> new double[] { lower, lower };
            case 1  -> new double[] { lower, POSITIVE_INFINITY };
            case 2  -> new double[] { NEGATIVE_INFINITY, upper };
            case 3  -> new double[] { NEGATIVE_INFINITY, POSITIVE_INFINITY };
            default -> new double[] { lower, upper };
        };
    }

    private static LinearSolver newSolver(final LinearSolver.Builder builder, final Function<LinearStructure, SimplexStore> factory) {
        return builder.newSimplexStore(factory).newPhasedSimplexSolver(new Optimisation.Options());
    }

    /**
     * min -2x - y, subject to x + y <= 10.5, 0 <= x and 0 <= y <= 5, with x limited and then not limited. And
     * the same, mirrored.
     */
    @Test
    public void testLimitRemovedAgain() {

        for (Function<LinearStructure, SimplexStore> factory : STORE_FACTORIES) {

            LinearSolver.Builder builder = LinearSolver.newBuilder(-2.0, -1.0);
            builder.inequality(10.5, 1.0, 1.0);
            builder.range(0, ZERO, POSITIVE_INFINITY).range(1, ZERO, 5.0);

            LinearSolver solver = InfiniteRangeUpdateTest.newSolver(builder, factory);

            InfiniteRangeUpdateTest.assertSolved(-21.0, solver);
            TestUtils.assertTrue(solver.updateRange(0, ZERO, TEN));
            InfiniteRangeUpdateTest.assertSolved(-20.5, solver);
            TestUtils.assertTrue(solver.updateRange(0, ZERO, POSITIVE_INFINITY));
            InfiniteRangeUpdateTest.assertSolved(-21.0, solver);

            builder = LinearSolver.newBuilder(2.0, 1.0);
            builder.inequality(10.5, -1.0, -1.0);
            builder.range(0, NEGATIVE_INFINITY, ZERO).range(1, -5.0, ZERO);

            solver = InfiniteRangeUpdateTest.newSolver(builder, factory);

            InfiniteRangeUpdateTest.assertSolved(-21.0, solver);
            TestUtils.assertTrue(solver.updateRange(0, -10.0, ZERO));
            InfiniteRangeUpdateTest.assertSolved(-20.5, solver);
            TestUtils.assertTrue(solver.updateRange(0, NEGATIVE_INFINITY, ZERO));
            InfiniteRangeUpdateTest.assertSolved(-21.0, solver);
        }
    }

    /**
     * Two range updates, with only one solve after them
     */
    @Test
    public void testLimitRemovedAndAnotherSet() {

        for (Function<LinearStructure, SimplexStore> factory : STORE_FACTORIES) {

            LinearSolver.Builder builder = LinearSolver.newBuilder(-2.0, -1.0);
            builder.inequality(10.5, 1.0, 1.0);
            builder.range(0, ZERO, TEN).range(1, ZERO, 5.0);

            LinearSolver solver = InfiniteRangeUpdateTest.newSolver(builder, factory);

            InfiniteRangeUpdateTest.assertSolved(-20.5, solver);
            TestUtils.assertTrue(solver.updateRange(0, ZERO, POSITIVE_INFINITY));
            TestUtils.assertTrue(solver.updateRange(0, ZERO, 7.0));
            InfiniteRangeUpdateTest.assertSolved(-17.5, solver);
            TestUtils.assertTrue(solver.updateRange(0, ZERO, POSITIVE_INFINITY));
            TestUtils.assertTrue(solver.updateRange(1, ONE, ONE));
            InfiniteRangeUpdateTest.assertSolved(-20.0, solver);
        }
    }

    /**
     * x has no limits, and no cost, and is not in the basis. Then it's at least 2, which the row doesn't
     * allow unless y is 2 or more.
     */
    @Test
    public void testLimitsSet() {

        for (Function<LinearStructure, SimplexStore> factory : STORE_FACTORIES) {

            LinearSolver.Builder builder = LinearSolver.newBuilder(ZERO, ONE);
            builder.inequality(ZERO, ONE, NEG);
            builder.range(0, NEGATIVE_INFINITY, POSITIVE_INFINITY).range(1, ZERO, TEN);

            LinearSolver solver = InfiniteRangeUpdateTest.newSolver(builder, factory);

            InfiniteRangeUpdateTest.assertSolved(ZERO, solver);
            TestUtils.assertTrue(solver.updateRange(0, TWO, 5.0));
            InfiniteRangeUpdateTest.assertSolved(TWO, solver);
        }
    }

    /**
     * Sequences of range updates to small random problems: the solves in between return the same as those of
     * solvers built from scratch.
     */
    @Test
    public void testRandom() {

        int[] count = new int[3];

        for (Function<LinearStructure, SimplexStore> factory : STORE_FACTORIES) {

            for (int seed = 1; seed <= 1000; seed++) {

                Random random = new Random(seed);

                int nbVariables = 2 + random.nextInt(7);
                int nbConstraints = 1 + random.nextInt(5);

                double[] objective = new double[nbVariables];
                double[][] body = new double[nbConstraints][nbVariables];
                double[] rhs = new double[nbConstraints];
                boolean[] equality = new boolean[nbConstraints];
                double[] lower = new double[nbVariables];
                double[] upper = new double[nbVariables];
                for (int j = 0; j < nbVariables; j++) {
                    objective[j] = random.nextInt(21) - 10;
                    double[] range = InfiniteRangeUpdateTest.newRange(random);
                    lower[j] = range[0];
                    upper[j] = range[1];
                }
                for (int i = 0; i < nbConstraints; i++) {
                    equality[i] = random.nextInt(4) == 0;
                    rhs[i] = equality[i] ? random.nextInt(7) - 3 : random.nextInt(20) - 4;
                    for (int j = 0; j < nbVariables; j++) {
                        body[i][j] = random.nextInt(13) - 6;
                    }
                }

                LinearSolver warm = null;

                for (int step = 0; step < 10; step++) {

                    for (int k = random.nextInt(3); k >= 0; k--) {
                        int j = random.nextInt(nbVariables);
                        double[] range = InfiniteRangeUpdateTest.newRange(random);
                        lower[j] = range[0];
                        upper[j] = range[1];
                        if (warm != null && !warm.updateRange(j, lower[j], upper[j])) {
                            warm = null; // Refused after a failed solve
                        }
                    }

                    LinearSolver.Builder builder = LinearSolver.newBuilder(objective.clone());
                    for (int i = 0; i < nbConstraints; i++) {
                        Mutate1D row = equality[i] ? builder.equality(rhs[i]) : builder.inequality(rhs[i]);
                        for (int j = 0; j < nbVariables; j++) {
                            row.set(j, body[i][j]);
                        }
                    }
                    for (int j = 0; j < nbVariables; j++) {
                        builder.range(j, lower[j], upper[j]);
                    }

                    LinearSolver cold = InfiniteRangeUpdateTest.newSolver(builder, factory);
                    Optimisation.Result expected = cold.solve();

                    if (warm == null) {
                        warm = cold;
                        continue;
                    }

                    Optimisation.Result actual = warm.solve();

                    String description = "Seed " + seed + ", step " + step + ": " + expected + " != " + actual;

                    TestUtils.assertEquals(description, expected.getState().isOptimal(), actual.getState().isOptimal());
                    if (expected.getState().isOptimal()) {
                        TestUtils.assertEquals(description, expected.getValue(), actual.getValue(), ACCURACY);
                        count[0]++;
                    } else {
                        TestUtils.assertEquals(description, expected.getState(), actual.getState());
                        count[expected.getState() == State.UNBOUNDED ? 1 : 2]++;
                    }
                }
            }
        }

        // Optimal, unbounded and infeasible
        TestUtils.assertTrue(count[0] + ", " + count[1] + ", " + count[2], count[0] > 1000 && count[1] > 1000 && count[2] > 1000);
    }

}

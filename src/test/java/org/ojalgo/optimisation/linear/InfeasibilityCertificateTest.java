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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.optimisation.DualConventionTest;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.structure.Access1D;
import org.ojalgo.type.context.NumberContext;

/**
 * When the dual simplex concludes INFEASIBLE, the dual multipliers and the reduced gradient are an
 * infeasibility certificate (a Farkas dual ray) instead, see {@link LinearSolver#getReducedGradient(int)}.
 * GitHub issue 701.
 * <p>
 * Such a certificate comes from a basis, and so it blames a minimal set of constraints and bounds: an
 * irreducible infeasible subsystem. When a problem has only one such subsystem, the certificate must blame
 * exactly that.
 */
public class InfeasibilityCertificateTest extends OptimisationLinearTests {

    /**
     * A problem as {@link LinearSolver.Builder} defines it: the inequalities {@code row x <= rhs}, the
     * equalities {@code row x == rhs}, and the variable bounds.
     */
    private static final class Problem {

        final double[] cost;
        /**
         * The coefficients followed by the right-hand side
         */
        final List<double[]> equalities = new ArrayList<>();
        /**
         * The coefficients followed by the right-hand side
         */
        final List<double[]> inequalities = new ArrayList<>();
        final double[] lower;
        final double[] upper;

        Problem(final double... cost) {
            super();
            this.cost = cost;
            lower = new double[cost.length];
            upper = new double[cost.length];
            Arrays.fill(upper, POSITIVE_INFINITY);
        }

        double[] assertCertificate(final Optimisation.Result result, final LinearSolver solver) {

            int nbVariables = cost.length;
            int nbInequalities = inequalities.size();
            int nbConstraints = nbInequalities + equalities.size();

            double[][] rows = new double[nbConstraints][];
            double[] rhs = new double[nbConstraints];

            for (int i = 0; i < nbConstraints; i++) {
                double[] row = i < nbInequalities ? inequalities.get(i) : equalities.get(i - nbInequalities);
                rows[i] = Arrays.copyOf(row, nbVariables);
                rhs[i] = row[nbVariables];
            }

            return InfeasibilityCertificateTest.assertCertificate(rows, rhs, nbInequalities, lower, upper, result, solver);
        }

        Problem eq(final double rhs, final double... coefficients) {
            double[] row = Arrays.copyOf(coefficients, cost.length + 1);
            row[cost.length] = rhs;
            equalities.add(row);
            return this;
        }

        Problem le(final double rhs, final double... coefficients) {
            double[] row = Arrays.copyOf(coefficients, cost.length + 1);
            row[cost.length] = rhs;
            inequalities.add(row);
            return this;
        }

        LinearSolver newSolver(final Boolean sparse) {

            LinearSolver.Builder builder = LinearSolver.newBuilder(cost);

            for (double[] row : inequalities) {
                builder.inequality(row[cost.length], Arrays.copyOf(row, cost.length));
            }
            for (double[] row : equalities) {
                builder.equality(row[cost.length], Arrays.copyOf(row, cost.length));
            }
            for (int j = 0; j < cost.length; j++) {
                builder.range(j, lower[j], upper[j]);
            }

            Optimisation.Options options = new Optimisation.Options();
            options.linear().dual();
            options.sparse = sparse;

            return builder.build(options);
        }

        Problem range(final int index, final double lowerBound, final double upperBound) {
            lower[index] = lowerBound;
            upper[index] = upperBound;
            return this;
        }

    }

    private static final Boolean[] STORES = { Boolean.FALSE, Boolean.TRUE };
    /**
     * Relative to the magnitude of the numbers involved
     */
    private static final double TOLERANCE = 1E-9;

    private static void assertNoCertificate(final Optimisation.Result result, final LinearSolver solver) {

        TestUtils.assertEquals(Optimisation.State.INFEASIBLE, result.getState());

        TestUtils.assertFalse(result.getDualSolution().isPresent());
        TestUtils.assertFalse(result.getReducedGradient().isPresent());

        TestUtils.assertTrue(Double.isNaN(solver.getDualMultiplier(0)));
        TestUtils.assertTrue(Double.isNaN(solver.getReducedGradient(0)));
    }

    /**
     * Asserts that an INFEASIBLE result has an infeasibility certificate, that the solver reports the same,
     * and returns the reduced gradient part of it. The multipliers {@code y} of the inequalities must be
     * non-negative, and the reduced gradient {@code g} must combine the constraints with them. Any solution
     * would then have {@code g'x <= y'b}, but within the bounds {@code g'x} must be greater than that.
     *
     * @param rows The constraint bodies in the solver's order: the inequalities ({@code row x <= rhs}) first,
     *             then the equalities ({@code row x == rhs})
     */
    static double[] assertCertificate(final double[][] rows, final double[] rhs, final int nbInequalities, final double[] lower, final double[] upper,
            final Optimisation.Result result, final LinearSolver solver) {

        TestUtils.assertEquals(Optimisation.State.INFEASIBLE, result.getState());

        TestUtils.assertTrue(result.getDualSolution().isPresent());
        TestUtils.assertTrue(result.getReducedGradient().isPresent());

        Access1D<?> multipliers = result.getDualSolution().get().get();
        Access1D<?> gradient = result.getReducedGradient().get().get();

        int nbConstraints = rows.length;
        int nbVariables = lower.length;

        double[] y = new double[nbConstraints];
        double[] g = new double[nbVariables];

        double largest = ZERO;
        for (int i = 0; i < nbConstraints; i++) {
            y[i] = multipliers.doubleValue(i);
            TestUtils.assertEquals(y[i], solver.getDualMultiplier(i));
            largest = Math.max(largest, Math.abs(y[i]));
        }
        for (int j = 0; j < nbVariables; j++) {
            g[j] = gradient.doubleValue(j);
            TestUtils.assertEquals(g[j], solver.getReducedGradient(j));
        }

        TestUtils.assertTrue("No multiplier is nonzero", largest > ZERO);

        for (int i = 0; i < nbInequalities; i++) {
            TestUtils.assertTrue("Multiplier " + i + " is " + y[i], y[i] >= -TOLERANCE * largest);
        }

        for (int j = 0; j < nbVariables; j++) {
            double combined = ZERO;
            double magnitude = ZERO;
            for (int i = 0; i < nbConstraints; i++) {
                double term = rows[i][j] * y[i];
                combined += term;
                magnitude += Math.abs(term);
            }
            TestUtils.assertEquals("Variable " + j, combined, g[j], TOLERANCE * Math.max(magnitude, largest));
        }

        double minimum = ZERO;
        double magnitude = ZERO;
        for (int j = 0; j < nbVariables; j++) {
            if (Math.abs(g[j]) > TOLERANCE * largest) {
                double bound = g[j] > ZERO ? lower[j] : upper[j];
                TestUtils.assertTrue("Variable " + j + " has an infinite bound", Double.isFinite(bound));
                minimum += g[j] * bound;
                magnitude += Math.abs(g[j] * bound);
            }
        }
        double combined = ZERO;
        for (int i = 0; i < nbConstraints; i++) {
            combined += y[i] * rhs[i];
            magnitude += Math.abs(y[i] * rhs[i]);
        }
        TestUtils.assertTrue(minimum + " is not greater than " + combined, minimum - combined > TOLERANCE * Math.max(magnitude, largest));

        return g;
    }

    /**
     * The bounds play no part, only the two contradicting constraints do. Another constraint, and the
     * variables only in that, are not part of the certificate either.
     */
    @Test
    public void testContradictingConstraints() {

        Problem problem = new Problem(1, 1, 1, 1).le(1, 1, 1).le(-3, -1, -1).le(10, 0, 0, 1, 1);
        problem.range(0, 0, 10).range(1, 0, 10).range(2, 0, 4).range(3, 0, 4);

        for (Boolean sparse : STORES) {

            LinearSolver solver = problem.newSolver(sparse);
            Optimisation.Result result = solver.solve();

            double[] gradient = problem.assertCertificate(result, solver);

            for (int j = 0; j < gradient.length; j++) {
                TestUtils.assertEquals(ZERO, gradient[j], TOLERANCE);
            }
            TestUtils.assertTrue(solver.getDualMultiplier(0) > ZERO);
            TestUtils.assertTrue(solver.getDualMultiplier(1) > ZERO);
            TestUtils.assertEquals(ZERO, solver.getDualMultiplier(2), TOLERANCE);
        }
    }

    /**
     * Crossing bounds make the problem infeasible, but there is no certificate.
     */
    @Test
    public void testCrossingBounds() {

        Problem problem = new Problem(-1, -1).le(4, 1, 1).range(0, 0, 5).range(1, 0, 5);

        for (Boolean sparse : STORES) {

            LinearSolver solver = problem.newSolver(sparse);
            TestUtils.assertStateNotLessThanOptimal(solver.solve());

            TestUtils.assertTrue(solver.updateRange(0, 3, 2));
            Optimisation.Result result = solver.solve();

            InfeasibilityCertificateTest.assertNoCertificate(result, solver);
        }
    }

    /**
     * x0 + 2 x1 == 10 can not be reached with the upper bounds 2.
     */
    @Test
    public void testEquality() {

        Problem problem = new Problem(1, 1).eq(10, 1, 2).range(0, 0, 2).range(1, 0, 2);

        for (Boolean sparse : STORES) {

            LinearSolver solver = problem.newSolver(sparse);
            Optimisation.Result result = solver.solve();

            double[] gradient = problem.assertCertificate(result, solver);

            TestUtils.assertTrue(gradient[0] < ZERO);
            TestUtils.assertTrue(gradient[1] < ZERO);
        }
    }

    /**
     * x0 + x1 <= -1 is violated at the lower bounds, 0. The constraint with x2 is not part of the
     * certificate.
     */
    @Test
    public void testLowerBounds() {

        Problem problem = new Problem(1, 1, 1).le(-1, 1, 1).le(3, 1, 0, 1).range(0, 0, 5).range(1, 0, 5).range(2, -1, 1);

        for (Boolean sparse : STORES) {

            LinearSolver solver = problem.newSolver(sparse);
            Optimisation.Result result = solver.solve();

            double[] gradient = problem.assertCertificate(result, solver);

            TestUtils.assertTrue(gradient[0] > ZERO);
            TestUtils.assertTrue(gradient[1] > ZERO);
            TestUtils.assertEquals(ZERO, gradient[2], TOLERANCE);
            TestUtils.assertEquals(ZERO, solver.getDualMultiplier(1), TOLERANCE);
        }
    }

    /**
     * An infeasible model, solved via {@link ExpressionsBasedModel}: the certificate is mapped to the model,
     * the same whether it is minimised or maximised (see {@link Optimisation.Result#getDualSolution()}). The
     * two constraints contradict each other, so no variable bound is involved.
     */
    @Test
    public void testModel() {

        for (Boolean sparse : STORES) {
            for (Optimisation.Sense sense : Optimisation.Sense.values()) {

                ExpressionsBasedModel model = new ExpressionsBasedModel();
                model.options.linear().dual();
                model.options.sparse = sparse;

                Variable x = model.addVariable("x").lower(0).upper(10).weight(1);
                Variable y = model.addVariable("y").lower(0).upper(10).weight(1);
                Variable z = model.addVariable("z").lower(0).upper(10).weight(1);

                model.addExpression("upper").set(x, 1).set(y, 1).set(z, -1).upper(1);
                model.addExpression("lower").set(x, 1).set(y, 1).set(z, -1).lower(3);

                Optimisation.Result result = sense.solve(model);

                double[] gradient = DualConventionTest.assertInfeasibilityCertificate("model " + sense, model, result, NumberContext.of(8));

                for (int j = 0; j < gradient.length; j++) {
                    TestUtils.assertEquals(ZERO, gradient[j], TOLERANCE);
                }
            }
        }
    }

    /**
     * {@link SimplexTableauSolver}, the primal simplex the builder uses when no bounds are set, provides no
     * certificate.
     */
    @Test
    public void testPrimalTableau() {

        LinearSolver solver = LinearSolver.newBuilder(1, 1).inequality(-1, 1, 1).build();
        Optimisation.Result result = solver.solve();

        InfeasibilityCertificateTest.assertNoCertificate(result, solver);
    }

    /**
     * Small random problems, with some infinite bounds. Every INFEASIBLE result must have a certificate.
     */
    @Test
    public void testRandom() {

        Random random = new Random(701);

        int nbInfeasible = 0;

        for (int p = 0; p < 500; p++) {

            int nbVariables = 2 + random.nextInt(6);
            int nbInequalities = 1 + random.nextInt(6);
            int nbEqualities = random.nextInt(3);

            double[] cost = new double[nbVariables];
            for (int j = 0; j < nbVariables; j++) {
                cost[j] = random.nextInt(7) - 3;
            }

            Problem problem = new Problem(cost);

            for (int i = 0; i < nbInequalities + nbEqualities; i++) {
                double[] row = new double[nbVariables];
                for (int j = 0; j < nbVariables; j++) {
                    row[j] = random.nextInt(9) - 4;
                }
                double rhs = random.nextInt(17) - 8;
                if (i < nbInequalities) {
                    problem.le(rhs, row);
                } else {
                    problem.eq(rhs, row);
                }
            }

            for (int j = 0; j < nbVariables; j++) {
                double lower = random.nextInt(5) == 0 ? NEGATIVE_INFINITY : -random.nextInt(6);
                double upper = random.nextInt(5) == 0 ? POSITIVE_INFINITY : random.nextInt(6);
                problem.range(j, lower, upper);
            }

            for (Boolean sparse : STORES) {

                LinearSolver solver = problem.newSolver(sparse);
                Optimisation.Result result = solver.solve();

                if (result.getState() == Optimisation.State.INFEASIBLE) {
                    problem.assertCertificate(result, solver);
                    nbInfeasible++;
                }
            }
        }

        TestUtils.assertTrue("Only " + nbInfeasible + " infeasible", nbInfeasible >= 10);
    }

    /**
     * x0 + x1 >= 3 can not be reached with the upper bounds 1. The constraint with x2 is not part of the
     * certificate.
     */
    @Test
    public void testUpperBounds() {

        Problem problem = new Problem(1, 1, 1).le(-3, -1, -1).le(5, -1, 0, 1).range(0, 0, 1).range(1, 0, 1).range(2, 0, 1);

        for (Boolean sparse : STORES) {

            LinearSolver solver = problem.newSolver(sparse);
            Optimisation.Result result = solver.solve();

            double[] gradient = problem.assertCertificate(result, solver);

            TestUtils.assertTrue(gradient[0] < ZERO);
            TestUtils.assertTrue(gradient[1] < ZERO);
            TestUtils.assertEquals(ZERO, gradient[2], TOLERANCE);
            TestUtils.assertEquals(ZERO, solver.getDualMultiplier(1), TOLERANCE);
        }
    }

    /**
     * Solved, then the ranges are updated in place and it is solved again (warm start): the use case of a
     * branch and bound or constraint programming search.
     */
    @Test
    public void testWarmStart() {

        Problem problem = new Problem(1, 1).le(-3, -1, -1).range(0, 0, 5).range(1, 0, 5);

        for (Boolean sparse : STORES) {

            LinearSolver solver = problem.newSolver(sparse);
            TestUtils.assertStateNotLessThanOptimal(solver.solve());

            problem.range(0, 0, 1).range(1, 0, 1);
            TestUtils.assertTrue(solver.updateRange(0, 0, 1));
            TestUtils.assertTrue(solver.updateRange(1, 0, 1));
            Optimisation.Result result = solver.solve();

            double[] gradient = problem.assertCertificate(result, solver);

            TestUtils.assertTrue(gradient[0] < ZERO);
            TestUtils.assertTrue(gradient[1] < ZERO);

            problem.range(0, 0, 5).range(1, 0, 5);
        }
    }

}

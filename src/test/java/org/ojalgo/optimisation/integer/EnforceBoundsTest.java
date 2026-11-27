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

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.optimisation.linear.LinearSolver;

/**
 * Enforcing the bounds of a node, in a dive: the LP solver is updated in place, not rebuilt.
 */
public class EnforceBoundsTest extends OptimisationIntegerTests {

    private static Optimisation.Environment newEnvironment(final AtomicInteger builds) {
        Optimisation.Environment retVal = Optimisation.newEnvironment();
        retVal.addIntegration(LinearSolver.INTEGRATION.withOptionsModifier(options -> builds.incrementAndGet()));
        return retVal;
    }

    private static NodeSolver newSolver(final ExpressionsBasedModel model, final NodeKey key, final ModelStrategy strategy) {
        ExpressionsBasedModel nodeModel = model.snapshot();
        key.setNodeState(nodeModel, strategy);
        return nodeModel.prepare(Optimisation.Sense.MAX, NodeSolver::new);
    }

    private static double solve(final NodeSolver solver) {
        Optimisation.Result result = solver.solve();
        TestUtils.assertStateNotLessThanOptimal(result);
        return result.getValue();
    }

    /**
     * The same node with other bounds of one variable, but not what that implies for the others
     */
    private static NodeKey tighten(final NodeKey key, final ExpressionsBasedModel model, final ModelStrategy strategy, final int index, final int lower,
            final int upper) {

        ExpressionsBasedModel unconstrained = new ExpressionsBasedModel();
        for (Variable variable : model.getVariables()) {
            unconstrained.newVariable(variable.getName()).lower(variable.getLowerLimit()).upper(variable.getUpperLimit());
        }

        int[] lowerBounds = key.copyLowerBounds();
        int[] upperBounds = key.copyUpperBounds();
        lowerBounds[index] = lower;
        upperBounds[index] = upper;

        return key.withTightened(lowerBounds, upperBounds, BoundPropagator.of(unconstrained, strategy));
    }

    /**
     * A variable that can be negative as well as positive is fixed, with any of ojAlgo's LP solvers. With the
     * primal ones it's two variables, and only one of them was fixed: the LP solution had another value. (The
     * LP is not the same as one built from scratch, as that has its limits rounded, but it can't be tighter.)
     */
    @Test
    public void testFixed() {

        int nbCompared = 0;

        for (boolean primal : new boolean[] { false, true }) {
            for (Boolean sparse : new Boolean[] { Boolean.FALSE, Boolean.TRUE }) {
                for (int seed = 1; seed <= 100; seed++) {

                    Random random = new Random(seed);

                    ExpressionsBasedModel model = new ExpressionsBasedModel();
                    model.options.sparse = sparse;
                    if (primal) {
                        model.options.linear().primal();
                    } else {
                        model.options.linear().dual();
                    }
                    for (int j = 0; j < 4; j++) {
                        model.newVariable("x" + j).integer().lower(-5).upper(5).weight(random.nextInt(21) - 10);
                    }
                    for (int i = 0; i < 3; i++) {
                        Expression row = model.newExpression("row" + i).lower(-7).upper(7);
                        for (int j = 0; j < 4; j++) {
                            row.set(j, random.nextInt(15) - 7);
                        }
                    }

                    ModelStrategy strategy = IntegerStrategy.DEFAULT.newModelStrategy(model);
                    NodeKey root = new NodeKey(model);
                    NodeSolver solver = EnforceBoundsTest.newSolver(model, root, strategy);
                    EnforceBoundsTest.solve(solver);

                    int index = random.nextInt(4);
                    int value = random.nextInt(11) - 5;
                    NodeKey key = EnforceBoundsTest.tighten(root, model, strategy, index, value, value);
                    key.enforceBounds(solver, root, strategy);

                    Optimisation.Result actual = solver.solve();
                    Optimisation.Result scratch = EnforceBoundsTest.newSolver(model, key, strategy).solve();

                    String description = "Primal " + primal + ", sparse " + sparse + ", seed " + seed + ", x" + index + " = " + value;

                    if (actual.getState().isOptimal()) {
                        TestUtils.assertEquals(description, value, actual.doubleValue(index), 1E-7);
                        nbCompared++;
                    }
                    if (scratch.getState().isOptimal()) {
                        TestUtils.assertTrue(description, actual.getState().isOptimal());
                        TestUtils.assertTrue(description, actual.getValue() >= scratch.getValue() - 1E-7);
                    }
                }
            }
        }

        TestUtils.assertTrue("Compared " + nbCompared, nbCompared > 100);
    }

    /**
     * Models small enough to enumerate all the combinations, with variables that can be both negative and
     * positive.
     */
    @Test
    public void testNegativeRanges() {

        int nbFeasible = 0;

        for (int seed = 1; seed <= 200; seed++) {

            Random random = new Random(seed);

            int nbVariables = 3 + random.nextInt(3);
            int nbConstraints = 1 + random.nextInt(4);

            int[] lower = new int[nbVariables];
            int[] upper = new int[nbVariables];
            int[] weights = new int[nbVariables];
            for (int j = 0; j < nbVariables; j++) {
                lower[j] = -random.nextInt(5);
                upper[j] = lower[j] + 1 + random.nextInt(6);
                weights[j] = random.nextInt(21) - 10;
            }
            int[][] factors = new int[nbConstraints][nbVariables];
            int[] limits = new int[nbConstraints];
            for (int i = 0; i < nbConstraints; i++) {
                for (int j = 0; j < nbVariables; j++) {
                    factors[i][j] = random.nextInt(15) - 7;
                }
                limits[i] = random.nextInt(21) - 10;
            }

            double expected = Double.NEGATIVE_INFINITY;
            int[] values = lower.clone();
            int position = 0;
            while (position < nbVariables) {
                boolean feasible = true;
                for (int i = 0; i < nbConstraints; i++) {
                    int body = 0;
                    for (int j = 0; j < nbVariables; j++) {
                        body += factors[i][j] * values[j];
                    }
                    feasible &= body <= limits[i];
                }
                if (feasible) {
                    int value = 0;
                    for (int j = 0; j < nbVariables; j++) {
                        value += weights[j] * values[j];
                    }
                    expected = Math.max(expected, value);
                }
                position = 0;
                while (position < nbVariables && ++values[position] > upper[position]) {
                    values[position] = lower[position];
                    position++;
                }
            }

            for (boolean cuts : new boolean[] { true, false }) {

                ExpressionsBasedModel model = new ExpressionsBasedModel();
                for (int j = 0; j < nbVariables; j++) {
                    model.newVariable("x" + j).integer().lower(lower[j]).upper(upper[j]).weight(weights[j]);
                }
                for (int i = 0; i < nbConstraints; i++) {
                    Expression row = model.newExpression("row" + i).upper(limits[i]);
                    for (int j = 0; j < nbVariables; j++) {
                        row.set(j, factors[i][j]);
                    }
                }
                if (!cuts) {
                    model.options.integer(IntegerStrategy.DEFAULT.withCutConfiguration(IntegerStrategy.DEFAULT.getCutConfiguration().withTypes()));
                }

                Optimisation.Result result = model.maximise();

                String description = "Seed " + seed + ", cuts " + cuts + ": " + expected + " != " + result;

                if (Double.isFinite(expected)) {
                    TestUtils.assertTrue(description, result.getState().isOptimal());
                    TestUtils.assertEquals(description, expected, result.getValue(), 1E-6);
                    nbFeasible++;
                } else {
                    TestUtils.assertEquals(description, Optimisation.State.INFEASIBLE, result.getState());
                }
            }
        }

        TestUtils.assertTrue("Feasible: " + nbFeasible, nbFeasible > 100);
    }

    /**
     * A variable that can be negative as well as positive is limited to negative values, and then to
     * positive ones
     */
    @Test
    public void testSignChange() {

        for (boolean negative : new boolean[] { true, false }) {

            AtomicInteger builds = new AtomicInteger();

            ExpressionsBasedModel model = EnforceBoundsTest.newEnvironment(builds).newModel();
            Variable x = model.newVariable("x").integer().lower(-5).upper(5).weight(1);
            Variable y = model.newVariable("y").integer().lower(-5).upper(5).weight(2);
            Variable z = model.newVariable("z").integer().lower(-5).upper(5).weight(3);
            model.newExpression("r0").set(x, 2).set(y, 3).set(z, 4).lower(-7).upper(7);
            model.newExpression("r1").set(x, 5).set(y, -3).set(z, 2).lower(-9).upper(9);

            ModelStrategy strategy = IntegerStrategy.DEFAULT.newModelStrategy(model);
            NodeKey root = new NodeKey(model);
            NodeSolver solver = EnforceBoundsTest.newSolver(model, root, strategy);

            EnforceBoundsTest.solve(solver);
            TestUtils.assertEquals(1, builds.get());

            BoundPropagator propagator = BoundPropagator.of(model, strategy);
            NodeKey key = negative ? root.createLowerBranch(1, -0.5, 0.0, propagator, null) : root.createUpperBranch(1, 0.5, 0.0, propagator, null);
            key.enforceBounds(solver, root, strategy);
            double actual = EnforceBoundsTest.solve(solver);
            TestUtils.assertEquals(1, builds.get());

            double expected = EnforceBoundsTest.solve(EnforceBoundsTest.newSolver(model, key, strategy));
            TestUtils.assertEquals(expected, actual, 1E-9);
            TestUtils.assertEquals(2, builds.get());

            // Also when the bounds are not those of the branching

            solver = EnforceBoundsTest.newSolver(model, root, strategy);
            EnforceBoundsTest.solve(solver);
            TestUtils.assertEquals(3, builds.get());

            key = EnforceBoundsTest.tighten(root, model, strategy, 1, negative ? -5 : 1, negative ? -1 : 5);
            key.enforceBounds(solver, 1, strategy);
            actual = EnforceBoundsTest.solve(solver);
            TestUtils.assertEquals(3, builds.get());

            expected = EnforceBoundsTest.solve(EnforceBoundsTest.newSolver(model, key, strategy));
            TestUtils.assertEquals(expected, actual, 1E-9);
        }
    }

    /**
     * Presolve fixes y, as x is 1, so y is not in the LP. Enforcing that y is 0 changes nothing, and the LP
     * can still be updated in place afterwards.
     */
    @Test
    public void testVariableNotInSolver() {

        AtomicInteger builds = new AtomicInteger();

        ExpressionsBasedModel model = EnforceBoundsTest.newEnvironment(builds).newModel();
        Variable x = model.newVariable("x").binary().weight(3);
        Variable y = model.newVariable("y").binary().weight(2);
        Variable u = model.newVariable("u").binary().weight(5);
        Variable v = model.newVariable("v").binary().weight(4);
        Variable w = model.newVariable("w").binary().weight(3);
        model.newExpression("r0").set(x, 1).set(y, 1).upper(1);
        model.newExpression("r1").set(u, 2).set(v, 3).set(w, 4).upper(6);

        ModelStrategy strategy = IntegerStrategy.DEFAULT.newModelStrategy(model);
        NodeKey key = EnforceBoundsTest.tighten(new NodeKey(model), model, strategy, 0, 1, 1);
        NodeSolver solver = EnforceBoundsTest.newSolver(model, key, strategy);

        EnforceBoundsTest.solve(solver);
        TestUtils.assertEquals(1, builds.get());
        TestUtils.assertFalse(solver.isInSolver(1));
        TestUtils.assertTrue(solver.isInSolver(2));

        key = EnforceBoundsTest.tighten(key, model, strategy, 1, 0, 0);
        key.enforceBounds(solver, 1, strategy);
        EnforceBoundsTest.solve(solver);
        TestUtils.assertEquals(1, builds.get());

        key = EnforceBoundsTest.tighten(key, model, strategy, 2, 0, 0);
        key.enforceBounds(solver, 2, strategy);
        double actual = EnforceBoundsTest.solve(solver);
        TestUtils.assertEquals(1, builds.get());

        TestUtils.assertEquals(EnforceBoundsTest.solve(EnforceBoundsTest.newSolver(model, key, strategy)), actual, 1E-9);
    }

}

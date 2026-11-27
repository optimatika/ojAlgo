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
import org.ojalgo.optimisation.ModelFileTest;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.optimisation.integer.IntegerStrategy.CutConfiguration;
import org.ojalgo.optimisation.integer.IntegerStrategy.CutType;
import org.ojalgo.optimisation.linear.LinearSolver;

/**
 * A cut round solves the LP relaxation again, with the cuts. That result, and that LP solver, are what the
 * search continues with – the same LP is not built and solved once more.
 */
public class CutRoundTest extends OptimisationIntegerTests {

    /**
     * Counts the dives, and the cut rounds that added cuts at nodes other than those before the search
     */
    private static final class Counter extends ModelStrategy.AbstractStrategy {

        private long myLastSequence = -1L;
        int dives = 0;
        int rounds = 0;

        Counter(final ExpressionsBasedModel model, final IntegerStrategy strategy) {
            super(model, strategy);
        }

        private void evaluated(final NodeKey key, final boolean diveContinues) {
            if (myLastSequence < 0L || key.parent != myLastSequence && key.sequence != myLastSequence) {
                dives++;
            }
            myLastSequence = diveContinues ? key.sequence : -1L;
        }

        @Override
        protected void markInfeasible(final NodeKey key, final boolean found, final double value) {
            if (myLastSequence == key.sequence) {
                myLastSequence = -1L; // Solved to an integer solution, but not a valid one. Already counted.
            } else if (key.isFeasible()) { // Otherwise not evaluated
                this.evaluated(key, false);
            }
            super.markInfeasible(key, found, value);
        }

        @Override
        protected void onCutSuccess(final NodeKey nodeKey) {
            rounds++;
            super.onCutSuccess(nodeKey);
        }

        @Override
        protected void onNodeSolved(final NodeKey key, final Optimisation.Result child, final double childObj, final boolean minimisation) {
            this.evaluated(key, true);
            super.onNodeSolved(key, child, childObj, minimisation);
        }

    }

    /**
     * A random multidimensional knapsack
     */
    private static ExpressionsBasedModel newModel(final Optimisation.Environment environment, final long seed) {

        Random random = new Random(seed);

        ExpressionsBasedModel model = environment.newModel();

        Expression[] rows = new Expression[4];
        int[] totals = new int[rows.length];
        for (int r = 0; r < rows.length; r++) {
            rows[r] = model.newExpression("row" + r);
        }
        for (int i = 0; i < 25; i++) {
            Variable item = model.newVariable("x" + i).binary().weight(10 + random.nextInt(90));
            for (int r = 0; r < rows.length; r++) {
                int coefficient = 5 + random.nextInt(60);
                totals[r] += coefficient;
                rows[r].set(item, coefficient);
            }
        }
        for (int r = 0; r < rows.length; r++) {
            rows[r].upper(totals[r] / 2);
        }

        return model;
    }

    /**
     * Implied bound cuts are generated at the root only. Here the LP relaxation has y = 10 and x = 0.2, and
     * the implied bound cut y &lt;= 10 x (instead of 50 x) moves the bound from 48 to 40.
     */
    @Test
    public void testImpliedBoundsAtRootOnly() {

        CutConfiguration cuts = IntegerStrategy.DEFAULT.getCutConfiguration().withTypes(CutType.IMPLIED_BOUNDS);

        for (boolean root : new boolean[] { true, false }) {

            ExpressionsBasedModel model = new ExpressionsBasedModel();
            Variable y = model.newVariable("y").lower(0).upper(10).weight(5);
            Variable x = model.newVariable("x").binary().weight(-10);
            model.newExpression("vub").set(y, 1).set(x, -50).upper(0);

            ModelStrategy strategy = IntegerStrategy.DEFAULT.withCutConfiguration(cuts).newModelStrategy(model);

            ExpressionsBasedModel relaxed = model.snapshot();
            NodeSolver solver = relaxed.prepare(Optimisation.Sense.MAX, NodeSolver::new);
            TestUtils.assertEquals(48.0, solver.solve().getValue(), 1E-9);

            Optimisation.Result result = solver.generateCuts(strategy, new CutStatistics(Optimisation.Sense.MAX), root);

            if (root) {
                TestUtils.assertStateNotLessThanOptimal(result);
                TestUtils.assertEquals(40.0, result.getValue(), 1E-9);
            } else {
                TestUtils.assertNull(result);
            }
        }
    }

    /**
     * One LP is built for each dive, and one for each cut round – with one round per attempt. (Fewer, when
     * the presolver finds a node infeasible.)
     */
    @Test
    public void testNumberOfSolversBuilt() {

        CutConfiguration cuts = IntegerStrategy.DEFAULT.getCutConfiguration().withIterations(1);

        int rounds = 0;

        for (long seed = 1L; seed <= 10L; seed++) {

            AtomicInteger builds = new AtomicInteger();
            Optimisation.Environment environment = Optimisation.newEnvironment();
            environment.addIntegration(LinearSolver.INTEGRATION.withOptionsModifier(options -> builds.incrementAndGet()));

            Counter[] counter = new Counter[1];

            ExpressionsBasedModel model = CutRoundTest.newModel(environment, seed);
            model.options.parallelism(1);
            model.options.integer(IntegerStrategy.DEFAULT.withCutConfiguration(cuts).withModelStrategyFactory((m, s) -> counter[0] = new Counter(m, s)));

            TestUtils.assertStateNotLessThanOptimal(model.maximise());

            // The root cut round, before the search starts, is not reported to the strategy
            int limit = counter[0].dives + counter[0].rounds + 1;

            TestUtils.assertTrue("Seed " + seed + ": " + builds + " solvers built, " + counter[0].dives + " dives and " + counter[0].rounds + " cut rounds",
                    builds.get() > 0 && builds.get() <= limit);

            rounds += counter[0].rounds;
        }

        TestUtils.assertTrue("Cut rounds: " + rounds, rounds > 20);
    }

    /**
     * At the root the rounds go on as long as they improve the bound. With bell3b it takes more than the 3
     * rounds there are at other nodes.
     */
    @Test
    public void testRootRounds() {

        double[] values = new double[2];

        for (int i = 0; i < 2; i++) {

            ExpressionsBasedModel model = ModelFileTest.makeModel("MIPLIB", "bell3b.mps", false).simplify();
            ModelStrategy strategy = IntegerStrategy.DEFAULT.newModelStrategy(model);

            ExpressionsBasedModel rootModel = model.snapshot();
            NodeKey.newRoot(model, BoundPropagator.of(model, strategy)).setNodeState(rootModel, strategy);
            NodeSolver solver = rootModel.prepare(Optimisation.Sense.MIN, NodeSolver::new);
            solver.solve(null);

            Optimisation.Result result = solver.generateCuts(strategy, new CutStatistics(Optimisation.Sense.MIN), i == 1);

            TestUtils.assertStateNotLessThanOptimal(result);
            values[i] = result.getValue();
        }

        TestUtils.assertTrue(values[0] + " < " + values[1], values[0] < values[1] - 1000.0);
    }

    /**
     * The result is that of the relaxation with the cuts, and the LP solver can be updated in place
     */
    @Test
    public void testSolvedAfterwards() {

        int nbCuts = 0;

        for (long seed = 1L; seed <= 10L; seed++) {

            ExpressionsBasedModel model = CutRoundTest.newModel(Optimisation.newEnvironment(), seed);
            ModelStrategy strategy = IntegerStrategy.DEFAULT.newModelStrategy(model);

            ExpressionsBasedModel relaxed = model.snapshot();
            NodeSolver solver = relaxed.prepare(Optimisation.Sense.MAX, NodeSolver::new);

            Optimisation.Result before = solver.solve();
            Optimisation.Result after = solver.generateCuts(strategy, new CutStatistics(Optimisation.Sense.MAX), true);

            if (after == null) {
                continue;
            }
            nbCuts += relaxed.countExpressions() - model.countExpressions();

            TestUtils.assertStateNotLessThanOptimal(after);
            TestUtils.assertTrue(after.getValue() <= before.getValue() + 1E-9);

            boolean anyReducedGradient = false;
            int fractional = -1;
            for (int i = 0; i < relaxed.countVariables(); i++) {
                anyReducedGradient |= solver.getReducedGradient(i) != 0.0;
                if (Math.abs(after.doubleValue(i) - Math.rint(after.doubleValue(i))) > 1E-3) {
                    fractional = i;
                }
            }
            TestUtils.assertTrue(anyReducedGradient); // It's 0 if not solved
            TestUtils.assertTrue(fractional >= 0);

            // In place
            solver.update(fractional, 0.0, 0.0);
            double updated = solver.solve().getValue();

            // From scratch
            solver.reset();
            TestUtils.assertEquals(after.getValue(), solver.solve().getValue(), 1E-9);
            solver.reset();
            relaxed.getVariable(fractional).level(0);
            TestUtils.assertEquals(updated, solver.solve().getValue(), 1E-9);
        }

        TestUtils.assertTrue("Cuts: " + nbCuts, nbCuts > 0);
    }

}

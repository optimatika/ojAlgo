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

import java.io.StringWriter;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.ModelFileTest;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;

/**
 * The dual bound of the final progress log, whether the search was completed or stopped. When it was stopped
 * it has to include the nodes that were being worked on, as well as those waiting.
 */
public class FinalDualBoundTest extends OptimisationIntegerTests implements ModelFileTest {

    /**
     * Stops the search – as if the time is up – after some number of node problems
     */
    private static final class Stopper extends ModelStrategy.AbstractStrategy {

        private final AtomicInteger myCount;
        private final int myLimit;
        private final Optimisation.Options myOptions;

        Stopper(final ExpressionsBasedModel model, final IntegerStrategy strategy, final Optimisation.Options options, final int limit,
                final AtomicInteger count) {
            super(model, strategy);
            myOptions = options;
            myLimit = limit;
            myCount = count;
        }

        @Override
        protected void onNodeSolved(final NodeKey key, final Optimisation.Result child, final double childObj, final boolean minimisation) {
            super.onNodeSolved(key, child, childObj, minimisation);
            if (myCount.incrementAndGet() >= myLimit) {
                myOptions.time_abort = 0L;
            }
        }

    }

    private static final Pattern DUAL_BOUND = Pattern.compile("dualBound=([^,\\s]+)");

    /**
     * Small random models, each stopped after every possible number of node problems. Then the last node
     * waiting, or being worked on, is often the one that matters.
     */
    private static void doTestRandom(final Optimisation.Sense sense, final int nbWorkers) {

        Optimisation.Result[] result = new Optimisation.Result[1];
        int nbStopped = 0;

        for (long seed = 1L; seed <= 60L; seed++) {

            FinalDualBoundTest.solve(FinalDualBoundTest.newRandom(seed, sense), sense, 0, 1, IntegerStrategy.DEFAULT, result);
            TestUtils.assertStateNotLessThanOptimal(result[0]);
            double optimum = result[0].getValue();

            for (int nodeLimit = 1; nodeLimit <= 100; nodeLimit++) {

                double bound = FinalDualBoundTest.solve(FinalDualBoundTest.newRandom(seed, sense), sense, nodeLimit, nbWorkers, IntegerStrategy.DEFAULT,
                        result);

                if (result[0].getState().isOptimal() || result[0].getState() == Optimisation.State.INFEASIBLE) {
                    break; // Not stopped
                }
                nbStopped++;

                String description = "Seed " + seed + ", stopped after " + nodeLimit + " nodes: " + result[0].getState() + ", dual bound " + bound
                        + ", optimum " + optimum;

                TestUtils.assertTrue(description, Double.isFinite(bound));
                if (sense == Optimisation.Sense.MIN) {
                    TestUtils.assertTrue(description, bound <= optimum + 1.0E-6);
                } else {
                    TestUtils.assertTrue(description, bound >= optimum - 1.0E-6);
                }
            }
        }

        TestUtils.assertTrue("Stopped: " + nbStopped, nbStopped > 100);
    }

    /**
     * Stopped after each number of nodes: the logged bound is a valid one
     *
     * @param optimum The known optimal value
     */
    private static void doTestStopped(final boolean knapsack, final Optimisation.Sense sense, final double optimum, final int nbWorkers,
            final IntegerStrategy.ConfigurableStrategy strategy) {

        Optimisation.Result[] result = new Optimisation.Result[1];
        int nbStopped = 0;

        for (int nodeLimit = 1; nodeLimit <= 120; nodeLimit += nodeLimit < 10 ? 1 : 11) {

            ExpressionsBasedModel model = knapsack ? FinalDualBoundTest.newKnapsack() : ModelFileTest.makeModel("MIPLIB", "p0201.mps", false);

            double bound = FinalDualBoundTest.solve(model, sense, nodeLimit, nbWorkers, strategy, result);

            String description = "Stopped after " + nodeLimit + " nodes: " + result[0].getState() + ", dual bound " + bound + ", optimum " + optimum;

            if (result[0].getState() == Optimisation.State.FEASIBLE || result[0].getState() == Optimisation.State.FAILED) {
                nbStopped++;
            }

            TestUtils.assertTrue(description, Double.isFinite(bound));
            double tolerance = 1.0E-6 * Math.abs(optimum);
            if (sense == Optimisation.Sense.MIN) {
                TestUtils.assertTrue(description, bound <= optimum + tolerance);
            } else {
                TestUtils.assertTrue(description, bound >= optimum - tolerance);
            }
        }

        TestUtils.assertTrue("Stopped before completed: " + nbStopped, nbStopped > 5);
    }

    /**
     * A random knapsack
     */
    private static ExpressionsBasedModel newKnapsack() {

        Random random = new Random(5);

        ExpressionsBasedModel model = new ExpressionsBasedModel();
        Expression weight = model.newExpression("weight").upper(400);
        Expression volume = model.newExpression("volume").upper(350);
        for (int i = 0; i < 25; i++) {
            Variable item = model.newVariable("x" + i).binary().weight(20 + random.nextInt(80));
            weight.set(item, 10 + random.nextInt(60));
            volume.set(item, 10 + random.nextInt(60));
        }

        return model;
    }

    /**
     * A small random model: a knapsack to maximise, or a covering problem to minimise
     */
    private static ExpressionsBasedModel newRandom(final long seed, final Optimisation.Sense sense) {

        Random random = new Random(seed);

        int nbItems = 8 + random.nextInt(8);
        int nbRows = 1 + random.nextInt(3);

        ExpressionsBasedModel model = new ExpressionsBasedModel();
        Expression[] rows = new Expression[nbRows];
        int[] totals = new int[nbRows];
        for (int r = 0; r < nbRows; r++) {
            rows[r] = model.newExpression("row" + r);
        }
        for (int i = 0; i < nbItems; i++) {
            Variable item = model.newVariable("x" + i).binary().weight(10 + random.nextInt(90));
            for (int r = 0; r < nbRows; r++) {
                int coefficient = 5 + random.nextInt(60);
                totals[r] += coefficient;
                rows[r].set(item, coefficient);
            }
        }
        for (int r = 0; r < nbRows; r++) {
            int limit = totals[r] * (30 + random.nextInt(40)) / 100;
            if (sense == Optimisation.Sense.MAX) {
                rows[r].upper(limit);
            } else {
                rows[r].lower(limit);
            }
        }

        return model;
    }

    /**
     * Solves, and stops after {@code nodeLimit} node problems (0 is no limit)
     *
     * @return The last dual bound of the log, NaN if there wasn't one, or it wasn't a number
     */
    private static double solve(final ExpressionsBasedModel model, final Optimisation.Sense sense, final int nodeLimit, final int nbWorkers,
            final IntegerStrategy.ConfigurableStrategy strategy, final Optimisation.Result[] result) {

        StringWriter log = new StringWriter();
        BasicLogger.BasicWriter appender = new BasicLogger.BasicWriter(log);

        model.options.parallelism(nbWorkers);
        model.options.progress(IntegerSolver.class);
        model.options.logger_appender = appender;

        AtomicInteger count = new AtomicInteger();
        if (nodeLimit > 0) {
            model.options.integer(strategy.withModelStrategyFactory((m, s) -> new Stopper(m, s, model.options, nodeLimit, count)));
        } else {
            model.options.integer(strategy);
        }

        result[0] = sense == Optimisation.Sense.MIN ? model.minimise() : model.maximise();

        appender.println(); // Anything left in the appender's buffer ends up in the log

        double retVal = Double.NaN;
        Matcher matcher = DUAL_BOUND.matcher(log.toString());
        while (matcher.find()) {
            try {
                retVal = Double.parseDouble(matcher.group(1));
            } catch (NumberFormatException cause) {
                retVal = Double.NaN;
            }
        }
        return retVal;
    }

    @Test
    public void testDepthFirst() {
        // No worker uses a queue ordered by objective value
        FinalDualBoundTest.doTestStopped(false, Optimisation.Sense.MIN, 7615.0, 1, IntegerStrategy.DEFAULT.withPriorityDefinitions(NodeKey.DEPTH_FIRST_SEARCH));
    }

    @Test
    public void testMaximisation() {

        Optimisation.Result[] result = new Optimisation.Result[1];
        FinalDualBoundTest.solve(FinalDualBoundTest.newKnapsack(), Optimisation.Sense.MAX, 0, 1, IntegerStrategy.DEFAULT, result);
        TestUtils.assertStateNotLessThanOptimal(result[0]);

        FinalDualBoundTest.doTestStopped(true, Optimisation.Sense.MAX, result[0].getValue(), 1, IntegerStrategy.DEFAULT);
    }

    @Test
    public void testMinimisation() {
        FinalDualBoundTest.doTestStopped(false, Optimisation.Sense.MIN, 7615.0, 1, IntegerStrategy.DEFAULT);
    }

    /**
     * When completed the bound is the optimal value
     */
    @Test
    public void testOptimal() {

        Optimisation.Result[] result = new Optimisation.Result[1];

        double bound = FinalDualBoundTest.solve(ModelFileTest.makeModel("MIPLIB", "p0201.mps", false), Optimisation.Sense.MIN, 0, 1, IntegerStrategy.DEFAULT,
                result);

        TestUtils.assertStateNotLessThanOptimal(result[0]);
        TestUtils.assertEquals(7615.0, result[0].getValue(), 1.0E-6);
        TestUtils.assertEquals(7615.0, bound, 1.0E-6);
    }

    @Test
    public void testRandomMaximisation() {
        FinalDualBoundTest.doTestRandom(Optimisation.Sense.MAX, 1);
    }

    @Test
    public void testRandomMinimisation() {
        FinalDualBoundTest.doTestRandom(Optimisation.Sense.MIN, 1);
    }

    @Test
    public void testRandomSeveralWorkers() {
        FinalDualBoundTest.doTestRandom(Optimisation.Sense.MIN, 3);
    }

    @Test
    public void testSeveralWorkers() {
        FinalDualBoundTest.doTestStopped(false, Optimisation.Sense.MIN, 7615.0, 3, IntegerStrategy.DEFAULT);
    }

}

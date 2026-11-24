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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;

/**
 * How often {@link ModelStrategy.DefaultStrategy} attempts to generate cuts: at every opportunity to begin
 * with, and less often (the interval is doubled) after each attempt that isn't useful. Never turned off.
 */
public class CutBackOffTest extends OptimisationIntegerTests {

    private static ModelStrategy.DefaultStrategy newStrategy(final Optimisation.Sense sense) {

        ExpressionsBasedModel model = new ExpressionsBasedModel();
        model.newVariable("x").binary().weight(1);
        model.newVariable("y").binary().weight(1);
        model.prepare(sense, NodeSolver::new);

        ModelStrategy.DefaultStrategy retVal = new ModelStrategy.DefaultStrategy(model, IntegerStrategy.DEFAULT);
        retVal.initialise();

        return retVal;
    }

    private static NodeKey newRoot() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();
        model.newVariable("x").binary().weight(1);
        model.newVariable("y").binary().weight(1);

        return new NodeKey(model);
    }

    /**
     * Is this opportunity used to attempt to generate cuts?
     */
    private static boolean isAttempt(final ModelStrategy strategy, final NodeKey key, final double nodeValue) {
        return strategy.isCutRatherThanBranch(key, 0, 0.5, nodeValue, null);
    }

    /**
     * An attempt that adds cuts, after which the node is solved again
     */
    private static void attemptWithCuts(final ModelStrategy strategy, final NodeKey key, final double valueBefore, final double valueAfter,
            final boolean minimisation) {
        TestUtils.assertTrue(CutBackOffTest.isAttempt(strategy, key, valueBefore));
        strategy.onCutSuccess(key);
        strategy.onNodeSolved(key, null, valueAfter, minimisation);
    }

    /**
     * An attempt without cuts
     */
    private static void attemptWithoutCuts(final ModelStrategy strategy, final NodeKey key) {
        TestUtils.assertTrue(CutBackOffTest.isAttempt(strategy, key, 10.0));
        strategy.onCutFailure();
    }

    /**
     * Skips the opportunities until the next attempt, and returns how many that was
     */
    private static int skips(final ModelStrategy strategy, final NodeKey key) {
        int retVal = 0;
        while (!CutBackOffTest.isAttempt(strategy, key, 10.0)) {
            retVal++;
        }
        strategy.onCutFailure();
        return retVal;
    }

    @Test
    public void testDoubledUpToTheLimitAndNeverTurnedOff() {

        ModelStrategy strategy = CutBackOffTest.newStrategy(Optimisation.Sense.MIN);
        NodeKey key = CutBackOffTest.newRoot();

        List<Integer> attempts = new ArrayList<>();
        for (int opportunity = 1; opportunity <= 10_000; opportunity++) {
            if (CutBackOffTest.isAttempt(strategy, key, 10.0)) {
                attempts.add(Integer.valueOf(opportunity));
                strategy.onCutFailure();
            }
        }

        // The intervals are doubled: 1, 2, 4... up to 1024, and stay there
        TestUtils.assertEquals(Arrays.asList(1, 3, 7, 15, 31, 63, 127, 255, 511, 1023, 2047, 3071, 4095, 5119, 6143, 7167, 8191, 9215), attempts);
    }

    /**
     * Cuts that were not enough to be useful count as a failure, cuts that moved the bound start over
     */
    @Test
    public void testImprovedBoundStartsOver() {

        for (Optimisation.Sense sense : new Optimisation.Sense[] { Optimisation.Sense.MIN, Optimisation.Sense.MAX }) {

            boolean minimisation = sense == Optimisation.Sense.MIN;
            double better = minimisation ? 1.0 : -1.0;

            ModelStrategy strategy = CutBackOffTest.newStrategy(sense);
            NodeKey key = CutBackOffTest.newRoot();

            CutBackOffTest.attemptWithoutCuts(strategy, key); // Interval 2
            TestUtils.assertEquals(1, CutBackOffTest.skips(strategy, key)); // Interval 4
            TestUtils.assertEquals(3, CutBackOffTest.skips(strategy, key)); // Interval 8

            for (int i = 0; i < 7; i++) {
                TestUtils.assertFalse(CutBackOffTest.isAttempt(strategy, key, 1000.0));
            }

            // Cuts that move the bound by 1 in 1000 (0.001 relative)
            CutBackOffTest.attemptWithCuts(strategy, key, 1000.0, 1000.0 + better, minimisation);
            TestUtils.assertEquals(0, CutBackOffTest.skips(strategy, key)); // At every opportunity again

            // Back off again: 1, 2...
            TestUtils.assertEquals(1, CutBackOffTest.skips(strategy, key));
        }
    }

    /**
     * Cuts that make the node infeasible are as useful as can be
     */
    @Test
    public void testInfeasibleStartsOver() {

        ModelStrategy strategy = CutBackOffTest.newStrategy(Optimisation.Sense.MIN);
        NodeKey key = CutBackOffTest.newRoot();

        CutBackOffTest.attemptWithoutCuts(strategy, key);
        TestUtils.assertEquals(1, CutBackOffTest.skips(strategy, key));

        for (int i = 0; i < 3; i++) {
            TestUtils.assertFalse(CutBackOffTest.isAttempt(strategy, key, 10.0));
        }

        TestUtils.assertTrue(CutBackOffTest.isAttempt(strategy, key, 10.0));
        strategy.onCutSuccess(key);
        strategy.markInfeasible(key, false, Double.NaN);

        TestUtils.assertEquals(0, CutBackOffTest.skips(strategy, key));
    }

    /**
     * Adding cuts is not enough: when the bound doesn't move, it's as if there were none
     */
    @Test
    public void testUnchangedBoundIsAFailure() {

        ModelStrategy strategy = CutBackOffTest.newStrategy(Optimisation.Sense.MIN);
        NodeKey key = CutBackOffTest.newRoot();

        // 1e-5 relative is too little
        CutBackOffTest.attemptWithCuts(strategy, key, 1000.0, 1000.01, true); // Interval 2
        TestUtils.assertEquals(1, CutBackOffTest.skips(strategy, key)); // Interval 4

        // A bound that moved the wrong way (or not at all) isn't improved either
        for (int i = 0; i < 3; i++) {
            TestUtils.assertFalse(CutBackOffTest.isAttempt(strategy, key, 1000.0));
        }
        CutBackOffTest.attemptWithCuts(strategy, key, 1000.0, 999.0, true); // Interval 8
        TestUtils.assertEquals(7, CutBackOffTest.skips(strategy, key));
    }

    /**
     * The cuts of one node are not judged by the bound of another
     */
    @Test
    public void testAnotherNodeIsNotTheOne() {

        ModelStrategy strategy = CutBackOffTest.newStrategy(Optimisation.Sense.MIN);
        NodeKey key = CutBackOffTest.newRoot();
        NodeKey other = key.createLowerBranch(0, 0.5, 10.0);

        TestUtils.assertTrue(CutBackOffTest.isAttempt(strategy, key, 1000.0));
        strategy.onCutSuccess(key);
        strategy.onNodeSolved(other, null, 2000.0, true); // The wrong node, ignored

        // Neither a useful nor a useless attempt, as before: at every opportunity
        TestUtils.assertEquals(0, CutBackOffTest.skips(strategy, key));
    }

    /**
     * Each solve starts with an attempt at every opportunity
     */
    @Test
    public void testInitialise() {

        ModelStrategy strategy = CutBackOffTest.newStrategy(Optimisation.Sense.MIN);
        NodeKey key = CutBackOffTest.newRoot();

        for (int i = 0; i < 5; i++) {
            CutBackOffTest.skips(strategy, key);
        }
        TestUtils.assertFalse(CutBackOffTest.isAttempt(strategy, key, 10.0));

        strategy.initialise();

        TestUtils.assertEquals(0, CutBackOffTest.skips(strategy, key));
    }

}

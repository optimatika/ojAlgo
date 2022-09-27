package org.ojalgo.optimisation.integer;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.optimisation.Variable;

/**
 * Tests based on TheMcNuggetsChallenge example code. Cases that have been problematic at some point.
 */
public class TestMcNuggetsChallenge extends OptimisationIntegerTests {

    static void doTest(final int... count) {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable numberOfPack6 = model.addVariable("#6-packs").lower(0).integer(true);
        Variable numberOfPack9 = model.addVariable("#9-packs").lower(0).integer(true);
        Variable numberOfPack20 = model.addVariable("#20-packs").lower(0).integer(true);

        Expression totalNuggetsOrdered = model.addExpression().weight(1);
        totalNuggetsOrdered.set(numberOfPack6, 6);
        totalNuggetsOrdered.set(numberOfPack9, 9);
        totalNuggetsOrdered.set(numberOfPack20, 20);

        if (DEBUG) {
            model.options.debug(Optimisation.Solver.class);
        }

        for (int i = 0; i < count.length; i++) {

            if (DEBUG) {
                BasicLogger.debug();
                BasicLogger.debug();
                BasicLogger.debug();
                BasicLogger.debug("McNuggets: {}", count[i]);
                BasicLogger.debug("=========================================");
                BasicLogger.debug();
            }

            if (count[i] == 83) {
                BasicLogger.debug();
            }

            totalNuggetsOrdered.upper(count[i]);
            Result result = model.maximise();

            if (DEBUG) {
                BasicLogger.debug();
                BasicLogger.debug(result);
                BasicLogger.debug();
            }

            TestUtils.assertSolutionFeasible(model, result);

            TestUtils.assertEquals(count[i], Math.round(result.getValue()));
        }
    }

    @Test
    public void testCase52() {
        TestMcNuggetsChallenge.doTest(53, 52);
    }

    @Test
    public void testCase63() {
        TestMcNuggetsChallenge.doTest(64, 63);
    }

    @Test
    public void testCase83() {
        TestMcNuggetsChallenge.doTest(84, 83);
    }

}

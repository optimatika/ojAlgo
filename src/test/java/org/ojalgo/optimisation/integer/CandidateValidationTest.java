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

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.ModelFileTest;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.type.context.NumberContext;

/**
 * An integer candidate carrying LP noise (integer variables at 1 + 1e-11, rows off their limits by 1e-11) must
 * not be discarded by the feasibility validation. With cut rounds at every node the LP noise grows, and on
 * 22433 the optimal candidate (LP value 21477) was rejected, the node closed, and 21561 returned.
 */
public class CandidateValidationTest extends OptimisationIntegerTests {

    /**
     * Cuts at every node down to depth 20, regardless of empty rounds. Deterministically reproduced the
     * discarded optimum before the fix.
     */
    static final class CutEverywhere extends ModelStrategy.AbstractStrategy {

        CutEverywhere(final ExpressionsBasedModel model, final IntegerStrategy strategy) {
            super(model, strategy);
        }

        @Override
        protected void initialise() {
            delegate.initialise();
        }

        @Override
        protected boolean isCutRatherThanBranch(final NodeKey nodeKey, final int branchIntegerIndex, final double variableValue, final double nodeValue,
                final Optimisation.Result bestResultSoFar) {
            return nodeKey.depth <= 20;
        }

        @Override
        protected void markInfeasible(final NodeKey key, final boolean found, final double incumbentValue) {
            delegate.markInfeasible(key, found, incumbentValue);
        }

        @Override
        protected void markInteger(final NodeKey key, final Optimisation.Result result) {
            delegate.markInteger(key, result);
        }

        @Override
        protected void observeBranch(final int idx, final boolean upper, final double observation) {
            delegate.observeBranch(idx, upper, observation);
        }

        @Override
        protected void onCutFailure() {
            delegate.onCutFailure();
        }

        @Override
        protected void onCutSuccess(final NodeKey nodeKey) {
            delegate.onCutSuccess(nodeKey);
        }

        @Override
        protected void onNodeSolved(final NodeKey key, final Optimisation.Result child, final double childObj, final boolean minimisation) {
            delegate.onNodeSolved(key, child, childObj, minimisation);
        }

        @Override
        protected double scoreBranch(final int idx, final double distanceDown, final double distanceUp, final boolean found) {
            return delegate.scoreBranch(idx, distanceDown, distanceUp, found);
        }

    }

    @Test
    public void test22433CutEverywhere() {

        ExpressionsBasedModel model = ModelFileTest.makeModel("MIPLIB", "22433.mps", false);

        model.options.integer(IntegerStrategy.DEFAULT.withModelStrategyFactory(CutEverywhere::new));

        Optimisation.Result result = model.minimise();

        TestUtils.assertTrue(result.getState().isOptimal());
        TestUtils.assertEquals(21477.0, result.getValue(), NumberContext.of(7));
        // The returned point carries LP noise (1e-11); the model's default feasibility context (12 significant
        // digits) is stricter than that, so validate to the accuracy the LP actually delivers
        TestUtils.assertTrue(model.validate(result, NumberContext.of(8), BasicLogger.NULL));
    }

}

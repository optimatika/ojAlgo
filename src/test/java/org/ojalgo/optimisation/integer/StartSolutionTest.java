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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.type.context.NumberContext;

/**
 * Start values set with {@link Variable#setValue(Comparable)} must become the initial incumbent when they
 * form a feasible integer solution, and must be ignored otherwise. Since v57.0.0 the start reached the solver
 * with state APPROXIMATE (the model's variable values are no longer validated when extracted), and the solver
 * only accepted a FEASIBLE start, so a valid start was always ignored. On MIPLIB p0548, given the optimum as
 * start, the solver then found no integer solution at all within its time limit.
 * <p>
 * The incumbent is observed through the strategy: the start is the only integer solution marked without a
 * node key.
 */
public class StartSolutionTest extends OptimisationIntegerTests {

    /**
     * Delegates everything, and records the integer solutions marked without a node key.
     */
    static final class StartRecorder extends ModelStrategy.AbstractStrategy {

        private final List<Optimisation.Result> myStarts;

        StartRecorder(final ExpressionsBasedModel model, final IntegerStrategy strategy, final List<Optimisation.Result> starts) {
            super(model, strategy);
            myStarts = starts;
        }

        @Override
        protected void initialise() {
            delegate.initialise();
        }

        @Override
        protected boolean isCutRatherThanBranch(final NodeKey nodeKey, final int branchIntegerIndex, final double variableValue, final double nodeValue,
                final Optimisation.Result bestResultSoFar) {
            return delegate.isCutRatherThanBranch(nodeKey, branchIntegerIndex, variableValue, nodeValue, bestResultSoFar);
        }

        @Override
        protected void markInfeasible(final NodeKey key, final boolean found, final double incumbentValue) {
            delegate.markInfeasible(key, found, incumbentValue);
        }

        @Override
        protected void markInteger(final NodeKey key, final Optimisation.Result result) {
            if (key == null) {
                myStarts.add(result);
            }
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

    /**
     * max 5x + 4y + 3z subject to 2x + 3y + z <= 5, 4x + y + 2z <= 11, 3x + 4y + 2z <= 8, with x, y, z
     * non-negative integers. The optimum is x = 2, y = 0, z = 1 with value 13.
     */
    private static ExpressionsBasedModel newModel(final List<Optimisation.Result> starts, final double startX, final double startY, final double startZ) {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x = model.newVariable("x").lower(0).weight(5).integer();
        Variable y = model.newVariable("y").lower(0).weight(4).integer();
        Variable z = model.newVariable("z").lower(0).weight(3).integer();

        Expression c1 = model.newExpression("c1").upper(5);
        c1.set(x, 2);
        c1.set(y, 3);
        c1.set(z, 1);

        Expression c2 = model.newExpression("c2").upper(11);
        c2.set(x, 4);
        c2.set(y, 1);
        c2.set(z, 2);

        Expression c3 = model.newExpression("c3").upper(8);
        c3.set(x, 3);
        c3.set(y, 4);
        c3.set(z, 2);

        x.setValue(startX);
        y.setValue(startY);
        z.setValue(startZ);

        model.options.integer(IntegerStrategy.DEFAULT.withModelStrategyFactory((m, s) -> new StartRecorder(m, s, starts)));

        return model;
    }

    @Test
    public void testFeasibleStartIsIncumbent() {

        List<Optimisation.Result> starts = new CopyOnWriteArrayList<>();

        Optimisation.Result result = StartSolutionTest.newModel(starts, 1, 0, 1).maximise();

        TestUtils.assertEquals(1, starts.size());
        TestUtils.assertEquals(8.0, starts.get(0).getValue(), NumberContext.of(12));

        TestUtils.assertTrue(result.getState().isOptimal());
        TestUtils.assertEquals(13.0, result.getValue(), NumberContext.of(12));
    }

    @Test
    public void testFractionalStartIsIgnored() {

        List<Optimisation.Result> starts = new CopyOnWriteArrayList<>();

        Optimisation.Result result = StartSolutionTest.newModel(starts, 1.5, 0, 1).maximise();

        TestUtils.assertEquals(0, starts.size());

        TestUtils.assertTrue(result.getState().isOptimal());
        TestUtils.assertEquals(13.0, result.getValue(), NumberContext.of(12));
    }

    @Test
    public void testInfeasibleStartIsIgnored() {

        List<Optimisation.Result> starts = new CopyOnWriteArrayList<>();

        Optimisation.Result result = StartSolutionTest.newModel(starts, 2, 1, 1).maximise();

        TestUtils.assertEquals(0, starts.size());

        TestUtils.assertTrue(result.getState().isOptimal());
        TestUtils.assertEquals(13.0, result.getValue(), NumberContext.of(12));
    }

}

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

import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.ModelFileTest;
import org.ojalgo.optimisation.Optimisation;

/**
 * With one worker, the same model is searched the same way however many times it is solved in a JVM. The cuts
 * are named by a counter, and the model keeps its expressions in a hash map by name, so the counter decides
 * the order of the LP rows – and with degenerate LPs the vertex found, the branching and the whole search.
 * That counter, and the node sequence numbers, were static: each solve continued where the previous one
 * ended, and p0282 needed 354, 673 and 982 nodes in three consecutive solves.
 */
public class ReproducibleSearchTest extends OptimisationIntegerTests {

    static final class NodeCounter extends ModelStrategy.AbstractStrategy {

        private final AtomicLong myCount;

        NodeCounter(final ExpressionsBasedModel model, final IntegerStrategy strategy, final AtomicLong count) {
            super(model, strategy);
            myCount = count;
        }

        @Override
        protected void onNodeSolved(final NodeKey key, final Optimisation.Result child, final double childObj, final boolean minimisation) {
            myCount.incrementAndGet();
            super.onNodeSolved(key, child, childObj, minimisation);
        }

    }

    /**
     * Solves the model with one worker and returns the number of nodes solved
     */
    private static long solve(final String name, final double optimal) {

        ExpressionsBasedModel model = ModelFileTest.makeModel("MIPLIB", name, false);

        AtomicLong count = new AtomicLong();
        model.options.parallelism(1);
        model.options.integer(IntegerStrategy.DEFAULT.withModelStrategyFactory((m, s) -> new NodeCounter(m, s, count)));

        Optimisation.Result result = model.minimise();

        TestUtils.assertStateNotLessThanOptimal(result);
        TestUtils.assertEquals(optimal, result.getValue(), 1E-6);

        return count.get();
    }

    @Test
    void testRepeatedSolve() {

        long first = ReproducibleSearchTest.solve("p0282.mps", 258411.0);
        ReproducibleSearchTest.solve("lseu.mps", 1120.0);
        long again = ReproducibleSearchTest.solve("p0282.mps", 258411.0);

        TestUtils.assertEquals(first, again);
    }

}

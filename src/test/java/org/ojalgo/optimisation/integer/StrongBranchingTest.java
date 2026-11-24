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

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;

/**
 * The strong-branching probes at the root change the bounds of the integer variables, one at a time, and then
 * restore them, unless a probe shows that a bound can be tightened. Whether the variables have limits or not,
 * the value of the root LP is a valid bound afterwards. Without limits it wasn't: it was NaN, and the model
 * (that has a limited optimal value) could be reported UNBOUNDED.
 */
public class StrongBranchingTest extends OptimisationIntegerTests {

    /**
     * Keeps the value of the first node – the root – and stops the search there, as if the time is up
     */
    private static final class RootValue extends ModelStrategy.AbstractStrategy {

        private final Optimisation.Options myOptions;
        private final double[] myValue;

        RootValue(final ExpressionsBasedModel model, final IntegerStrategy strategy, final Optimisation.Options options, final double[] value) {
            super(model, strategy);
            myOptions = options;
            myValue = value;
        }

        @Override
        protected void onNodeSolved(final NodeKey key, final Optimisation.Result child, final double childObj, final boolean minimisation) {
            super.onNodeSolved(key, child, childObj, minimisation);
            if (key.sequence == 0L) {
                myValue[0] = childObj;
            }
            myOptions.time_abort = 0L;
        }

    }

    private static final int NB_ITEMS = 3;
    private static final int NB_KNAPSACKS = 40;

    /**
     * Independent knapsacks, with any number of each item
     */
    private static ExpressionsBasedModel newModel(final int[][] weights, final int[][] values, final int[] capacities, final boolean limited,
            final boolean negated) {

        int sign = negated ? -1 : 1;

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        for (int k = 0; k < NB_KNAPSACKS; k++) {
            Expression row = model.newExpression("row" + k).upper(capacities[k]);
            for (int i = 0; i < NB_ITEMS; i++) {
                Variable variable = model.newVariable("x" + k + "_" + i).integer().weight(sign * values[k][i]);
                if (negated) {
                    variable.upper(0);
                } else {
                    variable.lower(0);
                }
                if (limited && negated) {
                    variable.lower(-1000);
                } else if (limited) {
                    variable.upper(1000);
                }
                row.set(variable, sign * weights[k][i]);
            }
        }

        return model;
    }

    private static int optimum(final int[] weights, final int[] values, final int capacity) {
        int[] best = new int[capacity + 1];
        for (int c = 0; c <= capacity; c++) {
            for (int i = 0; i < weights.length; i++) {
                if (weights[i] <= c) {
                    best[c] = Math.max(best[c], best[c - weights[i]] + values[i]);
                }
            }
        }
        return best[capacity];
    }

    @Test
    public void testRootAfterProbes() {

        for (long seed = 1L; seed <= 10L; seed++) {

            Random random = new Random(seed);

            int[][] weights = new int[NB_KNAPSACKS][NB_ITEMS];
            int[][] values = new int[NB_KNAPSACKS][NB_ITEMS];
            int[] capacities = new int[NB_KNAPSACKS];
            double optimum = 0.0;
            for (int k = 0; k < NB_KNAPSACKS; k++) {
                for (int i = 0; i < NB_ITEMS; i++) {
                    weights[k][i] = 3 + random.nextInt(12);
                    values[k][i] = 5 + random.nextInt(20);
                }
                capacities[k] = 10 + random.nextInt(30);
                optimum += StrongBranchingTest.optimum(weights[k], values[k], capacities[k]);
            }

            for (boolean limited : new boolean[] { true, false }) {
                for (boolean negated : new boolean[] { true, false }) {

                    double relaxation = StrongBranchingTest.newModel(weights, values, capacities, limited, negated).snapshot().maximise().getValue();

                    double[] root = { Double.NaN };

                    ExpressionsBasedModel model = StrongBranchingTest.newModel(weights, values, capacities, limited, negated);
                    model.options.parallelism(1);
                    // No cuts, as with those there are too few fractional variables left for any probes
                    model.options.integer(IntegerStrategy.DEFAULT.withCutConfiguration(IntegerStrategy.DEFAULT.getCutConfiguration().withTypes())
                            .withModelStrategyFactory((m, s) -> new RootValue(m, s, model.options, root)));

                    Optimisation.Result result = model.maximise();

                    String description = "Seed " + seed + ", limited " + limited + ", negated " + negated + ": " + optimum + " <= " + root[0] + " <= "
                            + relaxation;

                    TestUtils.assertTrue(description, result.getState() != Optimisation.State.UNBOUNDED);
                    TestUtils.assertTrue(description, optimum <= root[0] + 1E-6 && root[0] <= relaxation + 1E-6);
                }
            }
        }
    }

}

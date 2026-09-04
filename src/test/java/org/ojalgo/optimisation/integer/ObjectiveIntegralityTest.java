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
import org.ojalgo.array.ArrayR064;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;

/**
 * Detection of an integral objective, and the lattice rounding used for pruning and optimality proofs.
 */
public class ObjectiveIntegralityTest extends OptimisationIntegerTests {

    private static ExpressionsBasedModel makeModel(final boolean integerOnly, final Optimisation.Sense sense) {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x = model.newVariable("X").integer().lower(0).upper(10).weight(3);
        Variable y = model.newVariable("Y").integer().lower(0).upper(10).weight(-2);
        Variable z = model.newVariable("Z").lower(0).upper(10);
        if (!integerOnly) {
            z.weight(1);
        }

        model.newExpression("C").set(x, 1).set(y, 1).set(z, 1).lower(1);

        // Solving sets the model's optimisation sense
        if (sense == Optimisation.Sense.MAX) {
            model.maximise();
        } else {
            model.minimise();
        }

        return model;
    }

    @Test
    public void testContinuousVariableInObjective() {

        ExpressionsBasedModel model = ObjectiveIntegralityTest.makeModel(false, Optimisation.Sense.MIN);
        ModelStrategy strategy = IntegerStrategy.DEFAULT.newModelStrategy(model);

        TestUtils.assertFalse(strategy.isObjectiveIntegral());
        TestUtils.assertEquals(0.0, strategy.getObjectiveLatticeUnit());
        TestUtils.assertEquals(7.3, strategy.toLatticeBound(7.3));
        TestUtils.assertEquals(7.3, strategy.toLatticeValue(7.3));
    }

    @Test
    public void testIntegerCoefficientsMax() {

        ExpressionsBasedModel model = ObjectiveIntegralityTest.makeModel(true, Optimisation.Sense.MAX);
        ModelStrategy strategy = IntegerStrategy.DEFAULT.newModelStrategy(model);

        TestUtils.assertTrue(strategy.isObjectiveIntegral());
        TestUtils.assertEquals(1.0, strategy.getObjectiveLatticeUnit());

        TestUtils.assertEquals(7.0, strategy.toLatticeBound(7.3));
        TestUtils.assertEquals(7.0, strategy.toLatticeBound(7.0 - 1E-9));
        TestUtils.assertEquals(7.0, strategy.toLatticeBound(7.0));
        TestUtils.assertEquals(-8.0, strategy.toLatticeBound(-7.3));

        TestUtils.assertEquals(7.0, strategy.toLatticeValue(7.0 + 1E-9));
        TestUtils.assertEquals(7.0, strategy.toLatticeValue(7.0 - 1E-9));
    }

    @Test
    public void testIntegerCoefficientsMin() {

        ExpressionsBasedModel model = ObjectiveIntegralityTest.makeModel(true, Optimisation.Sense.MIN);
        ModelStrategy strategy = IntegerStrategy.DEFAULT.newModelStrategy(model);

        TestUtils.assertTrue(strategy.isObjectiveIntegral());
        TestUtils.assertEquals(1.0, strategy.getObjectiveLatticeUnit());

        TestUtils.assertEquals(8.0, strategy.toLatticeBound(7.3));
        TestUtils.assertEquals(7.0, strategy.toLatticeBound(7.0 + 1E-9));
        TestUtils.assertEquals(7.0, strategy.toLatticeBound(7.0));
        TestUtils.assertEquals(-7.0, strategy.toLatticeBound(-7.3));

        TestUtils.assertEquals(Double.POSITIVE_INFINITY, strategy.toLatticeBound(Double.POSITIVE_INFINITY));

        TestUtils.assertEquals(7.0, strategy.toLatticeValue(7.0 + 1E-9));
        TestUtils.assertEquals(7.0, strategy.toLatticeValue(7.0 - 1E-9));
    }

    /**
     * A node whose relaxation cannot reach a strictly better lattice value must be pruned, one that can must
     * not be, and an incumbent with LP noise must not confuse either.
     */
    @Test
    public void testPruning() {

        ExpressionsBasedModel model = ObjectiveIntegralityTest.makeModel(true, Optimisation.Sense.MIN);
        ModelStrategy strategy = IntegerStrategy.DEFAULT.newModelStrategy(model);

        Optimisation.Result incumbent = new Optimisation.Result(Optimisation.State.FEASIBLE, 7.0 + 1E-9, ArrayR064.wrap(new double[] { 1, 0, 0 }));

        TestUtils.assertFalse(strategy.isGoodEnough(incumbent, 6.4));
        TestUtils.assertFalse(strategy.isGoodEnough(incumbent, 6.0 + 1E-5));
        TestUtils.assertTrue(strategy.isGoodEnough(incumbent, 6.0 + 1E-9));
        TestUtils.assertTrue(strategy.isGoodEnough(incumbent, 6.0 - 1E-9));
        TestUtils.assertTrue(strategy.isGoodEnough(incumbent, 5.6));
    }

    @Test
    public void testScaledCoefficients() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x = model.newVariable("X").integer().lower(0).upper(10).weight(0.5);
        Variable y = model.newVariable("Y").integer().lower(0).upper(10).weight(0.25);
        // A fixed integer variable in the objective only shifts the attainable values by whole steps
        model.newVariable("K").integer().level(3).weight(7);
        model.newExpression("C").set(x, 1).set(y, 1).lower(1);

        model.minimise();

        ModelStrategy strategy = IntegerStrategy.DEFAULT.newModelStrategy(model);

        TestUtils.assertTrue(strategy.isObjectiveIntegral());
        TestUtils.assertEquals(0.25, strategy.getObjectiveLatticeUnit(), 1E-12);

        // Attainable values are k * 0.25
        TestUtils.assertEquals(21.5, strategy.toLatticeBound(21.302), 1E-12);
        TestUtils.assertEquals(21.25, strategy.toLatticeBound(21.25 + 1E-9), 1E-12);
        TestUtils.assertEquals(21.25, strategy.toLatticeValue(21.25 - 1E-9), 1E-12);

        // Closed when the bound rounds up to the incumbent itself
        TestUtils.assertTrue(strategy.isLatticeGapClosed(21.5, 21.3));
        TestUtils.assertTrue(strategy.isLatticeGapClosed(21.5 + 1E-9, 21.5 - 1E-9));
        // A bound at (or noise-close to) the next lower attainable value may still hold a better solution
        TestUtils.assertFalse(strategy.isLatticeGapClosed(21.5 + 1E-9, 21.25 + 1E-9));
        TestUtils.assertFalse(strategy.isLatticeGapClosed(21.5, 21.2));
    }

}

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
package org.ojalgo.optimisation.convex;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.ExpressionsBasedModel.Integration;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.optimisation.Optimisation.State;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.type.context.NumberContext;

/**
 * When [Q] is not positive semidefinite, not even within the tolerance (sqrt(smallDiagonal) * max|Q|, about
 * 1.2E-4 relative), the problem is not convex. The active set solvers still solve it, on a best effort basis,
 * but a solution is only a stationary point - it's reported as FEASIBLE, never OPTIMAL. Without constraints
 * the negative curvature makes the problem unbounded. Negative curvature within the tolerance, typically
 * rounding in the data, is accepted - those problems are solved as convex ones.
 * <p>
 * These used to be reported as OPTIMAL (or DISTINCT) at a saddle point or a maximum, and every
 * re-initialisation of the solver added the patch to [Q] again.
 */
public class IndefiniteHessianTest extends OptimisationConvexTests {

    private static final Integration<?>[] ACTIVE_SET = { ConfiguredIntegration.ASET_PLAIN_D, ConfiguredIntegration.ASET_PLAIN_S,
            ConfiguredIntegration.ASET_PROJ_D, ConfiguredIntegration.ASET_PROJ_S };

    /**
     * min -x<sup>2</sup> - y<sup>2</sup> + 0.1 x + 0.1 y when x + y <= 1.5 and 0 <= x, y <= 1. The stationary
     * point x = y = 0.05 is the maximum, the minimum is at x = 1 and y = 0.5 (or the other way around).
     */
    private static ExpressionsBasedModel newBoxConstrainedModel() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x = model.newVariable("x").lower(0).upper(1);
        Variable y = model.newVariable("y").lower(0).upper(1);

        model.newExpression("objective").weight(1).set(x, x, -1).set(x, 0.1).set(y, y, -1).set(y, 0.1);
        model.newExpression("constraint").set(x, 1).set(y, 1).upper(1.5);

        return model;
    }

    @Test
    public void testBoxConstrained() {

        for (int v = 0; v < ACTIVE_SET.length; v++) {

            ExpressionsBasedModel model = IndefiniteHessianTest.newBoxConstrainedModel();

            Result result = model.minimise(ACTIVE_SET[v]);

            TestUtils.assertFalse("ACTIVE_SET[" + v + "] " + result.getState(), result.getState().isOptimal());
            if (result.getState().isFeasible()) {
                TestUtils.assertTrue("ACTIVE_SET[" + v + "]", model.validate(result));
            }
        }
    }

    /**
     * The patch is added to [Q] once - solving (and re-initialising) again does not add it again.
     */
    @Test
    public void testPatchedOnce() {

        Optimisation.Options options = new Optimisation.Options();

        ConvexData<Double> data = ConvexSolver.copy(IndefiniteHessianTest.newBoxConstrainedModel(), R064Store.FACTORY);

        PhysicalStore<Double> mtrxQ = data.getObjective().quadratic();
        PhysicalStore<Double> original = mtrxQ.copy();
        double patch = options.convex().smallDiagonal() * original.aggregateAll(Aggregator.LARGEST).doubleValue();

        BasePrimitiveSolver solver = BasePrimitiveSolver.newSolver(data, options);

        solver.solve();
        solver.solve();

        TestUtils.assertTrue(solver.isIndefiniteQ());

        for (int i = 0; i < mtrxQ.countRows(); i++) {
            TestUtils.assertEquals(original.doubleValue(i, i) + patch, mtrxQ.doubleValue(i, i), NumberContext.of(12));
        }
    }

    /**
     * min 1/2 x<sup>2</sup> - 1/2 y<sup>2</sup> - x, without constraints, is unbounded (along y).
     */
    @Test
    public void testUnconstrained() {

        for (int v = 0; v < ACTIVE_SET.length; v++) {

            ExpressionsBasedModel model = new ExpressionsBasedModel();

            Variable x = model.newVariable("x").weight(-1);
            Variable y = model.newVariable("y");

            model.newExpression("objective").weight(0.5).set(x, x, 1).set(y, y, -1);

            TestUtils.assertEquals("ACTIVE_SET[" + v + "]", State.UNBOUNDED, model.minimise(ACTIVE_SET[v]).getState());
        }
    }

    /**
     * min 1/2 x<sup>2</sup> - 1/2 1E-6 y<sup>2</sup> - 0.5 x - 0.1 y when 0 <= x, y <= 1. The negative
     * curvature, -1E-6 relative to max|Q|, is more than the patch but within the tolerance - the problem is
     * solved as a convex one, and the solution, x = 0.5 and y = 1, is OPTIMAL.
     */
    @Test
    public void testWithinTolerance() {

        for (int v = 0; v < ACTIVE_SET.length; v++) {

            String id = "ACTIVE_SET[" + v + "]";

            ExpressionsBasedModel model = new ExpressionsBasedModel();

            Variable x = model.newVariable("x").lower(0).upper(1);
            Variable y = model.newVariable("y").lower(0).upper(1);

            model.newExpression("objective").weight(1).set(x, x, 0.5).set(x, -0.5).set(y, y, -0.5E-6).set(y, -0.1);

            Result result = model.minimise(ACTIVE_SET[v]);

            TestUtils.assertStateNotLessThanOptimal(result);
            TestUtils.assertEquals(id, 0.5, result.doubleValue(0), NumberContext.of(6));
            TestUtils.assertEquals(id, 1.0, result.doubleValue(1), NumberContext.of(6));
        }
    }

}

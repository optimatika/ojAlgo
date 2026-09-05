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
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;

/**
 * Strong-branching style probing: solver-only bound updates, one of which makes the LP infeasible. The probes
 * after an infeasible one must still be solved correctly, and a solver-only bound must not get lost when the
 * solver is rebuilt.
 */
public class WarmRestartAfterInfeasibleTest extends OptimisationIntegerTests {

    @Test
    public void testProbeAfterInfeasibleProbe() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x = model.newVariable("X").integer().lower(0).upper(2).weight(1);
        Variable y = model.newVariable("Y").integer().lower(0).upper(2).weight(2);
        Variable z = model.newVariable("Z").integer().lower(0).upper(2).weight(3);

        model.newExpression("C1").set(x, 1).set(y, 1).lower(3);
        model.newExpression("C2").set(y, 1).set(z, 1).lower(1);

        // LP relaxation, solved by the (updatable) simplex solver
        model.relax(true);

        NodeSolver solver = model.prepare(Optimisation.Sense.MIN, NodeSolver::new);

        Optimisation.Result root = solver.solve(null);
        TestUtils.assertTrue(root.getState().isOptimal());
        TestUtils.assertEquals(4.0, root.getValue(), 1E-9); // X=2, Y=1, Z=0

        // Down probe on X: X <= 0 leaves Y >= 3 > 2, infeasible
        solver.update(0, 0.0, 0.0);
        Optimisation.Result down = solver.solve(null);
        TestUtils.assertFalse(down.getState().isOptimal());

        // Restore, then up probe X >= 1: feasible, same optimum as the root
        solver.update(0, 0.0, 2.0);
        solver.update(0, 1.0, 2.0);
        Optimisation.Result up = solver.solve(null);
        TestUtils.assertTrue(up.getState().isOptimal());
        TestUtils.assertEquals(4.0, up.getValue(), 1E-9);

        // Another probe, Y fixed at 2, while the solver-only bound X >= 1 is still in place (X=1, Y=2)
        solver.update(1, 2.0, 2.0);
        Optimisation.Result probeY = solver.solve(null);
        TestUtils.assertTrue(probeY.getState().isOptimal());
        TestUtils.assertEquals(5.0, probeY.getValue(), 1E-9); // X=1, Y=2, Z=0

        // Restore Y and probe X <= 1 (still with lower 1): X=1, Y=2 -> 5
        solver.update(1, 0.0, 2.0);
        solver.update(0, 1.0, 1.0);
        Optimisation.Result fixX = solver.solve(null);
        TestUtils.assertTrue(fixX.getState().isOptimal());
        TestUtils.assertEquals(5.0, fixX.getValue(), 1E-9);

        solver.dispose();
    }

    /**
     * A solver-only bound update that leaves a variable with lower above upper (which happens when a node's
     * bound contradicts a bound presolve derived) must come back INFEASIBLE at once, not iterate forever.
     */
    @Test
    public void testCrossedBounds() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x = model.newVariable("X").integer().lower(0).upper(2).weight(1);
        Variable y = model.newVariable("Y").integer().lower(0).upper(2).weight(2);

        model.newExpression("C1").set(x, 1).set(y, 1).lower(3);

        model.relax(true);

        NodeSolver solver = model.prepare(Optimisation.Sense.MIN, NodeSolver::new);

        TestUtils.assertTrue(solver.solve(null).getState().isOptimal());

        solver.update(0, 2.0, 1.0);
        Optimisation.Result crossed = solver.solve(null);
        TestUtils.assertEquals(Optimisation.State.INFEASIBLE, crossed.getState());

        solver.update(0, 0.0, 2.0);
        Optimisation.Result restored = solver.solve(null);
        TestUtils.assertTrue(restored.getState().isOptimal());
        TestUtils.assertEquals(4.0, restored.getValue(), 1E-9);

        solver.dispose();
    }

    /**
     * Crossed bounds on a model variable: the model is infeasible.
     */
    @Test
    public void testCrossedBoundsInModel() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x = model.newVariable("X").integer().lower(2).upper(1).weight(1);
        Variable y = model.newVariable("Y").integer().lower(0).upper(2).weight(2);

        model.newExpression("C1").set(x, 1).set(y, 1).lower(1);

        Optimisation.Result result = model.minimise();

        TestUtils.assertFalse(result.getState().isFeasible());
    }

}

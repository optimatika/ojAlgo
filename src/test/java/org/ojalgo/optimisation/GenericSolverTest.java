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
package org.ojalgo.optimisation;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.optimisation.convex.ConvexSolver;
import org.ojalgo.optimisation.linear.LinearSolver;
import org.ojalgo.type.CalendarDateUnit;

/**
 * {@link GenericSolver#countIterations()} and {@link GenericSolver#countTime(CalendarDateUnit)}
 */
public class GenericSolverTest extends OptimisationTests {

    private static void assertCounters(final GenericSolver solver) {

        TestUtils.assertEquals(0, solver.countIterations());

        Optimisation.Result result = solver.solve();

        TestUtils.assertStateNotLessThanOptimal(result);

        TestUtils.assertTrue(solver.countIterations() >= 1);

        // The time is frozen when solve returns
        double time = solver.countTime(CalendarDateUnit.NANOS);
        TestUtils.assertTrue(time > 0.0);
        TestUtils.assertEquals(time, solver.countTime(CalendarDateUnit.NANOS), 0.0);
    }

    @Test
    public void testConvexSolver() {

        ConvexSolver solver = ConvexSolver.newBuilder(2).objective(0, 0, 2.0).objective(1, 1, 2.0).linear(2.0, 4.0).inequality(1.0, 1.0, 1.0).build();

        GenericSolverTest.assertCounters(solver);
    }

    @Test
    public void testLinearSolver() {

        LinearSolver solver = LinearSolver.newBuilder(-3.0, -2.0).inequality(4.0, 1.0, 1.0).inequality(6.0, 1.0, 3.0).build();

        GenericSolverTest.assertCounters(solver);
    }

}

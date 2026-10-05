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

import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.ExpressionsBasedModel.Integration;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.State;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.type.context.NumberContext;

/**
 * When [Q] is singular (not positive definite) the active set solvers add a small constant to its diagonal.
 * That makes every problem strictly convex, with a finite solution - also when the original problem is
 * unbounded. Unbounded problems used to be reported as OPTIMAL (or DISTINCT) with a huge solution. The ADMM
 * solver detected them, but reported them as INFEASIBLE. With the null space projection the reduced Hessian
 * could be zero, apart from rounding errors, and then gave huge meaningless solutions - also for bounded
 * problems.
 * <p>
 * The problems here are min 1/2 [x]<sup>T</sup>[Q][x] - [c]<sup>T</sup>[x], with [Q] singular. Along the
 * direction (1, -1) [Q] is zero, and so is the sum of the variables. All solver variants are tested.
 */
public class SingularHessianTest extends OptimisationConvexTests {

    private static final NumberContext ACCURACY = NumberContext.of(7);
    /**
     * No component along the null space direction - bounded, but with infinitely many optimal solutions
     */
    private static final double[] BOUNDED = { 0.1, 0.1 };
    private static final double[][] SINGULAR = { { 0.01, 0.01 }, { 0.01, 0.01 } };
    /**
     * Increasing along the null space direction - unbounded
     */
    private static final double[] UNBOUNDED = { 0.1, 0.0 };

    private static ExpressionsBasedModel newModel(final double[][] quadratic, final double[] linear, final boolean sumIsOne,
            final boolean redundantInequality) {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable[] variables = new Variable[linear.length];
        for (int i = 0; i < linear.length; i++) {
            variables[i] = model.newVariable("x" + i).weight(-linear[i]);
        }

        Expression objective = model.newExpression("Q").weight(0.5);
        for (int i = 0; i < linear.length; i++) {
            for (int j = 0; j < linear.length; j++) {
                objective.set(variables[i], variables[j], quadratic[i][j]);
            }
        }

        if (sumIsOne) {
            Expression sum = model.newExpression("Sum").level(1.0);
            for (Variable variable : variables) {
                sum.set(variable, 1.0);
            }
        }

        if (redundantInequality) {
            Expression sum = model.newExpression("Redundant").upper(5.0);
            for (Variable variable : variables) {
                sum.set(variable, 1.0);
            }
        }

        return model;
    }

    /**
     * Three assets, sum is one and a redundant inequality. Singular in one direction (1, -1, 0), but not in
     * the null space of the equality constraint as a whole - the projected (reduced) Hessian is singular, but
     * not zero.
     */
    private static ExpressionsBasedModel newPartiallySingularModel(final double[] linear) {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x0 = model.newVariable("x0").weight(-linear[0]);
        Variable x1 = model.newVariable("x1").weight(-linear[1]);
        Variable x2 = model.newVariable("x2").weight(-linear[2]);

        model.newExpression("Q").weight(0.5).set(x0, x0, 0.01).set(x0, x1, 0.01).set(x1, x0, 0.01).set(x1, x1, 0.01).set(x2, x2, 0.04);
        model.newExpression("Sum").level(1.0).set(x0, 1.0).set(x1, 1.0).set(x2, 1.0);
        model.newExpression("Redundant").upper(5.0).set(x0, 1.0).set(x1, 1.0).set(x2, 1.0);

        return model;
    }

    /**
     * Bounded problems with singular [Q] must not be reported as unbounded.
     */
    @Test
    public void testBounded() {

        for (Integration<?> integration : VARIANTS) {

            Optimisation.Result result = SingularHessianTest.newModel(SINGULAR, BOUNDED, true, false).minimise(integration);
            TestUtils.assertStateNotLessThanOptimal(result);
            TestUtils.assertEquals(-0.095, result.getValue(), ACCURACY);

            result = SingularHessianTest.newModel(SINGULAR, BOUNDED, false, false).minimise(integration);
            TestUtils.assertStateNotLessThanOptimal(result);
            TestUtils.assertEquals(-0.5, result.getValue(), ACCURACY);

            result = SingularHessianTest.newModel(SINGULAR, BOUNDED, true, true).minimise(integration);
            TestUtils.assertStateNotLessThanOptimal(result);
            TestUtils.assertEquals(-0.095, result.getValue(), ACCURACY);

            result = SingularHessianTest.newPartiallySingularModel(new double[] { 0.1, 0.1, 0.05 }).minimise(integration);
            TestUtils.assertStateNotLessThanOptimal(result);
            TestUtils.assertEquals(-0.111, result.getValue(), ACCURACY);

            // Unbounded direction blocked by variable bounds
            ExpressionsBasedModel model = SingularHessianTest.newModel(SINGULAR, UNBOUNDED, true, false);
            model.getVariables().forEach(v -> v.lower(-10.0).upper(10.0));
            result = model.minimise(integration);
            TestUtils.assertStateNotLessThanOptimal(result);
            TestUtils.assertEquals(10.0, result.doubleValue(0), ACCURACY);
            TestUtils.assertEquals(-9.0, result.doubleValue(1), ACCURACY);

            // A variable only in the linear part (zero row in [Q]) that is large, but bounded by a constraint
            model = new ExpressionsBasedModel();
            Variable x0 = model.newVariable("x0").weight(-1.0);
            Variable x1 = model.newVariable("x1").weight(-1.0);
            model.newExpression("Q").weight(0.5).set(x0, x0, 1.0);
            model.newExpression("C").upper(1E6).set(x0, 1.0).set(x1, 1.0);
            result = model.minimise(integration);
            TestUtils.assertStateNotLessThanOptimal(result);
            TestUtils.assertEquals(1E6, result.doubleValue(0) + result.doubleValue(1), ACCURACY);

            // No linear term - the objective is bounded below by 0
            model = new ExpressionsBasedModel();
            x0 = model.newVariable("x0");
            x1 = model.newVariable("x1");
            model.newExpression("Q").weight(0.5).set(x0, x0, 1.0).set(x0, x1, 1.0).set(x1, x0, 1.0).set(x1, x1, 1.0);
            model.newExpression("E").level(1.0).set(x0, 1.0).set(x1, -1.0);
            result = model.minimise(integration);
            TestUtils.assertStateNotLessThanOptimal(result);
            TestUtils.assertEquals(0.5, result.doubleValue(0), ACCURACY);
            TestUtils.assertEquals(-0.5, result.doubleValue(1), ACCURACY);
        }
    }

    /**
     * The patch (a small constant added to the diagonal of [Q]) makes the solution violate the original
     * optimality conditions by an amount proportional to the size of the solution. With large solutions that
     * used to be clearly visible - here x0 = 0.0149 (exact 0.0) and x2 = -0.070 (exact -0.1), with similarly
     * wrong multipliers. Reduced gradients (bound multipliers) were calculated with the patched [Q].
     */
    @Test
    public void testPatchBias() {

        NumberContext accuracy = NumberContext.of(6);

        for (Integration<?> integration : VARIANTS) {

            // x1 has no quadratic term: min 1/2 x0^2 - x0 - x1 s.t. x0 + x1 <= 1E6
            ExpressionsBasedModel model = new ExpressionsBasedModel();
            Variable x0 = model.newVariable("x0").weight(-1.0);
            Variable x1 = model.newVariable("x1").weight(-1.0);
            model.newExpression("Q").weight(0.5).set(x0, x0, 1.0);
            model.newExpression("C").upper(1E6).set(x0, 1.0).set(x1, 1.0);
            Optimisation.Result result = model.minimise(integration);
            TestUtils.assertStateNotLessThanOptimal(result);
            TestUtils.assertEquals(0.0, result.doubleValue(0), 1E-7);
            TestUtils.assertEquals(1.0, result.getDualSolution().map(Supplier::get).get().doubleValue(0), accuracy);

            // All variables have quadratic terms, but [Q] is singular: min 1/2 (x0 + x1)^2 + 1/2 x2^2 - 0.1 x0 s.t. x0 + x2 <= 1E6
            model = new ExpressionsBasedModel();
            x0 = model.newVariable("x0").weight(-0.1);
            x1 = model.newVariable("x1");
            Variable x2 = model.newVariable("x2");
            model.newExpression("Q").weight(0.5).set(x0, x0, 1.0).set(x0, x1, 1.0).set(x1, x0, 1.0).set(x1, x1, 1.0).set(x2, x2, 1.0);
            model.newExpression("C").upper(1E6).set(x0, 1.0).set(x2, 1.0);
            result = model.minimise(integration);
            TestUtils.assertStateNotLessThanOptimal(result);
            TestUtils.assertEquals(-0.1, result.doubleValue(2), accuracy);
            TestUtils.assertEquals(0.1, result.getDualSolution().map(Supplier::get).get().doubleValue(0), accuracy);

            // The bound multiplier (reduced gradient) of x1: min 1/2 x0^2 - x0 - x1 with x1 <= 1E6
            model = new ExpressionsBasedModel();
            x0 = model.newVariable("x0").weight(-1.0);
            x1 = model.newVariable("x1").weight(-1.0).upper(1E6);
            model.newExpression("Q").weight(0.5).set(x0, x0, 1.0);
            result = model.minimise(integration);
            TestUtils.assertStateNotLessThanOptimal(result);
            TestUtils.assertEquals(-1.0, result.getReducedGradient().map(Supplier::get).get().doubleValue(1), accuracy);
        }
    }

    @Test
    public void testUnbounded() {

        for (Integration<?> integration : VARIANTS) {

            TestUtils.assertEquals(State.UNBOUNDED, SingularHessianTest.newModel(SINGULAR, UNBOUNDED, true, false).minimise(integration).getState());
            TestUtils.assertEquals(State.UNBOUNDED, SingularHessianTest.newModel(SINGULAR, UNBOUNDED, false, false).minimise(integration).getState());

            TestUtils.assertEquals(State.UNBOUNDED, SingularHessianTest.newModel(SINGULAR, UNBOUNDED, true, true).minimise(integration).getState());
            TestUtils.assertEquals(State.UNBOUNDED,
                    SingularHessianTest.newPartiallySingularModel(new double[] { 0.1, 0.0, 0.05 }).minimise(integration).getState());
        }
    }

    /**
     * Same as {@link #testUnbounded()}, but using extended precision.
     */
    @Test
    public void testUnboundedExtendedPrecision() {

        for (boolean sumIsOne : new boolean[] { true, false }) {

            ExpressionsBasedModel model = SingularHessianTest.newModel(SINGULAR, UNBOUNDED, sumIsOne, false);
            model.options.convex().extendedPrecision(true);

            TestUtils.assertEquals(State.UNBOUNDED, model.minimise().getState());
        }
    }

}

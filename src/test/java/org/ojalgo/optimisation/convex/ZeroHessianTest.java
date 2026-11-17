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
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.optimisation.Optimisation.State;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.structure.Access1D;
import org.ojalgo.type.context.NumberContext;
import org.ojalgo.type.keyvalue.EntryPair.KeyedPrimitive;

/**
 * Presolve, or branching, can fix every variable that has quadratic terms in the objective. Then the convex
 * solvers get a problem with [Q] zero – an LP – and the LP solution is the solution: the state, the variables
 * and the dual values.
 * <p>
 * With no objective terms left at all [Q] used to be replaced by the identity matrix. The solution was then
 * the feasible one with the smallest norm, the solver's objective function value included half its squared
 * norm, and the dual values were not 0. With only linear terms left the active set solvers returned the LP
 * solution with all dual values 0, and the solver for equality constraints only reported INFEASIBLE.
 * <p>
 * In the models here q is fixed (at 2, unless branching fixes it) and is the only variable with quadratic
 * terms. Apart from the branch-and-bound case, the models are built without presolve – the problem the
 * solvers get is the one stated, and the dual values are unique.
 */
public class ZeroHessianTest extends OptimisationConvexTests {

    private static final NumberContext ACCURACY = NumberContext.of(7);
    private static final NumberContext DUAL_ACCURACY = NumberContext.of(6);

    private static double getDualValue(final Result result, final String name) {
        return result.getDualValues().stream().filter(kp -> name.equals(kp.getKey().left().getName())).mapToDouble(KeyedPrimitive::doubleValue).findFirst()
                .orElseThrow();
    }

    /**
     * min q<sup>2</sup> when x0 + x1 >= 1 and x0, x1 >= 0 – every feasible solution is optimal
     */
    private static ExpressionsBasedModel newConstantObjectiveModel() {

        ExpressionsBasedModel model = ZeroHessianTest.newModel();

        Variable x0 = model.newVariable("x0").lower(0);
        Variable x1 = model.newVariable("x1").lower(0);
        Variable q = model.newVariable("q").level(2);

        model.newExpression("Objective").weight(1).set(q, q, 1);
        model.newExpression("Sum").lower(1).set(x0, 1).set(x1, 1);

        return model;
    }

    /**
     * min q<sup>2</sup> + q x0 + q x1 (bounded) or q<sup>2</sup> + q x0 (unbounded) when x0 + x1 = 1 – no
     * inequalities, x0 and x1 are free. Bounded, the objective function value is 4 + 2 (x0 + x1) = 6 for
     * every feasible solution. Without quadratic terms the LP counterpart is min 2 x0 + 2 x1 (or 2 x0).
     */
    private static ExpressionsBasedModel newEqualityConstrainedModel(final boolean quadratic, final boolean bounded) {

        ExpressionsBasedModel model = ZeroHessianTest.newModel();

        Variable x0 = model.newVariable("x0");
        Variable x1 = model.newVariable("x1");

        if (quadratic) {
            Variable q = model.newVariable("q").level(2);
            Expression objective = model.newExpression("Objective").weight(1).set(q, q, 1).set(q, x0, 1);
            if (bounded) {
                objective.set(q, x1, 1);
            }
        } else {
            x0.weight(2);
            if (bounded) {
                x1.weight(2);
            }
        }

        model.newExpression("Sum").level(1).set(x0, 1).set(x1, 1);

        return model;
    }

    /**
     * min q<sup>2</sup> - q x0 - x1, that is 4 - 2 x0 - x1, when x0 + x1 + x2 = 1, x0 + 0.5 x2 <= 0.6 and x0,
     * x1, x2 >= 0. Without quadratic terms the LP counterpart is min -2 x0 - x1. The solution, x0 = 0.6, x1 =
     * 0.4 and x2 = 0, is unique and not degenerate.
     */
    private static ExpressionsBasedModel newLinearObjectiveModel(final boolean quadratic) {

        ExpressionsBasedModel model = ZeroHessianTest.newModel();

        Variable x0 = model.newVariable("x0").lower(0);
        Variable x1 = model.newVariable("x1").lower(0).weight(-1);
        Variable x2 = model.newVariable("x2").lower(0);

        if (quadratic) {
            Variable q = model.newVariable("q").level(2);
            model.newExpression("Objective").weight(1).set(q, q, 1).set(q, x0, -1);
        } else {
            x0.weight(-2);
        }

        model.newExpression("Sum").level(1).set(x0, 1).set(x1, 1).set(x2, 1);
        model.newExpression("Cap").upper(0.6).set(x0, 1).set(x2, 0.5);

        return model;
    }

    private static ExpressionsBasedModel newModel() {

        Optimisation.Environment environment = Optimisation.newEnvironment();
        environment.clearPresolvers();

        return environment.newModel();
    }

    /**
     * min q<sup>2</sup> - 2.8 q, q integer in [1, 2], when x + 5 q >= 10 and x >= 0. Branching on q fixes it,
     * and then nothing of the objective is left in the node problems. The optimum is q = 1, with value -1.8
     * (q = 2 gives -1.6). With [Q] replaced by the identity matrix the q = 1 node value used to include 1/2
     * x<sup>2</sup>, with x >= 5, and that's enough to make q = 2 look better.
     */
    @Test
    public void testBranchFixingQuadraticVariable() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable q = model.newVariable("q").lower(1).upper(2).integer(true).weight(-2.8);
        Variable x = model.newVariable("x").lower(0);

        model.newExpression("Objective").weight(1).set(q, q, 1);
        model.newExpression("Link").lower(10).set(x, 1).set(q, 5);

        Result result = model.minimise();

        TestUtils.assertStateNotLessThanOptimal(result);
        TestUtils.assertEquals(1.0, result.doubleValue(0), ACCURACY);
        TestUtils.assertEquals(-1.8, result.getValue(), ACCURACY);
    }

    @Test
    public void testConstantObjective() {

        for (int v = 0; v < VARIANTS.length; v++) {

            String id = "VARIANTS[" + v + "]";

            Result result = ZeroHessianTest.newConstantObjectiveModel().minimise(VARIANTS[v]);

            TestUtils.assertStateNotLessThanOptimal(result);
            TestUtils.assertEquals(id, 4.0, result.getValue(), ACCURACY);
            TestUtils.assertTrue(id, result.doubleValue(0) + result.doubleValue(1) >= 1.0 - 1E-7);
            TestUtils.assertEquals(id, 0.0, ZeroHessianTest.getDualValue(result, "Sum"), DUAL_ACCURACY);
        }

        // The solver's own objective function value, without the constant
        ConvexData<Double> data = ConvexSolver.copy(ZeroHessianTest.newConstantObjectiveModel(), R064Store.FACTORY);
        Result result = BasePrimitiveSolver.newSolver(data, new Optimisation.Options()).solve();

        TestUtils.assertStateNotLessThanOptimal(result);
        TestUtils.assertEquals(0.0, result.getValue(), ACCURACY);
    }

    @Test
    public void testEqualityConstrained() {

        Result expected = ZeroHessianTest.newEqualityConstrainedModel(false, true).minimise();
        TestUtils.assertStateNotLessThanOptimal(expected);

        for (int v = 0; v < VARIANTS.length; v++) {

            String id = "VARIANTS[" + v + "]";

            Result actual = ZeroHessianTest.newEqualityConstrainedModel(true, true).minimise(VARIANTS[v]);

            TestUtils.assertStateNotLessThanOptimal(actual);
            TestUtils.assertEquals(id, 6.0, actual.getValue(), ACCURACY);
            TestUtils.assertEquals(id, 1.0, actual.doubleValue(0) + actual.doubleValue(1), ACCURACY);
            TestUtils.assertEquals(id, ZeroHessianTest.getDualValue(expected, "Sum"), ZeroHessianTest.getDualValue(actual, "Sum"), DUAL_ACCURACY);

            Result unbounded = ZeroHessianTest.newEqualityConstrainedModel(true, false).minimise(VARIANTS[v]);

            TestUtils.assertEquals(id, State.UNBOUNDED, unbounded.getState());
        }
    }

    @Test
    public void testLinearObjective() {

        Result expected = ZeroHessianTest.newLinearObjectiveModel(false).minimise();
        TestUtils.assertStateNotLessThanOptimal(expected);

        Access1D<?> expectedGradient = expected.getReducedGradient().map(Supplier::get).orElseThrow();

        for (int v = 0; v < VARIANTS.length; v++) {

            String id = "VARIANTS[" + v + "]";

            Result actual = ZeroHessianTest.newLinearObjectiveModel(true).minimise(VARIANTS[v]);

            TestUtils.assertStateNotLessThanOptimal(actual);
            TestUtils.assertEquals(id, expected.getValue() + 4.0, actual.getValue(), ACCURACY);
            TestUtils.assertEquals(id, 0.6, actual.doubleValue(0), ACCURACY);
            TestUtils.assertEquals(id, 0.4, actual.doubleValue(1), ACCURACY);
            TestUtils.assertEquals(id, 0.0, actual.doubleValue(2), ACCURACY);

            TestUtils.assertEquals(id + " Sum", ZeroHessianTest.getDualValue(expected, "Sum"), ZeroHessianTest.getDualValue(actual, "Sum"), DUAL_ACCURACY);
            TestUtils.assertEquals(id + " Cap", ZeroHessianTest.getDualValue(expected, "Cap"), ZeroHessianTest.getDualValue(actual, "Cap"), DUAL_ACCURACY);

            actual.getReducedGradient().map(Supplier::get).ifPresent(actualGradient -> {
                for (int j = 0; j < 3; j++) {
                    TestUtils.assertEquals(id + " x" + j, expectedGradient.doubleValue(j), actualGradient.doubleValue(j), DUAL_ACCURACY);
                }
            });
        }
    }

    /**
     * No constraints, and x0 is free: optimal (at any x0) if nothing of the objective is left, otherwise
     * unbounded.
     */
    @Test
    public void testUnconstrained() {

        Optimisation.Options options = new Optimisation.Options();

        ExpressionsBasedModel model = ZeroHessianTest.newModel();

        Variable x0 = model.newVariable("x0");
        Variable q = model.newVariable("q").level(2);

        Expression objective = model.newExpression("Objective").weight(1).set(q, q, 1);

        Result result = BasePrimitiveSolver.newSolver(ConvexSolver.copy(model, R064Store.FACTORY), options).solve();

        TestUtils.assertStateNotLessThanOptimal(result);
        TestUtils.assertEquals(0.0, result.getValue(), ACCURACY);

        objective.set(q, x0, 1);

        result = BasePrimitiveSolver.newSolver(ConvexSolver.copy(model, R064Store.FACTORY), options).solve();

        TestUtils.assertEquals(State.UNBOUNDED, result.getState());
    }

}

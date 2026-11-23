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
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.ModelEntity;
import org.ojalgo.optimisation.ModelFileTest;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.ConstraintType;
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.type.context.NumberContext;
import org.ojalgo.type.keyvalue.EntryPair;
import org.ojalgo.type.keyvalue.EntryPair.KeyedPrimitive;

public class AlternatingDirectionSolverTest extends OptimisationConvexTests {

    private static KeyedPrimitive<EntryPair<ModelEntity<?>, ConstraintType>> getDualValue(final Result result, final ModelEntity<?> entity) {
        for (KeyedPrimitive<EntryPair<ModelEntity<?>, ConstraintType>> dual : result.getDualValues()) {
            if (dual.getKey().left() == entity) {
                return dual;
            }
        }
        throw new IllegalArgumentException();
    }

    /**
     * Convex regression, the convex sequence nearest to a sampled concave function – a smaller version of
     * Maros-Meszaros YAO: min 1/2 |x - c|<sup>2</sup> with c<sub>i</sub> = sin((i + 1) / n), all second
     * differences x<sub>i</sub> - 2 x<sub>i+1</sub> + x<sub>i+2</sub> >= 0, x<sub>0</sub> >= 0.08 and the
     * last two variables fixed at 0. The second difference operator is so badly conditioned that ADMM doesn't
     * converge. ADMM is what's chosen, automatically, for a model of this size, and when it fails the active
     * set solver should be used instead.
     */
    @Test
    public void testFallbackToActiveSet() {

        int n = 500;

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable[] x = new Variable[n];
        for (int i = 0; i < n; i++) {
            x[i] = model.newVariable("x" + i).weight(-Math.sin((i + 1.0) / n));
        }
        x[0].lower(0.08);
        x[n - 2].level(0.0);
        x[n - 1].level(0.0);

        Expression quadratic = model.newExpression("Q").weight(0.5);
        for (int i = 0; i < n; i++) {
            quadratic.set(x[i], x[i], 1.0);
        }

        for (int i = 0; i + 2 < n; i++) {
            model.newExpression("D" + i).lower(0.0).set(x[i], 1.0).set(x[i + 1], -2.0).set(x[i + 2], 1.0);
        }

        Result result = model.minimise(ConvexSolver.INTEGRATION);

        TestUtils.assertStateNotLessThanOptimal(result);
        TestUtils.assertEquals(-18.799554674, result.getValue(), NumberContext.of(8));
        TestUtils.assertTrue(model.validate(result));
    }

    /**
     * min x^2 subject to x <= -1 and -x <= -1 (x >= 1) is infeasible. Built by {@link ConvexSolver.Builder}
     * the inequality rows have lower limit -Infinity. The primal infeasibility test summed u * max(Δy, 0) + l
     * * min(Δy, 0), and -Infinity * 0 = NaN, so the problem was never certified infeasible – it ended
     * APPROXIMATE.
     */
    @Test
    public void testInfeasibleWithInfiniteLimit() {

        ConvexSolver.Builder builder = ConvexSolver.newBuilder(1).objective(0, 0, 1.0).inequality(-1.0, 1.0).inequality(-1.0, -1.0);

        AlternatingDirectionSolver solver = new AlternatingDirectionSolver(builder.getADProblem(), new Optimisation.Options());

        Result result = solver.solve();

        TestUtils.assertEquals(Optimisation.State.INFEASIBLE, result.getState());
    }

    /**
     * Minimising (x^2 + y^2) / 2 subject to x + y <= 1 and x, y >= 0 gives x = y = 0. Updating the range of x
     * to [2, +Infinity), as when branching, makes the problem infeasible. The upper limit of x's bound row is
     * then +Infinity, and (as with {@link #testInfeasibleWithInfiniteLimit()}) the problem was never
     * certified infeasible.
     */
    @Test
    public void testInfeasibleWithInfiniteRangeUpdate() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x = model.addVariable("x").lower(0);
        Variable y = model.addVariable("y").lower(0);

        model.addExpression("objective").weight(1).set(x, x, 0.5).set(y, y, 0.5);
        model.addExpression("constraint").set(x, 1).set(y, 1).upper(1);

        AlternatingDirectionSolver.Composer<Double> composer = AlternatingDirectionSolver.build(model, R064Store.FACTORY);
        AlternatingDirectionSolver solver = new AlternatingDirectionSolver(composer.toProblem(), new Optimisation.Options(), composer.getStructure());

        TestUtils.assertStateNotLessThanOptimal(solver.solve());

        TestUtils.assertTrue(solver.updateRange(0, 2.0, Double.POSITIVE_INFINITY));

        Result updated = solver.solve();

        TestUtils.assertEquals(Optimisation.State.INFEASIBLE, updated.getState());
    }

    /**
     * Minimising -x^2 - y^2 + 0.1x + 0.1y subject to x + y <= 1.5 and 0 <= x, y <= 1 is not a convex problem
     * (P = -2I). With the initial rho the KKT matrix does not have n positive eigenvalues, and the solver
     * returns INVALID rather than iterate to a stationary point and report that as OPTIMAL (it did, with x =
     * y = 0.05, while the minimum is about -1.1).
     */
    @Test
    public void testNonConvexIsInvalid() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x = model.newVariable("x").lower(0).upper(1);
        Variable y = model.newVariable("y").lower(0).upper(1);

        model.newExpression("objective").weight(1).set(x, x, -1).set(x, 0.1).set(y, y, -1).set(y, 0.1);
        model.newExpression("constraint").set(x, 1).set(y, 1).upper(1.5);

        AlternatingDirectionSolver.Composer<Double> composer = AlternatingDirectionSolver.build(model, R064Store.FACTORY);
        AlternatingDirectionSolver solver = new AlternatingDirectionSolver(composer.toProblem(), new Optimisation.Options(), composer.getStructure());

        Result result = solver.solve();

        TestUtils.assertEquals(Optimisation.State.INVALID, result.getState());
    }

    /**
     * Maros-Meszaros QGROW7 (optimal value -4.2798714E7) - ADMM doesn't converge, and returns APPROXIMATE
     * with an all NaN solution. NaN values pass the model validation, and that result was reported as OPTIMAL
     * with value 0.0.
     */
    @Test
    public void testNoSolutionIsNotOptimal() {

        ExpressionsBasedModel model = ModelFileTest.makeModel("marosmeszaros", "QGROW7.SIF", false);
        model.options.convex().algorithm(ConvexSolver.Algorithm.ADMM);

        Result result = model.minimise(ConvexSolver.INTEGRATION);

        if (result.getState().isOptimal()) {
            TestUtils.assertEquals(-4.2798714E7, result.getValue(), NumberContext.of(6));
        } else {
            TestUtils.assertEquals(Optimisation.State.APPROXIMATE, result.getState());
        }
    }

    /**
     * A variable's bound row is registered with the type of its bounds, and the sign of its multiplier is
     * interpreted accordingly. When {@link AlternatingDirectionSolver#updateRange(int, double, double)}
     * changes that type the reported dual values must follow – without changing those of a result already
     * returned.
     * <p>
     * Minimising (x^2 + y^2) / 2 - 800x - 800y subject to x + y <= 1500 and x, y >= 0 gives x = y = 750.
     * Adding the upper bound x <= 200 gives x = 200 with bound multiplier 600, and y = 800.
     */
    @Test
    public void testUpdateRangeChangesBoundType() {

        NumberContext accuracy = NumberContext.of(6);

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x = model.addVariable("x").lower(0);
        Variable y = model.addVariable("y").lower(0);

        model.addExpression("objective").weight(1).set(x, x, 0.5).set(x, -800).set(y, y, 0.5).set(y, -800);
        model.addExpression("constraint").set(x, 1).set(y, 1).upper(1500);

        AlternatingDirectionSolver.Composer<Double> composer = AlternatingDirectionSolver.build(model, R064Store.FACTORY);
        AlternatingDirectionSolver solver = new AlternatingDirectionSolver(composer.toProblem(), new Optimisation.Options(), composer.getStructure());

        Result initial = solver.solve();
        TestUtils.assertStateNotLessThanOptimal(initial);
        TestUtils.assertEquals(750.0, initial.doubleValue(0), accuracy);

        TestUtils.assertTrue(solver.updateRange(0, 0.0, 200.0));

        Result updated = solver.solve();
        TestUtils.assertStateNotLessThanOptimal(updated);
        TestUtils.assertEquals(200.0, updated.doubleValue(0), accuracy);
        TestUtils.assertEquals(800.0, updated.doubleValue(1), accuracy);

        KeyedPrimitive<EntryPair<ModelEntity<?>, ConstraintType>> bound = AlternatingDirectionSolverTest.getDualValue(updated, x);
        TestUtils.assertEquals(ConstraintType.UPPER, bound.getKey().right());
        TestUtils.assertEquals(600.0, bound.doubleValue(), accuracy);

        TestUtils.assertEquals(ConstraintType.LOWER, AlternatingDirectionSolverTest.getDualValue(initial, x).getKey().right());
    }

    /**
     * {@link AlternatingDirectionSolver#updateRange(int, double, double)} takes a solver variable index. The
     * variable bounds are rows of the constraint matrix, after the model's constraints, so the index has to
     * be mapped to the variable's bound row. A variable without bounds has no such row, and then the update
     * is refused.
     * <p>
     * Minimising (x^2 + y^2 + z^2) / 2 - 800x - 800y subject to x + y <= 1500, x and y in [0, 1000] and z
     * free gives x = y = 750 and z = 0. Fixing x at 200, in place, gives x = 200 and y = 800. (With these
     * bounds the variables' adjustment factor is not 1, which matters when the bounds are adjusted.)
     */
    @Test
    public void testUpdateRangeOfVariable() {

        NumberContext accuracy = NumberContext.of(6);

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x = model.addVariable("x").lower(0).upper(1000);
        Variable y = model.addVariable("y").lower(0).upper(1000);
        Variable z = model.addVariable("z");

        model.addExpression("objective").weight(1).set(x, x, 0.5).set(x, -800).set(y, y, 0.5).set(y, -800).set(z, z, 0.5);
        model.addExpression("constraint").set(x, 1).set(y, 1).upper(1500);

        AlternatingDirectionSolver.Composer<Double> composer = AlternatingDirectionSolver.build(model, R064Store.FACTORY);
        AlternatingDirectionSolver solver = new AlternatingDirectionSolver(composer.toProblem(), new Optimisation.Options(), composer.getStructure());

        Result initial = solver.solve();
        TestUtils.assertStateNotLessThanOptimal(initial);
        TestUtils.assertEquals(750.0, initial.doubleValue(0), accuracy);
        TestUtils.assertEquals(750.0, initial.doubleValue(1), accuracy);

        TestUtils.assertFalse(solver.updateRange(2, -1.0, 1.0));

        TestUtils.assertTrue(solver.updateRange(0, 200.0, 200.0));

        Result updated = solver.solve();
        TestUtils.assertStateNotLessThanOptimal(updated);
        TestUtils.assertEquals(200.0, updated.doubleValue(0), accuracy);
        TestUtils.assertEquals(800.0, updated.doubleValue(1), accuracy);
    }

    /**
     * Minimising ε x^2 - x subject to x >= -1 gives x = 1 / (2ε). The unboundedness (dual infeasibility) test
     * used a relative tolerance of 1E-3, from the first iteration, and any direction with less curvature than
     * that counted as an unbounded ray. With ε = 1E-4 (x = 5000) the problem was reported UNBOUNDED.
     */
    @Test
    public void testWeakCurvatureOneVariable() {

        double epsilon = 1E-4;

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x = model.addVariable("x").lower(-1);

        model.addExpression("objective").weight(1).set(x, x, epsilon).set(x, -1);

        Result result = model.minimise(ConfiguredIntegration.ADMM);

        TestUtils.assertStateNotLessThanOptimal(result);
        TestUtils.assertEquals(0.5 / epsilon, result.doubleValue(0), NumberContext.of(6));
    }

    /**
     * Two highly correlated assets, long-short: minimising [x]<sup>T</sup>[P][x] / 2 - x1 with [P] = [[1,
     * 1-δ], [1-δ, 1]], x1 >= -1 and x2 free. Along (1, -1) the curvature is δ, and with δ = 1E-4 the solution
     * is about (5000, -5000). That was reported UNBOUNDED – see {@link #testWeakCurvatureOneVariable()}. Row
     * and column scaling can't change the curvature along (1, -1).
     */
    @Test
    public void testWeakCurvatureTwoVariables() {

        double delta = 1E-4;

        double determinant = 1.0 - (1.0 - delta) * (1.0 - delta);

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x1 = model.addVariable("x1").lower(-1);
        Variable x2 = model.addVariable("x2");

        model.addExpression("objective").weight(1).set(x1, x1, 0.5).set(x2, x2, 0.5).set(x1, x2, 1.0 - delta).set(x1, -1);

        Result result = model.minimise(ConfiguredIntegration.ADMM);

        TestUtils.assertStateNotLessThanOptimal(result);
        TestUtils.assertEquals(1.0 / determinant, result.doubleValue(0), NumberContext.of(6));
        TestUtils.assertEquals(-(1.0 - delta) / determinant, result.doubleValue(1), NumberContext.of(6));
    }

}

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
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.ModelEntity;
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

}

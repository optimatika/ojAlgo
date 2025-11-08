package org.ojalgo.optimisation.conic;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;

/**
 * Verifies how {@link ExpressionsBasedModel} constructs map to conic form: A, b, Aeq, beq, Q, c and cone
 * blocks.
 */
public class ConicIntegrationMappingTest {

    @Test
    public void mapsDiagonalQuadraticInequalityToSecondOrderCone() {
        ExpressionsBasedModel model = new ExpressionsBasedModel();
        Variable t = model.addVariable("t");
        Variable x = model.addVariable("x");
        Variable y = model.addVariable("y");

        // SOC: -t^2 + x^2 + y^2 <= 0  <=>  t >= sqrt(x^2 + y^2)
        Expression soc = model.addExpression("soc");
        soc.add(t, t, -1.0);
        soc.add(x, x, 1.0);
        soc.add(y, y, 1.0);
        soc.upper(0.0);

        ConicSolver solver = ConicSolver.INTEGRATION.build(model);
        ConicSolver.ConicProblem p = solver.problem();

        // Expect a single SOC block of size 3 and no nonnegative block (no linear inequalities/bounds)
        TestUtils.assertEquals(1, p.cones.size());
        TestUtils.assertTrue(p.cones.get(0).cone instanceof ConicSolver.SecondOrderCone);
        TestUtils.assertEquals(3, p.cones.get(0).cone.size());

        // A and b should enforce s = -x for the SOC block variables -> rows with -1 on corresponding columns
        MatrixStore<Double> A = p.A;
        MatrixStore<Double> b = p.b;
        TestUtils.assertEquals(3L, A.countRows());
        TestUtils.assertEquals(3L, A.countColumns());
        TestUtils.assertEquals(3L, b.countRows());
        TestUtils.assertEquals(0.0, b.doubleValue(0));
        TestUtils.assertEquals(0.0, b.doubleValue(1));
        TestUtils.assertEquals(0.0, b.doubleValue(2));

        int it = model.indexOfFreeVariable(t);
        int ix = model.indexOfFreeVariable(x);
        int iy = model.indexOfFreeVariable(y);

        // Each row picks exactly one variable with coefficient -1
        double sumAbsRow0 = Math.abs(A.doubleValue(0, it)) + Math.abs(A.doubleValue(0, ix)) + Math.abs(A.doubleValue(0, iy));
        double sumAbsRow1 = Math.abs(A.doubleValue(1, it)) + Math.abs(A.doubleValue(1, ix)) + Math.abs(A.doubleValue(1, iy));
        double sumAbsRow2 = Math.abs(A.doubleValue(2, it)) + Math.abs(A.doubleValue(2, ix)) + Math.abs(A.doubleValue(2, iy));
        TestUtils.assertEquals(1.0, sumAbsRow0);
        TestUtils.assertEquals(1.0, sumAbsRow1);
        TestUtils.assertEquals(1.0, sumAbsRow2);
    }

    @Test
    public void mapsLinearBoundsAndConstraintsToNonnegativeCone() {
        ExpressionsBasedModel model = new ExpressionsBasedModel();
        Variable x = model.addVariable("x").lower(0.0); // x >= 0
        Variable y = model.addVariable("y").upper(2.0); // y <= 2
        Variable z = model.addVariable("z").lower(-1.0).upper(3.0); // -1 <= z <= 3

        // 2x - y <= 1
        model.addExpression("lin-ineq").set(x, 2.0).set(y, -1.0).upper(1.0);
        // x + z = 0
        model.addExpression("lin-eq").set(x, 1.0).set(z, 1.0).level(0.0);

        // minimise 0 (no objective terms needed here)
        Optimisation.Solver solver = ConicSolver.INTEGRATION.build(model);
        TestUtils.assertTrue(solver instanceof ConicSolver);
        ConicSolver conic = (ConicSolver) solver;
        ConicSolver.ConicProblem p = conic.problem();

        // One nonnegative cone covering all linear inequalities & bounds
        TestUtils.assertEquals(1, p.cones.size());
        TestUtils.assertTrue(p.cones.get(0).cone instanceof ConicSolver.NonnegativeCone);

        // Rows: 2x - y <= 1, y <= 2, z <= 3, x >= 0, z >= -1  -> total 5 rows
        MatrixStore<Double> A = p.A;
        MatrixStore<Double> b = p.b;
        TestUtils.assertEquals(5L, A.countRows());
        TestUtils.assertEquals(3L, A.countColumns());
        TestUtils.assertEquals(5L, b.countRows());

        // Equality part preserved as-is
        MatrixStore<Double> Aeq = p.Aeq;
        MatrixStore<Double> beq = p.beq;
        TestUtils.assertEquals(1L, Aeq.countRows());
        TestUtils.assertEquals(3L, Aeq.countColumns());
        // Row must represent x + z = 0
        // Check a couple of entries to avoid relying on row ordering in inequalities
        TestUtils.assertEquals(1.0, Aeq.doubleValue(0, model.indexOfFreeVariable(x)));
        TestUtils.assertEquals(1.0, Aeq.doubleValue(0, model.indexOfFreeVariable(z)));
        TestUtils.assertEquals(0.0, beq.doubleValue(0));

        // No quadratic objective terms were set: verify all Q entries zero
        for (int r = 0; r < p.Q.countRows(); r++) {
            for (int c2 = 0; c2 < p.Q.countColumns(); c2++) {
                TestUtils.assertEquals(0.0, p.Q.doubleValue(r, c2));
            }
        }
    }

    @Test
    public void mapsQuadraticObjectiveToQandc() {
        ExpressionsBasedModel model = new ExpressionsBasedModel();
        Variable x = model.addVariable("x");
        Variable y = model.addVariable("y");

        // Define objective as a proper weighted expression in the model so aggregation works
        Expression obj = model.addExpression("OBJ").weight(1.0);
        // Objective: 0.5 * [x y] [[1,1],[1,3]] [x;y] + [4 5]·[x;y]
        obj.add(x, x, 1.0);
        obj.add(x, y, 1.0); // off-diagonal -> symmetric
        obj.add(y, y, 3.0);
        obj.add(x, 4.0);
        obj.add(y, 5.0);

        ConicSolver solver = ConicSolver.INTEGRATION.build(model);
        ConicSolver.ConicProblem p = solver.problem();

        int ix = model.indexOfFreeVariable(x);
        int iy = model.indexOfFreeVariable(y);

        // Check Q entries
        TestUtils.assertEquals(1.0, p.Q.doubleValue(ix, ix));
        TestUtils.assertEquals(3.0, p.Q.doubleValue(iy, iy));
        TestUtils.assertEquals(1.0, p.Q.doubleValue(ix, iy));
        TestUtils.assertEquals(1.0, p.Q.doubleValue(iy, ix));

        // Check c entries
        TestUtils.assertEquals(4.0, p.c.doubleValue(ix));
        TestUtils.assertEquals(5.0, p.c.doubleValue(iy));
    }
}
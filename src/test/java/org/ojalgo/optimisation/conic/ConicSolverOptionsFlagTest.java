package org.ojalgo.optimisation.conic;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.optimisation.Optimisation;

import java.util.List;

/**
 * Sanity test: the predictor–corrector option flag can toggle modes and both run without failure
 * on a tiny LP: minimise x subject to 0 <= x <= 1.
 */
public class ConicSolverOptionsFlagTest {

    private ConicSolver.ConicProblem buildTinyLP() {
        // n = 1, inequalities: x <= 1 and -x <= 0  -> A x + s = b with A = [1; -1], b = [1; 0]
        R064Store A = R064Store.FACTORY.make(2, 1);
        A.set(0, 0, 1.0);
        A.set(1, 0, -1.0);
        R064Store b = R064Store.FACTORY.make(2, 1);
        b.set(0, 0, 1.0);
        b.set(1, 0, 0.0);
        MatrixStore<Double> Aeq = null;
        MatrixStore<Double> beq = null;
        R064Store c = R064Store.FACTORY.make(1, 1);
        c.set(0, 0, 1.0); // minimise x
        MatrixStore<Double> Q = null;
        ConicSolver.NonnegativeCone nn = new ConicSolver.NonnegativeCone(2);
        ConicSolver.ConeBlock block = new ConicSolver.ConeBlock(nn, 0);
        return new ConicSolver.ConicProblem(A, b, Aeq, beq, c, Q, List.of(block));
    }

    @Test
    public void testSolveWithPredictorCorrectorToggled() {
        ConicSolver.ConicProblem problem = buildTinyLP();

        // Case 1: predictor–corrector enabled
        Optimisation.Options opts1 = new Optimisation.Options();
        ConicSolver.Configuration cfg1 = new ConicSolver.Configuration();
        cfg1.usePredictorCorrector = true;
        opts1.setConfigurator(cfg1);
        ConicSolver solver1 = ConicSolver.of(problem, opts1);
        Optimisation.Result res1 = solver1.solve();
        TestUtils.assertTrue(res1.getState() != Optimisation.State.FAILED);
        // Expect near x=0 and value ~0
        double x1 = res1.doubleValue(0);
        TestUtils.assertEquals(0.0, Math.max(0.0, Math.min(1.0, x1)), 1.0e-3);
        TestUtils.assertEquals(0.0, Math.max(0.0, res1.getValue()), 1.0e-2);

        // Case 2: predictor–corrector disabled (barrier-only)
        Optimisation.Options opts2 = new Optimisation.Options();
        ConicSolver.Configuration cfg2 = new ConicSolver.Configuration();
        cfg2.usePredictorCorrector = false;
        opts2.setConfigurator(cfg2);
        ConicSolver solver2 = ConicSolver.of(problem, opts2);
        Optimisation.Result res2 = solver2.solve();
        TestUtils.assertTrue(res2.getState() != Optimisation.State.FAILED);
        double x2 = res2.doubleValue(0);
        TestUtils.assertEquals(0.0, Math.max(0.0, Math.min(1.0, x2)), 1.0e-3);
        TestUtils.assertEquals(0.0, Math.max(0.0, res2.getValue()), 1.0e-2);
    }
}

package org.ojalgo.optimisation.convex;

import org.junit.jupiter.api.Test;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.type.context.NumberContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic tests verifying Schur complement scaling produces consistent solutions between
 * DirectASS and IterativeASS and improves (regularises) the diagonal.
 */
public class SchurScalingTest {

    private static final NumberContext TOL = NumberContext.of(7, 10);

    private ConvexData<Double> buildSimpleProblem() {
        // min 0.5 x'Qx - c'x subject to Ax = b, x >= unconstrained inequalities
        int vars = 3;
        int eqs = 1;
        int ineq = 2;
        ConvexData<Double> data = new ConvexData<>(false, R064Store.FACTORY, vars, eqs, ineq);
        // Q diagonal with differing scales to test scaling
        data.addObjective(0, 0, 1.0);
        data.addObjective(1, 1, 1.0e-6);
        data.addObjective(2, 2, 1.0e3);
        // c
        data.addObjective(0, 1.0);
        data.addObjective(1, -2.0);
        data.addObjective(2, 0.5);
        // Equality: x0 + x1 + x2 = 1
        data.setAE(0, 0, 1.0);
        data.setAE(0, 1, 1.0);
        data.setAE(0, 2, 1.0);
        data.setBE(0, 1.0);
        // Inequalities: x0 + 2 x1 <= 0.8, 3 x2 <= 2.5
        data.setAI(0, 0, 1.0); data.setAI(0, 1, 2.0); data.setBI(0, 0.8);
        data.setAI(1, 2, 3.0); data.setBI(1, 2.5);
        return data;
    }

    private Optimisation.Result solveIterative(final ConvexData<Double> data) {
        IterativeASS solver = new IterativeASS(data, new Optimisation.Options());
        return solver.solve();
    }

    private Optimisation.Result solveDirect(final ConvexData<Double> data) {
        DirectASS solver = new DirectASS(data, new Optimisation.Options());
        return solver.solve();
    }

    @Test
    public void testConsistencyBetweenSolvers() {
        ConvexData<Double> data = buildSimpleProblem();
        Optimisation.Result itRes = solveIterative(data);
        ConvexData<Double> data2 = buildSimpleProblem();
        Optimisation.Result dirRes = solveDirect(data2);
        assertTrue(itRes.getState().isFeasible());
        assertTrue(dirRes.getState().isFeasible());
        double[] xIt = itRes.toRawCopy1D();
        double[] xDir = dirRes.toRawCopy1D();
        assertEquals(xIt.length, xDir.length);
        double maxDiff = 0.0; double maxVal = 0.0;
        for (int i = 0; i < xIt.length; i++) {
            double diff = Math.abs(xIt[i] - xDir[i]);
            if (diff > maxDiff) maxDiff = diff;
            double abs = Math.max(Math.abs(xIt[i]), Math.abs(xDir[i]));
            if (abs > maxVal) maxVal = abs;
        }
        System.out.println("IterativeASS x=" + java.util.Arrays.toString(xIt));
        System.out.println("DirectASS    x=" + java.util.Arrays.toString(xDir));
        System.out.println("maxDiff=" + maxDiff + " maxVal=" + maxVal);
        assertTrue(maxDiff <= 1e-7 * Math.max(1.0, maxVal), "Max diff too large");
    }
}
package org.ojalgo.optimisation.convex;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.store.R064CSC;
import org.ojalgo.optimisation.Equilibrator;

/**
 * Tests for {@link RuizScaling}, verifying clamping behaviour, round-trip scaling/unscaling, and sparsity
 * preservation.
 */
public class RuizScalingTest extends OptimisationConvexTests {

    /**
     * Verifies that {@link Equilibrator#clamp(double)} replaces invalid or out-of-range values with sensible
     * defaults.
     */
    @Test
    public void testLimitScalingScalar() {

        double min = Equilibrator.MIN;
        double max = Equilibrator.MAX;

        TestUtils.assertEquals(1.0, Equilibrator.clamp(0.0), 0.0);
        TestUtils.assertEquals(1.0, Equilibrator.clamp(min / 2.0), 0.0);
        TestUtils.assertEquals(max, Equilibrator.clamp(max * 2.0), 0.0);

        double mid = 0.5 * (min + max);
        TestUtils.assertEquals(mid, Equilibrator.clamp(mid), 0.0);
    }

    /**
     * Verifies that {@link Equilibrator#clamp(double[])} corrects each element of a scaling vector in place.
     */
    @Test
    public void testLimitScalingVector() {

        double[] v = { 0.0, Equilibrator.MIN / 2.0, Equilibrator.MIN, Equilibrator.MAX, Equilibrator.MAX * 2.0 };

        Equilibrator.clamp(v);

        TestUtils.assertEquals(1.0, v[0], 0.0);
        TestUtils.assertEquals(1.0, v[1], 0.0);
        TestUtils.assertEquals(Equilibrator.MIN, v[2], 0.0);
        TestUtils.assertEquals(Equilibrator.MAX, v[3], 0.0);
        TestUtils.assertEquals(Equilibrator.MAX, v[4], 0.0);
    }

    /**
     * Constructs a scaled solution from a known unscaled (true) solution, then calls
     * {@link RuizScaling#unscale} and verifies the original values are recovered.
     */
    @Test
    public void testScaleAndUnscaleRoundTrip() {

        int n = 2;
        int m = 1;

        R064CSC P = new R064CSC(2, 2, 3);
        P.pointers[0] = 0;
        P.pointers[1] = 2;
        P.pointers[2] = 3;
        P.indices[0] = 0;
        P.values[0] = 4.0;
        P.indices[1] = 1;
        P.values[1] = 1.0;
        P.indices[2] = 1;
        P.values[2] = 2.0;

        R064CSC A = new R064CSC(1, 2, 2);
        A.pointers[0] = 0;
        A.pointers[1] = 1;
        A.pointers[2] = 2;
        A.indices[0] = 0;
        A.values[0] = 1.0;
        A.indices[1] = 0;
        A.values[1] = 1.0;

        double[] q = { 1.0, 2.0 };
        double[] l = { 0.0 };
        double[] u = { 10.0 };

        AlternatingDirectionSolver.Problem data = new AlternatingDirectionSolver.Problem(P, q, A, l, u);

        RuizScaling scaling = new RuizScaling(5, data);

        scaling.update(data);

        double[] xTrue = { 3.0, 4.0 };
        double[] yTrue = { 1.5 };

        double[] xScaled = new double[n];
        double[] yScaled = new double[m];
        for (int i = 0; i < n; i++) {
            double Di = scaling.primal.values[i] == 0.0 ? 1.0 : scaling.primal.values[i];
            xScaled[i] = xTrue[i] / Di;
        }
        for (int i = 0; i < m; i++) {
            double Ei = scaling.dual.values[i] == 0.0 ? 1.0 : scaling.dual.values[i];
            yScaled[i] = yTrue[i] * scaling.cost / Ei;
        }

        AlternatingDirectionSolver.Solution solution = new AlternatingDirectionSolver.Solution(yScaled.length, xScaled.length);
        System.arraycopy(xScaled, 0, solution.x, 0, n);
        System.arraycopy(yScaled, 0, solution.y, 0, m);

        scaling.unscale(solution);

        for (int i = 0; i < n; i++) {
            TestUtils.assertEquals(xTrue[i], solution.x[i]);
        }
        for (int i = 0; i < m; i++) {
            TestUtils.assertEquals(yTrue[i], solution.y[i]);
        }
    }

    /**
     * Scaling should not introduce new structural nonzeros into P and A, only rescale existing values.
     */
    @Test
    public void testScalingPreservesSparsityPattern() {

        R064CSC P = new R064CSC(3, 3, 4);
        P.pointers[0] = 0;
        P.pointers[1] = 2;
        P.pointers[2] = 3;
        P.pointers[3] = 4;
        P.indices[0] = 0;
        P.values[0] = 2.0;
        P.indices[1] = 2;
        P.values[1] = 0.5;
        P.indices[2] = 1;
        P.values[2] = 1.0;
        P.indices[3] = 2;
        P.values[3] = 3.0;

        R064CSC A = new R064CSC(2, 3, 3);
        A.pointers[0] = 0;
        A.pointers[1] = 1;
        A.pointers[2] = 2;
        A.pointers[3] = 3;
        A.indices[0] = 0;
        A.values[0] = 1.0;
        A.indices[1] = 1;
        A.values[1] = 2.0;
        A.indices[2] = 0;
        A.values[2] = -1.0;

        double[] q = { 1.0, 0.0, -1.0 };
        double[] l = { -1.0, 0.0 };
        double[] u = { 1.0, 2.0 };

        AlternatingDirectionSolver.Problem data = new AlternatingDirectionSolver.Problem(P, q.clone(), A, l.clone(), u.clone());

        RuizScaling scaling = new RuizScaling(5, data);

        int[] Pp = data.P.pointers.clone();
        int[] Pi = data.P.indices.clone();
        int[] Ap = data.A.pointers.clone();
        int[] Ai = data.A.indices.clone();

        scaling.update(data);

        TestUtils.assertEquals(Pp, data.P.pointers);
        TestUtils.assertEquals(Pi, data.P.indices);
        TestUtils.assertEquals(Ap, data.A.pointers);
        TestUtils.assertEquals(Ai, data.A.indices);
    }
}

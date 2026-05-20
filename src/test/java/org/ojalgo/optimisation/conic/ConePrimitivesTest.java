package org.ojalgo.optimisation.conic;

import java.util.Random;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.type.context.NumberContext;

/**
 * Forward-compatible tests focusing on mathematical invariants of cone primitives only.
 * They deliberately avoid assumptions about solver iteration mechanics.
 */
public class ConePrimitivesTest {

    private static final NumberContext TOL = NumberContext.of(8, 6);

    @Test
    public void testNonnegativeConeBarrierGradientFiniteDifference() {
        ConicSolver.NonnegativeCone cone = new ConicSolver.NonnegativeCone(5);
        // Interior point generation with fixed seed for reproducibility (valid hex literal)
        Random rnd = new Random(0xC0B1CL);
        R064Store x = R064Store.FACTORY.make(cone.size(), 1);
        for (int i = 0; i < cone.size(); i++) {
            // Ensure strictly interior: values in (0.1, 1.0)
            x.set(i, 0.1 + 0.9 * rnd.nextDouble());
        }
        R064Store grad = R064Store.FACTORY.make(cone.size(), 1);
        cone.barrierGradient(x, grad);
        // Finite difference check
        double eps = 1e-7;
        for (int i = 0; i < cone.size(); i++) {
            double orig = x.doubleValue(i);
            x.add(i, 0, eps);
            double fPlus = cone.barrier(x);
            x.add(i, 0, -2 * eps);
            double fMinus = cone.barrier(x);
            x.set(i, orig);
            double fd = (fPlus - fMinus) / (2 * eps);
            TestUtils.assertEquals("Gradient component mismatch at index " + i, TOL.enforce(fd), TOL.enforce(grad.doubleValue(i)));
        }
    }

    @Test
    public void testSecondOrderConeInteriorAndProjection() {
        int k = 4; // dimension >= 2
        ConicSolver.SecondOrderCone soc = new ConicSolver.SecondOrderCone(k);
        // Construct a point outside the cone then project and verify feasibility
        R064Store v = R064Store.FACTORY.make(k, 1);
        v.set(0, 0.1); // t small
        for (int i = 1; i < k; i++) {
            v.set(i, 1.0); // large u components so ||u|| > t
        }
        TestUtils.assertFalse(soc.isInterior(v));
        soc.project(v);
        double t = v.doubleValue(0);
        double uNorm2 = 0.0;
        for (int i = 1; i < k; i++) {
            double ui = v.doubleValue(i);
            uNorm2 += ui * ui;
        }
        double uNorm = Math.sqrt(uNorm2);
        // Feasibility (cone membership) relaxed: allow boundary t == ||u|| within tolerance
        TestUtils.assertTrue("Projected point not in SOC (t < ||u|| - tol)", t + 1e-9 >= uNorm);
        // Interior may not hold if projection lands exactly on boundary; that's acceptable for feasibility test
        // Barrier may be infinite if on boundary; ensure not NaN instead of insisting on finiteness
        double barrierVal = soc.barrier(v);
        TestUtils.assertFalse(Double.isNaN(barrierVal));
    }
}
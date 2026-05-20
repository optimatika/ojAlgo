package org.ojalgo.optimisation.conic;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.store.R064Store;

/**
 * Minimal KKTSystem tests: SPD no-equality solve and small equality-constrained solve.
 */
public class KKTSystemTest {

    @Test
    public void testSolveNoEqualitySPD() {
        // H = diag(2,3), rx = (-2,-3) -> dx = (-1,-1)
        R064Store H = R064Store.FACTORY.make(2, 2);
        H.set(0, 0, 2.0);
        H.set(1, 1, 3.0);
        R064Store rx = R064Store.FACTORY.make(2, 1);
        rx.set(0, -2.0);
        rx.set(1, -3.0);
        R064Store dx = R064Store.FACTORY.make(2, 1);

        KKTSystem kkt = new DenseKKTSystem(2, 0);
        boolean ok = kkt.solve(H, null, rx, null, dx, null);
        TestUtils.assertEquals(true, ok);
        TestUtils.assertEquals(-1.0, dx.doubleValue(0), 1.0e-12);
        TestUtils.assertEquals(-1.0, dx.doubleValue(1), 1.0e-12);
    }

    @Test
    public void testSolveWithEquality() {
        // Solve [I A^T; A 0][dx;y] = [0; 1] with A = [1 1]
        // Expect dx = (0.5, 0.5), y = -0.5
        R064Store H = R064Store.FACTORY.make(2, 2);
        H.set(0, 0, 1.0);
        H.set(1, 1, 1.0);
        R064Store A = R064Store.FACTORY.make(1, 2);
        A.set(0, 0, 1.0);
        A.set(0, 1, 1.0);
        R064Store rx = R064Store.FACTORY.make(2, 1); // zeros
        R064Store ry = R064Store.FACTORY.make(1, 1);
        ry.set(0, 1.0);
        R064Store dx = R064Store.FACTORY.make(2, 1);
        R064Store y = R064Store.FACTORY.make(1, 1);

        KKTSystem kkt = new DenseKKTSystem(2, 1);
        boolean ok = kkt.solve(H, A, rx, ry, dx, y);
        TestUtils.assertEquals(true, ok);
        TestUtils.assertEquals(0.5, dx.doubleValue(0), 1.0e-12);
        TestUtils.assertEquals(0.5, dx.doubleValue(1), 1.0e-12);
        TestUtils.assertEquals(-0.5, y.doubleValue(0), 1.0e-12);
    }
}

package org.ojalgo.data.proximity;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;

public class CosineTest extends DataProximityTests {

    @Test
    public void arbitraryVectors_inRangeAndSymmetric() {
        double[] a = { 0.3, -0.2, 0.5, 1.7 };
        double[] b = { -0.1, 0.0, 0.25, 0.9 };
        double ab = Similarity.cosine(a, b);
        double ba = Similarity.cosine(b, a);
        TestUtils.assertTrue(ab >= -1.0 && ab <= 1.0, "cosine must be in [-1,1]");
        TestUtils.assertEquals(ab, ba, "cosine should be symmetric");
        TestUtils.assertEquals(1.0 - ab, Distance.cosine(a, b), 1e-12);
        TestUtils.assertEquals(Distance.cosine(a, b), Distance.cosine(b, a), 1e-12);
    }

    @Test
    public void dimensionMismatch_throws() {
        double[] a = { 1, 2 };
        double[] b = { 1, 2, 3 };
        TestUtils.assertThrows(IllegalArgumentException.class, () -> Similarity.cosine(a, b));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> Distance.cosine(a, b));
    }

    @Test
    public void identicalVectors_haveCosine1_andDistance0() {
        double[] a = { 1, 2, 3 };
        double[] b = { 1, 2, 3 };
        TestUtils.assertEquals(1.0, Similarity.cosine(a, b), 1e-12);
        TestUtils.assertEquals(0.0, Distance.cosine(a, b), 1e-12);
    }

    @Test
    public void oppositeVectors_haveCosineMinus1_andDistance2() {
        double[] a = { 1, 0, 0 };
        double[] b = { -1, 0, 0 };
        TestUtils.assertEquals(-1.0, Similarity.cosine(a, b), 1e-12);
        TestUtils.assertEquals(2.0, Distance.cosine(a, b), 1e-12);
    }

    @Test
    public void orthogonalVectors_haveCosine0_andDistance1() {
        double[] a = { 1, 0, 0 };
        double[] b = { 0, 1, 0 };
        TestUtils.assertEquals(0.0, Similarity.cosine(a, b), 1e-12);
        TestUtils.assertEquals(1.0, Distance.cosine(a, b), 1e-12);
    }

    @Test
    public void zeroVector_throws() {
        double[] a = { 0, 0, 0 };
        double[] b = { 1, 2, 3 };
        TestUtils.assertThrows(IllegalArgumentException.class, () -> Similarity.cosine(a, b));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> Distance.cosine(a, b));
    }
}

package org.ojalgo.matrix;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SimilarityTest {

    @Test
    public void identicalVectors_haveCosine1_andDistance0() {
        double[] a = {1, 2, 3};
        double[] b = {1, 2, 3};
        assertEquals(1.0, Similarity.cosine(a, b), 1e-12);
        assertEquals(0.0, Similarity.cosineDistance(a, b), 1e-12);
    }

    @Test
    public void orthogonalVectors_haveCosine0_andDistance1() {
        double[] a = {1, 0, 0};
        double[] b = {0, 1, 0};
        assertEquals(0.0, Similarity.cosine(a, b), 1e-12);
        assertEquals(1.0, Similarity.cosineDistance(a, b), 1e-12);
    }

    @Test
    public void oppositeVectors_haveCosineMinus1_andDistance2() {
        double[] a = {1, 0, 0};
        double[] b = {-1, 0, 0};
        assertEquals(-1.0, Similarity.cosine(a, b), 1e-12);
        assertEquals(2.0, Similarity.cosineDistance(a, b), 1e-12);
    }

    @Test
    public void dimensionMismatch_throws() {
        double[] a = {1, 2};
        double[] b = {1, 2, 3};
        assertThrows(IllegalArgumentException.class, () -> Similarity.cosine(a, b));
        assertThrows(IllegalArgumentException.class, () -> Similarity.cosineDistance(a, b));
    }

    @Test
    public void zeroVector_throws() {
        double[] a = {0, 0, 0};
        double[] b = {1, 2, 3};
        assertThrows(IllegalArgumentException.class, () -> Similarity.cosine(a, b));
        assertThrows(IllegalArgumentException.class, () -> Similarity.cosineDistance(a, b));
    }

    @Test
    public void arbitraryVectors_inRangeAndSymmetric() {
        double[] a = {0.3, -0.2, 0.5, 1.7};
        double[] b = {-0.1, 0.0, 0.25, 0.9};
        double ab = Similarity.cosine(a, b);
        double ba = Similarity.cosine(b, a);
        assertTrue(ab >= -1.0 && ab <= 1.0, "cosine must be in [-1,1]");
        assertEquals(ab, ba, 1e-12, "cosine should be symmetric");
        assertEquals(1.0 - ab, Similarity.cosineDistance(a, b), 1e-12);
        assertEquals(Similarity.cosineDistance(a, b), Similarity.cosineDistance(b, a), 1e-12);
    }
}

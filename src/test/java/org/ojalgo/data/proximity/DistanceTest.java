package org.ojalgo.data.proximity;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;

public class DistanceTest extends DataProximityTests {

    @Test
    public void testCanberraAndJaccardAndHellingerAndHamming() {
        float[] z = new float[] { 0f, 0f };
        float[] u = new float[] { 1f, 0f };
        float[] v = new float[] { 0f, 2f };
        float[] w = new float[] { 1f, 1f };
        float[] x = new float[] { 1f, 0f, 1f };
        float[] y = new float[] { 1f, 1f, 0f };

        // Canberra
        TestUtils.assertEquals(0.0, Distance.canberra(z, z));
        TestUtils.assertEquals(1.0, Distance.canberra(u, z));
        TestUtils.assertEquals(1.0 + (1.0 / 3.0), Distance.canberra(w, v));

        // Jaccard (on non-zero indices)
        TestUtils.assertEquals(0.0, Distance.jaccard(z, z));
        TestUtils.assertEquals(2.0 / 3.0, Distance.jaccard(x, y));

        // Hellinger
        TestUtils.assertEquals(0.0, Distance.hellinger(w, w));
        TestUtils.assertEquals(1.0, Distance.hellinger(new float[] { 1f, 0f }, new float[] { 0f, 1f }));

        // Hamming
        TestUtils.assertEquals(0.0, Distance.hamming(z, z));
        TestUtils.assertEquals(1.0, Distance.hamming(u, z));
        TestUtils.assertEquals(3.0, Distance.hamming(new float[] { 1f }, new float[] { 0f, 1f, 0f }));
    }

    @Test
    public void testCorrelationDistance() {
        float[] a = new float[] { 1f, 2f, 3f, 4f };
        float[] b = new float[] { 2f, 4f, 6f, 8f }; // perfectly correlated
        float[] c = new float[] { 4f, 3f, 2f, 1f }; // perfectly anti-correlated
        float[] d = new float[] { 5f, 5f, 5f, 5f }; // constant

        TestUtils.assertEquals(0.0, Distance.correlation(a, b));
        TestUtils.assertEquals(2.0, Distance.correlation(a, c));
        // both constant => 0 by implementation
        TestUtils.assertEquals(0.0, Distance.correlation(d, d));
        // one constant => 1 by implementation (maximally dissimilar)
        TestUtils.assertEquals(1.0, Distance.correlation(a, d));
    }

    @Test
    public void testCosineAndAngular() {
        float[] a = new float[] { 1f, 0f };
        float[] b = new float[] { 0f, 1f };
        float[] c = new float[] { 2f, 0f };
        float[] z = new float[] { 0f, 0f };

        // orthogonal
        TestUtils.assertEquals(1.0, Distance.cosine(a, b));
        TestUtils.assertEquals(Math.PI / 2.0, Distance.angular(a, b));
        // identical direction
        TestUtils.assertEquals(0.0, Distance.cosine(a, c));
        TestUtils.assertEquals(0.0, Distance.angular(a, c));
        // zero vectors per implementation
        TestUtils.assertEquals(0.0, Distance.angular(z, z));
        TestUtils.assertEquals(1.0, Distance.cosine(a, z)); // defined as maximal distance when one is zero
    }

    @Test
    public void testEuclideanAndSquared() {
        float[] a = new float[] { 1f, 2f, -3f };
        float[] b = new float[] { 4f, -2f, 1f };

        double sq = Distance.squaredEuclidean(a, b);
        double eu = Distance.euclidean(a, b);

        TestUtils.assertEquals(Math.sqrt(sq), eu);
        TestUtils.assertMoreThan(0.0, sq);
        TestUtils.assertMoreThan(0.0, eu);

        // identity
        TestUtils.assertEquals(0.0, Distance.squaredEuclidean(a, a));
        TestUtils.assertEquals(0.0, Distance.euclidean(a, a));
    }

    @Test
    public void testManhattanAndChebyshev() {
        float[] a = new float[] { 1f, 2f, 3f };
        float[] b = new float[] { 3f, 0f, 1f };

        TestUtils.assertEquals(6.0, Distance.manhattan(a, b)); // |2|+|2|+|2| = 6
        TestUtils.assertEquals(2.0, Distance.chebyshev(a, b)); // max |diff| = 2
    }
}
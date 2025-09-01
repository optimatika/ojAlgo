package org.ojalgo.data.cluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;

class SpectralClusteringTest extends ClusterTests {

    private static int label(final Point p, final int perBlob) {
        return p.id < perBlob ? 0 : 1;
    }

    private static List<Point> makeTwoBlobs(final int perBlob, final long seed) {
        Point.Factory factory = Point.newFactory();
        Random rnd = new Random(seed);
        List<Point> points = new ArrayList<>(2 * perBlob);
        for (int i = 0; i < perBlob; i++) {
            float x = (float) (rnd.nextGaussian() * 0.3);
            float y = (float) (rnd.nextGaussian() * 0.3);
            points.add(factory.newPoint(x, y)); // label 0 implicit
        }
        for (int i = 0; i < perBlob; i++) {
            float x = (float) (5.0 + rnd.nextGaussian() * 0.3);
            float y = (float) (5.0 + rnd.nextGaussian() * 0.3);
            points.add(factory.newPoint(x, y)); // label 1 implicit
        }
        return points;
    }

    @Test
    void testTrivialCase() {
        List<Point> data = SpectralClusteringTest.makeTwoBlobs(1, 7L).subList(0, 2); // exactly k points
        ClusteringAlgorithm<Point> alg = FeatureBasedClusterer.newSpectral(2);
        List<Set<Point>> clusters = alg.cluster(data);
        TestUtils.assertEquals(2, clusters.size());
        int total = clusters.stream().mapToInt(Set::size).sum();
        TestUtils.assertEquals(2, total);
    }

    @Test
    void testVariantsTwoBlobs() {
        int perBlob = 40;
        List<Point> data = SpectralClusteringTest.makeTwoBlobs(perBlob, 123456789L);

        ClusteringAlgorithm<Point> alg = FeatureBasedClusterer.newSpectral(2);
        List<Set<Point>> clusters = alg.cluster(data);
        TestUtils.assertEquals("Expected 2 clusters ", 2, clusters.size());

        boolean[] majorityUsed = new boolean[2];
        for (Set<Point> cluster : clusters) {
            int count0 = 0, count1 = 0;
            for (Point p : cluster) {
                if (SpectralClusteringTest.label(p, perBlob) == 0) {
                    count0++;
                } else {
                    count1++;
                }
            }
            int majLabel = count0 >= count1 ? 0 : 1;
            int majCount = Math.max(count0, count1);
            double purity = (double) majCount / cluster.size();
            TestUtils.assertTrue("Cluster purity too low " + ": " + purity, purity > 0.75);
            TestUtils.assertTrue("Duplicate majority label ", !majorityUsed[majLabel]);
            majorityUsed[majLabel] = true;
        }

    }
}

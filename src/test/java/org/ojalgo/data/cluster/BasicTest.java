package org.ojalgo.data.cluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;

public class BasicTest extends ClusterTests {

    static final int K = 2;

    final Set<Point> cluster1;
    final Set<Point> cluster2;

    final List<Point> points = new ArrayList<>();

    BasicTest() {

        super();

        Point.Factory factory = Point.newFactory(K);
        cluster1 = Set.of(factory.newPoint(1, 0), factory.newPoint(1, 4), factory.newPoint(1, 3), factory.newPoint(1, 1), factory.newPoint(1, 2));
        cluster2 = Set.of(factory.newPoint(9, 0), factory.newPoint(9, -4), factory.newPoint(9, -3), factory.newPoint(9, -1), factory.newPoint(9, -2));

        points.addAll(cluster1);
        points.addAll(cluster2);
    }

    @Test
    void testAuto() {

        List<Set<Point>> autoconfig = Point.cluster(points);
        if (DEBUG) {
            this.printClusters("autoconfig", autoconfig);
        }
        TestUtils.assertEquals(K, autoconfig.size());
        TestUtils.assertTrue(autoconfig.contains(cluster1));
        TestUtils.assertTrue(autoconfig.contains(cluster2));
    }

    @Test
    void testGready() {

        double threshold = 18.0;
        // max sum of squares within cluster is 4^2 = 16.0
        // min sum of squares between clusters is 8^2 = 64.0
        List<Set<Point>> greedy = Point.newGreedyClusterer(threshold).cluster(points);

        if (DEBUG) {
            this.printClusters("greedy", greedy);
        }
        TestUtils.assertEquals(K, greedy.size());
        TestUtils.assertTrue(greedy.contains(cluster1));
        TestUtils.assertTrue(greedy.contains(cluster2));
    }

    @Test
    void testKMeans() {

        List<Set<Point>> kmeans = Point.newKMeansClusterer(K).cluster(points);

        if (DEBUG) {
            this.printClusters("k-means", kmeans);
        }
        TestUtils.assertEquals(K, kmeans.size());
        TestUtils.assertTrue(kmeans.contains(cluster1));
        TestUtils.assertTrue(kmeans.contains(cluster2));
    }

}

package org.ojalgo.data.cluster;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.netio.BasicLogger;

public class GreedyClusteringTest extends ClusterTests {

    @Test
    void testBasic() {

        // (1, 1), (2, 1), (10, 10), (11, 11), (2, 2), (12, 10)

        // With a threshold of  2.5 , the output could be:

        //    Cluster 1: [(1.0, 1.0), (2.0, 1.0), (2.0, 2.0)]
        //    Cluster 2: [(10.0, 10.0), (11.0, 11.0), (12.0, 10.0)]

        // Sample points

        Set<ClusterTests.Point> cluster1 = Set.of(new ClusterTests.Point(1, 1), new ClusterTests.Point(2, 1), new ClusterTests.Point(2, 2));
        Set<ClusterTests.Point> cluster2 = Set.of(new ClusterTests.Point(10, 10), new ClusterTests.Point(11, 11), new ClusterTests.Point(12, 10));

        Set<ClusterTests.Point> points = new HashSet<>();
        points.addAll(cluster1);
        points.addAll(cluster2);

        double threshold = 2.5; // Maximum distance for points to be in the same cluster

        List<Set<ClusterTests.Point>> clusters = GreedyClustering.cluster(points, ClusterTests.Point::distance, threshold);

        if (DEBUG) {
            // Print clusters
            for (int i = 0; i < clusters.size(); i++) {
                BasicLogger.debug("Cluster " + (i + 1) + ": " + clusters.get(i));
            }
        }

        TestUtils.assertEquals(2, clusters.size());
        TestUtils.assertTrue(clusters.contains(cluster1));
        TestUtils.assertTrue(clusters.contains(cluster2));
    }

}

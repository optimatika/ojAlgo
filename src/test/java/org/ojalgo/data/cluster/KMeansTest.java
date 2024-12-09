package org.ojalgo.data.cluster;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

public class KMeansTest extends ClusterTests {

    @Test
    void testBasic() {

        // (1, 1), (2, 1), (4, 3), (5, 4), (8, 7), (7, 8)

        // k = 2

        //    Cluster 1: [(1.0, 1.0), (2.0, 1.0), (4.0, 3.0), (5.0, 4.0)]
        //    Cluster 2: [(8.0, 7.0), (7.0, 8.0)]

        // Sample points
        List<KMeansTest.Point> points = List.of(new KMeansTest.Point(1, 1), new KMeansTest.Point(2, 1), new KMeansTest.Point(4, 3), new KMeansTest.Point(5, 4),
                new KMeansTest.Point(8, 7), new KMeansTest.Point(7, 8));

        int k = 2; // Number of clusters
        int maxIterations = 100;

        List<Set<KMeansTest.Point>> clusters = KMeans.cluster(points, KMeansTest.Point::mean, KMeansTest.Point::distance, k, maxIterations);

        // Print clusters
        for (int i = 0; i < clusters.size(); i++) {
            System.out.println("Cluster " + (i + 1) + ": " + clusters.get(i));
        }
    }

}
